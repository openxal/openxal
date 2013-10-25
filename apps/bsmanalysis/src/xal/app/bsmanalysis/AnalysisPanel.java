/*
* AnalysisPanel.java
*
*/

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
import java.lang.*;
import static java.lang.Math.*;
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
import xal.extension.solver.*;
//import xal.tools.formula.*;
import xal.extension.solver.hint.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.market.*;
import xal.extension.solver.solutionjudge.*;

public class AnalysisPanel extends JPanel{
    
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    public JPanel mainPanel;
    public JTable datatable;
    public FunctionGraphsJPanel datapanel; //originally private
    private ColorSurfaceData imagedata;
    GenDocument doc;
    public FileSummaryPanel summarypanel; 
    
    private String filename;
    private  String bsmname;
    private  HashMap<String, Object> map; //hash map for the chosen filename
    private ArrayList<ArrayList<Object>> data; //Arraylist of arraylists in hash map
    private  double[] yvalues; //y-coordinates (before noise is subtracted)
    private  double[] currentyvalues; //yvalues with noise subtracted
    private  double[] logdata; //y-coordinates for log plot
    private  double[] xphase; //x-coordinates for phase scaled with respect to reference phi
    private  double[] x1; //x-coordinates for phase, not scaled to reference phi
    private  double[] distr; //normalized distribution
    private  double[] xfit; //Gaussian fit data (x-values)
    private  double[] yfit; //Gaussian fit for linear data (y-values)
    private  double[] ylogfit; //Gaussian fit for log data (y-values)
    private  double dx; //stepsize from file	
    private  double phi0; //reference phi - different for each file
    private  int t1; //start of time range
    private  int t2; //end of time range
    private  boolean dataHasBeenFit = false; //whether or not a Gaussian fit has been added
    private  boolean norm = false; //normalized data
    private  boolean statrms = false; //statistical RMS vs. sigma
    private  boolean table2 = false; //specifies between summarypanel and analysispanel calculations
    private  boolean plot = false; //whether or not the data has been plotted ("Get Mean Plot" button)
  
    //Panels
    private JPanel timepanel;
    private JPanel noisepanel; 
    private JPanel signalpanel;
    private JPanel rmspanel;
    private JPanel resultpanel;
   
    //Labels
    private JLabel time1label;
    private JLabel time2label;
    private JLabel noiselabelstart;
    private JLabel noiselabelend;
    private JLabel noiselabel;
    private JLabel signallabel;
    private JLabel[] rlabel = new JLabel[3];
    
    //Buttons
    private JButton getnoisebutton; 
    private JButton getmeanbutton;
    private JButton getrmsbutton;
    private JButton removebutton;
    private JButton normbutton;
    private JButton fitbutton;
    private JButton storebutton;
    private JButton clearstorebutton;
	private JButton hoffsetbutton;
    
    //Decimal Fields
    private  DecimalField time1;
    private  DecimalField time2;
    private  DecimalField noisestart;
    private  DecimalField noiseend;
    private  DecimalField noiseresult; 
    private  DecimalField signalthresh;
    private  DecimalField rmsresult;
	private  DecimalField hoffset;
    private  DecimalField[] result = new DecimalField[3]; 
    
    //Plot chooser
    private  boolean linearplot = true;
    private  String[] plottypes = {"Plot Linear Values", "Plot Log Values"};
    private  JComboBox<String> scalechooser = new JComboBox<String>(plottypes);
   
    //Number Formats
    private NumberFormat numFor1 = NumberFormat.getNumberInstance();		
    private NumberFormat numFor2 = NumberFormat.getNumberInstance();
    private NumberFormat numFor3 = NumberFormat.getNumberInstance();
    
    //Guess parameters for Gaussian Fit
    private  double xav; 
    private  double rms = 0.0; //statistical rms 
    private  double max; 
    
    //Gaussian Fit parameters
    private  double amp = 0.0; 
    private  double sigma = 0.0; 
    private  double center = 0.0;
    
    public AnalysisPanel(){}
    //Member function Constructor
    
