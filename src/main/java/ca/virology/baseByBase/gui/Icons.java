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

import java.util.*;

import javax.swing.*;


/**
 * This class holds references to icons that are useful
 * with toolbars
 *
 * @author Ryan Brodie
 * @version 1.0
 * @date jan 2, 2003
 */
public class Icons {
    protected static Icons c_instance = null;
    protected Map m_iconMap = new HashMap();

    protected Icons() {
        initMap();
    }

    /**
     * Static singleton retrieval method
     */
    public static Icons getInstance() {
        if (c_instance == null) {
            c_instance = new Icons();
        }

        return c_instance;
    }

    /**
     * Add an icon to the map
     *
     * @param name the name of the icon to add
     * @param icon the actual icon to add
     */
    public void setIcon(String name, ImageIcon icon) {
        m_iconMap.put(name, icon);
    }

    /**
     * Get the icon represented by a given name string
     *
     * @param name The name of the icon to retrieve
     */
    public ImageIcon getIcon(String name) {
        return (ImageIcon) m_iconMap.get(name);
    }

    /**
     * initialize the default icon mappings
     */
    protected void initMap() {
        ClassLoader cl = getClass().getClassLoader();

        java.net.URL imgURL = null;

        try {
            imgURL = cl.getResource("images/Swap.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("SWAP", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Frames.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("FRAMES", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Add24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("ADD", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Export24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("EXPORT", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Edit24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("EDIT", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Select24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("SELECT", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Find24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("FIND", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/FindAgain24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("FINDAGAIN", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Help24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("HELP", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/New24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("NEW", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Open24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("LOAD", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Preferences24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("PREFS", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Print24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("PRINT", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/PrintPreview24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("PPREVIEW", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Refresh24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("RELOAD", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Save24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("SAVE", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/SaveAs24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("SAVEAS", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Right.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("RIGHT20", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Right1.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("RIGHT1000", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Right10.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("RIGHT10000", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Left.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("LEFT20", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Left1.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("LEFT1000", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Left10.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("LEFT10000", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Top.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("TOPSTRAND", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Bottom.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("BOTTOMSTRAND", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Undo24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("UNDO", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Redo24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("REDO", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Back24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("BACK", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Forward24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("FORWARD", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/X24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("X", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/MoveUp.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("MOVEUP", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/MoveDown.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("MOVEDOWN", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/SkipUp.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("SKIPUP", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/SkipDown.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("SKIPDOWN", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/SetDisplay24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("SETDISPLAY", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/NextComment24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("NEXTCOMMENT", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/LastComment24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("LASTCOMMENT", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/NewComment.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("NEWCOMMENT", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/NextGene24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("NEXTGENE", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/LastGene24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("LASTGENE", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/StartOfGene.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("GENESTART", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/EndOfGene.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("GENEEND", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/NextDiff24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("NEXTDIFF", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/LastDiff24.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("LASTDIFF", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/NextGap.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("NEXTGAP", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/LastGap.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("LASTGAP", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Show.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("SHOW", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Hide.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("HIDE", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Glue.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("GLUE", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/NextPrimer.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("NEXTPRIMER", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/LastPrimer.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("LASTPRIMER", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Maximize.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("MAXIMIZE", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Restore.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("RESTORE", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Close.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("CLOSE", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Zoomin.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("ZOOMIN", new ImageIcon(imgURL));

        try {
            imgURL = cl.getResource("images/Zoomout.gif");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setIcon("ZOOMOUT", new ImageIcon(imgURL));
    }
}
