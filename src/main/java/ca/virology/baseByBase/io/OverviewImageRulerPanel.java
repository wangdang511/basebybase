package ca.virology.baseByBase.io;

import java.awt.*;

import javax.swing.*;

public class OverviewImageRulerPanel extends JPanel {

    //~ Instance fields ////////////////////////////////////////////////////////

    protected int m_sLength;
    protected int m_width;
    protected double m_scale;
    private int m_min = 0;
    private int m_max = m_sLength;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new RulerPanel object.
     *
     * @param start the leftmost position on the numberline
     * @param stop  the rightmost position on the numberline
     * @param width the image width of the ruler
     */
    public OverviewImageRulerPanel(int start, int stop, int width) {
        System.out.println("New Ruler: " + start + "->" + stop + " w: " +
                width);

        m_min = start;
        m_max = stop;

        m_sLength = stop - start + 1;
        m_width = width;
        m_scale = (double) m_sLength / (double) width;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the ruler size
     *
     * @return ruler size
     */
    public Dimension getPreferredSize() {
        return new Dimension((int) ((double) m_sLength / m_scale), 50);
    }

    /**
     * paint the ruler
     *
     * @param g the graphics context
     */
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        int width = (int) (m_sLength / m_scale);

        g.setColor(Color.black);

        g.translate(-(int) ((double) m_min / m_scale), 0);

        java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double();
        int min = m_min;
        int max = m_max;
        double minX = 0.0;
        double maxX = (double) m_width;
        double scale = 1.0 / m_scale;
        int tickHeight = 5;
        int horizLabelOffset = 20;

        double halfScale = scale * 0.5;

        line.setLine(minX - halfScale, 0.0, maxX + halfScale, 0.0);

        FontMetrics fMetrics = g2.getFontMetrics();

        // The widest (== maxiumum) coordinate to draw
        int coordWidth = fMetrics.stringWidth(Integer.toString(max));

        // Minimum gap getween ticks
        double minGap = (double) Math.max(coordWidth, 40);

        // How many symbols does a gap represent?
        int realSymsPerGap = (int) Math.ceil(((minGap + 5.0) / scale));

        // We need to snap to a value beginning 1, 2 or 5.
        double exponent = Math.floor(Math.log(realSymsPerGap) / Math.log(10));
        double characteristic = realSymsPerGap / Math.pow(10.0, exponent);

        int snapSymsPerGap;

        if (characteristic > 5.0) {
            // Use unit ticks
            snapSymsPerGap = (int) Math.pow(10.0, exponent + 1.0);
        } else if (characteristic > 2.0) {
            // Use ticks of 5
            snapSymsPerGap = (int) (5.0 * Math.pow(10.0, exponent));
        } else {
            snapSymsPerGap = (int) (2.0 * Math.pow(10.0, exponent));
        }

        int minP = min + ((snapSymsPerGap - min) % snapSymsPerGap);

        System.out.println(minP + " " + max + " " + snapSymsPerGap);

        for (int index = minP; index <= max; index += snapSymsPerGap) {
            double offset = index * scale;
            String labelString = String.valueOf(index);
            float halfLabelWidth = fMetrics.stringWidth(labelString) / 2;
            line.setLine(offset + halfScale, 0.0, offset + halfScale, tickHeight);
            g2.drawString(String.valueOf(index), (float) ((offset + halfScale) - halfLabelWidth), horizLabelOffset);
            g2.draw(line);
        }

        g.translate((int) ((double) m_min / m_scale), 0);
    }
}


