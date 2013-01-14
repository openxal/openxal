/*
 * Twiss.java
 *
 * Created on November 12, 2002, 5:44 PM
 */

package xal.tools.beam;


import java.io.PrintWriter;

import  xal.tools.math.r2.R2;



/**
 *  <p>
 *  Convenience class for dealing with Courant-Snyder (or Twiss) parameters.  These
 *  parameters represent an ellipse in phase space given by
 *  <br/>
 *  <br/>
 *  &nbsp; &nbsp; <i>&gamma;x</i><sup>2</sup> + 2<i>&alpha;xx'</i> + <i>&beta;x'</i><sup>2</sup> = &epsilon;
 *  <br/>
 *  <br/>
 *  Recall that these parameters are related by the fact that
 *  <br/>
 *  <br/>
 *  &nbsp; &nbsp; &beta;&gamma; - &alpha;<sup>2</sup> = 1
 *
 * @author  Christopher K. Allen
 */
public class Twiss implements java.io.Serializable {
    
    
    /*
     *  Global Operations
     */
    
    /**
     * Serialization version identifier
     */
    private static final long serialVersionUID = 1L;

    
    /*
     *  Local Attributes
     */
    
    /** Courant-Snyder alpha parameter */
    private double m_dblAlpha = 0.0;
    
    /** Courant-Snyder beta parameter */
    private double m_dblBeta = 0.0;
    
    /** Courant-Snyder gamma parameter */
    private double m_dblGamma = 0.0;
    
    /** beam emittance */
    private double m_dblEmitt = 0.0;
    
    
    /** envelope radius corresponding to twiss parameters */
    private double m_dblEnvRad = 0.0;
    
    /** envelope slope corresponding to twiss parameters */
    private double m_dblEnvSlp = 0.0;
    
    
    
    /*
     *  Initialization
     */
    
    /** 
     *  Creates a new, uninitialized, instance of Twiss 
     */
    public Twiss() {
    }
    
    /**
     * Copy constructor:  creates a <b>deep</b> copy of the argument.
     * 
     * @param   twiss   twiss object to be copied.
     */
    public Twiss(Twiss twiss)   {
        this(twiss.getAlpha(),twiss.getBeta(),twiss.getEmittance());
    }
    
    /** 
     *  Creates a new instance of Twiss initialized to the given
     *  Twiss parameters.
     *
     *  @param  dblAlpha    coefficient of 2*x*x'
     *  @param  dblBeta     coefficient of x'^2
     *  @param  dblEmitt    magnitude^2 of the ellipse (beam emittance)
     */
    public Twiss(double dblAlpha, double dblBeta, double dblEmitt) {
        this.setTwiss(dblAlpha, dblBeta, dblEmitt);
    }
    
    /**
     *  Sets the values of the Twiss parameters directly.
     *
     *  @param  dblAlpha    alpha parameter (phase plane coupling)
     *  @param  dblBeta     beta parameter (the envelope magnitude)
     *  @param  dblEmitt    beam emittance (phase space area)
     */
    public void setTwiss(double dblAlpha, double dblBeta, double dblEmitt) {
        m_dblAlpha = dblAlpha;
        m_dblBeta  = dblBeta;
        m_dblEmitt = dblEmitt;

        this.m_dblGamma  = (1.0 + m_dblAlpha*m_dblAlpha)/m_dblBeta;
        this.m_dblEnvRad = Math.sqrt(dblBeta*dblEmitt);
        this.m_dblEnvSlp = -dblAlpha*Math.sqrt(dblEmitt/dblBeta);
    }
    
