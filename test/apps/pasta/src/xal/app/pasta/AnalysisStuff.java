/*
 * PastaDocument.java
 *
 * Created on June 14, 2004
 */

package xal.app.pasta;

import java.awt.Toolkit;
import java.util.*;
import javax.swing.table.*;
import java.text.*;
import java.io.*;

import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.tools.apputils.*;
import xal.extension.fit.spline.*;
import xal.tools.dispatch.DispatchQueue;
import xal.extension.widgets.plot.*;
import xal.tools.beam.*;
import xal.tools.math.TrigStuff;
import xal.extension.scan.*;
import xal.model.xml.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.model.alg.ParticleTracker;
import xal.sim.scenario.Scenario;
import xal.tools.xml.XmlDataAdaptor;
import xal.extension.fit.spline.*;
import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.AlgorithmFactory;
/**
 * This class contains the components internal to the Scanning procedure.
 * @author  jdg
 */
public class AnalysisStuff {
    
    /** containers for fits of measured data */
    private HashMap<Integer, CubicSpline> splineFitsBPMDiff, splineFitsBPMAmp, splineFitsWOut;
    
    /** containers for the model predicted arrays */
    protected HashMap<Integer, Vector<Double>> phasesCavModelScaledV, phaseDiffsBPMModelV, WOutsV;
    protected HashMap<Integer, BasicGraphData> WOutModelMap = new HashMap<Integer, BasicGraphData>();
    
    /** containers for measured data */
    protected HashMap<Integer, Vector<Double>> phasesCavMeasured, phaseDiffsBPMMeasured;
    
    /** container for cavity phase points to evaluate BPM phases at */
    private Vector<Double> calcPointsV = new Vector<Double>();
    
    /** container for the parametric amplitude values of the measurement */
    private Vector<Double> paramMeasuredVals = new Vector<Double>();
    
    /** The index value (starting at 0) of the parametic curve to vary the
     * cavity amplitude for. The other amplitude values in ampValueV
     * are scaled from this indexed value, using paramMeasuredVals values */
    protected int amplitudeVariableIndex = 1;
    
    /** the setpoint to uise for the cavity phase (deg) */
    protected double cavPhaseSetpoint;
    /** the setpoint to use for the cavity amplitude (AU)*/
    protected double cavAmpSetpoint;
    
    // Stuff to save with the document:
    
    /** the minimum phase to start model scan from */
    protected double phaseModelMin=-90;
    /** the maximum phase to start model scan to */
    protected double phaseModelMax = 30;
    /** the number of model points to use per scan */
    protected int nCalcPoints = 20;
    /** the minimum BPM amplitude for points to consider in the scanning (mA) */
    protected double minBPMAmp = 5.;
    
    /** a multiplier used to get evaluation points more dense near the scan edges */
    private double stepMultiplier = 1.1;        // CKA - NEVER USED
    private double rad2deg = 180./Math.PI;
    
    /** defines the phase quadrant to work in */
    private double BPMPhaseMin = -180.;
    
    /** the number of parametric values for amplitude settings */
    protected int nParamAmpVals = 0;
    
    /** the minimum BPM amplitude, below which data is not parsed in
     * it may be further filtered for analysis purposes later */
    private double minBPMAmpRead= 1.;
    
    // Stuff used in the analysis calculations.
    
    /** the phase offset to apply to the model to line up the BPM phase with measurements (deg) */
    protected double cavPhaseOffset = 0.;
    /** the input energy of the beam (MeV) */
    protected double WIn = 2.5;
    /** the calculated output energy (MeV) */
    protected double WOutCalc;
    
    /** the nominal cavity voltage (MV/m) */
    protected double cavityVoltage = 1.;
    
    /** Container for the amplitude scaling factors */
    protected Vector<Double> ampValueV = new Vector<Double>();
    
    // stuff for 1 vs 2 BPM analysis
    /** flag indicating whether to use a single BPM */
    protected boolean useOneBPM = false;
    /** calculated BPM phases with the cavity off */
    protected double BPM1TimeCavOff, BPM2TimeCavOff;
    /** the BPMs to use in thge analysis for calculating phase differences */
    protected BPM firstBPM, secondBPM;
    
    /** the total error for the match
     * = sqrt(sum squares of [measure-model])*/
    protected double errorTotal = 0.;
    
    // analysis table stuff
    protected AnalysisTableModel analysisTableModel;
    
    // solver control stuff
    /** the time to try and solve (sec) */
    protected double timeoutPeriod = 100.;
    
    /** collection to hold the solver variables */
    protected Hashtable<String, Variable> variableList = new Hashtable<String, Variable>();
    /** Id for the type algorithm to use */
    protected String algorithmId = RANDOM_ALG;
    /** the solver algorithm */
    protected SearchAlgorithm algorithm;
    /** The solver */
    protected Solver solver;// = new Solver();
    /** the scorer object */
    private PastaScorer theScorer = new PastaScorer();
    
    // model stuff
    /** the probe to use for the model */
    protected Probe<?> theProbe;
    /** the default file for the probe file */
    protected File probeFile;
    /** the default filename for the probe file */
    protected String probeFileName;
    /** the model scenario */
    protected Scenario theModel;
    /** the default input energy for the cavity - just use the initial probe value */
    protected double defaultEnergy = 0.;
    
    // ready flags:
    /** flag if model is ready to run */
    protected boolean modelReady = false;
    /** flag that the scan data is imported */
    protected boolean analysisDataReady = false;
    /** flag the the solver variables are properly initialized */
    protected boolean solverReady = false;
    
    /** flag indicatingf whether data is aquired yet */
    protected boolean haveData = false;
    
    /** flag indicating whether to do analysis using the cavity-off information or not */
    protected boolean useCavOffData;
    
    /** Array to hold Booleans for variable use */
    protected ArrayList<Object> variableActiveFlags = new ArrayList<Object>();
    
    // Application stuff
    /** the document this belongs to */
    protected PastaDocument theDoc;
    
    //gr labels:
    static String RANDOM_ALG = "Random";
    static String SIMPLEX_ALG = "Simplex";
    static String POWELL_ALG = "Powell";
    static String Combo_ALG = "Combo";
    static String WIN_VAR_NAME = "WIn";
    static String PHASE_VAR_NAME = "CavPhaseOffset";
    static String AMP_VAR_NAME = "CavAmplitude";
    static String FF_VAR_NAME = "FudgeOffset";
    
