package ca.virology.baseByBase.gui.CodeHop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import ca.virology.baseByBase.gui.CodeHop.VGOFiles.OrganismFactory2;
import ca.virology.baseByBase.gui.CodeHop.VGOFiles.SelectionEvent2;
import ca.virology.baseByBase.gui.CodeHop.VGOFiles.SequenceMapWindow2;
import ca.virology.lib.util.gui.UITools;
import ca.virology.vgo.data.*;


public class CodeHopResultsPanel extends JPanel {

    static JPanel selectPanel;
    static JScrollPane infoScrollPane;
    static JPanel exportPanel;
    static JScrollPane exportScrollPane;
    static JEditorPane infoPane;
    static JSplitPane horizontalSplit;
    static JSplitPane verticalSplit;
    static JPanel mainContentPanel;
    static JPanel buttonPanel;
    static JButton export;
    static JButton selectPrimerInExportList;
    static JButton deselectPrimerInExportList;

    static Primer currentSelectedPrimer;
    static boolean exportButtonsVisible;

    static ArrayList<PrimerCheckBox> primerCheckBoxes;

    public CodeHopResultsPanel() {
        currentSelectedPrimer = null;
        exportButtonsVisible = false;
    }

    static class PrimerCheckBox {
        Primer primer;
        JCheckBox checkBox;

        public PrimerCheckBox(Primer p, JCheckBox c) {
            primer = p;
            checkBox = c;
        }
    }

    public void reset() {
        this.removeAll();
        selectPanel = null;
        infoScrollPane = null;
        exportPanel = null;
        exportScrollPane = null;
        infoPane = null;
        horizontalSplit = null;
        verticalSplit = null;
        mainContentPanel = null;
        buttonPanel = null;
        export = null;
        selectPrimerInExportList = null;
        deselectPrimerInExportList = null;
        currentSelectedPrimer = null;
        exportButtonsVisible = false;
        primerCheckBoxes = null;
    }

    public void showResults() {

        infoPane = new JEditorPane();
        infoPane.setEditable(false);

        try {
            Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
            infoPane.setContentType("text/html");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fontfamily = "Courier New";
        if (CodeHopWizard.primerCount == 0) {
            infoPane.setText("<p style=\"font-family:" + fontfamily + "\">No primers found. View the help for information on how to create more primers.<p>");
        } else {
            infoPane.setText("<p style=\"font-family:" + fontfamily + "\">Select primer in lower pane to view details.<br><br>" +
                    "Too many primers? Too few primers? Check out the help.<p>");
        }

        infoScrollPane = new JScrollPane(infoPane);
        infoScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.gray));
        Organism o = null;

        try {
            /*
            the lower display in the results panel (blue primers along a horizontal display) is taken from VGO.
            Therefore a lot of design constraints had to be applied. The Organism created below is the one in which VGO
            would normally look at and show some information. Here we have created a 'dummy' organism and stuffed it with
            data computed for codehop. This way the rest of the display uses exactly the same code.
            (Anyfile named *2.java is copied from  VGO and edited for codehop purposes)
             */
            OrganismFactory2 of = new OrganismFactory2();
            o = of.createOrganism();

        } catch (Exception e) {
            e.printStackTrace();
        }

        SequenceMapWindow2 smw;
        java.util.List<Organism> myList = new ArrayList();
        myList.add(o);
        try {
            smw = new SequenceMapWindow2(myList, null);
            smw.setVisible(false);
        } catch (OrganismException e) {
            e.printStackTrace();
            UITools.showError("Unexpected Error. This was not supposed to happen.", CodeHopWizard.getInstance());
            return;
        }

        createExportButtonPanel();
        createExportPanel();
        addExportFunction();

        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(infoScrollPane);
        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

