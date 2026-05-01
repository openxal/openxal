/*
 * GroupListModel.java
 *
 * Created on Tue Nov 01 10:05:00 EST 2005
 *
 * Copyright (c) 2005 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.knobs;

import javax.swing.*;


/**
 * GroupListModel is the list model used to manage the JList which displays the groups in the main model.
 * @author  tap
 */
public class GroupListModel extends AbstractListModel<String> implements KnobsModelListener {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	/** The main model */
	protected KnobsModel _model;
	
	/** Index of the list cell upon which a drag event has occurred */
	protected int _dragIndex;
	
	
	/**
	 * Constructor
	 * @param model The knob model
	 */
	public GroupListModel( final KnobsModel model ) {
		_dragIndex = -1;
		_model = model;
		_model.addKnobsModelListener( this );
	}
	
	
	/**
	 * Get the number of groups to display
	 * @return the number of groups to display
	 */
	public int getSize() {
		return _model.getGroupCount();
	}
	
	
	/**
	 * Get the group at the specified index in the group list
	 * @param index The index in the group list
	 * @return The group corresponding to the specified index
	 */
	public String getElementAt( final int index ) {
		return getGroupLabel( index );
	}
	
	
	/**
	 * Get the label for the group with the specified index in the list of the 
	 * model's groups.  If the group is being dragged over, then display the drag label
	 * otherwise display the standard label.
	 * @param index The index of the group for which to get the label
	 * @see #standardLabel
	 * @see #dragLabel
	 */
	protected String getGroupLabel( final int index ) {
		final KnobGroup group = _model.getGroup( index );
		String label = group.getLabel();
		return (index == _dragIndex) ? dragLabel( label ) : standardLabel( label ); 
	}
	
	
	/**
	 * Get the standard text of the label.  We simply return the label itself unchanged.
	 * @param label The label for which to get the standard form.
	 * @return the unchanged plain text label.
	 */
	static protected String standardLabel( final String label ) {
		return label;
	}
	
	
	
	/**
	 * Get a version of the label to use for the list cell to indicate that the 
	 * group has an application being dragged above it.
	 * @param label The plain text label of the group
	 * @return HTML bordered table cell with the label as contents of the table cell
	 */
	static protected String dragLabel( final String label ) {
		return "<html><body><table border=1 cellspacing=0 cellpadding=0><tr><td>" + label + "</td></tr></table></body></html>";
	}
	
	
	/** Clear the drag index by setting it to -1 to indicate that there is no drag over any cells in the group list. */
	public void clearDragIndex() {
		setDragIndex( -1 );
	}
	
	
	/**
	 * Set the index of the drag cell indicating which group list cell has an application dragged over it.
	 * @param index The index of the group cell which has the application dragged over it.
	 */
	public void setDragIndex( final int index ) {
		_dragIndex = index;
		fireContentsChanged( this, 0, getSize() );
	}
	
	
	/**
	 * Handle the event indicating that the groups in the model have changed. This event causes the group list to refresh.
	 * @param model The model whose groups have changed.
	 */
	public void groupsChanged( final KnobsModel model ) {
		fireContentsChanged( this, 0, getSize() );
	}
	
	
	/**
	 * Handle the event indicating that the knobs model has been modified.  This implementation does nothing.
	 * @param model The model which has been modified.
	 */
	public void modified( final KnobsModel model ) {}
}

