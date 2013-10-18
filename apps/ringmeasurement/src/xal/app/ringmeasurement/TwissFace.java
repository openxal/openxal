/*
 * TwissFace.java
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 * Created on May 14, 2008
 */

package xal.app.ringmeasurement;

import java.io.*;                                                              
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.text.ParseException;
import java.lang.Math.*;
import java.lang.String;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

import xal.tools.apputils.EdgeLayout;
import xal.smf.impl.*;
import xal.smf.Ring;
import xal.tools.swing.DecimalField;
import xal.ca.*;
import xal.tools.plot.*;
import xal.tools.apputils.files.*;
import xal.service.pvlogger.*;
//import xal.tools.pvlogger.query.*;
import xal.tools.database.*;
import xal.model.*;
import xal.tools.fit.LinearFit;
import xal.model.probe.TransferMapProbe;
import xal.sim.scenario.ProbeFactory;
import xal.model.alg.TransferMapTracker;
import xal.sim.scenario.Scenario;
import xal.model.probe.traj.TransferMapTrajectory;
import xal.model.probe.traj.TransferMapState;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.smf.*;
import xal.tools.xml.*;
import xal.tools.data.*;
import xal.extension.smf.application.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.data.XMLDataManager;
import xal.extension.application.*;

/**
 * 
 * @author cp3, swl
 */
