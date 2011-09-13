package xal.model.xml;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.w3c.dom.Document;

import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.elem.Element;

/**
 * Writes a Lattice object to an XML document, using the same format supported
 * by {@link LatticeXmlParser}.  Calling either the static method writeXml, or the
 * instance method writeLatticeToFile with a <code>Lattice</code> object and
 * a URI string identifying the fully-qualified output file, causes the elements
 * comprising the <code>Lattice</code> to be written to the specified file.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class LatticeXmlWriter {
	
	// share constants with <code>LatticeParser</code> for consistency
	private static final String DOC_TYPE = "Lattice";
	private static final String DTD_URI = "Lattice.mod.xal.dtd";
	private static final String LATTICE_LABEL = LatticeXmlParser.s_strElemLatt;
	private static final String SEQUENCE_LABEL = LatticeXmlParser.s_strElemSeq;
	private static final String ELEMENT_LABEL = LatticeXmlParser.s_strElemElem;
	private static final String COMMENT_LABEL = LatticeXmlParser.s_strElemComm;
	private static final String PARAM_LABEL = LatticeXmlParser.s_strElemParam;
	private static final String COMMENT_DELIMITER = LatticeXmlParser.s_strAttrSep;
	private static final String ID_LABEL = LatticeXmlParser.s_strAttrId;
	private static final String VERSION_LABEL = LatticeXmlParser.s_strAttrVer;	
	private static final String AUTHOR_LABEL = LatticeXmlParser.s_strAttrAuth;
	private static final String DATE_LABEL = LatticeXmlParser.s_strAttrDate;
	private static final String TEXT_LABEL = LatticeXmlParser.s_strAttrText;
	private static final String TYPE_LABEL = LatticeXmlParser.s_strAttrType;
	private static final String LENGTH_LABEL = LatticeXmlParser.s_strAttrLen;
	private static final String NAME_LABEL = LatticeXmlParser.s_strAttrName;
	private static final String VALUE_LABEL = LatticeXmlParser.s_strAttrVal;
	
	/**
	 * Writes supplied <code>Lattice</code> to the specified XML file.
	 * 
	 * @param lattice The <code>Lattice</code> to output
	 * @param fileURI String URI of output file
	 * 
	 * @throws IOException error writing to fileURI
	 */
	public static void writeXml(Lattice lattice, String fileURI) 
			throws IOException {
		LatticeXmlWriter writer = new LatticeXmlWriter();
		XmlDataAdaptor latticeDoc = writer.writeLatticeToDoc(lattice);
		latticeDoc.writeTo(new File(fileURI));
	}
	
	/**
	 * Returns a DOM for the supplied lattice.
	 * 
	 * @param lattice the Lattice whose DOM to return
	 * @return a DOM for the supplied lattice
	 */
	public static Document documentForLattice(Lattice lattice) throws IOException {
		LatticeXmlWriter writer = new LatticeXmlWriter();
		XmlDataAdaptor latticeDoc = writer.writeLatticeToDoc(lattice);
		return latticeDoc.document();
	}
	
	/**
	 * Writes supplied <code>Lattice</code> to the specified XML file.
	 * 
	 * @param lattice Lattice to write to XML file
	 * @param fileURI String URI of XML output file
	 * 
	 * @throws IOException error writing to fileURI
	 */
	private XmlDataAdaptor writeLatticeToDoc(Lattice lattice) 
			throws IOException {
		XmlDataAdaptor document = 
			XmlDataAdaptor.newEmptyDocumentAdaptor(DOC_TYPE, DTD_URI);
		writeLatticeObject(lattice, document);
		return document;
	}
	
	/**
	 * Used to start traversing the Lattice hierarchy.  Adds a top level node
	 * to the XML document representing the Lattice.  Adds optional attributes
	 * describing Lattice: ID, version, author, and date.  Adds optional comment
	 * element.  Then adds elements for the Lattice contents.
	 * 
	 * @param lattice The Lattice to write as XML
	 * @param container The XMLDataAdaptor representing the XML file
	 */
	private void writeLatticeObject(Lattice lattice, DataAdaptor container) {
		DataAdaptor latticeNode = container.createChild(LATTICE_LABEL);
		// the following four attributes are optional, so only add them if not null
		if (lattice.getId() != null)
			latticeNode.setValue(ID_LABEL, lattice.getId());
		if (lattice.getVersion() != null)
			latticeNode.setValue(VERSION_LABEL, lattice.getVersion());
		if (lattice.getAuthor() != null)
			latticeNode.setValue(AUTHOR_LABEL, lattice.getAuthor());
		if (lattice.getDate() != null)
			latticeNode.setValue(DATE_LABEL, lattice.getDate());
		// comment element is optional, so only add if not null
		if (lattice.getComments() != null)
			writeCommentObject(lattice.getComments(), latticeNode);
		writeCompositeContents(lattice, latticeNode);
	}
	
	/**
	 * Writes XML element for the supplied comment <code>String</code>.  Adds
	 * element label.  Uses delimiter specified in LatticeXmlParser to extract 3
	 * components of comment from the <code>String</code>.  This could probably
	 * be improved in conjunction with that LatticeXmlParser code that populates the
	 * comment String field.  Currently, simply punts if there are not 3 delimited
	 * tokens, and assumes they are in the same order as written by the parser.
	 * Clearly, this assumes the comment field has been populated by the parser
	 * and not set by some other method, in which case these assumptions may
	 * not be true.
	 * 
	 * @param comment A string containing author, date, and text, delimited by
	 * the String specified in the parser
	 * @param container The XML document node that contains the comment element
	 */
	private void writeCommentObject(String comment, DataAdaptor container) {
		DataAdaptor commentNode = container.createChild(COMMENT_LABEL);
		StringTokenizer st = new StringTokenizer(comment, COMMENT_DELIMITER);
		if (st.countTokens() == 3) {
			commentNode.setValue(AUTHOR_LABEL, st.nextToken());
			commentNode.setValue(DATE_LABEL, st.nextToken());
			commentNode.setValue(TEXT_LABEL, st.nextToken());
		}
	}
	
    /**
     * Writes XML element for supplied sequence object.  Creates child node
     * within the XML document node that contains this sequence (e.g., another
     * sequence or lattice).  Adds ID attribute.  Adds optional comment element.
     * Then adds elements contained by the sequence.
     * 
     * @param sequence the <code>ElementSeq</code> to write as XML
     * @param container the XML document node containing this sequence
     */
    private void writeCompositeObject(IComposite sequence, DataAdaptor container) {
        DataAdaptor sequenceNode = container.createChild(SEQUENCE_LABEL);
        sequenceNode.setValue(ID_LABEL, sequence.getId());
        // comment element is optional, so only add if not null
//        if (sequence.getComments() != null)
//            writeCommentObject(sequence.getComments(), sequenceNode);
        writeCompositeContents(sequence, sequenceNode);
    }
    
    /**
     * Writes the contents of a sequence (<code>ElementSeq</code> or <code>
     * Lattice</code> as XML.  Iterates over the sequence contents, calling the
     * appropriate method for writing contained <code>Element</code> and <code>
     * ElementSeq</code> objects.
     * 
     * @param sequence the <code>ElementSeq</code> whose contents to write
     * @param container the XML document node corresponding to the sequence
     */
    private void writeCompositeContents(IComposite sequence, DataAdaptor container) {
        Iterator<IComponent> iter = sequence.localIterator();        
        while (iter.hasNext())  {
            IComponent ifc = iter.next();            
            if (ifc instanceof IElement) {
                IElement elem = (IElement)ifc; 
                writeElementObject(elem, container);
            }            
            if (ifc instanceof IComposite)   {
                IComposite seq = (IComposite)ifc;
                writeCompositeObject(seq, container);
            }
        }
    }       

	/**
	 * Writes an <code>Element</code> as XML.  Creates a child node for the
	 * element within its containing sequence.  Adds the type attribute.  Adds
	 * optional ID and length attributes.  Uses bean introspection add parameter
	 * XML elements for each declared bean property.  The default bean introspection
	 * mechanism is employed to determine the property names, types, and read
	 * methods.  To customize the properties for an <code>Element</code> subclass,
	 * create a <code>BeanInfo</code> class that defines the public bean
	 * properties for the Element subclass.  Any exception encountered during
	 * the introspection process cases the System to exit.  This may not be
	 * the appropriate behavior, but seems like a reasonable starting place.
	 * 
	 * @param elem The <code>Element</code> to write as XML
	 * @param container The XML document node for the element's containing
	 * <code>ElementSeq</code>
	 */
	private void writeElementObject(IElement elem, DataAdaptor container) {
		DataAdaptor elementNode = container.createChild(ELEMENT_LABEL);
		elementNode.setValue(TYPE_LABEL, elem.getType());
		// ID attribute is optional, so don't add if null
		if (elem.getId() != null)
			elementNode.setValue(ID_LABEL, elem.getId());
		// don't add length attribute for thin elements
//		if (elem instanceof ThickElement) {
//			elementNode.setValue(LENGTH_LABEL, elem.getLength());
//		}
		try {
			BeanInfo bi = 
				Introspector.getBeanInfo (elem.getClass(), Element.class);		
			PropertyDescriptor pd[] = bi.getPropertyDescriptors();
			for (int i=0;i<pd.length;i++) {
				PropertyDescriptor propDesc = pd[i];
				Method getter = propDesc.getReadMethod();
				// getter is null if property doesn't have read access
				if (getter != null) {
					Object val = getter.invoke(elem, (Object[])null);
					if (val != null) {
						DataAdaptor paramNode = elementNode.createChild(PARAM_LABEL);
						paramNode.setValue(NAME_LABEL, propDesc.getName());
						paramNode.setValue(TYPE_LABEL, propDesc.getPropertyType().getName());
						paramNode.setValue(VALUE_LABEL, val.toString());
					}	
				}			
			}
			
		// one would not expect to encounter any of these exceptions in practice
		// because introspection is being used to determine an element bean's
		// properties, and then the properties returned by introspection are
		// written to the output XML document.  The class name, method name, and
		// property types used in introspection are taken directly from the Java
		// system, as opposed to using arbitrary strings that one might expect
		// to generate introspection errors.
		} catch (IntrospectionException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}