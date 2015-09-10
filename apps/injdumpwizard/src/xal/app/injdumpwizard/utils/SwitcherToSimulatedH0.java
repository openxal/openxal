package xal.app.injdumpwizard.utils;


import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;


import xal.ca.*;
import xal.extension.widgets.swing.*;
import xal.extension.scan.UpdatingEventController;
import xal.extension.scan.WrappedChannel;

/**
 *  This class switches to the simulated H0 beam mode 
 *  and back to the normal mode.
 *
 *@author     shishlo
*/
public class  SwitcherToSimulatedH0 {

	//JPanel with all GUI elements
	private JPanel switcherPanel = new JPanel(new BorderLayout());
	private TitledBorder switcherBorder = null;
	
	//the PV names to control
	Vector<WrappedChannel> wrpV = new Vector<WrappedChannel>();
	
	//controller for updating the live table
	UpdatingEventController updateCtrl = new UpdatingEventController();
	
	//table models
	private NameValueTableModel liveDataTableModel = new NameValueTableModel();
	private Vector<TableRecord> liveDataRecords = null;
	
	private NameValueTableModel memorizedDataTableModel = new NameValueTableModel();
	private Vector<TableRecord> memorizedRecords = null;	
	
	//JTables
	private JTable liveDataTable = null;
	private TitledBorder liveDataBorder = null;
	
	private JTable memorizedDataTable = null;	
	private TitledBorder memorizedDataBorder = null;
	
	//buttons for memorize and restore the values 
	private JButton memorizeButton = new JButton(" MEMORIZE VALUES ==> ");
	private JButton restoreButton = new JButton(" <== RESTORE  VALUES ");	
	
	//---------------------------------------
	//GUI elements for the control part
	//---------------------------------------
	
	private TitledBorder transformBorder = null;
	
  //transormation coefficients and labels
	private DoubleInputTextField injSpt_coeff_TextField = new DoubleInputTextField(10);	
	private DoubleInputTextField dha11_coeff_TextField = new DoubleInputTextField(10);
	private DoubleInputTextField dha12_coeff_TextField = new DoubleInputTextField(10);
	private JLabel injSpt_coeff_Label = new JLabel("Multiply InjSpt current by: ");
	private JLabel dha11_coeff_Label = new JLabel("Multiply DH_A11 current by: ");
	private JLabel dha12_coeff_Label = new JLabel("Set DH_A12 aurrent to [A]: ");	

	//perform transformation button
	private JButton transformButton = new JButton(" SET MAGNETS NOW! ");
	
	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();
	
