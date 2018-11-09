package ca.virology.baseByBase.util;

import org.biojava.bio.symbol.Location;

import java.util.*;


/**
 * This class handles comparison of two biojava 'Location' objects
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class LocationComparator
        implements Comparator {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * compare two objects
     *
     * @param o1 the first location
     * @param o2 the location to compare to
     * @return a value > 0 if location 1 starts to the left of location 2
     * on the number line.
     */
    public int compare(
            Object o1,
            Object o2) {
        return ((Location) o1).getMin() - ((Location) o2).getMin();
    }

    /**
     * check the equality of an object
     *
     * @param o1 returns true if the object passed in is another LocationComparator
     * @return true if the object passed in is another LocationComparator
     */
    public boolean equals(Object o1) {
        if (o1 instanceof LocationComparator) {
            return true;
        }

        return false;
    }
}
