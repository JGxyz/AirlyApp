package download;

import access.AccessProvider;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class InstallationDataProvider implements IDataProvider {
    private String urlString = "https://airapi.airly.eu/v2/installations/nearest?lat=50.062006&lng=19.940984&maxDistanceKM=5&maxResults=50";
    private String filePath = "/home/jolanta/AGH/AirlyApp/src/main/resources/installation.json";
    private long interval = 3600*24*7;//updatujemy dane dotyczące instalacji co tydzień

    @Override
    public void downloadData() {

        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Accept", AccessProvider.accept);
            con.setRequestProperty("apikey", AccessProvider.apikey);
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            System.out.println(con.getInputStream().toString());
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            System.out.println(content.toString());
            BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(filePath)));
            System.out.println(content.length());
            bwr.write(content.toString());
            bwr.close();
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateData() {
        File file = new File(filePath);
        long modTime = file.lastModified();
        Date date = new Date();
        long currTime = date.getTime();
        if (currTime - modTime > interval) {
            downloadData();
        }
    }

    @Override
    public String getResourcesPath() {
        return filePath;
    }
}
