/*
 *  ArrayPVViewerDocument.java
 *
 *  Created on July 31, 2003, 10:25 AM
 */
package xal.app.arraypvviewer;

import xal.extension.application.Commander;
import xal.extension.application.XalDocument;
import xal.extension.application.util.PredefinedConfController;
import xal.app.arraypvviewer.*;
import xal.ca.ChannelFactory;
import xal.tools.apputils.VerticalLayout;
import xal.tools.apputils.pvselection.PVTreeNode;
import xal.tools.apputils.pvselection.PVsSelector;
import xal.tools.apputils.pvselection.PVsTreePanel;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.plot.IncrementalColors;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.data.DataAdaptor;
import xal.extension.application.smf.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeModel;

/**
 *  ArrayPVViewerDocument is a custom XalDocument for Array PVs Viewer
 *  application. The document manages the data that is displayed in the window.
 *
 *@author     shishlo
 *@version    1.0
 */

public class ArrayPVViewerDocument extends AcceleratorDocument {

	static {
		ChannelFactory.defaultFactory().init();
	}

	//message text field. It is actually message text field from
	private JTextField messageTextLocal = new JTextField();

	//accelerator data file
	private File acceleratorDataFile = null;

	//Updating controller
	UpdatingController updatingController = new UpdatingController();

	//------------------------------------------------------
	//actions
	//------------------------------------------------------
	private Action setViewlAction = null;
	private Action setPVsAction = null;
	private Action setPredefConfigAction = null;

	//---------------------------------------------
	//view panel
	//---------------------------------------------
	private final JPanel viewPanel = new JPanel();

	private final JLabel viewPanelTitle_Label =
			new JLabel("============UPDATING MANAGEMENT============", JLabel.CENTER);

	private final JRadioButton autoUpdateView_Button = new JRadioButton("Auto Update", true);

	private final JSpinner freq_ViewPanel_Spinner = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
	private final JLabel viewPanelFreq_Label =
			new JLabel("x0.1 Update f[Hz]", JLabel.LEFT);

	//PVsTreePanel placed on the view panel
	private PVsTreePanel pvsTreePanelView = null;

	//ArrayViewerPV vectors
	private final Vector<ArrayViewerPV> arrayPVs = new Vector<ArrayViewerPV>();

	private final FunctionGraphsJPanel arrayPVGraphs = new FunctionGraphsJPanel();

	//---------------------------------------------
	//set PVs panel
	//---------------------------------------------
	private final JPanel setPVsPanel = new JPanel();
	private ValuesGraphPanel arrayPVsGraphPanel = null;

	private ArrayPVsTable pvsTable = null;

	//root node of the PVTree for PVsSelector
	private final String root_Name = "ROOT";
	private final String rootArrayPV_Name = "Array PVs";

	private PVTreeNode root_Node = null;
	private PVTreeNode rootArrayPV_Node = null;

	//PVsSelector by itself
	private PVsSelector pvsSelector = null;

	//PVTree listeners
	private ActionListener switchPVTreeListener = null;
	private ActionListener createDeletePVTreeListener = null;
	private ActionListener renamePVTreeListener = null;

	//-------------------------------------------------------------
	//PREFERENCES_PANEL and GUI elements, actions etc.
	//-------------------------------------------------------------
	private final JPanel preferencesPanel = new JPanel();
	private final JButton setFont_PrefPanel_Button = new JButton("Set Font Size");
	private final JSpinner fontSize_PrefPanel_Spinner = new JSpinner(new SpinnerNumberModel(7, 7, 26, 1));
	private Font globalFont = new Font("Monospaced", Font.BOLD, 10);

	//------------------------------------------------
	//PREDEFINED CONFIGURATION PANEL
	//------------------------------------------------
	private PredefinedConfController predefinedConfController = null;
	private JPanel configPanel = null;

	//------------------------------------------------
	//PANEL STATE
	//------------------------------------------------
	private int ACTIVE_PANEL = 0;
	private final int VIEW_PANEL = 0;
	private final int SET_PVS_PANEL = 1;
	private final int PREFERENCES_PANEL = 2;
	private final int PREDEF_CONF_PANEL = 3;

	//-------------------------------------
	//time and date related member
	//-------------------------------------
	private static DateAndTimeText dateAndTime = new DateAndTimeText();

