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

import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.RangeLocation;

import java.awt.Color;

import javax.swing.*;


/**
 * This is an implementation of the FeatureAction interface that is specific to
 * COMMENT features
 *
 * @author Ryan Brodie, asyed
 */
public class CommentFeatureAction
        extends AbstractFeatureAction {
    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new CommentFeatureAction object.
     *
     * @param f The feature to act upon
     * @throws IllegalArgumentException
     */
    public CommentFeatureAction(StrandedFeature f)
            throws IllegalArgumentException {
        super(f);

        if (!f.getType()
                .equals(FeatureType.COMMENT)) {
            throw new IllegalArgumentException("Feature type must be " +
                    "FeatureType.EVENT");
        }

        String et = "";
        String name = "";
        try {
            et = f.getAnnotation()
                    .getProperty(AnnotationKeys.COMMENT_TEXT)
                    .toString();
            name = f.getAnnotation()
                    .getProperty(AnnotationKeys.NAME)
                    .toString();
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Feature must have annotation pair " +
                            "{COMMENT_TEXT, text string}, {NAME, name string}");
        }

        if (et == null || name == null) {
            throw new IllegalArgumentException(
                    "Feature must have annotation pair " +
                            "{COMMENT_TEXT, text string}, {NAME, name string}");
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

        if (clickCount == 2) {
            String ctext = m_feature.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT).toString();
            String cname = m_feature.getAnnotation().getProperty(AnnotationKeys.NAME).toString();
            CommentEditorPane pane = new CommentEditorPane(cname, ctext);
            pane.setCommentBackground((Color) m_feature.getAnnotation().getProperty(AnnotationKeys.BGCOLOR));
            pane.setCommentForeground((Color) m_feature.getAnnotation().getProperty(AnnotationKeys.FGCOLOR));

            int val = JOptionPane.showConfirmDialog(null, pane, "Modify Comment", JOptionPane.OK_CANCEL_OPTION);

            if (val == JOptionPane.OK_OPTION) {
                try {
                    m_feature.getAnnotation().setProperty(AnnotationKeys.COMMENT_TEXT, pane.getComment());
                    m_feature.getAnnotation().setProperty(AnnotationKeys.NAME, pane.getName());
                    m_feature.getAnnotation().setProperty(AnnotationKeys.BGCOLOR, pane.getCommentBackground());
                    m_feature.getAnnotation().setProperty(AnnotationKeys.FGCOLOR, pane.getCommentForeground());
                } catch (org.biojava.utils.ChangeVetoException cve) {
                    cve.printStackTrace();
                }
            }
        }

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
        return m_feature.getAnnotation()
                .getProperty(AnnotationKeys.COMMENT_TEXT)
                .toString();
    }
}
