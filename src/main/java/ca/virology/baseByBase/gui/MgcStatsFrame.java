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

import ca.virology.lib.io.MultiFileFilter;
import ca.virology.lib.io.sequenceData.*;
import ca.virology.lib.util.gui.BookmarkMenu;

import ca.virology.baseByBase.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.io.*; //remove this when not needed

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;


/**
 * This frame displays a table of information which represents a statistical
 * breakdown of each gene in the displayed sequence, including start/stop
 * positions and information on differences for each gene.
 *
 * @author Ryan Brodie
 */
public class MgcStatsFrame extends JFrame {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static final int UPSTREAM = 200;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected java.text.NumberFormat m_format;
    protected FeaturedSequence m_seq;
    protected FeaturedSequence[] m_seqs;
    protected int m_start;
    protected int m_stop;
    protected int m_length;
    protected ReportTablePanel m_tPane;
    protected Map m_data;
    private String[] ColumnNames;
    private Vector Comparators;
    private boolean cancelled = false;


    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Construct a new frame to display the gvien sequences
     *
     * @param seq      the query sequence
     * @param standard the sequence to compare to
     * @param start    the start of the area to compute
     * @param stop     the end of the area to compute
     */
    public MgcStatsFrame(FeaturedSequence[] seq, int start, int stop) {

        m_seq = seq[0];
        m_seqs = seq;

        m_start = start;
        m_stop = stop;

        if (m_start == -1) {
            m_start = 0;
        }

        if (m_stop == -1) {
            m_stop = m_seq.length() - 1;
        }

        m_length = m_stop - m_start + 1;

        //initialize column names
        int numCols = 4 + (m_seqs.length - 1);
        ColumnNames = new String[numCols];
        ColumnNames[0] = "Ref Seq Location";
        ColumnNames[1] = "NA Substitution";
        ColumnNames[2] = "Feature Affected";
        ColumnNames[3] = "Ref AA Location";
        int comps = 1;
        for (int i = 4; i < numCols; ++i) {
            ColumnNames[i] = m_seqs[comps].getName().replaceAll("Human herpesvirus", "");
            ++comps;
        }

        //initialize comparators
        Comparators = new Vector();
        Comparators.add(new NumberComparator(false));
        Comparators.add(new GenericComparator());
        Comparators.add(new GeneNameComparator());
        Comparators.add(new GenericComparator());
        for (int i = 4; i < (numCols); ++i) {
            Comparators.add(new GenericComparator());
        }
        m_format = java.text.NumberFormat.getInstance();
        m_format.setMaximumFractionDigits(2);
        m_format.setMinimumFractionDigits(2);

        //calculate();
        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * initialize the user interface for this window
     */
    protected void initUI() {

        setTitle("Multi Genome Comparison Statistics");
        m_tPane = new ReportTablePanel(getData(), getComparators(), getColumnNames(), 1);

        if (!m_tPane.isCancelled()) {

            m_tPane.setRowName("Change");

            setJMenuBar(createMenuBar());
            setContentPane(m_tPane);

            Dimension d = new Dimension(500, 800);
            setSize(d);
        } else {
            this.cancelled = true;
        }

    }

    /**
     * Checks if table display was cancelled
     *
     * @param newName the new name
     */
    protected boolean isCancelled() {
        return cancelled;
    }

    /**
     * create the menubar for this window
     *
     * @return the menubar for this window
     */
    protected JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        mb.add(menu);

        JMenuItem mi = new JMenuItem("Save Selected (HTML)");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                writeHtml(false);
            }
        });
        menu.add(mi);
        mi = new JMenuItem("Save All (HTML)");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                writeHtml(true);
            }
        });
        menu.add(mi);
        mi = new JMenuItem("Save Selected (CSV)");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                writeCSV(false);
            }
        });
        menu.add(mi);
        mi = new JMenuItem("Save All (CSV)");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                writeCSV(true);
            }
        });
        menu.add(mi);
        mi = new JMenuItem("Close");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                MgcStatsFrame.this.dispose();
            }
        });
        menu.addSeparator();
        menu.add(mi);
        menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Show Only Affected Genes");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JCheckBoxMenuItem checkboxMenuItem = (JCheckBoxMenuItem) evt.getSource();
                m_tPane.setShowOnlyAffectedGenes(checkboxMenuItem.getState());
            }
        });
        menu.add(menuItem);
        mb.add(menu);
        return mb;
    }


    /**
     * get the names of the columns to display
     *
     * @return a vector of column names
     */
    protected String[] getColumnNames() {
        return ColumnNames;
    }

    /**
     * get the comparators used to sort this table
     *
     * @return a vector of comparators
     */
    protected Vector getComparators() {
        return Comparators;
    }


    /**
     * get the data to dispay in this window
     *
     * @return a vector of vectors, each representing a row in the table, if cancelled returns null.
     */
    protected Vector getData() {
        Vector ret = new Vector();
        int count = 0;
        ProgressMonitor pm = null;

        pm = new ProgressMonitor(getContentPane(), "Comparing Sequences", "", m_start, m_stop);


        for (int i = m_start; i < m_stop + 1; ++i) {
            pm.setProgress(count++);
            if (nucleotideChange(i, 'A', 'C').size() > 0)
                ret.add(nucleotideChange(i, 'A', 'C'));
            if (nucleotideChange(i, 'A', 'G').size() > 0)
                ret.add(nucleotideChange(i, 'A', 'G'));
            if (nucleotideChange(i, 'A', 'T').size() > 0)
                ret.add(nucleotideChange(i, 'A', 'T'));
            if (nucleotideChange(i, 'C', 'A').size() > 0)
                ret.add(nucleotideChange(i, 'C', 'A'));
            if (nucleotideChange(i, 'C', 'G').size() > 0)
                ret.add(nucleotideChange(i, 'C', 'G'));
            if (nucleotideChange(i, 'C', 'T').size() > 0)
                ret.add(nucleotideChange(i, 'C', 'T'));
            if (nucleotideChange(i, 'G', 'A').size() > 0)
                ret.add(nucleotideChange(i, 'G', 'A'));
            if (nucleotideChange(i, 'G', 'C').size() > 0)
                ret.add(nucleotideChange(i, 'G', 'C'));
            if (nucleotideChange(i, 'G', 'T').size() > 0)
                ret.add(nucleotideChange(i, 'G', 'T'));
            if (nucleotideChange(i, 'T', 'A').size() > 0)
                ret.add(nucleotideChange(i, 'T', 'A'));
            if (nucleotideChange(i, 'T', 'C').size() > 0)
                ret.add(nucleotideChange(i, 'T', 'C'));
            if (nucleotideChange(i, 'T', 'G').size() > 0)
                ret.add(nucleotideChange(i, 'T', 'G'));
            if (nucleotideChange(i, 'C', 'Y').size() > 0)
                ret.add(nucleotideChange(i, 'C', 'Y'));
            if (nucleotideChange(i, 'T', 'Y').size() > 0)
                ret.add(nucleotideChange(i, 'T', 'Y'));
            if (nucleotideChange(i, 'A', 'R').size() > 0)
                ret.add(nucleotideChange(i, 'A', 'R'));
            if (nucleotideChange(i, 'G', 'R').size() > 0)
                ret.add(nucleotideChange(i, 'G', 'R'));
            if (pm.isCanceled()) {
                pm.close();
                return null;
            }
        }
        pm.close();
        return ret;
    }

    /**
     * Creates a row in the table to show a nucleotide change from the master sequence
     *
     * @param position
     * @param original nucleotide
     * @param change   nucleotide
     * @return Vector
     */
    private Vector nucleotideChange(int i, char master, char change) {
        Vector row = new Vector();
        Vector empty = new Vector();
        Vector hits = new Vector();
        int found = 0;
        if (m_seq.charAt(i) == master) {
            for (int j = 1; j < m_seqs.length; ++j) {
                if (m_seqs[j].charAt(i) == '-') {
                    return empty;
                }
                if (m_seqs[j].charAt(i) == change) {
                    found = 1;
                    hits.add("X");
                } else
                    hits.add("");
            }
            if (found == 1) {

                row.add(m_seq.getAbsolutePosition(i) + ""); //absolutePosition
                //row.add(i+1+""); //index +1 for display because java indexes from 0
                if (change == 'Y')
                    row.add("" + master + "/" + change + "*");
                else if (change == 'R')
                    row.add("" + master + "/" + change + "*");
                else
                    row.add("" + master + "/" + change);
                Vector geneAff = genesAffected(i, hits);
                for (int k = 0; k < geneAff.size(); ++k)
                    row.add(geneAff.elementAt(k));
                return row;
            }
        }
        return empty;

    }

    /**
     * Finds if a position in a sequence affects a gene
     *
     * @param position  of nucleotide in the sequence
     * @param sequences to check
     * @return the genes the nucleotide is part of
     */
    private Vector genesAffected(int i, Vector hits) {
        Vector currentFeatures = new Vector();
        Vector affected = new Vector();
        String featureAffected = new String("");
        String positionAffected = new String("");
        FeatureFilter gf = new FeatureFilter.ByType(FeatureType.GENE);
        FeatureHolder gh = m_seq.filter(gf, false);
        Iterator genes = gh.features();
        while (genes.hasNext()) {
            StrandedFeature f = (StrandedFeature) genes.next();
            Location l = f.getLocation();
            String name = (String) f.getAnnotation().getProperty(AnnotationKeys.NAME);
            int start = m_seq.getRelativePosition(l.getMin());
            int stop = m_seq.getRelativePosition(l.getMax());
            if ((i >= start) && (i <= stop)) {
                currentFeatures.add(f);
                if (featureAffected.equals(""))
                    featureAffected = "" + name;
                else
                    featureAffected = featureAffected.concat(" ," + name + "");
                //handle AA ref
                if (f.getStrand().equals(StrandedFeature.NEGATIVE)) {
                    StringBuffer codon = new StringBuffer();
                    int num = 0;
                    int set = 0;
                    int found = 0;
                    for (int n = stop; n > (start - 1); --n) {
                        if (m_seq.charAt(n) != '-') {

                            if (n == i) {
                                found = 1;
                            }
                            codon.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(m_seq.charAt(n)));
                            ++set;
                        }
                        if (set == 3) {

                            ++num;
                            String aa1 = ca.virology.lib.io.tools.SequenceTools.getAminoAcid(codon.toString());

                            if (found == 1) {
                                if (positionAffected.equals(""))
                                    positionAffected = "bottom: " + aa1 + "" + num;
                                else
                                    positionAffected = positionAffected.concat(", bottom: " + aa1 + "" + num);
                            }
                            //System.out.println(AAAffected);
                            set = 0;
                            codon = new StringBuffer();
                            found = 0;
                        }

                    }

                } else if (f.getStrand().equals(StrandedFeature.POSITIVE)) {
                    StringBuffer codon = new StringBuffer();
                    int num = 0;
                    int set = 0;
                    int found = 0;
                    for (int n = start; n < (stop + 1); n++) {
                        if (m_seq.charAt(n) != '-') {

                            if (n == i) {
                                found = 1;
                            }
                            codon.append(m_seq.charAt(n));
                            ++set;
                        }
                        if (set == 3) {

                            ++num;
                            String aa1 = ca.virology.lib.io.tools.SequenceTools.getAminoAcid(codon.toString());

                            if (found == 1) {
                                if (positionAffected.equals(""))
                                    positionAffected = "top: " + aa1 + "" + num;
                                else
                                    positionAffected = positionAffected.concat(", bottom: " + aa1 + "" + num);
                            }
                            //System.out.println(AAAffected);
                            set = 0;
                            codon = new StringBuffer();
                            found = 0;
                        }

                    }

                }
                //handle AA ref
            }
        }
        if (featureAffected.equals("")) {
            affected.add("NA");
            affected.add("NA");
            for (int j = 0; j < hits.size(); ++j)
                affected.add(hits.elementAt(j));
            return affected;
        } else {
            affected.add(featureAffected);
            affected.add(positionAffected);
            for (int m = 1; m < m_seqs.length; ++m) {
                String AAAffected = new String("");
                for (int k = 0; k < currentFeatures.size(); ++k) {
                    FeaturedSequence compSeq = m_seqs[m];
                    StrandedFeature fet = (StrandedFeature) currentFeatures.elementAt(k);
                    Location loc = fet.getLocation();
                    int start1 = m_seq.getRelativePosition(loc.getMin());
                    int stop1 = m_seq.getRelativePosition(loc.getMax());
                    if (fet.getStrand().equals(StrandedFeature.NEGATIVE)) {
                        StringBuffer codon = new StringBuffer();
                        StringBuffer comp = new StringBuffer();
                        int num = 0;
                        int set = 0;
                        int found = 0;
                        for (int n = stop1; n > (start1 - 1); --n) {
                            if (m_seq.charAt(n) != '-') {

                                if (n == i) {
                                    found = 1;
                                }
                                codon.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(m_seq.charAt(n)));
                                comp.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(compSeq.charAt(n)));
                                ++set;
                            }
                            if (set == 3) {

                                ++num;

                                String aa1 = ca.virology.lib.io.tools.SequenceTools.getAminoAcid(codon.toString());
                                String aa2 = ca.virology.lib.io.tools.SequenceTools.getAminoAcid(comp.toString());
                                if (found == 1) {
                                    if (!aa1.equals(aa2))
                                        if (AAAffected.equals("")) {
                                            if (comp.indexOf("Y") != -1)
                                                AAAffected = aa1 + "-?/" + codon.toString() + "-" + comp.toString() + "";
                                            else if (comp.indexOf("R") != -1)
                                                AAAffected = aa1 + "-?/" + codon.toString() + "-" + comp.toString() + "";
                                            else
                                                AAAffected = aa1 + "-" + aa2 + "/" + codon.toString() + "-" + comp.toString() + "";
                                        } else {
                                            if (comp.indexOf("Y") != -1)
                                                AAAffected = AAAffected.concat(", " + aa1 + "-?/" + codon.toString() + "-" + comp.toString() + "");
                                            else if (comp.indexOf("R") != -1)
                                                AAAffected = AAAffected.concat(", " + aa1 + "-?/" + codon.toString() + "-" + comp.toString() + "");
                                            else
                                                AAAffected = AAAffected.concat(", " + aa1 + "-" + aa2 + "/" + codon.toString() + "-" + comp.toString() + "");
                                        }
                                    else if (AAAffected.equals(""))
                                        if (!hits.elementAt(m - 1).toString().equals("X"))
                                            AAAffected = "";
                                        else
                                            AAAffected = AAAffected.concat("X");
                                    else if (!hits.elementAt(m - 1).toString().equals("X"))
                                        AAAffected = "";
                                    else
                                        AAAffected = AAAffected.concat(", X");
                                }
                                //System.out.println(AAAffected);
                                set = 0;
                                comp = new StringBuffer();
                                codon = new StringBuffer();
                                found = 0;
                            }

                        }

                    } else if (fet.getStrand().equals(StrandedFeature.POSITIVE)) {
                        StringBuffer codon = new StringBuffer();
                        StringBuffer comp = new StringBuffer();
                        int num = 0;
                        int set = 0;
                        int found = 0;
                        for (int n = start1; n < (stop1 + 1); ++n) {
                            if (m_seq.charAt(n) != '-') {

                                if (n == i) {
                                    found = 1;
                                }
                                codon.append(m_seq.charAt(n));
                                comp.append(compSeq.charAt(n));
                                //System.out.println(codon.toString()+" "+comp.toString());
                                ++set;
                            }
                            if (set == 3) {

                                ++num;

                                String aa1 = ca.virology.lib.io.tools.SequenceTools.getAminoAcid(codon.toString());
                                String aa2 = ca.virology.lib.io.tools.SequenceTools.getAminoAcid(comp.toString());

                                if (found == 1) {
                                    if (!aa1.equals(aa2))
                                        if (AAAffected.equals("")) {
                                            if (comp.indexOf("Y") != -1)
                                                AAAffected = aa1 + "-?/" + codon.toString() + "-" + comp.toString() + "";
                                            else if (comp.indexOf("R") != -1)
                                                AAAffected = aa1 + "-?/" + codon.toString() + "-" + comp.toString() + "";
                                            else
                                                AAAffected = aa1 + "-" + aa2 + "/" + codon.toString() + "-" + comp.toString() + "";
                                        } else {
                                            if (comp.indexOf("Y") != -1)
                                                AAAffected = AAAffected.concat(", " + aa1 + "-?/" + codon.toString() + "-" + comp.toString() + "");
                                            else if (comp.indexOf("R") != -1)
                                                AAAffected = AAAffected.concat(", " + aa1 + "-?/" + codon.toString() + "-" + comp.toString() + "");
                                            else
                                                AAAffected = AAAffected.concat(", " + aa1 + "-" + aa2 + "/" + codon.toString() + "-" + comp.toString() + "");
                                        }
                                    else if (AAAffected.equals(""))
                                        if (!hits.elementAt(m - 1).toString().equals("X"))
                                            AAAffected = "";
                                        else
                                            AAAffected = AAAffected.concat("X");
                                    else if (!hits.elementAt(m - 1).toString().equals("X"))
                                        AAAffected = "";
                                    else
                                        AAAffected = AAAffected.concat(", X");
                                }
                                //System.out.println(AAAffected);
                                set = 0;
                                comp = new StringBuffer();
                                codon = new StringBuffer();
                                found = 0;
                            }

                        }

                    }


                }
                //System.out.println("->"+AAAffected);
                affected.add(AAAffected);

            }

            return affected;
        }
    }


    /**
     * write the current table (or a selected portion) to HTML
     *
     * @param writeAll if true this will write all the table to file, otherwise
     *                 it will write the selected portion only.
     */
    protected void writeHtml(boolean writeAll) {
        MultiFileFilter ff = new MultiFileFilter("HTML Files");
        ff.addExtension("html");
        ff.addExtension("htm");

        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(ff);
        fc.setDialogTitle("Save HTML File");

        if (fc.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();

                FileWriter out = new FileWriter(f);

                String head = "<H1>MGC Statistics</H1>" + "<H2>Master Sequence:" +
                        m_seq.getName() + "</H2>";
                String tail = "<BR><HR>Base-by-Base Report Generation";

                out.write(m_tPane.getHtml(head, tail, writeAll));
                out.flush();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * write the current table (or a selected portion) to a CSV file
     *
     * @param writeAll if true this will write all the table to file, otherwise
     *                 it will write the selected portion only.
     */
    protected void writeCSV(boolean writeAll) {
        MultiFileFilter ff = new MultiFileFilter("CSV Files");
        ff.addExtension("csv");
        ff.addExtension("txt");

        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(ff);
        fc.setDialogTitle("Save CSV File");

        if (fc.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();

                FileWriter out = new FileWriter(f);

                out.write(m_tPane.getCSV(writeAll));
                out.flush();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


}

	
	
