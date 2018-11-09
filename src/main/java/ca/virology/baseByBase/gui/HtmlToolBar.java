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

// HtmlToolBar
// - From Advanced Java 2 Platform - How to Program; Deitel
package ca.virology.baseByBase.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


/**
 * Implements the navigation functions for an HTML viewer
 */
public class HtmlToolBar extends JToolBar implements HyperlinkListener {
    //~ Instance fields ////////////////////////////////////////////////////////

    private HtmlPane browser;
    private JButton back;
    private JButton forw;
    private JComboBox page;
    private boolean newLink = true;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Default constructor
     *
     * @param browserPane the html browser pane
     */
    public HtmlToolBar(HtmlPane browserPane) {
        super("Web Navigation");
        browser = browserPane;
        browser.addHyperlinkListener(this);
        page = new JComboBox();
        page.setEditable(true);
        page.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String newSelection = (String) page.getSelectedItem();
                URL url = null;

                if (newSelection == null) {
                    return;
                }

                if (newSelection.length() == 0) {
                    return;
                }

                try {
                    url = new URL(newSelection);

                    if (newLink) {
                        browser.gotoURL(url);
                    }

                    newLink = true;
                } catch (MalformedURLException urlE) {
                    JOptionPane.showMessageDialog(null, "URL error!\n" + url + "\n" + urlE.getMessage(), "HTML Viewer", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        back = new JButton(" < ");
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                newLink = false;

                URL url = browser.backward();

                if (url != null) {
                    page.setSelectedItem(url.toString());
                }
            }
        });
        forw = new JButton(" > ");
        forw.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                newLink = false;

                URL url = browser.forward();

                if (url != null) {
                    page.setSelectedItem(url.toString());
                }
            }
        });
        add(back);
        add(forw);
        add(new JLabel(" Netsite: "));
        add(page);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Set a netsite
     *
     * @param url the url to go to
     */
    public void setPage(URL url) {
        newLink = false;

        if (url != null) {
            page.addItem(url.toString());
        }
    }

    /**
     * Display the URL activated
     *
     * @param event the link event
     */
    public void hyperlinkUpdate(HyperlinkEvent event) {
        String u;

        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            newLink = true;

            URL url = event.getURL();
            u = url.toString();

            while (u.indexOf('\n') != -1) {
                u = u.substring(0, u.indexOf('\n')) + u.substring(u.indexOf('\n') + 1);
            }

            try {
                url = new URL(u);
            } catch (MalformedURLException urlE) {
                JOptionPane.showMessageDialog(null, "Invalid URL address!\n" + url + "\n" + urlE.getMessage(), "HTML Viewer", JOptionPane.ERROR_MESSAGE);
            }

            browser.gotoURL(url);
            page.addItem(url.toString());
        }
    }
}
