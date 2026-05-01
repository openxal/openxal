//
//  KnobView.java
//  xal
//
//  Created by Thomas Pelaia on 12/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.extension.application.Application;
import xal.tools.IconLib;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


/** View for displaying knob detail */
public class KnobView extends Box implements KnobListener {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	/** the knob to display */
	final protected Knob _knob;
	
	/** displays and hides the knob editor */
	final protected KnobEditDisplayer EDIT_DISPLAYER;
	
	/** knob editor */
	protected KnobControl _knobControl;
	
	/** knob conrol */
	protected KnobEditor _knobEditor;
	
	
	/** Constructor */
	public KnobView( final Knob knob, final KnobEditDisplayer editDisplayer ) {
		super( BoxLayout.Y_AXIS );
		
		_knob = knob;
		EDIT_DISPLAYER = editDisplayer;
				
		add( makeButtonRow() );
		add( new KnobControl( knob ) );
		
		setBorder( BorderFactory.createTitledBorder( knob.getName() ) );
		
		knob.addKnobListener( this );
		displayEditor( false );
	}
	
	
	/** set to edit mode */
	protected void displayEditor( final boolean displayEditor ) {
		if ( displayEditor ) {
			if ( _knobEditor == null ) {
				_knobEditor = new KnobEditor( _knob, EDIT_DISPLAYER );
			}
			
			EDIT_DISPLAYER.display( _knobEditor );
		}
	}
	
	
	/** make the button row */
	protected Component makeButtonRow() {
		final Box row = new Box( BoxLayout.X_AXIS );
		final JButton editButton = new JButton( IconLib.getIcon( IconLib.IconGroup.GENERAL, "Edit24.gif" ) );
		editButton.setToolTipText( "Edit the knob definition." );
		
		editButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				displayEditor( true );
			}
		});
		
		row.add( editButton );
		row.add( Box.createHorizontalGlue() );
		
		return row;
	}
	
	
	/** event indicating that the specified knob's name has changed */
	public void nameChanged( final Knob knob, final String newName ) {
		((TitledBorder)getBorder()).setTitle( newName );
		repaint();
		validate();
	}
	
	
	/** ready state changed */
	public void readyStateChanged( final Knob knob, final boolean isReady ) {}
	
	
	/** event indicating that the knob's limits have changed */
	public void limitsChanged( final Knob knob, final double lowerLimit, final double upperLimit ) {}
	
	
	/** event indicating that the knob's current value setting has changed */
	public void currentSettingChanged( final Knob knob, final double value ) {}
	
	
	/** event indicating that an element has been added */
	public void elementAdded( final Knob knob, final KnobElement element ) {}
	
	
	/** event indicating that an element has been removed */
	public void elementRemoved( final Knob knob, final KnobElement element ) {}
	
	
	/** event indicating that the specified knob element has been modified */
	public void elementModified( final Knob knob, final KnobElement element ) {}
	
	
	/** event indicating that the knob's most previously pending set operation has completed */
	public void valueSettingPublished( final Knob knob ) {}
}
