package model;

import download.IDataProvider;
import org.apache.orc.impl.HadoopShims;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import scala.Tuple2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Monitor {
    public JavaRDD<Installation> installations;

    public Monitor(SparkContext sparkContext, IDataProvider dataProvider) {
        SQLContext sqlContext = new SQLContext(sparkContext);
        Dataset<Row> installationsDS = sqlContext.read().json(dataProvider.getResourcesPath());
        installationsDS.printSchema();
        this.installations = installationsDS.toJavaRDD().map(r -> new Installation(/*id*/r.getAs("id"),
                /*latitude*/((Row)r.getAs("location")).getAs("latitude"),
                /*longitude*/((Row)r.getAs("location")).getAs("longitude"),
                /*city*/((Row)r.getAs("address")).getAs("city"),
                /*country*/((Row)r.getAs("address")).getAs("country"),
                /*number*/((Row)r.getAs("address")).getAs("number"),
                /*street*/((Row)r.getAs("address")).getAs("street")));
    }

    public long countInstallations() {
        return installations.count();
    }

    public void groupByStreet() {
        installations
                .groupBy(installation -> installation.getStreet())
                .collect()
                .forEach(tuple -> {System.out.println(tuple._1 + ":\n");
                StreamSupport.stream(tuple._2.spliterator(), false).forEach(System.out::print);
                });
    }
}
