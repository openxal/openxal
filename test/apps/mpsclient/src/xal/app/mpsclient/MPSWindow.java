/*
 * MPSWindow.java
 *
 * Created on Tue Feb 17 16:30:56 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import xal.extension.application.*;
import xal.service.mpstool.MPSPortal;
import xal.tools.data.GenericRecord;
import xal.extension.widgets.swing.KeyValueTableModel;
import xal.tools.dispatch.*;
import xal.tools.UpdateListener;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collections;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;


/**
 * MPSWindow is the main window for displaying the status of a remote MPS service.
 *
 * @author  tap
 */
class MPSWindow extends XalWindow implements SwingConstants, DataKeys, ScrollPaneConstants, UpdateListener {
    
    private static final long serialVersionUID = 1L;

	/** date formatter for displaying timestamps */
	static final protected DateFormat TIMESTAMP_FORMAT;
	
	/** Table of MPS tools running on the local network */
	protected JTable mpsTable;
	
	/** State indicating whether the table has any selected rows */
	protected boolean mpsTableHasSelectedRows;
	
	/** Field for entering and displaying the update period */
	protected JTextField periodField;
	
	/** Action for reloading the MPS signals from the global database */
	protected Action _reloadSignalsAction;
	
	/** Action for shutting down the selected service */
	protected Action _shutdownServiceAction;
	
	/** main application wide model */
	protected MPSModel _mainModel;
	
	/** model for this window's document */
	protected DocumentModel _model;
	
    
    /** New OpenXAL Stuff */
    private final KeyValueTableModel<RemoteMPSRecord> MPS_TABLE_MODEL;
    private final DispatchTimer REFRESH_TIMER;
    /** optional handler of the update event */
	private UpdateListener _updateListener;
	
	/**
	 * static initializer
	 */
	static {
		TIMESTAMP_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
	}
	
	
    /** Creates a new instance of MainWindow */
    public MPSWindow(MPSDocument aDocument) {
        super(aDocument);
        setSize(1000, 500);
		_model = aDocument.getModel();
		//_model.addDocumentModelListener(this);
		_mainModel = _model.getMainModel();

        MPS_TABLE_MODEL = new KeyValueTableModel<RemoteMPSRecord>( new ArrayList<RemoteMPSRecord>(), "launchTime", "hostName", "processID", "logsStatistics", "lastCheckTime", "serviceOkay" );

		mpsTableHasSelectedRows = false;
		makeContent();
		manageActions();
		handleMPSEvents();
//		_mainModel.updateServiceList();
        
        mpsTable.setModel ( MPS_TABLE_MODEL );
        
		REFRESH_TIMER = new DispatchTimer( DispatchQueue.getMainQueue(), new Runnable() {
			public void run() {
				for ( final RemoteMPSRecord record : MPS_TABLE_MODEL.getRowRecords() ) {
                    try {
                        record.refresh();
                    }
                    catch(Exception e) {
                        System.out.println("ERROR REFRESHING RECORD");
                    }
				}
                updateView();
				//updateChannelsInspector();
			}
		});
		REFRESH_TIMER.startNowWithInterval( 10000, 0 );	// refresh the table every 10 seconds
        
    } 
	
    /** called when the source posts an update to this observer */
	public void observedUpdate( final Object source ) {
		// propagate update notification to the update listener if any
		final UpdateListener updateHandler = _updateListener;
		if ( updateHandler != null ) {
			updateHandler.observedUpdate( this );
		}
	}
    
