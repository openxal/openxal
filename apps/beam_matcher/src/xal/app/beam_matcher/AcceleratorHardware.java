
/**
 * AcceleratorHardware.java
 *
 * @author  Christopher K. Allen
 * @since	Jun 22, 2011
 */
package xal.app.beam_matcher;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.ProfileMonitor;
import xal.smf.impl.Quadrupole;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * Tests retrieval of hardware objects from the default accelerator
 * object.
 * </p>
 * <p>
 * <h4>NOTES</h4>
 * &middot; Look in the file <tt>gov.sns.xal.smf.impl.Quadrupole</tt> to find the
 * "<i>type codes</i>" for the horizontal and vertical quadrupoles.
 * <br/>
 * &middot; Other type codes can be found by looking in the source file for the
 * device which you want to use.  For example, look in file
 * <tt>gov.sns.xal.smf.impl.ProfileMonitor</tt> to find the type codes for
 * wire scanners and harps.
 * </p>
 *
 * @author Matthew Reat
 * @author Frank Cui
 * @author Eric Dai
 * @author Christopher K. Allen
 *
 * @since   Jun 22, 2011
 */
public class AcceleratorHardware {
    
    
    /*
     * Global Constants
     */
    
    /** Target accelerator sequence */
    public static final String STR_ID = "HEBT1";
    
    
    /** Type code for horizontal quadrupoles */
    private static final String STR_HOR_QUAD = Quadrupole.HORIZONTAL_TYPE;
    
    /** Type code for vertical quadrupoles */
    private static final String STR_VER_QUAD = Quadrupole.VERTICAL_TYPE;
    
    /** Type code for all quadrupoles */
    private static final String STR_ALL_QUAD = Quadrupole.QUAD;
    
    /** Type code for wire scanners */
    private static final String STR_DIAG_WS = ProfileMonitor.s_strType;
    
    
    
    
    
    /**
     * <p>
     * Application entry point.
     * </p>
     * <p>
     * Fetches all the horizontal quadrupoles from the
     * target sequence.
     * </p>
     *
     * @param args      <em>not used</em>
     * @throws ModelException
     * @throws ConnectionException
     *
     * @since  Jun 22, 2011
     *
     */
    
    
    
    /*
     * Local Attributes
     */
    /** The accelerator object */
    Accelerator     accl;
    
    
    public AcceleratorHardware() {
        GenDocument main = new GenDocument();
        accl = main.getAccelerator();
    }
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new <code>AcceleratorHardware</code> object that
     * use the default accelerator for obtaining hardware objects.
     *
     * @throws  RuntimeException    the default accelerator was not defined or did not load
     *
     * @author  Christopher K. Allen
     * @since   Jun 22, 2011
     */
    
    
    /**
     * Creates a new <code>AcceleratorHardware</code> object that uses
     * the accelerator defined in the file at the given file path.
     *
     * @param strPath               path of file defining accelerator
     *
     * @throws FilePathException    the file path is not valid
     *
     * @author  Christopher K. Allen
     * @since   Jun 22, 2011
     */
    
    
    
    /*
     * Operations
     */
    
    /**
     * Returns all the horizontal quadrupoles in the given accelerator
     * sequence.
     *
     * @param   strSeqId    string identifier of the accelerator sequence
     *
     * @return              list of all horizontal quadrupoles in sequence
     *
     * @since  Jun 22, 2011
     */
    public List<AcceleratorNode> getHorQuadrupoles(String strSeqId) {
        
        AcceleratorSeq seq = this.accl.getSequence(strSeqId);
        
        List<AcceleratorNode> lstQuads = seq.getNodesOfType(STR_HOR_QUAD);
        
        return lstQuads;
    }
    
    
    /**
     * Returns all the VERTICAL quadrupoles in the given accelerator
     * sequence.
     *
     * @param   strSeqId    string identifier of the accelerator sequence
     *
     * @return              list of all horizontal quadrupoles in sequence
     *
     * @since  Jun 22, 2011
     */
    public List<AcceleratorNode> getVerQuadrupoles(String strSeqId) {
        
        AcceleratorSeq seq = this.accl.getSequence(strSeqId);
        
        List<AcceleratorNode> lstQuads = seq.getNodesOfType(STR_VER_QUAD);
        
        return lstQuads;
    }
    
    /**
     * Returns all the quadrupoles in the given accelerator
     * sequence.
     *
     * @param   strSeqId    string identifier of the accelerator sequence
     *
     * @return              list of all horizontal quadrupoles in sequence
     *
     * @since  Jun 22, 2011
     */
    public List<AcceleratorNode> getAllQuadrupoles(String strSeqId) {
        
        AcceleratorSeq seq = this.accl.getSequence(strSeqId);
        
        List<AcceleratorNode> lstQuads = seq.getNodesOfType(STR_ALL_QUAD);
        
        return lstQuads;
    }
    
    /**
     * Returns all the wire scanners in the given accelerator
     * sequence.
     *
     * @param   strSeqId    string identifier of the accelerator sequence
     *
     * @return              list of all wire scanners in sequence
     *
     * @since  Jun 25, 2011
     */
    
    public List<AcceleratorNode> getWireScanners(String strSeqId) {
        
        AcceleratorSeq seq = this.accl.getSequence(strSeqId);
        
        List<AcceleratorNode> lstWireScan = seq.getNodesOfType(STR_DIAG_WS);
        
        return lstWireScan;
    }
    
    
    
