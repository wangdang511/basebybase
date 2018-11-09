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

import ca.virology.lib.io.sequenceData.*;

import java.awt.*;

import java.util.*;
import java.util.List;

import javax.swing.*;


/**
 * This panel displays a list of the sequences in the database and allows the
 * user to select one to map on to their selected genome.
 *
 * @author Ryan Brodie
 * @version $Revision: 1.1.1.1 $
 */
public class DbSequenceSelectPanel
        extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected FeaturedSequence m_seq;
    protected JList m_list;
    protected Map m_idMap;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new DbSequenceSelectPanel object.
     *
     * @param seq    The sequence to map
     * @param vidMap the id map of viruses in the db
     */
    public DbSequenceSelectPanel(
            FeaturedSequence seq,
            Map vidMap) {
        m_seq = seq;
        m_idMap = vidMap;

        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * returns the id selected, or -1 if none is selected
     *
     * @return the id selected, or -1 if none is selected
     */
    public int getSelectedID() {
        Object o = m_list.getSelectedValue();

        if (o != null) {
            Object ob = m_idMap.get(o);

            if (ob == null) {
                return -1;
            }

            Integer i = (Integer) ob;
            return i.intValue();
        } else {
            return -1;
        }
    }

    /**
     * init the UI
     */
    protected void initUI() {
        Vector data = new Vector();
        List mapKeys = new ArrayList(m_idMap.keySet());

        Collections.sort(mapKeys, String.CASE_INSENSITIVE_ORDER);

        for (int i = 0; i < mapKeys.size(); i++) {
            data.add(mapKeys.get(i).toString());
        }
        data.add("None");

        m_list = new JList(data);

        JLabel desc = new JLabel("<html>Organism: " + m_seq.getName() + "<br><br>" +
                "Please select an organism from the list below to <br>" +
                "import the genes into this alignment.  If the <br>" +
                "organism you have selected is not in the list below,<br>" +
                "ensure that you are connected to the appropriate <br>" +
                "database, and that the organism you have selected <br>" +
                "is in that database.<br></html>");
        desc.setBorder(BorderFactory.createTitledBorder("Instructions"));

        JPanel lpan = new JPanel(new BorderLayout());
        lpan.setBorder(BorderFactory.createTitledBorder("Organisms"));
        lpan.add(new JScrollPane(m_list));

        setLayout(new BorderLayout());
        add(desc, BorderLayout.NORTH);
        add(lpan, BorderLayout.CENTER);
    }
}
