//
// ScreenCaptureController.java
// xal
//
// Created by Pelaia II, Tom on 8/21/12
// Copyright 2012 ORNL. All rights reserved.
//

package xal.app.labbook;

import xal.extension.application.*;
import xal.extension.bricks.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


/** ScreenCaptureController */
public class ScreenCaptureController {
	/** Dialog for selecting the capture region */
	final private JDialog CAPTURE_DIALOG;

	/** indicates whether the user has indicated the image should be captured */
	private boolean _shouldCaptureImage;

	/** handler of mouse motion events */
	private MouseMotionHandler _mouseMotionHandler;



	/** Constructor */
    protected ScreenCaptureController( final JFrame owner ) {
		_mouseMotionHandler = null;
		_shouldCaptureImage = false;

		final WindowReference screenCaptureDialogRef = Application.getAdaptor().getDefaultWindowReference( "ScreenCaptureDialog", owner );
		CAPTURE_DIALOG = (JDialog)screenCaptureDialogRef.getWindow();
		CAPTURE_DIALOG.setModal( true );
		CAPTURE_DIALOG.setLocationRelativeTo( owner );
		CAPTURE_DIALOG.setUndecorated( true );
		CAPTURE_DIALOG.setBackground( new Color( 0.5f, 0.5f, 0.5f, 0.5f ) );	// set the background to translucent gray

		final JButton screenCaptureCancelButton = (JButton)screenCaptureDialogRef.getView( "ScreenCaptureCancelButton" );
		screenCaptureCancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_shouldCaptureImage = false;
				CAPTURE_DIALOG.setVisible( false );
			}
		});

		final JButton screenCaptureRegionButton = (JButton)screenCaptureDialogRef.getView( "ScreenCaptureRegionButton" );
		screenCaptureRegionButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_shouldCaptureImage = true;
				CAPTURE_DIALOG.setVisible( false );
			}
		});


		CAPTURE_DIALOG.addMouseListener( new MouseAdapter() {
			public void mousePressed( final MouseEvent event ) {
				_mouseMotionHandler = MouseMotionHandler.getInstance( CAPTURE_DIALOG, event.getPoint() );
			}
		});

		CAPTURE_DIALOG.addMouseMotionListener( new MouseMotionAdapter() {
			public void mouseMoved( final MouseEvent event ) {
				MouseMotionHandler.updateCursor( CAPTURE_DIALOG, event.getPoint() );
			}
			
			public void mouseDragged( final MouseEvent event ) {
				if ( _mouseMotionHandler != null ) {
					_mouseMotionHandler.mouseDragged( event );
				}
			}
		});
    }


	/** show the dialog for selecting the region to capture */
	private void showCaptureRegionSelector() {
		CAPTURE_DIALOG.setVisible( true );
	}


	/** capture the image if the user requested to do so */
	private BufferedImage captureImageIfRequested() {
		if ( _shouldCaptureImage ) {
			final Rectangle dialogBounds = CAPTURE_DIALOG.getBounds();
			//System.out.println( "Capturing image at: " + dialogBounds );
			try {
				return new Robot().createScreenCapture( dialogBounds );
			}
			catch ( Exception exception ) {
				throw new RuntimeException( exception );
			}
		}
		else {
			return null;
		}
	}


	/** Display a dialog to select the region for which to capture the image */
	static public BufferedImage captureSelectedRegion( final JFrame owner ) {
		final ScreenCaptureController controller = new ScreenCaptureController( owner );
		controller.showCaptureRegionSelector();
		return controller.captureImageIfRequested();
	}
}



/** base class for mouse motion handlers */
abstract class MouseMotionHandler extends MouseMotionAdapter {
	/** width of the window edge which is used for resizing */
	final static private int RESIZE_EDGE_WIDTH = 10;

	/** Dialog to track */
	final protected Window WINDOW;

	/** offset of the mouse relative to the window */
	final protected Point INITIAL_MOUSE_OFFSET;


