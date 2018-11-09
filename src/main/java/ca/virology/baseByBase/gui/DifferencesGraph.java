package ca.virology.baseByBase.gui;

import ca.virology.baseByBase.util.Debug;
import ca.virology.lib.io.MultiFileFilter;
import ca.virology.lib.io.sequenceData.AnnotationKeys;
import ca.virology.lib.io.sequenceData.DifferenceType;
import ca.virology.lib.io.sequenceData.FeatureType;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib2.ui.GuiUtils;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Deprecated
public class DifferencesGraph extends JFrame {
    protected Plot m_plot;
    protected int[][] m_data;

    public DifferencesGraph(String title, int[][] data) {
        m_data = data;
        initPlot(500, 0);

        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        rootPanel.add(m_plot, BorderLayout.CENTER);

        JPanel textPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        final JSpinner binSizeSpinner = new JSpinner(new SpinnerNumberModel(500, 1, 100000, 1));
        final JSpinner offsetSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 499, 1));
        binSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                resetPlot((Integer) binSizeSpinner.getValue(), (Integer) offsetSpinner.getValue());

                SpinnerModel offsetSpinnerModel = offsetSpinner.getModel();
                if (offsetSpinnerModel instanceof SpinnerNumberModel) {
                    ((SpinnerNumberModel) offsetSpinnerModel).setMaximum((Integer) binSizeSpinner.getValue() - 1);
                    if ((Integer) offsetSpinner.getValue() > (Integer) binSizeSpinner.getValue() - 1) {
                        offsetSpinner.setValue((Integer) binSizeSpinner.getValue() - 1);
                    }
                }
            }
        });
        offsetSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                resetPlot((Integer) binSizeSpinner.getValue(), (Integer) offsetSpinner.getValue());
            }
        });


        c.gridx = 0;
        c.gridy = 0;
        textPanel.add(new JLabel(" Bin Size: "), c);
        c.gridx = 1;
        c.gridy = 0;
        textPanel.add(binSizeSpinner, c);
        c.gridx = 2;
        c.gridy = 0;
        textPanel.add(new JLabel(" Offset: "), c);
        c.gridx = 3;
        c.gridy = 0;
        textPanel.add(offsetSpinner, c);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));


        JButton exportButton = new JButton("Export CSV File");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = GuiUtils.showFileChooser("Export to CSV File", new String[]{".csv"}, GuiUtils.FileChooserMode.SAVE);

                if (f != null) {

                    try {
                        if (!f.exists()) {
                            f.createNewFile();
                        }
                        BufferedWriter br = new BufferedWriter(new FileWriter(f));
                        for (int[] dataSet : m_data) {
                            //todo: differentiate between data sets?
                            br.write(histogramToCSV(createHistogram(dataSet, (Integer) binSizeSpinner.getValue(), (Integer) offsetSpinner.getValue())));
                            br.newLine();
                        }
                        br.flush();
                        br.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });


        buttonsPanel.add(exportButton);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 4;
        textPanel.add(buttonsPanel, c);
        textPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        rootPanel.add(textPanel, BorderLayout.SOUTH);
        this.add(rootPanel);
        pack();
        setTitle(title);
        setLocationRelativeTo(null);
        repaint();
    }

    public static DifferencesGraph createFindDifferencesGraph(String title, List<Integer> values) {
        int[][] data = new int[1][values.size()];
        for (int i = 0; i < values.size(); i++) {
            data[0][i] = values.get(i);
        }
        return new DifferencesGraph(title, data);
    }


    //Returns int[2][numBins] histogram where histogram[0] is the stop index and histogram[1] is the count
    public static int[][] createHistogram(int[] values, int binSize, int offset) {
        int numBins;
        int maxValue = values[0];

        for (Integer value : values) {
            if (value > maxValue) {
                maxValue = value;
            }
        }

        offset = offset % binSize;
        if (offset == 0) {
            offset = binSize;
        }
        numBins = (maxValue - offset) / binSize + 2;

        int[][] histogram = new int[2][numBins];
        for (int i = 0; i < numBins; i++) {
            histogram[0][i] = offset + i * binSize;
            histogram[1][i] = 0;
        }
        for (Integer value : values) {
            if (value <= offset) {
                histogram[1][0] += 1;
            } else {
                int index = (value - offset) / binSize + 1;
                histogram[1][index] += 1;
            }
        }
        //System.out.println(histogramToCSV(histogram));
        return histogram;
    }

    public static String histogramToCSV(int[][] histogram) {
        String csv = "bin_stop, count\n";
        for (int i = 0; i < histogram[0].length; i++) {
            csv += histogram[0][i] + "," + histogram[1][i] + "\n";
        }
        return csv;
    }

    private void resetPlot(int binSize, int offset) {
        int longestSequenceLength = 0;
        int maxCount = 0;
        for (int i = 0; i < m_data.length; i++) {

            m_plot.clear(i);
            int[][] histogram = createHistogram(m_data[i], binSize, offset);
            int sequenceLength = (createHistogram(m_data[i], binSize, 0)[0].length + 1) * binSize;
            int count = 0;
            m_plot.addPoint(i, 0, 0, true);
            for (int j = 0; j < histogram[0].length; j++) {
                if (histogram[1][j] > maxCount) {
                    maxCount = histogram[1][j];
                }
                m_plot.addPoint(i, histogram[0][j], histogram[1][j], true);
            }

            if (sequenceLength > longestSequenceLength) {
                longestSequenceLength = sequenceLength;
            }
            if (count > maxCount) {
                maxCount = count;
            }

            m_plot.addPoint(i, sequenceLength, 0, true);
        }
        // set range and zoom to center graph
        m_plot.setXRange(0, longestSequenceLength);
        m_plot.setYRange(0, maxCount);

        m_plot.zoom(0, 0, longestSequenceLength, maxCount);
    }

    /**
     * Creates the sequence similarity graph and adds it to the containing frame.
     */
    private void initPlot(int binSize, int offset) {
        m_plot = new Plot();

        int longestSequenceLength = 0;
        int maxCount = 0;
        for (int i = 0; i < m_data.length - 1; i++) {
            int[][] histogram = createHistogram(m_data[i], binSize, offset);
            //todo: make this more efficient
            int sequenceLength = ((createHistogram(m_data[i], binSize, 0)[0].length + 1)) * binSize;
            int count = 0;
            m_plot.addPoint(i, 0, 0, true);
            for (int j = 0; j < histogram[0].length; j++) {
                if (histogram[1][j] > count) {
                    count = histogram[1][j];
                }
                m_plot.addPoint(i, histogram[0][j], histogram[1][j], true);
            }
            if (sequenceLength > longestSequenceLength) {
                longestSequenceLength = sequenceLength;
            }
            if (count > maxCount) {
                maxCount = count;
            }
            m_plot.addPoint(i, sequenceLength, 0, true);
        }

        // set range and zoom to center graph
        m_plot.setXRange(0, longestSequenceLength);
        m_plot.setYRange(0, maxCount);

        m_plot.setXLabel("Sequence Position");
        m_plot.setYLabel("Differences");

        m_plot.zoom(0, 0, longestSequenceLength, maxCount);


    }
}
