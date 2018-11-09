package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.AnnotationKeys;
import ca.virology.lib.io.sequenceData.DifferenceType;
import ca.virology.lib.io.sequenceData.FeatureType;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.util.gui.GuiDefaults;
import ca.virology.lib.util.gui.UITools;
import ca.virology.lib2.ui.GuiUtils;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.util.ShapeUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class DifferencesChart extends JFrame {
    protected JFreeChart chart;
    protected DefaultCategoryDataset dataset;
    protected DefaultCategoryDataset datasetAll; //Copy of the original

    int[][] data; //Difference positions for each series of differences
    int dataLength; //Length of the sequence
    Paint[] seriesPaints;
    String[] seriesNames;
    String[] categories; //Initial chart intervals

    /*#######################################################################>>>>>
    * Purpose: Creates the chart window interface and buttons.
    * Includes all the action listeners for this file in this method.
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    private DifferencesChart(List<Integer> pos, List<String> names, List<Integer> count, int[][] data, int dataLength, String[] seriesNames, Paint[] seriesPaints, ChartType chartType, String title, String hAxisName, String vAxisName, boolean legend, boolean tooltips, boolean urls) {
        this.data = data;
        this.dataLength = dataLength;
        this.seriesNames = seriesNames;
        this.seriesPaints = seriesPaints;

        dataset = new DefaultCategoryDataset();

        /*#######################################################################
        * Chart Initialization Settings   >>----->>>>>
        * #######################################################################*/

        int setBinStart = 10;
        int setBinCount;

        if (dataLength < 1000) {
            categories = new String[10];
            setBinCount = updateDataset(10, 0);
        } else {
            categories = new String[100];
            setBinCount = updateDataset(100, 0);
            setBinStart = 100;
        }

        /*#######################################################################
        * Chart Initialization Settings   <<<<<-----<<
        * #######################################################################*/

        this.chart = createChart(chartType, null, hAxisName, vAxisName, dataset, PlotOrientation.VERTICAL, legend, tooltips, urls);

        if (this.seriesNames == null) {
            this.seriesNames = new String[data.length];
        }
        CategoryItemRenderer renderer = chart.getCategoryPlot().getRenderer();
        if (this.seriesPaints == null) {
            this.seriesPaints = new Color[data.length];

            for (int i = 0; i < data.length; i++) {
                this.seriesPaints[i] = ((AbstractRenderer) renderer).lookupSeriesPaint(i);
            }
        } else {
            for (int i = 0; i < data.length; i++) {
                renderer.setSeriesPaint(i, this.seriesPaints[i]);
            }
        }

        ChartPanel chartPanel = new ChartPanel(chart);

        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        rootPanel.add(chartPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JPanel textPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        final JSpinner binCountSpinner = new JSpinner(new SpinnerNumberModel(setBinStart, 1, 4000, 1));
        final JSpinner offsetSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 4000, 1));

        c.gridx = 0;
        c.gridy = 0;
        textPanel.add(new JLabel(" Bin Count: "), c);
        c.gridx = 1;
        c.gridy = 0;
        textPanel.add(binCountSpinner, c);
        c.gridx = 2;
        c.gridy = 0;
        textPanel.add(new JLabel(" Offset: "), c);
        c.gridx = 3;
        c.gridy = 0;
        textPanel.add(offsetSpinner, c);

        final JComboBox chartTypeComboBox = new JComboBox(ChartType.values());
        chartTypeComboBox.setSelectedItem(chartType);
        chartTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DifferencesChart.this.setChartType((ChartType) (chartTypeComboBox.getSelectedItem()));
            }
        });

        c.gridx = 4;
        c.gridy = 0;
        textPanel.add(new JLabel(" Chart Type: "), c);
        c.gridx = 5;
        c.gridy = 0;
        textPanel.add(chartTypeComboBox, c);

        final List<String> uniqueNames = new ArrayList<String>();
        uniqueNames.add("Perfect Match");
        boolean isThere = false;

        //Following code takes the names and puts them in an array such that they uniquely appear
        for (int i = 0; i < names.size(); i++) {
            String[] bits = names.get(i).toString().split("_ ");
            for (int j = 0; j < bits.length; j++) {
                String on = bits[j];
                on = on.replaceAll(" ", "");
                if (on.equals("[empty]")) {
                    break;
                }

                for (int k = 0; k < uniqueNames.size(); k++) {
                    if (on.equals(uniqueNames.get(k))) {   //Duplicate detection mechanism
                        isThere = true;
                        break;
                    }
                }
                if (isThere == false) {
                    uniqueNames.add(on);
                }
                isThere = false;
            }
        }

        if (names != null) {
            textPanel.add(new JLabel(" Tolerated genomes: "));
            final JComboBox tolgenomes = new JComboBox(uniqueNames.toArray());

            final JCheckBoxList cbl = new JCheckBoxList(uniqueNames.toArray());
            cbl.setRowSelectionAllowed(false);
            for (int i = 0; i < cbl.getRowCount(); i++) {
                cbl.mark(i, true);
            }
            cbl.repaint();

            Dimension d = new Dimension(150, 100);

            JScrollPane jsp = new JScrollPane(cbl);
            jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jsp.setPreferredSize(d);
            textPanel.add(jsp);

            GridLayout gbl = new GridLayout(2, 1);
            GridLayout rbtns = new GridLayout(2, 1);

            final JButton sort = new JButton("Plot");
            JButton sAll = new JButton("Mark All");
            JButton uAll = new JButton("Unmark All");
            JButton help = new JButton("Help");
            final JRadioButton incl = new JRadioButton("Inclusive");
            final JRadioButton excl = new JRadioButton("Exclusive");

            incl.setSelected(true);

            JPanel btns = new JPanel(gbl);
            JPanel rdio = new JPanel(rbtns);

            final ButtonGroup grp = new ButtonGroup();
            grp.add(incl);
            grp.add(excl);

            rdio.add(incl);
            rdio.add(excl);

            buttonsPanel.add(sort);
            buttonsPanel.add(help);
            btns.add(sAll);
            btns.add(uAll);

            textPanel.add(btns);
            textPanel.add(rdio);

            try {
                datasetAll = (DefaultCategoryDataset) dataset.clone(); //make a new copy of full dataset so we can return to it if we change graph
            } catch (CloneNotSupportedException shit) {
                shit.printStackTrace();
                //yea...no...
                return;
            }

            //log entry =
            //5678: 4 VACX_ BOB_ ALICE_ MARK_
            //all indexed on how the position they appear in the log

            final List<Integer> posi = pos;        //would contain 5678
            final List<Integer> countn = count;    //would contain 4
            final List<String> allnames = names;   //would contain "VACX_ BOB_ ALICE_ MARK_"

            /*#######################################################################
            * Chart Initial Visual Settings   >>----->>>>>
            * #######################################################################*/
            //Initialize x domain settings
            final CategoryPlot plot = (CategoryPlot) chart.getPlot();
            final CategoryAxis domainAxis = plot.getDomainAxis();
            final ValueAxis rangeAxis = plot.getRangeAxis();

            //Set the x domain label appearances
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
            domainAxis.setLabelFont(new Font("Arial Narrow", Font.PLAIN, 16));
            domainAxis.setTickLabelFont(new Font("Arial Narrow", Font.PLAIN, 8));

            //Set the y range label appearances
            rangeAxis.setLabelFont(new Font("Arial", Font.PLAIN, 15));
            rangeAxis.setTickLabelFont(new Font("Arial Narrow", Font.PLAIN, 12));

            //Determine domain intervals for printed intervals
            double[] domainInt = domainIntervals(setBinCount);
            int domainCheck = 0;

            //Hide labels not found in domainInt[]
            if (setBinStart > 10) {
                for (int i = 0; i < setBinCount; i++) {
                    if (i != domainInt[domainCheck]) {
                        domainAxis.setTickLabelPaint(categories[i], new Color(240, 240, 240, 1));
                    } else {
                        domainCheck++;
                    }
                }
            }

            //Set the y-axis so it only shows integer values
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            /*#######################################################################
            * Chart Initial Visual Settings   <<<<<-----<<
            * #######################################################################*/

            //Update the chart every time the user changes the offset value
            offsetSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    sort.doClick();
                }
            });

            incl.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (incl.isSelected()) {
                        return;
                    } else {
                        grp.clearSelection();
                        incl.setSelected(true);
                    }
                }
            });

            excl.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (excl.isSelected()) {
                        return;
                    } else {
                        grp.clearSelection();
                        excl.setSelected(true);
                    }
                }
            });

            help.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showHelp();
                }
            });

            sAll.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < cbl.getRowCount(); i++) {
                        cbl.mark(i, true);
                    }
                    cbl.repaint();
                }
            });

            uAll.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < cbl.getRowCount(); i++) {
                        cbl.mark(i, false);
                    }
                    cbl.repaint();
                }
            });

            //Read in button values and adjust chart ("Plot!" button)
            sort.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dataset.clear();

                    String[] choices = new String[cbl.getCheckedValues().length]; //Get user selection
                    for (int i = 0; i < cbl.getCheckedValues().length; i++) {
                        choices[i] = cbl.getCheckedValues()[i].toString();
                    }

                    //Get JSpinner values
                    int bin = Integer.parseInt(binCountSpinner.getValue().toString());
                    int offset = Integer.parseInt(offsetSpinner.getValue().toString());
                    int intervalSize = getBinSize(bin, 0);
                    int max = posi.get(posi.size() - 1);

                    //Re-adjust the buckets to better display the data
                    bin = binAdjust(max, bin, intervalSize);

                    //Cant have more bins than data points
                    //Throw an error message and force change the UI and internal values
                    if (bin > max) {
                        bin = max;
                        binCountSpinner.setValue(bin);
                        UITools.showInfoMessage("Too many bins!\n" +
                                "Bin Count automatically set to " + bin + " (max)\n" +
                                "\nNote: The maximum allowable number of bins is 4000", "Invalid Entry: Bin Count", null);
                    }

                    //Cant have an offset greater than the bin interval
                    //Do not throw an error message
                    if (offset >= intervalSize) {
                        offset = intervalSize - 1;
                        offsetSpinner.setValue(offset);
                    }

                    //Determine the domain intervals to be printed
                    double[] domainInt = domainIntervals(bin);
                    int domainCheck = 0;

                    //Calculate bin intervals and their counts
                    int intervalxLow = 0;
                    for (int i = 0; i < bin; i++) {
                        if (i == 0) {
                            intervalxLow = i * intervalSize + offset + 1;
                        } else {
                            intervalxLow = i * intervalSize + offset;
                        }

                        if (i != 0) {
                            intervalxLow += 1;
                        }
                        int interValXHigh = (i + 1) * intervalSize + offset;

                        if (i == bin - 1) {
                            interValXHigh = intervalxLow + intervalSize - 1;
                        }

                        int count = 0; //Count elements in each bin

                        //find how many entries in the interval:
                        for (int k = 0; k < posi.size(); k++) {
                            int currCheck = Integer.parseInt(posi.get(k).toString());

                            if (currCheck >= intervalxLow && currCheck <= interValXHigh) { //found entry in interval range
                                String check = allnames.get(k).toString();

                                //entry "check" passed to functions to see if it needs to be counted
                                if (incl.isSelected()) {
                                    count += graphIncl(choices, check);
                                }
                                if (excl.isSelected()) {
                                    count += graphExcl(choices, check, countn.get(k));
                                }
                            }
                            if (currCheck > interValXHigh) {
                                break;
                            }
                        }

                        //Update chart values
                        dataset.setValue(count, "Find Differences", String.valueOf("(" + intervalxLow + " : " + interValXHigh + ")"));

                        //Fix the x-axis to properly present the labels
                        if (bin > 10) {
                            if (i != (int) domainInt[domainCheck]) {
                                domainAxis.setTickLabelPaint(String.valueOf("(" + intervalxLow + " : " + interValXHigh + ")"), new Color(240, 240, 240, 1));
                            } else {
                                domainCheck++;
                            }
                        }
                    }
                }
            });
        }

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

                        for (int j = 0; j < dataset.getColumnCount(); j++) {
                            br.write(dataset.getColumnKey(j).toString());
                            if (j < dataset.getColumnCount() - 1) {
                                br.write(", ");
                            }
                        }
                        br.newLine();
                        for (int i = 0; i < dataset.getRowCount(); i++) {
                            for (int j = 0; j < dataset.getColumnCount(); j++) {
                                br.write(dataset.getValue(i, j).toString());
                                if (j < dataset.getColumnCount() - 1) {
                                    br.write(", ");
                                }
                            }
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
        textPanel.setBorder(ca.virology.lib.util.gui.GuiUtils.createWindowBorder("Control Panel", new GuiDefaults()));

        rootPanel.add(textPanel, BorderLayout.SOUTH);
        this.add(rootPanel);
        pack();
        setTitle(title);
        setLocationRelativeTo(null);
        repaint();
    }

    /*#######################################################################>>>>>
    * Purpose: Creates a new help window when the 'Help' button is pressed.
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    private void showHelp() {
        UITools.showInfoMessage("<html><h1><center><b>Find Differences Help</b></h1></center>" +
                "<p><b>Info</b>: This graph works off the log generate from \"Find Differences\". Simply,</br>" +
                "<br>select the tolerated genomes you wish to be plotted from the check box list.</br><p>" +
                "<p><b>Bin Count</b>: The number of sequence position groupings.</br>" +
                "<br><b>Offset</b>: Offset of the bin count range.</br>" +
                "<br><b>Perfect Match</b>: Refers to the entries in the log which contain 0 tolerated genomes.</br>" +
                "<br>Perfect matches are always plotted regardless of inclusion or exclusion.</br>" +
                "<br><b>Inclusive</b>: This implies the graph will be plotted from log entries which contain any tolerated genomes from your selection.</br>" +
                "<br><b>Exclusive</b>: This implies the graph will be plotted from log entries containing exactly your selection</p>" +
                "<p><br><b>Example</b>:If genomes 3, 4, 5 are selected. Then Exclusive = {(3,4,5),(3,4),(3,5),(4,5),(3),(4),(5)} and Inclusive = {(3,4,5)}.</br></p>" +
                "</html>", "Find Differences Help", this);
    }

    /*#######################################################################>>>>>
    * Purpose: Plots graph points given the users choice and line from the log.
    * > Searching for multiple:
         * - count singles
         * - count entries which are subgroups of selection
    * > Searching for singles:
         * - only count those singles
         * - selecting 0's
         * - count entry
    * Written - Spring 2015
    * #######################################################################>>>>>*/

    private int graphIncl(String[] choices, String entry) {
        for (int i = 0; i < choices.length; i++) {

            if (choices[i].equals("Perfect Match")) {
                if (entry.contains("[empty]")) {
                    return 1;
                }
            }
            if (entry.contains(choices[i])) {
                return 1;
            }
        }
        return 0;
    }

    /*#######################################################################>>>>>
    * Purpose: Plots graph points given the users choice and a line from the log.
    * Count only entries which match the selection exactly.
    * If 0 and something is selected, then nothing should be counted.
    * Written -  Spring 2015
    //#######################################################################>>>>>*/

    private int graphExcl(String[] choices, String entry1, int entrysize) {
        String entry = entry1.replace("_", "");
        entry = entry.replace(" ", "");
        for (int i = 0; i < choices.length; i++) {

            if (choices[i].equals("Perfect Match") && choices.length == 1) {
                if (entry.equals("[empty]")) {
                    return 1;
                }
                return 0;
            }

            if (!entry.contains(choices[i])) { //if choice is not in the entry, do not count
                return 0;
            } else {
                entry = entry.replace(choices[i], ""); //remove choice from entry
            }

        }
        if (entry.length() == 0) {
            return 1;
        }
        return 0;
    }

    /*#######################################################################>>>>>
    * Purpose: Determine which labels will be printed on the x-axis (only 10 labels max).
    * Written - Summer 2015
    * #######################################################################>>>>>*/

    protected double[] domainIntervals(int bin) {
        //Determine which bins get labelled (only 10 labels max)
        double domainStep = (double) bin / 9.0;
        double[] domainInt = new double[10];

        //Assign bins that will be labelled
        for (int i = 0; i < 10; i++) {
            if (i == 0) {
                domainInt[i] = 0;
            } else if (i == 10 - 1) {
                domainInt[i] = bin - 1;
            } else {
                domainInt[i] = domainInt[i - 1] + domainStep;
            }
        }

        //Round the double values to the closest integer
        for (int i = 0; i < 10; i++) {
            domainInt[i] = Math.round(domainInt[i]);
        }

        return domainInt;
    }

    /*#######################################################################>>>>>
    * Purpose: Adjust the bins to display data more efficiently.
    * Written - Summer 2015
    * #######################################################################>>>>>*/

    protected int binAdjust(int max, int binCount, int binSize) {
        //Change the max value so it is divisible by the bin count
        if ((max % binCount) != 0) {
            max += binCount - (max % binCount);
        }

        //Removes additional dataless buckets
        if (max != binCount) {
            //Do not add extra bucket if max is divisible by bin
            if (dataLength % binSize != 0) {
                binCount = dataLength / binSize + 1;
            } else {
                binCount = dataLength / binSize;
            }
        }
        return binCount;
    }

    /*#######################################################################>>>>>
    * Purpose: Returns the size of each bin given the number of total bins.
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    protected int getBinSize(final int binCount, final int offset) {
        return (int) (Math.ceil(((double) (dataLength)) / ((double) (binCount))));
    }

    /*#######################################################################>>>>>
    * Purpose: Initializes the plot differences chart. It is only called once when within the
    * 'Find Differences # - Stopped/Running' window when the 'Plot Differences' button is pressed.
    * Any changes made to the chart is not handled by this method.
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    protected int updateDataset(int binCount, int offset) {
        dataset.clear();

        int binSize = getBinSize(binCount, offset);
        int max = dataLength;

        //Re-adjust the buckets to better display the data
        binCount = binAdjust(max, binCount, binSize);

        //Determine bin sizes. The bin are inclusive i.e [x,y]
        for (int bin = 0; bin < binCount; bin++) {
            categories[bin] = "(" + (Math.max(0, offset + (bin) * binSize) + 1) + " : " + Math.min(offset + (bin + 1) * binSize, binCount * binSize) + ")";
        }

        //Determine the number of matches in each bin
        for (int series = 0; series < data.length; series++) {
            int i = 0;

            //Go through each bin
            for (int bin = 0; bin <= binCount; bin++) {
                //Count the number of differences in each bin
                int count = 0;
                while (i < data[series].length && data[series][i] <= bin * binSize + offset) {
                    count++;
                    i++;
                }

                if (bin > 0) { //Add bin count values to chart
                    dataset.addValue((Number) count, this.seriesNames[series], categories[bin - 1]);
                }
            }
        }
        return binCount;
    }

    protected void setSeriesColor(int series, Color color) {
        CategoryItemRenderer renderer = chart.getCategoryPlot().getRenderer();
        renderer.setSeriesPaint(series, color);
    }

    protected JFreeChart getChart() {
        return chart;
    }

    protected DefaultCategoryDataset getDataset() {
        return dataset;
    }

    /*#######################################################################>>>>>
    * Purpose: Sets chart type and visuals (Not for initialization).
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    protected void setChartType(ChartType chartType) {
        CategoryItemRenderer renderer = new DefaultCategoryItemRenderer();

        switch (chartType) {
            case STACKED_AREA:
                chart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
                chart.getCategoryPlot().setRangeGridlinePaint(Color.BLACK);
                chart.getCategoryPlot().getDomainAxis().setCategoryMargin(0.2);
                renderer = new StackedAreaRenderer();

                break;
            case STACKED_BAR:
                chart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
                chart.getCategoryPlot().setRangeGridlinePaint(Color.BLACK);
                chart.getCategoryPlot().getDomainAxis().setCategoryMargin(0.2);
                renderer = new StackedBarRenderer();
                ((BarRenderer) renderer).setItemMargin(.2);
                break;
            case LINE:
                chart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
                chart.getCategoryPlot().setRangeGridlinePaint(Color.BLACK);
                chart.getCategoryPlot().getDomainAxis().setCategoryMargin(0.2);

                //Add plot points
                renderer = new LineAndShapeRenderer(true, true);
                renderer.setSeriesShape(0, ShapeUtilities.createDiamond(2));

                for (int i = 0; i < dataset.getRowCount(); i++) {
                    renderer.setSeriesStroke(i, new BasicStroke(1f));
                }
                break;
        }
        CategoryToolTipGenerator tooltip = new StandardCategoryToolTipGenerator();
        for (int i = 0; i < data.length; i++) {
            renderer.setSeriesPaint(i, this.seriesPaints[i]);
            renderer.setSeriesToolTipGenerator(i, tooltip);
        }

        chart.getCategoryPlot().setRenderer(renderer);
    }

    private static enum ChartType {
        STACKED_AREA {
            public String toString() {
                return "Area Chart";
            }
        },
        STACKED_BAR {
            public String toString() {
                return "Bar Chart";
            }
        },
        LINE {
            public String toString() {
                return "Line Chart";
            }
        }
    }

    /*#######################################################################>>>>>
    * Purpose: Initializes chart types and visuals (called once).
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    private static JFreeChart createChart(ChartType chartType, String title, String hAxisName, String vAxisName, CategoryDataset dataset, PlotOrientation orientation, boolean legend, boolean tooltips, boolean urls) {
        JFreeChart chart;
        switch (chartType) {
            case STACKED_AREA:
                chart = ChartFactory.createStackedAreaChart(title, hAxisName, vAxisName, dataset, orientation, legend, tooltips, urls);
                chart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
                chart.getCategoryPlot().setRangeGridlinePaint(Color.BLACK);
                return chart;
            case STACKED_BAR:
                chart = ChartFactory.createStackedBarChart(title, hAxisName, vAxisName, dataset, orientation, legend, tooltips, urls);
                chart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
                chart.getCategoryPlot().setRangeGridlinePaint(Color.BLACK);
                return chart;
            case LINE:
                chart = ChartFactory.createLineChart(title, hAxisName, vAxisName, dataset, orientation, legend, tooltips, urls);
                chart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
                chart.getCategoryPlot().setRangeGridlinePaint(Color.BLACK);
                LineAndShapeRenderer renderer = ((LineAndShapeRenderer) (chart.getCategoryPlot().getRenderer()));

                for (int i = 0; i < dataset.getRowCount(); i++) {
                    renderer.setSeriesStroke(i, new BasicStroke(1f));
                }
                return chart;
        }
        return null;
    }

    /*#######################################################################>>>>>
    * Purpose: Create a proper log list and create a chart to display the info.
    * This is the primary method for this file.
    * Edited - Summer 2015
    * #######################################################################>>>>>*/

    public static DifferencesChart createFindDifferencesChart(String title, java.util.List<Integer> values, String log) {
        int[][] data = new int[1][values.size()];

        //Make the log into lists
        String cut = log.split("Differences...\n" + "----------------------------------------------------------------------------------------------------\n")[1];

        List<Integer> pos = new ArrayList<Integer>();               //example:      6479: 1 (VARV-GBR_harv)
        List<String> names = new ArrayList<String>();               //              pos: count ( name )
        List<Integer> count = new ArrayList<Integer>();
        Scanner scanner = new Scanner(cut); //cut is long string, better be scanned rather than tokenized
        int el = 0;

        while (scanner.hasNextLine()) { //tokenize log into 3 above lists
            String line = scanner.nextLine();
            String parts[] = line.split(" ");
            if (line.contains("--")) {
                break;
            }
            pos.add(el, Integer.parseInt(parts[0].replace(":", "")));

            if (parts.length == 2) {
                names.add(el, "[empty]");
                count.add(el, 0);
                el += 1;
                continue;
            }
            count.add(el, Integer.parseInt(parts[1]));

            String last = line.substring(line.indexOf("("), line.indexOf(")"));
            names.add(el, last.replace("(", ""));

            el += 1;
        }
        scanner.close();

        try {
            int maxValue = values.get(0);
            for (int i = 0; i < values.size(); i++) {
                data[0][i] = values.get(i);
                if (values.get(i) > maxValue) {
                    maxValue = values.get(i);
                }
            }

            DifferencesChart differencesChart = new DifferencesChart(pos, names, count, data, maxValue, new String[]{"Find Differences"}, new Paint[]{new Color(240, 0, 0)}, ChartType.STACKED_BAR, title, "Sequence Position", "Differences", false, true, false);
            return differencesChart;
        } catch (IndexOutOfBoundsException e) {
            DifferencesChart differencesChart = new DifferencesChart(pos, names, count, data, 100, new String[]{"Find Differences"}, new Paint[]{new Color(240, 0, 0)}, ChartType.STACKED_BAR, title, "Sequence Position", "Differences", false, true, false);
            return differencesChart;
        }
    }
}

