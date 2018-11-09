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
 */
public class VerticalFlowLayout
        implements LayoutManager {
    //~ Instance fields ////////////////////////////////////////////////////////

    private int vgap = 0;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * VerticalFlowLayout constructor comment.
     */
    public VerticalFlowLayout() {
        this(0);
    }

    /**
     * VerticalFlowLayout constructor comment.
     *
     * @param vgap the vertical gap
     */
    public VerticalFlowLayout(int vgap) {
        if (vgap < 0) {
            this.vgap = 0;
        } else {
            this.vgap = vgap;
        }
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * addLayoutComponent method comment.
     *
     * @param name the name of the component
     * @param comp the component itself
     */
    public void addLayoutComponent(
            String name,
            Component comp) {
    }

    /**
     * layoutContainer method comment.
     *
     * @param parent the parent component for this
     */
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int w = parent.getSize().width - insets.left - insets.right;

        // int h = parent.size().height - insets.top - insets.bottom;
        int numComponents = parent.getComponentCount();

        if (numComponents == 0) {
            return;
        }

        int y = insets.top;
        int x = insets.left;

        for (int i = 0; i < numComponents; ++i) {
            Component c = parent.getComponent(i);

            if (c.isVisible()) {
                Dimension d = c.getPreferredSize();

                c.setBounds(x, y, w, d.height);
                y += (d.height + vgap);
            }
        }
    }

    /**
     * minimumLayoutSize method comment.
     *
     * @param parent the parent component
     * @return the minimum size
     */
    public Dimension minimumLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        int maxWidth = 0;
        int totalHeight = 0;
        int numComponents = parent.getComponentCount();

        for (int i = 0; i < numComponents; ++i) {
            Component c = parent.getComponent(i);

            if (c.isVisible()) {
                Dimension cd = c.getMinimumSize();

                maxWidth = Math.max(maxWidth, cd.width);
                totalHeight += cd.height;
            }
        }

        Dimension td =
                new Dimension(maxWidth + insets.left + insets.right,
                        totalHeight + insets.top + insets.bottom +
                                (vgap * numComponents));

        return td;
    }

    /**
     * preferredLayoutSize method comment.
     *
     * @param parent the parent
     * @return the preferred size
     */
    public Dimension preferredLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        int maxWidth = 0;
        int totalHeight = 0;
        int numComponents = parent.getComponentCount();

        for (int i = 0; i < numComponents; ++i) {
            Component c = parent.getComponent(i);

            if (c.isVisible()) {
                Dimension cd = c.getPreferredSize();

                maxWidth = Math.max(maxWidth, cd.width);
                totalHeight += cd.height;
            }
        }

        Dimension td =
                new Dimension(maxWidth + insets.left + insets.right,
                        totalHeight + insets.top + insets.bottom +
                                (vgap * numComponents));

        return td;
    }

    /**
     * removeLayoutComponent method comment.
     *
     * @param comp the component to remove
     */
    public void removeLayoutComponent(Component comp) {
    }
}