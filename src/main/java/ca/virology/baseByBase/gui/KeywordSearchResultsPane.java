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

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import ca.virology.lib.util.gui.*;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * @author asyed
 */

public class KeywordSearchResultsPane extends JFrame {

    protected PrimaryPanel m_primary;
    protected Vector m_locations;
    protected Vector m_names;
    protected String m_keywords;
    protected Vector m_comments;
    protected Vector m_strand;
    protected String m_selected;

    protected JScrollPane topPane;
    protected JPanel southPanel;
    protected JTable hitsTable;
    protected JButton closeButton;
    protected JButton showPrimer;

    protected boolean m_type;

    public KeywordSearchResultsPane(PrimaryPanel pp, Vector loc, Vector name, Vector comments, Vector strand, String keywords, boolean type) {
        m_primary = pp;
        m_locations = loc;
        m_names = name;
        m_keywords = keywords;
        m_comments = comments;
        m_strand = strand;
        m_type = type;
        initComponents();
        otherInit();
        setVisible(true);
    }

    public void initComponents() {

        if (m_type) {
            setTitle("Primer Keyword Search Hits for " + m_keywords);
        } else {
            setTitle("Comment Keyword Search Hits for " + m_keywords);
        }

        topPane = new JScrollPane();

        hitsTable = new JTable();

        DefaultTableModel dtm = new DefaultTableModel(0, 5) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (m_type) {
            dtm.setColumnIdentifiers(new Object[]{"Keyword", "Primer Name", "Organism Name", "Comments", "Position", "Strand"});
        } else {
            dtm.setColumnIdentifiers(new Object[]{"Keyword", "Comment Name", "Organism Name", "Comments", "Position", "Strand"});
        }
        hitsTable.setModel(dtm);
        hitsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fillTable(dtm);

        topPane.setPreferredSize(new Dimension(800, 600));
        topPane.setViewportView(hitsTable);
        getContentPane().add(topPane, BorderLayout.NORTH);

        //SOUTH
        southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        if (m_type) {
            showPrimer = new JButton("Show Primer");
            showPrimer.addActionListener(new ShowFeatureAction());
        } else {
            showPrimer = new JButton("Show Comment");
            showPrimer.addActionListener(new ShowFeatureAction());
        }

        closeButton = new JButton("Close");
        closeButton.addActionListener(new CloseAction());
        southPanel.add(showPrimer);
        southPanel.add(closeButton);

        getContentPane().add(southPanel, BorderLayout.SOUTH);

        pack();
    }

    public void otherInit() {
        hitsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getButton() == me.BUTTON1 && me.getClickCount() == 2) {
                    int row = hitsTable.rowAtPoint(me.getPoint());

                    if (row >= 0) {
                        hitsTable.getSelectionModel().setSelectionInterval(row, row);
                        m_selected = (String) hitsTable.getValueAt(row, 4);
                    }
                    showFeature();
                }
                if (me.getButton() == me.BUTTON1 && me.getClickCount() == 1) {
                    int row = hitsTable.rowAtPoint(me.getPoint());

                    if (row >= 0) {
                        hitsTable.getSelectionModel().setSelectionInterval(row, row);
                        m_selected = (String) hitsTable.getValueAt(row, 4);
                    }
                }
            }
        });
    }


    public void fillTable(DefaultTableModel dtm) {
        Vector loc = m_locations;
        Vector name = m_names;

        for (int i = 0; i < loc.size(); i++) {
            Location l = (Location) loc.get(i);
            String location = l.getMin() + "->" + l.getMax();
            StringTokenizer st = new StringTokenizer((String) name.get(i), ";");
            String comments = (String) m_comments.get(i);
            String strand = (String) m_strand.get(i);

            String key = st.nextToken();
            String organism = st.nextToken();
            String name_feature = st.nextToken();

            dtm.addRow(new Object[]{key, name_feature, organism, comments, location, strand});

        }
    }

    public void closeAction() {
        this.setVisible(false);
        this.dispose();
        return;
    }

    public void showFeature() {
        if (m_selected == null) {
            UITools.showWarning("Select keyword you want to browse before performing this operation", null);
            return;
        }

        StringTokenizer st1 = new StringTokenizer(m_selected, "->");
        int start = Integer.parseInt(st1.nextToken());
        m_primary.scrollToLocation(start);
    }

    //INNER CLASS
    public class CloseAction extends AbstractAction {
        public CloseAction() {
        }

        public void actionPerformed(ActionEvent ae) {
            closeAction();
        }
    }

    public class ShowFeatureAction extends AbstractAction {
        public ShowFeatureAction() {

        }

        public void actionPerformed(ActionEvent ae) {
            showFeature();
        }
    }

}
