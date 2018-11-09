package ca.virology.baseByBase.gui.CodeHop;

import ca.virology.baseByBase.gui.PrimaryPanel;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.util.gui.UITools;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;


//=============================================================================================================================
//
// Main frame of the codehop wizard. Contains the outer layout incl the buttons, the rat and the codehop title. The inside portion
// cycles between 3 panels:
//      |--CodeHopwizard--------|
//      | -codehopSelectPanel   |
//      | -codehopLoadPanel     |
//      | -codehopResultsPanel  |
//      |_______________________|
//
//=============================================================================================================================

public class CodeHopWizard extends JFrame implements ActionListener {

    static boolean properTempCalc;
    PrimaryPanel primaryPanel;
    private static CodeHopWizard instance = null;
    static CodeHopWizard mainFrame;
    JButton help, cancel, compute, newAnalysis;
    JPanel cards, southPanel, northPanel;
    CodeHopSelectPanel selectPanel;
    CodeHopLoadPanel loadPanel;
    CodeHopResultsPanel resultsPanel;
    CardLayout cardLayout;
    private SwingWorker<ArrayList<Block>, Integer[]> swingSlaveLabourer;
    public static ArrayList<Block> blockList;
    public static int minSeqLen;
    public static String wholeSequenceConsensus;
    public static String getWholeSequenceConsensusNOSPACES;
    public static int primerCount;
    public static String[] seqNames;

    static int smallWindowWidth;
    static int smallWindowHeight;
    static int largeWindowWidth;
    static int largeWindowHeight;


