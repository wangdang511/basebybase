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

import ca.virology.baseByBase.DiffEditor;
import ca.virology.baseByBase.data.*;
import ca.virology.baseByBase.gui.CodeHop.CodeHopWizard;
import ca.virology.baseByBase.io.DiffEditFeaturedSequenceReader;
import ca.virology.baseByBase.io.VocsTools;
import ca.virology.baseByBase.util.AppConstants;
import ca.virology.baseByBase.util.FileDrop;
import ca.virology.baseByBase.util.GenePos;
import ca.virology.baseByBase.util.ITRCalculator;
import ca.virology.lib.io.MultiFileFilter;
import ca.virology.lib.io.VGOExporter;
import ca.virology.lib.io.reader.*;
import ca.virology.lib.io.sequenceData.AnnotationKeys;
import ca.virology.lib.io.sequenceData.EditableSequence;
import ca.virology.lib.io.sequenceData.FeatureType;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.io.tools.FeatureFileTools;
import ca.virology.lib.io.tools.SequenceTools;
import ca.virology.lib.io.writer.FeaturedSequenceWriter;
import ca.virology.lib.prefs.BBBPrefs;
import ca.virology.lib.prefs.DBPrefs;
import ca.virology.lib.prefs.UserBookmarkPrefs;
import ca.virology.lib.search.SearchTools;
import ca.virology.lib.search.gui.FuzzySearchPanel;
import ca.virology.lib.util.common.Args;
import ca.virology.lib.util.common.SequenceUtility;
import ca.virology.lib.util.gui.*;
import ca.virology.lib2.common.service.bio.BioMessages;
import javafx.util.Pair;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.ChangeVetoException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.biojava.bio.seq.StrandedFeature.NEGATIVE;
import static org.biojava.bio.seq.StrandedFeature.POSITIVE;

/**
 * This is the main application window for the DiffEditor application
 *
 * @author Ryan Brodie
 */
public class DiffEditorFrame extends JFrame implements FeaturedSequenceModel {

    public static Vector seqsFromMafftAdd = new Vector();
    public static boolean mafftadd = false;
    public static boolean mafftaddfrag = false;
    //~ Static fields/initializers /////////////////////////////////////////////
    public ActionListener compList;
    public static final Dimension DEFAULT_SIZE = new Dimension(1024, 350);
    protected static final Dimension TOOLBAR_BUTTON_SIZE = new Dimension(21, 22);
    protected static final String MODIFIED_TEXT = " (modified)";
    protected static int c_windowCount = 0;
    private static BookmarkList s_bookmarkList = null;

    public static final int AA_SEQUENCE = 0;
    public static final int DNA_SEQUENCE = 1;

    //~ Instance fields ////////////////////////////////////////////////////////
    protected PrimaryPanel m_dataPanel;
    protected final StatusBar m_status = new StatusBar("");
    protected final JPanel m_splash = new JPanel();
    protected final DefaultComboBoxModel m_geneBoxModel = new DefaultComboBoxModel();
    protected final JPanel m_main = new JPanel();
    protected boolean snip = false;
    protected boolean m_process;
    protected boolean m_workSavable = false;
    protected String m_workFilename = "";
    String notebook = "";
    protected int m_mouseMode = EditPanel.SELECT_MODE;
    private final DBPrefs m_dbpref;
    //To facilitate copy
    protected final Toolkit m_toolkit = Toolkit.getDefaultToolkit();
    protected final java.awt.datatransfer.Clipboard m_clipboard = m_toolkit.getSystemClipboard();
    protected mRNAs mrnas;
    //
    protected File m_currentDirectory = null;
    protected JPopupMenu m_popup = new JPopupMenu();
    protected JButton m_loadButton = null;
    protected JFrame theFrame;
    JRadioButtonMenuItem comp1;
    JRadioButtonMenuItem comp2;
    JRadioButtonMenuItem comp3;
    NotebookFrame nb_frame = null;
    /**
     * Set of components that need sequences open to be valid
     */
    protected Set<JComponent> m_restrictedComponents = new HashSet<JComponent>();

    /**
     * strand button
     */
    protected JButton m_strandButton;
    protected JMenuItem m_muscle;//to enable/disable muscle depending on AA seq or DNA seq
    int primerRange;
    int selectedGeneIndex = '0';

    private boolean m_consensusRefreshRunning = false;
    private Thread m_consensusRefreshThread = null;
    private Thread mRNAPlotter = null;

    boolean continueLoop;
    boolean breakLoop;

    /**
     * Strip columns menu item
     */
    protected JMenuItem m_strip; //to update "Remove Columns.. " menu item depending on AA seq or DNA seq

    protected JMenuItem MI_RNAOverview;

    private File file; // Used to Import an analysis file

    public static String letters;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Construct a new <code>DiffEditorFrame</code>
     */
    public DiffEditorFrame(DBPrefs dbpref) {
        super(AppConstants.APP_TITLE);

        //importOldPrefs();
        theFrame = this;
        m_dbpref = dbpref;
        setPrimaryPanel(new PrimaryPanel());
        getPrimaryPanel().setMouseMode(m_mouseMode);
        getPrimaryPanel().setChannelPreferences(getShownChannels());

        ++c_windowCount;

        m_process = false;

        if (s_bookmarkList == null) {
            new UserBookmarkPrefs(Args.getInstance().get_bbbprefs(), "bbb");
            s_bookmarkList = new BookmarkList(UserBookmarkPrefs.getInstance("bbb"));
            s_bookmarkList.addSystemBookmark("Viral Bioinformatics Resource Center", "https://4virology.net/");
        }

        initListeners();
        initUI();
        new FileDrop(this, new FileImportDropListener());
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent ev) {
                AppConstants.setSequenceHolder(DiffEditorFrame.this);
            }

