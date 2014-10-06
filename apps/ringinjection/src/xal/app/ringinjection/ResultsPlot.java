//TuneSettings GUI

//Test Plot
package xal.app.ringinjection;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.lang.*;
import java.text.*;

import xal.tools.apputils.*;
import xal.extension.widgets.swing.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.data.*;
import xal.extension.widgets.plot.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.messaging.*;


public class ResultsPlot extends JPanel{
	/** serial version required by Serializable */
	private static final long serialVersionUID = 1L;

    private FunctionGraphsJPanel hdatapanel;
    private FunctionGraphsJPanel vdatapanel;
    
    Dimension dimension = new Dimension(400,175);
    GenDocument doc;
    
    int nbpmplots;

    //Member functions
    
    public ResultsPlot() {

	setPreferredSize(new Dimension(400,400));

	//createComponents();
	createHResultsPlot();
	createVResultsPlot();

	this.add(hdatapanel);
	this.add(vdatapanel);
    }
    


    private void createHResultsPlot(){
	
	hdatapanel = new FunctionGraphsJPanel();
	hdatapanel.setPreferredSize(new Dimension(400, 175));
	hdatapanel.setGraphBackGroundColor(Color.WHITE);
	
    }	
    
    private void createVResultsPlot(){
	
	vdatapanel = new FunctionGraphsJPanel();
	vdatapanel.setPreferredSize(new Dimension(400, 175));
	vdatapanel.setGraphBackGroundColor(Color.WHITE);
    }	


    void refreshHPlot(BpmAgent bpmagent){
	
	hdatapanel.removeAllGraphData();
	
	int i=0;
	int size;
	final int fitresolution = 100;

	BasicGraphData rawgraphdata = new BasicGraphData();
	BasicGraphData fitgraphdata = new BasicGraphData();	
	BpmAgent agent = (BpmAgent)bpmagent;
	size=agent.getDataSize();

	
	//Plot the raw data
	double[] sdata = new double[size];
	double[] data = new double[size];
	double[] xtemp = bpmagent.getXTBTData();
	
	for(i=0; i<size; i++){
	    sdata[i]=i;
	    data[i] = xtemp[i];
	}
	rawgraphdata.addPoint(sdata, data);
	rawgraphdata.setDrawPointsOn(true);
	rawgraphdata.setDrawLinesOn(true);
	rawgraphdata.setGraphProperty("Legend", new String("raw data"));
	rawgraphdata.setGraphColor(Color.RED);
	hdatapanel.addGraphData(rawgraphdata);

	
	//Plot the fitted data.
	
	double[] fsdata=new double[size*fitresolution];
	double[] fdata=new double[size*fitresolution];
	
	double xparams[] = agent.getXTBTFitParams();
	double pi=3.14159;
	double arg=0.0;
	double q = xparams[0];
	double phi = xparams[1];
	double slope = xparams[2];
	double amp = xparams[3];
	double offset = xparams[4];
	double x=0.0;
	
	
	for(i=0; i<size*fitresolution; i++){
	    arg=2*pi*q*x;
	    fsdata[i]=x;
	    fdata[i]=(amp*Math.exp(-x*slope)*Math.cos(arg+phi) + offset);
	    x+=1.0/fitresolution;
	}
	
	fitgraphdata.addPoint(fsdata, fdata);
	fitgraphdata.setDrawPointsOn(false);
	fitgraphdata.setDrawLinesOn(true);
	fitgraphdata.setGraphProperty("Legend", new String("fit data"));
	fitgraphdata.setGraphColor(Color.BLACK);
	hdatapanel.addGraphData(fitgraphdata);
	hdatapanel.setLegendButtonVisible(true);
	hdatapanel.setChooseModeButtonVisible(true);
	hdatapanel.setName("   "+ agent.toString() + ": HORIZONTAL");
    }
    
    
    void refreshVPlot(BpmAgent bpmagent){
	
	vdatapanel.removeAllGraphData();
	
	int i=0;
	int size;
	final int fitresolution = 100;

	BasicGraphData rawgraphdata = new BasicGraphData();
	BasicGraphData fitgraphdata = new BasicGraphData();	
	BpmAgent agent = (BpmAgent)bpmagent;
	size=agent.getDataSize();

	//Plot the raw data
	double[] sdata = new double[size];
	double[] data = new double[size];
	double[] ytemp = bpmagent.getYTBTData();
	
	for(i=0; i<size; i++){
	    sdata[i]=i;
	    data[i] = ytemp[i];
	}
	rawgraphdata.addPoint(sdata, data);
	rawgraphdata.setDrawPointsOn(true);
	rawgraphdata.setDrawLinesOn(true);
	rawgraphdata.setGraphProperty("Legend", new String("raw data"));
	rawgraphdata.setGraphColor(Color.RED);
	vdatapanel.addGraphData(rawgraphdata);

	
	//Plot the fitted data.
	
	double[] fsdata=new double[size*fitresolution];
	double[] fdata=new double[size*fitresolution];
	
	double yparams[] = agent.getYTBTFitParams();
	double pi=3.14159;
	double arg=0.0;
	double q = yparams[0];
	double phi = yparams[1];
	double slope = yparams[2];
	double amp = yparams[3];
	double offset = yparams[4];
	double x=0.0;
	
	
	for(i=0; i<size*fitresolution; i++){
	    arg=2*pi*q*x;
	    fsdata[i]=x;
	    fdata[i]=(amp*Math.exp(-x*slope)*Math.cos(arg+phi) + offset);
	    x+=1.0/fitresolution;
	}
	fitgraphdata.addPoint(fsdata, fdata);
	fitgraphdata.setDrawPointsOn(false);
	fitgraphdata.setDrawLinesOn(true);
	fitgraphdata.setGraphProperty("Legend", new String("fit data"));
	fitgraphdata.setGraphColor(Color.BLACK);
	vdatapanel.addGraphData(fitgraphdata);
	vdatapanel.setLegendButtonVisible(true);
	vdatapanel.setChooseModeButtonVisible(true);
	vdatapanel.setName("   "+ agent.toString() + ": VERTICAL");
    }	



	
}
    
