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

import ca.virology.baseByBase.data.DiffEditFeaturedSequence;
import ca.virology.baseByBase.gui.PopulateGenesPanel;
import ca.virology.baseByBase.sql.BBBSQLSelects;
import ca.virology.lib.io.sequenceData.FeatureType;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.io.tools.FeatureTools;
import ca.virology.lib.util.gui.DBChooser;
import ca.virology.lib.vocsdbAccess.SQLAccess;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.RangeLocation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


/**
 * this class contains various tools for dealing with vocs databases
 *
 * @author Ryan Brodie
 * @version $Revision: 1.3 $
 */
public class VocsTools {
    /**
     * get a featured sequence from the vocs database
     *
     * @param virusId the virus id
     * @param dbName  the name of the database
     * @return a featured sequence with genes
     * @throws IOException if there's a db problem
     */
    public static synchronized FeaturedSequence getGenomeData(int virusId, String dbName) throws IOException {
        Vector result = null;

        try {
            result = BBBSQLSelects.virusDNASequenceSelect(virusId, new SQLAccess(dbName, "BaseByBase"));
        } catch (Exception ex) {
            databaseError();
        }

        if (result == null) {
            databaseError();
        }

        String vname = ((Object[]) result.get(0))[0].toString();
        String dna = ((Object[]) result.get(0))[2].toString();

        FeaturedSequence retSeq = new DiffEditFeaturedSequence(virusId, vname, dna);

        try {
            result = BBBSQLSelects.geneOrfInfoSelect(virusId, new SQLAccess(dbName, "BaseByBase"));

        } catch (Exception ex) {
            databaseError();
        }

        if (result == null) {
            databaseError();
        }

        for (int i = 0; i < result.size(); ++i) {
            Object[] data = (Object[]) result.elementAt(i);
            int gid = Integer.parseInt(data[0].toString());
            String name = data[1].toString();
            //      String product = "";
            int start = Integer.parseInt(data[2].toString()); // 1 corrected
            int stop = Integer.parseInt(data[3].toString()); // 1 corrected

            if (data[4].toString().equals("-")) {
                int temp;
                temp = start;
                start = stop;
                stop = temp;
            }

            StrandedFeature.Strand strand = (data[4].toString().toUpperCase().equals("TOP") || data[4].toString().toUpperCase().equals("+")) ? StrandedFeature.POSITIVE : StrandedFeature.NEGATIVE;

            if (!FeatureTools.createGeneFeature(retSeq, gid, name, new RangeLocation(start, stop), strand, FeatureType.AUTO_GENERATED)) {
                error("Could not create feature");
            }
        }

        return (retSeq);
    }

    /**
     * setup the sequence to be a vocs featured sequence with gene features
     * etc.
     *
     * @param virusId the id from the vocs database
     * @param seq     the sequence to set up
     * @param dbName  the name of the database
     * @throws IOException
     */
    public static synchronized void setupSequence(int virusId, FeaturedSequence seq, String dbName) throws IOException {
        if (virusId < 0) {
            return;
        }

        Vector result = null;

        try {
            result = BBBSQLSelects.virusNameandSizeSelect(virusId, new SQLAccess(dbName, "BaseByBase"));
        } catch (Exception ex) {
            databaseError();
        }

        if (result == null) {
            databaseError();
        }

        String vname = ((Object[]) result.get(0))[0].toString();
        Double sized = new Double(((Object[]) result.get(0))[1].toString());
        int size = (int) sized.doubleValue();

        System.out.println(virusId + ": db(" + size + "), seq(" + seq.sequenceLength() + ")");

        if (size != seq.sequenceLength()) {
            throw (new IOException("Sequence length mismatch, cannot map genes."));
        }

        // set the virus name in the sequence
        seq.setName(vname);
        seq.setId(virusId);

        result = null;

        try {
            result = BBBSQLSelects.geneOrfInfoSelect(virusId, new SQLAccess(dbName, "BaseByBase"));
        } catch (Exception ex) {
            databaseError();
        }

        if (result == null) {
            databaseError();
        }

        FeatureFilter ff = new FeatureFilter.ByType(FeatureType.GENE);
        FeatureHolder genes = seq.filter(ff, false);

        for (Iterator i = genes.features(); i.hasNext(); ) {
            try {
                seq.removeFeature((Feature) i.next());
            } catch (org.biojava.utils.ChangeVetoException cve) {
                // do nothing
            } catch (org.biojava.bio.BioException e) {
            }
        }

        for (int i = 0; i < result.size(); ++i) {
            Object[] data = (Object[]) result.elementAt(i);
            int gid = Integer.parseInt(data[0].toString());
            String name = data[1].toString();
            //      String product = "";
            int start = Integer.parseInt(data[2].toString()); // 1 corrected
            int stop = Integer.parseInt(data[3].toString()); // 1 corrected

            if (data[4].toString().equals("-")) {
                int temp;
                temp = start;
                start = stop;
                stop = temp;
            }

            StrandedFeature.Strand strand = (data[4].toString().toUpperCase().equals("TOP") || data[4].toString().toUpperCase().equals("+")) ? StrandedFeature.POSITIVE : StrandedFeature.NEGATIVE;

            if (!FeatureTools.createGeneFeature(seq, gid, name, new RangeLocation(start, stop), strand, FeatureType.AUTO_GENERATED)) {
                error("Could not create feature");
            }
        }
    }

