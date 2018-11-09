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

import ca.virology.lib.io.sequenceData.EditableSequence;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.RangeLocation;


/**
 * This is a simple implementation of the FeatureAction
 *
 * @author Ryan Brodie
 */
public class AbstractFeatureAction
        implements FeatureAction {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected Feature m_feature;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new AbstractFeatureAction object.
     */
    public AbstractFeatureAction() {
    }

    /**
     * Creates a new AbstractFeatureAction object.
     *
     * @param f the feature for this action
     */
    protected AbstractFeatureAction(Feature f) {
        m_feature = f;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * fires a click action with relation to the specified sequence
     *
     * @param clicks   the number of clicks
     * @param position the position on the alignment of the click
     * @param seq      the sequence where the click occurred
     * @return A location indicating a possible selection to be made by this
     * click
     */
    public Location doClick(
            int clicks,
            int position,
            EditableSequence seq) {
        int clickCount = clicks;
        System.out.println("Clicked " + clickCount);

        Location l = m_feature.getLocation();

        return new RangeLocation(
                seq.getRelativePosition(l.getMin()),
                seq.getRelativePosition(l.getMax()));
    }

    /**
     * get the tooltip text for this featureaction
     *
     * @return a tooltip string
     */
    public String getTooltipText() {
        return "-";
    }

    /**
     * get the feature targeted by this action
     *
     * @return the feature
     */
    public Feature getFeature() {
        return m_feature;
    }
}