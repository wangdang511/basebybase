package ca.virology.baseByBase.gui.CodeHop;

import java.util.ArrayList;

public class DegenerateCore {

    int startNTPos; // start NUCLEOTIDE position within a BLOCK
    String core;
    int degeneracy;
    ArrayList<MatrixEntry>[] excludedAA;
    String aaSeq;

    public DegenerateCore(int startNTPos, String core, int degeneracy, ArrayList<MatrixEntry>[] cutOffAA, String aaSeq) {
        this.startNTPos = startNTPos;
        this.core = core;
        this.degeneracy = degeneracy;
        this.excludedAA = cutOffAA;
        this.aaSeq = aaSeq;
    }

    public DegenerateCore(DegenerateCore another) {
        this.startNTPos = another.startNTPos;
        this.core = another.core;
        this.degeneracy = another.degeneracy;
        this.excludedAA = another.excludedAA;
        this.aaSeq = another.aaSeq;
    }
}