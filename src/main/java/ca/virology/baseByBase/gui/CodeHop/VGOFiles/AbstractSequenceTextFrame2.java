package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

import ca.virology.vgo.gui.AbstractVGOWindow;
import ca.virology.vgo.gui.VGOButton;
import ca.virology.vgo.gui.VGOHelpMenu;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * This class is the parent class for VGO windows GenbankCDSTextFrame and
 * GenericSequenceTextFrame It also implements VGOUIObject interface
 *
 * @author Angie Wang
 * @version 1.0
 * @date June 14, 2002
 */
public abstract class AbstractSequenceTextFrame2 extends AbstractVGOWindow2 {
    // Fields of this class:
    // m_mainPane: main pane of this frame. It is added to the contentPane of this frame
    // m_showPane: the pane shows the content of a sequence
    // m_showScrollPane: the scroll pane added onto the m_showPane
    // m_lowPane: the pane added at the down side of the window. contains button panes.
    // m_closePane: contains the close button. this pane is needed because we want to
    //              control the position of the m_clossBtn button.
    protected JPanel m_lowPane, m_mainPane, m_closePane;
    protected VGOButton m_closeBtn;
    protected JEditorPane m_showPane;
    protected JScrollPane m_showScrollPane;
    private static JMenuBar m_menuBar;
    protected Font m_font = new Font("Courier", Font.PLAIN, 12);


    /**
     * Constructor of this class.
     */
    AbstractSequenceTextFrame2() {

    }



/*    *
     * Add a menu to the main menu bar
     * @param menu the menu to be added*/

    protected void addMenuToMenuBar(JMenu menu) {
        //JMenu a_menu = m_menuBar.getMenu(1);
        //a_menu.add( menu );
    }
}
