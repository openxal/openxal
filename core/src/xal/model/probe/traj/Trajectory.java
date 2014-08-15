package xal.model.probe.traj;

import xal.tools.RealNumericIndexer;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;
import xal.model.probe.Probe;
import xal.model.xml.ParsingException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Manages the history for a probe.  Saves <code>ProbeState</code> objects,
 * each of which reflects the state of the <code>Probe</code> at a particular
 * point in time.
 * 
 * @author Craig McChesney
 * @author Christopher K. Allen
 * (cosmetic enhancements)
 * @version $id:
 * 
 */
public class Trajectory<S extends ProbeState<S>> implements IArchive, Iterable<S> {
	
    /*
     * Global Constants
     */

    // *********** I/O Support

    /** XML element tag for trajectory */
    public final static String TRAJ_LABEL = "trajectory";
    
    /** XML element tag for trajectory concrete type */
    private final static String TYPE_LABEL = "type";
    
    /** XML element tag for time stamp data */
    private static final String TIMESTAMP_LABEL = "timestamp";
    
    /** XML element tag for user comment data */
    private static final String DESCRIPTION_LABEL = "description";


    /*
     * Local Types
     */
    
    /**
     * <p>
     * Maintains a list of probe states for every each element class.  Each element
     * class is refined as the collection is built.  States are placed into the map
     * using the modeling element ID as the key.  If it is found that that element ID
     * is similar to an existing element ID, than the state is entered into the element
     * ID class.  By similar, we mean that one ID string can be contained within another
     * ID (or is equal to).
     * </p>
     * <p>
     * The idea is that each element ID class is then representative of probe states associated
     * with a single hardware node.  See {@link IdentifierEquivClass} for another explanation.  
     * </p>
     * <p>
     * <h4>NOTES:</h4>
     * &middot; I am not sure if we have to explicitly consider the empty identifier string.
     * This empty ID is a substring of all IDs.  
     * <br/>
     * &middot; But we use the 
     * <code>{@link TreeMap#ceilingEntry(Object)}</code> for access so that may
     * circumvent things.
     * <br/>
     * &middot; The easy thing to do might be just to reject empty strings in 
     * <code>{@link #putState(String, ProbeState)}</code>
     * </p>
     *
     * @author Christopher K. Allen
     * @since  Aug 14, 2014
     */
    private class NodeIdToElemStates {

        /** Map of SMF node identifiers to probe states within all modeling elements */
        private final TreeMap<String, RealNumericIndexer<S>>    mapNodeToStates;
        
        /** The last map entry to be accessed when adding a new probe state */
        private Map.Entry<String, RealNumericIndexer<S>>        entryLast;
        
        /*
         * Initialization
         */
        
        /**
         * Creates a new uninitialized instance of <code>ElementStateMap</code>.
         *
         * @author Christopher K. Allen
         * @since  Jun 5, 2013
         */
        public NodeIdToElemStates() {

            // Create the comparator for ordering the tree map nodes according to node IDs
            //  Then create the map itself
            Comparator<String>  cmpIdOrder = new Comparator<String>() {

                @Override
                public int compare(String strId1, String strId2) {
                    
                    if (strId2.startsWith(strId1))
                        return -1;
                    
                    else if (strId1.startsWith(strId2))
                        return +1;
                    
                    else
                        return strId1.compareTo(strId2);
                }
                
                
            };
            
            this.mapNodeToStates = new TreeMap<String, RealNumericIndexer<S>>(cmpIdOrder);
            
            // Create a blank last map entry
            String                  ideEmpty   = new String("ZzZzXxXxXxXxXx999888777666555444333222111WwWwWwWwYYyy");
            RealNumericIndexer<S>   setIdEmpty = new RealNumericIndexer<S>();
            
            this.mapNodeToStates.put(ideEmpty, setIdEmpty);
            this.entryLast = new AbstractMap.SimpleEntry<String, RealNumericIndexer<S>>(ideEmpty, setIdEmpty);
        }

        /*
         * Operations
         */
        
