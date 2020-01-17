package pl.edu.agh.airly.comparator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.airly.model.Installation;
import pl.edu.agh.airly.model.Measurement;

import java.io.Serializable;
import java.util.Comparator;

public class InstallationMeasurementPairComparator implements Serializable, Comparator<ImmutablePair<Installation, Measurement>> {

    @Override
    public int compare(ImmutablePair<Installation, Measurement> o1, ImmutablePair<Installation, Measurement> o2) {
        MeasurementComparator measurementComparator = new MeasurementComparator();
        return measurementComparator.compare(o2.getRight(), o1.getRight());
    }
}