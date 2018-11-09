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

import ca.virology.baseByBase.DiffEditor;
import ca.virology.baseByBase.data.ColorScheme;
import ca.virology.baseByBase.data.IdentityColorScheme;
import ca.virology.baseByBase.data.StrandedColorScheme;
import ca.virology.baseByBase.io.VocsTools;
import ca.virology.lib.io.sequenceData.AnnotationKeys;
import ca.virology.lib.io.sequenceData.DifferenceType;
import ca.virology.lib.io.sequenceData.FeatureType;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.io.tools.FeatureTools;
import ca.virology.lib.io.tools.SequenceTools;
import ca.virology.lib.prefs.BBBPrefs;
import ca.virology.lib.search.SearchHit;
import ca.virology.lib.util.gui.UITools;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.PointLocation;
import org.biojava.bio.symbol.RangeLocation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;


/**
 * This class is the main display panel for editable sequences. This handles
 * the sequence editing. <BR><B>Things to look out for:</B><BR> There are
 * really 3 different 'views' onto an edited sequence. This panel handles all
 * of one and most of another.  The three views are 1) The viewport, 2) the
 * displayed area and 3) the whole sequence alignment.<BR><BR> Logically, (1)
 * is a subview of (2) and (2) is a subview of (3). <BR> This class handles
 * rendering and events for the 3rd case and most of the 2nd. See
 * <CODE>PrimaryPanel </CODE> for the other portion of the 2nd view and all of
 * the 1st. To handle the idea of a 'displayed area', I've had to implement a
 * couple of methods, namely <CODE>translate()</CODE> and
 * <CODE>untranslate()</CODE>.  translate converts a position from the
 * displayed area into the scale of the whole view, and untranslate converts a
 * position in the whole view back into the displayed view.
 *
 * @author Ryan Brodie
 * @see PrimaryPanel
 */
public class EditPanel extends AbstractSequencePanel implements Selectable {
    private static class ScalingProperties {
        public double scalingFactor;
        public int channelSpacing;
        public int channelHeight;
        public Font normalFont;
        public Font channelFont;
        public int normalFontDesc;
        public int normalFontHeight;
        public int normalFontWidth;
        public FontMetrics normalFontMetrics;
        public FontMetrics channelFontMetrics;
    }

    //~ Static fields/initializers /////////////////////////////////////////////

    public static final int NULL_CHANNEL = -10000;
    public static final int ALIGN_CHANNEL = 10002;
    public static final int ACID_CHANNEL = 10003;
    public static final int DIFF_CHANNEL = 10004;
    public static final int EVENT_CHANNEL = 10005;
    public static final int SEARCH_CHANNEL = 10008;
    public static final int SCALE_CHANNEL = 10006;
    public static final int ABSCALE_CHANNEL = 10007;

    public static final int PRIMER_CHANNEL = 10009;    //DANNY another channel for primers


    //~ Instance fields ////////////////////////////////////////////////////////

    protected FeaturedSequence m_seq;
    protected java.util.List m_eventLayers = new ArrayList();
    protected java.util.List m_primerLayers = new ArrayList();    //DANNY: organizes the primers so they don't overlap
    protected int m_charStart = -1;
    protected int m_charStop = -1;
    protected Font m_chanFont = new Font("", Font.BOLD, 10);
    protected ScalingProperties m_defaultScalingProperties = new ScalingProperties();
    protected ColorScheme m_colorScheme = new IdentityColorScheme();
    protected int m_diffColor = ColorScheme.DIFF_CLASSIC_SCHEME;
    protected int[] m_lanePrefs = {SCALE_CHANNEL, ABSCALE_CHANNEL, ALIGN_CHANNEL, ACID_CHANNEL, DIFF_CHANNEL, EVENT_CHANNEL, SEARCH_CHANNEL, PRIMER_CHANNEL //DANNY ADDED PRIMER ONE
    };
    protected int m_selStart = -1;
    protected int m_selStop = -1;
    protected String m_dbName;
    public boolean m_showDiff = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showDifferences")).booleanValue();
    public boolean m_showScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showScale")).booleanValue();
    public boolean m_showSeqScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showSeqScale")).booleanValue();
    public boolean m_showEvent = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showUserEvents")).booleanValue();
    public boolean m_showPrimer = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showPrimers")).booleanValue();
    boolean[] m_showFrame = {false, false, false};

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Construct a new edit panel to display the given sequence.
     *
     * @param sequence The sequence to display.  Though it is a
     *                 <CODE>FeaturedSequence</CODE>, it doesn't neccessarily have to
     *                 have features, in which case only sequence data will be
     *                 displayed.
     */
    public EditPanel(FeaturedSequence sequence, String dbName) {
        m_seq = sequence;
        m_dbName = dbName;

        m_defaultScalingProperties.scalingFactor = 1;

        setBackground(Color.white);
        setOpaque(true);

        layerDifferences();

        setDisplayFont(m_dispFont);
        initListeners();
        //        printCommentLocations();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public String getDbName() {
        return m_dbName;
    }

    /**
     * Set the 'visible' property of a particular frame
     *
     * @param frame   The frame to modify
     * @param showing The visibility status
     */
    public void setFrameVisible(int frame, boolean showing) {
        if ((frame < 0) || (frame >= 3)) {
            return;
        }

        m_showFrame[frame] = showing;
    }

    /**
     * Determine the visibility of a particular frame in the amino acid
     * translation
     *
     * @param frame The frame to query
     * @return true if the frame is set to 'visible' and false otherwise
     */
    public boolean isFrameVisible(int frame) {
        return m_showFrame[frame];
    }

    /**
     * set the color scheme to be used by this panel
     *
     * @param cs the new scheme
     */
    public void setColorScheme(ColorScheme cs) {
        m_colorScheme = cs;
        repaint();
    }

    public void setDiffColor(int cs) {
        m_diffColor = cs;
        repaint();
    }

    /**
     * set the strand to display in this panel
     *
     * @param strand the new strand
     */
    public void setDisplayStrand(StrandedFeature.Strand strand) {
        super.setDisplayStrand(strand);
        repaint();
    }

    /**
     * set the sequence font for this panel
     *
     * @param f the new font
     */
    public void setDisplayFont(Font f) {
        super.setDisplayFont(f);
        initText();
        resetView();
    }

    /**
     * Set the area of the panel to display.  By default the whole area will be
     * displayed. Passing in (-1,-1) will show the entire sequence space.
     *
     * @param start The leftmost position to display, if -1, show from the
     *              first position
     * @param stop  The rightmost position to display, if -1, show to the end of
     *              the sequence
     */
    public void setDisplayArea(int start, int stop) {
        m_charStart = start;
        m_charStop = stop;

        resetView();
    }

    /**
     * get the leftmost displayed position, or -1 if that position is the 'end'
     *
     * @return the leftmost displayed position or -1 for the 'endpoint'
     */
    public int getDisplayStart() {
        return m_charStart;
    }

    /**
     * get the rightmost displayed position, or -1 if that position is the
     * 'end'
     *
     * @return the rightmost displayed position or -1 for the 'endpoint'
     */
    public int getDisplayStop() {
        return m_charStop;
    }

    /**
     * Get the sequence displayed by this panel
     *
     * @return the sequence
     */
    public FeaturedSequence getSequence() {
        return m_seq;
    }

    public void setSequence(FeaturedSequence fs) {
        m_seq = fs;
    }

    /**
     * returns an array of ints which represents which channels to draw and in
     * which order.  Possibilities are <CODE>ALIGN_CHANNEL, ACID_CHANNEL,
     * EVENT_CHANNEL, DIFF_CHANNEL, SCALE_CHANNEL, ABSCALE_CHANNEL</CODE>
     *
     * @return the preferences array
     */
    public int[] getChannelPreferences() {
        return m_lanePrefs;
    }

    /**
     * Set the display properties for this panel.
     *
     * @param newPrefs An array of ints that represent the channels to display
     *                 at their respective positions.  (eg. if newPrefs[2] ==
     *                 ALIGN_CHANNEL, then the 3rd row of the display will be that of
     *                 the gapped sequence.
     */
    public void setChannelPreferences(int[] newPrefs) {
        m_lanePrefs = newPrefs;

        resetView();
        fireDisplayChangeEvent();
    }

    /**
     * Get the headers for the currently displayed rows
     *
     * @return a String array representing the headers for the currently
     * displayed channels.
     */
    public String[] getHeaders() {
        boolean showDiff = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showDifferences")).booleanValue();
        boolean showScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showScale")).booleanValue();
        boolean showSeqScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showSeqScale")).booleanValue();
        boolean showEvent = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showUserEvents")).booleanValue();
        boolean showPrimer = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showPrimers")).booleanValue();


        int size = 0;

        for (int i = 0; i < m_lanePrefs.length; ++i) {
            switch (m_lanePrefs[i]) {
                case ALIGN_CHANNEL:
                    ++size;

                    break;

                case ACID_CHANNEL:
                    size += getFramesShown();

                    break;

                case DIFF_CHANNEL:

                    if (!showDiff) {
                        break;
                    }

                    size += 1;

                    break;

                case EVENT_CHANNEL:

                    if (!showEvent) {
                        break;
                    }

                    size += 1;

                    break;

                case PRIMER_CHANNEL: //primer channel
                    if (!showPrimer) {
                        break;
                    }
                    size += 1;
                    break;

                case SEARCH_CHANNEL:
                    size += 1;

                    break;

                case ABSCALE_CHANNEL:

                    if (!showSeqScale) {
                        break;
                    }

                    size += 1;

                    break;

                case SCALE_CHANNEL:

                    if (!showScale) {
                        break;
                    }

                    size += 1;
            }
        }

        String[] ret = new String[size];

        int pos = 0;

        for (int i = 0; i < m_lanePrefs.length; ++i) {

            switch (m_lanePrefs[i]) {

                case ALIGN_CHANNEL:
                    ret[pos] = m_seq.getName();
                    ++pos;

                    break;

                case ACID_CHANNEL:

                    if (m_showFrame[0]) {
                        ret[pos] = "Frame 1";
                        ++pos;
                    }

                    if (m_showFrame[1]) {
                        ret[pos] = "Frame 2";
                        ++pos;
                    }

                    if (m_showFrame[2]) {
                        ret[pos] = "Frame 3";
                        ++pos;
                    }

                    break;

                case DIFF_CHANNEL:

                    if (!showDiff) {
                        break;
                    }

                    ret[pos] = "Differences";
                    ++pos;

                    break;

                case EVENT_CHANNEL:

                    if (!showEvent) {
                        break;
                    }

                    ret[pos] = "User Events";
                    ++pos;

                    break;

                case PRIMER_CHANNEL:
                    if (!showPrimer) {
                        break;
                    }
                    ret[pos] = "Primers";
                    ++pos;

                    break;

                case SEARCH_CHANNEL:
                    ret[pos] = "Search Results";
                    ++pos;

                    break;

                case ABSCALE_CHANNEL:

                    if (!showSeqScale) {
                        break;
                    }

                    ret[pos] = "Seq Position";
                    ++pos;

                    break;

                case SCALE_CHANNEL:

                    if (!showScale) {
                        break;
                    }

                    ret[pos] = "Scale";

                    ++pos;

                    break;

                case NULL_CHANNEL:
                    break;
            }
        }

        return ret;
    }

