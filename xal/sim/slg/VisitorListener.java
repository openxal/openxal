/*
 * VisitorListener.java
 *
 * Created on April 5, 2003, 11:02 PM
 */

package xal.sim.slg;

/**
 * Objects implementing VisitorListener can be called to accept a Visitor.
 *
 * @author  wdklotz
 */
public interface VisitorListener {
    
    /**
     * When called with a Visitor reference the implementor can either
     * reject to be visited (empty method body) or call the Visitor by
     * passing its own object reference.
     *
     *@param v the Visitor which wants to visit this object.
     */
    public void accept(Visitor v);
    
}