public class TwissFace extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
   
    EdgeLayout edgeLayout = new EdgeLayout();
    JPanel mainPanel= new JPanel();	
   
    JButton openbutton;
    JButton calcbutton;
    JButton filterbutton;
    JButton plotbetabutton;
    JButton plottableClearbutton;
	JButton exportbutton; 
    EdgeLayout layout = new EdgeLayout();
    RecentFileTracker ft;
    JFileChooser fc;
    ArrayList<String> filesopened = new ArrayList<String>(0);
    JScrollPane datascrollpane;
    JScrollPane BPMlistScrollPane;
    JScrollPane betaplotscrollpane;
    
	FunctionGraphsJPanel datapanel;
	FunctionGraphsJPanel betapanel;
	public HashMap<String, ArrayList<Double>> bpmXData;
	public HashMap<String, ArrayList<Double>> bpmYData;
	public HashMap<String, ArrayList<Double>> bpmAmpData;
   
    public HashMap<String, ArrayList<HashMap<String, ArrayList<Double>>>>  masterHashmap = new HashMap<String, ArrayList<HashMap<String, ArrayList<Double>>>>();
    private String filename = new String("No Files Selected Yet");
    private String previousfilename = new String("was not a previous filename");
    private String activefilename = new String("no files active yet");
    private String activeBpmname = new String("No BPMs Yet");
    private String[] filescolnames = {"File Name", "Analyze?"};
    private String[] BPMcolnames = {"BPM","S Position", "Use?", "plot BPM?"};
    private String[] Plottablecolnames = {"Filename","Turns", "Bad BPMs", "Missing BPMs", "Plot Color", "Plotted?"};
    private DataTableModel datatablemodel;
    private DataTableModel bpmtablemodel;
    private DataTableModel plottablemodel;
    private JTable datatable;                     
    private JTable BPMtable;
    private JTable plottable;
    public AcceleratorSeqCombo ringseq;
    Accelerator accl = new Accelerator();   
    ArrayList<AcceleratorNode> bpmlist = new ArrayList<AcceleratorNode>();
    ArrayList<String> shortenedBPMlist = new ArrayList<String>();
    JLabel nturnslabel = new JLabel("Enter Number of Turns to Use:");
    JLabel filelabel = new JLabel(filename);
    JLabel bpmplotlabel = new JLabel("Turn by Turn Plot of Individual BPMs");
    JLabel betaplotlabel = new JLabel("Betatron Function along length of Ring in Meters");
    JLabel badBPMlabel = new JLabel("Bad BPMs: No BPMs yet");
    JLabel missingBPMlabel = new JLabel("Missing BPMs: No BPMs yet");
    JLabel bpmRemovalWarninglabel = new JLabel(" ");
    JLabel bpmRemovalPhaselabel = new JLabel(" ");
    JLabel bpmRemovalBetalabel = new JLabel(" ");
	JLabel xelementlabel = new JLabel("Beta X Scale: ");
	JLabel xscalelabel = new JLabel("to value: ");
	JLabel yelementlabel = new JLabel("Beta Y Scale: ");
	JLabel yscalelabel = new JLabel("to value: ");

	private String[] xscaleelements = {"No Scaling"};
    private JComboBox<String> xelementchooser = new JComboBox<String>(xscaleelements);
	private String[] yscaleelements = {"No Scaling"};
    private JComboBox<String> yelementchooser = new JComboBox<String>(yscaleelements);
	
    private ArrayList<Double> bpmdataforplot = new ArrayList<Double>();
    private ArrayList<String> badBPMlist = new ArrayList<String>();
    private ArrayList<String> missingBPMlist = new ArrayList<String>();
    
    DecimalField numberturns;
	DecimalField xscalebeta;
	DecimalField yscalebeta;
    NumberFormat numfor = NumberFormat.getNumberInstance();
	private JFileChooser fe;
	 
    //this buttongroup controls which type of BPM data is plotted: amplitutde, horizontal, or Vertical
    ButtonGroup plottypechooser = new ButtonGroup();
    JRadioButton Xchooser;
    JRadioButton Ychooser;	
    JRadioButton ampchooser;

    //this buttongroup controls which type of analysis data is plotted: Horizontal Beta, Vertical Beta, or phase advance
    ButtonGroup betaplottypechooser = new ButtonGroup();
    JRadioButton Xchoice;
    JRadioButton Ychoice;	
    JRadioButton Xphasechoice;
    JRadioButton Yphasechoice;
   
    //these ints keep track of BPM data in various ways
    public int numBPMs; //number of BPMs. This value comes from AccSequenceCombo and = 44
    public int numturns; //number of turns to use in analysis set by user in decimal field numberturns
    public int plottype = 3; //determines whether to plot amplitutde, horizontal, or Vertical BPM data
    public int betaplottype = 1; //determines whether to plot horizontal, Vertical or phase Beta
    public int numbad = 0; //counts the number of BPMs that are missing or bad
   
    //create variables used in MIA analysis
    Matrix XBPMmatrix;
    Matrix YBPMmatrix;
    SingularValueDecomposition XSVD;
    SingularValueDecomposition YSVD;
    double[] XSigma;
    double[] YSigma;
    Matrix UXmatrix;
    Matrix UYmatrix;
    Matrix VXmatrix;
    Matrix VYmatrix;
    double[] XBeta;
    double[] YBeta;
    double[] BPMpos;
    double[] XPhase;
    double[] YPhase;
    double[] XPhaseacc;
    double[] YPhaseacc;
    HashMap<String, HashMap<String, Object>> plottedData = new HashMap<String, HashMap<String, Object>>();
    String KeyMaster; 
    ArrayList<String> keymasterList = new ArrayList<String>();
    int colorlistindex = 0;
    ArrayList<Color> colorlist = new ArrayList<Color>();
    ArrayList<String> colorlistStrings = new ArrayList<String>();
    
    //Member function Constructor
	public TwissFace(){
		makeComponents();//Creation of all GUI components
		makeDataTable(); //creation of the datatable displaying active files. new code
		BPMFactory();	//creates arraylist of BPMs and arraylist of shortened BPM name strings
		makeBPMTable(); //creation of table showing individual bpmlist in an analyzed file
		makePlotTable(); //calls the creation of a table showing analyzed data sets
		addComponents();  //Add all components to the layout and panels
		setAction(); //Set the action listeners
	}
    

	public void addComponents(){
		layout.setConstraints(mainPanel, 0, 0, 0, 0, EdgeLayout.ALL_SIDES, EdgeLayout.GROW_BOTH);
		this.add(mainPanel);
		EdgeLayout newlayout = new EdgeLayout();
		
		GridLayout initgrid = new GridLayout(6, 4);
		mainPanel.setLayout(newlayout);
		
		newlayout.add(datascrollpane,mainPanel,4, 50,EdgeLayout.LEFT); //add table to display opened files
		newlayout.add(openbutton,mainPanel,4,10,EdgeLayout.LEFT); //add button to open files
		newlayout.add(BPMlistScrollPane,mainPanel,4,180,EdgeLayout.LEFT); //adds and locates BPM display table
		newlayout.add(xelementlabel,mainPanel,4,460,EdgeLayout.LEFT); 
		newlayout.add(xelementchooser,mainPanel,90,455,EdgeLayout.LEFT); 
		newlayout.add(xscalelabel,mainPanel,235,460,EdgeLayout.LEFT); 
		newlayout.add(xscalebeta,mainPanel,300,455,EdgeLayout.LEFT); 
		newlayout.add(yelementlabel,mainPanel,4,490,EdgeLayout.LEFT); 
		newlayout.add(yelementchooser,mainPanel,90,485,EdgeLayout.LEFT); 
		newlayout.add(yscalelabel,mainPanel,235,490,EdgeLayout.LEFT); 
		newlayout.add(yscalebeta,mainPanel,300,485,EdgeLayout.LEFT); 
		newlayout.add(calcbutton,mainPanel,0,535,EdgeLayout.LEFT); //add MIA analysis JButton
		newlayout.add(filterbutton,mainPanel,4,375,EdgeLayout.LEFT); //add MIA analysis JButton
		newlayout.add(filelabel,mainPanel,4,160,EdgeLayout.LEFT); //add Jlabel for filenames
		newlayout.add(bpmplotlabel,mainPanel,350,10,EdgeLayout.LEFT); //add Jlabel for filenames
		newlayout.add(nturnslabel,mainPanel,360,255,EdgeLayout.LEFT); //add Jlabel for number of turns field
		newlayout.add(datapanel, mainPanel, 350, 25, EdgeLayout.LEFT); //add plot of BPMs
		newlayout.add(numberturns, mainPanel, 560,250, EdgeLayout.LEFT); // add number of turns field
		newlayout.add(Xchooser,mainPanel,640,250,EdgeLayout.LEFT); //add X radio button
		newlayout.add(Ychooser,mainPanel,720,250,EdgeLayout.LEFT); //add Y radio button
		newlayout.add(ampchooser,mainPanel,800,250,EdgeLayout.LEFT); //add amp radio button
		newlayout.add(Xchoice,mainPanel,10,635,EdgeLayout.LEFT); //add X radio button
		newlayout.add(Ychoice,mainPanel,150,635,EdgeLayout.LEFT); //add Y radio button
		newlayout.add(Xphasechoice,mainPanel,10,665,EdgeLayout.LEFT); //add amp radio button
		newlayout.add(Yphasechoice,mainPanel,150,665,EdgeLayout.LEFT); //add amp radio button
		newlayout.add(badBPMlabel,mainPanel,4,415,EdgeLayout.LEFT); //add Jlabel to indicate which BPMs are bad
		newlayout.add(missingBPMlabel,mainPanel,4,430,EdgeLayout.LEFT); //add Jlabel to indicate which BPMs are missing
		newlayout.add(betaplotscrollpane,mainPanel,350,280,EdgeLayout.LEFT); //add Jtable of processed data for plotting 	
		newlayout.add(plottableClearbutton,mainPanel,360,390,EdgeLayout.LEFT); //add button for clearing results table
		newlayout.add(plotbetabutton,mainPanel,770,390,EdgeLayout.LEFT); //add button for plotting Beta
		newlayout.add(exportbutton,mainPanel,770,420,EdgeLayout.LEFT); //add export button	
		newlayout.add(betapanel, mainPanel, 350,545, EdgeLayout.LEFT); //add plot of beta
		newlayout.add(betaplotlabel,mainPanel,350,520,EdgeLayout.LEFT); //add Jlabel for beta plot
		newlayout.add(bpmRemovalWarninglabel,mainPanel,10,575,EdgeLayout.LEFT); //add label to warn user about too many bad BPMs
		newlayout.add(bpmRemovalPhaselabel,mainPanel,10,595,EdgeLayout.LEFT); //add label to warn user about too many bad BPMs affecting phase
		newlayout.add(bpmRemovalBetalabel,mainPanel,10,585,EdgeLayout.LEFT); //add label to warn user about too many bad BPMs affecting phase
	}     
    
    public void makeComponents(){
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(950,900));
		openbutton = new JButton("Add New BPM File");
		calcbutton = new JButton("MIA Analyze");
		plotbetabutton = new JButton("Update Results Plot");
		filterbutton = new JButton("Find BPMs with Bad Data");
		plottableClearbutton = new JButton("Clear Results Table");
		exportbutton = new JButton("Export Results to File");
		fe = new JFileChooser();

		//makes active file display table
		datatablemodel = new DataTableModel(filescolnames, 0);
		datatable = new JTable(datatablemodel);
		datascrollpane = new JScrollPane(datatable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		//makes BPM display table
		bpmtablemodel = new DataTableModel(BPMcolnames, 0);
		BPMtable = new JTable(bpmtablemodel);
		BPMlistScrollPane = new JScrollPane(BPMtable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		 
		//makes plot choosing table
		plottablemodel = new DataTableModel(Plottablecolnames, 0);
		plottable = new JTable(plottablemodel);
		betaplotscrollpane = new JScrollPane(plottable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		//makes BPM Plot
		datapanel = new FunctionGraphsJPanel();
		datapanel.setPreferredSize(new Dimension(580, 220));
		datapanel.setGraphBackGroundColor(Color.WHITE);

		//makes beta Plot
		betapanel = new FunctionGraphsJPanel();
		betapanel.setPreferredSize(new Dimension(580, 220));
		betapanel.setGraphBackGroundColor(Color.WHITE);

		//makes number field for number of turns to analyze	
		numberturns = new DecimalField(200,4, numfor);
		numfor.setMinimumFractionDigits(1);    
		
		//for scaling the beta function
		xscalebeta = new DecimalField(15,4, numfor);
		yscalebeta = new DecimalField(15,4, numfor);
		
		//makes a radiobutton group for choosing the type of data to display on BPM plot
		Xchooser = new JRadioButton("X Data");
		Ychooser = new JRadioButton("Y Data");
		ampchooser = new JRadioButton("Amp Data");
		ampchooser.setSelected(true);
		plottypechooser.add(Xchooser);
		plottypechooser.add(Ychooser);
		plottypechooser.add(ampchooser);


		//makes a radiobutton group for choosing the type of data to display on BPM plot
		Xchoice = new JRadioButton("X Beta");
		Ychoice = new JRadioButton("Y Beta");
		Xphasechoice = new JRadioButton("X Phase Advance");
		Yphasechoice = new JRadioButton("Y Phase Advance");
		//Xphasechoice.setEnabled(false);
		//Yphasechoice.setEnabled(false);
		Xchoice.setSelected(true);
		betaplottypechooser.add(Xchoice);
		betaplottypechooser.add(Ychoice);
		betaplottypechooser.add(Xphasechoice);
		betaplottypechooser.add(Yphasechoice);

		fc = new JFileChooser();
		ft = new RecentFileTracker(1, this.getClass(), "bpmfile");
		ft.applyRecentFolder(fc);

		filesopened = new ArrayList<String>();
    }
    

	public void setAction(){
		 Xchoice.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				betaplottype = 1;
				plotBeta();
			}
		});
		Ychoice.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				betaplottype = 2;
				plotBeta();
			}
		});    
		Xphasechoice.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				betaplottype = 3;
				plotBeta();
			}
		});  
		Yphasechoice.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				betaplottype = 4;
				plotBeta();
			}
		});  
		
		Xchooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				plottype = 1;
				plotBPMs();
			}
		});
		Ychooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				plottype = 2;
				plotBPMs();
			}
		});    
		ampchooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				plottype = 3;
				plotBPMs();
			}
		});   
		plotbetabutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				if(XBeta == null) {
					System.out.println("Please perform analysis before plotting");
				}
				else { 
				plotBeta();
				}
			}
		});
		
		plottableClearbutton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
			plottablemodel.clearAllData();	
			keymasterList.clear();
			plottedData.clear();
			}
		});
		
		calcbutton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
			MIAanalysis();
		 }
		});
		
		filterbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
			BadBPMfinder();
			 }
		});  
   
	    
    	openbutton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent e) {
			int returnValue = fc.showOpenDialog(TwissFace.this);
			if(returnValue == JFileChooser.APPROVE_OPTION){
				File file = fc.getSelectedFile();
				ft.cacheURL(file);
				
				ArrayList<HashMap<String, ArrayList<Double>>> newdata = new ArrayList<HashMap<String, ArrayList<Double>>>(parseFile(file));
				
				String name = new String(file.toString());
				String[] tokens;
				tokens=name.split("/");
				filename = new String(tokens[tokens.length-1]);
				
				if(!filesopened.contains(filename)){
					System.out.println("Opening file: " + filename);
				//put the filename in the filesopen arraylist
					filesopened.add(filename);
				//create public hashmap for general use of BPM data
					masterHashmap.put(filename,newdata);   
					
					toTable(filename, newdata);
					datatablemodel.fireTableDataChanged();

				}
				else{
					System.out.println("File " + filename + " has already been opened.");
					}
				}
			else{
				System.out.println("Open command cancelled by user.");
				}
			}
		});  
	 
		exportbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int returnValue = fe.showSaveDialog(TwissFace.this);
				if(returnValue == JFileChooser.APPROVE_OPTION){
					File file = fe.getSelectedFile();
					try{
						exportData(file);
					}
					catch(IOException ioe){}
				}
				else{
					System.out.println("Save command canceled by user.");
				}
			}
		});
	
    }
    

    
    //puts the data from new files into the display table. is called from actionlistener
	public void toTable(String filename, ArrayList<HashMap<String, ArrayList<Double>>> newdata){
		ArrayList<Object> tabledata = new ArrayList<Object>();
		tabledata.add(filename);
		tabledata.add(new Boolean(false));
			
		datatablemodel.addTableData(new ArrayList<Object>(tabledata));
		datatablemodel.fireTableDataChanged();
	} 
    

    
  //parsing for data files into arraylist called newdata
	public ArrayList<HashMap<String, ArrayList<Double>>> parseFile(File newfile){
		ParseBPMFile parsefile = new ParseBPMFile();
		ArrayList<HashMap<String, ArrayList<Double>>> newdata = new ArrayList<HashMap<String, ArrayList<Double>>>();
		try{
			newdata = parsefile.parseFile(newfile);
		}
		catch(IOException e){
			System.out.println("Warning, returning empty data set.");
		}
		return newdata;
    }  

	
	//creates a table of filenames with option to show BPM data
	 public void makeDataTable(){
		datatable.getColumnModel().getColumn(0).setMinWidth(180);
		datatable.getColumnModel().getColumn(1).setMinWidth(100);
		
		datatable.setPreferredScrollableViewportSize(datatable.getPreferredSize());
		datatable.setRowSelectionAllowed(false);
		datatable.setColumnSelectionAllowed(false);
		datatable.setCellSelectionEnabled(false);

		datascrollpane.getVerticalScrollBar().setValue(0);
		datascrollpane.getHorizontalScrollBar().setValue(1);
		datascrollpane.setPreferredSize(new Dimension(300, 100));	

		ButtonRenderer buttonRenderer = new ButtonRenderer();
		datatable.getColumnModel().getColumn(1).setCellRenderer(buttonRenderer);
		datatable.getColumnModel().getColumn(1).setCellEditor(buttonRenderer); 
		
	  }
  
	//creates the BPM display table
	  public void makeBPMTable(){
		BPMtable.getColumnModel().getColumn(0).setMinWidth(40);
		BPMtable.getColumnModel().getColumn(1).setMinWidth(80);
		BPMtable.getColumnModel().getColumn(2).setMinWidth(40);
		BPMtable.getColumnModel().getColumn(3).setMinWidth(80);
		
		BPMtable.setPreferredScrollableViewportSize(BPMtable.getPreferredSize());
		BPMtable.setRowSelectionAllowed(false);
		BPMtable.setColumnSelectionAllowed(false);
		BPMtable.setCellSelectionEnabled(false);
		
		BPMlistScrollPane.getVerticalScrollBar().setValue(0);    
		BPMlistScrollPane.getHorizontalScrollBar().setValue(1);
		BPMlistScrollPane.setPreferredSize(new Dimension(300, 190));
		
		BPMButtonRenderer BPMbuttonrenderer = new BPMButtonRenderer();
		BPMtable.getColumnModel().getColumn(3).setCellRenderer(BPMbuttonrenderer);
		BPMtable.getColumnModel().getColumn(3).setCellEditor(BPMbuttonrenderer);
		bpmtablemodel.fireTableDataChanged();
		} 


	//creates a table of finished plot data so that multiple lines can be displayed on results plot
	 public void makePlotTable(){
		plottable.getColumnModel().getColumn(0).setMinWidth(180);
		plottable.getColumnModel().getColumn(1).setMinWidth(20);
		plottable.getColumnModel().getColumn(2).setMinWidth(120);
		plottable.getColumnModel().getColumn(3).setMinWidth(60);
		plottable.getColumnModel().getColumn(4).setMinWidth(60);
		plottable.getColumnModel().getColumn(5).setMinWidth(50);
		
		plottable.setPreferredScrollableViewportSize(datatable.getPreferredSize());
		plottable.setRowSelectionAllowed(true);
		plottable.setColumnSelectionAllowed(false);
		plottable.setCellSelectionEnabled(false);

		betaplotscrollpane.getVerticalScrollBar().setValue(0);
		betaplotscrollpane.getHorizontalScrollBar().setValue(0);
		betaplotscrollpane.setPreferredSize(new Dimension(600, 100));	
	 } 
	
