import model.City;
import model.Installation;
import model.Monitor;
import model.Parameter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Application {

    private static Map<Integer, City> citiesMap;
    private static Map<Long, Installation> installationsMap;
    private static Monitor monitor;

    public static void citiesOptions() {
        citiesMap = new HashMap<>();
        System.out.println("Choose cities from the list. Enter valid number: ");
        City[] cities = City.getAll().toArray(new City[0]);
        for (int i = 0; i < cities.length; i++) {
            System.out.println(i+1+" - "+cities[i].getName());
            citiesMap.put(i+1, cities[0]);
        }
    }

    public static void installationsOptions(City city) {
        installationsMap = new HashMap<>();
        System.out.println("Choose installations from the list. Enter valid number: ");
        List<Installation> installations = monitor.getInstallationsFromCity(city);
        installations.forEach(installation -> {
            System.out.println(installation.getId()+" - "+installation);
            installationsMap.put(installation.getId(), installation);
        });

    }

    public static void main(String[] args) throws IOException {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);

        SparkConf sparkConf = new SparkConf().setAppName("AirlyApp").setMaster("local[4]");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        monitor = new Monitor(sparkContext);

        monitor.readInstallationAndDownloadMeasurements();

        monitor.readMeasuremensts();

        Scanner scanner = new Scanner(System.in);
        citiesOptions();
        int chosenCity = scanner.nextInt();
        City city = citiesMap.get(chosenCity);
        installationsOptions(city);
        long chosenInstallation = scanner.nextLong();
        Installation installation = installationsMap.get(chosenInstallation);
        Parameter.getAll().forEach(parameter -> {
            System.out.println("PARAMETER: "+parameter.getName());
            monitor.showMeasurementsFromInstallation(parameter, installation);
        });

        sparkContext.close();
    }
}
