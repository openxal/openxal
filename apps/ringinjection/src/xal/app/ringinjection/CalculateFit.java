/*
 * CalculateFit.java
 *
 * Created on April 1, 2004 
 */

package xal.app.ringinjection;
import java.util.*;
import java.util.*;
import java.util.HashMap;
import java.lang.*;
import java.net.*;
import java.io.*;
import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.event.*;

import xal.ca.*;
import xal.extension.application.*;
import xal.ca.*;
import xal.tools.*;
import xal.tools.beam.*;
import xal.tools.xml.*;
import xal.extension.fit.lsm.*;
import xal.tools.data.*;
import xal.tools.messaging.*;
import xal.tools.xml.XmlDataAdaptor;

import xal.smf.*;
import xal.smf.impl.*;
import xal.extension.application.smf.*;
import xal.smf.data.*;
import xal.model.*;
import xal.smf.proxy.*;
import xal.model.probe.traj.*;
import xal.model.probe.*; 
import xal.model.xml.*; 
import xal.sim.scenario.*;
import xal.tools.math.r3.R3;
import xal.extension.widgets.plot.*;
import xal.model.alg.*;

/**
 * Performs the fit for the turn-by-turn signal on a BPM.  Records the 
 * fit parameters in the BPMAgent class.
 * @author  cp3
 */

public class CalculateFit{
	
    protected Channel channel;
    protected BpmAgent localagent;
    
    public ArrayList fitparams;
    private Accelerator accl = new Accelerator();
    private AcceleratorSeqCombo seq;
    private TransferMapProbe probe;
    private Scenario scenario;
    private Trajectory<TransferMapState> traj;
    private String syncstate;
	
    double xtune;
    double xphase;
    double xslope;
    double xamp;
    double xoffset;
    
    double ytune;
    double yphase;
    double yslope;
    double yamp; 
    double yoffset;
    
    double xamp_err;
    double yamp_err;
    double xphase_err;
    double yphase_err;
    
    
    GenDocument doc;
    /** Creates new CalculateFit*/
    public CalculateFit(GenDocument aDocument, String latticestate) {
		doc = aDocument;
		syncstate = latticestate;
		setupModel();
    }
    
