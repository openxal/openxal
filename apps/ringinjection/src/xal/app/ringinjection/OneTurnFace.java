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

public class OneTurnFace extends JPanel{
	/** serial version required by Serializable */
	private static final long serialVersionUID = 1L;

    public JPanel mainPanel;
    public JTable bpmtable;
    public JTable resultstable;
    public JTabbedPane masterPane;
    public JPanel initialPanel;
    public JPanel resultsPanel;
    private NumberFormat tuneFor;
    private NumberFormat pointsFor;
    private NumberFormat avgFor;
    private HashMap closedorbitmap;
    
    protected MessageCenter messageCenter = new MessageCenter("Injection Spot Message");
	
    public void addInjSpotListener(InjSpotListener listener) {
    messageCenter.registerTarget(listener, this, InjSpotListener.class);
    }
   
    protected InjSpotListener injectionProxy;  
    
    JButton calcbutton;
    JButton storebutton;
    JButton loadbutton;
    JButton closedorbitbutton;
    JLabel alabel = new JLabel("Amplitude:");
    JLabel philabel = new JLabel("Phase Offset:");
    JLabel initialguess = new JLabel("----Initial Fit Parameters----");
    JLabel pointslabel = new JLabel("Number of Points to Fit:");
    JLabel xlabel = new JLabel(" x (mm): ");
    JLabel xplabel = new JLabel(" x' (mrad): ");
    JLabel ylabel = new JLabel(" y (mm): ");
    JLabel yplabel = new JLabel(" y' (mrad):"); 
    JLabel[] errorlabel = new JLabel[4];
    JScrollPane bpmscrollpane;
    Color mycolor = new Color(190,190,220);
    
    //OneTurnResultsPlot resultsplot = new OneTurnResultsPlot();
    OneTurnFit fit;
    //CalculateFit bpmfit = new CalculateFit();
    BPMTableModel bpmtablemodel;
    //ResultsTableModel resultstablemodel;
    EdgeLayout layout = new EdgeLayout();
    ArrayList activebpmagents = new ArrayList();
    ArrayList plotbpmagents = new ArrayList();
    ArrayList acceptedbpmagents = new ArrayList();

    DecimalField a = new DecimalField();
    DecimalField phi = new DecimalField();
 
    DecimalField x = new DecimalField();
    DecimalField xerror = new DecimalField();
    DecimalField xp = new DecimalField();
    DecimalField xperror = new DecimalField();
    DecimalField y = new DecimalField();
    DecimalField yerror = new DecimalField();
    DecimalField yp = new DecimalField();
    DecimalField yperror = new DecimalField();
    GenDocument doc; 
    
    int i;
    double inj_params[] = new double[4];

    //Member function Constructor
 
