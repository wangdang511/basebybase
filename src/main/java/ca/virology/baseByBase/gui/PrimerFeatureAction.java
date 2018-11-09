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
import org.biojava.bio.symbol.LocationTools;
import org.biojava.bio.symbol.RangeLocation;

import java.awt.Color;

import javax.swing.*;


/**
 * This is an implementation of the FeatureAction interface that is specific to
 * PRIMER features
 *
 * @author Daniel Horspool
 */
public class PrimerFeatureAction
        extends AbstractFeatureAction {
    //~ Constructors ///////////////////////////////////////////////////////////
    protected PrimaryPanel m_dataPanel;

    /**
     * Creates a new PrimerFeatureAction object.
     *
     * @param f The feature to act upon
     * @throws IllegalArgumentException
     */
    public PrimerFeatureAction(StrandedFeature f)
            throws IllegalArgumentException {
        super(f);

        if (!f.getType()
                .equals(FeatureType.PRIMER)) {
            throw new IllegalArgumentException("Feature type must be " +
                    "FeatureType.EVENT");
        }

        String et = "";

        try {
            et = f.getAnnotation()
                    .getProperty(AnnotationKeys.NAME)
                    .toString();
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Feature must have annotation pair " +
                            "{NAME, text string}");
        }

        if (et == null) {
            throw new IllegalArgumentException(
                    "Feature must have annotation pair " +
                            "{NAME, text string}");
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
     * @author Neil Hillen
     */
    public Location doClick(
            int clicks,
            int position,
            EditableSequence seq) {
        int clickCount = clicks;

        if (clickCount == 2) {
            String name = m_feature.getAnnotation().getProperty(AnnotationKeys.NAME).toString();
            String segseq = m_feature.getAnnotation().getProperty(AnnotationKeys.PRIMER_SEQ).toString();

            String melt;
            /*if (AnnotationKeys.PRIMER_MELTINGTEMP == ""){
				melt = PrimaryPanel.getPrimerTemp(AnnotationKeys.PRIMER_SEQ.toString())+"";
			}else{*/
            melt = m_feature.getAnnotation().getProperty(AnnotationKeys.PRIMER_MELTINGTEMP).toString();
            //}

            String fridge = m_feature.getAnnotation().getProperty(AnnotationKeys.PRIMER_FRIDGE).toString();
            String comment = m_feature.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT).toString();

            PrimerEditorPane pane = new PrimerEditorPane(name, segseq, melt, fridge, comment);

            int val = JOptionPane.showConfirmDialog(null, pane, "Modify primer Details", JOptionPane.OK_CANCEL_OPTION);

            if (val == JOptionPane.OK_OPTION) {
                try {
                    m_feature.getAnnotation().setProperty(AnnotationKeys.NAME, pane.getName());
                    m_feature.getAnnotation().setProperty(AnnotationKeys.PRIMER_SEQ, pane.getSeqmentSequence());
                    m_feature.getAnnotation().setProperty(AnnotationKeys.PRIMER_MELTINGTEMP, pane.getMeltingTemp());
                    m_feature.getAnnotation().setProperty(AnnotationKeys.PRIMER_FRIDGE, pane.getFridgeLocation());
                    m_feature.getAnnotation().setProperty(AnnotationKeys.COMMENT_TEXT, pane.getComment());

                    //changes the length of the primer feature if the length of the primer sequence changes during editing (remains left aligned)
                    Location temploc = m_feature.getLocation();
                    Location setloc = LocationTools.makeLocation(temploc.getMin(), (temploc.getMin()) + (pane.getSeqmentSequence().length()) - 1);
                    m_feature.setLocation(setloc);


                    //recalculate the primer temp
                    //String recalculated = PrimaryPanel.getPrimerTemp(pane.getMeltingTemp())+"";
                    //m_feature.getAnnotation().setProperty(AnnotationKeys.PRIMER_MELTINGTEMP, recalculated);

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
     * @author Neil Hillen
     */
    public String getTooltipText() {
        return "Primer: " + m_feature.getAnnotation().getProperty(AnnotationKeys.NAME).toString()
                + "\nSequence: 5`- " + m_feature.getAnnotation().getProperty(AnnotationKeys.PRIMER_SEQ).toString() + " -3`"
                + "\nMelting temp: " + m_feature.getAnnotation().getProperty(AnnotationKeys.PRIMER_MELTINGTEMP).toString()
                + "\nLocation: " + m_feature.getAnnotation().getProperty(AnnotationKeys.PRIMER_FRIDGE).toString()
                + "\nComments: " + m_feature.getAnnotation().getProperty(AnnotationKeys.COMMENT_TEXT).toString();
    }
}
