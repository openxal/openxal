/*
 *  ScanDocument1D.java
 *
 *  Created on July 31, 2003, 10:25 AM
 */
package xal.app.emittanceanalysis;

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
import java.util.prefs.*;

import xal.ca.*;
import xal.extension.widgets.plot.*;
import xal.extension.application.*;
import xal.extension.application.util.*;
import xal.tools.xml.*;
import xal.extension.widgets.apputils.*;
import xal.extension.widgets.swing.*;

import xal.app.emittanceanalysis.rawdata.*;
import xal.app.emittanceanalysis.analysis.*;

/**
 *  EmittanceDocument is a custom XalDocument for Emittance Analysis
 *  application. The document manages the data that is displayed in the window.
 *
 *@author     shishlo
 *@version    1.0
 */

public class EmittanceDocument extends XalDocument {

	//static {
	//    ChannelFactory.defaultFactory().init();
	//}

	//message text field. It is actually message text field
	//from main window
	private JTextField messageTextLocal = new JTextField();

	//------------------------------------------------------
	//actions
	//------------------------------------------------------
	private Action setRawEmittancePanelAction = null;
	private Action setAnalysisPanelAction = null;
	private Action setPredefConfigAction = null;

	//---------------------------------------------
	//raw emittance panel
	//---------------------------------------------
	private JPanel rawEmittancePanel = null;
	private RawDataPanel rawDataPanel = new RawDataPanel();

	//---------------------------------------------
	//analysis panel
	//---------------------------------------------
	private JPanel analysisPanel = null;
	private AnalysisController analysisController = new AnalysisController();

	//-------------------------------------------------------------
	//PREFERENCES_PANEL and GUI elements, actions etc.
	//-------------------------------------------------------------
	private JPanel preferencesPanel = new JPanel();

	private TitledBorder fontSize_PrefPanel_Bborder = null;
	private JButton setFont_PrefPanel_Button = new JButton("Set Font Size");
	private JSpinner fontSize_PrefPanel_Spinner = new JSpinner(new SpinnerNumberModel(7, 7, 26, 1));
	private Font globalFont = new Font("Monospaced", Font.PLAIN, 10);

	private TitledBorder dataDir_PrefPanel_Bborder = null;
	private JTextField emittDataDirectory_Text = new JTextField(50);
	private JButton browseDataDir_PrefPanel_Button = new JButton("Browse ...");
	private JButton setDataDir_PrefPanel_Button = new JButton("Set Directory as Default");
	private String emtDefaultDirKey = "emittance_data_default_dir";

	//------------------------------------------------
	//PREDEFINED CONFIGURATION PANEL
	//------------------------------------------------
	private PredefinedConfController predefinedConfController = null;
	private JPanel configPanel = null;

	//------------------------------------------------
	//PANEL STATE
	//------------------------------------------------
	private int ACTIVE_PANEL = 0;
	private int RAW_EMITTANCE_PANEL = 0;
	private int ANALYSIS_PANEL = 1;
	private int PREFERENCES_PANEL = 2;
	private int PREDEF_CONF_PANEL = 3;

	//-------------------------------------
	//time and date related member
	//-------------------------------------
	private static DateAndTimeText dateAndTime = new DateAndTimeText();

	//------------------------------------------
	//SAVE RESTORE PART
	//------------------------------------------
	//root node name
	private String dataRootName = "EMITTANCE_ANALYSIS";

	//Preferences for this package
	private Preferences preferences = null;


	/**
	 *  Create a new empty EmittanceDocument
	 */
	public EmittanceDocument() {

		ACTIVE_PANEL = RAW_EMITTANCE_PANEL;

		//make all panels
		makeRawEmittancePanel();
		makeAnalysisPanel();
		makePreferencesPanel();
		makePredefinedConfigurationsPanel();

	}


	/**
	 *  Create a new document loaded from the URL file
	 *
	 *@param  url  The URL of the file to load into the new document.
	 */
	public EmittanceDocument(URL url) {
		this();
		if (url == null) {
			return;
		}
		setSource(url);
		readEmittanceAnalysisDocument(url);

		//super class method - will show "Save" menu active
		if (url.getProtocol().equals("jar")) {
			return;
		}
		setHasChanges(true);
	}


