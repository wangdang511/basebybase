package ca.virology.baseByBase.gui.CodeHop;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.text.NumberFormat;

/*
First panel to appear after opening codehop. Has all the input. Also allows for custom codon table which
opens a new window (CustomCodonTable)
 */
public class CodeHopSelectPanel extends JPanel {

    JLabel table;
    JLabel strict;
    JLabel degen;
    JLabel blockLengthlabl;
    JLabel title;
    JLabel alignType;
    JLabel coreLength;
    JLabel minAAFreqLabl;
    JLabel clamplen;
    JLabel coreLabel;
    JLabel clampLabel;
    static JLabel fileNameDisplay;

    static JRadioButton muscle;
    static JRadioButton mafft;
    static JRadioButton clustal;
    static JRadioButton noAlign;
    static JRadioButton setClampByTemp;
    static JRadioButton setClamplen;

    static JComboBox selectCodonTable;

    static JFormattedTextField tempinput;
    static JFormattedTextField strictinput;
    static JFormattedTextField degeninput;
    static JFormattedTextField coreLeninput;
    static JFormattedTextField blockLeninput;
    static JFormattedTextField minAAFreqinput;
    static JFormattedTextField manualClampLeninput;


    // advanced options
    JLabel lastPos;
    JLabel GorCinLastPos;
    JLabel excludeLSandA;
    JLabel primerConcentration;

    static JRadioButton invariantLastPosinput;
    static JRadioButton coreStrictinput;
    static JCheckBox GorCinLastPosinput;
    static JCheckBox excludeLSandAinput;
    static JFormattedTextField primerConcentrationinput;


    JPanel me = this;
    public static CodonTable codonTable;
    String[] codonDropDownLabels;
    String previousCodonTable;


    CustomCodonTable customCodonTable;
    JDialog codonTableDialog;
    static JButton[] codonTableButtons = {
            new JButton("OK"),
            new JButton("Export Table"),
            new JButton("Cancel")
    };


