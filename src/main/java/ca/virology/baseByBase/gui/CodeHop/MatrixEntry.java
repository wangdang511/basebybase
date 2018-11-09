package ca.virology.baseByBase.gui.CodeHop;

import java.util.ArrayList;

//=========================================================================
// Class: MatrixEntry
//
// The pssm matrix will contain MatrixEntry objects in its cells
//=========================================================================
public class MatrixEntry {

    public char aminoAcidName;
    public double aminoAcidFreq;
    public ArrayList<CodonOccurrence> codonOccurrenceArray;

    public MatrixEntry(char a, double b) {
        this.aminoAcidName = a;
        this.aminoAcidFreq = b;
        codonOccurrenceArray = new ArrayList<CodonOccurrence>(6);
        addToCodonOccurence(aminoAcidName, aminoAcidFreq);
    }

    private void addToCodonOccurence(char aa, double freq) {
        ArrayList<CodonTableEntry> entry = CodeHopSelectPanel.codonTable.getCodons(aa);
        for (int i = 0; i < entry.size(); i++) {
            double codonOccFreq = freq * entry.get(i).prob;
            codonOccurrenceArray.add(new CodonOccurrence(entry.get(i).codon, codonOccFreq));
        }
    }
}
