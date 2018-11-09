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

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.util.*;

import javax.swing.*;

import ca.virology.lib.prefs.BBBPrefs;
import ca.virology.lib.util.gui.UITools;


/**
 * This dialog allows users to edit and apply their settings and preferences
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class EditSettingsDialog extends JDialog {
    PrimaryPanel m_primaryPanel = null;
    HashMap m_components = new HashMap();
    PropertyChangeSupport m_changeSupport = new PropertyChangeSupport(this);

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new EditSettingsDialog object.
     */
    public EditSettingsDialog(PrimaryPanel p) {
        m_primaryPanel = p;
        setModal(false);

        initUI();
        pack();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * init the user interface
     */
    protected void initUI() {
        getContentPane().setLayout(new BorderLayout());
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setTitle("Edit Preferences");

        BBBPrefs bbbPrefs = BBBPrefs.getInstance();

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Display / Usability Settings"));
        settingsPanel.add(categoryPanel);

        String[] fieldLabels = new String[]{"Hilight Start/Stop Codons with Arrows  ",
                "Show AA Arrows ('===>')  ",
                "Show events and differences  ",
                "Show gapped position scale  ",
                "Show sequence position scale  ",
                "Show user comments/annotations  ",
                "Show primers  ",    //primer
                "Propogate edits to marked sequences  "};
        String[] fieldPrefNames = new String[]{"gui.hilightSSCodons",
                "gui.showAcidArrows",
                "gui.showDifferences",
                "gui.showScale",
                "gui.showSeqScale",
                "gui.showUserEvents",
                "gui.showPrimers",    //primer
                "gui.use.propEdit"};
        JPanel fieldPanel;
        JCheckBox checkBox;
        for (int i = 0; i < fieldLabels.length; i++) {
            fieldPanel = new JPanel();
            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
            fieldPanel.add(new JLabel(fieldLabels[i]));
            fieldPanel.add(Box.createHorizontalGlue());
            checkBox = new JCheckBox();
            checkBox.setSelected(Boolean.valueOf(bbbPrefs.get_bbbPref(fieldPrefNames[i])).booleanValue());
            m_components.put(fieldPrefNames[i], checkBox);
            fieldPanel.add(checkBox);
            categoryPanel.add(fieldPanel);
        }

        fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
        fieldPanel.add(new JLabel("Consensus Refresh Rate (in seconds)  "));
        fieldPanel.add(Box.createHorizontalGlue());
        int value = 0;
        try {
            value = Integer.parseInt(bbbPrefs.get_bbbPref("gui.consensus.refreshRate"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, 0, 1800, 15));
        m_components.put("gui.consensus.refreshRate", spinner);
        fieldPanel.add(spinner);
        categoryPanel.add(fieldPanel);

        categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Warning/Confirmation Message Settings"));
        settingsPanel.add(categoryPanel);

        fieldLabels = new String[]{"Show message when substituting aligned sequence",
                "Show message when quitting"};
        fieldPrefNames = new String[]{"warn.onAlign",
                "warn.onQuit"};
        for (int i = 0; i < fieldLabels.length; i++) {
            fieldPanel = new JPanel();
            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
            fieldPanel.add(new JLabel(fieldLabels[i]));
            fieldPanel.add(Box.createHorizontalGlue());
            checkBox = new JCheckBox();
            checkBox.setSelected(Boolean.valueOf(bbbPrefs.get_bbbPref(fieldPrefNames[i])).booleanValue());
            m_components.put(fieldPrefNames[i], checkBox);
            fieldPanel.add(checkBox);
            categoryPanel.add(fieldPanel);
        }

        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        buttons.add(Box.createHorizontalGlue());
        JButton button = new JButton("Cancel");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                dispose();
            }
        });
        buttons.add(button);
        buttons.add(Box.createHorizontalStrut(2));
        button = new JButton("Ok");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    commitChanges();
                } catch (Exception ex) {
                    UITools.showError("Error saving settings: " + ex.getClass(), getContentPane());
                }

                dispose();
            }
        });
        buttons.add(button);

        getContentPane().add(settingsPanel, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();

        // Position the dialog window
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = getWidth();
        int h = getHeight();
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        setLocation(x, y);
    }

    private void commitChanges() {
        BBBPrefs bbbPrefs = BBBPrefs.getInstance();

        String[] prefNames = new String[]{"gui.hilightSSCodons",
                "gui.showAcidArrows",
                "gui.showDifferences",
                "gui.showScale",
                "gui.showSeqScale",
                "gui.showUserEvents",
                "gui.showPrimers",    //primer
                "gui.use.propEdit",
                "gui.consensus.refreshRate",
                "warn.onAlign",
                "warn.onQuit"};
        for (int i = 0; i < prefNames.length; i++) {
            JComponent component = (JComponent) m_components.get(prefNames[i]);
            if (component instanceof JCheckBox) {
                bbbPrefs.set_bbbPref(prefNames[i], Boolean.toString(((JCheckBox) component).isSelected()));
            } else if (component instanceof JSpinner) {
                bbbPrefs.set_bbbPref(prefNames[i], ((JSpinner) component).getValue().toString());
            }
        }

        m_primaryPanel.refreshState();
        //m_changeSupport.firePropertyChange("", null, null);
    }

    public void addCommitListener(PropertyChangeListener l) {
        m_changeSupport.addPropertyChangeListener(l);
    }
}
