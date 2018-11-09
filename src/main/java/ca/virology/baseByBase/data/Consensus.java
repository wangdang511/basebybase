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

import org.biojava.bio.seq.StrandedFeature;

import java.awt.Color;


/**
 * This represents a consensus calculator
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public interface Consensus
{
    //~ Instance fields ////////////////////////////////////////////////////////

    Consensus EMPTY_CONSENSUS = new EmptyConsensus();

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the name of the consensus
     *
     * @return the name
     */
    public String getName();

    /**
     * returns the value for the given sequence position across the alignment.
     *
     * @param index The index of the sequence position to query
     *
     * @return An integer between 0.0 and 100.0 inclusive
     */
    public double getValue(int index);

    /**
     * get the color of the consensus for a particular index along the
     * consensus sequence.  This is used for bar-graphing.
     *
     * @param index the position to get the color for
     *
     * @return the color
     */
    public Color getColor(int index);

    /**
     * get the consensus sequence for a particular range
     *
     * @param start beginning of the range
     * @param stop end of the range
     *
     * @return the sequence string for this range
     */
    public String getSequence(
        int start,
        int stop);

    /**
     * Set the strand that we're talking about for this consensus
     *
     * @param strand the strand
     */
    public void setStrand(StrandedFeature.Strand strand);

    /**
     * get the length of the consensus sequence
     *
     * @return the length
     */
    public int getLength();

    /**
     * get the strand calculated by this consensus
     *
     * @return the strand
     */
    public StrandedFeature.Strand getStrand();

    /**
     * do the consensus calculations
     */
    public void calculate();

    //~ Inner Classes //////////////////////////////////////////////////////////

    final class EmptyConsensus
        implements Consensus
    {
        public String getName()
        {
            return "";
        }

        public double getValue(int index)
        {
            return 0.0;
        }

        public Color getColor(int index)
        {
            return Color.gray;
        }

        public String getSequence(
            int start,
            int stop)
        {
            return "";
        }

        public void setStrand(StrandedFeature.Strand strand)
        {
        }

        public int getLength()
        {
            return 0;
        }

        public StrandedFeature.Strand getStrand()
        {
            return StrandedFeature.POSITIVE;
        }

        public void calculate()
        {
        }
    }
}