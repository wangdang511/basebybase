package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

import ca.virology.baseByBase.gui.CodeHop.CodeHopResultsPanel;
import ca.virology.lib.search.MalformedPatternException;
import ca.virology.lib.util.common.Logger;
import ca.virology.vgo.data.AbstractAnalysisPlugin;
import ca.virology.vgo.data.GenericInput;
import ca.virology.vgo.data.Organism;
import ca.virology.vgo.gui.*;
import ca.virology.vgo.util.VGOConstants;
import org.biojava.bio.Annotatable;
import org.biojava.bio.Annotation;
import org.biojava.bio.gui.sequence.*;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.utils.ChangeVetoException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


import ca.virology.vgo.data.*;
import ca.virology.vgo.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;
import java.util.List;

import org.biojava.bio.*;
import org.biojava.bio.gui.sequence.*;
import org.biojava.bio.seq.*;

import org.biojava.utils.*;

import ca.virology.vgo.lcs.LCSResult;
import ca.virology.lib.search.*;
import ca.virology.lib.util.common.Logger;


/**
 * This class contains a renderer to draw sequence info on the screen.  This class provides
 * a lot of facade functionality to the <CODE>VGOSequenceRenderer</CODE> class.
 *
 * @author Ryan Brodie
 * @date July 8, 2002
 */
public class VGOSequencePanel2 extends TranslatedSequencePanel implements Selectable, VGOUIObject {

    protected Organism m_organism;
    protected VGOSequenceRenderer2 m_renderer;

    protected SequencePanelHolder2 m_sequencePanelHolder;

    // vgo GUI framework objects
    protected java.util.List m_selListeners;
    protected VGOUIObject m_parent;

    protected java.util.List m_children;

    protected JPopupMenu m_popup = new JPopupMenu("Sequence Map");

    protected int m_selectionStart;
    protected int m_selectionStop;
    protected StrandedFeature.Strand m_selectionStrand;


    /**
     * Constructor for the VGOSequencePanel object
     */
    public VGOSequencePanel2() {
        m_children = new ArrayList();
        m_selListeners = new ArrayList();
    }


    /**
     * This returns a new panel object
     *
     * @param o an initialized organism object reference, no new
     *          initialization will be done here
     */
    public VGOSequencePanel2(Organism o, SequencePanelHolder2 sequencePanelHolder) {
        this();
        m_organism = o;
        m_sequencePanelHolder = sequencePanelHolder;
        m_renderer = new VGOSequenceRenderer2(o.getID(), o.getDbName());

        setSequence(o.getSequence());
        setDirection(SequenceRenderContext.HORIZONTAL);
        setBackground(java.awt.Color.white);
        setMinimumSize(new Dimension(0, 0));
        // should have no minimum size (good for split panes)
        setOpaque(true);

        PanelSelector ps = new PanelSelector();
        ClickHandler ch = new ClickHandler();
        PopupHandler ph = new PopupHandler();
        PointerHelpHandler phh = new PointerHelpHandler();
        addSequenceViewerListener(ps);
        addSequenceViewerMotionListener(ps);
        addSequenceViewerMotionListener(ph);
        addSequenceViewerListener(ch);

        addSequenceViewerMotionListener(phh);

        try {
            setRenderer(m_renderer);
        } catch (Exception ex) {
            Utils.showError("Error setting renderer: " + ex.getMessage());
            Logger.log(ex);
        }
    }

    /**
     * Get the organism for this panel
     *
     * @return the organism represented by this panel
     */
    public Organism getOrganism() {
        return m_organism;
    }


    public void addSelectionListener(SelectionListener l) {
        m_selListeners.add(l);
    }


    public void removeSelectionListener(SelectionListener l) {
        m_selListeners.remove(l);
    }

    /**
     * Set the popup menu for this panel
     *
     * @param menu The new popup menu
     */
    protected void setPopupMenu(JPopupMenu menu) {
        m_popup = menu;
    }

    /**
     * Get the popup menu for the current mouse position / context
     *
     * @return The JPopupuMenu
     */
    public JPopupMenu getPopupMenu() {
        return m_popup;
    }

