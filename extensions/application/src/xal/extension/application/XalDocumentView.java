//
//  XalWindowInterface.java
//  xal
//
//  Created by Thomas Pelaia on 3/28/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.application;

import javax.swing.*;
import java.awt.Point;
import java.util.*;


/** Interface of XAL windows. */
public interface XalDocumentView extends RootPaneContainer {	
    /** Show this window.  Make it visible (de-iconify if necessary) and bring it to the front. */
    public void showWindow();
    
    
    /** Iconify this window. */
    public void hideWindow();
	
	
	/** get the view's location */
	public Point getLocation();
	
	
	/** get the view's location relative to the screen */
	public Point getLocationOnScreen();
	
	
	/** set the view's location */
	public void setLocation( final Point location );
	
	
	/** set the view's visibility */
	public void setVisible( final boolean state );
	
	
	/** determine the view's visibility */
	public boolean isVisible();
	
	
	/** get the view's menu bar */
	public JMenuBar getJMenuBar();
	
	
	/** */
	public void setJMenuBar( final JMenuBar menuBar );
    
	
	/**
	 * Get the toolbar associated with this window.
	 * @return This window's toolbar or null if none was added.
	 */
	public JToolBar getToolBar();
	
	
	/** Capture the window as an image. */
    public void captureAsImage();
	
	
	/**
	 * Display a confirmation dialog with a title and message
	 * @param title The title of the dialog
	 * @param message The message to display
	 * @return YES_OPTION or NO_OPTION 
	 */
	public int displayConfirmDialog(String title, String message);
	
    
    /**
	 * Display a warning dialog box and provide an audible alert.
     * @param aTitle Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    public void displayWarning(String aTitle, String message);
	
    
    /**
	 * Display a warning dialog box showing information about an exception that 
     * has been thrown and provide an audible alert.
     * @param exception The exception whose description is being displayed.
     */
    public void displayWarning(Exception exception);    
    
    
    /**
	 * Display a warning dialog box with information about the exception and provide
     * an audible alert.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param aTitle Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    public void displayWarning(String aTitle, String prefix, Exception exception);	
	
    
    /**
	 * Display an error dialog box and provide an audible alert.
     * @param aTitle Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    public void displayError(String aTitle, String message);
    
    
    /**
	 * Display an error dialog box with information about the exception and 
     * provide an audible alert.
     * @param exception The exception about which the warning dialog is displayed.
     */
    public void displayError(Exception exception);
    
    
    /**
	 * Display an error dialog box with information about the exception and 
     * provide an audible alert.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param aTitle Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    public void displayError(String aTitle, String prefix, Exception exception);	
}
