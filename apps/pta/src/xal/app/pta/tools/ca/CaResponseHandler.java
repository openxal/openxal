/**
 * CaResponseHandler.java
 *
 * @author  Christopher K. Allen
 * @since	Mar 29, 2011
 */
package xal.app.pta.tools.ca;

import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.tools.logging.NullLogger;
import xal.ca.ChannelRecord;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used by the response action objects
 * to make a delayed call to heavy-weight processing code.
 * A thread is spawned to make the actual response method
 * invocation.
 * This thread is necessary to allow the underlying channel access
 * monitor to return before making any channel access inquiries
 * in the processing code.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jun 10, 2010
 * @author Christopher K. Allen
 */
public class CaResponseHandler implements ActionListener, SmfPvMonitor.IAction {
    
    //
    // Global Constants
    //
    
    /** Time (in ms) we wait for channel access to return before updating processing window */
    public static final long               LNG_DELAY = 100;
    
    
    

    //
    //  Local Attributes
    //
    
    /** The object upon which we invoke it */
    private final Object        objTarget;
    
    /** The arguments of the method */
    private final Object[]      arrArgs;
    
    /** Name of the method we are to invoke */
    @SuppressWarnings("unused")
    private final String        strMthName;
    
    
    /** The method we are to invoke */
    private final Method        mthTarget;
    
    
    /** Event logging sink for the <code>run</code> method exceptions */
    private IEventLogger        lgrMsgSnk;
    
    
    /* 
     * Initialization
     */
    
    
    /**
     * Creates a new <code>CaResponseHandler</code> for the given
     * object and method.  When the <code>{@link #actionPerformed(ActionEvent)}</code>
     * method is called the given method spawn a thread to invoke the 
     * method provided on the given object, after sleeping for 
     * <code>{@link #LNG_DELAY}</code> milliseconds.
     * 
     * @param objTarget     the target object of this thread
     * @param strMthName    the name of the class method to invoke 
     * @param arrArgs       the array of arguments used for method invocation
     *
     * @author  Christopher K. Allen
     * @since   Mar 28, 2011
     */
    public CaResponseHandler(Object objTarget, String strMthName, Object...arrArgs) {
        this.objTarget  = objTarget;
        this.arrArgs    = arrArgs;
        this.strMthName = strMthName;
        
        this.mthTarget = this.findMethod(objTarget, strMthName, arrArgs);
        this.lgrMsgSnk = new NullLogger();
    }
    
    /**
     * Sets the logging object to receive any exception messages
     * while this object and its spawned thread executes.  If
     * the logger object is not set then any exception messages are
     * ignored, which may give the false impression that the
     * target method completed normally.
     * 
     * @param lgrMsgSnk the event logger object to receive exception messages
     *
     * @author Christopher K. Allen
     * @since  Mar 29, 2011
     */
    public void setLogger(IEventLogger lgrMsgSnk) {
        this.lgrMsgSnk = lgrMsgSnk;
    }


    /*
     * SmfMonitor.IAction Interface
     */
    
    /**
     * Method called by event-generating channel monitor to notify this
     * object that a change in monitored values has occurred.
     * 
     * @since Mar 31, 2011
     * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
     */
    @Override
    public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {

        MethodThread  thrResponse = new MethodThread(this.objTarget, this.mthTarget, this.arrArgs);
        thrResponse.setLogger(this.getLogger());
        
        thrResponse.start();
    }
    
    /*
     * ActionListener Interface
     */
    
    /**
     * Method called by event-generating object to notify this
     * object that the (adjective) event has occurred.
     * 
     * @param   e   The event data structure containing event information
     * 
     * @since Mar 30, 2011
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        MethodThread  thrResponse = new MethodThread(this.objTarget, this.mthTarget, this.arrArgs);
        thrResponse.setLogger(this.getLogger());
        
        thrResponse.start();
    }
    
    

    /*
     * Support Methods
     */

    /**
     * Returns the current event logger object, which may
     * be the <code>{@link NullLogger}</code> object if
     * method <code>{@link #setLogger(IEventLogger)}</code>
     * was not called.
     *
     * @return  the current event logger
     *
     * @author Christopher K. Allen
     * @since  Mar 29, 2011
     */
    private IEventLogger    getLogger() {
        if (this.lgrMsgSnk == null)
            this.lgrMsgSnk = new NullLogger();
        
        return this.lgrMsgSnk;
    }

