/*
 * ThinMatrix.java
 *
 * Created on October 17, 2002, 11:31 AM
 *
 */

package xal.model.elem;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;


/**
 *  User element represent a general beamline element.  Arbitrary beamline elements
 *  are specified by providing the energy gain and tranfer matrix a priori.  Note that
 *  for this element the transfer matrix and energy gain are independent of any probe
 *  objects.  Thus, this class should be used carefully.
 *  <p>
 *  Since this is a thin element there are no space charge kicks in the particle 
 *  dynamics.  If 
 *
 * @author  Christopher Allen
 */
public class ThinMatrix extends ThinElement {
    
    /*
     *  Global Attributes
     */
    
    /** string type identifier for all TranferMatrix objects */
    public static final String          s_strType = "ThinMatrix";
    
    
    
    /*
     *  Local Attributes
     */
    
    /** elapsed time for all probes to propagate this element */
    private double          m_dblDelT = 0.0;
     
    /** energy gain imparted to all probes */
    private double          m_dblDelW = 0.0;
    
    /** transfer map for all probes */
    private PhaseMap        m_mapPhi = PhaseMap.identity();
    
    
    
    /*
     *  Initialization
     */
    
    /**
     *  Creates a new instance of ThinMatrix
     *
     *  @param  strId       string identifier of this object
     *  @param  matPhi      7x7 transfer matrix of element in homogeneous coordinates
     *  @param  dblDelW     energy gain of element (<b>in electron-volts</b>)
     */
    public ThinMatrix(String strId, PhaseMatrix matPhi, double dblDelW)    {
        super(s_strType, strId);
        
        this.setTransferMatrix( matPhi );
        m_dblDelW = dblDelW;
    };
        
    /**
     *  Creates a new instance of TransferMatrix.
     *  The energy gain is initialized to zero.
     *
     *  @param  strId       string identifier of this object
     *  @param  matPhi      7x7 transfer matrix of element in homogeneous coordinates
     */
    public ThinMatrix(String strId, PhaseMatrix matPhi)    {
        this(strId, matPhi, 0.0);
    };
        
    /**
     *  Creates a new instance of TransferMatrix.
     *  The energy gain is initialized to zero.
     *  The transfer matrix is initialized to the 7x7 identity.
     *
     *  @param  strId       string identifier of this object
     */
    public ThinMatrix(String strId)    {
        this(strId, PhaseMatrix.identity(), 0.0);
    };
    
    /**
     *  JavaBean constructor - creates a new uninitialized instance of ThinMatrix
     *
     *  <b>BE CAREFUL</b>
     */
    public ThinMatrix()   {
        super(s_strType);
    };
    

    /** 
     * Set the elapsed time for <b>all</b> probes to propagate this element.
     * 
     * @param   dblDelT     elapsed time through element in <b>seconds</b>  
     */
    public void setElapsedTime(double dblDelT)  {
        this.m_dblDelT = dblDelT;
    }
    
    /**
     *  Set the energy gain imparted to all probes.
     *
     *  @param  dblDelW     energy gain (<b>in electron-volts</b>)
     */
    public void setEnergyGain(double dblDelW)   {
        this.m_dblDelW = dblDelW; 
    }
    
    /**
     *  Set the transfer matrix of the element for all probes.
     *
     *  @param  matPhi      7x7 transfer matrix in homogeneous phase space coordinates
     */
    public void setTransferMatrix(PhaseMatrix matPhi)    {
        this.m_mapPhi = new PhaseMap(matPhi);
    }
    
    
    /*
     *  IElement Interface
     */
    
    /**
     * Returns the time taken for the probe to propagate through element.
     * 
     *  @param  probe   propagating probe
     *  
     *  @return         the value zero 
     */
    @Override
    public double elapsedTime(IProbe probe)  {
        return this.m_dblDelT;
    }
    
    /**
     *  Returns the energy gain of this element, which is independent of all probe
     *  parameters.
     *
     *  @param  probe   dummy argument
     *
     *  @return         energy gain (<b>in electron-volts</b>)
     */
    @Override
    public double   energyGain(IProbe probe)    { 
        return this.m_dblDelW; 
    };
    
    /**  
     *  <p>
     *  Returns the transfer map of this element, which only has a linear component 
     *  corresponding to the transfer matrix, 
     *  </p><p>
     *  Note that since the transfer matrix is constant it is independent of the IProbe
     *  argument.
     *  </p>
     *
     *  @param  probe   dummy argument
     *
     *  @return         phase space transfer map
     *
     *  @throws ModelException    this should not occur
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {
        return this.m_mapPhi;
    }
    
    
}