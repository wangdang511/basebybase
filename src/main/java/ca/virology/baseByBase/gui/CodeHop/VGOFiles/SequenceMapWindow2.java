package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

import ca.virology.lib.util.common.Logger;

import ca.virology.vgo.data.*;
import ca.virology.vgo.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;


/**
 * This is the block and primer display lower panel screen. It has been
 * adapted from a vgo class, so a lot of code is similar structurally.
 * Most of the 'action' occurs
 * here.  This can display sequence frames with different properties, etc. on
 * the screen and allows for selection and selection listening.
 *
 * @author Ryan Brodie
 * @date July 8, 2002
 */
public class SequenceMapWindow2 extends AbstractSequenceDisplay {


    /**
     * default title of this window
     */
    public static final String WINDOW_TITLE = "Sequence Map Display";
    public static final int WINDOW_HEIGHT = 800;

    protected static final int NONE = -1;

    protected static final double SCALE_FACTOR = 100000;
    protected static final int HILITE_WIDTH = 3;
    protected static final int MAX_ORGS = 100;

    // list of sequence panels
    protected final java.util.Map m_seqPanelWrappers = new HashMap();
    protected int m_activeOrg;

    // swing components
    protected Container c = new Container();
    protected final JPanel m_tbPane = new JPanel();
    protected final JPanel m_mainPane = new JPanel();
    protected final JPanel m_btnPane = new JPanel();

    protected final StatusBar m_status = new StatusBar();

    protected JComponent m_splits = null;
    protected JButton m_close = null;
    protected JCheckBox m_famHighlight = null;
    protected JCheckBox m_lcsMatchHighlight = null;

    // global actions (menu and toolbar actions)

    protected ActionListener m_genSubseqListener;


    /**
     * This is the parent selector object that broadcasts all selections.
     * This is used to capture selections in objects created by this window
     * without creating neeldess dependancies.
     */
    protected Selectable m_selectionSource = null;


    /**
     * Constructor for the SequenceMapWindow object
     */
    public SequenceMapWindow2() {
        super();
        setVisible(false);
    }


    public JPanel getM_mainPane() {
        return m_mainPane;
    }

    /**
     * Constructor for the SequenceMapWindow object
     *
     * @param organisms list of organism objects to be rendered to the screen
     * @throws OrganismException if an error occurs adding the exception
     */
    public SequenceMapWindow2(java.util.List organisms, Selectable parentSelector) throws OrganismException {
        this();
        //setVisible(false);
        if (parentSelector == null) {
            m_selectionSource = this;
        } else {
            m_selectionSource = parentSelector;
        }
        if (organisms.size() > MAX_ORGS) {
            throw new OrganismException("Organism Limit of " + MAX_ORGS +
                    " exceeded");
        }
        // If there is only one organism to add, and it is the first one added,
        // then let it use the default viewing options.
        boolean useDefaultView = false;
        if (m_seqPanelWrappers.keySet().size() == 0 && organisms.size() == 1) {
            useDefaultView = true;
        }


        for (Iterator it = organisms.iterator(); it.hasNext(); ) {
            addOrganism((Organism) it.next(), useDefaultView);
        }
        initSwingUI();
        readjustPanels();
        pack();
        setSize(700, 200);
        setTitle((organisms.size() > 1) ? "Multiple " + WINDOW_TITLE : WINDOW_TITLE);
        ca.virology.vgo.gui.Utils.positionCenter(this);
        setVisible(true);

        Iterator i = m_seqPanelWrappers.keySet().iterator();
        while (i.hasNext()) {
            SequencePanelHolder2 sp = (SequencePanelHolder2) m_seqPanelWrappers.get(i.next());
            sp.setScale(7);
        }
    }


