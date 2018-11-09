package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import ca.virology.baseByBase.gui.CodeHop.CodeHopWizard;
import org.biojava.bio.Annotation;
import org.biojava.bio.gui.sequence.FeatureRenderer;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;

public class GeneRenderer2 extends AbstractChangeable implements FeatureRenderer {
    protected Paint m_outline;
    protected Paint m_fill;
    protected Map m_geneColors;
    protected double m_blockDepth;
    protected Font m_font;

    public GeneRenderer2() {
        this.m_outline = Color.blue;
        this.m_fill = Color.blue;
        this.m_geneColors = new HashMap();
        this.m_blockDepth = 10.0D;
        this.m_font = new Font("", 0, 10);
    }

    public void setColorMap(Map colorMap) throws ChangeVetoException {
        //TODO: hasListeners has been deprecated
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SequenceRenderContext.REPAINT);
            synchronized (cs) {
                ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.REPAINT, colorMap, this.m_geneColors);
                cs.firePreChangeEvent(ce);
                this.m_geneColors = colorMap;
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_geneColors = colorMap;
        }

    }

    public void setFill(Paint p) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SequenceRenderContext.REPAINT);
            synchronized (cs) {
                ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.REPAINT, p, this.m_fill);
                cs.firePreChangeEvent(ce);
                this.m_fill = p;
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_fill = p;
        }

    }

    public Paint getFill() {
        return this.m_fill;
    }

    public void setOutline(Paint p) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SequenceRenderContext.REPAINT);
            synchronized (cs) {
                ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.REPAINT);
                cs.firePreChangeEvent(ce);
                this.m_outline = p;
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_outline = p;
        }

    }

    public Paint getOutline() {
        return this.m_outline;
    }

    public void setBlockDepth(double depth) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(SequenceRenderContext.LAYOUT);
            synchronized (cs) {
                ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.LAYOUT);
                cs.firePreChangeEvent(ce);
                this.m_blockDepth = depth;
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.m_blockDepth = depth;
        }

    }

    public double getDepth(SequenceRenderContext src) {
        return this.m_blockDepth + 1.0D;
    }

    public void renderFeature(Graphics2D g, Feature f, SequenceRenderContext context) {
        Location loc = f.getLocation();
        Iterator i = loc.blockIterator();
        Location last = null;
        Annotation a = f.getAnnotation();

        Integer id;
        try {
            id = (Integer) a.getProperty("id");
            a.getProperty("name");
            a.getProperty("SequenceViewerActor");
        } catch (Exception var11) {
            return;
        }

        Object color = (Paint) this.m_geneColors.get(id);
        if (color == null) {
            color = this.m_fill;
        }

        if (((String) ((String) f.getAnnotation().getProperty("gene_fragment"))).equals("mature_protein")) {
            color = new Color(85, 0, 238);
        }

        Location next;
        if (this.getStrand(f).equals(StrandedFeature.POSITIVE)) {
            if (i.hasNext()) {
                last = (Location) i.next();
                if (!i.hasNext()) {
                    this.renderArrowLocation(g, last, false, context);
                } else {
                    this.renderLocation(g, last, (Paint) color, context);
                }
            }

            for (; i.hasNext(); last = next) {
                next = (Location) i.next();
                this.renderLink(g, f, last, next, context);
                if (!i.hasNext()) {
                    this.renderArrowLocation(g, next, false, context);
                } else {
                    this.renderLocation(g, next, (Paint) color, context);
                }
            }
        } else if (this.getStrand(f).equals(StrandedFeature.NEGATIVE)) {
            if (i.hasNext()) {
                last = (Location) i.next();
                this.renderArrowLocation(g, last, true, context);
            }

            while (i.hasNext()) {
                next = (Location) i.next();
                this.renderLink(g, f, last, next, context);
                this.renderLocation(g, next, (Paint) color, context);
                last = next;
            }
        } else {
            if (i.hasNext()) {
                last = (Location) i.next();
                this.renderLocation(g, last, (Paint) color, context);
            }

            while (i.hasNext()) {
                next = (Location) i.next();
                this.renderLink(g, f, last, next, context);
                this.renderLocation(g, next, (Paint) color, context);
                last = next;
            }
        }

    }

    protected void renderArrowLocation(Graphics2D g, Location loc, boolean lead, SequenceRenderContext context) {

        int[] x = new int[0];
        int[] y = new int[0];
        int min = (int) context.sequenceToGraphics(loc.getMin());
        int max = (int) context.sequenceToGraphics(loc.getMax() + 1);
        boolean points = false;
        byte points1;
        if (max - min <= 3) {
            x = new int[4];
            y = new int[4];
            x[0] = min;
            y[0] = 0;
            x[1] = min;
            y[1] = (int) this.m_blockDepth;
            x[2] = max;
            y[2] = (int) this.m_blockDepth;
            x[3] = max;
            y[3] = 0;
            points1 = 4;
        } else {
            x = new int[5];
            y = new int[5];
            boolean poly = false;
            int poly1;
            if ((double) (max - min) < this.m_blockDepth / 2.0D) {
                poly1 = max - min;
            } else {
                poly1 = (int) this.m_blockDepth / 2;
            }

            if (lead) {
                x[0] = max;
                y[0] = 0;
                x[1] = max;
                y[1] = (int) this.m_blockDepth;
                x[2] = min + poly1;
                y[2] = (int) this.m_blockDepth;
                x[3] = min;
                y[3] = (int) this.m_blockDepth / 2;
                x[4] = min + poly1;
                y[4] = 0;
            } else {
                x[0] = min;
                y[0] = 0;
                x[1] = min;
                y[1] = (int) this.m_blockDepth;
                x[2] = max - poly1;
                y[2] = (int) this.m_blockDepth;
                x[3] = max;
                y[3] = (int) this.m_blockDepth / 2;
                x[4] = max - poly1;
                y[4] = 0;
            }

            points1 = 5;
        }


        Polygon poly2 = new Polygon(x, y, points1);

        g.setColor(Color.BLUE);
        g.fill(poly2);
        g.setColor(new Color(255, 227, 176));
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        String primerName = CodeHopWizard.getPrimerName(loc.getMin(), lead);
        int width = g.getFontMetrics().stringWidth(primerName);
        g.drawString(primerName, (x[0] + x[4]) / 2 - width / 2, y[1] - 1);  //draw the primerName center of the primer rectangle


    }

    protected void renderLocation(Graphics2D g, Location loc, Paint color, SequenceRenderContext context) {
        Rectangle2D.Double block = new Rectangle2D.Double();
        double min = context.sequenceToGraphics(loc.getMin());
        double max = context.sequenceToGraphics(loc.getMax() + 1);
        if (context.getDirection() == 0) {
            block.setFrame(min, 0.0D, max - min, this.m_blockDepth);
            if (color != null) {
                g.setPaint(color);
                g.fill(block);
            }

        }
    }

    protected StrandedFeature.Strand getStrand(Feature f) {
        if (f instanceof StrandedFeature) {
            return ((StrandedFeature) f).getStrand();
        } else {
            FeatureHolder fh = f.getParent();
            return fh instanceof Feature ? this.getStrand((Feature) fh) : StrandedFeature.UNKNOWN;
        }
    }

    protected void renderLink(Graphics2D g, Feature f, Location source, Location dest, SequenceRenderContext context) {
        java.awt.geom.Line2D.Double line = new java.awt.geom.Line2D.Double();
        double half = this.m_blockDepth * 0.5D;
        java.awt.geom.Point2D.Double startP;
        java.awt.geom.Point2D.Double midP;
        java.awt.geom.Point2D.Double endP;
        double start;
        double end;
        double mid;
        if (context.getDirection() == 0) {
            if (this.getStrand(f) == StrandedFeature.NEGATIVE) {
                start = context.sequenceToGraphics(dest.getMin());
                end = context.sequenceToGraphics(source.getMax() + 1);
                mid = (start + end) * 0.5D;
                startP = new java.awt.geom.Point2D.Double(start, half);
                midP = new java.awt.geom.Point2D.Double(mid, this.m_blockDepth);
                endP = new java.awt.geom.Point2D.Double(end, half);
            } else {
                start = context.sequenceToGraphics(source.getMax());
                end = context.sequenceToGraphics(dest.getMin() + 1);
                mid = (start + end) * 0.5D;
                startP = new java.awt.geom.Point2D.Double(start, half);
                midP = new java.awt.geom.Point2D.Double(mid, 0.0D);
                endP = new java.awt.geom.Point2D.Double(end, half);
            }
        } else if (this.getStrand(f) == StrandedFeature.NEGATIVE) {
            start = context.sequenceToGraphics(dest.getMin());
            end = context.sequenceToGraphics(source.getMax() + 1);
            mid = (start + end) * 0.5D;
            startP = new java.awt.geom.Point2D.Double(half, start);
            midP = new java.awt.geom.Point2D.Double(this.m_blockDepth, mid);
            endP = new java.awt.geom.Point2D.Double(half, end);
        } else {
            start = context.sequenceToGraphics(source.getMax());
            end = context.sequenceToGraphics(dest.getMin() + 1);
            mid = (start + end) * 0.5D;
            startP = new java.awt.geom.Point2D.Double(half, start);
            midP = new java.awt.geom.Point2D.Double(0.0D, mid);
            endP = new java.awt.geom.Point2D.Double(half, end);
        }

        g.setPaint(this.getOutline());
        line.setLine(startP, midP);
        g.draw(line);
        line.setLine(midP, endP);
        g.draw(line);
    }

    public FeatureHolder processMouseEvent(FeatureHolder hits, SequenceRenderContext src, MouseEvent me) {
        return hits;
    }
}

