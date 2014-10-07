/*************************************************************
//
// class TuneFace:
// This class is responsible for the Graphic User Interface
// components and action listeners for the tune setting tab.
//
/*************************************************************/

package xal.app.ringinjection;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;

import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import java.io.*;
import java.lang.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import xal.extension.widgets.swing.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.messaging.*;
import xal.ca.*;
import xal.tools.data.*;
import java.text.NumberFormat;


public class BPMFace extends JPanel{
	/** serial version required by Serializable */
	private static final long serialVersionUID = 1L;

    public JPanel mainPanel;
    public JTable bpmtable;
    public JTable resultstable;
    public JTabbedPane masterPane;
    public JPanel resultsPanel;
    private NumberFormat tuneFor;
    private NumberFormat pointsFor;
    private NumberFormat avgFor;
    
    private JComboBox<String> syncstate;
    private String[] syncstates = {"Model Live Lattice", "Model Design Lattice"};
    protected MessageCenter messageCenter = new MessageCenter("Injection Spot Message");
    public String latticestate = "Design";
    
    public void addInjSpotListener(InjSpotListener listener) {
    messageCenter.registerTarget(listener, this, InjSpotListener.class);
    }
    
    protected InjSpotListener injectionProxy;
    
    JButton calcbutton;
    JButton avgbutton;
    JButton storebutton;
    JLabel qxlabel = new JLabel("Fractional X Tune:");
    JLabel qylabel = new JLabel("Fractional Y Tune:");
    JLabel initialguess = new JLabel("----Initial Fit Parameters----");
    JLabel pointslabel = new JLabel("Number of Points to Fit:");
    JLabel xlabel = new JLabel("x (mm): ");
    JLabel xplabel = new JLabel("x' (mrad): ");
    JLabel ylabel = new JLabel("y (mm): ");
    JLabel yplabel = new JLabel("y' (mrad): "); 
    JLabel[] pmlabel = new JLabel[4];
    JScrollPane bpmscrollpane;
    JScrollPane resultsscrollpane;
    Color mycolor = new Color(190,190,220);
    
    ResultsPlot resultsplot = new ResultsPlot();
    CalculateFit bpmfit; 
    BPMTableModel bpmtablemodel;
    ResultsTableModel resultstablemodel;
    EdgeLayout layout = new EdgeLayout();
    ArrayList<BpmAgent> activebpmagents = new ArrayList<>();

    DecimalField points = new DecimalField();
    DecimalField x = new DecimalField();
    DecimalField xp = new DecimalField();
    DecimalField y = new DecimalField();
    DecimalField yp = new DecimalField();
    DecimalField xerr = new DecimalField();
    DecimalField xperr = new DecimalField();
    DecimalField yerr = new DecimalField();
    DecimalField yperr = new DecimalField();    
    GenDocument doc; 
    
    int i;
    double inj_params[] = new double[4];

    //Member function Constructor
 
    public BPMFace(GenDocument aDocument, JPanel masterPanel){
	
	doc=aDocument;
	
	injectionProxy=(InjSpotListener)messageCenter.registerSource(this, InjSpotListener.class);
	
	setPreferredSize(new Dimension(950,500));
	setLayout(layout);
	
	makeComponents(); //Creation of all GUI components
	addComponents();  //Add all components to the layout and panels
	setAction();      //Set the action listeners
    }

