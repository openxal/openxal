/*
 *  BpmViewerPV.java
 *
 *  Created on July 8, 2004
 */
package xal.app.bpmviewer;

import java.awt.Color;
import java.text.*;

import xal.ca.*;
import xal.extension.widgets.plot.*;
import xal.tools.xml.*;
import xal.tools.data.DataAdaptor;

/**
 *  Keeps the references to the ArrayDataPV and graph data instances.
 *
 *@author     shishlo
 *@version    July 29, 2004
 */
public class BpmViewerPV {

    private ArrayDataPV arrayDataPV = new ArrayDataPV();

    private CurveData gd = new CurveData();

    private double[] x_arr = new double[0];

    private FunctionGraphsJPanel gp = null;

    private int colorInd = 0;

    private boolean wrapData = false;

    //format for numbers
    private DecimalFormat int_Format = new DecimalFormat( "###0" );
    private DecimalFormat dbl_Format = new DecimalFormat( "0.00E0" );


    /**
     *  Constructor for the BpmViewerPV object
     *
     *@param  gpIn  Description of the Parameter
     */
    public BpmViewerPV( FunctionGraphsJPanel gpIn ) {
        gp = gpIn;
        gd.setLineWidth( 2 );
    }


    /**
     *  Sets the color to the enclosed graph data.
     *
     *@param  colorInd  The new color index.
     */
    public void setColorIndex( int colorInd ) {
        this.colorInd = colorInd;
        Color color = IncrementalColors.getColor( colorInd );
        gd.setColor( color );
        arrayDataPV.update();
    }


    /**
     *  Sets the color to the enclosed graph data.
     *
     *@param  color  The new color.
     */
    public void setColor( Color color ) {
        gd.setColor( color );
        arrayDataPV.update();
    }


    /**
     *  Sets the channel name.
     *
     *@param  chanName  The new channel name.
     */
    public void setChannelName( String chanName ) {
        arrayDataPV.setChannelName( chanName );
        arrayDataPV.update();
    }


    /**
     *  Returns the channel name
     *
     *@return    The channel name.
     */
    public String getChannelName() {
        return arrayDataPV.getChannelName();
    }


    /**
     *  Sets the channel.
     *
     *@param  chIn  The new channel.
     */
    public void setChannel( Channel chIn ) {
        arrayDataPV.setChannel( chIn );
        arrayDataPV.update();
    }


    /**
     *  Returns the channel.
     *
     *@return    The channel.
     */
    public Channel getChannel() {
        return arrayDataPV.getChannel();
    }


    /**
     *  Returns the arrayDataPV attribute of the BpmViewerPVTreeNode object.
     *
     *@return    The arrayDataPV instance.
     */
    public ArrayDataPV getArrayDataPV() {
        return arrayDataPV;
    }


    /**
     *  Returns the graphData attribute of the BpmViewerPV object
     *
     *@return    The graphData value
     */
    public CurveData getGraphData() {
        return gd;
    }


    /**  Updates the graph data. */
    public void update() {
        Object syncObj = arrayDataPV.getSyncObject();
        synchronized ( syncObj ) {
            double[] y_arr = arrayDataPV.getValues();
            if(wrapData){
                for(int i = 1, n= y_arr.length; i < n; i++){
                    y_arr[i] = unwrap(y_arr[i],y_arr[i-1]);
                }
            }            
            if ( x_arr.length != y_arr.length ) {
                x_arr = new double[y_arr.length];
                for ( int i = 0; i < y_arr.length; i++ ) {
                    x_arr[i] = i;
                }
            }
            gd.setPoints( x_arr, y_arr );
        }
    }


    /**
     *  Sets the wrapDataProperty attribute of the BpmViewerPV object
     *
     *@param  wrapData  The new wrapDataProperty value
     */
    public void setWrapDataProperty( boolean wrapData ) {
        this.wrapData = wrapData;
    }


    /**
     *  Returns the wrapDataProperty attribute of the BpmViewerPV object
     *
     *@return    The wrapDataProperty value
     */
    public boolean getWrapDataProperty() {
        return wrapData;
    }


    /**
     *  This method finds +-2*PI to produce the nearest points
     *
     *@param  y    The value to be wrapped
     *@param  yIn  The previous value
     *@return      The wrapped value
     */
    private static double unwrap( double y, double yIn ) {
        if ( y == yIn ) {
            return y;
        }
        int n = 0;
        double diff = yIn - y;
        double diff_min = Math.abs( diff );
        double sign = diff / diff_min;
        int n_curr = n + 1;
        double diff_min_curr = Math.abs( y + sign * n_curr * 360. - yIn );
        while ( diff_min_curr < diff_min ) {
            n = n_curr;
            diff_min = Math.abs( y + sign * n * 360. - yIn );
            n_curr++;
            diff_min_curr = Math.abs( y + sign * n_curr * 360. - yIn );
        }
        return ( y + sign * n * 360. );
    }


    /**
     *  Dumps information about the configuration of the BpmViewerPV instance
     *  into the DataAdaptor instance
     *
     *@param  da  The DataAdaptor instance as a place to keep config
     *      information
     */
    public void dumpConfig( DataAdaptor da ) {
        DataAdaptor bpmPV_DA = da.createChild( "BPM_PV" );
        String chName = "empty";
        String chName0 = arrayDataPV.getChannelName();
        if ( chName0 != null ) {
            chName = chName0;
        }
        bpmPV_DA.setValue( "chName", chName );
        bpmPV_DA.setValue( "switchOn", arrayDataPV.getSwitchOn() );
        bpmPV_DA.setValue( "nGraphPoints", gd.getSize() );
        if ( gd.getSize() > 0 ) {
            for ( int j = 0, nj = gd.getSize(); j < nj; j++ ) {
                DataAdaptor g_DA = bpmPV_DA.createChild( "point" );
                g_DA.setValue( "x", int_Format.format( gd.getX( j ) ) );
                g_DA.setValue( "y", dbl_Format.format( gd.getY( j ) ) );
            }
        }
    }


    /**
     *  Configures the BpmViewerPV instance according the configuration
     *  information in the DataAdaptor instance
     *
     *@param  bpmPV  The new config value
     */
    public void setConfig( DataAdaptor bpmPV ) {
        String pvName = bpmPV.stringValue( "chName" );
        arrayDataPV.setChannelName( pvName );
        boolean switchOnTmp = bpmPV.booleanValue( "switchOn" );
        arrayDataPV.setSwitchOn( switchOnTmp );
        int nPoints = bpmPV.intValue( "nGraphPoints" );
        gd.clear();
        if ( nPoints <= 0 ) {
            return;
        }
        
        for (final DataAdaptor g_DA : bpmPV.childAdaptors()) {
            gd.addPoint( g_DA.doubleValue( "x" ), g_DA.doubleValue( "y" ) );
        }
    }

}