    /**
     * Gets the height of this widget
     *
     * @return the height of this widget
     */
    public int getHeight() {
        int[] heads = getHeaderHeights();

        int ret = 0;

        for (int i = 0; i < heads.length; ++i) {
            ret += heads[i];
        }

        return ret;
    }

    /**
     * Get the heights of the headers currently displayed.  Note that
     * getHeaders().length == getHeaderHeights().length.
     *
     * @return an int array of ints representing the height of each header in
     * turn from top to bottom
     */
    public int[] getHeaderHeights() {
        boolean showDiff = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showDifferences")).booleanValue();
        boolean showScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showScale")).booleanValue();
        boolean showSeqScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showSeqScale")).booleanValue();
        boolean showEvent = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showUserEvents")).booleanValue();
        boolean showPrimer = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showPrimers")).booleanValue();

        int size = 0;

        for (int i = 0; i < m_lanePrefs.length; ++i) {
            switch (m_lanePrefs[i]) {
                case ALIGN_CHANNEL:
                    ++size;

                    break;

                case ACID_CHANNEL:
                    size += getFramesShown();

                    break;

                case DIFF_CHANNEL:

                    if (!showDiff) {
                        break;
                    }

                    size += 1;

                    break;

                case EVENT_CHANNEL:

                    if (!showEvent) {
                        break;
                    }

                    size += 1;

                    break;

                case PRIMER_CHANNEL:
                    if (!showPrimer) {
                        break;
                    }
                    size += 1;
                    break;
                case SEARCH_CHANNEL:
                    size += 1;

                    break;

                case ABSCALE_CHANNEL:

                    if (!showSeqScale) {
                        break;
                    }

                    size += 1;

                    break;

                case SCALE_CHANNEL:

                    if (!showScale) {
                        break;
                    }

                    size += 1;
                case NULL_CHANNEL:
                    break;
            }
        }

        int[] ret = new int[size];

        int pos = 0;

        for (int i = 0; i < m_lanePrefs.length; ++i) {
            switch (m_lanePrefs[i]) {
                case ALIGN_CHANNEL:
                    ret[pos] = m_defaultScalingProperties.normalFontDesc + m_defaultScalingProperties.normalFontHeight +
                            m_defaultScalingProperties.channelSpacing;
                    ++pos;

                    break;

                case ACID_CHANNEL:

                    if (m_showFrame[0]) {
                        ret[pos] = m_defaultScalingProperties.normalFontDesc + m_defaultScalingProperties.normalFontHeight +
                                m_defaultScalingProperties.channelSpacing;
                        ++pos;
                    }

                    if (m_showFrame[1]) {
                        ret[pos] = m_defaultScalingProperties.normalFontDesc + m_defaultScalingProperties.normalFontHeight +
                                m_defaultScalingProperties.channelSpacing;
                        ++pos;
                    }

                    if (m_showFrame[2]) {
                        ret[pos] = m_defaultScalingProperties.normalFontDesc + m_defaultScalingProperties.normalFontHeight +
                                m_defaultScalingProperties.channelSpacing;
                        ++pos;
                    }

                    break;

                case DIFF_CHANNEL:

                    if (!showDiff) {
                        break;
                    }

                    ret[pos] = m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing;
                    ++pos;

                    break;

                case EVENT_CHANNEL:

                    if (!showEvent) {
                        break;
                    }

                    ret[pos] = m_eventLayers.size() * (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing);
                    ++pos;

                    break;

                case PRIMER_CHANNEL:

                    if (!showPrimer) {
                        break;
                    }

                    ret[pos] = m_primerLayers.size() * (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing);
                    ++pos;

                    break;

                case SEARCH_CHANNEL:
                    ret[pos] = ((hasSearch()) ? 1 : 0) * (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing);
                    ++pos;

                    break;

                case ABSCALE_CHANNEL:

                    if (!showSeqScale) {
                        break;
                    }

                    ret[pos] = m_defaultScalingProperties.normalFontDesc + m_defaultScalingProperties.normalFontHeight +
                            m_defaultScalingProperties.channelSpacing;
                    ++pos;

                    break;

                case SCALE_CHANNEL:

                    if (!showScale) {
                        break;
                    }

                    ret[pos] = m_defaultScalingProperties.normalFontDesc + m_defaultScalingProperties.normalFontHeight +
                            m_defaultScalingProperties.channelSpacing;
                    ++pos;
            }
        }

        return ret;
    }

    /**
     * Get the heights of the headers currently displayed.  Note that
     * getHeaders().length == getHeaderHeights().length.
     *
     * @return an int array of ints representing the height of each header in
     * turn from top to bottom
     */
    public int[] getHeaderHeights(double scalingFactor) {
        boolean showDiff = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showDifferences")).booleanValue();
        boolean showScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showScale")).booleanValue();
        boolean showSeqScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showSeqScale")).booleanValue();
        boolean showEvent = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showUserEvents")).booleanValue();
        boolean showPrimer = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showPrimers")).booleanValue();

        int size = 0;

        for (int i = 0; i < m_lanePrefs.length; ++i) {
            switch (m_lanePrefs[i]) {
                case ALIGN_CHANNEL:
                    ++size;

                    break;

                case ACID_CHANNEL:
                    size += getFramesShown();

                    break;

                case DIFF_CHANNEL:

                    if (!showDiff) {
                        break;
                    }

                    size += 1;

                    break;

                case EVENT_CHANNEL:

                    if (!showEvent) {
                        break;
                    }

                    size += 1;

                    break;

                case PRIMER_CHANNEL:

                    if (!showPrimer) {
                        break;
                    }

                    size += 1;

                    break;

                case SEARCH_CHANNEL:
                    size += 1;

                    break;

                case ABSCALE_CHANNEL:

                    if (!showSeqScale) {
                        break;
                    }

                    size += 1;

                    break;

                case SCALE_CHANNEL:

                    if (!showScale) {
                        break;
                    }

                    size += 1;
            }
        }

        int[] ret = new int[size];

        int pos = 0;

        Font font = new Font(m_dispFont.getName(), m_dispFont.getStyle(), (int) (m_dispFont.getSize() * scalingFactor));
        int fontDesc = getFontMetrics(font).getDescent();
        int fontHeight = getFontMetrics(font).getAscent();
        Font chanFont = new Font(m_chanFont.getName(), m_chanFont.getStyle(), (int) (m_chanFont.getSize() * scalingFactor));
        int cHeight = getFontMetrics(chanFont).getAscent() + getFontMetrics(chanFont).getDescent();
        int cSpacing = (int) (m_defaultScalingProperties.channelSpacing * scalingFactor);

        for (int i = 0; i < m_lanePrefs.length; ++i) {
            switch (m_lanePrefs[i]) {
                case ALIGN_CHANNEL:
                    ret[pos] = fontDesc + fontHeight + cSpacing;
                    ++pos;

                    break;

                case ACID_CHANNEL:

                    if (m_showFrame[0]) {
                        ret[pos] = fontDesc + fontHeight + cSpacing;
                        ++pos;
                    }

                    if (m_showFrame[1]) {
                        ret[pos] = fontDesc + fontHeight + cSpacing;
                        ++pos;
                    }

                    if (m_showFrame[2]) {
                        ret[pos] = fontDesc + fontHeight + cSpacing;
                        ++pos;
                    }

                    break;

                case DIFF_CHANNEL:

                    if (!showDiff) {
                        break;
                    }

                    ret[pos] = cHeight + cSpacing;
                    ++pos;

                    break;

                case EVENT_CHANNEL:

                    if (!showEvent) {
                        break;
                    }

                    ret[pos] = m_eventLayers.size() * (cHeight + cSpacing);
                    ++pos;

                    break;
                case PRIMER_CHANNEL:

                    if (!showPrimer) {
                        break;
                    }

                    ret[pos] = m_primerLayers.size() * (cHeight + cSpacing);
                    ++pos;

                    break;

                case SEARCH_CHANNEL:
                    ret[pos] = ((hasSearch()) ? 1 : 0) * (cHeight + cSpacing);
                    ++pos;

                    break;

                case ABSCALE_CHANNEL:

                    if (!showSeqScale) {
                        break;
                    }

                    ret[pos] = fontDesc + fontHeight + cSpacing;
                    ++pos;

                    break;

                case SCALE_CHANNEL:

                    if (!showScale) {
                        break;
                    }

                    ret[pos] = fontDesc + fontHeight + cSpacing;
                    ++pos;
            }
        }

        return ret;
    }

    /**
     * get the selection in this window in the contents of a BioJava
     * <CODE>Location</CODE> object.
     *
     * @return a Range or Point location depending on whether this selection
     * ranges over more than one position, or null if there is no
     * selection at all.
     */
    public Location getAbsoluteSelection() {

        Location l = null;
        try {
            int start = m_seq.getAbsolutePosition(m_selStart);
            int stop = m_seq.getAbsolutePosition(m_selStop);

            if (start < 0) {
                start = 0;
                //start = 1;
            }
            if (stop < 0) {
                stop = 0;
                //stop = 1;
            }
            if (start == stop) {
                l = new PointLocation(start);
            } else {
                l = new RangeLocation(start, stop);
            }
            return l;

        } catch (IndexOutOfBoundsException e) {

            UITools.showWarning("In order to add a comment, please make nucleotide sequence selection", null);
            return l;
        }
    }


    public Location getAbsoluteSelection2() {

        Location l = null;
        try {
            int start = m_seq.getAbsolutePosition(m_selStart);
            int stop = m_seq.getAbsolutePosition(m_selStop);

            if (start < 0) {
                start = 1;
            }
            if (stop < 0) {
                stop = 1;
            }
            if (start == stop) {
                l = new PointLocation(start);
            } else {
                l = new RangeLocation(start, stop);
            }
            return l;

        } catch (IndexOutOfBoundsException e) {


            return l;
        }
    }

    /**
     * get the selection in terms of the alignment, including gaps
     *
     * @return the selection location
     */
    public Location getRelativeSelection() {
        if ((m_selStart < 0) || (m_selStop < 0)) {
            return null;
        }

        if (m_selStop >= m_seq.length()) {
            return null;
        }

        Location l = null;

        if (m_selStart == m_selStop) {
            l = new PointLocation(m_selStart);
        } else if (m_selStart < m_selStop) {
            l = new RangeLocation(m_selStart, m_selStop);
        }

        return l;
    }

