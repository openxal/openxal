/*
 * ImageCaptureManager.java
 *
 * Created on May 23, 2003, 2:41 PM
 */

package xal.tools.apputils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.*;
import javax.imageio.ImageIO;

import xal.tools.apputils.files.*;


/**
 * ImageCaptureManager manages the file chooser used to save image captures of 
 * the main window.  All applications share the same default folder where the 
 * images are saved.  This default folder is the last folder where a user saved an
 * image.
 * @author  tap
 */
public class ImageCaptureManager {
	// static constants for confirmation dialogs
	final static protected int YES_OPTION = JOptionPane.YES_OPTION;
	final static protected int NO_OPTION = JOptionPane.NO_OPTION;
	
	/** default image capture manager to be shared throughout the application */
    final static private ImageCaptureManager _defaultManager;
    
    /** chooser for saving images */
    final protected JFileChooser _fileChooser;
	
	/** accessory for managing the default snapshot folder */
	final protected DefaultFolderAccessory _defaultFolderAccessory;
    
    
	/**
	 * static initializer
	 */
    static {
        _defaultManager = new ImageCaptureManager();
    }
    
    
    /** Creates a new instance of ImageCaptureManager */
    protected ImageCaptureManager() {
        _fileChooser = new JFileChooser();		
        _fileChooser.setMultiSelectionEnabled( false );
		
		_defaultFolderAccessory = new DefaultFolderAccessory( this.getClass(), null, "snapshots" );
		_defaultFolderAccessory.applyTo( _fileChooser );		
		
        _fileChooser.addChoosableFileFilter( new FileFilter() {
            public boolean accept( final File file ) {
                String name = file.getName().toLowerCase();
                if ( file.isDirectory() )  return true;
                if ( name.endsWith( "." + "png" ) )  return true;
                return false;
            }
            
            
            /**
             * Description of the file filter which is simply "Supported Files".
             * @return Description of the file filter.
             */
            public String getDescription() {
                return "PNG Files";
            }
        });
    }
    
    
    /**
     * Get the default ImageCaptureManager instance.
     * @return The default ImageCaptureManager instance.
     */
    static public ImageCaptureManager defaultManager() {
        return _defaultManager;
    }
    
    
    /**
     * Get the file chooser which determines where snapshots are saved.
     * @return the image capture file chooser
     */
    public JFileChooser getFileChooser() {
        return _fileChooser;
    }
    
    
    /**
     * Save a snapshot of the component as a PNG image.
     * @param component The component to capture.
     * @throws java.awt.AWTException If there is an AWT exception.
     * @throws java.io.IOException If there is a file exception.
     */
    public void saveSnapshot( final Component component ) throws java.awt.AWTException, IOException {
		saveSnapshot( component, "Untitled" );
    }
    
    
    /**
     * Save a snapshot of the component as a PNG image.
     * @param component The component to capture.
	 * @param name The snapshot name
     * @throws java.awt.AWTException If there is an AWT exception.
     * @throws java.io.IOException If there is a file exception.
     */
    public void saveSnapshot( final Component component, final String name ) throws java.awt.AWTException, IOException {
        BufferedImage image = new BufferedImage( component.getWidth(), component.getHeight(), BufferedImage.TYPE_3BYTE_BGR );
        component.paintAll( image.createGraphics() );
		
		File defaultFolder = _fileChooser.getCurrentDirectory();
		File defaultFile = new File( defaultFolder, name + ".png" );
		_fileChooser.setSelectedFile( defaultFile );
		
        int status = _fileChooser.showSaveDialog( component );
        switch(status) {
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.APPROVE_OPTION:
                File fileSelection = _fileChooser.getSelectedFile();
				if ( fileSelection.exists() ) {
					int confirm = displayConfirmDialog(component, "Overwrite Confirmation", "The selected file:  " + fileSelection + " already exists! \n Overwrite selection?");
					if ( confirm == NO_OPTION ) {
						saveSnapshot( component );	// offer a new selection
						return;
					}
				}
				ImageIO.write( image, "png", fileSelection );
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }
    }
	
	
	/**
	 * Display a confirmation dialog with a title and message
	 * @param owner The component in front of which to show the dialog
	 * @param title The title of the dialog
	 * @param message The message to display
	 * @return YES_OPTION or NO_OPTION 
	 */
	static public int displayConfirmDialog( final Component owner, final String title, final String message ) {
        java.awt.Toolkit.getDefaultToolkit().beep();
        return JOptionPane.showConfirmDialog( owner, message, title, JOptionPane.YES_NO_OPTION );
	}
}
