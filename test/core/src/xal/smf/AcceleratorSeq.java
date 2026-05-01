package xal.smf;

import xal.smf.impl.qualify.*;
import xal.tools.data.*;
import xal.smf.attr.*;
import xal.ca.ChannelFactory;

import java.util.*;
import java.util.logging.*;


/** 
 * The implementation of the accelerator composite node,
 * ordered collection of accelerator elements and sequences.
 *
 * @author  Nikolay Malitsky, Christopher K. Allen
 */

public class AcceleratorSeq extends AcceleratorNode implements DataListener {
	/** indicates the node type as being a sequence */
    public static final String    s_strType = "sequence";

    /**    bucket for sequence parameters  */
    protected SequenceBucket   m_bucSequence;

    /** Container of immediate nodes in this sequence */
    protected List<AcceleratorNode> m_arrNodes;
	
	/** table of nodes keyed by ID */
	protected Map<String, AcceleratorNode> nodeTable;
	
	/** Container of immediate subsequences */
	protected List<AcceleratorSeq> _sequences;
  
    
    /*
     * Bucket Support
     */

    public void addBucket(AttributeBucket buc)  {
		super.addBucket(buc);
        if (buc.getClass().equals( SequenceBucket.class )) {
            setSequence((SequenceBucket)buc);
        }
	}
  

    /** returns the bucket containing the sequence parameters
     *   - see attr. SequenceBucket  */
    public SequenceBucket      getSequenceBuc()          { return m_bucSequence; };
    /* sets the bucket containing the twiss parameters
     *   - see attr.TwissBucket  */

    /**
     *
     * @param buc
     *
     * @author Christopher K. Allen
     * @since  May 3, 2011
     */
    public void setSequence(SequenceBucket buc)   { 
        m_bucSequence = buc; 
        m_mapAttrs.put(buc.getType(), buc); 
        };


    /** data adaptor label */
    public String dataLabel() { return "sequence"; }
    
    
    /** Update this sequence from the specified data adaptor */ 
    public void update( final DataAdaptor adaptor )  throws NumberFormatException {
        super.update( adaptor );
		
		final Accelerator accelerator = getAccelerator();
		final AcceleratorNodeFactory nodeFactory = accelerator.getNodeFactory();
        
        // read all child sequences
        final List<DataAdaptor> sequenceAdaptors = adaptor.childAdaptors( "sequence" );
        for ( final DataAdaptor sequenceAdaptor : sequenceAdaptors ) {
            try {
				if ( sequenceAdaptor.hasAttribute( "exclude" ) ) {
					if ( sequenceAdaptor.booleanValue( "exclude" ) ) {
						final String nodeID = sequenceAdaptor.stringValue( "id" );
						final AcceleratorNode node = getNodeWithId( nodeID );
						if ( node != null ) {
							removeNode( node );
						}
						continue;
					}
				}
                addChildSequence( sequenceAdaptor, accelerator, nodeFactory );
            }
            catch ( ClassNotFoundException exception ) {
				final String message = "Error reading child sequence for parent: " + m_strId;
				Logger.getLogger( "global" ).log( Level.SEVERE, message, exception );
                exception.printStackTrace();
            }
        }

        // read all of the child accelerator nodes
        final List<DataAdaptor> nodeAdaptors = adaptor.childAdaptors( "node" );
        for ( final DataAdaptor nodeAdaptor : nodeAdaptors ) {
            try {
				if ( nodeAdaptor.hasAttribute( "exclude" ) ) {
					if ( nodeAdaptor.booleanValue( "exclude" ) ) {
						final String nodeID = nodeAdaptor.stringValue( "id" );
						final AcceleratorNode node = getNodeWithId( nodeID );
						if ( node != null ) {
							removeNode( node );
						}
						continue;
					}
				}
                addChildNode( nodeAdaptor, accelerator, nodeFactory );
            }
            catch (ClassNotFoundException exception) {
				final String message = "Error reading child node for sequence: " + m_strId;
				Logger.getLogger( "global" ).log( Level.SEVERE, message, exception );
                exception.printStackTrace();
            }
        }
    }
    
    
    /** support for dataListener */
    private void addChildSequence( final DataAdaptor sequenceAdaptor, final Accelerator accelerator, final AcceleratorNodeFactory nodeFactory ) throws ClassNotFoundException {
        String sequenceType = sequenceAdaptor.stringValue( "type" );
        if ( sequenceType == null || sequenceType.isEmpty() ) {   // it's just a plain sequence
            String sequenceId = sequenceAdaptor.stringValue( "id" );
            // check if we already have the sequence
            AcceleratorSeq sequence = (AcceleratorSeq)getNodeWithId( sequenceId );
            // if the sequence doesn't already exist, create a new one and add the new sequence to the accelerator root sequence
            if ( sequence == null ) {
                sequence = new AcceleratorSeq( sequenceId );
                sequence.setAccelerator( accelerator );
                sequence.update( sequenceAdaptor );   // update the sequence
                addNode( sequence );
            }
            else {
                sequence.update( sequenceAdaptor );   // update the sequence
            }
        }
        else {
           addChildNode( sequenceAdaptor, accelerator, nodeFactory );
        }
    }
    
    
    /** support for dataListener */
    private void addChildNode( final DataAdaptor nodeAdaptor, final Accelerator accelerator, final AcceleratorNodeFactory nodeFactory ) throws ClassNotFoundException {
        String nodeId = nodeAdaptor.stringValue("id");
        // see if we already have the accelerator node
        AcceleratorNode node = getNodeWithId( nodeId );
        if ( node == null ) {
            // create a new AcceleratorNode off of the adaptor
            node = nodeFactory.createNode( nodeAdaptor );
            node.setAccelerator( accelerator );
            node.update( nodeAdaptor );   // update the node
            addNode( node );
        }
        else {
            node.update( nodeAdaptor );   // update the node
        }
    }
	
	
	/** 
	 * write this sequence's child accelerator nodes deeply traversing each branch.  
	 * Some applications call this method since Combo sequence overrides the write method to provide only the constituents.
	 */
	public void writeDeeply( final DataAdaptor adaptor ) {
        super.write( adaptor );
        adaptor.writeNodes( m_arrNodes );
	}
        

