package pl.edu.agh.airly.model;

import java.io.Serializable;
import java.util.Objects;

public class Installation implements Serializable {
    private long id;
    private double latitude;
    private double longitude;
    private String city;
    private String country;
    private String number;
    private String street;

    public Installation(long id, double latitude, double longitude, String city, String country, String number, String street) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.country = country;
        this.number = number;
        this.street = street;
    }

    public Installation() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public String toString() {
        if (street != null && number != null)
            return String.format("%d - %s %s", id, street, number);
        if (street != null)
            return String.format("%d - %s", id, street);
        return String.format("%d", id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Installation that = (Installation) o;
        return id == that.id &&
                Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Objects.equals(city, that.city) &&
                Objects.equals(country, that.country) &&
                Objects.equals(number, that.number) &&
                Objects.equals(street, that.street);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, latitude, longitude, city, country, number, street);
    }

    public String showInStatistics() {
        StringBuilder stringBuilder = new StringBuilder(city + " - " + id + "\n");
        if (street != null) {
            stringBuilder.append(street + " ");
            if (number != null)
                stringBuilder.append(number);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