    /**
     *  Set the values of the Twiss parameters from the corresponding phase
     *  space envelope values.
     *
     *  @param  dblEnvRad   envelope radius
     *  @param  dblEnvSlp   envelope slope
     *  @param  dblEmitt    beam emittance
     */
    public void setEnvelope(double dblEnvRad, double dblEnvSlp, double dblEmitt) {
        this.m_dblEnvRad = dblEnvRad;
        this.m_dblEnvSlp = dblEnvSlp;
        this.m_dblEmitt  = dblEmitt;
        
        this.m_dblAlpha  = -dblEnvRad*dblEnvSlp/dblEmitt;
        this.m_dblBeta   = dblEnvRad*dblEnvRad/dblEmitt;
        this.m_dblGamma  = (1.0 + m_dblAlpha*m_dblAlpha)/m_dblBeta;
    }
    
    
    /*
     *  Data Query
     */
    
    /**
     *  Return the alpha Twiss parameter
     */
    public double getAlpha()    { return m_dblAlpha; };
    
    /**
     *  Return the beta Twiss parameter
     */
    public double getBeta()     { return m_dblBeta; };
    
    /**
     *  Return the gamma Twiss parameter
     */
    public double getGamma()    { return m_dblGamma; };
    
    /**
     *  Return the envelope radius extent
     */
    public double getEnvelopeRadius()   { return m_dblEnvRad; };
    
    /**
     *  Return the envelope slope 
     */
    public double getEnvelopeSlope()    { return m_dblEnvSlp; };
    
    /**
     *  Return the beam emittance
     */
    public double getEmittance()    { return m_dblEmitt; };
    
    
    /**
     *  Return the Twiss matrix associated with these Twiss parameters.
     *  This matrix has the form
     *
     *      S = | gamma  alpha |
     *          | alpha  beta  |
     *
     *  so that the equation of the phase space ellipse is given by
     *
     *      (x,x')*S*(x,x') = emittance
     *  
     *  @return     2x2 Twiss matrix
     */
    public double[][]   twissMatrix()   {
        double[][]      arrTwiss = new double[2][2];
        
        arrTwiss[0][0] = this.getGamma();
        arrTwiss[0][1] = this.getAlpha();
        arrTwiss[1][0] = this.getAlpha();
        arrTwiss[1][1] = this.getBeta();
        
        return arrTwiss;
    }
    
    /**
     *  Return the correlation matrix associated with these Twiss parameters.
     *
     *  @return     2x2 phase space correlation matrix
     */
    public double[][]   correlationMatrix()   {
        double[][]      arrCorr = new double[2][2];
        
        arrCorr[0][0] = this.getBeta()*this.getEmittance();
        arrCorr[0][1] = -this.getAlpha()*this.getEmittance();
        arrCorr[1][0] = -this.getAlpha()*this.getEmittance();
        arrCorr[1][1] = this.getGamma()*this.getEmittance();
        
        return arrCorr;
    }
    
    
    /**
     *  Compute the phase space ellipse's rotation from upright.
     *
     *  @return     phase space rotation (<b>radians</b>)
     */
    public double computeRotation() {
        double        fAlpha;             // alpha twiss parameter
        double        fDescr;             // descriminate of Twiss quadratic
        double        fZeta;              // gamma - beta
        double        fTheta;             // rotation angle

        fAlpha  = this.getAlpha();
        fZeta   = this.getGamma() - this.getBeta();
        fDescr  = 4.0*fAlpha*fAlpha + fZeta*fZeta;

        fTheta  = Math.atan2(2.0*fAlpha, fZeta + Math.sqrt(fDescr));

        return fTheta;
    }

    /**
     *  Computes and returns the semi-axes of the phase space ellipse
     *  represented by the Twiss parameters.  
     *
     *  NOTE:
     *  Since the ellipse may be rotated these values do not necessarily
     *  correspond to any particular values of x, and x' in the phase plane.
     *
     *  @return     two-dimension array of semi-axes (a,b)
     */
    public double[] computeSemiAxes()   {
        double        fEmitt;             // emittance Twiss parameter
        double        fLambda1;           // eigenvalue of Twiss matrix
        double        fLambda2;           // eigenvalue of Twiss matrix
        double[]      arrEigVals;         // eigenvalues of Twiss matrix
        double[]      arrSemiAxes;        // the semi-axes of ellipse

        
        arrEigVals  = this.computeEigenvalues();
        fEmitt      = this.getEmittance();
        
        fLambda1 = arrEigVals[0];
        fLambda2 = arrEigVals[1];

        arrSemiAxes = arrEigVals;
        arrSemiAxes[0] = Math.sqrt(fEmitt/fLambda1);
        arrSemiAxes[1] = Math.sqrt(fEmitt/fLambda2);

        return arrSemiAxes; 
    }


