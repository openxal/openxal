/*
 * Created on Apr 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package xal.model;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;


import junit.framework.TestCase;

import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.xml.LatticeXmlParser;
import xal.model.xml.LatticeXmlWriter;
import xal.model.xml.ParsingException;
import xal.model.xml.ProbeXmlParser;
import xal.model.xml.ProbeXmlWriter;
import xal.model.xml.TrajectoryXmlWriter;
import xal.tools.beam.CorrelationMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.data.DataFormatException;
import xal.tools.math.r3.R3;



/**
 * JUnit driver for XML Model subsystem validation
 * 
 * Currently configured for testing of the ParticleProbe
 * and EnvelopeProbe simulations.
 * 
 * @author Christopher Allen
 * @since  Apr 18, 2003
 */
public class ModelValidationTest extends TestCase {

    /*
     *  Global Attributes
     */	
     
    /** lattice file */
//    public static final String      s_strUrlLattice = "xml/ModelValidation.updated.lat.mod.xal.xml";
//    public static final String      s_strUrlLattice = "xml/ModelValidation.lat.mod.xal.xml";
	public static final String		s_strUrlLattice = "xml/ModelValidation.updated.lat.mod.xal.xml";
//    public static final String      s_strUrlLattice = "xml/ModelValidation.ElementTest.lat.mod.xal.xml";
    
    /** particle probe initialization file */
    public static final String      s_strUrlProbePart = "C:/Users/Snake/Projects/Sns/Code/xaldev/xal_xmls/MebtEntrance-particle.probe";
//    public static final String      s_strUrlProbePart = "xml/ModelValidation.particle.probe.mod.xal.xml";

    /** envelope probe initialization file */
    public static final String      s_strUrlProbeEnv = "C:/Users/Snake/Projects/Sns/Code/xaldev/xal_xmls/MebtEntrance-adapt-envelope.probe";
//    public static final String      s_strUrlProbeEnv = "xml/ModelValidation.envelope.probe.mod.xal.xml";
//    public static final String      s_strUrlProbeEnv = "xml/ModelValidation.offcenter.envelope.probe.mod.xal.xml";



    /** particle probe trajectory file */
    public static final String      s_strUrlTrajPart = "ModelValiation.Particle.Traj.xml";

    /** ascii file containing particle trajectory information */
    public static final String      s_strUrlTrajPartData = "ModelValitation.Particle.Traj.Data.txt";


    /** envelope probe trajectory file */
    public static final String      s_strUrlTrajEnv = "ModelValiation.Envelope.Traj.xml";

    /** ascii file containing envelope trajectory envelope data */
    public static final String      s_strUrlTrajEnvData = "ModelValitation.Envelope.Traj.Env.txt";

    /** ascii file containing envelope trajectory envelope data */
    public static final String      s_strUrlTrajEnvTwiss = "ModelValitation.Envelope.Traj.Twiss.txt";

    /** ascii file containing envelope matrix data */
    public static final String      s_strUrlTrajMatrix = "ModelValidation.Envelope.Matrix.txt";
         
     
     
    /** lattice debugging output */
    public static final String      s_strUrlLattDbg = "ModelValidation.Lattice.Dbg.xml"; 
    
    /** particle probe debugging output */
    public static final String      s_strUrlProbePartDbg = "ModelValidation.Particle.Probe.Dbg.xml";
     
    /** envelope probe debugging output */
    public static final String      s_strUrlProbeEnvDbg = "ModelValidation.Envelope.Probe.Dbg.xml";
     
     
     
    /*
     * 	Local Attributes
     */
	 
    /** lattice object used in validation test */
    private Lattice         m_lattTest = null;


	 
    /** particle probe used in validation */
    private ParticleProbe   m_probPart = null;
    
    /** trajectory of particle probe */
    private Trajectory      m_trajPart = null;
    

    
    /** envelope probe used for Trace3D-type validation */
    private EnvelopeProbe   m_probEnv = null;

    /** trajectory of envelope probe */
    private Trajectory      m_trajEnv = null;
    
    


    /**
     * Constructor for ModelValidationTest.
     * @param arg0
     */
    public ModelValidationTest(String arg0) {
        super(arg0);
    }
    
    /**
     *  Model Validation Tests Driver
     * 
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ModelValidationTest.class);
    }
    
    /**
     *  Save validation configuration for debugging as well as
     *  provide a test of XML persistent storage.
     */
    public void testValidationConfiguration()   {
        System.out.println("Saving Validation Configuration...");
        System.out.println("  Lattice id          : " + this.m_lattTest.getId());
        System.out.println("  Lattice length   (m): " + this.m_lattTest.getLength());
        System.out.println("  Particle probe desc : " + this.m_probPart.getComment());
        System.out.println("  Envelope probe desc : " + this.m_probEnv.getComment());
        
        
        // save lattice configuration for debugging
        try {
            LatticeXmlWriter.writeXml(this.m_lattTest, s_strUrlLattDbg);
            
        } catch (java.io.IOException e) {
            fail("Lattice debug file write error: " + e.getMessage());
            
        }
        
        // save state of particle probe for debugging
        try {
            ProbeXmlWriter.writeXml(this.m_probPart, s_strUrlProbePartDbg);
            
        } catch (java.io.IOException e) {
            fail("EnvelopeProbe debug file write error: " + e.getMessage());
            
        }

        // save state of envelope probe for debugging       
        try {
            ProbeXmlWriter.writeXml(this.m_probEnv, s_strUrlProbeEnvDbg);
            
        } catch (java.io.IOException e) {
            fail("EnvelopeProbe debug file write error: " + e.getMessage());
            
        }
        
    }
 