    public void bpmXFit(BpmAgent bpmagent, int points) {
		int i;
		
		double[] iarr;
		double[] xarr;
		
		localagent = bpmagent;
		
		iarr = new double[points];
		xarr = new double[points];
		if(localagent.isConnected()){
			System.arraycopy(localagent.getXAvgTBTArray(), 0, xarr, 0, points);
		}
		else{
			System.out.println("BPM " + localagent + " is not connected.");
		}
		
		for(i=0; i<points; i++){
			iarr[i]=(new Integer(i)).doubleValue();
		}
		
		
		Cosine xcs = new Cosine();
		
		xcs.clear();
		xcs.use_slope_positive = true;
		xcs.setData(iarr, xarr);
		xcs.fitParameter(Cosine.TUNE, true);
		xcs.fitParameter(Cosine.PHASE, true);
		xcs.fitParameter(Cosine.SLOPE, true);
		xcs.fitParameter(Cosine.AMP, true);
		xcs.fitParameter(Cosine.OFFSET, false);
		
		int iterations = 1;
		System.out.println("Here about to call guess and fit.");
		boolean result = xcs.guessAndFit(iterations);
		
		System.out.println("Guessed Slope = " + xcs.getParameter(Cosine.SLOPE) + " +- " + xcs.getParameterError(Cosine.SLOPE));	
		
		xcs.setParameter(Cosine.TUNE, xcs.getParameter(Cosine.TUNE));
		xcs.setParameter(Cosine.PHASE, xcs.getParameter(Cosine.PHASE));
		xcs.setParameter(Cosine.SLOPE, xcs.getParameter(Cosine.SLOPE));
		xcs.setParameter(Cosine.AMP, xcs.getParameter(Cosine.AMP));       
		xcs.setParameter(Cosine.OFFSET, xcs.getParameter(Cosine.OFFSET));
		
		iterations=35;
		result = xcs.fit();
		
		
		System.out.println("\nHorizontal fit results for BPM " + localagent.name());
		System.out.println("Tune = " + xcs.getParameter(Cosine.TUNE) + " +- " + xcs.getParameterError(Cosine.TUNE));		
		System.out.println("Phase = " + xcs.getParameter(Cosine.PHASE) + " +- " + xcs.getParameterError(Cosine.PHASE));
		System.out.println("Slope = " + xcs.getParameter(Cosine.SLOPE) + " +- " + xcs.getParameterError(Cosine.SLOPE));		
		System.out.println("Amp = " + xcs.getParameter(Cosine.AMP) + " +- " + xcs.getParameterError(Cosine.AMP));
		System.out.println("Offset = " + xcs.getParameter(Cosine.OFFSET) + " +- " + xcs.getParameterError(Cosine.OFFSET) + "\n");
		
		
		xtune = xcs.getParameter(Cosine.TUNE);
		xphase = xcs.getParameter(Cosine.PHASE);
		xslope = xcs.getParameter(Cosine.SLOPE);
		xamp = xcs.getParameter(Cosine.AMP);
		xoffset = xcs.getParameter(Cosine.OFFSET);
		
		xamp_err = xcs.getParameterError(Cosine.AMP);
		xphase_err = xcs.getParameterError(Cosine.PHASE);
		
		//Store the fit data and results in the bpm agent class.
		
		double[] xfitparams = new double[5];
		xfitparams[0] = xtune;
		xfitparams[1] = xphase;
		xfitparams[2] = xslope;
		xfitparams[3] = xamp;
		xfitparams[4] = xoffset;
		
		localagent.setDataSize(points);
		localagent.saveXTBTData(xarr);  
		localagent.setXTBTFitParams(xfitparams);
		
	}
	
	
	public void bpmYFit(BpmAgent bpmagent, int points) {
		int i;
		
		double[] iarr;
		double[] yarr;
		
		localagent = bpmagent;
		
		iarr = new double[points];
		yarr = new double[points];
		System.arraycopy(localagent.getYAvgTBTArray(), 0, yarr, 0, points);
		
		for(i=0; i<points; i++){
			iarr[i]=(new Integer(i)).doubleValue();
			//yarr_err[i] = 0.001;
			//System.out.println(iarr[i] + " " + yarr[i]);
		}
		
		Cosine ycs = new Cosine();
		
		ycs.clear();
		
		ycs.setData(iarr, yarr);
		ycs.fitParameter(Cosine.TUNE, true);
		ycs.fitParameter(Cosine.PHASE, true);
		ycs.fitParameter(Cosine.SLOPE, true);
		ycs.fitParameter(Cosine.AMP, true);
		ycs.fitParameter(Cosine.OFFSET, true);
		
		int iterations = 1;
		boolean result = ycs.guessAndFit(iterations);
		
		ycs.setParameter(Cosine.TUNE, ycs.getParameter(Cosine.TUNE));
		ycs.setParameter(Cosine.PHASE, ycs.getParameter(Cosine.PHASE));
		ycs.setParameter(Cosine.SLOPE, ycs.getParameter(Cosine.SLOPE));
		ycs.setParameter(Cosine.AMP, ycs.getParameter(Cosine.AMP)); 
		ycs.setParameter(Cosine.OFFSET, ycs.getParameter(Cosine.OFFSET)); 
		
		iterations=35;
		result = ycs.fit();
		
		
		System.out.println("\nVertical fit results for BPM " + localagent.name());
		System.out.println("Tune = " + ycs.getParameter(Cosine.TUNE) + " +- " + ycs.getParameterError(Cosine.TUNE));		
		System.out.println("Phase = " + ycs.getParameter(Cosine.PHASE) + " +- " + ycs.getParameterError(Cosine.PHASE));
		System.out.println("Slope = " + ycs.getParameter(Cosine.SLOPE) + " +- " + ycs.getParameterError(Cosine.SLOPE));		
		System.out.println("Amp = " + ycs.getParameter(Cosine.AMP) + " +- " + ycs.getParameterError(Cosine.AMP));
		System.out.println("Offset = " + ycs.getParameter(Cosine.OFFSET) + " +- " + ycs.getParameterError(Cosine.OFFSET) + "\n");
		
		
		ytune = ycs.getParameter(Cosine.TUNE);
		yphase = ycs.getParameter(Cosine.PHASE);
		yslope = ycs.getParameter(Cosine.SLOPE);
		yamp = ycs.getParameter(Cosine.AMP);
		yoffset = ycs.getParameter(Cosine.OFFSET);
		
		yamp_err = ycs.getParameterError(Cosine.AMP);
		yphase_err = ycs.getParameterError(Cosine.PHASE);
		
		//Store the fit data and results in the bpm agent class.
		
		double[] yfitparams = new double[5];
		yfitparams[0] = ytune;
		yfitparams[1] = yphase;
		yfitparams[2] = yslope;
		yfitparams[3] = yamp;
		yfitparams[4] = yoffset;
		
		localagent.setDataSize(points);
		localagent.saveYTBTData(yarr);  
		localagent.setYTBTFitParams(yfitparams);
		
		getFoilParams();
		
	}
	
	
    public void setupModel(){	    
		accl = doc.getAccelerator();
		seq = accl.getComboSequence("Ring");
		
		try{
			TransferMapTracker tracker = new TransferMapTracker();
			System.out.println("TransferMapTracker is " + tracker + " " + seq.getId());
			probe = ProbeFactory.getTransferMapProbe(seq, tracker);
			scenario = Scenario.newScenarioFor(seq);
			scenario.setProbe(probe);
			if(syncstate == "Live"){
				scenario.setSynchronizationMode( Scenario.SYNC_MODE_RF_DESIGN );
			}
			else{
				scenario.setSynchronizationMode( Scenario.SYNC_MODE_DESIGN );
			}
			
			//scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
			scenario.setStartElementId("Ring_Inj:Foil");
			scenario.resync();
			scenario.run();
			traj = (TransferMapTrajectory)scenario.getTrajectory();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
    }
    
    
    void getFoilParams(){
		
     	//ProbeState injstate = traj.stateForElement("Ring_Inj:Foil");
		double position = scenario.getPositionRelativeToStart(0.0);
		//TransferMapState injstate = (TransferMapState)traj.statesInPositionRange(position - 0.00001, position + 0.00001)[0];
		TransferMapState injstate = (TransferMapState)traj.stateForElement("Ring_Inj:Foil");
		Twiss[] injtwiss=injstate.getTwiss();
		R3 injphase = injstate.getBetatronPhase();
		PhaseVector injorbit = injstate.getFixedOrbit();
		double beta_x_i = injtwiss[0].getBeta(); 
		double alpha_x_i = injtwiss[0].getAlpha();
		double beta_y_i = injtwiss[1].getBeta(); 
		double alpha_y_i = injtwiss[1].getAlpha();
		
		//System.out.println("At position " + position + ", beta x, alpha_x, beta y, and alphay are "+ beta_x_i + " " + alpha_x_i + " " + beta_y_i + " " + alpha_y_i + "\n");
		
		
		TransferMapState localstate = (TransferMapState)traj.statesForElement(localagent.getNode().getId())[0];
		Twiss[] localtwiss=localstate.getTwiss();
		R3 localphase = localstate.getBetatronPhase();
		PhaseVector localorbit = localstate.getFixedOrbit();
		double beta_x = localtwiss[0].getBeta(); 
		double alpha_x = localtwiss[0].getAlpha();
		double beta_y = localtwiss[1].getBeta(); 
		double alpha_y = localtwiss[1].getAlpha();
		double phase_x = localphase.getx();
		double phase_y = localphase.gety();
		
		//Calculate the position and angle at the foil.
		
      	double[] xfinalParams = new double[4];
		double[] yfinalParams = new double[4];
		double xarg = phase_x - xphase;
		double yarg = phase_y - yphase;
		
       	xfinalParams[0]=xamp*Math.sqrt(beta_x_i/beta_x)*Math.cos(xarg);
		yfinalParams[0]=yamp*Math.sqrt(beta_y_i/beta_y)*Math.cos(yarg);
		
       	xfinalParams[1]=Math.abs((Math.sqrt(beta_x_i/beta_x)*Math.cos(xarg) * xamp_err + 
								  xamp*Math.sqrt(beta_x_i/beta_x)*Math.sin(xarg) * xphase_err));
		yfinalParams[1]=Math.abs((Math.sqrt(beta_y_i/beta_y)*Math.cos(yarg) * yamp_err + 
								  yamp*Math.sqrt(beta_y_i/beta_y)*Math.sin(yarg) * yphase_err));		
		
       	xfinalParams[2]=xamp/Math.sqrt(beta_x_i*beta_x)*(Math.sin(xarg) - alpha_x_i*Math.cos(xarg));
       	yfinalParams[2]=yamp/Math.sqrt(beta_y_i*beta_y)*(Math.sin(yarg) - alpha_y_i*Math.cos(yarg));
		
		
		double term1 = (Math.sin(xarg) - alpha_x_i*Math.cos(xarg)) * xamp_err;
		double term2 = xamp*(Math.cos(xarg) + alpha_x_i*Math.sin(xarg)) * xphase_err;
		
		xfinalParams[3]=Math.abs(1/(Math.sqrt(beta_x_i*beta_x)) * (term1 - term2));
		
		term1 = (Math.sin(yarg) - alpha_y_i*Math.cos(yarg)) * yamp_err;
		term2 = yamp*(Math.cos(yarg) + alpha_y_i*Math.sin(yarg)) * yphase_err;
		
		yfinalParams[3]=Math.abs(1/(Math.sqrt(beta_y_i*beta_y)) * (term1 - term2));
		
       	localagent.setXFoilResults(xfinalParams);
		localagent.setYFoilResults(yfinalParams);
		
	}
	
	public void changeSyncState(String latticestate){
		syncstate = latticestate;
		setupModel();
		System.out.println("Here1");
	}
	
}




