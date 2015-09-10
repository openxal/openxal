/*
 * SetupIO.java
 */

package xal.app.wirescan;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import xal.extension.application.*;
import xal.tools.data.*;
import xal.tools.xml.*;
import xal.smf.*;
import xal.extension.application.smf.*;
import xal.smf.data.*;

/**
 * This class does the read from and writing to xml files for saving and restoring
 * a wirescanner application session.
 * Items that can be saved and restored within a wirescanner application session are:
 * <ul>
 *	<li>Selected accelerator
 *	<li>Selected sequence(s)
 *	<li>Selected wirescanner(s)
 *	<li>Type of scan (parallel or series)
 * </ul>
 *
 * @author	S. Bunch
 * @version	1.0
 */
public class SetupIO {
	private WireDoc theDoc;
	private WireWindow theWindow;
	private XmlDataAdaptor xdaRead, xdaWrite;
	private DataAdaptor da;

	/**
	 * The SetupIO constructor takes a WireDoc and WireWindow type.
	 *
	 * @param window		The WireWindow currently in use
	 * @param wiredocument	The WireDoc currently in use
	 * @see WireDoc
	 * @see WireWindow
	 */
	public SetupIO(WireDoc wiredocument, WireWindow window) {
		theWindow = window;
		theDoc = wiredocument;
	}

	/**
	 * The function used to save a current wirescanner application session.
	 * 
	 * @param url	The url to save session to
	 * @see URL
	 */
	protected void saveSetupTo(URL url) {
		xdaWrite = XmlDataAdaptor.newEmptyDocumentAdaptor();
		da = xdaWrite.createChild("WireSettings");
		DataAdaptor daXMLFile = da.createChild("accelerator");
		daXMLFile.setValue("xmlFile", theDoc.getAcceleratorFilePath());
		// save selected sequences
		ArrayList<String> seqs;
		if (theDoc.getSelectedSequence() != null) {
			DataAdaptor daSeq = da.createChild("sequences");
			daSeq.setValue("name", theDoc.getSelectedSequence().getId());
			if (theDoc.getSelectedSequence().getClass()
			                == AcceleratorSeqCombo.class) {
				AcceleratorSeqCombo asc =
				        (AcceleratorSeqCombo) theDoc.getSelectedSequence();
				seqs = (ArrayList<String>) asc.getConstituentNames();
			} else {
				seqs = new ArrayList<String>();
				seqs.add(theDoc.getSelectedSequence().getId());
			}
			Iterator<String> itr = seqs.iterator();
			while (itr.hasNext()) {
				DataAdaptor daSeqComponents = daSeq.createChild("seq");
				daSeqComponents.setValue("name", itr.next());
			}
		}
		// save selected wirescanners
		ArrayList<AcceleratorNode> wires;
		theWindow.selectWires();
		if (theDoc.selectedWires != null) {
			DataAdaptor daWire = da.createChild("wirescanners");
			daWire.setValue("name", theDoc.selectedWires);
			wires = new ArrayList<AcceleratorNode>();
			wires = theDoc.selectedWires;

			Iterator<AcceleratorNode> itr = wires.iterator();
			while (itr.hasNext()) {
				DataAdaptor daWireComponents = daWire.createChild("wire");
				daWireComponents.setValue("name", itr.next());
			}
		}
		DataAdaptor daButton = da.createChild("seriesparallel");
		daButton.setValue("onoroff", theDoc.series.toString());
		xdaWrite.writeToUrl(url);
	}

	/**
	 * The function used to restore a previous wirescanner application session.
	 * 
	 * @param url	The url to restore session from
	 * @see URL
	 */
	protected void readSetupFrom(URL url) {
		XmlDataAdaptor xda =  XmlDataAdaptor.adaptorForUrl(url, false);
		DataAdaptor xyzda = xda.childAdaptor("WireSettings");
		// get the accelerator file
		String acceleratorPath = xyzda.childAdaptor("accelerator").stringValue("xmlFile");
		if ( acceleratorPath.length() > 0 ) {
			theDoc.setAcceleratorFilePath(acceleratorPath);
			System.out.println("accelFile = " + theDoc.getAcceleratorFilePath());
			String accelUrl = "file://"+ theDoc.getAcceleratorFilePath();
			try {
				XMLDataManager  dMgr = new XMLDataManager(accelUrl);
				theDoc.setAccelerator(dMgr.getAccelerator(), theDoc.getAcceleratorFilePath());
			}
			catch(Exception exception) {
				System.err.println( exception.getMessage() );
				exception.printStackTrace();
			}
			theDoc.acceleratorChanged();
		}
		// set up the right sequence combo from selected primaries:
		List<DataAdaptor> temp = xyzda.childAdaptors("sequences");
		if (temp.isEmpty())
			return; // bail out, nothing left to do
		ArrayList<AcceleratorSeq> seqs = new ArrayList<AcceleratorSeq>();
		DataAdaptor da2a = xyzda.childAdaptor("sequences");
		String seqName = da2a.stringValue("name");
		temp = da2a.childAdaptors("seq");
		Iterator<DataAdaptor> itr = temp.iterator();
		while (itr.hasNext()) {
			DataAdaptor da = itr.next();
			seqs.add(theDoc.getAccelerator().getSequence(da.stringValue("name")));
		}
		theDoc.setSelectedSequence(new AcceleratorSeqCombo(seqName, seqs));
		// set up the selected wirescanners
		List<DataAdaptor> temp2 = xyzda.childAdaptors("wirescanners");
		if (temp2.isEmpty())
			return; // bail out, nothing left to do
		//ArrayList wires = new ArrayList();
		DataAdaptor da2a2 = xyzda.childAdaptor("wirescanners");
		temp2 = da2a2.childAdaptors("wire");
		Iterator<DataAdaptor> itr2 = temp2.iterator();
		while (itr2.hasNext()) {
			DataAdaptor da = itr2.next();
			for(int i = 0; i < theDoc.wirescanners.size(); i++)
			{
				if(theWindow.tableModel.getValueAt(i,0).toString().equals(da.stringValue("name")))
				{
					theWindow.tableModel.setValueAt(Boolean.TRUE, i, 1);
				}
			}
		}
		DataAdaptor daButton2 = xyzda.childAdaptor("seriesparallel");
		if (daButton2.stringValue("onoroff").equals("false"))
		{
			theDoc.series = Boolean.FALSE;
			theWindow.parallelButton.setSelected(true);
		}
		else
		{
			theDoc.series = Boolean.TRUE;
			theWindow.seriesButton.setSelected(true);
		}
		theDoc.setHasChanges(false);
	}
}
