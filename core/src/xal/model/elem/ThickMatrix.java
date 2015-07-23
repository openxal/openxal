/*
 * ThickMatrix.java
 *
 *
 * Created on October 18, 2002, 3:46 PM
 *
 * Modified:
 *      02/13/03 CKA    - refactored to new model architecture
 *
 */

package xal.model.elem;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;




/**
 * <p>
 * User element representing a general beamline element.  Arbitrary beamline 
 * elements are specified by providing the transfer matrix <b>generator</b>, 
 * elapsed time, and energy gain a priori.  Note that for this element the 
 * transfer matrix, elapsed time, and energy gain are independent of any probe
 * objects.  Note also that the generator for the transfer matrix is specified,
 * NOT the actual transfer matrix.  Thus, this class should be used carefully.
 * </p>
 * <p>
 * This element is derived from the ThickElement base so that space charge 
 * kicks may be applied throughout the element.  
 * </p>
 * <p>
 * Denoting the generator matrix as <b>A</b>, then the transfer matrix 
 * <b>M</b>(<i>s</i>) for a section of length <i>s</i> is given by
 * <br>
 * <br>
 * &nbsp; &nbsp; <b>M</b>(<i>s</i>) = <i>e</i><sup><i>s</i><b>A</b></sup>
 * <br>
 * <br>
 * where <i>e</i><sup><b>A</b></sup> is the matrix exponential.  
 * </p>
 * <h3>NOTE:</h3>
 * <p>
 * Currently the class implements the matrix exponential only to 
 * second order.  Therefore 
 * <br>
 * <br>
 * &nbsp; &nbsp;  <b>M</b>(<i>s</i>) = <b>I</b> + <i>s</i><b>A</b>  
 *                                     + &frac12;<i>s</i><sup>2</sup><b>A</b><sup>2</sup> 
 *                                     ( + <i>O</i>(<i>s</i><sup>3</sup>) )  
 * <br>
 * <br>
 * </p>
 * 
 *
 * @author  Christopher K. Allen
 */
public class ThickMatrix extends ThickElement {

    
    /*
     *  Global Attributes
     */
    
    /** string type identifier for all ThickMatrix objects */
    public static final String          s_strType = "ThickMatrix";
    
    
    
    /*
     *  Local Attributes
     */
    
    /** elapsed time for all probes to propagate this element */
    private double          m_dblDelT = 0.0;
     
    /** the energy gain imparted to all probes */
    private double          m_dblDelW = 0.0;
    
    /** element transfer matrix generator for all probes */
    private PhaseMatrix        m_matGen = PhaseMatrix.zero();
    

    
    /*
     *  Initialization
     */
    
    /** 
     *  Creates a new instance of ThickMatrix 
     *
     *  @param  strId       identifier of this ThickMatrix object
     *  @param  dblLen      length of the element (<b>in meters</b>)
     *  @param  matPhiSub   7x7 transfer matrix for a subelement
     *  @param  dblDelW     energy gain imparted of this element (<b>in electron-volts</b>)
     */
    public ThickMatrix(String strId, double dblLen, PhaseMatrix matPhiSub, double dblDelW)    {
        super(s_strType, strId, dblLen);
        
        this.setEnergyGain(dblDelW);
        this.setTransferMapGenerator(matPhiSub);
    }
    
    /** 
     *  Creates a new instance of ThickMatrix.
     *  Energy gain for each subelement is initialized to zero.
     *
     *  @param  strId       identifier of this ThickMatrix object
     *  @param  dblLen      length of the element (<b>in meters</b>)
     *  @param  matPhiSub   7x7 transfer matrix for a subelement
     */
    public ThickMatrix(String strId, double dblLen, PhaseMatrix matPhiSub)    {
        this(strId, dblLen, matPhiSub, 0.0);
    }
    