            public void windowClosing(WindowEvent ev) {
                m_consensusRefreshRunning = false;
            }
        });

        addWindowFocusListener(new WindowFocusListener() {
            public void windowGainedFocus(WindowEvent e) {
                DiffEditor.frame = (DiffEditorFrame) e.getWindow();
            }

            public void windowLostFocus(WindowEvent e) {
            }
        });

    }

    public static BookmarkList getBookmarkList() {
        return s_bookmarkList;
    }

    public void importOldPrefs() {


        BBBPrefs bbbPrefs = BBBPrefs.getInstance();

    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * This method loads a file which is supplied as a startup item
     *
     * @param file is the name of the file to be loaded
     * @author asyed
     */
    public boolean setBBBfile(String file) {

        try {

            String ext = (file.substring(file.lastIndexOf(".") + 1, file.length())).toUpperCase();
            URL url = new URL(file);

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            String allLines = "";
            while ((inputLine = br.readLine()) != null)
                allLines = allLines + inputLine + "\n";

            getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
            m_status.setText("Adding Sequences from File URL (Startup) ...");

            FeaturedSequenceReader fsr = DiffEditFeaturedSequenceReader.createFeaturedSequenceReader(allLines, ext);
            final ListIterator li = fsr.getSequences();

            Vector<FeaturedSequence> fileSequences = new Vector<FeaturedSequence>();

            while (li.hasNext()) {
                fileSequences.add((FeaturedSequence) li.next());
            }

            appendSequences(fileSequences.toArray(new FeaturedSequence[0]));

            return true;
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
        } catch (IOException ex) {
            ex.printStackTrace();
            UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
        } catch (Exception iex) {
            iex.printStackTrace();
            UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
        } finally {
            m_status.clear();
            getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        return false;

    }

    /**
     * Set the name of this frame (not the title!)
     *
     * @param name The name string to use
     */
    public void setName(String name) {
        super.setName(name);

        if (getPrimaryPanel() != null) {
            getPrimaryPanel().setName(name + "::PrimaryPanel");
        }
    }

    /**
     * indicate whether or not this window has control to close the  VM process
     * on exit.
     *
     * @param process
     */
    public void setProcess(boolean process) {
        m_process = process;
    }

    private synchronized boolean isConsensusRefreshRunning() {
        return m_consensusRefreshRunning;
    }

    protected synchronized void resetConsensusRefreshRunning(boolean newValue) {
        m_consensusRefreshRunning = newValue;
    }

    /**
     * This closes the window appropriately depending on whether or not this is
     * a process or not.
     */
    public void close() {

        if (c_windowCount <= 1) {
            if (!checkForQuit()) {
                return;
            }
        }

        // cleanup code goes here ...
        if (!checkForSave()) {
            return;
        }

        --c_windowCount;

        // end cleanup code
        if (m_process && (c_windowCount <= 0)) {
            System.exit(0);
        } else {
            dispose();
        }
    }

    /**
     * prevents all sequences in a given list from being shown
     *
     * @param hidden The sequences to hide
     */
    public void setSequenceFilter(FeaturedSequence[] hidden) {
        if (getPrimaryPanel() != null) {
            getPrimaryPanel().setSequenceFilter(hidden);
        }
    }

    /**
     * Get the sequences held by this window
     *
     * @return The sequences in an array
     */
    public FeaturedSequence[] getSequences() {
        if (getPrimaryPanel() != null) {
            return getPrimaryPanel().getSequences();
        } else {
            return new FeaturedSequence[0];
        }
    }

    /**
     * Get the sequences currently visible on the screen
     *
     * @return The sequences visible in an array
     */
    public FeaturedSequence[] getVisibleSequences() {
        if (getPrimaryPanel() != null) {
            return getPrimaryPanel().getVisibleSequences();
        } else {
            return new FeaturedSequence[0];
        }
    }

    /**
     * Get the sequences selected in this window
     *
     * @return The sequences in an array
     */
    public FeaturedSequence[] getSelectedSequences() {
        if (getPrimaryPanel() != null) {
            return getPrimaryPanel().getSelectedSequences();
        } else {
            return new FeaturedSequence[0];
        }
    }

    public int[] getSelectedIndices() {
        if (getPrimaryPanel() != null) {
            return getPrimaryPanel().getSelectedIndices();
        } else {
            return null;
        }


    }


    /**
     * Scroll the window to the given position
     *
     * @param location the gapped position to scroll to
     */
    public void scrollToLocation(int location) {
        if (getPrimaryPanel() != null) {
            getPrimaryPanel().scrollToLocation(0, location, true);
        }
    }

    /**
     * Set the mouse mode for this window
     *
     * @param mode The mode to set the window to, Must be one of
     *             <CODE>EditPanel.SELECT_MODE</CODE> or
     *             <CODE>EditPanel.SELECT_MODE</CODE>
     */
    public void setMouseMode(int mode) {
        m_mouseMode = mode;

        if (getPrimaryPanel() != null) {
            getPrimaryPanel().setMouseMode(mode);
        }
    }

    /**
     * check if the user really wants to quit the program
     *
     * @return true if the user has said they want to quit, or have otherwise
     * specified that they don't want to be warned.
     */
    public boolean checkForQuit() {
        boolean warn = Boolean.valueOf(BBBPrefs.getInstance().get_bbbPref("warn.onQuit")).booleanValue();

        if (!warn) {
            return true;
        }

        return JOptionPane.showConfirmDialog(getContentPane(), "Are you sure you would like to quit?", "Base-by-Base Quit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /**
     * This method will check to see if there have been any sequence
     * modifications made to the underlying sequences displayed on the screen
     *
     * @return True if there have been modifications made
     */
    public boolean checkForSave() {
        boolean nb_changed = false;
        try {
            if (!notebook.equals(nb_frame.getText())) {
                nb_changed = true;
            }
        } catch (Exception e) {
        }

        if (UndoHandler.getInstance().isModified() || nb_changed) {
            String message = "";

            try {
                notebook = nb_frame.getText();
            } catch (NullPointerException ne) {
            }

            if (isSavable()) {
                message = getWorkFilename() + " sequences modified, do you wish to save?";
            } else {
                message = "Sequences modified, do you wish to save?";
            }

            int val = JOptionPane.showConfirmDialog(getContentPane(), message, "Sequences Modified", JOptionPane.YES_NO_CANCEL_OPTION);

            if (val == JOptionPane.YES_OPTION) {
                if (isSavable()) {
                    return saveSequences(getWorkFilename());
                } else {
                    return saveSequences();
                }
            }

            return (val != JOptionPane.CANCEL_OPTION);
        } else {
            return true;
        }
    }

    /**
     * equalize the lengths in the alignment.
     */
    protected void equalizeLengths() {
        m_dataPanel.equalizeLengths();
    }

    /**
     * Set the <CODE>PrimaryPanel</CODE> object that is used by this  window to
     * display sequences.
     *
     * @param pane The new panel object
     */
    protected void setPrimaryPanel(PrimaryPanel pane) {
        if ((m_dataPanel = pane) != null) {
            m_dataPanel.addGenomeSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    refreshGeneComboBox();
                }
            });
        }
    }

    /**
     * get the current primary panel object used by this window
     * changed to public
     *
     * @return the panel
     */
    public PrimaryPanel getPrimaryPanel() {
        return m_dataPanel;
    }

    public JPanel getMainPanel() {
        return m_main;
    }

    /**
     * Get the 'strand' button
     *
     * @return The strand button
     */
    protected JButton getStrandButton() {
        return m_strandButton;
    }

    /**
     * get the filename of the current working file
     *
     * @return the filename
     */
    protected String getWorkFilename() {
        return m_workFilename;
    }

    /**
     * checks the 'savable' flag on the current working  document.  A document
     * is savable if it has a proper filename(and type) associated with it.
     *
     * @return true if the current document is 'savable'
     */
    protected boolean isSavable() {
        return m_workSavable;
    }

    /**
     * set the filename for the current working document
     *
     * @param fname the new filename
     */
    protected void setWorkFilename(String fname) {
        m_workFilename = fname;
    }

    /**
     * refresh the window title
     */
    protected void refreshTitle() {
        StringBuffer title = new StringBuffer(AppConstants.APP_TITLE + ": " + m_workFilename);

        if (UndoHandler.getInstance().isModified()) {
            title.append(MODIFIED_TEXT);
        }

        setTitle(title.toString());
    }

    /**
     * sets the 'savable' flag for the current document
     *
     * @param savable the new value
     */
    protected void setSavable(boolean savable) {
        m_workSavable = savable;
    }

    /**
     * Sets the 'enabled' property of all components which are restricted by
     * the condition that there must be a sequence open in order for them to
     * be valid
     *
     * @param enabled the new boolean value, if true, all components will be
     *                enabled, and if false, all will be disabled
     */
    protected void setRestrictedComponentsEnabled(boolean enabled) {
        for (Iterator<JComponent> i = m_restrictedComponents.iterator(); i.hasNext(); ) {
            JComponent comp = i.next();

            comp.setEnabled(enabled);
        }
    }

    /**
     * initialize window listeners and global actions
     */
    protected void initListeners() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                close();
            }
        });

        UndoHandler.getInstance().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                refreshTitle();
            }
        });
    }

    /**
     * init the ui layout, etc
     */
    protected void initUI() {
        m_splash.setBackground(new Color(96, 118, 183));
        m_splash.setOpaque(true);

        //setMaximumSize(Utils.getScreenSize());

        ClassLoader cl = this.getClass().getClassLoader();

        java.net.URL url1 = null;
        //java.net.URL url2 = null;

        try {
            url1 = cl.getResource("images/bbb-words.gif");
            //url2 = cl.getResource("images/loadinst.gif");
        } catch (Exception ex) {
            ex.printStackTrace();

            return;
        }

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));

        // Base-By-Base graphic
        JLabel p = new JLabel(new ImageIcon(url1));
        top.add(p);
        top.setBackground(new Color(96, 118, 183));
        top.setOpaque(true);

        // Instructions
        JPanel bot = new JPanel();
        bot.setLayout(new BoxLayout(bot, BoxLayout.X_AXIS));
        bot.add(Box.createHorizontalGlue());
        //p = new JLabel(new ImageIcon(url2));
        p = new JLabel("<html>Welcome to Base-By-Base.<br>" +
                "To get started, add some sequences to be aligned by going to the 'File' menu and choosing<br>" +
                "'Add Sequences to Alignment'. You can choose to add sequences from a file (in FASTA, ClustalW,<br>" +
                "or Base-By-Base format) or from a VOCs database. Once the sequences are loaded you can click<br>" +
                "and drag in the sequence display to insert gaps and align your sequence. To save your alignment<br>" +
                "go to the 'File' menu and click 'Save'. If you want to revise this alignment later, open Base-By-Base<br>" +
                "and select 'Open Alignment' from the 'File' menu. " +
                "<br><br>" +
                "j-CODEHOP Primer Design:<br>" +
                "To get started, import related protein sequences from a protein family using the method described<br>" +
                "above. Click on the Advanced menu and select j-CODEHOP. Within the j-CODEHOP page, select the<br>" +
                "alignment method of choice. No need to align before opening j-CODEHOP.<br><br>" +
                "Thank you for using Base-By-Base.</html>", JLabel.CENTER);
        p.setForeground(Color.white);
        Font font = new Font("Verdana", Font.BOLD, 12);
        p.setFont(font);
        bot.add(p);
        bot.setBackground(new Color(96, 118, 183));
        //bot.setBackground(Color.red);

        bot.setOpaque(true);

        m_splash.add(top, BorderLayout.NORTH);
        m_splash.add(bot, BorderLayout.CENTER);

        setJMenuBar(createMenuBar());
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        m_main.setLayout(new BorderLayout());
        m_main.add(createGeneralToolBar(), BorderLayout.NORTH);
        m_main.add(m_status, BorderLayout.SOUTH);
        m_main.add(m_splash, BorderLayout.CENTER);

        setContentPane(m_main);

        setRestrictedComponentsEnabled(false);

        //Set Icon
        String path = "";
        if (System.getProperty("os.name").startsWith("Windows")) {
            path = "\\logo\\bbb.png";
        } else {
            path = "/logo/bbb.png";
        }

        URL url = this.getClass().getResource(path);
        try {
            theFrame.setIconImage(ImageIO.read(url));

        } catch (IOException e) {
            //couldnt get icon .png, will use default javacup
            //e.printStackTrace();
            System.out.println("IO error could not open icon .png");
        } catch (Exception e) {
            System.out.println("Could not get icon... will use jcup instead");
        }


    }

    /**
     * create the menubar used in this window
     *
     * @return the menubar
     */
    JMenuBar mb = new JMenuBar();

    protected JMenuBar createMenuBar() {
        mb.add(createFileMenu());
        mb.add(createEditMenu());
        mb.add(createViewMenu());
        mb.add(createNavigateMenu());
        mb.add(createReportsMenu());
        mb.add(createToolsMenu());
        mb.add(createAdvancedMenu());
        //mb.add(new BookmarkMenu("Links", DiffEditorFrame.getBookmarkList()));
        mb.add(createLinksMenu());
        mb.add(Box.createHorizontalGlue());
        mb.add(createHelpMenu());

        return mb;
    }
    protected JMenu createLinksMenu(){

        final JMenu menu = new JMenu("Links");
        JMenuItem mi = new JMenuItem("Viral Bioinformatics Resource Center");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {

                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://4virology.net/"));
                    }
                    catch (URISyntaxException | IOException e){}
                }

            }
        });
        menu.add(mi);
        
        return menu;

    }

    protected JMenu createFileMenu() {

        JMenu menu = null;
        JMenu subMenu = null;
        JMenuItem mi = null;
        JMenu fileMenu = null;

        menu = new JMenu("File");
        menu.setMnemonic(MenuKeyEvent.VK_F);

        mi = new JMenuItem("New Alignment", MenuKeyEvent.VK_N);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        mi.addActionListener(new NewAlignmentAction());
        menu.add(mi);

        // Quick Add
        /*mi = new JMenuItem("Quick Add Alignments/Sequences", MenuKeyEvent.VK_A);
        mi.setAccelerator(KeyStroke.getKeyStroke("control A"));
		mi.addActionListener(new QuickAddFilesAction());
		menu.add(mi);*/

        // Append Sequences Submenu
        subMenu = new JMenu("Add Alignment/Sequence");
        subMenu.setMnemonic(MenuKeyEvent.VK_A);
        menu.add(subMenu);

        mi = new JMenuItem("From Your Local File...", MenuKeyEvent.VK_F);
        //mi.setAccelerator(KeyStroke.getKeyStroke("control F"));
        mi.addActionListener(new LoadFileAction("Append Sequences"));
        subMenu.add(mi);
        /*
        fileMenu = new JMenu("From Your Local File...");
		// 20040527 KCS - added Icon to this menu item for consistency with Load menu
		fileMenu.setIcon( Icons.getInstance().getIcon("LOAD") );
		subMenu.add(fileMenu);

		mi = new JMenuItem("From Fasta file", MenuKeyEvent.VK_F);
		mi.setAccelerator(KeyStroke.getKeyStroke("control F"));
		mi.addActionListener(new LoadFileAction("Append Sequences", "FASTA"));
		fileMenu.add(mi);

		mi = new JMenuItem("From Base-by-base file", MenuKeyEvent.VK_B);
		mi.setAccelerator(KeyStroke.getKeyStroke("control B"));
		mi.addActionListener(new LoadFileAction("Append Sequences", "BBB"));
		fileMenu.add(mi);

		mi = new JMenuItem("From Clustal Formatted file", MenuKeyEvent.VK_L);
		mi.setAccelerator(KeyStroke.getKeyStroke("control L"));
		mi.addActionListener(new LoadFileAction("Append Sequences", "CLUSTAL"));
		fileMenu.add(mi);

		mi = new JMenuItem("From GenBank file", MenuKeyEvent.VK_G);
		mi.setAccelerator(KeyStroke.getKeyStroke("control G"));
		mi.addActionListener(new LoadFileAction("Append Sequences", "GENBANK"));
		fileMenu.add(mi);

		mi = new JMenuItem("From EMBL file", MenuKeyEvent.VK_E);
		mi.setAccelerator(KeyStroke.getKeyStroke("control E"));
		mi.addActionListener(new LoadFileAction("Append Sequences", "EMBL"));
		fileMenu.add(mi);
        //*/
        mi = new JMenuItem("From VOCs Database...", MenuKeyEvent.VK_V);
        mi.addActionListener(new AppendDBAction(this));
        subMenu.add(mi);


        // Open Alignment Submenu
        subMenu = new JMenu("Open Alignment");
        subMenu.setMnemonic(MenuKeyEvent.VK_O);
        menu.add(subMenu);

        mi = new JMenuItem("From Your Local File...", MenuKeyEvent.VK_F);
        //mi.setAccelerator(KeyStroke.getKeyStroke("control F"));
        mi.addActionListener(new LoadFileAction("Load Sequences"));
        subMenu.add(mi);

        /*
        fileMenu = new JMenu("From Your Local File...");
		fileMenu.setIcon( Icons.getInstance().getIcon("LOAD") );
		subMenu.add(fileMenu);

		mi = new JMenuItem("From Fasta file", MenuKeyEvent.VK_F);
		mi.setAccelerator(KeyStroke.getKeyStroke("control F"));
		mi.addActionListener(new LoadFileAction("Load Sequences", "FASTA"));
		fileMenu.add(mi);

		mi = new JMenuItem("From Base-by-base file", MenuKeyEvent.VK_B);
		mi.setAccelerator(KeyStroke.getKeyStroke("control B"));
		mi.addActionListener(new LoadFileAction("Load Sequences", "BBB"));
		fileMenu.add(mi);

		mi = new JMenuItem("From Clustal Formatted file", MenuKeyEvent.VK_L);
		mi.setAccelerator(KeyStroke.getKeyStroke("control L"));
		mi.addActionListener(new LoadFileAction("Load Sequences", "CLUSTAL"));
		fileMenu.add(mi);

		mi = new JMenuItem("From GenBank file", MenuKeyEvent.VK_G);
		mi.setAccelerator(KeyStroke.getKeyStroke("control G"));
		mi.addActionListener(new LoadFileAction("Load Sequences", "GENBANK"));
		fileMenu.add(mi);

		mi = new JMenuItem("From EMBL file", MenuKeyEvent.VK_E);
		mi.setAccelerator(KeyStroke.getKeyStroke("control E"));
		mi.addActionListener(new LoadFileAction("Load Sequences", "EMBL"));
		fileMenu.add(mi);
        //*/

        // Remove Sequences entry
        mi = new JMenuItem("Remove Sequences", MenuKeyEvent.VK_R);
        mi.addActionListener(new RemoveSequencesAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Save", MenuKeyEvent.VK_S);
        mi.addActionListener(new SaveAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Save As...", MenuKeyEvent.VK_V);
        mi.addActionListener(new SaveAsAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Save Primers...", MenuKeyEvent.VK_P);
        mi.addActionListener(new SavePrimerAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Import Analysis from File...", MenuKeyEvent.VK_I);
        mi.addActionListener(new ImportAnalysisFileAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Clear Analysis Result...", MenuKeyEvent.VK_C);
        m_restrictedComponents.add(mi);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                getPrimaryPanel().deleteSelectedFeature(FeatureType.COMMENT);
            }
        });
        menu.add(mi);

        mi = new JMenuItem("Export Selection (Marked Sequences)...", MenuKeyEvent.VK_E);
        mi.addActionListener(new ExportSelectionAction(false));
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Export Selection (All Sequences)...", MenuKeyEvent.VK_X);
        mi.addActionListener(new ExportSelectionAction(true));
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Export Alignment Image", MenuKeyEvent.VK_I);
        mi.addActionListener(new ExportImageAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Export Alignment Overview");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Thread t = new Thread() {
                    public void run() {
                        ExportAlignmentOverview();
                    }
                };
                t.run();
            }
        });
        m_restrictedComponents.add(mi);
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Save Selected Regions as .bbb");
        mi.addActionListener(new ExportSelectedRegionsBBBAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Save Selected Regions as .fasta");
        mi.addActionListener(new ExportSelectedRegionsFASTAAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Open Selected Regions to new BBB");
        mi.addActionListener(new OpenSelectedRegionsBBBAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        menu.addSeparator();

        //		Append Preferences Submenu
        subMenu = new JMenu("Preferences");
        subMenu.setMnemonic(MenuKeyEvent.VK_A);
        menu.add(subMenu);

        Vector<String> v = new Vector<String>();
        v.add(ExtPrefsChooser.CLUSTALW);
        //v.add(ExtPrefsChooser.TCOFFEE);
        v.add(ExtPrefsChooser.MUSCLE);
        //		v.add(ExtPrefsChooser.MAFFT);
        v.add(ExtPrefsChooser.BLAST);
        mi = new ExtPrefsMenuItem("Program Preferences", v, ExtPrefsChooser.CLUSTALW);
        mi.setMnemonic(MenuKeyEvent.VK_P);
        subMenu.add(mi);

        mi = new JMenuItem("Display/Usability Preferences", MenuKeyEvent.VK_D);
        mi.addActionListener(new PreferencesAction());
        subMenu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Close Alignment", MenuKeyEvent.VK_C);
        m_restrictedComponents.add(mi);
        mi.addActionListener(new CloseAlignmentAction());
        menu.add(mi);

        JMenuItem reportbug = new JMenuItem("Report bug..", MenuKeyEvent.VK_B);
        reportbug.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JOptionPane.showMessageDialog(null, "Please report all bugs to cupton@uvic.ca", "Report bug", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(reportbug);

        mi = new JMenuItem("Quit", MenuKeyEvent.VK_Q);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        mi.addActionListener(new QuitAction());
        menu.add(mi);

        return (menu);
    }

    protected JMenu createEditMenu() {
        JMenu menu = null;
        JMenu subMenu = null;
        JMenuItem mi = null;

        menu = new JMenu("Edit");
        m_restrictedComponents.add(menu);
        menu.setMnemonic(MenuKeyEvent.VK_E);

        mi = new JMenuItem("Undo", MenuKeyEvent.VK_U);
        mi.addActionListener(new UndoAction());
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Redo", MenuKeyEvent.VK_R);
        mi.addActionListener(new RedoAction());
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        m_restrictedComponents.add(mi);
        menu.add(mi);

        boolean fastaFormat; //only applies to menu items "Amino Acid" and "Fasta AA"
        subMenu = new JMenu("Copy As");
        ButtonGroup bg = new ButtonGroup();

        mi = new JMenuItem("Nucleotide");
        fastaFormat = false;
        mi.addActionListener(new CopyNucleotideSeqAction(fastaFormat));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        m_restrictedComponents.add(mi);
        subMenu.add(mi);

        mi = new JMenuItem("Amino Acid");
        fastaFormat = false;
        mi.addActionListener(new CopyAminoAcidSeqAction(fastaFormat));
        m_restrictedComponents.add(mi);
        subMenu.add(mi);

        mi = new JMenuItem("Fasta NT");
        fastaFormat = true;
        mi.addActionListener(new CopyNucleotideSeqAction(fastaFormat));
        m_restrictedComponents.add(mi);
        subMenu.add(mi);

        mi = new JMenuItem("Fasta AA");
        fastaFormat = true;
        mi.addActionListener(new CopyAminoAcidSeqAction(fastaFormat));
        m_restrictedComponents.add(mi);
        subMenu.add(mi);

        menu.add(subMenu);
        menu.addSeparator();

        mi = new JMenuItem("Edit Sequence Names", MenuKeyEvent.VK_S);
        mi.addActionListener(new EditSequenceNamesAction());
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Mark All Sequences", MenuKeyEvent.VK_M);
        mi.addActionListener(new MarkAllAction());
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Unmark Sequences", MenuKeyEvent.VK_U);
        mi.addActionListener(new UnmarkSequencesAction());
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Select Whole Sequence", MenuKeyEvent.VK_W);
        mi.addActionListener(new SelectWholeSequenceAction());
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Select Region", MenuKeyEvent.VK_E);
        mi.addActionListener(new SelectRegionAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Remove Selected Sequence Region(s)");
        mi.addActionListener(new RemoveSelectedRegionAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Remove ALL but Selected Sequence Region(s)");
        mi.addActionListener(new RemoveAllButSelectedRegionAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Remove Gaps in Selected Region(s)", MenuKeyEvent.VK_G);
        mi.addActionListener(new RemoveGapsFromSelectedRegionAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Remove All-Gap Columns", MenuKeyEvent.VK_C);
        mi.addActionListener(new RemoveAllGapColumnsAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Remove All Comments from Sequence");
        mi.addActionListener(new RemoveAllCommentsAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Insert Gaps", MenuKeyEvent.VK_I);
        mi.addActionListener(new InsertGapsAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Insert Nucleotide(s)", MenuKeyEvent.VK_I);
        mi.addActionListener(new InsertSeqAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Edit MSA Notes");
        mi.addActionListener(new NoteBookFrameAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        return (menu);
    }

    protected JMenu createViewMenu() {
        JMenu menu = null;
        JMenu subMenu = null;
        JMenuItem mi = null;

        menu = new JMenu("View");
        m_restrictedComponents.add(menu);
        menu.setMnemonic(MenuKeyEvent.VK_V);

        subMenu = new JMenu("Comparison Method");

        comp1 = new JRadioButtonMenuItem("Pairwise Comparison");
        comp2 = new JRadioButtonMenuItem("Against Consensus");
        comp3 = new JRadioButtonMenuItem("Against Top Sequence");
        ButtonGroup bg = new ButtonGroup();
        bg.add(comp3);
        bg.add(comp1);
        bg.add(comp2);

        comp3.setSelected(true);

        compList = new ComparisonAction(comp1, comp2, comp3);
        comp1.addActionListener(compList);
        comp2.addActionListener(compList);
        comp3.addActionListener(compList);
        subMenu.add(comp3);
        subMenu.add(comp1);
        subMenu.add(comp2);

        menu.add(subMenu);
        menu.addSeparator();

        mi = new JMenuItem("Set Display Area...", MenuKeyEvent.VK_S);
        mi.addActionListener(new DisplayAreaAction());
        menu.add(mi);

        mi = new JMenuItem("Show/Hide Sequences...", MenuKeyEvent.VK_D);
        mi.addActionListener(new ShowSequencesAction());
        mi.setIcon(Icons.getInstance().getIcon("SHOW"));
        menu.add(mi);

        menu.addSeparator();

        subMenu = new JMenu("Color Scheme");
        bg = new ButtonGroup();

        mi = new JRadioButtonMenuItem("Nucleotide / AA");
        mi.setSelected(true);
        mi.addActionListener(new SetColorSchemeAction(ColorScheme.BBB_SCHEME));
        bg.add(mi);
        subMenu.add(mi);

        mi = new JRadioButtonMenuItem("Hide Residues Identical to Top Sequence");
        mi.setSelected(true);
        mi.addActionListener(new SetColorSchemeAction(ColorScheme.HID_SCHEME));
        bg.add(mi);
        subMenu.add(mi);

        mi = new JRadioButtonMenuItem("Similarity");
        mi.addActionListener(new SetColorSchemeAction(ColorScheme.SIM_SCHEME));
        bg.add(mi);

        subMenu.add(mi);

        mi = new JRadioButtonMenuItem("Hydrophobicity");
        mi.addActionListener(new SetColorSchemeAction(ColorScheme.HYDRO_SCHEME));
        bg.add(mi);
        subMenu.add(mi);

        mi = new JRadioButtonMenuItem("BLOSUM62 Score");
        mi.addActionListener(new SetColorSchemeAction(ColorScheme.BLOSUM62_SCHEME));
        bg.add(mi);
        subMenu.add(mi);

        mi = new JRadioButtonMenuItem("PAM250 Score");
        mi.addActionListener(new SetColorSchemeAction(ColorScheme.PAM250_SCHEME));
        bg.add(mi);
        subMenu.add(mi);

        mi = new JRadioButtonMenuItem("Percent ID");
        mi.addActionListener(new SetColorSchemeAction(ColorScheme.PCT_ID_SCHEME));
        bg.add(mi);
        subMenu.add(mi);

        mi = new JRadioButtonMenuItem("Custom");
        mi.addActionListener(new SetColorSchemeAction(ColorScheme.CUSTOM_SCHEME));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {

                String firstseq = getPrimaryPanel().m_epanels.get(0).m_seq.toString();
                boolean aaSeq = false;
                for (int i = 0; i < firstseq.length(); i++) {
                    String spec = Character.toString(firstseq.charAt(i));
                    if (!spec.equals("A") && !spec.equals("C") && !spec.equals("T") && !spec.equals("G") && !spec.equals("-")) {
                        aaSeq = true;
                        break;
                    }
                }
                if (!aaSeq) {
                    UITools.showInfoMessage("This feature only works with amino acid sequences.", getFrame());
                    return;
                }

                CustomColorPopUp od = new CustomColorPopUp();
                int val = JOptionPane.showConfirmDialog(getFrame(), od, "Input", JOptionPane.OK_CANCEL_OPTION);
                if (val == 2) {
                    return;
                }
                letters = od.getLetters();
            }
        });
        bg.add(mi);
        subMenu.add(mi);

        menu.add(subMenu);

        subMenu = new JMenu("Difference Colors");
        bg = new ButtonGroup();

        mi = new JRadioButtonMenuItem("Insertions/Substitutions/Deletions");
        mi.setSelected(true);
        mi.addActionListener(new SetDifferenceColorsAction(ColorScheme.DIFF_CLASSIC_SCHEME));
        bg.add(mi);
        subMenu.add(mi);

        mi = new JRadioButtonMenuItem("Nucleotide Differences");
        mi.addActionListener(new SetDifferenceColorsAction(ColorScheme.DIFF_NT_SCHEME));
        bg.add(mi);
        subMenu.add(mi);

        menu.add(subMenu);
        menu.addSeparator();

        mi = new JMenuItem("Remove Identical Sequence(s)");
        mi.addActionListener(new RemoveIdenticalSequencesAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        menu.addSeparator();


        subMenu = new JMenu("Display Consensus");

        mi = new JMenuItem("% Identity");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {

                if (getPrimaryPanel().m_rnaScroll.isVisible()) {
                    getPrimaryPanel().rna_visible(false);
                }

                getPrimaryPanel().cons_visible(true);


                setConsensus(ConsensusFactory.IDENTITY);

                if (m_consensusRefreshThread != null)
                    return;

                BBBPrefs prefs = BBBPrefs.getInstance();
                int value = 0;
                try {
                    value = Integer.parseInt(prefs.get_bbbPref("gui.consensus.refreshRate"));
                } catch (NumberFormatException e) {
                }
                if (value > 0) {
                    final int refreshRate = value;

                    m_consensusRefreshThread = new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(refreshRate * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            while (DiffEditorFrame.this.isConsensusRefreshRunning()) {
                                DiffEditorFrame.this.m_dataPanel.refreshConsensus();
                                try {
                                    Thread.sleep(refreshRate * 1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            DiffEditorFrame.this.m_consensusRefreshThread = null;
                        }
                    };

                    resetConsensusRefreshRunning(true);
                    m_consensusRefreshThread.start();
                }
            }
        });
        subMenu.add(mi);
        subMenu.addSeparator();
        mi = new JMenuItem("Hide Consensus");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                resetConsensusRefreshRunning(false);
                getPrimaryPanel().cons_visible(false);
            }
        });
        subMenu.add(mi);

        menu.add(subMenu);
        mi = new JMenuItem("Refresh Consensus");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                getPrimaryPanel().refreshEditors();
                getPrimaryPanel().refreshConsensus();
            }
        });
        menu.add(mi);

        MI_RNAOverview = new JMenuItem("Display Sequence Expression Summary");
        MI_RNAOverview.setEnabled(false);
        //MI_RNAOverview.setVisible(false);


		/* This batch handles the mRNA expression data plotting tools.
		 * Please refer to the following associated classes:
		 * ca.virology.baseByBase.data.mRNAs
		 * ca.virology.baseByBase.data.mRNA
		 * ca.virology.baseByBase.gui.RNAOverviewFrame
		 * ca.virology.baseByBase.gui.mRNADisplay */

        this.add(getPrimaryPanel().m_rnaScroll);
        subMenu = new JMenu("Display mRNA Expression Data");
        menu.add(subMenu);
        mi = new JMenuItem("Load and Display mRNA expression data");
        subMenu.add(mi);
        mi.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                final File file = getSequenceFileFromCommonDialog("mRNA");
                try {
                    if (!file.exists()) {
                        UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
                        return;
                    }
                } catch (NullPointerException e) {
                    UITools.showWarning("Please Select a valid file.", getContentPane());
                    return;
                }

                mRNAPlotter = new Thread() {


                    public void run() {


                        if (getPrimaryPanel().m_consScroll.isVisible()) {
                            getPrimaryPanel().cons_visible(false);
                        }

                        getPrimaryPanel().rna_visible(true);

                        if (getPrimaryPanel().m_rnaDisp == null) {

                            UITools.showWarning("There is no MRNA data loaded.", getContentPane());
                        }

                        mrnas = new mRNAs(file);

                        getPrimaryPanel().m_rnaDisp.setMRNA(mrnas);
                        getPrimaryPanel().m_rnaDisp.setPrimaryPanel(getPrimaryPanel());
                        getPrimaryPanel().m_rnaDisp.setVisible(true);
                        getPrimaryPanel().m_rnaHeadScroll.setVisible(true);
                        getPrimaryPanel().m_rnaScroll.setVisible(true);
                        Graphics g = getPrimaryPanel().m_rnaHead.getGraphics();
                        //g.drawString("Expression", 30, 30);
                        g.drawString("Counts", 5, 10);


                        Graphics sg = getPrimaryPanel().m_rnaDisp.getGraphics();


                        // getPrimaryPanel().m_rnaDisp.setMRNAS(mrnas);
                        if (sg != null) {
                            getPrimaryPanel().m_rnaDisp.paintComponent(sg);
                        } else {
                            System.out.println("sg is null");
                            return;
                        }
                        getPrimaryPanel().rna_visible(true);
                        getPrimaryPanel().m_rnaDisp.repaint();

                        MI_RNAOverview.setEnabled(true);
                        //MI_RNAOverview.setVisible(true);

                    }
                };


                mRNAPlotter.start();

            }

        });


        MI_RNAOverview.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {

                if (getPrimaryPanel() == null || mrnas == null) {
                    UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
                    return;
                }
                getPrimaryPanel().viewRnaPreviewWindow(mrnas);

            }

        });
        subMenu.add(MI_RNAOverview);

        return (menu);
    }


    //Creates the menubar (not to be confused with toolbar!)
    protected JMenu createNavigateMenu() {
        JMenu menu = null;
        JMenuItem mi = null;

        menu = new JMenu("Navigate");
        m_restrictedComponents.add(menu);
        menu.setMnemonic(MenuKeyEvent.VK_N);

        mi = menu.add(new GoToAction());
        mi.setMnemonic(MenuKeyEvent.VK_G);
        menu.addSeparator();
        mi = menu.add(new SequenceSkipAction(10000, false));
        mi = menu.add(new SequenceSkipAction(1000, false));
        mi = menu.add(new SequenceSkipAction(1000, true));
        mi = menu.add(new SequenceSkipAction(10000, true));
        menu.addSeparator();
        mi = new JMenuItem("Go to Previous Comment");
        mi.addActionListener(new FeatureNavListenerAction(FeatureType.COMMENT, false, true, "nope"));
        mi.setIcon(Icons.getInstance().getIcon("LASTCOMMENT"));
        menu.add(mi);
        mi = new JMenuItem("Go to Next Comment");
        mi.addActionListener(new FeatureNavListenerAction(FeatureType.COMMENT, true, true, "nope"));
        mi.setIcon(Icons.getInstance().getIcon("NEXTCOMMENT"));
        menu.add(mi);
        menu.addSeparator();

        mi = new JMenuItem("Go to Previous Primer");
        mi.addActionListener(new FeatureNavListenerAction(FeatureType.PRIMER, false, true, "nope"));
        mi.setIcon(Icons.getInstance().getIcon("LASTPRIMER"));
        menu.add(mi);
        mi = new JMenuItem("Go to Next Primer");
        mi.addActionListener(new FeatureNavListenerAction(FeatureType.PRIMER, true, true, "nope"));
        mi.setIcon(Icons.getInstance().getIcon("NEXTPRIMER"));
        menu.add(mi);
        menu.addSeparator();

        mi = new JMenuItem("Go to Previous Gene");
        mi.addActionListener(new FeatureNavListenerAction(FeatureType.GENE, false, false, "nope"));
        mi.setIcon(Icons.getInstance().getIcon("LASTGENE"));
        menu.add(mi);
        mi = new JMenuItem("Go to Next Gene");
        mi.addActionListener(new FeatureNavListenerAction(FeatureType.GENE, true, false, "nope"));
        mi.setIcon(Icons.getInstance().getIcon("NEXTGENE"));
        menu.add(mi);

        mi = new JMenuItem("Go to Beginning/End of Gene");
        mi.addActionListener(new FeatureNavListenerAction(FeatureType.GENE, false, false, "front"));
        mi.setIcon(Icons.getInstance().getIcon("GENESTART"));
        menu.add(mi);
        mi = new JMenuItem("Go to End/Beginning of Gene");
        mi.addActionListener(new FeatureNavListenerAction(FeatureType.GENE, true, false, "back"));
        mi.setIcon(Icons.getInstance().getIcon("GENEEND"));
        menu.add(mi);

        menu.addSeparator();
        mi = new JMenuItem("Go to Previous Difference");
        mi.setIcon(Icons.getInstance().getIcon("LASTDIFF"));
        mi.addActionListener(new ResolveDifferenceAction(false));
        menu.add(mi);
        mi = new JMenuItem("Go to Next Difference");
        mi.setIcon(Icons.getInstance().getIcon("NEXTDIFF"));
        mi.addActionListener(new ResolveDifferenceAction(true));
        menu.add(mi);


        //neil
        menu.addSeparator();
        mi = new JMenuItem("Go to Previous Gap");
        mi.setIcon(Icons.getInstance().getIcon("LASTGAP"));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }
                getPrimaryPanel().getGapPosition(false);
            }
        });
        menu.add(mi);
        mi = new JMenuItem("Go to Next Gap");
        mi.setIcon(Icons.getInstance().getIcon("NEXTGAP"));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }
                getPrimaryPanel().getGapPosition(true);
            }
        });
        menu.add(mi);

        return (menu);
    }

    protected JMenu createReportsMenu() {
        JMenu menu = null;
        JMenu subMenu = null;
        JMenuItem mi = null;

        menu = new JMenu("Reports");
        m_restrictedComponents.add(menu);
        menu.setMnemonic(MenuKeyEvent.VK_R);

        mi = menu.add(new TreeViewAction("Neighbor Joining Tree", TreeFrame.NJ_TREE));
        //subMenu = new JMenu("Clustering Tree");
        //mi = subMenu.add(new TreeViewAction("Complete Linkage", TreeFrame.CLUSTER_COMPLETE));
        //mi = subMenu.add(new TreeViewAction("Single Linkage", TreeFrame.CLUSTER_SINGLE));
        //mi = subMenu.add(new TreeViewAction("UPGMA Tree", TreeFrame.CLUSTER_UPGMA));
        //mi = subMenu.add(new TreeViewAction("WPGMA Tree", TreeFrame.CLUSTER_WPGMA));
        //menu.add(subMenu);
        menu.addSeparator();

        //CDS Statistics
        mi = menu.add(new GeneStatsAction());
        mi.setMnemonic(MenuKeyEvent.VK_C);

        //Multi Genome Comparison Feature
        mi = menu.add(new MGCStatsAction());
        mi.setMnemonic(MenuKeyEvent.VK_M);

        //Primer Report Table
        mi = menu.add(new PrimerReportAction());

        mi = menu.add(new EventBreakdownAction());
        mi.setMnemonic(MenuKeyEvent.VK_E);
        menu.add(new GapCountAction());
        mi = menu.add(new PairwiseAction());
        mi = menu.add(new ConsReportAction());
        //keep this out of the menu until it is finished
        mi = menu.add(new SequenceSimilarityAction());
        mi = menu.add(new SequenceDifferencesAction());
        mi = menu.add(new BaseContentAction());
        mi = new JMenuItem("Visual Summary");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                getPrimaryPanel().viewPreviewWindow();

                //Detects when the 'Visual Summary' screen appears
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!OverviewFrame.ready) {
                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }

                        //Properly synchronizes BBB's gui and 'Visual Summary' graph
                        getPrimaryPanel().scrollToLocation(getPrimaryPanel().getPosition());
                        OverviewFrame.ready = false;
                    }
                });
                t.setPriority(Thread.MIN_PRIORITY);
                t.start();
            }
        });
        menu.add(mi);
        menu.addSeparator();

        menu.add(new InfoAction());
        //menu.add(new PrimerNearAction());

        menu.addSeparator();

        // Count
        mi = new JMenuItem("Get Counts");
        mi.addActionListener(new GetCountsAction());
        menu.add(mi);

        // SNP Count
        mi = new JMenuItem("Get SNP Counts of Top 2 Sequences");
        mi.addActionListener(new GetSNPCountsAction());
        menu.add(mi);

        // Unique Positions
        mi = new JMenuItem("Get Unique Positions");
        mi.addActionListener(new GetUniquePositionsAction());
        menu.add(mi);

        return (menu);
    }

    public JFrame getFrame() {
        return this;
    }

    protected JMenu createToolsMenu() {
        JMenu menu = null;
        JMenu subMenu = null;
        JMenuItem mi = null;

        menu = new JMenu("Tools");
        m_restrictedComponents.add(menu);
        menu.setMnemonic(MenuKeyEvent.VK_T);

        subMenu = new JMenu("Search");
        m_restrictedComponents.add(subMenu);
        subMenu.setMnemonic(MenuKeyEvent.VK_S);

        mi = new JMenuItem("AA in MSA columns");
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        m_restrictedComponents.add(mi);
        subMenu.add(mi);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {

                String firstseq = getPrimaryPanel().m_epanels.get(0).m_seq.toString();
                boolean aaSeq = false;
                for (int i = 0; i < firstseq.length(); i++) {
                    String spec = Character.toString(firstseq.charAt(i));
                    if (!spec.equals("A") && !spec.equals("C") && !spec.equals("T") && !spec.equals("G") && !spec.equals("-")) {
                        aaSeq = true;
                        break;
                    }
                }
                if (!aaSeq) {
                    UITools.showInfoMessage("This feature only works with amino acid sequences.", getFrame());
                    return;
                }

                OptionDialog od = new OptionDialog();
                int val = JOptionPane.showConfirmDialog(getFrame(), od, "Input", JOptionPane.OK_CANCEL_OPTION);
                if (val == 2) {
                    return;
                }
                //todo
                String result = od.getAA();
                final char thechar = result.charAt(0);
                final int identity = od.getIdentity();

                final Color commentColor = od.getCommentColor();


                Runnable runner = new Runnable() {
                    public void run() {

                        int seqnum = getPrimaryPanel().m_epanels.size();
                        String[] seqs = new String[seqnum];
                        int longest = 0;
                        for (int i = 0; i < seqnum; i++) {
                            seqs[i] = getPrimaryPanel().m_epanels.get(i).m_seq.toString();
                            if (seqs[i].length() > longest) {
                                longest = seqs[i].length();
                            }
                        }

                        //begin search
                        int matches = 0;
                        double pct;
                        for (int i = 0; i < longest; i++) { //for each char in string
                            int adjustedSeqnum = 0;
                            for (int j = 0; j < seqnum; j++) { //for each string
                                if (seqs[j].length() > i) {
                                    adjustedSeqnum += 1;
                                    if (seqs[j].charAt(i) == thechar) {
                                        matches += 1;
                                    }
                                }
                            }

                            if (adjustedSeqnum > 1) {
                                pct = (double) matches / (double) adjustedSeqnum * 100;   // make the sequence number adjust as well
                                pct = Math.round(pct * 100.0) / 100.0;
                                if (pct >= identity) { //found good column
                                    getPrimaryPanel().addCommentNoUI(pct + "% position=" + (i + 1) + " AA=" + thechar, i, longest, thechar, commentColor);
                                }
                                matches = 0;
                            }

                        }
                    }
                };

                UITools.invokeProgressWithMessageNoButtons(getFrame(), runner, "Searching sequence..");
            }
        });


        mi = subMenu.add(new FuzzySearchAction());
        m_restrictedComponents.add(mi);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        mi = subMenu.add(new RegexSearchAction());
        m_restrictedComponents.add(mi);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        mi = subMenu.add(new PrimerSearchAction());
        m_restrictedComponents.add(mi);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        menu.add(subMenu);
        menu.addSeparator();

        mi = new JMenuItem("Export Differences to VGO", MenuKeyEvent.VK_E);
        m_restrictedComponents.add(mi);
        mi.addActionListener(new VGOExportListener());
        menu.add(mi);
        menu.addSeparator();

        subMenu = new JMenu("Comment");
        m_restrictedComponents.add(subMenu);
        subMenu.setMnemonic(MenuKeyEvent.VK_C);

        mi = new JMenuItem("Add Comment...", MenuKeyEvent.VK_C);
        m_restrictedComponents.add(mi);
        mi.addActionListener(new AddEventFeatureAction(FeatureType.COMMENT));
        mi.setIcon(Icons.getInstance().getIcon("NEWCOMMENT"));
        subMenu.add(mi);
        mi = new JMenuItem("Remove Comment", MenuKeyEvent.VK_R);
        m_restrictedComponents.add(mi);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                getPrimaryPanel().deleteSelectedFeature(FeatureType.COMMENT);
            }
        });
        subMenu.add(mi);

        mi = new JMenuItem("Edit Comment");
        m_restrictedComponents.add(mi);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                getPrimaryPanel().editFeature("COMMENT");
            }
        });
        subMenu.add(mi);

        mi = new JMenuItem("Search for Keywords", MenuKeyEvent.VK_S);
        m_restrictedComponents.add(mi);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                getPrimaryPanel().keywordSearch("COMMENT");
            }
        });
        subMenu.add(mi);

        JMenuItem commentListerItem = new JMenuItem("List Comments");
        m_restrictedComponents.add(commentListerItem);
        commentListerItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedSequenceIndices = getPrimaryPanel().getSelectedIndices();
                if (selectedSequenceIndices.length == 0) {
                    return;
                }
                FeaturedSequence sequence = getPrimaryPanel().getSequences()[selectedSequenceIndices[0]];

                StringBuilder sb = new StringBuilder("");

                //for every comment on the selected sequence...
                Iterator<StrandedFeature> it = sequence.features();
                while (it.hasNext()) {
                    StrandedFeature feature = it.next();
                    if ("COMMENT".equals(feature.getType())) {
                        String comment = (String) feature.getAnnotation().getProperty("COMMENT_TEXT");
                        int sequenceLocation = feature.getLocation().getMin();
                        int alignedLocation = sequence.getRelativePosition(sequenceLocation);

                        sb.append("Sequence Location: " + sequenceLocation + "\tAligned Location: " + (alignedLocation + 1) + "\t" + comment + "\r\n");
                    }
                }

                showTextWindow(sb.toString());
            }
        });
        subMenu.add(commentListerItem);

        menu.add(subMenu);
        menu.addSeparator();

        //primer buttons, add primer, remove primer
        subMenu = new JMenu("Primer");
        m_restrictedComponents.add(subMenu);
        subMenu.setMnemonic(MenuKeyEvent.VK_P);

        m_restrictedComponents.add(subMenu);

        mi = new JMenuItem("Add Primer...");
        m_restrictedComponents.add(mi);
        mi.addActionListener(new AddEventFeatureAction(FeatureType.PRIMER));
        mi.setIcon(Icons.getInstance().getIcon("NEWPRIMER"));
        subMenu.add(mi);
        mi = new JMenuItem("Remove Primer");
        m_restrictedComponents.add(mi);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                getPrimaryPanel().deleteSelectedFeature(FeatureType.PRIMER);
            }
        });
        subMenu.add(mi);

        mi = new JMenuItem("Edit Primer");
        m_restrictedComponents.add(mi);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                getPrimaryPanel().editFeature("PRIMER");
            }
        });
        subMenu.add(mi);

        mi = new JMenuItem("Search for Keywords");
        m_restrictedComponents.add(mi);
        mi.addActionListener(new SearchPrimerKeywordAction());
        subMenu.add(mi);


        mi = new JMenuItem("Add Primers From File");
        m_restrictedComponents.add(mi);
        mi.addActionListener(new LoadPrimerAction());
        mi.setIcon(Icons.getInstance().getIcon("LOAD"));
        subMenu.add(mi);
        menu.add(subMenu);

        menu.addSeparator();

        //Sequence tools
        subMenu = new JMenu("Sequence");

        mi = new JMenuItem("Reverse and Complement");
        mi.addActionListener(new SequenceToolsAction(SequenceToolsActionType.REVERSE_COMPLEMENT_ACTION));
        m_restrictedComponents.add(mi);
        subMenu.add(mi);

        m_restrictedComponents.add(subMenu);
        mi = new JMenuItem("Calculate A+T%");
        mi.addActionListener(new SequenceToolsAction(SequenceToolsActionType.CALCULATE_A_PLUS_T_ACTION));
        m_restrictedComponents.add(mi);
        subMenu.add(mi);


        mi = new JMenuItem("Calculate ITRs");
        mi.addActionListener(new SequenceToolsAction(SequenceToolsActionType.CALCULATE_ITR_ACTION));
        m_restrictedComponents.add(mi);
        subMenu.add(mi);

        menu.add(subMenu);

        subMenu = new JMenu("Align Selection");
        m_restrictedComponents.add(subMenu);
        //mi = new JMenuItem("With ClustalW");
        //mi.addActionListener(new AlignRegionListener("clustalw"));
        //subMenu.add(mi);
        mi = new JMenuItem("With Clustal" + "\u03A9");
        mi.addActionListener(new AlignRegionListener("clustalo"));
        subMenu.add(mi);
        //mi = new JMenuItem("With T-Coffee");
        //mi.addActionListener(new AlignRegionListener("t_coffee"));
        subMenu.add(mi);
        m_muscle = new JMenuItem("With Muscle");
        m_muscle.addActionListener(new AlignRegionListener("muscle"));
        subMenu.add(m_muscle);
        mi = new JMenuItem("With Mafft");
        mi.addActionListener(new AlignRegionListener("mafft"));
        subMenu.add(mi);

        menu.add(subMenu);


        subMenu = new JMenu("Align all sequences (all bases)");
        m_restrictedComponents.add(subMenu);
        //mi = new JMenuItem("With ClustalW");
        //mi.addActionListener(new AlignRegionListener("clustalw"));
        //subMenu.add(mi);
        mi = new JMenuItem("With Clustal" + "\u03A9");
        mi.addActionListener(new SelectWholeSequenceAction());
        mi.addActionListener(new AlignRegionListener("clustalo"));
        subMenu.add(mi);
        //mi = new JMenuItem("With T-Coffee");
        //mi.addActionListener(new AlignRegionListener("t_coffee"));
        subMenu.add(mi);

        m_muscle = new JMenuItem("With Muscle");
        m_muscle.addActionListener(new SelectWholeSequenceAction());
        m_muscle.addActionListener(new AlignRegionListener("muscle"));
        subMenu.add(m_muscle);

        mi = new JMenuItem("With Mafft");
        mi.addActionListener(new SelectWholeSequenceAction());
        mi.addActionListener(new AlignRegionListener("mafft"));
        subMenu.add(mi);

        menu.add(subMenu);
        

        menu.addSeparator();
        subMenu = new JMenu("Import Genes");

        mi = new JMenuItem("From VOCs Database...", MenuKeyEvent.VK_G);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                VocsTools.guiSetupSequences(getVisibleSequences());
                refreshGeneComboBox();
            }
        });
        subMenu.add(mi);
        mi = subMenu.add(new FeatureFileImportAction(FeatureType.GENE));
        mi.setText("From Feature File...");
        m_restrictedComponents.add(subMenu);
        menu.add(subMenu);

        mi = menu.add(new RemoveFeaturesAction(FeatureType.GENE));
        m_restrictedComponents.add(mi);

        menu.addSeparator();

        // New menu item for strip functionality
        m_strip = new JMenuItem("Delete Columns Containing Non-ACTGU Characters and Export");
        m_restrictedComponents.add(m_strip);
        m_strip.addActionListener(new StripInvalidCharactersAction());
        menu.add(m_strip);


        // File -> Delete Gap Columns and Export item moved to the Tools menu and renamed to �Delete Gap Columns.� .
        mi = new JMenuItem("Delete Columns Containing Gap(s) and Export", MenuKeyEvent.VK_D);
        mi.addActionListener(new DeleteGapsAndExportAction());
        m_restrictedComponents.add(mi);
        menu.add(mi);

        mi = new JMenuItem("Delete Specified Columns and Export");
        mi.addActionListener(new DeleteSpecifiedColumnsAction());
        menu.add(mi);


        return (menu);
    }

    private void showTextWindow(String text) {
        //create a text window
        final JDialog logPane = new JDialog();
        logPane.setSize(new Dimension(800, 600));
        logPane.setLocationRelativeTo(null);
        JPanel mainPanel = new JPanel(new BorderLayout());
        logPane.add(mainPanel);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        textPanel.setPreferredSize(new java.awt.Dimension(780, 510));
        JTextArea commentArea = new JTextArea(780, 510);
        commentArea.setLineWrap(true);
        JScrollPane csp = new JScrollPane();
        csp.setPreferredSize(new Dimension(780, 510));
        csp.setViewportView(commentArea);
        textPanel.add(csp);
        textPanel.setBorder(BorderFactory.createTitledBorder("Operation Output"));
        //add the text
        commentArea.setText(text);
        mainPanel.add(textPanel);
        //Close Button
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
        btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        JButton close = new JButton("Close");
        btns.add(Box.createHorizontalStrut(3));
        btns.add(close);
        mainPanel.add(btns, BorderLayout.SOUTH);
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                logPane.dispose();
            }
        });
        logPane.setVisible(true);
    }

    protected JMenu createAdvancedMenu() {

        final JMenu menu = new JMenu("Advanced");
     //   m_restrictedComponents.add(menu);

        // See
        JMenuItem mi = new JMenuItem("See Advanced/Experimental Tools");
        m_restrictedComponents.add(mi);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                showAdvancedItems();
            }
        });
        menu.add(mi);

        ImageIcon icon = null;
        ImageIcon newIcon = null;
        Image img = null;
        try {
            java.net.URL imgURL = null;
            ClassLoader cl = getClass().getClassLoader();
            imgURL = cl.getResource("images/diamond.png");
            img = ImageIO.read(imgURL);

            BufferedImage bi = ImageIO.read(imgURL);
            icon = new ImageIcon(bi);
            Image image = icon.getImage();
            Image newImg = image.getScaledInstance(20, 13, Image.SCALE_SMOOTH);
            newIcon = new ImageIcon(newImg);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        final Image ic = img;
        mi = new JMenuItem(newIcon);
        mi.setText("j-CODEHOP");
        m_restrictedComponents.add(mi);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {


                //check for AA only (check the first 50 chars only, or less)
                int check;
                String firstseq = getPrimaryPanel().m_epanels.get(0).m_seq.toString();
                if (firstseq.length() >= 50) {
                    check = 50;
                } else {
                    check = firstseq.length();
                }
                String regexPattern = "[^ATCG]";
                Pattern p = Pattern.compile(regexPattern);
                Matcher m = p.matcher(firstseq.substring(0, check));
                if (!m.find()) {
                    UITools.showInfoMessage("j-CODEHOP currently only works with amino acid sequences.", getFrame());
                    return;
                }


                //check length
                for (int i = 0; i < getPrimaryPanel().m_epanels.size(); i++) {
                    if (getPrimaryPanel().m_epanels.get(i).m_seq.toString().length() > 5000) {
                        UITools.showInfoMessage("j-CODEHOP limits protein sequence length (5000)!", getFrame());
                        return;
                    }
                }

                Thread t = new Thread() {
                    public void run() {
                        CodeHopWizard.getInstance(getPrimaryPanel(), ic);
                    }
                };
                t.start();
            }
        });
        menu.add(mi);

        mi = new JMenuItem(newIcon);
        mi.setText("Load j-CODEHOP demo data");
        //       menu.add(mi);

        //mi = new JMenuItem("Open demo file", MenuKeyEvent.VK_F);
        //mi.setAccelerator(KeyStroke.getKeyStroke("control F"));
        mi.addActionListener(new TestFileAction("Load Sequences"));
        menu.add(mi);



        return (menu);
    }

    private JTextArea purposeText;
    private JPanel launchPanel;
    private JButton launch;
    private JButton log;
    private JDialog logPane;

    protected void showAdvancedItems() {

        Thread t = new Thread() {
            public void run() {

                //Set up log pane
                logPane = new JDialog(DiffEditorFrame.this);
                logPane.setSize(new Dimension(750, 400));
                logPane.setLocationRelativeTo(null);
                logPane.setTitle("Advanced/Experimental Tools");

                //Set up panel
                JPanel mainPanel = new JPanel(new BorderLayout());
                logPane.add(mainPanel);

                //Initialize contents


                //Launch panel
                launchPanel = new JPanel(new BorderLayout());
                launchPanel.setPreferredSize(new Dimension(580, 265));
                launchPanel.setBorder(BorderFactory.createTitledBorder("Information"));
                mainPanel.add(launchPanel, BorderLayout.CENTER);

                JPanel launchTextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                launchPanel.add(launchTextPanel, BorderLayout.CENTER);
                launchTextPanel.setPreferredSize(new Dimension(565, 255));
                purposeText = new JTextArea(20, 46);
                purposeText.setEditable(false);
                purposeText.setLineWrap(true);
                purposeText.setWrapStyleWord(true);
                purposeText.setBackground(new Color(237, 237, 237));
                purposeText.setText("To the left are advanced and/or experimental tools and reports." +
                        " Use at your own risk." +
                        "\nRoll over each to see a description.");
                launchTextPanel.add(purposeText);

                JPanel launchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                launchPanel.add(launchButtonsPanel, BorderLayout.SOUTH);
                //Launch Button
                launch = new JButton("Launch");
                //launch.setMinimumSize(new Dimension(110, 50));
                //launch.setPreferredSize(new Dimension(110, 50));
                launch.setVisible(false);
                launchButtonsPanel.add(launch);

                //Differences Log Button
                log = new JButton("Log");
                //log.setMinimumSize(new Dimension(110, 50));
                //log.setPreferredSize(new Dimension(110, 50));
                log.addActionListener(new ShowLogDifferencesAction());
                if (!logOptionAdded) {
                    log.setVisible(false);
                }
                launchButtonsPanel.add(log);

                //Buttons panel
                JPanel functionPanel = new JPanel(new BorderLayout());
                mainPanel.add(functionPanel, BorderLayout.WEST);
                functionPanel.setBorder(BorderFactory.createTitledBorder("Function"));
                functionPanel.setPreferredSize(new Dimension(180, 300));
                //sideMenu.setMaximumSize(new Dimension(150, 300));

                JPanel functionButtonsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
                functionPanel.add(functionButtonsPanel, BorderLayout.NORTH);

                // Unique Positions Matrix
                functionButtonsPanel.add(createAdvancedButton("Pairwise Comparisons", new GetUniquePositionsMatrixAction(), "A matrix that shows the number of differences between each sequence. " + "\nThe differences exclude gaps from the sequence on the right, but not the sequence on the top."));

                // Find Column Differences
                functionButtonsPanel.add(createAdvancedButton("Find Differences", new FindDifferencesAction(DifferencesEditorPane.FIND_DIFFERENCES_TAB), "Tool for comparing groups of user selected sequences. " +
                        "\nThese sequences are selected by dragging them into the various columns/groups. " +
                        "Positions will be commented where they are: " +
                        "\n   * Found to be the same in the first group" +
                        "\n   * AND different from the second group" +
                        "\n   * (AND optionally the same as one sequence in the third group.)" +
                        "\nThese positions can also be viewed as a log and exported to VGO."));

                // Subgroup Column Matching
                functionButtonsPanel.add(createAdvancedButton("Subgroup Matching", new FindDifferencesAction(DifferencesEditorPane.SUBGROUP_MATCHING_TAB), "Tool for comparing groups of user selected sequences. " +
                        "\nThese sequences are selected by dragging them into the various columns/groups. " +
                        "Positions will be commented where:" +
                        "\n   * Differences are found in the first subgroup (any position where the nucleotides in the group are NOT all the same)" +
                        "\n   * AND those differences are also found in the second subgroup." +
                        "\nThese positions can also be viewed as a log and exported to VGO"));

                //SNIP Utility Button
                functionButtonsPanel.add(createAdvancedButton("SNIP", new SnipSelectedRegionsAction(), "BEWARE: Your sequences will be modified with the changes applied in a new window. " +
                        "\n\nJune 2017 UPDATE: This script now deals with columns containing more than two different nucleotides." +
                        "\nThis tool calculates the consensus nucleotide for each column in a multiple sequence alignment and the " +
                        "number of times that each of the remaining three non-consensus nucleotides appears. " +
                        "If these numbers are less than or equal to the threshold value given, the differing nucleotides will be changed to the consensus." +
                        "The threshold value can be set from  1-5. " +
                        "Some examples:" +
                        "1. If column contains [A A T C A A], given a threshold of 1, both the T and C will change to an A. " +
                        "2. If column contains [A A T T C A], given a threshold of 1, only the C will change to an A." +
                        "3. If column contains [A A T T C A], given a threshold of 2, both the T and 2 C's will change to an A." +
                        "\nFor more info, see https://4virology.net/virology-ca-tools/scripts/"));

                // mafft add
                functionButtonsPanel.add(createAdvancedButton("mafft --add", new mafftAddAction(), "Mafft --add computes the alignment of a set of sequences to another set which has already been aligned. " +
                        "\n  * This function substantially reduces execution time when computing alignments of large sequences." +
                        "\n  * Usage: First open a file into BBB and align sequences manually using Mafft, Clustal, or Muscle." +
                        "\n  * Then return to this dialog box and select launch. You will be prompted for a set of sequences to add to the alignment." +
                        "\n  * Mafft will then compute the alignment and automatically apply the changes to BBB's sequence viewer." +
                        "\n  * Note: The order in which sequences are aligned may affect the final result. " +

                        "\n  * Note: The already aligned sequences MUST be aligned using BBB."));

            //    functionButtonsPanel.add(createAdvancedButton("mafft --addfragments (BETA)", new mafftAddFragAction(), "Mafft fragment"));


                //Close Button
                JPanel bottomButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                bottomButtonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                JButton close = new JButton("Close");
                bottomButtonsPanel.add(close);
                mainPanel.add(bottomButtonsPanel, BorderLayout.SOUTH);
                close.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        logPane.setVisible(false);
                        logPane.dispose();
                    }
                });

                //show log pane
                logPane.setVisible(true);
            }
        };
        t.start();
    }

    public class mafftAddAction extends AbstractAction {


        public void actionPerformed(ActionEvent ev) {

            seqsFromMafftAdd.removeAllElements();

            mafftadd = true;
            LoadFileAction l = new LoadFileAction("Append Sequences");
            l.actionPerformed(ev);
            // showAdvancedItems();

            // mark all sequences
            MarkAllAction m = new MarkAllAction();
            m.actionPerformed(ev);
            // select whole sequence
            SelectWholeSequenceAction s = new SelectWholeSequenceAction();
            s.actionPerformed(ev);

            AlignRegionListener a = new AlignRegionListener("mafft");
            //System.out.println("In diff editor frame, about to go to action performed");
            a.actionPerformed(ev);
            logPane.setVisible(false);
            logPane.dispose();
        }
    }

    public class mafftAddFragAction extends AbstractAction {

        public void actionPerformed(ActionEvent ev) {

            seqsFromMafftAdd.removeAllElements();

            mafftaddfrag = true;
            LoadFileAction l = new LoadFileAction("Append Sequences");
            l.actionPerformed(ev);
            // showAdvancedItems();

            // mark all sequences
            MarkAllAction m = new MarkAllAction();
            m.actionPerformed(ev);
            // select whole sequence
            SelectWholeSequenceAction s = new SelectWholeSequenceAction();
            s.actionPerformed(ev);

            AlignRegionListener a = new AlignRegionListener("mafft");
            //System.out.println("In diff editor frame, about to go to action performed");
            a.actionPerformed(ev);
            logPane.setVisible(false);
            logPane.dispose();
        }
    }


    protected JButton createAdvancedButton(final String title, final AbstractAction clickAction, final String description) {
        final JButton item = new JButton(title);
        //item.setOpaque(true);
        item.addActionListener(clickAction);
        //item.setPreferredSize(new Dimension(140, 40));
        item.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent arg0) {
            }

            public void mouseEntered(MouseEvent arg0) {
                launchPanel.setBorder(BorderFactory.createTitledBorder(title));
                purposeText.setText(description);
                purposeText.setLineWrap(true);
                purposeText.setWrapStyleWord(true);
                item.setSelected(true);
                launch.setVisible(true);
                for (int i = 0; i < launch.getActionListeners().length; i++) {
                    launch.removeActionListener(launch.getActionListeners()[i]);
                }
                launch.addActionListener(clickAction);
            }

            public void mouseExited(MouseEvent arg0) {
                item.setSelected(false);
            }

            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseReleased(MouseEvent arg0) {
            }
        });

        return item;
    }


    protected JMenu createHelpMenu() {
        JMenu menu = null;
        JMenuItem mi = null;

        menu = new JMenu("Help");
        menu.setMnemonic(MenuKeyEvent.VK_H);
        mi = menu.add(new HelpAction());
        mi.setText("Online Manual");

        return (menu);
    }

    /**
     * Create the Nav toolbar used in this frame
     *
     * @return the toolbar
     */
    protected JToolBar createNavToolBar() {
        ///////////////////////////////
        /// left hand side buttons  ///
        ///////////////////////////////
        JToolBar ret = new JToolBar();
        ret.setFloatable(true);
        ret.setMargin(new Insets(0, 5, 0, 5));

        JButton b;

        b = ret.add(new SequenceSkipAction(10000, false));
        m_restrictedComponents.add(b);
        b.setText("");
        b.setToolTipText("Skip Left 10k");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b = ret.add(new SequenceSkipAction(1000, false));
        m_restrictedComponents.add(b);
        b.setText("");
        b.setToolTipText("Skip Left 1k");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b = ret.add(new SequenceSkipAction(1000, true));
        m_restrictedComponents.add(b);
        b.setText("");
        b.setToolTipText("Skip Right 1k");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b = ret.add(new SequenceSkipAction(10000, true));
        m_restrictedComponents.add(b);
        b.setText("");
        b.setToolTipText("Skip Right 10k");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);

        ret.addSeparator();

        b = new JButton(Icons.getInstance().getIcon("LASTDIFF"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Last Difference");
        m_restrictedComponents.add(b);
        b.addActionListener(new ResolveDifferenceAction(false));
        ret.add(b);

        b = new JButton(Icons.getInstance().getIcon("NEXTDIFF"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Next Difference");
        m_restrictedComponents.add(b);
        b.addActionListener(new ResolveDifferenceAction(true));
        ret.add(b);
        ret.addSeparator();

        b = new JButton(Icons.getInstance().getIcon("LASTCOMMENT"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.COMMENT, false, true, "nope"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Last Comment");
        ret.add(b);
        b = new JButton(Icons.getInstance().getIcon("NEXTCOMMENT"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.COMMENT, true, true, "nope"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Next Comment");
        ret.add(b);
        ret.addSeparator();

        b = new JButton(Icons.getInstance().getIcon("LASTPRIMER"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.PRIMER, false, true, "nope"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Last Primer");
        ret.add(b);
        b = new JButton(Icons.getInstance().getIcon("NEXTPRIMER"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.PRIMER, true, true, "nope"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Next Primer");
        ret.add(b);
        ret.addSeparator();
/*
		final JComboBox box = new JComboBox(m_geneBoxModel);
		m_restrictedComponents.add(box);
		box.setMaximumSize(
				new Dimension((int) TOOLBAR_BUTTON_SIZE.getWidth() * 6,
						(int) TOOLBAR_BUTTON_SIZE.getHeight()));
		box.setPreferredSize(
				new Dimension((int) TOOLBAR_BUTTON_SIZE.getWidth() * 6,
						(int) TOOLBAR_BUTTON_SIZE.getHeight()));
		box.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent ev)
					{
						if (getPrimaryPanel() == null) {
							return;
						}

						int posn =
							getPrimaryPanel()
							.resolveGenePosition(
									box.getSelectedItem().toString());

						if (posn >= 0) {
							getPrimaryPanel()
							.scrollToLocation(posn);
						}
					}
				});

		ret.add(box);*/

        return ret;
    }

    /**
     * Creates the toolbar (not to be confused with menubar!)
     *
     * @return the toolbar
     */
    protected JToolBar createGeneralToolBar() {
        ///////////////////////////////
        /// left hand side buttons  ///
        ///////////////////////////////
        JToolBar ret = new JToolBar();
        ret.setFloatable(true);
        ret.setMargin(new Insets(0, 5, 0, 5));

        JMenuItem mi = null;
        mi = new JMenuItem("From Fasta file", MenuKeyEvent.VK_F);
        mi.setAccelerator(KeyStroke.getKeyStroke("control T"));
        mi.addActionListener(new LoadFileAction("Load Sequences", "FASTA"));
        m_popup.add(mi);

        mi = new JMenuItem("From Base-by-base file", MenuKeyEvent.VK_B);
        mi.setAccelerator(KeyStroke.getKeyStroke("control B"));
        mi.addActionListener(new LoadFileAction("Load Sequences", "BBB"));
        m_popup.add(mi);

        mi = new JMenuItem("From Clustal Formatted file", MenuKeyEvent.VK_B);
        mi.setAccelerator(KeyStroke.getKeyStroke("control L"));
        mi.addActionListener(new LoadFileAction("Load Sequences", "CLUSTAL"));
        m_popup.add(mi);

        mi = new JMenuItem("From GenBank file", MenuKeyEvent.VK_G);
        mi.setAccelerator(KeyStroke.getKeyStroke("control G"));
        mi.addActionListener(new LoadFileAction("Load Sequences", "GENBANK"));
        m_popup.add(mi);

        mi = new JMenuItem("From EMBL file", MenuKeyEvent.VK_E);
        mi.setAccelerator(KeyStroke.getKeyStroke("control E"));
        mi.addActionListener(new LoadFileAction("Load Sequences", "EMBL"));
        m_popup.add(mi);

        m_loadButton = ret.add(new LoadAction(false));
        m_loadButton.setText("");
        m_loadButton.setToolTipText("Load Sequences");
        m_loadButton.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        m_loadButton.setPreferredSize(TOOLBAR_BUTTON_SIZE);

        m_loadButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    m_popup.show(m_loadButton, e.getX(), e.getY());
                }
            }
        });

        JButton b = null;
        b = ret.add(new SaveAction());
        b.setText("");
        b.setToolTipText("Save Sequence");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);

        m_restrictedComponents.add(b);
        ret.addSeparator();

        b = ret.add(new DisplayAreaAction());
        b.setText("");
        b.setToolTipText("Set Display Area");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        m_restrictedComponents.add(b);

        ret.addSeparator();

        final JToggleButton b1 = new JToggleButton(Icons.getInstance().getIcon("EDIT"));
        m_restrictedComponents.add(b1);
        b1.setToolTipText("Go To 'Edit' Mouse Mode");
        b1.setText("");
        b1.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b1.setPreferredSize(TOOLBAR_BUTTON_SIZE);

        final JToggleButton b2 = new JToggleButton(Icons.getInstance().getIcon("SELECT"));
        m_restrictedComponents.add(b2);
        b2.setToolTipText("Go To 'Select' Mouse Mode");
        b2.setText("");
        b2.setSelected(true);
        b2.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b2.setPreferredSize(TOOLBAR_BUTTON_SIZE);

        final JToggleButton b3 = new JToggleButton(Icons.getInstance().getIcon("GLUE"));
        m_restrictedComponents.add(b3);
        b3.setToolTipText("Go To 'Block Glue' Mouse Mode");
        b3.setText("");
        b3.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b3.setPreferredSize(TOOLBAR_BUTTON_SIZE);

        ButtonGroup bg = new ButtonGroup();
        ActionListener l = new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (b1.isSelected()) {
                    setMouseMode(EditPanel.EDIT_MODE);

                    //Warning message
                    JOptionPane.showMessageDialog(getFrame(), "There will be noticeable delay between the GUI and the user's actions.\n" + "Please be patient and refrain from erratically moving sequences.", "'Edit' Mouse Mode Warning", JOptionPane.WARNING_MESSAGE);

                    if (getPrimaryPanel() != null) {
                        getPrimaryPanel().clearSelection();
                    }

                    m_status.setDefaultText("Alignment Edit Mode");
                    m_status.clear();
                } else if (b2.isSelected()) {
                    setMouseMode(EditPanel.SELECT_MODE);
                    m_status.setDefaultText("Sequence Selection Mode");
                    m_status.clear();
                } else {
                    setMouseMode(EditPanel.GLUE_MODE);

                    //Warning message
                    JOptionPane.showMessageDialog(getFrame(), "There will be noticeable delay between the GUI and the user's actions.\n" + "Please be patient and refrain from erratically moving sequences.", "'Block Glue' Mouse Mode Warning", JOptionPane.WARNING_MESSAGE);

                    if (getPrimaryPanel() != null) {
                        getPrimaryPanel().clearSelection();
                    }

                    m_status.setDefaultText("Alignment Glue Mode");
                    m_status.clear();
                }
            }
        };

        bg.add(b1);
        bg.add(b3);
        bg.add(b2);
        b1.addActionListener(l);
        b2.addActionListener(l);
        b3.addActionListener(l);

        ret.add(b1);
        ret.add(b3);
        ret.add(b2);
        ret.addSeparator();

        b = new JButton(Icons.getInstance().getIcon("UNDO"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Undo last sequence modification");
        m_restrictedComponents.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                if (!getPrimaryPanel().undo()) {
                    System.out.println("Could not undo");
                }
            }
        });
        ret.add(b);
        b = new JButton(Icons.getInstance().getIcon("REDO"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Redo last sequence modification");
        m_restrictedComponents.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                if (!getPrimaryPanel().redo()) {
                    System.out.println("Could not redo");
                }
            }
        });
        ret.add(b);
        ret.addSeparator();

        b = ret.add(new SequenceSkipAction(10000, false));
        m_restrictedComponents.add(b);
        b.setText("");
        b.setToolTipText("Skip Left 10k");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b = ret.add(new SequenceSkipAction(1000, false));
        m_restrictedComponents.add(b);
        b.setText("");
        b.setToolTipText("Skip Left 1k");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);

        b = ret.add(new SequenceSkipAction(20, false));
        m_restrictedComponents.add(b);
        b.setText("");
        b.setToolTipText("Skip Left 20");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b = ret.add(new SequenceSkipAction(20, true));
        m_restrictedComponents.add(b);
        b.setText("");
        b.setToolTipText("Skip Right 20");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);

        b = ret.add(new SequenceSkipAction(1000, true));
        m_restrictedComponents.add(b);
        b.setText("");
        b.setToolTipText("Skip Right 1k");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b = ret.add(new SequenceSkipAction(10000, true));
        m_restrictedComponents.add(b);
        b.setText("");
        b.setToolTipText("Skip Right 10k");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);

        ret.addSeparator();

        b = new JButton(Icons.getInstance().getIcon("LASTDIFF"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Previous Difference");
        m_restrictedComponents.add(b);
        b.addActionListener(new ResolveDifferenceAction(false));
        ret.add(b);

        b = new JButton(Icons.getInstance().getIcon("NEXTDIFF"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Next Difference");
        m_restrictedComponents.add(b);
        b.addActionListener(new ResolveDifferenceAction(true));
        ret.add(b);
        ret.addSeparator();

        b = new JButton(Icons.getInstance().getIcon("LASTCOMMENT"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.COMMENT, false, true, "nope"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Previous Comment");
        m_restrictedComponents.add(b);
        ret.add(b);
        b = new JButton(Icons.getInstance().getIcon("NEXTCOMMENT"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.COMMENT, true, true, "nope"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Next Comment");
        m_restrictedComponents.add(b);
        ret.add(b);
        ret.addSeparator();

        b = new JButton(Icons.getInstance().getIcon("LASTGAP"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Previous Gap");
        m_restrictedComponents.add(b);
        b.addActionListener(new GapPositionAction(false));
        ret.add(b);
        b = new JButton(Icons.getInstance().getIcon("NEXTGAP"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Next Gap");
        m_restrictedComponents.add(b);
        b.addActionListener(new GapPositionAction(true));
        ret.add(b);
        ret.addSeparator();

        b = new JButton(Icons.getInstance().getIcon("LASTPRIMER"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.PRIMER, false, true, "nope"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Last Primer");
        m_restrictedComponents.add(b);
        ret.add(b);
        b = new JButton(Icons.getInstance().getIcon("NEXTPRIMER"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.PRIMER, true, true, "nope"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Next Primer");
        m_restrictedComponents.add(b);
        ret.add(b);
        ret.addSeparator();

        b = new JButton(Icons.getInstance().getIcon("LASTGENE"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.GENE, false, true, "nope"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Previous Gene");
        m_restrictedComponents.add(b);
        ret.add(b);

        b = new JButton(Icons.getInstance().getIcon("GENESTART"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.GENE, false, true, "front"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Start of Gene");
        m_restrictedComponents.add(b);
        ret.add(b);
        b = new JButton(Icons.getInstance().getIcon("GENEEND"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.GENE, true, true, "back"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to End of Gene");
        m_restrictedComponents.add(b);
        ret.add(b);

        b = new JButton(Icons.getInstance().getIcon("NEXTGENE"));
        b.addActionListener(new FeatureNavListenerAction(FeatureType.GENE, true, true, "nope"));
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Go to Next Gene");
        m_restrictedComponents.add(b);
        ret.add(b);


        ret.addSeparator();
        ret.add(Box.createHorizontalGlue());

        final JComboBox box = new JComboBox(m_geneBoxModel);
        m_restrictedComponents.add(box);
        box.setMinimumSize(new Dimension((int) TOOLBAR_BUTTON_SIZE.getWidth() * 9, (int) (TOOLBAR_BUTTON_SIZE.getHeight() + 1))); // +1 to accomodate OSX JComboBox
        box.setPreferredSize(new Dimension((int) TOOLBAR_BUTTON_SIZE.getWidth() * 9, (int) TOOLBAR_BUTTON_SIZE.getHeight()));
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if ((getPrimaryPanel() == null) || (box.getSelectedItem() == null)) {
                    return;
                }

                int posn = getPrimaryPanel().resolveGenePosition(box.getSelectedItem().toString());

                if (posn >= 0) {
                    getPrimaryPanel().scrollToLocation(posn);
                }
            }
        });

        ret.add(box);
        ret.addSeparator();

        b = new JButton("5' Top 3'", Icons.getInstance().getIcon("TOPSTRAND"));
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setToolTipText("View Other Strand");


        //  If the loaded sequences are DNA then we can look at top and bottom strands.
        //	If the sequences are AA, then there is no point to this button and it will
        //  be disabled.
        FeaturedSequence[] seq = getSequences();
        boolean is_DNA = false;
        for (int i = 0; i < seq.length; i++) {
            if (seq[i].getSequenceType() == DNA_SEQUENCE) {
                is_DNA = true;
            }
        }
        m_strandButton = b;
        m_strandButton.setEnabled(is_DNA);

        m_restrictedComponents.add(b);
        b.setMaximumSize(new Dimension((int) TOOLBAR_BUTTON_SIZE.getWidth() * 4, (int) TOOLBAR_BUTTON_SIZE.getHeight()));
        b.setPreferredSize(new Dimension((int) TOOLBAR_BUTTON_SIZE.getWidth() * 4, (int) TOOLBAR_BUTTON_SIZE.getHeight()));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (getPrimaryPanel() == null) {
                    return;
                }

                JButton src = (JButton) ev.getSource();

                if (getPrimaryPanel().getDisplayStrand().equals(POSITIVE)) {
                    getPrimaryPanel().setDisplayStrand(NEGATIVE);
                    src.setIcon(Icons.getInstance().getIcon("BOTTOMSTRAND"));
                    src.setText("3' Bottom 5'");
                } else {
                    getPrimaryPanel().setDisplayStrand(POSITIVE);
                    src.setIcon(Icons.getInstance().getIcon("TOPSTRAND"));
                    src.setText("5' Top 3'");
                }

                refreshGeneComboBox();
            }
        });
        ret.add(b);
        ret.addSeparator(new Dimension(20, 0));
        b = ret.add(new HelpAction());
        b.setText("");
        b.setMaximumSize(TOOLBAR_BUTTON_SIZE);
        b.setPreferredSize(TOOLBAR_BUTTON_SIZE);
        b.setToolTipText("Open Help");

        return ret;
    }

    /**
     * sets the consensus type to be displayed in this window.
     *
     * @param consType the new consensus type
     */
    protected void setConsensus(int consType) {
        if (getPrimaryPanel() == null) {
            return;
        }

        getPrimaryPanel().showConsensus(consType);
    }

    /**
     * This refreshes the Strand Top/Button button to the current correct title
     */
    protected void refreshStrandButton() {
        if (getPrimaryPanel() == null) {
            return;
        }

        m_strandButton.setText((getPrimaryPanel().getDisplayStrand() == POSITIVE) ? "5' Top 3'" : "3' Bottom 5'");
    }


    /**
     * This refreshes the gene name combo box to display genes that are
     * appropriate for the currently open sequences and the strand that is
     * currently displayed.
     */
    public void refreshGeneComboBox() {
        if (getPrimaryPanel() == null) {
            return;
        }

        m_geneBoxModel.removeAllElements();

        String[] names = getPrimaryPanel().getSelectedGeneNames();

        m_geneBoxModel.addElement((getPrimaryPanel().getDisplayStrand() == POSITIVE) ? "Top Genes" : "Bottom Genes");

        for (int i = 0; i < names.length; ++i) {
            m_geneBoxModel.addElement(names[i]);
        }
    }

    /**
     * close the currently selected sequences
     */
    protected void removeSelectedSequences() {
        if (getPrimaryPanel() == null) {
            return;
        }

        int rc = JOptionPane.showConfirmDialog(getContentPane(), "<html>Removing sequences from an alignment may result in columns consisting entirely of gaps<br>" +
                "in the remaining alignment. Base-By-Base can remove these gapped columns automatically,<br>" +
                "however this may result in real sequence data being deleted. Would you like to remove these<br>" +
                "gapped columns?</html>", "Remove Selected Sequences", JOptionPane.YES_NO_CANCEL_OPTION);

        if (rc == JOptionPane.CANCEL_OPTION) {
            return;
        }

        getPrimaryPanel().removeSelectedSequences();

        if (rc == JOptionPane.YES_OPTION) {
            getPrimaryPanel().removeAllGapColumns();
        }

        if (getSequences().length == 0) {
            m_main.remove(getPrimaryPanel());
            m_main.add(m_splash, BorderLayout.CENTER);

            setSize(DEFAULT_SIZE);

            setRestrictedComponentsEnabled(false);

            setSavable(false);
            setWorkFilename("");

            UndoHandler.getInstance().reset();
            UndoHandler.getInstance().setModified(false);

            setPrimaryPanel(null);
        } else {
            UndoHandler.getInstance().reset();
            UndoHandler.getInstance().setModified(true);
        }

        refreshTitle();
    }

    /**
     * Close all currently opened sequences and redisplay the splash logo
     */
    protected void closeAllSequences() {
        if (!checkForSave()) {
            return;
        }

        if (getPrimaryPanel() == null) {
            return;
        }

        getPrimaryPanel().removeAllSequences();
        m_main.remove(getPrimaryPanel());
        m_main.add(m_splash, BorderLayout.CENTER);

        setSize(DEFAULT_SIZE);

        setRestrictedComponentsEnabled(false);

        setSavable(false);
        setWorkFilename("");

        UndoHandler.getInstance().reset();
        UndoHandler.getInstance().setModified(false);

        setPrimaryPanel(null);

        refreshTitle();
    }

    /**
     * save the currently open sequences to the given filename
     *
     * @param filename the file to save to
     * @return true if it was completed succesfully
     */
    protected boolean saveSequences(String filename) {
        System.out.println("SaveSequences2");
        try {
            getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
            m_status.setText("Saving File " + filename);
            if (notebook == null) {
                notebook = "";
            }

            FeaturedSequenceWriter out = FeaturedSequenceWriter.createFeaturedSequenceWriter(filename, notebook);

            ArrayList<FeaturedSequence> l = new ArrayList<FeaturedSequence>();
            FeaturedSequence[] seqs = getSequences();

            for (int i = 0; i < seqs.length; ++i) {
                l.add(seqs[i]);
            }

            out.writeSequences(l.listIterator());

            UITools.invoke(new Runnable() {
                public void run() {
                    getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    m_status.clear();
                }
            });

            String title = getTitle();
            if (title.endsWith(MODIFIED_TEXT)) {
                title = title.substring(0, title.length() - MODIFIED_TEXT.length());
                setTitle(title);
            }

            UndoHandler.getInstance().setModified(false);

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NullPointerException e) {
            //e.printStackTrace();
            SaveAsAction n = new SaveAsAction();
            n.setEnabled(true);
            System.out.println(n);

            return false;
        } catch (Exception iex) {
            iex.printStackTrace();
        }

        UITools.showWarning("An error occured while saving to the file " + filename, DiffEditorFrame.this);
        return false;
    }

    /**
     * Exports selected sequences to file
     *
     * @param all if true, export all, otherwise only the one selected
     * @return true if it completed
     */
    protected boolean exportSequences(boolean all) {

        if (getPrimaryPanel() == null) {
            UITools.showError("No sequences to export", null);
        }

        FeaturedSequence[] seqs = getPrimaryPanel().getSelectedSegments(all);


        if (seqs == null) {
            UITools.showWarning("Please select a region of sequence to export", getContentPane());
            return false;
        }

        JFileChooser fc = new JFileChooser(m_currentDirectory);
        fc.setDialogTitle("Export Selected Sequence Data");

        MultiFileFilter ff1 = new MultiFileFilter("Fasta Format");
        ff1.addExtension("fasta");
        fc.addChoosableFileFilter(ff1);

        MultiFileFilter ff2 = new MultiFileFilter("BBB Format (XML)");
        ff2.addExtension("bbb");
        fc.addChoosableFileFilter(ff2);

        MultiFileFilter ff3 = new MultiFileFilter("ClustalW Format");
        ff3.addExtension("aln");
        fc.addChoosableFileFilter(ff3);

        MultiFileFilter ff4 = new MultiFileFilter("Primer Format");
        ff4.addExtension("primer");
        fc.addChoosableFileFilter(ff4);

        fc.setFileFilter(ff1);
        fc.setAcceptAllFileFilterUsed(false);

        try {
            if (fc.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                String filename = f.getAbsolutePath();
                javax.swing.filechooser.FileFilter ff = fc.getFileFilter();
                if (fc.getFileFilter() == null) {
                    m_status.setText("Please select a file format.");
                }

                if (ff == ff1) {
                    if (!ff1.accept(f)) {
                        if (!filename.endsWith(".")) {
                            filename += ".";
                        }

                        filename += "fasta";
                    }
                } else if (ff == ff2) {
                    if (!ff2.accept(f)) {
                        if (!filename.endsWith(".")) {
                            filename += ".";
                        }

                        filename += "bbb";
                    }
                } else if (ff == ff3) {
                    if (!ff3.accept(f)) {
                        if (!filename.endsWith(".")) {
                            filename += ".";
                        }

                        filename += "aln";
                    }
                } else if (ff == ff4) {
                    if (!ff4.accept(f)) {
                        if (!filename.endsWith(".")) {
                            filename += ".";
                        }

                        filename += "primer";
                    }
                }

                getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                m_status.setText("Saving File " + f.toString());
                System.out.println(filename);
                FeaturedSequenceWriter out = FeaturedSequenceWriter.createFeaturedSequenceWriter(filename, notebook);
                ArrayList<FeaturedSequence> l = new ArrayList<FeaturedSequence>();

                for (int i = 0; i < seqs.length; ++i) {
                    l.add(seqs[i]);
                }

                out.writeSequences(l.listIterator());

                UITools.invoke(new Runnable() {
                    public void run() {
                        getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        m_status.clear();
                    }
                });

                return true;
            } else {
                return false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception iex) {
            iex.printStackTrace();
        }

        return false;
    }

    /**
     * Save the currently viewed sequences to a data (xml) file
     *
     * @return true if it completed
     */
    protected boolean saveSequences() {

        System.out.println("SaveSequences no params");
        if (getPrimaryPanel() == null) {
            UITools.showError("No sequences to save", null);
        }

        JFileChooser fc = new JFileChooser(m_currentDirectory);
        fc.setDialogTitle("Save Sequence Data");

        MultiFileFilter ff1 = new MultiFileFilter("Fasta Format");
        ff1.addExtension("fasta");
        fc.addChoosableFileFilter(ff1);

        MultiFileFilter ff2 = new MultiFileFilter("BBB Format (XML)");
        ff2.addExtension("bbb");
        fc.addChoosableFileFilter(ff2);

        MultiFileFilter ff3 = new MultiFileFilter("ClustalW Format");
        ff3.addExtension("aln");
        fc.addChoosableFileFilter(ff3);

        MultiFileFilter ff4 = new MultiFileFilter("BBB Primer Format");
        ff4.addExtension("primer");
        fc.addChoosableFileFilter(ff4);

        MultiFileFilter ff5 = new MultiFileFilter("Phylip Format");
        ff5.addExtension("phy");
        fc.addChoosableFileFilter(ff5);

        fc.setFileFilter(ff2);
        String filename = "";
        add(fc);
        validate();

        try {
            int returnVal = fc.showSaveDialog(fc.getParent());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                filename = f.getAbsolutePath();
                javax.swing.filechooser.FileFilter ff = fc.getFileFilter();

                if (ff == ff1) {
                    if (!ff1.accept(f)) {
                        if (!filename.endsWith(".fasta")) {
                            filename += ".fasta";
                        }
                    }
                } else if (ff == ff2) {
                    if (!ff2.accept(f)) {
                        if (!filename.endsWith(".bbb")) {
                            filename += ".bbb";
                        }
                    }
                } else if (ff == ff3) {
                    if (!ff3.accept(f)) {
                        if (!filename.endsWith(".aln")) {
                            filename += ".aln";
                        }
                    }
                    if (!getPrimaryPanel().checkLengths()) {
                        boolean val = UITools.showYesNo("Sequences to be saved (in .aln) format are of different lengths" + "\n" +
                                " would you like to equalize this alignment before saving?", this);
                        if (val) {
                            getPrimaryPanel().equalizeLengths();
                        }
                    }
                } else if (ff == ff4) {
                    if (!ff4.accept(f)) {
                        if (!filename.endsWith(".primer")) {
                            filename += ".primer";
                        }
                    }
                } else if (ff == ff5) {
                    if (!ff5.accept(f)) {
                        if (!filename.endsWith(".phy")) {
                            filename += ".phy";
                        }
                    }
                    if (!getPrimaryPanel().checkLengths()) {
                        boolean val = UITools.showYesNo("Sequences to be saved (in .phy) format are of different lengths, they need to be equalized before saving" + "\n" +
                                " would you like to proceed with this?", this);
                        if (val) {
                            getPrimaryPanel().equalizeLengths();
                        } else
                            return false;
                    }
                }


                File outfile = new File(filename);

                if (outfile.exists()) {
                    if (!UITools.showYesNo(filename + " exists, are you sure you would like to replace it?", getPrimaryPanel())) {
                        return false;
                    }
                }

                getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                m_status.setText("Saving File " + f.toString());
                try {
                    notebook = nb_frame.getText();
                } catch (Exception e) {
                }
                FeaturedSequenceWriter out = FeaturedSequenceWriter.createFeaturedSequenceWriter(filename, notebook);

                ArrayList<FeaturedSequence> l = new ArrayList<FeaturedSequence>();
                FeaturedSequence[] seqs = getPrimaryPanel().getSequences();

                for (int i = 0; i < seqs.length; ++i) {

                    l.add(seqs[i]);
                }

                out.writeSequences(l.listIterator());


                setSavable(true);
                setWorkFilename(filename);
                UndoHandler.getInstance().reset();
                UndoHandler.getInstance().setModified(false);


                UITools.invoke(new Runnable() {
                    public void run() {
                        getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        m_status.clear();
                    }
                });

                return true;
            } else {
                return false;
            }
        } catch (IOException ex) {
            getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            m_status.setText("Error Saving File");
            ex.printStackTrace();
        } catch (Exception iex) {
            getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            m_status.setText("Error Saving File");
            iex.printStackTrace();
        }

        UITools.showWarning("An error occured while saving to the file " + filename, DiffEditorFrame.this);
        return false;
    }

    /**
     * add sequences from file to the current open alignment
     *
     * @return true if it completed
     */
    protected boolean appendSequences() {
        File f = getSequenceFileFromCommonDialog("Append Sequence(s)");
        if (f == null) {
            return false;
        }

        try {
            getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
            m_status.setText("Adding Sequences from File " + f.getAbsolutePath() + "...");

            FeaturedSequenceReader in = DiffEditFeaturedSequenceReader.createFeaturedSequenceReader(f);
            final ListIterator li = in.getSequences();

            Vector<FeaturedSequence> fileSequences = new Vector<FeaturedSequence>();

            while (li.hasNext()) {
                fileSequences.add((FeaturedSequence) li.next());
            }

            appendSequences(fileSequences.toArray(new FeaturedSequence[0]));

            getPrimaryPanel().refreshEditors();
            getPrimaryPanel().refreshConsensus();

            return true;
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
        } catch (IOException ex) {
            ex.printStackTrace();
            UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
        } catch (Exception iex) {
            iex.printStackTrace();
            UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
        } finally {
            m_status.clear();
            getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        return false;
    }

    protected boolean appendSequences(final FeaturedSequence[] seqs, String dbName) throws InterruptedException, java.lang.reflect.InvocationTargetException {
        PrimaryPanel p = null;

        if (getPrimaryPanel() == null) {
            p = new PrimaryPanel();
            setPrimaryPanel(p);
            p.setMouseMode(m_mouseMode);
            p.setChannelPreferences(getShownChannels());
        } else {
            p = getPrimaryPanel();
        }

        setRestrictedComponentsEnabled(true);

        // Following statements updates "Remove Columns.. " menu item depending on AA seq or DNA seq
        if (seqs[0].getSequenceType() == EditableSequence.AA_SEQUENCE) {
            m_strip.setText("Remove Columns Containing Non-Amino Acid Codes");
        } else {
            m_strip.setText("Remove Columns Containing Non-ACTGU Characters");
        }

        getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));

        for (int i = 0; i < seqs.length; ++i) {
            p.addSequenceEditor(seqs[i], dbName);
        }

        p.refreshEditors();
        p.updateDifferenceLists();
        p.refreshState();

        UITools.invoke(new Runnable() {
            public void run() {
                m_main.remove(m_splash);
                m_main.add(getPrimaryPanel(), BorderLayout.CENTER);

                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                m_status.clear();

                resetBounds();
                refreshGeneComboBox();

                UndoHandler.getInstance().reset();
                UndoHandler.getInstance().setModified(true);
            }
        });

        return true;
    }

    protected boolean appendSequences(FeaturedSequence[] seqs) throws InterruptedException, java.lang.reflect.InvocationTargetException {
        return appendSequences(seqs, null);
    }

    /**
     * load sequences into the window from an array of sequences already
     * in memory
     *
     * @param seqs the sequences
     * @return true if it worked
     */
    public boolean loadSequences(final FeaturedSequence[] seqs) {
        System.out.println("LOAD SEQUENCES 2");
        if (getPrimaryPanel() != null) {
            m_main.remove(getPrimaryPanel());
        }



        final PrimaryPanel p = new PrimaryPanel(seqs);
        setPrimaryPanel(p);
        p.setMouseMode(m_mouseMode);
        p.setChannelPreferences(getShownChannels());

        setRestrictedComponentsEnabled(true);
        m_muscle.setEnabled(true);
        if (seqs[0].getSequenceType() == EditableSequence.AA_SEQUENCE) {
            getStrandButton().setEnabled(false);
            p.setStranded(false);
            m_dataPanel.setDisplayStrand(POSITIVE);
        } else {
            getStrandButton().setEnabled(true);
            p.setStranded(true);
        }

        // Following statements updates "Remove Columns.. " menu item depending on AA seq or DNA seq
        if (seqs[0].getSequenceType() == EditableSequence.AA_SEQUENCE)
            m_strip.setText("Remove Columns Containing Non-Amino Acid Codes");
        else if (seqs[0].getSequenceType() == EditableSequence.DNA_SEQUENCE)
            m_strip.setText("Remove Columns Containing Non-ACTGU Characters");

        try {
            UITools.invoke(new Runnable() {
                public void run() {
                    m_main.remove(m_splash);
                    m_main.add(p, BorderLayout.CENTER);
                    getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    m_status.clear();

                    resetBounds();
                    refreshGeneComboBox();
                    setSavable(false);
                    setWorkFilename("");
                    UndoHandler.getInstance().reset();
                    UndoHandler.getInstance().setModified(true);
                    refreshTitle();
                }
            });
        } catch (Exception ex) {
        }
        // thread exceptions
        return true;
    }

    /**
     * prompt for file and load sequences from disk. This will close the
     * current file and load a new one.
     *
     * @return true if it completed
     */
    protected boolean loadSequences() {
        //
        // --- check saved status ---
        //
        if (!mafftaddfrag && !mafftadd) {
            if (!checkForSave()) {
                return false;
            }
        }


        File f = null;
        // Autoload predefined file if doing snip algorithm
        if (snip)
            f = new File("tempout.bbb");
        else if (mafftadd || mafftaddfrag)
            f = new File("out.fasta");
        else
            f = getSequenceFileFromCommonDialog("Load Sequence(s)");
        if (f == null)
            return false;

        try {
            final String filename = f.getAbsolutePath();

            getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
            m_status.setText("Loading Alignment from File:" + filename + "...");

            FeaturedSequenceReader in = DiffEditFeaturedSequenceReader.createFeaturedSequenceReader(filename);
            final ListIterator li = in.getSequences();

            Vector v = new Vector();
            while (li.hasNext()) {
                v.add(li.next());
            }
            System.out.println(filename + " has been parsed.");
            loadSequences((FeaturedSequence[]) v.toArray(new FeaturedSequence[0]));
            setSavable(true);
            setWorkFilename(filename);
            refreshTitle();
            return true;
        } catch (IllegalArgumentException iae) {
            UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            iae.printStackTrace();
        } catch (IOException ex) {
            UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            ex.printStackTrace();
        } catch (Exception iex) {
            UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            iex.printStackTrace();
            m_status.setText("Error reading file.");
        } finally {
            m_status.clear();
            getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        return false;
    }


    /**
     * This class method is used to create a primer feature for the sequence from the file
     *
     * @param lines is the lines read from the File
     * @author asyed
     */
    public void primerSearchAndInsert(Vector<String> lines, int mm_percent) {
        String delim = '\t' + "";
        String primer_seq = null, primer_name = null, fridge1 = null, fridge2 = null, temp = null, comments = null;
        Vector<Vector<String>> allLines = new Vector<Vector<String>>();
        for (int i = 0; i < lines.size(); i++) {
            StringTokenizer st = new StringTokenizer(lines.get(i), delim);
            Vector<String> v = new Vector<String>();
            if (st.countTokens() == 6) {
                primer_seq = st.nextToken();
                v.add(primer_seq);
                primer_name = st.nextToken();
                v.add(primer_name);
                fridge1 = st.nextToken();
                v.add(fridge1);
                fridge2 = st.nextToken();
                v.add(fridge2);
                temp = st.nextToken();
                if (temp.equals("null") || temp.equals(" ") || temp.equals("")) {
                    temp = "" + PrimaryPanel.getPrimerTemp(primer_seq);
                }
                v.add(temp);
                comments = st.nextToken();
                v.add(comments);

            } else {
                primer_seq = st.nextToken();
                v.add(primer_seq);
                primer_name = st.nextToken();
                v.add(primer_name);
                fridge1 = st.nextToken();
                v.add(fridge1);
                fridge2 = st.nextToken();
                v.add(fridge2);
                v.add("" + PrimaryPanel.getPrimerTemp(primer_seq));
                comments = st.nextToken();
                v.add(comments);
            }
            allLines.add(v);
        }
        getPrimaryPanel().loadPrimerWithValues(mm_percent, allLines);
    }


    /**
     * Load sequences from a string of sequence text
     *
     * @param seqtext The sequence text
     * @param type    The type of the text ("bbb","aln" or "fasta")
     * @return true if the load was successful
     * @throws IOException If a problem occured loading
     */
    public boolean loadSequences(final String seqtext, final String type) throws IOException {
        FeaturedSequenceReader in = DiffEditFeaturedSequenceReader.createFeaturedSequenceReader(seqtext, type);
        final ListIterator li = in.getSequences();
        Vector v = new Vector();
        while (li.hasNext()) {
            v.add(li.next());
        }
        return (loadSequences((FeaturedSequence[]) v.toArray(new FeaturedSequence[0])));
    }


    protected File getSequenceFileFromCommonDialog(String title) {
        File chosenFile = new FileChooser().openFile(title, file);
        //If the user clicks cancel, we want to remember the last valid file
        if (chosenFile != null) {
            file = chosenFile;
        }
        return chosenFile;
    }

    /**
     * Checks if the file is properly formatted
     *
     * @param f      is the loaded file
     * @param format of the file
     * @return the array
     */
    public boolean checkFileFormat(File f, String format) {
        try {
            String regex, regexM = "MUSCLE";
            String line;
            System.out.println("File: " + f.getName() + " exists: " + f.exists());
            BufferedReader br = new BufferedReader(new FileReader(f));
            while ((line = br.readLine()) == "\n") {
                System.out.println("line: " + line);
            }

            if (line != null) {
                if (format.equals("FASTA")) {
                    regex = ">";
                } else if (format.equals("CLUSTAL")) {
                    regex = "CLUSTAL";
                } else if (format.equals("GENBANK")) {
                    regex = "LOCUS";
                } else if (format.equals("BBB")) {
                    regex = "<\\?xml";
                } else if (format.equals("mRNA")) {
                    regex = "SEQ_NAME	START	END	STRAND	COUNTS";
                } else {
                    regex = "ID";
                }

                regex = "^" + regex;
                line.trim();
                System.out.println(regex + "::" + line);
                Pattern pattern = Pattern.compile(regex);
                Pattern patternM = Pattern.compile(regexM);
                Matcher matcher = pattern.matcher(line);
                Matcher matcherM = patternM.matcher(line);

                if (!matcher.find()) {
                    if (format.equals("CLUSTAL") && matcherM.find()) {
                        return true;
                    }
                    UITools.showWarning("Base-By-Base accepts ONLY *TEXT* formatted Files. The file you \nhave selected is not in " + format + " format please try again", null);

                    return false;
                }
                return true;
            } else {
                UITools.showWarning("You have entered an invalid or empty file, please check and retry", null);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get an array of the channels that are displayed in this window
     *
     * @return the array
     */
    protected int[] getShownChannels() {
        return new int[]{ //primer, NOTE the channels are set somewhere else, so this really isn't used. but for consistency
                EditPanel.SCALE_CHANNEL, EditPanel.ABSCALE_CHANNEL, EditPanel.ACID_CHANNEL, EditPanel.EVENT_CHANNEL, EditPanel.SEARCH_CHANNEL, EditPanel.PRIMER_CHANNEL, EditPanel.ALIGN_CHANNEL, EditPanel.DIFF_CHANNEL};
    }

    /**
     * Reset the window bounds to the viewable properties
     */
    protected void resetBounds() {
        try {
            final DiffEditorFrame diffEditorFrame = this;
            UITools.invoke(new Runnable() {
                public void run() {
                    if (getPrimaryPanel() == null) {
                        return;
                    }

                    Dimension d = getPrimaryPanel().getPreferredSize();
                    Dimension screen = UITools.getScreenSize();

                    int newHeight = d.height + 150;

                    if (newHeight > screen.height) {
                        newHeight = screen.height - 60;
                    }

                    // resize and then center the window
                    setSize(new Dimension(getWidth(), newHeight));
                    UITools.positionCenter(diffEditorFrame);
                }
            });
        } catch (Exception iex) {
            iex.printStackTrace();
        }
    }

    /**
     * The following generates a text document with as dense a text packing as possible
     * It builds the comparisons based on the individuals current comprison style
     */


    public void ExportAlignmentOverview() {
        if (getPrimaryPanel() == null) {
            return;
        }

        int chars_wide = 100; // number of characters per line


        if (this.getSelectedSequences().length == 0) {
            UITool.showWarning("Please select at least one sequence", getContentPane());
            return;
        }

        if (comp1.isSelected()) {
            if (getPrimaryPanel().m_consensus == Consensus.EMPTY_CONSENSUS) {
                getPrimaryPanel().m_consensus = ConsensusFactory.createConsensus(ConsensusFactory.IDENTITY, getVisibleSequences());
            }
        }
        String nl = System.getProperty("line.separator");
        System.out.println("Starting export......");
        FeaturedSequence[] seqs = getPrimaryPanel().getSelectedSegments(false);

        //  If the user has not selected a region get the whole region
        try {
            if (seqs.length == 0) {
                seqs = getPrimaryPanel().getSelectedSequences();
            }
        } catch (Exception e) {
            seqs = getPrimaryPanel().getSelectedSequences();
        }


        //  What is the longest sequence length
        int ml = 0;

        for (int i = 0; i < seqs.length; ++i) {
            if (seqs[i].length() > ml) {
                ml = seqs[i].length();
            }
        }


        JFileChooser fc = new JFileChooser(m_currentDirectory);
        fc.setDialogTitle("Export Alignment to Image");

        if (fc.showDialog(DiffEditorFrame.this, "Save") == JFileChooser.APPROVE_OPTION) {
            final StringBuffer s = new StringBuffer(fc.getSelectedFile().getAbsolutePath());

            if (s.toString().equals("")) {
                return;
            }

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(s.toString()));


                //Write the names of the sequences and their corresponding indices
                for (int i = 0; i < seqs.length; ++i) {
                    bw.write(((i + 1) + ": " + seqs[i].getName() + nl));
                }
                bw.write(nl + nl);


                //Pairwise comparison
                if (comp1.isSelected()) {
                    for (int i = 0; i < (1 + ml / chars_wide) - 1; i++) {

                        Vector<Character> one = new Vector<Character>();

                        int start = i * chars_wide;
                        int stop = (1 + i) * chars_wide;

                        for (int j = start; j < stop; j++) {
                            try {
                                one.add(seqs[0].charAt(j));
                            } catch (Exception e) {
                                one.add('-');
                            }
                        }
                        String temp = "";
                        for (int l = 0; l < one.size(); l++) {
                            temp += one.elementAt(l);
                        }
                        bw.write(("1: " + temp + nl));

                        Vector<Comparable> temp_seq;
                        Vector<Character> comp_to;

                        for (int j = 1; j < seqs.length; j++) {
                            temp_seq = new Vector<Comparable>();
                            comp_to = new Vector<Character>();
                            for (int k = start; k < stop; k++) {
                                try {
                                    temp_seq.add(seqs[j].charAt(k));
                                } catch (Exception e) {
                                    temp_seq.add('-');
                                }
                                try {
                                    comp_to.add(seqs[j - 1].charAt(k));
                                } catch (Exception e) {
                                    comp_to.add('-');
                                }
                                if (temp_seq.toArray()[k - start] != comp_to.toArray()[k - start]) {
                                    String hold = temp_seq.elementAt(k - start).toString().toLowerCase();
                                    temp_seq.removeElementAt(k - start);
                                    temp_seq.insertElementAt(hold, k - start);
                                }
                            }
                            temp = "";
                            for (int l = 0; l < temp_seq.size(); l++) {
                                temp += temp_seq.elementAt(l);
                            }
                            bw.write(((j + 1) + ": " + temp + nl));
                        }

                        bw.write(nl);
                        bw.flush();
                        //bw.write(("#################################################" + nl + nl));
                        //System.out.println("#################################################\n\n");
                    }
                }

                //Consensus Comparison
                else if (comp2.isSelected()) {
                    if (getPrimaryPanel().m_consensus == Consensus.EMPTY_CONSENSUS) {
                        getPrimaryPanel().m_consensus = ConsensusFactory.createConsensus(ConsensusFactory.IDENTITY, getVisibleSequences());
                    }

                    for (int i = 0; i < (1 + ml / chars_wide); i++) {

                        int start = i * chars_wide;
                        int stop = (1 + i) * chars_wide;
                        if (stop > ml) {
                            stop = ml;
                        }

                        Vector<Character> cons = new Vector<Character>();
                        char[] temps = getPrimaryPanel().m_consensus.getSequence(start, stop).toCharArray();
                        for (int k = 0; k < chars_wide; k++) {
                            try {
                                cons.add(temps[k]);
                            } catch (Exception e) {
                                cons.add('-');
                            }
                        }


                        Vector<Comparable> temp_seq;
                        for (int j = 0; j < seqs.length; j++) {

                            temp_seq = new Vector<Comparable>();
                            //char[] temp_seq = seqs[j].substring(start, stop).toCharArray();
                            for (int k = start; k < stop; k++) {
                                try {
                                    temp_seq.add(seqs[j].charAt(k));
                                } catch (Exception e) {
                                    temp_seq.add('-');
                                }
                                if (temp_seq.toArray()[k - start] != cons.toArray()[k - start]) {
                                    String hold = temp_seq.elementAt(k - start).toString().toLowerCase();
                                    temp_seq.removeElementAt(k - start);
                                    temp_seq.insertElementAt(hold, k - start);
                                }
                            }
                            String temp = "";
                            for (int l = 0; l < temp_seq.size(); l++) {
                                temp += temp_seq.elementAt(l);
                            }

                            bw.write(((j + 1) + ": " + temp + nl));
                            //System.out.println((j+1)+": " + new String(temp_seq));

                        }
                        bw.write(nl);
                        bw.flush();
                        //bw.write(("#################################################" + nl + nl));
                        ///System.out.println("#################################################\n\n");
                    }
                }

                // Compare against top sequence
                else {

                    for (int i = 0; i < (1 + ml / chars_wide); i++) {

                        int start = i * chars_wide;
                        int stop = (1 + i) * chars_wide;
                        if (stop > ml) {
                            stop = ml;
                        }

                        Vector<Comparable> top = new Vector<Comparable>();
                        char[] temps = seqs[0].substring(start, stop).toCharArray();
                        if (temps.length > 1) {
                            for (int k = 0; k < chars_wide; k++) {
                                try {
                                    top.add(temps[k]);
                                } catch (Exception e) {
                                    top.add('-');
                                }
                            }
                        } else {
                            for (int k = 0; k < chars_wide; k++) {
                                try {
                                    top.add(" ");
                                } catch (Exception e) {
                                }
                            }
                        }
                        String temp = "";
                        for (int l = 0; l < top.size(); l++) {
                            temp += top.elementAt(l);
                        }
                        bw.write("1: " + temp + nl);

                        Vector<Comparable> temp_seq;
                        for (int j = 1; j < seqs.length; j++) {
                            temp_seq = new Vector<Comparable>();

                            for (int k = start; k < stop; k++) {


                                try {
                                    temp_seq.add(seqs[j].charAt(k));
                                } catch (Exception e) {
                                    temp_seq.add('-');
                                }
                                //System.out.println(k);

                                try {
                                    if (temp_seq.toArray()[k - start] != top.toArray()[k - start]) {
                                        String hold = temp_seq.elementAt(k - start).toString().toLowerCase();
                                        temp_seq.removeElementAt(k - start);
                                        temp_seq.insertElementAt(hold, k - start);
                                    }
                                } catch (Exception e) {
                                    System.out.println(k - start);
                                }


                            }
                            temp = "";
                            for (int l = 0; l < temp_seq.size(); l++) {
                                temp += temp_seq.elementAt(l);
                            }
                            bw.write(((j + 1) + ": " + temp + nl));
                        }
                        bw.write(nl);
                    }

                    //	}


                    bw.write(nl);
                    bw.flush();
                    //bw.write(("#################################################" + nl + nl));
                    //System.out.println("#################################################\n\n");
                }
                //}


                bw.flush();
                bw.close();
                System.out.println("Done!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


    /**
     * spawn a new editor
     *
     * @return the new editor
     */
    protected DiffEditorFrame spawnNewWindow() {
        return new DiffEditorFrame(m_dbpref);
    }

    //~ Inner Classes //////////////////////////////////////////////////////////
    private class FileImportDropListener implements FileDrop.Listener {
        public void filesDropped(java.io.File[] files) {
            try {

                getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));

                for (File file : files) {

                    String type = getFileType(file);
                    if (type == null) {
                        UITools.showWarning("Unsupported file format. We support the following:\n" +
                                "GenBank (*.gb or *.gbk), " +
                                "EMBL (*.embl), " +
                                "BBB (*.bbb), " +
                                "FASTA (*.fasta or *.fas or *.fa), " +
                                "and CLUSTAL (*.clustal or *.clustalw)", getContentPane());
                        return;
                    }

                    String filename = file.getAbsolutePath();
                    if (!checkFileFormat(file, type)) {
                        return;
                    }

                    FeaturedSequenceReader in = null;
                    if (type.equals("CLUSTAL"))
                        in = new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.CLUSTAL_FORMAT, file, DiffEditFeaturedSequence.class);
                    else if (type.equals("EMBL"))
                        in = new EMBLFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                    else if (type.equals("FASTA"))
                        in = new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.FASTA_FORMAT, file, DiffEditFeaturedSequence.class);
                    else if (type.equals("GENBANK"))
                        in = new GenBankFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                    else if (type.equals("BBB")) {
                        in = new BSMLFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                    } else {
                        UITools.showWarning("Unsupported file format. We support the following:\nGenBank, EMBL, BBB, FASTA, and CLUSTAL", getContentPane());
                        return;
                    }

                    //ADDING SEQUENCES TO ALIGNMENT
                    m_status.setText("Adding Sequences from File " + file.getAbsolutePath() + "...");
                    final ListIterator li = in.getSequences();
                    Vector<FeaturedSequence> fileSequences = new Vector<FeaturedSequence>();
                    while (li.hasNext()) {
                        fileSequences.add((FeaturedSequence) li.next());
                    }

                    appendSequences(fileSequences.toArray(new FeaturedSequence[0]));

                    if (type.equals("BBB")) {
                        notebook = ((BSMLFeaturedSequenceReader) in).getNotebook();
                    }

                    setWorkFilename(filename);
                    refreshTitle();
                    setSavable(true);
                    System.out.println(filename + " Successfully parsed.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());

            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());

            } catch (Exception iex) {
                iex.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());

            } finally {
                m_status.clear();
                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    protected class NewAlignmentAction extends AbstractAction {
        public NewAlignmentAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            int val = JOptionPane.showConfirmDialog(getContentPane(), "Opening multiple alignments simultaneously is allowed.\nYou may run out of memory after opening 3 or 4 windows, especially if the alignments are very large.", "New Alignment Window", JOptionPane.OK_CANCEL_OPTION);

            if (val == JOptionPane.YES_OPTION) {
                DiffEditorFrame df = spawnNewWindow();
                df.setProcess(m_process);
                df.setSize(DiffEditorFrame.DEFAULT_SIZE);
                df.setVisible(true);
            }
        }
    }

    protected class AppendFileAction extends AbstractAction {
        public AppendFileAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            Thread t = new Thread() {
                public void run() {
                    appendSequences();
                }
            };
            t.start();
        }
    }

    protected class RemoveSequencesAction extends AbstractAction {
        public RemoveSequencesAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel().getSelectedSequences().length == 0) {
                UITools.showWarning("Mark sequences by clicking their names on the left, or by choosing 'Mark All Sequences' from the 'Edit' Menu.", getContentPane());
            } else {
                if (UITools.showYesNo("<html>All marked sequences will be removed from the alignment.<br>" + "Are you sure you would like to do this?</html>", getContentPane())) {
                    removeSelectedSequences();
                }
            }
        }
    }

    protected class FeatureFileImportAction extends AbstractAction {
        private String m_type;

        public FeatureFileImportAction(String type) {
            super("Import " + FeatureType.getCommonName(type) + "s from file");
            m_type = type;
        }

        public void actionPerformed(ActionEvent ev) {

            Map m = null;
            String filename = "";
            String type = "";
            String prefix = "";

            FeaturedSequence seqs[] = getSelectedSequences();
            if (seqs.length == 0) {
                UITools.showWarning("Please select a sequence.", getPrimaryPanel());
            } else {
                //Before importing genes, first remove current features--
                FeatureFilter ff = new FeatureFilter.ByType(FeatureType.GENE);
                Iterator it = seqs[0].filter(ff).features();

                while (it.hasNext()) {

                    Feature f = (Feature) it.next();
                    try {
                        seqs[0].removeFeature(f);

                    } catch (Exception bx) {
                        bx.printStackTrace();
                    }
                }

                FeatureFileSelectPanel pane = new FeatureFileSelectPanel();
                int val = JOptionPane.showConfirmDialog(DiffEditorFrame.this, pane, "Select File", JOptionPane.OK_CANCEL_OPTION);

                if (val == JOptionPane.CANCEL_OPTION)
                    return;

                type = pane.getFileType();
                filename = pane.getFilename();
                prefix = pane.getPrefix();

                if (type.equals("VGO"))
                    type = FeatureFileTools.FF_VGO;
                else if (type.equals("GENBANK"))
                    type = FeatureFileTools.FF_GENBANK;


                if (seqs.length > 0) {
                    try {
                        FeatureFileTools.setupSequence(prefix, type, filename, seqs[0]);
                    } catch (Exception ex) {
                        UITools.showWarning("<html>Could not load genes from file:<br> " + ex.getMessage(), DiffEditorFrame.this);
                        ex.printStackTrace();
                    }
                }

                refreshGeneComboBox();
            }
        }
    }


    /**
     * Action for deleting all features of a type from selected sequences
     */
    protected class RemoveFeaturesAction extends AbstractAction {
        private String m_type;

        public RemoveFeaturesAction(String ftype) {
            super("Delete " + FeatureType.getCommonName(ftype) + "s");
            m_type = ftype;
        }

        public void actionPerformed(ActionEvent ev) {
            Runnable r = new Runnable() {
                public void run() {
                    getPrimaryPanel().removeFeaturesFromSelection(m_type);
                }
            };
            try {
                UITools.invokeProgressWithMessageNoButtons(DiffEditorFrame.this, r, "Removing Genes from Marked Sequences...");
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Action for menus/buttons that invokes the load sequence method
     *
     * @author Ryan Brodie
     */
    protected class LoadAction extends AbstractAction {
        private boolean m_newWin;

        public LoadAction(boolean newWindow) {
            super("Load...", Icons.getInstance().getIcon("LOAD"));
            m_newWin = newWindow;
        }

        public void actionPerformed(ActionEvent ev) {
        }
    }



    /*
	 * This Inner class removes all the identical sequences in the panel.
	 * @author asyed
	 */
    protected class RemoveIdenticalSequencesAction extends AbstractAction {
        public void actionPerformed(ActionEvent ev) {
            //System.out.println("In similar");
            FeaturedSequence[] vSeqs = getVisibleSequences();

            if (vSeqs.length == 0) {
                UITools.showWarning("There are no sequences loaded into the panel", null);
                return;
            }
            int i = 0, j = 0;
            for (i = 0; i < vSeqs.length; i++) {
                for (j = i + 1; j < vSeqs.length; j++) {
                    String seq1 = vSeqs[i].toString();
                    String seq2 = vSeqs[j].toString();
                    if (seq1.equals(seq2)) {
                        getPrimaryPanel().removeSequence(vSeqs[j]);
                        vSeqs = getVisibleSequences();
                        j--;
                    }
                }
            }
            getPrimaryPanel().refreshEditors();
            getPrimaryPanel().updateDifferenceLists();
            getPrimaryPanel().refreshState();
        }
    }

    //author @asyed
    //Search for keywords within primers
    protected class SearchPrimerKeywordAction extends AbstractAction {

        public SearchPrimerKeywordAction() {
            super("Searching primer for keywords");
        }

        public void actionPerformed(ActionEvent ae) {
            if (getPrimaryPanel() == null) {
                return;
            }
            getPrimaryPanel().keywordSearch("PRIMER");
        }
    }


    /**
     * Action for menus/buttons that invokes the load Primer method
     * Also defines the primer Format : TAB //t
     * Column order;	Sequence Name Comment Location Melting
     *
     * @author Neil Hillen @author asyed (re-written)
     */
    protected class LoadPrimerAction extends AbstractAction {
        boolean found_primers = false;

        public void actionPerformed(ActionEvent ev) {
            FeaturedSequence[] seqs = getVisibleSequences();
            FeaturedSequence[] sSeqs = getSelectedSequences();
            if (sSeqs.length == 0) {
                UITools.showWarning("Please mark the sequences you wish to search", null);
                return;
            } else if (seqs[0].getSequenceType() == EditableSequence.AA_SEQUENCE) {
                UITools.showWarning("Primer Search is only applicable to Nucleotide sequences.", DiffEditorFrame.this);
                return;
            }
            JFileChooser fc = new JFileChooser(m_currentDirectory);
            try {
                // If the file is selected for opening and reading
                if (fc.showOpenDialog(DiffEditorFrame.this) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    String fileName = f.getAbsolutePath();
                    getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    m_status.setText("Loading primer from file " + f.toString());

                    /////////////////////////////
                    // LOADING THE PRIMER HERE //
                    /////////////////////////////
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    StringTokenizer st = null;
                    String line = null;
                    while ((line = br.readLine()) != null) {

                        String checkComment = line.trim();

                        if (checkComment.length() == 0 || checkComment.startsWith("#")) {
                            continue;
                        } else {
                            String delim = '\t' + "";
                            st = new StringTokenizer(line, delim);

                            if (st.countTokens() > 6 || st.countTokens() < 6) {
                                String e_file = "At least one of the lines in Primer File " + fileName + " is WRONGLY FORMATTED" + "\n" +
                                        "CORRECT FORMAT: Primer_Sequence(TAB)Primer_Name(TAB)Fridge_1(TAB)Fridge_2(TAB)Temperature(TAB)Comments" + "\n" +
                                        "Lines in primer file must have aforementioned Format. NOTE: Primer Sequence & Name are REQUIRED attributes." + "\n" +
                                        "Enter the value 'null' in the file wherever there is no value for other attributes.(TAB) means a tab separation.";
                                UITools.showWarning(e_file, null);
                                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                m_status.setText("Errors in Primer file...");
                                return;
                            }
                            String seq = st.nextToken();
                            String nam = st.nextToken();

                            if (seq.equals("") || nam.equals("")) {
                                UITools.showWarning("Primer file you have entered has errors either PRIMER" + "\n" + "Sequence/Name is not eneter for at least one Sequence", null);
                                m_status.setText("Errors in Primer file...");
                                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                return;
                            }

                            //THIS Checks if the First String entered in every line is a VALID Primer
                            seq = seq.toUpperCase();
                            for (int i = 0; i < seq.length(); i++) {
                                char ch = seq.charAt(i);
                                if (ch == 'A' || ch == 'G' || ch == 'C' || ch == 'T') {
                                    // DO NOTHING
                                } else {
                                    UITools.showWarning("At least ONE of the PRIMER Sequence in the FILE is not correct[not made up of A/C/G/T]" + "\n" +
                                            "CORRECT FORMAT: Primer_Sequence(TAB)Primer_Name(TAB)Fridge_1(TAB)Fridge_2(TAB)Temperature(TAB)Comments" + "\n" +
                                            "Lines in primer file must have aforementioned Format. NOTE: Primer Sequence & Name are REQUIRED." + "\n" +
                                            "Enter the value 'null' in the file wherever there is no value for other attributes.(TAB) means a tab seperation.", null);
                                    m_status.setText("Errors in Primer file...");
                                    getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                    return;
                                }
                            }
                        }
                    }
                    boolean value_p = true;
                    boolean in_excep = false;
                    int mm_percent = 0;
                    while (value_p) {
                        in_excep = false;
                        String val = JOptionPane.showInputDialog("Please enter the percentage (%) of maximum allowed mismatches\n" + "(Number Only)");
                        try {
                            mm_percent = Integer.parseInt(val);
                        } catch (NumberFormatException e) {
                            Object[] options = {"OK", "CANCEL"};
                            int proceed = JOptionPane.showOptionDialog(null, "You have entered invalid percentage value\n" + "Do you wish to re-enter the value?", "Invalid Percentage Value", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                            if (proceed == 1) {
                                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                m_status.setText("Loading Primers from file stopped...");
                                return;
                            }
                            in_excep = true;
                        }

                        if (!in_excep && value_p) {
                            if (mm_percent > 100 || mm_percent < 1) {
                                Object[] options = {"OK", "CANCEL"};
                                int proceed = JOptionPane.showOptionDialog(null, "Percentage value cannot be greater than 100 or less than 0\n" + "Do you wish to re-enter the value?", "Invalid Percentage Value", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                                if (proceed == 1) {
                                    getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                    m_status.setText("Loading Primers from file stopped...");
                                    return;
                                }
                                value_p = true;
                                in_excep = true;
                            }
                        }
                        if (!in_excep)
                            value_p = false;
                    }
                    Vector<String> v = new Vector<String>();
                    br = new BufferedReader(new FileReader(f));
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.length() != 0 && !line.startsWith("#"))
                            v.add(line);
                    }
                    primerSearchAndInsert(v, mm_percent);
                }
                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } catch (IOException e) {
                m_status.setText("Errors in Primer file...");
            }
            return;
        }
    }


    protected class ConsReportAction extends AbstractAction {
        public ConsReportAction() {
            super("Consensus Report");
        }

        public void actionPerformed(ActionEvent ev) {
            Consensus cons = ConsensusFactory.createConsensus(ConsensusFactory.IDENTITY, getVisibleSequences());

            JFrame f = new SequenceListingWindow(cons.getSequence(0, cons.getLength()), getPrimaryPanel().getDisplayStrand(), false);
            f.setVisible(true);
        }
    }

    protected class SequenceSimilarityAction extends AbstractAction {
        public SequenceSimilarityAction() {
            super("Sequence Similarity Graph");
        }

        public void actionPerformed(ActionEvent ev) {
            try {
                FeaturedSequence seqs[] = getSelectedSequences();
                if (seqs.length < 2) {
                    UITools.showWarning("You must select at least 2 sequences to construct a Sequence Similarity Graph", getPrimaryPanel());
                    return;
                }

                //SequenceSimilarityGraph.displayGraphOptions(seqs);
                PairwiseSimilarityGraph graph = new PairwiseSimilarityGraph(seqs, true);
                graph.drawPlot();
                graph.initGraph();
            } catch (IllegalArgumentException iaEx) {
                UITools.showWarning("You must select at least 2 sequences to construct a Sequence Similarity Graph", getPrimaryPanel());
            }
        }
    }

    protected class SequenceDifferencesAction extends AbstractAction {
        public SequenceDifferencesAction() {
            super("Sequence Differences Graph");
        }

        public void actionPerformed(ActionEvent ev) {
            try {
                FeaturedSequence seqs[] = getSelectedSequences();
                if (seqs.length < 2) {
                    UITools.showWarning("You must select at least 2 sequences to construct a Sequence Differences Graph", getPrimaryPanel());
                    return;
                }

                PairwiseSimilarityGraph graph = new PairwiseSimilarityGraph(seqs, false);
                graph.drawPlot();
                graph.initGraph();
                /*
                String comparisonType = "";
                switch (getPrimaryPanel().getComparisonType()) {
                    case PrimaryPanel.CONSENSUS_COMPARISON:
                        comparisonType = "Consensus Comparison";
                        break;
                    case PrimaryPanel.PAIRWISE_COMPARISON:
                        comparisonType = "Pairwise Comparison";
                        break;
                    case PrimaryPanel.MODEL_COMPARISON:
                        comparisonType = "Against top sequence";
                        break;
                }
                Frame graph = DifferencesChart.createNucleotideDifferencesChart("Differences Report ("+seqs[0].getName()+" - "+comparisonType+")", seqs[0]);
                graph.setVisible(true);
                */
            } catch (IllegalArgumentException iaEx) {
                UITools.showWarning("You must select at least 2 sequences to construct a Sequence Differences Graph", getPrimaryPanel());
            }
        }
    }

    protected class BaseContentAction extends AbstractAction {
        public BaseContentAction() {
            super("Nucleotide Content Graph");
        }

        public void actionPerformed(ActionEvent ev) {
            try {
                FeaturedSequence[] seqs = getSelectedSequences();
                if (seqs.length < 1) {
                    UITools.showWarning("You must select at least 1 sequence to construct a Nucleotide Content Graph", getPrimaryPanel());
                    return;
                }
                if (!isAlignedDNA(seqs[0].toString())) {
                    UITools.showWarning("Nucleotide Content Graph only works with nucleotide sequences", getPrimaryPanel());
                    return;
                }

                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                JCheckBox a = new JCheckBox("A");
                JCheckBox c = new JCheckBox("C");
                JCheckBox g = new JCheckBox("G");
                JCheckBox t = new JCheckBox("T");
                p.add(a);
                p.add(c);
                p.add(g);
                p.add(t);

                char[] search = new char[4];
                if (JOptionPane.showConfirmDialog(DiffEditorFrame.this, p, "Base Composition Selection", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
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

                NucleotideContentGraph graph = new NucleotideContentGraph(seqs, search);
                graph.drawPlot();
                graph.initGraph();

            } catch (IllegalArgumentException iaEx) {
                UITools.showWarning("You must select at least 1 sequence to construct a Nucleotide Content Graph", getPrimaryPanel());
            }
        }

        public boolean isAlignedDNA(String s) {
            // Note: we only require 90% ACGT, to allow for a few
            // N's or other ambiguity codes
            String nucleotides = "aAcCgGtT-";
            double threshold = 0.9;
            int count = 0;
            int slen = s.length();
            for (int i = 0; i < slen; i++) {
                if (nucleotides.indexOf(s.codePointAt(i)) != -1) {
                    count += 1;
                }
            }
            return (double) count / (double) slen > threshold;
        }
    }


    protected class PairwiseAction extends AbstractAction {
        public PairwiseAction() {
            super("Pairwise Alignment");
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel().getSelectedSequences().length < 2) {
                UITools.showWarning("You must mark at least 2 sequences to do the Parwise Alignment", null);
                return;
            }

            PairwiseAlignmentFrame paf = new PairwiseAlignmentFrame(getPrimaryPanel().getSelectedSequences());
            paf.setVisible(true);
        }
    }

    protected class TreeViewAction extends AbstractAction {
        protected String m_name;
        protected int m_type;

        public TreeViewAction(String name, int type) {
            super(name);
            m_name = name;
            m_type = type;
        }

        public void actionPerformed(ActionEvent ev) {
            try {
                String filename = getWorkFilename();
                filename = filename.substring(filename.lastIndexOf(File.separator) + 1);
                FeaturedSequence[] seqs = getVisibleSequences();
                boolean aligned = true;

                for (int i = 0; i < seqs.length; i++) {
                    if (seqs[i].length() != seqs[0].length()) {
                        aligned = false;
                    }
                }

                if (!aligned) {
                    UITools.showWarning("Sequences must be aligned in order to produce " + m_name, null);
                    return;
                }

                ArchaeopteryxTree arctree = new ArchaeopteryxTree(seqs, m_type, m_name, filename);

                // Loading BBB with sorted sequences to match the tree
                if (arctree.getConfirm() == JOptionPane.YES_OPTION) {

                    ArrayList li = arctree.getSequences();
                    ListIterator l = li.listIterator();

                    Vector v = new Vector();
                    int count = v.size();
                    while (l.hasNext()) {

                        v.add(l.next());
                        System.out.println("adding to vector size: " + v.size());
                    }
                    loadSequences((FeaturedSequence[]) v.toArray(new FeaturedSequence[0]));
                    getPrimaryPanel().refreshEditors();
                    getPrimaryPanel().refreshState();
                }
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(DiffEditorFrame.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected class FeatureNavListenerAction implements ActionListener {
        protected String m_ftype;
        protected boolean m_forward;
        protected boolean m_ignoreStrand;
        protected String m_withinGene;

        public FeatureNavListenerAction(String featureType, boolean forward, boolean ignoreStrand, String withinGene) {
            m_ftype = featureType;
            m_forward = forward;
            m_ignoreStrand = ignoreStrand;
            m_withinGene = withinGene;
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            int pos = -1;
            if (m_withinGene != "nope") {
                GenePos pair = null;
                int back = getPrimaryPanel().resolveFeaturePosition(getPrimaryPanel().getCenterPosition(), false, m_ftype, m_ignoreStrand);

                if (getPrimaryPanel().featurePair != null) {
                    if ((getPrimaryPanel().getCenterPosition() > getPrimaryPanel().featurePair.start) && (getPrimaryPanel().getCenterPosition() < getPrimaryPanel().featurePair.end)) {
                        pair = getPrimaryPanel().featurePair;
                    }
                }

                if (pair != null) {
                    if (m_withinGene == "front") {
                        pos = pair.start;
                        getPrimaryPanel().scrollToLocation(pos);
                    } else if (m_withinGene == "back") {
                        pos = pair.end - (getPrimaryPanel().getWidth() / 10);
                        getPrimaryPanel().scrollToLocation(pos);
                    }
                } else {
                    UITools.showWarning("Not within a gene", getContentPane());
                }
            } else {
                pos = getPrimaryPanel().resolveFeaturePosition(getPrimaryPanel().getPosition(), m_forward, m_ftype, m_ignoreStrand);
                if (pos == -1) {
                    UITools.showWarning("There are no more features of type " + FeatureType.getCommonName(m_ftype), getContentPane());
                } else {
                    //position is 0-based whereas scrollToLocation takes a 1-based index
                    getPrimaryPanel().scrollToLocation(pos + 1);
                }
            }

            repaint();
        }
    }

    protected class FuzzySearchAction extends AbstractAction {
        public FuzzySearchAction() {
            super("Fuzzy Motif Search");
        }

        public void actionPerformed(ActionEvent ev) {
            FeaturedSequence[] seqs = getVisibleSequences();
            FeaturedSequence[] sSeqs = getSelectedSequences();
            if (sSeqs.length == 0) {
                UITools.showWarning("Please mark the sequences you wish to search", null);
                return;
            }
            // fixes bug 381
            // enable search on amino acids
			/*			else if (seqs[0].getSequenceType() == EditableSequence.AA_SEQUENCE) {
				UITools.showWarning("Fuzzy Search is only applicable to Nucleotide sequences.", DiffEditorFrame.this);
				return;
			} */

            //if (!getPrimaryPanel().searchResultsFeatured()) {
            FuzzySearchPanel fsp = new FuzzySearchPanel();
            int val = JOptionPane.showConfirmDialog(DiffEditorFrame.this, fsp, "Fuzzy Search", JOptionPane.OK_CANCEL_OPTION);
            if (val != JOptionPane.OK_OPTION) {
                return;
            }

            final String search = fsp.getPattern();
            final int misses = fsp.getMismatches();
            if ((search == null) || search.equals("")) {
                return;
            }

            Thread t = new Thread() {
                public void run() {
                    if (!getPrimaryPanel().searchSelectedSequences(SearchTools.FUZZY, search, misses, true, false)) {
                        System.out.println("Search failed");
                    }

                    setEnabled(true);
                    getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    m_status.clear();
                }
            };

            getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
            setEnabled(false);
            m_status.setText("Fuzzy Searching for '" + search + "'...");
            t.start();
            //} else {
            //	JOptionPane.showMessageDialog(DiffEditorFrame.this, "Note: Search results currently being displayed" +
            //	". Please close before running new search.");
            //}
        }
    }

    protected class RegexSearchAction extends AbstractAction {
        public RegexSearchAction() {
            super("Reg. Expression Search");
        }

        public void actionPerformed(ActionEvent ev) {
            FeaturedSequence[] seqs = getVisibleSequences();
            FeaturedSequence[] sSeqs = getSelectedSequences();

            if (sSeqs.length == 0) {
                UITools.showWarning("Please mark the sequences you wish to search", null);

                return;
            }
            // fixes bug 381
            // enable search on amino acids
			/*			else if (seqs[0].getSequenceType() == EditableSequence.AA_SEQUENCE) {
				UITools.showWarning("Regex Search is only applicable to Nucleotide sequences.",
						DiffEditorFrame.this);

				return;
			} */


            if (getPrimaryPanel() == null) {
                return;
            }

            String getInput = JOptionPane.showInputDialog(getContentPane(), "Regular Expression");
            if (getInput == null) {
                return;
            } else {
                System.out.println("Search Input: " + getInput);
            }

            String[] tokenize = getInput.split("-");
            getInput = "";
            for (int i = 0; i < tokenize.length; i++) {
                if (!tokenize[i].equals("")) {
                    getInput += tokenize[i];
                }
            }
            System.out.println("Searching...: " + getInput);

            final String search = getInput;
            if ((search == null) || search.equals("")) {
                return;
            }

            Thread t = new Thread() {
                public void run() {
                    if (!getPrimaryPanel().searchSelectedSequences(SearchTools.REGEX, search, 0, true, false)) {
                        System.out.println("Search failed");
                    }

                    setEnabled(true);
                    m_status.clear();
                }
            };

            setEnabled(false);
            m_status.setText("Regular Expression Searching for /" + search +
                    "/...");
            t.start();
        }
    }

    protected class PrimerSearchAction extends AbstractAction {
        public PrimerSearchAction() {
            super("Primer Search");
        }

        public void actionPerformed(ActionEvent ev) {
            FeaturedSequence[] seqs = getVisibleSequences();
            FeaturedSequence[] sSeqs = getSelectedSequences();
            if (sSeqs.length == 0) {
                UITools.showWarning("Please mark the sequences you wish to search", null);
                return;
            }
            // fixes bug 381
            // enable search on amino acids
			/*			else if (seqs[0].getSequenceType() == EditableSequence.AA_SEQUENCE) {
				UITools.showWarning("Primer Search is only applicable to Nucleotide sequences.",
						DiffEditorFrame.this);
				return;
			} */

            if (!getPrimaryPanel().searchResultsFeatured()) {
                FuzzySearchPanel fsp = new FuzzySearchPanel();
                int val = JOptionPane.showConfirmDialog(DiffEditorFrame.this, fsp, "Primer Search", JOptionPane.OK_CANCEL_OPTION);

                if (val != JOptionPane.OK_OPTION) {
                    return;
                }

                final String search = fsp.getPattern();
                final int misses = fsp.getMismatches();

                if ((search == null) || search.equals("")) {
                    return;
                }

                Thread t = new Thread() {
                    public void run() {
                        if (!getPrimaryPanel().searchSelectedSequences(SearchTools.FUZZY, search, misses, true, true)) {
                            System.out.println("Search failed");
                        }

                        setEnabled(true);
                        getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        m_status.clear();
                    }
                };

                getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                setEnabled(false);
                m_status.setText("Searching for '" + search + "'...");
                t.start();
            } else {
                JOptionPane.showMessageDialog(DiffEditorFrame.this, "Note: Search arrows currently being displayed" + ". Please close before running new search.");
            }
        }
    }

    protected class ComparisonAction extends AbstractAction {
        final JRadioButtonMenuItem comp1;
        final JRadioButtonMenuItem comp2;
        final JRadioButtonMenuItem comp3;

        public ComparisonAction(JRadioButtonMenuItem c1, JRadioButtonMenuItem c2, JRadioButtonMenuItem c3) {
            comp1 = c1;
            comp2 = c2;
            comp3 = c3;
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            if (comp2.isSelected()) {
                getPrimaryPanel().setComparisonType(PrimaryPanel.CONSENSUS_COMPARISON);
            } else if (comp1.isSelected()) {
                getPrimaryPanel().setComparisonType(PrimaryPanel.PAIRWISE_COMPARISON);
            } else if (comp3.isSelected()) {
                getPrimaryPanel().setComparisonType(PrimaryPanel.MODEL_COMPARISON);
            }

            if (getPrimaryPanel().getOverviewFrame() != null) {
                if (getPrimaryPanel().getOverviewFrame().isShowing())
                    getPrimaryPanel().viewPreviewWindow();
            }
        }
    }

    protected class DisplayAreaAction extends AbstractAction {
        public DisplayAreaAction() {
            super("Set Display Area...", Icons.getInstance().getIcon("SETDISPLAY"));
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            DisplayAreaPanel p = new DisplayAreaPanel(getPrimaryPanel().getDisplayStart(), getPrimaryPanel().getDisplayStop());
            int val = JOptionPane.showConfirmDialog(null, p, "Display Area", JOptionPane.OK_CANCEL_OPTION);

            if (val == JOptionPane.OK_OPTION) {
                int start = 0;
                int stop = Integer.MAX_VALUE;

                try {
                    start = (p.getLeftValue() == -1) ? 0 : p.getLeftValue();
                    stop = (p.getRightValue() == -1) ? Integer.MAX_VALUE : p.getRightValue();
                } catch (NumberFormatException ex) {
                    UITools.showError("Error parsing entered value, please try again", getContentPane());

                    return;
                }

                boolean valid = true;

                if (start >= stop) {
                    valid = false;
                }

                if ((start < 0) || (stop <= 0)) {
                    valid = false;
                }

                if (valid) {
                    getPrimaryPanel().setDisplayArea(p.getLeftValue(), p.getRightValue());
                    refreshGeneComboBox();
                } else {
                    String startString = (start == 0) ? "Endpoint" : (start + "");
                    String stopString = (stop == Integer.MAX_VALUE) ? "Endpoint" : (stop + "");
                    String msg = "<html>The range [" + startString + ", " + stopString +
                            "] is not a<br>" +
                            "valid display range.  Please select another set of<br>" +
                            "values and try again.</html>";
                    UITools.showWarning(msg, getContentPane());
                }
            }
        }
    }

    protected class ShowSequencesAction extends AbstractAction {
        public ShowSequencesAction() {
            super("Show/Hide Sequences...");
        }

        public void actionPerformed(ActionEvent ev) {
            ShowSequencesDialog f = new ShowSequencesDialog(DiffEditorFrame.this);
            f.setSize(new Dimension(800, 300));
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        }
    }

    protected class SetColorSchemeAction extends AbstractAction {
        protected int colorScheme;

        public SetColorSchemeAction(int cs) {
            colorScheme = cs;
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            AbstractButton b = (AbstractButton) ev.getSource();

            if (!b.isSelected()) {
                return;
            }

            getPrimaryPanel().setColorScheme(colorScheme);
        }
    }

    protected class SetDifferenceColorsAction extends AbstractAction {
        protected int colorScheme;

        public SetDifferenceColorsAction(int cs) {
            colorScheme = cs;
        }

        public void actionPerformed(ActionEvent ev) {

            if (getPrimaryPanel() == null) {
                return;
            }

            AbstractButton b = (AbstractButton) ev.getSource();

            if (!b.isSelected()) {
                return;
            }

            getPrimaryPanel().setDifferenceColors(colorScheme);
        }
    }

    protected class InfoAction extends AbstractAction {
        public InfoAction() {
            super("Percent Identity Table");
        }

        public void actionPerformed(ActionEvent ev) {
            final int start = getPrimaryPanel().getDisplayStart();
            final int stop = getPrimaryPanel().getDisplayStop();

            AlignmentInfoPanel aip = new AlignmentInfoPanel(getVisibleSequences(), start, stop);
            aip.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            final AlignmentInfoPanel fAip = aip;

            final JDialog diag = new JDialog(DiffEditorFrame.this, "Alignment Info", false);

            JPanel p = new JPanel(new BorderLayout());

            JButton close = new JButton("Close");
            close.setMnemonic(MenuKeyEvent.VK_C);
            close.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    diag.dispose();
                }
            });

            ActionListener saveActionListener = new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    fAip.saveFlatFile();
                }
            };

            JMenuBar mbar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem saveItem = new JMenuItem("Save to tab delimited file");
            saveItem.addActionListener(saveActionListener);
            fileMenu.add(saveItem);
            mbar.add(fileMenu);

            diag.setJMenuBar(mbar);

            JPanel btn = new JPanel();
            btn.setLayout(new BoxLayout(btn, BoxLayout.X_AXIS));
            btn.add(Box.createHorizontalGlue());
            btn.add(close);
            btn.add(Box.createHorizontalGlue());
            btn.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            p.add(aip, BorderLayout.CENTER);
            p.add(btn, BorderLayout.SOUTH);

            diag.setContentPane(p);
            diag.pack();

            if (diag.getWidth() > 800) {
                diag.setSize(800, diag.getHeight());
            }

            if (diag.getHeight() > 600) {
                diag.setSize(diag.getWidth(), 600);
            }

            // Position the dialog window
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (dim.width - diag.getWidth()) / 2;
            int y = (dim.height - diag.getHeight()) / 2;
            diag.setLocation(x, y);

            diag.setVisible(true);
        }
    }

    protected class PrimerNearAction extends AbstractAction {
        public PrimerNearAction() {
            super("Surrounding Primers");
        }

        public void actionPerformed(ActionEvent ev) {
            primerRange = '0';
            selectedGeneIndex = '0';


            final JDialog diag = new JDialog(DiffEditorFrame.this, true);
            diag.setTitle("Surrounding Primers");

            JPanel p = new JPanel(new BorderLayout());

            //////InputBox
            final JTextField inputDistance = new JTextField();
            inputDistance.setColumns(15);
            p.add(inputDistance, BorderLayout.EAST);

            ///// list of genes
            final DefaultComboBoxModel geneSelectBox = new DefaultComboBoxModel();

            FeaturedSequence[] seqs = getPrimaryPanel().getSelectedSequences();
            FeaturedSequence seq = seqs[0];

            FeatureFilter ff = new FeatureFilter.ByType(FeatureType.GENE);
            //get the filtered Features
            final FeatureHolder fh = seq.filter(ff);
            //iterate over the Features in fh
            for (Iterator i = fh.features(); i.hasNext(); ) {
                Feature feat = (Feature) i.next();
                geneSelectBox.addElement(feat.getAnnotation().getProperty(AnnotationKeys.NAME));
            }

            final JComboBox geneBox = new JComboBox(geneSelectBox);

            geneBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (getPrimaryPanel() == null) {
                        return;
                    }

                    int posn = getPrimaryPanel().resolveGenePosition(geneBox.getSelectedItem().toString());

                    if (posn >= 0) {
                        getPrimaryPanel().scrollToLocation(posn);
                    }
                }
            });

            p.add(geneBox, BorderLayout.NORTH); ///\

            //////JTable
            JTable foundPrimer = new JTable();
            p.add(foundPrimer, BorderLayout.CENTER);

            ////// close button
            JButton close = new JButton("Close");
            close.setMnemonic(MenuKeyEvent.VK_C);
            close.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    diag.dispose();
                }
            });
            final JButton update = new JButton("Update");
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    try {
                        primerRange = Integer.parseInt(inputDistance.getText());
                    } catch (NumberFormatException nfe) {
                        inputDistance.setText("0");
                    }
                    selectedGeneIndex = geneBox.getSelectedIndex();

                    //get start and stop of all positions
                    int startPosn = getPrimaryPanel().resolveGenePosition(geneBox.getSelectedItem().toString());
                    int stopPosn = getPrimaryPanel().resolveGeneEnd(geneBox.getSelectedItem().toString());
                    //include the ranges
                    int rangeStart = startPosn - primerRange;
                    int rangeStop = stopPosn + primerRange;

                    getPrimaryPanel().setDisplayArea(rangeStart, rangeStop);

                }
            });

            JPanel btn = new JPanel();
            btn.setLayout(new BoxLayout(btn, BoxLayout.X_AXIS));
            btn.add(update);
            btn.add(Box.createHorizontalGlue());
            btn.add(close);
            btn.add(Box.createHorizontalGlue());
            btn.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            p.add(btn, BorderLayout.SOUTH);

            diag.setContentPane(p);
            diag.pack();
            diag.setLocationRelativeTo(null);
            diag.setVisible(true);

			/*you now have a list of primers -- you can get the selected by its index - and get the values for start and stop.
			 * then calclate a range- option or restricting view to this range?
			 * draw the table*/
        }
    }

    protected class SaveAsAction extends AbstractAction {
        public SaveAsAction() {
            super("Save As...", Icons.getInstance().getIcon("SAVE"));
        }

        public void actionPerformed(ActionEvent ev) {
			/*Thread t =
				new Thread("Save-As-Thread") {
				public void run()
				{        */
            saveSequences();
				/*}
			};

			t.start();   */
        }
    }

    protected class SavePrimerAction implements ActionListener {
        public SavePrimerAction() {}

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            getPrimaryPanel().savePrimersToFile();
        }
    }


    protected class OpenSelectedRegionsBBBAction extends AbstractAction {
        public OpenSelectedRegionsBBBAction() {

        }

        public void actionPerformed(ActionEvent e) {
            FeaturedSequence[] sequences = getPrimaryPanel().getSelectedSegments(false);

            if (sequences == null || sequences.length == 0) {
                UITools.showWarning("Please mark one or more sequences to work with.", null);
            } else {
                openSequencesInNewBBB(sequences);
            }
        }

    }


    protected class ExportSelectedRegionsBBBAction extends AbstractAction {
        public ExportSelectedRegionsBBBAction() {
        }

        public void actionPerformed(ActionEvent e) {
            exportSelectedRegionsBBB();

            /*
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() { exportSelectedRegionsBBB(); }
            });
            t.setPriority(Thread.MIN_PRIORITY);
			t.start();
			*/
        }

        private void exportSelectedRegionsBBB() {
            int[] selected_seqs = getPrimaryPanel().getSelectedIndices();

            if (selected_seqs == null || selected_seqs.length == 0) {
                UITools.showError("No Sequences Selected", null);
            }

            JFileChooser fc = new JFileChooser(m_currentDirectory);
            fc.setDialogTitle("Save Selected Data");
            MultiFileFilter ff = new MultiFileFilter("BBB Format (XML)");
            ff.addExtension("bbb");
            fc.addChoosableFileFilter(ff);
            fc.setFileFilter(ff);
            String filename = "";

            try {
                if (fc.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    filename = f.getAbsolutePath();
                    if (!ff.accept(f)) {
                        if (!filename.endsWith("bbb")) {
                            filename += ".bbb";
                        }
                    }

                    File outfile = new File(filename);
                    if (outfile.exists()) {
                        if (!UITools.showYesNo(filename + " exists.  Replace it?", getPrimaryPanel())) {
                            return;
                        }
                    }

                    getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    m_status.setText("Saving File: " + f.toString());
                    FeaturedSequenceWriter out = FeaturedSequenceWriter.createFeaturedSequenceWriter(filename, notebook);
                    ArrayList<FeaturedSequence> l = new ArrayList<FeaturedSequence>();
                    FeaturedSequence[] seqs = getPrimaryPanel().getSelectedSegments(true);

                    for (int i = 0; i < selected_seqs.length; i++) {
                        l.add(seqs[selected_seqs[i]]);
                    }

                    out.writeSequences(l.listIterator());
                    setWorkFilename(filename);
                    UndoHandler.getInstance().reset();
                    UndoHandler.getInstance().setModified(false);

                    UITools.invoke(new Runnable() {
                        public void run() {
                            getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                            m_status.clear();
                        }
                    });
                }

            } catch (IOException ex) {
                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                m_status.setText("Error Saving File");
                ex.printStackTrace();
            } catch (Exception iex) {
                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }

    }


    protected class ExportSelectedRegionsFASTAAction extends AbstractAction {
        public ExportSelectedRegionsFASTAAction() {
        }

        public void actionPerformed(ActionEvent e) {
            exportSelectedRegionsFASTA();

            /*
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() { exportSelectedRegionsFASTA(); }
            });
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
            */
        }

        private void exportSelectedRegionsFASTA() {
            int[] selected_seqs = getPrimaryPanel().getSelectedIndices();

            if (selected_seqs == null || selected_seqs.length == 0) {
                UITools.showError("No Sequences Selected", null);
                return;
            }

            JFileChooser fc = new JFileChooser(m_currentDirectory);
            fc.setDialogTitle("Save Selected Data");
            MultiFileFilter ff = new MultiFileFilter("Fasta Format)");
            ff.addExtension("fasta");
            fc.addChoosableFileFilter(ff);
            fc.setFileFilter(ff);
            String filename = "";

            try {
                if (fc.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    filename = f.getAbsolutePath();
                    if (!ff.accept(f)) {
                        if (!filename.endsWith("fasta")) {
                            filename += ".fasta";
                        }
                    }

                    File outfile = new File(filename);
                    if (outfile.exists()) {
                        if (!UITools.showYesNo(filename + " exists.  Replace it?", getPrimaryPanel())) {
                            return;
                        }
                    }

                    getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    m_status.setText("Saving File: " + f.toString());
                    FeaturedSequenceWriter out = FeaturedSequenceWriter.createFeaturedSequenceWriter(filename, notebook);
                    ArrayList<FeaturedSequence> l = new ArrayList<FeaturedSequence>();
                    FeaturedSequence[] seqs = getPrimaryPanel().getSelectedSegments(true);

                    for (int i = 0; i < selected_seqs.length; i++) {
                        l.add(seqs[selected_seqs[i]]);
                    }

                    out.writeSequences(l.listIterator());
                    setWorkFilename(filename);
                    UndoHandler.getInstance().reset();
                    UndoHandler.getInstance().setModified(false);

                    UITools.invoke(new Runnable() {
                        public void run() {
                            getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                            m_status.clear();
                        }
                    });
                }

            } catch (IOException ex) {
                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                m_status.setText("Error Saving File");
                ex.printStackTrace();
            } catch (Exception iex) {
                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    protected class DeleteGapsAndExportAction extends AbstractAction {
        public DeleteGapsAndExportAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            Thread t = new Thread("Delete Columns Containing Gap(s) and Export") {
                public void run() {
                    //getPrimaryPanel().exportGapDeletedSeqs();

                    //Get all visible sequences
                    FeaturedSequence[] seqs = getSequences();
                    FeaturedSequence[] clones = new FeaturedSequence[seqs.length];

                    //Store original sequences into clone variable
                    for (int i = 0; i < seqs.length; ++i) {
                        clones[i] = (FeaturedSequence) seqs[i].clone();
                    }

                    //Delete all gap columns and open changed sequences in new BBB instance
                    ca.virology.lib.io.tools.SequenceTools.deleteGaps(clones, true);
                    openSequencesInNewBBB(clones);
                }
            };
            t.start();
        }
    }

    protected class DeleteSpecifiedColumnsAction extends AbstractAction {
        public DeleteSpecifiedColumnsAction() {

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Thread t = new Thread("Delete specified columns and export") {
                public void run() {
                    FeaturedSequence[] seqs = getSequences();
                    FeaturedSequence[] clones = new FeaturedSequence[seqs.length];
                    // store original sequences into clone variable
                    for (int i = 0; i < seqs.length; i++) {
                        clones[i] = (FeaturedSequence) seqs[i].clone();
                    }

                    // create dialog window
                    DeleteColumnsDialog dcd = new DeleteColumnsDialog(seqs);
                    dcd.setMinimumSize(new Dimension(250, 250));
                    dcd.setMaximumSize(new Dimension(1000, 1000));
                    dcd.setVisible(true);

                    List<Pair<Integer, Integer>> deleteIndices = dcd.getDeleteIndices();
                    if (deleteIndices != null) {
                        Integer adjustment = new Integer(0);
                        for (Pair<Integer, Integer> p : deleteIndices) {
                            SequenceTools.deleteSection(clones, p.getKey() - adjustment, p.getValue() - adjustment);
                            adjustment += (p.getValue() - p.getKey() + 1);
                        }
                        openSequencesInNewBBB(clones);
                    }
                }
            };
            t.start();
        }
    }


    protected class ExportImageAction extends AbstractAction {
        public ExportImageAction() {
            super("Export Alignment Image...");
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            FeaturedSequence[] seqs = getPrimaryPanel().getSequences();

            int ml = 0;

            for (int i = 0; i < seqs.length; ++i) {
                if (seqs[i].length() > ml) {
                    ml = seqs[i].length();
                }
            }

            final ActionEvent ev1 = ev;

            final ExportImagePanel eip = new ExportImagePanel(0.0, ml);
            final int ch = JOptionPane.showConfirmDialog(null, eip, "Export Image", JOptionPane.OK_CANCEL_OPTION);

            int start1 = eip.getStart();
            int stop1 = eip.getStop();

            if (stop1 - start1 > 5000) {
                UITool.showInfoMessage("Sequence too long! Please make sure your range does not exceed 5000 nucleotides", null);
                return;
            }

            Runnable runner = new Runnable() {
                public void run() {

                    if (ch == JOptionPane.OK_OPTION) {
                        int start = 0;
                        int stop = 0;

                        try {
                            start = eip.getStart();
                            stop = eip.getStop();
                        } catch (NumberFormatException ex) {
                            UITools.showWarning("Please enter a start and stop position to export", null);
                            actionPerformed(ev1);
                        }

                        double width = eip.getImageWidth();
                        double space = eip.getSpacing();
                        final String imageType = eip.getImageType();

                        if ((stop <= start) || (stop == 0)) {
                            UITools.showWarning("Please enter a start and stop position to export", null);
                            actionPerformed(ev1);
                        }


                        final BufferedImage i = getPrimaryPanel().exportWrapped(start, stop, width, space, eip.getScalingFactor());

                        JFileChooser fc = new JFileChooser(m_currentDirectory);
                        fc.setDialogTitle("Export Alignment to Image");
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {

                        }
                        if (fc.showDialog(DiffEditorFrame.this, "Save") == JFileChooser.APPROVE_OPTION) {
                            final StringBuffer s = new StringBuffer(fc.getSelectedFile().getAbsolutePath());

                            if (s.toString().equals("")) {
                                return;
                            }

                            if (!s.toString().endsWith(imageType)) {
                                s.append(((s.toString().endsWith(".")) ? "" : ".") + imageType);
                            }
                            try {
                                long t0 = System.currentTimeMillis();
                                FileOutputStream fos = new FileOutputStream(s.toString());
                                if (imageType.equals("jpg")) {
                                    try {
                                        Iterator writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
                                        ImageWriter writer = (ImageWriter) writers.next();

                                        ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
                                        writer.setOutput(ios);

                                        ImageWriteParam param = writer.getDefaultWriteParam();
                                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                        param.setCompressionQuality(1.0f);

                                        IIOImage iioi = new IIOImage(i, null, null);
                                        writer.write(null, iioi, param);
                                        ios.close();
                                    } catch (Exception ex) {

                                    }
                                } else {
                                    ImageIO.write(i, imageType, fos);
                                }
                                fos.close();
                                long t1 = System.currentTimeMillis();
                                System.out.println("Saving took " + (t1 - t0) + "ms");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                }
            };
            UITools.invokeProgressWithMessageNoButtons(null, runner, "Creating image..");
        }
    }

    /**
     * This action listener loads a file from the disk, creates a GenericInput based
     * on its contents and then adds that GI source to the sequence panel
     *
     * @author Sangeeta Neti
     */
    protected class ImportAnalysisFileAction extends AbstractAction {

        public ImportAnalysisFileAction() {
            super("Import Analysis File...");
        }

        public void actionPerformed(ActionEvent ev) {

            FeaturedSequence seqs[] = getSelectedSequences();

            if (seqs.length == 0) {
                UITools.showWarning("Please select a sequence.", getPrimaryPanel());
            } else if (seqs.length > 1) {
                UITools.showWarning("You have selected more than one sequence. Please select one sequence.", DiffEditorFrame.this);
            } else {
                file = new FileChooser().openFile("", file);

                if (file != null) {

                    GenericInput gi = null;

                    try {

                        gi = new GenericInputFile(file);

                    } catch (java.io.IOException ex) {

                        ex.printStackTrace();
                        UITools.showError("Error opening " + file.toString() + ": " + ex.getMessage(), null);
                        return;
                    }
                    getPrimaryPanel().addAnalysisResultToSelection(gi);

                }
            }
        }
    }

    protected class ExportSelectionAction extends AbstractAction {
        protected boolean m_all;

        public ExportSelectionAction(boolean all) {
            super(all ? "Export Selection (All Sequences)..." : "Export Selection (Marked Sequences)...");

            m_all = all;
        }

        public void actionPerformed(ActionEvent ev) {
            //			Thread t =
            //				new Thread("export-Thread") {
            //				public void run()
            //				{
            exportSequences(m_all);
            //				}
            //			};
            //
            //			t.start();
        }
    }

    /*#######################################################################>>>>>
    * Purpose:  Action for menus/buttons that invokes the save sequences method.
    * Written: ??? Ryan Brodie
    * Edited: Summer 2015
    * #######################################################################>>>>>*/
    protected class SaveAction extends AbstractAction {
        public SaveAction() {
            super("Save", Icons.getInstance().getIcon("SAVE"));
        }

        public void actionPerformed(ActionEvent ev) {
            if (getWorkFilename().equals("New File")) {
                saveSequences();
                return;
            }

            try {
                notebook = nb_frame.getText();
            } catch (NullPointerException e) {
            }

            if (isSavable()) {
                saveSequences(m_workFilename);
            } else {
                saveSequences();
            }

            /*Thread t = new Thread("Save-Thread") {
                public void run() {
                    //The above code was in here...
				}
			};
			t.start();*/
        }
    }

    protected class CloseAlignmentAction extends AbstractAction {
        public CloseAlignmentAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            closeAllSequences();
        }
    }

    protected class QuitAction extends AbstractAction {
        public QuitAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            close();
        }
    }

    protected class UndoAction extends AbstractAction {
        public UndoAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            if (!getPrimaryPanel().undo()) {
                UITools.showWarning("An error occured while trying to undo.", DiffEditorFrame.this);
                System.out.println("Could not undo");
            }
        }
    }

    protected class RedoAction extends AbstractAction {
        public RedoAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            if (!getPrimaryPanel().redo()) {
                UITools.showWarning("An error occured while trying to redo.", DiffEditorFrame.this);
                System.out.println("Could not redo");
            }
        }
    }

    /**
     * Action for menu that invokes the 'Edit Sequence' method
     *
     * @author Sangeeta Neti
     */
    protected class EditSequenceNamesAction extends AbstractAction {
        public EditSequenceNamesAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            boolean result = getPrimaryPanel().EditSequenceNames();

            if (result) {
                if (isSavable()) {
                    saveSequences(m_workFilename);
                } else {
                    saveSequences();
                }
            }

        }
    }

    protected class MarkAllAction extends AbstractAction {
        public MarkAllAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            getPrimaryPanel().selectAll();
        }
    }

    protected class UnmarkSequencesAction extends AbstractAction {
        public UnmarkSequencesAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }
            getPrimaryPanel().deselectAll();
        }
    }

    protected class SelectWholeSequenceAction extends AbstractAction {
        public SelectWholeSequenceAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            getPrimaryPanel().selectAllData();
        }
    }

    /**
     * Action for menus/buttons that invokes the 'select region' method
     *
     * @author Ryan Brodie
     */
    protected class SelectRegionAction extends AbstractAction {
        public SelectRegionAction() {
            super("Select Region...");
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }


            SelectRegionPanel gtp = new SelectRegionPanel(getVisibleSequences());

            int[] seq = getSelectedIndices();
            if (seq.length == 0 || seq == null) {
                JOptionPane.showMessageDialog(DiffEditorFrame.this, "Please select sequences you wish to observe.");
                return;
            }


            JOptionPane pane = new JOptionPane(gtp, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            JDialog dialog = pane.createDialog(DiffEditorFrame.this, "Select Range");

            dialog.pack();
            dialog.setMinimumSize(dialog.getMinimumSize());
            dialog.setResizable(true);
            dialog.setVisible(true);
            dialog.dispose();

            if (Integer.parseInt(pane.getValue().toString()) == JOptionPane.YES_OPTION) {
                //System.out.println("Selecting options: " + gtp.getSequence());
                try {
                    getPrimaryPanel().selectRegion(gtp.getSequence(), gtp.getStartPosition(), gtp.getStopPosition(), gtp.isGappedSelected(), gtp.isPropogateSelected(), seq);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    protected class RemoveSelectedRegionAction extends AbstractAction {
        public RemoveSelectedRegionAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }
            getPrimaryPanel().removeSelectedRegion();
        }
    }

    /*
    * This Inner class removes unselected sequence regions from the sequence(s).
    * @author asyed
    */
    protected class RemoveAllButSelectedRegionAction extends AbstractAction {
        public void actionPerformed(ActionEvent ae) {
            FeaturedSequence[] sSeqs = getSelectedSequences();
            if (sSeqs.length == 0) {
                UITools.showWarning("Select at least one sequence before performing this action", null);
                return;
            }
            getPrimaryPanel().removeAllButSelectedRegion();
            getPrimaryPanel().refreshEditors();
            getPrimaryPanel().updateDifferenceLists();
            getPrimaryPanel().refreshState();
        }
    }


    protected class RemoveGapsFromSelectedRegionAction extends AbstractAction {
        public RemoveGapsFromSelectedRegionAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }
            getPrimaryPanel().removeGapsFromSelectedRegion();
        }
    }

    protected class RemoveAllGapColumnsAction extends AbstractAction {
        public RemoveAllGapColumnsAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }
            getPrimaryPanel().removeAllGapColumns();
        }
    }

    protected class InsertGapsAction extends AbstractAction {
        public InsertGapsAction() {
            super("Insert Gaps...");
        }

        public void actionPerformed(ActionEvent ev) {
            FeaturedSequence[] seqs = getPrimaryPanel().getSelectedSequences();
            if (seqs.length <= 0) {
                UITools.showWarning("Please mark one or more sequences to work with", DiffEditorFrame.this);
                return;
            }

            String s = JOptionPane.showInputDialog(DiffEditorFrame.this, "How many gaps to insert?", "Insert Gaps", JOptionPane.QUESTION_MESSAGE);
            if (s != null && !s.equals("")) {
                int n = 0;
                try {
                    n = Integer.parseInt(s);
                } catch (NumberFormatException ex) {
                    UITools.showWarning("Please Enter an Integer", DiffEditorFrame.this);
                    actionPerformed(ev);
                }

                if (n >= 0) {
                    getPrimaryPanel().insertGaps(n);
                }
            }
        }
    }

    protected class InsertSeqAction extends AbstractAction {
        public InsertSeqAction() {

        }

        public void actionPerformed(ActionEvent e) {
            if (getPrimaryPanel() == null) {
                return;
            }
            getPrimaryPanel().insertSeqBeforeSelection();
        }
    }


    protected class NoteBookFrameAction extends AbstractAction {
        public NoteBookFrameAction() {

        }

        public void actionPerformed(ActionEvent ev) {

            if (getPrimaryPanel() == null) {
                return;
            }

            if (nb_frame == null) {
                nb_frame = new NotebookFrame(getPrimaryPanel().getSequences(), notebook);
            } else {
                nb_frame.setVisible(true);
            }

        }
    }



    /**
     * Action for menu that invokes the 'strip invalid characters' method
     *
     * @author Sangeeta Neti
     */
    protected class StripInvalidCharactersAction extends AbstractAction {
        public StripInvalidCharactersAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            getPrimaryPanel().stripInvalidCharacters();
        }
    }

    /**
     * Action for menus/buttons that invokes the help method
     *
     * @author Ryan Brodie
     */
    protected class HelpAction extends AbstractAction {
        public HelpAction() {
            super("Help", Icons.getInstance().getIcon("HELP"));
        }

        public void actionPerformed(ActionEvent ev) {
            String url = "https://4virology.net/virology-ca-tools/base-by-base/";

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(new URI(url));
                } catch (Exception e) {
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




			/*try {
				java.net.URL helpURL =
					new java.net.URL(
					"http://206.12.59.223/virology-ca-tools/base-by-base/");
				UITools.displayURL(helpURL, true);
			} catch (Exception ex) {
				ex.printStackTrace();
				UITools.showError(
						"Error displaying help: " + ex.getMessage(),
						getContentPane());
			}*/
        }
    }

    /**
     * Action for menus/buttons that invokes the 'go to position' method
     *
     * @author Ryan Brodie
     */
    protected class GoToAction extends AbstractAction {
        public GoToAction() {
            super("Go To Location...");
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            GoToLocationPanel gtp = new GoToLocationPanel(getVisibleSequences());

            int value = JOptionPane.showConfirmDialog(DiffEditorFrame.this, gtp, "Go To Position", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (value == JOptionPane.OK_OPTION) {
                try {
                    getPrimaryPanel().scrollToLocation(gtp.getSequence(), gtp.getPosition(), gtp.isGappedSelected());
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Action for menus/buttons that adds a feature to the given selection
     *
     * @author Ryan Brodie
     */
    protected class AddEventFeatureAction extends AbstractAction {
        String m_type;

        public AddEventFeatureAction(String type) {
            super(type);

            m_type = type;
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            getPrimaryPanel().addFeatureToSelection(m_type);
        }
    }

    /**
     * Action for opening the event breakdown window (treeview)
     *
     * @author Ryan Brodie
     */
    protected class EventBreakdownAction extends AbstractAction {
        public EventBreakdownAction() {
            super("View Event Breakdown");
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            FeaturedSequence[] seqs = null;

            if (getVisibleSequences().length > 2) {
                seqs = getPrimaryPanel().getSelectedSequences();

                if (seqs.length != 2) {
                    UITools.showWarning("Please mark 2 sequences to create the event breakdown", getContentPane());
                    return;
                }
            } else {
                seqs = getVisibleSequences();
            }
            final int start = getPrimaryPanel().getDisplayStart();
            final int stop = getPrimaryPanel().getDisplayStop();

            final FeaturedSequence[] fseqs = seqs;
            Runnable r = new Runnable() {
                public void run() {
                    JFrame f = new JFrame("Event Breakdown Report");
                    f.setName(getName() + "::EvtBrkdwnRpt(" + f.getName() + ")");
                    f.setContentPane(new EventBreakdownPanel(fseqs[0], fseqs[1], start, stop));
                    f.pack();

                    Rectangle r = f.getBounds();
                    Dimension screen = UITools.getScreenSize();
                    if (r.height > screen.height) {
                        r.setRect(r.x, 0, r.width, screen.height);
                        f.setBounds(r);
                    }
                    int x = (screen.width - f.getWidth()) / 2;
                    int y = (screen.height - f.getHeight()) / 2;
                    f.setLocation(x, y);
                    f.setVisible(true);
                }
            };

            UITools.invokeProgressWithMessageNoButtons(DiffEditorFrame.this, r, "Processing Event Breakdown...");
        }
    }

    protected class GapCountAction extends AbstractAction {
        public GapCountAction() {
            super("View Gap Count");
        }

        public void actionPerformed(ActionEvent evt) {
            final JDialog countWindow = new JDialog((java.awt.Frame) null, "Gap Count");
            countWindow.setLocationRelativeTo(null);
            BoxLayout outerLayout = new BoxLayout(countWindow.getContentPane(), BoxLayout.Y_AXIS);
            countWindow.getContentPane().setLayout(outerLayout);

            JPanel countsPanel = new JPanel();
            GridBagLayout layout = new GridBagLayout();
            countsPanel.setLayout(layout);

            JScrollPane sp = new JScrollPane(countsPanel);
            Border line = BorderFactory.createLineBorder(Color.black);
            sp.setViewportBorder(BorderFactory.createTitledBorder(line, "Sequences"));
            countWindow.getContentPane().add(sp);

            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.BOTH;
            gc.weightx = 0;
            gc.gridx = 0;
            gc.gridy = 0;
            gc.ipadx = 5;
            gc.ipady = 5;

            java.util.List counts = DiffEditor.frame.m_dataPanel.getGapCounts();
            for (Iterator it = counts.iterator(); it.hasNext(); ) {
                PrimaryPanel.SequenceGapCount sgc = (PrimaryPanel.SequenceGapCount) it.next();
                gc.gridx = 0;
                JLabel label = new JLabel(sgc.sequenceName + ": ");
                layout.setConstraints(label, gc);
                countsPanel.add(label);
                gc.gridx = 1;
                label = new JLabel(Integer.toString(sgc.gapCount));
                layout.setConstraints(label, gc);
                countsPanel.add(label);
                gc.gridy++;
            }

            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
            buttonPane.add(Box.createHorizontalGlue());
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    countWindow.dispose();
                }
            });
            buttonPane.add(closeButton);
            countWindow.getContentPane().add(buttonPane);

            countWindow.pack();
            countWindow.setLocationRelativeTo(null);
            countWindow.setVisible(true);
        }
    }

    /**
     * Action for menus/buttons that invokes the window
     *
     * @author Ryan Brodie
     */
    public static FeaturedSequence[] cdsSeqs;

    protected class GeneStatsAction extends AbstractAction {
        public GeneStatsAction() {
            super("View CDS Statistics");
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            cdsSeqs = getPrimaryPanel().getSelectedSequences();

            if (cdsSeqs.length != 2) {
                UITools.showWarning("Please mark 2 sequences to create CDS statistics for", getContentPane());

                return;
            }

            final int start = getPrimaryPanel().getDisplayStart();
            final int stop = getPrimaryPanel().getDisplayStop();

            Runnable r = new Runnable() {
                public void run() {
                    try {
                        JFrame f = new CdsStatsFrame(cdsSeqs[0], cdsSeqs[1], start, stop);
                        f.setName(getName() + "::CDSStatsFrame(" + f.getName() + ")");
                        f.pack();
                        Dimension d = UITools.getScreenSize();
                        f.setSize(d.width, d.height - 150);
                        f.setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };


            UITools.invokeProgressWithMessageNoButtons(DiffEditorFrame.this, r, "Processing CDS Statistics...");
        }
    }

    /**
     * Action for Multi Genome Comparison Feature
     */
    protected class MGCStatsAction extends AbstractAction {
        public MGCStatsAction() {
            super("View Multi Genome Comparison Statistics");
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            FeaturedSequence[] seqs = getPrimaryPanel().getSelectedSequences();
            if (seqs.length >= 2) {

                final int start = getPrimaryPanel().getDisplayStart();
                final int stop = getPrimaryPanel().getDisplayStop();

                final FeaturedSequence[] fseqs = seqs;
                Runnable r = new Runnable() {
                    public void run() {
                        try {
                            MgcStatsFrame f = new MgcStatsFrame(fseqs, start, stop);
                            f.setName(getName() + "::MgcStatsFrame(" +
                                    f.getName() + ")");
                            f.pack();

                            Dimension d = UITools.getScreenSize();
                            f.setSize(d.width, d.height - 150);
                            if (!f.isCancelled())
                                f.setVisible(true);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                };

                UITools.invokeProgressWithMessageNoButtons(DiffEditorFrame.this, r, "Processing MGC Statistics...");
            } else {
                UITools.showWarning("Please mark 2 or more sequences to create MGC statistics for", getContentPane());

                return;
            }

        }
    }


    /**
     * Action for Multi Genome Comparison Feature
     */
    protected class PrimerReportAction extends AbstractAction {
        public PrimerReportAction() {
            super("View Primer Report");
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            FeaturedSequence[] seqs = null;
            if (getSelectedSequences().length > 0) {
                seqs = getPrimaryPanel().getSelectedSequences();
            } else {
                seqs = getVisibleSequences();
            }

            final int start = getPrimaryPanel().getDisplayStart();
            final int stop = getPrimaryPanel().getDisplayStop();

            final FeaturedSequence[] fseqs = seqs;
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        JFrame f = new PrimerReportFrame(fseqs, start, stop);
                        f.setName(getName() + "::PrimerReportFrame(" +
                                f.getName() + ")");
                        f.pack();

                        Dimension d = UITools.getScreenSize();
                        f.setSize(d.width, d.height - 150);
                        f.setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };

            UITools.invokeProgressWithMessageNoButtons(DiffEditorFrame.this, r, "Processing Primer Report");
        }
    }

    /**
     * Action for opening the 'Edit Preferences' dialog for modifying the user
     * settings for BBB
     *
     * @author Ryan Brodie
     */
    protected class PreferencesAction extends AbstractAction {
        public PreferencesAction() {
            super("Preferences...", Icons.getInstance().getIcon("PREFS"));
        }

        public void actionPerformed(ActionEvent ev) {
            EditSettingsDialog settingsDialog = new EditSettingsDialog(getPrimaryPanel());
            settingsDialog.addCommitListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    getPrimaryPanel().refreshState();
                }
            });
            settingsDialog.setVisible(true);
        }
    }

    /**
     * Action for printing in BBB
     *
     * @author Ryan Brodie
     */
    protected class PrintAction extends AbstractAction {
        boolean m_sel;

        public PrintAction(boolean printSelection) {
            super("Print " + (printSelection ? "Selection" : "View"));

            m_sel = printSelection;
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            final PrinterJob pjob = PrinterJob.getPrinterJob();
            final PageFormat pf = pjob.defaultPage();
            getPrimaryPanel().setPrintSelection(m_sel);
            pjob.setPrintable(getPrimaryPanel(), pf);

            Thread t = new Thread("Print-Thread") {
                public void run() {
                    try {
                        if (pjob.printDialog()) {
                            pjob.print();
                        }
                    } catch (PrinterException e) {
                        e.printStackTrace();
                    }
                }
            };

            t.start();
        }
    }


    public enum SequenceToolsActionType {
        REVERSE_COMPLEMENT_ACTION,
        CALCULATE_ITR_ACTION,
        CALCULATE_A_PLUS_T_ACTION,
        NO_ACTION
    }

    public void openSequencesInNewBBB(final FeaturedSequence[] sequences) {
        Thread t = new Thread("Open in new BBB") {
            public void run() {
                //<html>Could not load genes from file:<br> "
                UITools.showWarning("<html>Multiple BBB Windows are experimental and not all features will work. <br>  If you wish to edit the sequence, save the new file as a new BBB and then open it separately.", getContentPane());
                openSelectedRegionBBB();
            }

            private void openSelectedRegionBBB() {

                DiffEditorFrame df = spawnNewWindow();

                boolean rc = df.loadSequences(sequences);
                System.out.println(rc);
                df.setSavable(true);
                df.setWorkFilename("New File");
                df.refreshTitle();
                df.setProcess(m_process);
                df.setSize(DiffEditorFrame.DEFAULT_SIZE);
                df.setVisible(true);
            }

        };
        t.start();
    }

    public double calculateAPlusT(String seq) {
        double count = 0.0;
        for (int i = 0; i < seq.length(); i++) {
            char c = seq.charAt(i);
            if (c == 'a' || c == 't' || c == 'A' || c == 'T') {
                count += 1.0;
            }
        }

        return count / ((double) seq.length());
    }

    protected class SequenceToolsAction extends AbstractAction {


        private SequenceToolsActionType action = SequenceToolsActionType.NO_ACTION;

        private SequenceToolsAction(SequenceToolsActionType action) {
            this.action = action;
        }


        @Override
        public void actionPerformed(ActionEvent e) {

            if (getPrimaryPanel() == null) {
                return;
            }

            FeaturedSequence[] sequences = getPrimaryPanel().getSelectedSegments(false);
            if (sequences == null || sequences.length == 0) {
                UITools.showWarning("Please mark one or more sequences to work with.", null);
                return;
            }
            switch (action) {
                case REVERSE_COMPLEMENT_ACTION:
                    //show a info dialog - reverse, complement, both
                    boolean doReverse = true;
                    boolean doComplement = true;
                    String choice = (String) JOptionPane.showInputDialog(DiffEditorFrame.this, "Reverse complement options:", "Calculate Reverse Complement", JOptionPane.PLAIN_MESSAGE, null, new Object[]{"Reverse Complement", "Reverse", "Complement"}, "ham");
                    if (choice == null || choice.length() == 0) {
                        return;
                    } else if (choice.equals("Reverse Complement")) {
                        doComplement = true;
                        doReverse = true;
                    } else if (choice.equals("Reverse")) {
                        doComplement = false;
                        doReverse = true;
                    } else if (choice.equals("Complement")) {
                        doComplement = true;
                        doReverse = false;
                    }
                    final FeaturedSequence[] rcSequences = new FeaturedSequence[sequences.length];
                    for (int i = 0; i < sequences.length; i++) {
                        String seq = sequences[i].toString();
                        if (doComplement) {
                            seq = SequenceUtility.make_simple_complement(seq);
                        }
                        if (doReverse) {
                            seq = new StringBuilder(seq).reverse().toString();
                        }
                        rcSequences[i] = new FeaturedSequence(sequences[i].getId(), sequences[i].getName(), seq);
                    }
                    openSequencesInNewBBB(rcSequences);

                    break;

                case CALCULATE_A_PLUS_T_ACTION:
                    for (FeaturedSequence sequence : sequences) {
                        if (sequence.getSequenceType() == AA_SEQUENCE) {
                            UITools.showWarning("Please select only DNA sequences to calculate A+T%.", null);
                            return;
                        }
                    }
                case CALCULATE_ITR_ACTION:
                    //show small text dialog with list of sequences and A+T% values
                    final JDialog reportWindow = new JDialog((java.awt.Frame) null, "");
                    if (action == SequenceToolsActionType.CALCULATE_A_PLUS_T_ACTION) {
                        reportWindow.setTitle("A+T%");
                    } else if (action == SequenceToolsActionType.CALCULATE_ITR_ACTION) {
                        reportWindow.setTitle("ITRs");
                    } else {
                        reportWindow.setTitle("");
                    }

                    reportWindow.setLocationRelativeTo(null);
                    BoxLayout outerLayout = new BoxLayout(reportWindow.getContentPane(), BoxLayout.Y_AXIS);
                    reportWindow.getContentPane().setLayout(outerLayout);

                    JPanel countsPanel = new JPanel();
                    GridBagLayout layout = new GridBagLayout();
                    countsPanel.setLayout(layout);

                    JScrollPane sp = new JScrollPane(countsPanel);
                    Border line = BorderFactory.createLineBorder(Color.black);
                    sp.setViewportBorder(BorderFactory.createTitledBorder(line, "Sequences"));
                    reportWindow.getContentPane().add(sp);

                    GridBagConstraints gc = new GridBagConstraints();
                    gc.fill = GridBagConstraints.BOTH;
                    gc.weightx = 0;
                    gc.gridx = 0;
                    gc.gridy = 0;
                    gc.ipadx = 5;
                    gc.ipady = 5;

                    for (FeaturedSequence sequence : sequences) {
                        gc.gridx = 0;
                        JLabel label = new JLabel(sequence.getName() + ": ");
                        layout.setConstraints(label, gc);
                        countsPanel.add(label);
                        gc.gridx = 1;

                        if (action == SequenceToolsActionType.CALCULATE_A_PLUS_T_ACTION) {
                            label = new JLabel(String.format("%.4f", calculateAPlusT(sequence.toString())));
                        } else if (action == SequenceToolsActionType.CALCULATE_ITR_ACTION) {
                            int[] itr = ITRCalculator.findITRPartialTerminalError(sequence.toString(), 0, 5);
                            label = new JLabel(String.format("(%d, %d)...complement(%d, %d)", itr[0], itr[1], itr[2], itr[3]));
                        } else {
                            label = new JLabel("");
                        }
                        layout.setConstraints(label, gc);
                        countsPanel.add(label);
                        gc.gridy++;
                    }

                    JPanel buttonPane = new JPanel();
                    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
                    buttonPane.add(Box.createHorizontalGlue());
                    JButton closeButton = new JButton("Close");
                    closeButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            reportWindow.dispose();
                        }
                    });
                    buttonPane.add(closeButton);
                    reportWindow.getContentPane().add(buttonPane);

                    reportWindow.pack();
                    reportWindow.setLocationRelativeTo(null);
                    reportWindow.setVisible(true);
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * Action for aligning a selected region of sequence
     */
    protected class AlignRegionListener implements ActionListener{
        protected String m_alignProg;

        public AlignRegionListener(String alignProg) {
            m_alignProg = alignProg;
        }

        public void actionPerformed(ActionEvent ev)  {
            if (getPrimaryPanel() == null) {
                return;
            }
            /**
             Call alignRegion for all other programs but mafft add.
             Results of mafft add are returned in vector global variable.
             They are then processed into a featured sequence vector, saved to a file, and then loaded into BBB.
             */
            if (m_alignProg.equals("mafft") && (mafftadd || mafftaddfrag)){
                Thread t = new Thread() {
                    public void run() {
                        getPrimaryPanel().alignRegion(m_alignProg);
                        Vector<FeaturedSequence> m = new Vector();

                        if (seqsFromMafftAdd.isEmpty()){

                            UITools.showWarning("mafft --add failed.", null);
                            mafftadd = false;
                            return;
                        }

                        int numOfSequences = 0;

                        //System.out.println("In action listener in diff editor frame");

                        for(int i = 0; i < seqsFromMafftAdd.size(); i++){

                            System.out.println(seqsFromMafftAdd.get(i).toString());
                            if (seqsFromMafftAdd.get(i).toString().contains(">")){
                                numOfSequences++;

                            }

                        }
                        FeaturedSequence[] g = new FeaturedSequence[numOfSequences];

                        String name = "";
                        String seq = "";
                        int j = 0;
                        int k = 0;

                        for(int i = 0; i < seqsFromMafftAdd.size(); i++){
                            FeaturedSequence f;
                            System.out.println(seqsFromMafftAdd.get(i).toString());
                            if (seqsFromMafftAdd.get(i).toString().contains(">")){

                                name = seqsFromMafftAdd.get(i).toString();

                                k = i + 1;
                                while (k < seqsFromMafftAdd.size() && !seqsFromMafftAdd.get(k).toString().contains(">")){
                                    seq+= seqsFromMafftAdd.get(k).toString();
                                    k++;
                                }
                               // System.out.println("SEQ = " + seq);
                                f = new FeaturedSequence(j,name,seq);
                                m.add(f);
                                seq = "";
                                g[j] = f;
                                j++;
                            }
                        }
                        try{
                        saveMafftAddSeqs();
                        loadSequences();
                        }
                        catch (IOException e){};

                            //loadSequences(m.toArray(new FeaturedSequence[0]));
                        if (mafftadd)
                            mafftadd = false;
                        else if (mafftaddfrag)
                            mafftaddfrag = false;

                        PrimaryPanel.ALREADY_ALIGNED = numOfSequences;

                        setSavable(true);
                        setWorkFilename("Mafft --add results");
                        refreshTitle();

                    }
                };
                t.start();
            }

            /**
             Call alignRegion for all other programs but mafft add.
             */
            else {
                Thread t = new Thread() {
                    public void run() {
                        getPrimaryPanel().alignRegion(m_alignProg);
                    }
                };
                t.start();
            }
        }
    }

    /**

    This function writes the returned sequences from mafft --add to a temporary file, out.fasta.

     */
    private void saveMafftAddSeqs() throws IOException{

        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter("out.fasta"));
        for (int i = 0; i < seqsFromMafftAdd.size(); i++) {
            outputWriter.write(seqsFromMafftAdd.get(i).toString() +"");
            outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();

    }
    /**
     * Action for skipping blocks of sequence backward and forward
     *
     * @author Ryan Brodie
     */
    protected class SequenceSkipAction extends AbstractAction {
        protected int m_dist;
        protected boolean m_forward;

        public SequenceSkipAction(int dist, boolean forward) throws IllegalArgumentException {
            super("Skip " + (forward ? "Right" : "Left") + " " + dist / 1000 + "k", Icons.getInstance().getIcon((forward ? "RIGHT" : "LEFT") + dist));
            if ((dist != 20) && (dist != 1000) && (dist != 10000)) {
                throw new IllegalArgumentException("Skip Distance must be 20, 1000, 10000");
            }
            m_dist = dist;
            m_forward = forward;
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            int val = (m_forward ? 1 : (-1)) * m_dist;

            int curPos = getPrimaryPanel().getPosition();
            int max = getPrimaryPanel().getMaxPosition();

            int next = Math.max(0, Math.min(max, curPos + (val)));

            getPrimaryPanel().scrollToLocation(next);

            System.out.println("Skipping " +
                    (m_forward ? "forward" : "backward") + " to " + next);
        }
    }

    protected class VGOExportListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }

            FeaturedSequence seq1 = null;
            FeaturedSequence seq2 = null;

            if (true) {
                SequenceSelector p = new SequenceSelector(getVisibleSequences(), "Select the primary 'Query' sequence", 1, 1);

                if (JOptionPane.showConfirmDialog(getContentPane(), p, "Select Sequences", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (p.getSelectionCount() != 1) {
                        UITools.showWarning("Please select 1 sequence", getContentPane());
                        actionPerformed(ev);
                    } else {
                        seq1 = p.getSelectedSequences()[0];
                    }
                } else {
                    return;
                }
            }

            if (getVisibleSequences().length > 2) {
                SequenceSelector p = new SequenceSelector(getVisibleSequences(), "Select the secondary or 'Standard' sequence", 1, 1);

                if (JOptionPane.showConfirmDialog(getContentPane(), p, "Select Sequences", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (p.getSelectionCount() != 1) {
                        UITools.showWarning("Please select 1 sequence", getContentPane());
                        actionPerformed(ev);
                    } else {
                        seq2 = p.getSelectedSequences()[0];
                    }
                } else {
                    return;
                }
            } else {
                seq2 = (getVisibleSequences()[0] == seq1) ? getVisibleSequences()[1] : getVisibleSequences()[0];
            }

            JFileChooser fc = new JFileChooser(m_currentDirectory);
            fc.setDialogTitle("Save to VGO File");

            MultiFileFilter ff = new MultiFileFilter("VGO Files");
            ff.addExtension("vgo");
            fc.addChoosableFileFilter(ff);

            if (fc.showDialog(DiffEditorFrame.this, "Export") == JFileChooser.APPROVE_OPTION) {
                final File f = fc.getSelectedFile();

                try {
                    VGOExporter.exportDifferences(seq1, seq2, f.getAbsolutePath());
                } catch (IOException ex) {
                    UITools.showError("Error exporting to " + f.getAbsolutePath(), DiffEditorFrame.this);
                }
            }
        }
    }

    /*
    Class including functions to used to create the amino acid sequence corresponding to the input nucleotide sequence
    Used when copying as Amino Acid and copying as FastaAA
     */
    protected class AminoAcidSequence {
        String sequence = "";

        public AminoAcidSequence(FeaturedSequence seq) {
            sequence = createAminoAcidSequences(seq);
        }

        /* initializes amino acid lookup array
         * 0 = T, 1 = C, 2 = A, 3 = G
         */
        public void initializeAALookup(char[][][] AALookup) {
            AALookup[0][0][0] = 'F';
            AALookup[0][0][1] = 'F';

            AALookup[0][0][2] = 'L';
            AALookup[0][0][3] = 'L';
            AALookup[1][0][0] = 'L';
            AALookup[1][0][1] = 'L';
            AALookup[1][0][2] = 'L';
            AALookup[1][0][3] = 'L';

            AALookup[2][0][0] = 'I';
            AALookup[2][0][1] = 'I';
            AALookup[2][0][2] = 'I';

            AALookup[2][0][3] = 'M';

            AALookup[3][0][0] = 'V';
            AALookup[3][0][1] = 'V';
            AALookup[3][0][2] = 'V';
            AALookup[3][0][3] = 'V';

            AALookup[0][1][0] = 'S';
            AALookup[0][1][1] = 'S';
            AALookup[0][1][2] = 'S';
            AALookup[0][1][3] = 'S';

            AALookup[1][1][0] = 'P';
            AALookup[1][1][1] = 'P';
            AALookup[1][1][2] = 'P';
            AALookup[1][1][3] = 'P';

            AALookup[2][1][0] = 'T';
            AALookup[2][1][1] = 'T';
            AALookup[2][1][2] = 'T';
            AALookup[2][1][3] = 'T';

            AALookup[3][1][0] = 'A';
            AALookup[3][1][1] = 'A';
            AALookup[3][1][2] = 'A';
            AALookup[3][1][3] = 'A';

            AALookup[0][2][0] = 'Y';
            AALookup[0][2][1] = 'Y';

            AALookup[0][2][2] = '*'; //stop
            AALookup[0][2][3] = '*'; //stop

            AALookup[1][2][0] = 'H';
            AALookup[1][2][1] = 'H';

            AALookup[1][2][2] = 'Q';
            AALookup[1][2][3] = 'Q';

            AALookup[2][2][0] = 'N';
            AALookup[2][2][1] = 'N';

            AALookup[2][2][2] = 'K';
            AALookup[2][2][3] = 'K';

            AALookup[3][2][0] = 'D';
            AALookup[3][2][1] = 'D';

            AALookup[3][2][2] = 'E';
            AALookup[3][2][3] = 'E';

            AALookup[0][3][0] = 'C';
            AALookup[0][3][1] = 'C';

            AALookup[0][3][2] = '*'; //stop

            AALookup[0][3][3] = 'W';

            AALookup[1][3][0] = 'R';
            AALookup[1][3][1] = 'R';
            AALookup[1][3][2] = 'R';
            AALookup[1][3][3] = 'R';

            AALookup[2][3][0] = 'S';
            AALookup[2][3][1] = 'S';

            AALookup[2][3][2] = 'R';
            AALookup[2][3][3] = 'R';

            AALookup[3][3][0] = 'G';
            AALookup[3][3][1] = 'G';
            AALookup[3][3][2] = 'G';
            AALookup[3][3][3] = 'G';
        }

        public String getComplement(String seq) {
            char[] charArr = seq.toCharArray();
            for (int i = 0; i < charArr.length; i++) {
                switch (charArr[i]) {
                    case 'T':
                        charArr[i] = 'A';
                        break;
                    case 'A':
                        charArr[i] = 'T';
                        break;
                    case 'C':
                        charArr[i] = 'G';
                        break;
                    case 'G':
                        charArr[i] = 'C';
                        break;
                }
            }
            return new String(charArr);
        }

        public String removeDashes(String seq) {
            for (int i = 0; i < seq.length(); i++) {
                if (seq.charAt(i) == '-') {
                    seq = seq.substring(0, i) + seq.substring(i + 1, seq.length()); //remove the dash
                    i--; //subtract 1 because s.length decreased by 1
                }
            }
            return seq;
        }

        public String createAminoAcidSequences(FeaturedSequence seq) {
            String AAseq = "";
            char[][][] AALookup = new char[4][4][4];
            initializeAALookup(AALookup);
            int base[] = new int[3];

            String seqCopy = seq.toString();
            seqCopy = removeDashes(seqCopy);

            // if looking at 3' Bottom 5' sequence get the complement and reverse the nucleotide sequence
            if (getPrimaryPanel().getDisplayStrand().equals(NEGATIVE)) {
                seqCopy = getComplement(seqCopy);
                seqCopy = new StringBuilder(seqCopy).reverse().toString();
            }

            for (int k = 0; k < seqCopy.length(); k += 3) { //look at every base in nucleotide sequence
                for (int basePos = 0; basePos < 3; basePos++) {
                    if (k + basePos >= seqCopy.length()) {
                        return AAseq;
                    }
                    if (seqCopy.charAt(k + basePos) == 'T') {
                        base[basePos] = 0;
                    } else if (seqCopy.charAt(k + basePos) == 'C') {
                        base[basePos] = 1;
                    } else if (seqCopy.charAt(k + basePos) == 'A') {
                        base[basePos] = 2;
                    } else if (seqCopy.charAt(k + basePos) == 'G') {
                        base[basePos] = 3;
                    }
                }
                AAseq += AALookup[base[0]][base[1]][base[2]];
            }
            return AAseq;
        }
    }

    protected class CopyNucleotideSeqAction extends AbstractAction {

        boolean fastaFormat;
        String theString = "";

        public CopyNucleotideSeqAction(boolean copyingFasta) {
            fastaFormat = copyingFasta;
        }

        /**
         * Action invoked when copying nucleotide sequence to clipboard
         */
        public void actionPerformed(ActionEvent ae) {
            String errMsg = "Please make a valid selection of genes and sequences.\n" + "Ensure that a sequence name on the left pane is highlighted and a selection of nucleotides has been made.";
            try {
                theString = "";
                int[] selected_seqs = getPrimaryPanel().getSelectedIndices();

                if (selected_seqs.length < 1) {
                    UITools.showWarning(errMsg, null);
                    return;
                }

                FeaturedSequence[] seqs = getPrimaryPanel().getSelectedSegments(true);
                FeaturedSequence[] names = getPrimaryPanel().getSelectedSequences();
                int min = 0;
                int max = 0;

                for (FeaturedSequence fs : names) {
                    if (fs.getSequenceType() != EditableSequence.DNA_SEQUENCE) {
                        JOptionPane.showMessageDialog(null, "Cannot copy amino acid sequence as nucleotide sequence.", "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }

                if (fastaFormat) {
                    EditPanel p = getPrimaryPanel().m_epanels.get(selected_seqs[0]);
                    FeaturedSequence seq = p.getSequence();
                    Location l = p.getAbsoluteSelection2();
                    min = seq.getRelativePosition(l.getMin() + 1);
                    max = seq.getRelativePosition(l.getMax() + 1);
                }

                for (int t = 0; t < names.length; t++) {
                    if (fastaFormat) {
                        theString += ">" + names[t].getName() + " (" + min + "," + max + ")\n";
                    }

                    theString += seqs[selected_seqs[t]];

                    if (t < names.length - 1) {
                        theString += "\n\n";
                    }
                }

                StringSelection selection = new StringSelection(theString);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            } catch (NullPointerException e) {
                UITools.showWarning(errMsg, null);
            } catch (ArrayIndexOutOfBoundsException e) {
                UITools.showWarning(errMsg, null);
            }
        }

    }

    protected class CopyAminoAcidSeqAction extends AbstractAction {

        boolean fastaFormat;

        public CopyAminoAcidSeqAction(boolean copyingFasta) {
            fastaFormat = copyingFasta;
        }

        /**
         * Action invoked when copying amino acid sequence to clipboard
         */
        public void actionPerformed(ActionEvent ae) {
            String errMsg = "Please make a valid selection of genes and sequences.\n" + "Ensure that a sequence name on the left pane is highlighted and a selection of nucleotides has been made.";
            try {
                int[] selected_seqs = getPrimaryPanel().getSelectedIndices();

                if (selected_seqs.length > 1) {
                    UITools.showWarning("Please select only 1 sequence to be copied.", null);
                    return;
                }
                if (selected_seqs.length < 1) {
                    UITools.showWarning(errMsg, null);
                    return;
                }

                FeaturedSequence[] seqs = getPrimaryPanel().getSelectedSegments(true);
                FeaturedSequence[] names = getPrimaryPanel().getSelectedSequences();
                int selected = getPrimaryPanel().getSelectedSequence();
                String theString = "";
                // check if selected sequence is already an amino acid sequence
                if (seqs[selected].getSequenceType() == EditableSequence.AA_SEQUENCE) {
                    theString = seqs[selected].toString();
                } else {
                    AminoAcidSequence AAseq = new AminoAcidSequence(seqs[selected]);
                    theString = ""; //string to be copied to clipboard

                    if (fastaFormat) {
                        EditPanel p = getPrimaryPanel().m_epanels.get(selected_seqs[0]);
                        FeaturedSequence seq = p.getSequence();
                        Location l = p.getAbsoluteSelection2();
                        int min = seq.getRelativePosition(l.getMin() + 1);
                        int max = seq.getRelativePosition(l.getMax() + 1);
                        theString += ">" + names[0].getName() + " (" + min + "," + max + ")\n";
                    }
                    theString += AAseq.sequence;

                }
                StringSelection selection = new StringSelection(theString);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            } catch (NullPointerException e) {
                UITools.showWarning(errMsg, null);
            } catch (ArrayIndexOutOfBoundsException e) {
                UITools.showWarning(errMsg, null);
            }
        }
    }

    protected class ResolveDifferenceAction extends AbstractAction {

        boolean direction;

        public ResolveDifferenceAction(boolean dir) {
            direction = dir;
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }
            int pos = getPrimaryPanel().resolveDifference(getPrimaryPanel().getPosition(), direction);
            //position is 0-based whereas scrollToLocation takes a 1-based index
            getPrimaryPanel().scrollToLocation(pos + 1);
        }

    }

    protected class GapPositionAction extends AbstractAction {
        boolean direction;

        public GapPositionAction(boolean dir) {
            direction = dir;
        }

        public void actionPerformed(ActionEvent ev) {
            if (getPrimaryPanel() == null) {
                return;
            }
            getPrimaryPanel().getGapPosition(direction);
        }
    }

    protected class QuickAddFilesAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            final JFrame frame = new javax.swing.JFrame("Quick File Drop");

            final List<String> fileList = new ArrayList<String>();

            //Table setup
            String[] columnNames = {"Sequence"};
            Object[][] data = {};
            final DefaultTableModel model = new DefaultTableModel(data, columnNames);
            final JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            table.setFillsViewportHeight(true);
            frame.setLayout(new BorderLayout());
            frame.add(table.getTableHeader(), BorderLayout.PAGE_START);
            table.getTableHeader().setVisible(false);
            //column widths
            TableColumn column = table.getColumnModel().getColumn(0);
            column.setPreferredWidth(600);

            //table file drop listener
            new FileDrop(System.out, table, new FileDrop.Listener() {
                public void filesDropped(java.io.File[] files) {
                    for (int i = 0; i < files.length; i++) {
                        try {
                            if (getFileType(files[i].getCanonicalPath()) != null) {
                                model.insertRow(model.getRowCount(), new Object[]{files[i].getCanonicalPath()});
                                fileList.add(files[i].getCanonicalPath());
                            }
                        } catch (java.io.IOException e) {
                        }
                    }
                }
            });

            //Drop files here background
            String labelText = "<html><FONT COLOR='#333333' SIZE='22'>Drop files here</FONT></html>";
            final JLabel bg_text = new JLabel(labelText, JLabel.CENTER);
            frame.add(bg_text);
            //bg file drop listener
            new FileDrop(System.out, bg_text, new FileDrop.Listener() {
                public void filesDropped(java.io.File[] files) {
                    table.getTableHeader().setVisible(true);
                    frame.add(table, BorderLayout.CENTER);
                    bg_text.setVisible(false);
                    for (int i = 0; i < files.length; i++) {
                        try {
                            if (getFileType(files[i].getCanonicalPath()) != null) {
                                model.insertRow(model.getRowCount(), new Object[]{files[i].getCanonicalPath()});
                                fileList.add(files[i].getCanonicalPath());
                            }
                        } catch (java.io.IOException e) {
                        }
                    }
                }
            });


            //Buttons
            JPanel btns = new JPanel();
            btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
            btns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            //Add Sequences
            JButton add = new JButton("Add These Sequences");
            Font f = new Font("Verdana", Font.BOLD, 12);
            //Font f = new Font("Dialog", Font.PLAIN, 16);
            add.setFont(f);
            btns.add(Box.createHorizontalStrut(3));
            btns.add(add);
            add.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    for (int i = 0; i < fileList.size(); i++) {
                        String filename = fileList.get(i);
                        FeaturedSequenceReader in;
                        try {
                            in = getFileType(filename);
                            final ListIterator li = in.getSequences();
                            Vector<FeaturedSequence> fileSequences = new Vector<FeaturedSequence>();
                            while (li.hasNext()) {
                                fileSequences.add((FeaturedSequence) li.next());
                            }
                            if (getSequences().length == 0) {
                                boolean rc = loadSequences(fileSequences.toArray(new FeaturedSequence[0]));
                                setSavable(true);
                                setWorkFilename(filename);
                                refreshTitle();
                            } else {

                            }
                            System.out.print(filename + " added\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
						/*fileList.clear();
						table.getTableHeader().setVisible(false);
						while(model.getRowCount() > 0)
							model.removeRow(0);
						bg_text.setVisible(true);*/
                    frame.dispose();
                }
            });

            //browse button
            JButton browse = new JButton("Browse Files");
            btns.add(Box.createHorizontalStrut(3));
            btns.add(browse);
            browse.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    File fi = new FileChooser().openFile("Load Sequences", file);
                    try {
                        if (getFileType(fi.getCanonicalPath()) != null) {
                            if (bg_text.isVisible()) {
                                frame.add(table, BorderLayout.CENTER);
                                bg_text.setVisible(false);
                            }
                            model.insertRow(model.getRowCount(), new Object[]{fi.getCanonicalPath()});
                            fileList.add(fi.getCanonicalPath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            //add buttons
            frame.add(btns, BorderLayout.SOUTH);

            //set frame size
            frame.setBounds(100, 100, 600, 400);
            frame.setVisible(true);

        }

        private FeaturedSequenceReader getFileType(String filename) {
            try {
                File file = new File(filename);
                String ext = getExtension(file);

                if (ext.toLowerCase().equals("clustal") || ext.toLowerCase().equals("aln"))
                    return new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.CLUSTAL_FORMAT, file, DiffEditFeaturedSequence.class);
                else if (ext.toLowerCase().equals("embl"))
                    return new EMBLFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                else if (ext.toLowerCase().equals("fasta") || ext.toLowerCase().equals("fna") || ext.toLowerCase().equals("ffn") || ext.toLowerCase().equals("faa") || ext.toLowerCase().equals("frn") || ext.toLowerCase().equals("fa") || ext.toLowerCase().equals("fsa") || ext.toLowerCase().equals("mpfa"))
                    return new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.FASTA_FORMAT, file, DiffEditFeaturedSequence.class);
                else if (ext.toLowerCase().equals("genbank") || ext.toLowerCase().equals("gb") || ext.toLowerCase().equals("gbk"))
                    return new GenBankFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                else if (ext.toLowerCase().equals("bbb") || ext.toLowerCase().equals("xml"))
                    return new BSMLFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                else
                    UITools.showWarning("Unsupported file format. We support the following:\nGenBank, EMBL, BBB, FASTA, and CLUSTAL", getContentPane());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        private String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 && i < s.length() - 1)
                ext = s.substring(i + 1).toLowerCase();

            if (ext == null)
                return "";
            return ext;
        }


    }

    public String getFileType(File file) {
        String filename = file.getName();
        int i = filename.lastIndexOf(".");
        if (i == -1) {
            return null;
        }
        String extension = filename.substring(i).toLowerCase();

        if (extension.equals(".clustal") || extension.equals(".clustalw")) {
            return "CLUSTAL";

        } else if (extension.equals(".embl")) {
            return "EMBL";

        } else if (extension.equals(".fasta") || extension.equals(".fas") || extension.equals(".fa")) {
            return "FASTA";

        } else if (extension.equals(".gb") || extension.equals(".gbk")) {
            return "GENBANK";

        } else if (extension.equals(".bbb")) {
            return "BBB";
        }
        return null;
    }

    protected class DemoFileAction extends AbstractAction {

        String action = "Load Sequences";
        String type = null;
        boolean autoType = false;

        public DemoFileAction(String newAction, String fileType) {
            action = newAction;
            type = fileType;
        }

        public DemoFileAction(String newAction) {
            action = newAction;
            autoType = true;
        }


        public void actionPerformed(ActionEvent ae) {

            try {
                Desktop.getDesktop().browse(new URL("https://4virology.net/files/codehop/example-uracildnaglycosylase.fasta").toURI());
            } catch (Exception e) {}

            File file = null;

            //File file = getSequenceFileFromCommonDialog(action);
            //No file chosen or cancel opted
            if (file == null) {
                return;
            }
            if (autoType) {
                type = getFileType(file);
                if (type == null) {
                    UITools.showWarning("Unsupported file format. We support the following:\n" +
                            "GenBank (*.gb or *.gbk), " +
                            "EMBL (*.embl), " +
                            "BBB (*.bbb), " +
                            "FASTA (*.fasta or *.fas or *.fa), " +
                            "and CLUSTAL (*.clustal or *.clustalw)", getContentPane());
                    return;
                }
            }


            String filename = file.getAbsolutePath();
            if (!checkFileFormat(file, type)) {
                return;//If there is an error in reading the file format!!
            }
            try {
                // --- check saved status ---
                //if (!checkForSave()) {
                //	return;
                //}

                getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));

                FeaturedSequenceReader in = null;
                if (type.equals("CLUSTAL"))
                    in = new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.CLUSTAL_FORMAT, file, DiffEditFeaturedSequence.class);
                else if (type.equals("EMBL"))
                    in = new EMBLFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                else if (type.equals("FASTA"))
                    in = new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.FASTA_FORMAT, file, DiffEditFeaturedSequence.class);
                else if (type.equals("GENBANK"))
                    in = new GenBankFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                else if (type.equals("BBB")) {
                    in = new BSMLFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                } else {
                    UITools.showWarning("Unsupported file format. We support the following:\nGenBank, EMBL, BBB, FASTA, and CLUSTAL", getContentPane());
                    return;
                }

                //FRESHLY LOADING SEQUENCES
                if (action.equals("Load Sequences")) {
                    m_status.setText("Loading Alignment from File:" + filename + "...");
                    final ListIterator li = in.getSequences();

                    Vector v = new Vector();
                    while (li.hasNext()) {
                        v.add(li.next());
                    }


                    boolean rc = loadSequences((FeaturedSequence[]) v.toArray(new FeaturedSequence[0]));


                    setSavable(true);
                    setWorkFilename(filename);
                    refreshTitle();
                } else {
                    //ADDING SEQUENCES TO ALIGNMENT
                    m_status.setText("Adding Sequences from File " + file.getAbsolutePath() + "...");
                    final ListIterator li = in.getSequences();
                    Vector<FeaturedSequence> fileSequences = new Vector<FeaturedSequence>();
                    while (li.hasNext()) {
                        fileSequences.add((FeaturedSequence) li.next());
                    }

                    appendSequences(fileSequences.toArray(new FeaturedSequence[0]));
                }

                if (type.equals("BBB")) {
                    notebook = ((BSMLFeaturedSequenceReader) in).getNotebook();
                }

                System.out.println(filename + " Successfully parsed.");
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            } catch (IOException ex) {
                ex.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            } catch (Exception iex) {
                iex.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            } finally {
                m_status.clear();
                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    //Loads a BBB or FASTA or EMBL or GENBANK or CLUSTAL file into BBB editor
    protected class LoadFileAction extends AbstractAction {

        String action = "Load Sequences";
        String type = null;
        boolean autoType = false;

        public LoadFileAction(String newAction, String fileType) {
            action = newAction;
            type = fileType;
        }

        public LoadFileAction(String newAction) {
            action = newAction;
            autoType = true;
        }


        public void actionPerformed(ActionEvent ae) {
            File file = getSequenceFileFromCommonDialog(action);
            //No file chosen or cancel opted
            if (file == null) {
                return;
            }
            if (autoType) {
                type = getFileType(file);
                if (type == null) {
                    UITools.showWarning("Unsupported file format. We support the following:\n" +
                            "GenBank (*.gb or *.gbk), " +
                            "EMBL (*.embl), " +
                            "BBB (*.bbb), " +
                            "FASTA (*.fasta or *.fas or *.fa), " +
                            "and CLUSTAL (*.clustal or *.clustalw)", getContentPane());
                    return;
                }
            }


            String filename = file.getAbsolutePath();
            if (!checkFileFormat(file, type)) {
                return;//If there is an error in reading the file format!!
            }
            try {
                // --- check saved status ---
                //if (!checkForSave()) {
                //	return;
                //}

                getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));

                FeaturedSequenceReader in = null;
                if (type.equals("CLUSTAL"))
                    in = new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.CLUSTAL_FORMAT, file, DiffEditFeaturedSequence.class);
                else if (type.equals("EMBL"))
                    in = new EMBLFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                else if (type.equals("FASTA"))
                    in = new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.FASTA_FORMAT, file, DiffEditFeaturedSequence.class);
                else if (type.equals("GENBANK"))
                    in = new GenBankFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                else if (type.equals("BBB")) {
                    in = new BSMLFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                } else {
                    UITools.showWarning("Unsupported file format. We support the following:\nGenBank, EMBL, BBB, FASTA, and CLUSTAL", getContentPane());
                    return;
                }

                //FRESHLY LOADING SEQUENCES
                if (action.equals("Load Sequences")) {
                    m_status.setText("Loading Alignment from File:" + filename + "...");
                    final ListIterator li = in.getSequences();

                    Vector v = new Vector();
                    while (li.hasNext()) {
                        v.add(li.next());
                    }


                    boolean rc = loadSequences((FeaturedSequence[]) v.toArray(new FeaturedSequence[0]));


                    setSavable(true);
                    setWorkFilename(filename);
                    refreshTitle();

                    boolean aligned = false;

                    for (int i = 0; i < v.size(); i++){

                        //System.out.println(v.get(i).toString().length());

                        int currSeqLength = v.get(i).toString().length();

                        if (currSeqLength != v.get(0).toString().length())
                            aligned = false;
                            
                        else {
                            aligned = true;
                            PrimaryPanel.ALREADY_ALIGNED = v.size();
                        }

                    }

                } else {
                    //ADDING SEQUENCES TO ALIGNMENT
                    m_status.setText("Adding Sequences from File " + file.getAbsolutePath() + "...");
                    final ListIterator li = in.getSequences();
                    Vector<FeaturedSequence> fileSequences = new Vector<FeaturedSequence>();
                    while (li.hasNext()) {
                        fileSequences.add((FeaturedSequence) li.next());
                    }

                    appendSequences(fileSequences.toArray(new FeaturedSequence[0]));
                }

                if (type.equals("BBB")) {
                    notebook = ((BSMLFeaturedSequenceReader) in).getNotebook();
                }

                System.out.println(filename + " Successfully parsed.");
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            } catch (IOException ex) {
                ex.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            } catch (Exception iex) {
                iex.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            } finally {
                m_status.clear();
                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    //Loads a BBB or FASTA or EMBL or GENBANK or CLUSTAL file into BBB editor
    protected class TestFileAction extends AbstractAction {

        String action = "Load Sequences";
        String type = null;
        boolean autoType = false;

        

        public TestFileAction(String newAction, String fileType) {
            action = newAction;
            type = fileType;
        }

        public TestFileAction(String newAction) {
            action = newAction;
            autoType = true;
        }
        public void loaddemo(){
            int id = 0;
            String name = ">CMLV-CMS-127";
            String sequence = "MNSVTVLHAPYTITYHDDWEPVMNQLVEFYNEVASWLLRDETSPIPDKFFIQLKQPLRNKRVCVCGIDPYPKDGTGVPFESPNFTKKSIKEIASSISRLTGVIDYKGYNLNIIDGVIPWNYYLSCKLGETKSHAIYWDKISKLLLHHITKHVSVLYCLGKTDFSNIRAKLESPVTTIVGYHPAARDRQFEKDRSFEIINVLLELDNKAPINWAQGFIY";
            FeaturedSequence f = new FeaturedSequence(id,name,sequence);

            id = 1;
            name = ">COTV-SPAn232-098";
            sequence = "MKSVTIKYRPYIIEYDNDWEPIICKLVECYNEVAIWILNDETSPEPENFFKQLKVPLRDKRVCVCGIDPYPKDATGVPFESLNFSKKTIIAIASKVSTITGITGYKGFNLNNIDGLIPWNYYLSCKIGETKSHSLHWKKISKLLLQHITKYVNVLYCLGKTDFSNIRSLIETPVTTIVGYHPAARDKQFEKDTSLEIVNLLLKVNDKQSINWSQGFIF";

            FeaturedSequence f1 = new FeaturedSequence(id,name,sequence);

            id = 2;
            name = ">CRV-ZWE-112";
            sequence = "MSSPMILKKYLQHPPYEILYHHDWQDVVDRIAEPLSDVARYIFSENTSPKRDRIFRQLSEPLNDKTVCVMGIDPYPRDATGVPFQSPSFSKLAVRNLANKIAGHYGYRNYTNFDFSRIPGVFPWNYYLSCRVGETKSQALYWERCSKLLLNHICSRVRLLYCLGRSDFENVKSKIENPITLVVGYHPSTRDPALFREDNSLWIVNELLALQDLEPVEWWRGLTFD";

            FeaturedSequence f2 = new FeaturedSequence(id,name,sequence);

            id = 3;
            name = ">DPV-W848_83-089";
            sequence = "MKKITIKYRPYIIEYHEDWESIIDQLVDGYNEVAKWILKDETSPIPENFFKQLSVSLKDKRVCICGIDPYPHDATGVPFESPNFSKKTIKSIAASVSNITGVVHYKGYNLNIIDGVFPWNYYLSCRIGETKSHSLHWKKISKILLQHITKYVNVLYCLGKTDFSIIKSLLDTPVTTIIGYHPAARDKQFEKDKGFEIVNILLELNDKPAIDWSQGFSY";

            FeaturedSequence f3 = new FeaturedSequence(id,name,sequence);

            id = 4;
            name = ">GTPV-G20LKV-080";
            sequence = "MKTIKTKSFPYFIEYHDDWEAVINQLVDLYDEVAEWILKDETSPIPENFFKQLQKPLNDKLVCICGIDPYPRDATGVPFESPNFSKKTIKSIAETISNITGISNYKGYNLNNVKGVFPWNYYLSCKIGETKSHSLHWKKISKLLLQHITKYVNILYCLGKTDFSNIKSIIEVPVTTIIGYHPAARDRQFSKDKSFEAINILLEINGKEIIHWEEGFCY";

            FeaturedSequence f4 = new FeaturedSequence(id,name,sequence);

            id = 5;
            name = ">MOCV-st1-094";
            sequence = "MLRERALRAAPHVLRYHEDWEPVAEPLADAYAEVAPWLLRDRTEPAPERFFRQLELPLRDKRVCIVGIDPYPEGATGVPFESPDFSKKTARALAAAAARAAEHGGCRRVSAYRNYDFRGVQGVLAWNYYLSCRRGETKSHAMHWERIARMLLAHIARFVRVFYFLGRSDFGGVRAKLTAPVTLLVGYHPAARGGQFESERTLEILNVLLELHGLAPVDWAQGFVPL";

            FeaturedSequence f5 = new FeaturedSequence(id,name,sequence);

            id = 6;
            name = ">MYXV-BD23-083";
            sequence = "MRRVVLAHEPYVIEYHEDWEHIIARLVDMYNEVAEWILKDDTSPTPDKFFKQLSVSLKDKRVCVCGIDPYPRDATGVPFESPNFTKKTIKCIAETVSNITGVGYYKGYNLNDVEGVFPWNYYLSCKIGETKSHALHWKRISKLLLQHITKYVNVLYCLGKTDFANIRSILETPVTTVIGYHPAAREKQFEKDKGFEIVNVLLEINNKPAIRWEQGFSY";

            FeaturedSequence f6 = new FeaturedSequence(id,name,sequence);

            id = 7;
            name = ">RCNV-Herman-105";
            sequence = "MNTVSVSYRPYTITYHNDWEPVMNQLVEFYNEVASWLLRDDTSPIPDNFFIQLKQPLRNKRVCVCGIDPYPRDGTGVPFESPNFTKKSIKEIASSISRLTGVIDYKGYNLNIIDGVIPWNYYLSCRLGETKSHAIYWDKISKLLLQHISKHVSVLYCLGKTDFSNIRAKLESPVTTIVGYHPAARDRQFEKDRSFEIINVLLELDNKEPINWSQGFIY";

            FeaturedSequence f7 = new FeaturedSequence(id,name,sequence);

            id = 8;
            name = ">SWPV-Neb-079";
            sequence = "MKTIRVHYDPFIIEYHEDWDHIMDQIAEMYNEVAEWILRDNTSPSPNNFFKQLRVPLKNKRVCVCGIDPYPNDATGIPFESPNFSKKTIKAIALSISKITGIVNYKGYNLNHVDGVIPWNYYLSCKVGETKSHALHWKKISKICLQHITKYVNILYCLGKTDFSNIKSILDTPITTIVGYHPAARDKQFDKDRAFEVINVLLEINDKLPINWEQGFY";

            FeaturedSequence f8 = new FeaturedSequence(id,name,sequence);

            id = 9;
            name = ">YKV-DakArB_4268-089";
            sequence = "MIKSISVSYSPYNIEYHEDWEPIMSQLVCLYNEIASWLLRDETSPIPDNFFVQLKQPLRNKRVCICGIDPYPKDATGVPFESPRFNKKSIKTIAESVSKLTGVTDYSGYNLNIIDGVIPWNYYLSCKIGVTKSHAIYWEKISKLLLQHITKHVNILYCLGKTDFSNIRAKLDTPVTVVIGYHPAARDRQFEKDKAFEIINVLLELNNYEPINWAKGFYY";

            FeaturedSequence f9 = new FeaturedSequence(id,name,sequence);

            id = 10;
            name = ">YMTV-Amano-075";
            sequence = "MKTIKLEHKPYVLEYHEDWEPVIEQFKILYDEVAKWIIKDETSPKPEFFFLQFKLPLNDKRVCVCGIDPYPRDATGVPFESPTFSKKTIRYIAESVSKITGITDYKGYNLNKIEGVFPWNYYLSCRLGETKSHALHWKKISKMLLQHISKHVSVLYCLGKTDFCTIRSILDSPVTTVVGYHPAARDKQFEKDCAFEVVNVLLEINGEAPIDWSQGFTYH";

            FeaturedSequence f10 = new FeaturedSequence(id,name,sequence);
            
            FeaturedSequence[] g = new FeaturedSequence[11];
            g[0] = f;
            g[1] = f1;
            g[2] = f2;
            g[3] = f3;
            g[4] = f4;
            g[5] = f5;
            g[6] = f6;
            g[7] = f7;
            g[8] = f8;
            g[9] = f9;
            g[10] = f10;
            boolean r = loadSequences(g);


            setSavable(true);
            setWorkFilename("demo");
            refreshTitle();

            m_status.clear();
            getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        public void actionPerformed(ActionEvent ae) {
            //File file = getSequenceFileFromCommonDialog(action);
            //No file chosen or cancel opted

            File file = null;
            if (file == null) {
                loaddemo();
                return;
            }
            if (autoType) {
                type = getFileType(file);
                if (type == null) {
                    UITools.showWarning("Unsupported file format. We support the following:\n" +
                            "GenBank (*.gb or *.gbk), " +
                            "EMBL (*.embl), " +
                            "BBB (*.bbb), " +
                            "FASTA (*.fasta or *.fas or *.fa), " +
                            "and CLUSTAL (*.clustal or *.clustalw)", getContentPane());
                    return;
                }
            }


            String filename = file.getAbsolutePath();
            if (!checkFileFormat(file, type)) {
                return;//If there is an error in reading the file format!!
            }
            try {
                // --- check saved status ---
                //if (!checkForSave()) {
                //	return;
                //}

                getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));

                FeaturedSequenceReader in = null;
                if (type.equals("CLUSTAL"))
                    in = new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.CLUSTAL_FORMAT, file, DiffEditFeaturedSequence.class);
                else if (type.equals("EMBL"))
                    in = new EMBLFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                else if (type.equals("FASTA"))
                    in = new TextFileFeaturedSequenceReader(TextFileFeaturedSequenceReader.FASTA_FORMAT, file, DiffEditFeaturedSequence.class);
                else if (type.equals("GENBANK"))
                    in = new GenBankFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                else if (type.equals("BBB")) {
                    in = new BSMLFeaturedSequenceReader(file.getAbsolutePath(), DiffEditFeaturedSequence.class);
                } else {
                    UITools.showWarning("Unsupported file format. We support the following:\nGenBank, EMBL, BBB, FASTA, and CLUSTAL", getContentPane());
                    return;
                }

                //FRESHLY LOADING SEQUENCES
                if (action.equals("Load Sequences")) {
                    m_status.setText("Loading Alignment from File:" + filename + "...");
                    final ListIterator li = in.getSequences();

                    Vector v = new Vector();
                    while (li.hasNext()) {
                        v.add(li.next());
                        System.out.println(li.next());
                    }

                    int id = 0;
                    String name = ">CMLV-CMS-127";
                    String sequence = "MNSVTVLHAPYTITYHDDWEPVMNQLVEFYNEVASWLLRDETSPIPDKFFIQLKQPLRNKRVCVCGIDPYPKDGTGVPFESPNFTKKSIKEIASSISRLTGVIDYKGYNLNIIDGVIPWNYYLSCKLGETKSHAIYWDKISKLLLHHITKHVSVLYCLGKTDFSNIRAKLESPVTTIVGYHPAARDRQFEKDRSFEIINVLLELDNKAPINWAQGFIY";
                    FeaturedSequence f = new FeaturedSequence(id,name,sequence);

                    id = 1;
                    name = ">COTV-SPAn232-098";
                    sequence = "MKSVTIKYRPYIIEYDNDWEPIICKLVECYNEVAIWILNDETSPEPENFFKQLKVPLRDKRVCVCGIDPYPKDATGVPFESLNFSKKTIIAIASKVSTITGITGYKGFNLNNIDGLIPWNYYLSCKIGETKSHSLHWKKISKLLLQHITKYVNVLYCLGKTDFSNIRSLIETPVTTIVGYHPAARDKQFEKDTSLEIVNLLLKVNDKQSINWSQGFIF";

                    FeaturedSequence f1 = new FeaturedSequence(id,name,sequence);

                    FeaturedSequence[] g = new FeaturedSequence[2];
                    g[0] = f;
                    g[1] = f1;
                    boolean r = loadSequences(g);

                    //boolean rc = loadSequences((FeaturedSequence[]) v.toArray(new FeaturedSequence[0]));


                    setSavable(true);
                    setWorkFilename(filename);
                    refreshTitle();
                } else {
                    //ADDING SEQUENCES TO ALIGNMENT
                    m_status.setText("Adding Sequences from File " + file.getAbsolutePath() + "...");
                    final ListIterator li = in.getSequences();
                    Vector<FeaturedSequence> fileSequences = new Vector<FeaturedSequence>();
                    while (li.hasNext()) {
                        fileSequences.add((FeaturedSequence) li.next());
                    }

                    appendSequences(fileSequences.toArray(new FeaturedSequence[0]));
                }

                if (type.equals("BBB")) {
                    notebook = ((BSMLFeaturedSequenceReader) in).getNotebook();
                }

                System.out.println(filename + " Successfully parsed.");
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            } catch (IOException ex) {
                ex.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            } catch (Exception iex) {
                iex.printStackTrace();
                UITools.showWarning("Unsupported/Corrupt file format.", getContentPane());
            } finally {
                m_status.clear();
                getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }


    // Feb 2012 - Find Differences
    // Allows user to select several sequences to compare with rest and find differences
    protected class FindDifferencesAction extends AbstractAction {
        int openIndex = 0;

        public FindDifferencesAction(int openIndex) {
            this.openIndex = openIndex;
        }

        public void actionPerformed(ActionEvent ev) {
            Thread t = new Thread() {
                public void run() {
                    //add view log option
                    if (!logOptionAdded) {
                        logOptionAdded = true;
                        log.setVisible(true);
                        JMenu menu = (JMenu) mb.getComponent(6);
                        JMenuItem mi = new JMenuItem("View Log");
                        mi.addActionListener(new ShowLogDifferencesAction());
                        menu.add(mi);
                    }
                    //launch differences pane
                    getPrimaryPanel().findDifferences(openIndex, logPane);
                }
            };
            t.start();
        }

    }

    // Feb 2012 - Show Log Differences
    // Allows user to select several sequences to compare with rest and find differences
    boolean logOptionAdded = false;

    protected class ShowLogDifferencesAction extends AbstractAction {

        public ShowLogDifferencesAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            Thread t = new Thread() {
                public void run() {
                    getPrimaryPanel().showLogDifferences();
                }
            };
            t.start();
        }

    }

    // Feb 2012 - Get Column Counts - gaps, # of nucleotides
    protected class GetCountsAction extends AbstractAction {

        public GetCountsAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            try {
                FeaturedSequence[] sequences = getPrimaryPanel().getVisibleSequences();
                if (sequences == null || sequences.length == 0) {
                    UITools.showWarning("At least one sequence must be visible.", null);
                    return;

                }
                Thread t = new Thread() {
                    public void run() {
                        getPrimaryPanel().getCounts();
                    }
                };
                t.start();
            } catch (Exception iex) {
                iex.printStackTrace();
            }
        }

    }

    // May 2017 - Get SNP Counts of Top Two Sequences
    protected class GetSNPCountsAction extends AbstractAction {

        public GetSNPCountsAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            try {
                FeaturedSequence[] sequences = getPrimaryPanel().getVisibleSequences();
                if (sequences == null || sequences.length < 2) {
                    UITools.showWarning("At least two sequences must be visible.", null);
                    return;
                }
                Thread t = new Thread() {
                    public void run() {
                        getPrimaryPanel().getSNPCounts();
                    }
                };
                t.start();
            } catch (Exception iex) {
                iex.printStackTrace();
            }
        }
    }

    // Feb 2012 - Find virus unique positions that are not gap
    protected class GetUniquePositionsAction extends AbstractAction {

        public GetUniquePositionsAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            try {
                FeaturedSequence[] sequences = getPrimaryPanel().getVisibleSequences();
                if (sequences == null || sequences.length < 2) {
                    UITools.showWarning("At least one sequence must be visible.", null);
                    return;
                }
                Thread t = new Thread() {
                    public void run() {
                        getPrimaryPanel().getUniquePositions();
                    }
                };
                t.start();
            } catch (Exception iex) {
                iex.printStackTrace();
            }
        }

    }

    // Feb 2012 - Find virus unique positions that are not gap
    protected class GetUniquePositionsMatrixAction extends AbstractAction {

        public GetUniquePositionsMatrixAction() {

        }

        public void actionPerformed(ActionEvent ev) {
            Thread t = new Thread() {
                public void run() {
                    getPrimaryPanel().getUniquePositionsMatrix();
                }
            };
            t.start();
        }

    }


    // Feb 2012 - Remove Comments of a sequence
    // Allows user to remove all comments from selected sequences
    protected class RemoveAllCommentsAction extends AbstractAction {

        public RemoveAllCommentsAction() {

        }

        public void actionPerformed(ActionEvent ev) {

            FeaturedSequence[] seqs = getSelectedSequences();

            for (int i = 0; i < seqs.length; ++i) {
                Iterator<StrandedFeature> features = seqs[i].features();
                List<StrandedFeature> toDelete = new ArrayList<StrandedFeature>();
                while (features.hasNext()) {
                    StrandedFeature feature = features.next();
                    boolean delete = true;

                    if (feature == null) {
                        delete = false;
                    } else {
                        // validate that it's a comment
                        delete = feature.getType().equals(FeatureType.COMMENT);
                    }

                    if (delete) {
                        try {
                            if (seqs[i].containsFeature(feature)) {
                                toDelete.add(feature);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                //can't delete during iteration - throws exception - so save in toDelete and remove after
                for (int j = 0; j < toDelete.size(); j++) {
                    try {
                        seqs[i].removeFeature(toDelete.get(j));
                    } catch (ChangeVetoException e) {
                        e.printStackTrace();
                    } catch (BioException e) {
                        e.printStackTrace();
                    }
                }
            }
            UndoHandler.getInstance().setModified(true);
            getPrimaryPanel().refreshState();
        }

    }

    // Apr 2012 - Integrates functionality into BBB
    // Spawns new window with edited sequences
    protected class SnipSelectedRegionsAction extends AbstractAction {
        private boolean halt = false;
        private Integer threshold = null;

        public SnipSelectedRegionsAction() {

        }

        public void actionPerformed(ActionEvent e) {
            Thread t = new Thread("Open in new BBB") {
                public void run() {
                    // Ask for threshold, break if user clicks cancel
                    threshold = askForThreshold();
                    if (threshold == null)
                        return;

                    // Save current sequences to temporary snipin.bbb
                    try {
                        notebook = nb_frame.getText();
                    } catch (NullPointerException e) {
                    }
                    saveSequences("tempin.bbb");

                    File f = new File("tempin.bbb");
                    // Run bsnip.py
                    runBsnip(f);

                    // Open new window with results
                    if (!halt)
                        openBBBWindow();

                    // Cleanup
                    if (f.exists() && f.isFile())
                        f.delete();
                    File g = new File("tempout.bbb");
                    if (g.exists() && g.isFile())
                        g.delete();
                }

                private Integer askForThreshold() {
                    Integer[] choices = {1, 2, 3, 4, 5};
                    String message = "Multiple BBB Windows are experimental and not all features will work." + "\nThis will open a new BBB window with the Snipped sequences.\n\nChoose Threshold (1-5):";
                    return (Integer) JOptionPane.showInputDialog(null, message, "Snip Threshold", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
                }

                private void runBsnip(File inputf) {
                    try {
                        String fileContents = new Scanner(inputf).useDelimiter("\\Z").next();
                        //runs bsnip.py on AppServer (uses bsnip.py website version)
                        String result = BioMessages.sendBSnipMessage(DiffEditor.context.getMessageService(), fileContents, threshold);
                        File outputf = new File("tempout.bbb");
                        BufferedWriter bw = new BufferedWriter(new FileWriter(outputf));
                        bw.write(result, 0, result.length());
                        bw.close();
                        System.out.println("Snipping done");
                    } catch (Exception e) {
                        String cause = e.getMessage();
                        System.out.println(cause);
                        UITools.showWarning("bsnip.py script may be missing or broken.\nUnable to complete snipping operation.", getContentPane());
                        halt = true;
                    }
                }

                private void openBBBWindow() {
                    DiffEditorFrame df = spawnNewWindow();

                    df.snip = true;
                    boolean rc = df.loadSequences();
                    System.out.println(rc);
                    df.snip = false;

                    df.setSavable(true);
                    df.setWorkFilename(threshold + " - Snipped");
                    df.refreshTitle();
                    df.setProcess(m_process);
                    df.setSize(DiffEditorFrame.DEFAULT_SIZE);
                    df.setVisible(true);
                }
            };

            t.start();
        }
    }
}
