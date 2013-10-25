/*************************************************************
//
// class StoredResults:
// This class is responsible for the Graphic User Interface
// components and action listeners for stored results.
//
/*************************************************************/

package xal.app.bsmanalysis;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.JOptionPane;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer.*;
import java.net.URL;
import java.util.List;
import java.io.*;
import java.io.File.*;
import javax.swing.text.PlainDocument;
import java.lang.*;
import xal.extension.widgets.swing.*;
import xal.tools.statistics.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.apputils.files.RecentFileTracker;
import xal.extension.widgets.plot.*;
import xal.tools.data.*;
import xal.extension.fit.lsm.*;
import java.text.NumberFormat;
import xal.tools.messaging.*;
import xal.ca.*;

public class StoredResultsPanel extends JPanel{
    
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    public JPanel mainPanel;
    public JTable datatable;
    public DataTableModel datatablemodel;
    private ArrayList<String> tablelist = new ArrayList<String>();
    private JFileChooser fc;
   
    private JButton refreshbutton;
    private JButton plotbutton;
    private JButton exportbutton;
   
    GenDocument doc;
    EdgeLayout layout = new EdgeLayout();
    FunctionGraphsJPanel datapanel; 
    JScrollPane datascrollpane;
    
    private ArrayList<Integer> bsms;
    private HashMap<String, Object> map;
    
    //Plot chooser
    private  boolean linearplot = true;
    private  String[] plottypes = {"Plot Linear Values", "Plot Log Values"};
    private  JComboBox<String> scalechooser = new JComboBox<String>(plottypes);
    
    public StoredResultsPanel(){}
    //Member function Constructor

