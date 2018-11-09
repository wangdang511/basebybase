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
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//import org.apache.log4j.*;


/**
 * This frame displays a table of information which represents a statistical
 * breakdown of each gene in the displayed sequence, including start/stop
 * positions and information on differences for each gene.
 *
 * @author Ryan Brodie
 */
public class CdsStatsFrame extends JFrame {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static final int UPSTREAM = 200;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected java.text.NumberFormat m_format;
    protected FeaturedSequence m_seq;
    protected FeaturedSequence m_standard;
    protected int m_start;
    protected int m_stop;
    protected int m_length;
    protected ReportTablePanel m_tPane;
    protected Map m_data;
    protected static String[] ColumnNames = {"Gene Name",
            //        "Product Name",
            "Strand", "ORF Start", "ORF Stop", "Length", "Aligned Length", "Differences", "Difference %", "Subs", "Inserts", "Deletes", UPSTREAM + "b Upstream Diffs", "AA Changes", "AA Total Diff", "Silent Changes", "Counterpart", "Length", "Length Difference"};
    protected ArrayList<TableColumn> tableColumns = new ArrayList<TableColumn>();

    //    static Logger logger = Logger.getLogger(CdsStatsFrame.class);


    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Construct a new frame to display the gvien sequences
     *
     * @param seq      the query sequence
     * @param standard the sequence to compare to
     * @param start    the start of the area to compute
     * @param stop     the end of the area to compute
     */
    public CdsStatsFrame(FeaturedSequence seq, FeaturedSequence standard, int start, int stop) {
        //        org.apache.log4j.BasicConfigurator.configure();
        m_seq = seq;
        m_standard = standard;

        m_start = start;        //start corresponds to the start position (set display area in view menu)
        m_stop = stop;          //stop corresponds to the end position (set display area in view menu)

        if (m_start == -1) {    // a value of -1 corresponds to the natural endpoint of the sequence
            m_start = 0;
        }

        if (m_stop == -1) {
            m_stop = m_seq.length() - 1;
        }

        m_length = m_stop - m_start + 1;

        m_format = java.text.NumberFormat.getInstance();
        m_format.setMaximumFractionDigits(2);
        m_format.setMinimumFractionDigits(2);

        calculate();
        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * initialize the user interface for this window
     */
    protected void initUI() {
        setTitle("CDS Event Statistics");

        m_tPane = new ReportTablePanel(getData(), getComparators(), getColumnNames());
        m_tPane.setRowName("Gene");

        TableColumnModel tcm = m_tPane.getTable().getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(); i++)
            tableColumns.add(tcm.getColumn(i));

        setJMenuBar(createMenuBar());
        setContentPane(m_tPane);


        Dimension d = new Dimension(500, 800);
        setSize(d);
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
                CdsStatsFrame.this.dispose();
            }
        });
        menu.addSeparator();
        menu.add(mi);

        menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Show Only Different Genes");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JCheckBoxMenuItem checkboxMenuItem = (JCheckBoxMenuItem) evt.getSource();
                m_tPane.setShowOnlyDifferentGenes(checkboxMenuItem.getState());
            }
        });

        menu = new JMenu("Show/Hide Columns");
        JCheckBoxMenuItem checkBox;
        String[] columnNames = getColumnNames();
        for (int i = 0; i < columnNames.length; i++) {
            checkBox = new JCheckBoxMenuItem(ColumnNames[i]);
            checkBox.setName("" + i);
            checkBox.setState(true);
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    showColumns(actionEvent);
                }
            });
            menu.add(checkBox);
        }
        mb.add(menu);

        mb.add(new BookmarkMenu("Links", DiffEditorFrame.getBookmarkList()));

        return mb;
    }

    protected void showColumns(ActionEvent actionEvent) {
        JCheckBoxMenuItem source = (JCheckBoxMenuItem) actionEvent.getSource();
        int index = Integer.parseInt(source.getName()); // the column index is name of jcheckboxmenu selected
        JTable jTable = m_tPane.m_table;
        TableColumnModel tableColumnModel = jTable.getColumnModel();
        // remove column if a checked menu option is selected
        if (!source.getState()) {
            TableColumn tableColumn = tableColumns.get(index);
            tableColumnModel.removeColumn(tableColumn);

        }
        // insert hidden column back into the correct position if an unchecked menu option is selected
        else {
            tableColumnModel.addColumn(tableColumns.get(index)); // add column as last column in list
            Enumeration<TableColumn> columns = tableColumnModel.getColumns(); // get enum for current visible columns
            int insertPosition = 0;
            TableColumn column;
            int columnIndex;
            // find the first column c that appears after the column we are reinserting in the master list i
            // and insert column i in front of column c. this maintains the original order of the columns
            while (columns.hasMoreElements()) {
                column = columns.nextElement();
                columnIndex = tableColumns.indexOf(column); // corresponding index position in masterlist
                if (columnIndex > index)
                    break;
                else {
                    if (columns.hasMoreElements())
                        insertPosition++;
                }
            }
            tableColumnModel.moveColumn(tableColumnModel.getColumnCount() - 1, insertPosition);

        }
        ReportTableModel reportTableModel = m_tPane.getReportModel();
        reportTableModel.fireTableDataChanged();
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

                String head = "<H1>CDS Alignment Statistics</H1>" + "<H2>" +
                        m_seq.getName() + " vs. " + m_standard.getName() + "</H2>";
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
        Vector v = new Vector();
        v.add(new GeneNameComparator());    //Gene Name
        //        v.add(new GenericComparator());     //Product Name
        v.add(new GenericComparator());     //Strand
        v.add(new NumberComparator(false)); //ORF Start
        v.add(new NumberComparator(false)); //ORF Stop
        v.add(new NumberComparator(false)); //Length
        v.add(new NumberComparator(false)); //Aligned Length
        v.add(new NumberComparator(false)); //Differences
        v.add(new NumberLeadingStringComparator(false)); //Diff %
        v.add(new NumberLeadingStringComparator(false)); // subs
        v.add(new NumberLeadingStringComparator(false)); //ins
        v.add(new NumberLeadingStringComparator(false)); //dels
        v.add(new NumberComparator(false)); //upstream diffs
        v.add(new NumberComparator(false)); //aa diffs in intersecting region
        v.add(new NumberComparator(false)); //aa diffs including non-intersecting regions
        v.add(new NumberComparator(false)); //silent
        v.add(new GeneNameComparator()); //counterpart name
        v.add(new NumberComparator(false)); //length (cpt)
        v.add(new NumberComparator(true)); //length difference

        return v;
    }

    /**
     * get the data to display in this window, called only after calculate() is executed
     *
     * @return a vector of vectors, each representing a row in the table
     */
    protected Vector getData() {
        Vector ret = new Vector();
        Iterator i = m_data.keySet().iterator();

        while (i.hasNext()) {
            Vector v = new Vector();
            Object key = i.next();
            List l = (List) m_data.get(key);

            if (l == null) {
                continue;
            }

            for (int j = 0; j < l.size(); ++j) {
                v.add(l.get(j));
            }

            ret.add(v);
        }

        return ret;
    }

    /**
     * calculate the data to display
     */
    protected void calculate() {
        System.out.println("Calculating Gene Report for " + m_seq.getName() + " which has " + m_seq.countFeatures() + " Features...");

        FeatureFilter gf = new FeatureFilter.ByType(FeatureType.GENE);

        FeatureHolder gh = m_seq.filter(gf, false);
        Iterator genes = gh.features();
        // getDifferences returns an array indexed by nucleotide position, where diffs[i] has a value corresponding to
        // the type of change in seq relative to the standard (insertion, deletion, substitution or none)
        int[] diffs = ca.virology.lib.io.tools.SequenceTools.getDifferences(m_seq.toString(), m_standard.toString());

        int absstart = m_seq.getAbsolutePosition(m_start); // start position of unaligned data (all "-" removed)
        int absstop = m_seq.getAbsolutePosition(m_stop);   // end position of unaligned data

        m_data = new HashMap();

        int total = gh.countFeatures();
        int count = 0;
        ProgressMonitor pm = null;

        pm = new ProgressMonitor(getContentPane(), "Analyzing Genes", "", 0, total);

        int[] iblock = null;
        int[] dblock = null;
        int[] sblock = null;
        int maxi = 0;
        int maxd = 0;
        int maxs = 0;
        int j = 0;

        // iterate through each gene feature (biojava object)
        while (genes.hasNext()) {
            StrandedFeature f = (StrandedFeature) genes.next();

            Location l = f.getLocation(); // returns the orf start/stop positions on unaligned sequence
            String name = (String) f.getAnnotation().getProperty(AnnotationKeys.NAME);

            pm.setNote("Reviewing " + name);
            pm.setProgress(count++);

            List datum = new ArrayList();

            // only calculate statistics if gene feature is in displayed region
            if (LocationTools.overlaps(l, new RangeLocation(absstart, absstop))) {
                l = LocationTools.intersection(l, new RangeLocation(absstart, absstop));

                int start = m_seq.getRelativePosition(l.getMin());
                int stop = m_seq.getRelativePosition(l.getMax());

                int gapLength = stop - start + 1;
                int absLength = l.getMax() - l.getMin() + 1;

                int diffCount = 0;
                int subs = 0;
                int ins = 0;
                int del = 0;

                for (j = start; j <= stop; ++j) {
                    if (diffs[j] != DifferenceType.I_NONE) {
                        ++diffCount;
                    }

                    if (diffs[j] == DifferenceType.I_INSERTION) {
                        ++ins;
                    }

                    if (diffs[j] == DifferenceType.I_DELETION) {
                        ++del;
                    }

                    if (DifferenceType.isSubstitution(diffs[j])) {
                        ++subs;
                    }
                }

                iblock = new int[diffs.length];

                dblock = new int[diffs.length];

                sblock = new int[diffs.length];

                boolean inIf = false;
                for (j = start; j <= stop; ++j) {
                    inIf = false;
                    if (diffs[j] == DifferenceType.I_INSERTION) {
                        for (int k = j + 1; k <= stop; ++k) {
                            if (diffs[k] != DifferenceType.I_INSERTION) {
                                iblock[k - j]++;

                                if ((k - j) > maxi) {
                                    maxi = k - j;
                                }

                                j = k;
                                inIf = true;
                                break;
                            }
                        }
                    }

                    if (diffs[j] == DifferenceType.I_DELETION) {

                        for (int k = j + 1; k <= stop; ++k) {
                            if (diffs[k] != DifferenceType.I_DELETION) {
                                if ((k - j) > maxd) {
                                    maxd = k - j;
                                }

                                dblock[k - j]++;
                                j = k;
                                inIf = true;
                                break;
                            }
                        }
                    }

                    if (DifferenceType.isSubstitution(diffs[j])) {

                        for (int k = j + 1; k <= stop; ++k) {
                            if (!DifferenceType.isSubstitution(diffs[k])) {
                                if ((k - j) > maxs) {
                                    maxs = k - j;
                                }

                                sblock[k - j]++;
                                j = k;
                                inIf = true;
                                break;
                            }
                        }
                    }

                    if (inIf) {
                        j--;
                    }
                }

                StringBuffer subStr = new StringBuffer(subs + "");
                StringBuffer delStr = new StringBuffer(del + "");
                StringBuffer insStr = new StringBuffer(ins + "");

                for (j = 0; j <= maxs; ++j) {
                    if (sblock[j] > 0) {
                        subStr.append(", " + j + " (" + sblock[j] + ")");
                    }
                }

                for (j = 0; j <= maxi; ++j) {
                    if (iblock[j] > 0) {
                        insStr.append(", " + j + " (" + iblock[j] + ")");
                    }
                }

                for (j = 0; j <= maxd; ++j) {
                    if (dblock[j] > 0) {
                        delStr.append(", " + j + " (" + dblock[j] + ")");
                    }
                }

                int upDiffs = 0;

                if (f.getStrand().equals(StrandedFeature.POSITIVE)) {
                    for (j = start - UPSTREAM; j <= start; ++j) {
                        if (j < 0) {
                            continue;
                        }

                        if (diffs[j] != DifferenceType.I_NONE) {
                            ++upDiffs;
                        }
                    }
                } else {
                    for (j = stop + 1; j < (stop + 201); ++j) {
                        if (j >= diffs.length) {
                            break;
                        }

                        if (diffs[j] != DifferenceType.I_NONE) {
                            ++upDiffs;
                        }
                    }
                }

                datum.add(f.getAnnotation().getProperty(AnnotationKeys.NAME));
                //                datum.add(f.getAnnotation().getProperty(AnnotationKeys.PRODUCT));
                datum.add(f.getStrand().equals(StrandedFeature.POSITIVE) ? "Top" : "Bottom");
                datum.add(String.valueOf(l.getMin()));
                datum.add(String.valueOf(l.getMax()));
                datum.add(String.valueOf(absLength));
                datum.add(String.valueOf(gapLength));
                datum.add(String.valueOf(diffCount));
                datum.add(m_format.format((double) diffCount / (double) gapLength * 100) + "%");
                datum.add(subStr.toString());
                datum.add(insStr.toString());
                datum.add(delStr.toString());
                datum.add(String.valueOf(upDiffs)); //upstream
                datum.addAll(getD(f));
                //				datum.addAll(getCounterpartData(f));
                m_data.put(f, datum);
            } else { // add 'zeroed' row
                datum.add(f.getAnnotation().getProperty(AnnotationKeys.NAME));
                datum.add(f.getStrand().equals(StrandedFeature.POSITIVE) ? "Top" : "Bottom");
                datum.add(String.valueOf(l.getMin()));
                datum.add(String.valueOf(l.getMax()));
                datum.add(String.valueOf(l.getMax() - l.getMin() + 1));
                datum.add(String.valueOf(l.getMax() - l.getMin() + 1));
                datum.add(String.valueOf(0));
                datum.add("0.00");
                datum.add(String.valueOf(0));
                datum.add(String.valueOf(0));
                datum.add(String.valueOf(0));
                datum.add(String.valueOf(0));
                datum.addAll(getD(f));
                //				datum.addAll(getCounterpartData(f));
            }
        }

        pm.close();
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


        int start = m_seq.getRelativePosition(l.getMin());
        int stop = m_seq.getRelativePosition(l.getMax());

        int stdStart = m_standard.getRelativePosition(start);
        int stdStop = m_standard.getRelativePosition(stop);


        String seq1 = "";
        String seq2 = "";

        ArrayList ret = new ArrayList();

        try {
            seq1 = m_seq.substring(start, stop + 1);
            seq2 = m_standard.substring(start, stop + 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            ret.add(new Integer(0));
        }

        if (strand == StrandedFeature.NEGATIVE) {
            StringBuffer b = new StringBuffer();

            for (int i = seq1.length() - 1; i >= 0; --i) {
                b.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(seq1.charAt(i)));
            }

            seq1 = b.toString();
            b = new StringBuffer();

            for (int i = seq2.length() - 1; i >= 0; --i) {
                b.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(seq2.charAt(i)));
            }

            seq2 = b.toString();
        }

        AAComparison aaComparison = new AAComparison();
        aaComparison.calculateChanges(seq1, seq2);
        ret.add(String.valueOf(aaComparison.changes));
        ret.add(String.valueOf(aaComparison.silent));

        // find intersection of two genes
        int sectStart;
        int sectStop;
        StrandedFeature feature = (StrandedFeature) findCounterpartFeature(f);
        if (feature == null) {
            ret.add(String.valueOf(0));
            ret.add(String.valueOf(0));
            return ret;
        } else {
            Location seqLocation = feature.getLocation();
            int gene2start = m_standard.getRelativePosition(seqLocation.getMin()); //aligned startPos
            int gene2stop = m_standard.getRelativePosition(seqLocation.getMax());   //aligned stopPos

            sectStart = start > gene2start ? start : gene2start;
            sectStop = stop < gene2stop ? stop : gene2stop;


        }
        try {
            seq1 = m_seq.substring(sectStart, sectStop + 1);
            seq2 = m_standard.substring(sectStart, sectStop + 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            ret.add(String.valueOf(0));
            ret.add(String.valueOf(0));
            return ret;
        }
        if (strand == StrandedFeature.NEGATIVE) {
            StringBuffer b = new StringBuffer();

            for (int i = seq1.length() - 1; i >= 0; --i) {
                b.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(seq1.charAt(i)));
            }

            seq1 = b.toString();
            b = new StringBuffer();

            for (int i = seq2.length() - 1; i >= 0; --i) {
                b.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(seq2.charAt(i)));
            }

            seq2 = b.toString();
        }
        aaComparison.calculateChanges(seq1, seq2);
        ret.add(String.valueOf(aaComparison.changes));
        ret.add(String.valueOf(aaComparison.silent));

        return ret;
    }

    protected List getD(StrandedFeature standard) {
        ArrayList ret = new ArrayList();

        Location stdLoc = standard.getLocation();

        StrandedFeature sequence = (StrandedFeature) findCounterpartFeature(standard);
        if (sequence == null) {
            ret.add(new Integer(0));
            ret.add(new Integer(0));
            ret.add(new Integer(0));
            ret.add("- None -");
            ret.add(stdLoc.getMax() - stdLoc.getMin());
            ret.add(new Integer(0));
            return ret;
        }
        Location seqLoc = sequence.getLocation();

        //        logger.info("stdname "+standard.getAnnotation().getProperty(AnnotationKeys.NAME));
        //        logger.info("seqname "+sequence.getAnnotation().getProperty(AnnotationKeys.NAME));

        int stdStart = m_seq.getRelativePosition(stdLoc.getMin());
        int seqStart = m_standard.getRelativePosition(seqLoc.getMin());

        //        logger.info("stdStartRelSeq "+stdStart);
        //        logger.info("seqStartRelStd "+seqStart);

        int stdStop = m_seq.getRelativePosition(stdLoc.getMax());
        int seqStop = m_standard.getRelativePosition(seqLoc.getMax());

        //        logger.info("stdStopRelSeq "+stdStop);
        //        logger.info("seqStopRelStd "+seqStop);

        String subStd;
        String subSeq;

        int startOffset = Math.abs(stdStart - seqStart);
        int stopOffset = Math.abs(stdStop - seqStop);

        int cmpStart = stdStart > seqStart ? stdStart : seqStart;   // get intersection
        int cmpStop = stdStop < seqStop ? stdStop : seqStop;

        // create substrings of both sequences at intersection points
        try {
            subStd = m_standard.substring(cmpStart, cmpStop + 1);
            //logger.info("subStd = "+subStd);
            subSeq = m_seq.substring(cmpStart, cmpStop + 1);
            //logger.info("subStd = "+subSeq);
        } catch (Exception ex) {
            ex.printStackTrace();
            ret.add(new Integer(0));
            ret.add(new Integer(0));
            ret.add(new Integer(0));
            ret.add("- None -");
            ret.add(stdLoc.getMax() - stdLoc.getMin());
            ret.add(new Integer(0));
            return ret;
        }
        // if bottom gene, we get the reversed complement, comparisons are still done left to right
        if (standard.getStrand() == StrandedFeature.NEGATIVE) {
            StringBuffer b = new StringBuffer();

            for (int i = subStd.length() - 1; i >= 0; --i) {
                b.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(subStd.charAt(i)));
            }

            subStd = b.toString();
            b = new StringBuffer();

            for (int i = subSeq.length() - 1; i >= 0; --i) {
                b.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(subSeq.charAt(i)));
            }

            subSeq = b.toString();
        }
        AAComparison aaComparison = new AAComparison();
        aaComparison.calculateChanges(subStd, subSeq);
        ret.add(String.valueOf(aaComparison.changes)); // AA changes in intersecting regions
        ret.add(String.valueOf(aaComparison.changes + ((startOffset + stopOffset) / 3))); // Total AA changes
        ret.add(String.valueOf(aaComparison.silent));             // add silent changes to return vector
        ret.add(sequence.getAnnotation().getProperty(AnnotationKeys.NAME)); // add counterpart name to return vector
        ret.add(String.valueOf(stdLoc.getMax() - stdLoc.getMin())); // add length of gene on standard to return value
        ret.add(String.valueOf((stdStop - stdStart) - (seqStop - seqStart))); // add length difference

        return ret;
    }

    protected Feature findCounterpartFeature(StrandedFeature f) {
        Location myLoc = f.getLocation();
        Location compLoc = new RangeLocation(m_standard.getAbsolutePosition(m_seq.getRelativePosition(myLoc.getMin())), m_standard.getAbsolutePosition(m_seq.getRelativePosition(myLoc.getMax())));

        FeatureFilter ff = new FeatureFilter.And(new FeatureFilter.And(new FeatureFilter.OverlapsLocation(compLoc), new FeatureFilter.StrandFilter(f.getStrand())), new FeatureFilter.ByType(FeatureType.GENE));

        FeatureHolder fh = m_standard.filter(ff, false);
        Feature compare = null;

        if (fh.countFeatures() == 0)
            ;
        else if (fh.countFeatures() == 1) {
            compare = (Feature) fh.features().next();
        } else { // >1 feature

            Iterator i = fh.features();
            Feature tmp = null;
            int max = 0;

            while (i.hasNext()) {
                Feature myf = (Feature) i.next();
                Location l = LocationTools.intersection(myf.getLocation(), compLoc);

                if ((l.getMax() - l.getMin() + 1) > max) {
                    max = l.getMax() - l.getMin() + 1;
                    tmp = myf;
                }
            }

            compare = tmp;
        }
        return compare;
    }

    /**
     * get the data for a feature's counterpart on the standard sequence.
     *
     * @param f the feature to get data for
     * @return a list of data items
     */
    protected List getCounterpartData(StrandedFeature f) {
        Location myLoc = f.getLocation();
        Location compLoc = new RangeLocation(m_standard.getAbsolutePosition(m_seq.getRelativePosition(myLoc.getMin())), m_standard.getAbsolutePosition(m_seq.getRelativePosition(myLoc.getMax())));

        FeatureFilter ff = new FeatureFilter.And(new FeatureFilter.And(new FeatureFilter.OverlapsLocation(compLoc), new FeatureFilter.StrandFilter(f.getStrand())), new FeatureFilter.ByType(FeatureType.GENE));

        FeatureHolder fh = m_standard.filter(ff, false);
        Feature compare = null;
        ArrayList ret = new ArrayList();

        if (fh.countFeatures() == 0) {
            ret.add("-None-");
            ret.add(new Integer(0));
            ret.add(new Integer(0));

            return ret;
        } else if (fh.countFeatures() == 1) {
            compare = (Feature) fh.features().next();
        } else { // >1 feature

            Iterator i = fh.features();
            Feature tmp = null;
            int max = 0;

            while (i.hasNext()) {
                Feature myf = (Feature) i.next();
                Location l = LocationTools.intersection(myf.getLocation(), compLoc);

                if ((l.getMax() - l.getMin() + 1) > max) {
                    max = l.getMax() - l.getMin() + 1;
                    tmp = myf;
                }
            }

            compare = tmp;
        }

        ret.add(compare.getAnnotation().getProperty(AnnotationKeys.NAME));

        int myLen = myLoc.getMax() - myLoc.getMin() + 1;
        int cLen = compare.getLocation().getMax() - compare.getLocation().getMin() + 1;
        ret.add(String.valueOf(cLen));
        ret.add(String.valueOf(myLen - cLen));

        return ret;
    }

    /**
     * print the data to a print writer
     *
     * @param out the writer to print to
     */
    public void printData(PrintWriter out) {
        Iterator i = m_data.keySet().iterator();
        out.println("Name\tabsLen\tgapLen\tdcount\tdpct");

        while (i.hasNext()) {
            Object key = i.next();
            List l = (List) m_data.get(key);

            if (l == null) {
                continue;
            }

            for (int j = 0; j < l.size(); ++j) {
                out.print(l.get(j) + "\t");
            }

            out.print("\n");
        }
    }
}
