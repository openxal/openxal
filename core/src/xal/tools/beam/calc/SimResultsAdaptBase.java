/**
 * SimResultsAdaptBase.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 15, 2013
 */
package xal.tools.beam.calc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import xal.model.probe.traj.ProbeState;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Nov 15, 2013
 */
public class SimResultsAdaptBase<I , S extends ProbeState> {

    
    /*
     * Local Attributes
     */
    
    /** map of probe state types to ISimLocationResults calculation engine types */
    private final Map<Class<S>, I>   mapArgToCalc;
    
//    /** map of probe state types to ISimLocationResults calculation engine types */
//    private final Map<Class<? extends ProbeState>, ISimEnvelopeResults<? extends ProbeState>>   mapArgToEnvCalc;
    
    
    /*
     * Initialization
     */
    
    /**
     * Constructor for SimResultsAdaptBase.
     *
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2013
     */
    public SimResultsAdaptBase() {
        this.mapArgToCalc = new HashMap<Class<S>, I>();
    }

    /**
     * Register the location calculation engine for the given probe data type.
     * 
     * @param clsType       class type of the simulation trajectory states
     * 
     * @param engResults    the computation engine for processing the data
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2013
     */
    public void   registerLocEngine(Class<S> clsType, I engResults) {
        this.mapArgToCalc.put(clsType, engResults);
    }
    
    
    /*
     * Support Methods
     */

    /**
     * Determines the sub-type of the <code>ProbeState</code> object
     * and uses that information to determine which <code>ISimEnvelopeResults</code>
     * computation engine is used to compute the machine parameters.  (That is,
     * are we looking at a ring or at a Linac.)  The result is passed back up to the
     * <code>ISimEnvelopeResults</code> interface exposed by this class (i.e.,
     * the interface method that invoked this method) where its type is identified
     * and returned to the user of this class.
     * 
     * @param strMthName    name of the method in the <code>ISimEnvelopeResults</code> interface
     * @param staArg        <code>ProbeState</code> derived object that is an argument to one of the
     *                      methods in the <code>ISimEnvelopeResults</code> interface
     *                      
     * @return              result of invoking the given <code>ISimEnvelopeResults</code> method on 
     *                      the given <code>ProbeState</code> argument
     *  
     * @author Christopher K. Allen
     * @since  Nov 8, 2013
     */
    protected Object  compute(String strMthName, ProbeState staArg) {

        try {

            Class<? extends ProbeState> clsType = staArg.getClass();

            I   engResult = this.mapArgToCalc.get(clsType);

            if (engResult == null) {

                throw new IllegalArgumentException("Unknown probe state type " + staArg.getClass().getName());
            }

            Method mthResult = engResult.getClass().getDeclaredMethod(strMthName, clsType);
            Object objResult = mthResult.invoke(engResult, staArg);

            return objResult;


        } catch (ClassCastException | 
                NoSuchMethodException | 
                SecurityException | 
                IllegalAccessException | 
                IllegalArgumentException | 
                InvocationTargetException e
                ) {

            throw new IllegalArgumentException("Included exception thrown invoking method " + strMthName, e);
        }
    }
}
