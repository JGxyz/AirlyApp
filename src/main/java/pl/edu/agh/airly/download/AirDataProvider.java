package pl.edu.agh.airly.download;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.SQLContext;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

public abstract class AirDataProvider<T> {
    protected SQLContext sqlContext;
    protected long interval = 3600000;//time in milliseconds

    public AirDataProvider(JavaSparkContext sparkContext) {
        this.sqlContext = new SQLContext(sparkContext);
    }

    public AirDataProvider() {}

    public SQLContext getSqlContext() { return sqlContext; }

    public void setSqlContext(SQLContext sqlContext) { this.sqlContext = sqlContext;}

    public void downloadData() {
        HttpURLConnection con = null;
        try {
            String urlString = getUrl();
            System.out.println(urlString);
            URL url = new URL(urlString);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("apikey", "2GFR0BjJeEnD0ytTbKKpYk3x3N6ri62z");
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            saveContentToFile(content.toString());
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } finally {
            if (con != null)
                con.disconnect();
        }
    }

    public boolean isUptodate() {
        File file = new File(getResourcePath());
        long modTime = file.lastModified();
        Date date = new Date();
        long currTime = date.getTime();
        return currTime-modTime < interval ? true : false;
    }

    public Optional<JavaRDD<T>> readData() {
        File file = new File(getResourcePath());
        if (file.exists()) {
            Dataset<T> dataset = sqlContext.read().json(getResourcePath()).as(getEncoder());
            return Optional.of(dataset.toJavaRDD());
        }
        return Optional.empty();
   }

    protected abstract String getUrl();
    public abstract Encoder<T> getEncoder();
    protected abstract void saveContentToFile(String content);
    protected abstract String getResourcePath();
}
