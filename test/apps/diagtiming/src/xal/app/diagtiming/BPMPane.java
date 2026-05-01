package xal.app.diagtiming;

import java.util.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;

import xal.smf.*;
import xal.smf.impl.BPM;
import xal.smf.impl.RingBPM;
import xal.ca.*;
import xal.tools.apputils.EdgeLayout;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.DecimalField;
import xal.smf.AcceleratorNode;

public class BPMPane extends JPanel implements ConnectionListener, ActionListener {
	static final long serialVersionUID = 0;

	String[] bpmNames;

	JTable bpmTable;

	DeviceTableModel bpmTableModel;

	DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();

	Channel[] bpmDelays, bpmDelayRBs, bpmAvgStarts, bpmAvgStops, bpmChopFreqs, bpmTDelays;

	Channel[] st1LenChs, st1GainChs, st1MthdChs;

	Channel[] st2LenChs, st2GainChs, st2MthdChs;

	Channel[] st3LenChs, st3GainChs, st3MthdChs;

	Channel[] st4LenChs, st4GainChs, st4MthdChs;

	Channel[] st1MthdLenChs, st2MthdLenChs, st3MthdLenChs, st4MthdLenChs;

	Channel[] trigEvtChs, trigEvtRbChs;

	Channel[] freqChs, freqRbChs;

	Channel[] betaChs, betaRbChs;
	
	Channel[] opModeChs, opModeRbChs;
	
	Channel[] trnDlyChs, trnDlyRbChs;

	Channel[] bpmAvgWidthSets, bpmWidthSets, bpmSamplingSets;

	Channel[] st1LenSetChs, st1GainSetChs, st1MthdSetChs;

	Channel[] st2LenSetChs, st2GainSetChs, st2MthdSetChs;

	Channel[] st3LenSetChs, st3GainSetChs, st3MthdSetChs;

	Channel[] st4LenSetChs, st4GainSetChs, st4MthdSetChs;
	
	Channel[] gainSatChs;
	
	Channel[] xWFChs, yWFChs;

	private HashMap<String, Vector<InputPVTableCell>> monitorQueues = new HashMap<String, Vector<InputPVTableCell>>();

	/** List of the monitors */
	final Vector<Monitor> mons = new Vector<Monitor>();

	int selectedRow;

	Monitor phaseArryMonitor = null;

	Monitor ampArryMonitor = null;

	Monitor xTBTArryMonitor = null;

	Monitor yTBTArryMonitor = null;

	Monitor beamWidthMonitor = null;

	FunctionGraphsJPanel phasePlot, ampPlot;

	FunctionGraphsJPanel xTBTPlot, yTBTPlot;

	CurveData phasePlotData = new CurveData();

	CurveData ampPlotData = new CurveData();

	CurveData xTBTPlotData = new CurveData();

	CurveData yTBTPlotData = new CurveData();

	double[] x1 = new double[1100];

	double[] x2 = new double[1060];

	double[] yPhase = new double[1100];

	double[] yAmp = new double[1100];

	double[] yXTBT = new double[1060];

	double[] yYTBT = new double[1060];

	DecimalField beamWidthFld;

	double beamWidth = 0.;

	JButton findOne, setOne, findAll, prepAll1, setAll, refresh;

	JButton set1RBPM, findAllRTiming, prepAll2, setAllRBPMs;

	CalcLinacTiming calcT;

	CalcLinacTiming[] calcAllT;

	NumberFormat nf = NumberFormat.getNumberInstance();

	DecimalField startField, endField;

	double low = 0.;

	double high = 50.;

	java.util.List<AcceleratorNode> theNodes;

	/**
	 * 0 = linac BPM, 1 = ring BPM, 2 = RTBT BPM
	 */
	int typeInd = 0;

	// HashMap to link between numbers and names for trigger event
	HashMap<String, String> trigMap = new HashMap<String, String>(48);

	HashMap<Integer, String> trigMap1 = new HashMap<Integer, String>(48);

	// HashMap to link between numbers and names for gain
	HashMap<String, String> gainMap = new HashMap<String, String>(48);

	HashMap<Integer, String> gainMap1 = new HashMap<Integer, String>(48);

	// HashMap to link between numbers and names for analysis
	HashMap<String, String> analysisMap = new HashMap<String, String>(48);

	HashMap<Integer, String> analysisMap1 = new HashMap<Integer, String>(48);

	// HashMap to link between numbers and names for operation mode
	HashMap<String, String> operModeMap = new HashMap<String, String>(48);

	HashMap<Integer, String> operModeMap1 = new HashMap<Integer, String>(48);
	
	JComboBox<String> setMode;

	public BPMPane(int type) {
		typeInd = type;
	}

