package pl.edu.agh.airly.comparator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.airly.model.City;

import java.io.Serializable;
import java.util.Comparator;

public class CityDoublePairComparator implements Serializable, Comparator<ImmutablePair<City, Double>> {

    @Override
    public int compare(ImmutablePair<City, Double> o1, ImmutablePair<City, Double> o2) {
        return Double.compare(o2.getRight(), o1.getRight());
    }
}