//
//  SimulatorView.java
//  xal
//
//  Created by Thomas Pelaia on 7/13/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import java.util.logging.*;
import java.io.*;
import java.net.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Dimension;


/** View for displaying simulator settings. */
public class SimulatorView extends Box {
    
    private static final long serialVersionUID = 1L;
    
	/** the machine simulator */
	final protected MachineSimulator _simulator;
	
	
	/** Constructor */
	public SimulatorView( final MachineSimulator simulator ) {
		super( BoxLayout.Y_AXIS );
		
		_simulator = simulator;
		
		makeContent();
	}
	
	
	/** make this view's content */
	protected void makeContent() {
	}
	
	
	/** apply this view's settings to the simulator */
	public void applySettings() {}
	
	
	/** refresh this view's settings to reflect its simulator's settings */
	public void refreshSettings() {}
}
