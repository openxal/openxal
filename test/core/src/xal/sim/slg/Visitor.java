/*
 * Visitor.java
 *
 * Created on April 5, 2003, 11:12 PM
 */

package xal.sim.slg;

/**
 * The Visitor gets called by objects that implement the VisitorListener
 * interface and that have accepted to be visited. The implementor of
 * Visitor has to provide a visit() member function for each class (or
 * type) that implements VisitorListener. This gives the implementing
 * class the possibility to construct complex operations on a collection
 * of VisitorListeners.
 *
 * P.S.: I hope this explanation is clear and understandable *^&amp;^% (wdk).
 *
 * @author  wdklotz
 */
public interface Visitor {
    /** visit a BCMonitor lattice element */
    public void visit(BCMonitor e);
    /** visit a BPMonitor lattice element */
    public void visit(BPMonitor e);
    /** visit a BLMonitor lattice element */
    public void visit(BLMonitor e);
    /** visit a BSMonitor lattice element */
    public void visit(BSMonitor e);
    /** visit a Dipole lattice element */
    public void visit(Dipole e);
    /** visit a Drift lattice element */
    public void visit(Drift e);
    /** visit an Extraction Kicker lattice element */
    public void visit( EKicker e );
    /** visit a HSteerer lattice element */
    public void visit(HSteerer e);
    /** visit a Marker lattice element */
    public void visit(Marker e);
    /** visit a Octupole lattice element */
    public void visit(Octupole e);
    /** visit a PermMarker lattice element */
    public void visit(PermMarker e);
    /** visit a Quadrupole lattice element */
    public void visit(Quadrupole e);
    /** visit a RFGap lattice element */
    /** visit a Electrostatic Quadrupole lattice element */
    public void visit(EQuad e);
    /** visit a Electrostatic Dipole lattice element */
    public void visit(EDipole e);
    public void visit(RFGap e);
    /** visit a Sextupole lattice element */
    public void visit(Sextupole e);
    /** visit a SkewQuad lattice element */
    public void visit(SkewQuad e);
    /** visit a SkewSext lattice element */
    public void visit(SkewSext e);
    /** visit a VSteerer lattice element */
    public void visit(VSteerer e);
    /** visit a Quadrupole lattice element */
    public void visit(Solenoid e);
    /** visit a WScanner lattice element */
    public void visit(WScanner e);    
}
