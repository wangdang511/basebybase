package ca.virology.baseByBase.gui.CodeHop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//=========================================================================
// Class: Matrix
//
// Stores MatrixEntry objects in an Array of ArrayList
//=========================================================================
public class Matrix {

    Block Block;
    String[] block; //array of amino acid sequences
    int numSeqs, blockAALen;
    ArrayList<MatrixEntry>[] matrix; //array of arraylists to store MatrixEntry objects
    double[] AAfreq;
    ArrayList<DNACodonProbability> conscensusTriplets = new ArrayList<DNACodonProbability>(); //for each analyzed codon, this holds the most likely one and its probablity


    public Matrix(String[] block, Block blockHolder) {

        Block = blockHolder;
        this.block = block;
        numSeqs = block.length;
        blockAALen = block[0].length();
        matrix = (ArrayList<MatrixEntry>[]) new ArrayList[blockAALen];
        AAfreq = new double[21];

        //initialize each matrix element as an empty ArrayList
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = new ArrayList();
        }

        for (int col = 0; col < blockAALen; col++) {
            Arrays.fill(AAfreq, 0); //set all array indices to 0

            //count number of times each amino acid occurs in a column
            for (int row = 0; row < numSeqs; row++) {
                char AA = block[row].charAt(col);
                AAfreq[CodonTable.getLookupVal(AA)]++;
            }

            //divide each value by the number of sequences to produce the frequency
            //store each frequency and corresponding amino acid in MatrixEntry object
            //store MatrixEntry object in matrix
            for (int index = 0; index < AAfreq.length; index++) {
                AAfreq[index] /= numSeqs;
                if (AAfreq[index] > 0) {
                    char AA = CodonTable.getAminoAcid(index);
                    matrix[col].add(new MatrixEntry(AA, AAfreq[index]));
                }
            }
        }
    }


    //=========================================================================
    // Method: getEntireConsensusClamp()
    //
    // Calculates a consensus for the entire sequnce of amino acids to allow for easy primer extension.
    // Not to be confused with a regular consensus, which simply takes the most occurring amino acid in a column.
    // Each primer has it's own consensus clamp, which is created by taking nucleotides from this entire consensus clamp
    //
    // Looks at each codon probability in the block -> matrix -> matrix entry ->codonOccurrence
    // See documentation for algorithm
    //=========================================================================
    public String getEntireConsensusClamp(int[] clampPositionScores) {
        String entireConsensusClamp = "";
        double[] aScore = new double[3]; //will hold the percentage of each codon
        double[] gScore = new double[3]; // ATG : a= [0.24][0][0]
        double[] cScore = new double[3]; //       t= [0][0.4][0]
        double[] tScore = new double[3]; //       g= [0][0][0.33]


        //"The big daddy"
        for (int column = 0; column < matrix.length; column++) { //for every matrix column
            Arrays.fill(aScore, 0);
            Arrays.fill(cScore, 0);
            Arrays.fill(tScore, 0);
            Arrays.fill(gScore, 0);
            for (int matrixEntry = 0; matrixEntry < matrix[column].size(); matrixEntry++) { //for every matrixEntry in the matrix column
                for (int codon = 0; codon < matrix[column].get(matrixEntry).codonOccurrenceArray.size(); codon++) { //for every codonEntry in matrixEntry
                    for (int charInCodon = 0; charInCodon < 3; charInCodon++) { // for every codon
                        CodonOccurrence currentCodon = matrix[column].get(matrixEntry).codonOccurrenceArray.get(codon);
                        //determine the occurrence frequency for each codon letter in the codon column
                        if (currentCodon.codonName.charAt(charInCodon) == 'A') {
                            aScore[charInCodon] += currentCodon.codonFreq;
                        }
                        if (currentCodon.codonName.charAt(charInCodon) == 'C') {
                            cScore[charInCodon] += currentCodon.codonFreq;
                        }
                        if (currentCodon.codonName.charAt(charInCodon) == 'T') {
                            tScore[charInCodon] += currentCodon.codonFreq;
                        }
                        if (currentCodon.codonName.charAt(charInCodon) == 'G') {
                            gScore[charInCodon] += currentCodon.codonFreq;
                        }
                    }
                }
            }
            //place the results in an object, and obj to list
            DNACodonProbability dataHolder = new DNACodonProbability(aScore, gScore, cScore, tScore, clampPositionScores, column);
            conscensusTriplets.add(dataHolder);
        }
        for (int k = 0; k < conscensusTriplets.size(); k++) {
            entireConsensusClamp += conscensusTriplets.get(k).triplet;
        }
        return getAdjustedConsensusClamp(entireConsensusClamp);
    }

    /* ===============================================================
        Method: getAdjustedConsensusClamp(string)

       Given the original output consensus from getEntireConsensusClamp(), this calls 2 other methods:
       isPolyNucLimitExceeded() - checks whether string DNA needs to be corrected and returns the indexes of the problematic substring
       getHighestProbabilityIndexCodonFromList()  - returns the nuc with the lowest probability from the substring

       the consensus clamp is made up of codons (groups of 3 nucleotides), so ATGATGGTCACA is actually formed up of codons
       ATG,ATG,GTC,ACA put together from DNACodonProbability.

       When attempting to adjust the consensus clamp, the method must access the list of DNACodonProbabilities to see the original probability
       of each nucl. in each codon. Throughout this method (and the two mentioned above in this comment), indexes are often i/3 or i%3 to account for which
       DNACodonProbability object a specific one belongs in the list <consensusTriplets> (since every 3 belong in one position of the list - list.get(i/3)).
       The mod operator is to obtain the position of the codon.
       Ex: ATCTAAGTTAAA
       The position of G in the string is 6, so it belongs in the 6/3= 2nd block: ATC,TAA,GTT,AAA and is located at the 6%3 =0 spot : GTT

       ===============================================================
     */
    private String getAdjustedConsensusClamp(String inputDNA) {
        String adjustedDNA = inputDNA;
        String original = inputDNA;
        boolean changes = false;
        int positions[];
        int lowestValIndex;
        char workingOn;

        do {
            positions = isPolyNucLimitExceeded(adjustedDNA);

            if (positions[0] == -1 && positions[1] == -1) {
                changes = false;

            } else {
                changes = true;
                workingOn = adjustedDNA.charAt(positions[0]);

                if (workingOn == 'A') {
                    lowestValIndex = getLowestProbabilityIndexCodonFromList(positions[0], positions[1], 'A');
                } else if (workingOn == 'C') {
                    lowestValIndex = getLowestProbabilityIndexCodonFromList(positions[0], positions[1], 'C');
                } else if (workingOn == 'G') {
                    lowestValIndex = getLowestProbabilityIndexCodonFromList(positions[0], positions[1], 'G');
                } else {
                    lowestValIndex = getLowestProbabilityIndexCodonFromList(positions[0], positions[1], 'T');
                }

                conscensusTriplets.get(lowestValIndex / 3).setNextBest(lowestValIndex % 3);
                String temp = "";
                for (int i = 0; i < conscensusTriplets.size(); i++) {
                    temp += conscensusTriplets.get(i).triplet;
                }
                adjustedDNA = temp;
            }

        } while (changes);

        if (changes) {
            String temp = "";
            for (int i = 0; i < conscensusTriplets.size(); i++) {
                temp += conscensusTriplets.get(i).triplet;
            }
            adjustedDNA = temp;
        }

        return adjustedDNA;
    }

    /*
    ======================================================================
    method:  getHighestProbabilityCodonFromList()

    given substring indeces, this method looks at consensustriplets list and returns the index of the list
    in which there is the object with the lowest DNACodonProbability.

    ======================================================================
     */
    private int getLowestProbabilityIndexCodonFromList(int lowIndex, int highIndex, char codon) {

        //set the first postition as the highest and then compare the rest
        int codonPosition = lowIndex % 3;
        int lowestValIndex = lowIndex;
        double lowestVal = conscensusTriplets.get(lowestValIndex / 3).getNucleotideProb(codon, codonPosition);

        for (int i = lowIndex; i < highIndex; i++) {
            if (conscensusTriplets.get(i / 3).getNucleotideProb(codon, i % 3) < lowestVal) {
                lowestVal = conscensusTriplets.get(i / 3).getNucleotideProb(codon, i % 3);
                lowestValIndex = i;
            }
        }
        return lowestValIndex;
    }


    /*  ==================================================================
        Method: isPolyNucLimitExceeded(String)

        Given a string (nucleotides), this method CHECKS to see whether there are <limit> number of consecutive letters in the string.
        if there are more than the <limit> number of characters in a row, method returns the start and end index of the substring
        of consecutive characters ([n][n+m]) else it returns [-1][-1].
        <limit> is set from the codehopselect panel as one of the JSpinners

        ==================================================================
    */
    private int[] isPolyNucLimitExceeded(String a) {
        int limit = 4; //need at least limit+1 consecutive chars to trigger change
        String aPattern = "A{" + limit + ",}";
        String cPattern = "C{" + limit + ",}";
        String gPattern = "G{" + limit + ",}";
        String tPattern = "T{" + limit + ",}";
        Pattern aP = Pattern.compile(aPattern);
        Pattern cP = Pattern.compile(cPattern);
        Pattern gP = Pattern.compile(gPattern);
        Pattern tP = Pattern.compile(tPattern);
        Matcher m;
        int pos[] = {-1, -1};

        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                m = aP.matcher(a);
            } else if (i == 1) {
                m = cP.matcher(a);
            } else if (i == 2) {
                m = gP.matcher(a);
            } else {
                m = tP.matcher(a);
            }

            if (m.find()) {
                pos[0] = m.start();
                pos[1] = m.end(); //end() returns the index of the character just after the end of the matching section (for use in forloop upper index)
                return pos;
            }
        }
        return pos;
    }


    //=========================================================================
    // Method: getDegenerateCores()
    //
    // Generates all possible degenerate cores in a block
    // Returns an ArrayList of DegenerateCore objects
    //
    // Refer to documentation for in depth explanation and example
    //=========================================================================
    public ArrayList<DegenerateCore> getDegenerateCores() {


        // user input variables (from CodeHopSelectPanel)
        int len = CodeHopSelectPanel.getDegenerateCoreLength();
        double strictness = CodeHopSelectPanel.getStrictness();
        int maxDegeneracy = CodeHopSelectPanel.getDegeneracy();

        // minimum frequency an amino acid must appear to be included in degeneracy calculation
        double minFreq = (double) CodeHopSelectPanel.getminAAFreq() / 100;

        // list containing all possible found core regions
        ArrayList<DegenerateCore> coreList = new ArrayList<DegenerateCore>();

        //list containing all excluded AAs which did not meet the cutoff
        ArrayList<MatrixEntry>[] excludedAAList = (ArrayList<MatrixEntry>[]) new ArrayList[blockAALen];
        for (int i = 0; i < excludedAAList.length; i++) {
            excludedAAList[i] = new ArrayList<MatrixEntry>();
        }

        // to be changed later - used because cores near the beginning of a sequence will never be  used because there is no room for a consensus clamp
        int startPos = 0;

        // stores the consensus codon for each column
        // to be used when concatenating to produce complete core string
        String[] coreArr = new String[blockAALen];
        Arrays.fill(coreArr, "");

        // stores the codon degeneracy for each column
        int[] colDegenArr = new int[blockAALen];
        Arrays.fill(colDegenArr, 1);


        //====================================================================
        // Go through every column, determining its consensus codon and degeneracy

        // iterate through every column
        for (int col = startPos; col < blockAALen; col++) {

            //list containing all codons for a column of amino acids
            ArrayList<CodonTableEntry> codons = appendAllCodons(excludedAAList, col, minFreq);

            //for each nucleotide within a codon, determine the consensus nucleotide and degeneracy
            for (int nucPos = 0; nucPos < 3; nucPos++) {


                // holds the nucleotide frequency (as a # / 100) - called the Value
                //refer to this link for more info on value and ratio: http://blocks.fhcrc.org/blocks/help/CODEHOP/CODEHOP_strictness.html
                double[] nucFreq = new double[4];

                //boolean array where each position corresponds to a nucleotide as follows: [A, T, G, C]
                //used to track whether a nucleotide is present
                boolean[] containsNucleotide = new boolean[4];

                //for each nucleotide set its value (percent occurrence)
                setNucValues(nucPos, nucFreq, codons);


                double max = getMaxValue(nucFreq);
                int colDegen = 0; //keeps track of nucleotides with ratio greater than strictness

                // first, get ratio for each amino acid
                // test if column degeneracy is greater than the user input strictness.
                // if yes, append correct letter to core string and increment colDegen
                for (int i = 0; i < nucFreq.length; i++) {
                    double ratio = nucFreq[i] / max;
                    if (ratio >= strictness && ratio != 0) {
                        colDegen++;
                        containsNucleotide[i] = true;
                    }
                }

                char nuc = getNucleotideCode(containsNucleotide);

                coreArr[col] += nuc;
                colDegenArr[col] *= colDegen;
            }
        }


        //===================================================================
        // Look at every possible start position to determine all possible cores

        for (int col = startPos; col <= matrix.length - len; col++) {

            String core = "";
            int degeneracy = 1;
            boolean belowMinAAFreq = false;
            String aaSeq = "";

            for (int index = col; index < col + len; index++) {
                if (coreArr[index].charAt(0) == '-') {
                    belowMinAAFreq = true;
                    break;
                }
                aaSeq += Block.getBlockConsensus().charAt(index);
                core += coreArr[index];
                degeneracy *= colDegenArr[index];
            }

            //if within degeneracy range create new DegenerateCore object and add to coreList
            if (degeneracy <= maxDegeneracy && !belowMinAAFreq) {
                coreList.add(new DegenerateCore(col * 3, core, degeneracy, Arrays.copyOfRange(excludedAAList, col, col + len - 1), aaSeq));
            }
        }
        return coreList;
    }


    //=========================================================================
    // Method: setNucValues()
    //
    // Sets the nucleotide values for every position in a codon(percent occurrence)
    //=========================================================================
    public void setNucValues(int nucPos, double[] nucFreq, ArrayList<CodonTableEntry> codons) {
        for (int codon = 0; codon < codons.size(); codon++) {
            char nuc = codons.get(codon).codon.charAt(nucPos);
            if (nuc == 'A') {
                nucFreq[0] += 100 / (double) codons.size();
            } else if (nuc == 'T') {
                nucFreq[1] += 100 / (double) codons.size();
            } else if (nuc == 'G') {
                nucFreq[2] += 100 / (double) codons.size();
            } else if (nuc == 'C') {
                nucFreq[3] += 100 / (double) codons.size();
            }
        }
    }


    //=========================================================================
    // Method: appendAllCodons(int, double)
    //
    // Returns a list of all codons in a column (including repeated codons)
    //=========================================================================
    public ArrayList<CodonTableEntry> appendAllCodons(ArrayList<MatrixEntry>[] excludedAA, int col, double minFreq) {

        ArrayList<CodonTableEntry> codons = new ArrayList<CodonTableEntry>();

        //for each row, append all codons to codons ArrayList
        for (int row = 0; row < matrix[col].size(); row++) {
            char aminoAcid = matrix[col].get(row).aminoAcidName;
            double AAfreq = matrix[col].get(row).aminoAcidFreq;

            if (AAfreq >= minFreq) {
                double AAcount = AAfreq * numSeqs;
                ArrayList<CodonTableEntry> codonlist = CodonTable.getCodons(aminoAcid);
                for (int i = 0; i < AAcount; i++) {
                    codons.addAll(codonlist);
                }
            } else {
                excludedAA[col].add(matrix[col].get(row));
            }
        }
        return codons;
    }


    //=========================================================================
    // Method: getMaxValue()
    //
    // Returns the maximum codon frequency in codonFreq array
    //=========================================================================
    public double getMaxValue(double[] codonFreq) {
        double max = 0;

        for (int i = 0; i < codonFreq.length; i++) {
            if (codonFreq[i] > max) {
                max = codonFreq[i];
            }
        }
        return max;
    }


    //=========================================================================
    // Method: getNucleotideCode(boolean[])
    //
    // Returns the respective nucleotide code given which nucleotides are present in a column
    // The following website was used for reference: http://www.bioinformatics.org/sms/iupac.html
    //=========================================================================
    public char getNucleotideCode(boolean[] containsNucleotide) {
        String s = Arrays.toString(containsNucleotide);

        // order of nucleotides in boolean array:  [A, T, G, C]
        // true if nucleotide is present, false otherwise
        switch (s) {
            //all true
            case "[true, true, true, true]":
                return 'n';

            //1 false
            case "[false, true, true, true]":
                return 'b';
            case "[true, false, true, true]":
                return 'v';
            case "[true, true, false, true]":
                return 'h';
            case "[true, true, true, false]":
                return 'd';

            //2 false
            case "[false, false, true, true]":
                return 's';
            case "[false, true, false, true]":
                return 'y';
            case "[false, true, true, false]":
                return 'k';

            case "[true, false, false, true]":
                return 'm';
            case "[true, false, true, false]":
                return 'r';
            case "[true, true, false, false]":
                return 'w';

            //3 false
            case "[false, false, false, true]":
                return 'c';
            case "[false, false, true, false]":
                return 'g';
            case "[false, true, false, false]":
                return 't';
            case "[true, false, false, false]":
                return 'a';
        }

        //all false
        return '-';
    }
}