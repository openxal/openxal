/*
 * Detector.java
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
public class Detector {
    
    //double inphase;
    //double quadruture;
    double attenuate;   
    Signal det;
    
    /** Creates a new instance of Detector */
    public Detector() {        
        attenuate = 1.0;
    }
    
    /*    
    public void setinput(Signal cv) {
        det = new Signal(cv.getamp(), cv.getphase());
        det.setnoise(cv.getamp()*0.0005, 0.001);
        det.compute();
        //output = new Signal(input.getamp(), input.getphase());
        //output.compute();
        //inphase = output.getreal();
        //quadruture = output.getimage();        
    }
    */
    
    public Signal getout(Signal in) {
        det = new Signal(in.getamp()/attenuate, in.getphase());
        det.setnoise(0.001, 0.001);
        return det;
    }
    
    public void setatt(double at) {
        attenuate = at;
    }
}
