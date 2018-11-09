package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.FeaturedSequence;

/**
 * this class draws pair-wise identity graphs. identity is 0 when we have a mismatch and 1 when we have a match.
 *
 * @author Songhan L (Sept 2009)
 * @version 1.0
 */

public class PairwiseIdentityGraph extends AbstractPairwiseGraph {
    private static final long serialVersionUID = 1L;    //this can be removed

    /**
     * constructor. create a PairwiseIdentityGraph object
     *
     * @param seqs the sequences to be plotted
     */
    public PairwiseIdentityGraph(FeaturedSequence[] seqs) {
        super(seqs);
        theFrame.setTitle("Pairwise Identity");
    }

    @Override
    protected void initGraph() {
        theGraph.setColor(true);
        theGraph.setGrid(true);
        theGraph.setYLabel("Identity");
        theGraph.setXLabel("Sequence Position");
    }

    @Override
    protected void drawPlot() {
        double[] temp;
        double windowSum;
        int offset, start;
        for (int i = 0; i < getQueriesIndex().length; i++) {
            temp = calculateIntermediateGraph(getSeqs()[getRefSeqIndex()], getSeqs()[getQueriesIndex()[i]]);
            for (start = 0; start + getWindowSize() <= temp.length; start += getStepSize()) {
                windowSum = 0.0;
                for (offset = 0; offset < getWindowSize(); offset++)
                    windowSum += temp[start + offset];
                double x = start + ((double) (getWindowSize() - 1) / 2.0);
                double y = windowSum / getWindowSize();

                //take care of odd/even window size. even -> x = x+1, odd x = x+0.5
                if (getWindowSize() % 2 != 0)
                    theGraph.addPoint(i, x + 1, y, true);
                else
                    theGraph.addPoint(i, x + 1.5, y, true);
            }
            theGraph.addLegend(i, getSeqs()[getQueriesIndex()[i]].getName());
        }
        repaint();
    }

    /**
     * computes an intermediate sequence based on s1 and s2 and return the result as a double array.
     * when the two sequences have a match, store 1 to the resulting array, otherwise store 0. gap is considered
     * a mismatch. it's a helper function for drawPlot().
     *
     * @return the intermediate array without step size and window size being applied to it.
     */
    protected double[] calculateIntermediateGraph(FeaturedSequence s1, FeaturedSequence s2) {
        //compute the length of the array. we will use the longer one.
        int length = getMax(s1.length(), s2.length());

        //create a double array to hold y coordinates. x coordinates will just be 0 ~ (length-1)
        double[] temp = new double[length];

        //now calculate y
        for (int i = 0; i < getMin(s1.length(), s2.length()); i++) {
            if (s1.charAt(i) == s2.charAt(i))
                temp[i] = 1.0;
            else
                temp[i] = 0.0;
        }
        return temp;
    }
}