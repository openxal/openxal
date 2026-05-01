/*
 *  OrbitSourceEditor.java
 *
 *  Created on Thu Aug 19 10:48:20 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.service.pvlogger.*;
import xal.tools.database.*;
import xal.smf.impl.BPM;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.util.*;
import java.util.logging.*;
import java.sql.Connection;


/**
 * OrbitSourceEditor
 *
 * @author   tap
 * @since    Aug 19, 2004
 */
public class OrbitSourceEditor extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
	/** live orbit type */
	public final static int LIVE_TYPE = 0;

	/** orbit difference type */
	public final static int DIFFERENCE_TYPE = 1;

	/** static orbit type */
	public final static int SNAPSHOT_TYPE = 2;

	/** logged orbit type for orbits logged by pvlogger */
	public final static int LOGGED_TYPE = 3;
	
	/** user defined orbit */
	public final static int USER_DEFINED_TYPE = 4;

	/** the orbit model */
	protected final OrbitModel _orbitModel;

	/** the editor for the orbit source */
	protected OrbitSourceView _sourceEditor;

	
	/**
	 * Primary constructor
	 *
	 * @param orbitType   identifies the type of orbit source to edit
	 * @param orbitModel  the orbit model
	 * @param owner       the owner of this dialog box
	 * @param title       the title of this dialog box
	 * @param modal       true for modal behavior
	 */
	public OrbitSourceEditor( final int orbitType, final OrbitModel orbitModel, final JFrame owner, final String title, final boolean modal ) {
		super( owner, title, modal );
		setSize( 300, 400 );

		_orbitModel = orbitModel;
		makeContent( orbitType );
	}

	
	/**
	 * Constructor with default modal behavior.
	 *
	 * @param orbitType   identifies the type of orbit source to edit
	 * @param orbitModel  the orbit model
	 * @param owner       the owner of this dialog box
	 * @param title       the title of this dialog box
	 */
	public OrbitSourceEditor( final int orbitType, final OrbitModel orbitModel, final JFrame owner, final String title ) {
		this( orbitType, orbitModel, owner, title, true );
	}

	
	/**
	 * Constructor with default title and modal behavior.
	 *
	 * @param orbitType   identifies the type of orbit source to edit
	 * @param orbitModel  the orbit model
	 * @param owner       the owner of this dialog box
	 */
	public OrbitSourceEditor( final int orbitType, final OrbitModel orbitModel, final JFrame owner ) {
		this( orbitType, orbitModel, owner, "Orbit Source Editor" );
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
	

	/**
	 * Create the dialog's subviews.
	 *
	 * @param orbitType  Description of the Parameter
	 */
	protected void makeContent( final int orbitType ) {
		Box dialogView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( dialogView );

		dialogView.add( createEditorView( orbitType ) );
		dialogView.add( createConfirmBar() );

		pack();
	}

	
	/**
	 * Create the confirmation bar.
	 *
	 * @return   a bar of confirmation buttons
	 */
	protected Component createConfirmBar() {
		Box confirmBar = new Box( BoxLayout.X_AXIS );
		confirmBar.setBorder( BorderFactory.createEtchedBorder() );

		confirmBar.add( Box.createHorizontalGlue() );

		JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					setVisible( false );
					dispose();
				}
			} );
		confirmBar.add( cancelButton );

		JButton okayButton = new JButton( "Okay" );
		okayButton.addActionListener(
			new ActionListener() {
				public void actionPerformed( ActionEvent event ) {
					if ( _sourceEditor != null ) {
						OrbitSource orbitSource = _sourceEditor.getOrbitSource();
						_orbitModel.addOrbitSource( orbitSource );
					}
					setVisible( false );
					dispose();
				}
			} );
		confirmBar.add( okayButton );
		getRootPane().setDefaultButton( okayButton );
		
		return confirmBar;
	}

	
	/**
	 * Create the view for editing the orbit source for the specified orbit type.
	 *
	 * @param ORBIT_TYPE  the type of orbit source to edit
	 * @return            a component for editing the orbit source
	 */
	protected Component createEditorView( final int ORBIT_TYPE ) {
		switch ( ORBIT_TYPE ) {
			case LIVE_TYPE:
				_sourceEditor = new LiveOrbitSourceView( _orbitModel );
				break;
			case DIFFERENCE_TYPE:
				_sourceEditor = new DifferenceOrbitSourceView( _orbitModel );
				break;
			case SNAPSHOT_TYPE:
				_sourceEditor = new SnapshotOrbitSourceView( _orbitModel );
				break;
			case LOGGED_TYPE:
				_sourceEditor = new LoggedOrbitSourceView( _orbitModel, this );
				break;
			case USER_DEFINED_TYPE:
				_sourceEditor = new UserDefinedOrbitSourceView( _orbitModel, this );
                break;
			default:
				break;
		}

		return ( _sourceEditor != null ) ? _sourceEditor.getComponent() : null;
	}
}


