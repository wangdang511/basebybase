package ca.virology.baseByBase.gui;

import ca.virology.baseByBase.DiffEditor;
import ca.virology.baseByBase.io.VocsTools;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * This panel lets users load sequences from the database in to
 * base by base
 *
 * @author Ryan Brodie
 * @version 1.0
 */
@Deprecated
public class DbLoadSeqPanel extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected DefaultListModel m_model = new DefaultListModel();
    protected JList m_list = new JList(m_model);
    protected Map m_vMap = null;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new DbLoadSeqPanel object.
     */
    public DbLoadSeqPanel() {
        init();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the ids the user has selected in the virus list
     *
     * @return an array of ids
     */
//    public int[] getSelectedIDs() {
//        Object[] sel = m_list.getSelectedValues();
//        int[] ret = new int[sel.length];
//
//        for (int i = 0; i < sel.length; ++i) {
//            Integer val = (Integer) m_vMap.get(sel[i]);
//            ret[i] = val.intValue();
//        }
//
//        return ret;
//    }

    /**
     * init this window
     *
     * @return true if it was possible to init the window
     */
    protected boolean init() {
        m_vMap = null;

        m_vMap = new TreeMap(VocsTools.getVirusIDMap(DiffEditor.getDbName()));
        Iterator i = m_vMap.keySet().iterator();

        while (i.hasNext()) {
            m_model.addElement(i.next());
        }

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(m_list), BorderLayout.CENTER);
        p.add(new JLabel("<html>" +
                "The box below contains a listing of all available " +
                "sequences in the currently connected database.<br> " +
                "Select one or more and click 'Open' to load these " +
                "sequences, with gene annotations, into Base-by-base.<br><br>" +
                "<b>Note: These sequences will be loaded unaligned as raw<br>" +
                "sequence data, and must be aligned before reports such<br>" +
                "as the CDS report and event breakdown are to be useful.</b>" + "</html>"), BorderLayout.NORTH);
        p.setBorder(BorderFactory.createTitledBorder("Available Sequences"));

        setLayout(new BorderLayout());
        add(p, BorderLayout.CENTER);

        return true;
    }
}
