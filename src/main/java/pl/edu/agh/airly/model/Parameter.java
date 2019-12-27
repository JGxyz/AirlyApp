package pl.edu.agh.airly.model;

import java.util.EnumSet;

public enum Parameter {
    PM1("PM1", false, Integer.MAX_VALUE),
    PM25("PM25", true, 25.0),
    PM10("PM10", true, 50.0),
    NO2("NO2", true, 200.0),
    SO2("SO2", true, 350.0),
    CO("CO", true, 30000.0),
    PRESSURE("Ciśnienie", false, Integer.MAX_VALUE),
    HUMIDITY("Wilgotność", false, Integer.MAX_VALUE),
    TEMPERATURE("Temperatura", false, Integer.MAX_VALUE);

    private String name;
    private boolean hasStandard;
    private double standard;

    Parameter(String name, boolean hasStandard, double standard) {
        this.name = name;
        this.hasStandard = hasStandard;
        this.standard = standard;
    }

    public String getName() {
        return name;
    }

    public boolean hasStandard() {
        return hasStandard;
    }

    public double getStandard() {
        return standard;
    }

    public static EnumSet<Parameter> getAll() { return EnumSet.allOf(Parameter.class);}

    @Override
    public String toString() {
        return name;
    }
}
