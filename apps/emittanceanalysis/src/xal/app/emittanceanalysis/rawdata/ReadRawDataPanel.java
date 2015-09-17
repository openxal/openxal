package xal.app.emittanceanalysis.rawdata;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.border.*;
import java.awt.event.*;

import xal.extension.widgets.swing.*;
import xal.tools.xml.*;

/**
 *  This is the sub-panel of RawDataPanel. It reads a raw data file with
 *  waveforms. It includes a combo-box to choose type of the emittance device
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public class ReadRawDataPanel {

	private String xmlName = "READ_RAW_DATA_PANEL";

	//panel by inself
	private JPanel panel = new JPanel();
	private TitledBorder border = null;

	//local data file
	private File dataFile = null;

	//raw data object
	private WireRawData rawData = null;

	//active type
	//0 - MEBT emittance device
	//1 - DTL  emittance device
	//2 - new (fall 2004) MEBT device
	//3 - cvs input file for new (fall 2004) MEBT device
	private int iType = 0;

	//set of different choices
	private int nTypes = 4;

	private String[] typeNames_arr = {"MEBT Emittance Device",
			"DTL Emittance Device", "New (fall 2004) MEBT Emittance Device",
			"CSV File for New MEBT Emittance Device"};

	private int[] nWires_arr = {32, 26, 32, 16};
	private int[] nChannels_arr = {30, 26, 30, 16};

	//format for numbers of rows and channels
	private DecimalFormat numb_Format = new DecimalFormat("###0");

	//listeners for completion of the data reading
	private Vector<ActionListener> readDataListenersV = new Vector<>();

	//listeners for the type index setting completion
	private Vector<ActionListener> setIndexListenersV = new Vector<>();

	//listeners for completion of the data reading

	//-----------------------------------
	//GUI elements
	//-----------------------------------
	private JButton read_Button = new JButton("READ RAW DATA FILE");
	private JRadioButton dataPolarity_Button = new JRadioButton("Data Polarity",true);

	private JLabel nWires_Label = new JLabel(" Wires:");
	private JLabel nChannels_Label = new JLabel(" Channels:");
	private JLabel nPositionsSlit_Label = new JLabel(" Slit Pos.:");
	private JLabel nPositionsHarp_Label = new JLabel(" Harp Pos.:");

	private DoubleInputTextField nWires_Text = new DoubleInputTextField(5);
	private DoubleInputTextField nChannels_Text = new DoubleInputTextField(5);
	private DoubleInputTextField nPositionsSlit_Text = new DoubleInputTextField(5);
	private DoubleInputTextField nPositionsHarp_Text = new DoubleInputTextField(5);

	private JComboBox<String> typeChooser = null;

	//default raw data directory
	private String rawDataDirectoryLocation = "";

	/**
	 *  Constructor for the ReadRawDataPanel object
	 *
	 *@param  rawDataIn  The WireRawData instance
	 */
	public ReadRawDataPanel(WireRawData rawDataIn) {
		rawData = rawDataIn;

		nWires_Text.setNumberFormat(numb_Format);
		nChannels_Text.setNumberFormat(numb_Format);
		nPositionsSlit_Text.setNumberFormat(numb_Format);
		nPositionsHarp_Text.setNumberFormat(numb_Format);

		nWires_Text.setHorizontalAlignment(JTextField.CENTER);
		nChannels_Text.setHorizontalAlignment(JTextField.CENTER);
		nPositionsSlit_Text.setHorizontalAlignment(JTextField.CENTER);
		nPositionsHarp_Text.setHorizontalAlignment(JTextField.CENTER);

		nPositionsSlit_Text.setEditable(false);
		nPositionsSlit_Text.setText(null);
		nPositionsSlit_Text.setBackground(Color.white);

		nPositionsHarp_Text.setEditable(false);
		nPositionsHarp_Text.setText(null);
		nPositionsHarp_Text.setBackground(Color.white);

		nWires_Text.setBackground(Color.white);
		nChannels_Text.setBackground(Color.white);

		nWires_Label.setHorizontalAlignment(SwingConstants.CENTER);
		nChannels_Label.setHorizontalAlignment(SwingConstants.CENTER);
		nPositionsSlit_Label.setHorizontalAlignment(SwingConstants.CENTER);
		nPositionsHarp_Label.setHorizontalAlignment(SwingConstants.CENTER);

		read_Button.setForeground(Color.blue.darker());
		//read_Button.setBackground( Color.cyan );
		read_Button.setBorder(BorderFactory.createRaisedBevelBorder());

		typeChooser = new JComboBox<>(typeNames_arr);
		typeChooser.setBackground(Color.cyan);
		typeChooser.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index = typeChooser.getSelectedIndex();
					setTypeIndex(index);
					makeSetIndexActions();
				}
			});

		read_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					double data_polarity = +1.;
					if(!dataPolarity_Button.isSelected()){
						data_polarity = -1.;
					}
					int nWires = (int) nWires_Text.getValue();
					int nChannels = (int) nChannels_Text.getValue();

					JFileChooser ch = new JFileChooser(rawDataDirectoryLocation);
					ch.setDialogTitle("Read Raw Emittance Data");
					if (dataFile != null) {
						ch.setSelectedFile(dataFile);
					}
					int returnVal = ch.showOpenDialog(panel);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						dataFile = ch.getSelectedFile();
						rawData.getMessageTextField().setText(null);
						if (rawData.readData(dataFile, nWires, nChannels,data_polarity)) {
							nPositionsSlit_Text.setValue((double) rawData.getPositionsNumberSlit());
							nPositionsHarp_Text.setValue((double) rawData.getPositionsNumberHarp());
							//notify listeners that data has been read
							makeReadDataActions();
						}
					}
				}
			});

		//panel border
		Border etchedBorder = BorderFactory.createEtchedBorder();
		border = BorderFactory.createTitledBorder(etchedBorder, "reading raw data");
		panel.setBorder(border);
		panel.setLayout(new BorderLayout());
		panel.setBackground(panel.getBackground().darker());

		JPanel tmp_panel_0 = new JPanel();
		tmp_panel_0.setLayout(new GridLayout(1, 1, 1, 1));
		tmp_panel_0.add(typeChooser);

		JPanel tmp_panel_1 = new JPanel();
		tmp_panel_1.setLayout(new GridLayout(2, 4, 3, 1));
		tmp_panel_1.add(nWires_Label);
		tmp_panel_1.add(nChannels_Label);
		tmp_panel_1.add(nPositionsSlit_Label);
		tmp_panel_1.add(nPositionsHarp_Label);
		tmp_panel_1.add(nWires_Text);
		tmp_panel_1.add(nChannels_Text);
		tmp_panel_1.add(nPositionsSlit_Text);
		tmp_panel_1.add(nPositionsHarp_Text);

		JPanel tmp_panel_2 = new JPanel();
		//tmp_panel_2.setLayout(new GridLayout(1,1,1,1));
		//tmp_panel_2.setBorder(BorderFactory.createEmptyBorder(3,15,3,15));
		tmp_panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		tmp_panel_2.add(read_Button);
		tmp_panel_2.add(dataPolarity_Button);

		panel.add(tmp_panel_0, BorderLayout.NORTH);
		panel.add(tmp_panel_1, BorderLayout.CENTER);
		panel.add(tmp_panel_2, BorderLayout.SOUTH);

		setTypeIndex(3);
	}


	/**
	 *  Sets the font for all GUI elements
	 *
	 *@param  fnt  The new font
	 */
	public void setFontForAll(Font fnt) {
		border.setTitleFont(fnt);

		read_Button.setFont(fnt);
		dataPolarity_Button.setFont(fnt);
		nWires_Label.setFont(fnt);
		nChannels_Label.setFont(fnt);
		nPositionsSlit_Label.setFont(fnt);
		nPositionsHarp_Label.setFont(fnt);
		nWires_Text.setFont(fnt);
		nChannels_Text.setFont(fnt);
		nPositionsSlit_Text.setFont(fnt);
		nPositionsHarp_Text.setFont(fnt);

		typeChooser.setFont(fnt);
		((JTextField) typeChooser.getEditor().getEditorComponent()).setFont(fnt);
		typeChooser.setPreferredSize(new Dimension(1, fnt.getSize() + 10));
	}


	/**
	 *  Returns the JPanel of this class
	 *
	 *@return    The JPanel
	 */
	public JPanel getJPanel() {
		return panel;
	}


	/**
	 *  Returns the name of the file with raw waveform data
	 *
	 *@return    The name of the file
	 */
	public String getFileName() {
		if (dataFile != null) {
			return dataFile.getName();
		}
		return "";
	}


	/**
	 *  Returns the file object with raw waveform data
	 *
	 *@return    The file
	 */
	public File getFile() {
		return dataFile;
	}


	/**
	 *  Sets the file object with raw waveform data
	 *
	 *@param  file  The file
	 */
	public void setFile(File file) {
		dataFile = file;
	}


	/**
	 *  Sets the parameters on the panel according the emittance device type
	 *
	 *@param  ind  The type index of the emittance device
	 */
	public void setTypeIndex(int ind) {
		if (ind >= nTypes) {
			return;
		}
		rawData.setDeviceType(getTypeIndex());
		nWires_Text.setValue((double) nWires_arr[ind]);
		nChannels_Text.setValue((double) nChannels_arr[ind]);
		typeChooser.setSelectedIndex(ind);
	}


	/**
	 *  Returns the type index of the emittance device
	 *
	 *@return    The type index of the emittance device
	 */
	public int getTypeIndex() {
		return typeChooser.getSelectedIndex();
	}


	/**
	 *  Performs action after reading the waveform data file
	 */
	private void makeReadDataActions() {
		ActionEvent e = new ActionEvent(this, 0, "data_ready");
		for (int i = 0; i < readDataListenersV.size(); i++) {
			readDataListenersV.get(i).actionPerformed(e);
		}
	}


	/**
	 *  Adds an ActionListener that listenes to reading the new waveform data file
	 *
	 *@param  al  The ActionListener
	 */
	public void addReadDataActionListener(ActionListener al) {
		readDataListenersV.add(al);
	}


	/**
	 *  Removes all ActionListeners that listen to reading the new waveform data
	 *  file
	 *
	 *@param  al  Description of the Parameter
	 */
	public void removeReadDataActionListener(ActionListener al) {
		readDataListenersV.remove(al);
	}


	/**
	 *  Returns a vector with ActionListeners that listen to reading the new
	 *  waveform data file
	 *
	 *@return    The vector with ActionListeners
	 */
	public Vector<ActionListener> getReadDataActionListeners() {
		return readDataListenersV;
	}


	/**
	 *  Notifies all listeners that the type index of emittance device has been
	 *  changed
	 */
	private void makeSetIndexActions() {
		ActionEvent e = new ActionEvent(this, 0, "index set");
		for (int i = 0; i < setIndexListenersV.size(); i++) {
			setIndexListenersV.get(i).actionPerformed(e);
		}
	}


	/**
	 *  Adds a new ActionListener to listen the change in the type device index
	 *
	 *@param  al  The ActionListener
	 */
	public void addSetIndexActionListener(ActionListener al) {
		setIndexListenersV.add(al);
	}


	/**
	 *  Removes an ActionListeners to listen the change in the type device index
	 *
	 *@param  al  The ActionListener
	 */
	public void removeSetIndexActionListener(ActionListener al) {
		setIndexListenersV.remove(al);
	}


	/**
	 *  Returns a vector with ActionListeners to listen the change in the type
	 *  device index
	 *
	 *@return    The vector with ActionListeners
	 */
	public Vector<ActionListener> getSetIndexActionListeners() {
		return setIndexListenersV;
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
	}

	/**
	 *  Returns the name of sub-structure in the XML data adapter with
	 *  configuration data
	 *
	 *@return    The XML data adapter
	 */
	public String getNameXMLData() {
		return xmlName;
	}


	/**
	 *  Writes the parameters on the panel into the XML data adapter
	 *
	 *@param  rawDataPanelData  The XML data adapter
	 */
	public void dumpDataToXML(XmlDataAdaptor rawDataPanelData) {
		XmlDataAdaptor readRawDataPanelData = (XmlDataAdaptor) rawDataPanelData.createChild(getNameXMLData());
		XmlDataAdaptor params = (XmlDataAdaptor) readRawDataPanelData.createChild("PARAMS");

		params.setValue("nWires", nWires_Text.getValue());
		params.setValue("nChannels", nChannels_Text.getValue());
		params.setValue("nPositionsSlit", nPositionsSlit_Text.getValue());
		params.setValue("nPositionsHarp", nPositionsHarp_Text.getValue());
	}


	/**
	 *  Sets the parameters on the panel according to information in the XML data
	 *  adapter
	 *
	 *@param  rawDataPanelData  The XML data adapter
	 */
	public void setDataFromXML(XmlDataAdaptor rawDataPanelData) {
		XmlDataAdaptor readRawDataPanelData = (XmlDataAdaptor) rawDataPanelData.childAdaptor(getNameXMLData());
		XmlDataAdaptor params = (XmlDataAdaptor) readRawDataPanelData.childAdaptor("PARAMS");

		//set GUI values
		nWires_Text.setValue(params.doubleValue("nWires"));
		nChannels_Text.setValue(params.doubleValue("nChannels"));
		nPositionsSlit_Text.setValue(params.doubleValue("nPositionsSlit"));
		nPositionsHarp_Text.setValue(params.doubleValue("nPositionsHarp"));
	}

}

