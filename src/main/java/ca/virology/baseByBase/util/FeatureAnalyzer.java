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

import ca.virology.lib.io.sequenceData.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

import java.util.*;


/**
 * This compares two features for important differences, returning the results
 * in a hash.
 *
 * @author Ryan Brodie
 * @version $Revision: 1.1.1.1 $
 */
public class FeatureAnalyzer {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static final int UPSTREAM = 200;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected EditableSequence m_qs;
    protected EditableSequence m_ss;
    protected StrandedFeature m_feat;
    protected int[] m_diffs;
    protected ArrayList m_headers;
    protected ArrayList m_data;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new FeatureAnalyzer object.
     *
     * @param feat The feature to compare
     * @param qs   The sequence holding the feature
     * @param ss   The sequence to compare against
     */
    public FeatureAnalyzer(
            StrandedFeature feat,
            EditableSequence qs,
            EditableSequence ss) {
        if ((qs != null) && (ss != null)) {
            m_diffs =
                    ca.virology.lib.io.tools.SequenceTools.getDifferences(
                            qs.toString(),
                            ss.toString());
        } else {
            m_diffs = new int[0];
        }

        m_qs = qs;
        m_ss = ss;
        m_feat = feat;

        initHeaders();
        initData();
    }

    /**
     * Creates a new FeatureAnalyzer object.
     *
     * @param feat      The feature to compare
     * @param qs        The sequence holding the feature
     * @param ss        The sequence to compare against
     * @param diffArray The difference array for the two sequences
     */
    public FeatureAnalyzer(
            StrandedFeature feat,
            EditableSequence qs,
            EditableSequence ss,
            int[] diffArray) {
        m_diffs = diffArray;
        m_qs = qs;
        m_ss = ss;
        m_feat = feat;

        initHeaders();
        initData();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the headers
     *
     * @return A list of strings indicating the headers for all comparisons
     */
    public List getHeaders() {
        return (ArrayList) m_headers.clone();
    }

    /**
     * Get the data
     *
     * @return A list of objects representing the data of the comparison
     */
    public List getData() {
        return (ArrayList) m_data.clone();
    }

    /**
     * Init the headers
     */
    protected void initHeaders() {
        m_headers = new ArrayList();
        m_headers.add("Gene Name");
        m_headers.add("Strand");
        m_headers.add("ORF Start");
        m_headers.add("ORF Stop");
        m_headers.add("Length");
        m_headers.add("Aligned Length");

        if (m_ss == null) {
            m_headers.add("No Comparison Sequence");
        } else {
            m_headers.add("Differences");
            m_headers.add("Difference %");
            m_headers.add("Subs");
            m_headers.add("Inserts");
            m_headers.add("Deletes");
            m_headers.add(UPSTREAM + "b Upstream Diffs");
            m_headers.add("AA Changes");
            m_headers.add("Silent Changes");
            //m_headers.add("+stops");
        }
    }

    /**
     * Init the data
     */
    protected void initData() {
        m_data = new ArrayList();

        Location l = m_feat.getLocation();

        int start = m_qs.getRelativePosition(l.getMin());
        int stop = m_qs.getRelativePosition(l.getMax());

        int gapLength = stop - start + 1;
        int absLength = l.getMax() - l.getMin() + 1;

        m_data.add(m_feat.getAnnotation().getProperty(AnnotationKeys.NAME));
        m_data.add(m_feat.getStrand().equals(StrandedFeature.POSITIVE) ? "Top"
                : "Bottom");
        m_data.add(new Integer(l.getMin()));
        m_data.add(new Integer(l.getMax()));
        m_data.add(new Integer(absLength));
        m_data.add(new Integer(gapLength));

        if (m_ss == null) {
            return;
        }

        int diffCount = 0;
        int subs = 0;
        int ins = 0;
        int del = 0;

        for (int j = start; j <= stop; ++j) {
            if (m_diffs[j] != ca.virology.lib.io.sequenceData.DifferenceType.I_NONE) {
                ++diffCount;
            }

            if (m_diffs[j] == ca.virology.lib.io.sequenceData.DifferenceType.I_INSERTION) {
                ++ins;
            }

            if (m_diffs[j] == ca.virology.lib.io.sequenceData.DifferenceType.I_DELETION) {
                ++del;
            }

            if (DifferenceType.isSubstitution(m_diffs[j])) {
                ++subs;
            }
        }

        int[] iblock = new int[m_diffs.length];
        int maxi = 0;
        int[] dblock = new int[m_diffs.length];
        int maxd = 0;
        int[] sblock = new int[m_diffs.length];
        int maxs = 0;

        for (int j = start; j <= stop; ++j) {
            if (m_diffs[j] == ca.virology.lib.io.sequenceData.DifferenceType.I_INSERTION) {
                for (int k = j + 1; k <= stop; ++k) {
                    if (m_diffs[k] != ca.virology.lib.io.sequenceData.DifferenceType.I_INSERTION) {
                        iblock[k - j]++;

                        if ((k - j) > maxi) {
                            maxi = k - j;
                        }

                        j = k;

                        break;
                    }
                }
            }

            if (m_diffs[j] == ca.virology.lib.io.sequenceData.DifferenceType.I_DELETION) {
                for (int k = j + 1; k <= stop; ++k) {
                    if (m_diffs[k] != ca.virology.lib.io.sequenceData.DifferenceType.I_DELETION) {
                        if ((k - j) > maxd) {
                            maxd = k - j;
                        }

                        dblock[k - j]++;
                        j = k;

                        break;
                    }
                }
            }

            if (DifferenceType.isSubstitution(m_diffs[j])) {
                for (int k = j + 1; k <= stop; ++k) {
                    if (!(DifferenceType.isSubstitution(m_diffs[k]))) {
                        if ((k - j) > maxs) {
                            maxs = k - j;
                        }

                        sblock[k - j]++;
                        j = k;

                        break;
                    }
                }
            }
        }

        StringBuffer subStr = new StringBuffer(subs + "");
        StringBuffer delStr = new StringBuffer(del + "");
        StringBuffer insStr = new StringBuffer(ins + "");

        for (int j = 0; j <= maxs; ++j) {
            if (sblock[j] > 0) {
                subStr.append(", " + j + " (" + sblock[j] + ")");
            }
        }

        for (int j = 0; j <= maxi; ++j) {
            if (iblock[j] > 0) {
                insStr.append(", " + j + " (" + iblock[j] + ")");
            }
        }

        for (int j = 0; j < maxd; ++j) {
            if (dblock[j] > 0) {
                delStr.append(", " + j + " (" + dblock[j] + ")");
            }
        }

        int upDiffs = 0;

        if (m_feat.getStrand()
                .equals(StrandedFeature.POSITIVE)) {
            for (int j = start - UPSTREAM; j <= start; ++j) {
                if (j < 0) {
                    continue;
                }

                if (m_diffs[j] != ca.virology.lib.io.sequenceData.DifferenceType.I_NONE) {
                    ++upDiffs;
                }
            }
        } else {
            for (int j = stop + 1; j < (stop + 201); ++j) {
                if (j >= m_diffs.length) {
                    break;
                }

                if (m_diffs[j] != ca.virology.lib.io.sequenceData.DifferenceType.I_NONE) {
                    ++upDiffs;
                }
            }
        }

        java.text.NumberFormat format = java.text.NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);

        m_data.add(new Integer(diffCount));
        m_data.add(format.format((double) diffCount / (double) gapLength * 100) +
                "%");
        m_data.add(subStr.toString());
        m_data.add(insStr.toString());
        m_data.add(delStr.toString());
        m_data.add(new Integer(upDiffs)); //upstream
        m_data.addAll(getCodingData(m_feat));
    }

