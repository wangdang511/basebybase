/*
 * Base-by-base: Whole Genome pairwise and multiple alignment editor
 * Copyright (C) 2003  Dr. Chris Upton, University of Victoria
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ca.virology.baseByBase.io;

import ca.virology.lib.io.reader.*;

import org.xml.sax.Attributes;

//xml packages
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;

import java.io.*;

import java.net.URL;

//java packages
import java.util.*;


/**
 * This is the most basic inventory content handler.  It currently leaves empty
 * definitions for methods declared in <CODE>ContentHandler</CODE>.
 *
 * @author Ryan Brodie
 */
public class WebFileParser implements ContentHandler {
    //~ Static fields/initializers /////////////////////////////////////////////

    protected static final int STATELESS = -1;
    protected static final int FILE = 0;
    protected static final int DESC = 1;

    //~ Instance fields ////////////////////////////////////////////////////////

    protected List m_files = new ArrayList();
    protected String curName;
    protected String curURL;
    protected int curSeqs;
    protected int curLength;
    protected StringBuffer curDesc;
    protected String curType;
    protected int state = STATELESS;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Creates a new WebFileParser object.
     *
     * @param fileURL the url of the file to parse
     */
    public WebFileParser(URL fileURL) {
        System.out.println("parsing xml:" + fileURL);

        XMLReader parser = null;
        GeneralErrorHandler errors = new GeneralErrorHandler();

        try {
            Reader read = new InputStreamReader(fileURL.openStream());

            parser = SAXParserFactory.createSAXParser(false);
            parser.setContentHandler(this);
            parser.setErrorHandler(errors);
            parser.parse(new InputSource(read));
        } catch (Exception ex) {
            System.out.println("parse() exception: " + ex.getMessage());
        }
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * get the files in a list
     *
     * @return a list of files
     */
    public List getFiles() {
        return m_files;
    }

    //
    // SAX Callbacks
    //
    public void characters(char[] ch, int start, int length) {
        if (state == DESC) {
            for (int i = 0; i < length; ++i) {
                curDesc.append(ch[i + start]);
            }

            state = STATELESS;
        }
    }

    /**
     * endDocument event
     */
    public void endDocument() {
    }

    /**
     * endElement event
     *
     * @param namespaceURI -
     * @param localName    -
     * @param qName        -
     */
    public void endElement(String namespaceURI, String localName, String qName) {
        if (qName.equals("file")) {
            m_files.add(new WebFile(curName, curType, curURL, curSeqs, curLength, curDesc.toString()));
        }
    }

    /**
     * endPrefixMapping event
     *
     * @param prefix -
     */
    public void endPrefixMapping(String prefix) {
    }

    /**
     * ignorableWhitespace event
     *
     * @param ch     -
     * @param start  -
     * @param length -
     */
    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    /**
     * processingInstruction event
     *
     * @param target -
     * @param data   -
     */
    public void processingInstruction(String target, String data) {
    }

    /**
     * set the document locator
     *
     * @param locator the new locator
     */
    public void setDocumentLocator(Locator locator) {
    }

    /**
     * skippedEntity event
     *
     * @param name -
     */
    public void skippedEntity(String name) {
    }

    /**
     * startDocument event
     */
    public void startDocument() {
    }

    /**
     * startElement event
     *
     * @param namespaceURI !
     * @param localName    !
     * @param qname        !
     * @param atts         !
     */
    public void startElement(String namespaceURI, String localName, String qname, Attributes atts) {
        if (qname.equals("file")) {
            curName = atts.getValue("name");
            curURL = atts.getValue("url");

            String seqAtt = atts.getValue("sequences");
            String lenAtt = atts.getValue("length");
            curType = atts.getValue("fileType");

            if ((seqAtt != null) && !seqAtt.equals("")) {
                curSeqs = Integer.parseInt(seqAtt);
            }

            if ((lenAtt != null) && !lenAtt.equals("")) {
                curLength = Integer.parseInt(lenAtt);
            }
        } else if (qname.equals("description")) {
            curDesc = new StringBuffer();
            state = DESC;
        }
    }

    /**
     * startPrefixMapping event
     *
     * @param prefix !
     * @param uri    !
     */
    public void startPrefixMapping(String prefix, String uri) {
    }
}