    /** Create an object */
    public AnalysisStuff(PastaDocument doc) {
        
        theDoc = doc;
                
        for (int i = 0; i<3; i++) variableActiveFlags.add(new Boolean(true));
        variableActiveFlags.add(new Boolean(false));
        
        analysisTableModel = new AnalysisTableModel(this);
        
        splineFitsBPMDiff = new HashMap<Integer, CubicSpline>();
        splineFitsBPMAmp = new HashMap<Integer, CubicSpline>();
        splineFitsWOut = new HashMap<Integer, CubicSpline>();
        phasesCavModelScaledV = new HashMap<Integer, Vector<Double>>();
        phaseDiffsBPMModelV = new HashMap<Integer, Vector<Double>>();
        WOutsV = new HashMap<Integer, Vector<Double>>();
        phaseDiffsBPMMeasured = new HashMap<Integer, Vector<Double>>();
        phasesCavMeasured = new HashMap<Integer, Vector<Double>>();
        
        makeCalcPoints();
    }
    
    /** initialize the analysis stuff, after the setup + scan stuff is done */
    protected void init() {
	    useCavOffData = theDoc.scanStuff.haveCavOffData() && (theDoc.myWindow()).useCavOffBox.isSelected();
	    if(!useCavOffData && (!theDoc.myWindow().useBPM1Box.isSelected() || !theDoc.myWindow().useBPM2Box.isSelected() ) ) {
            Toolkit.getDefaultToolkit().beep();
            String errText = "Cavity Off data must exist to use only 1 BPM";
            theDoc.myWindow().errorText.setText(errText);
            System.err.println(errText);
            return;
		    
	    }
	    updateAnalysisData();
	    theDoc.myWindow().updateScanNumberSelector();
	    theDoc.myWindow().updateScanUseSelector();
	    theDoc.myWindow().plotMeasuredData();
	    updateAmpFactors();
	    analysisTableModel.fireTableDataChanged();
	    analysisDataReady = true;
	    theDoc.setHasChanges(true);
    }
    
    /** initialize the cavity voltages by scaling from the measured parametric values
     * Note - one of the amplitude settings is set from the user/solver.
     * this is specified by amplitudeVariableIndex. The others are scaled.
     */
    protected void updateAmpFactors() {
	    
	    if(amplitudeVariableIndex < 0) {
            Toolkit.getDefaultToolkit().beep();
            String errText = "Please pick a scan number for the voltage variable to correspond to.";
            theDoc.myWindow().errorText.setText(errText);
            System.err.println(errText);
            return;
	    }
	    
	    // check for sanity:
	    if(amplitudeVariableIndex > nParamAmpVals-1)
		    amplitudeVariableIndex = nParamAmpVals-1;
	    ampValueV.clear();
	    
        
	    double nominalField = avgFieldRB(amplitudeVariableIndex);
        
	    for (int i=0; i< nParamAmpVals; i++) {
		    // if a guess for the cavity voltage is not given, prescribe "one"
		    if(i == amplitudeVariableIndex)
			    ampValueV.add(new Double(cavityVoltage));
		    else {
                double ratio = 0.;
                double field1 = avgFieldRB(i);
                if(nominalField != 0) ratio = field1/nominalField;
                //System.out.println("i = " + i + " field1 = " + field1 + " nomfield = " + nominalField);
                if (ratio == 0.) {
                    // backward compatibility case, before measured E-field were taken:
                    ratio = ( paramMeasuredVals.get(i)).doubleValue()/ (paramMeasuredVals.get(amplitudeVariableIndex)).doubleValue();
                }
                double val2 = ratio * cavityVoltage;
                ampValueV.add(new Double(val2));
		    }
        }
    }
    
    /** gets the average value of the field readback, for a requeseted scan index */
    double avgFieldRB(int index) {
        if(theDoc.scanStuff.cavAmpRBMV == null) {
            Toolkit.getDefaultToolkit().beep();
            String errText = "Requested cav field container does not exist yet";
            theDoc.myWindow().errorText.setText(errText);
            System.err.println(errText);
            return 0.;
        }
        
        if(theDoc.scanStuff.cavAmpRBMV.getNumberOfDataContainers() < index) {
            Toolkit.getDefaultToolkit().beep();
            String errText = "Requested cav field info for a scan number that does not exist? index =  " + (new Integer(index)).toString();
            theDoc.myWindow().errorText.setText(errText);
            System.err.println(errText);
            return 0.;
        }
        
        if( theDoc.scanStuff.cavAmpRBMV.getDataContainer(index) == null) {
            Toolkit.getDefaultToolkit().beep();
            String errText = "Requested field readback info does not exist, index =  " + (new Integer(index)).toString();
            theDoc.myWindow().errorText.setText(errText);
            System.err.println(errText);
            return 0.;
        }
        
        BasicGraphData cavFieldBGD = theDoc.scanStuff.cavAmpRBMV.getDataContainer(index);
        double sum = 0.;
        for (int i=0; i < cavFieldBGD.getNumbOfPoints(); i++) sum += cavFieldBGD.getY(i);
        return sum/((double) cavFieldBGD.getNumbOfPoints());
    }
    
    
    
    /** this method takes raw data from the scan Stuff and puts it into the analysis
     * containers. Some filtering is done here, to restrict data to above some critical
     * BPM amplitude.
     */
    
