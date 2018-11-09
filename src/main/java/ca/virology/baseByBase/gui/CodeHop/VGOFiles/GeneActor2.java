package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

import ca.virology.baseByBase.gui.CodeHop.Primer;
import ca.virology.lib.util.common.Logger;
import ca.virology.vgo.data.DatasourceException;
import ca.virology.vgo.data.FamilyTools;
import ca.virology.vgo.gui.GeneCDSTextFrame;
import ca.virology.vgo.gui.SelectionEvent;
import ca.virology.vgo.gui.SequencePanelHolder;
import ca.virology.vgo.gui.SequenceViewerActor;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;

import java.util.ArrayList;
import java.util.List;


/**
 * This implementation of SequenceViewerActor is used to respond to
 * actions relating to gene features drawn on the sequence map
 *
 * @author Ryan Brodie
 * @date July 8, 2002
 */
public class GeneActor2 implements SequenceViewerActor2 {
    protected StrandedFeature m_geneFeature;
    protected int m_geneID;
    protected String m_dbName;
    protected Primer primer;
    protected int primerNum;


    /**
     * Constructor for the GeneActor object
     *
     * @param geneFeature The feature for which this actor will 'act'
     */
    public GeneActor2(StrandedFeature geneFeature, String dbName, Primer primer, int primerNum) {
        m_geneFeature = geneFeature;
        m_geneID = ((Integer) m_geneFeature.getAnnotation().getProperty("id")).intValue();
        m_dbName = dbName;
        this.primer = primer;
        this.primerNum = primerNum;
    }

    /**
     * This is called usually on a double click, but is the 'action' associated
     * with this actor
     *
     * @param src         The source of the double click event
     * @param seqLocation The location in the sequence that was clicked
     * @param sph         The sequence panel holder that the event was in
     * @return A list of windows opened by this action
     */
    public List action(SequenceRenderContext src, int seqLocation, SequencePanelHolder2 sph) {
        Logger.println(m_geneFeature.getAnnotation().getProperty("name") + " -- action");
        List windows = new ArrayList();

        GeneCDSTextFrame2 f = new GeneCDSTextFrame2(m_geneID, m_geneFeature.getSource());
        f.pack();
        f.setVisible(true);
        windows.add(f);

        return windows;
    }

    /**
     * This is called usually on a single click and returns the selection
     * relevant to this click
     *
     * @param src         The source of the double click event
     * @param seqLocation The location in the sequence that was clicked
     * @param isClick     <CODE>true</CODE> if the event was from a button click, <CODE>false</CODE>
     *                    otherwise
     * @param sph         The sequence panel holder that the event was in
     * @return A new SelectionEvent
     */
    public SelectionEvent2 selection(SequenceRenderContext src, int seqLocation, boolean isClick,
                                     SequencePanelHolder2 sph) {

        Location loc = m_geneFeature.getLocation();

        SelectionEvent2 ev = new SelectionEvent2(
                loc.getMin(),
                loc.getMax(),
                m_geneFeature.getStrand(),
                -1, // virusID -- fill this out later? -- is it worth it?
                m_dbName,
                m_geneFeature,
                m_geneFeature.getAnnotation().getProperty("name").toString(),
                (int) m_geneFeature.getAnnotation().getProperty("blocknum"),//blocknum
                primerNum,//primer num
                primer
        );
        return ev;
    }
}
