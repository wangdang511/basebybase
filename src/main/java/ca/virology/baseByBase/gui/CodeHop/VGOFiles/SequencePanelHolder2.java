package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

import ca.virology.lib.prefs.DBPrefs;
import ca.virology.lib.prefs.VGOPrefs;
import ca.virology.lib.util.common.Args;
import ca.virology.lib.util.common.Logger;
import ca.virology.lib.util.gui.OptionsDialog;
import ca.virology.vgo.data.GraphData;
import ca.virology.vgo.gui.*;
import ca.virology.vgo.gui.Utils;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;


import ca.virology.vgo.util.*;
import ca.virology.vgo.data.GraphData;

import javax.swing.*;
import java.awt.*;

import java.awt.event.*;
import java.util.*;

// biojava import
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

import ca.virology.lib.util.common.Args;
import ca.virology.lib.util.common.Logger;
import ca.virology.lib.util.gui.*;
import ca.virology.lib.prefs.VGOPrefs;
import ca.virology.lib.prefs.DBPrefs;

/**
 * This handles scrolling, naming and view property handling of a
 * SequencePanel object.  This is THE place where view settings for the
 * sequence panel and sequence renderer are determined and handled. If you
 * add any new renderers or such things that need view settings global to
 * the sequence panel, make sure to edit this window or you won't get a
 * proper way to edit them.
 *
 * @author Ryan Brodie
 * @date July 8, 2002
 */
public class SequencePanelHolder2 extends JPanel {
    protected static final Font NAME_FONT = new Font("", Font.PLAIN, 11);
    protected static final Font OPT_FONT = new Font("", Font.PLAIN, 11);
    protected static final Color BG_COLOR = new Color(150, 150, 150);
    protected static final Color FG_COLOR = Color.black;

    protected static final int DEF_EXTENT = 1000;

    /**
     * The VGO SequencePanel shows most of the data
     */
    public VGOSequencePanel2 m_seqPanel;
    /**
     * The graph panel shows the graph data (histogram, etc)
     */
    protected GraphPanel m_graphPanel;

    protected int m_selStart = -1;
    protected int m_selStop = -1;

    // flags and values for visibility -- initially retrieved from the settings file
    protected boolean m_showBottom;
    protected boolean m_showORF;
    protected boolean m_showSS;
    //	protected boolean m_showLCS;
    protected boolean m_showRR;
    protected boolean m_showBBBComments;
    protected boolean m_showBBBPrimers;
    protected boolean m_showGFS;
    //	protected int m_LCSminLength;
    protected String m_showGeneName;
    protected int m_orfLength;
    protected boolean m_showDesc;


