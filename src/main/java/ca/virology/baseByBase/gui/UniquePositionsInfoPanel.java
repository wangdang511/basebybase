package ca.virology.baseByBase.gui;

import ca.virology.lib.io.MultiFileFilter;
import ca.virology.lib.io.sequenceData.FeaturedSequence;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;


/**
 * This panel provides a synopsis of the differences between each sequence
 * Ignores gaps for the sequence on the left
 */
public class UniquePositionsInfoPanel
        extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected FeaturedSequence[] m_seqs;
    protected double[][] m_value;
    final ValLabel[][] lMatrix;
    final JButton[] hBtns;
    final JButton[] vBtns;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new AlignmentInfoPanel object.
     *
     * @param seqs the sequences to summarize
     */
    public UniquePositionsInfoPanel(FeaturedSequence[] seqs) {
        m_seqs = seqs;

        m_value = new double[m_seqs.length][m_seqs.length];

        lMatrix = new ValLabel[m_seqs.length][m_seqs.length];
        hBtns = new JButton[m_seqs.length];
        vBtns = new JButton[m_seqs.length];

        calcVal();
        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Select a row in the matrix
     *
     * @param row the row
     */
    protected void selectRow(int row) {
        for (int j = 0; j < m_seqs.length; ++j) {
            for (int k = 0; k < m_seqs.length; ++k) {
                if (j == row) {
                    lMatrix[j][k].setVSel(true);
                } else {
                    lMatrix[j][k].setVSel(false);
                }
            }
        }
    }

    /**
     * select a column in the matrix
     *
     * @param col the column
     */
    protected void selectCol(int col) {
        for (int j = 0; j < m_seqs.length; ++j) {
            for (int k = 0; k < m_seqs.length; ++k) {
                if (k == col) {
                    lMatrix[j][k].setHSel(true);
                } else {
                    lMatrix[j][k].setHSel(false);
                }
            }
        }
    }

    /**
     * calculate the diffs
     */
    protected void calcVal() {

        for (int i = 0; i < m_seqs.length; ++i) {
            for (int j = 0; j < m_seqs.length; ++j) {
                //iterate through
                int val = 0;
                for (int k = 0; k < m_seqs[i].length(); k++) {
                    if (m_seqs[i].charAt(k) != '-') {
                        if (m_seqs[i].charAt(k) != m_seqs[j].charAt(k)) {
                            val++;
                        }
                    }
                }
                m_value[i][j] = val;
            }
        }

    }

    /**
     * init the gui
     */
    protected void initUI() {
        //
        // listeners
        //
        ActionListener hlist =
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        JButton b = (JButton) ev.getSource();
                        int i = Integer.parseInt(b.getText());
                        i--;
                        selectCol(i);
                    }
                };

        ActionListener vlist =
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        JButton b = (JButton) ev.getSource();
                        int i = Integer.parseInt(b.getText());
                        i--;
                        selectRow(i);
                    }
                };

        //
        // seq info panel
        //
        final JPanel seqInfoPanel = new JPanel();
        seqInfoPanel.setLayout(new BoxLayout(seqInfoPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < m_seqs.length; ++i) {
            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());

            JLabel title = new JLabel((i + 1) + ": " + m_seqs[i].getName());
            JLabel info = new JLabel();
            int seqLength = m_seqs[i].sequenceLength();

            StringBuffer subInfo = new StringBuffer("(");
            subInfo.append(seqLength + " bases/residues");
            subInfo.append(")");

            Font f = title.getFont().deriveFont((float) (title.getFont().getSize() - 1));
            info.setFont(f);
            info.setText(subInfo.toString());

            p.add(title, BorderLayout.NORTH);
            p.add(info, BorderLayout.CENTER);

            final int myIndex = i;
            p.addMouseListener(
                    new MouseAdapter() {
                        public void mouseEntered(MouseEvent ev) {
                            p.setBackground(new Color(200, 200, 255));
                            p.repaint();
                        }

                        public void mouseExited(MouseEvent ev) {
                            p.setBackground(seqInfoPanel.getBackground());
                            p.repaint();
                        }

                        public void mouseClicked(MouseEvent ev) {
                            selectRow(myIndex);
                            selectCol(myIndex);
                        }
                    });

            seqInfoPanel.add(p);
        }

        //
        // percent identity matrix
        // 
        JPanel matrixPanel = new JPanel(new GridLayout(m_seqs.length + 1, m_seqs.length + 1));
        java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);

        matrixPanel.add(new JLabel());

        for (int i = 0; i < m_seqs.length; ++i) {
            hBtns[i] = new JButton((i + 1) + "");
            matrixPanel.add(hBtns[i]);
            hBtns[i].addActionListener(hlist);
        }

        for (int i = 0; i < m_seqs.length; ++i) {
            vBtns[i] = new JButton((i + 1) + "");
            vBtns[i].addActionListener(vlist);
        }

        for (int i = 0; i < m_seqs.length; ++i) {
            matrixPanel.add(vBtns[i]);

            for (int j = 0; j < m_seqs.length; ++j) {
                final int myI = i;
                final int myJ = j;
                lMatrix[i][j] = new ValLabel(format.format(m_value[i][j]));
                lMatrix[i][j].setBackground(Color.white);
                lMatrix[i][j].setOpaque(true);
                lMatrix[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                matrixPanel.add(lMatrix[i][j]);
                lMatrix[i][j].addMouseListener(
                        new MouseAdapter() {
                            public void mouseClicked(MouseEvent ev) {
                                selectRow(myI);
                                selectCol(myJ);
                            }
                        });
            }
        }

        //
        // final layout
        //
        setLayout(new BorderLayout());

        JScrollPane left = new JScrollPane(seqInfoPanel);
        JScrollPane right = new JScrollPane(matrixPanel);

        left.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Sequences"), left.getBorder()));
        right.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Position Differences"), left.getBorder()));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(150);
        add(split, BorderLayout.CENTER);

    }

    /**
     * save the text to disk
     */
    protected void saveFlatFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Alignment Information");

        MultiFileFilter ff1 = new MultiFileFilter("Tab Delimited Plain Text");
        ff1.addExtension("");
        fc.addChoosableFileFilter(ff1);
        fc.setFileFilter(ff1);

        try {
            if (fc.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(f));

                java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance();
                format.setMaximumFractionDigits(2);

                for (int j = 0; j < m_seqs.length; ++j) {
                    String num = (j + 1) + "";
                    out.write("[" + num + "]" + "\t", 0, num.length() + 3);
                }

                out.newLine();

                for (int j = 0; j < m_seqs.length; ++j) {
                    for (int k = 0; k < m_seqs.length; ++k) {
                        String pct = format.format(m_value[j][k]);
                        out.write(pct + "\t", 0, pct.length() + 1);
                    }
                    out.write(m_seqs[j].getName() + " [" + (j + 1) + "]" + "\t", 0, m_seqs[j].getName().length() + 5);
                    out.newLine();
                }
                out.flush();
                out.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //~ Inner Classes //////////////////////////////////////////////////////////

    protected class ValLabel
            extends JLabel {
        protected boolean m_hSel;
        protected boolean m_vSel;

        public ValLabel(String s) {
            super(s);
        }

        public void setHSel(boolean sel) {
            m_hSel = sel;
            repaint();
        }

        public void setVSel(boolean sel) {
            m_vSel = sel;
            repaint();
        }

        public Color getBackground() {
            if (m_hSel) {
                if (m_vSel) {
                    return new Color(255, 200, 200);
                } else {
                    return new Color(200, 200, 255);
                }
            } else {
                if (m_vSel) {
                    return new Color(200, 200, 255);
                } else {
                    return Color.white;
                }
            }
        }
    }
}
