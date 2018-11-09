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

import javax.swing.*;
import javax.swing.border.*;


/**
 * This class is used to implement the status bar.
 *
 * @author Ryan Brodie
 */
public class StatusBar extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected JLabel textLbl = new JLabel("");
    protected String m_defText;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Constructor for the StatusBar object
     *
     * @param defLbl the default label to display
     */
    public StatusBar(String defLbl) {
        super();

        if ((defLbl == null) || defLbl.equals("")) {
            defLbl = " ";
        }

        m_defText = defLbl;
        textLbl.setText(m_defText);

        textLbl.setFont(new Font("", Font.PLAIN, 11));
        textLbl.setForeground(Color.black);
        setLayout(new java.awt.BorderLayout());
        textLbl.setBorder(new BevelBorder(1));
        add(textLbl, java.awt.BorderLayout.CENTER);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * set the default text to display
     *
     * @param s the new default
     */
    public void setDefaultText(String s) {
        if (s != null) {
            m_defText = s;
        } else {
            s = "";
        }
    }

    /**
     * Sets the text attribute of the StatusBar object
     *
     * @param s The new text value
     */
    public void setText(String s) {
        textLbl.setText(s);
    }

    /**
     * Gets the text attribute of the StatusBar object
     *
     * @return The text value
     */
    public String getText() {
        return textLbl.getText();
    }

    /**
     * clear the status bar back to the original text
     */
    public void clear() {
        textLbl.setText(m_defText);
    }
}
