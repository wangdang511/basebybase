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

import org.biojava.bio.symbol.Location;

import java.beans.*;


/**
 * this interface contains definitions for sequence selectable objects
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public interface Selectable {
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final String SELECTION_PROPERTY = "SELECTION_PROPERTY";

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the selection in terms of the alignment
     *
     * @return a selection location
     */
    public Location getRelativeSelection();

    /**
     * Get the selection of this object in terms of the sequence
     *
     * @return the selection location
     */
    public Location getAbsoluteSelection();

    /**
     * set the selectoin in terms of the alignemnt
     *
     * @param x1 the first position
     * @param x2 the last position
     */
    public void setRelativeSelection(int x1, int x2);

    /**
     * set the selection in terms of the underlying sequence scale
     *
     * @param start the first position ( >0 )
     * @param stop  the last position
     */
    public void setAbsoluteSelection(int start, int stop);

    /**
     * clear the selection to nothing
     */
    public void clearSelection();

    /**
     * add a property change listener
     *
     * @param listener the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * add a listener for a particular property
     *
     * @param propertyName the property
     * @param listener     the listener
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * remove a property change listener
     *
     * @param listener the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * remove a property change listener for a particular property
     *
     * @param propertyName the property
     * @param listener     the listener
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
}