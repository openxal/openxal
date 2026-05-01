/*
 * WireDoc.java
 */

package xal.app.wirescan;

import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.PlainDocument;
import javax.swing.JFileChooser;

import xal.extension.application.*;
import xal.smf.*;
import xal.smf.impl.ProfileMonitor;
import xal.extension.application.smf.*;
import xal.tools.apputils.files.*;
import xal.ca.*;
import xal.ca.correlator.*;      // for correlator

/**
 * This class creates the main document that is used.
 * A WireDoc contains most methods unrelated to the
 * drawing of the window, for example the export feature
 * for saving wirescanner data is done here.
 *
 * @author	S. Bunch
 * @version	1.0
 * @see AcceleratorDocument
 */
public class WireDoc extends AcceleratorDocument {
	private boolean sequenceChanged = false;
	private PlainDocument textDocument;

	/** The wirescanners in the current sequence */
	protected ArrayList<AcceleratorNode> wirescanners;
	/** The wirescanners selected for scans */
	protected ArrayList<AcceleratorNode> selectedWires;
	/** True if series run, false if parallel run */
	protected Boolean series;
	/** The HashMap containing WireData for each wirescanner */
	protected HashMap<String, WireData> wireDataMap;
	/** Save/Restore component */
	protected SetupIO setupIO;
        
    protected String matlabFileName="";

    // for input file 
    private RecentFileTracker _inpFileTracker;

	Channel ch_v, ch_h, ch_d;
	Channel ch_posV, ch_posH, ch_posD;
	
    /** the correlator */
    protected ChannelCorrelator correlator;

    /** The blank WireDoc constructor. */
	public WireDoc() {
            this(null);
            // inp file management
            _inpFileTracker = new RecentFileTracker(1, this.getClass(), "recent_inputs");
	}

	/**
	 * WireDoc constructor that can read from a URL from a saved
	 * setup from a previous scan.
	 *
	 * @param url	The URL of the file to restore from
	 */
	public WireDoc(java.net.URL url) {
		setSource(url);
		makeTextDocument();
		selectedWires = new ArrayList<AcceleratorNode>();
		wireDataMap = new HashMap<String, WireData>();
	}

	/**
	 * Make a main window by instantiating the WireWindow. 
	 * Does the actual restore for a previous scan if available.
	 */
	public void makeMainWindow() {
		mainWindow = new WireWindow(this);
		setupIO = new SetupIO(this, (WireWindow) mainWindow);
		if(getSource() != null ) setupIO.readSetupFrom(getSource());
	}

	/**
	 * Save the document to the specified URL.
	 * @param url	The URL to which the document should be saved.
	 */
	public void saveDocumentAs(URL url) {
		setupIO.saveSetupTo(url);
		setHasChanges(false);
	}

	/**
	 * Allows an action to be used in a menu item in the menu bar.
	 * @param commander		Commander currently being used
	 * @see Commander
	 */
	public void customizeCommands(Commander commander) {
		Action exportDataAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;
			                          public void actionPerformed(ActionEvent event) {
				                          System.err.println("Export data");
				                          myWindow().takeAndPublishPVLoggerSnapshot();
				                          myWindow().exportAction();
			                          }
		                          };
		exportDataAction.putValue(Action.NAME, "exportdata");
		commander.registerAction(exportDataAction);

		Action convertFileTypeAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;

