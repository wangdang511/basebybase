package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

import ca.virology.baseByBase.gui.CodeHop.CodeHopWizard;
import ca.virology.vgo.data.*;

import java.util.*;


/**
 * This class is used to aquire references to Organism implementations. It can
 * also be used to aquire a list of organisms which are available for
 * retrieval.  This is to say that if you don't provide a datasource to the
 * methods, there is an implementation that will retrieve the data from the default source
 * for the system.
 *
 * @author Ryan Brodie
 * @version 1.0
 * @date June 14, 2002
 */
public class OrganismFactory2 {
    /**
     * static instance
     */
    protected static OrganismFactory2 c_instance = null;

    protected static Map m_createdOrganisms;


    /**
     * Constructor for the OrganismFactory object
     */
    public OrganismFactory2() {
        m_createdOrganisms = new HashMap();
    }


    /**
     * Creates an Organism object corresponding to the given virusID, from the
     * given Datasource. In order to save space in memory, particular organisms
     * may only be created once. Thereafter, a reference is kept in memory and
     * passed back for each subsequent call to this organism. When the original
     * calling method believes it is done with the organism, it may call <CODE>closeOrganism</CODE>
     * which will remove this reference.
     *
     * @param virusID The Virus ID for the organism
     * @param ds      Description of the Parameter
     * @return A fully instantiated Organism Object
     * @throws DatasourceException if an error occurs regarding the datasource
     * @throws OrganismException   if an error occurs creating the organism
     *                             object
     * @author Ryan Brodie
     */
    public synchronized Organism createOrganism() throws DatasourceException, OrganismException {

        String lengthString = "";
        int rulerLength = CodeHopWizard.minSeqLen * 3; //*1.3 because VGO files cut off the ruler, change this is ruler is cut too short

        for (int i = 0; i < rulerLength; i++) {
            lengthString += "AA";
        }

        // dummy variables needed to create dummy organismImpl
        int vID = 0;
        String vName = "a";
        String vAbbrev = "a";
        String vOrder = "a";
        String vStrain = "a";
        int gbGINo = 0;
        String gbAccNo = "a";
        int size = 0;
        String link = "a";
        String prefix = "a";
        String dbName = "a";

        Organism org = new OrganismImpl(vID, vName, vAbbrev, vOrder,
                vStrain, gbGINo, gbAccNo,
                size, lengthString, link, prefix, dbName);

        GeneFactory2 gFact = GeneFactory2.getInstance();
        gFact.populateGeneFeatures(org.getSequence());

        return org;
    }


    /**
     * Returns the static instance of this object
     *
     * @return OrganismFactory object
     * @author Ryan Brodie
     */
    public static synchronized OrganismFactory2 getInstance() {
        if (c_instance == null) {
            c_instance = new OrganismFactory2();
        }
        return c_instance;
    }
}