/** Orbit Source View */
abstract class OrbitSourceView {

	/** the generated orbit source */
	protected OrbitSource _orbitSource;

	/** the editor view */
	protected Box _editor;

	/** the field for editing the label */
	protected JTextField _labelField;


	/**
	 * Constructor
	 *
	 * @param orbitModel  Description of the Parameter
	 */
	public OrbitSourceView( final OrbitModel orbitModel ) {
		makeOrbitSource( orbitModel );
		makeContent( orbitModel );
	}


	/**
	 * Get the orbit source
	 *
	 * @return   The orbitSource value
	 */
	public OrbitSource getOrbitSource() {
		applySettings();
		return _orbitSource;
	}


	/**
	 * Get the component for this view.
	 *
	 * @return   this view's component
	 */
	public Component getComponent() {
		return _editor;
	}

	
	/** Apply user settings to the orbit source. */
	public void applySettings() {
		_orbitSource.setLabel( _labelField.getText() );
	}


	/**
	 * Instantiate the new orbit source. Subclasses should override this method to instantiate
	 * their specific orbit source.
	 *
	 * @param orbitModel  The orbit model.
	 */
	protected abstract void makeOrbitSource( final OrbitModel orbitModel );

	
	/**
	 * Make the editor content
	 *
	 * @param orbitModel  The orbit model.
	 */
	protected void makeContent( final OrbitModel orbitModel ) {
		_editor = new Box( BoxLayout.Y_AXIS );
		_editor.setBorder( BorderFactory.createEtchedBorder() );

		_labelField = new JTextField( 20 );
		_editor.add( _labelField );
		_labelField.setText( _orbitSource.getLabel() );
	}
}


/** Live Orbit Source View */
class LiveOrbitSourceView extends OrbitSourceView {

	/**
	 * Constructor
	 *
	 * @param orbitModel  The orbit model
	 */
	public LiveOrbitSourceView( final OrbitModel orbitModel ) {
		super( orbitModel );
	}

	
	/**
	 * Instantiate the new orbit source.
	 *
	 * @param orbitModel  The orbit model.
	 */
	protected void makeOrbitSource( final OrbitModel orbitModel ) {
		_orbitSource = new LiveOrbitSource( "Live Orbit", orbitModel.usesBeamEventTrigger() );
	}
}


