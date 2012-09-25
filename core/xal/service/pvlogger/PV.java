/*
 * PV.java
 *
 * Created on Fri Dec 12 15:02:32 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;


/**
 * PV
 *
 * @author  tap
 */
public class PV {
	protected long _id;
	protected String _address;
	
	
	/**
	 * Constructor
	 * @param id the unique identifier
	 * @param address the PV address
	 */
	public PV(long id, String address) {
		_id = id;
		_address = address;
	}
	
	
	/**
	 * Get the id
	 * @return the id
	 */
	public long getId() {
		return _id;
	}
	
	
	/**
	 * Get the PV address
	 * @return the PV address
	 */
	String getAddress() {
		return _address;
	}
}

