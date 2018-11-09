package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.io.sequenceData.SimpleEditableSequence;
import ca.virology.lib.util.gui.UITools;

/*#######################################################################>>>>>
* Purpose: This class draws pair-wise similarity graphs based on PAM-250 matrix.
* Written: Winter 2009
* Edited: Summer 2015
* #######################################################################>>>>>*/
public class PairwiseSimilarityGraph extends AbstractPairwiseGraph {
    //True = Similarity Graph, False = Differences Graph
    boolean graphType;
    //int[][] similarityMatrix;
    double gapPenalty;

    /*#######################################################################>>>>>
    * Purpose: Constructor. Create a PairwiseSimilarityGraph object.
    * Param: seqs -> the sequences to be plotted.
    * Written: Winter 2009
    * Edited: Summer 2015
    * #######################################################################>>>>>*/
    public PairwiseSimilarityGraph(FeaturedSequence[] seqs, Boolean type) {
        super(seqs);

        graphType = type;
        if (graphType) {
            theFrame.setTitle("Sequence Similarity Graph");
        } else {
            theFrame.setTitle("Sequence Differences Graph");
        }
        //similarityMatrix = initPamMatrix();
        gapPenalty = -10;
    }

    @Override
    protected void drawPlot() {
        double[] temp;
        double windowSum;
        int offset, start;
        for (int i = 0; i < getQueriesIndex().length; i++) {
            if (getSeqs()[getRefSeqIndex()].getSequenceType() == FeaturedSequence.DNA_SEQUENCE && getSeqs()[getQueriesIndex()[i]].getSequenceType() == FeaturedSequence.DNA_SEQUENCE) {
                temp = calculateIntermediateDNAGraph(getSeqs()[getRefSeqIndex()], getSeqs()[getQueriesIndex()[i]]);
            } else if (getSeqs()[getRefSeqIndex()].getSequenceType() == FeaturedSequence.AA_SEQUENCE && getSeqs()[getQueriesIndex()[i]].getSequenceType() == FeaturedSequence.AA_SEQUENCE) {
                temp = calculateIntermediateDNAGraph(getSeqs()[getRefSeqIndex()], getSeqs()[getQueriesIndex()[i]]);
            } else {
                String f1 = getSeqs()[getRefSeqIndex()].getName();
                String f2 = getSeqs()[getQueriesIndex()[i]].getName();

                UITools.showWarning(
                        "<html>Cannot compare a DNA to an AA Sequence<br>" +
                                "Please check your data <br> " + f1 + " <br> " + f2 +
                                "<br> could not be compared.</html>",
                        getRootPane());
                break;
            }

            for (start = 0; start + getWindowSize() <= temp.length; start += getStepSize()) {
                windowSum = 0.0;
                for (offset = 0; offset < getWindowSize(); offset++)
                    windowSum += temp[start + offset];
                double x = start + ((double) (getWindowSize() - 1) / 2.0);
                double y = windowSum / getWindowSize();

                //Take care of odd/even window size. even -> x = x+1, odd x = x+0.5
                if (getWindowSize() % 2 != 0)
                    theGraph.addPoint(i, x + 1, y, true);
                else
                    theGraph.addPoint(i, x + 1.5, y, true);
            }
            theGraph.addLegend(i, getSeqs()[getQueriesIndex()[i]].getName());
        }
        repaint();
    }

    @Override
    protected void initGraph() {
        theGraph.setColor(true);
        theGraph.setGrid(true);
        if (graphType) {
            theGraph.setYLabel("Similarity Score (%)");
        } else {
            theGraph.setYLabel("Difference Score (%)");
        }
        theGraph.setXLabel("Sequence Position");
    }

    /*#######################################################################>>>>>
    * Purpose: Computes a binary match for nucleic acid score. It is a helper function for drawPlot().
    * Return: the intermediate array w/o step size and window size being applied.
    * Written: Winter 2009
    * Edited: Summer 2015
    * #######################################################################>>>>>*/
    protected double[] calculateIntermediateDNAGraph(FeaturedSequence s1, FeaturedSequence s2) {
        int length = getMax(s1.length(), s2.length());
        double[] temp = new double[length];

        if (graphType) {
            for (int i = 0; i < getMin(s1.length(), s2.length()); i++) {
                if (s1.charAt(i) == s2.charAt(i)) {
                    temp[i] = 1;
                }
            }
        } else {
            for (int i = 0; i < getMin(s1.length(), s2.length()); i++) {
                if (s1.charAt(i) != s2.charAt(i)) {
                    temp[i] = 1;
                }
            }
        }

        return temp;
    }

    /*#######################################################################>>>>>
    * Purpose: This function is not currently being used. The percentage version in calculateIntermediateDNAGraph() is used instead.
    * Computes an intermediate sequence based on s1 and s2. The result is a double array which contains similarity scores for each position
    * derived from the PAM-250 matrix. It's a helper function for drawPlot().
    * Return: The intermediate array without step size and window size being applied to it.
    * Written: Winter 2009
    * Edited: Summer 2015
    * #######################################################################>>>>>*/

