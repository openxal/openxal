/*
 * TraceModel.java
 *
 * Created on Wed Jan 07 13:45:30 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;


/**
 * TraceModel
 *
 * @author  tap
 */
public class TraceModel {
	protected TraceSource _traceSource;
	protected boolean _isVisible;
	
	
	/**
	 *
	 */
	public TraceModel(final TraceSource traceSource) {
		_traceSource = traceSource;
	}
	
	
	/**
	 *
	 */
	public String getLabel() {
		return "";
	}
	
	
	/**
	 *
	 */
	public boolean isVisible() {
		return _isVisible;
	}
	
	
	/**
	 *
	 */
	public void setVisible(boolean visible) {
		_isVisible = visible;
	}
	
	
	/**
	 *
	 */
	public TraceSource getTraceSource() {
		return _traceSource;
	}
	
	
	/**
	 *
	 */
	public String getDescription() {
		return toString();
	}
}

