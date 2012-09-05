//
// RemoteCacheConfig.java
// Open XAL
//
// Created by Pelaia II, Tom on 9/4/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;


/** RemoteCacheConfig */
public class RemoteCacheConfig {
	/** seconds after which data is marked stale */
	private double _expirationLength;
	

	/**
	 * Primary Constructor
	 * @param expirationLength seconds after which data is marked stale
	 */
    public RemoteCacheConfig( final double expirationLength ) {
		_expirationLength = expirationLength;
    }


	/** Constructor with default expiration */
    public RemoteCacheConfig() {
		this( 5.0 );
    }


	/** set the expiration length in seconds */
	public void setExpirationLength( final double expirationLength ) {
		_expirationLength = expirationLength;
	}


	/** Get the expiration lenght in seconds */
	public double getExpirationLength() {
		return _expirationLength;
	}
}