    /**
     * Run a validation of the envelope simulation.
     */
    public void testParticleValidation()    {
        System.out.println("Running Particle Validation...");
 
        // propagate the particle probe through lattice and get trajectory        
        try {
            this.m_lattTest.propagate(this.m_probPart);
            this.m_trajPart = this.m_probPart.getTrajectory();
            this.m_trajPart.setDescription("Model validation particle trajectory");
        
        } catch (ModelException e) {
            fail("ParticleProbe propagation error: " + e.getMessage());
            
        }
        
        // write out trajectory
        try {
            TrajectoryXmlWriter.writeXml(this.m_trajPart, s_strUrlTrajPart);
            
        } catch (java.io.IOException e) {
            fail("EnvelopeTrajectory file write error: " + e.getMessage());
            
        }
    }

    
    /**
     * Run a validation of the envelope simulation.
     */
    public void testEnvelopeValidation()    {
        System.out.println("Running Envelope Validation...");
 
        // propagate the envelope probe through lattice and get trajectory        
        try {
            this.m_lattTest.propagate(this.m_probEnv);
            this.m_trajEnv = this.m_probEnv.getTrajectory();
            this.m_trajEnv.setDescription("Model validation envelope trajectory");
        
        } catch (ModelException e) {
            fail("EnvelopeProbe propagation error: " + e.getMessage());
            
        }
        
        // write out trajectory
        try {
            TrajectoryXmlWriter.writeXml(this.m_trajEnv, s_strUrlTrajEnv);
            
        } catch (java.io.IOException e) {
            fail("EnvelopeTrajectory file write error: " + e.getMessage());
            
        }
    }




    /*
     * Support Methods
     */

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // load the lattice object        
        try {
            this.m_lattTest = LatticeXmlParser.parse(s_strUrlLattice, false);

        } catch (ParsingException e) {
            fail("Lattice Parsing Exception: " + e.getMessage());

        } catch (Exception e)   {
            fail("Lattice parsing general exception: " + e.getMessage());

        }
        
        // load the particle probe
        try {
            this.m_probPart = (ParticleProbe)ProbeXmlParser.parse(s_strUrlProbePart);
            
        } catch (DataFormatException e) {
            fail("Particle probe parsing exception: " + e.getMessage());
            
        }
        
        // load the envelope probe
        try {
            this.m_probEnv = (EnvelopeProbe)ProbeXmlParser.parse(s_strUrlProbeEnv);

        } catch (DataFormatException e) {
            fail("Envelope probe parsing exception: " + e.getMessage());

        }
        
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // save particle trajectory data
        System.out.println("Saving particle simulation data in ASCII format...");
        if (m_trajPart != null) {
            try {
                // save the particle probe trajectory positions
                FileOutputStream    osPos = new FileOutputStream(s_strUrlTrajPartData);
                
                savePartTrajData(new PrintStream(osPos));
                osPos.close();
                
                    
            } catch (java.io.IOException e) {
                fail("Unable to open particle trajectory data file - " + s_strUrlTrajEnvData);
                
            }
        }
                
