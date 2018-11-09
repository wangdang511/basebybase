package ca.virology.baseByBase.gui.CodeHop.VGOFiles;


import ca.virology.baseByBase.gui.CodeHop.*;
import ca.virology.lib.util.common.Logger;
import ca.virology.vgo.data.DatasourceException;
import ca.virology.vgo.gui.Utils;
import org.biojava.bio.BioException;
import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.utils.ChangeVetoException;

import java.util.ArrayList;
import java.util.List;


public class GeneFactory2 {
    protected static GeneFactory2 c_instance;

    protected GeneFactory2() {
    }

    public static synchronized GeneFactory2 getInstance() {
        if (c_instance == null) {
            c_instance = new GeneFactory2();
        }
        return c_instance;
    }


    public synchronized List populateGeneFeatures(FeatureHolder holder) throws DatasourceException {
        ArrayList returnValue = new ArrayList();

        String dbName = "no DB";
        int primerCounter = 1;

        for (Block block : CodeHopWizard.blockList) {
            for (Primer p : block.forwardPrimerList) {

                SimpleAnnotation a = new SimpleAnnotation();
                try {
                    a.setProperty("name", "anything");
                    a.setProperty("blocknum", 0);
                    a.setProperty("id", 0);
                    a.setProperty("family", 0);
                    a.setProperty("primernum", 0);
                    a.setProperty("prefix", 0.0);
                    a.setProperty("gene_fragment", "anything");
                    a.setProperty("gb_name", "anything");

                } catch (ChangeVetoException var29) {
                    Utils.showError("Error setting Properties in Gene");
                    Logger.log(var29);
                }

                StrandedFeature.Template temp1 = new StrandedFeature.Template();
                temp1.type = "gene";
                temp1.source = dbName;
                temp1.location = new RangeLocation(p.startNTPosInSeq + 1, p.endNTPosInSeq); //+1 was an easy fix for display purposes only
                temp1.annotation = a;
                temp1.strand = StrandedFeature.POSITIVE;

                try {
                    StrandedFeature ex = (StrandedFeature) holder.createFeature(temp1);
                    ex.getAnnotation().setProperty("SequenceViewerActor", new GeneActor2(ex, dbName, p, primerCounter));
                    returnValue.add(ex);
                    primerCounter++;
                } catch (BioException var27) {
                    Utils.showError("BioException populating gene features");
                    Logger.log(var27);
                } catch (ChangeVetoException var28) {
                    Utils.showError("ChangeVetoException populating geneFeatures");
                    Logger.log(var28);
                }
            }

            for (Primer p : block.reversePrimerList) {

                SimpleAnnotation a = new SimpleAnnotation();
                try {
                    a.setProperty("name", "anything");
                    a.setProperty("id", 0);
                    a.setProperty("family", 0);
                    a.setProperty("blocknum", 0);
                    a.setProperty("primernum", 0);
                    a.setProperty("prefix", 0.0);
                    a.setProperty("gene_fragment", "anything");
                    a.setProperty("gb_name", "anything");
                } catch (ChangeVetoException var29) {
                    Utils.showError("Error setting Properties in Gene");
                    Logger.log(var29);
                }

                StrandedFeature.Template temp1 = new StrandedFeature.Template();
                temp1.type = "gene";
                temp1.source = dbName;
                temp1.location = new RangeLocation(p.startNTPosInSeq + 1, p.endNTPosInSeq - 1); //+1 was an easy fix for display purposes only
                temp1.annotation = a;
                temp1.strand = StrandedFeature.NEGATIVE;

                try {
                    StrandedFeature ex = (StrandedFeature) holder.createFeature(temp1);
                    ex.getAnnotation().setProperty("SequenceViewerActor", new GeneActor2(ex, dbName, p, primerCounter));
                    returnValue.add(ex);
                    primerCounter++;
                } catch (BioException var27) {
                    Utils.showError("BioException populating gene features");
                    Logger.log(var27);
                } catch (ChangeVetoException var28) {
                    Utils.showError("ChangeVetoException populating geneFeatures");
                    Logger.log(var28);
                }
            }
        }
        return returnValue;
    }
}

