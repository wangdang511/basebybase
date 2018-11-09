package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import ca.virology.lib.prefs.VGOPrefs;
import ca.virology.lib.search.MalformedPatternException;
import ca.virology.lib.search.SearchHit;
import ca.virology.lib.search.SearchTools;
import ca.virology.lib.util.common.Logger;
import ca.virology.vgo.data.AbstractAnalysisPlugin;
import ca.virology.vgo.data.GenericInput;
import ca.virology.vgo.gui.*;
import org.biojava.bio.BioException;
import org.biojava.bio.gui.sequence.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import ca.virology.lib.prefs.VGOPrefs;
import ca.virology.lib.search.MalformedPatternException;
import ca.virology.lib.search.SearchHit;
import ca.virology.lib.search.SearchTools;
import ca.virology.lib.util.common.Logger;
import ca.virology.vgo.data.AbstractAnalysisPlugin;
import ca.virology.vgo.data.GenericInput;
import ca.virology.vgo.data.GenericInput.Analysis;
import ca.virology.vgo.gui.AnalysisPluginRenderer;
import ca.virology.vgo.gui.BBBCommentRenderer;
import ca.virology.vgo.gui.BBBPrimerRenderer;
import ca.virology.vgo.gui.GFSLabelRenderer;
import ca.virology.vgo.gui.GFSRenderer;
import ca.virology.vgo.gui.GeneLabelRenderer;
import ca.virology.vgo.gui.GeneRenderer;
import ca.virology.vgo.gui.GenericAnalysisRenderer;
import ca.virology.vgo.gui.LCSRenderer;
import ca.virology.vgo.gui.OpenReadingFrameRenderer;
import ca.virology.vgo.gui.RepeatRegionRenderer;
import ca.virology.vgo.gui.SearchRenderer;
import ca.virology.vgo.gui.StartStopRenderer;
import ca.virology.vgo.gui.TitleRenderer;
import ca.virology.vgo.gui.Utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.biojava.bio.BioException;
import org.biojava.bio.gui.sequence.BumpedRenderer;
import org.biojava.bio.gui.sequence.FeatureBlockSequenceRenderer;
import org.biojava.bio.gui.sequence.FilteringRenderer;
import org.biojava.bio.gui.sequence.MultiLineRenderer;
import org.biojava.bio.gui.sequence.OverlayRendererWrapper;
import org.biojava.bio.gui.sequence.PaddingRenderer;
import org.biojava.bio.gui.sequence.RulerRenderer;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.gui.sequence.SequenceRenderer;
import org.biojava.bio.gui.sequence.SequenceViewerEvent;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.FeatureFilter.And;
import org.biojava.bio.seq.FeatureFilter.ByType;
import org.biojava.bio.seq.FeatureFilter.StrandFilter;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.PointLocation;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

public class VGOSequenceRenderer2 extends AbstractChangeable implements SequenceRenderer {
    protected static final int PADDING = 2;
    public static final ChangeType SELECTION;
    public static final ChangeType SHOWX;
    public static final ChangeType PLUGIN;
    protected boolean m_showORF = false;
    protected boolean m_showSS = false;
    protected boolean m_showBottom = false;
    protected String m_showGeneName = "None";
    protected boolean m_showGeneAnalysis = false;
    protected boolean m_showBBBComments = false;
    protected boolean m_showBBBCommentsLabel = true;
    protected boolean m_showBBBPrimers = false;
    protected boolean m_showBBBPrimersLabel = true;
    protected boolean m_showRepeatRegions = true;
    protected boolean m_showRepeatRegionsLabel = true;
    protected boolean m_showGFSLabel = true;
    protected boolean m_showGFS = true;
    protected boolean m_showDesc = false;
    protected int m_orfLength = 2147483647;
    protected int m_virusID;
    protected String m_dbName;
    protected double m_depth;
    protected MultiLineRenderer m_renderer = null;
    protected MultiLineRenderer m_posRenderer = null; //anything above ruler
    protected MultiLineRenderer m_negRenderer = null; //anything below ruler
    protected MultiLineRenderer m_posGenes = null;
    protected MultiLineRenderer m_negGenes = null;
    protected List m_orfRenderers = null;
    protected List m_ssRenderers = null;
    protected MultiLineRenderer m_posGFS = null;
    protected MultiLineRenderer m_negGFS = null;
    protected GFSRenderer m_GFSRenderer = new GFSRenderer();
    protected GeneRenderer2 m_geneRenderer = new GeneRenderer2();
    protected RulerRenderer2 m_ruler = null;
    protected List m_plugins = null;
    protected List m_genInputs = null;
    protected Map m_searchExp = null;
    protected Map m_posSearchResults = null;
    protected Map m_negSearchResults = null;
    protected Map m_posLCSResults = null;
    protected Map m_negLCSResults = null;
    protected MultiLineRenderer m_posLCSRenderer = null;
    protected MultiLineRenderer m_negLCSRenderer = null;
    protected RepeatRegionRenderer m_repeatRegionRenderer = new RepeatRegionRenderer();
    protected BBBCommentRenderer m_bbbCommentsRenderer = new BBBCommentRenderer();
    protected BBBPrimerRenderer m_bbbPrimersRenderer = new BBBPrimerRenderer();
    protected VGOSequenceRenderer.Selection m_selection;
    protected Map m_highlightMap = new HashMap();
    protected Map m_gfshighlightMap = new HashMap();
    protected Location m_helperTarget = new PointLocation(1);
    protected Point m_helperPoint;
    protected String m_helperDesc;
    protected StrandedFeature.Strand m_helperStrand;
    protected int m_helperY;

