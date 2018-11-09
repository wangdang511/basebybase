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

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JViewport;

import ca.virology.baseByBase.data.*;

import ca.virology.baseByBase.io.OverviewImageWriter;

import ca.virology.lib.io.sequenceData.*;
import ca.virology.lib.io.MultiFileFilter;
import ca.virology.lib.util.gui.BookmarkMenu;
import ca.virology.lib.util.gui.UITools;

import org.biojava.bio.symbol.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * @author will
 * @version1.0 This Frame is used to provide a zoomable ineractive Frame to observe mRNA expression data from
 * MOCHIView format
 */


public class RNAOverviewFrame extends JFrame {

    protected JPanel content = new JPanel();
    Dimension d = new Dimension(300, 200);
    protected FeaturedSequence[] m_seqs;
    protected mRNA[] m_rnas;
    protected ArrayList m_pos;
    protected Component m_dataPanel;
    protected PrimaryPanel par;
    protected JPanel p = new JPanel(new BorderLayout());
    protected JScrollPane mainPane;
    protected MainPanel mainPanel;
    protected JSlider m_zoom = new JSlider();
    protected JSlider m_vertZoom = new JSlider();
    JLabel vertScaleBox;
    protected JPanel m_rulerPanel;
    protected RangeLocation m_view = new RangeLocation(1, 2);
    protected int m_visibleStart = -1;
    protected int m_visibleStop = -1;
    protected boolean m_showAll = true;
    protected boolean m_showGenes = true;
    protected boolean m_showPrimers = true;
    protected boolean m_showComments = true;
    protected boolean m_refreshWaiting = false;
    protected final FeaturedSequenceModel m_holder;
    protected SequenceSummaryModel m_summaryModel;
    protected String m_modelType;
    int max_val, min_val;
    protected static final double SCALE_FACTOR = 100000;

    //~ Constructors ///////////////////////////////////////////////////////////


    /**
     * Creates a new OverviewFrame object.
     *
     * @param holder the featured sequence holder that this should pay
     *               attention to
     */
    public RNAOverviewFrame(FeaturedSequenceModel holder, mRNAs rna, PrimaryPanel parent) {
        this("Visual Overview", holder, rna, parent);
    }

    /**
     * Creates a new OverviewFrame object.
     *
     * @param title  The title of the window
     * @param holder the featured sequence holder that this should pay
     *               attention to.
     */
    public RNAOverviewFrame(String title, FeaturedSequenceModel holder, mRNAs rna, PrimaryPanel parent) {

        setTitle(title);
        par = parent;
        content.setLayout(new BorderLayout());

        m_holder = holder;

        m_rnas = rna.getmRNA();
        try {
            m_pos = new ArrayList();
            for (int i = 0; i < m_rnas.length; i++) {
                m_pos.add(m_rnas[i].pos);
            }
        } catch (Exception e) {
            UITools.showWarning("Unsupported/Corrupt file format.", this);
            return;
        }


        FeaturedSequenceModel defInstance = m_holder;
        m_seqs = defInstance.getVisibleSequences();


        String[] models = SummaryModelFactory.getModelNames();


        initUI();
        add(content);
        //mainPanel.renderDisplay(mainPanel.getGraphics());
        setSize(d);
        setVisible(true);

        repaint();


    }

    //~ Methods ////////////////////////////////////////////////////////////////


    /**
     * Build the UI
     */
    protected void initUI() {
        setBackground(Color.white);
        buildMenu();
        mainPanel = new MainPanel();
        mainPane = new JScrollPane(mainPanel);
        mainPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mainPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        content.add(mainPane, BorderLayout.CENTER);
        content.add(buildButtons(), BorderLayout.SOUTH);

        pack();
        pack();

    }