    /** write the acceleratorSeq to the data adaptor */
    public void write( final DataAdaptor adaptor ) {
		writeDeeply( adaptor );
    }
        

    /** base constructor */
    public AcceleratorSeq( final String strId ) {
        this( strId, 0 );
    }
    
	
    /** constructor that sets aside some space for nodes to come */   
    public AcceleratorSeq( final String strId, int intReserve ) {
        this( strId, ChannelFactory.defaultFactory(), intReserve );
    }


	/** constructor that sets aside some space for nodes to come */
	public AcceleratorSeq( final String strId, final ChannelFactory channelFactory ) {
		this( strId, channelFactory, 0 );
	}


	/** designated constructor that sets aside some space for nodes to come */
	public AcceleratorSeq( final String strId, final ChannelFactory channelFactory, int intReserve ) {
		super( strId, channelFactory );

		m_arrNodes = new ArrayList<AcceleratorNode>( intReserve );
		_sequences = new ArrayList<AcceleratorSeq>();
		nodeTable = new HashMap<String, AcceleratorNode>( intReserve );
	}


    /** Support the node type */
    public String getType() { return s_strType; };
	
		
	/** 
	 * Get the ID of this sequence.  Subclasses which represent combinations of sequences should override this 
	 * method to return the ID of the first sequence in the combination.
	 * @return the ID of this sequence
	 */
	public String getEntranceID() {
		return getId();
	}
 	
	
	/** get the primary ancestor sequence that is a direct child of the accelerator */
	public AcceleratorSeq getPrimaryAncestor() {
		return ( getParent() == m_objAccel ) ? this : getParent().getPrimaryAncestor();
	}


    /** returns the number of nodes in this sequence */
    public int getNodeCount() { return getNodes().size(); }

	
    /** 
	 * returns the index of a node
     * @param child the node for which to get the index
     */
    public int getIndexOfNode( final AcceleratorNode child ) { return getNodes().indexOf( child ); }

	
    /** 
     * returns the accelerator node at a prescribed index within this sequence
     * @param iIndex the index for the node of interest (indexing starts with 0)
     */
    public AcceleratorNode  getNodeAt( final int iIndex ) { 
		return getNodes().get( iIndex );
	}
      
    
    /**
     * Find the index to insert the node in increasing order of position.
     * For efficiency, start comparing with the last node since they are
     * likely to roughly be added in order.
     */
    protected int indexToAddNode( final AcceleratorNode newNode ) {
        int insertIndex = 0;
        for ( int index = m_arrNodes.size()-1 ; index >= 0 ; index-- ) {
            AcceleratorNode node = m_arrNodes.get(index);
            if ( newNode.m_dblPos >= node.m_dblPos ) {
                insertIndex = index + 1;
                break;
            }
        }
        
        return insertIndex;
    }
    
    
    /** Method to move a node from one sequence to this sequence
     * warning be careful - need to check node position when moving it about!!
     */
    public boolean addNode( final AcceleratorNode newNode ) {
        final int insertIndex = indexToAddNode( newNode );
        
        try {
            addNodeAt( insertIndex, newNode );
			nodeTable.put( newNode.getId(), newNode );
			if ( newNode instanceof AcceleratorSeq ) { 
				_sequences.add( (AcceleratorSeq)newNode );
			}
            return true;
        }
        catch( IndexOutOfBoundsException exception ) {
            return false;
        }
    }
      

