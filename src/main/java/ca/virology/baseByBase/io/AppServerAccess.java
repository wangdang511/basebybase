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

import ca.virology.baseByBase.util.Debug;
import ca.virology.lib.prefs.AppClientPrefs;
import ca.virology.lib.prefs.DBPrefs;
import ca.virology.lib.server.AppServerRequest;
import ca.virology.lib.server.ServerRequestException;
import ca.virology.lib.util.common.Args;

import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This provides access to the vocs server.  This is roughly equivalent to
 * ServerRequest in the vocs package.
 *
 * @author Ryan Brodie
 * @version $Revision: 1.1.1.1 $
 */
public class AppServerAccess {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static AppServerAccess c_instance = null;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new VocsDBAccess object.
     */
    protected AppServerAccess() {
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the instance of this class
     *
     * @return the singleton instance
     */
    public static AppServerAccess getInstance() {
        if (c_instance == null) {
            c_instance = new AppServerAccess();
        }
        return c_instance;
    }


    /**
     * Does a multiple alignment
     *
     * @param prg the alignmnet program to use (clustalo|t_coffee)
     * @param db  the sequences to align in fasta format
     * @return the alignment file (.aln)
     * @throws IOException if a problem occurs
     */
    public Vector doAlignmentQuery(String prg, Vector db) throws IOException {
        Vector ret = new Vector();
        Args pargs = Args.getInstance();
        DBPrefs dbpref = DBPrefs.getInstance();
        AppClientPrefs apppref = AppClientPrefs.getInstance();

        String appip = apppref.get_client_appserveraddress();
        int appport = apppref.get_client_appserverport();

        if (Debug.isOn()) {
            System.out.println("AppServerRequest: alignQuery " + prg +
                    " on server " + appip + " port " + appport);
        }
        try {
            AppServerRequest sreq = new AppServerRequest(appip, appport);
            sreq.setRequestorName("BBB");
            ret = sreq.alignQuery(prg, db);
            sreq.close();
        } catch (ServerRequestException ex) {
            System.out.println("SREqEX: ");
            ex.printStackTrace(System.out);
            throw new IOException(ex.getMessage());
        } catch (NullPointerException ex2) {
            ex2.printStackTrace();
        }

        // Clean up the sequence names
        if (prg.equals("muscle") || prg.equals("clustalo")) {
            Pattern sequencePattern = Pattern.compile("^\\d+;.*");
            for (int i = 0; i < ret.size(); i++) {
                String line = (String) ret.get(i);
                Matcher m = sequencePattern.matcher(line);
                if (m.matches()) {
                    ret.set(i, line.replaceFirst(";", "_"));
                }
            }
        }

        return ret;
    }
}
