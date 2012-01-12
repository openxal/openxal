//
// ConversionAdaptor.java
// 
//
// Created by Tom Pelaia on 12/27/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.coding;


/** Declares methods for converting between an object of some native type and a representation suitable for a particular tool (e.g. for archiving and unarchiving objects). */
public interface ConversionAdaptor<NativeType,RepresentationType> {
    /** convert the native type to a desired representation */
    public RepresentationType toRepresentation( final NativeType custom );
    
    
    /** convert the representation into the native type */
    public NativeType toNative( final RepresentationType primitive );
}