    /**
     * Returns the java reflection <code>java.reflect.Method</code> object for the
     * given method name having the given arguments.  The target object class is
     * used to find the method.
     *
     * @param objTarget     the object on which we intend to invoke the method
     * @param strMthName    the method name within the object's class
     * @param arrArgs       signature of the method
     * 
     * @return              the <code>Method</code> object for the method
     *
     * @author Christopher K. Allen
     * @since  Mar 31, 2011
     */
    private Method  findMethod(Object objTarget, String strMthName, Object...arrArgs) {
        
        // Create the argument signature - a formal array of argument class types
        List<Class<?>>  lstClsArg = new LinkedList<Class<?>>();
        for (Object objArg : arrArgs)
            lstClsArg.add( objArg.getClass() );
        Class<?>[]  arrStore  =  new Class<?>[lstClsArg.size()];
        Class<?>[]  arrClsArg = lstClsArg.toArray(arrStore);
        
        // Get the type of the target object
        Class<?>    clsTarget = objTarget.getClass();
        
        // Get the method of the target object
        try {
            Method      mthTarget = clsTarget.getMethod(strMthName, arrClsArg);

            return mthTarget;
            
        } catch (NoSuchMethodException e) {
            getLogger().logException(CaResponseHandler.class, e, "Missing target method " + strMthName);
            
        } catch (SecurityException e) {
            getLogger().logException(CaResponseHandler.class, e, "Method " + strMthName + " is inaccessible");
            
        }

        return null;
    }
}


/**
 * Hidden class implementing the actual thread that
 * calls the response method after allowing Channel Access
 * to return.
 *
 * @author Christopher K. Allen
 * @since   Mar 31, 2011
 */
class MethodThread extends Thread {
    

    //
    //  Local Attributes
    //
    
    /** The object upon which we invoke it */
    private final Object        objTarget;
    
    /** The arguments of the method */
    private final Object[]      arrArgs;
    
    
    /** The method we are to invoke */
    private final Method        mthTarget;
    
    
    /** Event logging sink for the <code>run</code> method exceptions */
    private IEventLogger        lgrMsgSnk;

    
    /* 
     * Initialization
     */
    
    /**
     * Creates a new <code>MethodThread</cod> object for the given
     * object and method.  This thread is spawned when the 
     * <code>{@link CaResponseHandler#actionPerformed(ActionEvent)}</code>
     * method is called.  This thread pauses 
     * <code>{@link CaResponseHandler#LNG_DELAY}</code> milliseconds
     * then executes the given method on the given object. 
     * 
     * @param objTarget     the target object of this thread
     * @param mthTarget     class method to invoke 
     * @param arrArgs       the array of arguments used for method invocation
     *
     * @author  Christopher K. Allen
     * @since   Mar 28, 2011
     */
    public MethodThread(Object objTarget, Method mthTarget, Object...arrArgs) {
        this.objTarget = objTarget;
        this.mthTarget = mthTarget;
        this.arrArgs   = arrArgs;
        
        this.lgrMsgSnk = null;
    }
    
    /**
     * Sets the logging object to receive any exception messages
     * while the <code>{@link #run()}</code> method executes.  If
     * the logger object is not set then any exception messages are
     * ignored, which may give the false impression that the
     * <code>{@link #run()}</code> method completed normally.
     * 
     * @param lgrMsgSnk the event logger object to receive exception messages
     *
     * @author Christopher K. Allen
     * @since  Mar 29, 2011
     */
    public void setLogger(IEventLogger lgrMsgSnk) {
        this.lgrMsgSnk = lgrMsgSnk;
    }

    
    /*
     * Thread/Runnable Interface
     */

    /**
     * Waits <code>{@link CaResponseHandler#LNG_DELAY}</code> milli-seconds (allowing
     * channel access to return) then calls the given method of the
     * target object.
     *
     * @since   Jun 10, 2010
     * @author  Christopher K. Allen
     *
     */
    @Override
    public void run() {
        try {
            Thread.sleep(CaResponseHandler.LNG_DELAY);
            
        } catch (InterruptedException e) {
            getLogger().logException(getClass(), e, "Interrupted while waiting for CA to return");
            
        }
        
        try {
            this.mthTarget.invoke(this.objTarget, this.arrArgs);
            
        } catch (IllegalArgumentException e) {
            getLogger().logError(getClass(), "The target method is inaccessible");

        } catch (IllegalAccessException e) {
            getLogger().logError(getClass(), "The target method is inaccessible");
            
        } catch (InvocationTargetException e) {
            getLogger().logError(getClass(), "The target method is inaccessible");
            
        }
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Returns the current event logger object, which may
     * be the <code>{@link NullLogger}</code> object if
     * method <code>{@link #setLogger(IEventLogger)}</code>
     * was not called.
     *
     * @return  the current event logger
     *
     * @author Christopher K. Allen
     * @since  Mar 29, 2011
     */
    private IEventLogger    getLogger() {
        if (this.lgrMsgSnk == null)
            this.lgrMsgSnk = new NullLogger();
        
        return this.lgrMsgSnk;
    }

    
}
