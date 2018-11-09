package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

import ca.virology.vgo.gui.VGOUIObject;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Abstract class, used as the super class of other VGO windows.
 *
 * @author Angie Wang
 * @date July 8, 2002
 */
public abstract class AbstractVGOWindow2 extends JFrame implements VGOUIObject {

    protected Set m_children = Collections.synchronizedSet(new HashSet());
    protected VGOUIObject m_parent;

    /**
     * Constructor for the AbstractVGOWindow2 object
     */
    public AbstractVGOWindow2(String title) {
        super(title);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        close();
                    }
                }
        );
    }

    /**
     * Constructs a VGO Window with no title
     */
    public AbstractVGOWindow2() {
        this("");
    }

    /**
     * Sets the 'parent' object for this particular object
     *
     * @param obj The new parent value
     */
    public void setParent(VGOUIObject obj) {
        m_parent = obj;
    }


    /**
     * Takes the given object and makes it a child of this particular UI Object
     *
     * @param obj The child object to take ownership of
     */
    public void takeOwnership(VGOUIObject obj) {
        m_children.add(obj);
        obj.setParent(this);
    }

    /**
     * Request ownership be taken away from this object of the given child object
     *
     * @param obj The object to relinquish control of
     */
    public void removeOwnership(VGOUIObject obj) {
        m_children.remove(obj);
    }

    /**
     * Refreshes this object
     */
    public void refresh() {
        this.repaint();
        Iterator i = m_children.iterator();
        while (i.hasNext()) {
            VGOUIObject o = (VGOUIObject) i.next();
            o.refresh();
        }
    }


    /**
     * Close / destroy this UI object, and do so with all child objects.<BR>
     * Basically, 'prune' the tree.
     */
    public void close() {
        Iterator i = m_children.iterator();
        while (i.hasNext()) {
            VGOUIObject o = (VGOUIObject) i.next();
            o.close();
        }
        dispose();
        if (m_parent != null) m_parent.removeOwnership(this);
    }
}
