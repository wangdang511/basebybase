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

import java.awt.*;


/**
 * This class handles slightly more complex graphics tasks than are provided by
 * the java2D graphics model.
 *
 * @author Ryan Brodie
 */
public final class GraphicsTools {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * This draws a stroked arrow on a given graphics object
     *
     * @param g     The object to draw to
     * @param paint The paint to use when drawing
     * @param x1    The end position of the tail of the arrow
     * @param x2    The end position of the head of the arrow
     * @param y1    The top of the arrow
     * @param y2    the bottom of the arrow
     */
    public static void drawArrow(Graphics2D g, Paint paint, int x1, int x2, int y1, int y2) {
        int[] x = new int[6];
        int[] y = new int[6];
        int min = Math.min(x1, x2);
        int max = Math.max(x1, x2);

        boolean lead = x1 > x2;

        int height = y2 - y1;

        int arrowSize = 0;

        if ((max - min) < (height / 2)) {
            arrowSize = max - min;
        } else {
            arrowSize = (int) height / 2;
        }

        if (lead) {
            x[0] = max;
            y[0] = y1;
            x[1] = max - arrowSize;
            y[1] = y1 + (height / 2);
            x[2] = max;
            y[2] = y1 + height;
            x[3] = min + arrowSize;
            y[3] = y1 + height;
            x[4] = min;
            y[4] = y1 + (height / 2);
            x[5] = min + arrowSize;
            y[5] = y1;
        } else {
            x[0] = min;
            y[0] = y1;
            x[1] = min + arrowSize;
            y[1] = y1 + (height / 2);
            x[2] = min;
            y[2] = y1 + height;
            x[3] = max - arrowSize;
            y[3] = y1 + height;
            x[4] = max;
            y[4] = y1 + (height / 2);
            x[5] = max - arrowSize;
            y[5] = y1;
        }

        Polygon poly = new Polygon(x, y, 6);
        g.setPaint(paint);
        g.draw(poly);
    }

    /**
     * This draws a filled arrow on a given graphics object
     *
     * @param g     The object to draw to
     * @param paint The paint to use when drawing
     * @param x1    The end position of the tail of the arrow
     * @param x2    The end position of the head of the arrow
     * @param y1    The top of the arrow
     * @param y2    the bottom of the arrow
     */
    public static void fillArrow(Graphics2D g, Paint paint, int x1, int x2, int y1, int y2) {
        int[] x = new int[6];
        int[] y = new int[6];
        int min = Math.min(x1, x2);
        int max = Math.max(x1, x2);

        boolean lead = x1 > x2;

        int height = y2 - y1;

        int arrowSize = 0;

        if ((max - min) < (height / 2)) {
            arrowSize = max - min;
        } else {
            arrowSize = (int) height / 2;
        }

        if (lead) {
            x[0] = max;
            y[0] = y1;
            x[1] = max - arrowSize;
            y[1] = y1 + (height / 2);
            x[2] = max;
            y[2] = y1 + height;
            x[3] = min + arrowSize;
            y[3] = y1 + height;
            x[4] = min;
            y[4] = y1 + (height / 2);
            x[5] = min + arrowSize;
            y[5] = y1;
        } else {
            x[0] = min;
            y[0] = y1;
            x[1] = min + arrowSize;
            y[1] = y1 + (height / 2);
            x[2] = min;
            y[2] = y1 + height;
            x[3] = max - arrowSize;
            y[3] = y1 + height;
            x[4] = max;
            y[4] = y1 + (height / 2);
            x[5] = max - arrowSize;
            y[5] = y1;
        }

        Polygon poly = new Polygon(x, y, 6);
        g.setPaint(paint);
        g.fill(poly);
    }


    public static void drawFilledCurvedArrowHead(Graphics g, boolean goesright, Color c, int x, int y, int w, int h) {
        g.setColor(c);
        if (goesright) {
            g.fillArc(x - w, y, w + w, h - 1, 270, 180);
        } else {
            g.fillArc(x, y, w + w, h - 1, 90, 180);
        }
    }

    public static void drawFilledArrowTail(Graphics g, boolean goesright, Color c, int x, int y, int w, int h) {
        g.setColor(c);
        //the points just trace basically two triangles, from the bottom right, to the right, then up and around.

        int[] xpoints = new int[5];
        int[] ypoints = new int[5];

        if (goesright) {
            xpoints[0] = x;
            xpoints[1] = x + w;
            xpoints[2] = x + w;
            xpoints[3] = x;
            xpoints[4] = x + w / 2;

            ypoints[0] = y + h - 1;
            ypoints[1] = y + h - 1;   //the -1 is to fix some weird extra pixel that is showing up, boxs arent alligned with this unless we add it
            ypoints[2] = y;
            ypoints[3] = y;
            ypoints[4] = y + h / 2;
        } else {
            xpoints[0] = x;
            xpoints[1] = x + w;
            xpoints[2] = x + w / 2;
            xpoints[3] = x + w;
            xpoints[4] = x;

            ypoints[0] = y + h - 1;
            ypoints[1] = y + h - 1;   //the -1 is to fix some weird extra pixel that is showing up, boxs arent alligned with this unless we add it
            ypoints[2] = y + h / 2;
            ypoints[3] = y;
            ypoints[4] = y;
        }

        g.fillPolygon(xpoints, ypoints, 5);
    }
    /**
     * This draws a filled rectangle on a given graphics object
     *
     * @param g     The object to draw to
     * @param paint The paint to use when drawing
     * @param x1    The leftmost
     * @param x2    The end position of the head of the arrow
     * @param y1    The top of the arrow
     * @param y2    the bottom of the arrow
     */
    public static void fillRectangle(Graphics2D g, Paint paint, int x1, int x2, int y1, int y2) {
        int[] x = new int[4];
        int[] y = new int[4];
        int min = Math.min(x1, x2);
        int max = Math.max(x1, x2) ;

        x[0] = min;
        y[0] = y1;
        x[1] = min;
        y[1] = y2;
        x[2] = max;
        y[2] = y2;
        x[3] = max;
        y[3] = y1;


        Polygon poly = new Polygon(x, y, 4);
        g.setPaint(paint);
        g.fill(poly);
    }
    /**
     * This draws a stroked rectangle on a given graphics object
     *
     * @param g     The object to draw to
     * @param paint The paint to use when drawing
     * @param x1    The leftmost x coordinate of the rectangle
     * @param x2    The rightmost x coordinate of the rectangle
     * @param y1    The top left y coordinate of the rectangle
     * @param y2    the bottom left y coordinate of rectangle
     */
    public static void drawRectangle(Graphics2D g, Paint paint, int x1, int x2, int y1, int y2) {

        int min = Math.min(x1, x2);
        int max = Math.max(x1, x2);

        g.setStroke(new BasicStroke(1));
        g.setPaint(paint);
        g.drawRect(min, y1, max -  min, y2);
    }

}