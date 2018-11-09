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

import org.biojava.bio.seq.StrandedFeature;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;


/**
 * this is a default implementation of SequenceDisplay interface and provides
 * the functionality used by all sequence panels.
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public abstract class AbstractSequencePanel
        extends JPanel
        implements SequenceDisplay {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected Font m_dispFont = new Font(Font.MONOSPACED, Font.PLAIN, 10);
    protected PropertyChangeSupport m_propSupport = new PropertyChangeSupport(this);
    protected StrandedFeature.Strand m_dispStrand = StrandedFeature.POSITIVE;

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * add a property change listener
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        m_propSupport.addPropertyChangeListener(listener);
    }

    /**
     * add a listener to a specific property
     *
     * @param propertyName the property
     * @param listener     the listener to add
     */
    public void addPropertyChangeListener(
            String propertyName,
            PropertyChangeListener listener) {
        m_propSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * remove a listener
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        m_propSupport.removePropertyChangeListener(listener);
    }

    /**
     * remove a listener from a specific property
     *
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListener(
            String propertyName,
            PropertyChangeListener listener) {
        m_propSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Sets the strand to be displayed by the panel
     *
     * @param strand The new strand to display
     */
    public void setDisplayStrand(StrandedFeature.Strand strand) {
        StrandedFeature.Strand old = m_dispStrand;
        m_dispStrand = strand;

        m_propSupport.firePropertyChange(STRAND_PROPERTY, old, strand);
    }

    /**
     * Gets the strand currently being displayed by this panel
     *
     * @return The strand currently under display
     */
    public StrandedFeature.Strand getDisplayStrand() {
        return m_dispStrand;
    }

    /**
     * Set the font used to display sequence data
     *
     * @param f The font to display
     */
    public void setDisplayFont(Font f) {
        Font old = m_dispFont;
        m_dispFont = f;

        m_propSupport.firePropertyChange(STRAND_PROPERTY, old, f);
    }

    /**
     * Get the font used to display sequence data
     *
     * @return the current display font
     */
    public Font getDisplayFont() {
        return m_dispFont;
    }

    /**
     * paint the component to a graphics context
     *
     * @param g the graphics context
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderDisplay(g);
    }

    /**
     * get the preferrred size of this component
     *
     * @return the preferred size
     */
    public Dimension getPreferredSize() {
        return new Dimension(
                getWidth(),
                getHeight());
    }

    /**
     * get the maximum size of this component
     *
     * @return the maximum size
     */
    public Dimension getMaximumSize() {
        return new Dimension(
                getWidth(),
                getHeight());
    }

    /**
     * get the minimum size of this component
     *
     * @return the minimum size
     */
    public Dimension getMinimumSize() {
        return new Dimension(
                getWidth(),
                getHeight());
    }

    /**
     * Gets the height of this widget
     *
     * @return the height of this widget
     */
    public abstract int getHeight();

    /**
     * get the width of this panel
     *
     * @return the width
     */
    public abstract int getWidth();

    /**
     * Get the heights of the headers currently displayed.  Note that
     * getHeaders().length == getHeaderHeights().length.
     *
     * @return an int array of ints representing the height of each header in
     * turn from top to bottom
     */
    public abstract int[] getHeaderHeights();

    /**
     * Get the x position on the screen for a given sequence position
     *
     * @param seqPos the sequence position to convert
     * @return the screen x position representing the seq position
     */
    public abstract int sequenceToGraphics(int seqPos);

    /**
     * Get the sequence position for a given graphics x position
     *
     * @param grPos the screen x position to convert
     * @return the position in sequence terms of that x
     */
    public abstract int graphicsToSequence(int grPos);

    /**
     * Get the headers for the currently displayed rows
     *
     * @return a String array representing the headers for the currently
     * displayed channels.
     */
    public abstract String[] getHeaders();

    /**
     * render the display to a graphics component
     *
     * @param sg the graphics component to render to
     */
    public abstract void renderDisplay(Graphics g);
}
