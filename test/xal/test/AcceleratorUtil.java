//
// AcceleratorUtil.java
// Open XAL 
//
// Created by Tom Pelaia on 9/13/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.test;

import xal.smf.data.XMLDataManager;

import xal.smf.Accelerator;

import java.util.*;
import java.net.URL;


/** AcceleratorUtil is not a test suite, but rather it is an Accelerator utility used by test suites which reference an accelerator. */
public class AcceleratorUtil {
    /** Load and get the default test accelerator */
    static public Accelerator getTestAccelerator() {
        return getAcceleratorForResource( "/xal/config/main.xal" );
    }
    
    
    /** 
     * Load and get the accelerator at the specified resource path
     * @param resourcePath fully qualified modified package path to the resource (see the notes for the getResource() method of java.lang.Class)
     */
    static public Accelerator getAcceleratorForResource( final String resourcePath ) {
        final URL opticsURL = AcceleratorUtil.class.getResource( resourcePath );
        return opticsURL != null ? XMLDataManager.getInstance( opticsURL ).getAccelerator() : null;
    }
}
