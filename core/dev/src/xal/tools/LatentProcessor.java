//
//  LatentProcessor.java
//  xal
//
//  Created by Tom Pelaia on 5/21/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//

package xal.tools;

import java.util.concurrent.*;


/** Process events with latency and replace any pending requests with the latest request */
public class LatentProcessor extends FreshProcessor {
	/** millisecond portion of latency */
	private final long LATENCY_MILLISECONDS;
	
	/** nanosecond portion of latency */
	private final int LATENCY_NANOSECONDS;
	
	
	/** 
	 * Constructor 
	 * @param latency Latency in seconds between successive processing of requests. The latency should be positive.
	 */
	public LatentProcessor( final double latency ) {
		if ( latency < 0.0 )  throw new RuntimeException( "Latency must be greater than or equal to zero seconds. The supplied latency was: " + latency );
		
		final double latencyMilliseconds = 1000.0 * latency;		// convert from seconds to milliseconds
		LATENCY_MILLISECONDS = (long) ( latencyMilliseconds );		// millisecond portion of latency
		
		final double remainderNanos = 1.0e6 * ( latencyMilliseconds - LATENCY_MILLISECONDS );	// get the nanosecond remainder
		LATENCY_NANOSECONDS = (int)( remainderNanos + 0.5 );		// nanosecond portion of latency rounded up
	}
	
	
	/**
	 * Get the latency
	 * @return latency in seconds
	 */
	public double getLatency() {
		return 1.0e-3 * LATENCY_MILLISECONDS + 1.0e-9 * LATENCY_NANOSECONDS;
	}
	
	
	/** Perform post processing */
	protected void postProcess() throws Exception {
		Thread.sleep( LATENCY_MILLISECONDS, LATENCY_NANOSECONDS );
	}
}
