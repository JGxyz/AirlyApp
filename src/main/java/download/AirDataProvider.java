package download;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.SQLContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public abstract class AirDataProvider<T> {
    protected JavaSparkContext sparkContext;

    public AirDataProvider(JavaSparkContext sparkContext) {
        this.sparkContext = sparkContext;
    }

    public AirDataProvider() {}

    public void getContent() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {

            HttpGet request = new HttpGet(getUrl());

            // add request headers
            request.addHeader("Accept", "application/json");
            request.addHeader("apikey", "2GFR0BjJeEnD0ytTbKKpYk3x3N6ri62z");

            CloseableHttpResponse response = httpClient.execute(request);

            try {

                // Get HttpResponse Status
                System.out.println(response.getProtocolVersion());              // HTTP/1.1
                System.out.println(response.getStatusLine().getStatusCode());   // 200
                System.out.println(response.getStatusLine().getReasonPhrase()); // OK
                System.out.println(response.getStatusLine().toString());        // HTTP/1.1 200 OK

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    String result = EntityUtils.toString(entity);
                    System.out.println(result);
                }

            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }

    }

    public void downloadData() {
        try {
            String urlString = getUrl();
            System.out.println(urlString);
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateData() {
        long interval = 3600*24*7;//updatujemy dane dotyczące instalacji co tydzień
        File file = new File(getResourcePath());
        long modTime = file.lastModified();
        Date date = new Date();
        long currTime = date.getTime();
        if (currTime - modTime > interval) {
            downloadData();
        }
    }

    public JavaRDD<T> readData() {
        SQLContext sqlContext = new SQLContext(sparkContext);
        Dataset<T> dataset = sqlContext.read().json(getResourcePath()).as(getEncoder());
        return dataset.toJavaRDD();
    }

    protected abstract String getUrl();
    public abstract Encoder<T> getEncoder();
    protected abstract void saveContentToFile(String content);
    protected abstract String getResourcePath();
}
