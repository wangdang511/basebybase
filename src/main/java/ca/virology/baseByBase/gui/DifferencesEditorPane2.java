package ca.virology.baseByBase.gui;

import ca.virology.baseByBase.data.FeaturedSequenceModel;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.io.tools.FeatureTools;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

@Deprecated
public class DifferencesEditorPane2 extends JDialog {
    protected final FeaturedSequenceModel m_holder;
    protected EditPanel p;

    // maps a sequences name to the sequence object
    // sequences are passed around by sequence name
    final protected Map nameMap = new HashMap();

    int openIndex = 0;
    //string used to always have an element even on an empty list
    String blankDNDListing = "           Drag Here            ";

    Object[] allSame;
    Object[] allDiff;

    DefaultListModel allSameList = new DefaultListModel();
    DefaultListModel allDiffList = new DefaultListModel();

    ArrayList<Comment> comments = new ArrayList<Comment>();


    String diffLog = "";

    Color[] commentColors = {Color.MAGENTA, Color.RED, Color.BLUE, Color.GREEN};

    private CommentTask commentTask;
    private ProgressMonitor progressMonitor;

    public DifferencesEditorPane2(FeaturedSequenceModel holder, EditPanel p, int openIndex) {
        m_holder = holder;
        this.p = p;
        setTitle("Find Differences in Sequences");
        setModal(true);
        this.openIndex = openIndex;

        initUI();
    }


