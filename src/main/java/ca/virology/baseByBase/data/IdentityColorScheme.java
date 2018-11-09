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
import ca.virology.lib.io.tools.SequenceTools;

import org.biojava.bio.seq.StrandedFeature;

import java.awt.Color;


/**
 * This is a generic color scheme for painting characters to the screen.
 *
 * @author Ryan brodie
 */
public class IdentityColorScheme
        implements StrandedColorScheme {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected Object m_lock = new Object();
    protected Color[] m_colors = new Color[26];
    protected Color m_gapColor;
    protected EditableSequence[] m_seqs;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Constructs a <CODE>ColorScheme</CODE> for the sequence passed in.
     */
    public IdentityColorScheme() {
        m_colors['A' - 'A'] = Color.yellow;
        m_colors['C' - 'A'] = Color.green;
        m_colors['T' - 'A'] = Color.orange;
        m_colors['G' - 'A'] = Color.cyan;

        m_colors['B' - 'A'] = new Color(128, 128, 255);
        m_colors['D' - 'A'] = new Color(200, 200, 255);
        m_colors['E' - 'A'] = new Color(255, 128, 128);
        m_colors['F' - 'A'] = new Color(255, 200, 200);
        m_colors['H' - 'A'] = new Color(255, 255, 128);
        m_colors['I' - 'A'] = new Color(255, 255, 200);
        m_colors['J' - 'A'] = new Color(128, 255, 128);
        m_colors['K' - 'A'] = new Color(200, 255, 200);
        m_colors['L' - 'A'] = new Color(255, 128, 255);
        m_colors['M' - 'A'] = new Color(255, 200, 255);
        m_colors['N' - 'A'] = new Color(128, 255, 255);
        m_colors['O' - 'A'] = new Color(200, 255, 255);
        m_colors['P' - 'A'] = new Color(128, 200, 255);
        m_colors['Q' - 'A'] = new Color(200, 128, 255);
        m_colors['R' - 'A'] = new Color(128, 255, 200);
        m_colors['S' - 'A'] = new Color(200, 255, 128);
        m_colors['U' - 'A'] = new Color(255, 128, 200);
        m_colors['V' - 'A'] = new Color(255, 200, 128);
        m_colors['W' - 'A'] = Color.red;
        m_colors['X' - 'A'] = Color.white;
        m_colors['Y' - 'A'] = Color.pink;
        m_colors['Z' - 'A'] = Color.magenta;

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
     * Get the backgrond of the given index position for the given sequence
     *
     * @param st    The sequence to use
     * @param seq   The sequence to query
     * @param index The index in the given sequence
     * @return the background color
     */
    public Color getBackground(
            StrandedFeature.Strand st,
            EditableSequence seq,
            int index) {
        char c = seq.charAt(index);

        if (st == StrandedFeature.NEGATIVE) {
            c = SequenceTools.getDNAComplement(c);
        }

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
     * Get the foregrond of the given index position for the given sequence
     *
     * @param st    The strand to query
     * @param seq   The sequence to query
     * @param index The index in the given sequence
     * @return the foreground color
     */
    public Color getForeground(
            StrandedFeature.Strand st,
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

        if ((c < 'A') || (c > 'Z')) {
            return m_gapColor;
        } else {
            return m_colors[c - 'A'];
        }
    }
}