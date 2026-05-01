//
//  Knob.java
//  xal
//
//  Created by Thomas Pelaia on 10/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.smf.Accelerator;
import xal.ca.Channel;
import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.StringJoiner;

import java.util.*;


/** Knob of PVs */
public class Knob implements Comparable<Knob>, KnobElementListener, DataListener {
	/** The DataAdaptor label */
	static public final String DATA_LABEL = "Knob";
	
	/** The message center for this knob's originated events */
	protected final MessageCenter MESSAGE_CENTER;
	
	/** The proxy for posting KnobListener events */
	protected final KnobListener EVENT_PROXY;
	
	/** Proxy for posting messages to the message board */
	protected final KnobsMessageListener BOARD_PROXY;
	
	/** the name for this knob */
	protected String _name;
	
	/** unique identifier of this knob */
	protected long _id;
	
	/** list of knob elements */
	protected final List<KnobElement> _elements;
	
	/** this knob's current setting */
	protected double _currentSetting;
	
	/** the minimum possible knob setting */
	protected double _lowerLimit;
	
	/** the maximum possible knob setting */
	protected double _upperLimit;
	
	/** dirty flag indicating whether limits need to be recalculated */
	protected volatile boolean _limitsNeedUpdate;
	
	/** lock to block setting values until any previous value was published successfully */
	protected final Object PUBLISH_LOCK;
	
	/** accelerator */
	protected Accelerator _accelerator;
	
	
	/** Primary Constructor */
	public Knob( final long ID, final String name ) {
		MESSAGE_CENTER = new MessageCenter( "Knob" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, KnobListener.class );
		BOARD_PROXY = MessageCenter.defaultCenter().registerSource( this, KnobsMessageListener.class );
		
		PUBLISH_LOCK = new Object();
		
		_limitsNeedUpdate = true;
		_currentSetting = 0.0;
		_elements = new ArrayList<KnobElement>();
		
		setID( ID );
		setName( name );
	}
	
	
	
