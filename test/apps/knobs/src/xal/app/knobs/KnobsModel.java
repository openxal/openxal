//
//  KnobsModel.java
//  xal
//
//  Created by Thomas Pelaia on 10/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.tools.messaging.MessageCenter;
import xal.tools.data.*;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;

import java.util.*;
import java.io.File;


/** Model for the knobs document */
public class KnobsModel implements DataListener {
	/** DataAdaptor label used in reading and writing */
	static public final String DATA_LABEL = "KnobsModel";
	
	/** message center used to post events from this instance */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy for posting events */
	final protected KnobsModelListener EVENT_PROXY;
	
	/** The main group which contains a list of all loaded applications */
	final protected MainKnobGroup _mainGroup;
	
	/** List of knob groups */
	final protected List<KnobGroup> _groups;
	
	/** handler of group events */
	final protected GroupHandler _groupHandler;
	
	/** comparator for sorting groups */
	final protected Comparator<KnobGroup> _groupComparator;
	
	/** accelerator for node channel references */
	protected Accelerator _accelerator;
	
	/** accelerator sequence for knob generation */
	protected AcceleratorSeq _sequence;
	
	
	/** KnobsModel */
	public KnobsModel( final Accelerator accelerator, final AcceleratorSeq sequence ) {
		MESSAGE_CENTER = new MessageCenter( DATA_LABEL );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, KnobsModelListener.class );
		
		_groupHandler = new GroupHandler();
		_groupComparator = new GroupComparator();
		
		_mainGroup = new MainKnobGroup();
		_groups = new ArrayList<KnobGroup>();
		addGroup( _mainGroup );
		
