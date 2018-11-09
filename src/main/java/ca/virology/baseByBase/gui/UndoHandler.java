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

import javax.swing.event.*;
import javax.swing.undo.*;


/**
 * This class handles undos for the bbb application
 *
 * @author Ryan Brodie
 */
public class UndoHandler {
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final UndoableEdit EMPTY_EDIT = new AbstractUndoableEdit() {
    };

    protected static UndoHandler c_instance = null;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected UndoManager m_manager;
    protected UndoableEditSupport m_support;
    protected boolean m_modified = false;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new UndoHandler object.
     */
    protected UndoHandler() {
        m_manager = new UndoManager();
        m_support = new UndoableEditSupport();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the singleton instance
     *
     * @return the singleton instance
     */
    public static UndoHandler getInstance() {
        if (c_instance == null) {
            c_instance = new UndoHandler();
        }

        return c_instance;
    }

    /**
     * sets the 'modified' flag.  This flag overrides the 'undoable' flag that
     * would normally be used because some changes can't be easily traced by
     * the undo manager.
     *
     * @param modified the new flag
     */
    public void setModified(boolean modified) {
        m_modified = modified;
        m_support.postEdit(EMPTY_EDIT);
    }

    /**
     * Post an edit to the handler
     *
     * @param edit the edit to post
     */
    public void postEdit(UndoableEdit edit) {
        //System.out.println(edit+" posted ");
        m_manager.addEdit(edit);
        m_support.postEdit(edit);
        m_modified = true;
    }

    /**
     * Undo the last change
     *
     * @throws CannotUndoException
     */
    public void undo() throws CannotUndoException {

        m_manager.undo();

        m_support.postEdit(EMPTY_EDIT);
    }

    /**
     * redo the last change
     *
     * @throws CannotRedoException
     */
    public void redo() throws CannotRedoException {
        m_manager.redo();

        m_support.postEdit(EMPTY_EDIT);
    }

    /**
     * Reset the undo handler (Forgets all changes)
     */
    public void reset() {
        m_manager.end();
        m_manager = new UndoManager();

        m_support.postEdit(EMPTY_EDIT);
    }

    /**
     * get the modified flag
     *
     * @return true if changes have been made such that there is still undoing
     * possible
     */
    public boolean isModified() {
        return (m_modified);
    }

    /**
     * Registers an UndoableEditListener.
     *
     * @param l the listener
     */
    public void addUndoableEditListener(UndoableEditListener l) {
        m_support.addUndoableEditListener(l);
    }

    /**
     * Deregisters an UndoableEditListener.
     *
     * @param l the listener
     */
    public void removeUndoableEditListener(UndoableEditListener l) {
        m_support.removeUndoableEditListener(l);
    }
}
