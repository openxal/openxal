/*
 * LatticeFactory.java
 *
 * Created on March 18, 2003, 9:36 PM
 */

package xal.sim.slg;

import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.Bend;
import xal.smf.impl.Magnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
/**
 * Factory to create a complete lattice from an XAL accelerator sequence
 * or acclerator combo sequence.
 *
 * @author  wdklotz
 */
public class LatticeFactory {
	private static java.io.PrintStream cout = System.out;
	private Lattice lattice;
	private final ArrayList thickKinds;
	private final ArrayList thinKinds;
	private static final ArrayList<String> std_thickKinds;
	private static final ArrayList<String> std_thinKinds;
	private boolean debug;
	private boolean verbose;
	private boolean halfmag;
	//the sequence this lattice is derived from.
	private AcceleratorSeq accelSeq;

	static {
		std_thickKinds = new ArrayList<String>();
		std_thinKinds = new ArrayList<String>();

		std_thickKinds.add("DH");
//		std_thickKinds.add("QH");
//		std_thickKinds.add("QTH");
//		std_thickKinds.add("QV");
//		std_thickKinds.add("QTV");
//		std_thickKinds.add("QSC");
		std_thickKinds.add("PQ");
		std_thickKinds.add("SH");
		std_thickKinds.add("SV");
		std_thickKinds.add("RG");     //slim
		std_thickKinds.add("BCM");   //slim 
		std_thickKinds.add("SOL");
		std_thickKinds.add("QUAD");

//		std_thinKinds.add("SH");
//		std_thinKinds.add("SV");
		std_thinKinds.add("DCH");
		std_thinKinds.add("DCV");
		std_thinKinds.add("EKick");
		std_thinKinds.add("RG");      //slim
		std_thinKinds.add("BCM");    //slim
		std_thinKinds.add("BPM");
		std_thinKinds.add("BLM");
		std_thinKinds.add("BSM");
		std_thinKinds.add("WS");
		std_thinKinds.add("Foil");
//		std_thinKinds.add("VIW");
//		std_thinKinds.add("Harp");
//		std_thinKinds.add("Tgt");
//		std_thinKinds.add("Marker");
                std_thinKinds.add("marker");
	}

	/**
	 * Create a new instance of LatticeFactory.
	 *@param thickKinds : a list of strings of thick (and slim) element kinds.
	 * <code>Example: ["DH","QH","QV","PQ","RG","BCM"].</code>
	 *@param thinKinds : a list of strings of thin (and slim) element kinds.
	 * <code>Example: ["DCH","DCV","RG","BCM","BPM","WS","Foil","VacuumWindow"].</code>
	 */
	public LatticeFactory(ArrayList thickKinds, ArrayList thinKinds) {
		this.thickKinds = thickKinds;
		this.thinKinds = thinKinds;
		this.debug = false;
		this.verbose = false;
		this.halfmag = true;
	}

	/**
	 * Create a new instance of LatticeFactory with default element kinds.
	 * Kinds of thick (and slim) elements default to 
	 * <code>["DH","QH","QV","PQ","SH","SV","RG","BCM"].</code>
	 * Kinds of thin (and slim) elements default to 
	 * <code>["DCH","DCV","RG","BCM","BPM","WS","Foil","VacuumWindow"].</code>
	 */
	public LatticeFactory() {
		this(std_thickKinds, std_thinKinds);
	}

	/**
	 * Concatenate two lattices and generate the Node-to-Element dictionary for the
	 * resulting combined lattice. The concatenated lattice will be made of clones from
	 * the lattices given as input variables.
	 * @param left lattice in front of 'right'.
	 * @param right lattice behind 'left'.
	 * @return the concatenation lattice  'left+right'.
	 */
	public static Lattice concatenate(Lattice left, Lattice right)
		throws LatticeError {
		//concatenate two lattices ...
		Lattice concat = Lattice.concatenate(left, right);
		//and generate the forward dictionary
		concat.setNode2ElementMapper(concat.makeNode2ElementMapper());
		return concat;
	}

