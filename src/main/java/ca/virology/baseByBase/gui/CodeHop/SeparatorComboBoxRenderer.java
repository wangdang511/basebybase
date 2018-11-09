package ca.virology.baseByBase.gui.CodeHop;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

// class from http://esus.com/creating-a-jcombobox-with-a-divider-separator-line/

class SeparatorComboBoxRenderer extends BasicComboBoxRenderer implements ListCellRenderer {
    public SeparatorComboBoxRenderer() {
        super();
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setFont(list.getFont());
        if (value instanceof Icon) {
            setIcon((Icon) value);
        }
        if (value instanceof JSeparator) {
            return (Component) value;
        } else {
            setText((value == null) ? "" : value.toString());
        }

        return this;
    }
}
