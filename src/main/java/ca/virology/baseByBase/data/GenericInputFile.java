package ca.virology.baseByBase.data;


//biojava import

import org.biojava.bio.seq.*;

import ca.virology.lib.util.common.Logger;

import java.awt.Color;

import java.io.*;

import java.util.*;


/**
 * This class is used to retrieve data from a file in a standard format.  This
 * format is laid out below as an example:<BR><BR><CODE>> Title of 1st
 * analysis 22|222|POSITIVE|name of region 1|ff00ff 226|1000|NEGATIVE|name of
 * region 2 > Title of 2nd analysis 500|9800|POSITIVE|name of region 3|ffff00
 * 10320|11000|POSITIVE|name of region 4 </CODE> Generally, each analysis is
 * represented as follows, were many may be included in a single
 * file:<BR><CODE>> Description of analysis
 * {StartPos}|{StopPos}|{POSITIVE/NEGATIVE}|[{description of region}]|[{Color
 * of bar}] [{StartPos}|{StopPos}|{POSITIVE/NEGATIVE}|[{description of
 * region}]|[{Color of bar}]]</CODE>
 *
 * @author Sangeeta Neti
 */
public class GenericInputFile
        implements GenericInput {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected File m_file;
    protected List m_analyses;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new GenericInput based on the given filename
     *
     * @param filename The name of the file to input
     * @throws java.io.IOException If there is a problem accessing the file
     */
    public GenericInputFile(String filename)
            throws java.io.IOException {
        this(new File(filename));
    }

    /**
     * Creates a generic input from the given file
     *
     * @param file the file to input
     * @throws java.io.IOException if there is a problem accessing the file
     */
    public GenericInputFile(File file)
            throws java.io.IOException {
        m_file = file;
        m_analyses = new LinkedList();

        parseInput();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * <B>Copied from GenericInput</B><BR> This returns an array of analysis
     * objects which represent the  results of the input to BBB
     *
     * @return An array of <CODE>GenericInput.Analysis</CODE> objects
     */
    public GenericInput.Analysis[] getAnalyses() {
        return (GenericInput.Analysis[]) m_analyses.toArray(
                new GenericInput.Analysis[0]);
    }

    /**
     * <B>Copied from GenericInput</B><BR> Returns the number of analyses
     * parsed by this input
     *
     * @return The number of analysis objects held by this input
     */
    public int countAnalyses() {
        return m_analyses.size();
    }

    /**
     * <B>Copied from GenericInput</B><BR> Returns an iterator over the list of
     * analysis objects
     *
     * @return an <CODE>Iterator</CODE> reference
     */
    public Iterator analysisIterator() {
        return m_analyses.iterator();
    }

    /**
     * <B>Copied from GenericInput</B><BR> Add an analysis to this generic
     * input
     *
     * @param a The analysis to add
     */
    public void addAnalysis(Analysis a) {
        m_analyses.add(a);
    }

    /**
     * <B>Copied from GenericInput</B><BR> Remove an analysis from this input
     *
     * @param a The analysis to remove
     */
    public void removeAnalysis(Analysis a) {
        m_analyses.remove(a);
    }

    /**
     * Parse the input from the file
     *
     * @throws java.io.IOException if there is a problem reading from the file
     */
    protected void parseInput()
            throws java.io.IOException {
        final BufferedReader in = new BufferedReader(new FileReader(m_file));

        String line = "";
        Analysis currentAnalysis = null;

        while (true) {
            line = in.readLine();

            if (line == null) {
                break;
            }

            line = line.trim();

            if (line.startsWith(">")) { // found an analysis

                if (currentAnalysis != null) {
                    m_analyses.add(currentAnalysis);
                }

                String title = line.substring(1);
                Logger.println("Analysis: " + title);
                currentAnalysis = new Analysis(title);
            } else {
                if (currentAnalysis == null) {
                    throw new java.io.IOException(
                            "File format Error: Expected \">\"");
                }

                StringTokenizer st = new StringTokenizer(line, "|");

                String token;
                int start;
                int stop;
                String name;
                StrandedFeature.Strand strand;
                Color color;

                // start
                try {
                    token = st.nextToken();
                    start = Integer.parseInt(token);

                    //Debug.print(token+" ");
                } catch (NumberFormatException ex) {
                    throw new java.io.IOException(
                            "File Format Error: Expected beginning sequence location");
                }

                // stop
                try {
                    token = st.nextToken();
                    stop = Integer.parseInt(token);

                    //Debug.print(token+" ");
                } catch (NumberFormatException ex) {
                    throw new java.io.IOException(
                            "File Format Error: Expected ending sequence location");
                }

                // strand
                token = st.nextToken();

                //Debug.print(token+" ");
                if (token.equals("POSITIVE")) {
                    strand = StrandedFeature.POSITIVE;
                } else if (token.equals("NEGATIVE")) {
                    strand = StrandedFeature.NEGATIVE;
                } else {
                    throw new java.io.IOException(
                            "File Format Error: Expected \"POSITIVE\" or \"NEGATIVE\" Strand indication");
                }

                // description ( optional )
                if (st.hasMoreTokens()) {
                    name = st.nextToken();

                    //Debug.print(name+" ");
                } else {
                    name = "";
                }

                Region r = new Region(start, stop, strand, name);


                // color (optional)
                String colorname = null;
                if (st.hasMoreTokens()) {
                    colorname = st.nextToken();
                    color = ca.virology.lib.util.gui.UITool.stringToColor(colorname);

                    //Debug.print( color+" " );
                    r.setColor(color);
                } else {
                    r.setColor(Color.black);
                }

                //Debug.print("\n");
                currentAnalysis.addRegion(r);
            }
        }

        if (currentAnalysis != null) {
            m_analyses.add(currentAnalysis);
        }
    }
}