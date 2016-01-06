package xal.model.xml;

import xal.model.probe.traj.Trajectory;
import xal.tools.xml.XmlDataAdaptor;

/**
 * Parses the XML representation of a Trajectory object and its state history.
 * Creates Trajectory and ProbeState instances of the appropriate species based
 * on type info in the XML file.  This class is responsible only for reading the
 * XML file and creating the corresponding <code>DataAdaptor</code>.  The task
 * of reading the DataAdaptor contents is delegated to the Trajectory class
 * itself, since it is responsible for writing the XML file.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 * @deprecated  This functionality is now contained in the <code>Trajectory</code> class itself
 * 
 */
@Deprecated
public class TrajectoryXmlParser {
	
	/**
	 * Parse the XML file specified by the supplied URI.  Return a <code>
	 * Trajectory</code> of the appropriate species.
	 * 
	 * @param fileUri the URI specifying the XML file to parse
	 * @return the Trajectory object described by the XML file
	 */
	public static Trajectory<?> parse(String fileUri) throws ParsingException {
		TrajectoryXmlParser parser = new TrajectoryXmlParser();
		return parser.parseTrajectoryFile(fileUri);
	}
	
	/**
	 * Parse the XML file specified by the supplied URI.  Return a <code>
	 * Trajectory</code> of the appropriate species.
	 * 
	 * @param fileUri the URI specifying the XML file to parse
	 * @return the Trajectory object described by the XML file
	 */
	public Trajectory<?> parseTrajectoryFile(String fileUri) 
			throws ParsingException {
		XmlDataAdaptor document = 
			XmlDataAdaptor.adaptorForUrl(fileUri, false);
//		DataAdaptor trajNode = document.childAdaptor(Trajectory.TRAJ_LABEL);
//		return Trajectory.readFrom(trajNode);
            return Trajectory.loadFrom(document);
	}

}