	/**
	 * Return a lattice for an accelerator sequence.
	 *@param sequence the XAL combo sequence of the accelerator.
	 * @return The lattice.
	 */
	public Lattice getLattice(AcceleratorSeqCombo sequence)
		throws LatticeError {
		return getLattice((AcceleratorSeq) sequence);
	}

	/**
	 * Return a lattice for an accelerator sequence.
	 *@param sequence the XAL sequence of the accelerator.
	 * @return The lattice.
	 * @throws LatticeError
	 */
	public Lattice getLattice(AcceleratorSeq sequence) throws LatticeError {
		accelSeq = sequence;
		lattice = new Lattice(sequence.getId());
		lattice.setDebug(debug);
		lattice.setVerbose(verbose);
//		double seq_pos = sequence.getPosition();
		// calculate the total length of the (combo)sequence.
		double seq_len = calcTotalSeqLength(sequence);

		//process all thick (len>0) elements first
		processThickElements(sequence);

		//fill lattice up to end with drift space
		if (seq_len > lattice.getLength()) {
			double lend = seq_len - lattice.getLength();
			lattice.append(new Drift(seq_len - lend * 0.5, lend));
		}

		//process all thin and slim elements
		processThinElements(sequence);

		//make a consistency check
		try {
			lattice.forConsistency();
			if (debug) {
				cout.println("lattice passed consistency check: OK");
			}
		} catch (LatticeError lerr) {
			if (debug) {
				cout.println("lattice failed consistency check: NOT OK");
			}
			throw lerr;
		}

		//make the forward dictionary,i.e. [(key,value)=(node,element)]
		Node2ElementMapper mapper = lattice.makeNode2ElementMapper();
		// & deposit the accumulated map in the lattice object
		lattice.setNode2ElementMapper(mapper);
		if (debug) {
			Set entry_set = mapper.entrySet();
			Iterator set_iter = entry_set.iterator();
			while (set_iter.hasNext()) {
				Map.Entry map_entry = (Map.Entry) set_iter.next();
				AcceleratorNode node = (AcceleratorNode) map_entry.getKey();
				Element element = (Element) map_entry.getValue();
				cout.println(
					node.getId()
						+ ": ==mapped to==>\t"
						+ element.getType()
						+ ": "
						+ element.getName()
						+ ": s= "
						+ element.getPosition());
			}
		}

		return lattice;
	}

	/**
	 * Process all thick nodes. Filter them from 'sequence' and append them to
	 * the lattice.
	 *@param sequence the XAL accelerator sequence.
	 * @throws LatticeError
	 */
	private void processThickElements(AcceleratorSeq sequence)
		throws LatticeError {
		if (debug) {
			cout.println("processing THICK elements");
		}
		ArrayList<Object> allElements = new ArrayList<Object>();
		//walk the XAL object tree to get all nodes of a given kind
		nodesOfKind(sequence, thickKinds, allElements);
		//sort all Elements by their position
		sortElementsByPosition(allElements);
		//append all elements to the lattice
		// jdg: Note - this is where the PermMarker indicator for device
		// center must be added - but do not add any other thin elements
		// for some reason this craps out the lattice generattion
		for (ListIterator it = allElements.listIterator(); it.hasNext();) {
			Element next = (Element) it.next();
			if (next.isThick() ) {
				lattice.append(next);
			}
			else {
				if(next instanceof PermMarker) {
				lattice.insert(next);
				}				
			}
		}
	}

	/**
	 * Process all thin nodes. Filter them from 'sequence' and insert them into
	 * the lattice.
	 *@param sequence the XAL acclerator sequence.
	 * @throws LatticeError
	 */
	private void processThinElements(AcceleratorSeq sequence)
		throws LatticeError {
		if (debug) {
			cout.println("processing THIN elements");
		}
		ArrayList<Object> allElements = new ArrayList<Object>();
		//walk the XAL object tree to get all nodes of a given kind
		nodesOfKind(sequence, thinKinds, allElements);
		//sort all Elements by their position
		sortElementsByPosition(allElements);
		//insert all elements to the lattice
		for (ListIterator it = allElements.listIterator(); it.hasNext();) {
			Element next = (Element) it.next();
			if (!next.isThick()){
				lattice.insert(next);
			}
		}
	}

