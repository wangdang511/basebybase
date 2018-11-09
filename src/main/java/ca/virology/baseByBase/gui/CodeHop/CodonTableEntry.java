package ca.virology.baseByBase.gui.CodeHop;

/**
 * CLASS: CodonTableEntry
 * <p>
 * holds codon entries which are added in a list in the CodonTable object.
 */
public class CodonTableEntry {
    public String codon;
    public double prob;
    public String aa;

    public CodonTableEntry(String codonIn, double probIn, String aaIn) {
        this.codon = codonIn;
        this.prob = probIn;
        this.aa = aaIn;
    }
}