    protected void updateAnalysisData() {
	    clearData();
        
	    nParamAmpVals = theDoc.scanStuff.BPM1PhaseMV.getNumberOfDataContainers();
	    if( theDoc.scanStuff.BPM2PhaseMV.getNumberOfDataContainers() != nParamAmpVals ||
           theDoc.scanStuff.BPM1AmpMV.getNumberOfDataContainers() != nParamAmpVals ||
           theDoc.scanStuff.BPM2AmpMV.getNumberOfDataContainers() != nParamAmpVals) {
			Toolkit.getDefaultToolkit().beep();
			String errText = "Opps, The number of parametric scans is different for the some of the BPM amp. and phases\nDid the scan complete?";
			theDoc.myWindow().errorText.setText(errText);
			System.err.println(errText);
			return;
        }
        
        double cavPhase, amp1, amp2, theAmp, phase1, phase2;
        
        // get the data from the scan stuff and store it in our own containers.
        
        for (int i=0; i<nParamAmpVals; i++) {
            
            BasicGraphData bgdBPMAmp1 = theDoc.scanStuff.BPM1AmpMV.getDataContainer(i);
            BasicGraphData bgdBPMAmp2 = theDoc.scanStuff.BPM2AmpMV.getDataContainer(i);
            BasicGraphData bgdBPMPhase1 = theDoc.scanStuff.BPM1PhaseMV.getDataContainer(i);
            BasicGraphData bgdBPMPhase2 = theDoc.scanStuff.BPM2PhaseMV.getDataContainer(i);
            
            Double paramValue = (Double) bgdBPMAmp1.getGraphProperty("PARAMETER_VALUE_RB");
            paramMeasuredVals.add(paramValue);
            
            // check for equal number of points per parametic scan for all amplitude and phase curves
            
			if(bgdBPMAmp1.getNumbOfPoints() != bgdBPMAmp2.getNumbOfPoints()  ||
               bgdBPMAmp1.getNumbOfPoints() != bgdBPMPhase1.getNumbOfPoints() ||
               bgdBPMAmp1.getNumbOfPoints() != bgdBPMPhase2.getNumbOfPoints() ) {
				Toolkit.getDefaultToolkit().beep();
				String errText = "The number of points a parametric scan is not the same for BPM amp. and phases\nDid the scan complete?";
				theDoc.myWindow().errorText.setText(errText);
				System.err.println(errText);
				return;
            }
            
            int np = bgdBPMAmp1.getNumbOfPoints();
            Vector<Double> cavPhaseV = new Vector<Double>();
            Vector<Double> BPMPhaseDiffV = new Vector<Double>();
            Vector<Double> BPMAmpV = new Vector<Double>();
            
            minBPMAmpRead = minBPMAmp; // filter using the same value as for analysis later
            
            int index;
            for (int j=0; j< np; j++) {
                cavPhase = bgdBPMAmp1.getX(j);
                double diff;
                double cavPhaseScaled = cavPhase + theDoc.DTLPhaseOffset;
                double delta = cavPhaseScaled - 180.;
                if(delta >  0.) cavPhaseScaled = -180. + delta;
                if(delta < -360.) cavPhaseScaled = 540. + delta;
                
                amp1 = bgdBPMAmp1.getValueY(cavPhase);
                amp2 = bgdBPMAmp2.getValueY(cavPhase);
                theAmp = amp2;
                if(!theDoc.myWindow().useBPM1Box.isSelected()) theAmp = amp2;
                if(!theDoc.myWindow().useBPM2Box.isSelected()) theAmp = amp1;
                if(theDoc.myWindow().useBPM1Box.isSelected() && theDoc.myWindow().useBPM2Box.isSelected()) theAmp = Math.min(amp1, amp2);
                // filter if the beam intensity is too small
                if(theAmp> minBPMAmpRead) {
                    phase1 = bgdBPMPhase1.getValueY(cavPhase);
                    phase2 = bgdBPMPhase2.getValueY(cavPhase);
                    if(useOneBPM) {
                        if(theDoc.myWindow().useBPM1Box.isSelected())
                            diff = phase1 -  theDoc.scanStuff.BPM1CavOffPhase(cavPhase);
                        else
                            diff = phase2 -  theDoc.scanStuff.BPM2CavOffPhase(cavPhase);
                    }
                    else {
                        diff = phase2 - phase1;
                        if(useCavOffData) {
                            diff -= (theDoc.scanStuff.BPM2CavOffPhase(cavPhase) - theDoc.scanStuff.BPM1CavOffPhase(cavPhase));
                        }
                    }
                    diff += theDoc.BPMPhaseDiffOffset;
                    // wrap to a 2-pi  interval:
                    if(diff < BPMPhaseMin) diff += 360.;
                    if( diff > BPMPhaseMin+360.) diff -= 360.;
                    index = findInsertPoint(cavPhaseV, cavPhaseScaled);
                    if(index < 0) {
                        cavPhaseV.add(new Double(cavPhaseScaled));
                        BPMPhaseDiffV.add(new Double(diff));
                        BPMAmpV.add(new Double(theAmp));
                    }
                    else {
                        cavPhaseV.insertElementAt(new Double(cavPhaseScaled), index);
                        BPMPhaseDiffV.insertElementAt(new Double(diff), index);
                        BPMAmpV.insertElementAt(new Double(theAmp), index);
                        
                    }
                    
                }
            }
            
            // make fits of the measured BPM diff andf amplitude to use later
            // stash them
            if(cavPhaseV.size() < 5) {
                Toolkit.getDefaultToolkit().beep();
                String errText = "Hmm - there do not appear to be enough good points to analyzel";
                theDoc.myWindow().errorText.setText(errText);
                System.err.println(errText);
                return;
                
            }
            
            if(theDoc.myWindow().useWrappingButton.isSelected() && (BPMPhaseDiffV.size() > 0)) {
                // unwrap data option here:
                double previousVal = ( BPMPhaseDiffV.get(0)).doubleValue();
                for (int k = 1; k < BPMPhaseDiffV.size(); k++) {
                    double oldVal = ( BPMPhaseDiffV.get(k)).doubleValue();
                    double newVal = TrigStuff.unwrap(oldVal, previousVal);
                    BPMPhaseDiffV.setElementAt(new Double(newVal), k);
                    previousVal = newVal;
                }
            }
            
            phasesCavMeasured.put(new Integer(i), cavPhaseV);
            phaseDiffsBPMMeasured.put(new Integer(i), BPMPhaseDiffV);
            CubicSpline splineFitBPMDiff = new CubicSpline(toDouble(cavPhaseV), toDouble(BPMPhaseDiffV));
            CubicSpline splineFitBPMAmp = new CubicSpline(toDouble(cavPhaseV), toDouble(BPMAmpV));
            splineFitsBPMAmp.put(new Integer(i), splineFitBPMAmp);
            splineFitsBPMDiff.put(new Integer(i), splineFitBPMDiff);
            
            theDoc.useScanInMatch.add(new Boolean(true));
        }
    }
    
    /** clear the containers of data */
    private void clearData() {
        splineFitsBPMDiff.clear();
        splineFitsBPMAmp.clear();
        splineFitsWOut.clear();
        phasesCavModelScaledV.clear();
        phaseDiffsBPMModelV.clear();
        WOutsV.clear();
        phasesCavMeasured.clear();
        phaseDiffsBPMMeasured.clear();
        paramMeasuredVals.clear();
        theDoc.useScanInMatch.clear();
        haveData = false;
        // check for 1 vs. 2 BPMs
        if(!theDoc.myWindow().useBPM1Box.isSelected() && !theDoc.myWindow().useBPM2Box.isSelected()) {
		    Toolkit.getDefaultToolkit().beep();
		    String errText = "Hey dude - you gotta select at least 1 BPM to analyze";
		    theDoc.myWindow().errorText.setText(errText);
		    System.err.println(errText);
		    return;
        }
        
        if(theDoc.myWindow().useBPM1Box.isSelected() && theDoc.myWindow().useBPM2Box.isSelected()) {
            useOneBPM = false;
        }
        else
            useOneBPM = true;
    }
    
    /** refresh both measured and model plot data */
    
    protected void plotUpdate() {
	    theDoc.myWindow().plotMeasuredData();;
	    theDoc.myWindow().plotModelData();
    }
    