    /**
     * Adds a feature to the Organism attribute of the SequenceMapWindow object
     *
     * @param o              The feature to be added to the Organism attribute
     * @param useDefaultView If <CODE>true</CODE> then use the default viewing options
     *                       for this organism, otherwise use the minimal viewing options
     * @return true if the organism can be added
     */
    public synchronized boolean addOrganism(final Organism o, boolean useDefaultView) {
        boolean retval = super.addOrganism(o);
        final SequencePanelHolder2 pane = new SequencePanelHolder2(o, useDefaultView);

        // Forward all selections to the listeners of this window
        pane.getSeqPanel().addSelectionListener(new SelectionListener() {

            public void selection(SelectionEvent ev) {

                selectionNotify(ev);
                m_status.setText(ev.getDescription());

                // try getting the target.
                // get the family for the gene selected
                // try to send new highlight maps to each sequence panel

                if (ev.getTarget() instanceof Feature) {

                    Feature f = (Feature) ev.getTarget();

                    /*
                     * Feature type: GENE
                     */
                    if (f.getType().equals("gene")) {

                        Annotation a = f.getAnnotation();
                        Integer id = (Integer) a.getProperty("family");
                        if (id != null && m_famHighlight.isSelected()) {
                            int fid = id.intValue();
                            java.util.List family;
                            try {
                                family = FamilyTools.getFamilyGeneIDs(fid);
                            } catch (DatasourceException ex) {
                                Logger.log(ex);
                                family = new ArrayList();
                            }
                            Map m = new HashMap();
                            Map highlight = new HashMap();
                            for (Iterator i = family.iterator(); i.hasNext(); ) {
                                Object o = i.next();
                                m.put(o, Color.red);
                                highlight.put(o, Color.yellow);
                            }
                            for (Iterator i = m_seqPanelWrappers.keySet().iterator(); i.hasNext(); ) {
                                SequencePanelHolder2 sp = (SequencePanelHolder2) m_seqPanelWrappers.get(i.next());
                                sp.getSeqPanel().setGeneColorMap(m);
                                sp.getSeqPanel().setGeneHighlightMap(highlight);
                            }
                        } else {
                            pane.getSeqPanel().setGeneColorMap(new HashMap());
                            pane.getSeqPanel().setGeneHighlightMap(new HashMap());
                        }

                    }
                }
            }
        });

        MouseListener l = new MouseAdapter() {
            public void mouseReleased(MouseEvent ev) {
                setActiveOrganism(o.getID());
            }
        };

        pane.getSeqPanel().addMouseListener(l);
        pane.addMouseListener(l);

        pane.setBorder(BorderFactory.createEmptyBorder(
                HILITE_WIDTH,
                HILITE_WIDTH,
                HILITE_WIDTH,
                HILITE_WIDTH));

        m_seqPanelWrappers.put(new Integer(o.getID()), pane);
        setActiveOrganism(o.getID());

        SelectionEvent ev = new SelectionEvent(
                0,
                0,
                StrandedFeature.POSITIVE,
                o.getID(),
                o.getDbName(),
                null,
                ""
        );

        selectionNotify(ev);

        return retval;

    }


    /**
     * Removes an organism from the list
     *
     * @param o The organism to remove
     * @return true if the organism could be properly removed, false otherwise
     */
    public synchronized boolean removeOrganism(Organism o) {
        boolean retval = super.removeOrganism(o);
        m_seqPanelWrappers.remove(new Integer(o.getID()));
        m_seqPanelWrappers.remove(new Integer(o.getID()));

        if (m_seqPanelWrappers.size() == 0) {
            this.close();
            return retval;
        }

        try {
            ca.virology.vgo.gui.Utils.invoke(
                    new Runnable() {
                        public void run() {
                            readjustPanels();
                            setVisible(false);
                            pack();
                            setVisible(true);
                        }
                    }
            );
        } catch (Exception ex) {
            ca.virology.vgo.gui.Utils.showError("Error removing organism: " + ex.getMessage());
            Logger.log(ex);
        }
        setActiveOrganism(NONE);
        return retval;
    }


