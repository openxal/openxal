/**
 * MultiGraphDisplayPanel.java
 *
 *  Created	: Jul 16, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.plt;

import xal.app.pta.MainApplication;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.tools.logging.IEventLogger;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.plot.GridLimits;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ProfileDevice.ANGLE;
import xal.smf.impl.profile.ProfileDevice.IProfileData;
import xal.smf.impl.profile.ProfileDevice.IProfileDomain;
import xal.smf.impl.profile.Signal;
import xal.smf.impl.profile.SignalSet;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Base class for classes that Display beam profile data. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Mar 29, 2009
 * @author Christopher K. Allen
 */
abstract public class GraphDisplayBase extends JPanel {

    
    /*
     * Global Constants
     */
    
    /**  Serialization version ID */
    private static final long serialVersionUID = 1L;

    
    

    /*
     * Abstract Methods
     */
    
    /**
     * Adds the given curve to the plot.  The curve's data source
     * is specified by the argument <var>enmAng</var>.
     *
     * @param enmAng    projection angle of the curve data  
     * @param datCrv    the curve data
     * 
     * @since  Feb 18, 2010
     * @author Christopher K. Allen
     */
    abstract public void displayCurve(ANGLE enmAng, BasicGraphData datCrv);
    
    
    
    /**
     * Returns the list of graph objects that display the data 
     * from the given projection angle.
     *
     * @param angPlt    projection angle of the profile data
     *  
     * @return          list of all graph objects that corresponding to the given projection angle
     * 
     * @since  Mar 29, 2010
     * @author Christopher K. Allen
     */
    abstract protected List<FunctionGraphsJPanel>    getDataGraphs(WireScanner.ANGLE angPlt);
    
    
    /**
     * Returns the set of all graph objects used in the
     * display.
     *
     * @return  list of each graph object used in the profile display
     * 
     * @since  Mar 29, 2010
     * @author Christopher K. Allen
     */
    abstract protected List<FunctionGraphsJPanel>     getAllDataGraphs();
    

    
    
    /*
     * Local Attributes
     */
    
    /** color of the plots */
    private Color               clrPlt;
    
    
    /** Legend key for each graph */
    private final Map<ANGLE,String>      mapLgdKey;
    
    /** current drawing color for each projection curve */
    private final Map<ANGLE,Color>       mapCrvClr;
    
    /** name of current curve */
    private final Map<ANGLE,String>      mapCrvLbl;
    
    /** display curve data points toggle flag */
    private final Map<ANGLE,Boolean>     mapCrvPts;
    
    /** curve thickness */
    private final Map<ANGLE,Integer>     mapCrvSz;
    
    /** vertical line event action listeners */
    private final Map<ANGLE, ActionListener>    mapVerLnActs;
    
    /** horizontal line event action listeners */
    private final Map<ANGLE, ActionListener>    mapHorLnActs;

    
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>MultiGraphDisplayPanel</code> object.
     *
     * @since     Jul 16, 2009
     * @author    Christopher K. Allen
     */
    protected GraphDisplayBase() {
       
        this.mapLgdKey = new HashMap<ANGLE, String>();
        this.mapCrvClr = new HashMap<ANGLE, Color>();
        this.mapCrvPts = new HashMap<ANGLE, Boolean>();
        this.mapCrvLbl = new HashMap<ANGLE, String>();
        this.mapCrvSz  = new HashMap<ANGLE, Integer>();
        this.mapVerLnActs = new HashMap<>();
        this.mapHorLnActs = new HashMap<>();
        
        this.configure();
    }

    
    

    /*
     * Operations
     */
    
    
    
    /**
     * Refreshes only the plots that contain curves for the given
     * projection data.
     *
     * @param enmAng    projection data curves that will be refreshed
     * 
     * @since  Feb 18, 2010
     * @author Christopher K. Allen
     */
    public void refreshPlot(ANGLE enmAng) {
        for (FunctionGraphsJPanel plt : this.getDataGraphs(enmAng))
            plt.refreshGraphJPanel();
    }
    
    /**
     * Forces a redraw of all the profile data
     * plots.  For example, if data was changed
     * internally.
     *
     * 
     * @since  Feb 8, 2010
     * @author Christopher K. Allen
     */
    public void refreshPlots() {
        for (FunctionGraphsJPanel pnlGraph : this.getAllDataGraphs()) 
            pnlGraph.refreshGraphJPanel();
    }
    