        // save envelope trajectory data
        System.out.println("Saving envelope simulation data in ASCII format...");
        if (m_trajEnv != null)  {
            
            try {
                // save the envelope probe trajectory matrix 
                FileOutputStream osMatrix = new FileOutputStream(s_strUrlTrajMatrix);
                
                saveStateMatrix(new PrintStream(osMatrix));
                osMatrix.close();
                
                
                // save the envelope probe envelope parameters
                FileOutputStream osEnvel = new FileOutputStream(s_strUrlTrajEnvData);
                
                saveEnvTrajData(new PrintStream(osEnvel));
                osEnvel.close();
                
                
                // save the envelope probe twiss parameters
                FileOutputStream osTwiss = new FileOutputStream(s_strUrlTrajEnvTwiss);
                
                saveEnvTrajTwiss(new PrintStream(osTwiss));
                osTwiss.close();
                
                
            } catch (java.io.IOException e) {
                fail("Unable to open envelope trajectory data file - " + s_strUrlTrajEnvData);
                
            }
                        
        }
    }


    /**
     *  Save the particle trajectory position data in ASCII 
     *  format.
     * 
     * @param osFile    output stream to receive trajectory data
     */
    protected void savePartTrajData(PrintStream os) {
        Iterator<ProbeState>    iterState = this.m_trajPart.stateIterator();
        
        while (iterState.hasNext()) {
            ParticleProbeState state = (ParticleProbeState)iterState.next();
            
            double s = state.getPosition();
//            double W = state.getKineticEnergy();
            String strId = state.getElementId();
            
            PhaseVector vecPos = state.phaseCoordinates();
            double  x = vecPos.getx();
            double xp = vecPos.getxp();
            double  y = vecPos.gety();
            double yp = vecPos.getyp();
            double  z = vecPos.getz();
            double zp = vecPos.getzp();
            
            os.println(s + " " + strId + " " + x + " " + xp + " " + y + " " + yp + " " + z + " " + zp);
        }
            
    }
    
    /**
     *  Save the Correlation/Covariance matrix in ASCII format
     * 
     *  @param  os  output stream 
     * 
     *  @author Christopher Allen
     */
    protected void saveStateMatrix(PrintStream osFile)    {
        PrintWriter os        = new PrintWriter(osFile);
        Iterator<ProbeState>    iterState = this.m_trajEnv.stateIterator();
        
        while (iterState.hasNext()) {
            EnvelopeProbeState state = (EnvelopeProbeState)iterState.next();

//            CorrelationMatrix matCorr = state.getCorrelationMatrix();   
			CorrelationMatrix matCov  = state.phaseCovariance();
            
            matCov.print(os);
            os.println("");                     
        }
    }

 
    /**
     *  Save the EnvelopeTrajectory data in ASCII format
     * 
     *  @param  os  output stream 
     * 
     *  @author Christopher Allen
     */
    protected void saveEnvTrajData(PrintStream os)    {
        Iterator<ProbeState>    iterState = this.m_trajEnv.stateIterator();
        
        while (iterState.hasNext()) {
            EnvelopeProbeState state = (EnvelopeProbeState)iterState.next();
            
            double s = state.getPosition();
//            double W = state.getKineticEnergy();
            String strId = state.getElementId();
            
            Twiss[] arrTwiss = state.twissParameters();
            double X = arrTwiss[0].getEnvelopeRadius();
            double Xp = arrTwiss[0].getEnvelopeSlope();
//            double ex = arrTwiss[0].getEmittance();
            
            double Y = arrTwiss[1].getEnvelopeRadius();
            double Yp = arrTwiss[1].getEnvelopeSlope();
//            double ey = arrTwiss[1].getEmittance();
            
            double Z = arrTwiss[2].getEnvelopeRadius();
            double Zp = arrTwiss[2].getEnvelopeSlope();
//            double ez = arrTwiss[2].getEmittance();
  
//            os.println(s + " " + W + " " + X + " " + Xp + " " + Y + " " + Yp + " " + Z + " " + Zp);
            os.println(s + " " + strId + " " + X + " " + Xp + " " + Y + " " + Yp + " " + Z + " " + Zp);
        }
    }
 
    /**
     *  Save the EnvelopeTrajectory Twiss parameters in ASCII 
     *  format.
     * 
     *  @param  os  output stream 
     * 
     *  @author Christopher Allen
     */
    protected void saveEnvTrajTwiss(PrintStream os)    {
        Iterator<ProbeState>    iterState = this.m_trajEnv.stateIterator();
        
        while (iterState.hasNext()) {
            EnvelopeProbeState state = (EnvelopeProbeState)iterState.next();
            
            double s = state.getPosition();
            double W = state.getKineticEnergy();
            
            Twiss[] arrTwiss = state.twissParameters();
            double ax = arrTwiss[0].getAlpha();
            double bx = arrTwiss[0].getBeta();
            double ex = arrTwiss[0].getEmittance();
//            double px = arrTwiss[0].computeRotation();
            
            double ay = arrTwiss[1].getAlpha();
            double by = arrTwiss[1].getBeta();
            double ey = arrTwiss[1].getEmittance();
//            double py = arrTwiss[1].computeRotation();
            
            double az = arrTwiss[2].getAlpha();
            double bz = arrTwiss[2].getBeta();
            double ez = arrTwiss[2].getEmittance();
//            double pz = arrTwiss[2].computeRotation();
            
            PhaseVector vecMean = state.phaseMean();
            double x = vecMean.getx();
            double y = vecMean.gety();
            double z = vecMean.getz();
            
            R3      vecPhase = state.getBetatronPhase();
            double  phix = vecPhase.get1();
            double  phiy = vecPhase.get2(); 
            double  phiz = vecPhase.get3();
            
            double  etax = state.getChromDispersionX();
            double  etay = state.getChromDispersionY();
            
            
            // Convert to Trace3D units
            az = -az;
            bz = bz/0.756259;
            
  
            os.println(s + " " + W + " " + ax + " " + bx + " " + ex + " " + ay + " " + by + " " + ey + " " + az + " " + bz + " " + ez + " " + x + " " + y + " " + z + " " + phix + " " + phiy + " " + phiz + " " + etax + " " + etay);
        }
    }
}
