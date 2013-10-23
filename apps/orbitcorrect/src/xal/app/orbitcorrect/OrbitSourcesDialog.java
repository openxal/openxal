/*
 *  OrbitSourcesDialog.java
 *
 *  Created on Mon Aug 16 13:17:49 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.extension.application.*;
import xal.smf.AcceleratorSeq;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Component;
import java.util.*;


/**
 * OrbitSourcesDialog
 *
 * @author   tap
 * @since    Aug 16, 2004
 */
public class OrbitSourcesDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
	/** the orbit model whose orbit sources are managed */
	protected OrbitModel _orbitModel;

	/** list view of orbit sources */
	protected JList<Object> _listView;

	/** list model of orbit sources */
	protected OrbitSourceListModel _listModel;


	/**
	 * Primary constructor
	 *
	 * @param orbitModel  the orbit model whose orbit sources will be managed
	 * @param owner       the owner of this dialog box
	 * @param title       the title of this dialog box
	 * @param modal       true for modal behavior
	 */
	public OrbitSourcesDialog( OrbitModel orbitModel, JFrame owner, String title, boolean modal ) {
		super( owner, title, modal );
		setSize( 300, 400 );

		setOrbitModel( orbitModel );

		makeContent();
	}


	/**
	 * Constructor with default modal behavior.
	 *
	 * @param orbitModel  the orbit model whose orbit sources will be managed
	 * @param owner       the owner of this dialog box
	 * @param title       the title of this dialog box
	 */
	public OrbitSourcesDialog( OrbitModel orbitModel, JFrame owner, String title ) {
		this( orbitModel, owner, title, true );
	}


	/**
	 * Constructor with default title and modal behavior.
	 *
	 * @param orbitModel  the orbit model whose orbit sources will be managed
	 * @param owner       the owner of this dialog box
	 */
	public OrbitSourcesDialog( OrbitModel orbitModel, JFrame owner ) {
		this( orbitModel, owner, "Manage orbit sources" );
	}


	/**
	 * Set the orbit model to that which is specified.
	 *
	 * @param orbitModel  the orbit model to use.
	 */
	public void setOrbitModel( OrbitModel orbitModel ) {
		_orbitModel = orbitModel;

		if ( _listModel != null ) {
			_listModel.setOrbitModel( orbitModel );
		}
	}


	/** Create the dialog's subviews. */
	protected void makeContent() {
		Box dialogView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( dialogView );

		Box mainView = new Box( BoxLayout.X_AXIS );
		mainView.add( createListView() );
		mainView.add( createButtonView() );
		
		dialogView.add( mainView );
		dialogView.add( createConfirmBar() );
	}


	/**
	 * Create view to display the list of orbit sources.
	 *
	 * @return   a scroll pane containing the list of view of orbit sources
	 */
	protected Component createListView() {
		_listModel = new OrbitSourceListModel( _orbitModel );
		_listView = new JList<Object>( _listModel );
		return new JScrollPane( _listView );
	}


	/**
	 * Create view buttons for controlling the dialog.
	 *
	 * @return   a view of buttons
	 */
	protected Component createButtonView() {
		Box buttonView = new Box( BoxLayout.Y_AXIS );
		
		final JButton deleteSourceButton = new JButton( "Delete" );
		buttonView.add( deleteSourceButton );
		deleteSourceButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final OrbitSource selection = (OrbitSource)_listView.getSelectedValue();
				_orbitModel.removeOrbitSource(selection);
			}
		});
		
		
		final JToggleButton enableSourceButton = new JToggleButton( "Enable" );
		buttonView.add( enableSourceButton );
		enableSourceButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final OrbitSource selection = (OrbitSource)_listView.getSelectedValue();
				selection.setEnabled( enableSourceButton.isSelected() );
				enableSourceButton.setText( enableSourceButton.isSelected() ? "Enabled" : "Enable" );
			}
		});
		
		final JButton editSourceButton = new JButton( "Edit" );
		buttonView.add( editSourceButton );
		editSourceButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final OrbitSource selection = (OrbitSource)_listView.getSelectedValue();
				if ( ! ( selection instanceof SnapshotOrbitSource ) )  {
					JOptionPane.showMessageDialog( OrbitSourcesDialog.this, "Only snapshot orbits are editable!", "Edit Failure", JOptionPane.ERROR_MESSAGE );
					editSourceButton.setEnabled( false );
					return;
				}
				final MutableOrbit orbit = new MutableOrbit( selection.getOrbit() );
				final OrbitEditor.CloseStatus status = OrbitEditor.showEditor( OrbitSourcesDialog.this, orbit );
				switch ( status ) {
					case OKAY:
						((SnapshotOrbitSource)selection).setSnapshot( orbit.getOrbit() );
						break;
					case CANCELED:
						break;
					default:
						break;
				}				
			}
		});
		
		_listView.addListSelectionListener( new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if ( !event.getValueIsAdjusting() ) {
					final OrbitSource selection = (OrbitSource)_listView.getSelectedValue();
					deleteSourceButton.setEnabled( selection != null );
					enableSourceButton.setEnabled( selection != null );
					enableSourceButton.setSelected( selection != null && selection.isEnabled() );
					enableSourceButton.setText( enableSourceButton.isSelected() ? "Enabled" : "Enable" );
					editSourceButton.setEnabled( selection != null && selection instanceof SnapshotOrbitSource );
				}
			}
		});
		final OrbitSource selection = (OrbitSource)_listView.getSelectedValue();
		deleteSourceButton.setEnabled( selection != null );
		enableSourceButton.setEnabled( selection != null );
		enableSourceButton.setSelected( selection != null && selection.isEnabled() );
		enableSourceButton.setText( enableSourceButton.isSelected() ? "Enabled" : "Enable" );
		editSourceButton.setEnabled( selection != null && selection instanceof SnapshotOrbitSource );

		buttonView.add( Box.createVerticalGlue() );

		return buttonView;
	}
	
	
	/**
	 * Create the confirmation bar.
	 *
	 * @return a bar of confirmation buttons
	 */
	protected Component createConfirmBar() {
		Box confirmBar = new Box( BoxLayout.X_AXIS );
		confirmBar.setBorder( BorderFactory.createEtchedBorder() );
		
		confirmBar.add( Box.createHorizontalGlue() );
		
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setVisible( false );
				dispose();
			}
		});
		
		confirmBar.add( closeButton );
		
		return confirmBar;
	}


	/**
	 * Show this dialog window near the specified component
	 *
	 * @param component  the component near which to display this dialog
	 */
	public void showNear( Component component ) {
		setLocationRelativeTo( component );
		setVisible( true );
	}
}

