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
 * This is a generic string-based comparator
 */
public class GenericComparator
        implements java.util.Comparator {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * compare two gene names.  See the class outline for more info
     *
     * @param o1 the first object
     * @param o2 the second object
     * @return a positive number if o1 &gt; o2, negative if o1 &lt; o1  and 0
     * if they are equal.
     */
    public int compare(
            Object o1,
            Object o2) {
        return o1.toString()
                .compareTo(o2.toString());
    }

    /**
     * compare two comparitors
     *
     * @param o obj to compare to
     * @return true if they're 'equal'
     */
    public boolean equals(Object o) {
        if (o instanceof GenericComparator) {
            return true;
        } else {
            return false;
        }
    }
}