    //=========================================================================
    // Constructor: CodeHopWizard(PrimaryPanel)
    //
    // Initialization of main JFrame for CodeHopWizard
    //=========================================================================
    private CodeHopWizard(PrimaryPanel p) {
        smallWindowWidth = 900;
        smallWindowHeight = 530;
        largeWindowWidth = 1200;
        largeWindowHeight = 900;
        setSize(smallWindowWidth, smallWindowHeight);
        setLocationRelativeTo(null);
        setTitle("COnsensus-DEgenerate Hybrid Oligonucleotide Primers");
        setLayout(new BorderLayout());
        mainFrame = this;
        primaryPanel = p;
        initNorthPanel();
        initMainPanel();
        initSouthPanel();

        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                instance = null; //reset instance so a new CodeHopWizard can be opened
                blockList = null;
                mainFrame = null;
            }
        });


        // download the dynamic library from the http://206.12.59.223/files/codehop/filename
        // save the file to a temporary folder
        // load the native library from the temporary folder
        String OSname = System.getProperty("os.name");
        String fileName;

        if (OSname.startsWith("Mac") || OSname.startsWith("mac")) {

            fileName = "libhyfiModel.so";

            String tempDir = System.getProperty("java.io.tmpdir");
            String urlStr = "https://4virology.net/files/codehop/" + fileName;

            try {
                if (!new File(tempDir + fileName).exists()) { //if the file does not already exist
                    URL website = new URL(urlStr);
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    FileOutputStream fos = new FileOutputStream(tempDir + fileName);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
                System.load(tempDir + fileName);
            } catch (java.net.MalformedURLException e1) {
                System.err.println("Failed to read file from " + urlStr + "\n" + e1);
                tempCalcErrMsg(1);
            } catch (java.io.IOException e2) {
                System.err.println("Failed to read file from " + urlStr + "\n" + e2);
                tempCalcErrMsg(1);
            } catch (UnsatisfiedLinkError e3) {
                System.err.println("Native code library failed to load\n" + e3);
                tempCalcErrMsg(2);
            }
        } else {
            tempCalcErrMsg(2);
        }
    }

    public void tempCalcErrMsg(int err) {

        String errMsg = "";

        if (err == 1) {
            errMsg = "<html>Failed to communicate with server when downloading file for temperature calculation.<br>" +
                    "Please set consensus clamp length manually instead.</html>";
        }
        if (err == 2) {
            errMsg = "<html>Temperature calculations requires Mac OSX 64-bit.<br>" +
                    "Please set consensus clamp length manually instead.</html>";
        }

        JOptionPane.showMessageDialog(this, errMsg, "Warning", JOptionPane.WARNING_MESSAGE);
        properTempCalc = false;
        CodeHopSelectPanel.setClamplen.doClick();
        CodeHopSelectPanel.tempinput.setEditable(false);
        CodeHopSelectPanel.setClampByTemp.setEnabled(false);
    }

    //=========================================================================
    // Method: getInstance(PrimaryPanel)
    //
    // Must be called to create a new CodeHopWizard instance
    // Only allows for one instance of CodeHopWizard to be created (Singleton)
    //=========================================================================
    public static CodeHopWizard getInstance(PrimaryPanel primaryPanel, Image ic) {
        if (instance == null) {
            instance = new CodeHopWizard(primaryPanel);
            instance.setIconImage(ic);
        } else {
            mainFrame.toFront();
        }
        return instance;
    }


    //=========================================================================
    // Method: getInstance()
    //
    // Called by CodeHopSelectPanel to set parent as CodeHopWizard
    //=========================================================================
    public static CodeHopWizard getInstance() {
        return instance;
    }


    //=========================================================================
    // Method: initNorthPanel()
    //
    // Initializes NorthPanel which includes CODEHOP title and image
    // Stays static as main screens change
    //=========================================================================
    public void initNorthPanel() {
        northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());

        addImage("images/chtree4.png", BorderLayout.WEST, 167, 80);
        addImage("images/kr2.png", BorderLayout.EAST, 110, 56);

        mainFrame.add(northPanel, BorderLayout.NORTH);
    }

    public void addImage(String imagePath, String position, int width, int height) {
        ClassLoader cl = getClass().getClassLoader();
        try {
            java.net.URL imgURL = cl.getResource(imagePath);
            BufferedImage bi = ImageIO.read(imgURL);
            ImageIcon iconimage = new ImageIcon(bi);
            Image image = iconimage.getImage();
            Image newImg = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(newImg));
            northPanel.add(imageLabel, position);
        } catch (Exception e) {
            System.out.println("Error loading image " + imagePath);
        }
    }


    //=========================================================================
    // Method: initMainPanel()
    //
    // Initializes MainPanel which includes the cardLayout used to switch between main screens
    //=========================================================================

    public void initMainPanel() {
        selectPanel = new CodeHopSelectPanel();
        loadPanel = new CodeHopLoadPanel();
        resultsPanel = new CodeHopResultsPanel();
        cards = new JPanel(new CardLayout());

        cards.add(selectPanel, "first card");
        cards.add(loadPanel, "second card");
        cards.add(resultsPanel, "third card");

        cardLayout = new CardLayout();
        cardLayout = (CardLayout) cards.getLayout();

        mainFrame.add(cards, BorderLayout.CENTER);
    }


    //=========================================================================
    // Method: initSouthPanel()
    //
    // Initializes SouthPanel which includes the buttons at the bottom of the window
    //=========================================================================
    public void initSouthPanel() {
        southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(1, 5, 5, 5));

        help = new JButton("Help");
        help.addActionListener(mainFrame);

        compute = new JButton("Compute");
        compute.addActionListener(mainFrame);

        cancel = new JButton("Cancel");
        cancel.addActionListener(mainFrame);

        newAnalysis = new JButton("New Analysis");
        newAnalysis.addActionListener(mainFrame);

        southPanel.add(help);
        southPanel.add(new JLabel(" "));
        southPanel.add(new JLabel(" "));
        southPanel.add(new JLabel(" "));
        southPanel.add(compute);
        mainFrame.add(southPanel, BorderLayout.SOUTH);
    }


    //=========================================================================
    // Method: actionPerformed()
    //
    // Catches all ActionEvents (button clicks) in CodeHopWizard
    //=========================================================================
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == compute) {
            if (!CodeHopSelectPanel.verifyInputs()) {
                UITools.showInfoMessage("Please ensure all your inputs are in the valid range!", this);
                return;
            }
            showLoadCard();
            processResults();
            SwingUtilities.updateComponentTreeUI(mainFrame);
        } else if (e.getSource() == newAnalysis) {
            resultsPanel.reset();
            setSize(smallWindowWidth, smallWindowHeight);
            setLocationRelativeTo(null);
            showSelectCard();
            SwingUtilities.updateComponentTreeUI(mainFrame); //reset view
        } else if (e.getSource() == help) {
            ImageIcon icon = null;
            ImageIcon newIcon = null;
            try {
                java.net.URL imgURL = null;
                ClassLoader cl = getClass().getClassLoader();
                imgURL = cl.getResource("images/kr3.png");
                BufferedImage bi = ImageIO.read(imgURL);
                icon = new ImageIcon(bi);
                Image image = icon.getImage();
                Image newImg = image.getScaledInstance(105, 79, Image.SCALE_SMOOTH);
                newIcon = new ImageIcon(newImg);


            } catch (Exception ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, getHelpPanel(), "j-CODEHOP Help", JOptionPane.OK_OPTION, newIcon);
        }
    }

    private JPanel getHelpPanel() {
        JPanel dialog = new JPanel();
        dialog.setLayout(new BorderLayout());
        String helpString = "<html>Welcome to j-CODEHOP!<br><br>" +
                "An interactive tool for designing CODEHOPs from conserved blocks of amino acids within multiply-aligned protein sequences.<br>" +
                "Each CODEHOP consists of a 3' degenerate core and a 5' consensus sequence.<br><br>" +

                "Each 3’ degenerate core is generated from 3-5 highly conserved amino acids of the alignment, which are decoded to their<br>" +
                "respective codons, creating a sequence of nucleotides represented using the IUPAC nucleotide codes to display multiple<br>" +
                "nucleotide possibilities within a single position.<br><br>" +

                "The 5’ consensus clamp is created by taking consensus nucleotides neighbouring the degenerate core and adding them to the<br>" +
                "primer in the correct direction to increase the length of the the primer without increasing degeneracy.<br><br>" +

                "CODEHOPs can be used to predict PCR primers for amplification of distantly related gene sequences from varying families, genera,<br> strains, etc. <br><br>" +
                "The tool uses amino acid sequences from BaseByBase as input. For additional help on the parameters, please go to the CODEHOP<br></html>";

        JLabel helpLabel = new JLabel(helpString);

        String link = "<html><a href=\"http://blocks.fhcrc.org/blocks/help/CODEHOP/CODEHOP_help.html\">website.</a></html>";

        JLabel linklabel = new JLabel(link);
        linklabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linklabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        URI ur = new URI("http://blocks.fhcrc.org/blocks/help/CODEHOP/CODEHOP_help.html");
                        desktop.browse(ur);
                    } catch (Exception t) {
                        t.printStackTrace();
                    }
                }

            }
        });

        String morePrimersHelp = "<html><br><br>Not enough primers being created? Here are a few things you can try." +
                "<ul>" +
                "<li>Increase max degeneracy</li>" +
                "<li>Decrease strictness</li>" +
                "<li>Decrease min AA conservation in core region" +
                "<li>Use core strictness for 3' NT (advanced options)</li>" +
                "</ul><br>" +

                "Try changing one parameter at a time. More primers may be produced than expected.<br>" +
                "If too many primers are being created, try doing the opposite of some of the above ideas." +
                "</html>";

        JLabel morePrimersLabel = new JLabel(morePrimersHelp);
        dialog.add(helpLabel, BorderLayout.NORTH);
        dialog.add(linklabel, BorderLayout.CENTER);
        dialog.add(morePrimersLabel, BorderLayout.SOUTH);


        return dialog;
    }


    //=========================================================================
    // Method: processResults()
    //
    // Called when Process Results button is clicked
    //
    // Performs main functions of CODHEOP:
    //      1) align sequences
    //      2) generate blocks
    //      3) generate matrix
    //      4) generate consensus clamp
    //      5) generate cores
    //      6) generate primers
    //=========================================================================
    public void processResults() {
        blockList = new ArrayList<Block>();
        primerCount = 0;

        swingSlaveLabourer = new SwingWorker<ArrayList<Block>, Integer[]>() {

            @Override //background Thread
            protected ArrayList<Block> doInBackground() throws Exception {

                System.out.println("beginning heavy work..");

                Integer[] status = new Integer[2];
                status[1] = 1;
                status[0] = 0;
                this.publish(status);


                //align regions (indeterminate progress bar)
                String alignToDo = CodeHopSelectPanel.getAlignmentType();
                if (!alignToDo.equals("noAlign")) {
                    boolean al = primaryPanel.alignSequencesForCodehop(alignToDo);
                    System.out.println("");
                    if (al == false) {
                        showSelectCard();
                        UITools.showWarning("Error retrieving alignment, if problem persists select \"do not align\"", CodeHopWizard.getInstance());
                        return blockList;
                    }
                    System.out.println("Done align.");
                }

                //==================================================================


                // generate blocks
                status[1] = 2;
                status[0] = 0;
                this.publish(status);
                generateBlocks();
                int blocknum = blockList.size();
                System.out.println("Done generating blocks [" + blocknum + "]");
                if (blocknum == 0) {
                    UITools.showWarning("Alignment did not produce any blocks!\nUsing another alignment tool may fix this.", CodeHopWizard.getInstance());
                    return blockList;
                }

                //===================================================================

                computeWholeSequenceConsensus(); //used for display in the CodeHopResultsPanel - not part of the main algorithm

                //==================================================================

                int pctwork = 100 / blocknum;

                //make pssm matrix (change to determinate progress bar)
                status[1] = 3;
                status[0] = 0;
                this.publish(status);

                for (int i = 0; i < blocknum; i++) {
                    blockList.get(i).generateMatrix();
                    status[0] += pctwork;
                    this.publish(status);
                    sleepy(20);
                }

                //==================================================================

                //generate consensus clamp
                status[1] = 4;
                status[0] = 0;
                this.publish(status);

                for (int k = 0; k < blocknum; k++) {
                    blockList.get(k).generateEntireConsensusClamp();
                    status[0] += pctwork;
                    this.publish(status);
                    sleepy(30);
                }


                //==================================================================

                //generate cores
                status[1] = 5;
                status[0] = 0;
                this.publish(status);

                for (int j = 0; j < blocknum; j++) {
                    blockList.get(j).generateCores();
                    status[0] += pctwork;
                    this.publish(status);
                    sleepy(40);
                }


                //==================================================================

                //generate primers
                status[1] = 6;
                status[0] = 0;
                this.publish(status);

                for (int j = 0; j < blocknum; j++) {
                    blockList.get(j).generatePrimers(j + 1);
                    status[0] += pctwork;
                    this.publish(status);
                }


                //==================================================================

                //prepare results pane
                status[1] = 7;
                status[0] = 0;
                this.publish(status);
                resultsPanel.showResults();
                setSize(largeWindowWidth, largeWindowHeight);
                setLocationRelativeTo(null);

                //==================================================================

                //printInfoForTesting();

                return blockList;
            }


            //=========================================================================
            // Method: process(List<Integer[]>)
            //
            // Updates the status text
            //=========================================================================
            @Override //EDT thread
            protected void process(List<Integer[]> chunks) {
                if (chunks.get(0)[1] == 1) {
                    CodeHopLoadPanel.processingText.setText("Aligning with " + CodeHopSelectPanel.getAlignmentTypeForDisplay() + "...");
                } else if (chunks.get(0)[1] == 2) {
                    CodeHopLoadPanel.processingText.setText("Generating blocks...");
                } else if (chunks.get(0)[1] == 3) {
                    CodeHopLoadPanel.processingText.setText("Generating PSSM matrix for blocks...");
                    CodeHopLoadPanel.progressBar.setIndeterminate(false);
                    CodeHopLoadPanel.progressBar.setMinimum(0);
                    CodeHopLoadPanel.progressBar.setMaximum(100);
                    CodeHopLoadPanel.progressBar.setValue(chunks.get(0)[0]);
                } else if (chunks.get(0)[1] == 4) {
                    CodeHopLoadPanel.processingText.setText("Generating Consensus Clamp...");
                    CodeHopLoadPanel.progressBar.setValue(chunks.get(0)[0]);
                } else if (chunks.get(0)[1] == 5) {
                    CodeHopLoadPanel.processingText.setText("Generating Degenerate Cores...");
                    CodeHopLoadPanel.progressBar.setValue(chunks.get(0)[0]);
                } else if (chunks.get(0)[1] == 6) {
                    CodeHopLoadPanel.processingText.setText("Generating Primers...");
                    CodeHopLoadPanel.progressBar.setValue(chunks.get(0)[0]);
                } else if (chunks.get(0)[1] == 7) {
                    CodeHopLoadPanel.progressBar.setIndeterminate(true);
                    CodeHopLoadPanel.processingText.setText("Preparing results...");
                    CodeHopLoadPanel.progressBar.setValue(chunks.get(0)[0]);
                }
            }


            //=========================================================================
            // Method: done()
            //
            // Updates the view when results have finished processing
            //=========================================================================
            @Override //same EDT thread
            protected void done() {
                showResultsCard();
                SwingUtilities.updateComponentTreeUI(mainFrame); //reset view
            }
        };
        swingSlaveLabourer.execute();
    }


    //=========================================================================
    // Method: generateBlocks()
    //
    // Generates blocks using the aligned sequences
    // need to add conditions to test whether a block is conserved enough (minimum similarity in columns)
    //=========================================================================
    public void generateBlocks() {
        FeaturedSequence[] seqs = primaryPanel.getAllSequences(); //amino acid

        storeSeqNames(seqs);

        if (seqs.length < 2) {
            // no sequences to create blocks from... not sure if this can happen
            return;
        }

        // length of the shortest sequence
        minSeqLen = getMinLen(seqs);

        int blocklen = 0;
        for (int col = 0; col < minSeqLen; col++) {

            for (int row = 0; row < seqs.length; row++) {

                // if a gap is found or at the end of the sequence, stop iterating a attempt to create a block
                if (seqs[row].charAt(col) == '-' || col == minSeqLen - 1) {
                    String[] block;
                    if (col == minSeqLen - 1 && !colContainsDash(seqs, col) && blocklen + 1 >= CodeHopSelectPanel.getMinBlockLength()) { //at the end of the shortest sequence and column is valid (no dashes)
                        block = extractBlock(col - blocklen, col + 1, seqs);
                    } else if (blocklen < CodeHopSelectPanel.getMinBlockLength()) {
                        blocklen = -1; //to account for the blocklen being incremented after inner loop breaks;
                        break; //don't create a new block
                    } else {
                        block = extractBlock(col - blocklen, col, seqs);
                    }

                    blockList.add(new Block(block, col - blocklen));
                    blocklen = -1; //to account for the blocklen being incremented after inner loop breaks;
                    break;
                }
            }
            blocklen++;
        }
    }

    public String[] extractBlock(int start, int end, FeaturedSequence[] seqs) {
        int numSeqs = seqs.length;
        String[] block = new String[numSeqs];

        //loop through sequences and perform substring to retrieve block
        for (int seq = 0; seq < numSeqs; seq++) {
            block[seq] = seqs[seq].substring(start, end);
        }
        return block;
    }

    public boolean colContainsDash(FeaturedSequence[] seqs, int col) {
        for (int row = 0; row < seqs.length; row++) {
            if (seqs[row].charAt(col) == '-') {
                return true;
            }
        }
        return false;
    }


    //=========================================================================
    // Method: getMinLenSeq()
    //
    // Returns:  length of the shortest FeaturedSequence
    //=========================================================================
    public int getMinLen(FeaturedSequence[] seqs) {
        int minSeqLen = seqs[0].length();
        for (int seq = 1; seq < seqs.length; seq++) {
            if (seqs[seq].length() < minSeqLen) {
                minSeqLen = seqs[seq].length();
            }
        }
        return minSeqLen;
    }

    private void showResultsCard() {
        cardLayout.show(cards, "third card");
        southPanel.removeAll();
        southPanel.add(help);
        southPanel.add(new JLabel(" "));
        southPanel.add(new JLabel(" "));
        southPanel.add(new JLabel(" "));
        southPanel.add(newAnalysis);
    }

    private void showLoadCard() {
        loadPanel.addComponents();
        cardLayout.show(cards, "second card");
        southPanel.removeAll();
    }

    private void showSelectCard() {
        cardLayout.show(cards, "first card");
        southPanel.removeAll();
        southPanel.add(help);
        southPanel.add(new JLabel(" "));
        southPanel.add(new JLabel(" "));
        southPanel.add(new JLabel(" "));
        southPanel.add(compute);
    }

    private void sleepy(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            System.out.println("shit");
        }
    }

    private void computeWholeSequenceConsensus() {
        String nospace = "";
        wholeSequenceConsensus = "";
        int blockCounter = 0;
        for (int i = 0; i < minSeqLen && blockCounter < blockList.size(); i++) {
            Block currentBlock = blockList.get(blockCounter);
            if (i == currentBlock.blockAAStartPosition) {
                nospace += currentBlock.getBlockConsensus();
                i += currentBlock.getBlockConsensus().length() - 1;
                blockCounter += 1;
            } else {
                nospace += "-";
            }
        }
        getWholeSequenceConsensusNOSPACES = nospace;
        for (int i = 0; i < nospace.length(); i++) {
            if (nospace.charAt(i) == '-') {
                wholeSequenceConsensus += "---";
            } else {
                wholeSequenceConsensus += " " + nospace.charAt(i) + " ";
            }
        }
    }


    private void storeSeqNames(FeaturedSequence[] seqs) {
        seqNames = new String[seqs.length];
        for (int i = 0; i < seqs.length; i++) {
            seqNames[i] = seqs[i].getName();
        }
    }


    /*
    This method looks through all the primers, and returns the name of the primer
    whose start position is "startLoc".
    position is in nucleotides
    startLoc-1 because it has been adjusted for the display.
     */
    public static String getPrimerName(int startLoc, boolean lead) {

        for (int i = 0; i < blockList.size(); i++) { //for block in blocklist

            if (lead) {
                for (int k = 0; k < blockList.get(i).reversePrimerList.size(); k++) { //for reverse primer in block
                    Primer h = blockList.get(i).reversePrimerList.get(k);
                    if (h.startNTPosInSeq == startLoc - 1) {
                        return h.primerName;
                    }
                }
            } else {
                for (int j = 0; j < blockList.get(i).forwardPrimerList.size(); j++) { //for forward primers in block
                    Primer p = blockList.get(i).forwardPrimerList.get(j);
                    if (p.startNTPosInSeq == startLoc - 1) {
                        return p.primerName;
                    }
                }
            }

        }
        return "no name";
    }


    //=========================================================================
    // TESTING CODE  - prints blocks and options for other info
    //=========================================================================
    public void printInfoForTesting() {

        boolean printForwardCores = true;
        boolean printReverseCores = true;
        boolean printForwardConsensusClamp = true;
        boolean printReverseConsensusClamp = true;
        boolean printForwardPrimers = true;
        boolean printReversePrimers = true;

        for (int i = 0; i < blockList.size(); i++) {
            Block block = blockList.get(i);
            String[] blockArr = block.block;
            int blocknumber = i + 1;
            System.out.println("BLOCK " + blocknumber + "  startPos = " + block.blockAAStartPosition);
            // print block
            for (int k = 0; k < blockArr.length; k++) {
                System.out.println("block:     " + blockArr[k]);
            }

            //print consensus
            System.out.printf("Consensus: ");
            for (int j = 0; j < block.consensus.size(); j++) {
                System.out.printf("%c", block.consensus.get(j).aminoAcid);
            }
            System.out.println(" \n");


            if (printForwardConsensusClamp) {
                System.out.println("Forward Consensus Clamp:(NT) ");
                System.out.println(block.forwardConsensusClamp);
                System.out.println(" ");
            }


            if (printReverseConsensusClamp) {
                System.out.println("Reverse Consensus Clamp:(NT) ");
                System.out.println(block.reverseConsensusClamp);
                System.out.println(" ");
            }


            if (printForwardCores) {
                System.out.println("Forward Cores:(NT) ");
                for (DegenerateCore core : block.forwardCoreList) {
                    System.out.println(core.core + ", " + core.degeneracy + ", " + core.startNTPos);
                }
                System.out.println(" ");
            }


            if (printReverseCores) {
                System.out.println("Reverse Cores:(NT) ");
                for (DegenerateCore core : block.reverseCoreList) {
                    System.out.println(core.core + ", " + core.degeneracy + ", " + core.startNTPos);
                }
                System.out.println(" ");
            }


            if (printForwardPrimers) {
                System.out.println(block.forwardPrimerList.size() + " Forward Primers:");
                for (Primer primer : block.forwardPrimerList) {
                    System.out.println(primer.primerSeq + "    start position:(NT) " + primer.startNTPosInSeq + " primer name: " + primer.primerName);
                }
                System.out.println(" ");
            }

            if (printReversePrimers) {
                System.out.println(block.reversePrimerList.size() + " Reverse Primers:");
                for (Primer primer : block.reversePrimerList) {
                    System.out.println(primer.primerSeq + "    start position:(NT)  " + primer.startNTPosInSeq + " primer name: " + primer.primerName);
                }
                System.out.println(" ");
            }
            System.out.println("\n\n");
        }
    }
}