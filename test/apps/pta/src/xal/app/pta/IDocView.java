/**
 * IDocumentView.java
 *
 *  Created	: Jun 15, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta;

/**
 * <p>
 * Interface 
 * for objects that are <tt>View</tt> components of the application data.
 * View classes that wish to receive updates when the application data is modified
 * should implement this interface then register with the <code>MainDocument</code>
 * class.
 * </p>
 * <p>
 * Recall that the application architecture is based upon the
 * <tt>Model/View/Controller</tt> design pattern.  
 * This interface
 * is enforcing the fact that the object exposing it is a view component.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jun 15, 2009
 * @author Christopher K. Allen
 */
public interface IDocView  {
    
    
    
//    /**
//     * <p>
//     * Document accelerator change event notification.
//     * </p>
//     * <p>
//     * This method is called by the application
//     * document object, implemented by the class <code>MainDocument</code>.
//     * Whenever the document object detects a change in its
//     * accelerator object, it should fire this event so that
//     * all registered views can decide if
//     * they need to do something.
//     * </p>
//     *
//     * @param docMain
//     * 
//     * @since  Jun 15, 2009
//     * @author Christopher K. Allen
//     */
//    public void         updateAccelerator(MainDocument docMain);
//    
    /**
     * <p>
     * Measurement data update event notification.
     * </p>
     * <p>
     * This method is called on all registered view objects
     * whenever the measurement data has been
     * changed in the main document.
     * </p>
     *
     * @param docMain   the application's main data document
     * 
     * @since  Mar 1, 2010
     * @author Christopher K. Allen
     */
    public void         updateMeasurementData(MainDocument docMain);
    
//    /**
//     * <p>
//     * Document DAQ device list change notification.
//     * </p>
//     * <p>
//     * This method is called on all registered view objects
//     * whenever the list of data acquisition devices has been
//     * changed.
//     * </p>
//     *
//     * @param docMain   the application's main data document
//     * 
//     * @since  Nov 17, 2009
//     * @author Christopher K. Allen
//     */
//    public void         updateDevices(MainDocument docMain);

}
