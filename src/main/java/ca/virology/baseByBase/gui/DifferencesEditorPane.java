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

import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.io.tools.FeatureTools;
import ca.virology.lib.util.gui.UITools;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.RangeLocation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * This dialog shows a list of sequences, with multiple available columns.
 * It is used to mark spots in the alignment with comments where the sequences
 * are same/different (depending on what sequences are placed in each column)
 * <p>
 * There are two different tabs
 * - one for finding differences
 * - one for matching subgroups
 */
public class DifferencesEditorPane extends JDialog {
    //~ Static fields //////////////////////////////////////////////////////////
    public static final int FIND_DIFFERENCES_TAB = 0;
    public static final int SUBGROUP_MATCHING_TAB = 1;

    private static final String divider = "--------------------------------------------------"
            + "--------------------------------------------------\n";

    //~ Instance fields ////////////////////////////////////////////////////////

    protected final PrimaryPanel sequencePanel;
    protected EditPanel editPanel;

    //DragList objects for the find differences pane - also stores the sequences
    private final ObjectListTransferHandler findDifferencesListTransferHandler = new ObjectListTransferHandler();
    private final SimpleDragList findDifferencesSequenceList = new SimpleDragList("Sequences", findDifferencesListTransferHandler);
    private final List<FindDifferencesDragList> findDifferencesDragLists = new ArrayList<FindDifferencesDragList>();

    //DragList objects for the subgroup matching pane - also stores the sequences
    private final ObjectListTransferHandler subgroupMatchingSequenceListTransferHandler = new ObjectListTransferHandler();
    private final SimpleDragList subgroupMatchingSequenceList = new SimpleDragList("Sequences", subgroupMatchingSequenceListTransferHandler);
    private final SimpleDragList subgroupMatchingFirstSubgroupList = new SimpleDragList("First Subgroup", subgroupMatchingSequenceListTransferHandler);
    private final SimpleDragList subgroupMatchingSecondSubgroupList = new SimpleDragList("Second Subgroup", subgroupMatchingSequenceListTransferHandler);

    //ColorChoosers
    private final JPanel subgroupMatchingColorChooser = new JPanel();
    private final JPanel findDifferencesColorChooser = new JPanel();

    private final JTextField subgroupMatchingNameChooser = new JTextField("Subgroup Matching");
    private final JTextField findDifferencesNameChooser = new JTextField("Find Differences");


    private List<LoggingProcess> loggingProcesses;
    int openIndex = 0;

    Color commentColor = Color.MAGENTA;


    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new ShowSequencesDialog object.
     *
     * @param sequencePanel the featured sequence holder/panel that this should
     *                      pay attention to.
     */
    public DifferencesEditorPane(Window parent, PrimaryPanel sequencePanel, EditPanel editPanel, int openIndex,
                                 final List<LoggingProcess> loggingProcesses) {
        super(parent);
        this.sequencePanel = sequencePanel;
        this.editPanel = editPanel;
        this.loggingProcesses = loggingProcesses;
        setTitle("Find Differences in Sequences");
        setModal(true);
        this.openIndex = openIndex;
        initUI();
    }
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the sequences available
     *
     * @return the sequences in an array
     */
    protected FeaturedSequence[] getSequences() {
        return sequencePanel.getSequences();
    }


    /**
     * Search for the selected differences
     */
    private class FindDifferencesProcess extends LoggingProcess {
        @Override
        public void run() {
            if (!loggingProcesses.contains(this)) {
                int count = 1;
                for (LoggingProcess loggingProcess : loggingProcesses) {
                    if (loggingProcess.getName().contains(findDifferencesNameChooser.getText())) {
                        count++;
                    }
                }
                this.setName(findDifferencesNameChooser.getText() + " " + count);
                loggingProcesses.add(this);
            }
            status = "Running";
            runDifferences();
            if (stopped) {
                status = "Stopped";
            } else {
                status = "Complete";
            }
        }

        public String toString() {
            return this.getName() + " - " + status;
        }

        /**
         * Search for the selected differences
         */
        /**
         * Print sequence names to log file
         */
        private void writeNamesToLog() {
            this.log += divider + "-- Find Differences Parameters:\n" + divider;

            for (SimpleDragList sequenceList : findDifferencesDragLists) {
                DefaultListModel columnSequenceNames = sequenceList.getListModel();
                if (columnSequenceNames.size() > 0) {
                    this.log += "-- " + sequenceList.getName();

                    this.log += " [" + ((FindDifferencesDragList) sequenceList).getMinimumMatchThreshold() + ":"
                            + ((FindDifferencesDragList) sequenceList).getMaximumMatchThreshold() + "]";

                    this.log += " -";
                    String separator = "";
                    String sequenceName = "";
                    for (int i = 0; i < columnSequenceNames.size(); i++) {
                        sequenceName = ((FeaturedSequence) columnSequenceNames.get(i)).getName().trim();
                        this.log += separator + " " + sequenceName;
                        if (separator.equals("")) {
                            separator = ",";
                        }
                    }
                    this.log += "\n";
                }

            }
            this.log += divider;
        }

