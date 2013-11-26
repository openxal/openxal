//
//  BumpGeneratorDialog.java
//  xal
//
//  Created by Thomas Pelaia on 3/13/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.smf.AcceleratorNode;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;


/** Dialog for managing and observing the bump generator */
public class BumpGeneratorDialog extends JDialog implements BumpGeneratorListener {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	/** bump generator */
	final BumpGenerator BUMP_GENERATOR;
	
	/** run button */
	final JButton RUN_BUTTON;
	
	/** Cancel button */
	final JButton CANCEL_BUTTON;
	
	/** progress bar */
	final JProgressBar PROGRESS_BAR;
	
	/** the element count control model */
	final SpinnerNumberModel ELEMENT_COUNT_CONTROL_MODEL;
	
	/** indicates whether the model should use the live model or design */
	protected boolean _useLiveModel;
	
	/** selected bump shape adaptor */
	protected BumpShapeAdaptor _bumpShapeAdaptor;
	
	
	/** Constructor */
	public BumpGeneratorDialog( final JFrame owner, final KnobsModel model, final KnobGroup knobGroup, final PlaneAdaptor planeAdaptor ) {
		super( owner, planeAdaptor.planeName() + " Bump Generator", true );
		
		BUMP_GENERATOR = new BumpGenerator( model, knobGroup );
		BUMP_GENERATOR.addBumpGeneratorListener( this );
		BUMP_GENERATOR.setPlaneAdaptor( planeAdaptor );
		
		RUN_BUTTON = new JButton( "Generate" );
		CANCEL_BUTTON = new JButton( "Cancel" );
		CANCEL_BUTTON.setEnabled( false );
		
		PROGRESS_BAR = new JProgressBar();
		PROGRESS_BAR.setStringPainted( true );
		PROGRESS_BAR.setString( " Bump Generation Progress... " );
		
		ELEMENT_COUNT_CONTROL_MODEL = new SpinnerNumberModel( 3, 3, 100, 1 );
		
		makeContent();
		
		setLocationRelativeTo( owner );
		
		addWindowListener( new WindowAdapter() {
			public void windowClosing( final WindowEvent event ) {
				BUMP_GENERATOR.cancelBumpGeneration();
			}
		});		
	}
	
	
	/** make the content */
	protected void makeContent() {
		setResizable( false );
		
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
				
		final Box contentView = new Box( BoxLayout.Y_AXIS );
		contentView.setBorder( BorderFactory.createEtchedBorder() );
				
		contentView.add( makeSettingsView() );
		contentView.add( Box.createVerticalStrut( 5 ) );
		contentView.add( PROGRESS_BAR );
		
		mainView.add( contentView );
		mainView.add( makeButtonRow() );
		
		pack();
	}
	
	
	/** make the settings view */
	protected Component makeSettingsView() {
		final Box view = new Box( BoxLayout.X_AXIS );
		
		view.add( Box.createHorizontalGlue() );		
		view.add( makeModelSourceSelector() );
		view.add( makeBumpShapeSelector() );
		view.add( makeMagnetCountView() );
		
		return view;
	}
	
	
	/** make the magnet count view */
	protected Component makeMagnetCountView() {
		final Box view = new Box( BoxLayout.Y_AXIS );
		
		view.setBorder( BorderFactory.createTitledBorder( "Magnets" ) );
		view.add( new JSpinner( ELEMENT_COUNT_CONTROL_MODEL ) );
		view.add( Box.createVerticalGlue() );
		
		return view;
	}
	
	
	/** make model source selector */
	protected Component makeModelSourceSelector() {
		final Box view = new Box( BoxLayout.Y_AXIS );
		view.setBorder( BorderFactory.createTitledBorder( "Model" ) );
		
		_useLiveModel = BUMP_GENERATOR.usesLiveModel();
		
		final JRadioButton designButton = new JRadioButton( "Design" );
		designButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_useLiveModel = !designButton.isSelected();
			}
		});
		designButton.setSelected( !_useLiveModel );
		
		final JRadioButton liveButton = new JRadioButton( "Live" );
		liveButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_useLiveModel = liveButton.isSelected();
			}
		});
		liveButton.setSelected( _useLiveModel );
		
		view.add( designButton );
		view.add( liveButton );
		
		final ButtonGroup group = new ButtonGroup();
		group.add( designButton );
		group.add( liveButton );
				
		return view;
	}
	
	
	/** make bump shape selector */
	protected Component makeBumpShapeSelector() {
		final Box view = new Box( BoxLayout.Y_AXIS );
		view.setBorder( BorderFactory.createTitledBorder( "Bump" ) );
		
		_bumpShapeAdaptor = BUMP_GENERATOR.getBumpShapeAdaptor();
		updateMinimumElementCount();
		
		final JRadioButton offsetButton = new JRadioButton( "Offset" );
		offsetButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_bumpShapeAdaptor = BumpShapeAdaptor.getBumpOffsetAdaptor();
				updateMinimumElementCount();
			}
		});
		offsetButton.setSelected( _bumpShapeAdaptor == BumpShapeAdaptor.getBumpOffsetAdaptor() );
		
		final JRadioButton angleButton = new JRadioButton( "Angle" );
		angleButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_bumpShapeAdaptor = BumpShapeAdaptor.getBumpAngleAdaptor();
				updateMinimumElementCount();
			}
		});
		angleButton.setSelected( _bumpShapeAdaptor == BumpShapeAdaptor.getBumpAngleAdaptor() );
		
		view.add( offsetButton );
		view.add( angleButton );
		
		final ButtonGroup group = new ButtonGroup();
		group.add( offsetButton );
		group.add( angleButton );
				
		return view;
	}
	
	
	/** update the minimum element count */
	protected void updateMinimumElementCount() {
		final Number value = ELEMENT_COUNT_CONTROL_MODEL.getNumber();
		final Integer minimumCount = new Integer( _bumpShapeAdaptor.getMinimumElementCount() );
		ELEMENT_COUNT_CONTROL_MODEL.setMinimum( minimumCount );
		if ( minimumCount.intValue() > value.intValue() ) {
			ELEMENT_COUNT_CONTROL_MODEL.setValue( minimumCount );
		}
	}
	
	
	/** make the button row */
	protected Component makeButtonRow() {
		final Box row = new Box( BoxLayout.X_AXIS );
		row.setBorder( BorderFactory.createEtchedBorder() );

		RUN_BUTTON.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				RUN_BUTTON.setEnabled( false );
				final int elementCount = ELEMENT_COUNT_CONTROL_MODEL.getNumber().intValue();
				BUMP_GENERATOR.setBumpElementCount( elementCount );
				BUMP_GENERATOR.setBumpShapeAdaptor( _bumpShapeAdaptor );
				BUMP_GENERATOR.setUsesLiveModel( _useLiveModel );
				spawnBumpGeneration();
			}
		});
		
		CANCEL_BUTTON.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				BUMP_GENERATOR.cancelBumpGeneration();
				setVisible( false );
			}
		});
		
		row.add( Box.createHorizontalStrut( 100 ) );
		row.add( Box.createHorizontalGlue() );
		row.add( CANCEL_BUTTON );
		row.add( RUN_BUTTON );
		row.add( Box.createHorizontalStrut( 10 ) );
		
		getRootPane().setDefaultButton( RUN_BUTTON );
				
		return row;
	}
	
	
	/** spawn bump generation */
	protected void spawnBumpGeneration() {
		CANCEL_BUTTON.setEnabled( true );
		
		new Thread( new Runnable() {
			/** perform the bump generation */
			public void run() {
				try {
					BUMP_GENERATOR.makeBumpKnobs();
				}
				finally {
					setupControlsForRunCompletion();
				}
			}
		}).start();
	}
	
	
	/** setup controls for run completion */
	protected void setupControlsForRunCompletion() {
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {
					RUN_BUTTON.setEnabled( true );
					CANCEL_BUTTON.setEnabled( false );
				}
			});
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** handle the event indicating that a new knob is about to be generated */
	public void willGenerateKnob( final BumpGenerator generator, final AcceleratorNode node ) {
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {
					PROGRESS_BAR.setMaximum( BUMP_GENERATOR.getBumpCount() );
					PROGRESS_BAR.setString( node.getId() );
				}
			});
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** handle event indicating that the knob has been generated */
	public void knobGenerated( final BumpGenerator generator, final Knob knob ) {
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {
					PROGRESS_BAR.setValue( BUMP_GENERATOR.getProcessedBumpCount() );
				}
			});
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** handle event indicating that the generator failed to make the bump for the specified node */
	public void knobGeneratorException( final BumpGenerator generator, final AcceleratorNode node, final Exception exception ) {}
	
	
	/** handle event indicating that the knob generation is complete */
	public void knobGenerationComplete( final BumpGenerator generator, final List<Knob> knobs ) {
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {
					setVisible( false );
				}
			});
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** handle event indicating that the knob generation failed */
	public void knobGenerationFailed( final BumpGenerator generator, final Exception exception ) {
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog( BumpGeneratorDialog.this, exception.getMessage(), "Generator Failed", JOptionPane.ERROR_MESSAGE );
					setVisible( false );
				}
			});
		}
		catch( Exception runException ) {
			runException.printStackTrace();
		}
	}
}
