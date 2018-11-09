package ca.virology.baseByBase.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * This classe lets users select a feature file to
 * features from.
 *
 * @author Ryan Brodie
 */
public class FeatureFileSelectPanel extends JPanel {

    protected JTextField m_fnField = new JTextField(30);
    protected JTextField m_prefixField = new JTextField(30);
    protected JComboBox m_fTypeBox = new JComboBox();

    public FeatureFileSelectPanel() {
        init();
    }

    /**
     * Get the file type chosen
     *
     * @return the file type
     */
    public String getFileType() {
        return m_fTypeBox.getSelectedItem()
                .toString();
    }

    /**
     * Get the filename chosen
     *
     * @return the file name
     */
    public String getFilename() {
        return m_fnField.getText();
    }

    /**
     * Get the prefix chosen if there is no prefix insert a meaningful prefix
     *
     * @return The prefix
     */
    public String getPrefix() {
        if (m_prefixField.getText() != null)
            return m_prefixField.getText();
        else return "";
    }

    /**
     * pop the browse window
     */
    protected void browseAction() {
        JFileChooser fc = new JFileChooser();
        int val = fc.showOpenDialog(this);

        if (val == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            m_fnField.setText(f.getAbsolutePath());
        }
    }

    /**
     * init the layout and components
     */
    protected void init() {
        m_fTypeBox.addItem("GENBANK");
//        m_fTypeBox.addItem("VGO");
//        m_fTypeBox.addItem("SWISSPROT");
//        m_fTypeBox.addItem("EMBL");

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        JPanel name = new JPanel(new BorderLayout());
        JPanel prefix = new JPanel(new BorderLayout());
        JPanel type = new JPanel(new BorderLayout());

        JButton br = new JButton("Browse...");
        br.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        browseAction();
                    }
                });

        name.add(new JLabel("Filename"), BorderLayout.WEST);
        type.add(new JLabel("File Type"), BorderLayout.WEST);
        prefix.add(new JLabel("Gene Prefix"), BorderLayout.WEST);
        name.add(m_fnField, BorderLayout.CENTER);
        type.add(m_fTypeBox, BorderLayout.CENTER);
        prefix.add(m_prefixField, BorderLayout.CENTER);
        name.add(br, BorderLayout.EAST);

        pane.add(name);
        pane.add(type);
        pane.add(prefix);

        setLayout(new BorderLayout());
        add(pane, BorderLayout.CENTER);
    }
}
