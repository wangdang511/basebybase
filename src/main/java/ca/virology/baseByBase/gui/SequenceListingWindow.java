package ca.virology.baseByBase.gui;

import ca.virology.lib.io.MultiFileFilter;
import ca.virology.lib.util.gui.UITools;
import ca.virology.lib.util.common.SequenceUtility;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;


/**
 * This class displays a scrollable frame which gives access to the entirety of
 * a sequence.
 *
 * @author Ryan Brodie
 */
public class SequenceListingWindow
        extends JFrame {
    //~ Static fields/initializers /////////////////////////////////////////////

    /**
     * The default length of lines in this window
     */
    protected static final int LINELENGTH = 70;

    /**
     * The default number of lines to display
     */
    protected static final int NUMLINES = 30;

    /**
     * The default marker spacing used to dispay the header
     */
    protected static final int HEADERMODULO = 10;

    /**
     * The default width of line numbers
     */
    protected static final int LINENUMWIDTH = 7;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected int m_orgID;
    protected String m_symbols;
    protected final JTextArea m_text = new JTextArea();
    protected final JTextArea m_base = new JTextArea();
    protected final Highlighter m_highlight = new DefaultHighlighter();
    protected final Map m_painters = new HashMap();
    protected final StatusBar m_status = new StatusBar("Sequence Viewer");

    /**
     * map of location,String type pairs to highlight
     */
    protected final Map m_locations = new TreeMap(new ca.virology.baseByBase.util.LocationComparator());
    protected StrandedFeature.Strand m_strand = StrandedFeature.POSITIVE;
    protected boolean m_showProtien = false;
    protected final JScrollBar m_scroll = new JScrollBar(JScrollBar.VERTICAL);
    protected int m_lines;
    protected int m_curLine;
    protected int m_linesShown;
    protected int m_lineLength;
    protected int m_lastLine;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new window displaying the sequence of the given organism
     *
     * @param seqString   The organism to show the sequence for
     * @param strand      The strand of the organism to show
     * @param showProtien if true, this will show the amino acid sequence (NOT
     *                    IMPLEMENTED YET)
     */
    public SequenceListingWindow(
            String seqString,
            StrandedFeature.Strand strand,
            boolean showProtien) {
        super();
        m_strand = strand;

        if (strand == StrandedFeature.POSITIVE) {
            m_symbols = seqString;
        } else {
            m_symbols = SequenceUtility.make_simple_complement(seqString);
        }

        m_linesShown = NUMLINES;
        m_lineLength = LINELENGTH;
        m_lines = (m_symbols.length() / m_lineLength) + m_linesShown;
        m_curLine = 0;
        m_lastLine = 0;

        initHighlight();
        initComponents();
        refreshScrollBar();
        refreshText();

        setTitle("Sequence Viewer: " +
                ((strand == StrandedFeature.POSITIVE) ? "Positive" : "Negative") +
                " Strand");
        setSize(new Dimension(700, 660));

//      Position the dialog window
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = getWidth();
        int h = getHeight();
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        setLocation(x, y);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Select the designated region on the sequence display (this may manifest
     * itself in any number of ways depending on the further implementation.
     *
     * @param min    the beginning position on the sequence
     * @param max    the ending position on the sequence
     * @param strand the strand on which the selection should operate
     * @param desc   the textual description of the region to be selected.
     */
    public void selectRegion(
            final int min,
            final int max,
            StrandedFeature.Strand strand,
            String desc) {
        if (strand != m_strand) { /* Debug.println("Wrong Strand");*/
            return;
        }

        m_locations.clear();

        if (strand == StrandedFeature.POSITIVE) {
            m_locations.put(
                    new RangeLocation(min - 1, max),
                    "selection");
            refreshHighlight();
            SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            scrollTo(min);
                        }
                    });
        } else {
            m_locations.put(
                    new RangeLocation(m_symbols.length() - max,
                            m_symbols.length() - min + 1),
                    "selection");
            refreshHighlight();
            SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            scrollTo(m_symbols.length() - max);
                        }
                    });
        }
    }

    /**
     * sets the length of lines to be used in this panel
     *
     * @param lineLength the new length of lines
     */
    public void setLineLength(int lineLength) {
        m_lineLength = lineLength;
        m_lastLine = m_curLine; // done so refresh acts as an initialize
        m_lines = (m_symbols.length() / m_lineLength) + m_linesShown;
        refreshText();
        refreshScrollBar();
        refreshHighlight();
    }

    /**
     * returns the length of lines displayed in this panel
     *
     * @return line length
     */
    public int getLineLength() {
        return m_lineLength;
    }

    /**
     * set the number of lines displayed in this panel
     *
     * @param lineCount the new number of lines
     */
    public void setLineCount(int lineCount) {
        m_linesShown = lineCount;
        m_lastLine = m_curLine; // done so refresh acts as an initialize
        m_lines = (m_symbols.length() / m_lineLength) + m_linesShown;
        refreshText();
        refreshScrollBar();
        refreshHighlight();
    }

    /**
     * returns the number of lines displayed by this window
     *
     * @return line count
     */
    public int getLineCount() {
        return m_linesShown;
    }

    /**
     * This will scroll the window to the given sequence location
     *
     * @param seqLocation the location to scroll to
     */
    public void scrollTo(int seqLocation) {
        int line = seqLocation / (m_lineLength);
        setLine(line);
        refreshScrollBar();
        refreshText();
        refreshHighlight();
    }

    /**
     * sets the line variable
     *
     * @param line The new line value
     */
    protected void setLine(int line) {
        m_lastLine = m_curLine;
        m_curLine = line;
    }

    /**
     * initiallizes the highlighters, etc.
     */
    protected void initHighlight() {
        DefaultHighlighter.DefaultHighlightPainter selection = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);
        m_painters.put("selection", selection);

        m_text.setHighlighter(m_highlight);
    }

    /**
     * initializes the swing components
     */
    protected void initComponents() {
        m_text.setFont(new Font("Serif", Font.PLAIN, 12));
        m_base.setFont(new Font("Serif", Font.PLAIN, 12));

        m_base.setPreferredSize(new Dimension(60, 30));

        JPanel main = new JPanel(new BorderLayout());
        JScrollPane spl =
                new JScrollPane(m_base, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane spr =
                new JScrollPane(m_text, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JSplitPane split = new JSplitPane();
        split.setRightComponent(spl);
        split.setLeftComponent(spr);
        split.setDividerLocation(580);

        main.add(split, BorderLayout.CENTER);
        main.add(m_scroll, BorderLayout.EAST);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JButton close = new JButton("Close");
        close.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        dispose();
                    }
                });
        buttons.add(Box.createHorizontalGlue());
        buttons.add(close);

        JButton b1 = new JButton("hl");
        final JTextField f1 = new JTextField();
        final JTextField f2 = new JTextField();
        b1.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        selectRegion(
                                Integer.parseInt(f1.getText()),
                                Integer.parseInt(f2.getText()),
                                m_strand,
                                "Selection");
                        scrollTo(Integer.parseInt(f1.getText()));
                    }
                });

        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        mb.add(menu);

        JMenuItem mi = new JMenuItem("Save as Fasta...");
        mi.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        saveText();
                    }
                });
        menu.add(mi);

        m_text.addCaretListener( // may not do this 
                new CaretListener() {
                    public void caretUpdate(CaretEvent ev) {
                        //selectRegion( m_orgID, ev.getDot(), ev.getMark(), m_strand, "Mouse Selection" );
                    }
                });

        m_scroll.addAdjustmentListener(
                new AdjustmentListener() {
                    public void adjustmentValueChanged(AdjustmentEvent ev) {
                        setLine(ev.getValue());
                        refreshText();
                        refreshHighlight();
                    }
                });

        m_base.setEditable(false);
        m_text.setEditable(false);

        Container c = getContentPane();

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        c.setLayout(new BorderLayout());
        p.add(main, BorderLayout.CENTER);
        p.add(buttons, BorderLayout.SOUTH);
        c.add(p, BorderLayout.CENTER);

        setJMenuBar(mb);
    }

    /**
     * Analyzes the variables in this class and sets the scroll bar values
     * accordingly
     */
    protected void refreshScrollBar() {
        m_scroll.setValues(m_curLine, m_linesShown, 0, m_lines - m_linesShown);
    }

    /**
     * Refreshes the text in the display according to the dimensions and
     * current view of the organism sequence.  This simply resets the text. It
     * no longer does  intelligent refreshing at the cost of speed and the
     * benefit of rock solid stability.
     */
    protected void refreshText() {
        m_text.setText(getHeader() + "\n");
        m_base.setText("\n");

        for (int i = m_curLine; i <= (m_curLine + m_linesShown); i++) {
            m_base.append(((i + 1) * (m_lineLength)) + "\n");
            m_text.append(getSeqLine(i) + "\n");
        }
    }

    /**
     * Refreshes the text in the display according to the dimensions and
     * current view of the organism sequence<br>
     * <B> This is the old way!!! -- really buggy, but left in for reference
     * </B>
     */
    protected void oldRefreshText() {
        System.out.println("lines:   " + m_lines);
        System.out.println("last:    " + m_lastLine);
        System.out.println("current: " + m_curLine);
        System.out.println("shown:   " + m_linesShown);
        System.out.println("length:  " + m_lineLength);

        if (Math.abs(m_curLine - m_lastLine) > m_linesShown) {
            m_lastLine = m_curLine;
        }

        try {
            if (m_curLine == m_lastLine) { // just load text striaght up
                m_text.setText(getHeader() + "\n");
                m_base.setText("\n");

                for (int i = m_curLine; i <= (m_curLine + m_linesShown); i++) {
                    m_base.append(((i + 1) * (m_lineLength)) + "\n");
                    m_text.append(getSeqLine(i) + "\n");
                }
            } else if (m_curLine < m_lastLine) { // insert lines to front, remove from end

                for (int i = m_lastLine - 1; i >= m_curLine; i--) {
                    m_text.insert(
                            getSeqLine(i) + "\n",
                            m_text.getLineStartOffset(1));
                    m_base.insert(
                            ((i + 1) * (m_lineLength)) + "\n",
                            m_base.getLineStartOffset(1));
                    m_text.replaceRange(
                            null,
                            m_text.getLineStartOffset(m_text.getLineCount() - 1),
                            m_text.getLineEndOffset(m_text.getLineCount() - 1));
                    m_base.replaceRange(
                            null,
                            m_base.getLineStartOffset(m_base.getLineCount() - 1),
                            m_base.getLineEndOffset(m_base.getLineCount() - 1));
                }
            } else { // remove lines from beginning, append to end

                for (int i = m_lastLine; i < m_curLine; i++) {
                    m_text.replaceRange(
                            null,
                            m_text.getLineStartOffset(1),
                            m_text.getLineEndOffset(1));
                    m_base.replaceRange(
                            null,
                            m_base.getLineStartOffset(1),
                            m_base.getLineEndOffset(1));

                    if ((i + m_linesShown) <= m_lines) {
                        m_text.append(getSeqLine(i + 1 + m_linesShown) + "\n");
                        m_base.append(((i + 2 + m_linesShown) * (m_lineLength)) +
                                "\n");
                    }
                }
            }
        } catch (BadLocationException ex) {
            UITools.showError("Error refreshing text: " + ex.getMessage(), this);
        }
    }

    /**
     * gets the header string used for visualization
     *
     * @return header string
     */
    protected String getHeader() {
        StringBuffer b = new StringBuffer();

        for (int i = 1; i <= m_lineLength; i++) {
            if ((i % HEADERMODULO) == 0) {
                b.append("|");
            } else {
                b.append("-");
            }
        }

        return b.toString();
    }

    /**
     * returns the given line of the sequence which will be used to display
     *
     * @param line the line to return
     * @return sequnce data
     */
    protected String getSeqLine(int line) {
        if (line == 0) {
            return m_symbols.substring(0, m_lineLength)
                    .toUpperCase();
        } else {
            int start = (line * (m_lineLength));
            int end = (start + m_lineLength);

            if (end > m_symbols.length()) {
                end = m_symbols.length();
            }

            if (start > m_symbols.length()) {
                return "";
            } else {
                return m_symbols.substring(start, end)
                        .toUpperCase();
            }
        }
    }

    /**
     * returns the caret mark in the text window which represents the point
     * nearest to the given sequence location.  If the location exists above
     * the viewable location, the first offset is returned, if the location
     * exists below the viewable location, the last offset is returned, and if
     * the location exists within the viewable location its offset is
     * returned.
     *
     * @param seqLocation The location to get a mark for
     * @return an offset or -1 if an error occured
     */
    protected int getNearestMark(int seqLocation) {
        int line = seqLocation / (m_lineLength);
        line++;

        try {
            if (line < m_curLine) {
                return m_text.getLineStartOffset(1);
            } else if (line > (m_curLine + m_linesShown)) {
                return m_text.getLineEndOffset(m_linesShown + 1);
            } else {
                return m_text.getLineStartOffset(line - m_curLine) +
                        (seqLocation % (m_lineLength));
            }
        } catch (BadLocationException ex) {
            return -1;
        }
    }

    /**
     * refreshes the highlights on the screen based on the current view
     * parameters
     */
    protected void refreshHighlight() {
        Set s = m_locations.keySet();
        int top = m_curLine * (m_lineLength);
        int bottom = (top + m_linesShown + 1) * (m_lineLength);
        Location view = new RangeLocation(top, bottom);

        for (Iterator i = s.iterator(); i.hasNext(); ) {
            Location l = (Location) i.next();

            if (LocationTools.overlaps(l, view)) {
                try {
                    int start = getNearestMark(l.getMin());
                    int stop = getNearestMark(l.getMax());

                    DefaultHighlighter.DefaultHighlightPainter selection;
                    selection = (DefaultHighlighter.DefaultHighlightPainter) m_painters.get(m_locations.get(l));

                    if (selection == null) {
                        selection = (DefaultHighlighter.DefaultHighlightPainter) m_painters.get("selection");
                    }

                    if ((m_highlight.getHighlights()).length > 0) {
                        m_highlight.removeAllHighlights();
                    }

                    if (start != stop) {
                        m_highlight.addHighlight(start, stop, selection);
                    }

                    m_text.updateUI();
                } catch (BadLocationException ex) {
                    UITools.showWarning(
                            "Location not in Sequence",
                            this.getContentPane());
                }
            }
        }
    }

    /**
     * save the text to disk
     */
    protected void saveText() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Sequence Data");

        MultiFileFilter ff1 = new MultiFileFilter("Fasta Format");
        ff1.addExtension("fasta");
        fc.addChoosableFileFilter(ff1);
        fc.setFileFilter(ff1);

        try {
            if (fc.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(f));

                String head = "> Consensus Sequence Data";

                out.write(
                        head,
                        0,
                        head.length());
                out.newLine();
                out.write(
                        m_symbols,
                        0,
                        m_symbols.length());

                out.flush();
                out.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
