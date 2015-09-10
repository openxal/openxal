package xal.app.magnetcycling;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;

import xal.extension.widgets.swing.*;
import xal.tools.apputils.*;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import java.text.*;

import xal.ca.view.*;

import xal.extension.scan.UpdatingEventController;

/**
 *  The ContentController class is a container for power cycling groups and
 *  provides possibility to change their content and set parameters of cycling
 *  for every power cycler.
 *
 *@author     shishlo
 */
public class ContentController {

	//active power supply group
	private PowerSupplyGroup runnerPSG = new PowerSupplyGroup();

	//all PS groups that could be used
	private Vector<PowerSupplyGroup> powerSupplyGroupV = new Vector<PowerSupplyGroup>();

	//Updating controller
	UpdatingEventController contentUC = new UpdatingEventController();

	//message text field. It is actually message text field from MagnetcyclingDocument
	private JTextField messageTextLocal = new JTextField();

	//======================================================
	//GUI Objects
	//======================================================

	//content panel with all elements
	private JPanel contentControllerPanel = new JPanel();

	private JTextField leftTopTitleText = new JTextField("============= PS Groups =============");

	private JList<PowerSupplyGroup> groupList = new JList<PowerSupplyGroup>(new DefaultListModel<PowerSupplyGroup>());

	private JTable psTable = new JTable();

	private FunctionGraphsJPanel graphSubPanel = new FunctionGraphsJPanel();

	private Vector<PowerSupplyCycler> PowerSupplyCyclerV = new Vector<PowerSupplyCycler>();

	//The Power Supply Adding panel
	private JPanel addRemovePanel = new JPanel();
	private JButton addPSButton = new JButton("Add New Power Supply");
	private JButton removePSButton = new JButton("Remove Power Supply");

	private JLabel pvSetLabel = new JLabel(" <- Current PV Set");
	private JLabel pvRBLabel = new JLabel(" <- Current PV Read Back");

	private JTextField pvSetNameJText = new JTextField(new ChannelNameDocument(), "", 30);
	private JTextField pvRBNameJText = new JTextField(new ChannelNameDocument(), "", 30);

	//parameters panel
	private JPanel timeTableParamsPanel = new JPanel();

	private JRadioButton setToAllButton = new JRadioButton("Set to all PS cyclers in Table", false);

	private DecimalFormat decimalFormat = new DecimalFormat("####.###");
	private DecimalFormat intFormat = new DecimalFormat("###");

	private DoubleInputTextField nCyclesText = new DoubleInputTextField(10);
	private DoubleInputTextField changeRateText = new DoubleInputTextField(10);
	private DoubleInputTextField minCurrTimeText = new DoubleInputTextField(10);
	private DoubleInputTextField maxCurrTimeText = new DoubleInputTextField(10);

	private DoubleInputTextField maxCurrText = new DoubleInputTextField(10);

	private JButton nCyclesButton = new JButton("<- Number of Cycles");
	private JButton changeRateButton = new JButton("<- Set d(I)/d(t) [A/sec]");
	private JButton minCurrTimeButton = new JButton("<- I=0 duration [sec]");
	private JButton maxCurrTimeButton = new JButton("<- I=Max duration [sec]");

	private JButton maxCurrButton = new JButton("<- Max I [A]");

