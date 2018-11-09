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
package ca.virology.baseByBase.util;

import ca.virology.lib.io.tools.*;

import java.util.*;


/**
 * This class handles calculation of amino acid coding comparisons between two
 * strings.
 *
 * @author Ryan Brodie
 * @version $Revision: 1.1.1.1 $
 */
public class CodingComparison {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static final int UNKNOWN = -1;
    protected static final int MISSING = 1000;
    protected static final int ADDED = 1001;
    protected static final int CHANGE = 1002;
    protected static final int STOP1 = 1003;
    protected static final int STOP2 = 1004;
    protected static final int SILENT = 1005;

    //~ Instance fields ////////////////////////////////////////////////////////

    public List missing = new ArrayList();
    public List added = new ArrayList();
    public List silent = new ArrayList();
    public List stops1 = new ArrayList();
    public List stops2 = new ArrayList();
    public List changes = new ArrayList();
    public int frameshift1 = -1;
    public int frameshift2 = -1;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new CodingComparison object.
     *
     * @param query    The query sequence
     * @param standard The standard sequence against which comparisons  are
     *                 made
     */
    public CodingComparison(
            String query,
            String standard) {
        StringBuffer s1 = new StringBuffer(query);
        StringBuffer s2 = new StringBuffer(standard);

        process(s1, s2);
        process(s2, s1);

        for (int i = 0; (((i + 2) < s1.length()) && ((i + 2) < s2.length()));
             i += 3) {
            String cd1 = s1.substring(i, i + 3);
            String cd2 = s2.substring(i, i + 3);
            compare(cd1, cd2);
        }

        //System.out.println("======");
        //System.out.println(query);
        //System.out.println(standard);
        //System.out.println(s1);
        //System.out.println(s2);
        //System.out.println("Added: "+added.size());
        //System.out.println(added+"\n----");
        //System.out.println("Missing: "+missing.size());
        //System.out.println(missing+"\n----");
        //System.out.println("Silent: "+silent.size());
        //System.out.println(silent+"\n----");
        //System.out.println("Stops in 1: "+stops1.size());
        //System.out.println(stops1+"\n----");
        //System.out.println("Stops in 2: "+stops2.size());
        //System.out.println(stops2+"\n----");
        //System.out.println("Changes: "+changes.size());
        //System.out.println(changes+"\n----");
        //System.out.println("Frameshift in 1 @ "+frameshift1);
        //System.out.println("Frameshift in 2 @ "+frameshift2);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Process two string buffers to align them for proper comparison
     *
     * @param s1 the first string buffer
     * @param s2 the second buffer
     */
    protected void process(
            StringBuffer s1,
            StringBuffer s2) {
        StringBuffer codon1 = null;
        StringBuffer codon2 = null;
        int i = 0;
        int j = 0;
        int cdstart = 0;
        int leadgaps = 0;

        for (i = 0; ((i < s1.length()) && (i < s2.length())); ++i) {
            //System.out.println(s1);
            //System.out.println(s2);
            //for (int k=0; k<i; ++k ) {
            //    System.out.print(' ');
            //}
            //System.out.println('^');
            //System.out.println(i);
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);

            codon1 = new StringBuffer();
            codon2 = new StringBuffer();

            if (Character.isLetter(c1)) {
                codon1.append(c1);

                for (j = i + 1; j < s1.length(); ++j) {
                    if (Character.isLetter(s1.charAt(j))) {
                        codon1.append(s1.charAt(j));

                        if (codon1.length() == 3) {
                            break;
                        }
                    } else {
                        s2.insert(j, '-');
                    }
                }

                //System.out.println((j-i-2)+" gaps in codon at "+i+": "+codon1);
                int gaps = (j - i - 3) + 1;

                for (int k = 0; k < (gaps + leadgaps); ++k) {
                    if (j < (s1.length() - 1)) {
                        s1.insert(j + 1, '-');
                    } else {
                        s1.append('-');
                    }
                }

                i = j;
                cdstart = i + 1;
                leadgaps = 0;
            } else {
                int k = i;
                boolean cdfound = false;

                for (k = i; k < s1.length() && k < s2.length(); ++k) {
                    if (s1.charAt(k) == '-') {
                        if (Character.isLetter(s2.charAt(k))) {
                            codon2.append(s2.charAt(k));

                            if (codon2.length() == 3) {
                                //System.out.println("codon!");
                                i = k;
                                cdfound = true;

                                break;
                            }
                        }
                    } else {
                        cdfound = false;

                        break;
                    }
                }

                if (!cdfound) {
                    //System.out.println("gap");
                    s2.insert(cdstart, '-');
                    ++leadgaps;

                    //s1.append('-');
                }
            }
        }

        for (i = 0; ((i < s1.length()) && (i < s2.length())); ++i) {
            if ((s1.charAt(i) == '-') && (s2.charAt(i) == '-')) {
                //System.out.println("boo");
                s1.deleteCharAt(i);
                s2.deleteCharAt(i);
                --i;
            }
        }
    }

    /**
     * Compare two codons and count the difference appropriately
     *
     * @param codon1 first codon
     * @param codon2 second codon
     * @return the type of difference (constant static member)
     */
    protected int compare(
            String codon1,
            String codon2) {
        String aa1 = SequenceTools.getAminoAcid(codon1);
        String aa2 = SequenceTools.getAminoAcid(codon2);

        if (codon1.equals("---")) {
            missing.add(codon2 + "|" + aa2);

            return MISSING;
        } else if (codon2.equals("---")) {
            added.add(codon1 + "|" + aa1);

            return ADDED;
        } else if (aa1.equals(aa2) && !codon1.equals(codon2)) {
            //silent
            //System.out.println(codon1+","+codon2+"->"+aa1+", silent");
            silent.add(1 + "," + codon1 + "|" + 2 + "," + codon2 + "|" + aa1);

            return SILENT;
        } else if (aa1.equals("*") && !aa2.equals("*")) {
            // aa1 truncated / aa2 elongated
            //System.out.println("Stop codon in 1, not in 2");
            stops1.add(1 + "," + codon1 + "|" + 2 + "," + codon2);

            return STOP1;
        } else if (aa2.equals("*") && !aa1.equals("*")) {
            // aa2 truncated / aa1 elongated
            //System.out.println("Stop codon in 2, not in 1");
            stops2.add(2 + "," + codon2 + "|" + 1 + "," + codon1);

            return STOP2;
        } else if (!aa1.equals(aa2)) {
            // change
            //System.out.println("aa change: "+aa1+"->"+aa2);
            changes.add(1 + "," + codon1 + "," + aa1 + "|" + 2 + "," + codon2 +
                    "," + aa2);

            return CHANGE;
        } else {
            return UNKNOWN;
        }
    }
}