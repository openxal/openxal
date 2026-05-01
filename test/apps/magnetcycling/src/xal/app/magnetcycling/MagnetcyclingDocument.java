/*
 *  MagnetcyclingDocument.java
 *
 *  Created on September 18, 2006
 */
package xal.app.magnetcycling;

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

import xal.extension.scan.UpdatingEventController;


/**
 *  MagnetcyclingDocument is a custom XalDocument for Magnetcycling application.
 *  The document manages the data that is displayed in the window.
 *
 *@author     shishlo
 */

public class MagnetcyclingDocument extends XalDocument {

	static {
		ChannelFactory.defaultFactory().init();
	}

	//message text field. It is actually message text field from
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
	private RunnerController runnerController = null;

	private ContentController contentController = null;

	//------------------------------------------
	//SAVE RESTORE PART
	//------------------------------------------
	//root node name
	private String dataRootName = "MAGNETS_CYCLING";


	/**
	 *  Create a new empty MagnetcyclingDocument
	 */
	public MagnetcyclingDocument() {
		updatingController.setUpdateTime(1.0);

		//controllers of the panels
		runnerController = new RunnerController();
		firstPanel = runnerController.getPanel();

		contentController = new ContentController();
		secondPanel = contentController.getPanel();

		runnerController.setContentController(contentController);

		mainTabbedPanel.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					JTabbedPane tbp = (JTabbedPane) e.getSource();
					setActivePanel(tbp.getSelectedIndex());
				}
			});

		//make all panels
		makePreferencesPanel();

		mainTabbedPanel.add("Cycler", firstPanel);
		mainTabbedPanel.add("Content", secondPanel);
		mainTabbedPanel.add("Preferences", preferencesPanel);

		mainTabbedPanel.setSelectedIndex(0);

		//debug lines ========= START ===============
		//PowerSupplyGroup psg = new PowerSupplyGroup();
		//psg.setName("all");
		//runnerController.addPowerSupplyGroup(psg);
		//debug lines ========= STOP ===============

	}


	/**
	 *  Create a new document loaded from the URL file
	 *
	 *@param  url  The URL of the file to load into the new document.
	 */
	public MagnetcyclingDocument(URL url) {
		this();
		if(url == null) {
			return;
		}
		setSource(url);
		readMagnetcyclingDocument(url);

		//super class method - will show "Save" menu active
		if(url.getProtocol().equals("jar")) {
			return;
		}
		setHasChanges(true);
	}


	/**
	 *  Make a main window by instantiating the MagnetcyclingWindow window.
	 */
	public void makeMainWindow() {
		mainWindow = new MagnetcyclingWindow(this);
		//---------------------------------------------------------------
		//this is the place for initializing initial state of main window
		//---------------------------------------------------------------

		//define initial state of the window
		getMagnetcyclingWindow().setJComponent(mainTabbedPanel);

		//set connections between message texts
		messageTextLocal = getMagnetcyclingWindow().getMessageTextField();

		//set all text messages for sub frames
		runnerController.getMessageText().setDocument(messageTextLocal.getDocument());
		contentController.getMessageText().setDocument(messageTextLocal.getDocument());

		fontSize_PrefPanel_Spinner.setValue(new Integer(globalFont.getSize()));
		setFontForAll(globalFont);

		timeDealyUC_Spinner.setValue(new Double(updatingController.getUpdateTime()));
		updatingController.setUpdateTime(((Double) timeDealyUC_Spinner.getValue()).doubleValue());

		//set timer
		JTextField timeTxt_temp = dateAndTime.getNewTimeTextField();
		timeTxt_temp.setHorizontalAlignment(JTextField.CENTER);
		getMagnetcyclingWindow().addTimeStamp(timeTxt_temp);

		mainWindow.setSize(new Dimension(800, 600));
	}


	/**
	 *  Dispose of MagnetcyclingDocument resources. This method overrides an empty
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
	public void readMagnetcyclingDocument(URL url) {

		//read the document content from the persistent storage

		XmlDataAdaptor readAdp = null;
		readAdp = XmlDataAdaptor.adaptorForUrl(url, false);

		if(readAdp != null) {
			XmlDataAdaptor magnetcyclingData_Adaptor = (XmlDataAdaptor) readAdp.childAdaptor(dataRootName);
			if(magnetcyclingData_Adaptor != null) {
				cleanUp();
				setTitle(magnetcyclingData_Adaptor.stringValue("title"));

				//set font
				XmlDataAdaptor params_font = (XmlDataAdaptor) magnetcyclingData_Adaptor.childAdaptor("font");
				int font_size = params_font.intValue("size");
				int style = params_font.intValue("style");
				String font_Family = params_font.stringValue("name");
				globalFont = new Font(font_Family, style, font_size);
				fontSize_PrefPanel_Spinner.setValue(new Integer(font_size));
				setFontForAll(globalFont);

				XmlDataAdaptor params_da = (XmlDataAdaptor) magnetcyclingData_Adaptor.childAdaptor("shared_parameters");
				updatingController.setUpdateTime(params_da.doubleValue("update_time"));

				//write the data about power supplys into the data adaptor
				runnerController.readData(magnetcyclingData_Adaptor);

			}
		}
	}


	/**
	 *  Save the MagnetcyclingDocument document to the specified URL.
	 *
	 *@param  url  Description of the Parameter
	 */
	public void saveDocumentAs(URL url) {
		//this is the place to write document to the persistent storage

		XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
		XmlDataAdaptor magnetcyclingData_Adaptor = (XmlDataAdaptor) da.createChild(dataRootName);
		magnetcyclingData_Adaptor.setValue("title", url.getFile());

		//dump parameters
		XmlDataAdaptor params_font = (XmlDataAdaptor) magnetcyclingData_Adaptor.createChild("font");
		params_font.setValue("name", globalFont.getFamily());
		params_font.setValue("style", globalFont.getStyle());
		params_font.setValue("size", globalFont.getSize());

		XmlDataAdaptor params_da = (XmlDataAdaptor) magnetcyclingData_Adaptor.createChild("shared_parameters");
		params_da.setValue("update_time", updatingController.getUpdateTime());

		//write the data about power supplys into the data adaptor
		runnerController.writeData(magnetcyclingData_Adaptor);

		//dump data into the file
		try {
			magnetcyclingData_Adaptor.writeTo(new File(url.getFile()));
			//super class method - will show "Save" menu active
			setHasChanges(true);
		} catch(IOException e) {
			System.out.println("IOException e=" + e);
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
	 *  Convenience method for getting the MagnetcyclingWindow window. It is the
	 *  cast to the proper subclass of XalWindow. This allows me to avoid casting
	 *  the window every time I reference it.
	 *
	 *@return    The main window cast to its dynamic runtime class
	 */
	private MagnetcyclingWindow getMagnetcyclingWindow() {
		return (MagnetcyclingWindow) mainWindow;
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
					int h = getMagnetcyclingWindow().getHeight();
					int w = getMagnetcyclingWindow().getWidth();
					getMagnetcyclingWindow().validate();
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
	 *  Sets the fontForAll attribute of the MagnetcyclingDocument object
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
		runnerController.setFontForAll(fnt);
		contentController.setFontForAll(fnt);

		timeDealyUC_Label.setFont(fnt);
		timeDealyUC_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) timeDealyUC_Spinner.getEditor()).getTextField().setFont(fnt);
	}


	/**
	 *  Sets the activePanel attribute of the MagnetcyclingDocument object
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
			runnerController.isGoingToShowUp();
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

