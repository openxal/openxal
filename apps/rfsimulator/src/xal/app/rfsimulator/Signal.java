/*
 * Signal.java
 *
 * Created on March 15, 2006, 2:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */

public class Signal {
    
    // If loop and controller bandwidth concerned, should have f 
    // double frequency;
    
    double amplitude;
    double phase;
     
    double quadrature;
    double inphase;
    //double noisep;
    //double noisea;
    
    /** Creates a new instance of Signal */
    public Signal() {       
        amplitude = 0.;
        phase = 0.;        
        quadrature = 0.;
        inphase = 0.;
    }
    
    public Signal(Signal a) {
        amplitude = a.getamp();
        phase = a.getphase();       
    }
        
    public Signal(double a, double p) {
        amplitude = a;
        phase = p;
        //noisep = 0.;
        //noisea = 0.;                
    } 
    
    public void setnoise(double na, double np) {
        amplitude = (1. + na*(1. - 2.*Math.random()))*amplitude;
        phase = phase + np*(1. - 2.*Math.random());
    }
    
    public void compute() {        
        inphase = amplitude*Math.cos(phase);
        quadrature =amplitude*Math.sin(phase);
    } 
    
    public double getamp() {
        return amplitude;        
    }
    
    public double getphase() {
        return phase;        
    }    
    
    public double getreal() {
        return inphase;        
    }
    
    public double getimage() {
        return quadrature;        
    }
    
    private void tophase(double x, double y) {
        
        if (Math.abs(x) < 1.0E-15) {            
           if (y > 1.0E-15 )               
               phase = 0.5*Math.PI;          
           else if (y < -1.0E-15 )
               phase =-0.5*Math.PI;           
           else
               phase = 0.0;
        }
        
        else 
        {  
            phase = Math.atan(y/x);            
            if (x < 0.0 )
                phase = phase + Math.PI;
        }        
        rott(0.0);       
    }
    
    public void reconstruct(double rl, double img) {
        inphase = rl;
        quadrature = img;                
        amplitude = Math.sqrt(rl*rl + img*img);
        tophase(rl, img);
    }
    //copy phasor
    public void plus(Signal p1){
        
        double ax, ay;
        ax = amplitude*Math.cos(phase) + p1.getamp()*Math.cos(p1.getphase());
        ay = amplitude*Math.sin(phase) + p1.getamp()*Math.sin(p1.getphase());
        amplitude = Math.sqrt(ax*ax+ay*ay);
        tophase(ax, ay);
    }
    
    public void multiply(double am) {
        amplitude = am*amplitude;
    }
    
    public void times(Signal s) {
        amplitude = amplitude*s.getamp();
        phase = phase + s.getphase();        
        rott(0.);
    }
    
    public void minus(Signal p1){
        Signal p2 = new Signal(p1.getamp(), Math.PI + p1.getphase());
        plus(p2);
    }
    
    public void rott(double ph){
        phase = phase + ph;                
        while (phase > Math.PI)
            phase = phase - 2.0*Math.PI;        
        while (phase <= -Math.PI)
            phase = phase + 2.0*Math.PI;   
    }
    
    public void damp(double dc){
        amplitude = amplitude*Math.exp(-dc);
    }
}
