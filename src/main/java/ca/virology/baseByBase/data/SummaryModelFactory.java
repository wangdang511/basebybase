package ca.virology.baseByBase.data;

import ca.virology.lib.io.sequenceData.FeaturedSequence;


/**
 * This is a factory class that creates summary data models. get the names of
 * the possible models with 'getModelNames'.  Create a model by passing a name
 * string and an array of sequences to 'createSummaryModel'.  This will
 * complain if your sequences are not compatible with the model type you
 * chose.
 *
 * @author Ryan Brodie
 */
public class SummaryModelFactory {


    //~ Static fields/initializers /////////////////////////////////////////////

    protected static String[] m_modelNames =
            {
                    "Differences", "Nucleotide Differences", "Raw Conservation", "Scored Similarity (PAM250)",
                    "Scored Similarity (BLOSUM62)"
            };

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the possible model types this factory can create
     *
     * @return an array of model names
     */
    public static String[] getModelNames() {
        return m_modelNames;
    }

    /**
     * Create a new data model based on the given array of sequences of the
     * type given.  This type name must exist in the array of types returned
     * by getModelNames.
     *
     * @param name The name of the model type
     * @param seqs The sequence array
     * @return the representative model
     * @throws IllegalArgumentException if the model doesn't support the sequences provided
     */
    public static SequenceSummaryModel createSummaryModel(
            String name,
            FeaturedSequence[] seqs,
            int type)
            throws IllegalArgumentException {
        if (name.equals(m_modelNames[0])) {
            return new DifferenceSummaryModel(seqs, type, ColorScheme.DIFF_CLASSIC_SCHEME);
        } else if (name.equals(m_modelNames[1])) {
            return new DifferenceSummaryModel(seqs, type, ColorScheme.DIFF_NT_SCHEME);
        } else if (name.equals(m_modelNames[2])) {
            return new SimilaritySummaryModel(seqs);
        } else if (name.equals(m_modelNames[3])) {
            return new ScoredSimilaritySummaryModel(
                    seqs,
                    AminoAcid.createPam250Matrix());
        } else if (name.equals(m_modelNames[4])) {
            return new ScoredSimilaritySummaryModel(
                    seqs,
                    AminoAcid.createBlosum62Matrix());
        } else {
            return null;
        }
    }
}