    /** this method returns the phase of a node in a trajectory, relative to the first point in the model run for this state. 
     * 
     * CKA - NEVER USED
     */
    private double getPhase( ProbeState<? extends ProbeState<?>> state, BPM bpm) {
	    double freq = bpm.getBPMBucket().getFrequency() * 1.e6;
	    
	    // correction time for electrode being offset from the BPM center:
	    double gamma = 1. + state.getKineticEnergy()/state.getSpeciesRestEnergy();
	    double beta = Math.sqrt(1. - 1./(gamma*gamma));
	    double dt = ( bpm.getBPMBucket().getOrientation() * bpm.getBPMBucket().getLength()) / (2*IConstants.LightSpeed * beta);
	    double phase = (state.getTime() + dt) * 2. * Math.PI * freq;
	    phase = (phase % (2. * Math.PI)) * rad2deg;
	    return phase;
    }
    
    private double getPhaseAbs(double time, BPM bpm) {
	    double freq = bpm.getBPMBucket().getFrequency() * 1.e6;
	    double phase = (time) * 2. * Math.PI * freq;
	    phase *= rad2deg;
	    return phase;
    }
    
    private double getPhase(double time, BPM bpm) {
	    double freq = bpm.getBPMBucket().getFrequency() * 1.e6;
	    double phase = (time) * 2. * Math.PI * freq;
	    phase = (phase % (2. * Math.PI)) * rad2deg;
	    return phase;
    }
    
    private double getTime(ProbeState<?> state, BPM bpm) {
	    
	    // correction time for electrode being offset from the BPM center:
	    double gamma = 1. + state.getKineticEnergy()/state.getSpeciesRestEnergy();
	    double beta = Math.sqrt(1. - 1./(gamma*gamma));
	    double dt = ( bpm.getBPMBucket().getOrientation() * bpm.getBPMBucket().getLength()) / (IConstants.LightSpeed * beta);
	    return (state.getTime() + dt);
    }
    
    /** calculate the BPM phases when the cavity is off
     */
    private void cavOffCalc() {
        
	    // some model stuff
	    Trajectory<?> traj;
	    //EnvelopeProbeState state0, state1, state2;
	    ProbeState<?> state0, state1, state2;
	    java.util.List<? extends ProbeState<?>> states;
	    double time0;
	    
	    theDoc.theCavity.updateDesignAmp(0.); // turn off the cavity
	    theDoc.theCavity.updateDesignPhase(0.);// cavity phase setting does not matter
	    
	    try{
		    theModel.resync();
	    }
	    catch (Exception exc) {
		    System.out.println("Trouble resyncing probe with cavity off " + "\n" + exc.getMessage());
	    }
	    // set the probe to the solver's guess
	    theProbe.reset();
	    theProbe.setKineticEnergy(WIn * 1.e6);
	    // OK let's run the model:
	    try{
		    theModel.run();
		    traj = theModel.getProbe().getTrajectory();
            
		    ArrayList<RfGap> al = new ArrayList<RfGap>(theDoc.theCavity.getGaps());
		    RfGap gap0 = al.get(0);
		    states = traj.statesForElement(gap0.getId());
		    //state0 = (EnvelopeProbeState) states[0];
		    state0 = states.get(0);
		    //System.out.println("time0 = " + state0.getElementId() + " " + state0.getTime());
		    time0 = state0.getTime();
		    
		    states = traj.statesForElement(theDoc.BPM1.getId());
		    //state1 = (EnvelopeProbeState) states[0];
		    state1 = states.get(0);
		    
		    states = traj.statesForElement(theDoc.BPM2.getId());
		    //state2 = (EnvelopeProbeState) states[0];
		    state2 = states.get(0);
            
		    BPM1TimeCavOff = getTime(state1, theDoc.BPM1) - time0;
		    BPM2TimeCavOff = getTime(state2, theDoc.BPM2) - time0;
	    }
	    catch(Exception exc) {
		    System.out.println("Model evaluation failed at cavity off calc" + exc.getMessage());
	    }
	    //System.out.println("off calcs: " + BPM1TimeCavOff +  "\t" + BPM2TimeCavOff + "\t" + WIn);
    }
    
    /* run the model for a given phae, amplitude and beam input energy */
    
    private void runModel(double amp, double phase, double energy) {
        theDoc.theCavity.updateDesignAmp(amp);
        theDoc.theCavity.updateDesignPhase(phase);
        try{
            theModel.resync();
        }
        catch (Exception exc) {
            System.out.println("Trouble resyncing probe with phase = " + phase + "\n" + exc.getMessage());
        }
        // set the probe to the solver's guess
        theProbe.reset();
        theProbe.setKineticEnergy(energy);
        // OK let's run the model:
        try{
            //System.out.println("Phase = " + cavPhaseScaled + " bpmAmp = " + bpmAmp);
            theModel.run();
        }
        catch (Exception exc) {
            System.out.println("Troubler unning modelwith phase = " + phase +   " amp =  " + amp + " energy = " + energy + "\n" + exc.getMessage());
        }
    }
    
