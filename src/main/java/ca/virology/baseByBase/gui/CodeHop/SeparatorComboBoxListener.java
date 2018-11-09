package ca.virology.baseByBase.gui.CodeHop;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//class from http://esus.com/creating-a-jcombobox-with-a-divider-separator-line/

public class SeparatorComboBoxListener implements ActionListener {
    JComboBox combobox;
    Object oldItem;

    SeparatorComboBoxListener(JComboBox combobox) {
        this.combobox = combobox;
        combobox.setSelectedIndex(0);
        oldItem = combobox.getSelectedItem();
    }

    public void actionPerformed(ActionEvent e) {
        Object selectedItem = combobox.getSelectedItem();
        if (selectedItem instanceof JSeparator) {
            combobox.setSelectedItem(oldItem);
        } else {
            oldItem = selectedItem;
        }
    }
}
