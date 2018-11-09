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

import ca.virology.lib.io.sequenceData.AnnotationKeys;


/**
 * This panel is used to edit Primer Annotations.  It displayes a Textfields for all Annotation Types
 *
 * @author Neil Hillen
 */
public class PrimerEditorPane extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////
    protected JTextField name;
    protected JTextField segmentsequence;
    protected JTextField meltingtemp;
    protected JTextField fridge;
    protected JTextArea comment;


    //~ Constructors ///////////////////////////////////////////////////////////
    protected PrimaryPanel m_dataPanel;

    /**
     * Construct a new comment pane with the given as the default typed text
     *
     * @param text the default text to display
     */
    public PrimerEditorPane(String name, String segmentsequence, String meltingtemp, String fridge, String comment) {
        this();
        this.name.setText(name);
        this.segmentsequence.setText(segmentsequence);
        this.meltingtemp.setText(meltingtemp);
        this.fridge.setText(fridge);
        this.comment.setText(comment);
    }

    /**
     * Construct a new commment pane with no default text
     */
    public PrimerEditorPane() {
        super();
        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public String getName() {
        String ret = name.getText();
        if ((ret == null) || ret.equals("")) {
            ret = " ";
        }
        return ret;
    }

    public String getSeqmentSequence() {
        String ret = segmentsequence.getText();
        if ((ret == null) || ret.equals("")) {
            ret = " ";
        }
        return ret;
    }

    public String getMeltingTemp() {
        String ret = meltingtemp.getText();
        if ((ret == null) || ret.equals("")) {
            ret = " ";
        }
        return ret;
    }

    public String getFridgeLocation() {
        String ret = fridge.getText();
        if ((ret == null) || ret.equals("")) {
            ret = " ";
        }
        return ret;
    }

    public String getComment() {
        String ret = comment.getText();
        if ((ret == null) || ret.equals("")) {
            ret = " ";
        }
        return ret;
    }


    public void setName(String name) {
        this.name.setText(name);
    }

    public void setSegmentSequence(String segmentsequence) {
        this.segmentsequence.setText(segmentsequence);
    }

    public void setMeltingTemp(String meltingtemp) {
        /*if (meltingtemp == ""){
            this.meltingtemp.setText(PrimaryPanel.getPrimerTemp());
		} else {*/
        this.meltingtemp.setText(meltingtemp);
        //}
    }

    public void setFridgeLocation(String fridge) {
        this.fridge.setText(fridge);
    }

    public void setComment(String comment) {
        this.comment.setText(comment);
    }


    /**
     * Init the UI for this component
     */
    protected void initUI() {
        name = new JTextField(50);
        segmentsequence = new JTextField(50);
        meltingtemp = new JTextField(50);
        fridge = new JTextField(50);
        comment = new JTextArea(50, 50);


        // init the components
        this.setLayout(new GridBagLayout());//new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createTitledBorder("Primer Details"));
        this.setPreferredSize(new Dimension(600, 250));
        //this.setMinimumSize(new Dimension(400, 200));

        JPanel nameandvaluelist = new JPanel();

        nameandvaluelist.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;


        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        nameandvaluelist.add(new JLabel("Name:"), c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        nameandvaluelist.add(name, c);

        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_START;
        nameandvaluelist.add(new JLabel("Sequence (5` -> 3`) :"), c);
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_END;
        nameandvaluelist.add(segmentsequence, c);


        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.LINE_START;
        nameandvaluelist.add(new JLabel("Melting Temp:"), c);
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.LINE_END;
        nameandvaluelist.add(meltingtemp, c);


        c.gridx = 0;
        c.gridy = 3;
        c.anchor = GridBagConstraints.LINE_START;
        nameandvaluelist.add(new JLabel("Fridge Location:"), c);
        c.gridx = 1;
        c.gridy = 3;
        c.anchor = GridBagConstraints.LINE_END;
        nameandvaluelist.add(fridge, c);

        c.gridx = 0;
        c.gridy = 4;
        c.anchor = GridBagConstraints.PAGE_START;
        nameandvaluelist.add(new JLabel("Comment:"), c);
        c.gridx = 1;
        c.gridy = 4;
        c.ipady = 50;
        c.ipadx = 300;
        c.anchor = GridBagConstraints.LINE_END;
        nameandvaluelist.add(new JScrollPane(comment), c);


        this.add(nameandvaluelist);

    }
}
