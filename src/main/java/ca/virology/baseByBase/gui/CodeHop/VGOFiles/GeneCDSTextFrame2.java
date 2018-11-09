package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

import ca.virology.lib.util.gui.Utils;
import ca.virology.vgo.data.*;
import ca.virology.vgo.gui.*;
import ca.virology.vgo.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import java.io.*;

import java.net.*;
import java.util.List;

import ca.virology.lib.prefs.DBPrefs;
import ca.virology.lib.server.*;
import ca.virology.lib.util.common.Args;
import ca.virology.lib.util.common.Logger;
import ca.virology.lib.util.common.TempFileManager;
import ca.virology.lib.util.gui.*;
import ca.virology.lib.vocsdbAccess.SQLAccess;
import ca.virology.lib.vocsdbAccess.dataTypes.Gene;

/**
 * This class is used to display a DNA sequence of a particular organism. it is
 * a subclass of class AbstractSequenceTextFrame.
 *
 * @author Angie Wang
 * @version 1.0
 * @date July 8, 2002
 */
public class GeneCDSTextFrame2 extends AbstractSequenceTextFrame2 {

    private String m_dbName;


    /**
     * constructor that takes the ID of a gene as the parameter.
     *
     * @param gene_id The ID of the gene to display
     */
    GeneCDSTextFrame2(int gene_id, String dbName) {
        super();

        m_dbName = dbName;

        Logger.println("TextFrame Constructor gene_id: " + gene_id +
                " db=" + m_dbName);

        Gene g = null;
        GeneFactory gFac = GeneFactory.getInstance();

        try {
            g = gFac.createGene(gene_id, m_dbName);
        } catch (Exception e) {
            e.printStackTrace();
            ca.virology.vgo.gui.Utils.showError("Error creating gene: " + e.getMessage());
            close();
            return;
        }
    }

}