	/**
	 *  Constructor for the ContentController object
	 */
	public ContentController() {

		contentUC.setUpdateTime(1.0);

		runnerPSG.setName("PS Group");

		makeAddRemovePanel();
		makeParamsPanel();

		//set up left control panel
		JPanel cntrlPanel = new JPanel();
		cntrlPanel.setLayout(new VerticalLayout());

		cntrlPanel.add(leftTopTitleText);

		groupList.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "pv goups"));
		cntrlPanel.add(groupList);

		//set up graph and button panel
		JPanel graphAndButtonPanel = new JPanel(new BorderLayout());
		graphAndButtonPanel.add(graphSubPanel, BorderLayout.CENTER);
		graphAndButtonPanel.add(addRemovePanel, BorderLayout.SOUTH);
		graphAndButtonPanel.setBorder(BorderFactory.createEtchedBorder());

		//set up main panel
		contentControllerPanel.setLayout(new BorderLayout());
		contentControllerPanel.add(cntrlPanel, BorderLayout.WEST);
		contentControllerPanel.add(graphAndButtonPanel, BorderLayout.CENTER);

		//define graph panel's properties
		SimpleChartPopupMenu.addPopupMenuTo(graphSubPanel);
		graphSubPanel.setOffScreenImageDrawing(true);
		graphSubPanel.setName("Cycler : Current vs. Time");
		graphSubPanel.setAxisNames("time, sec", "Current, A");
		graphSubPanel.setGraphBackGroundColor(Color.white);
		graphSubPanel.setLegendButtonVisible(true);
		graphSubPanel.setLegendBackground(Color.white);

		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(psTable);
		//??? this is stupid way to set vertical and horizontal size of the table
		scrollPane.setPreferredSize(new Dimension(1, 150));
		cntrlPanel.add(scrollPane);

		//add parameters panel
		cntrlPanel.add(timeTableParamsPanel);

		//List of PowerSupplyGroups
		groupList.setVisibleRowCount(5);
		groupList.setEnabled(true);
		groupList.setFixedCellWidth(10);
		groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionListener groupListListener =
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					psgSelectionChange();
				}
			};

		groupList.addListSelectionListener(groupListListener);

		//Table of PowerSupplyCyclers
		AbstractTableModel tableModel =
			new AbstractTableModel() {
                /** ID for serializable version */
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

		psTable.getSelectionModel().addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					updateGraphDataSet();
					updateAddRemovePanel();
					updateParamsPanel();
				}
			});

		//Update Controller action
		contentUC.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					graphSubPanel.refreshGraphJPanel();
				}
			});

	}


	/**
	 *  Updates the content of the power supply table because of the changes in
	 *  power supply group selection or content
	 */
	private void psgSelectionChange() {
		Object obj = groupList.getSelectedValue();
		if(obj != null) {
			Vector<PowerSupplyCycler> v_tmp = new Vector<PowerSupplyCycler>();
			runnerPSG.removePowerSupplyCyclers();
			PowerSupplyGroup psg = (PowerSupplyGroup) obj;
			Vector<PowerSupplyCycler> pscV = psg.getPowerSupplyCyclers();
			v_tmp.addAll(pscV);
			for(int j = 0, m = pscV.size(); j < m; j++) {
				PowerSupplyCycler psc = pscV.get(j);
				runnerPSG.addPowerSupplyCycler(psc);
			}
			PowerSupplyCyclerV = v_tmp;
			((AbstractTableModel) psTable.getModel()).fireTableDataChanged();
			updateGraphDataSet();
		}
	}


	/**
	 *  Updates the add and remove power supply panel
	 */
	private void updateAddRemovePanel() {
		int i_selected = psTable.getSelectedRow();
		if(i_selected >= 0) {
			removePSButton.setEnabled(true);
			PowerSupplyCycler psc = PowerSupplyCyclerV.get(i_selected);
			if(!psc.getChannelName().equals("null")) {
				pvSetNameJText.setText(psc.getChannelName());
			} else {
				pvSetNameJText.setText(null);
			}
			if(!psc.getChannelNameRB().equals("null")) {
				pvRBNameJText.setText(psc.getChannelNameRB());
			} else {
				pvRBNameJText.setText(null);
			}
		} else {
			removePSButton.setEnabled(false);
			pvSetNameJText.setText(null);
			pvRBNameJText.setText(null);
		}
		messageTextLocal.setText(null);
	}

	/**
	 *  Updates the subpanel with parametrs
	 */
	private void updateParamsPanel() {

		int i_selected = psTable.getSelectedRow();
		if(i_selected >= 0) {
			PowerSupplyCycler psc = PowerSupplyCyclerV.get(i_selected);

			int nCycles = psc.getnCycles();
			nCyclesText.setValue((double) nCycles);

			double changeRate = psc.getChangeRate();
			changeRateText.setValue(changeRate);

			double minCurrTime = psc.getMinCurrTime();
			minCurrTimeText.setValue(minCurrTime);

			double maxCurrTime = psc.getMaxCurrTime();
			maxCurrTimeText.setValue(maxCurrTime);

			double maxCurr = psc.getMaxCurrent();
			maxCurrText.setValue(maxCurr);
		}
	}


	/**
	 *  Adds graph data to the Graph Panel.
	 */
	private void updateGraphDataSet() {
		Vector<BasicGraphData> gdV = new Vector<BasicGraphData>();
		int i_selected = psTable.getSelectedRow();
		if(i_selected >= 0 && i_selected <= (PowerSupplyCyclerV.size() - 1)) {
			PowerSupplyCycler psc = PowerSupplyCyclerV.get(i_selected);
			psc.createTimeTable();
			if(psc.getActive()) {
				BasicGraphData gd = psc.getGraphTimeTable();
				gd.setGraphColor(Color.blue);
				gd.setLineThick(5);
				gd.setGraphPointSize(11);
				gdV.add(gd);
			}
		}
		graphSubPanel.setGraphData(gdV);
	}


	/**
	 *  Returns the panel attribute of the ContentController object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return contentControllerPanel;
	}

	/**
	 *  Sets the all component fonts
	 *
	 *@param  fnt  The new font
	 */
	public void setFontForAll(Font fnt) {
		leftTopTitleText.setFont(fnt);
		groupList.setFont(fnt);
		((TitledBorder) groupList.getBorder()).setTitleFont(fnt);

		addPSButton.setFont(fnt);
		removePSButton.setFont(fnt);
		pvSetLabel.setFont(fnt);
		pvRBLabel.setFont(fnt);
		pvSetNameJText.setFont(fnt);
		pvRBNameJText.setFont(fnt);

		changeRateText.setFont(fnt);
		nCyclesText.setFont(fnt);
		minCurrTimeText.setFont(fnt);
		maxCurrTimeText.setFont(fnt);
		maxCurrText.setFont(fnt);

		nCyclesButton.setFont(fnt);
		changeRateButton.setFont(fnt);
		minCurrTimeButton.setFont(fnt);
		maxCurrTimeButton.setFont(fnt);
		setToAllButton.setFont(fnt);
		maxCurrButton.setFont(fnt);
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
	}

	/**
	 *  Removes all PowerSupplyGroups.
	 */
	public void removePowerSupplyGroups() {
		powerSupplyGroupV.clear();
		DefaultListModel<PowerSupplyGroup> listModel = (DefaultListModel<PowerSupplyGroup>) groupList.getModel();
		listModel.clear();
		groupList.setSelectedIndex(-1);
	}


	/**
	 *  Returns the powerSupplyGroups attribute of the ContentController object
	 *
	 *@return    The powerSupplyGroups value
	 */
	public Vector<PowerSupplyGroup> getPowerSupplyGroups() {
		return powerSupplyGroupV;
	}

	/**
	 *  Sets the selected PS Group indexes of the ContentController object
	 *
	 *@param  indV  The Vector of Integers with indexes
	 */
	public void setSelectedPSGroupIndexes(Vector<Integer> indV) {
		int n = indV.size();
		int[] indices = new int[n];
		for(int i = 0; i < n; i++) {
			indices[i] = ( indV.get(i)).intValue();
		}
		groupList.setSelectedIndices(indices);
	}

	/**
	 *  Returns the messageText attribute
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}

	//--------------------------------------------------
	//private methods
	//--------------------------------------------------

	/**
	 *  Creates the panel with add and remove buttons.
	 */
	private void makeAddRemovePanel() {
		addRemovePanel.setLayout(new GridLayout(3, 1, 1, 1));

		addRemovePanel.setBorder(BorderFactory.createEtchedBorder());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		JPanel pvSetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		JPanel pvRbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));

		addRemovePanel.add(buttonPanel);
		addRemovePanel.add(pvSetPanel);
		addRemovePanel.add(pvRbPanel);

		buttonPanel.add(addPSButton);
		buttonPanel.add(removePSButton);

		pvSetPanel.add(pvSetNameJText);
		pvSetPanel.add(pvSetLabel);

		pvRbPanel.add(pvRBNameJText);
		pvRbPanel.add(pvRBLabel);

		removePSButton.setEnabled(false);

		//make buttons action
		addPSButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					PowerSupplyGroup obj = groupList.getSelectedValue();
					if(obj != null) {
						PowerSupplyGroup psg = obj;
						if(pvSetNameJText.getText().length() > 0) {
							PowerSupplyCycler psc = new PowerSupplyCycler();
							psc.setChannelNameSet(pvSetNameJText.getText());
							if(pvRBNameJText.getText().length() > 0) {
								psc.setChannelNameRB(pvRBNameJText.getText());
							}
							psg.addPowerSupplyCycler(psc);
							psgSelectionChange();
							int n_psc = PowerSupplyCyclerV.size();
							psTable.setRowSelectionInterval(n_psc - 1, n_psc - 1);
							((AbstractTableModel) psTable.getModel()).fireTableDataChanged();
						} else {
							messageTextLocal.setText("The PV Set name is empty! Please, fix it!");
						}
					} else {
						messageTextLocal.setText("Please select the Power Supply Group!");
					}
				}
			});

		removePSButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					Object obj = groupList.getSelectedValue();
					if(obj != null) {
						PowerSupplyGroup psg = (PowerSupplyGroup) obj;
						int i_selected = psTable.getSelectedRow();
						if(i_selected >= 0 && i_selected <= (PowerSupplyCyclerV.size() - 1)) {
							PowerSupplyCycler psc = PowerSupplyCyclerV.get(i_selected);
							psg.removePowerSupplyCycler(psc);
							psgSelectionChange();
							((AbstractTableModel) psTable.getModel()).fireTableDataChanged();
						}
					}
				}
			});
	}

	/**
	 *  Creates the panel with add and remove buttons.
	 */
	private void makeParamsPanel() {

		timeTableParamsPanel.setLayout(new BorderLayout());
		timeTableParamsPanel.setBorder(BorderFactory.createEtchedBorder());

		JPanel innerPanel = new JPanel(new GridLayout(5, 1, 1, 1));
		innerPanel.setBorder(BorderFactory.createEtchedBorder());

		JPanel setMaxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		setMaxPanel.setBorder(BorderFactory.createEtchedBorder());

		JPanel tmp_main = new JPanel(new BorderLayout());
		tmp_main.add(innerPanel, BorderLayout.NORTH);
		tmp_main.add(setMaxPanel, BorderLayout.CENTER);

		timeTableParamsPanel.add(tmp_main, BorderLayout.NORTH);

		//add subpanels with buttons and text fields
		JPanel tmp_1_Panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		tmp_1_Panel.add(setToAllButton);
		innerPanel.add(tmp_1_Panel);

		JPanel tmp_2_Panel = new JPanel(new GridLayout(1, 2, 1, 1));
		tmp_2_Panel.add(nCyclesText);
		tmp_2_Panel.add(nCyclesButton);
		innerPanel.add(tmp_2_Panel);

		JPanel tmp_3_Panel = new JPanel(new GridLayout(1, 2, 1, 1));
		tmp_3_Panel.add(changeRateText);
		tmp_3_Panel.add(changeRateButton);
		innerPanel.add(tmp_3_Panel);

		JPanel tmp_4_Panel = new JPanel(new GridLayout(1, 2, 1, 1));
		tmp_4_Panel.add(minCurrTimeText);
		tmp_4_Panel.add(minCurrTimeButton);
		innerPanel.add(tmp_4_Panel);

		JPanel tmp_5_Panel = new JPanel(new GridLayout(1, 2, 1, 1));
		tmp_5_Panel.add(maxCurrTimeText);
		tmp_5_Panel.add(maxCurrTimeButton);
		innerPanel.add(tmp_5_Panel);

		JPanel tmp_max_Panel = new JPanel(new GridLayout(1, 2, 1, 1));
		tmp_max_Panel.add(maxCurrText);
		tmp_max_Panel.add(maxCurrButton);
		setMaxPanel.add(tmp_max_Panel);

		//elements
		changeRateText.setNormalBackground(Color.white);
		nCyclesText.setNormalBackground(Color.white);
		minCurrTimeText.setNormalBackground(Color.white);
		maxCurrTimeText.setNormalBackground(Color.white);
		maxCurrText.setNormalBackground(Color.white);

		changeRateText.setNumberFormat(decimalFormat);
		nCyclesText.setNumberFormat(intFormat);
		minCurrTimeText.setNumberFormat(decimalFormat);
		maxCurrTimeText.setNumberFormat(decimalFormat);
		maxCurrText.setNumberFormat(decimalFormat);

		changeRateText.setHorizontalAlignment(JTextField.CENTER);
		nCyclesText.setHorizontalAlignment(JTextField.CENTER);
		minCurrTimeText.setHorizontalAlignment(JTextField.CENTER);
		maxCurrTimeText.setHorizontalAlignment(JTextField.CENTER);
		maxCurrText.setHorizontalAlignment(JTextField.CENTER);

		nCyclesButton.setHorizontalAlignment(SwingConstants.LEFT);
		changeRateButton.setHorizontalAlignment(SwingConstants.LEFT);
		minCurrTimeButton.setHorizontalAlignment(SwingConstants.LEFT);
		maxCurrTimeButton.setHorizontalAlignment(SwingConstants.LEFT);

		//set actions to the buttons
		nCyclesButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(setToAllButton.isSelected()) {
						for(int i = 0, n = PowerSupplyCyclerV.size(); i < n; i++) {
							PowerSupplyCycler psc = PowerSupplyCyclerV.get(i);
							if(psc.getActive()) {
								//do settings
								psc.setnCycles((int) Math.round(nCyclesText.getValue()));
							}
						}
						messageTextLocal.setText(null);
					} else {
						int i_selected = psTable.getSelectedRow();
						if(i_selected >= 0) {
							PowerSupplyCycler psc = PowerSupplyCyclerV.get(i_selected);
							//do settings
							psc.setnCycles((int) Math.round(nCyclesText.getValue()));
							messageTextLocal.setText(null);
						} else {
							messageTextLocal.setText("Please select the Power Supply from the Table.");
						}
					}
					updateGraphDataSet();
				}
			});

		changeRateButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(setToAllButton.isSelected()) {
						for(int i = 0, n = PowerSupplyCyclerV.size(); i < n; i++) {
							PowerSupplyCycler psc = PowerSupplyCyclerV.get(i);
							if(psc.getActive()) {
								//do settings
								psc.setChangeRate(changeRateText.getValue());
							}
						}
						messageTextLocal.setText(null);
					} else {
						int i_selected = psTable.getSelectedRow();
						if(i_selected >= 0) {
							PowerSupplyCycler psc = PowerSupplyCyclerV.get(i_selected);
							//do settings
							psc.setChangeRate(changeRateText.getValue());
							messageTextLocal.setText(null);
						} else {
							messageTextLocal.setText("Please select the Power Supply from the Table.");
						}
					}
					updateGraphDataSet();
				}
			});

		minCurrTimeButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(setToAllButton.isSelected()) {
						for(int i = 0, n = PowerSupplyCyclerV.size(); i < n; i++) {
							PowerSupplyCycler psc = PowerSupplyCyclerV.get(i);
							if(psc.getActive()) {
								//do settings
								psc.setMinCurrTime(minCurrTimeText.getValue());
							}
						}
						messageTextLocal.setText(null);
					} else {
						int i_selected = psTable.getSelectedRow();
						if(i_selected >= 0) {
							PowerSupplyCycler psc = PowerSupplyCyclerV.get(i_selected);
							//do settings
							psc.setMinCurrTime(minCurrTimeText.getValue());
							messageTextLocal.setText(null);
						} else {
							messageTextLocal.setText("Please select the Power Supply from the Table.");
						}
					}
					updateGraphDataSet();
				}
			});

		maxCurrTimeButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(setToAllButton.isSelected()) {
						for(int i = 0, n = PowerSupplyCyclerV.size(); i < n; i++) {
							PowerSupplyCycler psc = PowerSupplyCyclerV.get(i);
							if(psc.getActive()) {
								//do settings
								psc.setMaxCurrTime(maxCurrTimeText.getValue());
							}
						}
						messageTextLocal.setText(null);
					} else {
						int i_selected = psTable.getSelectedRow();
						if(i_selected >= 0) {
							PowerSupplyCycler psc = PowerSupplyCyclerV.get(i_selected);
							//do settings
							psc.setMaxCurrTime(maxCurrTimeText.getValue());
							messageTextLocal.setText(null);
						} else {
							messageTextLocal.setText("Please select the Power Supply from the Table.");
						}
					}
					updateGraphDataSet();
				}
			});

		maxCurrButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i_selected = psTable.getSelectedRow();
					if(i_selected >= 0) {
						PowerSupplyCycler psc = PowerSupplyCyclerV.get(i_selected);
						//do settings
						psc.setMaxCurrent(maxCurrText.getValue());
						messageTextLocal.setText(null);
					} else {
						messageTextLocal.setText("Please select the Power Supply from the Table.");
					}
					updateGraphDataSet();
				}
			});

	}
}

