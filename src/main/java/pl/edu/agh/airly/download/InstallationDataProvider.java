package pl.edu.agh.airly.download;

import pl.edu.agh.airly.model.City;
import pl.edu.agh.airly.model.Installation;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;

import java.io.Serializable;
import java.util.Arrays;

public class InstallationDataProvider extends AirDataProvider<Installation> implements Serializable {
    private City city;

    public InstallationDataProvider(JavaSparkContext sparkContext) {
        super(sparkContext);
    }

    public InstallationDataProvider(){
        super();
    }

    public void setCity(City city) {
        this.city = city;
    }

    @Override
    public String getUrl() {
        return "https://airapi.airly.eu/v2/installations/nearest?lat="+city.getLatitude()+"&lng="+city.getLongitude()+"&maxDistanceKM=5&maxResults=50";
    }

    @Override
    public Encoder<Installation> getEncoder() {
        return Encoders.bean(Installation.class);
    }

    @Override
    protected void saveContentToFile(String content) {
        Dataset<String> tempDS = sqlContext.createDataset(Arrays.asList(content), Encoders.STRING());
        Dataset<Row> installationsDS = sqlContext.read().json(tempDS);
        JavaRDD<Installation> installations = installationsDS.toJavaRDD().map(r -> new Installation(/*id*/r.getAs("id"),
                /*latitude*/((Row)r.getAs("location")).getAs("latitude"),
                /*longitude*/((Row)r.getAs("location")).getAs("longitude"),
                /*city*/((Row)r.getAs("address")).getAs("city"),
                /*country*/((Row)r.getAs("address")).getAs("country"),
                /*number*/((Row)r.getAs("address")).getAs("number"),
                /*street*/((Row)r.getAs("address")).getAs("street")));
        Dataset<Row> ds = sqlContext.createDataFrame(installations, Installation.class);
        ds.write().mode("overwrite").json(getResourcePath());
    }

    @Override
    public String getResourcePath() {
        return "/home/jolanta/AGH/AirlyApp/src/main/resources/installations/"+city.getNameWithoutPolishSigns()+"_installations.json";
    }

}