	/**
	 * Recursive walk through all accelerator sequences and subsequences, filtering
	 * nodes of kind listed in 'kind' and returning a list of lattice
	 * elements in 'result'.
	 *@param sequence the XAL accelerator sequence.
	 *@param kinds list of node types (kinds) to filter to go into the lattice.
	 *@param result the resulting list of lattice elements that passed the kind filter.
	 * @throws LatticeError
	 */
	private void nodesOfKind(
		AcceleratorSeq sequence,
		ArrayList kinds,
		ArrayList<Object> result)
		throws LatticeError {
		//        double position_base=sequence.getPosition();
		for (ListIterator itk = kinds.listIterator(); itk.hasNext();) {
			String kind = (String) itk.next();
			List nodes = sequence.getNodesOfType( kind, true );
			if (debug) {
				String out = sequence.getId() + ": " + kind + " [";
				for (int itn = 0; itn < nodes.size(); itn++) {
					AcceleratorNode node = (AcceleratorNode) nodes.get(itn);
					if (itn == nodes.size() - 1) {
						out += node.getId();
					} else {
						out += node.getId() + ",";
					}
				}
				out += "]";
				cout.println(out);
			}
			for (ListIterator itn = nodes.listIterator(); itn.hasNext();) {
				AcceleratorNode node = (AcceleratorNode) itn.next();
//				double n_pos = sequence.getPosition(node);
				nodeToElement(node, result);
			}
		}
		List subsequences = sequence.getSequences();
		if (!subsequences.isEmpty()) {
			for (ListIterator its = subsequences.listIterator();
				its.hasNext();
				) {
				AcceleratorSeq s = (AcceleratorSeq) its.next();
				nodesOfKind(s, kinds, result);
			}
		} else {
			return;
		}
	}