        /**
         * <p>
         * Enters the given probe state into the map using the element ID as the 
         * initial key.  Note that this key can be changed internally if the
         * element ID belongs to an element ID class already identified.
         * </p>
         * <p>
         * To improve efficiency the method checks if the given element ID is the same
         * (same equivalence class, that is) as the last one provided in the previous call
         * to this method.  A reference to the list of elements for that ID is kept
         * on hand so a full map search is not used.
         * </p>
         * 
         * @param strNodeId     string identifier of the modeling element associated with the probe state
         * @param state         probe state to be entered into the map
         *
         * @author Christopher K. Allen
         * @since  Aug 14, 2014
         */
        public void putState(String strNodeId, S state) {

            // We're going to reject the empty ID
            if (strNodeId.equals(""))
                return;

            // Get the Node ID class for last node accessed (needed for mapping elements)
            //      and get the position of the state within the sequence 
            //      (needed for indexing states within elements)
            double                    dblPos = state.getPosition();
            String                    strIdLast = this.entryLast.getKey();
            
            // The state is a member of the last equivalence class accessed
//            if ( this.entryLast.getKey().equals(strNodeId) ) {
            if ( strNodeId.startsWith(strIdLast) ) {
                RealNumericIndexer<S> setStates = this.entryLast.getValue();
                
                setStates.add(dblPos, state);
                return;
            }
            
            // This is a new equivalence class - that is, different then the last accessed
            //      Get the list of states corresponding to the ID class
            Map.Entry<String, RealNumericIndexer<S>> entNode = this.mapNodeToStates.floorEntry(strNodeId);
            
            // If there is no element list for this ID class, create one and add it to the map
            if (entNode == null) {
                this.createNewEquivClass(strNodeId, state);
                
                return;
            }
            
            // Retrieve the entry key and value (state set) then process the given state
            String                  strMapKey = entNode.getKey();
            RealNumericIndexer<S>   setStates = entNode.getValue();
           
            // Map key is a substring of the argument Node ID
            if ( strNodeId.startsWith(strMapKey) ) {
                setStates.add(dblPos, state);

                // This will have the same equiv. class ID
                this.updateLastEntry(strMapKey, setStates);
                
                return;
            }
                
            // Argument ID is a substring of the map key
            if ( strMapKey.startsWith(strNodeId) ) {
                this.mapNodeToStates.remove(strMapKey);
                this.mapNodeToStates.put(strNodeId, setStates);
                setStates.add(dblPos, state);

                // Save the last list to be accessed
                this.updateLastEntry(strNodeId, setStates);
            }
                
            // Neither ID is a substring of the other
            //      We are definitely looking at a different hardware node.
            //      Create a new equivalence class
            this.createNewEquivClass(strNodeId, state);
        }
        
        /**
         * Returns a set of probe states corresponding to the identifier
         * class containing the given modeling element ID.
         * 
         * @param strElemId     model element identifier
         * 
         * @return              all the probe states which are associated with the given element ID and its class,
         *                      or <code>null</code> if there are none
         * @param strNodeId
         * @return
         *
         * @author Christopher K. Allen
         * @since  Aug 14, 2014
         */
        public RealNumericIndexer<S>    getStates(String strNodeId) {
            
            // Specific case; We get lucky, the given node ID is in the last equivalence class accessed
            String                    strIdLast = this.entryLast.getKey();
            
            if ( strNodeId.startsWith(strIdLast) ) {
                RealNumericIndexer<S> setStates = this.entryLast.getValue();
                
                return setStates;
            }
            
            // The general case: We must get the list of states corresponding to the ID class
            Map.Entry<String, RealNumericIndexer<S>> entNode   = this.mapNodeToStates.floorEntry(strNodeId);
            String                                   strClsId  = entNode.getKey();
            RealNumericIndexer<S>                    setStates = entNode.getValue();
            
            if ( strNodeId.startsWith(strClsId) )
                return setStates;
            
            else
                return null;
        }
        
        /*
         * Support Methods
         */
        