    public AnalysisPanel(GenDocument aDocument, DataTableModel dtm){
	doc=aDocument;
	
	makeComponents(); //Creation of all GUI components
	addComponents();  //Add all components to the layout and panels
	setAction();      //Set the action listeners	
    }
    
    
    //Creation of all GUI components
    public void makeComponents(){ 
	mainPanel = new JPanel();
	mainPanel.setPreferredSize(new Dimension(500, 620));
	mainPanel.setBorder(BorderFactory.createTitledBorder("BSM Analysis"));
	
	summarypanel = new FileSummaryPanel(doc);
	
	datapanel = new FunctionGraphsJPanel();
	datapanel.setPreferredSize(new Dimension(390, 300));
	datapanel.setGraphBackGroundColor(Color.WHITE);
	datapanel.setAxisNameX("Phase ");
	
	numFor1.setMaximumFractionDigits(0);
	numFor2.setMinimumFractionDigits(6);
	numFor3.setMinimumFractionDigits(4);

	//time panel
	timepanel = new JPanel();
	time1label = new JLabel("Time 1");
	time1 = new DecimalField(0, 3, numFor1);
	time2label = new JLabel("to time 2");
	time2 = new DecimalField(0, 3, numFor1);
	
	timepanel.add(time1label);
	timepanel.add(time1);
	timepanel.add(time2label);
	timepanel.add(time2);
	
	//Noise	panel
	noisepanel = new JPanel();
	noiselabelstart = new JLabel("Noise range from ");
	noisestart = new DecimalField(1, 4, numFor1);
	noiselabelend = new JLabel("to");
	noiseend = new DecimalField(10, 4, numFor1);
	noiselabel = new JLabel("  ");
	getnoisebutton = new JButton("Get Noise");
	noiseresult = new DecimalField(0.0, 6, numFor2);
	
	noisepanel.add(noiselabelstart);
	noisepanel.add(noisestart);
	noisepanel.add(noiselabelend);
	noisepanel.add(noiseend);
	noisepanel.add(noiselabel);
	noisepanel.add(getnoisebutton);
	noisepanel.add(noiseresult);
	
	//Signal threshold panel
	signalpanel = new JPanel();
	signallabel = new JLabel("Signal Level (Fraction of Maximum)");
	signalthresh = new DecimalField(.01, 5, numFor3);
	signalpanel.add(signallabel);
	signalpanel.add(signalthresh);
	
	//RMS panel
	rmspanel = new JPanel();
	getrmsbutton = new JButton("Get RMS");
	rmsresult = new DecimalField(0.0, 5, numFor3);
	rmspanel.add(getrmsbutton);
	rmspanel.add(rmsresult);
	
	//Additional buttons and fields
	getmeanbutton = new JButton("Get Mean Plot");
	removebutton = new JButton("Remove Point");
	normbutton = new JButton("Normalize by Area");	
	fitbutton = new JButton("Fit with Gaussian");
	storebutton = new JButton("Store Results");
	clearstorebutton = new JButton("Clear Stored Results");
	hoffsetbutton =  new JButton("H Offset by: ");
	hoffset = new DecimalField(0, 3, numFor1);
	
	//Result panel
	resultpanel = new JPanel();
	resultpanel.setBorder(BorderFactory.createTitledBorder("Fit Results"));
	resultpanel.setLayout(new GridLayout(3, 2));
	
	rlabel[0] = new JLabel("   Center");
	rlabel[1] = new JLabel("Sigma/RMS");
	rlabel[2] = new JLabel("Amplitude");
	
	for(int i = 0; i < 3; i++){
		result[i] = new DecimalField(0, 2, numFor3);
	}
	resultpanel.add(rlabel[0]);resultpanel.add(result[0]);
	resultpanel.add(rlabel[1]);resultpanel.add(result[1]);
	resultpanel.add(rlabel[2]);resultpanel.add(result[2]);
    }
    
    
    //Add all components to the layout and panels
    public void addComponents(){
	EdgeLayout layout = new EdgeLayout();
	mainPanel.setLayout(layout);
	
	layout.add(datapanel, mainPanel, 10, 15, EdgeLayout.LEFT);
	layout.add(timepanel, mainPanel, 10, 320, EdgeLayout.LEFT);
	layout.add(noisepanel, mainPanel, 10, 345, EdgeLayout.LEFT);
	layout.add(signalpanel, mainPanel, 10, 375, EdgeLayout.LEFT);
	layout.add(getmeanbutton, mainPanel, 10, 410, EdgeLayout.LEFT);
	layout.add(scalechooser, mainPanel, 10, 440, EdgeLayout.LEFT);
	layout.add(rmspanel, mainPanel, 10, 465, EdgeLayout.LEFT);
	layout.add(removebutton, mainPanel, 10, 500, EdgeLayout.LEFT);
	layout.add(hoffsetbutton, mainPanel, 10, 530, EdgeLayout.LEFT);
	layout.add(hoffset, mainPanel, 150, 530, EdgeLayout.LEFT);
	layout.add(normbutton, mainPanel, 10, 560, EdgeLayout.LEFT);
	layout.add(fitbutton, mainPanel, 250, 410, EdgeLayout.LEFT);	
	layout.add(resultpanel, mainPanel, 250, 450, EdgeLayout.LEFT);
	layout.add(storebutton, mainPanel, 80, 590, EdgeLayout.LEFT);
	layout.add(clearstorebutton, mainPanel, 230, 590, EdgeLayout.LEFT);
	
	this.add(mainPanel);
   }
    
   
    //Set the action listeners	
    public void setAction(){
	    getnoisebutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				    calcNoise(); //calculates and displays the noise for the specified time range, it should be pushed after every change in time range or noise range
			    }			//calculates the noise from the data in the FILE - including points that may have been previously removed	
	    });
	    
	    getmeanbutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				    plot = true;
				    norm = false;
				    if(scalechooser.getSelectedIndex()==0) 
					    linearplot = true;
				    else
					    linearplot = false;
				   dataHasBeenFit = false; //any fit will be removed
				   getYValues(); //gets the data from the FILE - including points that may have been previously removed
				   calcMeanPlot(); //calculates the two 1D arrays for plotting - this function only has to be called after noise, signal, and time range changes
				   plotData();
				   
				   //fit parameters are removed since the fit is removed
				   result[0].setValue(0.0); 
				   result[1].setValue(0.0); 
				   result[2].setValue(0.0); 
			    }			
	    });
	    
	    scalechooser.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				    plot = true;
				    norm = false;
				    if(scalechooser.getSelectedIndex()==0)
					    linearplot = true;
				    else
					    linearplot = false;
				   if(dataHasBeenFit){
					   calcRMS();
					   gaussFit();
				   }
				   plotData(); 
			    }
	    });
	    
	    getrmsbutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				    statrms = true;
				    if(norm)
					    normalize();
				    calcRMS(); //calculates and displays the statistical RMS value - uses the data that has been adjusted by noise and removal of points
			    }
	    });
	    
	    removebutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				    removePoint(); //removes the specified point from the plot - also removes any gaussian fit
			    }
	    });
	    
	    normbutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				   plot = true;
				   norm = true;
				   normalize();
				   if(dataHasBeenFit){
					   calcRMS();
					   gaussFit();
				   }
				   plotData();
			    }				
	    });
		
		hoffsetbutton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
						offset();
						plotData();
					}				
				});
		
	    
	    fitbutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				    plot = true;
				    dataHasBeenFit = true;
				    statrms = false;
				    getMax(); //gets the amplitude guess for fit calculation
				    if (norm)
					    normalize();
				    calcRMS(); //calculates the statistical RMS value (does not display value)
				    gaussFit(); //computer finds the best fit
				    plotData(); 
			    }
	    });
	    
	    storebutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				  storeResult(); //store analysis result in "doc.resultMap" for results panel access
			    }
	    });   
	    
	    clearstorebutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				    clearStoreResult(); //removes all results from "doc.resultMap" - thus results are NOT accesible from the results panel 
			    }
	    });   
    }
    
     @SuppressWarnings ("unchecked") // The value of map has multiple types. Must suppress unchecked cast warning.
    public  void resetCurrentData(String file){		
	    filename = file;
	    

	    if(doc.masterDataMap.containsKey(filename)){
		 map = new HashMap<String, Object>();
		 map = doc.masterDataMap.get(filename);
		 
		 data =(ArrayList<ArrayList<Object>>) map.get("data");//Get bsmdata (an ArrayList filled with ArrayLists)
		 phi0 = ((Double)map.get("phase")).doubleValue(); //get reference phase
		 bsmname = (String)map.get("name"); //get bsm name (for datapanel title)
		 dx = ((Double)map.get("stepsize")).doubleValue();
	
		 plot = false; 
		 
		 setDefaults(); //set the decimalfields to default values
		 calcNoise(); //calculates the noise given the default range
	    }
    }
    
    
    //Set the decimalfields to default values
    public  void setDefaults(){  
		 table2 = false;
		 double end = 10*dx + phi0;
		 time1.setValue(40);
		 time2.setValue(50);
		 noisestart.setValue(1);
		 noiseend.setValue(end);
		 noiseresult.setValue(0);
		 signalthresh.setValue(0.01);
		 rmsresult.setValue(0.0);
		 result[0].setValue(0.0);
		 result[1].setValue(0.0);
		 result[2].setValue(0.0);
    }
    
    
    //Plots the 2D color image
    public void plotDataImage(){	
	    datapanel.removeAllGraphData();
	    
	    int sizeX = data.size();
	    int sizeY = ((ArrayList)data.get(0)).size();
	    
	    imagedata = Data3DFactory.getDefaultData3D(sizeX, sizeY); 
	    double minX = dx + phi0;
	    double maxX = ((double)sizeX)*dx + phi0; 
	    imagedata.setMinMaxX(minX, maxX);
	    imagedata.setMinMaxY(0,(double)sizeY);
	    imagedata.setScreenResolution(sizeX, sizeY);
	   
	    for(int x = 0; x < sizeX; x++){ 
		for(int y = 0; y < sizeY; y++){ 
		    double v = ((Double)((ArrayList)data.get(x)).get(y)).doubleValue();
		    imagedata.setValue(x,y,v);
		}
	    }
	    datapanel.setAxisNameY("Time Steps");
	    datapanel.setColorSurfaceData(imagedata);
	    datapanel.setOffScreenImageDrawing(true);
	    datapanel.setGridLinesVisibleY(false);
	    datapanel.setGridLinesVisibleX(false);
	    datapanel.setName("BSM " + bsmname);
    }
    
    
    //Extracts data from the time interval specified by t1 and t2,  
    public void getYValues(){ 
	    if(!table2){
		    t1 = (int)time1.getDoubleValue();
		    t2 = (int)time2.getDoubleValue();
	    }
	    Double sum = 0.0; 	
	    int sizeX = data.size();
	    int sizeY = ((ArrayList)data.get(0)).size(); 
	    yvalues = new double[sizeX];
	    t1 = t1 - 1;
	    t2 = t2 - 1;
	    
	    if(!table2){
		    //If a number is entered that is out of range, the limit will appear in the decimal field 
		    //the limit will then be used in the calculations
		    if(t1 < 0 || t1 > (sizeY - 1)){
			    System.out.print("Initial time is out of range.");
			    if(t1 < 0){
			    	time1.setValue(1); t1 = 0; }
			    else if (t1 > (sizeY - 1)){
			    	time1.setValue(sizeY); t1 = sizeY - 1; }
		    }
		    if(t2 < 0 || t2 > (sizeY - 1)){
			    System.out.print("Final time is out of range.");
			    if(t2 < 0){
			    	time2.setValue(1); t2 = 0; }
			    else if (t2 > (sizeY - 1)){
			    	time2.setValue(sizeY); t2 = sizeY - 1; }
		    }
	    }
	    
	    for(int i = 0; i < sizeX; i++){ 
		    for(int j = t1; j <= t2; j++){ //includes t1 and t2
			    sum = sum + ((Double)((ArrayList)data.get(i)).get(j)).doubleValue();
		    }
		    yvalues[i] = sum.doubleValue()/(abs(t2 - t1) + 1); //finds the mean for each phase (column)
		    sum = (Double)0.0;
	    }
    }
    
    
    //Finds the noise over the specifed range - calculated from the original data values including any points that may have been removed
    public  void calcNoise(){
	    getYValues();
	    int sizeX = yvalues.length;
		double startinputscaled = (double)noisestart.getValue();
		double endinputscaled = (double)noiseend.getValue();

		Double startinputnorm = new Double((startinputscaled - phi0)/dx);
		Double endinputnorm = new Double((endinputscaled - phi0)/dx);
			
		int startinput = startinputnorm.intValue(); //get the initial point of the noise range
	    int endinput = endinputnorm.intValue(); //get the final point of the noise range
		
		startinput = startinput - 1;
	    endinput = endinput - 1;
	    
	    //If a number is entered that is out of range, the limit will appear in the decimal field and that limit will be used in the calculations
		
	    if(startinput < 0 || startinput > (sizeX - 1)){
		    System.out.print("Initial noise input is out of range.");
		    if(startinput < 0){
		    	noisestart.setValue(1); startinput = 0; }
		    else if(startinput > (sizeX - 1)){
			    noisestart.setValue(sizeX); startinput = sizeX - 1; }
	    }
	    if(endinput < 0 || endinput > (sizeX - 1)){
		    System.out.print("Final noise input is out of range.");
		    if(endinput < 0){
			    noiseend.setValue(1); endinput = 0; }
		    else if(endinput > (sizeX - 1)){
			    noiseend.setValue(sizeX); endinput = sizeX - 1;}
	    }		
	    
	    double sum = 0.0;
	    for(int i = startinput; i <= endinput; i++){ //includes the endpoints
		    sum = sum + yvalues[i];
	    }
	    double noise = sum/(endinput - startinput + 1); //find mean of noise
	    
	    noiseresult.setValue(noise); //output result to the decimalfield
    }    
    
    
    //Finds the maximum of currentyvalues
    public void getMax(){
	int templength = currentyvalues.length;
	double[] temparray = new double[templength];
	
	for(int i = 0; i < templength; i++){
		temparray[i] = currentyvalues[i];	
	}
	
	Arrays.sort(temparray);
	max = temparray[templength - 1]; //amplitude guess for fit calculation
    }
    
    
    //Calculates the two 1D arrays required for plotting
    public  void calcMeanPlot(){
	   double thresh = signalthresh.getDoubleValue(); //gets the integration threshold (initially 0.01)
	   double noise = (double)noiseresult.getValue(); //gets the value in the nosie result box - does not recalculate the noise
	   			      	
	   int sizeX = yvalues.length;
	   double[] temp = new double[sizeX];
	   double[] noNoise = new double[sizeX];
	  
	   for(int i = 0; i < sizeX; i++){ 
		    noNoise[i] = yvalues[i] - noise; //Subtract noise from all the y-values
		    temp[i] = noNoise[i];
	    }

	   Arrays.sort(temp);
	   max = temp[sizeX - 1]; //amplitude guess 
	   
	    int count = 0;
	    for(int i = 0; i < sizeX; i++){
		    if(noNoise[i] > (thresh * max)) //Determine how many points exist above the threshold
			    count++;
	    }
	
	    currentyvalues = new double[count];
	    xphase = new double[count];
	    x1 = new double[count];
	    double x;
	    int j = 0;
	    
	    //Delete the values that are below the threshold from the x and y-values
	    for(int i = 0; i < sizeX; i++){
		x = (double)(i+1)*dx;
		if(noNoise[i] > (thresh * max)){
			currentyvalues[j] = noNoise[i];
			x1[j] = x; 
			xphase[j] = x1[j] + phi0;
			j++;
		}
	    }
    }
    
    
    //Calculates the RMS value
    public  void calcRMS(){
	    int sizeX = currentyvalues.length;
				
	    //Perform Integral
	    double sum = 0.0;
	    for(int i = 0; i < sizeX; i++){
		    sum = sum + currentyvalues[i];
	    }
	    double intgr = sum * dx;
	    
	     //Normalize the distribution
	    distr = new double[sizeX];
	    double[] temparray = new double[sizeX]; 
	    
	    for(int i = 0; i < sizeX; i++){
		distr[i] = (currentyvalues[i]/intgr);
		temparray[i] = distr[i];
	    }
	    
	    //Find max of normalized distribution 
	    Arrays.sort(temparray);
	    double maximum = temparray[sizeX - 1]; 
	
	    if (norm)
		    max = maximum; //amplitude guess for normalized distribution 
	    
	    for(int i = 0; i < xphase.length; i++){
		x1[i] = xphase[i] - phi0;    
	    } 
	    
	    //Calculate the mean
	    sum = 0.0;
	    double sum2 = 0.0;
	    for(int i = 0; i < sizeX; i++){
		    sum = sum + (x1[i]*distr[i]);
		    sum2 = sum2 + ((x1[i]*x1[i])*distr[i]);
	    }
	    xav = sum * dx; //center guess 
	    double x2av = sum2 * dx; 
	    
	    //calculate the RMS value
	    rms = sqrt(x2av - (xav*xav)); //statistical rms (sigma guess)
	    if(!table2){
		    if(statrms){
			    rmsresult.setValue(rms); //display statistical rms value in decimalfield
		  
			    //Fill the results table
			    result[0].setValue(0.0); 
			    result[1].setValue(rms); 
			    result[2].setValue(0.0); 
		    }
	    }
    }

    
    //Plots the data points and plots the gaussian fit (if a fit has been selected)
    public void plotData(){  
	datapanel.removeAllGraphData();
	datapanel.setColorSurfaceData(null);
	
	BasicGraphData rawgraphdata = new BasicGraphData(); 
	BasicGraphData fitgraphdata = new BasicGraphData();
	
	if(!linearplot){
		double temp;
		logdata = new double[currentyvalues.length];
		for(int i = 0; i < logdata.length; i++){
			temp = currentyvalues[i];
			if(temp <= 0.0)
				temp = 0.00001;
			logdata[i] = Math.log(temp)/Math.log(10);
		}
		rawgraphdata.addPoint(xphase, logdata);
	}
	else
		rawgraphdata.addPoint(xphase, currentyvalues);
	
	rawgraphdata.setDrawPointsOn(true);
	rawgraphdata.setDrawLinesOn(false); 
	rawgraphdata.setGraphProperty("Legend", new String("raw data"));
	rawgraphdata.setGraphColor(Color.RED);
	
	datapanel.addGraphData(rawgraphdata);
	datapanel.setAxisNameY(" ");
	datapanel.setLegendButtonVisible(true);
	datapanel.setChooseModeButtonVisible(true);
	datapanel.setGridLinesVisibleX(true);
	datapanel.setGridLinesVisibleY(true);
	datapanel.setName("BSM " + bsmname);
	
	//Plots the Gaussian Fit
	if( dataHasBeenFit ){
	    center = center + phi0;
	    double xmin = center - 5*sigma;
	    double xmax = center + 5*sigma;
	    double points = 100.0;
	    double inc = (xmax - xmin)/points;
	    int npoints = (new Double(points)).intValue();
	    xfit = new double[npoints];
	    yfit = new double[npoints];
	  
	    int i = 0;
	    double x = xmin;
	    while(x <= xmax && i < npoints){
		    xfit[i] = x;
		    yfit[i] = amp*Math.exp(-(x-center)*(x-center)/(2.0*sigma*sigma*.7)); 
		    x += inc;
		    i++;
	    }
	    
	    //Plots fit for log plot
	    if(!linearplot){
		double temp;
		ylogfit = new double[yfit.length];
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
    
    
    //Removes the point from the plot
    public void removePoint(){
	    Integer index =datapanel.getPointChosenIndex();
	    
	    if(index != null){
		    int newsize = xphase.length - 1;
		    
		    int iindex = index.intValue(); 
		    double[] oldxphase = xphase;
		    double[] oldyvalues = currentyvalues;
		    double[] tempxdata = new double[newsize];
		    double[] tempdata = new double[newsize];
		    
		    for(int i=0; i<newsize; i++){
			    if(i < iindex){
				    tempxdata[i]=oldxphase[i];
				    tempdata[i]=oldyvalues[i];
			    }
			    else{
				    
				    tempxdata[i]=oldxphase[i+1];
				    tempdata[i]=oldyvalues[i+1];
			    }
		    }
		    xphase = tempxdata;
		    currentyvalues = tempdata;
		    dataHasBeenFit = false;
		    plotData();
	    }
    }
    
    
    public  void gaussFit(){
	    System.out.print("Fitting Gaussian \n");
	    
	    double upperAmp = 4*max;
	    double upperSigma = 4*rms;
	    double upperCenter = 4*xav;
	    
	    ArrayList<Variable> variables =  new ArrayList<Variable>();
	    variables.add(new Variable("amp", max, 0, upperAmp)); 
	    variables.add(new Variable("sigma",rms, 0, upperSigma));
	    variables.add(new Variable("center",xav, 0, upperCenter));
	
	    ArrayList<Objective> objectives = new ArrayList<Objective>();
	    objectives.add(new TargetObjective( "diff", 0.0 ) );
	    
	    Evaluator1 evaluator = new Evaluator1( objectives, variables );
	    
	    Problem problem = new Problem( objectives, variables, evaluator );
	    problem.addHint(new InitialDelta( 0.05) );
	    
	    double solvetime = 2; 	
	    Stopper maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( 1, solvetime, 0.999 );
	    Solver solver = new Solver(new RandomShrinkSearch(), maxSolutionStopper );

	    solver.solve( problem );
	    System.out.println("score is " + solver.getScoreBoard());
	    Trial best = solver.getScoreBoard().getBestSolution();
	   
	    calcError(variables, best);
	    Iterator<Variable> itr = variables.iterator();
	    while(itr.hasNext()){
		    Variable variable = itr.next();
		    double value = best.getTrialPoint().getValue(variable);
		    String name = variable.getName();
		    if(name.equalsIgnoreCase("amp")) amp = value;
		    if(name.equalsIgnoreCase("sigma")) sigma = value;
		    if(name.equalsIgnoreCase("center")) center = value;
	}
	if(!table2){
		result[0].setValue(center + phi0);
		result[1].setValue(sigma);
		result[2].setValue(amp);
	}
    }
    
    
    //Calculates and returns the error
    public  double calcError(ArrayList<Variable> vars, Trial trial){
		double error = 0.0;
		double temp = 0.0;
        int size = xphase.length;
		double amp=0.0;
		double sigma=0.0;
		double center=0.0;
		double x;

		Iterator<Variable> itr = vars.iterator();
        while(itr.hasNext()){
	    Variable variable = itr.next();
            double value = trial.getTrialPoint().getValue(variable);
            String name = variable.getName();
            if(name.equalsIgnoreCase("amp")) amp = value;
            if(name.equalsIgnoreCase("sigma")) sigma = value;
            if(name.equalsIgnoreCase("center")) center = value;
		}
	
        for(int i=0; i<size; i++){
	    x = x1[i]; 
	    temp = amp*Math.exp(-(x-center)*(x-center)/(2.0*sigma*sigma*.7)); 
	    error += Math.pow((currentyvalues[i] - temp), 2.0);
	}
	error = Math.sqrt(error);
	return error;
    }	
    
    
    //Calculates the skewness()
    //public void getSkewness(){
	//double skewness = 0.0;
	//int sizeX = currentyvalues.length;
	//for(int i = 0; i < sizeX; i++){
		//skewness = skewness + (((x1[i] - xav) * (x1[i] - xav) * (x1[i] - xav) * distr[i] * dx) / (rms * rms * rms));
	//}
    //}
    
    
    public void normalize(){
		    int sizeX = currentyvalues.length;
				
		    //Perform Integral
		    double sum = 0.0;
		    for(int i = 0; i < sizeX; i++){
			    sum = sum + currentyvalues[i];
		    }
		    double intgr = sum * dx;
	    
		    //Normalize the distribution
		    distr = new double[sizeX];
		    for(int i = 0; i < sizeX; i++){
			    distr[i] = (currentyvalues[i]/intgr);
			    currentyvalues[i] = distr[i];
		    }
    }
	
    public void offset(){
		int sizeX = xphase.length;
		
		//Normalize the distribution
		for(int i = 0; i < sizeX; i++){
			xphase[i] -= hoffset.getDoubleValue();
		}
    }
	
    
    //Saves all the results so that they can be stored in the results panel
    public void storeResult(){
	  if(!dataHasBeenFit){
		  sigma = 0.0; center = 0.0; amp = 0.0;
	  }
	  int length = currentyvalues.length;
	  HashMap<String, Object> storeMap = new HashMap<String, Object>();
	  storeMap.put("name", map.get("name")); //bsm name
	  storeMap.put("phase", map.get("phase")); //reference phi
	  storeMap.put("xvalues", xphase);
	  storeMap.put("length", length); //length of currentyvalues
	  storeMap.put("rms", rms); //statistical RMS value
	  storeMap.put("center", center);
	  storeMap.put("sigma", sigma);
	  storeMap.put("amp", amp);
	  storeMap.put("fit", dataHasBeenFit); //fit boolean
	  storeMap.put("linearplot", linearplot); //linear/log boolean
	  storeMap.put("statrms", statrms); //RMS boolean - tells if the user picked gauss. or stat.
	  storeMap.put("yvalues", currentyvalues); //does NOT pass the log-values to results panel
	  
	  System.out.print("name = " + map.get("name") + "\n\n\n");
	  
	  doc.resultMap.put(filename, storeMap);
    }
    
    
    //Clears all the results that were saved for the stored results panel
    public void clearStoreResult(){
	    if(doc.resultMap.size()==0)
		    System.out.println("No files to remove. ");
	    else
		    doc.resultMap.clear();	    
    }
    
    
       //Evaluates beam properties for a trial point
        class Evaluator1 implements Evaluator{
    
	       protected ArrayList<Objective> _objectives;
	       protected ArrayList<Variable> _variables;
	       public Evaluator1( final ArrayList<Objective> objectives, final ArrayList<Variable> variables ) {
		       _objectives = objectives;
		       _variables = variables;
	       }
    
	       public void evaluate(final Trial trial){
		       double error =0.0; 
		       Iterator<Objective> itr = _objectives.iterator();
	
		       while(itr.hasNext()){
			       TargetObjective objective = (TargetObjective)itr.next();
			       error = calcError(_variables, trial);
			       trial.setScore(objective, error);
		       }
	       }
       }
       
       
       // objective class for solver.
         class TargetObjective extends Objective{
    
	       protected final double _target;
   
	       public TargetObjective(final String name, final double target){
		       super(name);
		       _target=target;
	       }
    
	       public double satisfaction(double value){
		       double error = _target - value;
		       return 1.0/(1+error*error);
	       }
       }   

       
       public class FileSummaryPanel extends JPanel{
           
           /** serialization ID */
           private static final long serialVersionUID = 1L;
           
	       public JPanel mainPanel;
	       public JTable datatable;
	       public DataTableModel datatablemodel;
	       private JScrollPane datascrollpane;
	       GenDocument doc;
	       
	       private JLabel intervallabel;
	       private DecimalField intervalfield;
	       private JPanel intervalpanel;
	       private JButton fillbutton;
	       private JButton clearbutton;
	       
	       double[] xvaluestemp;
	       double[] yvaluestemp; 
	       double[] xfittemp;
	       double[] yfittemp;
	       double[] x1temp;
	       double rmstemp;
	       double sigmatemp;
	       double centertemp;
	       int t1temp; 
	       int t2temp;
	       double amptemp;
	       
	       
	       private int time;
	      // private int timeSize;
	       
	       public FileSummaryPanel(){}
	       //Member function Constructor
	       
	       public FileSummaryPanel(GenDocument aDocument){
		       doc = aDocument;
		       
		       makeComponents();
		       addComponents();
		       setAction();
	       }
	       
	       public void makeComponents(){
			mainPanel = new JPanel();
			mainPanel.setPreferredSize(new Dimension(575, 135));
			mainPanel.setBorder(BorderFactory.createTitledBorder("BSM Analysis"));
			
			makeDataTable();
			
			intervallabel = new JLabel("Time Interval");
			intervalfield = new DecimalField(10, 4, numFor1);
			intervalpanel = new JPanel();
			intervalpanel.add(intervallabel);
			intervalpanel.add(intervalfield);
			
			
			
			fillbutton = new JButton("Fill Table");
			clearbutton = new JButton("Clear Table");
	       }
	       
	       public void makeDataTable(){
			String[] colnames = {"Time 1", "Time 2", "RMS", "Sigma", "Mean"}; 
			datatablemodel = new DataTableModel(colnames, 0);    
			
			datatable = new JTable(datatablemodel);
			datatable.getColumnModel().getColumn(0).setMinWidth(70);
			datatable.getColumnModel().getColumn(1).setMinWidth(70);
			datatable.getColumnModel().getColumn(2).setMinWidth(70);
			datatable.getColumnModel().getColumn(3).setMinWidth(70);
			datatable.getColumnModel().getColumn(4).setMinWidth(70);
			
			datatable.setRowSelectionAllowed(false);
			datatable.setColumnSelectionAllowed(false);
			datatable.setCellSelectionEnabled(false);
			datatable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	
			datascrollpane = new JScrollPane(datatable);
			datascrollpane.setColumnHeaderView(datatable.getTableHeader());
			datascrollpane.setPreferredSize(new Dimension(393, 100));
	       }
	       
	       public void addComponents(){
		       EdgeLayout layout = new EdgeLayout();
		       mainPanel.setLayout(layout);
		       
		       layout.add(datascrollpane, mainPanel, 175, 20, EdgeLayout.LEFT);
		       layout.add(intervalpanel, mainPanel, 15, 30, EdgeLayout.LEFT);
		       layout.add(fillbutton, mainPanel, 15, 70, EdgeLayout.LEFT);
		       layout.add(clearbutton, mainPanel, 15, 100, EdgeLayout.LEFT);
		       
		       this.add(mainPanel);
	       }
	       
	       public void setAction(){
		       fillbutton.addActionListener(new ActionListener(){
				  public void actionPerformed(ActionEvent e){				   
			           if(plot)
					  saveCurrent();
				   int i = 0;
				   int timeSize = ((ArrayList)data.get(0)).size();
				   table2 = true;
				   getTime();
				   while(i < timeSize){
				       	   calcNoise();
					   calcMeanPlot();
					   if(norm)
						   normalize();
					   calcRMS();
					   gaussFit();
					   toTable();
					   
					   t1 = t2 + 2;
					   t2 = t2 + time + 1;
					   if(t2 > timeSize && t1 < timeSize){
						   t2 = timeSize;
						   i = timeSize - 1;
					   }
					   else
						   i = i + time;
				   }
				   table2 = false;
				   if(plot)
					   reset();
			    }				
		       });
		       
		       clearbutton.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
				datatablemodel.clearAllData();	
			    }
		       });
	       }
	       
	        public void getTime(){
			time = (int)intervalfield.getValue();
			t1 = 1;
			t2 = time; 
		}
		
		public void toTable(){
			ArrayList<Object> tabledata = new ArrayList<Object>();
			
			tabledata.add(t1 + 1);
			tabledata.add(t2 + 1);
			tabledata.add(rms);
			tabledata.add(sigma);
			center = center + phi0;
			tabledata.add(center);
			
			datatablemodel.addTableData(new ArrayList<Object>(tabledata));
			datatablemodel.fireTableDataChanged();
		}
		
		public void saveCurrent(){
				   yvaluestemp = new double[currentyvalues.length];
				   xvaluestemp = new double[xphase.length];
				   x1temp = new double[x1.length];
				   
				   if(!linearplot)
					   yvaluestemp = logdata;
				   else
					   yvaluestemp = currentyvalues;
				   xvaluestemp = xphase;
				   x1temp = x1;
				   
				   if(dataHasBeenFit){
					   xfittemp = new double[xfit.length];
					   yfittemp = new double[yfit.length];
					   xfittemp = xfit;
					   if(!linearplot)
						   yfittemp = ylogfit;
					   else
						   yfittemp = yfit;
				   }
					   
				   rmstemp = rms;
				   sigmatemp = sigma;
				   centertemp = center;
				   t1temp = t1;
				   t2temp = t2;
				   amptemp = amp;
		}
		
		public void reset(){
			rms = rmstemp; 
			sigma =sigmatemp;
			center = centertemp;
			t1 = t1temp;
			t2 = t2temp;
			amp = amptemp;
			
			if(!linearplot)
				logdata = yvaluestemp;
			else
				currentyvalues =  yvaluestemp;
			xphase = xvaluestemp;
			x1 = x1temp;
			if(dataHasBeenFit){
				xfit = xfittemp;
				if(!linearplot)
					ylogfit = yfittemp;
				else
					yfit = yfittemp;
			}
			calcNoise();
		}
       }
}