	/**
	 *  Make a main window by instantiating the EmittanceWindow window.
	 */
	public void makeMainWindow() {
		mainWindow = new EmittanceWindow(this);
		//---------------------------------------------------------------
		//this is the place for initializing initial state of main window
		//---------------------------------------------------------------

		//define initial state of the window
		getEmittanceWindow().setJComponent(rawEmittancePanel);

		//set connections between message texts
		messageTextLocal = getEmittanceWindow().getMessageTextField();

		//set all text messages for sub frames
		rawDataPanel.setMessageTextField(messageTextLocal);
		analysisController.setMessageTextField(messageTextLocal);

		fontSize_PrefPanel_Spinner.setValue(new Integer(globalFont.getSize()));
		setFontForAll(globalFont);

		//set connections for  message text in selection of config. panel
		predefinedConfController.setMessageTextField(getEmittanceWindow().getMessageTextField());

		//set timer
		JToolBar toolbar = getEmittanceWindow().getToolBar();
		JTextField timeTxt_temp = dateAndTime.getNewTimeTextField();
		timeTxt_temp.setHorizontalAlignment(JTextField.CENTER);
		toolbar.add(timeTxt_temp);

		mainWindow.setSize(new Dimension(700, 600));
	}


	/**
	 *  Dispose of EmittanceDocument resources. This method overrides an empty
	 *  superclass method.
	 */
	public void freeCustomResources() {
		cleanUp();
	}


	/**
	 *  Reads the content of the document from the specified URL.
	 *
	 *@param  url  The URL for a file with configuration information
	 */
	public void readEmittanceAnalysisDocument(URL url) {

		//read the document content from the persistent storage

		XmlDataAdaptor readAdp = null;
		readAdp = XmlDataAdaptor.adaptorForUrl(url, false);

		if (readAdp != null) {
			XmlDataAdaptor emittData_Adaptor = (XmlDataAdaptor) readAdp.childAdaptor(dataRootName);
			if (emittData_Adaptor != null) {
				cleanUp();
				int font_size = emittData_Adaptor.intValue("font_size");
				globalFont = new Font(globalFont.getName(), globalFont.getStyle(), font_size);
				fontSize_PrefPanel_Spinner.setValue(new Integer(globalFont.getSize()));
				setFontForAll(globalFont);

				if (emittData_Adaptor.hasAttribute("title")) {
					setTitle(emittData_Adaptor.stringValue("title"));
				} else {
					setTitle(url.getFile());
				}

				rawDataPanel.setDataFromXML(emittData_Adaptor);
			}
		}
	}


	/**
	 *  Save the EmittanceDocument document to the specified URL.
	 *
	 *@param  url  The URL for file where the information will be stored
	 */
	public void saveDocumentAs( final URL url ) {
		//this is the place to write document to the persistent storage

		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
		XmlDataAdaptor emittData_Adaptor = (XmlDataAdaptor) da.createChild(dataRootName);

		//title can be there, but you have to put it in the xml file by hand
		//emittData_Adaptor.setValue( "title", getTitle() );

		emittData_Adaptor.setValue("font_size", globalFont.getSize());
		rawDataPanel.dumpDataToXML(emittData_Adaptor);

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
	 *  Convenience method for getting the EmittanceWindow window. It is the cast
	 *  to the proper subclass of XalWindow. This allows me to avoid casting the
	 *  window every time I reference it.
	 *
	 *@return    The main window cast to its dynamic runtime class
	 */
	private EmittanceWindow getEmittanceWindow() {
		return (EmittanceWindow) mainWindow;
	}


	/**
	 *  Register actions for the menu items and toolbar.
	 *
	 *@param  commander  The Commander instance
	 */

	public void customizeCommands(Commander commander) {

		// define the "show-raw-emitt-panel" set raw emittance panel action action
		setRawEmittancePanelAction =
			new AbstractAction("show-raw-emitt-panel") {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent event) {
					setActivePanel(RAW_EMITTANCE_PANEL);
				}
			};
		commander.registerAction(setRawEmittancePanelAction);

		// define the "show-analysis-panel" set raw emittance appearance panel action
		setAnalysisPanelAction =
			new AbstractAction("show-analysis-panel") {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent event) {
					setActivePanel(ANALYSIS_PANEL);
				}
			};
		commander.registerAction(setAnalysisPanelAction);

