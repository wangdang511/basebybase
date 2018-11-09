/*
 * Copyright (C) 2003  Dr. Chris Upton University of Victoria
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

// HtmlPane
// - From Advanced Java 2 Platform - How to Program; Deitel
package ca.virology.baseByBase.gui;

import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;


/**
 * Creates the area to display the content of an HTML document
 */
public class HtmlPane extends JEditorPane {
    //~ Instance fields ////////////////////////////////////////////////////////

    private List history = new ArrayList();
    private int historyIndex;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Default constructor
     */
    public HtmlPane() {
        setEditable(false);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Display the given URL and add it to the history
     *
     * @param url the URL to be displayed
     */
    public void gotoURL(URL url) {
        int i = 0;
        displayPage(url);
        historyIndex = history.size() - 1;
    }

    /**
     * Display next history URL
     *
     * @return the url displayed
     */
    public URL forward() {
        URL url = null;

        if (history.size() > 0) {
            historyIndex++;

            if (historyIndex >= history.size()) {
                historyIndex = history.size() - 1;
            }

            url = (URL) history.get(historyIndex);
            displayPage(url);
        }

        return url;
    }

    /**
     * Display previous history URL
     *
     * @return the url displayed
     */
    public URL backward() {
        URL url = null;

        if (history.size() > 0) {
            historyIndex--;

            if (historyIndex < 0) {
                historyIndex = 0;
            }

            url = (URL) history.get(historyIndex);
            displayPage(url);
        }

        return url;
    }

    /**
     * Display the given URL
     *
     * @param url the URL to be displayed
     */
    private void displayPage(URL url) {
        try {
            setPage(url);
            history.add(url);
        } catch (IOException ioE) {
            JOptionPane.showMessageDialog(null, "Error displaying page:\n" + url + "\n" + ioE.getMessage(), "HTML Viewer", JOptionPane.ERROR_MESSAGE);
        }
    }
}
