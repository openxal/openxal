/*
 * ScoreRow.java
 *
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.score;

import java.math.BigDecimal;
import java.text.*;

/**
 * ScoreRow is a representation of the data associated with a row of score data
 * It is used for transfer into and out of the database.
 * The data is stored as Strings (as used in Oracle) and converted to the proper type
 * required by score, in the "get" methods
 *
 * @author jdg
 */
public class ScoreRow {
	final private DataTypeAdaptor DATA_TYPE_ADAPTOR;
	final protected String _rbName;
	final protected String _spName;
	final protected Object _rbVal;
	final protected Object _spVal;
	final protected String _useRB;
	final protected String _system;
	final protected String _subSystem;
	
	/** format for displaying in scientific notation */
	private DecimalFormat scientificFormat = new DecimalFormat("0.000E0");;
	
	/** format for displaying in float notation */
	private DecimalFormat floatFormat = new DecimalFormat("0.00000");
	
	
	/** Primary constructor of a Row. */
	public ScoreRow( final String sys, final String subSystem, final DataTypeAdaptor dataTypeAdaptor, final String rbName, final Object rbVal, String spName, final Object spVal, final String useRB ) {
		_system = sys;
		_subSystem = subSystem;
		DATA_TYPE_ADAPTOR = dataTypeAdaptor;
		_rbVal = rbVal;
		_spName = spName;
		_rbName = rbName;
		_spVal = spVal;
		_useRB = useRB;
	}
	
	
	/** create a row with no saved values */
	public ScoreRow( final String sys, final String subSystem, final DataTypeAdaptor dataTypeAdaptor, final String rbName, final String spName, final String useRB ) {
		this( sys, subSystem,  dataTypeAdaptor, rbName, dataTypeAdaptor.empty(), spName, dataTypeAdaptor.empty(), useRB );
	}
	
	
	/** Get an instance of this class where the data values are specified in string representation  */
	static public ScoreRow getInstanceWithStringRepValues( final String sys, final String subSystem, final DataTypeAdaptor dataTypeAdaptor, final String rbName, final String rbVal, String spName, final String spVal, final String useRB ) {
		final Object readbackValue = dataTypeAdaptor.parse( rbVal );
		final Object savepointValue = dataTypeAdaptor.parse( spVal );
		return new ScoreRow( sys, subSystem,  dataTypeAdaptor, rbName, readbackValue, spName, savepointValue, useRB );
	}
	
	
	/** get the data type adaptor */
	public DataTypeAdaptor getDataTypeAdaptor() {
		return DATA_TYPE_ADAPTOR;
	}
	
	
	/**
	 * Get the readback PV name
	 * @return the name */
	public String getRBName() {
		return _rbName;
	}
	
	
	/**
	 * Get the setpoint PV name
	 * @return the name */
	public String getSPName() {
		return _spName;
	}
	
	
	/**
	 * Get the readback PV value
	 * @return the name */
	public Object getRBValue() {
		return _rbVal;
	}
	
	
	/**
	 * Get the setpoint PV value
	 * @return the name */
	public Object getSPValue() {
		return _spVal;
	}
	
	
	/**
	 * Get the setpoint PV value as a string representation which the database can handle
	 * @return the name */
	public String  getSPValueAsString() {
		return DATA_TYPE_ADAPTOR.asString( _spVal );
	}

	
	/**	
	 * Get the readback PV value as a string representation which the database can handle
	 * @return the name */
	public String getRBValueAsString() {
		return DATA_TYPE_ADAPTOR.asString( _rbVal );
	}
	
	
	/**
	 * Get the value of the system
	 * @return the system name */
	public String getSystem() {
		return _system;
	}
	
	
	/**
	 * Get the value of the system
	 * @return the system name */
	public String getSubSystem() {
		return _subSystem;
	}
	
	
	/**
	 * should the rb value be used for the SP restore ? 
	 */
	public boolean useRB() {
		boolean tf = 
		_useRB.equalsIgnoreCase("Y") ? true : false;
		return tf;
	}
	
	
	/**
	 * return a Y / N string indicator for whether the RB should be used for the SP restore ? 
	 */
	public String getUseRB() {
		return _useRB;
	}
	
	
	/** dump to screen */
	public void print() {
		System.out.println("Sys = " + getSystem() + " subsys = " + getSubSystem() + " spname = " + getSPName() + " spVal = " + getSPValueAsString() + " rbname = " + getRBName() + " rbVal = " + getRBValueAsString() + " urbasp = " + _useRB);
	}
}

