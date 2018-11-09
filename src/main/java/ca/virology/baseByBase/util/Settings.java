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

import ca.virology.lib.util.gui.UITools;

import java.awt.Color;

import java.beans.*;

import java.io.*;

import java.net.*;

import java.util.*;


/**
 * This class holds the user settings retrieved from the default file and the
 * users own configuration file
 *
 * @author Ryan Brodie
 */
public class Settings
        extends java.util.Properties {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static Settings c_instance = null;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected String fileSeparator = null;
    protected String javaVersion = null;
    protected String osName = null;
    protected String pathSeparator = null;
    protected String userDir = null;
    protected String userHome = null;
    protected String userName = null;
    protected Map m_nameMap = new HashMap();
    protected Map m_typeMap = new HashMap();
    protected Map m_choiceMap = new HashMap();
    protected Map m_catMap = new HashMap();
    protected Map m_commentMap = new HashMap();
    protected PropertyChangeSupport m_support = new PropertyChangeSupport(this);

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Constructor for the Settings object -- specifies default settings
     */
    protected Settings() {
        initFields();
        initValues();
        loadDefaultSettings();
        loadUserFile();
        saveUserFile();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Gets the instance attribute of the Settings class
     *
     * @return The instance value
     */
    public static Settings getInstance() {
        if (c_instance == null) {
            c_instance = new Settings();
        }

        return c_instance;
    }

    /**
     * add a property change listener
     *
     * @param l the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        m_support.addPropertyChangeListener(l);
    }

    /**
     * remove a property change listener
     *
     * @param l the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        m_support.removePropertyChangeListener(l);
    }

    /**
     * add a property change listener for a specific property
     *
     * @param pname the property
     * @param l     the listener
     */
    public void addPropertyChangeListener(
            String pname,
            PropertyChangeListener l) {
        m_support.addPropertyChangeListener(pname, l);
    }

    /**
     * remove a property change listener
     *
     * @param pname the property
     * @param l     the listener
     */
    public void removePropertyChangeListener(
            String pname,
            PropertyChangeListener l) {
        m_support.removePropertyChangeListener(pname, l);
    }

    /**
     * return true if this object has property change listeners
     *
     * @param property the property
     * @return true if this object has property change listeners
     */
    public boolean hasListeners(String property) {
        return m_support.hasListeners(property);
    }

    public static File getUserPropertiesFile() {
        String userHome = System.getProperty("user.home");
        String fileSeparator = System.getProperty("file.separator", "/");
        if (!userHome.endsWith(fileSeparator)) {
            userHome += fileSeparator;
        }
        return new File(userHome + AppConstants.USER_DIR + fileSeparator + AppConstants.USER_SETTINGS_FILENAME);
    }

    /**
     * set the property to the given value
     *
     * @param key   the property
     * @param value the new value
     * @return the old value
     */
    public Object setProperty(
            String key,
            Object value) {
        Object oldv = getProperty(key);
        Object newv = value;

        put(key, newv);

        m_support.firePropertyChange(key, oldv, newv);

        return oldv;
    }

    /**
     * Load the user settings from file
     *
     * @param file The file name to load from
     * @return True if it succeeded
     */
    public boolean loadSettings(File file) {
        try {
            if (Debug.isOn()) {
                System.out.println("loading settings from: " + file);
            }
            load(new FileInputStream(file));

            //store( System.out, "header" );
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    /**
     * Save the user settings to file
     *
     * @param out The file to load
     */
    public void saveSettings(PrintStream out) {
        try {
            BufferedReader in = null;

            in = new BufferedReader(
                    new StringReader(m_commentMap.get("overview").toString()));

            String ln = null;

            while ((ln = in.readLine()) != null) {
                out.println("#" + ln);
            }

            out.println("#--\n\n");

            Set set = getCategories();

            for (Iterator i = set.iterator(); i.hasNext(); ) {
                String cat = i.next()
                        .toString();
                in = new BufferedReader(
                        new StringReader(m_commentMap.get(cat).toString()));
                ln = null;

                while ((ln = in.readLine()) != null) {
                    out.println("#" + ln);
                }

                out.println("");

                Set settings = getSettings(cat);

                for (Iterator j = settings.iterator(); j.hasNext(); ) {
                    String s = j.next()
                            .toString();
                    out.println("#" + m_commentMap.get(s));
                    out.println(s + "=" + getProperty(s));
                }

                out.println("#--\n\n");
            }

            in = new BufferedReader(
                    new StringReader(m_commentMap.get("system").toString()));
            ln = null;

            while ((ln = in.readLine()) != null) {
                out.println("#" + ln);
            }

            out.println("");
            set = getSettings("system");

            for (Iterator i = set.iterator(); i.hasNext(); ) {
                String s = i.next()
                        .toString();
                out.println("#" + m_commentMap.get(s));
                out.println(s + "=" + getProperty(s));
            }

            out.flush();
        } catch (IOException ex) {
            UITools.showError(ex.getClass() + ": " + ex.getMessage(), null);
            ex.printStackTrace();
        }
    }

    /**
     * Copy the defaults file to the given filename
     *
     * @param file The file to copy to
     * @return True if the operation succeeded
     */
    public boolean copyDefaults(File file) {
        return true;
    }

    /**
     * get the common user oriented name for a setting
     *
     * @param setting the setting to name
     * @return the name of the setting
     */
    public String getSettingName(String setting) {
        if (!m_nameMap.containsKey(setting)) {
            return null;
        }

        return m_nameMap.get(setting)
                .toString();
    }

    /**
     * get the data type of the setting
     *
     * @param setting the setting to check
     * @return the class of the type for the setting
     */
    public Class getSettingType(String setting) {
        if (!m_typeMap.containsKey(setting)) {
            return null;
        }

        return (Class) m_typeMap.get(setting);
    }

    /**
     * get the possible choices for a setting
     *
     * @param setting the setting to check
     * @return a list of possible choices
     */
    public List getSettingChoices(String setting) {
        if (!m_choiceMap.containsKey(setting)) {
            return null;
        }

        return (List) m_choiceMap.get(setting);
    }

    /**
     * get the possible categories for user preferences
     *
     * @return a set of categories (strings)
     */
    public Set getCategories() {
        return new TreeSet(m_catMap.keySet());
    }

    /**
     * get the name of a given category
     *
     * @param cat the category
     * @return the name of the category
     */
    public String getCategoryName(String cat) {
        return m_catMap.get(cat)
                .toString();
    }

    /**
     * get all the settings for a given category
     *
     * @param category the category
     * @return a set of setting names
     */
    public Set getSettings(String category) {
        TreeSet ret = new TreeSet();

        Enumeration e = propertyNames();

        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();

            if (s.startsWith(category)) {
                ret.add(s);
            }
        }

        return ret;
    }

    /**
     * Load the user settings from their own file
     */
    public void loadUserFile() {
        String userFilename = userHome + AppConstants.USER_DIR + fileSeparator + AppConstants.USER_SETTINGS_FILENAME;

        try {
            File userFile = new File(userFilename);

            if (!userFile.exists()) {
                // do nothing -- yet
            } else {
                if (Debug.isOn()) {
                    System.out.println("loading user settings from: " + userFilename);
                }
                FileInputStream in = new FileInputStream(userFile);
                load(in);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Save settings to the users own settings file
     */
    public void saveUserFile() {
        String userFilename = userHome + AppConstants.USER_DIR + fileSeparator + AppConstants.USER_SETTINGS_FILENAME;
        String userDir = userHome + AppConstants.USER_DIR;

        File dir = new File(userDir);

        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            PrintStream out =
                    new PrintStream(new FileOutputStream(userFilename));
            saveSettings(out);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Load the defaults from the file
     */
    public void loadDefaultSettings() {
//		System.out.println("> loadDefaultSettings()");
        ClassLoader cl = this.getClass().getClassLoader();
//		System.out.println("  cl: "+cl);
        URL defaultsURL = cl.getResource(AppConstants.SETTINGS_FILENAME);
//		System.out.println("  filename: "+AppConstants.SETTINGS_FILENAME);
//		System.out.println("  url: "+defaultsURL);
        if (Debug.isOn()) {
            System.out.println("loading defaults from: " + defaultsURL);
        }

        // represses exception
        if (defaultsURL == null) return;
        // end

        try {
            load(defaultsURL.openStream());
        } catch (Exception ex) {
            UITools.showError(ex.getClass() + ": " + ex.getMessage(), null);
            ex.printStackTrace();
        }
//		System.out.println("< loadDefaultSettings()");
    }

    /**
     * initialize the setting values
     */
    protected void initValues() {
        ArrayList boolChoice = new ArrayList();
        boolChoice.add(new Boolean(true));
        boolChoice.add(new Boolean(false));

        m_catMap.put("gui", "Display / Usability Settings");
        m_catMap.put("warn", "Warning/Confirmation Message Settings");

        //m_catMap.put( "color", "Colours Used in this Application" );
        m_nameMap.put("color.baset", "T base BG Colour");
        m_typeMap.put("color.baset", Color.class);
        m_choiceMap.put("color.baset", new ArrayList());
        m_commentMap.put("color.baset", m_nameMap.get("color.baset") + " [4 field hex code (rgba)]");

        m_nameMap.put("warn.onQuit", "Show message when quitting");
        m_typeMap.put("warn.onQuit", Boolean.class);
        m_choiceMap.put("warn.onQuit", boolChoice.clone());
        m_commentMap.put("warn.onQuit", m_nameMap.get("warn.onQuit") + " [true, false]");

        m_nameMap.put("warn.onAlign", "Show message when substituting aligned sequence");
        m_typeMap.put("warn.onAlign", Boolean.class);
        m_choiceMap.put("warn.onAlign", boolChoice.clone());
        m_commentMap.put("warn.onAlign", m_nameMap.get("warn.onAlign") + " [true, false]");

		/*
        m_nameMap.put("gui.showAcidsFrame1", "Frame 1: AA translation (with genes)  ");
        m_typeMap.put("gui.showAcidsFrame1", Boolean.class);
        m_choiceMap.put(
            "gui.showAcidsFrame1",
            boolChoice.clone());
        m_nameMap.put("gui.showAcidsFrame2", "Frame 2: AA translation (with genes)  ");
        m_typeMap.put("gui.showAcidsFrame2", Boolean.class);
        m_choiceMap.put(
            "gui.showAcidsFrame2",
            boolChoice.clone());
        m_nameMap.put("gui.showAcidsFrame3", "Frame 3: AA translation (with genes)  ");
        m_typeMap.put("gui.showAcidsFrame3", Boolean.class);
        m_choiceMap.put(
            "gui.showAcidsFrame3",
            boolChoice.clone());
		 */
        m_nameMap.put("gui.showAcidArrows", "Show AA Arrows ('===>')  ");
        m_typeMap.put("gui.showAcidArrows", Boolean.class);
        m_choiceMap.put("gui.showAcidArrows", boolChoice.clone());
        m_commentMap.put("gui.showAcidArrows", m_nameMap.get("gui.showAcidArrows") + " [true, false]");

        m_nameMap.put("gui.showDifferences", "Show events and differences  ");
        m_typeMap.put("gui.showDifferences", Boolean.class);
        m_choiceMap.put("gui.showDifferences", boolChoice.clone());
        m_commentMap.put("gui.showDifferences", m_nameMap.get("gui.showDifferences") + " [true, false]");

        m_nameMap.put("gui.showScale", "Show gapped position scale  ");
        m_typeMap.put("gui.showScale", Boolean.class);
        m_choiceMap.put("gui.showScale", boolChoice.clone());
        m_commentMap.put("gui.showScale", m_nameMap.get("gui.showScale") + " [true, false]");

        m_nameMap.put("gui.showSeqScale", "Show sequence position scale  ");
        m_typeMap.put("gui.showSeqScale", Boolean.class);
        m_choiceMap.put("gui.showSeqScale", boolChoice.clone());
        m_commentMap.put("gui.showSeqScale", m_nameMap.get("gui.showSeqScale") + " [true, false]");

        m_nameMap.put("gui.showUserEvents", "Show user comments/annotations  ");
        m_typeMap.put("gui.showUserEvents", Boolean.class);
        m_choiceMap.put("gui.showUserEvents", boolChoice.clone());
        m_commentMap.put("gui.showUserEvents", m_nameMap.get("gui.showUserEvents") + " [true, false]");

        m_nameMap.put("gui.showPrimers", "Show primers  ");    //primer
        m_typeMap.put("gui.showPrimers", Boolean.class);
        m_choiceMap.put("gui.showPrimers", boolChoice.clone());
        m_commentMap.put("gui.showPrimers", m_nameMap.get("gui.showPrimers") + " [true, false]");

        m_nameMap.put("gui.hilightSSCodons", "Hilight Start/Stop Codons with Arrows  ");
        m_typeMap.put("gui.hilightSSCodons", Boolean.class);
        m_choiceMap.put("gui.hilightSSCodons", boolChoice.clone());
        m_commentMap.put("gui.hilightSSCodons", m_nameMap.get("gui.hilightSSCodons") + " [true, false]");

        m_nameMap.put("gui.use.propEdit", "Propogate edits to marked sequences");
        m_typeMap.put("gui.use.propEdit", Boolean.class);
        m_choiceMap.put("gui.use.propEdit", boolChoice.clone());
        m_commentMap.put("gui.use.propEdit", m_nameMap.get("gui.use.propEdit") + " [true, false]");

        //m_nameMap.put("gui.use.propSelect",
        //    "Propogate selections to selected sequences");
        //m_typeMap.put("gui.use.propSelect", Boolean.class);
        //m_choiceMap.put(
        //    "gui.use.propSelect",
        //    boolChoice.clone());
        //m_commentMap.put("gui.use.propSelect",
        //    m_nameMap.get("gui.use.propSelect") + " [true, false]");
        m_commentMap.put("system.dbtype",
                "Database type [VOCS] -- Do not yet customizable");

        m_commentMap.put("overview",
                "Base-by-Base Configuration File\n" +
                        "-------------------------------\n" +
                        "This file is automatically generated by\n" +
                        "Base-by-Base when settings are modified,\n" +
                        "however changes in this file will be refleacted\n" +
                        "next time you start Base-by-Base");

        m_commentMap.put("color",
                "color.* ---\n" +
                        "Colours used in this application can be set here\n" +
                        "but it would probably be easiest to set these in\n" +
                        "the application itself as there is a color chooser\n" +
                        "dialog to help with the process");

        m_commentMap.put("gui",
                "gui.* ---\n" + "User interface configuration that can be\n" +
                        "edited here or inside the program.  Changes\n" +
                        "here will not be seen until the program is\n" + "restarted.");

        m_commentMap.put("warn",
                "warn.* ---\n" +
                        "These refer to warning messages that can appear\n" +
                        "during the regular usage of the program.  If you want\n" +
                        "to prevent any of these messages, set the appropriate\n" +
                        "setting to false");

        m_commentMap.put("system",
                "system.* ---\n" +
                        "System configuration.  These settings affect\n" +
                        "the database connections and other 'under the hood'\n" +
                        "items.  Do not edit these unless you have a good\n" +
                        "idea what they will be used for");
    }

    /**
     * init the other fields used to process settings files
     */
    protected void initFields() {
        try {
            fileSeparator = System.getProperty("file.separator", "/");
            javaVersion = System.getProperty("java.version");
            osName = System.getProperty("os.name");
            pathSeparator = System.getProperty("path.separator", ":");
            userDir = System.getProperty("user.dir");

            if (!userDir.endsWith(fileSeparator)) {
                userDir += fileSeparator;
            }

            userHome = System.getProperty("user.home");

            if (!userHome.endsWith(fileSeparator)) {
                userHome += fileSeparator;
            }

            userName = System.getProperty("user.name");

            if (userName == null) {
                Random r = new Random();
                userName = "a" + r.nextInt();
            }

            userName = userName.replace(' ', '_');
        } catch (Exception ex) {
            UITools.showError(ex.getClass() + ": " + ex.getMessage(), null);
            ex.printStackTrace();
        }
    }
}