    /** Method to add a node from to this sequence
     * the node is also kept in its original sequence. This is used when
     * concatenating nodes together into a new sequence. 
     */
    protected boolean addSoft( final AcceleratorSeq node ){
   
		final int insertIndex = indexToAddNode( node );
        m_arrNodes.add( insertIndex, node );
		nodeTable.put( node.getId(), node );
        
        this.setAccelerator( node.getAccelerator() ); // set to the same accelerator as added node
 
        this.m_bolIsSoft = true;
        return true;
    };
    

    /**
     * Check to see if the node is a shallow child of this sequence.
     * @param node - the node to check
	 * @return true if the node is a shallow child of this sequence and false if not
     */
    public boolean contains( final AcceleratorNode node ) {
		return getNodes().contains( node );
    }
	

    /** Add a node at a prescribed index
     * @param iIndex = the index to insert this node
     * @param node - the node to insert
     */
    public void addNodeAt( int iIndex, AcceleratorNode node ) throws IndexOutOfBoundsException {
        m_arrNodes.add( iIndex, node );
        
        // Set new parent sequence
        node.setParent( this );
        node.setAccelerator( this.getAccelerator() );
    }
    
	
    /**
     * remove a node from this sequence
     * @param node the node to remove
     */
    public boolean removeNode( final AcceleratorNode node ) {
        if( !m_arrNodes.remove( node ) )
            return false;
        
		nodeTable.remove( node.getId() );
		
		if ( node instanceof AcceleratorSeq ) {
			_sequences.remove( node );
		}
		
        node.setParent( null );
        node.setAccelerator( null );

        return true;
    }
   
   
    /** Remove all nodes from the this sequence. */
    public void removeAllNodes() {      
        // remove parent and accelerator data for each node
		for ( final AcceleratorNode node : m_arrNodes ) {
            node.setParent( null );
            node.setAccelerator( null );
        }
    
        // Clean collection of nodes
        m_arrNodes.clear();
		_sequences.clear();
		nodeTable.clear();
    }

    
    /**
	 * Search deeply for and get the node with the specified id.
	 * @param label The id of the node we are seeking.
	 * @return the node corresponding to the requested id or null if no such node is found.
	 */    
    public AcceleratorNode getNodeWithId( final String label ) {
		// check if this node is itself a match
		if ( getId().equals( label ) ) {
			return this;
		}
		// else check if immediate children are matches
		else if ( nodeTable.containsKey(label) ) {
			return nodeTable.get(label);
		}
        
        // If we still haven't found the node, search deeply
		for ( final AcceleratorSeq sequence : getSequences() ) {
            AcceleratorNode node = sequence.getNodeWithId( label );
            if ( node != null ) {
                return node;
            }
        }
        
        return null;
    }
    
    
    /** 
	 * Filter nodes from the source list which can be cast to the specified result class.
     * @param resultClass filters the returned nodes to those which can be cast to this class
	 * @param sourceNodes the list of nodes to filter
	 * @return the list of nodes matching the qualifier criteria
	 */
    @SuppressWarnings( "unchecked" )    // we do check the class cast, but the compiler has no way of knowing
    static public <SourceType extends AcceleratorNode,NodeType extends SourceType> List<NodeType> filterNodesByClass( final Class<NodeType> resultClass, final List<SourceType> sourceNodes ) {
        final List<NodeType> matchedNodes = new ArrayList<NodeType>();    // returned list
        
		for ( final SourceType node : sourceNodes ) {
            if ( resultClass.isInstance( node ) ) {
                matchedNodes.add( (NodeType)node );     // the cast is only for compile since generics are stripped at runtime
            }
		}
        
        return matchedNodes;
    }


    /**
	 * Filter nodes from the source list using the specified qualifier.
     * @param resultClass filters the returned nodes to those which can be cast to this class (assumes AcceleratorNode if null)
	 * @param sourceNodes the list of nodes to filter
	 * @param qualifier the qualifier used to filter the nodes
	 * @return the list of nodes matching the qualifier criteria
	 */
    static public <SourceType extends AcceleratorNode,NodeType extends SourceType> List<NodeType> getNodesOfClassWithQualifier( final Class<NodeType> resultClass, final List<SourceType> sourceNodes, final TypeQualifier qualifier ) {
        return appendNodesOfClassWithQualifier( resultClass, new ArrayList<NodeType>(), sourceNodes, qualifier );
    }



