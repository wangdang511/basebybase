package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.io.tools.SequenceTools;
import ca.virology.lib.search.SearchHit;
import ca.virology.lib.search.SearchHitComparator;
import ca.virology.lib.util.common.SequenceUtility;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

/**
 * This panel displays a list of search results run over a group of sequences.
 *
 * @author Ryan Brodie
 * @version $Revision: 1.2 $
 */
public class SearchResultPanel extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected final PrimaryPanel m_primary;
    protected final String m_searchTerm;
    protected final Map m_posResults;
    protected final Map m_negResults;
    protected final ArrayList m_models = new ArrayList();
    protected Comparator m_curComparator = null;
    protected boolean m_ascending = true;
    protected boolean m_fuzzySearch = false;
    protected String m_name = null;
    protected JList pList;
    protected JList nList;

    protected Map jplist = new Hashtable();
    protected Map jnlist = new Hashtable();

    final JLabel resultAlign = new JLabel();
    JPanel infoPane;
    JPanel top;
    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new SearchResultPanel object.
     *
     * @param ppane             The primary panel to tie scroll requests to
     * @param search            The search term
     * @param posResults        all positive result hits
     * @param negResults        all negative result hits
     * @param fuzzy             a boolean indicating if it was a fuzzy search
     */
    public SearchResultPanel(PrimaryPanel ppane, String search, Map posResults, Map negResults, boolean fuzzy) {

        m_searchTerm = search;
        m_fuzzySearch = fuzzy;
        m_primary = ppane;
        m_posResults = posResults;
        m_negResults = negResults;
        m_curComparator = new SearchHitComparator(SearchHitComparator.BY_LOCATION);
        initUI();
    }


    public SearchResultPanel(PrimaryPanel ppane, String search, Map posResults, Map negResults, boolean fuzzy, String primerName) {

        m_searchTerm = search;
        m_fuzzySearch = fuzzy;
        m_primary = ppane;
        m_posResults = posResults;
        m_negResults = negResults;
        m_name = primerName;
        m_curComparator = new SearchHitComparator(SearchHitComparator.BY_LOCATION);
        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * sort the lists by location
     */
    protected void sortByLocation() {
        m_curComparator = new SearchHitComparator(SearchHitComparator.BY_LOCATION);
        sort(m_ascending);
    }

    /**
     * sort the lists by confidence
     */
    protected void sortByConfidence() {
        m_curComparator = new SearchHitComparator(SearchHitComparator.BY_CONFIDENCE);
        sort(m_ascending);
    }

    /**
     * sort the lists
     *
     * @param ascending if true, the lists will be sorted ascending, otherwise
     *                  they'll be sorted descending
     */
    protected void sort(boolean ascending) {
        m_ascending = ascending;

        for (int i = 0; i < m_models.size(); ++i) {
            DefaultListModel mod = (DefaultListModel) m_models.get(i);
            javax.swing.event.ListDataListener[] ldl = mod.getListDataListeners();

            for (int j = 0; j < ldl.length; ++j) {
                mod.removeListDataListener(ldl[j]);
            }

            Object[] os = mod.toArray();
            Arrays.sort(os, m_curComparator);
            mod.removeAllElements();

            if (m_ascending) {
                for (int j = 0; j < os.length; ++j) {
                    mod.addElement(os[j]);
                }
            } else {
                for (int j = os.length - 1; j >= 0; --j) {
                    mod.addElement(os[j]);
                }
            }

            for (int j = 0; j < ldl.length; ++j) {
                mod.addListDataListener(ldl[j]);
            }

            if (mod.size() > 0) {
                Object o = mod.remove(0);
                mod.add(0, o);
            }
        }
    }

    /*
    * This class method saves a performed fuzzy search.
    * @author asyed
    */
    protected void saveFuzzyInformation() {
        try {
            PrintWriter p = null;
            Set keys = m_posResults.keySet();
            keys.retainAll(m_negResults.keySet());

            JFileChooser jfc = new JFileChooser();
            String[][] extensions = {{"Text Format", "txt", "txt"}};

            if (jfc.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
                File f = jfc.getSelectedFile();
                p = new PrintWriter(new java.io.FileOutputStream(f));
            } else {
                return;
            }

            for (Iterator it = keys.iterator(); it.hasNext(); ) {
                final FeaturedSequence key = (FeaturedSequence) it.next();
                SearchHit[] pos = (SearchHit[]) m_posResults.get(key);
                SearchHit[] neg = (SearchHit[]) m_negResults.get(key);

                String sequence = key.toString();
                p.println("Fuzzy Search Results for the Sequence " + key.getName());
                p.println("\n");
                p.println("Positive Direction Hits (----->):");
                int i = 0;
                for (i = 0; i < pos.length; i++) {
                    int start = pos[i].getStart() + 1;
                    int stop = pos[i].getStop() + 1;
                    double confidence = pos[i].getConfidence();
                    //System.out.println(start+"--"+stop+"--"+confidence+"--"+sequence.substring(start-1,stop));
                    String writeToFile = start + ";" + stop + ";" + sequence.substring(start - 1, stop) + ";" + confidence;
                    p.println(writeToFile);
                }
                p.println("Negative Direction Hits (<-----):");
                i = 0;
                for (i = 0; i < neg.length; i++) {
                    int start = neg[i].getStart() + 1;
                    int stop = neg[i].getStop() + 1;
                    double confidence = neg[i].getConfidence();
                    //System.out.println(start+"--"+stop+"--"+confidence+"--"+sequence.substring(start-1,stop));
                    String writeToFile = start + ";" + stop + ";" + sequence.substring(start - 1, stop) + ";" + confidence;
                    p.println(writeToFile);
                }
                p.println("***********END OF HITS FOR THIS SEQUENCE***********");
                p.println("\n");
            }
            p.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * initialize the gui
     */
    protected void initUI() {
        Set keys = m_posResults.keySet();
        keys.retainAll(m_negResults.keySet());

        JPanel main = new JPanel(new BorderLayout());
        JTabbedPane tab = new JTabbedPane(JTabbedPane.LEFT);

        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            final FeaturedSequence key = (FeaturedSequence) i.next();
            SearchHit[] pos = (SearchHit[]) m_posResults.get(key);
            SearchHit[] neg = (SearchHit[]) m_negResults.get(key);

            DefaultListModel posmod = new DefaultListModel();
            DefaultListModel negmod = new DefaultListModel();

            for (int j = 0; j < pos.length; ++j) {
                posmod.addElement(pos[j]);
            }

            for (int j = 0; j < neg.length; ++j) {
                negmod.addElement(neg[j]);
            }

            pList = new JList(posmod);
            nList = new JList(negmod);

            jplist.put(key, pList);
            jnlist.put(key, nList);


            pList.setFixedCellHeight(21);
            pList.setFixedCellWidth(300);
            nList.setFixedCellHeight(21);
            nList.setFixedCellWidth(300);

            m_models.add(posmod);
            m_models.add(negmod);


            pList.setCellRenderer(new MyCellRenderer(key, m_searchTerm, true, m_fuzzySearch));
            nList.setCellRenderer(new MyCellRenderer(key, m_searchTerm, false, m_fuzzySearch));
            nList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            MouseListener mln = new MouseAdapter() {
                public void mouseClicked(MouseEvent ev) {
                    if (ev.getClickCount() == 2) {
                        JList l = (JList) ev.getSource();
                        if (l.getModel().getSize() > 0) {
                            int index = l.locationToIndex(ev.getPoint());
                            Object o = l.getModel().getElementAt(index);


                            SearchHit loc = (SearchHit) o;
                            m_primary.scrollToLocation(key.getRelativePosition(loc.getStart()));
                            m_primary.setDisplayStrand(StrandedFeature.NEGATIVE);

                        }
                    }
                }
            };
            MouseListener mlp = new MouseAdapter() {
                public void mouseClicked(MouseEvent ev) {
                    if (ev.getClickCount() == 2) {
                        JList l = (JList) ev.getSource();
                        if (l.getModel().getSize() > 0) {
                            int index = l.locationToIndex(ev.getPoint());
                            Object o = l.getModel().getElementAt(index);


                            SearchHit loc = (SearchHit) o;
                            m_primary.scrollToLocation(key.getRelativePosition(loc.getStart()));
                            m_primary.setDisplayStrand(StrandedFeature.POSITIVE);

                        }
                    }
                }
            };

            MouseListener mlpList = new MouseAdapter() {
                public void mouseClicked(MouseEvent ev) {
                    if (ev.getClickCount() == 1) {
                        JList l = (JList) ev.getSource();
                        if (l.getModel().getSize() > 0) {

                            int index = l.locationToIndex(ev.getPoint());
                            Object o = l.getModel().getElementAt(index);
                            SearchHit loc = (SearchHit) o;
                            pList.getSelectedValue();

                            int min = key.getRelativePosition(loc.getStart() + 1);
                            int max = key.getRelativePosition(loc.getStop() + 2);


                            resultAlign.setText("Result Term: '" + SequenceTools.getUngappedBuffer(key.substring(min, max)).toString() + "'");
                            infoPane.repaint();
                        }
                    }
                }
            };

            MouseListener mlnList = new MouseAdapter() {
                public void mouseClicked(MouseEvent ev) {
                    if (ev.getClickCount() == 1) {
                        JList l = (JList) ev.getSource();
                        if (l.getModel().getSize() > 0) {
                            int index = l.locationToIndex(ev.getPoint());
                            try {
                                Object o = l.getModel().getElementAt(index);
                                SearchHit loc = (SearchHit) o;
                                //need to get the reverse complement of the sequence
                                SymbolList targetBioj;
                                try {
                                    int min = key.getRelativePosition(loc.getStart() + 1);
                                    int max = key.getRelativePosition(loc.getStop() + 1);
                                    targetBioj = DNATools.createDNA(SequenceTools.getUngappedBuffer(key.substring(min, max)).toString());
                                    targetBioj = DNATools.reverseComplement(targetBioj);
                                    resultAlign.setText("Result Term: '" + targetBioj.seqString().toUpperCase() + "'");
                                } catch (IllegalSymbolException e) {
                                    e.printStackTrace();
                                } catch (IllegalAlphabetException e) {
                                    e.printStackTrace();
                                }
                                infoPane.repaint();

                            } catch (ArrayIndexOutOfBoundsException aioobe) {
                                aioobe.printStackTrace();
                            }
                        }
                    }
                }
            };

            pList.addMouseListener(mlpList);
            nList.addMouseListener(mlnList);

            pList.addMouseListener(mlp);
            nList.addMouseListener(mln);

            JPanel respan = new JPanel();
            respan.setLayout(new BoxLayout(respan, BoxLayout.X_AXIS));

            JScrollPane pScroll = new JScrollPane(pList);
            JScrollPane nScroll = new JScrollPane(nList);

            pScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Top"), pScroll.getBorder()));
            nScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Bottom"), nScroll.getBorder()));

            respan.add(pScroll);
            respan.add(Box.createHorizontalStrut(5));
            respan.add(nScroll);

            String s = key.getName();

            if (s.length() > 10) {
                s = s.substring(0, 10) + "...";
            }

            if (m_name == null) {
                tab.add(respan, key.getName());
            } else {
                tab.add(respan, m_name);
            }
            respan.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        }

        JPanel sortStyle = new JPanel();
        sortStyle.setLayout(new BoxLayout(sortStyle, BoxLayout.X_AXIS));
        sortStyle.setBorder(BorderFactory.createTitledBorder("Sorting Style"));

        ButtonGroup ascgrp = new ButtonGroup();
        final JRadioButton rbasc = new JRadioButton("Ascending");
        final JRadioButton rbdes = new JRadioButton("Descending");
        ascgrp.add(rbasc);
        ascgrp.add(rbdes);

        if (m_fuzzySearch) {
            ButtonGroup sortgrp = new ButtonGroup();
            final JRadioButton byloc = new JRadioButton("Location");
            final JRadioButton bycon = new JRadioButton("Confidence");
            sortgrp.add(byloc);
            sortgrp.add(bycon);
            byloc.setSelected(false);
            bycon.setSelected(true);
            rbasc.setSelected(false);
            rbdes.setSelected(true);
            m_ascending = false;
            sortByConfidence();
            sortStyle.add(new JLabel("Sort List By: "));
            sortStyle.add(Box.createHorizontalStrut(5));
            sortStyle.add(byloc);
            sortStyle.add(Box.createHorizontalStrut(5));
            sortStyle.add(bycon);

            ActionListener al = new ActionListener() {
                public void actionPerformed(final ActionEvent ev) {
                    new Thread() {
                        public void run() {
                            if (byloc.isSelected()) {
                                sortByLocation();
                            } else {
                                sortByConfidence();
                            }
                        }
                    }.start();
                }
            };

            byloc.addActionListener(al);
            bycon.addActionListener(al);
        } else {
            sortStyle.add(new JLabel("Sort List By Location, "));
            m_ascending = true;
            sortByLocation();
            rbasc.setSelected(true);
            rbdes.setSelected(false);
        }

        sortStyle.add(Box.createHorizontalStrut(15));
        sortStyle.add(new JLabel("Sort List: "));
        sortStyle.add(Box.createHorizontalStrut(5));
        sortStyle.add(rbasc);
        sortStyle.add(Box.createHorizontalStrut(5));
        sortStyle.add(rbdes);
        sortStyle.add(Box.createHorizontalGlue());

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                new Thread() {
                    public void run() {
                        sort(rbasc.isSelected());
                    }
                }.start();
            }
        };

        rbasc.addActionListener(al);
        rbdes.addActionListener(al);


        JLabel searchTerm = new JLabel("Search Term: '" + m_searchTerm + "'");
        //set the font for the sequence channels
        resultAlign.setText("");
        Font monofont = new Font("Monospaced", Font.PLAIN, 15);
        resultAlign.setFont(monofont);
        searchTerm.setFont(monofont);


        infoPane = new JPanel();
        infoPane.setLayout(new BoxLayout(infoPane, BoxLayout.Y_AXIS));
        infoPane.add(searchTerm);
        infoPane.add(resultAlign);
        infoPane.add(new JLabel(""));//spacer
        infoPane.add(new JLabel("Sequences Searched: " + m_posResults.size()));
        infoPane.add(Box.createHorizontalGlue());
        infoPane.add(Box.createHorizontalStrut(5));
        infoPane.setBorder(BorderFactory.createTitledBorder("Search Info"));

        top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(infoPane);
        top.setVisible(true);
        top.add(sortStyle);
        /*if(m_fuzzySearch){
            JMenuBar mainMenuBar = new JMenuBar();
			JMenu file = new JMenu("File");
			JMenuItem save = new JMenuItem("Save Fuzzy Results");
			save.addActionListener(new SaveFuzzyAction());
			file.add(save);
			mainMenuBar.add(file);
			top.add(mainMenuBar);
		}*/
        main.add(top, BorderLayout.NORTH);
        main.add(tab, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(main, BorderLayout.CENTER);

    }

    //~ Inner Classes //////////////////////////////////////////////////////////
    class MyCellRenderer extends JLabel implements ListCellRenderer {
        protected SearchHit latest = null;
        protected FeaturedSequence seqs;
        protected String querySeq;
        protected boolean positiveStrand;
        protected boolean fuzzySearch;

        public MyCellRenderer(FeaturedSequence m_seq, String m_searchTerm, boolean positive, boolean fuzzy) {
            seqs = m_seq;
            querySeq = m_searchTerm;
            positiveStrand = positive;
            fuzzySearch = fuzzy;
        }

        public Component getListCellRendererComponent(JList list, Object value, // value to display
                                                      int index, // cell index
                                                      boolean isSelected, // is the cell selected
                                                      boolean cellHasFocus) // the list and the cell have the focus
        {


            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());

            latest = (SearchHit) value;

            setOpaque(true);

            return this;
        }

        public Dimension getPreferredSize() {
            return new Dimension(300, 21);
        }

        public void paintComponent(Graphics g) {
            if (latest == null) {
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, 300, 21);

            FontMetrics fm = getFontMetrics(getFont());
            int textHeight = (fm.getHeight() / 2) + 11;

            java.text.DecimalFormat df = new java.text.DecimalFormat();
            df.setMaximumFractionDigits(0);

            String conf = df.format((100.0 * latest.getConfidence())) + "%";

            int graphWidth = 100;

            if (!positiveStrand) {
                g2d.setFont(getFont());
                g2d.setColor(getForeground());
                g2d.drawString(conf, 3, textHeight);
                g2d.drawString((latest.getStart() + 1) + " -> " +
                        (latest.getStop()), 150, textHeight);
            } else {
                g2d.setFont(getFont());
                g2d.setColor(getForeground());
                g2d.drawString(conf, 3, textHeight);
                g2d.drawString((latest.getStart() + 1) + " -> " +
                        (latest.getStop() + 1), 150, textHeight);
            }

            if (querySeq.length() > graphWidth || !fuzzySearch) {//\
                g2d.setColor(Color.green);
                g2d.fillRect(40, 7, (int) (latest.getConfidence() * 50.0), 7);
                g2d.setColor(Color.red);
                g2d.fillRect(40 + (int) (latest.getConfidence() * 50.0), 7, (50 - ((int) (latest.getConfidence() * 50.0))), 7);
                g2d.setColor(Color.black);
                g2d.drawRect(40, 7, 50, 7);
            } else {
                //any query longer than graphWidth wont work in this format

                int graphUnit = (int) (graphWidth / querySeq.length());
                String targetSeq = "";
                if (!positiveStrand) {
                    // only nucleic acids have two strands, this is dna
                    try {
                        int min = seqs.getRelativePosition(latest.getStart());
                        int max = seqs.getRelativePosition(latest.getStop() + 1);
                        SequenceTools.getUngappedBuffer(seqs.substring(min, max)).toString();
                        targetSeq = SequenceTools.getUngappedBuffer(seqs.substring(min, max)).toString();
                        //targetSeq = seqs.toString().substring(latest.getStart(),latest.getStop());
                        SymbolList targetBioj = DNATools.createDNA(targetSeq);
                        targetBioj = DNATools.reverseComplement(targetBioj);
                        targetSeq = targetBioj.seqString().toUpperCase();
                    } catch (IllegalSymbolException ex) {
                        //this will happen if you try and make the DNA seq using non IUB symbols
                        ex.printStackTrace();
                    } catch (IllegalAlphabetException ex) {
                        //this will happen if you try and reverse complement a non DNA sequence using DNATools
                        ex.printStackTrace();
                    } catch (StringIndexOutOfBoundsException stroob) {
                        System.out.println("StringIndexOutOfBoundsException; " + "max ; " + seqs.getRelativePosition(latest.getStart()) + "min ; " + seqs.getRelativePosition(latest.getStop()) + "seq ; ");
                    }

                } else {
                    // can be amino acid sequence
                    int min = seqs.getRelativePosition(latest.getStart() + 1);
                    int max = seqs.getRelativePosition(latest.getStop() + 2);
                    SequenceTools.getUngappedBuffer(seqs.substring(min, max)).toString();
                    targetSeq = SequenceTools.getUngappedBuffer(seqs.substring(min, max)).toString();
                    //targetSeq = seqs.toString().substring(latest.getStart(),(latest.getStop()+1));
                }

                //remove RX from query sequence
                String processingQuery = "";
                boolean inRX = false;

                for (int i = 0; i < querySeq.length(); i++) {
                    if (querySeq.charAt(i) == '[') {
                        inRX = true;
                    }
                    if (inRX == false) {
                        processingQuery += querySeq.charAt(i);
                    }
                    if (querySeq.charAt(i) == ']') {
                        inRX = false;
                        processingQuery += '*';
                    }
                }

                //update the query
                querySeq = processingQuery;
                //create a set of ambiguous codes which can be searched.
                Set<String> ambiguitySetDNA = new HashSet<String>();
                ambiguitySetDNA.add("R");
                ambiguitySetDNA.add("M");
                ambiguitySetDNA.add("W");
                ambiguitySetDNA.add("V");
                ambiguitySetDNA.add("H");
                ambiguitySetDNA.add("D");
                ambiguitySetDNA.add("N");
                ambiguitySetDNA.add("Y");
                ambiguitySetDNA.add("S");
                ambiguitySetDNA.add("B");
                ambiguitySetDNA.add("K");
                ambiguitySetDNA.add("X");

                Set<String> ambiguitySetAA = new HashSet<String>();
                ambiguitySetAA.add("B");
                ambiguitySetAA.add("J");
                ambiguitySetAA.add("U");
                ambiguitySetAA.add("X");
                ambiguitySetAA.add("Z");

                for (int i = 0; i < querySeq.length(); i++) {
                    //it will be a match if it is a RegEx/Ambiguous search - symbolised by '*'
                    if (querySeq.charAt(i) == targetSeq.charAt(i)) {
                        g2d.setColor(Color.green);
                    } else if (SequenceUtility.isDNA(seqs.toString()) && ambiguitySetDNA.contains("" + querySeq.charAt(i))) {
                        g2d.setColor(Color.orange);
                    } else if (!SequenceUtility.isDNA(seqs.toString()) && ambiguitySetAA.contains("" + querySeq.charAt(i))) {
                        g2d.setColor(Color.orange);
                    } else {
                        g2d.setColor(Color.red);
                    }
                    g2d.fillRect(40 + (i * graphUnit), 7, graphUnit, 7);

                    g2d.setColor(Color.black);
                    g2d.drawRect(40, 7, (querySeq.length()) * graphUnit, 7);
                }
            }
        }
    }

    public List getPSelected(final FeaturedSequence key) {
        //caity - getSelectedValues() is deprecated -> switch to getSelectedValuesList()
//        JList indexedplist = (JList) jplist.get(key);
//        Object[] pSelecteditems = indexedplist.getSelectedValues();
//        List ret = new ArrayList();
//        for (int i = 0; i < pSelecteditems.length; i++) {
//            ret.add(pSelecteditems[i]);
//        }
//        return ret;
        JList indexedplist = (JList) jplist.get(key);
        List pSelecteditems = indexedplist.getSelectedValuesList();
        return pSelecteditems;
    }

    public List getNSelected(final FeaturedSequence key) {
        //caity - getSelectedValues() is deprecated -> switch to getSelectedValuesList()
//        JList indexednlist = (JList) jnlist.get(key);
//        Object[] nSelecteditems = indexednlist.getSelectedValues();
//        List ret = new ArrayList();
//        for (int i = 0; i < nSelecteditems.length; i++) {
//            ret.add(nSelecteditems[i]);
//        }
//        return ret;
        JList indexednlist = (JList) jnlist.get(key);
        List nSelecteditems = indexednlist.getSelectedValuesList();
        return nSelecteditems;
    }
}