        /**
         * Creates a new set of probe states to go into the 
         * (NodeID,{probe states}) mapping.  The given probe state is
         * used as the first element in the new set and the given
         * equivalence class identifier is used to key the map.  The set
         * is created and put into the map.
         * 
         * @param strClsId      ID of the equivalence class of probe states 
         *                      (usually a hardware node prefix)
         * @param stateFirst    The first (and only) state in the new state set 
         *
         * @author Christopher K. Allen
         * @since  Aug 14, 2014
         */
        private void createNewEquivClass(String strClsId, S stateFirst) {
            
            // Create the set of probe states (indexed by position)
            //  and add the given state to this new set
            RealNumericIndexer<S>   setStates = new RealNumericIndexer<S>();

            double  dblPos = stateFirst.getPosition();
            setStates.add(dblPos, stateFirst);
            
            // Now put the equivalence class ID - probe state pair into the map 
            this.mapNodeToStates.put(strClsId, setStates);

            // Save the last list to be accessed
            this.updateLastEntry(strClsId, setStates);
//            this.entryLast = new AbstractMap.SimpleEntry<String, RealNumericIndexer<S>>(strClsId, setStates);
        }
        
        /**
         * Convenience method for reseting the last entry maintained by this class.
         * Saves the space of having to write out all the nested types and
         * generic information.
         *
         * @param strClsId      ID of the equivalence class of probe states 
         *                      (usually a hardware node prefix)
         * @param setStates     The set of probe states belonging to the given class ID
         *  
         * @author Christopher K. Allen
         * @since  Aug 14, 2014
         */
        private void updateLastEntry(String strClsId, RealNumericIndexer<S> setStates) {
            this.entryLast = new AbstractMap.SimpleEntry<String, RealNumericIndexer<S>>(strClsId, setStates);
        }
    }
    
    /**
     * <p>
     * Maintains a list of probe states for every each element class.  Each element
     * class is refined as the collection is built.  States are placed into the map
     * using the modeling element ID as the key.  If it is found that that element ID
     * is similar to an existing element ID, than the state is entered into the element
     * ID class.  By similar, we mean that one ID string can be contained within another
     * ID (or is equal to).
     * </p>
     * <p>
     * The idea is that each element ID class is then representative of probe states associated
     * with a single hardware node.  See {@link IdentifierEquivClass} for another explanation.  
     * </p>
     * 
     * @param <S>   probe state type for particular probe trajectory
     * 
     * @author Christopher K. Allen
     * @since  Jun 5, 2013
     */
    private static class ElementStateMap<S extends ProbeState<S>>  {
        
        /*
         * Local Attributes
         */
        
        /** map from element prefix to element states */
        private final Map<String, RealNumericIndexer<S>>    mapStateList;
        
        /** the last map entry to be accessed (added to) */
        private Map.Entry<String, RealNumericIndexer<S>>    entryLast;
        
        
        /*
         * Initialization
         */
        
        /**
         * Creates a new uninitialized instance of <code>ElementStateMap</code>.
         *
         * @author Christopher K. Allen
         * @since  Jun 5, 2013
         */
        public ElementStateMap() {

            // Create the comparator for ordering the tree map nodes
            //  Then create the map itself
            Comparator<String>  cmpIdClsOrder = new Comparator<String>() {

                @Override
                public int compare(String id1, String id2) {
                    return id1.compareTo(id2);
                }
            };
            
            this.mapStateList = new TreeMap<String, RealNumericIndexer<S>>(cmpIdClsOrder);
            
            // Create a blank last map entry
            String                  ideEmpty   = new String("");
            RealNumericIndexer<S>   setIdEmpty = new RealNumericIndexer<S>();
            
            this.mapStateList.put(ideEmpty, setIdEmpty);
            this.entryLast = new AbstractMap.SimpleEntry<String, RealNumericIndexer<S>>(ideEmpty, setIdEmpty);
        }

        
        /*
         * Operations
         */
        
