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

import java.awt.Font;
import java.awt.Graphics;

import java.beans.*;


/**
 * This is the generic interface for all sequence displays
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public interface SequenceDisplay {
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final String DISPLAY_PROPERTY = "DISPLAY_PROPERTY";
    public static final String FONT_PROPERTY = "FONT_PROPERTY";
    public static final String STRAND_PROPERTY = "STRAND_PROPERTY";
    public static final int EDIT_MODE = 20001;
    public static final int SELECT_MODE = 20002;
    public static final int GLUE_MODE = 20003;

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * add a property change listener
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * add a listener to a specific property
     *
     * @param propertyName the property
     * @param listener     the listener to add
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * remove a listener
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * remove a listener from a specific property
     *
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * set the strand of the sequence to display in this display
     *
     * @param strand the new strand
     */
    public void setDisplayStrand(StrandedFeature.Strand strand);

    /**
     * get the strand currently displayed here
     *
     * @return the strand
     */
    public StrandedFeature.Strand getDisplayStrand();

    /**
     * get the headers for this display
     *
     * @return an array of header names
     */
    String[] getHeaders();

    /**
     * get the height of this display
     *
     * @return the height
     */
    public int getHeight();

    /**
     * get the width of this display
     *
     * @return the width
     */
    public int getWidth();

    /**
     * get the heights of each header (and therefor row) in this display
     *
     * @return the heights array
     */
    public int[] getHeaderHeights();

    /**
     * set the main display font for this display
     *
     * @param f the font
     */
    public void setDisplayFont(Font f);

    /**
     * get the main display font for this display
     *
     * @return the font
     */
    public Font getDisplayFont();

    /**
     * convert a position from sequence terms to screen graphics terms
     *
     * @param seqPos the position
     * @return its screen x coord equivalent
     */
    public int sequenceToGraphics(int seqPos);

    /**
     * convert a screen x coordinate to sequence positions
     *
     * @param grPos the x coordinate
     * @return its sequence position equivalent
     */
    public int graphicsToSequence(int grPos);

    /**
     * render the display to a graphics object
     *
     * @param g the graphics context to display
     */
    public void renderDisplay(Graphics g);
}