/**
 * TestCompareLiveAccelerator.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 8, 2014
 */
package xal.sim.run;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.IComponent;
import xal.model.IElement;
import xal.model.alg.ParticleTracker;
import xal.model.elem.IdealMagQuad;
import xal.model.elem.IdealMagSteeringDipole;
import xal.model.elem.IdealRfGap;
import xal.model.probe.ParticleProbe;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.BPM;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.RfGap;
import xal.smf.impl.SCLCavity;
import xal.smf.impl.VDipoleCorr;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.smf.proxy.RfCavityPropertyAccessor;
import xal.smf.proxy.RfGapPropertyAccessor;
import xal.tools.ResourceManager;
import xal.tools.beam.PhaseVector;

/**
 * Test cases for the Orbit Correction anomaly seen in Open XAL.
 *
 *
 * @author Christopher K. Allen
 * @since  Sep 8, 2014
 */
public class TestCompareLiveAccelerator {
    

    /*
     * Global Constants
     */
    
    
    /** Flag used for indicating whether to type out to stout or file */
    private static final boolean        BOL_TYPE_STOUT = false;
    
    /** Flag used for running tests involving live accelerator */
    private static final boolean        BOL_LIVE_TESTS = false;
    
    /** Flag used for comparing the design and production trajectories (otherwise just compute design) */
    private static final boolean        BOL_COMPARE = false;
    

    /** Location of the output file */
    static final private String         STR_FILENAME_OUTPUT = "Output.txt";

    
    
    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_DSGN = "/site/optics/design/main.xal";

    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_PROD = "/site/optics/production/main.xal";
    
    
    /** The sequence we are testing in both accelerator configurations */
    static final private String         STR_ID_TESTSEQ = "SCLMed";

    
    /*
     * Global Attributes
     */
    
    /** The design Accelerator under test */
    static private Accelerator          ACCEL_DSGN;
    
    /** The production Accelerator under test */
    static private Accelerator          ACCEL_PROD;
    
    
    /** The design Accelerator Sequence under test */
    static private AcceleratorSeq     SEQ_PROD;

    /** The design Accelerator Sequence under test */
    static private AcceleratorSeq     SEQ_DSGN;
    
    
    /** The online model of the design accelerator sequence */
    static private Scenario           MOD_DSGN;

    /** The online model of the production accelerator sequence */
    static private Scenario           MOD_PROD;
    
    
    /** The results output file stream */
    static private FileWriter         WTR_OUTPUT;

    
    
    /*
     * Global Methods
     */
    
    /**
     * Loads an SMF accelerator object given the path relative to the
     * Open XAL project home (i.e., OPENXAL_HOME).
     * 
     * @param arrPathRel    relative path to the accelerator configuration file
     * 
     * @return              SMF accelerator object loaded from the given path
     *
     * @author Christopher K. Allen
     * @since  Sep 8, 2014
     */
    private static Accelerator loadAccelerator(String... arrPathRel) {
        if (arrPathRel.length == 0)
            return XMLDataManager.loadDefaultAccelerator();
        
        String  strPathXal = ResourceManager.getProjectHomePath();
        String  strFileAccel = strPathXal + arrPathRel[0];
        
        Accelerator accel = XMLDataManager.acceleratorWithPath(strFileAccel);
        return accel;
    }
    
    /**
     * Creates a new output file in the testing output directory with the 
     * given file name.
     * 
     * @param strFileName   name of the output file
     * 
     * @return              new output file object
     *
     * @author Christopher K. Allen
     * @since  Sep 11, 2014
     */
    private static File createOutputFile(String strFileName) {
        String  strPack     = TestCompareLiveAccelerator.class.getPackage().getName();
        String  strPathRel  = strPack.replace('.', '/');
        String  strPathFile = strPathRel + '/' + strFileName; 
        File    fileOutput  = xal.test.ResourceManager.getOutputFile(strPathFile);
        
        return fileOutput;
    }
    
    
    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Sep 8, 2014
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        if (BOL_COMPARE) {
            ACCEL_DSGN = loadAccelerator(STR_CFGFILE_DSGN);
            ACCEL_PROD = loadAccelerator(STR_CFGFILE_PROD);
        } else {
            ACCEL_DSGN = loadAccelerator();
            ACCEL_PROD = loadAccelerator();
        }

