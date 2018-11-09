package ca.virology.baseByBase.gui.CodeHop;

import javax.swing.*;
import java.awt.*;

public class CodeHopLoadPanel extends JPanel {
    JLabel label;
    static JLabel processingText;
    static JProgressBar progressBar;
    JPanel[][] panelHolder;

    public CodeHopLoadPanel() {

        int rows = 4;
        int cols = 3;
        panelHolder = new JPanel[rows][cols];
        setLayout(new GridLayout(rows, cols));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                panelHolder[i][j] = new JPanel();
                add(panelHolder[i][j]);
            }
        }

        label = new JLabel("Processing Results");
        progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        processingText = new JLabel("Currently Processing... ");

    }

    public void addComponents() {
        panelHolder[1][1].add(label);
        panelHolder[1][1].add(progressBar);
        panelHolder[2][1].add(processingText);
    }
}
