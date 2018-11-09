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

import java.text.*;

import javax.swing.*;
import javax.swing.event.*;


/**
 * This panel provides preferences for the user to select the output  format of
 * the image export for their alignment
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public class ExportImagePanel extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected int m_maxLength;
    protected JComboBox m_typCbox = new JComboBox();
    protected JTextField m_startField = new JTextField(10);
    protected JTextField m_stopField = new JTextField(10);
    protected final JLabel m_wLabel = new JLabel("800");
    protected final JLabel m_sLabel = new JLabel("10");
    protected final JLabel m_scalingLabel = new JLabel("1");
    protected final JSlider m_widthSlider = new JSlider(JSlider.HORIZONTAL, 100, 3000, 800);
    protected final JSlider m_spacingSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 10);
    protected final JSlider m_scalingSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new ExportImagePanel object.
     *
     * @param hwidth    the width of the image by default
     * @param maxLength the max length requestable
     */
    public ExportImagePanel(double hwidth, int maxLength) {
        setLayout(new BorderLayout());

        m_maxLength = maxLength;
        m_startField.setText(0 + "");
        m_stopField.setText(maxLength + "");

        m_typCbox.addItem("jpg");
        m_typCbox.addItem("png");

        JPanel labels = new JPanel(new GridLayout(6, 1));
        JPanel data = new JPanel(new GridLayout(6, 1));
        JPanel readout = new JPanel(new GridLayout(6, 1));

        data.add(m_typCbox);
        data.add(m_startField);
        data.add(m_stopField);
        data.add(m_widthSlider);
        data.add(m_spacingSlider);
        data.add(m_scalingSlider);

        labels.add(new JLabel("Image Type"));
        labels.add(new JLabel("Start Pos"));
        labels.add(new JLabel("Stop Pos"));
        labels.add(new JLabel("Image Width "));
        labels.add(new JLabel("Spacing "));
        labels.add(new JLabel("Scaling "));

        readout.add(new JLabel("               "));
        readout.add(new JLabel(""));
        readout.add(new JLabel(""));
        readout.add(m_wLabel);
        readout.add(m_sLabel);
        readout.add(m_scalingLabel);

        add(labels, BorderLayout.WEST);
        add(data, BorderLayout.CENTER);
        add(readout, BorderLayout.EAST);

        m_widthSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                m_wLabel.setText(m_widthSlider.getValue() + "");
            }
        });

        m_spacingSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                m_sLabel.setText(m_spacingSlider.getValue() + "");
            }
        });

        final DecimalFormat numberFormat = new DecimalFormat();
        numberFormat.setMaximumFractionDigits(1);
        m_scalingSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                m_scalingLabel.setText(numberFormat.format((double) m_scalingSlider.getValue() / 10));
            }
        });
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the image width requested
     *
     * @return the image width
     */
    public double getImageWidth() {
        return (double) m_widthSlider.getValue();
    }

    /**
     * get the spacing between 'lines'
     *
     * @return the spacing
     */
    public double getSpacing() {
        return (double) m_spacingSlider.getValue();
    }

    /**
     * get the scaling factor
     *
     * @return the scaling factor
     */
    public double getScalingFactor() {
        return ((double) m_scalingSlider.getValue() / 10);
    }

    /**
     * get the start position to export
     *
     * @return the start position
     */
    public int getStart() {
        return (int) Double.parseDouble(m_startField.getText());
    }

    /**
     * get the stop position
     *
     * @return the stop position
     */
    public int getStop() {
        return (int) Double.parseDouble(m_stopField.getText());
    }

    /**
     * get the image type to export
     *
     * @return the image type
     */
    public String getImageType() {
        return (String) m_typCbox.getSelectedItem();
    }
}