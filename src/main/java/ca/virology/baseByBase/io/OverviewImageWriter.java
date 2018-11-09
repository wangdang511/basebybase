package ca.virology.baseByBase.io;

import ca.virology.baseByBase.data.SequenceSummaryModel;
import ca.virology.baseByBase.gui.HeaderPanel;
import ca.virology.baseByBase.gui.SummarySequencePanel;
import ca.virology.lib.io.sequenceData.FeaturedSequence;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;


public class OverviewImageWriter {

    public static void writeToFile(FeaturedSequence[] seqs, SequenceSummaryModel ssm, SummarySequencePanel[] ssps, int start, int stop, int width, int spacing, File outfile) throws java.io.IOException, IllegalArgumentException {
        try {
            System.out.println("Rendering");
            RenderedImage ri = createOverviewImage(seqs, ssm, ssps, start, stop, width, spacing);
            System.out.println("Writing");
            ImageIO.write(ri, "png", outfile);
        } catch (IOException ex) {
            throw (ex);
        } catch (IllegalArgumentException iaex) {
            throw (iaex);
        }
    }

    /**
     * create an image from an iterator of sequences
     *
     * @param seqs      the sequences
     * @param ssps      array of summary sequence panels - contains showGenes, showSubs, showIndels etc boolean values
     * @param start     start position
     * @param stop      stop position
     * @param width     image width
     * @param spacing   image spacing
     * @return the image
     * @throws java.io.IOException if there's an io problem
     */
    private static RenderedImage createOverviewImage(FeaturedSequence[] seqs, SequenceSummaryModel model, SummarySequencePanel[] ssps, int start, int stop, int width, int spacing) {
        System.out.println("Creating (custom " + start + "->" + stop + ") width: " + width);

        int margin = 5;
        int tail = 20;
        int iWidth = width - (2 * margin) - tail;
        int height = 0;
        int hWidth = 2;

        SummarySequencePanel[] views = new SummarySequencePanel[seqs.length];
        HeaderPanel[] heads = new HeaderPanel[seqs.length];

        int myStart = Math.max(0, start);
        int max_length = 0;
        for (FeaturedSequence seq : seqs) {
            if (seq.length() > max_length) {
                max_length = seq.length();
            }
        }
        int myStop = Math.min(max_length, stop);
        double myScale = ((double) (myStop - myStart + 1) / (double) iWidth);

        for (int i = 0; i < seqs.length; ++i) {
            //Create new ssp; assign same boolean values as original
            SummarySequencePanel ssp = new SummarySequencePanel(seqs[i], model);
            ssp.setShowGenes(ssps[i].showsGenes());
            ssp.setShowSubs(ssps[i].showsSubs());
            ssp.setShowIndels(ssps[i].showsIndels());
            ssp.setShowPrimers(ssps[i].showsPrimers());
            ssp.setShowComments(ssps[i].showsComments());

            ssp.setDisplayArea(myStart, myStop);
            ssp.setShowAll(false);
            ssp.setUseView(false);

            ssp.setScale(myScale);

            HeaderPanel hp = new HeaderPanel(ssp);
            hp.setDisplayFont(new Font("", Font.PLAIN, 12));
            height += (ssp.getHeight() + spacing);

            FontMetrics hfm = hp.getFontMetrics(hp.getDisplayFont());

            String[] head = ssp.getHeaders();

            for (int j = 0; j < head.length; ++j) {
                if (hfm.stringWidth(head[j]) > hWidth) {
                    hWidth = hfm.stringWidth(head[j]);
                }
            }
            views[i] = ssp;
            heads[i] = hp;
        }

        hWidth += 3;

        OverviewImageRulerPanel rp = new OverviewImageRulerPanel(myStart, myStop, iWidth);
        rp.setBackground(Color.white);
        rp.setOpaque(true);
        height += rp.getPreferredSize().height;

        BufferedImage ret = new BufferedImage(width + hWidth, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = ret.createGraphics();
        g.setClip(0, 0, width + hWidth, height);
        g.setColor(Color.white);
        g.fillRect(0, 0, width + hWidth, height);

        g.translate(margin, margin);
        int tval = 0;
        g.translate(hWidth, 0);

        for (int i = 0; i < seqs.length; ++i) {
            SummarySequencePanel ssp = views[i];
            ssp.renderDisplay(g);
            g.setColor(Color.black);
            g.drawLine(0, ssp.getHeight(), iWidth, ssp.getHeight());
            g.drawLine(iWidth, 0, iWidth, ssp.getHeight());
            g.translate(0, ssp.getHeight() + spacing);
            tval += (ssp.getHeight() + spacing);
        }

        g.translate(-hWidth, -tval);
        g.setColor(Color.white);
        g.fillRect(-margin, 0, hWidth + margin, tval);
        g.fillRect(hWidth + iWidth + 1, 0, 100, tval);

        for (int i = 0; i < seqs.length; ++i) {
            SummarySequencePanel ssp = views[i];
            HeaderPanel hp = heads[i];
            g.setColor(Color.black);
            g.drawLine(hWidth, 0, hWidth, ssp.getHeight());
            hp.paintComponent(g);
            g.translate(0, spacing);
        }

        g.translate(hWidth, 0);
        rp.paintComponent(g);
        g.dispose();

        return ret;
    }
}
