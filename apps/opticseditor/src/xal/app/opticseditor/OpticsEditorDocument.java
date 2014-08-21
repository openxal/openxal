/*
 * OpticsEditorDocument.java
 *
 * Created on Fri Oct 5 4:06:57 EDT 2007
 *
 * Copyright (c) 2007 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.opticseditor;

import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.tools.messaging.MessageCenter;
import xal.tools.data.*;
import xal.tools.FreshProcessor;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.data.XMLDataManager;

import java.io.File;
import java.net.URL;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.util.*;
import java.util.regex.Pattern;


/**
 * OpticsEditorDocument and main controller
 * @author  t6p
 */
class OpticsEditorDocument extends XalDocument {
	/** data label */
	static final private String DATA_LABEL = "sources";
	
	/** reference to the main window */
	private WindowReference _mainWindowReference;
	
	/** optics */
	private Optics _optics;
	
	
	/** Constructor */
    public OpticsEditorDocument() {
        this( null );
    }
	
	
	/** Primary Constructor */
	public OpticsEditorDocument( final URL mainURL ) {
        setSource( mainURL );
		
        if ( mainURL != null ) {
            System.out.println( "Opening document: " + mainURL.toString() );
			
			final XMLDataManager opticsDataManager = XMLDataManager.getInstance( mainURL );
			final String designURLSpec = opticsDataManager.opticsUrlSpec();
			final String hardwareStatusURLSpec = opticsDataManager.getHardwareStatusURLSpec();
			
			_optics = new Optics( mainURL, designURLSpec, hardwareStatusURLSpec );
			
            setHasChanges( false );
        }
	}
    
    
    /** Make a main window by instantiating the my custom window. */
    public void makeMainWindow() {
		final WindowReference windowReference = getDefaultWindowReference( "MainWindow", this );
		_mainWindowReference = windowReference;		
        mainWindow = (XalWindow)windowReference.getWindow();
		
		final NodeRecordCompoundFilter recordFilter = new NodeRecordCompoundFilter();
		final NodeRecordModeFilter statusFilter = NodeRecordModeFilter.getStatusFilterInstance();
		recordFilter.addFilter( statusFilter );
		final NodeRecordModeFilter excludeFilter = NodeRecordModeFilter.getExclusionFilterInstance();
		recordFilter.addFilter( excludeFilter );
		final NodeRecordModeFilter modifiedFilter = NodeRecordModeFilter.getModificationFilterInstance();
		recordFilter.addFilter( modifiedFilter );
		recordFilter.addFilter( makeNodeNameFilterHandler() );
		
		setupModeFilterRadioButtons( statusFilter, "AnyStatus", "GoodStatus", "BadStatus" );
		setupModeFilterRadioButtons( excludeFilter, "AnyInclusion", "Excluded", "Included" );
		setupModeFilterRadioButtons( modifiedFilter, "AnyModification", "Modified", "Unmodified" );
				
		final NodesTableModel nodesTableModel = new NodesTableModel( _optics.getNodeRecords(), recordFilter );
		final JTable nodesTable = getNodesTable();
		nodesTable.setModel( nodesTableModel );
		nodesTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
	}
	
	
	/** get the nodes table model */
	private NodesTableModel getNodesTableModel() {
		return (NodesTableModel)getNodesTable().getModel();
	}
	
	
	/** get the nodes table */
	private JTable getNodesTable() {
		return (JTable)_mainWindowReference.getView( "NodesTable" );
	}
	
	
	/** get the selected node record rows */
	private int[] getSelectedNodeRecordRows() {
		final JTable nodesTable = getNodesTable();
		return nodesTable.getSelectedRows();
	}
	
	
	/** get the node records corresponding to the specified rows */
	private List<NodeRecord> getNodeRecords( final int[] rows ) {
		final NodesTableModel tableModel = getNodesTableModel();
		return tableModel.getRecordsForRows( rows );
	}
	
	
	/** get the selected records */
	private List<NodeRecord> getSelectedNodeRecords() {
		final JTable nodesTable = getNodesTable();
		final NodesTableModel tableModel = getNodesTableModel();
		final int[] selectedRows = nodesTable.getSelectedRows();
		return tableModel.getRecordsForRows( selectedRows );
	}
	
	
	/** refresh the specified rows */
	private void refreshRows( final int[] rows ) {
		final NodesTableModel tableModel = getNodesTableModel();
		for ( final int row : rows ) {
			tableModel.fireTableRowsUpdated( row, row );
		}
	}
	
	
	/** update the mode filter based on the radio button selection */
	private void updateModeFilter( final NodeRecordModeFilter modeFilter, final JRadioButton anyButton, final JRadioButton passButton ) {
		if ( anyButton.isSelected() ) {
			modeFilter.setMode( NodeRecordModeFilter.ANY_MODE );
		}
		else if ( passButton.isSelected() ) {
			modeFilter.setMode( NodeRecordModeFilter.PASS_MODE );
		}
		else {
			modeFilter.setMode( NodeRecordModeFilter.FAIL_MODE );
		}
		getNodesTableModel().refreshRecords();
	}
	
	
	/** apply the specified status to the selected node records */
	private void applyStatusToSelectedNodeRecords( final boolean status ) {
		final int[] selectedRows = getSelectedNodeRecordRows();
		final List<NodeRecord> records = getNodeRecords( selectedRows );
		for ( final NodeRecord record : records ) {
			record.setStatus( status );
		}
		refreshRows( selectedRows );
		setHasChanges( true );
	}
	
	
	/** apply the specified exclusion to the selected node records */
	private void applyExclusionToSelectedNodeRecords( final boolean exclusion ) {
		final int[] selectedRows = getSelectedNodeRecordRows();
		final List<NodeRecord> records = getNodeRecords( selectedRows );
		for ( final NodeRecord record : records ) {
			record.setExclude( exclusion );
		}
		refreshRows( selectedRows );
		setHasChanges( true );
	}
    
    
    /**
	 * Register custom commands. 
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands( final Commander commander ) {
        final Action enableNodesAction = new AbstractAction( "enable-nodes" ) {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
				applyStatusToSelectedNodeRecords( true );
            }
        };        
        commander.registerAction( enableNodesAction );
		
        final Action disableNodesAction = new AbstractAction( "disable-nodes" ) {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
				applyStatusToSelectedNodeRecords( false );
            }
        };        
        commander.registerAction( disableNodesAction );
		
        final Action includeNodesAction = new AbstractAction( "include-nodes" ) {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
				applyExclusionToSelectedNodeRecords( false );
            }
        };        
        commander.registerAction( includeNodesAction );
		
        final Action excludeNodesAction = new AbstractAction( "exclude-nodes" ) {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
				applyExclusionToSelectedNodeRecords( true );
            }
        };        
        commander.registerAction( excludeNodesAction );
	}
	
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
		try {
			System.out.println( "Saving to: " + url );
			if ( _optics.save( this ) ) {
				setHasChanges( false );
				// to do: really should make a logbook entry about this
			}
			else {
				JOptionPane.showMessageDialog( getMainWindow(), "The save operation was cancelled.", "Save Cancelled" , JOptionPane.WARNING_MESSAGE );
			}
		}
		catch( Exception exception ) {
			displayError( "Save Failed", "Exception while attempting to save changes", exception );
		}
	}
	
	
	/** 
	 * Request a file name for the hardware status.
	 * @return user specified file name
	 */
	String newHardwareStatusURLSpec( final URL baseURL ) throws Exception {
		final String urlSpec = "hardware_status.xdxf";
		if ( existingURL( baseURL, urlSpec ) ) {
			return requestHardwareStatusFileName( baseURL );
		}
		else {
			return urlSpec;
		}
	}
	
	
	/** request the hardware status file name */
	private String requestHardwareStatusFileName( final URL baseURL ) throws Exception {
		final String fileName = JOptionPane.showInputDialog( getMainWindow(), "Enter a unique hardware status file name: " );
		if ( fileName != null ) {
			final String urlSpec = fileName.endsWith( ".xdxf" ) ? fileName : fileName + ".xdxf";
			if ( existingURL( baseURL, urlSpec ) ) {
				return requestHardwareStatusFileName( baseURL );
			}
			else {
				return urlSpec;
			}
		}
		else {
			return null;
		}
	}
	
	
	/** determine if the URL spec corresponds to an existing file */
	static private boolean existingURL( final URL baseURL, final String urlSpec ) throws Exception {
		final URL url = new URL( baseURL, urlSpec );
		return new File( url.toURI() ).exists();
	}
	
	
	/** setup the node name filter handler */
	private NodeRecordFilter makeNodeNameFilterHandler() {
		final WindowReference windowReference = _mainWindowReference;
		
		final JTextField nodeFilterField = (JTextField)windowReference.getView( "NodeFilterField" );
		final NodeRecordNameFilter nameFilter = new NodeRecordNameFilter();
		
		final FreshProcessor nodeFilterProcessor = new FreshProcessor();
		nodeFilterField.getDocument().addDocumentListener( new DocumentListener() {
			  public void changedUpdate( final DocumentEvent event ) {
					nodeFilterProcessor.post( new NodeNameFilterOperation( nodeFilterField.getText(), nameFilter) );
			  }
			  public void insertUpdate( final DocumentEvent event ) {
					nodeFilterProcessor.post( new NodeNameFilterOperation( nodeFilterField.getText(), nameFilter) );
			  }
			  public void removeUpdate( final DocumentEvent event ) {
					nodeFilterProcessor.post( new NodeNameFilterOperation( nodeFilterField.getText(), nameFilter) );
			  }
		});
		
		
		final JButton clearButton = (JButton)windowReference.getView( "NodeNameFilterClearButton" );
		clearButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				nodeFilterField.setText( "" );
			}
		});
		
		return nameFilter;
	}
	
	
	/** Performs an operation filter the nodes based on the name pattern */
	private class NodeNameFilterOperation implements Runnable {
		/** text with which to filter */
		final private String FILTER_TEXT;

		/** node name filter */
		final private NodeRecordNameFilter NAME_FILTER;

		/** Constructor */
		public NodeNameFilterOperation( final String filterText, final NodeRecordNameFilter nameFilter ) {
			FILTER_TEXT = filterText;
			NAME_FILTER = nameFilter;
		}
		
		
		/** perform the node filter operation */
		public void run() {
			NAME_FILTER.setFilterText( FILTER_TEXT );
			getNodesTableModel().refreshRecords();	
		}
	}
	
	
	/** 
	 * setup the filter radio buttons 
	 * @param filter node record mode filter associated with the three modes (any, pass, fail)
	 * @param anyPrefix prefix of the "any" radio button tag
	 * @param passPrefix prefix of the "pass" radio button tag
	 * @param failPrefix prefix of the "fail" radio button tag
	 */
	private void setupModeFilterRadioButtons( final NodeRecordModeFilter modeFilter, final String anyPrefix, final String passPrefix, final String failPrefix ) {
		final WindowReference windowReference = _mainWindowReference;
		final JRadioButton anyButton = (JRadioButton)windowReference.getView( anyPrefix + "RadioButton" );
		final JRadioButton passButton = (JRadioButton)windowReference.getView( passPrefix + "RadioButton" );
		final JRadioButton failButton = (JRadioButton)windowReference.getView( failPrefix + "RadioButton" );
		new ModeFilterHandler( modeFilter, anyButton, passButton, failButton );
	}
	
	
	
	/** mode filter handler */
	class ModeFilterHandler implements ActionListener {
		private final NodeRecordModeFilter _modeFilter;
		private final JRadioButton _anyButton;
		private final JRadioButton _passButton;
		
		/** Constructor */
		public ModeFilterHandler( final NodeRecordModeFilter modeFilter, final JRadioButton anyButton, final JRadioButton passButton, final JRadioButton failButton ) {
			_anyButton = anyButton;
			_passButton = passButton;
			_modeFilter = modeFilter;
			
			final ButtonGroup statusGroup = new ButtonGroup();
			statusGroup.add( anyButton );
			statusGroup.add( passButton );
			statusGroup.add( failButton );
			
			anyButton.addActionListener( this );
			passButton.addActionListener( this );
			failButton.addActionListener( this );
		}
		
		/** action handler */
		public void actionPerformed( final ActionEvent event ) {
			updateModeFilter( _modeFilter, _anyButton, _passButton );
		}
	}
}



