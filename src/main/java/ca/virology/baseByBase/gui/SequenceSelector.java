/*
 * Base-by-base: Whole Genome pairwise and multiple alignment editor
 * Copyright (C) 2003  Dr. Chris Upton, University of Victoria
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.FeaturedSequence;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This displays a panel that allows you to select one or more  sequencs from a
 * list of sequences.  The visual properties of the panel are all
 * customizable.
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class SequenceSelector extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected int m_maxSels;
    protected int m_minSels;
    protected String m_message;
    protected FeaturedSequence[] m_seqs;
    protected JList m_list = new JList();
    protected Map m_seqMap = new HashMap();

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new SequenceSelector object.
     *
     * @param seqs       The sequences to choose from
     * @param message    the explanatory message for the user
     * @param minSelects the minimum number of selections to make (or zero for
     *                   no min)
     * @param maxSelects the maximum number of selections to make (or zero for
     *                   no max)
     * @throws IllegalArgumentException
     */
    public SequenceSelector(FeaturedSequence[] seqs, String message, int minSelects, int maxSelects) throws IllegalArgumentException {
        if ((maxSelects > 0) && (minSelects > maxSelects)) {
            throw new IllegalArgumentException("Minimum number of sels (" +
                    minSelects + ") must be " + "Less than the max (" + maxSelects +
                    ")");
        }

        m_message = message;
        m_seqs = seqs;
        m_maxSels = maxSelects;
        m_minSels = minSelects;

        for (int i = 0; i < seqs.length; ++i) {
            m_seqMap.put(seqs[i].getName(), seqs[i]);
        }

        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the selected sequences
     *
     * @return the selected sequences in an array
     */
    public FeaturedSequence[] getSelectedSequences() {
        //caity - getSelectedValues() is deprecated -> switched to getSelectedValuesList()
//        Object[] os = m_list.getSelectedValues();
//        FeaturedSequence[] ret = new FeaturedSequence[os.length];
//
//        for (int i = 0; i < os.length; ++i) {
//            ret[i] = (FeaturedSequence) m_seqMap.get(os[i].toString());
//        }
//        return ret;
        List<FeaturedSequence> fsl = m_list.getSelectedValuesList();
        FeaturedSequence[] fsa = new FeaturedSequence[fsl.size()];
        for (int i = 0; i < fsl.size(); i++) {
            fsa[i] = (FeaturedSequence) m_seqMap.get(fsl.get(i));
        }
        return fsa;
    }

    /**
     * get the number of selected sequences
     *
     * @return the selection count
     */
    public int getSelectionCount() {
        return m_list.getSelectedIndices().length;
    }

    /**
     * initialize the user interface for the panel
     */
    protected void initUI() {
        setLayout(new BorderLayout());

        JPanel main = new JPanel(new BorderLayout());

        String title = "Please select ";

        if (m_maxSels == m_minSels) {
            title += (m_maxSels + " sequences");
        } else if (m_maxSels == 0) {
            title += (m_minSels + " or more sequences");
        } else {
            title += (m_minSels + " to " + m_maxSels + " sequences");
        }

        main.setBorder(BorderFactory.createTitledBorder(title));

        DefaultListModel model = new DefaultListModel();
        m_list.setModel(model);

        for (int i = 0; i < m_seqs.length; ++i) {
            model.addElement(m_seqs[i].getName());
        }

        JLabel l = new JLabel(m_message);

        main.add(new JScrollPane(m_list), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        add(l, BorderLayout.NORTH);
    }
}