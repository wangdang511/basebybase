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

import ca.virology.baseByBase.io.*;

import java.awt.*;
import java.awt.event.*;

import java.io.IOException;

import java.util.*;

import javax.swing.*;


/**
 * This panel lets users select a mapping from their genome alignments onto
 * sequences in the database.
 *
 * @author $author$
 * @version $Revision: 1.2 $
 */
public class PopulateGenesPanel
        extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected FeaturedSequence[] m_seqs;
    protected Map m_idMap;
    protected final Map m_selectedMap = new HashMap();
    protected String m_dbName;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new PopulateGenesPanel object.
     *
     * @param seqs the sequences to populate
     * @throws IOException if there's an io problem
     */
    public PopulateGenesPanel(FeaturedSequence[] seqs, String dbName)

    {
        m_seqs = seqs;
        m_dbName = dbName;


        m_idMap = VocsTools.getVirusIDMap(m_dbName);

        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * actually put the data into the sequences
     */
    public void confirmSequences() {
        Iterator i = m_selectedMap.keySet().iterator();

        HashMap status = new HashMap();

        while (i.hasNext()) {
            FeaturedSequence seq = (FeaturedSequence) i.next();
            int val = ((Integer) m_selectedMap.get(seq)).intValue();

            if (getName(val).equals("None")) {
                val = -1;
            }

            try {
                if (val >= 0) {
                    VocsTools.setupSequence(val, seq, m_dbName);
                    status.put(seq, "Import Successful!");
                }
            } catch (Exception ex) {
                status.put(
                        seq,
                        ex.getMessage());

                //ex.printStackTrace();
            }
        }

        if (status.size() > 0) {
            JPanel p = new JPanel(new BorderLayout());
            JPanel lp = new JPanel(new VerticalFlowLayout());
            JPanel rp = new JPanel(new VerticalFlowLayout());

            p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder("Import Status")));
            lp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

            p.add(lp, BorderLayout.WEST);
            p.add(rp, BorderLayout.CENTER);

            Iterator j = status.keySet().iterator();

            while (j.hasNext()) {
                FeaturedSequence s = (FeaturedSequence) j.next();
                String m = (String) status.get(s);

                lp.add(new JLabel(s.getName()));
                rp.add(new JLabel(m));
            }

            JOptionPane.showMessageDialog(null, p);
        }
    }

    /**
     * init the gui
     */
    protected void initUI() {
        setLayout(new BorderLayout());

        JLabel desc = new JLabel("<html>Below is a list of each sequence marked<br>" +
                "in this alignment along with the sequence it is <br>" +
                "matched with in the current database.  If there is no<br>" +
                "match given, or the match given is incorrect, click the <br>" +
                "button to the right to select from a list of sequences <br>" +
                "in the database. All sequences selected here should be <br>" +
                "in the same database.</html>");
        desc.setBorder(BorderFactory.createTitledBorder("Instructions"));

        JPanel body = new JPanel(new GridLayout(m_seqs.length + 1, 2));
        body.add(new JLabel("Alignment Sequence"));
        body.add(new JLabel("Database Organism"));

        for (int i = 0; i < m_seqs.length; ++i) {
            final FeaturedSequence mySeq = m_seqs[i];
            JLabel nlab = new JLabel(m_seqs[i].getName());
            JButton mbut = new JButton(getName(m_seqs[i].getId()));

            // attempt to automatically select correct gene from feature file/vocs
            // search through the id map for a key that the feature file name contains as a substring
            m_selectedMap.put(m_seqs[i], new Integer(m_seqs[i].getId()));
            for (Iterator it = m_idMap.keySet().iterator(); it.hasNext(); ) {
                String key = it.next().toString();
                if (m_seqs[i].getName().contains(key)) {
                    m_selectedMap.put(m_seqs[i], new Integer((Integer) m_idMap.get(key)));
                    mbut.setText(key);
                }
            }
            mbut.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent ev) {
                            JButton b = (JButton) ev.getSource();
                            DbSequenceSelectPanel selPan = new DbSequenceSelectPanel(mySeq, m_idMap);
                            int val = JOptionPane.showConfirmDialog(null, selPan,
                                    "Select Sequence", JOptionPane.OK_CANCEL_OPTION);

                            if (val == JOptionPane.OK_OPTION) {
                                b.setText(getName(selPan.getSelectedID()));
                                m_selectedMap.put(mySeq, new Integer(selPan.getSelectedID()));
                            }
                        }
                    });

            body.add(nlab);
            body.add(mbut);
        }

        add(desc, BorderLayout.NORTH);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setPreferredSize(new Dimension(500, 300));
        bodyScroll.setBorder(BorderFactory.createTitledBorder("Sequences"));

        add(bodyScroll, BorderLayout.CENTER);
    }

    /**
     * get the id for a given virus -- from the local map
     *
     * @param vname the virus
     * @return the id
     */
    protected int getId(String vname) {
        if (m_idMap.containsKey(vname)) {
            Integer i = (Integer) m_idMap.get(vname);

            return i.intValue();
        } else {
            return -1;
        }
    }

    protected void autoSelectMappings() {
        System.out.println("m_idMap key/values: " + m_idMap.toString());
        System.out.println("key set" + m_idMap.keySet().toString());
        for (int i = 0; i < m_seqs.length; ++i) {
            final FeaturedSequence mySeq = m_seqs[i];

            System.out.println("m_seqs[i].getId(): " + m_seqs[i].getId());


            if (m_idMap.containsKey(m_seqs[i].getName())) {
                m_selectedMap.put(m_seqs[i], m_seqs[i].getId());
                System.out.println("mapping found");
            } else
                System.out.println("no mapping found");
        }

    }

    /**
     * get the name for a virus id
     *
     * @param vid the virus
     * @return the name
     */
    protected String getName(int vid) {
        if (m_idMap.containsValue(new Integer(vid))) {
            Iterator i = m_idMap.keySet()
                    .iterator();

            while (i.hasNext()) {
                String name = (String) i.next();
                Integer val = (Integer) m_idMap.get(name);

                if (val.intValue() == vid) {
                    return name;
                }
            }
        } else {
            return "None";
        }

        return "None";
    }
}
