//
//  WaveformFace.java
//  xal
//
//  Created by S. Cousineau on March 1, 2008
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.escap;

import xal.ca.*;
import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.tools.messaging.MessageCenter;
//import xal.apps.escap.*;
import java.net.URL;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;
import java.util.Date;
import java.sql.Connection;
import java.awt.Color;
import java.lang.Object;
import java.net.*;
import java.io.*;
import xal.extension.widgets.plot.*;
import xal.tools.apputils.files.RecentFileTracker;
import xal.tools.apputils.EdgeLayout;
import javax.swing.border.*;
import xal.extension.widgets.swing.*;
import java.awt.Dimension;
import java.text.NumberFormat;

/** Controller for the client that monitors the trip monitor services */
public class ScannerFace extends JPanel{
	/** reference to the main window */

    private static final long serialVersionUID = 1L;
    
	public JPanel mainPanel;
	/** Kicker ramp time in us */
	
	private JFileChooser fc;
	NumberFormat numFor = NumberFormat.getNumberInstance();
	
	RecentFileTracker ft;
	//FunctionGraphsJPanel hdataPlot;
	FunctionGraphsJPanel dataPlot;
	JLabel thresholdLabel;
	DecimalField thresholdField;
	JButton readButton;
	JButton plotButton;
	JButton lineButton;
	//JButton loadFileButton; 
	
	ArrayList<ArrayList<Double>> data= new ArrayList<ArrayList<Double>>();
	double[] slinedata;
	double[] ylinedata;
	
	/** Constructor */
	
	public ScannerFace() {
	    makeComponents();
	    addComponents();
	    setAction();
	}
	
	public void addComponents(){
	    EdgeLayout layout = new EdgeLayout();
	    mainPanel.setLayout(layout);
	    layout.add(readButton, mainPanel, 15, 50, EdgeLayout.LEFT);
	    layout.add(plotButton, mainPanel, 15, 80, EdgeLayout.LEFT);
	    layout.add(thresholdLabel, mainPanel, 15, 110, EdgeLayout.LEFT);
	    layout.add(thresholdField, mainPanel, 200,110, EdgeLayout.LEFT);
	    layout.add(lineButton, mainPanel, 15, 130, EdgeLayout.LEFT);
	    //layout.add(hdataPlot, mainPanel, 0, 105, EdgeLayout.LEFT);
	    layout.add(dataPlot, mainPanel, 100, 185, EdgeLayout.LEFT);
	    
	    this.add(mainPanel);
    }
   
   
   public void makeComponents(){
       
       	mainPanel = new JPanel();
	mainPanel.setPreferredSize(new Dimension(1150, 750));
	
       	fc = new JFileChooser();
	ft = new RecentFileTracker(1, this.getClass(), "scanfile");
	ft.applyRecentFolder(fc);
	
	numFor.setMinimumFractionDigits(2);
	
	dataPlot = new FunctionGraphsJPanel();
	
	readButton = new JButton("Read Image File");
	plotButton = new JButton("Plot File");
	//hdataPlot.setPreferredSize(new Dimension(400, 300));
	lineButton = new JButton("Draw Data Line");
	thresholdLabel = new JLabel("Data Threshold (% of max)): ");
	thresholdField = new DecimalField(0.75, 4, numFor);
	dataPlot.setPreferredSize(new Dimension(500, 400));
   }

