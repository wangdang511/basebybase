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
public class HydrophobicityColorScheme
        implements ColorScheme {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected Object m_lock = new Object();
    protected EditableSequence[] m_seqs;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Constructs a <CODE>ColorScheme</CODE> for the sequence passed in.
     */
    public HydrophobicityColorScheme() {
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * set the sequences used by this scheme to calculate the  appropriate
     * colors.
     *
     * @param seqs the array of sequences
     */
    public void setSequences(EditableSequence[] seqs) {
        synchronized (m_lock) {
            m_seqs = seqs;
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
        char c = seq.charAt(index);

        if ((c == '*') || (c == '-') || (c == ' ')) {
            return Color.white;
        }

        double score = AminoAcid.getHydrophobicityNorm(AminoAcid.valueOf(c));

        if (score > 1.0) {
            score = 1.0;
        }

        if (score < 0.0) {
            score = 0.0;
        }

        return new Color((float) score, (float) 0.0, (float) (1.0 - score));
    }

    /**
     * Get the foregrond of the given index position for the given sequence
     *
     * @param seq   The sequence to use
     * @param index The index in the given sequence
     * @return the foreground color
     */
    public Color getForeground(
            EditableSequence seq,
            int index) {
        char c = seq.charAt(index);

        if ((c == '*') || (c == '-') || (c == ' ')) {
            return Color.black;
        }

        return Color.white;
    }
}