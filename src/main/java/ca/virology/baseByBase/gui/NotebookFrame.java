package ca.virology.baseByBase.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.biojava.bio.seq.impl.SimpleStrandedFeature;

import ca.virology.lib.io.sequenceData.FeaturedSequence;


/**
 * NoteBookFrame
 *
 * @author will
 * @version 0.1
 *          <p>
 *          This class is a GUI for the Notebook option.  It is very simple and intended to allow the user
 *          to write notes that are associated with a location on the sequence.
 */


public class NotebookFrame extends JFrame {

    //int n_cols = 75;
    //int n_rows = 500;
    String notebook = "";

    //Document n_doc = new Document(null);
    JTextArea n_notes = new JTextArea();
    JScrollPane n_scroll = new JScrollPane(n_notes);

    public NotebookFrame(FeaturedSequence[] seqs, String notes) {
        System.out.println("initializing NotebookFrame");
        setPreferredSize(new Dimension(600, 800));
        setSize(600, 800);
        setTitle("MSA Notes");
        notebook = notes;


        n_notes.setEnabled(true);
        n_notes.setBackground(Color.white);
        n_notes.setText(notebook);
        n_notes.setEditable(true);

        add(n_scroll);

        setLocationRelativeTo(null);
        setVisible(true);

        for (int i = 0; i < seqs.length; i++) {
            Iterator it = seqs[i].features();


            while (it.hasNext()) {
                SimpleStrandedFeature se = (SimpleStrandedFeature) it.next();
                //System.out.println(se.toString());
            }
        }


    }

    //  Returns the text in the note.
    public String getText() {

        return n_notes.getText();

    }
}