/** Difference Orbit Source View */
class DifferenceOrbitSourceView extends OrbitSourceView {
	protected JComboBox<OrbitSource> _primarySourceMenu;
	protected JComboBox<OrbitSource> _referenceSourceMenu;
	
	
	/**
	 * Constructor
	 *
	 * @param orbitModel  The orbit model
	 */
	public DifferenceOrbitSourceView( final OrbitModel orbitModel ) {
		super( orbitModel );
	}

	
	/**
	 * Instantiate the new orbit source.
	 *
	 * @param orbitModel  The orbit model.
	 */
	protected void makeOrbitSource( final OrbitModel orbitModel ) {
		_orbitSource = new OrbitDifferenceSource( "Difference Orbit" );
	}

	
	/** Apply user settings to the orbit source. */
	public void applySettings() {
		super.applySettings();
		
		OrbitSource primarySource = (OrbitSource)_primarySourceMenu.getSelectedItem();
		OrbitSource referenceSource = (OrbitSource)_referenceSourceMenu.getSelectedItem();
		
		OrbitDifferenceSource orbitSource = (OrbitDifferenceSource)_orbitSource;
		
		orbitSource.setPrimarySource( primarySource );
		orbitSource.setReferenceSource( referenceSource );
	}

	
	/**
	 * Make the editor content
	 *
	 * @param orbitModel  The orbit model.
	 */
	protected void makeContent( final OrbitModel orbitModel ) {
		super.makeContent(orbitModel);
		
		Vector<OrbitSource> sources = new Vector<OrbitSource>( orbitModel.getOrbitSources() );
		
		_referenceSourceMenu = new JComboBox<OrbitSource>(sources);
		_referenceSourceMenu.setEditable(false);
		_primarySourceMenu = new JComboBox<OrbitSource>(sources);
		_primarySourceMenu.setEditable(false);
		
		Box refBox = new Box( BoxLayout.X_AXIS );
		Box primaryBox = new Box( BoxLayout.X_AXIS );
		
		refBox.add( new JLabel("Reference Orbit: ") );
		refBox.add( Box.createHorizontalGlue() );
		_editor.add( Box.createVerticalStrut(10) );
		_editor.add( refBox );
		_editor.add( _referenceSourceMenu );
		
		primaryBox.add( new JLabel("Primary Orbit: ") );
		primaryBox.add( Box.createHorizontalGlue() );
		_editor.add( Box.createVerticalStrut(10) );
		_editor.add( primaryBox );
		_editor.add( _primarySourceMenu );
	}
}



/** Snapshot Orbit Source View */
class SnapshotOrbitSourceView extends OrbitSourceView {
	protected JComboBox<OrbitSource> _sourceMenu;
	
	
	/**
	 * Constructor
	 * @param orbitModel  The orbit model
	 */
	public SnapshotOrbitSourceView( final OrbitModel orbitModel ) {
		super( orbitModel );
	}

	
	/**
	 * Instantiate the new orbit source.
	 * @param orbitModel  The orbit model.
	 */
	protected void makeOrbitSource( final OrbitModel orbitModel ) {
		_orbitSource = new SnapshotOrbitSource( "" );
	}

	
	/** Apply user settings to the orbit source. */
	public void applySettings() {
		super.applySettings();
		
		OrbitSource source = (OrbitSource)_sourceMenu.getSelectedItem();
		
		final SnapshotOrbitSource orbitSource = (SnapshotOrbitSource)_orbitSource;
		orbitSource.setSnapshot( source.getOrbit() );
        final String labelText = _labelField.getText();
        // if the user has entered a nontrivial label then use it otherwise generate it from the original source
        final String label = labelText != null && labelText.trim().length() > 0 ? labelText : source.getLabel() + " (" + source.getOrbit().getTimeStamp() + ")";
        orbitSource.setLabel( label );
	}

	
	/**
	 * Make the editor content
	 * @param orbitModel  The orbit model.
	 */
	protected void makeContent( final OrbitModel orbitModel ) {
		super.makeContent(orbitModel);
		
		Vector<OrbitSource> sources = new Vector<OrbitSource>( orbitModel.getOrbitSources() );
		
		_sourceMenu = new JComboBox<OrbitSource>(sources);
		_sourceMenu.setEditable(false);
		
		Box sourceBox = new Box( BoxLayout.X_AXIS );
		
		sourceBox.add( new JLabel("Orbits: ") );
		sourceBox.add( Box.createHorizontalGlue() );
		_editor.add( Box.createVerticalStrut(10) );
		_editor.add( sourceBox );
		_editor.add( _sourceMenu );
	}
}



