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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import ca.virology.lib.io.sequenceData.FeaturedSequence;


/**
 * This panel is used to edit sequence names.
 *
 * @author Sangeeta Neti
 */
public class SequenceEditorDialog
        extends JDialog {
//	~ Instance fields ////////////////////////////////////////////////////////	

    protected boolean m_approved = false;
    protected JTextField seqNames[] = null;
    FeaturedSequence[] seqs = null;


    public SequenceEditorDialog(FeaturedSequence[] names) {

        setTitle("Edit Sequence Names");
        this.seqs = names;
        seqNames = new JTextField[seqs.length];

        for (int i = 0; i < names.length; i++) {
            this.seqNames[i] = new JTextField(50);
            this.seqNames[i].setText(names[i].getName());
        }

        setModal(true);
        initUI();

    }

    //~ Methods ////////////////////////////////////////////////////////////////
    public String[] getNames() {
        String ret[] = new String[seqs.length];

        for (int i = 0; i < seqs.length; i++) {
            ret[i] = seqNames[i].getText();
            if ((ret[i] == null) || ret[i].equals("")) {
                ret[i] = " ";
            }
        }

        return ret;
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
     * set the approval status
     *
     * @param newval the approval status
     */
    protected void setApproval(boolean newval) {
        m_approved = newval;
    }

    /**
     * Init the UI for this component
     */
    protected void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        JPanel sequencePane = new JPanel();
        sequencePane.setLayout(new BoxLayout(sequencePane, BoxLayout.Y_AXIS));

        JScrollPane sp = new JScrollPane(sequencePane);
        sp.setPreferredSize(new Dimension(300, 250));
        Border line = BorderFactory.createLineBorder(Color.black);
        sp.setViewportBorder(BorderFactory.createTitledBorder(line, "Sequences"));

        Dimension labelSize = new Dimension(100, 20);
        Dimension textSize = new Dimension(100, 20);

        for (int i = 0; i < seqs.length; i++) {
            JLabel label = new JLabel("Sequence " + (i + 1) + ":");
            label.setMaximumSize(labelSize);
            label.setMinimumSize(labelSize);
            label.setPreferredSize(labelSize);

            seqNames[i].setMaximumSize(textSize);
            seqNames[i].setMinimumSize(textSize);
            seqNames[i].setPreferredSize(textSize);

            JPanel pane = new JPanel();
            pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));

            pane.add(Box.createHorizontalStrut(25));
            pane.add(Box.createHorizontalGlue());
            pane.add(label);
            pane.add(seqNames[i]);
            pane.add(Box.createHorizontalStrut(25));
            pane.add(Box.createHorizontalGlue());
            sequencePane.add(pane);
        }

        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        JPanel btns = new JPanel();
        btns.add(Box.createHorizontalGlue());
        btns.add(save);
        btns.add(cancel);

        main.add(sp, BorderLayout.CENTER);
        main.add(btns, BorderLayout.SOUTH);
        setContentPane(main);

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