	/** Construction */
	protected MouseMotionHandler( final Window window, final Point initialMouseOffset ) {
		WINDOW = window;
		INITIAL_MOUSE_OFFSET = initialMouseOffset;
	}


	/** Get the appropriate handler for the current mouse offset within the window */
	static public MouseMotionHandler getInstance( final Window window, final Point initialMouseOffset ) {
		final Dimension windowSize = window.getSize();

		if ( isOnLeftEdge( windowSize, initialMouseOffset ) && isOnTopEdge( windowSize, initialMouseOffset ) ) {
			return new MouseWindowResizer( window, initialMouseOffset, 1, 1, -1, -1 );
		}
		else if ( isOnLeftEdge( windowSize, initialMouseOffset ) && isOnBottomEdge( windowSize, initialMouseOffset ) ) {
			return new MouseWindowResizer( window, initialMouseOffset, 1, 0, -1, 1 );
		}
		else if ( isOnRightEdge( windowSize, initialMouseOffset ) && isOnTopEdge( windowSize, initialMouseOffset ) ) {
			return new MouseWindowResizer( window, initialMouseOffset, 0, 1, 1, -1 );
		}
		else if ( isOnRightEdge( windowSize, initialMouseOffset ) && isOnBottomEdge( windowSize, initialMouseOffset ) ) {
			return new MouseWindowResizer( window, initialMouseOffset, 0, 0, 1, 1 );
		}
		else if ( isOnLeftEdge( windowSize, initialMouseOffset ) ) {
			return new MouseWindowResizer( window, initialMouseOffset, 1, 0, -1, 0 );
		}
		else if ( isOnRightEdge( windowSize, initialMouseOffset ) ) {
			return new MouseWindowResizer( window, initialMouseOffset, 0, 0, 1, 0 );
		}
		else if ( isOnTopEdge( windowSize, initialMouseOffset ) ) {
			return new MouseWindowResizer( window, initialMouseOffset, 0, 1, 0, -1 );
		}
		else if ( isOnBottomEdge( windowSize, initialMouseOffset ) ) {
			return new MouseWindowResizer( window, initialMouseOffset, 0, 0, 0, 1 );
		}
		else {
			return new MouseDialogLocationTracker( window, initialMouseOffset );
		}
	}


	/** determine whether the mouse is on the left edge of the window */
	static public boolean isOnLeftEdge( final Dimension windowSize, final Point mouseOffset ) {
		return mouseOffset.x < RESIZE_EDGE_WIDTH;
	}


	/** determine whether the mouse is on the right edge of the window */
	static public boolean isOnRightEdge( final Dimension windowSize, final Point mouseOffset ) {
		return mouseOffset.x > windowSize.width - RESIZE_EDGE_WIDTH;
	}


	/** determine whether the mouse is on the top edge of the window */
	static public boolean isOnTopEdge( final Dimension windowSize, final Point mouseOffset ) {
		return mouseOffset.y < RESIZE_EDGE_WIDTH;
	}


	/** determine whether the mouse is on the bottom edge of the window */
	static public boolean isOnBottomEdge( final Dimension windowSize, final Point mouseOffset ) {
		return mouseOffset.y > windowSize.height - RESIZE_EDGE_WIDTH;
	}


