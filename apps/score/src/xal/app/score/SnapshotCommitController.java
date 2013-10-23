//
// SnapshotCommitController.java: Source file for 'SnapshotCommitController'
// Project xal
//
// Created by t6p on 7/29/10
//

package xal.app.score;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.event.*;

import xal.extension.application.XalDocument;
import xal.extension.bricks.WindowReference;


/** SnapshotCommitController */
public class SnapshotCommitController {
	/** maximum valid length of a comment */
	static final private int MAX_COMMENT_LENGTH = 250;
	
	/** dialog window */
	final private JDialog DIALOG;
	
	/** text pane for entering the comment */
	final private JTextArea COMMENT_PANE;
	
	/** button for saving the comment */
	final private JButton SAVE_BUTTON;
	
	/** indicates whether the user has pressed the save button */
	volatile private boolean _shouldCommit;
	
	
	/** Constructor */
    private SnapshotCommitController( final WindowReference dialogReference ) {
		DIALOG = (JDialog)dialogReference.getWindow();
		
		SAVE_BUTTON = (JButton)dialogReference.getView( "SaveButton" );
		SAVE_BUTTON.setDefaultCapable( true );
		DIALOG.getRootPane().setDefaultButton( SAVE_BUTTON );
		SAVE_BUTTON.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				dismissDialog( true );
			}
		});
		
		final JButton cancelButton = (JButton)dialogReference.getView( "CancelButton" );		
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				dismissDialog( false );
			}
		});
		
		COMMENT_PANE = (JTextArea)dialogReference.getView( "CommentTextPane" );
		COMMENT_PANE.setDocument( new PlainDocument() {
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
			public void insertString( final int offset, final String newText, final AttributeSet attributes ) throws BadLocationException {
				if ( newText != null && getLength() + newText.length() > MAX_COMMENT_LENGTH ) {
					java.awt.Toolkit.getDefaultToolkit().beep();
				}
				else {
					super.insertString( offset, newText, attributes );
					handleTextChange();
				}
			}
			
			
			public void remove( final int offset, final int length ) throws BadLocationException {
				super.remove( offset, length );
				handleTextChange();
			}
			
			private void handleTextChange() {
				final String text = COMMENT_PANE.getText();
				SAVE_BUTTON.setEnabled( text != null && text.length() > 0 ); 
			}
		});		
    }
	
	
	/** 
	 * Instantiate and display the dialog 
	 * @param document the document whose window is the owner of the dialog
	 * @return the user's comment or null if the user canceled
	 */
	public static String showDialog( final XalDocument document ) {
		final JFrame mainWindow = document.getMainWindow();
		final WindowReference dialogReference = XalDocument.getDefaultWindowReference( "SnapshotCommitDialog", mainWindow );
		return new SnapshotCommitController( dialogReference ).displayDialogNear( mainWindow );
	}
	
	
	/** 
	 * Display the dialog with the specified window as the owner
	 * @param window owner of this dialog
	 * @return the user's comment or null if the user canceled
	 */
	private String displayDialogNear( final JFrame window ) {
		SAVE_BUTTON.setEnabled( false );
		COMMENT_PANE.setText( "" );
		_shouldCommit = false;
		
		DIALOG.setLocationRelativeTo( window );
		DIALOG.setVisible( true );
		
		return _shouldCommit ? COMMENT_PANE.getText() : null;
	}
	
	
	/** dismiss the dialog and with the indicated save status */
	private void dismissDialog( final boolean shouldSave ) {
		_shouldCommit = shouldSave;
		DIALOG.setVisible( false );
	}
}
