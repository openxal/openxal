/*
 * CalculateFit.java
 *
 * Created on April 1, 2004 
 */

package xal.app.ringinjection;
import java.util.*;
import java.net.*;
import java.io.*;
import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.event.*;

import xal.tools.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.tools.*;
import xal.ca.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.data.*;
import xal.extension.application.*;
import xal.model.*;
import xal.ca.*;
import xal.tools.*;
import xal.tools.beam.*;
import xal.tools.xml.*;
import xal.tools.data.*;
import xal.tools.messaging.*;
import java.util.HashMap;
import xal.tools.xml.XmlDataAdaptor;
 
import xal.smf.proxy.*;
import xal.model.probe.traj.*;
import xal.model.probe.*; 
import xal.model.xml.*; 
import xal.sim.scenario.*;
import java.util.*;
import java.lang.*;
import xal.tools.math.r3.R3;
import xal.extension.widgets.plot.*;
import xal.model.alg.*;

/**
 * Performs the fit for the turn-by-turn signal on a BPM.  Records the 
 * fit parameters in the BPMAgent class.
 * @author  cp3
 */
 
public class OneTurnFit extends JPanel{
 
    protected Channel channel;
    protected BpmAgent localagent;
    
    public ArrayList fitparams;
    private Accelerator accl = new Accelerator();
    private FunctionGraphsJPanel hdatapanel;
    private FunctionGraphsJPanel vdatapanel;
    private AcceleratorSeqCombo seq;
    private TransferMapProbe probe;
    private Scenario scenario;
    private Trajectory<TransferMapState> traj;
    
    double[] xfitparams = new double[4];
    double[] yfitparams = new double[4];
    double[] xfoilparams = new double[4];
    double[] yfoilparams = new double[4];
    
    GenDocument doc;
    Betatron bt;
    
    public OneTurnFit(GenDocument aDocument) {

	doc = aDocument;
	bt = new Betatron(doc);
	
	setPreferredSize(new Dimension(450,365));
	
	createHResultsPlot();
	createVResultsPlot();

	this.add(hdatapanel);
	this.add(vdatapanel);
    }
    

    
    public double[] xbpmFit(ArrayList bpms) {
	
	int size = bpms.size();
	int i = 0;
	double[] snewdata = new double[size];
	double[] xnewdata = new double[size];
	
	Iterator itr = bpms.iterator();
	
	while(itr.hasNext()){
	    //Do the fit for all active BPM Agents
	    BpmAgent bpmagent = (BpmAgent)itr.next();
	    snewdata[i] = bpmagent.getPosition();
	    xnewdata[i] = bpmagent.getXAvgTBTArray()[0] - bpmagent.getXAvg();
	    //xnewdata[i] = bpmagent.getXAvgTBTArray()[0];
	    i++;
	}
	
	//Do this first for the x direction
	bt.horizontal_data = true;
	bt.setData(snewdata, xnewdata);
	bt.fitParameter(Betatron.AMP, true);
	bt.fitParameter(Betatron.PHASE, true);
	
	bt.setupModel();
	
	int iterations = 5;
	boolean result = bt.guessAndFit(iterations);		
	
	bt.setParameter(Betatron.AMP, bt.getParameter(Betatron.AMP));
	bt.setParameter(Betatron.PHASE, bt.getParameter(Betatron.PHASE));
	
	iterations=15;
	result = bt.fit();
	
	/*
	System.out.println("Main: a = " + bt.getParameter(Betatron.AMP) + " +- " + bt.getParameterError(Betatron.AMP));		
	System.out.println("Main: p = " + bt.getParameter(Betatron.PHASE) + " +- " + bt.getParameterError(Betatron.PHASE));
	*/
	
	if(result){
	    xfitparams[0] = bt.getParameter(Betatron.AMP);
	    xfitparams[1] = bt.getParameter(Betatron.PHASE);
	    xfitparams[2] = bt.getParameterError(Betatron.AMP);
	    xfitparams[3] = bt.getParameterError(Betatron.PHASE);
	}
	

	double position = scenario.getPositionRelativeToStart(0.0);
	TransferMapState injstate = (TransferMapState)traj.statesInPositionRange(position - 0.00001, position + 0.00001)[0];
	Twiss[] injtwiss=injstate.getTwiss();
	R3 phase = injstate.getBetatronPhase();
	PhaseVector orbit = injstate.getFixedOrbit();
	double beta_0=injtwiss[0].getBeta(); 
	double alpha_0=injtwiss[0].getAlpha();

	double arg = xfitparams[1] + phase.getx();
	double sbeta = Math.sqrt(beta_0);
	double cosarg = Math.cos(arg);
	double sinarg = Math.sin(arg);
	
	xfoilparams[0] = (xfitparams[0]*sbeta*cosarg);
	xfoilparams[1] = Math.abs(sbeta*cosarg*xfitparams[2] - xfitparams[0]*sbeta*sinarg*xfitparams[3]);
	xfoilparams[2] = -xfitparams[0]/sbeta*(alpha_0*cosarg + sinarg);
	xfoilparams[3] = Math.abs(-1/sbeta*(alpha_0*cosarg + sinarg)*xfitparams[2]
			+ xfitparams[0]/sbeta*(alpha_0*sinarg - cosarg)*xfitparams[3]);
	/*
	System.out.println("x = " + xfoilparams[0] + " +/- " + xfoilparams[1]);
	System.out.println("px = " + xfoilparams[2] + " +/- " + xfoilparams[3]);
	*/
	return xfoilparams;
   }
   
   
   
