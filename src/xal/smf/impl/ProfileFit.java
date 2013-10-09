/*
 * ProfileFit.java
 *
 * Created on Sept. 25, 2002, 5:56 PM
 */

package xal.smf.impl;


/**
 *
 * This class is a container for fit information coming from the 
 * profile monitors.
 * @author  jdg
 * @version unknown
 */
public class ProfileFit{

    /**
     * Create a new <code>ProfileFit</code> object.
     *
     *
     * @since     Apr 20, 2009
     * @author    Christopher K. Allen
     */
    public ProfileFit()   { 
    }
        
    /** the mean position of a profile (mm) */
    private double mean;

    /** the standard deviation of a fitter gaussian to the profile (mm) */
    private double sigma;

    /** the amplitude at the mean value (AU)*/
    private double amp;

    /** the offset that is subtracted for background (AU) */
    private double offset;

    /** the slope used to subtract out any systematic error */
    private double slope;

   /** the integrated area under the profile */
    private double area;

    /* convenience methods */

    public double getMean() { return mean;}
    public void setMean(double m) { mean = m;}

    public double getSigma() { return sigma;}
    public void setSigma(double s) { sigma = s;}

    public double getArea() { return area;}
    public void setArea(double a) { area = a;}

    public double getAmp() { return amp;}
    public void  setAmp(double a) { amp = a;}

    public double getOffset() { return offset;}
    public void setOffset(double o) { offset = o;}

    public double getSlope() { return slope;}
    public void setSlope(double s) { slope = s;}
}