    /**
     * Set the selection in terms of the gapped sequence
     *
     * @param start the first position in the selection
     * @param stop  The second position in the selection
     */
    public void setRelativeSelection(int start, int stop) {
        Location l = getRelativeSelection();
        start = Math.max(start, 0);
        stop = Math.max(stop, 0);

        if (start > stop) {
            int tmp = stop;
            stop = start;
            start = tmp;
        }

        m_selStart = start;
        m_selStop = stop;

        m_selStop = Math.min(m_seq.length() - 1, m_selStop);

        m_propSupport.firePropertyChange(SELECTION_PROPERTY, l, new RangeLocation(start, stop));

        //System.out.println (l+" -> "+new RangeLocation(start, stop));
        repaint();
    }

    /**
     * set the selection in absolute sequence positional terms
     *
     * @param x1 the selection start in absolute sequence position
     * @param x2 the selection stop in absolute sequence position
     */
    public void setAbsoluteSelection(int x1, int x2) {
        x1 = m_seq.getRelativePosition(x1);
        x2 = m_seq.getRelativePosition(x2);

        int start = Math.min(x1, x2);
        int stop = Math.max(x1, x2);

        setRelativeSelection(start, stop);
    }

    /**
     * Void the current selection
     */
    public void clearSelection() {
        m_selStart = -1;
        m_selStop = -1;

        repaint();
    }

    /**
     * get the width of font used to display the main sequence
     *
     * @return the width
     */
    public int getDisplayFontWidth() {
        return m_defaultScalingProperties.normalFontWidth;
    }

    /**
     * Set the font used to display channel data
     *
     * @param f the font to use
     */
    public void setChannelFont(Font f) {
        m_chanFont = f;
        initText();
        resetView();
    }

    /**
     * Get the font used to display channel data
     *
     * @return the current channel display font
     */
    public Font getChannelFont() {
        return m_chanFont;
    }

    /**
     * Set the spacing between channels
     *
     * @param newval the new spacing height
     */
    public void setChannelSpacing(int newval) {
        m_defaultScalingProperties.channelSpacing = newval;
        resetView();
    }

    /**
     * Get the spacing between channels
     *
     * @param newval the new spacing value
     * @return an int value
     */
    public int getChannelSpacing(int newval) {
        return m_defaultScalingProperties.channelSpacing;
    }

    /**
     * Set the height of the displayed channels (features, etc)
     *
     * @param newval the new height of displayed features
     */
    public void setChannelHeight(int newval) {
        m_defaultScalingProperties.channelHeight = newval;

        if (m_defaultScalingProperties.channelHeight < (m_defaultScalingProperties.channelFontMetrics.getAscent() + m_defaultScalingProperties.channelFontMetrics.getDescent())) {
            m_defaultScalingProperties.channelHeight = m_defaultScalingProperties.channelFontMetrics.getAscent() + m_defaultScalingProperties.channelFontMetrics.getDescent();
        }

        resetView();
    }

    /**
     * get the height of the displayed channels (features, etc)
     *
     * @return the height
     */
    public int getChannelHeight() {
        return m_defaultScalingProperties.channelHeight;
    }

    /**
     * get the width of this panel
     *
     * @return the width
     */
    public int getWidth() {
        return ((m_seq.length() * m_defaultScalingProperties.normalFontWidth) + 80);
    }

    /**
     * This finds the row indicated by the given point, and the searches that
     * row to see if there are any features directly under the x value at that
     * row and subrow
     *
     * @param p the point to search for
     * @return The feature directly under the point, or null if there is none
     */
    public StrandedFeature getFeatureAtPoint(Point p) {
        FeaturedSequence seq = getSequence();
        int screen = graphicsToSequence((int) p.getX());
        int loc = translate(Math.max(0, screen));
        int abloc = Math.max(1, seq.getAbsolutePosition(loc));
        String rowString = getRow((int) p.getY());
        StringTokenizer t = new StringTokenizer(rowString, "|");
        int row = Integer.parseInt(t.nextToken());
        int sub = -1;

        if (t.hasMoreTokens()) {
            sub = Integer.parseInt(t.nextToken());
        }

        // get the feature targeted
        FeatureFilter ff = new FeatureFilter.OverlapsLocation(new PointLocation(abloc));
        StrandedFeature clicked = null;
        FeatureHolder fh = null;

        switch (row) {
            case ACID_CHANNEL:
                fh = getSequence();
                FeatureFilter sgff = new FeatureFilter.And(new FeatureFilter.ByType(FeatureType.GENE), new FeatureFilter.StrandFilter(getDisplayStrand()));
                ff = new FeatureFilter.And(sgff, ff);
                fh = fh.filter(ff, false);

                if (fh.countFeatures() > 0) {
                    for (Iterator i = fh.features(); i.hasNext(); ) {
                        StrandedFeature f = (StrandedFeature) i.next();
                        Location l = f.getLocation();
                        int frame = 0;

                        if (getDisplayStrand().equals(StrandedFeature.POSITIVE)) {
                            frame = (l.getMin() - 1) % 3;
                        } else {
                            frame = (l.getMax() + 1) % 3;
                        }

                        if (frame == sub) {
                            clicked = f;
                            break;
                        }
                    }
                }
                break;
            case EVENT_CHANNEL:

                if (m_eventLayers.size() > 0) {
                    sub = Math.max(0, Math.min(sub, m_eventLayers.size() - 1));
                    fh = (FeatureHolder) m_eventLayers.get(sub);
                } else {
                    break;
                }

                if (fh.filter(ff, false).countFeatures() > 0) {
                    clicked = (StrandedFeature) fh.filter(ff, false).features().next();
                }
                break;
            case PRIMER_CHANNEL:
                if (m_primerLayers.size() > 0) {
                    sub = Math.max(0, Math.min(sub, m_primerLayers.size() - 1));
                    fh = (FeatureHolder) m_primerLayers.get(sub);
                } else {
                    break;
                }

                if (fh.filter(ff, false).countFeatures() > 0) {
                    clicked = (StrandedFeature) fh.filter(ff, false).features().next();
                }
                break;
            case DIFF_CHANNEL:

                if (sub == 0) {
                    fh = getSequence();
                    // following line removed by Gord so that comment removal
                    // works.
                    //ff = new FeatureFilter.ByType(FeatureType.DIFFERENCE_LIST);
                }

                // Changes by Gord to allow proper feature selection
                FeatureHolder newset = fh.filter(ff, false);
                Iterator item = newset.features();
                int bigdiff = Integer.MAX_VALUE;
                while (item.hasNext()) {
                    StrandedFeature f = (StrandedFeature) item.next();
                    Location l = f.getLocation();
                    int diff = (abloc - l.getMin()) + (l.getMax() - abloc);
                    if (diff < bigdiff) {
                        clicked = f;
                        bigdiff = diff;
                    }
                }

                break;

            case SEARCH_CHANNEL:

                if (hasSearch()) {
                    ff = new FeatureFilter.And(new FeatureFilter.ByType(FeatureType.SEARCH_RESULTS), new FeatureFilter.StrandFilter(getDisplayStrand()));
                }

                fh = m_seq.filter(ff, false);

                if (fh.countFeatures() > 0) {
                    clicked = (StrandedFeature) fh.features().next();
                }

                break;
        }

        return clicked;
    }

    public void renderDisplay(Graphics g) {
        renderDisplay(g, 1);
    }

