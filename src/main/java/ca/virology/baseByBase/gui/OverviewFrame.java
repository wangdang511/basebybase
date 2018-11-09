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

import ca.virology.baseByBase.data.FeaturedSequenceModel;
import ca.virology.baseByBase.data.SequenceSummaryModel;
import ca.virology.baseByBase.data.SummaryModelFactory;
import ca.virology.baseByBase.io.OverviewImageWriter;
import ca.virology.lib.io.MultiFileFilter;
import ca.virology.lib.io.sequenceData.EditableSequence;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.util.gui.BookmarkMenu;
import ca.virology.lib.util.gui.UITools;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.RangeLocation;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//import com.sun.servicetag.SystemEnvironment;

/*#######################################################################>>>>>
* Purpose:  This window displays the 'Visual Summary' which is generally available from
* the report menu in BBB.
* Written: ??? Ryan Brodie
* Edited: Summer 2015
* #######################################################################>>>>>*/
public class OverviewFrame extends JFrame {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected FeaturedSequence[] m_seqs;
    protected SummarySequencePanel[] m_views;
    protected Component m_dataPanel;
    protected JPanel m_heads;
    protected JSlider m_zoom = new JSlider();
    protected JButton m_refreshButton = new JButton("Refresh");
    protected JButton m_centerButton = new JButton("Center");
    protected RulerPanel m_rulerPanel;
    protected RangeLocation m_view = new RangeLocation(1, 2);
    protected int m_visibleStart = -1;
    protected int m_visibleStop = -1;
    protected boolean m_showAll = true;
    protected boolean m_showGenes = true;
    protected boolean m_showSubs = true;
    protected boolean m_showIndels = true;
    protected boolean m_showPrimers = true;
    protected boolean m_showComments = true;
    protected Set m_listeners = new HashSet();
    protected boolean m_refreshWaiting = false;
    protected RefreshThread m_refreshThread = null;
    protected final FeaturedSequenceModel m_holder;
    protected SequenceSummaryModel m_summaryModel;
    protected String m_modelType;

    protected JLabel m_legend = new JLabel();
    protected JLabel m_legendHeader = new JLabel("Legend:");
    protected static final double SCALE_FACTOR = 100000;
    protected static boolean ready = false;

    public static final int PAIRWISE_COMPARISON = 0x000A;
    public static final int CONSENSUS_COMPARISON = 0x000B;
    public static final int MODEL_COMPARISON = 0x000C;
    static int m_compType = PAIRWISE_COMPARISON;

