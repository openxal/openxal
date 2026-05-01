/*
 *  ScalarPVsChartPanel.java
 *
 *  Created on May 24, 2005
 */
package xal.app.scalarpvviewer;

import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.DateGraphFormat;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.scan.UpdatingEventController;
import xal.tools.text.ScientificNumberFormat;
import xal.extension.widgets.plot.CurveData;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *  The panel keeps the charts and the PVs table.
 *
 *@author    shishlo
 */
public class ScalarPVsChartPanel {

	private final JPanel mainPanel = new JPanel(new BorderLayout());
	private final JLabel titleLabel = new JLabel("EMPTY VIEWER. CHARTS. GO TO PREDEFINED CONFIG. PANEL", JLabel.CENTER);

	private JSplitPane mainSplitPanel = null;

	private final JPanel upperPanel = new JPanel(new BorderLayout());
	private final JPanel graphPanel = new JPanel(new BorderLayout());
	private final JPanel tablePanel = new JPanel(new BorderLayout());

	private TitledBorder borderGraph = null;
	private final FunctionGraphsJPanel GP = new FunctionGraphsJPanel();
	private final DateGraphFormat dgfrmt = new DateGraphFormat("MM/dd/yy HH:mm:ss");

	private ScalarPVs spvs = null;

	private ScalarPVsChartsTable table = null;

	private UpdatingEventController ucExt = null;
	private final UpdatingEventController uc = new UpdatingEventController();

	private final JRadioButton recordButton = new JRadioButton("Recording On", true);
	private final JSpinner freq_cntrlPanel_Spinner = new JSpinner(new SpinnerNumberModel(30, 1, 300, 1));
	private final JLabel cntrlPanelTime_Label = new JLabel("Update Time [sec]", JLabel.LEFT);
	private final JButton clearButton = new JButton("Clear All Data");


	/**
	 *  Constructor for the ScalarPVsChartPanel object
	 *
	 *@param  spvsIn  ScalarPVs object with ScalarPVs
	 *@param  ucExtIn    The update controller
	 */
	public ScalarPVsChartPanel(ScalarPVs spvsIn, UpdatingEventController ucExtIn) {
		ucExt = ucExtIn;
		spvs = spvsIn;
		table = new ScalarPVsChartsTable(spvs);

		//Graph panel definition
		GP.setOffScreenImageDrawing(true);
		GP.setGraphBackGroundColor(Color.white);
		GP.setAxisNames("time", "PV Value");
		GP.setSmartGL(false);
		GP.setNumberFormatX(dgfrmt);

		ScientificNumberFormat frmt = new ScientificNumberFormat( 5, 9, false );
		frmt.setFixedLength(true);
		GP.setNumberFormatY(frmt);

		SimpleChartPopupMenu.addPopupMenuTo(GP);

		Border etchedBorder = BorderFactory.createEtchedBorder();
		borderGraph = BorderFactory.createTitledBorder(etchedBorder, "Current,Reference, and Difference PVs Charts");
		graphPanel.setBorder(borderGraph);

		graphPanel.add(GP, BorderLayout.CENTER);

		//make control panel
		JPanel tmp_panel_1 = new JPanel();
		tmp_panel_1.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
		tmp_panel_1.add(recordButton);

		JPanel tmp_panel_2 = new JPanel();
		tmp_panel_2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_panel_2.add(freq_cntrlPanel_Spinner);
		tmp_panel_2.add(cntrlPanelTime_Label);

		JPanel tmp_panel_3 = new JPanel();
		tmp_panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
		tmp_panel_3.add(clearButton);

		JPanel tmp_panel_4 = new JPanel();
		tmp_panel_4.setLayout(new GridLayout(3, 1, 1, 1));
		tmp_panel_4.add(tmp_panel_1);
		tmp_panel_4.add(tmp_panel_2);
		tmp_panel_4.add(tmp_panel_3);
		tmp_panel_4.setBorder(etchedBorder);

		JPanel tmp_panel_5 = new JPanel(new BorderLayout());
		tmp_panel_5.add(tmp_panel_4, BorderLayout.NORTH);

		//make upper panel
		upperPanel.add(graphPanel, BorderLayout.CENTER);
		upperPanel.add(tmp_panel_5, BorderLayout.WEST);

		//make table panel
		tablePanel.add(table.getPanel(), BorderLayout.CENTER);

		mainSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				upperPanel, tablePanel);

