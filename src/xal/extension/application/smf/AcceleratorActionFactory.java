/*
 * AcceleratorActionFactory.java
 *
 * Created on May 20, 2003, 12:38 PM
 */

package xal.extension.application.smf;

import xal.smf.*;
import xal.smf.data.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.logging.*;


/**
 * AcceleratorActionFactory provides factory methods specific to the accelerator based application theme.
 * @author  tap
 */
public class AcceleratorActionFactory {
    
    /** Creates a new instance of AcceleratorActionFactory */
    protected AcceleratorActionFactory() {} 
    
    
    /**
     * The handler that dynamically builds the Sequence submenu whenever the  menu is selected.  
	 * Specifically it builds menu items for loading accelerator sequences associated with the selected accelerator.
     * @param document The document for which the menu is built
     * @return The menu listener that handles the "menuSelected" event.
     */
    public static MenuListener sequenceHandler( final AcceleratorDocument document ) {
        return new MenuListener() {
            /** MenuListener interface */
            public void menuSelected( final MenuEvent event ) {
                final Accelerator accelerator = document.getAccelerator();
                final List<AcceleratorSeq> sequences = accelerator != null ? new ArrayList<AcceleratorSeq>( accelerator.getSequences() ) : Collections.<AcceleratorSeq>emptyList();
                
                final JMenu menu = (JMenu)event.getSource();
                menu.removeAll();
                
                if ( sequences.isEmpty() )  return;
				
                final AcceleratorSeq selectedSequence = document.getSelectedSequence();

                final ButtonGroup sequenceGroup = new ButtonGroup();
				for ( final AcceleratorSeq sequence : sequences ) {
                    final JMenuItem sequenceItem = selectSequenceMenuItem( sequence, document );
                    menu.add( sequenceItem );
                    sequenceGroup.add( sequenceItem );
                    if ( sequence == selectedSequence ) {
                        sequenceItem.setSelected( true );
                    }
                }

				// add the combo sequences				
				menu.addSeparator();
                
				// fetch the pre-defined combo sequences and make sure we don't overwrite the list
				final List<AcceleratorSeqCombo> comboSequences = (accelerator != null) ? new ArrayList<AcceleratorSeqCombo>( accelerator.getComboSequences() ) : Collections.<AcceleratorSeqCombo>emptyList();
				
                // If the selected sequence is a combo sequence make sure there is an appropriate menu item
                if ( selectedSequence != null && selectedSequence instanceof AcceleratorSeqCombo && !comboSequences.contains( selectedSequence ) ) {
                    comboSequences.add( (AcceleratorSeqCombo)selectedSequence );
                }
				
				for ( final AcceleratorSeq sequence : comboSequences ) {
                    final JMenuItem sequenceItem = selectSequenceMenuItem( sequence, document );
                    menu.add(sequenceItem);
                    sequenceGroup.add( sequenceItem );
                    if ( sequence.equals( selectedSequence ) ) {
                        sequenceItem.setSelected( true );
                    }
                }
                
                menu.addSeparator();
                menu.add( comboSequenceSelectorMenuItem(document) );
            }


            /** MenuListener interface */
            public void menuCanceled( final MenuEvent event ) {}


            /** MenuListener interface */
            public void menuDeselected( final MenuEvent event ) {}
        };
    }
    
    
    /**
     * Creates the action that, when activated, will load the default accelerator.
     * @param document The document into which the accelerator will be set.
     * @return The action that will load the accelerator when activated.
     */
    public static Action loadDefaultAcceleratorAction( final AcceleratorDocument document ) {
        final Action action = new AbstractAction() {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				try {
					document.loadDefaultAccelerator();
				}
				catch( Exception exception ) {
					final String message = "Exception while loading the default accelerator: ";
					System.err.println( message + '\n' + exception );
					Logger.getLogger("global").log( Level.SEVERE, message, exception );
					document.displayError( "Exception", message, exception );
				}
            }
        };
        action.putValue( Action.NAME, "load-default-accelerator" );
        
        return action;
    }
    
    
    /**
     * Creates the action that, when activated, will provide an open dialog box that allows the user to select an accelerator file.
     * @param document The document into which the accelerator will be set.
     * @return The action that when activated will load the accelerator selected by the user.
     */
    public static Action loadAcceleratorAction( final AcceleratorDocument document ) {
        final Action action = new AbstractAction() {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = ((AcceleratorApplication)AcceleratorApplication.getApp()).getAcceleratorFileChooser();
				int status = fileChooser.showOpenDialog( document.getMainWindow() );
				try {
					switch(status) {
						case JFileChooser.CANCEL_OPTION:
							break;
						case JFileChooser.APPROVE_OPTION:
							final File fileSelection = fileChooser.getSelectedFile();
							final String filePath = fileSelection.getAbsolutePath();
							final Accelerator accelerator = XMLDataManager.acceleratorWithPath( filePath );
							document.setAccelerator( accelerator, filePath );
							break;
						case JFileChooser.ERROR_OPTION:
							break;
					}
				}
				catch(Exception exception) {
					final String message = "Exception while loading the selected accelerator: ";
					System.err.println( message + '\n' + exception );
					Logger.getLogger( "global" ).log( Level.SEVERE, message, exception );
					document.displayError( "Exception", message, exception );
				}
            }
        };
        action.putValue( Action.NAME, "load-accelerator" );
        
        return action;
    }
    
        
    /**
     * Creates the menu item that selects a particular sequence and sets the document's selected sequence to that sequence.
     * @param sequence The sequence that gets selected when the menu item is selected.
     * @param document The document for which the menu item applies the selected sequence.
     * @return The menu item used to select the specific sequence.
     */
    static private JMenuItem selectSequenceMenuItem( final AcceleratorSeq sequence, final AcceleratorDocument document ) {
        final String label = sequence.getId();
        final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem( label );
        menuItem.setAction( new AbstractAction() {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
                document.setSelectedSequence( sequence );
            }
        });
        menuItem.setText( label );
        
        return menuItem;        
    }
    
    
    /**
     * Creates the menu item that allows the user to construct and select a combo sequence.
     * @param document The document into which the combo sequence is selected.
     * @return The menu item used to construct the combo sequence.
     */
    static private JMenuItem comboSequenceSelectorMenuItem( final AcceleratorDocument document ) {
        final String label = "New Combo Sequence";
        final JMenuItem menuItem = new JMenuItem( label );
        menuItem.setAction( new AbstractAction() {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				final AcceleratorSeqCombo comboSequence = ComboSequenceComposer.composeComboSequence( document.getAccelerator(), document.getMainWindow() );
				if ( comboSequence != null ) {
					document.setSelectedSequence( comboSequence );
					document.setSelectedSequenceList( comboSequence.getConstituents() );
			    }
            }
        });
        menuItem.setText( label );
        
        return menuItem;
    }    
}
