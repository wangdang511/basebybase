package ca.virology.baseByBase.gui.CodeHop;

/**
 * Created with IntelliJ IDEA.
 * User: localadmin
 * Date: 2015-10-01
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsensusAA {
    char aminoAcid;
    char frequencyIdentifier; //a character (star or space) that indicates whether there is only 1 type of amino acid in that column (100% conserved)

    public ConsensusAA(char aminoAcid, double freq) {
        this.aminoAcid = aminoAcid;
        if (freq == 1) {
            frequencyIdentifier = '*';
        } else {
            frequencyIdentifier = ' ';
            //this cutoff frequency must be changed once we know the actual cutoff
            if (freq < 0.6) {
                this.aminoAcid = Character.toLowerCase(aminoAcid);
            }
        }

    }
}
