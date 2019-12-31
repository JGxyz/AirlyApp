package pl.edu.agh.airly.download;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import pl.edu.agh.airly.comparator.MeasurementDateComparator;
import pl.edu.agh.airly.model.Measurement;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

public class MeasurementDataProvider extends AirDataProvider<Measurement> implements Serializable {
    private long installationId;

    public MeasurementDataProvider(JavaSparkContext sparkContext) {
        super(sparkContext);
    }

    public MeasurementDataProvider() {
        super();
    }

    public void setInstallationId(long installationId) {
        this.installationId = installationId;
    }

    @Override
    public String getUrl() {
        return "https://airapi.airly.eu/v2/measurements/installation?installationId="+installationId;
    }

    @Override
    public Encoder<Measurement> getEncoder() {
        return Encoders.bean(Measurement.class);
    }

    @Override
    protected void saveContentToFile(String content) {
        Dataset<String> tempDS = sqlContext.createDataset(Arrays.asList(content), Encoders.STRING());
        Dataset<Row> measurementsDS = sqlContext.read().json(tempDS);
        Dataset<Row> history = measurementsDS.select(org.apache.spark.sql.functions.explode(measurementsDS.col("history")));
        Dataset<Row> values = history.select(history.col("col.fromDateTime"), history.col("col.tillDateTime"), org.apache.spark.sql.functions.explode(history.col("col.values")).as("values"));
        if (values.count() != 0) {
            Dataset<Row> finalDS = values.select(values.col("fromDateTime"),
                    values.col("tillDateTime"),
                    values.col("values.name"),
                    values.col("values.value"));
            finalDS.show();
            JavaRDD<Measurement> measurements = finalDS.toJavaRDD().map(row -> new Measurement(row.getAs("fromDateTime"), row.getAs("tillDateTime"), row.getAs("name"), row.getAs("value"), installationId));
            Dataset<Row> ds = sqlContext.createDataFrame(measurements, Measurement.class);
            Optional<Measurement> latest = getLatestMeasurement();
            if (latest.isPresent())
                ds = ds.where(ds.col("fromDateTime").gt(latest.get().getFromDateTime()));
            ds.write().mode("append").json(getResourcePath());
        }
    }

    private Optional<Measurement> getLatestMeasurement() {
        Optional<Measurement> measurement = Optional.empty();
        Optional<JavaRDD<Measurement>> measurementJavaRDD = readData();
        if (measurementJavaRDD.isPresent()) {
            measurement = Optional.of(measurementJavaRDD.get().max(new MeasurementDateComparator()));
        }
        return measurement;
    }

    @Override
    public String getResourcePath() {
        return "/home/jolanta/AGH/AirlyApp/src/main/resources/measurements/"+installationId+"_measurement.json";
    }


}
