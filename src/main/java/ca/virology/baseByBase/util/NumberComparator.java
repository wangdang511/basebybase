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
package ca.virology.baseByBase.util;

/**
 * This class compares to objects by first trying to turn them into numbers.
 *
 * @author Ryan Brodie
 */
public class NumberComparator
        implements java.util.Comparator {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected boolean m_absVal;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Constructs a new number comparator
     *
     * @param absoluteValue if true, this will compare the absolute value of
     *                      two numbers
     */
    public NumberComparator(boolean absoluteValue) {
        m_absVal = absoluteValue;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the absolute value flag
     *
     * @return true if this comparitor will compare absolute values
     */
    public boolean getAbsVal() {
        return m_absVal;
    }

    /**
     * compare two gene names.  See the class outline for more info
     *
     * @param obj1 the first object
     * @param obj2 the second object
     * @return a positive number if o1 &gt; o2, negative if o1 &lt; o1  and 0
     * if they are equal.
     * @throws NumberFormatException if the objects passed in aren't strings representing numbers (or numbers themselves)
     */
    public int compare(
            Object obj1,
            Object obj2)
            throws NumberFormatException {
        double d1 = 0.0;
        double d2 = 0.0;

        if (!m_absVal) {
            d1 = new Double(obj1.toString()).doubleValue();
            d2 = new Double(obj2.toString()).doubleValue();
        } else {
            d1 = Math.abs(new Double(obj1.toString()).doubleValue());
            d2 = Math.abs(new Double(obj2.toString()).doubleValue());
        }

        if (d1 == d2) {
            return 0;
        }

        if (d1 > d2) {
            return 1;
        }

        if (d1 < d2) {
            return -1;
        }

        return 0;
    }

    /**
     * compare two comparitors
     *
     * @param o obj to compare to
     * @return true if they're 'equal'
     */
    public boolean equals(Object o) {
        if (o instanceof NumberComparator) {
            if (((NumberComparator) o).getAbsVal() == m_absVal) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
