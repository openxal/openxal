package xal.app.diagtiming;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionListener;
import xal.ca.GetException;
import xal.ca.ConnectionException;
import xal.ca.Monitor;
import xal.ca.MonitorException;
import xal.ca.IEventSinkValTime;
import xal.ca.ChannelTimeRecord;
import xal.smf.AcceleratorNode;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.tools.apputils.EdgeLayout;
import xal.extension.widgets.plot.CurveData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.smf.impl.CurrentMonitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.util.*;

public class BCMPane extends JPanel implements ConnectionListener,
		ActionListener {
	static final long serialVersionUID = 0;

	/**
	 * 0 = linac BPM, 1 = ring BPM, 2 = RTBT BPM, 4 = BCM
	 */
	int typeInd = 4;
	
	List<CurrentMonitor> theNodes;
	String[] bcmNames;
	
	JTable bcmTable;
	DeviceTableModel bcmTableModel;
	
	int selectedRow;
	
	Monitor bcmWFMonitor = null;
	double[] x1 = new double[1200];
	double[] yBCMWF = new double[1200];
	
	FunctionGraphsJPanel bcmPlot;
	CurveData bcmPlotData = new CurveData();
	
	Channel[] bcmWFChs;
	
	public void connectionDropped(Channel channel) {
		// TODO Auto-generated method stub

	}

	public void connectionMade(Channel channel) {
		// TODO Auto-generated method stub

	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	protected void initializeBCMPane(List<CurrentMonitor> nodes) {
		theNodes = nodes;
		
		String[] columnNames = { "BCM" };
		bcmTableModel = new DeviceTableModel(columnNames, nodes.size());
		bcmTable = new JTable(bcmTableModel);
		
		bcmNames = new String[nodes.size()];
		
		bcmWFChs = new Channel[nodes.size()];
		
		for (int i = 0; i < nodes.size(); i++) {
			bcmNames[i] = (nodes.get(i)).getId();
			bcmTableModel.addRowName(bcmNames[i], i);
			
			bcmWFChs[i] = ChannelFactory.defaultFactory().getChannel(bcmNames[i] + ":currentTBT");
		}
		
		JScrollPane bcmScrollPane = new JScrollPane(bcmTable);
		bcmScrollPane.setPreferredSize(new Dimension(200, 400));

		EdgeLayout edgeLayout = new EdgeLayout();
		setLayout(edgeLayout);
		edgeLayout.setConstraints(bcmScrollPane, 50, 30, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(bcmScrollPane);

		JPanel plotPane = new JPanel();
		plotPane.setLayout(new BoxLayout(plotPane, BoxLayout.Y_AXIS));
		plotPane.setPreferredSize(new Dimension(750, 470));
		
		bcmPlot = new FunctionGraphsJPanel();
		bcmPlot.setLimitsAndTicksX(0., 1200., 400.);
		bcmPlot.addCurveData(bcmPlotData);
		bcmPlot.setName("BCM Waveform: ");
		bcmPlot.setAxisNames("point no.", "I (mA)");
		bcmPlot.addMouseListener(new SimpleChartPopupMenu(bcmPlot));
		plotPane.add(bcmPlot);
		
		edgeLayout.setConstraints(plotPane, 20, 470, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(plotPane);
		
		bcmTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		ListSelectionModel rowSM = bcmTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					// do nothing
				} else {
					selectedRow = lsm.getMinSelectionIndex();
					updatePlot(bcmNames[selectedRow]);
				}
			}
		});
		
	}

	protected void connectAll() {
		MakeConnections mc = new MakeConnections(this, typeInd);
		Thread thread = new Thread(mc);
		thread.start();
	}

	private void updatePlot(String name) {
		stopMonitors();
		startMonitors(name);
	}

	private void startMonitors(String name) {
		ChannelFactory caF = ChannelFactory.defaultFactory();

			Channel bcmWF = caF.getChannel(name + ":currentTBT");
			try {
				double[] tmpArry = bcmWF.getArrDbl();
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
				bcmWFMonitor = bcmWF.addMonitorValTime(
						new IEventSinkValTime() {
							public void eventValue(ChannelTimeRecord newRecord,
									Channel chan) {
								yBCMWF = newRecord.doubleArray();
								bcmPlotData.setPoints(x1, yBCMWF);
								bcmPlot.refreshGraphJPanel();
							}
						}, Monitor.VALUE);

			} catch (ConnectionException e) {
				System.out.println("Cannot connect to " + bcmWF.getId());
			} catch (MonitorException e) {
				System.out.println("Cannot monitor " + bcmWF.getId());
			}		
	}

	private void stopMonitors() {
		if (bcmWFMonitor != null) {
			bcmWFMonitor.clear();
			bcmWFMonitor = null;
		}
	}
}
