package ca.virology.baseByBase.data;

import ca.virology.lib.io.sequenceData.AnnotationKeys;
import ca.virology.lib.io.sequenceData.FeatureType;
import ca.virology.lib.io.sequenceData.FeaturedSequence;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


/**
 * Partial implementation of the <CODE>SequenceSummaryModel</CODE> interface
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public abstract class AbstractSequenceSummaryModel
    implements SequenceSummaryModel
{
    //~ Instance fields ////////////////////////////////////////////////////////

    protected List m_seqs = new ArrayList();

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Get the index of the given sequence in the alignment
     *
     * @param seq The sequence to index
     *
     * @return The index of the sequence or -1 if it's not there
     */
    public int getIndex(FeaturedSequence seq)
    {
        for (int i = 0; i < m_seqs.size(); ++i) {
            if (m_seqs.get(i) == seq) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the length (in characters) of the alignment.  This is usually the
     * length of the longest string in the alignment
     *
     * @return the length
     */
    public int getLength()
    {
        int ml = 0;

        for (int i = 0; i < m_seqs.size(); ++i) {
            FeaturedSequence seq = (FeaturedSequence) m_seqs.get(i);

            if (seq.length() > ml) {
                ml = seq.length();
            }
        }

        return ml;
    }

    /**
     * add a sequence to the alignment
     *
     * @param seq The sequence to add
     */
    public void addSequence(FeaturedSequence seq)
    {
        m_seqs.add(seq);
    }

    /**
     * remove a sequence from the alignment
     *
     * @param seq the sequence to remove
     */
    public void removeSequence(FeaturedSequence seq)
    {
        m_seqs.remove(seq);
    }

    /**
     * Get the seqences in this alignment in an array
     *
     * @return The sequence array
     */
    public FeaturedSequence[] getSequences()
    {
        return (FeaturedSequence[]) m_seqs.toArray(new FeaturedSequence[0]);
    }

    /**
     * Get the sequence at the given index
     *
     * @param index The index to retrieve
     *
     * @return the sequence for that position
     */
    public FeaturedSequence getSequence(int index)
    {
        if ((index >= m_seqs.size()) || (index < 0)) {
            return null;
        }

        return (FeaturedSequence) m_seqs.get(index);
    }

    /**
     * Count the number of sequences in the model
     *
     * @return The number of sequences
     */
    public int countSequences()
    {
        return m_seqs.size();
    }

    protected void drawCommentLegend(Graphics2D g, int x, int y, int width, int height) {

        FeatureFilter featureFilter = new FeatureFilter.ByType( FeatureType.COMMENT );
        List<Color> commentColors = new ArrayList<Color>();
        FeaturedSequence[] seqs = getSequences();
        for (FeaturedSequence seq: seqs) {
            FeatureHolder fh = seq.filter(featureFilter, false);
            Iterator j = fh.features();

            while (j.hasNext()) {
                StrandedFeature f = (StrandedFeature) j.next();
                //Text Color
                Color fgcolor = (Color)f.getAnnotation().getProperty(AnnotationKeys.FGCOLOR);
                //Comment Color
                Color bgcolor = (Color)f.getAnnotation().getProperty(AnnotationKeys.BGCOLOR);
                if (!commentColors.contains(bgcolor)) {
                    commentColors.add(bgcolor);
                }
            }
        }
        if (commentColors.size() <= 0) {
            commentColors.add(new Color(255, 0, 255));
        }
        double commentDelta = ((double)height)/((double)commentColors.size());

        for (int i = 0; i < commentColors.size(); i++) {
            g.setPaint(commentColors.get(i));
            int startY = y+((int)(((double)i)*commentDelta));
            int deltaHeight = (int)(Math.ceil(commentDelta));
            g.fillRect(x,startY,width,deltaHeight);
        }

        g.setPaint(Color.black);
        g.drawRect(x,10,10,30);
    }
}