    public StoredResultsPanel(GenDocument aDocument){	
	doc=aDocument;

	makeComponents(); //Creation of all GUI components
	addComponents();  //Add all the components to the layout and panel
	setAction();      //Set the action listeners	
    }
    
    
    //Creation of all GUI components
    public void makeComponents(){
	mainPanel = new JPanel();
	mainPanel.setPreferredSize(new Dimension(500, 455));
	mainPanel.setBorder(BorderFactory.createTitledBorder("View Stored Results"));

	datapanel = new FunctionGraphsJPanel();
	datapanel.setPreferredSize(new Dimension(375, 235));
	datapanel.setGraphBackGroundColor(Color.WHITE);
	
	refreshbutton = new JButton("Refresh Table");
	plotbutton = new JButton("Plot Selected Profiles");
	exportbutton = new JButton("Export");
	
	fc = new JFileChooser();
	
	makeDataTable();
    }
    
    
    //Creates the table
    public void makeDataTable(){
	String[] colnames = {"File Name", "BSM", "Sigma/RMS", "Plot"}; 
	datatablemodel = new DataTableModel(colnames, 0);
	
	datatable = new JTable(datatablemodel);
	datatable.getColumnModel().getColumn(0).setMinWidth(175);
	datatable.getColumnModel().getColumn(1).setMinWidth(50);
	datatable.getColumnModel().getColumn(2).setMinWidth(50);
	datatable.getColumnModel().getColumn(3).setMinWidth(17);
	
	datatable.setRowSelectionAllowed(false);
	datatable.setColumnSelectionAllowed(false);
	datatable.setCellSelectionEnabled(false);
	datatable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	
	datascrollpane = new JScrollPane(datatable);
	datascrollpane.setColumnHeaderView(datatable.getTableHeader());
	datascrollpane.setPreferredSize(new Dimension(403, 100));
    }
    
    
    //Add all the components to the layout and panels
    public void addComponents(){
	EdgeLayout layout = new EdgeLayout();
	mainPanel.setLayout(layout);
	layout.add(datapanel, mainPanel, 10, 15, EdgeLayout.LEFT);
	layout.add(refreshbutton, mainPanel, 10, 260, EdgeLayout.LEFT);
	layout.add(plotbutton, mainPanel, 10, 410, EdgeLayout.LEFT);
	layout.add(exportbutton, mainPanel, 385, 410, EdgeLayout.LEFT);
	layout.add(scalechooser, mainPanel, 200, 410, EdgeLayout.LEFT);
	layout.add(datascrollpane, mainPanel, 10, 290, EdgeLayout.LEFT);
	
	this.add(mainPanel);
   }
   
    
    public void setAction(){
	    refreshbutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				  refreshTable();  //Fills the table with the saved results (the results that have not already been displayed)
			    }			
	    });
	    
	    //Plots the data for the files with the plot box checked
	    plotbutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				  getArrayList();
				  plotData(bsms);
			    }
	    });
	    
	    scalechooser.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				    getArrayList();
				    plotData(bsms); 
			    }
	    });
	    
	    exportbutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				    int returnValue = fc.showSaveDialog(StoredResultsPanel.this);
				    if(returnValue == JFileChooser.APPROVE_OPTION){
					    File file = fc.getSelectedFile();
					    try{
						    writeMatLabFile(file);
					    }
					    catch(IOException ioe){
					    }
				    }
				    else{
					    System.out.println("Save command canceled by user.");
				    }   
			    }			
	    });
	    
    }
    
    
    //Calculates the ArrayList for plotting data
    public void getArrayList(){
	    int nrows = datatablemodel.getRowCount();
	    bsms = new ArrayList<Integer>();
	    for(int i = 0; i < nrows; i++){
		  if(((Boolean)datatable.getValueAt(i, 3)).booleanValue() == true){         
			  bsms.add(new Integer(i));
		  }
	    }
}


    //Plots the data and the fit (if there is a fit)
    public void plotData(ArrayList<Integer> bsms){
	    int colorindex = 1;
	    datapanel.removeAllGraphData();
	  
	    Iterator<Integer> itr = bsms.iterator(); //iterate through all of the files that have the plot box checked
	    while(itr.hasNext()){
		     int rownumber = (itr.next()).intValue();
		     String nameOfFile = (String)datatable.getValueAt(rownumber, 0);
		    
		     map = doc.resultMap.get(nameOfFile); //get the HashMap associated with the current file
		     int length = ((Integer)map.get("length")).intValue(); 
		     boolean fit = ((Boolean)map.get("fit")).booleanValue();
		     double[] xvalues = new double[length];
		     double[] yvalues = new double[length];
		     xvalues = (double[])map.get("xvalues");
		     yvalues = (double[])map.get("yvalues");
		     
		     linearplot = (Boolean)map.get("linearplot");
			    if(scalechooser.getSelectedIndex()==0)
				    linearplot = true;
			    else
				    linearplot = false;
			    
		    BasicGraphData graphdata = new BasicGraphData();
		    
	            if(!linearplot){
			double temp;
			double[] logdata = new double[yvalues.length];
			for(int i = 0; i < logdata.length; i++){
				temp = yvalues[i];
				if(temp <= 0.0)
					temp = 0.00001;
				logdata[i] = Math.log(temp)/Math.log(10);
			}
			graphdata.addPoint(xvalues, logdata);    
		    }
		    
		    else
			    graphdata.addPoint(xvalues, yvalues);
		    
		     graphdata.setDrawPointsOn(true);
		     graphdata.setDrawLinesOn(true);
		     graphdata.setGraphProperty("Legend", new String(nameOfFile));
		     datapanel.setLegendButtonVisible(true);
		     graphdata.setGraphColor(IncrementalColors.getColor(colorindex));
		     datapanel.addGraphData(graphdata);
		     colorindex++;
		     
		     if(fit){
			    BasicGraphData fitgraphdata = new BasicGraphData();
			    
			    //get the fit parameters from map
			    double phase = ((Double)map.get("phase")).doubleValue(); 
			    double center = ((Double)map.get("center")).doubleValue(); 
			    double amp = ((Double)map.get("amp")).doubleValue();
			    double sigma = ((Double)map.get("sigma")).doubleValue();
			   
			    double xmin = center - 5*sigma;
			    double xmax = center + 5*sigma;
			    double points = 100.0;
			    double inc = (xmax - xmin)/points;
			    int npoints = (new Double(points)).intValue();
			    double xfit[] = new double[npoints];
			    double yfit[] = new double[npoints];
	  
			    //Calculate the x and y coordinates for gaussian fit
			    int i = 0;
			    double x = xmin;
			    while(x <= xmax && i < npoints){
				    xfit[i] = x;
				    yfit[i] = amp*Math.exp(-(x-center)*(x-center)/(2.0*sigma*sigma*.7)); 
				    x += inc;
				    i++;
			    }
	   
			    
			    
			     if(!linearplot){
				     double temp;
				     double []ylogfit = new double[yfit.length];
				     for(int j = 0; j < ylogfit.length; j++){ 
					     temp=yfit[j];
					     if(temp<=0.0) temp=0.00001;
					     ylogfit[j] = Math.log(temp)/Math.log(10);
				     }
				     fitgraphdata.addPoint(xfit, ylogfit);
			     }
			     else{
				     fitgraphdata.addPoint(xfit, yfit);
			     }
			    
			    fitgraphdata.setDrawPointsOn(false);
			    fitgraphdata.setDrawLinesOn(true);
			    fitgraphdata.setGraphProperty("Legend", new String("fit data"));
			    fitgraphdata.setGraphColor(Color.black);
	    
			    datapanel.addGraphData(fitgraphdata);
		     }
	    }
    }
    
    
    //Fills the table with the saved results
    public void refreshTable(){
	    if(doc.resultMap.size() != 0){
		    String tempfilename; 
		    String bsmname;
		    double sigma_rms;

		    Set<String> keys = doc.resultMap.keySet();
		    Iterator<String> itr = keys.iterator();
		    
		    while(itr.hasNext()){ //iterate through each file
			    tempfilename = itr.next();
			    bsmname = (String)((HashMap)doc.resultMap.get(tempfilename)).get("name");
			    Boolean statrms = (Boolean)((HashMap)doc.resultMap.get(tempfilename)).get("statrms");
			   if(statrms)
				    sigma_rms = ((Double)((HashMap)doc.resultMap.get(tempfilename)).get("rms")).doubleValue();
			   else
				    sigma_rms = ((Double)((HashMap)doc.resultMap.get(tempfilename)).get("sigma")).doubleValue();
			    
			    toTable(tempfilename, sigma_rms, bsmname);	
		    }
	    }
	    else{
		    datatablemodel.clearAllData();
		    tablelist.clear();
		    datapanel.removeAllGraphData();
		    System.out.println("There are no saved result files.  ");
	    }    
    }
    
    
    //Transfers the file name, sigma value, and name of bsm to table
    public void toTable(String nameOfFile, Double sig, String nameOfbsm){
	    ArrayList<Object> tabledata = new ArrayList<Object>();
	    tabledata.add(nameOfFile);
	    tabledata.add(nameOfbsm);
	    tabledata.add(sig);
	    tabledata.add(new Boolean(false));
	    if(!tablelist.contains(nameOfFile)){ //add the file to table
		    tablelist.add(nameOfFile);
		    datatablemodel.addTableData(new ArrayList<Object>(tabledata));
		    datatablemodel.fireTableDataChanged();
	    }
	    else{
		     int nrows = datatablemodel.getRowCount(); //the file is already in the table so it updates the RMS/sigma value
		     for(int i = 0; i < nrows; i++){
			     if(((String)datatable.getValueAt(i, 0)) == nameOfFile){
				     datatablemodel.setValueAt(sig, i, 2);
				     datatablemodel.fireTableDataChanged();
			     }
		     }
	    }
    }
    
    
    public void writeMatLabFile(File file) throws IOException{
	    if(doc.resultMap.isEmpty()){
		    System.out.println("No data available to write!");
	    }
	    else{
		    OutputStream fout = new FileOutputStream(file);
		    String tempfilename;
		    String bsmname;
		    HashMap<String, Object> map = new HashMap<String, Object>();
		 
		    Set<String> keys = doc.resultMap.keySet();
		    Iterator<String> itr = keys.iterator();
		    
		    while(itr.hasNext()){
			    tempfilename = itr.next();
			    map = doc.resultMap.get(tempfilename);
			    bsmname = (String)map.get("name");
		    
			    String line = "%  " + tempfilename + ":BSM " + bsmname + "\n";
		    
			    int length = ((Integer)map.get("length")).intValue();
			    double[] xvalues = new double[length];
			    double[] yvalues = new double[length];
			    xvalues = (double[])map.get("xvalues");
			    yvalues = (double[])map.get("yvalues");
		    
			    line = line + xvalues.length + "\t" + yvalues.length + "\n";
			    for(int i=0; i<xvalues.length; i++){
				    line = line + xvalues[i] + "\t" + yvalues[i] + "\n";
			    }
		  
			    byte buf[] = line.getBytes();
			    fout.write(buf);
		    }
		    fout.close();
	    }
    }
    
}
