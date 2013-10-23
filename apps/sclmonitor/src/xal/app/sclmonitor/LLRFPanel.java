package xal.app.sclmonitor;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.ConnectionListener;
import xal.ca.GetException;
import xal.ca.IEventSinkValTime;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.ca.PutListener;
import xal.tools.apputils.EdgeLayout;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.CurveData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.smf.impl.SCLCavity;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

public class LLRFPanel extends JPanel implements ConnectionListener {

	static final long serialVersionUID = 1001;

	private JScrollPane jScrollPane = null;

	private JTable cavTable = null;

	private List<SCLCavity> cavs;

	private String[] homNames;

	EdgeLayout layout = new EdgeLayout();

	FunctionGraphsJPanel hom0Plot = new FunctionGraphsJPanel();

	FunctionGraphsJPanel hom1Plot = new FunctionGraphsJPanel();

	/** for HOM HB0 live data */
	CurveData hom0PlotData = new CurveData();

	/** for HOM HB1 live data */
	CurveData hom1PlotData = new CurveData();

	/** for HOM HB0 logged data */
	CurveData hom0aPlotData = new CurveData();

	/** for HOM HB1 logged data */
	CurveData hom1aPlotData = new CurveData();

	Monitor hb0Monitor = null;

	Monitor hb1Monitor = null;

	double[] x1 = new double[1024];

	double[] hom0 = new double[1024];

	double[] hom1 = new double[1024];

	String[][] cellData;

	double[][] hom0a;

	double[][] hom1a;

	private int theHOM = -1;

	private final HashMap<String, Vector<InputPVTableCell>> monitorQueues = new HashMap<String, Vector<InputPVTableCell>>();

	/** List of the monitors */
	final Vector<Monitor> mons = new Vector<Monitor>();

	Channel[] repChs, cavVChs;

	RFTableModel tableModel;
	
	Channel hom0StateCh;
	Channel hom1StateCh;
	
	int hom0State = -1;
	int hom1State = -1;
	
	public void connectionMade(Channel channel) {
		connectMons(channel);
	}

	public void connectionDropped(Channel channel) {

	}

	/**
	 * This is the default constructor
	 */
	public LLRFPanel() {
		super();
		this.setLayout(layout);
	}

	public void setCavs(List<SCLCavity> rfCavs) {
		cavs = rfCavs;

		hom0a = new double[cavs.size()][1024];
		hom1a = new double[cavs.size()][1024];
	}

	/**
	 * This method initializes this
	 * 
	 */
	protected void initialize() {
		// this.setSize(688, 450);
		layout.setConstraints(getJScrollPane(), 20, 50, 0, 0,
				EdgeLayout.TOP_LEFT);
		this.add(getJScrollPane());

		JPanel plotPane = new JPanel();
		plotPane.setLayout(new BoxLayout(plotPane, BoxLayout.Y_AXIS));
		plotPane.setPreferredSize(new Dimension(350, 470));

		plotPane.add(getHOM0Plot());
		plotPane.add(getHOM1Plot());

		layout.setConstraints(plotPane, 20, 0, 0, 50, EdgeLayout.TOP_RIGHT);

		this.add(plotPane);
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());

