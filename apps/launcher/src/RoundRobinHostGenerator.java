//
//  RoundRobinHostGenerator.java
//  xal
//
//  Created by Thomas Pelaia on 9/8/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import java.util.*;
import java.net.InetAddress;


/** generates hosts via round robin */
public class RoundRobinHostGenerator implements HostGenerator {
	/** the list of available hosts */
	final protected List<HostSetting> AVAILABLE_HOSTS;
	
	/** the index of the last host selected */
	private int _lastHostIndex;
	
	
	/** Constructor */
	public RoundRobinHostGenerator( final List<HostSetting> hosts ) {
		_lastHostIndex = -1;
		AVAILABLE_HOSTS = hosts;
	}
	
	
	/** get the index of the next host */
	protected int getIndexOfNextHost() {
		final int hostCount = AVAILABLE_HOSTS.size();
		if ( AVAILABLE_HOSTS == null || hostCount == 0 ) {
			return -1;
		}
		else if ( _lastHostIndex < 0 ) {
			return hostCount > 0 ? new Random().nextInt( hostCount ) : 0;
		}
		else {
			final int lastIndex = _lastHostIndex;
			return lastIndex < hostCount - 1 ? lastIndex + 1 : 0;
		}
	}
	
	
	/** try to reach the next host */
	protected String tryNextHost( final int trial ) {
		if ( AVAILABLE_HOSTS == null || AVAILABLE_HOSTS.size() == 0 ) {
			return "127.0.0.1";		// just use the loopback
		}
		else if ( trial < AVAILABLE_HOSTS.size() ) {
			final int hostIndex = getIndexOfNextHost();
			final HostSetting hostSetting = hostIndex >= 0 ? AVAILABLE_HOSTS.get( hostIndex ) : null;
			final String host = hostSetting != null ? hostSetting.getHost() : "127.0.0.1";
			_lastHostIndex = hostIndex;
			
			// test if the host is reachable
			try {
				if ( InetAddress.getByName( host ).isReachable( 2000 ) ) {
					return host;
				}
				else {
					return tryNextHost( trial + 1 );
				}
			}
			catch( Exception exception ) {
				return tryNextHost( trial + 1 );
			}
		}
		else {
			return null;
		}
	}
	
	
	/** get the next host */
	public String nextHost() {
		return tryNextHost( 0 );
	}
	
	
	/** get the type of the host generator */
	public String getType() {
		return "Round Robin";
	}
	
	
	/** description of this generator */
	public String toString() {
		return getType();
	}
}