        exportScrollPane = new JScrollPane(exportPanel);
        exportScrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.gray));

        selectPanel = smw.getM_mainPane();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        verticalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mainContentPanel, exportScrollPane);
        verticalSplit.setDividerLocation(930);
        verticalSplit.setResizeWeight(1);

        horizontalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, verticalSplit, selectPanel);
        horizontalSplit.setDividerLocation(500);
        horizontalSplit.setResizeWeight(1);

        this.add(horizontalSplit);
    }

    public void createExportButtonPanel() {

        selectPrimerInExportList = new JButton("Select Primer in Export List");
        deselectPrimerInExportList = new JButton("Deselect Primer in Export List");
        selectPrimerInExportList.setVisible(false);
        deselectPrimerInExportList.setVisible(false);
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.gray));
        buttonPanel.add(selectPrimerInExportList);
        buttonPanel.add(deselectPrimerInExportList);

        selectPrimerInExportList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (PrimerCheckBox primerCheckBox : primerCheckBoxes) {
                    if (currentSelectedPrimer.primerName.equals(primerCheckBox.checkBox.getText())) {
                        primerCheckBox.checkBox.setSelected(true);
                        break;
                    }
                }
            }
        });

        deselectPrimerInExportList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (PrimerCheckBox primerCheckBox : primerCheckBoxes) {
                    if (currentSelectedPrimer.primerName.equals(primerCheckBox.checkBox.getText())) {
                        primerCheckBox.checkBox.setSelected(false);
                        break;
                    }
                }
            }
        });

    }


    public static void createExportPanel() {
        exportPanel = new JPanel();
        exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.Y_AXIS));
        exportPanel.setOpaque(true);
        exportPanel.setBackground(Color.WHITE);
        exportPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        exportPanel.add(new JLabel("Primers to Export"));
        exportPanel.add(new JLabel(" "));

        primerCheckBoxes = new ArrayList<PrimerCheckBox>();
        PrimerCheckBox primerCheckBox;
        int blockNum = 1;
        for (Block block : CodeHopWizard.blockList) {
            if (block.forwardPrimerList.size() != 0 || block.reversePrimerList.size() != 0) {
                exportPanel.add(new JLabel("Block " + blockNum));

                for (Primer primer : block.forwardPrimerList) {
                    primerCheckBox = new PrimerCheckBox(primer, new JCheckBox(primer.primerName));
                    exportPanel.add(primerCheckBox.checkBox);
                    primerCheckBoxes.add(primerCheckBox);
                }
                for (Primer primer : block.reversePrimerList) {
                    primerCheckBox = new PrimerCheckBox(primer, new JCheckBox(primer.primerName));
                    exportPanel.add(primerCheckBox.checkBox);
                    primerCheckBoxes.add(primerCheckBox);
                }
            }
            blockNum++;
        }
        exportPanel.add(Box.createVerticalGlue());
        export = new JButton("Export Primers");
        exportPanel.add(export);
    }


    public static void updateTopPanel(SelectionEvent2 sev) {

        if (!exportButtonsVisible) {
            selectPrimerInExportList.setVisible(true);
            deselectPrimerInExportList.setVisible(true);
            exportButtonsVisible = true;
        }

        int primerNum = sev.getPrimerNum();

        Primer p = sev.primer;
        currentSelectedPrimer = p;

        Block block = CodeHopWizard.blockList.get(p.blocknum - 1);

        int primerStart = block.getDisplayStart(p.startNTPosInBlock, 0);  //INCLUSIVE start position

        // +2 causes amino acid in consensus to be bold even when not all nucleotides (of the AA) are present
        int primerEnd = block.getDisplayEnd(p.endNTPosInBlock + 2, 0); //EXCLUSIVE end position

        int displayStart = block.getDisplayStart(p.startNTPosInBlock, 6);  //INCLUSIVE start position
        int displayEnd = block.getDisplayEnd(p.endNTPosInBlock, 6); //EXCLUSIVE end position

        String surroundingConsensus = block.getConsensusChunk(displayStart, displayEnd);

        String displayInfo = formattedStringForDisplay(block, p, primerNum, primerStart, primerEnd, displayStart, displayEnd, surroundingConsensus);

        infoPane.setText(displayInfo);
        infoPane.setCaretPosition(0);
    }


    private static String formattedStringForDisplay(Block block, Primer primer, int primerNum, int primerStart, int primerEnd, int displayStart, int displayEnd, String surroundingConsensus) {

        String coreLen;
        int aaCoreLen = CodeHopSelectPanel.getDegenerateCoreLength();
        int ntCoreLen = aaCoreLen * 3;
        int AAstartPos = primer.startNTPosInSeq / 3 + 1;
        int AAendPos = primer.endNTPosInSeq / 3 + 1;
        String leftEndIndicator;
        String rightEndIndicator;

        if (primer.direction == "forward") {
            ntCoreLen -= 1; // subtract 1 because the last nucleotide is not included
            leftEndIndicator = "5'";
            rightEndIndicator = "3'";
        } else {
            leftEndIndicator = "3'";
            rightEndIndicator = "5' " + primer.primerName;
        }
        coreLen = aaCoreLen + "aa (" + ntCoreLen + "bp)";

        String temp;
        if (CodeHopWizard.properTempCalc) {
            temp = String.valueOf(primer.temp);
        } else {
            temp = "-- ";
        }

        //capitalize first letter of direction
        String direction = primer.direction.substring(0, 1).toUpperCase() + primer.direction.substring(1);

        String fontfamily = "Courier New";

        String display = "<body style=\"font-family:" + fontfamily + "\">";

        display += "<table style=\"padding-left:5px\">" +

                "<tr style=\"font-size:small\">" +
                "<td style=\"font-size:medium\"><b>" + direction + " Primer: " + primer.primerName + "</b> (" + primerNum + "/" + CodeHopWizard.primerCount + ")</td>" +
                "<td><i>Block ID: " + primer.blocknum + "/" + CodeHopWizard.blockList.size() + "</i></b></td>" +
                "<td><i>Align Type: " + CodeHopSelectPanel.getAlignmentTypeForDisplay() + "</i></td>" +
                "</tr>" +

                "<tr style=\"font-size:small\">" +
                "<td><i>Clamp Length: " + primer.clampSeq.length() + "bp</i></td>" +
                "<td><i>Clamp Annealing Temp (C): " + temp + "</i></td>" +
                "<td><i>Clamp Score: " + primer.clampScore + "</i></td>" +
                "</tr>" +

                "<tr style=\"font-size:small\">" +
                "<td><i>Core Length: " + coreLen + "</i></td>" +
                "<td><i>Core Degeneracy: " + primer.degeneracy + "</i></td>" +
                "<td><i>Max Core Degeneracy: " + CodeHopSelectPanel.getDegeneracy() + "</i></td>" +
                "</tr>" +

                "<tr style=\"font-size:small\">" +
                "<td><i>Primer Location: " + AAstartPos + "-" + AAendPos + "aa (" + (primer.startNTPosInSeq + 1) + "-" + primer.endNTPosInSeq + "bp)</i></td>" +
                "<td><i>Minimum AA Freq: " + CodeHopSelectPanel.getminAAFreq() + "%</i></td>" +
                "<td><i>Strictness: " + CodeHopSelectPanel.getStrictness() + "</i></td>" +
                "</tr>" +

                "</table>" +

                "<br>" +

                "<table cellspacing=\"0\" cellpadding=\"0\" style=\"padding-left:5px\">";

        if (primer.direction.equals("reverse")) {
            display += "<tr>" +
                    "<td align=\"right\">" + "5'-" + "</td>" +
                    "<td><pre style=\"font-family:" + fontfamily + "\">" + addSpaces(displayStart, primerStart, primer) + complementPrimer(primer.primerSeq) + "</pre></td>" +
                    "<td>" + "-3'" + "</td>" +
                    "</tr>";
        }

        display +=
                "<tr>" +
                        "<td align=\"right\">" + leftEndIndicator + "-</td>" +
                        "<td><pre style=\"font-family:" + fontfamily + "\">" + addSpaces(displayStart, primerStart, primer) + primer.primerSeq + "</pre></td>" +
                        "<td>-" + rightEndIndicator + "</td>" +
                        "</tr>" +


                        "<tr>" +
                        "<td> </td>" +
                        "<td style=\"font-family:" + fontfamily + "\">" + getConsensusFreqIdentifier(displayStart, displayEnd, block) + "</td>" +
                        "</tr>" +

                        "<tr>" +
                        "<td align=\"right\" style=\"font-size:small\">Consensus: </td>" +
                        "<td><pre style=\"font-family:" + fontfamily + "\">" + getConsensus(primerStart, primerEnd, displayStart, displayEnd, block) + "</pre></td>" +
                        "</tr>" +

                        getBlockSurrounding(displayStart, displayEnd, block, fontfamily) +

                        "</table>" +

                        "</body>";

        return display;
    }

    public static String addSpaces(int displayStart, int primerStart, Primer primer) {
        String spaces = "";
        for (int i = displayStart; i < primerStart; i++) {
            spaces += "   ";
        }
        if (primer.direction.equals("forward")) {
            if (primer.clampSeq.length() % 3 == 1) {
                spaces += "  ";
            } else if (primer.clampSeq.length() % 3 == 2) {
                spaces += " ";
            }
        }
        return spaces;
    }


    public static String getConsensusFreqIdentifier(int displayStart, int displayEnd, Block block) {
        String frequencyIdentifier = "<pre>";

        for (int w = displayStart; w < displayEnd; w++) {
            frequencyIdentifier += block.consensus.get(w).frequencyIdentifier + "  ";
        }
        return frequencyIdentifier + "</pre>";
    }


    public static String getConsensus(int primerStart, int primerEnd, int displayStart, int displayEnd, Block block) {
        String fullConsensus = block.getBlockConsensus();
        String partConsensus = "";

        for (int x = displayStart; x < displayEnd; x++) {
            if (x >= primerStart && x < primerEnd) {
                partConsensus += "<b>" + fullConsensus.charAt(x) + "  </b>";
            } else {
                partConsensus += fullConsensus.charAt(x) + "  ";
            }
        }
        return partConsensus;
    }


    public static String getBlockSurrounding(int displayStart, int displayEnd, Block block, String fontfamily) {
        String consensus = block.getBlockConsensus();
        String blockSurrounding = "";
        String[] seqNames = CodeHopWizard.seqNames;

        for (int i = 0; i < block.block.length; i++) {

            blockSurrounding += "<tr>" +
                    "<td align=\"right\" style=\"font-size:small\">" + seqNames[i] + ": </td>" +
                    "<td><pre style=\"font-family:" + fontfamily + "\">";

            for (int k = displayStart; k < displayEnd; k++) {
                if (block.block[i].charAt(k) == Character.toUpperCase(consensus.charAt(k))) {
                    blockSurrounding += ".  ";
                } else {
                    blockSurrounding += block.block[i].charAt(k) + "  ";
                }
            }
            blockSurrounding += "</pre></td> </tr>";
        }
        return blockSurrounding;
    }

    /*
        Exports the Primer information in an CSV file format
     */
    public static void addExportFunction() {

        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                boolean noPrimersSelected = true;
                for (PrimerCheckBox primerCheckBox : primerCheckBoxes) {
                    if (primerCheckBox.checkBox.isSelected()) {
                        noPrimersSelected = false;
                        break;
                    }
                }
                if (noPrimersSelected) {
                    JOptionPane.showMessageDialog(CodeHopWizard.mainFrame, "No primers have been selected to export. \nCheck the boxes beside the primer names to choose which primers to export.", "No Primers Selected", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JFileChooser fc = new JFileChooser(new File(System.getProperty("user.home")));
                fc.setDialogTitle("Export Primers");
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
                        int ret = JOptionPane.showConfirmDialog(CodeHopWizard.mainFrame, "File \"" + filename + "\" already exists. Overwrite?", "Export Primers", JOptionPane.YES_NO_OPTION);
                        if (ret == 1) {
                            return;
                        }
                    }
                    PrintWriter writer;
                    try {
                        writer = new PrintWriter(fullPathFileName, "UTF-8");
                    } catch (Exception e1) {
                        UITools.showError("Could not write/create file", CodeHopWizard.mainFrame);
                        return;
                    }

                    writer.println("Primer Name, Primer Sequence 5'-3', Direction, Annealing Temperature (C), Primer Length (NT), Clamp Length (NT), Core length (NT(AA)), Degeneracy, Primer Location (AA), Primer Location (NT), Primer AA Sequence, Clamp Score");
                    for (PrimerCheckBox primerCheckBox : primerCheckBoxes) {

                        if (primerCheckBox.checkBox.isSelected()) {

                            Primer p = primerCheckBox.primer;

                            String primerSeq = p.primerSeq;
                            int AAstartPos = p.startNTPosInSeq / 3 + 1;
                            int AAendPos = p.endNTPosInSeq / 3 + 1;
                            String AAseq = CodeHopWizard.getWholeSequenceConsensusNOSPACES.substring(AAstartPos - 1, AAendPos);
                            String temp;

                            if (CodeHopWizard.properTempCalc) {
                                temp = String.valueOf(p.temp);
                            } else {
                                temp = "N/A";
                            }

                            int aaCoreLen = CodeHopSelectPanel.getDegenerateCoreLength();
                            int ntCoreLen = aaCoreLen * 3;

                            if (p.direction == "forward") {
                                ntCoreLen -= 1; // subtract 1 because the last nucleotide is not included
                            } else {
                                //reverse the string
                                primerSeq = new StringBuilder(primerSeq).reverse().toString();
                            }

                            writer.println(p.primerName + ", " + primerSeq + ", " + p.direction + ", " + temp + ", " + p.primerSeq.length() + ", " + p.clampSeq.length() + ", " + ntCoreLen + "(" + aaCoreLen + ")" + ", " + p.degeneracy + ", " + AAstartPos + "-" + AAendPos + ", " + +(p.startNTPosInSeq + 1) + "-" + p.endNTPosInSeq + ", " + AAseq + ", " + p.clampScore);
                        }
                    }

                    String clampLenUserSet;
                    String clampLenByTemp;

                    if (CodeHopSelectPanel.setClampByTemp.isSelected()) {
                        clampLenByTemp = String.valueOf(CodeHopSelectPanel.getTemp());
                        clampLenUserSet = "N/A";
                    } else {
                        clampLenByTemp = "N/A";
                        clampLenUserSet = String.valueOf(CodeHopSelectPanel.getUserDefinedConsensusClampLength());
                    }

                    String threePrimeNT;
                    if (CodeHopSelectPanel.invariantLastPosinput.isSelected()) {
                        threePrimeNT = "Invariant";
                    } else {
                        threePrimeNT = "Use core strictness";
                    }


                    writer.println(" ");
                    writer.println("Primer Design Variables");
                    writer.println("Block making alignment tool" + ", " + "Codon table" + ", " + "Clamp length user set" + ", " + "Clamp length by temp" + ", " + "Max degeneracy" + ", " + "Core length" + ", " + "Strictness" + ", " + "Min AA conservation" + ", " + "3' nucleotide" + ", " + "Min block length" + ", " + "Primer concentration" + ", " + "Restrict 3' nucleotide to G or C" + ", " + "Exclude Leu Ser and Arg from 3' region");
                    writer.println(CodeHopSelectPanel.getAlignmentTypeForDisplay() + ", " + CodonTable.getName() + ", " + clampLenUserSet + ", " + clampLenByTemp + ", " + CodeHopSelectPanel.getDegeneracy() + ", " + CodeHopSelectPanel.getDegenerateCoreLength() + ", " + CodeHopSelectPanel.getStrictness() + ", " + CodeHopSelectPanel.getminAAFreq() + ", " + threePrimeNT + ", " + CodeHopSelectPanel.getMinBlockLength() + ", " + CodeHopSelectPanel.getPrimerConcentration() + ", " + CodeHopSelectPanel.GorCinLastPosinput.isSelected() + ", " + CodeHopSelectPanel.excludeLSandAinput.isSelected());

                    writer.close();
                    UITools.showInfoMessage("File successfully saved. \nPrimer information and design variables can be viewed in a spreadsheet application.", CodeHopWizard.mainFrame);

                }
            }
        });
    }

    public static String complementPrimer(String seq) {
        String out = "";
        for (int i = 0; i < seq.length(); i++) {
            switch (seq.charAt(i)) {
                case 'a':
                    out += 't';
                    break;
                case 'c':
                    out += 'g';
                    break;
                case 'g':
                    out += 'c';
                    break;
                case 't':
                    out += 'a';
                    break;
                case 'r':
                    out += 'y';
                    break;
                case 'y':
                    out += 'r';
                    break;
                case 's':
                    out += 's';
                    break;
                case 'w':
                    out += 'w';
                    break;
                case 'k':
                    out += 'm';
                    break;
                case 'm':
                    out += 'k';
                    break;
                case 'b':
                    out += 'v';
                    break;
                case 'd':
                    out += 'h';
                    break;
                case 'h':
                    out += 'd';
                    break;
                case 'v':
                    out += 'b';
                    break;
                case 'n':
                    out += 'n';
                    break;
                case '-':
                    out += '-';
                    break;
                case 'A':
                    out += 'T';
                    break;
                case 'C':
                    out += 'G';
                    break;
                case 'T':
                    out += 'A';
                    break;
                case 'G':
                    out += 'C';
                    break;
            }
        }
        return out;
    }
}