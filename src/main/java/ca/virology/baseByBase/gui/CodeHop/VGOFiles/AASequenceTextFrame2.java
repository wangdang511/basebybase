package ca.virology.baseByBase.gui.CodeHop.VGOFiles;


import javax.swing.*;


/**
 * This class implements a window to display a Amino Acid sequence.
 *
 * @author Angie Wang
 * @version 1.0
 * @date July 18, 2002
 */
public class AASequenceTextFrame2 extends AbstractSequenceTextFrame2 {
    protected JMenu m_blastp_menu;
    protected String m_nucl_seqString;
    protected String m_aa_seqString;
    protected String m_dbName;

    /**
     * Constructor for the AASequenceTextFrame object
     *
     * @param ss The sequence string to display
     */
    AASequenceTextFrame2(String seqString, String dbName) {
        super();
        m_dbName = dbName;
        m_nucl_seqString = seqString;
        m_aa_seqString = ca.virology.vgo.util.SequenceTools.getAAcidsFrSeq(m_nucl_seqString);
    }

}
