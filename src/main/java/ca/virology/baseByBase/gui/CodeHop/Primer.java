package ca.virology.baseByBase.gui.CodeHop;

import java.util.ArrayList;
import java.lang.Math;

/**
 * CLASS: Primer
 * <p>
 * The constructor of this class contains the calculations for generating primers
 * using the pre-generated consensus and core
 */
public class Primer {

    boolean computeAvgTemp = false;
    boolean computeConsensusClampTemp = CodeHopWizard.properTempCalc;

    public int startNTPosInSeq; //inclusive
    public int endNTPosInSeq; //exclusive
    public int startNTPosInBlock; //inclusive
    public int endNTPosInBlock; //exclusive
    public String primerSeq;
    public int degeneracy;
    ArrayList<MatrixEntry>[] excludedAAList;
    ArrayList<String> allPossibleCores;
    ArrayList<Character>[] allPossibleNucs;
    String clampSeq;
    int clampScore;
    double tempSlack = 5;
    double requestedTemp;
    public double temp;
    public int blocknum;
    String direction;
    String primerName;

    public Primer(DegenerateCore core, String entireConsensusSeq, int[] clampPositionScores, int blockAAStartPosition, int blocknum, String direction) {

        if (direction.equals("forward")) {
            this.direction = "forward";
            primerName = core.aaSeq + "-F " + core.degeneracy + "x";
        } else if (direction.equals("reverse")) {
            this.direction = "reverse";
            primerName = core.aaSeq + "-R " + core.degeneracy + "x";
        }

        primerSeq = "";
        excludedAAList = core.excludedAA;
        degeneracy = core.degeneracy;
        allPossibleCores = new ArrayList();
        clampSeq = "";
        requestedTemp = CodeHopSelectPanel.getTemp();
        this.blocknum = blocknum;

        if (!computeConsensusClampTemp) {
            requestedTemp = CodeHopSelectPanel.getUserDefinedConsensusClampLength();
        }

        temp = 0;
        int currentPos = core.startNTPos - 1; //start looking at nucleotides 1 position before the core region

        if (currentPos < 0) {//can probably remove this later when we change the startPos of getDegenerateCores so a core in the 0th position is never created
            return;
        }


        //temporary - computes the average temp of all possible degenerate cores
        if (computeAvgTemp) {
            allPossibleNucs = getAllPossibleNucs(core.core);
            getAllPossibleCores("", allPossibleNucs, 0);
            temp = getAvgTemp();
        }


        //continue making the primer longer until the temperature has reached the correct value
        while (temp < requestedTemp) {
            clampSeq = entireConsensusSeq.charAt(currentPos) + clampSeq;
            currentPos--;


            if (computeAvgTemp) {
                temp = getAvgTemp();
            } else if (computeConsensusClampTemp) {
                temp = Temp.getHyfiTM(clampSeq, Block.reverseAndComplement(clampSeq), CodeHopSelectPanel.getPrimerConcentration(), 1e-14, 0.05, 0.002);
            } else { //mimicking temperature calculation to speedup displaying results
                temp += 1;
            }


            // will need to change the second condition to allow the primer to be slightly under the requested temp and still work
            //not enough nucleotides to reach desired temperature - no primer created
            if (currentPos < 0 && temp < requestedTemp) {
                if (temp < requestedTemp - tempSlack) {
                    return; //don't make a primer
                } else {
                    break; //break out of loop and still make primer
                }
            }
        }
        currentPos++;

        // if the temperature has gone above the requested temperature determine best temp to use (temp before or after going above requested temp)
        if (temp > requestedTemp) {
            double tempBefore = temp;
            if (clampSeq.length() != 0) {
                clampSeq = clampSeq.substring(1);
                currentPos++;
            }
            //double tempAfter = getAvgTemp();
            double tempAfter = Temp.getHyfiTM(clampSeq, Block.reverseAndComplement(clampSeq), CodeHopSelectPanel.getPrimerConcentration(), 1e-14, 0.05, 0.002);

            if (Math.abs(tempBefore - requestedTemp) < Math.abs(tempAfter - requestedTemp)) {
                currentPos--;
                clampSeq = entireConsensusSeq.charAt(currentPos) + clampSeq;
            } else {
                temp = tempAfter;
            }

        }
        primerSeq = clampSeq + core.core;

        startNTPosInBlock = currentPos; //inclusive start position
        endNTPosInBlock = startNTPosInBlock + primerSeq.length(); //exclusive end position

        clampScore = getClampScore(clampPositionScores);

        if (direction.equals("reverse")) {
            adjustPositionsForDisplay();
        }

        startNTPosInSeq = startNTPosInBlock + blockAAStartPosition * 3;
        endNTPosInSeq = startNTPosInSeq + primerSeq.length();

    }

