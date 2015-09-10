/*
 *  QuadshakerDocument.java
 *
 *  Created on September 18, 2006
 */
package xal.app.quadshaker;

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

import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.tools.data.DataAdaptor;

import xal.extension.scan.UpdatingEventController;

/**
 *  QuadshakerDocument is a custom XalDocument for Quadshaker application. The
 *  document manages the data that is displayed in the window.
 *
 *@author     shishlo
 */

public class QuadshakerDocument extends AcceleratorDocument {
	static {
		ChannelFactory.defaultFactory().init();
	}

	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	//Updating controller
	UpdatingEventController updatingController = new UpdatingEventController();

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

	//--------------------------------------------------------------
	//The third panel with control elements
	//--------------------------------------------------------------
	private JPanel thirdPanel = null;

	//-------------------------------------------------------------
	//PREFERENCES_PANEL and GUI elements, actions etc.
	//-------------------------------------------------------------
	private JPanel preferencesPanel = new JPanel();
	private JButton setFont_PrefPanel_Button = new JButton("Set Font Size");
	private JSpinner fontSize_PrefPanel_Spinner = new JSpinner(new SpinnerNumberModel(7, 7, 26, 1));
	private JLabel timeDealyUC_Label = new JLabel("time delay for graphics update [sec]", JLabel.LEFT);
	private JSpinner timeDealyUC_Spinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.1));
	private JLabel signCoeffX_Label = new JLabel("correction coeff sign X", JLabel.LEFT);
	private JLabel signCoeffY_Label = new JLabel("correction coeff sign Y", JLabel.LEFT);

	private Font globalFont = new Font("Monospaced", Font.BOLD, 10);

	//------------------------------------------------
	//PANEL STATE
	//------------------------------------------------
	private int ACTIVE_PANEL = 0;
	private int FIRST_PANEL = 0;
	private int SECOND_PANEL = 1;
	private int THIRD_PANEL = 2;
	private int PREFERENCES_PANEL = 3;
	//-------------------------------------
	//time and date related member
	//-------------------------------------
	private static DateAndTimeText dateAndTime = new DateAndTimeText();

	//=====================================
	//Controllers of the sub-panels
	//=====================================
	private ShakerController shakerController = null;

	private ShakeAnalysis shakeAnalysis = null;

	private OrbitCorrector orbitCorrector	= null;

	private QuadsTable quadsTable = new QuadsTable();
	private BPMsTable bpmsTable = new BPMsTable();

	//------------------------------------------
	//SAVE RESTORE PART
	//------------------------------------------
	//root node name
	private String dataRootName = "QUAD_SHAKER";


	/**
	 *  Create a new empty QuadshakerDocument
	 */
	public QuadshakerDocument() {
		super();

		updatingController.setUpdateTime(1.0);

		//controllers of the panels
		shakerController = new ShakerController(updatingController);
		shakerController.setTableModels(quadsTable, bpmsTable);

		shakeAnalysis = new ShakeAnalysis();
		shakeAnalysis.setTableModels(quadsTable, bpmsTable);

		shakerController.getShakerRunController().setShakeAnalysis(shakeAnalysis);

		orbitCorrector = new OrbitCorrector();
		orbitCorrector.setTableModel(quadsTable);

		shakeAnalysis.setOrbitCorrector(orbitCorrector);

		//set up tabbed panels
		firstPanel = shakerController.getPanel();
		secondPanel = shakeAnalysis.getPanel();
		thirdPanel = orbitCorrector.getPanel();

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
		mainTabbedPanel.add("Analysis", secondPanel);
		mainTabbedPanel.add("Orbit Correction", thirdPanel);
		mainTabbedPanel.add("Preferences", preferencesPanel);


		mainTabbedPanel.setSelectedIndex(0);

		//=========================================================
		//CCL specific part for initial development only
		//??? will be removed later
		//=========================================================
		loadDefaultAccelerator();
		Accelerator acc = getAccelerator();
		AcceleratorSeq seq1 = acc.getSequence("CCL1");
		AcceleratorSeq seq2 = acc.getSequence("CCL2");
		AcceleratorSeq seq3 = acc.getSequence("CCL3");
		AcceleratorSeq seq4 = acc.getSequence("CCL4");

		ArrayList<AcceleratorSeq> cclArr = new ArrayList<AcceleratorSeq>();
		cclArr.add(seq1);
		cclArr.add(seq2);
		cclArr.add(seq3);
		cclArr.add(seq4);

		AcceleratorSeqCombo cclSeq = new AcceleratorSeqCombo("CCL", cclArr);
		java.util.List<Quadrupole> cclQuads = cclSeq.getAllNodesWithQualifier((new OrTypeQualifier()).or(Quadrupole.s_strType));
		Iterator<Quadrupole> itr = cclQuads.iterator();
		while(itr.hasNext()) {
			Quadrupole quad = itr.next();
			Quad_Element quadElm = new Quad_Element(quad.getId());
			quadElm.setActive(true);
			quadsTable.getListModel().addElement(quadElm);
			quadElm.getWrpChRBField().setChannelName(quad.getChannel("fieldRB").channelName());
			if(quad.getType().equals("QTH") || quad.getType().equals("QTV")) {
				//has trim
				quadElm.isItTrim(true);
				quadElm.getWrpChCurrent().setChannelName(quad.getChannel("trimI_Set").channelName());
			} else {
				//no trim
				quadElm.isItTrim(false);
				quadElm.getWrpChCurrent().setChannelName(quad.getChannel("I_Set").channelName());
			}
		}

		AcceleratorSeq seq5 = acc.getSequence("SCLMed");

		java.util.List<BPM> cclBPMs = cclSeq.getAllNodesOfType("BPM");
		java.util.List<BPM> sclBPMs = seq5.getAllNodesOfType("BPM");

		int n_add = Math.min(4, sclBPMs.size());
		for(int i = 0; i < n_add; i++) {
			cclBPMs.add(sclBPMs.get(i));
		}

		for(int i = 0, n = cclBPMs.size(); i < n; i++) {
			BPM bpm = cclBPMs.get(i);
			BPM_Element bpmElm = new BPM_Element(bpm.getId());
			bpmElm.setActive(true);
			bpmElm.getWrpChannelX().setChannelName(bpm.getChannel("xAvg").channelName());
			bpmElm.getWrpChannelY().setChannelName(bpm.getChannel("yAvg").channelName());
			bpmsTable.getListModel().addElement(bpmElm);
		}

		AcceleratorSeq seq0 = acc.getSequence("DTL6");

		ArrayList<AcceleratorSeq> dtl_cclArr = new ArrayList<AcceleratorSeq>();
		dtl_cclArr.add(seq0);
		dtl_cclArr.add(seq1);
		dtl_cclArr.add(seq2);
		dtl_cclArr.add(seq3);
		dtl_cclArr.add(seq4);
		AcceleratorSeqCombo comboSeq1 = new AcceleratorSeqCombo("DTL-CCL", dtl_cclArr);

		orbitCorrector.setAccelSeq(comboSeq1);

		//create final combo-seq
		ArrayList<AcceleratorSeq> dtl_sclArr = new ArrayList<AcceleratorSeq>();
		dtl_sclArr.add(seq0);
		dtl_sclArr.add(seq1);
		dtl_sclArr.add(seq2);
		dtl_sclArr.add(seq3);
		dtl_sclArr.add(seq4);
		dtl_sclArr.add(seq5);
		AcceleratorSeqCombo comboSeq2 = new AcceleratorSeqCombo("DTL-SCL", dtl_sclArr);

		shakeAnalysis.setAccelSeq(comboSeq2);
	}


	/**
	 *  Create a new document loaded from the URL file
	 *
	 *@param  url  The URL of the file to load into the new document.
	 */
	public QuadshakerDocument(URL url) {
		this();
		if(url == null) {
			return;
		}
		setSource(url);
		readQuadshakerDocument(url);

		//super class method - will show "Save" menu active
		if(url.getProtocol().equals("jar")) {
			return;
		}
		setHasChanges(true);
	}


	/**
	 *  Make a main window by instantiating the QuadshakerWindow window.
	 */
	public void makeMainWindow() {
		mainWindow = new QuadshakerWindow(this);
		//---------------------------------------------------------------
		//this is the place for initializing initial state of main window
		//---------------------------------------------------------------

		//define initial state of the window
		getQuadshakerWindow().setJComponent(mainTabbedPanel);

		//set connections between message texts
		messageTextLocal = getQuadshakerWindow().getMessageTextField();

		//set all text messages for sub frames
		shakerController.getMessageText().setDocument(messageTextLocal.getDocument());
		shakeAnalysis.getMessageText().setDocument(messageTextLocal.getDocument());
		orbitCorrector.getMessageText().setDocument(messageTextLocal.getDocument());

		fontSize_PrefPanel_Spinner.setValue(new Integer(globalFont.getSize()));
		setFontForAll(globalFont);

		timeDealyUC_Spinner.setValue(new Double(updatingController.getUpdateTime()));
		updatingController.setUpdateTime(((Double) timeDealyUC_Spinner.getValue()).doubleValue());

		//set timer
		JTextField timeTxt_temp = dateAndTime.getNewTimeTextField();
		timeTxt_temp.setHorizontalAlignment(JTextField.CENTER);
		getQuadshakerWindow().addTimeStamp(timeTxt_temp);

		mainWindow.setSize(new Dimension(800, 600));
	}


	/**
	 *  Dispose of QuadshakerDocument resources. This method overrides an empty
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
	public void readQuadshakerDocument(URL url) {

		//read the document content from the persistent storage

		XmlDataAdaptor readAdp = null;
		readAdp = XmlDataAdaptor.adaptorForUrl(url, false);

		if(readAdp != null) {
			DataAdaptor quadshakerData_Adaptor = readAdp.childAdaptor(dataRootName);
			if(quadshakerData_Adaptor != null) {
				cleanUp();
				setTitle(quadshakerData_Adaptor.stringValue("title"));

				//set font
				DataAdaptor params_font = quadshakerData_Adaptor.childAdaptor("font");
				int font_size = params_font.intValue("size");
				int style = params_font.intValue("style");
				String font_Family = params_font.stringValue("name");
				globalFont = new Font(font_Family, style, font_size);
				fontSize_PrefPanel_Spinner.setValue(new Integer(font_size));
				setFontForAll(globalFont);

				DataAdaptor params_da =  quadshakerData_Adaptor.childAdaptor("shared_parameters");
				updatingController.setUpdateTime(params_da.doubleValue("update_time"));

				//read the application specific data
				//from quadshakerData_Adaptor
				shakerController.readData(quadshakerData_Adaptor);
				long pvLoggerId = shakerController.getShakerRunController().pvLoggerSnapshotId();
				if(pvLoggerId > 1) {
					shakeAnalysis.setPVLoggerId(pvLoggerId);
				}
				else{
					shakeAnalysis.setPVLoggerId(0);
				}
			}
		}
	}


	/**
	 *  Save the QuadshakerDocument document to the specified URL.
	 *
	 *@param  url  Description of the Parameter
	 */
	public void saveDocumentAs(URL url) {
		//this is the place to write document to the persistent storage

		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
		XmlDataAdaptor quadshakerData_Adaptor = (XmlDataAdaptor) da.createChild(dataRootName);
		quadshakerData_Adaptor.setValue("title", url.getFile());

		//dump parameters
		DataAdaptor params_font = quadshakerData_Adaptor.createChild("font");
		params_font.setValue("name", globalFont.getFamily());
		params_font.setValue("style", globalFont.getStyle());
		params_font.setValue("size", globalFont.getSize());

		DataAdaptor params_da =  quadshakerData_Adaptor.createChild("shared_parameters");
		params_da.setValue("update_time", updatingController.getUpdateTime());

		//write the application specific data
		//into the quadshakerData_Adaptor XmlDataAdaptor
		shakerController.dumpData(quadshakerData_Adaptor);


		//dump data into the file
		try {
			da.writeToUrl( url );

			//super class method - will show "Save" menu active
			setHasChanges( false );
		}
        catch(XmlDataAdaptor.WriteException exception ) {
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
	 *  Convenience method for getting the QuadshakerWindow window. It is the cast
	 *  to the proper subclass of XalWindow. This allows me to avoid casting the
	 *  window every time I reference it.
	 *
	 *@return    The main window cast to its dynamic runtime class
	 */
	private QuadshakerWindow getQuadshakerWindow() {
		return (QuadshakerWindow) mainWindow;
	}


	/**
	 *  Register actions for the menu items and toolbar.
	 *
	 *@param  commander  Description of the Parameter
	 */

	public void customizeCommands(Commander commander) {
		// define the "save-data-to-ascii" action
		Action dumpDataToASCIIAction =
			new AbstractAction("save-data-to-ascii") {
                
                /** ID for serializable version */
                private static final long serialVersionUID = 1L;
                
				public void actionPerformed(ActionEvent event) {
					System.out.println("debug dump data to ascii");
				}
			};
		commander.registerAction(dumpDataToASCIIAction);
	}


	/**
	 *  Description of the Method
	 */
	private void makePreferencesPanel() {

		fontSize_PrefPanel_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		timeDealyUC_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

		JPanel tmp_0 = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_0.add(fontSize_PrefPanel_Spinner);
		tmp_0.add(setFont_PrefPanel_Button);

		JPanel tmp_1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_1.add(timeDealyUC_Spinner);
		tmp_1.add(timeDealyUC_Label);

		JPanel tmp_2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_2.add(orbitCorrector.getSignXText());
		tmp_2.add(signCoeffX_Label);

		JPanel tmp_3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_3.add(orbitCorrector.getSignYText());
		tmp_3.add(signCoeffY_Label);

		JPanel tmp_elms = new JPanel(new GridLayout(0, 1));
		tmp_elms.add(tmp_0);
		tmp_elms.add(tmp_1);
		tmp_elms.add(tmp_2);
		tmp_elms.add(tmp_3);

		preferencesPanel.setLayout(new BorderLayout());
		preferencesPanel.add(tmp_elms, BorderLayout.NORTH);

		setFont_PrefPanel_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int fnt_size = ((Integer) fontSize_PrefPanel_Spinner.getValue()).intValue();
					globalFont = new Font(globalFont.getFamily(), globalFont.getStyle(), fnt_size);
					setFontForAll(globalFont);
					int h = getQuadshakerWindow().getHeight();
					int w = getQuadshakerWindow().getWidth();
					getQuadshakerWindow().validate();
				}
			});

		timeDealyUC_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent evnt) {
					updatingController.setUpdateTime(((Double) timeDealyUC_Spinner.getValue()).doubleValue());
				}
			});

	}

	/**
	 *  Clean up the document content
	 */
	private void cleanUp() {
		cleanMessageTextField();
		shakeAnalysis.clearAllGraphContent();
	}

	/**
	 *  Description of the Method
	 */
	private void cleanMessageTextField() {
		messageTextLocal.setText(null);
		messageTextLocal.setForeground(Color.red);
	}

	/**
	 *  Sets the fontForAll attribute of the QuadshakerDocument object
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
		shakerController.setFontForAll(fnt);
		shakeAnalysis.setFontForAll(fnt);
		orbitCorrector.setFontForAll(fnt);


		timeDealyUC_Label.setFont(fnt);
		timeDealyUC_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) timeDealyUC_Spinner.getEditor()).getTextField().setFont(fnt);

		signCoeffX_Label.setFont(fnt);
		signCoeffY_Label.setFont(fnt);
	}


	/**
	 *  Sets the activePanel attribute of the QuadshakerDocument object
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
		}  else if(oldActPanelInd == THIRD_PANEL) {
			//action before third panel will disappear
		} else if(oldActPanelInd == PREFERENCES_PANEL) {
			//action before preferences panel will disappear
		}

		//make something before the new panel will show up
		if(newActPanelInd == FIRST_PANEL) {
			//action before view values panel will show up
		} else if(newActPanelInd == SECOND_PANEL) {
			//action before second pane will show up
		} else if(newActPanelInd == THIRD_PANEL) {
			//action before third pane will show up
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

