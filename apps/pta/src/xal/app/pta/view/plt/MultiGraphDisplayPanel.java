/**
 * MultiGraphDisplayPanel.java
 *
 *  Created	: Jul 16, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.plt;

import xal.app.pta.rscmgt.AppProperties;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.smf.impl.profile.ProfileDevice.ANGLE;

import java.awt.Color;
import java.awt.LayoutManager;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.border.BevelBorder;

/**
 * Displays beam profile data as three separate plots, one for each 
 * diagnostic plane. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jul 16, 2009
 * @author Christopher K. Allen
 */
public class MultiGraphDisplayPanel extends GraphDisplayBase {

    
    /**
     * Enumeration of the available data plot layouts.
     *
     * @since  Jul 16, 2009
     * @author Christopher K. Allen
     */
    public enum LAYOUT {
        
        /** layout the data plots horizontally */
        HOR(BoxLayout.X_AXIS),
        
        /** layout the data plots vertically */
        VER(BoxLayout.Y_AXIS);
        
        
        /**
         * Returns the <code>BoxLayout</code> layout direction 
         * constant value for the data plot.
         *
         * @return      the <code>BoxLayout</code> layout constant 
         * 
         * @since  Jul 16, 2009
         * @author Christopher K. Allen
         */
        public int getLayoutConstant() {
            return this.intLoutConst;
        }
        
        // Private Stuff
        
        /** the <code>BoxLayout</code> layout constant */
        private final int     intLoutConst;
        
        /**
         * Create the <code>LAYOUT</code> constants with
         * their values.
         * 
         * @param intLoutConst axis value for SWING box layout
         *
         * @author  Christopher K. Allen
         * @since   Oct 3, 2011
         */
        private LAYOUT(int intLoutConst) {
            this.intLoutConst = intLoutConst;
        }
    }
    
    
    /*
     * Global Constants
     */
    
    /**  Serialization version ID */
    private static final long serialVersionUID = 1L;

    
    /****  Layout    ****/
    
    /** horizontal spacing between graphs */
    public static final int     INT_HOR_SPACE_BUFFER = 20;

    /** vertical spacing between graphs */
    public static final int     INT_VER_SPACE_BUFFER = 30;

    
    /** Graph **/
    
    /** The number of <i>x</i>-divisions on the graphs */
    public static final int     CNT_X_DIVS = 20;
    
    
    
    /*
     * Local Attributes
     */
    
    
    /** Plot layout */
    private final LAYOUT        layout;
    
    /** color of the plots */
    private Color               clrPlt;
    
    
    /** array of data plots */
    private FunctionGraphsJPanel[]      arrPlots;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>MultiGraphDisplayPanel</code> object.
     *
     * @param layout    profile data plot layout direction 
     *
     * @since     Jul 16, 2009
     * @author    Christopher K. Allen
     */
    public MultiGraphDisplayPanel(LAYOUT layout) {
        super();
        
        this.layout = layout;
        
        this.initDataGraphs();
        this.buildPanelGui();
    }

    
    
    /*
     *  Plot Attributes 
     */
    
    /**
     * Return the panel layout enumeration constant
     * (this does not change for any 
     * <code>MultiGraphDisplayPanel</code> instance.
     *
     * @return  the panel layout
     * 
     * @since  Jul 16, 2009
     * @author Christopher K. Allen
     */
    public LAYOUT getPanelLayout() {
        return this.layout;
    }
    
    
    

    /*
     * GraphDisplayBase Abstract Methods
     */

    /**
     * Returns all the graph objects in this panel.
     * 
     * @return  a list containing all graph objects, one for each projection angle
     *
     * @since   Mar 30, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.plt.GraphDisplayBase#getAllDataGraphs()
     */
    @Override
    protected List<FunctionGraphsJPanel> getAllDataGraphs() {
        List<FunctionGraphsJPanel>      lst = new LinkedList<FunctionGraphsJPanel>();
        
        for (ANGLE ang : ANGLE.values()) 
            lst.add( this.getDataPlot(ang) );
        
        return lst;
    }



    /**
     * Returns the graph object for the given data projection angle.
     * 
     * @return  list containing the graph corresponding to the given projection angle.
     *
     * @since   Mar 30, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.plt.GraphDisplayBase#getDataGraphs(xal.smf.impl.WireScanner.Data.ANGLE)
     */
    @Override
    protected List<FunctionGraphsJPanel> getDataGraphs(ANGLE angPlt) {
        List<FunctionGraphsJPanel>      lst = new LinkedList<FunctionGraphsJPanel>();
        
        lst.add( this.getDataPlot(angPlt) );
        
        return lst;
    }

    /**
     * Adds the given graph curve to the plot specified by the
     * argument <var>enmAng</var>.
     *
     * @param enmAng    the plot that receives the curve data 
     * @param datCrv    the curve data
     * 
     * @since  Feb 18, 2010
     * @author Christopher K. Allen
     */
    @Override
    public void displayCurve(ANGLE enmAng, BasicGraphData datCrv) {
        this.getDataPlot(enmAng).addGraphData(datCrv);
    }
    


    
    /*
     * Support
     */
    
    /**
     * Instantiate and initialize the profile data
     * display graphs.
     *
     * 
     * @since  Jul 16, 2009
     * @author Christopher K. Allen
     */
    private void initDataGraphs()       {
        
        // create and configure the data plots 
        this.clrPlt   = AppProperties.PLT.CLR_BGND.getValue().asColor();
        this.arrPlots = new FunctionGraphsJPanel[ANGLE.values().length];
        for (ANGLE angle : ANGLE.values()) {
            int index = angle.getIndex();

            this.arrPlots[index] = new FunctionGraphsJPanel();
            this.arrPlots[index].setGraphBackGroundColor(clrPlt);
        }
    }
    
    /**
     * Build the panel's GUI according the
     * the layout parameters.
     *
     * 
     * @since  Jul 14, 2009
     * @author Christopher K. Allen
     */
    private void buildPanelGui(){
        
        // Configure the main panel
        LayoutManager mgrLout = new BoxLayout(this, this.layout.getLayoutConstant());

        this.setLayout(mgrLout);
        this.setBorder(BorderFactory.createRaisedBevelBorder());

        // Configure the data plots and add to main panel
        for (ANGLE angle : ANGLE.values()) {

            this.getDataPlot(angle).setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            this.getDataPlot(angle).setName(angle.getLabel());
            this.add(this.getDataPlot(angle));
        }
        
    }

    /**
     * Returns data display plot for the 
     * given projection angle.
     *
     * @param angle     projection angle
     * 
     * @return  the data graph for the given projection angle
     * 
     * @since  Jul 16, 2009
     * @author Christopher K. Allen
     */
    private FunctionGraphsJPanel        getDataPlot(ANGLE angle)    {
        return this.arrPlots[ angle.getIndex() ];
    }

}

