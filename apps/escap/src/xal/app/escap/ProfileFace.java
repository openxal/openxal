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
import xal.tools.plot.*;
import xal.tools.apputils.files.RecentFileTracker;
import xal.tools.apputils.EdgeLayout;
import javax.swing.border.*;
import xal.tools.swing.*;
import java.awt.Dimension;
import java.text.NumberFormat;

/** Controller for the client that monitors the trip monitor services */
public class ProfileFace extends JPanel{
	/** reference to the main window */

    private static final long serialVersionUID = 1L;
    
	public JPanel mainPanel;
	/** Kicker ramp time in us */
	
	private JFileChooser fc;
	NumberFormat numFor = NumberFormat.getNumberInstance();
	
	RecentFileTracker ft;
	//FunctionGraphsJPanel hdataPlot;
	FunctionGraphsJPanel dataPlot;
	
	JButton pvVButton;
	JButton pvHButton;
	JButton writeButton;
	//JButton loadFileButton; 
	Channel ch_v, ch_h;
	Channel ch_posv, ch_posh;
	//ArrayList hdata = new ArrayList();
	//ArrayList vdata = new ArrayList();
	double[] x, y;
	double[] pos_x, pos_y;
	/** Constructor */
	
	public ProfileFace() {
	    makeComponents();
	    addComponents();
	    setAction();
	}
	
	public void addComponents(){
	    EdgeLayout layout = new EdgeLayout();
	    mainPanel.setLayout(layout);
	    layout.add(pvHButton, mainPanel, 10, 30, EdgeLayout.LEFT);
	    layout.add(pvVButton, mainPanel, 235, 30, EdgeLayout.LEFT);
	    layout.add(writeButton, mainPanel, 10, 80, EdgeLayout.LEFT);
	    //layout.add(hdataPlot, mainPanel, 0, 105, EdgeLayout.LEFT);
	    layout.add(dataPlot, mainPanel, 100, 115, EdgeLayout.LEFT);
	    
	    this.add(mainPanel);
    }
   
   public void makeComponents(){
       
	mainPanel = new JPanel();
	mainPanel.setPreferredSize(new Dimension(1150, 750));
	
	fc = new JFileChooser();
	ft = new RecentFileTracker(1, this.getClass(), "scanfile");
	ft.applyRecentFolder(fc);
	
	numFor.setMinimumFractionDigits(3);
	
	dataPlot = new FunctionGraphsJPanel();
	
	pvVButton = new JButton("Read Vertical Profile PVs");
	pvHButton = new JButton("Read Horizontal Profile PVs");
	writeButton = new JButton("Write Data To File");
	//hdataPlot.setPreferredSize(new Dimension(400, 300));
	dataPlot.setPreferredSize(new Dimension(400, 300));
   }

	public void setAction(){
	

	pvHButton.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		    connectHScannerPVs();
		    try {
			pos_x = ch_posh.getArrDbl();
			x = ch_h.getArrDbl();
		    } catch (ConnectionException ce) {
			System.out.println("Cannot connect to scanner PVs");
		    } catch (GetException ce) {
			System.out.println("Cannot get scanner PVs");
		    }	
		    
		    plotData(pos_x, x);
		    
	    }
	});	
		
		
	pvVButton.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		    connectVScannerPVs();
		    try {
			pos_y = ch_posv.getArrDbl();
			y = ch_v.getArrDbl();
		    } catch (ConnectionException ce) {
			System.out.println("Cannot connect to scanner PVs");
		    } catch (GetException ce) {
			System.out.println("Cannot get scanner PVs");
		    }	
		    
		    plotData(pos_y, y);
		    
	    }
	});
	

	writeButton.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) { 
		int returnValue = fc.showSaveDialog(ProfileFace.this);
		if(returnValue == JFileChooser.APPROVE_OPTION){
		    File file = fc.getSelectedFile();
		    try{
			writeDataFile(pos_x, x, pos_y, y, file);
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
	
	private void connectHScannerPVs() {
		ChannelFactory cf = ChannelFactory.defaultFactory();
		
		ch_posh = cf.getChannel("Ring_Diag:ELS01:PosX");
		ch_h = cf.getChannel("Ring_Diag:ELS01:SignalX");
		ch_h.connectAndWait();
		ch_posh.connectAndWait();
		
	}

	private void connectVScannerPVs() {
		ChannelFactory cf = ChannelFactory.defaultFactory();
		
		ch_posv = cf.getChannel("Ring_Diag:ELS01:PosY");
		ch_v = cf.getChannel("Ring_Diag:ELS01:SignalY");
		ch_v.connectAndWait();
		ch_posv.connectAndWait();
		
	}
	
	public void plotData(double[] sdata, double[] data){

	    dataPlot.removeAllGraphData();
	    
	    BasicGraphData fitgraphdata = new BasicGraphData();
	 
	    fitgraphdata.addPoint(sdata, data);
	    fitgraphdata.setDrawPointsOn(true);
	    fitgraphdata.setDrawLinesOn(true);
	    fitgraphdata.setGraphProperty("Legend", new String("data"));
	    fitgraphdata.setGraphColor(Color.RED);
	    
	    dataPlot.addGraphData(fitgraphdata);
	    dataPlot.refreshGraphJPanel();   
	}
	
	public void writeDataFile(double[] sxdata, double[] xdata, double[] sydata, double[] ydata, File file) throws IOException{
	
	    OutputStream fout = new FileOutputStream(file);
	    String line = "start time: \n\n";
	    line = line + "Ring_Diag:ELS01 X \n";
	    line = line + "X_Position\tX_Raw\n";
	    line = line + "--------\t-----\n";
		if(xdata!=null){
			for(int i=0; i<sxdata.length; i++){
				line = line + sxdata[i] + "\t\t" + xdata[i] + "\n";
			}
		}
	    line = line + "\n";
	    line = line + "Ring_Diag:ELS01 Y \n";
	    line = line + "Y_Position\tY_Raw\n";
	    line = line + "--------\t-----\n";
		if(ydata!=null){
			for(int i=0; i<sydata.length; i++){
				line = line + sydata[i] + "\t\t" + ydata[i] + "\n";
			}
		}
	    line = line + "\n";
	    byte buf[] = line.getBytes();
	    fout.write(buf);
	    
	    fout.close();
	}
		
	

}
