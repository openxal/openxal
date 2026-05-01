/*
 * Amplifier.java
 *
 * Created on March 15, 2006, 2:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */
public class Amplifier {
    
    //Signal input  = new Signal();
    
    Signal output = new Signal();     
    double amp;
    double top;
    double bottom;
    //double phasecoef;
            
    /** Creates a new instance of Amplifier */
    public Amplifier() {
        
        amp = 1.0;        
        // upper output limit - 200 A
        top = 200.0;
        // base - 0.01 A        
        bottom = 0.01;        
        //phasecoef = 0.0;
    }
       
    public void setamp(double a) {
        amp = a;
    }
        
    public void setlimit(double u, double b) {
        top = u;
        bottom = b;
    }
    
    // None linear amp and phase 
    /*    
    public void setampcoef(double a0, double a1, double a2) {
        amp = amp*(a0 + a1*input.getamp() + a2*input.getamp()*input.getamp());
    }
    
    public void setphasecoef(double a0, double a1, double a2) {
        phasecoef = a0 + a1*input.getamp() + a2*input.getamp()*input.getamp();
    }        
    */
    
    // Assume a perfect amplifier     
    public Signal getout(Signal in) {
                
        if (in.getamp() < bottom)
            return new Signal();
        
        if (in.getamp()*amp > top ) {
            output = new Signal(top, in.getphase());
        }
        
        else {
            output = new Signal(in.getamp()*amp, in.getphase());
        }             
        output.setnoise(0.001, 0.001);
        return output;
            
        // An assumed not so perfect amp
        /*    
        if (input.getamp()*amp < top) {
            output = new Signal(input.getamp()*amp, 
                                input.getphase() + phasecoef));
        }
        
        else if (input.getamp()*amp > 2.*top) {
            output = new Signal(0., 0.);
        }
        
        else {            
            output = new Signal(top, 
            input.getphase()-2.*Math.asin(0.5*(input.getamp()-top)/top));
        }
        */       
    }    
}
