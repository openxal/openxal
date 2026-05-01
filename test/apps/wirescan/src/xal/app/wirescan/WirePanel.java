/*
 * WirePanel.java
 */

package xal.app.wirescan;

import java.awt.Dimension;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTable;

import xal.extension.application.*;
import xal.ca.*;
import xal.tools.apputils.*;
import xal.smf.*;
import xal.extension.application.smf.*;
import xal.smf.impl.*;

/**
 * This class constructs and sets up the individual panels
 * displayed with each selected wirescanner that is
 * currently running.
 *
 * @author    S. Bunch
 * @version   1.0
 * @see JPanel
 */
public class WirePanel  extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
	private EdgeLayout eLayout;
	private EdgeConstraints eConstraints;
	private JLabel statusLabel;
	private JLabel positionLabel;
	private JTextArea position;
	private JButton abortButton;
	private JButton logButton;
	protected PVUpdaterDbl posUpdate;
	private PVUpdaterByteArry statUpdate;
	private PVProdderByte statProdder;
	private WireDoc theDoc;
	private WireWindow theWindow;
	protected boolean isRunning;
	private JTable dataTable;
	private WireDataTableModel dataTableModel;
	private JScrollPane scrollDataPane;
        private boolean logScale = false;
	
	/** Status display for wirescanner */
	protected JTextArea status;
	/** The wirescanner itself */
	protected ProfileMonitor pm;
	/** Position updater */
	protected PVProdderDbl posProdder;
	/** True if current wirescanner is scanning, false if not */
	protected boolean isScanning;
	
	double maxPos = 180.0;
	
	double sqrt2 = Math.sqrt(2.);
	
