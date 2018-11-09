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
package ca.virology.baseByBase;

import ca.virology.baseByBase.data.DiffEditFeaturedSequence;
import ca.virology.baseByBase.gui.DiffEditorFrame;
import ca.virology.baseByBase.io.DiffEditFeaturedSequenceReader;
import ca.virology.baseByBase.util.Debug;
import ca.virology.lib.io.reader.FeaturedSequenceReader;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.prefs.AppClientPrefs;
import ca.virology.lib.prefs.BBBPrefs;
import ca.virology.lib.prefs.DBPrefs;
import ca.virology.lib.prefs.ExternalAppsPrefs;
import ca.virology.lib.util.common.Args;
import ca.virology.lib.util.common.ClientHitTracker;
import ca.virology.lib.util.common.UserVerification;
import ca.virology.lib.util.gui.UITools;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.Vector;


/**
 * The main Diff Editor class.  This launches the application as a process,
 * therefore when it closes, it will take the VM with it
 *
 * @author Ryan Brodie
 */
public class DiffEditor {
    //~ Instance fields ////////////////////////////////////////////////////////

    public static DiffEditorFrame frame;
    protected DiffEditorFrame m_frame;
    protected String m_frName;

    private Args pargs;
    private DBPrefs dbpref;
    private BBBPrefs bbbpref;
    private AppClientPrefs appclientpref;

    private static String s_dbName = null;
    public static BBBContext context = new BBBContext();
    //~ Methods ////////////////////////////////////////////////////////////////

    public static String getDbName() {
        if (s_dbName == null) {
            s_dbName = Args.getInstance().get_dbname();
        }
        return s_dbName;
    }

    public static void setDbName(String dbName) {
        s_dbName = dbName;
    }

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new DiffEditorLE object.
     *
     * @param proc If true, this will have control of the process it runs in,
     *             meaning it might kill the VM on close.
     */
    public DiffEditor(boolean proc) {
        System.out.println("DIFFEDITOR 1");
        pargs = Args.getInstance();
        dbpref = DBPrefs.getInstance();
        appclientpref = AppClientPrefs.getInstance();
        bbbpref = BBBPrefs.getInstance();

        ca.virology.baseByBase.util.AppConstants.APP_TITLE = "Base-by-Base: Multiple Alignment Editor";
        m_frame = new DiffEditorFrame(dbpref);
        ca.virology.baseByBase.util.AppConstants.setSequenceHolder(m_frame);
        m_frame.setProcess(proc);
        m_frame.setSize(DiffEditorFrame.DEFAULT_SIZE);
        UITools.positionCenter(m_frame);
        setFramePrefix("bbb");
    }

