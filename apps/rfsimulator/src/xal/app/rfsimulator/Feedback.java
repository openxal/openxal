/*
 * Feedback.java
 *
 * Created on March 15, 2006, 2:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */

public class Feedback {
    
    double gain;
    //Signal erri = new Signal();
    Signal errp;
    Signal setpoint;    
    
    /** Creates a new instance of Feedback */
    public Feedback() {
        gain   = 1.0;
    }
    
    public void setgain(double g) {
        gain   = g;        
    }
    /*
    public void setmode( boolean b) {
        closed = b;
    }
    */
    public Signal getout(Signal st, Signal ep) {
        
        //erri  = new Signal(ei.getamp(), ei.getphase());
        errp  = new Signal(ep);
        setpoint = new Signal(st);
        errp.multiply(gain);
        setpoint.plus(errp);
        
        return setpoint;        
    }
/*    
    public Signal getcontrol() {
                
        if (closed) {                     
            // erri.multiply(0.);
            // errp.plus(erri);            
            errp.multiply(gain);
            setpoint.plus(errp);
        }
        
        return setpoint;
    }
 */
}
