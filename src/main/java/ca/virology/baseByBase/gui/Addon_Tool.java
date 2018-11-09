package ca.virology.baseByBase.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

/**
 * The add on tool is the toolbar paired with either the consensus view of the mRNA Expression view
 * It decides which plot type it is paired with and builds a tool bar with the appropriate attributes.
 * <p>
 * A refresh button - refreshes the current plot
 * A close button - closes the consenus/rna plot
 * Vertical Scale slider - for rna plot only.  Adjusts the vertical scale factor
 */

public class Addon_Tool extends JToolBar {
    PrimaryPanel panel;
    boolean consensus;
    JSlider s = null;

    public Addon_Tool(boolean cons, PrimaryPanel pan) {
        panel = pan;
        consensus = cons;

        this.setOrientation(1);
        this.setBackground(Color.white);
        //this.setPreferredSize(new Dimension(panel.hScroll.getWidth(), (int) 2*panel.CONS_HEIGHT-40));
        //JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        this.setBackground(Color.white);
        this.setOpaque(true);
        this.setFloatable(false);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(Color.WHITE);

        this.add(p);

        JButton b;
        b = new JButton(Icons.getInstance().getIcon("RELOAD"));
        b.setToolTipText("Reload");
        b.setMaximumSize(new Dimension(22, 22));
        b.setPreferredSize(new Dimension(22, 22));
        b.setLocation(0, 0);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (consensus) {
                    panel.cons_visible(true);
                } else {
                    panel.rna_visible(true);
                }
                panel.repaint();

            }
        });
        p.add(b);

        b = new JButton(Icons.getInstance().getIcon("X"));
        b.setToolTipText("Close");
        b.setMaximumSize(new Dimension(22, 22));
        b.setPreferredSize(new Dimension(22, 22));
        b.setLocation(0, 0);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (consensus) {
                    panel.cons_visible(false);
                } else {
                    panel.rna_visible(false);
                }
                panel.repaint();
            }
        });
        p.add(b);

        if (!consensus) {

            JLabel l = new JLabel("Divisor");

            final JFormattedTextField slider_num = new JFormattedTextField();

            s = new JSlider(1, 1000);
            s.setValue(10);
            s.setVisible(true);
            s.setPreferredSize(new Dimension(66, 22));
            s.setToolTipText("Divisor");
            s.setBackground(Color.white);
            this.add(s);

            s.addMouseListener(new MouseListener() {

                public void mouseClicked(MouseEvent arg0) {
                    slider_num.setValue(s.getValue());
                    panel.m_rnaDisp.repaint();

                }

                public void mouseEntered(MouseEvent e) {
                    panel.m_rnaDisp.repaint();
                }

                public void mouseExited(MouseEvent e) {
                    panel.m_rnaDisp.repaint();
                }

                public void mousePressed(MouseEvent e) {
                    panel.m_rnaDisp.repaint();
                }

                public void mouseReleased(MouseEvent e) {
                    slider_num.setValue(s.getValue());
                    panel.m_rnaDisp.repaint();
                }

            });


            slider_num.setValue(s.getValue());
            slider_num.setToolTipText("Divisor");
            this.add(slider_num);
            this.add(l);
            slider_num.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    System.out.println("Value Changed: " + slider_num.getValue().toString());
                    s.setValue(Integer.parseInt(slider_num.getValue().toString()));
                    panel.m_rnaDisp.repaint();
                }

            });

        }
        
        this.repaint();
    }


}
