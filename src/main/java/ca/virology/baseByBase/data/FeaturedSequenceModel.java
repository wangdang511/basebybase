package ca.virology.baseByBase.data;

import ca.virology.lib.io.sequenceData.FeaturedSequence;


/**
 * This is an interface for all objects that hold a set of  FeaturedSequence
 * objects.  This allows other classes to  generically add filters and
 * retrieve the sequences from these classes.
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public interface FeaturedSequenceModel {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * prevents all sequences in a given list from being shown
     */
    void setSequenceFilter(FeaturedSequence[] hidden);

    /**
     * Get the sequences held by this object
     *
     * @return An array of the sequences
     */
    FeaturedSequence[] getSequences();

    /**
     * Get the visible (unfiltered) sequence
     *
     * @return An array of the visible sequences
     */
    FeaturedSequence[] getVisibleSequences();
}
