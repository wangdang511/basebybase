/*
 * Base-by-base: Whole Genome pairwise and multiple alignment editor
 * Copyright (C) 2003  Dr. Chris Upton, University of Victoria
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ca.virology.baseByBase.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.*;

import java.text.NumberFormat;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

//import com.sun.rsasign.c;


/**
 * This displays a table report of given data
 */
public class ReportTablePanel extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected ReportTableModel m_model;
    protected JTable m_table;
    protected Vector m_data;
    protected final StatusBar m_status = new StatusBar("Report Data");
    private boolean cancelled = false;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Construct a new panel with the given data
     *
     * @param data        The data to display
     * @param comparators The comparators used to sort each column
     * @param colNames    A vector of strings, each representing a column name
     */
    public ReportTablePanel(Vector data, Vector comparators, String[] colNames) {

        m_model = new ReportTableModel(data, comparators, colNames);
        m_table = new JTable(m_model);

        initTable();
        initUI();

    }

    /**
     * Construct a new panel with the given data
     *
     * @param data        The data to display
     * @param comparators The comparators used to sort each column
     * @param colNames    A vector of strings, each representing a column name
     */
    public ReportTablePanel(Vector data, Vector comparators, String[] colNames, int MGC) {

        if (data != null) {
            m_model = new ReportTableModel(data, comparators, colNames);
            m_table = new JTable(m_model);
            initMGCTable();
            initUI();
        } else {
            cancelled = true;
        }
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * set the name of the rows
     *
     * @param newName the new name
     */
    public boolean isCancelled() {
        return cancelled;
    }


    /**
     * set the name of the rows
     *
     * @param newName the new name
     */
    public void setRowName(String newName) {
        m_model.setRowName(newName);
    }

    /**
     * get the name of the rows
     *
     * @return the name of the rows
     */
    public String getRowName() {
        return m_model.getRowName();
    }

    /**
     * get the table object displayed by this panel
     *
     * @return the JTable
     */
    protected JTable getTable() {
        return m_table;
    }

    /**
     * get the datamodel for this panel
     *
     * @return the data model
     */
    protected ReportTableModel getReportModel() {
        return m_model;
    }

    /**
     * init the table's properties
     */
    protected void initTable() {
        // Disable autoCreateColumnsFromModel otherwise all the
        // column customizations and adjustments will be lost
        // when the model data is sorted
        m_table.setAutoCreateColumnsFromModel(false);

    }

    /**
     * init the table's properties
     */
    protected void initMGCTable() {
        // Disable autoCreateColumnsFromModel otherwise all the
        // column customizations and adjustments will be lost
        // when the model data is sorted
        m_table.setAutoCreateColumnsFromModel(false);
        m_table.setDefaultRenderer(String.class, new CustomTableCellRenderer());

    }

    /**
     * init the UI Properties
     */
    protected void initUI() {
        setLayout(new BorderLayout());

        final JTableHeader header = m_table.getTableHeader();
        header.addMouseListener(new ColumnHeaderListener());

        SelectionListener listener = new SelectionListener(m_table);
        m_table.getSelectionModel().addListSelectionListener(listener);
        m_table.getColumnModel().getSelectionModel().addListSelectionListener(listener);

        //m_table.setDragEnabled(true);


        add(new JScrollPane(m_table), BorderLayout.CENTER);
        add(m_status, BorderLayout.SOUTH);
    }

    /**
     * sort based on a given column
     *
     * @param colIndex the column to sort
     */
    protected void doSort(int colIndex) {
        m_model.sortAllRowsBy(colIndex);
    }

    /**
     * get the html representation of this table
     *
     * @param hHtml a header section of html
     * @param tHtml a tale section of html
     * @param all   if true, all rows, otherwise, just what's selected
     * @return a string of HTML code
     */
    protected String getHtml(String hHtml, String tHtml, boolean all) {
        StringBuffer b = new StringBuffer();

        int[] rowsToWrite;

        if (all) {
            rowsToWrite = null;
        } else {
            rowsToWrite = m_table.getSelectedRows();

            if (rowsToWrite.length <= 0) {
                return "";
            }
        }

        b.append("<HTML>");
        b.append("<BODY>");
        b.append(hHtml);
        b.append("<TABLE BORDER=2>");

        TableColumnModel colModel = m_table.getColumnModel();
        int nColumns = colModel.getColumnCount();
        int nRow;
        int nCol;
        b.append("  <TR>");


        String str = "";

        for (nCol = 0; nCol < nColumns; nCol++) {
            TableColumn tk = colModel.getColumn(nCol);
            str = (String) tk.getIdentifier();
            //str = str.replace('�', ' ');
            //str = str.replace('�', ' ');
            b.append("    <TD><B>" + str + "</B></TD>");
        }

        b.append("  </TR>");

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);

        TableModel tblModel = m_table.getModel();
        int endRow = m_table.getRowCount();
        String align;

        for (nRow = 0; nRow < endRow; nRow++) {
            if (rowsToWrite != null) {
                if (Arrays.binarySearch(rowsToWrite, nRow) < 0) {
                    continue;
                }
            }

            b.append("  <TR>");

            for (nCol = 0; nCol < nColumns; nCol++) {
                Object obj = m_table.getValueAt(nRow, nCol);
                str = null;
                align = "left";

                if (obj != null) {
                    if (obj instanceof Double) {
                        str = nf.format((Double) obj);
                    } else {
                        str = obj.toString();
                    }

                    if (obj instanceof Number) {
                        align = "right";
                    }

                    b.append("    <TD><DIV ALIGN=" + align + ">" + str +
                            "</DIV></TD>");
                }
            }

            b.append("  </TR>");
        }

        b.append("  </TABLE>");
        b.append(tHtml);
        b.append("  </BODY>");
        b.append("  </HTML>");

        return b.toString();
    }

    public String getCSV(boolean all) {
        StringBuffer b = new StringBuffer();

        int[] rowsToWrite;

        if (all) {
            rowsToWrite = null;
        } else {
            rowsToWrite = m_table.getSelectedRows();

            if (rowsToWrite.length <= 0) {
                return "";
            }
        }

        TableColumnModel colModel = m_table.getColumnModel();
        int nColumns = colModel.getColumnCount();
        int nRow;
        int nCol;

        String str = "";

        for (nCol = 0; nCol < nColumns; nCol++) {
            TableColumn tk = colModel.getColumn(nCol);
            str = (String) tk.getIdentifier();
            //str = str.replace('�', ' ');
            //str = str.replace('�', ' ');
            if (nCol > 0)
                b.append(", ");
            b.append(str);
        }

        b.append("\r\n");

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);

        TableModel tblModel = m_table.getModel();
        int endRow = m_table.getRowCount();

        for (nRow = 0; nRow < endRow; nRow++) {
            if (rowsToWrite != null) {
                if (Arrays.binarySearch(rowsToWrite, nRow) < 0) {
                    continue;
                }
            }

            if (nRow > 0)
                b.append("\r\n");

            for (nCol = 0; nCol < nColumns; nCol++) {
                Object obj = m_table.getValueAt(nRow, nCol);
                str = null;

                if (obj != null) {
                    if (obj instanceof Double) {
                        str = nf.format((Double) obj);
                    } else {
                        str = obj.toString();
                    }

                    if (str.indexOf(',') >= 0) {
                        str = "\"" + str + "\"";
                    }
                    b.append(str);
                }
                b.append(",");
            }
        }

        return b.toString();
    }

    public String getTSV(boolean all) {
        StringBuffer b = new StringBuffer();

        int[] rowsToWrite;

        if (all) {
            rowsToWrite = null;
        } else {
            rowsToWrite = m_table.getSelectedRows();

            if (rowsToWrite.length <= 0) {
                return "";
            }
        }

        TableColumnModel colModel = m_table.getColumnModel();
        int nColumns = colModel.getColumnCount();
        int nRow;
        int nCol;

        String str = "";

        for (nCol = 0; nCol < nColumns; nCol++) {
            TableColumn tk = colModel.getColumn(nCol);
            str = (String) tk.getIdentifier();
            /*str = str.replace('�', ' ');
            str = str.replace('�', ' ');*/
            str = str.replace('\t', ' ');
            if (nCol > 0)
                b.append("\t");
            b.append(str);
        }

        b.append("\r");

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);

        TableModel tblModel = m_table.getModel();
        int endRow = m_table.getRowCount();

        for (nRow = 0; nRow < endRow; nRow++) {
            if (rowsToWrite != null) {
                if (Arrays.binarySearch(rowsToWrite, nRow) < 0) {
                    continue;
                }
            }

            if (nRow > 0)
                b.append("\r\n");

            for (nCol = 0; nCol < nColumns; nCol++) {
                Object obj = m_table.getValueAt(nRow, nCol);
                str = null;

                if (obj != null) {
                    if (obj instanceof Double) {
                        str = nf.format((Double) obj);
                    } else {
                        str = obj.toString();
                    }

                    if (str.indexOf('\t') >= 0) {
                        str = "\"" + str + "\"";
                    }
                    b.append(str);
                }
                b.append("\t");
            }
        }

        return b.toString();
    }


    public void setShowOnlyDifferentGenes(boolean state) {
        m_model.setShowOnlyDifferentGenes(state);
    }

    public void setShowOnlyAffectedGenes(boolean state) {
        m_model.setShowOnlyAffectedGenes(state);
    }

    //~ Inner Classes //////////////////////////////////////////////////////////

    public class ColumnHeaderListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            JTable table = ((JTableHeader) evt.getSource()).getTable();
            TableColumnModel colModel = table.getColumnModel();

            // The index of the column whose header was clicked
            int vColIndex = colModel.getColumnIndexAtX(evt.getX());
            int mColIndex = table.convertColumnIndexToModel(vColIndex);

            // Return if not clicked on any column header
            if (vColIndex == -1) {
                return;
            }

            // Determine if mouse was clicked between column heads
            Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);

            if (vColIndex == 0) {
                headerRect.width -= 3; // Hard-coded constant
            } else {
                headerRect.grow(-3, 0); // Hard-coded constant
            }

            if (!headerRect.contains(evt.getX(), evt.getY())) {
                // Mouse was clicked between column heads
                // vColIndex is the column head closest to the click
                // vLeftColIndex is the column head to the left of the click
                int vLeftColIndex = vColIndex;

                if (evt.getX() < headerRect.x) {
                    vLeftColIndex--;
                }
            }

            int col = table.getTableHeader().columnAtPoint(evt.getPoint());
            doSort(col);
        }
    }

    public class SelectionListener implements ListSelectionListener {
        JTable table;
        int first;
        int last;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table) {
            this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {
            // If cell selection is enabled, both row and column
            // change events are fired
            if ((e.getSource() == table.getSelectionModel()) && table.getRowSelectionAllowed()) {
                // Column selection changed
                first = e.getFirstIndex();
                last = e.getLastIndex();
            } else if ((e.getSource() == table.getColumnModel().getSelectionModel()) && table.getColumnSelectionAllowed()) {
            }

            if (e.getValueIsAdjusting()) {
                // The mouse button has not yet been released
            } else {
            }

            JTable t = getTable();
            int[] rows = t.getSelectedRows();
            String name = getReportModel().getRowName();

            if ((name == null) || name.equals("")) {
                name = "Row";
            }

            if (rows.length > 0) {
                m_status.setText(rows.length + " " + name + "s Selected");
            } else {
                m_status.clear();
            }
        }
    }

    public class CustomTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (table.getValueAt(row, column).toString().equals("X") || table.getValueAt(row, column).toString().equals("NA")) {
                cell.setForeground(Color.gray);
                //System.out.println(table.getValueAt(row,column).toString());

            } else {
                cell.setForeground(Color.BLACK);
            }


            return cell;

        }
    }
}
