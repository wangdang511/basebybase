package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import ca.virology.baseByBase.gui.CodeHop.CodeHopWizard;
import org.biojava.bio.gui.sequence.GUITools;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.gui.sequence.SequenceRenderer;
import org.biojava.bio.gui.sequence.SequenceViewerEvent;
import org.biojava.bio.symbol.Location;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class RulerRenderer2 implements SequenceRenderer {
    public static final int TICKS_UP = 0;
    public static final int TICKS_DOWN = 1;
    private Line2D line;
    private FontRenderContext fontRx;
    private double depth;
    private AffineTransform antiQuarter;
    private int tickDirection;
    private float tickHeight;
    private float horizLabelOffset;
    private float vertLabelOffset;

    public RulerRenderer2() throws IllegalArgumentException {
        this(1);
    }

    public RulerRenderer2(int tickDirection) throws IllegalArgumentException {
        this.line = new Line2D.Double();
        this.fontRx = new FontRenderContext((AffineTransform) null, true, true);
        this.antiQuarter = AffineTransform.getRotateInstance(Math.toRadians(-90.0D));
        if (tickDirection != 0 && tickDirection != 1) {
            throw new IllegalArgumentException("Tick direction may only be set to RulerRenderer.TICKS_UP or RulerRenderer.TICKS_DOWN");
        } else {
            this.tickDirection = tickDirection;
            this.depth = 20.0D;
            this.tickHeight = 4.0F;
            this.horizLabelOffset = (float) this.depth - this.tickHeight - 2.0F;
            this.vertLabelOffset = (float) this.depth - (this.tickHeight + 2.0F) * 2.0F;
        }
    }

    public double getMinimumLeader(SequenceRenderContext context) {
        return 0.0D;
    }

    public double getMinimumTrailer(SequenceRenderContext context) {
        return 0.0D;
    }

    public double getDepth(SequenceRenderContext src) {
        return this.depth + 1.0D;
    }

    public void paint(Graphics2D g2, SequenceRenderContext context) {
        AffineTransform prevTransform = g2.getTransform();
        g2.setPaint(Color.black);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        Location visible = GUITools.getVisibleRange(context, g2);
        g2.draw(this.line);
        if (visible != Location.empty) {

            int tickGap = 60;


            int min = visible.getMin(); //min sequence position
            //int max = visible.getMax(); //max sequence position (the range of numbers visible on ruler)
            int max = CodeHopWizard.wholeSequenceConsensus.length() + tickGap;
            double minX = context.sequenceToGraphics(min);  //min visible position in panel on x axis (different unit, always start at 0)
            double maxX = context.sequenceToGraphics(max);  //min visible position in panel on x axis
            double scale = context.getScale();
            double halfScale = scale * 0.5D;

            //draw line across screen that is displayed
            this.line.setLine(minX - halfScale, 0.0D, maxX + halfScale, 0.0D);
            g2.draw(this.line);


            //draw labels across where the line is
            FontMetrics fMetrics = g2.getFontMetrics();
            int snapSymsPerGap = tickGap; //ruler increments
            int minP = min + (snapSymsPerGap - min) % snapSymsPerGap;
            String consensus = CodeHopWizard.wholeSequenceConsensus;
            int val1;
            int val2;


            for (int index = minP; index <= max; index += snapSymsPerGap) {
                double ConsensusOffset = context.sequenceToGraphics(index - snapSymsPerGap); //offset is distance between each tick
                double offset = context.sequenceToGraphics(index); //offset is distance between each tick

                //should have a constant (final?) for the number 7 thoughout the code

                val2 = index;
                if (index > consensus.length()) {
                    val2 = consensus.length();
                }

                val1 = index - tickGap;
                if (val1 < 0) {
                    val1 = 0;
                }

                String labelString = consensus.substring(val1, val2);
                float halfLabelWidth = (float) (fMetrics.stringWidth(String.valueOf(index / 3)) / 2);// where lable sits below tick mark

                float labelWidth = (float) fMetrics.stringWidth(labelString);

                this.line.setLine(offset + halfScale, 0.0D, offset + halfScale, (double) this.tickHeight); //set ticks
                g2.drawString(String.valueOf(index / 3), (float) (offset + halfScale - (double) halfLabelWidth), this.horizLabelOffset + 10);//set number labels
                g2.drawString(labelString, (float) (ConsensusOffset + halfScale), this.horizLabelOffset);//set consensus labels
                g2.draw(this.line); //draw ticks
            }
        }
    }

    public SequenceViewerEvent processMouseEvent(SequenceRenderContext context, MouseEvent me, java.util.List path) {
        path.add(this);
        int sPos = context.graphicsToSequence(me.getPoint());
        return new SequenceViewerEvent(this, (Object) null, sPos, me, path);
    }
}

