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
import ca.virology.lib.io.sequenceData.AnnotationKeys;
import ca.virology.lib.io.sequenceData.FeatureType;
import ca.virology.lib.search.SearchHit;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.PointLocation;
import org.biojava.bio.symbol.RangeLocation;


/**
 * This handles actions done on search features in BBB display
 *
 * @author Ryan Brodie
 * @version $Revision: 1.1.1.1 $
 */
public class SearchFeatureAction
        extends AbstractFeatureAction {
    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new SearchFeatureAction object.
     *
     * @param f The target feature
     * @throws IllegalArgumentException if the feature is not a search feature
     */
    public SearchFeatureAction(Feature f) {
        super(f);

        if (!f.getType()
                .equals(FeatureType.SEARCH_RESULTS)) {
            throw new IllegalArgumentException("Feature type must be " +
                    "FeatureType.SEARCH_RESULTS");
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
        int apos = seq.getAbsolutePosition(position);
        Annotation ann = m_feature.getAnnotation();
        SearchHit[] locs =
                (SearchHit[]) ann.getProperty(AnnotationKeys.SEARCH_RESULTS);

        for (int i = 0; i < locs.length; ++i) {
            if ((locs[i].getStart() <= apos) && (locs[i].getStop() >= apos)) {
                return new RangeLocation(
                        seq.getRelativePosition(locs[i].getStart() + 1),
                        seq.getRelativePosition(locs[i].getStop() + 1) - 1);
            }
        }

        return new PointLocation(position);
    }

    /**
     * get the tooltip text for this featureaction
     *
     * @return a tooltip string
     */
    public String getTooltipText() {
        return (String) m_feature.getAnnotation()
                .getProperty(AnnotationKeys.SEARCH_TERM);
    }
}
