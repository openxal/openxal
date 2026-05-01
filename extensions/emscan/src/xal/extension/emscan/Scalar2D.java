/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

/**
 *
 * @author sashazhukov
 */
public class Scalar2D extends DataRecord2D {
    public Scalar2D(long millis,double[] data){
        super(millis,data);
    }
    public Scalar2D(long millis, double x, double y, double value){
        this(millis,new double[]{x,y,value});
    }
    public double getValue(){
        return getValue(0);
    }
    
}