    /**
     * Returns the name of a gene's ortholog as a String, given a geneName (as a String)
     *
     * @param geneName
     * @return the gene ortholog name
     * @throws IOException if an error occurs
     */
    public static String getGeneOrthologName(String geneName, String dbName) throws IOException {
        if (geneName.length() == 0)
            return "";

        String result = "";
        try {
            result = BBBSQLSelects.geneOrthologNameSelect(geneName, new SQLAccess(dbName, "BaseByBase"));
        } catch (Exception e) {
            databaseError();
        }

        return result;
    }

    /**
     * Returns a map of the form <CODE>[ String, Integer ]</CODE> where the
     * integer is a virus ID and the string is the name for the corresponding
     * virusID.
     *
     * @param dbName the name of the database
     * @return The virusIDMap value
     * @throws IOException if an error occurs
     */
    public static Map getVirusIDMap(String dbName)
    // throws IOException
    {
        Map returnVal = new HashMap();

        Vector result = null;


        try {
            result = BBBSQLSelects.virusIDandNameSelect(new SQLAccess(dbName, "BaseByBase"));
        } catch (Exception ex) {
            System.out.println(ex);
            // databaseError();
        }

        try {
            for (int i = 0; i < result.size(); ++i) {
                Object obj = result.get(i);
                returnVal.put(((Object[]) obj)[1], ((Object[]) obj)[0]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            returnVal = new HashMap();
        }

        return (returnVal);
    }

    /**
     * pop a window to map genes to sequences
     *
     * @param seqs the sequence to set up
     */
    public static void guiSetupSequences(final FeaturedSequence[] seqs) {
        int value = -999;

        DBChooser dbChooser = new DBChooser(null, null);
        String newDB = dbChooser.chooseDB();

        if (newDB == null) {
            return;
        }

        final PopulateGenesPanel pan = new PopulateGenesPanel(seqs, newDB);
        final JFrame frame = new JFrame("Import Genes from Vocs Database");
        frame.getRootPane().setLayout(new BorderLayout());
        frame.getRootPane().add(pan, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pan.confirmSequences();
                frame.dispose();
            }
        });
        buttonsPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        buttonsPanel.add(cancelButton);
        frame.getRootPane().add(buttonsPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * throw an exception with a format error message
     *
     * @throws IOException
     */
    protected static void formatError() throws IOException {
        throw (new IOException("File Format Error"));
    }

    /**
     * throw an exception with a db error message
     *
     * @throws IOException
     */
    protected static void databaseError() throws IOException {
        throw (new IOException("Couldn't load data from Vocs Database"));
    }

    /**
     * throw an exception with a customized error message
     *
     * @param message the message to send back
     * @throws IOException
     */
    protected static void error(String message) throws IOException {
        throw (new IOException("Error: " + message));
    }
}
