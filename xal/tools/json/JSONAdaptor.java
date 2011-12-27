//
// JSONAdaptor.java
// 
//
// Created by Tom Pelaia on 12/27/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.json;


/** JSONAdaptor declares methods for converting between a custom object and a representation construct which can be coded in JSON */
public interface JSONAdaptor<CustomType,RepresentationType> {
    /** convert the custom type to a representation in terms of JSON constructs */
    public RepresentationType toRepresentation( final CustomType custom );
    
    
    /** convert the JSON representatino into the custom type */
    public CustomType toCustom( final RepresentationType primitive );
}