	/**
	 * <p>
	 * Convert an XAL accelerator node into lattice elements and add them to the
	 * 'result' list.
	 * </p>
	 * 
	 * <p>
	 * CKA NOTES:
	 * <br>- Casing by the <code>isKindOf()</code> method seems really klugey to
	 * me.  This is very brittle. The order of a case statement should not matter,
	 * here it is critical.
	 * <br>- The 100 line if-then-else statement is always suspicious. 
	 * </p>
	 * 
	 *@param node the XAL accelerator node.
	 *@param result the list to which the lattice elements are added.
	 * @throws LatticeError
	 */
	private void nodeToElement(AcceleratorNode node, ArrayList<Object> result)
		throws LatticeError {
		String name = node.getId();
//		String type = node.getType();
		double position = accelSeq.getPosition(node);
		double length = node.getLength();
		double effLength = 0.0;
		if (node instanceof Magnet) {
			if (node instanceof Bend) 
			  effLength = ((Bend) node).getDfltPathLength();
			else  
			  effLength = ((Magnet) node).getEffLength();
		}
		if (debug) {
			cout.println(
				"nodeToElement: "
					+ name
					+ " p= "
					+ position
					+ ", l= "
					+ length
					+ ", effl= "
					+ effLength);
		}
		
		//thick elements
		if (node.isKindOf("dh")) { //dipoles
			//we use only effective lengths for magnets
			//			length= ((Magnet) node).getEffLength();
			Element dipole = new Dipole(position, effLength, name);
			dipole.setAcceleratorNode(node);
			if (halfmag) {
				//split the magnet and put a permanent marker in its center
				PermMarker center =
					new PermMarker(position, 0.d, "ELEMENT_CENTER:" + name);
				center.setAcceleratorNode(node);
				ArrayList half = dipole.split(center);
				result.add(half.get(0));
				result.add(half.get(2));
				result.add(half.get(4));
			} else {
				result.add(dipole);
			}
			
		} else if (node.isKindOf("QSC"))  {       // Skew quadrupoles
		    
		    // Create the SLG representation of the skewed quadrupole
		    Element slgElem = new SkewQuad(position, effLength, name);
		    slgElem.setAcceleratorNode(node);

		    
		    // Now check our configuration
		    if (halfmag)  {   // configuration expressed as a conditional
		        // Let's repeat the same code for every different element type
		        PermMarker    slgMarkCtr = new PermMarker(position, 0.0d, "ELEMENT_CENTER:" + name);
		        slgMarkCtr.setAcceleratorNode(node);
		        
                ArrayList     lstElems = slgElem.split(slgMarkCtr);
                
                result.add(lstElems.get(0));
                result.add(lstElems.get(2));
                result.add(lstElems.get(4));
                
		    } else    {       // express other option of the the configuration
		        result.add(slgElem);
		        
		    }
		    
		    
		} else if (node.isKindOf("q") || node.isKindOf("qt")) { //quadrupoles
			//we use only effective lengths for magnets
			//			length= ((Magnet) node).getEffLength();
			Element quadrupole = new Quadrupole(position, effLength, name);
			quadrupole.setAcceleratorNode(node);
			if (halfmag) {
				//split the magnet and put a permanent marker in its center
				PermMarker center =
					new PermMarker(position, 0.d, "ELEMENT_CENTER:" + name);
				center.setAcceleratorNode(node);
				ArrayList half = quadrupole.split(center);
				result.add(half.get(0));
				result.add(half.get(2));
				result.add(half.get(4));
			} else {
				result.add(quadrupole);
			}
			
		} else if (node.isKindOf("pq")) { // permanent magnet quadrupoles
			//we use only effective lengths for magnets
			//			length= ((Magnet) node).getEffLength();
			Element quadrupole = new Quadrupole(position, effLength, name);
			quadrupole.setAcceleratorNode(node);
			if (halfmag) {
				//split the magnet and put a permanent marker in its center
				PermMarker center =
					new PermMarker(position, 0.d, "ELEMENT_CENTER:" + name);
				center.setAcceleratorNode(node);
				ArrayList half = quadrupole.split(center);
				result.add(half.get(0));
				result.add(half.get(2));
				result.add(half.get(4));
			} else {
				result.add(quadrupole);
			}
		} 
		else if (node.isKindOf("S")) {
			Element sextupole = new Sextupole(position, effLength, name);
			sextupole.setAcceleratorNode(node);
			if (halfmag) {
				//split the magnet and put a permanent marker in its center
				PermMarker center =
					new PermMarker(position, 0.d, "ELEMENT_CENTER:" + name);
				center.setAcceleratorNode(node);
				ArrayList half = sextupole.split(center);
				result.add(half.get(0));
				result.add(half.get(2));
				result.add(half.get(4));
			} else {
				result.add(sextupole);
			}
		//slim elements
		// rf gap	
		} 
		else if (node.isKindOf("SOL")) {
			//we use only effective lengths for magnets
			//			length= ((Magnet) node).getEffLength();
			Element solenoid = new Solenoid(position, effLength, name);
			solenoid.setAcceleratorNode(node);
			if (halfmag) {
				//split the magnet and put a permanent marker in its center
				PermMarker center =
					new PermMarker(position, 0.d, "ELEMENT_CENTER:" + name);
				center.setAcceleratorNode(node);
				ArrayList half = solenoid.split(center);
				result.add(half.get(0));
				result.add(half.get(2));
				result.add(half.get(4));
			} else {
				result.add(solenoid);
			}
			
		}
		
		else if (node.isKindOf("rfgap")) {
			RFGap rfgap = new RFGap(position, length, name);
			if (rfgap.isThick()) {
				for (ListIterator it = (rfgap.asTuple()).listIterator(); it.hasNext();) {
					Element e = (Element) it.next();
					e.setAcceleratorNode(node);
					result.add(e);
				}
			} else {
				rfgap.setAcceleratorNode(node);
				result.add(rfgap);
			}
		} else if (node.isKindOf("bcm")) {
			BCMonitor bcmonitor = new BCMonitor(position, length, name);
			if (bcmonitor.isThick()) {
				for (ListIterator it = (bcmonitor.asTuple()).listIterator(); it.hasNext();) {
					Element e = (Element) it.next();
					e.setAcceleratorNode(node);
					result.add(e);
				}
			} else {
				bcmonitor.setAcceleratorNode(node);
				result.add(bcmonitor);
			}
			//thin elements
		} else if (node.isKindOf("dch")) {
			Element hsteerer = new HSteerer(position, length, name);
			hsteerer.setAcceleratorNode(node);
			result.add(hsteerer);
		} else if (node.isKindOf("dcv")) {
			Element vsteerer = new VSteerer(position, length, name);
			vsteerer.setAcceleratorNode(node);
			result.add(vsteerer);
		} else if ( node.isKindOf( "EKick" ) ) {
			final Element eKicker = new EKicker( position, length, name );
			eKicker.setAcceleratorNode( node );
			result.add( eKicker );
		} 
		else if (node.isKindOf("bpm")) {
			Element bpm = new BPMonitor(position, length, name);
			bpm.setAcceleratorNode(node);
			result.add(bpm);
		} 
		else if ( node.isKindOf( "bsm" ) ) {
			final Element bsm = new BSMonitor( position, length, name );
			bsm.setAcceleratorNode( node );
			result.add(bsm);
		} 
		else if (node.isKindOf("blm")) {
			Element blm = new BLMonitor(position, length, name);
			blm.setAcceleratorNode(node);
			result.add(blm);
		} 
		else if (node.isKindOf("ws")) {
			Element wscanner = new WScanner(position, length, name);
			wscanner.setAcceleratorNode(node);
			result.add(wscanner);
		} 
		else if (node.isKindOf("marker")) {
			Element marker = new PermMarker(position, length, name);
			marker.setAcceleratorNode(node);
			result.add(marker);
		} 
		else {
			// treat everything else as markers
			Element marker = new PermMarker(position, length, name);
			marker.setAcceleratorNode(node);
			result.add(marker);
		}
	}

