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

import ca.virology.lib.io.sequenceData.EditableSequence;


public class KimuraSimilarityModel implements SequenceSimilarityModel {

    public KimuraSimilarityModel() {

    }

    public String getModelName() {
        return ("Kimura 2-Parameter Distance");
    }

    public int getSequenceType() {
        return (EditableSequence.DNA_SEQUENCE);
    }

    /**
     * Calculates the Kimura 2-parameter distance between two sequences.
     *
     * @param queryWindow sequence against which other sequences are being compared
     * @param compWindow  sequence to compare to query window
     * @returns distance of the sequences as a percentage (may be NaN if there are too many transversional gaps)
     */
    public double getSimilarity(String queryWindow, String compWindow) {
        double sim;

        int minLen = queryWindow.length();
        int maxLen = compWindow.length();
        if (compWindow.length() < minLen) {
            minLen = compWindow.length();
            maxLen = queryWindow.length();
        }

        // count transitional differences
        double p = 0;
        // count transversion differences
        double q = 0;

        for (int i = 0; i < minLen; i++) {
            char qChar = queryWindow.charAt(i);
            char cChar = compWindow.charAt(i);
            switch (qChar) {
                case 'A':
                    switch (cChar) {
                        case 'G':
                            p++;
                            break;
                        case 'T':
                        case 'C':
                            q++;
                            break;
                        default:
                            // do nothing
                            break;
                    }
                    break;
                case 'G':
                    switch (cChar) {
                        case 'A':
                            p++;
                            break;
                        case 'T':
                        case 'C':
                            q++;
                            break;
                        default:
                            // do nothing
                            break;
                    }
                    break;
                case 'C':
                    switch (cChar) {
                        case 'T':
                            p++;
                            break;
                        case 'A':
                        case 'G':
                            q++;
                            break;
                        default:
                            // do nothing
                            break;
                    }
                    break;
                case 'T':
                    switch (cChar) {
                        case 'C':
                            p++;
                            break;
                        case 'A':
                        case 'G':
                            q++;
                            break;
                        default:
                            // do nothing
                            break;
                    }
                    break;
                default:
                    // do nothing
                    break;
            }
        }

        p = p / maxLen;
        q = q / maxLen;

        double w1 = (1 - ((2 * p) - q));
        double w2 = (1 - (2 * q));

        sim = (-1 * (0.5 * Math.log(w1))) + (-1 * (0.25 * Math.log(w2)));

        return (sim);
    }


}
