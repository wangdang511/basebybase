package ca.virology.baseByBase.util;

public class GenePos {
    public final int start;
    public final int end;

    public GenePos(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int compareTo(GenePos o) {
        return this.start - o.start;
    }

} 


