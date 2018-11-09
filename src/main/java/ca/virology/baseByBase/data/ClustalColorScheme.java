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
public class ClustalColorScheme
    implements ColorScheme
{
    //~ Instance fields ////////////////////////////////////////////////////////

    protected Object             m_lock = new Object();
    protected Color[]            m_colors = new Color[AminoAcid.COUNT];
    protected EditableSequence[] m_seqs;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Constructs a <CODE>ColorScheme</CODE> for the sequence passed in.
     */
    public ClustalColorScheme()
    {
        Color blue = new Color(128, 128, 255);

        m_colors[AminoAcid.G] = Color.orange;
        m_colors[AminoAcid.P] = Color.orange;
        m_colors[AminoAcid.S] = Color.orange;
        m_colors[AminoAcid.T] = Color.orange;
        m_colors[AminoAcid.H] = Color.red;
        m_colors[AminoAcid.K] = Color.red;
        m_colors[AminoAcid.R] = Color.red;
        m_colors[AminoAcid.F] = blue;
        m_colors[AminoAcid.W] = blue;
        m_colors[AminoAcid.Y] = blue;
        m_colors[AminoAcid.I] = Color.green;
        m_colors[AminoAcid.L] = Color.green;
        m_colors[AminoAcid.M] = Color.green;
        m_colors[AminoAcid.V] = Color.green;
        m_colors[AminoAcid.A] = Color.orange;
        m_colors[AminoAcid.C] = Color.red;
        m_colors[AminoAcid.T] = blue;
        m_colors[AminoAcid.G] = Color.green;

        for (int i = 0; i < m_colors.length; ++i) {
            if (m_colors[i] == null) {
                m_colors[i] = Color.white;
            }
        }
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * set the sequences used by this scheme to calculate the  appropriate
     * colors.
     *
     * @param seqs the array of sequences
     */
    public void setSequences(EditableSequence[] seqs)
    {
        synchronized (m_lock) {
            m_seqs = seqs;
        }
    }

    /**
     * Get the backgrond of the given index position for the given sequence
     *
     * @param seq The sequence to use
     * @param index The index in the given sequence
     *
     * @return the background color
     */
    public Color getBackground(
        EditableSequence seq,
        int index)
    {
        char c = seq.charAt(index);

        return getColor(c);
    }

    /**
     * Get the foregrond of the given index position for the given sequence
     *
     * @param seq The sequence to use
     * @param index The index in the given sequence
     *
     * @return the foreground color
     */
    public Color getForeground(
        EditableSequence seq,
        int index)
    {
        return Color.black;
    }

    /**
     * gets the color associated with the particular character
     *
     * @param c the character to color
     *
     * @return the color for this character
     */
    protected Color getColor(char c)
    {
        c = Character.toUpperCase(c);

        int aa = AminoAcid.valueOf(c);

        if (aa >= m_colors.length) {
            return Color.white;
        } else {
            return m_colors[aa];
        }
    }
}