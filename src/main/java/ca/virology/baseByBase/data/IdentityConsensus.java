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
package ca.virology.baseByBase.data;

import ca.virology.lib.color.GenericAnalysisColorGenerator;
import ca.virology.lib.io.sequenceData.EditableSequence;

import java.awt.Color;


/**
 * This is a consensus based on %identity
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class IdentityConsensus
        extends AbstractConsensus {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static final Color DEF_COLOR = Color.red;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected Color[] m_colors;
    protected double[] m_values;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new IdentityConsensus object.
     *
     * @param seqs The sequences to target
     */
    public IdentityConsensus(EditableSequence[] seqs) {
        super(seqs);

        GenericAnalysisColorGenerator cg =
                new GenericAnalysisColorGenerator(Color.blue, Color.red, 11);
        m_colors = cg.getColors();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * returns the value for the given sequence position across the alignment.
     *
     * @param index The index of the sequence position to query
     * @return An integer between 0.0 and 100.0 inclusive
     */
    public double getValue(int index) {
        if ((index < 0) || (index >= m_values.length)) {
            return 0.0;
        } else {
            return m_values[index];
        }
    }

    /**
     * Get the name of the consensus
     *
     * @return the name
     */
    public String getName() {
        return "% Identity";
    }

    /**
     * get the color of the consensus for a particular index along the
     * consensus sequence.  This is used for bar-graphing.
     *
     * @param index the position to get the color for
     * @return the color
     */
    public Color getColor(int index) {
        double val = 0.0;

        if ((index < 0) || (index >= m_values.length)) {
            val = 0.0;
        } else {
            val = m_values[index];
        }

        int i = (int) Math.floor(val);
        i = i / 10;

        return m_colors[i];
    }

    /**
     * do the consensus calculations
     */
    public void calculate() {
        m_length = 0;

        for (int i = 0; i < m_seqs.length; ++i) {
            if (m_seqs[i].length() > m_length) {
                m_length = m_seqs[i].length();
            }
        }

//        System.out.println("Calculating For Length " + getLength());
        m_cons = new StringBuffer();

        m_values = new double[getLength()];

        int[] totals = new int[getLength()];
        int[][] fTable = new int[getLength()][AminoAcid.COUNT];

        for (int i = 0; i < getLength(); ++i) {
            java.util.Arrays.fill(fTable[i], 0);
        }

        java.util.Arrays.fill(totals, 0);

        for (int i = 0; i < getLength(); ++i) {
            for (int j = 0; j < m_seqs.length; ++j) {
                if (i >= m_seqs[j].length()) {
                    continue;
                }

                fTable[i][AminoAcid.valueOf(m_seqs[j].charAt(i))]++;
                totals[i]++;
            }
        }

        for (int i = 0; i < getLength(); ++i) {
            int max = 0;
            int maxInd = 0;

            for (int j = 0; j < fTable[i].length; ++j) {
                if (fTable[i][j] > max) {
                    max = fTable[i][j];
                    maxInd = j;
                }
            }

            if (((double) fTable[i][maxInd] / (double) totals[i]) <= 0.51) {
                if (m_seqs[m_seqs.length - 1].getSequenceType() == EditableSequence.AA_SEQUENCE) {
                    m_cons.append('X');
                } else {
                    m_cons.append('N');
                }

                m_values[i] = 0.0;
            } else {
                m_cons.append(
                        Character.toUpperCase(AminoAcid.charValue(maxInd)));

                if (maxInd == AminoAcid.GAP) {
                    m_values[i] = 0.0;
                } else {
                    m_values[i] = (double) max / (double) totals[i] * 100.0;
                }
            }
        }

        // cleanup
        totals = null;
        fTable = null;
        System.gc();
    }
}
