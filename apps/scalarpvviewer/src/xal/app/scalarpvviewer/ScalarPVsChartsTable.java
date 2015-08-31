/*
 *  ScalarPVsChartsTable.java
 *
 *  Created on May 24, 2005
 */
package xal.app.scalarpvviewer;

import java.awt.*;
import java.awt.Color;
import java.text.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;

import xal.extension.widgets.plot.*;
import xal.tools.text.ScientificNumberFormat;
import xal.extension.scan.UpdatingEventController;

/**
 *  Shows the table with PVs names and atributes realted to the value, reference
 *  value, and difference.
 *
 *@author    shishlo
 */
public class ScalarPVsChartsTable {

	private JPanel mainLocalPanel = new JPanel(new BorderLayout());

	private JPanel tablePanel = new JPanel(new BorderLayout());

	private ScientificNumberFormat fmtVal = new ScientificNumberFormat( 4, 10, false );

	private JPanel leftTablePanel = new JPanel(new BorderLayout());
	private JPanel centerTablePanel = new JPanel(new BorderLayout());
	private JPanel rightTablePanel = new JPanel(new BorderLayout());

	private JTable leftTable = null;
	private JTable centerTable = null;
	private JTable rightTable = null;

	private String[] columnNamesLeftTable = {"#", "Val.", "Ref.", "Diff."};
	private String[] columnNamesCenterTable = {"PV Name"};
	private String[] columnNamesRightTable = {"Color"};

	private ScalarPVs spvs = null;

	private Vector<ActionListener> changeListenersV = new Vector<ActionListener>();


