package ca.virology.baseByBase.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import ca.virology.baseByBase.data.Consensus.EmptyConsensus;

/**
 * This class represents a data storage object for mRNA expression data in the mochiview format.
 *
 * @author will
 */

public class mRNAs extends Object {

    public mRNA[] mRNAs;


    /**
     * @param file - the file to be read in.
     */

    public mRNAs(File file) {

        try {
            BufferedReader m_in = new BufferedReader(new FileReader(file));
            String line;
            int count = 0;
            //  Read in only the files that contain the appropriate information (it contains info on +/- strand
            while ((line = m_in.readLine()) != null) {
                if (line.contains("+") || line.contains("-")) {
                    count++;
                }
            }
            m_in.close();
            m_in = new BufferedReader(new FileReader(file));
            mRNAs = new mRNA[count];

            //  Process the data and place it in an array of mRNA objects (see mRNA class)
            int index = 0;
            while ((line = m_in.readLine()) != null) {
                if (line.contains("+") || line.contains("-")) {
                    String[] curr_line = line.split("\t");
                    try {
                        mRNAs[index++] = new mRNA(curr_line[0], Integer.parseInt(curr_line[1]), curr_line[3], Integer.parseInt(curr_line[4]));
                    } catch (NumberFormatException er) {
                        System.out.println("NumErr");
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("OutOfBoundsErr");
                    } catch (NullPointerException ec) {
                        System.out.println("NullPointerExc");
                    }
                }
            }
            m_in.close();

            System.out.println("Opened: " + count + " entries.");


        } catch (FileNotFoundException e1) {
            mRNAs = null;
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @return the mRNA objects
     */
    public mRNA[] getmRNA() {
        return mRNAs;
    }

    /**
     * @return an array of th different sequences in the mRNA sequence data
     */
    public String[] getNames() {

        Vector<String> names = new Vector<String>();


        for (int i = 0; i < mRNAs.length; i++) {
            if (!names.contains(mRNAs[i].name)) {
                names.add(mRNAs[i].name);
            }
        }

        return (String[]) names.toArray();
    }


}
