/*
* PVsPanel.java
*
*/

package xal.app.mtv;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.*;

import xal.ca.*;

/**
* The window representation / view of an xiodiag document
*
* @author  shishlo
*/
public class PVsPanel extends JPanel {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	
	private MTVDocument theDoc;

	protected ArrayList <PVTableCell> PVs = new ArrayList<PVTableCell>();
	
	private JTextField pvNameText = new JTextField ("===   Put new PV name here!   ===",50);
	private JButton addPVtoTableButton = new JButton("Add PV to Table");
	private JTable pvsTable;
	protected PVTableModel pvsTableModel;
	private JScrollPane tableScrollPane;
	protected javax.swing.Timer timer;
		
	/** Creates a new instance of MainWindow */
	public PVsPanel(MTVDocument aDocument) {
		super(new BorderLayout());
		theDoc = aDocument;

		JPanel pvNamePanel0 = new JPanel(new BorderLayout());
		JPanel pvNamePanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel pvNamePanel = new JPanel(new BorderLayout());
		
		pvNamePanel0.add(pvNameText,BorderLayout.CENTER);
		pvNamePanel1.add(addPVtoTableButton);
		pvNamePanel.add(pvNamePanel0,BorderLayout.NORTH);
		pvNamePanel.add(pvNamePanel1,BorderLayout.SOUTH);

		addPVtoTableButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					String chanName = pvNameText.getText();
					Channel chan = ChannelFactory.defaultFactory().getChannel(chanName);
					chan.connectAndWait();
					try{
						double[] valArr = chan.getArrDbl();
						PVTableCell cell = getNewCell(chan);
						PVs.add(cell);
						updatePVsTable();	
					}
					catch(Exception excp){
						pvNameText.setText(null);
						pvNameText.setText("Bad Channel Name. Try again!");
					}
				}
		});
		
		makePVsTable();
		pvsTable.setCellSelectionEnabled(true);
		tableScrollPane = new JScrollPane(pvsTable);
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				tableTask();
			}
		};	
		timer = new javax.swing.Timer(1500, taskPerformer);
		timer.start();	
		
		final TableCellRenderer defaultRenderer = pvsTable.getDefaultRenderer(String.class);
		TableCellRenderer newRenderer = new TableCellRenderer(){
			 public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				 boolean hasFocus, int row, int column){
			 			Component component = defaultRenderer.getTableCellRendererComponent(table,value,isSelected,
							hasFocus,row,column);
						if(column != 0){
							PVTableCell cell = (PVTableCell) pvsTableModel.getValueAt(row,column);
							if(cell.isValueChanged()){
								component.setForeground(Color.blue);
							}
							else{
								component.setForeground(Color.black);
							}
						}
						else{
							component.setForeground(Color.black);
						}
						return component;
				 }
		};
		pvsTable.setDefaultRenderer(String.class,newRenderer); 
		pvsTable.setDefaultRenderer(PVTableCell.class,newRenderer);
		
		//make all JPanels
		add(pvNamePanel,BorderLayout.NORTH);
		add(tableScrollPane,BorderLayout.CENTER);		
	}
	
	/** the action to perform when the tables need updating */
	protected void tableTask() {
		pvsTableModel.fireTableRowsUpdated(0, pvsTableModel.getRowCount());
	}

	/** returns the number of PVs */
	public int getCellsNumber(){
		return PVs.size();
	}
		
	/** returns the PVTableCell at a certain position */
	public PVTableCell getCell(int row){
		return PVs.get(row);
	}
	
	/** update the magnet table for the selected types */
	private void makePVsTable() {
		pvsTableModel = new PVTableModel(this);
		pvsTable = new JTable(pvsTableModel);
		pvsTable.addMouseListener( new MouseAdapter() {
				public void mouseClicked( final MouseEvent event ) {
					int col = pvsTable.columnAtPoint(event.getPoint());
					int row = pvsTable.getSelectedRow();
					//System.out.println("row = " + row + " col = " + col);
					if(col == 1) {
						theDoc.myWindow().wheelPanel.setPVTableCell(PVs.get(row));
						return;
					}
					theDoc.myWindow().wheelPanel.setPVTableCell(null);
				}
		});	    
	}
	
	/** update the magnet table based on the selected magnet types */
	protected void updatePVsTable() {
		pvsTableModel.fireTableDataChanged();
	}

	
	private PVTableCell getNewCell(Channel chan){
		String pvName = chan.getId();
		Hashtable<String,PVTableCell> hash = theDoc.getCellHashtable();
		if(hash.containsKey(pvName)){
			return hash.get(pvName);
		}
		PVTableCell newCell = new PVTableCell(pvName);
		hash.put(pvName,newCell);
		return newCell;
	}	
}
