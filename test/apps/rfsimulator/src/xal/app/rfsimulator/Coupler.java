/*
 * Coupler.java
 *
 * Created on March 15, 2006, 2:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */
public class Coupler {
    
    Signal fwd;
    Signal rfl;
    
    Signal input;
    Signal output;
    
    /** Creates a new instance of Coupler */
    public Coupler() {
    }
    
    public void setinput(Signal in) {
        input = new Signal(in.getamp(), in.getphase());
        //input.setnoise(in.getamp()*0.0005, 0.001);
        //input.compute();
        
        output = new Signal(input.getamp(), input.getphase());        
        fwd = new Signal(0.01*input.getamp(), input.getphase() + Math.PI);

    }   
    
    public void setrfl(Signal in) {
        rfl = new Signal(0.01*in.getamp(), in.getphase() + Math.PI);        
    }
    
    public Signal getout() {
        return output;
    }
    
    public Signal getfwd() {
        return fwd;
    }
    
    public Signal getrfl() {
        return rfl;
    }
}
