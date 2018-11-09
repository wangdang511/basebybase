package ca.virology.baseByBase.gui;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;


/**
 * <B>Not currently in use</B>
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public class FeatureFileLoadSeqPanel
        extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected JComboBox m_fTypeBox = new JComboBox();
    protected JComboBox m_sTypeBox = new JComboBox();
    protected JTextField m_fnField = new JTextField(30);
    protected JTextField m_gnField = new JTextField(30);
    protected JCheckBox m_guessBox = new JCheckBox("Guess");

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new FeatureFileLoadSeqPanel object.
     */
    public FeatureFileLoadSeqPanel() {
        init();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFilename() {
        return m_fnField.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getGivenName() {
        return m_gnField.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean guessName() {
        return m_guessBox.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFileType() {
        return m_fTypeBox.getSelectedItem()
                .toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getSequenceType() {
        return m_sTypeBox.getSelectedItem()
                .toString();
    }

    /**
     * DOCUMENT ME!
     */
    protected void init() {
        m_fTypeBox.addItem("GENBANK");
        m_fTypeBox.addItem("EMBL");
        m_fTypeBox.addItem("GENPEPT");
        m_fTypeBox.addItem("SWISSPROT");
        m_sTypeBox.addItem("DNA");
        m_sTypeBox.addItem("AA");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JButton br = new JButton("Browse...");
        br.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        browseAction();
                    }
                });

        JPanel pane = new JPanel(new BorderLayout());
        JPanel labels = new JPanel(new GridLayout(2, 1));
        JPanel fields = new JPanel(new GridLayout(2, 1));
        JPanel btns = new JPanel(new GridLayout(2, 1));
        pane.add(labels, BorderLayout.WEST);
        pane.add(fields, BorderLayout.CENTER);
        pane.add(btns, BorderLayout.EAST);
        pane.setBorder(BorderFactory.createTitledBorder("File"));

        labels.add(new JLabel("Filename"));
        labels.add(new JLabel("Load As (Name)"));
        fields.add(m_fnField);
        fields.add(m_gnField);
        btns.add(br);
        btns.add(m_guessBox);

        add(pane);
        add(Box.createVerticalStrut(5));

        //-----
        pane = new JPanel(new BorderLayout());
        labels = new JPanel(new GridLayout(2, 1));
        fields = new JPanel(new GridLayout(2, 1));
        pane.add(labels, BorderLayout.WEST);
        pane.add(fields, BorderLayout.CENTER);
        pane.setBorder(BorderFactory.createTitledBorder("Sequence Info"));

        labels.add(new JLabel("File Type"));
        labels.add(new JLabel("Sequence Type"));
        fields.add(m_fTypeBox);
        fields.add(m_sTypeBox);

        add(pane);
    }

    /**
     * DOCUMENT ME!
     */
    protected void browseAction() {
        JFileChooser fc = new JFileChooser();
        int val = fc.showOpenDialog(this);

        if (val == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            m_fnField.setText(f.getAbsolutePath());
        }
    }
}
