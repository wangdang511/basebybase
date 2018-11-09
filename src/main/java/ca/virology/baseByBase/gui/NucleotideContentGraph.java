package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.io.sequenceData.SimpleEditableSequence;
import ca.virology.lib.util.gui.UITools;

public class NucleotideContentGraph extends AbstractBaseContentGraph {

    char[] m_search;

    public NucleotideContentGraph(FeaturedSequence[] seqs, char[] search) {
        super(seqs, search);
        m_search = getSearchParameters();
        theFrame.setTitle("Nucleotide Content Graph");
    }

    @Override
    public void drawPlot() {
        double windowSum;
        int start;

        String yLabel = "";
        for (int i = 0; i < m_search.length; i++) {
            if (m_search[i] != '0') {
                yLabel = yLabel + m_search[i];
            }
        }
        yLabel = yLabel + " Composition (%)";
        theGraph.setYLabel(yLabel);

        for (int i = 0; i < getQueriesIndex().length; i++) {
            FeaturedSequence seq = getSeqs()[getQueriesIndex()[i]];
            for (start = 0; start + getWindowSize() <= seq.length(); start += getStepSize()) {
                windowSum = calculateIntermediateNucleotideGraph(seq, start);
                double x = start + ((double) (getWindowSize() - 1) / 2.0);
                double y = windowSum / getWindowSize();

                //Take care of odd/even window size. even -> x = x+1, odd x = x+0.5
                if (getWindowSize() % 2 != 0) {
                    theGraph.addPoint(i, x + 1, y, true);
                } else {
                    theGraph.addPoint(i, x + 1.5, y, true);
                }
            }
            theGraph.addLegend(i, getSeqs()[getQueriesIndex()[i]].getName());
        }
        repaint();
    }

    @Override
    public void initGraph() {
        theGraph.setColor(true);
        theGraph.setGrid(true);

        theGraph.setXLabel("Sequence Position");
    }

    protected double calculateIntermediateNucleotideGraph(FeaturedSequence seq, int start) {
        int count = 0;

        for (int i = start; i < start + getWindowSize(); i++) {
            for (int j = 0; j < m_search.length; j++) {
                if ((m_search[j] == 'a') || (m_search[j] == 'A')) {
                    if ((seq.charAt(i) == 'a') || (seq.charAt(i) == 'A')) {
                        count++;
                    }
                } else if ((m_search[j] == 'c') || (m_search[j] == 'C')) {
                    if ((seq.charAt(i) == 'c') || (seq.charAt(i) == 'C')) {
                        count++;
                    }
                } else if ((m_search[j] == 'g') || (m_search[j] == 'G')) {
                    if ((seq.charAt(i) == 'g') || (seq.charAt(i) == 'G')) {
                        count++;
                    }
                } else if ((m_search[j] == 't') || (m_search[j] == 'T')) {
                    if ((seq.charAt(i) == 't') || (seq.charAt(i) == 'T')) {
                        count++;
                    }
                }
            }
        }

        return (double) count;
    }
}