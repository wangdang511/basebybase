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

import ca.virology.lib.util.gui.UITools;

import java.awt.*;

import javax.swing.*;

/**
 * This panel displays the headers for the editor This shares a 'has a'
 * relationship with <CODE>EditPanel</CODE>
 *
 * @author Ryan Brodie
 */
public class HeaderPanel extends JPanel {
    // ~ Static fields/initializers /////////////////////////////////////////////

    /**
     * maximum 'min' width in characters of this component
     */
    protected static final int MAX_WIDTH = 20;

    // ~ Instance fields ////////////////////////////////////////////////////////

    protected SequenceDisplay m_viewer;
    protected Font m_dispFont = new Font(Font.MONOSPACED, Font.PLAIN, 10);
    protected int m_fontHeight;
    protected int m_fontWidth;

    // ~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Construct a header panel to display the given viewer
     *
     * @param viewer the target of this header panel
     */
    public HeaderPanel(SequenceDisplay viewer) {
        m_viewer = viewer;

        m_viewer.addPropertyChangeListener(SequenceDisplay.DISPLAY_PROPERTY, new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                try {
                    UITools.invoke(new Runnable() {
                        public void run() {
                            refreshBounds();
                            revalidate();
                            repaint();
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        setBackground(Color.white);
        setOpaque(true);

        setDisplayFont(m_dispFont);
    }

    // ~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the font displayed by this panel
     *
     * @return the font
     */
    public Font getDisplayFont() {
        return m_dispFont;
    }

    /**
     * Set the font that will be used to display headers
     *
     * @param f The new font to display
     */
    public void setDisplayFont(Font f) {
        m_dispFont = f;

        initText();
        refreshBounds();
        repaint();
    }

    /**
     * paint this component
     *
     * @param g the graphics context on which to paint
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D sg = (Graphics2D) g;

        sg.setFont(m_dispFont);
        sg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        int[] height = m_viewer.getHeaderHeights();
        String[] header = m_viewer.getHeaders();

        for (int i = 0; i < header.length && i < height.length; i++) {
            if (height[i] == 0) {
                continue;
            }

            sg.setPaint(getForeground());
            sg.drawString(header[i], 0, m_fontHeight);
            sg.translate(0, height[i]);
        }

        sg.drawLine(0, -1, 200, -1);
        refreshBounds();
    }

    /**
     * init fonts, etc
     */
    protected void initText() {
        FontMetrics fm = getFontMetrics(m_dispFont);
        m_fontHeight = fm.getAscent();
        m_fontWidth = fm.charWidth('?');
    }

    /**
     * make sure min/max sizes match data
     */
    protected void refreshBounds() {
        int[] height = m_viewer.getHeaderHeights();

        int totalh = 0;

        for (int i = 0; i < height.length; ++i) {
            totalh += height[i];
        }

        Dimension max = new Dimension(150, totalh);
        setPreferredSize(max);
    }
}