    public CodeHopSelectPanel() {
        setLayout(new BorderLayout());
        JPanel gbl = new JPanel(new GridBagLayout());
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc1.insets = new Insets(5, 5, 5, 5);
        Insets originalInsets = gbc1.insets;

        codonTable = new CodonTable();
        customCodonTable = new CustomCodonTable(codonTable);

        NumberFormat nf = NumberFormat.getInstance();

        muscle = new JRadioButton("Muscle");
        mafft = new JRadioButton("Mafft");
        clustal = new JRadioButton("Clustal");
        noAlign = new JRadioButton("Do Not Align");

        codonDropDownLabels = new String[]{"Homo sapiens", "Mus musculus", "Cowpox virus", "Enterobacteria phage ES18", "Enterobacteria phage T4", "Escherichia coli O157", "Custom..", "Import Custom.."};
        previousCodonTable = codonDropDownLabels[0];
        //selectCodonTable = new JComboBox(codonDropDownLabels);

        selectCodonTable = new JComboBox(new Object[]{
                codonDropDownLabels[0],
                codonDropDownLabels[1],
                codonDropDownLabels[2],
                codonDropDownLabels[3],
                codonDropDownLabels[4],
                codonDropDownLabels[5],
                new JSeparator(JSeparator.HORIZONTAL),
                codonDropDownLabels[6],
                codonDropDownLabels[7]
        });


        selectCodonTable.setRenderer(new SeparatorComboBoxRenderer());
        selectCodonTable.addActionListener(new SeparatorComboBoxListener(selectCodonTable));


        fileNameDisplay = new JLabel("");
        setClampByTemp = new JRadioButton("Set by temp (40-100\u00b0C):");
        setClamplen = new JRadioButton("User set (5-50bp):");

        manualClampLeninput = createNewInputField(nf, 5, 50, "25", 3);
        tempinput = createNewInputField(nf, 40, 100, "60", 3);
        degeninput = createNewInputField(nf, 0, 2000, "16", 5);
        strictinput = createNewInputField(nf, 0, 100, "0", 5);
        coreLeninput = createNewInputField(nf, 3, 5, "4", 5);
        minAAFreqinput = createNewInputField(nf, 0, 100, "80", 5);

        title = new JLabel(" ");

        alignType = new JLabel("Block making alignment tool:");
        table = new JLabel("Codon table:");

        clampLabel = new JLabel("Clamp (non-degenerate 5' region)");
        clamplen = new JLabel(addSpaces() + "Length:");

        coreLabel = new JLabel("Core (degenerate 3' region)");
        degen = new JLabel(addSpaces() + "Max degeneracy (0-2000):");
        coreLength = new JLabel(addSpaces() + "Length (3-5aa):");
        strict = new JLabel(addSpaces() + "Strictness (%):");
        minAAFreqLabl = new JLabel(addSpaces() + "Min AA conservation (%):");


        // more options dialog
        blockLengthlabl = new JLabel("Min block length (5-100aa):");
        lastPos = new JLabel("3' nucleotide:");
        GorCinLastPos = new JLabel("Restrict 3' nucleotide to G or C");
        excludeLSandA = new JLabel("Exclude Leu, Ser, and Arg from 3' region");
        primerConcentration = new JLabel("Primer concentration (nM):");

        blockLeninput = createNewInputField(nf, 5, 100, "5", 5);
        invariantLastPosinput = new JRadioButton("Invariant 3' NT");
        coreStrictinput = new JRadioButton("Use core strictness for 3' NT");
        GorCinLastPosinput = new JCheckBox();
        excludeLSandAinput = new JCheckBox();
        primerConcentrationinput = createNewInputField(nf, 0, 100, "50", 5);

        invariantLastPosinput.doClick();
        ButtonGroup bg = new ButtonGroup();
        bg.add(invariantLastPosinput);
        bg.add(coreStrictinput);

        JButton advancedOptions = new JButton("Advanced Options");


        JButton codonTableHelp = new JButton();


        // add image
        try {
            ImageIcon icon = null;
            ImageIcon smallIcon = null;

            java.net.URL imgURL2 = null;
            ClassLoader cl2 = getClass().getClassLoader();
            imgURL2 = cl2.getResource("images/circle_question_mark.png");
            BufferedImage bi3 = ImageIO.read(imgURL2);
            icon = new ImageIcon(bi3);
            Image img = icon.getImage();
            Image newimg = img.getScaledInstance(15, 15, java.awt.Image.SCALE_SMOOTH);
            smallIcon = new ImageIcon(newimg);
            codonTableHelp.setIcon(smallIcon);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        noAlign.doClick();

        bg = new ButtonGroup();
        bg.add(muscle);
        bg.add(clustal);
        //bg.add(mafft);
        bg.add(noAlign);

        bg = new ButtonGroup();
        bg.add(setClampByTemp);
        bg.add(setClamplen);

        //=============================

        gbc1.gridy = 0;
        gbc1.gridx = 0;
        gbl.add(title, gbc1);

        //=============================

        gbc1.gridy = 1;
        gbc1.gridx = 0;
        gbl.add(alignType, gbc1);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(clustal);
        p.add(muscle);

        gbc1.insets = new Insets(0, 0, 0, 0);
        gbc1.gridy = 1;
        gbc1.gridx = 1;
        gbl.add(p, gbc1);

        gbc1.insets = originalInsets;
        gbc1.gridy = 1;
        gbc1.gridx = 2;
        gbl.add(noAlign, gbc1);

        gbc1.gridy = 1;
        gbc1.gridx = 3;
        //gbl.add(noAlign, gbc1);

        //=============================

        p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(codonTableHelp);
        p.add(fileNameDisplay);
        p.setBorder(new EmptyBorder(0, 0, 0, 0));

        gbc1.gridy = 2;
        gbc1.gridx = 0;
        gbl.add(table, gbc1);

        gbc1.gridwidth = 2;
        gbc1.gridy = 2;
        gbc1.gridx = 1;
        gbl.add(selectCodonTable, gbc1);

        gbc1.insets = new Insets(0, 0, 0, 0);
        gbc1.gridwidth = 1;
        gbc1.gridy = 2;
        gbc1.gridx = 3;
        gbl.add(p, gbc1);
        gbc1.insets = originalInsets;

        //=============================

        clampLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        gbc1.gridy = 4;
        gbc1.gridx = 0;
        gbl.add(clampLabel, gbc1);

        //=============================

        gbc1.gridy = 5;
        gbc1.gridx = 0;
        gbl.add(clamplen, gbc1);

        gbc1.gridy = 5;
        gbc1.gridx = 1;
        gbl.add(setClamplen, gbc1);

        gbc1.gridy = 5;
        gbc1.gridx = 2;
        gbl.add(manualClampLeninput, gbc1);

        gbc1.gridy = 5;
        gbc1.gridx = 3;
        gbl.add(setClampByTemp, gbc1);

        gbc1.gridy = 5;
        gbc1.gridx = 4;
        gbl.add(tempinput, gbc1);

        //=============================

        coreLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        gbc1.gridy = 7;
        gbc1.gridx = 0;
        gbl.add(coreLabel, gbc1);

        //=============================

        gbc1.gridy = 8;
        gbc1.gridx = 0;
        gbl.add(degen, gbc1);

        gbc1.gridy = 8;
        gbc1.gridx = 1;
        gbl.add(degeninput, gbc1);

        //=============================

        gbc1.gridy = 9;
        gbc1.gridx = 0;
        gbl.add(coreLength, gbc1);

        gbc1.gridy = 9;
        gbc1.gridx = 1;
        gbl.add(coreLeninput, gbc1);

        //=============================

        gbc1.gridy = 10;
        gbc1.gridx = 0;
        gbl.add(strict, gbc1);


        gbc1.gridy = 10;
        gbc1.gridx = 1;
        gbl.add(strictinput, gbc1);

        //=============================

        gbc1.gridy = 11;
        gbc1.gridx = 0;
        gbl.add(minAAFreqLabl, gbc1);

        gbc1.gridy = 11;
        gbc1.gridx = 1;
        gbl.add(minAAFreqinput, gbc1);

        //=============================

        gbc1.gridy = 12;
        gbc1.gridx = 0;
        gbl.add(advancedOptions, gbc1);


        this.add(gbl, BorderLayout.NORTH);


        advancedOptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImageIcon icon = null;
                ImageIcon newIcon = null;
                try {
                    java.net.URL imgURL = null;
                    ClassLoader cl = getClass().getClassLoader();
                    imgURL = cl.getResource("images/kr4.png");
                    BufferedImage bi = ImageIO.read(imgURL);
                    icon = new ImageIcon(bi);
                    Image image = icon.getImage();
                    Image newImg = image.getScaledInstance(105, 79, Image.SCALE_SMOOTH);
                    newIcon = new ImageIcon(newImg);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(CodeHopWizard.getInstance(), getOptionsPanel(), "Advanced Options", JOptionPane.OK_OPTION, newIcon);
            }
        });