        /**
         * <p>
         * Enters the given probe state into the map using the element ID as the 
         * initial key.  Note that this key can be changed internally if the
         * element ID belongs to an element ID class already identified.
         * </p>
         * <p>
         * To improve efficiency the method checks if the given element ID is the same
         * (same equivalence class, that is) as the last one provided in the previous call
         * to this method.  A reference to the list of elements for that ID is kept
         * on hand so a full map search is not used.
         * </p>
         * 
         * @param strElemId     string identifier of the modeling element associated with the probe state
         * @param state         probe state to be entered into the map
         *
         * @author Christopher K. Allen
         * @since  Jun 5, 2013
         */
        public void putState(String strElemId, S state) {
            
            // Create the ID class for the element ID (needed for indexing elements)
            //  and get the position of the state within the sequence (needed for indexing states within elements)
            String                    idElem = strElemId;
            double                    dblPos = state.getPosition();
            
            // The state is a member of the last equivalence class accessed
            if ( this.entryLast.getKey().equals(idElem) ) {
                RealNumericIndexer<S> setStates = this.entryLast.getValue();
                
                setStates.add(dblPos, state);
                return;
            }
            
            // This is a new equivalence class - that is, different then the last accessed
            //  Get the list of states corresponding to the ID class
            RealNumericIndexer<S>    setStates = this.mapStateList.get(idElem);
            
            // If there is no list for this ID class, create one and add it to the map
            if (setStates == null) {
                setStates = new RealNumericIndexer<S>();
                
                this.mapStateList.put(idElem, setStates);
            }
            
            // Add the given state to the list
            setStates.add(dblPos, state);
            
            // Save the last list to be accessed
            this.entryLast = new AbstractMap.SimpleEntry<String, RealNumericIndexer<S>>(idElem, setStates);
        }
        
        /**
         * Returns a list of probe states corresponding to the identifier
         * class containing the given modeling element ID.
         * 
         * @param strElemId     model element identifier
         * 
         * @return              all the probe states which are associated with the given element ID and its class,
         *                      or <code>null</code> if there are none
         *
         * @author Christopher K. Allen
         * @since  Jun 5, 2013
         */
        public RealNumericIndexer<S>  getStates(String strElemId) {
            RealNumericIndexer<S> lstStates = this.mapStateList.get(strElemId);
            
            return lstStates;
        }
    }


    /*
     *  Local Attributes
     */
     
    /** any user comments regard the trajectory */
    private String description = "";
    
    /** the history of probe states along the trajectory */
    private RealNumericIndexer<S>      _history;
    
    /** Probe states by element name */
//    private final ElementStateMap<S>   mapStates;
    private final NodeIdToElemStates     mapStates;
    
    /** Type class of the underlying state objects */
    private final Class<S>            clsStates;
    
    /** time stamp of trajectory */
    private Date                      timestamp = new Date();





    // Factory Methods ========================================================


    
    // I think this will have to be rewritten once the other trajectory classes
    //   are gone since there will be no way to instantiate [newInstance()]
    /**
     * Read the contents of the supplied <code>DataAdaptor</code> and return
     * an instance of the appropriate Trajectory species.
     * 
     * @param container <code>DataAdaptor</code> to read a Trajectory from
     * @return a Trajectory for the contents of the DataAdaptor
     * @throws ParsingException error encountered reading the DataAdaptor
     */
    @SuppressWarnings("unchecked")
	public static Trajectory<? extends ProbeState<?>> readFrom(DataAdaptor container)
        throws ParsingException {
        DataAdaptor daptTraj = container.childAdaptor(Trajectory.TRAJ_LABEL);
        if (daptTraj == null)
            throw new ParsingException("Trajectory#readFrom() - DataAdaptor contains no trajectory node");
        
        String type = container.stringValue(Trajectory.TYPE_LABEL);
        Trajectory<? extends ProbeState<?>> trajectory;
        try {
            Class<?> trajectoryClass = Class.forName(type);
            trajectory = (Trajectory<? extends ProbeState<?>>) trajectoryClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParsingException(e.getMessage());
        }
        trajectory.load(daptTraj);
        return trajectory;
    }





    // ************* abstract protocol specification


    /**
     * Override this method in subclasses to add subclass-specific properties to
     * the output.  Subclass implementations should call super.addPropertiesTo
     * so that superclass implementations are executed.
     * 
     * @param container the <code>DataAdaptor</code> to add properties to
     */
    protected void addPropertiesTo(DataAdaptor container) {}

    /**
     * Allow subclasses to read subclass-specific properties from the <code>
     * DataAdaptor</code>.  Implementations should call super.readPropertiesFrom
     * to ensure that superclass implementations are executed.
     * 
     * @param container <code>DataAdaptor</code> to read properties from
     */
    protected void readPropertiesFrom(DataAdaptor container) throws ParsingException {}







    // ************ initialization
    
//    /**
//     * Create a new, empty <code>Trajectory</code> object.
//     */
//    public Trajectory() {
//        this._history  = new RealNumericIndexer<S>();
//        this.mapStates = new ElementStateMap<S>();
//        this.clsStates = null;
//    }
    
