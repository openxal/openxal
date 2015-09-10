/*
 *  ArrayDataPV.java
 *
 *  Created on July 7, 2004, 10:25 AM
 */
package xal.app.bpmviewer;

import java.util.*;
import java.awt.event.*;

import xal.ca.*;
import xal.extension.scan.MonitoredPV;

/**
 *  This class keeps a reference to the PV with array data and listens to the
 *  data change. It notifies a plot controller that data have been changed and
 *  needed to be re-ploted.
 *
 *@author     shishlo
 *@version    July 29, 2004
 */

public class ArrayDataPV {

    private Object syncObj = new Object();

    private Vector<Object> controllers = new Vector<Object>();

    private double[] vals = new double[0];

    private MonitoredPV mpv = null;

    private ActionListener updateListener = null;

    private static int nextIndex = 0;

    private volatile boolean switchOn = true;


    /**  Constructor for the ArrayDataPV object. */
    public ArrayDataPV() {

        updateListener =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    synchronized ( syncObj ) {

                        if ( !switchOn ) {
                            if ( vals.length != 0 ) {
                                vals = new double[0];
                            }
                            return;
                        }

                        if ( mpv != null && mpv.isGood() ) {
                            double[] localVals = new double[0];
                            Channel ch = mpv.getChannel();

                            try {
                                localVals = ch.getArrDbl();
                            }
                            catch ( ConnectionException exp ) {
                                vals = new double[0];
                            }
                            catch ( GetException exp ) {
                                vals = new double[0];
                            }

                            if ( localVals.length != vals.length ) {
                                vals = new double[localVals.length];
                            }
                            for ( int i = 0; i < localVals.length; i++ ) {
                                vals[i] = localVals[i];
                            }
                        }
                        else {
                            vals = new double[0];
                        }
                    }
                    update();
                }
            };

        mpv = MonitoredPV.getMonitoredPV( "ArrayDataPV_" + nextIndex );
        mpv.addValueListener( updateListener );
        nextIndex++;
    }



    /**
     *  Returns the reference to the data array. The operations with this array
     *  should be synchronized by syncObj that can be received by using
     *  getSyncObject() method.
     *
     *@return    The data array
     */
    public double[] getValues() {
        return vals;
    }


    /**
     *  Sets the channel name.
     *
     *@param  chanName  The new channel name.
     */
    public void setChannelName( String chanName ) {
        mpv.setChannelName( chanName );
    }


    /**
     *  Returns the channel name
     *
     *@return    The channel name.
     */
    public String getChannelName() {
        return mpv.getChannelName();
    }


    /**
     *  Sets the channel.
     *
     *@param  chIn  The new channel.
     */
    public void setChannel( Channel chIn ) {
        mpv.setChannel( chIn );
    }


    /**
     *  Returns the channel.
     *
     *@return    The channel.
     */
    public Channel getChannel() {
        return mpv.getChannel();
    }


    /**
     *  Calls for controllers' update methods. This method call will cause the
     *  update procedure of all controllers will be called.
     */
    public void update() {
        for ( int j = 0; j < controllers.size(); j++ ) {
            ( (UpdatingController) controllers.get( j ) ).update();
        }
    }


    /**
     *  Returns all controllers registered with this ArrayDataPV. This method
     *  has to be called from UpdatingController class only.
     *
     *@return    The vector with references to UpdatingController instances.
     */
    protected Vector<Object> getControllers() {
        return new Vector<Object>( controllers );
    }


    /**
     *  Returns the syncObj reference. It is used for synchronization.
     *
     *@return    The syncObj reference
     */
    public Object getSyncObject() {
        return syncObj;
    }


    /**
     *  Sets the syncObject attribute of the ArrayDataPV object. This method has
     *  to be called from UpdatingController class only.
     *
     *@param  syncObj  The new syncObject value
     */
    protected void setSyncObject( Object syncObj ) {
        synchronized ( syncObj ) {
            synchronized ( this.syncObj ) {
                this.syncObj = syncObj;
            }
        }
    }


    /**
     *  Sets the controller instance. This method has to be called from
     *  UpdatingController class only.
     *
     *@param  cntr  The new UpdatingController instance
     */
    protected void addController( UpdatingController cntr ) {
        if ( cntr == null ) {
            return;
        }
        if ( !controllers.contains( cntr ) ) {
            Object localSyncObj = cntr.getSyncObj();
            synchronized ( localSyncObj ) {
                synchronized ( syncObj ) {
                    controllers.add( cntr );
                    setSyncObject( localSyncObj );
                    startMonitoring();
                }
            }
        }
    }


    /**
     *  Removes the controller instance. This method has to be called from
     *  UpdatingController class only.
     *
     *@param  cntr  The UpdatingController instance.
     */
    protected void removeController( UpdatingController cntr ) {
        if ( cntr == null ) {
            return;
        }
        synchronized ( cntr.getSyncObj() ) {
            controllers.remove( cntr );
            if ( controllers.size() == 0 ) {
                stopMonitoring();
                setSyncObject( new Object() );
            }
        }
    }


    /**
     *  Removes all controllers from ArrayDataPV instance. This method should be
     *  called to make this instance garbage collectible.
     */
    public void removeControllers() {
        Vector<Object> cntrls = getControllers();
        for ( int i = 0; i < cntrls.size(); i++ ) {
            ( (UpdatingController) cntrls.get( i ) ).removeArrayDataPV( this );
        }
    }


    /**
     *  Returns true if the update monitor is working. By default it is On.
     *
     *@return    true if the update monitor is working, false otherwise.
     */
    public boolean getSwitchOn() {
        return switchOn;
    }


    /**
     *  Sets the switch on key for monitoring.
     *
     *@param  switchOn  The new switchOn value
     */
    public void setSwitchOn( boolean switchOn ) {
        this.switchOn = switchOn;
        synchronized ( syncObj ) {
            if ( !switchOn ) {
                if ( vals.length != 0 ) {
                    vals = new double[0];
                }
            }
        }
        update();
    }


    /**  Stops monitoring of changes in the PV. */
    private void stopMonitoring() {
        mpv.stopMonitor();
    }


    /**  Starts monitoring of changes in the PV. */
    private void startMonitoring() {
        mpv.startMonitor();
    }


    /**  Removes the monitored PV. */
    protected void finalize() {
        MonitoredPV.removeMonitoredPV( mpv );
    }

}

