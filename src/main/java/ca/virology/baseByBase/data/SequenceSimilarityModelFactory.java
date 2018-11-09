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

/**
 * This frame displays a graph of sequence similarity achieved by using a progressive scanning window,
 * where points on the graph represent the similarity of each sequence to a 'query' sequence.
 *
 * @author Alex Smith
 * @version $Revision: 1.1.1.1 $
 */
public class SequenceSimilarityModelFactory {
    protected static String[] m_modelNames = {
            "Kimura 2-Parameter Distance", "PAM250", "BLOSUM62"
    };

    public static String[] getModelNames() {
        return (m_modelNames);
    }

    public static SequenceSimilarityModel createSequenceSimilarityModel(String name) {
        if (name.equals(m_modelNames[0])) {
            return new KimuraSimilarityModel();
        }
        return null;
    }
}
