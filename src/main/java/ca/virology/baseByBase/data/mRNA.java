package ca.virology.baseByBase.data;

import java.util.ListIterator;

/**
 * @author will
 *         <p>
 *         This class represents a method of storing mRNA expression data
 *         each object represents a single data entry in the mRNA file
 */

public class mRNA extends Object {
    public String name;
    public int pos;            //The position of the counts measurement
    public int strand;        //
    public int counts;


    public mRNA(String name_val, int position, String strand_sense, int count_val) {

        name = name_val;
        pos = position;
        counts = count_val;

        if (strand_sense.equals("+")) {
            strand = 1;
        } else {
            strand = -1;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String new_name) {
        name = new_name;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int new_pos) {
        pos = new_pos;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(String new_strand) {
        if (new_strand.equals("+")) {
            strand = 1;
        } else {
            strand = -1;
        }
    }

    public int getCounts() {
        return counts;
    }

    public void setCounts(int new_counts) {
        counts = new_counts;
    }


}
