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

import ca.virology.lib.util.gui.UITools;

import java.awt.*;

import javax.swing.*;


/**
 * This class provides useful static methods which should speed up development
 * of UI components, as well as ease standardization of messages, etc.
 *
 * @author Ryan Brodie
 */
public final class Utils {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * print the current stack trace with this method and a message as the
     * first entry.
     *
     * @param warning a message to display
     */
    public static void warningTrace(String warning) {
        new Throwable("Warning Message: " + warning).printStackTrace();
    }

    /**
     * This will invoke the given set of commands in a thread while displaying
     * a 'waiting' cursor.
     *
     * @param owner The object which owns the dialog
     * @param r     The Runnable object to run
     * @throws InterruptedException
     */
    public static void invokeWithWaitCursor(final JFrame owner, final Runnable r) throws InterruptedException {
        Thread t = new Thread("InvokeWithWaitCursor(" + r + ")") {
            public void run() {
                owner.repaint();
                owner.getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));

                try {
                    UITools.invoke(new Runnable() {
                        public void run() {
                            try {
                                r.run();
                                owner.getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                            } catch (Exception ex) {
                            }
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        t.start();
    }
}