    /**
     * render the display to a graphics component
     *
     * @param sg the graphics component to render to
     */
    public Dimension renderDisplay(Graphics sg, double scalingFactor) {
        // this method uses the Graphics2D.translate() method to maintain
        // the genericity of helper functions.  Each time something is
        // drawn in a channel, g.translate() is called and moves the
        // graphics context down the amount of space taken up by the
        // previous action.

        ScalingProperties scalingProperties;
        if (scalingFactor == 1) {
            scalingProperties = m_defaultScalingProperties;
        } else {
            scalingProperties = new ScalingProperties();
            scalingProperties.scalingFactor = scalingFactor;
            scalingProperties.normalFont = new Font(m_defaultScalingProperties.normalFont.getName(), m_defaultScalingProperties.normalFont.getStyle(), (int) Math.floor(m_defaultScalingProperties.normalFont.getSize() * scalingFactor + 0.5d));
            scalingProperties.normalFontMetrics = getFontMetrics(scalingProperties.normalFont);
            scalingProperties.normalFontDesc = scalingProperties.normalFontMetrics.getDescent();
            scalingProperties.normalFontHeight = scalingProperties.normalFontMetrics.getAscent();

            scalingProperties.normalFontWidth = scalingProperties.normalFontMetrics.charWidth('-');
            for (char c = 'A'; c <= 'Z'; c++) {
                int charWidth = scalingProperties.normalFontMetrics.charWidth(c);
                if (charWidth > scalingProperties.normalFontWidth)
                    scalingProperties.normalFontWidth = charWidth;
            }

            scalingProperties.channelFont = new Font(m_defaultScalingProperties.channelFont.getName(), m_defaultScalingProperties.channelFont.getStyle(), (int) Math.floor(m_defaultScalingProperties.channelFont.getSize() * scalingFactor + 0.5d));
            scalingProperties.channelFontMetrics = getFontMetrics(scalingProperties.channelFont);
            scalingProperties.channelHeight = (scalingProperties.channelFontMetrics.getAscent() + scalingProperties.channelFontMetrics.getDescent());
            scalingProperties.channelSpacing = (int) Math.floor(m_defaultScalingProperties.channelSpacing * scalingFactor + 0.5d);
        }

        Graphics2D g = (Graphics2D) sg;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        Rectangle r = sg.getClipBounds();

        int x1pos = (int) r.getX();
        int x2pos = (int) (r.getX() + r.getWidth());
        int charStart = (x1pos / scalingProperties.normalFontWidth) - 10;
        int charStop = (x2pos / scalingProperties.normalFontWidth) + 10;
        int end = Math.max(0, Math.min(m_seq.length(), charStop));
        charStart = Math.max(0, charStart);

        if ((end == 0) || (end < charStart)) {
            return new Dimension(0, 0);
        }
        charStop = end;
        if (charStop >= m_seq.length()) {
            charStop = m_seq.length() - 1;
        }
        String shownSeq = m_seq.substring(translate(charStart), Math.min(translate(charStop), m_seq.length()) + 1);

        if (getDisplayStrand().equals(StrandedFeature.NEGATIVE)) {
            shownSeq = ca.virology.lib.io.tools.SequenceTools.getDNAComplement(shownSeq);
        }

        boolean showDiff = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showDifferences")).booleanValue();
        boolean showScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showScale")).booleanValue();
        boolean showSeqScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showSeqScale")).booleanValue();
        boolean showEvent = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showUserEvents")).booleanValue();
        boolean showPrimer = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showPrimers")).booleanValue();

        int transx = 0;
        int transy = 0;

        for (int j = 0; j < m_lanePrefs.length; ++j) {
            switch (m_lanePrefs[j]) {
                case ALIGN_CHANNEL:
                    paintSequence(g, shownSeq, charStart, scalingProperties);
                    transy += (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight + scalingProperties.channelSpacing);
                    g.translate(0, scalingProperties.normalFontDesc + scalingProperties.normalFontHeight + scalingProperties.channelSpacing); //move down

                    break;
                case ACID_CHANNEL:
                    // draw genes in the 3 frames they belong in
                    paintGenes(g, charStart, charStop, scalingProperties);
                    // draw 3-frame translation
                    if (getDisplayStrand().equals(StrandedFeature.POSITIVE)) {
                        paintTopTranslation(g, charStart, charStop, scalingProperties);
                    } else {
                        paintBottomTranslation(g, charStart, charStop, scalingProperties);
                    }
                    int fcount = getFramesShown();
                    transy += (fcount * (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight +
                            scalingProperties.channelSpacing));
                    g.translate(0, fcount * (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight + scalingProperties.channelSpacing)); //move down
                    break;
                case SEARCH_CHANNEL:

                    boolean hasSearch = hasSearch();

                    if (hasSearch) {
                        paintSearchResults(g, charStart, charStop, scalingProperties);
                        transy += (scalingProperties.channelHeight + scalingProperties.channelSpacing);
                        g.translate(0, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                    }
                    break;
                case EVENT_CHANNEL: //includes differences and user comments

                    if (!showEvent) {
                        break;
                    }
                    paintEvents(g, charStart, charStop, scalingProperties);
                    transy += (m_eventLayers.size() * (scalingProperties.channelHeight + scalingProperties.channelSpacing));
                    g.translate(0, m_eventLayers.size() * (scalingProperties.channelHeight + scalingProperties.channelSpacing)); //move down
                    break;
                case PRIMER_CHANNEL:
                    if (!showPrimer) {
                        break;
                    }
                    paintPrimers(g, shownSeq, charStart, charStop, scalingProperties);
                    //we need to move down the number of rows we used to draw all the primers
                    transy += (m_primerLayers.size() * (scalingProperties.channelHeight + scalingProperties.channelSpacing));
                    g.translate(0, m_primerLayers.size() * (scalingProperties.channelHeight + scalingProperties.channelSpacing)); //move down
                    break;
                case DIFF_CHANNEL:

                    if (!showDiff) {
                        break;
                    }
                    paintDifferences(g, charStart, charStop, scalingProperties);
                    transy += (scalingProperties.channelHeight + scalingProperties.channelSpacing);
                    g.translate(0, (scalingProperties.channelHeight + scalingProperties.channelSpacing));
                    break;

                case SCALE_CHANNEL:

                    if (!showScale) {
                        break;
                    }
                    paintGappedScale(g, charStart, charStop, scalingProperties);
                    transy += (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight + scalingProperties.channelSpacing);
                    g.translate(0, scalingProperties.normalFontDesc + scalingProperties.normalFontHeight + scalingProperties.channelSpacing);
                    break;

                case ABSCALE_CHANNEL:

                    if (!showSeqScale) {
                        break;
                    }
                    paintAbsScale(g, charStart, charStop, scalingProperties);
                    transy += (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight + scalingProperties.channelSpacing);
                    g.translate(0, scalingProperties.normalFontDesc + scalingProperties.normalFontHeight + scalingProperties.channelSpacing);
            }
        }

        // un-translate graphics to draw all the lane-generic stuff
        g.translate(-transx, -transy);
        paintSelection(g, scalingProperties);
        return new Dimension(transx, transy);
    }

    /**
     * This will translate a given screen position to the actual sequence
     * position it represents.  If the leftmost point is 100 on the  ruler,
     * this method would turn 15 into 115.  If it was -1 (endpoint) it would
     * do nothing.
     *
     * @param position the position to translate
     * @return the translated position.
     */
    protected int translate(int position) {
        int delta = 0;

        if (m_charStart != -1) {
            delta = Math.abs(m_charStart);
        }

        int ret = position + delta;

        return ret;
    }

    /**
     * this exactly counteracts the affects of translate
     *
     * @param tpos the translated position
     * @return the position on the screen for the given translated position
     */
    protected int untranslate(int tpos) {
        if (m_charStart == -1) {
            return tpos;
        }

        return tpos - m_charStart;
    }

    /**
     * paint the search results (if there are any to display)
     *
     * @param g         The graphics object ot draw
     * @param charStart the position on the screen to draw
     * @param charStop  the position on the screen to stop drawing
     */
    protected void paintSearchResults(Graphics2D g, int charStart, int charStop, ScalingProperties scalingProperties) {
        g.setFont(scalingProperties.channelFont);
        g.setPaint(Color.black);

        int start = m_seq.getAbsolutePosition(translate(charStart));
        int stop = m_seq.getAbsolutePosition(translate(charStop));
        start = Math.max(0, start);
        stop = Math.max(0, stop);
        stop = Math.max(start, stop);

        FeatureFilter ff = new FeatureFilter.And(new FeatureFilter.ByType(FeatureType.SEARCH_RESULTS), new FeatureFilter.StrandFilter(getDisplayStrand()));
        FeatureHolder fh = m_seq.filter(ff, false);
        if (fh.countFeatures() <= 0) {
            return;
        }

        StrandedFeature f = (StrandedFeature) fh.features().next();

        SearchHit[] res = (SearchHit[]) f.getAnnotation().getProperty(AnnotationKeys.SEARCH_RESULTS);

        for (int i = 0; i < res.length; ++i) {
            if (res[i].getStop() < start) {
                continue;
            }
            if (res[i].getStart() > stop) {
                break;
            }
            // these are 1-off corrected since the search is done relative to the
            // string and not the sequence. (0-based instead of 1-based)
            int min = untranslate(m_seq.getRelativePosition(res[i].getStart() + 1));
            int max = untranslate(m_seq.getRelativePosition(res[i].getStop() + 1));

            int colorIndex = 255 - (int) (res[i].getConfidence() * 255.0);
            Color c = new Color(colorIndex, colorIndex, colorIndex);
            g.setColor(c);

            // draw line arrow pointing the right direction
            if (getDisplayStrand() == StrandedFeature.POSITIVE) {
                if ((max - min - 1) > 0) {
                    g.fillRect(min * scalingProperties.normalFontWidth, scalingProperties.channelHeight / 3, (max - min) * scalingProperties.normalFontWidth, scalingProperties.channelHeight / 3);
                }
                int[] x = {(max) * scalingProperties.normalFontWidth, (max + 1) * scalingProperties.normalFontWidth, (max) * scalingProperties.normalFontWidth};
                int[] y = {0, scalingProperties.channelHeight / 2, scalingProperties.channelHeight};
                g.fillPolygon(x, y, 3);
            } else {
                if ((max - min - 1) > 0) {
                    g.fillRect((min + 1) * scalingProperties.normalFontWidth, scalingProperties.channelHeight / 3, (max - min - 1) * scalingProperties.normalFontWidth, scalingProperties.channelHeight / 3);
                }
                int[] x = {(min + 1) * scalingProperties.normalFontWidth, (min) * scalingProperties.normalFontWidth, (min + 1) * scalingProperties.normalFontWidth};
                int[] y = {0, scalingProperties.channelHeight / 2, scalingProperties.channelHeight};
                g.fillPolygon(x, y, 3);
            }
        }
    }

    /**
     * returns true if the sequence to display has a search result feature or
     * not
     *
     * @return true if the sequence to display has a search result feature or
     * not.
     */
    protected boolean hasSearch() {
        FeatureFilter ff = new FeatureFilter.And(new FeatureFilter.ByType(FeatureType.SEARCH_RESULTS), new FeatureFilter.StrandFilter(getDisplayStrand()));

        return m_seq.filter(ff, false).countFeatures() > 0;
    }

    /**
     * paint the alignment channel ( sequence data ) to a given graphics object
     *
     * @param g         The graphics object ot draw
     * @param seqString the string to draw
     * @param charStart the position on the screen to draw
     */
    protected void paintSequence(Graphics2D g, String seqString, int charStart, ScalingProperties scalingProperties) {
        g.setFont(scalingProperties.normalFont);

        for (int i = 0; i < seqString.length(); ++i) {
            Color c = Color.white;

            if (m_colorScheme instanceof StrandedColorScheme) {
                c = ((StrandedColorScheme) m_colorScheme).getBackground(getDisplayStrand(), getSequence(), translate(i + charStart));
            } else {
                c = m_colorScheme.getBackground(getSequence(), translate(i + charStart));
            }

            g.setPaint(c);
            g.fillRect((i + charStart) * scalingProperties.normalFontWidth, 0, scalingProperties.normalFontWidth, scalingProperties.normalFontDesc + scalingProperties.normalFontHeight);

            if (m_colorScheme instanceof StrandedColorScheme) {
                c = ((StrandedColorScheme) m_colorScheme).getForeground(getDisplayStrand(), getSequence(), translate(i + charStart));
            } else {
                c = m_colorScheme.getForeground(getSequence(), translate(i + charStart));
            }

            g.setPaint(c);
            g.drawString(seqString.charAt(i) + "", ((i + charStart) * scalingProperties.normalFontWidth), scalingProperties.normalFontHeight);
        }
    }

    /**
     * Paint the sequence position scale ot a given graphics object
     *
     * @param g         The graphics object to draw on
     * @param charStart the first position to draw
     * @param charStop  the last position to draw
     */
    protected void paintAbsScale(Graphics2D g, int charStart, int charStop, ScalingProperties scalingProperties) {
        g.setFont(scalingProperties.normalFont);
        g.setColor(Color.black);

        int last = -999;

        for (int i = charStart - 15; i < (charStop + 15); ++i) {
            if (i < 0) {
                continue;
            }

            int j = m_seq.getAbsolutePosition(translate(i));

            if ((((j % 20) == 0) && (j >= 0)) || (j == 1)) {
                int x = sequenceToGraphics(i, scalingProperties.scalingFactor);

                if (j == last) {
                    continue;
                }

                g.drawString("|" + j, x, scalingProperties.normalFontHeight);
                last = j;
            }
        }
    }

    private void printCommentLocations() {
        Iterator layeri = m_eventLayers.iterator();
        while (layeri.hasNext()) {
            FeatureHolder fh = (FeatureHolder) layeri.next();
            Iterator fi = fh.features();

            while (fi.hasNext()) {
                StrandedFeature f = (StrandedFeature) fi.next();
                Location l = f.getLocation();
                int min = untranslate(m_seq.getRelativePosition(l.getMin()));
                int max = untranslate(m_seq.getRelativePosition(l.getMax()));

                for (int i = min; i < max; i++) {
                    if (f.getType().equals(FeatureType.COMMENT)) {
                        System.out.println(i + 1);
                    }
                }
            }
        }
    }

    /**
     * Paint the gapped position scale ot a given graphics object
     *
     * @param g         The graphics object to draw on
     * @param charStart the first position to draw
     * @param charStop  the last position to draw
     */
    protected void paintGappedScale(Graphics2D g, int charStart, int charStop, ScalingProperties scalingProperties) {
        g.setFont(scalingProperties.normalFont);
        g.setColor(Color.black);

        int last = -999;

        for (int i = charStart - 15; i < (charStop + 15); ++i) {
            if (i < 0) {
                continue;
            }

            if (((i + 1) % 20) == 0 || i == 0) {
                int x = sequenceToGraphics(i, scalingProperties.scalingFactor);

                if (i == last) {
                    continue;
                }

                g.drawString("|" + (translate(i) + 1), x, scalingProperties.normalFontHeight);
                last = i;
            }
        }
    }

    /**
     * Paint a selection on the given graphics object
     *
     * @param g The graphics object to draw the selection rectangle on.
     */
    protected void paintSelection(Graphics2D g, ScalingProperties scalingProperties) {
        if ((m_selStart == -1) && (m_selStop == -1)) {
            return;
        }

        int y = 0;
        int h = 0;
        int[] headerHeights = getHeaderHeights(scalingProperties.scalingFactor);
        for (int i = 0; i < headerHeights.length; i++) {
            h += headerHeights[i];
        }
        int x = (untranslate(m_selStart)) * scalingProperties.normalFontWidth;
        int x2 = (untranslate(m_selStop + 1)) * scalingProperties.normalFontWidth;
        int w = x2 - x;

        g.setColor(new Color(0, 0, 0, 50));
        g.fillRect(x, y, w, h);
    }

    /**
     * Paint a 1-2-3 frame translation on the given graphics object
     *
     * @param g         The object to draw on
     * @param charStart the first position to draw
     * @param charStop  the last position to draw
     */
    protected void paintTopTranslation(Graphics2D g, int charStart, int charStop, ScalingProperties scalingProperties) {
        charStart = translate(charStart);
        charStop = translate(charStop);

        g.setFont(scalingProperties.normalFont);

        boolean showArrow = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showAcidArrows")).booleanValue();
        boolean hilightSS = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.hilightSSCodons")).booleanValue();

        // aquire position of first non gap character in displayed sequence
        int[] start = new int[3];

        for (int i = charStart; i < m_seq.length(); ++i) {
            if (m_seq.charAt(i) != '-') {
                start[0] = i;

                for (int j = i + 1; j < m_seq.length(); ++j) {
                    if (m_seq.charAt(j) != '-') {
                        start[1] = j;

                        for (int k = j + 1; k < m_seq.length(); ++k) {
                            if (m_seq.charAt(k) != '-') {
                                start[2] = k;

                                break;
                            }
                        }

                        break;
                    }
                }

                break;
            }
        }

        int[] frame = new int[3];

        for (int i = 0; i < 3; ++i) {
            frame[i] = (m_seq.getAbsolutePosition(start[i]) - 1) % 3;
        }

        for (int k = 0; k < 3; ++k) {
            int i = start[k];

            if ((charStart - i) > 5) {
                break;
            }

            if (frame[k] < 0) {
                break;
            }

            int j = 0;
            StringBuffer codon = null;

            for (; i < charStop; ++i) {
                codon = new StringBuffer();

                if (m_seq.charAt(i) == ' ') {
                    break;
                }

                if (m_seq.charAt(i) != '-') {
                    codon.append(m_seq.charAt(i));

                    for (j = i + 1; j <= charStop; ++j) {
                        if (m_seq.charAt(j) != '-') {
                            codon.append(m_seq.charAt(j));

                            if (codon.length() == 3) {
                                break;
                            }
                        }
                    }
                } else {
                    drawFramedString(g, "-", frame[k], untranslate(i), scalingProperties);

                    continue;
                }

                if (codon.length() < 3) {
                    break;
                }

                String aa = ca.virology.lib.io.tools.SequenceTools.getAminoAcid(codon.toString());

                Color c = null;
                int startArrow = 0;

                if (aa.equals("M") && hilightSS) {
                    c = new Color(100, 255, 100);
                    startArrow = untranslate(i);
                } else if (aa.equals("*") && hilightSS) {
                    c = new Color(255, 100, 100);
                    startArrow = untranslate(i);
                }

                if ((c != null) && m_showFrame[frame[k]]) {
                    GraphicsTools.fillArrow(g, c, (startArrow) * scalingProperties.normalFontWidth, untranslate(j + 1) * scalingProperties.normalFontWidth, frame[k] * (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight + scalingProperties.channelSpacing), (frame[k] * (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight + scalingProperties.channelSpacing)) +
                            scalingProperties.normalFontHeight + scalingProperties.normalFontDesc);
                }

                drawFramedString(g, aa, frame[k], untranslate(i), scalingProperties);
                ++i;

                while (i < j) {
                    if (showArrow) {
                        drawFramedString(g, "=", frame[k], untranslate(i), scalingProperties);
                    }

                    ++i;
                }

                if (showArrow) {
                    drawFramedString(g, ">", frame[k], untranslate(i), scalingProperties);
                }
            }
        }
    }

    /**
     * Paint a 4-5-6 frame translation on the given graphics object
     *
     * @param g         The object to draw on
     * @param charStart the first position to draw
     * @param charStop  the last position to draw
     */
    protected void paintBottomTranslation(Graphics2D g, int charStart, int charStop, ScalingProperties scalingProperties) {
        charStart = translate(charStart);
        charStop = translate(charStop);

        //formerly channelFont
        g.setFont(scalingProperties.normalFont);

        boolean showArrow = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showAcidArrows")).booleanValue();
        boolean hilightSS = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.hilightSSCodons")).booleanValue();

        // aquire position of first non gap character in displayed sequence
        int[] start = new int[3];
        int end = (charStop >= m_seq.length()) ? (m_seq.length() - 1) : charStop;

        for (int i = end; i >= 0; --i) {
            if (m_seq.charAt(i) != '-') {
                start[0] = i;

                for (int j = i - 1; j >= 0; --j) {
                    if (m_seq.charAt(j) != '-') {
                        start[1] = j;

                        for (int k = j - 1; k >= 0; --k) {
                            if (m_seq.charAt(k) != '-') {
                                start[2] = k;

                                break;
                            }
                        }

                        break;
                    }
                }

                break;
            }
        }

        int[] frame = new int[3];

        for (int i = 0; i < 3; ++i) {
            frame[i] = (m_seq.getAbsolutePosition(start[i]) + 1) % 3;
        }

        for (int k = 0; k < 3; ++k) {
            int i = start[k];
            int j = 0;
            StringBuffer codon = null;

            for (; i >= charStart; --i) {
                codon = new StringBuffer();

                if (m_seq.charAt(i) != '-') {
                    codon.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(m_seq.charAt(i)));

                    for (j = i - 1; j >= charStart; --j) {
                        if (m_seq.charAt(j) != '-') {
                            codon.append(ca.virology.lib.io.tools.SequenceTools.getDNAComplement(m_seq.charAt(j)));

                            if (codon.length() == 3) {
                                break;
                            }
                        }
                    }
                } else {
                    drawFramedString(g, "-", frame[k], untranslate(i), scalingProperties);

                    continue;
                }

                if (codon.length() < 3) {
                    break;
                }

                String aa = ca.virology.lib.io.tools.SequenceTools.getAminoAcid(codon.toString());

                Color c = null;
                int startArrow = 0;

                if (aa.equals("M") && hilightSS) {
                    c = new Color(100, 255, 100);
                    startArrow = untranslate(i);
                } else if (aa.equals("*") && hilightSS) {
                    c = new Color(255, 100, 100);
                    startArrow = untranslate(i);
                }

                if ((c != null) && m_showFrame[frame[k]]) {
                    GraphicsTools.fillArrow(g, c, (startArrow + 1) * scalingProperties.normalFontWidth, untranslate(j) * scalingProperties.normalFontWidth, frame[k] * (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight +
                            scalingProperties.channelSpacing), (frame[k] * (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight + scalingProperties.channelSpacing)) + (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight));
                }

                drawFramedString(g, aa, frame[k], untranslate(i), scalingProperties);
                --i;

                while (i > j) {
                    if (showArrow) {
                        drawFramedString(g, "=", frame[k], untranslate(i), scalingProperties);
                    }

                    --i;
                }

                if (showArrow) {
                    drawFramedString(g, "<", frame[k], untranslate(i), scalingProperties);
                }
            }
        }
    }