    /**
     *
     *
     * @param 	WS_ID the wire scanner name which you want to get
     * @return returns the wire scanner as an Accelerator node... or at least it should
     * @author Matthew Reat
     */
    public AcceleratorNode getWireScannerByName(String WS_ID) {
        
        AcceleratorSeq seq = this.accl.getSequence("HEBT1");
        
        AcceleratorNode wireScanByName = seq.getNodeWithId(WS_ID);
        
        return wireScanByName;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Returns all wire scanner data from WS01 gotten previously from the accelerator in a consistent format.
     *
     * @return              ArrayList<Double> of data from the wire scanners formatted (horiz,vert,diag)
     *
     * @since  Jun 28, 2011
     * @author Frank Cui
     * @author Matthew Reat
     */
    
    public ArrayList<Double> organizeWireScan01() {
        
        WireScanData wsData = new WireScanData();
        ArrayList<Double> wireScan01 = new ArrayList<Double>();
        try {
            ArrayList<Double> lstOfHoriz = wsData.wireScanHoriz();
            ArrayList<Double> lstOfVert = wsData.wireScanVert();
            ArrayList<Double> lstOfDiag = wsData.wireScanDiag();
            
            
            double horizontalWireScan01 = lstOfHoriz.remove(0);
            double verticalWireScan01 = lstOfVert.remove(0);
            double diagonalWireScan01 = lstOfDiag.remove(0);
            
            wireScan01.add(horizontalWireScan01);
            wireScan01.add(verticalWireScan01);
            wireScan01.add(diagonalWireScan01);
            
            return wireScan01;
            
        }
        catch (ConnectionException e) {
            
            e.printStackTrace();
            
        } catch (GetException e) {
            
            e.printStackTrace();
            
        }
        
        return wireScan01;
        
    }
    
    /**
     * Returns all wire scanner data from WS02 gotten previously from the accelerator in a consistent format.
     *
     * @return              ArrayList<Double> of data from the wire scanners formatted (horiz,vert,diag)
     *
     * @since  Jun 28, 2011
     * @author Frank Cui
     * @author Matthew Reat
     */
    public ArrayList<Double> organizeWireScan02() {
        
        WireScanData wsData = new WireScanData();
        ArrayList<Double> wireScan02 = new ArrayList<Double>();
        try {
            ArrayList<Double> lstOfHoriz = wsData.wireScanHoriz();
            ArrayList<Double> lstOfVert = wsData.wireScanVert();
            ArrayList<Double> lstOfDiag = wsData.wireScanDiag();
            
            double horizontalWireScan02 = lstOfHoriz.remove(1);
            double verticalWireScan02 = lstOfVert.remove(1);
            double diagonalWireScan02 = lstOfDiag.remove(1);
            
            wireScan02.add(horizontalWireScan02);
            wireScan02.add(verticalWireScan02);
            wireScan02.add(diagonalWireScan02);
            
            return wireScan02;
            
        }
        catch (ConnectionException e) {
            
            e.printStackTrace();
            
        } catch (GetException e) {
            
            e.printStackTrace();
            
        }
        
        return wireScan02;
    }
    
    /**
     * Returns all wire scanner data from WS03 gotten previously from the accelerator in a consistent format.
     *
     * @return              ArrayList<Double> of data from the wire scanners formatted (horiz,vert,diag)
     *
     * @since  Jun 28, 2011
     * @author Frank Cui
     * @author Matthew Reat
     */
    public ArrayList<Double> organizeWireScan03() {
        
        WireScanData wsData = new WireScanData();
        ArrayList<Double> wireScan03 = new ArrayList<Double>();
        try {
            ArrayList<Double> lstOfHoriz = wsData.wireScanHoriz();
            ArrayList<Double> lstOfVert = wsData.wireScanVert();
            ArrayList<Double> lstOfDiag = wsData.wireScanDiag();
            
            double horizontalWireScan03 = lstOfHoriz.remove(2);
            double verticalWireScan03 = lstOfVert.remove(2);
            double diagonalWireScan03 = lstOfDiag.remove(2);
            
            wireScan03.add(horizontalWireScan03);
            wireScan03.add(verticalWireScan03);
            wireScan03.add(diagonalWireScan03);
            
            return wireScan03;
            
        }
        catch (ConnectionException e) {
            
            e.printStackTrace();
            
        } catch (GetException e) {
            
            e.printStackTrace();
            
        }
        
        return wireScan03;
        
    }
    
    
    /**
     * Returns all wire scanner data from WS04 gotten previously from the accelerator in a consistent format.
     *
     * @return              ArrayList<Double> of data from the wire scanners formatted (horiz,vert,diag)
     *
     * @since  Jun 28, 2011
     * @author Frank Cui
     * @author Matthew Reat
     */
    public ArrayList<Double> organizeWireScan04() {
        
        
        WireScanData wsData = new WireScanData();
        ArrayList<Double> wireScan04 = new ArrayList<Double>();
        try {
            ArrayList<Double> lstOfHoriz = wsData.wireScanHoriz();
            ArrayList<Double> lstOfVert = wsData.wireScanVert();
            ArrayList<Double> lstOfDiag = wsData.wireScanDiag();
            
            double horizontalWireScan04 = lstOfHoriz.remove(4);
            double verticalWireScan04 = lstOfVert.remove(4);
            double diagonalWireScan04 = lstOfDiag.remove(4);
            
            wireScan04.add(horizontalWireScan04);
            wireScan04.add(verticalWireScan04);
            wireScan04.add(diagonalWireScan04);
            
            
            return wireScan04;
            
        }
        catch (ConnectionException e) {
            
            e.printStackTrace();
            
        } catch (GetException e) {
            
            e.printStackTrace();
            
        }
        
        return wireScan04;
        
    }
}