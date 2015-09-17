package xal.app.emittanceanalysis.rawdata;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.border.*;
import java.awt.event.*;

import xal.extension.widgets.swing.*;
import xal.extension.widgets.plot.*;
import xal.tools.xml.*;

/**
 *  This is the sub-panel to plot raw data. This panel is a sub-panel of the
 *  RawDataPanel
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public class PlotRawDataPanel {

	private String xmlName = "PLOT_RAW_DATA_PANEL";

	//panel by inself
	private JPanel panel = new JPanel();
	private TitledBorder border = null;

	//raw data object
	private WireRawData rawData = null;

	//graph panel
	private FunctionGraphsJPanel GP = null;

	private ActionListener dragVerLine_Listener = null;

	//format for numbers of rows and channels
	private DecimalFormat int_Format = new DecimalFormat("##0");

	//vectors that keeps already created CurveData instances
	private Vector<CurveData> cdStoreV = new Vector<>();

	//-----------------------------------
	//GUI elements
	//-----------------------------------
	private JButton plot_Button = new JButton("PLOT RAW DATA");

	private JLabel col_0_Label = new JLabel("Type/Param.");
	private JLabel col_1_Label = new JLabel("From");
	private JLabel col_2_Label = new JLabel("Step");
	private JLabel col_3_Label = new JLabel("To");

	private JLabel line_1_Label = new JLabel("Channels ");
	private JLabel line_2_Label = new JLabel("Pos. Slit");
	private JLabel line_3_Label = new JLabel("Pos. Harp");

	private JSpinner chStart_Spinner = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
	private JSpinner chStep_Spinner = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
	private JSpinner chStop_Spinner = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));

	private JSpinner posStart_Spinner = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
	private JSpinner posStep_Spinner = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
	private JSpinner posStop_Spinner = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));

	private JSpinner posHStart_Spinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
	private JSpinner posHStep_Spinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
	private JSpinner posHStop_Spinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));

	private JLabel noiseInd_Label = new JLabel("Noise Up To: ");
	private JLabel sampleStart_Label = new JLabel("Satrt Sample:");
	private JLabel sampleStop_Label = new JLabel("Stop Sample:");

	private DoubleInputTextField noiseInd_Text = new DoubleInputTextField(3);
	private DoubleInputTextField sampleStart_Text = new DoubleInputTextField(3);
	private DoubleInputTextField sampleStop_Text = new DoubleInputTextField(3);


	/**
	 *  Constructor for the PlotRawDataPanel object
	 *
	 *@param  rawDataIn  The WireRawData object reference
	 *@param  GP_in      The FunctionGraphsJPanel with wire waveform plots
	 */
	public PlotRawDataPanel(WireRawData rawDataIn, FunctionGraphsJPanel GP_in) {
		rawData = rawDataIn;
		GP = GP_in;

		noiseInd_Text.setNumberFormat(int_Format);
		sampleStart_Text.setNumberFormat(int_Format);
		sampleStop_Text.setNumberFormat(int_Format);

		noiseInd_Text.setHorizontalAlignment(JTextField.CENTER);
		sampleStart_Text.setHorizontalAlignment(JTextField.CENTER);
		sampleStop_Text.setHorizontalAlignment(JTextField.CENTER);

		noiseInd_Text.setForeground(Color.blue);
		sampleStart_Text.setForeground(Color.red);
		sampleStop_Text.setForeground(Color.red);

		noiseInd_Text.setBackground(Color.white);
		sampleStart_Text.setBackground(Color.white);
		sampleStop_Text.setBackground(Color.white);

		noiseInd_Label.setHorizontalAlignment(SwingConstants.CENTER);
		sampleStart_Label.setHorizontalAlignment(SwingConstants.CENTER);
		sampleStop_Label.setHorizontalAlignment(SwingConstants.CENTER);

		noiseInd_Label.setForeground(Color.blue);
		sampleStart_Label.setForeground(Color.red);
		sampleStop_Label.setForeground(Color.red);

		plot_Button.setForeground(Color.blue.darker());
		//plot_Button.setBackground( Color.cyan );
		plot_Button.setBorder(BorderFactory.createRaisedBevelBorder());

		col_0_Label.setHorizontalAlignment(SwingConstants.CENTER);
		col_1_Label.setHorizontalAlignment(SwingConstants.CENTER);
		col_2_Label.setHorizontalAlignment(SwingConstants.CENTER);
		col_3_Label.setHorizontalAlignment(SwingConstants.CENTER);

		line_1_Label.setHorizontalAlignment(SwingConstants.LEFT);
		line_2_Label.setHorizontalAlignment(SwingConstants.LEFT);
		line_3_Label.setHorizontalAlignment(SwingConstants.LEFT);

		chStart_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		chStep_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		chStop_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

		posStart_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		posStep_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		posStop_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

		posHStart_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		posHStep_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		posHStop_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

		GP.setDraggingVerLinesGraphMode(true);

		GP.addVerticalLine(100., Color.blue);
		GP.addVerticalLine(130., Color.red);
		GP.addVerticalLine(150., Color.red);

		dragVerLine_Listener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int ind = GP.getDraggedLineIndex();
					double pos = GP.getVerticalValue(ind);
					if (ind == 0) {
						noiseInd_Text.setValueQuietly(pos);
					} else {
						if (ind == 1) {
							sampleStart_Text.setValueQuietly(pos);
						} else {
							sampleStop_Text.setValueQuietly(pos);
						}
					}
				}
			};

		GP.addDraggedVerLinesListener(dragVerLine_Listener);
		GP.setDraggedVerLinesMotionListen(true);

		noiseInd_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GP.addDraggedVerLinesListener(null);
					double pos = noiseInd_Text.getValue();
					GP.setVerticalLineValue(pos, 0);
					GP.addDraggedVerLinesListener(dragVerLine_Listener);
				}
			});

		sampleStart_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GP.addDraggedVerLinesListener(null);
					double pos = sampleStart_Text.getValue();
					GP.setVerticalLineValue(pos, 1);
					GP.addDraggedVerLinesListener(dragVerLine_Listener);
				}
			});

		sampleStop_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GP.addDraggedVerLinesListener(null);
					double pos = sampleStop_Text.getValue();
					GP.setVerticalLineValue(pos, 2);
					GP.addDraggedVerLinesListener(dragVerLine_Listener);
				}
			});

		noiseInd_Text.setValue(100.);
		sampleStart_Text.setValue(130.);
		sampleStop_Text.setValue(150.);

		plot_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plotRawData();
				}
			});

		//panel border
		Border etchedBorder = BorderFactory.createEtchedBorder();
		border = BorderFactory.createTitledBorder(etchedBorder, "plot raw data");
		panel.setBorder(border);
		panel.setLayout(new BorderLayout());
		panel.setBackground(panel.getBackground().darker());

		JPanel tmp_panel_0 = new JPanel();
		tmp_panel_0.setLayout(new GridLayout(4, 4, 1, 1));
		tmp_panel_0.setBorder(etchedBorder);
		tmp_panel_0.add(col_0_Label);
		tmp_panel_0.add(col_1_Label);
		tmp_panel_0.add(col_2_Label);
		tmp_panel_0.add(col_3_Label);
		tmp_panel_0.add(line_1_Label);
		tmp_panel_0.add(chStart_Spinner);
		tmp_panel_0.add(chStep_Spinner);
		tmp_panel_0.add(chStop_Spinner);
		tmp_panel_0.add(line_2_Label);
		tmp_panel_0.add(posStart_Spinner);
		tmp_panel_0.add(posStep_Spinner);
		tmp_panel_0.add(posStop_Spinner);
		tmp_panel_0.add(line_3_Label);
		tmp_panel_0.add(posHStart_Spinner);
		tmp_panel_0.add(posHStep_Spinner);
		tmp_panel_0.add(posHStop_Spinner);

		JPanel tmp_panel_1 = new JPanel();
		tmp_panel_1.setLayout(new GridLayout(2, 3, 3, 1));
		tmp_panel_1.setBorder(etchedBorder);
		tmp_panel_1.add(noiseInd_Label);
		tmp_panel_1.add(sampleStart_Label);
		tmp_panel_1.add(sampleStop_Label);
		tmp_panel_1.add(noiseInd_Text);
		tmp_panel_1.add(sampleStart_Text);
		tmp_panel_1.add(sampleStop_Text);

		JPanel tmp_panel_2 = new JPanel();
		tmp_panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		tmp_panel_2.add(plot_Button);

		panel.add(tmp_panel_0, BorderLayout.NORTH);
		panel.add(tmp_panel_1, BorderLayout.CENTER);
		panel.add(tmp_panel_2, BorderLayout.SOUTH);
	}


	/**
	 *  Sets the font for all GUI elements
	 *
	 *@param  fnt  The new font
	 */
	public void setFontForAll(Font fnt) {
		border.setTitleFont(fnt);

		plot_Button.setFont(fnt);

		col_0_Label.setFont(fnt);
		col_1_Label.setFont(fnt);
		col_2_Label.setFont(fnt);
		col_3_Label.setFont(fnt);

		line_1_Label.setFont(fnt);
		line_2_Label.setFont(fnt);
		line_3_Label.setFont(fnt);

		noiseInd_Label.setFont(fnt);
		sampleStart_Label.setFont(fnt);
		sampleStop_Label.setFont(fnt);

		noiseInd_Text.setFont(fnt);
		sampleStart_Text.setFont(fnt);
		sampleStop_Text.setFont(fnt);

		chStart_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) chStart_Spinner.getEditor()).getTextField().setFont(fnt);
		chStep_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) chStep_Spinner.getEditor()).getTextField().setFont(fnt);
		chStop_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) chStop_Spinner.getEditor()).getTextField().setFont(fnt);

		posStart_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) posStart_Spinner.getEditor()).getTextField().setFont(fnt);
		posStep_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) posStep_Spinner.getEditor()).getTextField().setFont(fnt);
		posStop_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) posStop_Spinner.getEditor()).getTextField().setFont(fnt);

		posHStart_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) posHStart_Spinner.getEditor()).getTextField().setFont(fnt);
		posHStep_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) posHStep_Spinner.getEditor()).getTextField().setFont(fnt);
		posHStop_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) posHStop_Spinner.getEditor()).getTextField().setFont(fnt);

	}


	/**
	 *  Returns the JPanel of this class
	 *
	 *@return    The JPanel of this class
	 */
	public JPanel getJPanel() {
		return panel;
	}


	/**
	 *  Updates the graph sub-panel with wire waveforms accordind the parameters on
	 *  the panel
	 */
	public void plotRawData() {

		GP.removeAllCurveData();

		int chStart = ((Integer) chStart_Spinner.getValue()).intValue();
		int chStep = ((Integer) chStep_Spinner.getValue()).intValue();
		int chStop = ((Integer) chStop_Spinner.getValue()).intValue();

		int posStart = ((Integer) posStart_Spinner.getValue()).intValue();
		int posStep = ((Integer) posStep_Spinner.getValue()).intValue();
		int posStop = ((Integer) posStop_Spinner.getValue()).intValue();

		int posHStart = ((Integer) posHStart_Spinner.getValue()).intValue();
		int posHStep = ((Integer) posHStep_Spinner.getValue()).intValue();
		int posHStop = ((Integer) posHStop_Spinner.getValue()).intValue();

		int chMax = rawData.getChannelsNumber();
		int posMax = rawData.getPositionsNumberSlit();
		int posHMax = rawData.getPositionsNumberHarp();

		if (chStart < 0 || chStart > chMax) {
			return;
		}
		if (posStart < 0 || posStart > posMax) {
			return;
		}

		if (posHStart < 0 || posHStart > posHMax) {
			return;
		}

		//find max
		double max_val = 0.;

		for (int chInd = chStart; (chInd <= chMax && chInd <= chStop); chInd += chStep) {
			for (int posInd = posStart; (posInd <= posMax && posInd <= posStop); posInd += posStep) {
				for (int posHInd = posHStart; (posHInd <= posHMax && posHInd <= posHStop); posHInd += posHStep) {
					double val = rawData.getMaxValue(posInd - 1, posHInd - 1, chInd - 1);
					if (val > max_val) {
						max_val = val;
					}
				}
			}

		}

		//increasing of the curve data store size
		int iniStoreSize = cdStoreV.size();
		for (int i = iniStoreSize; i < chMax * posMax * posHMax; i++) {
			cdStoreV.add(new CurveData());
		}

		//create graphs
		int nSamples = rawData.getSamplesNumber();

		Vector<CurveData> cdV = new Vector<>();

		int index = 0;

		for (int chInd = chStart; (chInd <= chMax && chInd <= chStop); chInd += chStep) {
			for (int posInd = posStart; (posInd <= posMax && posInd <= posStop); posInd += posStep) {
				for (int posHInd = posHStart; (posHInd <= posHMax && posHInd <= posHStop); posHInd += posHStep) {

					CurveData cd = cdStoreV.get((chInd - 1) +
							chMax * (posInd - 1) +
							chMax * posMax * (posHInd - 1));
					cd.clear();
					cd.setColor(IncrementalColors.getColor(index));
					for (int is = 0; is < nSamples; is++) {
						double value = rawData.getValue(is, posInd - 1, posHInd - 1, chInd - 1) / max_val;
						cd.addPoint((double) is, value);
					}
					cdV.add(cd);
					index++;

				}
			}
		}

		GP.addCurveData(cdV);
	}


	/**
	 *  Sets the spinners max, min and current values
	 *
	 *@param  nChannels        The new spinner model for "number of channels" field
	 *@param  nPositions_Slit  The new spinnerModels value
	 *@param  nPositions_Harp  The new spinnerModels value
	 */
	public void setSpinnerModels(int nChannels, int nPositions_Slit, int nPositions_Harp) {
		int chStart = ((Integer) chStart_Spinner.getValue()).intValue();
		int chStep = ((Integer) chStep_Spinner.getValue()).intValue();
		int chStop = ((Integer) chStop_Spinner.getValue()).intValue();

		int posStart = ((Integer) posStart_Spinner.getValue()).intValue();
		int posStep = ((Integer) posStep_Spinner.getValue()).intValue();
		int posStop = ((Integer) posStop_Spinner.getValue()).intValue();

		int posHStart = ((Integer) posHStart_Spinner.getValue()).intValue();
		int posHStep = ((Integer) posHStep_Spinner.getValue()).intValue();
		int posHStop = ((Integer) posHStop_Spinner.getValue()).intValue();

		chStart_Spinner.setModel(new SpinnerNumberModel(1, 1, nChannels, 1));
		chStep_Spinner.setModel(new SpinnerNumberModel(1, 1, nChannels, 1));
		chStop_Spinner.setModel(new SpinnerNumberModel(1, 1, nChannels, 1));

		posStart_Spinner.setModel(new SpinnerNumberModel(1, 1, nPositions_Slit, 1));
		posStep_Spinner.setModel(new SpinnerNumberModel(1, 1, nPositions_Slit, 1));
		posStop_Spinner.setModel(new SpinnerNumberModel(1, 1, nPositions_Slit, 1));

		posHStart_Spinner.setModel(new SpinnerNumberModel(1, 1, nPositions_Harp, 1));
		posHStep_Spinner.setModel(new SpinnerNumberModel(1, 1, nPositions_Harp, 1));
		posHStop_Spinner.setModel(new SpinnerNumberModel(1, 1, nPositions_Harp, 1));

		chStart_Spinner.setValue(new Integer(chStart));
		chStep_Spinner.setValue(new Integer(chStep));
		chStop_Spinner.setValue(new Integer(chStop));

		posStart_Spinner.setValue(new Integer(posStart));
		posStep_Spinner.setValue(new Integer(posStep));
		posStop_Spinner.setValue(new Integer(posStop));

		posHStart_Spinner.setValue(new Integer(posHStart));
		posHStep_Spinner.setValue(new Integer(posHStep));
		posHStop_Spinner.setValue(new Integer(posHStop));

	}


	/**
	 *  Sets the default Spinners Models
	 */
	public void setDefaultSpinnersValues() {
		int nPos = ((Integer) ((SpinnerNumberModel) posStart_Spinner.getModel()).getMaximum()).intValue();
		int nCh = ((Integer) ((SpinnerNumberModel) chStart_Spinner.getModel()).getMaximum()).intValue();

		chStart_Spinner.setValue(new Integer(nCh / 2 - 3));
		chStep_Spinner.setValue(new Integer(1));
		chStop_Spinner.setValue(new Integer(nCh / 2 + 3));

		posStart_Spinner.setValue(new Integer(nPos / 2 - 3));
		posStep_Spinner.setValue(new Integer(1));
		posStop_Spinner.setValue(new Integer(nPos / 2 + 3));

		posHStart_Spinner.setValue(new Integer(1));
		posHStep_Spinner.setValue(new Integer(1));
		posHStop_Spinner.setValue(new Integer(1));

	}


	/**
	 *  Returns the limits for waveform integration as an array
	 *
	 *@return    The array of limits (indexes) - [noise max index, sample start,
	 *      sample stop]
	 */
	public int[] getLimits() {
		int[] vals = new int[3];
		vals[0] = (int) noiseInd_Text.getValue();
		vals[1] = (int) sampleStart_Text.getValue();
		vals[2] = (int) sampleStop_Text.getValue();
		return vals;
	}


	/**
	 *  Returns the vectotr with curveData objects with waveforms
	 *
	 *@return    The vectotr with curveData objects
	 */
	public Vector<CurveData> getCurveDataVector() {
		return cdStoreV;
	}


	/**
	 *  Returns the string identifier in the XML structure.
	 *
	 *@return    The name of sub-structure in the XML data adapter
	 */
	public String getNameXMLData() {
		return xmlName;
	}


	/**
	 *  Writes configuration information into the XML data adapter
	 *
	 *@param  rawDataPanelData  The XML data adapter
	 */
	public void dumpDataToXML(XmlDataAdaptor rawDataPanelData) {
		XmlDataAdaptor plotRawDataPanelData = (XmlDataAdaptor) rawDataPanelData.createChild(getNameXMLData());
		XmlDataAdaptor params = (XmlDataAdaptor) plotRawDataPanelData.createChild("PARAMS");

		int chStart = ((Integer) chStart_Spinner.getValue()).intValue();
		int chStep = ((Integer) chStep_Spinner.getValue()).intValue();
		int chStop = ((Integer) chStop_Spinner.getValue()).intValue();

		int posStart = ((Integer) posStart_Spinner.getValue()).intValue();
		int posStep = ((Integer) posStep_Spinner.getValue()).intValue();
		int posStop = ((Integer) posStop_Spinner.getValue()).intValue();

		int posHStart = ((Integer) posHStart_Spinner.getValue()).intValue();
		int posHStep = ((Integer) posHStep_Spinner.getValue()).intValue();
		int posHStop = ((Integer) posHStop_Spinner.getValue()).intValue();

		int nPositionsSlit = ((Integer) ((SpinnerNumberModel) posStart_Spinner.getModel()).getMaximum()).intValue();
		int nPositionsHarp = ((Integer) ((SpinnerNumberModel) posHStart_Spinner.getModel()).getMaximum()).intValue();
		int nChannels = ((Integer) ((SpinnerNumberModel) chStart_Spinner.getModel()).getMaximum()).intValue();

		params.setValue("chStart", chStart);
		params.setValue("chStep", chStep);
		params.setValue("chStop", chStop);
		params.setValue("posStartSlit", posStart);
		params.setValue("posStepSlit", posStep);
		params.setValue("posStopSlit", posStop);

		params.setValue("posStartHarp", posHStart);
		params.setValue("posStepHarp", posHStep);
		params.setValue("posStopHarp", posHStop);

		params.setValue("nPositionsSlit", nPositionsSlit);
		params.setValue("nPositionsHarp", nPositionsHarp);
		params.setValue("nChannels", nChannels);

		params.setValue("noiseInd", noiseInd_Text.getValue());
		params.setValue("sampleStart", sampleStart_Text.getValue());
		params.setValue("sampleStop", sampleStop_Text.getValue());
	}


	/**
	 *  Sets configuration from information in the XML data file.
	 *
	 *@param  rawDataPanelData  The XML data adapter
	 */
	public void setDataFromXML(XmlDataAdaptor rawDataPanelData) {
		XmlDataAdaptor plotRawDataPanelData = (XmlDataAdaptor) rawDataPanelData.childAdaptor(getNameXMLData());
		XmlDataAdaptor params = (XmlDataAdaptor) plotRawDataPanelData.childAdaptor("PARAMS");

		//set GUI values
		setSpinnerModels(params.intValue("nChannels"), params.intValue("nPositionsSlit"), params.intValue("nPositionsHarp"));

		chStart_Spinner.setValue(new Integer(params.intValue("chStart")));
		chStep_Spinner.setValue(new Integer(params.intValue("chStep")));
		chStop_Spinner.setValue(new Integer(params.intValue("chStop")));

		posStart_Spinner.setValue(new Integer(params.intValue("posStartSlit")));
		posStep_Spinner.setValue(new Integer(params.intValue("posStepSlit")));
		posStop_Spinner.setValue(new Integer(params.intValue("posStopSlit")));

		posStart_Spinner.setValue(new Integer(params.intValue("posStartHarp")));
		posStep_Spinner.setValue(new Integer(params.intValue("posStepHarp")));
		posStop_Spinner.setValue(new Integer(params.intValue("posStopHarp")));

		noiseInd_Text.setValue(params.doubleValue("noiseInd"));
		sampleStart_Text.setValue(params.doubleValue("sampleStart"));
		sampleStop_Text.setValue(params.doubleValue("sampleStop"));

	}

}

