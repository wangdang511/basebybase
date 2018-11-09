package ca.virology.baseByBase.data;

import java.awt.Color;


/**
 * This represents one segment of an alignment summary.  This
 * contains color and style information for the renderers to use
 * to display the indicator
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public final class SummaryIndicator {
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final int IND_TICK = 10001;
    public static final int IND_GAP = 10002;
    public static final int IND_EMPTY = 10003;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected int m_type;
    protected Color m_color;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new SummaryIndicator object.
     *
     * @param type The type of indicator (one of IND_GAP, IND_TICK or IND_EMPTY)
     * @param col  the color
     */
    public SummaryIndicator(
            int type,
            Color col) {
        m_type = type;
        m_color = col;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the type
     *
     * @return IND_GAP, IND_TICK or IND_EMPTY
     */
    public int getType() {
        return m_type;
    }

    /**
     * set the type
     *
     * @param newType IND_GAP, IND_TICK or IND_EMPTY
     */
    public void setType(int newType) {
        m_type = newType;
    }

    /**
     * get the color
     *
     * @return the color
     */
    public Color getColor() {
        return m_color;
    }

    /**
     * set the color
     *
     * @param c the new color
     */
    public void setColor(Color c) {
        m_color = c;
    }
}