//iterates through BPMs to populate BPM table
	public void BPMfiller(ArrayList<AcceleratorNode> bpmlist) {
		filelabel.setText(filename); //update Jlabel over BPM table with current filename 
		SwingUtilities.updateComponentTreeUI(filelabel); //update Jlabel
		
		System.out.println("bpmfiller running");
		System.out.println(filename + " = filename and " + previousfilename + " = previousfilename");
		
		Iterator<AcceleratorNode> itr = bpmlist.iterator();
		int i=0;
		BPMtable.removeAll();
		xelementchooser.removeAllItems();
		yelementchooser.removeAllItems();
		xelementchooser.addItem(new String("No Scaling"));
		yelementchooser.addItem(new String("No Scaling")); 
		bpmtablemodel.clearAllData();
		String[] bpmnamelist = new String[bpmlist.size()];
		while(itr.hasNext()){
			BPM tempbpm = (BPM)itr.next();
			ArrayList<Double> BPMposArrayList = new ArrayList<Double>();
			AcceleratorNode bpmposnode = ringseq.getNodeWithId(tempbpm.toString());
			xelementchooser.addItem(tempbpm.toString().substring(10));
			yelementchooser.addItem(tempbpm.toString().substring(10));
			BPMposArrayList.add(( (double)(Math.round( (ringseq.getPosition(bpmposnode)*100)) )/100 ) );
			
			ArrayList<Object> tabledata = new ArrayList<Object>();
			tabledata.add(tempbpm.getId().toString().substring(14));
			tabledata.add(BPMposArrayList);
			tabledata.add(new Boolean(true));
			tabledata.add(new Boolean(false));
			bpmtablemodel.addTableData(new ArrayList<Object>(tabledata));
			i++;     
			} 
		bpmtablemodel.fireTableDataChanged();	
	}