    /**
     * Builds the Menu bar
     */
    protected void buildMenu() {
        JMenuBar jmb = new JMenuBar();
        JMenu fileMenu = makeFileMenu();
        JMenu viewMenu = makeViewMenu();
        jmb.add(fileMenu);
        jmb.add(viewMenu);
        //jmb.add(new BookmarkMenu("Links", DiffEditorFrame.getBookmarkList()));
        setJMenuBar(jmb);
    }


    /**
     * Builds the button panel on the bottom of the panel
     */

    protected JPanel buildButtons() {
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
        btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        btns.add(new JLabel("Vertical Scale: 1 pixel = "));

	/*		vertScaleBox = new JTextArea(Integer.toString((int)m_vertZoom.getValue()));
            vertScaleBox.setEditable(false);
			vertScaleBox.setLineWrap(true);
			vertScaleBox.setWrapStyleWord(true);

			vertScaleBox.setColumns(6); */

        vertScaleBox = new JLabel(Integer.toString((int) m_vertZoom.getValue()));

        btns.add(vertScaleBox);
        pack();
        btns.add(new JLabel(" counts"));

        btns.add(m_vertZoom);

        //  The vertZoom slider handles the vertical scale factor
        m_vertZoom.setMinimum(1);
        m_vertZoom.setMaximum(1000);
        m_vertZoom.setValue(100);
        m_vertZoom.setInverted(true);

        m_vertZoom.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent arg0) {
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
            }

            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseReleased(MouseEvent arg0) {
                vertScaleBox.setText(Integer.toString((int) m_vertZoom.getValue()));
                repaint();
            }

        });


        btns.add(Box.createHorizontalGlue());


        btns.add(Box.createHorizontalGlue());
        btns.add(new JLabel("     Sequence Zoom     "));

        //  The m_zoom slider handles the horizontal zoom factor
        btns.add(m_zoom);
        m_zoom.setMinimum(2);
        m_zoom.setMaximum(100);
        m_zoom.setValue(100);
        m_zoom.setInverted(true);

        m_zoom.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent arg0) {
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
            }

            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseReleased(MouseEvent arg0) {
                mainPane.getViewport().revalidate();
                repaint();
            }
        });

        return btns;
    }

    /**
     * getPrimaryPanel
     *
     * @return the parent primary panel object
     */
    public JPanel getPrimaryPanel() {
        return par;
    }

    /**
     * Set the scale of the sequence displays
     *
     * @param scale The scale
     */
    public void setScale(double scale) {
        m_zoom.setValue((int) scale);
    }

    /**
     * Get the scale of the sequence displays as they are currently displayed
     *
     * @return The scale
     */
    public double getScale() {
        return m_zoom.getValue();
    }

    public double getVertScale() {
        return m_vertZoom.getValue();
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }

    public RNAOverviewFrame getPanel() {
        return this;
    }


    /**
     * Brings up window asking for output options and writes visual overview to a specified file.
     */
    protected void exportToImage() throws java.io.IOException, IllegalArgumentException {
        // find the maximum length sequence
        int maxLen = 0;
        for (int i = 0; i < m_seqs.length; i++) {
            if (m_seqs[i].length() > maxLen) {
                maxLen = m_seqs[i].length();
            }
        }
        // set up components for taking input
        JPanel eip = new JPanel();
        JPanel labels = new JPanel(new GridLayout(4, 1));
        JPanel data = new JPanel(new GridLayout(4, 1));
        JPanel readout = new JPanel(new GridLayout(4, 1));
        int defStart = 1;
        final JSlider startSlider = new JSlider(JSlider.HORIZONTAL, 1, maxLen, defStart);
        final JLabel startLabel = new JLabel(String.valueOf(defStart));
        int defStop = maxLen + 1;
        final JSlider stopSlider = new JSlider(JSlider.HORIZONTAL, 2, maxLen + 1, defStop);
        final JLabel stopLabel = new JLabel(String.valueOf(defStop));
        int defWid = 800;
        final JSlider widthSlider = new JSlider(JSlider.HORIZONTAL, 100, 3000, defWid);
        final JLabel wLabel = new JLabel(String.valueOf(defWid));
        int defSpace = 3;
        final JSlider spacingSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, defSpace);
        final JLabel sLabel = new JLabel(String.valueOf(defSpace));

        data.add(startSlider);
        data.add(stopSlider);
        data.add(widthSlider);
        data.add(spacingSlider);

        labels.add(new JLabel("Start Pos"));
        labels.add(new JLabel("Stop Pos"));
        labels.add(new JLabel("Image Width (pixels)"));
        labels.add(new JLabel("Spacing Width (pixels)"));

        readout.add(startLabel);
        readout.add(stopLabel);
        readout.add(wLabel);
        readout.add(sLabel);

        eip.add(labels, BorderLayout.WEST);
        eip.add(data, BorderLayout.CENTER);
        eip.add(readout, BorderLayout.EAST);

        widthSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                wLabel.setText(widthSlider.getValue() + "");
            }
        });
        spacingSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                sLabel.setText(spacingSlider.getValue() + "");
            }
        });

        startSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                // make sure the start slider is always before the stop slider
                if (stopSlider.getValue() < startSlider.getValue()) {
                    stopSlider.setValue(startSlider.getValue() + 1);
                }
                startLabel.setText(startSlider.getValue() + "");
            }
        });
        stopSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                // make sure the stop slider is always after the start slider
                if (stopSlider.getValue() < startSlider.getValue()) {
                    startSlider.setValue(stopSlider.getValue() - 1);
                }
                stopLabel.setText(stopSlider.getValue() + "");
            }
        });

        // show dialogue with image options
        int rc = JOptionPane.showConfirmDialog(null, eip, "Export Image", JOptionPane.OK_CANCEL_OPTION);

        if (rc == JOptionPane.OK_OPTION) {
            // open file chooser to decide where to save file
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Export Overview Image");

            MultiFileFilter ff1 = new MultiFileFilter("PNG Format");
            ff1.addExtension("png");
            fc.addChoosableFileFilter(ff1);
            fc.setFileFilter(ff1);
            try {
                if (fc.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    String filename = f.getAbsolutePath();
                    javax.swing.filechooser.FileFilter ff = fc.getFileFilter();
                    if (ff == ff1) {
                        if (!ff1.accept(f)) {
                            if (!filename.endsWith(".")) {
                                filename += ".";
                            }
                            filename += "png";
                        }
                    }

                    File outfile = new File(filename);
                    if (outfile.exists()) {
                        if (!UITools.showYesNo(filename + " exists, are you sure you would like to replace it?", this)) {
                            return;
                        }
                    }

                    //  try {

                    BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics2D gp = img.createGraphics();
                    gp.setBackground(Color.white);
                    paintComponent(gp, startSlider.getValue(), stopSlider.getValue(), widthSlider.getValue(), spacingSlider.getValue());


                    ((Graphics2D) this.getGraphics()).drawRenderedImage(img, new AffineTransform());


                    ImageIO.write(img, "png", outfile);

                    repaint();

                }
                // catch (Exception e) {e.printStackTrace();}
                // write the image to the file
                //OverviewImageWriter.writeToFile(m_seqs, m_summaryModel, ((int) startSlider.getValue()) - 1, ((int) stopSlider.getValue()) - 1, (int) widthSlider.getValue(), (int) spacingSlider.getValue(), outfile);
                //}
            } catch (IOException ex) {
                ex.printStackTrace();
                throw (ex);
            } catch (IllegalArgumentException iaex) {
                iaex.printStackTrace();
                throw (iaex);
            }
        }
    }


    /**
     * Creates the file menu with appropriate action listeners.
     *
     * @return the JMenu for the file menu
     */
    protected JMenu makeFileMenu() {
        JMenu fileMenu = new JMenu("File");

        JMenuItem mi = new JMenuItem("Export Image");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    exportToImage();
                } catch (Exception e) {
                    UITools.showWarning("<html>Could not export to image. You may not have enough<br>" + "space or permission to write to the specified file.</html>", getRootPane());
                }
            }
        });
        //fileMenu.add(mi);

        fileMenu.addSeparator();

        mi = new JMenuItem("Close");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                dispose();
            }

            private void dispose() {
                //this = null;

            }
        });
        fileMenu.add(mi);

        return (fileMenu);

    }

    /**
     * Creates the view menu with appropriate action listeners.
     *
     * @return the JMenu for the view menu
     */
    protected JMenu makeViewMenu() {
        JMenu viewMenu = new JMenu("View");

        viewMenu.addSeparator();
        return (viewMenu);
    }

    /**
     * Handles the interactive display between this class and the primary panel
     */
    public void update_panels() {
        System.out.println(min_val + " " + max_val);
        par.m_rnaDisp.setDisplayArea(min_val, max_val);
        par.scrollToLocation(1, min_val, false);
    }

    /**
     * Adjusts the perspected of the Primary Panel to match the users click
     */
    public void update_panels(int x) {
        min_val = (int) (x * getScale());
        max_val = min_val + par.eScroll.getWidth() / par.m_rnaDisp.m_fontWidth;
        setView(min_val, max_val);
        par.scrollToLocation(1, min_val, false);
        repaint();
    }


    /**
     * Handles the actual drawing
     */

    public class MainPanel extends JPanel {

        JScrollPane par;


        public MainPanel() {
            setBackground(Color.white);
            max_val = 0;
            min_val = 0;
            this.setPreferredSize(new Dimension((int) (m_rnas.length / getScale()), 300));
            this.addMouseListener(new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    int x = e.getX();
                    update_panels(x);
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                    int x = e.getX();
                    update_panels(x);
                }

                public void mouseReleased(MouseEvent e) {
                    int x = e.getX();
                    update_panels(x);
                }

            });
        }

        /**
         * Handles drawing by breaking it into 2 tasks
         */
        public void paintComponent(Graphics sg) {

            super.paintComponent(sg);
            Graphics2D g = (Graphics2D) sg;
            this.setBackground(Color.white);
            this.setOpaque(true);
            this.setPreferredSize(new Dimension((int) (m_rnas.length / getScale()), this.getHeight()));

            draw_seqs(g);
            draw_ruler(g);
            //setView(getPrimaryPanel());
            update_panels();
            setView(min_val, max_val);

        }

        /**
         * Draws the mRNA expression data as a series of rectangles
         */

        public void draw_seqs(Graphics2D g) {
            int m_iHeight = 30;
            double horiz_scale = getScale();

            g.setColor(Color.red);

            Rectangle r = g.getClipBounds();

            if (r == null) {
                r = new Rectangle(0, 0, getWidth(), getHeight());
            }


            try {

                for (int i = 0; i < m_rnas.length; i++) {

                    double h1 = (double) Math.round(m_rnas[i].counts / getVertScale());

                    int x = (int) (m_rnas[i].pos / horiz_scale);
                    int w = (int) (1 / horiz_scale);
                    if (w == 0) {
                        w = 1;
                    }
                    double y = Math.round(0.5 * r.getHeight());

                    //  Shift the rectanlge up or down to match the positive or negative sign
                    // of the expression data.
                    if (m_rnas[i].counts >= 0) {
                        y = Math.round(0.5 * r.getHeight() - h1);
                    } else {
                        y = Math.round(0.5 * r.getHeight());
                    }

                    g.fillRect(x, (int) y, w, (int) Math.abs(h1));

                }
            }
            //catch (NullPointerException e) {}
            catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            g.setColor(Color.black);
            g.draw(new Line2D.Float((float) (0), (float) (Math.round(0.5 * r.getHeight())), (float) (horiz_scale * m_rnas.length), (float) (0.5 * r.getHeight())));

            // renderValues(g, charStart, (int) charStop+ charStart);
            g.translate(0, m_iHeight);

            int scale = (int) getScale();
            g.setPaint(Color.green);
            System.out.println(min_val / scale);
            g.drawLine(min_val / scale, -100, min_val / scale, this.getHeight());
            g.drawLine(max_val / scale, -100, max_val / scale, this.getHeight());


        }

        /**
         * Draws the nucleotide base ruler along the top of the panel.
         */
        public void draw_ruler(Graphics2D g) {
            Graphics2D g2 = (Graphics2D) g;
            g.setColor(Color.black);

            java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double();
            int min = 0;
            int max = m_rnas[m_rnas.length - 1].pos;
            double minX = (min / getScale()) + 50;
            double maxX = (max / getScale());
            double scale = 1.0 / getScale();
            int tickHeight = 5;
            int horizLabelOffset = 20;

		    /*        if (!showsAll()) {
		                g.translate(-(int) (min / getScale()), 0);
		            } */

            double halfScale = scale * 0.5;
            line.setLine(minX - halfScale, 0.0, maxX + halfScale, 0.0);
            FontMetrics fMetrics = g2.getFontMetrics();
            // The widest (== maxiumum) coordinate to draw
            int coordWidth = fMetrics.stringWidth(Integer.toString(max));
            // Minimum gap getween ticks
            double minGap = (double) Math.max(coordWidth, 40);
            // How many symbols does a gap represent?
            int realSymsPerGap = (int) Math.ceil(((minGap + 5.0) / scale));
            // We need to snap to a value beginning 1, 2 or 5.
            double exponent = Math.floor(Math.log(realSymsPerGap) / Math.log(10));
            double characteristic = realSymsPerGap / Math.pow(10.0, exponent);

            int snapSymsPerGap;

            if (characteristic > 5.0) {
                // Use unit ticks
                snapSymsPerGap = (int) Math.pow(10.0, exponent + 1.0);
            } else if (characteristic > 2.0) {
                // Use ticks of 5
                snapSymsPerGap = (int) (5.0 * Math.pow(10.0, exponent));
            } else {
                snapSymsPerGap = (int) (2.0 * Math.pow(10.0, exponent));
            }

            if (snapSymsPerGap == 0) {
                snapSymsPerGap = 1;
            }

            int minP = min + ((snapSymsPerGap - min) % snapSymsPerGap);

            for (int index = minP; index <= max; index += snapSymsPerGap) {
                double offset = (double) index * scale;
                // System.out.println(index + " " + offset );
                String labelString = String.valueOf(index);
                float halfLabelWidth = fMetrics.stringWidth(labelString) / 2;
                line.setLine(offset + halfScale, 0.0, offset + halfScale, tickHeight);

                g2.drawString(String.valueOf(index), (float) ((offset + halfScale) - halfLabelWidth), horizLabelOffset);
                g2.draw(line);
            }
        }

        public int[] get_view() {
            int[] val = {min_val, max_val};
            return val;
        }

    }


    public void setView(int start, int stop) {
        try {
            Graphics2D g = (Graphics2D) this.getGraphics();
            int scale = (int) getScale();
            g.setPaint(Color.red);
            g.drawLine(start * scale, 0, start * scale, this.getHeight());
            g.drawLine(stop * scale, 0, stop * scale, this.getHeight());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handles Drawing a savable image!
     */
    public void paintComponent(Graphics sg, int start, int stop, int width, int spacing) {

        // super.paintComponent(sg);
        Graphics2D g = (Graphics2D) sg;
        g.setBackground(Color.white);

        draw_seqs(g, start, stop, width, spacing);
        draw_ruler(g, start, stop, width, spacing);
        //setView(getPrimaryPanel());
        update_panels();
        // setView(min_val, max_val);

    }

    /**
     * Draws the mRNA expression data as a series of rectangles
     */

    public void draw_seqs(Graphics2D g, int start, int stop, int width, int spacing) {
        int m_iHeight = 30;
        double horiz_scale = getScale();
        g.setColor(Color.red);

        Rectangle r = g.getClipBounds();

        if (r == null) {
            r = new Rectangle(0, 0, getWidth(), getHeight());
        }

        this.setPreferredSize(new Dimension(width, this.getHeight()));


        try {

            for (int i = 0; i < m_rnas.length; i++) {

                if (m_rnas[i].pos < start) {
                    continue;
                } else if (m_rnas[i].pos > stop) {
                    break;
                }

                double h1 = (double) Math.round(m_rnas[i].counts / getVertScale());

                int x = (int) (m_rnas[i].pos / horiz_scale);
                int w = (int) (1 / horiz_scale);
                if (w == 0) {
                    w = 1;
                }
                double y = Math.round(0.5 * r.getHeight());

                //  Shift the rectanlge up or down to match the positive or negative sign
                // of the expression data.
                if (m_rnas[i].counts >= 0) {
                    y = Math.round(0.5 * r.getHeight() - h1);
                } else {
                    y = Math.round(0.5 * r.getHeight());
                }

                g.fillRect(x, (int) y, w, (int) Math.abs(h1));

            }
        }
        //catch (NullPointerException e) {}
        catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        g.setColor(Color.white);
        g.draw(new Line2D.Float((float) (0), (float) (Math.round(0.5 * r.getHeight())), (float) ((stop - start) / getScale()), (float) (0.5 * r.getHeight())));

        // renderValues(g, charStart, (int) charStop+ charStart);
        g.translate(0, m_iHeight);

    }

    /**
     * Draws the nucleotide base ruler along the top of the panel.
     */
    public void draw_ruler(Graphics2D g, int start, int stop, int width, int spacing) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.white);
        g2.setPaint(Color.white);
        g2.setBackground(Color.white);
        java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double();
        int min = start;
        int max = start;
        double minX = (min / getScale()) + 50;
        double maxX = (max / getScale());
        double scale = 1.0 / getScale();
        int tickHeight = 5;
        int horizLabelOffset = 20;

	    /*        if (!showsAll()) {
	                g.translate(-(int) (min / getScale()), 0);
	            } */

        double halfScale = scale * 0.5;
        line.setLine(minX - halfScale, 0.0, maxX + halfScale, 0.0);
        FontMetrics fMetrics = g2.getFontMetrics();
        // The widest (== maxiumum) coordinate to draw
        int coordWidth = fMetrics.stringWidth(Integer.toString(max));
        // Minimum gap getween ticks
        double minGap = (double) Math.max(coordWidth, 40);
        // How many symbols does a gap represent?
        int realSymsPerGap = (int) Math.ceil(((minGap + 5.0) / scale));
        // We need to snap to a value beginning 1, 2 or 5.
        double exponent = Math.floor(Math.log(realSymsPerGap) / Math.log(10));
        double characteristic = realSymsPerGap / Math.pow(10.0, exponent);

        int snapSymsPerGap;

        if (characteristic > 5.0) {
            // Use unit ticks
            snapSymsPerGap = (int) Math.pow(10.0, exponent + 1.0);
        } else if (characteristic > 2.0) {
            // Use ticks of 5
            snapSymsPerGap = (int) (5.0 * Math.pow(10.0, exponent));
        } else {
            snapSymsPerGap = (int) (2.0 * Math.pow(10.0, exponent));
        }

        if (snapSymsPerGap == 0) {
            snapSymsPerGap = 1;
        }

        int minP = min + ((snapSymsPerGap - min) % snapSymsPerGap);

        for (int index = minP; index <= max; index += snapSymsPerGap) {
            double offset = (double) index * scale;
            // System.out.println(index + " " + offset );
            String labelString = String.valueOf(index);
            float halfLabelWidth = fMetrics.stringWidth(labelString) / 2;
            line.setLine(offset + halfScale, 0.0, offset + halfScale, tickHeight);

            g2.drawString(String.valueOf(index), (float) ((offset + halfScale) - halfLabelWidth), horizLabelOffset);
            g2.draw(line);
        }
    }


}






