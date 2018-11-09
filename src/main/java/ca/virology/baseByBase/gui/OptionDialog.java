package ca.virology.baseByBase.gui;

//Modified from http://www.coderanch.com/t/338255/GUI/java/JDialog-Multiple-inputs

import ca.virology.lib.util.gui.GuiDefaults;
import ca.virology.lib.util.gui.GuiUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Color;

public class OptionDialog extends JPanel {
    protected int identity;
    protected JSlider slider;
    protected GuiDefaults m_guiDefaults;
    protected JRadioButton[] rb;
    protected String aa = "";
    protected Color commentColor;

    public OptionDialog() {
        super();
        m_guiDefaults = new GuiDefaults();
        initUI();
    }

    /*#######################################################################>>>>>
    * Purpose: Initializes the "AA in MSA columns" frame.
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    protected void initUI() {
        JPanel mainP = new JPanel();
        mainP.setLayout(new BoxLayout(mainP, BoxLayout.Y_AXIS));

        Border queryWindowBorder = GuiUtils.createWindowBorder("Search Parameters", m_guiDefaults);
        Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        queryWindowBorder = BorderFactory.createCompoundBorder(emptyBorder, queryWindowBorder);
        mainP.setBorder(queryWindowBorder);

        //Create frame objects
        JPanel l1 = new JPanel(); //line 1
        JPanel l2 = new JPanel(); //line 2
        JPanel l3 = new JPanel(); //line 3
        JPanel btns = new JPanel();  //buttons
        JPanel slide = new JPanel(); //slider
        JPanel rbtns = new JPanel(); //radio buttons

        //Create border object for comment color section
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.setBorder(BorderFactory.createTitledBorder("Current Comment Color"));
        colorPanel.setPreferredSize(new Dimension(220, 50));
        colorPanel.setMinimumSize(new Dimension(220, 50));

        //Create color box for comment color section
        final JPanel colorChooser = new JPanel();
        colorChooser.setBackground(new Color(33, 188, 255));

        //Create change color button for comment color section
        JButton colorButton = new JButton();
        colorButton.setText("Change");

        //Create a grid layout for the change color section
        GridLayout glol3 = new GridLayout(1, 2);
        colorPanel.setLayout(glol3);
        colorPanel.add(colorChooser);
        colorPanel.add(colorButton);

        //Align the frame components
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        l2.setAlignmentX(Component.LEFT_ALIGNMENT);
        l3.setAlignmentX(Component.LEFT_ALIGNMENT);
        slide.setAlignmentX(Component.LEFT_ALIGNMENT);
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel aa = new JLabel("Single Amino Acid:");
        final JLabel iden = new JLabel("Identity match in columns: 50%");
        slider = new JSlider(0, 100, 50);

        //Add objects to the frame objects
        l1.add(aa);
        l2.add(iden);
        l3.add(colorPanel);
        slide.add(slider);

        //Create radio buttons
        ButtonGroup buttonGroup = new ButtonGroup();
        rb = new JRadioButton[20];

        rb[0] = new JRadioButton("A", true);
        rb[1] = new JRadioButton("C");
        rb[2] = new JRadioButton("D");
        rb[3] = new JRadioButton("E");
        rb[4] = new JRadioButton("F");
        rb[5] = new JRadioButton("G");
        rb[6] = new JRadioButton("H");
        rb[7] = new JRadioButton("I");
        rb[8] = new JRadioButton("K");
        rb[9] = new JRadioButton("L");
        rb[10] = new JRadioButton("M");
        rb[11] = new JRadioButton("N");
        rb[12] = new JRadioButton("P");
        rb[13] = new JRadioButton("Q");
        rb[14] = new JRadioButton("R");
        rb[15] = new JRadioButton("S");
        rb[16] = new JRadioButton("T");
        rb[17] = new JRadioButton("V");
        rb[18] = new JRadioButton("W");
        rb[19] = new JRadioButton("Y");

        GridLayout glo = new GridLayout(5, 4);
        setAA("A");
        rbtns.setLayout(glo);

        //Add button listeners
        for (int i = 0; i < 20; i++) {
            buttonGroup.add(rb[i]);
            final int on = i;

            rb[i].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setAA(rb[on].getText());
                }
            });
            rbtns.add(rb[i]);
        }

        //Add frame objects to main frame
        btns.add(rbtns);
        mainP.add(l1);
        mainP.add(btns);
        mainP.add(l2);
        mainP.add(slide);
        mainP.add(l3);

        //Initialize the slider value
        setIdentity(slider.getValue());

        //Listen to the slider
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                iden.setText(String.valueOf("Identity match in columns: " + slider.getValue() + "%"));
                identity = slider.getValue();
            }
        });

        //Initialize comment color
        setCommentColor(new Color(33, 188, 255));

        //Listen to the "Change" button
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commentColor = JColorChooser.showDialog(null, "Choose a Color", new Color(33, 188, 255));

                if (commentColor == null) {
                    commentColor = new Color(33, 188, 255);
                }
                colorChooser.setBackground(commentColor);
            }
        });

        add(mainP);
    }

    private void setAA(String a) {
        aa = a;
    }

    public String getAA() {
        return aa;
    }

    /*#######################################################################>>>>>
    * Purpose: Initializes the slider value (called only when frame is created).
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    public void setIdentity(int id) {
        this.identity = id;
    }

    /*#######################################################################>>>>>
    * Purpose: Get slider value from other files.
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    public int getIdentity() {
        return identity;
    }

    /*#######################################################################>>>>>
    * Purpose: Initializes the comment color (called only when frame is created).
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    public void setCommentColor(Color color) {
        this.commentColor = color;
    }

    //todo
     /*public Color getColor(){
        return color;
    }*/

    /*#######################################################################>>>>>
    * Purpose: Get comment color from other files.
    * Edited - Summer 2015
    * #######################################################################>>>>>*/
    public Color getCommentColor() {
        return commentColor;
    }
}
