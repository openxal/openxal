/*
 *  ScanDocument1D.java
 *
 *  Created on July 31, 2003, 10:25 AM
 */
package xal.app.scan1d;

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
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.application.*;
import xal.tools.xml.*;
import xal.tools.data.DataAdaptor;
import xal.tools.apputils.*;
import xal.tools.apputils.pvselection.*;
import xal.extension.widgets.swing.*;
import xal.extension.scan.*;
import xal.extension.scan.analysis.*;
import xal.extension.application.util.PredefinedConfController;
import xal.extension.application.smf.*;

import xal.service.pvlogger.*;
import xal.tools.database.*;

/**
 *  ScanDocument1D is a custom XalDocument for 1D scan application. The document
 *  manages the data that is displayed in the window.
 *
 *@author    shishlo
 */

public class ScanDocument1D extends AcceleratorDocument {

	static {
		ChannelFactory.defaultFactory().init();
	}

	//------------------------------------------------------
	//actions
	//------------------------------------------------------
	private Action setScanPanelAction = null;
	private Action setPVsChooserPanelAction = null;
	private Action setAnalysisPanelAction = null;
	private Action setPredefConfigAction = null;

	//------------------------------------------------------
	//PVTree and PVsSelector
	//------------------------------------------------------
	private JPanel selectionPVsPanel = new JPanel();

	//root node of the PVTree for PVsSelector
	private PVTreeNode root_Node = new PVTreeNode("ROOT");
	private PVTreeNode rootParameterPV_Node = new PVTreeNode("Parameter PV");
	private PVTreeNode parameterPV_Node = new PVTreeNode("PV Set");
	private PVTreeNode parameterPV_RB_Node = new PVTreeNode("PV Read Back");
	private PVTreeNode rootScanPV_Node = new PVTreeNode("Scan PV");
	private PVTreeNode scanPV_Node = new PVTreeNode("PV Set");
	private PVTreeNode scanPV_RB_Node = new PVTreeNode("PV Read Back");
	private PVTreeNode measuredPVs_Node = new PVTreeNode("Measured PVs");
	private PVTreeNode validationPVs_Node = new PVTreeNode("Validation PVs");

	private String rootParameterPV_Node_Name = "Parameter PV";
	private String rootScanPV_Node_Name = "Scan PV";
	private String measuredPVs_Node_Name = "Measured PVs";
	private String validationPVs_Node_Name = "Validation PVs";

	//PVsSelector by itself
	private PVsSelector pvsSelector = null;

	//PVTree listeners
	private ActionListener switchPVTreeListener = null;
	private ActionListener createDeletePVTreeListener = null;
	private ActionListener renamePVTreeListener = null;

	//---------------------------------------------
	//scan panel
	//---------------------------------------------
	private JPanel scanPanel = new JPanel();
	private JPanel leftScanControlPanel = null;

	//scan controller
	private ScanController1D scanController = new ScanController1D("SCAN CONTROL PANEL");

	//averaging controller
	private AvgController avgCntr = new AvgController();

	//validation controller
	private ValidationController vldCntr = new ValidationController();

	//scan variable
	private ScanVariable scanVariable = null;

	//place for measured and validation MeasuredValue instances
	private Vector<MeasuredValue> measuredValuesV = new Vector<MeasuredValue>();
	private Vector<MeasuredValue> validationValuesV = new Vector<MeasuredValue>();

	//state MeasuredValue and ScanPV and ScanPV_RB  (show or not)
	private Vector<Boolean> measuredValuesShowStateV = new Vector<Boolean>();
	private boolean scanPV_ShowState = false;
	private boolean scanPV_RB_ShowState = false;

	//PVsTreePanel placed on the scan panel
	private PVsTreePanel pvsTreePanelScan = null;

	//---------------------------------------------
	//analysis panel
	//---------------------------------------------
	private JPanel analysisPanel = new JPanel();
	private MainAnalysisController analysisController = null;

	//PVsTreePanel placed on the scan panel
	private PVsTreePanel pvsTreePanelAnalysis = null;

	//------------------------------------------------------------
	//parameter PV controller and panel
	//------------------------------------------------------------
	private ParameterPV_Controller parameterPV_Controller = null;

	private ScanVariable scanVariableParameter = null;

	private JRadioButton parameterPV_Button = new JRadioButton("Use Parameter PV", false);
	private ActionListener parameterPV_Button_Listener = null;
	private boolean paramPV_ON = false;

	//-----------------------------------------------------------
	//graphs panels that are placed in the scan and setPVs panels
	//-----------------------------------------------------------
	private FunctionGraphsJPanel graphScan = new FunctionGraphsJPanel();
	private FunctionGraphsJPanel graphAnalysis = new FunctionGraphsJPanel();

	//message text field. It is actually message text field from
	private JTextField messageTextLocal = null;

	//this is to create unique aliases for MonitoredPV through all program
	private static volatile int monitoredPV_Count = 0;

	//-------------------------------------------------------------
	//PREFERENCES_PANEL and GUI elements, actions etc.
	//-------------------------------------------------------------
	private JPanel preferencesPanel = new JPanel();
	private JButton setFont_PrefPanel_Button = new JButton("Set Font Size");
	private JSpinner fontSize_PrefPanel_Spinner = new JSpinner(new SpinnerNumberModel(7, 7, 26, 1));
	private Font globalFont = null;

	private JCheckBox useTimeStampButton = new JCheckBox("Use Time Stamp On Legend", true);
	private JCheckBox restoreValueAfterScanButton = new JCheckBox("Restore Scan PV value after scan", true);

	//------------------------------------------------
	//PREDEFINED CONFIGURATION PANEL
	//------------------------------------------------
	private PredefinedConfController predefinedConfController = null;
	private JPanel configPanel = null;

	//------------------------------------------------
	//PANEL STATE
	//------------------------------------------------
	private int ACTIVE_PANEL = 0;
	private int SCAN_PANEL = 0;
	private int ANALYSIS_PANEL = 1;
	private int SET_PVs_PANEL = 2;
	private int PREFERENCES_PANEL = 3;
	private int PREDEF_CONF_PANEL = 4;

	//-------------------------------------
	//time and date related member
	//-------------------------------------
	private static DateAndTimeText dateAndTime = new DateAndTimeText();

	//-------------------------------------------------
	//set Accelerator Action related members
	//-------------------------------------------------

	//accelerator data file
	private File acceleratorDataFile = null;

	//------------------------------------------
	//SAVE RESTORE PART
	//------------------------------------------
	//root node name
	private String dataRootName_SR = "Scan1D_Application";

	//child nodes names
	private String paramsName_SR = "app_params";
	private String paramPV_SR = "param_PV";
	private String scanPV_SR = "scan_PV";
	private String measurePVs_SR = "measure_PVs";
	private String validationPVs_SR = "validation_PVs";

	//place to keep analysis configuration file
	private DataAdaptor analysisConfig = null;
	private String analysisConfig_SR = "ANALYSIS_CONFIGURATIONS";

	//PV Logger part
	private JButton makeSnapshotButton = new JButton("Make PV Logger Snapshot");
	private JButton clearSnapshotButton = new JButton("Clear Snapshot");
	private String noSnapshotIdString = "No Snapshot";
	private String snapshotIdString = "Last Snapshot Id: ";
	private JLabel snapshotIdLabel = new JLabel("No Snapshot", JLabel.LEFT);
	private long snapshotId = -1;
	private boolean pvLogged = false;