		setAccelerator( accelerator );
		setSequence( sequence );
	}
    
    
    /** 
	 * dataLabel() provides the name used to identify the class in an external data source.
	 * @return The tag for this data node.
	 */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /**
	 * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) {
		final DataAdaptor mainAdaptor = adaptor.childAdaptor( MainKnobGroup.DATA_LABEL );
		final List<DataAdaptor> knobAdaptors = mainAdaptor.childAdaptors( Knob.DATA_LABEL );
		for ( DataAdaptor knobAdaptor : knobAdaptors ) {
			addKnob( Knob.getInstance( _accelerator, knobAdaptor ) );
		}		
		_mainGroup.update( mainAdaptor );
		
		final List<DataAdaptor> groupAdaptors = adaptor.childAdaptors( KnobGroup.DATA_LABEL );
		for ( DataAdaptor groupAdaptor : groupAdaptors ) {
			final KnobGroup group = new KnobGroup( groupAdaptor.stringValue( "label" ) );
			addGroup( group );
			
			for (final DataAdaptor knobRefAdaptorIter : mainAdaptor.childAdaptors()) {
				final long knobID = knobRefAdaptorIter.longValue( "knobID" );
				final Knob knob = _mainGroup.getKnobWithID( knobID );
				group.addKnob( knob );
			}
			group.update( groupAdaptor );
		}
    }
    
    
    /**
	 * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.writeNodes( _groups );
    }
	
	
	/**
	 * Add a listener of KnobsModel events from this instance
	 * @param listener The listener to add
	 */
	public void addKnobsModelListener( final KnobsModelListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, KnobsModelListener.class );
	}
	
	
	/**
	 * Remove a listener of KnobsModel events from this instance
	 * @param listener The listener to remove
	 */
	public void removeKnobsModelListener( final KnobsModelListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, KnobsModelListener.class );
	}
	
	
	/**
	 * Set the accelerator.
	 * @param accelerator the new accelerator
	 */
	public void setAccelerator( final Accelerator accelerator ) {
		_accelerator = accelerator;
		
		final List<Knob> knobs = _mainGroup.getKnobs();
		for ( Knob knob : knobs ) {
			knob.setAccelerator( accelerator );
		}
	}
	
	
	/**
	 * Get the accelerator sequence.
	 * @return the accelerator sequence
	 */
	public AcceleratorSeq getSequence() {
		return _sequence;
	}
	
	
	/**
	 * Set the sequence.
	 * @param sequence the new accelerator sequence
	 */
	public void setSequence( final AcceleratorSeq sequence ) {
		_sequence = sequence;
	}
	
	
	/**
	 * Get the main knob group.
	 * @return the main knob group
	 */
	public MainKnobGroup getMainKnobGroup() {
		return _mainGroup;
	}
	
	
	/**
	 * Get the group at the index from the list of groups.  The main group is always at the 0 index.  
	 * The other groups follow sequentially after that.
	 * @param index The index of the group to get
	 * @return the group at the specified index
	 */
	synchronized public KnobGroup getGroup( final int index ) {
		return _groups.get( index );
	}
	
	
	/**
	 * Get the list of groups.  The main group is always the first group in the list.
	 * The groups are sorted according to their comparable method, except for the main group.
	 * @return the list of KnobGroup groups
	 */
	synchronized public List<KnobGroup> getGroups() {
		return _groups;
	}
	
	
	/**
	 * Get the number of groups including the main group.
	 * @return the number of groups.
	 */
	synchronized public int getGroupCount() {
		return _groups.size();
	}
	
	
	/**
	 * Add a collection of groups.
	 * @param groups The collection of KnobGroup groups to add.
	 */
	synchronized public void addGroups( final Collection<KnobGroup> groups ) {
		for ( KnobGroup group : groups ) {
			_groups.add( group );
			group.addKnobGroupListener( _groupHandler );
		}
		sortGroups();
		EVENT_PROXY.groupsChanged( this );
	}
	
	
	/**
	 * Add a group.
	 * @param group The group to add.
	 */
	synchronized public void addGroup( final KnobGroup group ) {
		_groups.add( group );
		group.addKnobGroupListener( _groupHandler );
		sortGroups();
		EVENT_PROXY.groupsChanged( this );
	}
	
	
	/** 
	 * Get the next knob ID incrementing the last ID by 1
	 * @return the next knob ID
	 */
	synchronized private long nextKnobID() {
		return _mainGroup.getKnobCount();
	}
	
	
	/**
	 * Create a new knob in the specified group
	 * @param group The group to which to add the new knob
	 * @param knobName the name for the knob
	 */
	synchronized public Knob createKnobInGroup( final KnobGroup group, final String knobName ) {
		final Knob knob = new Knob( nextKnobID(), knobName );
		_mainGroup.addKnob( knob );
		group.addKnob( knob );
		knob.setAccelerator( _accelerator );
		return knob;
	}
	
	
	/**
	 * Add knobs to a specific group.
	 * @param knobs the knobs to add to the group
	 * @param group The group to which to add the knobs
	 */
	synchronized public void addKnobsToGroup( final List<Knob> knobs, final KnobGroup group ) {
		_mainGroup.addKnobs( knobs );
		group.addKnobs( knobs );
	}
	
	
	/**
	 * Add an knoblication to the table of knobs.
	 * @param knob The application ot add.
	 */
	synchronized public void addKnob( final Knob knob ) {
		_mainGroup.addKnob( knob );
	}
	
	
	/**
	 * Remove the specified group.  However this method will not allow one to remove the main group.
	 * @param group The group to remove
	 */
	synchronized public void removeGroup( final KnobGroup group ) {
		if ( _groups.contains( group ) && ( group != _mainGroup ) ) {
			group.removeKnobGroupListener( _groupHandler );
			_groups.remove( group );
			_groupHandler.knobsRemoved( group, group.getKnobs() );		
			EVENT_PROXY.groupsChanged( this );
		}
	}
	
	
	/**
	 * Sort the groups using the GroupComparator to assure that the MainKnobGroup instance appears ahead
	 * of all other groups and groups are otherwise sorted alphabetically.
	 */
	protected void sortGroups() {
		Collections.sort( _groups, _groupComparator );
	}
	
	
	
	/** Handle KnobGroupListener events from the groups in the model */
	protected class GroupHandler implements KnobGroupListener {
		/**
         * Notice that knobs have been added to the specified group
		 * @param group The group which has new knobs
         * @param addedKnobs The knobs which have been added to the group
		 */
		public void knobsAdded( final KnobGroup group, final Collection<Knob> addedKnobs ) {
			EVENT_PROXY.modified( KnobsModel.this );
		}
		
		
		/**
		 * Notice that applications have been removed from the specified group
		 * @param source The group from which applications were removed
		 * @param removedKnobs The Knob instances which have been removed from the group
		 */
		public void knobsRemoved( final KnobGroup source, final Collection<Knob> removedKnobs ) {
			if ( source == _mainGroup ) {
				for ( KnobGroup group : _groups ) {
					for ( Iterator<Knob> knobIter = removedKnobs.iterator() ; knobIter.hasNext() ; ) {
						Knob knob = knobIter.next();
						group.removeKnob(knob);
					}					
				}
			}
			EVENT_PROXY.modified( KnobsModel.this );
		}
		
		
		/**
		 * The group's label has been changed
		 * @param group the group whose label has been changed
		 */
		public void labelChanged( final KnobGroup group ) {
			sortGroups();
			EVENT_PROXY.groupsChanged( KnobsModel.this );
			EVENT_PROXY.modified( KnobsModel.this );
		}
		
		
		/**
		 * Indicates that a knob has been modified.
		 * @param group the group whose knob has been modified
		 * @param knob the knob which has been modified
		 */
		public void knobModified( KnobGroup group, Knob knob ) {
			EVENT_PROXY.modified( KnobsModel.this );
		}
	}
}



/**
 * GroupComparator assures that the MainKnobGroup instances knobear ahead of all other 
 * groups and groups are otherwise sorted alphabetically.
 */
class GroupComparator implements Comparator<KnobGroup> {
	/**
	 * Compare two groups.  Comparison is made based on alphabetical ordering.  However, if
	 * a MainKnobGroup instance is being compared, it must appear ahead of any other group.
	 * @param group1 The first group in the comparison
	 * @param group2 The second group in the comparison
	 * @return 0 if the two are equal, negative if group1 appears ahead of group2 and positive otherwise
	 */
	public int compare( final KnobGroup group1, final KnobGroup group2 ) {
		return (group2 instanceof MainKnobGroup) ? -group2.compareTo( group1 ) : group1.compareTo( group2 );
	}
}
