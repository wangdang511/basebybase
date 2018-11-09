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

import ca.virology.lib.io.sequenceData.*;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.Location;


/**
 * This interface is used to represent the actions that can be taken w.r.t. a
 * feature on the screen.
 *
 * @author Ryan Brodie
 */
public interface FeatureAction {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * This gets the feature represented by this action
     *
     * @return The feature for this action
     */
    public Feature getFeature();

    /**
     * This invokes an action based on the click count, and appropriate for
     * this feature
     *
     * @param clicks the number of clicks counted
     * @return a possible selection location
     */
    public Location doClick(int clicks, int position, EditableSequence seq);

    /**
     * This gets the tooltip text that is appropriate for the given action
     *
     * @return The appropriate tooltip text
     */
    public String getTooltipText();
}