package ca.virology.baseByBase.gui.CodeHop;

/**
 * CLASS: DNACodonProbability
 * <p>
 * Holds the output result of the main codehop process.
 * For a codon letter, there are DNAs which match to it, each with different probablitity.
 * For each codon (eg CBA) there is DNA match of the for CGT with a % probablitity.
 */


import java.util.Arrays;

public class DNACodonProbability {
    public double[] aProb = new double[3];
    public double[] cProb = new double[3];
    public double[] tProb = new double[3];
    public double[] gProb = new double[3];
    public String triplet = "";
    public double tripletProbability;
    double[] nucProbs = new double[4];
    char[][] nucs = new char[3][4];
    double[][] probs = new double[3][4];


    public DNACodonProbability(double a[], double g[], double c[], double t[], int[] clampPositionScores, int col) {
        for (int i = 0; i < 3; i++) {
            aProb[i] = a[i];
            cProb[i] = c[i];
            gProb[i] = g[i];
            tProb[i] = t[i];
        }

        // for each codon position
        for (int i = 0; i < 3; i++) {
            nucProbs[0] = a[i];
            nucProbs[1] = c[i];
            nucProbs[2] = g[i];
            nucProbs[3] = t[i];

            Arrays.sort(nucProbs);


            // reverse array so it is in descending order
            for (int k = 0; k < nucProbs.length / 2; k++) {
                double temp = nucProbs[k];
                nucProbs[k] = nucProbs[nucProbs.length - k - 1];
                nucProbs[nucProbs.length - k - 1] = temp;
            }

            clampPositionScores[col * 3 + i] = (int) (nucProbs[0] * 100);

            boolean[] used = new boolean[4];

            for (int k = 0; k < nucProbs.length; k++) {
                if (nucProbs[k] == a[i] && !used[0]) {
                    nucs[i][k] = 'A';
                    probs[i][k] = a[i];
                    used[0] = true;
                } else if (nucProbs[k] == c[i] && !used[1]) {
                    nucs[i][k] = 'C';
                    probs[i][k] = c[i];
                    used[1] = true;
                } else if (nucProbs[k] == g[i] && !used[2]) {
                    nucs[i][k] = 'G';
                    probs[i][k] = g[i];
                    used[2] = true;
                } else if (nucProbs[k] == t[i] && !used[3]) {
                    nucs[i][k] = 'T';
                    probs[i][k] = t[i];
                    used[3] = true;
                }
            }
        }

        tripletProbability = probs[0][0] * probs[1][0] * probs[2][0];
        triplet = Character.toString(nucs[0][0]) + nucs[1][0] + nucs[2][0];
    }


    public void setNextBest(int pos) {
        int index = 0;


        // find the index of the next best nucleotide
        // array is sorted in so it is in the position after the present nucleotide
        while (nucs[pos][index] != triplet.charAt(pos)) {
            index++;
        }
        index++;


        // remove old nucleotide probability from tripletProbability
        switch (triplet.charAt(pos)) {
            case 'A':
                tripletProbability /= aProb[pos];
                break;

            case 'C':
                tripletProbability /= cProb[pos];
                break;

            case 'G':
                tripletProbability /= gProb[pos];
                break;

            case 'T':
                tripletProbability /= tProb[pos];
        }


        // change the triplet string to include the next best nucleotide found
        switch (pos) {
            case 0:
                triplet = nucs[0][index] + triplet.substring(1);
                break;

            case 1:
                triplet = Character.toString(triplet.charAt(0)) + nucs[1][index] + triplet.charAt(2);
                break;

            case 2:
                triplet = triplet.substring(0, 2) + nucs[2][index];
        }


        // include new nucleotide probability in tripletProbability
        switch (triplet.charAt(pos)) {
            case 'A':
                tripletProbability *= aProb[pos];
                break;

            case 'C':
                tripletProbability *= cProb[pos];
                break;

            case 'G':
                tripletProbability *= gProb[pos];
                break;

            case 'T':
                tripletProbability *= tProb[pos];
        }
    }


    /*
    Given a nuc (A,C,T,G) and a position, this method returns the probability for that nucleotide at that position.
    This data CAN be obtained from the class' varibles, but this access is cleaner for the caller.
     */
    public double getNucleotideProb(char nuc, int nucPosition) {
        double array[];
        if (nuc == 'A') {
            array = aProb;
        } else if (nuc == 'C') {
            array = cProb;
        } else if (nuc == 'T') {
            array = tProb;
        } else {
            array = gProb;
        }
        return array[nucPosition];

    }
}