/**
 * SingleGraphDisplayPanel.java
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

/**
 * Displays beam profile data on one plot, one for each 
 * diagnostic plane has a separate curve. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Mar 29, 2009
 * @author Christopher K. Allen
 */
public class SingleGraphDisplayPanel extends GraphDisplayBase {

    
    
    /*
     * Global Constants
     */
    
    /**  Serialization version ID */
    private static final long serialVersionUID = 1L;


    
    /**
     * Data structure containing properties for each curve
     * in the graph.
     *
     * @since  Mar 30, 2010
     * @author Christopher K. Allen
     */
    private class CurveProps {

        /** The color of the curve in the graph */
        public Color    clrCurve;
        
        /** The key of the curve in the legend */
        public String   strKey;
     
        /**
         * Creates a new, uninitialized <code>CurveProps</code>
         * object.
         *
         * @author  Christopher K. Allen
         * @since   Oct 3, 2011
         */
        public CurveProps() {
            this.strKey   = "";
            this.clrCurve = Color.WHITE;
        }
    }
    
    
    /*
     * Local Attributes
     */
    
    /** legend colors for each curve */
    private Map<ANGLE, CurveProps>      mapCrvProps;
    
    /** array of data plots */
    private FunctionGraphsJPanel        pnlGraph;
    
    
    /** list of the single data plot */
    private List<FunctionGraphsJPanel>    lstGraph;
    

    
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>SingleGraphDisplayPanel</code> object.
     *
     * @since     Jul 16, 2009
     * @author    Christopher K. Allen
     */
    public SingleGraphDisplayPanel() {
        super();
    
        this.initGraph();
        this.initLegend();
        this.layoutGui();
    }

    
    /*
     *  Plot Attributes 
     */
    

    /**
     * Returns data display plot for the 
     * current profile data.
     *
     * @return  the data graph panel
     * 
     * @since  Jul 16, 2009
     * @author Christopher K. Allen
     */
    public FunctionGraphsJPanel getDataPlot()    {
        return this.pnlGraph;
    }



    /*
     * GraphDisplayBase Abstract Methods
     */

    /**
     * Returns the single graph component of this display (a list of one).
     * 
     * @return  a list containing one element, the graphing component
     *
     * @since   Mar 29, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.plt.GraphDisplayBase#getAllDataGraphs()
     */
    @Override
    protected List<FunctionGraphsJPanel> getAllDataGraphs() {
        return this.lstGraph;
    }



    /**
     * Returns the single graph component of this display (a list of one), regardless
     * of the argument.
     * 
     * @param   angPlt  the projection angle, not used 
     * 
     * @return  a list containing one element, the graphing component
     *
     * @since   Mar 29, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.plt.GraphDisplayBase#getDataGraphs(xal.smf.impl.WireScanner.ANGLE)
     */
    @Override
    protected List<FunctionGraphsJPanel> getDataGraphs(ANGLE angPlt) {
        return this.getAllDataGraphs();
    }


    /**
     * Displays the given curve labeled as the given projection.
     * 
     * @param   enmAng  the projection angle, used as a label
     * @param   datCrv  the data curve to be displayed 
     *
     * @since   Mar 30, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.plt.GraphDisplayBase#displayCurve(xal.smf.impl.WireScanner.ANGLE, xal.extension.widgets.plot.BasicGraphData)
     */
   @Override
   public void displayCurve(ANGLE enmAng, BasicGraphData datCrv) {
       CurveProps       prps = this.mapCrvProps.get(enmAng);
       String           strKeyLgd = this.pnlGraph.getLegendKeyString();
       
       datCrv.setGraphColor(prps.clrCurve);
       datCrv.setGraphProperty(strKeyLgd, prps.strKey);
       
       this.pnlGraph.addGraphData(datCrv);
   }



   /**
    * Displays the given three curves to be labeled as
    * horizontal projection data, vertical projection data,
    * and diagonal projection data, respectively. 
    * 
    * @param   datHor   the horizontal data curve to be displayed 
    * @param   datVer   the vertical data curve to be displayed 
    * @param   datDia   the diagonal data curve to be displayed 
    *
    * @since   Mar 30, 2010
    * @author  Christopher K. Allen
    *
    * @see xal.app.pta.view.plt.GraphDisplayBase#displayCurves(xal.extension.widgets.plot.BasicGraphData, xal.extension.widgets.plot.BasicGraphData, xal.extension.widgets.plot.BasicGraphData)
    */
   @Override
   public void displayCurves(BasicGraphData datHor, 
                             BasicGraphData datVer,
                             BasicGraphData datDia) 
   {
       this.displayCurve(ANGLE.HOR, datHor);
       this.displayCurve(ANGLE.VER, datVer);
       this.displayCurve(ANGLE.DIA, datDia);
       
   }

   

    /*
     * Support Methods
     */
    
    
    /**
     * Instantiate and initialize the profile data
     * display graph.
     * 
     * @since  Jul 16, 2009
     * @author Christopher K. Allen
     */
    private void initGraph()       {
        
        // Create and configure the graph
        Color   clrBkgnd = AppProperties.PLT.CLR_BGND.getValue().asColor();

        this.pnlGraph = new FunctionGraphsJPanel();
        this.pnlGraph.setName("All Planes");
        this.pnlGraph.setGraphBackGroundColor(clrBkgnd);
        this.pnlGraph.setLegendVisible(true);

        // Create the graph list and initialize it (for abstract methods getAllDataGraphs(), ...)
        this.lstGraph = new LinkedList<FunctionGraphsJPanel>();
        this.lstGraph.add( this.pnlGraph );
    }
    
    /**
     * Initializes the data structures for the plot legend and load
     * them into the legend map. 
     *
     * @author Christopher K. Allen
     * @since  Nov 14, 2011
     */
    private void initLegend() {
        
        // Do the legend 
        this.mapCrvProps  = new HashMap<ANGLE, CurveProps>();

        CurveProps prpHor = new CurveProps();
        prpHor.strKey   = AppProperties.PLT.LGD_KEY_HOR.getValue().asString();
        prpHor.clrCurve = AppProperties.PLT.CLR_CRV_HOR.getValue().asColor();
        this.mapCrvProps.put(ANGLE.HOR, prpHor);
        
        CurveProps prpVer = new CurveProps();
        prpVer.strKey   = AppProperties.PLT.LGD_KEY_VER.getValue().asString();
        prpVer.clrCurve = AppProperties.PLT.CLR_CRV_VER.getValue().asColor();
        this.mapCrvProps.put(ANGLE.VER, prpVer);

        CurveProps prpDia = new CurveProps();
        prpDia.strKey   = AppProperties.PLT.LGD_KEY_DIA.getValue().asString();
        prpDia.clrCurve = AppProperties.PLT.CLR_CRV_DIA.getValue().asColor();
        this.mapCrvProps.put(ANGLE.DIA, prpDia);
    }

    /**
     * Layout and display the GUI components on the GUI face.
     *
     * @author Christopher K. Allen
     * @since  Nov 14, 2011
     */
    private void layoutGui() {

        // Configure the main panel
        LayoutManager mgrLout = new BoxLayout(this, BoxLayout.X_AXIS);

        this.setLayout(mgrLout);
        this.setBorder(BorderFactory.createRaisedBevelBorder());
        this.add(this.pnlGraph);
//        this.add( new JLabel("Really Test You") );
    }

}