	/** update the cursor based on the mouse position within the window */
	static public void updateCursor( final Window window, final Point mouseOffset ) {
		final Dimension windowSize = window.getSize();

		if ( isOnLeftEdge( windowSize, mouseOffset ) && isOnTopEdge( windowSize, mouseOffset ) ) {
			window.setCursor( new Cursor( Cursor.NW_RESIZE_CURSOR ) );
		}
		else if ( isOnLeftEdge( windowSize, mouseOffset ) && isOnBottomEdge( windowSize, mouseOffset ) ) {
			window.setCursor( new Cursor( Cursor.SW_RESIZE_CURSOR ) );
		}
		else if ( isOnRightEdge( windowSize, mouseOffset ) && isOnTopEdge( windowSize, mouseOffset ) ) {
			window.setCursor( new Cursor( Cursor.NE_RESIZE_CURSOR ) );
		}
		else if ( isOnRightEdge( windowSize, mouseOffset ) && isOnBottomEdge( windowSize, mouseOffset ) ) {
			window.setCursor( new Cursor( Cursor.SE_RESIZE_CURSOR ) );
		}
		else if ( isOnLeftEdge( windowSize, mouseOffset ) ) {
			window.setCursor( new Cursor( Cursor.W_RESIZE_CURSOR ) );
		}
		else if ( isOnRightEdge( windowSize, mouseOffset ) ) {
			window.setCursor( new Cursor( Cursor.E_RESIZE_CURSOR ) );
		}
		else if ( isOnTopEdge( windowSize, mouseOffset ) ) {
			window.setCursor( new Cursor( Cursor.N_RESIZE_CURSOR ) );
		}
		else if ( isOnBottomEdge( windowSize, mouseOffset ) ) {
			window.setCursor( new Cursor( Cursor.S_RESIZE_CURSOR ) );
		}
		else {
			window.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
		}
	}
}



/** Resize the window based on the mouse motion */
class MouseWindowResizer extends MouseMotionHandler {
	/** initial size of the window */
	final private Dimension INITIAL_WINDOW_SIZE;

	/** initial location of the window */
	final private Point INITIAL_WINDOW_POSITION;

	/** factor to apply to x location changes */
	final private int X_LOCATION_FACTOR;
	
	/** factor to apply to y location changes */
	final private int Y_LOCATION_FACTOR;

	/** factor to apply to x size changes */
	final private int X_SIZE_FACTOR;
	
	/** factor to apply to y size changes */
	final private int Y_SIZE_FACTOR;


	/** Construction */
	public MouseWindowResizer( final Window window, final Point initialMouseOffset, final int xLocationFactor, final int yLocationFactor, final int xSizeFactor, final int ySizeFactor ) {
		super( window, initialMouseOffset );

		INITIAL_WINDOW_POSITION = window.getLocation();
		INITIAL_WINDOW_SIZE = window.getSize();

		X_LOCATION_FACTOR = xLocationFactor;
		Y_LOCATION_FACTOR = yLocationFactor;
		X_SIZE_FACTOR = xSizeFactor;
		Y_SIZE_FACTOR = ySizeFactor;
	}


	/** process the mouse drag event */
	public void mouseDragged( final MouseEvent event ) {
		final Point currentMouseOffset = event.getPoint();
		final int xMove = currentMouseOffset.x - INITIAL_MOUSE_OFFSET.x;
		final int yMove = currentMouseOffset.y - INITIAL_MOUSE_OFFSET.y;

		final Point nextDialogPosition = new Point( INITIAL_WINDOW_POSITION.x + X_LOCATION_FACTOR * xMove, INITIAL_WINDOW_POSITION.y + Y_LOCATION_FACTOR * yMove );
		final Dimension nextDialogSize = new Dimension( INITIAL_WINDOW_SIZE.width + X_SIZE_FACTOR * xMove, INITIAL_WINDOW_SIZE.height + Y_SIZE_FACTOR * yMove );
		final Rectangle bounds = new Rectangle( nextDialogPosition, nextDialogSize );
		WINDOW.setBounds( bounds );
	}
}



/** Moves the window to track the mouse */
class MouseDialogLocationTracker extends MouseMotionHandler {
	/** Construction */
	public MouseDialogLocationTracker( final Window window, final Point initialMouseOffset ) {
		super( window, initialMouseOffset );

		window.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
	}


	/** process the mouse drag event */
	public void mouseDragged( final MouseEvent event ) {
		final Point currentMouseOffset = event.getPoint();
		final int xMove = currentMouseOffset.x - INITIAL_MOUSE_OFFSET.x;
		final int yMove = currentMouseOffset.y - INITIAL_MOUSE_OFFSET.y;

		final Point currentDialogPosition = WINDOW.getLocation();
		final Point nextDialogPosition = new Point( currentDialogPosition.x + xMove, currentDialogPosition.y + yMove );
		WINDOW.setLocation( nextDialogPosition );
	}
}