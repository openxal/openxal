/*
 *  InjDumpWizardDocument.java
 *
 *  Created on October 10, 2007
 */
package xal.app.injdumpwizard;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;


import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import xal.ca.ChannelFactory;

import xal.extension.application.Application;
import xal.extension.application.Commander;
import xal.extension.application.smf.AcceleratorDocument;

import xal.tools.xml.XmlDataAdaptor;
import xal.smf.impl.qualify.KindQualifier;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.BPM;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.WireScanner;
import xal.model.probe.ParticleProbe;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.AlgorithmFactory;
import xal.model.alg.ParticleTracker;

//local packages
import xal.app.injdumpwizard.utils.IDmpBPM_Wrapper;
import xal.app.injdumpwizard.utils.IDmpMagnets_Wrapper;
import xal.app.injdumpwizard.utils.IDmpPositionCalculator;
import xal.app.injdumpwizard.utils.IDmpWS_Wrapper;
import xal.app.injdumpwizard.utils.SwitcherToSimulatedH0;

/**
 *  InjDumpWizardDocument is a custom XalDocument for Injection Dump Wizard
 *  application. The document manages the data that is displayed in the window.
 *
 *@author     shishlo
 */

public class InjDumpWizardDocument extends AcceleratorDocument {

	static {
		ChannelFactory.defaultFactory().init();
	}

	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	//the tabbed panel that will keep all subpanels
	private JTabbedPane mainTabbedPanel = new JTabbedPane();

	//--------------------------------------------------------------
	//The first panel with control elements
	//--------------------------------------------------------------
	private JPanel tabPanel_1 = null;

	//--------------------------------------------------------------
	//The second panel with control elements
	//--------------------------------------------------------------
	private JPanel tabPanel_2 = null;

	//-------------------------------------------------------------
	//PREFERENCES_PANEL and GUI elements, actions etc.
	//-------------------------------------------------------------
	private JPanel preferencesPanel = new JPanel();
	private JButton setFont_PrefPanel_Button = new JButton("Set Font Size");
	private JSpinner fontSize_PrefPanel_Spinner = new JSpinner(new SpinnerNumberModel(7, 7, 26, 1));

	private Font globalFont = new Font("Monospaced", Font.BOLD, 10);

	//------------------------------------------------
	//PANEL STATE
	//------------------------------------------------
	private int ACTIVE_PANEL = 0;
	private int TAB_PANEL_1 = 1;
	private int TAB_PANEL_2 = 1;
	private int PREFERENCES_PANEL = 2;
	//-------------------------------------
	//time and date related member
	//-------------------------------------
	private static DateAndTimeText dateAndTime = new DateAndTimeText();

	//=====================================
	//Controllers of the sub-panels
	//=====================================
	private IDmpWS_Wrapper wsWrapper = new IDmpWS_Wrapper();
	private IDmpBPM_Wrapper bpm00Wrapper = new IDmpBPM_Wrapper();
	private IDmpBPM_Wrapper bpm01Wrapper = new IDmpBPM_Wrapper();
	private IDmpBPM_Wrapper bpm02Wrapper = new IDmpBPM_Wrapper();
	private IDmpBPM_Wrapper bpm03Wrapper = new IDmpBPM_Wrapper();
	private IDmpMagnets_Wrapper magnetsWrapper = new IDmpMagnets_Wrapper();
	private IDmpPositionCalculator positionCalculator = new IDmpPositionCalculator();
	
	private SwitcherToSimulatedH0 switcherToSimulatedH0 = new SwitcherToSimulatedH0();
	
	//------------------------------------------
	//SAVE RESTORE PART
	//------------------------------------------
	//root node name
	private String dataRootName = "INJ_DUMP_WIZARD";