    public VGOSequenceRenderer2(int virusID, String dbName) {
        this.m_helperStrand = StrandedFeature.POSITIVE;
        this.m_helperY = 0;
        this.m_virusID = virusID;
        this.m_dbName = dbName;
        this.m_orfRenderers = new ArrayList();
        this.m_ssRenderers = new ArrayList();
        this.m_plugins = new ArrayList();
        this.m_genInputs = new ArrayList();
        this.m_posSearchResults = new HashMap();
        this.m_negSearchResults = new HashMap();
        this.m_searchExp = new HashMap();
        this.m_posLCSResults = new HashMap();
        this.m_negLCSResults = new HashMap();

        try {
            this.setRenderers();
        } catch (Exception var5) {
            Utils.showError("Error setting renderers: " + var5.getMessage());
            Logger.log(var5);
        }

        Logger.println("New Renderer for Virus: " + virusID);

        try {
            this.m_depth = Double.parseDouble(VGOPrefs.getInstance().get_vgoPref("map.rowdepth"));
        } catch (Exception var4) {
            Logger.log(var4);
            this.m_depth = 5.0D;
        }

    }

    public double getDepth(SequenceRenderContext src) {
        return this.m_renderer.getDepth(src) + 1.0D;
    }

    public StrandedFeature.Strand getPointStrand(int loc, SequenceRenderContext src) {
        return (double) loc <= this.getForwardBot(src) ? StrandedFeature.POSITIVE : StrandedFeature.NEGATIVE;
    }

    public double getForwardTop(SequenceRenderContext src) {
        return 0.0D;
    }

    public double getForwardBot(SequenceRenderContext src) {
        return this.m_posRenderer.getDepth(src);
    }

    public double getReverseTop(SequenceRenderContext src) {
        return this.getForwardBot(src) + this.m_ruler.getDepth(src) + 4.0D;
    }

    public double getReverseBot(SequenceRenderContext src) {
        return this.getReverseTop(src) + this.m_negRenderer.getDepth(src);
    }

    public double getMinimumLeader(SequenceRenderContext src) {
        return this.m_renderer.getMinimumLeader(src);
    }

    public double getMinimumTrailer(SequenceRenderContext src) {
        return this.m_renderer.getMinimumTrailer(src);
    }

    public void paint(Graphics2D g2, SequenceRenderContext context) {
        this.paintSelection(g2, context);
        this.paintHighlightMap(g2, context);
        this.paintGFSHighlightMap(g2, context);
        this.m_renderer.paint(g2, context);
        this.paintPointerHelper(g2, context);
    }

    public SequenceViewerEvent processMouseEvent(SequenceRenderContext context, MouseEvent me, List path) {
        path.add(this);
        return this.m_renderer.processMouseEvent(context, me, path);
    }

    protected void setORFRendererLength(int length) throws ChangeVetoException {
        Iterator i = this.m_orfRenderers.iterator();

        while (i.hasNext()) {
            OpenReadingFrameRenderer r = (OpenReadingFrameRenderer) i.next();
            r.setORFLength(length);
        }

    }

    public void setGeneColorMap(Map colormap) throws ChangeVetoException {
        this.m_geneRenderer.setColorMap(colormap);
    }