    /**
     * Creates a new DiffEditorLE object.
     *
     * @param proc     If true, this will kill the VM on close.
     * @param loadfile The file handle to get initial sequence data from.
     */
    public DiffEditor(boolean proc, File loadfile) {
        this(proc);
        System.out.println("DIFFEDITOR 2");
        try {
            FeaturedSequenceReader in = DiffEditFeaturedSequenceReader.createFeaturedSequenceReader(loadfile.getName());
            final ListIterator li = in.getSequences();

            Vector<FeaturedSequence> v = new Vector<FeaturedSequence>();
            while (li.hasNext()) {
                v.add((FeaturedSequence) li.next());
            }

            boolean rc = m_frame.loadSequences(v.toArray(new FeaturedSequence[v.size()]));
            if (!rc) {
                throw new java.io.IOException("Error loading sequence data");
            }
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates an instance of a BBB Lite app
     *
     * @param proc    If true, this will kill the VM on exit.
     * @param seqtext Sequence file data in a string
     * @param type    must be one of "aln", "bbb" or "fasta"
     */
    public DiffEditor(boolean proc, String seqtext, String type) {
        this(proc);
        System.out.println("DIFFEDITOR 3");
        try {
            m_frame.loadSequences(seqtext, type);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Constructor
     *
     * @param seqs the sequences to be loaded
     */
    public DiffEditor(FeaturedSequence[] seqs) {
        this(false);
        System.out.println("DIFFEDITOR 4");
        try {
            DiffEditFeaturedSequence[] diffEditSeqs = new DiffEditFeaturedSequence[seqs.length];
            for (int i = 0; i < seqs.length; i++) {
                diffEditSeqs[i] = new DiffEditFeaturedSequence(seqs[i]);
            }
            m_frame.loadSequences(diffEditSeqs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void setConnection(String dbName, String a, String b, String c, int d, String e, int f) {
        s_dbName = dbName;
    }


    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Set the prefix to be used in the 'name' property of frames created by
     * this application.  This is useful for identifying windows created by
     * BBB-Lite from outside.
     *
     * @param s The prefix
     */
    public void setFramePrefix(String s) {
        m_frName = s;
        m_frame.setName(s + "|main");
    }

    /**
     * Get the prefix for frames created by this app
     *
     * @return The prefix
     * <p>
     * see setFramePrefix(String)
     */
    public String getFramePrefix() {
        return m_frName;
    }

    /**
     * author Francesco Marass
     */
    public DiffEditorFrame getFrame() {
        return m_frame;
    }

    /**
     * Show the main BBB-Lite frame
     */
    public void show() {
        m_frame.setVisible(true);
    }

    /**
     * Hide the main BBB-Lite frame
     */
    public void hide() {
        m_frame.setVisible(false);
    }

    /**
     * Load sequence data from the given file
     *
     * @param file The file to load from
     */
    public void loadSequences(File file) {
        try {
            FeaturedSequenceReader in = DiffEditFeaturedSequenceReader.createFeaturedSequenceReader(file.getName());
            final ListIterator li = in.getSequences();

            Vector<FeaturedSequence> v = new Vector<FeaturedSequence>();
            while (li.hasNext()) {
                v.add((FeaturedSequence) li.next());
            }

            boolean rc = m_frame.loadSequences(v.toArray(new FeaturedSequence[v.size()]));
            if (!rc) {
                throw new Exception("Error loading sequence data");
            }
        } catch (Exception ex) {
            UITools.showError("Error loading " + file, m_frame);
            ex.printStackTrace();
        }
    }

    /**
     * The main application method
     *
     * @param args The parameters for this application
     */
    public static void main(String[] args) {
        boolean debug = false;


        Args pargs = new Args(args);

        // DM: The following will ensure the user has the required permissions to run the program.

        //intellij on dionysus (windows pc) does not clear the user verification stages. when committing, ensure the lines below are not commented!
        //For testing, you may comment out to skip the user verification. Alternatively, run configurations should be set to "DiffEditor (user verification)"

        //////d/user verification start

        	    try {
        	    	UserVerification userCheck = new UserVerification(pargs.get_username(), pargs.get_userVerificationServer(), "BB");
        	    	if (!userCheck.isValidUser()) {
        	    		JOptionPane.showMessageDialog(null, userCheck.getFirstErrorString(), "Permissions Error", JOptionPane.ERROR_MESSAGE);
        	    		System.exit(1);
        	    	}
        	    } catch (IOException e) {
        	    	JOptionPane.showMessageDialog(null, "ERROR: User Verification IOException", "Unexpected Error", JOptionPane.ERROR_MESSAGE);
        	    	System.err.println("ERROR: User Verification IOException");
        	    	System.exit(1);
        	    }
        ///user verification end
         


        DBPrefs dbpref = new DBPrefs(pargs.get_dbprefs());
        AppClientPrefs appclientpref = new AppClientPrefs(pargs.get_appclientprefs());
        BBBPrefs bbbpref = new BBBPrefs(pargs.get_bbbprefs());
        ExternalAppsPrefs extappspref = new ExternalAppsPrefs(pargs.get_externalprefs());
        System.out.println(pargs.get_externalprefs());
  ClientHitTracker tracker = new ClientHitTracker();
      tracker.doRecordHit(ClientHitTracker.APPNAME_BBB);

        debug = pargs.get_debug();
        Debug.setDebugging(debug);

        // ensure that macs have menus on their windows and not on the top
        System.setProperty("com.apple.macos.useScreenMenuBar", "false");
        System.setProperty("apple.laf.useScreenMenuBar", "false");

        boolean javaws = pargs.get_javawebstart();
        if (javaws) {
            System.setProperty("webstart.enabled", "true");
        } else {
            System.setProperty("webstart.enabled", "false");
        }

        frame = new DiffEditorFrame(dbpref);
        String inFile = pargs.get_bbbfile();
        ca.virology.baseByBase.util.AppConstants.setSequenceHolder(frame);
        frame.setProcess(true);
        frame.setSize(DiffEditorFrame.DEFAULT_SIZE);
        UITools.positionCenter(frame);
        if (inFile != null) {
            frame.setBBBfile(inFile);
        }

        frame.setVisible(true);

    }
}