    /** do a single model pass - scan through the phases for the prescibed
     * amplitude factor. The amp factor is specified though the argument i
     * to this method. This method should not be called directly. It is meant
     * to be called by the doCalc method which loops though all amp factors.
     */
    private void singlePass(int i) {
	    
	    // calculate phasesCavModelScaledV and phaseDiffsBPMModelV here
	    
        // CKA - The values of betaZ1, betaZ2, sigmaz1, sigmaz2 are never used
        
	    boolean firstPass = true;
	    double time0, time1, time2, deltaPhi, deltaPhi0, diff0;
	    double cavPhase, cavPhaseScaled, betaZ1, betaZ2, phase1, phase2, diff;
	    double sigmaz1, sigmaz2, WOut;
	    // range of phases that have BPM amplitude readings above minimum for parsing:
	    double cavPhaseScaledMin, cavPhaseScaledMax;
	    Vector<Double> phaseCavMeasured = phasesCavMeasured.get(new Integer(i));
	    cavPhaseScaledMin = (phaseCavMeasured.get(0)).doubleValue();
	    cavPhaseScaledMax = (phaseCavMeasured.get(phaseCavMeasured.size()-1)).doubleValue();
        
	    // some model stuff
	    Trajectory<? extends ProbeState<?>> traj;
	    //EnvelopeProbeState state0, state1, state2;
	    ProbeState<? extends ProbeState<?>> state0, state1, state2;
	    java.util.List<? extends ProbeState<?>> states;
	    Twiss[] twiss1, twiss2;
	    
	    double ampModel = (ampValueV.get(i)).doubleValue();
	    CubicSpline splineFitBPMAmp = splineFitsBPMAmp.get(new Integer(i));
	    CubicSpline splineFitPhaseDiff = splineFitsBPMDiff.get(new Integer(i));
	    
	    theDoc.theCavity.updateDesignAmp(ampModel);
	    
	    double[] phases = toDouble(calcPointsV);
	    
	    double measuredDiff0 = splineFitPhaseDiff. evaluateAt(phases[0]);
	    Vector<Double> phaseCavModel = new Vector<Double>();
	    Vector<Double> phaseDiffBPMModel = new Vector<Double>();
	    Vector<Double> WOutV = new Vector<Double>();
	    deltaPhi0 = 0.;
	    diff0= 0.;
        
	    for (int j = 0; j < phases.length; j++) {
		    
		    //jdg 10/11/04: use scaled phase to work with - much easier for user
		    //cavPhase = phases[j];
		    //cavPhaseScaled = cavPhase + cavPhaseOffset;
		    cavPhaseScaled = phases[j];
		    cavPhase = cavPhaseScaled - cavPhaseOffset;
		    
		    double bpmAmp = splineFitBPMAmp.evaluateAt(cavPhaseScaled);
		    // is the measurement worth comparing the model to?:
		    // spline fit is only valid for
		    // cavPhaseScaledMin  < phaseScaled < cavPhaseScaledMax
		    //if(bpmAmp > minBPMAmp) {
		    if(cavPhaseScaled > cavPhaseScaledMin && cavPhaseScaled < cavPhaseScaledMax && bpmAmp > minBPMAmp) {
                try {
                    runModel(ampModel, cavPhase, WIn*1.e6);
                    
				    traj = theModel.getProbe().getTrajectory();
				    
				    ArrayList<RfGap> al = new ArrayList<RfGap>(theDoc.theCavity.getGaps());
				    RfGap gap0 = al.get(0);
				    states = traj.statesForElement(gap0.getId());
				    //state0 = (EnvelopeProbeState) states[0];
				    state0 = states.get(0);
				    //System.out.println("time0 = " + state0.getElementId() + " " + state0.getTime());
				    time0 = state0.getTime();
				    
				    states =traj.statesForElement(firstBPM.getId()); //traj.statesForElement(theDoc.BPM1.getId());
				    //state1 = (EnvelopeProbeState) states[0];
				    state1 = states.get(0);
				    //twiss1 = state1.phaseCorrelation().twissParameters();
				    //betaZ1 = twiss1[2].getBeta();
				    
				    states = traj.statesForElement(secondBPM.getId()); //traj.statesForElement(theDoc.BPM2.getId());
				    //state2 = (EnvelopeProbeState) states[0];
				    state2 = states.get(0);
				    //twiss2 = state2.phaseCorrelation().twissParameters();
				    //betaZ2 = twiss2[2].getBeta();
				    WOut = state1.getKineticEnergy()/1.e6;
                    
				    //sigmaz1 = twiss1[2].getEnvelopeRadius();
				    //sigmaz2 = twiss2[2].getEnvelopeRadius();
				    //System.out.println(betaZ1 + "\t" + betaZ2 + "\t" + sigmaz1 + "\t" + sigmaz2);
				    
				    //check that the beam is not blown up too much longitudinally
				    //if( Math.abs(betaZ1) < 1.e5 && Math.abs(betaZ2) < 1.e5) {
                    if(true) {
					    //System.out.println("phase = " + cavPhase + " betaz = " + betaZ + " bpmamp = " + bpmAmp);
					    time1 = getTime(state1, firstBPM) - time0;
					    phase1 = getPhase(time1, firstBPM);
					    time2 = getTime(state2, secondBPM) - time0;
					    phase2 = getPhase(time2, secondBPM);
					    if (useOneBPM)  {
						    if(theDoc.myWindow().useBPM1Box.isSelected()) {
							    time1 = BPM1TimeCavOff;
						    }
						    else {
							    time1 = BPM2TimeCavOff;
						    }
						    phase1 = getPhase(time1, firstBPM);
						    phase2 = getPhase(time2, secondBPM);
						    diff = phase2 - phase1;
					    }
					    else  {
						    diff = phase2 - phase1;
						    if(useCavOffData) {
							    double p20 = getPhase(BPM2TimeCavOff, secondBPM);
							    double p10 = getPhase(BPM1TimeCavOff, firstBPM);
							    diff -= (p20 - p10);
							    //System.out.println("Off phases = " + p10 + "\t" + p20);
							    //System.out.println("Off times = " + BPM1TimeCavOff + "\t" + BPM2TimeCavOff);
						    }
					    }
					    diff += theDoc.BPMPhaseDiffOffset + theDoc.fudgePhaseOffset;  if(theDoc.myWindow().useWrappingButton.isSelected() ){
                            if (firstPass) {
                                firstPass = false;
                                deltaPhi0 = getPhaseAbs(time2, secondBPM) - getPhaseAbs(time1, firstBPM);
                                // stick the starting calculated diff in the closest 2pi period to the measured diff
                                diff = TrigStuff.unwrap(diff, measuredDiff0);
                                /*
                                 if(diff < BPMPhaseMin)
                                 diff += 360.;
                                 if(diff > (BPMPhaseMin + 360.))
                                 diff -= 360.;
                                 */
                                diff0 = diff;
                            }
                            else {
                                deltaPhi = getPhaseAbs(time2,secondBPM)  - getPhaseAbs(time1, firstBPM);
                                diff = diff0 + deltaPhi - deltaPhi0;
                            }
					    }
					    else {
						    if(diff < BPMPhaseMin)
							    diff += 360.;
						    if(diff > (BPMPhaseMin + 360.))
							    diff -= 360.;
					    }
                        
					    phaseCavModel.add(new Double(cavPhaseScaled));
					    phaseDiffBPMModel.add( new Double(diff));
					    WOutV.add(new Double(WOut));
					    //System.out.println(phase1 + "\t" + phase2 + "\t" + diff);
				    }
                    
			    }
			    catch(Exception exc) {
				    System.out.println("Model evaluation failed at Phase = " + cavPhase + " amp  = " + ampModel + "  " + exc.getMessage());
			    }
		    }
	    }
	    phasesCavModelScaledV.put(new Integer (i), phaseCavModel);
	    phaseDiffsBPMModelV.put(new Integer (i), phaseDiffBPMModel);
	    WOutsV.put(new Integer (i), WOutV);
    }
    
    /** loop through all amplitude setings and scan the phase for each with the model
     * Also calculate the total error between the model and measurements */
    
    // make another method that the solver calls to set WIn, cavPhaseOffset + ampValueV values first, and then calls this
    