    public void addComponents(){
	
	layout.setConstraints(mainPanel, 0, 0, 0, 0, EdgeLayout.ALL_SIDES, EdgeLayout.GROW_BOTH);
	this.add(mainPanel);
	
	EdgeLayout newlayout = new EdgeLayout();
	mainPanel.setLayout(newlayout);
	newlayout.setConstraints(bpmscrollpane, 10, 15, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(bpmscrollpane);
	newlayout.setConstraints(resultsscrollpane, 10, 245, 20, 5, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(resultsscrollpane);	
	newlayout.setConstraints(resultsplot, 120, 260, 100, 50, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(resultsplot);
	newlayout.setConstraints(pointslabel, 350, 30, 10, 450, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(pointslabel);
	newlayout.setConstraints(points, 340, 190, 10, 450, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(points);
	newlayout.setConstraints(syncstate, 380, 30, 170, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(syncstate);
	newlayout.setConstraints(calcbutton, 420, 30, 170, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(calcbutton);
	
	
	GridLayout resultsgrid = new GridLayout(4, 4);
	resultsPanel.setLayout(resultsgrid);
	resultsPanel.add(xlabel); resultsPanel.add(x); resultsPanel.add(pmlabel[0]); resultsPanel.add(xerr);
	resultsPanel.add(xplabel); resultsPanel.add(xp); resultsPanel.add(pmlabel[1]); resultsPanel.add(xperr);
	resultsPanel.add(ylabel); resultsPanel.add(y); resultsPanel.add(pmlabel[2]); resultsPanel.add(yerr);
	resultsPanel.add(yplabel); resultsPanel.add(yp); resultsPanel.add(pmlabel[3]); resultsPanel.add(yperr);
	
	newlayout.setConstraints(avgbutton, 180, 730, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(avgbutton);
	newlayout.setConstraints(resultsPanel, 210, 680, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(resultsPanel);
	newlayout.setConstraints(storebutton, 335, 745, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(storebutton);
	
	
	//this.add(bpmscrollpane);
    }

    public void makeComponents(){
	mainPanel = new JPanel();
	mainPanel.setPreferredSize(new Dimension(950,500));
	mainPanel.setVisible(true);

	resultsPanel = new JPanel();
	resultsPanel.setPreferredSize(new Dimension(260,110));
	resultsPanel.setBorder(BorderFactory.createTitledBorder("Averaged Results (w.r.t. closed orbit)"));

	makeBPMTable();
	makeResultsTable();
	
	bpmfit = new CalculateFit(doc,latticestate);
		
	syncstate = new JComboBox<>(syncstates);
	syncstate.setSelectedIndex(1);

	calcbutton = new JButton("Calculate Fits");
	calcbutton.setBackground(mycolor);
	avgbutton = new JButton("Calculate Average");
	avgbutton.setBackground(mycolor);
	storebutton = new JButton("Store Results");
	storebutton.setBackground(mycolor);
	tuneFor = NumberFormat.getNumberInstance();
	tuneFor.setMinimumFractionDigits(2);
	pointsFor = NumberFormat.getNumberInstance();
	pointsFor.setMaximumFractionDigits(0);
	avgFor = NumberFormat.getNumberInstance();
	avgFor.setMaximumFractionDigits(2);
	avgFor.setMaximumFractionDigits(3);
	
	pmlabel[0] = new JLabel("   +/-"); pmlabel[1] = new JLabel("   +/-");
	pmlabel[2] = new JLabel("   +/-"); pmlabel[3] = new JLabel("   +/-");
	
	points = new DecimalField(40, 2, pointsFor);
	x = new DecimalField(0.0, 6, avgFor);
	xp = new DecimalField(0.0, 6, avgFor);
	y = new DecimalField(0.0, 6, avgFor);
	yp = new DecimalField(0.0, 6, avgFor);
	xerr = new DecimalField(0.0, 6, avgFor);
	xperr = new DecimalField(0.0, 6, avgFor);
	yerr = new DecimalField(0.0, 6, avgFor);
	yperr = new DecimalField(0.0, 6, avgFor);
    } 

    public void setAction(){
	
	syncstate.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		if(syncstate.getSelectedIndex() == 0){
		   latticestate="Live";
		   bpmfit.changeSyncState(latticestate);
		}
		if(syncstate.getSelectedIndex() == 1){
		   latticestate="Design";
		   bpmfit.changeSyncState(latticestate);
		}
	    }
	});  
	
	//Does the fit for all seleced BPMs
	calcbutton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent ae) {
		int i = 0;
		activebpmagents.clear();
		Iterator itr = (doc.bpmagents).iterator();
		
		while(itr.hasNext() && i <= bpmtable.getRowCount()-1){
		    
		    BpmAgent bpmagent = (BpmAgent)itr.next();   
		    if(((Boolean)bpmtable.getValueAt(i, 1)).booleanValue() == true){
			if(bpmagent.isConnected()){
			    activebpmagents.add(bpmagent);
			    if( points.getDoubleValue() > 50 ) points.setValue(50);
			    bpmfit.bpmXFit(bpmagent, (int)points.getValue());
			    bpmfit.bpmYFit(bpmagent, (int)points.getValue());
			}
		    }
		    i++;
		}
		//Make the results table.
		Object[][] tabledata = new Object[activebpmagents.size()][9];

		double xparams[];
		double yparams[];
		DecimalFormat decfor =  new DecimalFormat("###.000");
		
		int count = activebpmagents.size();
		for(int k=0; k < count; k++){
		    
		    BpmAgent bpmagent = (BpmAgent)activebpmagents.get(k); 
		    xparams = bpmagent.getXFoilResults();
		    yparams = bpmagent.getYFoilResults();

		    System.out.println("xparams = " + xparams[0] + " " + xparams[1] + " " + xparams[2] + " "+ xparams[3]);
		    System.out.println("yparams = " + yparams[0] + " " + yparams[1] + " " + yparams[2] + " "+ yparams[3]);
		    tabledata[k][0]=bpmagent.name();

		    String xval = new String(decfor.format(xparams[0]) + " +- " + decfor.format(xparams[1]));
		    tabledata[k][1] = new String(xval);
		    String xpval = new String(decfor.format(xparams[2]) + " +- " + decfor.format(xparams[3]));
		    tabledata[k][2] = new String(xpval);
		    String yval = new String(decfor.format(yparams[0]) + " +- " + decfor.format(yparams[1]));
		    tabledata[k][3] = new String(yval);
		    String ypval = new String(decfor.format(yparams[2]) + " +- " + decfor.format(yparams[3]));
		    tabledata[k][4] = new String(ypval);
		    tabledata[k][5] = new Boolean(false);
		}		resultstablemodel.setTableData(activebpmagents.size(),tabledata,activebpmagents);
	    }
	});
	
	//Gets the average results for accepted individual BPM results
	avgbutton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		int i=0, count=0;
		double sum_x=0.0, sum_xp=0.0, sum_y=0.0, sum_yp=0.0;
		double avg_x=0.0, avg_xp=0.0, avg_y=0.0, avg_yp=0.0;
		double sum_xerr=0.0, sum_xperr=0.0, sum_yerr=0.0, sum_yperr=0.0;
		double avg_xerr=0.0, avg_xperr=0.0, avg_yerr=0.0, avg_yperr=0.0;
		Iterator itr = (doc.bpmagents).iterator();
		
		while(itr.hasNext() && i <= resultstable.getRowCount()-1){
		    BpmAgent bpmagent = (BpmAgent)itr.next();   
		    if(((Boolean)resultstable.getValueAt(i, resultstable.getColumnCount()-2)).booleanValue() == true){
			String[] xstring = (new String((String)resultstable.getValueAt(i,1))).split("\\s");
			String[] xpstring = (new String((String)resultstable.getValueAt(i,2))).split("\\s");
			String[] ystring = (new String((String)resultstable.getValueAt(i,3))).split("\\s");
			String[] ypstring = (new String((String)resultstable.getValueAt(i,4))).split("\\s");
			sum_x += (new Double(Double.parseDouble(xstring[0]))).doubleValue();
			sum_xp += (new Double(Double.parseDouble(xpstring[0]))).doubleValue();
			sum_y += (new Double(Double.parseDouble(ystring[0]))).doubleValue();
			sum_yp += (new Double(Double.parseDouble(ypstring[0]))).doubleValue();
			sum_xerr += Math.pow((new Double(Double.parseDouble(xstring[2]))).doubleValue(), 2);
			sum_xperr += Math.pow((new Double(Double.parseDouble(xpstring[2]))).doubleValue(), 2);
			sum_yerr += Math.pow((new Double(Double.parseDouble(ystring[2]))).doubleValue(), 2);
			sum_yperr += Math.pow((new Double(Double.parseDouble(ypstring[2]))).doubleValue(), 2);
			count++;
		    }
		    i++;
		}
		if(count != 0){
		    avg_x=sum_x/((new Integer(count)).doubleValue());
		    avg_xp=sum_xp/((new Integer(count)).doubleValue());
		    avg_y=sum_y/((new Integer(count)).doubleValue());
		    avg_yp=sum_yp/((new Integer(count)).doubleValue());
		    avg_xerr=Math.sqrt(sum_xerr)/((new Integer(count)).doubleValue());
		    avg_xperr=Math.sqrt(sum_xperr)/((new Integer(count)).doubleValue());
		    avg_yerr=Math.sqrt(sum_yerr)/((new Integer(count)).doubleValue());
		    avg_yperr=Math.sqrt(sum_yperr)/((new Integer(count)).doubleValue());
		}
		
		x.setValue(avg_x);
		xp.setValue(avg_xp);
		y.setValue(avg_y);
		yp.setValue(avg_yp);
		xerr.setValue(avg_xerr);
		xperr.setValue(avg_xperr);
		yerr.setValue(avg_yerr);
		yperr.setValue(avg_yperr);
		inj_params[0]=avg_x;
		inj_params[1]=avg_xp;
		inj_params[2]=avg_y;
		inj_params[3]=avg_yp;
	    }	
	});
	
	//Stores the average results as the current accepted result
	storebutton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		inj_params[0]=x.getDoubleValue();
		inj_params[1]=xp.getDoubleValue();
		inj_params[2]=y.getDoubleValue();
		inj_params[3]=yp.getDoubleValue();
		doc.setInjSpot(inj_params);	
		injectionProxy.updateInjSpot(inj_params);
	    }
	});
    }
    
    
    public void makeBPMTable(){
	String[] colnames = {"Device", "Select"};
	bpmtablemodel = new BPMTableModel(colnames, doc.nbpmagents);
	
	bpmtable = new JTable(bpmtablemodel);
	bpmtable.getColumnModel().getColumn(0).setPreferredWidth(150);
	bpmtable.getColumnModel().getColumn(1).setPreferredWidth(70);
	bpmtable.setPreferredScrollableViewportSize(bpmtable.getPreferredSize());
	bpmtable.setRowSelectionAllowed(false);
	bpmtable.setColumnSelectionAllowed(false);
	bpmtable.setCellSelectionEnabled(false);
	
	bpmscrollpane = new JScrollPane(bpmtable);
	bpmscrollpane.getVerticalScrollBar().setValue(0);
	bpmscrollpane.setPreferredSize(new Dimension(225, 300));
	
	//Set some initial values.
	Iterator<BpmAgent> itr = (doc.bpmagents).iterator();
	int i =0;
	boolean initial = true;
	while(itr.hasNext()){
	    BpmAgent bpmagent = (BpmAgent)itr.next();   
	    bpmtablemodel.setValueAt(bpmagent.name(), i, 0);
	    bpmtablemodel.setValueAt(new Boolean(false), i, 1);
	    i++;
	}
	bpmtablemodel.fireTableDataChanged();
    }
    
    
    public void makeResultsTable(){
	String[] colnames = {"Device", "X (mm)", "X' (mrad)", "Y (mm)", "Y' (mrad)", "Accept", "Plot"};

	resultstablemodel = new ResultsTableModel(colnames, activebpmagents.size());
	
	resultstable = new JTable(resultstablemodel);
	resultstable.getColumnModel().getColumn(0).setMinWidth(128);
	resultstable.getColumnModel().getColumn(1).setMinWidth(115);
	resultstable.getColumnModel().getColumn(2).setMinWidth(115);
	resultstable.getColumnModel().getColumn(3).setMinWidth(115);
	resultstable.getColumnModel().getColumn(4).setMinWidth(115);
	resultstable.getColumnModel().getColumn(5).setMaxWidth(45);
	resultstable.getColumnModel().getColumn(6).setMaxWidth(60);
	resultstable.setPreferredScrollableViewportSize(resultstable.getPreferredSize());
	resultstable.setRowSelectionAllowed(false);
	resultstable.setColumnSelectionAllowed(false);
	resultstable.setCellSelectionEnabled(true);

	resultsscrollpane = new JScrollPane(resultstable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	resultsscrollpane.setColumnHeaderView(resultstable.getTableHeader());
	resultstable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	resultsscrollpane.setPreferredSize(new Dimension(700, 85));
	
	ButtonRenderer buttonRenderer = new ButtonRenderer();
	resultstable.getColumnModel().getColumn(6).setCellRenderer(buttonRenderer);
	resultstable.getColumnModel().getColumn(6).setCellEditor(buttonRenderer);

    }
    
    //Renderer for doing the last column of the results table
	class ButtonRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor,ActionListener{
		/** serial version required by Serializable */
		private static final long serialVersionUID = 1L;

		public JButton theButton;
		protected static final String EDIT = "edit";

		public ButtonRenderer(){
			theButton = new JButton("plot");
			theButton.setActionCommand(EDIT);
			theButton.setEnabled(true);
			theButton.addActionListener(this);
		}

		public Component getTableCellEditorComponent(JTable table,
													 Object agent, boolean isSelected, int row, int column){
			BpmAgent bpmagent = (BpmAgent)agent;
			resultsplot.refreshHPlot(bpmagent);
			resultsplot.refreshVPlot(bpmagent);
			return theButton;

		}

		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {
				fireEditingStopped(); //Make the renderer reappear.
			}
		}

		public Object getCellEditorValue(){
			return "";
		}

		public boolean isCellEditable(){
			return true;
		}

		public Component getTableCellRendererComponent(JTable table,
													   Object value,
													   boolean isSelected,
													   boolean hasFocus,
													   int row,
													   int column){
			return theButton;
		}
		
	}
    
    
}








