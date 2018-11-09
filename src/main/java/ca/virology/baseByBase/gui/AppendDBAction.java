/*
 * DBChooser: Creates a frame in which the user can select a db to connect
 * Copyright (C) 2004  Dr. Chris Upton University of Victoria
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

import java.awt.*;
import java.util.List;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.SwingWorker;

import ca.virology.baseByBase.DiffEditor;
import ca.virology.baseByBase.io.VocsTools;
import ca.virology.lib.io.sequenceData.*;
import ca.virology.lib.util.gui.*;

/**
 * @author Norman Jordan
 *         <p>
 *         Provides the handler for when the user chooses the menu option
 */
public class AppendDBAction extends AbstractAction {
    private JFrame m_parentWindow;
    private DBChooser m_dbChooser;

    /**
     * Creates a new instance of AppendDBAction
     *
     * @param parentWindow The parent window that the dialog windows will belong to
     */
    public AppendDBAction(JFrame parentWindow) {
        super("Append Sequences from VOCs Database", null);
        m_parentWindow = parentWindow;
    }

    /**
     * Called when the menu option File -> Add Sequences to Alignment -> From VOCs
     * Database ... is selected.
     */

    public void actionPerformed(ActionEvent e) {
        String dbName = DiffEditor.getDbName();
        m_dbChooser = new DBChooser(dbName, null);

        String newDB = m_dbChooser.chooseDB();

        if (newDB == null) {
            return;
        }

        dbName = newDB;

        DiffEditor.setDbName(dbName);
        System.out.println(dbName);

        final JDialog virusChooser = new JDialog(m_parentWindow);
        JPanel virusChooserOuterPanel = new JPanel();
        virusChooserOuterPanel.setLayout(new BoxLayout(virusChooserOuterPanel, BoxLayout.Y_AXIS));

        JPanel virusChooserPanel = new JPanel();
        GridLayout virusChooserLayout = new GridLayout(1, 2);
        virusChooserPanel.setLayout(virusChooserLayout);

        GridBagLayout virusChooserListLayout = new GridBagLayout();
        GuiDefaults virusChooserGuiDefaults = new GuiDefaults(12);
        GridBagConstraints virusChooserConstraints = new GridBagConstraints();
        JPanel virusChooserMemberGui = new JPanel();
        JPanel virusChooserNoMemberGui = new JPanel();
        virusChooserPanel.setBorder(GuiUtils.createWindowBorder("Virus Selector", new GuiDefaults()));

        final ListMatchSelector lsm1 = new ListMatchSelector("Select these viruses", dbName, virusChooserGuiDefaults,
                virusChooserListLayout, virusChooserConstraints, virusChooserMemberGui, true, false);
        lsm1.setChecked(true);
        virusChooserPanel.add(virusChooserMemberGui);

        final ListMatchSelector lsm2 = new ListMatchSelector("Do NOT select these viruses", dbName,
                virusChooserGuiDefaults, virusChooserListLayout, virusChooserConstraints, virusChooserNoMemberGui,
                false, false);
        virusChooserMemberGui.setLayout(virusChooserListLayout);
        virusChooserNoMemberGui.setLayout(virusChooserListLayout);
        virusChooserPanel.add(virusChooserNoMemberGui);
        virusChooserOuterPanel.add(virusChooserPanel);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.add(Box.createHorizontalGlue());

        JButton button = new JButton("Cancel");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                virusChooser.dispose();
            }
        });
        buttonPane.add(button);
        button = new JButton("Select");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                virusChooser.dispose();

                Object[] toAdd = lsm1.getVirusIdList();
                Object[] toNotAdd = lsm2.getVirusIdList();
                ArrayList<Object> viruses = new ArrayList();
                HashSet ignoreViruses = new HashSet();

                for (int i = 0; i < toNotAdd.length; i++) {
                    ignoreViruses.add(toNotAdd[i]);
                }

                for (int i = 0; i < toAdd.length; i++) {
                    if (!ignoreViruses.contains(toAdd[i])) {
                        viruses.add(toAdd[i]);
                    }
                }

                DiffEditor.frame.getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                DiffEditor.frame.m_status.setText("Appending " + viruses.size() + " Sequences from VOCs Database");
                DiffEditor.frame.m_status.repaint();

                // Set up the progress bar (a.k.a. loading bar) and fetch sequences from VOCs
                FeaturedSequence[] seqs = new FeaturedSequence[viruses.size()];
                JWindow loadingWindow = new JWindow();
                JPanel loadingPanel = new JPanel(new GridLayout(3, 1));
                JProgressBar loadingBar = new JProgressBar(0, viruses.size());
                JLabel loadingText = new JLabel("Loading viruses...", SwingConstants.CENTER);
                JLabel loadingMessage = new JLabel("", SwingConstants.CENTER);
                loadingText.setFont(
                        new Font(loadingText.getFont().getName(), Font.BOLD, loadingText.getFont().getSize() + 5));
                loadingBar.setStringPainted(true);
                loadingPanel.add(loadingText);
                loadingPanel.add(loadingMessage);
                loadingPanel.add(loadingBar);
                loadingWindow.add(loadingPanel);
                loadingWindow.setSize(400, 200);
                loadingWindow.setVisible(true);
                loadingWindow.setLocationRelativeTo(null);

                VirusGetter loadViruses = new VirusGetter(loadingBar, loadingWindow, loadingMessage, seqs, viruses);
                loadViruses.execute();
            }
        });
        buttonPane.add(button);
        virusChooserOuterPanel.add(buttonPane);
        virusChooser.setContentPane(virusChooserOuterPanel);

        virusChooser.pack();

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = virusChooser.getWidth();
        int h = virusChooser.getHeight();
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        virusChooser.setLocation(x, y);
        virusChooser.setModal(true);
        virusChooser.setVisible(true);

    }

    /**
     * This class displays a JProgressBar while it fetches genome data from the VOCs
     * DB
     */
    static class VirusGetter extends SwingWorker<FeaturedSequence, Integer> {
        JProgressBar loadingBar;
        FeaturedSequence[] seqs;
        ArrayList<Object> viruses;
        JWindow loadingWindow;
        String[] loadingMessages = new String[] { "Downloading sequences...", "Pumping in viruses...",
                "Pulling out genomes...", "Extracting amino acids...", "Sequencing DNA...", "Querying databases...",
                "Colouring letters in...", "Locating proteins...", "Confirming literature...",
                "Biology viruses, not computer viruses", "Getting rid of bacteria...",
                "In the event of a fire call 911 or your local emergency number", "Aligning everything together...",
                "Base by Base!", "Going viral...", "If you're not part of the solution, you're part of the precipitate",
                "Putting on the codons...", "A T C G", "Comparing genomes...", "Assembling sequences...",
                "Removing rogue mutations...", "Replicating viruses...", "Unravelling DNA strands...",
                "Going for coffee...", "This might take a while" }; // TODO: add more :)
        String finalMessage = "Almost done, just cleaning up...";
        JLabel loadingText;

        /**
         * Constructor just assigns instance variables
         * 
         * @param loadingBar    The loading bar that shows progress
         * @param loadingWindow The window that the loading bar is in
         * @param loadingText   The loading text that gets regularly updated
         * @param seqs          The sequences that get displayed
         * @param viruses       The viruses to fetch
         */
        public VirusGetter(JProgressBar loadingBar, JWindow loadingWindow, JLabel loadingText, FeaturedSequence[] seqs,
                ArrayList<Object> viruses) {
            this.loadingBar = loadingBar;
            this.seqs = seqs;
            this.viruses = viruses;
            this.loadingWindow = loadingWindow;
            this.loadingText = loadingText;
        }

        /**
         * Runs on the Event Dispatcher Thread and updates the progress bar
         */
        @Override
        protected void process(List<Integer> chunks) {
            for (int chunk : chunks) {
                loadingBar.setValue(chunk);
                if (chunk == 1) {
                    loadingText.setText(loadingMessages[0]);
                }
                // Pick a random loading message every 5 updates
                else if (chunk % 5 == 0) {
                    loadingText.setText(loadingMessages[new Random().nextInt(loadingMessages.length)]);
                }

                if (chunk == viruses.size()) {
                    loadingText.setText(finalMessage);
                }
            }
        }

        /**
         * Runs on a background SwingWorker Thread Fetches genome data from the database
         * and sends progress updates to the loading bar
         */
        @Override
        protected FeaturedSequence doInBackground() throws Exception {
            for (int i = 0; i < viruses.size(); i++) {
                publish(i + 1);
                String id = (String) viruses.get(i);
                try {
                    seqs[i] = VocsTools.getGenomeData(Integer.parseInt(id), DiffEditor.getDbName());

                    if (seqs[i] == null || seqs[i].toString() == null) {
                        UITools.showWarning(
                                "There was some problem while we were fetching requested sequences\nPlease check your firewall settings. Otherwise, contact us to report a bug.",
                                null);
                        return null;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
            return null;
        }

        /**
         * Called when all data has been received from the db Displays the sequences
         */
        @Override
        protected void done() {
            try {
                DiffEditor.frame.appendSequences(seqs);
                loadingWindow.dispose();
            } catch (Exception ex) {
                UITools.showWarning("There was an error appending sequences from the database",
                        DiffEditor.frame.getContentPane());
                ex.printStackTrace();
            }
        }
    }

}