    /**
     * Notify listeners of an event
     *
     * @param ev The event to notify of
     */
    protected void selectionNotify(SelectionEvent ev) {
        m_selectionStart = ev.getSeqStart();
        m_selectionStop = ev.getSeqStop();
        m_selectionStrand = ev.getStrand();
        for (Iterator it = m_selListeners.iterator(); it.hasNext(); ) {
            SelectionListener l = (SelectionListener) it.next();
            try {
                l.selection(ev);
            } catch (Exception ex) {
                //                vgo.util.Logger.println("Exception!");
                Logger.log(ex);
                //i.remove();
            }
        }
    }


    /**
     * Gets the name attribute of the Organism object
     *
     * @return The name value
     */
    public String getName() {
        return m_organism.getName();
    }


    /**
     * Selects a region of the window.
     *
     * @param seqStart    The leftmost position
     * @param seqStop     The rightmost position
     * @param description A text description of the selection
     * @param strand      The strand for the selection
     */
    public void selectRegion(int seqStart, int seqStop, String description, StrandedFeature.Strand strand) {
        if (seqStart > seqStop) {
            int tmp = seqStart;
            seqStart = seqStop;
            seqStop = tmp;
        }

        try {
            if (strand == StrandedFeature.POSITIVE) {
                m_renderer.selectRegion(
                        seqStart,
                        seqStop + 1,
                        (int) m_renderer.getForwardTop(this),
                        (int) m_renderer.getForwardBot(this) + 17,
                        description, true
                );
            } else {
                m_renderer.selectRegion(
                        seqStart,
                        seqStop + 1,
                        (int) m_renderer.getReverseTop(this) - 22,
                        (int) m_renderer.getReverseBot(this),
                        description, true
                );
            }
            SelectionEvent sev =
                    new SelectionEvent(
                            (seqStart > 0) ? seqStart : 0,
                            (seqStop <= getSymbols().length()) ? seqStop : getSymbols().length(),
                            strand,
                            m_organism.getID(),
                            m_organism.getDbName(),
                            null,
                            // this should be passed in
                            description);
            selectionNotify(sev);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error selecting region: " + ex.getMessage());
            Logger.log(ex);
        }
    }

    public void removeSearch(String description) {
        try {
            m_renderer.removeSearch(description, this);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error removing search: " + ex.getMessage());
            Logger.log(ex);
        }
    }


    /**
     * removeLCSResults
     * Called when the user right clicks on the LCS results row on the renderer, and selects
     * Remove, (should be right below Visual Settings).
     * This just forwards the action to the renderer
     *
     * @param description The title/description/name of the LCS row that we are trying to remove
     * @author Daniel Horspool
     */
    public void removeLCSResults(String description) {
        try {
            m_renderer.removeLCSResults(description, this);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error removing LCS: " + ex.getMessage());
            Logger.log(ex);
        }
    }


    /**
     * show or hide lane descriptions
     *
     * @param value if true, lane descriptions will be shown, otherwise they'll be hidden
     */
    public void displayLaneDescriptions(boolean value) {
        try {
            m_renderer.displayLaneDescriptions(value);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error setting value: " + ex.getMessage());
            Logger.log(ex);
        }
    }

    /**
     * Show or hide Open Reading Frames
     *
     * @param value The deciding boolean value
     */
    public void displayORF(boolean value) {
        try {
            m_renderer.displayORF(value);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error setting value: " + ex.getMessage());
            Logger.log(ex);
        }
    }


    /**
     * Show or hide Repeat Regions
     *
     * @param value The deciding boolean value
     */
    public void displayRepeatRegions(boolean value) {
        try {
            m_renderer.displayRepeatRegions(value);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error setting value: " + ex.getMessage());
            Logger.log(ex);
        }
    }


    /**
     * Show or hide BBB Comments
     *
     * @param value The deciding boolean value
     */
    public void displayBBBComments(boolean value) {
        try {
            m_renderer.displayBBBComments(value);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error setting value: " + ex.getMessage());
            Logger.log(ex);
        }
    }