	/**
	 * Listen for new MPS status events and update the views accordingly
	 */
	protected void handleMPSEvents() {
		_mainModel.addMPSModelListener( new MPSModelListener() {
			/**
			 * The status of MPS tools have been updated.  Every record specifies information about 
			 * a single application.
			 * @param aModel The model whose services changed
			 * @param records The records of every application found on the local network.
			 */
			public void servicesChanged(MPSModel aModel, final java.util.List<RemoteMPSRecord> records) {
                DispatchQueue.getMainQueue().dispatchAsync( new Runnable() {
					public void run() {
						populateRecords();
						for ( final RemoteMPSRecord record : records ) {
							record.setUpdateListener( MPSWindow.this );
						}
					}
				});
			}
			
			
			/**
			 * The request handler associated with the specified record has checked for new status
			 * information from the remote service.
			 * @param record The request handler record for which the update has been made
			 * @param timestamp The timestamp of the check
			 */
			public void lastCheck(RemoteMPSRecord record, Date timestamp) {}
		});
	}
	
	
	/**
	 * Determine whether to display the toolbar.
	 * @return true to display the toolbar and false otherwise.
	 */
	public boolean usesToolbar() {
		return true;
	}
	
	
	/**
	 * Update the view to reconcile it with the model.
	 */
	protected void updateView() {
        final int rowCount = MPS_TABLE_MODEL.getRowCount();
        if ( rowCount > 0 ) {
            MPS_TABLE_MODEL.fireTableRowsUpdated( 0, rowCount - 1 );
        }
	}
    
    
    private void populateRecords() {
        final java.util.List<RemoteMPSRecord> records = _mainModel.getRemoteMPSTools();
        MPS_TABLE_MODEL.setRecords( records );
    }
	
	
	/**
	 * Build the component contents of the window.
	 * @param mpsTableModel The table model for the MPS view
	 */
	protected void makeContent() {
		Box mainView = new Box(VERTICAL);
		getContentPane().add(mainView);
		
		//mainView.add( makePeriodView() );
		Box mpsPanel = new Box(HORIZONTAL);
		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		splitPane.setTopComponent( makeMPSTable(MPS_TABLE_MODEL) );
		splitPane.setBottomComponent( makeMPSInspector() );
		splitPane.setResizeWeight(0);
		
		addWindowListener( new WindowAdapter() {
			public void windowOpened(WindowEvent event) {
				splitPane.setDividerLocation(0.20);
			}
		});
		
		mpsPanel.add(splitPane);
		mainView.add(mpsPanel);
		
        populateRecords();
	}
	
