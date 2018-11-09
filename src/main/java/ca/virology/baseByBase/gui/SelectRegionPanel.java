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

import ca.virology.lib.io.sequenceData.EditableSequence;

import java.awt.*;

import javax.swing.*;


/**
 * This is a panel that allows the user to input a sequence location to select
 *
 * @author Ryan Brodie
 */
public class SelectRegionPanel
        extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected EditableSequence[] m_seqs = null;
    protected JComboBox m_seq = new JComboBox();
    protected JTextField m_startPos = new JTextField(10);
    protected JTextField m_stopPos = new JTextField(10);
    protected JRadioButton m_gapped = new JRadioButton("Gapped Position");
    protected JRadioButton m_ungapped =
            new JRadioButton("Absolute Position");
    protected JCheckBox m_prop = new JCheckBox("Propogate Selection");

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Construct a new panel that will offer locations on the given sequences
     *
     * @param sequences the sequences to target
     */
    public SelectRegionPanel(EditableSequence[] sequences) {
        m_seqs = sequences;

        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Init the UI Properties
     */
    protected void initUI() {
        setLayout(new BorderLayout());

        JPanel input = new JPanel(new VerticalFlowLayout());

        JPanel p = new JPanel(new BorderLayout());
        p.add(
                new JLabel("Sequence: "),
                BorderLayout.WEST);
        //p.add(m_seq, BorderLayout.CENTER);

        input.add(p);

        p = new JPanel();

        ButtonGroup group = new ButtonGroup();
        group.add(m_gapped);
        group.add(m_ungapped);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalGlue());
        p.add(m_ungapped);
        p.add(Box.createVerticalStrut(4));
        p.add(m_gapped);
        p.add(Box.createVerticalStrut(4));
        p.add(m_prop);
        p.add(Box.createVerticalGlue());
        input.add(p);

        p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;

        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("From  "), c);
        c.gridx = 1;
        p.add(m_startPos, c);
        c.gridx = 0;
        c.gridy = 1;
        p.add(new JLabel("To  "), c);
        c.gridx = 1;
        p.add(m_stopPos, c);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 2;
        c.gridy = 2;
        p.add(Box.createGlue(), c);

        input.add(p);

        add(input, BorderLayout.NORTH);

        // setup initial state and data
        //	for (int i = 0; i < m_seqs.length; ++i) {
        //		m_seq.addItem(m_seqs[i].getName());
        //	}

        //	if (m_seqs.length > 0) {
        //		m_seq.setSelectedIndex(0);
        //	}

        m_ungapped.setSelected(true);

    }

    /**
     * Get the sequence selected by the user
     *
     * @return the sequence index
     */
    public int getSequence() {
        return m_seq.getSelectedIndex();
    }

    /**
     * Returns true if the user selected the 'gapped' position
     *
     * @return true if 'gapped' was selected, false otherwise
     */
    public boolean isGappedSelected() {
        return m_gapped.isSelected();
    }

    /**
     * returns the value of the 'propogate selection' flag
     *
     * @return the value of the flag
     */
    public boolean isPropogateSelected() {
        return m_prop.isSelected();
    }

    /**
     * Get the start position inputted by the user
     *
     * @return the start position
     * @throws NumberFormatException
     */
    public int getStartPosition()
            throws NumberFormatException {
        return Integer.parseInt(m_startPos.getText());
    }

    /**
     * Get the stop position inputted by the user
     *
     * @return the stop position
     * @throws NumberFormatException
     */
    public int getStopPosition()
            throws NumberFormatException {
        return Integer.parseInt(m_stopPos.getText());
    }
}