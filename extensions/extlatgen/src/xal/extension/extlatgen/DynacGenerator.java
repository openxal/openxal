/*
 * @(#)DynacGenerator.java	0.2 04/07/2003
 *
 * Copyright (c) 2002-2003 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.extension.extlatgen;

import java.io.*;
import java.util.Date;

import xal.smf.*;
import xal.smf.impl.*;
import xal.ca.*;
import xal.sim.slg.*;   // for lattice generation
import xal.model.ModelException;
import xal.model.probe.*;  // Probe for t3d header
import xal.model.probe.traj.*;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.tools.beam.TraceXalUnitConverter;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.Twiss; //had to import to fix deprecation issue with getTwiss

/**
 * DynacGenerator generates Dynac input file from XAL lattice view.
 * Usage: create a DynacGenerator object with an XAL lattice as input, then call
 * the method createT3dInput() which one can specify either DESIGN or LIVE data
 * as argument.
 *
 * @author  C.M.Chu
 * @version    0.2  07 Apr 2003
 */

public class DynacGenerator {
    
    /** input lattice view */
    protected Lattice myLattice;
    
    /** Probe for initial condition */
    protected EnvelopeProbe myProbe;
    
    protected String myLatticeName = null;
    
    private boolean mebtInd = false;        // TODO: CKA - NEVER USED

    private AcceleratorSeq myAccSeq;
    private Scenario myScenario;
    private String mySrcSelector = Scenario.SYNC_MODE_DESIGN;
    
    /** Constructor
     * @param lattice XAL lattice view
     */
    public DynacGenerator(Lattice lattice, AcceleratorSeq accSeq, EnvelopeProbe envProbe) {
        myLattice = lattice;
        myProbe = envProbe;
        myAccSeq = accSeq;
        try{
            myScenario = Scenario.newScenarioFor(myAccSeq);
        } catch (ModelException e) {
            System.out.println("Cannot create Scenario for " + myAccSeq.getId());
        }
    };
    
    public DynacGenerator(String latticeName, Lattice lattice, AcceleratorSeq accSeq, EnvelopeProbe envProbe) {
        this(lattice, accSeq, envProbe);
        myLatticeName = latticeName;
    };
    