    /**
     * Show or hide BBB Primers
     *
     * @param value The deciding boolean value
     */
    public void displayBBBPrimers(boolean value) {
        try {
            m_renderer.displayBBBPrimers(value);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error setting value: " + ex.getMessage());
            Logger.log(ex);
        }
    }


    /**
     * Show or hide the Bottom Strand codons
     *
     * @param value The deciding boolean value
     */
    public void displayBottom(boolean value) {
        try {
            m_renderer.displayBottom(value);
        } catch (ChangeVetoException ex) {
            Utils.showError(ex);
            Logger.log(ex);
        }
    }


    /**
     * Show or hide Start/Stop codons
     *
     * @param value The deciding boolean value
     */
    public void displaySS(boolean value) {
        try {
            m_renderer.displaySS(value);
        } catch (ChangeVetoException ex) {
            Utils.showError(ex);
            Logger.log(ex);
        }
    }


    /**
     * Show or hide GFS features
     */
    public void displayGFS(boolean value) {
        try {
            m_renderer.displayGFS(value);
        } catch (ChangeVetoException ex) {
            Utils.showError(ex);
            Logger.log(ex);
        }
    }


    /**
     * Sets the oRFLength attribute of the VGOSequencePanel object
     *
     * @param value The new oRFLength value
     */
    public void setORFLength(int value) {
        try {
            m_renderer.setORFLength(value);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error setting value: " + ex.getMessage());
            Logger.log(ex);
        }
    }

    /**
     * Returns the length of OpenReadingFrames displayed
     *
     * @return an integer length
     */
    public int getORFLength() {
        return m_renderer.getORFLength();
    }


    /**
     * Set the type of name to show for genes
     *
     * @param value one of "Name", "Family" or "None"
     */
    public void displayGeneName(String value) {
        try {
            m_renderer.displayGeneName(value);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error setting value: " + ex.getMessage());
            Logger.log(ex);
        }
    }


    /**
     * Show or hide Gene Analysis channels
     *
     * @param value The decidng boolean value
     */
    public void displayGeneAnalysis(boolean value) {
        try {
            m_renderer.displayGeneAnalysis(value);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error setting value: " + ex.getMessage());
            Logger.log(ex);
        }
    }


    /**
     * Removes the given generic input source from the underlying sequence
     * renderer
     *
     * @param gi The source to remove
     */
    public void addGenericInput(GenericInput gi) {
        try {
            m_renderer.addGenericInput(gi);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error adding generic input: " + ex.getMessage());
            Logger.log(ex);
        }
    }


    /**
     * Removes a particular analysis from the screen
     *
     * @param an the Analysis to remove
     */
    public void removeGenericInputAnalysis(GenericInput.Analysis an) {
        try {
            m_renderer.removeGenericInputAnalysis(an);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error remvoing analysis: " + ex.getMessage());
            Logger.log(ex);
        }
    }


    /**
     * Removes the given plugin from the sequence panel.  This means that it will no longer
     * be rendered on the screen.
     *
     * @param pu The plugin to remove
     */
    public void removePlugin(AbstractAnalysisPlugin pu) {
        try {
            m_renderer.removePlugin(pu);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error removing plugin: " + ex.getMessage());
            Logger.log(ex);
        }
    }

    public void setGeneHighlightMap(Map hiMap) {
        try {
            m_renderer.setGeneHighlightMap(hiMap);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error setting gene highlight: " + ex.getMessage());
            Logger.log(ex);
        }
        repaint();
    }

    /**
     * This method sets the color map for the underlying gene renderer so that genes may be highlighted
     * based on certain characteristics
     *
     * @param colormap The map of colors used to highlight genes on the screen
     */
    public void setGeneColorMap(Map colormap) {
        try {
            m_renderer.setGeneColorMap(colormap);
        } catch (ChangeVetoException ex) {
            Utils.showError("Error setting gene color map: " + ex.getMessage());
            Logger.log(ex);
        }
        repaint();
    }


    /**
     * Sets the parent attribute of the VGOSequencePanel object
     *
     * @param obj The new parent value
     */
    public void setParent(VGOUIObject obj) {
        m_parent = obj;
    }


