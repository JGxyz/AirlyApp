package model;

import download.InstallationDataProvider;
import download.MeasurementDataProvider;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Monitor implements Serializable {
    public Map<City, JavaRDD<Installation>> installations;
    public Map<Long, JavaRDD<Measurement>> measurements;
    public InstallationDataProvider installationDataProvider;
    public MeasurementDataProvider measurementsDataProvider;

    public Monitor(JavaSparkContext sparkContext) {
        this.installationDataProvider = new InstallationDataProvider(sparkContext);
        this.measurementsDataProvider = new MeasurementDataProvider(sparkContext);
        this.installations = new HashMap<>();
    }

    public void downloadInstallationsData() {
        City.getAll().forEach( city -> {
                    installationDataProvider.setCity(city);
                    installationDataProvider.downloadData();
                }
        );
    }

    public void readInstallationAndDownloadMeasurements() {
        readInstallationData();
        installations.values().forEach( installations -> {
                    installations
                            .foreach(installation -> {
                                measurementsDataProvider.setInstallationId(installation.getId());
                                measurementsDataProvider.downloadData();
                            });
                }
        );
   }

   public void readMeasuremensts() {
       installations.values().forEach( installations -> {
                   installations
                           .foreach(installation -> {
                                measurementsDataProvider.setInstallationId(installation.getId());
                                JavaRDD<Measurement> tempMeasurements = measurementsDataProvider.readData();
                                measurements.put(installation.getId(), tempMeasurements);

                           });
               }
       );
   }

    public void readInstallationData() {
        City.getAll().forEach(city -> {
            installationDataProvider.setCity(city);
            JavaRDD<Installation> tempInstallations = installationDataProvider.readData();
            installations.put(city, tempInstallations);

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

}
