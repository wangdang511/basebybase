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
 * but WITHOUT ANY WARRANTY; wizthout even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ca.virology.baseByBase.gui;

import ca.virology.baseByBase.DiffEditor;
import ca.virology.baseByBase.data.*;
import ca.virology.baseByBase.io.AppServer2Access;
import ca.virology.baseByBase.io.AppServerAccess;
import ca.virology.baseByBase.util.Debug;
import ca.virology.baseByBase.util.GeneNameComparator;
import ca.virology.baseByBase.util.GenePos;
import ca.virology.baseByBase.util.ReorderableJList;
import ca.virology.lib.io.MultiFileFilter;
import ca.virology.lib.io.reader.TextFileFeaturedSequenceReader;
import ca.virology.lib.io.sequenceData.*;
import ca.virology.lib.io.tools.FeatureTools;
import ca.virology.lib.io.writer.FeaturedSequenceWriter;
import ca.virology.lib.prefs.BBBPrefs;
import ca.virology.lib.search.SearchHit;
import ca.virology.lib.search.SearchTools;
import ca.virology.lib.util.common.SequenceUtility;
import ca.virology.lib.util.gui.UITools;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.PointLocation;
import org.biojava.bio.symbol.RangeLocation;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.Frame;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the main visual panel for the DiffEditor app.  This aggregates
 * header panels and edit panels representing a list of sequences
 * <BR><B>Things to look out for:</B><BR> There are really 3 different 'views'
 * onto an edited sequence. This panel handles all of one and most of another.
 * The three views are 1) The viewport, 2) the displayed area and 3) the whole
 * sequence alignment.<BR><BR> Logically, (1) is a subview of (2) and (2) is a
 * subview of (3). <BR> This class handles logic and dispay for some of the
 * 2nd case and most of the 1st (1st is handled mostly by Java/Swing). To
 * handle the idea of a 'displayed area', I've had to implement a couple of
 * methods, namely <CODE>translate()</CODE> and <CODE>untranslate()</CODE>.
 * translate converts a position from the displayed area into the scale of the
 * whole view, and untranslate converts a position in the whole view back into
 * the displayed view.<BR><B>To Do:</B><BR> Abstract the idea of a 'display
 * area' into some other class that decorates either EditPanel orJPanel
 * PrimaryPanel.
 *
 * @author Ryan Brodie
 * @see EditPanel
 */

/**
 * GUI Layout Note
 * <p>
 * In initUI there is an elaborate GUI layout. The Tree structure of this GUI is
 * shown below
 * <p>
 * Window ->m_seqTools ->split ->r_panel ->eScroll ->m_consScroll ->m_rnaSCroll
 * ->l_panel ->pan1 ->hScroll ->pan2 ->consTool ->m_consHead ->pan3 ->rnaTool
 * ->m_rnaHead
 */

public class PrimaryPanel extends JPanel implements Printable, FeaturedSequenceModel {
    protected static final Font SEQ_FONT = new Font("Lucida Sans Typewriter", Font.PLAIN, 14);
    protected static final Font CHAN_FONT = new Font("", Font.PLAIN, 10);
    protected static final Font PRINT_FONT = new Font("Courier", Font.PLAIN, 8);
    protected static final int CHAN_HEIGHT = 0;
    protected static final int CHAN_SPACE = 0;
    protected static final int CONS_HEIGHT = 80;
    protected static final int PANEL_SPACE = 0;
    protected static final int SPLIT_LOC = 125;

    /**
     * TODO: DOCUMENT ME!
     */
    public static final int PAIRWISE_COMPARISON = 0x000A;

    /**
     * TODO: DOCUMENT ME!
     */
    public static final int CONSENSUS_COMPARISON = 0x000B;
    public static final int MODEL_COMPARISON = 0x000C;
    protected ColorScheme m_colorScheme = new IdentityColorScheme();
    protected int m_colorType = -1;
    protected int m_diffColor = ColorScheme.DIFF_CLASSIC_SCHEME;
    protected final java.util.List<EditPanel> m_epanels;
    protected final java.util.List<HeaderPanel> m_hpanels;
    protected JScrollPane m_scroller;
    protected Consensus m_consensus = Consensus.EMPTY_CONSENSUS;

    protected final ConsensusDisplay m_consDisp = new ConsensusDisplay(m_consensus, CONS_HEIGHT);
    protected final JScrollPane m_consScroll = new JScrollPane(m_consDisp);
    protected final HeaderPanel m_consHead = new HeaderPanel(m_consDisp);
    protected final JScrollPane m_consHeadScroll = new JScrollPane();

    protected mRNAs mRNA = null;
    protected mRNADisplay m_rnaDisp = new mRNADisplay(CONS_HEIGHT);
    protected JScrollPane m_rnaScroll = new JScrollPane(m_rnaDisp);
    protected JPanel m_rnaHead = new JPanel();
    protected JScrollPane m_rnaHeadScroll = new JScrollPane();
    JSplitPane split;
    protected JSplitPane r_panel;
    protected JSplitPane l_panel;
    protected JPanel pan1;
    protected JPanel pan2;
    protected JPanel pan3;
    JScrollPane hScroll = new JScrollPane();
    JScrollPane eScroll = new JScrollPane();

    protected final JPanel m_editContainer = new JPanel();
    protected final JPanel m_headContainer = new JPanel();
    protected JToolBar m_seqTools;
    protected final DefaultListModel m_headListModel = new DefaultListModel();
    protected final JList m_hList = new JList(m_headListModel);
    protected final JPanel m_eSpacer = new JPanel();
    protected final JPanel m_hSpacer = new JPanel();
    protected JPopupMenu m_popupMenu = new JPopupMenu();
    protected JButton m_frameButton;
    protected OverviewFrame m_ovFrame = null;
    protected RNAOverviewFrame m_rnaOvFrame;
    final UndoManager m_undoManager = new UndoManager();
    protected int m_charStart = -1;
    protected int m_charStop = -1;
    protected int m_mouseMode;
    protected int m_selectedSequence;
    protected boolean m_printSel = false;
    protected boolean m_lastPage = false;
    protected int m_compType = MODEL_COMPARISON;
    protected StrandedFeature m_selectedFeature;
    protected FeaturedSequence[] m_seqFilter = new FeaturedSequence[0];
    protected int[] m_chanPrefs = new int[0];
    protected boolean m_updateDiffs = true;
    protected String m_primerName = null; // This is USED in primer loading from
    // file, do not delete
    SearchHit[] posRes = new SearchHit[0];
    SearchHit[] negRes = new SearchHit[0];
    protected boolean m_processing = false;
    protected boolean isSearchActive = false;
    protected boolean isSearchAborted = false;
    protected boolean m_wholesequences = false; // true if all sequences are
    // selected
    protected File m_currentDirectory = null;

    protected final StatusBar m_status = new StatusBar("");
    protected int count = 0;
    Addon_Tool cons_tool;
    Addon_Tool rna_tool;

    public static int ALREADY_ALIGNED = 0;

    ArrayList newList = new ArrayList();

    /**
     * Creates an empty <code>PrimaryPanel</code>
     */
    public PrimaryPanel() {
        this(new FeaturedSequence[0]);
    }

    /**
     * Create a <code>PrimaryPanel</code> that will display the given list of
     * sequences.
     *
     * @param sequences a list of<CODE>FeaturedSequence</CODE>s to display
     */
    public PrimaryPanel(FeaturedSequence[] sequences) {
        m_epanels = new ArrayList<EditPanel>();
        m_hpanels = new ArrayList<HeaderPanel>();

        initUI();
        initListeners();

        for (FeaturedSequence sequence : sequences) {
            addSequenceEditor(sequence, DiffEditor.getDbName());
        }

        refreshEditors();
        updateDifferenceLists();
        refreshState();

        cons_visible(false);
        rna_visible(false);

        revalidate();
        checkNames(sequences);
    }

    /**
     * Create a <code>PrimaryPanel</code> that will display the given list of
     * sequences.
     *
     * @param sequences a list of<CODE>FeaturedSequence</CODE>s to display
     */
    public PrimaryPanel(java.util.List sequences) {
        m_epanels = new ArrayList<EditPanel>();
        m_hpanels = new ArrayList<HeaderPanel>();

        initUI();
        initListeners();

        for (Object sequence : sequences) {
            addSequenceEditor((FeaturedSequence) sequence, DiffEditor.getDbName());
        }

        refreshEditors();
        updateDifferenceLists();
        refreshState();

        cons_visible(false);
        rna_visible(false);

        revalidate();
    }

    /**
     * Adds a list selection listener to the genome list view on the left side of
     * the panel.
     *
     * @param listener A listener to be invoked when the user selects a genome in
     *                 the left list
     */
    public void addGenomeSelectionListener(ListSelectionListener listener) {
        m_hList.getSelectionModel().addListSelectionListener(listener);
    }