    /**
     * Draws a string on the screen at the given position in the given frame
     *
     * @param g        the object to draw on
     * @param s        The string to draw
     * @param frame    the frame in 0,1,2
     * @param position The x-position (in characters) to start drawing at
     */
    protected void drawFramedString(final Graphics2D g, final String s, final int frame, final int position, final ScalingProperties scalingProperties) {
        g.setPaint(Color.black);
        g.drawString(s, position * scalingProperties.normalFontWidth, getFrameTop(frame, scalingProperties) + scalingProperties.normalFontHeight);
    }

    /**
     * Paints the difference on the screen
     *
     * @param g         the graphics object to draw to
     * @param charStart the first position to draw
     * @param charStop  the last position to draw
     */
    protected void paintDifferences(Graphics2D g, int charStart, int charStop, ScalingProperties scalingProperties) {
        //System.out.println("paintDifferences: "+m_diffColor);
        int start = m_seq.getAbsolutePosition(charStart);
        int stop = m_seq.getAbsolutePosition(charStop);

        start = Math.max(0, start);
        stop = Math.max(0, stop);
        stop = Math.max(start, stop);

        FeatureFilter ff = new FeatureFilter.ByType(FeatureType.DIFFERENCE_LIST);

        if (m_seq.filter(ff, false).countFeatures() > 0) {
            Iterator it = m_seq.filter(ff, false).features();
            Feature f = (Feature) it.next(); //should be only one, but get the first only if more
            int[] diffs = (int[]) f.getAnnotation().getProperty(AnnotationKeys.DIFF_ARRAY);
            for (int i = charStart; i < (charStop + 1); ++i) {
                int ti = translate(i);

                if (ti >= diffs.length) {
                    break;
                }
                Color c = getDifferenceColor(diffs[ti], m_diffColor);
                if (c == null) {
                    c = Color.black;
                }
                if (diffs[ti] != ca.virology.lib.io.sequenceData.DifferenceType.I_NONE) {
                    g.setPaint(c);
                    g.fillRect(i * scalingProperties.normalFontWidth, 0, scalingProperties.normalFontWidth, scalingProperties.channelHeight);
                }
            }
        }
    }

