package pl.edu.agh.airly.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Measurement implements Serializable {
    long installationId;
    private String fromDateTime;
    private String tillDateTime;
    private String paramName;
    private double value;

    public Measurement(String fromDateTime, String tillDateTime, String paramName, double value, long installationId) {
        this.fromDateTime = fromDateTime;
        this.tillDateTime = tillDateTime;
        this.paramName = paramName;
        this.value = value;
        this.installationId = installationId;
    }

    public Measurement() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Measurement that = (Measurement) o;
        return Double.compare(that.value, value) == 0 &&
                installationId == that.installationId &&
                Objects.equals(fromDateTime, that.fromDateTime) &&
                Objects.equals(tillDateTime, that.tillDateTime) &&
                Objects.equals(paramName, that.paramName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromDateTime, tillDateTime, paramName, value, installationId);
    }

    public int getHour() {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime date = LocalDateTime.parse(fromDateTime, formatter);
        return date.getHour();
    }

    public String getFromDateTime() {
        return fromDateTime;
    }

    public void setFromDateTime(String fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public String getTillDateTime() {
        return tillDateTime;
    }

    public void setTillDateTime(String tillDateTime) {
        this.tillDateTime = tillDateTime;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getInstallationId() {
        return installationId;
    }

    public void setInstallationId(long installationId) {
        this.installationId = installationId;
    }

    @Override
    public String toString() {
        return "TIME: " + fromDateTime + "-" + tillDateTime + " PARAM: " + paramName + " VALUE: " + value;
    }

    public String showInStatistics() {
        Parameter parameter = Parameter.getParameter(paramName);
        return String.format("MAX VALUE: %.2f\nPERCENTAGE OF THE STANDARD: %.2f", value, (int) value / parameter.getStandard() * 100) + "%\n\n";
    }
}
