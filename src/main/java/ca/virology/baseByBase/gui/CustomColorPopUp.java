package ca.virology.baseByBase.gui;

import ca.virology.lib.util.gui.GuiDefaults;
import ca.virology.lib.util.gui.GuiUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

public class CustomColorPopUp extends JPanel {
    protected int identity;
    protected GuiDefaults m_guiDefaults;
    protected JRadioButton[] rb;
    protected String selectedLetters = "";

    public CustomColorPopUp() {
        super();
        m_guiDefaults = new GuiDefaults();
        initUI();
    }

    protected void initUI() {
        JPanel mainP = new JPanel();
        mainP.setLayout(new BoxLayout(mainP, BoxLayout.Y_AXIS));

        Border queryWindowBorder = GuiUtils.createWindowBorder("Letters for coloring", m_guiDefaults);
        Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        queryWindowBorder = BorderFactory.createCompoundBorder(emptyBorder, queryWindowBorder);
        mainP.setBorder(queryWindowBorder);

        JPanel l1 = new JPanel();
        JPanel l2 = new JPanel();
        JPanel btns = new JPanel();
        JPanel rbtns = new JPanel();

        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        l2.setAlignmentX(Component.LEFT_ALIGNMENT);
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel aa = new JLabel("Select letters to be colored:");

        l1.add(aa);

        ButtonGroup buttonGroup = new ButtonGroup();
        rb = new JRadioButton[20];

        rb[0] = new JRadioButton("A");
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
        rbtns.setLayout(glo);

        for (int i = 0; i < 20; i++) {
            final int on = i;

            rb[i].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setLetters(rb[on].getText());
                }
            });
            rbtns.add(rb[i]);
        }

        btns.add(rbtns);
        mainP.add(l1);
        mainP.add(btns);
        mainP.add(l2);

        add(mainP);

    }

    private void setLetters(String a) {
        selectedLetters += a;
    }


    public String getLetters() {
        return selectedLetters;
    }
}