	public SwitcherToSimulatedH0(){
		Border border = BorderFactory.createEtchedBorder();
		switcherBorder = BorderFactory.createTitledBorder(border, "Switcher to H0 Simmulation Mode");
		switcherPanel.setBorder(switcherBorder);

		//set the live table update time to the 0.5 sec
		updateCtrl.setUpdateTime(0.5);
		
		liveDataRecords = liveDataTableModel.getRecords();
		memorizedRecords = memorizedDataTableModel.getRecords();
		
		liveDataTable = new JTable(liveDataTableModel);
		memorizedDataTable = new JTable(memorizedDataTableModel);
		
		//set up panels
		
		//Upper panels
		JPanel upperPanel = new JPanel(new BorderLayout());
		
		JPanel upperInsidePanel = new JPanel(new GridLayout(1, 2, 1, 1));
		
		liveDataBorder = BorderFactory.createTitledBorder(border, "Live Data");
		memorizedDataBorder = BorderFactory.createTitledBorder(border, "Memorized Data");
		
		JPanel liveDataPanel = new JPanel(new BorderLayout());
		liveDataPanel.setBorder(liveDataBorder);
		JScrollPane scrollpane1 = new JScrollPane(liveDataTable);
		liveDataPanel.add(scrollpane1,BorderLayout.CENTER);
		
		JPanel memorizedDataPanel = new JPanel(new BorderLayout());
		memorizedDataPanel.setBorder(memorizedDataBorder);
		
		JPanel memButtonPanel = new JPanel(new GridLayout(2, 1, 1, 1));
		memButtonPanel.add(memorizeButton);
		memButtonPanel.add(restoreButton);
		
		JPanel memButton_0_Panel = new JPanel(new BorderLayout());
		memButton_0_Panel.add(memButtonPanel,BorderLayout.NORTH);
		JPanel memButton_1_Panel = new JPanel(new BorderLayout());
		memButton_1_Panel.add(memButton_0_Panel,BorderLayout.WEST);
		JPanel memButton_2_Panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
		memButton_2_Panel.add(memButton_1_Panel);
		
		memorizedDataPanel.add(memButton_2_Panel,BorderLayout.WEST);

		JScrollPane scrollpane2 = new JScrollPane(memorizedDataTable);
		memorizedDataPanel.add(scrollpane2,BorderLayout.CENTER);
		
		upperInsidePanel.add(liveDataPanel);
		upperInsidePanel.add(memorizedDataPanel);
		
		upperPanel.add(upperInsidePanel,BorderLayout.CENTER);
		
		//lower panel with transformation info and buttons
		
		JPanel lowerPanel = new JPanel(new BorderLayout());
		transformBorder = BorderFactory.createTitledBorder(border, "Magnet's Current Settings");
		lowerPanel.setBorder(transformBorder);
		
		JPanel coeffPanel = new JPanel(new GridLayout(3, 2, 1, 1));
		coeffPanel.add(injSpt_coeff_Label);
		coeffPanel.add(injSpt_coeff_TextField);
		coeffPanel.add(dha11_coeff_Label);
		coeffPanel.add(dha11_coeff_TextField);
		coeffPanel.add(dha12_coeff_Label);
		coeffPanel.add(dha12_coeff_TextField);
		
		JPanel lower_0_Panel = new JPanel(new BorderLayout());
		lower_0_Panel.add(coeffPanel,BorderLayout.CENTER);
		lower_0_Panel.add(transformButton,BorderLayout.SOUTH);
		
		lowerPanel.add(lower_0_Panel,BorderLayout.WEST);
		
		//final setup for main panel 
		switcherPanel.add(upperPanel,BorderLayout.NORTH);
		switcherPanel.add(lowerPanel,BorderLayout.CENTER);
		
		//set coefficients
		injSpt_coeff_TextField.setNumberFormat(new DecimalFormat("######.######"));
		dha11_coeff_TextField.setNumberFormat(new DecimalFormat("######.######"));
		dha12_coeff_TextField.setNumberFormat(new DecimalFormat("######.######"));
		
		injSpt_coeff_TextField.setValue(1.01474);
		dha11_coeff_TextField.setValue(0.742995);
		dha12_coeff_TextField.setValue(20.0);
		
		//set PVs
		for(int i = 0; i < 9; i++){
			wrpV.add(new WrappedChannel());
		}
		wrpV.get(0).setChannelNameQuietly("HEBT_Mag:PS_InjSptm:I_Set");
		wrpV.get(1).setChannelNameQuietly("Ring_Mag:PS_DH_A11:I_Set");
		wrpV.get(2).setChannelNameQuietly("Ring_Mag:PS_DH_A12:I_Set");
		wrpV.get(3).setChannelNameQuietly("HEBT_Mag:InjSptm:B");
		wrpV.get(4).setChannelNameQuietly("Ring_Mag:PS_DH_A11:B");
		wrpV.get(5).setChannelNameQuietly("Ring_Mag:PS_DH_A12:B");
		wrpV.get(6).setChannelNameQuietly("HEBT_Mag:PS_InjSptm:B_Book");
		wrpV.get(7).setChannelNameQuietly("Ring_Mag:PS_DH_A11:B_Book");
		wrpV.get(8).setChannelNameQuietly("Ring_Mag:PS_DH_A12:B_Book");
		
		Iterator<WrappedChannel> iter = wrpV.iterator();
		while(iter.hasNext()){
			WrappedChannel wch = iter.next();
			liveDataTableModel.addTableRecord(wch.getChannelName());
			memorizedDataTableModel.addTableRecord(wch.getChannelName());
		}		

		//set up listeners for PVs
		//all PVs act through the update controller - updateCtrl
		ActionListener pvListener = new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 updateCtrl.update();
			 }
		 };
		
		iter = wrpV.iterator();
		while(iter.hasNext()){
			WrappedChannel wch = iter.next();
			wch.addStateListener(pvListener);
			wch.addValueListener(pvListener);
		}
		
		ActionListener updateListener =new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Iterator<WrappedChannel> iter_inner = wrpV.iterator();
				while(iter_inner.hasNext()){
					WrappedChannel wch = iter_inner.next();
					TableRecord liveTR = liveDataTableModel.getRecord(wch.getChannelName());
					if(wch.isGood()){
						liveTR.setValue(wch.getValue());
						liveTR.setStatus(true);
					}
					else{
						liveTR.setValue(0.);
						liveTR.setStatus(false);
						messageTextLocal.setText("Cannot read PV: "+wch.getChannelName());
					}
				}
				liveDataTableModel.fireTableDataChanged();
			}
		};
	
		updateCtrl.addActionListener(updateListener);
		
		//----------------------------------
		//set up actions for buttons
		//----------------------------------
		
		//memorize button
	 memorizeButton.addActionListener(
		 new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				Iterator<WrappedChannel> iter_inner = wrpV.iterator();
				while(iter_inner.hasNext()){
					WrappedChannel wch = iter_inner.next();
					TableRecord memTR = memorizedDataTableModel.getRecord(wch.getChannelName());
					if(wch.isGood()){
						memTR.setValue(wch.getValue());
						memTR.setStatus(true);
					}
					else{
						memTR.setValue(0.);
						memTR.setStatus(false);
						messageTextLocal.setText("Cannot read PV: "+wch.getChannelName());
					}
				}
				memorizedDataTableModel.fireTableDataChanged();				 
			 }
		 });		
		
	 //restore from memory button
	 restoreButton.addActionListener(
		 new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 int[] inds = {6,7,8,0,1,2};
				 for(int i = 0; i < inds.length; i++){
					 int ind = inds[i];
					 WrappedChannel wch = wrpV.get(ind);
					 TableRecord memTR = memorizedDataTableModel.getRecord(wch.getChannelName());
					 if(memTR.getStatus()){
						 wch.setValue(memTR.getValue());
					 }
				 }
			 }
		 });
		
		 //restore from memory button
		 transformButton.addActionListener(
			 new ActionListener() {
				 public void actionPerformed(ActionEvent e) {
					 //---------------------------------------------
					 //Setting up the simulated H0 beam according to 
					 //Jeff Holmes's algorithm
					 //----------------------------------------------
					 //InjSpt set up
					 WrappedChannel wchI = wrpV.get(0);
					 WrappedChannel wchB = wrpV.get(3);
					 WrappedChannel wchB_Book = wrpV.get(6);
					 if(wchB.isGood()){
						 double coeff = wchB.getValue()/wchI.getValue();
						 double valI = wchI.getValue()*injSpt_coeff_TextField.getValue();
						 double valB_Book = coeff*valI;
						 wchI.setValue(valI);
						 wchB_Book.setValue(valB_Book);
					 } else {
						 messageTextLocal.setText("Cannot read PV: "+wchB.getChannelName());
					 }
					 //DH_11 set up
					 wchI = wrpV.get(1);
					 wchB = wrpV.get(4);
					 wchB_Book = wrpV.get(7);
					 if(wchB.isGood()){
						 double coeff = wchB.getValue()/wchI.getValue();
						 double valI = wchI.getValue()*dha11_coeff_TextField.getValue();
						 double valB_Book = coeff*valI;
						 wchI.setValue(valI);
						 wchB_Book.setValue(valB_Book);
					 } else {
						 messageTextLocal.setText("Cannot read PV: "+wchB.getChannelName());
					 }				 
					 //DH_12 set up
					 wchI = wrpV.get(2);
					 wchB = wrpV.get(5);
					 wchB_Book = wrpV.get(8);
					 if(wchB.isGood()){
						 double coeff = wchB.getValue()/wchI.getValue();
						 double valI = dha12_coeff_TextField.getValue();
						 double valB_Book = coeff*valI;
						 wchI.setValue(valI);
						 wchB_Book.setValue(valB_Book);
					 } else {
						 messageTextLocal.setText("Cannot read PV: "+wchB.getChannelName());
					 }				 
				 }
			 }); 
	}
	
	/**
	* Returns the panel with all GUI elements
	*/
	public JPanel getJPanel(){
		return switcherPanel;
	}
	
	/**
	* Starts monitors for all PVs. 
	*/	
  public void startMonitorPVs(){
		Iterator<WrappedChannel> iter = wrpV.iterator();
		while(iter.hasNext()){
			WrappedChannel wch = iter.next();
			wch.startMonitor();
		}
	}		
	
	/**
	* Connects the local text message field with the outside field
	*/
	public void setMessageText( JTextField messageTextLocal){
		this.messageTextLocal.setDocument(messageTextLocal.getDocument());
	}

	/**
	*  Sets the font for all GUI elements.
	*
	*@param  fnt  The new font
	*/
	public void setFontForAll(Font fnt) {
		switcherBorder.setTitleFont(fnt);
		liveDataBorder.setTitleFont(fnt);
		memorizedDataBorder.setTitleFont(fnt);
		transformBorder.setTitleFont(fnt);
		
		memorizeButton.setFont(fnt);
		restoreButton.setFont(fnt);
		transformButton.setFont(fnt);
		
		dha11_coeff_TextField.setFont(fnt);
		dha12_coeff_TextField.setFont(fnt);
		injSpt_coeff_TextField.setFont(fnt);
		
		dha11_coeff_Label.setFont(fnt);
		dha12_coeff_Label.setFont(fnt);
		injSpt_coeff_Label.setFont(fnt);
		
		liveDataTable.setFont(fnt);
		memorizedDataTable.setFont(fnt);
		liveDataTable.setRowHeight((int) (fnt.getSize()*1.2));
		memorizedDataTable.setRowHeight((int) (fnt.getSize()*1.2));
    liveDataTable.setPreferredScrollableViewportSize(new Dimension(1, 14*fnt.getSize()));
		memorizedDataTable.setPreferredScrollableViewportSize(new Dimension(1,14*fnt.getSize()));
		liveDataTable.getTableHeader().setFont(fnt);
		memorizedDataTable.getTableHeader().setFont(fnt);
		
	}	
	
	
	
}
