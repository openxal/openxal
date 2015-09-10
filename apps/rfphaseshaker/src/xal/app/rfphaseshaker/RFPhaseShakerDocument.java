/*
 *  RFPhaseShakerDocument.java
 *
 *  Created on Feb. 25 2009
 */
package xal.app.rfphaseshaker;

import java.net.*;
import java.io.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import javax.swing.tree.DefaultTreeModel;

import xal.ca.*;
import xal.extension.widgets.plot.*;
import xal.extension.application.*;
import xal.tools.xml.*;
import xal.tools.apputils.*;
import xal.extension.widgets.swing.*;

import xal.extension.application.smf.AcceleratorDocument;

import xal.smf.data.XMLDataManager;
import xal.tools.data.DataAdaptor;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;


/**
 *  RFPhaseShakerDocument is a custom XalDocument for RFPhaseShaker application. The
 *  document manages the data that is displayed in the window.
 *
 *@author     shishlo
 */

public class RFPhaseShakerDocument extends AcceleratorDocument {

	static {
		ChannelFactory.defaultFactory().init();
	}
	
	//Controllers
	private ShakerController shakerController = null;	
	

	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	//the tabbed panel that will keep all subpanels
	private JTabbedPane mainTabbedPanel = new JTabbedPane();

	//--------------------------------------------------------------
	//The first panel with control elements
	//--------------------------------------------------------------
	private JPanel firstPanel = null;

	//--------------------------------------------------------------
	//The second panel with control elements
	//--------------------------------------------------------------
	private JPanel secondPanel = null;

	//-------------------------------------------------------------
	//PREFERENCES_PANEL and GUI elements, actions etc.
	//-------------------------------------------------------------
	private JPanel preferencesPanel = new JPanel();
	private JButton setFont_PrefPanel_Button = new JButton("Set Font Size");
	private JSpinner fontSize_PrefPanel_Spinner = new JSpinner(new SpinnerNumberModel(7, 7, 26, 1));
	private JLabel timeDealyUC_Label = new JLabel("time delay for graphics update [sec]", JLabel.LEFT);

	private Font globalFont = new Font("Monospaced", Font.BOLD, 10);

	//------------------------------------------------
	//PANEL STATE
	//------------------------------------------------
	private int ACTIVE_PANEL = 0;
	private int FIRST_PANEL = 0;
	private int SECOND_PANEL = 1;
	private int PREFERENCES_PANEL = 2;
	//-------------------------------------
	//time and date related member
	//-------------------------------------
	private static DateAndTimeText dateAndTime = new DateAndTimeText();

	//=====================================
	//Controllers of the sub-panels
	//=====================================


	//------------------------------------------
	//SAVE RESTORE PART
	//------------------------------------------
	//root node name
	private String dataRootName = "QUAD_SHAKER";


