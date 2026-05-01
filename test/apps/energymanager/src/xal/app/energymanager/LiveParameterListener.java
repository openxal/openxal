//
//  LiveParameterListener.java
//  xal
//
//  Created by Thomas Pelaia on 4/25/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;


/** Listener of live parameter events. */
public interface LiveParameterListener {
	/**
	 * Handle the event indicating that parameter's control channel connection has changed.
	 * @param parameter the live parameter whose connection has changed.
	 * @param isConnected true if the channel is now connected and false if it is disconnected
	 */
	public void controlConnectionChanged( LiveParameter parameter, boolean isConnected );
	
	
	/** 
	 * Handle the event indicating that parameter's readback channel connection has changed.
	 * @param parameter the live parameter whose connection has changed.
	 * @param isConnected true if the channel is now connected and false if it is disconnected
	 */
	public void readbackConnectionChanged( LiveParameter parameter, boolean isConnected );
	
	
	/** 
	 * Handle the event indicating that parameter's control value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new control value of the parameter
	 */
	public void controlValueChanged( LiveParameter parameter, double value );
	
	
	/**
	 * Handle the event indicating that parameter's readback value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new readback value of the parameter
	 */
	public void readbackValueChanged( LiveParameter parameter, double value );
	
	
	/**
	 * Handle the event in which the parameter's variable status has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param isVariable indicates whether the parameter is now variable or not
	 */
	public void variableStatusChanged( LiveParameter parameter, boolean isVariable );
	
	
	/**
	 * Handle the event in which the parameter's custom value has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param value the new custom value
	 */
	public void customValueChanged( LiveParameter parameter, double value );
	
	
	/**
	 * Handle the event in which the parameter's custom limits have changed.
	 * @param parameter the core parameter whose limits have changed.
	 * @param limits the new custom limits
	 */
	public void customLimitsChanged( LiveParameter parameter, double[] limits );
	
	
	/**
	 * Handle the event in which the parameter's initial variable value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new initial variable value
	 */
	public void initialValueChanged( LiveParameter parameter, double value );
	
	
	/**
	 * Handle the event in which the parameter's lower variable limit has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param limit the new lower variable limit
	 */
	public void lowerLimitChanged( LiveParameter parameter, double limit );
	
	
	/**
	 * Handle the event in which the parameter's upper variable limit has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param limit the new upper variable limit
	 */
	public void upperLimitChanged( LiveParameter parameter, double limit );
	
	
	/**
	 * Handle the event in which the parameter's variable source has changed.
	 * @param parameter the live parameter whose variable source has changed.
	 * @param source the indicator of the parameter's variable source
	 */
	public void variableSourceChanged( LiveParameter parameter, int source );
}