    protected double doCalc() {
	    
	    // run the model through the cav phase scan for all amplitude factors:
	    phasesCavModelScaledV.clear();
	    phaseDiffsBPMModelV.clear();
	    WOutsV.clear();
	    errorTotal = 0.;
	    if(useOneBPM) {
		    //cavOffCalc(); // if using only 1 BPM - do the cavity off calc
		    if (theDoc.myWindow().useBPM1Box.isSelected() ) {
                firstBPM = theDoc.BPM1;
                secondBPM = theDoc.BPM1;
		    }
		    else {
                firstBPM = theDoc.BPM2;
                secondBPM = theDoc.BPM2;
		    }
	    }
	    else {
		    firstBPM = theDoc.BPM1;
		    secondBPM = theDoc.BPM2;
	    }
	    
	    if(!modelReady) {
		    if (!calcInit()) return 0.;
	    }
	    
	    if(useCavOffData) cavOffCalc();
	    
	    for (int i=0; i< ampValueV.size(); i++) {
		    if( (theDoc.useScanInMatch.get(i)).booleanValue() ) {
			    singlePass(i);	// do the scan at this amplitude
			    // stash results:
			    Vector<Double> phaseDiffBPMModelV = phaseDiffsBPMModelV.get(new Integer(i) );
			    Vector<Double> phaseCavModelScaledV = phasesCavModelScaledV.get(new Integer(i));
			    CubicSpline splineFit = splineFitsBPMDiff.get(new Integer(i));
			    double error = 0.;
			    
			    //calculate error for this amplitude factor
			    for( int j=0; j< phaseCavModelScaledV.size(); j++) {
                    double phase = (phaseCavModelScaledV.get(j)).doubleValue();
                    double diff = (phaseDiffBPMModelV.get(j)).doubleValue() - splineFit.evaluateAt(phase);
                    error += Math.pow(diff, 2.);
			    }
			    error /= (double) phaseCavModelScaledV.size();
			    errorTotal += error;
		    }
	    }
	    
	    System.out.println(errorTotal);
        haveData = true; theDoc.myWindow().errorField.setText(theDoc.myWindow().prettyString(errorTotal));
	    return errorTotal;
	    
    }
    
    /** start the solver
     * the error from the solver is returned
     * i.e. sqrt(sum residuals)*/
    
    protected  void solve() {
	    variableList.clear();
	    
	    if( ((Boolean) variableActiveFlags.get(1)).booleanValue()) {
		    Variable varWIn = new Variable(WIN_VAR_NAME, WIn, WIn*0.90, WIn*1.1);
		    variableList.put("WIn",varWIn);
	    }
	    
	    if( ((Boolean) variableActiveFlags.get(0)).booleanValue()) {
		    Variable varCavPhaseOffset = new Variable(PHASE_VAR_NAME, cavPhaseOffset, cavPhaseOffset-30.,cavPhaseOffset+30.);
		    variableList.put("PhaseOffset", varCavPhaseOffset);
	    }
	    
	    if( ((Boolean) variableActiveFlags.get(2)).booleanValue()) {
		    double val = cavityVoltage;
		    //String name = "Amp fac " +(new Integer(i+1)).toString();
		    Variable pp = new Variable(AMP_VAR_NAME, val, val*0.7, val*1.3);
		    variableList.put("AmpFac", pp);
	    }
	    
	    if( ((Boolean) variableActiveFlags.get(3)).booleanValue()) {
            Variable varFF = new Variable(FF_VAR_NAME, theDoc.fudgePhaseOffset, -180, 180);
            variableList.put("Fudge", varFF);
	    }
        

		final Problem problem = new Problem();
		final ErrorObjective objective = new ErrorObjective( "Pasta Error", 0.001 );
		problem.addObjective( objective );
		problem.setVariables( new ArrayList<Variable>( variableList.values() ) );
		problem.setEvaluator( new PastaEvaluator( theScorer, objective, problem.getVariables() ) );

	    solver = new Solver( new SimplexSearchAlgorithm(), SolveStopperFactory.minMaxTimeSatisfactionStopper( 0, timeoutPeriod, SatisfactionCurve.inverseSquareSatisfaction( 0.001, 0.1 ) ) );

        solverReady = true;
        
	    solver.solve( problem );
	    System.out.println("Done solving");
	    showSolution();
    }
    
    /** display the variable values on the main window */
    protected void showSolution() {
	    Variable pp;
	    ScoreBoard sb = solver.getScoreBoard();
	    System.out.println(sb.toString());
	    //errorTotal = sb.getBestScore();
	    Map<Variable, Number> solutionMap = sb.getBestSolution().getTrialPoint().getValueMap();
	    if(variableList.get("WIn") != null) {
		    pp = variableList.get("WIn");
		    WIn = ((Double) solutionMap.get(pp)).doubleValue();
	    }
	    if(variableList.get("PhaseOffset") != null)  {
		    pp = variableList.get("PhaseOffset");
		    cavPhaseOffset = ((Double) solutionMap.get(pp)).doubleValue();
	    }
	    if(variableList.get("AmpFac") != null) {
		    pp = variableList.get("AmpFac");
		    Double val = (Double) solutionMap.get(pp);
		    cavityVoltage = val.doubleValue();
		    updateAmpFactors();
	    }
        
        // run model through final point again so plot shows final solution.
        doCalc();
        analysisTableModel.fireTableDataChanged(); theDoc.myWindow().errorField.setText(theDoc.myWindow().prettyString(errorTotal));
        plotUpdate();
    }
    
    /** Model initialization and checks */
    
    private boolean calcInit() {
	    Collection<RfCavity> cavs2;
	    
	    // any data to compare to ?
	    
	    if(!analysisDataReady) {
		    Toolkit.getDefaultToolkit().beep();
		    String errText = "Whoa there - please import measured data before running model";
		    theDoc.myWindow().errorText.setText(errText);
		    System.err.println(errText);
		    return  false;
	    }
	    
	    // probe set yet ?
	    probeInit();
        
	    // model latice constructed yet ?
	    
	    if(theModel == null) {
		    Toolkit.getDefaultToolkit().beep();
		    String errText = "Whoa there - the model is not defined - is a sequence even selected?";
		    theDoc.myWindow().errorText.setText(errText);
		    System.err.println(errText);
		    return false;
	    }
	    // turn off all downstream cavities in the model
	    
		if ((theDoc.theSequence.getClass()).equals(AcceleratorSeqCombo.class))
		{
			OrTypeQualifier kq = (new OrTypeQualifier()).or("rfcavity").or(SCLCavity.s_strType);
			cavs2 = (theDoc.theSequence).getAllInclusiveNodesWithQualifier(kq);
			Iterator<RfCavity> itr = cavs2.iterator();
			while (itr.hasNext()) {
				AcceleratorSeq seq = (AcceleratorSeq) itr.next();	if(!seq.getId().equals(theDoc.theCavity.getId()) ) {
					((RfCavity) seq) .updateDesignAmp(0.);
					System.out.println("deactivating cav " + seq.getId());
				}
			}
		}
        //make sure  that RF phase is calculated
        theProbe.getAlgorithm().setRfGapPhaseCalculation(true);
        // turn charge off - does not affect longitudinal dynamics:
        //((BeamProbe)theProbe).setBeamCharge(0.);
        //((BeamProbe)theProbe).setBeamCurrent(0.);
        theModel.setProbe(theProbe);
        theModel.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        // run only through part of sequence of interest:
        theProbe.getAlgorithm().setStopElementId(theDoc.theCavity.getId());
        theProbe.getAlgorithm().setStopElementId(secondBPM.getId());
        
        modelReady = true;
        return true;
    }
    
