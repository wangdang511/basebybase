package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.*;

import pal.alignment.Alignment;
import pal.alignment.SimpleAlignment;

import pal.datatype.*;

import pal.distance.*;

import pal.gui.TreeComponent;

import pal.misc.*;

import pal.tree.ClusterTree;
import pal.tree.NeighborJoiningTree;
import pal.tree.Tree;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * This presents a phylogenetic tree using the PAL library
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public class TreeFrame extends JFrame {
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final int NJ_TREE = 5005;
    public static final int CLUSTER_COMPLETE = 5007;
    public static final int CLUSTER_SINGLE = 5008;
    public static final int CLUSTER_UPGMA = 5009;
    public static final int CLUSTER_WPGMA = 5010;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new TreeFrame object.
     *
     * @param seqs the sequences to compare
     * @param type the tree type
     * @param name the name of the tree
     */
    public TreeFrame(EditableSequence[] seqs, int type, String name) {
        Identifier[] id = new Identifier[seqs.length];
        String[] seqstrings = new String[seqs.length];

        boolean aa = false;

        for (int i = 0; i < seqs.length; ++i) {
            EditableSequence seq = seqs[i];
            id[i] = new Identifier(seq.getName());
            seqstrings[i] = seq.toString();

            if (seqs[i].getSequenceType() == EditableSequence.AA_SEQUENCE) {
                aa = true;
            }
        }

        DataType dt = null;

        if (aa) {
            dt = new AminoAcids();
        } else {
            dt = new IUPACNucleotides();
        }

        Alignment al = new SimpleAlignment(id, seqstrings, dt);
        DistanceMatrix dm = new JukesCantorDistanceMatrix(al);

        Tree tr = null;

        switch (type) {
            case NJ_TREE:
                if (seqs.length > 2) {
                    tr = new NeighborJoiningTree(dm);
                } else {
                    throw new IllegalArgumentException("You must have at least 3 sequences in your alignment to make a Neighbor Joining Tree.");
                }

                break;

            case CLUSTER_COMPLETE:
                tr = new ClusterTree(dm, ClusterTree.COMPLETE_LINKAGE);

                break;

            case CLUSTER_SINGLE:
                tr = new ClusterTree(dm, ClusterTree.SINGLE_LINKAGE);

                break;

            case CLUSTER_UPGMA:
                tr = new ClusterTree(dm, ClusterTree.UPGMA);

                break;

            case CLUSTER_WPGMA:
                tr = new ClusterTree(dm, ClusterTree.WPGMA);

                break;

            default:
                throw new IllegalArgumentException("unknown tree type");
        }

        TreeComponent tc = new TreeComponent(tr, name);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(tc);
        treePanel.setBorder(BorderFactory.createTitledBorder("Tree View"));

        JPanel p = new JPanel(new BorderLayout());
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
        btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                dispose();
            }
        });

        btns.add(Box.createHorizontalGlue());
        btns.add(close);

        p.add(btns, BorderLayout.SOUTH);
        p.add(treePanel, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setContentPane(p);
        setBounds(10, 10, 500, 500);

        // Position the dialog window
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = getWidth();
        int h = getHeight();
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        setLocation(x, y);
        setResizable(true);
    }
}
