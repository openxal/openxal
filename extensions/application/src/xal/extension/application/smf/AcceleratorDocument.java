/*
 * AcceleratorDocument.java
 *
 * Created on May 20, 2003, 12:37 PM
 */

package xal.extension.application.smf;

import xal.smf.*;
import xal.extension.application.*;
import xal.smf.data.*;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;


/**
 * AcceleratorDocument is a subclass of XalDocument for accelerator based applications.
 * @author  tap
 */
abstract public class AcceleratorDocument extends XalDocument {
    protected Accelerator accelerator;
    protected AcceleratorSeq selectedSequence;
    protected String acceleratorFilePath;
    protected List<AcceleratorSeq> selectedSequenceList;
    
	
    /** Creates a new instance of AcceleratorDocument */
    public AcceleratorDocument() {
        super();
        accelerator = null;
		acceleratorFilePath = null;
		selectedSequenceList = new ArrayList<AcceleratorSeq>();
     }
	
	
	/**
	 * Get the accelerator window managed by this document
	 * @return this document's accelerator window
	 */
	public AcceleratorWindow getAcceleratorWindow() {
		return (AcceleratorWindow)getMainWindow();
	}
	
	
	/** Generate and set the title for this document. */
	public void generateDocumentTitle() {
		final String filePath = getDisplayFilePath();
		final String title = selectedSequence != null ? " (" + selectedSequence.getId() + ") - " + filePath : filePath;
		setTitle( title );
	}
	
	
	/** 
	 * Get the prefix for a new file (precedes timestamp) defaulting to the selected sequence ID if any or the super class's default if no sequence is selected.
	 * @return prefix for a new file
	 */
	public String getNewFileNamePrefix() {
		return selectedSequence != null ? selectedSequence.getId().replace( ":", "-" ) : super.getNewFileNamePrefix();
	}
	
	
	/**
	 * Attempt to load the accelerator with the specified path and if none exists, then request a substitute accelerator from the user.
	 * @param filePath file path to the accelerator for which to first attempt to load
	 */
	public Accelerator applySelectedAcceleratorWithDefaultPath( final String filePath ) {
		if ( filePath == null || filePath.length() == 0 ) {
			return requestAndSetAccelerator( "No accelerator file has been specified. \n Please select an accelerator file." );
		}
		else if ( new File( filePath ).exists() ) {
			try {
				setAcceleratorWithPath( filePath );
				return getAccelerator();
			}
			catch( OpticsVersionException exception ) {
				return requestAndSetAccelerator( exception.getMessage() + " \nPlease select a substitute accelerator." );
			}
		}
		else {
			return requestAndSetAccelerator( "Cannot locate the accelerator file: \n" + filePath + " \nPlease select a substitute accelerator." );
		}
	}
	
	
	/** 
	 * Request and set an accelerator
	 * @param message the text to display in the dialog box
	 */
	private Accelerator requestAndSetAccelerator( final String message ) {
		final java.awt.Component targetWindow = getMainWindow();
		final String USE_DEFAULT_ACCELERATOR = "Use the Default Accelerator";
		final String SELECT_ACCELERATOR = "Select an Accelerator...";
		final String CANCEL_OPTION = "Cancel";
		final Object[] options = new Object[] { USE_DEFAULT_ACCELERATOR, SELECT_ACCELERATOR, CANCEL_OPTION };
		final int selectedOptionIndex = JOptionPane.showOptionDialog( targetWindow, message, "Select a Substitute Accelerator", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, USE_DEFAULT_ACCELERATOR );
		switch( selectedOptionIndex ) {
			case 0:
				loadDefaultAccelerator();
				return accelerator;
			case 1:
				return selectAccelerator();
            case 2:
                return null;
			case JOptionPane.CLOSED_OPTION:
				return null;
			default:
				return null;
		}
	}
	
	
	/** Displays a file selection dialog to allow the user to select an accelerator */
	private Accelerator selectAccelerator() {
		AcceleratorActionFactory.loadAcceleratorAction( this ).actionPerformed( null );
		return accelerator;
	}
    
    
    /**
     * Set the accelerator managed by the document.  Every document manages
     * one accelerator (or possibly null).
     * @param newAccelerator The accelerator managed by this document.
     * @param newPath The path to the accelerator managed by this document.
     */
    public void setAccelerator( final Accelerator newAccelerator, final String newPath ) {
        selectedSequence = null;
		selectedSequenceList = new ArrayList<AcceleratorSeq>();
        accelerator = newAccelerator;
        setAcceleratorFilePath( newPath );
        acceleratorChanged();       // hook for possible further processing
    }
	
	
	/** Set the accelerator given the file path */
	private void setAcceleratorWithPath( final String filePath ) {
		final Accelerator theAccelerator = XMLDataManager.acceleratorWithPath( filePath );
		setAccelerator( theAccelerator, filePath );
	}
    
    
    /**
     * Get the accelerator managed by this document.
     * @return The accelerator managed by this document.
     */
    public Accelerator getAccelerator() {
        return accelerator;
    }
    
