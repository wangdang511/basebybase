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
 * This is a generic color scheme for painting DNA characters to the screen.
 *
 * @author Ryan brodie
 */
public class DNAColorScheme
        implements ColorScheme {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected Object m_lock = new Object();
    protected Color m_aColor;
    protected Color m_cColor;
    protected Color m_tColor;
    protected Color m_gColor;
    protected Color m_gapColor;
    protected EditableSequence[] m_seqs;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Constructs a <CODE>DNAColorScheme</CODE> for the sequence passed in.
     */
    public DNAColorScheme() {
        m_aColor = Color.yellow;
        m_cColor = Color.green;
        m_tColor = Color.orange;
        m_gColor = Color.cyan;
        m_gapColor = Color.white;
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

        return getColor(c);
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
        return Color.black;
    }

    /**
     * gets the color associated with the particular character
     *
     * @param c the character to color
     * @return the color for this character
     */
    protected Color getColor(char c) {
        c = Character.toUpperCase(c);

        switch (c) {
            case 'A':
                return m_aColor;

            case 'C':
                return m_cColor;

            case 'T':
                return m_tColor;

            case 'G':
                return m_gColor;

            default:
                return m_gapColor;
        }
    }
}