    private ActionListener buttonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            refreshScrollPosition();
        }
    };

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new OverviewFrame object.
     *
     * @param holder the featured sequence holder that this should pay
     *               attention to
     */
    public OverviewFrame(FeaturedSequenceModel holder, int type) {
        this("Visual Summary", holder, type);
    }

    /*#######################################################################>>>>>
    * Purpose:  Creates a new OverviewFrame object.
    * Param: title -> The title of this window
    *       holder -> The featured sequence holder that this should pay attention to.
    * Written: ??? Ryan Brodie
    * Edited: Summer 2015
    * #######################################################################>>>>>*/
    public OverviewFrame(String title, FeaturedSequenceModel holder, int type) {
        super(title);

        m_holder = holder;

        m_compType = type;

        FeaturedSequenceModel defInstance = m_holder;
        m_seqs = defInstance.getVisibleSequences();
        m_views = new SummarySequencePanel[m_seqs.length];

        String[] models = SummaryModelFactory.getModelNames();

        m_summaryModel = SummaryModelFactory.createSummaryModel(models[0], m_seqs, m_compType);
        m_modelType = models[0];

        m_refreshThread = new RefreshThread(1000);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                refreshScale();
            }

            public void componentHidden(ComponentEvent e) {
                m_refreshThread.signalStop();
            }

            public void componentShown(ComponentEvent e) {
                m_refreshThread.start();
                refreshScale();
                repaint();
            }
        });

        initUI();
        ready = true;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    // resets the horizontal scroll bar to center at the region selected in the primary view window
    void refreshScrollPosition() {

        JSplitPane jSplitPane = (JSplitPane) m_dataPanel;
        JScrollPane jScrollPane = (JScrollPane) jSplitPane.getBottomComponent();
        JScrollBar jScrollBar = jScrollPane.getHorizontalScrollBar();

        int scrollMax = jScrollBar.getMaximum();

        int mainViewMin = m_view.getMin();
        int mainViewMax = m_view.getMax();

        int start = Math.max(m_visibleStart, 0);
        int stop = (m_visibleStop == -1) ? m_summaryModel.getLength() : m_visibleStop;

        float centerOfView = ((mainViewMax - mainViewMin) / 2) + mainViewMin; // centered sequence position of visible area on primary view
        float centerRatio = centerOfView / (stop - start); // ratio of position to length of visible sequence

        int scrollPos = Math.round(scrollMax * centerRatio); // corresponding position of the horizontal scroll bar

        int visibleAmount = jScrollBar.getVisibleAmount();
        float numOfBlocks = scrollMax / visibleAmount;
        float blockSize = scrollMax / numOfBlocks;
        int offset = Math.round(blockSize / 2);

        jScrollPane.getHorizontalScrollBar().setValue(scrollPos - offset);
        repaint();
    }

    void setLegendVisible(boolean visible) {
        m_legend.setVisible(visible);
        m_legendHeader.setVisible(visible);
    }

    /**
     * Set the scale of the sequence displays
     *
     * @param scale The scale
     */
    public void setScale(double scale) {
        for (int i = 0; i < m_views.length; ++i) {
            m_views[i].setScale(scale);
        }
    }

    /**
     * Get the scale of the sequence displays as they are currently displayed
     *
     * @return The scale
     */
    public double getScale() {
        if (m_views.length <= 0) {
            return 100.0;
        } else {
            return m_views[0].getScale();
        }
    }

    /**
     * set the summary model type. See the SummaryModelFactory class
     *
     * @param modelType the model type
     */
    public void setModelType(String modelType) {
        SequenceSummaryModel mod = SummaryModelFactory.createSummaryModel(modelType, m_seqs, m_compType);

        if (mod != null) {
            m_summaryModel = mod;
            m_modelType = modelType;

            for (int i = 0; i < m_views.length; ++i) {
                m_views[i].setSummaryModel(mod);
            }
            Image img = m_summaryModel.createLegendImage();
            if (img != null) {
                m_legend.setIcon(new ImageIcon(img));
                m_legend.setText(null);
            } else {
                m_legend.setIcon(null);
                m_legend.setText("Not Defined!");
            }
        }
    }

    /**
     * get the current data model
     *
     * @return the data model
     */
    public SequenceSummaryModel getSummaryModel() {
        return m_summaryModel;
    }

    /**
     * Refresh the scale used in this window (based on a possibly resized
     * window.
     */
    protected void refreshScale() {
        Rectangle r = getBounds();

        int start = Math.max(m_visibleStart, 0);
        int stop = (m_visibleStop == -1) ? m_summaryModel.getLength() : m_visibleStop;

        if (!showsAll()) {
            setScale((double) (stop - start + 1) / ((double) r.width * m_zoom.getValue() - 136.0));
            m_rulerPanel.setRange(start, stop);
        } else {
            setScale((double) m_summaryModel.getLength() / ((double) r.width * m_zoom.getValue() - 136.0));
            m_rulerPanel.setRange(0, m_summaryModel.getLength());
        }

        repaint();
    }

    /**
     * Determine if the 'show all' option is set.  Show all means that the
     * whole alignment is shown and not just the current 'display area' which
     * would be zoomed in if this option is false.
     *
     * @return the value of the property
     */
    public boolean showsAll() {
        return m_showAll;
    }

    /**
     * set the 'show all' option.
     *
     * @param b the new property value
     *          <p>
     *          see showAll()
     */
    public void setShowAll(boolean b) {
        m_showAll = b;
        refreshScale();

        for (int i = 0; i < m_views.length; ++i) {
            m_views[i].setShowAll(b);
        }
    }

    /**
     * Determine if the 'show genes' option is set. Determines whether gene feature information
     * will be displayed under the sequences.
     *
     * @return the value of the property
     */
    public boolean showsGenes() {
        return m_showGenes;
    }

    /**
     * set the 'show genes' option.
     *
     * @param b the new property value
     *          <p>
     *          see showAll()
     */
    public void setShowGenes(boolean b) {
        m_showGenes = b;
        setSequences(m_holder.getVisibleSequences());

        refresh();
        validate();
        invalidate();
    }

    /**
     * Determine if the 'show differences' option is set. Determines whether the differences between alignments
     * will be displayed.
     *
     * @return the value of the property
     */
    public boolean showsSubs() {
        return m_showSubs;
    }

    /**
     * set the 'show differences' option.
     *
     * @param b the new property value
     */

    public void setShowSubs(boolean b) {
        m_showSubs = b;
        setSequences(m_holder.getVisibleSequences());

        refresh();
        validate();
        invalidate();
    }
    /**
     * Determine if the 'show differences' option is set. Determines whether the differences between alignments
     * will be displayed.
     *
     * @return the value of the property
     */
    public boolean showsIndels() {
        return m_showIndels;
    }

    /**
     * set the 'show differences' option.
     *
     * @param b the new property value
     */

    public void setShowIndels(boolean b) {
        m_showIndels = b;
        setSequences(m_holder.getVisibleSequences());

        refresh();
        validate();
        invalidate();
    }

    public boolean showsPrimers() {
        return m_showPrimers;
    }


    /**
     * set the 'show primers' option.
     *
     * @param b the new property value
     *          <p>
     *          see showAll()
     */
    public void setShowPrimers(boolean b) {
        m_showPrimers = b;
        setSequences(m_holder.getVisibleSequences());

        refresh();
        validate();
        invalidate();
    }


    public boolean showsComments() {
        return m_showComments;
    }

    /**
     * set the 'show comments' option.
     *
     * @param b the new property value
     *          <p>
     *          see showAll()
     */
    public void setShowComments(boolean b) {
        m_showComments = b;
        setSequences(m_holder.getVisibleSequences());

        refresh();
        validate();
        invalidate();
    }

    /**
     * Refresh the model.  This recalculates the consensus used to calculate
     * the locations of differences.
     */
    public void refresh() {
        m_summaryModel.refresh();
        repaint();
    }

    /**
     * Set the display area of this window.  This narrows the view of the
     * window to a smaller region (unless the 'show all' option is set, in
     * which case it would be masked instead).
     *
     * @param start the leftmost location of the view area (or -1 for the end)
     * @param stop  the rightmost location of the view area (or -1 for the end)
     */
    public void setDisplayArea(int start, int stop) {
        m_visibleStart = start;
        m_visibleStop = stop;

        for (int i = 0; i < m_views.length; ++i) {
            m_views[i].setDisplayArea(start, stop);
        }
    }

    /**
     * This sets the 'view' property of the window.  This is different from the
     * display area in that it indicates the currently displayed are in the
     * main window.
     *
     * @param start The leftmost viewable region of the main window
     * @param stop  The rightmost viewable region of the main window
     */
    public void setView(int start, int stop) {
        m_view = new RangeLocation(start, stop);

        for (int i = 0; i < m_views.length; ++i) {
            m_views[i].setView(start, stop);
        }
    }

    /**
     * Get the view of the main window as it is displayed in this window
     *
     * @return A <CODE>RangeLocation</CODE> object.
     */
    public RangeLocation getView() {
        return m_view;
    }

    /**
     * Return true if there are action listeners listening to this window
     *
     * @return true if there are listeners listening
     */
    public synchronized boolean hasListeners() {
        return !m_listeners.isEmpty();
    }

    /**
     * Add an action listener to this window
     *
     * @param l The listener
     */
    public synchronized void addActionListener(ActionListener l) {
        m_listeners.add(l);
    }

    /**
     * Remove an action listener from this window
     *
     * @param l The listener
     */
    public synchronized void removeActionListener(ActionListener l) {
        m_listeners.remove(l);
    }

    /**
     * Set the sequences to display in this window
     *
     * @param seqs The sequences to display (in an array)
     */
    public synchronized void setSequences(FeaturedSequence[] seqs) {
        m_seqs = seqs;
        m_summaryModel = SummaryModelFactory.createSummaryModel(m_modelType, m_seqs, m_compType);
        m_views = new SummarySequencePanel[m_seqs.length];
        getContentPane().remove(m_dataPanel);
        m_dataPanel = createDataComponent();
        Image img = m_summaryModel.createLegendImage();
        if (img != null) {
            m_legend.setIcon(new ImageIcon(img));
            m_legend.setText(null);
        } else {
            m_legend.setIcon(null);
            m_legend.setText("Not Defined!");
        }

        getContentPane().add(m_dataPanel, BorderLayout.CENTER);
        refreshScale();
        ((JComponent) getContentPane()).revalidate();
    }

    /**
     * Fire an action event, send it to all action listeners
     *
     * @param ev The event to broadcast
     */
    protected synchronized void fireActionEvent(ActionEvent ev) {
        Iterator i = m_listeners.iterator();

        while (i.hasNext()) {
            ActionListener l = (ActionListener) i.next();
            l.actionPerformed(ev);
        }
    }

    /**
     * This sets a flag that the consensus should be
     *
     * @param waiting The status flag
     */
    protected synchronized void setRefreshWaiting(boolean waiting) {
        m_refreshWaiting = waiting;
    }

    /**
     * Get the flag indicating a consensus refresh is necessary
     *
     * @return The flag value
     */
    protected synchronized boolean isRefreshWaiting() {
        return m_refreshWaiting;
    }

    /**
     * Create the dataview component
     *
     * @return The data view component
     */
    protected Component createDataComponent() {
        JPanel main = new JPanel(new VerticalFlowLayout(0));
        main.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createEmptyBorder(0, 5, 5, 0)));
        main.setBackground(Color.white);
        main.setOpaque(true);

        m_heads = new JPanel(new VerticalFlowLayout(0));
        m_heads.setBackground(Color.white);
        m_heads.setOpaque(true);

        MouseInputAdapter viewListener = new MouseInputAdapter() {
            public void mouseClicked(MouseEvent ev) {
                act(ev);
            }

            public void mouseDragged(MouseEvent ev) {
                act(ev);
            }

            protected void act(MouseEvent ev) {
                SummarySequencePanel ssp = (SummarySequencePanel) ev.getSource();
                int newPos = ssp.graphicsToSequence((int) ev.getX());
                Location l = getView();
                int width = (l.getMax() - l.getMin() + 1);

                setView(newPos, newPos + width);
                ActionEvent aev = new ActionEvent(OverviewFrame.this, 0, "ChangeView(" + newPos + ")");
                fireActionEvent(aev);
            }
        };

        for (int i = 0; i < m_seqs.length; ++i) {
            SummarySequencePanel ssp = new SummarySequencePanel(m_seqs[i], m_summaryModel);
            // decide whether to show genes -- BEFORE -- creating headers
            ssp.setShowGenes(showsGenes());
            ssp.setShowSubs(showsSubs());
            ssp.setShowIndels(showsIndels());
            ssp.setShowPrimers(showsPrimers());
            ssp.setShowComments(showsComments());

            HeaderPanel hp = new HeaderPanel(ssp);
            hp.setDisplayFont(new Font("", Font.PLAIN, 12));
            main.add(ssp);
            m_heads.add(hp);
            m_views[i] = ssp;

            ssp.addMouseListener(viewListener);
            ssp.addMouseMotionListener(viewListener);

            m_seqs[i].addPropertyChangeListener(EditableSequence.SEQUENCE_PROPERTY, new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent evt) {
                    setRefreshWaiting(true);
                }
            });
        }

        m_rulerPanel = new RulerPanel();
        m_rulerPanel.setBackground(Color.white);
        m_rulerPanel.setOpaque(true);
        main.add(m_rulerPanel);
        JPanel lbox = new JPanel();
        lbox.setBackground(Color.white);
        lbox.setOpaque(true);
        lbox.setLayout(new BoxLayout(lbox, BoxLayout.X_AXIS));
        lbox.add(m_legendHeader);
        lbox.add(Box.createHorizontalStrut(5));
        lbox.add(m_legend);
        lbox.add(Box.createHorizontalGlue());
        main.add(lbox);
        final JScrollPane mScroll = new JScrollPane(main, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final JScrollPane hScroll = new JScrollPane(m_heads, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        hScroll.setBackground(Color.white);
        hScroll.setOpaque(true);

        // bind the main scrollbar to the header list
        mScroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent ev) {
                Point p1 = mScroll.getViewport().getViewPosition();
                Point p2 = hScroll.getViewport().getViewPosition();
                hScroll.getViewport().setViewPosition(new Point((int) p2.getX(), (int) p1.getY()));
                hScroll.repaint();
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, hScroll, mScroll);
        split.setDividerLocation(80);
        split.setDividerSize(2);
        split.setContinuousLayout(false);
        return split;
    }

    /**
     * Refresh the sequences from the global sequence holder object
     */
    protected void refreshSequences() {
        try {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    FeaturedSequence[] seqs = m_holder.getVisibleSequences();
                    setSequences(seqs);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        int defWid = 1000;
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
                    // write the image to the file
                    OverviewImageWriter.writeToFile(m_seqs, m_summaryModel, m_views,((int) startSlider.getValue()) - 1, ((int) stopSlider.getValue()) - 1, (int) widthSlider.getValue(), (int) spacingSlider.getValue(), outfile);
                }
            } catch (IOException ex) {
                throw (ex);
            } catch (IllegalArgumentException iaex) {
                throw (iaex);
            }
        }
    }

    /**
     * Initialize the user interface for this window
     */
    protected void initUI() {
        JMenuBar jmb = new JMenuBar();
        JMenu fileMenu = makeFileMenu();
        JMenu viewMenu = makeViewMenu();
        jmb.add(fileMenu);
        jmb.add(viewMenu);
        jmb.add(new BookmarkMenu("Links", DiffEditorFrame.getBookmarkList()));
        setJMenuBar(jmb);

        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
        btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        m_legend.setBackground(Color.white);
        m_legend.setOpaque(true);
        m_legendHeader.setBackground(Color.white);
        m_legendHeader.setOpaque(true);
        setLegendVisible(true);
        final JComboBox typecombo = new JComboBox(SummaryModelFactory.getModelNames());
        typecombo.setMinimumSize(new Dimension(20, typecombo.getPreferredSize().height));
        typecombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                String old = m_modelType;
                Object val = typecombo.getSelectedItem();
                if ((val == null) || !(val instanceof String)) {
                    return;
                }

                try {
                    setModelType((String) val);
                } catch (IllegalArgumentException ex) {
                    typecombo.setSelectedItem(old);
                    UITools.showWarning("<html>Could not display " + val + ": <br>" +
                            ex.getMessage() + "</html>", getContentPane());
                    setModelType(old);
                }
            }
        });


        btns.add(typecombo);
        btns.add(Box.createHorizontalGlue());
        btns.add(m_refreshButton);
        btns.add(m_centerButton);
        btns.add(Box.createRigidArea(new Dimension(5, 0)));
        btns.add(new JLabel("Global Zoom"));
        btns.add(Box.createRigidArea(new Dimension(5, 0)));
        btns.add(m_zoom);
        m_refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshSequences();
            }
        });
        m_centerButton.setText("Center");
        m_centerButton.addActionListener(buttonListener);
        // leave some width for the scrollbar
        m_zoom.setMinimum(1);
        m_zoom.setMaximum(100);
        m_zoom.setValue(1);
        m_zoom.addChangeListener(new ZoomSliderListener());
        m_dataPanel = createDataComponent();

        Image img = m_summaryModel.createLegendImage();
        if (img != null) {
            m_legend.setIcon(new ImageIcon(img));
            m_legend.setText(null);
        } else {
            m_legend.setIcon(null);
            m_legend.setText("Not Defined!");
        }

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(m_dataPanel, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        setContentPane(p);
        repaint();
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
                    UITools.showWarning("<html>Could not export to image. You may not have enough<br>" + "space or permission to write to the specified file.</html>", getContentPane());
                }
            }
        });
        fileMenu.add(mi);

        fileMenu.addSeparator();

        mi = new JMenuItem("Close");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                m_refreshThread.signalStop();
                dispose();
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

        final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem("Show Whole Alignment");
        cbmi.setSelected(showsAll());
        cbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setShowAll(cbmi.isSelected());
            }
        });
        viewMenu.add(cbmi);

        final JCheckBoxMenuItem genecbmi = new JCheckBoxMenuItem("Show Genes");
        genecbmi.setSelected(showsGenes());
        genecbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setShowGenes(genecbmi.isSelected());
                refreshScale();
            }
        });
        viewMenu.add(genecbmi);

        final JCheckBoxMenuItem subcbmi = new JCheckBoxMenuItem("Show Substitutions");
        subcbmi.setSelected(showsSubs());
        subcbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setShowSubs(subcbmi.isSelected());
                refreshScale();
            }
        });
        viewMenu.add(subcbmi);

        final JCheckBoxMenuItem indelcbmi = new JCheckBoxMenuItem("Show Insertions/Deletions");
        indelcbmi.setSelected(showsIndels());
        indelcbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setShowIndels(indelcbmi.isSelected());
                refreshScale();
            }
        });
        viewMenu.add(indelcbmi);

        final JCheckBoxMenuItem primercbmi = new JCheckBoxMenuItem("Show Primers");
        primercbmi.setSelected(showsPrimers());
        primercbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setShowPrimers(primercbmi.isSelected());
                refreshScale();
            }
        });
        viewMenu.add(primercbmi);

        final JCheckBoxMenuItem comcbmi = new JCheckBoxMenuItem("Show Comments");
        comcbmi.setSelected(showsComments());
        comcbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setShowComments(comcbmi.isSelected());
                refreshScale();
            }
        });
        viewMenu.add(comcbmi);


        viewMenu.addSeparator();

        JMenuItem mi = new JMenuItem("Refresh Sequences");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                refreshSequences();
            }
        });
        viewMenu.add(mi);

        return (viewMenu);
    }

    //~ Inner Classes //////////////////////////////////////////////////////////

    protected class RefreshThread extends Thread {
        protected int m_refresh;
        protected boolean m_stopSignal = false;

        public RefreshThread(int refreshTime) {
            super("Visual Overview Refresh");

            if (refreshTime < 1000) {
                refreshTime = 1000;
            }

            m_refresh = refreshTime;
        }

        public void signalStop() {
            m_stopSignal = true;
        }

        public void run() {
            while (!m_stopSignal) {
                yield();

                if (isRefreshWaiting()) {
                    refresh();
                    setRefreshWaiting(false);
                }

                try {
                    sleep(m_refresh);
                } catch (Exception ex) {
                }
            }
        }
    }

    protected class RulerPanel extends JPanel {
        private int m_min = 0;
        private int m_max = m_summaryModel.getLength();

        public void setRange(int min, int max) {
            m_min = min;
            m_max = max;
            revalidate();
        }

        public Dimension getPreferredSize() {
            if (showsAll()) {
                return new Dimension((int) (m_summaryModel.getLength() / getScale()), 20);
            } else {
                int start = Math.max(m_visibleStart, 0);
                int stop = (m_visibleStop == -1) ? m_summaryModel.getLength() : m_visibleStop;

                return new Dimension((int) ((stop - start) / getScale()), 20);
            }
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g.setColor(Color.black);

            java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double();
            int min = 0;
            int max = m_summaryModel.getLength();
            double minX = (m_min / getScale()) + 50;
            double maxX = (m_max / getScale());
            double scale = 1.0 / getScale();
            int tickHeight = 5;
            int horizLabelOffset = 20;

            if (!showsAll()) {
                g.translate(-(int) (m_min / getScale()), 0);
            }

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

            int minP = min + ((snapSymsPerGap - min) % snapSymsPerGap);

            for (int index = minP; index <= max; index += snapSymsPerGap) {
                double offset = (double) index * scale;
                //                System.out.println(index + " " + offset );
                String labelString = String.valueOf(index);
                float halfLabelWidth = fMetrics.stringWidth(labelString) / 2;
                line.setLine(offset + halfScale, 0.0, offset + halfScale, tickHeight);
                g2.drawString(String.valueOf(index), (float) ((offset + halfScale) - halfLabelWidth), horizLabelOffset);
                g2.draw(line);
            }
            if (!showsAll()) {
                g.translate((int) (m_min / getScale()), 0);
            }
        }
    }

    /**
     * This class is used to listen to adjustment events in the slider and translate those into
     * zoom changes in each of the sequence panels on display.
     *
     * @author asyed
     * @date Feb 2, 2006
     */
    protected class ZoomSliderListener implements javax.swing.event.ChangeListener {
        public void stateChanged(javax.swing.event.ChangeEvent ev) {
            refreshScale();
            refreshScrollPosition();
        }
    }
}
