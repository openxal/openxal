/*
 *
 * BSMonitor.java
 *
 * Created on February 2, 2010, 10:03 AM
 */

package xal.sim.slg;


/**
 * The bunch shape monitor (a slim element).
 * @author  tom pelaia
 */
public class BSMonitor extends ThinElement {
	private static final String type = "bunchshapemonitor";
	
	
	/** Primary Constructor */
	public BSMonitor( final double position, final double len, final String name ) {
		super( name, position, len );
		handleAsThick = false;
	}
	
	
	/** Creates a new instance of BSMonitor */
	public BSMonitor( final double position, final double len ) {
		this( position, len, "BSM" );
	}
	
	
	/** Creates a new instance of BSMonitor */
	public BSMonitor( final double position ) {
		this( position, 0.0 );
	}
	
	
	/** Creates a new instance of BSMonitor */
	public BSMonitor( final Double position, final Double len, final String name ) {
		this( position.doubleValue(), len.doubleValue(), name );
	}
	
	/** Creates a new instance of BSMonitor */
	public BSMonitor( final Double position, final Double len ) {
		this( position.doubleValue(), len.doubleValue() );
	}
	
	/** Creates a new instance of BSMonitor */
	public BSMonitor( final Double position ) {
		this( position.doubleValue() );
	}
	
	/**
	 * Return the element type.
	 */
	public String getType() {
		return type;
	}
	
	
	/**
	 * Returns a printable string of this element.
	 */
	public String toCoutString() {
		String retval = "";
		double el_pos = getPosition();
		double el_len = getEffLength();
		double a_start = toAbsolutePosition(getStartPosition());
		String name = getName();
		String type = getType();
		retval += "s="
		+ fmt.format(a_start)
		+ " m\t"
		+ name
		+ "\t"
		+ type
		+ " p="
		+ fmt.format(el_pos)
		+ " leff="
		+ fmt.format(el_len);
		return retval;
	}
	
	
	/**
	 * When called with a Visitor reference the implementor can either
	 * reject to be visited (empty method body) or call the Visitor by
	 * passing its own object reference.
	 *
	 *@param v the Visitor which wants to visit this object.
	 */
	public void accept( final Visitor v ) {
		v.visit( this );
	}
	
}