	/**
	 * Make a table that lists the currently running MPS tools
	 * @param tableModel The table model
	 * @return the table view
	 */
	protected JComponent makeMPSTable(final KeyValueTableModel<RemoteMPSRecord> tableModel) {
		mpsTable = new JTable(tableModel);
		mpsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		TableCellRenderer numericCellRenderer = makeNumericCellRenderer();
		TableColumnModel columnModel = mpsTable.getColumnModel();
    
        JScrollPane mpsScrollPane = new JScrollPane(mpsTable);
		mpsScrollPane.setColumnHeaderView( mpsTable.getTableHeader() );
		mpsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mpsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		return mpsScrollPane;
	}
	
	
	/**
	 * Make an inspector for a selected MPS service
	 * @return the inspector view
	 */
	protected JComponent makeMPSInspector() {
		Box typeBox = new Box(BoxLayout.Y_AXIS);
		typeBox.add( new JLabel("MPS Types: ") );
		final JList<String> typeList = new JList<>();
		typeBox.add(new JScrollPane(typeList));
		typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);	
		typeList.addListSelectionListener( new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if ( event.getValueIsAdjusting() )  return;
				int index = typeList.getSelectedIndex();
				_model.setSelectedMPSTypeIndex(index);
			}
		});
		typeList.setModel( new AbstractListModel<String>() {
            
            private static final long serialVersionUID = 1L;

			{
				_model.addDocumentModelListener(new DocumentModelListener() {
						public void handlerSelected(DocumentModel model, RemoteMPSRecord handler) {
							typeList.clearSelection();
							fireContentsChanged(this, 0, getSize());
						}
						
						public void mpsTypeSelected(DocumentModel model, int index) {}
												
						public void mpsChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}
												
						public void inputChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}
						
						public void mpsEventsUpdated(RemoteMPSRecord handler, int mpsTypeIndex) {}
						
						public void lastCheck(RemoteMPSRecord handler, Date timestamp) {}
				});
			}
			
			public String getElementAt(int index) {
				RemoteMPSRecord handler = _model.getSelectedHandler();
				return (handler != null) ? handler.getMPSTypes().get(index) : "";
			}
			
			public int getSize() {
				RemoteMPSRecord handler = _model.getSelectedHandler();
				return (handler != null) ? handler.getMPSTypes().size() : 0;
			}
		});
		
		JTabbedPane tabPane = new JTabbedPane();
		final JSplitPane inspector = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, typeBox, tabPane);
		inspector.setContinuousLayout(true);
		inspector.setResizeWeight(0);
		
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent event) {
				inspector.setDividerLocation(0.2);
			}
		});
		
		mpsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				int selectedRow = mpsTable.getSelectedRow();
				if ( selectedRow >= 0 ) {
					//MPSTableModel tableModel = (MPSTableModel)mpsTable.getModel();
					//String id = tableModel.getRecord(selectedRow).stringValueForKey(ID_KEY);
					_model.setSelectedHandler( getSelectedRecord() );
				}
				else {
					_model.setSelectedHandler(null);
				}
			}
		});
		
		tabPane.addTab("Latest Event", makeLatestMPSEventView() );
		tabPane.addTab("First Hits", makeFirstHitsView() );
		tabPane.addTab("Trip Summary", makeTripSummaryView() );
		tabPane.addTab("MPS PVs", makeMPSPVsTab() );
		tabPane.addTab("Input PVs", makeInputPVsTab() );
		
		return inspector;
	}
	
    private RemoteMPSRecord getSelectedRecord() {
        
        final int selectedRow = mpsTable.getSelectedRow();
        
        if(selectedRow >= 0) {
            final int modelRow = mpsTable.convertRowIndexToModel(selectedRow);
            return MPS_TABLE_MODEL.getRecordAtRow(modelRow);
        } else {
            return null;
        }
        
    }
	
	/**
	 * Make the view that displays the first hit statistics.
	 *
	 * @return the view that displays the first hit statistics
	 */
	protected JComponent makeFirstHitsView() {
		Box statsView = new Box(VERTICAL);
		statsView.add( new JLabel("Daily First Hit Summary:") );
		final JTextArea statsTextView = new JTextArea();
		statsTextView.setEditable(false);
		statsView.add( new JScrollPane( statsTextView, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED ) );
		
		Box buttonRow = new Box(HORIZONTAL);
		statsView.add(buttonRow);
		buttonRow.add(Box.createHorizontalGlue());
		final JButton dumpButton = new JButton("dump");
		buttonRow.add(dumpButton);
		dumpButton.setEnabled(false);
		dumpButton.addActionListener( new ActionListener() {
			JFileChooser fileChooser = new JFileChooser();
			
			public void actionPerformed(ActionEvent event) {
				try {
					String text = statsTextView.getText();
					fileChooser.setSelectedFile( new File(fileChooser.getCurrentDirectory(), "Untitled.txt") );
					int status = fileChooser.showSaveDialog(MPSWindow.this);
					switch(status) {
						case JFileChooser.APPROVE_OPTION:
							File selectedFile = fileChooser.getSelectedFile();
							if ( selectedFile.exists() ) {
								int confirm = displayConfirmDialog("Warning", "The selected file:  " + selectedFile + " already exists! \n Overwrite selection?");
								if ( confirm == NO_OPTION )  return;
							}
							else {
								selectedFile.createNewFile();
							}
							FileWriter writer = new FileWriter(selectedFile);
							writer.write(text, 0, text.length());
							writer.flush();
							break;
						default:
							break;
					}
				}
				catch(Exception exception) {
					displayError("Save Error", "Error saving file: ", exception);
				}
			}
		});
		
		_model.addDocumentModelListener( new DocumentModelListener() {
			public void handlerSelected(DocumentModel model, RemoteMPSRecord handler) {
				updateLog();
			}
	
			public void mpsTypeSelected(DocumentModel model, int index) {
				updateLog();
			}

			public void mpsChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}

			public void inputChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}
			
			public void mpsEventsUpdated(RemoteMPSRecord handler, int mpsTypeIndex) {
				updateLog();
			}
			
			public void lastCheck(RemoteMPSRecord handler, Date timestamp) {}
			
			protected void updateLog() {
				final int mpsType = _model.getSelectedMPSTypeIndex();
				final RemoteMPSRecord handler = _model.getSelectedHandler();
				
				String text = "";
				if ( mpsType >= 0 && handler != null ) {
					text = handler.getFirstHitText(mpsType);
				}
				statsTextView.setText(text);
				statsTextView.setSelectionStart(0);
				statsTextView.moveCaretPosition(0);
				dumpButton.setEnabled( text != "" && text != null );
			}
		});
		
		return statsView;
	}
	
	
	/**
	 * Make the view that displays the daily MPS trips summary.
	 *
	 * @return the view that displays the daily MPS trip summary
	 */
	protected JComponent makeTripSummaryView() {
		Box statsView = new Box(VERTICAL);
		statsView.add( new JLabel("Daily Trip Summary:") );
		final JTextArea statsTextView = new JTextArea();
		statsTextView.setEditable(false);
		statsView.add( new JScrollPane( statsTextView, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED ) );
		
		Box buttonRow = new Box(HORIZONTAL);
		statsView.add(buttonRow);
		buttonRow.add(Box.createHorizontalGlue());
		final JButton dumpButton = new JButton("dump");
		buttonRow.add(dumpButton);
		dumpButton.setEnabled(false);
		dumpButton.addActionListener( new ActionListener() {
			JFileChooser fileChooser = new JFileChooser();
			
			public void actionPerformed(ActionEvent event) {
				try {
					String text = statsTextView.getText();
					fileChooser.setSelectedFile( new File(fileChooser.getCurrentDirectory(), "Untitled.txt") );
					int status = fileChooser.showSaveDialog(MPSWindow.this);
					switch(status) {
						case JFileChooser.APPROVE_OPTION:
							File selectedFile = fileChooser.getSelectedFile();
							if ( selectedFile.exists() ) {
								int confirm = displayConfirmDialog("Warning", "The selected file:  " + selectedFile + " already exists! \n Overwrite selection?");
								if ( confirm == NO_OPTION )  return;
							}
							else {
								selectedFile.createNewFile();
							}
							FileWriter writer = new FileWriter(selectedFile);
							writer.write(text, 0, text.length());
							writer.flush();
							break;
						default:
							break;
					}
				}
				catch(Exception exception) {
					displayError("Save Error", "Error saving file: ", exception);
				}
			}
		});
		
		_model.addDocumentModelListener( new DocumentModelListener() {
			public void handlerSelected(DocumentModel model, RemoteMPSRecord handler) {
				updateLog();
			}
	
			public void mpsTypeSelected(DocumentModel model, int index) {
				updateLog();
			}

			public void mpsChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}

			public void inputChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}
			
			public void mpsEventsUpdated(RemoteMPSRecord handler, int mpsTypeIndex) {
				updateLog();
			}
			
			public void lastCheck(RemoteMPSRecord handler, Date timestamp) {}
			
			protected void updateLog() {
				final int mpsType = _model.getSelectedMPSTypeIndex();
				final RemoteMPSRecord handler = _model.getSelectedHandler();
				String text = "";
				if ( mpsType >= 0 && handler != null ) {
                    text = handler.getMPSTripSummary(mpsType);
//                    System.out.println("TRIP SUMMARY[" + mpsType + "]=" + text);
				}
				statsTextView.setText(text);
				statsTextView.setSelectionStart(0);
				statsTextView.moveCaretPosition(0);
				dumpButton.setEnabled( text != "" && text != null );
			}
		});
		
		return statsView;
	}
	
	
	/**
	 * Make the view that displays the latest MPS event.
	 * 
	 * @return the view that displays the latest MPS event
	 */
	protected JComponent makeLatestMPSEventView() {
		final Box eventView = new Box(VERTICAL);
		//eventView.add( new JLabel("Latest MPS Event:") );
		final JLabel eventTimestampLabel = new JLabel("");
		eventView.add(eventTimestampLabel);
		final MPSEventTableModel eventTableModel = new MPSEventTableModel(null);
		JTable eventTable = new JTable(eventTableModel);
		JScrollPane eventScrollPane = new JScrollPane(eventTable);
		eventView.add(eventScrollPane);
		eventScrollPane.setColumnHeaderView( eventTable.getTableHeader() );
		
		Box eventButtonRow = new Box(HORIZONTAL);
		eventView.add(eventButtonRow);
		eventButtonRow.add( Box.createHorizontalGlue() );
		final JButton bufferButton = new JButton("Buffer");
		bufferButton.setEnabled(false);
		eventButtonRow.add(bufferButton);
		bufferButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int mpsType = _model.getSelectedMPSTypeIndex();
				RemoteMPSRecord handler = _model.getSelectedHandler();
				if ( mpsType >= 0 && handler != null ) {
					EventBufferDocument bufferDocument =  new EventBufferDocument(handler, mpsType);
					Application.getApp().produceDocument(bufferDocument, false);
					bufferDocument.getMainWindow().setLocationRelativeTo(MPSWindow.this);
					bufferDocument.showDocument();
				}
			}
		});
		
		_model.addDocumentModelListener( new DocumentModelListener() {
			public void handlerSelected(DocumentModel model, RemoteMPSRecord handler) {
				updateLog();
			}
	
			public void mpsTypeSelected(DocumentModel model, int index) {
				updateLog();
			}

			public void mpsChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}

			public void inputChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}
			
			public void mpsEventsUpdated(RemoteMPSRecord handler, int mpsTypeIndex) {
				updateLog();
			}
			
			public void lastCheck(RemoteMPSRecord handler, Date timestamp) {}
			
			protected void updateLog() {
                
				final int mpsType = _model.getSelectedMPSTypeIndex();
				final RemoteMPSRecord handler = _model.getSelectedHandler();
				if ( mpsType >= 0 && handler != null ) {
					MPSEvent mpsEvent = handler.getLatestMPSEvent(mpsType);
					if ( mpsEvent != null ) {
						eventTimestampLabel.setText( mpsEvent.getTimestamp().toString() );
						eventTableModel.setEvent(mpsEvent);
						bufferButton.setEnabled(true);
					}
					else {
//                        System.out.println("LATEST MPS EVENT IS NULL");
						clearEvent();
					}
				}
				else {
					clearEvent();
				}				
			}
			
			protected void clearEvent() {
				eventTimestampLabel.setText("");
				eventTableModel.setEvent(null);
				bufferButton.setEnabled(false);
			}
		});
		
		return eventView;
	}
	
	
	/**
	 * Make a tab that displays the pvs being logged and distinguishes those that are connected
	 * from those that are not connected.
	 * @return the tab view
	 */
	protected JComponent makeMPSPVsTab() {
		Box view = new Box(VERTICAL);
		
		final JList<String> pvList = new JList<>();
		view.add( new JScrollPane(pvList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER) );
		final java.util.List<ChannelRef> channelRefs = new ArrayList<>();
		
		pvList.setModel( new AbstractListModel<String>() {
            
            private static final long serialVersionUID = 1L;

			{
				_model.addDocumentModelListener( new DocumentModelListener() {
					public void handlerSelected(DocumentModel model, RemoteMPSRecord handler) {
						updatePVs( this );
					}
			
					public void mpsTypeSelected(DocumentModel model, int index) {
						updatePVs( this );
					}
	
					public void mpsChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {
						updatePVs( this );
					}
	
					public void inputChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}
					
					public void mpsEventsUpdated(RemoteMPSRecord handler, int mpsTypeIndex) {}
					
					public void lastCheck(RemoteMPSRecord handler, Date timestamp) {}
				});
			}
			

			public void updatePVs( final DocumentModelListener source ) {
				channelRefs.clear();
				final int mpsType = _model.getSelectedMPSTypeIndex();
				final RemoteMPSRecord handler = _model.getSelectedHandler();
				if ( mpsType >= 0 && handler != null ) {
                    try {
					java.util.List<ChannelRef> refs = new ArrayList<>( handler.getMPSPVs( mpsType ) );
                        if(refs.size() > 0) {
                            channelRefs.addAll( refs );
                            System.out.println("[TYPE " + mpsType + "] ADDED PV=" + channelRefs.get(0) );
                        }
                    }
                    catch(Exception e) {
                        System.err.println("");
                    }
				}
                else {
                    channelRefs.clear();
                }
				
				fireContentsChanged( source, 0, getSize() );
			}


			public int getSize() {
				int mpsType = _model.getSelectedMPSTypeIndex();
				RemoteMPSRecord handler = _model.getSelectedHandler();
				final java.util.List<ChannelRef> mpsPVs = ( mpsType >= 0 && handler != null ) ? handler.getMPSPVs( mpsType ) : null;
				return mpsPVs != null ? mpsPVs.size() : 0;
			}

			
			public String getElementAt(int index) {
				try {
					ChannelRef channelRef = channelRefs.get(index);
					String pv = channelRef.getPV();
					return (channelRef.isConnected()) ? pv : "<html><body><font COLOR=#ff0000>" + pv + "</body></html>";
				}
				catch ( IndexOutOfBoundsException exception ) {
					return "";
				}
			}
		});
		
		return view;
	}
	
	
	/**
	 * Make a tab that displays the pvs being logged and distinguishes those that are connected
	 * from those that are not connected.
	 * @return the tab view
	 */
	protected JComponent makeInputPVsTab() {
		Box view = new Box(VERTICAL);
		
		final JList<String> pvList = new JList<>();
		view.add( new JScrollPane(pvList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER) );
		
		final java.util.List<ChannelRef> channelRefs = new ArrayList<>();
		
		pvList.setModel( new AbstractListModel<String>() {
            
            private static final long serialVersionUID = 1L;

			{
				_model.addDocumentModelListener( new DocumentModelListener() {
					public void handlerSelected( DocumentModel model, RemoteMPSRecord handler ) {
						updatePVs( this );
					}
			
					public void mpsTypeSelected( DocumentModel model, int index ) {
						updatePVs( this );
					}
	
					public void mpsChannelsUpdated( RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs ) {}
	
					public void inputChannelsUpdated( RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs ) {
                        System.out.println("INPUT CHANNELS UPDATED");
                        updatePVs( this );
					}
					
					public void mpsEventsUpdated( RemoteMPSRecord handler, int mpsTypeIndex ) {}
					
					public void lastCheck( RemoteMPSRecord handler, Date timestamp ) {}
				});
			}
			
			
			public void updatePVs( final DocumentModelListener source ) {
				channelRefs.clear();
				final int mpsType = _model.getSelectedMPSTypeIndex();
				final RemoteMPSRecord handler = _model.getSelectedHandler();
                //handler.refresh();
				if ( mpsType >= 0 && handler != null ) {
                    // TODO: HANDLE - NO EVENTS, check for 0 size
                    try {
						final java.util.List<ChannelRef> refs = new ArrayList<ChannelRef>( handler.getInputPVs(mpsType) );
						if(refs.size() > 0)  channelRefs.addAll( refs );
                    }
                    catch (Exception e) {
                        System.err.println("");
                    }
				}
					
				fireContentsChanged( source, 0, getSize() );				
			}

			
			public int getSize() {
				return channelRefs.size();
			}
			
			
			public String getElementAt( int index ) {
				try {
					ChannelRef channelRef = channelRefs.get(index);
					String pv = channelRef.getPV();
					return (channelRef.isConnected()) ? pv : "<html><body><font COLOR=#ff0000>" + pv + "</body></html>";
				}
				catch( IndexOutOfBoundsException exception ) {
					return "";
				}
			}
		});
		
		return view;
	}
	
	
	/**
	 * Convenience method for getting the document as an instance of HistoryDocument.
	 * @return The document cast as an instace of HistoryDocument.
	 */
	public MPSDocument getDocument() {
		return (MPSDocument)document;
	}
	
	
	/**
	 * Get the MPS model
	 * @return The MPS model
	 */
	public DocumentModel getModel() {
		return getDocument().getModel();
	}
	
	
	/**
	 * Apply to the model the period setting from the period field.
	 */
	protected void applyPeriodSetting() {
		try {
			int period = Integer.parseInt( periodField.getText() );
			period = Math.max(period, 1);
			period = Math.min(period, 999);
			//_mainModel.setUpdatePeriod(period);
		}
		catch(NumberFormatException exception) {
			Toolkit.getDefaultToolkit().beep();
		}
		updateView();
	}
    
    
    /**
     * Right justify text associated with numeric values.
     * @return A renderer for numeric values.
     */
    private TableCellRenderer makeNumericCellRenderer() {
        return new DefaultTableCellRenderer() {
            
            private static final long serialVersionUID = 1L;

            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(RIGHT);
                return label;
            }
        };
    }
	
	
	/**
	 * Add event listeners to manage the enable state of the actions.
	 */
	protected void manageActions() {
		setTableSelectionActionsEnabled(false);
		mpsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				setTableSelectionActionsEnabled(mpsTable.getSelectedRow() >= 0);
			}
		});		
	}
	
	
	/**
	 * Enable/disable table selection actions depending on the specified state.
	 * @param state enable actions if true and disable actions if false
	 */
	protected void setTableSelectionActionsEnabled(boolean state) {
		mpsTableHasSelectedRows = state;
//		updateView();
	}
    
    
    /**
     * Register actions specific to this window instance. 
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands(Commander commander) {
		// setup the "reload signals" action
        _reloadSignalsAction = new AbstractAction("reload-signals") {
            
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				RemoteMPSRecord handler = _model.getSelectedHandler();
				handler.reloadSignals();
            }
		};
		_reloadSignalsAction.setEnabled(false);
		commander.registerAction(_reloadSignalsAction);
		
		// setup the "shutdown service" action
        _shutdownServiceAction = new AbstractAction("shutdown-service") {
            
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				final String message = "Are you sure you want to shutdown the selected service?";
				int result = JOptionPane.showConfirmDialog(MPSWindow.this, message, "Careful!", JOptionPane.YES_NO_OPTION);
				if ( result == JOptionPane.YES_OPTION ) {
					RemoteMPSRecord handler = _model.getSelectedHandler();
					handler.shutdown(0);
				}
            }
		};
		_shutdownServiceAction.setEnabled(false);
		commander.registerAction(_shutdownServiceAction);
	}
	
	
	/**
	 * Indicates that a new handler has been selected.
	 * @param model The document model posting the event.
	 * @param handler The selected handler or null if no handler is selected.
	 */
	public void handlerSelected(DocumentModel model, RemoteMPSRecord handler) {
		_reloadSignalsAction.setEnabled(handler != null);
		_shutdownServiceAction.setEnabled(handler != null);
	}
	
	
	/**
	 * This event is sent to indicate that a new MPS type has been selected.
	 * @param model The model sending the event
	 * @param index The index of the MPS type selected or -1 if none is selected
	 */
	public void mpsTypeSelected(DocumentModel model, int index) {}
	
	
	/**
	 * Indicates that MPS channels have been updated.
	 * @param handler The handler sending the event
	 * @param mpsTypeIndex index of the MPS type for which the event applies
	 * @param channelRefs The list of the new ChannelRef instances
	 */
	public void mpsChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}
	
	
	/**
	 * Indicates that input channels have been updated.
	 * @param handler The handler sending the event
	 * @param mpsTypeIndex index of the MPS type for which the event applies
	 * @param channelRefs The list of the new ChannelRef instances
	 */
	public void inputChannelsUpdated(RemoteMPSRecord handler, int mpsTypeIndex, java.util.List<ChannelRef> channelRefs) {}
	
	
	/**
	 * Indicates that an MPS event has happened.
	 * @param handler The handler sending the event
	 * @param mpsTypeIndex index of the MPS type for which the event applies
	 */				
	public void mpsEventsUpdated(RemoteMPSRecord handler, int mpsTypeIndex) {}	
	
	
	/**
	 * Indicates that the handler has checked for new status from the MPS service.
	 * @param handler The handler sending the event.
	 * @param timestamp The timestamp of the latest status check
	 */
	public void lastCheck(RemoteMPSRecord handler, Date timestamp) {}
}

