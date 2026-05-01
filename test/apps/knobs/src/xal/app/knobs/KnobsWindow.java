//
//  KnobsWindow.java
//  xal
//
//  Created by Thomas Pelaia on 9/13/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.IconLib;
import xal.tools.IconLib.IconGroup;

import static javax.swing.SwingConstants.*;
import static javax.swing.ScrollPaneConstants.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.*;
import javax.swing.event.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** Knobs main window */
public class KnobsWindow extends AcceleratorWindow {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	/** Message board timestamp format */
	final DateFormat MESSAGE_TIMESTAMP_FORMAT = new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" );
	
	/** Message label for displaying the messages */
	final JLabel MESSAGE_LABEL = new JLabel( "Message Board" );
	
	/** The main model for the document */
	final protected KnobsModel _model;
	
	/** The controller holds the selection state of groups and applications */
	final protected KnobsController _controller;
	
	/** The knobs workspace which displays selected knobs */
	protected Container _workspace;
	
	/** split pane which contains the workspace */
	protected JSplitPane _workPane;
	
	/** displays and hides the knob editor */
	protected KnobEditDisplayer _knobEditDisplayer;
	
	/** The main pane */
	protected JSplitPane _mainPane;
	
	/** The selector pane */
	protected JSplitPane _selectorPane;
	
	/** table of cached knob views keyed by knob */
	protected Map<Knob,KnobView> _knobViews;
	
	
    /** Constructor */
    public KnobsWindow( final XalDocument aDocument ) {
        super( aDocument );
		
		_model = ((KnobsDocument)aDocument).getModel();
		_controller = new KnobsController();
		_knobViews = new HashMap<Knob,KnobView>();
		
        setSize( 1100, 700 );
		makeContents();
		
		handleWindowEvents();
    }
    
    
    /**
	 * Register actions specific to this document instance.  
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands( final Commander commander ) {
        final Action PROPORTIONAL_COEFFICIENTS_ACTION = new AbstractAction( "make-proportional-coefficents" ) {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
				final List<Knob> selectedKnobs = _controller.getSelectedKnobs();
				for ( final Knob knob : selectedKnobs ) {
					knob.makeProportionalCoefficients();
				}
            }
        };
        commander.registerAction( PROPORTIONAL_COEFFICIENTS_ACTION );
	}
	
	
	/** Handle window events */
	private void handleWindowEvents() {
		addWindowListener( new WindowAdapter() {
			public void windowOpened( final WindowEvent event ) {
				_mainPane.setDividerLocation( 0.3 );
				_selectorPane.setDividerLocation( 0.4 );
				_workPane.setDividerLocation( 1.0 );
			}
		});
	}
	
	
	/** Get a knob view from the cache for the specified knob creating one if necessary */
	protected KnobView getKnobView( final Knob knob ) {
		final KnobView view = _knobViews.get( knob );
		if ( view != null ) {
			return view;
		}
		else {
			_knobViews.put( knob, new KnobView( knob, _knobEditDisplayer ) );
			return getKnobView( knob );
		}
	}
	
	
	/** Get the knobs controller */
	public KnobsController getController() {
		return _controller;
	}
    
    
    /**
	 * Do not show the toolbar.
	 * @return false
     */
    public boolean usesToolbar() {
        return true;
    }
	
	
	/**
	 * Construct the contents of this window
	 */
	protected void makeContents() {
		final Component groupView = makeGroupView();
		final Component knobsView = makeKnobsView();
		_workspace = makeWorkspace();
		
		_workPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, new JScrollPane( _workspace ), null );
		_workPane.setResizeWeight( 0.4 );
		_workPane.setContinuousLayout( true );
		
		_knobEditDisplayer = new KnobEditDisplayer( _workPane );
		
