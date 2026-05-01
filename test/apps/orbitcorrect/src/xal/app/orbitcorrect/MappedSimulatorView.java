//
//  MappedSimulatorView.java
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


/** View for displaying Mapped simulator settings. */
public class MappedSimulatorView extends SimulatorView {
	
    private static final long serialVersionUID = 1L;

    /** Constructor */
	public MappedSimulatorView( final MappedSimulator simulator ) {
		super( simulator );
	}
	
	
	/** Convenience method for getting the mapped simulator. */
	protected MappedSimulator getMappedSimulator() {
		return (MappedSimulator)_simulator;
	}
	
	
	/** make this view's content */
	protected void makeContent() {
		super.makeContent();
		
		add( Box.createVerticalStrut( 10 ) );
		
		final Box row = new Box( BoxLayout.X_AXIS );
		add( row );
		JButton clearButton = new JButton( "clear map" );
		row.add( clearButton );
		clearButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				getMappedSimulator().clear();
			}
		});
		
		row.add( Box.createHorizontalGlue() );
	}
}
