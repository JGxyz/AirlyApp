package pl.edu.agh.airly.model;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MonitorTest {
    private Monitor monitor;

    @Before
    public void setMonitor() {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);

        SparkConf sparkConf = new SparkConf().setAppName("AirlyApp").setMaster("local[*]");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        this.monitor = new Monitor(sparkContext);
        Installation b1 = new Installation(9983, 53.135394,23.130144,"Bialystok",
                "Poland", "11", "Zwyciestwa");
        Installation b2 = new Installation(7978, 53.114879, 23.194593, "Bialystok", "Poland", "16", "Żubrów");
        Installation b3 = new Installation(2920, 53.15586, 23.200312, "Bialystok", "Poland", "5", "Trawiasta");

        Installation w1 = new Installation(787,51.072434,17.006976,"Wrocław","Poland","8","aleja Karkonoska");
        Installation w2 = new Installation(7901, 51.09364, 16.97707, "Wrocław", "Poland", "7A","Nasturcjowa");
        Installation w3 = new Installation(2141, 51.127037, 16.978491, "Wrocław", "Poland", "9", "Tokarska");

        Installation k1 = new Installation(2227,50.304292,19.033288,"Katowice","Poland","8","1 Maja");
        Installation k2 = new Installation(43, 50.264611, 18.975028, "Katowice", "Poland", "7A","Sławka");
        Installation k3 = new Installation(1011, 50.308643, 19.006968, "Katowice", "Poland", "9", "Władysława Reymonta");


        JavaRDD<Installation> bialystok = sparkContext.parallelize(Arrays.asList(b1, b2, b3));
        JavaRDD<Installation> wroclaw = sparkContext.parallelize(Arrays.asList(w1,w2,w3));
        JavaRDD<Installation> katowice = sparkContext.parallelize(Arrays.asList(k1,k2,k3));

        monitor.addInstallations(City.BIALYSTOK, bialystok);
        monitor.addInstallations(City.WROCLAW, wroclaw);
        monitor.addInstallations(City.KATOWICE, katowice);

        Measurement b11 = new Measurement("2019-12-29T23:00:00.000Z","2019-12-30T00:00:00.000Z",
                "PM10",  46.0, 9983);
        Measurement b12 = new Measurement("2019-12-30T23:00:00.000Z","2019-12-31T00:00:00.000Z",
                "PM10",  51.0, 9983);
        Measurement b13 = new Measurement("2019-12-30T22:00:00.000Z","2019-12-30T23:00:00.000Z",
                "PM10",  44.0, 9983);

        Measurement b21 = new Measurement("2019-12-29T23:00:00.000Z","2019-12-30T00:00:00.000Z",
                "PM10",  46.0, 7978);
        Measurement b22 = new Measurement("2019-12-29T22:00:00.000Z","2019-12-29T23:00:00.000Z",
                "PM10",  51.0, 7978);
        Measurement b23 = new Measurement("2019-12-30T22:00:00.000Z","2019-12-30T23:00:00.000Z",
                "PM10",  44.0, 7978);

        Measurement b31 = new Measurement("2019-12-29T12:00:00.000Z","2019-12-29T13:00:00.000Z",
                "PM10",  40.0, 2920);
        Measurement b32 = new Measurement("2019-12-29T22:00:00.000Z","2019-12-29T23:00:00.000Z",
                "PM10",  54.0, 2920);
        Measurement b33 = new Measurement("2019-12-30T14:00:00.000Z","2019-12-30T15:00:00.000Z",
                "PM10",  47.0, 2920);

        Measurement w11 = new Measurement("2019-12-29T23:00:00.000Z","2019-12-30T00:00:00.000Z",
                "PM10",  55.0, 787);
        Measurement w12 = new Measurement("2019-12-30T23:00:00.000Z","2019-12-31T00:00:00.000Z",
                "PM10",  50.0, 787);
        Measurement w13 = new Measurement("2019-12-30T22:00:00.000Z","2019-12-30T23:00:00.000Z",
                "PM10",  34.0, 787);

        Measurement w21 = new Measurement("2019-12-29T23:00:00.000Z","2019-12-30T00:00:00.000Z",
                "PM10",  47.0, 7901);
        Measurement w22 = new Measurement("2019-12-29T22:00:00.000Z","2019-12-29T23:00:00.000Z",
                "PM10",  48.0, 7901);
        Measurement w23 = new Measurement("2019-12-30T22:00:00.000Z","2019-12-30T23:00:00.000Z",
                "PM10",  44.0, 7901);

        Measurement w31 = new Measurement("2019-12-29T12:00:00.000Z","2019-12-29T13:00:00.000Z",
                "PM10",  40.0, 2141);
        Measurement w32 = new Measurement("2019-12-29T22:00:00.000Z","2019-12-29T23:00:00.000Z",
                "PM10",  53.0, 2141);
        Measurement w33 = new Measurement("2019-12-30T14:00:00.000Z","2019-12-30T15:00:00.000Z",
                "PM10",  45.0, 2141);

        Measurement k11 = new Measurement("2019-12-29T23:00:00.000Z","2019-12-30T00:00:00.000Z",
                "PM10",  50.0, 2227);
        Measurement k12 = new Measurement("2019-12-30T23:00:00.000Z","2019-12-31T00:00:00.000Z",
                "PM10",  51.0, 2227);
        Measurement k13 = new Measurement("2019-12-30T22:00:00.000Z","2019-12-30T23:00:00.000Z",
                "PM10",  32.0, 2227);

        Measurement k21 = new Measurement("2019-12-29T23:00:00.000Z","2019-12-30T00:00:00.000Z",
                "PM10",  49.0, 43);
        Measurement k22 = new Measurement("2019-12-29T22:00:00.000Z","2019-12-29T23:00:00.000Z",
                "PM10",  46.0, 43);
        Measurement k23 = new Measurement("2019-12-30T22:00:00.000Z","2019-12-30T23:00:00.000Z",
                "PM10",  60.0, 43);

        Measurement k31 = new Measurement("2019-12-29T12:00:00.000Z","2019-12-29T13:00:00.000Z",
                "PM10",  44.0, 1010);
        Measurement k32 = new Measurement("2019-12-29T22:00:00.000Z","2019-12-29T23:00:00.000Z",
                "PM10",  40.0, 1010);
        Measurement k33 = new Measurement("2019-12-30T14:00:00.000Z","2019-12-30T15:00:00.000Z",
                "PM10",  45.0, 1010);

        JavaRDD<Measurement> bm1 = sparkContext.parallelize(Arrays.asList(b11, b12, b13));
        JavaRDD<Measurement> bm2 = sparkContext.parallelize(Arrays.asList(b21, b22, b23));
        JavaRDD<Measurement> bm3 = sparkContext.parallelize(Arrays.asList(b31,b32,b33));

        JavaRDD<Measurement> wm1 = sparkContext.parallelize(Arrays.asList(w11, w12, w13));
        JavaRDD<Measurement> wm2 = sparkContext.parallelize(Arrays.asList(w21,w22,w23));
        JavaRDD<Measurement> wm3 = sparkContext.parallelize(Arrays.asList(w31,w32,w33));

        JavaRDD<Measurement> km1 = sparkContext.parallelize(Arrays.asList(k11, k12, k13));
        JavaRDD<Measurement> km2 = sparkContext.parallelize(Arrays.asList(k21,k22,k23));
        JavaRDD<Measurement> km3 = sparkContext.parallelize(Arrays.asList(k31,k32,k33));

        monitor.addMeasurements(b1, bm1);
        monitor.addMeasurements(b2, bm2);
        monitor.addMeasurements(b3, bm3);

        monitor.addMeasurements(w1, wm1);
        monitor.addMeasurements(w2, wm2);
        monitor.addMeasurements(w3, wm3);

        monitor.addMeasurements(k1, km1);
        monitor.addMeasurements(k2, km2);
        monitor.addMeasurements(k3, km3);
    }

    @Test
    public void findInstallationsWithHighestValuesTest() {
        Measurement b12 = new Measurement("2019-12-30T23:00:00.000Z","2019-12-31T00:00:00.000Z",
                "PM10",  51.0, 9983);
        Measurement b22 = new Measurement("2019-12-29T22:00:00.000Z","2019-12-29T23:00:00.000Z",
                "PM10",  51.0, 7978);
        Measurement b32 = new Measurement("2019-12-29T22:00:00.000Z","2019-12-29T23:00:00.000Z",
                "PM10",  54.0, 2920);
        Measurement k33 = new Measurement("2019-12-30T14:00:00.000Z","2019-12-30T15:00:00.000Z",
                "PM10",  45.0, 1010);
        Measurement k23 = new Measurement("2019-12-30T22:00:00.000Z","2019-12-30T23:00:00.000Z",
                "PM10",  60.0, 43);
        Measurement k12 = new Measurement("2019-12-30T23:00:00.000Z","2019-12-31T00:00:00.000Z",
                "PM10",  51.0, 2227);
        Measurement w32 = new Measurement("2019-12-29T22:00:00.000Z","2019-12-29T23:00:00.000Z",
                "PM10",  53.0, 2141);
        Measurement w22 = new Measurement("2019-12-29T22:00:00.000Z","2019-12-29T23:00:00.000Z",
                "PM10",  48.0, 7901);
        Measurement w11 = new Measurement("2019-12-29T23:00:00.000Z","2019-12-30T00:00:00.000Z",
                "PM10",  55.0, 787);

        Installation b1 = new Installation(9983, 53.135394,23.130144,"Bialystok",
                "Poland", "11", "Zwyciestwa");
        Installation b2 = new Installation(7978, 53.114879, 23.194593, "Bialystok", "Poland", "16", "Żubrów");
        Installation b3 = new Installation(2920, 53.15586, 23.200312, "Bialystok", "Poland", "5", "Trawiasta");

        Installation w1 = new Installation(787,51.072434,17.006976,"Wrocław","Poland","8","aleja Karkonoska");
        Installation w2 = new Installation(7901, 51.09364, 16.97707, "Wrocław", "Poland", "7A","Nasturcjowa");
        Installation w3 = new Installation(2141, 51.127037, 16.978491, "Wrocław", "Poland", "9", "Tokarska");

        Installation k1 = new Installation(2227,50.304292,19.033288,"Katowice","Poland","8","1 Maja");
        Installation k2 = new Installation(43, 50.264611, 18.975028, "Katowice", "Poland", "7A","Sławka");
        Installation k3 = new Installation(1011, 50.308643, 19.006968, "Katowice", "Poland", "9", "Władysława Reymonta");


        Pair<Installation, Measurement> km1 = new ImmutablePair<>(k1, k12);
        Pair<Installation, Measurement> km2 = new ImmutablePair<>(k2, k23);
        Pair<Installation, Measurement> km3 = new ImmutablePair<>(k3, k33);

        Pair<Installation, Measurement> wm1 = new ImmutablePair<>(w1, w11);
        Pair<Installation, Measurement> wm2 = new ImmutablePair<>(w2, w22);
        Pair<Installation, Measurement> wm3 = new ImmutablePair<>(w3, w32);

        Pair<Installation, Measurement> bm1 = new ImmutablePair<>(b1, b12);
        Pair<Installation, Measurement> bm2 = new ImmutablePair<>(b2, b22);
        Pair<Installation, Measurement> bm3 = new ImmutablePair<>(b3, b32);

        Parameter parameter = Parameter.PM10;
        String fromDateTime = "2019-12-29T00:00:00.000Z";
        String tillDateTime = "2019-12-31T23:00:00.000Z";

        List<Pair<Installation, Measurement>> result = monitor.findInstallationsWithValuesAboveStandard(parameter, fromDateTime, tillDateTime);
        List<Pair<Installation, Measurement>> expected = Arrays.asList(km2, wm1, bm3, wm3, bm1, bm2, km1, wm2, km3);

        assertEquals(expected, result);

    }

    @Test
    public void getHourWithTheHighestAverageTest() {
        Parameter parameter = Parameter.PM10;
        City city = City.BIALYSTOK;
        /*12 - 40
        * 23 - 46, 51, 46 (47.666...)
        * 22 - 44, 51, 44, 54 (48.25)
        * 14 - 47*/
        Integer hour = 22;
        Double value = 48.25;
        Pair<Integer, Double> result = monitor.getHourWithTheHighestAverage(city, parameter);
        System.out.println(result.getLeft());
        System.out.println(result.getRight());
        assertEquals(result.getLeft(), hour);
        assertEquals(result.getRight(), value, 0.001);
    }

    @Test
    public void findCitiesWithHighestAverageValuesTest() {
        Parameter parameter = Parameter.PM10;
        /*
        Wroclaw:
        23 - 55, 50, 47 (50.666...)
        22 - 34, 48, 44, 53 (44.75)
        12 - 40
        14 - 45
        Avg: 45.104

        Katowice:
        23 - 50, 51, 49 (50)
        22 - 32, 46, 60, 40 (44.5)
        12 - 44
        14 - 45
        Avg: 45.875

        Białystok:
        12 - 40
        23 - 46, 51, 46 (47.666...)
        22 - 44, 51, 44, 54 (48.25)
        14 - 47
        Avg: 45.73
        */

        Pair<City, Double> wroclaw = new ImmutablePair<>(City.WROCLAW, 45.104);
        Pair<City, Double> katowice = new ImmutablePair<>(City.KATOWICE, 45.875);
        Pair<City, Double> bialystok = new ImmutablePair<>(City.BIALYSTOK, 45.73);

        List<Pair<City, Double>> expected = Arrays.asList(katowice, bialystok, wroclaw);
        List<Pair<City, Double>> result = monitor.findCitiesWithHighestAverageValues(parameter);

        assertEquals(expected, result);
    }



    @Test
    public void countMeasurementsTest() {
        assertEquals(9, monitor.countMeasurement(City.BIALYSTOK));
    }

}