    /**
     * set the display area. See the class comment for an explanation of this
     *
     * @param start the first displayable position
     * @param stop  the last displayable position
     */
    public void setDisplayArea(int start, int stop) {
        scrollToLocation(0);
        m_charStart = start;
        m_charStop = stop;

        m_consDisp.setDisplayArea(start, stop);
        m_rnaDisp.setDisplayArea(start, stop);

        refreshEditors();
        refreshStateAfterPicture();

        if ((getOverviewFrame() != null) && getOverviewFrame().isDisplayable()) {
            getOverviewFrame().setDisplayArea(start, stop);
            getOverviewFrame().refreshScale();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param proc DOCUMENT ME!
     */
    public void setProcessing(boolean proc) {
        m_processing = proc;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isProcessing() {
        return (m_processing);
    }

    /**
     * Set the comparison type of this window. Currently this is only able to accept
     * the CONSENSUS_COMPARISON type.
     *
     * @param compType currently must be CONSENSUS_COMPARISON
     */
    public void setComparisonType(int compType) {
        m_compType = compType;

        if (compType == CONSENSUS_COMPARISON) {
            if (m_consensus == Consensus.EMPTY_CONSENSUS) {
                m_consensus = ConsensusFactory.createConsensus(ConsensusFactory.IDENTITY, getVisibleSequences());
            }
        }

        refreshEditors();
        updateDifferenceLists();
        refreshState();
    }

    /**
     * get the first displayable position or -1 if that is the endpoint
     *
     * @return the first displayable position or -1 if that is the endpoint
     */
    public int getDisplayStart() {
        return (m_charStart);
    }

    /**
     * get the last displayable position or -1 if that is the endpoint
     *
     * @return the last displayable position or -1 if that is the endpoint
     */
    public int getDisplayStop() {
        return (m_charStop);
    }

    /**
     * Get the comparison type used to display the sequences
     *
     * @return the comparison type
     */
    public int getComparisonType() {
        return (m_compType);
    }

    /**
     * get the currently displayed color scheme type
     *
     * @return the color scheme type
     */
    public int getColorScheme() {
        return (m_colorType);
    }

    /**
     * set the color scheme type to be used by this panel and all sub-editors
     *
     * @param schemeType the scheme type
     * @throws IllegalArgumentException if an invalid color scheme type is passed in
     * @see ColorScheme
     */
    public void setColorScheme(int schemeType) {
        switch (schemeType) {
        case -1:
            return;

        case ColorScheme.BBB_SCHEME:
            m_colorType = schemeType;
            m_colorScheme = new IdentityColorScheme();
            break;

        case ColorScheme.HID_SCHEME:
            m_colorType = schemeType;
            m_colorScheme = new IdenticalResiduesColorScheme();
            break;

        case ColorScheme.SIM_SCHEME:
            m_colorType = schemeType;
            m_colorScheme = new SimilarityColorScheme();
            break;

        case ColorScheme.BLOSUM62_SCHEME:
            m_colorType = schemeType;
            m_colorScheme = new ScoreColorScheme(ScoreColorScheme.BLOSUM62);
            break;

        case ColorScheme.PAM250_SCHEME:
            m_colorType = schemeType;
            m_colorScheme = new ScoreColorScheme(ScoreColorScheme.PAM250);
            break;

        case ColorScheme.HYDRO_SCHEME:
            m_colorType = schemeType;
            m_colorScheme = new HydrophobicityColorScheme();
            break;

        case ColorScheme.CLUSTAL_SCHEME:
            m_colorType = schemeType;
            m_colorScheme = new ClustalColorScheme();
            break;

        case ColorScheme.PCT_ID_SCHEME:
            m_colorType = schemeType;
            m_colorScheme = new PctIDColorScheme();
            break;

        case ColorScheme.CUSTOM_SCHEME:
            m_colorType = schemeType;
            m_colorScheme = new CustomColorModel(DiffEditorFrame.letters);
            break;

        default:
            throw (new IllegalArgumentException("Unknown Color Scheme Type"));
        }

        m_colorScheme.setSequences(getVisibleSequences());

        for (EditPanel p : m_epanels) {
            p.setColorScheme(m_colorScheme);
        }
    }

    public void setDifferenceColors(int colorScheme) {
        switch (colorScheme) {

        case ColorScheme.DIFF_CLASSIC_SCHEME:
            m_diffColor = colorScheme;
            break;

        case ColorScheme.DIFF_NT_SCHEME:
            m_diffColor = colorScheme;
            break;
        default:
            throw (new IllegalArgumentException("Unknown Color Scheme Type"));
        }
        for (EditPanel p : m_epanels) {
            p.setDiffColor(m_diffColor);
        }
    }

    /**
     * Gets the strand currently being displayed by this panel
     *
     * @return The strand currently under display
     */
    public StrandedFeature.Strand getDisplayStrand() {
        if (m_epanels.size() == 0) {
            return (StrandedFeature.POSITIVE);
        }

        EditPanel p = (EditPanel) m_epanels.get(0);

        return (p.getDisplayStrand());
    }

    /**
     * Sets the strand to be displayed by the panel
     *
     * @param strand The new strand to display
     */
    public void setDisplayStrand(StrandedFeature.Strand strand) {
        for (EditPanel p : m_epanels) {
            p.setDisplayStrand(strand);
        }

        repaint();
    }

    /**
     * prevents all sequences in a given list from being shown
     *
     * @param hidden the sequences to filter out
     */
    public void setSequenceFilter(FeaturedSequence[] hidden) {
        m_seqFilter = hidden;
        refreshEditors();
        updateDifferenceLists();
        refreshState();
    }

    /**
     * adds the currently selected sequences to the hidden list
     */
    public void hideSelectedSequences() {
        HashSet<FeaturedSequence> s = new HashSet<FeaturedSequence>();
        Collections.addAll(s, m_seqFilter);
        FeaturedSequence[] sel = getSelectedSequences();
        Collections.addAll(s, sel);

        setSequenceFilter(s.toArray(new FeaturedSequence[s.size()]));
    }

    /**
     * Get the list of sequences displayed by this panel
     *
     * @return the array of sequences
     */
    public FeaturedSequence[] getSequences() {
        FeaturedSequence[] seqs = new FeaturedSequence[m_epanels.size()];

        for (int i = 0; i < seqs.length; ++i) {
            seqs[i] = (m_epanels.get(i)).getSequence();
        }

        return (seqs);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FeaturedSequenceWrapper[] getSequenceWrappers() {
        FeaturedSequenceWrapper[] seqs = new FeaturedSequenceWrapper[m_epanels.size()];

        for (int i = 0; i < seqs.length; i++) {
            EditPanel ep = m_epanels.get(i);
            seqs[i].sequence = ep.getSequence();
            seqs[i].dbName = ep.getDbName();
        }

        return (seqs);
    }

    /**
     * Get the editors currently visible in the screen
     *
     * @return the editors visible in an array
     */
    public EditPanel[] getVisibleEditors() {
        ArrayList<EditPanel> l = new ArrayList<EditPanel>();

        for (EditPanel m_epanel : m_epanels) {
            if (m_epanel.isVisible()) {
                l.add(m_epanel);
            }
        }
        return l.toArray(new EditPanel[l.size()]);
    }

    /**
     * Get the sequences currently visible on the screen
     *
     * @return The visible sequences in an array
     */
    public FeaturedSequence[] getVisibleSequences() {
        ArrayList<FeaturedSequence> l = new ArrayList<FeaturedSequence>();

        FeaturedSequence[] seqs = getSequences();

        for (FeaturedSequence seq : seqs) {
            boolean found = false;

            for (FeaturedSequence aM_seqFilter : m_seqFilter) {
                if (seq == aM_seqFilter) {
                    found = true;
                }
            }

            if (!found) {
                l.add(seq);
            }
        }

        return l.toArray(new FeaturedSequence[l.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FeaturedSequenceWrapper[] getVisibleSequenceWrappers() {
        ArrayList<FeaturedSequenceWrapper> l = new ArrayList<FeaturedSequenceWrapper>();

        FeaturedSequenceWrapper[] seqs = getSequenceWrappers();

        for (FeaturedSequenceWrapper seq : seqs) {
            boolean found = false;

            for (FeaturedSequence aM_seqFilter : m_seqFilter) {
                if (seq.sequence == aM_seqFilter) {
                    found = true;
                }
            }

            if (!found) {
                l.add(seq);
            }
        }

        return l.toArray(new FeaturedSequenceWrapper[l.size()]);
    }

    /**
     * This sub function is called by CopyAction from DiffEditor
     *
     * @return returns the copied selection (one sequence only)
     */
    public String getCopySelection() {
        FeaturedSequence[] seqs = getSelectedSequences();

        if (seqs.length > 1 || seqs.length == 0) {
            UITools.showWarning("Please select ONE sequence and select a string before copying", this);

            return (null);
        } else {
            EditPanel p = m_epanels.get(m_selectedSequence);
            FeaturedSequence seq = p.getSequence();
            Location l = p.getAbsoluteSelection();
            int min = seq.getRelativePosition(l.getMin());
            int max = seq.getRelativePosition(l.getMax() + 1);

            String sequence = seq.substring(min, max);

            if (sequence.length() < 2) {
                UITools.showWarning("Please select an area of the sequence", this);

                return (null);
            } else {
                return (sequence);
            }
        }
    }

    /**
     * Insert a given number of gaps into the currently selected set of sequences.
     * The gaps will go into the lefthand marker of the selection if the selection
     * spans more than one base position
     *
     * @param number The number of gaps to insert
     */
    public void insertGaps(int number) {
        FeaturedSequence[] seqs = getSelectedSequences();

        int pos = 0;

        if (m_selectedSequence >= 0) {
            EditPanel p = (EditPanel) m_epanels.get(m_selectedSequence);
            Location l = p.getRelativeSelection();

            if (l == null) {
                UITools.showWarning("Please select an area of the sequence", this);

                return;
            }

            pos = l.getMin();
        } else {
            return;
        }

        ca.virology.lib.io.tools.SequenceTools.insertGaps(seqs, pos, number);

        updateDifferenceLists();
        refreshState();
    }

    /**
     * Returns the location of the next gap roughly centered on the display
     *
     * @param forward Boolean direction to start looking
     */
    public void getGapPosition(boolean forward) {
        int pos = this.getPosition();
        FeaturedSequence[] seqs = getSequences();
        int[] firstGapPositions = new int[seqs.length];
        int gotoPos = 0;
        int currentMinimum = Integer.MAX_VALUE;
        int currentMaximum = Integer.MIN_VALUE;

        // i pos -/+1 to make sure it does not stick on a result
        if (forward) {
            for (int c = 0; c < seqs.length; c++) {
                for (int i = pos + 1; i < seqs[c].length(); i++) {
                    if (seqs[c].charAt(i) == '-') {
                        firstGapPositions[c] = i;

                        break;
                    }
                }
            }

            for (int s = 0; s < firstGapPositions.length; s++) {
                if (firstGapPositions[s] > pos && firstGapPositions[s] < currentMinimum) {
                    currentMinimum = firstGapPositions[s];
                } else {
                    currentMaximum = pos;
                }
            }

            if (currentMinimum > pos) {
                gotoPos = currentMinimum;
            } else {
                gotoPos = pos;
            }

            // reverse section begins here
        } else {
            for (int c = 0; c < seqs.length; c++) {
                for (int i = pos - 1; i > 0; i--) {
                    if (seqs[c].charAt(i) == '-') {
                        firstGapPositions[c] = i;

                        break;
                    }
                }
            }

            for (int s = 0; s < firstGapPositions.length; s++) {
                if (firstGapPositions[s] < pos && firstGapPositions[s] > currentMaximum) {
                    currentMaximum = firstGapPositions[s];
                }
            }

            if (currentMaximum < pos) {
                gotoPos = currentMaximum;
            } else {
                gotoPos = pos;
            }
        }

        if (gotoPos > 0 && gotoPos != Integer.MAX_VALUE) {
            // position is 0-based whereas scrollToLocation takes a 1-based index
            this.scrollToLocation(gotoPos + 1);
        } else {
            UITools.showWarning("No more gaps found in this direction.", null);
        }
    }

    /**
     * Find if search results currently displayed
     *
     * @return true if search results featured
     */

    public boolean searchResultsFeatured() {
        final FeaturedSequence[] seqs = getSelectedSequences();

        for (FeaturedSequence seq : seqs) {
            FeatureFilter ff = new FeatureFilter.ByType(FeatureType.SEARCH_RESULTS);
            Iterator it = seq.filter(ff, false).features();

            while (it.hasNext()) {
                Feature f = (Feature) it.next();

                try {
                    return (true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return (false);
    }

    /**
     * Determines if the search thread is running and thus a results window is
     * presently open.
     */
    public boolean isSearchActive() {
        return (isSearchActive);
    }

    /**
     * Determines if the search has been exited.
     */
    public boolean isSearchAborted() {
        return (isSearchAborted);
    }

    /**
     * Search the selected sequences for a given pattern using a given search
     * type/algorithm
     *
     * @param type             one of SearchTools.FUZZY or SearchTools.REGEX
     * @param searchExpression The search pattern
     * @param mismatches       Max number of mismatches (not always read)
     * @param displayLog       If true, displays a list of the results
     * @param isPrimer         boolean - shows primer specific buttons.
     * @return true if the search was carried out
     */
    public boolean searchSelectedSequences(final int type, final String searchExpression, final int mismatches,
            final boolean displayLog, final boolean isPrimer) {
        return (searchSelectedSequences(type, searchExpression, mismatches, displayLog, isPrimer, null));
    }

    /**
     * DOCUMENT ME!
     *
     * @param type             DOCUMENT ME!
     * @param searchExpression DOCUMENT ME!
     * @param mismatches       DOCUMENT ME!
     * @param displayLog       DOCUMENT ME!
     * @param isPrimer         DOCUMENT ME!
     * @param primerInfo       DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public boolean searchSelectedSequences(final int type, final String searchExpression, final int mismatches,
            final boolean displayLog, final boolean isPrimer, final String[] primerInfo) {
        final JDialog d = new JDialog();
        final String search = searchExpression.toUpperCase();
        final FeaturedSequence[] seqs = getSelectedSequences();

        isSearchActive = true;
        isSearchAborted = false;

        if (seqs.length <= 0) {
            UITools.showWarning("Please mark the sequences you wish to search", null);

            return (false);
        }

        final Map posResMap = Collections.synchronizedMap(new HashMap());
        final Map negResMap = Collections.synchronizedMap(new HashMap());
        final ArrayList<Thread> threads = new ArrayList<Thread>();

        for (int k = 0; k < seqs.length; ++k) {
            final int i = k;
            Thread t = new Thread() {
                public void run() {
                    try {
                        StringBuffer b = ca.virology.lib.io.tools.SequenceTools.getUngappedBuffer(seqs[i].toString());
                        StringBuffer cb = new StringBuffer(); // Complement of sequence 'b'

                        if (SequenceUtility.isDNA(seqs[i].toString())) {
                            for (int j = b.length() - 1; j >= 0; --j) {
                                cb.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(b.charAt(j)));
                            }
                        }

                        SearchHit[] posRes = new SearchHit[0];
                        SearchHit[] negRes = new SearchHit[0];

                        if (type == SearchTools.REGEX) {
                            posRes = SearchTools.regexSearch(b.toString(), search);
                            if (SequenceUtility.isDNA(seqs[i].toString())) {
                                negRes = SearchTools.regexSearch(cb.toString(), search);
                            }
                        } else if (type == SearchTools.FUZZY) {
                            posRes = SearchTools.fuzzySearch(b.toString(), search, mismatches);
                            if (SequenceUtility.isDNA(seqs[i].toString())) {
                                negRes = SearchTools.fuzzySearch(cb.toString(), search, mismatches);
                            }
                        } else {
                            return;
                        }

                        for (int j = 0; j < posRes.length; ++j) {
                            posRes[j] = new SearchHit(posRes[j].getType(), posRes[j].getConfidence(),
                                    posRes[j].getStart(), posRes[j].getStop());
                        }

                        SearchHit[] newNegRes = new SearchHit[negRes.length];

                        for (int j = 0; j < negRes.length; ++j) {
                            newNegRes[negRes.length - 1 - j] = new SearchHit(negRes[j].getType(),
                                    negRes[j].getConfidence(), b.length() - negRes[j].getStop() - 1,
                                    b.length() - negRes[j].getStart());
                        }

                        if ((posRes.length > 0) || (negRes.length > 0)) {
                            posResMap.put(seqs[i], posRes);
                            negResMap.put(seqs[i], newNegRes);
                            FeatureTools.setSearchFeature(seqs[i], search, posRes, newNegRes);
                        } else {
                            // Utils.showInfoMessage("<html>No results found for search:<br>
                            // "+search+"</html>",this);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        UITools.showError("Error searching in " + seqs[i].getName() + ": " + ex.getMessage(), null);

                        return;
                    }
                }
            };

            threads.add(t);
        }

        Runnable r = new Runnable() {
            public void run() {
                for (Thread t : threads) {
                    t.start();
                }

                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                    }
                }

                // update panels
                refreshState();

                boolean foundResults = false;

                for (Object o : posResMap.keySet()) {
                    SearchHit[] hits = (SearchHit[]) posResMap.get(o);

                    if (hits.length > 0) {
                        foundResults = true;
                    }
                }

                for (Object o : negResMap.keySet()) {
                    SearchHit[] hits = (SearchHit[]) negResMap.get(o);

                    if (hits.length > 0) {
                        foundResults = true;
                    }
                }

                if (!foundResults) {
                    if (!isPrimer) {
                        UITools.showInfoMessage("<html>No results found for search:<br>Sequence: " + search + "</html>",
                                PrimaryPanel.this);
                    } else {
                        UITools.showWarning("<html>No results found for search:<br>Sequence: " + search + "<br>Name: "
                                + m_primerName + "</html>", PrimaryPanel.this);
                        ((DiffEditorFrame) PrimaryPanel.this.getTopLevelAncestor()).m_status
                                .setText("No results found for search: " + search);
                    }

                    isSearchActive = false;
                } else if (displayLog) {
                    try {
                        final SearchResultPanel srp = new SearchResultPanel(PrimaryPanel.this, search, posResMap,
                                negResMap, type == SearchTools.FUZZY);
                        JPanel view = new JPanel(new BorderLayout());
                        JPanel btns = new JPanel();

                        // adds save as button to primer searches
                        if (isPrimer) {
                            d.setTitle("Primer Search");

                            d.addWindowListener(new WindowAdapter() {
                                // A listener that will close the window
                                // when the user clicks its close box
                                public void windowClosing(WindowEvent evt) {
                                    int answer = JOptionPane.showConfirmDialog(PrimaryPanel.this, "Load next primer?");

                                    if (answer == JOptionPane.NO_OPTION) {
                                        isSearchAborted = true;
                                        isSearchActive = false;
                                        removeFeaturesFromVisible(FeatureType.SEARCH_RESULTS);
                                        d.dispose();

                                        return;
                                    } else if (answer == JOptionPane.YES_OPTION) {
                                        removeFeaturesFromVisible(FeatureType.SEARCH_RESULTS);
                                        d.dispose();
                                        isSearchActive = false;

                                        return;
                                    } else if (answer == JOptionPane.CANCEL_OPTION) {
                                        removeFeaturesFromVisible(FeatureType.SEARCH_RESULTS);
                                        d.dispose();
                                        isSearchActive = false;

                                        return;
                                    }
                                }
                            }); // end addWindowListener statement

                            if (primerInfo != null) {
                                JButton abortSearch = new JButton("Abort Search");
                                abortSearch.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent ev) {
                                        isSearchAborted = true;
                                        isSearchActive = false;
                                        removeFeaturesFromVisible(FeatureType.SEARCH_RESULTS);
                                        d.dispose();
                                        ((DiffEditorFrame) PrimaryPanel.this.getTopLevelAncestor()).m_status
                                                .setText("Primer saving aborted!");

                                        return;
                                    }
                                });
                                btns.add(Box.createHorizontalGlue());
                                btns.add(abortSearch);
                            }

                            JButton saveAs = new JButton("Save Selected Primer(s)");
                            saveAs.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent ev) {
                                    PrimerEditorPane pane = new PrimerEditorPane();

                                    // String fiveThreeSearch = new
                                    // StringBuffer().append(search).reverse().toString();
                                    pane.setSegmentSequence(search);

                                    try {
                                        // if loaded from table of primers
                                        if (primerInfo != null) {
                                            pane.setName(primerInfo[0]);
                                            pane.setComment(primerInfo[1]);
                                            pane.setFridgeLocation(primerInfo[2]);

                                            // if there is no Temperature in the source file -
                                            // estimate the temperature
                                            if (primerInfo[3].equals(null) || primerInfo[3].equals(" ")
                                                    || primerInfo[3].equals("")) {
                                                pane.setMeltingTemp("" + getPrimerTemp(search));
                                            } else {
                                                pane.setMeltingTemp(primerInfo[3]);
                                            }
                                        } else {
                                            pane.setMeltingTemp("" + getPrimerTemp(search));
                                        }
                                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                                        System.out.println("missing some fields in source file");
                                    }

                                    int val = JOptionPane.showConfirmDialog(srp, pane, "Add Primer to Selection",
                                            JOptionPane.OK_CANCEL_OPTION);

                                    if (val == JOptionPane.OK_OPTION) {
                                        String name = pane.getName();
                                        String segseq = pane.getSeqmentSequence();
                                        String melting = pane.getMeltingTemp();
                                        String fridge = pane.getFridgeLocation();
                                        String comment = pane.getComment();

                                        for (Object o : posResMap.keySet()) {
                                            FeaturedSequence currentseq = (FeaturedSequence) o;

                                            Location l;

                                            List posSel = srp.getPSelected(currentseq);
                                            List negSel = srp.getNSelected(currentseq);

                                            for (Object aPosSel : posSel) {
                                                l = new RangeLocation(((SearchHit) aPosSel).getStart() + 1,
                                                        ((SearchHit) aPosSel).getStop() + 1);
                                                FeatureTools.createPrimerFeature(currentseq, l,
                                                        StrandedFeature.POSITIVE, name, segseq, melting, fridge,
                                                        comment);
                                            } // changed search to segseq

                                            for (Object aNegSel : negSel) {
                                                l = new RangeLocation(((SearchHit) aNegSel).getStart() + 1,
                                                        ((SearchHit) aNegSel).getStop());
                                                FeatureTools.createPrimerFeature(currentseq, l,
                                                        StrandedFeature.NEGATIVE, name, segseq, melting, fridge,
                                                        comment);
                                            }
                                        }
                                    }
                                }
                            });
                            btns.add(Box.createHorizontalGlue());
                            btns.add(saveAs);
                        } else {
                            d.addWindowListener(new WindowAdapter() {
                                public void windowClosing(WindowEvent evt) {
                                    int val = JOptionPane.showConfirmDialog(PrimaryPanel.this,
                                            "Warning: Closing this box will remove search result"
                                                    + " arrows from main display window!",
                                            "Warning", JOptionPane.OK_CANCEL_OPTION);

                                    if (val != JOptionPane.OK_OPTION) {
                                        return;
                                    }

                                    removeFeaturesFromVisible(FeatureType.SEARCH_RESULTS);
                                    d.dispose();
                                }
                            });

                            d.setTitle(((type == SearchTools.FUZZY) ? "Fuzzy " : "Regular Expression ")
                                    + "Search Results");
                        }

                        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));

                        JButton close, fuzzysave, newS = null;

                        if (primerInfo != null) {
                            close = new JButton("Load Next Primer");
                            fuzzysave = new JButton("Save Fuzzy Results");
                        } else {
                            fuzzysave = new JButton("Save Fuzzy Results");
                            close = new JButton("Close");
                            newS = new JButton("New Search");
                        }

                        close.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent ev) {
                                removeFeaturesFromVisible(FeatureType.SEARCH_RESULTS);
                                d.dispose();
                                isSearchActive = false;

                                if (isPrimer) {
                                    ((DiffEditorFrame) PrimaryPanel.this.getTopLevelAncestor()).m_status
                                            .setText("Loading next primer. . .");
                                }

                                return;
                            }
                        });

                        fuzzysave.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent ev) {
                                srp.saveFuzzyInformation();
                                ((DiffEditorFrame) PrimaryPanel.this.getTopLevelAncestor()).m_status
                                        .setText("Successfully saved fuzzy search");
                            }
                        });

                        newS.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                d.dispose();
                                String getInput = JOptionPane.showInputDialog("Regular Expression");
                                if (getInput == null) {
                                    return;
                                } else {
                                    System.out.println("Search Input: " + getInput);
                                }

                                String[] tokenize = getInput.split("-");
                                getInput = "";
                                for (int i = 0; i < tokenize.length; i++) {
                                    if (!tokenize[i].equals("")) {
                                        getInput += tokenize[i];
                                    }
                                }
                                System.out.println("Searching...: " + getInput);

                                final String search = getInput;
                                if ((search == null) || search.equals("")) {
                                    return;
                                }

                                Thread t = new Thread() {
                                    public void run() {
                                        if (!getPrimaryPanel().searchSelectedSequences(SearchTools.REGEX, search, 0,
                                                true, false)) {
                                            System.out.println("Search failed");
                                        }

                                        setEnabled(true);
                                        m_status.clear();
                                    }
                                };

                                setEnabled(false);
                                m_status.setText("Fuzzy Searching for '" + search + "'...");
                                t.start();

                            }
                        });

                        btns.add(Box.createHorizontalGlue());
                        btns.add(close);
                        btns.add(newS);

                        if (primerInfo == null) {
                            btns.add(fuzzysave);
                        }

                        btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
                        view.add(srp, BorderLayout.CENTER);
                        view.add(btns, BorderLayout.SOUTH);

                        d.setContentPane(view);
                        d.setSize(640, 480);
                        d.setModal(false);
                        d.setVisible(true);
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            }
        };

        UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) getTopLevelAncestor(), r,
                "<html>Searching Sequences<br>Please Wait...</html>");

        return (true);
    }

    /**
     * This adds a feature of the given type to the current user selection. The
     * feature will have a source of FeatureType.USER_DEFINED
     *
     * @param type The type of feature to add
     * @return true if succeeded
     */
    public boolean addFeatureToSelection(String type) {
        if (type.equals(ca.virology.lib.io.sequenceData.DifferenceType.TRANSPOSITION)) {
            return (addTransposeEventToSelection());
        } else if (type.equals(FeatureType.COMMENT)) {
            return (addUserCommentToSelection());
        } else if (type.equals(FeatureType.PRIMER)) {
            return (addPrimerToSelection());
        } else {
            return (false);
        }
    }

    /**
     * delete all gaps columns in this alignment. <B>DANGEROUS</B> This will
     * probably delete real sequence data.
     *
     * @return true if it works out
     */
    public boolean deleteAllGaps() {
        int val = JOptionPane.showConfirmDialog(this,
                "<html>This will delete all columns across this alignment " + "where there is at least one gap.<br> "
                        + "This action could possibly delete real sequence <BR>"
                        + "data and cannot be undone, do you wish to continue? </html>",
                "Question", JOptionPane.YES_NO_OPTION);

        if (val == JOptionPane.YES_OPTION) {
            Runnable r = new Runnable() {
                public void run() {
                    ca.virology.lib.io.tools.SequenceTools.deleteGaps(getSequences(), true);
                    updateDifferenceLists();
                    repaint();
                }
            };

            try {
                UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) getTopLevelAncestor(), r,
                        "Finding and deleting all gaps...");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            UndoHandler.getInstance().reset();
            UndoHandler.getInstance().setModified(true);

            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Remove the gaps from the current selection
     */
    public void removeGapsFromSelectedRegion() {
        FeaturedSequence[] selseqs = getSelectedSequences();
        int[] sels = m_hList.getSelectedIndices();

        if (sels.length <= 0) {
            UITools.showWarning("Please mark a sequence to edit", null);

            return;
        }

        EditPanel p = (EditPanel) m_epanels.get(sels[0]);
        Location l = p.getRelativeSelection();

        if (l == null) {
            UITools.showWarning("Please make a selection to remove gaps from", null);

            return;
        }

        for (FeaturedSequence selseq : selseqs) {
            selseq.removeGaps(l.getMin(), l.getMax());
        }

        updateDifferenceLists();
        refreshState();

        if (m_wholesequences) {
            selectAllData();
        }
    }

    /**
     * asks the user for a sequence and inserts it before the selection author
     * mohammed
     */
    public void insertSeqBeforeSelection() {
        Runnable r = new Runnable() {
            public void run() {
                FeaturedSequence[] sSeqs = getSelectedSequences();
                int[] sels = m_hList.getSelectedIndices();

                if (sels == null || sels.length <= 0) {
                    UITools.showWarning("Please mark a sequence to edit", null);
                    return;
                }

                EditPanel p = (EditPanel) m_epanels.get(sels[0]);
                Location l = p.getRelativeSelection();

                if (l == null) {
                    UITools.showWarning("Please make a selection to insert a sequence before", null);
                    return;
                }

                int place = l.getMin();

                String seq = JOptionPane.showInputDialog("Please Type the Sequence");

                if (seq == null) {
                    UITools.showWarning("Invalid Sequence", null);
                    return;
                }

                seq = seq.trim().toLowerCase();

                if (seq.length() == 0) {
                    UITools.showWarning("Invalid Sequence", null);
                    return;
                }

                for (int i = 0; i < seq.length(); i++)
                    if (seq.charAt(i) != 'a' && seq.charAt(i) != 'c' && seq.charAt(i) != 'g' && seq.charAt(i) != 't'
                            && seq.charAt(i) != 'n') {
                        UITools.showWarning("Invalid Charecter: " + seq.charAt(i), null);
                        return;
                    }

                for (int i = 0; i < sSeqs.length; i++) {
                    String finalSeq = sSeqs[i].toString().substring(0, place) + seq
                            + sSeqs[i].toString().substring(place);
                    ((EditPanel) m_epanels.get(sels[i])).getSequence().setSequenceText(finalSeq);
                }
                updateDifferenceLists();
                refreshState();
            }
        };

        UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) this.getTopLevelAncestor(), r,
                "Inserting Sequence");
    }

    /**
     * Remove not selected regions of the sequence author asyed
     */
    public void removeAllButSelectedRegion() {
        Runnable r = new Runnable() {
            public void run() {
                FeaturedSequence[] sSeqs = getSelectedSequences();
                int[] sels = m_hList.getSelectedIndices();

                if (sels.length <= 0) {
                    UITools.showWarning("Please mark a sequence to edit", null);

                    return;
                }

                EditPanel p = (EditPanel) m_epanels.get(sels[0]);
                Location l = p.getRelativeSelection();

                if (l == null) {
                    UITools.showWarning("Please make a selection to keep", null);

                    return;
                }
                int start = l.getMin();
                int stop = l.getMax();
                for (int i = 0; i < sSeqs.length; i++) {
                    String sequence = (sSeqs[i].toString()).substring(start, stop + 1);
                    ((EditPanel) m_epanels.get(sels[i])).getSequence().setSequenceText(sequence);
                }
                updateDifferenceLists();
                refreshState();
            }
        };

        UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) this.getTopLevelAncestor(), r,
                "Removing ALL but selected region");
    }

    /**
     * Remove the current selection author asyed
     */
    public void removeSelectedRegion() {
        Runnable r = new Runnable() {
            public void run() {
                FeaturedSequence[] selseqs = getSelectedSequences();
                int[] sels = m_hList.getSelectedIndices();

                if (sels.length <= 0) {
                    UITools.showWarning("Please mark a sequence to edit", null);

                    return;
                }

                EditPanel p = m_epanels.get(sels[0]);
                Location l = p.getRelativeSelection();

                if (l == null) {
                    UITools.showWarning("Please make a selection to remove", null);

                    return;
                }

                for (FeaturedSequence selseq : selseqs) {
                    selseq.delete(l.getMin(), l.getMax() + 1);
                }

                updateDifferenceLists(); // works on all visible sequences
                refreshState();
            }
        };

        UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) this.getTopLevelAncestor(), r,
                "Removing selected region");
    }

    /**
     * Strip invalid characters from the sequences
     * <p>
     * author Sangeeta Neti
     * <p>
     * Filter the following codes
     * <p>
     * Gap characters (-)
     * <p>
     * For Nucleotides Description Abbreviation
     * ----------------------------------------------- Purine (A or G) R Pyrimidine
     * (C, T, or U) Y C or A M T, U, or G K T, U, or A W C or G S C, T, U, or G (not
     * A) B A, T, U, or G (not C) D A, T, U, or C (not G) H A, C, or G (not T, not
     * U) V Any base (A, C, G, T, or U) N
     * <p>
     * For Amino Acids Description Abbreviation
     * ----------------------------------------------- Aspartic acid or Asparagine
     * Asx B Glutamine or Glutamic acid Glx Z Any amino acid Xaa X
     *
     * @return true if it completed
     */
    public boolean stripInvalidCharacters() {
        boolean withMonitor = true;
        int maxLen = 0;
        int pos = 0;
        int noOfColDeleted = 0;

        SimpleEditableSequence[] seqs = getSequences();
        int seqType = seqs[0].getSequenceType();

        // Build a List of codes to be filtered for Nucleic Acids and Amino Acids
        ArrayList<Character> codesToFilter = new ArrayList<Character>();
        codesToFilter.add(new Character('-'));

        if (seqType == EditableSequence.AA_SEQUENCE) {
            codesToFilter.add(new Character('B'));
            codesToFilter.add(new Character('Z'));
            codesToFilter.add(new Character('X'));
        } else if (seqType == EditableSequence.DNA_SEQUENCE) {
            codesToFilter.add(new Character('R'));
            codesToFilter.add(new Character('Y'));
            codesToFilter.add(new Character('M'));
            codesToFilter.add(new Character('K'));
            codesToFilter.add(new Character('W'));
            codesToFilter.add(new Character('S'));
            codesToFilter.add(new Character('B'));
            codesToFilter.add(new Character('D'));
            codesToFilter.add(new Character('H'));
            codesToFilter.add(new Character('V'));
            codesToFilter.add(new Character('N'));
        }

        SimpleEditableSequence[] clones = new SimpleEditableSequence[seqs.length];

        for (int i = 0; i < seqs.length; ++i) {
            clones[i] = (SimpleEditableSequence) seqs[i].clone();
        }

        // find max sequence length
        for (int i = 0; i < seqs.length; ++i) {
            if (seqs[i].length() > maxLen) {
                maxLen = seqs[i].length();
                pos = i;
            }
        }

        ProgressMonitor pm = null;

        // withMonitor true, a progress monitor will display after a time
        if (withMonitor) {
            pm = new ProgressMonitor(null, "Striping Columns", "", 0, maxLen);
        }

        int start = 0;
        int stop = 0;
        long time0 = System.currentTimeMillis();
        boolean foundElement = false;

        // go through each row and column for any invalid codes
        for (int i = 0; i < clones.length; ++i) {
            if (withMonitor && ((i % 100) == 0)) {
                pm.setNote("Reviewing " + clones[i].getName());
            }

            for (int j = 0; j < clones[i].length(); ++j) {
                if (withMonitor && ((j % 100) == 0)) {
                    pm.setProgress(j);
                }

                // find if the character at this position is an invalid code by iterating
                // through the list
                foundElement = false;
                for (Character aCodesToFilter : codesToFilter) {
                    if ((aCodesToFilter).equals(clones[i].charAt(j))) {
                        foundElement = true;
                    }
                }
                if (foundElement) {
                    int k = j;

                    foundElement = false;
                    for (k = j; k <= clones[i].length(); ++k) {
                        if ((k == clones[i].length())) {
                            break;
                        }

                        for (Character aCodesToFilter : codesToFilter) {
                            if ((aCodesToFilter).equals(clones[i].charAt(k))) {
                                foundElement = true;
                                break;
                            } else
                                foundElement = false;
                        }
                        if (!foundElement)
                            break;
                    }

                    start = j;
                    stop = k;

                    // remove invalid codes from each sequence
                    for (SimpleEditableSequence clone : clones) {
                        if (start < clone.length())
                            clone.delete(start, Math.min(stop, clone.length() - 1));
                        // I think this should be clone.delete(start, Math.min(stop, clone.length()));
                    }
                }
                if (pm.isCanceled()) {
                    pm.close();
                    return false;
                }

            }

            foundElement = false;
            for (Character aCodesToFilter : codesToFilter) {
                if ((aCodesToFilter).equals(clones[i].charAt(clones[i].length() - 1))) {
                    // I think should be if
                    // ((aCodesToFilter).equals(clones[i].charAt(clones[i].length()))) {
                    foundElement = true;
                }
            }

            if (foundElement) {
                ca.virology.lib.io.tools.SequenceTools.deleteSection(clones, clones[i].length() - 1,
                        clones[i].length() - 1);
                // I think should be
                // ca.virology.lib.io.tools.SequenceTools.deleteSection(clones,
                // clones[i].length() - 1, clones[i].length());
            }
        }

        if (withMonitor) {
            pm.close();
        }

        long time1 = System.currentTimeMillis();
        System.out.println("Deletion took " + (time1 - time0) + "ms");

        // display the resulting alignment in a separate window with Save/Cancel option
        noOfColDeleted = seqs[pos].length() - clones[pos].length();

        final ArrayList<SimpleEditableSequence> newList = new ArrayList<SimpleEditableSequence>();

        ResultingStripAlignmentDialog dialog = new ResultingStripAlignmentDialog(newList, noOfColDeleted);

        Collections.addAll(newList, clones);

        dialog.setSequences(newList);
        dialog.setBounds(100, 100, 500, 300);
        dialog.setVisible(true);

        JFileChooser fc = new JFileChooser(m_currentDirectory);
        fc.setDialogTitle("Resulting Sequence Data");

        MultiFileFilter ff1 = new MultiFileFilter("Fasta Format");
        ff1.addExtension("fasta");
        fc.addChoosableFileFilter(ff1);

        MultiFileFilter ff3 = new MultiFileFilter("ClustalW Format");
        ff3.addExtension("aln");
        fc.addChoosableFileFilter(ff3);
        fc.setAcceptAllFileFilterUsed(false);

        fc.setFileFilter(ff1);

        if (dialog.getApproval()) {
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

                            filename += "fasta";
                        }
                    } else if (ff == ff3) {
                        if (!ff3.accept(f)) {
                            if (!filename.endsWith(".")) {
                                filename += ".";
                            }

                            filename += "aln";
                        }
                    }

                    FeaturedSequenceWriter out = FeaturedSequenceWriter.createFeaturedSequenceWriter(filename, "");

                    final ListIterator it = dialog.getSequences();

                    out.writeSequences(it);

                    return true;
                }
            } catch (IOException ex) {
                ex.printStackTrace();

                return false;
            } catch (Exception iex) {
                iex.printStackTrace();

                return false;
            }
        }
        return (true);
    }

    /**
     * delete the selected region from the panel. <B>DANGEROUS</B> This will likely
     * delete real sequence data.
     *
     * @return true if it works out
     */
    public boolean deleteSelectedRegion() {
        int[] sels = m_hList.getSelectedIndices();

        // warn if selection size != size of list ( will create inconsistencies ) --
        // offer to pad gaps in these sequences to maintain alignment (Y/N/C)
        if (sels.length != m_epanels.size()) {
            int val = JOptionPane.showConfirmDialog(this,
                    "<html>The selection does not include all sequences. <BR>"
                            + "This could possibly damage the alignment, do you wish to <BR>" + "continue?</html>",
                    "Question", JOptionPane.YES_NO_OPTION);

            if (val == JOptionPane.NO_OPTION) {
                return (false);
            }
        }

        // also warn that this cannot be undone, and that it will possibly change
        // the underlying sequences
        int val = JOptionPane.showConfirmDialog(this,
                "<html>This action could possibly delete real sequence <BR>"
                        + "data and cannot be undone, do you wish to continue? </html>",
                "Question", JOptionPane.YES_NO_OPTION);

        if (val == JOptionPane.NO_OPTION) {
            return (false);
        }

        if (sels.length == 0) {
            if (getSelectedSequence() == -1) {
                return (false);
            }

            EditPanel p = (EditPanel) m_epanels.get(getSelectedSequence());
            SimpleEditableSequence s = p.getSequence();
            Location l = p.getRelativeSelection();

            if (l == null) {
                return (false);
            }

            int start = l.getMin();
            int stop = l.getMax();

            for (int j = start; j <= stop; ++j) {
                s.silentDelete(start);
            }
        } else {
            for (int sel : sels) {
                EditPanel p = (EditPanel) m_epanels.get(sel);
                SimpleEditableSequence s = p.getSequence();
                Location l = p.getRelativeSelection();

                int start = l.getMin();
                int stop = l.getMax();

                for (int j = start; j <= stop; ++j) {
                    s.silentDelete(start);
                }
            }
        }

        updateDifferenceLists();
        repaint();
        UndoHandler.getInstance().reset();
        UndoHandler.getInstance().setModified(true);

        return (true);
    }

    /**
     * Equalize the lengths of the sequences in the window
     */
    public void equalizeLengths() {
        FeaturedSequence[] seqs = getSequences();

        int mlen = 0;

        for (FeaturedSequence seq : seqs) {
            if (seq.length() > mlen) {
                mlen = seq.length();
            }
        }

        ProgressMonitor pm = new ProgressMonitor(null, "Equalizing Alignments", "", 0, seqs.length);

        for (int i = 0; i < seqs.length; ++i) {
            String add = getEqualizingString(mlen - seqs[i].length());

            pm.setMillisToDecideToPopup(1);
            pm.setProgress(i);

            seqs[i].silentInsertAtEnd(add);

            if (pm.isCanceled()) {
                pm.close();
                break;
            }
        }
        pm.close();
    }

    /**
     * Make the string that it required to equalize the current sequence
     */
    public String getEqualizingString(int length) {
        String str = "";

        for (int i = 0; i < length; i++) {
            str = str + '-';
        }

        return (str);
    }

    /**
     * this class method is used to identify the sequence location, used to re
     * arrange the clustal/tcoffee output before display
     *
     * @param seq is the FeaturedSequence
     * @return the position of the 'seq' in selected sequences
     */
    public int getSeqPosition(FeaturedSequence seq) {
        int pos = -1;
        FeaturedSequence[] preSeqs = getSelectedSequences();

        for (int i = 0; i < preSeqs.length; ++i) {
            if ((seq.getName()).equals((preSeqs[i]).getName())) {
                pos = i;
            }
        }

        return (pos);
    }

    public boolean sequencesAreAligned() {

        FeaturedSequence[] seqs = getSelectedSequences();

        for (int i = 0; i < ALREADY_ALIGNED; i++) {
            if (seqs[i].length() != seqs[0].length()) {

                return false;
            }

        }

        return true;
    }

    /**
     * Load the region from the sequence viewer, preprocess and call the appropriate
     * appserver to handle the request.
     *
     * @param type The type of alignment program
     * @return True or false based on success or failure of the operation
     */
    public boolean alignRegion(final String type) {
        if (Debug.isOn())
            System.out.println("> alignRegion(String)");
        System.out.println("In align region");
        int start = 0;
        int stop = 0;
        FeaturedSequence[] preSeqs = getSelectedSequences();

        for (FeaturedSequence i : preSeqs)
            System.out.println(i.toString());

        // make sure there are selections for the alignment
        if (preSeqs.length == 0) {
            UITools.showWarning(
                    "Mark sequences by clicking their names on the left, or by selecting 'Mark All Sequences' from the 'Edit' Menu.",
                    null);

            return (false);
        } else if (preSeqs.length == 1) {
            UITools.showWarning("You must mark two or more sequences to align.", null);

            return (false);
        }

        EditPanel p = m_epanels.get(m_selectedSequence);
        Location l = p.getRelativeSelection();

        if (l == null) {
            UITools.showWarning(
                    "Make a selection either by choosing the 'Select' Mouse Mode or the 'Select Whole Sequence' entry in the edit menu.",
                    null);

            return (false);
        } else {
            start = l.getMin();
            stop = l.getMax();
        }

        final FeaturedSequence[] seqs = preSeqs;
        for (FeaturedSequence i : seqs)
            System.out.println(i.toString());
        System.out.println("NUM SELECTED SEQUENCES:" + seqs.length + " SEQS 1: " + seqs[1].getName());
        final FeaturedSequence[] fseqs = new FeaturedSequence[seqs.length];
        String[] names = new String[seqs.length];

        final ArrayList list = new ArrayList();

        SimpleAlignmentDialog dialog = new SimpleAlignmentDialog(list);

        // setup and retreive the alignment

        if (DiffEditorFrame.mafftadd || DiffEditorFrame.mafftaddfrag) {

            if (!sequencesAreAligned()) {
                UITools.showWarning("You must first align a set of sequences", null);
                return false;
            }

            boolean valButt = UITools.showYesNo("Your new sequences will be aligned to the " + ALREADY_ALIGNED
                    + " already aligned sequences. Proceed?", (java.awt.Frame) getTopLevelAncestor());

            if (!valButt) {
                return (false);
            }
        }

        boolean rc = UITools.showYesNo("<html>Alignments are done through the VOCs server, not on your machine.<br>"
                + "You have chosen to align " + seqs.length + " sequences of " + (stop - start) + " bases.<br>"
                + "Are you sure you wish to continue?</html>", (java.awt.Frame) getTopLevelAncestor());

        if (!rc) {
            return (false);
        }

        final Vector<String> v = new Vector<String>();
        final HashMap<String, String> nameMap = new HashMap<>();

        // build vector of fastas of sequences for the alignment util
        for (int i = 0; i < seqs.length; ++i) {
            // clustal munges names, so keep track of the name to sequence match.
            names[i] = new String(seqs[i].getName());
            names[i] = names[i].replace(' ', '_');
            names[i] = names[i].replace('(', '_');
            names[i] = names[i].replace(')', '_');
            names[i] = names[i].replace(':', '_');
            names[i] = names[i].replace(';', '_');
            names[i] = names[i].replace('&', '_');
            names[i] = names[i].replace('"', '_');
            names[i] = names[i].replace('\'', '_');
            names[i] = names[i].replace(',', '_');

            // clustal will give 27 characters of name space. to uniquely identify,
            // take the end and beginning of the sequence name
            if (names[i].length() > 27) {
                names[i] = names[i].substring(0, 17) + "-"
                        + names[i].substring(names[i].length() - 9, names[i].length());
            }

            nameMap.put(i + "_" + names[i], seqs[i].getName());

            v.addElement(">" + i + ";" + names[i]);

            // caity getSelectedValues has been deprecated - switch to getSelectedValuesList
            List<EditPanel> eplist = m_hList.getSelectedValuesList();
            for (EditPanel ep : eplist) {
                EditableSequence s = ep.getSequence();
                if (s == seqs[i]) {
                    Location ll = ep.getRelativeSelection();
                    start = ll.getMin();
                    stop = ll.getMax();
                    break;
                }
            }
            StringBuffer b = new StringBuffer();

            if (type.equals("mafft")) {
                for (int j = start; j <= stop; ++j) {
                    b.append(seqs[i].charAt(j));
                }
            } else {
                for (int j = start; j <= stop; ++j) {
                    if (seqs[i].charAt(j) != '-') {
                        b.append(seqs[i].charAt(j));
                    }
                }

            }
            v.addElement(b.toString());

        }

        for (String i : v)
            System.out.println(i);

        // db initialize
        final AppServerAccess datasource = AppServerAccess.getInstance();
        final StringBuffer res = new StringBuffer();

        Runnable r = new Runnable() {
            public void run() {
                Vector ret = null;

                try {
                    if (type.equals("mafft") && (DiffEditorFrame.mafftadd || DiffEditorFrame.mafftaddfrag)) {
                        ret = AppServer2Access.doAlignmentQuery(type, v, ALREADY_ALIGNED);
                    } else if (type.equals("mafft")) {
                        ret = AppServer2Access.doAlignmentQuery(type, v);
                    } else {
                        ret = datasource.doAlignmentQuery(type, v);
                    }
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                }
                if (ret == null || ret.size() == 0) {
                    UITools.showError("There was an error in returning your alignment.", null);
                    System.out.println(
                            "Null alignment vector returned, there was some problem in running your alignment");
                    return;
                }

                for (Object aRet : ret) {
                    if (type.equals("mafft") && (DiffEditorFrame.mafftadd || DiffEditorFrame.mafftaddfrag)) {
                        DiffEditorFrame.seqsFromMafftAdd.add(aRet);
                    }
                    res.append(aRet).append("\n");
                    System.out.println("NUM OBJECTS returned from query: " + aRet.toString());
                }

                TextFileFeaturedSequenceReader tffsr;

                tffsr = new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.CLUSTAL_FORMAT,
                        res.toString());

                try {
                    ListIterator it = tffsr.getSequences();

                    int numSeqs = 0;
                    while (it.hasNext()) {

                        FeaturedSequence fs = (FeaturedSequence) it.next();
                        System.out.println(fs.toString());
                        String resultName = fs.getName();

                        fs.setName((String) nameMap.get(resultName));
                        list.add(fs);
                        fseqs[numSeqs] = fs;
                        System.out.println("fseqs[" + numSeqs + "] = " + fs.getName());
                        numSeqs++;
                    }
                    System.out.println("seqs.length: " + seqs.length + " NUM SEQS: " + numSeqs);
                    newList.addAll(Arrays.asList(fseqs).subList(0, seqs.length));
                } catch (java.io.IOException ioex) {
                    ioex.printStackTrace();
                }
            }

        };

        try {
            UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) getTopLevelAncestor(), r,
                    "Retrieving Alignment").join();
            // r.run(); // makes it work with eGATU
            dialog.setSequences(newList);
            dialog.setBounds(100, 100, 500, 300);
            dialog.setVisible(true);
            dialog.setSequences(list);
        } catch (Exception ex) {
            ex.printStackTrace();
            return (false);
        }

