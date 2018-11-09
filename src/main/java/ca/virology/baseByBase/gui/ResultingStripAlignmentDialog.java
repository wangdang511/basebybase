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

import ca.virology.lib.io.sequenceData.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * This dialog is used to display the resulting strip alignment.
 *
 * @author Sangeeta Neti
 */
public class ResultingStripAlignmentDialog
        extends JDialog {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected java.util.List m_seqs;
    protected boolean m_approved = false;
    protected int m_columnsDeleted = 0;
    protected final PrimaryPanel m_ppanel = new PrimaryPanel();

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new ResultingStripAlignmentDialog object.
     *
     * @param sequences          the sequences to display
     * @param noOfColumnsDeleted the number of columns deleted
     */

    public ResultingStripAlignmentDialog(java.util.List sequences, int noOfColumnsDeleted) {
        setTitle("Resulting Alignment");
        m_seqs = sequences;
        m_columnsDeleted = noOfColumnsDeleted;

        int[] lanePrefs =
                {
                        EditPanel.SCALE_CHANNEL, EditPanel.ALIGN_CHANNEL,


                        //EditPanel.ABSCALE_CHANNEL,
                        //EditPanel.ACID_CHANNEL,
                        EditPanel.DIFF_CHANNEL,
                        //EditPanel.EVENT_CHANNEL,
                        EditPanel.SEARCH_CHANNEL
                };
        m_ppanel.setChannelPreferences(lanePrefs);

        setModal(true);
        initUI();

        m_ppanel.refreshEditors();

        System.out.println("Resulting Alignment Dialog Created: " + hashCode());
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * set the sequences to display
     *
     * @param sequences the sequences to display
     */
    public void setSequences(java.util.List sequences) {
        m_ppanel.removeAllSequences();
        m_seqs = sequences;

        for (int i = 0; i < m_seqs.size(); ++i) {
            m_ppanel.addSequenceEditor((FeaturedSequence) m_seqs.get(i), null);
        }

        m_ppanel.refreshEditors();
    }

    /**
     * get the approval status
     *
     * @return the approval status
     */
    public boolean getApproval() {
        return m_approved;
    }

    /**
     * get the resulting sequences
     *
     * @return the resulting sequences
     */
    public java.util.ListIterator getSequences() {
        return m_seqs.listIterator();
    }

    /**
     * set the approval status
     *
     * @param newval the approval status
     */
    protected void setApproval(boolean newval) {
        m_approved = newval;
    }

    /**
     * initialize the UI properties
     */
    protected void initUI() {
        for (int i = 0; i < m_seqs.size(); ++i) {
            m_ppanel.addSequenceEditor((FeaturedSequence) m_seqs.get(i), null);
        }

        m_ppanel.setMouseMode(EditPanel.EDIT_MODE);
        m_ppanel.setSequenceToolsVisible(false);

        JPanel main = new JPanel(new BorderLayout());
        JPanel btns = new JPanel();
        btns.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));

        JPanel mid = new JPanel(new BorderLayout());
        mid.setBorder(
                BorderFactory.createTitledBorder(m_columnsDeleted +
                        " Columns Deleted, Do you wish to save?"));

        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");

        btns.add(Box.createHorizontalGlue());
        btns.add(save);
        btns.add(cancel);

        mid.add(m_ppanel);

        main.add(mid, BorderLayout.CENTER);
        main.add(btns, BorderLayout.SOUTH);

        setContentPane(main);

        addWindowListener(
                new WindowAdapter() {
                    public void windowOpened(WindowEvent ev) {
                        m_ppanel.refreshState();
                        m_ppanel.repaint();
                    }
                });

        save.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        setApproval(true);
                        dispose();
                    }
                });

        cancel.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        setApproval(false);
                        dispose();
                    }
                });
    }
}