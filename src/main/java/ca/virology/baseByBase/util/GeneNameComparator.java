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
 * This will compare two strings by breaking them down into sets of numbers and
 * letters and comparing the sets with each other.  This will have the effect
 * of allowing for proper mixed value sorting ( such as having numbers sort
 * 1a,2a,10b,20b instead of 1a,10b,2a,20b as normal ).
 */
public class GeneNameComparator
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
        String s1 = o1.toString();
        String s2 = o2.toString();

        String[] val1 = new String[20];
        String[] val2 = new String[20];

        GenericComparator gc = new GenericComparator();
        NumberComparator nc = new NumberComparator(false);

        StringBuffer val = new StringBuffer();
        boolean digit = false;
        boolean decimal = false;
        int seg = 0;

        for (int i = 0; i < s1.length(); ++i) {
            if (Character.isDigit(s1.charAt(i))) {
                if (digit) {
                    val.append(s1.charAt(i));
                } else {
                    if (val.length() > 0) {
                        val1[seg++] = val.toString();
                        val = new StringBuffer();
                    }

                    val.append(s1.charAt(i));
                    digit = true;
                }
            } else if (s1.charAt(i) == '.') {
                if (digit) {
                    if (decimal) { // 2nd decimal is just plain text
                        digit = false;
                        decimal = false;

                        if (val.length() > 0) {
                            val1[seg++] = val.toString();
                            val = new StringBuffer();
                        }

                        val.append(s1.charAt(i));
                    } else {
                        val.append('.');
                        decimal = true;
                    }
                } else {
                    val.append(s1.charAt(i));
                }
            } else if (Character.isWhitespace(s1.charAt(i))) {
                continue;
            } else { //text

                if (digit) {
                    digit = false;

                    if (val.length() > 0) {
                        val1[seg++] = val.toString();
                        val = new StringBuffer();
                    }

                    val.append(s1.charAt(i));
                } else {
                    val.append(s1.charAt(i));
                }
            }
        }

        if (val.length() > 0) {
            val1[seg] = val.toString();
        }

        val = new StringBuffer();
        digit = false;
        decimal = false;
        seg = 0;

        for (int i = 0; i < s2.length(); ++i) {
            if (Character.isDigit(s2.charAt(i))) {
                if (digit) {
                    val.append(s2.charAt(i));
                } else {
                    if (val.length() > 0) {
                        val2[seg++] = val.toString();
                        val = new StringBuffer();
                    }

                    val.append(s2.charAt(i));
                    digit = true;
                }
            } else if (s2.charAt(i) == '.') {
                if (digit) {
                    if (decimal) { // 2nd decimal is just plain text
                        digit = false;
                        decimal = false;

                        if (val.length() > 0) {
                            val2[seg++] = val.toString();
                            val = new StringBuffer();
                        }

                        val.append(s2.charAt(i));
                    } else {
                        val.append('.');
                        decimal = true;
                    }
                } else {
                    val.append(s2.charAt(i));
                }
            } else if (Character.isWhitespace(s2.charAt(i))) {
                continue;
            } else { //text

                if (digit) {
                    digit = false;

                    if (val.length() > 0) {
                        val2[seg++] = val.toString();
                        val = new StringBuffer();
                    }

                    val.append(s2.charAt(i));
                } else {
                    val.append(s2.charAt(i));
                }
            }
        }

        if (val.length() > 0) {
            val2[seg] = val.toString();
        }

        int ret = 0;

        for (int i = 0; i < val1.length; ++i) {
            if (((val1[i] == null) || (val2[i] == null)) ||
                    (val1[i].equals("") || val2.equals(""))) {
                ret = 0;

                break;
            } else if (Character.isDigit(val1[i].charAt(0)) &&
                    !Character.isDigit(val2[i].charAt(0))) {
                ret = 1;

                break;
            } else if (!Character.isDigit(val1[i].charAt(0)) &&
                    Character.isDigit(val2[i].charAt(0))) {
                ret = -1;

                break;
            } else {
                if (Character.isDigit(val1[i].charAt(0))) { //numbers
                    ret = nc.compare(val1[i], val2[i]);
                } else { // text
                    ret = gc.compare(val1[i], val2[i]);
                }
            }

            if (ret != 0) {
                break;
            }
        }

        return ret;
    }

    /**
     * compare two comparitors
     *
     * @param o obj to compare to
     * @return true if they're 'equal'
     */
    public boolean equals(Object o) {
        if (o instanceof GeneNameComparator) {
            return true;
        } else {
            return false;
        }
    }
}