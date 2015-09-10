//
//  EnergyManagerListener.java
//  xal
//
//  Created by Thomas Pelaia on 4/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import java.util.List;

import xal.model.probe.Probe;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;


/** Interface for receivers of energy manager events. */
public interface EnergyManagerListener {
	/**
	 * Handle the event indicating that the list of evaluation nodes has changed.
	 * @param model the model posting the event
	 * @param range the new position range of evaluation nodes (first position, last position)
	 * @param nodes the new evaluation nodes
	 */
	public void evaluationNodesChanged( EnergyManager model, double[] range, List<AcceleratorNode> nodes );
	
	
	/**
	 * Handle the event indicating that the model's entrance probe has changed.
	 * @param model the model posting the event
	 * @param entranceProbe the new entrance probe
	 */
	public void entranceProbeChanged( EnergyManager model, Probe<?> entranceProbe );
	
	
	/** 
	 * Handle the event indicating that the model's sequence has changed. 
	 * @param model the model posting the event
	 * @param sequence the model's new sequence
	 * @param nodeAgents the model's node agents
	 * @param parameters the list of live parameters
	 */
	public void sequenceChanged( EnergyManager model, AcceleratorSeq sequence, List<NodeAgent> nodeAgents, List<LiveParameter> parameters );
	
	
	/**
	 * Event indicating that a live parameter has been modified.
	 * @param model the source of the event.
	 * @param parameter the parameter which has changed.
	 */
	public void liveParameterModified( EnergyManager model, LiveParameter parameter );
	
	
	/**
	 * Event indicating that the optimizer settings have changed.
	 * @param model the source of the event.
	 * @param optimizer the optimizer whose settings have changed.
	 */
	public void optimizerSettingsChanged( EnergyManager model, OpticsOptimizer optimizer );
	
	
	/**
	 * Event indicating that the optimizer has found a new optimal solution.
	 * @param model the source of the event.
	 * @param optimizer the optimizer which has found a new optimial solution.
	 */
	public void newOptimalSolutionFound( EnergyManager model, OpticsOptimizer optimizer );
	
	
	/**
	 * Event indicating that the optimizer has started.
	 * @param model the source of the event.
	 * @param optimizer the optimizer which has started.
	 */
	public void optimizerStarted( EnergyManager model, OpticsOptimizer optimizer );
}
