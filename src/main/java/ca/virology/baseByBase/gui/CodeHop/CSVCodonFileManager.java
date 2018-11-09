package ca.virology.baseByBase.gui.CodeHop;

import ca.virology.lib.util.gui.UITools;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
Class is used when user tries to import custom codon table from csv file.
File should be in the following:
    GCT, 0.36
    GCC, 0.13
    ...

 */

public class CSVCodonFileManager {

    double codonProb[] = new double[21];
    ArrayList<String> codonsInFile = new ArrayList<String>(64);
    String filePath;
    String fileName;
    boolean cancelled = false;
    double slack;


    public CSVCodonFileManager(JPanel parent) {

        FileFilter filter = new FileNameExtensionFilter("CSV files", "csv");
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.home")));
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(parent.getParent());

        if (result != JFileChooser.APPROVE_OPTION) {
            CodeHopSelectPanel.fileNameDisplay.setText("");
            cancelled = true;
            return;
        }

        slack = CustomCodonTable.slack;
        filePath = fileChooser.getSelectedFile().getAbsolutePath();
        fileName = fileChooser.getSelectedFile().getName();
        CodeHopSelectPanel.fileNameDisplay.setText(fileName);
    }

    /*
    Responsible for ensuring file is in the right format (codon csv).
     */
    public boolean isCorrectCSV() {

        Scanner scanner; //scanner

        try {
            scanner = new Scanner(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        scanner.useDelimiter(",|\\n");
        boolean onProbability = false; //variable to keep track of alternating pattern codon/probability/codon/prob...
        String codon = "";
        double pct;

        while (scanner.hasNext()) {

            if (!onProbability) {//if we are reading the codon
                codon = scanner.next().replace("\n", "").replace("\r", "");
                onProbability = true;
            } else {//if we are reading the probability
                onProbability = false;

                try {
                    pct = Double.parseDouble(scanner.next().replace("\n", "").replace("\r", ""));
                } catch (Exception e) {
                    //e.printStackTrace();
                    UITools.showError("Invalid csv formatting. Please refer to the help section for formatting.", CodeHopWizard.mainFrame);
                    System.out.println("csv read error, there should be a number there!");
                    scanner.close();
                    return false;
                }

                if (!readCodons(codon)) {
                    scanner.close();
                    return false;
                }
                if (!readProbabilities(getAAfromCodon(codon), pct)) {
                    UITools.showError("File content error: The sum of each probability for an AA must be close to 1!", CodeHopWizard.mainFrame);
                    scanner.close();
                    return false;
                }
            }
        }
        scanner.close();

        return true;
    }


    /*
    return a CodonTable made from the input file. simple.
    structure similar to isCorrectCSV()
     */
    public CodonTable getCodonTableFromCSV() {
        CodonTable newTable = new CodonTable();

        Scanner scanner; //scanner
        boolean onProbability = false;
        String codon = "";
        double pct;

        try {
            scanner = new Scanner(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        scanner.useDelimiter(",|\\n");

        while (scanner.hasNext()) {

            if (!onProbability) {
                codon = scanner.next().replace("\n", "").replace("\r", "");
                onProbability = true;
            } else {
                onProbability = false;
                pct = Double.parseDouble(scanner.next().replace("\n", "").replace("\r", ""));
                newTable.modify(codon, pct);
            }
        }
        scanner.close();
        return newTable;
    }

    /*
    as the file is read, the codons are passed to this function to keep track of which has been read.
    Returns true if the codon that is being read has not alrady been previously read.
     */
    private boolean readCodons(String codon) {
        if (!isCodon(codon)) {//check if codon is codon
            UITools.showError("File content error: \"" + codon + "\" not a codon!", CodeHopWizard.mainFrame);
            return false;
        }
        if (codonsInFile.size() == 0) {// add the first one
            codonsInFile.add(codon);
            return true;
        }

        for (int i = 0; i < codonsInFile.size(); i++) {//check if its already in the list
            if (codonsInFile.get(i).equals(codon)) {
                UITools.showError("File content error: Duplicate codon (" + codon + ") in CSV file!", CodeHopWizard.mainFrame);
                return false;
            }
        }

        codonsInFile.add(codon);
        return true;
    }

    /*
    As codons are read, their probabilities are added in codonProb
     */
    private boolean readProbabilities(String aa, double prob) {
        switch (aa) {
            case "A":
                codonProb[0] += prob;
                return checkCurrentProb(0, false);
            case "C":
                codonProb[1] += prob;
                return checkCurrentProb(1, false);
            case "D":
                codonProb[2] += prob;
                return checkCurrentProb(2, false);
            case "E":
                codonProb[3] += prob;
                return checkCurrentProb(3, false);
            case "F":
                codonProb[4] += prob;
                return checkCurrentProb(4, false);
            case "G":
                codonProb[5] += prob;
                return checkCurrentProb(5, false);
            case "H":
                codonProb[6] += prob;
                return checkCurrentProb(6, false);
            case "I":
                codonProb[7] += prob;
                return checkCurrentProb(7, false);
            case "K":
                codonProb[8] += prob;
                return checkCurrentProb(8, false);
            case "L":
                codonProb[9] += prob;
                return checkCurrentProb(9, false);
            case "M":
                codonProb[10] += prob;
                return checkCurrentProb(10, false);
            case "N":
                codonProb[11] += prob;
                return checkCurrentProb(11, false);
            case "P":
                codonProb[12] += prob;
                return checkCurrentProb(12, false);
            case "Q":
                codonProb[13] += prob;
                return checkCurrentProb(13, false);
            case "R":
                codonProb[14] += prob;
                return checkCurrentProb(14, false);
            case "S":
                codonProb[15] += prob;
                return checkCurrentProb(15, false);
            case "T":
                codonProb[16] += prob;
                return checkCurrentProb(16, false);
            case "V":
                codonProb[17] += prob;
                return checkCurrentProb(17, false);
            case "W":
                codonProb[18] += prob;
                return checkCurrentProb(18, false);
            case "Y":
                codonProb[19] += prob;
                return checkCurrentProb(19, false);
            case "*":
                codonProb[20] += prob;
                return checkCurrentProb(20, false);
        }
        return true;
    }

    /*
    Given an index for the codonProb array list, this method checks whether the probability contained is valid (between 0.95 and 1.05)
    finalCheck flag indicates whether or not this is the final check of the input (ie all values have been put in)
    true = all values have been read in
    false = still reading from file and adding
     */
    private boolean checkCurrentProb(int index, boolean finalCheck) {
        if (codonProb[index] > 1 + slack) {
            return false;
        } else if (finalCheck == true && codonProb[index] < 1 - slack) {
            return false;
        } else {
            return true;
        }
    }

    /*
    Determines whether input string is codon. Returns boolean.
     */
    private boolean isCodon(String codon) {
        if (codon.length() != 3) {
            return false;
        }

        char c1 = codon.charAt(0);
        char c2 = codon.charAt(1);
        char c3 = codon.charAt(2);

        if (c1 != 'A' && c1 != 'T' && c1 != 'C' && c1 != 'G') {
            return false;
        }

        if (c2 != 'A' && c2 != 'T' && c2 != 'C' && c2 != 'G') {
            return false;
        }

        if (c3 != 'A' && c3 != 'T' && c3 != 'C' && c3 != 'G') {
            return false;
        }
        return true;
    }

    /*
    returns aa from a codon. Made static so can be accessed from elsewhere. useful.
     */
    public static String getAAfromCodon(String codon) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("TTT", "F");
        map.put("TTC", "F");
        map.put("TTA", "L");
        map.put("TTG", "L");
        map.put("CTT", "L");
        map.put("CTC", "L");
        map.put("CTA", "L");
        map.put("CTG", "L");
        map.put("ATT", "I");
        map.put("ATC", "I");
        map.put("ATA", "I");
        map.put("ATG", "M");
        map.put("GTT", "V");
        map.put("GTC", "V");
        map.put("GTA", "V");
        map.put("GTG", "V");
        map.put("TCT", "S");
        map.put("TCC", "S");
        map.put("TCA", "S");
        map.put("TCG", "S");
        map.put("AGT", "S");
        map.put("AGC", "S");
        map.put("CCT", "P");
        map.put("CCC", "P");
        map.put("CCA", "P");
        map.put("CCG", "P");
        map.put("ACT", "T");
        map.put("ACC", "T");
        map.put("ACA", "T");
        map.put("ACG", "T");
        map.put("GCT", "A");
        map.put("GCC", "A");
        map.put("GCA", "A");
        map.put("GCG", "A");
        map.put("TAT", "Y");
        map.put("TAC", "Y");
        map.put("TAA", "*");
        map.put("TAG", "*");
        map.put("CAT", "H");
        map.put("CAC", "H");
        map.put("CAA", "Q");
        map.put("CAG", "Q");
        map.put("AAT", "N");
        map.put("AAC", "N");
        map.put("AAA", "K");
        map.put("AAG", "K");
        map.put("GAT", "D");
        map.put("GAC", "D");
        map.put("GAA", "E");
        map.put("GAG", "E");
        map.put("TGT", "C");
        map.put("TGC", "C");
        map.put("TGA", "*");
        map.put("TGG", "W");
        map.put("CGT", "R");
        map.put("CGC", "R");
        map.put("CGA", "R");
        map.put("CGG", "R");
        map.put("AGA", "R");
        map.put("AGG", "R");
        map.put("GGT", "G");
        map.put("GGC", "G");
        map.put("GGA", "G");
        map.put("GGG", "G");
        return map.get(codon);
    }


    /*
    Exports the UI table to csv format. Method is called when "Export table" is clicked from the UI -> static
     */
    public static void exportCurrentTableToCSV(String[][] table) {
        JFileChooser fc = new JFileChooser(new File(System.getProperty("user.home")));
        fc.setDialogTitle("Save Codon Table");
        int returnVal = fc.showSaveDialog(fc.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            String fullPathFileName = f.getAbsolutePath();
            String filename = f.getName();
            if (!fullPathFileName.endsWith(".csv")) {
                fullPathFileName += ".csv";
                f = new File(fullPathFileName);
                filename += ".csv";
            }
            if (f.exists()) {
                int ret = JOptionPane.showConfirmDialog(CodeHopWizard.mainFrame, "File \"" + filename + "\" already exists. Overwrite?", "Save codon table", JOptionPane.YES_NO_OPTION);
                if (ret == 1) {
                    return;
                }
            }
            PrintWriter writer;
            try {
                writer = new PrintWriter(fullPathFileName, "UTF-8");
            } catch (Exception e) {
                //catch this
                UITools.showError("Could not write/create file", CodeHopWizard.mainFrame);
                return;
            }
            for (int i = 0; i < table.length; i++) {
                writer.println(table[i][1] + ", " + table[i][2]);
            }
            writer.close();
            UITools.showInfoMessage("File successfully written. Custom codon table will be used.", CodeHopWizard.mainFrame);
        }
    }
}