    /**
	 * Filter nodes from the source list using the specified qualifier.
     * @param resultClass filters the returned nodes to those which can be cast to this class (assumes AcceleratorNode if null)
	 * @param matchedNodes container to append the list of matching nodes
	 * @param sourceNodes the list of nodes to filter
	 * @param qualifier the qualifier used to filter the nodes
	 * @return the list of nodes matching the qualifier criteria
	 */
    @SuppressWarnings( "unchecked" )    // we do check the class cast, but the compiler has no way of knowing
	static public <SourceType extends AcceleratorNode,NodeType extends SourceType> List<NodeType> appendNodesOfClassWithQualifier( final Class<NodeType> resultClass, final List<NodeType> matchedNodes, final List<SourceType> sourceNodes, final TypeQualifier qualifier ) {
        // for performance reasons, we handle NodeType for Accelerator nodes separately from AcceleratorNode subclasses
        if ( resultClass == null || AcceleratorNode.class.equals( resultClass ) ) {     // we don't need to check the node class since the source nodes are all accelerator nodes
            for ( final SourceType node : sourceNodes ) {
                if ( qualifier.match( node ) ) {
                    matchedNodes.add( (NodeType)node );     // the cast is only for compile since generics are stripped at runtime
                }
            }
        }
        else {
            for ( final SourceType node : sourceNodes ) {
                if ( resultClass.isInstance( node ) && qualifier.match( node ) ) {
                    matchedNodes.add( (NodeType)node );     // the cast is only for compile since generics are stripped at runtime
                }
            }
        }

        return matchedNodes;
	}



