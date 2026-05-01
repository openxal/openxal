/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

/**
 *
 * @author sashazhukov
 */
public class DataPoint {
    private final double x;
    private final double y;
    private final double z;
    
    public DataPoint(double x, double y, double z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public double getZ(){
        return z;
    }
    
    public String toString(){
        return "{"+x+","+y+","+z+"}";
    }
    
}
