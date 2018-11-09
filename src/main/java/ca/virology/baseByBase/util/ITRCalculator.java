package ca.virology.baseByBase.util;

import ca.virology.lib.util.common.SequenceUtility;

import java.util.ArrayList;
import java.util.List;

public class ITRCalculator {
    public static int[] findITRPartialTerminalError(String seq, final int MIN_ITR_LENGTH, final int ERROR_MARGIN) {

        int[] itr = new int[]{1, 1, 1, 1};
        //only test outer 25% of sequence
        String seq1 = seq.substring(0, seq.length() / 4).toUpperCase();
        String seq2 = SequenceUtility.make_complement(seq.substring((seq.length() * 3) / 4, seq.length()));
        int[] posStart = findPartialTerminalRepeatError(seq1, seq2, ERROR_MARGIN);
        int[] negStart = findPartialTerminalRepeatError(seq2, seq1, ERROR_MARGIN);

        if ((posStart[1] - posStart[0]) >= (negStart[1] - negStart[0]) && (posStart[1] - posStart[0]) > MIN_ITR_LENGTH) {
            //System.out.println("(1..."+posStart[0]+"), complement("+(seq.length()-(posStart[1]+posStart[0])+1)+"..."+(seq.length()-posStart[1])+")");

            itr[0] = posStart[0] + 1;
            itr[1] = posStart[1] + 1;
            itr[2] = seq.length() - posStart[3];
            itr[3] = seq.length() - posStart[2];

        } else if ((negStart[1] - negStart[0]) >= (posStart[1] - posStart[0]) && (negStart[1] - negStart[0]) > MIN_ITR_LENGTH) {
            //System.out.println("("+negStart[1]+"..."+(negStart[1]+negStart[0])+"), complement("+(seq.length()-negStart[0]+1)+"..."+seq.length()+")");

            itr[0] = negStart[2] + 1;
            itr[1] = negStart[3] + 1;
            itr[2] = seq.length() - negStart[1];
            itr[3] = seq.length() - negStart[0];

        }
        return itr;
    }

    private static int[] findPartialTerminalRepeatError(String seq1, String seq2, int errorMargin) {
        int bestStart = 0;
        int bestLength = 0;

        for (int i = 0; i < seq2.length(); i++) {
            //if it is no longer possible to find a longer subsequence exit the loop;
            if (seq2.length() - i < bestLength) {
                break;
            }
            int length = findRepeatLength(seq1, seq2.substring(i));

            if (length > bestLength) {
                bestLength = length;
                bestStart = i;
            }
        }
        int start1 = 0;
        int stop1 = bestLength - 1;
        int start2 = bestStart;
        int stop2 = bestStart + bestLength - 1;
        //Currently using endmargin = 10. This seems to work well, although could use some testing. 5 seems to be too low.
        int[] lengths = findRepeatLengthsError(seq1.substring(stop1), seq2.substring(stop2), errorMargin, 10);

        return new int[]{start1, stop1 + lengths[2], start2, stop2 + lengths[3]};
    }

    //Calculates the distance for which seq1[i] == seq2[i]
    public static int findRepeatLength(String seq1, String seq2) {
        int length = 0;
        for (int i = 0; i < Math.min(seq1.length(), seq2.length()); i++) {
            if (seq1.charAt(i) == seq2.charAt(i)) {
                length++;
            } else {
                break;
            }
        }
        return length;
    }

    /* Recursively calculates the distance for which seq1[i] == seq2[i],
     * allowing for up to depth errors (insertions, deletes, or substitutions)
     * Returns an array of size 4, specified as follows:
     * length[0] = the length of the match on seq1
     * length[1] = the length of the match on seq2
     * length[2] = the predicted actual match length on seq1, given no errors are allowed within
     *  endmargin characters of the last matching character
     * length[3] = the predicted actual match length on seq2, as for length[2].
     * Note that length[0] may not equal length[1] due to possible insertions or deletions.
     */
    public static int[] findRepeatLengthsError(String seq1, String seq2, int depth, int endmargin) {
        if (depth == 0) {
            return new int[]{0, 0, 0, 0};
        }
        if (seq1.length() <= 1 || seq2.length() <= 1) {
            return new int[]{0, 0, 0, 0};
        }


        int[] lengths, ins, sub, del;
        int length;

        length = findRepeatLength(seq1, seq2.substring(1));
        ins = findRepeatLengthsError(seq1.substring(length + 1), seq2.substring(length), depth - 1, endmargin);
        ins = calcRepeatLengthsArray(ins, length, 1, 0, endmargin);

        length = findRepeatLength(seq1.substring(1), seq2.substring(1));
        sub = findRepeatLengthsError(seq1.substring(length + 1), seq2.substring(length + 1), depth - 1, endmargin);
        sub = calcRepeatLengthsArray(sub, length, 1, 1, endmargin);

        length = findRepeatLength(seq1.substring(1), seq2);
        del = findRepeatLengthsError(seq1.substring(length), seq2.substring(length + 1), depth - 1, endmargin);
        del = calcRepeatLengthsArray(del, length, 0, 1, endmargin);

        if (ins[0] + ins[1] > sub[0] + sub[1] && ins[0] + ins[1] > del[0] + del[1]) {
            lengths = ins;
        } else if (sub[0] + sub[1] > ins[0] + ins[1] && sub[0] + sub[1] > del[0] + del[1]) {
            lengths = sub;
        } else if (del[0] + del[1] > sub[0] + sub[1] && del[0] + del[1] > ins[0] + ins[1]) {
            lengths = del;
        } else {
            lengths = sub;
        }

        return lengths;

    }

    private static int[] calcRepeatLengthsArray(int[] lengths, int deltaLength, int delta1, int delta2, int margin) {
        //if best position has not been set and delta is sufficiently large, set best position
        //(do not include current error)
        if (lengths[2] == 0 && lengths[3] == 0) {
            if (deltaLength > margin) {
                if (delta1 == 1 && delta2 == 1) {
                    lengths[2] = deltaLength;
                    lengths[3] = deltaLength;
                } else {
                    lengths[2] = deltaLength - delta1;
                    lengths[3] = deltaLength - delta2;
                }
            }
            //if best position has been set, update it
        } else {
            lengths[2] = lengths[2] + deltaLength + delta1;
            lengths[3] = lengths[3] + deltaLength + delta2;
        }

        //update current position
        lengths[0] = lengths[0] + deltaLength + delta1;
        lengths[1] = lengths[1] + deltaLength + delta2;

        return lengths;
    }


}