    /**
	 * Filter nodes from the source list using the specified qualifier.
	 * @param matchedNodes container to append the list of matching nodes
	 * @param sourceNodes the list of nodes to filter
	 * @param qualifier the qualifier used to filter the nodes
	 * @return the list of nodes matching the qualifier criteria
	 */
	static public <SourceType extends AcceleratorNode,NodeType extends SourceType> List<NodeType> appendNodesWithQualifier( final List<NodeType> matchedNodes, final List<SourceType> sourceNodes, final TypeQualifier qualifier ) {
		return appendNodesOfClassWithQualifier( null, matchedNodes, sourceNodes, qualifier );
	}

    
    /** 
	 * Get nodes using the specified qualifier.
     * @param resultClass filters the returned nodes to those which can be cast to this class
	 * @param qualifier the qualifier used to filter the nodes
	 * @return the list of nodes matching the qualifier criteria
	 */
    public <SourceType extends AcceleratorNode,NodeType extends SourceType> List<NodeType> getNodesOfClassWithQualifier( final Class<NodeType> resultClass, final TypeQualifier qualifier ) {
        return getNodesOfClassWithQualifier( resultClass, getNodes(), qualifier );
    }
    
    
    /** 
	 * Get nodes of the specified class with the specified status.
     * @param resultClass filters the returned nodes to those which can be cast to this class
     * @param statusFilter the status for which to filter nodes
	 * @return the list of nodes matching the qualifier criteria
	 */
    public <SourceType extends AcceleratorNode,NodeType extends SourceType> List<NodeType> getNodesOfClassWithStatus( final Class<NodeType> resultClass, final boolean statusFilter ) {
        return getNodesOfClassWithQualifier( resultClass, getNodes(), QualifierFactory.getStatusQualifier( statusFilter ) );
    }
    
    
    /** 
	 * Filter nodes from the source list using the specified qualifier.
	 * @param sourceNodes the list of nodes to filter
	 * @param qualifier the qualifier used to filter the nodes
	 * @return the list of nodes matching the qualifier criteria
	 */
    static public <SourceType extends AcceleratorNode,NodeType extends SourceType> List<NodeType> getNodesWithQualifier( final List<SourceType> sourceNodes, final TypeQualifier qualifier ) {
		return appendNodesWithQualifier( new ArrayList<NodeType>(), sourceNodes, qualifier );
    }
    
    
    /** 
	* Filter nodes from the source list using the specified status filter.
	* @param nodes the list of nodes to filter
	* @param statusFilter the status for which to filter nodes
	* @return the list of nodes matching the status criterion
	*/
    static public <NodeType extends AcceleratorNode> List<NodeType> filterNodesByStatus( final List<NodeType> nodes, final boolean statusFilter ) {
        return getNodesWithQualifier( nodes, QualifierFactory.getStatusQualifier( statusFilter ) );        
    }
	
    
    /** 
	 * Shallow fetch of nodes whose type is given by the string.  Only nodes that are immediate children of this sequence are filtered.  A node will be considered as a match
	 * if the specified type matches either the node directly or that of one of the node's superclasses.  For example, a quadrupole would match both the "Q" type and the "magnet" type.
	 * @param strTypeId type identifier of the nodes to fetch
	 * @return a list of this sequence's nodes which match the specified type
	 */
    public <NodeType extends AcceleratorNode> List<NodeType> getNodesOfType( final String strTypeId )    {
        return this.<NodeType>getNodesWithQualifier( new KindQualifier( strTypeId ) );
    }
	
    
    /** 
	* Shallow fetch of nodes whose type is given by the string.  Only nodes that are immediate children of this sequence are filtered.  A node will be considered as a match if the specified
	* type matches either the node directly or that of one of the node's superclasses.  For example, a quadrupole would match both the "Q" type and the "magnet" type.
	* @param strTypeId type identifier of the nodes to fetch
	* @param statusFilter the status for which to filter the nodes
	* @return a list of this sequence's nodes which match the specified type
	*/
    public <NodeType extends AcceleratorNode> List<NodeType> getNodesOfType( final String strTypeId, final boolean statusFilter )    {
        return this.<NodeType>getNodesWithQualifier( new AndTypeQualifier().and( strTypeId ).and( QualifierFactory.getStatusQualifier( statusFilter ) ) );
    }
    
    
    /** 
	 * Shallow fetch of nodes that are matched by the qualifier. Only nodes that are immediate children of this sequence are filtered.
	 * @param qualifier the qualifier used to filter nodes
	 * @return a list of this sequence's nodes which match the qualifier criteria
	 */
    public <NodeType extends AcceleratorNode> List<NodeType> getNodesWithQualifier( final TypeQualifier qualifier ) {
		return appendNodesWithQualifier( new ArrayList<NodeType>(), getNodes(), qualifier );
    }
        
    
    /** 
	 * Fetch all nodes whose type is matched through the qualifier and are also contained in this sequence looking deeply through its
	 * nested child sequences.  This sequence itself is not among the nodes that will be tested against the qualifier.
	 * @param strTypeId the type of node for which we are fetching
	 * @return the list of all inclusive nodes which match the qualifier criteria
	 */
    public <NodeType extends AcceleratorNode> List<NodeType> getAllNodesOfType( final String strTypeId ) {
        return this.<NodeType>getAllNodesWithQualifier( new KindQualifier( strTypeId ) );
    }
        
    
    /** 
	 * Fetch all nodes whose type is matched through the qualifier and are also contained in this sequence or one of its nested child sequences.
	 * This sequence itself is not among the nodes that will be tested against the qualifier.
	 * @param qualifier the qualifier for filtering the nodes
	 * @return the list of all inclusive nodes which match the qualifier criteria
	 */
    public <NodeType extends AcceleratorNode> List<NodeType> getAllNodesWithQualifier( final TypeQualifier qualifier ) {
		return appendNodesWithQualifier( new ArrayList<NodeType>(), getAllNodes(), qualifier );
    }
    
    
    /** 
	* Fetch all nodes which are contained in this sequence looking deeply through its nested child sequences.  
	* This sequence itself is among the nodes included, hence the "Inclusive" nature of this method.
	* @return the list of all inclusive nodes
	*/
    public List<AcceleratorNode> getAllInclusiveNodes() {
		List<AcceleratorNode> allNodes = new ArrayList<AcceleratorNode>();
		allNodes.add( this );
		allNodes.addAll( getAllNodes() );
        
        return allNodes;
    }
    
    
    /** 
	* Fetch all nodes which are contained in this sequence looking deeply through its nested child sequences.  
	* This sequence itself is among the nodes included, hence the "Inclusive" nature of this method.
	* @param statusFilter the status for which to filter nodes
	* @return the list of all inclusive nodes
	*/
    public List<AcceleratorNode> getAllInclusiveNodes( final boolean statusFilter ) {
		return filterNodesByStatus( getAllInclusiveNodes(), statusFilter );
    }
    
    
    /** 
	 * Fetch all nodes whose type is matched through the qualifier and are also contained in this sequence looking deeply through its nested child sequences.  This sequence itself is among the nodes that
	 * will be tested against the qualifier, hence the "Inclusive" nature of this method.
	 * @param qualifier the qualifier for filtering the nodes
	 * @return the list of all inclusive nodes which match the qualifier criteria
	 */
    public <NodeType extends AcceleratorNode> List<NodeType> getAllInclusiveNodesWithQualifier( final TypeQualifier qualifier ) {
		return appendNodesWithQualifier( new ArrayList<NodeType>(), getAllInclusiveNodes(), qualifier );
    }
    
    
	/** 
	 * Get an iterator of this sequence's immediate child nodes.
	 * @return an iterator of this sequence's immediate child nodes.
	 */
    public Iterator<AcceleratorNode> getLeaves()   {
        return getNodes().iterator();
    }
    
    
    /** 
	 * Get a list of this sequence's immediate child nodes.
	 * @return a list of this sequence's immediate child nodes
	 */
    public List<AcceleratorNode> getNodes() {
        return Collections.<AcceleratorNode>unmodifiableList( m_arrNodes );
    }
	
	
	/**
	 * Get the this sequence's immediate child nodes with the specified status.
	 * @param statusFilter the status for which to qualify nodes
	 */
	public List<AcceleratorNode> getNodes( final boolean statusFilter ) {
		return filterNodesByStatus( getNodes(), statusFilter );
	}
    
    
    /** 
	 * Fetch all nodes contained in this sequence looking deeply through its nested child sequences.
	 * @return a list of all nodes contained in this sequence
	 */
    public List<AcceleratorNode> getAllNodes()   {
        return recurNodeSearch( new LinkedList<AcceleratorNode>(), this );
    }
	
	
	/**
	 * Get all nodes filtered for the specified status.
	 * @param statusFilter the status for which to filter the nodes
	 * @return the list of all nodes deeply nested with the specified status
	 */
	public List<AcceleratorNode> getAllNodes( final boolean statusFilter ) {
		return filterNodesByStatus( getAllNodes(), statusFilter );
	}
	
	
    /**
     * Return the total length (m) of this sequence 
     * @return the length of this sequence along the closed orbit
     */
    public double getLength() {
		return m_dblLen;
    }
	
	
    /**
     * Return the allowed predecessor sequences of a sequence 
     * At most there can be 2 predecessors.
	 * @return an array of allowed predessesor sequences
     */
    public String[] getPredecessors() {
        if ( m_bucSequence != null ) {
            return m_bucSequence.getPredecessors();
        }
        else {
            return new String[] {};
        }
    }
	
	
	/**
	 * Determine if this sequence can precede the one specified.
	 * @param sequence the sequence which we are testing if this sequence can precede
	 * @return true if this sequence can precede the sequence specified in the argument and false if not
	 */
	public boolean canPrecede( final AcceleratorSeq sequence ) {
		String[] predecessors = sequence.getPredecessors();
		for ( int index = 0 ; index < predecessors.length ; index++ ) {
			if ( m_strId.equals( predecessors[index] ) )  return true;
		}
		return false;
	}
	
	
	/**
	 * Determing if the ordered list of sequences forms a closed loop.  The determination
	 * is based on whether each successive sequence has a predecessor (based on
	 * the getPredecessors() method) that precedes it in the list, and the last item in
	 * the list is a predecessor of the first item in the list.
	 * @param sequences an ordered list of sequences to test for forming a ring
	 * @return true if the sequences form a ring and false if not
	 */
	static public boolean formsRing( final List<AcceleratorSeq> sequences ) {
		final int count = sequences.size();
		
		// test if we have more than one sequence
		if ( count < 2 )  return false;
		
		// test if each sequence can precede the one that follows
		AcceleratorSeq previousSequence = sequences.get( count - 1 );
		for ( int index = 0 ; index < count ; index++ ) {
			final AcceleratorSeq sequence = sequences.get( index );
			if ( !previousSequence.canPrecede( sequence ) )  return false;
			previousSequence = sequence;
		}
		
		return true;
	}
    
    
    /**
     * Given a collection of sequences, order the sequences according to their predecessor attribute.
	 * @param sequences a collection of contiguous sequences to order from start to finish
	 * @return the ordered list of sequences
     */
    static public List<AcceleratorSeq> orderSequences( final Collection<AcceleratorSeq> sequences ) throws SequenceOrderingException {
        final Map<String,AcceleratorSeq> sequenceMap = new HashMap<String,AcceleratorSeq>( sequences.size() );
        Iterator<AcceleratorSeq> sequenceIter = sequences.iterator();
         
        AcceleratorSeq sequence = null;
        while ( sequenceIter.hasNext() ) {
            sequence = sequenceIter.next();
            sequenceMap.put( sequence.getId(), sequence );
        }
         
        if ( sequence == null )  return Collections.<AcceleratorSeq>emptyList();
         
        final LinkedList<AcceleratorSeq> orderedSequences = new LinkedList<AcceleratorSeq>();
        sequence.addSequenceChain( orderedSequences, sequenceMap );
        
        while ( !sequenceMap.isEmpty() ) {
            Collection<AcceleratorSeq> remainingSequences = sequenceMap.values();
            sequenceIter = remainingSequences.iterator();
            sequence = sequenceIter.next();
            final LinkedList<AcceleratorSeq> localList = new LinkedList<AcceleratorSeq>();
            sequence.addSequenceChain( localList, sequenceMap );
            
            // verify that the local list can be linked to the main list
            final AcceleratorSeq localLink = localList.getFirst();
            final AcceleratorSeq mainLink = orderedSequences.getLast();
            final String mainLinkId = mainLink.getId();
            final String[] predecessors = localLink.getPredecessors();
            boolean validLink = false;
            for ( int preIndex = 0 ; preIndex < predecessors.length ; preIndex++ ) {
                if ( mainLinkId.equals( predecessors[preIndex] ) ) {
                    validLink = true;
                    break;
                }
            }
            
            // link the new local list to the main list
            if ( validLink ) {
                orderedSequences.addAll( localList );
            }
            else {
                throw new SequenceOrderingException( sequences );
            }
        }
         
        return new ArrayList<AcceleratorSeq>( orderedSequences );
    }
    
    
    /** 
     * Internal support for the orderSequences() method.  Add this sequence to the 
     * beginning of the orderedSequences list and remove it from the sequenceMap.
     * Recursively do the same for its predecessor if it has one in the sequenceMap.
	 * @param orderedSequences the list of ordered sequences to append the next sequence
	 * @param sequenceMap the table of sequences from which to fetch the next sequence
     */
    protected List<AcceleratorSeq> addSequenceChain( final LinkedList<AcceleratorSeq> orderedSequences, final Map<String,AcceleratorSeq> sequenceMap ) {
        orderedSequences.addFirst( this );
        sequenceMap.remove( this.getId() );
		
        final String[] predecessors = getPredecessors();
        for ( int index = 0 ; index < predecessors.length ; index++ ) {
            final String predecessor = predecessors[index];
            final AcceleratorSeq preSequence = sequenceMap.get( predecessor );
            if ( preSequence != null ) {
                 return preSequence.addSequenceChain( orderedSequences, sequenceMap );
            }         
        }
		
        return orderedSequences;
    }

   
    /**
	 * Get a list of all of this sequence's subsequences including those deeply nested.
     * return a list of all of this sequence's subsequences
     */
    public List<AcceleratorSeq> getAllSeqs() {
        return recurSeqSearch( new LinkedList<AcceleratorSeq>(), this ); 
    }
    

