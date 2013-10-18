//
//  PropertyTableModel.java
//  xal
//
//  Created by Thomas Pelaia on 7/17/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;


/** interface for table models implementing property display support */
public interface PropertyTableModel {
	/** get the property descriptor for the specified row */
	public Class<?> getPropertyClass( final int row );
}
