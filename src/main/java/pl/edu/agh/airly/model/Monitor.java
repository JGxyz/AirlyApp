package pl.edu.agh.airly.model;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import pl.edu.agh.airly.comparator.MeasurementComparator;
import pl.edu.agh.airly.connection.Tester;
import pl.edu.agh.airly.download.InstallationDataProvider;
import pl.edu.agh.airly.download.MeasurementDataProvider;
import pl.edu.agh.airly.visitor.IMonitorVisitor;
import pl.edu.agh.airly.visitor.Visitable;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Monitor implements Serializable, Visitable {
    public Map<City, JavaRDD<Installation>> installations;
    public Map<Long, JavaRDD<Measurement>> measurements;
    public InstallationDataProvider installationDataProvider;
    public MeasurementDataProvider measurementsDataProvider;
    public transient JavaSparkContext sparkContext;
    public transient Tester tester;

    public Monitor(JavaSparkContext sparkContext) {
        this.installationDataProvider = new InstallationDataProvider(sparkContext);
        this.measurementsDataProvider = new MeasurementDataProvider(sparkContext);
        this.installations = new HashMap<>();
        this.measurements = new HashMap<>();
        this.sparkContext = sparkContext;
        this.tester = new Tester();
    }

    public void downloadInstallations() {
        if (!tester.testConnection()) return;
        City.getAll().forEach( city -> {
                    installationDataProvider.setCity(city);
                    installationDataProvider.downloadData();
                }
        );
    }

    public void readInstallationAndDownloadMeasurements() {
        if (!tester.testConnection()) {
            readInstallations();
            return;
        }
        readInstallations();
        installations.values().forEach( installations -> {
                    installations
                            .collect()
                            .forEach(installation -> {
                                measurementsDataProvider.setInstallationId(installation.getId());
                                if (!measurementsDataProvider.isUptodate()){
                                    measurementsDataProvider.downloadData();
                                    try {
                                        Thread.sleep(1201);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                }
        );
   }

   public void readMeasuremensts() {
       installations.values().forEach( installations -> {
                   installations
                           .collect()
                           .forEach(installation -> {
                                measurementsDataProvider.setInstallationId(installation.getId());
                                measurementsDataProvider
                                        .readData()
                                        .ifPresent(measurementJavaRDD -> measurements.put(installation.getId(), measurementJavaRDD));

                           });
               }
       );
   }

    public void readInstallations() {
        City.getAll().forEach(city -> {
            installationDataProvider.setCity(city);
            installationDataProvider
                    .readData()
                    .ifPresent(installationJavaRDD -> {installations.put(city, installationJavaRDD);
                        System.out.println(city.getName());
                    });
        });
    }

    public void showInstallationsFromCity(City city) {
        System.out.println("INSTALLATIONS FROM CITY: "+city.getName());
        getInstallationsFromCity(city).forEach(System.out::println);
    }

    public List<Installation> getInstallationsFromCity(City city) {
        return installations
                .get(city)
                .collect();
    }

    public Optional<List<Parameter>> getParametersFromInstallation(Installation installation) {
        Optional<List<String>> names = Optional.empty();
        Optional<List<Parameter>> parameters = Optional.empty();

        if (installation == null) return parameters;

        Boolean exists = measurements.keySet().stream().anyMatch(id -> id == installation.getId());
        if(exists){
            JavaRDD<Measurement> selected = measurements.get(installation.getId());
            names = Optional.of(selected
                    .map(measurement -> measurement.getParamName())
                    .distinct()
                    .collect());

        }

        if (names.isPresent()) {
            Optional<List<String>> finalNames = names;
            parameters = Optional.of(Parameter
                                        .getAll()
                                        .stream()
                                        .filter(parameter -> finalNames.get().contains(parameter.getName()))
                                        .collect(Collectors.toList()));
        }

        return parameters;
    }

    public void showMeasurementsFromInstallation(Parameter param, Installation installation) {
        JavaRDD<Measurement> selected = measurements.get(installation.getId());
        selected.filter(measurement -> measurement.getParamName().equals(param.getName()))
                .collect()
                .forEach(System.out::println);
    }

    public long countInstallations() {
        return installations
                .entrySet()
                .stream()
                .mapToLong(el -> el.getValue().count())
                .sum();
    }

    public List<Measurement> getParameterMeasurementsFromInstallation(Parameter parameter, Installation installation) {
        if (parameter == null || installation == null) return Collections.emptyList();
        System.out.println("INSTALLATION: "+installation.getId());
        return measurements
                .get(installation.getId())
                .filter(measurement -> measurement.getParamName().equals(parameter.getName()))
                .collect();
    }

    public Measurement getMaxMeasurement(Parameter parameter, Installation installation) {
        if (parameter == null || installation == null) return null;
        return measurements
                .get(installation.getId())
                .filter(m -> m.getParamName().equals(parameter.getName()))
                .max(new MeasurementComparator());
    }

    public List<String> getDistinctDatesFromInstallation(Installation installation) {
        if (installation == null) return null;
        return measurements
                .get(installation.getId())
                .map(m -> m.getFromDateTime())
                .distinct()
                .sortBy(m -> m, true, 10)
                .collect();
    }

    @Override
    public void accept(IMonitorVisitor visitor) {
        visitor.visit(this);
    }
}