    /**
     * Clears only the plots that contain curves for 
     * the given profile data. 
     *
     * @param enmAng    projection data curves that will be cleared
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     */
    public void clear(ANGLE enmAng) {
        for (FunctionGraphsJPanel pnlGraph : this.getDataGraphs(enmAng)) { 
            pnlGraph.removeAllGraphData();
            pnlGraph.removeVerticalValues();
            pnlGraph.removeHorizontalValues();
        }
    }

    /**
     * Clear all profile data from graphs 
     *
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     */
    public void clear() {
        for (FunctionGraphsJPanel pnlGraph : this.getAllDataGraphs()) {
            
            pnlGraph.removeAllGraphData();
            pnlGraph.removeVerticalValues();
            pnlGraph.removeHorizontalValues();
        }
    }

    
    
    /**
     * Turns on auto-scaling for the <i>x</i> axis of each plot. 
     *
     * 
     * @since  Feb 4, 2010
     * @author Christopher K. Allen
     */
    public void autoScaleAbscissas() {
        for (FunctionGraphsJPanel pnlGraph : this.getAllDataGraphs()) {
            GridLimits  glmPlt = pnlGraph.getCurrentGL();

            glmPlt.setSmartLimitsX();
        }
    }
    
//    /**
//     * Sets the plot limits to the size
//     * necessary to display the data for each of the given
//     * devices.  Specifically, the <i>x</i> axis is just large enough
//     * so that all profile data can be displayed.
//     *
//     * @param lstDevs   we are configuring the plots to display data for 
//     *                  this devices  
//     * 
//     * @since  Feb 4, 2010
//     * @author Christopher K. Allen
//     */
//    public void scaleAbsissa(List<WireScanner> lstDevs) {
//
//        if (lstDevs.size() == 0)
//            return;
//        
//        // Initialize the minimum and maximum values
//        double  dblMinPos = Double.MAX_VALUE;
//        double  dblMaxPos = Double.MIN_VALUE;
//
//        // Find the minimum and maximum axis values 
//        for (AcceleratorNode devActive : lstDevs) {
//            if ( !(devActive instanceof WireScanner) )
//                continue;
//            
//            WireScanner ws = (WireScanner)devActive;
//            
//            try {
//                // Get the scan configuration for this device and look at scan range
//                WireScanner.ScanConfig  cfgScan = WireScanner.ScanConfig.acquire(ws);
//                
//                if (cfgScan.posInit < dblMinPos)
//                    dblMinPos = cfgScan.posInit;
//                
//                if (cfgScan.lngScan > dblMaxPos) 
//                    dblMaxPos = cfgScan.lngScan;
//                
//            } catch (ConnectionException e) {
//                this.getLogger().logException(this.getClass(), e, "DevId = " + ws.getId()); //$NON-NLS-1$
//                continue;
//                
//            } catch (GetException e) {
//                this.getLogger().logException(this.getClass(), e, "DevId = " + ws.getId()); //$NON-NLS-1$
//                continue;
//                
//            }
//        }
//
//        // We must scale the minimum value by 1/Sqrt(2) 
//        //  for horizontal and vertical wires
//        double dblMinPosTrn = dblMinPos/Math.sqrt(2.0);
//        double dblMaxPosTrn = dblMaxPos/Math.sqrt(2.0);
//        
//        // Set the x-axis properties
//        for (FunctionGraphsJPanel pnlGraph : this.getAllDataGraphs()) { 
//            
//            GridLimits  glmPlt = pnlGraph.getCurrentGL();
//
//            // Look for the diagonal wire - it has no scaling factor
//            if (pnlGraph.getName().equals(WireScanner.ANGLE.DIA.getLabel())) {
//                glmPlt.setXmin(dblMinPos);
//                glmPlt.setXmax(dblMaxPos);
//                
//            } else {
//                glmPlt.setXmin(dblMinPosTrn);
//                glmPlt.setXmax(dblMaxPosTrn);
//                
//            }
//            
//            pnlGraph.setExternalGL(glmPlt);
//        }
//    }
//    
    /**
     * Sets the plot limits to the size
     * necessary to display the data for each of the given
     * devices.  Specifically, the <i>x</i> axis is just large enough
     * so that all profile data can be displayed.
     *
     * @param lstDomains   we are configuring the plots to display data for 
     *                  this devices  
     * 
     * @since  Feb 4, 2010
     * @version April 24, 2014
     * @author Christopher K. Allen
     */
    public void scaleAbsissa(List<IProfileDomain> lstDomains) {

        if (lstDomains.size() == 0)
            return;

        for (ANGLE angle : ANGLE.values()) {
            // Initialize the minimum and maximum values
            double  dblMinPos = Double.MAX_VALUE;
            double  dblMaxPos = Double.MIN_VALUE;

            // Find the minimum and maximum axis values 
            for (IProfileDomain domain : lstDomains) {
                double  dblPtLt = domain.getInitialPosition(angle);
                double  dblPtRt = dblPtLt + domain.getIntervalLength(angle);

                if (dblPtLt < dblMinPos)
                    dblMinPos = dblPtLt;

                if (dblPtRt > dblMaxPos)
                    dblMaxPos = dblPtRt;
            }

            // Set the x-axis limits of the current projection plane graphs
            for (FunctionGraphsJPanel pnlGraph : this.getDataGraphs(angle)) { 

                GridLimits  glmPlt = pnlGraph.getCurrentGL();

                glmPlt.setXmin(dblMinPos);
                glmPlt.setXmax(dblMaxPos);

                pnlGraph.setExternalGL(glmPlt);
            }
        }
    }
    
