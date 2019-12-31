package pl.edu.agh.airly.comparator;

import pl.edu.agh.airly.model.Measurement;

import java.io.Serializable;
import java.util.Comparator;

public class MeasurementDateComparator implements Serializable, Comparator<Measurement> {
    @Override
    public int compare(Measurement m1, Measurement m2) {
        return CharSequence.compare(m1.getFromDateTime(), m2.getFromDateTime());
    }
}