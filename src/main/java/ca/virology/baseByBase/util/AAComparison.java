package ca.virology.baseByBase.util;

import java.util.HashMap;

public class AAComparison {
    public int changes;
    public int silent;
    private HashMap hashMap;

    public AAComparison() {
        String[] values = {
                "F", "F", "L", "L", "Y", "Y", "*", "*", // last 2 used to be X
                "L", "L", "L", "L", "H", "H", "Q", "Q", "I", "I", "I", "M", "N", "N",
                "K", "K", "V", "V", "V", "V", "D", "D", "E", "E", "S", "S", "S", "S",
                "C", "C", "*", "W", //2nd last used to be U
                "P", "P", "P", "P", "R", "R", "R", "R", "T", "T", "T", "T", "S", "S",
                "R", "R", "A", "A", "A", "A", "G", "G", "G", "G", "F", "L", "S", "S",
                "S", "S", "S", "S", "S", "S", "S", "S", "S", "Y", "C", "L", "L", "L",
                "L", "L", "L", "L", "L", "L", "L", "L", "P", "P", "P", "P", "P", "P",
                "P", "P", "P", "P", "P", "H", "Q", "R", "R", "R", "R", "R", "R", "R",
                "R", "R", "R", "R", "I", "I", "I", "I", "T", "T", "T", "T", "T", "T",
                "T", "T", "T", "T", "T", "N", "K", "S", "R", "V", "V", "V", "V", "V",
                "V", "V", "V", "V", "V", "V", "A", "A", "A", "A", "A", "A", "A", "A",
                "A", "A", "A", "D", "E", "G", "G", "G", "G", "G", "G", "G", "G", "G",
                "G", "G", "L", "L", "L", "R", "R", "R"
        };

        String[] keys = {
                "TTT", "TTC", "TTA", "TTG", "TAT", "TAC", "TAA", "TAG", "CTT", "CTC",
                "CTA", "CTG", "CAT", "CAC", "CAA", "CAG", "ATT", "ATC", "ATA", "ATG",
                "AAT", "AAC", "AAA", "AAG", "GTT", "GTC", "GTA", "GTG", "GAT", "GAC",
                "GAA", "GAG", "TCT", "TCC", "TCA", "TCG", "TGT", "TGC", "TGA", "TGG",
                "CCT", "CCC", "CCA", "CCG", "CGT", "CGC", "CGA", "CGG", "ACT", "ACC",
                "ACA", "ACG", "AGT", "AGC", "AGA", "AGG", "GCT", "GCC", "GCA", "GCG",
                "GGT", "GGC", "GGA", "GGG", "TTY", "TTR", "TCN", "TCR", "TCY", "TCK",
                "TCM", "TCS", "TCW", "TCB", "TCD", "TCH", "TCV", "TAY", "TGY", "CTN",
                "CTR", "CTY", "CTK", "CTM", "CTS", "CTW", "CTB", "CTD", "CTH", "CTV",
                "CCN", "CCR", "CCY", "CCK", "CCM", "CCS", "CCW", "CCB", "CCD", "CCH",
                "CCV", "CAY", "CAR", "CGN", "CGR", "CGY", "CGK", "CGM", "CGS", "CGW",
                "CGB", "CGD", "CGH", "CGV", "ATH", "ATY", "ATM", "ATW", "ACN", "ACR",
                "ACY", "ACK", "ACM", "ACS", "ACW", "ACB", "ACD", "ACH", "ACV", "AAY",
                "AAR", "AGY", "AGR", "GTN", "GTR", "GTY", "GTK", "GTM", "GTS", "GTW",
                "GTB", "GTD", "GTH", "GTV", "GCN", "GCR", "GCY", "GCK", "GCM", "GCS",
                "GCW", "GCB", "GCD", "GCH", "GCV", "GAY", "GAR", "GGN", "GGR", "GGY",
                "GGK", "GGM", "GGS", "GGW", "GGB", "GGD", "GGH", "GGV", "YTR", "YTA",
                "YTG", "MGR", "MGA", "MGG"
        };

        hashMap = new HashMap();

        for (int i = 0; i < values.length; i++) {
            hashMap.put(keys[i], values[i]);
        }

    }

    public String getAminoAcid(String codon) {
        if (hashMap.get(codon) == null)
            return "X";
        else
            return (String) hashMap.get(codon);
    }

    public void calculateChanges(String query, String standard) {
        changes = silent = 0;
        String codon1, codon2, aa1, aa2;
        int max = standard.length() / 3;  //floor division, this is the number of codons

        for (int i = 0; i < max * 3; i += 3) {
            codon1 = query.substring(i, i + 3).toUpperCase();
            codon2 = standard.substring(i, i + 3).toUpperCase();
            if (!codon1.equals(codon2)) {
                aa1 = getAminoAcid(codon1);
                aa2 = getAminoAcid(codon2);
                if (aa1.equals(aa2)) silent++;
                else changes++;
            }
        }
    }
}