    /**
     * main layout / positioning initializer
     */
    protected void initSwingUI() {
        // init the main layout of the window /////////////
        c = getContentPane();
        c.setLayout(new java.awt.BorderLayout());


        JScrollPane scroll = new JScrollPane(m_mainPane);
        // winsys handles outer borders itself, remove JScrollPane's border
        scroll.setBorder(null);

        c.add(m_tbPane, java.awt.BorderLayout.NORTH);
        c.add(scroll, java.awt.BorderLayout.CENTER);

        JPanel bottom = new JPanel(new java.awt.BorderLayout());
        bottom.add(m_btnPane, java.awt.BorderLayout.NORTH);
        bottom.add(m_status, java.awt.BorderLayout.SOUTH);

        c.add(bottom, java.awt.BorderLayout.SOUTH);

        m_mainPane.setLayout(new java.awt.BorderLayout());
        m_tbPane.setLayout(new BoxLayout(m_tbPane, BoxLayout.X_AXIS));
        m_btnPane.setLayout(new BoxLayout(m_btnPane, BoxLayout.X_AXIS));

        m_btnPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        ///////////////////////////////////////////////////


        m_close = new VGOButton("Close");
        m_famHighlight = new JCheckBox("Auto-highlight Related Genes");
        m_lcsMatchHighlight = new JCheckBox("Auto-highlight Common Substrings");

    }


    /**
     * This adds the sequence panels to the screen. Depending on how many there
     * are, it adds them to splitpanes as neces1sary.
     *
     * @author Ryan Brodie
     */
    protected void readjustPanels() {
        if (m_splits != null) {
            m_mainPane.remove(m_splits);
        }

        if (m_seqPanelWrappers.size() == 0) {
            return;
        } else if (m_seqPanelWrappers.size() == 1) {
            Iterator i = m_seqPanelWrappers.keySet().iterator();
            JComponent c = (JComponent) (m_seqPanelWrappers.get(i.next()));
            c.setMinimumSize(new Dimension(0, 0));
            m_mainPane.add(c, java.awt.BorderLayout.CENTER);
            m_splits = c;
            return;
        }

        Iterator it = m_seqPanelWrappers.keySet().iterator();
        JSplitPane arr[] = new JSplitPane[m_seqPanelWrappers.size()];
        for (int i = 0; i < m_seqPanelWrappers.size(); i++) {
            JComponent c = (JComponent) (m_seqPanelWrappers.get(it.next()));
            c.setMinimumSize(new Dimension(500, 100));
            arr[i] = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
            arr[i].setLeftComponent(c);
            arr[i].setOneTouchExpandable(true);
        }
        for (int i = m_seqPanelWrappers.size() - 1; i > 1; i--) {
            arr[i - 1].setBorder(null);
            arr[i].setRightComponent(arr[i - 1]);
        }
        arr[1].setRightComponent(arr[0].getLeftComponent());
        m_mainPane.add(arr[m_seqPanelWrappers.size() - 1], java.awt.BorderLayout.CENTER);
        m_splits = arr[m_seqPanelWrappers.size() - 1];
        ((JSplitPane) m_splits).resetToPreferredSizes();
    }


    /**
     * This method sets the active organism to the one indicated. It also puts
     * a line border around the appropriate frame to indicate this state.
     *
     * @param newOrg the organdism id for the newly activated panel
     */
    public void setActiveOrganism(int newOrg) {
        m_activeOrg = newOrg;

        Iterator i = m_seqPanelWrappers.keySet().iterator();
        while (i.hasNext()) {
            JComponent other = (JComponent) m_seqPanelWrappers.get(i.next());
            other.setBorder(
                    BorderFactory.createEmptyBorder(
                            HILITE_WIDTH,
                            HILITE_WIDTH,
                            HILITE_WIDTH,
                            HILITE_WIDTH));
        }

        if (newOrg != NONE) {

            Organism o = (Organism) m_organisms.get(new Integer(newOrg));
            m_status.setText(o.getName() + " Now Active");
            if (m_genSubseqListener != null) {
                ((GetSequenceAction) m_genSubseqListener).setOrganism(o);
            }

        } else {
            // ???
        }


    }
}
