//
//  EKicker.java
//  xal
//
//  Created by Tom Pelaia on 1/9/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.sim.slg;


/** vertical extraction kicker as a thin element */
public class EKicker extends ThinElement {
	/** extraction kicker type */
    private static final String type = "EKick";
    
	
    /** Creates a new instance of EKicker */
    public EKicker( final double position, final double len, final String name ) {
        super( name, position, 0.0 );
		handleAsThick = false;
    }
    
	
    /** Creates a new instance of EKicker */
    public EKicker( final double position, final double len ) {
        this( position, len, "EKick" );
    }
    
	
    /** Creates a new instance of EKicker */
    public EKicker( final double position ) {
        this( position, 0.0 );
    }
    
	
    /** Creates a new instance of EKicker */
    public EKicker( final Double position, final Double len, final String name ) {
        this( position.doubleValue(), len.doubleValue(), name );
    }
    
	
    /** Creates a new instance of EKicker */
    public EKicker( final Double position, final Double len ) {
        this( position.doubleValue(), len.doubleValue() );
    }
    
	
    /** Creates a new instance of EKicker */
    public EKicker( final Double position ) {
        this( position.doubleValue() );
    }
    
	
    /** Return the element type. */
    public String getType() {
        return type;
    } 

    
    /**
	 * When called with a Visitor reference the implementor can either reject to be visited (empty method body) or call the Visitor by passing its own object reference.
     * @param v the Visitor which wants to visit this object.
     */
    public void accept( final Visitor v ) {
        v.visit( this );
    }
}
