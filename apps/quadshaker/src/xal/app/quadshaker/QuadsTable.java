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
public class QuadsTable extends AbstractTableModel {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;

	private DefaultListModel<Quad_Element> listModel = new DefaultListModel<Quad_Element>();


	/**
	 *  Constructor for the QuadsTable object
	 */
	public QuadsTable() {

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
	 *  Returns the listModel attribute of the QuadsTable object
	 *
	 *@return    The listModel value
	 */
	public DefaultListModel<Quad_Element> getListModel() {
		return listModel;
	}


	/**
	 *  Description of the Method
	 */
	public void startMonitor() {
		for(Enumeration<Quad_Element> e = listModel.elements(); e.hasMoreElements(); ) {
			Quad_Element elm = e.nextElement();
			elm.startMonitor();
		}
	}

	/**
	 *  Description of the Method
	 */
	public void stopMonitor() {
		for(Enumeration<Quad_Element> e = listModel.elements(); e.hasMoreElements(); ) {
			Quad_Element elm = e.nextElement();
			elm.stopMonitor();
		}
	}


	/**
	 *  Description of the Method
	 */
	public void removeAllElements() {
		for(Enumeration<Quad_Element> e = listModel.elements(); e.hasMoreElements(); ) {
			Quad_Element elm = e.nextElement();
			elm.clear();
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
		} else if(columnIndex == 1) {
			return Boolean.class;
		}
		return Integer.class;
	}

	/**
	 *  Returns the columnName attribute of the BPMsList object
	 *
	 *@param  column  The Parameter
	 *@return         The columnName value
	 */
	public String getColumnName(int column) {
		if(column == 0) {
			return "Quad";
		} else if(column == 1) {
			return "Use";
		}
		return "#";
	}

	/**
	 *  Returns the cellEditable attribute of the BPMsList object
	 *
	 *@param  rowIndex     The Parameter
	 *@param  columnIndex  The Parameter
	 *@return              The cellEditable value
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 1) {
			return true;
		}
		return false;
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
		return 3;
	}

	/**
	 *  Returns the valueAt attribute of the BPMsList object
	 *
	 *@param  row     The Parameter
	 *@param  column  The Parameter
	 *@return         The valueAt value
	 */
	public Object getValueAt(int row, int column) {
		Quad_Element elm = listModel.elementAt(row);
		if(column == 0) {
			return elm.getName();
		} else if(column == 1) {
			return elm.isActiveObj();
		}
		return new Integer(row);
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
			Quad_Element elm =  listModel.elementAt(row);
			elm.setActive(!elm.isActive());
			fireTableCellUpdated(row, column);
		}
	}
}

