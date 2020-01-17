package pl.edu.agh.airly.comparator;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Serializable;
import java.util.Comparator;

public class IntDoublePairComparator implements Serializable, Comparator<ImmutablePair<Integer, Double>> {
    @Override
    public int compare(ImmutablePair<Integer, Double> o1, ImmutablePair<Integer, Double> o2) {
        return Double.compare(o1.getRight(), o2.getRight());
    }
}

