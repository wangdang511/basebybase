package ca.virology.baseByBase.data;

import ca.virology.lib.io.sequenceData.FeaturedSequence;
import ca.virology.lib.io.sequenceData.EditableSequence;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import java.util.Arrays;


/**
 * Implementation of SummarySequenceModel that gives indications based on
 * differences between each sequence and the consensus sequence for the
 * alignment
 */
public class SimilaritySummaryModel
        extends AbstractSequenceSummaryModel {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected int[][] m_sims;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new SimilaritySummaryModel object.
     *
     * @param seqs the sequences for the model
     */
    public SimilaritySummaryModel(FeaturedSequence[] seqs) {
        m_seqs = Arrays.asList(seqs);
        refresh();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Create an image that can be displayed as a legend
     * in reports
     *
     * @return the legend image
     */
    public Image createLegendImage() {
        int w = 520;
        int h = 50;

        int ly = 15;
        int by = 17;
        int bh = h - by - 1;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setFont(new Font("", Font.PLAIN, 10));

        FontMetrics fm = g.getFontMetrics();

        g.setPaint(Color.white);
        g.fillRect(0, 0, w, h);

        int bwidth = fm.stringWidth("Gapped Region");
        g.setPaint(Color.black);
        g.drawLine(10, by + bh / 2, 10 + bwidth, by + bh / 2);
        g.drawLine(10, by, 10, by + bh);
        g.drawLine(10 + bwidth, by, 10 + bwidth, by + bh);
        g.drawString("Gapped Region", 10, ly);

        int x = 20 + bwidth;

        bwidth = fm.stringWidth("Not Conserved -> Similar/Conserved");
        for (int i = 0; i < bwidth; ++i) {
            double d = i / (double) bwidth;
            int val = (int) ((d) * 255.0);
            g.setPaint(new Color(255 - val, 255 - val, 255 - val));
            g.drawLine(x + i, by, x + i, by + bh);
        }
        g.setPaint(Color.black);
        g.drawRect(x, by, bwidth, bh);
        g.drawString("Not Conserved -> Similar/Conserved", x, ly);

        g.dispose();

        return img;
    }

    /**
     * Get the sequence type for this model
     *
     * @return one of <CODE>EditableSequence.AA_SEQUENCE</CODE>,
     * <CODE>EditableSequence.DNA_SEQUENCE</CODE>, or
     * <CODE>EditableSequence.GENERIC_SEQUENCE</CODE>
     */
    public int getSequenceType() {
        return EditableSequence.GENERIC_SEQUENCE;
    }

    /**
     * add a sequence to the alignment
     *
     * @param seq The sequence to add
     */
    public void addSequence(FeaturedSequence seq) {
        super.addSequence(seq);

        refresh();
    }

    /**
     * remove a sequence from the alignment
     *
     * @param seq the sequence to remove
     */
    public void removeSequence(FeaturedSequence seq) {
        super.removeSequence(seq);

        refresh();
    }

    /**
     * Get the indicator for a particular range of sequence data.  This can be
     * arrived at by any number of means.
     *
     * @param index The index of the sequence to check
     * @param start The start pos of the sequence (gapped position) of the
     *              range
     * @param stop  The stop position of the sequence (gapped position) of the
     *              range
     * @return a <CODE>SummaryIndicator</CODE> object
     */
    public SummaryIndicator getIndicator(
            int index,
            int start,
            int stop) {
        if ((index >= m_seqs.size()) || (index < 0)) {
            return null;
        }

        int type = SummaryIndicator.IND_TICK;
        Color col = null;

        int gaps = 0;
        int cnt = 0;
        int total = stop - start;

        for (int i = start; i < stop; ++i) {
            if (i >= m_sims[index].length) {
                break;
            }

            if (m_sims[index][i] == -1) {
                ++gaps;
            } else {
                cnt += m_sims[index][i];
            }
        }

        if (gaps > (total * 2 / 3)) {
            col = Color.black;
            type = SummaryIndicator.IND_GAP;
        } else {
            int avg = (int) ((double) cnt / (double) total);
            col = new Color(255 - avg, 255 - avg, 255 - avg);
        }

        return new SummaryIndicator(type, col);
    }

    /**
     * Get the indicator for a location on a sequence in the alignment
     *
     * @param index    The index of the sequence in the alignment
     * @param position The index along the sequence (gapped) of the alignment
     * @return the indicator for that position
     */
    public SummaryIndicator getIndicator(
            int index,
            int position) {
        if ((index >= m_seqs.size()) || (index < 0) ||
                (position > m_sims[index].length)) {
            return null;
        }

        int type = SummaryIndicator.IND_TICK;
        Color col = Color.white;

        if (m_sims[index][position] == -1) {
            col = Color.black;
            type = SummaryIndicator.IND_GAP;
        } else {
            col = new Color(255 - m_sims[index][position],
                    255 - m_sims[index][position], 255 -
                    m_sims[index][position]);
        }

        return new SummaryIndicator(type, col);
    }

    /**
     * Re-retrieve and recalculate data based on an external event
     */
    public void refresh() {
        refreshDifferences();
    }

    /**
     * refresh the difference lists
     */
    protected void refreshDifferences() {
        int len = getLength();
        int seqs = countSequences();

        m_sims = new int[seqs][len];

        for (int i = 0; i < len; ++i) {
            int[] cc = getConsCount(i);

            for (int j = 0; j < countSequences(); ++j) {
                FeaturedSequence seq = (FeaturedSequence) m_seqs.get(j);

                if (seq.charAt(i) == '-') {
                    m_sims[j][i] = -1;
                } else if (seq.charAt(i) == cc[0]) {
                    m_sims[j][i] =
                            (int) ((double) cc[1] / (double) seqs * 255.0);

//                    System.out.println("match: ["+j+","+i+"]: "+m_sims[j][i]);
                } else {
                    m_sims[j][i] = 0;
                }
            }
        }
    }

    /**
     * get the consensus info for a position
     *
     * @param index the position
     * @return an array of ints/chars as follows
     * val[0] = the consensus character
     * val[1] = the count of that character
     * val[2] = the total number of actual sequence chars at that pos
     */
    protected int[] getConsCount(int index) {
        int[] vals = new int[26];
        int gaps = 0;
        int total = 0;

        for (int i = 0; i < m_seqs.size(); ++i) {
            FeaturedSequence seq = (FeaturedSequence) m_seqs.get(i);

            if (index >= seq.length()) {
                continue;
            }

            char c = Character.toUpperCase(seq.charAt(index));

            if (c == '-') {
                gaps++;

                continue;
            }

            vals[(int) (c - 'A')]++;
            ++total;
        }

        int max = 0;
        int maxind = 0;

        for (int i = 0; i < 26; ++i) {
            if (vals[i] > max) {
                max = vals[i];
                maxind = i;
            }
        }

        int[] ret = new int[4];

        // Following few lines have been commented out
        // because it is requested that all the segments must be
        // show no matter what is the similarity rate. cheers!!
/*
        if (gaps > max && gaps > (total*2/3)) {
            ret[0] = (int) '-';
            ret[1] = gaps;
        } else*/
        {
            ret[0] = (int) ('A' + maxind);
            ret[1] = max;
        }

        ret[2] = total;

        ret[3] = max;

        return ret;
    }
}
