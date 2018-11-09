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
package ca.virology.baseByBase.io;

import ca.virology.lib.io.sequenceData.*;
import ca.virology.lib.io.reader.*;

import java.io.*;
import java.util.*;


/**
 * Decorator class for getting gene features from the vocs db
 */
public class VocsFeaturedSequenceReader extends FeaturedSequenceReader {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected FeaturedSequenceReader m_reader;
    protected ArrayList m_list = new ArrayList();
    protected boolean m_parsed;
    protected String m_dbName;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new VocsFeaturedSequenceReader object.
     *
     * @param seqReader the reader to get sequences from
     * @param dbName    the name of the database
     * @throws IOException
     */
    public VocsFeaturedSequenceReader(FeaturedSequenceReader seqReader, String dbName) throws IOException {
        m_reader = seqReader;
        m_dbName = dbName;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the sequences from the input source provided
     *
     * @return An iterator over a list of FeaturedSequences
     * @throws IOException if there is a problem parsing the file
     */
    public ListIterator getSequences() throws IOException {
        ListIterator seqs = m_reader.getSequences();

        try {
            while (seqs.hasNext()) {
                FeaturedSequence s = (FeaturedSequence) seqs.next();
                VocsTools.setupSequence(s.getId(), s, m_dbName);
                m_list.add(s);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return m_list.listIterator();
    }
}