//	private List legendItems;
//	private JCLegendItem tempItem = new JCLegendItem();

	/**
	 * The WirePanel constructor requires a ProfileMonitor (wirescanner)
	 * and the current WireDoc being used.
	 *
	 * @param scanner		The wirescanner this panel will monitor
	 * @param wiredocument	The current WireDoc being used
	 * @param window		The current WireWindow being used
	 */
	public WirePanel(ProfileMonitor scanner, WireDoc wiredocument, WireWindow window) {
		pm = scanner;
		theDoc = wiredocument;
		theWindow = window;
		isScanning = true;
		isRunning = false;
		makeContent();
	}

	/**
	 * The routine used to draw the panel and it's contents.
	 * Each panel contains a position and status display at the top,
	 * a chart to plot the raw and fitted data, a table to display
	 * the post scan data, and an abort button.
	 */
	protected void makeContent() {
		/* Set up layout */
		eLayout = new EdgeLayout();
		eConstraints = new EdgeConstraints();
		abortButton = new JButton("Abort Scan");

		try {
			maxPos = pm.getScanLength();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}

		
		setLayout(eLayout);
		/* Set up status */
		status = new JTextArea("Not Available");
		status.setBackground(Color.RED);
		statusLabel = new JLabel("Status");
		/* Set up position */
		positionLabel = new JLabel("Position");
		position = new JTextArea("NA");
		/* Set up data table */
		dataTableModel = new WireDataTableModel();
		dataTable = new JTable(dataTableModel);
		/* Scroll pane for data table */
		scrollDataPane = new JScrollPane(dataTable);
		scrollDataPane.setVisible(true);
		scrollDataPane.setPreferredSize(new Dimension(580, 115));
		status.setEditable(false);
		position.setEditable(false);
		/* Define layout */
		eLayout.setConstraints(statusLabel, 5, 5, 0, 0, EdgeLayout.TOP_LEFT, EdgeLayout.NO_GROWTH);
		eLayout.setConstraints(status, 5, 50, 0, 0, EdgeLayout.TOP_LEFT, EdgeLayout.NO_GROWTH);
		eLayout.setConstraints(positionLabel, 5, 250, 0, 0, EdgeLayout.TOP_LEFT, EdgeLayout.NO_GROWTH);
		eLayout.setConstraints(position, 5, 310, 0, 0, EdgeLayout.TOP_LEFT, EdgeLayout.NO_GROWTH);
		eLayout.setConstraints(abortButton, 0, 5, 5, 0, EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
//		eLayout.setConstraints(logButton, 0, 100, 5, 0, EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		eLayout.setConstraints(scrollDataPane, 0, 5, 180, 0, EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		/* Add components to layout */
		add(statusLabel);
		add(status);
		add(positionLabel);
		add(position);
		add(abortButton);
		add(scrollDataPane);
		abortButton.addActionListener(new java.awt.event.ActionListener() {
			                              public void actionPerformed(java.awt.event.ActionEvent evt) {
				                              abortAction();
			                              }
		                              });
//		logButton.addActionListener(new java.awt.event.ActionListener() {
//			                              public void actionPerformed(java.awt.event.ActionEvent evt) {
//				                              logAction();
//			                              }
//		                              });

		// Set up and start position monitor 
		posUpdate = new PVUpdaterDbl(pm.PosC);
		posProdder = new PVProdderDbl(position, posUpdate);
		posProdder.start();
		// Set up and start status monitor 
		statUpdate = new PVUpdaterByteArry(pm.StatArrayC);
		statProdder = new PVProdderByte(status, statUpdate, this);
		statProdder.start();
    setVisible(true);
                
	}

	/**
	 * The routine used to abort the current individual
	 * scan in progress.
	 */
	protected void abortAction() {
		/* Stop all running threads first */
		theWindow.stopProgProdders();
//		theWindow.progProdder.stopT();
		posProdder.stopT();
		statProdder.stopT();
		isScanning = false;
		/* Then try to stop the scan */
		try{
			pm.stopScan();
		}
		catch(ConnectionException ce){
			System.out.println("I could not establish a connection to " + ((AcceleratorNode) pm).getId()
			                   + " AbortScan to stop the scan");
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
			JOptionPane.showMessageDialog(theWindow, "Could not abort wirescan", ((AcceleratorNode) pm).getId(), JOptionPane.ERROR_MESSAGE);
		}
		catch(PutException pe){
			System.out.println("I could not establish a connection to " + ((AcceleratorNode) pm).getId()
			                   + " AbortScan to write to");
			System.err.println( pe.getMessage() );
			pe.printStackTrace();
			JOptionPane.showMessageDialog(theWindow, "Could not abort wirescan", ((AcceleratorNode) pm).getId(), JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void doPostGraph() {
		System.out.println("Started doPostGraph...");
		WireData wd = theDoc.wireDataMap.get(pm.getId());
              // get final diagonal raw data array
                try {
			wd.dvaluesS = pm.getDDataArray();
                        wd.pos[1] = pm.getPosArray();
                        int posLength = pm.getPosArray().length;
                        for (int i=posLength; i<wd.pos[1].length;i++)
                            wd.pos[1][i] = wd.pos[1][posLength-1];   
                } catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
              // get final vertical raw data array
		try {
			wd.vvaluesS = pm.getVDataArray();
                        int posLength = pm.getPosArray().length;
                        for (int i=0; i<posLength; i++)
                            wd.pos[0][i] = wd.pos[1][i]/sqrt2;
                        for (int i=posLength; i<wd.pos[0].length;i++)
                            wd.pos[0][i] = wd.pos[0][posLength-1];
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
              // get final horizontal raw data array
		try {
			wd.hvaluesS = pm.getHDataArray();
			wd.pos[2] = wd.pos[0];
//                        for (int i=0; i<pm.getPosArray().length; i++)
//                            wd.pos[2][i] = wd.pos[1][i]/sqrt2;
//                        for (int i=pm.getPosArray().length; i<wd.pos[2].length;i++)
//                            wd.pos[2][i] = wd.pos[2][pm.getPosArray().length-1];
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
              // get final vertical fit array
		try {
			wd.vfit = pm.getVFitArray();
                        wd.pos[3] = pm.getVPos();
//                        for (int i=0; i<wd.pos[1].length; i++)
//                            wd.pos[3][i] = wd.pos[1][i]/sqrt2;
                        int vPosLength = pm.getVPos().length;
                        for (int i=vPosLength; i<wd.pos[3].length;i++)
                            wd.pos[3][i] = wd.pos[3][vPosLength-1];
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
              // get final diagonal fit array
		try {
			wd.dfit = pm.getDFitArray();
                        for (int i=0; i<wd.pos[1].length; i++)
                            wd.pos[4][i] = wd.pos[1][i];
//                        for (int i=wd.pos[1].length; i<wd.pos[4].length;i++)
//                            wd.pos[4][i] = wd.pos[4][wd.pos[4].length-1];
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
              // get final horizontal fit array
		try {
			wd.hfit = pm.getHFitArray();
			wd.pos[5] = pm.getHPos();
                        int hPosLength = pm.getHPos().length;
//                        for (int i=0; i<pm.getPosArray().length; i++)
//                            wd.pos[5][i] = wd.pos[1][i]/sqrt2;
                        for (int i=hPosLength; i<wd.pos[5].length;i++)
                            wd.pos[5][i] = wd.pos[5][hPosLength-1];
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
                // get number of steps
		try {
			wd.nsteps = pm.getNSteps();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
	}

	protected void doPostTable() {
		WireData wd = theDoc.wireDataMap.get(pm.getId());
		dataTableModel.setValueAt(new Double(wd.xareaf), 0, 1);
		dataTableModel.setValueAt(new Double(wd.xaream), 0, 2);
		dataTableModel.setValueAt(new Double(wd.yareaf), 0, 3);
		dataTableModel.setValueAt(new Double(wd.yaream), 0, 4);
		dataTableModel.setValueAt(new Double(wd.zareaf), 0, 5);
		dataTableModel.setValueAt(new Double(wd.zaream), 0, 6);
		dataTableModel.setValueAt(new Double(wd.xamplf), 1, 1);
		dataTableModel.setValueAt(new Double(wd.xamplm), 1, 2);
		dataTableModel.setValueAt(new Double(wd.yamplf), 1, 3);
		dataTableModel.setValueAt(new Double(wd.yamplm), 1, 4);
		dataTableModel.setValueAt(new Double(wd.zamplf), 1, 5);
		dataTableModel.setValueAt(new Double(wd.zamplm), 1, 6);
		dataTableModel.setValueAt(new Double(wd.xmeanf), 2, 1);
		dataTableModel.setValueAt(new Double(wd.xmeanm), 2, 2);
		dataTableModel.setValueAt(new Double(wd.ymeanf), 2, 3);
		dataTableModel.setValueAt(new Double(wd.ymeanm), 2, 4);
		dataTableModel.setValueAt(new Double(wd.zmeanf), 2, 5);
		dataTableModel.setValueAt(new Double(wd.zmeanm), 2, 6);
		dataTableModel.setValueAt(new Double(wd.xsigmaf), 3, 1);
		dataTableModel.setValueAt(new Double(wd.xsigmam), 3, 2);
		dataTableModel.setValueAt(new Double(wd.ysigmaf), 3, 3);
		dataTableModel.setValueAt(new Double(wd.ysigmam), 3, 4);
		dataTableModel.setValueAt(new Double(wd.zsigmaf), 3, 5);
		dataTableModel.setValueAt(new Double(wd.zsigmam), 3, 6);
		dataTableModel.setValueAt(new Double(wd.xoffsetf), 4, 1);
		dataTableModel.setValueAt(new Double(wd.xoffsetm), 4, 2);
		dataTableModel.setValueAt(new Double(wd.yoffsetf), 4, 3);
		dataTableModel.setValueAt(new Double(wd.yoffsetm), 4, 4);
		dataTableModel.setValueAt(new Double(wd.zoffsetf), 4, 5);
		dataTableModel.setValueAt(new Double(wd.zoffsetm), 4, 6);
		dataTableModel.setValueAt(new Double(wd.xslopef), 5, 1);
		dataTableModel.setValueAt(new Double(wd.xslopem), 5, 2);
		dataTableModel.setValueAt(new Double(wd.yslopef), 5, 3);
		dataTableModel.setValueAt(new Double(wd.yslopem), 5, 4);
		dataTableModel.setValueAt(new Double(wd.zslopef), 5, 5);
		dataTableModel.setValueAt(new Double(wd.zslopem), 5, 6);
	}
        
        protected WireWindow getWireWindow() {
            return theWindow;
        }

	protected void getAreas() {
		WireData wd = theDoc.wireDataMap.get(pm.getId());
		try {
			wd.xareaf = pm.getVAreaF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.yareaf = pm.getDAreaF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zareaf = pm.getHAreaF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.xaream = pm.getVAreaM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.yaream = pm.getDAreaM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zaream = pm.getHAreaM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
	}

	protected void getAmpls() {
		WireData wd = theDoc.wireDataMap.get(pm.getId());
		try {
			wd.xamplf = pm.getVAmplF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.yamplf = pm.getDAmplF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zamplf = pm.getHAmplF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.xamplm = pm.getVAmplM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.yamplm = pm.getDAmplM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zamplm = pm.getHAmplM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
	}

	protected void getMeans() {
		WireData wd =  theDoc.wireDataMap.get(pm.getId());
		try {
			wd.xmeanf = pm.getVMeanF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.ymeanf = pm.getDMeanF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zmeanf = pm.getHMeanF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.xmeanm = pm.getVMeanM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.ymeanm = pm.getDMeanM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zmeanm = pm.getHMeanM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
	}

	protected void getSigmas() {
		WireData wd = theDoc.wireDataMap.get(pm.getId());
		try {
			wd.xsigmaf = pm.getVSigmaF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.ysigmaf = pm.getDSigmaF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zsigmaf = pm.getHSigmaF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.xsigmam = pm.getVSigmaM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.ysigmam = pm.getDSigmaM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zsigmam = pm.getHSigmaM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
	}

	protected void getOffsets() {
		WireData wd = theDoc.wireDataMap.get(pm.getId());
		try {
			wd.xoffsetf = pm.getVOffsetF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.yoffsetf = pm.getDOffsetF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zoffsetf = pm.getHOffsetF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.xoffsetm = pm.getVOffsetM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.yoffsetm = pm.getDOffsetM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zoffsetm = pm.getHOffsetM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
	}

	protected void getSlopes() {
		WireData wd = theDoc.wireDataMap.get(pm.getId());
		try {
			wd.xslopef = pm.getVSlopeF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.yslopef = pm.getDSlopeF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zslopef = pm.getHSlopeF();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.xslopem = pm.getVSlopeM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.yslopem = pm.getDSlopeM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
		try {
			wd.zslopem = pm.getHSlopeM();
		}
		catch(ConnectionException ce){
			System.err.println( ce.getMessage() );
			ce.printStackTrace();
		}
		catch(GetException ge){
			System.err.println( ge.getMessage() );
			ge.printStackTrace();
		}
	}
	public void clear() {
	}
}