    /**
     * Creates a new <code>Trajectory</code> given the <code>Class&lt;S&gt;</code>
     * object of the underlying <code>ProbeState</code> type, <code><b>S</b></code>.
     * 
     * 
     * @param clsStates - the <code>Class&lt;S&gt;</code> object of the underlying
     * 		<code>ProbeState</code> type
     * 
     * @since Jul 1, 2014
     */
    public Trajectory(final Class<S> clsStates) {
        this._history  = new RealNumericIndexer<S>();
//        this.mapStates = new ElementStateMap<S>();
        this.mapStates = new NodeIdToElemStates();
        this.clsStates = clsStates;
    }

    /**
     * Set the user comment string
     * 
     *  @param  strDescr    user comment string
     */
    public void setDescription(String strDescr) {   description = strDescr; };

    /**
     * Set the time stamp of the trajectory.
     * 
     * @param lngTimeStamp  number of milliseconds since January 1, 1970 GMT  
     */
    public void setTimestamp(long lngTimeStamp) { timestamp = new Date(lngTimeStamp);  }

    /** 
     * Gets the <code>Class&lt;S&gt;</code> object of the generic type <b><code>S</code></b>. 
     * 
     * @return a <code>Class&lt;S&gt;</code> object for the class <b><code>S</code></b>
     * 
     * @author Jonathan M. Freed
     */
	public Class<S> getStateClass() {
		return this.clsStates;
    }   

    // ************* Trajectory Operations

    /**
     * Captures the specified probe's current state to a <code>ProbeState</code> object
     * then saves it to the trajectory.  State goes at the tail of the trajectory list.
     * 
     *  @param  probe   target probe object
     */
	public void update(Probe<S> probe) {
        S state = probe.cloneCurrentProbeState();
        saveState(state);
    }

    /**
     * Save the <code>ProbeState</code> object directly to the trajectory at the tail.
     * @param state     new addition to trajectory
     */
    public void saveState( final S state ) {
        double  dblPos = state.getPosition();
        _history.add( dblPos, state );
        
        String  strElemId = state.getElementId();
        this.mapStates.putState(strElemId, state);
    }
    
    /**
     * Remove the last state from the trajectory and return it.
     * 
     * @return  the most recent <code>ProbeState</code> in the history
     */
    public S popLastState()  {
        return _history.remove( _history.size() - 1 );
    }



    // ************* Trajectory Data

	
    /**
     * Return the user comment associated with this trajectory.
     * 
     * @return user comment (meta-data)
     */
    public String getDescription() {return description; }

    /**
     * Return the time stamp of the trajectory object
     * 
     * @return  trajectory time stamp
     */
    public Date getTimestamp() { return timestamp; }
    
    
    /**
     * Return an Iterator over the iterator's states.
     */
    public Iterator<S> stateIterator() {
        return _history.iterator();
    }
    
    /**
     * Return the number of states in the trajectory.
     * 
     * @return the number of states
     */
    public int numStates() {
        return _history.size();
    }


    /**
     * Returns the probe's initial state or null if there is none.
     * 
     * @return the probe's initial state or null
     */
    public S initialState() {
        return stateWithIndex(0);
    }
	
   /**
     * Returns the probe's final state or null if there is none.
     * 
     * @return the probe's final state or null
     */
    public S finalState() {
        return stateWithIndex(numStates()-1);
    }
    
	/**
	 * Get the list of states.
	 *
	 * @return a new list of this trajectory's states
	 */
	protected List<S> getStates() {
		return _history.toList();
	}
	
    /**
     * Returns the probe state at the specified position.  Returns null if there
     * is no state for the specified position.
     */
    public S stateAtPosition(double pos) {
        for(S state : _history) {
            if (state.getPosition() == pos)
                return state;
        }
        return null;
    }
	
	
	/**
	 * Get the state that is closest to the specified position
	 * @param position the position for which to find a state
	 * @return the state nearest the specified position
	 */
	public S stateNearestPosition( final double position ) {
		final int index = _history.getClosestIndex( position );
		return _history.size() > 0 ? _history.get( index ) : null;
	}

	
	// Does the return type need to be an array?  Otherwise an ArrayList would
	//   support generics
    /**
     * Returns the states that fall within the specified position range, inclusive.
     * @param low lower bound on position range
     * @param high upper bound on position range
     * @return an array of <code>ProbeState</code> objects whose position falls
     * within the specified range
     */
    public List<S> statesInPositionRange( final double low, final double high ) {
		final int[] range = _history.getIndicesWithinLocationRange( low, high );
		if ( range != null ) {
			final List<S> result = new ArrayList<S>( range[1] - range[0] + 1 );
			for ( int index = range[0] ; index <= range[1] ; index++ ) {
				result.add( _history.get( index ) );
			}
			//
//			final ProbeState[] resultArray = new ProbeState[result.size()];
//			return result.toArray( resultArray );
			return result;
		}
		else {
//			return new ProbeState[0];
		    return new LinkedList<S>();
		}
    }
	
