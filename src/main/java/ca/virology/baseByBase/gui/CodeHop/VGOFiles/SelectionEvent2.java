package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import ca.virology.baseByBase.gui.CodeHop.Primer;
import org.biojava.bio.seq.StrandedFeature;

public class SelectionEvent2 {
    protected int m_start = 0;
    protected int m_stop = 0;
    protected StrandedFeature.Strand m_strand;
    protected int m_virusID;
    protected String m_dbName;
    protected String m_desc;
    protected Object m_target;
    protected int blockNum;
    protected int primerNum;
    public Primer primer;

    public SelectionEvent2(int start, int stop, StrandedFeature.Strand strand, int virusID, String dbName, Object target, String description, int blocknum, int primerNum, Primer primer) {
        this.m_strand = StrandedFeature.UNKNOWN;
        this.m_virusID = -1;
        this.m_desc = "";
        this.m_target = null;
        this.m_start = start;
        this.m_stop = stop;
        this.m_strand = strand;
        this.m_desc = description;
        this.m_target = target;
        this.m_virusID = virusID;
        this.m_dbName = dbName;
        this.blockNum = blocknum;
        this.primerNum = primerNum;
        this.primer = primer;
    }

    public StrandedFeature.Strand getStrand() {
        return this.m_strand;
    }

    public int getVirusID() {
        return this.m_virusID;
    }

    public String getDbName() {
        return this.m_dbName;
    }

    public String getDescription() {
        return this.m_desc;
    }

    public int getSeqStart() {
        return this.m_start;
    }

    public int getSeqStop() {
        return this.m_stop;
    }

    public Object getTarget() {
        return this.m_target;
    }

    public int getBlockNum() {
        return this.blockNum;
    }

    public int getPrimerNum() {
        return this.primerNum;
    }
}