	/*protected double[] calculateIntermediateGraph(FeaturedSequence s1, FeaturedSequence s2)
    {
		//compute the length of the array. we will use the length of the longer one for later calculation.
		int length = getMax(s1.length(), s2.length());
		
		//create a double array to hold y coordinates. x coordinates will just be 0 ~ (length-1)
		double[] temp = new double[length];
		int index1, index2;
		
		//now calculate y
		for(int i = 0; i < getMin(s1.length(), s2.length()); i++)
		{
			switch(s1.charAt(i))
			{
				case 'C':
					index1 = 0; break;
				case 'S':
					index1 = 1; break;
				case 'T':
					index1 = 2; break;
				case 'P':
					index1 = 3; break;
				case 'A':
					index1 = 4; break;
				case 'G':
					index1 = 5; break;
				case 'N':
					index1 = 6; break;
				case 'D':
					index1 = 7; break;
				case 'E':
					index1 = 8; break;
				case 'Q':
					index1 = 9; break;
				case 'H':
					index1 = 10; break;
				case 'R':
					index1 = 11; break;
				case 'K':
					index1 = 12; break;
				case 'M':
					index1 = 13; break;
				case 'I':
					index1 = 14; break;
				case 'L':
					index1 = 15; break;
				case 'V':
					index1 = 16; break;
				case 'F':
					index1 = 17; break;
				case 'Y':
					index1 = 18; break;
				case 'W':
					index1 = 19; break;
				default:
					index1 = 20; break;
			}
			
			switch(s2.charAt(i))
			{
				case 'C':
					index2 = 0; break;
				case 'S':
					index2 = 1; break;
				case 'T':
					index2 = 2; break;
				case 'P':
					index2 = 3; break;
				case 'A':
					index2 = 4; break;
				case 'G':
					index2 = 5; break;
				case 'N':
					index2 = 6; break;
				case 'D':
					index2 = 7; break;
				case 'E':
					index2 = 8; break;
				case 'Q':
					index2 = 9; break;
				case 'H':
					index2 = 10; break;
				case 'R':
					index2 = 11; break;
				case 'K':
					index2 = 12; break;
				case 'M':
					index2 = 13; break;
				case 'I':
					index2 = 14; break;
				case 'L':
					index2 = 15; break;
				case 'V':
					index2 = 16; break;
				case 'F':
					index2 = 17; break;
				case 'Y':
					index2 = 18; break;
				case 'W':
					index2 = 19; break;
				default:
					index2 = 20; break;
			}

		//	if(index1 == -1 || index2 == -1)
		//		temp[i] = gapPenalty;
		//	else
		//	{
				try
				{
					temp[i] = similarityMatrix[index1][index2];
				}
				catch(IndexOutOfBoundsException e)	
				{
					temp[i] = similarityMatrix[index2][index1];
				}
		//	}
		}
		return temp;
	}*/

    /*#######################################################################>>>>>
    * Purpose: Generate a PAM-250 matrix as a 2D int array.
    * Return: PAM-250 matrix.
    * Written: Winter 2009
    * Edited: Summer 2015
    * #######################################################################>>>>>*/

 	/*private int[][] initPamMatrix()
	{
		int[][] PAM_250 = 
		{
				//C,  S,  T,  P,  A,  G,  N,  D,  E,  Q,  H,  R,  K,  M,  I,  L,  V,  F,  Y,  W, gap
				{12  																			 },
				{ 0,  2                                                                        	 },
				{-2,  1,  3																	   	 },
				{-3,  1,  0,  6																	 },
				{-2,  1,  1,  1,  2																 },
				{-3,  1,  0, -1,  1,  5															 },
				{-4,  1,  0, -1,  0,  0,  2														 },
				{-5,  0,  0, -1,  0,  1,  2,  4													 },
				{-5,  0,  0, -1,  0,  0,  1,  3,  4												 },
				{-5, -1, -1,  0,  0, -1,  1,  2,  2,  4											 }, 
				{-3, -1, -1,  0, -1, -2,  2,  1,  1,  3,  6										 },
				{-4,  0, -1,  0, -2, -3,  0, -1, -1,  1,  2,  6									 },
				{-5,  0,  0, -1, -1, -2,  1,  0,  0,  1,  0,  3,  5								 },
				{-5, -2, -1, -2, -1, -3, -2, -3, -2, -1, -2,  0,  0,  6							 },
				{-2, -1,  0, -2, -1, -3, -2, -2, -2, -2, -2, -2, -2,  2,  5						 },
				{-6, -3, -2, -3, -2, -4, -3, -4, -3, -2, -2, -3, -3,  4,  2,  6					 },
				{-2, -1,  0, -1,  0, -1, -2, -2, -2, -2, -2, -2, -2,  2,  4,  2,  4				 },
				{-4, -3, -3, -5, -5, -5, -4, -6, -5, -5, -2, -4, -5,  0,  1,  2, -1,  9			 },
				{ 0, -3, -3, -5, -3, -5, -2, -4, -4, -4,  0, -4, -4, -2, -1, -1, -2,  7, 10		 },
				{-8, -2, -5, -6, -6, -7, -4, -7, -7, -5, -3,  2, -3, -4, -5, -2, -6,  0,  0, 17	 },
				{-5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, 2}
		};
		return PAM_250;
	} */
}
