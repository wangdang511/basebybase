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
import java.util.ArrayList;

import javax.swing.*;

import pal.alignment.Alignment;
import pal.alignment.SimpleAlignment;
import pal.datatype.AminoAcids;
import pal.datatype.DataType;
import pal.datatype.IUPACNucleotides;
import pal.distance.DistanceMatrix;
import pal.distance.JukesCantorDistanceMatrix;
import pal.gui.TreeComponent;
import pal.misc.Identifier;
import pal.tree.ClusterTree;
import pal.tree.NeighborJoiningTree;
import pal.tree.Tree;

import ca.virology.lib.io.sequenceData.EditableSequence;

import javax.swing.event.MenuKeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.awt.geom.AffineTransform;


/**
 * This presents a phylogenetic tree using the PAL library.
 *
 * @author Sangeeta Neti
 */
public class TreeDialog extends JDialog implements MouseMotionListener, MouseListener {
    //	~ Instance fields ////////////////////////////////////////////////////////

    public static final int NJ_TREE = 5005;
    public static final int CLUSTER_COMPLETE = 5007;
    public static final int CLUSTER_SINGLE = 5008;
    public static final int CLUSTER_UPGMA = 5009;
    public static final int CLUSTER_WPGMA = 5010;

    protected String m_name;
    protected Tree tr = null;
    final ArrayList list = new ArrayList();
    protected boolean is_closed = false;
    public TreeComponent tc = null;
    float zoom = 2.0f;
    int x, y, mx, my, width, height, Cx, Cy;
    Dimension dimension;
    ZoomPanel zp;


