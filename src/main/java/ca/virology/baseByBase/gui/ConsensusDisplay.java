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

import org.biojava.bio.seq.StrandedFeature;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.math.*;

import java.awt.event.MouseAdapter;


/**
 * this panel displays consensus data and a graph relating a set of  sequences
 * to each other
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class ConsensusDisplay extends AbstractSequencePanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected Consensus m_consensus;
    protected int m_iHeight = 30;
    protected int m_fontHeight;
    protected int m_fontWidth;
    protected int m_charStart = -1;
    protected int m_charStop = -1;

    ArrayList<RectInfo> rInfo = new ArrayList<>();

    PrimaryPanel m_dataPanel;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new ConsensusDisplay object.
     *
     * @param cons the target consensus
     */
    public ConsensusDisplay(Consensus cons) {
        this(cons, 30);
    }

    /**
     * Creates a new ConsensusDisplay object.
     *
     * @param cons   the target consensus
     * @param height the height of the panel
     */
    public ConsensusDisplay(Consensus cons, int height) {
        m_iHeight = height;
        m_consensus = cons;

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent ev) {
                int height = ev.getComponent().getHeight();
                setHeight(height);
                setSize(getWidth(), getHeight());
                repaint();
                revalidate();
            }
        });


        addMouseListener(new MouseListener() {
            @Override
            /*
            If a rectangle is clicked, print its identity percentage
             */
            public void mouseClicked(MouseEvent e) {

                int mx = e.getX();
                int my = e.getY();

                for(RectInfo info : rInfo){ // for each rectangle

                    if (mx > info.x && mx < info.x + info.w&& my > info.y && my < info.y + info.h) { // if the click is within a rectangle

                        Double toBeTruncated = new Double(info.val); // truncate the double
                        Double truncatedDouble = BigDecimal.valueOf(toBeTruncated)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue();

                       JOptionPane.showMessageDialog(getParent(),truncatedDouble + "% identity" );
                       break;
                    } else {}
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        setBackground(Color.white);
        setOpaque(true);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * set the consensus to display
     *
     * @param cons the consensus
     */
    public void setConsensus(Consensus cons) {
        m_consensus = cons;

        repaint();
    }

    /**
     * set the strand to dispaly
     *
     * @param strand the strand
     */
    public void setDisplayStrand(StrandedFeature.Strand strand) {
        super.setDisplayStrand(strand);
        m_consensus.setStrand(strand);

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

    //Set the heigh of the contents
    public void setHeight(int h) {
        m_iHeight = h - m_fontHeight;
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
        return (m_consensus.getLength() * m_fontWidth) + 80;
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


    public void setPrimaryPanel(PrimaryPanel pane) {
        m_dataPanel = pane;
    }

    public PrimaryPanel getPrimaryPanel() {
        return m_dataPanel;
    }

    /**
     * get hte header array of strings
     *
     * @return the header array
     */
    public String[] getHeaders() {
        String[] ret = {m_consensus.getName()};

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
        //System.out.println("Rendering display here");

        Graphics2D g = (Graphics2D) sg;
        //System.out.println("Rendering");
        Rectangle r = sg.getClipBounds();

        int x1pos = (int) r.getX();
        int x2pos = (int) (r.getX() + r.getWidth());

        int charStart = (x1pos / m_fontWidth) - 10;
        int charStop = (x2pos / m_fontWidth) + 10;

        int end = Math.max(0, Math.min(m_consensus.getLength(), charStop));
        charStart = Math.max(0, charStart);

        if ((end == 0) || (end < charStart)) {
            return;
        }

        charStop = end;

        renderValues(g, charStart, charStop);
        //g.translate(0, m_iHeight);
        renderSequence(g, charStart, charStop);
        //g.translate(0, -m_iHeight);

    }

    /**
     * render the graph
     *
     * @param g     the graphics context
     * @param start where to start
     * @param stop  where to stop
     */
    protected void renderValues(Graphics2D g, int start, int stop) {
        for (int i = start; i < stop; ++i) {
            int j = i + Math.max(0, m_charStart);

            double val = m_consensus.getValue(j);

            int h = (int) ((val / 100) * (m_iHeight));
            Color c = m_consensus.getColor(j);

            int x = sequenceToGraphics(i);
            int y = (m_iHeight) - h;
            int w = m_fontWidth;

            g.setPaint(c);
            g.fillRect(x, y, w, h);

            RectInfo r = new RectInfo(x,y,w,h,val);  // create an object storing each rectangle's info
            rInfo.add(r); // add it to an arraylist
            
        }
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

        String seq = m_consensus.getSequence(mystart, mystop);
        g.setPaint(Color.black);
        g.drawString(seq, sequenceToGraphics(start), m_iHeight + m_fontHeight);
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