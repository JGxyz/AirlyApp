package pl.edu.agh.airly.model;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import pl.edu.agh.airly.comparator.*;
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
    private InstallationDataProvider installationDataProvider;
    private MeasurementDataProvider measurementsDataProvider;
    private transient JavaSparkContext sparkContext;
    private transient Tester tester;

    public Monitor(JavaSparkContext sparkContext) {
        this.installationDataProvider = new InstallationDataProvider(sparkContext);
        this.measurementsDataProvider = new MeasurementDataProvider(sparkContext);
        this.installations = new HashMap<>();
        this.measurements = new HashMap<>();
        this.sparkContext = sparkContext;
        this.tester = new Tester();
    }

    public void addInstallations(City city, JavaRDD<Installation> installationRDD) {
        this.installations.put(city, installationRDD);
    }

    public void addMeasurements(Installation installation, JavaRDD<Measurement> measurementRDD) {
        this.measurements.put(installation, measurementRDD);
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

    private Optional<Measurement> getMaxMeasurement(JavaRDD<Measurement> measurements, Parameter parameter, String fromDateTime, String tillDateTime) {
        List<Measurement> selected = measurements
                .filter(m -> m.getParamName().equals(parameter.getName()))
                .filter(m -> CharSequence.compare(m.getFromDateTime(), fromDateTime) >= 0 && CharSequence.compare(m.getFromDateTime(), tillDateTime) <= 0)
                .collect();
        if (selected != null && !selected.isEmpty())
                return Optional.of(sparkContext.parallelize(selected)
                        .max(new MeasurementComparator()));
        else return Optional.empty();
    }

    public List<Pair<Installation, Measurement>> findInstallationsWithValuesAboveStandard(Parameter parameter, String fromDateTime, String tillDateTime) {
        return measurements
                .entrySet()
                .stream()
                .map(entry ->
                        new ImmutablePair<>(entry.getKey(), getMaxMeasurement(entry.getValue(), parameter, fromDateTime, tillDateTime)))
                .filter(pair -> pair.right.isPresent() && pair.right.get().getValue() >= parameter.getStandard())
                .map(pair -> new ImmutablePair<>(pair.left, pair.right.get()))
                .sorted(new InstallationMeasurementPairComparator())
                .limit(10)
                .collect(Collectors.toList());
    }

    JavaRDD<Measurement> getMeasurements(Installation installation) {
        return measurements.get(installation);
    }

    public long countMeasurement(City city) {
        JavaRDD<Installation> installationsRDD = installations.get(city);

        Optional<Long> optionalLong = installationsRDD
                .collect()
                .stream()
                .map(installation ->
                    measurements.get(installation).count())
                .reduce((x, y) -> x+y);

        if (optionalLong.isPresent()) {
            return optionalLong.get();
        } else {
            return  0;
        }
    }

    private JavaRDD<Measurement> joinMeasurement(JavaRDD<Installation> installations) {
        JavaRDD<Measurement> joinedMeasurement = null;
        for (Installation installation : installations.collect()) {
            JavaRDD<Measurement> tmpMeasurements = measurements.getOrDefault(installation, null);
            if (tmpMeasurements == null) continue;
            if (joinedMeasurement == null || joinedMeasurement.isEmpty()) {
                joinedMeasurement = tmpMeasurements;
            }
            else
                joinedMeasurement = joinedMeasurement.union(tmpMeasurements);
        }
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
                    JavaRDD<Measurement> joinedMeasurements = joinMeasurement(entry.getValue()).filter(measurement -> measurement.getParamName().equals(parameter.getName()));
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
                .filter(measurement -> measurement.getParamName().equals(parameter.getName()))
                .groupBy(Measurement::getHour)
                .map(group -> new ImmutablePair<>(group._1, getAverageValue(group._2)))
                .max(new IntDoublePairComparator());

    }

    public List<String> getAllDates() {
        Set<String> dates = new HashSet<>();

        for (JavaRDD<Measurement> measurementJavaRDD : measurements.values()) {
            List<String> fromDateTime = measurementJavaRDD
                    .map(measurement -> measurement.getFromDateTime())
                    .collect();
            if (fromDateTime.size() > 0)
                dates.addAll(fromDateTime);
        }

        List<String> uniqueDates = new LinkedList<String>(dates);

        uniqueDates.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return CharSequence.compare(o1, o2);
            }
        });

        return uniqueDates;
    }

}
