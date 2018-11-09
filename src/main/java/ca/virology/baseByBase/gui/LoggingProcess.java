package ca.virology.baseByBase.gui;

public abstract class LoggingProcess extends Thread {
    protected boolean stopped = false;
    protected String log = "";
    protected String status = "Idle";

    public String getLog() {
        return log;
    }

    public void stopProcess() {
        stopped = true;
    }
}