    private int getClampScore(int[] clampPositionScores) {
        int score = 0;
        int end = startNTPosInBlock + clampSeq.length();

        for (int i = startNTPosInBlock; i < end; i++) {
            score += clampPositionScores[i];
        }

        return score / clampSeq.length();
    }

    public void adjustPositionsForDisplay() {
        int lastNucPosInBlock = CodeHopWizard.blockList.get(blocknum - 1).matrix.blockAALen * 3 - 1;
        startNTPosInBlock = lastNucPosInBlock - startNTPosInBlock - primerSeq.length() + 1;
        endNTPosInBlock = startNTPosInBlock + primerSeq.length();
    }


    //=========================================================================
    // Method: getAvgTemp()
    //
    // Returns the average temperature of all possible degenerate cores
    //=========================================================================
    public double getAvgTemp() {
        double avgTemp = 0;
        String primer;

        for (String core : allPossibleCores) {
            primer = clampSeq + core;
            avgTemp += Temp.getHyfiTM(primer, Block.reverseAndComplement(primer), CodeHopSelectPanel.getPrimerConcentration(), 1e-14, 0.05, 0.002);
        }
        avgTemp /= allPossibleCores.size();
        return avgTemp;
    }


    //=========================================================================
    // Method: getAllPossibleCores(String, ArrayList<Character>[], int)
    //
    // Returns an arrayList of all possible degenerate cores
    //=========================================================================
    public void getAllPossibleCores(String core, ArrayList<Character>[] allPossibleNucs, int k) {
        if (k == allPossibleNucs.length) {
            allPossibleCores.add(core);
        } else {
            for (Character nuc : allPossibleNucs[k]) {
                getAllPossibleCores(core + nuc, allPossibleNucs, k + 1);
            }
        }
    }


    //=========================================================================
    // Method: getAllPossibleNucs(String)
    //
    // Returns an array of arraylists where each position in the array holds an
    //  arraylist that contains all possible nucleotides that can be in that
    //  position of the degenerate core
    //=========================================================================
    public ArrayList<Character>[] getAllPossibleNucs(String core) {

        ArrayList<Character>[] allPossibleNucs = (ArrayList<Character>[]) new ArrayList[core.length()];

        //initialize each matrix element as an empty ArrayList
        for (int i = 0; i < allPossibleNucs.length; i++) {
            allPossibleNucs[i] = new ArrayList();
        }

        for (int i = 0; i < core.length(); i++) {

            switch (core.charAt(i)) {
                case 'n':
                    allPossibleNucs[i].add('A');
                    allPossibleNucs[i].add('T');
                    allPossibleNucs[i].add('C');
                    allPossibleNucs[i].add('G');
                    break;

                case 'b':
                    allPossibleNucs[i].add('C');
                    allPossibleNucs[i].add('G');
                    allPossibleNucs[i].add('T');
                    break;

                case 'v':
                    allPossibleNucs[i].add('A');
                    allPossibleNucs[i].add('C');
                    allPossibleNucs[i].add('G');
                    break;

                case 'h':
                    allPossibleNucs[i].add('A');
                    allPossibleNucs[i].add('C');
                    allPossibleNucs[i].add('T');
                    break;

                case 'd':
                    allPossibleNucs[i].add('A');
                    allPossibleNucs[i].add('G');
                    allPossibleNucs[i].add('T');
                    break;

                case 's':
                    allPossibleNucs[i].add('G');
                    allPossibleNucs[i].add('C');
                    break;

                case 'y':
                    allPossibleNucs[i].add('C');
                    allPossibleNucs[i].add('T');
                    break;

                case 'k':
                    allPossibleNucs[i].add('G');
                    allPossibleNucs[i].add('T');
                    break;

                case 'm':
                    allPossibleNucs[i].add('A');
                    allPossibleNucs[i].add('C');
                    break;

                case 'r':
                    allPossibleNucs[i].add('A');
                    allPossibleNucs[i].add('G');
                    break;

                case 'w':
                    allPossibleNucs[i].add('A');
                    allPossibleNucs[i].add('T');
                    break;

                case 'c':
                    allPossibleNucs[i].add('C');
                    break;

                case 'g':
                    allPossibleNucs[i].add('G');
                    break;

                case 't':
                    allPossibleNucs[i].add('T');
                    break;

                case 'a':
                    allPossibleNucs[i].add('A');
            }
        }
        return allPossibleNucs;
    }
}
