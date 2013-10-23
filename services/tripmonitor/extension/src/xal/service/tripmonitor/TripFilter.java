//
//  TripFilter.java
//  xal
//
//  Created by Tom Pelaia on 11/30/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;


/** filter for identifying trips */
public interface TripFilter {
	/** determine if the new value represents a new trip */
	public boolean isTripped( final int oldValue, final int newValue );
}
