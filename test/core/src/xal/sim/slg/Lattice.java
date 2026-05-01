/*
 * Lattice.java
 *
 * Created on March 16, 2003, 8:58 PM
 */

package xal.sim.slg;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.*;
import org.w3c.dom.*;
/**
 * The lattice is a linear sequence of elements. Each element is associated
 * with one and only one XAL AcceleratorNode object.
 *
 * @author  wdklotz
 */
public class Lattice implements Cloneable {
	private String name; 																//the lattice name
	private double base;															//the base position
	private List<Element> elements; 													//the element list
	private PrintStream cout; 														//console
	public static final double EPS; 											//precision limit for position calculations
	private boolean debug; 														// print flag
	private boolean verbose; 													// print flag
	public static NumberFormat fmt; 										//number formater
	private Node2ElementMapper node2ElementMap; 				//dictionary [(key,value)=(node,element)]

	static {
		fmt= NumberFormat.getNumberInstance();
		((DecimalFormat) fmt).applyPattern("0.000000");
		EPS= 1.e-5d;
	}

	/** Creates an empty lattice with a 'name' and a 'base' position.
	 * @param name the lattice name.
	 * @param base the lattice base position.
	 *  */
	public Lattice(String name, double base) {
		this.name= name;
		this.base= base;
		elements= new ArrayList<Element>();
		cout= System.out;
		debug= false;
		verbose= false;
		//an empty lattice
		elements.add(new PermMarker(0.d, 0.d, "BEGIN_" + name));
		elements.add(new Marker(0.d));
		elements.add(new PermMarker(0.d, 0.d, "END_" + name));
		//all elements get same offset
		updateBases();
	}

	/** Creates an empty lattice with a 'name' and a 'base' position */
	public Lattice(String name, Double base) {
		this(name, base.doubleValue());
	}

	/** Creates an empty lattice with a 'name' and a 'base=0' position */
	public Lattice(String name) {
		this(name, 0.d);
	}

	/**
	 * Return the lattice base position.
	 */
	public double getBase() {
		return base;
	}

	/**
	 * Set the lattice base position.
	 */
	public void setBase(double base) {
		this.base= base;
		updateBases();
	}

/**Clone a lattice. The cloned lattice is a deep copied clone of the original.*/
	public Object clone() {
		try {
			Lattice cloned= (Lattice) super.clone();
			//make a deep copy of the list of lattice elements
			cloned.elements= new ArrayList<Element>();
			LatticeIterator liter= latticeIterator();
			while (liter.hasNext()) {
				Element elm= (Element) liter.next().clone();
				cloned.elements.add(elm);
			}
			return cloned;
		} catch (CloneNotSupportedException e) {
			//cannot happen -- we support clone
			throw new InternalError(e.toString());
		}
	}
	
	/**
	 * Concatenate two lattices. The resulting lattice will be a lattice with right added to the end of
	 * left. 
	 * @return the combined lattice from left to right.
	 * */
	static Lattice concatenate(Lattice left, Lattice right) throws LatticeError {
		Lattice part1 = (Lattice) left.clone();
		Lattice part2 = (Lattice) right.clone();
		part2.setBase(part1.getLength());    //shift position base of second part
		Lattice part3 = new Lattice(part1.getName()+"+"+part2.getName(),0d);
		part3.clearMarkers(); //remove dummy makers
		int last = part3.len()-1;
		part3.elements.addAll(last,part1.elements);    //add all from 1st part
		last = part3.len()-1;
		part3.elements.addAll(last,part2.elements);  //add all from 2nd part
		last = part3.len()-1;
		//update position of END marker
		(part3.elements.get(last)).setPosition(part1.getLength()+part2.getLength());
		return part3;
	}

	/**
	 * Return the lattice length in number of elements.
	 */
	public int len() {
		return elements.size();
	}

	/**
	 * Get element at 'index' from lattice.
	 */
	public Element getItem(int index) {
		return elements.get(index);
	}

	/**
	 * Return the lattice length in distance units.
	 */
	public double getLength() {
		return getItem(len() - 1).getEndPosition();
	}

	/**
	 * Set the lattice name property.
	 */
	public void setName(java.lang.String name) {
		this.name= name;
	}

	/**
	 * Return the name property.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for the forward dictionary property: [(key,value)=(node,element)]
	 */
	public void setNode2ElementMapper(Node2ElementMapper node2ElementMap) {
		this.node2ElementMap= node2ElementMap;
	}

