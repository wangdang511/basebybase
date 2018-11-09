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

import ca.virology.baseByBase.data.Consensus;
import ca.virology.baseByBase.data.mRNA;
import ca.virology.baseByBase.data.mRNAs;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.util.gui.UITools;

import org.biojava.bio.seq.StrandedFeature;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.swing.ToolTipManager;


/**
 * this panel displays consensus data and a graph relating a set of  sequences
 * to each other
 *
 * @author Will Hillary - adapted from Ryan Brodie
 * @version 1.0
 */
public class mRNADisplay extends AbstractSequencePanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected mRNA[] mrna;
    protected mRNAs m_mrnas;
    //protected int[] 	posits;

    protected ArrayList posits;
    protected static int m_iHeight = 30;
    protected int m_fontHeight;
    protected int m_fontWidth;
    protected int m_charStart = -1;
    protected int m_charStop = -1;

    PrimaryPanel m_dataPanel;

    //~ Constructors ///////////////////////////////////////////////////////////


    public mRNADisplay(int height) {
        this(null, height);
    }

    /**
     * Creates a new ConsensusDisplay object.
     *
     * @param cons the target consensus
     */
    public mRNADisplay(mRNAs mrnas) {
        this(mrnas, m_iHeight);
    }

    /**
     * Creates a new ConsensusDisplay object.
     *
     * @param cons   the target consensus
     * @param height the height of the panel
     */
    public mRNADisplay(mRNAs mrnas, int height) {
        m_iHeight = height;
        setBackground(Color.white);
        setOpaque(true);
        if (mrna == null) {
            return;
        }
        mrna = mrnas.getmRNA();
        m_mrnas = mrnas;


    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * set the parent panel
     *
     * @parm pane - the parent Primary Panel
     */

    public void setPrimaryPanel(PrimaryPanel pane) {
        m_dataPanel = pane;
    }

    /**
     * @return - the parent Primary Panel
     */

    public PrimaryPanel getPrimaryPanel() {
        return m_dataPanel;
    }


    /**
     * set the consensus to display
     *
     * @param cons the consensus
     */
    public void setMRNA(mRNAs mrnas) {
        mrna = mrnas.getmRNA();


        if (mrna == null) {
            UITools.showWarning("Unsupported/Corrupt file format.", this);
            return;
        }
        try {
            posits = new ArrayList();
            for (int i = 0; i < mrna.length; i++) {
                posits.add(mrna[i].pos);
            }
        } catch (Exception e) {
            UITools.showWarning("Unsupported/Corrupt file format.", this);
            return;
        }

        //  This action listener follows the mouse around and calculated the 
        //  position and number of RNA counts for that position
        this.addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            public void mouseMoved(MouseEvent arg0) {
                Point loc = arg0.getPoint();
                int x_loc = loc.x / m_fontWidth;
                int y_loc1 = 0;
                int y_loc2 = 0;
                if (posits.indexOf(x_loc) != -1) {
                    y_loc1 = mrna[posits.indexOf(x_loc)].counts;//*((int)mult);
                }
                if (posits.lastIndexOf(x_loc) != -1) {
                    y_loc2 = mrna[posits.lastIndexOf(x_loc)].counts;
                }
                ToolTipManager.sharedInstance().setInitialDelay(0);
                String newline = System.getProperty("line.separator");
                String toolTip = "";
                if (y_loc1 == y_loc2) {
                    toolTip += "Base Index: " + x_loc + "\nCounts: " + y_loc1;
                } else {
                    toolTip += "Base Index:  " + x_loc + "\nCounts: " + y_loc1 + "\nCounts: " + y_loc2;
                }

                getPrimaryPanel().m_rnaDisp.setToolTipText(UITools.formatToHTML(toolTip));

            }

        });


        repaint();
    }

    /**
     * set the strand to dispaly
     *
     * @param strand the strand
     */
    public void setDisplayStrand(StrandedFeature.Strand strand) {
        super.setDisplayStrand(strand);
        // m_consensus.setStrand(strand);

        repaint();
    }

    /**
     * set the sequence font
     *
     * @param f the font
     */
    public void setDisplayFont(Font f) {
        super.setDisplayFont(f);
        initText();
    }

    /**
     * set the height of the graph
     *
     * @param h the height
     */
    public void setInfoHeight(int h) {
        m_iHeight = h;
    }

    /**
     * get the height of this display
     *
     * @return the height
     */
    public int getHeight() {
        return m_fontHeight + m_iHeight;
    }

    /**
     * get the width of this display
     *
     * @return the width
     */
    public int getWidth() {
        if (mrna == null) {
            return 100;
        } else {
            return (mrna.length * m_fontWidth) + 80;
        }
    }

    /**
     * get the height of the headers/rows
     *
     * @return the height array
     */
    public int[] getHeaderHeights() {
        int[] ret = {getHeight()};

        return ret;
    }

    /**
     * Get the x position on the screen for a given sequence position
     *
     * @param seqPos
     * @return
     */
    public int sequenceToGraphics(int seqPos) {
        return seqPos * m_fontWidth;
    }

    /**
     * Get the sequence position for a given graphics x position
     *
     * @param grPos
     * @return
     */
    public int graphicsToSequence(int grPos) {
        return grPos / m_fontWidth;
    }

    /**
     * get hte header array of strings
     *
     * @return the header array
     */
    public String[] getHeaders() {
        String[] ret = m_mrnas.getNames();

        return ret;
    }

    /**
     * set the display area to be shown by this display
     *
     * @param start
     * @param stop
     */
    public void setDisplayArea(int start, int stop) {
        m_charStart = start;
        m_charStop = stop;
        repaint();
    }


    /**
     * render the display to a graphics object
     *
     * @param sg the graphics object
     */
    public void renderDisplay(Graphics sg) {
        if (mrna == null) {
            return;
        }

        Graphics2D g = (Graphics2D) sg;


        Rectangle r = sg.getClipBounds();
        int height = 3 * getPrimaryPanel().m_rnaScroll.getHeight();  // - getPrimaryPanel().r_panel.getDividerLocation();

        try {
            this.setPreferredSize(new Dimension(this.getWidth(), height));
        } catch (Exception e) {
            System.out.println("sizing error");
        }

        if (r == null) {
            r = new Rectangle(0, 0, getWidth(), getHeight());
        }


        if (m_fontWidth == 0) {
            m_fontWidth = 10;
        }

        int x1pos = (int) r.getX();
        int x2pos = (int) (r.getX() + r.getWidth());

        int charStart = (x1pos / m_fontWidth) - 10;
        int charStop = (x2pos / m_fontWidth) + 10;

        int end = Math.max(0, Math.min(mrna.length, charStop));
        charStart = Math.max(0, charStart);

        if ((end == 0) || (end < charStart)) {
            return;
        }

        charStop = end;

        renderValues(g, charStart, (int) charStop + charStart);
        g.translate(0, m_iHeight);

        if (getPrimaryPanel().m_rnaOvFrame != null && getPrimaryPanel().m_rnaOvFrame.isVisible()) {
            getPrimaryPanel().m_rnaOvFrame.setView(charStart, (int) charStop + charStart);
        }
    }

    /**
     * render the graph
     *
     * @param g     the graphics context
     * @param start where to start
     * @param stop  where to stop
     */
    protected void renderValues(Graphics2D g, int start, int stop) {

        final double mult = getPrimaryPanel().rna_tool.s.getValue();
        Rectangle rect = getPrimaryPanel().m_rnaScroll.getViewportBorderBounds();
        Color c = Color.red;

        g.setPaint(c);
        //  Determine the indices of the MRNA sequence data of interest for the
        //  plotting window
        int s_start = -1;
        int temp = start;
        stop = (int) start + (int) Math.round(rect.getWidth() / m_fontWidth);
        while (temp <= stop) {
            s_start = posits.indexOf(temp);
            if (s_start > -1) {
                break;
            }
            temp++;
        }


        if (s_start < 0) {
            s_start = 0;
        }

        //  Calculate the mrna expression information for the window
        try {

            for (int i = s_start; mrna[i].pos <= (11 + stop); i++) {

                rect.getHeight();

                int j = i + Math.max(0, m_charStart);

                double h1 = (double) Math.round(mrna[i].counts / mult);
                ;

                int x = mrna[i].pos * m_fontWidth;
                int w = m_fontWidth;
                double y = Math.round(0.5 * rect.getHeight());

                if (mrna[i].counts >= 0) {
                    y = Math.round(0.5 * rect.getHeight() - h1);
                } else {
                    y = 0.5 * rect.getHeight();
                }

                g.fillRect(x, (int) y, w, (int) Math.abs(h1));


            }
        } catch (NullPointerException e) {
            getPrimaryPanel().rna_visible(false);
        } catch (IndexOutOfBoundsException e) {
        }

        g.setPaint(Color.black);

        //  Draw the x-axis
        g.draw(new Line2D.Float(start * m_fontWidth, (float) (Math.round(0.5 * rect.getHeight())), m_fontWidth * (stop + start), (float) (0.5 * rect.getHeight()))); //Draw x-axis line


    }

    /**
     * render the sequence
     *
     * @param g     the grpahics context
     * @param start where to start
     * @param stop  where to stop
     */
    protected void renderSequence(Graphics2D g, int start, int stop) {
        g.setFont(m_dispFont);

        int mystart = Math.max(start, Math.max(0, m_charStart));
        int mystop = stop + Math.max(m_charStart, 0);

        String seq = "Sequence";

        g.setPaint(Color.black);
        g.drawString(seq, sequenceToGraphics(start), m_fontHeight);
    }

    /**
     * init fonts, etc
     */
    protected void initText() {
        FontMetrics fm = getFontMetrics(m_dispFont);
        m_fontHeight = fm.getAscent();
        m_fontWidth = fm.charWidth('?');
    }
}