    /**  
     * Return a sequence whose id matches the argument and which is an 
     * immediate child of this sequence.
     */
    public AcceleratorSeq getSequence( final String strId )    {
		for ( final AcceleratorSeq sequence : getSequences() ) {
            if ( sequence.getId().equals( strId ) )  return sequence;
        }
        
        return null;
    }
    
    
    /** Get the sequences that are immediate children of this sequence */
    public List<AcceleratorSeq> getSequences() {
        return _sequences;
    }
    
    
    /** Get the sequences that are immediate children of this sequence */
    public List<AcceleratorSeq> getSequences( final boolean statusFilter ) {
		return filterNodesByStatus( getSequences(), statusFilter );
    }
    
    
    /**
     * Get the position of a node in the sequence, including the possible offset of a local sequence containing the node.
     * @param node - the node for which the position is wanted
     */
    public double getPosition( final AcceleratorNode node )  {
		if ( node == this )  return 0.0;	// case of a sequence itself.

		final AcceleratorSeq parent = node.getParent();
	
		// If this sequence is not the node's parent sequence then add the parent sequence's position.
		// Calling "getPosition()" takes care of nested sequences.
		return ( parent == this ) ? node.m_dblPos : node.m_dblPos + getPosition( parent );
    }
	
	
	/**
	 * Get the distance of the second node from the first node.
	 * @param reference the node from which to measure the distance
	 * @param node the node to which to measure the distance
	 */
	public double getDistanceBetween( final AcceleratorNode reference, final AcceleratorNode node ) {
		return getRelativePosition( getPosition( node ), reference );
	}
	
	
	/**
	 * Convert the sequence position to a position relative to the specified reference node.
	 */
	public double getRelativePosition( final double position, final String referenceNodeID ) {
		return getRelativePosition( position, getNodeWithId( referenceNodeID ) );
	}
	
	
	/**
	 * Convert the sequence position to a position relative to the specified reference node.
	 * @param position the position of a location relative to the sequence's start
	 * @param referenceNode the node relative to which we wish to get the position
	 */
	public double getRelativePosition( final double position, final AcceleratorNode referenceNode ) {
		return position - getPosition( referenceNode );
	}
	
	
	/**
	 * Get the shortest relative postion of one node with respect to a reference node.  This is really useful for ring sequences.
	 * @param node the node whose relative position is sought
	 * @param referenceNode the reference node relative to which the node's position is calculated
	 * @return the distance (positive or negative) of the node with respect to the reference node whose magnitude is shortest 
	 */
	public double getShortestRelativePosition( final AcceleratorNode node, final AcceleratorNode referenceNode ) {
		return getDistanceBetween( referenceNode, node );
	}
	
    
    /**
     * Identify whether the sequence is within a linear section.  This helps 
     * us to determine whether it is meaningful to identify one node as being 
     * downstream from another.
     * At this time we will simply return true, but when the ring is 
     * introduced into the optics, we need to distinguish the ring from 
     * the linear sections.
     */
    public boolean isLinear() {
        return true;
    }


