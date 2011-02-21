//
//  TrimmedMagnet.java
//  xal
//
//  Created by Tom Pelaia on 11/10/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.smf.impl;


/** interface for trimmable magnets */
public interface TrimmedMagnet {
    /**
	 * Get the trim power supply for this magnet.
     * @return The trim power supply for this magnet
     */
    abstract public MagnetTrimSupply getTrimSupply();
}