	/**
	 * Get the probe state for the specified element ID.
	 * @param elemID the name of the element for which to get the probe state
	 * @return The first probe state for the specified element.
	 */
	public S stateForElement( final String elemID ) {
//		return statesForElement( elemID )[0];
	    return statesForElement( elemID ).get(0);
	}
	
    /**
     * Returns the states associated with the specified element.
     * @param strElemId    the name of the element to search for
     * @return             an array of <code>ProbeState</code> objects for that element
     * @deprecated
     */
	@SuppressWarnings("rawtypes")
    @Deprecated
    public ProbeState[] statesForElement_OLD(String strElemId) {
        List<ProbeState> result = new ArrayList<ProbeState>();
        Iterator<S> it = stateIterator();
        while (it.hasNext()) {
            ProbeState state = it.next();
            if ((state.getElementId().equals(strElemId))
            	||(state.getElementId().equals(strElemId+"y"))) {
                result.add(state);
            }
        }
        ProbeState[] resultArray = new ProbeState[result.size()];
        return result.toArray(resultArray);
    }

    /**
     * <p>
     * Revised version of state lookup method for an element ID
     * class, which theoretically corresponds to a hardware node
     * ID.  The element ID class consists of all the modeling element
     * string identifiers which have the same prefix.  Since the lattice
     * generator creates element IDs by suffixing accelerator node IDs,
     * this technique hopefully works.
     * </p>
     * <p>
     * The revised part comes from the fact that the states are now stored and retrieved by
     * hashing, rather that by a linear search.  Specifically, a list of probe
     * states for every element ID class is hashed using the prefix of the 
     * element ID class.
     * </p>
     * 
     * @param strElemId element ID which identifies the accelerator node 
     * 
     * @return          all the probe states associated with the element ID class containing 
     *                  the given element ID
     *
     * @author Christopher K. Allen
     * @since  Jun 5, 2013
     */
    public List<S> statesForElement(String strElemId) {
        RealNumericIndexer<S>    setStates = this.mapStates.getStates(strElemId);
        List<S>                  lstStates = setStates.toList();
        
//        ProbeState[] arrStates = new ProbeState[lstStates.size()];
//        return lstStates.toArray(arrStates);
        return lstStates;
    }

    /**
     * Returns an array of the state indices corresponding to the specified element.
     * @param element name of element to search for
     * @return an array of integer indices corresponding to that element
     */
    public int[] indicesForElement(String element) {
        List<Integer> indices = new ArrayList<Integer>();
        int c1 = 0;
        Iterator<S> it = stateIterator();
        while (it.hasNext()) {
            S state = it.next();
            if ((state.getElementId().equals(element)
            		|| state.getElementId().equals(element+"y"))) {
                indices.add(c1);
            }
            c1++;
        }
        int[] resultArray = new int[indices.size()];
        int c2 = 0;
        for (Iterator<Integer> indIt = indices.iterator(); indIt.hasNext(); c2++) {
            resultArray[c2] = indIt.next();
        }
        return resultArray;
    }
    
    /**
     * Returns the state corresponding to the specified index, or null if there is none.
     * @param i index of state to return
     * @return state corresponding to specified index
     */
    public S stateWithIndex(int i) {
        try {
            return _history.get(i); 
        } catch (IndexOutOfBoundsException e) {
            return null;    
        }
    }
		

    /*
     * Iterable Interface
     */
    