    public static Color getDifferenceColor(final int diff, final int diffColor) {

        switch (diffColor) {
            case ColorScheme.DIFF_NT_SCHEME:
                switch (diff) {
                    case DifferenceType.I_INSERTION:
                    case DifferenceType.I_DELETION:
                    case DifferenceType.I_TRANSPOSITION:
                        return Color.black;
                    case DifferenceType.I_SUBSTITUTION:
                        return Color.blue;
                    case DifferenceType.I_SUB_NT_CA:
                    case DifferenceType.I_SUB_NT_GA:
                    case DifferenceType.I_SUB_NT_TA:
                        return Color.YELLOW;
                    case DifferenceType.I_SUB_NT_AC:
                    case DifferenceType.I_SUB_NT_GC:
                    case DifferenceType.I_SUB_NT_TC:
                        return Color.GREEN;
                    case DifferenceType.I_SUB_NT_AG:
                    case DifferenceType.I_SUB_NT_CG:
                    case DifferenceType.I_SUB_NT_TG:
                        return Color.CYAN;
                    case DifferenceType.I_SUB_NT_AT:
                    case DifferenceType.I_SUB_NT_CT:
                    case DifferenceType.I_SUB_NT_GT:
                        return Color.ORANGE;
                    default:
                        return null;
                }

            default:
                if (diff == ca.virology.lib.io.sequenceData.DifferenceType.I_INSERTION) {
                    return Color.green;
                } else if (diff == ca.virology.lib.io.sequenceData.DifferenceType.I_DELETION) {
                    return Color.red;
                } else if (DifferenceType.isSubstitution(diff)) {
                    return Color.blue;
                    //Note that transposition never occurs
                } else if (diff == ca.virology.lib.io.sequenceData.DifferenceType.I_TRANSPOSITION) {
                    return Color.magenta;
                } else {
                    return null;
                }
        }
    }


