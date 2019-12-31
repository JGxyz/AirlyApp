package pl.edu.agh.airly;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import pl.edu.agh.airly.controller.AppController;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;

    private AppController appController;

    public static void main(String[] args) throws IOException {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AirlyApp");
        SparkConf sparkConf = new SparkConf().setAppName("AirlyApp").setMaster("local[*]");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        this.appController = new AppController(primaryStage, sparkContext);
        this.appController.initRootLayout();
    }
}
