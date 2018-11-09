package ca.virology.baseByBase.data;


//biojava import

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

import java.awt.Color;

//java imports
import java.util.*;


/**
 * This interface provides methods which allow for the generic input of regions
 * of interest into BBB.  Implementing interfaces will determine where these
 * regions will come from and how they will be interpreted.
 *
 * @author Sangeeta Neti
 */
public interface GenericInput {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * This returns an array of analysis objects which represent the  results
     * of the input to BBB
     *
     * @return An array of <CODE>GenericInput.Analysis</CODE> objects
     */
    GenericInput.Analysis[] getAnalyses();

    /**
     * Returns the number of analyses parsed by this input
     *
     * @return The number of analysis objects held by this input
     */
    int countAnalyses();

    /**
     * Returns an iterator over the list of analysis objects
     *
     * @return an <CODE>Iterator</CODE> reference
     */
    Iterator analysisIterator();

    /**
     * Add an analysis to this generic input
     *
     * @param a The analysis to add
     */
    void addAnalysis(Analysis a);

    /**
     * Remove an analysis from this input
     *
     * @param a The analysis to remove
     */
    void removeAnalysis(Analysis a);

    //~ Inner Classes //////////////////////////////////////////////////////////

    /**
     * This class represents one analysis in a group contained within an input
     * from some source.
     */
    public class Analysis {
        protected List m_regions;
        protected String m_name;

        /**
         * Constructs an analysis with the given name and no regions
         *
         * @param name The name to give to this analysis
         */
        public Analysis(String name) {
            this(name, new Region[0]);
        }

        /**
         * Constructs an analysis with the given name and list of regions
         *
         * @param name    The name to give to this analysis
         * @param regions an array of regions of interest for this analysis
         */
        public Analysis(
                String name,
                Region[] regions) {
            m_regions = new LinkedList();
            m_name = name;

            for (int i = 0; i < regions.length; i++) {
                m_regions.add(regions[i]);
            }
        }

        /**
         * Adds the given region to this analysis
         *
         * @param r The region to add
         */
        public void addRegion(Region r) {
            m_regions.add(r);
        }

        /**
         * Removes the region from the analysis
         *
         * @param r The region to remove
         */
        public void removeRegion(Region r) {
            m_regions.remove(r);
        }

        /**
         * Returns the number of regions in this analysis
         *
         * @return an int value of the region count
         */
        public int countRegions() {
            return m_regions.size();
        }

        /**
         * Iterator
         *
         * @return A ListIterator pointing to the head of a list representing
         * the regions in this analysis
         */
        public ListIterator regionIterator() {
            return (ListIterator) m_regions.iterator();
        }

        /**
         * Get the name of this analysis
         *
         * @return the name
         */
        public String getName() {
            return m_name;
        }
    }

    /**
     * This class represents a region within an analysis contained within an
     * input from some source
     *
     * @author Sangeeta Neti
     */
    public class Region {
        protected Location m_location;
        protected String m_name;
        protected StrandedFeature.Strand m_strand;
        protected Color m_color;

        /**
         * Constructs a region surrounding the location given on the strand
         * with the given name
         *
         * @param l           The location for this region
         * @param strand      The strand on which this region shal exist
         * @param description A string description for this region of interest
         */
        public Region(
                Location l,
                StrandedFeature.Strand strand,
                String description) {
            m_location = l;
            m_name = description;
            m_strand = strand;
        }

        /**
         * Constructs a region surrounding the location given on the strand
         * with the given name
         *
         * @param start       The start position fo the location for this region
         * @param stop        The stop position for the location of this region
         * @param strand      The strand on which this region shal exist
         * @param description A string description for this region of interest
         */
        public Region(
                int start,
                int stop,
                StrandedFeature.Strand strand,
                String description) {
            this(new RangeLocation(start, stop), strand, description);
        }

        /**
         * Constructs a region with no name
         *
         * @param l      The location for the region
         * @param strand The strand for the region
         */
        public Region(
                Location l,
                StrandedFeature.Strand strand) {
            this(l, strand, "");
        }

        /**
         * Get the location for this region
         *
         * @return A location
         */
        public Location getLocation() {
            return m_location;
        }

        /**
         * Get the strand for this region
         *
         * @return A StrandedFeature.Strand
         */
        public StrandedFeature.Strand getStrand() {
            return m_strand;
        }

        /**
         * Get the name for this region
         *
         * @return A string name
         */
        public String getName() {
            return m_name;
        }

        /**
         * Get the color of this region
         *
         * @return the color
         */
        public Color getColor() {
            return m_color;
        }

        /**
         * Set the color of this region
         *
         * @param color the color
         */
        public void setColor(Color color) {
            m_color = color;
        }

        /**
         * Get the string representation for this region
         *
         * @return A string
         */
        public String toString() {
            StringBuffer b = new StringBuffer();

            synchronized (b) {
                b.append("Region \"" + m_name + "\" - ");
                b.append("(" + m_location.toString() + "), ");
                b.append(m_strand.toString());
            }

            return b.toString();
        }
    }
}
