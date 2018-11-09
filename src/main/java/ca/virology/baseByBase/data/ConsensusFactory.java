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


/**
 * This is a utility class for retreiving different consensus types  for
 * display in base-by-base.
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public final class ConsensusFactory
{
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final int IDENTITY = 100;

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * create a consensus with no target sequences
     *
     * @param type the type of the consensus to create
     *
     * @return a consensus with nothing in it
     *
     * @throws IllegalArgumentException
     */
    public static Consensus createEmptyConsensus(int type)
    {
        switch (type) {
            case IDENTITY:
                return new IdentityConsensus(new EditableSequence[0]);
        }

        throw new IllegalArgumentException("Unknown Consensus Type");
    }

    /**
     * Create a consensus of the given type targeted at the given sequences
     *
     * @param type the type of the consensus
     * @param seqs the sequences to calculate
     *
     * @return a consensus as specified
     *
     * @throws IllegalArgumentException
     */
    public static Consensus createConsensus(
        int type,
        EditableSequence[] seqs)
        throws IllegalArgumentException
    {
        switch (type) {
            case IDENTITY:
                return new IdentityConsensus(seqs);
        }

        throw new IllegalArgumentException("Unknown Consensus Type");
    }
}