    /**
     * Paints the primers on the screen
     *
     * @param g         the graphics object to draw to
     * @param charStart the first position to draw
     * @param charStop  the last position to draw
     */
    protected void paintPrimers(Graphics2D g, String sequence, int charStart, int charStop, ScalingProperties scalingProperties) {
        int start = m_seq.getAbsolutePosition(translate(charStart));
        int stop = m_seq.getAbsolutePosition(translate(charStop));
        start = Math.max(0, start);
        stop = Math.max(0, stop);
        if (start > stop) {
            int tmp = start;
            start = stop;
            stop = tmp;
        }
        Location view;

        if (start == stop) {
            view = new PointLocation(start);
        } else {
            view = new RangeLocation(start, stop);
        }

        FeatureFilter ff = new FeatureFilter.And(new FeatureFilter.OverlapsLocation(view), new FeatureFilter.BySource(FeatureType.PRIMER));
        Iterator layeri = m_primerLayers.iterator();
        int cnt = 0;
        while (layeri.hasNext()) {
            FeatureHolder fh = (FeatureHolder) layeri.next();
            Iterator fi = fh.filter(ff, false).features();

            while (fi.hasNext()) {
                StrandedFeature f = (StrandedFeature) fi.next();
                Location l = f.getLocation();
                int min = untranslate(m_seq.getRelativePosition(l.getMin()));
                int max = untranslate(m_seq.getRelativePosition(l.getMax()));
                int relmin = m_seq.getRelativePosition(l.getMin());
                int relmax = m_seq.getRelativePosition(l.getMax());

                g.translate(0, cnt * (scalingProperties.channelHeight + scalingProperties.channelSpacing));

                String primersequence = (String) f.getAnnotation().getProperty(AnnotationKeys.PRIMER_SEQ);
                if (f.getStrand() == StrandedFeature.NEGATIVE) {
                    primersequence = SequenceTools.getDNAComplement(primersequence);
                    primersequence = new StringBuffer().append(primersequence).reverse().toString();
                }

                Color mismatch;
                Color match;
                if (getDisplayStrand() == f.getStrand()) {
                    mismatch = new Color(1.0f, 0.68f, 0.73f);
                    match = new Color(0.69f, 0.89f, 1.0f);
                } else {
                    mismatch = new Color(1.0f, 0.90f, 0.91f);
                    match = new Color(0.91f, 0.97f, 1.0f);
                }

                int gapoffset = 0;
                int j = 0;
                while (j < primersequence.length()) {
                    if (m_seq.charAt(relmin + j + gapoffset) == '-') {
                        g.setColor(Color.LIGHT_GRAY);
                        g.fillRect((relmin + j + gapoffset) * scalingProperties.normalFontWidth, 0, scalingProperties.normalFontWidth, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                        gapoffset++;
                        continue;
                    }
                    if (primersequence.charAt(j) == m_seq.charAt(relmin + j + gapoffset)) {
                        g.setColor(match);
                    } else {
                        g.setColor(mismatch);
                    }

                    boolean goesright = (f.getStrand() == StrandedFeature.POSITIVE);

                    if (j == 0) {
                        if (goesright) {
                            GraphicsTools.drawFilledArrowTail(g, goesright, g.getColor(), (relmin + j + gapoffset) * scalingProperties.normalFontWidth, 0, scalingProperties.normalFontWidth, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                        } else {
                            GraphicsTools.drawFilledCurvedArrowHead(g, goesright, g.getColor(), (relmin + j + gapoffset) * scalingProperties.normalFontWidth, 0, scalingProperties.normalFontWidth, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                        }
                    } else if (j == primersequence.length() - 1) {
                        if (goesright) {
                            GraphicsTools.drawFilledCurvedArrowHead(g, goesright, g.getColor(), (relmin + j + gapoffset) * scalingProperties.normalFontWidth, 0, scalingProperties.normalFontWidth, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                        } else {
                            GraphicsTools.drawFilledArrowTail(g, goesright, g.getColor(), (relmin + j + gapoffset) * scalingProperties.normalFontWidth, 0, scalingProperties.normalFontWidth, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                        }
                    } else {
                        g.fillRect((relmin + j + gapoffset) * scalingProperties.normalFontWidth, 0, scalingProperties.normalFontWidth, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                    }

                    j++;
                }


                //adds an outline to all the arrows
                if (f.getStrand() == StrandedFeature.POSITIVE) {
                    GraphicsTools.drawArrow(g, Color.GRAY, (relmin) * scalingProperties.normalFontWidth, (relmax + 1) * scalingProperties.normalFontWidth, 0, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                } else {
                    GraphicsTools.drawArrow(g, Color.GRAY, (relmax + 1) * scalingProperties.normalFontWidth, (relmin) * scalingProperties.normalFontWidth, 0, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                }

                String name = f.getAnnotation().getProperty(AnnotationKeys.NAME).toString();
                Color fg = Color.BLACK;
                if ((name != null) && !name.equals("")) {
                    int blockw = (max - min + 1) * scalingProperties.normalFontWidth;
                    int namew = scalingProperties.channelFontMetrics.stringWidth(name);

                    if (namew > blockw) {
                        boolean set = false;

                        for (int i = name.length() - 1; i >= 5; --i) {
                            if (scalingProperties.channelFontMetrics.stringWidth(name.substring(0, i) + "...") < blockw) {
                                name = name.substring(0, i) + "...";
                                set = true;
                                break;
                            }
                        }
                        if (!set) {
                            name = name.substring(0, 5) + "...";
                        }
                    }

                    namew = scalingProperties.channelFontMetrics.stringWidth(name);

                    int namex = (((min) * scalingProperties.normalFontWidth) + (blockw / 2)) - (namew / 2);
                    int namey = (scalingProperties.channelHeight / 2) + (scalingProperties.channelFontMetrics.getAscent() / 2);

                    g.setPaint(fg);
                    g.setFont(scalingProperties.channelFont);
                    g.drawString(name, namex, namey);
                }

                g.translate(0, -(cnt * (scalingProperties.channelSpacing + scalingProperties.channelHeight)));
            }

            ++cnt;
        }
    }


    /**
     * Paint user generated events on the given graphics object - includes comments, sequence selection...
     *
     * @param g         The object to draw to
     * @param charStart the first position to draw
     * @param charStop  the last position to draw
     */
    protected void paintEvents(Graphics2D g, int charStart, int charStop, ScalingProperties scalingProperties) {
        int start = m_seq.getAbsolutePosition(translate(charStart));
        int stop = m_seq.getAbsolutePosition(translate(charStop));

        start = Math.max(0, start);
        stop = Math.max(0, stop);

        if (start > stop) {
            int tmp = start;
            start = stop;
            stop = tmp;
        }
        Location view;

        if (start == stop) {
            view = new PointLocation(start);
        } else {
            view = new RangeLocation(start, stop);
        }
        FeatureFilter ff;
        ff = new FeatureFilter.And(new FeatureFilter.OverlapsLocation(view), new FeatureFilter.BySource(FeatureType.USER_GENERATED));

        Iterator layeri = m_eventLayers.iterator();
        int cnt = 0;

        while (layeri.hasNext()) {
            FeatureHolder fh = (FeatureHolder) layeri.next();
            Iterator fi = fh.filter(ff, false).features();

            while (fi.hasNext()) {
                StrandedFeature f = (StrandedFeature) fi.next();
                Location l = f.getLocation();
                int min = untranslate(m_seq.getRelativePosition(l.getMin()));
                int max = untranslate(m_seq.getRelativePosition(l.getMax()));

                String evType = null;

                if (f.getAnnotation().containsProperty(AnnotationKeys.EVENT_TYPE)) {
                    evType = (String) f.getAnnotation().getProperty(AnnotationKeys.EVENT_TYPE);
                }

                Paint p = null;

                if ((evType != null) && evType.equals(ca.virology.lib.io.sequenceData.DifferenceType.TRANSPOSITION)) {
                    p = Color.magenta;
                } else if (f.getType().equals(FeatureType.COMMENT)) {
                    Color theColor = (Color) f.getAnnotation().getProperty(AnnotationKeys.BGCOLOR);

                    if (getDisplayStrand() != f.getStrand()) {
                        p = Color.black;
                    } else {
                        p = theColor;
                    }
                } else {
                    p = Color.black;
                }

                g.translate(0, cnt * (scalingProperties.channelHeight + scalingProperties.channelSpacing));


                if (f.getType().equals(FeatureType.COMMENT)) {
                    if (getDisplayStrand() == f.getStrand()) {
                        GraphicsTools.fillRectangle(g, p, (min) * scalingProperties.normalFontWidth, (max + 1) * scalingProperties.normalFontWidth, 0, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                    } else { //!= so draw outline of rectangle
                        GraphicsTools.drawRectangle(g, p, (min) * scalingProperties.normalFontWidth, (max + 1) * scalingProperties.normalFontWidth - 1, 0, scalingProperties.channelHeight + scalingProperties.channelSpacing - 1);
                    }
                }
                else {
                    if (getDisplayStrand() != f.getStrand()) {
                        if (f.getStrand() == StrandedFeature.POSITIVE) {
                            GraphicsTools.drawArrow(g, p, (min) * scalingProperties.normalFontWidth, (max + 1) * scalingProperties.normalFontWidth, 0, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                        } else {
                            GraphicsTools.drawArrow(g, p, (max + 1) * scalingProperties.normalFontWidth, (min) * scalingProperties.normalFontWidth, 0, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                        }
                    } else {
                        if (f.getStrand() == StrandedFeature.POSITIVE) {
                            GraphicsTools.fillArrow(g, p, (min) * scalingProperties.normalFontWidth, (max + 1) * scalingProperties.normalFontWidth, 0, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                        } else {
                            GraphicsTools.fillArrow(g, p, (max + 1) * scalingProperties.normalFontWidth, (min) * scalingProperties.normalFontWidth, 0, scalingProperties.channelHeight + scalingProperties.channelSpacing);
                        }
                    }
                }

                if (f.getType().equals(FeatureType.COMMENT)) { // draw comment string
                    String cmt = f.getAnnotation().getProperty(AnnotationKeys.NAME).toString();
                    Color fg = (Color) f.getAnnotation().getProperty(AnnotationKeys.FGCOLOR);

                    if ((cmt != null) && !cmt.equals("")) {
                        int blockw = (max - min + 1) * scalingProperties.normalFontWidth;
                        int namew = scalingProperties.channelFontMetrics.stringWidth(cmt);
                        if (namew > blockw) {
                            boolean set = false;
                            for (int i = cmt.length() - 1; i >= 5; --i) {
                                if (scalingProperties.channelFontMetrics.stringWidth(cmt.substring(0, i) + "...") < blockw) {
                                    cmt = cmt.substring(0, i) + "...";
                                    set = true;
                                    break;
                                }
                            }
                            if (!set) {
                                if (cmt.length() < 6 && cmt.length() > 0) {
                                    cmt = "" + cmt.charAt(0);
                                } else if (cmt.length() == 0) {
                                    cmt = "";
                                } else {
                                    cmt = cmt.substring(0, 5) + "...";
                                }
                            }
                        }
                        namew = scalingProperties.channelFontMetrics.stringWidth(cmt);
                        int namex = (((min) * scalingProperties.normalFontWidth) + (blockw / 2)) - (namew / 2);
                        int namey = (scalingProperties.channelHeight / 2) + (scalingProperties.channelFontMetrics.getAscent() / 2);

                        g.setPaint(fg);
                        g.setFont(scalingProperties.channelFont);
                        g.drawString(cmt, namex, namey);
                    }
                }
                g.translate(0, -(cnt * (scalingProperties.channelSpacing + scalingProperties.channelHeight)));
            }
            ++cnt;
        }
    }

    /**
     * Get the graphics position of the top of the given frame in the amino
     * acid translation
     *
     * @param frame The frame to query
     * @return The position on the screen of the top of the frame
     */
    protected int getFrameTop(int frame, ScalingProperties scalingProperties) {
        if (!m_showFrame[frame]) {
            return -999;
        }

        switch (frame) {
            case 0:
                return 0;

            case 1:

                if (m_showFrame[0]) {
                    return (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight +
                            scalingProperties.channelSpacing);
                } else {
                    return 0;
                }

            case 2:
                return (getFramesShown() - 1) * (scalingProperties.normalFontDesc + scalingProperties.normalFontHeight +
                        scalingProperties.channelSpacing);

            default:
                return -999;
        }
    }

    /**
     * Get the number of frames displayed currently in the amino acid
     * translation
     *
     * @return the number of displayed frames
     */
    protected int getFramesShown() {
        int fcount = 0;
        for (int i = 0; i < m_showFrame.length; ++i) {
            if (m_showFrame[i]) {
                ++fcount;
            }
        }
        return fcount;
    }

    /**
     * paint genes on the given graphics object
     *
     * @param g         The graphics object to draw to
     * @param charStart the first position to draw
     * @param charStop  the last position to draw
     */
    protected void paintGenes(Graphics2D g, int charStart, int charStop, ScalingProperties scalingProperties) {
        int start = m_seq.getAbsolutePosition(translate(charStart));
        int stop = m_seq.getAbsolutePosition(translate(charStop));
        Location view = null;

        start = Math.max(0, start);
        stop = Math.max(0, stop);
        stop = Math.max(start, stop);

        if (start == stop) {
            view = new PointLocation(start);
        } else {
            view = new RangeLocation(start, stop);
        }

        FeatureFilter ff = new FeatureFilter.And(new FeatureFilter.And(new FeatureFilter.ByType(FeatureType.GENE), new FeatureFilter.OverlapsLocation(view)), new FeatureFilter.StrandFilter(getDisplayStrand()));

        Iterator i = m_seq.filter(ff, false).features();

        while (i.hasNext()) {
            StrandedFeature f = (StrandedFeature) i.next();
            Location l = f.getLocation();
            int frame = 0;

            if (getDisplayStrand().equals(StrandedFeature.POSITIVE)) {
                frame = (l.getMin() - 1) % 3;
            } else {
                frame = (l.getMax() + 1) % 3;
            }

            drawGene(g, f, frame, scalingProperties);
        }
    }

    /**
     * Draw a gene on the given graphics object using the properties passed in.
     *
     * @param g     The graphics context
     * @param f     The feature representing the gene to draw
     * @param frame The frame to draw the gene on
     */
    protected void drawGene(Graphics2D g, StrandedFeature f, int frame, ScalingProperties scalingProperties) {
        Location l = f.getLocation();
        int start = l.getMin();
        int stop = l.getMax();
        int min = untranslate(m_seq.getRelativePosition(start));
        int max = untranslate(m_seq.getRelativePosition(stop) + 1); // placement correction
        Color c = null;
        min = min * scalingProperties.normalFontWidth;
        max = max * scalingProperties.normalFontWidth;

        g.translate(0, getFrameTop(frame, scalingProperties));

        if (f.getStrand() == StrandedFeature.POSITIVE) {
            c = new Color(255, 0, 0, 100); // light red, !lead
            GraphicsTools.fillArrow(g, c, min, max, 0, scalingProperties.normalFontHeight + scalingProperties.normalFontDesc);
        } else {
            c = new Color(0, 0, 255, 100); // light blue, lead
            GraphicsTools.fillArrow(g, c, max, min, 0, scalingProperties.normalFontHeight + scalingProperties.normalFontDesc);
        }
        g.translate(0, -getFrameTop(frame, scalingProperties));
    }

    /**
     * Get the x position on the screen for a given sequence position
     *
     * @param seqPos the sequence position to convert
     * @return the screen x position representing the seq position
     */
    public int sequenceToGraphics(int seqPos) {
        return seqPos * m_defaultScalingProperties.normalFontWidth;
    }

    /**
     * Get the x position on the screen for a given sequence position
     *
     * @param seqPos the sequence position to convert
     * @return the screen x position representing the seq position
     */
    public int sequenceToGraphics(int seqPos, double scalingFactor) {
        Font font = new Font(m_dispFont.getName(), m_dispFont.getStyle(), (int) (m_dispFont.getSize() * scalingFactor));
        int fontWidth = getFontMetrics(font).charWidth('-');
        return seqPos * fontWidth;
    }

    /**
     * Get the sequence position for a given graphics x position
     *
     * @param grPos the screen x position to convert
     * @return the position in sequence terms of that x
     */
    public int graphicsToSequence(int grPos) {
        return grPos / m_defaultScalingProperties.normalFontWidth;
    }

    /**
     * redefine the preferred values of size and position and then repaint
     */
    public void resetView() {
        layerDifferences(); //organizes all the eventlayer features so they don't overlap, into multiple rows
        //layerComments();
        layerPrimers();    //organizes all the primers into multiple layers so they dont overlap
        revalidate();
        fireDisplayChangeEvent();
    }

    /**
     * get the minimum size of this component
     *
     * @return the minimum size
     */
    public Dimension getMinimumSize() {
        int length = 0;
        if (m_charStop == -1) {
            length = m_seq.length();
        } else {
            length = m_charStop;
        }
        if (m_charStart >= 0) {
            length -= m_charStart;
        }
        return new Dimension((length * m_defaultScalingProperties.normalFontWidth) + 80, getHeight());
    }

    /**
     * get the maximum size of this component
     *
     * @return the maximum size
     */
    public Dimension getMaximumSize() {
        int length = 0;
        if (m_charStop == -1) {
            length = m_seq.length();
        } else {
            length = m_charStop;
        }
        if (m_charStart >= 0) {
            length -= m_charStart;
        }
        return new Dimension((length * m_defaultScalingProperties.normalFontWidth) + 80, getHeight());
    }

    /**
     * get the preferrred size of this component
     *
     * @return the preferred size
     */
    public Dimension getPreferredSize() {
        int length = 0;
        if (m_charStop == -1) {
            length = m_seq.length();
        } else {
            length = m_charStop;
        }
        if (m_charStart >= 0) {
            length -= m_charStart;
        }
        return new Dimension((length * m_defaultScalingProperties.normalFontWidth) + 80, getHeight());
    }

    /**
     * fire an event indicating that the display on this panel has somehow
     * changed (size, properties, etc.)
     */
    protected void fireDisplayChangeEvent() {
        m_propSupport.firePropertyChange(DISPLAY_PROPERTY, null, this);
    }

    /**
     * init fonts, etc
     */
    protected void initText() {
        if (m_defaultScalingProperties.normalFont == null || !m_defaultScalingProperties.normalFont.equals(m_dispFont)) {
            m_defaultScalingProperties.normalFont = m_dispFont;
            m_defaultScalingProperties.normalFontMetrics = getFontMetrics(m_defaultScalingProperties.normalFont);
            m_defaultScalingProperties.normalFontDesc = m_defaultScalingProperties.normalFontMetrics.getDescent();
            m_defaultScalingProperties.normalFontHeight = m_defaultScalingProperties.normalFontMetrics.getAscent();
            m_defaultScalingProperties.normalFontWidth = m_defaultScalingProperties.normalFontMetrics.charWidth('-');
            m_defaultScalingProperties.normalFontWidth = (m_defaultScalingProperties.normalFontWidth < m_defaultScalingProperties.normalFontMetrics.charWidth('A')) ? m_defaultScalingProperties.normalFontMetrics.charWidth('A') : m_defaultScalingProperties.normalFontWidth;
            m_defaultScalingProperties.normalFontWidth = (m_defaultScalingProperties.normalFontWidth < m_defaultScalingProperties.normalFontMetrics.charWidth('C')) ? m_defaultScalingProperties.normalFontMetrics.charWidth('C') : m_defaultScalingProperties.normalFontWidth;
            m_defaultScalingProperties.normalFontWidth = (m_defaultScalingProperties.normalFontWidth < m_defaultScalingProperties.normalFontMetrics.charWidth('T')) ? m_defaultScalingProperties.normalFontMetrics.charWidth('T') : m_defaultScalingProperties.normalFontWidth;
            m_defaultScalingProperties.normalFontWidth = (m_defaultScalingProperties.normalFontWidth < m_defaultScalingProperties.normalFontMetrics.charWidth('G')) ? m_defaultScalingProperties.normalFontMetrics.charWidth('G') : m_defaultScalingProperties.normalFontWidth;
            m_defaultScalingProperties.normalFontWidth = (m_defaultScalingProperties.normalFontWidth < m_defaultScalingProperties.normalFontMetrics.charWidth(' ')) ? m_defaultScalingProperties.normalFontMetrics.charWidth(' ') : m_defaultScalingProperties.normalFontWidth;
            m_defaultScalingProperties.normalFontWidth = (m_defaultScalingProperties.normalFontWidth < m_defaultScalingProperties.normalFontMetrics.charWidth('?')) ? m_defaultScalingProperties.normalFontMetrics.charWidth('?') : m_defaultScalingProperties.normalFontWidth;
        }

        if (m_defaultScalingProperties.channelFont == null || !m_defaultScalingProperties.channelFont.equals(m_chanFont)) {
            m_defaultScalingProperties.channelFont = m_chanFont;
            m_defaultScalingProperties.channelFontMetrics = getFontMetrics(m_defaultScalingProperties.channelFont);

            if (m_defaultScalingProperties.channelHeight < (m_defaultScalingProperties.channelFontMetrics.getAscent() + 2)) {
                m_defaultScalingProperties.channelHeight = m_defaultScalingProperties.channelFontMetrics.getAscent() + 2;
            }
        }
    }

    /**
     * init window and component listeners
     */
    protected void initListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent ev) {
                doToolTip(ev);
            }
        });
    }

    /**
     * Returns the row string that is represented by the given y-value in
     * screen units
     *
     * @param yval The y-value of (presumably) the user click
     * @return The row string which is really two integers, a row and a subrow,
     * broken by a '|' pipe character.  The rows can be any of
     * ALIGN_CHANNEL, ACID_CHANNEL, DIFF_CHANNEL, EVENT_CHANNEL,
     * SCALE_CHANNEL or ABSCALE_CHANNEL, and the subrow can be
     * anything 0 or larger.  Rows that would not otherwise have a are
     * represented by [rownum]|0, so they always have a subrow of
     * zero.
     */
    protected String getRow(int yval) {
        int dist = 0;
        int i = 0;
        boolean showDiff = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showDifferences")).booleanValue();
        boolean showScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showScale")).booleanValue();
        boolean showSeqScale = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showSeqScale")).booleanValue();
        boolean showEvent = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showUserEvents")).booleanValue();
        boolean showPrimer = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("gui.showPrimers")).booleanValue();

        for (; i < m_lanePrefs.length; ++i) {
            switch (m_lanePrefs[i]) {
                case ALIGN_CHANNEL:
                    dist += (m_defaultScalingProperties.normalFontDesc + m_defaultScalingProperties.normalFontHeight +
                            m_defaultScalingProperties.channelSpacing);
                    break;

                case ACID_CHANNEL:
                    dist += (getFramesShown() * (m_defaultScalingProperties.normalFontDesc +
                            m_defaultScalingProperties.normalFontHeight + m_defaultScalingProperties.channelSpacing));

                    break;

                case EVENT_CHANNEL:
                    if (!showEvent) {
                        break;
                    }

                    dist += (m_eventLayers.size() * (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing));
                    break;

                case SEARCH_CHANNEL:
                    if (!hasSearch()) {
                        break;
                    }
                    dist += (((hasSearch()) ? 1 : 0) * (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing));
                    break;

                case DIFF_CHANNEL:
                    if (!showDiff) {
                        break;
                    }
                    dist += (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing +
                            (m_eventLayers.size() * (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing)));

                    break;
                case PRIMER_CHANNEL:
                    if (!showPrimer) {
                        break;
                    }
                    dist += (m_primerLayers.size() * (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing));
                    break;

                case ABSCALE_CHANNEL:
                    if (!showSeqScale) {
                        break;
                    }
                    dist += (m_defaultScalingProperties.normalFontDesc + m_defaultScalingProperties.normalFontHeight +
                            m_defaultScalingProperties.channelSpacing);
                    break;

                case SCALE_CHANNEL:
                    if (!showScale) {
                        break;
                    }
                    dist += (m_defaultScalingProperties.normalFontDesc + m_defaultScalingProperties.normalFontHeight +
                            m_defaultScalingProperties.channelSpacing);
                    break;
            }
            if (yval <= dist) {
                break;
            }
        }

        String ret = m_lanePrefs[i] + "|";

        switch (m_lanePrefs[i]) {
            case ACID_CHANNEL:
                dist -= (getFramesShown() * (m_defaultScalingProperties.normalFontDesc +
                        m_defaultScalingProperties.normalFontHeight +
                        m_defaultScalingProperties.channelSpacing));
                int sub = (yval - dist) / (m_defaultScalingProperties.normalFontDesc +
                        m_defaultScalingProperties.normalFontHeight + m_defaultScalingProperties.channelSpacing);

                if (sub == 2) {
                    ret += (sub + "");
                    break;
                } else if (sub == 1) {
                    boolean found = false;
                    for (int j = 0; j < m_showFrame.length; ++j) {
                        if (m_showFrame[j] && found) {
                            ret += (j + "");
                            break;
                        } else if (m_showFrame[j]) {
                            found = true;
                        }
                    }
                } else if (sub == 0) {
                    for (int j = 0; j < m_showFrame.length; ++j) {
                        if (m_showFrame[j]) {
                            ret += (j + "");
                            break;
                        }
                    }
                } else {
                    ret += (-1 + "");
                }
                break;

            case EVENT_CHANNEL:
                if (!showEvent) {
                    break;
                }
                dist -= (m_eventLayers.size() * (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing));
                sub = (yval - dist) / ((m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing));
                ret += (sub + "");
                break;

            case PRIMER_CHANNEL:    //primer, determines, what row of the multiple primer rows this occured in
                //then we would return a value like PRIMERCHANNEL | 3 <- where 3 is the 3rd row

                if (!showPrimer) {
                    break;
                }
                int startofprimers = dist - (m_primerLayers.size() * (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing));
                int diff = yval - startofprimers;
                int index = diff / (m_defaultScalingProperties.channelHeight + m_defaultScalingProperties.channelSpacing);

                ret += (index + "");
                break;
            default:
                ret += "0";
        }
        return ret;
    }

    /**
     * Given a mouse event (and location), this will determine if there are any
     * features under the location and use a <CODE>FeatureAction</CODE> to
     * determine a good tool tip text to display
     *
     * @param ev The mouse event to get the tool tip based on
     */
    protected void doToolTip(MouseEvent ev) {
        Point p = ev.getPoint();
        if (graphicsToSequence((int) p.getX()) >= m_seq.length()) {
            return;
        }
        StrandedFeature over = getFeatureAtPoint(ev.getPoint());
        FeatureAction fa = FeatureActionFactory.getInstance().createFeatureAction(over);
        int pos = translate(graphicsToSequence(ev.getX()));
        int seqPos = m_seq.getAbsolutePosition(pos);
        pos++;

        //Makes tooltips persist as people may want to read them.
        // Get current delay
        int dismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
        // Keep the tool tip showing
        dismissDelay = Integer.MAX_VALUE;
        ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);

        if (fa != null) {
            if (fa.getFeature().getType().equals(FeatureType.COMMENT)) {
                setToolTipText(UITools.formatToHTML("Comment: " + fa.getTooltipText() +
                        "\nAligned Position: " + pos +
                        "\nSequence Position: " + ((seqPos <= 0) ? "" : (seqPos + ""))));
            } else if (fa.getFeature().getType().equals(FeatureType.GENE)) {
                try {
                    String geneName = fa.getFeature().getAnnotation().getProperty(AnnotationKeys.NAME).toString();
                    String ortholog = VocsTools.getGeneOrthologName(geneName, DiffEditor.getDbName());
                    setToolTipText(UITools.formatToHTML("Gene: " + fa.getTooltipText() +
                            "\nOrtholog: " + ortholog +
                            "\nAligned Position: " + pos +
                            "\nSequence Position: " + ((seqPos <= 0) ? "" : (seqPos + ""))));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else if (fa.getFeature().getType().equals(FeatureType.PRIMER)) {
                setToolTipText(UITools.formatToHTML("Primer: " + fa.getTooltipText() +
                        "\nAligned Position: " + pos +
                        "\nSequence Position: " + ((seqPos <= 0) ? "" : (seqPos + ""))));
            } else { //Another type of feature
                setToolTipText(UITools.formatToHTML("Aligned Position: " + pos + "\nSequence Position: " + ((seqPos <= 0) ? "" : (seqPos + ""))));
            }
        } else { //not a feature
            setToolTipText(UITools.formatToHTML("Aligned Position: " + pos + "\nSequence Position: " + ((seqPos <= 0) ? "" : (seqPos + ""))));
        }
    }

    /**
     * Layer the differences into the different feature holders
     */
    protected void layerDifferences() {
        //m_eventLayers.removeAll(new ArrayList(m_eventLayers));
        m_eventLayers.clear();

        for (int i = 0; i < 10; ++i) {
            m_eventLayers.add(new SimpleFeatureHolder());
        }

        FeatureFilter dfilt = new FeatureFilter.Or(new FeatureFilter.ByType(FeatureType.EVENT), new FeatureFilter.ByType(FeatureType.COMMENT));
        //m_eventLayers contains 10 simple feature holders, iterator contains all features of type event OR comment  '
        try {
            FeatureTools.layerFeatures(m_eventLayers, m_seq.filter(dfilt, false).features());
        } catch (NullPointerException n) {
            System.out.println(" NULL: m_seq.filter(dfilt, false).features() is null: " + m_seq.countFeatures());
        }

        //remove all of the empty channels of eventLayers?
        for (int i = 0; i < m_eventLayers.size(); ++i) {
            if (((FeatureHolder) m_eventLayers.get(i)).countFeatures() == 0) {
                m_eventLayers.remove(m_eventLayers.get(i));
                --i;
            }
        }
    }

    /**
     * Same as layerDifferences()/
     * used to organize all the primers into multiple rows if they overlap.
     * when we draw it, each row of primers in the List will be drawn on a different level so they don't clash.
     */
    protected void layerPrimers() {
        //m_primerLayers.removeAll(new ArrayList(m_primerLayers));
        m_primerLayers.clear();

        for (int i = 0; i < 10; ++i) {
            m_primerLayers.add(new SimpleFeatureHolder());
        }

        FeatureFilter dfilt = new FeatureFilter.ByType(FeatureType.PRIMER);

        FeatureTools.layerFeatures(m_primerLayers, m_seq.filter(dfilt, false).features());

        for (int i = 0; i < m_primerLayers.size(); ++i) {
            if ( ((FeatureHolder) m_primerLayers.get(i)).countFeatures() == 0) {
                m_primerLayers.remove(m_primerLayers.get(i));
                --i;
            }
        }
    }

    public int getGapCount() {
        int gapCount = 0;
        boolean inGap = false;
        for (int i = 0; i < m_seq.length(); i++) {
            if (m_seq.charAt(i) == '-') {
                if (!inGap) {
                    inGap = true;
                }
            } else {
                if (inGap) {
                    gapCount++;
                    inGap = false;
                }
            }
        }

        if (inGap) {
            gapCount++;
        }

        return gapCount;
    }


    //~ Inner Classes //////////////////////////////////////////////////////////
}
