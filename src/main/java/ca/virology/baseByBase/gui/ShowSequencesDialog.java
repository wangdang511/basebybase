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

import ca.virology.baseByBase.data.*;

import ca.virology.lib.io.sequenceData.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * This dialog shows a list of the sequences with checkboxes.  This is usually
 * used to let users filter the sequences in the current display
 *
 * @author Ryan Brodie
 * @version $Revision: 1.3 $
 */
public class ShowSequencesDialog
        extends JDialog {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected Map m_status = new HashMap();
    protected final FeaturedSequenceModel m_holder;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new ShowSequencesDialog object.
     *
     * @param holder the featured sequence holder that this should
     *               pay attention to.
     */
    public ShowSequencesDialog(FeaturedSequenceModel holder) {
        m_holder = holder;
        setTitle("Filter Visible Sequences");
        setModal(true);
        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the sequences available
     *
     * @return the sequences in an array
     */
    protected FeaturedSequence[] getSequences() {
        return m_holder.getSequences();
    }

    /**
     * apply the changes made in the main display (hide/show the appropriate
     * sequence edtors)
     */
    protected void applyChanges() {
        ArrayList l = new ArrayList();
        Iterator it = m_status.keySet()
                .iterator();

        while (it.hasNext()) {
            Object key = it.next();
            Boolean value = (Boolean) m_status.get(key);

            if (!value.booleanValue()) {
                l.add(key);
            }
        }

        FeaturedSequence[] seqs =
                (FeaturedSequence[]) l.toArray(new FeaturedSequence[0]);

        m_holder.setSequenceFilter(seqs);
    }

    /**
     * set the visible status of a sequence
     *
     * @param seq     The sequence to mod
     * @param visible the new status
     */
    protected void setStatus(
            FeaturedSequence seq,
            boolean visible) {
        m_status.put(
                seq,
                new Boolean(visible));
    }

    /**
     * initialize the UI
     */
    protected void initUI() {
        final FeaturedSequence[] seqs = getSequences();
        final FeaturedSequence[] vseqs = m_holder.getVisibleSequences();

        JPanel main = new JPanel(new BorderLayout());
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));

        final JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        content.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Select Sequences to Show"),
                        BorderFactory.createEmptyBorder(3, 3, 3, 3)));

        JButton cancel = new JButton("Cancel");
        JButton ok = new JButton("Ok");
        JButton apply = new JButton("Apply");
        btns.add(Box.createHorizontalGlue());
        btns.add(ok);
        btns.add(Box.createHorizontalStrut(3));
        btns.add(apply);
        btns.add(Box.createHorizontalStrut(3));
        btns.add(cancel);

        JScrollPane scroll = new JScrollPane(content);
        main.add(btns, BorderLayout.SOUTH);
        main.add(scroll, BorderLayout.CENTER);

        for (int i = 0; i < seqs.length; ++i) {
            //JPanel          p = new JPanel(new BorderLayout());
            final JCheckBox cb = new JCheckBox(seqs[i].getName());
            content.add(cb);

            final int index = i;
            cb.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent ev) {
                            setStatus(
                                    seqs[index],
                                    cb.isSelected());
                        }
                    });

            boolean visible = false;

            for (int j = 0; j < vseqs.length; ++j) {
                if (seqs[i] == vseqs[j]) {
                    visible = true;
                }
            }

            if (visible) {
                cb.setSelected(true);
                setStatus(seqs[i], true);
            } else {
                cb.setSelected(false);
                setStatus(seqs[i], false);
            }
        }


        //select all button
        JButton selectall = new JButton("Select All");
        content.add(selectall);
        selectall.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        Component[] components = content.getComponents();
                        Component component = null;
                        int index = 0;
                        for (int i = 0; i < components.length; i++) {
                            component = components[i];
                            if (component instanceof JCheckBox) {
                                JCheckBox cb = (JCheckBox) component;
                                cb.setSelected(true);
                                setStatus(seqs[i], true);
                                index++;
                            }
                        }
                    }
                });

        apply.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        applyChanges();
                    }
                });

        ok.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        applyChanges();
                        dispose();
                    }
                });

        cancel.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        dispose();
                    }
                });

        setContentPane(main);
        //  setContentPane(scroll);

        pack();

        // Position the dialog window
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = getWidth();
        int h = getHeight();
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        setLocation(x, y);
        setResizable(true);
    }
}
