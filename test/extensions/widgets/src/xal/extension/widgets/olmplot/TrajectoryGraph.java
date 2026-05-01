/**
 * EnvelopeGraph.java
 *
 * @author Christopher K. Allen
 * @since  Nov 26, 2012
 *
 */

/**
 * EnvelopeGraph.java
 *
 * @author  Christopher K. Allen
 * @since	Nov 26, 2012
 */
package xal.extension.widgets.olmplot;


import java.awt.Dimension;

import xal.extension.widgets.olmplot.EnvelopeCurve;
import xal.extension.widgets.olmplot.PLANE;
import xal.extension.widgets.olmplot.ParticleCurve;
import xal.extension.widgets.olmplot.TrajectoryGraph;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.Trajectory;

/**
 * Represents a graph of an online model simulation solution.  The
 * graph will display the horizontal, vertical, and longitudinal RMS envelopes
 * (see <code>{@link EnvelopeCurve}</code>), particle positions 
 * (see <code>{@link ParticleCurve}</code>), or any other object derived from
 * <code>{@link BasicGraphData}</code>.
 *
 * @author Christopher K. Allen
 * @since   Nov 26, 2012
 */
public class TrajectoryGraph extends FunctionGraphsJPanel {

    
    
    /*
     * Global Constants
     */

    /** Serialization version number */
    private static final long serialVersionUID = 1L;

    
    //
    // Formatting
    //
    
    /** The dimensions of the solution graph */
    static private Dimension                    DIM_GRAPH_SOLN = new Dimension(600,450);
    
    
    /*
     * Global Methods
     */
    
    
    /**
     * Creates a new graph object of the particle trajectory contained in the given
     * <code>Trajectory</code> object.
     * 
     * @param trjPar    trajectory object containing particle coordinate positions as function of beamline position
     * 
     * @return          graph with curves showing particle positions in each phase plane
     * 
     * @throws IllegalArgumentException the argument is not a particle trajectory object
     *
     * @author Christopher K. Allen
     * @since  Nov 26, 2012
     */
    public static TrajectoryGraph createParticleGraph(Trajectory<ParticleProbeState> trjPar) throws IllegalArgumentException {
        TrajectoryGraph   graph = new TrajectoryGraph();
        graph.loadParticleCurves(trjPar);
        
        return graph;
    }
    
    /**
     * Creates a new graph object of the RMS envelope solution contained in the
     * given trajectory.
     *
     * @param trjEnv    trajectory object containing envelope solution
     *  
     * @return          graph with curves showing envelope trajectory for each phase plane
     *
     * @throws  IllegalArgumentException    the argument is not an envelope trajectory object
     * 
     * @author Christopher K. Allen
     * @since  Nov 26, 2012
     */
    public static TrajectoryGraph createEnvelopeGraph(Trajectory<EnvelopeProbeState> trjEnv) throws IllegalArgumentException {
        TrajectoryGraph   graph = new TrajectoryGraph();
        graph.loadEnvelopeCurves(trjEnv);
        
        return graph;
    }
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new, empty graph object of online model solutions.
     * 
     * @author  Christopher K. Allen
     * @since   Nov 26, 2012
     * 
     * @see ParticleCurve
     * @see EnvelopeCurve
     */
    private TrajectoryGraph() {
        super();
        this.setPreferredSize( DIM_GRAPH_SOLN );
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Adds a new curve to the trajectory graph for the give plane.  The curve type need only be
     * derived from <code>BasicGraphData</code> so this is a general object. 
     * 
     * @param plane     the curve will display under this phase plane characteristics
     * @param crvTrj    the curve being displayed
     *
     * @author Christopher K. Allen
     * @since  Apr 18, 2013
     */
    public void addGraphData(PLANE plane, BasicGraphData crvTrj) {

        // Get the legend key string
        String      strKeyLgnd = super.getLegendKeyString();
        
        crvTrj.setGraphProperty(strKeyLgnd, plane.toString());
        super.addGraphData(crvTrj);
    }
    
    /*
     * Support Methods
     */
    
    /**
     * Creates and loads a curve for each particle phase position trajectory.  The
     * given trajectory object provides the data for the curve.
     * 
     * @param trjPar    particle trajectory object containing the data for the curve
     * 
     * @throws IllegalArgumentException the given argument is not a particle trajectory object
     *
     * @author Christopher K. Allen
     * @since  Nov 26, 2012
     */
    private void    loadParticleCurves(Trajectory<ParticleProbeState> trjPar) throws IllegalArgumentException {
        
        // Create particle trajectory curves
        for (PLANE plane : PLANE.values()) {
            ParticleCurve   crvPar = new ParticleCurve(plane, trjPar);
            
            super.addGraphData(crvPar);
        }
    }
    
    /**
     * Creates and loads an envelope curve for each phase plane.  The curve
     * is constructed from the given trajectory data for each phase plane.
     *
     * @param trjEnv   object containing the data for the curve
     * 
     * @throws  IllegalArgumentException    the argument is not an envelope trajectory object
     * 
     * @author Christopher K. Allen
     * @since  Nov 26, 2012
     */
    private void    loadEnvelopeCurves(Trajectory<EnvelopeProbeState> trjEnv) throws IllegalArgumentException {

        // Get the legend key string
        String      strKeyLgnd = super.getLegendKeyString();
        
        // Create envelope trajectory curves
        for (PLANE plane : PLANE.values()) {
            EnvelopeCurve   crvEnv = new EnvelopeCurve(plane, trjEnv);
            
            crvEnv.setGraphProperty(strKeyLgnd, plane.toString());
            super.addGraphData(crvEnv);
        }
    }
    
    
}