    public TreeDialog(EditableSequence[] seqs, int type, String name, String filename) {
        m_name = name;
        Identifier[] id = new Identifier[seqs.length];
        String[] seqstrings = new String[seqs.length];

        boolean aa = false;
        boolean tooLong = false;

        for (int i = 0; i < seqs.length; ++i) {
            EditableSequence seq = seqs[i];

            //	Truncating the sequence names if it contains more than 40 characters. This is done because long names makes mess.
            String seqName = seq.getName();
            if (seqName.length() < 40) {
                id[i] = new Identifier(seq.getName());
            } else {
                id[i] = new Identifier(seqName.substring(0, 40));
                tooLong = true;
            }

            seqstrings[i] = seq.toString();

            if (seqs[i].getSequenceType() == EditableSequence.AA_SEQUENCE) {
                aa = true;
            }
        }
        //todo: this was a quick fix for the problem of shortening the names until some were the same which has causes an bunch of error when ordering sequences after tree is drawn, need better implementation
        if (tooLong) {
            int seqLengthError = JOptionPane.showConfirmDialog(getContentPane(), "WARNING: Some of the sequence names are more than 40 characters \n This may cause an error!", "Warning Message", JOptionPane.OK_CANCEL_OPTION);
            if (seqLengthError == JOptionPane.CANCEL_OPTION) {
                return;  //throws null pointer exception
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

        // Sorting the sequence names to match the tree. The sorted sequences are stored in "list".
        ArrayList newlist = new ArrayList();
        for (int i = 0; i < tr.getIdCount(); ++i) {

            for (int j = 0; j < seqs.length; j++) {
                System.out.println("SEQ NUMBER: " + j);
                String identifierName = tr.getExternalNode(i).getIdentifier().getName();
                String seqName = null;

                // Sequence names were truncated to 40 characters in the tree view to avoid the mess.
                if (seqs[j].getName().length() < 40)
                    seqName = seqs[j].getName();
                else
                    seqName = seqs[j].getName().substring(0, 40);

                if (identifierName.equals(seqName)) {
                    System.out.println("adding " + seqName);
                    newlist.add(seqs[j]);
                }
            }
        }
        // Reverse the order
        int h = newlist.size() - 1;
        for (int k = 0; k < newlist.size(); k++) {
            System.out.println("NEW SEQ NUMBER: " + k);
            list.add(k, newlist.get(h--));
        }

        setModal(true);
        initUI(filename);

    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the approval status
     *
     * @return the approval status
     */
    public boolean getApproval() {
        return is_closed;
    }

    /**
     * set the approval status
     *
     * @param newval the approval status
     */
    protected void setApproval(boolean newval) {
        is_closed = newval;
    }

    /**
     * get the approval status
     *
     * @return the approval status
     */
    public java.util.ArrayList getSequences() {
        return list;
    }

    /**
     * Init the UI for this component
     */
    protected void initUI(String filename) {
        tc = new TreeComponent(tr, m_name);
        tc.setMode(TreeComponent.NORMAL_BW);
        zp = new ZoomPanel(1.0);
        zp.setLayout(new BorderLayout());
        zp.add(tc);
        zp.setBackground(Color.white);
        zp.addMouseMotionListener(this);
        zp.addMouseListener(this);


        JPanel p = new JPanel(new BorderLayout());
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
        btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setApproval(true);
                dispose();
            }
        });
        btns.add(new JLabel(filename));
        btns.add(Box.createHorizontalGlue());
        btns.add(close);

        // Menu
        JMenuBar mb = new JMenuBar();
        JMenu menu = null;
        JMenuItem mi = null;


        menu = new JMenu("View");
        menu.setMnemonic(MenuKeyEvent.VK_F);

        mi = new JMenuItem("Full Screen", MenuKeyEvent.VK_N);
        mi.setIcon(Icons.getInstance().getIcon("MAXIMIZE"));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                Dimension dim1 = Toolkit.getDefaultToolkit().getScreenSize();
                setLocation(0, 0);
                setSize(dim1.width - 50, dim1.height - 50);
            }
        });
        menu.add(mi);

        mi = new JMenuItem("Restore", MenuKeyEvent.VK_N);
        mi.setIcon(Icons.getInstance().getIcon("RESTORE"));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                int x = (dimension.width - width) / 2;
                int y = (dimension.height - height) / 2;
                setLocation(x, y);
                setSize(width, height);
            }
        });
        menu.add(mi);

        mi = new JMenuItem("Zoom In", MenuKeyEvent.VK_N);
        mi.setIcon(Icons.getInstance().getIcon("ZOOMIN"));
        mi.addActionListener(new ZoomAction(zp, 0.25));
        menu.add(mi);

        mi = new JMenuItem("Zoom Out", MenuKeyEvent.VK_N);
        mi.setIcon(Icons.getInstance().getIcon("ZOOMOUT"));
        mi.addActionListener(new ZoomAction(zp, -0.25));
        menu.add(mi);
        menu.addSeparator();

        mi = new JMenuItem("Close", MenuKeyEvent.VK_N);
        mi.setIcon(Icons.getInstance().getIcon("CLOSE"));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setApproval(true);
                dispose();
            }
        });
        menu.add(mi);

        mb.add(menu);
        setJMenuBar(mb);
        p.add(btns, BorderLayout.SOUTH);
        p.add(new JScrollPane(zp), BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setContentPane(p);
        setBounds(10, 10, 800, 500);

        // Position the dialog window
        dimension = Toolkit.getDefaultToolkit().getScreenSize();
        width = getWidth();
        height = getHeight();
        int x = (dimension.width - width) / 2;
        int y = (dimension.height - height) / 2;
        setLocation(x, y);
        setResizable(true);
    }

    public void mousePressed(MouseEvent e) {
        mx = e.getX();
        my = e.getY();
        e.consume();
    }

    public void mouseDragged(MouseEvent e) {

        int new_mx = e.getX();
        int new_my = e.getY();

        x += (new_mx - mx);
        y += (new_my - my);

        // update our data
        mx = new_mx;
        my = new_my;

        repaint();
        e.consume();

    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }


    public void paint(Graphics g) {
        super.paint(g); // clears background
        tc.setLocation(x, y);
    }

    class ZoomPanel extends JPanel {
        protected double zoom;

        public ZoomPanel(double initialZoom) {
            super(new FlowLayout());
            setName("Zoom Panel");
            zoom = initialZoom;
        }

        public void paint(Graphics g) {
            super.paintComponent(g); // clears background
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform backup = g2.getTransform();
            g2.scale(zoom, zoom);
            super.paint(g);
            g2.setTransform(backup);
        }

        public boolean isOptimizedDrawingEnabled() {
            return false;
        }

        public Dimension getPreferredSize() {
            Dimension unzoomed = getLayout().preferredLayoutSize(this);
            Dimension zoomed = new Dimension((int) ((double) unzoomed.width * zoom), (int) ((double) unzoomed.height * zoom));
            return zoomed;
        }

        public void setZoom(double newZoom) throws PropertyVetoException {
            if (newZoom <= 0.0) {
                throw new PropertyVetoException("Zoom must be positive-valued", new PropertyChangeEvent(this, "zoom", new Double(zoom), new Double(newZoom)));
            }
            double oldZoom = zoom;
            if (newZoom != oldZoom) {
                Dimension oldSize = getPreferredSize();
                zoom = newZoom;
                Dimension newSize = getPreferredSize();
                firePropertyChange("zoom", oldZoom, newZoom);
                firePropertyChange("preferredSize", oldSize, newSize);
                revalidate();
                repaint();
            }
        }

        public double getZoom() {
            return zoom;
        }
    }

    class ZoomAction implements ActionListener {

        protected double amount;
        protected ZoomPanel zp;


        public ZoomAction(ZoomPanel zp, double amount) {
            this.amount = amount;
            this.zp = zp;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                zp.setZoom(zp.getZoom() + amount);
            } catch (PropertyVetoException ex) {
                JOptionPane.showMessageDialog((Component) e.getSource(), "Couldn't change zoom: " + ex.getMessage());
            }
        }
    }

}