    /** 
     *  Creates a new instance of ThickMatrix.
     *  The sub-element energy gain is initialized to zero.
     *  The sub-element transfer matrix is initialized to the 7x7 identity.
     *
     *  @param  strId       identifier of this ThickMatrix object
     *  @param  dblLen      length of the element (<b>in meters</b>)
     */
    public ThickMatrix(String strId, double dblLen)    {
        this(strId, dblLen, PhaseMatrix.identity(), 0.0);
    }
    
    /** 
     *  JavaBean constructor - creates a new uninitialized instance of ThickMatrix
     *
     *  <b>BE CAREFUL</b>
     */
    public ThickMatrix() 
    {
        super(s_strType);
    }
    
    
    /** 
     * Set the total elapsed time for all probes to propagate the entire
     * element.
     * 
     * @param   dblDelT     elapsed time through element in <b>seconds</b>  
     */
    public void setElapsedTime(double dblDelT)  {
        this.m_dblDelT = dblDelT;
    }
    
    /**
     *  Set the total energy gain imparted to any probe propagating through
     *  entire element.
     *
     *  @param  dblDelW  energy gain imparted to all probes (<b>in electron-volts</b>)
     */
    public void setEnergyGain(double dblDelW) {
        this.m_dblDelW = dblDelW;
    }
    
    /**
     * Set the transfer map generator <b>A</b> for the element.  
     * The transfer map <b>M</b>(s) over a distance <i>s</i> is then given by
     * 
     *      M(s) = Exp(s*A)
     *
     * where Exp() is the matrix exponential function.  Thus, the map M 
     * generated by A is also a matrix.
     * 
     * 
     *  @param  matGen   transfer matrix generator (probe independent)
     */
    public void setTransferMapGenerator(PhaseMatrix matGen)  {
        this.m_matGen = matGen;
    }
    
    
    /*
     *  ThickElement Abstract Methods
     */
   
    /**
     * Returns the time taken for any probe to drift through part of the
     * element.  The value dT(dblLen) returned by this method is given by
     * 
     *      dT(dblLen) = dblLen/getLength() * dblDelT
     * 
     * where dblDelT is the value given to <code>#setElapsedTime</code>.
     * 
     *  @param  probe   dummy argument
     *  @param  dblLen  length of subsection to propagate through <b>meters</b>
     *  
     *  @return         the elapsed time through section<b>Units: seconds</b> 
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen)  {
        return (dblLen/this.getLength()) * this.m_dblDelT;
    }
    
    /**
     *  Returns the energy gain imparted to any probe when going through part
     * of the element.  The value dW(dblLen) returned by this method is given by
     * 
     *      DW(dblLen) = dblLen/getLength() * dblDelW
     * 
     * where dblDelW is the value given to <code>#setEnergyGain</code>.
     *
     *  @param  dblLen  dummy argument
     *  @param  probe   dummy argument
     *
     *  @return         energy gain for each subelement (<b>in electron-volts</b>)
     */
    @Override
    public double energyGain(IProbe probe, double dblLen) {
        return (dblLen/this.getLength())* this.m_dblDelW; 
    }
    
    /**  
     * Returns the transfer map produced by the generator matrix over the 
     * distance <code>dblLen</code>. 
     * 
     * NOTE:
     * Currently the transfer map returned is accurate only to order two.
     * That is the matrix exponential function is approximated by its first
     * three terms.
     *
     * @param  dblLen  propagation length in <b>meters</b>
     * @param  probe   dummy argument
     *
     * @return         computed transfer map 
     *
     * @exception  ModelException    this should not occur
     * 
     * @see #setTransferMapGenerator
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException {
        double          s = dblLen;
        PhaseMatrix     A = this.m_matGen;
        PhaseMatrix     M = PhaseMatrix.identity();
        
        M.plusEquals( A.times(s) );
        M.plusEquals( A.times(A.times(0.5*s*s)) ); 
        
        return new PhaseMap( M );
    }

}