//
// OneWay.java
// 
//
// Created by Tom Pelaia on 1/5/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.service;

import java.lang.annotation.*;


/** Indicates that a remote method is one-way and will not return so the caller should not wait for it */
@Retention( RetentionPolicy.RUNTIME )
public @interface OneWay {}
