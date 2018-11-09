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
import ca.virology.baseByBase.util.*;

import ca.virology.lib.io.sequenceData.*;

import org.biojava.bio.seq.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


/**
 * This class displays the results of a comparison of features.  This is like a
 * row in the CDS report window.
 *
 * @author Ryan Brodie
 * @version $Revision: 1.1.1.1 $
 */
public class FeatureAnalyzerFrame
        extends JFrame {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected FeatureAnalyzer m_fanalyzer;
    protected StrandedFeature m_feature;
    protected EditableSequence m_source;
    protected EditableSequence[] m_seqs;
    protected int m_index;
    protected Map m_seqMap = new TreeMap();

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new FeatureAnalyzerFrame object.
     *
     * @param feat Feature to analyze
     * @param qs   The sequence holding the feature
     */
    public FeatureAnalyzerFrame(
            StrandedFeature feat,
            EditableSequence qs) {
        setTitle("Gene: " +
                feat.getAnnotation().getProperty(AnnotationKeys.NAME));

        m_source = qs;
        m_feature = feat;

        FeaturedSequenceModel sh = AppConstants.getSequenceHolder();
        m_seqs = sh.getVisibleSequences();

        for (int i = 0; i < m_seqs.length; ++i) {
            if (m_seqs[i] == qs) {
                m_index = i;
            }

            m_seqMap.put(
                    m_seqs[i].getName(),
                    m_seqs[i]);
        }

        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * init ui
     */
    protected void initUI() {
        final JPanel main = new JPanel(new BorderLayout());
        final JComboBox cbx = new JComboBox();
        Iterator it = m_seqMap.keySet()
                .iterator();

        while (it.hasNext()) {
            cbx.addItem(it.next());
        }

        JLabel desc =
                new JLabel("<html>Select a sequence below to see a comparison<br>" +
                        "      of this gene in relation to the same region on<br>" +
                        "      that partcular sequence.</html>");
        desc.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JPanel top = new JPanel(new BorderLayout());
        top.add(desc, BorderLayout.NORTH);
        top.add(cbx, BorderLayout.SOUTH);

        EditableSequence comp = null;

        if (m_index < (m_seqs.length - 1)) {
            comp = m_seqs[m_index + 1];
        } else if (m_index > 0) {
            comp = m_seqs[m_index - 1];
        }

        if (comp != null) {
            cbx.setSelectedItem(comp.getName());
        }

        JPanel resPane = getResultsPanel(comp);
        resPane.setName("DATA");
        resPane.setBorder(
                BorderFactory.createTitledBorder("Gene Information/Comparison"));

        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));

        JButton close = new JButton("Close");
        close.setMnemonic(KeyEvent.VK_C);
        close.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        dispose();
                    }
                });
        btns.add(Box.createHorizontalGlue());
        btns.add(close);
        btns.add(Box.createHorizontalGlue());
        btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        main.add(top, BorderLayout.NORTH);
        main.add(resPane, BorderLayout.CENTER);
        main.add(btns, BorderLayout.SOUTH);
        main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final Map seqMap = m_seqMap;
        cbx.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        Object o = cbx.getSelectedItem();
                        System.out.println(o + " Selected");

                        if (o != null) {
                            EditableSequence comp =
                                    (EditableSequence) seqMap.get(o);

                            Component[] c = main.getComponents();

                            for (int i = 0; i < c.length; ++i) {
                                if ((c[i] == null) || (c[i].getName() == null)) {
                                    continue;
                                }

                                if (c[i].getName()
                                        .equals("DATA")) {
                                    main.remove(c[i]);

                                    break;
                                }
                            }

                            JPanel p = getResultsPanel(comp);
                            p.setName("DATA");

                            main.add(p, BorderLayout.CENTER);
                            main.revalidate();
                            pack();
                        }
                    }
                });

        setContentPane(main);
    }

    /**
     * get a comparison panel for the given sequence
     *
     * @param comp the sequence to compare to
     * @return the panel
     */
    protected JPanel getResultsPanel(EditableSequence comp) {
        m_fanalyzer = new FeatureAnalyzer(m_feature, m_source, comp);

        java.util.List headers = m_fanalyzer.getHeaders();
        java.util.List data = m_fanalyzer.getData();

        JPanel dpanel = new JPanel(new GridLayout(
                headers.size(),
                2));
        dpanel.setBorder(BorderFactory.createTitledBorder("Gene Comparison"));

        for (int i = 0; i < headers.size(); ++i) {
            String h = (String) headers.get(i);
            String d = "";

            if (i < data.size()) {
                d = formatString(data.get(i) + "", 30);
            }

            dpanel.add(new JLabel(h));

            JLabel dl = new JLabel(d);
            dl.setBackground(Color.white);
            dl.setOpaque(true);
            dl.setBorder(BorderFactory.createLineBorder(Color.black));
            dpanel.add(dl);
        }

        return dpanel;
    }

    /**
     * format a string to a maximum length
     *
     * @param in     the string
     * @param cutoff the cutoff length
     * @return the formatted string
     */
    protected String formatString(
            String in,
            int cutoff) {
        StringBuffer b = new StringBuffer(in);

        return b.toString();
    }
}
