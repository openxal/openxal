//
//  BumpGeneratorListener.java
//  xal
//
//  Created by Thomas Pelaia on 3/13/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.smf.AcceleratorNode;

import java.util.List;


/** interface used to handle bump generator events */
public interface BumpGeneratorListener {
	/** handle the event indicating that a new knob is about to be generated */
	public void willGenerateKnob( final BumpGenerator generator, final AcceleratorNode node );
	
	
	/** handle event indicating that the knob has been generated */
	public void knobGenerated( final BumpGenerator generator, final Knob knob );
	
	
	/** handle event indicating that the generator failed to make the bump for the specified node */
	public void knobGeneratorException( final BumpGenerator generator, final AcceleratorNode node, final Exception exception );
	
	
	/** handle event indicating that the knob generation is complete */
	public void knobGenerationComplete( final BumpGenerator generator, final List<Knob> knobs );
	
	
	/** handle event indicating that the knob generation failed */
	public void knobGenerationFailed( final BumpGenerator generator, final Exception exception );
}
