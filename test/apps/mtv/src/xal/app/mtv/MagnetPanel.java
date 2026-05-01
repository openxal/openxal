/*
* MyWindow.java
*
* Created on April 14, 2003, 10:25 AM
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
* @author  jdg
*/
public class MagnetPanel extends JPanel implements ItemListener {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	
	private MTVDocument theDoc;
	
	private ArrayList <String> magTypes, selectedMagTypes;
	private ArrayList <JCheckBox> magCheckBoxes;
	
	protected ArrayList <String> magnetNames;
	protected ArrayList <PVTableCell> B_Sets, B_RBs, B_Trim_Sets, B_Books;
	
	private JPanel magCheckBoxPanel;
	private JButton updateTableButton;
	private JTable magnetTable;
	protected MagnetTableModel magnetTableModel;
	private JScrollPane tableScrollPane;
	protected javax.swing.Timer timer;
		
	/** Creates a new instance of MainWindow */
	public MagnetPanel(MTVDocument aDocument) {
		super(new BorderLayout());
		theDoc = aDocument;
		selectedMagTypes = new ArrayList<String>();
		magTypes = new ArrayList<String>();
		magCheckBoxes = new ArrayList<JCheckBox>();
		magnetNames = new ArrayList<String>();
		B_Sets = new ArrayList<PVTableCell> ();
		B_RBs = new ArrayList<PVTableCell> ();
		B_Trim_Sets = new ArrayList<PVTableCell> ();
		B_Books = new ArrayList<PVTableCell> ();	
		magCheckBoxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		updateTableButton = new JButton("Make Table");
		updateTableButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					updateMagnetTable();		
				}
		});
		makeMagnetTable();
		magnetTable.setCellSelectionEnabled(true);
		tableScrollPane = new JScrollPane(magnetTable);
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				tableTask();
			}
		};	
		timer = new javax.swing.Timer(1500, taskPerformer);
		timer.start();	
		
		final TableCellRenderer defaultRenderer = magnetTable.getDefaultRenderer(String.class);
		TableCellRenderer newRenderer = new TableCellRenderer(){
			 public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				 boolean hasFocus, int row, int column){
			 			Component component = defaultRenderer.getTableCellRendererComponent(table,value,isSelected,
							hasFocus,row,column);
						if(column != 0){
							PVTableCell cell = (PVTableCell) magnetTableModel.getValueAt(row,column);
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
		magnetTable.setDefaultRenderer(String.class,newRenderer); 
		magnetTable.setDefaultRenderer(PVTableCell.class,newRenderer);
		
		//make all JPanels
		JPanel commandPanel = new JPanel(new GridLayout(2,1));
		JPanel tableButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		tableButtonPanel.add(updateTableButton);
		commandPanel.add(magCheckBoxPanel);
		commandPanel.add(tableButtonPanel);
		add(commandPanel,BorderLayout.NORTH);
		add(tableScrollPane,BorderLayout.CENTER);		
	}
	
	public ArrayList <String> getSelectedTypes() { return selectedMagTypes;}
	
	/** the action to perform when the tables need updating */
	protected void tableTask() {
		magnetTableModel.fireTableRowsUpdated(0, magnetTableModel.getRowCount());
	}
	
	/** find the magnet types in the selcted accelerator sequence */
	protected void updateMagnetTypes() {
		magTypes.clear();
		selectedMagTypes.clear();
		magCheckBoxes.clear();
		magTypes.add("all");
		JCheckBox magBox = new JCheckBox("magnet");
		magBox.addItemListener(this);
		magCheckBoxes.add(magBox);
		
		java.util.List <AcceleratorNode> magNodes = theDoc.getSelectedSequence().getNodesOfType("magnet");
		for (AcceleratorNode mag : magNodes) {
			String type = mag.getType();
			if(!magTypes.contains(type)){
				magTypes.add(type);
				magBox = new JCheckBox(type);
				magBox.setSelected(false);
				magBox.addItemListener(this);
				magCheckBoxes.add(magBox);
				//System.out.println(type);
			}
		}	    
	}
	
	public void itemStateChanged(ItemEvent e) {
		JCheckBox source = (JCheckBox) e.getItemSelectable();
		String type = source.getText();
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			int index = selectedMagTypes.indexOf(type);
			if (index >= 0)
				selectedMagTypes.remove(index);
		}
		if (e.getStateChange() == ItemEvent.SELECTED) {
			int index = selectedMagTypes.indexOf(type);
			if (index < 0)
				selectedMagTypes.add(type);
		}
	}
	
	/** update the magnet table with type options for the selected sequence */ 
	protected void updateMagnetPanel(){
		magCheckBoxPanel.removeAll();
		for (JCheckBox box : magCheckBoxes) {
			magCheckBoxPanel.add(box);
		}
    this.validate();
	}
	/** update the magnet table for the selected types */
	private void makeMagnetTable() {
		magnetTableModel = new MagnetTableModel(this);
		magnetTable = new JTable(magnetTableModel);
		//magnetTable.setDefaultRenderer(Integer.class,  new IntegerRenderer());
		magnetTable.setRowSelectionAllowed(true);
		magnetTable.addMouseListener( new MouseAdapter() {
				public void mouseClicked( final MouseEvent event ) {
					int col = magnetTable.columnAtPoint(event.getPoint());
					int row = magnetTable.getSelectedRow();
					//System.out.println("row = " + row + " col = " + col);
					if(col == 1) {
						theDoc.myWindow().wheelPanel.setPVTableCell(B_Sets.get(row));
						return;
					}
					if(col == 2) {
						theDoc.myWindow().wheelPanel.setPVTableCell(B_Trim_Sets.get(row));
						return;
					}
					if(col == 4) {
						theDoc.myWindow().wheelPanel.setPVTableCell(B_Books.get(row));
						return;
					}
					theDoc.myWindow().wheelPanel.setPVTableCell(null);
				}
		});	    
	}
	
	/** update the magnet table based on the selected magnet types */
	protected void updateMagnetTable() {
		magnetNames.clear();
		B_Sets.clear();
		B_RBs.clear();
		B_Trim_Sets.clear();
		B_Books.clear();
		
		java.util.List <AcceleratorNode> nodes = theDoc.getSelectedSequence().getAllNodes();
		for (AcceleratorNode node : nodes) {
			boolean useIt = false;
			for (String type : selectedMagTypes) {
				if (node.isKindOf(type)) {
					useIt = true;
					break;
				}
			}
			if(!node.getStatus()){
				useIt = false;
			}
			if(useIt && !node.isKindOf("pmag")) {
				magnetNames.add(node.getId());
				Channel bRB = ((Electromagnet) node).getChannel( Electromagnet.FIELD_RB_HANDLE);
				MagnetMainSupply mms = ((Electromagnet) node).getMainSupply();
				Channel bSet = mms.getChannel(MagnetMainSupply.FIELD_SET_HANDLE);
				PVTableCell pvrb = null;
				if(bRB != null) 
					pvrb = getNewCell(bRB);
				else 
					pvrb = getNewCell();
				B_RBs.add(pvrb);
				
				PVTableCell pvsp = null;
				if(bSet != null)
					pvsp = getNewCell(bSet);
				else 
					pvsp = getNewCell();
				B_Sets.add(pvsp);
				
				PVTableCell pvBook = null;
				try {
					Channel bBookSet = mms.getChannel(MagnetMainSupply.FIELD_BOOK_HANDLE);
					pvBook = getNewCell(bBookSet);
					if(pvsp.getChannel() != null){
						pvsp.setBBookCell(pvBook);
					}
				}
				catch (Exception ex) {
					pvBook = getNewCell();
				}
				B_Books.add(pvBook);
				
				PVTableCell pvtsp;
				if(node.isKindOf("trimmedquad")) {
					MagnetTrimSupply mts = ((TrimmedQuadrupole) node).getTrimSupply();
					Channel btSet = mts.getChannel(MagnetTrimSupply.FIELD_SET_HANDLE);
					if(btSet != null)
						pvtsp = getNewCell(btSet);
					else 
						pvtsp = getNewCell();
					B_Trim_Sets.add(pvtsp);
				}
				else {
					pvtsp = getNewCell();
					B_Trim_Sets.add(pvtsp);
				}
			}
		}
		magnetTableModel.fireTableDataChanged();
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
	
	private PVTableCell getNewCell(){
		String pvName = "null";
		Hashtable<String,PVTableCell> hash = theDoc.getCellHashtable();
		if(hash.containsKey(pvName)){
			return hash.get(pvName);
		}
		PVTableCell newCell = new PVTableCell();
		hash.put(pvName,newCell);
		return newCell;
	}
	
}
