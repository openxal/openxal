/**
 * Tools.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jul 10, 2015
 */
package xal.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

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
public abstract class ResourceTools {
    
    
    /*
     * Internal Classes
     */

    /**
     * Enumerations of the acceleration configurations available for instantiation.
     *
     *
     * @author Christopher K. Allen
     * @since  Jul 13, 2015
     */
    public enum ACCEL {
        
        /** The default accelerator defined by the current system environment */
        DFLT(null),
    
        /** The accelerator configuration file in core/test branch */
        TEST(STR_CFGFILE_TEST),
        
        /** The design accelerator configuration file in the local branch */
        DSGN(STR_CFGFILE_DSGN),
        
        /** The production accelerator configuration file in the local branch */
        PROD(STR_CFGFILE_PROD);
        
        /** 
         * Returns the acceleration configuration file name for the current
         * enumeration.
         * 
         * @return      configuration file name w.r.t. this class location 
         * 
         */
        public String   getFileName() {
            return this.strFileName;
        }
        
        
        /*
         * Local Attributes
         */
        
        /** acceleration configuration file name for this constant */
        final private String    strFileName;
        
        /*
         * Initialization
         */
        
        /**
         * Construct this constant with given configuration file.
         * 
         * @param   strFileName     accelerator configuration file
         */
        private ACCEL(String strFileName) {
            this.strFileName = strFileName;
        }
    }

    
    /*
     * Global Constants
     */
    
    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_TEST = "/core/test/resources/config/main.xal";
   
    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_DSGN = "/site/optics/design/main.xal";

    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_PROD = "/site/optics/production/main.xal";
    
    
    /*
     * Global Methods
     */
    
    /**
     * Loads an SMF accelerator object corresponding to the given enumeration
     * constant.  The paths to the configuration files are defined relative to the
     * OPENXAL_HOME directory by the global string constants defined in 
     * <code>{@link ACCEL}</code>. 
     * 
     * @param enmAccel  the accelerator configuration file constant
     * 
     * @return          SMF accelerator object loaded from the given path
     *
     * @author Christopher K. Allen
     * @since  Sep 8, 2014
     */
    public static Accelerator loadAccelerator(ACCEL enmAccel) {
        if (enmAccel == ACCEL.DFLT)
            return XMLDataManager.loadDefaultAccelerator();
        
        String  strPathRel = enmAccel.getFileName();
        String  strPathXal = ResourceManager.getProjectHomePath();
        String  strFileAccel = strPathXal + strPathRel;
        
        Accelerator accel = XMLDataManager.acceleratorWithPath(strFileAccel);
        return accel;
    }
    
    /**
     * Creates a new output file in the testing output directory with the 
     * given file name and the relative path given by the class location.
     * 
     * @param clsRelPath    class with package specifying relative path
     * @param strFileName   name of the output file
     * 
     * @return              new output file object
     *
     * @author Christopher K. Allen
     * @since  Sep 11, 2014
     */
    public static File createOutputFile(Class<?> clsRelPath, String strFileName) {
        String  strPack     = clsRelPath.getPackage().getName();
        String  strPathRel  = strPack.replace('.', '/');
        String  strPathFile = strPathRel + '/' + strFileName; 
        File    fileOutput  = xal.test.ResourceManager.getOutputFile(strPathFile);
        
        return fileOutput;
    }
    
    /**
     * Creates a new output file in the testing output directory with the given
     * class used for both the file path location (class package) and file
     * name (class name).
     *  
     * @param clsAbsPath    class whose package and name specify the file    
     * 
     * @return              an output file with location specified by class
     *
     * @since  Jul 13, 2015   by Christopher K. Allen
     */
    public static File createOutputFile(Class<?> clsAbsPath) {
        String  strFileName = clsAbsPath.getName() + ".txt";
        
        return createOutputFile(clsAbsPath, strFileName);
    }
    
    /**
     * Returns an output stream connected to a file in the testing output
     * directory.  The file has the given file name and the relative path
     * is determined by the package of the given class.
     * 
     * @param clsRelPath    class with package specifying relative path
     * @param strFileName   name of the file connected to the output stream
     * 
     * @return              output stream connect to file specified by path and name,
     *                      <code>null</code> if the path/name combination is invalid
     *
     * @since  Jul 13, 2015   by Christopher K. Allen
     */
    public static PrintStream   createOutputStream(Class<?> clsRelPath, String strFileName)  {
        try {
            File        fileOutput = createOutputFile(clsRelPath, strFileName);
            PrintStream ostrOutput = new PrintStream(fileOutput);

            return ostrOutput;
            
        } catch (FileNotFoundException e) {
            System.err.println("Unable to create output file " + strFileName + " for stream");
            e.printStackTrace();
            
            return null;
        }
    }
    
    /**
     * Returns an output stream connected to a file in the testing output
     * directory.  The file has the name given by the name of the given class
     * (post-fixed with <tt>".txt"</tt>) and the path given by the package 
     * location of the given class.
     * 
     * @param clsAbsPath    class supplying name and location of connected file
     *  
     * @return              output stream connected to file specified by given class
     *
     * @since  Jul 13, 2015   by Christopher K. Allen
     */
    public static PrintStream   createOutputStream(Class<?> clsAbsPath) {
        String      strFileName = clsAbsPath.getName() + ".txt";
        
        return createOutputStream(clsAbsPath, strFileName);
    }
    
    
    
    /*
     * Support Methods
     */
    
    /**
     * Private any instances from being constructed.
     *
     *
     * @since  Jul 10, 2015   by Christopher K. Allen
     */
    private ResourceTools() {
    }

}