    /**
     * Sets the visibility of the graph legends.
     *
     * @param bolVisible  makes the legend visible if <tt>true</tt>
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public void setLegendVisible(boolean bolVisible) {
        
        for (FunctionGraphsJPanel pnlGraph : this.getAllDataGraphs()) { 
            pnlGraph.setLegendVisible(bolVisible);        
        }
    }
    
    /**
     * Sets the legend key to be the given value for all
     * graphs.  The legend key is the key into the properties
     * map for each curve on the associated graph.
     *
     * @param strKey    new legend key
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     * 
     * @see BasicGraphData#getGraphProperty(Object)
     */
    public void setLegendKey(String strKey) {

        for (FunctionGraphsJPanel pnlGraph : this.getAllDataGraphs())  
            pnlGraph.setLegendKeyString(strKey);        
        
        for (ANGLE angle : ANGLE.values()) 
            this.mapLgdKey.put(angle, strKey);
    }
    
    /**
     * Set the legend key for the specific graph.  
     * The legend key is the key into the properties
     * map for each curve on the associated graph.
     *
     * @param angle     projection angle specifying graph
     * @param strKey    new legend key
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     * 
     * @see BasicGraphData#getGraphProperty(Object)
     */
    public void setLegendKey(WireScanner.ANGLE angle, String strKey) {
        for (FunctionGraphsJPanel pnlGraph : this.getDataGraphs(angle))
            pnlGraph.setLegendKeyString(strKey);
        
        this.mapLgdKey.put(angle, strKey);
    }
    
    /**
     * Sets the legend key label for the next curve to be drawn.  This is
     * done for every graph in the display panel regardless of 
     * projection angle.
     *
     * @param strLabel    string used to describe the next curve in the
     *                  graph legend
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public void setCurveLabel(String strLabel) {

        for (ANGLE angle : ANGLE.values()) 
            this.mapCrvLbl.put(angle, strLabel);
    }

    /**
     * Sets the legend key label for the next curve to be drawn.  This is
     * done for every graph in the display panel corresponding to 
     * the given projection angle.
     *
     * @param angle     selects graphs for this projection angle
     * @param strLabel  string used to describe the next curve in the
     *                  graph legend
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public void setCurveLabel(WireScanner.ANGLE angle, String strLabel) {

        this.mapCrvLbl.put(angle, strLabel);
    }
    
    /**
     * Sets the color for the next curve to be drawn.  This is
     * done for every graph in the display panel regardless of 
     * projection angle.
     *
     * @param color     color used for the next curve (also in the
     *                  graph legend)
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public void setCurveColor(Color color) {

        for (ANGLE angle : ANGLE.values())
            this.mapCrvClr.put(angle, color);
    }

    /**
     * Sets the color for the next curve to be drawn.  This is
     * done for every graph in the display panel corresponding to 
     * the given projection angle.
     *
     * @param angle     selects graphs for this projection angle
     * @param color     color used for the next curve (also in the
     *                  graph legend)
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public void setCurveColor(WireScanner.ANGLE angle, Color color) {
        this.mapCrvClr.put(angle, color);
    }
    
    /**
     * Toggle the explicit drawing of data points on the next curve to be
     * drawn.
     *
     * @param bolDrawPoints data points are drawn if <code>true</code>,
     *                      a smooth curve if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Apr 18, 2012
     */
    public void setCurvePoints(boolean bolDrawPoints) {
        for (ANGLE angle : ANGLE.values() )
            this.mapCrvPts.put(angle, bolDrawPoints);
    }
    
    /**
     * Sets the thickness of the next curve to be drawn.
     *
     * @param szThickness   thickness in points
     *
     * @author Christopher K. Allen
     * @since  Apr 18, 2012
     */
    public void setCurveThickness(int szThickness) {
        for (ANGLE angle : ANGLE.values() )
            this.mapCrvSz.put(angle, szThickness);
    }
    