    /** beam initial condition */
    protected double beamci[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    
    /** set the beam initial condition */
    public void setBeamCI(double[] newBeamCI) {
        beamci = newBeamCI;
    }
    
    /**
     * @param srcSelector select the data source: design (Scenario.SYNC_MODE_DESIGN) or live (Scenario.SYNC_MODE_LIVE) data
     */
    public void createDynacInput(String srcSelector) throws IOException {
        
        mySrcSelector = srcSelector;
        
        if (myLatticeName == null)
            myLatticeName = myLattice.getName();
        
        FileWriter dynac_input = new FileWriter(myLatticeName+".in");
        Date today = new Date();
        //       int elementCount = myLattice.len();
        
        TraceXalUnitConverter uc = TraceXalUnitConverter.newConverter(
                                                                      402500000.,
                                                                      myProbe.getSpeciesRestEnergy(),
                                                                      myProbe.getKineticEnergy());
        // for Dynac header
        CovarianceMatrix covarianceMatrix = myProbe.getCovariance();

        Twiss[] twiss = covarianceMatrix.computeTwiss();
        String dynac_header =
        "SNS " + myLatticeName + " " + today.toString() + "\n"
        + "GEBEAM\n"
        + "2 1\n"
        + "402.5E06\n"
        + "10000\n"
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).getx()/10. + " "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).getxp() + " "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).gety()/10. + " "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).getyp() + " "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).getzp()*3600. + " "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).getz()/1000. + "\n"
        + uc.xalToTraceTransverse(twiss[0]).getAlpha() + " "
        + uc.xalToTraceTransverse(twiss[0]).getBeta() + " "
        + uc.xalToTraceTransverse(twiss[0]).getEmittance() + "\n"
        + uc.xalToTraceTransverse(twiss[1]).getAlpha() + " "
        + uc.xalToTraceTransverse(twiss[1]).getBeta() + " "
        + uc.xalToTraceTransverse(twiss[1]).getEmittance() + "\n"
        + uc.xalToTraceLongitudinal(twiss[2]).getAlpha() + " "
        + uc.xalToTraceLongitudinal(twiss[2]).getBeta() + " "
        + uc.xalToTraceLongitudinal(twiss[2]).getEmittance() + "\n"
        + "87647\n"
        + "INPUT\n"
        + "1111111\n"
        //       + (myProbe.getSpeciesRestEnergy()/1.e6) + " 1. " + (myProbe.getSpeciesCharge() /1.602e-19) +"\n"
        + (myProbe.getSpeciesRestEnergy()/1.e6) + " 1. " + (myProbe.getSpeciesCharge()) +"\n"
        + (myProbe.getKineticEnergy()/1.e6) + " 0. \n"
        + "1\n"
        + "EMIPRT\n"
        + "1\n"
        + "SCDYNAC\n"
        + "3\n"
        + myProbe.getBeamCurrent()*1000.
        + " 3.\n"
        + "0\n"
        + "EMITGR\n"
        + "BEAM AT INPUT " + myLatticeName + "\n"
        + "0 5\n"
        + "1. 50. 1. 50. 1. 1.  90. 0.1\n";
        
        
        LatticeIterator ilat=myLattice.latticeIterator();
        int counter = 1;        // TODO: CKA - NEVER USED
        String str = dynac_header;
        //       int devTypeInd = 1;
        String devStr = "";
        
        String prevElementType = "";
        double quadLength = 0.;
        
        char buffer_header[] = new char[str.length()];
        str.getChars(0, str.length(), buffer_header, 0);
        dynac_input.write(buffer_header);
        
        // run online model here so we don't propagate the probe before we set all the initial conditions
        runOnlineModel();
        
        // DTL indicator
        boolean DTLInd = false;
        
        while(ilat.hasNext()) {
            Element element = ilat.next();
            // for diagnostic devices
            if (element.getType().equals("beampositionmonitor") ||
                element.getType().equals("beamcurrentmonitor") ||
                element.getType().equals("beamlossmonitor") ||
                element.getType().equals("wirescanner") ||
                element.getType().equals("pmarker") ||
                element.getType().equals("foil")) {
                devStr = "DRIFT\n"
                + "  " + element.getLength()*100. + "\n";
            }
            // drift space
            else if (element.getType().equals("drift")) {
                // for regular drift space except for DTL's (do nothing for DTL drifts)
                if (!DTLInd){
                    // if the drift is too long (>10cm), break it to more pieces
                    devStr = "";
                    long driftPieces = 0;
                    double stepSize = 10.;
                    if (element.getLength()*100.> stepSize) {
                        driftPieces = Math.round(element.getLength()*100./stepSize);
                        if (driftPieces*stepSize > element.getLength()*100.)
                            driftPieces = driftPieces - 1;
                        for (int i=0; i<driftPieces; i++) {
                            devStr = devStr
                            + "DRIFT\n"
                            + "  " + stepSize + "\n";
                        }
                    }
                    devStr = devStr
                    + "DRIFT\n"
                    + "  " + (element.getLength()*100.-driftPieces*stepSize) + "\n";
                } else {
                    // do nothing for DTL drift space
                    devStr = "";
                }
                //           prevElementType = "";
            }
            // for quads
            else if (element.getType().equals("quadrupole") ) {
                // if previous element is a rfgap, go backward half of the magnet length for correct rf gap calculation
                if (element.getAcceleratorNode().getId().substring(0,4).equals("MEBT"))
                    mebtInd = true;
                if (prevElementType.equals("rfgap") && DTLInd) {
                    devStr = "DRIFT\n"
                    + "  " + -1.*element.getLength()*100. + "\n";
                }
                else
                    devStr = "";
                
                // magnet aperture
                double aper = element.getAcceleratorNode().getAper().getAperX();
                
                // for PM quads, i.e. always use design field
                if (element.getAcceleratorNode().getType().equals("PMQH") ||
                    element.getAcceleratorNode().getType().equals("PMQV") ) {
                    devStr = devStr + "QUADRUPO\n"
                    + "  " + element.getLength()*100. + " "
                    + ((xal.smf.impl.PermQuadrupole) element.getAcceleratorNode()).getDfltField()*aper*10.
                    + " " + aper*100. + "\n";
                } else {
                    if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN))
                        devStr = devStr + "QUADRUPO\n"
                        + "  " + element.getLength()*100. + " "
                        + ((xal.smf.impl.Quadrupole) element.getAcceleratorNode()).getDfltField()*aper*10.
                        + " " + aper*100. + "\n";
                    else if (srcSelector.equals(Scenario.SYNC_MODE_LIVE)||
                             srcSelector.equals(Scenario.SYNC_MODE_RF_DESIGN)) {
                        try{
                            devStr = devStr + "QUADRUPO\n"
                            + "  " + element.getLength()*100. + " "
                            + ((xal.smf.impl.Quadrupole) element.getAcceleratorNode()).getField()*aper*10.
                            + " " + aper*100. +"\n";
                        }
                        catch(ConnectionException e){
                            devStr = devStr + "QUADRUPO\n"
                            + "  " + element.getLength()*100. + " "
                            + "0. 1.\n";
                        }
                        catch(GetException e){}
                    }
                }
                
                prevElementType = "quadrupole";
                quadLength = element.getLength()*100.;
                if (element.getAcceleratorNode().getId().substring(0,4).equals("MEBT"))
                    mebtInd = true;
                if (element.getAcceleratorNode().getId().substring(0,3).equals("DTL"))
                    DTLInd = true;
            }
            // for horizontal dipole correctors
            else if(element.getType().equals("hsteerer")){
                if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN))
                    devStr = "STEER\n"
                    + "  " + ((xal.smf.impl.HDipoleCorr) element.getAcceleratorNode()).getDfltField()
                    + " 0\n";
                else if (srcSelector.equals(Scenario.SYNC_MODE_LIVE)||
                         srcSelector.equals(Scenario.SYNC_MODE_RF_DESIGN)) {
                    try{
                        devStr = "STEER\n"
                        + "  " + ((xal.smf.impl.HDipoleCorr) element.getAcceleratorNode()).getField()*
                        ((xal.smf.impl.HDipoleCorr) element.getAcceleratorNode()).getLength()
                        + " 0\n";
                    }
                    catch(ConnectionException e){
                        devStr = "STEER\n"
                        + "  0. 0\n";
                    }
                    catch(GetException e){}
                }
                
                prevElementType = "hsteerer";
            }
            // for vertical dipole correctors
            else if(element.getType().equals("vsteerer")) {
                if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN))
                    devStr = "STEER\n"
                    + "  " + ((xal.smf.impl.VDipoleCorr) element.getAcceleratorNode()).getDfltField()
                    + " 1\n";
                else if (srcSelector.equals(Scenario.SYNC_MODE_LIVE)||
                         srcSelector.equals(Scenario.SYNC_MODE_RF_DESIGN)) {
                    try{
                        devStr = "STEER\n"
                        + "  " + ((xal.smf.impl.VDipoleCorr) element.getAcceleratorNode()).getField()*
                        ((xal.smf.impl.VDipoleCorr) element.getAcceleratorNode()).getLength()
                        + " 1\n";
                    }
                    catch(ConnectionException e) {
                        devStr = "STEER\n"
                        + "  0. 1\n";
                    }
                    catch(GetException e){}
                }
                
                prevElementType = "vsteerer";
            }
            // for rf gaps
            else if(element.getType().equals("rfgap")) {
                // get TTF etc.
                ProbeState<?> state = myScenario.getTrajectory().statesForElement(element.getAcceleratorNode().getId()).get(0);
                double gamma = 1. + state.getKineticEnergy()/state.getSpeciesRestEnergy();
                double beta = Math.sqrt(1.-1./(gamma*gamma));
                double TTF = ((RfGap) element.getAcceleratorNode()).getTTFFit().evaluateAt(beta);
                double TTF_Prime = ((RfGap) element.getAcceleratorNode()).getTTFPrimeFit().evaluateAt(beta);
                
                // if the previous element is a quad, move backward half magnet length to get correct rf gap calculation
                if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN)||
                    srcSelector.equals(Scenario.SYNC_MODE_RF_DESIGN))
                    if (element.getAcceleratorNode().getParent().getType().equals("Bnch"))
                        devStr = "BUNCHER\n"
                        + "  " + -1.*((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapDfltE0TL()
                        + " " + ((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapDfltPhase()
                        + " 1 1.5 "
                        + "\n";
                    else {
                        int indOfGapCount = element.getAcceleratorNode().getId().indexOf("Rg");
                        String gapCount = element.getAcceleratorNode().getId().substring(indOfGapCount+2);
                        devStr = "";
                        
                        if (prevElementType.equals("quadrupole") && DTLInd) {
                            devStr = "DRIFT\n"
                            + "  " + -1.*quadLength + "\n";
                        }
                        
                        devStr = devStr + "CAVSC\n"
                        + Integer.parseInt(gapCount) + "  0.  0.  "
                        + ((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapLength()*100. + " "
                        + TTF + " "
                        + TTF_Prime
                        + "  0.  0.  0.  0.  "
                        + " " + ((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapDfltAmp()
                        + " " + ((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapDfltPhase()
                        + " 0.  0.  402.5  1."
                        + "\n";
                    }
                    else if (srcSelector.equals(Scenario.SYNC_MODE_LIVE)) {
                        try{
                            if (element.getAcceleratorNode().getParent().getType().equals("Bnch"))
                                devStr = "BUNCHER\n"
                                + "  " + ((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapE0TL()
                                + " " + ((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapPhaseAvg()
                                + " 1 1.5 "
                                + "\n";
                            else {
                                devStr = "";
                                if (prevElementType.equals("quadrupole") && DTLInd)
                                    devStr = "DRIFT\n"
                                    + "  " + -1.*quadLength + "\n";
                                devStr = devStr + "CAVSC\n"
                                + "  0  0.  0.  "
                                + ((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapLength()*100. + " " 
                                + TTF + " " 
                                + TTF_Prime 
                                + "  0.  0.  0.  0.  "
                                + ((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapAmpAvg() + " " 
                                + ((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapPhaseAvg()
                                + "  0.  0.  402.5  1."
                                + "\n";
                            }
                        }
                        catch(ConnectionException e){
                            //	       devStr = devTypeInd + ", A(1, " + counter + ")=" 
                            //	              + "0., 0., 1, 1, 1, \n";	       
                        }
                        catch(GetException e){}
                    }
                
                prevElementType="rfgap";
                if (element.getAcceleratorNode().getId().substring(0,4).equals("MEBT"))
                    mebtInd = true;
                if (element.getAcceleratorNode().getId().substring(0,3).equals("DTL"))
                    DTLInd = true;
            }
            
            if (!element.getName().substring(0,4).equals("DRFT"))
                str = ";" + element.getName() + "\n"
                + devStr;
            else 
                str = devStr;
            
            char buffer[] = new char[str.length()];
            str.getChars(0, str.length(), buffer, 0);
            dynac_input.write(buffer);	
            
            counter++;
        }
        str = "STOP";
        
        char buffer_end[] = new char[str.length()];
        str.getChars(0, str.length(), buffer_end, 0);
        dynac_input.write(buffer_end);	
        
        dynac_input.close();   
        
    }
    
    /**
     * run online model to obtain correct TTF and so on
     */
    private void runOnlineModel() {
        myScenario.setProbe(myProbe);
        myScenario.setSynchronizationMode(mySrcSelector);
        try {
            myScenario.resync();
            myScenario.run();
        } catch (SynchronizationException e) {
            System.out.println(e);
        } catch (ModelException e) {
            System.out.println(e);
        }
    }
    
}
