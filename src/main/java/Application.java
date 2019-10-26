import download.IDataProvider;
import download.InstallationDataProvider;
import model.Monitor;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class Application {
    public static void main(String[] args) {
        IDataProvider dataProvider = new InstallationDataProvider();
        dataProvider.updateData();
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);

        SparkConf sparkConf = new SparkConf().setAppName("AirlyApp").setMaster("local[4]");
        SparkContext sparkContext = new SparkContext(sparkConf);

        Monitor monitor = new Monitor(sparkContext, dataProvider);

        System.out.println(monitor.countInstallations());

    }
}
