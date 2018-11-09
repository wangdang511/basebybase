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
import javax.swing.*;
import javax.swing.border.*;

import ca.virology.lib.util.gui.*;

/**
 * This panel is used to edit user comments.  It displayes a place to enter
 * text, as well as offering a color chooser for the background and foreground
 * colors
 *
 * @author Ryan Brodie
 */
public class CommentEditorPane extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected JTextArea m_comment;
    protected JPanel m_fgp;
    protected JPanel m_bgp;
    protected JTextField nameField;
    protected GuiDefaults m_guiDefaults;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Construct a new comment pane with the given as the default typed text
     *
     * @param text the default text to display
     */
    public CommentEditorPane(String name, String text) {
        this();
        m_guiDefaults = new GuiDefaults();
        m_comment.setText(text);
        nameField.setText(name);
    }

    /**
     * Construct a new commment pane with no default text
     */
    public CommentEditorPane() {
        super();
        m_guiDefaults = new GuiDefaults();
        initUI();
    }


    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the comment typed intot he box
     *
     * @return the comment
     */
    public String getComment() {
        String ret = m_comment.getText();

        if ((ret == null) || ret.equals("")) {
            ret = " ";
        }

        return ret;
    }

    public String getName() {
        return nameField.getText();
    }

    /**
     * Get the current foreground selected
     *
     * @return the foreground color
     */
    public Color getCommentForeground() {
        return m_fgp.getBackground();
    }

    /**
     * Get the current background selected
     *
     * @return the background color
     */
    public Color getCommentBackground() {
        return m_bgp.getBackground();
    }

    /**
     * Set the current background
     *
     * @param bg the new background color
     */
    public void setCommentBackground(Color bg) {
        m_bgp.setBackground(bg);
    }

    /**
     * Set the current foreground
     *
     * @param fg the new foreground color
     */
    public void setCommentForeground(Color fg) {
        m_fgp.setBackground(fg);
    }

    /**
     * Init the UI for this component
     */
    protected void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        Border queryWindowBorder = GuiUtils.createWindowBorder("User Annotation Comments", m_guiDefaults);
        Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

        queryWindowBorder = BorderFactory.createCompoundBorder(emptyBorder, queryWindowBorder);
        mainPanel.setBorder(queryWindowBorder);


        JPanel textPanel = new JPanel();
        textPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        textPanel.setPreferredSize(new java.awt.Dimension(550, 255));

        JLabel nameLabel = new JLabel("Enter Name: ");
        nameField = new JTextField(30);
        textPanel.add(nameLabel);
        textPanel.add(nameField);

        JLabel commentLabel = new JLabel("Enter Annotation: ");
        textPanel.add(commentLabel);

        m_comment = new JTextArea(500, 150);
        JScrollPane csp = new JScrollPane();
        csp.setPreferredSize(new Dimension(500, 150));
        csp.setViewportView(m_comment);
        textPanel.add(csp);

        textPanel.setBorder(BorderFactory.createTitledBorder("Comment"));

        mainPanel.add(textPanel);


        Dimension prefColorSize = new Dimension(500, 35);

        m_fgp = new JPanel();
        m_fgp.setBackground(m_comment.getForeground());
        m_fgp.setOpaque(true);

        JPanel fgb = new JPanel(new BorderLayout());
        fgb.setOpaque(true);
        fgb.setBorder(BorderFactory.createTitledBorder("Text Color"));
        fgb.setPreferredSize(prefColorSize);
        fgb.setMinimumSize(prefColorSize);
        fgb.add(m_fgp);

        mainPanel.add(fgb);

        m_bgp = new JPanel();
        m_bgp.setBackground(m_comment.getBackground());
        m_bgp.setOpaque(true);

        JPanel bgb = new JPanel(new BorderLayout());
        bgb.setOpaque(true);
        bgb.setBorder(BorderFactory.createTitledBorder("Background"));
        bgb.setPreferredSize(prefColorSize);
        bgb.setMinimumSize(prefColorSize);
        bgb.add(m_bgp);

        mainPanel.add(bgb);

        add(mainPanel);

        m_fgp.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ev) {
                JPanel b = (JPanel) ev.getSource();
                Color c = JColorChooser.showDialog(CommentEditorPane.this, "Select Text Color", b.getBackground());

                if (c != null) {
                    setCommentForeground(c);
                }
            }
        });

        m_bgp.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ev) {
                JPanel b = (JPanel) ev.getSource();
                Color c = JColorChooser.showDialog(CommentEditorPane.this, "Select Background Color", b.getBackground());

                if (c != null) {
                    setCommentBackground(c);
                }
            }
        });
    }
}
