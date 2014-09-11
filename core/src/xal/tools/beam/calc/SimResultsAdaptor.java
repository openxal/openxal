/**
 * SimResultsAdaptor.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 15, 2013
 */
package xal.tools.beam.calc;

import java.util.HashMap;
import java.util.Map;

import xal.model.probe.traj.ProbeState;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.calc.ISimulationResults.ISimEnvResults;
import xal.tools.beam.calc.ISimulationResults.ISimLocResults;
import xal.tools.math.r3.R3;

/**
 * <p>
 * This class allows the developer to dynamically choose which calculation engine
 * applies to which simulation results type.  This is done by first registering
 * the calculation engine (i.e., <code>CalculationsOnBeams</code>, 
 * <code>CalculationsOnRings</code>, etc.) using the method 
 * <code>{@link #registerCalcEngine(Class, ISimulationResults)}</code> while providing
 * the type of <code>ProbeState</code> derived simulation data and the previously
 * instantiated calculation engine.
 * </p>
 * <p>
 * Currently, either or both of the interfaces<code>ISimulationResults.ISimLocResults</code> and 
 * <code>ISimulationResults.ISimEnvResults</code> are recognized by the adaptor and the
 * adaptor exposes both interfaces itself.  When registering the adaptor checks if 
 * the calculation engine exposes each of the above interfaces then registers it as
 * a provider of that calculation output.  Any calls to 
 * <code>{@link #registerCalcEngine(Class, ISimulationResults)}</code> supersedes any previous 
 * registrations.
 * </p>
 * <p>
 * When an exposed method of either of the supported interfaces is called upon this
 * adaptor, it first looks at the type of <code>ProbeState</code> it was given
 * then retrieves the calculation engine registered to that type.  It then 
 * delegates the method call to that simulation engine and returns the result.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Nov 15, 2013
 */
public class SimResultsAdaptor implements ISimLocResults<ProbeState<?>>, ISimEnvResults<ProbeState<?>> {

    
    /*
     * Local Attributes
     */
    
    /** map of probe state types to ISimLocResults calculation engine types */
    private final Map<Class<? extends ProbeState<?>>, ISimLocResults<ProbeState<?>>>   mapArgToLocCalc;
    
    /** map of probe state types to ISimEnvResults calculation engine types */
    private final Map<Class<? extends ProbeState<?>>, ISimEnvResults<ProbeState<?>>>   mapArgToEnvCalc;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Constructor for SimResultsAdaptor.
     *
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2013
     */
    public SimResultsAdaptor() {
        this.mapArgToLocCalc = new HashMap<Class<? extends ProbeState<?>>, ISimLocResults<ProbeState<?>>>();
        this.mapArgToEnvCalc = new HashMap<Class<? extends ProbeState<?>>, ISimEnvResults<ProbeState<?>>>();
    }

    /**
     * Register the location calculation engine for the given probe data type.
     * 
     * @param clsType       class type of the simulation trajectory states
     * 
     * @param iCalcEngine   interface of computation engine for processing the data
     * 
     * @throws  IllegalArgumentException    unknown calculation engine type
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2013
     */
    public <I extends ISimulationResults> void   registerCalcEngine(Class<? extends ProbeState<?>> clsType, I iCalcEngine)
            throws IllegalArgumentException
    {
        boolean     bolRegistered = false;
        
        if (iCalcEngine instanceof ISimLocResults<?>) {
            @SuppressWarnings("unchecked")
            ISimLocResults<ProbeState<?>> calcLocEngine = (ISimLocResults<ProbeState<?>>)iCalcEngine;

            this.mapArgToLocCalc.put(clsType, calcLocEngine);
            bolRegistered = true;
        }  
        
        if (iCalcEngine instanceof ISimEnvResults<?>) {
            @SuppressWarnings("unchecked")
            ISimEnvResults<ProbeState<?>> calcEnvEngine = (ISimEnvResults<ProbeState<?>>)iCalcEngine;

            this.mapArgToEnvCalc.put(clsType, calcEnvEngine);
            bolRegistered = true;
        }

        if (!bolRegistered)
            throw new IllegalArgumentException("Unknown calculation engine: " + iCalcEngine.getClass().getName());
    }

    

    
    /*
     * ISimLocResults Interface
     */
    
    /**
     *
     * @see xal.tools.beam.calc.ISimulationResults.ISimLocResults#computeCoordinatePosition(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2013
     */
    @Override
    public PhaseVector computeCoordinatePosition(ProbeState<?> state) throws IllegalArgumentException {
        
        ISimLocResults<ProbeState<?>>    iCalcEngine = this.retrieveLocCalcEngine(state);

        return iCalcEngine.computeCoordinatePosition(state);
    }

    /**
     *
     * @see xal.tools.beam.calc.ISimulationResults.ISimLocResults#computeFixedOrbit(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2013
     */
    @Override
    public PhaseVector computeFixedOrbit(ProbeState<?> state) {
        
        ISimLocResults<ProbeState<?>>    iCalcEngine = this.retrieveLocCalcEngine(state);

        return iCalcEngine.computeFixedOrbit(state);
    }

    /**
     *
     * @see xal.tools.beam.calc.ISimulationResults.ISimLocResults#computeChromAberration(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2013
     */
    @Override
    public PhaseVector computeChromAberration(ProbeState<?> state) {
        
        ISimLocResults<ProbeState<?>>    iCalcEngine = this.retrieveLocCalcEngine(state);

        return iCalcEngine.computeChromAberration(state);
    }


