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
import ca.virology.lib.io.sequenceData.FeatureType;
import ca.virology.lib.io.sequenceData.AnnotationKeys;
import ca.virology.lib.io.sequenceData.DifferenceType;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.PointLocation;
import org.biojava.bio.symbol.RangeLocation;


/**
 * This class is the action taken upon difference lists.  This reacts by
 * returning appropriate selections for clicks and reacting properly to double
 * clicks.
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class DifferenceFeatureAction
        extends AbstractFeatureAction {
    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new DifferenceFeatureAction object.
     *
     * @param f the feature for this action
     * @throws IllegalArgumentException if the feature is of an incorrect type
     */
    public DifferenceFeatureAction(Feature f) {
        super(f);

        if (!f.getType().equals(FeatureType.DIFFERENCE_LIST)) {
            throw new IllegalArgumentException("Feature type must be FeatureType.DIFFERENCE_LIST");
        }
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
        Annotation ann = m_feature.getAnnotation();
        int[] diffs = (int[]) ann.getProperty(AnnotationKeys.DIFF_ARRAY);

        int start = -1;
        int stop = -1;

        for (int i = position + 1; i < diffs.length; ++i) {
            if (diffs[i] != diffs[position]) {
                stop = i - 1;

                break;
            }
        }

        if (stop == -1) {
            stop = seq.length();
        }

        for (int i = position - 1; i >= 0; --i) {
            if (diffs[i] != diffs[position]) {
                start = i + 1;

                break;
            }
        }

        if (start == -1) {
            start = 0;
        }

        if (diffs[position] == DifferenceType.I_NONE) {
            return new PointLocation(position);
        } else {
            return new RangeLocation(start, stop);
        }
    }

    /**
     * get the tooltip text for this featureaction
     *
     * @return a tooltip string
     */
    public String getTooltipText() {
        return "Differences";
    }
}
