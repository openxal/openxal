package xal.app.emittanceanalysis.analysis;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.border.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;
import xal.extension.widgets.apputils.*;
import xal.tools.text.*;

import xal.app.emittanceanalysis.rawdata.*;

/**
 *  This is a common part of all analysis. The panels of this common part are
 *  located on the left top corner and at the center of the top part of the
 *  panel
 *
 *@author     shishlo
 *@version    1.0
 */
class AnalysisCommonPart extends AnalysisBasic {

	//name of the analysis
	/**
	 *  The name of the analysis
	 */
	protected String name = "AnalysisCommonPart";

	//panels for custom GUI elements for the common part of analyses
	private JPanel centerTopPanel = new JPanel();

	private TitledBorder leftTopBborder = null;

	//external listeners for change of type of analysis
	private Vector<ActionListener> typeChangeListenerV = new Vector<>();

	//analysis controller reference
	private AnalysisController analysisController = null;

	//combo-box for choosing the type of nalysis
	private JComboBox<String> analysisTypeChooser = null;

	//emittance plot graph panel
	private FunctionGraphsJPanel GP_ep = new FunctionGraphsJPanel();

	//emittance data as ColorSurfaceData instance (for analysis only)
	private ColorSurfaceData emittance3Da = null;

	//--------------------------------------------------------
	//GUI elements on the panels
	//--------------------------------------------------------

	//--------------------------------------------------------
	//GUI elements on the left common panel
	//--------------------------------------------------------

	//text area for a short discription of the analysis
	private JTextArea analysisDescriptionText = new JTextArea(11, 30);

	//the value of the threshold that will be used through out all analyses
	//format for threshold value
	private DoubleInputTextField threshold_Text = new DoubleInputTextField(6);
	private DecimalFormat threshold_Format = new DecimalFormat("##0.#");
	private JLabel threshold_label = new JLabel("Threshold (global)[%]:", JLabel.CENTER);

	//--------------------------------------------------------
	//GUI elements on the right common panel
	//-------------------------------------------------------

	private JLabel empty1_Label = new JLabel("", JLabel.CENTER);
	private JLabel empty2_Label = new JLabel("", JLabel.CENTER);

	private JLabel alpha_Label = new JLabel("Alpha", JLabel.CENTER);
	private JLabel beta_Label = new JLabel("Beta", JLabel.CENTER);
	private JLabel emt_Label = new JLabel("Emittance", JLabel.CENTER);

	private JLabel alphaDim_Label = new JLabel("[ ]", JLabel.CENTER);
	private JLabel betaDim_Label = new JLabel("[mm/mrad]", JLabel.CENTER);
	private JLabel rmsDim_Label = new JLabel("[mm mrad]", JLabel.CENTER);

	private JLabel rms_Label = new JLabel(" RMS:", JLabel.LEFT);
	private JLabel fit_Label = new JLabel(" FIT:", JLabel.LEFT);
	private JLabel gauss_Label = new JLabel(" GAU:", JLabel.LEFT);

	private JLabel[] labelArr = new JLabel[11];

	private DoubleInputTextField alphaRMS_Text = new DoubleInputTextField(10);
	private DoubleInputTextField betaRMS_Text = new DoubleInputTextField(10);
	private DoubleInputTextField emtRMS_Text = new DoubleInputTextField(10);

	private DoubleInputTextField alphaFIT_Text = new DoubleInputTextField(10);
	private DoubleInputTextField betaFIT_Text = new DoubleInputTextField(10);
	private DoubleInputTextField emtFIT_Text = new DoubleInputTextField(10);

	private DoubleInputTextField alphaGAU_Text = new DoubleInputTextField(10);
	private DoubleInputTextField betaGAU_Text = new DoubleInputTextField(10);
	private DoubleInputTextField emtGAU_Text = new DoubleInputTextField(10);

	private DoubleInputTextField[] dblTextArr = new DoubleInputTextField[9];

	private NumberFormat dbl_Format = new ScientificNumberFormat(5);


