package ca.virology.baseByBase.util;

public class Debug {

    public static Debug m_debug = null;
    public boolean m_bool = false;

    private Debug() {
        m_bool = false;
    }

    private static Debug getInstance() {
        if (m_debug == null) {
            m_debug = new Debug();
        }

        return m_debug;
    }

    public static boolean isOn() {
        Debug d = getInstance();
        return (d.getDebug());
    }

    public static void setDebugging(boolean debug) {
        Debug d = getInstance();
        if (debug) {
            System.out.println("Utils.Debug: turning DEBUG mode ON");
        }
        d.setDebug(debug);
    }

    private boolean getDebug() {
        return m_bool;
    }

    private void setDebug(boolean debug) {
        m_bool = debug;
    }
}
