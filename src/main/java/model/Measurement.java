package model;

import org.joda.time.DateTime;

import java.io.Serializable;

public class Measurement implements Serializable {
    private DateTime fromDateTime;
    private DateTime tillDateTime;
    private String paramName;
    private String value;
    long installationId;

    public Measurement(DateTime fromDateTime, DateTime tillDateTime, String paramName, String value, long installationId) {
        this.fromDateTime = fromDateTime;
        this.tillDateTime = tillDateTime;
        this.paramName = paramName;
        this.value = value;
        this.installationId = installationId;
    }

    public Measurement() {
    }


    public DateTime getFromDateTime() {
        return fromDateTime;
    }

    public void setFromDateTime(DateTime fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public DateTime getTillDateTime() {
        return tillDateTime;
    }

    public void setTillDateTime(DateTime tillDateTime) {
        this.tillDateTime = tillDateTime;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
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
        return "TIME: "+fromDateTime+"-"+tillDateTime+" PARAM: "+paramName+" VALUE: "+value;
    }
}
