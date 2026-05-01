/*
 * RFLoop.java
 *
 * Created on March 15, 2006, 5:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */

public class RFLoop {
    
    RFcavity cav;
    Detector detect;    
    Feedback feedback;
    Feedforward feedfwd;
    Amplifier ampl;
    
    /*
    double bandwidth;
    double centralfrequency;
    double signalfrequency;
    */
    //Coupler coup;    
    
    Signal gain;
    Signal loop;
    Signal cv;
        
/** Creates a new instance of RFLoop */
    public RFLoop() {
         
        cav = new RFcavity();
        detect = new Detector();
        feedback = new Feedback();
        feedfwd = new Feedforward();
        ampl = new Amplifier();             
        gain = new Signal(1.0, 0.0);        
        //coup = new Coupler();        
    } 
    
    public void setcalibration(double g, double att) {
        ampl.setamp(g);
        detect.setatt(att);
        
        // RF filter or narrow bandwidth
        
        //bandwidth = 1.0E5;
        //centralfrequency = 1.056E6;        
        //signalfrequency = Fourier spectrum or a specified frequency;
        //ql = centralfrequency/(2*bandwidth);
        //df = ql*(singalfrequency/centralfrequency - centralfrequency/signalfrequency); 
        //gain.reconstruct(1./(1.+df*df), -df/(1.+df*df));  
        
    }
        
    public Signal getloop() {        
        return loop; 
    }
    
    public Signal getcv() {
        return cv; 
    }
        
    public void loopcircle(Signal st, Signal er, 
                           Signal ep, Signal ei, 
                           Signal bm, double dt ) {
        
        loop = feedfwd.getout(feedback.getout(st,er), ep, ei);
        
        //coup.setinput(ampl.getout(loop));
        //cv = cav.getfield(coup.getout(), bm, dt);
        
        cv = cav.getfield(ampl.getout(loop), bm, dt);        
        loop = detect.getout(cv);
    }
    
    public void loopcircle(Signal st, Signal er,  
                           Signal bm, double dt ) {
        
        loop = feedback.getout(st, er);        
        cv = cav.getfield(ampl.getout(loop), bm, dt);        
        loop = detect.getout(cv);
    }

    public void loopcircle(Signal st, Signal bm, double dt ) {
        
        cv = cav.getfield(ampl.getout(st), bm, dt);        
        loop = detect.getout(cv);
    }
        
    public RFcavity getcav() {
        return cav;
    }
    
    public Feedback getfb() {
        return feedback;
    }
    
    public Feedforward getfwd() {
        return feedfwd;
    }
    
    public Detector getdetect() {
        return detect;
    }
        
    public Amplifier getampl() {
        return ampl;
    }
    
    /*
    public Coupler getcoup() {
        return coup;
    }
     */    
}
