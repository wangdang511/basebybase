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

import org.biojava.bio.seq.StrandedFeature;

import java.awt.Color;


/**
 * This is the generic interface for color schemes used to paint sequence data
 * in the editor window.
 *
 * @author Ryan Brodie
 */
public interface StrandedColorScheme
        extends ColorScheme {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * This returns the colour associated with the given sequence character
     *
     * @return the color of the character given
     */
    Color getForeground(
            StrandedFeature.Strand st,
            EditableSequence s,
            int index);

    /**
     * Get the backgrond of the given index position for the given sequence
     *
     * @param strand The strand to see
     * @param s      The sequence to use
     * @param index  The index in the given sequence
     * @return the background color
     */
    Color getBackground(
            StrandedFeature.Strand st,
            EditableSequence s,
            int index);

    /**
     * set the sequences used by this scheme to calculate the  appropriate
     * colors.
     *
     * @param seqs the array of sequences
     */
    void setSequences(EditableSequence[] seqs);
}