        boolean warn = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("warn.onAlign"));

        // display the alignment with ok/cancel option to propogate the alignment
        if (dialog.getApproval()) {
            if (warn) {
                int val = JOptionPane.showConfirmDialog((java.awt.Frame) getTopLevelAncestor(),
                        "This action cannot be undone. Implement these changes?", "Accept Alignment",
                        JOptionPane.YES_NO_OPTION);

                if (val == JOptionPane.NO_OPTION) {
                    newList.clear();

                    return (false);
                }
            }

            if (!DiffEditorFrame.mafftadd)
                ALREADY_ALIGNED = seqs.length;
            final ListIterator it = dialog.getSequences();
            final String[] rNames = names;
            Runnable r2d2 = new Runnable() {
                public void run() {
                    boolean[] done = new boolean[seqs.length];

                    while (it.hasNext()) {
                        // the aligned version of this sequence
                        FeaturedSequence in = (FeaturedSequence) it.next();
                        String inName = in.getName();
                        inName = inName.replace(' ', '_');
                        inName = inName.replace('(', '_');
                        inName = inName.replace(')', '_');
                        inName = inName.replace(':', '_');
                        inName = inName.replace(',', '_');
                        inName = inName.replace(';', '_');
                        inName = inName.replace('&', '_');
                        inName = inName.replace('"', '_');
                        inName = inName.replace('\'', '_');

                        for (int i = 0; i < seqs.length; ++i) {
                            int rNameLength = rNames[i].length();
                            boolean matchedSeq = false;

                            if (rNameLength < 27) {
                                if (inName.equalsIgnoreCase(rNames[i])) {
                                    matchedSeq = true;
                                }
                            } else if (rNameLength == 27) {
                                String firstSub = rNames[i].substring(0, 17);
                                String secondSub = rNames[i].substring(18, 27);
                                int firstIdx = inName.indexOf(firstSub);
                                int secondIdx = inName.indexOf(secondSub);

                                if (firstIdx >= 0 && secondIdx >= firstIdx) {
                                    matchedSeq = true;
                                }
                            } else {
                                System.out.println(
                                        "There was an error setting up the names of sequences when exporting.");
                            }

                            if (matchedSeq && !done[i]) {
                                int start = 0, stop = 0;
                                // caity - getSelectedValues() is deprecated -> switch to
                                List<EditPanel> eplist = m_hList.getSelectedValuesList();
                                for (EditPanel ep : eplist) {
                                    EditableSequence s = ep.getSequence();
                                    if (s == seqs[i]) {
                                        Location ll = ep.getRelativeSelection();
                                        start = ll.getMin();
                                        stop = ll.getMax();
                                        break;
                                    }
                                }
                                seqs[i].delete(start, stop + 1);
                                seqs[i].insert(start, in.toString());
                                done[i] = true;

                                break;
                            }
                        }
                    }

                    updateDifferenceLists();
                    refreshState();
                    UndoHandler.getInstance().reset();
                    UndoHandler.getInstance().setModified(true);
                }
            };

            try {
                UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) getTopLevelAncestor(), r2d2,
                        "Applying Alignment");
            } catch (Exception ex) {
                UITools.showError("Error updating alignment into sequence", null);
                ex.printStackTrace();
            }
        }

        newList.clear();

        if (Debug.isOn())
            System.out.println("< alignRegion(String)");
        return (true);
    }

    /*
     * Same method as above, but does not prompt user so that it may be used in the
     * codehop code. Has changes since the method above gets the selection from what
     * the user has highlighted. Here the program just takes everything regardless.
     */

    public boolean alignSequencesForCodehop(final String type) {
        if (Debug.isOn())
            System.out.println("> alignRegion(String)");
        int start = 0;
        int stop = 0;
        FeaturedSequence[] preSeqs = getAllSequences();

        final FeaturedSequence[] seqs = preSeqs;
        final FeaturedSequence[] fseqs = new FeaturedSequence[seqs.length];

        String[] names = new String[seqs.length];

        final ArrayList list = new ArrayList();

        SimpleAlignmentDialog dialog = new SimpleAlignmentDialog(list);

        final Vector<String> v = new Vector<String>();
        final HashMap<String, String> nameMap = new HashMap<String, String>();

        ListModel lm = m_hList.getModel();

        // build vector of fastas of sequences for the alignment util

        for (int i = 0; i < seqs.length; ++i) {
            // clustal munges names, so keep track of the name to sequence match.
            names[i] = new String(seqs[i].getName());
            names[i] = names[i].replace(' ', '_');
            names[i] = names[i].replace('(', '_');
            names[i] = names[i].replace(')', '_');
            names[i] = names[i].replace(':', '_');
            names[i] = names[i].replace(';', '_');
            names[i] = names[i].replace('&', '_');
            names[i] = names[i].replace('"', '_');
            names[i] = names[i].replace('\'', '_');
            names[i] = names[i].replace(',', '_');

            // clustal will give 27 characters of name space. to uniquely identify,
            // take the end and beginning of the sequence name
            if (names[i].length() > 27) {
                names[i] = names[i].substring(0, 17) + "-"
                        + names[i].substring(names[i].length() - 9, names[i].length());
            }

            nameMap.put(i + "_" + names[i], seqs[i].getName());

            v.addElement(">" + i + ";" + names[i]);
            EditPanel ep = (EditPanel) lm.getElementAt(i);
            v.addElement(ep.m_seq.toString());

        }

        // db initialize
        final AppServerAccess datasource = AppServerAccess.getInstance();
        final StringBuffer res = new StringBuffer();

        Vector ret = null;

        try {
            if (type.equals("mafft")) {
                ret = AppServer2Access.doAlignmentQuery(type, v);
            } else {
                ret = datasource.doAlignmentQuery(type, v);
            }
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        if (ret == null || ret.size() == 0) {
            UITools.showError("There was an error in returning your alignment.", null);
            System.out.println("Null alignment vector returned, there was some problem in running your alignment");
            return false;
        }

        for (Object aRet : ret) {
            res.append(aRet).append("\n");

        }

        TextFileFeaturedSequenceReader tffsr = new TextFileFeaturedSequenceReader(
                TextFileFeaturedSequenceReader.CLUSTAL_FORMAT, res.toString());

        try {
            ListIterator it = tffsr.getSequences();
            int numSeqs = 0;
            while (it.hasNext()) {

                FeaturedSequence fs = (FeaturedSequence) it.next();
                String resultName = fs.getName();

                fs.setName((String) nameMap.get(resultName));
                list.add(fs);
                fseqs[numSeqs] = fs;
                numSeqs++;
            }

            newList.addAll(Arrays.asList(fseqs).subList(0, seqs.length));
        } catch (java.io.IOException ioex) {
            ioex.printStackTrace();
        }

        final ListIterator it = dialog.getSequences();
        final String[] rNames = names;

        boolean[] done = new boolean[seqs.length];
        int loc = 0;
        while (it.hasNext()) {
            // the aligned version of this sequence
            FeaturedSequence in = (FeaturedSequence) it.next();
            String inName = in.getName();
            inName = inName.replace(' ', '_');
            inName = inName.replace('(', '_');
            inName = inName.replace(')', '_');
            inName = inName.replace(':', '_');
            inName = inName.replace(';', '_');
            inName = inName.replace('&', '_');
            inName = inName.replace('"', '_');
            inName = inName.replace('\'', '_');

            seqs[loc].setSequenceText(in.toString());
            loc += 1;

        }
        ALREADY_ALIGNED = seqs.length;
        updateDifferenceLists();
        refreshState();
        UndoHandler.getInstance().reset();
        UndoHandler.getInstance().setModified(true);

        newList.clear();

        if (Debug.isOn())
            System.out.println("< alignRegion(String)");
        return (true);
    }

    /**
     * This prompts for a comment. If one is given, this comment is added as a
     * feature to the underlying sequence to the selected panel
     *
     * @return true if succeeded
     */
    protected boolean addUserCommentToSelection() {
        if (m_mouseMode == SequenceDisplay.EDIT_MODE) {
            UITools.showWarning("You cannot perform this operation in EDIT mode. Please switch to SELECT mode,"
                    + "\nand select the region where primer should be added", this);

            return (false);
        }

        EditPanel p = null;
        FeaturedSequence[] seqs = getSelectedSequences();
        Location[] l = new Location[seqs.length];
        StrandedFeature.Strand s = null;

        if (seqs.length == 0) {
            UITools.showWarning("Please select sequence(s).", this);

            return (false);
        }

        for (int i = 0; i < seqs.length; i++) {
            for (int j = 0; j < m_epanels.size(); ++j) {
                if ((m_epanels.get(j)).m_seq.getName() == seqs[i].getName()) {
                    p = m_epanels.get(j);

                    l[i] = p.getAbsoluteSelection();
                    if (l[i].getMin() == l[i].getMax() && l[i].getMin() == 0) {
                        UITools.showWarning("Please select a region to annotate.", this);
                        return (false);
                    }

                    // Check to see if a comment starts or ends in the gap. If it is then show a warning.
                    Location relativeLocation = p.getRelativeSelection();
                    if (seqs[i].charAt(relativeLocation.getMin()) == '-'
                            || seqs[i].charAt(relativeLocation.getMax()) == '-') {
                        UITools.showWarning(
                                "You may not create a comment that starts or ends within a gap in the sequence.", this);
                        return (false);
                    }
                }
            }
        }

        CommentEditorPane pane = new CommentEditorPane();
        pane.setCommentBackground(Color.cyan);

        int val = JOptionPane.showConfirmDialog(this, pane, "Add Comment to Selection", JOptionPane.OK_CANCEL_OPTION);
        boolean ret = true;

        if (val == JOptionPane.OK_OPTION) {
            if (pane.getName().equals(" ") || pane.getName().length() == 0) {
                UITools.showWarning("You have entered invalid comment name.", this);

                return (false);
            }
            for (int i = 0; i < seqs.length; i++) {
                for (int j = 0; j < m_epanels.size(); ++j) {
                    if ((m_epanels.get(j)).m_seq.getName() == seqs[i].getName()) {
                        p = m_epanels.get(j);
                        s = p.getDisplayStrand();
                        boolean r = FeatureTools.createUserComment(seqs[i], l[i], s, pane.getName(), pane.getComment(),
                                pane.getCommentForeground(), pane.getCommentBackground());
                        ret = (ret && r);
                        p.revalidate();
                        p.resetView();
                    }
                }
            }
            refreshState();
            return (ret);
        }

        System.out.println("s=" + s);
        return (true);
    }

    public void addCommentNoUI(String comm, int loc, int top, char letter, Color commentColor) {
        int start = loc + 1;
        int stop = loc + 7;
        int offset = 0;
        EditPanel p = null;
        Location l = null;
        FeaturedSequence seq = m_epanels.get(m_epanels.size() - 1).getSequence();
        StrandedFeature.Strand s = null;

        for (int i = 0; i <= loc; i++) {
            if (seq.charAt(i) == '-') {
                offset += 1;
            }
        }

        start -= offset;
        stop -= offset;
        Location actualloc = new RangeLocation(start, stop);

        p = m_epanels.get(m_epanels.size() - 1);
        s = p.getDisplayStrand();

        boolean r = FeatureTools.createUserComment(seq, actualloc, s, letter + " " + (loc + 1), comm, Color.BLACK,
                commentColor);

        p.revalidate();
        p.resetView();
        refreshState();
    }

    /**
     * Analysis result is added as feature to the underlying sequence to the
     * selected panel
     *
     * @return true if succeeded
     *         <p>
     *         author Sangeeta Neti
     */
    protected boolean addAnalysisResultToSelection(GenericInput gi) {

        EditPanel p = null;
        p = m_epanels.get(m_selectedSequence);
        boolean ret = true;
        FeaturedSequence[] seq = getSelectedSequences();

        for (Iterator j = gi.analysisIterator(); j.hasNext();) {
            GenericInput.Analysis ann = (GenericInput.Analysis) j.next();
            String analysisName = ann.getName();

            for (Iterator i = ann.regionIterator(); i.hasNext();) {

                GenericInput.Region r = (GenericInput.Region) i.next();

                Location l = r.getLocation();
                StrandedFeature.Strand s = r.getStrand();
                String name = r.getName();

                ret = FeatureTools.createUserComment(seq[0], l, s, name, analysisName, Color.black, r.getColor());
            }
        }
        p.revalidate();
        p.resetView();
        refreshState();

        return (ret);
    }

    /**
     * This class method loads a primer, here all the values of the primer are
     * provided This is used with a PRIMER is LOADED from FILE
     * <p>
     * primerSeq The primer sequence primerName is the primer name fridge_1 is the
     * fridge location fridge_2 is the second fridge location temp is the
     * temperature comments are user comments for this primer
     * <p>
     * author asyed
     */
    public void loadPrimerWithValues(final int mm_percent, final Vector allLines) {
        FeaturedSequence[] seqs = getSelectedSequences();
        EditPanel p = m_epanels.get(m_selectedSequence);

        if (seqs.length == 0 || seqs.length > 1) {
            UITools.showWarning("Please select ONE sequence to which primers are to be added.", this);

            return;
        }

        Thread t = new Thread() {
            public void run() {
                for (Object allLine : allLines) {
                    Vector v = (Vector) allLine;
                    String primerSeq = (String) v.get(0);
                    m_primerName = (String) v.get(1);

                    String fridge1 = (String) v.get(2);
                    String fridge2 = (String) v.get(3);
                    String temp = (String) v.get(4);
                    String comments = (String) v.get(5);
                    String fridge = null;

                    if (fridge1 != null && fridge2 != null) {
                        fridge = fridge1 + "(Location1) / " + fridge2 + "(Location2)";
                    } else if (fridge1 != null) {
                        fridge = fridge1;
                    } else {
                        fridge = "null";
                    }

                    String[] info = new String[] { m_primerName, comments, fridge, temp };

                    if (!searchSelectedSequences(SearchTools.FUZZY, primerSeq, (mm_percent * primerSeq.length()) / 100,
                            true, true, info)) {
                        System.out.println("Search failed");
                        JOptionPane.showMessageDialog(null, "Primer Search", "No Primers found for " + primerSeq,
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                    try {
                        sleep(400);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    while (isSearchActive()) {
                        try {
                            sleep(400);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (isSearchAborted()) {
                        return;
                    }
                }
            }
        };
        t.start();

        // redraw the alignment to make sure it shows
        p.revalidate();
        p.resetView();
        refreshState();
        p.repaint();
    }

    /**
     * This class method loads a primer, here all the values of the primer are
     * provided This is used with a PRIMER is LOADED from FILE
     * <p>
     * primerSeq The primer sequence primerName is the primer name fridge_1 is the
     * fridge location fridge_2 is the second fridge location temp is the
     * temperature comments are user comments for this primer
     *
     * @author asyed
     */

    public void savePrimersToFile() {
        try {
            FeaturedSequence[] seqs = getSelectedSequences();
            PrintWriter p = null;

            if (seqs.length != 1) {
                UITools.showWarning("You may have selected no sequence/more than one Sequences" + "\n" + "Please select ONLY the sequence whose PRIMERs you would like to save", null);
                return;
            }

            Iterator it_features = seqs[0].features();

            StringBuilder fileContents = new StringBuilder();

            while (it_features.hasNext()) {
                Feature ft = (Feature) it_features.next();

                if (ft.getType().equals("PRIMER")) {
                    String sequence = (String) ft.getAnnotation().getProperty(AnnotationKeys.PRIMER_SEQ);
                    String name = (String) ft.getAnnotation().getProperty(AnnotationKeys.NAME);
                    String temp = (String) ft.getAnnotation().getProperty(AnnotationKeys.PRIMER_MELTINGTEMP);
                    String fridge_one = (String) ft.getAnnotation().getProperty(AnnotationKeys.PRIMER_FRIDGE);
                    String comment = (String) ft.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT);
                    String delim = '\t' + "";

                    // CHANGE FRIDGE TWO LATER
                    String fridge_two = "null";

                    if (fridge_one.equals("")) {
                        fridge_one = "null";
                    }

                    if (comment.equals("")) {
                        comment = "null";
                    }

                    if (fridge_two.equals("")) {
                        fridge_two = "null";
                    }

                    fileContents.append(sequence);
                    fileContents.append(delim);
                    fileContents.append(name);
                    fileContents.append(delim);
                    fileContents.append(fridge_one);
                    fileContents.append(delim);
                    fileContents.append(fridge_two);
                    fileContents.append(delim);
                    fileContents.append(temp);
                    fileContents.append(delim);
                    fileContents.append(comment);
                    fileContents.append("\n");
                }
            }

            if (fileContents.length() == 0) {
                UITools.showWarning("Sequence does not contain any primers.", null);
            } else {
                JFileChooser jfc = new JFileChooser();

                int saveDialogStatus = jfc.showSaveDialog(PrimaryPanel.this);

                if (saveDialogStatus == JFileChooser.APPROVE_OPTION) {
                    File f = jfc.getSelectedFile();

                    p = new PrintWriter(new java.io.FileOutputStream(f));

                    p.println(fileContents.toString().trim());

                    p.close();

                    ((DiffEditorFrame) PrimaryPanel.this.getTopLevelAncestor()).m_status.setText("Primers Saved Successfully");
                }
            }
        } catch (Exception e) {
            System.out.println("Error Occured While Saving Primers To File. .");
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     */
    public void keywordSearch(String type) {
        FeaturedSequence seq = null;
        FeaturedSequence[] seqs = getSelectedSequences();
        boolean keyword_found = false;

        if (seqs.length == 0) {
            UITools.showWarning("Please select sequence before performing keyword search.", this);

            return;
        }

        if (seqs.length > 1) {
            UITools.showWarning(
                    "Current version of BBB for keyword search, supports one sequence at a time.\n Please select only one sequence and perform keyword search.",
                    this);

            return;
        }

        KeywordSearchPane sp = new KeywordSearchPane();
        int val = JOptionPane.showConfirmDialog(this, sp, "Keyword Search Panel", JOptionPane.OK_CANCEL_OPTION);

        String keywords = null;

        if (val == JOptionPane.OK_OPTION) {
            keywords = sp.getComment();

            if (keywords == null || keywords.length() == 0 || keywords.equals(" ")) {
                UITools.showWarning("Search cannot be performed for your search criteria", this);

                return;
            }

            seq = seqs[0];

            Iterator it = seq.features();
            StringTokenizer st = new StringTokenizer(keywords, ";");
            int num_Keywords = st.countTokens();
            String[] tokens = new String[st.countTokens()];
            int i = 0;

            while (st.hasMoreTokens()) {
                tokens[i] = st.nextToken();
                i++;
            }

            Vector<Location> v_loc = new Vector<Location>();
            Vector<String> v_name = new Vector<String>();
            Vector<String> v_comments = new Vector<String>();
            Vector<String> v_strand = new Vector<String>();

            while (it.hasNext()) {
                i = 0;

                StrandedFeature ft = (StrandedFeature) it.next();
                Location loc = ft.getLocation();

                if (ft.getType().equals(type)) {
                    // for each keyword
                    while (i < num_Keywords) {
                        String key = tokens[i].trim(); // Pattern to be searched

                        String comment = (String) ft.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT);
                        String feature_name = (String) ft.getAnnotation().getProperty(AnnotationKeys.NAME);
                        String t_comment = comment.toLowerCase();
                        String t_key = key.toLowerCase();
                        String t_primer_name = feature_name.toLowerCase();
                        Pattern pat = Pattern.compile(t_key);
                        Matcher inComment = pat.matcher(t_comment);
                        Matcher inName = pat.matcher(t_primer_name);

                        if (inComment.find() || inName.find()) {
                            String strand = ft.getStrand().toString();
                            String organism = seqs[0].getName();
                            String str = key + ";" + organism + ";" + feature_name;
                            v_loc.add(loc);
                            v_name.add(str);
                            v_comments.add(comment);
                            v_strand.add(strand);
                            keyword_found = true;
                        }

                        i++;
                    } // has more
                } // if feature
            } // while has next feature

            if (!keyword_found) {
                UITools.showWarning("No Primers found with keywords:\n" + keywords, null);
            } else {
                new KeywordSearchResultsPane(this, v_loc, v_name, v_comments, v_strand, keywords, true);
            }
        } // OK
    }

    /**
     * This prompts for a primer. If one is given, this primer is added as a feature
     * to the underlying sequence to the selected panel
     * <p>
     * Primers should always be literal 5` -->3` and only reversed for rendering
     *
     * @return true if succeeded author Neil Hillen
     */
    protected boolean addPrimerToSelection() {
        if (m_mouseMode == SequenceDisplay.EDIT_MODE) {
            UITools.showWarning(
                    "You cannot perform this operation in EDIT mode. Please switch to SELECT mode, \nand select the region where primer should be added",
                    this);

            return (false);
        }

        EditPanel p = null;
        Location l = null;
        FeaturedSequence seq = null;
        StrandedFeature.Strand s = null;
        FeaturedSequence[] seqs = getSelectedSequences();
        String segseq;

        if (seqs.length == 0) {
            UITools.showWarning("Please select sequence(s).", this);

            return (false);
        }

        if (seqs.length > 1) {
            String p_str = "You have selected more than one sequence. A PRIMER sequence (matching with last instance of selection)"
                    + "\n" + "will be added to all sequences, do you wish to contine?";
            int opt = JOptionPane.showConfirmDialog(null, p_str, "Primer For Multiple Sequences",
                    JOptionPane.YES_NO_OPTION);

            if (!(opt == JOptionPane.YES_OPTION)) {
                return (false);
            }
        }

        if (m_selectedSequence >= 0) {
            p = (EditPanel) m_epanels.get(m_selectedSequence);
            s = p.getDisplayStrand();
            seq = p.getSequence();
            l = p.getAbsoluteSelection();
        } else {
            UITools.showWarning("Please select a region to annotate.", this);

            return (false);
        }

        PrimerEditorPane pane = new PrimerEditorPane();

        int min = seq.getRelativePosition(l.getMin());
        int max = seq.getRelativePosition(l.getMax() + 1);

        // Check which strand you have
        if (getDisplayStrand() == StrandedFeature.NEGATIVE) {
            String primseq = ca.virology.lib.io.tools.SequenceTools.getDNAComplement(seq.substring(min, max));
            String fiveToThreeSeq = new StringBuffer().append(primseq).reverse().toString();
            pane.setSegmentSequence(fiveToThreeSeq);

            String primTemp = "" + getPrimerTemp(primseq);
            pane.setMeltingTemp(primTemp);
        } else {
            pane.setSegmentSequence(seq.substring(min, max));
            segseq = "" + getPrimerTemp(seq.substring(min, max));
            pane.setMeltingTemp(segseq);
        }

        // Prompt user for some more information
        int val = JOptionPane.showConfirmDialog(this, pane, "Add Primer to Selection", JOptionPane.OK_CANCEL_OPTION);

        if (val == JOptionPane.OK_OPTION) {
            String name = pane.getName();
            segseq = pane.getSeqmentSequence();

            String melting = pane.getMeltingTemp();
            String fridge = pane.getFridgeLocation();
            String comment = pane.getComment();
            segseq = segseq.toUpperCase();

            boolean ret2 = true;

            for (FeaturedSequence seq1 : seqs) {
                ret2 = ret2 && FeatureTools.createPrimerFeature(seq1, l, s, name, segseq, melting, fridge, comment);
            }

            // redraw the alignment to make sure it shows
            p.revalidate();
            p.resetView();
            refreshState();
            p.repaint();

            return (ret2);
        }

        return (true);
    }

    /**
     * Add a feature to the underlying sequence currently selected in the position
     * selected.
     *
     * @return true if it works out
     */
    protected boolean addTransposeEventToSelection() {
        String diffType = ca.virology.lib.io.sequenceData.DifferenceType.TRANSPOSITION;

        if (m_selectedSequence == -1) {
            return (false);
        }

        EditPanel p = (EditPanel) m_epanels.get(m_selectedSequence);
        FeaturedSequence comp = null;

        if ((m_selectedSequence + 1) < m_epanels.size()) {
            comp = ((EditPanel) m_epanels.get(m_selectedSequence + 1)).getSequence();
        }

        if (p == null) {
            return (false);
        }

        FeaturedSequence seq = p.getSequence();
        Location l = p.getAbsoluteSelection();

        if (l == null) {
            return (false);
        }

        boolean ret = FeatureTools.createDifferenceFeature(seq, comp, diffType, l, FeatureType.USER_GENERATED);
        p.resetView();

        return (ret);
    }

    /**
     * Set the mouse mode for this window
     *
     * @param mode The mode to set the window to, Must be one of
     *             <CODE>EditPanel.SELECT_MODE</CODE> or
     *             <CODE>EditPanel.SELECT_MODE</CODE>
     */
    public void setMouseMode(int mode) {
        m_mouseMode = mode;
    }

    /**
     * Get the mouse mode for this window
     *
     * @return the mouse mode
     */
    public int getMouseMode() {
        return (m_mouseMode);
    }

    /**
     * Move the selected sequences in the specified way
     *
     * @param move The direction to move<BR>
     *             <B>Note:</B> This direction must be one of:<BR>
     *             <CODE>SwingConstants.TOP</CODE><BR>
     *             <CODE>SwingConstants.BOTTOM</CODE><BR>
     *             <CODE>SwingConstants.NEXT</CODE><BR>
     *             <CODE>SwingConstants.PREVIOUS</CODE><BR>
     */
    public synchronized void moveSelectedSequences(int move) {
        FeaturedSequence[] seqs = getSelectedSequences();

        if (seqs.length > 0) {
            if ((move == SwingConstants.TOP) || (move == SwingConstants.PREVIOUS)) {
                for (FeaturedSequence seq : seqs) {
                    moveSequence(seq, move);
                }
            } else if ((move == SwingConstants.BOTTOM) || (move == SwingConstants.NEXT)) {
                for (int i = seqs.length - 1; i >= 0; --i) {
                    moveSequence(seqs[i], move);
                }
            }

            m_hList.clearSelection();
        }

        int[] newIndices = new int[seqs.length];

        for (int i = 0; i < seqs.length; ++i) {
            EditPanel p = null;
            int j = 0;

            for (j = 0; j < m_epanels.size(); ++j) {
                p = m_epanels.get(j);

                if (p.getSequence() == seqs[i]) {
                    break;
                }
            }

            if (j == m_epanels.size()) {
                p = null;
            }

            newIndices[i] = m_headListModel.indexOf(p);
        }

        m_hList.setSelectedIndices(newIndices);
    }

    /**
     * Removes the selected sequences from the window
     */
    public synchronized void removeSelectedSequences() {
        FeaturedSequence[] seqs = getSelectedSequences();

        if (seqs.length > 0) {
            for (FeaturedSequence seq : seqs) {
                removeSequence(seq);
            }
        }

        refreshEditors();
        updateDifferenceLists();
        refreshState();
    }

    /**
     * Removes all columns which contain gaps in all sequences
     */
    public synchronized void removeAllGapColumns() {
        FeaturedSequence[] seqs = getSequences();

        if (seqs.length <= 0) {
            return;
        }

        // find min seq length
        int minSeqLen = 0;
        boolean foundOnce = false;

        for (FeaturedSequence seq : seqs) {
            if (foundOnce) {
                if (seq.length() < minSeqLen) {
                    minSeqLen = seq.length();
                }
            } else {
                minSeqLen = seq.length();
                foundOnce = true;
            }
        }

        // go through each column and see if all sequences have gaps
        for (int i = 0; i < minSeqLen; i++) {
            int j;

            for (j = 0; j < seqs.length; j++) {
                if (seqs[j].charAt(i) != '-') {
                    break;
                }
            }

            // if we found a non-gap char in a seq at this position, move to the next position
            if (j != seqs.length) {
                continue;
            }

            // remove gaps from each sequence
            for (j = 0; j < seqs.length; j++) {
                seqs[j].silentDelete(i);
            }

            // account for deleted column
            i--;
            minSeqLen--;
        }

        refreshEditors();
        updateDifferenceLists();
        refreshState();
    }

    /**
     * Removes all instances of a given feature type from the selected sequences
     *
     * @param type the type of feature to remove
     */
    public synchronized void removeFeaturesFromSelection(String type) {
        FeaturedSequence[] seqs = getSelectedSequences();

        setProcessing(true);

        for (FeaturedSequence seq : seqs) {
            FeatureTools.removeFeatures(seq, type);
        }

        setProcessing(false);

        for (EditPanel m_epanel : m_epanels) {
            EditPanel ep = (EditPanel) m_epanel;
            ep.resetView();
        }

        refreshState();
    }

    /**
     * Removes all instances of a given feature type from the visible sequences
     *
     * @param type the type of feature to remove
     */
    public synchronized void removeFeaturesFromVisible(String type) {
        FeaturedSequence[] seqs = getVisibleSequences();

        setProcessing(true);

        for (FeaturedSequence seq : seqs) {
            FeatureTools.removeFeatures(seq, type);
        }

        setProcessing(false);

        for (EditPanel m_epanel : m_epanels) {
            EditPanel ep = (EditPanel) m_epanel;
            ep.resetView();
        }

        refreshState();
    }

    /**
     * move the specified sequence the specified direction in the window
     *
     * @param seq  The sequence to move
     * @param move The direction to move<BR>
     *             <B>Note:</B> This direction must be one of:<BR>
     *             <CODE>SwingConstants.TOP</CODE><BR>
     *             <CODE>SwingConstants.BOTTOM</CODE><BR>
     *             <CODE>SwingConstants.NEXT</CODE><BR>
     *             <CODE>SwingConstants.PREVIOUS</CODE><BR>
     * @return The <CODE>EditPanel</CODE> affected by this operation
     */
    public synchronized EditPanel moveSequence(final FeaturedSequence seq, final int move) {
        EditPanel ep = null;

        for (int i = 0; i < m_headListModel.size(); ++i) {
            EditPanel editor = (EditPanel) m_headListModel.get(i);

            if (editor.getSequence() == seq) {
                ep = editor;

                break;
            }
        }

        if (ep == null) {
            return (null);
        }

        final EditPanel editor = ep;
        Runnable r = new Runnable() {
            public void run() {
                int index = m_headListModel.indexOf(editor);

                if (index == -1) {
                    return;
                } else if ((index == 0) && ((move == SwingConstants.PREVIOUS) || (move == SwingConstants.TOP))) {
                    return;
                } else if ((index == (m_headListModel.size() - 1))
                        && ((move == SwingConstants.NEXT) || (move == SwingConstants.BOTTOM))) {
                    return;
                } else {
                    removeSequence(seq);

                    if (move == SwingConstants.BOTTOM) {
                        m_epanels.add(editor);
                        m_headListModel.addElement(editor);
                        m_editContainer.add(editor);
                    } else if (move == SwingConstants.TOP) {
                        m_epanels.add(0, editor);
                        m_headListModel.add(0, editor);
                        m_editContainer.add(editor, 0);
                    } else if (move == SwingConstants.NEXT) {
                        m_epanels.add(index + 1, editor);
                        m_headListModel.add(index + 1, editor);
                        m_editContainer.add(editor, index + 1);
                    } else if (move == SwingConstants.PREVIOUS) {
                        m_epanels.add(index - 1, editor);
                        m_headListModel.add(index - 1, editor);
                        m_editContainer.add(editor, index - 1);
                    }
                }
            }
        };

        try {
            UITools.invoke(r);
        } catch (Exception ex) {
        }

        refreshEditors();
        updateDifferenceLists();
        refreshState();

        return (ep);
    }

    /**
     * Remove the given sequence from the panel<BR>
     * <B>NOT IMPLEMENTED</B>
     *
     * @param seq The sequence to remove
     * @return The <CODE>EditPanel</CODE> removed from the window
     */
    public synchronized EditPanel removeSequence(FeaturedSequence seq) {
        EditPanel ep = null;

        for (EditPanel m_epanel : m_epanels) {

            if (m_epanel.getSequence() == seq) {
                ep = m_epanel;

                break;
            }
        }

        if (ep == null) {
            return (null);
        }

        final EditPanel editor = ep;
        Runnable r = new Runnable() {
            public void run() {
                m_epanels.remove(editor);
                m_headListModel.removeElement(editor);
                m_editContainer.remove(editor);

                if (getSequences().length == 0) {
                    closeOverviewFrame();
                }
            }
        };

        try {
            UITools.invoke(r);
        } catch (Exception ex) {
        }

        return (ep);
    }

    /**
     * remove all sequences from this panel. This is like a 'close' operation for
     * this class. Sequences can be further added and edited later.
     */
    public void removeAllSequences() {
        UndoHandler.getInstance().reset();
        m_epanels.removeAll(new ArrayList<EditPanel>(m_epanels));
        m_hpanels.removeAll(new ArrayList<HeaderPanel>(m_hpanels));

        try {
            UITools.invoke(new Runnable() {
                public void run() {
                    m_editContainer.removeAll();
                    m_headListModel.removeAllElements();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        closeOverviewFrame();
    }

    /**
     * Scroll the panel to the given location
     *
     * @param location The location to scroll to
     */
    public void scrollToLocation(int location) {
        scrollToLocation(0, location, true);
    }

    /**
     * scroll the panel so that the location given is in the middle of the view
     *
     * @param location The location to scroll to
     */
    public void scrollCenterToLocation(int location) {
        if (m_epanels.size() == 0) {
            return;
        }

        EditPanel p = (EditPanel) m_epanels.get(0);
        int grpos = p.sequenceToGraphics(location);
        int width = m_scroller.getViewport().getExtentSize().width;
        int x = grpos - (width / 2);
        location = p.graphicsToSequence(x);
        m_scroller.getHorizontalScrollBar().setValue(location);
    }

    /**
     * Get the current position at the center hand side of the view
     *
     * @return the position at the center of the screen
     */
    public int getCenterPosition() {
        if (m_epanels.size() == 0) {
            return (0);
        }

        int xpos = m_scroller.getHorizontalScrollBar().getValue();
        System.out.println("xpos: " + xpos);
        xpos += (m_scroller.getViewport().getExtentSize().width / 2);
        System.out.println("Viewport w: " + m_scroller.getViewport().getExtentSize().width);

        EditPanel p = m_epanels.get(0);

        return (p.graphicsToSequence(xpos));
    }

    /**
     * Get the current position at the left hand side of the screen
     *
     * @return the position at the center of the screen
     */
    public int getPosition() {
        if (m_epanels.size() == 0) {
            return (0);
        }

        int xpos = m_scroller.getHorizontalScrollBar().getValue();
        EditPanel p = m_epanels.get(0);

        return (translate(p.graphicsToSequence(xpos)));
    }

    /**
     * Get the rightmost viewable position (the end of the longest displayed
     * sequence
     *
     * @return the rightmost viewable position
     */
    public int getMaxPosition() {
        if (m_epanels.size() == 0) {
            return (0);
        }

        int xmax = m_scroller.getHorizontalScrollBar().getMaximum();
        EditPanel p = m_epanels.get(0);

        return (translate(p.graphicsToSequence(xmax)));
    }

    /**
     * Undo the last sequence change
     *
     * @return true if succeeded
     */
    public boolean undo() {
        try {
            UndoHandler.getInstance().undo();
            updateDifferenceLists();
            repaint();

            return (true);
        } catch (Exception ex) {
            return (false);
        }
    }

    /**
     * redo the last sequence change
     *
     * @return true if succeeded
     */
    public boolean redo() {
        try {
            UndoHandler.getInstance().redo();
            updateDifferenceLists();
            repaint();

            return (true);
        } catch (Exception ex) {
            return (false);
        }
    }

    /**
     * returns the flag that indicates whether or not difference lists are being
     * updated.
     *
     * @return the flag's value
     */
    public boolean differencesUpdated() {
        return (m_updateDiffs);
    }

    /**
     * set the flag that indicates whether or not difference lists are being
     * updated.
     *
     * @param update the new flag value
     */
    public void setUpdateDifferences(boolean update) {
        m_updateDiffs = update;
    }

    /**
     * Set the channels visible. These channels are listed in the class
     * <CODE>EditPanel</CODE>
     *
     * @param prefs the preferences in an array
     */
    public void setChannelPreferences(int[] prefs) {
        m_chanPrefs = prefs;
        refreshEditors();
    }

    /**
     * refresh the properties of the editors in this panel. This is good for
     * resorting them and when some seuqences have been closed or added.
     */
    public void refreshEditors() {
        int[] firstChan = {
                // reordered to match other channels
                EditPanel.SCALE_CHANNEL, EditPanel.ABSCALE_CHANNEL, EditPanel.ALIGN_CHANNEL, EditPanel.ACID_CHANNEL,
                EditPanel.DIFF_CHANNEL, EditPanel.EVENT_CHANNEL, EditPanel.SEARCH_CHANNEL, EditPanel.PRIMER_CHANNEL };
        int[] middleChan = { EditPanel.ABSCALE_CHANNEL, EditPanel.ALIGN_CHANNEL, EditPanel.ACID_CHANNEL,
                EditPanel.DIFF_CHANNEL, EditPanel.EVENT_CHANNEL, EditPanel.SEARCH_CHANNEL, EditPanel.PRIMER_CHANNEL };
        int[] lastChan = null;

        if (m_compType == PAIRWISE_COMPARISON) {
            lastChan = new int[] { EditPanel.ABSCALE_CHANNEL, EditPanel.ALIGN_CHANNEL, EditPanel.EVENT_CHANNEL,
                    EditPanel.PRIMER_CHANNEL, EditPanel.ACID_CHANNEL, EditPanel.SEARCH_CHANNEL,
                    EditPanel.SCALE_CHANNEL, };
        } else if (m_compType == CONSENSUS_COMPARISON) {
            lastChan = new int[] { EditPanel.ABSCALE_CHANNEL, EditPanel.ALIGN_CHANNEL, EditPanel.EVENT_CHANNEL,
                    EditPanel.PRIMER_CHANNEL, EditPanel.ACID_CHANNEL, EditPanel.DIFF_CHANNEL, EditPanel.SEARCH_CHANNEL,
                    EditPanel.SCALE_CHANNEL };
        } else {
            lastChan = new int[] { EditPanel.ABSCALE_CHANNEL, EditPanel.ALIGN_CHANNEL, EditPanel.EVENT_CHANNEL,
                    EditPanel.PRIMER_CHANNEL, EditPanel.ACID_CHANNEL, EditPanel.DIFF_CHANNEL, EditPanel.SEARCH_CHANNEL,
                    EditPanel.SCALE_CHANNEL };
        }

        for (int i = 0; i < firstChan.length; ++i) {
            boolean found = false;

            for (int m_chanPref : m_chanPrefs) {
                if (m_chanPref == firstChan[i]) {
                    found = true;
                }
            }

            if (!found) {
                firstChan[i] = EditPanel.NULL_CHANNEL;
            }
        }

        for (int i = 0; i < middleChan.length; ++i) {
            boolean found = false;

            for (int m_chanPref : m_chanPrefs) {
                if (m_chanPref == middleChan[i]) {
                    found = true;
                }
            }

            if (!found) {
                middleChan[i] = EditPanel.NULL_CHANNEL;
            }
        }

        for (int i = 0; i < lastChan.length; ++i) {
            boolean found = false;

            for (int m_chanPref : m_chanPrefs) {
                if (m_chanPref == lastChan[i]) {
                    found = true;
                }
            }

            if (!found) {
                lastChan[i] = EditPanel.NULL_CHANNEL;
            }
        }

        ArrayList<EditPanel> l = new ArrayList<EditPanel>();

        for (EditPanel m_epanel : m_epanels) {
            EditPanel myEp = m_epanel;
            boolean hidden = false;

            for (FeaturedSequence aM_seqFilter : m_seqFilter) {
                if (myEp.getSequence() == aM_seqFilter) {
                    myEp.setVisible(false);
                    hidden = true;
                }
            }

            if (!hidden) {
                myEp.setVisible(true);
                l.add(myEp);
            }
        }

        for (int i = 0; i < l.size(); ++i) {
            EditPanel myEp = m_epanels.get(i);
            myEp.setDisplayArea(m_charStart, m_charStop);
        }

        if (l.size() > 0) {
            EditPanel myEp = l.get(0);
            myEp.setChannelPreferences(firstChan);

            if (l.size() == 2) {
                myEp = l.get(1);
                myEp.setChannelPreferences(lastChan);
            } else if (l.size() > 2) {
                for (int i = 1; i < (l.size() - 1); ++i) {
                    myEp = l.get(i);
                    myEp.setChannelPreferences(middleChan);
                }

                myEp = l.get(l.size() - 1);
                myEp.setChannelPreferences(lastChan);
            }
        }

        m_headListModel.removeAllElements();

        for (EditPanel aL : l) {
            m_headListModel.addElement(aL);
        }
    }

    /**
     * Refresh the visual state, repaint and revalidate all contained panels
     */
    public void refreshState() {
        FeaturedSequence[] seqs = getSelectedSequences();

        // revalidate the containers for the headers and edit panels
        m_editContainer.revalidate();
        m_editContainer.repaint();

        Object[] o = m_headListModel.toArray();
        m_headListModel.removeAllElements();

        for (int i = 0; i < o.length; i++) {
            m_headListModel.addElement(o[i]);
        }
        m_scroller.isValidateRoot();
        revalidate();

        int[] newIndices = new int[seqs.length];

        for (int i = 0; i < seqs.length; ++i) {
            EditPanel p = null;
            int j;

            for (j = 0; j < m_epanels.size(); ++j) {
                p = m_epanels.get(j);

                if (p.getSequence() == seqs[i]) {
                    break;
                }
            }

            if (j == m_epanels.size()) {
                p = null;
            }

            newIndices[i] = m_headListModel.indexOf(p);
        }

        m_hList.setSelectedIndices(newIndices);
    }

    // refresh gui after having saved its contents to image

    public void refreshStateAfterPicture() {
        FeaturedSequence[] seqs = getSelectedSequences();

        // revalidate the containers for the headers and edit panels
        m_editContainer.revalidate();
        m_editContainer.repaint();

        Object[] o = m_headListModel.toArray();
        m_headListModel.removeAllElements();

        for (int i = 0; i < o.length; ++i) {
            m_headListModel.addElement(o[i]);
            try {
                Thread.sleep(100); // m_headlistModel is used by UI thread! give it some time
            } catch (Exception e) {

            }
        }

        m_scroller.isValidateRoot();
        revalidate();

        int[] newIndices = new int[seqs.length];

        for (int i = 0; i < seqs.length; ++i) {
            EditPanel p = null;
            int j = 0;

            for (j = 0; j < m_epanels.size(); ++j) {
                p = m_epanels.get(j);

                if (p.getSequence() == seqs[i]) {
                    break;
                }
            }

            if (j == m_epanels.size()) {
                p = null;
            }

            newIndices[i] = m_headListModel.indexOf(p);
        }

        m_hList.setSelectedIndices(newIndices);
    }

    /**
     * get the an array of selected sequences
     *
     * @return the array of selected sequences or an empty one if none are selected
     */
    public FeaturedSequence[] getSelectedSequences() {
        List<Object> olist = m_hList.getSelectedValuesList();
        List<FeaturedSequence> fs = new ArrayList<>();
        for (Object o : olist) {
            if (o instanceof EditPanel) {
                fs.add(((EditPanel) o).getSequence());
            }
        }
        return fs.toArray(new FeaturedSequence[fs.size()]);
    }

    /*
     * Get the array of all regions. Used for aligning in code hop. Everything will
     * be aligned (as opposed to selected regions only (above))
     */

    public FeaturedSequence[] getAllSequences() {
        ListModel model = m_hList.getModel();

        List<FeaturedSequence> l = new ArrayList<FeaturedSequence>();

        for (int i = 0; i < model.getSize(); i++) {
            Object o = model.getElementAt(i);
            l.add(((EditPanel) o).getSequence());
        }
        return l.toArray(new FeaturedSequence[l.size()]);
    }

    /**
     * get the an array of sequences not selected
     *
     * @return the array of unselected sequences or an empty one if all are selected
     */
    public FeaturedSequence[] getUnselectedVisibleSequences() {
        ArrayList l = new ArrayList();

        FeaturedSequence[] seqs = getSequences();
        FeaturedSequence[] selectedSeqs = getSelectedSequences();

        for (int i = 0; i < seqs.length; ++i) {
            boolean found = false;

            for (int j = 0; j < m_seqFilter.length; ++j) {
                if (seqs[i] == m_seqFilter[j]) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                for (int j = 0; j < selectedSeqs.length; j++) {
                    if (seqs[i] == selectedSeqs[j]) {
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                l.add(seqs[i]);
            }

        }

        return ((FeaturedSequence[]) l.toArray(new FeaturedSequence[0]));
    }

    public int[] getSelectedIndices() {
        return m_hList.getSelectedIndices();
    }

    /**
     * select all sequence data for currently chosen sequences
     */
    public void selectAllData() {
        int maxLengthSeq = 0;
        int maxLength = 0;
        FeaturedSequence[] seqs = getSelectedSequences();

        if (seqs.length == 0) {
            selectAll();
        }

        seqs = getSelectedSequences();

        boolean same = true;

        for (int i = 0; i < seqs.length; ++i) {
            for (int j = 0; j < seqs.length; ++j) {
                if (seqs[i].length() != seqs[j].length()) {
                    same = false;
                }
            }
        }

        if (!same) {
            for (int i = 0; i < seqs.length; ++i) {
                if (seqs[i].length() > maxLength) {
                    maxLength = seqs[i].length();
                    maxLengthSeq = i;
                }
            }

            for (EditPanel ep : m_epanels) {
                if (ep.getSequence() == seqs[maxLengthSeq]) {
                    propogateSelect(ep, 0, seqs[maxLengthSeq].length());

                    break;
                }
            }
        } else {
            for (EditPanel ep : m_epanels) {
                if (ep.getSequence() == seqs[0]) {
                    propogateSelect(ep, 0, seqs[0].length());

                    break;
                }
            }
        }

        m_wholesequences = true;
    }

    /**
     * Check the lengths of various sequences in BBB panel, and if different length
     * return false
     *
     * @return false if sequences are of different lengths
     */
    public boolean checkLengths() {
        FeaturedSequence[] seqs = getVisibleSequences();
        boolean same = true;

        for (int i = 0; i < seqs.length; ++i) {
            for (int j = 0; j < seqs.length; ++j) {
                if (seqs[i].length() != seqs[j].length()) {
                    same = false;
                }
            }
        }

        return (same);
    }

    /**
     * Check the names of sequences in BBB panel, and if any two are the same exit
     *
     * @return false if no two sequences have the same name
     */
    public boolean checkNames(FeaturedSequence[] seqs) {
        boolean same = false;
        int count = 1;
        for (int i = 0; i < seqs.length; ++i) {
            for (int j = i + 1; j < seqs.length; ++j) {
                if (seqs[i].getName().trim() == seqs[j].getName().trim() && i != j) {
                    same = true;
                    count++;
                }
            }
        }
        // two or more sequences have the same name show warning then exit BBB
        while (same) {
            UITools.showWarning(count
                    + " or more sequences were found to have the same name. Please edit these and then reopen Base By Base!",
                    null);
            System.exit(0);
        }
        return same;
    }

    /**
     * Edit names of the sequences
     *
     * @return true if succeeded
     * @author Sangeeta Neti
     */
    protected boolean EditSequenceNames() {

        EditPanel p = (EditPanel) m_epanels.get(m_selectedSequence);
        FeaturedSequence[] seqs = getSequences();
        boolean val = false;

        // display the sequence in a separate window for editing names with Save/Cancel option
        SequenceEditorDialog dialog = new SequenceEditorDialog(getSequences());
        dialog.setBounds(100, 100, 800, 300);
        dialog.setVisible(true);

        if (dialog.getApproval()) {
            String[] names = dialog.getNames();

            for (int i = 0; i < seqs.length; i++) {
                seqs[i].setName(names[i]);
            }
            val = UITools.showYesNo(
                    "This action cannot be undone, although names may be re-edited. Are you sure you wish to continue?",
                    this);
        }

        // redraw the alignment to make sure it shows
        if (val) {
            p.revalidate();
            p.resetView();
            refreshState();
            p.repaint();
            return (true);
        } else
            return (false);

    }

    /**
     * Select all sequencs
     */
    public void selectAll() {
        m_hList.setSelectionInterval(0, m_headListModel.getSize() - 1);
        m_hList.repaint();
    }

    /**
     * unselect all sequences
     */
    public void deselectAll() {
        m_hList.removeSelectionInterval(0, m_headListModel.getSize() - 1);
        m_hList.repaint();
    }

    /**
     * Returns FeaturedSequence fragments (subsequences) of the current sequences.
     * Each will have no features, just sequence data and a name
     *
     * @param all If true, will return the selected segment from each sequence
     *            otherwise will return the segment from only the selected sequence.
     * @return null if no sequences are selected
     */
    public FeaturedSequence[] getSelectedSegments(boolean all) {
        if (getSelectedSequence() == -1) {
            return (null);
        }

        FeaturedSequence[] seqs;

        if (all) {
            seqs = getVisibleSequences();
        } else {
            seqs = getSelectedSequences();

            if (seqs.length == 0) {
                return (null);
            }
        }

        FeaturedSequence[] ret = null;
        ret = new FeaturedSequence[seqs.length];

        EditPanel p = (EditPanel) m_epanels.get(getSelectedSequence());
        Location l = p.getRelativeSelection();

        if (l == null) {
            return (null);
        }

        int start = l.getMin();
        int stop = l.getMax() + 1;

        for (int i = 0; i < seqs.length; ++i) {
            String seq = seqs[i].substring(start, stop);
            int rstrt = start;
            int rstp = stop;
            FeaturedSequence segment = new FeaturedSequence(-1, seqs[i].getName() + " (" + rstrt + "," + rstp + ")",
                    seq);

            //////////////////////////////////////////////////
            // Here is the code for retrieving all the Features
            // @authour asyed
            //////////////////////////////////////////////////
            Iterator it_features = seqs[i].features();

            while (it_features.hasNext()) {
                Feature ft = (Feature) it_features.next();
                Location loc = ft.getLocation();
                StrandedFeature.Template template = (StrandedFeature.Template) ft.makeTemplate();

                if (rstrt <= seqs[i].getRelativePosition(loc.getMin())
                        && rstp >= seqs[i].getRelativePosition(loc.getMax())) {
                    int relative_start = 0;
                    int relative_stop = 0;

                    if (start > seqs[i].getAbsolutePosition(start) + 1
                            && seqs[i].getAbsolutePosition(start) == seqs[i].getAbsolutePosition(start - 1)) {
                        relative_start = seqs[i].getRelativePosition(loc.getMin())
                                - (seqs[i].getRelativePosition(loc.getMin())
                                        - seqs[i].getAbsolutePosition(seqs[i].getRelativePosition(loc.getMin())))
                                - (start - (start - seqs[i].getAbsolutePosition(start)));
                        relative_stop = seqs[i].getRelativePosition(loc.getMax())
                                - (seqs[i].getRelativePosition(loc.getMax())
                                        - seqs[i].getAbsolutePosition(seqs[i].getRelativePosition(loc.getMax())))
                                - (start - (start - seqs[i].getAbsolutePosition(start)));
                    } else {
                        relative_start = seqs[i].getRelativePosition(loc.getMin())
                                - (seqs[i].getRelativePosition(loc.getMin())
                                        - seqs[i].getAbsolutePosition(seqs[i].getRelativePosition(loc.getMin())))
                                - (start - (start - seqs[i].getAbsolutePosition(start))) + 1;
                        relative_stop = seqs[i].getRelativePosition(loc.getMax())
                                - (seqs[i].getRelativePosition(loc.getMax())
                                        - seqs[i].getAbsolutePosition(seqs[i].getRelativePosition(loc.getMax())))
                                - (start - (start - seqs[i].getAbsolutePosition(start))) + 1;
                    }

                    Location loc_f = null;

                    if (relative_start == relative_stop) {
                        loc_f = new PointLocation(relative_start);
                    } else {
                        loc_f = new RangeLocation(relative_start, relative_stop);
                    }

                    if (ft.getType().equals("PRIMER")) {
                        FeatureTools.createPrimerFeature(segment, loc_f, template.strand,
                                ft.getAnnotation().getProperty(AnnotationKeys.NAME).toString(),
                                ft.getAnnotation().getProperty(AnnotationKeys.PRIMER_SEQ).toString(),
                                ft.getAnnotation().getProperty(AnnotationKeys.PRIMER_MELTINGTEMP).toString(),
                                ft.getAnnotation().getProperty(AnnotationKeys.PRIMER_FRIDGE).toString(),
                                ft.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT).toString());
                    } else if (ft.getType().equals("GENE")) {
                        FeatureTools.createGeneFeature(segment,
                                ((Integer) ft.getAnnotation().getProperty(AnnotationKeys.GENE_ID)).intValue(),
                                ft.getAnnotation().getProperty(AnnotationKeys.NAME).toString(), loc_f, template.strand,
                                ft.getSource());
                    } else if (ft.getType().equals("COMMENT")) {
                        FeatureTools.createUserComment(segment, loc_f, template.strand,
                                ft.getAnnotation().getProperty(AnnotationKeys.NAME).toString(),
                                ft.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT).toString(),
                                (Color) ft.getAnnotation().getProperty(AnnotationKeys.BGCOLOR),
                                (Color) ft.getAnnotation().getProperty(AnnotationKeys.FGCOLOR));
                    }
                }
            }

            ret[i] = segment;
        }

        return (ret);
    }

    /**
     * get the consensus of all of the displayed sequences
     *
     * @return the consensus
     */
    public Consensus getConsensus() {
        return (m_consensus);
    }

    /**
     * show a consensus of all sequences.
     *
     * @param consType the consensus type to display
     */
    public void showConsensus(final int consType) {
        Consensus c = ConsensusFactory.createEmptyConsensus(consType);

        if (c.getClass().equals(m_consensus.getClass())) {
            cons_visible(true);
            refreshConsensus();

            return;
        }

        Runnable r = new Runnable() {
            public void run() {
                Consensus cons = ConsensusFactory.createConsensus(consType, getVisibleSequences());
                showConsensus(cons);
            }
        };

        try {
            UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) getTopLevelAncestor(), r,
                    "Calculating " + c.getName());
        } catch (Exception ex) {
        }
    }

    /**
     * show the given consensus
     *
     * @param cons the new consensus
     */
    protected void showConsensus(Consensus cons) {
        m_consensus = cons;
        m_consDisp.setConsensus(cons);
        cons_visible(true);
    }

    /**
     * refresh (recalculate) the consensus displayed. This can be time consuming and
     * shouldn't be done in real-time.
     */
    public void refreshConsensus() {
        Runnable r = new Runnable() {
            public void run() {
                m_consensus.calculate();

                if (getComparisonType() == CONSENSUS_COMPARISON) {
                    updateDifferenceLists();
                }

                repaint();
            }
        };

        try {
            UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) getTopLevelAncestor(), r,
                    "Refreshing Consensus");
        } catch (Exception ex) {
        }
    }

    /**
     * set the visiblity of the sequence ordering tools
     *
     * @param visible the new visibility
     */
    public void setSequenceToolsVisible(boolean visible) {
        m_seqTools.setVisible(visible);
    }

    /**
     * get the names of all genes selected in the current display area.
     *
     * @return an array of gene names
     */
    public String[] getSelectedGeneNames() {
        FeaturedSequence[] seqs = getSelectedSequences();
        FeatureFilter ff = new FeatureFilter.And(new FeatureFilter.StrandFilter(getDisplayStrand()),
                new FeatureFilter.ByType(FeatureType.GENE));
        ArrayList nList = new ArrayList();

        for (FeaturedSequence seq : seqs) {
            int start = 0;
            int stop = Integer.MAX_VALUE;

            if (m_charStart != -1) {
                if (m_charStart >= seq.length()) {
                    continue;
                }

                start = seq.getAbsolutePosition(m_charStart);
            }

            if (m_charStop != -1) {
                if (m_charStop >= seq.length()) {
                    continue;
                }

                stop = seq.getAbsolutePosition(m_charStop);
            }

            if (start >= stop) {
                continue;
            }

            Location range = new RangeLocation(start, stop);
            FeatureFilter filt = new FeatureFilter.And(new FeatureFilter.OverlapsLocation(range), ff);
            FeatureHolder fh = seq.filter(filt, false);

            for (Iterator j = fh.features(); j.hasNext();) {
                Feature f = (Feature) j.next();
                String name = f.getAnnotation().getProperty(AnnotationKeys.NAME).toString();
                nList.add(name);
            }
        }

        String[] names = (String[]) nList.toArray(new String[0]);
        Arrays.sort(names, new GeneNameComparator());

        return (names);
    }

    /**
     * Gets the position of the difference on the screen in relation to a given
     * position. Positions are relative (gapped).
     *
     * @param posn      The position to get the first gap to the right of
     * @param direction 'true' indicates forward direction and 'false' indicates
     *                  reverse direction
     * @return An integer representing the position on the screen of the first
     *         difference to the left of the given position
     */
    public int resolveDifference(int posn, boolean direction) {
        FeaturedSequence[] seqs = getVisibleSequences();
        FeatureFilter ff = new FeatureFilter.ByType(FeatureType.DIFFERENCE_LIST);
        MergeFeatureHolder mfh = new MergeFeatureHolder();

        for (int i = 0; i < seqs.length; ++i) {
            try {
                mfh.addFeatureHolder(seqs[i]);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // filter all seqs
        Iterator it = mfh.filter(ff, false).features();
        ArrayList l = new ArrayList();

        // get the diff array from sequence
        while (it.hasNext()) {
            Feature f = (Feature) it.next();

            if (f.getAnnotation().containsProperty(AnnotationKeys.DIFF_ARRAY)) {
                l.add(f.getAnnotation().getProperty(AnnotationKeys.DIFF_ARRAY));
            }
        }

        if (direction) {
            // SEARCHES IN THE FORWARD DIRECTION
            for (int i = posn + 1; i < getMaxPosition(); ++i) {
                for (int j = 0; j < l.size(); ++j) {
                    int[] d = (int[]) l.get(j);

                    if (i >= d.length) {
                        return (posn);
                    }

                    if (d[i] != ca.virology.lib.io.sequenceData.DifferenceType.I_NONE) {
                        return (i);
                    }
                }
            }
        } else {
            // SEARCHES IN REVERSE DIRECTION
            for (int i = posn - 1; i >= 0; --i) {
                for (int j = 0; j < l.size(); ++j) {
                    if (((int[]) l.get(j))[i] != ca.virology.lib.io.sequenceData.DifferenceType.I_NONE) {
                        return (i);
                    }
                }
            }
        }

        return (posn);
    }

    /**
     * Resolve the position of the next or last event in the window depending on the
     * given position, and even type.
     *
     * @param position     The position in the sequence(s) to start from
     * @param next         if true, look right (forward) otherwise look left
     *                     (backward).
     * @param feType       The type of feature to look for. This should be a string
     *                     member of <CODE>FeatureType</CODE>
     * @param ignoreStrand if true, this will ignore the strand on which the feature
     *                     occurs
     * @return The relative position of the next or last event or -1 if there is
     *         nothing in the given direction of the given type
     */
    public GenePos featurePair = null;

    public int resolveFeaturePosition(int position, boolean next, String feType, boolean ignoreStrand) {

        FeaturedSequence[] seqs = getSelectedSequences();
        if (seqs.length == 0) {
            seqs = getVisibleSequences();
        }
        FeatureFilter ff = new FeatureFilter.ByType(feType);

        if (!ignoreStrand) {
            ff = new FeatureFilter.And(new FeatureFilter.StrandFilter(getDisplayStrand()), ff);
        }

        GenePos[] pairs = new GenePos[seqs.length];
        Location sloc = null;

        for (int i = 0; i < seqs.length; ++i) {
            try {
                if (next) {
                    sloc = new RangeLocation(seqs[i].getAbsolutePosition(position), Integer.MAX_VALUE);
                } else {
                    sloc = new RangeLocation(1, seqs[i].getAbsolutePosition(position));
                }
            } catch (Exception ex) {

            }

            FeatureFilter sff = new FeatureFilter.And(new FeatureFilter.OverlapsLocation(sloc), ff);
            FeatureHolder fh = seqs[i].filter(sff, false);
            int[] posns = new int[fh.countFeatures()];
            int[] lposns = new int[fh.countFeatures()];

            int cnt = 0;

            for (Iterator j = fh.features(); j.hasNext();) {
                Feature f = (Feature) j.next();
                int pos = seqs[i].getRelativePosition(f.getLocation().getMin());
                int posEnd = seqs[i].getRelativePosition(f.getLocation().getMax());

                if (next && (pos > position)) {
                    posns[cnt] = pos;
                    lposns[cnt] = posEnd;
                    cnt++;
                } else if (!next && (pos < position)) {
                    posns[cnt] = pos;
                    lposns[cnt] = posEnd;
                    cnt++;
                }
            }

            Arrays.sort(posns);
            Arrays.sort(lposns);

            if (posns.length > 0) {
                if (next) {
                    for (int j = 0; j < posns.length; ++j) {
                        if (posns[j] != 0) {
                            pairs[i] = new GenePos(posns[j], lposns[j]);
                            break;
                        }
                    }
                } else {
                    for (int j = posns.length - 1; j >= 0; --j) {
                        if (posns[j] != 0) {
                            pairs[i] = new GenePos(posns[j], lposns[j]);
                            break;
                        }
                    }
                }
            }
        }

        Arrays.sort(pairs, new Comparator<GenePos>() {
            @Override
            public int compare(GenePos a, GenePos b) {
                if (a != null && b != null)
                    return a.start - b.start;
                else
                    return 0;
            }
        });

        if (next) {
            for (int i = 0; i < pairs.length; ++i) {
                if (pairs[i] != null) {
                    featurePair = pairs[i];
                    return (pairs[i].start);
                }
            }
        } else {
            for (int i = pairs.length - 1; i >= 0; --i) {
                if (pairs[i] != null) {
                    featurePair = pairs[i];
                    return (pairs[i].start);
                }
            }
        }

        return (-1);
    }

    /**
     * Resolves the position of the gene with the given name on the screen.
     *
     * @param geneName The name of the gene to search for
     * @return The integer position of the requested gene or -1 if the name is not
     *         to be found on the screen (in the displayed strand)
     */
    public int resolveGenePosition(String geneName) {
        FeaturedSequence[] seqs = getVisibleSequences();
        FeatureFilter ff = new FeatureFilter.And(new FeatureFilter.ByAnnotation(AnnotationKeys.NAME, geneName),
                new FeatureFilter.ByType(FeatureType.GENE));

        for (int i = 0; i < seqs.length; ++i) {
            FeatureHolder fh = seqs[i].filter(ff, false);

            if (fh.countFeatures() > 0) {
                Feature f = (Feature) fh.features().next(); // get the first one
                int pos = seqs[i].getRelativePosition(f.getLocation().getMin());

                return (pos);
            }
        }

        return (-1);
    }

    /**
     * calculated Tm using Bolton and McCarthy, PNAS 84:1390 (1962) from Sambrook,
     * Fritsch and Maniatis, Molecular Cloning, p 11.46 (1989, CSHL Press).
     *
     * @param primerSeq The sequence of the Primer
     * @return int the calculated temperature to no more than 2 decimal places
     */
    public static double getPrimerTemp(String primerSeq) {
        double primerTemp;
        int contentGC = 0; // Assumes the seq does not contain N or other characters
        int gaps = 0;
        int primerLength = primerSeq.length();

        for (int i = 0; i < primerLength; i++) {
            if (primerSeq.charAt(i) == 'G' || primerSeq.charAt(i) == 'C') {
                contentGC++;
            } else if (primerSeq.charAt(i) == '-') {
                gaps++;
            }
        }

        double percentGC = (double) contentGC / (primerSeq.length() - gaps);

        if (primerLength < 14) {
            // simplest calculation less than 14 bases. Assumes presence of 50mM
            // monovalent cations
            primerTemp = ((4 * contentGC) + (2 * (primerSeq.length() - contentGC)));
        } else {
            // Salt Concentration
            primerTemp = 81.5 + 16.6 * (Math.log(0.05) / Math.log(10)) + 0.41 * (percentGC * 100)
                    - 600 / primerSeq.length();
        }
        // other stock equations//64.9 + 41*(contentGC -16.4)/primerSeq.length();

        primerTemp = 0.01 * (int) (primerTemp * 100 + 0.5);

        return (primerTemp);
    }

    /**
     * Resolves the position of the gene with the given name on the screen.
     *
     * @param geneName The name of the gene to search for
     * @return The integer position of the requested gene or -1 if the name is not
     *         to be found on the screen (in the displayed strand)
     */
    public int resolveGeneEnd(String geneName) {
        FeaturedSequence[] seqs = getVisibleSequences();
        FeatureFilter ff = new FeatureFilter.And(new FeatureFilter.ByAnnotation(AnnotationKeys.NAME, geneName),
                new FeatureFilter.ByType(FeatureType.GENE));

        for (int i = 0; i < seqs.length; ++i) {
            FeatureHolder fh = seqs[i].filter(ff, false);

            if (fh.countFeatures() > 0) {
                Feature f = (Feature) fh.features().next(); // get the first one
                int pos = seqs[i].getRelativePosition(f.getLocation().getMax());

                return (pos);
            }
        }

        return (-1);
    }

    /**
     * Delete the last selected Feature (used for comments and primers so far.)
     */
    public void deleteSelectedFeature(String type) {
        boolean delete = true;

        if (m_selectedFeature == null) {
            delete = false;
        } else {
            // validate that it's a comment
            delete = m_selectedFeature.getType().equals(type);
        }

        if (delete) {
            FeaturedSequence[] seqs = getVisibleSequences();

            for (FeaturedSequence seq : seqs) {
                try {
                    if (seq.containsFeature(m_selectedFeature)) {
                        seq.removeFeature(m_selectedFeature);
                    }

                    UndoHandler.getInstance().setModified(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            refreshState();
        } else {
            UITools.showWarning("Please select a valid feature to delete.", this);
        }
    }

    public void setRNAOverviewFrame(RNAOverviewFrame frame) {
        m_rnaOvFrame = frame;
    }

    public RNAOverviewFrame getRNAOverviewFrame() {
        return m_rnaOvFrame;
    }

    public JPanel getPrimaryPane() {
        return this;
    }

    public void viewRnaPreviewWindow(final mRNAs mrnas) {

        Runnable r = new Runnable() {
            public void run() {

                if ((getRNAOverviewFrame() == null) || !getRNAOverviewFrame().isDisplayable()) {
                    m_rnaOvFrame = new RNAOverviewFrame(PrimaryPanel.this, mrnas, getPrimaryPanel());
                    setRNAOverviewFrame(m_rnaOvFrame);

                    int[] frame_view = m_rnaOvFrame.mainPanel.get_view();

                    getRNAOverviewFrame().pack();
                    // Position the dialog window
                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    int w = dim.width / 2;
                    int h = getRNAOverviewFrame().getHeight();
                    int x = (dim.width - w) / 2;
                    int y = (dim.height - h) / 2;
                    getRNAOverviewFrame().setBounds(x, y, w, h);
                    getRNAOverviewFrame().setVisible(true);

                } else {
                    getRNAOverviewFrame().dispose();
                    setRNAOverviewFrame(null);
                    viewRnaPreviewWindow(mrnas);
                }

            }
        };

        try {
            UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) getTopLevelAncestor(), r,
                    "Processing Visual Summary...");
        } catch (Exception ex) {
            System.out.println("Failed to Generated Visual Summary...");
        }

    }

    /**
     * Display the visual overview window
     */
    public void viewPreviewWindow() {
        Runnable r = new Runnable() {
            public void run() {
                if ((getOverviewFrame() == null) || !getOverviewFrame().isDisplayable()) {
                    setOverviewFrame(new OverviewFrame(PrimaryPanel.this, m_compType));
                    getOverviewFrame().addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ev) {
                            RangeLocation l = getOverviewFrame().getView();
                            int min = l.getMin();
                            int max = l.getMax();

                            if (l.getMin() < m_charStart) {
                                getOverviewFrame().setView(m_charStart, m_charStart + (max - min));
                                scrollToLocation(m_charStart);
                            } else if ((l.getMax() > m_charStop) && (m_charStop != -1)) {
                                getOverviewFrame().setView(m_charStop - (max - min), m_charStop);
                                scrollToLocation(m_charStop - (max - min));
                            } else {
                                scrollToLocation(l.getMin());
                            }
                        }
                    });

                    getOverviewFrame().pack();

                    // Position the dialog window
                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    int w = dim.width / 2;
                    int h = getOverviewFrame().getHeight();
                    int x = (dim.width - w) / 2;
                    int y = (dim.height - h) / 2;
                    getOverviewFrame().setBounds(x, y, w, h);
                    getOverviewFrame().setDisplayArea(m_charStart, m_charStop);
                    getOverviewFrame().setVisible(true);
                } else {
                    getOverviewFrame().dispose();
                    setOverviewFrame(null);
                    viewPreviewWindow();
                }
            }
        };

        try {
            UITools.invokeProgressWithMessageNoButtons((java.awt.Frame) getTopLevelAncestor(), r,
                    "Processing Visual Summary...");
        } catch (Exception ex) {
            System.out.println("Failed to Generated Visual Summary...");
        }
    }

    /**
     * Close the visual overview frame
     */
    protected void closeOverviewFrame() {
        if ((getOverviewFrame() != null) && getOverviewFrame().isDisplayable()) {
            getOverviewFrame().dispose();
            setOverviewFrame(null);
            System.out.print("ASDSADAS");
        }
    }

    /**
     * set the overview frame object that is displayed by this panel
     *
     * @param frame the new frame
     */
    protected void setOverviewFrame(OverviewFrame frame) {
        m_ovFrame = frame;
    }

    /**
     * Get the overview frame object
     *
     * @return the overview frame
     */
    protected OverviewFrame getOverviewFrame() {
        return (m_ovFrame);
    }

    /**
     * set the view displayed in the preview window
     *
     * @param x1 the leftmost position in the view
     * @param x2 the rightmost position in the view
     */
    protected void setPreviewView(int x1, int x2) {
        if (m_epanels.size() > 0) {
            EditPanel p = (EditPanel) m_epanels.get(0);

            if ((m_ovFrame != null) && m_ovFrame.isDisplayable()) {
                m_ovFrame.setView(translate(p.graphicsToSequence(x1)), translate(p.graphicsToSequence(x2)));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setStranded(boolean b) {
        m_frameButton.setEnabled(b);
    }

    /**
     * Add the given sequence to the panel
     *
     * @param seq    The sequence to add
     * @param dbName The name of the database that the sequence is from, or
     *               <CODE>null<CODE> if it is from a file
     */
    public synchronized void addSequenceEditor(FeaturedSequence seq, String dbName) {
        final EditPanel ep = new EditPanel(seq, dbName);
        final HeaderPanel hp = new HeaderPanel(ep);

        m_epanels.add(ep);
        m_hpanels.add(hp);
        ep.setDisplayFont(SEQ_FONT);
        ep.setChannelFont(CHAN_FONT);
        ep.setChannelHeight(CHAN_HEIGHT);
        ep.setChannelSpacing(CHAN_SPACE);

        m_colorScheme.setSequences(getVisibleSequences());
        ep.setColorScheme(m_colorScheme);
        hp.setDisplayFont(CHAN_FONT);

        m_scroller.getHorizontalScrollBar().setUnitIncrement(ep.getDisplayFontWidth());
        seq.addPropertyChangeListener(EditableSequence.SEQUENCE_PROPERTY, new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (!isProcessing()) {
                    updateDifferenceLists();
                    repaint();
                }
            }
        });

        seq.addPropertyChangeListener(FeaturedSequence.FEATURES_PROPERTY, new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (!isProcessing()) {
                    for (int i = 0; i < m_epanels.size(); ++i) {
                        EditPanel ep = (EditPanel) m_epanels.get(i);
                        ep.resetView();
                    }

                    repaint();
                }
            }
        });

        ep.addPropertyChangeListener(EditPanel.SELECTION_PROPERTY, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                for (int i = 0; i < m_epanels.size(); ++i) {
                    if (ev.getSource() == m_epanels.get(i)) {
                        if ((m_epanels.get(i)).getRelativeSelection() != null) {
                            setSelectedSequence(i);
                        }
                    }
                }
            }
        });

        ep.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent ev) {
                Point p = ev.getPoint();
                int x = (int) p.getX();

                if (x < 0) {
                    x = 0;
                }

                if (x > m_scroller.getHorizontalScrollBar().getMaximum()) {
                    x = m_scroller.getHorizontalScrollBar().getMaximum();
                }

                Rectangle r = m_scroller.getViewport().getViewRect();

                if (x < r.getX()) {
                    m_scroller.getHorizontalScrollBar().setValue(x);
                } else if (x > (r.getX() + r.getWidth())) {
                    m_scroller.getHorizontalScrollBar().setValue(x - (int) r.getWidth());
                }
            }
        });

        KeyAdapter ki = new KeyAdapter() {

            public void keyPressed(KeyEvent ev) {
                System.out.println("Hello");
            }
        };

        ep.addKeyListener(ki);

        MouseInputAdapter mi = new MouseInputAdapter() {
            boolean drag = false;
            Point first = null;
            Point last = null;

            public void mouseMoved(MouseEvent ev) {
                if (shiftPressed) {

                    if (first == null) {
                        first = ev.getPoint();
                    }

                    if (last == null) {
                        last = ev.getPoint();
                    }
                    StrandedFeature feat = ep.getFeatureAtPoint(first);
                    m_selectedFeature = feat;

                    Point current = ev.getPoint();

                    if (m_mouseMode == SequenceDisplay.EDIT_MODE) {
                        propogateEdit(ep, (int) last.getX(), (int) first.getY(), (int) current.getX(),
                                (int) first.getY());
                    } else if (m_mouseMode == SequenceDisplay.SELECT_MODE) {
                        propogateSelect(ep, (int) first.getX(), (int) first.getY(), (int) current.getX(),
                                (int) first.getY());
                    } else {
                        revisedGlueSlide(ep, (int) first.getX(), (int) last.getX(), (int) current.getX(),
                                (int) first.getY());
                    }
                }
            }

            public void mouseDragged(MouseEvent ev) {

                if (!shiftPressed) {
                    drag = true;

                    if (first == null) {
                        first = ev.getPoint();
                    }

                    if (last == null) {
                        last = ev.getPoint();
                    }
                    StrandedFeature feat = ep.getFeatureAtPoint(first);
                    m_selectedFeature = feat;

                    Point current = ev.getPoint();

                    if (m_mouseMode == SequenceDisplay.EDIT_MODE) {
                        propogateEdit(ep, (int) last.getX(), (int) first.getY(), (int) current.getX(),
                                (int) first.getY());
                    } else if (m_mouseMode == SequenceDisplay.SELECT_MODE) {
                        propogateSelect(ep, (int) first.getX(), (int) first.getY(), (int) current.getX(),
                                (int) first.getY());
                    } else {

                        revisedGlueSlide(ep, (int) first.getX(), (int) last.getX(), (int) current.getX(),
                                (int) first.getY());
                    }

                    if (ev.isShiftDown()) {
                        shiftPressed = true;
                    }

                    last = current;

                }
            }

            boolean shiftPressed = false;

            public void mouseReleased(MouseEvent ev) {
                // check if shift pressed
                if (!shiftPressed) {
                    drag = false;
                    last = null;
                    first = null;
                    shiftPressed = false;
                }
            }

            public void mouseClicked(MouseEvent ev) {

                if (shiftPressed) {
                    shiftPressed = false;
                } else {
                    if (ev.isShiftDown()) {
                        shiftPressed = true;
                    } else {
                        drag = false;
                        last = null;
                        first = null;
                        shiftPressed = false;
                    }

                    Point p = ev.getPoint();

                    StrandedFeature feat = ep.getFeatureAtPoint(p);
                    m_selectedFeature = feat;

                    int posn = ep.graphicsToSequence((int) p.getX());

                    if (m_selectedSequence >= 0) {
                        if (ev.getButton() == MouseEvent.BUTTON3) {
                            m_popupMenu.show(ep, ev.getX(), ev.getY());
                        }
                    }

                    if (ev.getButton() != MouseEvent.BUTTON3) {
                        if (feat == null) {
                            propogateSelect(ep, (int) p.getX(), (int) p.getY(), (int) p.getX(), (int) p.getY());
                        } else {
                            Location l = null;
                            FeatureAction fa = FeatureActionFactory.getInstance().createFeatureAction(feat);

                            if (fa != null) {
                                l = fa.doClick(ev.getClickCount(), posn, ep.getSequence());
                                refreshState();
                            }

                            if (ev.getClickCount() == 1) {
                                int p1 = untranslate(l.getMin());
                                int p2 = untranslate(l.getMax());

                                propogateSelect(ep, p1, p2);
                            }
                        }
                    }
                }
            }
        };

        ep.addMouseMotionListener(mi);
        ep.addMouseListener(mi);

        try {
            UITools.invoke(new Runnable() {
                public void run() {
                    m_editContainer.add(ep);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void editFeature(String featureType) {
        /*
         * Only one sequence needs to be selected and a sequence region must be selected
         * as well otherwise popup following errors for checks
         */
        // Get all the featured sequences
        FeaturedSequence[] seqs = getSelectedSequences();

        // If no sequences were selected pop-up and error/warning
        if (seqs.length == 0) {
            UITools.showWarning(
                    "You have to select the sequence, and a feature within\nthe sequence that you want to edit. Try Again!",
                    null);

            return;
        } else if (seqs.length > 1) {
            // If more than one sequences are selected pop-up following error
            UITools.showWarning(
                    "You have to select only ONE sequence, and a feature within\nthe sequence that you want to edit. Try again!",
                    null);

            return;
        }

        try {
            EditPanel p = m_epanels.get(getSelectedSequence());
            Location l = p.getAbsoluteSelection();

            // If no sequence region is selected within the FeaturedSequence
            if (l == null) {
                UITools.showWarning(
                        "You have to select a sequence regions with feature\nthat you want to edit. Try Again!", null);

                return;
            }

            if (m_selectedFeature != null) {
                FeatureAction fa = FeatureActionFactory.getInstance().createFeatureAction(m_selectedFeature);

                Location fLoc = m_selectedFeature.getLocation();
                int posn = (fLoc.getMin() + fLoc.getMax()) / 2;
                l = fa.doClick(2, posn, p.getSequence());
                refreshState();
            } else {
                UITools.showWarning("No feature selected.", null);
            }
        } catch (Exception e) {
            System.out.println("Error Occured While Saving Primers To File. .");
            e.printStackTrace();
        }
    }

    /**
     * This will translate a given screen position to the actual sequence position
     * it represents. If the leftmost point is 100 on the ruler, this method would
     * turn 15 into 115. If it was -1 (endpoint) it would do nothing.
     *
     * @param position the position to translate
     * @return the translated position.
     */
    protected int translate(int position) {
        if (m_charStart == -1) {
            return (position);
        }

        return (position + Math.abs(m_charStart));
    }

    /**
     * this exactly counteracts the affects of translate
     *
     * @param tpos the translated position
     * @return the position on the screen for the given translated position
     */
    protected int untranslate(int tpos) {
        if (m_charStart == -1) {
            return (tpos);
        }

        return (tpos - m_charStart);
    }

    /**
     * propogate a select action across all editors
     *
     * @param source the source panel of the selection
     * @param pos1   the first position of the selection
     * @param pos2   the end position of the selection
     */
    protected void propogateSelect(EditPanel source, int pos1, int pos2) {
        int end = pos2;
        List olist = m_hList.getSelectedValuesList();
        Object[] all = m_headListModel.toArray();
        EditPanel ep;
        for (Object anAll : all) {
            if (anAll instanceof EditPanel) {
                ep = (EditPanel) anAll;
                ep.clearSelection();
            }
        }
        if (olist.size() > 0) {
            for (Object anO : olist) {
                if (anO instanceof EditPanel) {
                    ep = (EditPanel) anO;
                    EditableSequence s = ep.getSequence();
                    if (pos2 >= s.length()) {
                        pos2 = s.length();
                    } else if (pos2 < 0) {
                        pos2 = 0;
                    }

                    if (pos1 >= s.length()) {
                        pos1 = s.length();
                    } else if (pos1 < 0) {
                        pos1 = 0;
                    }
                    ep.setRelativeSelection(translate(pos1), translate(pos2));
                    pos2 = end;
                }
            }
        } else {
            EditableSequence s = source.getSequence();
            if (pos2 >= s.length()) {
                pos2 = s.length();
            } else if (pos2 < 0) {
                pos2 = 0;
            }
            if (pos1 >= s.length()) {
                pos1 = s.length();
            } else if (pos1 < 0) {
                pos1 = 0;
            }
            source.setRelativeSelection(translate(pos1), translate(pos2));
        }
    }

    /**
     * propogate a select across all editors
     *
     * @param source the source panel of the selection
     * @param x1     the x position of start
     * @param y1     the y position of start
     * @param x2     the x position of stop
     * @param y2     the y position of stop
     */
    protected void propogateSelect(EditPanel source, int x1, int y1, int x2, int y2) {
        x1 = source.graphicsToSequence(x1);
        x2 = source.graphicsToSequence(x2);

        List olist = m_hList.getSelectedValuesList();
        Object[] all = m_headListModel.toArray();
        EditPanel ep;

        for (Object anAll : all) {
            if (anAll instanceof EditPanel) {
                ep = (EditPanel) anAll;
                ep.clearSelection();
            }
        }

        if (olist.size() > 0) {
            for (Object anO : olist) {
                if (anO instanceof EditPanel) {
                    ep = (EditPanel) anO;
                    EditableSequence s = ep.getSequence();
                    int pos1 = x1;
                    int pos2 = x2;
                    if (pos2 >= s.length()) {
                        pos2 = s.length();
                    } else if (pos2 < 0) {
                        pos2 = 0;
                    }
                    if (pos1 >= s.length()) {
                        pos1 = s.length();
                    } else if (pos1 < 0) {
                        pos1 = 0;
                    }
                    ep.setRelativeSelection(translate(pos1), translate(pos2));
                }
            }
        } else {
            EditableSequence s = source.getSequence();
            int pos1 = x1;
            int pos2 = x2;
            if (pos2 >= s.length()) {
                pos2 = s.length();
            } else if (pos2 < 0) {
                pos2 = 0;
            }
            if (pos1 >= s.length()) {
                pos1 = s.length();
            } else if (pos1 < 0) {
                pos1 = 0;
            }
            source.setRelativeSelection(translate(pos1), translate(pos2));
        }
    }

    /**
     * move a section of sequence within the alignment following the rules of the
     * 'glue' mouse mode. This is that a block of sequence will move within a larger
     * gapped region until it comes in contact with another block of sequence which
     * it will adhere to.
     *
     * @param source the source of the edit
     * @param initX  the first x of the sequence to drag (the starting position of
     *               the sequence)
     * @param lastX  the last x of the sequence to drag
     * @param currX  the current x in the sequence, (the initial mouse click)
     * @param y1     the y position of the initial drag
     **/

    protected void revisedGlueSlide(EditPanel source, int initX, int lastX, int currX, int y1) {
        // Bounds check, how big is the gap on either side of the sequence
        // OR do we want to find out how much distance is inbetween each block in only
        // the direction
        // in which it is moving towards. In that case then the calculation of the
        // distance only
        // needs to be done in the if/else statement at the bottom.

        // Translate screen space to sequence space
        int startPosition = source.graphicsToSequence(lastX);
        int endPosition = source.graphicsToSequence(currX);
        int dragDistance = Math.abs(startPosition - endPosition);

        // The Mouse hasn't moved yet so the sequence doesn't need to move
        if (endPosition == startPosition) {
            return;
        }

        // Get an array of each sequence selected if anything is even selected
        FeaturedSequence[] selSeqs = getSelectedSequences();
        // Used to find the outer edges of the sequence
        FeaturedSequence seq;

        // Adjust the placement of sequences
        System.out.println("########################### >>>--->>>");
        for (int i = 0; i < selSeqs.length; i++) {
            seq = selSeqs[i];

            // Check that a sequence has actually been selected and not just a gap
            if (seq.charAt(startPosition) == '-') {
                System.out.println("Running Sequence: " + (i + 1) + "/" + selSeqs.length);
                continue;
            } else {
                System.out.println("Running Sequence: " + (i + 1) + "/" + selSeqs.length);
            }

            // start and end of the block we're dragging
            int seqStartPos = findStartOfSequence(seq, startPosition);
            int seqEndPos = findEndOfSequence(seq, startPosition);

            // Calculate the distances to the next block depending on which direction the
            // sequence is moving towards.
            if (endPosition >= startPosition) { // Moving right, insert at start, remove at end
                int gapToNext = distanceToTheRight(seqEndPos, seq);
                int d = Math.min(gapToNext, dragDistance);

                seq.assertGaps(seqStartPos, seqStartPos + d); // inserts d '-'s at seqStartPos
                seq.removeGap(seqEndPos + d + d + 1, seqEndPos + d + 1); // +d because assertGaps shifted everything
            } else { // Moving left, remove at start, insert at end
                int gapToNext = distanceToTheLeft(seqStartPos, seq);
                int d = Math.min(gapToNext, dragDistance);

                seq.removeGap(seqStartPos, seqStartPos - d);
                seq.assertGaps(seqEndPos - d + 1, seqEndPos + 1);
            }
        }
    }

    private int findStartOfSequence(FeaturedSequence seq, int seqStartPos) {
        // Find the start of this sequence
        while (seqStartPos >= 0) {
            // Continue until a gap is found '-'
            if (seq.charAt(seqStartPos) == '-') {
                seqStartPos++;
                break;
            }

            seqStartPos--;
        }
        return seqStartPos;
    }

    private int findEndOfSequence(FeaturedSequence seq, int seqEndPos) {
        // Find the end position of this sequence
        while (seqEndPos < seq.length()) {
            // Continue until a gap is found '-'
            if (seq.charAt(seqEndPos) == '-') {
                seqEndPos--;
                break;
            }
            seqEndPos++;
        }
        return seqEndPos;
    }

    /**
     * Calculates the gap distance to the left of the selected sequence
     *
     * @param seqStartPos the starting position of the selected sequence
     * @param seq         the whole sequence
     * @return the distance from the selected sequence to the next sequence on the
     *         left
     */
    public int distanceToTheLeft(int seqStartPos, FeaturedSequence seq) {
        int counter = 0;
        while (seqStartPos > 0 && seq.charAt(--seqStartPos) == '-') {
            counter++;
        }
        return counter;
    }

    /**
     * Calculates the gap distance to the right of the selected sequence
     *
     * @param seqEndPos the last position of the selected sequence
     * @param seq       the whole sequence
     * @return the distance from the selected sequence to the next sequence on the
     *         right
     */
    public int distanceToTheRight(int seqEndPos, FeaturedSequence seq) {
        int counter = 0;
        while (seqEndPos < seq.length() - 1 && seq.charAt(++seqEndPos) == '-') {
            counter++;
        }
        return counter;
    }

    /**
     * propogate an edit across all selected sequences
     *
     * @param source the source of the edit
     * @param x1     the x position of start
     * @param y1     the y position of start
     * @param x2     the x position of stop
     * @param y2     the y position of stop
     */
    protected void propogateEdit(EditPanel source, int x1, int y1, int x2, int y2) {
        x1 = source.graphicsToSequence(x1);
        x2 = source.graphicsToSequence(x2);
        boolean prop = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.use.propEdit")).booleanValue();

        List olist = m_hList.getSelectedValuesList();
        FeaturedSequence s;

        if ((olist.size() > 0) && prop) {
            for (Object o : olist) {
                if (o instanceof EditPanel) {
                    s = ((EditPanel) o).getSequence();

                    int pos1 = x1;
                    int pos2 = x2;

                    if (pos2 >= s.length()) {
                        pos2 = s.length();
                    } else if (pos2 < 0) {
                        pos2 = 0;
                    }

                    if (pos1 >= s.length()) {
                        pos1 = s.length();
                    } else if (pos1 < 0) {
                        pos1 = 0;
                    }

                    if (pos2 > pos1) { // Move right add gap
                        s.assertGaps(translate(pos1), translate(pos2));
                    } else if (pos2 < pos1) { // Move left remove gap
                        s.removeGap(translate(pos1), translate(pos2));
                    }
                }
            }
        } else {
            s = source.getSequence();
            int pos1 = x1;
            int pos2 = x2;

            if (pos2 >= s.length()) {
                pos2 = s.length();
            } else if (pos2 < 0) {
                pos2 = 0;
            }

            if (pos1 >= s.length()) {
                pos1 = s.length();
            } else if (pos1 < 0) {
                pos1 = 0;
            }

            if (pos2 > pos1) {
                s.assertGaps(translate(pos1), translate(pos2));
            } else {
                s.removeGap(translate(pos1), translate(pos2));
            }
        }
    }

    /**
     * sets the flag indicating that only the selected region should be printed, not
     * the whole thing.
     *
     * @param newVal the new flag value
     */
    public void setPrintSelection(boolean newVal) {
        m_printSel = newVal;
    }

    /**
     * Export a region of the alignment as an image
     *
     * @param start   the start position in the alignment
     * @param stop    the stop position of the region in the alignment
     * @param width   the width of the image
     * @param spacing the space between 'lines' in the alignment
     * @return an image to be encoded as one wishes
     */
    public BufferedImage exportWrapped(int start, int stop, double width, double spacing) {
        this.setDisplayArea(start, stop);

        ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
        double margin = 10.0;
        double iw = width - (margin * 2);

        if (m_epanels.size() <= 0) {
            BufferedImage i = (BufferedImage) createImage(200, 20);
            Graphics2D g = i.createGraphics();
            g.drawString("No Sequences", 10, 18);

            return (i);
        }

        EditPanel exEp = m_epanels.get(0);

        FontMetrics fm = getFontMetrics(exEp.getDisplayFont());
        FontMetrics hfm = getFontMetrics(m_hList.getFont());
        int fwidth = fm.charWidth('-');

        int hWidth = 0;
        int height = 0;

        for (EditPanel m_epanel : m_epanels) {
            String[] head = m_epanel.getHeaders();

            for (String aHead : head) {
                if (hfm.stringWidth(aHead) > hWidth) {
                    hWidth = hfm.stringWidth(aHead);
                }
            }
        }

        height = m_hList.getPreferredSize().height;

        int firstWidth = ((int) iw - (hWidth + 10)) / fwidth;
        int restWidth = (int) iw / fwidth;

        firstWidth *= fwidth;
        restWidth *= fwidth;

        BufferedImage i = (BufferedImage) createImage((int) iw, height);
        Graphics2D g = i.createGraphics();

        g.setClip(0, 0, firstWidth, height);
        m_editContainer.paint(g);
        g.setClip(firstWidth, 0, (int) iw, height);
        g.setPaint(Color.white);
        g.fillRect(0, 0, (int) iw, height);

        images.add(i);
        g.dispose();

        int cnt = ((int) iw - (hWidth + 10)) / fwidth;
        int step = (int) iw / fwidth;
        int line = 0;

        if (cnt < stop) {
            while (true) {
                cnt += step;

                int myWidth = restWidth;

                if (cnt >= stop) {
                    System.out.print("Width from: " + myWidth);

                    int diff = cnt - stop - 1;
                    myWidth = myWidth - (diff * fwidth);
                    System.out.println(" to: " + myWidth);
                }

                i = (BufferedImage) createImage((int) iw, height);
                g = i.createGraphics();
                g.setClip(0, 0, myWidth, height);
                g.translate(-firstWidth - (line * restWidth), 0);
                m_editContainer.paint(g);
                g.translate(firstWidth + ((line) * restWidth), 0);
                g.setClip(myWidth, 0, (int) iw - myWidth, height);
                ++line;

                g.setPaint(Color.white);
                g.fillRect(0, 0, (int) iw, height);

                images.add(i);
                g.dispose();

                if (cnt >= stop) {
                    break;
                }
            }
        }

        i = new BufferedImage((int) width, ((height + (int) spacing) * images.size()) + (int) margin,
                BufferedImage.TYPE_INT_RGB);
        g = i.createGraphics();
        g.setPaint(Color.white);
        g.fillRect(0, 0, (int) width, ((height + (int) spacing) * images.size()) + (int) margin);
        g.translate((int) margin, (int) margin);
        m_hList.paint(g);
        g.translate(hWidth + 10, 0);

        BufferedImage row = images.get(0);
        g.drawImage(row, 0, 0, this);
        g.translate(-hWidth - 10.0, height + (int) spacing);

        for (int j = 1; j < images.size(); ++j) {
            row = images.get(j);
            g.drawImage(row, 0, 0, this);
            g.translate(0, height + (int) spacing);
        }

        g.dispose();

        return (i);
    }

    /**
     * Export a region of the alignment as an image
     *
     * @param start   the start position in the alignment
     * @param stop    the stop position of the region in the alignment
     * @param width   the width of the image
     * @param spacing the space between 'lines' in the alignment
     * @return an image to be encoded as one wishes
     */
    public BufferedImage exportWrapped(int start, int stop, double width, double spacing, double scalingFactor) {

        this.setDisplayArea(start, stop);

        ArrayList images = new ArrayList();
        double margin = 10.0;
        double iw = width - (margin * 2);

        if (m_epanels.size() <= 0) {
            BufferedImage i = (BufferedImage) createImage(200, 20);
            Graphics2D g = i.createGraphics();
            g.drawString("No Sequences", 10, 18);

            return (i);
        }

        EditPanel exEp = (EditPanel) m_epanels.get(0);
        Font editPanelFont = new Font(exEp.getDisplayFont().getName(), exEp.getDisplayFont().getStyle(),
                (int) (exEp.getDisplayFont().getSize() * scalingFactor));
        FontMetrics fm = getFontMetrics(editPanelFont);
        Font headerFont = new Font(m_hList.getFont().getName(), m_hList.getFont().getStyle(),
                (int) (m_hList.getFont().getSize() * scalingFactor));
        FontMetrics hfm = getFontMetrics(headerFont);
        int fwidth = fm.charWidth('-');
        int hWidth = 0;
        int height = 0;

        for (EditPanel editPanel : m_epanels) {
            String[] head = editPanel.getHeaders();

            for (String aHead : head) {
                if (hfm.stringWidth(aHead) > hWidth) {
                    hWidth = hfm.stringWidth(aHead);
                }
            }

            int[] headerHeights = editPanel.getHeaderHeights(scalingFactor);

            for (int headerHeight : headerHeights) {
                height += headerHeight;
            }
        }

        int firstWidth = ((int) iw - (hWidth + 10)) / fwidth;
        int restWidth = (int) iw / fwidth;

        firstWidth *= fwidth;
        restWidth *= fwidth;

        BufferedImage i = (BufferedImage) createImage((int) iw, height);
        Graphics2D g = i.createGraphics();

        g.setClip(0, 0, firstWidth, height);

        int transYTotal = 0;

        for (Iterator it = m_epanels.iterator(); it.hasNext();) {
            EditPanel editPanel = (EditPanel) it.next();
            Dimension dim = editPanel.renderDisplay(g, scalingFactor);
            transYTotal += dim.getHeight();
            g.translate(0, dim.getHeight());
        }

        g.translate(0, -transYTotal);
        g.setClip(firstWidth, 0, (int) iw, height);
        g.setPaint(Color.white);
        g.fillRect(0, 0, (int) iw, height);
        images.add(i);
        g.dispose();

        int cnt = ((int) iw - (hWidth + 10)) / fwidth;
        int step = (int) iw / fwidth;
        int line = 0;
        cnt = start + cnt;

        if (cnt < stop) {
            while (true) {
                cnt += step;

                int myWidth = restWidth;

                if (cnt >= stop) {
                    int diff = cnt - stop - 1;
                    myWidth = myWidth - (diff * fwidth);
                }

                i = (BufferedImage) createImage((int) iw, height);
                g = i.createGraphics();
                g.setClip(0, 0, myWidth, height);
                g.translate(-firstWidth - (line * restWidth), 0);
                transYTotal = 0;

                for (Iterator it = m_epanels.iterator(); it.hasNext();) {
                    EditPanel editPanel = (EditPanel) it.next();
                    Dimension dim = editPanel.renderDisplay(g, scalingFactor);
                    transYTotal += dim.getHeight();
                    g.translate(0, dim.getHeight());
                }

                g.translate(0, -transYTotal);
                g.translate(firstWidth + ((line) * restWidth), 0);
                g.setClip(myWidth, 0, (int) iw - myWidth, height);
                ++line;

                g.setPaint(Color.white);
                g.fillRect(0, 0, (int) iw, height);

                images.add(i);
                g.dispose();

                if (cnt >= stop) {
                    break;
                }
            }
        }

        i = new BufferedImage((int) width, ((height + (int) spacing) * images.size()) + (int) margin,
                BufferedImage.TYPE_INT_RGB);

        g = i.createGraphics();
        g.setPaint(Color.white);
        g.fillRect(0, 0, (int) width, ((height + (int) spacing) * images.size()) + (int) margin);
        g.translate((int) margin, (int) margin);

        g.setFont(headerFont);
        g.setColor(Color.BLACK);

        int headerBottom = 0;

        for (Iterator it = m_epanels.iterator(); it.hasNext();) {
            EditPanel ep = (EditPanel) it.next();
            String[] headers = ep.getHeaders();
            int[] headerHeights = ep.getHeaderHeights(scalingFactor);

            for (int j = 0; j < headers.length; j++) {
                if (headerHeights[j] == 0) {
                    continue;
                }

                int yPos = headerBottom + headerHeights[j] - (headerHeights[j] - hfm.getAscent()) / 2;
                headerBottom += headerHeights[j];
                g.drawString(headers[j], 5, yPos);
            }
        }

        g.translate(hWidth + 10, 0);

        BufferedImage row = (BufferedImage) images.get(0);
        g.drawImage(row, 0, 0, this);
        g.translate(-hWidth - 10.0, height + (int) spacing);

        for (int j = 1; j < images.size(); ++j) {
            row = (BufferedImage) images.get(j);
            g.drawImage(row, 0, 0, this);
            g.translate(0, height + (int) spacing);
        }
        g.dispose();

        return (i);
    }

    /**
     * print this panel
     *
     * @param graphics  the graphics context to print to
     * @param pf        the format of the page
     * @param pageIndex the index of the page to print
     * @return
     */
    public int print(Graphics graphics, PageFormat pf, int pageIndex) {

        int start = 0;
        int stop = 0;

        if (m_printSel) {
            if (m_selectedSequence >= 0) {
                EditPanel p = (EditPanel) m_epanels.get(m_selectedSequence);
                Location l = p.getRelativeSelection();
                start = l.getMin();
                stop = l.getMax();
            } else {
                UITools.showWarning("Please select a region to annotate.", this);

                return (NO_SUCH_PAGE);
            }
        }

        System.out.println("Print Selection: " + m_printSel);

        System.out.println(start + " -> " + stop);

        double ix = pf.getImageableX();
        double iy = pf.getImageableY();
        double iw = pf.getImageableWidth();
        double ih = pf.getImageableHeight();

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(ix, iy);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        ArrayList headers = new ArrayList();
        ArrayList editors = new ArrayList();

        FeaturedSequenceWrapper[] seqs = getVisibleSequenceWrappers();

        for (int i = 0; i < seqs.length; ++i) {
            final EditPanel ep = new EditPanel(seqs[i].sequence, seqs[i].dbName);
            ep.setChannelPreferences(((EditPanel) m_epanels.get(i)).getChannelPreferences());
            ep.setDisplayFont(PRINT_FONT);
            ep.setChannelFont(PRINT_FONT);
            ep.setChannelHeight(CHAN_HEIGHT);
            ep.setChannelSpacing(CHAN_SPACE);

            final HeaderPanel hp = new HeaderPanel(ep);
            hp.setDisplayFont(PRINT_FONT);

            headers.add(hp);
            editors.add(ep);
        }

        FontMetrics fm = getFontMetrics(PRINT_FONT);
        int fwidth = fm.charWidth('-');

        int hWidth = 0;
        int height = 0;

        for (int i = 0; i < editors.size(); ++i) {
            String[] head = ((EditPanel) editors.get(i)).getHeaders();
            int[] heights = ((EditPanel) editors.get(i)).getHeaderHeights();

            for (int j = 0; j < head.length; ++j) {
                if (fm.stringWidth(head[j]) > hWidth) {
                    hWidth = fm.stringWidth(head[j]);
                }

                height += heights[j];
            }
        }

        int firstWidth = (int) ((iw - (double) hWidth + 10.0) / (double) fwidth);
        int restWidth = (int) (iw / (double) fwidth);
        int charsPerPage = (int) (((double) restWidth * ((ih / ((double) height + 5)) - 1.0)) + (double) firstWidth);

        System.out.println("Cpp " + charsPerPage);

        if (((charsPerPage * pageIndex) + start) > stop) {
            if (m_lastPage) {
                m_lastPage = false;
                System.out.println("now returning no page");

                return (NO_SUCH_PAGE);
            } else {
                m_lastPage = true;
            }
        }

        System.out.println(((charsPerPage * pageIndex) + start) + " " + m_lastPage);

        int thisStart = (pageIndex * charsPerPage) + start;
        System.out.println("This Page(" + pageIndex + ") Starts: " + thisStart);

        for (int i = 0; i < editors.size(); ++i) {
            ((HeaderPanel) headers.get(i)).paintComponent(g2d);
            g2d.translate(0, ((HeaderPanel) headers.get(i)).getHeight());
        }

        g2d.translate(hWidth + 10, -height);

        for (Object editor : editors) {
            EditPanel ep = ((EditPanel) editor);
            int pheight = 0;
            int[] heights = ep.getHeaderHeights();

            for (int height1 : heights) {
                pheight += height1;
            }

            ep.paintComponent(g2d);
            g2d.translate(0, pheight);
        }

        g2d.translate(-(hWidth + 10), 5);

        int k = Math.min(thisStart + firstWidth, stop);

        if (k == stop) {
            return (PAGE_EXISTS);
        }

        for (int j = 0; j < (int) (ih / ((double) height + 5)); ++j) {
            for (Object editor : editors) {
                EditPanel ep = ((EditPanel) editor);
                int pheight = 0;
                int[] heights = ep.getHeaderHeights();

                for (int height1 : heights) {
                    pheight += height1;
                }

                ep.paintComponent(g2d);
                g2d.translate(0, pheight);

                if (Math.min(k + ((j + 1) * restWidth), stop) == stop) {
                    return (PAGE_EXISTS);
                }
            }
        }

        System.out.println("Going on to next page...");
        g2d.dispose();

        return (PAGE_EXISTS);
    }

    /**
     * Update the difference lists for all displayed sequences
     */
    protected void updateDifferenceLists() {
        if (!m_updateDiffs) {
            return;
        }

        FeaturedSequence[] seqs = getVisibleSequences();
        m_colorScheme.setSequences(seqs);

        if (m_compType == CONSENSUS_COMPARISON) {

            String consSeq = m_consensus.getSequence(0, m_consensus.getLength() - 1);

            for (FeaturedSequence seq : seqs) {
                FeatureTools.refreshNTDifferenceList(seq, consSeq);
            }
        } else if (m_compType == PAIRWISE_COMPARISON) {

            for (int i = 0; i < (seqs.length - 1); ++i) {
                FeatureTools.refreshNTDifferenceList(seqs[i], seqs[i + 1].toString());
            }

            if (seqs.length > 0) {
                FeatureTools.setNTDifferenceList(seqs[seqs.length - 1], "");
            }
        } else if (m_compType == MODEL_COMPARISON) {

            for (FeaturedSequence seq : seqs) {
                FeatureTools.refreshNTDifferenceList(seq, seqs[0].toString());
            }
        }
    }

    /**
     * Clear the selections from all underlying editors
     */
    protected void clearSelection() {
        for (EditPanel m_epanel : m_epanels) {
            (m_epanel).clearSelection();
        }
    }

    /**
     * Set the sequence currently selected -- this is only useful when the list of
     * sequence headers has no selections in it. Otherwise that should be used as
     * the overriding authority on what is selected.
     *
     * @param index the sequence
     */
    protected void setSelectedSequence(int index) {
        m_selectedSequence = index;
    }

    /**
     * Get the sequence currently selected
     *
     * @return the sequence index
     */
    protected int getSelectedSequence() {
        return (m_selectedSequence);
    }

    /**
     * Scroll to the givne location in the given sequence
     *
     * @param sequence The sequence to scroll in
     * @param location The location to scroll to
     * @param gapped   If true, the frame will scroll to the relative gapped
     *                 location, otherwise it'll scroll to the absolute sequence
     *                 position.
     */
    protected void scrollToLocation(int sequence, int location, boolean gapped) {
        if (sequence >= m_epanels.size()) {
            return;
        }

        location = untranslate(location);

        EditPanel p = (EditPanel) m_epanels.get(sequence);
        EditableSequence s = p.getSequence();

        // location is 1-based
        if (location <= 0) {
            location = 0;
        } else if (!gapped) {
            // getRelativeLocation returns a 0-based location
            location = s.getRelativePosition(location) + 1;
        }

        // the scrollbar takes a 0-based index
        m_scroller.getHorizontalScrollBar().setValue(p.sequenceToGraphics(location - 1));
    }

    /**
     * Select the given region of the given sequence
     *
     * @param sequence the index of the sequence to select
     * @param start    the leftmost position
     * @param stop     the rightmost position
     * @param gapped   if true, the start and stop represent gapped positions,
     *                 otherwise they represent absolute sequence positions
     * @param prop     if true, propagate the selection across all other sequences
     */
    protected void selectRegion(int sequence, int start, int stop, boolean gapped, boolean prop, int[] selected) {
        if (sequence >= m_epanels.size()) {
            return;
        }

        if (sequence < 0) {
            sequence = 0;
        }

        EditPanel p = (EditPanel) m_epanels.get(sequence);
        EditableSequence s = p.getSequence();
        EditPanel[] seqs = getVisibleEditors();

        for (EditPanel seq : seqs) {
            seq.clearSelection();
        }

        if (!gapped) {
            start = s.getRelativePosition(start);
            stop = s.getRelativePosition(stop);
        } else {
            // compensate for positions being counted from
            // zero internally, but 1 from the user's perspective
            start = start - 1;
            stop = stop - 1;
        }

        for (int aSelected : selected) {
            seqs[aSelected].setRelativeSelection(start, stop);
        }

        p.setRelativeSelection(start, stop);

    }

    /**
     * Set the 'visible' flags for the amino acid translation for the given set of
     * sequences.
     *
     * @param seqs    The sequences to flag
     * @param visible if true, frames will be displayed, otherwise they will be
     *                hidden
     */
    //
    // This method was protected and was changed to public for better and easier
    // integration in eGATU. Francesco Marass, 25 June 2008
    //
    public void setFramesVisible(FeaturedSequence[] seqs, boolean visible) {
        for (int i = 0; i < seqs.length; ++i) {
            for (int j = 0; j < m_epanels.size(); ++j) {
                EditPanel s = (EditPanel) m_epanels.get(j);

                if (s.getSequence() == seqs[i]) {
                    s.setFrameVisible(0, visible);
                    s.setFrameVisible(1, visible);
                    s.setFrameVisible(2, visible);
                    s.resetView();

                    break;
                }
            }
        }
    }

    /**
     * create the sequence tool bar
     *
     * @return the toolbar
     */
    protected JToolBar createSequenceTool() {
        JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        bar.setOpaque(true);
        bar.setFloatable(false);

        JButton b;
        b = new JButton(Icons.getInstance().getIcon("SKIPUP"));
        b.setToolTipText("Move Marked Sequence(s) to Top");
        b.setMaximumSize(new Dimension(22, 22));
        b.setPreferredSize(new Dimension(22, 22));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                moveSelectedSequences(SwingConstants.TOP);
            }
        });
        bar.add(b);
        b = new JButton(Icons.getInstance().getIcon("MOVEUP"));
        b.setToolTipText("Move Marked Sequence(s) Up");
        b.setMaximumSize(new Dimension(22, 22));
        b.setPreferredSize(new Dimension(22, 22));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                moveSelectedSequences(SwingConstants.PREVIOUS);
            }
        });
        bar.add(b);
        b = new JButton(Icons.getInstance().getIcon("MOVEDOWN"));
        b.setToolTipText("Move Marked Sequence(s) Down");
        b.setMaximumSize(new Dimension(22, 22));
        b.setPreferredSize(new Dimension(22, 22));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                moveSelectedSequences(SwingConstants.NEXT);
            }
        });
        bar.add(b);
        b = new JButton(Icons.getInstance().getIcon("SKIPDOWN"));
        b.setToolTipText("Move Marked Sequence(s) to Bottom");
        b.setMaximumSize(new Dimension(22, 22));
        b.setPreferredSize(new Dimension(22, 22));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                moveSelectedSequences(SwingConstants.BOTTOM);
            }
        });
        bar.add(b);
        bar.addSeparator();

        b = new JButton(Icons.getInstance().getIcon("HIDE"));
        b.setToolTipText("Hide Marked Sequences");
        b.setMaximumSize(new Dimension(22, 22));
        b.setPreferredSize(new Dimension(22, 22));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                hideSelectedSequences();
            }
        });
        bar.add(b);
        b = new JButton(Icons.getInstance().getIcon("SHOW"));
        b.setToolTipText("Set Sequence Filter");
        b.setMaximumSize(new Dimension(22, 22));
        b.setPreferredSize(new Dimension(22, 22));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                ShowSequencesDialog f = new ShowSequencesDialog(PrimaryPanel.this);
                f.setVisible(true);
            }
        });
        bar.add(b);

        bar.addSeparator();
        b = new JButton(Icons.getInstance().getIcon("FRAMES"));
        m_frameButton = b;
        b.setToolTipText("Show/Hide Translation");
        b.setMaximumSize(new Dimension(22, 22));
        b.setPreferredSize(new Dimension(22, 22));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                FeaturedSequence[] seqs = getSelectedSequences();

                if (seqs.length == 0) {
                    UITools.showWarning("Mark one or more sequences by clicking the sequence name.", null);

                    return;
                }

                boolean origVal = false;

                for (int i = 0; i < m_epanels.size(); ++i) {
                    EditPanel p = (EditPanel) m_epanels.get(i);

                    if (p.getSequence() == seqs[0]) {
                        origVal = p.isFrameVisible(0);

                        break;
                    }
                }

                setFramesVisible(seqs, !origVal);
                refreshState();
            }
        });
        bar.add(b);

        bar.addSeparator();
        b = new JButton(Icons.getInstance().getIcon("SWAP"));
        m_frameButton = b;
        b.setToolTipText("Reorder Sequences");
        b.setMaximumSize(new Dimension(22, 22));
        b.setPreferredSize(new Dimension(22, 22));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                final FeaturedSequence[] sequences = getSequences();

                final JDialog disp = new JDialog();
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                int w = getWidth();
                int h = getHeight();
                int x = (dim.width - w);
                int y = (dim.height - h);
                disp.setBounds(x, y, 350, 600);
                disp.setResizable(true);

                JPanel main = new JPanel(new BorderLayout());
                final JPanel content = new JPanel();
                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                content.setBorder(
                        BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Re-order Sequences"),
                                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
                JScrollPane scroll = new JScrollPane(content);
                main.add(scroll, BorderLayout.CENTER);

                JList list = new ReorderableJList();
                DefaultListModel model = new DefaultListModel();
                list.setModel(model);

                // setup sequence
                for (FeaturedSequence sequence : sequences) {
                    model.addElement(sequence.getName());
                }
                content.add(list);

                model.addListDataListener(new ListDataListener() {
                    int rem = -1;

                    public void intervalAdded(ListDataEvent e) {
                        if (rem >= 0) {
                            for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                                if (i != rem) {
                                    EditPanel editor = (EditPanel) m_headListModel.get(rem);
                                    m_epanels.remove(rem);
                                    m_headListModel.remove(rem);
                                    m_editContainer.remove(rem);
                                    m_epanels.add(i, editor);
                                    m_headListModel.add(i, editor);
                                    m_editContainer.add(editor, i);

                                }
                            }
                        }
                        rem = -1;
                    }

                    public void intervalRemoved(ListDataEvent e) {
                        for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                            rem = i;
                        }
                    }

                    public void contentsChanged(ListDataEvent arg0) {
                    }
                });

                disp.add(main);

                // Close Buttom
                JPanel btns = new JPanel();
                btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
                btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
                JButton close = new JButton("Close");
                btns.add(Box.createHorizontalStrut(3));
                btns.add(close);
                main.add(btns, BorderLayout.SOUTH);
                close.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        disp.dispose();
                    }
                });

                disp.setVisible(true);

            }
        });
        bar.add(b);

        return (bar);
    }

    /**
     * Handles making the consensus panels appear and disappear
     */

    protected void cons_visible(boolean visible) {

        if (visible) {
            r_panel.setBottomComponent(m_consScroll);
            r_panel.setDividerLocation(this.getHeight() / 2);
            l_panel.setBottomComponent(pan2);
            l_panel.setDividerLocation(this.getHeight() / 2);
            m_consDisp.setHeight(m_consScroll.getViewport().getHeight());
        } else {
            r_panel.remove(m_consScroll);
            r_panel.setDividerLocation(this.getHeight());
            r_panel.remove(pan2);
            l_panel.setDividerLocation(this.getHeight());
        }

        m_consDisp.setVisible(visible);
        m_consScroll.setVisible(visible);
        cons_tool.setVisible(visible);
        m_consHead.setVisible(visible);
        pan2.setVisible(visible);
        repaint();
    }

    protected void rna_visible(boolean visible) {

        if (!visible) {
            r_panel.remove(m_rnaScroll);
            r_panel.setDividerLocation(this.getHeight());
            l_panel.remove(m_rnaScroll);
            l_panel.setDividerLocation(this.getHeight());
        } else {
            r_panel.setBottomComponent(m_rnaScroll);
            r_panel.setDividerLocation(this.getHeight() / 2);
            l_panel.setBottomComponent(pan3);
            l_panel.setDividerLocation(this.getHeight() / 2);
            m_rnaDisp.setInfoHeight(this.getHeight() / 2);
            m_rnaHead.setSize(rna_tool.getWidth(), this.getHeight() / 2);
        }

        m_rnaDisp.setVisible(visible);
        m_rnaScroll.setVisible(visible);
        rna_tool.setVisible(visible);
        m_rnaHead.setVisible(visible);
        pan3.setVisible(visible);
        repaint();
    }

    /**
     * Init the user interface for this component
     */
    protected void initUI() {
        int[] lanePrefs = { EditPanel.SCALE_CHANNEL, EditPanel.ALIGN_CHANNEL, EditPanel.ABSCALE_CHANNEL,
                EditPanel.ACID_CHANNEL, EditPanel.DIFF_CHANNEL, EditPanel.EVENT_CHANNEL, EditPanel.SEARCH_CHANNEL };
        setChannelPreferences(lanePrefs);

        setLayout(new BorderLayout());
        setBackground(Color.white);
        setOpaque(true);

        m_hSpacer.setLayout(new BorderLayout());
        m_hSpacer.setBackground(Color.white);
        m_hSpacer.setOpaque(true);
        m_eSpacer.setLayout(new BorderLayout());
        m_eSpacer.setBackground(Color.white);
        m_eSpacer.setOpaque(true);

        JPanel headers = m_headContainer;
        headers.setBackground(Color.white);
        headers.setOpaque(true);
        headers.setLayout(new BorderLayout());

        JList hlist = m_hList;
        hlist.setFont(CHAN_FONT);
        hlist.setCellRenderer(new HeaderCellRenderer(PANEL_SPACE));
        hlist.setSelectionForeground(Color.white);
        hlist.setSelectionBackground(Color.black);

        headers.add(hlist, BorderLayout.CENTER);
        m_seqTools = createSequenceTool();

        hScroll = new JScrollPane(headers);
        hScroll.setBackground(Color.white);
        hScroll.setOpaque(true);
        hScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        // HACK: this is a workaround for a bug that causes misallignments
        hScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        hScroll.setWheelScrollingEnabled(false);

        JPanel editors = m_editContainer;
        eScroll = new JScrollPane(editors);
        editors.setLayout(new VerticalFlowLayout(PANEL_SPACE));
        editors.setBackground(Color.white);
        editors.setOpaque(true);
        m_scroller = eScroll;
        eScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        eScroll.getVerticalScrollBar().setUnitIncrement(5);
        eScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        m_consScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        m_consScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        m_rnaScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        m_rnaScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        m_consScroll.setBackground(Color.WHITE);
        m_rnaScroll.setBackground(Color.WHITE);

        eScroll.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent ev) {
                Point p1 = eScroll.getViewport().getViewPosition();
                Point p2 = m_consScroll.getViewport().getViewPosition();
                Point p3 = m_rnaScroll.getViewport().getViewPosition();
                // Fixes alignment issue that occurs when only eScroll has a horizontal scrollbar.
                m_consScroll.getViewport().setSize(eScroll.getViewport().getWidth(),
                        m_consScroll.getViewport().getHeight());
                m_rnaScroll.getViewport().setSize(eScroll.getViewport().getWidth(),
                        m_rnaScroll.getViewport().getHeight());

                m_consScroll.getViewport().setViewPosition(new Point((int) p1.getX(), (int) p2.getY()));

                if (eScroll.getViewport().getViewPosition().getX() != m_consScroll.getViewport().getViewPosition()
                        .getX()) {
                    System.out.print(p1.getX() + " = ");
                    System.out.println(eScroll.getViewport().getViewPosition().getX() + ";"
                            + m_consScroll.getViewport().getViewPosition().getX());
                }

                m_rnaScroll.getViewport().setViewPosition(new Point((int) p1.getX(), (int) p3.getY()));

                try {
                    m_rnaOvFrame.min_val = (int) (p1.getX() / getFontMetrics(SEQ_FONT).charWidth('?'));
                    m_rnaOvFrame.max_val = m_rnaOvFrame.min_val + eScroll.getWidth() / m_rnaDisp.m_fontWidth;
                    m_rnaOvFrame.repaint();
                } catch (NullPointerException e) {
                }

                Rectangle r = eScroll.getViewport().getViewRect();
                setPreviewView(r.x, r.x + r.width);
            }
        });

        eScroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent ev) {
                Point p1 = eScroll.getViewport().getViewPosition();
                Point p2 = hScroll.getViewport().getViewPosition();
                hScroll.getViewport().setViewPosition(new Point((int) p2.getX(), (int) p1.getY()));
            }
        });

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        split.setDividerSize(3);

        // Set the desired font for the display
        m_consDisp.setDisplayFont(SEQ_FONT);
        m_consHead.setDisplayFont(CHAN_FONT);
        m_rnaDisp.setDisplayFont(SEQ_FONT);

        // Give the plot tools a parent to grab onto
        m_consDisp.setPrimaryPanel(this);
        m_rnaDisp.setPrimaryPanel(this);

        // Initialize the main holders
        r_panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        l_panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);

        r_panel.setTopComponent(eScroll);

        split.setRightComponent(r_panel);

        Border blackline = BorderFactory.createLineBorder(Color.black);

        pan1 = new JPanel();
        pan1.setLayout(new BorderLayout());
        pan2 = new JPanel();
        pan2.setLayout(new BoxLayout(pan2, BoxLayout.Y_AXIS));
        pan2.setBackground(Color.WHITE);
        pan3 = new JPanel();
        pan3.setLayout(new BorderLayout());
        pan3.setBackground(Color.WHITE);
        l_panel.setDividerLocation(this.getHeight() / 2);
        m_consHeadScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        m_consHeadScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        m_rnaHeadScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        m_rnaHeadScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        cons_tool = new Addon_Tool(true, this);
        rna_tool = new Addon_Tool(false, this);

        pan1.add(hScroll);
        pan2.add(cons_tool, BorderLayout.NORTH);
        pan2.add(m_consHead, BorderLayout.CENTER);
        pan3.add(rna_tool, BorderLayout.NORTH);
        pan3.add(m_rnaHead, BorderLayout.CENTER);

        l_panel.setTopComponent(pan1);

        split.setLeftComponent(l_panel);
        l_panel.getLeftComponent().setMaximumSize(new Dimension(150, 0));
        split.setDividerLocation(SPLIT_LOC);
        add(split, BorderLayout.CENTER);
        add(m_seqTools, BorderLayout.WEST);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * init main listeners for this component
     */
    protected void initListeners() {

        // left split pane:
        pan1.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                r_panel.setDividerLocation(l_panel.getDividerLocation());
            }
        });

        // right split pane:
        eScroll.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                l_panel.setDividerLocation(r_panel.getDividerLocation());
            }
        });

        m_consScroll.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                m_consDisp.setHeight(m_consScroll.getViewport().getHeight());
                m_consDisp.setSize(m_consDisp.getWidth(), m_consDisp.getHeight());
            }
        });

        m_rnaScroll.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                m_rnaDisp.setSize(m_rnaDisp.getWidth(), m_rnaScroll.getViewport().getHeight());
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent ev) {
                int height = ev.getComponent().getHeight();
                if (m_consDisp.isVisible()) {
                    m_consDisp.setHeight(m_consScroll.getViewport().getHeight());
                    m_consDisp.setSize(m_consDisp.getWidth(), m_consDisp.getHeight());
                    getPrimaryPanel().l_panel.setDividerLocation(height / 2);
                    getPrimaryPanel().r_panel.setDividerLocation(height / 2);
                } else if (m_rnaDisp.isVisible()) {
                    m_rnaDisp.setSize(m_rnaDisp.getWidth(), height / 2);
                    m_rnaDisp.m_iHeight = height / 2;
                    pan3.setSize(pan3.getWidth(), height / 2);
                    getPrimaryPanel().l_panel.setDividerLocation(height / 2);
                    getPrimaryPanel().r_panel.setDividerLocation(height / 2);
                }

                getPrimaryPanel().repaint();

                revalidate();
            }
        });
    }

    public PrimaryPanel getPrimaryPanel() {
        return this;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public java.util.List getGapCounts() {
        ArrayList results = new ArrayList();

        for (Iterator it = m_epanels.iterator(); it.hasNext();) {
            EditPanel epanel = (EditPanel) it.next();
            SequenceGapCount sgc = new SequenceGapCount();
            sgc.sequenceName = epanel.getSequence().getName();
            sgc.gapCount = epanel.getGapCount();
            results.add(sgc);
        }

        return (results);
    }

    public static class FeaturedSequenceWrapper {
        public FeaturedSequence sequence;
        public String dbName;
    }

    public static class SequenceGapCount {
        public String sequenceName;
        public int gapCount;
    }

    public void displayReadingFrames() {
        FeaturedSequence[] seqs = getSelectedSequences();

        if (seqs.length == 0) {
            UITools.showWarning("Mark one or more sequences by clicking the sequence name.", null);

            return;
        }

        boolean origVal = false;

        for (int i = 0; i < m_epanels.size(); ++i) {
            EditPanel p = (EditPanel) m_epanels.get(i);

            if (p.getSequence() == seqs[0]) {
                origVal = p.isFrameVisible(0);

                break;
            }
        }

        setFramesVisible(seqs, !origVal);
        refreshState();
    }

    /**
     * Find differences between selected sequences
     */
    protected final List<LoggingProcess> loggingProcesses = new ArrayList<LoggingProcess>();

    protected void findDifferences(final int openIndex, final Window parent) {
        final Thread t = new Thread() {
            public void run() {
                EditPanel p = (EditPanel) m_epanels.get(m_selectedSequence);

                DifferencesEditorPane diffPane = new DifferencesEditorPane(parent, PrimaryPanel.this, p, openIndex,
                        loggingProcesses);
                diffPane.setSize(new Dimension(1050, 500));
                diffPane.setLocationRelativeTo(null);
                diffPane.setVisible(true);
            }
        };
        t.start();

        refreshState();

    }

    /**
     * Show these differences as a log file
     */
    protected void showLogDifferences() {
        final LoggingProcess diffPane = (LoggingProcess) JOptionPane.showInputDialog(this, "Choose Process:",
                "View Log", JOptionPane.PLAIN_MESSAGE, null, loggingProcesses.toArray(),
                (loggingProcesses.size() <= 0 ? null : loggingProcesses.get(0)));
        if (diffPane == null) {
            return;
        }
        // Set up log pane
        final JDialog logPane = new JDialog();
        logPane.setSize(new Dimension(840, 600));
        logPane.setLocationRelativeTo(null);
        logPane.setTitle(diffPane.toString());

        // Set up panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        logPane.add(mainPanel);
        JPanel textPanel = new JPanel(new BorderLayout());

        // Set text area
        final JTextArea m_comment = new JTextArea();
        m_comment.setLineWrap(true);
        m_comment.setEditable(false);
        JScrollPane csp = new JScrollPane(m_comment);
        textPanel.add(csp, BorderLayout.CENTER);
        textPanel.setBorder(BorderFactory.createTitledBorder("Show Differences Log"));
        // add the text
        m_comment.setText(diffPane.getLog());
        mainPanel.add(textPanel);

        // Close Buttom
        JPanel btns = new JPanel();
        btns.setLayout(new FlowLayout(FlowLayout.LEFT));
        btns.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mainPanel.add(btns, BorderLayout.SOUTH);

        final JButton stop = new JButton("Stop Process");
        btns.add(stop);
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                diffPane.stopProcess();
            }
        });
        if (!diffPane.isAlive()) {
            stop.setEnabled(false);
        }
        // Plot findDifferences
        final JButton plot = new JButton("Plot Differences");
        btns.add(plot);
        plot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Frame findDifferencesChart = DifferencesChart.createFindDifferencesChart(diffPane.toString(),
                        DifferencesEditorPane.readDiffLog(diffPane.getLog()), diffPane.log);
                findDifferencesChart.setVisible(true);
            }
        });

        // Export to VGO button
        // Converts logfile to vgo file
        // TODO: What is this actually used for/what is this supposed to do?
        JButton vgoExport = new JButton("Export to VGO");
        // TODO: Re-enable when working correctly...
        // btns.add(vgoExport);
        vgoExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JFileChooser fc = new JFileChooser(m_currentDirectory);
                fc.setDialogTitle("Save to VGO File");

                MultiFileFilter ff = new MultiFileFilter("VGO Files");
                ff.addExtension("vgo");
                fc.addChoosableFileFilter(ff);

                if (fc.showDialog(PrimaryPanel.this, "Export") == JFileChooser.APPROVE_OPTION) {
                    final File f = fc.getSelectedFile();

                    if (!f.exists()) {
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    try {
                        String str = diffPane.getLog();
                        String strVGO = "> ";

                        Pattern comment_pattern = Pattern.compile("([\\s\\S]*)--");
                        Matcher m = comment_pattern.matcher(str);
                        if (m.find()) {
                            strVGO += m.group(1);
                        }
                        strVGO = strVGO.replaceAll("--", "");
                        strVGO = strVGO.replaceAll("\n", ", ") + "\n";

                        Pattern main_pattern = Pattern.compile("([0-9]+), ");
                        m = main_pattern.matcher(str);
                        while (m.find()) {

                        }
                        main_pattern = Pattern.compile("([0-9]+)\n");
                        m = main_pattern.matcher(str);
                        if (m.find()) {

                        }

                        FileWriter fwrite = new FileWriter(f);
                        fwrite.write(strVGO);
                        fwrite.flush();
                        fwrite.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        final JButton trim = new JButton("List SNP positions only");
        // creates a new window displaying only column positions of SNPs
        trim.setToolTipText("Press this button to open a new window displaying columns and whitespace only.\n"
                + "This is useful for copy and pasting into 'Delete Specified Columns and Export' window.");
        btns.add(trim);
        trim.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                final JDialog trimDialog = new JDialog((JFrame) null, "Trimmed Data");
                trimDialog.setSize(new Dimension(840, 600));
                trimDialog.setLocationRelativeTo(null);

                // Set up panel
                JPanel mainPanel = new JPanel(new BorderLayout());
                trimDialog.add(mainPanel);
                JPanel textPanel = new JPanel(new BorderLayout());

                // Set text area
                final JTextArea trimmedTextArea = new JTextArea();
                trimmedTextArea.setLineWrap(true);
                trimmedTextArea.setWrapStyleWord(true);
                trimmedTextArea.setEditable(true);
                JScrollPane csp = new JScrollPane(trimmedTextArea);
                textPanel.add(csp, BorderLayout.CENTER);
                textPanel.setBorder(BorderFactory.createTitledBorder("SNP Positions only"));
                // add the text
                String rawLines[] = diffPane.getLog().split("\\r?\\n");
                String selectedLines = Stream.of(rawLines).parallel().map(line -> {
                    StringBuilder number = new StringBuilder();
                    // check if line starts with number
                    if (Character.isDigit(line.charAt(0))) {
                        number.append(line.charAt(0));
                        int i = 1;
                        // add while charAt(i) is still numeric - will break when charAt(i) is ':';
                        while (Character.isDigit(line.charAt(i))) {
                            number.append(line.charAt(i));
                            i++;
                        }
                    }
                    return number;
                }).collect(Collectors.joining(" "));
                trimmedTextArea.setText(selectedLines);
                mainPanel.add(textPanel);

                trimmedTextArea.setText(selectedLines);
                mainPanel.add(textPanel);

                trimDialog.setVisible(true);
            }
        });

        JButton close = new JButton("Close");
        btns.add(close);
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                logPane.dispose();
            }
        });
        // refresh timer
        Action updateLog = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!diffPane.isAlive()) {
                    stop.setEnabled(false);
                }
                if (!diffPane.getLog().equals(m_comment.getText())) {
                    m_comment.setText(diffPane.getLog());
                }
                if (!diffPane.toString().equals(logPane.getTitle())) {
                    logPane.setTitle(diffPane.toString());
                }
            }
        };
        new Timer(10, updateLog).start();

        // show log pane
        logPane.setVisible(true);

    }

    /**
     * Counts the column types Divided into columns w/ gaps, one, two, three, or
     * four nucleotides
     */
    protected void getCounts() {

        // get the sequences
        FeaturedSequence[] seqs = getVisibleSequences();

        int gapNum = 0;
        int one = 0, two = 0, three = 0, four = 0;
        int ac = 0, ag = 0, at = 0, cg = 0, ct = 0, gt = 0;

        // Loop through the columns
        for (int i = 0; i < seqs[0].length(); i++) {

            String nucleotides = "";

            // Loop through the sequences
            seqsloop: for (int j = 0; j < seqs.length; j++) {

                // increment gapNum and break when break found
                if (seqs[j].charAt(i) == '-') {
                    gapNum++;
                    // don't want to count nucleotides when gap exists
                    nucleotides = "";
                    break seqsloop;
                }

                // Checks for unique nucleotide and adds to nucleotides if exists
                boolean change = true;
                nucleoloop: for (int k = 0; k < nucleotides.length(); k++) {
                    if (seqs[j].charAt(i) == nucleotides.charAt(k)) {
                        change = false;
                        break nucleoloop;
                    }
                }
                if (change && seqs[j].charAt(i) != ' ')
                    nucleotides += seqs[j].charAt(i);

            }

            // Checks nucleotides length for number of nucleotides in column
            switch (nucleotides.length()) {
            case 1:
                one++;
                break;
            case 2:
                two++;
                if ((nucleotides.indexOf("AC") > -1) || (nucleotides.indexOf("CA") > -1)) {
                    ac++;
                } else if ((nucleotides.indexOf("AG") > -1) || (nucleotides.indexOf("GA") > -1)) {
                    ag++;
                } else if ((nucleotides.indexOf("AT") > -1) || (nucleotides.indexOf("TA") > -1)) {
                    at++;
                } else if ((nucleotides.indexOf("CG") > -1) || (nucleotides.indexOf("GC") > -1)) {
                    cg++;
                } else if ((nucleotides.indexOf("CT") > -1) || (nucleotides.indexOf("TC") > -1)) {
                    ct++;
                } else if ((nucleotides.indexOf("GT") > -1) || (nucleotides.indexOf("TG") > -1)) {
                    gt++;
                }
                break;
            case 3:
                three++;
                break;
            case 4:
                four++;
                break;
            default:
                break;
            }

        }

        final JDialog countDialog = new JDialog((JFrame) null, "Get Counts");

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Alignment Counts"));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        countDialog.add(mainPanel);

        // Total # of columns
        System.out.println("Total: " + seqs[0].length());
        JLabel total = new JLabel("Total # of Columns: " + seqs[0].length());
        mainPanel.add(total);

        // # of columns that contain a gap
        System.out.println("Gaps: " + gapNum);
        JLabel gaps = new JLabel("# of Columns that Contain a Gap: " + gapNum);
        mainPanel.add(gaps);

        // # of columns that contain a single nucleotide
        System.out.println("One: " + one);
        JLabel oneLabel = new JLabel("# of Columns that Contain a Single Nucleotide: " + one);
        mainPanel.add(oneLabel);

        // # of columns that contain 2 nucleotides
        System.out.println("Two: " + two);
        JLabel twoLabel = new JLabel("# of Columns that Contain Two Nucleotides: " + two);
        mainPanel.add(twoLabel);
        mainPanel.add(new JLabel("        A/C: " + ac));
        mainPanel.add(new JLabel("        A/G: " + ag));
        mainPanel.add(new JLabel("        A/T: " + at));
        mainPanel.add(new JLabel("        C/G: " + cg));
        mainPanel.add(new JLabel("        C/T: " + ct));
        mainPanel.add(new JLabel("        G/T: " + gt));

        // # of columns that contain 3 nucleotides
        System.out.println("Three: " + three);
        JLabel threeLabel = new JLabel("# of Columns that Contain Three Nucleotides: " + three);
        mainPanel.add(threeLabel);

        // # of columns that contain 4 nucleotides
        System.out.println("Four: " + four);
        JLabel fourLabel = new JLabel("# of Columns that Contain Four Nucleotides: " + four);
        mainPanel.add(fourLabel);

        // Check it all adds up
        System.out.println("Check: " + (gapNum + one + two + three + four));

        countDialog.pack();
        countDialog.setMinimumSize(countDialog.getMinimumSize());
        countDialog.setLocationRelativeTo(null);
        countDialog.setVisible(true);

    }

    /*
     * Compares nucleotide differences between reference (first) sequence and target
     * (second) sequence Lists 12 possible nucleotide pairs
     */
    protected void getSNPCounts() {

        // get the first 2 visible sequences
        FeaturedSequence[] seqs = new FeaturedSequence[2];
        seqs[0] = getVisibleSequences()[0];
        seqs[1] = getVisibleSequences()[1];

        int gapNum = 0, one = 0, two = 0;
        int ac = 0, ag = 0, at = 0, ca = 0, cg = 0, ct = 0, ga = 0, gc = 0, gt = 0, ta = 0, tc = 0, tg = 0;

        // Loop through the columns
        for (int i = 0; i < seqs[0].length(); i++) {

            String nucleotides = "";

            // Loop through the sequences
            seqsloop: for (int j = 0; j < seqs.length; j++) {

                // break when break found
                if (seqs[j].charAt(i) == '-') {
                    // don't want to count nucleotides when gap exists
                    gapNum++;
                    nucleotides = "";
                    break seqsloop;
                }

                // Checks for unique nucleotide and adds to nucleotides if exists
                boolean change = true;
                nucleoloop: for (int k = 0; k < nucleotides.length(); k++) {
                    if (seqs[j].charAt(i) == nucleotides.charAt(k)) {
                        change = false;
                        break nucleoloop;
                    }
                }
                if (change && seqs[j].charAt(i) != ' ')
                    nucleotides += seqs[j].charAt(i);

            }

            // Checks nucleotides length for number of nucleotides in column
            switch (nucleotides.length()) {
            case 1:
                one++;
                break;
            case 2:
                two++;
                if (nucleotides.indexOf("AC") > -1) {
                    ac++;
                } else if (nucleotides.indexOf("AG") > -1) {
                    ag++;
                } else if (nucleotides.indexOf("AT") > -1) {
                    at++;
                } else if (nucleotides.indexOf("CA") > -1) {
                    ca++;
                } else if (nucleotides.indexOf("CG") > -1) {
                    cg++;
                } else if (nucleotides.indexOf("CT") > -1) {
                    ct++;
                } else if (nucleotides.indexOf("GA") > -1) {
                    ga++;
                } else if (nucleotides.indexOf("GC") > -1) {
                    gc++;
                } else if (nucleotides.indexOf("GT") > -1) {
                    gt++;
                } else if (nucleotides.indexOf("TA") > -1) {
                    ta++;
                } else if (nucleotides.indexOf("TC") > -1) {
                    tc++;
                } else if (nucleotides.indexOf("TG") > -1) {
                    tg++;
                }
                break;
            default:
                break;
            }
        }

        final JDialog compareDialog = new JDialog((JFrame) null, "Get SNP Counts");

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("# of Columns that Contain Two Nucleotides: " + two));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        compareDialog.add(mainPanel);

        mainPanel.add(new JLabel("        A/C: " + ac));
        mainPanel.add(new JLabel("        A/G: " + ag));
        mainPanel.add(new JLabel("        A/T: " + at));
        mainPanel.add(new JLabel("        C/A: " + ca));
        mainPanel.add(new JLabel("        C/G: " + cg));
        mainPanel.add(new JLabel("        C/T: " + ct));
        mainPanel.add(new JLabel("        G/A: " + ga));
        mainPanel.add(new JLabel("        G/C: " + gc));
        mainPanel.add(new JLabel("        G/T: " + gt));
        mainPanel.add(new JLabel("        T/A: " + ta));
        mainPanel.add(new JLabel("        T/C: " + tc));
        mainPanel.add(new JLabel("        T/G: " + tg));

        mainPanel.add(new JLabel("These counts represent the number of SNPS between two sequences."));
        mainPanel.add(new JLabel("For example, 'A/C: 5' means that in 5 columns the first sequence"));
        mainPanel.add(new JLabel("contains an A and the second sequence contains a C."));

        // Check it all adds up
        System.out.println("Total: " + seqs[0].length());
        System.out.println("gapNum: " + gapNum);
        System.out.println("one: " + one);
        System.out.println("two: " + two);
        System.out.println("Check: " + (gapNum + one + two));

        compareDialog.pack();
        compareDialog.setMinimumSize(compareDialog.getMinimumSize());
        compareDialog.setLocationRelativeTo(null);
        compareDialog.setVisible(true);

    }

    /**
     * Counts unique positions
     */
    protected void getUniquePositions() {

        FeaturedSequence[] seqs = getVisibleSequences();

        final JDialog countDialog = new JDialog((JFrame) null, "Get Unique Positions");
        countDialog.setSize(new Dimension(450, 600));
        countDialog.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Number of unique positions that are not a gap"));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        countDialog.add(mainPanel);

        JScrollPane scroll = new JScrollPane(mainPanel);
        countDialog.add(scroll, BorderLayout.CENTER);

        // Loop through the sequences
        for (int i = 0; i < seqs.length; i++) {

            int uniqueCount = 0;

            // Loop through the columns
            seqsloop: for (int j = 0; j < seqs[i].length(); j++) {

                // Ignore if its a gap
                if (seqs[i].charAt(j) == '-') {
                    continue;
                }

                int tempCount = 0;
                // Checks if unique
                innerloop: for (int k = 0; k < seqs.length; k++) {
                    if (seqs[k].charAt(j) == seqs[i].charAt(j)) {
                        tempCount++;
                        if (tempCount > 1) {
                            break innerloop;
                        }
                    }
                }
                if (tempCount < 2) {
                    uniqueCount++;
                }
            }

            // Add it to output
            System.out.println(seqs[i].getName() + ": " + uniqueCount);
            JLabel total = new JLabel(seqs[i].getName() + ": " + uniqueCount);
            mainPanel.add(total);

        }

        countDialog.setVisible(true);

    }

    /**
     * Gets unique positions matrix Across
     */
    protected void getUniquePositionsMatrix() {

        final int start = getPrimaryPanel().getDisplayStart();
        final int stop = getPrimaryPanel().getDisplayStop();

        UniquePositionsInfoPanel aip = new UniquePositionsInfoPanel(getVisibleSequences());
        aip.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final UniquePositionsInfoPanel fAip = aip;

        final JDialog diag = new JDialog();
        diag.setTitle("Unique Position Matrix");

        JPanel p = new JPanel(new BorderLayout());

        JButton close = new JButton("Close");
        close.setMnemonic(MenuKeyEvent.VK_C);
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                diag.dispose();
            }
        });

        ActionListener saveActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                fAip.saveFlatFile();
            }
        };

        JMenuBar mbar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save to tab delimited file");
        saveItem.addActionListener(saveActionListener);
        fileMenu.add(saveItem);
        mbar.add(fileMenu);
        diag.setJMenuBar(mbar);

        JTextArea description = new JTextArea(
                "  The following matrix shows the number of differences between each sequence."
                        + "\n    The differences exclude gaps from the sequence on the right, but not the sequence on the top.");
        description.setEditable(false);
        p.add(description, BorderLayout.NORTH);

        JPanel btn = new JPanel();
        btn.setLayout(new BoxLayout(btn, BoxLayout.X_AXIS));
        btn.add(Box.createHorizontalGlue());
        btn.add(close);
        btn.add(Box.createHorizontalGlue());
        btn.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        p.add(aip, BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);

        diag.setContentPane(p);
        diag.pack();

        if (diag.getWidth() > 800) {
            diag.setSize(800, diag.getHeight());
        }

        if (diag.getHeight() > 600) {
            diag.setSize(diag.getWidth(), 600);
        }

        // Position the dialog window
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - diag.getWidth()) / 2;
        int y = (dim.height - diag.getHeight()) / 2;
        diag.setLocation(x, y);

        diag.setVisible(true);

    }
}
