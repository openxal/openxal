package xal.model;

import xal.tools.data.IArchive;
import xal.tools.data.IContextAware;



/** 
 * <h1>The defining interface of all probe tracking algorithms</h1>
 * 
 * <p>
 * According to the Element-Algorithm-Probe analysis pattern, 
 * algorithm objects describe interactions between accelerator structures
 * (Element's) and physical objects (Probe's). 
 * Thus, tracking algorithm objects are responsible for propagating probe 
 * objects, represented by the <code>IProbe</code> interface, through 
 * element objects, represented by the <code>IElement</code> interface.
 * <p>  
 * 
 * @author  Nikolay Malitsky, Christopher K. Allen
 * @version $Id: Algorithm.java,v 2.0
 * 
 * @see     xal.model.IElement
 * @see     xal.model.IProbe
 */
public interface IAlgorithm extends java.rmi.Remote, IContextAware, IArchive {


    /*
     * Identification
     */

    /**
     * Returns a string type identifier of algorithm class
     * 
     * @return  <code>String</code> algorithm type identifier  
     */
    public String getType();
    
    /**
     * Returns the version number of this algorithm
     * 
     * @return  <code>int</code> version number of the algorithm
     */
    public int getVersion();



    /*
     * Propagation
     */

    /**
     * Check if probe can be handled by this algorithm.
     * 
     * @param   probe    probe to be tested
     * 
     * @return  <b>true</b> if algorithm can propagation probe, <b>false</b> otherwise
     * 
     */
    public boolean validProbe(IProbe probe);
	
	
    /**
	 * Get the modeling element string identifier where propagation is to start.
     * 
     * @return  string id if element is defined, null otherwise
     */
    public String getStartElementId();

    
    /**
     * Set the id of the element from which to start propagation.
     * 
     * @param id <code>String</code> id of the element from which to start propagation
     */
    public void setStartElementId(String id);
    
	
    /**
     * reset Start point to the beginning of the sequence
     */
    public void unsetStartElementId();
	
	
    /**
	 * Get the modeling element string identifier where propagation is to stop.
     * 
     * @return  string id if element is defined, null otherwise
     */
    public String getStopElementId();

    
    /**
     * Set the id of the element at which to stop propagation.
     * 
     * @param id <code>String</code> id of the element at which to stop propagation
     */
    public void setStopElementId(String id);
    
    /**
     * Sets the flag that determines whether or not the
     * propagation stops at the entrance of the stop element (if set),
     * or at the exit of the stop node.  The later case is the default.
     *  
     * @param bolInclStopElem    propagation stops after stop element if <code>true</code>,
     *                           before the stop element if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Oct 20, 2014
     */
    public void setIncludeStopElement(boolean bolInclStopElem);
    
	
    /**
     * reset Stop point to the end of the sequence
     */
    public void unsetStopElementId();
    
	
    /** 
     * indicates whether to calculate the beam phase in multi gap Rf cavities,
     * (a la Parmila) rather than use default values (a  la Trace   3D)
     * @author jdg 3/20/03
     */
    public boolean getRfGapPhaseCalculation();
   
    /** 
     * indicates whether to calculate the beam phase in multi gap Rf cavities,
     * (a la Parmila) rather than use default values (a  la Trace   3D)
     * @author jdg 3/20/03
     */
    public void setRfGapPhaseCalculation(boolean tf);     

    /**
     *  Initialize the algorithm for propagation.
     *      Reset any necessary state variables
     *
     *  @exception  ModelException  unable to initialize algorithm
     */
    public void initialize() throws ModelException;
    
	
    /** 
     * Propagates the probe through the element.
     * 
     * @param   probe   target object
     * @param   elem    beamline element acting on the probe 
     */
    public void propagate(IProbe probe, IElement elem) throws ModelException;
    
    
    public IAlgorithm copy();
    

    /*
     * Interfaces
     */    
    
//    /**
//     * Return the <code>IArchive</code> interface for archiving
//     * this algorithm object.
//     * 
//     * @author Christopher Allen    11/17/03
//     */
//     public IArchive getArchive();
 
};
