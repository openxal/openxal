/**
 * MeasurementCurve.java
 *
 * @author Christopher K. Allen
 * @since  Apr 29, 2013
 */
package xal.extension.twissobserver;

import xal.extension.widgets.plot.BasicGraphData;

import xal.extension.widgets.olmplot.PLANE;
import xal.extension.widgets.olmplot.TrajectoryGraph;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;

import java.util.ArrayList;

/**
 * This class represents a curve within a graph 
 * (see <code>{@link FunctionGraphsJPanel}</code> and
 * child class <code>{@link TrajectoryGraph}</code>) containing
 * measurement data.  The curve is composed of unconnected data points for the
 * beam size measurements of a particular phase plane.
 *
 * @author Christopher K. Allen
 * @since   Nov 27, 2012
 */
public class MeasurementCurve extends BasicGraphData {
    
    /*
     * Local Attributes
     */
    
    /** The phase plane of this curve */
    private final PLANE             plane;
    
    /** The accelerator sequence where the data was taken */
    private final AcceleratorSeq    smfSeq;
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new, initialized curve object containing the given measurement data as
     * points.  The data for the given phase plane is extracted from the data set.  The
     * <code>AcceleratorSeq</code> object must be the location within the accelerator where
     * the data was taken.
     *  
     * @param plane         curve is built using data for this phase plane
     * @param smfSeqSrc     accelerator location where data was taken 
     * @param arrMsmt       measurement data
     *
     * @author  Christopher K. Allen
     * @since   Nov 27, 2012
     */
    public MeasurementCurve(PLANE plane, AcceleratorSeq smfSeqSrc, ArrayList<Measurement> arrMsmt) {
        super();
        
        this.plane  = plane;
        this.smfSeq = smfSeqSrc;
        
        this.setDrawLinesOn(false);
        this.setGraphColor(plane.getColor());
        this.setGraphName(plane.toString());
        this.setGraphPointSize(10);
        
        this.loadMeasurements(arrMsmt);
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Loads the data points from the given measurement data into this
     * curve object.
     *
     * @param arrMsmt       data structure containing the measurement data
     *
     * @author Christopher K. Allen
     * @since  Nov 27, 2012
     */
    private void    loadMeasurements(ArrayList<Measurement> arrMsmt) {
        
        for (Measurement msmt : arrMsmt) {
            AcceleratorNode     smfDev = this.smfSeq.getNodeWithId(msmt.strDevId);
            double              dblPos = smfDev.getPosition();
            double              dblSig = this.getMeasurement(msmt);
            
            this.addPoint(dblPos, dblSig);
        }
    }
    
    /**
     * Returns the correct beam size measurement from the measurement record 
     * for the given phase plane. 
     *
     * @param plane     phase plane of the desired measurement
     * @param msmt      the data record of beam size measurements      
     * 
     * @return          beam size measurement from the provided data structure in the given phase plane
     *
     * @author Christopher K. Allen
     * @since  Nov 27, 2012
     */
    private double  getMeasurement(Measurement msmt) {
        
        switch (this.plane) {
        
        case HOR: return msmt.dblSigHor;
        case VER: return msmt.dblSigVer;
        case LNG: return msmt.dblSigLng;
        default:  return 0.0;
        }
    }
}