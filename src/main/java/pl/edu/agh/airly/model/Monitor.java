package pl.edu.agh.airly.model;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import pl.edu.agh.airly.comparator.CityDoublePairComparator;
import pl.edu.agh.airly.comparator.InstallationMeasurementPairComparator;
import pl.edu.agh.airly.comparator.IntDoublePairComparator;
import pl.edu.agh.airly.comparator.MeasurementComparator;
import pl.edu.agh.airly.connection.Tester;
import pl.edu.agh.airly.download.InstallationDataProvider;
import pl.edu.agh.airly.download.MeasurementDataProvider;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Monitor implements Serializable {
    private Map<City, JavaRDD<Installation>> installations;
    private Map<Installation, JavaRDD<Measurement>> measurements;
    private JavaRDD<Measurement> joinedMeasurements;
    private InstallationDataProvider installationDataProvider;
    private MeasurementDataProvider measurementsDataProvider;
    private transient JavaSparkContext sparkContext;
    private transient Tester tester;

    public Monitor(JavaSparkContext sparkContext) {
        this.installationDataProvider = new InstallationDataProvider(sparkContext);
        this.measurementsDataProvider = new MeasurementDataProvider(sparkContext);
        this.installations = new HashMap<>();
        this.measurements = new HashMap<>();
        this.joinedMeasurements = sparkContext.emptyRDD();
        this.sparkContext = sparkContext;
        this.tester = new Tester();
    }

    public void downloadInstallations() {
        if (!tester.testConnection()) return;
        City.getAll().forEach(city -> {
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
        installations.values().forEach(installations -> {
                    installations
                            .collect()
                            .forEach(installation -> {
                                measurementsDataProvider.setInstallationId(installation.getId());
                                if (!measurementsDataProvider.isUptodate()) {
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

    public void readMeasurements() {
        installations.values().forEach(installations -> {
                    installations
                            .collect()
                            .forEach(installation -> {
                                measurementsDataProvider.setInstallationId(installation.getId());
                                measurementsDataProvider
                                        .readData()
                                        .ifPresent(measurementJavaRDD ->
                                                measurements.put(installation, measurementJavaRDD)

                                        );

                            });
                }
        );
    }

    public void readInstallations() {
        City.getAll().forEach(city -> {
            installationDataProvider.setCity(city);
            installationDataProvider
                    .readData()
                    .ifPresent(installationJavaRDD -> {
                        installations.put(city, installationJavaRDD);
                        System.out.println(city.getName());
                    });
        });
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

        Boolean exists = measurements.keySet().stream().anyMatch(inst -> inst.equals(installation));
        if (exists) {
            JavaRDD<Measurement> selected = measurements.get(installation);
            names = Optional.of(selected
                    .map(Measurement::getParamName)
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


    public List<Measurement> getParameterMeasurementsFromInstallation(Parameter parameter, Installation installation) {
        if (parameter == null || installation == null) return Collections.emptyList();
        System.out.println("INSTALLATION: " + installation.getId());
        return measurements
                .get(installation)
                .filter(measurement -> measurement.getParamName().equals(parameter.getName()))
                .collect();
    }

    public Measurement getMaxMeasurement(Parameter parameter, Installation installation) {
        if (parameter == null || installation == null) return null;
        return measurements
                .get(installation)
                .filter(m -> m.getParamName().equals(parameter.getName()))
                .max(new MeasurementComparator());
    }

    public List<String> getDistinctDatesFromInstallation(Installation installation) {
        if (installation == null) return null;
        return measurements
                .get(installation)
                .map(Measurement::getFromDateTime)
                .distinct()
                .sortBy(m -> m, true, 10)
                .collect();
    }

    private Measurement getMaxMeasurement(JavaRDD<Measurement> measurements, Parameter parameter, String fromDateTime, String tillDateTime) {
        return measurements
                .filter(m -> m.getParamName().equals(parameter.getName()))
                .filter(m -> CharSequence.compare(m.getFromDateTime(), fromDateTime) >= 0 && CharSequence.compare(m.getFromDateTime(), tillDateTime) <= 0)
                .max(new MeasurementComparator());
    }

    public List<Pair<Installation, Measurement>> findInstallationsWithHighestValues(Parameter parameter, String fromDateTime, String tillDateTime) {
        return measurements
                .entrySet()
                .stream()
                .map(entry ->
                        new ImmutablePair<>(entry.getKey(), getMaxMeasurement(entry.getValue(), parameter, fromDateTime, tillDateTime)))
                .filter(pair -> pair.right != null)
                .sorted(new InstallationMeasurementPairComparator())
                .limit(10)
                .collect(Collectors.toList());
    }

    private JavaRDD<Measurement> joinMeasurement(JavaRDD<Installation> installations) {
        JavaRDD<Measurement> joinedMeasurement = sparkContext.emptyRDD();
        installations
                .foreach(
                        installation -> {
                            JavaRDD<Measurement> tmpMeasurements = measurements.getOrDefault(installation, null);
                            joinedMeasurement.union(tmpMeasurements);
                        }
                );
        return joinedMeasurement;
    }

    private Double getAverageValue(Iterable<Measurement> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.averagingDouble(Measurement::getValue));
    }

    private Double getAverageValue(JavaRDD<Measurement> measurements) {
        return sparkContext.parallelizeDoubles(measurements
                .groupBy(Measurement::getHour)
                .map(group -> getAverageValue(group._2)).collect())
                .mean();
    }


    public List<Pair<City, Double>> findCitiesWithHighestAverageValues(Parameter parameter) {
        return installations
                .entrySet()
                .stream()
                .map(entry -> {
                    JavaRDD<Measurement> joinedMeasurements = joinMeasurement(entry.getValue());
                    return new ImmutablePair<>(entry.getKey(), getAverageValue(joinedMeasurements));
                    })
                .filter(pair -> pair.right != null)
                .sorted(new CityDoublePairComparator())
                .limit(10)
                .collect(Collectors.toList());
    }

    public Pair<Integer, Double> getHourWithTheHighestAverage(City city, Parameter parameter) {
        JavaRDD<Measurement> joinedMeasurement = joinMeasurement(installations
                                    .get(city));

        return joinedMeasurement
                .groupBy(Measurement::getHour)
                .map(group -> new ImmutablePair<>(group._1, getAverageValue(group._2)))
                .max(new IntDoublePairComparator());


    }

}
