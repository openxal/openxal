//
//  AttachmentController.java
//  xal
//
//  Created by Thomas Pelaia on 9/25/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import xal.extension.bricks.*;
import xal.tools.messaging.MessageCenter;

import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;


/** controller for attachments */
public class AttachmentController extends AbstractController {
	/** file watcher */
	final protected FileWatcher FILE_WATCHER;
	
	/** folder watch list model */
	final protected FolderWatchListModel FOLDER_WATCH_LIST_MODEL;
	
	/** file table model */
	final protected FileTableModel FILE_TABLE_MODEL;
	
	/** media entry table model */
	final protected AttachmentEntryTableModel MEDIA_ENTRY_TABLE_MODEL;
	
	/** file chooser for selecting folders */
	protected JFileChooser _folderChooser;
	
	/** file chooser for selecting media files */
	protected JFileChooser _mediaChooser;
	
	
	/** Constructor */
	public AttachmentController( final MessageCenter messageCenter, final WindowReference windowReference ) {
		super( messageCenter, windowReference );
		
		FILE_WATCHER = FileWatcher.getAttachmentWatcherInstance( AttachmentController.class );
		
		MEDIA_ENTRY_TABLE_MODEL = new AttachmentEntryTableModel();
		FOLDER_WATCH_LIST_MODEL = new FolderWatchListModel();
		FILE_TABLE_MODEL = new FileTableModel();
		
		setupAttachmentEntryTable( windowReference );
		setupFolderWatchList( windowReference );
		setupFilePicker( windowReference );
	}
	
	
	/** get the attachment entries */
	public List<AttachmentEntry> getMediaEntries() {
		return MEDIA_ENTRY_TABLE_MODEL.getMediaEntries();
	}
	
	
	/**
	 * Validate whether the summary is valid
	 * @return true if the summary is valid and false if not
	 */
	public boolean validate() {
		final List<AttachmentEntry> entries = getMediaEntries();
		for ( final AttachmentEntry entry : entries ) {
			final int titleLength = entry.getTitle().length();
			if ( titleLength == 0 ) {
				_validationText = "Attachment title must have at least one character.";
				return false;
			}
			else if ( titleLength > 120 ) {
				_validationText = "Attachment title length must be less than 120 characters.  \n\"" + entry.getTitle() + "\" has " + titleLength;
				return false;
			}
			else if ( !entry.getMediaFile().exists() ) {
				_validationText = "Attachment file \"" + entry.getTitle() + "\" does not exist";
				return false;
			}
		}
		
		_validationText = "Okay";
		return true;
	}
	
	
	/**
	 * Get the validation text
	 * @return the validation text
	 */
	public String getValidationText() {
		return _validationText;
	}
	
	
	/** setup the attachment entry table */
	protected void setupAttachmentEntryTable( final WindowReference windowReference ) {
		final JTable mediaEntryTable = (JTable)windowReference.getView( "AttachmentTable" );
		mediaEntryTable.setModel( MEDIA_ENTRY_TABLE_MODEL );
		mediaEntryTable.setDragEnabled( true );
		mediaEntryTable.setTransferHandler( new AttachmentEntryListTransferHandler() );
		
		mediaEntryTable.addMouseMotionListener( new MouseMotionAdapter() {
			public void mouseMoved( final MouseEvent event ) {
				final int row = mediaEntryTable.rowAtPoint( event.getPoint() );
				if ( row >= 0 ) {
					final AttachmentEntry entry = MEDIA_ENTRY_TABLE_MODEL.getMediaEntry( row );
					if ( entry != null ) {
						mediaEntryTable.setToolTipText( entry.getMediaFile().getAbsolutePath() );
					}
					else {
						mediaEntryTable.setToolTipText( null );
					}
				}
				else {
					mediaEntryTable.setToolTipText( null );
				}
			}
		});
		
		mediaEntryTable.addMouseListener( new MouseAdapter() {
			public void mouseExited( final MouseEvent event ) {
				mediaEntryTable.setToolTipText( null );
			}
		});		
	}
	
	
	/** setup the files table */
	protected void setupFilePicker( final WindowReference windowReference ) {
		final JTable filesTable = (JTable)windowReference.getView( "AttachmentFilesTable" );
		filesTable.setModel( FILE_TABLE_MODEL );
		filesTable.setDragEnabled( true );
		filesTable.setTransferHandler( new FileListTransferHandler() );
		
		filesTable.addMouseMotionListener( new MouseMotionAdapter() {
			public void mouseMoved( final MouseEvent event ) {
				final int row = filesTable.rowAtPoint( event.getPoint() );
				if ( row >= 0 ) {
					final File file = FILE_TABLE_MODEL.getFile( row );
					if ( file != null ) {
						filesTable.setToolTipText( file.getAbsolutePath() );
					}
					else {
						filesTable.setToolTipText( null );
					}
				}
				else {
					filesTable.setToolTipText( null );
				}
			}
		});
		
		filesTable.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				if ( event.getClickCount() == 2 ) {
					final int row = filesTable.rowAtPoint( event.getPoint() );
					if ( row >= 0 ) {
						final File file = FILE_TABLE_MODEL.getFile( row );
						final AttachmentEntry entry = new AttachmentEntry( file );
						final List<AttachmentEntry> entries = new ArrayList<AttachmentEntry>( MEDIA_ENTRY_TABLE_MODEL.getMediaEntries() );
						entries.add( entry );
						MEDIA_ENTRY_TABLE_MODEL.setMediaEntries( entries );
						postDocumentChangeEvent();
					}
				}	
			}
			