        private void runDifferences() {
            int rowNum = loggingProcesses.indexOf(this);

            refreshView();
            //Print sequence names to log file
            writeNamesToLog();
            this.log += "-- " + (new Date()) + ": Begin Find Differences...\n" + divider;
            int totalDifferences = 0;

            //System.out.println("threshold = " + threshold);

            /*
            Color[] color = new Color[numColors];
            color[0] = m_bgp1.getBackground();
            color[1] = toleranceColours[0].getBackground();
            color[2] = toleranceColours[1].getBackground();
            color[3] = toleranceColours[2].getBackground();
            Random rand = new Random();
            for (int i = 4; i < numColors; i++) {
                float hue = (float)(1.0 / (float)color.length) * i;
                float saturation = 0.9F + rand.nextFloat()/10F;
                float brightness = 1F;
                color[i] = Color.getHSBColor(hue, saturation, brightness);
            }*/
            int current = 0;
            int count;
            String note = "";

            //TODO: Change this to test all columns for the first valid entry
            List<FeaturedSequence> sameSequences = findDifferencesDragLists.get(0).getSequenceList();
            //check for selected/unselected sequences
            if (sameSequences.size() < 1) {
                UITools.showWarning("Not enough selected same sequences", null);
                return;
            }
            //Sequence to compare against and add annotations to
            FeaturedSequence mainSequence = sameSequences.get(0);
            //start of selection. -1 means no selection started
            int selectStart = -1;

            //Local Variables
            char sameVal;
            String log;
            String toleratedSequences;
            int numTolerated;
            int numMatches;
            int minMatches;
            int maxMatches;
            int totalMatches;
            int totalSequences;
            List<FeaturedSequence> sequences;
            //FeaturedSequence sequence;

            //Local 'createComment' variables
            int[] cc_i = new int[mainSequence.length()];
            cc_i[0] = -1;
            int[] cc_selectStart = new int[mainSequence.length()];
            String[] cc_note = new String[mainSequence.length()];

            //Cycle through the sequences and mark line where different
            for (int i = 0; i < mainSequence.length(); i++) {
                count = 0;

                sameVal = mainSequence.charAt(i);
                log = "";
                toleratedSequences = "";
                numTolerated = 0;
                totalMatches = 0;
                totalSequences = 0;

                for (FindDifferencesDragList sequenceList : findDifferencesDragLists) {

                    numMatches = 0;
                    maxMatches = sequenceList.getMaximumMatchThreshold();
                    minMatches = sequenceList.getMinimumMatchThreshold();
                    sequences = sequenceList.getSequenceList();

                    for (FeaturedSequence sequence : sequences) {
                        totalSequences++;
                        if (sameVal == '-' || sequence.charAt(i) == '-') {
                            //ignore all gapped columns
                            sameVal = 'x';
                            break;
                        }
                        if (sameVal == sequence.charAt(i)) {
                            numMatches++;
                            totalMatches++;
                            if (sequenceList.getPreset() == FindDifferencesDragList.ALL_DIFFERENT) {
                                toleratedSequences += sequence.getName().trim() + ", ";
                                numTolerated++;
                            }
                        } else {
                            if (sequenceList.getPreset() == FindDifferencesDragList.ALL_THE_SAME) {
                                toleratedSequences += sequence.getName().trim() + ", ";
                                numTolerated++;
                            }
                        }
                        if (numMatches > maxMatches) {
                            sameVal = 'x';
                            break;
                        }
                    }
                    if (numMatches < minMatches) {
                        sameVal = 'x';
                    }
                    if (sameVal == 'x') {
                        break;
                    } else {
                        //log += "Matched "+numMatches+" sequences in column "+sequenceList.getName()+" (Range ["+minMatches+":"+maxMatches+"]\n";
                    }
                }
                if (toleratedSequences.length() >= 2) {
                    toleratedSequences = toleratedSequences.substring(0, toleratedSequences.length() - 2).replace(",", "_");
                    log += numTolerated + " (" + toleratedSequences + ")";
                } else {
                    log += numTolerated;
                }
                //log += ""+totalMatches+" match"+(totalMatches==1 ? "" : "es")+" in " +totalSequences+" sequence"+(totalSequences==1 ? "" : "s");

                //to log
                if (sameVal != 'x') {
                    this.log += "" + (i + 1) + ": " + log + "\n";
                    totalDifferences++;
                    //System.out.println((i+1)+": "+(diffLog == null ? "null" : diffLog.length()));
                }

                if (selectStart < 0) {
                    if (sameVal != 'x') {
                        // Start comment selection
                        selectStart = i;
                        note = "Find Differences" + rowNum;
                        current = count;
                    }
                }
                // Draw comment selection
                else if (sameVal == 'x' && selectStart >= 0) {
                    //Record the placement parameters for each comment
                    cc_i[i] = i;
                    cc_selectStart[i] = selectStart;
                    cc_note[i] = note;
                    selectStart = -1;
                    current = 0;
                }
            }

            //Dialog displays the total number of differences found and asks user
            //if they wish to create comments
            Object[] options = {"No", "Yes"};
            int choice = JOptionPane.showOptionDialog(null, "Find Differences is complete! " +
                            "There are " + totalDifferences + " differences.\n\n" +
                            "However, comments have not been added to the GUI...\n" +
                            "Do you wish to add them?",
                    "Find Differences",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            //Create comments after data is fully processed
            if (choice == 1) {
                sequencePanel.setProcessing(true);
                for (int i = 0; i < mainSequence.length(); i++) {
                    if (i == cc_i[i]) {
                        if (this.stopped) {
                            break;
                        }
                        createComment(mainSequence, new RangeLocation(mainSequence.getAbsolutePosition(cc_selectStart[i]),
                                mainSequence.getAbsolutePosition(i - 1)), commentColor, note);
                    }
                }
                //End possible open comment
                if (selectStart != -1) {
                    createComment(mainSequence, new RangeLocation(mainSequence.getAbsolutePosition(selectStart),
                            mainSequence.getAbsolutePosition(mainSequence.length() - 1)), commentColor, note);
                }
                sequencePanel.setProcessing(false);
                for (int i = 0; i < sequencePanel.m_epanels.size(); ++i) {
                    EditPanel ep = sequencePanel.m_epanels.get(i);
                    ep.resetView();
                }
                sequencePanel.repaint();

            }

            this.log += divider
                    + "-- " + (new Date()) + ": Find Differences complete!\n"
                    + "-- Found " + totalDifferences + " differences.\n"
                    + divider;
            refreshView();
        }
    }