		setPredefConfigAction =
			new AbstractAction("set-predef-config") {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent event) {
					setActivePanel(PREDEF_CONF_PANEL);
				}
			};
		commander.registerAction(setPredefConfigAction);

	}


	/**
	 *  Creates rawDataPanel instance
	 */
	private void makeRawEmittancePanel() {
		rawEmittancePanel = rawDataPanel.getJPanel();
	}


	/**
	 *  Creates an analysis panel
	 */
	private void makeAnalysisPanel() {
		analysisPanel = analysisController.getAnalysisPanel();
		analysisController.setRawDataPanel(rawDataPanel);
	}


	/**
	 *  Creates a preference panel
	 */
	private void makePreferencesPanel() {
		Border etchedBorder = BorderFactory.createEtchedBorder();

		JPanel fntSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		fontSize_PrefPanel_Bborder =
				BorderFactory.createTitledBorder(etchedBorder, "Set Font Size");
		fntSizePanel.setBorder(fontSize_PrefPanel_Bborder);

		fontSize_PrefPanel_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

		fntSizePanel.add(fontSize_PrefPanel_Spinner);
		fntSizePanel.add(setFont_PrefPanel_Button);

		JPanel setEmtDataDirPanel = new JPanel(new BorderLayout());

		dataDir_PrefPanel_Bborder =
				BorderFactory.createTitledBorder(etchedBorder, "Set Default Raw Emittance Data Path");
		setEmtDataDirPanel.setBorder(dataDir_PrefPanel_Bborder);

		JPanel setEmtDataDirPanel_0 = new JPanel(new BorderLayout());
		setEmtDataDirPanel_0.add(emittDataDirectory_Text, BorderLayout.CENTER);
		setEmtDataDirPanel_0.add(browseDataDir_PrefPanel_Button, BorderLayout.WEST);

		JPanel setEmtDataDirPanel_1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
		setEmtDataDirPanel_1.add(setDataDir_PrefPanel_Button);

		setEmtDataDirPanel.add(setEmtDataDirPanel_0, BorderLayout.NORTH);
		setEmtDataDirPanel.add(setEmtDataDirPanel_1, BorderLayout.SOUTH);

		preferences = Preferences.userNodeForPackage(this.getClass());

		String defDir = preferences.get(emtDefaultDirKey, null);
		emittDataDirectory_Text.setText(defDir);

		if(defDir != null){
		rawDataPanel.setRawDataDirectory(defDir);
		}
		
		JPanel tmpPanel = new JPanel(new BorderLayout());
		tmpPanel.add(fntSizePanel, BorderLayout.NORTH);
		tmpPanel.add(setEmtDataDirPanel, BorderLayout.SOUTH);

		preferencesPanel.setLayout(new BorderLayout());
		preferencesPanel.add(tmpPanel, BorderLayout.NORTH);

		//define actions
		setFont_PrefPanel_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int fnt_size = ((Integer) fontSize_PrefPanel_Spinner.getValue()).intValue();
					globalFont = new Font(globalFont.getFamily(), globalFont.getStyle(), fnt_size);
					setFontForAll(globalFont);
				}
			});

		browseDataDir_PrefPanel_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(emittDataDirectory_Text.getText());
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = chooser.showDialog(getEmittanceWindow(), "Set Dir");
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						//System.out.println("You chose to open this file: " +
						//		chooser.getSelectedFile().getAbsolutePath());
						emittDataDirectory_Text.setText(chooser.getSelectedFile().getAbsolutePath());
					}
				}
			});

		setDataDir_PrefPanel_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					preferences.put(emtDefaultDirKey, emittDataDirectory_Text.getText());
					try {
						preferences.flush();
						rawDataPanel.setRawDataDirectory(emittDataDirectory_Text.getText());
					} catch (BackingStoreException exept) {
					}
				}
			});
	}


	/**
	 *  Creates a predifined configuration panel
	 */
	private void makePredefinedConfigurationsPanel() {
		predefinedConfController = new PredefinedConfController("config","predefinedConfiguration.emt");
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
					readEmittanceAnalysisDocument(url);

					//super class method - will show "Save" menu unactive
					setHasChanges(false);

					setFontForAll(globalFont);

					setActivePanel(RAW_EMITTANCE_PANEL);
				}
			};
		predefinedConfController.setSelectorListener(selectConfListener);
	}


	/**
	 *  Cleans up the configuration of the application
	 */
	private void cleanUp() {
		cleanMessageTextField();
		analysisController.initialize();
	}


	/**
	 *  Cleans the message text field
	 */
	private void cleanMessageTextField() {
		messageTextLocal.setText(null);
		messageTextLocal.setForeground(Color.red);
	}


	/**
	 *  Sets the font for all component
	 *
	 *@param  fnt  The new font
	 */
	private void setFontForAll(Font fnt) {
		messageTextLocal.setFont(fnt);
		rawDataPanel.setFontForAll(fnt);
		analysisController.setFontForAll(fnt);
		fontSize_PrefPanel_Spinner.setValue(new Integer(fnt.getSize()));
		((JSpinner.DefaultEditor) fontSize_PrefPanel_Spinner.getEditor()).getTextField().setFont(fnt);
		predefinedConfController.setFontsForAll(fnt);
		setFont_PrefPanel_Button.setFont(fnt);

		fontSize_PrefPanel_Bborder.setTitleFont(fnt);
		dataDir_PrefPanel_Bborder.setTitleFont(fnt);

		emittDataDirectory_Text.setFont(fnt);
		browseDataDir_PrefPanel_Button.setFont(fnt);
		setDataDir_PrefPanel_Button.setFont(fnt);

		globalFont = fnt;
	}


	/**
	 *  Sets the active panel
	 *
	 *@param  newActPanelInd  The new active panel index
	 */
	private void setActivePanel(int newActPanelInd) {
		int oldActPanelInd = ACTIVE_PANEL;

		if (oldActPanelInd == newActPanelInd) {
			return;
		}

		//shut up active panel
		if (oldActPanelInd == RAW_EMITTANCE_PANEL) {
			rawDataPanel.goingShowOff();
		} else if (oldActPanelInd == ANALYSIS_PANEL) {
		} else if (oldActPanelInd == PREFERENCES_PANEL) {
		} else if (oldActPanelInd == PREDEF_CONF_PANEL) {
		}

		//make something before the new panel will show up
		if (newActPanelInd == RAW_EMITTANCE_PANEL) {
			rawDataPanel.goingShowUp();
			getEmittanceWindow().setJComponent(rawEmittancePanel);
		} else if (newActPanelInd == ANALYSIS_PANEL) {
			analysisController.goingShowUp();
			getEmittanceWindow().setJComponent(analysisPanel);
		} else if (newActPanelInd == PREFERENCES_PANEL) {
			getEmittanceWindow().setJComponent(preferencesPanel);
		} else if (newActPanelInd == PREDEF_CONF_PANEL) {
			getEmittanceWindow().setJComponent(configPanel);
		}

		ACTIVE_PANEL = newActPanelInd;

		cleanMessageTextField();
	}
}

//----------------------------------------------
//Class deals with date and time
//----------------------------------------------
/**
 *  This class provides text field with constantly updated time and date
 *
 *@author     shishlo
 *@version    1.0
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
	 *  Returns the time as a string
	 *
	 *@return    The time string
	 */
	protected String getTime() {
		return dateTimeField.getText();
	}


	/**
	 *  Returns the text field with time and date
	 *
	 *@return    The time text field
	 */
	protected JFormattedTextField getTimeTextField() {
		return dateTimeField;
	}


	/**
	 *  Returns the new time text field
	 *
	 *@return    The new time text field
	 */
	protected JTextField getNewTimeTextField() {
		JTextField newText = new JTextField();
		newText.setDocument(dateTimeField.getDocument());
		newText.setEditable(false);
		return newText;
	}
}