	/**
	 * Getter for the forward dictionary property: [(key,value)=(node,element)]
	 */
	public Node2ElementMapper getNode2ElementMapper() {
		if (node2ElementMap == null) {
			//make & deposit the accumulated map in the lattice object
			setNode2ElementMapper(makeNode2ElementMapper());
		}
		return node2ElementMap;
	}

	/**
	 *  Make a new forward dictionary: [(key,value)=(node,element)].
	 * @return The forward dictionary that maps accelerator nodes to lattice elements.
	 */
	public Node2ElementMapper makeNode2ElementMapper() {
		//generate the forward dictionary,i.e. [(key,value)=(node,element)]
		Node2ElementMapper mapper= new Node2ElementMapper();
		LatticeIterator liter= latticeIterator();
		while (liter.hasNext()) {
			// visit every lattice element
			liter.next().accept((Visitor) mapper);
		}
		return mapper;
	}

	/**
	 * Append an element at the end of the lattice.
	 */
	void append(Element element) throws LatticeError {
		if (debug) {
			cout.println(
				"append: " + element.getName() + ", pos= " + element.getPosition() + ", len= " + element.getLength());
		}
		Element lattice_end= elements.remove(len() - 1);
		Element last= getItem(len() - 1);
		double start_pos= element.getStartPosition();
		//is there space to fill up with drift space ?
		double drift_len= start_pos - last.getPosition();
		if (drift_len < -EPS) {
			//ooops! negative length: severe error ...
			String message=
				"negative length when appending: " + element.getName() + ": calculated length= " + drift_len;
			message += "\nlast: " + last.getName() + ": pos= " + last.getPosition() + ", len= " + last.getLength();
			throw new LatticeError(message);
		} else if (Math.abs(drift_len) < EPS) {
			/*too short drift: ingnore*/;
		} else {
			//add an upstream drift space
			double drift_pos= last.getPosition() + drift_len * 0.5;
			elements.add(new Drift(drift_pos, drift_len));
			elements.add(new Marker(start_pos));
		}
		//add the element
		elements.add(element);
		double end_pos= element.getEndPosition();
		elements.add(new Marker(end_pos));
		//place lattice 'END' marker
		elements.add(new PermMarker(end_pos, 0.0, lattice_end.getName()));
		//all elements get same offset
		updateBases();
	}

	/**
	 * Append a tuple to the lattice end.
	 */
	void appendTuple( final List<Element> tuple ) throws LatticeError {
		for ( final Element element : tuple ) {
			append( element );
		}
	}

	/**
	 * Insert a thin element (length=0) into the lattice. Thick elements
	 * at the same position will be sliced accordingly.
	 */
	void insert(Element element) throws LatticeError {
		if (element.getLength() != 0.0) {
			throw new LatticeError("length must be zero when inserting: " + element.getName());
		}
		//assemble a list of indices of marker elements
		//exclude BEGIN and END markers
		int[] markers= new int[3 * len()];
		int jx= 0;
		for (int ix= 1; ix < len() - 1; ix++) {
			if (getItem(ix).getType().equals("marker")) {
				markers[jx++]= ix;
			}
		}
		//        if(false) {
		//            for(int ix=0; ix<jx; ix++) {
		//                cout.println("makers["+ix+"]="+markers[ix]+": p="+getItem(markers[ix]).getPosition());
		//            }
		//        }
		//search for markers that embrace the element
		int before= markers[0];
		int after= markers[jx - 1];
		for (int m= 0; m < jx; m++) {
			if (getItem(markers[m]).getPosition() <= element.getPosition()) {
				before= markers[m];
				continue;
			}
			after= markers[m];
			break;
		}
		//slice the element between the two markers
		int between= after - 1;
		//        if(false) {cout.println("(before,between,after)=("+before+", "+between+", "+after+")");}
		Element to_split= elements.remove(between);
		final List<Element> to_insert= to_split.split(element);
		elements.addAll(between, to_insert);
		if (debug & verbose) {
			cout.println(
				"insert: replacing "
					+ to_split.getName()
					+ ", p= "
					+ to_split.getPosition()
					+ ", l= "
					+ to_split.getLength()
					+ " with");
			ListIterator<Element> lit= to_insert.listIterator();
			while (lit.hasNext()) {
				Element el= lit.next();
				cout.println("\t" + el.getName() + ", p= " + el.getPosition() + ", l= " + el.getLength());
			}
		}
		//all elements get same offset
		updateBases();
	}

	/**
	 * Remove all non permanent marker elements from the lattice.
	 */
	public void clearMarkers() {
		for (LatticeIterator lit= latticeIterator(); lit.hasNext();) {
			if (lit.next().getType().equals("marker")) {
				lit.remove();
			}
		}
	}