			public void mouseExited( final MouseEvent event ) {
				filesTable.setToolTipText( null );
			}
		});
		
		final JButton filesRefreshButton = (JButton)windowReference.getView( "AttachmentFilesRefreshButton" );
		filesRefreshButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				refreshFilesList();
			}
		});
		
		final JButton mediaBrowseButton = (JButton)windowReference.getView( "AttachmentBrowseButton" );
		mediaBrowseButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser mediaChooser = getMediaChooser();
				final int status = mediaChooser.showOpenDialog( mediaBrowseButton );
				switch( status ) {
					case JFileChooser.CANCEL_OPTION:
						break;
					case JFileChooser.APPROVE_OPTION:
						final File[] selections = mediaChooser.getSelectedFiles();
						if ( selections.length > 0 ) {
							final List<AttachmentEntry> entries = new ArrayList<AttachmentEntry>( MEDIA_ENTRY_TABLE_MODEL.getMediaEntries() );
							for ( final File selection : selections ) {
								final AttachmentEntry entry = new AttachmentEntry( selection );
								entries.add( entry );
							}
							MEDIA_ENTRY_TABLE_MODEL.setMediaEntries( entries );
						}
							break;
					case JFileChooser.ERROR_OPTION:
						break;
					default:
						break;
				}
			}
		});		
		
		refreshFilesList();
	}
	
	
	/** refresh the files */
	protected void refreshFilesList() {
		final List<File> files = FILE_WATCHER.listFiles();
		FILE_TABLE_MODEL.setFiles( files );
	}
	
	
	/** setup the folder watch list */
    // Had to suppress unchecked warnings because getView returns an object and it cannot be cast.
    @SuppressWarnings ("unchecked") 
	protected void setupFolderWatchList( final WindowReference windowReference ) {
		final JList<String> watchList = (JList<String>)windowReference.getView( "AttachmentFolderList" );
		watchList.setModel( FOLDER_WATCH_LIST_MODEL );
		FOLDER_WATCH_LIST_MODEL.setFolders( new ArrayList<File>( FILE_WATCHER.getFolders() ) );
		
		final JButton folderDeleteButton = (JButton)windowReference.getView( "AttachmentFolderDeleteButton" );
		folderDeleteButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int[] selectedIndices = watchList.getSelectedIndices();
				if ( selectedIndices.length > 0 ) {
					final List<File> selectedFolders = new ArrayList<File>();
					for ( final int index : selectedIndices ) {
						selectedFolders.add( FILE_WATCHER.getFolder( index ) );
					}
					for ( final File folder : selectedFolders ) {
						FILE_WATCHER.ignoreFolder( folder );
					}
					FOLDER_WATCH_LIST_MODEL.setFolders( new ArrayList<File>( FILE_WATCHER.getFolders() ) );
					refreshFilesList();
				}
			}
		});
		
		final JButton folderAddButton = (JButton)windowReference.getView( "AttachmentFolderAddButton" );
		folderAddButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser folderChooser = getFolderChooser();
				final int status = folderChooser.showOpenDialog( folderAddButton );
				switch( status ) {
					case JFileChooser.CANCEL_OPTION:
						break;
					case JFileChooser.APPROVE_OPTION:
						final File[] selections = folderChooser.getSelectedFiles();
						if ( selections.length > 0 ) {
							for ( final File selection : selections ) {
								FILE_WATCHER.watchFolder( selection );
							}
							FOLDER_WATCH_LIST_MODEL.setFolders( new ArrayList<File>( FILE_WATCHER.getFolders() ) );
							refreshFilesList();
						}
							break;
					case JFileChooser.ERROR_OPTION:
						break;
					default:
						break;
				}
			}
		});		
	}
	
	
	/** get the folder chooser creating it if one does not already exist */
	protected JFileChooser getFolderChooser() {
		if ( _folderChooser == null )  {
			_folderChooser = new JFileChooser();
			_folderChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
			_folderChooser.setMultiSelectionEnabled( true );
		}
		
		return _folderChooser;
	}
	
	
	/** get the file chooser for media creating it if one does not already exist */
	protected JFileChooser getMediaChooser() {
		if ( _mediaChooser == null )  {
			_mediaChooser = new JFileChooser();
			_mediaChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
			_mediaChooser.setMultiSelectionEnabled( true );
		}
		
		return _mediaChooser;
	}
	
	
	
	/** File list transfer handler */
	class FileListTransferHandler extends TransferHandler {
        /** ID for serializable version */
        private static final long serialVersionUID = 1L;
		/** transfer files from the files list to media entries */
		protected Transferable createTransferable( final JComponent component ) {
			final JTable table = (JTable)component;
			final int[] rows = table.getSelectedRows();
			if ( rows.length >= 0 ) {
				final List<File> files = new ArrayList<File>( rows.length );
				for ( final int row : rows ) {
					final File file = FILE_TABLE_MODEL.getFile( row );
					files.add( file );
				}
				return AttachmentEntry.getTransferable( files );
			}
			else {
				return null;
			}
		}
		
		
		/** provides copy or move operation */
		public int getSourceActions( final JComponent component ) {
			return TransferHandler.COPY;
		}
	}
	
	
	/** Attachment entry list tranfer handler */
	class AttachmentEntryListTransferHandler extends TransferHandler {
        /** ID for serializable version */
        private static final long serialVersionUID = 1L;
		/** rows of the entries copied */
		protected final List<Integer> ROWS_COPIED;
		
		/** row onto which the entries were dropped */
		protected int dropRow;
		
		
		/** Constructor */
		public AttachmentEntryListTransferHandler() {
			ROWS_COPIED = new ArrayList<Integer>();
		}
		
		
		/** transfer files from the files list to media entries */
		protected Transferable createTransferable( final JComponent component ) {
			dropRow = -1;
			ROWS_COPIED.clear();
			
			final JTable table = (JTable)component;
			final int[] rows = table.getSelectedRows();
			if ( rows.length >= 0 ) {
				final List<AttachmentEntry> entries = new ArrayList<AttachmentEntry>( rows.length );
				for ( final int row : rows ) {
					final AttachmentEntry entry = MEDIA_ENTRY_TABLE_MODEL.getMediaEntry( row );
					entries.add( entry );
					ROWS_COPIED.add( row );
				}
				return AttachmentEntry.getTransferableForEntries( entries );
			}
			else {
				return null;
			}
		}
		
		
		/** provides copy or move operation */
		public int getSourceActions( final JComponent component ) {
			return TransferHandler.MOVE;
		}
		
		
		/** determine if the table can import at least one of the tranferable flavors */
		public boolean canImport( final JComponent component, final DataFlavor[] flavors ) {
			for ( DataFlavor flavor : flavors ) {
				if ( flavor == AttachmentEntryTransferable.MEDIA_ENTRY_FLAVOR )  return true;
			}
			return false;
		}
		
		
		/** import the transferable */
        // Had to suppress warnings cast from transferable to List<AttachmentEntry>
        @SuppressWarnings ("unchecked")
		public boolean importData( final JComponent component, final Transferable transferable ) {
			final JTable table = (JTable)component;
			try {
				final int selectedRow = table.getSelectedRow();
				dropRow = selectedRow;
				final List<AttachmentEntry> entries = new ArrayList<AttachmentEntry>( MEDIA_ENTRY_TABLE_MODEL.getMediaEntries() );
				final List<AttachmentEntry> transfers = (List<AttachmentEntry>)transferable.getTransferData( AttachmentEntryTransferable.MEDIA_ENTRY_FLAVOR );
				if ( selectedRow >= 0 ) {
					int row = selectedRow;
					for ( final AttachmentEntry entry : transfers ) {
						entries.add( row++, entry );
					}
				}
				else {
					for ( final AttachmentEntry entry : transfers ) {
						entries.add( entry );
					}
				}
				MEDIA_ENTRY_TABLE_MODEL.setMediaEntries( entries );
				postDocumentChangeEvent();
				return true;
			}
			catch( UnsupportedFlavorException exception ) {
				exception.printStackTrace();
				return false;
			}
			catch( java.io.IOException exception ) {
				exception.printStackTrace();
				return false;
			}
			catch( Exception exception ) {
				exception.printStackTrace();
				return false;
			}
		}
		
		
		/** complete the transfer */
		public void exportDone( final JComponent component, final Transferable transferable, final int action ) {
			switch( action ) {
				case MOVE:
					final JTable table = (JTable)component;
					final int selectedRow = table.getSelectedRow();
					final List<AttachmentEntry> entries = new ArrayList<AttachmentEntry>( MEDIA_ENTRY_TABLE_MODEL.getMediaEntries() );
					Collections.sort( ROWS_COPIED );
					Collections.reverse( ROWS_COPIED );
					if ( dropRow < 0 && selectedRow >= 0 ) {
						for ( final Number rowNumber : ROWS_COPIED ) {
							final int row = rowNumber.intValue();
							entries.remove( row );
						}
					}
					else if ( dropRow >= 0 ) {
						final int rowShift = ROWS_COPIED.size();
						for ( final Number rowNumber : ROWS_COPIED ) {
							final int row = rowNumber.intValue();
							if ( row < dropRow ) {
								entries.remove( row );
							}
							else {
								entries.remove( row + rowShift );
							}
						}						
					}
					MEDIA_ENTRY_TABLE_MODEL.setMediaEntries( entries );
					ROWS_COPIED.clear();
					postDocumentChangeEvent();
					break;
				default:
					break;
			}
		}
	}	
}
