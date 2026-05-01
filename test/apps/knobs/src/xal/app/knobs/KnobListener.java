//
//  KnobListener.java
//  xal
//
//  Created by Thomas Pelaia on 11/1/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;


/** Knob events */
public interface KnobListener {
	/** event indicating that the specified knob's name has changed */
	public void nameChanged( final Knob knob, final String newName );
	
	
	/** event indicating that the knob's ready state has changed */
	public void readyStateChanged( final Knob knob, final boolean isReady );
	
	
	/** event indicating that the knob's limits have changed */
	public void limitsChanged( final Knob knob, final double lowerLimit, final double upperLimit );
	
	
	/** event indicating that the knob's current value setting has changed */
	public void currentSettingChanged( final Knob knob, final double value );
	
	
	/** event indicating that the knob's most previously pending set operation has completed */
	public void valueSettingPublished( final Knob knob );
	
	
	/** event indicating that an element has been added */
	public void elementAdded( final Knob knob, final KnobElement element );
	
	
	/** event indicating that an element has been removed */
	public void elementRemoved( final Knob knob, final KnobElement element );
	
	
	/** event indicating that the specified knob element has been modified */
	public void elementModified( final Knob knob, final KnobElement element );
}
