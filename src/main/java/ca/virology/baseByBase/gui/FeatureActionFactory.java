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
import org.biojava.bio.seq.StrandedFeature;

import java.util.*;


/**
 * This class is used to get appropriate action classes for a given feature
 * generically.  This will determine which class is best to use and return an
 * instance of that class
 *
 * @author Ryan Brodie
 */
public class FeatureActionFactory {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static final int MAX_CACHE = 100;

    /**
     * static instance object
     */
    protected static FeatureActionFactory c_instance = null;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected Map m_fMap = new HashMap();
    protected List m_fList = new ArrayList();

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new FeatureActionFactory object.
     */
    protected FeatureActionFactory() {
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the singleton instance of this factory
     *
     * @return the instance
     */
    public static FeatureActionFactory getInstance() {
        if (c_instance == null) {
            c_instance = new FeatureActionFactory();
        }

        return c_instance;
    }

    /**
     * create an appropriate action for a given feature
     *
     * @param feature the feature to act upon
     * @return a feature action appropriate to this feature
     */
    public FeatureAction createFeatureAction(final StrandedFeature feature) {
        FeatureAction act = null;

        if (feature == null) {
            return null;
        }

        act = getAction(feature);

        if (act != null) {
            return act;
        }

        if (feature.getType()
                .equals(FeatureType.GENE)) {
            act = new GeneFeatureAction(feature);
        } else if (feature.getType()
                .equals(FeatureType.EVENT)) {
            // comment
            String et =
                    feature.getAnnotation()
                            .getProperty(AnnotationKeys.EVENT_TYPE)
                            .toString();
        } else if (feature.getType()
                .equals(FeatureType.SEARCH_RESULTS)) {
            act = new SearchFeatureAction(feature);
        } else if (feature.getType()
                .equals(FeatureType.COMMENT)) {
            act = new CommentFeatureAction(feature);
            //added for primer
        } else if (feature.getType()
                .equals(FeatureType.PRIMER)) {
            act = new PrimerFeatureAction(feature);
        } else if (feature.getType()
                .equals(FeatureType.DIFFERENCE_LIST)) {
            act = new DifferenceFeatureAction(feature);
        } else {
            act = new AbstractFeatureAction() {
            };
        }

        addFeatureMapping(feature, act);

        return act;
    }

    /**
     * add a feature and action to the map, this pools actions so that the
     * factory doesn't create an action for the same feature more than it has
     * to
     *
     * @param f the feature to store
     * @param a the action associated with the feature
     */
    protected void addFeatureMapping(
            Feature f,
            FeatureAction a) {
        if (m_fMap.containsKey(f)) { //shuffle to end

            int i = m_fList.indexOf(f);
            m_fList.remove(i);
            m_fList.add(f);
        } else if (m_fList.size() >= MAX_CACHE) {
            Object key = m_fList.remove(0);
            m_fMap.remove(key);
        } else {
            m_fList.add(f);
            m_fMap.put(f, a);
        }
    }

    /**
     * get the action from the pool for the given feature
     *
     * @param f the feature to retreive
     * @return the feature action or null if the feature has no precreated
     * associated action
     */
    protected FeatureAction getAction(Feature f) {
        if (!m_fMap.containsKey(f)) {
            return null;
        } else {
            return (FeatureAction) m_fMap.get(f);
        }
    }
}