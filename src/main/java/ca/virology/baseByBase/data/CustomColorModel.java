package ca.virology.baseByBase.data;

import ca.virology.baseByBase.gui.OptionDialog;
import ca.virology.lib.io.sequenceData.EditableSequence;
import ca.virology.lib.io.tools.SequenceTools;
import org.biojava.bio.seq.StrandedFeature;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: localadmin
 * Date: 2015-04-29
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomColorModel implements ColorScheme {

    protected Object             m_lock = new Object();
    protected Color[]            m_colors = new Color[30];
    protected Color              m_gapColor;
    protected EditableSequence[] m_seqs;
    protected String selectedLetters;


    public CustomColorModel(String input)
    {
        selectedLetters = input;
        m_colors['A' - 'A'] = Color.yellow;
        m_colors['C' - 'A'] = Color.green;
        m_colors['T' - 'A'] = Color.orange;
        m_colors['G' - 'A'] = Color.cyan;

        m_colors['D' - 'A'] = new Color(200, 200, 255);
        m_colors['E' - 'A'] = new Color(255, 128, 128);
        m_colors['F' - 'A'] = new Color(255, 200, 200);
        m_colors['H' - 'A'] = new Color(255, 255, 128);
        m_colors['I' - 'A'] = new Color(255, 255, 200);
        m_colors['K' - 'A'] = new Color(200, 255, 200);
        m_colors['L' - 'A'] = new Color(255, 128, 255);
        m_colors['M' - 'A'] = new Color(255, 200, 255);
        m_colors['N' - 'A'] = new Color(128, 255, 255);
        m_colors['P' - 'A'] = new Color(128, 200, 255);
        m_colors['Q' - 'A'] = new Color(200, 128, 255);
        m_colors['R' - 'A'] = new Color(128, 255, 200);
        m_colors['S' - 'A'] = new Color(200, 255, 128);
        m_colors['V' - 'A'] = new Color(255, 200, 128);
        m_colors['W' - 'A'] = Color.red;
        m_colors['Y' - 'A'] = Color.pink;
        m_gapColor = Color.white;
    }

    public void setSequences(EditableSequence[] seqs)
    {
        synchronized (m_lock) {
            m_seqs = seqs;
        }
    }

    /**
     * Get the backgrond of the given index position for the given sequence
     *
     * @param seq The sequence to use
     * @param index The index in the given sequence
     *
     * @return the background color
     */
    public Color getBackground(
            EditableSequence seq,
            int index)
    {
        char c = seq.charAt(index);

        if (selectedLetters.contains(""+c)) {
            return m_colors[c - 'A'];
        }
        return Color.white;
    }

    /**
     * Get the foreground of the given index position for the given sequence
     *
     * @param seq The sequence to use
     * @param index The index in the given sequence
     *
     * @return the foreground color
     */
    public Color getForeground(
            EditableSequence seq,
            int index)
    {
            return Color.black;
    }
}