	/**
	 *  Constructor for the ScalarPVsChartsTable object
	 *
	 *@param  spvsIn  Scalar PVs object
	 */
	public ScalarPVsChartsTable(ScalarPVs spvsIn) {
		spvs = spvsIn;

		TableModel leftTableModel =
			new AbstractTableModel() {
                /** serialization ID */
                private static final long serialVersionUID = 1L;
				public String getColumnName(int col) {
					return columnNamesLeftTable[col].toString();
				}


				public int getRowCount() {
					return spvs.getSize();
				}


				public int getColumnCount() {
					return 4;
				}


				public Object getValueAt(int row, int col) {
					if (col == 0) {
						return new Integer(row + 1);
					}
					if (col == 1) {
						return new Boolean(spvs.getScalarPV(row).showValueChart());
					}
					if (col == 2) {
						return new Boolean(spvs.getScalarPV(row).showRefChart());
					}
					return new Boolean(spvs.getScalarPV(row).showDifChart());
				}


				public boolean isCellEditable(int row, int col) {
					if (col == 0) {
						return false;
					}
					return true;
				}


				public void setValueAt(Object value, int row, int col) {
					if (col == 0) {
						return;
					}
					if (col == 1) {
						spvs.getScalarPV(row).showValueChart(((Boolean) value).booleanValue());
					}
					if (col == 2) {
						spvs.getScalarPV(row).showRefChart(((Boolean) value).booleanValue());
					}
					if (col == 3) {
						spvs.getScalarPV(row).showDifChart(((Boolean) value).booleanValue());
					}
					fireTableCellUpdated(row, col);
				}


				public Class<?> getColumnClass(int c) {
					return getValueAt(0, c).getClass();
				}

			};

		TableModel centerTableModel =
			new AbstractTableModel() {
                /** serialization ID */
                private static final long serialVersionUID = 1L;
				public String getColumnName(int col) {
					return columnNamesCenterTable[col].toString();
				}


				public int getRowCount() {
					return spvs.getSize();
				}


				public int getColumnCount() {
					return 1;
				}


				public Object getValueAt(int row, int col) {
					return spvs.getScalarPV(row).getMonitoredPV().getChannelName();
				}


				public boolean isCellEditable(int row, int col) {
					return false;
				}
			};

		TableModel rightTableModel =
			new AbstractTableModel() {
                /** serialization ID */
                private static final long serialVersionUID = 1L;
				public String getColumnName(int col) {
					return columnNamesRightTable[col].toString();
				}


				public int getRowCount() {
					return spvs.getSize();
				}


				public int getColumnCount() {
					return 1;
				}


				public Object getValueAt(int row, int col) {
					return spvs.getScalarPV(row).getChartColor();
				}


				public boolean isCellEditable(int row, int col) {
					return false;
				}


				public Class<?> getColumnClass(int c) {
					return getValueAt(0, c).getClass();
				}

			};

		//make tables
		leftTable = new JTable(leftTableModel);
		centerTable = new JTable(centerTableModel);
		rightTable = new JTable(rightTableModel);

		rightTable.setDefaultRenderer(
				Color.red.getClass(),
			new TableCellRenderer() {
				public Component getTableCellRendererComponent(JTable table,
						Object value,
						boolean isSelected,
						boolean hasFocus,
						int row,
						int col) {
					JLabel jl = new JLabel(" ");
					jl.setOpaque(true);
					jl.setBackground(spvs.getScalarPV(row).getChartColor());
					return jl;
				}
			});

		leftTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		leftTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		leftTable.getColumnModel().getColumn(2).setPreferredWidth(50);

		leftTable.setGridColor(Color.black);
		centerTable.setGridColor(Color.black);
		rightTable.setGridColor(Color.black);

		leftTable.setShowGrid(true);
		centerTable.setShowGrid(true);
		rightTable.setShowGrid(true);

		//make panels
		leftTablePanel.add(leftTable.getTableHeader(), BorderLayout.PAGE_START);
		leftTablePanel.add(leftTable, BorderLayout.CENTER);
		centerTablePanel.add(centerTable.getTableHeader(), BorderLayout.PAGE_START);
		centerTablePanel.add(centerTable, BorderLayout.CENTER);
		rightTablePanel.add(rightTable.getTableHeader(), BorderLayout.PAGE_START);
		rightTablePanel.add(rightTable, BorderLayout.CENTER);

		tablePanel.add(leftTablePanel, BorderLayout.WEST);
		tablePanel.add(centerTablePanel, BorderLayout.CENTER);
		tablePanel.add(rightTablePanel, BorderLayout.EAST);

		JScrollPane scrollpane = new JScrollPane(tablePanel);

		mainLocalPanel.add(scrollpane, BorderLayout.CENTER);

		//event definition
		leftTableModel.addTableModelListener(
			new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					ActionEvent stateChangedAction = new ActionEvent(spvs, 0, "changed");
					int nL = changeListenersV.size();
					for (int i = 0; i < nL; i++) {
						( changeListenersV.get(i)).actionPerformed(stateChangedAction);
					}
				}
			});

	}



	/**
	 *  Returns the main panel of these tables
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return mainLocalPanel;
	}


	/**
	 *  Adds the change listener
	 *
	 *@param  actionListener  The change listener
	 */
	public void addChangeListener(ActionListener actionListener) {
		if (actionListener != null) {
			changeListenersV.add(actionListener);
		}
	}


	/**
	 *  Removes the change listener
	 *
	 *@param  actionListener  The change listener
	 */
	public void removeChangeListener(ActionListener actionListener) {
		changeListenersV.remove(actionListener);
	}


	/**
	 *  Removes all change listeners
	 */
	public void removeChangeListeners() {
		changeListenersV.clear();
	}


	/**
	 *  Sets the font attribute of the ScalarPVsChartsTable object
	 *
	 *@param  fnt  The new font value
	 */
	public void setFont(Font fnt) {

		leftTable.setFont(fnt);
		centerTable.setFont(fnt);
		rightTable.setFont(fnt);

		leftTable.getTableHeader().setFont(fnt);
		centerTable.getTableHeader().setFont(fnt);
		rightTable.getTableHeader().setFont(fnt);

		leftTable.setRowHeight(fnt.getSize() + 2);
		centerTable.setRowHeight(fnt.getSize() + 2);
		rightTable.setRowHeight(fnt.getSize() + 2);

		leftTable.getColumnModel().getColumn(0).setPreferredWidth(fnt.getSize() * 4);
		leftTable.getColumnModel().getColumn(1).setPreferredWidth(fnt.getSize() * 4);
		leftTable.getColumnModel().getColumn(2).setPreferredWidth(fnt.getSize() * 4);

		rightTable.getColumnModel().getColumn(0).setPreferredWidth(fnt.getSize() * 4);

	}


	/**
	 *  Layout all tables
	 */
	public void doLayout() {
		leftTable.doLayout();
		centerTable.doLayout();
		rightTable.doLayout();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  args  Command lines arguments
	 */
	public static void main(String args[]) {
		JFrame mainFrame = new JFrame("Test of the ScalarPVsChartsTable class");
		mainFrame.addWindowListener(
			new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					System.exit(0);
				}
			}
				);

		mainFrame.getContentPane().setLayout(new BorderLayout());

		UpdatingEventController uc = new UpdatingEventController();
		ScalarPVs ss = new ScalarPVs(uc);
		ScalarPVsChartsTable spvT = new ScalarPVsChartsTable(ss);

		mainFrame.getContentPane().add(spvT.getPanel(), BorderLayout.CENTER);

		ss.addScalarPV("One0", 1.0);
		ss.addScalarPV("One1", 2.0);
		ss.addScalarPV("One2", 3.0);
		ss.addScalarPV("One3", 4.0);
		ss.addScalarPV("One4", 5.0);

		mainFrame.pack();
		mainFrame.setSize(new Dimension(300, 430));
		mainFrame.setVisible(true);

		Font globalFont = new Font("Monospaced", Font.PLAIN, 14);

		spvT.setFont(globalFont);

		spvT.addChangeListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					System.out.println("debug STATE changed!");
				}
			});

	}

}

