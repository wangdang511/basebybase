package ca.virology.baseByBase.io;

/**
 * This represents a file loadable from the web
 *
 * @author Ryan Brodie
 * @version $Revision: 1.1.1.1 $
 */
public class WebFile {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected String name;
    protected String url;
    protected int seqs;
    protected int length;
    protected String desc;
    protected String type;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new WebFile object.
     *
     * @param name   the common name of the file
     * @param type   the type of the file
     * @param url    the url of the file
     * @param seqs   the number of sequences in the file
     * @param length the length of the alignment
     * @param desc   a description of the file
     */
    public WebFile(String name, String type, String url, int seqs, int length, String desc) {
        this.name = name;
        this.url = url;
        this.seqs = seqs;
        this.length = length;
        this.desc = desc;
        this.type = type;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the file type (bbb, aln, fasta, etc.)
     *
     * @return the file type
     */
    public String getFileType() {
        return type;
    }

    /**
     * get the common name of the file
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * get the url of the file
     *
     * @return the url
     */
    public String getURL() {
        return url;
    }

    /**
     * get the number of sequences in the alignment
     *
     * @return the sequence count
     */
    public int getSequenceCount() {
        return seqs;
    }

    /**
     * get the length of the alignment
     *
     * @return the length of the alignment.  This should be the length of the
     * longest sequence in the alignment.
     */
    public int getLength() {
        return length;
    }

    /**
     * get the description of the file
     *
     * @return the description
     */
    public String getDescription() {
        return desc;
    }
}