	/**
	 * Calculates the total length of sequence including subsequences.
	 * Calculation stops at subsequences that have a non zero length, i.e.
	 * it is assumed that a sequence with non zero length is already the
	 * sum of all its subnodes and subsequences.
	 *@param seq the top sequence of nested accelerator sequences or a combo sequence.
	 */
	private double calcTotalSeqLength(AcceleratorSeq seq) {
		double total = 0.d;
		double seql = seq.getLength();
		List subSeqs = seq.getSequences();
		if (!subSeqs.isEmpty() & seql == 0.d) {
			Iterator ssit = subSeqs.iterator();
			while (ssit.hasNext()) {
				AcceleratorSeq subseq = (AcceleratorSeq) ssit.next();
				total += calcTotalSeqLength(subseq);
			}
			return total;
		}
		return seql;
	}

	/**
	 * Sort lattice elements by their position.
	 * @param allElements the list of 
	 * (@link gov.sns.xal.slg.Element lattice elements} to be sorted. 
	 */
	private void sortElementsByPosition(ArrayList allElements) {
		Collections.sort(allElements, new Comparator() {
			/** Comparator for the sortElementsByPosition member function. */
			public int compare(Object obj1, Object obj2) {
				double p1 = ((Element) obj1).getPosition();
				double p2 = ((Element) obj2).getPosition();
				return Double.compare(p1, p2);
			}
		});
	}

	/**Set debugging output to stdout.
	 * @param debug <code>true</code> for output else <code>false</code>.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**Set debugging output to verbose.
	 * @param verbose <code>true</code> for verbose output else <code>false</code>.
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**Set flag to force lattice generator to place a permanant marker in the middle of every
	 * thick element.
	 * @param halfmag <code>true</code> yes put the middle marker (default),  else <code>false</code>
	 * for no middle markers. Note that the forward dictionary that maps from accelerator
	 * nodes to lattice elements needs these middle markers. Unless the dictionary is not
	 * needed the default setting should always be accepted.
	 */
	public void setHalfMag(boolean halfmag) {
		this.halfmag = halfmag;
	}

	/** Alias for method setHalfMag() */
	public void setHalfQuad(boolean halfquad) {
		setHalfMag(halfquad);
	}

	/**
	 * Return a version string w/o the cvs keyword (i.e. <dollar>Id<dollar>).
	 */
	public static String version() {
		String st = " ";
		char[] woId = new char[st.length()];
		int srcBegin = 5;
		int srcEnd = st.length() - 6;
		int srccnt = srcEnd - srcBegin;
		if (srccnt <= 0) {
			return "";
		}
		st.getChars(srcBegin, srcEnd, woId, 0);
		return new String(woId, 0, srccnt);
	}
}
