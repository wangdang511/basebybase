/**
 * This class is used to render header panels in the context of a JList
 *
 * @author Ryan Brodie
 * @version 1.0
 */

package ca.virology.baseByBase.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import java.awt.Component;

class HeaderCellRenderer
        implements ListCellRenderer {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected int m_spacing;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new HeaderCellRenderer object.
     *
     * @param spacing
     */
    HeaderCellRenderer(int spacing) {
        m_spacing = spacing;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the rendered component
     *
     * @param list         the list context
     * @param value        the object to render
     * @param index        the position in the list
     * @param isSelected   true if the item is the selected item
     * @param cellHasFocus true if the cell has focus
     * @return a component
     */
    public Component getListCellRendererComponent(
            JList list,
            Object value, // value to display
            int index, // cell index
            boolean isSelected, // is the cell selected
            boolean cellHasFocus) // the list and the cell have the focus

    {
        if (value instanceof SequenceDisplay) {
            SequenceDisplay sd = (SequenceDisplay) value;
            HeaderPanel p = new HeaderPanel(sd);

            if (isSelected) {
                p.setBackground(list.getSelectionBackground());
                p.setForeground(list.getSelectionForeground());
            } else {
                p.setBackground(list.getBackground());
                p.setForeground(list.getForeground());
            }

            p.setDisplayFont(list.getFont());
            p.setBorder(BorderFactory.createEmptyBorder(0, 0, m_spacing, 0));

            return p;
        } else {
            JLabel l = new JLabel(value.toString());

            if (isSelected) {
                l.setBackground(list.getSelectionBackground());
                l.setForeground(list.getSelectionForeground());
            } else {
                l.setBackground(list.getBackground());
                l.setForeground(list.getForeground());
            }

            l.setEnabled(list.isEnabled());
            l.setFont(list.getFont());

            return l;
        }
    }
}

