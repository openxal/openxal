/*
 *  RingbpmviewerDocument.java
 *
 *  Created on May 24, 2005, 10:25 AM
 */
package xal.app.ringbpmviewer;

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
import java.util.regex.*;

import xal.ca.*;
import xal.extension.widgets.plot.*;
import xal.extension.application.*;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.*;
import xal.tools.apputils.*;
import xal.tools.apputils.pvselection.*;
import xal.extension.widgets.swing.*;

import xal.extension.scan.UpdatingEventController;

import xal.app.ringbpmviewer.*;

import xal.smf.data.XMLDataManager;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorNode;
import xal.smf.impl.BPM;


/**
 *  RingbpmviewerDocument is a custom XalDocument for Ringbpmviewer application.
 *  The document manages the data that is displayed in the window.
 *
 *@author     shishlo
 */

public class RingbpmviewerDocument extends XalDocument {
    
	static {
		ChannelFactory.defaultFactory().init();
	}
    
	//message text field. It is actually message text field from
	private JTextField messageTextLocal = new JTextField();
    
	//Updating controller
	UpdatingEventController updatingController = new UpdatingEventController();
    
	//update controller for the changing the set of bar columns in charts
	private UpdatingEventController ucContent = new UpdatingEventController();
    
	//the tabbed panel that will keep all subpanels
	private JTabbedPane mainTabbedPanel = new JTabbedPane();
    
	//--------------------------------------------------------------
	//The view ring BPM panel with control elements
	//--------------------------------------------------------------
	private JPanel viewRingBPMPanel = null;
	private RingBPMsController ringBPMsController = null;
    
	//--------------------------------------------------------------
	//The view ring BPM TBT waveforms panel with control elements
	//--------------------------------------------------------------
	private JPanel viewWaveFormPanel = null;
	RingBPMsWaveFormController ringBPMsWaveFormController = null;
    
	//--------------------------------------------------------------
	//The HEBT BPM panel with control elements
	//--------------------------------------------------------------
	private JPanel viewHebtBPMPanel = null;
	private TrLineBPMsController hebtBPMsController = null;
    
	//--------------------------------------------------------------
	//The RTBT BPM panel with control elements
	//--------------------------------------------------------------
	private JPanel viewRtbtBPMPanel = null;
	private TrLineBPMsController rtbtBPMsController = null;
    
