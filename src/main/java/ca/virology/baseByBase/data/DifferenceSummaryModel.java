package ca.virology.baseByBase.data;

import ca.virology.baseByBase.gui.EditPanel;
import ca.virology.baseByBase.gui.PrimaryPanel;
import ca.virology.lib.io.sequenceData.*;
import ca.virology.lib.io.tools.FeatureTools;
import ca.virology.lib.io.tools.SequenceTools;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.springframework.context.support.StaticApplicationContext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.util.*;


/**
 * Implementation of SummarySequenceModel that gives indications based on
 * differences between each sequence and the consensus sequence for the
 * alignment
 */
public class DifferenceSummaryModel
    extends AbstractSequenceSummaryModel
{
    //~ Instance fields ////////////////////////////////////////////////////////

    protected List m_diffList = new ArrayList();
    protected int m_diffColor = ColorScheme.DIFF_CLASSIC_SCHEME;
    protected Consensus m_cons;
    boolean m_seqListChanged = false;
    FeaturedSequence[] seqs;

    public static final int PAIRWISE_COMPARISON = 0x000A;
    public static final int CONSENSUS_COMPARISON = 0x000B;
    public static final int MODEL_COMPARISON = 0x000C;
    protected int m_compType = PAIRWISE_COMPARISON;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new DifferenceSummaryModel object.
     *
     * @param seqs the sequences
     */
    public DifferenceSummaryModel(FeaturedSequence[] seqs, int type, int diffColor)
    {
        for (int i = 0; i < seqs.length; ++i) {
            m_diffList.add(null);
        }
        m_compType = type;
        m_diffColor = diffColor;
        m_seqs = Arrays.asList(seqs);
        this.seqs = seqs;
        m_seqListChanged = true;
        refresh();
        m_seqListChanged = false;
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
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setFont(new Font("", Font.PLAIN, 10));
        g.setPaint(Color.white);
        g.fillRect(0, 0, w, h);

        int x = 10;

        switch (m_diffColor) {
            case ColorScheme.DIFF_CLASSIC_SCHEME:
                g.setPaint(Color.green);
                g.fillRect(x, 10, 10, 30);
                g.setPaint(Color.black);
                g.drawRect(x, 10, 10, 30);
                g.drawString("Insertion", x + 15, 30);

                x += 15 + g.getFontMetrics().stringWidth("Insertion") + 5;

                g.setPaint(Color.blue);
                g.fillRect(x, 10, 10, 30);
                g.setPaint(Color.black);
                g.drawRect(x, 10, 10, 30);
                g.drawString("Substitution", x + 15, 30);

                x += 15 + g.getFontMetrics().stringWidth("Substitution") + 5;

                g.setPaint(Color.red);
                g.fillRect(x, 10, 10, 30);
                g.setPaint(Color.black);
                g.drawRect(x, 10, 10, 30);
                g.drawString("Deletion", x + 15, 30);

                x += 15 + g.getFontMetrics().stringWidth("Deletion") + 5;

                g.setPaint(new Color(255, 150, 150));
                g.fillRect(x, 10, 10, 30);
                g.setPaint(Color.black);
                g.drawRect(x, 10, 10, 30);
                g.drawString("Gene", x + 15, 30);

                x += 15 + g.getFontMetrics().stringWidth("Gene") + 5;

                g.setPaint(new Color(000, 000, 000));
                g.fillRect(x, 10, 10, 30);
                g.setPaint(Color.black);
                g.drawRect(x, 10, 10, 30);
                g.drawString("Primer", x + 15, 30);

                x += 15 + g.getFontMetrics().stringWidth("Primer") + 5;

                drawCommentLegend(g, x, 10, 10, 30);
                //g.setPaint(new Color(255, 0, 255));
                //g.fillRect(x,10,10,30);
                //g.setPaint(Color.black);
                //g.drawRect(x,10,10,30);
                g.drawString("Comment", x + 15, 30);

                x += 15 + g.getFontMetrics().stringWidth("Comment") + 5;
                break;
            case ColorScheme.DIFF_NT_SCHEME:
                g.setPaint(Color.yellow);
                g.fillRect(x, 10, 10, 30);
                g.setPaint(Color.black);
                g.drawRect(x, 10, 10, 30);
                g.drawString("A", x + 15, 30);

                x += 15 + g.getFontMetrics().stringWidth("A") + 5;

                g.setPaint(Color.green);
                g.fillRect(x, 10, 10, 30);
                g.setPaint(Color.black);
                g.drawRect(x, 10, 10, 30);
                g.drawString("C", x + 15, 30);

                x += 15 + g.getFontMetrics().stringWidth("C") + 5;

                g.setPaint(Color.cyan);
                g.fillRect(x, 10, 10, 30);
                g.setPaint(Color.black);
                g.drawRect(x, 10, 10, 30);
                g.drawString("G", x + 15, 30);

                x += 15 + g.getFontMetrics().stringWidth("G") + 5;

                g.setPaint(Color.orange);
                g.fillRect(x, 10, 10, 30);
                g.setPaint(Color.black);
                g.drawRect(x, 10, 10, 30);
                g.drawString("T", x + 15, 30);

                x += 15 + g.getFontMetrics().stringWidth("T") + 5;

                break;
        }


        return img;
    }

    /**
     * Get the sequence type for this model
     *
     * @return one of <CODE>EditableSequence.AA_SEQUENCE</CODE>,
     * <CODE>EditableSequence.DNA_SEQUENCE</CODE>, or
     * <CODE>EditableSequence.GENERIC_SEQUENCE</CODE>
     */
    public int getSequenceType()
    {
        return EditableSequence.GENERIC_SEQUENCE;
    }

    /**
     * add a sequence to the alignment
     *
     * @param seq The sequence to add
     */
    public void addSequence(FeaturedSequence seq)
    {
        super.addSequence(seq);
        m_diffList.add(null);

        m_seqListChanged = true;
        refresh();
        m_seqListChanged = false;
    }

    /**
     * remove a sequence from the alignment
     *
     * @param seq the sequence to remove
     */
    public void removeSequence(FeaturedSequence seq)
    {
        super.removeSequence(seq);
        m_diffList.remove(0);

        m_seqListChanged = true;
        refresh();
        m_seqListChanged = false;
    }

    /**
     * Get the length (in characters) of the alignment.  This is usually the
     * length of the longest string in the alignment
     *
     * @return the length
     */
    public int getLength()
    {
        return m_cons.getLength();
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
     *
     * @return a <CODE>SummaryIndicator</CODE> object
     */
    public SummaryIndicator getIndicator(int index, int start, int stop) {

        if ((index >= m_seqs.size()) || (index < 0)) {
            return null;
        }

        int[] diffs = (int[]) m_diffList.get(index);

        //System.out.print("Getting range for "+getSequence(index).getName()+" for "+start+" to "+stop+": ");
        //for ( int i=start; i<start+10; ++i ) {
        //    System.out.print("["+diffs[i]+"]");
        //}
        int type = SummaryIndicator.IND_TICK;
        Color col = null;

        int subs = 0;
        int dels = 0;
        int ins = 0;

        //Construct a map of (values, counts)
        Map<Integer, Integer> typeCount = new HashMap<Integer, Integer>();

        for (int j = start; j < stop; ++j) {
            if (j >= diffs.length) {
                break;
            } else {
                Integer count = typeCount.get(diffs[j]);
                typeCount.put(diffs[j], count != null ? count + 1 : 1);
            }
        }

        typeCount.remove(DifferenceType.I_NONE);
        Integer maxCount = 0;
        Integer maxVal = 0;

        for (Integer diff : typeCount.keySet()) {
            if (typeCount.get(diff) > maxCount) {
                maxCount = typeCount.get(diff);
                maxVal = diff;
            }
        }
        col = EditPanel.getDifferenceColor(maxVal, m_diffColor);
        if (col == null) {
            type = SummaryIndicator.IND_EMPTY;
        }
        return new SummaryIndicator(type, col);
    }



    /**
     * Get the indicator for a location on a sequence in the alignment
     *
     * @param index    The index of the sequence in the alignment
     * @param position The index along the sequence (gapped) of the alignment
     *
     * @return the indicator
     */
    public SummaryIndicator getIndicator(
            int index,
        int position)
    {
        if ((index >= m_seqs.size()) || (index < 0)) {
            return null;
        }

        int[] diffs = (int[]) m_diffList.get(index);

        int type = SummaryIndicator.IND_TICK;

        Color col = EditPanel.getDifferenceColor(diffs[position], m_diffColor);

        if (col == null) {
            type = SummaryIndicator.IND_EMPTY;
        }

        return new SummaryIndicator(type, col);
    }

    /**
     * Re-retrieve and recalculate data based on an external event
     */
    public void refresh()
    {

        if (m_seqListChanged || (m_cons == null)) {
            m_cons = ConsensusFactory.createConsensus(ConsensusFactory.IDENTITY, getSequences());
        }

        m_cons.calculate();

        refreshDifferences();
    }

    /**
     * refresh the difference lists
     */
    protected void refreshDifferences()
    {
        //against consensus
        if (m_compType == CONSENSUS_COMPARISON) {
            for (int i = 0; i < countSequences(); ++i) {
                int[] diffs = SequenceTools.getNTDifferences(((FeaturedSequence) m_seqs.get(i)).toString(), m_cons.getSequence(0, m_cons.getLength()));
                m_diffList.set(i, diffs);
            }
        }
        //pairwise comparison
        else if (m_compType == PAIRWISE_COMPARISON) {
            for (int i = 0; i < countSequences(); ++i) {
                int[] diffs;
                if (i < countSequences() - 1)
                    diffs = SequenceTools.getNTDifferences(((FeaturedSequence) m_seqs.get(i)).toString(), ((FeaturedSequence) m_seqs.get(i + 1)).toString());
                else
                    diffs = SequenceTools.getNTDifferences(((FeaturedSequence) m_seqs.get(i)).toString(), ((FeaturedSequence) m_seqs.get(i)).toString());

                m_diffList.set(i, diffs);
            }
        }
        //against top sequence
        else {
            for (int i = 0; i < countSequences(); ++i) {
                int[] diffs = SequenceTools.getNTDifferences(((FeaturedSequence) m_seqs.get(i)).toString(), ((FeaturedSequence) m_seqs.get(0)).toString());
                m_diffList.set(i, diffs);
            }
        }
    }
}