    /**
     * get the protein coding data
     *
     * @param f the feature to get data for
     * @return a list of data items
     */
    protected List getCodingData(StrandedFeature f) {
        Location l = f.getLocation();
        StrandedFeature.Strand strand = f.getStrand();

        int start = m_qs.getRelativePosition(l.getMin());
        int stop = m_qs.getRelativePosition(l.getMax());

        String seq1 = "";
        String seq2 = "";

        ArrayList ret = new ArrayList();

        try {
            seq1 = m_qs.substring(start, stop + 1);
            seq2 = m_ss.substring(start, stop + 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            ret.add(new Integer(0));
        }

        if (strand == StrandedFeature.NEGATIVE) {
            StringBuffer b = new StringBuffer();

            for (int i = seq1.length() - 1; i >= 0; --i) {
                b.append(
                        ca.virology.lib.io.tools.SequenceTools.getDNAComplement(
                                seq1.charAt(i)));
            }

            seq1 = b.toString();
            b = new StringBuffer();

            for (int i = seq2.length() - 1; i >= 0; --i) {
                b.append(
                        ca.virology.lib.io.tools.SequenceTools.getDNAComplement(
                                seq2.charAt(i)));
            }

            seq2 = b.toString();
        }

        CodingComparison codeComp = new CodingComparison(seq1, seq2);

        ret.add(new Integer(codeComp.changes.size()));
        ret.add(new Integer(codeComp.silent.size()));
        //ret.add(new Integer(codeComp.stops1.size()));

        return ret;
    }
}
