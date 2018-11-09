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
 * This is the generic interface for color schemes used to paint sequence data
 * in the editor window.
 *
 * @author Ryan Brodie
 */
public interface ColorScheme
{
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final int BBB_SCHEME = 1000;
    public static final int SIM_SCHEME = 1001;
    public static final int PAM250_SCHEME = 1002;
    public static final int BLOSUM62_SCHEME = 1003;
    public static final int HYDRO_SCHEME = 1004;
    public static final int CLUSTAL_SCHEME = 1005;
    public static final int PCT_ID_SCHEME = 1006;
    public static final int CUSTOM_SCHEME = 1007;
    public static final int HID_SCHEME = 1008;

    public static final int DIFF_CLASSIC_SCHEME = 2000;
    public static final int DIFF_NT_SCHEME = 2001;

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * This returns the colour associated with the given sequence character
     *
     * @return the color of the character given
     */
    public Color getForeground(
        EditableSequence s,
        int index);

    /**
     * Get the backgrond of the given index position for the given sequence
     *
     * @param s The sequence to use
     * @param index The index in the given sequence
     *
     * @return the background color
     */
    public Color getBackground(
        EditableSequence s,
        int index);

    /**
     * set the sequences used by this scheme to calculate the  appropriate
     * colors.
     *
     * @param seqs the array of sequences
     */
    public void setSequences(EditableSequence[] seqs);
}