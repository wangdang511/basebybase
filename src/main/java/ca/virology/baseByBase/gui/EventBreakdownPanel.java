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
package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.*;

import ca.virology.baseByBase.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

import java.awt.BorderLayout;

import java.text.*;

import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;


/**
 * This panel displays a tree view of the data represented by the comparison of
 * two sequences
 *
 * @author Ryan Brodie
 */
public class EventBreakdownPanel
        extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected java.text.NumberFormat m_format;
    protected FeaturedSequence m_qs;
    protected FeaturedSequence m_ss;
    protected int m_start;
    protected int m_stop;
    protected JTree m_tree;
    protected DefaultTreeModel m_model;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Constructs a new panel
     *
     * @param query    the query sequence
     * @param standard the standard sequence (for comparison)
     * @param start    the first position in the analysis region
     * @param stop     the last position in the analysis region
     */
    public EventBreakdownPanel(
            FeaturedSequence query,
            FeaturedSequence standard,
            int start,
            int stop) {
        super();

        m_start = start;
        m_stop = stop;

        if (m_start == -1) {
            m_start = 0;
        }

        if (m_stop == -1) {
            m_stop = query.length() - 1;
        }

        m_format = NumberFormat.getInstance();
        m_format.setMaximumFractionDigits(2);
        m_format.setMinimumFractionDigits(2);

        m_qs = query;
        m_ss = standard;

        m_model = new DefaultTreeModel(new DefaultMutableTreeNode("Empty"));
        m_tree = new JTree(m_model);

        try {
            calculate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * calculate the breakdown
     *
     * @throws Exception
     */
    protected void calculate()
            throws Exception {
        int startPos = m_qs.getAbsolutePosition(m_start);
        int stopPos = m_qs.getAbsolutePosition(m_stop);

        FeatureFilter lf =
                new FeatureFilter.OverlapsLocation(new RangeLocation(startPos,
                        stopPos));

        FeatureFilter f3 =
                new FeatureFilter.And(new FeatureFilter.ByType(FeatureType.GENE), lf);

        int[] diffs =
                ca.virology.lib.io.tools.SequenceTools.getDifferences(
                        m_qs.toString(),
                        m_ss.toString());

        if (diffs == null) {
            error("");
        }

        FeatureHolder genes = m_qs.filter(f3, false);

        DefaultMutableTreeNode root =
                new DefaultMutableTreeNode(m_qs.getName() + " (" + m_qs.length() +
                        " bp)");
        DefaultMutableTreeNode diffNode = new DefaultMutableTreeNode("");
        DefaultMutableTreeNode cdsNode = new DefaultMutableTreeNode("");
        DefaultMutableTreeNode ncdsNode = new DefaultMutableTreeNode("");
        DefaultMutableTreeNode geneBDNode =
                new DefaultMutableTreeNode("Gene Breakdown");
        DefaultMutableTreeNode userNode = new DefaultMutableTreeNode("");

        ///////////////////////////////////////////////////////
        // crunch the numbers and build the tree  /////////////
        ///////////////////////////////////////////////////////
        // totals
        int totalDifs = 0, total = 0, diff = 0;

        String query = m_qs.toString();
        String standard = m_ss.toString();

        int minLength = Math.min(query.length(), standard.length());
        if (m_stop == -1 || m_stop >= minLength) {
            m_stop = minLength - 1;
        }

        for (int k = m_start; k < m_stop; ++k) {
            if (k >= m_stop) {
                continue;
            }
            char c1 = query.charAt(k);
            char c2 = standard.charAt(k);
            if ((c1 != '-') && (c2 != '-')) {
                ++total;
                if (c1 != c2) {
                    ++diff;
                }
            }
        }

        totalDifs = diff;

        double difpct = ((double) diff / (double) total) * 100.0;
        diffNode.setUserObject(diff + " Differnece Events (" + m_format.format(difpct) + "%)");
        getGeneData(genes, geneBDNode);
        Iterator gi = genes.features();
        int cdsDifs = 0;
        while (gi.hasNext()) {
            StrandedFeature f = (StrandedFeature) gi.next();
            Location l = f.getLocation();
            if (LocationTools.overlaps(
                    l,
                    new RangeLocation(m_start, m_stop))) {
                l = LocationTools.intersection(
                        l,
                        new RangeLocation(m_start, m_stop));
                int start =
                        Math.max(
                                m_qs.getRelativePosition(l.getMin()),
                                m_start);
                int stop =
                        Math.min(
                                m_qs.getRelativePosition(l.getMax()),
                                m_stop);
                for (int i = start; i < stop; ++i) {
                    if (diffs[i] != DifferenceType.I_NONE) {
                        ++cdsDifs;
                    }
                }
            }
        }
        double cdspct = (double) cdsDifs / (double) totalDifs * 100.0;
        cdsNode.setUserObject(cdsDifs + " in CDS (" + m_format.format(cdspct) + "%)");
        ncdsNode.setUserObject((totalDifs - cdsDifs) + " in non-coding regions");
        if (addUserEventData(userNode)) {
            userNode.setUserObject("User Event Breakdown");
        } else {
            userNode.setUserObject("No User Events Found");
        }

        ///////////////////////////////////////////////////////
        root.add(diffNode);
        cdsNode.add(geneBDNode);
        diffNode.add(cdsNode);
        diffNode.add(ncdsNode);
        root.add(userNode);
        m_model.setRoot(root);
    }

    /**
     * initialize the ui
     */
    protected void initUI() {
        setLayout(new BorderLayout());
        add(
                new JScrollPane(m_tree),
                BorderLayout.CENTER);
    }

    /**
     * throw an exception with a message
     *
     * @param msg the error
     * @throws Exception
     */
    protected void error(String msg)
            throws Exception {
        throw new Exception(msg);
    }

    /**
     * add data to a node that represents the brekadown of user events in this
     * region
     *
     * @param parent the node to add to
     * @return true if it works out
     */
    protected boolean addUserEventData(DefaultMutableTreeNode parent) {
        int startPos = m_qs.getAbsolutePosition(m_start);
        int stopPos = m_qs.getAbsolutePosition(m_stop);
        FeatureFilter lf1 =
                new FeatureFilter.And(new FeatureFilter.OverlapsLocation(
                        new RangeLocation(startPos, stopPos)),
                        new FeatureFilter.BySource(FeatureType.USER_GENERATED));

        startPos = m_start;
        stopPos = m_stop;

        if (startPos >= m_ss.length()) {
            startPos = m_ss.length() - 2;
        }

        if (stopPos >= m_ss.length()) {
            stopPos = m_ss.length() - 1;
        }

        startPos = m_ss.getAbsolutePosition(startPos);
        stopPos = m_ss.getAbsolutePosition(stopPos);

        FeatureFilter lf2 =
                new FeatureFilter.And(new FeatureFilter.OverlapsLocation(
                        new RangeLocation(startPos, stopPos)),
                        new FeatureFilter.BySource(FeatureType.USER_GENERATED));

        boolean ret = false;
        FeatureFilter ff = new FeatureFilter.BySource(FeatureType.USER_GENERATED);
        FeatureHolder qsEvents = m_qs.filter(lf1, false);
        FeatureHolder ssEvents = m_ss.filter(lf2, false);

        if (qsEvents.countFeatures() != 0) {
            DefaultMutableTreeNode qsNode =
                    new DefaultMutableTreeNode(m_qs.getName());

            for (Iterator i = qsEvents.features(); i.hasNext(); ) {
                StrandedFeature f = (StrandedFeature) i.next();
                String type =
                        (String) f.getAnnotation()
                                .getProperty(AnnotationKeys.EVENT_TYPE);

                if (type.equals(FeatureType.COMMENT)) {
                    String cmt =
                            new StringTokenizer(
                                    f.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT).toString(),
                                    "\n").nextToken();
                    Location l = f.getLocation();
                    DefaultMutableTreeNode cnode =
                            new DefaultMutableTreeNode("Comment: " + cmt + " (" +
                                    l.getMin() + "->" + l.getMax() + ")");
                    addBreakdownNodes(m_qs, l, cnode);
                    qsNode.add(cnode);
                }
            }

            parent.add(qsNode);
            ret = true;
        }

        if (ssEvents.countFeatures() != 0) {
            DefaultMutableTreeNode ssNode =
                    new DefaultMutableTreeNode(m_ss.getName());

            for (Iterator i = ssEvents.features(); i.hasNext(); ) {
                StrandedFeature f = (StrandedFeature) i.next();
                String type =
                        (String) f.getAnnotation()
                                .getProperty(AnnotationKeys.EVENT_TYPE);

                if (type.equals(FeatureType.COMMENT)) {
                    String cmt =
                            new StringTokenizer(
                                    f.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT).toString(),
                                    "\n").nextToken();
                    Location l = f.getLocation();
                    DefaultMutableTreeNode cnode =
                            new DefaultMutableTreeNode("Comment: " + cmt + " (" +
                                    l.getMin() + "->" + l.getMax() + ")");
                    addBreakdownNodes(m_ss, l, cnode);
                    ssNode.add(cnode);
                }
            }

            parent.add(ssNode);
            ret = true;
        }

        return ret;
    }

    /**
     * add a brekadown of substitutions, etc for a particular location on a
     * given sequence
     *
     * @param seq    the sequence to get a breakdown for
     * @param l      the location to get the breakdown for
     * @param parent the node to add the breakdown to
     * @return true if it works out
     */
    protected boolean addBreakdownNodes(
            FeaturedSequence seq,
            Location l,
            DefaultMutableTreeNode parent) {
        int startPos = seq.getAbsolutePosition(m_start);
        int stopPos = seq.getAbsolutePosition(m_stop);

        if (!LocationTools.overlaps(
                l,
                new RangeLocation(startPos, stopPos))) {
            DefaultMutableTreeNode regnode =
                    new DefaultMutableTreeNode(
                            "Exists outside viewing area (events not counted)");
            parent.add(regnode);

            return false;
        }

        l = LocationTools.intersection(
                l,
                new RangeLocation(startPos, stopPos));

        if (!l.isContiguous()) {
            DefaultMutableTreeNode regnode =
                    new DefaultMutableTreeNode("Region Breakdown");
            boolean added = false;

            for (Iterator i = l.blockIterator(); i.hasNext(); ) {
                Location loc = (Location) i.next();
                DefaultMutableTreeNode blocknode =
                        new DefaultMutableTreeNode("Block " + l.getMin() + "->" +
                                l.getMax() + " breakdown");

                if (addBreakdownData(
                        loc.getMin(),
                        loc.getMax(),
                        blocknode)) {
                    regnode.add(blocknode);
                    added = true;
                }
            }

            if (added) {
                parent.add(regnode);
            } else {
                return false;
            }
        } else {
            return addBreakdownData(
                    l.getMin(),
                    l.getMax(),
                    parent);
        }

        return true;
    }

    /**
     * add protein coding data to a node
     *
     * @param min    the first position on the sequence to analyze
     * @param max    the last position on the sequence to analyze
     * @param parent the node to add the data to
     */
    protected void addCodingData(
            int min,
            int max,
            DefaultMutableTreeNode parent) {
        min = m_qs.getRelativePosition(min);
        max = m_qs.getRelativePosition(max);

        max = Math.min(
                max,
                Math.min(
                        m_qs.length(),
                        m_ss.length()));

        String s1 = m_qs.substring(min, max);
        String s2 = m_ss.substring(min, max);

        CodingComparison codeComp = new CodingComparison(s1, s2);
    }

    /**
     * add breakdown data to a node
     *
     * @param min    the first pos in the seq to analyze
     * @param max    the last pos in the seq to analyze
     * @param parent the node to add the data to
     * @return true if it works out
     */
    protected boolean addBreakdownData(
            int min,
            int max,
            DefaultMutableTreeNode parent) {
        min = m_qs.getRelativePosition(min);
        max = m_qs.getRelativePosition(max);

        FeatureFilter ff = new FeatureFilter.ByType(FeatureType.DIFFERENCE_LIST);
        Feature tmpf = (Feature) m_qs.filter(ff, false).features().next();
        int[] diffs = (int[]) tmpf.getAnnotation().getProperty(AnnotationKeys.DIFF_ARRAY);

        int tc = 0;
        int ag = 0;
        int at = 0;
        int gc = 0;
        int othersub = 0;
        int dels = 0;
        int ins = 0;
        int subs = 0;

        for (int i = min; i <= max; ++i) {
            if (diffs[i] == DifferenceType.I_INSERTION) {
                ++ins;
            } else if (diffs[i] == DifferenceType.I_DELETION) {
                ++dels;

            } else if (DifferenceType.isSubstitution(diffs[i])) {
                ++subs;

                char c1 = m_qs.charAt(i);
                char c2 = m_ss.charAt(i);

                if (((c1 == 'T') && (c2 == 'C')) ||
                        ((c1 == 'C') && (c2 == 'T'))) {
                    ++tc;
                } else if (((c1 == 'A') && (c2 == 'G')) ||
                        ((c1 == 'G') && (c2 == 'A'))) {
                    ++ag;
                } else if (((c1 == 'A') && (c2 == 'T')) ||
                        ((c1 == 'T') && (c2 == 'A'))) {
                    ++at;
                } else if (((c1 == 'C') && (c2 == 'G')) ||
                        ((c1 == 'G') && (c2 == 'C'))) {
                    ++gc;
                } else {
                    ++othersub;
                }
            }

        }

        if ((subs == 0) && (dels == 0) && (ins == 0)) {
            parent.add(new DefaultMutableTreeNode("No Difference Events"));

            return false;
        } else {
            if (dels > 0) {
                parent.add(new DefaultMutableTreeNode(dels + " Deletions"));
            }

            if (ins > 0) {
                parent.add(new DefaultMutableTreeNode(ins + " Insertions"));
            }

            parent.add(new DefaultMutableTreeNode(tc + " T <-> C Subs"));
            parent.add(new DefaultMutableTreeNode(ag + " A <-> G Subs"));
            parent.add(new DefaultMutableTreeNode(at + " A <-> T Subs"));
            parent.add(new DefaultMutableTreeNode(gc + " G <-> C Subs"));
            parent.add(new DefaultMutableTreeNode(othersub + " other Subs"));
        }

        return true;
    }

    /**
     * add data for genes in the given feature holder to the parent node
     * specified
     *
     * @param genes  a feature holder of gene features
     * @param parent a node to ad data to
     */
    protected void getGeneData(
            FeatureHolder genes,
            DefaultMutableTreeNode parent) {
        for (Iterator i = genes.features(); i.hasNext(); ) {
            StrandedFeature f = (StrandedFeature) i.next();
            String name =
                    f.getAnnotation()
                            .getProperty(AnnotationKeys.NAME)
                            .toString();
            Location l = f.getLocation();
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);

            if (addBreakdownNodes(m_qs, l, node)) {
                parent.add(node);
            }
        }
    }
}
