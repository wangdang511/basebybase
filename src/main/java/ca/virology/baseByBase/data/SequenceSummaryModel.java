package ca.virology.baseByBase.data;

import ca.virology.lib.io.sequenceData.FeaturedSequence;


/**
 * This interface defines how sequence alignment summaries should be displayed.
 * This is the color and type of indicator that would be assigned to a
 * particular locaiton in the alignment.  This is replacing the static way it
 * was done before in SummarySequencePanel which only used to do color by
 * difference.
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public interface SequenceSummaryModel {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the index of the given sequence in the alignment
     *
     * @param seq The sequence to index
     * @return The index of the sequence
     */
    int getIndex(FeaturedSequence seq);

    /**
     * Get the sequence type for this model
     *
     * @return one of <CODE>EditableSequence.AA_SEQUENCE</CODE>,
     * <CODE>EditableSequence.DNA_SEQUENCE</CODE>, or
     * <CODE>EditableSequence.GENERIC_SEQUENCE</CODE>
     */
    int getSequenceType();

    /**
     * Create an image that can be displayed as a legend
     * in reports
     *
     * @return the legend image
     */
    java.awt.Image createLegendImage();

    /**
     * add a sequence to the alignment
     *
     * @param seq The sequence to add
     */
    void addSequence(FeaturedSequence seq);

    /**
     * remove a sequence from the alignment
     *
     * @param seq the sequence to remove
     */
    void removeSequence(FeaturedSequence seq);

    /**
     * Get the seqences in this alignment in an array
     *
     * @return The sequence array
     */
    FeaturedSequence[] getSequences();

    /**
     * Get the sequence at the given index
     *
     * @param index The index to retrieve
     * @return the sequence for this model at that index
     */
    FeaturedSequence getSequence(int index);

    /**
     * Count the number of sequences in the model
     *
     * @return The number of sequences
     */
    int countSequences();

    /**
     * Get the length (in characters) of the alignment.  This is usually the
     * length of the longest string in the alignment
     *
     * @return the max length
     */
    int getLength();

    /**
     * Get the indicator for a particular range of sequence data.  This can be
     * arrived at by any number of means.
     *
     * @param index The index of the sequence to check
     * @param start The start pos of the sequence (gapped position) of the
     *              range
     * @param stop  The stop position of the sequence (gapped position) of the
     *              range
     * @return a <CODE>SummaryIndicator</CODE> object
     */
    SummaryIndicator getIndicator(
            int index,
            int start,
            int stop);

    /**
     * Get the indicator for a location on a sequence in the alignment
     *
     * @param index    The index of the sequence in the alignment
     * @param position The index along the sequence (gapped) of the alignment
     * @return the model indicator
     */
    SummaryIndicator getIndicator(
            int index,
            int position);

    /**
     * Re-retrieve and recalculate data based on an external event
     */
    void refresh();
}