//Renderer class for plot button in BPM table
	class ButtonRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor,ActionListener{
        
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
		public JButton analyzeButton;
		protected static final String EDIT = "edit";
		
		public ButtonRenderer(){
			analyzeButton = new JButton("Analyze");
			analyzeButton.setActionCommand(EDIT);
			analyzeButton.addActionListener(this);
		
		}
		public Component getTableCellEditorComponent(JTable table, Object agent, boolean isSelected, int row, int column){
			   filename = filesopened.get(row);
			//System.out.print("The selected file is " + filename + "\n");
		
		
				//next 4 lines extract data from the currently active file, which was originally stored in masterHashmap
			ArrayList<HashMap<String, ArrayList<Double>>> currentdata = masterHashmap.get(filename);
			bpmXData = currentdata.get(0); //hashmap masterhashmap keys to arraylist of 3 hashmaps with X,Y, and amp data
			bpmYData = currentdata.get(1);
			bpmAmpData = currentdata.get(2);
			//System.out.println("Hashmap 35th element =" + (ArrayList)bpmXData.get("A02") );
			
			return analyzeButton;
		} 
		
		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {
				fireEditingStopped(); //Make the renderer reappear
			
			if(!filename.equals(previousfilename)){
				BPMfiller( bpmlist); //populate BPM table with BPM list
				badBPMlabel.setText("Bad BPMs: No BPMs yet"); //clear Jlabel for bad BPMs when new file is loaded
				SwingUtilities.updateComponentTreeUI(badBPMlabel);	  
				missingBPMlabel.setText("Missing BPMs: No BPMs yet"); //clear Jlabel for bad BPMs when new file is loaded
				SwingUtilities.updateComponentTreeUI(badBPMlabel);	  			
				datapanel.removeAllGraphData();
				//System.out.println("if statement being called means previousfilename != filename");
				}

			}
		}
		public Object getCellEditorValue(){
			return "";
		}
		public boolean isCellEditable(){
			return true;
		}
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
			return analyzeButton;
		} 
	
    } 
	//This class handles the buttons in the BPM list table. 
	class BPMButtonRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor,ActionListener{
        
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
		public JButton bpmplotbutton;
		protected static final String EDIT = "edit";
		public BPMButtonRenderer(){
			bpmplotbutton = new JButton("plot");
			bpmplotbutton.setActionCommand(EDIT);
			bpmplotbutton.addActionListener(this);
	}
	
	public Component getTableCellEditorComponent(JTable table, Object agent, boolean isSelected, int row, int column){
		activeBpmname = bpmlist.get(row).toString().substring(14);
		//System.out.println("plot BPM " + activeBpmname + " was pressed");
		
		//the label above the BPM plot gets the name of the active file. 
		bpmplotlabel.setText("BPM " + activeBpmname + " data from file " + filename + " in mm, Function of Turn #"); //update Jlabel with active BPM's name 
		SwingUtilities.updateComponentTreeUI(bpmplotlabel);
	
		return bpmplotbutton;
	} 
	
	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			fireEditingStopped(); 
			plotBPMs(); //call the function that fills the BPM tbt plot when you press the buttons in the BPM table
		}
	}
	public Object getCellEditorValue(){
	    return "";
	}
	public boolean isCellEditable(){ return true; }
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
	  	return bpmplotbutton;
	} 
   } 
   
   //This routine gets the sequence of BPMs in the accelerator and populates the table with them. 
	public void BPMFactory(){
		accl = XMLDataManager.loadDefaultAccelerator();
		ringseq = accl.getComboSequence("Ring");
		//bpmlist is a sequence of all BPMs in the machine, good or bad. each element is the BPM class
		bpmlist = (ArrayList<AcceleratorNode>)ringseq.getNodesOfType("BPM");
		
		//shortenedBPMlist contains just the strings of bpm names like "C09" etc
		numBPMs = bpmlist.size();
		for(int j = 0; j < numBPMs; j++){
			shortenedBPMlist.add(bpmlist.get(j).toString().substring(14));	
		}
	}  
	

   
	public void plotBPMs(){  
		Boolean plotted = true;
		datapanel.removeAllGraphData();
		BasicGraphData rawgraphdata = new BasicGraphData();
		
		numturns = (int)numberturns.getDoubleValue();
		
		//the plot type is set by the radio buttons below the BPM plot, and determines which type of plot is displayed via the integer plottype	
		if(plottype==1){
			bpmdataforplot = bpmXData.get(activeBpmname);
			}
		if(plottype==2){
			bpmdataforplot = bpmYData.get(activeBpmname);
			}
		if(plottype==3){
			bpmdataforplot = bpmAmpData.get(activeBpmname);
			}
		
		int datasize = bpmdataforplot.size();
		double[] yaxisarray =new double[numturns];
		double[] xaxis = new double[numturns];
		for(int i = 0; i < datasize && i < numturns; i++){ //for loop fills plot with Y values from raw BPM data
			yaxisarray[i]= bpmdataforplot.get(i);
			xaxis[i]= (double)i;
			} 	 
		
		rawgraphdata.addPoint(xaxis, yaxisarray); 
		rawgraphdata.setDrawPointsOn(true);
		rawgraphdata.setDrawLinesOn(true); 
		rawgraphdata.setGraphProperty("Legend", new String("raw data"));
		rawgraphdata.setGraphColor(Color.RED);
		datapanel.addGraphData(rawgraphdata);
		datapanel.setLegendButtonVisible(true);
		datapanel.setChooseModeButtonVisible(true);	
    } 
    


	public void MIAanalysis(){
		int numbad = 0;
		//badBPMlist.clear();
		missingBPMlist.clear();
		double xscalefac = 0.0;
		double yscalefac = 0.0; 
		
		if (bpmXData.size() == 0) { //should only be able to perform analysis on opened files shown on BPM plot
			System.out.println("no files open yet");
		}
		else {
			//this for loop does a lot of things to help keep track of missing and bad BPMs
			for(int k = 0; k < numBPMs; k++){ 
				//if statement looks for BPMs that have been deselected in the BPM table and renames them in the shortenedBPMlist to "bad"
				//they get added to the badBPMlist and numbad counts them
				if(((Boolean)BPMtable.getValueAt(k, 2)).booleanValue() == false &&
					(ArrayList)bpmXData.get(bpmlist.get(k).toString().substring(14)) != null){
					//System.out.println(shortenedBPMlist.remove(k) + " Removed");
					shortenedBPMlist.set(k, "bad");
					if(!badBPMlist.contains(bpmlist.get(k).toString().substring(14))){
						badBPMlist.add(bpmlist.get(k).toString().substring(14));
					}
					numbad++;
				}
				//this if statement looks for BPMs that are entirely missing from the data and labels them as "missing" in the shortenedBPMlist
				if((ArrayList)bpmXData.get(bpmlist.get(k).toString().substring(14)) == null ) {
					if(!missingBPMlist.contains(bpmlist.get(k).toString().substring(14))){
						//add missing BPMs to missingBPMlist:
						missingBPMlist.add(bpmlist.get(k).toString().substring(14));
					}
					shortenedBPMlist.set(k, "missing");
					numbad++;
				}
				//if a BPM had been previously set as bad and is now selected in the table, 
				//and if it is not missing, that BPM is given it's original name in the shortenedBPMlist and the number of bad BPMs goes down by 1
				
				if ( ((Boolean)BPMtable.getValueAt(k, 2)).booleanValue() == true && 
					shortenedBPMlist.get(k).equals("bad")) {
						badBPMlist.remove(bpmlist.get(k).toString().substring(14));
						shortenedBPMlist.set(k, bpmlist.get(k).toString().substring(14));
						//numbad = numbad-1;
						//System.out.println("In here adding back the BPM and decreasing number of bad bpms to " + numbad);
					}
				}
		}

		System.out.println("Final number of bad or unused bpms is " + numbad);
		numturns = (int)numberturns.getDoubleValue();	//ensures that analysis uses latest user-entered turn count

		//create 2D arrays out of out of X and Y bpm data
		double[][] XBPMarray = new double[numturns][numBPMs - numbad];
		double[][] YBPMarray = new double[numturns][numBPMs - numbad];

		BPMpos = new double[numBPMs - numbad];

		//populate data arrays with values from hashmaps bpmXdata and bpmYdata
		int indexoffset = 0;
		int xselectedscaleelement = xelementchooser.getSelectedIndex() - 1;
		int xscaleindex = 0;
		int yselectedscaleelement = yelementchooser.getSelectedIndex() - 1;
		int yscaleindex = 0;
		for(int j = 0; j < numBPMs; j++){
			if(shortenedBPMlist.get(j).equals("bad") || shortenedBPMlist.get(j).equals("missing") ) {
				System.out.println("BPM " + bpmlist.get(j).toString().substring(14) + " is " + shortenedBPMlist.get(j) + ", and will not be analyzed.");
				indexoffset++; //indexoffset helps to ensure that bad BPMs don't mess up the array indices. final arrays need length of numBPMs - numbad
			}
			if(!shortenedBPMlist.get(j).equals("bad") && !shortenedBPMlist.get(j).equals("missing") ){ //this line ensures that only good BPMs fill the array
				for(int i = 0; i < numturns; i++){
				//System.out.println(indexoffset);
				//once a bad BPM occurs, the previous if statement counts it, and shifts the array index to skip it
				XBPMarray[i][j-indexoffset] = (Double)((ArrayList)bpmXData.get(shortenedBPMlist.get(j))).get(i);
				YBPMarray[i][j-indexoffset] = (Double)((ArrayList)bpmYData.get(shortenedBPMlist.get(j))).get(i);
				}
				//keeps track of the user selected element for x beta function scaling
				if(j == xselectedscaleelement){
					xscaleindex = j - indexoffset;
				}
				//keeps track of the user selected element for y beta function scaling
				if(j == yselectedscaleelement){
					yscaleindex = j - indexoffset;
				}
				//get position of BPMs in ring and put them in array BPMpos: used for plotting
				AcceleratorNode newnode = ringseq.getNodeWithId("Ring_Diag:BPM_" + shortenedBPMlist.get(j));
				BPMpos[j-indexoffset] = ringseq.getPosition(newnode);
			}

		}

		/*
		//execute a linear fit and extract deviations of data from best fit: for closed orbit drift correction
		LinearFit linearXfit = new LinearFit();
		LinearFit linearYfit = new LinearFit();
		// a linear fit of the data for x and y gets the values in the XBPMarray and YBPMarray as Y points, where X points are just the index
		for(int r = 0; r < numBPMs - numbad; r++) {
			for(int t = 0; t < numturns; t++) {
				linearXfit.addSample(t,XBPMarray[t][r]);
				linearYfit.addSample(t,YBPMarray[t][r]);
			}
		}
		//the deviation from the fit becomes the new array value
		for(int r = 0; r < numBPMs - numbad; r++) {
			for(int t = 0; t < numturns; t++) {
				XBPMarray[t][r] = XBPMarray[t][r] - linearXfit.estimateY(t) -linearXfit.getIntercept();
				YBPMarray[t][r] = YBPMarray[t][r] - linearYfit.estimateY(t) -linearYfit.getIntercept();
			}
		}
		System.out.println("best fit linear equation for X = " + linearXfit.toString());
		*/
		
		//Replacing linear fit routine from above with simple offset subtraction. Assumes turn-by-turn closed orbit drift is zero. 
		for(int r = 0; r < numBPMs - numbad; r++) {
			double xsum = 0.0; 
			double ysum = 0.0; 
			double xoffset = 0.0; 
			double yoffset = 0.0; 
			for(int t = 0; t < numturns; t++) {
				xsum += XBPMarray[t][r];
				ysum += YBPMarray[t][r];
			}
			xoffset = xsum/numturns;
			yoffset = ysum/numturns;
			for(int t = 0; t < numturns; t++) {
				XBPMarray[t][r] -= xoffset;
				YBPMarray[t][r] -= yoffset;
			} 
			//System.out.println("for BPM " + r + " x and y offset are " + xoffset + " " + yoffset);
		}
		
		//create matrices out of BPM data arrays, once linear fitting correction has been done
		XBPMmatrix = new Matrix(XBPMarray);
		YBPMmatrix = new Matrix(YBPMarray);

		//	System.out.println("Here is the matrix of data going into SVD:");
		//	XBPMmatrix.print(numfor, 5); //prints the matrix
			
		//perform Singular Value Decomposition on data
		XSVD = new SingularValueDecomposition(XBPMmatrix);
		YSVD = new SingularValueDecomposition(YBPMmatrix);
		
		//get the singular values
		XSigma = XSVD.getSingularValues();
		YSigma = YSVD.getSingularValues();
		
		//get the U and V singular matrices
		UXmatrix = XSVD.getU();
		VXmatrix = XSVD.getV();
		UYmatrix = YSVD.getU();
		VYmatrix = YSVD.getV();
		
		//conY SVD V matrices back into 2D arrays
		double[][] VXarray = VXmatrix.getArray();
		double[][] VYarray = VYmatrix.getArray();
		
		//create temporary 1D arrays out of the first and second columns of the SVD V matrices
		double[] VXarrayCol1 = new double[VXarray.length];
		double[] VXarrayCol2 = new double[VXarray.length];
		double[] VYarrayCol1 = new double[VXarray.length];
		double[] VYarrayCol2 = new double[VXarray.length];
		
		//fill 1D arrays with values from the respective columns of V matrix arrays
		for(int i =0; i < VXarray.length; i++) {
			VXarrayCol1[i] = VXarray[i][0];
			VXarrayCol2[i] = VXarray[i][1];
			VYarrayCol1[i] = VYarray[i][0];
			VYarrayCol2[i] = VYarray[i][1];
			}
			
			//unscaled calculated beta values are arrays of the same length as the # of rows in the SVD V matrix. This # of rows is same as # of good BPMs
			XBeta = new double[VXarray.length];
			YBeta = new double[VYarray.length];
			
			XPhase = new double[VXarray.length];
			YPhase = new double[VYarray.length];
			
			XPhaseacc = new double[VXarray.length];
			YPhaseacc = new double[VYarray.length];
			
			//do the final calculations to obtain the horizontal and Vertical unscaled betas values
			for(int i =0; i < VXarray.length; i++) {
				XBeta[i] =  (XSigma[0]*VXarrayCol1[i])*(XSigma[0]*VXarrayCol1[i]) +(XSigma[1]*VXarrayCol2[i])*(XSigma[1]*VXarrayCol2[i]) ;
				YBeta[i] =  (YSigma[0]*VYarrayCol1[i])*(YSigma[0]*VYarrayCol1[i]) +(YSigma[1]*VYarrayCol2[i])*(YSigma[1]*VYarrayCol2[i]) ;
				
				//get the phase angle using arctangent calculations                                             
				XPhaseacc[i] =  Math.atan((XSigma[1]*VXarrayCol2[i])/(XSigma[0]*VXarrayCol1[i]));
				YPhaseacc[i] =  Math.atan((YSigma[1]*VYarrayCol2[i])/(YSigma[0]*VYarrayCol1[i]));
				XPhase[i] = XPhaseacc[i];
				YPhase[i] = YPhaseacc[i];
				//System.out.println("X Phase[i] is " + XPhaseacc[i]);
			}
			
			if(xselectedscaleelement >= 0){
				xscalefac = xscalebeta.getDoubleValue()/XBeta[xscaleindex];
				for(int i =0; i < XBeta.length; i++) {
					XBeta[i] *= xscalefac;
					}
			}
			if(yselectedscaleelement >= 0){
				yscalefac = yscalebeta.getDoubleValue()/YBeta[yscaleindex];
				for(int i =0; i < YBeta.length; i++) {
					YBeta[i] *= yscalefac;
			}
		}
			/*
			//this for loop unwraps accumulated phases by adding pi if the phase angle passes 2Pi radians between BPMs
			for(int i = 0; i < XPhaseacc.length - 1; i++) {
				while(XPhaseacc[i] + 0.01 > XPhaseacc[i+1]){ XPhaseacc[i+1] = XPhaseacc[i+1]+Math.PI; }
				while(YPhaseacc[i] + 0.01 > YPhaseacc[i+1]){ YPhaseacc[i+1] = YPhaseacc[i+1]+Math.PI; }
			}
			
			
			//if the accumulated phase at very end is greater than 50 that is a strong indication that the sign of the arc tangent was chosen incorrectly and needs to be negative.	
			if(XPhaseacc[XPhaseacc.length-1] > 50 )  {
				for(int i =0; i < XPhaseacc.length; i++) {
				XPhase[i] = Math.atan((-XSigma[1]*VXarrayCol2[i])/(XSigma[0]*VXarrayCol1[i]));
				//System.out.println("Xphase is being remade using minus sign");	
				}
			}
			
			//if the accumulated phase at very end is greater than 50 that is a strong indication that the sign of the arc tangent was chosen incorrectly and needs to be negative.	
			if(YPhaseacc[YPhaseacc.length-1] > 50 ){
				for(int i =0; i < XPhaseacc.length; i++) {
				YPhase[i] =  Math.atan((-YSigma[1]*VYarrayCol2[i])/(YSigma[0]*VYarrayCol1[i]));
					}
			}
			
			//unwrap final phases for X and Y
			for(int i = 0; i < XPhase.length -1 ; i++) {
				while(XPhase[i] + 0.01 > XPhase[i+1]){ XPhase[i+1] = XPhase[i+1]+Math.PI; }
				while(YPhase[i] + 0.01 > YPhase[i+1]){ YPhase[i+1] = YPhase[i+1]+Math.PI; }
			}
			*/

		//store the final analyzed data in a hashmap
		//tempPlotholder is used to contain all the data associated with each finished result
		HashMap<String, Object> tempPlotholder = new HashMap<String, Object>();
		//tempPlotholder.clear();
		tempPlotholder.put("XBeta",XBeta);
		tempPlotholder.put("YBeta",YBeta);
		tempPlotholder.put("XPhase",XPhase);
		tempPlotholder.put("YPhase",YPhase);
		tempPlotholder.put("numturns",numturns);
		tempPlotholder.put("badBPMlist",badBPMlist);
		tempPlotholder.put("missingBPMlist",missingBPMlist);
		tempPlotholder.put("BPMpos",BPMpos);
	
		//fill an Arraylist whose elements are the colors to be used for plots
		colorlist.add(Color.BLUE);   
		colorlist.add(Color.GREEN); 
		colorlist.add(Color.CYAN);
		colorlist.add(Color.ORANGE); 
		colorlist.add(Color.MAGENTA); 
		colorlist.add(Color.BLACK);
		colorlist.add(Color.RED);
		colorlist.add(Color.PINK);
		colorlist.add(Color.GRAY);
		colorlist.add(Color.YELLOW); 
		colorlistStrings.add("BLUE");   
		colorlistStrings.add("GREEN"); 
		colorlistStrings.add("CYAN");
		colorlistStrings.add("ORANGE"); 
		colorlistStrings.add("MAGENTA"); 
		colorlistStrings.add("BLACK");
		colorlistStrings.add("RED");
		colorlistStrings.add("PINK");
		colorlistStrings.add("GRAY");
		colorlistStrings.add("YELLOW"); 
		
		
		//keymaster is a unique key for the results hashmap that will be unique and identifying for all processed data sets
		KeyMaster = (filename + Integer.toString(numturns) + badBPMlist.toString() + missingBPMlist.toString() + (new Integer(xselectedscaleelement)).toString() + (new Double(xscalefac)).toString() + missingBPMlist.toString() + (new Integer(yselectedscaleelement)).toString() + (new Double(yscalefac)).toString() );
	
	
		if(!plottedData.containsKey(KeyMaster)) {  //use the new data for each keymaster only if it has not been previously processed
			//keep track of the keymasters in an arraylist keymasterList
			keymasterList.add(KeyMaster);
			
			tempPlotholder.put("color",colorlist.get(colorlistindex));
			resultsTableFiller(); //call function that fills the results table 
			colorlistindex++; //count the colors used for the plots
			
			if(colorlistindex == colorlist.size()) {colorlistindex =0;} //wraps color index back to 0 if a lot of plots are used
			
			//plottedData is the hashmap used to store data that has been analyzed and the user might wish to plot
			plottedData.put(KeyMaster,tempPlotholder); 
			
			if(numbad >= 4){
				bpmRemovalWarninglabel.setText("WARNING: 4+ BPMs were removed:"); //update Jlabel warning user removing BPMs 
				SwingUtilities.updateComponentTreeUI(bpmRemovalWarninglabel); //update Jlabel
				bpmRemovalBetalabel.setText("Beta resolution may be affected"); //warns about decreasing resolution of Beta when many BPMs have been deselected 
				SwingUtilities.updateComponentTreeUI(bpmRemovalBetalabel); //update Jlabel
				bpmRemovalPhaselabel.setText("and Phase Advance may be small by Pi or n*Pi due to algorithm"); //update Jlabel warning user about phase being off by pi or 2 pi because of a few missing BPMs
				SwingUtilities.updateComponentTreeUI(bpmRemovalPhaselabel); //update Jlabel
			}
			else{
				bpmRemovalWarninglabel.setText(" "); //clear the warning Jlabel
				SwingUtilities.updateComponentTreeUI(bpmRemovalWarninglabel); //update Jlabel
				bpmRemovalBetalabel.setText(" "); //clear the warning Jlabel
				SwingUtilities.updateComponentTreeUI(bpmRemovalBetalabel); //update Jlabel
				bpmRemovalPhaselabel.setText(" "); //clear the warning Jlabel
				SwingUtilities.updateComponentTreeUI(bpmRemovalPhaselabel); //update Jlabel
			
			}
		
			plotBeta(); //finally call the function that plots Beta or Phase advance
		}
	

	} //end of MIAanalysis


