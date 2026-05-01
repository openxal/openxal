/*
 * @(#)T3dGenerator.java	0.2 04/07/2003
 *
 * Copyright (c) 2002-2003 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.extension.extlatgen;

import java.io.*;
import java.util.Date;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import xal.smf.*;
import xal.smf.impl.*;
import xal.ca.*;
import xal.sim.slg.*;   // for lattice generation
import xal.model.probe.*;  // Probe for t3d header
import xal.model.probe.traj.EnvelopeProbeState;
import xal.sim.scenario.Scenario;
import xal.tools.beam.TraceXalUnitConverter;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.Twiss; //had to import to fix deprecation issue with getTwiss

/**
 * T3dGenerator generates Trace 3D input file from XAL lattice view.
 * Usage: create a T3dGenerator object with an XAL lattice as input, then call
 * the method createT3dInput() which one can specify Scenario.SYNC_MODE_DESIGN,
 * Scenario.SYNC_MODE_LIVE or Scenario.SYNC_MODE_RF_DESIGN data as argument.
 *
 * @author  C.M.Chu
 * @version    0.2  07 Apr 2003
 */

public class T3dGenerator {
    
    /** input lattice view */
    protected Lattice myLattice;
    
    /** Probe for initial condition */
    protected EnvelopeProbe myProbe;
    
    protected String myLatticeName = null;
    
    /** Constructor
     * @param lattice XAL lattice view
     */
    public T3dGenerator(Lattice lattice, EnvelopeProbe envProbe) {
        myLattice = lattice;
        myProbe = envProbe;
    };
    
    public T3dGenerator(String latticeName, Lattice lattice, EnvelopeProbe envProbe) {
        myLattice = lattice;
        myLatticeName = latticeName;
        myProbe = envProbe;
    };
    
