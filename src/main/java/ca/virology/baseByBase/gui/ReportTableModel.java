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

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;


/**
 * This is the datamodel for the report tables used in base-by-base
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class ReportTableModel extends AbstractTableModel {
    //~ Instance fields ////////////////////////////////////////////////////////

    public int m_rows;
    public int m_cols;
    public Vector m_data;
    public Vector m_origData;
    public Vector m_onlyDiffData;
    public Vector m_onlyAffGenesData;
    public String[] m_names;
    public Vector m_comps;
    public boolean m_valid;
    public boolean m_ascending;
    public int m_sortCol;
    public String m_rowName = "";
    private boolean m_showOnlyDifferentGenes = true;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new ReportTableModel object.
     *
     * @param data        the vector of vectors representing data rows and columns
     * @param comparators the vector of sorting comparators
     * @param colNames    the vector of column names
     */
    public ReportTableModel(Vector data, Vector comparators, String[] colNames) {
        m_comps = comparators;
        m_names = colNames;
        m_origData = data;
        m_data = data;
        m_rows = data.size();

        m_onlyDiffData = new Vector();
        for (Iterator it = m_origData.iterator(); it.hasNext(); ) {
            Vector rowData = (Vector) it.next();
            if (colNames.length > 8) {
                if (colNames[7].equals("Difference %")) {
                    String diff = (String) rowData.get(7);
                    if (!diff.equals("0.00%")) {
                        m_onlyDiffData.add(rowData);
                    }
                }
            }

        }

        m_onlyAffGenesData = new Vector();
        for (Iterator it = m_origData.iterator(); it.hasNext(); ) {
            Vector rowData = (Vector) it.next();
            if (colNames.length > 3) {
                if (colNames[2].equals("Feature Affected")) {
                    String diff = (String) rowData.get(2);
                    if (!diff.equals("NA")) {
                        m_onlyAffGenesData.add(rowData);
                    }
                }
            }

        }
    }

    public void setShowOnlyDifferentGenes(boolean state) {
        if (state) {
            m_data = m_onlyDiffData;
            m_rows = m_data.size();
        } else {
            m_data = m_origData;
            m_rows = m_data.size();
        }
        fireTableDataChanged();
    }

    public void setShowOnlyAffectedGenes(boolean state) {
        if (state) {
            m_data = m_onlyAffGenesData;
            m_rows = m_data.size();
        } else {
            m_data = m_origData;
            m_rows = m_data.size();
        }
        fireTableDataChanged();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * returns the column class
     *
     * @return the row count
     */
    public Class getColumnClass(int column) {
        if (m_data.get(0) instanceof Vector) {
            Vector v = (Vector) m_data.get(0);

            if (column >= v.size()) {
                return null;
            } else {
                return v.get(column).getClass();
            }
        } else {
            return null;
        }
    }

    /**
     * get the number of rows
     *
     * @return the row count
     */
    public int getRowCount() {
        return m_data.size();
    }

    /**
     * get the number of columnts
     *
     * @return the col count
     */
    public int getColumnCount() {
        return m_names.length;
    }

    /**
     * get the name of a column
     *
     * @param index the index of the column
     * @return the name of the column
     */
    public String getColumnName(int index) {
        String str = m_names[index].toString();

        //if (index==m_sortCol)
        //    str += m_ascending ? " " : " ";
        return str;
    }

    /**
     * get the name of the rows
     *
     * @return the row name
     */
    public String getRowName() {
        return m_rowName;
    }

    /**
     * set the name of the rows
     *
     * @param newName the new name
     */
    public void setRowName(String newName) {
        m_rowName = newName;
    }

    /**
     * get the vector of vectors that is the data set
     *
     * @return the data set
     */
    public Vector getDataVector() {
        return m_data;
    }

    /**
     * get the edit status of a cell
     *
     * @param row    the row of the cell
     * @param column the column of the cell
     * @return true if the cell is editable, false otherwise
     */
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    /**
     * get the value at the given cell
     *
     * @param row    the row of the cell
     * @param column the column of the cell
     * @return the value
     */
    public Object getValueAt(int row, int column) {
        if (m_data.get(row) instanceof Vector) {
            Vector v = (Vector) m_data.get(row);

            if (column >= v.size()) {
                return null;
            } else {
                return v.get(column);
            }
        } else {
            return null;
        }
    }

    // Regardless of sort order (ascending or descending),
    // null values always appear last.
    // colIndex specifies a column in model.
    public void sortAllRowsBy(int colIndex) {
        m_sortCol = colIndex;
        m_ascending = !m_ascending;

        Vector data = this.getDataVector();
        Collections.sort(data, new ColumnSorter(colIndex, m_ascending));
        this.fireTableStructureChanged();
    }

    //~ Inner Classes //////////////////////////////////////////////////////////

    // This comparator is used to sort vectors of data
    protected class ColumnSorter implements Comparator {
        int colIndex;
        boolean ascending;

        ColumnSorter(int colIndex, boolean ascending) {
            this.colIndex = colIndex;
            this.ascending = ascending;
        }

        public int compare(Object a, Object b) {
            Vector v1 = (Vector) a;
            Vector v2 = (Vector) b;
            Object o1 = v1.get(colIndex);
            Object o2 = v2.get(colIndex);
            Comparator c = (Comparator) m_comps.get(colIndex);

            int val = c.compare(o1, o2);

            if (!ascending) {
                val = -val;
            }

            return val;

			/*
             // Treat empty strains like nulls
			  if (o1 instanceof String && ((String)o1).length() == 0) {
			  o1 = null;
			  }
			  if (o2 instanceof String && ((String)o2).length() == 0) {
			  o2 = null;
			  }
			  
			  // Sort nulls so they appear last, regardless
			   // of sort order
			    if (o1 == null && o2 == null) {
			    return 0;
			    } else if (o1 == null) {
			    return 1;
			    } else if (o2 == null) {
			    return -1;
			    } else if (o1 instanceof Comparable) {
			    if (ascending) {
			    return ((Comparable)o1).compareTo(o2);
			    } else {
			    return ((Comparable)o2).compareTo(o1);
			    }
			    } else {
			    if (ascending) {
			    return o1.toString().compareTo(o2.toString());
			    } else {
			    return o2.toString().compareTo(o1.toString());
			    }
			    }
			    */
        }
    }

}