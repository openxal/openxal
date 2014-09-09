/**
 * TestCompareLiveAccelerator.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 8, 2014
 */
package xal.smf;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.smf.data.XMLDataManager;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.RfCavity;
import xal.smf.impl.SCLCavity;
import xal.tools.ResourceManager;

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
    
    /** Flag used for running tests involving live accelerator */
    private static final boolean        BOL_LIVE_TESTS = true;
    

    
    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_DESIGN = "/site/optics/design/main.xal";

    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_PROD = "/site/optics/production/main.xal";
    
    
    /** The sequence we are testing in both accelerator configurations */
    static final private String         STR_ID_TESTSEQ = "SCLMed";

    
    /*
     * Global Attributes
     */
    
    /** The design Accelerator under test */
    static private Accelerator          ACCEL_DESIGN;
    
    /** The production Accelerator under test */
    static private Accelerator          ACCEL_PROD;
    
    
    /** The design Accelerator Sequence under test */
    static private AcceleratorSeq     SEQ_PROD;

    /** The design Accelerator Sequence under test */
    static private AcceleratorSeq     SEQ_DESIGN;

    
    /*
     * Global Methods
     */
    
    /**
     * Loads an SMF accelerator object given the path relative to the
     * Open XAL project home (i.e., OPENXAL_HOME).
     * 
     * @param strPathRel    relative path to the accelerator configuration file
     * 
     * @return              SMF accelerator object loaded from the given path
     *
     * @author Christopher K. Allen
     * @since  Sep 8, 2014
     */
    private static Accelerator loadAccelerator(String strPathRel) {
        String  strPathXal = ResourceManager.getProjectHomePath();
        String  strFileAccel = strPathXal + strPathRel;
        
        Accelerator accel = XMLDataManager.acceleratorWithPath(strFileAccel);
        return accel;
    }
    
    
    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Sep 8, 2014
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ACCEL_DESIGN = loadAccelerator(STR_CFGFILE_DESIGN);
        ACCEL_PROD   = loadAccelerator(STR_CFGFILE_PROD);
        
        SEQ_DESIGN = ACCEL_DESIGN.getSequence(STR_ID_TESTSEQ);
        SEQ_PROD   = ACCEL_PROD.getSequence(STR_ID_TESTSEQ);
    }

    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Sep 8, 2014
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }


    /**
     * Test method for {@link xal.smf.AcceleratorSeq#getNodeWithId(java.lang.String)}.
     */
    @Test
    public final void testGetNodeWithId() {
        String  strId1 = "SCL_Mag:QH04";
        String  strId2 = "SCL_Mag:QV04";
        
        AcceleratorNode     smfDesign1 = SEQ_DESIGN.getNodeWithId(strId1);
        AcceleratorNode     smfDesign2 = SEQ_DESIGN.getNodeWithId(strId2);
        
        System.out.println("Node 1 = " + smfDesign1);
        System.out.println("Node 2 = " + smfDesign2);
        System.out.println();
        
    }

    /**
     * Test method for {@link xal.smf.AcceleratorSeq#getNodesOfType(java.lang.String)}.
     */
    @Test
    public final void testCompareQuadrupoleDesignAndProd() {
        List<AcceleratorNode> lstNodesDesign = SEQ_DESIGN.getAllNodesOfType(Quadrupole.s_strType);
        List<AcceleratorNode> lstNodesProd = SEQ_PROD.getAllNodesOfType(Quadrupole.s_strType);
        
        System.out.println("QUADRUPOLE NODE COMPARISON - Design Versus Production");
        System.out.println("Design Sequence Node Count     = " + lstNodesDesign.size());
        System.out.println("Production Sequence Node Count = " + lstNodesProd.size());
        System.out.println();
        
        int cntNodes = lstNodesProd.size() < lstNodesDesign.size() ? lstNodesProd.size() : lstNodesDesign.size();
        
        for (int index=0; index<cntNodes; index++) {
            Quadrupole     smfDsgn = (Quadrupole) lstNodesDesign.get(index);
            Quadrupole     smfProd = (Quadrupole) lstNodesProd.get(index);
            
            System.out.println("  Design    : " + smfDsgn.getId() + " s=" + smfDsgn.getPosition() + ", B=" + smfDsgn.getDfltField());
            System.out.println("  Production: " + smfProd.getId() + " s=" + smfProd.getPosition() + ", B=" + smfProd.getDfltField());
            System.out.println();
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
        
        System.out.println("QUADRUPOLE NODE COMPARISON - Live Values Versus Production");
        System.out.println("Production Sequence Node Count = " + lstNodesProd.size());
        System.out.println();
        
        try {
        
            for (AcceleratorNode smfNode : lstNodesProd) {
                Quadrupole     smfProd = (Quadrupole) smfNode;

                System.out.println("Quadrupole " + smfProd.getId() + " s=" + smfProd.getPosition() );
                System.out.println("  Production : " + " B=" + smfProd.getDfltField());
                System.out.println("  Live Values: " + " B=" + smfProd.getFieldReadback());
                System.out.println();
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
        List<AcceleratorNode> lstNodesDesign = SEQ_DESIGN.getAllNodesOfType(SCLCavity.s_strType);
        List<AcceleratorNode> lstNodesProd = SEQ_PROD.getAllNodesOfType(SCLCavity.s_strType);
        
        System.out.println("SCL CAVITY NODE COMPARISON - Design Versus Production");
        System.out.println("Design Sequence Node Count     = " + lstNodesDesign.size());
        System.out.println("Production Sequence Node Count = " + lstNodesProd.size());
        System.out.println();
        
        int cntNodes = lstNodesProd.size() < lstNodesDesign.size() ? lstNodesProd.size() : lstNodesDesign.size();
        
        for (int index=0; index<cntNodes; index++) {
            SCLCavity     smfDsgn = (SCLCavity)lstNodesDesign.get(index);
            SCLCavity     smfProd = (SCLCavity)lstNodesProd.get(index);
            
            System.out.println("  Design    : " + smfDsgn.getId() + " s=" + smfDsgn.getPosition() + ", V=" + smfDsgn.getDfltCavAmp() + ", phi=" + smfDsgn.getDfltCavPhase() + ", mode=" + smfDsgn.getStructureMode() + ", TTF=" + smfDsgn.getStructureTTF());
            System.out.println("  Production: " + smfProd.getId() + " s=" + smfProd.getPosition() + ", V=" + smfProd.getDfltCavAmp() + ", phi=" + smfProd.getDfltCavPhase() + ", mode=" + smfProd.getStructureMode() + ", TTF=" + smfProd.getStructureTTF());
            System.out.println();
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
        
        System.out.println("SCL CAVITY NODE COMPARISON - Live Values Versus Production");
        System.out.println("Production Sequence Node Count = " + lstNodesProd.size());
        System.out.println();
        
        try {
        
            for (AcceleratorNode smfNode : lstNodesProd) {
                SCLCavity smfProd = (SCLCavity) smfNode;

                System.out.println("SCL Cavity " + smfProd.getId() + " s=" + smfProd.getPosition() );
                System.out.println("  Production : " + " Vdft=" + smfProd.getDfltCavAmp() + ", phi_dft=" + smfProd.getDfltCavPhase() + ", TTF=" + smfProd.getStructureTTF());
                System.out.println("  Live Values: " + " Vavg=" + smfProd.getCavAmpAvg() + ", phi_avg=" + smfProd.getCavPhaseAvg() + ", V*TTF=" + smfProd.getStructureTTF()*smfProd.getCavAmpAvg() + " Vset=" + smfProd.getCavAmpSetPoint());
                System.out.println();
            }

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link xal.smf.AcceleratorSeq#getNodesWithQualifier(xal.smf.impl.qualify.TypeQualifier)}.
     */
    @Test
    public final void testGetNodesWithQualifierTypeQualifier() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link xal.smf.AcceleratorSeq#getAllNodesOfType(java.lang.String)}.
     */
    @Test
    public final void testGetAllNodesOfType() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link xal.smf.AcceleratorSeq#getAllNodesWithQualifier(xal.smf.impl.qualify.TypeQualifier)}.
     */
    @Test
    public final void testGetAllNodesWithQualifier() {
        fail("Not yet implemented"); // TODO
    }
    

    /*
     * Support Methods
     */

}