public void resultsTableFiller() {
		ArrayList<Object> tabledata = new ArrayList<Object>();
		tabledata.add(filename);
		tabledata.add(Integer.toString(numturns));
		tabledata.add(badBPMlist.toString());
		tabledata.add(missingBPMlist.toString());
		tabledata.add(colorlistStrings.get(colorlistindex));
		tabledata.add(new Boolean(true));
		plottablemodel.addTableData(new ArrayList<Object>(tabledata));
		
		
		//if statement deselects the previous plot when a new plot arrives, 
		//this is an optional convenience feature and can be removed
		if(keymasterList.size() >= 2){
			for(int i =0;i<keymasterList.size()-1;i++){
			plottable.setValueAt(false,i, 5);
			}
		}
		
		plottablemodel.fireTableDataChanged();
}


	
// this routine plots the beta function using arrays BPMpos and X/Y beta
public void plotBeta(){  
	
	Boolean plotted = true;
	betapanel.removeAllGraphData();

	if(betaplottype == 1 || betaplottype == 2) {
	betaplotlabel.setText("Beta for " + filename + " along length of Ring in Meters"); //update Jlabel above results plot with active filename
	SwingUtilities.updateComponentTreeUI(betaplotlabel);
	}
	if(betaplottype == 3 || betaplottype == 4){
	betaplotlabel.setText("phase advance for " + filename + " along length of Ring in Meters"); //update Jlabel above results plot with file name
	SwingUtilities.updateComponentTreeUI(betaplotlabel);
	}

	//for loop runs through all booleans in results table that are true and plots them on graph
	for(int k =0;k < keymasterList.size(); k++){
		plottablemodel.fireTableDataChanged();
		betapanel.refreshGraphJPanel(); 
		
		if( (Boolean)plottable.getValueAt(k,5) == true ) {
			double[] dataforplot = new double[XBeta.length];
			if(betaplottype == 1){ 
				dataforplot = (double[])((HashMap)plottedData.get( keymasterList.get(k) )).get("XBeta");
				}
			if(betaplottype == 2){
				 dataforplot =(double[])((HashMap)plottedData.get(  keymasterList.get(k) )).get("YBeta"); 
				}
			if(betaplottype == 3){
				 dataforplot =(double[])((HashMap)plottedData.get(  keymasterList.get(k) )).get("XPhase"); 
				}
			if(betaplottype == 4){
				 dataforplot =(double[])((HashMap)plottedData.get( keymasterList.get(k) )).get("YPhase"); 
				}  
			                                                                 
			BasicGraphData betagraphdata = new BasicGraphData();
			
			betagraphdata.setGraphColor((Color)((HashMap)plottedData.get( keymasterList.get(k) )).get("color") );
                        betagraphdata.addPoint((double[])((HashMap)plottedData.get( keymasterList.get(k) )).get("BPMpos"),dataforplot);
                        betagraphdata.setDrawPointsOn(true);
                        betagraphdata.setDrawLinesOn(true);
                        betagraphdata.setGraphProperty("Legend", new String("raw data"));
                        betapanel.addGraphData(betagraphdata); 
		}
	}

	betapanel.setLegendButtonVisible(true);
	betapanel.setChooseModeButtonVisible(true);	
    } 

	public void exportData(File file) throws IOException{
		OutputStream fout = new FileOutputStream(file);
		String line = new String(""); 
		for(int k =0;k < keymasterList.size(); k++){
			if( (Boolean)plottable.getValueAt(k,5) == true ) {
				String name = (String)plottable.getValueAt(k,0);
				String turns = (String)plottable.getValueAt(k,1);
				String badbpms = (String)plottable.getValueAt(k,2);
				String missingbpms = (String)plottable.getValueAt(k,3);
				double[] xbeta;
				double[] ybeta;
				double[] xphase;
				double[] yphase;
				double[] bpmpos;
				xbeta = (double[])((HashMap)plottedData.get(keymasterList.get(k))).get("XBeta");
				ybeta = (double[])((HashMap)plottedData.get(keymasterList.get(k))).get("YBeta"); 
				xphase = (double[])((HashMap)plottedData.get(keymasterList.get(k))).get("XPhase");
				yphase = (double[])((HashMap)plottedData.get(keymasterList.get(k))).get("YPhase"); 
				bpmpos = (double[])((HashMap)plottedData.get(keymasterList.get(k) )).get("BPMpos");
				line = line + "%\n% Data filename " + name + "; Turns used: " + turns + "; bad bpms identified: " + badbpms + "; missing bpms: " + missingbpms + "\n";
				line = line + "% BPM Pos \t X Beta \t Y Beta \t X Phase \t Y Phase \n"; 
				for(int i=0; i<xbeta.length; i++){
					line = line + bpmpos[i] + "\t" + xbeta[i] + "\t" + ybeta[i] +  "\t" + xphase[i] + "\t" + yphase[i] + "\n";
					}	
				}
			}
		byte buf[] = line.getBytes();
		fout.write(buf);
		fout.close();
		}
		
    
