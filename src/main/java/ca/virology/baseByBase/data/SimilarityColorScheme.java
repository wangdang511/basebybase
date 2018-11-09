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
 * This is a generic color scheme for painting characters to the screen.
 *
 * @author Ryan brodie
 */
public class SimilarityColorScheme
        implements ColorScheme {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected EditableSequence[] m_seqs;
    protected Color[] m_colors;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Constructs a <CODE>ColorScheme</CODE> for the sequence passed in.
     */
    public SimilarityColorScheme() {
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * set the sequences used by this scheme to calculate the  appropriate
     * colors.
     *
     * @param seqs the array of sequences
     */
    public void setSequences(EditableSequence[] seqs) {
        m_seqs = seqs;
        m_colors = new Color[seqs.length];

        if (m_seqs.length > 2) {
            m_colors[0] = Color.white;
            m_colors[m_colors.length - 1] = Color.black;
        } else {
            return;
        }

        for (int i = seqs.length - 2; i > 0; --i) {
            int val = i * ((255 / seqs.length) - 1);
            m_colors[i] = new Color(val, val, val);
        }
    }

    /**
     * Get the backgrond of the given index position for the given sequence
     *
     * @param seq   The sequence to use
     * @param index The index in the given sequence
     * @return the background color
     */
    public Color getBackground(
            EditableSequence seq,
            int index) {
        // if the char is a gap, or there aren't enough sequences, colour blank
        if ((m_seqs.length < 3) || (index >= seq.length()) || (index < 0) || (Character.toUpperCase(seq.charAt(index)) == '-')) {
            return Color.white;
        }

        int[] cons = getConsCount(index);
        Color ret;

        // if the char doesn't match consensus, or there is no consensus, colour white
        if (Character.toUpperCase(seq.charAt(index)) == (char) cons[0]) {
            if (cons[1] == 0) {
                ret = Color.white;
            } else {
                // the char is consensus, so colour it a fixed colour for the number of sequences showing that char at this position
                ret = m_colors[cons[1] - 1];
            }
        } else {
            ret = Color.white;
        }

        return ret;
    }

    /**
     * This returns the colour associated with the given sequence character
     *
     * @param seq   The sequence to query
     * @param index The index of that particular sequence
     * @return the color of the character given
     */
    public Color getForeground(
            EditableSequence seq,
            int index) {
        if ((m_seqs.length < 3) || (index >= seq.length()) || (index < 0)) {
            return Color.black;
        }

        Color c = getBackground(seq, index);

        if (c.getRed() > 150) {
            return Color.black;
        } else {
            return Color.white;
        }
    }

    /**
     * get the consensus count at a given index
     *
     * @param index the index
     * @return the count
     */
    protected int[] getConsCount(int index) {
        int[] vals = new int[26];
        int gaps = 0;

        for (int i = 0; i < m_seqs.length; ++i) {
            if (index >= m_seqs[i].length()) {
                continue;
            }

            char c = Character.toUpperCase(m_seqs[i].charAt(index));

            if (c == '-') {
                gaps++;

                continue;
            }

            vals[(int) (c - 'A')]++;
        }

        int max = 0;
        int maxind = 0;

        for (int i = 0; i < 26; ++i) {
            if (vals[i] > max) {
                max = vals[i];
                maxind = i;
            }
        }

        int[] ret = new int[2];

        if (gaps > max) {
            ret[0] = (int) '-';
            ret[1] = gaps;
        } else {
            ret[0] = (int) ('A' + maxind);
            ret[1] = max;
        }

        return ret;
    }
}
