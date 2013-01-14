//
// DispatchOperationListener.java
// xal
//
// Created by Tom Pelaia on 4/23/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.dispatch;


/** DispatchOperationListener */
interface DispatchOperationListener {
	public <ReturnType> void operationCompleted( final DispatchOperation<ReturnType> operation );
}
