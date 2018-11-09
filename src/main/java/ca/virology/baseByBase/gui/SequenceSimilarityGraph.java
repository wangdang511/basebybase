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
import javax.swing.*;

import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;


import ca.virology.lib.io.sequenceData.EditableSequence;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.util.gui.UITools;

import ca.virology.baseByBase.data.SequenceSimilarityModel;
import ca.virology.baseByBase.data.SequenceSimilarityModelFactory;
import ca.virology.baseByBase.util.Debug;


/**
 * This frame displays a graph of sequence similarity achieved by using a progressive scanning window,
 * where points on the graph represent the similarity of each sequence to a 'query' sequence.
 *
 * @author Alex Smith
 * @version $Revision: 1.2 $
 */
public class SequenceSimilarityGraph
        extends JFrame {

    protected int m_windowSize; // size of scanning window in base pairs
    protected int m_stepSize; // number of base pairs to skip to find the next similarity point
    protected FeaturedSequence m_querySeq; // query sequence
    protected FeaturedSequence[] m_seqs; // sequences to plot similarity against query
    protected SequenceSimilarityModel m_seqSimModel; // model used to calculate sequence similarity

    protected Plot m_plot;
    protected JPanel m_pBox;

    protected final static int DEFAULT_WINDOW_SIZE = 300; // default size of scanning window in base pairs
    protected final static int DEFAULT_STEP_SIZE = 10; // default number of base pairs to skip to find the next similarity point

    /**
     * Creates the window displaying the sequence similarity graph.
     *
     * @param seqs All the sequences which the graph will use. One of these will be selected
     *             as the query sequence, which the rest will be compared against.
     */
    public SequenceSimilarityGraph(FeaturedSequence querySeq, FeaturedSequence[] seqs, SequenceSimilarityModel seqSimModel, int windowSize, int stepSize) {
        m_querySeq = querySeq;
        m_seqs = seqs;
        m_seqSimModel = seqSimModel;
        m_windowSize = windowSize;
        m_stepSize = stepSize;

        // print debug info
        if (Debug.isOn()) {
            System.out.println("query sequence: " + m_querySeq.getName());
            System.out.println("reference sequences:");
            for (int i = 0; i < m_seqs.length; i++) {
                System.out.println("\t" + m_seqs[i].getName());
            }
        }

        // check params
        int seqType = m_seqSimModel.getSequenceType();
        String seqTypeName;
        if (seqType == EditableSequence.DNA_SEQUENCE) {
            seqTypeName = "DNA";
        } else {
            seqTypeName = "amino acid";
        }
        IllegalArgumentException eax = new IllegalArgumentException("The " + m_seqSimModel.getModelName() + " model only accepts " + seqTypeName + " sequences.");
        if (m_querySeq.getSequenceType() != seqType) {
            throw (eax);
        }
        for (int i = 0; i < m_seqs.length; i++) {
            if (m_seqs[i].getSequenceType() != seqType) {
                throw (eax);
            }
        }

        initUI();
    }

    private void initUI() {
        // create the plot and add it to the layout
        m_plot = createGraph();
        m_pBox = new JPanel();
        m_pBox.add(m_plot);
        this.add(m_pBox);

        // resize the window appropriately and show it
        setSize(m_plot.getPreferredSize());
        setTitle(m_seqSimModel.getModelName());
        repaint();
    }


    /**
     * Creates the sequence similarity graph and adds it to the containing frame.
     */
    private Plot createGraph() {
        Plot resultPlot = new Plot();

        int longestSequenceLength = getMaxSequenceLength();

        String queryWindow = null;
        String compWindow = null;

        int start;
        int end;

        double maxSim = 0;

        for (int sequencePos = 0; sequencePos < longestSequenceLength; sequencePos += m_stepSize) {
            start = sequencePos;
            end = sequencePos + m_windowSize;

            // only window if we are not already past the end of the query seq
            if (start < m_querySeq.length()) {
                // make sure we don't exceed the end of the query seq
                if ((start + m_windowSize) > m_querySeq.length()) {
                    end = m_querySeq.length();
                }
                queryWindow = m_querySeq.substring(start, end);
            }

            // window the comparison seqs
            for (int sequenceNum = 0; sequenceNum < m_seqs.length; sequenceNum++) {
                if (Debug.isOn()) {
                    System.out.println("windowing sequence #" + sequenceNum);
                }
                end = sequencePos + m_windowSize;
                if ((start + m_windowSize) > m_seqs[sequenceNum].length()) {
                    end = m_seqs[sequenceNum].length();
                }
                if (start < m_seqs[sequenceNum].length()) {
                    compWindow = m_seqs[sequenceNum].substring(start, end);
                }

                double similarity = m_seqSimModel.getSimilarity(queryWindow, compWindow);
                similarity = similarity * 100;

                if (Double.isNaN(similarity)) { // if similarity == NaN
                    similarity = 0;
                } else if (Double.isInfinite(similarity)) {
                    similarity = 1e9;
                }

                resultPlot.addPoint(sequenceNum, sequencePos, similarity, true);


                if (similarity > maxSim) {
                    maxSim = similarity;
                }

                if (Debug.isOn()) {
                    System.out.println("similarity = " + similarity);
                }
            }
        }

        // add legend entry for each sequence
        for (int sequenceNum = 0; sequenceNum < m_seqs.length; sequenceNum++) {
            resultPlot.addLegend(sequenceNum, m_seqs[sequenceNum].getName());
        }

        // set range and zoom to center graph
        resultPlot.setXRange(0, longestSequenceLength);
        resultPlot.setXLabel("Sequence Position");
        resultPlot.setYRange(0, 100);
        resultPlot.setYLabel("Similarity (%)");

        resultPlot.zoom(0, 0, longestSequenceLength, maxSim);

        return (resultPlot);

    }

    /**
     * Creates the sequence similarity graph and adds it to the containing frame.
     */
    private int getMaxSequenceLength() {
        int longestLength = m_querySeq.length();

        for (int i = 0; i < m_seqs.length; i++) {
            if (m_seqs[i].length() > longestLength) {
                longestLength = m_seqs[i].length();
            }
        }

        return (longestLength);
    }


    /**
     * Prompts user to input options needed when running the sequence similarity graph.
     *
     * @param seqs All the sequences which the graph will use. One of these will be selected
     *             as the query sequence, which the rest will be compared against.
     */
    public static void displayGraphOptions(FeaturedSequence[] seqs) {
        if (seqs.length < 2) {
            throw (new IllegalArgumentException());
        }

        // create components
        SequenceComboBoxItem scbi[] = new SequenceComboBoxItem[seqs.length];
        for (int i = 0; i < seqs.length; i++) {
            scbi[i] = new SequenceComboBoxItem(seqs[i]);
        }
        JComboBox selectQuerySeq = new JComboBox(scbi);

        JComboBox selectSimilarityModel = new JComboBox(SequenceSimilarityModelFactory.getModelNames());

        JTextField windowSize = new JTextField(10);
        JTextField stepSize = new JTextField(10);

        windowSize.setText(DEFAULT_WINDOW_SIZE + "");
        stepSize.setText(DEFAULT_STEP_SIZE + "");

        // create the layout and add components
        JPanel ssgOptionsPanel = new JPanel();
        ssgOptionsPanel.setLayout(new GridLayout(4, 2));
        ssgOptionsPanel.add(new JLabel("Query Sequence"));
        ssgOptionsPanel.add(selectQuerySeq);
        ssgOptionsPanel.add(new JLabel("Window Size"));
        ssgOptionsPanel.add(windowSize);
        ssgOptionsPanel.add(new JLabel("Step Size"));
        ssgOptionsPanel.add(stepSize);
        ssgOptionsPanel.add(new JLabel("Similarity Model"));
        ssgOptionsPanel.add(selectSimilarityModel);

        // display the dialog
        int ch = JOptionPane.showConfirmDialog(null, ssgOptionsPanel, "Sequence Similarity Graph Options", JOptionPane.OK_CANCEL_OPTION);

        if (ch == JOptionPane.OK_OPTION) {
            // get the data from dialogue and make the graph
            int ssgWindowSize = (int) Integer.parseInt(windowSize.getText());
            int ssgStepSize = (int) Integer.parseInt(stepSize.getText());
            FeaturedSequence ssgQuerySeq = ((SequenceComboBoxItem) selectQuerySeq.getSelectedItem()).getSequence();
            FeaturedSequence[] ssgSeqs = new FeaturedSequence[seqs.length - 1];
            SequenceSimilarityModel ssModel = SequenceSimilarityModelFactory.createSequenceSimilarityModel((String) selectSimilarityModel.getSelectedItem());

            int j = 0;
            for (int i = 0; i < seqs.length; i++) {
                if (seqs[i] == ssgQuerySeq) {
                    continue;
                } else {
                    ssgSeqs[j++] = seqs[i];
                }
            }

            try {
                SequenceSimilarityGraph ssg = new SequenceSimilarityGraph(ssgQuerySeq, ssgSeqs, ssModel, ssgWindowSize, ssgStepSize);
            } catch (IllegalArgumentException eax) {
                UITools.showError(eax.getMessage(), null);
                displayGraphOptions(seqs);
            }
        }

    }

}