    /**
     * Returns an iterator over all the probe states in the trajectory.  This is 
     * the single method in the <code>Iterable<T></code> interface which facilitates
     * the "for each" statement.
     * 
     * @return  iterator for use in a <code>(T X : Container&lt;T&gt;)</code> statement
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2013
     */
    public Iterator<S> iterator() {
        return this._history.iterator();
    }

    // *********** debugging

    /*
     * Object Overrides
     */

    /**
     * Store a textual representation of the trajectory to a string
     * @return     trajectory contents in string form 
     */
    @Override
    public String toString() {
    StringBuffer buf = new StringBuffer();
        buf.append("Trajectory: " + getClass().getName() + "\n");
        buf.append("Time: " + getTimestamp() + "\n");
        buf.append("Description: " + getDescription() + "\n");
        buf.append("States: " + _history.size() + "\n");
        Iterator<S> it = stateIterator();
        while (it.hasNext()) {
            buf.append(it.next().toString() + "\n");
        }
        return buf.toString();
    }






    // Persistence Methods ====================================================

    /*
     * IArchive Interface
     */

    /**
     * Adds a representation of this Trajectory and its state history to the supplied <code>DataAdaptor</code>.
     * @param container <code>DataAdaptor</code> in which to add <code>Trajectory</code> data
     */
    public void save(DataAdaptor container) {
        DataAdaptor trajNode = container.createChild(TRAJ_LABEL);
        trajNode.setValue(TYPE_LABEL, getClass().getName());
        trajNode.setValue(TIMESTAMP_LABEL, new Double(getTimestamp().getTime()));
        if (getDescription().length() > 0)
            trajNode.setValue(DESCRIPTION_LABEL, getDescription());
        addPropertiesTo(trajNode);
        addStatesTo(trajNode);
    }

    /**
     * Load the current <code>Trajectory</code> object with the state history
     * information in the <code>DataAdaptor</code> object.
     * 
     *  @param  container   <code>DataAdaptor</code> from which state history is extracted
     * 
     *  @exception  DataFormatException     malformated data in <code>DataAdaptor</code>
     */
    public void load(DataAdaptor container) throws DataFormatException {
//        DataAdaptor daptTraj = container.childAdaptor(Trajectory.TRAJ_LABEL);
//        if (daptTraj == null)
//            throw new DataFormatException("Trajectory#load() - DataAdaptor contains no trajectory node");
        DataAdaptor daptTraj = container;

        long time =
            new Double(daptTraj.doubleValue(TIMESTAMP_LABEL)).longValue();
        setTimestamp(time);
        setDescription(daptTraj.stringValue(DESCRIPTION_LABEL));
        try {
            readPropertiesFrom(daptTraj);
            readStatesFrom(daptTraj);
        } catch (ParsingException e) {
            e.printStackTrace();
            throw new DataFormatException(
                "Exception loading from adaptor: " + e.getMessage());
        }
    }





    // Support Methods ========================================================

    /**
     * Iterates over child nodes, asking the concrete Trajectory subclass to
     * create a <code>ProbeState</code> of the appropriate species, initialized
     * from the contents of the supplied <code>DataAdaptor</code>
     * 
     * @param container <code>DataAdaptor</code> containing the child state nodes
     */
    @SuppressWarnings("unchecked")
	private void readStatesFrom(DataAdaptor container)
        throws ParsingException {
        Iterator<? extends DataAdaptor> childNodes = container.childAdaptors().iterator();
        while (childNodes.hasNext()) {
            DataAdaptor childNode = childNodes.next();
            if (!childNode.name().equals(ProbeState.STATE_LABEL)) {
                throw new ParsingException(
                    "Expected state element, got: " + childNode.name());
            }
            
            //*********************************************************
            String type = childNode.stringValue(ProbeState.TYPE_LABEL);
            S probeState;
            try {
                Class<?> probeStateClass = Class.forName(type);
                probeState = (S) probeStateClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new ParsingException(e.getMessage());
            }
            
            probeState.load(childNode);
            saveState(probeState);
        }
    }

    /**
     * Save the current trajectory information in the proper trajectory format to the target <code>DataAdaptor</code> object.
     * @param container     <code>DataAdaptor</code> to receive trajectory history
     */
    private void addStatesTo(DataAdaptor container) {
        Iterator<S> it = stateIterator();
        while (it.hasNext()) {
            S ps = it.next();
            ps.save(container);
        }
    }

}
