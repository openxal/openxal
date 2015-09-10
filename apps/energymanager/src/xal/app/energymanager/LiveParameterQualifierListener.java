//
//  LiveParameterQualifierListener.java
//  xal
//
//  Created by Thomas Pelaia on 6/13/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;


/** interface for listeners of parameter qualifier events */
public interface LiveParameterQualifierListener {
	public void qualifierChanged( LiveParameterQualifier qualifier );
}