/** User Defined Orbit Source View */
class UserDefinedOrbitSourceView extends OrbitSourceView {
	/**
	 * Constructor
	 * @param orbitModel  The orbit model
	 */
	public UserDefinedOrbitSourceView( final OrbitModel orbitModel, final JDialog owner ) {
		super( orbitModel );
		
		final MutableOrbit orbit = new MutableOrbit( _orbitSource.getOrbit() );
		final OrbitEditor.CloseStatus status = OrbitEditor.showEditor( (Frame)owner.getOwner(), orbit );
		switch ( status ) {
			case OKAY:
				((SnapshotOrbitSource)_orbitSource).setSnapshot( orbit.getOrbit() );
				break;
			case CANCELED:
				break;
			default:
				break;
		}
	}
	
	
	/**
	 * Instantiate the new orbit source.
	 * @param orbitModel  The orbit model.
	 */
	protected void makeOrbitSource( final OrbitModel orbitModel ) {
		_orbitSource = SnapshotOrbitSource.getInstanceWithBPMs( "User Defined Orbit", orbitModel.getBPMAgents(), orbitModel.getSequence() );
	}
}




/** Logged Orbit Source View */
class LoggedOrbitSourceView extends OrbitSourceView {
	/** Dialog which owns this view */
	final private JDialog _owner;
	
	/** PV Logger store */
	private PVLogger _pvLogger;
	