    public OneTurnFace(GenDocument aDocument, JPanel masterPanel){
	
	doc=aDocument;
	
	injectionProxy=(InjSpotListener)messageCenter.registerSource(this, InjSpotListener.class);
	
	setPreferredSize(new Dimension(900,500));
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
	newlayout.setConstraints(loadbutton, 325, 100, 100, 50, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(loadbutton);
	newlayout.setConstraints(fit, 0, 430, 150, 50, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(fit);

	/*
	GridBagLayout initialgrid = new GridBagLayout();
	initialPanel.setLayout(initialgrid);
	GridBagConstraints b = new GridBagConstraints();
	b.gridwidth=1; 
	b.gridx=0; b.gridy=1;
	b.insets=new Insets(10,2,0,2);
	initialgrid.setConstraints(alabel, b);
	initialPanel.add(alabel);
	b.gridx=1; b.gridy=1;
	initialgrid.setConstraints(a, b);
	initialPanel.add(a);
	b.gridx=0; b.gridy=2;
	initialgrid.setConstraints(philabel, b);
	initialPanel.add(philabel);
	b.gridx=1; b.gridy=2;
	initialgrid.setConstraints(phi, b);
	initialPanel.add(phi);

	b.gridx=0; b.gridy=3;
	b.gridwidth=2;
	//b.insets=new Insets(20,2,0,2);
	initialgrid.setConstraints(calcbutton, b);
	initialPanel.add(calcbutton);
	newlayout.setConstraints(initialPanel, 355, 75, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(initialPanel);
	*/
	
	//newlayout.setConstraints(closedorbitbutton, 375, 100, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	//mainPanel.add(closedorbitbutton);
	
	newlayout.setConstraints(calcbutton, 375, 100, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(calcbutton);
	
	GridLayout resultsgrid = new GridLayout(4, 4);
	resultsPanel.setLayout(resultsgrid);
	resultsPanel.add(xlabel); resultsPanel.add(x); resultsPanel.add(errorlabel[0]); resultsPanel.add(xerror);
	resultsPanel.add(xplabel); resultsPanel.add(xp); resultsPanel.add(errorlabel[1]); resultsPanel.add(xperror);
	resultsPanel.add(ylabel); resultsPanel.add(y); resultsPanel.add(errorlabel[2]); resultsPanel.add(yerror);
	resultsPanel.add(yplabel); resultsPanel.add(yp); resultsPanel.add(errorlabel[3]); resultsPanel.add(yperror);
	
	newlayout.setConstraints(resultsPanel, 370, 520, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(resultsPanel);
	newlayout.setConstraints(storebutton, 465, 620, 50, 10, EdgeLayout.LEFT,
	EdgeLayout.NO_GROWTH);
	mainPanel.add(storebutton);

    }

    public void makeComponents(){
	mainPanel = new JPanel();
	mainPanel.setPreferredSize(new Dimension(900,500));
	mainPanel.setVisible(true);

	resultsPanel = new JPanel();
	resultsPanel.setPreferredSize(new Dimension(300,90));
	resultsPanel.setBorder(BorderFactory.createTitledBorder("Results (w.r.t. closed orbit)"));
	
	initialPanel = new JPanel();
	initialPanel.setPreferredSize(new Dimension(210,120));
	initialPanel.setBorder(BorderFactory.createTitledBorder("Initial Guess"));
	
	makeBPMTable();
	
	calcbutton = new JButton("Calculate");
	calcbutton.setBackground(mycolor);
	storebutton = new JButton("Store Results");
	storebutton.setBackground(mycolor);
	loadbutton = new JButton("Load New Table Data");
	loadbutton.setBackground(mycolor);
	closedorbitbutton = new JButton("Reload Closed Orbit");
	closedorbitbutton.setBackground(mycolor);
	tuneFor = NumberFormat.getNumberInstance();
	tuneFor.setMinimumFractionDigits(2);
	pointsFor = NumberFormat.getNumberInstance();
	pointsFor.setMaximumFractionDigits(0);
	avgFor = NumberFormat.getNumberInstance();
	avgFor.setMaximumFractionDigits(2);
	avgFor.setMaximumFractionDigits(3);
	
	a = new DecimalField(0.05, 4, tuneFor);
	phi = new DecimalField(0.0, 4, tuneFor);

	x = new DecimalField(0.0, 6, avgFor);
	xerror = new DecimalField(0.0, 6, avgFor);
	xp = new DecimalField(0.0, 6, avgFor);
	xperror = new DecimalField(0.0, 6, avgFor);
	y = new DecimalField(0.0, 6, avgFor);
	yerror = new DecimalField(0.0, 6, avgFor);
	yp = new DecimalField(0.0, 6, avgFor);
	yperror = new DecimalField(0.0, 6, avgFor);
	
	for(int i=0; i<=3; i++){
	    errorlabel[i] = new JLabel("     +/-");
	    //errorlabel[i].setHorizontalTextPosition(SwingConstants.CENTER);
	}
	
	 fit = new OneTurnFit(doc);
	
    } 

    public void setAction(){
	
	closedorbitbutton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
	    
	    getClosedOrbit();
	    
	    }
	});
	
	
	loadbutton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
	    int i=0;
	    Iterator itr = (doc.bpmagents).iterator();
	    while(itr.hasNext() && i <= bpmtable.getRowCount()-1){
		BpmAgent bpmagent = (BpmAgent)itr.next(); 
		if(bpmagent.isConnected()){
		    System.out.println("BPM agent " + bpmagent.name() + " has " + bpmagent.getXAvg());
		    bpmtablemodel.setValueAt(new Double(bpmagent.getXAvgTBTArray()[0]), i, 1);
		    bpmtablemodel.setValueAt(new Double(bpmagent.getYAvgTBTArray()[0]), i, 2);
		    bpmtablemodel.setValueAt(new Double(bpmagent.getXAvg()), i, 3);
		    bpmtablemodel.setValueAt(new Double(bpmagent.getYAvg()), i, 4);
		}
		else{
		    bpmtablemodel.setValueAt(new Double(-1000), i, 1);
		    bpmtablemodel.setValueAt(new Double(-1000), i, 2);
		    bpmtablemodel.setValueAt(new Double(-1000), i, 3);
		    bpmtablemodel.setValueAt(new Double(-1000), i, 4);
		    bpmtablemodel.setValueAt(false, i, 5);
		}
		i++;
	    }
	    bpmtablemodel.fireTableDataChanged();
	    }
	});
	
	
	calcbutton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent ae) {
		int i = 0;
		activebpmagents.clear();
		Iterator itr = (doc.bpmagents).iterator();
		
		while(itr.hasNext() && i <= bpmtable.getRowCount()-1){
		    //Do the fit for all active BPM Agents
		    BpmAgent bpmagent = (BpmAgent)itr.next();   
		    if(((Boolean)bpmtable.getValueAt(i, 5)).booleanValue() == true){
			activebpmagents.add(bpmagent);	
		    }
		    i++;
		}
		fit.setupModel();
		double[] xparams = fit.xbpmFit(activebpmagents);
		fit.refreshHPlot(activebpmagents);
		double[] yparams = fit.ybpmFit(activebpmagents);
		fit.refreshVPlot(activebpmagents);
		
		x.setValue(xparams[0]); xerror.setValue(xparams[1]);
		xp.setValue(xparams[2]); xperror.setValue(xparams[3]);
		y.setValue(yparams[0]); yerror.setValue(yparams[1]);
		yp.setValue(yparams[2]); yperror.setValue(yparams[3]);
	    }	
	}); 
	    
	storebutton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		inj_params[0]=x.getValue();
		inj_params[1]=xp.getValue();
		inj_params[2]=y.getValue();
		inj_params[3]=yp.getValue();
		doc.setInjSpot(inj_params);	
		injectionProxy.updateInjSpot(inj_params);
	    }
	});
    }
    
    
    public void makeBPMTable(){
	String[] colnames = {"Device", "X (mm)", "Y (mm)", "X_co", "Y_co", "Select"};
	bpmtablemodel = new BPMTableModel(colnames, doc.nbpmagents);
	
	bpmtable = new JTable(bpmtablemodel);
	bpmtable.getColumnModel().getColumn(0).setPreferredWidth(140);
	bpmtable.getColumnModel().getColumn(1).setPreferredWidth(60);
	bpmtable.getColumnModel().getColumn(2).setPreferredWidth(60);
	bpmtable.getColumnModel().getColumn(3).setPreferredWidth(60);
	bpmtable.getColumnModel().getColumn(4).setPreferredWidth(60);
	bpmtable.getColumnModel().getColumn(5).setPreferredWidth(50);
	bpmtable.setPreferredScrollableViewportSize(bpmtable.getPreferredSize());
	bpmtable.setRowSelectionAllowed(false);
	bpmtable.setColumnSelectionAllowed(false);
	bpmtable.setCellSelectionEnabled(false);
	
	bpmscrollpane = new JScrollPane(bpmtable);
	bpmscrollpane.getVerticalScrollBar().setValue(0);
	bpmscrollpane.setPreferredSize(new Dimension(425, 300));
	
	//Set some initial values.
	Iterator itr = (doc.bpmagents).iterator();
	int i =0;
	boolean initial = true;
	while(itr.hasNext()){
	    BpmAgent bpmagent = (BpmAgent)itr.next();   
	    bpmtablemodel.setValueAt(bpmagent.name(), i, 0);
	    bpmtablemodel.setValueAt(new Double(0.0), i, 1);
	    bpmtablemodel.setValueAt(new Double(0.0), i, 2);
	    bpmtablemodel.setValueAt(new Double(0.0), i, 3);
	    bpmtablemodel.setValueAt(new Double(0.0), i, 4);
	    bpmtablemodel.setValueAt(new Boolean(true), i, 5);
	    i++;
	}
	bpmtablemodel.fireTableDataChanged();
    }
    
    
    void getClosedOrbit(){
	HashMap orbitmap = new HashMap();
	
	Iterator itr = (doc.bpmagents).iterator();
	    while(itr.hasNext() && i <= bpmtable.getRowCount()-1){
		BpmAgent bpmagent = (BpmAgent)itr.next(); 
		double value = 0.0;
		orbitmap.put(new String(bpmagent.name()), new Double(value));
	    }	
	
	closedorbitmap = orbitmap;
    }
}