	/**
	 *  Create a new empty ScanDocument1D
	 */
	public ScanDocument1D() {

		ACTIVE_PANEL = SCAN_PANEL;

		//make PVsSelector and define its structure
		rootParameterPV_Node.setPVNamesAllowed(false);
		parameterPV_Node.setPVNamesAllowed(true);
		parameterPV_RB_Node.setPVNamesAllowed(true);
		rootScanPV_Node.setPVNamesAllowed(false);
		scanPV_Node.setPVNamesAllowed(true);
		scanPV_RB_Node.setPVNamesAllowed(true);
		measuredPVs_Node.setPVNamesAllowed(true);
		validationPVs_Node.setPVNamesAllowed(true);

		parameterPV_Node.setPVNumberLimit(1);
		parameterPV_RB_Node.setPVNumberLimit(1);
		parameterPV_Node.setCheckBoxVisible(false);
		parameterPV_RB_Node.setCheckBoxVisible(false);
		scanPV_Node.setPVNumberLimit(1);
		scanPV_RB_Node.setPVNumberLimit(1);

		rootParameterPV_Node.add(parameterPV_Node);
		rootParameterPV_Node.add(parameterPV_RB_Node);
		rootScanPV_Node.add(scanPV_Node);
		rootScanPV_Node.add(scanPV_RB_Node);

		root_Node.add(rootScanPV_Node);
		root_Node.add(measuredPVs_Node);
		root_Node.add(validationPVs_Node);

		pvsSelector = new PVsSelector(root_Node);
        if ( accelerator != null ) {
            pvsSelector.setAccelerator( accelerator );
        }
		pvsSelector.removeMessageTextField();

		makeTreeListeners();

		scanPV_Node.setSwitchedOnOffListener(switchPVTreeListener);
		scanPV_RB_Node.setSwitchedOnOffListener(switchPVTreeListener);
		measuredPVs_Node.setSwitchedOnOffListener(switchPVTreeListener);
		validationPVs_Node.setSwitchedOnOffListener(switchPVTreeListener);

		parameterPV_Node.setCreateRemoveListener(createDeletePVTreeListener);
		parameterPV_RB_Node.setCreateRemoveListener(createDeletePVTreeListener);
		scanPV_Node.setCreateRemoveListener(createDeletePVTreeListener);
		scanPV_RB_Node.setCreateRemoveListener(createDeletePVTreeListener);
		measuredPVs_Node.setCreateRemoveListener(createDeletePVTreeListener);
		validationPVs_Node.setCreateRemoveListener(createDeletePVTreeListener);

		parameterPV_Node.setRenameListener(renamePVTreeListener);
		parameterPV_RB_Node.setRenameListener(renamePVTreeListener);
		scanPV_Node.setRenameListener(renamePVTreeListener);
		scanPV_RB_Node.setRenameListener(renamePVTreeListener);
		measuredPVs_Node.setRenameListener(renamePVTreeListener);
		validationPVs_Node.setRenameListener(renamePVTreeListener);

		//make Parameter PV related elements
		scanVariableParameter = new ScanVariable("param_var_" + monitoredPV_Count, "param_var_RB_" + (monitoredPV_Count + 1));
		parameterPV_Controller = new ParameterPV_Controller(scanVariableParameter);

		//define actions for switching on and off for Parameter PV
		parameterPV_Button_Listener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (parameterPV_Button.isSelected()) {
						paramPV_ON = true;
						root_Node.insert(rootParameterPV_Node, 0);
						leftScanControlPanel.add(parameterPV_Controller.getJPanel());
						parameterPV_Controller.startMonitor();
						((DefaultTreeModel) pvsSelector.getPVsTreePanel().getJTree().getModel()).reload();
						((DefaultTreeModel) pvsTreePanelScan.getJTree().getModel()).reload();
						((DefaultTreeModel) pvsTreePanelAnalysis.getJTree().getModel()).reload();

					} else {
						paramPV_ON = false;
						root_Node.remove(rootParameterPV_Node);
						leftScanControlPanel.remove(parameterPV_Controller.getJPanel());
						parameterPV_Controller.stopMonitor();
						((DefaultTreeModel) pvsSelector.getPVsTreePanel().getJTree().getModel()).reload();
						((DefaultTreeModel) pvsTreePanelScan.getJTree().getModel()).reload();
						((DefaultTreeModel) pvsTreePanelAnalysis.getJTree().getModel()).reload();
					}
				}
			};
		parameterPV_Button.addActionListener(parameterPV_Button_Listener);

		//make scan panel and members
		scanVariable = new ScanVariable("scan_var_" + monitoredPV_Count, "scan_var_RB_" + (monitoredPV_Count + 1));
		monitoredPV_Count++;
		monitoredPV_Count++;
		scanController.setRestoreButton(restoreValueAfterScanButton);
		scanController.setScanVariable(scanVariable);
		scanController.setAvgController(avgCntr);
		scanController.setValidationController(vldCntr);
		scanController.getUnitsLabel().setText(" [a.u.]");
        scanController.setPhaseScanButtonVisible(true);
		scanController.addNewSetOfDataListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evn) {
					//new set of data
					String paramPV_string = "";
					String scanPV_string = "";
					String scanPV_RB_string = "";
					String measurePV_string = "";
					String legend_string = "";
					String legend_string_RB = "";
					Double paramValue = null;
					Double paramValueRB = null;
					if (paramPV_ON && parameterPV_Controller.getChannel() != null) {
						paramPV_string = paramPV_string + " par.PV : "
								 + parameterPV_Controller.getChannel().getId() + "="
								 + parameterPV_Controller.getValueAsString();
						paramValue = new Double(parameterPV_Controller.getValue());
						if (parameterPV_Controller.getChannelRB() != null) {
							paramValueRB = new Double(parameterPV_Controller.getValueRB());
						}
					}
					if (scanVariable.getChannel() != null) {
						scanPV_string = "xPV=" + scanVariable.getChannel().getId();
					}
					if (scanVariable.getChannelRB() != null) {
						scanPV_RB_string = "xPV=" + scanVariable.getChannelRB().getId();
					}
					for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
						if ( measuredValuesShowStateV.get(i).booleanValue() ) {
							MeasuredValue mv_tmp = measuredValuesV.get(i);
							BasicGraphData gd = mv_tmp.getDataContainer();
							if (mv_tmp.getChannel() != null) {
								measurePV_string = "yPV=" + mv_tmp.getChannel().getId();
							}

							if (useTimeStampButton.isSelected()) {
								legend_string = dateAndTime.getTime();
							} else {
								legend_string = "";
							}
							legend_string_RB = legend_string;
							legend_string = legend_string + " " + scanPV_string + " " + measurePV_string + paramPV_string + " ";
							if (gd != null) {
								gd.setGraphProperty(graphScan.getLegendKeyString(), legend_string);
								if (paramValue != null) {
									gd.setGraphProperty("PARAMETER_VALUE", paramValue);
								}
								if (paramValueRB != null) {
									gd.setGraphProperty("PARAMETER_VALUE_RB", paramValueRB);
								}
							}
							legend_string_RB = legend_string_RB + " " + scanPV_RB_string + " " + measurePV_string + paramPV_string + " ";
							if (scanVariable.getChannelRB() != null) {
								gd = mv_tmp.getDataContainerRB();
								if (gd != null) {
									gd.setGraphProperty(graphScan.getLegendKeyString(), legend_string_RB);
									if (paramValue != null) {
										gd.setGraphProperty("PARAMETER_VALUE", paramValue);
									}
									if (paramValueRB != null) {
										gd.setGraphProperty("PARAMETER_VALUE_RB", paramValueRB);
									}
								}
							}
						}
					}
					updateDataSetOnGraphPanels();
				}
			});

		scanController.addNewPointOfDataListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evn) {
					graphScan.refreshGraphJPanel();
					graphAnalysis.refreshGraphJPanel();
				}
			});

		//define graph panel's properties
		SimpleChartPopupMenu.addPopupMenuTo(graphScan);
		SimpleChartPopupMenu.addPopupMenuTo(graphAnalysis);
		graphScan.setOffScreenImageDrawing(true);
		graphAnalysis.setOffScreenImageDrawing(true);
		graphScan.setName("SCAN : Measured Values vs. Scan PV's Values");
		graphAnalysis.setName("ANALYSIS : Measured Values vs. Scan PV's Values");
		graphScan.setAxisNames("Scan PV Values", "Measured Values");
		graphAnalysis.setAxisNames("Scan PV Values", "Measured Values");
		graphScan.setGraphBackGroundColor(Color.white);
		graphAnalysis.setGraphBackGroundColor(Color.white);

		//place to keep analysis configuration file
		analysisConfig = XmlDataAdaptor.newEmptyDocumentAdaptor().createChild( analysisConfig_SR );
		analysisConfig.createChild("MANAGEMENT");
		analysisConfig.createChild("FIND_MIN_MAX");
		analysisConfig.createChild("POLYNOMIAL_FITTING");
		analysisConfig.createChild("INTERSECTION_FINDING");

		//make all panels
		makeScanPanel();
		makeAnalysisPanel();
		makeSelectionPVsPanel();
		makePreferencesPanel();
		makePredefinedConfigurationsPanel();
	}


	/**
	 *  Create a new document loaded from the URL file
	 *
	 *@param  url  The URL of the file to load into the new document.
	 */
	public ScanDocument1D(URL url) {
		this();
		if (url == null) {
			return;
		}
		setSource(url);
		readScanDocument(url);

		//super class method - will show "Save" menu active
		if (url.getProtocol().equals("jar")) {
			return;
		}
		setHasChanges(true);
	}


	/**
	 *  Reads the content of the document from the specified URL.
	 *
	 *@param  url  Description of the Parameter
	 */
	public void readScanDocument(URL url) {

		//read the document content from the persistent storage

		DataAdaptor readAdp = XmlDataAdaptor.adaptorForUrl( url, false );

		if (readAdp != null) {

			DataAdaptor scan1D_Adaptor = readAdp.childAdaptor(dataRootName_SR);

			DataAdaptor params_scan1D = scan1D_Adaptor.childAdaptor(paramsName_SR);
			DataAdaptor paramPV_scan1D = scan1D_Adaptor.childAdaptor(paramPV_SR);
			DataAdaptor scanPV_scan1D = scan1D_Adaptor.childAdaptor(scanPV_SR);
			DataAdaptor measurePVs_scan1D = scan1D_Adaptor.childAdaptor(measurePVs_SR);
			DataAdaptor validationPVs_scan1D = scan1D_Adaptor.childAdaptor(validationPVs_SR);

			DataAdaptor tmp_analysisConfig = scan1D_Adaptor.childAdaptor(analysisConfig_SR);
			if (tmp_analysisConfig != null) {
				analysisConfig = tmp_analysisConfig;
			} else {
				//default set of analysis
				analysisConfig = XmlDataAdaptor.newEmptyDocumentAdaptor().createChild(analysisConfig_SR);
				analysisConfig.createChild("MANAGEMENT");
				analysisConfig.createChild("FIND_MIN_MAX");
				analysisConfig.createChild("POLYNOMIAL_FITTING");
				analysisConfig.createChild("INTERSECTION_FINDING");
			}

			//set title
			setTitle(scan1D_Adaptor.stringValue("title"));

			//set font
			DataAdaptor params_font = params_scan1D.childAdaptor("font");
			globalFont = new Font(params_font.stringValue("name"), params_font.intValue("style"), params_font.intValue("size"));

			//set scan control panel title
			DataAdaptor params_scan_panel_title = params_scan1D.childAdaptor("scan_panel_title");
			if (params_scan_panel_title != null && params_scan_panel_title.stringValue("title") != null) {
				scanController.setTitle(params_scan_panel_title.stringValue("title"));
			} else {
				scanController.setTitle("SCAN CONTROL PANEL");
			}

			//pv logger Id
			DataAdaptor pv_logger_id = params_scan1D.childAdaptor("pv_logger_id");
			if (pv_logger_id != null && pv_logger_id.intValue("Id") > 0) {
				snapshotId = pv_logger_id.intValue("Id");
				snapshotIdLabel.setText(snapshotIdString + snapshotId + "  ");
				pvLogged = true;
			} else {
				snapshotId = -1;
				pvLogged = false;
				snapshotIdLabel.setText(noSnapshotIdString);
			}

			//set  paramPV_tree_node_name
			DataAdaptor paramPV_tree_node_name = params_scan1D.childAdaptor("parameterPV_tree_name");
			if (paramPV_tree_node_name != null && paramPV_tree_node_name.stringValue("name") != null) {
				rootParameterPV_Node.setName(paramPV_tree_node_name.stringValue("name"));
			} else {
				rootParameterPV_Node.setName(rootParameterPV_Node_Name);
			}

			//set  scanPV_tree_node_name
			DataAdaptor scanPV_tree_node_name = params_scan1D.childAdaptor("scanPV_tree_name");
			if (scanPV_tree_node_name != null && scanPV_tree_node_name.stringValue("name") != null) {
				rootScanPV_Node.setName(scanPV_tree_node_name.stringValue("name"));
			} else {
				rootScanPV_Node.setName(rootScanPV_Node_Name);
			}

			//set  measuredPVs_tree_node_name
			DataAdaptor measuredPVs_tree_node_name = params_scan1D.childAdaptor("measuredPVs_tree_name");
			if (measuredPVs_tree_node_name != null && measuredPVs_tree_node_name.stringValue("name") != null) {
				measuredPVs_Node.setName(measuredPVs_tree_node_name.stringValue("name"));
			} else {
				measuredPVs_Node.setName(measuredPVs_Node_Name);
			}

			//set  validationPVs_tree_node_name
			DataAdaptor validationPVs_tree_node_name = params_scan1D.childAdaptor("validationPVs_tree_name");
			if (validationPVs_tree_node_name != null && validationPVs_tree_node_name.stringValue("name") != null) {
				validationPVs_Node.setName(validationPVs_tree_node_name.stringValue("name"));
			} else {
				validationPVs_Node.setName(validationPVs_Node_Name);
			}

			//set UseTimeStamp parameter
			DataAdaptor params_UseTimeStamp = params_scan1D.childAdaptor("UseTimeStamp");
			if (params_UseTimeStamp != null && params_UseTimeStamp.hasAttribute("yes")) {
				useTimeStampButton.setSelected(params_UseTimeStamp.booleanValue("yes"));
			}

			//set lowLimits uppLimits Step time_delay
			DataAdaptor params_limits = params_scan1D.childAdaptor("limits_step_delay");
			scanController.setLowLimit(params_limits.doubleValue("low"));
			scanController.setUppLimit(params_limits.doubleValue("upp"));
			scanController.setStep(params_limits.doubleValue("step"));
			scanController.setSleepTime(params_limits.doubleValue("delay"));

			//set beam trigger state and time delay
			DataAdaptor params_trigger = params_scan1D.childAdaptor("beam_trigger");
			if (params_trigger != null) {
				//scanController.setBeamTriggerChannelName(params_trigger.stringValue("PV"));
				scanController.setBeamTriggerDelay(params_trigger.doubleValue("delay"));
				scanController.setBeamTriggerState(params_trigger.booleanValue("on"));
			}

			//set averaging parameter
			DataAdaptor params_averg = params_scan1D.childAdaptor("averaging");
			avgCntr.setOnOff(params_averg.booleanValue("on"));
			avgCntr.setAvgNumber(params_averg.intValue("N"));
			avgCntr.setTimeDelay(params_averg.doubleValue("delay"));

			//set validation parameters
			DataAdaptor params_validation = params_scan1D.childAdaptor("validation");
			vldCntr.setOnOff(params_validation.booleanValue("on"));
			vldCntr.setLowLim(params_validation.doubleValue("low"));
			vldCntr.setUppLim(params_validation.doubleValue("upp"));

			//set parameter PV using
			DataAdaptor params_paramPV_name = paramPV_scan1D.childAdaptor("PV");
			if (params_paramPV_name != null) {
				String PV_name = params_paramPV_name.stringValue("name");
				PVTreeNode pvNodeNew = new PVTreeNode(PV_name);
				Channel channel = ChannelFactory.defaultFactory().getChannel(PV_name);
				parameterPV_Controller.setChannel(channel);
				pvNodeNew.setChannel(channel);
				pvNodeNew.setAsPVName(true);
				pvNodeNew.setCheckBoxVisible(parameterPV_Node.isCheckBoxVisible());
				parameterPV_Node.add(pvNodeNew);
				pvNodeNew.setSwitchedOnOffListener(parameterPV_Node.getSwitchedOnOffListener());
				pvNodeNew.setCreateRemoveListener(parameterPV_Node.getCreateRemoveListener());
				pvNodeNew.setRenameListener(parameterPV_Node.getRenameListener());
			}

			DataAdaptor params_paramPV_nameRB = paramPV_scan1D.childAdaptor("PV_RB");
			if (params_paramPV_nameRB != null) {
				String PV_nameRB = params_paramPV_nameRB.stringValue("name");
				PVTreeNode pvNodeNew = new PVTreeNode(PV_nameRB);
				Channel channel = ChannelFactory.defaultFactory().getChannel(PV_nameRB);
				parameterPV_Controller.setChannelRB(channel);
				pvNodeNew.setChannel(channel);
				pvNodeNew.setAsPVName(true);
				pvNodeNew.setCheckBoxVisible(parameterPV_RB_Node.isCheckBoxVisible());
				parameterPV_RB_Node.add(pvNodeNew);
				pvNodeNew.setSwitchedOnOffListener(parameterPV_RB_Node.getSwitchedOnOffListener());
				pvNodeNew.setCreateRemoveListener(parameterPV_RB_Node.getCreateRemoveListener());
				pvNodeNew.setRenameListener(parameterPV_RB_Node.getRenameListener());
			}

			//set Parameter PV panel title
			String paramPV_PanelTitle = paramPV_scan1D.stringValue("panel_title");
			if (paramPV_PanelTitle != null) {
				parameterPV_Controller.setTitle(paramPV_PanelTitle);
			} else {
				parameterPV_Controller.setTitle("PARAMETER PV CONTROL");
			}

			paramPV_ON = paramPV_scan1D.booleanValue("on");
			parameterPV_Button.setSelected(paramPV_ON);
			if (paramPV_ON) {
				ActionEvent parameterPV_ButtonActionEvent = new ActionEvent(parameterPV_Button, 0,
						parameterPV_Button.getActionCommand());
				parameterPV_Button_Listener.actionPerformed(parameterPV_ButtonActionEvent);
			}

			//set scan PVs
			DataAdaptor scan_PV_name_DA =  scanPV_scan1D.childAdaptor("PV");
			if (scan_PV_name_DA != null) {
				String scan_PV_name = scan_PV_name_DA.stringValue("name");
				boolean scan_PV_on = scan_PV_name_DA.booleanValue("on");
				scanPV_ShowState = scan_PV_on;
				PVTreeNode pvNodeNew = new PVTreeNode(scan_PV_name);
				Channel channel = ChannelFactory.defaultFactory().getChannel(scan_PV_name);
				pvNodeNew.setChannel(channel);
				pvNodeNew.setAsPVName(true);
				pvNodeNew.setCheckBoxVisible(scanPV_Node.isCheckBoxVisible());
				scanPV_Node.add(pvNodeNew);
				pvNodeNew.setSwitchedOnOffListener(scanPV_Node.getSwitchedOnOffListener());
				pvNodeNew.setCreateRemoveListener(scanPV_Node.getCreateRemoveListener());
				pvNodeNew.setRenameListener(scanPV_Node.getRenameListener());
				scanVariable.setChannel(channel);
				graphScan.setAxisNames("Scan PV : " + scan_PV_name, "Measured Values");
				graphAnalysis.setAxisNames("Scan PV : " + scan_PV_name, "Measured Values");
				pvNodeNew.setSwitchedOn(scanPV_ShowState);
			}

			DataAdaptor scan_PV_RB_name_DA =  scanPV_scan1D.childAdaptor("PV_RB");
			if (scan_PV_RB_name_DA != null) {
				String scan_PV_RB_name = scan_PV_RB_name_DA.stringValue("name");
				boolean scan_PV_RB_on = scan_PV_RB_name_DA.booleanValue("on");
				scanPV_RB_ShowState = scan_PV_RB_on;
				PVTreeNode pvNodeNew = new PVTreeNode(scan_PV_RB_name);
				Channel channel = ChannelFactory.defaultFactory().getChannel(scan_PV_RB_name);
				pvNodeNew.setChannel(channel);
				pvNodeNew.setAsPVName(true);
				pvNodeNew.setCheckBoxVisible(scanPV_RB_Node.isCheckBoxVisible());
				scanPV_RB_Node.add(pvNodeNew);
				pvNodeNew.setSwitchedOnOffListener(scanPV_RB_Node.getSwitchedOnOffListener());
				pvNodeNew.setCreateRemoveListener(scanPV_RB_Node.getCreateRemoveListener());
				pvNodeNew.setRenameListener(scanPV_RB_Node.getRenameListener());
				scanVariable.setChannelRB(channel);
				pvNodeNew.setSwitchedOn(scanPV_RB_ShowState);
			}

			//set validation PVs
			for ( final DataAdaptor validationPV_DA : validationPVs_scan1D.childAdaptors() ) {
				String name = validationPV_DA.stringValue("name");
				boolean onOff = validationPV_DA.booleanValue("on");
				PVTreeNode pvNodeNew = new PVTreeNode(name);
				Channel channel = ChannelFactory.defaultFactory().getChannel(name);
				pvNodeNew.setChannel(channel);
				pvNodeNew.setAsPVName(true);
				pvNodeNew.setCheckBoxVisible(validationPVs_Node.isCheckBoxVisible());
				validationPVs_Node.add(pvNodeNew);
				pvNodeNew.setSwitchedOn(onOff);
				pvNodeNew.setSwitchedOnOffListener(validationPVs_Node.getSwitchedOnOffListener());
				pvNodeNew.setCreateRemoveListener(validationPVs_Node.getCreateRemoveListener());
				pvNodeNew.setRenameListener(validationPVs_Node.getRenameListener());
				MeasuredValue mv_tmp = new MeasuredValue("validation_pv_" + monitoredPV_Count);
				monitoredPV_Count++;
				mv_tmp.setChannel(pvNodeNew.getChannel());
				validationValuesV.add(mv_tmp);
				if (onOff) {
					scanController.addValidationValue(mv_tmp);
				}
			}

			//set measured PVs and graph's data
			for ( final DataAdaptor measuredPV_DA : measurePVs_scan1D.childAdaptors() ) {
				String name = measuredPV_DA.stringValue("name");
				boolean onOff = measuredPV_DA.booleanValue("on");
				boolean unWrappedData = false;
				if (measuredPV_DA.stringValue("unWrapped") != null) {
					unWrappedData = measuredPV_DA.booleanValue("unWrapped");
				}
				PVTreeNode pvNodeNew = new PVTreeNode(name);
				Channel channel = ChannelFactory.defaultFactory().getChannel(name);
				pvNodeNew.setChannel(channel);
				pvNodeNew.setAsPVName(true);
				pvNodeNew.setCheckBoxVisible(measuredPVs_Node.isCheckBoxVisible());
				measuredPVs_Node.add(pvNodeNew);
				pvNodeNew.setSwitchedOn(onOff);
				pvNodeNew.setSwitchedOnOffListener(measuredPVs_Node.getSwitchedOnOffListener());
				pvNodeNew.setCreateRemoveListener(measuredPVs_Node.getCreateRemoveListener());
				pvNodeNew.setRenameListener(measuredPVs_Node.getRenameListener());
				MeasuredValue mv_tmp = new MeasuredValue("measured_pv_" + monitoredPV_Count);
				mv_tmp.generateUnwrappedData(unWrappedData);
				monitoredPV_Count++;
				mv_tmp.setChannel(pvNodeNew.getChannel());
				measuredValuesShowStateV.add(new Boolean(onOff));
				measuredValuesV.add(mv_tmp);
				if (onOff) {
					scanController.addMeasuredValue(mv_tmp);
				}

				for ( final DataAdaptor data : measuredPV_DA.childAdaptors( "Graph_For_scanPV" ) ) {
					BasicGraphData gd = new BasicGraphData();
					gd.setGraphProperty( "yLabel", name );
					if ( scanVariable != null )  gd.setGraphProperty( "xLabel", scanVariable.getChannelName() );
					mv_tmp.addNewDataConatainer(gd);

					String legend = data.stringValue("legend");

					DataAdaptor paramDataValue = data.childAdaptor("parameter_value");
					if (paramDataValue != null) {
						double parameter_value = paramDataValue.doubleValue("value");
						gd.setGraphProperty("PARAMETER_VALUE", new Double(parameter_value));
					}

					DataAdaptor paramDataValueRB = data.childAdaptor("parameter_value_RB");
					if (paramDataValueRB != null) {
						double parameter_value_RB = paramDataValueRB.doubleValue("value");
						gd.setGraphProperty("PARAMETER_VALUE_RB", new Double(parameter_value_RB));
					}

					gd.setGraphProperty(graphScan.getLegendKeyString(), legend);
					for ( final DataAdaptor xyerr : data.childAdaptors("XYErr") ) {
						gd.addPoint( xyerr.doubleValue("x"), xyerr.doubleValue("y"), xyerr.doubleValue("err") );
					}

				}

				for ( final DataAdaptor data : measuredPV_DA.childAdaptors("Graph_For_scanPV_RB") ) {
					String legend = data.stringValue("legend");
					BasicGraphData gd = new BasicGraphData();
					gd.setGraphProperty( "yLabel", name );
					if ( scanVariable != null )  gd.setGraphProperty( "xLabel", scanVariable.getChannelNameRB() );
					mv_tmp.addNewDataConatainerRB(gd);
					if (gd != null) {
						gd.setGraphProperty(graphScan.getLegendKeyString(), legend);
						for ( final DataAdaptor xyerr : data.childAdaptors("XYErr") ) {
							gd.addPoint( xyerr.doubleValue("x"), xyerr.doubleValue("y"), xyerr.doubleValue("err") );
						}
					}
				}

			}

			//create the child analysis panels
			analysisController.createChildAnalysis(analysisConfig);

			setColors(-1);
			updateDataSetOnGraphPanels();
		}
	}



	/**
	 *  Make a main window by instantiating the ScanWindow1D window.
	 */
	public void makeMainWindow() {
		mainWindow = new ScanWindow1D(this);
		//---------------------------------------------------------------
		//this is the place for initializing initial state of main window
		//---------------------------------------------------------------

		//define initial state of the window
		getScanWindow().setJComponent(scanPanel);

		//set connections between message texts
		messageTextLocal = getScanWindow().getMessageTextField();
		scanController.getMessageText().setDocument(messageTextLocal.getDocument());
		pvsSelector.getMessageJTextField().setDocument(messageTextLocal.getDocument());
		analysisController.setMessageTextField(messageTextLocal);
		parameterPV_Controller.setMessageTextField(messageTextLocal);
		if (globalFont == null) {
			globalFont = new Font("Monospaced", Font.BOLD, 10);
		}
		fontSize_PrefPanel_Spinner.setValue(new Integer(globalFont.getSize()));
		setFontForAll(globalFont);

		//set connections for  message text in selection of config. panel
		predefinedConfController.setMessageTextField(getScanWindow().getMessageTextField());

		//set timer
		JToolBar toolbar = getScanWindow().getToolBar();
		JTextField timeTxt_temp = dateAndTime.getNewTimeTextField();
		timeTxt_temp.setHorizontalAlignment(JTextField.CENTER);
		toolbar.add(timeTxt_temp);

		mainWindow.setSize(new Dimension(700, 600));
	}


	/**
	 *  Dispose of ScanDocument1D resources. This method overrides an empty
	 *  superclass method.
	 */
	public void freeCustomResources() {
		cleanUp();
	}


	/**
	 *  Save the ScanDocument1D document to the specified URL.
	 *
	 *@param  url  Description of the Parameter
	 */
	@SuppressWarnings( "unchecked" )		// cast needed for Enumeration since PVTreeNode does not support generic types
	public void saveDocumentAs(URL url) {
		//this is the place to write document to the persistent storage

		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
		DataAdaptor scan1D_Adaptor =  da.createChild(dataRootName_SR);
		//scan1D_Adaptor.setValue("title",getTitle());
		scan1D_Adaptor.setValue("title", url.getFile());

		DataAdaptor params_scan1D =  scan1D_Adaptor.createChild(paramsName_SR);
		DataAdaptor paramPV_scan1D =  scan1D_Adaptor.createChild(paramPV_SR);
		DataAdaptor scanPV_scan1D =  scan1D_Adaptor.createChild(scanPV_SR);
		DataAdaptor validationPVs_scan1D =  scan1D_Adaptor.createChild(validationPVs_SR);
		analysisConfig =  scan1D_Adaptor.createChild(analysisConfig_SR);
		DataAdaptor measurePVs_scan1D =  scan1D_Adaptor.createChild(measurePVs_SR);

		//make analysis configuration part of the DataAdaptor
		analysisController.dumpChildAnalysisConfig(analysisConfig);

		//dump parameters
		DataAdaptor params_font =  params_scan1D.createChild("font");
		params_font.setValue("name", globalFont.getFamily());
		params_font.setValue("style", globalFont.getStyle());
		params_font.setValue("size", globalFont.getSize());

		//dump scan control panel title
		DataAdaptor params_scan_panel_title =  params_scan1D.createChild("scan_panel_title");
		params_scan_panel_title.setValue("title", scanController.getTitle());

		//dump PV Logger Id
		DataAdaptor pv_logger_id =  params_scan1D.createChild("pv_logger_id");
		pv_logger_id.setValue("Id", (int) snapshotId);

		//dump names of the tree panel items
		DataAdaptor paramPV_tree_node_name =  params_scan1D.createChild("parameterPV_tree_name");
		paramPV_tree_node_name.setValue("name", rootParameterPV_Node.getName());
		DataAdaptor scanPV_tree_node_name =  params_scan1D.createChild("scanPV_tree_name");
		scanPV_tree_node_name.setValue("name", rootScanPV_Node.getName());
		DataAdaptor measuredPVs_tree_node_name =  params_scan1D.createChild("measuredPVs_tree_name");
		measuredPVs_tree_node_name.setValue("name", measuredPVs_Node.getName());
		DataAdaptor validationPVs_tree_node_name =  params_scan1D.createChild("validationPVs_tree_name");
		validationPVs_tree_node_name.setValue("name", validationPVs_Node.getName());

		DataAdaptor params_UseTimeStamp =  params_scan1D.createChild("UseTimeStamp");
		params_UseTimeStamp.setValue("yes", useTimeStampButton.isSelected());

		//dump lowLimits uppLimits Step time_delay
		DataAdaptor params_limits =  params_scan1D.createChild("limits_step_delay");
		params_limits.setValue("low", scanController.getLowLimit());
		params_limits.setValue("upp", scanController.getUppLimit());
		params_limits.setValue("step", scanController.getStep());
		params_limits.setValue("delay", scanController.getSleepTime());

		//dump beam trigger state and time delay
		DataAdaptor params_trigger =  params_scan1D.createChild("beam_trigger");
		params_trigger.setValue("on", scanController.getBeamTriggerState());
		params_trigger.setValue("delay", scanController.getBeamTriggerDelay());
		//params_trigger.setValue("PV",scanController.getBeamTriggerChannelName());

		//average controller
		DataAdaptor params_averg =  params_scan1D.createChild("averaging");
		params_averg.setValue("on", avgCntr.isOn());
		params_averg.setValue("N", avgCntr.getAvgNumber());
		params_averg.setValue("delay", avgCntr.getTimeDelay());

		//validation control
		DataAdaptor params_validation =  params_scan1D.createChild("validation");
		params_validation.setValue("on", vldCntr.isOn());
		params_validation.setValue("low", vldCntr.getInnerLowLim());
		params_validation.setValue("upp", vldCntr.getInnerUppLim());

		//using parameter PV
		paramPV_scan1D.setValue("on", paramPV_ON);
		paramPV_scan1D.setValue("panel_title", parameterPV_Controller.getTitle());
		if (scanVariableParameter.getChannel() != null) {
			DataAdaptor params_paramPV_name =  paramPV_scan1D.createChild("PV");
			params_paramPV_name.setValue("name", scanVariableParameter.getChannelName());
		}
		if (scanVariableParameter.getChannelRB() != null) {
			DataAdaptor params_paramPV_nameRB =  paramPV_scan1D.createChild("PV_RB");
			params_paramPV_nameRB.setValue("name", scanVariableParameter.getChannelNameRB());
		}

		//dump scan PVs
		if (scanVariable.getChannel() != null) {
			DataAdaptor scan_PV_name =  scanPV_scan1D.createChild("PV");
			scan_PV_name.setValue("name", scanVariable.getChannelName());
			scan_PV_name.setValue("on", scanPV_ShowState);
		}
		if (scanVariable.getChannelRB() != null) {
			DataAdaptor scan_PV_RB_name =  scanPV_scan1D.createChild("PV_RB");
			scan_PV_RB_name.setValue("name", scanVariable.getChannelNameRB());
			scan_PV_RB_name.setValue("on", scanPV_RB_ShowState);
		}

		//dump validation variables and their state
		Enumeration<PVTreeNode> validation_children = validationPVs_Node.children();
		while (validation_children.hasMoreElements()) {
			PVTreeNode pvNode = validation_children.nextElement();
			DataAdaptor validationPV_node =  validationPVs_scan1D.createChild("Validation_PV");
			validationPV_node.setValue("name", pvNode.getChannel().channelName());
			validationPV_node.setValue("on", pvNode.isSwitchedOn());
		}

		//dump measured PVs and graph's data
		for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
			MeasuredValue mv_tmp = measuredValuesV.get(i);
			DataAdaptor measuredPV_DA =  measurePVs_scan1D.createChild("MeasuredPV");
			measuredPV_DA.setValue("name", mv_tmp.getChannel().channelName());
			measuredPV_DA.setValue("on", measuredValuesShowStateV.get(i).booleanValue() );
			measuredPV_DA.setValue("unWrapped", new Boolean(mv_tmp.generateUnwrappedDataOn()));
			Vector<BasicGraphData> dataV = mv_tmp.getDataContainers();
			for (int j = 0, nd = dataV.size(); j < nd; j++) {
				BasicGraphData gd = dataV.get(j);
				if (gd.getNumbOfPoints() > 0) {
					DataAdaptor graph_DA =  measuredPV_DA.createChild("Graph_For_scanPV");
					graph_DA.setValue("legend", (String) gd.getGraphProperty(graphScan.getLegendKeyString()));

					Double paramValue = (Double) gd.getGraphProperty("PARAMETER_VALUE");
					if (paramValue != null) {
						DataAdaptor paramDataValue =  graph_DA.createChild("parameter_value");
						paramDataValue.setValue("value", paramValue.doubleValue());
					}

					Double paramValueRB = (Double) gd.getGraphProperty("PARAMETER_VALUE_RB");
					if (paramValueRB != null) {
						DataAdaptor paramDataValueRB =  graph_DA.createChild("parameter_value_RB");
						paramDataValueRB.setValue("value", paramValueRB.doubleValue());
					}

					for (int k = 0, np = gd.getNumbOfPoints(); k < np; k++) {
						DataAdaptor point_DA =  graph_DA.createChild("XYErr");
						point_DA.setValue("x", gd.getX(k));
						point_DA.setValue("y", gd.getY(k));
						point_DA.setValue("err", gd.getErr(k));
					}
				}
			}

			dataV = mv_tmp.getDataContainersRB();
			for (int j = 0, nd = dataV.size(); j < nd; j++) {
				BasicGraphData gd = dataV.get(j);
				if (gd.getNumbOfPoints() > 0) {
					DataAdaptor graph_DA =  measuredPV_DA.createChild("Graph_For_scanPV_RB");
					graph_DA.setValue("legend", (String) gd.getGraphProperty(graphScan.getLegendKeyString()));
					for (int k = 0, np = gd.getNumbOfPoints(); k < np; k++) {
						DataAdaptor point_DA =  graph_DA.createChild("XYErr");
						point_DA.setValue("x", gd.getX(k));
						point_DA.setValue("y", gd.getY(k));
						point_DA.setValue("err", gd.getErr(k));
					}
				}
			}
		}

		//dump data into the file
		try {
			da.writeToUrl( url );

			//super class method - will show "Save" menu active
			setHasChanges(true);
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
		if (!scanController.isScanON()) {
			if (ACTIVE_PANEL == ANALYSIS_PANEL) {
				analysisController.isGoingShutUp();
				updateDataSetOnGraphPanels();
			}
			getScanWindow().setJComponent(preferencesPanel);
			cleanMessageTextField();
			ACTIVE_PANEL = PREFERENCES_PANEL;
		} else {
			Toolkit.getDefaultToolkit().beep();
		}

	}


	/**
	 *  Convenience method for getting the ScanWindow1D window. It is the cast to
	 *  the proper subclass of XalWindow. This allows me to avoid casting the
	 *  window every time I reference it.
	 *
	 *@return    The main window cast to its dynamic runtime class
	 */
	private ScanWindow1D getScanWindow() {
		return (ScanWindow1D) mainWindow;
	}


	/**
	 *  Register actions for the menu items and toolbar.
	 *
	 *@param  commander  Description of the Parameter
	 */

	public void customizeCommands(Commander commander) {

		// define the "show-scan-panel" setScanPanelAction action
		setScanPanelAction =
			new AbstractAction("show-scan-panel") {
				private static final long serialVersionUID = 0L;
				public void actionPerformed(ActionEvent event) {
					if (!scanController.isScanON()) {
						if (ACTIVE_PANEL == ANALYSIS_PANEL) {
							analysisController.isGoingShutUp();
							updateDataSetOnGraphPanels();
						}
						getScanWindow().setJComponent(scanPanel);
						cleanMessageTextField();
						ACTIVE_PANEL = SCAN_PANEL;
					} else {
						Toolkit.getDefaultToolkit().beep();
					}
				}
			};
		commander.registerAction(setScanPanelAction);

		// define the "show-pvs-panel" setScanPanelAction action
		setPVsChooserPanelAction =
			new AbstractAction("show-pvs-panel") {
				private static final long serialVersionUID = 0L;
				public void actionPerformed(ActionEvent event) {
					if (!scanController.isScanON()) {
						if (ACTIVE_PANEL == ANALYSIS_PANEL) {
							analysisController.isGoingShutUp();
							updateDataSetOnGraphPanels();
						}
						getScanWindow().setJComponent(selectionPVsPanel);
						cleanMessageTextField();
						ACTIVE_PANEL = SET_PVs_PANEL;
					} else {
						Toolkit.getDefaultToolkit().beep();
					}
				}
			};
		commander.registerAction(setPVsChooserPanelAction);

		// define the "show-analysis-panel" setScanPanelAction action
		setAnalysisPanelAction =
			new AbstractAction("show-analysis-panel") {
				private static final long serialVersionUID = 0L;
				public void actionPerformed(ActionEvent event) {
					if (!scanController.isScanON()) {
						analysisController.isGoingShowUp();
						getScanWindow().setJComponent(analysisPanel);
						cleanMessageTextField();
						ACTIVE_PANEL = ANALYSIS_PANEL;
					} else {
						Toolkit.getDefaultToolkit().beep();
					}
				}
			};
		commander.registerAction(setAnalysisPanelAction);

		setPredefConfigAction =
			new AbstractAction("set-predef-config") {
				private static final long serialVersionUID = 0L;
				public void actionPerformed(ActionEvent event) {
					if (!scanController.isScanON()) {
						getScanWindow().setJComponent(configPanel);
						cleanMessageTextField();
						ACTIVE_PANEL = PREDEF_CONF_PANEL;
					} else {
						Toolkit.getDefaultToolkit().beep();
					}
				}
			};

		commander.registerAction(setPredefConfigAction);

	}


	/**
	 *  Description of the Method
	 */
	private void makeScanPanel() {
		pvsTreePanelScan = pvsSelector.getNewPVsTreePanel();
		pvsTreePanelScan.getJTree().setBackground(Color.white);

		pvsTreePanelScan.setPreferredSize(new Dimension(0, 0));
		pvsSelector.setPreferredSize(new Dimension(0, 0));

		scanPanel.setLayout(new BorderLayout());

		JPanel tmp_0 = new JPanel();
		tmp_0.setLayout(new VerticalLayout());
		tmp_0.add(scanController.getJPanel());
		tmp_0.add(avgCntr.getJPanel(0));
		tmp_0.add(vldCntr.getJPanel(0));

		leftScanControlPanel = tmp_0;

		JPanel tmp_1 = new JPanel();
		tmp_1.setLayout(new BorderLayout());
		tmp_1.add(tmp_0, BorderLayout.NORTH);
		tmp_1.add(pvsTreePanelScan, BorderLayout.CENTER);

		Border etchedBorder = BorderFactory.createEtchedBorder();

		JPanel tmp_2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
		tmp_2.add(makeSnapshotButton);
		tmp_2.add(snapshotIdLabel);
		tmp_2.add(clearSnapshotButton);
		tmp_2.setBorder(etchedBorder);

		JPanel tmp_3 = new JPanel();
		tmp_3.setLayout(new BorderLayout());
		tmp_3.setBorder(etchedBorder);
		tmp_3.add(tmp_2, BorderLayout.NORTH);
		tmp_3.add(graphScan, BorderLayout.CENTER);

		scanPanel.add(tmp_1, BorderLayout.WEST);
		scanPanel.add(tmp_3, BorderLayout.CENTER);

		//make pv logger snapshot
		makeSnapshotButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					RemoteLoggingCenter rL = new RemoteLoggingCenter();
					Date startTime = new Date();
					String comments = startTime.toString();
					comments = comments + " = Scan1D =";
					snapshotId = rL.takeAndPublishSnapshot( "default", comments);
					if(snapshotId > 0){
						pvLogged = true;
						snapshotIdLabel.setText(snapshotIdString + snapshotId + "  ");
					} else {
						pvLogged = false;
						snapshotIdLabel.setText("Unsuccessful PV Logging");
					}
				}
			});

		clearSnapshotButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					snapshotId = -1;
					pvLogged = false;
					snapshotIdLabel.setText(noSnapshotIdString);
				}
			});

	}


	/**
	 *  Description of the Method
	 */
	private void makeAnalysisPanel() {

		pvsTreePanelAnalysis = pvsSelector.getNewPVsTreePanel();
		pvsTreePanelAnalysis.setPreferredSize(new Dimension(0, 0));
		pvsTreePanelAnalysis.getJTree().setBackground(Color.white);

		analysisPanel.setLayout(new BorderLayout());

		//this panel is analysis control panel and should be sent to the
		//constructor of analysis class
		JPanel tmp_0 = new JPanel();

		JPanel tmp_1 = new JPanel();
		tmp_1.setLayout(new BorderLayout());
		tmp_1.add(tmp_0, BorderLayout.NORTH);
		tmp_1.add(pvsTreePanelAnalysis, BorderLayout.CENTER);

		Border etchedBorder = BorderFactory.createEtchedBorder();

		JPanel tmp_2 = new JPanel();
		tmp_2.setLayout(new BorderLayout());
		tmp_2.setBorder(etchedBorder);

		analysisPanel.add(tmp_1, BorderLayout.WEST);
		analysisPanel.add(tmp_2, BorderLayout.CENTER);

		//make analysis controller
		analysisController = new MainAnalysisController(this,
				analysisPanel,
				tmp_0,
				tmp_2,
				scanVariableParameter,
				scanVariable,
				measuredValuesV,
				measuredValuesShowStateV,
				graphScan,
				graphAnalysis);

		//create the child analysis panels
		analysisController.createChildAnalysis(analysisConfig);

	}


	/**
	 *  Description of the Method
	 */
	private void makeSelectionPVsPanel() {
		selectionPVsPanel.setLayout(new BorderLayout());
		selectionPVsPanel.add(pvsSelector, BorderLayout.CENTER);

		JPanel tmp_0 = new JPanel();
		tmp_0.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_0.add(parameterPV_Button);
		tmp_0.setBorder(BorderFactory.createEtchedBorder());

		selectionPVsPanel.add(tmp_0, BorderLayout.NORTH);
		parameterPV_Button.setHorizontalAlignment(JRadioButton.LEFT);
	}


	/**
	 *  Description of the Method
	 */
	private void makePreferencesPanel() {

		fontSize_PrefPanel_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

		preferencesPanel.setLayout(new BorderLayout());

		JPanel tmp_0 = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_0.add(fontSize_PrefPanel_Spinner);
		tmp_0.add(setFont_PrefPanel_Button);

		JPanel tmp_1 = new JPanel(new GridLayout(3, 1));
		tmp_1.add(tmp_0);
		tmp_1.add(useTimeStampButton);
		tmp_1.add(restoreValueAfterScanButton);

		preferencesPanel.add(tmp_1, BorderLayout.NORTH);

		setFont_PrefPanel_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int fnt_size = ((Integer) fontSize_PrefPanel_Spinner.getValue()).intValue();
					globalFont = new Font(globalFont.getFamily(), globalFont.getStyle(), fnt_size);
					setFontForAll(globalFont);
				}
			});
	}


	/**
	 *  Description of the Method
	 */
	private void makePredefinedConfigurationsPanel() {
		predefinedConfController = new PredefinedConfController( "config", "predefinedConfiguration.scan1D" );
		configPanel = predefinedConfController.getJPanel();
		ActionListener selectConfListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					URL url = (URL) e.getSource();

					if (url == null) {
						Toolkit.getDefaultToolkit().beep();
						messageTextLocal.setText(null);
						messageTextLocal.setText("Cannot find an input configuration file!");
					}

					cleanUp();
					readScanDocument(url);

					//super class method - will show "Save" menu unactive
					setHasChanges(false);

					setFontForAll(globalFont);
					if (ACTIVE_PANEL == ANALYSIS_PANEL) {
						analysisController.isGoingShutUp();
						updateDataSetOnGraphPanels();
					}
					getScanWindow().setJComponent(scanPanel);
					cleanMessageTextField();
					ACTIVE_PANEL = SCAN_PANEL;
				}
			};
		predefinedConfController.setSelectorListener(selectConfListener);
	}


	/**
	 *  Description of the Method
	 */
	private void makeTreeListeners() {

		//listener for switch ON and OFF
		switchPVTreeListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String command = e.getActionCommand();
					PVTreeNode pvn = (PVTreeNode) e.getSource();
					boolean switchOnLocal = command.equals(PVTreeNode.SWITCHED_ON_COMMAND);
					PVTreeNode pvn_parent = (PVTreeNode) pvn.getParent();
					int index = -1;
					if (switchOnLocal) {
						if (pvn_parent == scanPV_Node) {
							//System.out.println("debug switch on Scan PV");
							scanPV_ShowState = true;
							updateDataSetOnGraphPanels();
						}
						if (pvn_parent == scanPV_RB_Node) {
							//System.out.println("debug switch on Scan PV_RB");
							scanPV_RB_ShowState = true;
							updateDataSetOnGraphPanels();
						}
						if (pvn_parent == measuredPVs_Node) {
							index = pvn_parent.getIndex(pvn);
							//System.out.println("debug switch on Measured PV index="+index);
							measuredValuesShowStateV.set(index, new Boolean(true));
							MeasuredValue mv_tmp = measuredValuesV.get(index);
							scanController.removeMeasuredValue(mv_tmp);
							scanController.addMeasuredValue(mv_tmp);
							updateDataSetOnGraphPanels();
						}
						if (pvn_parent == validationPVs_Node) {
							index = pvn_parent.getIndex(pvn);
							//System.out.println("debug switch on Validation PV index="+index );
							scanController.removeValidationValue( validationValuesV.get(index) );
							scanController.addValidationValue( validationValuesV.get(index) );
						}
					} else {
						if (pvn_parent == scanPV_Node) {
							//System.out.println("debug switch off Scan PV");
							scanPV_ShowState = false;
							updateDataSetOnGraphPanels();
						}
						if (pvn_parent == scanPV_RB_Node) {
							//System.out.println("debug switch off Scan PV_RB");
							scanPV_RB_ShowState = false;
							updateDataSetOnGraphPanels();
						}
						if (pvn_parent == measuredPVs_Node) {
							index = pvn_parent.getIndex(pvn);
							//System.out.println("debug switch off Measured PV index="+index );
							measuredValuesShowStateV.set(index, new Boolean(false));
							MeasuredValue mv_tmp = measuredValuesV.get(index);
							scanController.removeMeasuredValue(mv_tmp);
							updateDataSetOnGraphPanels();
						}
						if (pvn_parent == validationPVs_Node) {
							index = pvn_parent.getIndex(pvn);
							//System.out.println("debug switch off Validation PV index="+index );
							scanController.removeValidationValue( validationValuesV.get(index) );
						}
					}
				}
			};

		//listener deleting or create PV
		createDeletePVTreeListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PVTreeNode pvn = (PVTreeNode) e.getSource();
					PVTreeNode pvn_parent = (PVTreeNode) pvn.getParent();
					String command = e.getActionCommand();
					boolean bool_removePV = command.equals(PVTreeNode.REMOVE_PV_COMMAND);
					int index = -1;
					if (bool_removePV) {
						if (pvn_parent == parameterPV_Node) {
							//System.out.println("debug delete Parameter PV");
							parameterPV_Controller.setChannel(null);
						}
						if (pvn_parent == parameterPV_RB_Node) {
							//System.out.println("debug delete Parameter PV Read Back");
							parameterPV_Controller.setChannelRB(null);
						}
						if (pvn_parent == scanPV_Node) {
							//System.out.println("debug delete Scan PV");
							scanVariable.setChannel(null);
							scanPV_ShowState = false;
							graphScan.setAxisNames("Scan PV Values", "Measured Values");
							graphAnalysis.setAxisNames("Scan PV Values", "Measured Values");
							for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
								MeasuredValue mv_tmp = measuredValuesV.get(i);
								mv_tmp.removeAllDataContainersNonRB();
							}
							updateDataSetOnGraphPanels();
						}
						if (pvn_parent == scanPV_RB_Node) {
							//System.out.println("debug delete Scan PV_RB");
							scanVariable.setChannelRB(null);
							scanPV_RB_ShowState = false;
							for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
								MeasuredValue mv_tmp = measuredValuesV.get(i);
								mv_tmp.removeAllDataContainersRB();
							}
							updateDataSetOnGraphPanels();
						}
						if (pvn_parent == measuredPVs_Node) {
							index = pvn_parent.getIndex(pvn);
							//System.out.println("debug delete Measured PV index="+index );
							MeasuredValue mv_tmp = measuredValuesV.get(index);
							scanController.removeMeasuredValue(mv_tmp);
							MonitoredPV mpv_tmp = mv_tmp.getMonitoredPV();
							MonitoredPV.removeMonitoredPV(mpv_tmp);
							measuredValuesV.remove(index);
							measuredValuesShowStateV.remove(index);
							updateDataSetOnGraphPanels();
							setColors(index);
						}
						if (pvn_parent == validationPVs_Node) {
							index = pvn_parent.getIndex(pvn);
							//System.out.println("debug delete Validation PV index="+index );
							MeasuredValue mv_tmp = validationValuesV.get(index);
							scanController.removeValidationValue(mv_tmp);
							MonitoredPV mpv_tmp = mv_tmp.getMonitoredPV();
							MonitoredPV.removeMonitoredPV(mpv_tmp);
							validationValuesV.remove(index);
						}
					} else {
						if (pvn_parent == parameterPV_Node) {
							//System.out.println("debug create Parameter PV");
							parameterPV_Controller.setChannel(pvn.getChannel());
						}
						if (pvn_parent == parameterPV_RB_Node) {
							//System.out.println("debug create Parameter PV Read Back");
							parameterPV_Controller.setChannelRB(pvn.getChannel());
						}
						if (pvn_parent == scanPV_Node) {
							//System.out.println("debug create Scan PV");
							scanVariable.setChannel(pvn.getChannel());
							scanPV_ShowState = true;
							graphScan.setAxisNames("Scan PV : " + pvn.getChannel().getId(), "Measured Values");
							graphAnalysis.setAxisNames("Scan PV : " + pvn.getChannel().getId(), "Measured Values");
							updateDataSetOnGraphPanels();
						}
						if (pvn_parent == scanPV_RB_Node) {
							//System.out.println("debug create Scan PV_RB");
							scanVariable.setChannelRB(pvn.getChannel());
							scanPV_RB_ShowState = true;
							updateDataSetOnGraphPanels();
						}
						if (pvn_parent == measuredPVs_Node) {
							index = pvn_parent.getIndex(pvn);
							//System.out.println("debug create Measured PV index="+index );
							MeasuredValue mv_tmp = new MeasuredValue("measured_pv_" + monitoredPV_Count);
							monitoredPV_Count++;
							mv_tmp.setChannel(pvn.getChannel());
							measuredValuesV.add(mv_tmp);
							measuredValuesShowStateV.add(new Boolean(true));
							scanController.addMeasuredValue(mv_tmp);
							setColors(-1);
						}
						if (pvn_parent == validationPVs_Node) {
							index = pvn_parent.getIndex(pvn);
							//System.out.println("debug create Validation PV index="+index );
							MeasuredValue mv_tmp = new MeasuredValue("measured_pv_" + monitoredPV_Count);
							monitoredPV_Count++;
							mv_tmp.setChannel(pvn.getChannel());
							validationValuesV.add(mv_tmp);
							scanController.addValidationValue(mv_tmp);
						}
					}
				}
			};

		//listener rename PV
		renamePVTreeListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PVTreeNode pvn = (PVTreeNode) e.getSource();
					PVTreeNode pvn_parent = (PVTreeNode) pvn.getParent();
					int index = -1;
					if (pvn_parent == parameterPV_Node) {
						//System.out.println("debug rename Parameter PV");
						parameterPV_Controller.setChannel(pvn.getChannel());
					}
					if (pvn_parent == parameterPV_RB_Node) {
						//System.out.println("debug rename Parameter PV Read Back");
						parameterPV_Controller.setChannelRB(pvn.getChannel());
					}
					if (pvn_parent == scanPV_Node) {
						//System.out.println("debug rename Scan PV");
						scanVariable.setChannel(pvn.getChannel());
						graphScan.setAxisNames("Scan PV : " + pvn.getChannel().getId(), "Measured Values");
						graphAnalysis.setAxisNames("Scan PV : " + pvn.getChannel().getId(), "Measured Values");
						graphScan.refreshGraphJPanel();
						graphAnalysis.refreshGraphJPanel();
					}
					if (pvn_parent == scanPV_RB_Node) {
						//System.out.println("debug rename Scan PV_RB");
						scanVariable.setChannelRB(pvn.getChannel());
					}
					if (pvn_parent == measuredPVs_Node) {
						index = pvn_parent.getIndex(pvn);
						//System.out.println("debug rename Measured PV index="+index );
						MeasuredValue mv_tmp = measuredValuesV.get(index);
						MonitoredPV mpv_tmp = mv_tmp.getMonitoredPV();
						mpv_tmp.setChannel(pvn.getChannel());

					}
					if (pvn_parent == validationPVs_Node) {
						index = pvn_parent.getIndex(pvn);
						//System.out.println("debug rename  Validation PV index="+index );
						MeasuredValue mv_tmp = validationValuesV.get(index);
						MonitoredPV mpv_tmp = mv_tmp.getMonitoredPV();
						mpv_tmp.setChannel(pvn.getChannel());
					}
				}
			};
	}


	/**
	 *  Description of the Method
	 */
	private void updateDataSetOnGraphPanels() {
		graphScan.removeAllGraphData();
		for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
			if ( measuredValuesShowStateV.get(i).booleanValue() ) {
				MeasuredValue mv_tmp = measuredValuesV.get(i);
				if (scanPV_ShowState || scanVariable.getChannel() == null) {
					graphScan.addGraphData(mv_tmp.getDataContainers());
				}
				if (scanPV_RB_ShowState) {
					graphScan.addGraphData(mv_tmp.getDataContainersRB());
				}
			}
		}

		//update analysis panel
		analysisController.setScanPVandScanPV_RB_State(scanPV_ShowState, scanPV_RB_ShowState);
		analysisController.updateDataSetOnGraphPanel();
	}


	//if deleteIndex < 0 then nothing to delete
	/**
	 *  Sets the colors attribute of the ScanDocument1D object
	 *
	 *@param  deleteIndex  The new colors value
	 */
	@SuppressWarnings( "unchecked" )		// cast needed for Enumeration since PVTreeNode does not support generic types
	private void setColors(int deleteIndex) {
		for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
			MeasuredValue mv_tmp = measuredValuesV.get(i);
			mv_tmp.setColor(IncrementalColor.getColor(i));
		}
		graphScan.refreshGraphJPanel();
		graphAnalysis.refreshGraphJPanel();

		Enumeration<PVTreeNode> enumNode = measuredPVs_Node.children();
		int i = 0;
		int count = 0;
		while (enumNode.hasMoreElements()) {
			PVTreeNode pvn = enumNode.nextElement();
			if (count != deleteIndex) {
				pvn.setColor(IncrementalColor.getColor(i));
				i++;
			}
			count++;
		}
	}


	//clean up
	/**
	 *  Description of the Method
	 */
	private void cleanUp() {
		MonitoredPV mpv_tmp = scanVariable.getMonitoredPV();
		MonitoredPV.removeMonitoredPV(mpv_tmp);
		mpv_tmp = scanVariable.getMonitoredPV_RB();
		MonitoredPV.removeMonitoredPV(mpv_tmp);
		scanVariable.setChannel(null);
		scanVariable.setChannelRB(null);

		mpv_tmp = scanVariableParameter.getMonitoredPV();
		MonitoredPV.removeMonitoredPV(mpv_tmp);
		mpv_tmp = scanVariableParameter.getMonitoredPV_RB();
		MonitoredPV.removeMonitoredPV(mpv_tmp);
		scanVariableParameter.setChannel(null);
		scanVariableParameter.setChannelRB(null);

		for (int i = 0, n = measuredValuesV.size(); i < n; i++) {
			MeasuredValue mv_tmp = measuredValuesV.get(i);
			mpv_tmp = mv_tmp.getMonitoredPV();
			MonitoredPV.removeMonitoredPV(mpv_tmp);
		}
		for (int i = 0, n = validationValuesV.size(); i < n; i++) {
			MeasuredValue mv_tmp = validationValuesV.get(i);
			mpv_tmp = mv_tmp.getMonitoredPV();
			MonitoredPV.removeMonitoredPV(mpv_tmp);
		}
		measuredValuesV.clear();
		validationValuesV.clear();
		scanController.removeAllValidationValues();
		measuredValuesShowStateV.clear();
		scanPV_ShowState = false;
		scanPV_RB_ShowState = false;
		graphScan.removeAllGraphData();
		graphAnalysis.removeAllGraphData();
		scanPV_Node.removeAllChildren();
		scanPV_RB_Node.removeAllChildren();
		measuredPVs_Node.removeAllChildren();
		validationPVs_Node.removeAllChildren();

		parameterPV_Node.removeAllChildren();
		parameterPV_RB_Node.removeAllChildren();
		if (root_Node.isNodeChild(rootParameterPV_Node)) {
			root_Node.remove(rootParameterPV_Node);
		}
		paramPV_ON = false;
		parameterPV_Button.setSelected(false);
		leftScanControlPanel.remove(parameterPV_Controller.getJPanel());

		((DefaultTreeModel) pvsSelector.getPVsTreePanel().getJTree().getModel()).reload();
		((DefaultTreeModel) pvsTreePanelScan.getJTree().getModel()).reload();
		((DefaultTreeModel) pvsTreePanelAnalysis.getJTree().getModel()).reload();

	}


	/**
	 *  Description of the Method
	 */
	private void cleanMessageTextField() {
		messageTextLocal.setText(null);
		messageTextLocal.setForeground(Color.red);
	}


	/**
	 *  Sets the fontForAll attribute of the ScanDocument1D object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	private void setFontForAll(Font fnt) {
		scanController.setFontForAll(fnt);
		messageTextLocal.setFont(fnt);
		pvsSelector.setAllFonts(fnt);
		pvsTreePanelScan.setAllFonts(fnt);
		pvsTreePanelAnalysis.setAllFonts(fnt);
		parameterPV_Controller.setFontsForAll(fnt);
		parameterPV_Button.setFont(fnt);
		globalFont = fnt;
		fontSize_PrefPanel_Spinner.setValue(new Integer(globalFont.getSize()));
		predefinedConfController.setFontsForAll(fnt);
		makeSnapshotButton.setFont(fnt);
		clearSnapshotButton.setFont(fnt);
		snapshotIdLabel.setFont(fnt);
	}
    
    
    //attempt to make an accelerator based application
    public void acceleratorChanged() {
        if (accelerator != null) {
            if ( pvsSelector != null ) {
                pvsSelector.setAccelerator( accelerator );
            }
            
            setHasChanges(true);
        }
        
    }



	//--------------------------------------------------------
	//This class deals with additional Parameter PV panel
	//and logic
	//--------------------------------------------------------
	/**
	 *  Description of the Class
	 *
	 *@author    shishlo
	 */
	private class ParameterPV_Controller {

		private ScanVariable scanVariableParameter = null;
		private JTextField messageTextParamCntrl = new JTextField(10);

		//-------------------------------------------------
		//GUI elements
		//-------------------------------------------------
		private JPanel paramPV_Panel = new JPanel();

		private JButton memorizeButton = new JButton("MEMORIZE");
		private JButton restoreButton = new JButton("RESTORE");

		private JLabel paramPV_Label = new JLabel(" PV Set:");
		private JLabel paramPV_RB_Label = new JLabel(" Read Back:");

		private double memValue = 0.;

		private DoubleInputTextField paramPV_ValueText = new DoubleInputTextField(8);
		private DoubleInputTextField paramPV_RB_ValueText = new DoubleInputTextField(8);

		private DecimalFormat valueFormat = new DecimalFormat("###.###");

		private JButton readButton = new JButton("READ FROM EPICS");

		private TitledBorder border = null;


		/**
		 *  Constructor for the ParameterPV_Controller object
		 *
		 *@param  scanVariableParameter_In  Description of the Parameter
		 */
		protected ParameterPV_Controller(ScanVariable scanVariableParameter_In) {

			scanVariableParameter = scanVariableParameter_In;

			setButtonState(true, false);

			paramPV_RB_ValueText.setEditable(false);
			paramPV_ValueText.setNumberFormat(valueFormat);
			paramPV_RB_ValueText.setNumberFormat(valueFormat);

			paramPV_ValueText.setHorizontalAlignment(JTextField.CENTER);
			paramPV_RB_ValueText.setHorizontalAlignment(JTextField.CENTER);

			paramPV_ValueText.removeInnerFocusListener();
			paramPV_RB_ValueText.removeInnerFocusListener();

			Border etchedBorder = BorderFactory.createEtchedBorder();
			border = BorderFactory.createTitledBorder(etchedBorder, "PARAMETER PV CONTROL");
			paramPV_Panel.setBorder(border);
			paramPV_Panel.setBackground(paramPV_Panel.getBackground().darker());

			//action definition
			paramPV_ValueText.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						scanVariableParameter.setValue(paramPV_ValueText.getValue());
					}
				});

			memorizeButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						memValue = scanVariableParameter.getValue();
						setButtonState(false, true);
					}
				});

			restoreButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						scanVariableParameter.setValue(memValue);
						setButtonState(true, false);
					}
				});

			readButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (scanVariableParameter.getChannel() != null) {
							paramPV_ValueText.setValueQuietly(scanVariableParameter.getValue());
						} else {
							paramPV_ValueText.setText(null);
							paramPV_ValueText.setBackground(Color.white);
						}
						if (scanVariableParameter.getChannelRB() != null) {
							paramPV_RB_ValueText.setValue(scanVariableParameter.getValueRB());
						} else {
							paramPV_RB_ValueText.setText(null);
							paramPV_RB_ValueText.setBackground(Color.white);
						}
					}
				});

			//composition of the panel
			JPanel tmp_0 = new JPanel();
			tmp_0.setLayout(new GridLayout(1, 2, 1, 1));
			tmp_0.add(memorizeButton);
			tmp_0.add(restoreButton);

			JPanel tmp_1 = new JPanel();
			tmp_1.setLayout(new GridLayout(2, 2, 1, 1));
			tmp_1.add(paramPV_Label);
			tmp_1.add(paramPV_ValueText);
			tmp_1.add(paramPV_RB_Label);
			tmp_1.add(paramPV_RB_ValueText);

			JPanel tmp_2 = new JPanel();
			tmp_2.setLayout(new BorderLayout());
			tmp_2.add(tmp_0, BorderLayout.NORTH);
			tmp_2.add(tmp_1, BorderLayout.CENTER);
			tmp_2.add(readButton, BorderLayout.SOUTH);

			paramPV_Panel.setLayout(new BorderLayout());
			paramPV_Panel.add(tmp_2, BorderLayout.NORTH);

		}


		/**
		 *  Sets the messageTextField attribute of the ParameterPV_Controller object
		 *
		 *@param  messageTextParamCntrl  The new messageTextField value
		 */
		protected void setMessageTextField(JTextField messageTextParamCntrl) {
			this.messageTextParamCntrl = messageTextParamCntrl;
			scanVariableParameter.setMessageTextField(messageTextParamCntrl);
		}


		/**
		 *  Sets the fonts for all objects
		 *
		 *@param  fnt  The new fontsForAll value
		 */
		protected void setFontsForAll(Font fnt) {
			memorizeButton.setFont(fnt);
			restoreButton.setFont(fnt);
			paramPV_Label.setFont(fnt);
			paramPV_RB_Label.setFont(fnt);
			paramPV_ValueText.setFont(fnt);
			paramPV_RB_ValueText.setFont(fnt);
			readButton.setFont(fnt);
			border.setTitleFont(fnt);
			analysisController.setFontsForAll(fnt);
		}


		/**
		 *  Gets the jPanel attribute of the ParameterPV_Controller object
		 *
		 *@return    The jPanel value
		 */
		protected JPanel getJPanel() {
			return paramPV_Panel;
		}


		/**
		 *  Gets the format attribute of the ParameterPV_Controller object
		 *
		 *@return    The format value
		 */
		protected DecimalFormat getFormat() {
			return valueFormat;
		}


		/**
		 *  Gets the valueAsString attribute of the ParameterPV_Controller object
		 *
		 *@return    The valueAsString value
		 */
		protected String getValueAsString() {
			return valueFormat.format(scanVariableParameter.getValue());
		}


		/**
		 *  Gets the value attribute of the ParameterPV_Controller object
		 *
		 *@return    The value value
		 */
		protected double getValue() {
			return scanVariableParameter.getValue();
		}


		/**
		 *  Gets the valueRB attribute of the ParameterPV_Controller object
		 *
		 *@return    The valueRB value
		 */
		protected double getValueRB() {
			return scanVariableParameter.getValueRB();
		}


		/**
		 *  Gets the title attribute of the ParameterPV_Controller object
		 *
		 *@return    The title value
		 */
		protected String getTitle() {
			return border.getTitle();
		}


		/**
		 *  Sets the title attribute of the ParameterPV_Controller object
		 *
		 *@param  title  The new title value
		 */
		protected void setTitle(String title) {
			border.setTitle(title);
		}


		/**
		 *  Sets the channel attribute of the ParameterPV_Controller object
		 *
		 *@param  ch  The new channel value
		 */
		protected void setChannel(Channel ch) {
			scanVariableParameter.setChannel(ch);
			setButtonState(true, false);
			memValue = 0.;
		}


		/**
		 *  Sets the channelRB attribute of the ParameterPV_Controller object
		 *
		 *@param  ch  The new channelRB value
		 */
		protected void setChannelRB(Channel ch) {
			scanVariableParameter.setChannelRB(ch);
		}


		/**
		 *  Gets the channel attribute of the ParameterPV_Controller object
		 *
		 *@return    The channel value
		 */
		protected Channel getChannel() {
			return scanVariableParameter.getChannel();
		}


		/**
		 *  Gets the channelRB attribute of the ParameterPV_Controller object
		 *
		 *@return    The channelRB value
		 */
		protected Channel getChannelRB() {
			return scanVariableParameter.getChannelRB();
		}


		/**
		 *  Description of the Method
		 */
		protected void stopMonitor() {
			scanVariableParameter.getMonitoredPV().stopMonitor();
			scanVariableParameter.getMonitoredPV_RB().stopMonitor();
		}


		/**
		 *  Description of the Method
		 */
		protected void startMonitor() {
			scanVariableParameter.getMonitoredPV().startMonitor();
			scanVariableParameter.getMonitoredPV_RB().startMonitor();
		}


		/**
		 *  Sets the buttonState attribute of the ParameterPV_Controller object
		 *
		 *@param  memButtonState      The new buttonState value
		 *@param  restoreButtonState  The new buttonState value
		 */
		private void setButtonState(boolean memButtonState, boolean restoreButtonState) {
			memorizeButton.setEnabled(memButtonState);
			restoreButton.setEnabled(restoreButtonState);
			if (memButtonState) {
				memorizeButton.setBackground(Color.red);
			} else {
				memorizeButton.setBackground(Color.lightGray);
			}
			if (restoreButtonState) {
				restoreButton.setBackground(Color.red);
			} else {
				restoreButton.setBackground(Color.lightGray);
			}
		}

	}
}

//----------------------------------------------
//Class deals with date and time
//----------------------------------------------
/**
 *  Description of the Class
 *
 *@author    shishlo
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
					while (true) {
						dateTimeField.setValue(new Date());
						try {
							Thread.sleep(30000);
						} catch (InterruptedException e) {}
					}
				}
			};

		Thread thr = new Thread(timer);
		thr.start();
	}


	/**
	 *  Gets the time attribute of the DateAndTimeText object
	 *
	 *@return    The time value
	 */
	protected String getTime() {
		return dateTimeField.getText();
	}


	/**
	 *  Gets the timeTextField attribute of the DateAndTimeText object
	 *
	 *@return    The timeTextField value
	 */
	protected JFormattedTextField getTimeTextField() {
		return dateTimeField;
	}


	/**
	 *  Gets the newTimeTextField attribute of the DateAndTimeText object
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

