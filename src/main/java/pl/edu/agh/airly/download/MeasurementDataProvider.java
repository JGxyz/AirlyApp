package pl.edu.agh.airly.download;

import pl.edu.agh.airly.model.Measurement;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.util.Arrays;

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
            ds.write().mode("append").json(getResourcePath());
        }
    }

    @Override
    public String getResourcePath() {
        return "/home/jolanta/AGH/AirlyApp/src/main/resources/measurements/"+installationId+"_measurement.json";
    }


}