		titleLabel.setForeground(Color.red);

		mainPanel.add(mainSplitPanel, BorderLayout.CENTER);
		mainPanel.add(titleLabel, BorderLayout.NORTH);

		//make listeners
		ucExt.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					uc.update();
				}
			});

		uc.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (recordButton.isSelected()) {
						spvs.memorize();
						GP.refreshGraphJPanel();
					}
				}
			});


		table.addChangeListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					updateGraph();
				}
			});

		recordButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					spvs.memorize();
					GP.refreshGraphJPanel();
				}
			});

		setTimeStep(getTimeStep());
		freq_cntrlPanel_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					double time_new = ((Integer) freq_cntrlPanel_Spinner.getValue()).doubleValue();
					double time_old = uc.getUpdateTime();
					uc.setUpdateTime(time_new);
					if (time_old > time_new) {
						uc.update();
					}
				}
			});


		clearButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					spvs.clearChart();
					GP.refreshGraphJPanel();
				}
			});

	}


	/**
	 *  Sets the title
	 *
	 *@param  title  The new title
	 */
	public void setTitle(String title) {
		titleLabel.setText(title);
	}


	/**
	 *  Returns the title
	 *
	 *@return    The title
	 */
	public String getTitle() {
		return titleLabel.getText();
	}


	/**
	 *  Returns the boolean value for recording
	 *
	 *@return    the boolean value for recording
	 */
	public boolean recordOn() {
		return recordButton.isSelected();
	}


	/**
	 *  Sets the recording state
	 *
	 *@param  isOn  the recording state
	 */
	public void recordOn(boolean isOn) {
		recordButton.setSelected(isOn);
	}


	/**
	 *  Gets the panel attribute of the ScalarPVsChartPanel object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return mainPanel;
	}


	/**
	 *  Update the graphs according the table
	 */
	public void updateGraph() {
		Vector<CurveData> gdV = new Vector<CurveData>();
		int nPV = spvs.getSize();
		for (int i = 0; i < nPV; i++) {
			ScalarPV spv = spvs.getScalarPV(i);
			if (spv.showValueChart()) {
				gdV.add(spv.getValueChartGraphData());
			}
			if (spv.showRefChart()) {
				gdV.add(spv.getRefChartGraphData());
			}
			if (spv.showDifChart()) {
				gdV.add(spv.getDifChartGraphData());
			}
		}
		spvs.findMinMax();
		GP.removeAllCurveData();
		GP.addCurveData(gdV);
	}

	/**
	 *  Returns the time step of updating
	 *
	 *@return    The time step value
	 */
	public double getTimeStep() {
		return ((Integer) freq_cntrlPanel_Spinner.getValue()).doubleValue();
	}


	/**
	 *  Sets the time step of updating
	 *
	 *@param  timeStep  The new time step value
	 */
	public void setTimeStep(double timeStep) {
		freq_cntrlPanel_Spinner.setValue(new Integer((int) timeStep));
		uc.setUpdateTime(timeStep);
	}

	/**
	 *  Sets the font
	 *
	 *@param  fnt  The new font
	 */
	public void setFont(Font fnt) {
		table.setFont(fnt);
		borderGraph.setTitleFont(fnt);
		titleLabel.setFont(fnt);

		recordButton.setFont(fnt);
		clearButton.setFont(fnt);

		freq_cntrlPanel_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) freq_cntrlPanel_Spinner.getEditor()).getTextField().setFont(fnt);
		cntrlPanelTime_Label.setFont(fnt);

	}

}

