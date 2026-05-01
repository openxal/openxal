package xal.app.magnetcycling;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;

import xal.extension.widgets.swing.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.tools.apputils.*;
import xal.extension.widgets.plot.*;
import xal.tools.xml.*;
import xal.tools.data.DataAdaptor;

import xal.extension.scan.UpdatingEventController;

/**
 *  The RunnerController class is a container for Runner class instance and
 *  provides other operations realted to the cycling control.
 *
 *@author     shishlo
 */
public class RunnerController {

	//the controller to set up time tables parameters for power supply cyclers
	//this object has the same level as RunnerController and used here to
	//synchronize the power supply groups in both controllers
	private ContentController contentController = null;

	private Runner runner = new Runner();

	private PowerSupplyGroup runnerPSG = new PowerSupplyGroup();

	//all PS groups that could be used
	private Vector<PowerSupplyGroup> powerSupplyGroupV = new Vector<PowerSupplyGroup>();

	//Updating controller
	UpdatingEventController runnerUC = new UpdatingEventController();

	//message text field. It is actually message text field from MagnetcyclingDocument
	private JTextField messageTextLocal = new JTextField();

	//======================================================
	//GUI Objects
	//======================================================

	//runner panle with buttons and time step text field
	private JPanel runnerControllerPanel = new JPanel();

	private JList<PowerSupplyGroup> groupList = new JList<PowerSupplyGroup>(new DefaultListModel<PowerSupplyGroup>());

	private JTable psTable = new JTable();

	private FunctionGraphsJPanel graphSubPanel = new FunctionGraphsJPanel();

	private Vector<PowerSupplyCycler> PowerSupplyCyclerV = new Vector<PowerSupplyCycler>();

	private Color[] colors = {Color.red, Color.blue, Color.green, Color.black, Color.magenta, Color.cyan};