    protected boolean probeInit() {
	    if(theProbe == null) {
		    // try reading in the probe file first if there is one
		    if(probeFileName != null) {
                try{
                    probeFile = new File(probeFileName);
                    if (!setProbe(probeFile)) return false;
                }
                catch (Exception exc) {
                    String errText = "Damn!, I can't read the probe file you requested: " + probeFileName;
                    Toolkit.getDefaultToolkit().beep();
                    theDoc.myWindow().errorText.setText(errText);
                    System.err.println(errText);
                    return false;
                }
		    }
		    else{
			    // force the default probe for this sequence:
			    /*
                 Toolkit.getDefaultToolkit().beep();
                 String errText = "Whoa there - please pick a probe file before running the model";
                 theDoc.myWindow().errorText.setText(errText);
                 System.err.println(errText);
                 return false;
                 */
                ParticleTracker pt = null;
                
                try {
                    
                    pt= AlgorithmFactory.createParticleTracker(theDoc.theSequence);
                    
                } catch (InstantiationException e) {
                    System.err.println( "Instantiation exception creating tracker." );
                    e.printStackTrace();
                }
                
			    pt.setRfGapPhaseCalculation(true);
			    System.out.println("cav = " + theDoc.theCavity.getId());
                try {
                    if(theDoc.theSequence.getId().equals("MEBT")) {
                        theProbe = ProbeFactory.createParticleProbe(theDoc.theSequence, AlgorithmFactory.createParticleTracker(theDoc.theSequence));
                    }
                    else {
                        theProbe = ProbeFactory.getParticleProbe(theDoc.theCavity.getId(), theDoc.theSequence, AlgorithmFactory.createParticleTracker(theDoc.theSequence));
                    }
                } catch (InstantiationException e) {
                    System.err.println( "Instantiation exception creating probe." );
                    e.printStackTrace();
                }
		    }
	    }
	    defaultEnergy = theProbe.getKineticEnergy()/1.e6;
	    return true;
    }
    
    /** make the CAV Phase points to evaluate the model at for the scan */
    
    protected void makeCalcPoints() {
        
	    calcPointsV.clear();
	    double delta = (phaseModelMax - phaseModelMin)/((double)(nCalcPoints-1));
	    double pos;
	    for (int i=0; i< nCalcPoints; i++) {
		    pos = phaseModelMin + ((double) i) * delta;
		    calcPointsV.add(new Double(pos));
	    }
	    
    }
    
    /** method to refresf table */
    protected void refreshTable() {
	    analysisTableModel.fireTableDataChanged();
	    System.out.println("Table refresh");
    }
    
    /** sets the probe from a filename */
    protected boolean setProbe(File file) {
	    XmlDataAdaptor probeXmlDataAdaptor;
	    try {
		    probeXmlDataAdaptor =XmlDataAdaptor.adaptorForFile(file, false);
		    theProbe = ProbeXmlParser.parseDataAdaptor(probeXmlDataAdaptor);
	    }
	    catch(Exception ex) {
		    Toolkit.getDefaultToolkit().beep();
		    String errText = "Darn, I can't pasre the file :" + file.getPath();
		    if(theDoc.myWindow() != null)
			    theDoc.myWindow().errorText.setText(errText);
		    System.err.println(errText);
		    return false;
	    }
        System.out.println("Probe " + probeFile.getPath() + " parsed OK ");
        // to speed up the calculation turn off space charge. This does not affect
        // motion of sync. particle
        //((BeamProbe) theProbe).setBeamCharge(0.);
        modelReady = false;
        return true;
    }
    
    /** method to estimate the phase and amplitude settings from present state of analysis */
    
    protected void updateSetpoints() {
	    double  WOutModel0, WOutModel1;    // CKA - these variables are never used
	    
	    if ( theDoc.BPM1 == null ||
            paramMeasuredVals.size() < 1 || ampValueV.size() < 1) {
		    Toolkit.getDefaultToolkit().beep();
		    String errText = "Whoa buddy - I don't see any scan data yet";
		    if(theDoc.myWindow() != null)
			    theDoc.myWindow().errorText.setText(errText);
		    System.err.println(errText);
		    return;
	    }
	    
	    double [] pvs = toDouble(paramMeasuredVals);
	    double [] svs = toDouble(ampValueV);
	    
	    cavPhaseSetpoint = theDoc.theDesignPhase + cavPhaseOffset - theDoc.DTLPhaseOffset;
	    if(svs.length < 2) {
		    // only one parametric point - no interpolation
		    cavAmpSetpoint = pvs[0] * theDoc.theDesignAmp/svs[0];
		    BasicGraphData WOutModel = WOutModelMap.get(new Integer(0));
		    WOutCalc = WOutModel.getValueY(cavPhaseSetpoint);
		    return;
	    }
	    
	    // find the index close to where we ant to set the amplitude
	    int ind = 0;
	    while(theDoc.theDesignAmp <= svs[ind] && ind < svs.length-1) {
		    ind++;
	    }
	    if(ind > (pvs.length -2)) ind = pvs.length -2;
	    double slope = (pvs[ind+1] - pvs[ind])/(svs[ind+1]-svs[ind]);
	    cavAmpSetpoint = pvs[ind] + (theDoc.theDesignAmp - svs[ind])*slope;
        
        runModel(cavityVoltage, theDoc.theDesignPhase, WIn*1.e6);
        Trajectory<?> traj = theModel.getProbe().getTrajectory();
        java.util.List<? extends ProbeState<?>> states =traj.statesForElement(firstBPM.getId());
        ProbeState<?> state = states.get(0);
        WOutCalc = state.getKineticEnergy()/1.e6;
        /*
         
         if(!((Boolean) theDoc.useScanInMatch.get(ind)).booleanValue()) {
         WOutCalc = ((BasicGraphData) WOutModelMap.get(new Integer(ind+1))).getValueY(cavPhaseSetpoint);
         return;
         }
         if(!((Boolean) theDoc.useScanInMatch.get(ind+1)).booleanValue()) {
         WOutCalc = ((BasicGraphData) WOutModelMap.get(new Integer(ind))).getValueY(cavPhaseSetpoint);
         return;
         }
         
         WOutModel0 = ((BasicGraphData) WOutModelMap.get(new Integer(ind))).getValueY(cavPhaseSetpoint + theDoc.DTLPhaseOffset);
         WOutModel1 = ((BasicGraphData) WOutModelMap.get(new Integer(ind+1))).getValueY(cavPhaseSetpoint + theDoc.DTLPhaseOffset);
         double slope2 = (WOutModel1 - WOutModel0)/(svs[ind+1]-svs[ind]);
         WOutCalc = WOutModel0 + (theDoc.theDesignAmp - svs[ind])*slope2;
         */
        
    }
    /** send the new setpoints to the control system */
    protected void  sendNewSetpoints() {
	    if(cavPhaseSetpoint == 0. || cavAmpSetpoint < 0.05) {
            Toolkit.getDefaultToolkit().beep();
            String errText = "Calculate setpoints first!";
            theDoc.myWindow().errorText.setText(errText);
            System.err.println(errText);
            return;
	    }
	    try {
		    theDoc.theCavity.setCavPhase(cavPhaseSetpoint);
		    theDoc.theCavity.setCavAmp(cavAmpSetpoint);
	    } catch (Exception ex) {
            Toolkit.getDefaultToolkit().beep();
            String errText = "Error trying to send new setpoints to EPICS";
            theDoc.myWindow().errorText.setText(errText);
            System.err.println(errText);
	    }
    }
    
