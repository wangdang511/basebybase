/*
 * JDotter: Java Interface to Dotter
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

// HtmlView
// - Adapted from Advanced Java 2 Platform - How to Program; Deitel

package ca.virology.baseByBase.gui;

import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Web Browser (a simple HTML viewer)
 *
 * @author Angelika Ehlers
 * @version 1.0
 * @extends JFrame
 */

public class HtmlView extends JFrame {
    private String title = "Web Browser";
    private HtmlToolBar toolBar;
    private HtmlPane pane;
    private int[] geometry;
    private boolean app = false;
    private boolean active = true;
    private JFrame frame;

    /**
     * Constructor
     *
     * @param url url of the page to be displayed
     */
    public HtmlView(String url) {
        super();
        setTitle(title);
        frame = this;
        geometry = new int[4];
        geometry[0] = 650;
        geometry[1] = 450;
        geometry[2] = 10;
        geometry[3] = 10;
        displayPage(url);
    }

    /**
     * Constructor
     *
     * @param url url of the page to be displayed
     * @param geo {H,W,X,Y}
     */
    public HtmlView(String url, int[] geo) {
        super();
        setTitle(title);
        frame = this;
        geometry = geo;
        displayPage(url);
    }

    /**
     * Constructor
     *
     * @param frameTitle name of the window
     * @param url        url of the page to be displayed
     * @param geo        {H,W,X,Y}
     */
    public HtmlView(String frameTitle, String url, int[] geo) {
        super(frameTitle);
        frame = this;
        title = frameTitle;
        geometry = geo;
        displayPage(url);
    }

    /**
     * Constructor
     *
     * @param frameTitle name of the window
     * @param url        url of the page to be displayed
     * @param apps       is true if we run as application
     * @param geo        {H,W,X,Y}
     */
    public HtmlView(String frameTitle, String url, boolean apps, int[] geo) {
        super(frameTitle);
        frame = this;
        title = frameTitle;
        app = apps;
        geometry = geo;
        displayPage(url);
    }

    /**
     * check if window is open
     *
     * @return true if window is open, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * close window
     */
    public void done() {
        active = false;
        if (app)
            System.exit(0);
        else
            dispose();
    }

    /**
     * setup the browser window and display the html file
     *
     * @param url url of the page to be displayed
     */
    public void displayPage(String url) {
        URL link;
        try {
            link = new URL(url);
        } catch (MalformedURLException urlE) {
            JOptionPane.showMessageDialog(null, "Address format error!\n" + url +
                    "\n" + urlE.getMessage(), "HTML Viewer", JOptionPane.ERROR_MESSAGE);
            done();
            return;
        }
        pane = new HtmlPane();
        toolBar = new HtmlToolBar(pane);
        pane.gotoURL(link);
        toolBar.setPage(link);

        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem close = new JMenuItem("Close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                done();
            }
        });
        file.add(close);
        menuBar.add(file);
        setJMenuBar(menuBar);

        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(pane), BorderLayout.CENTER);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                done();
            }
        });
        setBounds(geometry[2], geometry[3], geometry[0] + geometry[2], geometry[1] + geometry[3]);
        setSize(geometry[0], geometry[1]);
        setVisible(true);
    }
}