//This code is not used for anything. It doesn't work properly either.

    /*
    public static DifferencesGraph createNucleotideDifferencesChart(String title, FeaturedSequence seq)
    {
        int[][] data = new int[5][0];
        String[] series = new String[5];

        FeatureFilter ff =
                new FeatureFilter.ByType(FeatureType.DIFFERENCE_LIST);

        if(seq.filter(ff, false).countFeatures() > 0)
        {
            Iterator it = seq.filter(ff, false).features();
            Feature f = (Feature) it.next(); //should be only one, but get the first only if more
            int[]    diffs = (int[]) f.getAnnotation().getProperty(AnnotationKeys.DIFF_ARRAY);
            int subs = 0;
            int a = 0;
            int g = 0;
            int c = 0;
            int t = 0;

            for(int i = 0; i < diffs.length; i++) {
                switch (diffs[i]) {
                    case DifferenceType.I_SUB_NT_CA:
                    case DifferenceType.I_SUB_NT_GA:
                    case DifferenceType.I_SUB_NT_TA:
                        a++;
                        break;
                    case DifferenceType.I_SUB_NT_AC:
                    case DifferenceType.I_SUB_NT_GC:
                    case DifferenceType.I_SUB_NT_TC:
                        c++;
                        break;
                    case DifferenceType.I_SUB_NT_AG:
                    case DifferenceType.I_SUB_NT_CG:
                    case DifferenceType.I_SUB_NT_TG:
                        g++;
                        break;
                    case DifferenceType.I_SUB_NT_AT:
                    case DifferenceType.I_SUB_NT_CT:
                    case DifferenceType.I_SUB_NT_GT:
                        t++;
                        break;
                    default:
                        if (DifferenceType.isSubstitution(diffs[i])) {
                            subs++;
                        }
                        break;

                }
                if (DifferenceType.isSubstitution(diffs[i])) {
                    //subs++;
                }
            }
            data[4] = new int[subs];
            data[0] = new int[a];
            data[1] = new int[c];
            data[2] = new int[g];
            data[3] = new int[t];

            for (int i = diffs.length-1; i >= 0; i--) {
                switch (diffs[i]) {
                    case DifferenceType.I_SUB_NT_CA:
                    case DifferenceType.I_SUB_NT_GA:
                    case DifferenceType.I_SUB_NT_TA:
                        a--;
                        data[0][a] = i;
                        break;
                    case DifferenceType.I_SUB_NT_AC:
                    case DifferenceType.I_SUB_NT_GC:
                    case DifferenceType.I_SUB_NT_TC:
                        c--;
                        data[1][c] = i;
                        break;
                    case DifferenceType.I_SUB_NT_AG:
                    case DifferenceType.I_SUB_NT_CG:
                    case DifferenceType.I_SUB_NT_TG:
                        g--;
                        data[2][g] = i;
                        break;
                    case DifferenceType.I_SUB_NT_AT:
                    case DifferenceType.I_SUB_NT_CT:
                    case DifferenceType.I_SUB_NT_GT:
                        t--;
                        data[3][t] = i;
                        break;
                    default:
                        //Only substitutions not already recorded
                        if (DifferenceType.isSubstitution(diffs[i])) {
                            subs--;
                            data[4][subs] = i;
                        }
                        break;

                }
                if (DifferenceType.isSubstitution(diffs[i])) {
                    //subs--;
                    //data[0][subs] = i;
                }
            }

        }
        series[4] = "Other Differences";
        series[0] = "A";
        series[1] = "C";
        series[2] = "G";
        series[3] = "T";

        Paint[] paints = new Paint[5];
        paints[4] = Color.BLUE;
        paints[0] = new Color(255,255,0);
        paints[1] = new Color(0,255,0);
        paints[2] = new Color(0,255,255);
        paints[3] = new Color(255,200,0);

        DifferencesGraph differencesChart = new DifferencesGraph(title, data);

        return differencesChart;
    }
    */