    /** 
	 * Sort nodes in place by position where the position used is relative to the this sequence.
	 * @param nodes the list of nodes to sort
     */
    public void sortNodes( final List<? extends AcceleratorNode> nodes )  {
		Collections.sort( nodes, new Comparator<AcceleratorNode>() {
			public int compare( final AcceleratorNode node1, final AcceleratorNode node2 ) {
				final double position1 = getPosition( node1 );
				final double position2 = getPosition( node2 );
				return position1 < position2 ? -1 : position1 > position2 ? 1 : 0;				
			}
		});
    }
	
	
    /** 
	 * Sort nodes in place by proximity to a reference node.  Used to get nodes ranked by nearness to the reference node.
	 * @param nodes the list of nodes to sort
	 * @param referenceNode the reference node for the proximity test
	 */
    public void sortNodesByProximity( final List<? extends AcceleratorNode> nodes, final AcceleratorNode referenceNode )  {
		Collections.sort( nodes, new Comparator<AcceleratorNode>() {
			public int compare( final AcceleratorNode node1, final AcceleratorNode node2 ) {
				final double node1Proximity = Math.abs( getShortestRelativePosition( node1, referenceNode ) );
				final double node2Proximity = Math.abs( getShortestRelativePosition( node2, referenceNode ) );
				return node1Proximity < node2Proximity ? -1 : ( node1Proximity > node2Proximity ? 1 : 0 );
			}
		});
    }
	
	
    /** 
	 * Sort nodes in place by position relative to the reference node (ranked negative to positive).
	 * @param nodes the list of nodes to sort
	 * @param referenceNode the reference node for the proximity test
	 */
    public void sortNodesByRelativePosition( final List<? extends AcceleratorNode> nodes, final AcceleratorNode referenceNode )  {
		Collections.sort( nodes, new Comparator<AcceleratorNode>() {
			public int compare( final AcceleratorNode node1, final AcceleratorNode node2 ) {
				final double node1Proximity = getShortestRelativePosition( node1, referenceNode );
				final double node2Proximity = getShortestRelativePosition( node2, referenceNode );
				return node1Proximity < node2Proximity ? -1 : ( node1Proximity > node2Proximity ? 1 : 0 );
			}
		});
    }
	
    
    /** 
	 * Get all sequences in the specified sequence branch, searching down all of its branches 
	 * @param sequences the collection of all sequences found
	 * @param branch the sequence branch down which to search for more sequences
	 * @return the collection of all sequences found; returned for convenience
	 */
    static protected LinkedList<AcceleratorSeq> recurSeqSearch( final LinkedList<AcceleratorSeq> sequences, final AcceleratorSeq branch )  {
		for ( AcceleratorSeq sequence : branch.getSequences() ) {
			sequences.add( sequence );
			recurSeqSearch( sequences, sequence );
		}
		
		return sequences;
    }
    
	
    /** 
	 * Get all nodes in the specified sequence searching deeply through this sequence's child sequences.
	 * @param nodes the collection of all nodes found
	 * @param sequence the branch down which to search for nodes
	 * @return the collection of all nodes found; returned for convenience
	 */
    static protected LinkedList<AcceleratorNode> recurNodeSearch( final LinkedList<AcceleratorNode> nodes, final AcceleratorSeq sequence )  {
		for ( AcceleratorNode node : sequence.getNodes() ) {
			nodes.add( node );
			if ( node instanceof AcceleratorSeq ) {
				recurNodeSearch( nodes, (AcceleratorSeq)node );
			}
		}
		
		return nodes;
    }
}