	//-------------------------------------------------------------
	//PREFERENCES_PANEL and GUI elements, actions etc.
	//-------------------------------------------------------------
	private JPanel preferencesPanel = new JPanel();
	private JButton setFont_PrefPanel_Button = new JButton("Set Font Size");
	private JSpinner fontSize_PrefPanel_Spinner = new JSpinner(new SpinnerNumberModel(7, 7, 26, 1));
	private JLabel timeDealyUC_Label = new JLabel("time delay for graphics update [sec]", JLabel.LEFT);
	private JSpinner timeDealyUC_Spinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.1));
    
	private Font globalFont = new Font("Monospaced", Font.BOLD, 10);
    
	//------------------------------------------------
	//PANEL STATE
	//------------------------------------------------
	private int ACTIVE_PANEL = 0;
	private int VIEW_RING_BPM_PANEL = 0;
	private int VIEW_WAVEFORM_PANEL = 1;
	private int VIEW_HEBT_BPM_PANEL = 2;
	private int VIEW_RTBT_BPM_PANEL = 3;
	private int PREFERENCES_PANEL = 4;
	//-------------------------------------
	//time and date related member
	//-------------------------------------
	private static DateAndTimeText dateAndTime = new DateAndTimeText();
    
	//------------------------------------------
	//SAVE RESTORE PART
	//------------------------------------------
	//root node name
	private String dataRootName = "RING_BPM_VIEWER";
    
    
	/**
	 *  Create a new empty RingbpmviewerDocument
	 */
	public RingbpmviewerDocument() {
		updatingController.setUpdateTime(1.0);
		ucContent.setUpdateTime(0.3);
        
		ringBPMsController = new RingBPMsController(updatingController, ucContent);
		viewRingBPMPanel = ringBPMsController.getPanel();
        
		ringBPMsWaveFormController = new RingBPMsWaveFormController(updatingController, ucContent);
		viewWaveFormPanel = ringBPMsWaveFormController.getPanel();
        
		hebtBPMsController = new TrLineBPMsController(updatingController, ucContent);
		viewHebtBPMPanel = hebtBPMsController.getPanel();
        
		rtbtBPMsController = new TrLineBPMsController(updatingController, ucContent);
		viewRtbtBPMPanel = rtbtBPMsController.getPanel();
        
		mainTabbedPanel.addChangeListener(
                                          new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JTabbedPane tbp = (JTabbedPane) e.getSource();
                setActivePanel(tbp.getSelectedIndex());
            }
        });
        
		//make all panels
		makePreferencesPanel();
        
		mainTabbedPanel.add("Ring BPMs X,Y,Amp", viewRingBPMPanel);
		mainTabbedPanel.add("Ring TBT X,Y,Amp", viewWaveFormPanel);
		mainTabbedPanel.add("HEBT BPMs X,Y,Amp", viewHebtBPMPanel);
		mainTabbedPanel.add("RTBT BPMs X,Y,Amp", viewRtbtBPMPanel);
		mainTabbedPanel.add("Preferences", preferencesPanel);
        
		mainTabbedPanel.setSelectedIndex(0);

		//make new data adaptors from accelerator
		Accelerator accl = XMLDataManager.loadDefaultAccelerator();
        
		final DataAdaptor ring_bpms_da = XmlDataAdaptor.newEmptyDocumentAdaptor().createChild("RING_BPM_TBT_PVs");
		AcceleratorSeq accSeq = accl.findSequence("Ring");
		java.util.List<AcceleratorNode> bpms_list = accSeq.getAllNodesOfType(BPM.s_strType);
		Iterator<AcceleratorNode> iter = bpms_list.iterator();
		Pattern p = Pattern.compile(":BPM_((.*))");
		while(iter.hasNext()){
			AcceleratorNode node = iter.next();
			if(node.getStatus()){
				String bpm_name = node.getId();
				DataAdaptor bpm_da = ring_bpms_da.createChild("RING_BPM");
				Matcher m = p.matcher(bpm_name);
				m.find();
				bpm_da.setValue("name", m.group(1));
				DataAdaptor pvs_da = bpm_da.createChild("PV_NAMES");
				DataAdaptor pvx_da = pvs_da.createChild("xTBT");
				DataAdaptor pvy_da = pvs_da.createChild("yTBT");
				DataAdaptor pvAmp_da = pvs_da.createChild("ampTBT");
				pvx_da.setValue("name", bpm_name+":xTBT");
				pvy_da.setValue("name", bpm_name+":yTBT");
				pvAmp_da.setValue("name", bpm_name+":ampTBT");
			}
		}
		
		final DataAdaptor hebt_bpms_da = XmlDataAdaptor.newEmptyDocumentAdaptor().createChild("HEBT_BPM_PVs");
		accSeq = accl.findSequence("HEBT");
		bpms_list = accSeq.getAllNodesOfType(BPM.s_strType);
		iter = bpms_list.iterator();
		p = Pattern.compile(":BPM((.*))");
		while(iter.hasNext()){
			AcceleratorNode node = iter.next();
			if(node.getStatus()){
				String bpm_name = node.getId();
				DataAdaptor bpm_da = hebt_bpms_da.createChild("TRANSF_LINE_BPM");
				Matcher m = p.matcher(bpm_name);
				m.find();
				bpm_da.setValue("name", m.group(1));
				bpm_da.setValue("disabled", false);
				DataAdaptor pvs_da = bpm_da.createChild("PV_NAMES");
				DataAdaptor pvx_da = pvs_da.createChild("xAvg");
				DataAdaptor pvy_da = pvs_da.createChild("yAvg");
				DataAdaptor pvAmp_da = pvs_da.createChild("amplitudeAvg");
				pvx_da.setValue("name", bpm_name+":xAvg");
				pvy_da.setValue("name", bpm_name+":yAvg");
				pvAmp_da.setValue("name", bpm_name+":amplitudeAvg");
			}
		}
		
		final DataAdaptor rtbt_bpms_da = XmlDataAdaptor.newEmptyDocumentAdaptor().createChild("RTBT_BPM_PVs");
		accSeq = accl.findSequence("RTBT");
		bpms_list = accSeq.getAllNodesOfType(BPM.s_strType);
		iter = bpms_list.iterator();
		p = Pattern.compile(":BPM((.*))");
		while(iter.hasNext()){
			AcceleratorNode node = iter.next();
			if(node.getStatus()){
				String bpm_name = node.getId();
				DataAdaptor bpm_da = rtbt_bpms_da.createChild("TRANSF_LINE_BPM");
				Matcher m = p.matcher(bpm_name);
				m.find();
				bpm_da.setValue("name", m.group(1));
				bpm_da.setValue("disabled", false);
				DataAdaptor pvs_da = bpm_da.createChild("PV_NAMES");
				DataAdaptor pvx_da = pvs_da.createChild("xAvg");
				DataAdaptor pvy_da = pvs_da.createChild("yAvg");
				DataAdaptor pvAmp_da = pvs_da.createChild("amplitudeAvg");
				pvx_da.setValue("name", bpm_name+":xAvg");
				pvy_da.setValue("name", bpm_name+":yAvg");
				pvAmp_da.setValue("name", bpm_name+":ampAvg");
			}
		}

		ringBPMsController.init(ring_bpms_da);
		hebtBPMsController.init(hebt_bpms_da);
		rtbtBPMsController.init(rtbt_bpms_da);
        
		ringBPMsWaveFormController.getListenToEPICS_Button().setModel( ringBPMsController.getListenToEPICS_Button().getModel() );

		ringBPMsController.setListenToEPICS(false);
        
		ringBPMsWaveFormController.init(ringBPMsController.getRingBPMset());
	}
    
    
	/**
	 *  Create a new document loaded from the URL file
	 *
	 *@param  url  The URL of the file to load into the new document.
	 */
	public RingbpmviewerDocument(URL url) {
		this();
		if(url == null) {
			return;
		}
		setSource(url);
		readRingbpmviewerDocument(url);
        
		//super class method - will show "Save" menu active
		if(url.getProtocol().equals("jar")) {
			return;
		}
		setHasChanges(true);
	}
    
    
	/**
	 *  Make a main window by instantiating the RingbpmviewerWindow window.
	 */
	public void makeMainWindow() {
		mainWindow = new RingbpmviewerWindow(this);
		//---------------------------------------------------------------
		//this is the place for initializing initial state of main window
		//---------------------------------------------------------------
        
		//define initial state of the window
		getRingbpmviewerWindow().setJComponent(mainTabbedPanel);
        
		//set connections between message texts
		messageTextLocal = getRingbpmviewerWindow().getMessageTextField();
        
		//set all text messages for sub frames
		//???
		ringBPMsWaveFormController.setMessageTextLocal(messageTextLocal);
		ringBPMsWaveFormController.setOnwnerFrame(getRingbpmviewerWindow());
        
		fontSize_PrefPanel_Spinner.setValue(new Integer(globalFont.getSize()));
		setFontForAll(globalFont);
        
		timeDealyUC_Spinner.setValue(new Double(updatingController.getUpdateTime()));
		updatingController.setUpdateTime(((Double) timeDealyUC_Spinner.getValue()).doubleValue());
        
		//set connections to a message text in others panels
        
		//set timer
		JTextField timeTxt_temp = dateAndTime.getNewTimeTextField();
		timeTxt_temp.setHorizontalAlignment(JTextField.CENTER);
		getRingbpmviewerWindow().addTimeStamp(timeTxt_temp);
        
		mainWindow.setSize(new Dimension(700, 600));
	}
    
    
	/**
	 *  Dispose of RingbpmviewerDocument resources. This method overrides an empty
	 *  superclass method.
	 */
	protected void freeCustomResources() {
		cleanUp();
	}
    
    
	/**
	 *  Reads the content of the document from the specified URL.
	 *
	 *@param  url  Description of the Parameter
	 */
	public void readRingbpmviewerDocument(URL url) {
        
		//read the document content from the persistent storage
        
		XmlDataAdaptor readAdp = null;
		readAdp = XmlDataAdaptor.adaptorForUrl(url, false);
        
		if(readAdp != null) {
			XmlDataAdaptor ringbpmviewerData_Adaptor = (XmlDataAdaptor) readAdp.childAdaptor(dataRootName);
			if(ringbpmviewerData_Adaptor != null) {
				cleanUp();
				setTitle(ringbpmviewerData_Adaptor.stringValue("title"));
                
				//set font
				XmlDataAdaptor params_font = (XmlDataAdaptor) ringbpmviewerData_Adaptor.childAdaptor("font");
				int font_size = params_font.intValue("size");
				int style = params_font.intValue("style");
				String font_Family = params_font.stringValue("name");
				globalFont = new Font(font_Family, style, font_size);
				fontSize_PrefPanel_Spinner.setValue(new Integer(font_size));
				setFontForAll(globalFont);
                
				XmlDataAdaptor params_da = (XmlDataAdaptor) ringbpmviewerData_Adaptor.childAdaptor("shared_parameters");
				updatingController.setUpdateTime(params_da.doubleValue("update_time"));
                
				//read data to form bpms set
				XmlDataAdaptor ring_bpms_da = (XmlDataAdaptor) ringbpmviewerData_Adaptor.childAdaptor("RING_BPMs");
				ringBPMsController.readData(ring_bpms_da);
                
				//read data to form bpms set
				XmlDataAdaptor hebt_bpms_da = (XmlDataAdaptor) ringbpmviewerData_Adaptor.childAdaptor("HEBT_BPMs");
				hebtBPMsController.readData(hebt_bpms_da);
                
				//read data to form bpms set
				XmlDataAdaptor rtbt_bpms_da = (XmlDataAdaptor) ringbpmviewerData_Adaptor.childAdaptor("RTBT_BPMs");
				rtbtBPMsController.readData(rtbt_bpms_da);
			}
            
			//init the WaveForm controller
			ringBPMsWaveFormController.init(ringBPMsController.getRingBPMset());
		}
	}
    
    
	/**
	 *  Save the RingbpmviewerDocument document to the specified URL.
	 *
	 *@param  url  Description of the Parameter
	 */
	public void saveDocumentAs(URL url) {
		//this is the place to write document to the persistent storage
        
		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
		XmlDataAdaptor ringbpmviewerData_Adaptor = (XmlDataAdaptor) da.createChild(dataRootName);
		ringbpmviewerData_Adaptor.setValue("title", url.getFile());
        
		//dump parameters
		XmlDataAdaptor params_font = (XmlDataAdaptor) ringbpmviewerData_Adaptor.createChild("font");
		params_font.setValue("name", globalFont.getFamily());
		params_font.setValue("style", globalFont.getStyle());
		params_font.setValue("size", globalFont.getSize());
        
		XmlDataAdaptor params_da = (XmlDataAdaptor) ringbpmviewerData_Adaptor.createChild("shared_parameters");
		params_da.setValue("update_time", updatingController.getUpdateTime());
        
		//write the data about BPMs and stack
        
		XmlDataAdaptor ring_bpms_da = (XmlDataAdaptor) ringbpmviewerData_Adaptor.createChild("RING_BPMs");
		ringBPMsController.dumpData(ring_bpms_da);
        
		XmlDataAdaptor hebt_bpms_da = (XmlDataAdaptor) ringbpmviewerData_Adaptor.createChild("HEBT_BPMs");
		hebtBPMsController.dumpData(hebt_bpms_da);
        
		XmlDataAdaptor rtbt_bpms_da = (XmlDataAdaptor) ringbpmviewerData_Adaptor.createChild("RTBT_BPMs");
		rtbtBPMsController.dumpData(rtbt_bpms_da);
        
        
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
	 *  Convenience method for getting the RingbpmviewerWindow window. It is the
	 *  cast to the proper subclass of XalWindow. This allows me to avoid casting
	 *  the window every time I reference it.
	 *
	 *@return    The main window cast to its dynamic runtime class
	 */
	private RingbpmviewerWindow getRingbpmviewerWindow() {
		return (RingbpmviewerWindow) mainWindow;
	}
    
    
	/**
	 *  Register actions for the menu items and toolbar.
	 *
	 *@param  commander  Description of the Parameter
	 */
    
	protected void customizeCommands(Commander commander) {
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
        
		JPanel tmp_2 = new JPanel(new GridLayout(0, 1));
		tmp_2.add(tmp_0);
		tmp_2.add(tmp_1);
        
		preferencesPanel.setLayout(new BorderLayout());
		preferencesPanel.add(tmp_2, BorderLayout.NORTH);
        
		setFont_PrefPanel_Button.addActionListener(
                                                   new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int fnt_size = ((Integer) fontSize_PrefPanel_Spinner.getValue()).intValue();
                globalFont = new Font(globalFont.getFamily(), globalFont.getStyle(), fnt_size);
                setFontForAll(globalFont);
                int h = getRingbpmviewerWindow().getHeight();
                int w = getRingbpmviewerWindow().getWidth();
                getRingbpmviewerWindow().validate();
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
	}
    
    
	/**
	 *  Description of the Method
	 */
	private void cleanMessageTextField() {
		messageTextLocal.setText(null);
		messageTextLocal.setForeground(Color.red);
	}
    
    
	/**
	 *  Sets the fontForAll attribute of the RingbpmviewerDocument object
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
		ringBPMsController.setFont(fnt);
		ringBPMsWaveFormController.setFont(fnt);
		hebtBPMsController.setFont(fnt);
		rtbtBPMsController.setFont(fnt);
        
		timeDealyUC_Label.setFont(fnt);
		timeDealyUC_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) timeDealyUC_Spinner.getEditor()).getTextField().setFont(fnt);
	}
    
    
	/**
	 *  Sets the activePanel attribute of the RingbpmviewerDocument object
	 *
	 *@param  newActPanelInd  The new activePanel value
	 */
	private void setActivePanel(int newActPanelInd) {
		int oldActPanelInd = ACTIVE_PANEL;
        
		if(oldActPanelInd == newActPanelInd) {
			return;
		}
        
		//shut up active panel
		if(oldActPanelInd == VIEW_RING_BPM_PANEL) {
			//action before view values panel will disappear
		} else if(oldActPanelInd == VIEW_WAVEFORM_PANEL) {
			//action before waveform panel will disappear
			ringBPMsWaveFormController.setShowing(false);
		} else if(oldActPanelInd == VIEW_HEBT_BPM_PANEL) {
			//action before view HEBT panel will disappear
		} else if(oldActPanelInd == VIEW_RTBT_BPM_PANEL) {
			//action before view RTBT panel will disappear
		} else if(oldActPanelInd == PREFERENCES_PANEL) {
			//action before preferences panel will disappear
		}
        
		//make something before the new panel will show up
		if(newActPanelInd == VIEW_RING_BPM_PANEL) {
			//action before view values panel will show up
		} else if(newActPanelInd == VIEW_WAVEFORM_PANEL) {
			//action before waveform pane will show up
			ringBPMsWaveFormController.setShowing(true);
			ringBPMsWaveFormController.updateWFsetOnGraphs();
		} else if(newActPanelInd == VIEW_RTBT_BPM_PANEL) {
			//action before view RTBT panel will show up
		} else if(newActPanelInd == PREFERENCES_PANEL) {
			//action before preferences panel will show up
		} else if(newActPanelInd == PREFERENCES_PANEL) {
			//action before preferences pane will show u
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

