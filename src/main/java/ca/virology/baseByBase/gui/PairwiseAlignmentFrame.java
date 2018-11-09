package ca.virology.baseByBase.gui;

import ca.virology.baseByBase.data.*;

import ca.virology.lib.io.sequenceData.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * This window presents a pairwise alignment of a set of selected
 * sequences.  This is a textual report.
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class PairwiseAlignmentFrame
        extends JFrame {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected EditableSequence[] m_seqs;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new PairwiseAlignmentFrame object.
     *
     * @param seqs The sequences to compare
     */
    public PairwiseAlignmentFrame(EditableSequence[] seqs) {
        super("Pairwise Alignment Report");
        m_seqs = seqs;
        init();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Init the window
     */
    protected void init() {
        JTextArea text = new JTextArea();
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        for (int i = 0; i < m_seqs.length; ++i) {
            for (int j = 0; j < m_seqs.length; ++j) {
                if (j >= i) {
                    continue;
                }

                text.append(getPairwiseInfo(m_seqs[i], m_seqs[j]));
                text.append("\n---------------\n");
            }
        }

        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));

        JButton b = new JButton("Close");
        btns.add(Box.createHorizontalGlue());
        btns.add(b);
        btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        b.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        dispose();
                    }
                });

        JPanel p = new JPanel(new BorderLayout());

        JScrollPane scroll = new JScrollPane(text);
        scroll.getVerticalScrollBar()
                .setValue(0);

        p.add(scroll, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setContentPane(p);

        setSize(600, 500);

        // Position the dialog window
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = getWidth();
        int h = getHeight();
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        setLocation(x, y);
    }

    /**
     * get a string representing the pairwise report for two sequences.
     *
     * @param s1 the first sequence
     * @param s2 the second sequence
     * @return the pariwise report
     */
    protected String getPairwiseInfo(
            EditableSequence s1,
            EditableSequence s2) {
        StringBuffer buff = new StringBuffer();

        int nlen =
                Math.max(
                        s1.getName().length(),
                        s2.getName().length());
        int sp1 = nlen - s1.getName()
                .length() + 1;
        int sp2 = nlen - s2.getName()
                .length() + 1;

        int slen = Math.max(
                s1.length(),
                s2.length());

        int lineLength = 80 - nlen - 1;

        StringBuffer l1 = null;
        StringBuffer l2 = null;
        StringBuffer l3 = null;

        //buff.append("Score = ??\n");
        buff.append("Length of alignment = " + slen + "\n");
        buff.append("Sequnece " + s1.getName() + " length: " +
                s1.sequenceLength() + "\n");
        buff.append("Sequence " + s2.getName() + " length: " +
                s2.sequenceLength() + "\n\n");

        for (int i = 0; i < (slen + lineLength); i += lineLength) {
            l1 = new StringBuffer(s1.getName() + getSpaces(sp1));
            l2 = new StringBuffer(getSpaces(nlen + 1));
            l3 = new StringBuffer(s2.getName() + getSpaces(sp2));

            for (int j = 0; j < lineLength; ++j) {
                int k = i + j;
                boolean got1 = false;
                boolean got2 = false;
                char c1 = 0;
                char c2 = 0;

                if (k < s1.length()) {
                    c1 = s1.charAt(k);
                    got1 = true;
                }

                if (k < s2.length()) {
                    c2 = s2.charAt(k);
                    got2 = true;
                }

                if (got1) {
                    l1.append(c1);
                } else {
                    l1.append(' ');
                }

                if (got1 && got2) {
                    l2.append(getCompChar(
                            c1,
                            c2,
                            s1.getSequenceType()));
                } else {
                    l2.append(' ');
                }

                if (got2) {
                    l3.append(c2);
                } else {
                    l3.append(' ');
                }
            }

            buff.append(l1.toString() + "\n");
            buff.append(l2.toString() + "\n");
            buff.append(l3.toString() + "\n\n");
        }

        return buff.toString();
    }

    /**
     * return a string of spaces
     *
     * @param num the number of spaces
     * @return a string of 'num' spaces
     */
    protected String getSpaces(int num) {
        StringBuffer b = new StringBuffer();

        for (int i = 0; i < num; ++i) {
            b.append(' ');
        }

        return b.toString();
    }

    /**
     * get the character that would go between two lines in the alignment
     *
     * @param c1      the top char
     * @param c2      the bottom char
     * @param seqType the sequence type (aa, dna)
     * @return the char used to compare
     */
    protected char getCompChar(
            char c1,
            char c2,
            int seqType) {
        if (seqType == EditableSequence.AA_SEQUENCE) {
            if ((c1 == c2) && ((c1 != '-') && (c2 != '-'))) {
                return '|';
            } else if (((c1 != '-') && (c2 != '-')) &&
                    (AminoAcid.getPam250Score(
                            AminoAcid.valueOf(c1),
                            AminoAcid.valueOf(c2)) > 0)) {
                return '.';
            } else {
                return ' ';
            }
        } else {
            if ((c1 == c2) && ((c1 != '-') && (c2 != '-'))) {
                return '|';
            } else {
                return ' ';
            }
        }
    }
}
