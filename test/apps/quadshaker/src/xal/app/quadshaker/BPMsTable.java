package xal.app.quadshaker;

import java.net.*;
import java.io.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class BPMsTable extends AbstractTableModel {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;

	private DefaultListModel<BPM_Element> listModel = new DefaultListModel<BPM_Element>();

	/**
	 *  Constructor for the BPMsList object
	 */
	public BPMsTable() {

		listModel.addListDataListener(
			new ListDataListener() {

				public void contentsChanged(ListDataEvent e) {
					fireTableDataChanged();
				}

				public void intervalAdded(ListDataEvent e) {
					fireTableDataChanged();
				}

				public void intervalRemoved(ListDataEvent e) {
					fireTableDataChanged();
				}

			});
	}

	/**
	 *  Returns the listModel attribute of the BPMsList object
	 *
	 *@return    The listModel value
	 */
	public DefaultListModel<BPM_Element> getListModel() {
		return listModel;
	}

	/**
	 *  Returns the bPM attribute of the BPMsTable object
	 *
	 *@param  name  The Parameter
	 *@return       The bPM value
	 */
	public BPM_Element getBPM(String name) {
		BPM_Element bpmElm = null;
		Enumeration<BPM_Element> bpm_enum = listModel.elements();
		while(bpm_enum.hasMoreElements()) {
			BPM_Element bpmElm1 = bpm_enum.nextElement();
			if(bpmElm1.getName().equals(name)) {
				bpmElm = bpmElm1;
			}
		}
		return bpmElm;
	}


	/**
	 *  Description of the Method
	 */
	public void startMonitor() {
		for(Enumeration<BPM_Element> e = listModel.elements(); e.hasMoreElements(); ) {
			BPM_Element elm = e.nextElement();
			elm.startMonitor();
		}
	}

	/**
	 *  Description of the Method
	 */
	public void stopMonitor() {
		for(Enumeration<BPM_Element> e = listModel.elements(); e.hasMoreElements(); ) {
			BPM_Element elm = e.nextElement();
			elm.stopMonitor();
		}
	}


	/**
	 *  Description of the Method
	 */
	public void removeAllElements() {
		for(Enumeration<BPM_Element> e = listModel.elements(); e.hasMoreElements(); ) {
			BPM_Element elm = e.nextElement();
			elm.stopMonitor();
		}
		listModel.removeAllElements();
	}

	//-----------------------------------------------
	//The TableModel interface implementation
	//-----------------------------------------------

	/**
	 *  Returns the columnClass attribute of the BPMsList object
	 *
	 *@param  columnIndex  The Parameter
	 *@return              The columnClass value
	 */
	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex == 0) {
			return String.class;
		}
		return Boolean.class;
	}

	/**
	 *  Returns the columnName attribute of the BPMsList object
	 *
	 *@param  column  The Parameter
	 *@return         The columnName value
	 */
	public String getColumnName(int column) {
		if(column == 0) {
			return "BPM";
		}
		return "Use";
	}

	/**
	 *  Returns the cellEditable attribute of the BPMsList object
	 *
	 *@param  rowIndex     The Parameter
	 *@param  columnIndex  The Parameter
	 *@return              The cellEditable value
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0) {
			return false;
		}
		return true;
	}

	/**
	 *  Returns the rowCount attribute of the BPMsList object
	 *
	 *@return    The rowCount value
	 */
	public int getRowCount() {
		return listModel.getSize();
	}

	/**
	 *  Returns the columnCount attribute of the BPMsList object
	 *
	 *@return    The columnCount value
	 */
	public int getColumnCount() {
		return 2;
	}

	/**
	 *  Returns the valueAt attribute of the BPMsList object
	 *
	 *@param  row     The Parameter
	 *@param  column  The Parameter
	 *@return         The valueAt value
	 */
	public Object getValueAt(int row, int column) {
		BPM_Element elm =  listModel.elementAt(row);
		if(column == 0) {
			return elm.getName();
		}
		return elm.isActiveObj();
	}


	/**
	 *  Sets the valueAt attribute of the BPMsList object
	 *
	 *@param  aValue  The new valueAt value
	 *@param  row     The new valueAt value
	 *@param  column  The new valueAt value
	 */
	public void setValueAt(Object aValue, int row, int column) {
		if(column == 1) {
			BPM_Element elm = listModel.elementAt(row);
			elm.setActive(!elm.isActive());
			fireTableCellUpdated(row, column);
		}
	}

}

