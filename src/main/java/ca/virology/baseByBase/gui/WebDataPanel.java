package ca.virology.baseByBase.gui;

import ca.virology.baseByBase.io.*;

import java.awt.*;

import java.net.URL;

import javax.swing.*;
import javax.swing.event.*;


/**
 * This provides a view onto the files available on the web
 *
 * @author Ryan Brodie
 * @version $Revision: 1.1.1.1 $
 */
public class WebDataPanel extends JPanel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected java.util.List m_files;
    protected final JList m_list;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new WebDataPanel object.
     *
     * @param descriptor the descriptor url.  This is an xml file that must be
     *                   parsable by this system.
     */
    public WebDataPanel(URL descriptor) {
        WebFileParser parser = new WebFileParser(descriptor);
        m_files = parser.getFiles();

        String[] names = new String[m_files.size()];

        for (int i = 0; i < names.length; ++i) {
            names[i] = ((WebFile) m_files.get(i)).getName();
        }

        m_list = new JList(names);

        initUI();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the selected file url
     *
     * @return the url in string form
     */
    public String getSelectedURL() {
        String name = (String) m_list.getSelectedValue();
        WebFile f = getFile(name);

        if (f != null) {
            return f.getURL();
        } else {
            return null;
        }
    }

    /**
     * get the file type of the selection
     *
     * @return the type of the selection
     */
    public String getSelectionType() {
        String name = (String) m_list.getSelectedValue();
        WebFile f = getFile(name);

        if (f != null) {
            return f.getFileType();
        } else {
            return null;
        }
    }

    /**
     * get the file for a given name
     *
     * @param name the name of the file
     * @return the WebFile object
     */
    protected WebFile getFile(String name) {
        for (int i = 0; i < m_files.size(); ++i) {
            if (((WebFile) m_files.get(i)).getName().equals(name)) {
                return (WebFile) m_files.get(i);
            }
        }

        return null;
    }

    /**
     * init the ui
     */
    protected void initUI() {
        // text fields
        final JTextField urlField = new JTextField();
        final JTextField cntField = new JTextField();
        final JTextField lenField = new JTextField();
        final JTextPane descPane = new JTextPane();

        urlField.setEnabled(false);
        cntField.setEnabled(false);
        lenField.setEnabled(false);
        descPane.setEnabled(false);

        m_list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                String name = (String) m_list.getSelectedValue();
                WebFile f = getFile(name);

                urlField.setText(f.getURL());
                cntField.setText(f.getSequenceCount() + "");
                lenField.setText(f.getLength() + "");
                descPane.setText(f.getDescription());
            }
        });

        // left part
        JPanel listPane = new JPanel(new BorderLayout());
        listPane.setBorder(BorderFactory.createTitledBorder("Files"));
        listPane.add(new JScrollPane(m_list), BorderLayout.CENTER);

        // right part
        JPanel infoPane = new JPanel(new BorderLayout());
        infoPane.setBorder(BorderFactory.createTitledBorder("Information"));

        Box infoTop = Box.createVerticalBox();
        JPanel infoBot = new JPanel(new BorderLayout());

        Box row1 = Box.createHorizontalBox();
        row1.add(new JLabel("Location: "));
        row1.add(Box.createHorizontalStrut(5));
        row1.add(urlField);

        Box row2 = Box.createHorizontalBox();
        row2.add(new JLabel("Sequences: "));
        row2.add(Box.createHorizontalStrut(5));
        row2.add(cntField);
        row2.add(Box.createHorizontalStrut(5));
        row2.add(new JLabel("Length: "));
        row2.add(Box.createHorizontalStrut(5));
        row2.add(lenField);
        infoTop.add(row1);
        infoTop.add(row2);
        infoBot.add(new JLabel("Description"), BorderLayout.NORTH);
        infoBot.add(new JScrollPane(descPane), BorderLayout.CENTER);

        infoPane.add(infoTop, BorderLayout.NORTH);
        infoPane.add(infoBot, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(infoPane, BorderLayout.CENTER);
        add(listPane, BorderLayout.WEST);
    }
}