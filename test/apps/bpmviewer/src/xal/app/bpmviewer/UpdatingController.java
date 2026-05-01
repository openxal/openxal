/*
 *  UpdatingController.java
 *
 *  Created on July 7, 2004, 10:25 AM
 */
package xal.app.bpmviewer;

import java.util.*;
import java.awt.event.*;

/**
 *  This controller calls actionPerformed method of all registered listeners
 *  upon ArrayDataPVs requests
 *
 *@author     shishlo
 */

public class UpdatingController {

    private Object syncObj = new Object();

    //Internal synch object for purpose this class instance only
    private Object syncObjInernal = new Object();

    private Vector<ArrayDataPV> dataPVs = new Vector<ArrayDataPV>();

    private volatile boolean updateInProgress = false;

    private volatile boolean threadInProgress = false;

    private Vector<ActionListener> listenersV = new Vector<ActionListener>();

    private ActionEvent updateEvent = null;

    //stack counters
    private volatile int stackSize = 0;
    private volatile int currentIndex = 0;

    //sleep time in milliseconds
    private int sleepTime = 100;

    private Runnable updateRun = null;


    /**
     *  Constructor for the UpdatingPlotController object
     */
    public UpdatingController() {
        updateEvent = new ActionEvent(this, 0, "update");

        updateRun =
            new Runnable() {
                public void run() {
                    boolean hasToStop = false;
                    while (!hasToStop) {
                        threadInProgress = true;

                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            updateInProgress = false;
                            threadInProgress = false;
                            return;
                        }

                        synchronized (syncObj) {
                            currentIndex = stackSize;
                            for (int i = 0; i < listenersV.size(); i++) {
                                (listenersV.get(i)).actionPerformed(updateEvent);
                            }
                        }

                        synchronized (syncObjInernal) {
                            if (currentIndex >= stackSize) {
                                hasToStop = true;
                                updateInProgress = false;
                                threadInProgress = false;
                                stackSize = 0;
                            }
                        }
                    }
                }
            };
    }



    /**
     *  Sets the maximal update frequency.
     *
     *@param  fr  The new maximal frequency value in Hz
     */
    public void setUpdateFrequency(double fr) {
        if (fr <= 0.) {
            sleepTime = 30000;
            return;
        }
        sleepTime = (int) (1000. / fr);
    }


    /**
     *  Returns true if the updating has not been accomplished.
     *
     *@return    true of false
     */
    public boolean inProgress() {
        return updateInProgress;
    }

    /**
     *  Makes necessary action for registered listeners.
     */
    protected void update() {

        synchronized (syncObjInernal) {
            stackSize++;
            if(stackSize > 100000000){
                stackSize = 0;
            }
        }

        if (updateInProgress) {
            return;
        }

        updateInProgress = true;
        if (!threadInProgress) {
            Thread execThread = new Thread(updateRun);
            execThread.start();
        }
    }


    /**
     *  Adds the new ActionListener.
     *
     *@param  al  The new ActionListener.
     */
    public void addActionListener(ActionListener al) {
        listenersV.add(al);
    }


    /**
     *  Removes the ActionListener.
     *
     *@param  al  The ActionListener to be removed.
     */
    public void removeActionListener(ActionListener al) {
        listenersV.remove(al);
    }


    /**
     *  Returns all ActionListeners as a Vector
     *
     *@return    The vector with references to ActionListeners
     */
    public Vector<ActionListener> getActionListeners() {
        return new Vector<ActionListener>(listenersV);
    }


    /**
     *  Controller Adds the ArrayDataPV's instance to this controller.
     *
     *@param  arrayDataPV  The instance of ArrayDataPV to be added to this
     *      controller
     */
    public void addArrayDataPV(ArrayDataPV arrayDataPV) {
        if (arrayDataPV == null) {
            return;
        }
        synchronized (syncObj) {
            arrayDataPV.addController(this);
            dataPVs.add(arrayDataPV);
            setSyncObj(syncObj);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  arrayDataPV  The arrayDataPV instance
     */
    public void removeArrayDataPV(ArrayDataPV arrayDataPV) {
        if (arrayDataPV == null) {
            return;
        }
        if (dataPVs.contains(arrayDataPV)) {
            synchronized (syncObj) {
                dataPVs.remove(arrayDataPV);
                arrayDataPV.removeController(this);
            }
        }
    }


    /**
     *  Removes all array data PVs from this controller
     */
    public void removeArrayDataPV() {
        Vector<ArrayDataPV> dataPVsLocal = new Vector<ArrayDataPV>(dataPVs);
        for (int i = 0; i < dataPVsLocal.size(); i++) {
            removeArrayDataPV(dataPVsLocal.get(i));
        }
    }


    /**
     *  Sets the syncObj attribute of the UpdatingController object
     *
     *@param  syncObjNew  The new syncObj value
     */
    public void setSyncObj(Object syncObjNew) {
        synchronized (syncObj) {
            synchronized (syncObjNew) {
                if (dataPVs.size() != 0) {
                    Vector<Object> cntrlsAll = new Vector<Object>();
                    for (int i = 0; i < dataPVs.size(); i++) {
                        ArrayDataPV ard = dataPVs.get(i);
                        Vector<Object> cntrls = ard.getControllers();
                        for (int j = 0; j < cntrls.size(); j++) {
                            if (!cntrlsAll.contains(cntrls.get(j))
                                 && ((Object) this) != cntrls.get(j)) {
                                cntrlsAll.add(cntrls.get(j));
                            }
                        }
                    }
                    for (int j = 0; j < cntrlsAll.size(); j++) {
                        ((UpdatingController) cntrlsAll.get(j)).setSyncObjSimple(syncObjNew);
                    }
                }
                setSyncObjSimple(syncObjNew);
            }
        }
    }


    /**
     *  Sets the synchronization object without any others actions
     *
     *@param  syncObjNew  The new syncObj value
     */
    private void setSyncObjSimple(Object syncObjNew) {
        synchronized (syncObj) {
            synchronized (syncObjNew) {
                for (int i = 0; i < dataPVs.size(); i++) {
                    ArrayDataPV ard = dataPVs.get(i);
                    ard.setSyncObject(syncObjNew);
                }
                syncObj = syncObjNew;
            }
        }
    }



    /**
     *  Gets the syncObj attribute of the UpdatingController object
     *
     *@return    The syncObj value
     */
    public Object getSyncObj() {
        return syncObj;
    }

}