	/**
	 *  Constructor for the RunnerController object
	 */
	public RunnerController() {

		runnerUC.setUpdateTime(1.0);

		runnerPSG.setName("Runner PS Group");
		runner.addPowerSupplyGroup(runnerPSG);

		//set up left control panel
		JPanel cntrlPanel = new JPanel();
		cntrlPanel.setLayout(new VerticalLayout());

		cntrlPanel.add(runner.getPanel());

		groupList.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "pv goups"));
		cntrlPanel.add(groupList);

		//set up main panel
		runnerControllerPanel.setLayout(new BorderLayout());
		runnerControllerPanel.add(cntrlPanel, BorderLayout.WEST);
		runnerControllerPanel.add(graphSubPanel, BorderLayout.CENTER);

		//define graph panel's properties
		SimpleChartPopupMenu.addPopupMenuTo(graphSubPanel);
		graphSubPanel.setOffScreenImageDrawing(true);
		graphSubPanel.setName("Cycler : PV Values vs. Time");
		graphSubPanel.setAxisNames("time, sec", "PV Values");
		graphSubPanel.setGraphBackGroundColor(Color.white);
		graphSubPanel.setLegendButtonVisible(true);
		graphSubPanel.setLegendBackground(Color.white);
		graphSubPanel.setChooseModeButtonVisible(true);

		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(psTable);
		//??? this is stupid way to set vertical and horizontal size of the table
		scrollPane.setPreferredSize(new Dimension(1, 150));
		cntrlPanel.add(scrollPane);

		//List of PowerSupplyGroups
		groupList.setVisibleRowCount(5);
		groupList.setEnabled(true);
		groupList.setFixedCellWidth(10);
		ListSelectionListener groupListListener =
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					updatePSCyclerTable();
				}
			};

		groupList.addListSelectionListener(groupListListener);

		//Table of PowerSupplyCyclers
		AbstractTableModel tableModel =
			new AbstractTableModel() {
                /** serialization ID */
                private static final long serialVersionUID = 1L;
				public String getColumnName(int col) {
					if(col == 0) {
						return "Power Supply PV";
					}
					return " Use";
				}

				public int getRowCount() {
					return PowerSupplyCyclerV.size();
				}

				public int getColumnCount() {
					return 2;
				}

				public Object getValueAt(int row, int col) {
					PowerSupplyCycler psc = PowerSupplyCyclerV.get(row);
					if(col == 1) {
						return new Boolean(psc.getActive());
					}
					return psc.getChannelName();
				}

				public boolean isCellEditable(int row, int col) {
					if(col == 1) {
						return true;
					}
					return false;
				}

				public Class<?> getColumnClass(int c) {
					if(c == 1) {
						return (new Boolean(true)).getClass();
					}
					return (new String("a")).getClass();
				}

				public void setValueAt(Object value, int row, int col) {
					if(col == 1) {
						PowerSupplyCycler psc = PowerSupplyCyclerV.get(row);
						psc.setActive(!psc.getActive());
						runner.setNeedInit();
						updateGraphDataSet();
					}
					fireTableCellUpdated(row, col);
				}

			};

		psTable.setModel(tableModel);
		psTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumn column = null;
		for(int i = 0; i < 2; i++) {
			column = psTable.getColumnModel().getColumn(i);
			if(i == 1) {
				column.setPreferredWidth(1);
			} else {
				column.setPreferredWidth(1000);
			}
		}

		//set up listeners for start, step, error, and stop running
		runner.addStartListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					groupList.setEnabled(false);
					psTable.setEnabled(false);
					runnerUC.update();
				}
			});

		runner.addStepListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					runnerUC.update();
				}
			});

		runner.addStopListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(runner.isStartReady()) {
						groupList.setEnabled(true);
						psTable.setEnabled(true);
					} else {
						groupList.setEnabled(false);
						psTable.setEnabled(false);
					}
					runnerUC.update();
				}
			});

		runner.addInitListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(runner.isStartReady()) {
						groupList.setEnabled(true);
						psTable.setEnabled(true);
					} else {
						groupList.setEnabled(false);
						psTable.setEnabled(false);
					}
					runnerUC.update();
				}
			});

		//Update Controller action
		runnerUC.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					graphSubPanel.refreshGraphJPanel();
				}
			});

	}

	/**
	 *  Updates the ps cyclers table using selection in PS Group List
	 */
	private void updatePSCyclerTable() {
		PowerSupplyGroup[] objs = groupList.getSelectedValuesList().toArray( new PowerSupplyGroup[0] );
		if(objs.length > 0) {
			Vector<PowerSupplyCycler> v_tmp = new Vector<PowerSupplyCycler>();
			runnerPSG.removePowerSupplyCyclers();
			for(int i = 0, n = objs.length; i < n; i++) {
				PowerSupplyGroup psg = objs[i];
				Vector<PowerSupplyCycler> pscV = psg.getPowerSupplyCyclers();
				v_tmp.addAll(pscV);
				for(int j = 0, m = pscV.size(); j < m; j++) {
					PowerSupplyCycler psc = pscV.get(j);
					runnerPSG.addPowerSupplyCycler(psc);
				}
			}
			PowerSupplyCyclerV = v_tmp;
			((AbstractTableModel) psTable.getModel()).fireTableDataChanged();
			runner.setNeedInit();
			updateGraphDataSet();
		}
	}


	/**
	 *  Adds graph data to the Graph Panel.
	 */
	private void updateGraphDataSet() {
		Vector<BasicGraphData> gdV = new Vector<BasicGraphData>();
		int count = 0;
		for(int i = 0, n = PowerSupplyCyclerV.size(); i < n; i++) {
			PowerSupplyCycler psc = PowerSupplyCyclerV.get(i);
			if(psc.getActive()) {
				BasicGraphData gd = psc.getGraphSetPV();
				gd.setGraphColor(colors[count % colors.length]);
				gdV.add(gd);
				gd = psc.getGraphReadBackPV();
				gd.setGraphColor(colors[count % colors.length]);
				gdV.add(gd);
				count++;
			}
		}
		graphSubPanel.setGraphData(gdV);
	}


	/**
	 *  Returns the panel attribute of the RunnerController object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return runnerControllerPanel;
	}

	/**
	 *  Sets the all component fonts
	 *
	 *@param  fnt  The new font
	 */
	public void setFontForAll(Font fnt) {
		runner.setFontForAll(fnt);
		groupList.setFont(fnt);
		((TitledBorder) groupList.getBorder()).setTitleFont(fnt);
	}


	/**
	 *  Sets the ContentController to the RunnerController object
	 *
	 *@param  contentController  The ContentController instance
	 */
	public void setContentController(ContentController contentController) {
		this.contentController = contentController;
	}


	/**
	 *  Adds a PowerSupplyGroup.
	 *
	 *@param  psg  The PowerSupplyGroup
	 */
	public void addPowerSupplyGroup(PowerSupplyGroup psg) {
		powerSupplyGroupV.add(psg);
		DefaultListModel<PowerSupplyGroup> listModel = (DefaultListModel<PowerSupplyGroup>) groupList.getModel();
		listModel.addElement(psg);
		groupList.setSelectedIndex(-1);
		contentController.addPowerSupplyGroup(psg);
	}

	/**
	 *  Removes the PowerSupplyGroup.
	 *
	 *@param  psg  The PowerSupplyGroup
	 */
	public void removePowerSupplyGroup(PowerSupplyGroup psg) {
		powerSupplyGroupV.remove(psg);
		DefaultListModel<PowerSupplyGroup> listModel = (DefaultListModel<PowerSupplyGroup>) groupList.getModel();
		listModel.removeElement(psg);
		groupList.setSelectedIndex(-1);
		contentController.removePowerSupplyGroup(psg);
	}

	/**
	 *  Removes all PowerSupplyGroups.
	 */
	public void removePowerSupplyGroups() {
		powerSupplyGroupV.clear();
		DefaultListModel<PowerSupplyGroup> listModel = (DefaultListModel<PowerSupplyGroup>) groupList.getModel();
		listModel.clear();
		groupList.setSelectedIndex(-1);
		contentController.removePowerSupplyGroups();
	}


	/**
	 *  Returns the powerSupplyGroups attribute of the RunnerController object
	 *
	 *@return    The powerSupplyGroups value
	 */
	public Vector<PowerSupplyGroup> getPowerSupplyGroups() {
		return powerSupplyGroupV;
	}

	/**
	 *  Sets the selected PS Group indexes of the RunnerController object
	 *
	 *@param  indV  The Vector of Integers with indexes
	 */
	public void setSelectedPSGroupIndexes(Vector<Integer> indV) {
		int n = indV.size();
		int[] indices = new int[n];
		for(int i = 0; i < n; i++) {
			indices[i] = (indV.get(i)).intValue();
		}
		groupList.setSelectedIndices(indices);
	}

	/**
	 *  Does actions before the panel is going to show up
	 */
	public void isGoingToShowUp() {
		updatePSCyclerTable();
		messageTextLocal.setText(null);
	}

	/**
	 *  Returns the messageText attribute
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}

	//===================================================
	//Read and Write methods for initialization and save
	//===================================================

	/**
	 *  Reads data about power supplys groups and cyclers and initializes the
	 *  internal structures from data adaptor
	 *
	 *@param  data  The data adaptor
	 */
	public void readData(DataAdaptor data) {
		messageTextLocal.setText(null);
		DataAdaptor runnerDA = data.childAdaptor("RUNNER_CNTRL");
		if(runnerDA != null) {
            for (final DataAdaptor psgDA : runnerDA.childAdaptors("PowerSupplyGroup")) {
				PowerSupplyGroup psg = new PowerSupplyGroup();
				psg.setName(psgDA.stringValue("group_name"));
				//iteration over power supply cyclers
                
            for (final DataAdaptor pscDA : psgDA.childAdaptors("PowerSupplyCycler") ) {
                PowerSupplyCycler psc = new PowerSupplyCycler();

                DataAdaptor pscSetPV_DA =  pscDA.childAdaptor("PV_Set");
					if(!pscSetPV_DA.stringValue("pv_name").equals("null")) {
						psc.setChannelNameSet(pscSetPV_DA.stringValue("pv_name"));
					} else {
						psc.setChannelNameSet(null);
					}

					DataAdaptor pscRbPV_DA = pscDA.childAdaptor("PV_Rb");
					if(!pscRbPV_DA.stringValue("pv_name").equals("null")) {
						psc.setChannelNameRB(pscRbPV_DA.stringValue("pv_name"));
					} else {
						psc.setChannelNameRB(null);
					}

					DataAdaptor pscNeedCycle_DA = pscDA.childAdaptor("NeedCyclePV");
					if(!pscNeedCycle_DA.stringValue("pv_name").equals("null")) {
						psc.setChannelNameNeedCycle(pscNeedCycle_DA.stringValue("pv_name"));
					} else {
						psc.setChannelNameRB(null);
					}					

					DataAdaptor pscMaxCurr_DA = pscDA.childAdaptor("Max_I_Ampers");
					psc.setMaxCurrent(pscMaxCurr_DA.doubleValue("I"));

					DataAdaptor pscnCycl_DA = pscDA.childAdaptor("Number_of_Cycles");
					psc.setnCycles(pscnCycl_DA.intValue("n"));

					DataAdaptor pscChangeRate_DA =  pscDA.childAdaptor("Rate_Amper_per_sec");
					psc.setChangeRate(pscChangeRate_DA.doubleValue("rate"));

					DataAdaptor pscMinCurrTime_DA =  pscDA.childAdaptor("MinI_Time_sec");
					psc.setMinCurrTime(pscMinCurrTime_DA.doubleValue("time"));

					DataAdaptor pscMaxCurrTime_DA = pscDA.childAdaptor("MaxI_Time_sec");
					psc.setMaxCurrTime(pscMaxCurrTime_DA.doubleValue("time"));

					DataAdaptor pscActive_DA = pscDA.childAdaptor("Active");
					psc.setActive(pscActive_DA.booleanValue("isActive"));
					psg.addPowerSupplyCycler(psc);
				}
				addPowerSupplyGroup(psg);
			}
		} else {
			messageTextLocal.setText("The dummaged input file.");
		}
		updatePSCyclerTable();
	}

	/**
	 *  Writes data about power supplys groups and cyclers into the data adaptor
	 *
	 *@param  data  The data adaptor
	 */
	public void writeData(DataAdaptor data) {
		messageTextLocal.setText(null);
		DataAdaptor runnerDA = data.createChild("RUNNER_CNTRL");
		Vector<PowerSupplyGroup> psgV = getPowerSupplyGroups();
		for(int i = 0, n = psgV.size(); i < n; i++) {
			PowerSupplyGroup psg = psgV.get(i);
			DataAdaptor psgDA = runnerDA.createChild("PowerSupplyGroup");
			psgDA.setValue("group_name", psg.getName());
			for(int j = 0, nj = psg.getPowerSupplyCyclers().size(); j < nj; j++) {
				PowerSupplyCycler psc = psg.getPowerSupplyCyclers().get(j);
				DataAdaptor pscDA =  psgDA.createChild("PowerSupplyCycler");

				DataAdaptor pscSetPV_DA =  pscDA.createChild("PV_Set");
				pscSetPV_DA.setValue("pv_name", psc.getChannelName());

				DataAdaptor pscRbPV_DA =  pscDA.createChild("PV_Rb");
				pscRbPV_DA.setValue("pv_name", psc.getChannelNameRB());
				
				DataAdaptor pscNeedCycle_DA = pscDA.createChild("NeedCyclePV");
				pscNeedCycle_DA.setValue("pv_name", psc.getChannelNameNeedCycle());				

				DataAdaptor pscMaxCurr_DA = pscDA.createChild("Max_I_Ampers");
				pscMaxCurr_DA.setValue("I", psc.getMaxCurrent());

				DataAdaptor pscnCycl_DA = pscDA.createChild("Number_of_Cycles");
				pscnCycl_DA.setValue("n", psc.getnCycles());

				DataAdaptor pscChangeRate_DA = pscDA.createChild("Rate_Amper_per_sec");
				pscChangeRate_DA.setValue("rate", psc.getChangeRate());

				DataAdaptor pscMinCurrTime_DA = pscDA.createChild("MinI_Time_sec");
				pscMinCurrTime_DA.setValue("time", psc.getMinCurrTime());

				DataAdaptor pscMaxCurrTime_DA = pscDA.createChild("MaxI_Time_sec");
				pscMaxCurrTime_DA.setValue("time", psc.getMaxCurrTime());

				DataAdaptor pscActive_DA = pscDA.createChild("Active");
				pscActive_DA.setValue("isActive", psc.getActive());
			}
		}
	}

}

