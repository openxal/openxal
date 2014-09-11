/**
 * EnvelopeCurve.java
 *
 * @author  Christopher K. Allen
 * @since	Nov 26, 2012
 */
package xal.extension.widgets.olmplot;

import java.util.Iterator;

import xal.extension.widgets.olmplot.PLANE;
import xal.extension.widgets.plot.BasicGraphData;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;


/**
 * Graphics object representing a curve in a <code>{@FunctionGraphsJPanel}</code> object.
 * The curve is a <code>{@link BasicGraphData}</code> object that is an RMS envelope trajectory
 * produced by an online model simulation.
 *
 * @author Christopher K. Allen
 * @since   Nov 26, 2012
 */
public class EnvelopeCurve extends BasicGraphData {

    
    /*
     * Local Attributes
     */
    
    
    /** The phase plane of this curve */
    private final PLANE     plane;
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates an empty data object representing the RMS beam envelope for the 
     * given phase plane.  The curve object must be subsequently defined using the
     * method {@link #loadCurve(Trajectory)}</code>.
     * 
     * @param plane     phase plane of the rms envelope curve
     *
     * @author  Christopher K. Allen
     * @since   Nov 26, 2012
     */
    public  EnvelopeCurve(PLANE plane) {
        super();
        this.plane = plane;

        this.setDrawLinesOn(true);
        this.setDrawPointsOn(false);
        this.setGraphColor(plane.getColor());
        this.setGraphName(plane.toString());
    }
    
    /**
     * Creates the basic data object representing a curve on a graph.  The curve
     * is constructed from the given trajectory data for the given phase plane.
     *
     * @param plane     phase plane of the rms envelope curve
     * @param trjEnv    object containing the data for the curve
     *
     * @throws IllegalArgumentException the argument <var>trjEnv</var> is not an envelope 
     *                                  trajectory object
     * 
     * @author  Christopher K. Allen
     * @since   Nov 26, 2012
     */
    public EnvelopeCurve(PLANE plane, Trajectory<EnvelopeProbeState> trjEnv) throws IllegalArgumentException {
        this(plane);

        this.loadCurve(trjEnv);
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Defines the graphics curve according to the RMS envelope
     * trajectory data in the given <code>Trajectory</code> object.
     *
     * @param trjEnv    online model simulation trajectory containing curve data
     *
     * @throws IllegalArgumentException the argument is not an envelope trajectory object
     *  
     * @author Christopher K. Allen
     * @since  Nov 26, 2012
     */
    public  void    loadCurve(Trajectory<EnvelopeProbeState> trjEnv) throws IllegalArgumentException {

        // Load the trajectory data into the graphs
        Iterator<EnvelopeProbeState>   iterState = trjEnv.stateIterator();
        while (iterState.hasNext()) {
            Object             objEnv = iterState.next();
            
            if (! (objEnv instanceof EnvelopeProbeState) )
                throw new IllegalArgumentException("Trajectory object is not an Envelope trajectory");
            
            EnvelopeProbeState stateEnv = (EnvelopeProbeState)objEnv;
            
            double  dblPos = stateEnv.getPosition();
            double  dblSig = this.plane.getRmsEnvelope(stateEnv);

            this.addPoint(dblPos, dblSig);
        }
    }
}