    private class SubgroupMatchingProcess extends LoggingProcess {
        @Override
        public void run() {
            if (!loggingProcesses.contains(this)) {
                int count = 1;
                for (LoggingProcess loggingProcess : loggingProcesses) {
                    if (loggingProcess.getName().contains(subgroupMatchingNameChooser.getText())) {
                        count++;
                    }
                }
                this.setName(subgroupMatchingNameChooser.getText() + " " + count);
                loggingProcesses.add(this);
            }
            status = "Running";
            runGroupMatching();
            if (stopped) {
                status = "Stopped";
            } else {
                status = "Complete";
            }
        }

        public String toString() {
            return this.getName() + " - " + status;
        }

        /**
         * Print sequence names to log file
         */
        private void writeNamesToLog() {
            this.log += divider + "-- Subgroup Matching Parameters:\n" + divider;

            for (SimpleDragList sequenceList : Arrays.asList(new SimpleDragList[]{subgroupMatchingFirstSubgroupList, subgroupMatchingSecondSubgroupList})) {
                DefaultListModel columnSequenceNames = sequenceList.getListModel();
                if (columnSequenceNames.size() > 0) {
                    this.log += "-- " + sequenceList.getName();
                    this.log += " -";
                    String separator = "";
                    String sequenceName = "";
                    for (int i = 0; i < columnSequenceNames.size(); i++) {
                        sequenceName = ((FeaturedSequence) columnSequenceNames.get(i)).getName().trim();
                        this.log += separator + " " + sequenceName;
                        if (separator.equals("")) {
                            separator = ",";
                        }
                    }
                    this.log += "\n";
                }
            }
            this.log += divider;
        }

        /**
         * Search for the group matching
         */
        private void runGroupMatching() {
            refreshView();
            writeNamesToLog();
            this.log += "-- " + (new Date()) + ": Begin Subgroup Matching...\n" + divider;
            int totalDifferences = 0;
            //Get all the sequences
            List<FeaturedSequence> mainSubSeqs = subgroupMatchingFirstSubgroupList.getSequenceList();
            List<FeaturedSequence> anySubSeqs = subgroupMatchingSecondSubgroupList.getSequenceList();

            //check for selected/unselected sequences
            if (mainSubSeqs.size() < 1) {
                UITools.showWarning("Not enough selected same sequences", null);
                return;
            }

            FeaturedSequence mainSequence = mainSubSeqs.get(0);

            int[] baseChar = {'C', 'G', 'A', 'T'};

            //start of selection. -1 means no selection started
            int selectStart = -1;

            //Cycle through the sequences and mark line where different
            for (int i = 0; i < mainSubSeqs.get(0).length(); i++) {

                if (this.stopped) {
                    //Cancel execution on stopProcess()
                    break;
                }

                int[] base = {0, 0, 0, 0};
                for (int j = 0; j < mainSubSeqs.size(); j++) {
                    switch (mainSubSeqs.get(j).charAt(i)) {
                        case 'C':
                            base[0]++;
                            break;
                        case 'G':
                            base[1]++;
                            break;
                        case 'A':
                            base[2]++;
                            break;
                        case 'T':
                            base[3]++;
                            break;
                        default:
                            break;
                    }
                }

                boolean marked = false;
                anySubLoop:
                for (int j = 0; j < base.length; j++) {
                    if (base[j] > 0 && base[j] < mainSubSeqs.size() - 1) {
                        for (int k = 0; k < anySubSeqs.size(); k++) {
                            if (baseChar[j] == anySubSeqs.get(k).charAt(i)) {
                                marked = true;
                                break anySubLoop;
                            }
                        }
                    }
                }


                //to log
                if (marked) {
                    this.log += (i + 1) + ", ";
                    totalDifferences++;
                }

                // Draw comment selection
                if (selectStart < 0) {
                    if (marked) {
                        // Start comment selection
                        selectStart = i;
                    }
                } else {
                    if (!marked && mainSequence.toString().charAt(i) != '-') {
                        // End comment selection - Draw the comment
                        createComment(mainSequence, new RangeLocation(mainSequence.getAbsolutePosition(selectStart),
                                mainSequence.getAbsolutePosition(i)), commentColor, "");

                        selectStart = -1;

                    }
                }
            }

            //end possible open comment
            if (selectStart != -1) {
                createComment(mainSequence, new RangeLocation(mainSequence.getAbsolutePosition(selectStart),
                        mainSequence.getAbsolutePosition(mainSequence.length() - 1)), commentColor, "");
            }
            //cut off last ", "
            if (this.log.charAt(this.log.length() - 2) == ',') {
                this.log = this.log.substring(0, this.log.length() - 2);
            }
            this.log += "\n"
                    + divider
                    + "-- " + (new Date()) + ": Subgroup Matching complete!\n"
                    + "-- Found " + totalDifferences + " matching differences.\n"
                    + divider;
            refreshView();
        }
    }

