//
//  ComboSequenceComposer.java
//  xal
//
//  Created by Tom Pelaia on 9/25/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.application.smf;

import xal.extension.bricks.WindowReference;
import xal.smf.*;
import xal.tools.ResourceManager;

import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;


/** Displays a dialog box which allows the user to compose a combo sequence by selecting the end sequences. */
class ComboSequenceComposer {
	/** accelerator */
	final private Accelerator ACCELERATOR;
	
	/** sequence at the head of the combo */
	private AcceleratorSeq _startSequence;
	
	/** sequence at the end of the combo */
	private AcceleratorSeq _endSequence;
	
	/** indicates whether the user has confirmed the combo */
	private boolean _confirmed;
	
	
	/** Constructor */
	private ComboSequenceComposer( final Accelerator accelerator ) {
		ACCELERATOR = accelerator;
		_confirmed = false;
	}
	
	
	/** determine whether the user has confirmed the combo sequence */
	private boolean isConfirmed() {
		return _confirmed;
	}
	
	
	/** set whether the current combo is confirmed */
	private void setConfirmed( final boolean confirmed ) {
		_confirmed = confirmed;
	}
	
	
	/** get the accelerator */
	private Accelerator getAccelerator() {
		return ACCELERATOR;
	}
	
	
	/** set the start sequence */
	private void setStartSequence( final AcceleratorSeq sequence ) {
		_startSequence = sequence;
	}
	
	
	/** set the end sequence */
	private void setEndSequence( final AcceleratorSeq sequence ) {
		_endSequence = sequence;
	}
	
	
	/** determine whether a combo sequence is possible between the start and end sequences */
	private boolean isValidCombo() {
		if ( _startSequence != null && _endSequence != null && _startSequence != _endSequence ) {
			return generateCombo( "validate" ) != null;
		}
		else {
			return false;
		}
	}
	
	
	/** generate a combo sequence using the given name and the sequences between the start and end sequences */
	private AcceleratorSeqCombo generateCombo( final String name ) {
		final String comboID = name != null ? name : suggestedComboName();
		return _startSequence != null && _endSequence != null ? AcceleratorSeqCombo.getInstanceForRange( comboID, _startSequence, _endSequence ) : null;
	}
	
	
	/** generate a suggested name based on the start and end sequence */
	private String suggestedComboName() {
		final StringBuffer buffer = new StringBuffer();
		
		if ( _startSequence != null )  buffer.append( _startSequence.getId() );
		buffer.append( ":" );
		if ( _endSequence != null )  buffer.append( _endSequence.getId() );
		
		return buffer.toString();
	}
	
	
	/**
	 * Display a dialog for selecting the start and end sequences to form a combo sequence joining them.
	 * @param accelerator pass the Accelerator object here from main routine
	 * @param owner the window that owns the sequence selector
	 */
	@SuppressWarnings( "unchecked" )		// need to cast from untyped JList
	static public AcceleratorSeqCombo composeComboSequence( final Accelerator accelerator, final JFrame owner ) {
		final ComboSequenceComposer composer = new ComboSequenceComposer( accelerator );
		
		final List<AcceleratorSeq> sequences = accelerator.getSequences();
		
		final URL uiURL = ResourceManager.getResourceURL( ComboSequenceComposer.class, "ui.bricks" );
		final WindowReference windowReference = new WindowReference( uiURL, "ComboSequenceComposer", owner, "Combo Sequence Composer" );
		final JDialog dialog = (JDialog)windowReference.getWindow();
		
		final JTextField comboNameField = (JTextField)windowReference.getView( "Combo Name Field" );
		
		final JButton cancelButton = (JButton)windowReference.getView( "CancelButton" );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				dialog.setVisible( false );
			}
		});
		
		final JButton okayButton = (JButton)windowReference.getView( "OkayButton" );
		okayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				composer.setConfirmed( true );
				dialog.setVisible( false );
			}
		 });
		
		final Vector<String> sequenceNames = new Vector<String>();
		for ( final AcceleratorSeq sequence : sequences ) {
			sequenceNames.add( sequence.getId() );
		}
		
		final JList<String> startSequenceList = (JList<String>)windowReference.getView( "Start Sequence List" );
		startSequenceList.setListData( sequenceNames );
		startSequenceList.addListSelectionListener( new ListSelectionListener() {
		   public void valueChanged( final ListSelectionEvent event ) {
				if ( !event.getValueIsAdjusting() ) {
					final Object selection = startSequenceList.getSelectedValue();
				    final AcceleratorSeq sequence = selection != null ? accelerator.getSequence( selection.toString() ) : null;
					composer.setStartSequence( sequence );
					comboNameField.setText( composer.suggestedComboName() );
					okayButton.setEnabled( composer.isValidCombo() );
				}
		   }
		});
		
		final JList<String> endSequenceList = (JList<String>)windowReference.getView( "End Sequence List" );
		endSequenceList.setListData( sequenceNames );
	    endSequenceList.addListSelectionListener( new ListSelectionListener() {
			  public void valueChanged( final ListSelectionEvent event ) {
				  if ( !event.getValueIsAdjusting() ) {
					  final Object selection = endSequenceList.getSelectedValue();
					  final AcceleratorSeq sequence = selection != null ? accelerator.getSequence( selection.toString() ) : null;
					  composer.setEndSequence( sequence );
					  comboNameField.setText( composer.suggestedComboName() );
					  okayButton.setEnabled( composer.isValidCombo() );
				  }
			  }
		});
																							  
		dialog.setLocationRelativeTo( owner );
		dialog.setVisible( true );
		
		return composer.isConfirmed() ? composer.generateCombo( comboNameField.getText() ) : null;
	}
}