    protected void initUI() {
        int width = 1050;
        int height = 500;
        rootPane.setPreferredSize(new Dimension(width, height));
        rootPane.setMinimumSize(new Dimension(width, height));

        // root container
        JPanel root = new JPanel(new BorderLayout());
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        // columns
        JPanel columns = new JPanel(new BorderLayout());
        columns.setLayout(new BoxLayout(columns, BoxLayout.X_AXIS));
        root.add(columns, BorderLayout.NORTH);

        // add sequence pane as a column
        ReportingListTransferHandler arrayListHandler = new ReportingListTransferHandler();
        arrayListHandler.blankDNDListing = blankDNDListing;
        columns.add(initSequenceList(arrayListHandler));


        // rows
        JPanel rows = new JPanel(new BorderLayout());
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        columns.add(rows);


        JPanel sub_columns = new JPanel(new BorderLayout());
        sub_columns.setLayout(new BoxLayout(sub_columns, BoxLayout.X_AXIS));
        rows.add(sub_columns, BorderLayout.NORTH);

        sub_columns.add(addDragListTarget("All the same", arrayListHandler, allSameList));
        sub_columns.add(addDragListTarget("All different", arrayListHandler, allDiffList));

        // Buttons
        root.add(addButtons(), BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        // Position the dialog window
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(new Point((dim.width - width) / 2, (dim.height - height) / 2));
        setResizable(true);
        setVisible(true);

    }

    private JPanel initSequenceList(final ReportingListTransferHandler listHandler) {
        JPanel main = new JPanel(new BorderLayout());
        final JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Sequences"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        JScrollPane scroll = new JScrollPane(content);
        main.add(scroll, BorderLayout.CENTER);

        JList seqList = new JList();
        final DefaultListModel model = new DefaultListModel();
        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent listDataEvent) {
            }

            @Override
            public void intervalRemoved(ListDataEvent listDataEvent) {
                if (model.size() < 1) {
                    model.addElement(blankDNDListing);
                }
            }

            @Override
            public void contentsChanged(ListDataEvent listDataEvent) {
            }
        });


        // create hashmap of sequence name -> sequence object
        for (FeaturedSequence fs : m_holder.getSequences()) {
            String s = (nameMap.size() + 1) + ": " + fs.getName();
            nameMap.put(s, fs);
            model.addElement(s);
        }


        seqList.setModel(model);
        seqList.setName("Sequences");
        seqList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        seqList.setTransferHandler(listHandler);
        seqList.setDragEnabled(true);
        content.add(seqList);
        return main;
    }


    private JPanel addDragListTarget(final String text, final ReportingListTransferHandler arrayListHandler, DefaultListModel model) {
        JPanel main = new JPanel(new BorderLayout());
        final JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(text), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        JScrollPane scroll = new JScrollPane(content);
        main.add(scroll, BorderLayout.CENTER);

        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent listDataEvent) {
            }

            @Override
            public void intervalRemoved(ListDataEvent listDataEvent) {
                DefaultListModel listModel = (DefaultListModel) listDataEvent.getSource();
                if (listModel.size() < 1) {
                    listModel.addElement(blankDNDListing);
                }

            }

            @Override
            public void contentsChanged(ListDataEvent listDataEvent) {
            }
        });


        JList list = new JList(model);
        list.setName(text);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setTransferHandler(arrayListHandler);
        list.setDragEnabled(true);

        model.addElement(blankDNDListing);
        content.add(list);
        return main;
    }

    public JPanel addButtons() {
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
        btns.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

//        create spinner for threshold value
        JLabel spinnerLabel = new JLabel("Tolerance");
        SpinnerModel model = new SpinnerNumberModel(0, 0, (m_holder.getSequences().length - 1), 1);
        final JSpinner spinner = new JSpinner(model);
        spinner.setMaximumSize(new Dimension(10, 100));
        spinner.setName("Tolerance");

        final JCheckBox indels = new JCheckBox();
        indels.setText("Log possible indel locations");

        JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JEditorPane description = new JEditorPane();
                description.setBorder(BorderFactory.createEtchedBorder(1));
                description.setSize(new Dimension(500, 300));
                description.setEditable(false);
                description.setContentType("text/html");
                description.setText("<html>\n" +
                        "<body>\n" +
                        "<p>\n" +
                        "&bull; Positions will be noted where sequences in the \"All the same\" group share a common nucleotide and sequences in the<br> \n" +
                        "\"All different\" group differ from this nucleotide.<br>\n" +
                        "&bull; A match between a sequence in the \"All different\" group and this common nucleotide in may be tolerated up to a specified<br>\n" +
                        "number of times by setting a tolerance value. <br>\n" +
                        "&bull; Comments are annotated on the first sequence in the \"All the same\" group for tolerance values &lt 4 using the following <br>\n" +
                        "colours distinguish the number of tolerated sequences at a given position:<br>\n" +
                        "<font color=\"#FF00FF\">No tolerated genomes, </font>\n" +
                        "<font color=\"red\">1 tolerated genome, </font>\n" +
                        "<font color=\"blue\">2 tolerated genomes, </font>\n" +
                        "<font color=\"green\">3 tolerated genomes</font><br>\n" +
                        "Possible indel locations are noted as occurring from two possible situations: <br>\n" +
                        "&bull; A '-' denotes when all sequences in the \"All the same\" group are gapped at some position, and at least one sequence in <br>\n" +
                        "the \"All different\" is not gapped. <br>\n" +
                        "&bull; A '+' denotes a position where there is some common base in the \"All the same\" group and all sequences in the <br>\n" +
                        "\"All different\" group are gapped at that position.<br>\n" +
                        "</p>\n" +
                        "</body>\n" +
                        "</html>\n");
                JOptionPane.showMessageDialog(DifferencesEditorPane2.this, description, "Find Differences Help", JOptionPane.INFORMATION_MESSAGE, null);

            }
        });

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        dispose();
                    }
                });

        JButton ok = new JButton("Ok");
        ok.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        allSameList.removeElement(blankDNDListing);
                        allDiffList.removeElement(blankDNDListing);
                        allSame = allSameList.toArray();
                        allDiff = allDiffList.toArray();
                        Thread t = new Thread("Find Differences/Matching") {
                            public void run() {
                                findDifferences((Integer) spinner.getValue(), indels.isSelected());
                            }
                        };
                        t.start();
                        dispose();
                    }
                });


        btns.add(Box.createHorizontalGlue());
        btns.add(spinnerLabel);
        btns.add(spinner);
        btns.add(indels);
        btns.add(help);
        btns.add(ok);
        btns.add(cancel);

        return btns;

    }

    public void findDifferences(int tolerance, boolean indels) {

        ArrayList<String> allSameSeq = new ArrayList<String>();
        ArrayList<String> allDiffSeq = new ArrayList<String>();

        int shortest = Integer.MAX_VALUE;
        ArrayList<String> toleratedGenomes = new ArrayList<String>();
        HashMap<String, Integer> groupFrequency = new HashMap<String, Integer>();

        diffLog += "All The Same: \n";
        System.out.println("All the same: \n");
        for (Object key : allSame) {
            System.out.println(key);
            if (nameMap.containsKey(key)) {
                diffLog += key + "\n";
                FeaturedSequence fs = (FeaturedSequence) nameMap.get(key);
                allSameSeq.add(fs.toString());
                if (shortest > fs.toString().length())
                    shortest = fs.toString().length();
            }
        }

        diffLog += "\nAll Different: \n";
        System.out.println("All different: \n");
        for (Object key : allDiff) {
            System.out.println(key);
            if (nameMap.containsKey(key)) {
                diffLog += key + "\n";
                FeaturedSequence fs = (FeaturedSequence) nameMap.get(key);
                allDiffSeq.add(fs.toString());
                if (shortest > fs.toString().length())
                    shortest = fs.toString().length();
            }
        }
        diffLog += "\n----------------------------------------------------------------------------------------\n";

        int oldTol = -1;
        int length = 0;
        int count = 0;
        String commentNote = "";
        Comment comment;

        // for each nucleotide position in the sequences
        for (int i = 0; i < shortest; i++) {
            toleratedGenomes.clear();

            int tolerated = 0;
            boolean foundDiff = true;
            boolean foundSame = true;
            boolean foundIndel = true;


            char sameChar = allSameSeq.get(0).charAt(i);

            // check that all sequences in the allSame group have the same nucleotide at position i
            if (foundSame) {
                for (String s : allSameSeq) {
                    if (s.charAt(i) != sameChar) {
                        foundSame = false;
                        break;
                    }
                }
            }

            // check that all sequences in the allDiff group have a different nucleotide at position i than the sameChar
            // a mismatch can be tolerated up to n = tolerance times
            if (foundSame) {
                for (int j = 0; j < allDiffSeq.size(); j++) {
                    // if every char in allDiff is '-' then insertion has occurred
                    char diffChar = allDiffSeq.get(j).charAt(i);
                    if (diffChar != '-') foundIndel = false;
                    if (diffChar == sameChar) {
                        if (++tolerated > tolerance) {
                            foundDiff = false;
                            break;
                        }
                        toleratedGenomes.add(allDiff[j].toString());
                    }
                }
            }
            if (foundIndel) foundDiff = false;

            if (foundSame && sameChar == '-' && indels) {
                if (!foundIndel) {
                    foundSame = false;
                    diffLog += (i + 1) + " Possible indel -\n";
                    // write something to log for the possible deletion event
                }
            } else if (foundIndel && foundSame && indels) {
                // write something in log about possible insertion event
                diffLog += (i + 1) + " Possible indel +\n";

            }

            // found a difference (two cases: increase the length of current comment or if tolerated value has changed
            // draw the old comment and start a new one)
            // print to log positionally
            if (foundSame && foundDiff) {
                count++;
                // starting a new comment or increase the length of current comment
                if (oldTol == tolerated || oldTol < 0) {
                    length++;
                    oldTol = tolerated;
                } else {
                    // previous comment has been ended by a difference with a different tolerated value
                    comment = new Comment(i, length, oldTol, commentNote);
                    diffLog += commentNote;
                    comments.add(comment);
                    oldTol = tolerated;
                    length = 1;
                    commentNote = "";
                }
                commentNote += i;
                if (tolerance > 0) {
                    commentNote += " | " + oldTol;
                    if (toleratedGenomes.size() > 0) {
                        commentNote += " | ";
                        String key = "";
                        for (int j = 0; j < toleratedGenomes.size(); j++) {
                            key += toleratedGenomes.get(j);
                            key += (j == (toleratedGenomes.size() - 1) ? "" : ", ");
                        }
                        commentNote += key;
                        key += " | " + toleratedGenomes.size();

                        if (groupFrequency.containsKey(key)) {
                            groupFrequency.put(key, groupFrequency.get(key) + 1);
                        } else {
                            groupFrequency.put(key, 1);
                        }
                    }
                }
                commentNote += "\n";
            }

            // no difference found
            else {
                if (length > 0) {
                    comment = new Comment(i, length, oldTol, commentNote);
                    comments.add(comment);
                    diffLog += commentNote;
                }
                oldTol = -1;
                length = 0;
                commentNote = "";
            }
        }
        System.out.println("Finished!");
        diffLog += "Total: " + count + "\n";
        if (tolerance > 0) {

            diffLog += "\n----------------------------------------------------------------------------------------\n";

            diffLog += "Group frequencies of tolerated genomes:\n";
            for (Object key : groupFrequency.keySet()) {
                diffLog += groupFrequency.get(key) + " | " + (String) key + "\n";
            }
        }
        diffLog += "\n----------------------------------------D-O-N-E----------------------------------------\n";

        if (tolerance <= 3) {
            progressMonitor = new ProgressMonitor(DifferencesEditorPane2.this, "Drawing comments", "", 0, 100);
            progressMonitor.setProgress(0);
            commentTask = new CommentTask();
            commentTask.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("progress" == evt.getPropertyName()) {
                        int progress = (Integer) evt.getNewValue();
                        progressMonitor.setProgress(progress);
                        String message = String.format("Completed %d%%.\n", progress);
                        progressMonitor.setNote(message);
                        if (progressMonitor.isCanceled() || commentTask.isDone()) {
                            Toolkit.getDefaultToolkit().beep();
                            if (progressMonitor.isCanceled()) {
                                commentTask.cancel(true);
                            } else {
                            }
                        }
                    }

                }
            });
            commentTask.execute();
        }
    }

    class CommentTask extends SwingWorker<Void, Void> {

        @Override
        public Void doInBackground() {
            int progress = 0;
            int count = 0;
            setProgress(0);
            int step = comments.size() / 100;
            for (Comment c : comments) {
                c.createComment();
                count++;
                if (count * step > progress)
                    setProgress(++progress);
            }
            return null;
        }

    }

    public String returnLog() {
        return diffLog;
    }

    private class Comment {
        int start;
        int end;
        int tolerated;
        String commentNote;

        private Comment(int i, int length, int tolerated, String commentNote) {
            this.start = i - length;
            this.end = i - 1;
            this.tolerated = tolerated;
            this.commentNote = commentNote;
        }

        private void createComment() {
            FeaturedSequence fs = (FeaturedSequence) nameMap.get(allSame[0]);

            start = fs.getAbsolutePosition(start);
            end = fs.getAbsolutePosition(end);
            // annotate comments to first sequence in all same group
            Color color = commentColors[tolerated];
            Location loc = LocationTools.makeLocation(start, end);
            String title = "" + tolerated;

            FeatureTools.createUserComment(fs, loc, p.getDisplayStrand(), title, commentNote,
                    Color.BLACK, color);
            p.revalidate();
            p.resetView();
        }

        private void setCommentPosition(int start, int end) {
            this.start = start - 1;
            this.end = end - 1;
        }

    }
}