    /**
     * Take ownership of the given window
     *
     * @param obj The object to take ownership of
     */
    public void takeOwnership(VGOUIObject obj) {
        m_children.add(obj);
    }

    /**
     * Request ownership be taken away from this object of the given child object
     *
     * @param obj The object to relinquish control of
     */
    public void removeOwnership(VGOUIObject obj) {
        m_children.remove(obj);
    }

    /**
     * Refresh this window
     */
    public void refresh() {
        repaint();
    }


    /**
     * Close this window
     */
    public void close() {
        Iterator i = m_children.iterator();
        while (i.hasNext()) {
            VGOUIObject o = (VGOUIObject) i.next();
            o.close();
        }
        setVisible(false);
        if (m_parent != null) m_parent.removeOwnership(this);
    }

    protected class PointerHelpHandler extends SequenceViewerMotionAdapter {
        protected int lastLoc = 0;
        protected Object lastSource = null;

        public void mouseMoved(SequenceViewerEvent ev) {
            // get the source, if it's a SequenceViewerActor,
            // otherwise if it's not an actor, get the target, it may be one if it exists
            // check the # of clicks
            // depending on number of clicks, do the intended action
            MouseEvent mev = ev.getMouseEvent();
            int loc = graphicsToSequence((double) mev.getX());
            SequenceViewerActor2 sourceActor = null;
            SequenceViewerActor2 targetActor = null;


            if (ev.getSource() != null && ev.getSource() instanceof Annotatable) {
                Annotation ann = ((Annotatable) ev.getSource()).getAnnotation();
                sourceActor = (SequenceViewerActor2) ann.getProperty(VGOConstants.SEQ_VIEW_ACTOR);
            }
            if (ev.getTarget() != null && ev.getTarget() instanceof FeatureHolder) {
                Iterator i = ((FeatureHolder) ev.getTarget()).features();
                if (i.hasNext()) {
                    Annotation ann = ((Feature) i.next()).getAnnotation();
                    targetActor = (SequenceViewerActor2) ann.getProperty(VGOConstants.SEQ_VIEW_ACTOR);
                }
            }
            SequenceViewerActor2 actor = null;
            if (sourceActor != null) {
                actor = sourceActor;
            } else if (targetActor != null) {
                actor = targetActor;
            } else {
                return;
            }
            if (actor != null) {
                SelectionEvent2 sev = actor.selection(VGOSequencePanel2.this, loc, false, m_sequencePanelHolder);
            }
        }

    }

    /**
     * this is the 'action button' handler for mouse clicks
     *
     * @author Ryan Brodie
     * @date July 8, 2002
     */
    protected class ClickHandler extends SequenceViewerAdapter {

        protected int lastLoc = 0;
        protected Object lastSource = null;


        public void mouseClicked(SequenceViewerEvent ev) {
            // get the source, if it's a SequenceViewerActor,
            // otherwise if it's not an actor, get the target, it may be one if it exists
            // check the # of clicks
            // depending on number of clicks, do the intended action
            MouseEvent mev = ev.getMouseEvent();
            int loc = graphicsToSequence((double) mev.getX());
            SequenceViewerActor2 sourceActor = null;
            SequenceViewerActor2 targetActor = null;


            if (ev.getSource() != null && ev.getSource() instanceof Annotatable) {
                Annotation ann = ((Annotatable) ev.getSource()).getAnnotation();
                sourceActor = (SequenceViewerActor2) ann.getProperty(VGOConstants.SEQ_VIEW_ACTOR);
            }
            if (ev.getTarget() != null && ev.getTarget() instanceof FeatureHolder) {
                Iterator i = ((FeatureHolder) ev.getTarget()).features();
                if (i.hasNext()) {
                    Annotation ann = ((Feature) i.next()).getAnnotation();
                    targetActor = (SequenceViewerActor2) ann.getProperty(VGOConstants.SEQ_VIEW_ACTOR);
                }
            }
            SequenceViewerActor2 actor = null;
            if (sourceActor != null) {
                actor = sourceActor;
            } else if (targetActor != null) {
                actor = targetActor;
            } else {
                return;
            }
            if (actor != null) {
                if (mev.getClickCount() == 1) {
                    // single (selection) click
                    final SelectionEvent2 sev = actor.selection(VGOSequencePanel2.this, loc, true, m_sequencePanelHolder);

                    if (sev != null) {
                        selectRegion(
                                sev.getSeqStart(),
                                sev.getSeqStop(),
                                sev.getDescription(),
                                sev.getStrand()
                        );
                        SelectionEvent newEvent = new SelectionEvent(
                                sev.getSeqStart(),
                                sev.getSeqStop(),
                                sev.getStrand(),
                                m_organism.getID(),
                                m_organism.getDbName(),
                                sev.getTarget(),
                                sev.getDescription()
                        );
                        selectionNotify(newEvent);

                        CodeHopResultsPanel.updateTopPanel(sev);


                    }
                } else if (mev.getClickCount() == 2) {
                    // double (action) click


                }
            }

        }
    }