	/**
	 * All elements get the same offset.
	 */
	private void updateBases() {
		for (LatticeIterator lit= latticeIterator(); lit.hasNext();) {
			lit.next().setBase(base);
		}
	}

	/**
	 * Join neighbouring drift spaces into a single one.
	 */
	public void joinDrifts() {
		ArrayList<Element> lattice= new ArrayList<Element>();
		//pass1
		Element end= getItem(len() - 1);
		int ix= 0;
		while (ix < len() - 1) {
			Element el= getItem(ix);
			ix += 1;
			if (el.getType().equals("drift")) {
				Element elnext= getItem(ix);
				if (elnext.getType().equals("drift")) {
					ix += 1;
					double len1= el.getLength();
					double len2= elnext.getLength();
					double spos1= el.getStartPosition();
					double spos2= elnext.getStartPosition();
					double jlen= len1 + len2;
					double jpos= spos1 + jlen * 0.5;
					double jspos= jpos - jlen * 0.5;
					lattice.add(new Drift(jpos, jlen));
					if (debug) {
						cout.println(
							"join drifts: ("
								+ spos1
								+ ","
								+ len1
								+ ") + ("
								+ spos2
								+ ","
								+ len2
								+ ") = ("
								+ jspos
								+ ","
								+ jlen
								+ ")");
					}
				} else {
					lattice.add(el);
				}
			} else {
				lattice.add(el);
			}
		}
		lattice.add(end);
		elements= lattice;

		//pass 2
		//check if there are still touching drifts in the lattice,
		//if so call this function recursively until all done
		ix= 0;
		while (ix < len() - 1) {
			Element el= getItem(ix);
			ix += 1;
			if (el.getType().equals("drift")) {
				Element elnext= getItem(ix);
				if (elnext.getType().equals("drift")) {
					//repeat the joining process
					joinDrifts();
					return;
				}
			}
		}
	}

	//	public static Lattice concatenate(Lattice first, Lattice second) {
	//		Lattice result = new Lattice(first.getName()+"+"+second.getName());
	//		result.appendTuple(first.elements);
	//		result.appendTuple(second.elements);
	//		return result;
	//	}

	/**
	 * Make a printed output of the lattice.
	 */
	void toConsole() {
		for (LatticeIterator lit= latticeIterator(); lit.hasNext();) {
			cout.println(lit.next().toCoutString());
		}
		cout.println("Totals: length of " + getName() + " = " + getLength() + " m, with " + len() + " elements.\n");
	}

	/**
	 * A consistency check.
	 */
	void forConsistency() throws LatticeError {
		for (int ix= 0; ix < len() - 1; ix++) {
			Element el= getItem(ix);
			Element next= getItem(ix + 1);
			double el_pos= el.getPosition();
			double el_len= el.getLength();
			double next_pos= next.getPosition();
			double next_len= next.getLength();
			if (Math.abs(el_pos + (el_len + next_len) * 0.5 - next_pos) > EPS) {
				//ooops! this should never happen, continue debugging.
				throw new LatticeError("consistency check failed at about " + el.getName() + " and " + next.getName());
			}
		}
	}

	/*
	 * Debugging output.
	 */
	void setDebug(boolean debug) {
		this.debug= debug;
	}

	/*
	 * Verbose output
	 */
	void setVerbose(boolean verbose) {
		this.verbose= verbose;
	}

	/**
	 * Return a version string w/o the cvs keyword (i.e. $Id$).
	 */
	public static String version() {
		String st= "";
		char[] woId= new char[st.length()];
		int srcBegin= 5;
		int srcEnd= st.length() - 6;
		int srccnt= srcEnd - srcBegin;
		if (srccnt <= 0) {
			return "";
		}
		st.getChars(srcBegin, srcEnd, woId, 0);
		return new String(woId, 0, srccnt);
	}

	/**
	 * Return an iterator for the Lattice.
	 */
	public LatticeIterator latticeIterator() {
		return new LIterator();
	}

	/**
	 * Implementation of iterator support for a lattice.
	 */
	private class LIterator implements LatticeIterator {
		private ListIterator<Element> liter;

		private LIterator() {
			liter = elements.listIterator();
		}

		public boolean hasNext() {
			return liter.hasNext();
		}

		public Element next() {
			return liter.next();
		}

		public void remove() {
			liter.remove();
		}
	}

/**Return the lattice as a DOM document object. */
	public Document getLatticeAsDocument() {
		return new LatticeSynchronizer(this).getDocument();
	}

} ////////////////////// Lattice
