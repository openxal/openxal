/**
 * Tools.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jul 10, 2015
 */
package xal.test;

import java.io.File;

import xal.model.probe.traj.TestParticleProbeTrajectory;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;
import xal.tools.ResourceManager;

/**
 * Class <code>Tools</code>.  A utility class providing some tools for common operations
 * used in JUnit test cases.
 * 
 *
 * @author Christopher K. Allen
 * @since  Jul 10, 2015
 */
public abstract class Tools {

    
    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_DSGN = "/site/optics/design/main.xal";

    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_PROD = "/site/optics/production/main.xal";
    
    
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
    private static Accelerator loadAccelerator(String ...arrPathRel) {
        if (arrPathRel.length == 0)
            return XMLDataManager.loadDefaultAccelerator();
        String  strPathRel = arrPathRel[0];
        String  strPathXal = ResourceManager.getProjectHomePath();
        String  strFileAccel = strPathXal + strPathRel;
        
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
        String  strPack     = TestParticleProbeTrajectory.class.getPackage().getName();
        String  strPathRel  = strPack.replace('.', '/');
        String  strPathFile = strPathRel + '/' + strFileName; 
        File    fileOutput  = xal.test.ResourceManager.getOutputFile(strPathFile);
        
        return fileOutput;
    }
    
    
    
    /**
     * Constructor for Tools.
     *
     *
     * @since  Jul 10, 2015   by Christopher K. Allen
     */
    public Tools() {
        // TODO Auto-generated constructor stub
    }

}