    // important swing components
    protected final JLabel m_nameLabel = new JLabel();
    protected final JComboBox m_geneList = new JComboBox();
    protected final JScrollBar m_horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);


    public SequencePanelHolder2(ca.virology.vgo.data.Organism o, boolean useDefaultView) {
        initSequencePanelHolder(new VGOSequencePanel2(o, this), useDefaultView);
    }

    private void initSequencePanelHolder(final VGOSequencePanel2 child, boolean useDefaultView) {
        m_seqPanel = child;
        m_seqPanel.addSelectionListener(new SelectionBroadcaster());

        m_graphPanel = new GraphPanel(child.getOrganism(), GraphPanel.PLOT);
        m_graphPanel.addSelectionListener(new SelectionBroadcaster());

        initViewSettings(useDefaultView);

        setGraphVisible(false);

        refreshViewSettings(true);
        initGeneList();
        initComponents();
        initLayout();

        if (useDefaultView) {
            int height = 105;
            int descHeight = (m_showDesc) ? 12 : 0;
            if (m_showSS) height += 31 + descHeight;
            if (m_showGFS) height += 33 + descHeight;
            if (m_showORF) height += 31 + descHeight;
            if (m_showRR) height += 31 + descHeight;
            if (m_showBBBComments) height += 31 + descHeight;
            if (m_showBBBPrimers) height += 31 + descHeight;
            if (m_showBottom) {
                height += 33 + descHeight;
                if (m_showSS) height += 31 + descHeight;
                if (m_showGFS) height += 33 + descHeight;
                if (m_showORF) height += 31 + descHeight;
                if (m_showRR) height += 31 + descHeight;
                if (m_showBBBComments) height += 31 + descHeight;
                if (m_showBBBPrimers) height += 31 + descHeight;
            }
            setPreferredSize(new Dimension(800, height));
        } else {
            int height = 105;
            if (m_showBottom) height += 33;
            setPreferredSize(new Dimension(800, height));
        }

        child.addMouseListener(new PopupListener(child));
    }

    public ca.virology.vgo.data.Organism getOrganism() {
        return m_seqPanel.getOrganism();
    }

    public void addMouseListener(MouseListener l) {
        super.addMouseListener(l);

        m_graphPanel.addMouseListener(l);
    }

    public void removeMouseListener(MouseListener l) {
        super.removeMouseListener(l);
        m_graphPanel.removeMouseListener(l);
    }

    public void setGraphData(GraphData gds) {
        m_graphPanel.setGraphData(gds);
        setGraphVisible(true);
    }

    /**
     * Makes the graph pane visible or invisible
     *
     * @param visible if true, the graph will be visible (if it is defined) otherwise it'll be false
     */
    public void setGraphVisible(boolean visible) {
        m_graphPanel.setVisible(visible);
    }

    /**
     * Gets the seqPanel attribute of the SequencePanelHolder object
     *
     * @return The seqPanel value
     */
    public VGOSequencePanel2 getSeqPanel() {
        return m_seqPanel;
    }


    /**
     * Scrolls the panel to the desired position
     *
     * @param position an integer representing a sequence position offset to scroll to
     */
    public void scrollTo(int position) {
        m_horizontalScrollBar.setValue(position);
    }

    /**
     * Sets the scale used to render the underlying sequence panel, as well as the
     * extent of the scrollbar
     *
     * @param scale A double representing the new scale
     */
    public void setScale(double scale) {
        m_seqPanel.setScale(scale);
        m_graphPanel.setScale(scale);
        m_horizontalScrollBar.setBlockIncrement((int) (DEF_EXTENT * scale));
    }


    /**
     * Changes the display setting for this sequence, using the provided parameters.
     *
     * @param showSS       Whether the start/stop codons should be displayed
     * @param showORF      Whether the ORFS should be displayed
     * @param orfLength    The minimum ORF length
     * @param showBottom   Whether the bottom strand should be displayed
     * @param showGeneName What type of label to use for genes
     * @param showDesc     Whether the lane descriptons should be displayed
     * @param showGFS      Whether the GFS results should be displayed
     */

    private int getHeightEstimate() {
        int height = 105;
        int descHeight = (m_showDesc) ? 12 : 0;
        if (m_showSS) height += 31 + descHeight;
        if (m_showGFS) height += 33 + descHeight;
        if (m_showORF) height += 31 + descHeight;
        if (m_showRR) height += 31 + descHeight;
        if (m_showBBBComments) height += 31 + descHeight;
        if (m_showBBBPrimers) height += 31 + descHeight;
        int geneLabelHeight = 0;
        if (m_showGeneName != null && (m_showGeneName.equals(GeneLabelRenderer.GENE_NAME) ||
                m_showGeneName.equals(GeneLabelRenderer.FAMILY_NUMBER) ||
                m_showGeneName.equals(GeneLabelRenderer.SHORT_NAME) ||
                m_showGeneName.equals(GeneLabelRenderer.GB_NAME))) {
            geneLabelHeight += 9;
        }
        if (m_showBottom) {
            height += 33 + descHeight + geneLabelHeight;
            if (m_showSS) height += 31 + descHeight;
            if (m_showGFS) height += 33 + descHeight;
            if (m_showORF) height += 31 + descHeight;
            if (m_showRR) height += 31 + descHeight;
            if (m_showBBBComments) height += 31 + descHeight;
            if (m_showBBBPrimers) height += 31 + descHeight;
        }

        return height;
    }


    protected void initViewSettings(boolean useDefaultView) {
        Args vArgs = Args.getInstance();
        String dbName = m_seqPanel.m_organism.getDbName();
        DBPrefs dbp = DBPrefs.getInstance();
        VGOPrefs vgoP = VGOPrefs.getInstance();
        if (false) {
            try {
                if (dbp.isSingleStranded(dbName) &&
                        !Boolean.valueOf(vgoP.get_vgoPref("map.showBottom")).booleanValue()) {
                    m_showBottom = false;
                } else {
                    m_showBottom = true;
                }
                m_showORF = new Boolean(vgoP.get_vgoPref("map.showORF")).booleanValue();
                m_showSS = new Boolean(vgoP.get_vgoPref("map.showSS")).booleanValue();
                m_showGFS = new Boolean(vgoP.get_vgoPref("map.showGFS")).booleanValue();
                m_showRR = new Boolean(vgoP.get_vgoPref("map.showRR")).booleanValue();
                m_showBBBComments = new Boolean(vgoP.get_vgoPref("map.showBBBComments")).booleanValue();
                m_showBBBPrimers = new Boolean(vgoP.get_vgoPref("map.showBBBPrimers")).booleanValue();
                m_showGeneName = vgoP.get_vgoPref("map.showGeneName");
                //m_showGeneAnalysis = new Boolean( m_settings.getProperty( "map.showGeneAnalysis" ) ).booleanValue();
                m_showDesc = new Boolean(vgoP.get_vgoPref("map.showDescriptions")).booleanValue();
                try {
                    m_orfLength = Integer.parseInt(vgoP.get_vgoPref("map.orfLength"));
                } catch (NumberFormatException ex) {
                    Logger.log(ex);
                    ca.virology.vgo.gui.Utils.showError("Error reading from settings file, Number format error: " + ex.getMessage());
                }
            } catch (Exception ex) {
                Utils.showError("Error reading settings: " + ex.getMessage());
                Logger.log(ex);
            }
        } else {
            if (dbp.isSingleStranded(dbName)) m_showBottom = false;
            else m_showBottom = true;
            m_showORF = false;
            m_showSS = false;
            m_showGFS = false;
            m_showRR = false;
            m_showBBBComments = false;
            m_showBBBPrimers = false;
            m_showGeneName = "None";
            m_showDesc = false;
            m_orfLength = 200;
        }
    }


    protected void refreshViewSettings(boolean isInitial) {
        m_seqPanel.displayBottom(m_showBottom);
        m_seqPanel.displayORF(m_showORF);
        m_seqPanel.displaySS(m_showSS);
        m_seqPanel.displayGFS(m_showGFS);
        m_seqPanel.displayGeneName(m_showGeneName);
        m_seqPanel.displayRepeatRegions(m_showRR);
        m_seqPanel.displayBBBComments(m_showBBBComments);
        m_seqPanel.displayBBBPrimers(m_showBBBPrimers);

        //m_seqPanel.displayGeneAnalysis( m_showGeneAnalysis );
        m_seqPanel.displayGeneAnalysis(true);

        m_seqPanel.setORFLength(m_orfLength);
        m_seqPanel.displayLaneDescriptions(m_showDesc);

        if (!isInitial) {
            // Resize this pane, then resize the window
            int oldHeight = getHeight();
            int newHeight = getHeightEstimate();
            setPreferredSize(new Dimension(600, newHeight));
            JFrame parent = (JFrame) getTopLevelAncestor();
            int parentHeight = parent.getHeight();
            parent.setSize(parent.getWidth(), parentHeight + newHeight - oldHeight);

            // Adjust the panes to optimal size, if there is more than one sequence
            try {
                if (getParent() instanceof JSplitPane) {
                    JSplitPane splitPane = (JSplitPane) getParent();
                    splitPane.setDividerLocation(-1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Move the window up half of the height increase
            int windowX = parent.getX();
            int windowY = parent.getY();
            parent.setLocation(windowX, windowY - (newHeight - oldHeight) / 2);
        }
    }

    protected void initGeneList() {
        m_geneList.setMaximumSize(new java.awt.Dimension(60, 20));

        SortedSet vals = new TreeSet();

        FeatureFilter filter = new FeatureFilter.ByType("gene");
        final FeatureHolder features = m_seqPanel.getOrganism().getSequence().filter(filter, false);

        String[] sortedNames = null;
        if (features.countFeatures() > 0) {
            Iterator featureIterator = features.features();
            StrandedFeature sf = (StrandedFeature) featureIterator.next();
            if (sf.getSource() != null) {
                java.util.List namesList = new ArrayList();
                for (featureIterator = features.features(); featureIterator.hasNext(); ) {
                    sf = (StrandedFeature) featureIterator.next();
                    String geneName = (String) sf.getAnnotation().getProperty("name");
                    int insertionIndex = namesList.size();
                    for (int i = 0; i < namesList.size(); i++) {
                        if (geneName.compareTo((String) namesList.get(i)) < 0) {
                            insertionIndex = i;
                            break;
                        }
                    }
                    namesList.add(insertionIndex, geneName);
                }
                sortedNames = new String[namesList.size()];
                for (int i = 0; i < namesList.size(); i++) {
                    sortedNames[i] = (String) namesList.get(i);
                }
            } else {
                try {
                    sortedNames = ca.virology.vgo.data.GeneFactory.getInstance().getSortedGeneNames(
                            m_seqPanel.getOrganism().getID(),
                            m_seqPanel.getOrganism().getDbName());
                } catch (Exception ex) {
                    sortedNames = new String[0];
                    Utils.showError("Error sorting names: " + ex.getMessage());
                    Logger.log(ex);
                }
            }
        } else {
            sortedNames = new String[0];
        }

        m_geneList.setModel(new DefaultComboBoxModel(sortedNames));

        m_geneList.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        JComboBox cb = (JComboBox) ev.getSource();
                        String gene = (String) cb.getSelectedItem();
                        Logger.println("Selected " + gene);

                        Iterator i = features.features();
                        while (i.hasNext()) {
                            StrandedFeature f = (StrandedFeature) i.next();
                            Annotation a = f.getAnnotation();

                            String name = (String) a.getProperty("name");
                            if (name != null && name.equals(gene)) {
                                Location l = f.getLocation();
                                int point = (l.getMin());
                                scrollTo(point);
                            }
                        }
                    }
                }
        );
    }


    /**
     *
     */
    protected void initLayout() {
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(new JLabel("Primers:"));
        top.add(Box.createHorizontalGlue());
        top.add(Box.createHorizontalStrut(5));

        JPanel mid = new JPanel(new BorderLayout());
        mid.setBackground(java.awt.Color.white);
        mid.setOpaque(true);
        mid.add(m_seqPanel, BorderLayout.CENTER);
        mid.add(m_graphPanel, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout());
        JScrollPane sp = new JScrollPane(
                mid, //m_seqPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bottom.add(sp, BorderLayout.CENTER);
        bottom.add(m_horizontalScrollBar, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);
    }


    /**
     *
     */
    protected void initComponents() {
        m_nameLabel.setText("  " + m_seqPanel.getName());

        m_horizontalScrollBar.addAdjustmentListener(
                new AdjustmentListener() {
                    public void adjustmentValueChanged(AdjustmentEvent ae) {
                        JScrollBar source = (JScrollBar) ae.getAdjustable();
                        int translation = source.getValue();
                        setSymbolTranslation(translation);
                    }
                }
        );
        m_horizontalScrollBar.setValues(0, DEF_EXTENT, 0, m_seqPanel.getSequence().length());

        m_nameLabel.setFont(NAME_FONT);
        m_nameLabel.setForeground(FG_COLOR);
    }


    /**
     * Sets the symbolTranslation attribute of the SequencePanelHolder object
     *
     * @param transVal The new symbolTranslation value
     */
    public void setSymbolTranslation(int transVal) {
        m_seqPanel.setSymbolTranslation(transVal);
        m_graphPanel.setSymbolTranslation(transVal);
    }

    public void selectRegion(int start, int stop, StrandedFeature.Strand strand, String description) {
        if ((start == m_selStart && stop == m_selStop) ||
                (stop == m_selStart && start == m_selStop)) return;

        m_selStart = start;
        m_selStop = stop;

        if (m_graphPanel != null) {
            m_graphPanel.selectRegion(start, stop);
        }
        if (m_seqPanel != null) {
            m_seqPanel.selectRegion(start, stop, description, strand);
        }
    }

    protected class SelectionBroadcaster implements SelectionListener {
        public void selection(SelectionEvent ev) {
            selectRegion(ev.getSeqStart(), ev.getSeqStop(), ev.getStrand(), ev.getDescription());
        }
    }

    /**
     * Handles the poupup menu for the sequence panel contained.  This
     * gets the popup menu for the sequence panel and then adds its own
     * options for more general things to that menu
     */
    protected class PopupListener extends MouseAdapter {
        protected VGOSequencePanel2 m_panel;
        protected JMenuItem m_visSettings;
        protected JPopupMenu m_lastMenu;

        public PopupListener(VGOSequencePanel2 p) {
            m_panel = p;

            m_visSettings = new JMenuItem("Visual Settings...");
            m_visSettings.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent ev) {
                            //showSettingsDialog();
                            System.out.println("nothing11");
                        }
                    }
            );
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (!e.isPopupTrigger()) return;

            JPopupMenu pu = m_panel.getPopupMenu();

            if (pu != m_lastMenu) {
                m_lastMenu = pu;
                pu.addSeparator();
                pu.add(m_visSettings);
            }

            pu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}

