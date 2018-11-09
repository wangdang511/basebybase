package ca.virology.baseByBase.data;

/**
 * This represents a generic scoring matrix. The inputs to
 * <CODE>getScore(int,int)</CODE> MUST range from 0 to 26 and should be
 * retrieved from the <CODE>AminoAcid.valueOf(char)</CODE> method.  This
 * returns the int representation of the particular amino acid for  the
 * matrix.<BR><BR>Furthermore, any implementations of this interface must also
 * follow the same guidelines for the purposes of consistency.
 *
 * @author Ryan Brodie
 */
public interface ScoringMatrix {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the score between two amino acids based on this matrix
     *
     * @param aa1 The first amino acid
     * @param aa2 The second amino acid
     * @return The score
     */
    public int getScore(
            int aa1,
            int aa2);
}