			                               public void actionPerformed(ActionEvent event) {
				                               System.err.println("Convert File Types");
	//			                               convertToMatlabType();
                                                               toMatlabFile();
                                                               
                                                               myWindow().msgField.setText("exported to " + matlabFileName);
			                               }
		                               };
		convertFileTypeAction.putValue(Action.NAME, "convertFileType");
		commander.registerAction(convertFileTypeAction);
	}

	private WireWindow myWindow() {
		return (WireWindow)mainWindow;
	}

	private void makeTextDocument() {
		textDocument = new PlainDocument();
		textDocument.addDocumentListener(new DocumentListener() {
			                                 public void changedUpdate(javax.swing.event.DocumentEvent evt) {
				                                 setHasChanges(true);
			                                 }
			                                 public void removeUpdate(DocumentEvent evt) {
				                                 setHasChanges(true);
			                                 }
			                                 public void insertUpdate(DocumentEvent evt) {
				                                 setHasChanges(true);
			                                 }
		                                 });
	}

	/**
	 * Handle the selected sequence changed event by displaying the elements of the 
	 * selected sequence in the main window.
	 * Will only allow one sequence selection per document.
	 */
	public void selectedSequenceChanged() {
		if (sequenceChanged == true) {
			System.out.println("Please open a new document to change the sequence");
		}
		if ( (selectedSequence != null) && (sequenceChanged == false)) {
			sequenceChanged = true;
			getWireNodes();
                        setHasChanges(true);
		}
	}

	/**
	 * Parses through the selected sequence and finds all wirescanner nodes
	 * available.
	 * It then calls the makeTable() function in the WireWindow class.
	 * @see WireWindow
	 */
	public void getWireNodes() {
		if ( selectedSequence != null ) {
			final List<AcceleratorNode> allWirescanners = selectedSequence.getNodesOfType( "WS", true );
			// we need to make sure we only get the wire scanners that are Profile Monitors (original API) because that is all this application can handle
			final ArrayList<AcceleratorNode> theScanners = new ArrayList<AcceleratorNode>();
			for ( final AcceleratorNode scanner : allWirescanners ) {
				if ( scanner instanceof ProfileMonitor ) {
					theScanners.add( scanner );
				}
			}
			wirescanners = theScanners;
			
			myWindow().makeTable();
			
			if (selectedSequence.getId().equals("RTBT")
					|| selectedSequence.getId().equals("RTBT2"))
			{
				connectHarpPVs();
				myWindow().dumpHarp.setEnabled(true);
			}
		}
	}
	
	private void connectHarpPVs() {
		ChannelFactory cf = ChannelFactory.defaultFactory();
		
		ch_h = cf.getChannel("RTBT_Diag:Harp30:SignalX_Rb");
		ch_v = cf.getChannel("RTBT_Diag:Harp30:SignalY_Rb");
		ch_d = cf.getChannel("RTBT_Diag:Harp30:SignalZ_Rb");
		ch_posH = cf.getChannel("RTBT_Diag:Harp30:PosX_Rb");
		ch_posV = cf.getChannel("RTBT_Diag:Harp30:PosY_Rb");
		ch_posD = cf.getChannel("RTBT_Diag:Harp30:PosZ_Rb");
		
		ch_h.requestConnection();
		ch_v.requestConnection();
		ch_d.requestConnection();
		ch_posH.requestConnection();
		ch_posV.requestConnection();
		ch_posD.requestConnection();
		Channel.flushIO();
		
        // Make a correlator:
		correlator = new ChannelCorrelator(100.);
		correlator.addChannel(ch_h);
		correlator.addChannel(ch_v);
		correlator.addChannel(ch_d);
		correlator.addChannel(ch_posH);
		correlator.addChannel(ch_posV);
		correlator.addChannel(ch_posD);
		Channel.flushIO();
		//correlator.startMonitoring();
	}

	/**
	 * Saves the wirescanner data to the specified file.
	 * @param f		The File to save to.
	 */
	public void saveToFile(File f) throws Exception {
		OutputStream f1 = new FileOutputStream(f);
		double xpos, ypos, zpos;
		double pos, x, y, z, xf, yf, zf, xsigf, ysigf, zsigf, xsigm, ysigm, zsigm;
		double xarf, yarf, zarf, xarm, yarm, zarm;
		double xampf, yampf, zampf, xampm, yampm, zampm;
		double xmnf, ymnf, zmnf, xmnm, ymnm, zmnm;
		double xofff, yofff, zofff, xoffm, yoffm, zoffm;
		double xslf, yslf, zslf, xslm, yslm, zslm;
		WireData wd;
		String line = "start time: " + myWindow().startTime.toString() + "\n\n";
		for(int i = 0; i < selectedWires.size(); i++) {
			wd = wireDataMap.get((selectedWires.get(i)).getId());
			int nsteps = wd.nsteps;
			xarf = wd.xareaf;
			yarf = wd.yareaf;
			zarf = wd.zareaf;
			xarm = wd.xaream;
			yarm = wd.yaream;
			zarm = wd.zaream;
			xampf = wd.xamplf;
			yampf = wd.yamplf;
			zampf = wd.zamplf;
			xampm = wd.xamplm;
			yampm = wd.yamplm;
			zampm = wd.zamplm;
			xmnf = wd.xmeanf;
			ymnf = wd.ymeanf;
			zmnf = wd.zmeanf;
			xmnm = wd.xmeanm;
			ymnm = wd.ymeanm;
			zmnm = wd.zmeanm;
			xofff = wd.xoffsetf;
			yofff = wd.yoffsetf;
			zofff = wd.zoffsetf;
			xoffm = wd.xoffsetm;
			yoffm = wd.yoffsetm;
			zoffm = wd.zoffsetm;
			xsigf = wd.xsigmaf;
			ysigf = wd.ysigmaf;
			zsigf = wd.zsigmaf;
			xsigm = wd.xsigmam;
			ysigm = wd.ysigmam;
			zsigm = wd.zsigmam;
			xslf = wd.xslopef;
			yslf = wd.yslopef;
			zslf = wd.zslopef;
			xslm = wd.xslopem;
			yslm = wd.yslopem;
			zslm = wd.zslopem;
			line = line + ( selectedWires.get(i)).getId() + "\n\n";
			line = line + "Name\tX Fit\tX RMS\tY Fit\tY RMS\tZ Fit\tZ RMS\n";
			line = line + "-------\t-----\t-----\t-----\t-----\t-----\t-----\n";
			line = line + "Area\t" + xarf + "\t" + xarm +"\t" + yarf + "\t" + yarm +"\t" + zarf + "\t" + zarm +"\n";
			line = line + "Ampl\t" + xampf + "\t" + xampm +"\t" + yampf + "\t" + yampm +"\t" + zampf + "\t" + zampm +"\n";
			line = line + "Mean\t" + xmnf + "\t" + xmnm +"\t" + ymnf + "\t" + ymnm +"\t" + zmnf + "\t" + zmnm +"\n";
			line = line + "Sigma\t" + xsigf + "\t" + xsigm +"\t" + ysigf + "\t" + ysigm +"\t" + zsigf + "\t" + zsigm +"\n";
			line = line + "Offset\t" + xofff + "\t" + xoffm +"\t" + yofff + "\t" + yoffm +"\t" + zofff + "\t" + zoffm +"\n";
			line = line + "Slope\t" + xslf + "\t" + xslm +"\t" + yslf + "\t" + yslm +"\t" + zslf + "\t" + zslm +"\n\n";
			line = line + "Position\tX Raw\tY Raw\tZ Raw\n";
			line = line + "--------\t-----\t-----\t-----\n";
			
			// Test whether the allocated array from channel access is big enough to hold all the points taken.
			// Just check one array so we don't overwhelm the user with error message dialogs.
			final int elementCount = wd.hfit.length;	// element count is the allocated array size available for data (actual data may not fill the array)
			if ( i == 0 && elementCount < nsteps ) {
				final String message = "The number of points reported for the scan: " + nsteps + "\n exceeds the number of elements returned from the scanner: " + elementCount  + ".\n We will only write the available data.";
				System.err.println( message );
				displayWarning( "Points Excceeds Data Size", message );
			}
			
			final int numPoints = Math.min( elementCount, nsteps );		// write out the minimum of array allocation (elementCount) and data points actually taken by the scanner (nsteps)
			for(int j = 0; j < numPoints; j++) {
				// warning: we should use the position array for diagonal data as "raw" position.
			    xpos = wd.pos[5][j];
			    zpos = wd.pos[4][j];
			    ypos = wd.pos[3][j];
				pos = wd.pos[1][j];
				x = wd.vvaluesS[j];
				y = wd.dvaluesS[j];
				z = wd.hvaluesS[j];
				line = line + pos + "\t\t" + x + "\t" + y + "\t" + z + "\t" + xpos + "\t" + ypos + "\t" + zpos + "\n";
			}			
			line = line + "\n";
			line = line + "Position\tX Fit\tY Fit\tZ Fit\n";
			line = line + "--------\t-----\t-----\t-----\n";
			for(int j = 0; j < numPoints; j++) {
				xpos = wd.pos[5][j];
			    zpos = wd.pos[4][j];
			    ypos = wd.pos[3][j];
			    pos = wd.pos[3][j];
				xf = wd.vfit[j];
				yf = wd.dfit[j];
				zf = wd.hfit[j];
				line = line + pos + "\t\t" + xf + "\t" + yf + "\t" + zf + "\t" + xpos + "\t" + ypos + "\t" + zpos + "\n";
			}
			line = line + "\n";
		}
                // if also log the machine status to the PV Logger, attach the PV logger ID here
                if (myWindow().pvLogged) {
                    line = line + "\nPVLoggerID = " + myWindow().pvLoggerId;
                }
		byte buf[] = line.getBytes();
		f1.write(buf);
		f1.close();
		myWindow().msgField.setText("Saved File Successfully...File saved to..." + f.toString());
	}
	
	
	/**
	 * convert exported data file to a file with special format for Matlab analysis program
	 */
	public void toMatlabFile() {
		
		ExportToMatlab toMatlab = new ExportToMatlab();
		
		String	currentDirectory = _inpFileTracker.getRecentFolderPath();
		
		JFileChooser fileChooser= new JFileChooser(currentDirectory);
		// read in data file
		int status= fileChooser.showOpenDialog(myWindow());
		if (status == JFileChooser.APPROVE_OPTION) {
			_inpFileTracker.cacheURL(fileChooser.getSelectedFile());
			File inFile= fileChooser.getSelectedFile();
			
			toMatlab.readFile(inFile);                
		}
		
		// write out Matlab file
		status = fileChooser.showSaveDialog(myWindow());
		if (status == JFileChooser.APPROVE_OPTION) {
			File outFile= fileChooser.getSelectedFile();
			
			toMatlab.dump2Matlab(outFile);
			matlabFileName = outFile.getName();
		}
	}
	
	
	/**
	 * reset the wireDataMap to empty state
	 */
	public void resetWireDataMap() {
	    if (myWindow().firstScan) {
			wireDataMap = new HashMap<String, WireData>();
			//        wireDataMap.clear();
			//                wireDataMap = null;
		}
	}
	
                    
}
