/**
 * package-info.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 25, 2014
 */

/**
 * <p>
 * <h3>Introduction</h3>
 * This package contains classes that perform common processing tasks for simulation
 * data produced by the online model.  Such data is contained in a 
 * <code>{@link xal.model.probe.traj.Trajectory}</code> object where the template parameter indicates
 * the type of simulation that was run (i.e., single particle, transfer map calculation,
 * RMS envelope simulation, etc.).  The available processing options depend upon
 * the type of simulation data contained in a trajectory object.  For example, 
 * computation of Courant-Snyder parameters for a single particle simulation has
 * no meaning. Thus, the class that processes single particle simulation data,
 * <code>{@link CalculationsOnParticles}</code>, has no methods for such a calculation.
 * </p>
 * <p>
 * There are, however, methods with the same name in difference processing classes and
 * it is important to point out that the results of these computations <b>depend
 * upon context</b>.  For example, the method 
 * </code>{@link CalculationsOnRings#computeFixedOrbit(xal.model.probe.traj.TransferMapState)}</code> does not return
 * the same quantity as 
 * <code>{@link CalculationsOnParticles#computeFixedOrbit(xal.model.probe.traj.ParticleProbeState)}</code>.
 * In the former the expected quantity is computed, the position of the closed orbit at the
 * given state location.  In the later, the location of the particle at the state location
 * is returned.  This admittedly ambiguous situation is the result of a implementation
 * requirement that the various processing classes expose common interfaces for
 * providing computed parameters from the simulation data.  The consumers of this data
 * are assumed to know the context of the returned parameters based upon the 
 * simulation data they provide. 
 * </p>
 * <p>
 * <h3>Interfaces</h3>
 * There are two interfaces exposed by the processing classes in this package, both of
 * them contained in the parent interface <code>{@link ISimulationResults}</code>.  The
 * first is <code>{@link ISimLocResults<>}</code> which exposes methods involved in the 
 * computation of points or locations in phase space.  The second is  
 * <code>{@link ISimEnvResults<>}</code> which exposes methods that are concerned with
 * the computation of beam properties, such as Courant-Snyder parameters, betatron phase
 * of the envelope, and chromatic dispersion within the bunch.  See the  Javadoc for 
 * each interface for a more complete description of the expected quantities.
 * </p>
 * <p>
 * <h3>Calculation Engines</h3>
 * There are 4 classes that produce finished information from simulation data.  There is
 * an additional base class, <code>{@link CalculationEngine}</code>, which provides
 * common operations for the above concrete classes.  Each computational engine class
 * exposes at least one of the above interfaces, depending upon their function.  
 * A class may also provide additional methods which are appropriate to their context.
 * The computation classes and their function are listed below
 * <br>
 * <br>
 * <b><code>CalculationsOnParticles</code></b>: Provides processing functions appropriate 
 * for single particle simulation data.  Also has methods available for processing data
 * that was taken for a periodic system between two period locations.  The simulation data
 * is of type <code>Trajectory&lt;ParticleProbeState&gt;</code>.
 * <br>
 * <br>
 * <b><code>CalculationsOnMachines</code></b>: This class is concerned with the calculation of
 * machine properties without regard to any beam propagation.  The simulation data is
 * of type <code>Trajectory&lt;TransferMapState&gt;</code>. 
 * <br>
 * <br>
 * <b><code>CalculationsOnRings</code></b>: Computes ring properties and parameters.
 * This class is a super class of 
 * <code>CalculationsOnMachines</code>, since a ring is a machine.  Consequently
 * the simulation data is also of type <code>Trajectory&lt;TransferMapState&gt;</code>.
 * However, it is necessary that the simulation data be produced by a machine with
 * the topology of a ring for the calculations to have context.
 * <br>
 * <br>
 * <b><code>CalculationsOnBeams</code></b>: Computes parameters for a beam itself.
 * These include collective properties of a beam bunch.  The simulation data for
 * this class is of type <code>Trajectory&lt;EnvelopeProbeState&gt</code>.
 * <br>
 * <br>
 * Of the 4 classes only 2 operate on the same simulation data type, 
 * <code>CalculationsOnMachines</code> and <code>CalculationsOnRings</code>.
 * </p>
 * <p>
 * <h3>Convenience Classes</h3>
 * There are two additional convenience classes within the package, 
 * <code>SimResultsAdaptor</code> and <code>SimpleSimResultsAdaptor</code>.  The function
 * of <code>SimResultsAdaptor</code> is to expose both the above interfaces but where
 * the underlying computation engine used depends upon the type of simulation data provided.
 * After creating the class the developer specifies which computational engine is
 * to be used with which data.  The class <code>SimpleSimResultsAdaptor</code> defines
 * these computation engines <i>a priori</i>.  The <code>CalculationsOnRings</code>
 * class is used for simulation data of type <code>Trajectory&lt;TransferMapState&gt;</code>,
 * <code>CalculationsOnParticles</code> is used for type 
 * <code>Trajectory&lt;ParticleProbeState&gt</code>, and <code>CalculationsOnBeams</code> is
 * used for type <code>Trajectory&lt;EnvelopeProbeState&gt;</code>.
 * </p>
 *
 * @author  Christopher K. Allen
 * @since   Sep 25, 2014
 * @version Sep 25, 2014
 */

package xal.tools.beam.calc;