	/** button for fetching the orbit */
	protected JButton _fetchButton;
	
	
	/**
	 * Constructor
	 * @param orbitModel  The orbit model
	 */
	public LoggedOrbitSourceView( final OrbitModel orbitModel, final JDialog owner ) {
		super( orbitModel );
		
		_owner = owner;
		
		attemptDefaultConnection();
	}
	
	
	/** Attempt to connect to the logger store using the default connection dictionary. */
	protected void attemptDefaultConnection() {
		final ConnectionDictionary dictionary = PVLogger.newBrowsingConnectionDictionary();
		if ( dictionary != null && dictionary.hasRequiredInfo() ) {
			final DatabaseAdaptor adaptor = dictionary.getDatabaseAdaptor();
			if ( adaptor != null && adaptor.getConnection( dictionary ) != null ) {
				setupPVLogger( dictionary );
			}
		}
	}

	
	/**
	 * Instantiate the new orbit source.
	 * @param orbitModel  The orbit model.
	 */
	protected void makeOrbitSource( final OrbitModel orbitModel ) {
		_orbitSource = new SnapshotOrbitSource( "Logged Orbit" );
	}

	
	/** Apply user settings to the orbit source. */
	public void applySettings() {
		super.applySettings();
	}

	
	/**
	 * Make the editor content
	 * @param orbitModel  The orbit model.
	 */
	protected void makeContent( final OrbitModel orbitModel ) {
		super.makeContent(orbitModel);
		
		_editor.add( Box.createVerticalStrut(10) );
		
		JButton connectButton = new JButton("Connect");
		_editor.add(connectButton);
		connectButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				requestUserConnection();
			}
		});
		
		Box fetchRow = new Box( BoxLayout.X_AXIS );
		_editor.add( fetchRow );
		fetchRow.add( new JLabel("Snapshot ID:") );
		
		final JTextField snapshotField = new JTextField(10);
		fetchRow.add( snapshotField );
		snapshotField.setHorizontalAlignment( JTextField.RIGHT );
		
		_fetchButton = new JButton("Fetch");
		fetchRow.add( _fetchButton );
		_fetchButton.setEnabled( false );
		_fetchButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				try {
					String text = snapshotField.getText();
					long index = Long.parseLong(text);
					fetchOrbit(orbitModel, index);
				}
				catch(Exception exception) {
					exception.printStackTrace();
					Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Exception fetching orbit.", exception );
					final String message = exception.getMessage();
					final String title = "Exception fetching orbit...";
					JOptionPane.showMessageDialog(_owner, message, title, JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
	
	
	/**
	 * Setup the logger store with the specified database connection.
	 * @param connection the database connection to use
	 */
	protected void setupPVLogger( final ConnectionDictionary dictionary ) {
		_pvLogger = new PVLogger( dictionary );
		_fetchButton.setEnabled( true );
	}
	
	
	/** Display a connection dialog to the user and connect to the database using the resulting connection dictionary. */
	protected void requestUserConnection() {
		final ConnectionDictionary defaultDictionary = PVLogger.newBrowsingConnectionDictionary();
		final ConnectionDialog connectionDialog = ConnectionDialog.getInstance( _owner, defaultDictionary );
		final ConnectionDictionary dictionary = connectionDialog.getConnectionDictionary();
		if ( dictionary != null && dictionary.hasRequiredInfo() ) {
			final DatabaseAdaptor adaptor = dictionary.getDatabaseAdaptor();
			if ( adaptor != null && adaptor.getConnection( dictionary ) != null ) {
				setupPVLogger( dictionary );
			}
		}
	}
	
	
	/** 
	 * Fetch the orbit for the selected Machine Snapshot ID
	 * orbitModel The orbit model
	 * snapshotID The machine snapshot ID for which to fetch the Orbit
	 */
	protected void fetchOrbit( final OrbitModel orbitModel, final long snapshotID ) {
		try {
			MachineSnapshot machineSnapshot = _pvLogger.fetchMachineSnapshot( snapshotID );
			_pvLogger.loadChannelSnapshotsInto( machineSnapshot );
			
			final Date timestamp = machineSnapshot.getTimestamp();
			
			final ChannelSnapshot[] snapshots = machineSnapshot.getChannelSnapshots();
			final Map<String, ChannelSnapshot> snapshotMap = new HashMap<String, ChannelSnapshot>();
			for ( int index = 0 ; index < snapshots.length ; index++ ) {
				ChannelSnapshot snapshot = snapshots[index];
				String signal = snapshot.getPV();
				snapshotMap.put( signal, snapshot );
			}
			
			MutableOrbit orbit = new MutableOrbit( orbitModel.getSequence() );
			List<BpmAgent> bpmAgents = orbitModel.getBPMAgents();
			//unused List signals = new ArrayList();
			for ( BpmAgent bpmAgent : bpmAgents ) {
				String xAvgPV = bpmAgent.getChannel( BPM.X_AVG_HANDLE ).channelName();
				String yAvgPV = bpmAgent.getChannel( BPM.Y_AVG_HANDLE ).channelName();
				String ampAvgPV = bpmAgent.getChannel( BPM.AMP_AVG_HANDLE ).channelName();
				
				double xAvg = getValue( snapshotMap, xAvgPV );
				double yAvg = getValue( snapshotMap, yAvgPV );
				double ampAvg = getValue( snapshotMap, ampAvgPV );
				
				BpmRecord record = new BpmRecord( bpmAgent, timestamp, xAvg, yAvg, ampAvg );
				orbit.addRecord( record );
			}
			
			SnapshotOrbitSource orbitSource = (SnapshotOrbitSource)_orbitSource;
			orbitSource.setSnapshot( orbit.getOrbit() );
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog( _owner, exception, "Exception fetching orbit", JOptionPane.ERROR_MESSAGE );
		}
	}
	
	
	/**
	 * Get the snapshot value.
	 */
	protected double getValue( final Map<String, ChannelSnapshot> snapshotMap, final String signal ) {
		ChannelSnapshot snapshot = snapshotMap.get(signal);
		
		if ( snapshot != null ) {
			double[] array = snapshot.getValue();
			return array[0];
		}
		else {
			return Double.NaN;
		}
	}
}