//this routine runs through the data for each BPM and takes notes if many of the values are very small.	
	public void BadBPMfinder(){
		double zeroo = 0.0005;
		int i=0;
		badBPMlist.clear();
		missingBPMlist.clear();
		numturns = (int)numberturns.getDoubleValue();	//ensures that analysis uses latest user-entered turn count
		int zeroValuedDataCounter = 0;
		for(int j = 0; j < numBPMs; j++){
			zeroValuedDataCounter = 0;
			for(int k = 0; k < numturns; k++){
				if( bpmXData.get(shortenedBPMlist.get(j)) != null &&
					 zeroo > ((bpmXData.get(shortenedBPMlist.get(j))).get(k)) &&
					 -zeroo < ((bpmXData.get(shortenedBPMlist.get(j))).get(k))  ) { //checks if BPM data has many very small values
					zeroValuedDataCounter++;
				}

			}
			if(bpmXData.get(bpmlist.get(j).toString().substring(14)) == null && !missingBPMlist.contains(bpmlist.get(j).toString().substring(14)) ) {
				  missingBPMlist.add(bpmlist.get(j).toString().substring(14) );
				  BPMtable.setValueAt(false,j, 2); //deselects the boolean in table each time filter is run and finds a missing BPM
				  bpmtablemodel.fireTableDataChanged();
				}
			//System.out.println(zeroValuedDataCounter);
			if(zeroValuedDataCounter > numturns*(0.5) ){
				if( !badBPMlist.contains(bpmlist.get(j).toString().substring(14)) ){ //make sure the bad BPM doesn't get added multiple times
					badBPMlist.add( bpmlist.get(j).toString().substring(14)  ); //adds BPM to arraylist of bad BPMs
					System.out.println("BPM " + bpmlist.get(j).toString().substring(14) + " has data with many small values."); //tells user that a BPM has particular kind of bad data
					}
					BPMtable.setValueAt(false,j, 2); //deselects the boolean in table each time filter is run and finds a bad BPM
					bpmtablemodel.fireTableDataChanged();
			}
		}
		
		if(badBPMlist.size() == 1) {
		badBPMlabel.setText("BPM " + badBPMlist + " is bad. Has been deselected in table."); //update Jlabel above plot with active file name
		SwingUtilities.updateComponentTreeUI(badBPMlabel);
		}
		if(badBPMlist.size() > 1) {
		badBPMlabel.setText("BPMs " + badBPMlist + " are bad. Has been deselected in table."); //update Jlabel above plot with active file name
		SwingUtilities.updateComponentTreeUI(badBPMlabel);
		}
		if(badBPMlist.size() < 1) {
		badBPMlabel.setText("No BPMs with mostly Zero Data"); //update Jlabel above plot with active file name
		SwingUtilities.updateComponentTreeUI(badBPMlabel);
		}
		
		if(missingBPMlist.size() == 1) {
		missingBPMlabel.setText("BPM " + missingBPMlist + " is missing: Has been deselected.");
		SwingUtilities.updateComponentTreeUI(missingBPMlabel);
		}
		if(missingBPMlist.size() > 1) {
		missingBPMlabel.setText("BPMs " + missingBPMlist + " are missing: Have been deselected."); 
		SwingUtilities.updateComponentTreeUI(missingBPMlabel);
		}
		if(missingBPMlist.size() < 1) {
		missingBPMlabel.setText("No BPMs Missing Data"); 
		SwingUtilities.updateComponentTreeUI(missingBPMlabel);
		}	
 }  



} //end of class Twissface
	
	