    /** beam initial condition */
    protected double beamci[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    
    /** set the beam initial condition */
    public void setBeamCI(double[] newBeamCI) {
        beamci = newBeamCI;
    }
    
    /**
     * @param srcSelector select the data source: design (Scenario.SYNC_MODE_DESIGN)
     *        live (Scenario.SYNC_MODE_LIVE) or rf_design (Scenario.SYNC_MODE_RF_DESIGN) data
     */
    public void createT3dInput(String srcSelector) throws IOException {
        if (myLatticeName == null)
            myLatticeName = myLattice.getName();
        
        FileWriter t3d_input = new FileWriter(myLatticeName+".t3d");
        //       FileWriter t3d_input = new FileWriter("combo.t3d");
        Date today = new Date();
        int elementCount = myLattice.len();
        
        TraceXalUnitConverter uc = TraceXalUnitConverter.newConverter(
                                                                      402500000.,
                                                                      myProbe.getSpeciesRestEnergy(),
                                                                      myProbe.getKineticEnergy());
        
        NumberFormat nf = NumberFormat.getNumberInstance();
        ((DecimalFormat) nf).setMaximumFractionDigits(5);
        nf.setGroupingUsed(false);
        
        CovarianceMatrix covarianceMatrix = myProbe.createProbeState().getCovarianceMatrix();
        
        Twiss[] twiss = covarianceMatrix.computeTwiss();
        // for T3d header
        String t3d_header =
        " $DATA\n"
        //       + " ER= " + (myProbe.getSpeciesRestEnergy()/1.e6) + ", Q= " + (myProbe.getSpeciesCharge() /1.602e-19)
        + " ER= " + (myProbe.getSpeciesRestEnergy()/1.e6) + ", Q= " + (myProbe.getSpeciesCharge())
        + ", W =  " +  (myProbe.getKineticEnergy()/1.e6) + ", XI=  " + (myProbe.getBeamCurrent()*1000.) + "\n"
        + " EMITI =     "
        + nf.format(uc.xalToTraceTransverse(twiss[0]).getEmittance()) + ",  "
        + nf.format(uc.xalToTraceTransverse(twiss[1]).getEmittance()) + ",  "
        + nf.format(uc.xalToTraceLongitudinal(twiss[2]).getEmittance()) + ",\n"
        + " BEAMI =     "
        + nf.format(uc.xalToTraceTransverse(twiss[0]).getAlpha()) + ",   "
        + nf.format(uc.xalToTraceTransverse(twiss[0]).getBeta()) + ",   "
        + nf.format(uc.xalToTraceTransverse(twiss[1]).getAlpha()) + ",   "
        + nf.format(uc.xalToTraceTransverse(twiss[1]).getBeta()) + ",   "
        + nf.format(uc.xalToTraceLongitudinal(twiss[2]).getAlpha()) + ",   "
        + nf.format(uc.xalToTraceLongitudinal(twiss[2]).getBeta()) + ",\n"
        + " BEAMCI =      "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).getx() + ",   "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).getxp() + ",   "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).gety() + ",    "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).getyp() + ",      "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).getz() + ",      "
        + uc.xalToTraceCoordinates(myProbe.phaseMean()).getzp() + ",\n"
        
        + " FREQ=   402.500, ICHROM= 0, IBS= 0,\n"
        + " N1=  1, N2= " + elementCount
        + ", SMAX=   2.0, \n"
        + " VAL=     0.0000000,     0.0000000,     0.0000000,     0.0000000,     0.0000000,     0.0000000,\n"
        + " ISECURE=0,\n";
        
        LatticeIterator ilat=myLattice.latticeIterator();
        int counter = 1;
        String str = t3d_header;
        int devTypeInd = 1;
        String devStr = "";
        
        char buffer_header[] = new char[str.length()];
        str.getChars(0, str.length(), buffer_header, 0);
        t3d_input.write(buffer_header);
        
        while(ilat.hasNext()) {
            Element element = ilat.next();
            // for regular drift space, diagnostic devices
            if (element.getType().equals("drift") ||
                element.getType().equals("beampositionmonitor") ||
                element.getType().equals("beamcurrentmonitor") ||
                element.getType().equals("beamlossmonitor") ||
                element.getType().equals("wirescanner") ||
                element.getType().equals("pmarker") ||
                element.getType().equals("foil")  ) {
                devTypeInd = 1;
                devStr = devTypeInd + ", A(1, " + counter + ")="
                + nf.format(element.getLength()*1000.) + ",\n";
            }
            // for quads
            else if (element.getType().equals("quadrupole") ) {
                devTypeInd = 3;
                // for PM quads, i.e. always use design field
                if (element.getAcceleratorNode().getType().equals("PMQH") ||
                    element.getAcceleratorNode().getType().equals("PMQV") ) {
                    devStr = devTypeInd + ", A(1, " + counter + ")="
                    + nf.format(((xal.smf.impl.PermQuadrupole) element.getAcceleratorNode()).getDfltField()) + ", "
                    + nf.format(element.getLength()*1000.) + ",\n";
                } else {
                    if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN))
                        devStr = devTypeInd + ", A(1, " + counter + ")="
                        + nf.format(((xal.smf.impl.Quadrupole) element.getAcceleratorNode()).getDfltField()) + ", "
                        + nf.format(element.getLength()*1000.) + ",\n";
                    else if (srcSelector.equals(Scenario.SYNC_MODE_LIVE) ||
                             srcSelector.equals(Scenario.SYNC_MODE_RF_DESIGN) ) {
                        try{
                            devStr = devTypeInd + ", A(1, " + counter + ")="
                            + nf.format(((xal.smf.impl.Quadrupole) element.getAcceleratorNode()).getField()) + ", "
                            + nf.format(element.getLength()*1000.) + ",\n";
                        }
                        catch(ConnectionException e){
                            devStr = devTypeInd + ", A(1, " + counter + ")="
                            + "0., " + nf.format(element.getLength()*1000.) + ",\n";
                            System.out.println(e + "   Set the field to 0.");
                        }
                        catch(GetException e){}
                    }
                }
            }
            // for bending dipole
            else if(element.getType().equals("dipole")){
                devTypeInd = 8;
                if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN))
                    devStr = devTypeInd + ", A(1, " + counter + ")="
                    + nf.format(((xal.smf.impl.Bend) element.getAcceleratorNode()).getDfltBendAngle()/2.0) + ", "
                    + "0, "
                    + "0, "
                    + "0,\n";
                
            }
            // for solenoid
            else if (element.getType().equals("solenoid")) {
                devTypeInd = 5;
                if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN))
                    devStr = devTypeInd + ", A(1, " + counter + ")="
                    + nf.format(((xal.smf.impl.Solenoid) element.getAcceleratorNode()).getDfltField()*10000.) + ", "
                    + nf.format(((xal.smf.impl.Solenoid) element.getAcceleratorNode()).getLength()*1000.)
                    + ",\n";
                else if (srcSelector.equals(Scenario.SYNC_MODE_LIVE) ||
                         srcSelector.equals(Scenario.SYNC_MODE_RF_DESIGN) ) {
                    try {
                        devStr = devTypeInd + ", A(1, " + counter + ")="
                        + nf.format(((xal.smf.impl.Solenoid) element.getAcceleratorNode()).getField()*10000.) + ", "
                        + nf.format(((xal.smf.impl.Solenoid) element.getAcceleratorNode()).getLength()*1000.)
                        + ",\n";
                    } catch (ConnectionException e) {
                        devStr = devTypeInd + ", A(1, " + counter + ")="
                        + "0., " + nf.format(element.getLength()*1000.) + ",\n";
                    } catch (GetException e) {
                        devStr = devTypeInd + ", A(1, " + counter + ")="
                        + "0., " + nf.format(element.getLength()*1000.) + ",\n";
                    }
                }
            }
            // for horizontal dipole correctors
            else if(element.getType().equals("hsteerer")){
                devTypeInd = 19;
                if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN))
                    devStr = devTypeInd + ", A(1, " + counter + ")="
                    + nf.format(((xal.smf.impl.HDipoleCorr) element.getAcceleratorNode()).getDfltField())
                    + ", 0,\n";
                else if (srcSelector.equals(Scenario.SYNC_MODE_LIVE) ||
                         srcSelector.equals(Scenario.SYNC_MODE_RF_DESIGN)) {
                    try{
                        devStr = devTypeInd + ", A(1, " + counter + ")="
                        + nf.format(((xal.smf.impl.HDipoleCorr) element.getAcceleratorNode()).getField()*
                                    ((xal.smf.impl.HDipoleCorr) element.getAcceleratorNode()).getLength())
                        + ", 0,\n";
                    }
                    catch(ConnectionException e){
                        devStr = devTypeInd + ", A(1, " + counter + ")="
                        + "0., 0,\n";
                    }
                    catch(GetException e){}
                }
            }
            // for vertical dipole correctors
            else if(element.getType().equals("vsteerer")) {
                devTypeInd = 19;
                if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN))
                    devStr = devTypeInd + ", A(1, " + counter + ")="
                    + nf.format(((xal.smf.impl.VDipoleCorr) element.getAcceleratorNode()).getDfltField())
                    + ", 1,\n";
                else if (srcSelector.equals(Scenario.SYNC_MODE_LIVE) ||
                         srcSelector.equals(Scenario.SYNC_MODE_RF_DESIGN)) {
                    try{
                        devStr = devTypeInd + ", A(1, " + counter + ")="
                        + nf.format(((xal.smf.impl.VDipoleCorr) element.getAcceleratorNode()).getField()*
                                    ((xal.smf.impl.VDipoleCorr) element.getAcceleratorNode()).getLength())
                        + ", 1,\n";
                    }
                    catch(ConnectionException e) {
                        devStr = devTypeInd + ", A(1, " + counter + ")="
                        + "0., 1,\n";
                    }
                    catch(GetException e){}
                }
            }
            // for rf gaps
            else if(element.getType().equals("rfgap")) {
                devTypeInd = 10;
                if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN) ||
                    srcSelector.equals(Scenario.SYNC_MODE_RF_DESIGN))
                    devStr = devTypeInd + ", A(1, " + counter + ")="
                    + nf.format(((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapDfltE0TL()) + ", "
                    + nf.format(((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapDfltPhase()) + ", "
                    + "1, 1, 1, "
                    + "\n";
                else if (srcSelector.equals(Scenario.SYNC_MODE_LIVE)) {
                    try{
                        devStr = devTypeInd + ", A(1, " + counter + ")="
                        + nf.format(((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapE0TL()) + ", "
                        + nf.format(((xal.smf.impl.RfGap) element.getAcceleratorNode()).getGapPhaseAvg()) + ", "
                        + "1, 1, 1, "
                        + "\n";
                    }
                    catch(ConnectionException e){
                        devStr = devTypeInd + ", A(1, " + counter + ")=" 
                        + "0., 0., 1, 1, 1, \n";	       
                    }
                    catch(GetException e){}
                }
            }
            
            if (element.getType().equals("drift") ||
                element.getType().equals("beampositionmonitor") ||
                element.getType().equals("beamcurrentmonitor") ||
                element.getType().equals("beamlossmonitor") ||
                element.getType().equals("wirescanner") ||
                element.getType().equals("pmarker") ||
                element.getType().equals("foil")  )         
                str = "CMT(" + counter + ")='" + element.getName() + "', NT(" + counter + ")="
                + devStr;
            else
                str = "CMT(" + counter + ")='" + element.getAcceleratorNode().getId() + "', NT(" + counter + ")="
                + devStr;
            
            char buffer[] = new char[str.length()];
            str.getChars(0, str.length(), buffer, 0);
            t3d_input.write(buffer);	
            
            counter++;
        }
        if (srcSelector.equals(Scenario.SYNC_MODE_DESIGN))
            str = " COMMENT='" + today.toString() + "  Design Lattice" + "'\n"
            + " $END";
        else  if (srcSelector.equals(Scenario.SYNC_MODE_LIVE)|| 
	              srcSelector.equals(Scenario.SYNC_MODE_RF_DESIGN))
            str = " COMMENT='" + today.toString() + "  Measured Lattice" + "'\n"
            + " $END";
        
        char buffer_end[] = new char[str.length()];
        str.getChars(0, str.length(), buffer_end, 0);
        t3d_input.write(buffer_end);	
        
        t3d_input.close();   
        
    }
    
}
