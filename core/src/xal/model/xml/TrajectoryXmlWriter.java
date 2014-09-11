package xal.model.xml;

import java.io.File;
import java.io.IOException;

import xal.model.probe.traj.Trajectory;
import xal.tools.xml.XmlDataAdaptor;

/**
 * Writes a Trajectory object to an XML document.  This class simply creates an
 * XML document with the appropriate header information.  The work of adding
 * the Trajectory representation is delegated to the Trajectory class.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class TrajectoryXmlWriter {

	@SuppressWarnings("unused")
    private static final String DOC_TYPE = Trajectory.TRAJ_LABEL;
	@SuppressWarnings("unused")
    private static final String DTD_URI = "xml/Trajectory.mod.xal.dtd";

	/**
	 * Writes supplied <code>Trajectory</code> to the specified XML file.
	 * 
	 * @param trajectory The <code>Trajectory</code> to output
	 * @param fileURI String URI of output file
	 * 
	 * @throws IOException error writing to fileURI
	 */
	public static void writeXml(Trajectory<?> trajectory, String fileURI) 
			throws IOException {
		TrajectoryXmlWriter writer = new TrajectoryXmlWriter();
		writer.writeTrajectoryToFile(trajectory, fileURI);
	}
	
	/**
	 * Writes supplied <code>Trajectory</code> to the specified XML file.
	 * 
	 * @param trajectory Probe to write to XML file
	 * @param fileURI String URI of XML output file
	 * 
	 * @throws IOException error writing to fileURI
	 */
	public void writeTrajectoryToFile(Trajectory<?> trajectory, String fileURI) 
			throws IOException {
		XmlDataAdaptor document = 
			XmlDataAdaptor.newEmptyDocumentAdaptor(null, null);
		trajectory.save(document);
		document.writeTo(new File(fileURI));
	}
	
}