	/**
	 *  Create a new empty RFPhaseShakerDocument
	 */
	public RFPhaseShakerDocument() {

		//controllers of the panels
		//here they are just JPanels, but in real apps ???
		firstPanel = new JPanel();
		secondPanel = new JPanel();

		mainTabbedPanel.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					JTabbedPane tbp = (JTabbedPane) e.getSource();
					setActivePanel(tbp.getSelectedIndex());
				}
			});

		//make all panels
		makePreferencesPanel();

		mainTabbedPanel.add("Shaker", firstPanel);
		mainTabbedPanel.add("Fitting", secondPanel);
		mainTabbedPanel.add("Preferences", preferencesPanel);

		mainTabbedPanel.setSelectedIndex(0);

		//set up accelerator 
		if(!loadDefaultAccelerator()){
			applySelectedAcceleratorWithDefaultPath("/default/main.xal");
		}
		setupAccelerator(getAccelerator());
	}


	/**
	 *  Create a new document loaded from the URL file
	 *
	 *@param  url  The URL of the file to load into the new document.
	 */
	public RFPhaseShakerDocument(URL url) {
		this();
		if(url == null) {
			return;
		}
		setSource(url);
		readRFPhaseShakerDocument(url);

		//super class method - will show "Save" menu active
		if(url.getProtocol().equals("jar")) {
			return;
		}
		setHasChanges(true);
	}


	/**
	 *  Make a main window by instantiating the RFPhaseShakerWindow window.
	 */
	public void makeMainWindow() {
		mainWindow = new RFPhaseShakerWindow(this);
		//---------------------------------------------------------------
		//this is the place for initializing initial state of main window
		//---------------------------------------------------------------

		//define initial state of the window
		getRFPhaseShakerWindow().setJComponent(mainTabbedPanel);

		//set connections between message texts
		getRFPhaseShakerWindow().getMessageTextField().setDocument(messageTextLocal.getDocument());

		//set all text messages for sub frames
		//shakerController.setMessageText(messageTextLocal);
		
		fontSize_PrefPanel_Spinner.setValue(new Integer(globalFont.getSize()));
		setFontForAll(globalFont);

		//set timer
		JTextField timeTxt_temp = dateAndTime.getNewTimeTextField();
		timeTxt_temp.setHorizontalAlignment(JTextField.CENTER);
		getRFPhaseShakerWindow().addTimeStamp(timeTxt_temp);

		mainWindow.setSize(new Dimension(800, 600));
	}
   	
	/**
	 *  Dispose of RFPhaseShakerDocument resources. This method overrides an empty
	 *  superclass method.
	 */
	public void freeCustomResources() {
		cleanUp();
	}


	/**
	 *  Reads the content of the document from the specified URL.
	 *
	 *@param  url  Description of the Parameter
	 */
	public void readRFPhaseShakerDocument(URL url) {

		//read the document content from the persistent storage

		XmlDataAdaptor readAdp = null;
		readAdp = XmlDataAdaptor.adaptorForUrl(url, false);

		if(readAdp != null) {
			XmlDataAdaptor quadshakerData_Adaptor = (XmlDataAdaptor) readAdp.childAdaptor(dataRootName);
			if(quadshakerData_Adaptor != null) {
				cleanUp();
				setTitle(quadshakerData_Adaptor.stringValue("title"));

				//set font
				XmlDataAdaptor params_font = (XmlDataAdaptor) quadshakerData_Adaptor.childAdaptor("font");
				int font_size = params_font.intValue("size");
				int style = params_font.intValue("style");
				String font_Family = params_font.stringValue("name");
				globalFont = new Font(font_Family, style, font_size);
				fontSize_PrefPanel_Spinner.setValue(new Integer(font_size));
				setFontForAll(globalFont);

				//read accelerator, acc. sequences, and shaker controller
				XmlDataAdaptor acc_seqs_da = (XmlDataAdaptor) quadshakerData_Adaptor.childAdaptor("acc_sequences");
				applySelectedAcceleratorWithDefaultPath(acc_seqs_da.stringValue("acc_path"));
				Accelerator accl = this.getAccelerator();
				Vector<AcceleratorSeq> accSeqV = new Vector<AcceleratorSeq>();
				for(DataAdaptor seq_da: acc_seqs_da.childAdaptors("sequence")){
					String acc_seq_name = seq_da.stringValue("name");
					accSeqV.add(accl.findSequence(acc_seq_name));
				}
				
				shakerController = new ShakerController(accSeqV);
				shakerController.setFontForAll(globalFont);
				shakerController.setMessageText(messageTextLocal);
				
				firstPanel.removeAll();
				firstPanel.setLayout(new BorderLayout());
				firstPanel.add(shakerController.getJPanel(),BorderLayout.CENTER);
				
				//set up shaker controller parameters 
				XmlDataAdaptor params_da = (XmlDataAdaptor) quadshakerData_Adaptor.childAdaptor("shared_parameters");
				shakerController.phaseShiftTextFiled.setValue(params_da.doubleValue("phase_shake_ampl"));
				shakerController.sleepTimeTextFiled.setValue(params_da.doubleValue("sleep_time"));
				shakerController.numbAvgTextFiled.setValue(params_da.doubleValue("nAvg"));
				
				//set up rf nodes according their state in the file
				java.util.HashMap<String,Boolean> rfStateDict = new java.util.HashMap<String,Boolean>();
				XmlDataAdaptor rf_tree_da = (XmlDataAdaptor) quadshakerData_Adaptor.childAdaptor("rf_nodes");
				for(DataAdaptor node_da: rf_tree_da.childAdaptors("node")){
					String rf_name = node_da.stringValue("name");
					Boolean rf_On = new Boolean(node_da.booleanValue("is_on"));
					rfStateDict.put(rf_name,rf_On);
				}	
				
				DevTreeNode rootNode = shakerController.getRFRootNode();
				for(DevTreeNode accSeqNode : rootNode.children){
					boolean isOn = false;
					for(DevTreeNode accNodeNode : accSeqNode.children){
						if(rfStateDict.containsKey(accNodeNode.accNode.getId())){
							if(rfStateDict.get(accNodeNode.accNode.getId()).booleanValue()){
								accNodeNode.isOn = true;
								isOn =true;
							}
						}
					}
					accSeqNode.isOn = isOn;
				}	
				
				//set up bpm nodes according their state in the file
				java.util.HashMap<String,Boolean> bpmStateDict = new java.util.HashMap<String,Boolean>();
				XmlDataAdaptor bpm_tree_da = (XmlDataAdaptor) quadshakerData_Adaptor.childAdaptor("bpm_nodes");
				for(DataAdaptor node_da: bpm_tree_da.childAdaptors("node")){
					String bpm_name = node_da.stringValue("name");
					Boolean bpm_On = new Boolean(node_da.booleanValue("is_on"));
					rfStateDict.put(bpm_name,bpm_On);
				}	
				
				rootNode = shakerController.getBPMRootNode();
				for(DevTreeNode accSeqNode : rootNode.children){
					boolean isOn = false;
					for(DevTreeNode accNodeNode : accSeqNode.children){
						if(rfStateDict.containsKey(accNodeNode.accNode.getId())){
							if(rfStateDict.get(accNodeNode.accNode.getId()).booleanValue()){
								accNodeNode.isOn = true;
								isOn =true;
							}
						}
					}
					accSeqNode.isOn = isOn;
				}	
				
				//set up graphs
				XmlDataAdaptor graph_da = (XmlDataAdaptor) quadshakerData_Adaptor.childAdaptor("theory_graph");	
				BasicGraphData gd =  shakerController.getDesignGraph();
				gd.removeAllPoints();
				for(DataAdaptor gd_da: graph_da.childAdaptors("point")){
					double s = gd_da.doubleValue("s");
					double y = gd_da.doubleValue("y");
					double err = gd_da.doubleValue("err");
					gd.addPoint(s,y,err);
				}
				
				graph_da = (XmlDataAdaptor) quadshakerData_Adaptor.childAdaptor("measurement_graph");	
				gd =  shakerController.getMeasuredGraph();
				gd.removeAllPoints();
				for(DataAdaptor gd_da: graph_da.childAdaptors("point")){
					double s = gd_da.doubleValue("s");
					double y = gd_da.doubleValue("y");
					double err = gd_da.doubleValue("err");
					gd.addPoint(s,y,err);
				}
				
		    shakerController.fGraphPanel.refreshGraphJPanel();
				mainTabbedPanel.validate();
			}
		}
	}


	/**
	 *  Save the RFPhaseShakerDocument document to the specified URL.
	 *
	 *@param  url  Description of the Parameter
	 */
	public void saveDocumentAs(URL url) {
		//this is the place to write document to the persistent storage

		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
		XmlDataAdaptor quadshakerData_Adaptor = (XmlDataAdaptor) da.createChild(dataRootName);
		quadshakerData_Adaptor.setValue("title", url.getFile());
		
		//dump parameters
		XmlDataAdaptor params_font = (XmlDataAdaptor) quadshakerData_Adaptor.createChild("font");
		params_font.setValue("name", globalFont.getFamily());
		params_font.setValue("style", globalFont.getStyle());
		params_font.setValue("size", globalFont.getSize());
		
		//dump acc. seq. names and accelerator file path	
		XmlDataAdaptor acc_seqs_da = (XmlDataAdaptor) quadshakerData_Adaptor.createChild("acc_sequences");
		acc_seqs_da.setValue("acc_path", this.getAcceleratorFilePath());
		Vector<AcceleratorSeq> accSeqV = shakerController.getAccSeqV();
		for(AcceleratorSeq accSeq: accSeqV){
			XmlDataAdaptor seq_da = (XmlDataAdaptor) acc_seqs_da.createChild("sequence");
			seq_da.setValue("name", accSeq.getId());
		}
		
		//common parameters
		XmlDataAdaptor params_da = (XmlDataAdaptor) quadshakerData_Adaptor.createChild("shared_parameters");
		params_da.setValue("phase_shake_ampl", shakerController.phaseShiftTextFiled.getValue());
		params_da.setValue("sleep_time", shakerController.sleepTimeTextFiled.getValue());
		params_da.setValue("nAvg", shakerController.numbAvgTextFiled.getValue());

		//dump RF tree state
		XmlDataAdaptor rf_tree_da = (XmlDataAdaptor) quadshakerData_Adaptor.createChild("rf_nodes");
    DevTreeNode rootNode = shakerController.getRFRootNode();
		for(DevTreeNode accSeqNode : rootNode.children){
			for(DevTreeNode accNodeNode : accSeqNode.children){
				XmlDataAdaptor node_da = (XmlDataAdaptor) rf_tree_da.createChild("node");
				node_da.setValue("name", accNodeNode.accNode.getId());
				node_da.setValue("is_on", accNodeNode.isOn);
			}
		}		
	 
		//dump BPM tree state
		XmlDataAdaptor bpm_tree_da = (XmlDataAdaptor) quadshakerData_Adaptor.createChild("bpm_nodes");
    rootNode = shakerController.getBPMRootNode();
		for(DevTreeNode accSeqNode : rootNode.children){
			for(DevTreeNode accNodeNode : accSeqNode.children){
				XmlDataAdaptor node_da = (XmlDataAdaptor) bpm_tree_da.createChild("node");
				node_da.setValue("name", accNodeNode.accNode.getId());
				node_da.setValue("is_on", accNodeNode.isOn);
			}
		}
		
		//graph data for design
		XmlDataAdaptor graph_da = (XmlDataAdaptor) quadshakerData_Adaptor.createChild("theory_graph");
		BasicGraphData gd =  shakerController.getDesignGraph();
		int nPoints = gd.getNumbOfPoints();
		for(int i = 0; i < nPoints; i++){
			XmlDataAdaptor gd_da = (XmlDataAdaptor) graph_da.createChild("point");
			gd_da.setValue("s", gd.getX(i));
			gd_da.setValue("y", gd.getY(i));
			gd_da.setValue("err", gd.getErr(i));
		}
		
		//graph data for measurement
		graph_da = (XmlDataAdaptor) quadshakerData_Adaptor.createChild("measurement_graph");
		gd =  shakerController.getMeasuredGraph();
		nPoints = gd.getNumbOfPoints();
		for(int i = 0; i < nPoints; i++){
			XmlDataAdaptor gd_da = (XmlDataAdaptor) graph_da.createChild("point");
			gd_da.setValue("s", gd.getX(i));
			gd_da.setValue("y", gd.getY(i));
			gd_da.setValue("err", gd.getErr(i));
		}


		//dump data into the file
		try {
			da.writeToUrl( url );

			//super class method - will show "Save" menu active
			setHasChanges( true );
		}
        catch( XmlDataAdaptor.WriteException exception ) {
			if ( exception.getCause() instanceof java.io.FileNotFoundException ) {
				System.err.println(exception);
				displayError("Save Failed!", "Save failed due to a file access exception!", exception);
			}
			else if ( exception.getCause() instanceof java.io.IOException ) {
				System.err.println(exception);
				displayError("Save Failed!", "Save failed due to a file IO exception!", exception);
			}
			else {
				exception.printStackTrace();
				displayError("Save Failed!", "Save failed due to an internal write exception!", exception);
			}
        }
        catch( Exception exception ) {
			exception.printStackTrace();
            displayError("Save Failed!", "Save failed due to an internal exception!", exception);
        }

	}


	/**
	 *  Edit preferences for the document.
	 */
	void editPreferences() {
		//place for edit preferences
		mainTabbedPanel.setSelectedIndex(PREFERENCES_PANEL);
		setActivePanel(PREFERENCES_PANEL);
	}


	/**
	 *  Convenience method for getting the RFPhaseShakerWindow window. It is the cast
	 *  to the proper subclass of XalWindow. This allows me to avoid casting the
	 *  window every time I reference it.
	 *
	 *@return    The main window cast to its dynamic runtime class
	 */
	private RFPhaseShakerWindow getRFPhaseShakerWindow() {
		return (RFPhaseShakerWindow) mainWindow;
	}


	/**
	 *  Register actions for the menu items and toolbar.
	 *
	 *@param  commander  Description of the Parameter
	 */

	public void customizeCommands(Commander commander) {
	}


	/**
	 *  Description of the Method
	 */
	private void makePreferencesPanel() {

		fontSize_PrefPanel_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

		JPanel tmp_0 = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_0.add(fontSize_PrefPanel_Spinner);
		tmp_0.add(setFont_PrefPanel_Button);

		JPanel tmp_2 = new JPanel(new GridLayout(0, 1));
		tmp_2.add(tmp_0);

		preferencesPanel.setLayout(new BorderLayout());
		preferencesPanel.add(tmp_2, BorderLayout.NORTH);

		setFont_PrefPanel_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int fnt_size = ((Integer) fontSize_PrefPanel_Spinner.getValue()).intValue();
					globalFont = new Font(globalFont.getFamily(), globalFont.getStyle(), fnt_size);
					setFontForAll(globalFont);
					int h = getRFPhaseShakerWindow().getHeight();
					int w = getRFPhaseShakerWindow().getWidth();
					getRFPhaseShakerWindow().validate();
				}
			});

	}


	/**
	 *  Clean up the document content
	 */
	private void cleanUp() {
		cleanMessageTextField();
	}
	
	/**
	* This is to handle the accelerator change event.This method 
	* overrides the AccelaratorDocument parent class method
	*/
	public void acceleratorChanged() {
		Accelerator accl = this.getAccelerator();
		System.out.println("accelerator path: " + acceleratorFilePath);
		Vector<AcceleratorSeq> accSeqV = new Vector<AcceleratorSeq>();
		setupAccelerator(accl);
	}
	
	/**
	* This method sets the new accelerator
	*/	
	private void setupAccelerator(Accelerator accl){
		Vector<AcceleratorSeq> accSeqV = new Vector<AcceleratorSeq>();
		accSeqV.add(accl.findSequence("MEBT"));
		accSeqV.add(accl.findSequence("DTL1"));
		accSeqV.add(accl.findSequence("DTL2"));
		accSeqV.add(accl.findSequence("DTL3"));
		accSeqV.add(accl.findSequence("DTL4"));
		accSeqV.add(accl.findSequence("DTL5"));
		accSeqV.add(accl.findSequence("DTL6"));
		accSeqV.add(accl.findSequence("CCL1"));
		accSeqV.add(accl.findSequence("CCL2"));
		accSeqV.add(accl.findSequence("CCL3"));
		accSeqV.add(accl.findSequence("CCL4"));
		accSeqV.add(accl.findSequence("SCLMed"));
		accSeqV.add(accl.findSequence("SCLHigh"));
		accSeqV.add(accl.findSequence("HEBT1"));
		
		shakerController = new ShakerController(accSeqV);
		shakerController.setFontForAll(globalFont);
		shakerController.setMessageText(messageTextLocal);

		firstPanel.removeAll();
		firstPanel.setLayout(new BorderLayout());
		firstPanel.add(shakerController.getJPanel(),BorderLayout.CENTER);
		mainTabbedPanel.validate();
	}
	
	/**
	 *  Description of the Method
	 */
	private void cleanMessageTextField() {
		messageTextLocal.setText(null);
		messageTextLocal.setForeground(Color.red);
	}

	/**
	 *  Sets the fontForAll attribute of the RFPhaseShakerDocument object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	private void setFontForAll(Font fnt) {
		messageTextLocal.setFont(fnt);
		fontSize_PrefPanel_Spinner.setValue(new Integer(fnt.getSize()));
		setFont_PrefPanel_Button.setFont(fnt);
		fontSize_PrefPanel_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) fontSize_PrefPanel_Spinner.getEditor()).getTextField().setFont(fnt);

		globalFont = fnt;

		//mainTabbedPanel.setFont(fnt);

		//set fonts for sub-controllers
		//using the setFontForAll(fnt) method
		shakerController.setFontForAll(globalFont);

	}


	/**
	 *  Sets the activePanel attribute of the RFPhaseShakerDocument object
	 *
	 *@param  newActPanelInd  The new activePanel value
	 */
	private void setActivePanel(int newActPanelInd) {
		int oldActPanelInd = ACTIVE_PANEL;

		if(oldActPanelInd == newActPanelInd) {
			return;
		}

		//shut up active panel
		if(oldActPanelInd == FIRST_PANEL) {
			//action before first panel will disappear
		} else if(oldActPanelInd == SECOND_PANEL) {
			//action before second panel will disappear
		} else if(oldActPanelInd == PREFERENCES_PANEL) {
			//action before preferences panel will disappear
		}

		//make something before the new panel will show up
		if(newActPanelInd == FIRST_PANEL) {
			//action before view values panel will show up
		} else if(newActPanelInd == SECOND_PANEL) {
			//action before second pane will show up
		} else if(newActPanelInd == PREFERENCES_PANEL) {
			//action before preferences pane will show up
		}

		ACTIVE_PANEL = newActPanelInd;

		cleanMessageTextField();
	}
}

