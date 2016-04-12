/*
 * XmlWriter.java
 *
 * Created on March 1, 2002, 9:49 AM
 */

package xal.tools.xml;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

import org.w3c.dom.*;


/**
 * XmlWriter is a class that can write an XML document to a desired output
 * in XML format.  It only supports writing of Document, Element and Attribute
 * nodes.  Other node support may be added as the need arises.  XML writer is
 * quite generic for writing XML 1.0 documents with standard encoding.
 *
 * This class mainly implements convenience methods for writing documents to
 * different outputs.  The work of writing XML constructs to text is left to 
 * the member classes.
 *
 * @author  tap
 */
public class XmlWriter {
	// constants
    static final Pattern AMPERSAND_PATTERN;
	static final Pattern LEFT_ANGLE_BRACKET_PATTERN;
	static final Pattern QUOTE_PATTERN;
	static final Pattern APOSTROPHE_PATTERN;
	static final Pattern LINEBREAK_PATTERN;
	
    Document document;
    Writer writer;	
	
	
	static {
		AMPERSAND_PATTERN = Pattern.compile("&");
		LEFT_ANGLE_BRACKET_PATTERN = Pattern.compile("<");
		QUOTE_PATTERN = Pattern.compile("\"");
		APOSTROPHE_PATTERN = Pattern.compile("\'");
		LINEBREAK_PATTERN = Pattern.compile("\n");
	}
	
    
    /** Creates new XmlWriter */
    public XmlWriter(Document newDocument, Writer newWriter) {
        document = newDocument;
        writer = newWriter;
    }


