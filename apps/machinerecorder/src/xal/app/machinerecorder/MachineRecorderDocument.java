/*
 * MachineRecorderDocument.java
 *
 * Created on June 14, 2011, 1:32 PM
 */

package xal.app.machinerecorder;

import java.net.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.data.*;
import xal.extension.widgets.swing.KeyValueTableModel;


/**
 * Main document for Machine Recorder
 * @author  t6p
 */
public class MachineRecorderDocument extends AcceleratorDocument implements DataListener {    
 	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "MachineRecorderDocument";
    
    /** main model */
    private final MachineRecorder RECORDER;
    
    /** window reference */
    private WindowReference WINDOW_REFERENCE;
    
    /** record button */
    private JToggleButton RECORD_BUTTON;
    
    /** play button */
    private JToggleButton PLAY_BUTTON;
    
    /** table model for display groups */
    private KeyValueTableModel<DisplayGroup> DISPLAY_GROUP_TABLE_MODEL;
    
    /** table of the display groups */
    private JTable DISPLAY_GROUP_TABLE;
    
    /** controller for managing the display groups */
    private DisplayGroupConfigController DISPLAY_GROUP_CONFIG_CONTROLLER;
    
    
    /** Create a new empty document */
    public MachineRecorderDocument() {
        this( null );
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public MachineRecorderDocument( final URL url ) {
        setSource( url );
        
        RECORDER = new MachineRecorder();
        
        if ( url != null ) {
            System.out.println( "Opening document: " + url.toString() );
            final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
            update( documentAdaptor.childAdaptor( dataLabel() ) );
        }        
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        WINDOW_REFERENCE = getDefaultWindowReference( "MainWindow", this );
        mainWindow = (XalWindow)WINDOW_REFERENCE.getWindow();
        
        DISPLAY_GROUP_CONFIG_CONTROLLER = new DisplayGroupConfigController( WINDOW_REFERENCE );
        
        RECORD_BUTTON = (JToggleButton)WINDOW_REFERENCE.getView( "RecordButton" );
        RECORD_BUTTON.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                if ( RECORD_BUTTON.isSelected() ) {
                    RECORDER.record();
                }
                else {
                    RECORDER.stop();
                }
                
                // todo: buttons should update to reflect state
            }
        });
        
        
        PLAY_BUTTON = (JToggleButton)WINDOW_REFERENCE.getView( "PlayButton" );
        PLAY_BUTTON.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                if ( PLAY_BUTTON.isSelected() ) {
                    RECORDER.play();
                }
                else {
                    RECORDER.stop();
                }
                
                // todo: buttons should update to reflect state
            }
        });
        
        
        final JButton PRECEDING_FLAG_BUTTON = (JButton)WINDOW_REFERENCE.getView( "PrecedingFlagButton" );
        PRECEDING_FLAG_BUTTON.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                RECORDER.moveToPrecedingFlag();
            }
        });
        
        
        final JButton PRECEDING_EVENT_BUTTON = (JButton)WINDOW_REFERENCE.getView( "PrecedingEventButton" );
        PRECEDING_EVENT_BUTTON.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                RECORDER.moveToPrecedingEvent();
            }
        });
        
        
        final JButton NEXT_EVENT_BUTTON = (JButton)WINDOW_REFERENCE.getView( "NextEventButton" );
        NEXT_EVENT_BUTTON.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                RECORDER.moveToNextEvent();
            }
        });
        
        
        final JButton NEXT_FLAG_BUTTON = (JButton)WINDOW_REFERENCE.getView( "NextFlagButton" );
        NEXT_FLAG_BUTTON.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                RECORDER.moveToNextFlag();
            }
        });

        
        DISPLAY_GROUP_TABLE_MODEL = new KeyValueTableModel<DisplayGroup>( RECORDER.getDisplayGroups(), "name" );
        DISPLAY_GROUP_TABLE_MODEL.setColumnEditable( "name", true );
        
        DISPLAY_GROUP_TABLE = (JTable)WINDOW_REFERENCE.getView( "Display Groups Table" );
        DISPLAY_GROUP_TABLE.setModel( DISPLAY_GROUP_TABLE_MODEL );
        DISPLAY_GROUP_TABLE.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            public void valueChanged( final ListSelectionEvent event ) {
                final DisplayGroup group = getSelectedDisplayGroup();
                DISPLAY_GROUP_CONFIG_CONTROLLER.setDisplayGroup( group );
            }
        });
        
        final JButton addGroupButton = (JButton)WINDOW_REFERENCE.getView( "AddGroupButton" );
        addGroupButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                final int row = DISPLAY_GROUP_TABLE.getSelectedRow();
                if ( row >= 0 ) {
                    final int groupIndex = DISPLAY_GROUP_TABLE.convertRowIndexToModel( row );
                    RECORDER.addDisplayGroupAt( new DisplayGroup( selectedSequence ), groupIndex );
                }
                else {
                    RECORDER.addDisplayGroup( new DisplayGroup( selectedSequence ) );
                }
                DISPLAY_GROUP_TABLE_MODEL.setRecords( RECORDER.getDisplayGroups() );
            }
        });
        
        
        final JButton duplicateGroupButton = (JButton)WINDOW_REFERENCE.getView( "DuplicateGroupButton" );
        duplicateGroupButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                final int row = DISPLAY_GROUP_TABLE.getSelectedRow();
                if ( row >= 0 ) {
                    final int groupIndex = DISPLAY_GROUP_TABLE.convertRowIndexToModel( row );
                    final DisplayGroup originalGroup = RECORDER.getDisplayGroupAt( groupIndex );
                    RECORDER.addDisplayGroupAt( originalGroup.getDuplicate(), groupIndex );
                    DISPLAY_GROUP_TABLE_MODEL.setRecords( RECORDER.getDisplayGroups() );
                }
            }
        });
        
        
        final JButton removeGroupButton = (JButton)WINDOW_REFERENCE.getView( "RemoveGroupButton" );
        removeGroupButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                final int row = DISPLAY_GROUP_TABLE.getSelectedRow();
                if ( row >= 0 ) {
                    final int groupIndex = DISPLAY_GROUP_TABLE.convertRowIndexToModel( row );
                    RECORDER.removeDisplayGroupAt( groupIndex );
                    DISPLAY_GROUP_TABLE_MODEL.setRecords( RECORDER.getDisplayGroups() );
                }
            }
        });
	}
    
    
    /** Get the currently selected display group */
    public DisplayGroup getSelectedDisplayGroup() {
        final int row = DISPLAY_GROUP_TABLE.getSelectedRow();
        if ( row >= 0 ) {
            final int groupIndex = DISPLAY_GROUP_TABLE.convertRowIndexToModel( row );
            return RECORDER.getDisplayGroupAt( groupIndex );
        }
        else {
            return null;
        }
    }
    
    
    /** update the view to reflect the model */
    private void updateView() {
        
    }
    
    
    /** refresh the group table */
    private void refreshGroupTable() {
        
    }
    
    
    /** implement hook to handle an accelerator change event */
    public void acceleratorChanged() {
        RECORDER.setAccelerator( accelerator );
    }
    
    
    /** implement the hook to handle the accelerator sequence change event */
    public void selectedSequenceChanged() {
        final DisplayGroup group = getSelectedDisplayGroup();
        if ( group != null ) {
            group.setSequence( selectedSequence );
        }
    }
    
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
        try {
            final XmlDataAdaptor documentAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();
            documentAdaptor.writeNode( this );
            documentAdaptor.writeToUrl( url );
            setHasChanges( false );
        }
        catch( XmlDataAdaptor.WriteException exception ) {
			if ( exception.getCause() instanceof java.io.FileNotFoundException ) {
				System.err.println( exception );
				displayError( "Save Failed!", "Save failed due to a file access exception!", exception );
			}
			else if ( exception.getCause() instanceof java.io.IOException ) {
				System.err.println( exception );
				displayError( "Save Failed!", "Save failed due to a file IO exception!", exception );
			}
			else {
				exception.printStackTrace();
				displayError( "Save Failed!", "Save failed due to an internal write exception!", exception );
			}
        }
        catch(Exception exception) {
			exception.printStackTrace();
            displayError( "Save Failed!", "Save failed due to an internal exception!", exception );
        }
	}
	
    
    /** provides the name used to identify the class in an external data source. */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /** Instructs the receiver to update its data based on the given adaptor. */
    public void update( final DataAdaptor adaptor ) {
		if ( adaptor.hasAttribute( "acceleratorPath" ) ) {
			final String acceleratorPath = adaptor.stringValue( "acceleratorPath" );
			applySelectedAcceleratorWithDefaultPath( acceleratorPath );
			
			if ( accelerator != null && adaptor.hasAttribute( "sequence" ) ) {
				final String sequenceID = adaptor.stringValue( "sequence" );
				setSelectedSequence( accelerator.findSequence( sequenceID ) );
			}
		}
		
		final DataAdaptor modelAdaptor = adaptor.childAdaptor( MachineRecorder.DATA_LABEL );
		if ( modelAdaptor != null )  RECORDER.update( modelAdaptor );
    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {
        adaptor.setValue( "version", "1.0.0" );
        adaptor.setValue( "date", new java.util.Date().toString() );
		
		adaptor.writeNode( RECORDER );
		
		if ( getAccelerator() != null ) {
			adaptor.setValue( "acceleratorPath", getAcceleratorFilePath() );
			
			final AcceleratorSeq sequence = getSelectedSequence();
			if ( sequence != null ) {
				adaptor.setValue( "sequence", sequence.getId() );
			}
		}
    }
}