    /**
     * Determines, based upon what is under the mouse, what popup menu should be shown,
     * and what its parameter should be
     *
     * @author Ryan Brodie
     */
    protected class PopupHandler extends SequenceViewerMotionAdapter {
        protected Object m_lastSource = null;

        public void mouseMoved(SequenceViewerEvent ev) {
            if (ev.getSource() == m_lastSource) {
                return;
            }

            JPopupMenu popup = new JPopupMenu();

            // This determines which popup items should go on the screen
            m_lastSource = ev.getSource();
            if (m_lastSource instanceof AnalysisPluginRenderer) {

                final AbstractAnalysisPlugin p = ((AnalysisPluginRenderer) m_lastSource).getPlugin();
                JMenuItem remove = new JMenuItem("Remove " + p.getName());
                remove.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent ev) {
                                removePlugin(p);
                            }
                        }
                );
                popup.add(remove);
            } else if (m_lastSource instanceof OpenReadingFrameRenderer) {
                JMenuItem setLen = new JMenuItem("Change ORF Length...");
                setLen.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent ev) {
                                String s = JOptionPane.showInputDialog(
                                        VGOSequencePanel2.this,
                                        "New ORF Length: (Currently " + getORFLength() + ")",
                                        "ORF Length",
                                        JOptionPane.QUESTION_MESSAGE
                                );
                                if (s != null) {
                                    int val = 0;
                                    try {
                                        val = Integer.parseInt(s);
                                    } catch (NumberFormatException ex) {
                                        Utils.showWarning("Please input an integer value", VGOSequencePanel2.this);
                                        return;
                                    }
                                    setORFLength(val);
                                }
                            }
                        }
                );
                popup.add(setLen);
            } else if (m_lastSource instanceof SearchRenderer) {
                final String description = ((SearchRenderer) m_lastSource).getDescription();
                JMenuItem mi = new JMenuItem("Remove " + description);
                mi.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent ev) {
                                removeSearch(description);
                            }
                        }
                );
                popup.add(mi);
                //LCS Add the ability to remove the search results by rightclicking and selecting "Remove"
            } else if (m_lastSource instanceof LCSRenderer) {
                final String description = ((LCSRenderer) m_lastSource).getDescription();
                JMenuItem mi = new JMenuItem("Remove " + description);
                mi.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent ev) {
                                removeLCSResults(description);
                            }
                        }
                );
                popup.add(mi);
            } else if (m_lastSource instanceof GenericAnalysisRenderer) {
                final GenericInput.Analysis ann = ((GenericAnalysisRenderer) m_lastSource).getAnalysis();
                JMenuItem mi = new JMenuItem("Remove Analysis " + ann.getName());
                mi.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent ev) {
                                removeGenericInputAnalysis(ann);
                            }
                        }
                );
                popup.add(mi);
            }
            setPopupMenu(popup);
        }
    }

    /**
     * This class handles mouse events for selection of regions on the screen
     *
     * @author Ryan Brodie
     * @date July 8, 2002
     */
    protected class PanelSelector implements SequenceViewerListener, SequenceViewerMotionListener {
        protected boolean selected = false;
        protected java.awt.Point startSpot = null;

        public void mouseMoved(SequenceViewerEvent ev) {
        }

        public void mousePressed(SequenceViewerEvent ev) {
        }

        public void mouseClicked(SequenceViewerEvent ev) {
        }

        public void mouseDragged(SequenceViewerEvent ev) {
            try {
                MouseEvent me = ev.getMouseEvent();
                java.awt.Point pt = me.getPoint();
                if (startSpot != null) {
                    int min;
                    int max;
                    if (graphicsToSequence(startSpot.getX()) <= graphicsToSequence(pt.getX())) {
                        min = graphicsToSequence(startSpot.getX());
                        max = graphicsToSequence(pt.getX());
                    } else {
                        max = graphicsToSequence(startSpot.getX());
                        min = graphicsToSequence(pt.getX());
                    }

                    StrandedFeature.Strand strand = m_renderer.getPointStrand((int) pt.getY(), VGOSequencePanel2.this);
                    String description = "Mouse Selection";
                    if (strand == StrandedFeature.POSITIVE) {
                        m_renderer.selectRegion(
                                min,
                                max,
                                (int) m_renderer.getForwardTop(VGOSequencePanel2.this),
                                (int) m_renderer.getForwardBot(VGOSequencePanel2.this),
                                description, false
                        );
                    } else {
                        m_renderer.selectRegion(
                                min,
                                max,
                                (int) m_renderer.getReverseTop(VGOSequencePanel2.this),
                                (int) m_renderer.getReverseBot(VGOSequencePanel2.this),
                                description, false
                        );
                    }
                } else {
                    startSpot = pt;
                }
                selected = true;
            } catch (Exception ex) {
                Utils.showError("Error in selection: " + ex.getMessage());
                Logger.log(ex);
            }
        }


        /**
         * This method completes or clears the selection
         *
         * @param ev The event that triggered this method
         */
        public void mouseReleased(SequenceViewerEvent ev) {
            try {
                java.awt.Point pt = ev.getMouseEvent().getPoint();

                StrandedFeature.Strand strand = m_renderer.getPointStrand((int) pt.getY(), VGOSequencePanel2.this);
                if (selected) {
                    // make the final box selection
                    // complete selection

                    int min;
                    int max;
                    if (graphicsToSequence(startSpot.getX()) <= graphicsToSequence(pt.getX())) {
                        min = graphicsToSequence(startSpot.getX());
                        max = graphicsToSequence(pt.getX());
                    } else {
                        max = graphicsToSequence(startSpot.getX());
                        min = graphicsToSequence(pt.getX());
                    }

                    String description = "Mouse Selection";
                    if (strand == StrandedFeature.POSITIVE) {
                        m_renderer.selectRegion(
                                min,
                                max,
                                (int) m_renderer.getForwardTop(VGOSequencePanel2.this),
                                (int) m_renderer.getForwardBot(VGOSequencePanel2.this),
                                description, true
                        );
                    } else {
                        m_renderer.selectRegion(
                                min,
                                max,
                                (int) m_renderer.getReverseTop(VGOSequencePanel2.this),
                                (int) m_renderer.getReverseBot(VGOSequencePanel2.this),
                                description, true
                        );
                    }

                    SelectionEvent sev =
                            new SelectionEvent(
                                    (min > 0) ? min : 0,
                                    (max <= getSymbols().length()) ? max : getSymbols().length(),
                                    strand,
                                    // this will later be figured out
                                    m_organism.getID(),
                                    m_organism.getDbName(),
                                    null,
                                    description);
                    selectionNotify(sev);
                    selected = false;
                    startSpot = null;
                } else {
                    // clear the selection ( just a click )
                    m_renderer.clearSelection();
                    startSpot = null;

                    // send a zero selection message
                    SelectionEvent sev =
                            new SelectionEvent(
                                    0, 0, strand, m_organism.getID(), m_organism.getDbName(), null, ""
                            );
                    selectionNotify(sev);
                }
            } catch (Exception ex) {
                Utils.show(ex);
                Logger.log(ex);
            }
        }
    }
}