    /** helper method to dump contents of a Vector to a primitive double array */
    
    protected double[] toDouble(Vector<Double> vec) {
        double [] da = new double [vec.size()];
        Object za [] =  vec.toArray();
        for (int i=0; i< za.length; i++) da[i] = ((Double) za[i]).doubleValue();
        
        return da;
    }
    
    /** find the first occurance of a Vector of Doubles with a value > an input
     * return the index of this object
     * if none are > that the inpuit, return 0
     */
    private int findInsertPoint(Vector<Double> vec, double value) {
	    for (int i= 0; i< vec.size(); i++) {
		    double valTest = (vec.get(i)).doubleValue();
		    if(valTest > value) return i;
	    }
	    return -1;
    }


	/** method to come up with an initial guess for cavity offset, amplitude, and input beam energy */
	protected void initialGuess() {
		double minPhase, maxPhase, avgPhase, dPhase;
		BasicGraphData someMeasuredData = null;
		// start with beam energy - set it to design:
		WIn = defaultEnergy;
		// same with amplitude:
		cavityVoltage = theDoc.theDesignAmp;
		//assume the cavity phase setpoint is in the center of the scan range:
		if(theDoc.myWindow().useBPM1Box.isSelected())
			someMeasuredData = theDoc.scanStuff.BPM1AmpMV.getDataContainer(0);
		if(theDoc.myWindow().useBPM2Box.isSelected())
			someMeasuredData = theDoc.scanStuff.BPM2AmpMV.getDataContainer(0);
		if(someMeasuredData == null) {
			String errText = "Hey - give me some measured data first!!!: ";
			Toolkit.getDefaultToolkit().beep();
			theDoc.myWindow().errorText.setText(errText);
			System.err.println(errText);
			return;
		}
		Vector<Double> phaseCavMeasured = phasesCavMeasured.get(new Integer(0));
		minPhase = (phaseCavMeasured.get(0)).doubleValue();
		maxPhase = (phaseCavMeasured.get(phaseCavMeasured.size()-1)).doubleValue();
		//minPhase = someMeasuredData.getX(0);
		//int np = someMeasuredData.getNumbOfPoints();
		//maxPhase = someMeasuredData.getX(np-1);
		avgPhase = (minPhase + maxPhase)/2.;
		dPhase = maxPhase - minPhase;
		System.out.println("Phases " + minPhase + "  " + maxPhase);

		cavPhaseOffset = avgPhase - theDoc.theDesignPhase  + theDoc.DTLPhaseOffset;

		// give some margin away from the scan endpoints for analysis range:
		theDoc.analysisStuff.phaseModelMax = maxPhase - 0.1 * dPhase;
		theDoc.myWindow().maxScanPhaseField.setValue(phaseModelMax);
		theDoc.analysisStuff.phaseModelMin = minPhase + 0.1 * dPhase;
		theDoc.myWindow().minScanPhaseField.setValue(phaseModelMin);
		theDoc.analysisStuff.makeCalcPoints();
		analysisTableModel.fireTableDataChanged();

	}



    /** class to do solver function evaluation */
	private class PastaScorer  implements Scorer {
		public PastaScorer() {}

		public double  score( final Trial trial, final java.util.List<Variable> variables ) {
			final TrialPoint trialPoint = trial.getTrialPoint();

			// set the quantities used in function evaluation
			// to the solver guesses:
			if(variableList.get("WIn") != null)
				WIn = trialPoint.getValue( variableList.get("WIn") );

			if(variableList.get("PhaseOffset") != null)
				cavPhaseOffset = trialPoint.getValue( variableList.get("PhaseOffset") );
			
			if(variableList.get("AmpFac") != null) {
				Variable pp =  variableList.get("AmpFac");
				cavityVoltage = trialPoint.getValue( pp );
				updateAmpFactors();
			}
			
			if(variableList.get("Fudge") != null)  {
				theDoc.fudgePhaseOffset = trialPoint.getValue( variableList.get("Fudge") );
			}
			// calculate error term for these settings:
			System.out.print("scan at " + WIn + "  " + cavPhaseOffset + "  " + cavityVoltage + "  " + theDoc.fudgePhaseOffset + "  ");
			final double score = doCalc();

//			System.out.println( "" );
//			for ( final Variable variable : variables ) {
//				System.out.println( variable.getName() + ": " + trialPoint.getValue( variable ) );
//			}
//			System.out.println( "Score: " + score );

			return score;
		}
		
	}



	//Evaluates beam properties for a trial point
	class PastaEvaluator implements Evaluator {
		private final Objective OBJECTIVE;
		private final List<Variable> VARIABLES;
		private final Scorer SCORER;


		// Constructor
		public PastaEvaluator( final PastaScorer scorer, final Objective objective, final List<Variable> variables ) {
			SCORER = scorer;
			OBJECTIVE = objective;
			VARIABLES = variables;
		}


		// evaluate the trial
		public void evaluate( final Trial trial ){
			final double score = SCORER.score( trial, VARIABLES );
			trial.setScore( OBJECTIVE, score );
		}
	}



	// objective class for solver.
	class ErrorObjective extends Objective{
		// error tolerance
		final private double TOLERANCE;

		// Constructor
		public ErrorObjective( final String name, final double tolerance ){
			super( name );
			TOLERANCE = tolerance;
		}

		// compute the satisfaction for the given (positive) square error
		public double satisfaction( final double squareError ){
			return SatisfactionCurve.inverseSquareSatisfaction( Math.sqrt( squareError ), TOLERANCE );
		}
		
	}

}