    /*
     * ISimEnvResults Interface
     */
    
    /**
     *
     * @see xal.tools.beam.calc.ISimulationResults.ISimEnvResults#computeTwissParameters(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2013
     */
    @Override
    public Twiss[] computeTwissParameters(ProbeState<?> state) {
        
        ISimEnvResults<ProbeState<?>>  iCalcEngine = this.retrieveEnvCalcEngine(state);
        
        return iCalcEngine.computeTwissParameters(state);
    }

    /**
     *
     * @see xal.tools.beam.calc.ISimulationResults.ISimEnvResults#computeBetatronPhase(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2013
     */
    @Override
    public R3 computeBetatronPhase(ProbeState<?> state) {
        
        ISimEnvResults<ProbeState<?>>  iCalcEngine = this.retrieveEnvCalcEngine(state);
        
        return iCalcEngine.computeBetatronPhase(state);
    }

    /**
     *
     * @see xal.tools.beam.calc.ISimulationResults.ISimEnvResults#computeChromDispersion(xal.model.probe.traj.ProbeState)
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2013
     */
    @Override
    public PhaseVector computeChromDispersion(ProbeState<?> state) {
        
        ISimEnvResults<ProbeState<?>>  iCalcEngine = this.retrieveEnvCalcEngine(state);
        
        return iCalcEngine.computeChromDispersion(state);
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Searches the dictionary of (probe state type, calculation engine) pairs for the
     * data processing engine that was registered for the given probe state.
     * Uses the sub-type of the <code>ProbeState</code> object
     * and to determine which <code>ISimLocResults</code>
     * computation engine is used to compute the machine parameters.  (For example,
     * are we looking at a ring or at a linac?)  The result is passed back up to the
     * <code>ISimLocResults</code> interface exposed by this class (i.e.,
     * the interface method that invoked this method) where its type is identified
     * and returned to the user of this class.
     * 
     * @param state     probe state that is to be processed
     * 
     * @return          the calculation engine that will process the given probe state
     * 
     * @throws IllegalArgumentException     there was no calculation engine registered for the given probe
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2013
     */
    private ISimLocResults<ProbeState<?>> retrieveLocCalcEngine(ProbeState<? >state) throws IllegalArgumentException {
        
        ISimLocResults<ProbeState<?>>    iCalcEngine = this.mapArgToLocCalc.get(state.getClass());
        if (iCalcEngine == null)
            throw new IllegalArgumentException("No simulation data calculation engine for probe state type " + state.getClass());
        
        return iCalcEngine;
    }

    /**
     * Searches the dictionary of (probe state type, calculation engine) pairs for the
     * data processing engine that was registered for the given probe state.
     * Uses the sub-type of the <code>ProbeState</code> object
     * and to determine which <code>ISimEnvResults</code>
     * computation engine is used to compute the machine parameters.  (For example,
     * are we looking at a ring or at a linac?)  The result is passed back up to the
     * <code>ISimEnvResults</code> interface exposed by this class (i.e.,
     * the interface method that invoked this method) where its type is identified
     * and returned to the user of this class.
     * 
     * @param state     probe state that is to be processed
     * 
     * @return          the calculation engine that will process the given probe state
     * 
     * @throws IllegalArgumentException     there was no calculation engine registered for the given probe
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2013
     */
    private ISimEnvResults<ProbeState<?>> retrieveEnvCalcEngine(ProbeState<?> state) throws IllegalArgumentException {
        
        ISimEnvResults<ProbeState<?>>    iCalcEngine = this.mapArgToEnvCalc.get(state.getClass());
        if (iCalcEngine == null)
            throw new IllegalArgumentException("No simulation data calculation engine for probe state type " + state.getClass());
        
        return iCalcEngine;
    }

//    /**
//     * Determines the sub-type of the <code>ProbeState</code> object
//     * and uses that information to determine which <code>ISimEnvResults</code>
//     * computation engine is used to compute the machine parameters.  (That is,
//     * are we looking at a ring or at a Linac.)  The result is passed back up to the
//     * <code>ISimEnvResults</code> interface exposed by this class (i.e.,
//     * the interface method that invoked this method) where its type is identified
//     * and returned to the user of this class.
//     * 
//     * @param strMthName    name of the method in the <code>ISimEnvResults</code> interface
//     * @param staArg        <code>ProbeState</code> derived object that is an argument to one of the
//     *                      methods in the <code>ISimEnvResults</code> interface
//     *                      
//     * @return              result of invoking the given <code>ISimEnvResults</code> method on 
//     *                      the given <code>ProbeState</code> argument
//     *  
//     * @author Christopher K. Allen
//     * @since  Nov 8, 2013
//     */
//    private <I extends ISimulationResults> Object  compute(I iCalcEngine, String strMthName, ProbeState staArg) {
//    
//        try {
//    
//            Class<? extends ProbeState> clsType = staArg.getClass();
//    
//            Method mthResult = iCalcEngine.getClass().getDeclaredMethod(strMthName, clsType);
//            Object objResult = mthResult.invoke(iCalcEngine, staArg);
//    
//            return objResult;
//    
//    
//        } catch (ClassCastException | 
//                NoSuchMethodException | 
//                SecurityException | 
//                IllegalAccessException | 
//                IllegalArgumentException | 
//                InvocationTargetException e
//                ) {
//    
//            throw new IllegalArgumentException("Included exception thrown invoking method " + strMthName, e);
//        }
//    }
//
}
