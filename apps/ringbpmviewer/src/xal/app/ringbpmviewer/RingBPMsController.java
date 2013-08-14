package xal.app.ringbpmviewer;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.*;

import xal.tools.scan.UpdatingEventController;
import xal.tools.xml.*;
import xal.tools.swing.*;
import xal.tools.data.DataAdaptor;

import xal.tools.plot.barchart.*;

/**
 *  The GUI panel for ring BPM data representation.
 *
 *@author     shishlo
 */
public class RingBPMsController {

	private JPanel mainBPMsPanel = new JPanel(new BorderLayout());

	private JPanel graphAndKnobPanel = new JPanel(new BorderLayout());
	private JPanel northKnobPanel = new JPanel(new BorderLayout());
	private JPanel graphsPanel = new JPanel(new GridLayout(0, 1));
	private JPanel southKnobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));

	private JPanel bpmsPanel = new JPanel(new BorderLayout());

	//The set of ring BPMs
	private RingBPMset ring_bpm_set = null;

	// knobs for the graphs (bar charts) showing and policy about empty spaces if
	// user does not want to show specific BPM
	private JRadioButton showChartXButton = new JRadioButton("x", true);
	private JRadioButton showChartYButton = new JRadioButton("y", true);
	private JRadioButton showChartAmpButton = new JRadioButton("amp", true);

	private JRadioButton showEmptySpacesOnChartButton = new JRadioButton("Empty Spaces", true);
	private JButton selectAllBPM_Button = new JButton("SELECT ALL BPMs");
	private JButton deselectAllBPM_Button = new JButton("UN-SELECT ALL BPMs");

	//charts for X,Y,Amplitude
	private BarChart barChartX = new BarChart();
	private BarChart barChartY = new BarChart();
	private BarChart barChartAmp = new BarChart();

	//Titles for charts
	private String barChartX_avgTitle = "x [mm] - bpm signal";
	private String barChartY_avgTitle = "y [mm] - bpm signal";
	private String barChartAmp_avgTitle = "amplitude - bpm signal";

	private String barChartX_diffTitle = "turn N1 and N2 diff. x [mm]";
	private String barChartY_diffTitle = "turn N1 and N2 diff. y [mm]";
	private String barChartAmp_diffTitle = "turn N1 and N2 diff. amplitude";

	//-------------------------------------------------------------
	//south knobs panel
	//-------------------------------------------------------------

	//the controll knobs
	private JRadioButton listenToEPICS_Button = new JRadioButton("Listen to EPICS", false);
	private JButton clearMemStack_Button = new JButton("Clear Memory Stack");

	private JLabel stackSize_Label =
			new JLabel("memory stack size", JLabel.LEFT);
	private JSpinner stackSize_Spinner =
			new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));

	private JLabel fromTurn_Label =
			new JLabel("Avg. from turn:", JLabel.LEFT);
	private JSpinner fromTurn_Spinner =
			new JSpinner(new SpinnerNumberModel(1, 1, 3000, 1));

	private JLabel toTurn_Label =
			new JLabel(" to:", JLabel.LEFT);
	private JSpinner toTurn_Spinner =
			new JSpinner(new SpinnerNumberModel(30, 1, 3000, 1));

	private JRadioButton showDiffButton = new JRadioButton("Show Difference Signal", false);

	private JLabel plusTurn_Label =
			new JLabel("Turn N1:", JLabel.LEFT);
	private JSpinner plusTurn_Spinner =
			new JSpinner(new SpinnerNumberModel(1, 1, 3000, 1));

	private JLabel minusTurn_Label =
			new JLabel(" N2:", JLabel.LEFT);
	private JSpinner minusTurn_Spinner =
			new JSpinner(new SpinnerNumberModel(2, 1, 3000, 1));

	//update controller for the redrawing the bar charts
	private UpdatingEventController uc = null;

	//update controller for the changing the set of bar columns in charts
	private UpdatingEventController ucContent = null;

	private FortranNumberFormat frmt = new FortranNumberFormat("G10.4");


	/**
	 *  Constructor for the RingBPMsController object
	 *
	 *@param  ucIn         The update controller for the redrawing the bar charts
	 *@param  ucContentIn  The update controller for the changing the set of bar
	 *      columns in charts
	 */
	public RingBPMsController(UpdatingEventController ucIn, UpdatingEventController ucContentIn) {

		uc = ucIn;
		ucContent = ucContentIn;
		ring_bpm_set = new RingBPMset(uc, ucContent);

		barChartX.setTitle(barChartX_avgTitle);
		barChartY.setTitle(barChartY_avgTitle);
		barChartAmp.setTitle(barChartAmp_avgTitle);

		barChartX.setAxisNameY("x [mm]");
		barChartY.setAxisNameY("y [mm]");
		barChartAmp.setAxisNameY("amplitude");

		barChartX.getGraphPanel().setOffScreenImageDrawing(true);
		barChartY.getGraphPanel().setOffScreenImageDrawing(true);
		barChartAmp.getGraphPanel().setOffScreenImageDrawing(true);

		//button decoration
		showChartXButton.setForeground(Color.blue);
		showChartYButton.setForeground(Color.blue);
		showChartAmpButton.setForeground(Color.blue);

		listenToEPICS_Button.setForeground(Color.blue);
		clearMemStack_Button.setForeground(Color.blue);

		//set all tooltips
		showChartXButton.setToolTipText("Shows x-positions bar charts");
		showChartYButton.setToolTipText("Shows y-positions bar charts");
		showChartAmpButton.setToolTipText("Shows amplitudes bar charts");

		listenToEPICS_Button.setToolTipText("Listen to EPICS channels");
		clearMemStack_Button.setToolTipText("Remove all existing data");
		showEmptySpacesOnChartButton.setToolTipText("Shows the unchecked BPMs as empty spaces");

		stackSize_Label.setToolTipText("Defines the memory stack size");
		stackSize_Spinner.setToolTipText("Defines the memory stack size");

		//update controllers listeners
		uc.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(showChartXButton.isSelected()) {
						barChartX.updateChart();
					}
					if(showChartYButton.isSelected()) {
						barChartY.updateChart();
					}
					if(showChartAmpButton.isSelected()) {
						barChartAmp.updateChart();
					}
				}
			});

		ucContent.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateBPMsetOnGraphs();
				}
			});

		//buttons action
		showChartXButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					composeBarChartsPanel();
				}
			});

		showChartYButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					composeBarChartsPanel();
				}
			});

		showChartAmpButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					composeBarChartsPanel();
				}
			});

		showEmptySpacesOnChartButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateBPMsetOnGraphs();
				}
			});

		selectAllBPM_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ring_bpm_set.setSwitchedOn(true);
				}
			});

		deselectAllBPM_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ring_bpm_set.setSwitchedOn(false);
				}
			});

		listenToEPICS_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ring_bpm_set.setListenToEPICS(listenToEPICS_Button.isSelected());
				}
			});

		clearMemStack_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ring_bpm_set.clearStack();
				}
			});

		showDiffButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(showDiffButton.isSelected()) {
						barChartX.setTitle(barChartX_diffTitle);
						barChartY.setTitle(barChartY_diffTitle);
						barChartAmp.setTitle(barChartAmp_diffTitle);
					} else {
						barChartX.setTitle(barChartX_avgTitle);
						barChartY.setTitle(barChartY_avgTitle);
						barChartAmp.setTitle(barChartAmp_avgTitle);
					}
					updateBPMsetOnGraphs();
					mainBPMsPanel.validate();
					mainBPMsPanel.repaint();
				}
			});

		stackSize_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					ring_bpm_set.setStackCapacity((((Integer) stackSize_Spinner.getValue()).intValue()));
				}
			});

		fromTurn_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					ring_bpm_set.setStartIndex((((Integer) fromTurn_Spinner.getValue()).intValue() - 1));
				}
			});

		toTurn_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					ring_bpm_set.setStopIndex((((Integer) toTurn_Spinner.getValue()).intValue()));
				}
			});

		plusTurn_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					ring_bpm_set.setPlusDiffIndex((((Integer) plusTurn_Spinner.getValue()).intValue() - 1));
				}
			});

		minusTurn_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					ring_bpm_set.setMinusDiffIndex((((Integer) minusTurn_Spinner.getValue()).intValue() - 1));
				}
			});

		Border etchedBorder = BorderFactory.createEtchedBorder();
		//------------------------------------------------
		//compose panels
		//------------------------------------------------

		//north button panel
		JPanel tmp_1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		tmp_1.add(showChartXButton);
		tmp_1.add(showChartYButton);
		tmp_1.add(showChartAmpButton);
		tmp_1.setBorder(etchedBorder);

		JPanel tmp_2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		tmp_2.add(showEmptySpacesOnChartButton);
		tmp_2.add(selectAllBPM_Button);
		tmp_2.add(deselectAllBPM_Button);

		northKnobPanel.add(tmp_1, BorderLayout.WEST);
		northKnobPanel.add(tmp_2, BorderLayout.CENTER);

		//south button panel
		JPanel tmp_3 = new JPanel(new GridLayout(0, 1));
		JPanel tmp_4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel tmp_5 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tmp_3.add(tmp_4);
		tmp_3.add(tmp_5);
		tmp_4.add(listenToEPICS_Button);
		tmp_5.add(clearMemStack_Button);

		JPanel tmp_6 = new JPanel(new GridLayout(0, 1));
		JPanel tmp_7 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		JPanel tmp_8 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		tmp_6.add(tmp_7);
		tmp_6.add(tmp_8);
		tmp_7.add(stackSize_Spinner);
		tmp_7.add(stackSize_Label);
		tmp_8.add(fromTurn_Label);
		tmp_8.add(fromTurn_Spinner);
		tmp_8.add(toTurn_Label);
		tmp_8.add(toTurn_Spinner);

		JPanel tmp_9 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
		tmp_9.add(tmp_3);
		tmp_9.add(tmp_6);

		JPanel tmp_10 = new JPanel(new GridLayout(2, 1));

		JPanel tmp_11 = new JPanel(new GridLayout(0, 1));
		tmp_11.add(showDiffButton);

		JPanel tmp_12 = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
		tmp_12.add(plusTurn_Label);
		tmp_12.add(plusTurn_Spinner);
		tmp_12.add(minusTurn_Label);
		tmp_12.add(minusTurn_Spinner);

		tmp_10.add(tmp_11);
		tmp_10.add(tmp_12);

		southKnobPanel.add(tmp_9);
		southKnobPanel.add(tmp_10);
		southKnobPanel.setBorder(etchedBorder);

		//set panels
		graphAndKnobPanel.add(graphsPanel, BorderLayout.CENTER);
		graphAndKnobPanel.add(northKnobPanel, BorderLayout.NORTH);

		JPanel center_tmp = new JPanel(new BorderLayout());
		center_tmp.add(graphAndKnobPanel, BorderLayout.CENTER);
		center_tmp.add(southKnobPanel, BorderLayout.SOUTH);

		mainBPMsPanel.add(center_tmp, BorderLayout.CENTER);
		mainBPMsPanel.add(bpmsPanel, BorderLayout.WEST);

		composeBarChartsPanel();
		setListenToEPICS(listenToEPICS_Button.isSelected());
	}


	/**
	 *  Gets the panel attribute of the RingBPMsController object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return mainBPMsPanel;
	}


	/**
	 *  Creates the left vertical panel with BPM buttons
	 */
	private void makeBPMsSubpanel() {
		bpmsPanel.removeAll();

		JPanel tmpPanel = new JPanel(new GridLayout(0, 1));
		for(int i = 0, n = ring_bpm_set.size(); i < n; i++) {
			tmpPanel.add(ring_bpm_set.getRingBPM(i).getOnButton());
		}

		JPanel tmpPanel_1 = new JPanel(new BorderLayout());
		tmpPanel_1.add(tmpPanel, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(tmpPanel_1,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		bpmsPanel.add(scrollPane, BorderLayout.CENTER);
		mainBPMsPanel.validate();
		mainBPMsPanel.repaint();
	}


	/**
	 *  Description of the Method
	 */
	private void composeBarChartsPanel() {
		graphsPanel.removeAll();
		if(showChartXButton.isSelected()) {
			graphsPanel.add(barChartX.getPanel());
		}
		if(showChartYButton.isSelected()) {
			graphsPanel.add(barChartY.getPanel());
		}
		if(showChartAmpButton.isSelected()) {
			graphsPanel.add(barChartAmp.getPanel());
		}
		updateBPMsetOnGraphs();
		graphsPanel.validate();
		graphsPanel.repaint();
	}


	/**
	 *  Updates the set of BPMs that are showed on Bar Chart graphs
	 */
	public void updateBPMsetOnGraphs() {
		if(showChartXButton.isSelected()) {
			barChartX.clear();
			if(showEmptySpacesOnChartButton.isSelected()) {
				if(!showDiffButton.isSelected()) {
					barChartX.setBarColumns(ring_bpm_set.getAllBPMs_X());
				} else {
					barChartX.setBarColumns(ring_bpm_set.getAllBPMs_DiffX());
				}
			} else {
				if(!showDiffButton.isSelected()) {
					barChartX.setBarColumns(ring_bpm_set.getAllForShowBPMs_X());
				} else {
					barChartX.setBarColumns(ring_bpm_set.getAllForShowBPMs_DiffX());
				}
			}
		}
		if(showChartYButton.isSelected()) {
			barChartY.clear();
			if(showEmptySpacesOnChartButton.isSelected()) {
				if(!showDiffButton.isSelected()) {
					barChartY.setBarColumns(ring_bpm_set.getAllBPMs_Y());
				} else {
					barChartY.setBarColumns(ring_bpm_set.getAllBPMs_DiffY());
				}
			} else {
				if(!showDiffButton.isSelected()) {
					barChartY.setBarColumns(ring_bpm_set.getAllForShowBPMs_Y());
				} else {
					barChartY.setBarColumns(ring_bpm_set.getAllForShowBPMs_DiffY());
				}
			}
		}
		if(showChartAmpButton.isSelected()) {
			barChartAmp.clear();
			if(showEmptySpacesOnChartButton.isSelected()) {
				if(!showDiffButton.isSelected()) {
					barChartAmp.setBarColumns(ring_bpm_set.getAllBPMs_AMP());
				} else {
					barChartAmp.setBarColumns(ring_bpm_set.getAllBPMs_DiffAMP());
				}
			} else {
				if(!showDiffButton.isSelected()) {
					barChartAmp.setBarColumns(ring_bpm_set.getAllForShowBPMs_AMP());
				} else {
					barChartAmp.setBarColumns(ring_bpm_set.getAllForShowBPMs_DiffAMP());
				}
			}
		}
	}


	/**
	 *  Sets the "listen to EPICS" attribute
	 *
	 *@param  listenToEPICS  The new listen to EPICS boolean value
	 */
	public void setListenToEPICS(boolean listenToEPICS) {
		listenToEPICS_Button.setSelected(listenToEPICS);
		ring_bpm_set.setListenToEPICS(listenToEPICS_Button.isSelected());
	}


	/**
	 *  Returns "true" or "false" for the "listen to EPICS"
	 *
	 *@return    The listen to EPICS boolean value
	 */
	public boolean getListenToEPICS() {
		return listenToEPICS_Button.isSelected();
	}


	/**
	 *  Returns the "listen to EPICS" JRadioButton
	 *
	 *@return    The listen to EPICS button
	 */
	public JRadioButton getListenToEPICS_Button() {
		return listenToEPICS_Button;
	}


	/**
	 *  Initializes the set of ring BPM instances from the XML Data Adapter
	 *
	 *@param  da  The XML Data Adapter
	 */
	public void init(DataAdaptor da) {
		ring_bpm_set.init(da);
		makeBPMsSubpanel();
		ring_bpm_set.setStartIndex((((Integer) fromTurn_Spinner.getValue()).intValue() - 1));
		ring_bpm_set.setStopIndex((((Integer) toTurn_Spinner.getValue()).intValue()));
		ring_bpm_set.setStackCapacity((((Integer) stackSize_Spinner.getValue()).intValue()));

		/**
		 *  /debug part - add data to the BPMavg Vector bpms =
		 *  ring_bpm_set.getAllBPMs_X(); for (int i = 0; i < bpms.size(); i++) { for
		 *  (int j = 0; j < 3; j++) { ((RingBPMtbtAvg)
		 *  bpms.get(i)).addValue(Math.sin(1.0*i*j+1) * 0.5); } } bpms =
		 *  ring_bpm_set.getAllBPMs_Y(); for (int i = 0; i < bpms.size(); i++) { for
		 *  (int j = 0; j < 3; j++) { ((RingBPMtbtAvg) bpms.get(i)).addValue(
		 *  Math.sin(1.0*i*j+1)* 2.5); } } bpms = ring_bpm_set.getAllBPMs_AMP(); for
		 *  (int i = 0; i < bpms.size(); i++) { for (int j = 0; j < 3; j++) {
		 *  ((RingBPMtbtAvg) bpms.get(i)).addValue( Math.abs(Math.sin(1.0*i*j+1))*
		 *  10.5); } }
		 */

	}


	/**
	 *  Writes all data to the data adaptor
	 *
	 *@param  da  The data adaptor
	 */
	public void dumpData(DataAdaptor da) {
		DataAdaptor bpmSet_da = da.createChild("RING_BPMs");

		DataAdaptor params_da = bpmSet_da.createChild("shared_parameters");
		params_da.setValue("show_chart_x", showChartXButton.isSelected());
		params_da.setValue("show_chart_y", showChartYButton.isSelected());
		params_da.setValue("show_chart_amp", showChartAmpButton.isSelected());
		params_da.setValue("show_empty_spaces", showEmptySpacesOnChartButton.isSelected());
		params_da.setValue("listen_epics", listenToEPICS_Button.isSelected());
		params_da.setValue("stack_size", ((Integer) stackSize_Spinner.getValue()).intValue());
		params_da.setValue("start_turn", ((Integer) fromTurn_Spinner.getValue()).intValue());
		params_da.setValue("stop_turn", ((Integer) toTurn_Spinner.getValue()).intValue());

		RingBPMtbtAvg rbpm_pv;
		RingBPM rbpm;
		DataAdaptor bpm_pv_da;

		for(int i = 0, n = ring_bpm_set.size(); i < n; i++) {
			DataAdaptor bpm_da =  bpmSet_da.createChild("RING_BPM");
			rbpm = ring_bpm_set.getRingBPM(i);
			bpm_da.setValue("on", rbpm.getSwitchedOn());
			bpm_da.setValue("name", rbpm.getBPMName());

			bpm_pv_da = (XmlDataAdaptor) bpm_da.createChild("PV_X");
			rbpm_pv = rbpm.getBPM_X();
			bpm_pv_da.setValue("pv_name", rbpm_pv.getNamePV());
			for(int j = 0, nn = rbpm_pv.size(); j < nn; j++) {
				DataAdaptor bpm_pv_val_da = bpm_pv_da.createChild("Value");
				bpm_pv_val_da.setValue("value", frmt.format(rbpm_pv.value(j)));
			}

			bpm_pv_da = bpm_da.createChild("PV_Y");
			rbpm_pv = rbpm.getBPM_Y();
			bpm_pv_da.setValue("pv_name", rbpm_pv.getNamePV());
			for(int j = 0, nn = rbpm_pv.size(); j < nn; j++) {
				DataAdaptor bpm_pv_val_da = bpm_pv_da.createChild("Value");
				bpm_pv_val_da.setValue("value", frmt.format(rbpm_pv.value(j)));
			}

			bpm_pv_da = bpm_da.createChild("PV_AMP");
			rbpm_pv = rbpm.getBPM_AMP();
			bpm_pv_da.setValue("pv_name", rbpm_pv.getNamePV());
			for(int j = 0, nn = rbpm_pv.size(); j < nn; j++) {
				DataAdaptor bpm_pv_val_da = bpm_pv_da.createChild("Value");
				bpm_pv_val_da.setValue("value", frmt.format(rbpm_pv.value(j)));
			}
		}
	}


	/**
	 *  Reads the data to create BPMs structure and fills out stack data from the
	 *  data adaptor
	 *
	 *@param  da  The data adaptor
	 */
	public void readData(DataAdaptor da) {
		ring_bpm_set.clear();
		ring_bpm_set.setPlusDiffIndex(0);
		ring_bpm_set.setMinusDiffIndex(1);
		updateBPMsetOnGraphs();

		DataAdaptor bpmSet_da = da.childAdaptor("RING_BPMs");

		DataAdaptor params_da = bpmSet_da.childAdaptor("shared_parameters");
		showChartXButton.setSelected(params_da.booleanValue("show_chart_x"));
		showChartYButton.setSelected(params_da.booleanValue("show_chart_y"));
		showChartAmpButton.setSelected(params_da.booleanValue("show_chart_amp"));
		showEmptySpacesOnChartButton.setSelected(params_da.booleanValue("show_empty_spaces"));
		listenToEPICS_Button.setSelected(params_da.booleanValue("listen_epics"));
		stackSize_Spinner.setValue(new Integer(params_da.intValue("stack_size")));
		fromTurn_Spinner.setValue(new Integer(params_da.intValue("start_turn")));
		toTurn_Spinner.setValue(new Integer(params_da.intValue("stop_turn")));

		plusTurn_Spinner.setValue(new Integer(ring_bpm_set.getPlusDiffIndex()+1));
		minusTurn_Spinner.setValue(new Integer(ring_bpm_set.getMinusDiffIndex()+1));

		int localStackSize = params_da.intValue("stack_size");

		RingBPMtbtAvg rbpm_pv;
		RingBPM rbpm;
		DataAdaptor bpm_pv_da;

        for (final DataAdaptor bpm_da : bpmSet_da.childAdaptors("RING_BPM")){
			rbpm = new RingBPM(uc, ucContent);
			rbpm.setBPMName(bpm_da.stringValue("name"));
			rbpm.setStackCapacity(localStackSize);

			rbpm_pv = rbpm.getBPM_X();
			bpm_pv_da = (XmlDataAdaptor) bpm_da.childAdaptor("PV_X");
			rbpm_pv.setNamePV(bpm_pv_da.stringValue("pv_name"));
			for (final DataAdaptor bpm_pv_val_da : bpm_pv_da.childAdaptors("Value")){
				rbpm_pv.addValue(bpm_pv_val_da.doubleValue("value"));
			}

			rbpm_pv = rbpm.getBPM_Y();
			bpm_pv_da = (XmlDataAdaptor) bpm_da.childAdaptor("PV_Y");
			rbpm_pv.setNamePV(bpm_pv_da.stringValue("pv_name"));
			for (final DataAdaptor bpm_pv_val_da : bpm_pv_da.childAdaptors("Value")){
				rbpm_pv.addValue(bpm_pv_val_da.doubleValue("value"));
			}

			rbpm_pv = rbpm.getBPM_AMP();
			bpm_pv_da = (XmlDataAdaptor) bpm_da.childAdaptor("PV_AMP");
			rbpm_pv.setNamePV(bpm_pv_da.stringValue("pv_name"));
			for (final DataAdaptor bpm_pv_val_da : bpm_pv_da.childAdaptors("Value")){
				rbpm_pv.addValue(bpm_pv_val_da.doubleValue("value"));
			}

			rbpm.setSwitchedOn(bpm_da.booleanValue("on"));
			ring_bpm_set.addRingBPM(rbpm);
		}

		ring_bpm_set.setStackCapacity(localStackSize);
		ring_bpm_set.setStartIndex(((Integer) fromTurn_Spinner.getValue()).intValue());
		ring_bpm_set.setStopIndex(((Integer) toTurn_Spinner.getValue()).intValue());
		ring_bpm_set.setListenToEPICS(listenToEPICS_Button.isSelected());

		makeBPMsSubpanel();
		composeBarChartsPanel();
	}


	/**
	 *  Returns the set of ring BPMs
	 *
	 *@return    The set of ring BPMs
	 */
	public RingBPMset getRingBPMset() {
		return ring_bpm_set;
	}


	/**
	 *  Sets the font for RingBPM buttons
	 *
	 *@param  fnt  The new font
	 */
	public void setFont(Font fnt) {
		ring_bpm_set.setFont(fnt);

		showChartXButton.setFont(fnt);
		showChartYButton.setFont(fnt);
		showChartAmpButton.setFont(fnt);
		showEmptySpacesOnChartButton.setFont(fnt);

		selectAllBPM_Button.setFont(fnt);
		deselectAllBPM_Button.setFont(fnt);

		listenToEPICS_Button.setFont(fnt);
		clearMemStack_Button.setFont(fnt);

		stackSize_Label.setFont(fnt);
		fromTurn_Label.setFont(fnt);
		toTurn_Label.setFont(fnt);

		showDiffButton.setFont(fnt);
		plusTurn_Label.setFont(fnt);
		minusTurn_Label.setFont(fnt);

		stackSize_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) stackSize_Spinner.getEditor()).getTextField().setFont(fnt);

		fromTurn_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) fromTurn_Spinner.getEditor()).getTextField().setFont(fnt);

		toTurn_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) toTurn_Spinner.getEditor()).getTextField().setFont(fnt);

		plusTurn_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) plusTurn_Spinner.getEditor()).getTextField().setFont(fnt);

		minusTurn_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) minusTurn_Spinner.getEditor()).getTextField().setFont(fnt);
	}
}