    /**
     *  Compute and return the eigenvalues of the Twiss matrix.  This matrix has
     *  the form
     *
     *          | gamma  alpha |
     *          | alpha  beta  |
     *
     *  @return     eigenvalues of the above matrix
     */
    public double[] computeEigenvalues()  {
        double        fAlpha;             // alpha Twiss parameter
        double        fTemp1;             // gamma + beta
        double        fTemp2;             // gamma - beta
        double        fDescr;             // descriminate of Twiss quadratic
        double[]      arrEigVals;         // eigenvalues of the Twiss matrix

        
        arrEigVals = new double[2];
        
        fAlpha  = this.getAlpha();
        fTemp1  = this.getGamma() + this.getBeta(); 
        fTemp2  = this.getGamma() - this.getBeta(); 
        fDescr  = 4.0*fAlpha*fAlpha + fTemp2*fTemp2;

        arrEigVals[0] = 0.5*(fTemp1 - Math.sqrt(fDescr));
        arrEigVals[1] = 0.5*(fTemp1 + Math.sqrt(fDescr));

        return arrEigVals;
    }

    /**
     *  Compute and return the eigenvectors of the Twiss matrix.  This matrix has
     *  the form
     *
     *          | gamma  alpha |
     *          | alpha  beta  |
     *
     *  @return     two-element array of eigenvectors of the above matrix
     */
    public R2[] computeEigenvectors()   {
        double        fAlpha;             // alpha twiss parameter
        double        fZeta;              // gamma - beta
        double        fDescr;             // descriminate of Twiss quadratic
        R2            vecMaj;             // major semi-axis eigenvector
        R2            vecMin;             // minor semi-axis eigenvector

        vecMaj  = new R2();
        vecMin  = new R2();
        fAlpha  = this.getAlpha();
        fZeta   = this.getGamma() - this.getBeta(); 
        fDescr  = 4.0*fAlpha*fAlpha + fZeta*fZeta;

        if  (fAlpha != 0.0)    {
            vecMaj.set1( fZeta + Math.sqrt(fDescr) );
            vecMaj.set2( 2.0*fAlpha );
            vecMaj = vecMaj.times( 1.0/vecMaj.norm2() );

            vecMin.set1( fZeta - Math.sqrt(fDescr) );
            vecMin.set2( 2.0*fAlpha );
            vecMin = vecMin.times( 1.0/vecMin.norm2() );
        } else {
            vecMaj.set1( 0.0 );
            vecMaj.set2( 1.0 );

            vecMin.set1( 1.0 );
            vecMin.set2( 0.0 );
        }

    return new R2[] {vecMaj, vecMin};
    }
    
    
    
    /*
     * Testing and Debugging
     */
     
    /**
     *  Print out contents of the Twiss object.
     * 
     *  @param  pw      PrintWriter object to receive contents 
     */
    public void printOn(PrintWriter pw) {
        pw.println("alpha: " + getAlpha());
        pw.println("beta: " + getBeta());
        pw.println("gamma: " + getGamma());
        pw.println("emittance: " + getEmittance());
    }
    
    
    /**
     * Get the twiss parameters as a string
     */
    @Override
    public String toString() {
        return "alpha: " + m_dblAlpha + ", beta: " + m_dblBeta + ", emittance: " + m_dblEmitt;
    }

}