//----------------------------------------------
//Class deals with date and time
//----------------------------------------------
/**
 *  Description of the Class
 *
 *@author     shishlo
 *created    July 8, 2004
 *@version
 */
class DateAndTimeText {


	private SimpleDateFormat dFormat = null;
	private JFormattedTextField dateTimeField = null;


	/**
	 *  Constructor for the DateAndTimeText object
	 */
	public DateAndTimeText() {
		dFormat = new SimpleDateFormat("'Time': MM.dd.yy HH:mm ");
		dateTimeField = new JFormattedTextField(dFormat);
		dateTimeField.setEditable(false);
		Runnable timer =
			new Runnable() {
				public void run() {
					while(true) {
						dateTimeField.setValue(new Date());
						try {
							Thread.sleep(30000);
						} catch(InterruptedException e) {}
					}
				}
			};

		Thread thr = new Thread(timer);
		thr.start();
	}


	/**
	 *  Returns the time attribute of the DateAndTimeText object
	 *
	 *@return    The time value
	 */
	protected String getTime() {
		return dateTimeField.getText();
	}


	/**
	 *  Returns the timeTextField attribute of the DateAndTimeText object
	 *
	 *@return    The timeTextField value
	 */
	protected JFormattedTextField getTimeTextField() {
		return dateTimeField;
	}


	/**
	 *  Returns the newTimeTextField attribute of the DateAndTimeText object
	 *
	 *@return    The newTimeTextField value
	 */
	protected JTextField getNewTimeTextField() {
		JTextField newText = new JTextField();
		newText.setDocument(dateTimeField.getDocument());
		newText.setEditable(false);
		return newText;
	}
}

