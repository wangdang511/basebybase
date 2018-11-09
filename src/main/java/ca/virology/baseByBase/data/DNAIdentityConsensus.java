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

import ca.virology.lib.io.sequenceData.EditableSequence;

import java.awt.Color;


/**
 * This is a DNA consensus based on %identity
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class DNAIdentityConsensus
        extends DNAConsensus {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static final Color DEF_COLOR = Color.red;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected Color m_color = DEF_COLOR;
    protected double[] m_values;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new DNAIdentityConsensus object.
     *
     * @param seqs The sequences to target
     */
    public DNAIdentityConsensus(EditableSequence[] seqs) {
        super(seqs);
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
        return m_color;
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

        //System.out.println("Calculating For Length " + getLength());
        m_cons = new StringBuffer();

        // 'local' constants
        final int A = 0;
        final int C = 1;
        final int T = 2;
        final int G = 3;
        final int GAP = 4;

        m_values = new double[getLength()];

        int[] totals = new int[getLength()];
        int[][] fTable = new int[getLength()][5];

        for (int i = 0; i < getLength(); ++i) {
            java.util.Arrays.fill(fTable[i], 0);
        }

        java.util.Arrays.fill(totals, 0);

        for (int i = 0; i < getLength(); ++i) {
            for (int j = 0; j < m_seqs.length; ++j) {
                if (i >= m_seqs[j].length()) {
                    continue;
                }

                if ((m_seqs[j].charAt(i) == 'A') ||
                        (m_seqs[j].charAt(i) == 'a')) {
                    fTable[i][A]++;
                    totals[i]++;
                }

                if ((m_seqs[j].charAt(i) == 'C') ||
                        (m_seqs[j].charAt(i) == 'c')) {
                    fTable[i][C]++;
                    totals[i]++;
                }

                if ((m_seqs[j].charAt(i) == 'T') ||
                        (m_seqs[j].charAt(i) == 't')) {
                    fTable[i][T]++;
                    totals[i]++;
                }

                if ((m_seqs[j].charAt(i) == 'G') ||
                        (m_seqs[j].charAt(i) == 'g')) {
                    fTable[i][G]++;
                    totals[i]++;
                }

                if (m_seqs[j].charAt(i) == '-') {
                    fTable[i][GAP]++;
                    totals[i]++;
                }
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

            switch (maxInd) {
                case A:
                    m_cons.append('A');

                    break;

                case C:
                    m_cons.append('C');

                    break;

                case T:
                    m_cons.append('T');

                    break;

                case G:
                    m_cons.append('G');

                    break;

                case GAP:
                    m_cons.append('-');

                    break;
            }

            if (maxInd == GAP) {
                m_values[i] = 0.0;
            } else {
                m_values[i] = (double) max / (double) totals[i] * 100.0;
            }
        }

        // cleanup
        totals = null;
        fTable = null;
        System.gc();
    }
}