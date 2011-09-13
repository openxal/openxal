package xal.sim.mpx;

/**
 * @author wdklotz
 *  
 * created May 22, 2003
 * 
 */

/**The Interface for objects that listen to events from the on-line model proxy.
 * @author wdklotz
 */
public interface ModelProxyListener extends java.util.EventListener {

/**Named constant to indicate the cause 'accelerator changed'.*/
	public final static int ACCEL_CHANGED= 1;
/**Named constant to indicate the cause 'accelerator sequence changed'.*/
	public final static int SEQUENCE_CHANGED= 2;
/**Named constant to indicate the cause 'probe changed'.*/
	public final static int PROBE_CHANGED= 3;
/**Named constant to indicate the cause 'model results changed'.*/
	public final static int RESULTS_CHANGED= 4;
/**Named constant to indicate the cause 'not enough input to run the model'.*/
	public final static int MISSING_INPUT= 5;

/**Called by the model proxy to notify the listener in cause of <code>ACCEL_CHANGED</code>.*/
	public void accelMasterChanged(ModelProxy source);

/**Called by the model proxy to notify the listener in cause of <code>SEQUENCE_CHANGED</code>.*/
	public void accelSequenceChanged(ModelProxy source);

/**Called by the model proxy to notify the listener in cause of <code>PROBE_CHANGED</code>.*/
	public void probeMasterChanged(ModelProxy source);

/**Called by the model proxy to notify the listener in cause of <code>RESULTS_CHANGED</code>.*/
	public void modelResultsChanged(ModelProxy source);

/**Called by the model proxy to notify the listener in cause of <code>MISSING_INPUT</code>.*/
	public void missingInputToRunModel(ModelProxy source);

} ///////////////////