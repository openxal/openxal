package xal.app.emittanceanalysis.rawdata;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.net.*;

import xal.tools.xml.*;
import xal.extension.widgets.swing.*;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.apputils.*;
import xal.tools.apputils.*;

/**
 *  This class handles the raw data panel for the emittance analysis. This panel
 *  for all raw data manipulations that have to produce emittance data for
 *  further analysis. It includes the following sub-panels: "reading raw data";
 *  "plot raw data"; "filter raw data" and "plot emittance from raw data". They
 *  are provided by the following classes: ReadRawDataPanel; PlotRawDataPanel;
 *  FilterRawDataPanel and MakeRawToEmittancePanel. The panel of
 *  MakeRawToEmittancePanel class is composite panel that includes several
 *  sub-panel controlling the creation of the emittance data. In addition to the
 *  control sub-panels the RawDataPanel's panel includes graph panel to display
 *  waveform of the harp wires signals on the right-upper corner of the panel.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public final class RawDataPanel {

	private String xmlName = "RAW_DATA_PANEL";

	//message text field
	private JTextField messageTextLocal = new JTextField();

	//raw data object
	private WireRawData rawData = new WireRawData();

	private String raw_data_file_name = "";

	//read data sub-panel
	private ReadRawDataPanel readRawDataPanel = null;

	//plot raw data sub-panel
	private PlotRawDataPanel plotRawDataPanel = null;

	//make emittance data from raw data sub-panel
	private MakeRawToEmittancePanel makeRawToEmittancePanel = null;

	//filter raw data sub-panel
	private FilterRawDataPanel filterRawDataPanel = null;

	//listener for new set of data or generation of the new raw emittance.
	//It is used for analyses initialization.
	private ActionListener initializationAnalysisListener = null;
	private ActionEvent iniAnalysisEvent = null;

	//-----------------------------------
	//GUI elements
	//-----------------------------------

	//Raw Data Main Panel by inself
	private JPanel panel = new JPanel();
	private TitledBorder border = null;

	//graph panel for signal(sample) functions
	private FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

	//default raw data directory
	private String rawDataDirectoryLocation = "";


	/**
	 *  RawDataPanel constructor.
	 */
	public RawDataPanel() {
		initiate();
	}


	/**
	 *  Description of the Method
	 */
	private void initiate() {

		//Graph panel definition
		SimpleChartPopupMenu.addPopupMenuTo(GP);
		GP.setOffScreenImageDrawing(true);
		GP.setGraphBackGroundColor(Color.white);

		GP.setName("Wires' Signals. File: ");
		GP.setAxisNames("sample index", "signal, a.u.");
		GP.setNumberFormatX(new DecimalFormat("##0"));
		GP.setNumberFormatY(new DecimalFormat("0.0#"));

		GP.setLimitsAndTicksX(0., 50., 4, 4);
		GP.setLimitsAndTicksY(-1.0, 0.5, 4, 4);

		readRawDataPanel = new ReadRawDataPanel(rawData);
		plotRawDataPanel = new PlotRawDataPanel(rawData, GP);
		filterRawDataPanel = new FilterRawDataPanel(GP);
		makeRawToEmittancePanel = new MakeRawToEmittancePanel(GP);

		rawData.setMessageTextField(messageTextLocal);
		makeRawToEmittancePanel.setMessageTextField(messageTextLocal);

		makeRawToEmittancePanel.setWireRawData(rawData);
		makeRawToEmittancePanel.setFilterRawDataPanel(filterRawDataPanel);
		makeRawToEmittancePanel.setReadRawDataPanel(readRawDataPanel);
		makeRawToEmittancePanel.setPlotRawDataPanel(plotRawDataPanel);

		//panel border
		Border etchedBorder = BorderFactory.createEtchedBorder();
		border = BorderFactory.createTitledBorder(etchedBorder, "RAW DATA PANEL");
		panel.setBorder(border);
		panel.setLayout(new BorderLayout());

		//left sub-panel
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new VerticalLayout());
		leftPanel.add(readRawDataPanel.getJPanel());
		leftPanel.add(plotRawDataPanel.getJPanel());
		leftPanel.add(filterRawDataPanel.getJPanel());

		//create grap panel
		JPanel graphPanel = new JPanel();
		graphPanel.setBorder(etchedBorder);
		graphPanel.setLayout(new BorderLayout());
		graphPanel.add(GP, BorderLayout.CENTER);

		//upper half sub-panel
		JPanel upperPanel = new JPanel();
		upperPanel.setBorder(etchedBorder);
		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(leftPanel, BorderLayout.WEST);
		upperPanel.add(graphPanel, BorderLayout.CENTER);

		//add all componenet to the MAIN panel
		panel.add(upperPanel, BorderLayout.NORTH);
		panel.add(makeRawToEmittancePanel.getJPanel(), BorderLayout.CENTER);

		//event of new set of raw data has been read
		iniAnalysisEvent = new ActionEvent(this, 0, "READ_NEW_DATA");

		//define action after reading data
		readRawDataPanel.addReadDataActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GP.setName("Wires' Signals. File: " + readRawDataPanel.getFileName());
					raw_data_file_name = readRawDataPanel.getFileName();
					int nSamples = rawData.getSamplesNumber();
					int samplesStep = nSamples / 50;
					if ((nSamples % 50) > 0) {
						samplesStep++;
					}
					GP.setLimitsAndTicksX(0., 50., samplesStep, 4);
					plotRawDataPanel.setSpinnerModels(rawData.getChannelsNumber(), rawData.getPositionsNumberSlit(), rawData.getPositionsNumberHarp());
					plotRawDataPanel.setDefaultSpinnersValues();
					plotRawDataPanel.plotRawData();

					if (initializationAnalysisListener != null) {
						initializationAnalysisListener.actionPerformed(iniAnalysisEvent);
					}

					makeRawToEmittancePanel.initAfterReading();

				}
			});

		//define action after index of an emittance device type setting
		readRawDataPanel.addSetIndexActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					ReadRawDataPanel rdp = (ReadRawDataPanel) e.getSource();
					int index = rdp.getTypeIndex();
					//makeRawToEmittancePanel actions
					makeRawToEmittancePanel.setTypeDataIndex(index);
				}
			});

		//set fonts
		setFontForAll(new Font("Monospaced", Font.PLAIN, 10));

		//set index that define type of emittance device (MEBT, DTL etc)
		readRawDataPanel.setTypeIndex(3);

	}


	/**
	 *  Sets the initialization listener from analysis controller
	 *
	 *@param  al  The new action listener
	 */
	public void setInitializationAnalysisListener(ActionListener al) {
		initializationAnalysisListener = al;
		makeRawToEmittancePanel.setInitializationAnalysisListener(al);
	}


	/**
	 *  Returns the gammaBeta value from MakeRawToEmittancePanel
	 *
	 *@return    The gammaBeta value
	 */
	public double getGammaBeta() {
		return makeRawToEmittancePanel.getGammaBeta();
	}


	/**
	 *  Sets the configuration of the panel according to information in the XML
	 *  structure.
	 *
	 *@param  configIn  The XML data adapter with the configuration information
	 */
	public void configure(XmlDataAdaptor configIn) {
	}


	/**
	 *  Saves the configuration of the panel to the XML structure.
	 *
	 *@param  configOut  The XML data adapter for the configuration information
	 */
	public void dumpConfigure(XmlDataAdaptor configOut) {
	}


	/**
	 *  Sets all fonts.
	 *
	 *@param  fnt  The new font
	 */
	public void setFontForAll(Font fnt) {
		readRawDataPanel.setFontForAll(fnt);
		plotRawDataPanel.setFontForAll(fnt);
		filterRawDataPanel.setFontForAll(fnt);
		makeRawToEmittancePanel.setFontForAll(fnt);

		border.setTitleFont(fnt);
	}


	/**
	 *  Returns the JPanel of this component.
	 *
	 *@return    The JPanel of this class
	 */
	public JPanel getJPanel() {
		return panel;
	}


	/**
	 *  Performs actions before show the panel.
	 */
	public void goingShowUp() {
		makeRawToEmittancePanel.goingShowUp();
	}


	/**
	 *  Performs actions before hide the panel.
	 */
	public void goingShowOff() {
	}


	 
	/**
	 *  Returns the string identifier in the XML structure.
	 *
	 *@return    The name of XML sub-tree
	 */
	public String getNameXMLData() {
		return xmlName;
	}


	/**
	 *  Writes the configuration information into the xml data file.
	 *
	 *@param  rawDataPanelDataParent  The XML data adaptor
	 */
	public void dumpDataToXML(XmlDataAdaptor rawDataPanelDataParent) {
		XmlDataAdaptor rawDataPanelData = (XmlDataAdaptor) rawDataPanelDataParent.createChild(getNameXMLData());

		rawDataPanelData.setValue("raw_data_file_name", raw_data_file_name);

		File rawDataFile = readRawDataPanel.getFile();
		if (rawDataFile != null) {
			rawDataPanelData.setValue("RAW_DATA_URI", rawDataFile.toURI().toString());
		}

		rawDataPanelData.setValue("typeIndex", readRawDataPanel.getTypeIndex());

		readRawDataPanel.dumpDataToXML(rawDataPanelData);
		plotRawDataPanel.dumpDataToXML(rawDataPanelData);
		filterRawDataPanel.dumpDataToXML(rawDataPanelData);
		makeRawToEmittancePanel.dumpDataToXML(rawDataPanelData);

	}


	/**
	 *  Configures the raw data panel according the data from the xml data file.
	 *
	 *@param  rawDataPanelDataParent  The XML data adapter with configuration
	 *      information
	 */
	public void setDataFromXML(XmlDataAdaptor rawDataPanelDataParent) {
		XmlDataAdaptor rawDataPanelData = (XmlDataAdaptor) rawDataPanelDataParent.childAdaptor(getNameXMLData());
		if (rawDataPanelData == null) {
			return;
		}
		raw_data_file_name = "";
		if (rawDataPanelData.hasAttribute("raw_data_file_name")) {
			raw_data_file_name = rawDataPanelData.stringValue("raw_data_file_name");
			GP.setName("Wires' Signals. File: " + raw_data_file_name);
			String strURI = rawDataPanelData.stringValue("RAW_DATA_URI");
			if (strURI != null) {
				try {
					File rawDataFile = new File(new URI(strURI));

					if (rawDataFile.exists()) {
						readRawDataPanel.setFile(rawDataFile);
					}
				} catch (URISyntaxException ex) {
					readRawDataPanel.setFile(null);
				}
			}
		}

		readRawDataPanel.setTypeIndex(rawDataPanelData.intValue("typeIndex"));

		readRawDataPanel.setDataFromXML(rawDataPanelData);
		plotRawDataPanel.setDataFromXML(rawDataPanelData);
		filterRawDataPanel.setDataFromXML(rawDataPanelData);
		makeRawToEmittancePanel.setDataFromXML(rawDataPanelData);
	}


	/**
	 *  Returns emittance data for analysis. This method delegates request to the
	 *  MakeRawToEmittancePanel class instance
	 *
	 *@return    The emittance data for analysis
	 */
	public ColorSurfaceData getEmittanceData() {
		return makeRawToEmittancePanel.getEmittanceData();
	}


	/**
	 *  Returns the raw data file name.
	 *
	 *@return    The raw data file name
	 */
	public String getRawDataFileName() {
		return raw_data_file_name;
	}


	/**
	 *  Returns the raw data directory location
	 *
	 *@return    The raw data directory location
	 */
	public String getRawDataDirectory() {
		return rawDataDirectoryLocation;
	}

	/**
	 *  Sets the raw data directory the RawDataPanel object
	 *
	 *@param  rawDataDirectoryLocationIn  The new raw data directory 
	 */
	public void setRawDataDirectory(String rawDataDirectoryLocationIn) {
		rawDataDirectoryLocation = rawDataDirectoryLocationIn;
		readRawDataPanel.setRawDataDirectory(rawDataDirectoryLocation);
	}

	/**
	 *  Returns the message text field.
	 *
	 *@return    The text message field
	 */
	public JTextField getMessageTextField() {
		return messageTextLocal;
	}


	/**
	 *  Sets the message text field.
	 *
	 *@param  messageTextLocal  The new message text filed
	 */
	public void setMessageTextField(JTextField messageTextLocal) {
		this.messageTextLocal = messageTextLocal;
		rawData.setMessageTextField(messageTextLocal);
		makeRawToEmittancePanel.setMessageTextField(messageTextLocal);
	}

}