        SEQ_DSGN = ACCEL_DSGN.getSequence(STR_ID_TESTSEQ);
        SEQ_PROD = ACCEL_PROD.getSequence(STR_ID_TESTSEQ);
        
        MOD_DSGN = Scenario.newScenarioFor(SEQ_DSGN);
        MOD_DSGN.setSynchronizationMode(Scenario.SYNC_MODE_LIVE);
        MOD_DSGN.resync();
        
        MOD_PROD = Scenario.newScenarioFor(SEQ_PROD);
        MOD_PROD.setSynchronizationMode(Scenario.SYNC_MODE_LIVE);
        MOD_PROD.resync();
        
        if (!BOL_TYPE_STOUT) {
            File       fileOut = createOutputFile(STR_FILENAME_OUTPUT);
            
            WTR_OUTPUT = new FileWriter(fileOut);
        }
    }

    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Sep 8, 2014
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (!BOL_TYPE_STOUT) {
            WTR_OUTPUT.close();
        }
    }


    /**
     * Test method for {@link xal.smf.AcceleratorSeq#getNodeWithId(java.lang.String)}.
     */
    @Test
    public final void testGetNodeWithId() {
        String  strId1 = "SCL_Mag:DCH05";
        String  strId2 = "SCL_Mag:DCV05";
        
        AcceleratorNode     smfDesign1 = SEQ_DSGN.getNodeWithId(strId1);
        AcceleratorNode     smfDesign2 = SEQ_DSGN.getNodeWithId(strId2);
        
        this.typeOut("Node 1 = " + smfDesign1);
        this.typeOut("Node 2 = " + smfDesign2);
        this.typeOut();
        
        IComponent      modDsgn1 = MOD_DSGN.elementsMappedTo(smfDesign1).get(0);
        IComponent      modDsgn2 = MOD_DSGN.elementsMappedTo(smfDesign2).get(0);
        
        this.typeOut("Model element 1 = " + modDsgn1);
        this.typeOut("Model element 2 = " + modDsgn2);
        this.typeOut();
    }
    
    /**
     * Types out the contents of the design and production model lattices.
     *
     * @author Christopher K. Allen
     * @since  Sep 11, 2014
     */
    @Test
    public final void testTypeOutModelElements() {
        List<IComponent>  lstDsgn = MOD_DSGN.getLattice().getAllElements();
        List<IComponent>  lstProd = MOD_PROD.getLattice().getAllElements();
        
        this.typeOut("MODEL ELEMENT COMPARISON - Design Values Versus Production");
        this.typeOut("Design Lattice Element Count       = " + lstDsgn.size());
        this.typeOut("Production Lattice Element Count   = " + lstProd.size());
        this.typeOut();
        
        int cntNodes = lstDsgn.size() < lstProd.size() ? lstDsgn.size() : lstProd.size();
        
        this.typeOut("  Design                   Production" );
        for (int i=0; i<cntNodes; i++) {
            IComponent  elmDsgn = lstDsgn.get(i);
            IComponent  elmProd = lstDsgn.get(i);
            
            String strOut = "  " + elmDsgn.getId() + ", L=" + elmDsgn.getLength()
                          + "      " + elmProd.getId() + ", L=" + elmProd.getLength();
            this.typeOut(strOut);
        }
        
        this.typeOut();
    }

    /**
     * Compares the current live values of BMPs with simulation results at the
     * BPM locations.
     * 
     * @throws InstantiationException 
     */
    @Test
    public final void testCompareBpmsLiveAndSimulation() {
        if (!BOL_LIVE_TESTS)
            return;
        
        List<AcceleratorNode> lstNodesProd = SEQ_PROD.getAllNodesOfType(BPM.s_strType);
        
        this.typeOut("BPM NODE COMPARISON - Live Values Versus Production Simulation");
        this.typeOut("Production Sequence Node Count = " + lstNodesProd.size());
        this.typeOut();
        
        try {
        
            ParticleTracker algDsgn = AlgorithmFactory.createParticleTracker(SEQ_DSGN);
            ParticleProbe   prbDsgn = ProbeFactory.createParticleProbe(SEQ_DSGN, algDsgn);
            
            MOD_DSGN.setProbe(prbDsgn);
            MOD_DSGN.run();
            
            Trajectory<ParticleProbeState>  trjDsgn = MOD_DSGN.getTrajectory();

            ParticleTracker algProd = AlgorithmFactory.createParticleTracker(SEQ_PROD);
            ParticleProbe   prbProd = ProbeFactory.createParticleProbe(SEQ_PROD, algProd);
            
            MOD_PROD.setProbe(prbProd);
            MOD_PROD.run();
            
            Trajectory<ParticleProbeState>  trjProd = MOD_PROD.getTrajectory();
            
            for (AcceleratorNode smfNode : lstNodesProd) {
                BPM                 smfProd = (BPM) smfNode;
                ParticleProbeState  ppsProd = trjProd.stateForElement(smfProd.getId());
                PhaseVector         vecProd = ppsProd.getPhaseCoordinates();
                ParticleProbeState  ppsDsgn = trjDsgn.stateForElement(smfProd.getId());
                PhaseVector         vecDsgn = ppsDsgn.getPhaseCoordinates();

                this.typeOut("BPM " + smfProd.getId() + " s=" + smfProd.getPosition() );
                this.typeOut("  Live      : " + " Xavg=" + smfProd.getXAvg() + ", Yavg=" + smfProd.getYAvg());
                this.typeOut("  Sim Design: " + " Xavg=" + vecDsgn.getx()*1.e3 + ", Yavg=" + vecDsgn.gety()*1.e3);
                this.typeOut("  Sim Prod  : " + " Xavg=" + vecProd.getx()*1.e3 + ", Yavg=" + vecProd.gety()*1.e3);
                this.typeOut();
            }

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            
        }
    }

    /**
     * Compares the current live values of the dipole correctors with their reported default
     * values for production case.
     */
    @Test
    public final void testCompareDipoleCorrectsLiveAndModel() {
        if (!BOL_LIVE_TESTS)
            return;
        
        List<AcceleratorNode> lstNodesProdH = SEQ_PROD.getAllNodesOfType(HDipoleCorr.s_strType);
        List<AcceleratorNode> lstNodesProdV = SEQ_PROD.getAllNodesOfType(VDipoleCorr.s_strType);
        List<AcceleratorNode> lstNodesDsgnH = SEQ_DSGN.getAllNodesOfType(HDipoleCorr.s_strType);
        List<AcceleratorNode> lstNodesDsgnV = SEQ_DSGN.getAllNodesOfType(VDipoleCorr.s_strType);
        
        this.typeOut("DIPOLE CORRECTOR NODE COMPARISON - Live Values Versus Production");
        this.typeOut("Production Sequence Horizontal Node Count = " + lstNodesProdH.size());
        this.typeOut("Production Sequence Vertical Node Count   = " + lstNodesProdV.size());
        this.typeOut();
        
        int cntNodes = lstNodesProdH.size() < lstNodesProdV.size() ? lstNodesProdH.size() : lstNodesProdV.size();

        try {
        
            for (int i=0; i<cntNodes; i++) {
                HDipoleCorr smfProdH = (HDipoleCorr) lstNodesProdH.get(i);
                HDipoleCorr smfDsgnH = (HDipoleCorr) lstNodesDsgnH.get(i);
                VDipoleCorr smfProdV = (VDipoleCorr) lstNodesProdV.get(i);
                VDipoleCorr smfDsgnV = (VDipoleCorr) lstNodesDsgnV.get(i);

                this.typeOut("Corrector dsgnId=" + smfDsgnH.getId() + ", prodId=" + smfProdH.getId() + ", s=" + smfProdH.getPosition() );
                this.typeOut("  Production : " + " Bdft=" + smfProdH.getDfltField());

                if (smfProdH.getStatus() == true) {
                    this.typeOut("  Live Values: " + " Bliv=" + smfProdH.getField());

                    List<IElement>  lstDsgnH = MOD_DSGN.elementsMappedTo(smfDsgnH);
                    if (lstDsgnH == null)
                        continue;
                    IdealMagSteeringDipole modDsgnH = (IdealMagSteeringDipole)lstDsgnH.get(0);
                    this.typeOut("  Model Dsgn : " + " Bmod=" + modDsgnH.getMagField());

                    List<IElement>  lstProdH = MOD_PROD.elementsMappedTo(smfProdH);
                    if (lstProdH == null)
                        continue;
                    IdealMagSteeringDipole modProdH = (IdealMagSteeringDipole)lstProdH.get(0);
                    this.typeOut("  Model Prod : " + " Bmod=" + modProdH.getMagField());

                } else {
                    
                    this.typeOut("  Bad Status");
                }
                this.typeOut();

                this.typeOut("Corrector dsgnId=" + smfDsgnV.getId() + ", prodId=" + smfProdV.getId() + ", s=" + smfProdV.getPosition() );
                this.typeOut("  Production : " + " Bdft=" + smfProdV.getDfltField());
                
                if (smfProdV.getStatus() == true) {
                    this.typeOut("  Live Values: " + " Bliv=" + smfProdV.getField());

                    List<IElement>  lstDsgnV = MOD_DSGN.elementsMappedTo(smfDsgnV);
                    if (lstDsgnV == null)
                        continue;
                    IdealMagSteeringDipole modDsgnV = (IdealMagSteeringDipole)lstDsgnV.get(0);
                    this.typeOut("  Model Dsgn : " + " Bmod=" + modDsgnV.getMagField());

                    List<IElement>  lstProdV = MOD_PROD.elementsMappedTo(smfProdV);
                    if (lstProdV == null)
                        continue;
                    IdealMagSteeringDipole modProdV = (IdealMagSteeringDipole)lstProdV.get(0);
                    this.typeOut("  Model Prod : " + " Bmod=" + modProdV.getMagField());

                } else {
                    this.typeOut("  Bad Status");
                }
                this.typeOut();
            }

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            
        }
    }

    /**
     * Compare the RF Gap values for the design accelerator and the production accelerator objects.
     */
    @Test
    public final void testCompareRfGapDesignAndProd() {
        List<AcceleratorNode> lstNodesDesign = SEQ_DSGN.getAllNodesOfType(RfGap.s_strType);
        List<AcceleratorNode> lstNodesProd = SEQ_PROD.getAllNodesOfType(RfGap.s_strType);
        
        this.typeOut("RF GAP NODE COMPARISON - Design Versus Production");
        this.typeOut("Design Sequence Node Count     = " + lstNodesDesign.size());
        this.typeOut("Production Sequence Node Count = " + lstNodesProd.size());
        this.typeOut();
        
        int cntNodes = lstNodesProd.size() < lstNodesDesign.size() ? lstNodesProd.size() : lstNodesDesign.size();
        
        for (int index=0; index<cntNodes; index++) {
            RfGap     smfDsgn = (RfGap) lstNodesDesign.get(index);
            RfGap     smfProd = (RfGap) lstNodesProd.get(index);
            
            this.typeOut("  Design    : " + smfDsgn.getId() + " s=" + smfDsgn.getPosition() + ", Edft=" + smfDsgn.getGapDfltAmp() + ", phi_dft=" + smfProd.getGapDfltPhase());
            this.typeOut("  Production: " + smfProd.getId() + " s=" + smfProd.getPosition() + ", Edft=" + smfProd.getGapDfltAmp() + ", phi_dft=" + smfProd.getGapDfltPhase());
            this.typeOut();
        }
    }
    
    /**
     * Compares the current live values of RF gaps with their reported default
     * values for production case.
     */
    @Test
    public final void testCompareRfGapsLiveAndProd() {
        if (!BOL_LIVE_TESTS)
            return;
        
        List<AcceleratorNode> lstNodesProd = SEQ_PROD.getAllNodesOfType(RfGap.s_strType);
        
        this.typeOut("RF GAP NODE COMPARISON - Live Values Versus Production");
        this.typeOut("Production Sequence Node Count = " + lstNodesProd.size());
        this.typeOut();
        
        try {
        
            final double    dblRad2Deg = 180.0/Math.PI;
            double[]        arrDummy = {0,1};
            
            for (AcceleratorNode smfNode : lstNodesProd) {
                RfGap       smfProd = (RfGap) smfNode;
                IdealRfGap  modProd = (IdealRfGap)MOD_PROD.elementsMappedTo(smfProd).get(0);

                smfProd.getLivePropertyValue(RfGapPropertyAccessor.PROPERTY_ETL,arrDummy);
                
                this.typeOut("RF Gap " + smfProd.getId() + " s=" + smfProd.getPosition() );
                this.typeOut("  Production : " + " ETLdft=" + smfProd.getGapDfltE0TL() + ", phi_dft=" + smfProd.getGapDfltPhase());
                this.typeOut("  Live Values: " + " ETLavg=" + smfProd.getGapE0TL() + ", phi_avg=" + smfProd.getGapPhaseAvg() + 
                        ", ETLlive=" + arrDummy[0]);
                this.typeOut("  Sync Model : " + " ETLmod=" + modProd.getETL()/1.e6 + ", phi_mod=" + dblRad2Deg*modProd.getPhase());
                this.typeOut();
            }

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link xal.smf.AcceleratorSeq#getNodesOfType(java.lang.String)}.
     */
    @Test
    public final void testCompareQuadrupoleDesignAndProd() {
        List<AcceleratorNode> lstNodesDesign = SEQ_DSGN.getAllNodesOfType(Quadrupole.s_strType);
        List<AcceleratorNode> lstNodesProd = SEQ_PROD.getAllNodesOfType(Quadrupole.s_strType);
        
        this.typeOut("QUADRUPOLE NODE COMPARISON - Design Versus Production");
        this.typeOut("Design Sequence Node Count     = " + lstNodesDesign.size());
        this.typeOut("Production Sequence Node Count = " + lstNodesProd.size());
        this.typeOut();
        
        int cntNodes = lstNodesProd.size() < lstNodesDesign.size() ? lstNodesProd.size() : lstNodesDesign.size();
        
        for (int index=0; index<cntNodes; index++) {
            Quadrupole     smfDsgn = (Quadrupole) lstNodesDesign.get(index);
            Quadrupole     smfProd = (Quadrupole) lstNodesProd.get(index);
            
            this.typeOut("  Design    : " + smfDsgn.getId() + " s=" + smfDsgn.getPosition() + ", B=" + smfDsgn.getDfltField());
            this.typeOut("  Production: " + smfProd.getId() + " s=" + smfProd.getPosition() + ", B=" + smfProd.getDfltField());
            this.typeOut();
        }
    }
    
    /**
     * Compares the current live values of quadrupoles with their reported default
     * values for production case.
     */
    @Test
    public final void testCompareQuadrupoleLiveAndProd() {
        if (!BOL_LIVE_TESTS)
            return;
        
        List<AcceleratorNode> lstNodesProd = SEQ_PROD.getAllNodesOfType(Quadrupole.s_strType);
        
        this.typeOut("QUADRUPOLE NODE COMPARISON - Live Values Versus Production");
        this.typeOut("Production Sequence Node Count = " + lstNodesProd.size());
        this.typeOut();
        
        try {
            
            double[]    arrDummy = {0,1};
        
            for (AcceleratorNode smfNode : lstNodesProd) {
                Quadrupole     smfProd = (Quadrupole) smfNode;
                IdealMagQuad   modProd = (IdealMagQuad)MOD_PROD.elementsMappedTo(smfProd).get(0);

                this.typeOut("Quadrupole " + smfProd.getId() + " s=" + smfProd.getPosition() );
                this.typeOut("  Production : " + " B=" + smfProd.getDfltField());
                this.typeOut("  Live Values: " + " B=" + smfProd.getFieldReadback() + ", Blive=" + smfProd.getLivePropertyValue(ElectromagnetPropertyAccessor.PROPERTY_FIELD, arrDummy));
                this.typeOut("  Sync Model : " + " B=" + modProd.getMagField());
                this.typeOut();
            }

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link xal.smf.AcceleratorSeq#getNodesOfType(java.lang.String)}.
     */
    @Test
    public final void testCompareScCavitiesDesignAndProduction() {
        List<AcceleratorNode> lstNodesDesign = SEQ_DSGN.getAllNodesOfType(SCLCavity.s_strType);
        List<AcceleratorNode> lstNodesProd = SEQ_PROD.getAllNodesOfType(SCLCavity.s_strType);
        
        this.typeOut("SCL CAVITY NODE COMPARISON - Design Versus Production");
        this.typeOut("Design Sequence Node Count     = " + lstNodesDesign.size());
        this.typeOut("Production Sequence Node Count = " + lstNodesProd.size());
        this.typeOut();
        
        int cntNodes = lstNodesProd.size() < lstNodesDesign.size() ? lstNodesProd.size() : lstNodesDesign.size();
        
        for (int index=0; index<cntNodes; index++) {
            SCLCavity     smfDsgn = (SCLCavity)lstNodesDesign.get(index);
            SCLCavity     smfProd = (SCLCavity)lstNodesProd.get(index);
            
            this.typeOut("  Design    : " + smfDsgn.getId() + " s=" + smfDsgn.getPosition() + ", V=" + smfDsgn.getDfltCavAmp() + ", phi=" + smfDsgn.getDfltCavPhase() + ", mode=" + smfDsgn.getStructureMode() + ", TTF=" + smfDsgn.getStructureTTF());
            this.typeOut("  Production: " + smfProd.getId() + " s=" + smfProd.getPosition() + ", V=" + smfProd.getDfltCavAmp() + ", phi=" + smfProd.getDfltCavPhase() + ", mode=" + smfProd.getStructureMode() + ", TTF=" + smfProd.getStructureTTF());
            this.typeOut();
        }
    }

    /**
     * Compares the current live values of SCL cavities with their reported default
     * values for production case.
     */
    @Test
    public final void testCompareRfCavitiesLiveAndProd() {
        if (!BOL_LIVE_TESTS)
            return;
        
        List<AcceleratorNode> lstNodesProd = SEQ_PROD.getAllNodesOfType(SCLCavity.s_strType);
        
        this.typeOut("SCL CAVITY NODE COMPARISON - Live Values Versus Production");
        this.typeOut("Production Sequence Node Count = " + lstNodesProd.size());
        this.typeOut();
        
        try {
        
            double[]        arrDummy = {0,1};
            
            for (AcceleratorNode smfNode : lstNodesProd) {
                SCLCavity smfProd = (SCLCavity) smfNode;

                this.typeOut("SCL Cavity " + smfProd.getId() + " s=" + smfProd.getPosition() );
                this.typeOut("  Production : " + " Vdft=" + smfProd.getDfltCavAmp() + ", phi_dft=" + smfProd.getDfltCavPhase() + ", TTF=" + smfProd.getStructureTTF());
                this.typeOut("  Live Values: " + " Vavg=" + smfProd.getCavAmpAvg() + ", phi_avg=" + smfProd.getCavPhaseAvg() + ", V*TTF=" + smfProd.getStructureTTF()*smfProd.getCavAmpAvg() + " Vset=" + smfProd.getCavAmpSetPoint() + ", Vlive=" +  smfProd.getLivePropertyValue(RfCavityPropertyAccessor.PROPERTY_AMPLITUDE,  arrDummy));
                this.typeOut();
            }

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            
        }
    }


    /*
     * Support Methods
     */
    
    /**
     * Types to the standard output or to the global class file depending
     * upon what the type flag is set to.
     * 
     * @param strOutput     string to be typed out, new line if empty
     *
     * @author Christopher K. Allen
     * @since  Sep 11, 2014
     */
    private void    typeOut(String ...arrOutput) {
        String  strOutput;
        
        if (arrOutput.length == 0)
            strOutput = "\n";
        else
            strOutput = arrOutput[0];
            
        if (BOL_TYPE_STOUT) {
            System.out.println(strOutput);
            
        } else {
            try {
                
                WTR_OUTPUT.write(strOutput);
                WTR_OUTPUT.write("\n");
            } catch (IOException e) {
 
                e.printStackTrace();
                fail("Unable to write to file: " + strOutput);
            }
        }
    }

}
