/**
 * LaunchFromCaThread.java
 *
 * @author  Christopher K. Allen
 * @since	Mar 29, 2011
 */
package xal.app.pta.tools.ca;

import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.tools.logging.NullLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This thread is used by the response action objects
 * to make a delayed call to heavy-weight processing code.
 * This thread class is necessary to allow the underlying channel access
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
public class LaunchFromCaThread extends Thread {
    
    //
    // Global Constants
    //
    
    /** Time (in msec) we wait for channel access to return before updating processing window */
    public static final long               LNG_DELAY = 100;
    
    
    //
    //  Local Attributes
    //
    
    /** The method we are to invoke */
    private final Method        mthTarget;
    
    /** The object upon which we invoke it */
    private final Object        objTarget;
    
    /** The arguments of the method */
    private final Object[]      arrArgs;
    
    
    
    /** Event logging sink for the <code>run</code> method exceptions */
    private IEventLogger        lgrMsgSnk;
    
    
    /* 
     * Initialization
     */
    
    /**
     * Creates a new <code>LaunchFromCaThread</cod> for the given
     * object and method.  When the <code>{@link #run()}</code>
     * method of this thread object is called the given method will
     * be invoked on the given target after sleeping for 
     * <code>{@link #LNG_DELAY}</code> milliseconds.
     * 
     * @param objTarget the target object of this thread
     * @param mthTarget the method to invoke on the object
     * @param arrArgs   arguments of the above method
     *
     * @author  Christopher K. Allen
     * @since   Mar 28, 2011
     */
    public LaunchFromCaThread(Object objTarget, Method mthTarget, Object...arrArgs) {
        this.objTarget = objTarget;
        this.mthTarget = mthTarget;
        this.arrArgs   = arrArgs;
        
        this.lgrMsgSnk = new NullLogger();
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
     * Waits <code>{@link #LNG_DELAY}</code> milli-seconds (allowing
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
            Thread.sleep(LNG_DELAY);
            
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
        return this.lgrMsgSnk;
    }
}