    /**
     * Get the number of (user) vertical lines being managed in the
     * given graph.
     *
     * @param angGraph  projection angle associated with desired graph
     * @param indGraph  index in list of all graphs with above projection angle 
     *                  (if there are multiple one)
     *                  
     * @return          number of (user) vertical lines in the given graph
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2011
     */
    public int getVerticalLineCount(WireScanner.ANGLE angGraph, int indGraph) {
        return this.getDataGraphs(angGraph).get(indGraph).getNumberOfVerticalLines();
    }
    
    /**
     * Get the number of (user) vertical lines being managed in the
     * given graph. We assume there is only one graph for each projection
     * angle; otherwise, the value for the first graph is returned. 
     *
     * @param angGraph  projection angle associated with desired graph
     *                  
     * @return          number of (user) vertical lines in the given graph
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2011
     */
    public int getVerticalLineCount(WireScanner.ANGLE angGraph) {
        return this.getDataGraphs(angGraph).get(0).getNumberOfVerticalLines();
    }
    
    /**
     * Returns the position of the vertical line at the given index
     * and given angle.  If this class displays multiple graphs for the
     * same angle then the first graph is used.
     *
     * @param angle     projection plane graph
     * @param intIndex  index of the line on the graph
     *
     * @return axis position of the given line
     * 
     * @since  Jun 7, 2010
     * @author Christopher K. Allen
     */
    public double getVerticalLinePosition(WireScanner.ANGLE angle, int intIndex) {
        FunctionGraphsJPanel plt    = this.getDataGraphs(angle).get(0);
        double               dblPos = plt.getVerticalValue(intIndex);
        
        return dblPos;
    }
    
    /**
     * Draws a vertical line on the given graph at the given
     * axis location using the given color value.
     *
     * @param angle     projection plane graph
     * @param dblPos    axis position to draw vertical line
     * @param clrLine     color of the line drawn, or <code>null</code> for default
     * 
     * @since  Jun 3, 2010
     * @author Christopher K. Allen
     */
    public void addVerticalLine(WireScanner.ANGLE angle, double dblPos, Color clrLine) {
        for (FunctionGraphsJPanel plt : this.getDataGraphs(angle) ) {
            if (clrLine == null)
                plt.addVerticalLine(dblPos);
            else
                plt.addVerticalLine(dblPos, clrLine);
        }
    }
    
