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
 * This contains application constants
 *
 * @author Ryan Brodie
 */
public class AppConstants {
    //~ Static fields/initializers /////////////////////////////////////////////

    public static String APP_TITLE = "Base-by-Base: Sequence Alignment Annotation";

    /**
     * The settings filename for user configuration
     */
    public static final String SETTINGS_FILENAME = "conf/bbbase.config";

    /**
     * The settings filename to store in the user home directory
     */
    public static final String USER_SETTINGS_FILENAME = "bbbase.config";

    /**
     * the directory where settings information is kept
     */
    public static final String USER_DIR = ".bbb";
    protected static ca.virology.baseByBase.data.FeaturedSequenceModel c_holder = null;

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the current sequence holder for the app
     *
     * @return The currently active sequence holder
     */
    public static synchronized ca.virology.baseByBase.data.FeaturedSequenceModel getSequenceHolder() {
        return c_holder;
    }

    /**
     * Set the current sequence holder for the app
     *
     * @param holder The sequence holder
     */
    public static synchronized void setSequenceHolder(
            ca.virology.baseByBase.data.FeaturedSequenceModel holder) {
        c_holder = holder;
    }
}
