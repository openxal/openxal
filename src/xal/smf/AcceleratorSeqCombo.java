package xal.smf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import xal.smf.impl.qualify.KindQualifier;
import xal.smf.impl.qualify.NotTypeQualifier;
import xal.smf.impl.qualify.TypeQualifier;
import xal.tools.data.DataAdaptor;


/** 
 * The implementation of the accelerator combo sequence,
 * This class is meant to deal with pasting together existing
 * sequences into new sequences.
 *
 * @author J. Galambos
 */

public class AcceleratorSeqCombo extends AcceleratorSeq {
  
    /*
     *  Constants
     */
    public static final String    s_strType = "sequenceCombo";

	/** map of dummy sequences keyed by constituent sequences */
	private Map<AcceleratorSeq,AcceleratorSeq> _dummyMap;

    /** total length of all primary sequences combined */
    private double totalLen;

    /** the names of the constituent sequences */
    private List<String> constituentNames;
	
	/** list of base constituents */
	private List<AcceleratorSeq> _baseConstituents;
	
	/** list of immediate constituents */
	private List<AcceleratorSeq> _constituents;
	
	
    /** Primary constructor */
    public AcceleratorSeqCombo( final String strID, final List<AcceleratorSeq> seqs ) {
        super( strID, 0 );
		flatten( seqs );
    }
	
	
    /** Constructor */
    public AcceleratorSeqCombo( final String strID, final Accelerator accelerator, final DataAdaptor adaptor ) {
        this( strID, getSequences( accelerator, adaptor ) );
    }
	
	
	/**
	 * Instantiate a an AcceleratorSeqCombo or a subclass depending on whether the sequences form a ring.
	 * @param strID The identifier of the new combo sequence
	 * @param sequences The sequences to flatten into a combo sequence
	 * @return a new AcceleratorSeqCombo instance if the sequences do not form a ring and a Ring if they do
	 */
	static public AcceleratorSeqCombo getInstance( final String strID, final List<AcceleratorSeq> sequences ) {
		return AcceleratorSeq.formsRing( sequences ) ? new Ring( strID, sequences ) : new AcceleratorSeqCombo( strID, sequences );
	}
	
	
	/**
	 * Instantiate a an AcceleratorSeqCombo or a subclass depending on whether the sequences form a ring.
	 * @param strID The identifier of the new combo sequence
	 * @param accelerator The accelerator that holds to the sequences
	 * @param adaptor the data adaptor for specifying the sequences to combine
	 * @return a new AcceleratorSeqCombo instance if the sequences do not form a ring and a Ring if they do
	 */
	static public AcceleratorSeqCombo getInstance( final String strID, final Accelerator accelerator, final DataAdaptor adaptor ) {
		final List<AcceleratorSeq> sequences = getSequences( accelerator, adaptor );
		return getInstance( strID, sequences ); 
	}
	
	
	/**
	 * Instantiate a an AcceleratorSeqCombo or a subclass depending on whether the sequences form a ring.
	 * @param accelerator The accelerator that holds to the sequences
	 * @param adaptor the data adaptor for specifying the sequences to combine
	 * @return a new AcceleratorSeqCombo instance if the sequences do not form a ring and a Ring if they do
	 */
	static public AcceleratorSeqCombo getInstance( final Accelerator accelerator, final DataAdaptor adaptor ) {
		final String ID = adaptor.stringValue( "id" );
		return getInstance( ID, accelerator, adaptor );
	}
	
	
	/**
	 * Get an instance of a non-cycling combo sequence which starts and ends between the specified sequences inclusively.
	 * @param comboID unique ID to assign to the new combo sequence
	 * @param startSequence first sequence in combo
	 * @param endSequence last sequence in combo
	 * @return a combo sequence ranging from the first sequence to the last sequence or null if none can be found
	 */
	static public AcceleratorSeqCombo getInstanceForRange( final String comboID, final AcceleratorSeq startSequence, final AcceleratorSeq endSequence ) {
		final List<AcceleratorSeqCombo> combos = getInstancesForRange( comboID, startSequence, endSequence );
		return combos.size() > 0 ? combos.get( 0 ) : null;
	}
	
	
	/**
	 * Get the list of all non-cycling combo sequences which start and end between the specified sequences inclusively.
	 * @param comboID unique ID to assign to the new combo sequence
	 * @param startSequence first sequence in combo
	 * @param endSequence last sequence in combo
	 * @return list of combo sequences ranging from the first sequence to the last sequence
	 */
	static public List<AcceleratorSeqCombo> getInstancesForRange( final String comboID, final AcceleratorSeq startSequence, final AcceleratorSeq endSequence ) {
		// create a sequence chain with just the end sequence
		final List<AcceleratorSeq> primerChain = new ArrayList<AcceleratorSeq>();
		primerChain.add( endSequence );
		
		// extend the sequence chain to get all chains which terminate back to the start sequence
		final List<List<AcceleratorSeq>> sequenceChains = extendChains( startSequence, primerChain );
		
		// construct a combo sequence for each sequence chain
		final List<AcceleratorSeqCombo> combos = new ArrayList<AcceleratorSeqCombo>( sequenceChains.size() );
		for ( final List<AcceleratorSeq> sequenceChain : sequenceChains ) {
			final AcceleratorSeqCombo combo = getInstance( comboID, sequenceChain );
			combos.add( combo );
		}
		return combos;
	}
	
	
	/**
	 * Get the list of all non-cycling sequence chains which extend the specified sequence chain back to the terminal sequence.
	 * @param terminalSequence sequence back which to extend the chains
	 * @param sequenceChain initial chain of sequences to extend
	 * @return all non-cycling sequence chains extending the given sequence chain to the terminal sequence
	 */
	static private List<List<AcceleratorSeq>> extendChains( final AcceleratorSeq terminalSequence, final List<AcceleratorSeq> sequenceChain ) {
		final List<List<AcceleratorSeq>> viableChains = new ArrayList<List<AcceleratorSeq>>();
		final AcceleratorSeq firstSequence = sequenceChain.get( 0 );
		
		// if the first sequence is the terminal sequence then we are done
		if ( firstSequence == terminalSequence ) {
			viableChains.add( sequenceChain );
		}
		else {	
			// check that there are predecessors to the first sequence and if so keep extending the chain back
			final String[] predecessorIDs = firstSequence.getPredecessors();
			if ( predecessorIDs != null && predecessorIDs.length != 0 ) {
				for ( final String predecessorID : predecessorIDs ) {
					final AcceleratorSeq predecessor = terminalSequence.getAccelerator().getSequence( predecessorID );
					if ( predecessor != null && !sequenceChain.contains( predecessor ) ) {
						// populate the extended chain with the predecessor followed by the original chain of sequences
						final List<AcceleratorSeq> extendedChain = new ArrayList<AcceleratorSeq>();
						extendedChain.add( predecessor );
						extendedChain.addAll( sequenceChain );
						// get all chains which extend from the current extended chain and terminate at the terminal sequence
						final List<List<AcceleratorSeq>> extendedChains = extendChains( terminalSequence, extendedChain );
						viableChains.addAll( extendedChains );
					}
				}
			}
		}		
					
		return viableChains;
	}
	
	
	/**
	 * Flatten the sequences into a single combo sequence.
	 * @param seqs The sequences to flatten into a single combo sequence.
	 */
	private void flatten( final List<AcceleratorSeq> seqs ) {
       	AcceleratorSeq dummySeq;
		double length = 0.;
		String [] predescrs;
		boolean match;
		totalLen = 0.;
		constituentNames = new ArrayList<String>();
		_constituents = new ArrayList<AcceleratorSeq>();
		_dummyMap = new HashMap<AcceleratorSeq,AcceleratorSeq>();
	
		// Loop through the list of sequences:
	
		int isFirst = 0;
		int ii = 0;
		AcceleratorSeq prev = null;	

		for ( final AcceleratorSeq seq : seqs ) {
			totalLen += seq.getLength();
	
				// set the accelerator:
			if(this.getAccelerator() == null) 
				   this.setAccelerator(seq.getAccelerator());
	
			dummySeq = new AcceleratorSeq("dummy" + seq.getId());
			constituentNames.add(seq.getId());
			_constituents.add( seq );
	
			dummySeq.addSoft(seq);
			_dummyMap.put( seq, dummySeq );
			dummySeq.setLength(seq.getLength());
	
	// set the position to location in new concatenated seq
	
			dummySeq.setPosition(length);
	
			if(prev != null) {
				predescrs = seq.getPredecessors(); // check if this insertion is allowed
				for(int i=0; i< predescrs.length; i++) {
					if(predescrs[i].equals(prev.getId()) ) match = true;
				}
			}
			this.addNode(dummySeq);
			isFirst = 1;
			length += seq.getLength();  // increment length along new seq.
			prev = seq;
		}
		
		generateBaseConstituents();
	}
	
	
	/**
	 * Get the sequences in the accelerator which are referenced in the adaptor.
	 * @param accelerator the accelerator from which to get the sequences
	 * @param adaptor the combo sequence adaptor
	 */
	static protected List<AcceleratorSeq> getSequences( final Accelerator accelerator, final DataAdaptor adaptor ) {
		List<AcceleratorSeq> sequences = new ArrayList<AcceleratorSeq>();
		
        // read all sequence references
        final List<DataAdaptor> sequenceAdaptors = adaptor.childAdaptors( "sequence" );
        for ( final DataAdaptor sequenceAdaptor : sequenceAdaptors ) {
			final String sequenceID = sequenceAdaptor.stringValue( "id" );
			sequences.add( accelerator.getSequence( sequenceID ) );
        }
		return sequences;
	}
	
	
	/**
	 * Write this sequence's definition to a data adaptor.
	 * @param adaptor the adaptor to which to write out this combo sequence's definition.
	 */
	public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "id", getId() );

		for ( final AcceleratorSeq sequence : getConstituents() ) {
			final DataAdaptor constituentAdaptor = adaptor.createChild( "sequence" );
			constituentAdaptor.setValue( "id" , sequence.getId() );
		}
	}
	
	
	/**
	 * Override to identify this sequence as a combo sequence
	 * @return the combo sequence type identifier
	 */
	public String getType() {
		return s_strType;
	}


    /** override the total length for this combo sequence */
    public double getLength() { return totalLen;}
    

    /** gets the list of the names of the constituent seqnences */
    public List<String> getConstituentNames() { return constituentNames; }
	
	
    /**
     *  get the position of a node in the sequence, including
     * the extra length of the sequence starting position itself
     *  
     *  Note: this way could also be done in the parent AcceleratorSeq class,
     *  be we choose to use a more efficient scheme there, specfic to 
     *  primary sequences.
     *
     * @param node - the node for which the position is wanted
     */
    public double getPosition( final AcceleratorNode node ) {
        if( m_arrNodes == null || m_arrNodes.isEmpty() )  return m_dblPos + node.m_dblPos;

		final AcceleratorSeq baseSequence = node.getPrimaryAncestor();
		final AcceleratorSeq dummySequence = _dummyMap.get( baseSequence );
		
		return dummySequence.getPosition() + baseSequence.getPosition( node );
    }
	
	
	/** 
	 * Get the ID of the first base constituent sequence of this combo sequence.
	 * @return the ID of first base constituent sequence
	 */
	public String getEntranceID() {
		final List<AcceleratorSeq> baseConstituents = getBaseConstituents();
		
		if ( baseConstituents.size() <= 0 )  return null;
		
		final AcceleratorSeq sequence = baseConstituents.get(0);
		
		return sequence.getEntranceID();
	}
	
	
    /**
	 * Search deeply for and get the node with the specified id.  Overrides the inherited method to search down the
	 * path of the constituent sequences.
	 * @param label The id of the node we are seeking.
	 * @return the node corresponding to the requested id or null if no such node is found.
	 */    
    public AcceleratorNode getNodeWithId( final String label ) {
		// first check if this sequence is itself a match
		if ( getId().equals( label ) )  return this;
		
		for ( final AcceleratorSeq sequence : getBaseConstituents() ) {
			final AcceleratorNode node = sequence.getNodeWithId(label);
			if ( node != null )  return node;
		}
		
		return null;
	}
  
    
    /** 
     * Shallow fetch of nodes.  Override the AcceleratorSeq version to only return
     * the list of nodes which are a union of those nodes which belong to the base constituent sub-sequences.
     */
    public List<AcceleratorNode> getNodes() {
        final List<AcceleratorNode> nodes = new ArrayList<AcceleratorNode>();
		for ( final AcceleratorSeq constituent : getBaseConstituents() ) {
            nodes.addAll( constituent.getNodes() );
        }
        return Collections.unmodifiableList( nodes );
    }
  
    
    /** 
     * Shallow fetch of sequences.  Override the AcceleratorSeq version to only return the list of
	 * sequences which are a union of those nodes which belong to the base constituent sub-sequences.
     */
    public List<AcceleratorSeq> getSequences() {
        final List<AcceleratorSeq> sequences = new ArrayList<AcceleratorSeq>();
		for ( final AcceleratorSeq constituent : getBaseConstituents() ) {
            sequences.addAll( constituent.getSequences() );
        }
        return Collections.unmodifiableList( sequences );
    }
    
    
    /**
     * Get the constituent sequences.
	 * @return the list of constituent sequences that make the primary sequence
     */
    public List<AcceleratorSeq> getConstituents() {
		return _constituents;
    }
    
    
    /**
     * Get the constituent sequences that are matched by the qualifier.
	 * @param qualifier The qualifier to restrict which constituents are returned
	 * @return the list of constituent sequences that make the combo sequence
     */
    public List<AcceleratorSeq> getConstituentsWithQualifier( final TypeQualifier qualifier ) {
        final List<AcceleratorSeq> dummySequences = super.getSequences();
        final List<AcceleratorSeq> constituents = new ArrayList<AcceleratorSeq>();
		
		for ( AcceleratorSeq dummySequence : dummySequences ) {
            final List<AcceleratorNode> matchingNodes = dummySequence.getNodesWithQualifier( qualifier );
            final List<AcceleratorSeq> matchingSequences = new ArrayList<AcceleratorSeq>();
            for ( final AcceleratorNode node : matchingNodes ) {
                if ( node instanceof AcceleratorSeq ) {
                    matchingSequences.add( (AcceleratorSeq)node );
                }
            }
            constituents.addAll( matchingSequences );
		}
		
        return Collections.unmodifiableList( new ArrayList<AcceleratorSeq>( constituents ) );
    }
    
    
    /** Generate the constituent sequences looking deeply even if the combo sequences are nested in many layers. */
    private void generateBaseConstituents() {
		final TypeQualifier comboQualifier = new KindQualifier( s_strType );
		final List<AcceleratorSeq> baseConstituents = new ArrayList<AcceleratorSeq>();
		baseConstituents.addAll( getConstituentsWithQualifier( new NotTypeQualifier( comboQualifier ) ) );
		
        final List<AcceleratorSeq> combos = getConstituentsWithQualifier( comboQualifier );
		for ( final AcceleratorSeq constituent : combos ) {
			final AcceleratorSeqCombo combo = (AcceleratorSeqCombo)constituent;
			baseConstituents.addAll( combo.getBaseConstituents() );
		}
		
		_baseConstituents = Collections.unmodifiableList( baseConstituents );
    }
    
    
    /**
     * Get the constituent sequences looking deeply even if the combo sequences are nested in many layers.
	 * @return the list of constituent sequences that make the combo sequence
     */
    public List<AcceleratorSeq> getBaseConstituents() {
		return _baseConstituents;
    }
    
    
    /**
     * Get the constituent sequences looking deeply even if the combo
	 * sequences are nested in many layers.  Use the qualifier to limit the 
	 * constituent sequences to only those which match the qualifier.
	 * @param qualifier The qualifier for matching which constituents get returned
	 * @return the list of qualified constituent sequences that make the combo sequence
     */
    public List<AcceleratorSeq> getBaseConstituentsWithQualifier( final TypeQualifier qualifier ) {
		final TypeQualifier comboQualifier = new KindQualifier( s_strType );
		final List<AcceleratorSeq> baseConstituents = new ArrayList<AcceleratorSeq>();
		baseConstituents.addAll( getConstituentsWithQualifier( qualifier ) );
		
        final List<AcceleratorSeq> combos = getConstituentsWithQualifier( comboQualifier );
		for ( final AcceleratorSeq constituent : combos ) {
			final AcceleratorSeqCombo combo = (AcceleratorSeqCombo)constituent;
			baseConstituents.addAll( combo.getBaseConstituentsWithQualifier( qualifier ) );
		}
				
		return Collections.unmodifiableList( baseConstituents );
    }
    
    
    /** 
	 * Get all nodes including this sequence and constituent sequences and all of their children.
	 * @return the list of all inclusive nodes
	 */
    public List<AcceleratorNode> getAllInclusiveNodes() {
		final List<AcceleratorNode> allNodes = new ArrayList<AcceleratorNode>();
		allNodes.add( this );
		
		final List<AcceleratorSeq> constituents = getConstituents();
		for ( AcceleratorSeq constituent : constituents ) {
			allNodes.addAll( constituent.getAllInclusiveNodes() );			
		}
        
        return allNodes;
    }
    
    
    /** 
	 * Fetch all nodes looking deeply through nested child sequences.  Filter out constituent seqences.
	 * @return all child nodes looking deeply through nested child sequences
	 */
    public List<AcceleratorNode> getAllNodes()   {
        LinkedList<AcceleratorNode> lstNodes = new LinkedList<AcceleratorNode>();
		LinkedList<AcceleratorNode> extraNodes = new LinkedList<AcceleratorNode>();
        
        recurNodeSearch( lstNodes, this ); // get all nodes in this seq.
	      
	  	// Locate and prune soft nodes + primary sequences
		for ( AcceleratorNode node : lstNodes ) {
			if ( node.m_bolIsSoft )  extraNodes.add( node );
		}
		lstNodes.removeAll( extraNodes );
	
        return lstNodes;
    }
	
	
	/**
	 * Override the equals method to return true if and only if the two combo sequences 
	 * have equal IDs, are both combo sequences and have equal constituents.
	 * @param sequence the sequence against which to compare
	 * @return true if and only if the two sequences are equal
	 */
	public boolean equals( final Object sequence ) {
		// check that the sequence is indeed a combo sequence
		if ( !(sequence instanceof AcceleratorSeqCombo) )  return false;
		
		final AcceleratorSeqCombo compSequence = (AcceleratorSeqCombo)sequence;
		
		// check that the ID's are equal
		if ( !compSequence.getId().equals( getId() ) )  return false;
		
		// check whether the two combo sequences share the same65536 base constituents
		return compSequence.getBaseConstituents().equals( getBaseConstituents() );
	}


	/** Override hashCode() as required when overriding equals() */
	public int hashCode() {
		// hashCode must be consistent with equality which is based on base constituents
		return _baseConstituents.hashCode();
	}
}




	       
	       
