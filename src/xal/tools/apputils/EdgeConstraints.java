/*
 * EdgeConstraint.java
 *
 * Created on February 12, 2003, 2:01 PM
 */

package xal.tools.apputils;

/** 
 * Edge Constraint defines the strut and growth constrains for a component.  The struts are measured 
 * from the specified edge of the component to the specified edge of the container.  For example, 
 * the right strut is the distance between the right edge of the component and the right edge of 
 * the container.
 */
public class EdgeConstraints implements Cloneable {
    public int growBehavior;    // how the component should grow
    public int strutBehavior;   // how the 
    public int topStrut, leftStrut, bottomStrut, rightStrut;    // struts from each edge
    
    /** Empty Constructor */
    public EdgeConstraints() {
    }
    
    
    /** Constructor with all settings */
    public EdgeConstraints(int aTopStrut, int aLeftStrut, int aBottomStrut, int aRightStrut, int aStrutBehavior, int aGrowBehavior) {
        this.topStrut = aTopStrut;
        this.leftStrut = aLeftStrut;
        this.bottomStrut = aBottomStrut;
        this.rightStrut = aRightStrut;
        this.strutBehavior = aStrutBehavior;
        this.growBehavior = aGrowBehavior;
    }
    
    
    /** Implement toString() to print summary of constraint */
    public String toString() {
        return "grow behavior: " + growBehavior + ", strut behavior: " + strutBehavior + 
        ", top strut: " + topStrut + ", left strut: " + leftStrut + 
        ", bottom strut: " + bottomStrut + ", right strut: " + rightStrut;
    }
}