	/**
	 * Get a knob built from a data adaptor
	 * @param accelerator the accelerator from which to get nodes and their channels
	 * @param adaptor The data adaptor for generating the instance
	 * @return a new instance of Knob built from the adaptor
	 */
	static public Knob getInstance( final Accelerator accelerator, final DataAdaptor adaptor ) {
		final Knob knob = new Knob( adaptor.longValue( "knobID" ), adaptor.stringValue( "name" ) );
		knob.setAccelerator( accelerator );
		knob.update( adaptor );
		return knob;
	}
	
	
    /** 
	* Provides the name used to identify the class in a persistent storage.
	* @return The tag for this data node.
	*/
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /**
	 * Update the data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) {
		setName( adaptor.stringValue( "name" ) );
		
		final List<DataAdaptor>elementAdaptors = adaptor.childAdaptors( KnobElement.DATA_LABEL );
		for ( DataAdaptor elementAdaptor : elementAdaptors ) {
			KnobElement element = KnobElement.getInstance( _accelerator, elementAdaptor );
			addElement( element );
		}
    }
    
    
    /**
	 * Write the data to the adaptor for persistent storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "name", _name );
		adaptor.setValue( "knobID", _id );
		adaptor.writeNodes( _elements );
    }
	
	
	/**
	 * Add a listener of Knob events from this instance
	 * @param listener The new listener to add
	 */
	public void addKnobListener( final KnobListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, KnobListener.class );
		listener.readyStateChanged( this, isReady() );
	}
	
	
	/**
	 * Remove a listener of Knob events from this instance
	 * @param listener The listener to remove
	 */
	public void removeKnobListener( final KnobListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, KnobListener.class );
	}
	
	
	/**
	 * Get this knob's unique ID.
	 * @return this knob's unique ID
	 */
	public long getID() {
		return _id;
	}
	
	
	/**
	 * Set the ID to the one specified.
	 * @param ID the new ID
	 */
	void setID( final long ID ) {
		_id = ID;
	}
	
	
	/**
	 * Get the name to display identifying this knob
	 * @return this knob's display name
	 */
	public String getName() {
		return _name;
	}
	
	
	/**
	 * Set the name of this knob
	 * @param name the name to set for this knob
	 */
	public void setName( final String name ) {
		_name = name;
		EVENT_PROXY.nameChanged( this, name );
	}
	
	
	/**
	 * Set the accelerator
	 * @param accelerator the accelerator to use
	 */
	public void setAccelerator( final Accelerator accelerator ) {
		_accelerator = accelerator;
		
		for ( KnobElement element : _elements ) {
			element.setAccelerator( accelerator );
		}
	}
	
	
	/**
	 * Get the accelerator.
	 * @return the accelerator
	 */
	public Accelerator getAccelerator() {
		return _accelerator;
	}
	
	
	/**
	 * Offset this knob's current value by the specified amount.
	 * @param offset the new offset
	 */
	public void addOffset( final double offset ) {
		synchronized( this ) {
			_currentSetting = _currentSetting + offset;
			_limitsNeedUpdate = true;
			EVENT_PROXY.currentSettingChanged( this, getCurrentSetting() );
		}
	}
	
	
	/**
	 * Determine if this knob is connected and ready to be used (each element must have a live value).
	 * @return true if all of this knob's elements are ready
	 */
	public boolean isReady() {
		synchronized( this ) {
			for ( KnobElement element : _elements ) {
				if ( !element.isReady() )  return false;
			}
			return _elements.size() > 0 ? true : false;
		}
	}
	
	
	/**
	 * Determine the reason for not being ready.
	 * @return the excuse for not being ready
	 */
	public String getInactiveExcuse() {
		synchronized ( this ) {
			if ( isReady() )  return null;
			
			final StringJoiner joiner = new StringJoiner( "<br>" );
			for ( KnobElement element : _elements ) {
				if ( !element.isReady() ) {
					final String excuse = element.getInactiveExcuse();
					if ( excuse != null ) {
						joiner.append( excuse );
					}
				}
			}
			
			return "<html><body> " + joiner.toString() + " </body></html>";
		}
	}
	
	
	/**
	 * Get the current value setting.
	 * @return the current value setting
	 */
	public double getCurrentSetting() {
		return _currentSetting;
	}
	
	
	/** Set the current setting to the specified value without changing any of the element values */
	public void setCurrentSetting( final double value ) {
		synchronized( this ) {
			_currentSetting = value;
			_limitsNeedUpdate = true;
			calculateLimits();
			EVENT_PROXY.currentSettingChanged( this, value );
		}		
	}
	
	
	/** Set the reading to zero */
	public void zero() {
		setCurrentSetting( 0.0 );
	}
	
	
	/** make the element coefficients equal to their values and set the knob value to 1.0 */
	public void makeProportionalCoefficients() {
		synchronized( this ) {
			for ( KnobElement element : _elements ) {
				final double value = element.getLatestValue();
				element.setCoefficient( value );
			}
			setCurrentSetting( 1.0 );
		}
	}
	
	
	/**
	 * Set this knob's current value to the specified value.
	 * @param value the target setting
	 */
	public void setValue( final double value ) {
		synchronized( this ) {
			if ( value > _lowerLimit && value < _upperLimit ) {
				if ( isSetOperationPending() ) {
					final long startTime = new Date().getTime();
					while ( isSetOperationPending() && ( new Date().getTime() - startTime < 1000 ) );	// wait at most one second
				}
				
				if ( isTracking() ) {
					final double delta = value - _currentSetting;
					for ( KnobElement element : _elements ) {
						element.changeValueAndScale( delta );
					}
					Channel.flushIO();
					_currentSetting = value;
				}
				else {
					resync();
				}
			}
		}
		
		EVENT_PROXY.currentSettingChanged( this, _currentSetting );
	}
	
	
	/**
	 * Determine if this knob's elements are tracking.
	 * @return true if they are all tracking and false if any one of them is not tracking.
	 */
	public boolean isTracking() {
		synchronized( this ) {
			for ( KnobElement element : _elements ) {
				if ( !element.isTracking() )  return false;
			}
			return true;
		}
	}
	
	
	/**
	 * Determine if the current set operation is pending.
	 * @return true if the current set operation is pending and false if not
	 */
	public boolean isSetOperationPending()  {
		synchronized( this ) {
			for ( KnobElement element : _elements ) {
				if ( !element.isPutPending() )  return false;
			}
			return true;
		}
	}

	
	/** Synchronize each element */
	public void resync() {
		synchronized( this ) {
			for ( KnobElement element : _elements ) {
				element.resync();
			}
			Channel.flushIO();
			_limitsNeedUpdate = true;
		}
	}
	
	
	/**
	 * Get the lower limit
	 * @return the lower limit
	 */
	public double getLowerLimit() {
		calculateLimitsIfNeeded();
		return _lowerLimit;
	}
	
	
	/**
	 * Get the upper limit
	 * @return the upper limit
	 */
	public double getUpperLimit() {
		calculateLimitsIfNeeded();
		return _upperLimit;
	}
	
	
	/** Determine if limits need updating */
	public boolean limitsNeedUpdate() {
		return _limitsNeedUpdate;
	}
	
	
	/** Indicate that the limits need to be updated */
	public void setLimitsNeedUpdating() {
		_limitsNeedUpdate = true;
	}
	
	
	/** Calculate limits if they need updating */
	public void calculateLimitsIfNeeded() {
		if ( _limitsNeedUpdate ) {
			calculateLimits();
		}
	}
	
	
	/** Calculate the knob value limits */
	protected void calculateLimits() {
		synchronized( this ) {
			final double currentSetting = getCurrentSetting();
			final double oldLowerLimit = _lowerLimit;
			final double oldUpperLimit = _upperLimit;
			double proposedLowerLimit = currentSetting;
			double proposedUpperLimit = currentSetting;
			if ( isReady() ) {
				double minDelta = Double.NEGATIVE_INFINITY;
				double maxDelta = Double.POSITIVE_INFINITY;
				for ( KnobElement element : _elements ) {
					final double coefficient = element.getCoefficient();
					if ( coefficient != 0.0 ) {
						final double latestValue = element.getSettingValue();
						final double limitOne = ( element.getEffectiveLowerLimit() - latestValue ) / coefficient;
						final double limitTwo = ( element.getEffectiveUpperLimit() - latestValue ) / coefficient;
						final double minElementDelta = Math.min( limitOne, limitTwo );
						final double maxElementDelta = Math.max( limitOne, limitTwo );
						maxDelta = Math.min( maxDelta, maxElementDelta );
						minDelta = Math.max( minDelta, minElementDelta );
					}
				}
				proposedLowerLimit += minDelta;
				proposedUpperLimit += maxDelta;
			}
			
			if ( Double.isInfinite( proposedLowerLimit ) ) {
				proposedLowerLimit = currentSetting - 1000 * Math.abs( currentSetting );
			}
			
			if ( Double.isInfinite( proposedUpperLimit ) ) {
				proposedUpperLimit = currentSetting + 1000 * Math.abs( currentSetting );
			}
			
			final double scale = Math.abs( oldUpperLimit - oldLowerLimit ) + Math.abs( proposedUpperLimit - proposedLowerLimit );
			final double change = Math.abs( oldUpperLimit - proposedUpperLimit ) + Math.abs( oldLowerLimit - proposedLowerLimit );
			_limitsNeedUpdate = false;
			
			if ( change > 1.0e-6 * scale ) {
				_lowerLimit = proposedLowerLimit;
				_upperLimit = proposedUpperLimit;
				EVENT_PROXY.limitsChanged( this, _lowerLimit, _upperLimit );
			}
		}
	}
	
	
	/**
	 * Get the list of knob elements.
	 * @return this knob's list of elements
	 */
	public List<KnobElement> getElements() {
		synchronized( this ) {
			return _elements;
		}
	}
	
	
	/**
	 * Get the number of elements.
	 * @return the number of elements
	 */
	public int getElementCount() {
		synchronized( this ) {
			return _elements.size();
		}
	}
	
	
	/**
	 * Determine if this knob has any elements.
	 * @return true if this knob has elements and false if not
	 */
	public boolean hasElements() {
		return getElementCount() > 0;
	}
	
	
	/**
	 * Add the specified element to this knob.
	 * @param element the element to add
	 */
	public void addElement( final KnobElement element ) {
		synchronized( this ) {
			_elements.add( element );
			element.addKnobElementListener( this );
		}
		EVENT_PROXY.elementAdded( this, element );
	}
	
	
	/** Instantiate an empty element and add it to this knob */
	public void addElement() {
		addElement( new KnobElement() );
	}
	
	
	/**
	 * Remove the specified element from this knob.
	 * @param element the element to remove
	 */
	public void removeElement( final KnobElement element ) {
		synchronized( this ) {
			_elements.remove( element );
			element.removeKnobElementListener( this );
		}
			
		EVENT_PROXY.elementRemoved( this, element );
		EVENT_PROXY.readyStateChanged( this, isReady() );
	}
	
	
	/**
	 * Get the string representation of this instance
	 * @return this knob's name
	 */
	public String toString() {
		return getName();
	}
	
	
	/**
	 * Compare two instances.  The comparison is based on alphebetical sorting of the label.
	 * @param knob the other knob against which to compare
	 * @return a positive number if this comes after the argument, negative if this comes before and 0 if they are equal
	 */
	public int compareTo( final Knob knob ) {
		return getName().compareToIgnoreCase( knob.getName() );
	}
	
	
	/** event indicating that the knob element's channel has changed */
	public void channelChanged( final KnobElement element, final Channel channel ) {
		_limitsNeedUpdate = true;
		EVENT_PROXY.elementModified( this, element );
	}
	
	
	/** event indicating that the element's coefficient has changed */
	public void coefficientChanged( final KnobElement element, final double coefficient ) {
		_limitsNeedUpdate = true;
		EVENT_PROXY.elementModified( this, element );
	}
	
	
	/** connection changed event */
	public void connectionChanged( final KnobElement element, final boolean isConnected ) {
		_limitsNeedUpdate = true;
	}
	
	
	/** ready state changed */
	public void readyStateChanged( final KnobElement element, final boolean isReady ) {
		_limitsNeedUpdate = true;
		EVENT_PROXY.readyStateChanged( this, isReady() );
	}
	
	
	/** value changed event */
	public void valueChanged( final KnobElement element, final double value ) {
		if ( !element.isTracking() ) {
			_limitsNeedUpdate = true;
		}
	}
	
	
	/** value setting published */
	public void valueSettingPublished( final KnobElement element ) {
		EVENT_PROXY.valueSettingPublished( this );
	}
}