    /**
     * Draws a vertical line on the given graph at the given
     * axis location using the given color value.  The same line
     * is drawn on each projection graph.
     *
     * @param dblPos    axis position to draw vertical line
     * @param clrLine     color of the line drawn, or <code>null</code> for default
     * 
     * @since  Jun 7, 2010
     * @author Christopher K. Allen
     */
    public void addVerticalLine(double dblPos, Color clrLine) {
        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values()) {
            this.addVerticalLine(angle, dblPos, clrLine);
        }
    }
    
    /**
     * Allows any vertical line on the given graphs to be dragged within the
     * plot by the user.
     *
     * @param angle     projection plane for the affected graphs
     * @param bolDrag   value <code>true</code> enables dragging,
     *                  value <code>false</code> disables dragging
     *
     * @author Christopher K. Allen
     * @since  Mar 24, 2011
     */
    public void setVerticalLineDragging(WireScanner.ANGLE angle, boolean bolDrag) {
        for (FunctionGraphsJPanel plt : this.getDataGraphs(angle)) {
            if (bolDrag)
                plt.addDraggedVerLinesListener( this.mapVerLnActs.get(angle) );
            else
                plt.addDraggedVerLinesListener( null );
            
            plt.setDraggingVerLinesGraphMode(bolDrag);
        }
    }
    
    /**
     * Allows any vertical line on all the graphs to be dragged within the
     * plot by the user.
     *
     * @param bolDrag   value <code>true</code> enables dragging,
     *                  value <code>false</code> disables dragging
     *
     * @author Christopher K. Allen
     * @since  Mar 24, 2011
     */
    public void setVerticalLineDragging(boolean bolDrag) {
        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values()) 
            this.setVerticalLineDragging(angle, bolDrag);
    }
    
    /**
     * Add a dragged vertical line action to each plot corresponding to
     * the given angle.  The <code>{@link FunctionGraphsJPanel}</code>
     * currently supports only one dragged vertical line listener, so this
     * method replaces any listener previously set.
     *
     * @param angle             applies to graphs supporting this projection angle
     * @param lsnActDragged     action response to a movement of a vertical line
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2011
     */
    public void addDraggedVerLinesListener(WireScanner.ANGLE angle, ActionListener lsnActDragged) {
            
        this.mapVerLnActs.put(angle, lsnActDragged);
        for (FunctionGraphsJPanel plt : this.getDataGraphs(angle)) 
            plt.addDraggedVerLinesListener(lsnActDragged);
    }
    
    /**
     * Add a dragged vertical line action to each plot.
     * The <code>{@link FunctionGraphsJPanel}</code>
     * currently supports only one dragged vertical line listener, so this
     * method replaces any listener previously set.
     *
     * @param lsnActDragged     action response to a movement of a vertical line
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2011
     */
    public void addDraggedVerLinesListener(ActionListener lsnActDragged) {
            
        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values())
            this.addDraggedVerLinesListener(angle, lsnActDragged);
    }
    
    /**
     * Moves an existing vertical line from its current position to the given
     * abscissa location.
     *
     * @param angle     graph containing the line
     * @param index     index of the line on the graph
     * @param dblPos    new position of the line
     * 
     * @since  Jun 3, 2010
     * @author Christopher K. Allen
     */
    public void setVerticalLine(WireScanner.ANGLE angle, int index, double dblPos) {
        for (FunctionGraphsJPanel plt : this.getDataGraphs(angle) ) 
            plt.setVerticalLineValue(dblPos, index);
    }
    
    /**
     * Moves an existing vertical line from its current position to the given
     * abscissa location.  This is done for all projections; that is, the
     * given line is moved to the same location in each projection plot.
     *
     * @param index     index of the line on the graph
     * @param dblPos    new position of the line
     * 
     * 
     * @since  Jun 7, 2010
     * @author Christopher K. Allen
     * 
     * @see #setVerticalLine(xal.smf.impl.WireScanner.ANGLE, int, double)
     */
    public void setVerticalLine(int index, double dblPos) {
        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values())
            this.setVerticalLine(angle, index, dblPos);
    }
    
    /**
     * Draws an horizontal line on the given graph at the given
     * axis location using the given color value.
     *
     * @param angle     projection plane graph
     * @param dblPos    axis position to draw horizontal line
     * @param color     color of the line drawn, or <code>null</code> for default
     * 
     * @since  Jun 3, 2010
     * @author Christopher K. Allen
     */
    public void addHorizontalLine(WireScanner.ANGLE angle, double dblPos, Color color) {
        for (FunctionGraphsJPanel plt : this.getDataGraphs(angle) ) {
            if (color == null)
                plt.addHorizontalLine(dblPos);
            else
                plt.addHorizontalLine(dblPos, color);
        }
    }
    
    /**
     * Draws a horizontal line on the given graph at the given
     * axis location using the given color value.  The same line
     * is drawn on each projection graph.
     *
     * @param dblPos    axis position to draw vertical line
     * @param clrLine   color of the line drawn, or <code>null</code> for default
     * 
     * @since  Jun 7, 2010
     * @author Christopher K. Allen
     */
    public void addHorizontalLine(double dblPos, Color clrLine) {
        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values()) {
            this.addHorizontalLine(angle, dblPos, clrLine);
        }
    }
    
    /**
     * Moves an existing horizontal line from its current ordinate position 
     * to the given location.
     *
     * @param angle     graph containing the line
     * @param index     index of the line on the graph
     * @param dblPos    new position of the line
     * 
     * @since  Jun 3, 2010
     * @author Christopher K. Allen
     */
    public void moveHorizontalLine(WireScanner.ANGLE angle, int index, double dblPos) {
        for (FunctionGraphsJPanel plt : this.getDataGraphs(angle) ) 
            plt.setHorizontalLineValue(dblPos, index);
    }

    /**
     * Moves an existing horizontal line from its current position to the given
     * abscissa location.  This is done for all projections; that is, the
     * given line is moved to the same location in each projection plot.
     *
     * @param index     index of the line on the graph
     * @param dblPos    new position of the line
     * 
     * 
     * @since  Jun 7, 2010
     * @author Christopher K. Allen
     * 
     * @see #moveHorizontalLine(xal.smf.impl.WireScanner.ANGLE, int, double)
     */
    public void moveHorizontalLine(int index, double dblPos) {
        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values())
            this.moveHorizontalLine(angle, index, dblPos);
    }
    
    /**
     * Allows any horizontal line on the given graphs to be dragged within the
     * plot by the user.
     *
     * @param angle     projection plane for the affected graphs
     * @param bolDrag   value <code>true</code> enables dragging,
     *                  value <code>false</code> disables dragging
     *
     * @author Christopher K. Allen
     * @since  Mar 24, 2011
     */
    public void setHorizontalLineDragging(WireScanner.ANGLE angle, boolean bolDrag) {
        for (FunctionGraphsJPanel plt : this.getDataGraphs(angle)) {
            if (bolDrag)
                plt.addDraggedHorLinesListener( this.mapHorLnActs.get(angle) );
            else
                plt.addDraggedHorLinesListener( null );
            
            plt.setDraggingHorLinesGraphMode(bolDrag);
        }
    }
    
    /**
     * Allows any horizontal line on all the graphs to be dragged within the
     * plot by the user.
     *
     * @param bolDrag   value <code>true</code> enables dragging,
     *                  value <code>false</code> disables dragging
     *
     * @author Christopher K. Allen
     * @since  Mar 24, 2011
     */
    public void setHorizontalLineDragging(boolean bolDrag) {
        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values()) 
            this.setHorizontalLineDragging(angle, bolDrag);
    }
    
    /**
     * Add a dragged horizontal line action to each plot corresponding to
     * the given angle.  The <code>{@link FunctionGraphsJPanel}</code>
     * currently supports only one dragged horizontal line listener, so this
     * method replaces any listener previously set.
     *
     * @param angle             applies to graphs supporting this projection angle
     * @param lsnActDragged     action response to a movement of an horizontal line
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2011
     */
    public void addDraggedHorLinesListener(WireScanner.ANGLE angle, ActionListener lsnActDragged) {
            
        this.mapHorLnActs.put(angle, lsnActDragged);
        for (FunctionGraphsJPanel plt : this.getDataGraphs(angle)) 
            plt.addDraggedHorLinesListener(lsnActDragged);
    }
    
    /**
     * Add a dragged horizontal line action to each plot.
     * The <code>{@link FunctionGraphsJPanel}</code>
     * currently supports only one dragged horizontal line listener, so this
     * method replaces any listener previously set.
     *
     * @param lsnActDragged     action response to a movement of an horizontal line
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2011
     */
    public void addDraggedHorLinesListener(ActionListener lsnActDragged) {
            
        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values())
            this.addDraggedVerLinesListener(angle, lsnActDragged);
    }
    
    
    /*
     * Plotting
     */
    
    /**
     * Displays the given curves on the plots.  We have one
     * argument for each projection angle of a profile measure 
     * set. 
     *
     * @param crvHor    data curve for the horizontal projection
     * @param crvVer    data curve for the vertical projection
     * @param crvDia    data curve for the diagonal projection
     * 
     * @since  Jan 8, 2010
     * @author Christopher K. Allen
     */
    public void displayCurves(BasicGraphData crvHor, BasicGraphData crvVer, BasicGraphData crvDia) {
        displayCurveWithAttrs(ANGLE.HOR, crvHor);
        displayCurveWithAttrs(ANGLE.VER, crvVer);
        displayCurveWithAttrs(ANGLE.DIA, crvDia);
    }
    
    /**
     * This method intercepts calls to 
     * <code>{@link GraphDisplayBase#displayCurve(xal.smf.impl.WireScanner.ANGLE, BasicGraphData)}</code>
     * to add the supported attributes to the curves (i.e., color, name, etc.).
     * At least the method is intercepted here.
     *
     * @param angle     projection angle of the curve data  
     * @param datCrv    the curve data
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     */
    public void displayCurveWithAttrs(WireScanner.ANGLE angle, BasicGraphData datCrv) {
        String  strKey = this.mapLgdKey.get(angle);
        
        datCrv.setGraphColor( this.mapCrvClr.get(angle) );
        datCrv.setGraphName( this.mapCrvLbl.get(angle) );
        datCrv.setDrawPointsOn( this.mapCrvPts.get(angle) );
        datCrv.setLineThick( this.mapCrvSz.get(angle) );
        datCrv.setGraphProperty(strKey, this.mapCrvLbl.get(angle) ); 
        this.displayCurve(angle, datCrv);
    }

    /**
     * Plots the given set of profile data on the
     * display screen.  (All data is plotted.)
     *
     * @param datDev   signal data to be plotted
     * 
     * @since  Jan 8, 2010
     * @author Christopher K. Allen
     */
    public void displayProfile(SignalSet datDev) {
        
        BasicGraphData      datHor = new BasicGraphData();
        BasicGraphData      datVer = new BasicGraphData();
        BasicGraphData      datDia = new BasicGraphData();

        datHor.removeAllPoints();
        datVer.removeAllPoints();
        datDia.removeAllPoints();
        
        datHor.addPoint(datDev.hor.pos, datDev.hor.val);
        datVer.addPoint(datDev.ver.pos, datDev.ver.val);
        datDia.addPoint(datDev.dia.pos, datDev.dia.val);
        
        this.displayCurves(datHor, datVer, datDia);
    }
    
    /**
     * Plots the given set of profile data on the
     * display screen.  Only the data between the
     * given indices is displayed.  
     *
     * @param datDev   signal data to be plotted
     * @param indStart first index of plot data (inclusive) 
     * @param indStop  last array index of plot data (exclusive)
     * 
     * @since  Aug 23, 2010
     * @author Christopher K. Allen
     */
    public void displayProfile(SignalSet datDev, int indStart, int indStop) {

//        HashMap<ANGLE, BasicGraphData>  mapAngPltData = new HashMap<ANGLE, BasicGraphData>();
        
        for (ANGLE ang : ANGLE.values()) {
            Signal  sglDev = datDev.getSignal(ang);
            int indStartSgl = (indStart < 0) ? 0 : indStart;
            int indStopSgl  = (indStop > sglDev.cnt) ? sglDev.cnt : indStop;
            
            BasicGraphData  datPlot = new BasicGraphData();
            
            for (int i=indStartSgl; i<indStopSgl; i++) 
                datPlot.addPoint(sglDev.pos[i], sglDev.val[i]);
            
//            mapAngPltData.put(ang,  datPlot);
//
            this.displayCurveWithAttrs(ang, datPlot);
        }
        
//        
//        BasicGraphData      datHor = new BasicGraphData();
//        BasicGraphData      datVer = new BasicGraphData();
//        BasicGraphData      datDia = new BasicGraphData();
//        
//        for (int i=indStart; i<indStop; i++) {
//            datHor.addPoint(datDev.hor.pos[i], datDev.hor.val[i]);
//            datVer.addPoint(datDev.ver.pos[i], datDev.ver.val[i]);
//            datDia.addPoint(datDev.dia.pos[i], datDev.dia.val[i]);
//        }
//
//        this.displayCurves(datHor, datVer, datDia);
    }
    
    /**
     * Plots multiple profile data sets simultaneously.
     *
     * @param lstDevData        list of profile data sets
     * 
     * @since  Jan 8, 2010
     * @author Christopher K. Allen
     */
    public void displaySignalSet(List<SignalSet> lstDevData) {
        
        for (SignalSet datDev : lstDevData){
            
            BasicGraphData      datHor = new BasicGraphData();
            BasicGraphData      datVer = new BasicGraphData();
            BasicGraphData      datDia = new BasicGraphData();

            datHor.removeAllPoints();
            datVer.removeAllPoints();
            datDia.removeAllPoints();
            
            datHor.addPoint(datDev.hor.pos, datDev.hor.val);
            datVer.addPoint(datDev.ver.pos, datDev.ver.val);
            datDia.addPoint(datDev.dia.pos, datDev.dia.val);
            
            this.displayCurves(datHor,datVer,datDia);
        }
    }
    
    /**
     * Plots multiple profile data sets simultaneously.
     *
     * @param lstDevData        list of profile data sets
     * @param indStart          first index of plot data (inclusive) 
     * @param indStop           last array index of plot data (exclusive)
     * 
     * @since  Jan 8, 2010
     * @author Christopher K. Allen
     */
    public void displaySignalSet(List<SignalSet> lstDevData, int indStart, int indStop) {
        
        for (SignalSet datDev : lstDevData) {
            BasicGraphData      datHor = new BasicGraphData();
            BasicGraphData      datVer = new BasicGraphData();
            BasicGraphData      datDia = new BasicGraphData();

            for (int i=indStart; i<indStop; i++) {

                datHor.addPoint(datDev.hor.pos[i], datDev.hor.val[i]);
                datVer.addPoint(datDev.ver.pos[i], datDev.ver.val[i]);
                datDia.addPoint(datDev.dia.pos[i], datDev.dia.val[i]);
            }

            this.displayCurves(datHor,datVer,datDia);
        }
    }
    
    /**
     * Plots the raw signal data from the measurement data
     * taken from a single device.
     *
     * @param datDev   measurement data from a single device
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public void displayRawData(IProfileData datDev) {
        int         indStop = datDev.getDataSize();
        SignalSet   sigRaw  = datDev.getRawData();
        
        this.displayProfile(sigRaw, 0, indStop);
    }

    /**
     * Plots the fitted signal data from the measurement data
     * taken from a single device.
     *
     * @param datDev   measurement data from a single device
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public void displayFittedData(IProfileData datDev) {
        int         indStop = datDev.getDataSize();
        SignalSet   sigFit  = datDev.getFitData();
        
        this.displayProfile(sigFit, 0, indStop);
    }

    /**
     * Plots all the raw signal data in a complete 
     * measurement set.
     *
     * @param setMsmt   complete set of measurement data
     * 
     * @since  Jan 8, 2010
     * @author Christopher K. Allen
     */
    public void displayRawData(MeasurementData setMsmt) {
        
        for (IProfileData datMsmt : setMsmt.getDataSet()) {
            int       indStop = datMsmt.getDataSize();
            SignalSet sigRaw  = datMsmt.getRawData();
            
            this.displayProfile(sigRaw, 0, indStop);
        }
    }

    /**
     * Plots all the fitted signal data in a complete 
     * measurement set.
     *
     * @param setMsmt   complete set of measurement data
     * 
     * @since  Jan 8, 2010
     * @author Christopher K. Allen
     */
    public void displayFittedData(MeasurementData setMsmt) {
        
        for (IProfileData datMsmt : setMsmt.getDataSet()) {
            int       indStop = datMsmt.getDataSize();
            SignalSet sigFit  = datMsmt.getFitData();
            
            this.displayProfile(sigFit, 0, indStop);
        }
    }

    
    /**
     * Adds a single data point to each signal curve in the current
     * set of signal curves (i.e., one for each device).
     *
     * @param lstDevData  ordered list of data for each device 
     * 
     * @since  Feb 5, 2010
     * @author Christopher K. Allen
     */
    public void displayAppendedPt(List<SignalSet> lstDevData) {
        
        // Check for uninitialized graph data
        int             cntDevs = lstDevData.size();
        boolean         bolInit = false;
        
        for (FunctionGraphsJPanel pnlGraph : this.getAllDataGraphs()) {
            if (pnlGraph.getNumberOfInstanceOfGraphData() < cntDevs)
                bolInit = true;
        }

        // We initialize the graph here if necessary.
        //      One curve per device per graph
        if (bolInit) {
            for (FunctionGraphsJPanel plt : this.getAllDataGraphs()) {
                for (int index=0; index<cntDevs; index++) {
                    BasicGraphData      crv = plt.getInstanceOfGraphData(index);

                    if (crv == null) 
                        plt.addGraphData( new BasicGraphData() );
                }
            }
        }
        
        // Append the data points to each curve of each graph
        int     indDev = 0;
        for (SignalSet datDev : lstDevData){
            for (WireScanner.ANGLE ang : WireScanner.ANGLE.values()) {
                for (FunctionGraphsJPanel plt : this.getDataGraphs(ang)) {
                    BasicGraphData      crv = plt.getInstanceOfGraphData(indDev);
                    double              pos;
                    double              val;
                    
                    switch (ang) {
                    case HOR:
                        pos = datDev.hor.pos[0];
                        val = datDev.hor.val[0];
                        break;
                        
                    case VER:
                        pos = datDev.ver.pos[0];
                        val = datDev.ver.val[0];
                        break;

                    case DIA:
                        pos = datDev.dia.pos[0];
                        val = datDev.dia.val[0];
                        break;

                    default:
                        pos = 0.0;
                        val = 0.0;
                    }

                    crv.addPoint(pos, val);
                }
            }

            indDev++;
        }
    }
    
   
    /*
     * Support Methods
     */
    
    /**
     * Computes the panel layout from the
     * application configuration parameters
     * and the panel layout constants.
     *
     * 
     * @since  July 16, 2009
     * @author Christopher K. Allen
     */
    private void configure() {
        
        // Get the plot color
        this.clrPlt = AppProperties.PLT.CLR_BGND.getValue().asColor();
        
        // Initialize the optional curve attribute maps
        for (ANGLE angle : ANGLE.values()) {
            this.mapLgdKey.put(angle, "");
            this.mapCrvPts.put(angle, true);
            this.mapCrvSz.put(angle, 1);
        }
        
    }

    
    /*
     * Support Method
     */
    
    /**
     * Returns the application's main event logger object.
     *
     * @return  application logger 
     * 
     * @since  Dec 18, 2009
     * @author Christopher K. Allen
     */
    protected IEventLogger        getLogger() {
        return MainApplication.getEventLogger();
    }
    
    /**
     * Get the background color of the graphs as specified
     * by the application configuration manager.
     *
     * @return  data graph background color
     * 
     * @since  Mar 29, 2010
     * @author Christopher K. Allen
     */
    protected Color     getGraphBgColor() {
        return this.clrPlt;
    }




}

