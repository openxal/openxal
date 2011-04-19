package xal.model.xml;

import xal.tools.data.IDataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

import xal.model.probe.Probe;

/**
 * Parses the description of a <code>Probe</code> from an XML file.  Returns
 * an instance of the appropriate <code>Probe</code> species.  This class simply
 * provides methods for opening an xml document and creating a <code>
 * IDataAdaptor</code> for it.  It delegates the task of reading the <code>
 * Probe</code> definition and instantiating the <code>Probe</code> to that
 * class.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class ProbeXmlParser {


	// ******** static parsing methods
	
	
	/**
	 * Parse the XML file specified by the supplied URI.  Return a <code>
	 * Probe</code> of the appropriate species.
	 * 
	 * @param fileUri the URI specifying the XML file to parse
	 * @return the Probe object described by the XML file
	 */
	public static Probe parse(String fileUri) throws ParsingException {
		ProbeXmlParser parser = new ProbeXmlParser();
		return parser.parseProbeFile(fileUri);
	}
	
	/**
     * Parse the given data source and build a probe object according
     * to that described.  Convenience method calling 
     * <code>{@link #parseAdaptor(IDataAdaptor)}</code>.
	 *
	 * @param adaptor      data source containing probe description
	 * 
	 * @return             new probe object with properties specified by the data source
	 * 
	 * @throws ParsingException    general formating error in the data source
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 14, 2011
	 */
	public static Probe parseDataAdaptor(IDataAdaptor adaptor) throws ParsingException {
		ProbeXmlParser parser = new ProbeXmlParser();
		return parser.parseAdaptor(adaptor);
	}
	
	
	// ******** instance-based parsing methods
	
	
	/**
	 * Parse the XML file specified by the supplied URI.  Return a <code>
	 * Probe</code> of the appropriate species.
	 * 
	 * @param fileUri the URI specifying the XML file to parse
	 * @return the Probe object described by the XML file
	 */
	public Probe parseProbeFile(String fileUri) throws ParsingException {
		XmlDataAdaptor document = XmlDataAdaptor.adaptorForUrl(fileUri, false);
		return parseAdaptor(document);
	}
	
	
	/**
     * Parse the given data source and build a probe object according
     * to that described.  Calls method
     * <code>{@link Probe#readFrom(IDataAdaptor)}</code> to do
     * the actual parsing.
     *
     * @param adaptor      data source containing probe description
     * 
     * @return             new probe object with properties specified by the data source
     * 
     * @throws ParsingException    general formating error in the data source
     *
	 * @author Christopher K. Allen
	 * @since  Apr 14, 2011
	 */
	public Probe parseAdaptor(IDataAdaptor adaptor) throws ParsingException {
		return Probe.readFrom(adaptor);
	}

}
