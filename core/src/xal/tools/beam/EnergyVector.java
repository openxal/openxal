/**
 * EnergyVector.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 12, 2015
 */
package xal.tools.beam;

import java.util.EnumSet;

import xal.tools.data.DataAdaptor;
import xal.tools.math.BaseVector;
import xal.tools.math.IIndex;

/**
 * <p>
 * Class encapsulating the synchronous phase coordinates.  These are the longitudinal coordinates
 * of acceleration, the phase &phi; of the synchronous particle w.r.t. to the cavity RF and
 * the energy <i>W</i> of the synchronous particle.
 * </p>
 * <p>
 * The position &phi; of the synchronous particle is the origin of the local coordinate frame that
 * follows a particle beam bunch.  The motions of beam particles are then w.r.t. the position and
 * energy of the synchronous particle, are are usually described using a <code>PhaseVector</code>
 * object.
 * </p>   
 * <p>
 * The synchronous phase coordinates are represented in homogeneous coordinates so that they may
 * be acted on by class <code>SyncMatrix</code> objects.  In this manner translations are position
 * using matrix multiplication.  For example, instantaneous energy gains and phase jumps, such as
 * those found in a thin lens model, can be represented by matrix multiplication. 
 * </p>
 * <p>
 * Currently this class has minimal functionality.  Additional capabilities should be added as
 * necessary.
 * </p>
 *
 *
 * @author Christopher K. Allen
 * @since  Oct 12, 2015
 */
public class EnergyVector extends BaseVector<EnergyVector> {

    
    /*
     * Internal Classes 
     */
    
    /**
     * Enumeration for the element position indices for homogeneous
     * phase space objects.  This set include the phase space coordinates
     * and the homogeneous coordinate.
     *
     * @author Christopher K. Allen
     * @since  Oct 8, 2013
     */
    public enum IND implements IIndex {

        /*
         * Enumeration Constants
         */
        
        /** Index of the synchronous coordinate */
        PHI(0),
        
        /** Index of the energy coordinate */
        W(1),
        
        /** Index of the homogeneous coordinate */
        HOM(6);
        
        
        /*
         * Global Constants
         */
        
        /** the set of IND constants that only include synchronous phase space variables (not the homogeneous coordinate) */
        private final static EnumSet<IND> SET_PHASE = EnumSet.of(PHI, W);

        
        /*
         * Global Operations
         */
        
        /**
         * Returns the set of index constants that correspond to phase
         * coordinates only.  The homogeneous coordinate index is not
         * included (i.e., the <code>IND.HOM</code> constant).
         * 
         * @return  the set of phase indices <code>IND</code> less the <code>HOM</code> constant
         *
         * @see xal.tools.math.BaseMatrix.IIndex#val()
         *
         * @author Christopher K. Allen
         * @since  Oct 15, 2013
         */
        public static EnumSet<IND>  valuesSync() {
            return SET_PHASE;
        }

        
        /*
         * IIndex Interface
         */
        
        /**
         * Returns the numerical index of this enumeration constant,
         * corresponding to the index into the phase vector.
         * 
         * @return  numerical value of this index
         *
         * @author Christopher K. Allen
         * @since  Sep 25, 2013
         */
        public int val() {
            return this.val;
        }
        
        /*
         * Initialization
         */
        
        /** The numerical value of this enumeration index */
        final public    int     val; 
        
        /**
         * Creates a new <code>IND</code> enumeration constant
         * initialized to the given index value.
         * 
         * @param index     numerical index value for this constant
         *
         * @author Christopher K. Allen
         * @since  Sep 25, 2013
         */
        private IND(int index) {
            this.val = index;
        }
    }
    
    
    
    /*
     * Global Constants
     */
    
    /** Java serialization version number  */
    private static final long serialVersionUID = 1L;

    /** Size of all <code>EnergyVector</code> objects  */
    private static final int SIZE_VECTOR = 3;

    
    
    /*
     * Initialization
     */
    
    /**
     * Zero argument constructor for <code>EnergyVector</code>.
     *
     * @since  Oct 12, 2015,   Christopher K. Allen
     */
    public EnergyVector() {
        super(SIZE_VECTOR);
        this.setElem(IND.HOM, 1.0);
    }
    
    /**
     * Initializing constructor for <code>EnergyVector</code>.  The initial phase &phi; and
     * energy <i>W</i> are given as arguments in the constructor.
     *
     * @param phi       initial phase &phi;     (in radians)
     * @param W         initial energy <i>W</i> (in electron-Volts)
     *
     * @since  Oct 13, 2015,   Christopher K. Allen
     */
    public EnergyVector(double phi, double W) {
        super(SIZE_VECTOR);
        double[] arrVecInit = { phi, W, 1.0 };
        this.setVector(arrVecInit);
    }

