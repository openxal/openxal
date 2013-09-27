//
// AProperty.java
//
// Created by Tom Pelaia on 9/27/2013
// Copyright 2013 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.annotation;

import java.lang.annotation.*;


/** Annotation group for a property */
@Retention( RetentionPolicy.RUNTIME )	// make this annotation available at runtime via reflection
public @interface AProperty {
	/** Specifies units for the property accessor */
	@Retention( RetentionPolicy.RUNTIME )	// make this annotation available at runtime via reflection
	public @interface Units {
		String value() default "";
	}

	/** Specifies that the property should not be edited regardless of whether it is public */
	@Retention( RetentionPolicy.RUNTIME )	// make this annotation available at runtime via reflection
	public @interface NoEdit {}
}