	public void setAction(){
	

	readButton.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		int returnValue = fc.showOpenDialog(ScannerFace.this);
		if(returnValue == JFileChooser.APPROVE_OPTION){
		    File file = fc.getSelectedFile();
		    ft.cacheURL(file);
		    data = parseFile(file);
		}
		else{
		    System.out.println("Open command cancelled by user.");
		}
	    }
		   
	});
	
	plotButton.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		plotData(false);
	    }
		   
	});
	lineButton.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		getLineData();
		plotData(true);
	    }
		   
	});
	
	
	}
	
	private ArrayList<ArrayList<Double>> parseFile(File newfile){
	    ParseScannerFile parsefile = new ParseScannerFile();
	    ArrayList<ArrayList<Double>> newdata = new ArrayList<ArrayList<Double>>();
	    try{
		newdata = parsefile.parseFile(newfile);
	    }
	    catch(IOException e){
		System.out.println("Warning, returning empty data set.");
	    }
	    return newdata;
		
	}

	
	
	public void plotData(boolean linedata){
	    //System.out.println("data elements " + ((ArrayList)data.get(0)));
	    dataPlot.removeAllGraphData();
	    int ny = data.size();
	    int nx = ((ArrayList)data.get(0)).size();
	    
	    
	    ColorSurfaceData imagedata = Data3DFactory.getDefaultData3D(nx,ny);
	    imagedata.setMinMaxX(0,nx);
	    imagedata.setMinMaxY(0,ny);
	    imagedata.setScreenResolution(nx,ny);
	    for(int j=0; j<ny; j++){ 
		for(int i=0; i<nx; i++){ 
		    double v = ((Double)((ArrayList)data.get(j)).get(i)).doubleValue();
		    imagedata.setValue(i,j,v);
		}
	    }


	    /*
	    BasicGraphData fitgraphdata = new BasicGraphData();
	 
	    fitgraphdata.addPoint(sdata, ydata);
	    fitgraphdata.setDrawPointsOn(true);
	    fitgraphdata.setDrawLinesOn(true);
	    fitgraphdata.setGraphProperty("Legend", new String("fit data"));
	    fitgraphdata.setGraphColor(Color.RED);
	    */
	    dataPlot.setColorSurfaceData(imagedata);
	    
	    dataPlot.setOffScreenImageDrawing(true);
	    dataPlot.setGridLinesVisibleY(false);
	    dataPlot.setGridLinesVisibleX(false);
	    
	    if(linedata){
		BasicGraphData fitgraphdata = new BasicGraphData();
		
		fitgraphdata.addPoint(slinedata, ylinedata);
		fitgraphdata.setDrawPointsOn(true);
		fitgraphdata.setDrawLinesOn(true);
		fitgraphdata.setGraphProperty("Legend", new String("fit data"));
		fitgraphdata.setGraphColor(Color.RED);
		dataPlot.addGraphData(fitgraphdata);
	    }
	    dataPlot.refreshGraphJPanel();
	    
	}
	
	public void getLineData(){
	    
	    int rows = data.size();
	    int columns = ((ArrayList)data.get(0)).size();
	    double[] sdata = new double[columns];
	    double[] ydata = new double[columns];
	    double threshold = thresholdField.getDoubleValue();
	    ArrayList<Double> maxvalues = new ArrayList<Double>();
	    ArrayList<Double> meanvalues = new ArrayList<Double>();
	    
	    for(int j=0; j<columns; j++){ 
		double localmax = 0;
		for(int i=0; i<rows; i++){ 
		    double v = ((Double)((ArrayList)data.get(i)).get(j)).doubleValue();
		    if(v > localmax) localmax = v;
		}
		maxvalues.add(new Double(localmax));
	    }
	    
	   
	    for(int j=0; j<columns; j++){ 
		double localmean = 0;
		double meansize = 0;
		for(int i=0; i<rows; i++){ 
		    double v = ((Double)((ArrayList)data.get(i)).get(j)).doubleValue();
		    double localmax = (maxvalues.get(j)).doubleValue();
		    if(v > threshold*localmax){
			localmean += v * ((new Integer(i)).doubleValue());
			meansize += v;
		    }
		}
		localmean /= meansize;
		meanvalues.add(new Double(localmean));
	    }
	    
	    for(int j=0; j<columns; j++){ 
		sdata[j] = ((new Integer(j))).doubleValue();
		ydata[j] = (meanvalues.get(j)).doubleValue();
	    }
	    
	    slinedata = sdata;
	    ylinedata = ydata;
	    
	    }
		
	

}