   public double[] ybpmFit(ArrayList bpms) {
	
	int size = bpms.size();
	int i = 0;
	double[] snewdata = new double[size];
	double[] ynewdata = new double[size];
	
	Iterator itr = bpms.iterator();
	
	while(itr.hasNext()){
	    //Do the fit for all active BPM Agents
	    BpmAgent bpmagent = (BpmAgent)itr.next();
	    snewdata[i] = bpmagent.getPosition();
	    ynewdata[i] = bpmagent.getYAvgTBTArray()[0] - bpmagent.getYAvg();
	    //ynewdata[i] = bpmagent.getYAvgTBTArray()[0];
	    i++;
	}
	

	//Do this first for the x direction
	bt.horizontal_data = false;
	bt.setData(snewdata, ynewdata);
	bt.fitParameter(Betatron.AMP, true);
	bt.fitParameter(Betatron.PHASE, true);
	
	bt.setupModel();
	
	int iterations = 5;
	boolean result = bt.guessAndFit(iterations);
	
	bt.setParameter(Betatron.AMP, bt.getParameter(Betatron.AMP));
	bt.setParameter(Betatron.PHASE, bt.getParameter(Betatron.PHASE));
	
	iterations=15;
	result = bt.fit();
	
	/*
	System.out.println("Main: a = " + bt.getParameter(Betatron.AMP) + " +- " + bt.getParameterError(Betatron.AMP));		
	System.out.println("Main: p = " + bt.getParameter(Betatron.PHASE) + " +- " + bt.getParameterError(Betatron.PHASE));
	*/
	if(result){
	    yfitparams[0] = bt.getParameter(Betatron.AMP);
	    yfitparams[1] = bt.getParameter(Betatron.PHASE);
	    yfitparams[2] = bt.getParameterError(Betatron.AMP);
	    yfitparams[3] = bt.getParameterError(Betatron.PHASE);
	}
	

	double position = scenario.getPositionRelativeToStart(0.0);
	TransferMapState injstate = (TransferMapState)traj.statesInPositionRange(position- 0.00001, position + 0.00001)[0];
	Twiss[] injtwiss=injstate.getTwiss();
	R3 phase = injstate.getBetatronPhase();
	PhaseVector orbit = injstate.getFixedOrbit();
	double beta_0=injtwiss[1].getBeta(); 
	double alpha_0=injtwiss[1].getAlpha();

	double arg = yfitparams[1] + phase.gety();
	double sbeta = Math.sqrt(beta_0);
	double cosarg = Math.cos(arg);
	double sinarg = Math.sin(arg);
	
	yfoilparams[0] = (yfitparams[0]*sbeta*cosarg);
	yfoilparams[1] = Math.abs(sbeta*cosarg*yfitparams[2] - yfitparams[0]*sbeta*sinarg*yfitparams[3]);
	yfoilparams[2] = -yfitparams[0]/sbeta*(alpha_0*cosarg + sinarg);
	yfoilparams[3] = Math.abs(-1/sbeta*(alpha_0*cosarg + sinarg)*yfitparams[2]
			+ yfitparams[0]/sbeta*(alpha_0*sinarg - cosarg)*yfitparams[3]);
	
	/*
	System.out.println("y = " + yfoilparams[0] + " +/- " + yfoilparams[1]);
	System.out.println("py = " + yfoilparams[2] + " +/- " + yfoilparams[3]);	
	*/
	return yfoilparams;
   }  
   
   
    private void createHResultsPlot(){
	
	hdatapanel = new FunctionGraphsJPanel();
	hdatapanel.setPreferredSize(new Dimension(400, 175));
	hdatapanel.setGraphBackGroundColor(Color.WHITE);
	hdatapanel.setName("HORIZONTAL");

    }
    
