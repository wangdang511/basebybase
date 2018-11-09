package ca.virology.baseByBase.gui.CodeHop;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * CLASS: Block
 * <p>
 * Contains main methods of CODEHOP that are called to perform the majority of the calculations
 */
public class Block {

    Matrix matrix;
    ArrayList<DegenerateCore> forwardCoreList;
    ArrayList<DegenerateCore> reverseCoreList;
    public ArrayList<Primer> forwardPrimerList;
    public ArrayList<Primer> reversePrimerList;
    String forwardConsensusClamp;
    String reverseConsensusClamp;
    int[] forwardClampPositionScores;
    int[] reverseClampPositionScores;
    String[] block;
    ArrayList<ConsensusAA> consensus;
    public int blockAAStartPosition;


    public Block(String[] block, int blockAAStartPos) {
        this.block = block;
        this.blockAAStartPosition = blockAAStartPos;
        consensus = getConsensusSeq();
    }

    public String getBlockConsensus() {
        String out = "";
        for (int i = 0; i < consensus.size(); i++) {
            out += consensus.get(i).aminoAcid;
        }
        return out;
    }

    public void printBlock() {
        for (int i = 0; i < block.length; i++) {
            System.out.println(block[i]);
        }
        System.out.println("----");
    }

    public void generateEntireConsensusClamp() {
        forwardClampPositionScores = new int[matrix.matrix.length * 3];
        reverseClampPositionScores = new int[matrix.matrix.length * 3];

        forwardConsensusClamp = matrix.getEntireConsensusClamp(forwardClampPositionScores);
        reverseConsensusClamp = reverseAndComplement(forwardConsensusClamp);

        // reverseClampPositionScores is the reverse of forwardClampPositionScores
        for (int k = 0; k < forwardClampPositionScores.length; k++) {
            reverseClampPositionScores[k] = forwardClampPositionScores[forwardClampPositionScores.length - k - 1];
        }
    }

    public void generateCores() {
        forwardCoreList = matrix.getDegenerateCores();
        reverseCoreList = reverseAndComplementCores(forwardCoreList);


        for (int i = 0; i < forwardCoreList.size(); i++) {
            DegenerateCore c = forwardCoreList.get(i);

            //remove the last nucleotide in all FORWARD cores because the last position is always very degenerate
            int degeneracyToRemove = getDegeneracy(c.core.charAt(c.core.length() - 1));
            c.core = c.core.substring(0, c.core.length() - 1);
            c.degeneracy /= degeneracyToRemove;

            if ((CodeHopSelectPanel.invariantLastPosinput.isSelected() && isVariant(c.core.charAt(c.core.length() - 1))) ||
                    (CodeHopSelectPanel.excludeLSandAinput.isSelected() && endsWithLSorA(c.aaSeq, true)) ||
                    (CodeHopSelectPanel.GorCinLastPosinput.isSelected() && !endsWithGorC(c.core))) {
                forwardCoreList.remove(i);
                i--;
            }
        }

        for (int i = 0; i < reverseCoreList.size(); i++) {
            DegenerateCore c = reverseCoreList.get(i);

            if ((CodeHopSelectPanel.invariantLastPosinput.isSelected() && isVariant(c.core.charAt(c.core.length() - 1))) ||
                    (CodeHopSelectPanel.excludeLSandAinput.isSelected() && endsWithLSorA(c.aaSeq, false)) ||
                    (CodeHopSelectPanel.GorCinLastPosinput.isSelected() && !endsWithGorC(c.core))) {
                reverseCoreList.remove(i);
                i--;
            }
        }
    }

    private boolean endsWithLSorA(String aaSeq, boolean isForward) {
        char last;
        if (isForward) {
            last = aaSeq.charAt(aaSeq.length() - 1);
        } else {
            last = aaSeq.charAt(0);
        }

        if (last == 'L' ||
                last == 'l' ||
                last == 'S' ||
                last == 's' ||
                last == 'A' ||
                last == 'a') {
            return true;
        }
        return false;
    }

    private boolean endsWithGorC(String core) {
        char last = core.charAt(core.length() - 1);
        if (last == 'g' || last == 'c') {
            return true;
        }
        return false;
    }

    private boolean isVariant(char c) {
        if (c != 'g' && c != 'c' && c != 'a' && c != 't') {
            return true;
        }
        return false;
    }

    public void generatePrimers(int blocknum) {
        generateForwardPrimers(blocknum);
        generateReversePrimers(blocknum);
    }

    public void generateMatrix() {
        matrix = new Matrix(block, this);
    }

    public void generateForwardPrimers(int blocknum) {

        forwardPrimerList = new ArrayList<Primer>();
        for (DegenerateCore core : forwardCoreList) {
            Primer newPrimer = new Primer(core, forwardConsensusClamp, forwardClampPositionScores, blockAAStartPosition, blocknum, "forward");

            if (!newPrimer.primerSeq.equals("")) {
                forwardPrimerList.add(newPrimer);
                CodeHopWizard.primerCount++;
            }
        }
    }

    public void generateReversePrimers(int blocknum) {

        reversePrimerList = new ArrayList<Primer>();
        for (DegenerateCore core : reverseCoreList) {
            Primer newPrimer = new Primer(core, reverseConsensusClamp, reverseClampPositionScores, blockAAStartPosition, blocknum, "reverse");

            if (!newPrimer.primerSeq.equals("")) {
                //reverse the string
                newPrimer.primerSeq = new StringBuilder(newPrimer.primerSeq).reverse().toString();
                reversePrimerList.add(newPrimer);
                CodeHopWizard.primerCount++;
            }
        }
    }


