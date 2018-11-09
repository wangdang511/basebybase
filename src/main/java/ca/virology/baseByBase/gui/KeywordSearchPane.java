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
import javax.swing.border.*;
import javax.swing.*;

import ca.virology.lib.util.gui.*;
import ca.virology.lib.io.sequenceData.AnnotationKeys;


/**
 * @author asyed
 */
public class KeywordSearchPane extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////
    protected JTextField name;
    protected JTextArea comment;
    protected PrimaryPanel m_dataPanel;
    protected GuiDefaults m_guiDefaults;
    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * @param text the default text to display
     */
    public KeywordSearchPane(String name, String comment) {
        this.comment.setText(comment);
        m_guiDefaults = new GuiDefaults();
    }

    /**
     * Construct a new commment pane with no default text
     */
    public KeywordSearchPane() {
        super();
        m_guiDefaults = new GuiDefaults();
        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public String getComment() {
        String ret = comment.getText();
        if ((ret == null) || ret.equals("")) {
            ret = " ";
        }
        return ret;
    }

    public void setComment(String comment) {
        this.comment.setText(comment);
    }

    /**
     * Init the UI for this component
     */
    protected void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        Border queryWindowBorder = GuiUtils.createWindowBorder("Keyword Search Criteria", m_guiDefaults);
        Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

        queryWindowBorder = BorderFactory.createCompoundBorder(emptyBorder, queryWindowBorder);
        mainPanel.setBorder(queryWindowBorder);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        textPanel.setPreferredSize(new java.awt.Dimension(450, 150));
        textPanel.setBorder(new javax.swing.border.TitledBorder("Enter Keywords"));

        textPanel.add(new JLabel("Enter keywords (seperate keywords with ';') :"));

        comment = new JTextArea(350, 75);
        JScrollPane commentPane = new JScrollPane();
        commentPane.setPreferredSize(new Dimension(350, 75));
        commentPane.setViewportView(comment);
        textPanel.add(commentPane);

        mainPanel.add(textPanel);

        JPanel notePanel = new JPanel();

        notePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        notePanel.setPreferredSize(new java.awt.Dimension(450, 100));
        notePanel.setBorder(new javax.swing.border.TitledBorder("NOTE:"));
        notePanel.add(new JLabel("                                                            "));
        notePanel.add(new JLabel("    DNA  will  find  DNA  and  DNAse.", Icons.getInstance().getIcon("FORWARD"), SwingConstants.LEFT));
        notePanel.add(new JLabel("    DNA;pol  will  find  all  primers  with  DNA  or  pol  keywords.", Icons.getInstance().getIcon("FORWARD"), SwingConstants.LEFT));

        mainPanel.add(notePanel);
        this.add(mainPanel);

    }

}
