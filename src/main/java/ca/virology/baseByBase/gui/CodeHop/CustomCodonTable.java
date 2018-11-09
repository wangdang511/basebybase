package ca.virology.baseByBase.gui.CodeHop;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;


/*
This class is responsible for the gui class of the custom codon table input (after clicking custom jradiobutton)
 */

public class CustomCodonTable extends JPanel {

    public JTable viewtable;
    public JLabel errMsg1;
    public JLabel errMsg2;
    boolean sumErr;
    boolean inputErr;
    ArrayList<String> AAList = new ArrayList<String>(); //tracks amino acids that do not have probabilities summing to ~1
    static double slack = 0.05; //how far probabilities can deviate from 1


    public CustomCodonTable(CodonTable table) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        this.add(new JLabel(" "));
        this.add(new JLabel("Enter new codon probabilities."));
        this.add(new JLabel("Remember to export to use these probabilities again later."));
        this.add(new JLabel(" "));

        String columnNames[] = {"Amino Acid", "Codon", "Probability"};
        String tableContent[][] = table.getTableContents();

        EditableTableModel dtm = new EditableTableModel(tableContent, columnNames);
        dtm.setColumnEditable(2, true);
        viewtable = new JTable(dtm) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);

                String aminoAcid = viewtable.getValueAt(row, 0).toString();

                if (aminoAcid.equals("C") || aminoAcid.equals("E") || aminoAcid.equals("G") || aminoAcid.equals("I") || aminoAcid.equals("L") || aminoAcid.equals("N") || aminoAcid.equals("Q") || aminoAcid.equals("S") || aminoAcid.equals("V") || aminoAcid.equals("Y")) {
                    c.setBackground(new Color(228, 228, 228)); // light grey color
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        viewtable.setSelectionBackground(Color.BLUE);
        viewtable.setSelectionForeground(Color.BLACK);
        viewtable.setSize(300, 500);
        viewtable.setGridColor(Color.lightGray);

        viewtable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int col = e.getColumn();

                Col0Renderer col0renderer = new Col0Renderer();
                Col2Renderer col2renderer = new Col2Renderer();

                viewtable.getColumnModel().getColumn(2).setCellRenderer(col2renderer);
                viewtable.getColumnModel().getColumn(0).setCellRenderer(col0renderer);

                String AA = viewtable.getValueAt(e.getFirstRow(), 0).toString();
                if (!AAList.contains(AA)) {
                    AAList.add(AA);
                }

                double sum[] = new double[21];
                Arrays.fill(sum, 0);

                //check for incorrect input
                sumErr = false;
                inputErr = false;
                try {
                    for (int row = 0; row < viewtable.getRowCount(); row++) {
                        double d = Double.parseDouble(viewtable.getModel().getValueAt(row, col).toString()); // throws NumberFormatException
                        if (d < 0 || d > 1) {
                            inputErr = true;
                        }

                        char AminoAcid = viewtable.getValueAt(row, 0).toString().charAt(0);
                        int lookupVal = CodonTable.getLookupVal(AminoAcid);

                        sum[lookupVal] += Double.parseDouble(viewtable.getValueAt(row, 2).toString());
                    }
                } catch (NumberFormatException ne) {
                    inputErr = true;
                    sum[0] = 10; // something out of range b/c sum is 0 otherwise
                }

                // check for probability sum errors
                for (int i = 0; i < sum.length; i++) {

                    if (sum[i] > 1 + slack || sum[i] < 1 - slack) {
                        sumErr = true;
                    } else {
                        AA = String.valueOf(CodonTable.getAminoAcid(i)); // get the amino acid from lookupVal (i)
                        AAList.remove(AA); //remove it from the list of amino acids with incorrect summations
                    }
                }


                // display input error message if input error
                if (inputErr) {
                    errMsg1.setVisible(true);
                } else {
                    errMsg1.setVisible(false);
                }

                // display sum error message if sum error
                if (sumErr) {
                    errMsg2.setVisible(true);
                } else {
                    errMsg2.setVisible(false);
                }
            }
        });

        JScrollPane jsp = new JScrollPane(viewtable);
        viewtable.isCellEditable(0, 0);
        this.add(jsp);

        errMsg1 = new JLabel("* Enter probabilities between 0 and 1 (inclusive)");
        errMsg1.setForeground(Color.RED);
        errMsg1.setVisible(false); //initially invisible until improper input occurs
        this.add(errMsg1);

        errMsg2 = new JLabel("* Amino acid probabilities must sum to ~1");
        errMsg2.setForeground(Color.RED);
        errMsg2.setVisible(false); //initially invisible until improper input occurs
        this.add(errMsg2);

        this.setVisible(true);
    }

    //custom class needed to make only probablility cells editable. Nothing should be changed from it.
    public class EditableTableModel extends DefaultTableModel {

        boolean[] columnEditable;

        public EditableTableModel(Object[][] data, String[] columnNames) {
            super(data, columnNames);
            columnEditable = new boolean[columnNames.length];
            Arrays.fill(columnEditable, false);
        }

        public boolean isCellEditable(int row, int column) {
            if (!columnEditable[column]) {
                return false;
            } else {
                return super.isCellEditable(row, column);
            }
        }

        public void setColumnEditable(int column, boolean editable) {
            columnEditable[column] = editable;
        }

        public boolean getColumnEditable(int column) {
            return columnEditable[column];
        }
    }


    /*
        Col0Renderer Class

        Used to render the text colour in the Amino Acid column of the Custom Codon Table
        Checks whether probabilities for each amino acid add to 1
    */
    public class Col0Renderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            String aminoAcid = table.getValueAt(row, 0).toString();

            //check that probabilites add to 1
            if (AAList.contains(aminoAcid)) {
                // error occurred
                c.setForeground(Color.RED);
            } else {
                //no errors
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }


    /*
        Col2Renderer Class

        Used to render the text colour in the Probability column of the Custom Codon Table
        Checks whether input is correct
     */
    public class Col2Renderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            String data = table.getValueAt(row, col).toString();
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            //check that probability column input is correct
            try {
                double d = Double.parseDouble(data);

                if (d >= 0 && d <= 1) {
                    //no errors
                    c.setForeground(Color.BLACK);
                } else {
                    //error occurred - out of range
                    c.setForeground(Color.RED);
                }
            } catch (NumberFormatException e) {
                //error occurred - not a number
                c.setForeground(Color.RED);
            }

            //Return the JLabel which renders the cell.
            return c;
        }
    }


    //updates all codon values in the CodonTable object. Improve: only update rows with changes?
    public void updateCodonValues(CodonTable table) {
        for (int i = 0; i < viewtable.getRowCount(); i++) {
            String aa = viewtable.getModel().getValueAt(i, 0).toString();
            String codon = viewtable.getModel().getValueAt(i, 1).toString();
            double prob = Double.parseDouble(viewtable.getModel().getValueAt(i, 2).toString());
            //System.out.println("Updating row to "+aa+codon+prob);
            table.updateCodonEntry(aa, codon, prob);
        }
    }
}