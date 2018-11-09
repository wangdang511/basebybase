package ca.virology.baseByBase.gui;

import ca.virology.baseByBase.data.*;

import ca.virology.lib.io.sequenceData.*;
import ca.virology.lib.io.tools.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

import java.awt.*;

import java.util.*;


/**
 * This panel displays a whole sequence in a scaled down form.  This displays
 * the genes, differences, visual area, etc. all in one window.
 *
 * @author Ryan Brodie
 * @version $Revision: 1.3 $
 */
public class SummarySequencePanel
        extends AbstractSequencePanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected FeaturedSequence m_seq;
    protected SequenceSummaryModel m_model;
    protected double m_scale;
    protected java.util.List m_geneLayers = new ArrayList();
    protected java.util.List m_primerLayers = new ArrayList();
    protected java.util.List m_commentLayers = new ArrayList();

    protected Rectangle[] m_geneRects;
    protected boolean[] m_geneStrand;
    protected String[] m_geneName;

    protected Rectangle[] m_primerRects;
    protected boolean[] m_primerStrand;
    protected String[] m_primerName;

    protected Rectangle[] m_commentRects;
    protected boolean[] m_commentStrand;
    protected Color[] m_commentColor;
    protected String[] m_commentName;

    protected RangeLocation m_view = new RangeLocation(1, 2);
    protected boolean m_useView = true;
    protected int m_dispStart = -1;
    protected int m_dispStop = -1;
    protected boolean m_showAll = true;
    protected boolean m_showGenes = true;
    protected boolean m_showSubs = true;
    protected boolean m_showIndels = true;
    protected boolean m_showPrimers = true;
    protected boolean m_showComments = true;

    protected int m_spacing = 1;
    protected int m_diffHeight = 15;
    protected int m_geneHeight = 12;
    protected int m_primerHeight = 12;
    protected int m_commentHeight = 12;

    protected int m_commentRows = 0;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new SummarySequencePanel object.
     *
     * @param sequence The sequence to display
     * @param model    the sequence to compare against
     */
    public SummarySequencePanel(FeaturedSequence sequence, SequenceSummaryModel model) {
        m_model = model;
        m_scale = 100.0;
        m_seq = sequence;
        setBackground(Color.white);
        setOpaque(true);
        setDisplayFont(m_dispFont);

        layerGenes();
        layerPrimers();
        layerComments();

        refreshGenePositions();
        refreshPrimerPositions();
        refreshCommentPositions();


    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the sequence displayed
     *
     * @return the sequence
     */
    public FeaturedSequence getSequence() {
        return m_seq;
    }

    /**
     * get the sequence compared against
     *
     * @return the sequence
     */
    public SequenceSummaryModel getSummaryModel() {
        return m_model;
    }

    /**
     * set the sequence to compare to
     *
     * @param model the sequence
     */
    public void setSummaryModel(SequenceSummaryModel model) {
        m_model = model;
        repaint();
    }

    /**
     * set the 'show all' flag.  If true, the visual area will be shown in a
     * light blue mask, otherwise the window will be zoomed into the visual
     * area.
     *
     * @param b the flag value
     */
    public void setShowAll(boolean b) {
        m_showAll = b;
    }

    /**
     * get the 'show all' flag.  If true, the visual area will be shown in a
     * light blue mask, otherwise the window will be zoomed into the visual
     * area.
     *
     * @return the flag value
     */
    public boolean showsAll() {
        return m_showAll;
    }

    /**
     * set the 'show genes' flag.  If true, features will be displayed below the sequence
     *
     * @param b the flag value
     */
    public void setShowGenes(boolean b) {
        m_showGenes = b;
    }

    /**
     * get the 'show genes' flag.  If true, features will be displayed below the sequence
     *
     * @return the flag value
     */
    public boolean showsGenes() {
        return m_showGenes;
    }

    public void setShowSubs(boolean b) {
        m_showSubs = b;
    }

    /**
     * get the 'show differences' flag.  If true, differences will be displayed in row
     *
     * @return the flag value
     */
    public boolean showsSubs() {
        return m_showSubs;
    }

    public void setShowIndels(boolean b) {
        m_showIndels = b;
    }

    /**
     * get the 'show differences' flag.  If true, differences will be displayed in row
     *
     * @return the flag value
     */
    public boolean showsIndels() {
        return m_showIndels;
    }

    public void setShowPrimers(boolean b) {
        m_showPrimers = b;
    }

    public boolean showsPrimers() {
        return m_showPrimers;
    }

    public void setShowComments(boolean b) {
        m_showComments = b;
    }

    public boolean showsComments() {
        return m_showComments;
    }

    /**
     * set the 'display area'.
     *
     * @param start the start of the display area
     * @param stop  the stop of the display area
     */
    public void setDisplayArea(
            int start,
            int stop) {
        m_dispStart = start;
        m_dispStop = stop;

        repaint();
    }

    /**
     * set the are displayed in the main window (this is the user view, not the
     * display area)
     *
     * @param start !
     * @param stop  !
     */
    public void setView(
            int start,
            int stop) {
        m_view = new RangeLocation(start, stop);
        repaint();
    }

    /**
     * set whether to render lines denoting the displayed area in the main window
     *
     * @param use !
     */
    public void setUseView(
            boolean use) {
        m_useView = use;
        repaint();
    }


    /**
     * Set the scale in symbols per pixel
     *
     * @param scale !
     */
    public void setScale(double scale) {
        m_scale = scale;

        //refreshGenePositions();
        repaint();
    }

    /**
     * get the scale
     *
     * @return !
     */
    public double getScale() {
        return m_scale;
    }

    /**
     * Gets the height of this widget
     *
     * @return the height of this widget
     */
    public int getHeight() {
        int totalheight = (m_diffHeight + m_spacing);
        if (showsGenes()) {
            totalheight += ((m_geneHeight + m_spacing) * m_geneLayers.size());
        }
        if (showsPrimers()) {
            totalheight += ((m_primerHeight + m_spacing) * m_primerLayers.size());
        }
        if (showsComments()) {
            totalheight += ((m_commentHeight + m_spacing) * m_commentLayers.size());
        }
        return totalheight;
    }

    /**
     * get the width of this panel
     *
     * @return the width
     */
    public int getWidth() {
        int ret = 0;

        if (showsAll()) {
            ret = sequenceToGraphics(m_seq.length());
        } else {
            int start = Math.max(m_dispStart, 0);
            int stop = (m_dispStop == -1) ? m_seq.length() : m_dispStop;
            ret = (int) ((double) (stop - start) / m_scale);
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
        int arraysize = 1; //at the least we need the name.
        if (showsGenes()) {
            arraysize++;
        }
        if (showsPrimers()) {
            arraysize++;
        }
        if (showsComments()) {
            arraysize++;
        }
        int[] ret = new int[arraysize];

        int index = 0;

        ret[index] = (m_diffHeight + m_spacing); //name
        index++;
        if (showsGenes()) {
            ret[index] = (m_geneHeight + m_spacing) * (m_geneLayers.size());
            index++;
        }
        if (showsPrimers()) {
            ret[index] = (m_primerHeight + m_spacing) * (m_primerLayers.size());
            index++;
        }
        if (showsComments()) {
            ret[index] = (m_commentHeight + m_spacing) * (m_commentLayers.size());
            index++;
        }
            /*
	if(showsGenes()){
	    int[] ret = {
		(m_diffHeight + m_spacing), //name
		(m_geneHeight + m_spacing) * (m_geneLayers.size())
	    }; //genes
	    return ret;
	} else {
	    int[] ret = {
		(m_diffHeight + m_spacing) //name
	    };
	    return ret;
	}*/
        return ret;
    }

    /**
     * Get the x position on the screen for a given sequence position
     *
     * @param seqPos the sequence position to convert
     * @return the screen x position representing the seq position
     */
    public int sequenceToGraphics(int seqPos) {
        int ret = 0;

        if (!showsAll()) {
            seqPos -= Math.max(0, m_dispStart);
        }

        ret = (int) ((double) seqPos / m_scale);

        return ret;
    }

    /**
     * Get the sequence position for a given graphics x position
     *
     * @param grPos the screen x position to convert
     * @return the position in sequence terms of that x
     */
    public int graphicsToSequence(int grPos) {
        int val = (int) ((double) grPos * m_scale);

        if (!showsAll()) {
            val += Math.max(0, m_dispStart);
        }

        return val;
    }

    /**
     * Get the headers for the currently displayed rows
     *
     * @return a String array representing the headers for the currently
     * displayed channels.
     */
    public String[] getHeaders() {
/*
	if(showsGenes()){
	    String[] ret = { m_seq.getName(), "-> Genes" };
	    return ret;
	} else {
	    String[] ret = { m_seq.getName() };
	    return ret;
	}*/
        int arraysize = 1; //at the least we need the name.
        if (showsGenes()) {
            arraysize++;
        }
        if (showsPrimers()) {
            arraysize++;
        }
        if (showsComments()) {
            arraysize++;
        }
        String[] ret = new String[arraysize];

        int index = 0;

        ret[index] = m_seq.getName(); //name
        index++;
        if (showsGenes()) {
            ret[index] = "-> Genes";
            index++;
        }
        if (showsPrimers()) {
            ret[index] = "-> Primers";
            index++;
        }
        if (showsComments()) {
            ret[index] = "-> Comments";
            index++;
        }
        return ret;

    }

    /**
     * render the display to a graphics component
     *
     * @param sg the graphics component to render to
     */
    public void renderDisplay(Graphics sg) {

        Graphics2D g = (Graphics2D) sg;
        Rectangle r = sg.getClipBounds();
        int x1pos = (int) r.getX() - 5;
        int x2pos = (int) (r.getX() + r.getWidth()) + 5;
        int start = Math.max(0, graphicsToSequence(x1pos));
        int stop = Math.min(m_seq.length(), graphicsToSequence(x2pos));

        int translated = 0;

        int vstart = sequenceToGraphics(m_view.getMin());
        int vstop = sequenceToGraphics(m_view.getMax());

        renderVisibleArea(g, start, stop);

        //show the difference line
        renderDiffs(g, start, stop, showsSubs(), showsIndels());
        g.translate(0, (m_diffHeight + m_spacing));
        translated += (m_diffHeight + m_spacing);


        if (showsGenes()) {
            renderGenes(g, StrandedFeature.POSITIVE, start, stop);
            g.translate(0, m_geneLayers.size() * (m_geneHeight + m_spacing));
            translated += (m_geneLayers.size() * (m_geneHeight + m_spacing));
        }

        if (showsPrimers()) {
            renderPrimers(g, StrandedFeature.POSITIVE, start, stop);
            g.translate(0, m_primerLayers.size() * (m_primerHeight + m_spacing));
            translated += (m_primerLayers.size() * (m_primerHeight + m_spacing));
        }

        if (showsComments()) {
            renderComments(g, StrandedFeature.POSITIVE, start, stop);
            g.translate(0, m_commentLayers.size() * (m_commentHeight + m_spacing));
            translated += (m_commentLayers.size() * (m_commentHeight + m_spacing));
            //System.out.println("COMMENT LAYERS.SIZE: " + m_commentLayers.size() +" m_commentHeight + m_spacing: " + (m_commentHeight + m_spacing) );
        }

        g.translate(0, -translated);

        g.setPaint(Color.orange);
        if (m_useView) {
            g.drawLine(vstart, 1, vstart, getHeight() - 1);
            g.drawLine(vstop, 1, vstop, getHeight() - 1);
        }
    }

    /**
     * draw the visible area mask
     *
     * @param g     !
     * @param start !
     * @param stop  !
     */
    protected void renderVisibleArea(
            Graphics2D g,
            int start,
            int stop) {
        boolean drawStart = (m_dispStart != -1);
        boolean drawStop = (m_dispStop != -1);

        if (start > stop) {
            return;
        }

        g.setPaint(new Color(200, 200, 250));

        if (drawStart) {
            g.fillRect(
                    0,
                    0,
                    sequenceToGraphics(m_dispStart),
                    getHeight() - 1);
        }

        if (drawStop) {
            g.fillRect(
                    sequenceToGraphics(m_dispStop),
                    0,
                    stop - sequenceToGraphics(m_dispStop),
                    getHeight() - 1);
        }
    }

    /**
     * draw the gene row
     *
     * @param g      !
     * @param strand !
     * @param start  !
     * @param stop   !
     */
    protected void renderGenes(
            Graphics2D g,
            StrandedFeature.Strand strand,
            int start,
            int stop) {
        int astart = Math.max(1, m_seq.getAbsolutePosition(start));
        int astop = m_seq.getAbsolutePosition(stop);

        if (astop < 0) {
            astop = Integer.MAX_VALUE;
        }

        for (int i = 0; i < m_geneRects.length; ++i) {
            if (m_geneRects[i] == null) {
                continue;
            }

            //if ( m_geneRects[i].x > astop || (m_geneRects[i].x+m_geneRects[i].width) < astart ) continue;
            Color topcol = new Color(255, 150, 150);
            Color botcol = new Color(150, 150, 255);
            g.setPaint((m_geneStrand[i]) ? topcol : botcol);

            int gstart = sequenceToGraphics(m_geneRects[i].x);
            int gstop = sequenceToGraphics(m_geneRects[i].x + m_geneRects[i].width);

            g.fillRect(gstart, m_geneRects[i].y, gstop - gstart + 1, m_geneRects[i].height);
        }
    }


    /**
     * draw the gene row
     *
     * @param g      !
     * @param strand !
     * @param start  !
     * @param stop   !
     */
    protected void renderPrimers(
            Graphics2D g,
            StrandedFeature.Strand strand,
            int start,
            int stop) {

        for (int i = 0; i < m_primerRects.length; ++i) {
            if (m_primerRects[i] == null) {
                continue;
            }

            //if ( m_geneRects[i].x > astop || (m_geneRects[i].x+m_geneRects[i].width) < astart ) continue;

            Color topcol = new Color(000, 000, 000);//255, 200, 200);
            Color botcol = new Color(000, 000, 000);//200, 200, 255);

            g.setPaint((m_primerStrand[i]) ? topcol : botcol);

            int gstart = sequenceToGraphics(m_primerRects[i].x);
            int gstop = sequenceToGraphics(m_primerRects[i].x + m_primerRects[i].width);

            g.fillRect(gstart, m_primerRects[i].y, gstop - gstart + 1, m_primerRects[i].height);
        }
    }


    /**
     * draw the events (ie comments) row
     *
     * @param g      !
     * @param strand !
     * @param start  !
     * @param stop   !
     */
    protected void renderComments(
            Graphics2D g,
            StrandedFeature.Strand strand,
            int start,
            int stop) {

        for (int i = 0; i < m_commentRects.length; ++i) {

            if (m_commentRects[i] == null) {
                continue;
            }

            //if ( m_geneRects[i].x > astop || (m_geneRects[i].x+m_geneRects[i].width) < astart ) continue;
            //Color topcol = new Color(255, 0, 255);
            //Color botcol = new Color(200, 150, 255);
            Color topcol = m_commentColor[i];
            Color botcol = m_commentColor[i];
            g.setPaint((m_commentStrand[i]) ? topcol : botcol);

            int gstart = sequenceToGraphics(m_commentRects[i].x);
            int gstop = sequenceToGraphics(m_commentRects[i].x + m_commentRects[i].width);

            g.fillRect(gstart, m_commentRects[i].y, gstop - gstart + 1, m_commentRects[i].height);
        }
    }


    /**
     * render the differences row!
     *
     * @param g       graphics
     * @param start   start index of sequence
     * @param stop    stop index of sequence
     * @param display_subs specifies whether to display the substitutions or not, set by setShowSubs()
     * @param display_indels specifies whether to display the insertions/deletions or not, set by setShowIndels()
     */
    protected void renderDiffs(Graphics2D g, int start, int stop, boolean display_subs, boolean display_indels) {

        int gstart = sequenceToGraphics(start);
        int gstop = sequenceToGraphics(stop);

        if (display_subs || display_indels) {

            g.setPaint(Color.black);

            int scale = (m_scale < 1) ? 1 : (int) m_scale;

            for (int i = start; i < stop; i += scale) {
                SummaryIndicator si;

                if (m_scale < 1) {
                    si = m_model.getIndicator(m_model.getIndex(m_seq), i);
                } else {
                    si = m_model.getIndicator(m_model.getIndex(m_seq), i, i + scale);
                }

                if (si.getType() == SummaryIndicator.IND_EMPTY) {
                    continue;
                }

                g.setPaint(si.getColor());

                if (si.getType() == SummaryIndicator.IND_TICK) {

                    if (m_scale < 1) {
                        //if display_subs is true and color is blue
                        if (display_subs && g.getColor().equals(Color.blue)) {
                            myFillRect(g, sequenceToGraphics(i), 0, sequenceToGraphics(i + 1), m_diffHeight);
                        }
                        //if display_indels is true and colour is green or red
                        if (display_indels && (g.getColor().equals(Color.green) || g.getColor().equals(Color.red))) {
                            myFillRect(g, sequenceToGraphics(i), 0, sequenceToGraphics(i + 1), m_diffHeight);
                        }
                    } else {
                        //if display_subs is true and color is blue
                        if (display_subs && g.getColor().equals(Color.blue)) {
                            g.drawLine(sequenceToGraphics(i), 0, sequenceToGraphics(i), m_diffHeight);
                        }
                        //if display_indels is true and colour is green or red
                        if (display_indels && (g.getColor().equals(Color.green) || g.getColor().equals(Color.red))) {
                            g.drawLine(sequenceToGraphics(i), 0, sequenceToGraphics(i), m_diffHeight);
                        }
                    }
                } else if (si.getType() == SummaryIndicator.IND_GAP) {
                    g.drawLine(sequenceToGraphics(i), m_diffHeight / 2, sequenceToGraphics(i + scale), m_diffHeight / 2);
                }
            }
        }
        g.setPaint(Color.black);
        g.drawLine(gstart, 0, gstop, 0);
        g.drawLine(gstart, m_diffHeight, gstop, m_diffHeight);
    }

    /**
     * paint using coords instead of dimensions
     *
     * @param g  the graphics context
     * @param x1 corner1 x
     * @param y1 corner1 y
     * @param x2 opposite corner x
     * @param y2 opposite corner y
     */
    protected void myFillRect(
            Graphics g,
            int x1,
            int y1,
            int x2,
            int y2) {
//        System.out.println("["+x1+","+y1+","+(x2-x1)+","+(y2-y1)+"]");
        g.fillRect(x1, y1, (x2 - x1), (y2 - y1));
    }

    /**
     * Fills the m_geneRects, m_geneStrand and m_geneName arrays with information from the features on the sequence.
     */
    protected void refreshGenePositions() {
        FeatureFilter nfilt = new FeatureFilter.ByType(FeatureType.GENE);
        int featureCount = m_seq.filter(nfilt, false).countFeatures();

        m_geneRects = new Rectangle[featureCount];
        m_geneStrand = new boolean[featureCount];
        m_geneName = new String[featureCount];

        int cnt = 0;

        for (int i = 0; i < m_geneLayers.size(); ++i) {
            double y = i * (m_geneHeight + m_spacing);

            FeatureHolder fh = (FeatureHolder) m_geneLayers.get(i);
            Iterator j = fh.features();

            while (j.hasNext()) {
                StrandedFeature f = (StrandedFeature) j.next();
                Location l = f.getLocation();
                String name = f.getAnnotation().getProperty(AnnotationKeys.NAME).toString();

                StrandedFeature.Strand strand = f.getStrand();

                int start = m_seq.getRelativePosition(l.getMin());
                int stop = m_seq.getRelativePosition(l.getMax());

                m_geneRects[cnt] = new Rectangle(start, (int) y, (stop - start + 1), m_geneHeight);
                m_geneStrand[cnt] = (strand == StrandedFeature.POSITIVE);
                m_geneName[cnt] = name;

                ++cnt;
            }
        }
    }

    /**
     * @author Daniel Horspool
     * copied from refreshgenepositions and reworked for primers
     * Fills the m_primerRects, m_geneStrand and m_geneName arrays with information from the features on the sequence.
     */
    protected void refreshPrimerPositions() {
        FeatureFilter nfilt = new FeatureFilter.ByType(FeatureType.PRIMER);
        int featureCount = m_seq.filter(nfilt, false).countFeatures();

        m_primerRects = new Rectangle[featureCount];
        m_primerStrand = new boolean[featureCount];
        m_primerName = new String[featureCount];

        int cnt = 0;

        for (int i = 0; i < m_primerLayers.size(); ++i) {
            double y = i * (m_primerHeight + m_spacing);

            FeatureHolder fh = (FeatureHolder) m_primerLayers.get(i);
            Iterator j = fh.features();

            while (j.hasNext()) {
                StrandedFeature f = (StrandedFeature) j.next();
                Location l = f.getLocation();
                String name = f.getAnnotation().getProperty(AnnotationKeys.NAME).toString();

                StrandedFeature.Strand strand = f.getStrand();

                int start = m_seq.getRelativePosition(l.getMin());
                int stop = m_seq.getRelativePosition(l.getMax());

                m_primerRects[cnt] = new Rectangle(start, (int) y, (stop - start + 1), m_primerHeight);
                m_primerStrand[cnt] = (strand == StrandedFeature.POSITIVE);
                m_primerName[cnt] = name;

                ++cnt;
            }
        }
    }


    /**
     * @author Daniel Horspool
     * copied from refreshgenepositions and reworked for comments
     * Fills the m_commentRects, m_commentStrand and m_commentName arrays with information from the features on the sequence.
     */
    protected void refreshCommentPositions() {
        FeatureFilter nfilt = new FeatureFilter.ByType(FeatureType.COMMENT);
        int featureCount = m_seq.filter(nfilt, false).countFeatures();

        m_commentRects = new Rectangle[featureCount];
        m_commentStrand = new boolean[featureCount];
        m_commentName = new String[featureCount];
        m_commentColor = new Color[featureCount];

        int cnt = 0;

        for (int i = 0; i < m_commentLayers.size(); ++i) {
            double y = i * (m_commentHeight + m_spacing);

            FeatureHolder fh = (FeatureHolder) m_commentLayers.get(i);
            Iterator j = fh.features();

            while (j.hasNext()) {
                StrandedFeature f = (StrandedFeature) j.next();
                Location l = f.getLocation();
                String name = f.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT).toString();
                //Text Color
                Color fgcolor = (Color) f.getAnnotation().getProperty(AnnotationKeys.FGCOLOR);
                //Comment Color
                Color bgcolor = (Color) f.getAnnotation().getProperty(AnnotationKeys.BGCOLOR);

                StrandedFeature.Strand strand = f.getStrand();

                int start = m_seq.getRelativePosition(l.getMin());
                int stop = m_seq.getRelativePosition(l.getMax());

                m_commentRects[cnt] = new Rectangle(start, (int) y, (stop - start + 1), m_commentHeight);
                m_commentStrand[cnt] = (strand == StrandedFeature.POSITIVE);
                m_commentName[cnt] = name;
                m_commentColor[cnt] = bgcolor;


                ++cnt;
            }
        }
    }


    /**
     * Layer the differences into the different feature holders
     */
    protected void layerGenes() {
        layerChannel(FeatureType.GENE, m_geneLayers);
    }

    protected void layerPrimers() {
        layerChannel(FeatureType.PRIMER, m_primerLayers);
    }

    protected void layerComments() {
        layerChannel(FeatureType.COMMENT, m_commentLayers);
    }

    protected void layerChannel(String type, java.util.List thelist) {
        thelist.removeAll(new ArrayList(thelist));

        for (int i = 0; i < 10; ++i) {
            thelist.add(new SimpleFeatureHolder());
        }

        FeatureFilter nfilt = new FeatureFilter.ByType(type);

        FeatureTools.layerFeatures(thelist, m_seq.filter(nfilt, false).features());

        for (int i = 0; i < thelist.size(); ++i) {
            if (((FeatureHolder) thelist.get(i)).countFeatures() == 0) {
                thelist.remove(thelist.get(i));
                --i;
            }
        }
    }


}
