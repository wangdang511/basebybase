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
package ca.virology.baseByBase.data;

import org.biojava.bio.seq.StrandedFeature;

import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.io.sequenceData.EditableSequence;


/**
 * This is a simple implementation of the EditableSequence interface and
 * provides implementation of all required methods.
 *
 * @author Ryan Brodie
 */
public class DiffEditFeaturedSequence
extends FeaturedSequence
{
    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Construct a <CODE>DiffEditFeaturedSequence</CODE> with the given ID, name
     * and underlying sequence string
     *
     * @param id       the id of the new sequence
     * @param name     the name of the new sequence
     * @param sequence The string representation of the new sequence
     */
    public DiffEditFeaturedSequence(
            int id,
            String name,
			String sequence)
	{
        super(id, name, sequence);
    }

    public DiffEditFeaturedSequence(FeaturedSequence featuredSequence) {
        this(featuredSequence.getId(), featuredSequence.getName(), featuredSequence.toString());

        java.util.Iterator i = featuredSequence.features();
        while (i.hasNext()) {
            StrandedFeature f = (StrandedFeature) i.next();
            StrandedFeature.Template templ =
                    (StrandedFeature.Template) f.makeTemplate();
            System.out.println("Creating Feature:" + f.getType());
            try {
                createFeature(templ);
            } catch (Exception ex) {
            }
        }
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public void removeGap(
            int pos1,
			int pos2)
	{

        if (pos1 <= pos2) {
            return;
        }

        for (int i = pos2; i < pos1; ++i) {
            if (charAt(i) == '-') {
                silentDelete(i);

                ca.virology.baseByBase.gui.UndoHandler.getInstance().postEdit(new EditableSequence.CharEdit(this, EditableSequence.CharEdit.DELETE, i, '-'));

                --pos1;
                --i;


            }
        }

        fireSequenceChangeEvent();
    }

    /**
     * Remove all gaps from pos1 to pos2
     *
     * @param pos1 the start position
     * @param pos2 the stop position
     */
    public void removeGaps(
            int pos1,
			int pos2)
	{
        if (pos1 > pos2) {
            return;
        }

        for (int i = pos1; i <= pos2; ++i) {
            if (charAt(i) == '-') {
                silentDelete(i);

                ca.virology.baseByBase.gui.UndoHandler.getInstance().postEdit(new EditableSequence.CharEdit(this, EditableSequence.CharEdit.DELETE, i, '-'));

                --pos2;
                --i;


            }
        }

        fireSequenceChangeEvent();
    }

    /**
     * Remove all non-gaps from pos1 to pos2
     *
     * @param pos1 the start position
     * @param pos2 the stop position
     */
    public void removeNonGaps(
            int pos1,
			int pos2)
	{
        if (pos1 > pos2) {
            return;
        }

        int start = -1;
        int end = -1;
        for (int i = pos1; i <= pos2; ++i) {
            char c = charAt(i);
            if (c != '-') {
                if (start == -1) {
                    start = i;
                    end = i;
                } else {
                    end = i;
                }

                ca.virology.baseByBase.gui.UndoHandler.getInstance().postEdit(new EditableSequence.CharEdit(this, EditableSequence.CharEdit.DELETE, i, c));
            } else if (start != -1 && end != -1) {
                silentDelete(start, end + 1);
                i -= end - start + 2;
                pos2 -= end - start + 1;
                start = -1;
                end = -1;
            }
        }
        if (start != -1) {
            silentDelete(start, end + 1);
        }

        //fireSequenceChangeEvent();
    }

    /**
     * ensure there are gaps from pos1 to pos2
     *
     * @param pos1 the start position
     * @param pos2 the stop position
     */
    public void assertGaps(
            int pos1,
			int pos2)
	{
        if (pos1 >= pos2) {
            return;
        }

        for (int i = pos1; i < pos2; ++i) {
            //if (charAt(i) != '-') {
            for (int j = i; j < pos2; ++j) {
                silentInsert(i, '-');

                ca.virology.baseByBase.gui.UndoHandler.getInstance().postEdit(new EditableSequence.CharEdit(this, EditableSequence.CharEdit.INSERT, i, '-'));
            }

            break;
            //}
        }

        fireSequenceChangeEvent();
    }
}
