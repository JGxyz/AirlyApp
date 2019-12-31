package pl.edu.agh.airly.connection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Tester {
    public boolean testConnection() {
        try {
            URL url = new URL("http://www.google.com");
            URLConnection connection = url.openConnection();
            connection.connect();
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}