    private void refreshView() {
        //Update GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                editPanel.revalidate();
                editPanel.resetView();
                sequencePanel.refreshState();
            }
        });
    }

    private void setCommentColor(Color c) {
        commentColor = c;
        findDifferencesColorChooser.setBackground(c);
        subgroupMatchingColorChooser.setBackground(c);
    }

    /**
     * initialize the UI
     */
    private void initUI() {
        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Find Differences", initFindDifferencesPanel());
        tabs.addTab("Subgroup Matching", initSubgroupMatchingPanel());
        tabs.setSelectedIndex(openIndex);
        setContentPane(tabs);
        pack();
        setMinimumSize(getMinimumSize());

        // Position the dialog window
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = getWidth();
        int h = getHeight();
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        setLocation(x, y);
        setResizable(true);
    }

    private JPanel initButtonsPanel(final boolean isFindDifferences) {
        return initButtonsPanel(isFindDifferences, new JButton[0]);
    }

    private JPanel initButtonsPanel(final boolean isFindDifferences, JButton[] extraButtons) {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        JPanel colorPanel = new JPanel(new BorderLayout());
        JPanel namePanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton ok = new JButton("Ok");
        ok.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        Thread t;
                        if (isFindDifferences) {
                            t = new FindDifferencesProcess();
                        } else {
                            t = new SubgroupMatchingProcess();
                        }
                        t.start();
                        dispose();
                    }
                });
        buttonsPanel.add(ok);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        dispose();
                    }
                });
        buttonsPanel.add(cancel);

        for (JButton button : extraButtons) {
            buttonsPanel.add(button);
        }


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = 0;

        final JPanel colorChooser = (isFindDifferences ? findDifferencesColorChooser : subgroupMatchingColorChooser);

        colorChooser.setBackground(commentColor);
        colorChooser.setOpaque(true);

        colorPanel.setOpaque(true);
        colorPanel.setBorder(BorderFactory.createTitledBorder("Comment Color"));
        colorPanel.setPreferredSize(new Dimension(150, 45));
        colorPanel.setMinimumSize(new Dimension(150, 45));
        colorPanel.add(colorChooser, BorderLayout.CENTER);

        colorChooser.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent ev) {
                        JPanel b = (JPanel) ev.getSource();
                        Color c = JColorChooser.showDialog(DifferencesEditorPane.this, "Select Comment Color", b.getBackground());

                        if (c != null) {
                            setCommentColor(c);
                        }
                    }
                });

        mainPanel.add(colorPanel, c);

        c.gridx = 1;
        c.gridy = 0;

        final JTextField nameChooser = (isFindDifferences ? findDifferencesNameChooser : subgroupMatchingNameChooser);
        //nameChooser.setMinimumSize(new Dimension(150, 35));
        nameChooser.setText(isFindDifferences ? "Find Differences" : "Subgroup Matching");
        namePanel.setBorder(BorderFactory.createTitledBorder("Process Name"));
        namePanel.add(nameChooser);
        namePanel.setPreferredSize(new Dimension(150, 45));
        namePanel.setMinimumSize(new Dimension(150, 45));
        mainPanel.add(namePanel, c);

        c.anchor = GridBagConstraints.LINE_END;
        c.weightx = 1.0;
        c.gridx = 2;
        c.gridy = 0;
        mainPanel.add(buttonsPanel, c);

        return mainPanel;
    }

    private JPanel initFindDifferencesPanel() {
        JPanel main = new JPanel(new BorderLayout());
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        main.add(splitPane, BorderLayout.CENTER);

        for (FeaturedSequence sequence : getSequences()) {
            findDifferencesSequenceList.addSequence(sequence);
        }
        //findDifferencesSequenceList.setMinimumSize(new Dimension(256, 256));
        splitPane.setTopComponent(findDifferencesSequenceList);
        //main.add(findDifferencesSequenceList, BorderLayout.WEST);

        JPanel rows = new JPanel(new BorderLayout());
        splitPane.setBottomComponent(rows);
        //main.add(rows, BorderLayout.CENTER);
        JTextArea description = new JTextArea(" Drag sequences into the below lists to compare them.\n" +
                " Positions will be commented if: \n" +
                "     All sequences in the \"All the same\" column(s) match, allowing up to \"Tolerance\" unmatching sequences.\n" +
                "     All sequences in the \"All different\" column(s) do not match, allowing up to \"Tolerance\" matching sequences.\n" +
                " All sequences are matched against the first sequence in the first \"All the same\" column." +
                " Annotations are added to this sequence wherever differences are found.\n" +
                " Additional columns can be added with the \"Add a column\" button.");

        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        //description.setMaximumSize(maxSize);
        JScrollPane descriptionScrollPane = new JScrollPane(description);
        descriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //descriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        rows.add(descriptionScrollPane, BorderLayout.NORTH);
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent pce) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                splitPane.revalidate();
                                splitPane.repaint();
                            }
                        });
                    }
                });

        final JPanel columns = new JPanel(new GridLayout(1, 0));
        rows.add(columns, BorderLayout.CENTER);
        FindDifferencesDragList dragList;
        //columns.add(addDragListTarget("All the same", arrayListHandler, same_status, sameSeqNames));
        dragList = new FindDifferencesDragList(FindDifferencesDragList.ALL_THE_SAME, findDifferencesListTransferHandler);
        dragList.disableRemoveColumn();
        columns.add(dragList);
        findDifferencesDragLists.add(dragList);
        //columns.add(addDragListTarget("All different", arrayListHandler, diff_status, diffSeqNames));
        dragList = new FindDifferencesDragList(FindDifferencesDragList.ALL_DIFFERENT, findDifferencesListTransferHandler);
        dragList.disableRemoveColumn();
        columns.add(dragList);
        findDifferencesDragLists.add(dragList);
        //columns.add(addDragListTarget("The same in at least one", arrayListHandler, onesame_status, oneSameSeqNames));
        //dragList = new FindDifferencesDragList(FindDifferencesDragList.CUSTOM_SETTINGS, findDifferencesListTransferHandler);
        //columns.add(dragList);
        //findDifferencesDragLists.add(dragList);

        // Buttons
        JButton addColumnButton = new JButton("Add a column");
        addColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddColumnDialog dialog = new AddColumnDialog(DifferencesEditorPane.this, findDifferencesListTransferHandler);
                FindDifferencesDragList dragList = dialog.showDialog();
                if (dragList != null) {
                    columns.add(dragList);
                    findDifferencesDragLists.add(dragList);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (splitPane.getDividerLocation() < splitPane.getMinimumDividerLocation()) {
                                splitPane.setDividerLocation(splitPane.getMinimumDividerLocation());
                            } else if (splitPane.getDividerLocation() > splitPane.getMaximumDividerLocation()) {
                                splitPane.setDividerLocation((splitPane.getMaximumDividerLocation()));
                            }
                            DifferencesEditorPane.this.repaint();
                        }
                    });

                }
            }
        });
        JPanel buttons = initButtonsPanel(true, new JButton[]{addColumnButton});
        main.add(buttons, BorderLayout.SOUTH);
        return main;
    }

    private JPanel initSubgroupMatchingPanel() {
        JPanel main = new JPanel(new BorderLayout());
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        main.add(splitPane, BorderLayout.CENTER);

        /*
        JPanel matching = new JPanel(new BorderLayout());
        matching.setLayout(new BoxLayout(matching, BoxLayout.Y_AXIS));
        JPanel columns = new JPanel(new BorderLayout());
        columns.setLayout(new BoxLayout(columns, BoxLayout.X_AXIS));
        matching.add(columns, BorderLayout.NORTH);
*/

        for (FeaturedSequence sequence : getSequences()) {
            subgroupMatchingSequenceList.addSequence(sequence);
        }
        //columns.add(subgroupMatchingSequenceList, BorderLayout.CENTER);
        //subgroupMatchingSequenceList.setMinimumSize(new Dimension(256, 256));
        splitPane.setTopComponent(subgroupMatchingSequenceList);

        JPanel rows = new JPanel(new BorderLayout());
        //columns.add(rows);
        splitPane.setBottomComponent(rows);

        JTextArea description = new JTextArea("  Drag sequences into the below lists to compare them. " +
                "\n  Positions will be commented where:" +
                "\n     Differences are found in the first subgroup (any position where the nucleotides in the group are NOT all the same)" +
                "\n     AND those differences are also found in the second subgroup.");
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        //description.setMaximumSize(maxSize);
        JScrollPane descriptionScrollPane = new JScrollPane(description);
        descriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        rows.add(descriptionScrollPane, BorderLayout.NORTH);
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent pce) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                splitPane.revalidate();
                                splitPane.repaint();
                            }
                        });
                    }
                });


        JPanel columns = new JPanel(new GridLayout(1, 0));
        rows.add(columns, BorderLayout.CENTER);

        columns.add(subgroupMatchingFirstSubgroupList);
        columns.add(subgroupMatchingSecondSubgroupList);

        // Buttons
        JPanel buttons = initButtonsPanel(false);
        main.add(buttons, BorderLayout.SOUTH);

        return main;
    }

    public static List<Integer> readDiffLog(String diffLog) {
        BufferedReader br = new BufferedReader(new StringReader(diffLog));
        List<Integer> values = new ArrayList<Integer>(0);
        //Parse differences from file
        try {
            String line = br.readLine();
            while (line != null) {
                if (line.length() > 0) {
                    //Ignore separators and comments text
                    if (line.charAt(0) != '-') {
                        try {
                            //Support both line delimited and comma delimited values
                            for (String val : line.split(",")) {
                                int position = Integer.parseInt(val.split(":")[0].trim());
                                values.add(position);
                            }

                        } catch (NumberFormatException e) {
                            System.out.println("Bad file format");
                            e.printStackTrace();
                        }
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            System.out.println("File I/O Error");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return values;
    }

    /**
     * Add a comment to the given sequence
     *
     * @param holder the sequence to add the comment to
     * @param loc    The location the comment covers
     * @param color  The comment color
     * @param note   Additional text to display on the comment tooltip
     * @return true if successful
     */

    protected boolean createComment(FeaturedSequence holder, Location loc, Color color, String note) {
        boolean ret;
        try {
            ret = FeatureTools.createUserComment(holder, loc, editPanel.getDisplayStrand(), " ", (note == null || note.equals("") ? ("") : (note)), color, color);
        } catch (Exception e)  {
            System.out.print("Exception in create comment thread");
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }




    private class FeaturedSequenceListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof FeaturedSequence) {
                return super.getListCellRendererComponent(list, ((FeaturedSequence) value).getName(), index, isSelected, cellHasFocus);
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    public class AddColumnDialog extends JDialog {

        private FindDifferencesDragList column = null;

        public FindDifferencesDragList showDialog() {
            setVisible(true);
            return column;
        }

        public AddColumnDialog(Window parent, final ObjectListTransferHandler listTransferHandler) {
            super(parent, "Add a column");
            setModal(true);
            rootPane.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;


            JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            final JTextField subtitleTextField = new JTextField();
            subtitleTextField.setMinimumSize(new Dimension(200, 20));
            subtitleTextField.setPreferredSize(new Dimension(200, 20));
            textPanel.add(new JLabel("Name: "));
            textPanel.add(subtitleTextField);

            JPanel optionsPanel = new JPanel(new BorderLayout());
            optionsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            JPanel radioPanel = new JPanel(new GridLayout(0, 1));

            final JRadioButton allTheSameButton = new JRadioButton(FindDifferencesDragList.ALL_THE_SAME_TITLE);
            final JRadioButton allDifferentButton = new JRadioButton(FindDifferencesDragList.ALL_DIFFERENT_TITLE);
            final JRadioButton customButton = new JRadioButton(FindDifferencesDragList.CUSTOM_TITLE);
            allTheSameButton.setSelected(true);

            final ButtonGroup group = new ButtonGroup();
            group.add(allTheSameButton);
            group.add(allDifferentButton);
            group.add(customButton);

            radioPanel.add(allTheSameButton);
            radioPanel.add(allDifferentButton);
            radioPanel.add(customButton);

            optionsPanel.add(new JLabel("Column Type:"), BorderLayout.NORTH);
            optionsPanel.add(radioPanel, BorderLayout.CENTER);


            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int preset;
                    if (allTheSameButton.isSelected()) {
                        preset = FindDifferencesDragList.ALL_THE_SAME;
                    } else if (allDifferentButton.isSelected()) {
                        preset = FindDifferencesDragList.ALL_DIFFERENT;
                    } else {
                        preset = FindDifferencesDragList.CUSTOM_SETTINGS;
                    }
                    column = new FindDifferencesDragList(preset, listTransferHandler, subtitleTextField.getText());
                    System.out.println("Created new column");
                    setVisible(false);
                    dispose();
                }
            });
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    dispose();
                }
            });

            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);


            c.gridy = 0;
            rootPane.add(textPanel, c);
            c.gridy = 1;
            rootPane.add(optionsPanel, c);
            c.gridy = 2;
            rootPane.add(buttonPanel, c);

            pack();
            setResizable(false);
            setLocationRelativeTo(parent);
        }
    }


    private JScrollPane makeDragList(TransferHandler transferHandler, JList list) {
        final JPanel content = new JPanel(new BorderLayout());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setMinimumSize(new Dimension(100, 100));
        scroll.setPreferredSize(new Dimension(200, 200));

        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setTransferHandler(transferHandler);
        list.setDragEnabled(true);

        content.add(list, BorderLayout.CENTER);

        return scroll;
    }

    private class SimpleDragList extends JPanel {
        protected final Dimension minimumSize = new Dimension(150, 150);
        protected final TitledBorder titleBorder = BorderFactory.createTitledBorder("");
        protected DefaultListModel listModel = new DefaultListModel();

        protected SimpleDragList() {
            super(new BorderLayout());
        }

        public SimpleDragList(String title, final ObjectListTransferHandler listTransferHandler) {
            super(new BorderLayout());

            this.setName(title);
            titleBorder.setTitle(title);
            this.setBorder(BorderFactory.createCompoundBorder(titleBorder, BorderFactory.createEmptyBorder(3, 3, 3, 3)));
            this.setMinimumSize(minimumSize);

            //List of sequences
            JList list = new JList(listModel);
            list.setCellRenderer(new FeaturedSequenceListCellRenderer());
            this.add(makeDragList(listTransferHandler, list), BorderLayout.CENTER);
            //listModel.addListDataListener(new dragNDropListDataListener());
        }

        public void addSequence(List<FeaturedSequence> sequences) {
            for (FeaturedSequence sequence : sequences) {
                listModel.addElement(sequence);
            }
        }

        public void addSequence(FeaturedSequence sequence) {
            listModel.addElement(sequence);
        }

        public List<FeaturedSequence> getSequenceList() {
            List<FeaturedSequence> sequences = new ArrayList<FeaturedSequence>();
            for (int i = 0; i < listModel.size(); i++) {
                sequences.add(((FeaturedSequence) (listModel.get(i))));
            }
            return sequences;
        }

        protected DefaultListModel getListModel() {
            return listModel;
        }

        /*private class dragNDropListDataListener implements ListDataListener {

            public dragNDropListDataListener() {
                super();
                //this.arrayListHandler = (ReportingListTransferHandler)list.getTransferHandler();
            }

            public void intervalAdded(ListDataEvent e) { }
            public void intervalRemoved(ListDataEvent e) { }
            public void contentsChanged(ListDataEvent arg0) { }
        }*/
    }

    private class FindDifferencesDragList extends SimpleDragList {
        public static final int CUSTOM_SETTINGS = 0;
        public static final int ALL_THE_SAME = 1;
        public static final int ALL_DIFFERENT = 2;

        protected static final String CUSTOM_TITLE = "Custom column";
        protected static final String ALL_THE_SAME_TITLE = "All the same";
        protected static final String ALL_DIFFERENT_TITLE = "All different";

        private int preset = CUSTOM_SETTINGS;
        private final Dimension spinnerSize = new Dimension(50, 20);
        private final JSpinner minimumMatchSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
        private final JSpinner maximumMatchSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
        private final JSpinner toleranceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));

        private final JLabel minimumMatchSpinnerLabel = new JLabel("Minimum matches");
        private final JLabel maximumMatchSpinnerLabel = new JLabel("Maximum matches");
        private final JLabel toleranceSpinnerLabel = new JLabel("Tolerance");

        private final JButton removeColumnButton = new JButton("Remove column");
        //private final TitledBorder titleBorder = BorderFactory.createTitledBorder("");
        //private final DefaultListModel listModel = new DefaultListModel();

        public FindDifferencesDragList(int preset, final ObjectListTransferHandler listTransferHandler) {
            this(preset, listTransferHandler, "");
        }

        public FindDifferencesDragList(int preset, final ObjectListTransferHandler listTransferHandler, String subtitle) {
            super();
            //final FeaturedSequence[] sequences = getSequences();


            this.preset = preset;
            String title;
            switch (preset) {
                case CUSTOM_SETTINGS:
                    title = CUSTOM_TITLE;
                    break;
                case ALL_THE_SAME:
                    title = ALL_THE_SAME_TITLE;
                    break;
                case ALL_DIFFERENT:
                    title = ALL_DIFFERENT_TITLE;
                    break;
                default:
                    title = CUSTOM_TITLE;
                    break;
            }
            if (subtitle != null && !subtitle.trim().equals("")) {
                title += " - " + subtitle.trim();
            }
            this.setName(title);
            titleBorder.setTitle(title);
            this.setBorder(BorderFactory.createCompoundBorder(titleBorder, BorderFactory.createEmptyBorder(3, 3, 3, 3)));

            JList list = new JList(listModel);
            list.setCellRenderer(new FeaturedSequenceListCellRenderer());
            this.add(makeDragList(listTransferHandler, list), BorderLayout.CENTER);
            listModel.addListDataListener(new dragNDropListDataListener());

            minimumMatchSpinner.setMinimumSize(spinnerSize);
            maximumMatchSpinner.setMinimumSize(spinnerSize);
            toleranceSpinner.setMinimumSize(spinnerSize);
            minimumMatchSpinner.setPreferredSize(spinnerSize);
            maximumMatchSpinner.setPreferredSize(spinnerSize);
            toleranceSpinner.setPreferredSize(spinnerSize);

            final JPanel settings = new JPanel(new BorderLayout());
            this.add(settings, BorderLayout.SOUTH);

            JPanel labelPanel = new JPanel(new GridLayout(0, 1));
            JPanel spinnerPanel = new JPanel(new GridLayout(0, 1));

            settings.add(labelPanel, BorderLayout.CENTER);
            settings.add(spinnerPanel, BorderLayout.EAST);
            settings.add(removeColumnButton, BorderLayout.SOUTH);

            removeColumnButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeColumn();
                }
            });
            if (preset == ALL_THE_SAME || preset == ALL_DIFFERENT) {
                minimumMatchSpinner.setEnabled(false);
                maximumMatchSpinner.setEnabled(false);
                minimumMatchSpinner.setVisible(false);
                maximumMatchSpinner.setVisible(false);
                minimumMatchSpinnerLabel.setVisible(false);
                maximumMatchSpinnerLabel.setVisible(false);

                //labelPanel.add(minimumMatchSpinnerLabel);
                //labelPanel.add(maximumMatchSpinnerLabel);
                labelPanel.add(toleranceSpinnerLabel);

                //spinnerPanel.add(minimumMatchSpinner);
                //spinnerPanel.add(maximumMatchSpinner);
                spinnerPanel.add(toleranceSpinner);
            } else {
                toleranceSpinner.setEnabled(false);
                toleranceSpinner.setVisible(false);
                toleranceSpinnerLabel.setVisible(false);

                labelPanel.add(minimumMatchSpinnerLabel);
                labelPanel.add(maximumMatchSpinnerLabel);
                //labelPanel.add(toleranceSpinnerLabel);

                spinnerPanel.add(minimumMatchSpinner);
                spinnerPanel.add(maximumMatchSpinner);
                //spinnerPanel.add(toleranceSpinner);
            }

            minimumMatchSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent changeEvent) {
                    //The order of the methods determines which value overrides the other in case of a conflict
                    updateMinimumMatchSpinner();
                    updateMaximumMatchSpinner();
                }
            });

            maximumMatchSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent changeEvent) {
                    //The order of the methods determines which value overrides the other in case of a conflict
                    updateMaximumMatchSpinner();
                    updateMinimumMatchSpinner();

                }
            });
            toleranceSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateToleranceSpinner();
                }
            });

            updateToleranceSpinner();
            updateMaximumMatchSpinner();
            updateMinimumMatchSpinner();
        }

        public int getMaximumMatchThreshold() {
            return (Integer) maximumMatchSpinner.getValue();
        }

        public int getMinimumMatchThreshold() {
            return (Integer) minimumMatchSpinner.getValue();
        }

        public void enableRemoveColumn() {
            removeColumnButton.setEnabled(true);
        }

        public void disableRemoveColumn() {
            removeColumnButton.setEnabled(false);
        }

        public void removeColumn() {
            for (int i = 0; i < listModel.size(); i++) {
                findDifferencesSequenceList.getListModel().add(i, listModel.get(i));
            }
            findDifferencesDragLists.remove(this);
            this.getParent().remove(this);
            this.setVisible(false);
            DifferencesEditorPane.this.repaint();
        }

        /*
        public int getDefaultMaximumMatchThreshold() {
            switch (preset) {
                case CUSTOM_SETTINGS:
                    return listModel.size();
                case ALL_THE_SAME:
                    return listModel.size();
                case ALL_DIFFERENT:
                    return 0;
                default:
                    return listModel.size();
            }
        }
        public int getDefaultMinimumMatchThreshold() {
            switch (preset) {
                case CUSTOM_SETTINGS:
                    return 0;
                case ALL_THE_SAME:
                    return listModel.size();
                case ALL_DIFFERENT:
                    return 0;
                default:
                    return 0;
            }
        }*/
        public int getPreset() {
            return preset;
        }

        private void updateMinimumMatchSpinner() {
            //Check if list size has changed
            if ((Integer) minimumMatchSpinner.getValue() > listModel.size()) {
                minimumMatchSpinner.setValue(listModel.size());
            }
            ((SpinnerNumberModel) minimumMatchSpinner.getModel()).setMaximum(listModel.size());

            //Update maximum spinner
            if ((Integer) minimumMatchSpinner.getValue() > (Integer) maximumMatchSpinner.getValue()) {
                maximumMatchSpinner.setValue(minimumMatchSpinner.getValue());
            }

            minimumMatchSpinnerLabel.setText(getPercentLabel("Minimum matches", ((Integer) minimumMatchSpinner.getValue()), listModel.size()));
        }

        private void updateMaximumMatchSpinner() {
            //If value = prev maxval, set val to new maxval (?)
            if ((Integer) maximumMatchSpinner.getValue() == ((SpinnerNumberModel) maximumMatchSpinner.getModel()).getMaximum()) {
                //maximumMatchSpinner.setValue(listModel.size());
            }
            //Check if list size has changed
            if ((Integer) maximumMatchSpinner.getValue() > listModel.size()) {
                maximumMatchSpinner.setValue(listModel.size());
            }
            ((SpinnerNumberModel) maximumMatchSpinner.getModel()).setMaximum(listModel.size());

            //Update minimum spinner
            if ((Integer) minimumMatchSpinner.getValue() > (Integer) maximumMatchSpinner.getValue()) {
                minimumMatchSpinner.setValue(maximumMatchSpinner.getValue());
            }
            maximumMatchSpinnerLabel.setText(getPercentLabel("Maximum matches", ((Integer) maximumMatchSpinner.getValue()), listModel.size()));

        }

        private void updateToleranceSpinner() {
            if ((Integer) toleranceSpinner.getValue() > listModel.size()) {
                toleranceSpinner.setValue(listModel.size());
            }
            ((SpinnerNumberModel) toleranceSpinner.getModel()).setMaximum(listModel.size());
            switch (preset) {
                case ALL_THE_SAME:
                    minimumMatchSpinner.setValue(listModel.size() - ((Integer) toleranceSpinner.getValue()));
                    break;
                case ALL_DIFFERENT:
                    maximumMatchSpinner.setValue(toleranceSpinner.getValue());
                    break;
                default:
                    break;
            }
            toleranceSpinnerLabel.setText(getPercentLabel("Tolerance", ((Integer) toleranceSpinner.getValue()), listModel.size()));
        }

        private String getPercentLabel(String label, int val, int max) {
            double doubleVal = (double) val;
            double doubleMax = (double) max;
            double doublePercent = (doubleVal / doubleMax) * 100;
            int percent = (int) doublePercent;
            return String.format("%s (%d%%)", label, percent);
        }

        private void updateToPreset() {
            switch (preset) {
                case CUSTOM_SETTINGS:
                    updateMaximumMatchSpinner();
                    updateMinimumMatchSpinner();
                    break;
                case ALL_THE_SAME:
                    updateToleranceSpinner();
                    maximumMatchSpinner.setValue(listModel.size());
                    updateMinimumMatchSpinner();
                    updateMaximumMatchSpinner();
                    break;
                case ALL_DIFFERENT:
                    updateToleranceSpinner();
                    updateMaximumMatchSpinner();
                    updateMinimumMatchSpinner();
                    break;
                default:
                    updateToleranceSpinner();
                    updateMaximumMatchSpinner();
                    updateMinimumMatchSpinner();
                    break;
            }
        }

        private class dragNDropListDataListener implements ListDataListener {

            public dragNDropListDataListener() {
                super();
                //this.arrayListHandler = (ReportingListTransferHandler)list.getTransferHandler();
            }

            public void intervalAdded(ListDataEvent e) {
                //System.out.println("Added ["+e.getIndex0()+":"+e.getIndex1()+"]");
                updateToPreset();
            }

            public void intervalRemoved(ListDataEvent e) {
                updateToPreset();
            }

            public void contentsChanged(ListDataEvent arg0) {
                //updateToPreset();
            }
        }


    }


}
