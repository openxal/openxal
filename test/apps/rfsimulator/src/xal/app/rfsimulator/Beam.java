/*
 * Beam.java
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
public class Beam {
    
    protected double mc2 = 938.27;    
    double energy = 1000.0;
    double beta = 0.875025954;
    double gamma = 2.0657913;
    double current = 0.0;
    double de = 0.;
    double dp = 0.;
        
    /** Creates a new instance of Beam */
    public Beam() {
    }
    
    public Beam(double e, double c) {
        if (e > 800.)
            energy  = e;
        if (c > 0.00001)
            current = c;
        
        gamma = 1. + energy/mc2;
        beta = Math.sqrt(1. - 1./gamma/gamma);                
    }
    
    public void inject(double d, double p) {
        de = d*(1.-2.*Math.random());
        dp = p*(1.-2.*Math.random());
    }
    
    public void transport(double d, double p) {
        de = de + d;
        dp = dp + p;
    }
    
    public double getenergy() {
        return energy;
    }
            
    public double getde() {
        return de;
    }
    
    public double getdp() {
        return dp;
    }
    
    // Within-turn beam profile slices    
    public double getcurrent(double t, double w) {
        if (current < 0.00001)
            return 0.0;
        
        if (w < 2.5E-8)
            return current;
        
        if (t <= 0.25*w && t >= -0.25*w) {
            return 1.3333*current;
        }
        
        if (t <= 0.5*w && t > 0.25*w ) {
            return 2.6667*current*(1.0 - 2.*t/w);
        }
        
        if (t >= -0.5*w && t < -0.25*w) {
            return 2.6667*current*(1.0 + 2.*t/w);            
        }        
        
        return 0.0;
    }
    
    // Simple turn-by-turn beam profiles
    /*
    public double getcurrent(double f, double w) {
        
        if (w < 0) 
            return current;
            
        if (f > 999.)
            freq = f;
         
        if (w > 1.0E-7)
            width = w;
        
        double om = 2.*freq*Math.PI;    
                        
        int m = 2000;       
        double ts = -0.5*width;
        double dt = 0.0005*width;
        
        double sp = 0.25*width;
        
        double g  = 0.0;
        double fou= 0.0;
        double cal= 0.0;
       
        for (int i=0; i<m+1; i++) {
            ts = ts + dt;
            
            if (ts < -sp) {
                g = dt*2.6666667*(1.0 + 0.5*ts/sp);
            }
            
            else if (ts > sp) {
                g = dt*2.6666667*(1.0 - 0.5*ts/sp);
            }
            
            else {
                g = dt*1.3333333;
            }
            
            fou= fou + g*Math.cos(om*ts);
            cal= cal + g;        
        }
        
        return Math.abs(current*fou/cal); 
    }
    */
    
    public double getbeta() {        
        return beta;
    }
    
    public double getgamma() {
        return gamma;
    }
}
