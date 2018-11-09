package ca.virology.baseByBase.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Launch given URL from default browser
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ptolemy.plot.Plot;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.util.gui.GuiDefaults;
import ca.virology.lib.util.gui.GuiUtils;
import ca.virology.lib.util.gui.UITools;
import ca.virology.lib.util.gui.Utils;

/*#######################################################################>>>>>
* Purpose: This is an ABSTRACT class for all the base content graphs that can be plotted in BASE-BY-BASE.
* Written: Winter 2009
* #######################################################################>>>>>*/
public abstract class AbstractBaseContentGraph extends JFrame {
    protected AbstractBaseContentGraph theFrame = this;
    protected JPanel mainPanel;
    protected JPanel queryPanel;
    protected JPanel graphAndCrtlPanel;
    protected Plot theGraph;
    protected JPanel crtlPanel;
    private FeaturedSequence[] seqs;
    private FeaturedSequence[] backupSeqs; //Store unmodified seqs in backup variable
    private JTextField windowSizeText;
    private JTextField stepSizeText;
    private JCheckBox a;
    private JCheckBox c;
    private JCheckBox g;
    private JCheckBox t;
    private boolean deleteGap = false;
    private int windowSize = 300;
    private int stepSize = 100;
    private int height = 600;
    private int width = 1500;
    private int nSeqs;
    private int[] queriesIndex;
    private char[] search;

    /*#######################################################################>>>>>
    * Purpose: Initialize graph by setting up labels, title, and grid...etc
    * this function must be called if you want to reset the graph to default.
    * Written: Winter 2009
    * #######################################################################>>>>>*/
    protected abstract void initGraph();

    /*#######################################################################>>>>>
    * Purpose: Display plots. you may have to call java.awt.Component.repaint() for plots to be updated correctly.
    * Written: Winter 2009
    * #######################################################################>>>>>*/
    protected abstract void drawPlot();

