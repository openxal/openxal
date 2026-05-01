//
//  LiveParameterInspector.java
//  xal
//
//  Created by Thomas Pelaia on 5/13/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Insets;


/** Inspector of a live parameter */
public class LiveParameterInspector extends JDialog {
	
    private final static long serialVersionUID = 1L;
    
    /** internal parameter view */
	final private LiveParameterView _parameterView;
	
	
	/** Constructor */
	public LiveParameterInspector( final EnergyManagerWindow owner ) {
		super( owner, "Node", false );
		setLocationRelativeTo( owner );
		
		_parameterView = new LiveParameterView();
		
		makeContent();
		
		pack();
		setResizable( false );
	}
	
	
	/** Make the view's contents */
	protected void makeContent() {
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		
		mainView.add( _parameterView );
		mainView.add( makeNavigationBar() );
	}
	
	
	/**
	 * Make a navigation bar
	 * @return a navigation bar
	 */
	protected Component makeNavigationBar() {
		final Box bar = new Box( BoxLayout.X_AXIS );
		final JButton previousButton = new JButton( "<" );
		final JButton nextButton = new JButton( ">" );
		
		previousButton.setMargin( new Insets( 0, 0, 0, 0 ) );
		nextButton.setMargin( new Insets( 0, 0, 0, 0 ) );
		
		bar.add( Box.createHorizontalGlue() );
		bar.add( previousButton );
		bar.add( nextButton );
		
		previousButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				((EnergyManagerWindow)LiveParameterInspector.this.getOwner()).selectPreviousParameter();
			}
		});
		
		nextButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				((EnergyManagerWindow)LiveParameterInspector.this.getOwner()).selectNextParameter();
			}
		});
		
		return bar;
	}
	
	
	/**
	 * Set the parameter to inspect.
	 * @param parameter the new parameter to inspect.
	 */
	public void setParameter( final LiveParameter parameter ) {
		setTitle( parameter != null ? parameter.getNodeAgent().getID() : "No Selection" );
		_parameterView.setParameter( parameter );
	}
}
