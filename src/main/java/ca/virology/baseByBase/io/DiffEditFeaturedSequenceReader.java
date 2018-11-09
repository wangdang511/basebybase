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

import ca.virology.lib.io.MultiFileFilter;
import ca.virology.lib.io.reader.*;

import ca.virology.baseByBase.data.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.util.ListIterator;


/**
 * This class defines behaviour all io sequence readers will need to implement
 * to be useful to this application
 *
 * @author Ryan Brodie
 */
public abstract class DiffEditFeaturedSequenceReader extends FeaturedSequenceReader {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * create a reader for the given file
     *
     * @param seqText the contents of the file in a string
     * @param ext     the extension of the original file
     * @return a reader for the file
     * @throws IllegalArgumentException
     */
    public static FeaturedSequenceReader createFeaturedSequenceReader(String seqText, String ext) throws IllegalArgumentException {
        ext = ext.toUpperCase();

        try {
            if (ext.equals("BBB") || ext.equals("XML") || ext.equals("BSML")) {
                return new BSMLFeaturedSequenceReader(new StringReader(seqText), DiffEditFeaturedSequence.class);
            } else if (ext.equals("ALN")) {
                return new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.CLUSTAL_FORMAT, seqText, DiffEditFeaturedSequence.class);
            } else if (ext.equals("FASTA")) {
                return new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.FASTA_FORMAT, seqText, DiffEditFeaturedSequence.class);
            } else {
                throw new IllegalArgumentException("IO Error Reading for type " + ext);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("IO Error Reading for type " + ext);
        }
    }

    /**
     * create a reader for the given file
     *
     * @param seqText the contents of the file in a string
     * @param ext     the extension of the original file
     * @return a reader for the file
     * @throws IllegalArgumentException
     */
    public static FeaturedSequenceReader createFeaturedSequenceReader(File file) throws IllegalArgumentException {
        String filename = file.getAbsolutePath();
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            throw new IllegalArgumentException();
        }
        String ext = filename.substring(lastDot + 1);
        ext = ext.toUpperCase();

        try {
            if (ext.equals("BBB") || ext.equals("XML") || ext.equals("BSML")) {
                return new BSMLFeaturedSequenceReader(filename, DiffEditFeaturedSequence.class);
            } else if (ext.equals("ALN")) {
                return new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.CLUSTAL_FORMAT, file, DiffEditFeaturedSequence.class);
            } else if (ext.equals("FASTA")) {
                return new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.FASTA_FORMAT, file, DiffEditFeaturedSequence.class);
            } else if (ext.equals("EMBL")) {
                return new EMBLFeaturedSequenceReader(filename, DiffEditFeaturedSequence.class);
            } else if (ext.equals("GBK")) {
                return new GenBankFeaturedSequenceReader(filename, DiffEditFeaturedSequence.class);
            } else if (ext.equals("GB")) {
                return new GenBankFeaturedSequenceReader(filename, DiffEditFeaturedSequence.class);
            } else if (ext.equals("GENBANK")) {
                return new GenBankFeaturedSequenceReader(filename, DiffEditFeaturedSequence.class);
            } else {
                throw new IllegalArgumentException("IO Error Reading for type " + ext);

            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("IO Error/Exception Reading for type " + ext);
        }
    }

    /**
     * create a reader for the given file
     *
     * @param seqText the contents of the file in a string
     * @param ext     the extension of the original file
     * @param dbName  the name of the database
     * @return a reader for the file
     * @throws IllegalArgumentException
     */
    public static FeaturedSequenceReader createFeaturedSequenceReader(String seqText, String ext, String dbName) throws IllegalArgumentException {
        return createFeaturedSequenceReader(seqText, ext);
    }

    /**
     * create a reader for the given file
     *
     * @param filename the file to read
     * @return a reader for the file
     * @throws IllegalArgumentException
     */
    public static FeaturedSequenceReader createFeaturedSequenceReader(String filename) throws IllegalArgumentException {
        String ext = "";

        try {
            ext = MultiFileFilter.getExtension(new File(filename)).toUpperCase();
            System.out.println("Attempting to create a " + ext + " reader");

            if (ext.equals("BBB") || ext.equals("XML") || ext.equals("BSML")) {
                return new BSMLFeaturedSequenceReader(filename, DiffEditFeaturedSequence.class);
            } else if (ext.equals("ALN")) {
                return new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.CLUSTAL_FORMAT, new File(filename), DiffEditFeaturedSequence.class);
            } else if (ext.equals("FASTA")) {
                return new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.FASTA_FORMAT, new File(filename), DiffEditFeaturedSequence.class);
            } else if (ext.equals("EMBL")) {
                return new EMBLFeaturedSequenceReader(filename, DiffEditFeaturedSequence.class);
            } else if (ext.equals("GBK")) {
                return new GenBankFeaturedSequenceReader(filename, DiffEditFeaturedSequence.class);
            } else if (ext.equals("GB")) {
                return new GenBankFeaturedSequenceReader(filename, DiffEditFeaturedSequence.class);
            } else if (ext.equals("GENBANK")) {
                return new GenBankFeaturedSequenceReader(filename, DiffEditFeaturedSequence.class);
            } else {
                throw new IllegalArgumentException("Unknown Reader Type " + ext);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException("IO Error Reading " + filename +
                    " for type " + ext);
        }
    }

    /**
     * Returns an iterator to a list of sequences.
     *
     * @return an iterator over a list of sequences
     */
    public abstract ListIterator getSequences() throws IOException;
}