    /**
     * Cloning constructor for <code>EnergyVector</code>.
     *
     * @param vecParent     vector to be cloned and whose clone is then returned 
     * 
     * @since  Oct 12, 2015,   Christopher K. Allen
     */
    public EnergyVector(final EnergyVector vecParent) {
        super(vecParent);
    }

    /**
     * Initializing constructor for <code>EnergyVector</code>.  The initial values for the 
     * vector are contained in the given string.  The string should contain comma-separated
     * values with enclosing parenthesis, brackets, or curly braces.  For more information
     * see <code>{@link BaseVector#BaseVector(int,String)}</code>.
     *
     * @param strTokens     formatted character string containing initial values, e.g., "{ &phi; <i>W</i>, 1 }"
     *  
     * @throws IllegalArgumentException wrong number of token strings (make sure "1" is last value"
     * @throws NumberFormatException    bad number format, unreadable
     *
     * @since  Oct 12, 2015,   Christopher K. Allen
     * 
     * @see BaseVector#BaseVector(int,String)
     */
    public EnergyVector(String strTokens) throws IllegalArgumentException, NumberFormatException {
        super(SIZE_VECTOR, strTokens);
    }

    /**
     * Initializing constructor for class <code>EnergyVector</code>.  The initial values
     * for the created class are given by the double array of the argument.  The array must have
     * length 3 and have the last value as 1.
     *
     * @param arrVals       double array { &phi; <i>W</i>, 1 }
     * 
     * @throws ArrayIndexOutOfBoundsException   the argument must have the same dimensions as this vector
     *
     * @since  Oct 12, 2015,   Christopher K. Allen
     */
    public EnergyVector(final double[] arrVals) throws ArrayIndexOutOfBoundsException {
        super(arrVals);
    }

    /**
     * Initializing constructor for class <code>EnergyVector</code>.  Initial values are taken from 
     * the given data source with <code>DataAdaptor</code> interface.
     *
     * @param daSource      data source containing initial values
     *
     * @since  Oct 12, 2015,   Christopher K. Allen
     */
    public EnergyVector(DataAdaptor daSource) {
        super(SIZE_VECTOR, daSource);
    }

    
    /*
     * Setters and Getters
     */
    
    /**
     * Sets the synchronous phase component of the phase variables.
     * 
     * @param phi       synchronous phase w.r.t. to RF phase (radians)
     *
     * @since  Oct 12, 2015,   Christopher K. Allen
     */
    public void setPhase(double phi) {
        super.setElem(IND.PHI,  phi);
    }
    
    /**
     * Sets the kinetic energy component of the synchronous phase variables.
     * 
     * @param W     kinetic energy (electron-Volts)
     *
     * @since  Oct 12, 2015,   Christopher K. Allen
     */
    public void setEnergy(double W) {
        super.setElem(IND.W, W);
    }
    
    /**
     * Return the synchronous phase component of the synchronous phase vectors.
     * 
     * @return      synchronous phase w.r.t. to the RF phase (radians)
     *
     * @since  Oct 12, 2015,   Christopher K. Allen
     */
    public double   getPhase() {
        return super.getElem(IND.PHI);
    }
    
    /**
     * Returns the energy component of the synchronous phase coordinates.
     * 
     * @return  kinetic energy (electron-Volts)
     *
     * @since  Oct 12, 2015,   Christopher K. Allen
     */
    public double   getEnergy() {
        return super.getElem(IND.W);
    }
    
    
    /*
     * BaseVector Overrides
     */
    
    /**
     *
     * @see xal.tools.math.BaseVector#clone()
     *
     * @since  Oct 12, 2015,  Christopher K. Allen
     */
    @Override
    public EnergyVector clone() {
        EnergyVector    vec = new EnergyVector(this);
        
        return vec;
    }

    /**
     *
     * @see xal.tools.math.BaseVector#newInstance()
     *
     * @since  Oct 12, 2015,  Christopher K. Allen
     */
    @Override
    protected EnergyVector newInstance() {
        EnergyVector    vec = new EnergyVector();
        
        return vec;
    }

    /**
     *
     * @see xal.tools.math.BaseVector#newInstance(double[])
     *
     * @since  Jan 20, 2016,  Christopher K. Allen
     */
    @Override
    protected EnergyVector newInstance(double[] arrVecInit) {
        EnergyVector    vec = new EnergyVector(arrVecInit);
        
        return vec;
    }

}
