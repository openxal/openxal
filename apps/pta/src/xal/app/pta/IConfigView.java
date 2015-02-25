/**
 * IConfigView.java
 *
 * @author  Christopher K. Allen
 * @since	Jul 10, 2012
 */
package xal.app.pta;


/**
 * Interfaced used to receive updates that the machine configuration has chanced.  View
 * components should implement this interface then register themselves with the 
 * <code>MainConfiguration</code> class.
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Jul 10, 2012
 */
public interface IConfigView {

    /**
     * <p>
     * Machine configuration change event notification.
     * </p>
     * <p>
     * This method is called by the machine configuration manager,
     * implemented by the class <code>MainConfiguration</code>.
     * Whenever the manager object detects a change in 
     * the machine configuration, it fires this event so that
     * all registered views can decide if
     * they need to do something.
     * </p>
     *
     * @param cfgMain   the main machine configuration manager
     * @author Christopher K. Allen
     * @since  Jul 10, 2012
     */
    public  void    updateConfiguration(MainConfiguration cfgMain);
    
    /**
     * <p>
     * Entire accelerator changed event notification.
     * </p>
     * <p>
     * This method is called by the application
     * document object, implemented by the class <code>MainDocument</code>.
     * Whenever the document object detects a change in its
     * accelerator object, it should fire this event so that
     * all registered views can decide if
     * they need to do something.
     * </p>
     *
     * @param cfgMain   the main machine configuration manager
     * 
     * @author Christopher K. Allen
     * @since  Jul 10, 2012
     */
    public void     updateAccelerator(MainConfiguration cfgMain);
    
}
