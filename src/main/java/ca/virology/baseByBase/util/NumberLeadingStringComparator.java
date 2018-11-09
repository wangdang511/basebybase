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
 * This will compare two strings based on their leading digits.  If the strings
 * don't start with digits, exceptions may be thrown
 *
 * @author Ryan Brodie
 */
public class NumberLeadingStringComparator
        implements java.util.Comparator {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected boolean m_absVal;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new NumberLeadingStringComparator object.
     *
     * @param absoluteValue if true, this will compare the numbers in  terms of
     *                      their absolute value.
     */
    public NumberLeadingStringComparator(boolean absoluteValue) {
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
     * @throws NumberFormatException if the objects passed in don't match the syntax properly
     */
    public int compare(
            Object obj1,
            Object obj2)
            throws NumberFormatException {
        String s1 = obj1.toString();
        String s2 = obj2.toString();

        int i = 0;

        if (s1.startsWith("-")) {
            i = 1;
        }

        for (; i < s1.length(); ++i) {
            if (!Character.isDigit(s1.charAt(i)) && (s1.charAt(i) != '.')) {
                break;
            }
        }

        int j = 0;

        if (s2.startsWith("-")) {
            j = 1;
        }

        for (; j < s2.length(); ++j) {
            if (!Character.isDigit(s2.charAt(j)) && (s2.charAt(j) != '.')) {
                break;
            }
        }

        double d1 = 0.0;
        double d2 = 0.0;

        if (!m_absVal) {
            d1 = new Double(s1.substring(0, i)).doubleValue();
            d2 = new Double(s2.substring(0, j)).doubleValue();
        } else {
            d1 = Math.abs(new Double(s1.substring(0, i)).doubleValue());
            d2 = Math.abs(new Double(s2.substring(0, j)).doubleValue());
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
        if (o instanceof NumberLeadingStringComparator) {
            if (((NumberLeadingStringComparator) o).getAbsVal() == m_absVal) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
