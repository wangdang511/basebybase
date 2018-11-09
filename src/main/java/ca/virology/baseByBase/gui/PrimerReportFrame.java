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

import ca.virology.baseByBase.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.w3c.dom.Element;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.io.*; //remove this when not needed

import java.util.*;

import javax.swing.*;


/**
 * This frame displays a table of information which represents a statistical
 * breakdown of each gene in the displayed sequence, including start/stop
 * positions and information on differences for each gene.
 *
 * @author Ryan Brodie
 */
public class PrimerReportFrame extends JFrame {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static final int UPSTREAM = 200;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected java.text.NumberFormat m_format;
    //protected FeaturedSequence       m_seq;
    //protected FeaturedSequence       m_standard;
    protected FeaturedSequence[] m_seqs;
    protected int m_start;
    protected int m_stop;
    protected int m_length;
    protected ReportTablePanel m_tPane;
    protected Map m_data;
    private static String[] ColumnNames = {"Sequence", "Name", "Comment", "Location", "Melting Temp", "Start", "Stop", "Length", "Strand", "Species"};


    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Construct a new frame to display the gvien sequences
     *
     * @param seq      the query sequence
     * @param standard the sequence to compare to
     * @param start    the start of the area to compute
     * @param stop     the end of the area to compute
     */
    public PrimerReportFrame(
            /*FeaturedSequence seq,
             FeaturedSequence standard,*/
            FeaturedSequence[] seqs, int start, int stop) {
		/*m_seq = seq;
		 m_standard = standard;*/

        m_seqs = seqs;

        //might try top re-implement
		/*m_start = start;
		 m_stop = stop;
		 
		 if (m_start == -1) {
		 m_start = 0;
		 }
		 
		 if (m_stop == -1) {
		 m_stop = m_seq.length() - 1;
		 }
		 
		 m_length = m_stop - m_start + 1;
		 
		 m_format = java.text.NumberFormat.getInstance();
		 m_format.setMaximumFractionDigits(2);
		 m_format.setMinimumFractionDigits(2);*/

        //calculate();
        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * initialize the user interface for this window
     */
    protected void initUI() {
        setTitle("Primer Report");

        m_tPane = new ReportTablePanel(getData(), getComparators(), getColumnNames());
        m_tPane.setRowName("Primer");

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
        menu.addSeparator();

        mi = new JMenuItem("Save Selected (Comma Separated Values)");
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
        menu.addSeparator();

        mi = new JMenuItem("Save Selected (Tab Separated Values)");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                writeTSV(false);
            }
        });
        menu.add(mi);
        mi = new JMenuItem("Save All (TSV)");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                writeTSV(true);
            }
        });
        menu.add(mi);
        menu.addSeparator();

        mi = new JMenuItem("Close");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                PrimerReportFrame.this.dispose();
            }
        });
        menu.addSeparator();
        menu.add(mi);
		
		/*menu = new JMenu("View");
		 menu.setMnemonic(KeyEvent.VK_V);
		 JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Show Only Different Genes");
		 menuItem.addActionListener(new ActionListener() {
		 public void actionPerformed(ActionEvent evt) {
		 JCheckBoxMenuItem checkboxMenuItem = (JCheckBoxMenuItem)evt.getSource();
		 m_tPane.setShowOnlyDifferentGenes(checkboxMenuItem.getState());
		 }
		 });
		 menu.add(menuItem);
		 mb.add(menu);
		 
		 mb.add(new BookmarkMenu("Links", DiffEditorFrame.getBookmarkList()));
		 */

        return mb;
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

                for (int i = 0; i < m_seqs.length; i++) {
                    String head = "<H1>Primer Report</H1>" + "<H2>" +
                            m_seqs[i].getName() + "</H2>";
                    String tail = "<BR><HR>Base-by-Base Report Generation";

                    out.write(m_tPane.getHtml(head, tail, writeAll));
                    out.flush();
                }
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
     * write the current table (or a selected portion) to a TSV file
     *
     * @param writeAll if true this will write all the table to file, otherwise
     *                 it will write the selected portion only.
     */
    protected void writeTSV(boolean writeAll) {
        MultiFileFilter ff = new MultiFileFilter("TSV Files");
        ff.addExtension("tab");
        ff.addExtension("txt");

        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(ff);
        fc.setDialogTitle("Save TSV File");

        if (fc.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();

                FileWriter out = new FileWriter(f);

                out.write(m_tPane.getTSV(writeAll));
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

        v.add(new GeneNameComparator());//Sequence
        v.add(new GeneNameComparator());//Name
        v.add(new GeneNameComparator());//Comment
        v.add(new GeneNameComparator());//Location
        v.add(new NumberComparator(true));//Melting Temp
        v.add(new NumberComparator(true));//Start
        v.add(new NumberComparator(true));//Stop
        v.add(new NumberComparator(true));//Length
        v.add(new GenericComparator());//Strand
        v.add(new GeneNameComparator());

        return v;
    }

    /**
     * get the data to dispay in this window
     *
     * @return a vector of vectors, each representing a row in the table
     */
    protected Vector getData() {
        Vector ret = new Vector();
        /////////////////////////
        FeatureFilter pfilt = new FeatureFilter.ByType(FeatureType.PRIMER);

        for (int s = 0; s < m_seqs.length; s++) {
            FeatureHolder primers = m_seqs[s].filter(pfilt, false);
            // primers
            if (primers.countFeatures() > 0) {

                for (Iterator i = primers.features(); i.hasNext(); ) {

                    Vector v = new Vector();
                    StrandedFeature f = (StrandedFeature) i.next();
                    Location l = f.getLocation();
                    //StringBuffer cmt = new StringBuffer( f.getAnnotation().getProperty(AnnotationKeys.NAME).toString());

                    if (f.getStrand() == StrandedFeature.NEGATIVE) {
                        String segseq = f.getAnnotation().getProperty(AnnotationKeys.PRIMER_SEQ).toString();
                        //String revseq = new StringBuffer().append(segseq).reverse().toString();
                        v.add(segseq);
                        //System.out.println(segseq+"\n"+revseq);
                    } else {
                        String segseq = f.getAnnotation().getProperty(AnnotationKeys.PRIMER_SEQ).toString();
                        v.add(segseq);
                    }
                    String name = f.getAnnotation().getProperty(AnnotationKeys.NAME).toString();
                    v.add(name);
                    String comment = f.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT).toString();
                    v.add(comment);
                    String fridge = f.getAnnotation().getProperty(AnnotationKeys.PRIMER_FRIDGE).toString();
                    v.add(fridge);
                    String melt = f.getAnnotation().getProperty(AnnotationKeys.PRIMER_MELTINGTEMP).toString();
                    v.add(melt);
                    String start = "" + l.getMin();
                    v.add(start);
                    String stop = "" + l.getMax();
                    v.add(stop);
                    String length = "" + (l.getMax() - l.getMin());
                    v.add(length);
                    String strand = f.getStrand().toString();
                    v.add(strand);
                    String organism = m_seqs[s].getName();
                    v.add(organism);

                    ret.add(v);
                }
            }
        }

        return ret;
    }

    /**
     * calculate the data to display - no calculaion yet
     */
    protected void calculate() {
    }
}