        setClampByTemp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tempinput.setEditable(true);
                tempinput.setText("60");
                manualClampLeninput.setEditable(false);
                manualClampLeninput.setText("");
                CodeHopWizard.properTempCalc = true;
            }
        });

        setClamplen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tempinput.setEditable(false);
                tempinput.setText("");
                manualClampLeninput.setEditable(true);
                manualClampLeninput.setText("25");
                CodeHopWizard.properTempCalc = false;
            }
        });

        setClamplen.doClick(); //should be selected by default, and run actions


        codonTableHelp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImageIcon newIcon = null;
                // add image
                try {
                    ImageIcon icon = null;

                    java.net.URL imgURL2 = null;
                    ClassLoader cl2 = getClass().getClassLoader();
                    imgURL2 = cl2.getResource("images/circle_question_mark.png");
                    BufferedImage bi3 = ImageIO.read(imgURL2);
                    icon = new ImageIcon(bi3);
                    Image img = icon.getImage();
                    Image newimg = img.getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
                    newIcon = new ImageIcon(newimg);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                String helpMsg = "<html>The codon tables are derived from the </html>";

                String link = "<html><a href=\"http://www.kazusa.or.jp/codon/\">Codon Usage Database.</a></html>";

                JLabel linklabel = new JLabel(link);
                linklabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                linklabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                            try {
                                URI ur = new URI("http://www.kazusa.or.jp/codon/");
                                desktop.browse(ur);
                            } catch (Exception t) {
                                t.printStackTrace();
                            }
                        }

                    }
                });

                String helpMsg2 = "<html><br>In the drop down you may select a built in codon table, create a custom table, or import an already created custom table. <br><br>" +
                        "The \"Custom..\" option allows the user to edit the codon probabilities of the previously loaded codon table. <br>" +
                        "For example, if Mus musculus is first selected, then \"Custom..\" is clicked, the user can edit the probabilities<br>" +
                        "of the codons in the Mus musculus codon table.<br><br>" +
                        "To use the values again later, export the custom codon table to save it to your hard drive. The saved file can be<br>" +
                        "opened and edited in Excel or a similar program.<br><br>" +
                        "The file can then be imported later using the \"Import Custom..\" option.</html>";


                JLabel helpLabel = new JLabel(helpMsg);
                JLabel helpLabel2 = new JLabel(helpMsg2);

                JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT));
                f.add(helpLabel);
                f.add(linklabel);

                JPanel n = new JPanel(new BorderLayout());
                n.add(f, BorderLayout.NORTH);
                n.add(helpLabel2, BorderLayout.CENTER);


                JOptionPane.showMessageDialog(me, n, "Codon Table Help", JOptionPane.INFORMATION_MESSAGE, newIcon);
            }
        });


        //***************************************************
        // Action Listeners for Custom Codon Table Dialog Buttons
        //***************************************************

        // OK button listener
        codonTableButtons[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (customCodonTable.sumErr || customCodonTable.inputErr) {
                    String warningMsg = "Please fix any errors before submitting the custom codon table.";
                    JOptionPane.showMessageDialog(me, warningMsg, "Table Errors", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    customCodonTable.updateCodonValues(codonTable);
                    CodonTable.currentTableName = "Custom";
                    previousCodonTable = "Custom";
                    fileNameDisplay.setText("");
                    codonTableDialog.dispose();
                }
            }
        });

        // export table button listener
        codonTableButtons[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customCodonTable.updateCodonValues(codonTable);
                CSVCodonFileManager.exportCurrentTableToCSV(codonTable.getTableContents());
                codonTableDialog.dispose();
            }
        });


        // cancel button listener
        codonTableButtons[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectCodonTable.setSelectedItem(previousCodonTable);
                codonTableDialog.dispose();
            }
        });


        //***************************************************
        // Action Listener for select codon table JComboBox
        //***************************************************

        selectCodonTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = selectCodonTable.getSelectedItem().toString();

                if (selected.equals(codonDropDownLabels[6])) {
                    customTable();
                } else if (selected.equals(codonDropDownLabels[7])) {
                    customImport();
                } else {
                    fileNameDisplay.setText("");
                    if (selected.equals(CodonTable.currentTableName)) {
                        return;
                    } else if (selected.equals(codonDropDownLabels[0])) {
                        codonTable.setHomoSapien();
                    } else if (selected.equals(codonDropDownLabels[1])) {
                        codonTable.setMusMusculus();
                    } else if (selected.equals(codonDropDownLabels[2])) {
                        codonTable.setCowPox();
                    } else if (selected.equals(codonDropDownLabels[3])) {
                        codonTable.setEnteroBacteriaES18();
                    } else if (selected.equals(codonDropDownLabels[4])) {
                        codonTable.setEnteroBacteriaT4();
                    } else if (selected.equals(codonDropDownLabels[5])) {
                        codonTable.setEscherichiaColi();
                    }
                    previousCodonTable = selected;
                }
            }
        });
    }


    private void customImport() {
        CSVCodonFileManager csvImporter = new CSVCodonFileManager(me);

        if (csvImporter.cancelled == true || !csvImporter.isCorrectCSV()) {
            fileNameDisplay.setText("");
            selectCodonTable.setSelectedItem(previousCodonTable);
            return;
        }
        CodonTable ct = csvImporter.getCodonTableFromCSV();
        codonTable = ct;
        CodonTable.currentTableName = "Custom";
        selectCodonTable.setSelectedItem("Custom");
        previousCodonTable = "Custom";
    }

    private void customTable() {
        customCodonTable = new CustomCodonTable(codonTable);

        codonTableDialog = new JDialog(CodeHopWizard.getInstance());
        codonTableDialog.setSize(400, 600);
        codonTableDialog.setResizable(false);
        codonTableDialog.setLocationRelativeTo(null);
        codonTableDialog.setLayout(new BorderLayout());
        codonTableDialog.setModal(true);
        codonTableDialog.setTitle("Custom Codon Table");
        codonTableDialog.add(customCodonTable, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(codonTableButtons[1]);
        buttonPanel.add(codonTableButtons[2]);
        buttonPanel.add(codonTableButtons[0]);

        codonTableDialog.add(buttonPanel, BorderLayout.SOUTH);
        codonTableDialog.setVisible(true);
    }


    private JPanel getOptionsPanel() {
        JPanel dialog = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        dialog.setLayout(gbl);

        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.insets = new Insets(5, 5, 5, 5);

        //=============================

        gbc.gridy = 0;
        gbc.gridx = 0;
        dialog.add(lastPos, gbc);

        gbc.gridy = 0;
        gbc.gridx = 1;
        dialog.add(invariantLastPosinput, gbc);

        gbc.gridy = 0;
        gbc.gridx = 2;
        dialog.add(coreStrictinput, gbc);

        //=============================
        gbc.gridy = 1;
        gbc.gridx = 0;
        dialog.add(blockLengthlabl, gbc);

        gbc.gridy = 1;
        gbc.gridx = 1;
        dialog.add(blockLeninput, gbc);

        //=============================

        gbc.gridy = 2;
        gbc.gridx = 0;
        dialog.add(primerConcentration, gbc);

        gbc.gridy = 2;
        gbc.gridx = 1;
        dialog.add(primerConcentrationinput, gbc);

        //=============================

        gbc.gridy = 3;
        gbc.gridx = 0;
        dialog.add(GorCinLastPos, gbc);

        gbc.gridy = 3;
        gbc.gridx = 1;
        dialog.add(GorCinLastPosinput, gbc);

        //=============================

        gbc.gridy = 4;
        gbc.gridx = 0;
        dialog.add(excludeLSandA, gbc);

        gbc.gridy = 4;
        gbc.gridx = 1;
        dialog.add(excludeLSandAinput, gbc);

        //=============================

        return dialog;
    }

    public String addSpaces() {
        return "      ";
    }

    public JFormattedTextField createNewInputField(NumberFormat nf, int minVal, int maxVal, String defaultText, int numCols) {
        NumberFormatter restrict = new NumberFormatter(nf);
        restrict.setValueClass(Integer.class);
        restrict.setMinimum(minVal);
        restrict.setMaximum(maxVal);
        restrict.setCommitsOnValidEdit(true);
        JFormattedTextField textField = new JFormattedTextField(restrict);
        textField.setText(defaultText);
        textField.setColumns(numCols);
        textField.setHorizontalAlignment(JTextField.RIGHT);
        return textField;
    }

    public static String getAlignmentType() {
        if (mafft.isSelected()) {
            return "mafft";
        } else if (muscle.isSelected()) {
            return "muscle";
        } else if (clustal.isSelected()) {
            return "clustalo";
        } else {
            return "noAlign";
        }
    }

    public static String getAlignmentTypeForDisplay() {
        if (mafft.isSelected()) {
            return "Mafft";
        } else if (muscle.isSelected()) {
            return "Muscle";
        } else if (clustal.isSelected()) {
            return "Clustal";
        } else {
            return "No alignment";
        }
    }

    public static boolean verifyInputs() {
        double minAAfreq = getminAAFreq();

        if (minAAfreq < 0 || minAAfreq > 100) {
            return false;
        }
        double strict = getStrictness();
        if (strict < 0 || strict > 1) {
            return false;
        }
        int degen = getDegeneracy();
        if (degen < 0 || degen > 2000) {
            return false;
        }
        int temp = getTemp();
        if (temp < 40 || temp > 100) {
            return false;
        }
        int core = getDegenerateCoreLength();
        if (core < 3 || core > 5) {
            return false;
        }
        int mlen = getMinBlockLength();
        if (mlen < 5 || mlen > 100) {
            return false;
        }

        return true;
    }

    public static double getPrimerConcentration() {
        int val = (int) primerConcentrationinput.getValue();
        double v2 = val * 1e-9;
        return v2;
    }

    public static int getminAAFreq() {
        return (int) minAAFreqinput.getValue();
    }

    public static double getStrictness() {
        int val = (int) strictinput.getValue();
        return ((double) val) / 100;
    }

    public static int getDegeneracy() {
        return (Integer) degeninput.getValue();
    }

    public static int getTemp() {
        return (Integer) tempinput.getValue();
    }

    public static CodonTable getCodonTable() {
        return codonTable;
    }

    public static int getDegenerateCoreLength() {
        return (Integer) coreLeninput.getValue();
    }

    public static int getMinBlockLength() {
        return (Integer) blockLeninput.getValue();
    }

    public static int getUserDefinedConsensusClampLength() {
        return (Integer) manualClampLeninput.getValue();
    }

}