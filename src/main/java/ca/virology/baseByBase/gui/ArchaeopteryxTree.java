package ca.virology.baseByBase.gui;

import ca.virology.lib.io.sequenceData.EditableSequence;
import com.traviswheeler.libs.DefaultLogger;
import com.traviswheeler.libs.LogWriter;
import com.traviswheeler.ninja.ArgumentHandler;
import com.traviswheeler.ninja.DistanceCalculator;
import com.traviswheeler.ninja.SequenceFileReader.AlphabetType;
import com.traviswheeler.ninja.TreeBuilderManager;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AmbiguityDNACompoundSet;
import org.biojava3.core.sequence.io.FastaWriterHelper;
import org.forester.archaeopteryx.Archaeopteryx;
import org.forester.archaeopteryx.MainFrame;
import org.forester.phylogeny.Phylogeny;
import org.forester.phylogeny.PhylogenyNode;
import org.forester.phylogeny.iterators.ExternalForwardIterator;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ArchaeopteryxTree {

    public static final int NJ_TREE = 5005;
    //public static final int MAX_LIKELIHOOD = 5006;

    protected String m_name;
    protected String file_name;
    protected String tree_string;
    protected AlphabetType alph_type;
    protected EditableSequence[] seqs;
    final ArrayList list = new ArrayList();
    protected MainFrame tree_frame;
    protected int confirm;

    public ArchaeopteryxTree(EditableSequence[] seqs, int type, String name, String filename) {

        this.seqs = seqs;
        m_name = name;
        file_name = filename;
        String[] seqstrings = new String[seqs.length];
        String[] idstrings = new String[seqs.length];

        boolean aa = false;

        for (int i = 0; i < seqs.length; ++i) {
            EditableSequence seq = seqs[i];
            idstrings[i] = seq.getName();
            seqstrings[i] = seq.toString();
            idstrings[i] = idstrings[i].replace(' ', '_');
            idstrings[i] = idstrings[i].replace('(', '_');
            idstrings[i] = idstrings[i].replace(')', '_');
            idstrings[i] = idstrings[i].replace(':', '_');
            idstrings[i] = idstrings[i].replace(';', '_');
            idstrings[i] = idstrings[i].replace('&', '_');
            idstrings[i] = idstrings[i].replace('"', '_');
            idstrings[i] = idstrings[i].replace('\'', '_');
            idstrings[i] = idstrings[i].replace(',', '_');

            if (seqs[i].getSequenceType() == EditableSequence.AA_SEQUENCE) {
                aa = true;
            }
        }

        File tempFastaFile = null;
        try {
            tempFastaFile = File.createTempFile("align", ".fasta");
            tempFastaFile.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FastaWriterHelper fastaWriter = new FastaWriterHelper();

        if (aa) {
            alph_type = AlphabetType.amino;
            ArrayList<ProteinSequence> protAlign = new ArrayList<>();
            for (int i = 0; i < seqs.length; i++) {
                ProteinSequence protSeq = new ProteinSequence(seqstrings[i]);
                protSeq.setOriginalHeader(idstrings[i]);
                protAlign.add(protSeq);
            }

            try {
                fastaWriter.writeProteinSequence(tempFastaFile, protAlign);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            alph_type = AlphabetType.dna;
            ArrayList<DNASequence> aminoAlign = new ArrayList<>();
            for (int i = 0; i < seqs.length; i++) {
                DNASequence aminoSeq = new DNASequence(seqstrings[i], AmbiguityDNACompoundSet.getDNACompoundSet());
                aminoSeq.setOriginalHeader(idstrings[i]);
                aminoAlign.add(aminoSeq);
            }

            try {
                fastaWriter.writeNucleotideSequence(tempFastaFile, aminoAlign);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        switch (type) {
            case NJ_TREE:
                if (seqs.length > 2) {
                    tree_string = neighborJoiningTree(tempFastaFile);
                } else {
                    throw new IllegalArgumentException("You must have at least 3 sequences in your alignment to make a Neighbor Joining Tree.");
                }

                break;

            //case MAX_LIKELIHOOD:
            //tree_string = maximumLikelihoodTree(tempFastaFile);

            //break;

            /*case CLUSTER_SINGLE:

                break;

            case CLUSTER_UPGMA:

                break;

            case CLUSTER_WPGMA:

                break; */

            default:
                throw new IllegalArgumentException("unknown tree type");
        }

        String title = m_name;
        if (!file_name.equals("")) {
            title = title + " - " + file_name;
        }

        try {
            Phylogeny ph = Phylogeny.createInstanceFromNhxString(tree_string);
            rearrangeSequences(ph);
            Phylogeny[] phylogenies = new Phylogeny[1];
            phylogenies[0] = ph;
            tree_frame = Archaeopteryx.createApplication(phylogenies, "", title);
        } catch (IOException e) {
            e.printStackTrace();
        }

        confirm = JOptionPane.showConfirmDialog(tree_frame.getContentPane(), "Do you want to sort the sequences to match the tree?", "Sequences Modified", JOptionPane.YES_NO_OPTION);
    }

    public String neighborJoiningTree(File fasta) {
        TreeBuilderManager manager = new TreeBuilderManager("inmem", null, fasta.getAbsolutePath(), ArgumentHandler.InputType.alignment, alph_type, DistanceCalculator.CorrectionType.not_assigned);
        LogWriter.setLogger(new DefaultLogger());
        String treeString = null;
        try {
            treeString = manager.doJob();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return treeString;
    }

    //public String maximumLikelihoodTree(File fasta) {
    //    return "";
    //}

    public void rearrangeSequences(Phylogeny ph) {
        ExternalForwardIterator iterator = new ExternalForwardIterator(ph);
        ArrayList newlist = new ArrayList();

        while (iterator.hasNext()) {
            PhylogenyNode node = iterator.next();
            String nodeName = node.getName();
            for (int i = 0; i < seqs.length; i++) {
                String seqName = seqs[i].getName();
                seqName = seqName.replace(' ', '_');
                seqName = seqName.replace('(', '_');
                seqName = seqName.replace(')', '_');
                seqName = seqName.replace(':', '_');
                seqName = seqName.replace(';', '_');
                seqName = seqName.replace('&', '_');
                seqName = seqName.replace('"', '_');
                seqName = seqName.replace('\'', '_');
                seqName = seqName.replace(',', '_');
                if (nodeName.equals(seqName)) {
                    newlist.add(seqs[i]);
                }
            }
        }

        int h = newlist.size() - 1;
        for (int k = 0; k < newlist.size(); k++) {
            list.add(k, newlist.get(h--));
        }
    }

    public java.util.ArrayList getSequences() {
        return list;
    }

    public int getConfirm() {
        return confirm;
    }
}