    /**
     * Set the accelerator file path.  Every document manages
     * one accelerator (or possibly null).
     * @param newPath The path to the accelerator managed by this document.
     */
    public void setAcceleratorFilePath( final String newPath ) {
		acceleratorFilePath = newPath;
    }
    
    
    /**
     * Get the path to the xml file of the accelerator managed by this document.
     * @return path to the xml file of the accelerator managed by this document.
     */
    public String getAcceleratorFilePath() {
        return acceleratorFilePath;
    }
	
	
	/**
	 * Attempt to load the user's default accelerator.  If no default accelerator is specified,
	 * then prompt the user to specify the default optics path.  The document's accelerator
	 * file is set to the user's default accelerator.
	 * @return true if the default accelerator was successfully loaded
	 */
	protected boolean loadDefaultAccelerator() {
		Accelerator defaultAccelerator = null;
		
		try {
			defaultAccelerator = XMLDataManager.loadDefaultAccelerator();
		}
		catch( Exception exception ) {
            exception.printStackTrace();
			Application.displayError("Exception loading default accelerator", "Failed to load default accelerator", exception);
		}
		
		if ( defaultAccelerator != null ) {
			setAccelerator( defaultAccelerator, XMLDataManager.defaultPath() );
			return true;
		}
		else {
			final OpticsSwitcher switcher = OpticsSwitcher.getInstance();
            switcher.showDialogNearOwner( getAcceleratorWindow() );
            if ( switcher.isCanceled() ) {
                return false;
            }
			else if ( switcher.getDefaultOpticsPath() != null ) {
				return loadDefaultAccelerator();
			}
		}
		return false;
	}
    
    
    /**
     * Set the selected accelerator sequence managed by this document.
     * @param selection The accelerator sequence to be managed by this document.
     */
    public void setSelectedSequence( final AcceleratorSeq selection ) {
        selectedSequence = selection;
		final List<AcceleratorSeq> sequences = new ArrayList<AcceleratorSeq>();
		if ( selection != null ) {
			sequences.add( selection );
		}
		setSelectedSequenceList( sequences );
        selectedSequenceChanged();
		generateDocumentTitle();
    }
    
	
	/**
	 * Set a list of selected sequences
	 * @param seqList The list of selected sequences
	 */
    public void setSelectedSequenceList( final List<AcceleratorSeq> seqList ) {
        selectedSequenceList = seqList;
    }
    
    
    /**
     * Get the selected accelerator sequence managed by this document.
     * @return The sequence managed by this document.
     */
    public AcceleratorSeq getSelectedSequence() {
        return selectedSequence;
    }
    
	
	/**
	 * Get the selected sequence list
	 * @return the selected sequence list
	 */
    public List<AcceleratorSeq> getSelectedSequenceList() {
        return selectedSequenceList;
    }
    
    
    /**
     * Hook for handling the accelerator change event.  Subclasses should override
     * this method to provide custom handling.  The default handler does nothing.
     */
    public void acceleratorChanged() {
    }
    
    
    /**
     * Hook for handling the selected sequence change event.  Subclasses should override
     * this method to provide custom handling.  The default handler does nothing.
     */
    public void selectedSequenceChanged() {
    }


    /**
     * Hook for handling the accelerator file path change event.  Subclasses should override
     * this method to provide custom handling.  The default handler does nothing.
     */
    public void acceleratorFilePathChanged() {
    }
}