    public void setGeneHighlightMap(Map map) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SequenceRenderContext.REPAINT);
            ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.REPAINT, map, this.m_highlightMap);
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                this.m_highlightMap = map;
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_highlightMap = map;
        }

    }


    public void removeSearch(String description, SequenceRenderContext src) throws ChangeVetoException {
        HashMap newMap = new HashMap(this.m_posSearchResults);
        newMap.remove(description);
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SequenceRenderContext.REPAINT);
            ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.REPAINT, newMap, this.m_posSearchResults);
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                this.m_posSearchResults.remove(description);
                this.m_negSearchResults.remove(description);
                this.setRenderers();
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_posSearchResults.remove(description);
            this.m_negSearchResults.remove(description);
            this.setRenderers();
        }

    }

    public void mapSearch(final int searchType, String exp, final int mismatches, boolean showResults, final SequenceRenderContext src) throws ChangeVetoException, MalformedPatternException {
        final String expression = exp.toUpperCase();
        if (this.m_searchExp.get(expression) != null) {
            Utils.showWarning("That search expression has already been mapped.", (Component) null);
        } else {
            SymbolList init = src.getSymbols().subList(1, Math.min('?', src.getSymbols().length()));
            SearchHit[] ihits = SearchTools.search(searchType, init.seqString().toUpperCase(), expression, mismatches);
            double totals = (double) ihits.length * ((double) src.getSymbols().length() / 50000.0D);
            if (totals * 2.0D > 500.0D) {
                int results = JOptionPane.showConfirmDialog((Component) null, "This search could result in over 500 hits, would you like to continue?", "Question", 0);
                if (results == 1) {
                    return;
                }
            }

            final ArrayList results1 = new ArrayList();
            final ArrayList revResults = new ArrayList();

            try {
                Utils.invokeWithMessage(new Runnable() {
                    public void run() {
                        SearchHit[] rev;
                        int len;
                        try {
                            rev = SearchTools.search(searchType, src.getSymbols().seqString().toUpperCase(), expression, mismatches);
                            len = 0;

                            while (true) {
                                if (len >= rev.length) {
                                    results1.add(rev);
                                    break;
                                }

                                rev[len] = new SearchHit(rev[len].getType(), rev[len].getConfidence(), rev[len].getStart() + 1, rev[len].getStop() + 1);
                                ++len;
                            }
                        } catch (MalformedPatternException var8) {
                            return;
                        }

                        rev = null;

                        SymbolList var9;
                        try {
                            var9 = DNATools.reverseComplement(src.getSymbols());
                        } catch (IllegalAlphabetException var7) {
                            Logger.log(var7);
                            return;
                        }

                        len = var9.length();
                        SearchHit[] nhits = null;

                        try {
                            nhits = SearchTools.search(searchType, var9.seqString().toUpperCase(), expression, mismatches);
                        } catch (MalformedPatternException var6) {
                            return;
                        }

                        SearchHit[] newNegRes = new SearchHit[nhits.length];

                        for (int j = 0; j < nhits.length; ++j) {
                            newNegRes[nhits.length - 1 - j] = new SearchHit(nhits[j].getType(), nhits[j].getConfidence(), var9.length() - nhits[j].getStop(), var9.length() - nhits[j].getStart());
                        }

                        revResults.add(newNegRes);
                    }
                }, "Searching Sequence, Please Wait...", "Searching");
            } catch (Exception var24) {
                Logger.log(var24);
                Utils.showError("Error Searching Sequence");
                return;
            }

            SearchHit[] phits = (SearchHit[]) ((SearchHit[]) results1.get(0));
            SearchHit[] nhits = (SearchHit[]) ((SearchHit[]) revResults.get(0));

            try {
                nhits = (SearchHit[]) ((SearchHit[]) revResults.get(0));
            } catch (NullPointerException var23) {
                nhits = new SearchHit[0];
            }

            try {
                phits = (SearchHit[]) ((SearchHit[]) results1.get(0));
            } catch (NullPointerException var22) {
                phits = new SearchHit[0];
            }

            if (phits.length == 0 && nhits.length == 0) {
                Utils.showInfoMessage("There were no results from the sequence search of (" + expression + ").", (Component) null);
            } else {
                String description = "";
                if (searchType == 1) {
                    description = "Fuzzy Motif ";
                } else if (searchType == 2) {
                    description = "Regular Expression ";
                }

                description = description + " Search Results (" + expression + ")";
                HashMap newMap = new HashMap(this.m_posSearchResults);
                newMap.put(expression, phits);
                if (this.hasListeners()) {
                    ChangeSupport cs = this.getChangeSupport(SequenceRenderContext.REPAINT);
                    ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.REPAINT, newMap, this.m_posSearchResults);
                    synchronized (cs) {
                        cs.firePreChangeEvent(ce);
                        this.m_posSearchResults.put(description, phits);
                        this.m_negSearchResults.put(description, nhits);
                        this.setRenderers();
                        cs.firePostChangeEvent(ce);
                    }
                } else {
                    this.m_posSearchResults.put(description, phits);
                    this.m_negSearchResults.put(description, nhits);
                    this.setRenderers();
                }

            }
        }
    }

    public void removeLCSResults(String lcsname, SequenceRenderContext src) throws ChangeVetoException {
        HashMap newMap = new HashMap(this.m_posLCSResults);
        FeatureHolder fh = (FeatureHolder) newMap.remove(lcsname);
        Iterator it = fh.features();

        while (it.hasNext()) {
            Feature cs = (Feature) it.next();

            try {
                cs.getSequence().removeFeature(cs);
            } catch (ChangeVetoException var11) {
                var11.printStackTrace();
            } catch (BioException var12) {
                var12.printStackTrace();
            }
        }

        if (this.hasListeners()) {
            ChangeSupport cs1 = this.getChangeSupport(SequenceRenderContext.REPAINT);
            ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.REPAINT, newMap, this.m_posLCSResults);
            synchronized (cs1) {
                cs1.firePreChangeEvent(ce);
                this.m_posLCSResults.remove(lcsname);
                this.m_negLCSResults.remove(lcsname);
                this.setRenderers();
                cs1.firePostChangeEvent(ce);
            }
        } else {
            this.m_posLCSResults.remove(lcsname);
            this.m_negLCSResults.remove(lcsname);
            this.setRenderers();
        }

    }


    public void displayRepeatRegions(boolean value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, new Boolean(value), new Boolean(this.m_showRepeatRegions));
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                if (this.m_showRepeatRegions != value) {
                    this.m_showRepeatRegions = value;
                    this.setRenderers();
                }

                cs.firePostChangeEvent(ce);
            }
        } else if (this.m_showRepeatRegions != value) {
            this.m_showRepeatRegions = value;
            this.setRenderers();
        }

    }


    public void displayBBBComments(boolean value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, new Boolean(value), new Boolean(this.m_showBBBComments));
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                if (this.m_showBBBComments != value) {
                    this.m_showBBBComments = value;
                    this.setRenderers();
                }

                cs.firePostChangeEvent(ce);
            }
        } else if (this.m_showBBBComments != value) {
            this.m_showBBBComments = value;
            this.setRenderers();
        }

    }

    public void displayBBBPrimers(boolean value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, new Boolean(value), new Boolean(this.m_showBBBPrimers));
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                if (this.m_showBBBPrimers != value) {
                    this.m_showBBBPrimers = value;
                    this.setRenderers();
                }

                cs.firePostChangeEvent(ce);
            }
        } else if (this.m_showBBBPrimers != value) {
            this.m_showBBBPrimers = value;
            this.setRenderers();
        }

    }


    public void displayORF(boolean value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, new Boolean(value), new Boolean(this.m_showORF));
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                if (this.m_showORF != value) {
                    this.m_showORF = value;
                    this.setRenderers();
                }

                cs.firePostChangeEvent(ce);
            }
        } else if (this.m_showORF != value) {
            this.m_showORF = value;
            this.setRenderers();
        }

    }

    public void displayLaneDescriptions(boolean value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, new Boolean(value), new Boolean(this.m_showDesc));
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                if (this.m_showDesc != value) {
                    this.m_showDesc = value;
                    this.setRenderers();
                }

                cs.firePostChangeEvent(ce);
            }
        } else if (this.m_showDesc != value) {
            this.m_showDesc = value;
            this.setRenderers();
        }

    }


    public void displayBottom(boolean value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, new Boolean(value), new Boolean(this.m_showBottom));
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                if (this.m_showBottom != value) {
                    this.m_showBottom = value;
                    this.setRenderers();
                }

                cs.firePostChangeEvent(ce);
            }
        } else if (this.m_showBottom != value) {
            this.m_showBottom = value;
            this.setRenderers();
        }

    }


    public void displaySS(boolean value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, new Boolean(value), new Boolean(this.m_showSS));
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                if (this.m_showSS != value) {
                    this.m_showSS = value;
                    this.setRenderers();
                }

                cs.firePostChangeEvent(ce);
            }
        } else if (this.m_showSS != value) {
            this.m_showSS = value;
            this.setRenderers();
        }

    }

    public void displayGFS(boolean value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, new Boolean(value), new Boolean(this.m_showGFS));
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                if (this.m_showGFS != value) {
                    this.m_showGFS = value;
                    this.setRenderers();
                }

                cs.firePostChangeEvent(ce);
            }
        } else if (this.m_showGFS != value) {
            this.m_showGFS = value;
            this.setRenderers();
        }

    }

    public int getORFLength() {
        return this.m_orfLength;
    }

    public void setORFLength(int value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, new Integer(value), new Integer(this.m_orfLength));
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                this.m_orfLength = value;
                this.setORFRendererLength(value);
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_orfLength = value;
            this.setORFRendererLength(value);
        }

    }


    public void displayGeneName(String value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, value, this.m_showGeneName);
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                if (!this.m_showGeneName.equals(value)) {
                    this.m_showGeneName = value;
                    this.setRenderers();
                }

                cs.firePostChangeEvent(ce);
            }
        } else if (!this.m_showGeneName.equals(value)) {
            this.m_showGeneName = value;
            this.setRenderers();
        }

    }

    public void displayGeneAnalysis(boolean value) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SHOWX);
            ChangeEvent ce = new ChangeEvent(this, SHOWX, new Boolean(value), new Boolean(this.m_showGeneAnalysis));
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                if (this.m_showGeneAnalysis != value) {
                    this.m_showGeneAnalysis = value;
                    this.setRenderers();
                }

                cs.firePostChangeEvent(ce);
            }
        } else if (this.m_showGeneAnalysis != value) {
            this.m_showGeneAnalysis = value;
            this.setRenderers();
        }

    }

    public void removePlugin(AbstractAnalysisPlugin pu) throws ChangeVetoException {
        ArrayList newList = new ArrayList(this.m_plugins);
        newList.remove(pu);
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(PLUGIN);
            ChangeEvent ce = new ChangeEvent(this, PLUGIN, newList, this.m_plugins);
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                this.m_plugins.remove(pu);
                this.setRenderers();
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_plugins.remove(pu);
            this.setRenderers();
        }

    }

    public void removeGenericInputAnalysis(GenericInput.Analysis an) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport i = this.getChangeSupport(PLUGIN);
            ChangeEvent gi = new ChangeEvent(this, PLUGIN, (Object) null, (Object) null);
            synchronized (i) {
                i.firePreChangeEvent(gi);
                Iterator i1 = this.m_genInputs.iterator();

                while (i1.hasNext()) {
                    GenericInput gi1 = (GenericInput) i1.next();
                    gi1.removeAnalysis(an);
                }

                this.setRenderers();
                i.firePostChangeEvent(gi);
            }
        } else {
            Iterator i2 = this.m_genInputs.iterator();

            while (i2.hasNext()) {
                GenericInput gi2 = (GenericInput) i2.next();
                gi2.removeAnalysis(an);
            }

            this.setRenderers();
        }

    }


    public void addGenericInput(GenericInput gi) throws ChangeVetoException {
        ArrayList newList = new ArrayList(this.m_genInputs);
        newList.add(gi);
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(PLUGIN);
            ChangeEvent ce = new ChangeEvent(this, PLUGIN, newList, this.m_genInputs);
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                this.m_genInputs.add(gi);
                this.setRenderers();
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_genInputs.add(gi);
            this.setRenderers();
        }

    }


    public void clearSelection() throws ChangeVetoException {
        this.selectRegion(new VGOSequenceRenderer.Selection(0, 0, 0, 0, "", true));
    }

    protected void selectRegion(VGOSequenceRenderer.Selection sel) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SELECTION);
            ChangeEvent ce = new ChangeEvent(this, SELECTION, sel, this.m_selection);
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                this.m_selection = sel;
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_selection = sel;
        }

    }


    public void selectRegion(int min, int max, int top, int bottom, String annotation, boolean done) throws ChangeVetoException {
        VGOSequenceRenderer.Selection newSel = new VGOSequenceRenderer.Selection(min, max, top, bottom, annotation, done);
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SELECTION);
            ChangeEvent ce = new ChangeEvent(this, SELECTION, newSel, this.m_selection);
            synchronized (cs) {
                cs.firePreChangeEvent(ce);
                this.m_selection = newSel;
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_selection = newSel;
        }

    }

    protected void paintPointerHelper(Graphics2D g2, SequenceRenderContext context) {
        Paint p = g2.getPaint();
        if (this.m_helperTarget.getMin() > 0 && this.m_helperPoint != null) {
            Location loc = this.m_helperTarget;
            int x1 = (int) context.sequenceToGraphics(loc.getMin()) - 5;
            int x2 = (int) context.sequenceToGraphics(loc.getMax()) + 5;
            int y1 = this.m_helperY - 5;
            int y2 = this.m_helperY + 5;
            if (this.m_helperStrand == StrandedFeature.POSITIVE) {
                y1 = (int) this.getForwardTop(context);
                y2 = (int) this.getForwardBot(context);
            } else if (this.m_helperStrand == StrandedFeature.NEGATIVE) {
                y1 = (int) this.getReverseTop(context);
                y2 = (int) this.getReverseBot(context);
            } else {
                Logger.println("invalid strand ");
            }

            g2.setPaint(new Color(255, 0, 0, 20));
            g2.fillRoundRect(x1, y1, Math.abs(x2 - x1), Math.abs(y2 - y1), 10, 10);
            g2.setPaint(Color.black);
            g2.drawRoundRect(x1, y1, Math.abs(x2 - x1), Math.abs(y2 - y1), 10, 10);
            JLabel tl = new JLabel(this.m_helperDesc);
            tl.setBorder(BorderFactory.createLineBorder(Color.black));
            tl.setBackground(new Color(255, 255, 220));
            tl.paint(g2);
            g2.setPaint(p);
        }
    }

    protected void paintHighlightMap(Graphics2D g2, SequenceRenderContext context) {
        Paint p = g2.getPaint();
        if (!this.m_highlightMap.isEmpty()) {
            FeatureFilter.ByType ff = new FeatureFilter.ByType("gene");
            FeatureHolder genes = context.getFeatures().filter(ff, false);
            Iterator i = genes.features();

            while (i.hasNext()) {
                StrandedFeature ft = (StrandedFeature) i.next();
                Integer id = (Integer) ft.getAnnotation().getProperty("id");
                if (this.m_highlightMap.containsKey(id)) {
                    Location loc = ft.getLocation();
                    int x1 = (int) context.sequenceToGraphics(loc.getMin());
                    int x2 = (int) context.sequenceToGraphics(loc.getMax());
                    int y1 = 0;
                    int y2 = 0;
                    if (ft.getStrand() == StrandedFeature.POSITIVE) {
                        y1 = (int) this.getForwardTop(context);
                        y2 = (int) this.getForwardBot(context);
                    } else if (ft.getStrand() == StrandedFeature.NEGATIVE) {
                        y1 = (int) this.getReverseTop(context);
                        y2 = (int) this.getReverseBot(context);
                    } else {
                        Logger.println("invalid strand for " + id);
                    }

                    Color myCol = null;
                    Color c = (Color) this.m_highlightMap.get(id);
                    if (c == null) {
                        c = Color.orange;
                    } else {
                        myCol = new Color(c.getRed(), c.getGreen(), c.getBlue(), 100);
                    }

                    g2.setPaint(myCol);
                    g2.fillRect(x1, y1, Math.abs(x2 - x1), Math.abs(y2 - y1));
                }
            }

            g2.setPaint(p);
        }
    }

    protected void paintGFSHighlightMap(Graphics2D g2, SequenceRenderContext context) {
        Paint p = g2.getPaint();
        if (!this.m_gfshighlightMap.isEmpty()) {
            FeatureFilter.ByType ff = new FeatureFilter.ByType("gfs");
            FeatureHolder gfs = context.getFeatures().filter(ff, false);
            Iterator i = gfs.features();

            while (i.hasNext()) {
                StrandedFeature ft = (StrandedFeature) i.next();
                Integer id = (Integer) ft.getAnnotation().getProperty("id");
                if (this.m_gfshighlightMap.containsKey(id)) {
                    Location loc = ft.getLocation();
                    int x1 = (int) context.sequenceToGraphics(loc.getMin());
                    int x2 = (int) context.sequenceToGraphics(loc.getMax());
                    int y1 = 0;
                    int y2 = 0;
                    if (ft.getStrand() == StrandedFeature.POSITIVE) {
                        y1 = (int) this.getForwardTop(context);
                        y2 = (int) this.getForwardBot(context);
                    } else if (ft.getStrand() == StrandedFeature.NEGATIVE) {
                        y1 = (int) this.getReverseTop(context);
                        y2 = (int) this.getReverseBot(context);
                    } else {
                        Logger.println("invalid strand for " + id);
                    }

                    Color myCol = null;
                    Color c = (Color) this.m_gfshighlightMap.get(id);
                    if (c == null) {
                        c = Color.orange;
                    } else {
                        myCol = new Color(c.getRed(), c.getGreen(), c.getBlue(), 100);
                    }

                    g2.setPaint(myCol);
                    g2.fillRect(x1, y1, Math.abs(x2 - x1), Math.abs(y2 - y1));
                }
            }

            g2.setPaint(p);
        }
    }

    protected void paintSelection(Graphics2D g2, SequenceRenderContext context) {
        Paint p = g2.getPaint();
        if (this.m_selection != null) {
            int x1 = (int) context.sequenceToGraphics(this.m_selection.min);
            int y1 = this.m_selection.top;
            int w = (int) (context.sequenceToGraphics(this.m_selection.max) - context.sequenceToGraphics(this.m_selection.min));
            int h = this.m_selection.bottom - this.m_selection.top;
            if (this.m_selection.done) {
                g2.setPaint(new Color(100, 100, 100, 75));
                g2.fillRect(x1, y1, w, h);
            } else {
                g2.setPaint(Color.black);
                g2.drawRect(x1, y1, w, h);
            }

            g2.setPaint(p);
        }
    }

    protected void setRenderers() throws ChangeVetoException {
        long time = System.currentTimeMillis();
        this.m_renderer = new MultiLineRenderer();
        this.m_posLCSRenderer = new MultiLineRenderer();
        this.m_negLCSRenderer = new MultiLineRenderer();
        this.m_posRenderer = new MultiLineRenderer();
        this.m_negRenderer = new MultiLineRenderer();
        this.m_posGenes = new MultiLineRenderer();
        this.m_negGenes = new MultiLineRenderer();
        if (this.m_orfRenderers == null) {
            this.m_orfRenderers = new ArrayList();
        }

        if (this.m_ssRenderers == null) {
            this.m_ssRenderers = new ArrayList();
        }

        this.m_posGFS = new MultiLineRenderer();
        this.m_negGFS = new MultiLineRenderer();
        this.m_geneRenderer.setBlockDepth(10.0D);
        this.m_geneRenderer.setFill(Color.blue);
        FeatureBlockSequenceRenderer features = new FeatureBlockSequenceRenderer();
        features.setFeatureRenderer(this.m_geneRenderer);
        BumpedRenderer lsr = new BumpedRenderer();
        lsr.setRenderer(features);
        this.m_posGenes.addRenderer(new PaddingRenderer(new GeneLabelRenderer(this.m_showGeneName, StrandedFeature.POSITIVE, new Font("", 0, 10)), 1.0D));
        this.m_posGenes.addRenderer(new FilteringRenderer(lsr, new FeatureFilter.And(new FeatureFilter.StrandFilter(StrandedFeature.POSITIVE), new FeatureFilter.ByType("gene")), false));
        this.m_negGenes.addRenderer(new PaddingRenderer(new GeneLabelRenderer(this.m_showGeneName, StrandedFeature.NEGATIVE, new Font("", 0, 10)), 1.0D));
        this.m_negGenes.addRenderer(new FilteringRenderer(lsr, new FeatureFilter.And(new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE), new FeatureFilter.ByType("gene")), false));
        this.m_ruler = new RulerRenderer2();
        this.addSearchRenderers(StrandedFeature.POSITIVE);
        this.addLCSRenderers(StrandedFeature.POSITIVE);
        this.addGenericInputRenderers(StrandedFeature.POSITIVE);
        if (this.m_showGeneAnalysis) {
            this.addPluginRenderers(StrandedFeature.POSITIVE);
        }

        FeatureBlockSequenceRenderer or;
        if (this.m_showGFS) {
            this.m_GFSRenderer.setBlockDepth(10.0D);
            this.m_GFSRenderer.setFill(Color.pink);
            or = new FeatureBlockSequenceRenderer();
            or.setFeatureRenderer(this.m_GFSRenderer);
            BumpedRenderer ex = new BumpedRenderer();
            ex.setRenderer(or);
            this.m_posGFS.addRenderer(new PaddingRenderer(new GFSLabelRenderer(this.m_showGFSLabel, StrandedFeature.POSITIVE, new Font("", 0, 10)), 1.0D));
            this.m_posGFS.addRenderer(new FilteringRenderer(ex, new FeatureFilter.And(new FeatureFilter.ByType("gfs"), new FeatureFilter.StrandFilter(StrandedFeature.POSITIVE)), false));
            this.m_negGFS.addRenderer(new PaddingRenderer(new GFSLabelRenderer(this.m_showGFSLabel, StrandedFeature.NEGATIVE, new Font("", 0, 10)), 1.0D));
            this.m_negGFS.addRenderer(new FilteringRenderer(ex, new FeatureFilter.And(new FeatureFilter.ByType("gfs"), new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE)), false));
            if (this.m_showDesc) {
                this.m_posRenderer.addRenderer(new TitleRenderer("Top Strand GFS"));
            }

            this.m_posRenderer.addRenderer(this.m_posGFS);
        }

        OpenReadingFrameRenderer[] or1;
        if (this.m_showORF) {
            or1 = new OpenReadingFrameRenderer[3];
            if (this.m_orfRenderers.size() == 0) {
                or1[0] = new OpenReadingFrameRenderer(1, 200, StrandedFeature.POSITIVE, this.m_dbName);
                or1[1] = new OpenReadingFrameRenderer(2, 200, StrandedFeature.POSITIVE, this.m_dbName);
                or1[2] = new OpenReadingFrameRenderer(3, 200, StrandedFeature.POSITIVE, this.m_dbName);
                this.m_orfRenderers.add(or1[0]);
                this.m_orfRenderers.add(or1[1]);
                this.m_orfRenderers.add(or1[2]);
            } else {
                or1[0] = (OpenReadingFrameRenderer) this.m_orfRenderers.get(0);
                or1[1] = (OpenReadingFrameRenderer) this.m_orfRenderers.get(1);
                or1[2] = (OpenReadingFrameRenderer) this.m_orfRenderers.get(2);
            }

            if (this.m_showDesc) {
                this.m_posRenderer.addRenderer(new TitleRenderer("Open Reading Frames"));
            }

            this.m_posRenderer.addRenderer(new PaddingRenderer(or1[0], 2.0D));
            this.m_posRenderer.addRenderer(new PaddingRenderer(or1[1], 2.0D));
            this.m_posRenderer.addRenderer(new PaddingRenderer(or1[2], 2.0D));
        }

        StartStopRenderer[] or2;
        if (this.m_showSS) {
            or2 = new StartStopRenderer[3];
            if (this.m_ssRenderers.size() == 0) {
                or2[0] = new StartStopRenderer(1, StrandedFeature.POSITIVE);
                or2[1] = new StartStopRenderer(2, StrandedFeature.POSITIVE);
                or2[2] = new StartStopRenderer(3, StrandedFeature.POSITIVE);
                this.m_ssRenderers.add(or2[0]);
                this.m_ssRenderers.add(or2[1]);
                this.m_ssRenderers.add(or2[2]);
            } else {
                or2[0] = (StartStopRenderer) this.m_ssRenderers.get(0);
                or2[1] = (StartStopRenderer) this.m_ssRenderers.get(1);
                or2[2] = (StartStopRenderer) this.m_ssRenderers.get(2);
            }

            if (this.m_showDesc) {
                this.m_posRenderer.addRenderer(new TitleRenderer("Start / Stop Codons"));
            }

            this.m_posRenderer.addRenderer(new PaddingRenderer(or2[0], 2.0D));
            this.m_posRenderer.addRenderer(new PaddingRenderer(or2[1], 2.0D));
            this.m_posRenderer.addRenderer(new PaddingRenderer(or2[2], 2.0D));
        }

        OverlayRendererWrapper ex1;
        if (this.m_showRepeatRegions) {
            if (this.m_showRepeatRegionsLabel) {
                this.m_posRenderer.addRenderer(new TitleRenderer("Repeat Regions"));
            }

            or = new FeatureBlockSequenceRenderer();
            or.setFeatureRenderer(this.m_repeatRegionRenderer);
            ex1 = new OverlayRendererWrapper();
            ex1.setRenderer(or);
            this.m_posRenderer.addRenderer(new FilteringRenderer(ex1, new FeatureFilter.And(new FeatureFilter.ByType("repeat"), new FeatureFilter.StrandFilter(StrandedFeature.POSITIVE)), false));
        }

        if (this.m_showBBBComments) {
            if (this.m_showBBBCommentsLabel) {
                this.m_posRenderer.addRenderer(new TitleRenderer("BBB Comments"));
            }

            or = new FeatureBlockSequenceRenderer();
            or.setFeatureRenderer(this.m_bbbCommentsRenderer);
            ex1 = new OverlayRendererWrapper();
            ex1.setRenderer(or);
            this.m_posRenderer.addRenderer(new FilteringRenderer(ex1, new FeatureFilter.And(new FeatureFilter.ByType("comment"), new FeatureFilter.StrandFilter(StrandedFeature.POSITIVE)), false));
        }

        if (this.m_showBBBPrimers) {
            if (this.m_showBBBPrimersLabel) {
                this.m_posRenderer.addRenderer(new TitleRenderer("BBB Primers"));
            }

            or = new FeatureBlockSequenceRenderer();
            or.setFeatureRenderer(this.m_bbbPrimersRenderer);
            ex1 = new OverlayRendererWrapper();
            ex1.setRenderer(or);
            this.m_posRenderer.addRenderer(new FilteringRenderer(ex1, new FeatureFilter.And(new FeatureFilter.ByType("primer"), new FeatureFilter.StrandFilter(StrandedFeature.POSITIVE)), false));
        }

        if (this.m_showDesc) {
            this.m_posRenderer.addRenderer(new TitleRenderer("Top Strand Genes"));
        }

        this.m_posRenderer.addRenderer(this.m_posGenes);
        if (this.m_showBottom) {
            if (this.m_showDesc) {
                this.m_negRenderer.addRenderer(new TitleRenderer("Bottom Strand Genes"));
            }

            this.m_negRenderer.addRenderer(this.m_negGenes);
            if (this.m_showGFS) {
                if (this.m_showDesc) {
                    this.m_negRenderer.addRenderer(new TitleRenderer("Bottom Strand GFS"));
                }

                this.m_negRenderer.addRenderer(this.m_negGFS);
            }

            if (this.m_showRepeatRegions) {
                if (this.m_showRepeatRegionsLabel) {
                    this.m_negRenderer.addRenderer(new TitleRenderer("Repeat Regions"));
                }

                or = new FeatureBlockSequenceRenderer();
                or.setFeatureRenderer(this.m_repeatRegionRenderer);
                ex1 = new OverlayRendererWrapper();
                ex1.setRenderer(or);
                this.m_negRenderer.addRenderer(new FilteringRenderer(ex1, new FeatureFilter.And(new FeatureFilter.ByType("repeat"), new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE)), false));
            }

            if (this.m_showBBBComments) {
                if (this.m_showBBBCommentsLabel) {
                    this.m_negRenderer.addRenderer(new TitleRenderer("BBB Comments"));
                }

                or = new FeatureBlockSequenceRenderer();
                or.setFeatureRenderer(this.m_bbbCommentsRenderer);
                ex1 = new OverlayRendererWrapper();
                ex1.setRenderer(or);
                this.m_negRenderer.addRenderer(new FilteringRenderer(ex1, new FeatureFilter.And(new FeatureFilter.ByType("comment"), new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE)), false));
            }

            if (this.m_showBBBPrimers) {
                if (this.m_showBBBPrimersLabel) {
                    this.m_negRenderer.addRenderer(new TitleRenderer("BBB Primers"));
                }

                or = new FeatureBlockSequenceRenderer();
                or.setFeatureRenderer(this.m_bbbPrimersRenderer);
                ex1 = new OverlayRendererWrapper();
                ex1.setRenderer(or);
                this.m_negRenderer.addRenderer(new FilteringRenderer(ex1, new FeatureFilter.And(new FeatureFilter.ByType("primer"), new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE)), false));
            }

            if (this.m_showSS) {
                or2 = new StartStopRenderer[3];
                if (this.m_ssRenderers.size() == 3) {
                    or2[0] = new StartStopRenderer(1, StrandedFeature.NEGATIVE);
                    or2[1] = new StartStopRenderer(2, StrandedFeature.NEGATIVE);
                    or2[2] = new StartStopRenderer(3, StrandedFeature.NEGATIVE);
                    this.m_ssRenderers.add(or2[0]);
                    this.m_ssRenderers.add(or2[1]);
                    this.m_ssRenderers.add(or2[2]);
                } else {
                    or2[0] = (StartStopRenderer) this.m_ssRenderers.get(3);
                    or2[1] = (StartStopRenderer) this.m_ssRenderers.get(4);
                    or2[2] = (StartStopRenderer) this.m_ssRenderers.get(5);
                }

                if (this.m_showDesc) {
                    this.m_negRenderer.addRenderer(new TitleRenderer("Start / Stop Codons"));
                }

                this.m_negRenderer.addRenderer(new PaddingRenderer(or2[0], 2.0D));
                this.m_negRenderer.addRenderer(new PaddingRenderer(or2[1], 2.0D));
                this.m_negRenderer.addRenderer(new PaddingRenderer(or2[2], 2.0D));
            }

            if (this.m_showORF) {
                or1 = new OpenReadingFrameRenderer[3];
                if (this.m_orfRenderers.size() == 3) {
                    or1[0] = new OpenReadingFrameRenderer(1, 200, StrandedFeature.NEGATIVE, this.m_dbName);
                    or1[1] = new OpenReadingFrameRenderer(2, 200, StrandedFeature.NEGATIVE, this.m_dbName);
                    or1[2] = new OpenReadingFrameRenderer(3, 200, StrandedFeature.NEGATIVE, this.m_dbName);
                    this.m_orfRenderers.add(or1[0]);
                    this.m_orfRenderers.add(or1[1]);
                    this.m_orfRenderers.add(or1[2]);
                } else {
                    or1[0] = (OpenReadingFrameRenderer) this.m_orfRenderers.get(3);
                    or1[1] = (OpenReadingFrameRenderer) this.m_orfRenderers.get(4);
                    or1[2] = (OpenReadingFrameRenderer) this.m_orfRenderers.get(5);
                }

                if (this.m_showDesc) {
                    this.m_negRenderer.addRenderer(new TitleRenderer("Open Reading Frames"));
                }

                this.m_negRenderer.addRenderer(new PaddingRenderer(or1[0], 2.0D));
                this.m_negRenderer.addRenderer(new PaddingRenderer(or1[1], 2.0D));
                this.m_negRenderer.addRenderer(new PaddingRenderer(or1[2], 2.0D));

                try {
                    this.setORFRendererLength(this.m_orfLength);
                } catch (ChangeVetoException var7) {
                    Logger.log(var7);
                }
            }

            if (this.m_showGeneAnalysis) {
                this.addPluginRenderers(StrandedFeature.NEGATIVE);
            }

            this.addGenericInputRenderers(StrandedFeature.NEGATIVE);
            this.addSearchRenderers(StrandedFeature.NEGATIVE);
            this.addLCSRenderers(StrandedFeature.NEGATIVE);
        }

        this.m_renderer.addRenderer(this.m_posLCSRenderer);
        this.m_renderer.addRenderer(this.m_posRenderer);
        this.m_renderer.addRenderer(new PaddingRenderer(this.m_ruler, 2.0D));
        if (this.m_showBottom) {
            this.m_renderer.addRenderer(this.m_negRenderer);
        }

    }

    protected void addGenericInputRenderers(StrandedFeature.Strand strand) throws ChangeVetoException {
        MultiLineRenderer rend = strand == StrandedFeature.POSITIVE ? this.m_posRenderer : this.m_negRenderer;
        Iterator i = this.m_genInputs.iterator();

        while (i.hasNext()) {
            GenericInput gi = (GenericInput) i.next();

            GenericInput.Analysis ann;
            for (Iterator j = gi.analysisIterator(); j.hasNext(); rend.addRenderer(new GenericAnalysisRenderer("", ann, strand, this.m_dbName))) {
                ann = (GenericInput.Analysis) j.next();
                if (this.m_showDesc) {
                    rend.addRenderer(new TitleRenderer(ann.getName()));
                }
            }
        }

    }

    protected void addPluginRenderers(StrandedFeature.Strand strand) throws ChangeVetoException {
        MultiLineRenderer rend = strand == StrandedFeature.POSITIVE ? this.m_posRenderer : this.m_negRenderer;

        AbstractAnalysisPlugin pu;
        for (Iterator i = this.m_plugins.iterator(); i.hasNext(); rend.addRenderer(new FilteringRenderer(new AnalysisPluginRenderer(pu, strand, this.m_dbName), new FeatureFilter.StrandFilter(strand), false))) {
            pu = (AbstractAnalysisPlugin) i.next();
            if (this.m_showDesc) {
                rend.addRenderer(new TitleRenderer(pu.getName() + " (" + pu.getShortDescription() + ")"));
            }
        }

    }

    protected void addSearchRenderers(StrandedFeature.Strand strand) throws ChangeVetoException {
        MultiLineRenderer rend = strand == StrandedFeature.POSITIVE ? this.m_posRenderer : this.m_negRenderer;
        Map results = strand == StrandedFeature.POSITIVE ? this.m_posSearchResults : this.m_negSearchResults;
        TreeSet names = new TreeSet(results.keySet());

        String desc;
        SearchHit[] value;
        for (Iterator i = names.iterator(); i.hasNext(); rend.addRenderer(new SearchRenderer(value, desc, strand))) {
            desc = (String) i.next();
            value = (SearchHit[]) ((SearchHit[]) results.get(desc));
            if (this.m_showDesc) {
                rend.addRenderer(new TitleRenderer(desc));
            }
        }

    }

    protected void addLCSRenderers(StrandedFeature.Strand strand) throws ChangeVetoException {
        MultiLineRenderer rend = strand == StrandedFeature.POSITIVE ? this.m_posLCSRenderer : this.m_negLCSRenderer;
        Map results = strand == StrandedFeature.POSITIVE ? this.m_posLCSResults : this.m_negLCSResults;
        TreeSet names = new TreeSet(results.keySet());

        String lcsname;
        FeatureHolder lcsfeatures;
        for (Iterator i = names.iterator(); i.hasNext(); rend.addRenderer(new LCSRenderer(lcsname, strand, lcsfeatures))) {
            lcsname = (String) i.next();
            lcsfeatures = (FeatureHolder) results.get(lcsname);
            if (this.m_showDesc) {
                rend.addRenderer(new TitleRenderer(lcsname));
            }
        }

    }

    static {
        SELECTION = new ChangeType("Selection Changed", "ca.virology.vgo.gui.VGOSequenceRenderer", "SELECTION", SequenceRenderContext.LAYOUT);
        SHOWX = new ChangeType("Renderer configuration changed", "ca.virology.vgo.gui.VGOSequenceRenderer", "SHOWX", SequenceRenderContext.LAYOUT);
        PLUGIN = new ChangeType("Plugin configuration changed", "ca.virology.vgo.gui.VGOSequenceRenderer", "PLUGIN", SequenceRenderContext.LAYOUT);
    }

    public static final class Selection {
        public boolean done;
        public int min;
        public int max;
        public int top;
        public int bottom;
        public String annotation;

        public Selection(int min, int max, int top, int bottom, String ann, boolean done) {
            if (min > max) {
                this.min = max;
                this.max = min;
            } else {
                this.min = min;
                this.max = max;
            }

            if (top > bottom) {
                this.bottom = top;
                this.top = bottom;
            } else {
                this.top = top;
                this.bottom = bottom;
            }

            this.annotation = ann;
            this.done = done;
        }
    }
}