		_selectorPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, groupView, knobsView );
		_selectorPane.setResizeWeight( 0.5 );
		_selectorPane.setContinuousLayout( true );
		
		_mainPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, _selectorPane, _workPane );
		_mainPane.setResizeWeight( 0.0 );
		_mainPane.setContinuousLayout( true );
		_mainPane.setOneTouchExpandable( true );
		
		Box windowView = new Box( BoxLayout.Y_AXIS );
		windowView.add( _mainPane );
		windowView.add( MESSAGE_LABEL );
		
		getContentPane().add( windowView );
	}
	
	
	/** Show this window */
	public void showWindow() {
		super.showWindow();
		_controller.setSelectedGroup( _model.getGroup(0) );
	}
	
	
	/**
	 * Convenience method for getting the index of the list cell enclosing the specified point.
	 * Unlike locationToIndex(), this method tests that the location is actually in the bounds
	 * of the cell.  This is important to avoid associating an event that occurs at the bottom of 
	 * the list but outside of any cell.
	 * @param list The JList to test
	 * @param location The location of the event as a point
	 * @return the cell in the list corresponding to the location or -1 if no cell is associated
	 */
	static protected int getIndexAtEvent( final JList<String> list, final Point location ) {
		final int index = list.locationToIndex( location );
		final Rectangle cellBounds = list.getCellBounds( index, index );
		return cellBounds.contains( location ) ? index : -1; 
	}
	
	
	/**
	 * Post an application wide message from the source
	 * @param source The source of the message
	 * @param message The message posted
	 */
	public void postMessage( final Object source, final String message ) {
		displayMessage( message );
	}
	
	
	/**
	 * Post an application wide error message from the source
	 * @param source The source of the message
	 * @param message The message posted
	 */
	public void postErrorMessage( final Object source, final String message ) {
		displayErrorMessage( message );
	}
	
	
	/**
	 * Display a message in the message board with the specified font color
	 * @param message The message to display
	 * @param fontColor The font color used to display the message
	 */
	protected void displayMessage( final String message, final String fontColor ) {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "<html><body>" );
		buffer.append( MESSAGE_TIMESTAMP_FORMAT.format( new Date() ) );
		buffer.append( " - " );
		buffer.append( "<font COLOR=" + fontColor + ">" );
		buffer.append( message );
		buffer.append( "</font></body></html>" );
		MESSAGE_LABEL.setText( buffer.toString() );
	}
	
	
	/**
	 * Display a message in the message board with a font color of blue.
	 * @param message The message to display
	 */
	protected void displayMessage( final String message ) {
		displayMessage( message, "#000088" );
	}
	
	
	/**
	 * Display an error message in the message board with a font color of red.
	 * @param message The message to display
	 */
	protected void displayErrorMessage( final String message ) {
		displayMessage( message, "#ff0000" );
	}
	
	
	/**
	 * Build the view for displaying and managing the groups.  This includes a label, a
	 * scrollable JList of the groups and a button for adding a new group.
	 * @return The view for displaying and managing the groups
	 */
	protected Component makeGroupView() {
		final Box groupBox = new Box( BoxLayout.Y_AXIS );
		
		final Box groupLabelBox = new Box( BoxLayout.X_AXIS );
		groupLabelBox.add( new JLabel( "Groups:" ) );
		groupLabelBox.add( Box.createHorizontalGlue() );
		groupBox.add( groupLabelBox );
		final JList<String> groupList = new JList<String>();
		groupList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		groupBox.add( new JScrollPane( groupList ) );
		
		groupList.addKeyListener( new KeyAdapter() {
			public void keyPressed( final KeyEvent event ) {
				if ( event.getKeyCode() == KeyEvent.VK_BACK_SPACE ) {
					final KnobGroup group = _controller.getSelectedGroup();
					if ( group != null ) {
						_model.removeGroup( group );
						groupList.clearSelection();
						_controller.setSelectedGroup( null );
					}
				}
			}
		});
		
		groupList.addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				if ( event.getValueIsAdjusting() )  return;
				int selectedIndex = groupList.getSelectedIndex();
				if ( selectedIndex < 0 ) {
					_controller.setSelectedGroup( null );
				}
				else {
					final KnobGroup group = _model.getGroup( selectedIndex );
					_controller.setSelectedGroup( group );
				}
			}
		});
		
		groupList.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				if ( event.getClickCount() == 2 ) {
					int index = getIndexAtEvent( groupList, event.getPoint() );
					if ( index < 0 )  return;
					final KnobGroup group = _model.getGroup( index );
					if ( group.allowsLabelEdit() ) {
						final String message = "New Group Label for \"" + group.getLabel() + "\":";
						String name = JOptionPane.showInputDialog( KnobsWindow.this, message, "Rename the group ", JOptionPane.QUESTION_MESSAGE );
						if ( name != null ) {
							name = name.trim();
							if ( name != null && name.length() > 0)  group.setLabel( name );
						}
					}
					else {
						java.awt.Toolkit.getDefaultToolkit().beep();
					}
				}
			}
		});
		final GroupListModel listModel = new GroupListModel( _model );
		groupList.setModel( listModel );
		groupList.setSelectedIndex( 0 );
		groupList.setDragEnabled( true );
		groupList.setTransferHandler( new GroupListTransferHandler() );
		
		JButton groupAddButton = new JButton( IconLib.getIcon( IconGroup.GENERAL, "Add24.gif" ) );
		groupAddButton.setToolTipText( "Add a new button group." );
		groupAddButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final String name = JOptionPane.showInputDialog( KnobsWindow.this, "Group Label: ", "Label the group", JOptionPane.QUESTION_MESSAGE );
				if ( name != null ) {
					_model.addGroup( new KnobGroup( name ) );
				}
			}
		});
		Box groupButtonRow = new Box( BoxLayout.X_AXIS );
		groupButtonRow.add( groupAddButton );
		groupButtonRow.add(Box.createHorizontalGlue());
		groupBox.add( groupButtonRow );
		
		return groupBox;
	}
	
	
	/**
	 * Build the view for listing the knobs.  
	 * @return The view for displaying and managing the applications
	 */
	protected Component makeKnobsView() {
		Box knobsBox = new Box( BoxLayout.Y_AXIS );
		
		Box knobLabelBox = new Box( BoxLayout.X_AXIS );
		knobLabelBox.add( new JLabel( "Knobs:" ) );
		knobLabelBox.add( Box.createHorizontalGlue() );
		knobsBox.add( knobLabelBox );
		final JList<String> knobList = new JList<String>();
		knobList.setDragEnabled( true );
		knobList.setTransferHandler( new KnobListTransferHandler() );
		knobList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		
		knobsBox.add( new JScrollPane( knobList ) );
		knobList.addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				KnobGroup group = _controller.getSelectedGroup();
				int[] indices = knobList.getSelectedIndices();
				if ( indices.length == 0 ) {
					_controller.setSelectedKnobs( null );
					_workspace.removeAll();
				}
				else {
					_workspace.removeAll();
					final List<Knob> selectedKnobs = new ArrayList<Knob>();
					for ( int index : indices ) {
						final Knob knob = group.getKnob( index );
						selectedKnobs.add( knob );
						_workspace.add( getKnobView( knob ) );
					}
					
					// check to see that the edit knob (if any) is still in the selected list and hide it if not to prevent confusion
					final Knob editKnob = _knobEditDisplayer.getKnob();
					if ( editKnob == null || !selectedKnobs.contains( editKnob ) ) {
						_knobEditDisplayer.close();
					}
					
					_controller.setSelectedKnobs( selectedKnobs );
				}
				_workspace.getParent().repaint();
				_workspace.getParent().validate();
			}
		});
		
		knobList.addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if ( event.getKeyCode() == KeyEvent.VK_BACK_SPACE ) {
					final List<Knob> knobs = _controller.getSelectedKnobs();
					if ( !knobs.isEmpty() ) {
						final KnobGroup group = _controller.getSelectedGroup();
						group.removeKnobs( knobs );
						knobList.clearSelection();
						_controller.setSelectedKnob( null );
					}
				}
			}
		});
		
		_controller.addKnobsControllerListener( new KnobsControllerListener() {
			public void selectedGroupChanged( final KnobsController source, final KnobGroup newSelectedGroup ) {
				knobList.clearSelection();
			}
			public void selectedKnobsChanged( final KnobsController source, final List<Knob> newSelectedKnobs ) {}
		});
		
		final KnobListModel knobListModel = new KnobListModel();
		_controller.addKnobsControllerListener( knobListModel );
		knobList.setModel( knobListModel );
		knobList.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				if ( event.getClickCount() == 2 ) {
					int index = getIndexAtEvent( knobList, event.getPoint() );
					if ( index < 0 )  return;
					KnobGroup group = knobListModel.getGroup();
					Knob knob = group.getKnob( index );
					final String message = "New Knob name for \"" + knob.getName() + "\":";
					String name = JOptionPane.showInputDialog( KnobsWindow.this, message, "Rename the knob", JOptionPane.QUESTION_MESSAGE );
					if ( name != null ) {
						name = name.trim();
						if ( name != null && name.length() > 0)  knob.setName( name );
					}
				}
			}
		});
		
		final JButton knobAddButton = new JButton( IconLib.getIcon( IconGroup.GENERAL, "Add24.gif" ) );
		knobAddButton.setToolTipText( "Add a new knob." );
		knobAddButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final String name = JOptionPane.showInputDialog( KnobsWindow.this, "Knob Name: ", "Name the Knob", JOptionPane.QUESTION_MESSAGE );
				if ( name != null ) {
					final KnobGroup group = _controller.getSelectedGroup();
					_model.createKnobInGroup( group, name );
				}
			}
		});
		knobAddButton.setEnabled(_controller.getSelectedGroup() != null);
		
		_controller.addKnobsControllerListener( new KnobsControllerListener() {
			/**
			 * Handle the event indicating that the knobs controller has a new selected group.
			 * Enable the button that allows adding applictions if a group has been selected, else disable the button.
			 * @param source The knobs controller
			 * @param newSelectedGroup The new selected group
			 */
			public void selectedGroupChanged( final KnobsController source, final KnobGroup group ) {
				knobAddButton.setEnabled( group != null );
			}
			
			/**
			 * Handle the event indicating that the knobs controller has a new selected application.  This implementation does nothing.
			 * @param source The knobs controller
			 * @param knobs the new selected knobs
			 */
			public void selectedKnobsChanged( final KnobsController source, final List<Knob> knobs ) {}
		});
		
		Box knobButtonRow = new Box( BoxLayout.X_AXIS );
		knobButtonRow.add( knobAddButton );
		knobButtonRow.add( Box.createHorizontalGlue() );
		knobsBox.add( knobButtonRow );
		
		return knobsBox;
	}
	
	
	/**
	 * Construct the view for displaying the selected knobs details.
	 * @return a view for displaying selected knobs details
	 */
	protected Container makeWorkspace() {				
		Container workspace = new JPanel();
		java.awt.GridLayout layout = new java.awt.GridLayout( 0, 3 );
		workspace.setLayout( layout );
		return workspace;
	}
	
	
	
	/** Knob list transfer handler */
	class KnobListTransferHandler extends TransferHandler {
        /** serialization identifier */
        private static final long serialVersionUID = 1L;
		/** group that is the source of the knobs being copied or moved */
		protected KnobGroup _sourceGroup;
		
		
		/** transfer knobs from the knobs list */
		protected Transferable createTransferable( final JComponent component ) {
			final List<Knob> knobs = _controller.getSelectedKnobs();
			return new KnobTransferable( knobs );
		}
		
		
		/** provides copy or move operation */
		public int getSourceActions( final JComponent component ) {
			_sourceGroup = _controller.getSelectedGroup();
			return _sourceGroup == _model.getMainKnobGroup() ? COPY : COPY_OR_MOVE;
		}
		
		
		/** perform cleanup operations */
        @SuppressWarnings( "unchecked" )    // cast from transferable to List<Knob>
		protected void exportDone( final JComponent component, Transferable transferable, int action ) {
			switch( action ) {
				case TransferHandler.MOVE:
					if ( _sourceGroup != null && _sourceGroup != _model.getMainKnobGroup() && transferable != null ) {
						try {
							_sourceGroup.removeKnobs ((List<Knob>)transferable.getTransferData(KnobTransferable.KNOB_FLAVOR ) );
						}
						catch ( java.awt.datatransfer.UnsupportedFlavorException exception ) {
							exception.printStackTrace();
						}
						catch ( java.io.IOException exception ) {
							exception.printStackTrace();
						}
					}
					break;
				default:
					break;
			}
		}
	}
	
	
	/** Group list tranfer handler */
	class GroupListTransferHandler extends TransferHandler {
        /** serialization identifier */
        private static final long serialVersionUID = 1L;
		/** determine if the group list can import at least one of the tranferable flavors */
		public boolean canImport( final JComponent component, final DataFlavor[] flavors ) {
			for ( DataFlavor flavor : flavors ) {
				if ( flavor == KnobTransferable.KNOB_FLAVOR )  return true;
			}
			return false;
		}
		
		
		/** import the transferable */
        @SuppressWarnings( "unchecked" )    // cast from JComponent to JList and cast from transferable to List<Knob>
		public boolean importData( final JComponent component, final Transferable transferable ) {
			final JList<String> groupList = (JList<String>)component;
			final int groupIndex = groupList.getSelectedIndex();
			final KnobGroup group = _model.getGroup( groupIndex );
			try {
				final List<Knob> knobs = (List<Knob>)transferable.getTransferData( KnobTransferable.KNOB_FLAVOR );
				group.addKnobs( knobs );
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
	}
}



/** Implement a Transferable for the knobs being dragged */
class KnobTransferable implements Transferable {
	/** define the knob flavor */
	static public final DataFlavor KNOB_FLAVOR;
	
	/** the list of flavors associated with application transfer */
	static public final DataFlavor[] FLAVORS;
	
	/** The knobs being transferred */
	protected final List<Knob> _knobs;
	
	
	// static initializer
	static {
		KNOB_FLAVOR = new DataFlavor( Knob.class, "Knob" );
		FLAVORS = new DataFlavor[] { KNOB_FLAVOR };
	}
	
	
	/**
	 * Constructor
	 * @param knobs The knobs being transferred
	 */
	public KnobTransferable( final List<Knob> knobs ) {
		_knobs = new ArrayList<Knob>( knobs );
	}
	
	
	/**
	 * Get the data being transfered which in this case is simply the knob
	 * @param flavor The flavor of the transfer
	 * @return The knobs being transfered
	 */
	public Object getTransferData( final DataFlavor flavor ) {
		return _knobs;
	}
	
	
	/**
	 * The flavors handled by this transferable which is presently just KNOB_FLAVOR
	 * @return the array of flavors handled
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return FLAVORS;
	}
	
	
	/**
	 * Test if the specified flavor is supported by this instance.  Only KNOB_FLAVOR is currently supported.
	 * @param flavor The flavor to test.
	 * @return true if the flavor is among the supported flavors and false otherwise.
	 */
	public boolean isDataFlavorSupported( final DataFlavor flavor ) {
		for ( int index = 0 ; index < FLAVORS.length ; index++ ) {
			if ( FLAVORS[index].equals( flavor ) )  return true;
		}
		return false;
	}
}

