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

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.BorderFactory;


/**
 * This panel is used to get user preferences for the current displayed area.
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class DisplayAreaPanel extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected JCheckBox m_leftEndCB;
    protected JCheckBox m_rightEndCB;
    final protected JTextField m_leftVal = new JTextField(10);
    final protected JTextField m_rightVal = new JTextField(10);

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new DisplayAreaPanel object.
     *
     * @param defstart the default start position to display
     * @param defstop  the default stop position to display
     */
    public DisplayAreaPanel(int defstart, int defstop) {
        setLayout(new BorderLayout());

        JPanel entry = new JPanel(new GridLayout(2, 3));

        m_leftEndCB = new JCheckBox("Endpoint");
        m_rightEndCB = new JCheckBox("Endpoint");

        entry.add(new JLabel("Left Position: "));
        entry.add(m_leftVal);
        entry.add(m_leftEndCB);
        entry.add(new JLabel("Right Position: "));
        entry.add(m_rightVal);
        entry.add(m_rightEndCB);

        add(new JLabel("<html>Enter the leftmost and/or rightmost viewable<br>" +
                "positions in the fields below.  This will cut the flanking regions<br>" +
                "from view and allow you to work within the given area.  All<br>" +
                "reports will pertain to this new region.<br><br>" +
                "'Endpoint' refers to the leftmost point (1) or the<br>" +
                "rightmost point (the end of the alignment)<br><br></html>"), BorderLayout.NORTH);
        add(entry, BorderLayout.CENTER);

        m_leftEndCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JCheckBox cb = (JCheckBox) ev.getSource();
                m_leftVal.setEnabled(!cb.isSelected());
                m_leftVal.setBackground((cb.isSelected()) ? Color.lightGray : Color.white);
            }
        });

        m_rightEndCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JCheckBox cb = (JCheckBox) ev.getSource();
                m_rightVal.setEnabled(!cb.isSelected());
                m_rightVal.setBackground((cb.isSelected()) ? Color.lightGray : Color.white);
            }
        });

        if (defstart == -1) {
            m_leftEndCB.setSelected(true);
            m_leftVal.setEnabled(false);
            m_leftVal.setBackground(Color.lightGray);
        } else {
            m_leftVal.setText(defstart + 1 + "");
        }

        if (defstop == -1) {
            m_rightEndCB.setSelected(true);
            m_rightVal.setEnabled(false);
            m_rightVal.setBackground(Color.lightGray);
        } else {
            m_rightVal.setText(defstop + 1 + "");
        }

        setBorder(BorderFactory.createTitledBorder("Display Area Settings"));
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the leftmost point
     *
     * @return the value entered or -1 if 'endpoint' selected
     * @throws NumberFormatException
     */
    public int getLeftValue() throws NumberFormatException {
        if (m_leftEndCB.isSelected()) {
            return -1;
        }

        return (Integer.parseInt(m_leftVal.getText()) - 1);
    }

    /**
     * get the rightmost point
     *
     * @return the rightmost value or -1 if 'endpoint' selected
     * @throws NumberFormatException
     */
    public int getRightValue() throws NumberFormatException {
        if (m_rightEndCB.isSelected()) {
            return -1;
        }

        return (Integer.parseInt(m_rightVal.getText()) - 1);
    }
}