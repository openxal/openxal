//
// MonitorModelListener.java
// Open XAL
//
// Created by Pelaia II, Tom on 9/4/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import java.util.*;


/** MonitorModelListener */
public interface MonitorModelListener {
	/** event indicating that the list of remote apps has changed */
	public void remoteAppsChanged( final MonitorModel model, final List<RemoteAppRecord> remoteApps );
}