    private void createVResultsPlot(){
	
	vdatapanel = new FunctionGraphsJPanel();
	vdatapanel.setPreferredSize(new Dimension(400, 175));
	vdatapanel.setGraphBackGroundColor(Color.WHITE);
	vdatapanel.setName("VERTICAL");

    }
    
    
    public void setupModel(){	    
	accl = doc.getAccelerator();
	seq = accl.getComboSequence("Ring");

	try{
	   TransferMapTracker tracker = new TransferMapTracker();
	   //tracker.initializeFromEditContext(seq);
	   probe = ProbeFactory.getTransferMapProbe(seq, tracker);
	   scenario = Scenario.newScenarioFor(seq);
	   scenario.setProbe(probe);
	   //scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
	   scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
	   //testFixedOrbit(0.005, 0.005);
	   scenario.setStartElementId("Ring_Inj:Foil");
	   scenario.resync();
	   scenario.run();
	   traj = (Trajectory<TransferMapState>)scenario.getTrajectory();
	}
	catch(Exception exception){
	   exception.printStackTrace();
	}
    }
	
	
    void refreshHPlot(ArrayList bpms){	
	hdatapanel.removeAllGraphData();

	BasicGraphData rawgraphdata = new BasicGraphData();
	BasicGraphData fitgraphdata = new BasicGraphData();
	//Add the raw data	   
	int size = bpms.size();
	int i = 0;
	double[] sdata = new double[size];
	double[] data = new double[size];

	Iterator itr = bpms.iterator();
	double smax = 0.0;
	
	while(itr.hasNext()){
	    //Do the fit for all active BPM Agents
	    BpmAgent bpmagent = (BpmAgent)itr.next();
	    sdata[i] = bpmagent.getPosition();
	    if(sdata[i] > smax) smax = sdata[i];
	    data[i] = bpmagent.getXAvgTBTArray()[0] - bpmagent.getXAvg();
	    i++;
	}
	rawgraphdata.addPoint(sdata, data);
	
	rawgraphdata.setDrawPointsOn(true);
	rawgraphdata.setDrawLinesOn(false);
	rawgraphdata.setGraphProperty("Legend", new String("raw data"));
	rawgraphdata.setGraphColor(Color.RED);
	hdatapanel.addGraphData(rawgraphdata);
	
	//Add the fit.
	
	double points=100.0;
	double inc = smax/points;
	int npoints = (new Double(points)).intValue();

	R3 phase;
	Twiss[] twiss;
	TransferMapState state;
	PhaseVector fixedorbit;
		
	double x = 0.0;
	double s = 0.0;
	
	Iterator iterState = ((TransferMapTrajectory)traj).stateIterator();
	ArrayList posList = new ArrayList();
        ArrayList BetaList = new ArrayList(); 
        ArrayList PhaseList = new ArrayList();
	ArrayList OrbitList = new ArrayList();
	 
	while(iterState.hasNext()){
	   state = (TransferMapState)iterState.next();
           s = state.getPosition();
	   twiss = state.getTwiss();
	   phase = state.getBetatronPhase();
	   fixedorbit = state.getFixedOrbit();
	 
           posList.add(new Double(s));
           BetaList.add(new Double(twiss[0].getBeta()));
	   //System.out.println("Twiss here is " + twiss[0] + " " + state.getElementId());
           PhaseList.add(new Double(phase.getx()));
	   OrbitList.add(new Double(fixedorbit.getx()));
	}
     
       	double[] pos = new double[posList.size()];
	double[] beta = new double[posList.size()];
	double[] betaphi = new double[posList.size()];
	double[] orbit = new double[posList.size()];
	double[] yfit = new double[posList.size()];
       
      	for(i=0; i<posList.size(); i++) {
	    pos[i] = ((Double) posList.get(i)).doubleValue();
	    beta[i] = ((Double) BetaList.get(i)).doubleValue();
	    betaphi[i] = ((Double) PhaseList.get(i)).doubleValue();
	    orbit[i] = ((Double) OrbitList.get(i)).doubleValue();
	}	
      
      	for(i=0; i<posList.size(); i++) {
	    yfit[i] = (xfitparams[0]*Math.sqrt(beta[i])*Math.cos(betaphi[i] + xfitparams[1]));
	}
	
	fitgraphdata.addPoint(pos, yfit);
	fitgraphdata.setDrawPointsOn(false);
	fitgraphdata.setDrawLinesOn(true);
	fitgraphdata.setGraphProperty("Legend", new String("fit data"));
	fitgraphdata.setGraphColor(Color.BLACK);
	hdatapanel.addGraphData(fitgraphdata);
	hdatapanel.setLegendButtonVisible(true);
	hdatapanel.setChooseModeButtonVisible(true);
	//hdatapanel.setName("   "+ label);	
    } 
    
    
    void refreshVPlot(ArrayList bpms){	
	vdatapanel.removeAllGraphData();

	BasicGraphData rawgraphdata = new BasicGraphData();
	BasicGraphData fitgraphdata = new BasicGraphData();
	//Add the raw data	   
	int size = bpms.size();
	int i = 0;
	double[] sdata = new double[size];
	double[] data = new double[size];

	Iterator itr = bpms.iterator();
	double smax = 0.0;
	
	while(itr.hasNext()){
	    //Do the fit for all active BPM Agents
	    BpmAgent bpmagent = (BpmAgent)itr.next();
	    sdata[i] = bpmagent.getPosition();
	    if(sdata[i] > smax) smax = sdata[i];
	    data[i] = bpmagent.getYAvgTBTArray()[0] - bpmagent.getYAvg();
	    i++;
	}
	rawgraphdata.addPoint(sdata, data);
	
	rawgraphdata.setDrawPointsOn(true);
	rawgraphdata.setDrawLinesOn(false);
	rawgraphdata.setGraphProperty("Legend", new String("raw data"));
	rawgraphdata.setGraphColor(Color.RED);
	vdatapanel.addGraphData(rawgraphdata);
	
	//Add the fit.
	
	double points=100.0;
	double inc = smax/points;
	int npoints = (new Double(points)).intValue();

	R3 phase;
	Twiss[] twiss;
	TransferMapState state;
	PhaseVector fixedorbit;
	PhaseVector coordinates;
	
	double x = 0.0;
	double s = 0.0;
	
	Iterator iterState = ((TransferMapTrajectory)traj).stateIterator();
	ArrayList posList = new ArrayList();
        ArrayList BetaList = new ArrayList(); 
        ArrayList PhaseList = new ArrayList();
	ArrayList OrbitList = new ArrayList();
	ArrayList CoordList = new ArrayList();
	 
	while(iterState.hasNext()){
	   state = (TransferMapState)iterState.next();
           s = state.getPosition();
	   twiss=state.getTwiss();
	   phase = state.getBetatronPhase();
	   coordinates = state.phaseCoordinates();
	   fixedorbit = state.getFixedOrbit();
	   
           posList.add(new Double(s));
           BetaList.add(new Double(twiss[1].getBeta()));
           PhaseList.add(new Double(phase.gety()));
	   OrbitList.add(new Double(fixedorbit.gety()));
	   CoordList.add(new Double(coordinates.getx()));
	}
     
       	double[] pos = new double[posList.size()];
	double[] beta = new double[posList.size()];
	double[] betaphi = new double[posList.size()];
	double[] orbit = new double[posList.size()];
	double[] coords = new double[posList.size()];
	double[] yfit = new double[posList.size()];
       
      	for(i=0; i<posList.size(); i++) {
	    pos[i] = ((Double) posList.get(i)).doubleValue();
	    beta[i] = ((Double) BetaList.get(i)).doubleValue();
	    betaphi[i] = ((Double) PhaseList.get(i)).doubleValue();
	    orbit[i] = ((Double) OrbitList.get(i)).doubleValue();
	    coords[i] = ((Double) CoordList.get(i)).doubleValue();
	}	
      
      	for(i=0; i<posList.size(); i++) {
	    yfit[i] = (yfitparams[0]*Math.sqrt(beta[i])*Math.cos(betaphi[i] + yfitparams[1]));
	    //yfit[i] = orbit[i];
	}
	
	fitgraphdata.addPoint(pos, yfit);
	fitgraphdata.setDrawPointsOn(false);
	fitgraphdata.setDrawLinesOn(true);
	fitgraphdata.setGraphProperty("Legend", new String("fit data"));
	fitgraphdata.setGraphColor(Color.BLACK);
	vdatapanel.addGraphData(fitgraphdata);
	vdatapanel.setLegendButtonVisible(true);
	vdatapanel.setChooseModeButtonVisible(true);
    } 
    
    private void testFixedOrbit(double hstrength, double vstrength){
	scenario.setModelInput(seq.getNodeWithId("Ring_Mag:DCH_B06"), ElectromagnetPropertyAccessor.PROPERTY_FIELD, hstrength);
	scenario.setModelInput(seq.getNodeWithId("Ring_Mag:DCV_B07"), ElectromagnetPropertyAccessor.PROPERTY_FIELD, vstrength);
    }
    
}