	protected void initializeBPMPane(java.util.List<AcceleratorNode> nodes) {
		theNodes = nodes;

		// for both Linac and Ring BPMs
		bpmDelays = new Channel[nodes.size()];
		xWFChs = new Channel[nodes.size()];
		yWFChs = new Channel[nodes.size()];

		// for Linac BPMs
		if (typeInd == 0) {
			String[] columnNames = { "BPM", "Delay (us)", "avg_start", "avg_stop",
					"chop freq.", "Turns Delay", "new Delay", "new avg_start", "new avg_stop",
					"new chop freq.", "new Turns Delay"};

			bpmTableModel = new DeviceTableModel(columnNames, nodes.size());
			bpmTable = new JTable(bpmTableModel);
			bpmTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			// bpmTable.setPreferredSize(new Dimension(1000, 1000));

			bpmNames = new String[nodes.size()];
			bpmAvgStarts = new Channel[nodes.size()];
			bpmAvgStops = new Channel[nodes.size()];
			bpmChopFreqs = new Channel[nodes.size()];
			bpmTDelays = new Channel[nodes.size()];

			InputPVTableCell pvCell1, pvCell2, pvCell3, pvCell4, pvCell5;
			ChannelFactory caF = ChannelFactory.defaultFactory();

			for (int i = 0; i < nodes.size(); i++) {
// BPM theNode = (BPM) nodes.get(i);
				bpmNames[i] = ( nodes.get(i)).getId();

				bpmTableModel.addRowName(bpmNames[i], i);

				String dlyChName = bpmNames[i] + ":Delay00_Rb";
				String avgStartChName = bpmNames[i] + ":avgStart_Rb";
				String avgStopChName = bpmNames[i] + ":avgStop_Rb";
				String chopFreqChName = bpmNames[i] + ":chopFreq_Rb";
				String trnDlyChName = bpmNames[i] + ":TurnsDelay00_Rb";
				
				xWFChs[i] = caF.getChannel(bpmNames[i] + ":beamPA");
				yWFChs[i] = caF.getChannel(bpmNames[i] + ":beamIA");

				try {
					bpmDelays[i] = caF.getChannel(dlyChName);
					if (bpmDelays[i] != null) {
						pvCell1 = new InputPVTableCell(typeInd, bpmDelays[i], i, 1);
						bpmTableModel.addPVCell(pvCell1, i, 1);
						getChannelVec(bpmDelays[i]).add(pvCell1);
					}

					bpmAvgStarts[i] = caF.getChannel(avgStartChName);
					if (bpmAvgStarts[i] != null) {
						pvCell2 = new InputPVTableCell(typeInd, bpmAvgStarts[i], i, 2);
						bpmTableModel.addPVCell(pvCell2, i, 2);
						getChannelVec(bpmAvgStarts[i]).add(pvCell2);
					}

					bpmAvgStops[i] = caF.getChannel(avgStopChName);
					if (bpmAvgStops[i] != null) {
						pvCell3 = new InputPVTableCell(typeInd, bpmAvgStops[i], i, 3);
						bpmTableModel.addPVCell(pvCell3, i, 3);
						getChannelVec(bpmAvgStops[i]).add(pvCell3);
					}

					bpmChopFreqs[i] = caF.getChannel(chopFreqChName);
					if (bpmChopFreqs[i] != null) {
						pvCell4 = new InputPVTableCell(typeInd, bpmChopFreqs[i], i, 4);
						bpmTableModel.addPVCell(pvCell4, i, 4);
						getChannelVec(bpmChopFreqs[i]).add(pvCell4);
					}

					bpmTDelays[i] = caF.getChannel(trnDlyChName);
					if (bpmTDelays[i] != null) {
						pvCell5 = new InputPVTableCell(typeInd, bpmTDelays[i], i, 5);
						bpmTableModel.addPVCell(pvCell5, i, 5);
						getChannelVec(bpmTDelays[i]).add(pvCell5);
					}

				} catch (NoSuchChannelException e) {
					System.out.println(e);
				}

			}
		}
		// for Ring BPMs
		else {
			String[] columnNames = { "BPM", "Gain Ind.", "Dly (s)", "TrggEvt", "S1 trns",
					"S1 gain", "S1 mthd", "S2 trns", "S2 gain", "S2 mthd",
					"S3 trns", "S3 gain", "S3 mthd", "S4 trns", "S4 gain",
					"S4 mthd", "Freq_Mode", "direct_freq", "direct_beta",
					"1st Turn Dly"};

			bpmTableModel = new DeviceTableModel(columnNames, nodes.size());
			bpmTable = new JTable(bpmTableModel);
			bpmTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			bpmTable.getColumnModel().getColumn(2).setPreferredWidth(100);
			bpmTable.getColumnModel().getColumn(3).setPreferredWidth(150);
			bpmTable.getColumnModel().getColumn(5).setPreferredWidth(150);
			bpmTable.getColumnModel().getColumn(8).setPreferredWidth(150);
			bpmTable.getColumnModel().getColumn(11).setPreferredWidth(150);
			bpmTable.getColumnModel().getColumn(14).setPreferredWidth(150);
			bpmTable.getColumnModel().getColumn(19).setPreferredWidth(150);

			bpmNames = new String[nodes.size()];
// bpmDelays = new Channel[nodes.size()];
			bpmDelayRBs = new Channel[nodes.size()];

			st1LenChs = new Channel[nodes.size()];
			st1GainChs = new Channel[nodes.size()];
			st1MthdChs = new Channel[nodes.size()];
			st2LenChs = new Channel[nodes.size()];
			st2GainChs = new Channel[nodes.size()];
			st2MthdChs = new Channel[nodes.size()];
			st3LenChs = new Channel[nodes.size()];
			st3GainChs = new Channel[nodes.size()];
			st3MthdChs = new Channel[nodes.size()];
			st4LenChs = new Channel[nodes.size()];
			st4GainChs = new Channel[nodes.size()];
			st4MthdChs = new Channel[nodes.size()];
			freqChs = new Channel[nodes.size()];
			freqRbChs = new Channel[nodes.size()];
			betaChs = new Channel[nodes.size()];
			betaRbChs = new Channel[nodes.size()];
			opModeChs = new Channel[nodes.size()];
			opModeRbChs = new Channel[nodes.size()];
			trnDlyChs = new Channel[nodes.size()];
			trnDlyRbChs = new Channel[nodes.size()];

			trigEvtChs = new Channel[nodes.size()];
			trigEvtRbChs = new Channel[nodes.size()];

			st1LenSetChs = new Channel[nodes.size()];
			st1GainSetChs = new Channel[nodes.size()];
			st1MthdSetChs = new Channel[nodes.size()];
			st1MthdLenChs = new Channel[nodes.size()];
			st2LenSetChs = new Channel[nodes.size()];
			st2GainSetChs = new Channel[nodes.size()];
			st2MthdSetChs = new Channel[nodes.size()];
			st2MthdLenChs = new Channel[nodes.size()];
			st3LenSetChs = new Channel[nodes.size()];
			st3GainSetChs = new Channel[nodes.size()];
			st3MthdSetChs = new Channel[nodes.size()];
			st3MthdLenChs = new Channel[nodes.size()];
			st4LenSetChs = new Channel[nodes.size()];
			st4GainSetChs = new Channel[nodes.size()];
			st4MthdSetChs = new Channel[nodes.size()];
			st4MthdLenChs = new Channel[nodes.size()];
			
			gainSatChs = new Channel[nodes.size()];

			InputPVTableCell tDelayCell;
			ComboBoxPVCell trigEvtCell;
			InputPVTableCell s1LenCell, s2LenCell, s3LenCell, s4LenCell;
			InputPVTableCell s1GainCell, s2GainCell, s3GainCell, s4GainCell;
			InputPVTableCell s1MthdCell, s2MthdCell, s3MthdCell, s4MthdCell;
			InputPVTableCell freqCell, betaCell, opModeCell, trnDlyCell;
			InputPVTableCell gainSatCell;
			ChannelFactory caF = ChannelFactory.defaultFactory();

			for (int i = 0; i < nodes.size(); i++) {
				RingBPM theNode = (RingBPM) nodes.get(i);
				bpmNames[i] = (nodes.get(i)).getId();

				bpmTableModel.addRowName(bpmNames[i], i);

				String dlyChName = theNode.getId() + ":TriggerDelay";
				String dlyRbChName = theNode.getId() + ":TriggerDelay_RB";
				String trigEvtChName = theNode.getId() + ":TriggerEvent";
				String trigEvtRbChName = theNode.getId() + ":TriggerEvent_RB";

				String opModeChName = theNode.getId() + ":OperMode";
				String opModeRbChName = theNode.getId() + ":OperMode_RB";
				String freqChName = theNode.getId() + ":Direct_RingFreq";
				String freqRbChName = theNode.getId() + ":Direct_RingFreq_RB";
				String betaChName = theNode.getId() + ":Direct_Beta";
				String betaRbChName = theNode.getId() + ":Direct_Beta_RB";
				String trnDlyChName = theNode.getId() + ":FirstTurnDelay";
				String trnDlyRbChName = theNode.getId() + ":FirstTurnDelay_RB";
				
				String gainSatChName = theNode.getId() + ":SignalAnalysis_Saturated";

				xWFChs[i] = caF.getChannel(bpmNames[i] + ":xTBT");
				yWFChs[i] = caF.getChannel(bpmNames[i] + ":yTBT");
				
				try {
					gainSatChs[i] = caF.getChannel(gainSatChName);
					if (gainSatChs[i] != null) {
						gainSatCell = new InputPVTableCell(typeInd, gainSatChs[i], i, 1);
						bpmTableModel.addPVCell(gainSatCell, i, 1);
						getChannelVec(gainSatChs[i]).add(gainSatCell);
					}
					
					bpmDelays[i] = caF.getChannel(dlyChName);
					bpmDelayRBs[i] = caF.getChannel(dlyRbChName);
					if (bpmDelayRBs[i] != null) {
						tDelayCell = new InputPVTableCell(typeInd, bpmDelayRBs[i], i, 2);
						bpmTableModel.addPVCell(tDelayCell, i, 2);
						getChannelVec(bpmDelayRBs[i]).add(tDelayCell);
					}

					trigEvtChs[i] = caF.getChannel(trigEvtChName);
					trigEvtRbChs[i] = caF.getChannel(trigEvtRbChName);

					if (trigEvtRbChs[i] != null) {
						trigEvtCell = new ComboBoxPVCell(trigEvtRbChs[i], i, 3,
								this);
						bpmTableModel.addPVCell(trigEvtCell, i, 3);
						getChannelVec(trigEvtRbChs[i]).add(trigEvtCell);
					}

					st1LenChs[i] = theNode
							.getChannel(RingBPM.STAGE1_LEN_RB_HANDLE);
					st1LenSetChs[i] = theNode
							.getChannel(RingBPM.STAGE1_LEN_HANDLE);
					st1MthdLenChs[i] = caF.getChannel(theNode.getId()
							+ ":Analysis_Turns1");
					s1LenCell = new InputPVTableCell(typeInd, st1LenChs[i], i, 4);
					bpmTableModel.addPVCell(s1LenCell, i, 4);
					getChannelVec(st1LenChs[i]).add(s1LenCell);

					st1GainChs[i] = theNode
							.getChannel(RingBPM.STAGE1_GAIN_RB_HANDLE);
					st1GainSetChs[i] = theNode
							.getChannel(RingBPM.STAGE1_GAIN_HANDLE);
					if (st1GainChs[i] != null) {
						s1GainCell = new ComboBoxPVCell(st1GainChs[i], i, 5,
								this);
						bpmTableModel.addPVCell(s1GainCell, i, 5);
						getChannelVec(st1GainChs[i]).add(s1GainCell);
					}					
					
					st1MthdChs[i] = theNode
							.getChannel(RingBPM.STAGE1_METHOD_RB_HANDLE);
					st1MthdSetChs[i] = theNode
							.getChannel(RingBPM.STAGE1_METHOD_HANDLE);
					if (st1MthdChs[i] != null) {
						s1MthdCell = new ComboBoxPVCell(st1MthdChs[i], i, 6,
								this);
						bpmTableModel.addPVCell(s1MthdCell, i, 6);
						getChannelVec(st1MthdChs[i]).add(s1MthdCell);
					}					

					st2LenChs[i] = theNode
							.getChannel(RingBPM.STAGE2_LEN_RB_HANDLE);
					st2LenSetChs[i] = theNode
							.getChannel(RingBPM.STAGE2_LEN_HANDLE);
					st2MthdLenChs[i] = caF.getChannel(theNode.getId()
							+ ":Analysis_Turns2");
					s2LenCell = new InputPVTableCell(typeInd, st2LenChs[i], i, 7);
					bpmTableModel.addPVCell(s2LenCell, i, 7);
					getChannelVec(st2LenChs[i]).add(s2LenCell);

					st2GainChs[i] = theNode
							.getChannel(RingBPM.STAGE2_GAIN_RB_HANDLE);
					st2GainSetChs[i] = theNode
							.getChannel(RingBPM.STAGE2_GAIN_HANDLE);
					if (st2GainChs[i] != null) {
						s2GainCell = new ComboBoxPVCell(st2GainChs[i], i, 8,
								this);
						bpmTableModel.addPVCell(s2GainCell, i, 8);
						getChannelVec(st2GainChs[i]).add(s2GainCell);
					}					

					st2MthdChs[i] = theNode
							.getChannel(RingBPM.STAGE2_METHOD_RB_HANDLE);
					st2MthdSetChs[i] = theNode
							.getChannel(RingBPM.STAGE2_METHOD_HANDLE);
					if (st2MthdChs[i] != null) {
						s2MthdCell = new ComboBoxPVCell(st2MthdChs[i], i, 9,
								this);
						bpmTableModel.addPVCell(s2MthdCell, i, 9);
						getChannelVec(st2MthdChs[i]).add(s2MthdCell);
					}					

					st3LenChs[i] = theNode
							.getChannel(RingBPM.STAGE3_LEN_RB_HANDLE);
					st3LenSetChs[i] = theNode
							.getChannel(RingBPM.STAGE3_LEN_HANDLE);
					st3MthdLenChs[i] = caF.getChannel(theNode.getId()
							+ ":Analysis_Turns3");
					s3LenCell = new InputPVTableCell(typeInd, st3LenChs[i], i, 10);
					bpmTableModel.addPVCell(s3LenCell, i, 10);
					getChannelVec(st3LenChs[i]).add(s3LenCell);

					st3GainChs[i] = theNode
							.getChannel(RingBPM.STAGE3_GAIN_RB_HANDLE);
					st3GainSetChs[i] = theNode
							.getChannel(RingBPM.STAGE3_GAIN_HANDLE);
					if (st3GainChs[i] != null) {
						s3GainCell = new ComboBoxPVCell(st3GainChs[i], i, 11,
								this);
						bpmTableModel.addPVCell(s3GainCell, i, 11);
						getChannelVec(st3GainChs[i]).add(s3GainCell);
					}					

					st3MthdChs[i] = theNode
							.getChannel(RingBPM.STAGE3_METHOD_RB_HANDLE);
					st3MthdSetChs[i] = theNode
							.getChannel(RingBPM.STAGE3_METHOD_HANDLE);
					if (st3MthdChs[i] != null) {
						s3MthdCell = new ComboBoxPVCell(st3MthdChs[i], i, 12,
								this);
						bpmTableModel.addPVCell(s3MthdCell, i, 12);
						getChannelVec(st3MthdChs[i]).add(s3MthdCell);
					}					

					st4LenChs[i] = theNode
							.getChannel(RingBPM.STAGE4_LEN_RB_HANDLE);
					st4LenSetChs[i] = theNode
							.getChannel(RingBPM.STAGE4_LEN_HANDLE);
					st4MthdLenChs[i] = caF.getChannel(theNode.getId()
							+ ":Analysis_Turns4");
					s4LenCell = new InputPVTableCell(typeInd, st4LenChs[i], i, 13);
					bpmTableModel.addPVCell(s4LenCell, i, 13);
					getChannelVec(st4LenChs[i]).add(s4LenCell);

					st4GainChs[i] = theNode
							.getChannel(RingBPM.STAGE4_GAIN_RB_HANDLE);
					st4GainSetChs[i] = theNode
							.getChannel(RingBPM.STAGE4_GAIN_HANDLE);
					if (st4GainChs[i] != null) {
						s4GainCell = new ComboBoxPVCell(st4GainChs[i], i, 14,
								this);
						bpmTableModel.addPVCell(s4GainCell, i, 14);
						getChannelVec(st4GainChs[i]).add(s4GainCell);
					}					

					st4MthdChs[i] = theNode
							.getChannel(RingBPM.STAGE4_METHOD_RB_HANDLE);
					st4MthdSetChs[i] = theNode
							.getChannel(RingBPM.STAGE4_METHOD_HANDLE);
					if (st4MthdChs[i] != null) {
						s4MthdCell = new ComboBoxPVCell(st4MthdChs[i], i, 15,
								this);
						bpmTableModel.addPVCell(s4MthdCell, i, 15);
						getChannelVec(st4MthdChs[i]).add(s4MthdCell);
					}					

					opModeChs[i] = caF.getChannel(opModeChName);
					opModeRbChs[i] = caF.getChannel(opModeRbChName);
					if (opModeRbChs[i] != null) {
						opModeCell = new ComboBoxPVCell(st4GainChs[i], i, 16,
								this);
						bpmTableModel.addPVCell(opModeCell, i, 16);
						getChannelVec(opModeRbChs[i]).add(opModeCell);
					}

					freqChs[i] = caF.getChannel(freqChName);
					freqRbChs[i] = caF.getChannel(freqRbChName);
					if (freqRbChs[i] != null) {
						freqCell = new InputPVTableCell(typeInd, freqRbChs[i], i, 17);
						bpmTableModel.addPVCell(freqCell, i, 17);
						getChannelVec(freqRbChs[i]).add(freqCell);
					}

					betaChs[i] = caF.getChannel(betaChName);
					betaRbChs[i] = caF.getChannel(betaRbChName);
					if (betaRbChs[i] != null) {
						betaCell = new InputPVTableCell(typeInd, betaRbChs[i], i, 18);
						bpmTableModel.addPVCell(betaCell, i, 18);
						getChannelVec(betaRbChs[i]).add(betaCell);
					}

					trnDlyChs[i] = caF.getChannel(trnDlyChName);
					trnDlyRbChs[i] = caF.getChannel(trnDlyRbChName);
					if (trnDlyRbChs[i] != null) {
						trnDlyCell = new InputPVTableCell(typeInd, trnDlyRbChs[i], i, 19);
						bpmTableModel.addPVCell(trnDlyCell, i, 19);
						getChannelVec(trnDlyRbChs[i]).add(trnDlyCell);
					}

				} catch (NoSuchChannelException e) {
					System.out.println(e);
				}
			}
			
			setUpGainStatColumn(bpmTable.getColumnModel().getColumn(1));
			setUpTrigEvtColumn(bpmTable.getColumnModel().getColumn(3));
			setUpGainColumn(bpmTable.getColumnModel().getColumn(5));
			setUpGainColumn(bpmTable.getColumnModel().getColumn(8));
			setUpGainColumn(bpmTable.getColumnModel().getColumn(11));
			setUpGainColumn(bpmTable.getColumnModel().getColumn(14));
			setUpAnalysisColumn(bpmTable.getColumnModel().getColumn(6));
			setUpAnalysisColumn(bpmTable.getColumnModel().getColumn(9));
			setUpAnalysisColumn(bpmTable.getColumnModel().getColumn(12));
			setUpAnalysisColumn(bpmTable.getColumnModel().getColumn(15));
			setUpOperModeColumn(bpmTable.getColumnModel().getColumn(16));
		}

		bpmTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		ListSelectionModel rowSM = bpmTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					// do nothing
				} else {
					selectedRow = lsm.getMinSelectionIndex();
					updatePlot(bpmNames[selectedRow]);
					// for linac BPMs
					if (typeInd == 0) {
						bpmTableModel.setValueAt(bpmTableModel.getValueAt(selectedRow,1).toString(),selectedRow,6);
						bpmTableModel.setValueAt(bpmTableModel.getValueAt(selectedRow,2).toString(),selectedRow,7);
						bpmTableModel.setValueAt(bpmTableModel.getValueAt(selectedRow,3).toString(),selectedRow,8);
						bpmTableModel.setValueAt(bpmTableModel.getValueAt(selectedRow,4).toString(),selectedRow,9);
						bpmTableModel.setValueAt(bpmTableModel.getValueAt(selectedRow,5).toString(),selectedRow,10);
					}
				}
			}
		});

		final TableProdder prodder = new TableProdder(bpmTableModel);
		prodder.start();

		JScrollPane bpmScrollPane = new JScrollPane(bpmTable);
		bpmScrollPane.setPreferredSize(new Dimension(850, 400));

		EdgeLayout edgeLayout = new EdgeLayout();
		setLayout(edgeLayout);
		edgeLayout.setConstraints(bpmScrollPane, 30, 30, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(bpmScrollPane);

		JPanel plotPane = new JPanel();
		plotPane.setLayout(new BoxLayout(plotPane, BoxLayout.Y_AXIS));
		plotPane.setPreferredSize(new Dimension(350, 470));

		// for linac BPMs
		if (typeInd == 0) {
			phasePlot = new FunctionGraphsJPanel();
			phasePlot.setLimitsAndTicksX(0., 50., 10.);
			phasePlot.addCurveData(phasePlotData);
			phasePlot.setName("BPM phase: ");
			phasePlot.setAxisNames("point no.", "phase");
			phasePlot.addMouseListener(new SimpleChartPopupMenu(phasePlot));
			plotPane.add(phasePlot);
			ampPlot = new FunctionGraphsJPanel();
			ampPlot.setLimitsAndTicksX(0., 50., 500.);
			ampPlot.addCurveData(ampPlotData);
			ampPlot.setName("BPM Amplitude: ");
			ampPlot.setAxisNames("point no.", "amplitude");
			ampPlot.addMouseListener(new SimpleChartPopupMenu(ampPlot));
			plotPane.add(ampPlot);

			// phasePlot.setSmartGL(true);
			ampPlot.setVerLinesButtonVisible(true);
			ampPlot.setDraggedVerLinesMotionListen(true);
			ampPlot.addVerticalLine(low, Color.green);
			ampPlot.addVerticalLine(high, Color.red);
		}
		// for ring BPMs
		else {
			xTBTPlot = new FunctionGraphsJPanel();
			xTBTPlot.setLimitsAndTicksX(0., 500., 100.);
			xTBTPlot.addCurveData(xTBTPlotData);
			xTBTPlot.setName("xTBT: ");
			xTBTPlot.setAxisNames("turn no.", "x (mm)");
			xTBTPlot.addMouseListener(new SimpleChartPopupMenu(xTBTPlot));
			plotPane.add(xTBTPlot);
			yTBTPlot = new FunctionGraphsJPanel();
			yTBTPlot.setLimitsAndTicksX(0., 500., 500.);
			yTBTPlot.addCurveData(yTBTPlotData);
			yTBTPlot.setName("yTBT: ");
			yTBTPlot.setAxisNames("turn no.", "y (mm)");
			yTBTPlot.addMouseListener(new SimpleChartPopupMenu(yTBTPlot));
			plotPane.add(yTBTPlot);

			// phasePlot.setSmartGL(true);
			xTBTPlot.setVerLinesButtonVisible(true);
			xTBTPlot.setDraggedVerLinesMotionListen(true);
			xTBTPlot.addVerticalLine(low, Color.green);
			xTBTPlot.addVerticalLine(high, Color.red);
		}
		edgeLayout.setConstraints(plotPane, 20, 900, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(plotPane);

		JPanel rangePane = new JPanel();
		rangePane.setLayout(new GridLayout(2, 2));
		rangePane.setPreferredSize(new Dimension(200, 40));
		JLabel startLabel = new JLabel("start: ");
		JLabel endLabel = new JLabel("end: ");
		startField = new DecimalField();
		endField = new DecimalField();
		rangePane.add(startLabel);
		rangePane.add(startField);
		rangePane.add(endLabel);
		rangePane.add(endField);
		edgeLayout.setConstraints(rangePane, 500, 950, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(rangePane);

		endField.setValue(high);
		startField.setValue(low);

		// for linac BPMs
		if (typeInd == 0) {
			ampPlot.addDraggedVerLinesListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					FunctionGraphsJPanel fgjp = (FunctionGraphsJPanel) evt
							.getSource();
					int lineInd = fgjp.getDraggedLineIndex();
					double pos = fgjp.getVerticalValue(lineInd);

					double temp = Double
							.parseDouble(((InputPVTableCell) bpmTableModel
									.getValueAt(selectedRow, 1)).toString());

// String s = bpmNames[selectedRow].substring(0, 1);

					int[] del = new int[5];
					// no need to calculate relative time delay, it's built
					// in the
					// BPM software
/*
 * if (s.equals("M")) for (int i = 0; i < 5; i++) del[i] = -i; else if
 * (s.equals("D")) for (int i = 0; i < 5; i++) del[i] = -i + 1; else if
 * (s.equals("C")) for (int i = 0; i < 5; i++) del[i] = -i + 2; else if
 * (s.equals("S")) for (int i = 0; i < 5; i++) del[i] = -i + 3; else for (int i =
 * 0; i < 5; i++) del[i] = -i + 4;
 */
					if (lineInd == 1) {
						high = pos;
						if (high < low)
							endField.setForeground(Color.red);
						else {
							endField.setForeground(Color.black);
							
							temp = 0.;
							double startPt = low + temp;
							// double range = high - low;

							for (int i = 0; i < bpmNames.length; i++) {
								if (bpmNames[i].substring(0, 1).equals("M"))
									bpmTableModel.setValueAt(nf.format(Math
											.round(startPt + del[0])), i, 5);
								else if (bpmNames[i].substring(0, 1)
										.equals("D"))
									bpmTableModel.setValueAt(nf.format(Math
											.round(startPt + del[1])), i, 5);
								else if (bpmNames[i].substring(0, 1)
										.equals("C"))
									bpmTableModel.setValueAt(nf.format(Math
											.round(startPt + del[2])), i, 5);
								else if (bpmNames[i].substring(0, 1)
										.equals("S"))
									bpmTableModel.setValueAt(nf.format(Math
											.round(startPt + del[3])), i, 5);
								else
									bpmTableModel.setValueAt(nf.format(Math
											.round(startPt + del[4])), i, 5);

								//bpmTableModel.setValueAt(nf.format(Math
								//		.round(range)), i, 6);
								bpmTableModel.setValueAt(nf.format(Math.round(low)), i, 7);
								bpmTableModel.setValueAt(nf.format(Math.round(high)), i, 8);								
								bpmTableModel.setValueAt(bpmTableModel
										.getValueAt(i, 4).toString(), i, 9);
								bpmTableModel.setValueAt(bpmTableModel
										.getValueAt(i, 5).toString(), i, 10);
							}
						}

						endField.setValue(Math.round(high));
						setAll.setEnabled(true);
					} else {
						low = pos;
						if (high < low)
							startField.setForeground(Color.red);
						else {
							startField.setForeground(Color.black);
							
							temp =0.;
							double startPt = low + temp;
							// double range = high - low;

							for (int i = 0; i < bpmNames.length; i++) {
								if (bpmNames[i].substring(0, 1).equals("M")) {
									bpmTableModel.setValueAt(nf.format(Math
											.round(startPt + del[0])), i, 5);
								} else if (bpmNames[i].substring(0, 1).equals(
										"D"))
									bpmTableModel.setValueAt(nf.format(Math
											.round(startPt + del[1])), i, 5);
								else if (bpmNames[i].substring(0, 1)
										.equals("C"))
									bpmTableModel.setValueAt(nf.format(Math
											.round(startPt + del[2])), i, 5);
								else if (bpmNames[i].substring(0, 1)
										.equals("S"))
									bpmTableModel.setValueAt(nf.format(Math
											.round(startPt + del[3])), i, 5);
								else
									bpmTableModel.setValueAt(nf.format(Math
											.round(startPt + del[4])), i, 5);

								//bpmTableModel.setValueAt(nf.format(Math
								//		.round(range)), i, 6);
								bpmTableModel.setValueAt(nf.format(Math.round(low)), i, 7);
								bpmTableModel.setValueAt(nf.format(Math.round(high)), i, 8);								
								bpmTableModel.setValueAt(bpmTableModel
										.getValueAt(i, 4).toString(), i, 9);
								bpmTableModel.setValueAt(bpmTableModel
										.getValueAt(i, 5).toString(), i, 10);
							}

						}

						startField.setValue(Math.round(low));
						setAll.setEnabled(true);
					}

				}
			});
			// for individual linac BPM
			findOne = new JButton("Find settings for the selected BPM");
			// disable this button
			findOne.setEnabled(false);
			findOne.setActionCommand("findOne");
			findOne.addActionListener(this);
			edgeLayout.setConstraints(findOne, 0, 380, 100, 0,
					EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
			add(findOne);

			setOne = new JButton("Set the selected rows/cols.");
			setOne.setActionCommand("setOne");
			setOne.addActionListener(this);
			edgeLayout.setConstraints(setOne, 0, 380, 70, 0, EdgeLayout.BOTTOM,
					EdgeLayout.NO_GROWTH);
			add(setOne);

			// for all linac BPMs
			findAll = new JButton("Find all");
			// disable this button
			findAll.setEnabled(false);
			findAll.setActionCommand("findAll");
			findAll.addActionListener(this);
			edgeLayout.setConstraints(findAll, 0, 380, 40, 0,
					EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
			add(findAll);
			// findAll.setEnabled(false);

			prepAll1 = new JButton("Prepare for all");
			prepAll1.setActionCommand("prepAll1");
			prepAll1.addActionListener(this);
			edgeLayout.setConstraints(prepAll1, 0, 680, 100, 0,
					EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
			add(prepAll1);

			setAll = new JButton("Set all");
			setAll.setActionCommand("setAll");
			setAll.addActionListener(this);
			edgeLayout.setConstraints(setAll, 0, 680, 40, 0, EdgeLayout.BOTTOM,
					EdgeLayout.NO_GROWTH);
			add(setAll);
			setAll.setEnabled(false);
		}
		// for ring BPMs
		else {
			// do nothing, because no need to calculate trigger delay here
			
			xTBTPlot.addDraggedVerLinesListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					FunctionGraphsJPanel fgjp = (FunctionGraphsJPanel) evt
							.getSource();
					int lineInd = fgjp.getDraggedLineIndex();
					double pos = fgjp.getVerticalValue(lineInd);

//					double temp = Double
//							.parseDouble(((InputPVTableCell) bpmTableModel
//									.getValueAt(selectedRow, 2)).toString());
					
					// no need for calculating relative trigger delay
//					DecimalFormat df = new DecimalFormat("0.###E0");
					if (lineInd == 1) {
						high = pos;
/*						if (high < low)
							endField.setForeground(Color.red);
						else {
							endField.setForeground(Color.black);
							bpmTableModel.setValueAt(df.format((low + temp)*1.e-9),
									selectedRow, 2);
						}
*/
						endField.setValue(high);
					} else {
						low = pos;
/*						if (high < low)
							startField.setForeground(Color.red);
						else {
							startField.setForeground(Color.black);
							bpmTableModel.setValueAt(df.format((low + temp)*1.e-9),
									selectedRow, 2);
						}
*/
						startField.setValue(low);

					}

				}
			});
			
			// for individual ring BPM
			set1RBPM = new JButton("Set the selected rows/cols.");
			set1RBPM.setActionCommand("set1RBPM");
			set1RBPM.addActionListener(this);
			edgeLayout.setConstraints(set1RBPM, 0, 380, 100, 0,
					EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
			add(set1RBPM);

			// for all ring BPMs
			findAllRTiming = new JButton("Set all BPM timing");
			findAllRTiming.setActionCommand("findAllRTiming");
			findAllRTiming.addActionListener(this);
			edgeLayout.setConstraints(findAllRTiming, 0, 380, 70, 0,
					EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
			add(findAllRTiming);

			prepAll2 = new JButton("Prepare for all");
			prepAll2.setActionCommand("prepAll2");
			prepAll2.addActionListener(this);
			edgeLayout.setConstraints(prepAll2, 0, 680, 100, 0,
					EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
			add(prepAll2);

			setAllRBPMs = new JButton("Set all BPM stages");
			setAllRBPMs.setActionCommand("setAllRBPMs");
			setAllRBPMs.addActionListener(this);
			edgeLayout.setConstraints(setAllRBPMs, 0, 680, 70, 0,
					EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
			add(setAllRBPMs);

			refresh = new JButton("Refresh Table");
			refresh.setActionCommand("refresh");
			refresh.addActionListener(this);
			edgeLayout.setConstraints(refresh, 0, 380, 10, 0,
					EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
			add(refresh);

		}
		
		JLabel beamWidthLbl = new JLabel("Beam gate width (turns): ");
		edgeLayout.setConstraints(beamWidthLbl, 0, 50, 100, 0,
				EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
		add(beamWidthLbl);

		beamWidthFld = new DecimalField(beamWidth, 6);
		edgeLayout.setConstraints(beamWidthFld, 0, 220, 95, 0,
				EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
		add(beamWidthFld);
		ChannelFactory cf = ChannelFactory.defaultFactory();
		Channel beamGW = cf.getChannel("ICS_Tim:Gate_BeamRef:GateWidth");
		try {
			beamWidthMonitor = beamGW.addMonitorValTime(
					new IEventSinkValTime() {
						public void eventValue(ChannelTimeRecord newRecord,
								Channel chan) {
							beamWidth = newRecord.doubleValue();
							beamWidthFld.setValue(beamWidth);
						}
					}, Monitor.VALUE);

		} catch (ConnectionException e) {
			System.out.println("Cannot connect to " + beamGW.getId());
		} catch (MonitorException e) {
			System.out.println("Cannot monitor " + beamGW.getId());
		}

		// select for setting column/row
// String[] modes = {"select rows", "select columns"};
		String[] modes = {"select rows"};
		setMode = new JComboBox<String>(modes);
		
		edgeLayout.setConstraints(setMode, 0, 50, 50, 0,
				EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
		add(setMode);
		setMode.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (((String) (((JComboBox) evt.getSource()).getSelectedItem()))
						.equals("select columns")) {
					bpmTable.setColumnSelectionAllowed(true);
					bpmTable.setRowSelectionAllowed(false);
				} else if (((String) (((JComboBox) evt.getSource())
						.getSelectedItem())).equals("select rows")) {
					bpmTable.setColumnSelectionAllowed(false);
					bpmTable.setRowSelectionAllowed(true);					
				}
			}
		});
		
	}

	protected void connectAll() {
		MakeConnections mc = new MakeConnections(this, typeInd);
		Thread thread = new Thread(mc);
		thread.start();
	}

	/** get the list of table cells monitoring the prescibed channel */
	private Vector<InputPVTableCell> getChannelVec(Channel p_chan) {
		if (!monitorQueues.containsKey(p_chan.channelName()))
			monitorQueues.put(p_chan.channelName(),
					new Vector<InputPVTableCell>());

		return monitorQueues.get(p_chan.channelName());
	}

	/** ConnectionListener interface */
	public void connectionMade(Channel aChannel) {
		connectMons(aChannel);
	}

	/** ConnectionListener interface */
	public void connectionDropped(Channel aChannel) {
	}

	/** internal method to connect the monitors */
	private void connectMons(Channel p_chan) {
		Vector<InputPVTableCell> chanVec;

		try {
			chanVec = getChannelVec(p_chan);
			for (int i = 0; i < chanVec.size(); i++) {
				mons.add(p_chan.addMonitorValue(chanVec
						.elementAt(i), Monitor.VALUE));
			}
			chanVec.removeAllElements();

		} catch (ConnectionException e) {
			System.out.println("Connection Exception");
		} catch (MonitorException e) {
			System.out.println("Monitor Exception");
		}
	}

	private void updatePlot(String name) {
		stopMonitors();
		startMonitors(name);
	}

	private void startMonitors(String name) {
		ChannelFactory caF = ChannelFactory.defaultFactory();

		// for linac BPM waveforms
		if (typeInd == 0) {
			Channel bpmPhaseWF = caF.getChannel(name + ":beamPA");
			Channel bpmAmpWF = caF.getChannel(name + ":beamIA");
			try {
				double[] tmpArry = bpmPhaseWF.getArrDbl();
				x1 = new double[tmpArry.length];

			} catch (GetException ge) {
				// do nothing, use the default array size for waveforms
			} catch (ConnectionException ce) {
				// do nothing, use the default array size for waveforms
			}

			for (int i = 0; i < x1.length; i++) {
				x1[i] = i;
			}

			try {
				phaseArryMonitor = bpmPhaseWF.addMonitorValTime(
						new IEventSinkValTime() {
							public void eventValue(ChannelTimeRecord newRecord,
									Channel chan) {
								yPhase = newRecord.doubleArray();
								phasePlotData.setPoints(x1, yPhase);
								phasePlot.refreshGraphJPanel();
							}
						}, Monitor.VALUE);

			} catch (ConnectionException e) {
				System.out.println("Cannot connect to " + bpmPhaseWF.getId());
			} catch (MonitorException e) {
				System.out.println("Cannot monitor " + bpmPhaseWF.getId());
			}

			try {
				ampArryMonitor = bpmAmpWF.addMonitorValTime(
						new IEventSinkValTime() {
							public void eventValue(ChannelTimeRecord newRecord,
									Channel chan) {
								yAmp = newRecord.doubleArray();
								ampPlotData.setPoints(x1, yAmp);
								ampPlot.refreshGraphJPanel();
							}
						}, Monitor.VALUE);

			} catch (ConnectionException e) {
				System.out.println("Cannot connect to " + bpmAmpWF.getId());
			} catch (MonitorException e) {
				System.out.println("Cannot monitor " + bpmAmpWF.getId());
			}
		}
		// for ring BPM waveforms
		else {
			Channel bpmXTBTWF = caF.getChannel(name + ":xTBT");
			Channel bpmYTBTWF = caF.getChannel(name + ":yTBT");
			try {
				double[] tmpArry = bpmXTBTWF.getArrDbl();
				x2 = new double[tmpArry.length];

			} catch (GetException ge) {
				// do nothing, use the default array size for waveforms
			} catch (ConnectionException ce) {
				// do nothing, use the default array size for waveforms
			}

			for (int i = 0; i < x2.length; i++) {
				x2[i] = i;
			}

			try {
				xTBTArryMonitor = bpmXTBTWF.addMonitorValTime(
						new IEventSinkValTime() {
							public void eventValue(ChannelTimeRecord newRecord,
									Channel chan) {
								yXTBT = newRecord.doubleArray();
								xTBTPlotData.setPoints(x2, yXTBT);
								xTBTPlot.refreshGraphJPanel();
							}
						}, Monitor.VALUE);

			} catch (ConnectionException e) {
				System.out.println("Cannot connect to " + bpmXTBTWF.getId());
			} catch (MonitorException e) {
				System.out.println("Cannot monitor " + bpmXTBTWF.getId());
			}

			try {
				yTBTArryMonitor = bpmYTBTWF.addMonitorValTime(
						new IEventSinkValTime() {
							public void eventValue(ChannelTimeRecord newRecord,
									Channel chan) {
								yYTBT = newRecord.doubleArray();
								yTBTPlotData.setPoints(x2, yYTBT);
								yTBTPlot.refreshGraphJPanel();
							}
						}, Monitor.VALUE);

			} catch (ConnectionException e) {
				System.out.println("Cannot connect to " + bpmYTBTWF.getId());
			} catch (MonitorException e) {
				System.out.println("Cannot monitor " + bpmYTBTWF.getId());
			}
		}

	}

	private void stopMonitors() {
		if (phaseArryMonitor != null) {
			phaseArryMonitor.clear();
			phaseArryMonitor = null;
		}

		if (ampArryMonitor != null) {
			ampArryMonitor.clear();
			ampArryMonitor = null;
		}

		if (xTBTArryMonitor != null) {
			xTBTArryMonitor.clear();
			xTBTArryMonitor = null;
		}

		if (yTBTArryMonitor != null) {
			yTBTArryMonitor.clear();
			yTBTArryMonitor = null;
		}

	}

	public void actionPerformed(ActionEvent e) {
		// for linac BPMs
		if (e.getActionCommand().equals("findOne")) {
			// during calculation, "set" button should be disabled.
			setOne.setEnabled(false);
			System.out.println("selected BPM = " + bpmNames[selectedRow]);
			calcT = new CalcLinacTiming(selectedRow, bpmNames[selectedRow],
					this);
			// calcT.setBPM(selectedRow);
			Thread thread = new Thread(calcT);
			thread.start();
		} else if (e.getActionCommand().equals("setOne")) {
			int[] indices;
			// check row/col selection mode
			// for row selection
			if (setMode.getSelectedIndex() == 0) {
				indices = bpmTable.getSelectedRows();
				CalcLinacTiming[] calcTs = new CalcLinacTiming[indices.length];
				for (int i=0; i<indices.length; i++) {
					calcTs[i] = new CalcLinacTiming(indices[i], bpmNames[indices[i]], this);
					calcTs[i].setTimings();
				}
			} 
			// for column selection
			else {
				indices = bpmTable.getSelectedColumns();
// calcT.setTimings();
			}
			System.out.println(indices.length + " selected");

		} else if (e.getActionCommand().equals("findAll")) {
			calcAllT = new CalcLinacTiming[bpmNames.length];
			for (int i = 0; i < bpmNames.length; i++) {
				calcAllT[i] = new CalcLinacTiming(i, bpmNames[i], this);
				// calcAllT[i].setBPM(i);
				Thread thread = new Thread(calcAllT[i]);
				thread.start();
			}
			setAll.setEnabled(true);
		} else if (e.getActionCommand().equals("setAll")) {
			dtcr.setForeground(Color.black);
			// needs to change to "set PVs according to the table
			for (int i = 0; i < bpmNames.length; i++) {
				calcAllT[i].setTimings();
			}
		} else if (e.getActionCommand().equals("prepAll1")) {
			calcAllT = new CalcLinacTiming[bpmNames.length];
			for (int i = 0; i < bpmNames.length; i++) {
				calcAllT[i] = new CalcLinacTiming(i, bpmNames[i], this);
			}

			dtcr.setForeground(Color.red);
			for (int i = 6; i < bpmTableModel.getColumnCount(); i++)
				bpmTable.getColumnModel().getColumn(i).setCellRenderer(dtcr);

// String s = bpmNames[selectedRow].substring(0, 1);

			int[] del = new int[5];
/*
 * if (s.equals("M")) for (int i = 0; i < 5; i++) del[i] = -i; else if
 * (s.equals("D")) for (int i = 0; i < 5; i++) del[i] = -i + 1; else if
 * (s.equals("C")) for (int i = 0; i < 5; i++) del[i] = -i + 2; else if
 * (s.equals("S")) for (int i = 0; i < 5; i++) del[i] = -i + 3; else for (int i =
 * 0; i < 5; i++) del[i] = -i + 4;
 */
			for (int i = 0; i < bpmNames.length; i++) {
				try {
					double startPt = nf.parse(
							(String) bpmTableModel.getValueAt(selectedRow, 6))
							.intValue();
					// System.out.println("startPt = " + startPt);
					if (!((InputPVTableCell) (bpmTableModel.getValueAt(i, 1)))
							.toString().equals("null")) {
						if (bpmNames[i].substring(0, 1).equals("M"))
							bpmTableModel.setValueAt(nf.format(Math
									.round(startPt + del[0])), i, 6);
						else if (bpmNames[i].substring(0, 1).equals("D"))
							bpmTableModel.setValueAt(nf.format(Math
									.round(startPt + del[1])), i, 6);
						else if (bpmNames[i].substring(0, 1).equals("C"))
							bpmTableModel.setValueAt(nf.format(Math
									.round(startPt + del[2])), i, 6);
						else if (bpmNames[i].substring(0, 1).equals("S"))
							bpmTableModel.setValueAt(nf.format(Math
									.round(startPt + del[3])), i, 6);
						else
							bpmTableModel.setValueAt(nf.format(Math
									.round(startPt + del[4])), i, 6);
					} else {
						bpmTableModel.setValueAt("null", i, 6);
					}
				} catch (ParseException pe) {
					System.out.println(pe);
				}
			}

			for (int i = 0; i < bpmNames.length; i++) {
				for (int j = 7; j < bpmTableModel.getColumnCount(); j++) {
					try {
						if (!((InputPVTableCell) (bpmTableModel.getValueAt(i,
								j - 5))).toString().equals("null")) {
							bpmTableModel.setValueAt(bpmTableModel.getValueAt(
									selectedRow, j).toString(), i, j);
						} else {
							bpmTableModel.setValueAt("null", i, j);
						}
					} catch (NullPointerException ne) {
						// need to do something...
					} finally {
						// need to do something, i.e. fill with 0?
					}
				}
			}
			setAll.setEnabled(true);
		}
		// for ring BPMs
		else if (e.getActionCommand().equals("set1RBPM")) {
			dtcr.setForeground(Color.black);
			// set PVs
			int[] indices;
			// for row selection
			if (setMode.getSelectedIndex() == 0) {
				indices = bpmTable.getSelectedRows();
				Thread[] threads = new Thread[indices.length];
				for (int i=0; i<indices.length; i++) {
					try {
						// set trigger delay
						// bpmDelays[selectedRow].putVal(nf.parse(
						// ((InputPVTableCell)
						// bpmTableModel.getValueAt(selectedRow, 1))
						// .toString()).doubleValue());
						
						// set trigger event
						String evtString = ((ComboBoxPVCell) bpmTableModel.getValueAt(
								indices[i], 3)).toString();
						int evtId = Integer.parseInt(trigMap.get(evtString));
						// System.out.println("trigger event ID = " + evtId);
						trigEvtChs[indices[i]].putVal(evtId);
						
						// RingBPM rBPM = (RingBPM) theNodes.get(indices[i]);
						
						ConnectAndSetPVs caspv = new ConnectAndSetPVs(this,
								indices[i], 1);
						threads[i] = new Thread(caspv);
						threads[i].start();
						
					} catch (ConnectionException ce) {
						System.out.println(ce);
					} catch (PutException pe) {
						System.out.println(pe);
						// } catch (ParseException pe) {
						// System.out.println(pe);
					}
				}
				
				ArrayList<Integer> tmpList = new ArrayList<Integer>();
				for (int i=0; i<bpmNames.length; i++) {
					tmpList.add(new Integer(i));
				}
				
				for (int i=indices.length; i>0; i--) {
					tmpList.remove(indices[i-1]);
				}
				
				
				for (int i = 0; i < tmpList.size(); i++) {
					for (int j = 1; j < bpmTableModel.getColumnCount(); j++) {
						try { ((InputPVTableCell) bpmTableModel.getValueAt(tmpList.get(i).intValue(), j))
									.setUpdating(true);
						} catch (NullPointerException ne) { 
									// need to do something...
									
						} finally { // need to do something
						}	
					}
				}
			} 
			// for column selection
			else {
				indices = bpmTable.getSelectedColumns();
			}
			
		} else if (e.getActionCommand().equals("prepAll2")) {
			dtcr.setForeground(Color.red);
			for (int i = 2; i < bpmTableModel.getColumnCount(); i++)
				bpmTable.getColumnModel().getColumn(i).setCellRenderer(dtcr);

			for (int i = 0; i < bpmNames.length; i++) {
				if (i != selectedRow) {
					for (int j = 2; j < bpmTableModel.getColumnCount(); j++) {
						try {
							((InputPVTableCell) bpmTableModel.getValueAt(i, j))
									.setUpdating(false);
							bpmTableModel.setValueAt(bpmTableModel.getValueAt(
									selectedRow, j).toString(), i, j);
						} catch (NullPointerException ne) {
							// need to do something...
						} finally {
							// need to do something, i.e. fill with 0?
						}
					}
				}
			}
		} else if (e.getActionCommand().equals("setAllRBPMs")) {
			dtcr.setForeground(Color.black);
			// set table cell display color back to black
			for (int i = 2; i < bpmTableModel.getColumnCount(); i++) {
				bpmTable.getColumnModel().getColumn(i).setCellRenderer(dtcr);
			}

			for (int i = 0; i < bpmNames.length; i++) {
				// set PVs
				try {
					// set trigger delays
					// bpmDelays[i].putVal(nf.parse(
					// ((InputPVTableCell) bpmTableModel.getValueAt(i, 1))
					// .toString()).doubleValue());
					// set trigger event
					String evtString = ((ComboBoxPVCell) bpmTableModel
							.getValueAt(i, 3)).toString();
					int evtId = Integer.parseInt(trigMap.get(evtString));
					trigEvtChs[i].putVal(evtId);
					// set stage parameters
					RingBPM rBPM = (RingBPM) theNodes.get(i);

					ConnectAndSetPVs caspv = new ConnectAndSetPVs(this, i, 1);
					Thread thread = new Thread(caspv);
					thread.start();

					/*
					 * rBPM.setStage1Len(nf.parse( ((InputPVTableCell)
					 * bpmTableModel.getValueAt(i, 3))
					 * .toString()).longValue()); rBPM.setStage1Gain(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 4))
					 * .toString()).longValue()); rBPM.setStage1Method(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 5))
					 * .toString()).intValue()); rBPM.setStage2Len(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 6))
					 * .toString()).longValue()); rBPM.setStage2Gain(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 7))
					 * .toString()).longValue()); rBPM.setStage2Method(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 8))
					 * .toString()).intValue()); rBPM.setStage3Len(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 9))
					 * .toString()).longValue()); rBPM.setStage3Gain(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 10))
					 * .toString()).longValue()); rBPM.setStage3Method(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 11))
					 * .toString()).intValue()); rBPM.setStage4Len(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 12))
					 * .toString()).longValue()); rBPM.setStage4Gain(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 13))
					 * .toString()).longValue()); rBPM.setStage4Method(nf.parse(
					 * ((InputPVTableCell) bpmTableModel.getValueAt(i, 14))
					 * .toString()).intValue());
					 */
				} catch (ConnectionException ce) {
					System.out.println(ce);
				} catch (PutException pe) {
					System.out.println(pe);
					// } catch (ParseException pe) {
					// System.out.println(pe);
				}

				// enable table cell update (from CA monitor)
				/*
				 * for (int j = 1; j < bpmTableModel.getColumnCount(); j++) {
				 * try { ((InputPVTableCell) bpmTableModel.getValueAt(i, j))
				 * .setUpdating(true); } catch (NullPointerException ne) { //
				 * need to do something... } finally { // need to do something,
				 * i.e. fill with 0? } }
				 */
			}

		} else if (e.getActionCommand().equals("refresh")) {
			// for linac
			if (typeInd == 0) {
				// do nothing, for future use
			} 
			// for ring, etc.
			else {
				for (int i=0; i<bpmNames.length; i++) {
					for (int j=1; j<bpmTableModel.getColumnCount(); j++)
					((InputPVTableCell) (bpmTableModel.getValueAt(i, j))).setUpdating(true);
				}				
			}
			bpmTableModel.fireTableRowsUpdated(1, bpmNames.length);
		}

	}

	public void setUpTrigEvtColumn(TableColumn trigEvtColumn) {
		// Set up the editor for the sport cells.
		String[] trigEvtIds = new String[49];
		String[] trigEvtNames = new String[49];

		trigEvtIds[0] = "001";
		trigEvtNames[0] = "CYCLE_START_EVENT";
		trigEvtIds[1] = "003";
		trigEvtNames[1] = "MPS_RESET_EVENT";
		trigEvtIds[2] = "004";
		trigEvtNames[2] = "MPS_LATCH_EVENT";
		trigEvtIds[3] = "010";
		trigEvtNames[3] = "SPARE0_EVENT";
		trigEvtIds[4] = "011";
		trigEvtNames[4] = "SPARE1_EVENT";
		trigEvtIds[5] = "012";
		trigEvtNames[5] = "SPARE2_EVENT";
		trigEvtIds[6] = "027";
		trigEvtNames[6] = "SOURCE_ON_EVENT";
		trigEvtIds[7] = "028";
		trigEvtNames[7] = "WARM_LINAC_HPRF_EVENT";
		trigEvtIds[8] = "029";
		trigEvtNames[8] = "WARM_LINAC_LLRF_EVENT";
		trigEvtIds[9] = "030";
		trigEvtNames[9] = "COLD_LINAC_HPRF_EVENT";
		trigEvtIds[10] = "031";
		trigEvtNames[10] = "COLD_LINAC_LLRF_EVENT";
		trigEvtIds[11] = "036";
		trigEvtNames[11] = "BEAM_ON_EVENT";
		trigEvtIds[12] = "037";
		trigEvtNames[12] = "BEAM_REFERENCE_EVENT";
		trigEvtIds[13] = "038";
		trigEvtNames[13] = "END_INJECT_EVENT";
		trigEvtIds[14] = "039";
		trigEvtNames[14] = "EXTRACT_EVENT";
		trigEvtIds[15] = "040";
		trigEvtNames[15] = "KICKER_CHARGE_EVENT";
		trigEvtIds[16] = "041";
		trigEvtNames[16] = "DIAG_LASER_TRIG";
		trigEvtIds[17] = "043";
		trigEvtNames[17] = "RTDL_XMIT_EVENT";
		trigEvtIds[18] = "044";
		trigEvtNames[18] = "RTDL_VALID_EVNET";
		trigEvtIds[19] = "045";
		trigEvtNames[19] = "DIAG_DEMAND_EVENT";
		trigEvtIds[20] = "046";
		trigEvtNames[20] = "DIAG_SLOW_EVENT";
		trigEvtIds[21] = "047";
		trigEvtNames[21] = "DIAG_FAST_EVENT";
		trigEvtIds[22] = "048";
		trigEvtNames[22] = "DIAG_NO_BEAM_EVENT";
		trigEvtIds[23] = "049";
		trigEvtNames[23] = "DIAG_LASER";
		trigEvtIds[24] = "050";
		trigEvtNames[24] = "RF_SAMPLE_EVENT";
		trigEvtIds[25] = "052";
		trigEvtNames[25] = "60 HZ";
		trigEvtIds[26] = "053";
		trigEvtNames[26] = "30 Hz";
		trigEvtIds[27] = "054";
		trigEvtNames[27] = "20 Hz";
		trigEvtIds[28] = "055";
		trigEvtNames[28] = "10 Hz";
		trigEvtIds[29] = "056";
		trigEvtNames[29] = "05 Hz";
		trigEvtIds[30] = "057";
		trigEvtNames[30] = "02 Hz";
		trigEvtIds[31] = "058";
		trigEvtNames[31] = "01 Hz";
		trigEvtIds[32] = "059";
		trigEvtNames[32] = "RTBT-Slow";
		
		trigEvtIds[33] = "063";
		trigEvtNames[33] = "PRE_PULSE_EVENT";
		trigEvtIds[34] = "240";
		trigEvtNames[34] = "FLA 0";
		trigEvtIds[35] = "241";
		trigEvtNames[35] = "FLA 1";
		trigEvtIds[36] = "242";
		trigEvtNames[36] = "FLA 2";
		trigEvtIds[37] = "243";
		trigEvtNames[37] = "FLA 3";
		trigEvtIds[38] = "244";
		trigEvtNames[38] = "FLA 4";
		trigEvtIds[39] = "245";
		trigEvtNames[39] = "FLA 5";
		trigEvtIds[40] = "246";
		trigEvtNames[40] = "FLA 6";
		trigEvtIds[41] = "247";
		trigEvtNames[41] = "FLA 7";
		trigEvtIds[42] = "249";
		trigEvtNames[42] = "TEST_NETWORK_MARKER";
		trigEvtIds[43] = "250";
		trigEvtNames[43] = "DIAG_MPS_SNAPSHOT";
		trigEvtIds[44] = "251";
		trigEvtNames[44] = "COMPUTE_REP_RATE_EVENT";
		trigEvtIds[45] = "252";
		trigEvtNames[45] = "NEW_REP_RATE_EVENT";
		trigEvtIds[46] = "253";
		trigEvtNames[46] = "MPS_ERROR_RESET_EVENT";
		trigEvtIds[47] = "254";
		trigEvtNames[47] = "UTIL_ERROR_RESET_EVENT";
		trigEvtIds[48] = "255";
		trigEvtNames[48] = "SUPERCYCLE_START";

		JComboBox<String> comboBox = new JComboBox<String>();

		for (int i = 0; i < trigEvtIds.length; i++) {
			comboBox.addItem(trigEvtIds[i] + " | " + trigEvtNames[i]);
			// comboBox.addItem(trigEvtIds[i]);
			trigMap.put(trigEvtIds[i] + " | " + trigEvtNames[i], trigEvtIds[i]);
			trigMap1.put(new Integer(Integer.parseInt(trigEvtIds[i])),
					trigEvtIds[i] + " | " + trigEvtNames[i]);
		}

		// comboBox.addActionListener(new java.awt.event.ActionListener() {
		// public void actionPerformed(java.awt.event.ActionEvent evt) {
		// System.out.println(((String) (((JComboBox)
		// evt.getSource()).getSelectedItem())));
		// Fill in the task for this PV value
		// }
		// });

		trigEvtColumn.setCellEditor(new DefaultCellEditor(comboBox));

		// Set up tool tips for the trigger event cells.
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click to select trigger event");
		trigEvtColumn.setCellRenderer(renderer);

		// Set up tool tip for the trigger event column header.
		TableCellRenderer headerRenderer = trigEvtColumn.getHeaderRenderer();
		if (headerRenderer instanceof DefaultTableCellRenderer) {
			((DefaultTableCellRenderer) headerRenderer)
					.setToolTipText("Click the column header to see a list of choices");
			trigEvtColumn.setHeaderRenderer(headerRenderer);
		}
	}

	public void setUpGainColumn(TableColumn gainColumn) {
		// Set up the editor for the sport cells.
		String[] gainIds = new String[12];
		String[] gainNames = new String[12];

		gainIds[0] = "0";
		gainNames[0] = "Base band -10 db";
		gainIds[1] = "1";
		gainNames[1] = "Base band 0 db";
		gainIds[2] = "2";
		gainNames[2] = "Base band 10 db";
		gainIds[3] = "3";
		gainNames[3] = "Base band 20 db";
		gainIds[4] = "4";
		gainNames[4] = "Base band 30 db";
		gainIds[5] = "5";
		gainNames[5] = "Base band 40 db";
		gainIds[6] = "6";
		gainNames[6] = "Base band 50 db";
		gainIds[7] = "7";
		gainNames[7] = "Base band 60 db";
		gainIds[8] = "8";
		gainNames[8] = "RF 0 db";
		gainIds[9] = "9";
		gainNames[9] = "RF 10 db";
		gainIds[10] = "10";
		gainNames[10] = "RF 20 db";
		gainIds[11] = "11";
		gainNames[11] = "RF 30 db";

		JComboBox<String> comboBox = new JComboBox<String>();

		for (int i = 0; i < gainIds.length; i++) {
			comboBox.addItem(gainIds[i] + " | " + gainNames[i]);
			// comboBox.addItem(gainIds[i]);
			gainMap.put(gainIds[i] + " | " + gainNames[i], gainIds[i]);
			gainMap1.put(new Integer(Integer.parseInt(gainIds[i])),
					gainIds[i] + " | " + gainNames[i]);
		}

		gainColumn.setCellEditor(new DefaultCellEditor(comboBox));

		// Set up tool tips for the trigger event cells.
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click to select gain");
		gainColumn.setCellRenderer(renderer);

		// Set up tool tip for the trigger event column header.
		TableCellRenderer headerRenderer = gainColumn.getHeaderRenderer();
		if (headerRenderer instanceof DefaultTableCellRenderer) {
			((DefaultTableCellRenderer) headerRenderer)
					.setToolTipText("Click the column header to see a list of choices");
			gainColumn.setHeaderRenderer(headerRenderer);
		}
	}

	public void setUpAnalysisColumn(TableColumn analysisColumn) {
		// Set up the editor for the sport cells.
		String[] analysisIds = new String[3];
		String[] analysisNames = new String[3];

		analysisIds[0] = "0";
		analysisNames[0] = "RMS";
		analysisIds[1] = "1";
		analysisNames[1] = "Integration";
		analysisIds[2] = "2";
		analysisNames[2] = "Peak";
		
		JComboBox<String> comboBox = new JComboBox<String>();

		for (int i = 0; i < analysisIds.length; i++) {
			comboBox.addItem(analysisIds[i] + " | " + analysisNames[i]);
			// comboBox.addItem(analysisIds[i]);
			analysisMap.put(analysisIds[i] + " | " + analysisNames[i], analysisIds[i]);
			analysisMap1.put(new Integer(Integer.parseInt(analysisIds[i])),
					analysisIds[i] + " | " + analysisNames[i]);
		}

		analysisColumn.setCellEditor(new DefaultCellEditor(comboBox));

		// Set up tool tips for the trigger event cells.
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click to select analysis method");
		analysisColumn.setCellRenderer(renderer);

		// Set up tool tip for the trigger event column header.
		TableCellRenderer headerRenderer = analysisColumn.getHeaderRenderer();
		if (headerRenderer instanceof DefaultTableCellRenderer) {
			((DefaultTableCellRenderer) headerRenderer)
					.setToolTipText("Click the sport to see a list of choices");
		}
		
	}
	
	public void setUpOperModeColumn(TableColumn operModeColumn) {
		// Set up the editor for the sport cells.
		String[] operModeIds = new String[2];
		String[] operModeNames = new String[2];

		operModeIds[0] = "0";
		operModeNames[0] = "Global";
		operModeIds[1] = "1";
		operModeNames[1] = "Direct";
		
		JComboBox<String> comboBox = new JComboBox<String>();

		for (int i = 0; i < operModeIds.length; i++) {
			comboBox.addItem(operModeIds[i] + " | " + operModeNames[i]);
			// comboBox.addItem(operModeIds[i]);
			operModeMap.put(operModeIds[i] + " | " + operModeNames[i], operModeIds[i]);
			operModeMap1.put(new Integer(Integer.parseInt(operModeIds[i])),
					operModeIds[i] + " | " + operModeNames[i]);
		}

		operModeColumn.setCellEditor(new DefaultCellEditor(comboBox));

		// Set up tool tips for the trigger event cells.
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click to select operMode method");
		operModeColumn.setCellRenderer(renderer);

		// Set up tool tip for the trigger event column header.
		TableCellRenderer headerRenderer = operModeColumn.getHeaderRenderer();
		if (headerRenderer instanceof DefaultTableCellRenderer) {
			((DefaultTableCellRenderer) headerRenderer)
					.setToolTipText("Click the sport to see a list of choices");
		}		
	}
	
	public void setUpGainStatColumn(TableColumn gainStatColumn) {
		
		// Set up tool tips for the trigger event cells.
//		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
//		renderer.setOpaque(true);
		
		ColorRenderer renderer = new ColorRenderer();
		gainStatColumn.setCellRenderer(renderer);
		
	}
	
	class ColorRenderer extends JLabel implements TableCellRenderer {
		static final long serialVersionUID = 1000;
		
		public ColorRenderer() {
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(
				JTable table, Object val,
				boolean isSelected, boolean hasFocus,
				int row, int column) {
//			Color newColor = (Color)color;
//			setBackground(newColor);
		
			InputPVTableCell stat = (InputPVTableCell)val;
			if (stat.toString() == "OK") 
				setBackground(Color.green);
			else 
				setBackground(Color.red);
			
			return this;
		}
	}

}
