//
//  CoreParameterListener.java
//  xal
//
//  Created by Thomas Pelaia on 6/8/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;


/** Listener of core parameter events. */
public interface CoreParameterListener {
	/**
	 * Handle the event in which the parameter's variable status has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param isVariable indicates whether the parameter is now variable or not
	 */
	public void variableStatusChanged( CoreParameter parameter, boolean isVariable );
	
	
	/**
	 * Handle the event in which the parameter's custom value has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param value the new custom value
	 */
	public void customValueChanged( CoreParameter parameter, double value );
	
	
	/**
	 * Handle the event in which the parameter's custom limits have changed.
	 * @param parameter the core parameter whose limits have changed.
	 * @param limits the new custom limits
	 */
	public void customLimitsChanged( CoreParameter parameter, double[] limits );
	
	
	/**
	 * Handle the event in which the parameter's initial variable value has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param value the new initial variable value
	 */
	public void initialValueChanged( CoreParameter parameter, double value );
	
	
	/**
	 * Handle the event in which the parameter's lower variable limit has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param limit the new lower variable limit
	 */
	public void lowerLimitChanged( CoreParameter parameter, double limit );
	
	
	/**
	 * Handle the event in which the parameter's upper variable limit has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param limit the new upper variable limit
	 */
	public void upperLimitChanged( CoreParameter parameter, double limit );	
	
	
	/**
	 * Handle the event in which the parameter's variable source has changed.
	 * @param parameter the core parameter whose variable source has changed.
	 * @param source the indicator of the parameter's variable source
	 */
	public void variableSourceChanged( CoreParameter parameter, int source );
}
