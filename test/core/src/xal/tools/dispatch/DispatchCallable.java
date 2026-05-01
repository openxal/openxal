//
// DispatchCallable.java
// xal
//
// Created by Tom Pelaia on 5/12/14
// Copyright 2014 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.dispatch;

import java.util.concurrent.Callable;


/** Convenience interface so the dispatch package is self-contained */
public interface DispatchCallable<ReturnType> extends Callable<ReturnType> {
}