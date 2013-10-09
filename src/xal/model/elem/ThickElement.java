/*
 * ThickElement.java
 *
 * Created on September 10, 2002, 2:47 PM
 *
 */

package xal.model.elem;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.tools.beam.PhaseMap;



/**
 * <p>
 *  Base class for all modeling elements having a finite length.  As such, space charge
 *  will affect probes propagation through these elements.
 * </p>
 * <p>
 *  The transfer matrix that is stored in the parent element is actually the incremental
 *  transfer matrix of the full element.  Thus, derived classes should implement the
 *  method subTransferMatrix() that returns the nSecs root of the full transfer 
 *  matrix.  Deriving beamline elements from this class allows space charge kicks to 
 *  be applied at nSecs equally spaced locations throughout the element.
 * </p>
 * 
 * @author  Christopher K. Allen
 */
public abstract class ThickElement extends Element {
    
    
    /*
     *  Local Attributes
     */
    
    /** total length of the element */
    private double      m_dblLen = 0.0;
        

 

    /*
     *  Initialization
     */
    
    /**
     * Default constructor to be used by automatic lattice generation.
     * Creates a new parameter-unitialized instance of ThickElement.
     *
     * @param   strType     the string type-identifier of the element type
     */
    public ThickElement(String strType)   {
        super(strType);
    }
        
    /**
     * Default constructor to be used by automatic lattice generation.
     * Creates a new instance of ThickElement specifying its type identifier
     * and its instance identifier.
     *
     * @param   strType     the string type-identifier of the element type
     * @param   strId       the string identifier of the element instance
     */
    public ThickElement(String strType, String strId)   {
        super(strType, strId);
    }
        
    /** 
     *  Creates a new instance of ThickElement
     *
     *  @param  strType     string type identifier for the element
     *  @param  strId       string instance identifier for this element
     *  @param  dblLen      total length of the element (<b>in meters</b>)
     */
    public ThickElement(String strType, String strId, double dblLen) { 
        super(strType, strId);        
        this.setLength(dblLen);
    };
    
    /**
     *  Set the length of the element.
     *
     *  @param  dblLen      lenght of element (in <b>meters</b>)
     *
     */
    public void setLength(double dblLen)    {
        this.m_dblLen = dblLen;
    };
    
    /*
     *  IElement Interface
     */

    /** 
     *  Return the total length of this element
     *
     *   @return    total element length (in <b>meters</b>)
     */
    @Override
    public double getLength() { return m_dblLen; };
    
    /**
     *  Return the energy gain of the beamline element over a subsection of the
     *  specified length.
     *
     *  @param  probe   probe for which energy gain is to be determined
     *  @param  dblLen   subsection length to calculate energyGain for
     *
     *  @return         the value #subEnergyGain(probe)*#getCount()
     */
    @Override
    public abstract double energyGain(IProbe probe, double dblLen);
    
    /**
     *  <p>
     *  Compute the transfer map for a subsection of this element whose length
     *  is dblLen.  If dblLen is greater than or equal to the element's length,
     *  return the transfer map for the full element.  Note that this may not be
     *  very useful for an element with differential acceleration.
     *
     *  @param  probe   probe supplying parameters for the transfer matrix calculation
     *  @param  dblLen  length of element subsection to compute transfer map for
     * 
     *  @return         the full tranfer map of this element
     *
     *  @exception  ModelException    exception occurred in subTransferMap() method
     */
    @Override
    public abstract PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException;
    
};