			jScrollPane.setPreferredSize(new Dimension(370, 450));
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTable() {
		if (cavTable == null) {
			int rfCavSize = cavs.size();

			String[] columnNames = { "Cavity", "Rep Rate", "Rep Rate (logged)",
					"Field", "Field (logged)" };

			cellData = new String[rfCavSize][1];

			homNames = new String[rfCavSize];

			// rep rate PV names
			String repPV;
			// field PV names
			String cavVPV;

			tableModel = new RFTableModel(columnNames, rfCavSize);

			repChs = new Channel[cavs.size()];
			cavVChs = new Channel[cavs.size()];

			Iterator<SCLCavity> it = cavs.iterator();
			int i = 0;
			while (it.hasNext()) {
				cellData[i][0] = (it.next()).getId();
				tableModel.addRowName(cellData[i][0], i);
				homNames[i] = cellData[i][0].replace("RF:Cav", "LLRF:HPM");
				repPV = (cellData[i][0].replace("RF:Cav", "LLRF:Gate"))
						.concat("_LOCAL:EventSelect");
				repChs[i] = ChannelFactory.defaultFactory().getChannel(repPV);
				repChs[i].addConnectionListener(this);
				// repChs[i].requestConnection();
				InputPVTableCell pvCell1 = new InputPVTableCell(repChs[i], i, 1);
				tableModel.addPVCell(pvCell1, i, 1);
				getChannelVec(repChs[i]).add(pvCell1);

				cavVPV = (cellData[i][0].replace("RF:Cav", "LLRF:FCM"))
						.concat(":cavV");
				cavVChs[i] = ChannelFactory.defaultFactory().getChannel(cavVPV);
				cavVChs[i].addConnectionListener(this);
				// cavVChs[i].requestConnection();
				InputPVTableCell pvCell2 = new InputPVTableCell(cavVChs[i], i,
						3);
				tableModel.addPVCell(pvCell2, i, 3);
				getChannelVec(cavVChs[i]).add(pvCell2);

				i++;
			}

			cavTable = new JTable(tableModel);
			cavTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			cavTable.getColumnModel().getColumn(0).setPreferredWidth(120);

			// set the reference columns to red.
			DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
			dtcr.setForeground(Color.red);
			cavTable.getColumnModel().getColumn(2).setCellRenderer(dtcr);
			cavTable.getColumnModel().getColumn(4).setCellRenderer(dtcr);

			ListSelectionModel rowSM = cavTable.getSelectionModel();
			rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting())
						return;

					ListSelectionModel lsm = (ListSelectionModel) e.getSource();
					if (lsm.isSelectionEmpty()) {
						// do nothing
					} else {
						int selectedRow = lsm.getMinSelectionIndex();
						theHOM = selectedRow;
						// to perform action when a cavity is selected
						updatePlot(homNames[selectedRow]);
						System.out.println(cavTable.getValueAt(selectedRow, 0)
								+ " is selected.");
					}
				}
			});

			final TableProdder prodder = new TableProdder(tableModel);
			prodder.start();

			connectAll();
		}
		return cavTable;
	}

	protected void connectAll() {
		MakeConnections mc = new MakeConnections(this);
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

	private FunctionGraphsJPanel getHOM0Plot() {
		hom0Plot.addCurveData(hom0PlotData);
		hom0Plot.addCurveData(hom0aPlotData);
		hom0aPlotData.setColor(Color.red);
		hom0Plot.setName("HB0");
		hom0Plot.setLimitsAndTicksX(0., 1200., 400.);
		hom0Plot.addMouseListener(new SimpleChartPopupMenu(hom0Plot));
		return hom0Plot;
	}

	private FunctionGraphsJPanel getHOM1Plot() {
		hom1Plot.addCurveData(hom1PlotData);
		hom1Plot.addCurveData(hom1aPlotData);
		hom1aPlotData.setColor(Color.red);
		hom1Plot.setName("HB1");
		hom1Plot.setLimitsAndTicksX(0., 1200., 400.);
		hom1Plot.addMouseListener(new SimpleChartPopupMenu(hom1Plot));
		return hom1Plot;
	}

	protected void setPVLoggerID(long logID) {
		SCLCavPVLog pvLog = new SCLCavPVLog(logID);
		HashMap<String, double[][]> map = pvLog.getCavMap();
		for (int i = 0; i < cavs.size(); i++) {
			hom0a[i] = map.get(homNames[i])[0];
			hom1a[i] = map.get(homNames[i])[1];
		}

		try {
			HashMap<String, Double> map1 = pvLog.getCavMap1();
			HashMap<String, Double> map2 = pvLog.getCavMap2();
			
			// convert rep rate index to String
			HashMap<Double, String> repRateMap = new HashMap<Double, String>(8);
			repRateMap.put(new Double(0.0), "off");
			repRateMap.put(new Double(1.0), "1 Hz");
			repRateMap.put(new Double(2.0), "2 Hz");
			repRateMap.put(new Double(3.0), "5 Hz");
			repRateMap.put(new Double(4.0), "10 Hz");
			repRateMap.put(new Double(5.0), "20 Hz");
			repRateMap.put(new Double(6.0), "30 Hz");
			repRateMap.put(new Double(7.0), "60 Hz");
			
			for (int i = 0; i < cavs.size(); i++) {
				tableModel.setValueAt(repRateMap.get(map1.get(homNames[i].replace("HPM",
						"Gate"))), i, 2);
				tableModel.setValueAt(map2.get(homNames[i]
						.replace("HPM", "FCM")).toString(), i, 4);
				tableModel.fireTableCellUpdated(i, 2);
				tableModel.fireTableCellUpdated(i, 4);
			}
		} catch (NullPointerException e) {
			// don't do anything if there is no rep rate or cav field logged
		}

		if (theHOM >= 0)
			plotPVLoggerData();
	}

	private void plotPVLoggerData() {
		hom0aPlotData.setPoints(x1, hom0a[theHOM]);
		hom1aPlotData.setPoints(x1, hom1a[theHOM]);
		hom0Plot.refreshGraphJPanel();
		hom1Plot.refreshGraphJPanel();
	}

	private void updatePlot(String name) {
		stopMonitors();
		startMonitors(name);
		plotPVLoggerData();
	}

	private void startMonitors(String name) {
		ChannelFactory caF = ChannelFactory.defaultFactory();

		hom0StateCh = caF.getChannel(name + ":HBADC0_Ctl");
		hom1StateCh = caF.getChannel(name + ":HBADC1_Ctl");
		
		Channel hb0WF = caF.getChannel(name + ":HB0");
		Channel hb1WF = caF.getChannel(name + ":HB1");
		try {
			double[] tmpArry = hb0WF.getArrDbl();
			x1 = new double[tmpArry.length];
			// save which ADC's the present HOM 0 and 1 waveforms point to
			hom0State = Integer.parseInt(hom0StateCh.getValueRecord().stringValue());
			hom1State = Integer.parseInt(hom1StateCh.getValueRecord().stringValue());
			
		} catch (GetException ge) {
			// do nothing, use the default array size for waveforms
		} catch (ConnectionException ce) {
			// do nothing, use the default array size for waveforms
		}

		for (int i = 0; i < x1.length; i++) {
			x1[i] = i;
		}
		
		// set the HOM 0 and 1 waveforms to use ADC 4 and 5
		try {
			hom0StateCh.putRawValCallback(4, new PutListener() {
				public void putCompleted(Channel ch) {
					
				}
			});
			
			hom1StateCh.putRawValCallback(5, new PutListener() {
				public void putCompleted(Channel ch) {
					
				}
			});
		} catch (ConnectionException ce) {
			
		} catch (PutException pe) {
			
		}

		try {
			hb0Monitor = hb0WF.addMonitorValTime(new IEventSinkValTime() {
				public void eventValue(ChannelTimeRecord newRecord, Channel chan) {
					hom0 = newRecord.doubleArray();
					hom0PlotData.setPoints(x1, hom0);
					hom0Plot.refreshGraphJPanel();
				}
			}, Monitor.VALUE);

		} catch (ConnectionException e) {
			System.out.println("Cannot connect to " + hb0WF.getId());
		} catch (MonitorException e) {
			System.out.println("Cannot monitor " + hb0WF.getId());
		}

		try {
			hb1Monitor = hb1WF.addMonitorValTime(new IEventSinkValTime() {
				public void eventValue(ChannelTimeRecord newRecord, Channel chan) {
					hom1 = newRecord.doubleArray();
					hom1PlotData.setPoints(x1, hom1);
					hom1Plot.refreshGraphJPanel();
				}
			}, Monitor.VALUE);

		} catch (ConnectionException e) {
			System.out.println("Cannot connect to " + hb1WF.getId());
		} catch (MonitorException e) {
			System.out.println("Cannot monitor " + hb1WF.getId());
		}

	}

	private void stopMonitors() {
		if (hom0State >= 0 && hom0StateCh != null) {
			try {
				hom0StateCh.putRawValCallback(hom0State, new PutListener() {
					public void putCompleted(Channel ch) {
						
					}
				});
				
				hom1StateCh.putRawValCallback(hom1State, new PutListener() {
					public void putCompleted(Channel ch) {
						
					}
				});
			} catch (ConnectionException ce) {
				
			} catch (PutException pe) {
				
			}			
		}
		
		if (hb0Monitor != null) {
			hb0Monitor.clear();
			hb0Monitor = null;
		}

		if (hb1Monitor != null) {
			hb1Monitor.clear();
			hb1Monitor = null;
		}

	}

} // @jve:decl-index=0:visual-constraint="57,130"