	//------------------------------------------
	//SAVE RESTORE PART
	//------------------------------------------
	//root node name
	private final String dataRootName = "ARRAY_PV_VIEWER";


	/**
	 *  Create a new empty ArrayPVViewerDocument
	 */
	public ArrayPVViewerDocument() {

		ACTIVE_PANEL = VIEW_PANEL;

		double freq = 10.0;
		updatingController.setUpdateFrequency(freq);
		freq_ViewPanel_Spinner.setValue(new Integer((int) freq));

		updatingController.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (ACTIVE_PANEL == VIEW_PANEL) {
						if (autoUpdateView_Button.isSelected()) {
							for (int i = 0; i < arrayPVs.size(); i++) {
								(arrayPVs.get(i)).update();
							}
							updateGraphPanel();
						}
					}
				}
			});

		//make all panels
		pvsTable = new ArrayPVsTable(arrayPVs);
		pvsTable.addChangeListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					updateGraphPanel();
				}
			});

		makePreferencesPanel();
		makePredefinedConfigurationsPanel();
		makePVsSelectionPanel();
		makeViewPanel();
		AvgAndSigmaCalculator.setValuesGraphPanel(arrayPVsGraphPanel);


	}


	/**
	 *  Create a new document loaded from the URL file
	 *
	 *@param  url  The URL of the file to load into the new document.
	 */
	public ArrayPVViewerDocument(URL url) {
		this();
		if (url == null) {
			return;
		}
		setSource(url);
		readArrayPVViewerDocument(url);

		//super class method - will show "Save" menu active
		if (url.getProtocol().equals("jar")) {
			return;
		}
		setHasChanges(true);
	}


	/**
	 *  Make a main window by instantiating the ArrayPVViewerWindow window.
	 */
	@Override
    public void makeMainWindow() {
		mainWindow = new ArrayPVViewerWindow(this);
		//---------------------------------------------------------------
		//this is the place for initializing initial state of main window
		//---------------------------------------------------------------

		//define initial state of the window
		getArrayPVViewerWindow().setJComponent(viewPanel);

		//set connections between message texts
		messageTextLocal = getArrayPVViewerWindow().getMessageTextField();

		//set all text messages for sub frames
		//???
		pvsSelector.getMessageJTextField().setDocument(messageTextLocal.getDocument());

		fontSize_PrefPanel_Spinner.setValue(new Integer(globalFont.getSize()));
		setFontForAll(globalFont);

		//set connections for  message text in selection of config. panel
		predefinedConfController.setMessageTextField(getArrayPVViewerWindow().getMessageTextField());

		//set timer
		JToolBar toolbar = getArrayPVViewerWindow().getToolBar();
		JTextField timeTxt_temp = dateAndTime.getNewTimeTextField();
		timeTxt_temp.setHorizontalAlignment(JTextField.CENTER);
		toolbar.add(timeTxt_temp);

		mainWindow.setSize(new Dimension(700, 600));
	}


	/**
	 *  Dispose of ArrayPVViewerDocument resources. This method overrides an empty
	 *  superclass method.
	 */
	@Override
    public void freeCustomResources() {
		cleanUp();
	}


	/**
	 *  Reads the content of the document from the specified URL.
	 *
	 *@param  url  Description of the Parameter
	 */
	public void readArrayPVViewerDocument(URL url) {

		//read the document content from the persistent storage

		XmlDataAdaptor readAdp = null;
		readAdp = XmlDataAdaptor.adaptorForUrl(url, false);

		if (readAdp != null) {
			DataAdaptor arrViewerData_Adaptor = readAdp.childAdaptor(dataRootName);
			if (arrViewerData_Adaptor != null) {
				cleanUp();
				setTitle(arrViewerData_Adaptor.stringValue("title"));

				//set font
				DataAdaptor params_font = arrViewerData_Adaptor.childAdaptor("font");
				int font_size = params_font.intValue("size");
				int style = params_font.intValue("style");
				String font_Family = params_font.stringValue("name");
				globalFont = new Font(font_Family, style, font_size);
				fontSize_PrefPanel_Spinner.setValue(new Integer(font_size));
				setFontForAll(globalFont);

				//get the information about updating
				DataAdaptor params_DA = arrViewerData_Adaptor.childAdaptor("PARAMS");
				boolean autoUpdateOn = params_DA.booleanValue("AutoUpdate");
				int frequency = params_DA.intValue("Frequency");
				freq_ViewPanel_Spinner.setValue(new Integer(frequency));

				//temporary to calm down all updating during creations of PV nodes
				autoUpdateView_Button.setSelected(false);

				//set graph panels
				XmlDataAdaptor xPosPanelDA = (XmlDataAdaptor) arrViewerData_Adaptor.childAdaptor("ARRAY_PVS_PANEL");
				arrayPVsGraphPanel.setConfig(xPosPanelDA);

				//----------------------------
				//create the tree
				//----------------------------
				DataAdaptor arrayPVsDA =  arrViewerData_Adaptor.childAdaptor("ARRAY_PVs");

				//create ArrayViewerPV vectors
                for (final DataAdaptor g_DA : arrayPVsDA.childAdaptors()) {
					ArrayViewerPV arrPV = new ArrayViewerPV(arrayPVGraphs);
					arrPV.setConfig(g_DA);
					arrayPVs.add(arrPV);
					updatingController.addArrayDataPV(arrPV.getArrayDataPV());
				}

				//copy structure from ArrayViewerPV vectors to the tree
				for (int i = 0, n = arrayPVs.size(); i < n; i++) {
					ArrayViewerPV arrPV = arrayPVs.get(i);
					PVTreeNode pvNodeNew = new PVTreeNode(arrPV.getChannelName());
					pvNodeNew.setChannel(arrPV.getChannel());
					pvNodeNew.setAsPVName(true);
					pvNodeNew.setCheckBoxVisible(true);
					rootArrayPV_Node.add(pvNodeNew);
					pvNodeNew.setSwitchedOn(arrPV.getArrayDataPV().getSwitchOn());
					pvNodeNew.setSwitchedOnOffListener(switchPVTreeListener);
					pvNodeNew.setCreateRemoveListener(createDeletePVTreeListener);
					pvNodeNew.setRenameListener(renamePVTreeListener);
				}

				((DefaultTreeModel) pvsSelector.getPVsTreePanel().getJTree().getModel()).reload();
				((DefaultTreeModel) pvsTreePanelView.getJTree().getModel()).reload();

				setColors(rootArrayPV_Node, -1);

				updateGraphPanel();

				//permanent definition of auto update
				autoUpdateView_Button.setSelected(autoUpdateOn);

			}
		}
	}


	/**
	 *  Save the ArrayPVViewerDocument document to the specified URL.
	 *
	 *@param  url  Description of the Parameter
	 */
	@Override
    public void saveDocumentAs(URL url) {
		//this is the place to write document to the persistent storage

		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
		XmlDataAdaptor arrViewerData_Adaptor = (XmlDataAdaptor) da.createChild(dataRootName);
		arrViewerData_Adaptor.setValue("title", url.getFile());

		//dump parameters
		DataAdaptor params_font = arrViewerData_Adaptor.createChild("font");
		params_font.setValue("name", globalFont.getFamily());
		params_font.setValue("style", globalFont.getStyle());
		params_font.setValue("size", globalFont.getSize());

		DataAdaptor params_DA = arrViewerData_Adaptor.createChild("PARAMS");
		params_DA.setValue("AutoUpdate", autoUpdateView_Button.isSelected());
		params_DA.setValue("Frequency", ((Integer) freq_ViewPanel_Spinner.getValue()).intValue());

		//dump graph panels states
		XmlDataAdaptor xPosPanelDA = (XmlDataAdaptor) arrViewerData_Adaptor.createChild("ARRAY_PVS_PANEL");
		arrayPVsGraphPanel.dumpConfig(xPosPanelDA);

		//dump graph data and PVs
		DataAdaptor arrayPVsDA = arrViewerData_Adaptor.createChild("ARRAY_PVs");
		for (int i = 0, n = arrayPVs.size(); i < n; i++) {
			ArrayViewerPV arrViewer = arrayPVs.get(i);
			arrViewer.dumpConfig(arrayPVsDA);
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
		setActivePanel(PREFERENCES_PANEL);
	}


	/**
	 *  Convenience method for getting the ArrayPVViewerWindow window. It is the
	 *  cast to the proper subclass of XalWindow. This allows me to avoid casting
	 *  the window every time I reference it.
	 *
	 *@return    The main window cast to its dynamic runtime class
	 */
	private ArrayPVViewerWindow getArrayPVViewerWindow() {
		return (ArrayPVViewerWindow) mainWindow;
	}


	/**
	 *  Register actions for the menu items and toolbar.
	 *
	 *@param  commander  Description of the Parameter
	 */

	@Override
    public void customizeCommands(Commander commander) {

		// define the "show-view-panel" set raw emittance panel action action
		setViewlAction =
			new AbstractAction("show-view-panel") {
                /** serialization ID */
                private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent event) {
					setActivePanel(VIEW_PANEL);
				}
			};
		commander.registerAction(setViewlAction);

		// define the "show-set-pvs-panel" set PVs panel appearance action
		setPVsAction =
			new AbstractAction("show-set-pvs-panel") {
                /** serialization ID */
                private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent event) {
					setActivePanel(SET_PVS_PANEL);
				}
			};
		commander.registerAction(setPVsAction);


		setPredefConfigAction =
			new AbstractAction("set-predef-config") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent event) {
					setActivePanel(PREDEF_CONF_PANEL);
				}
			};
		commander.registerAction(setPredefConfigAction);

	}


	/**
	 *  Description of the Method
	 */
	private void makePreferencesPanel() {

		fontSize_PrefPanel_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

		preferencesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		preferencesPanel.add(fontSize_PrefPanel_Spinner);
		preferencesPanel.add(setFont_PrefPanel_Button);
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
		predefinedConfController = new PredefinedConfController( "config", "predefinedConfiguration.apv" );
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
					readArrayPVViewerDocument(url);

					//super class method - will show "Save" menu unactive
					setHasChanges(false);
					setFontForAll(globalFont);
					setActivePanel(VIEW_PANEL);
				}
			};
		predefinedConfController.setSelectorListener(selectConfListener);
	}


	/**
	 *  Creates the PVs selection panel.
	 */
	private void makePVsSelectionPanel() {
		root_Node = new PVTreeNode(root_Name);
		rootArrayPV_Node = new PVTreeNode(rootArrayPV_Name);

		rootArrayPV_Node.setPVNamesAllowed(true);

		root_Node.add(rootArrayPV_Node);

		//make PVs selectror and place it on the selectionPVsPanel
		pvsSelector = new PVsSelector(root_Node);
        if ( accelerator != null ) {
            pvsSelector.setAccelerator( accelerator );
        }
		pvsSelector.removeMessageTextField();

		setPVsPanel.setLayout(new BorderLayout());
		setPVsPanel.add(pvsSelector, BorderLayout.CENTER);

		//-------------------------------------------
		//make tree listeners
		//-------------------------------------------

		//listener for switch ON and OFF
		switchPVTreeListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String command = e.getActionCommand();
					PVTreeNode pvn = (PVTreeNode) e.getSource();
					boolean switchOnLocal = command.equals(PVTreeNode.SWITCHED_ON_COMMAND);
					PVTreeNode pvn_parent = (PVTreeNode) pvn.getParent();
					int index = -1;

					ArrayViewerPV arrPV = null;

					if (pvn_parent == rootArrayPV_Node) {
						//System.out.println("debug switch on the x-position PV switchOn=" + switchOnLocal);
						index = pvn_parent.getIndex(pvn);
						arrPV = arrayPVs.get(index);
					}

					if (index >= 0 && arrPV != null) {
						arrPV.getArrayDataPV().setSwitchOn(switchOnLocal);
						updateGraphPanel();
					}
					
					viewPanel.validate();
					viewPanel.repaint();
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
					ArrayViewerPV pv_tmp = null;
					if (bool_removePV) {

						if (pvn_parent == rootArrayPV_Node) {
							index = pvn_parent.getIndex(pvn);
							//System.out.println("debug delete ArrayViewerPV  from arrayPVs index=" + index);
							pv_tmp = arrayPVs.get(index);
							arrayPVs.remove(pv_tmp);
						}

						if (index >= 0) {
							updatingController.removeArrayDataPV(pv_tmp.getArrayDataPV());
							setColors(pvn_parent, index);
							updateGraphPanel();
						}
					} else {
						if (pvn_parent == rootArrayPV_Node) {
							index = pvn_parent.getIndex(pvn);
							//System.out.println("debug add ArrayViewerPV  from arrayPVs index="+index );
							pv_tmp = new ArrayViewerPV(arrayPVGraphs);
							arrayPVs.add(index, pv_tmp);
						}
						if (index >= 0) {
							pv_tmp.setChannel(pvn.getChannel());
							updatingController.addArrayDataPV(pv_tmp.getArrayDataPV());
							setColors(pvn_parent, -1);
							updateGraphPanel();
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
					ArrayViewerPV pv_tmp = null;

					if (pvn_parent == rootArrayPV_Node) {
						index = pvn_parent.getIndex(pvn);
						//System.out.println("debug rename ArrayViewerPV  from arrayPVs index="+index );
						pv_tmp = arrayPVs.get(index);
					}

					if (index >= 0) {
						pv_tmp.setChannel(pvn.getChannel());
						setColors(pvn_parent, -1);
						updateGraphPanel();
					}
				}
			};

		//register the listeners
		rootArrayPV_Node.setSwitchedOnOffListener(switchPVTreeListener);
		rootArrayPV_Node.setCreateRemoveListener(createDeletePVTreeListener);
		rootArrayPV_Node.setRenameListener(renamePVTreeListener);

	}


	/**
	 *  Creates the PV viewer panel with all graphs sub-panels,
	 */
	private void makeViewPanel() {

		arrayPVsGraphPanel = new ValuesGraphPanel(
				"PVs' waveforms",
				arrayPVs,
				arrayPVGraphs,
				this);

		//make left tree and "automatic updating" radio button
		pvsTreePanelView = pvsSelector.getNewPVsTreePanel();
		pvsTreePanelView.getJTree().setBackground(Color.white);

		pvsTreePanelView.setPreferredSize(new Dimension(0, 0));
		pvsSelector.setPreferredSize(new Dimension(0, 0));

		//set frequency spinner
		freq_ViewPanel_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		freq_ViewPanel_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int freq = ((Integer) freq_ViewPanel_Spinner.getValue()).intValue();
					updatingController.setUpdateFrequency((freq)*0.1);
				}
			});

		//make the view panel
		viewPanel.setLayout(new BorderLayout());

		JPanel tmp_panel_0 = new JPanel();
		//tmp_panel_0.setLayout(new GridLayout(2, 1, 1, 1));
		//tmp_panel_0.add(arrayPVsGraphPanel.getJPanel());
		//tmp_panel_0.add(pvsTable.getPanel());
		
		tmp_panel_0.setLayout(new BorderLayout());
		tmp_panel_0.add(arrayPVsGraphPanel.getJPanel(),BorderLayout.CENTER);
		tmp_panel_0.add(pvsTable.getPanel(),BorderLayout.SOUTH);		

		JPanel tmp_panel_1 = new JPanel();
		tmp_panel_1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_panel_1.add(autoUpdateView_Button);

		JPanel tmp_panel_2 = new JPanel();
		tmp_panel_2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_panel_2.add(freq_ViewPanel_Spinner);
		tmp_panel_2.add(viewPanelFreq_Label);

		JPanel tmp_panel_3 = new JPanel();
		tmp_panel_3.setLayout(new GridLayout(1, 2, 1, 1));
		tmp_panel_3.add(tmp_panel_1);
		tmp_panel_3.add(tmp_panel_2);

		JPanel tmp_panel_4 = new JPanel();
		tmp_panel_4.setLayout(new VerticalLayout());
		tmp_panel_4.add(viewPanelTitle_Label);
		tmp_panel_4.add(tmp_panel_3);

		JPanel tmp_panel_5 = new JPanel();
		tmp_panel_5.setLayout(new BorderLayout());
		tmp_panel_5.add(tmp_panel_4, BorderLayout.NORTH);
		tmp_panel_5.add(pvsTreePanelView, BorderLayout.CENTER);

		viewPanel.add(tmp_panel_0, BorderLayout.CENTER);
		viewPanel.add(tmp_panel_5, BorderLayout.WEST);

	}


	/**
	 *  Clean up the document content
	 */
	private void cleanUp() {

		cleanMessageTextField();

		for (int i = 0, n = arrayPVs.size(); i < n; i++) {
			ArrayViewerPV pv_tmp = arrayPVs.get(i);
			updatingController.removeArrayDataPV(pv_tmp.getArrayDataPV());
		}

		arrayPVs.clear();

		rootArrayPV_Node.removeAllChildren();
		setColors(rootArrayPV_Node, -1);

		((DefaultTreeModel) pvsSelector.getPVsTreePanel().getJTree().getModel()).reload();
		((DefaultTreeModel) pvsTreePanelView.getJTree().getModel()).reload();

	}


	/**
	 *  Description of the Method
	 */
	private void cleanMessageTextField() {
		messageTextLocal.setText(null);
		messageTextLocal.setForeground(Color.red);
	}


	/**
	 *  Sets the fontForAll attribute of the ArrayPVViewerDocument object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	private void setFontForAll(Font fnt) {

		pvsSelector.setAllFonts(fnt);

		arrayPVsGraphPanel.setAllFonts(fnt);

		pvsTable.setFont(fnt);

		pvsTreePanelView.setAllFonts(fnt);
		viewPanelTitle_Label.setFont(fnt);
		autoUpdateView_Button.setFont(fnt);

		viewPanelFreq_Label.setFont(fnt);
		freq_ViewPanel_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) freq_ViewPanel_Spinner.getEditor()).getTextField().setFont(fnt);

		messageTextLocal.setFont(fnt);
		fontSize_PrefPanel_Spinner.setValue(new Integer(fnt.getSize()));
		predefinedConfController.setFontsForAll(fnt);

		globalFont = fnt;
	}



	/**
	 *  Sets the colors for PVs ( graphs ) and tree nodes. If deleteIndex < 0 then
	 *  nothing to delete.
	 *
	 *@param  deleteIndex  The new colors value
	 *@param  pvNode       The new colors value
	 */
	private void setColors(PVTreeNode pvNode, int deleteIndex) {

		if (pvNode == rootArrayPV_Node) {
			for (int i = 0, n = arrayPVs.size(); i < n; i++) {
				ArrayViewerPV arrPV = arrayPVs.get(i);
				arrPV.setColor(IncrementalColors.getColor(i));
			}
		}

		final Enumeration<PVTreeNode> enumNodes =  pvNode.children();
		int keptNodeCounter = 0;  // counter of nodes that are not deleted
        for ( int nodeIndex = 0 ; enumNodes.hasMoreElements() ; nodeIndex++ ) {
			final PVTreeNode pvn = enumNodes.nextElement();
			if ( nodeIndex != deleteIndex ) {
				pvn.setColor( IncrementalColors.getColor( keptNodeCounter ) );
				keptNodeCounter++;
			}
		}
	}


	/**
	 *  Updates all data on graphs panels
	 */
	public void updateGraphPanel() {
		arrayPVsGraphPanel.update();

		for (int i = 0, n = arrayPVs.size(); i < n; i++) {
			ArrayViewerPV arrPV = arrayPVs.get(i);
			arrPV.updateAvgAndSigma();
		}

		pvsTable.doLayout();
	}


	/**
	 *  Sets the activePanel attribute of the ArrayPVViewerDocument object
	 *
	 *@param  newActPanelInd  The new activePanel value
	 */
	private void setActivePanel(int newActPanelInd) {
		int oldActPanelInd = ACTIVE_PANEL;

		if (oldActPanelInd == newActPanelInd) {
			return;
		}

		//shut up active panel
		if (oldActPanelInd == VIEW_PANEL) {
			//action before view panel will disappear
		} else if (oldActPanelInd == SET_PVS_PANEL) {
			//action before set PVs panel will disappear
		} else if (oldActPanelInd == PREFERENCES_PANEL) {
			//action before preferences panel will disappear
		} else if (oldActPanelInd == PREDEF_CONF_PANEL) {
			//action before predifined configurations panel will disappear
		}

		//make something before the new panel will show up
		if (newActPanelInd == VIEW_PANEL) {
			getArrayPVViewerWindow().setJComponent(viewPanel);
		} else if (newActPanelInd == SET_PVS_PANEL) {
			getArrayPVViewerWindow().setJComponent(setPVsPanel);
		} else if (newActPanelInd == PREFERENCES_PANEL) {
			getArrayPVViewerWindow().setJComponent(preferencesPanel);
		} else if (newActPanelInd == PREDEF_CONF_PANEL) {
			getArrayPVViewerWindow().setJComponent(configPanel);
		}

		ACTIVE_PANEL = newActPanelInd;

		cleanMessageTextField();
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
    
    
}

//----------------------------------------------
//Class deals with date and time
//----------------------------------------------
/**
 *  Description of the Class
 *
 *@author     shishlo
 *@version
 *@since    July 8, 2004
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

