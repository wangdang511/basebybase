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
 * This is the superclass for all other consensus sequences that are calculated
 * over sequences
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public abstract class AbstractConsensus
    implements Consensus
{
    //~ Instance fields ////////////////////////////////////////////////////////

    protected StrandedFeature.Strand m_strand;
    protected EditableSequence[]     m_seqs;
    protected int                    m_length;
    protected StringBuffer           m_cons;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new Consensus object.
     *
     * @param seqs the sequences to target with calculations
     */
    public AbstractConsensus(EditableSequence[] seqs)
    {
        m_seqs = seqs;

        calculate();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

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
        int stop)
    {
        if (start <= 0) {
            start = 0;
        }

        if (stop <= 0) {
            return "";
        }

        if (stop >= m_cons.length()) {
            stop = m_cons.length();
        }

        if (start >= m_cons.length()) {
            return "";
        }

        return m_cons.substring(start, stop);
    }

    /**
     * Set the strand that we're talking about for this consensus
     *
     * @param strand the strand
     */
    public void setStrand(StrandedFeature.Strand strand)
    {
        m_strand = strand;
    }

    /**
     * get the length of the consensus sequence
     *
     * @return the length
     */
    public int getLength()
    {
        return m_length;
    }

    /**
     * get the strand calculated by this consensus
     *
     * @return the strand
     */
    public StrandedFeature.Strand getStrand()
    {
        return m_strand;
    }

    /**
     * do the consensus calculations
     */
    public abstract void calculate();

    /**
     * returns the value for the given sequence position across the alignment.
     *
     * @param index The index of the sequence position to query
     *
     * @return An integer between 0.0 and 100.0 inclusive
     */
    public abstract double getValue(int index);

    /**
     * Get the name of the consensus
     *
     * @return the name
     */
    public abstract String getName();

    /**
     * get the color of the consensus for a particular index along the
     * consensus sequence.  This is used for bar-graphing.
     *
     * @param index the position to get the color for
     *
     * @return the color
     */
    public abstract Color getColor(int index);
}