    /** write the XML document to the URL */
    static public void writeToUrl( final Document document, final URL url ) throws MalformedURLException, IOException {  
		try {
			final File file = new File( url.toURI() );
			writeToFile( document, file );
		}
		catch( URISyntaxException exception ) {
			throw new RuntimeException( "URI Syntax Exception", exception );
		}
    }
    
        
    /** write the XML document to the URL spec */
    static public void writeToUrlSpec(Document newDocument, String urlSpec) throws MalformedURLException, IOException {  
        URL url = new URL(urlSpec);
        writeToUrl(newDocument, url);
    }
    
    
    /** write the XML document to the UNIX file path */
    static public void writeToPath(Document newDocument, String filePath) throws IOException {
        Writer fileWriter = new FileWriter(filePath);
        writeToWriter(newDocument, fileWriter);
    }
    
    
    /** 
	 * Write the document to a file 
	 * @param document the document to write
	 * @param file the file to be written
	 */
    static public void writeToFile( final Document document, final File file ) throws IOException {
        Writer fileWriter = new FileWriter( file );
        writeToWriter( document, fileWriter );
        fileWriter.flush();
        fileWriter.close();
    }
    
        
    /** return a string representation of the XML document */
    static public String writeToString(Document newDocument) {
        Writer stringWriter = new StringWriter();
        writeToWriter( newDocument, stringWriter );
        return stringWriter.toString();
    }
    
   
    /** write the XML document to the specified writer */
    static public void writeToWriter(Document newDocument, Writer aWriter) {
        XmlWriter xmlWriter = new XmlWriter( newDocument, aWriter );
        xmlWriter.write();
    }
    
    
    /** write the document to the instance's writer */
    protected void write() {
        try {
            DocumentWriter docWriter = new DocumentWriter();
            docWriter.write();
        }
        catch (java.io.IOException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "I/O Error writing XML.", exception );
            System.err.println(exception);
            exception.printStackTrace();
        }
    }

    
    
    /**
     *  Abstract class for writing an XML node
     */
    private abstract class NodeWriter {
        Node rootNode;      // the node being written
        int nestLevel;      // nesting level of this node in the document tree
        String indent;      // leading indent based on nest level
        List<Node> childNodes;    // list of children of the root node
        
        /** create a new NodeWriter */
        public NodeWriter(Node newRoot, int newLevel) {
            rootNode = newRoot;
            nestLevel = newLevel;
            readChildren();
            makeIndent();
        }
        
        
        /** get the children nodes associated with the rootNode */
        protected void readChildren() {
            NodeList nodeList = rootNode.getChildNodes();
            childNodes = new ArrayList<Node>();
            
            int numChildren = nodeList.getLength();
            for ( int index = 0 ; index < numChildren ; index++ ) {
                Node node = nodeList.item(index);
                String nodeName = node.getNodeName();
                if ( nodeName != null ) {
                    childNodes.add( node );
                }
            }
        }
        
        
        /** write a node */
        public void write() throws java.io.IOException {
            writeOpenTag();
            writeChildren();
            writeCloseTag();
        }

        
        /** write an opening XML tag (e.g. &lt;img src="some.gif"&gt; ) */
        protected void writeOpenTag() throws java.io.IOException {
            String name = rootNode.getNodeName();       
            if ( name == null || !(rootNode instanceof Element) )  return;
            writer.write(indent + "<" + name);
            
            writeAttributes();  // write node attributes if any
            
            // no child nodes => close the open tag after the attributes
            if ( childNodes.size() == 0 ) {
                writer.write("/");
            }
            writer.write(">\n");
            writer.flush();
        }
        
        
        /** write XML node attributes within the opening tag */
        protected void writeAttributes() throws java.io.IOException {
            NamedNodeMap attributeMap = rootNode.getAttributes();
            if ( attributeMap == null )  return;
            int numAttributes = attributeMap.getLength();
            
            for ( int index = 0 ; index < numAttributes ; index++ ) {
                Attr attribute = (Attr)attributeMap.item(index);
                String name = attribute.getName();
                String value = attribute.getValue();
                
                writer.write(" " + name + "=\"" + escapedValue(value) + "\"");
            }
        }
		
		
		/**
		 * Escape special characters as necessary to form valid XML.  In particular we escape
		 * the ampersand and less than characters.
		 * @param value The value whose special characters are escaped as necessary.
		 * @return The proper XML attribute value.
		 */
		private String escapedValue(String value) {
			String escapedValue = AMPERSAND_PATTERN.matcher(value).replaceAll("&amp;");
			escapedValue = LEFT_ANGLE_BRACKET_PATTERN.matcher(escapedValue).replaceAll("&lt;");
			escapedValue = QUOTE_PATTERN.matcher(escapedValue).replaceAll("&quot;");
			escapedValue = APOSTROPHE_PATTERN.matcher(escapedValue).replaceAll("&apos;");
			escapedValue = LINEBREAK_PATTERN.matcher(escapedValue).replaceAll("&#x0A;");
			return escapedValue;
		}
        
        
        /** write child nodes */
        protected void writeChildren() throws java.io.IOException {
            for ( final Node nextNode : childNodes ) {
                if ( !(nextNode instanceof Element) )  continue;
                Element element = (Element)nextNode;
                ElementWriter elementWriter = new ElementWriter( element, nestLevel+1 );
                elementWriter.write();
            }
            
            writer.flush();
        }
        
        
        /** write the closing XML tag if any (e.g. &lt;/table&gt;) */
        protected void writeCloseTag() throws java.io.IOException {
            // no child nodes => no closing tag
            if ( childNodes.size() == 0 )  return;

            String name = rootNode.getNodeName();
            if ( name == null || !(rootNode instanceof Element) )  return;
            
            String body = indent + "</" + rootNode.getNodeName() + ">\n";
            writer.write(body);
            
            writer.flush();
        }
        
        
        /** indent tags for easy reading according to the nesting level */
        protected void makeIndent() {
            indent = "";
            
            for ( int index = 0 ; index < nestLevel ; index++ ) {
                indent += "    ";
            }
        }
    }

    

    /**
     *  Class for writing an XML element
     */
    private class ElementWriter extends NodeWriter {    
        public ElementWriter(Element newElement, int newLevel) {
            super(newElement, newLevel);
        }
    }


    
    /**
     *  Class for writing an XML document
     */
    private class DocumentWriter extends NodeWriter {
        public DocumentWriter() {
            super(document, -1);
        }
        
        
        /** override write to prepend a document header */
        public void write() throws java.io.IOException {
            writeHeader();  // prepend with an XML header
            super.write();
        }
        
        
        /** write a standard XML header */
        public void writeHeader() throws java.io.IOException {
            writer.write("<?xml version = '1.0' encoding = 'UTF-8'?>\n");
            writeDoctype();
            writer.flush();
        }
        
        
        protected void writeDoctype() throws java.io.IOException {
            DocumentType docType = document.getDoctype();
            
            if ( docType == null )  return;
            
            String name = docType.getName();
            String dtdUri = docType.getSystemId();
            writer.write("<!DOCTYPE " + name + " SYSTEM \"" + dtdUri + "\">\n");
        }
    }
}