	/**
	 *  Create a new empty InjDumpWizardDocument
	 */
	public InjDumpWizardDocument() {

		//controllers of the panels
		//here they are just JPanels, but in real apps ???
		tabPanel_1 = new JPanel(new BorderLayout());
		tabPanel_2 = new JPanel(new BorderLayout());

		//tab 1
		JPanel tab_1_1_wrappers = new JPanel(new GridLayout(1, 2, 1, 1));
		tab_1_1_wrappers .add(wsWrapper.getJPanel());
		JPanel tab_1_1_1_wrappers = new JPanel(new GridLayout(1, 4, 1, 1));
		tab_1_1_1_wrappers .add(bpm00Wrapper.getJPanel());
		tab_1_1_1_wrappers .add(bpm01Wrapper.getJPanel());
		tab_1_1_1_wrappers .add(bpm02Wrapper.getJPanel());
		tab_1_1_1_wrappers .add(bpm03Wrapper.getJPanel());
		tab_1_1_wrappers .add(tab_1_1_1_wrappers);

		JPanel tab_1_3_wrappers = new JPanel(new BorderLayout());
		tab_1_3_wrappers.add(tab_1_1_wrappers,BorderLayout.NORTH);
		tab_1_3_wrappers.add(positionCalculator.getJPanel(),BorderLayout.CENTER);

		tabPanel_1.add(tab_1_3_wrappers,BorderLayout.CENTER);
		
		//tab 2		
		tabPanel_2.add(switcherToSimulatedH0.getJPanel(),BorderLayout.NORTH);


		mainTabbedPanel.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					JTabbedPane tbp = (JTabbedPane) e.getSource();
					setActivePanel(tbp.getSelectedIndex());
				}
			});

		//make all panels
		makePreferencesPanel();

		mainTabbedPanel.add("Beam Position", tabPanel_1);
		mainTabbedPanel.add("Simulated H0 Beam", tabPanel_2);
		mainTabbedPanel.add("Preferences", preferencesPanel);

		mainTabbedPanel.setSelectedIndex(0);
		
		//reads the BPM transformation table
		readBPMTransformationTable();
	}


	/**
	 *  Create a new document loaded from the URL file
	 *
	 *@param  url  The URL of the file to load into the new document.
	 */
	public InjDumpWizardDocument(URL url) {
		this();
		if(url == null) {
			return;
		}
		setSource(url);
		readInjDumpWizardDocument(url);

		//super class method - will show "Save" menu active
		if(url.getProtocol().equals("jar")) {
			return;
		}
		setHasChanges(true);
	}


	/**
	 *  Make a main window by instantiating the InjDumpWizardWindow window.
	 */
	public void makeMainWindow() {
		mainWindow = new InjDumpWizardWindow(this);
		//---------------------------------------------------------------
		//this is the place for initializing initial state of main window
		//---------------------------------------------------------------

		//define initial state of the window
		getInjDumpWizardWindow().setJComponent(mainTabbedPanel);

		//set connections between message texts
		messageTextLocal = getInjDumpWizardWindow().getMessageTextField();

		//set all text messages for sub frames
		//runnerController.getMessageText().setDocument(messageTextLocal.getDocument());
		wsWrapper.setMessageText( messageTextLocal);
		bpm00Wrapper.setMessageText( messageTextLocal);
		bpm01Wrapper.setMessageText( messageTextLocal);
		bpm02Wrapper.setMessageText( messageTextLocal);
		bpm03Wrapper.setMessageText( messageTextLocal);
		magnetsWrapper.setMessageText( messageTextLocal);
		positionCalculator.setMessageText( messageTextLocal);
		positionCalculator.setWrappers(wsWrapper,bpm00Wrapper,bpm01Wrapper,bpm02Wrapper,bpm03Wrapper,magnetsWrapper);
		
		switcherToSimulatedH0.setMessageText( messageTextLocal);
		
		fontSize_PrefPanel_Spinner.setValue(new Integer(globalFont.getSize()));
		setFontForAll(globalFont);

		//set timer
		JTextField timeTxt_temp = dateAndTime.getNewTimeTextField();
		timeTxt_temp.setHorizontalAlignment(JTextField.CENTER);
		getInjDumpWizardWindow().addTimeStamp(timeTxt_temp);

		mainWindow.setSize(new Dimension(800, 600));

		//activate connections
		loadDefaultAccelerator();
		Accelerator acc = getAccelerator();
		AcceleratorSeq seq = acc.findSequence("IDmp+");

		//wire scaner
        java.util.List<WireScanner> wss = seq.getAllNodesWithQualifier( KindQualifier.qualifierWithStatusAndType( true, WireScanner.s_strType ) );
        WireScanner ws = wss.get(1);
//		java.util.List<ProfileMonitor> wss = seq.<ProfileMonitor>getAllNodesWithQualifier( KindQualifier.qualifierWithStatusAndType( true, ProfileMonitor.s_strType ) );
//		ProfileMonitor ws = wss.get(1);
		wsWrapper.setWS(ws);

		java.util.List<BPM> bpms = seq.<BPM>getAllNodesWithQualifier( KindQualifier.qualifierWithStatusAndType( true, BPM.s_strType ) );
		BPM bpm = bpms.get(0);
		bpm00Wrapper.setBPM(bpm);
		bpm = bpms.get(1);
		bpm01Wrapper.setBPM(bpm);
		bpm = bpms.get(2);
		bpm02Wrapper.setBPM(bpm);
		bpm = bpms.get(3);
		bpm03Wrapper.setBPM(bpm);
		
		java.util.List<Electromagnet> mags = seq.<Electromagnet>getAllNodesWithQualifier( KindQualifier.qualifierWithStatusAndType( true, Electromagnet.s_strType ) );
		Electromagnet quad = mags.get(mags.size()-3);
		Electromagnet dch = mags.get(mags.size()-2);
		Electromagnet dcv = mags.get(mags.size()-1);
		//System.out.println("debug quad ="+quad.getId());
		//System.out.println("debug dch ="+dch.getId());
		//System.out.println("debug dcv ="+dcv.getId());
		magnetsWrapper.setMagnets(quad,dch,dcv);

		try {
			final ParticleTracker tracker = AlgorithmFactory.createParticleTracker( seq );
			final ParticleProbe probe =  ProbeFactory.createParticleProbe( seq, tracker );
			double momentum = probe.getSpeciesRestEnergy()*probe.getBeta()*probe.getGamma();
			positionCalculator.setMomentum(momentum);
			//System.out.println("debug momentum="+momentum);
		}
		catch ( InstantiationException exception ) {
			throw new RuntimeException( "Exception instantiating the model.", exception );
		}
		
		//start monitoring all PVs
		switcherToSimulatedH0.startMonitorPVs();
	}


	/**
	 *  Dispose of InjDumpWizardDocument resources. This method overrides an empty
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
	public void readInjDumpWizardDocument(URL url) {
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

				// CKA - not used
//				XmlDataAdaptor params_da = (XmlDataAdaptor) quadshakerData_Adaptor.childAdaptor("shared_parameters");

				//read the application specific data
				//from quadshakerData_Adaptor
				//???
			}
		}
	}


	/**
	 *  Save the InjDumpWizardDocument document to the specified URL.
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

		// CKA - not used
//		XmlDataAdaptor params_da = (XmlDataAdaptor) quadshakerData_Adaptor.createChild("shared_parameters");

		//write the application specific data
		//into the quadshakerData_Adaptor XmlDataAdaptor
		//???

		//dump data into the file
		try {
			da.writeToUrl( url );

			//super class method - will show "Save" menu active
			setHasChanges( false );
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
	 *  Convenience method for getting the InjDumpWizardWindow window. It is the cast
	 *  to the proper subclass of XalWindow. This allows me to avoid casting the
	 *  window every time I reference it.
	 *
	 *@return    The main window cast to its dynamic runtime class
	 */
	private InjDumpWizardWindow getInjDumpWizardWindow() {
		return (InjDumpWizardWindow) mainWindow;
	}

	/**
	 *  This method will call IDmpPositionCalculator to read BPM transformation table.
	 */	
	 private void readBPMTransformationTable(){
		 final URL dataURL = Application.getAdaptor().getResourceURL( "data/bpm_transf_table.dat" );
		 try {
			 InputStream inps = dataURL.openStream();		
			 positionCalculator.readBPMTransformationTable(inps);
			 inps.close();
		 } catch (IOException exception) {
			 messageTextLocal.setText(null);
			 messageTextLocal.setText("Fatal error. Can not read file with BPM transformation table!" +
				 " Stop execution. Call the developer.");
		 }
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
		
		JPanel tmp_3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_3.add(magnetsWrapper.getMagnetCoeffPanel());
		
		JPanel tmp_4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_4.add(positionCalculator.bpmTableSwitcherButton);	
		
		JPanel tmp_5 = new JPanel(new BorderLayout());
		tmp_5.add(tmp_2, BorderLayout.NORTH);
		tmp_5.add(tmp_3, BorderLayout.CENTER);
		tmp_5.add(tmp_4, BorderLayout.SOUTH);
		
		preferencesPanel.setLayout(new BorderLayout());
		preferencesPanel.add(tmp_5, BorderLayout.NORTH);

		setFont_PrefPanel_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int fnt_size = ((Integer) fontSize_PrefPanel_Spinner.getValue()).intValue();
					globalFont = new Font(globalFont.getFamily(), globalFont.getStyle(), fnt_size);
					setFontForAll(globalFont);
					
					// CKA - not used
//					int h = getInjDumpWizardWindow().getHeight();
//					int w = getInjDumpWizardWindow().getWidth();
					getInjDumpWizardWindow().validate();
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
	 *  Sets the fontForAll attribute of the InjDumpWizardDocument object
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
		wsWrapper.setFontForAll(globalFont);
		bpm00Wrapper.setFontForAll(globalFont);
		bpm01Wrapper.setFontForAll(globalFont);
		bpm02Wrapper.setFontForAll(globalFont);
		bpm03Wrapper.setFontForAll(globalFont);
		magnetsWrapper.setFontForAll(globalFont);
		positionCalculator.setFontForAll(globalFont);
		switcherToSimulatedH0.setFontForAll(globalFont);
	}


	/**
	 *  Sets the activePanel attribute of the InjDumpWizardDocument object
	 *
	 *@param  newActPanelInd  The new activePanel value
	 */
	private void setActivePanel(int newActPanelInd) {
		int oldActPanelInd = ACTIVE_PANEL;

		if(oldActPanelInd == newActPanelInd) {
			return;
		}

		//shut up active panel
		if(oldActPanelInd == TAB_PANEL_1) {
			//action before first panel will disappear
		} else if(oldActPanelInd == TAB_PANEL_2) {
			//action before second panel will disappear
		} else if(oldActPanelInd == PREFERENCES_PANEL) {
			//action before preferences panel will disappear
		}

		//make something before the new panel will show up
		if(newActPanelInd == TAB_PANEL_1) {
			//action before view values panel will show up
		} else if(newActPanelInd == TAB_PANEL_2) {
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

