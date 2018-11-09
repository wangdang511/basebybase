package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.FeaturedSequence;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class DeleteColumnsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea textArea;
    public FeaturedSequence[] seqs;
    private List<Pair<Integer, Integer>> deleteIndices;

    public DeleteColumnsDialog(FeaturedSequence[] seqs) {
        this.seqs = seqs;
        setContentPane(contentPane);
        setModal(true);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        boolean success = validateInput();
        if (success)
            dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        textArea.setText("");
        this.deleteIndices = null;
        dispose();
    }

    /**
     * @return boolean indicating whether user-supplied inputs for column indices are valid
     */
    private boolean validateInput() {
        List inputs = new ArrayList();
        List<Pair<Integer, Integer>> inputRanges = new ArrayList<>();
        inputs.addAll(Arrays.asList(textArea.getText().trim().split("\\s+")));
        for (int i = 0; i < inputs.size(); i++) {
            // if true, all characters valid
            if (((String) inputs.get(i)).replaceAll("[0-9-]*", "").length() == 0) {
                // if element is range
                if (((String) inputs.get(i)).contains("-")) {
                    try {
                        String[] bounds = ((String) inputs.get(i)).split("-");
                        if (bounds[0].length() == 0 | bounds[1].length() == 0) {
                            JOptionPane.showMessageDialog(this, "Cannot parse input. If you have provided ranges, please make sure\n" + "there is no whitespace before or after '-'. Negative values not permitted.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                            return false;
                        } else if (Integer.parseInt(bounds[0]) > Integer.parseInt(bounds[1])) {
                            JOptionPane.showMessageDialog(this, "Cannot parse input. Please ensure that lower bound of range is less than value of upper bound.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                        // add range
                        for (Pair<Integer, Integer> p : inputRanges) {
                            if ((Integer.parseInt(bounds[0]) >= p.getKey() & Integer.parseInt(bounds[1]) <= p.getValue()) |
                                    Integer.parseInt(bounds[1]) >= p.getKey() & Integer.parseInt(bounds[1]) <= p.getValue() |
                                    Integer.parseInt(bounds[0]) <= p.getKey() & Integer.parseInt(bounds[1]) >= p.getValue()) {
                                JOptionPane.showMessageDialog(this, "Overlapping ranges and/or duplicate values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                                return false;
                            }
                        }
                        inputRanges.add(new Pair<>(Integer.parseInt(bounds[0]), Integer.parseInt(bounds[1])));

                    } catch (IndexOutOfBoundsException e) {
                        JOptionPane.showMessageDialog(this, "Cannot parse input. Please remove all whitespace before and after '-'.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                // add single values as if they were a range (by making start == stop)
                else {
                    if (textArea.getText().length() == 0)
                        return false;
                    for (Pair<Integer, Integer> p : inputRanges) {
                        if (Integer.parseInt((String) inputs.get(i)) >= p.getKey() & Integer.parseInt((String) inputs.get(i)) <= p.getValue()) {
                            JOptionPane.showMessageDialog(this, "Overlapping ranges and/or duplicate values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                    inputRanges.add(new Pair<>(Integer.parseInt((String) inputs.get(i)), Integer.parseInt((String) inputs.get(i))));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid characters used. Please enter only integer values, with bounds of ranges separated by a '-'\n" + "and multiple inputs separated by whitespace only.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        //find length of longest sequence
        int max = -1;
        for (FeaturedSequence fs : seqs) {
            if (fs.length() > max) {
                max = fs.length();
            }
        }
        // check that no value in inputInts exceeds max
        for (Pair<Integer, Integer> i : inputRanges) {
            if (i.getValue() > max) {
                JOptionPane.showMessageDialog(this, "One or more specified values exceed the length of the longest sequence.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        this.deleteIndices = inputRanges;

        sortPairCollection(deleteIndices);
        //System.out.println("sorted deleteIndices: " + deleteIndices);

        return true;
    }

    public List<Pair<Integer, Integer>> getDeleteIndices() {
        if (validateInput())
            return deleteIndices;
        else
            return null;
    }

    public static <K extends Comparable<? super K>, V extends Comparable<? super V>> void sortPairCollection(List<Pair<K, V>> col) {
        Collections.sort(col, new Comparator<Pair<K, V>>() {
            @Override
            public int compare(Pair<K, V> o1, Pair<K, V> o2) {
                int result = o1.getValue().compareTo(o2.getValue()) == 0 ? o1.getKey().compareTo(o2.getKey()) : o1.getValue().compareTo(o2.getValue());
                return result;

            }
        });
    }

    {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-921103));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("Apply");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Specify columns to delete:");
        panel3.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, 1, null, null, null, 0, false));
        final JTextArea textArea1 = new JTextArea();
        textArea1.setBackground(new Color(-1118482));
        textArea1.setEditable(false);
        textArea1.setEnabled(true);
        textArea1.setLineWrap(true);
        textArea1.setRows(0);
        textArea1.setText("How to Use: column indices can be either individual values or ranges. \nMultiple indices should be separated by whitespace only. \nRanges should be indicated by a \"-\", with no whitespace between the lower and upper bounds. \nThe lower and upper bounds of ranges are inclusive.\n(For example, entering \"50 100-200\" will delete column 50 and the columns numbered from 100 up to and including 200.) ");
        textArea1.setWrapStyleWord(true);
        panel3.add(textArea1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, new Dimension(150, 50), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane1.setViewportView(textArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