    public static String reverseAndComplement(String DNA) {

        //get the complement
        char[] charArr = DNA.toCharArray();
        for (int i = 0; i < charArr.length; i++) {
            switch (charArr[i]) {
                case 'T':
                    charArr[i] = 'A';
                    break;
                case 'A':
                    charArr[i] = 'T';
                    break;
                case 'C':
                    charArr[i] = 'G';
                    break;
                case 'G':
                    charArr[i] = 'C';
                    break;
            }
        }
        String newDNA = new String(charArr);

        //reverse the string
        newDNA = new StringBuilder(newDNA).reverse().toString();

        return newDNA;
    }


    public ArrayList<DegenerateCore> reverseAndComplementCores(ArrayList<DegenerateCore> coreList) {

        //make a clone of every object in forwardCoreList
        ArrayList<DegenerateCore> reverseCores = new ArrayList<DegenerateCore>();

        for (DegenerateCore core : coreList) {
            DegenerateCore clone = new DegenerateCore(core);

            adjustPositionsForCalculations(clone);

            //get the reverse complement
            char[] charArr = clone.core.toCharArray();
            for (int i = 0; i < charArr.length; i++) {
                switch (charArr[i]) {
                    case 'a':
                        charArr[i] = 't';
                        break;
                    case 'c':
                        charArr[i] = 'g';
                        break;
                    case 'g':
                        charArr[i] = 'c';
                        break;
                    case 't':
                        charArr[i] = 'a';
                        break;
                    case 'r':
                        charArr[i] = 'y';
                        break;
                    case 'y':
                        charArr[i] = 'r';
                        break;
                    case 's':
                        charArr[i] = 's';
                        break;
                    case 'w':
                        charArr[i] = 'w';
                        break;
                    case 'k':
                        charArr[i] = 'm';
                        break;
                    case 'm':
                        charArr[i] = 'k';
                        break;
                    case 'b':
                        charArr[i] = 'v';
                        break;
                    case 'd':
                        charArr[i] = 'h';
                        break;
                    case 'h':
                        charArr[i] = 'd';
                        break;
                    case 'v':
                        charArr[i] = 'b';
                        break;
                    case 'n':
                        charArr[i] = 'n';
                        break;
                    case '-':
                        charArr[i] = '-';
                        break;
                }
            }
            clone.core = new String(charArr);

            //reverse the string
            clone.core = new StringBuilder(clone.core).reverse().toString();

            reverseCores.add(clone);
        }
        return reverseCores;
    }


    public void adjustPositionsForCalculations(DegenerateCore clone) {
        int lastNucPosInBlock = matrix.blockAALen * 3 - 1;
        int nucCoreLength = CodeHopSelectPanel.getDegenerateCoreLength() * 3;
        clone.startNTPos = lastNucPosInBlock - clone.startNTPos - nucCoreLength + 1;
    }


    /*
        Produces the consensus Amino acid sequence (and the frequency of each AA)
        Output is used as a visual aid for displaying results
     */
    private ArrayList<ConsensusAA> getConsensusSeq() {
        ArrayList<ConsensusAA> consensus = new ArrayList<ConsensusAA>();

        int[] tracker = new int[21];

        if (block.length == 0) return null;

        int blocklen = block[0].length();
        int numBlocks = block.length;


        for (int i = 0; i < blocklen; i++) {
            Arrays.fill(tracker, 0);
            for (int k = 0; k < numBlocks; k++) {
                char aminoAcid = block[k].charAt(i);
                tracker[CodonTable.getLookupVal(aminoAcid)]++;
            }

            int lookupVal = getConsensusAA(tracker, numBlocks);
            char aminoAcid = CodonTable.getAminoAcid(lookupVal);
            double freq = tracker[lookupVal] / (double) numBlocks;
            consensus.add(new ConsensusAA(aminoAcid, freq));
        }

        return consensus;
    }

    public int getConsensusAA(int[] tracker, int numBlocks) {
        int max = tracker[0];
        int maxIndex = 0;

        for (int i = 1; i < tracker.length; i++) {
            if (tracker[i] > max) {
                max = tracker[i];
                maxIndex = i;
            }
        }
        if (tracker[maxIndex] == numBlocks) {

        }
        return maxIndex;
    }

    public int getDisplayStart(int NTStartPosInBlock, int sideAAs) {
        int startPos = NTStartPosInBlock / 3 - sideAAs;
        if (startPos < 0) {
            startPos = 0;
        }
        return startPos;
    }


    public int getDisplayEnd(int NTEndPosInBlock, int sideAAs) {
        int endPos = NTEndPosInBlock / 3 + sideAAs;
        if (endPos > consensus.size()) {
            endPos = consensus.size();
        }
        return endPos;
    }


    public String getConsensusChunk(int start, int end) {
        String consensusChunk = "";
        for (int i = start; i < end; i++) {
            consensusChunk += consensus.get(i).aminoAcid;
        }
        return consensusChunk;
    }

    private int getDegeneracy(char NT) {
        switch (NT) {
            case 'n':
                return 4;

            case 'b':
                return 3;
            case 'v':
                return 3;
            case 'h':
                return 3;
            case 'd':
                return 3;

            case 's':
                return 2;
            case 'y':
                return 2;
            case 'k':
                return 2;
            case 'm':
                return 2;
            case 'r':
                return 2;
            case 'w':
                return 2;

            case 'c':
                return 1;
            case 'g':
                return 1;
            case 't':
                return 1;
            case 'a':
                return 1;
        }
        return 1; //should never get here
    }
}