    /*#######################################################################>>>>>
    * Purpose: Create an AbstractBaseContentGraph object.
    * Written: Winter 2009
    * Edited: Winter 2016
    * #######################################################################>>>>>*/
    public AbstractBaseContentGraph(FeaturedSequence[] seqs, char[] search) {
        setSeqs(seqs);
        this.search = search;
        backupSeqs = getSeqs();
        nSeqs = seqs.length;
        queriesIndex = new int[nSeqs];
        for (int i = 0; i < nSeqs; i++)
            queriesIndex[i] = i;
        initMainPanel();
        this.getContentPane().add(mainPanel);
        this.pack();
        //this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /*#######################################################################>>>>>
    * Purpose: Initialize the main panel which is to be attached to the main frame later.
    * Written: Winter 2009
    * #######################################################################>>>>>*/
    private void initMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(width, height));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        queryPanel = generateSelectioPanel();
        graphAndCrtlPanel = new JPanel();
        graphAndCrtlPanel.setLayout(new BoxLayout(graphAndCrtlPanel, BoxLayout.Y_AXIS));
        theGraph = new Plot();
        initGraph();
        crtlPanel = generateCtrlPanel();
        graphAndCrtlPanel.add(theGraph);
        graphAndCrtlPanel.add(crtlPanel);
        mainPanel.add(queryPanel);
        mainPanel.add(graphAndCrtlPanel);
        this.setJMenuBar(buildMenu());
        //this.add(buildMenu());
    }

    public JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Help");
        bar.add(Box.createHorizontalGlue());
        bar.add(Box.createHorizontalGlue());
        bar.add(Box.createHorizontalGlue());
        bar.add(Box.createHorizontalGlue());
        bar.add(Box.createHorizontalGlue());
        bar.add(Box.createHorizontalGlue());
        JMenuItem me = new JMenuItem("Help");
        menu.add(me);
        me.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                //Launch Base-By-Base info section on virology.ca
                String url = "https://4virology.net/virology-ca-tools/base-by-base/";

                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(new URI(url));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                } else {
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        runtime.exec("xdg-open " + url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        bar.add(me);
        return bar;
    }

    /*#######################################################################>>>>>
    * Purpose: Generate a control panel.
    * Return: A new control panel.
    * Written: Winter 2009
    * #######################################################################>>>>>*/
    private JPanel generateSelectioPanel() {
        JPanel sPanel = new JPanel();
        sPanel.setLayout(new GridLayout(1, 1));
        sPanel.setPreferredSize(new Dimension(150, height));
        sPanel.setMaximumSize(new Dimension(120, 5000));
        sPanel.setMinimumSize(new Dimension(120, 0));
        sPanel.setBorder(GuiUtils.createWindowBorder("Pick Queries", new GuiDefaults()));
        String[] sequenceCollection = new String[nSeqs];
        for (int i = 0; i < nSeqs; i++)
            sequenceCollection[i] = seqs[i].getName();
        JList sequenceList = new JList(sequenceCollection);
        sequenceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sequenceList.setLayoutOrientation(JList.VERTICAL);
        sequenceList.setSelectedIndices(queriesIndex);
        sequenceList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                JList list = (JList) e.getSource();
                queriesIndex = list.getSelectedIndices();
                //System.out.printf("Number of selected queries = " + queriesIndex.length + "\n");
            }
        });
        sPanel.add(new JScrollPane(sequenceList));
        return sPanel;
    }

    protected void setSeqs(FeaturedSequence[] seqs) {
        this.seqs = seqs;
    }

    protected FeaturedSequence[] getSeqs() {
        return seqs;
    }

    protected char[] getSearchParameters() {
        return search;
    }

    /*#######################################################################>>>>>
    * Purpose: Reset the sequences to be plotted. Listener for 'Submit'.
    * Written: Winter 2009
    * Edited: Summer 2015
    * #######################################################################>>>>>*/
    private class refreshActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int parsedWindowSize = Integer.parseInt(windowSizeText.getText());
            int parsedStepSize = Integer.parseInt(stepSizeText.getText());
            try {
                if (parsedWindowSize == 0 || parsedStepSize == 0) {
                    throw new ArithmeticException();
                }

                //Check to see if 'Remove Gap Columns' checkbox is checked
                if (deleteGap) {
                    FeaturedSequence[] seqs = getSeqs();
                    FeaturedSequence[] clones = new FeaturedSequence[seqs.length];

                    for (int i = 0; i < seqs.length; ++i) {
                        clones[i] = (FeaturedSequence) seqs[i].clone();
                    }

                    //Delete the gaps
                    ca.virology.lib.io.tools.SequenceTools.deleteGaps(clones, true);
                    setSeqs(clones);
                } else {
                    deleteGap = false;
                    setSeqs(backupSeqs);
                }

                setWindowSize(parsedWindowSize);
                setStepSize(parsedStepSize);
                getNucleotideCheckbox();
                theGraph.clearLegends();
                theGraph.clear(false);
                drawPlot();
            } catch (ArithmeticException a) {
                JOptionPane.showMessageDialog(null, "Window/Step size cannot be zero", "Invalid Window Size", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /*#######################################################################>>>>>
    * Purpose: Recenter the sequences. Listener for 'Recentre'.
    * Written: Winter 2009
    * #######################################################################>>>>>*/
    private class recenterActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int parsedWindowSize = Integer.parseInt(windowSizeText.getText());
            int parsedStepSize = Integer.parseInt(stepSizeText.getText());
            try {
                if (parsedWindowSize == 0 || parsedStepSize == 0) {
                    throw new ArithmeticException();
                }

                setWindowSize(parsedWindowSize);
                setStepSize(parsedStepSize);
                theGraph.clear(true);
                initGraph();
                drawPlot();
            } catch (ArithmeticException a) {
                JOptionPane.showMessageDialog(null, "Window/Step size cannot be zero", "Invalid Window Size", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /*#######################################################################>>>>>
    * Purpose: Delete any columns for all sequences that contain gaps. Listener for 'Remove Gap Columns'.
    * Written: Summer 2015
    * #######################################################################>>>>>*/
    private class deleteGapActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!deleteGap) {
                deleteGap = true;
            } else {
                deleteGap = false;
            }
        }
    }

    /*#######################################################################>>>>>
    * Purpose: Generates control panel which allows users to set step size, window size, and refresh graph...etc.
    * Return: New control panel.
    * Written: Winter 2009
    * Edited: Summer 2015
    * #######################################################################>>>>>*/
    private JPanel generateCtrlPanel() {
        JPanel cPanel = new JPanel();
        cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.X_AXIS));
        //submit and recenter
        JButton submit = new JButton("Submit");
        JButton recenter = new JButton("Recentre");
        submit.addActionListener(new refreshActionListener());
        recenter.addActionListener(new recenterActionListener());

        //Delete Gap checkbox
        JCheckBox deleteGapCB = new JCheckBox();
        deleteGapCB.addActionListener(new deleteGapActionListener());
        //Close button
        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                theFrame.dispose();
            }
        });
        windowSizeText = new JTextField(4);
        windowSizeText.setMaximumSize(new Dimension(20, 20));
        windowSizeText.setEditable(true);
        windowSizeText.setText(windowSize + "");
        Utils.addNumberVerifier(windowSizeText, "[\\d]*", 4);    //add JTextVerifier so that only integers can be added.
        stepSizeText = new JTextField(4);
        stepSizeText.setMaximumSize(new Dimension(20, 20));
        stepSizeText.setEditable(true);
        stepSizeText.setText(stepSize + "");
        Utils.addNumberVerifier(stepSizeText, "[\\d]*", 4);    //add JTextVerifier so that only integers can be added.
        cPanel.add(new JLabel("Base Composition Selection: "));
        a = new JCheckBox("A");
        c = new JCheckBox("C");
        g = new JCheckBox("G");
        t = new JCheckBox("T");
        cPanel.add(a);
        cPanel.add(c);
        cPanel.add(g);
        cPanel.add(t);
        setNucleotideCheckbox();
        cPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        cPanel.add(new JLabel("Window Size: "));
        cPanel.add(windowSizeText);
        cPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        cPanel.add(new JLabel("Step Size: "));
        cPanel.add(stepSizeText);
        cPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        cPanel.add(new JLabel("Remove Gap Columns: "));
        cPanel.add(deleteGapCB);
        cPanel.add(Box.createHorizontalGlue());
        cPanel.add(submit);
        cPanel.add(recenter);
        cPanel.add(close);
        cPanel.setBorder(GuiUtils.createWindowBorder("Control Panel", new GuiDefaults()));
        return cPanel;
    }

    /*#######################################################################>>>>>
    * Purpose: Set window size.
    * Param: New window size.
    * Written: Winter 2009
    * #######################################################################>>>>>*/
    protected void setWindowSize(int i) {
        windowSize = i;
        windowSizeText.setText("" + windowSize);
    }

    /*#######################################################################>>>>>
    * Purpose: Set step size.
    * Param: New step size.
    * Written: Winter 2009
    * #######################################################################>>>>>*/
    protected void setStepSize(int i) {
        stepSize = i;
        stepSizeText.setText("" + stepSize);
    }

    /*#######################################################################>>>>>
    * Purpose: Get current window size.
    * Written: Winter 2009
    * #######################################################################>>>>>*/
    protected int getWindowSize() {
        return windowSize;
    }

    /*#######################################################################>>>>>
    * Purpose: Get current step size.
    * Written: Winter 2009
    * #######################################################################>>>>>*/
    protected int getStepSize() {
        return stepSize;
    }

    /*#######################################################################>>>>>
    * Purpose: Get current indices of queries.
    * Return: An array that contains the indices of all the query sequences.
    * Written: Winter 2009
    * #######################################################################>>>>>*/
    protected int[] getQueriesIndex() {
        return queriesIndex;
    }

    protected int getMax(int a, int b) {
        if (a > b)
            return a;
        else
            return b;
    }

    protected int getMin(int a, int b) {
        if (a > b)
            return b;
        else
            return a;
    }

    protected void setNucleotideCheckbox() {
        for (int i = 0; i < search.length; i++) {
            if ((search[i] == 'a') || (search[i] == 'A')) {
                a.setSelected(true);
            } else if ((search[i] == 'c') || (search[i] == 'C')) {
                c.setSelected(true);
            } else if ((search[i] == 'g') || (search[i] == 'G')) {
                g.setSelected(true);
            } else if ((search[i] == 't') || (search[i] == 'T')) {
                t.setSelected(true);
            }
        }
    }

    protected void getNucleotideCheckbox() {
        if (a.isSelected()) {
            search[0] = 'A';
        } else {
            search[0] = '0';
        }
        if (c.isSelected()) {
            search[1] = 'C';
        } else {
            search[1] = '0';
        }
        if (g.isSelected()) {
            search[2] = 'G';
        } else {
            search[2] = '0';
        }
        if (t.isSelected()) {
            search[3] = 'T';
        } else {
            search[3] = '0';
        }
    }
}