	/**
	 *  Constructor for the AnalysisCommonPart object
	 *
	 *@param  crossParamMap         The HashMap with Parameters of the analyses
	 *@param  analysisTypeIndex_In  The type index of the analysis
	 *@param  typeNames_arr         Description of the Parameter
	 */
	AnalysisCommonPart(String[] typeNames_arr, int analysisTypeIndex_In, HashMap<String,Object> crossParamMap) {
		super(analysisTypeIndex_In, crossParamMap);

		analysisDescriptionString = "The common part of all analyses.";

		Border etchedBorder = BorderFactory.createEtchedBorder();

		threshold_Text.setNumberFormat(threshold_Format);
		threshold_Text.setHorizontalAlignment(JTextField.CENTER);
		threshold_Text.setValue(-100.0);
		threshold_Text.setEditable(false);
		threshold_Text.setBackground(Color.white);

		analysisController = (AnalysisController) crossParamMap.get("AnalysisController");

		ActionListener typeChangeListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int ID = analysisTypeChooser.getSelectedIndex();
					ActionEvent changeAnalysisType =
							new ActionEvent(analysisController, ID, "type_changed");
					for (int i = 0; i < typeChangeListenerV.size(); i++) {
						typeChangeListenerV.get(i).actionPerformed(changeAnalysisType);
					}
				}
			};

		analysisTypeChooser = new JComboBox<>(typeNames_arr);
		analysisTypeChooser.setBackground(Color.cyan);
		analysisTypeChooser.addActionListener(typeChangeListener);
		analysisTypeChooser.setSelectedIndex(0);

		analysisDescriptionText.setLineWrap(true);
		analysisDescriptionText.setForeground(Color.blue);
		JScrollPane textScrollPane = new JScrollPane(analysisDescriptionText,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		//emittance contour plot graph panel
		//SimpleChartPopupMenu.addPopupMenuTo( GP_ep );
		GP_ep.setOffScreenImageDrawing(true);
		GP_ep.setGraphBackGroundColor(Color.black);
		GP_ep.setGridLinesVisibleX(false);
		GP_ep.setGridLinesVisibleY(false);
		GP_ep.setAxisNames("x, [mm]", "xp, [mrad]");

		//set text fields properties
		labelArr[0] = alpha_Label;
		labelArr[1] = beta_Label;
		labelArr[2] = emt_Label;
		labelArr[3] = alphaDim_Label;
		labelArr[4] = betaDim_Label;
		labelArr[5] = rmsDim_Label;
		labelArr[6] = rms_Label;
		labelArr[7] = fit_Label;
		labelArr[8] = gauss_Label;
		labelArr[9] = empty1_Label;
		labelArr[10] = empty2_Label;

		dblTextArr[0] = alphaRMS_Text;
		dblTextArr[1] = betaRMS_Text;
		dblTextArr[2] = emtRMS_Text;
		dblTextArr[3] = alphaFIT_Text;
		dblTextArr[4] = betaFIT_Text;
		dblTextArr[5] = emtFIT_Text;
		dblTextArr[6] = alphaGAU_Text;
		dblTextArr[7] = betaGAU_Text;
		dblTextArr[8] = emtGAU_Text;

		for (int i = 0; i < dblTextArr.length; i++) {
			dblTextArr[i].setNumberFormat(dbl_Format);
		}

		for (int i = 0; i < dblTextArr.length; i++) {
			dblTextArr[i].setHorizontalAlignment(JTextField.CENTER);
		}

		for (int i = 0; i < dblTextArr.length; i++) {
			dblTextArr[i].setEditable(false);
		}

		for (int i = 0; i < dblTextArr.length; i++) {
			dblTextArr[i].setText(null);
		}

		for (int i = 0; i < dblTextArr.length; i++) {
			dblTextArr[i].setBackground(Color.white);
		}

		//Text field right top panel
		JPanel emtLabelPanel = new JPanel(new GridLayout(5, 1, 1, 1));
		emtLabelPanel.add(empty1_Label);
		emtLabelPanel.add(empty2_Label);
		emtLabelPanel.add(rms_Label);
		emtLabelPanel.add(fit_Label);
		emtLabelPanel.add(gauss_Label);

		JPanel emtFieldPanel = new JPanel(new GridLayout(5, 3, 1, 1));
		emtFieldPanel.add(alpha_Label);
		emtFieldPanel.add(beta_Label);
		emtFieldPanel.add(emt_Label);

		emtFieldPanel.add(alphaDim_Label);
		emtFieldPanel.add(betaDim_Label);
		emtFieldPanel.add(rmsDim_Label);

		emtFieldPanel.add(alphaRMS_Text);
		emtFieldPanel.add(betaRMS_Text);
		emtFieldPanel.add(emtRMS_Text);

		emtFieldPanel.add(alphaFIT_Text);
		emtFieldPanel.add(betaFIT_Text);
		emtFieldPanel.add(emtFIT_Text);

		emtFieldPanel.add(alphaGAU_Text);
		emtFieldPanel.add(betaGAU_Text);
		emtFieldPanel.add(emtGAU_Text);

		JPanel emtPanel = new JPanel(new BorderLayout());
		emtPanel.setBorder(etchedBorder);
		emtPanel.add(emtLabelPanel, BorderLayout.WEST);
		emtPanel.add(emtFieldPanel, BorderLayout.CENTER);

		//panel border
		leftTopBborder = BorderFactory.createTitledBorder(etchedBorder, "analysis chooser");

		JPanel tmp_0 = new JPanel(new BorderLayout());
		tmp_0.add(analysisTypeChooser, BorderLayout.NORTH);
		tmp_0.add(textScrollPane, BorderLayout.CENTER);

		JPanel tmp_1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 1));
		tmp_1.add(threshold_label);
		tmp_1.add(threshold_Text);

		getLeftTopPanel().setLayout(new BorderLayout());
		getLeftTopPanel().add(tmp_0, BorderLayout.NORTH);
		getLeftTopPanel().add(tmp_1, BorderLayout.SOUTH);
		getLeftTopPanel().setBackground(new Color(128, 128, 255));
		getLeftTopPanel().setBorder(leftTopBborder);

		centerTopPanel.setLayout(new BorderLayout());
		centerTopPanel.add(GP_ep, BorderLayout.CENTER);
		centerTopPanel.setBorder(etchedBorder);

		getRightTopPanel().setLayout(new BorderLayout());
		getRightTopPanel().add(emtPanel, BorderLayout.NORTH);

		//debug button - for debugging only
		//final JButton printHashButton = new JButton( "print HashMap" );
		//printHashButton.addActionListener(
		//    new ActionListener() {
		//        public void actionPerformed( ActionEvent e ) {
		//           Object[] keys = getParamsHashMap().keySet().toArray();
		//           for ( int i = 0; i < keys.length; i++ ) {
		//               System.out.println( "debug i=" + i + " key=" + keys[i].toString() );
		//           }
		//       }
		//   } );
		//getRightTopPanel().add( printHashButton, BorderLayout.SOUTH );

	}


	/**
	 *  Performs actions before show the panel
	 */
	void goingShowUp() {
		emittance3Da = (ColorSurfaceData) getParamsHashMap().get("RawEmittanceData");
		GP_ep.setColorSurfaceData(emittance3Da);

		RawDataPanel rawDataPanel = (RawDataPanel) getParamsHashMap().get("RawDataPanel");
		String gp_ep_graph_name = "File: " + rawDataPanel.getRawDataFileName();
		GP_ep.setName(gp_ep_graph_name);
	}


	/**
	 *  Performs actions before close the panel
	 */
	void goingShowOff() { }


	/**
	 *  Sets all analyzes in the initial state with removing all temporary data
	 */
	void initialize() {

		for (int i = 0; i < dblTextArr.length; i++) {
			dblTextArr[i].setText(null);
		}

		for (int i = 0; i < dblTextArr.length; i++) {
			dblTextArr[i].setBackground(Color.white);
		}

		getParamsHashMap().put("IS_READY_RMS", new Boolean(false));
		getParamsHashMap().put("IS_READY_FIT", new Boolean(false));
		getParamsHashMap().put("IS_READY_GAU", new Boolean(false));

		analysisTypeChooser.setSelectedIndex(0);

	}


	/**
	 *  Creates objects for the global HashMap using put method only
	 */
	void createHashMapObjects() {
		getParamsHashMap().put(name, this);
		getParamsHashMap().put("THRESHOLD_TEXT", threshold_Text);
		getParamsHashMap().put("EMITTANCE_3D_PLOT", GP_ep);

		getParamsHashMap().put("ALPHA_RMS", alphaRMS_Text);
		getParamsHashMap().put("BETA_RMS", betaRMS_Text);
		getParamsHashMap().put("EMT_RMS", emtRMS_Text);
		getParamsHashMap().put("IS_READY_RMS", new Boolean(false));

		getParamsHashMap().put("ALPHA_FIT", alphaFIT_Text);
		getParamsHashMap().put("BETA_FIT", betaFIT_Text);
		getParamsHashMap().put("EMT_FIT", emtFIT_Text);
		getParamsHashMap().put("IS_READY_FIT", new Boolean(false));

		getParamsHashMap().put("ALPHA_GAU", alphaGAU_Text);
		getParamsHashMap().put("BETA_GAU", betaGAU_Text);
		getParamsHashMap().put("EMT_GAU", emtGAU_Text);
		getParamsHashMap().put("IS_READY_GAU", new Boolean(false));
	}


	/**
	 *  Connects to the objects in the global HashMap using only get method of the
	 *  HashMap
	 */
	void connectToHashMapObjects() {
	}


	/**
	 *  Adds a feature to the TypeChangeListener attribute of the
	 *  AnalysisCommonPart object
	 *
	 *@param  al  The feature to be added to the TypeChangeListener attribute
	 */
	void addTypeChangeListener(ActionListener al) {
		typeChangeListenerV.add(al);
	}


	/**
	 *  Sets the descriptionText attribute of the AnalysisCommonPart object
	 *
	 *@param  text  The new descriptionText value
	 */
	void setDescriptionText(String text) {
		analysisDescriptionText.setText(null);
		analysisDescriptionText.append(text);
	}


	/**
	 *  Returns the central part of the top panel. It is the common part of all
	 *  analyses
	 *
	 *@return    The centerTopPanel value
	 */
	JPanel getCenterTopPanel() {
		return centerTopPanel;
	}


	/**
	 *  Sets all fonts.
	 *
	 *@param  fnt  The new font
	 */
	void setFontForAll(Font fnt) {

		leftTopBborder.setTitleFont(fnt);

		analysisDescriptionText.setFont(fnt);

		analysisTypeChooser.setFont(fnt);
		((JTextField) analysisTypeChooser.getEditor().getEditorComponent()).setFont(fnt);
		analysisTypeChooser.setPreferredSize(new Dimension(1, fnt.getSize() + 10));

		threshold_Text.setFont(fnt);
		threshold_label.setFont(fnt);

		for (int i = 0; i < labelArr.length; i++) {
			labelArr[i].setFont(fnt);
		}

		for (int i = 0; i < dblTextArr.length; i++) {
			dblTextArr[i].setFont(fnt);
		}

	}

}

