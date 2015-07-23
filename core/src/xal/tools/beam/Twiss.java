/*
 * Twiss.java
 *
 * Created on November 12, 2002, 5:44 PM
 */

package xal.tools.beam;


import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import xal.tools.math.r2.R2;



/**
 * <p>
 *  Convenience class for dealing with Courant-Snyder (or Twiss) parameters.  These
 *  parameters represent an ellipse in phase space given by
 *  <br>
 *  <br> 
 *  &nbsp; &nbsp; &gamma;<i>x</i><sup>2</sup> + 2&alpha;<i>xx'</i> + &beta;<i>x</i>'<sup>2</sup> = &epsilon;
 *  <br>
 *  <br>
 *  where &alpha;, &beta;, &gamma;, and &epsilon; are the Courant-Snyder parameters and (<i>x,x'</i>) are
 *  coordinates on the horizontal phase plane.  (There are analogous equations for the other phase planes.)
 *  Recall that the Courant-Snyder parameters are not independent but related by the
 *  fact
 *  <br>
 *  <br>
 *  &nbsp; &nbsp;  &beta;&gamma; - &alpha;<sup>2</sup> = 1
 *</p>
 *
 * @author  Christopher K. Allen
 * @since   Nov 12, 2002
 * @version Sep 25, 2014
 */
public class Twiss implements java.io.Serializable {
    
    
    /*
     *  Global Constants
     */
    
    /**
     * Serialization version identifier
     */
    private static final long serialVersionUID = 1L;

    
    /*
     * Inner Classes
     */
    
    /**
     * Enumeration of the Courant-Snyder parameters used.  Intended for
     * expediting display of these parameters. 
     *
     * @author Christopher K. Allen
     * @since  Oct 2, 2014
     */
    public enum PROP {
        
        /** The Courant-Snyder alpha parameter */
        ALPHA("alpha", "getAlpha"),
        
        /** The Courant-Snyder beta parameter */
        BETA("beta", "getBeta"),
        
        /** The Courant-Snyder emittance */
        EMIT("emittance", "getEmittance");
        
        /**
         * Returns the label of the property in the data structure
         * which corresponds to this enumeration constant.
         *
         * @return  property label
         * 
         * @since  Nov 13, 2009
         * @author Christopher K. Allen
         */
        public String       getPropertyLabel() {
            return this.strFldLbl;
        }

        /**
         * Using reflection, we return the value of the field that this
         * enumeration constant represents, within the given data structure.
         *
         * @param data      <code>Twiss</code> data structure having field corresponding to this constant
         * 
         * @return          value of the given data structure's field 
         * 
         * @since  Apr 22, 2010
         * @author Christopher K. Allen
         */
        public double       getPropertyValue(Twiss data) {

            try {
                Method      mthFldGtr = this.mthFldGtr;
                double      dblFldVal = (Double) mthFldGtr.invoke(data);

                return dblFldVal;

            } catch (SecurityException e) {
                System.err.println("SERIOUS ERROR: Twiss$PROP#getPropertyValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (IllegalArgumentException e) {
                System.err.println("SERIOUS ERROR: Twiss$PROP#getPropertyValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (IllegalAccessException e) {
                System.err.println("SERIOUS ERROR: Twiss$PROP#getPropertyValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (InvocationTargetException e) {
                System.err.println("SERIOUS ERROR: Twiss$PROP#getPropertyValue()"); //$NON-NLS-1$
                e.printStackTrace();

            }

            return 0.0;
        }

        
        
        /** The property label */
        private final String        strFldLbl;
        
        /** name of the field in the data structure */
        private Method              mthFldGtr;

        
        /** 
         * Create the property enumeration constant with given label.
         * 
         * @param strLabel     label for the signal property 
         * @param strFldGtr    the name of the getter method for the field corresponding to this enumeration constant
         */
        private PROP(String strLabel, String strFldGtr) {
            this.strFldLbl = strLabel;
            this.mthFldGtr = null;
            
            try {
                this.mthFldGtr = Twiss.class.getMethod( strFldGtr );
                
            } catch (SecurityException e) {
                System.err.println("SERIOUS ERROR: Twiss$PROP#PROP() - getter inaccessible: " + strFldGtr); //$NON-NLS-1$
                e.printStackTrace();

            } catch (NoSuchMethodException e) {
                System.err.println("SERIOUS ERROR: Twiss$PROP#PROP() no getter method " + strFldGtr); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
    }

        
    /*
     *  Global Operations
     */
    
    /**
     * Creates a new <code>Twiss</code> object initialized by the given set of 
     * central, second-order moments of the beam in whatever phase plane.  Note that the 
     * RMS emittance &epsilon; is given by 
     * [&lt;<i>x</i><sup>2</sup>&gt;&lt;<i>x'</i><sup>2</sup>&gt; - &lt;<i>xx'</i>&gt;<sup>2</sup>]<sup>1/2</sup>For example,
     * in the horizontal phase plane the parameters are given as
     *
     * @param dblMmtSigX    the second moment &lt;<i>x</i><sup>2</sup>&gt;
     * @param dblMmtCov     the second moment &lt;<i>xx'</i>&gt;
     * @param dblMmtSigXp   the second moment &lt;<i>x'</i><sup>2</sup>&gt;
     * 
     * @return  the Courant-Snyder parameters (&alpha;=-&lt;<i>xx'</i>&gt;/&epsilon;,
     *                                         &beta;=&lt;<i>x</i><sup>2</sup>&gt;/&epsilon;,
     *                                         &epsilon;)
     *
     * @author Christopher K. Allen
     * @since  Aug 29, 2012
     */
    public static Twiss createFromMoments(double dblMmtSigX, double dblMmtCov, double dblMmtSigXp) {
        double dblDet  = dblMmtSigX*dblMmtSigXp - dblMmtCov*dblMmtCov;
        double dblEmit = Math.sqrt( dblDet );
        
        double dblBeta  =  dblMmtSigX / dblEmit;
        double dblAlpha = -dblMmtCov / dblEmit;

        return new Twiss(dblAlpha, dblBeta, dblEmit);
    }
    
    /**
     * <p>
     * Create a new set of <code>Twiss</code> parameters from the given covariance matrix.
     * The covariance matrix is simply the second-order moments packaged up as a symmetric
     * matrix.  The form of this matrix <b>&sigma;</b> is
     * <br>
     * <pre>
     * &nbsp; &nbsp; <b>&sigma;</b> &cong; | &lt;x<sup>2</sup>&gt; &lt;xx'&gt; |
     *         | &lt;xx'&gt; &lt;x'<sup>2</sup>&gt; |
     * </pre> 
     *
     * @param arrCov    symmetric matrix array of second-order moments
     * 
     * @return          Courant-Snyder parameters corresponding to the given covariance matrix
     *
     * @author Christopher K. Allen
     * @since  Aug 29, 2012
     */
    public static Twiss createFromCovarianceMatrix(double[][] arrCov) {
        double dblMmtSigX  = arrCov[0][0];
        double dblMmtCov   = arrCov[1][0];
        double dblMmtSigXp = arrCov[1][1];
        
        return createFromMoments(dblMmtSigX, dblMmtCov, dblMmtSigXp);
    }
    
    /**
     * <p>
     * Creates a new <code>Twiss</code> object given the parameters of the equivalent
     * uniform beam.  This is the uniform beam that has the same second-moments as the
     * beam under study.  Because of the uniform charge distribution it can be modeled
     * as a KV (Kapchinsky-Vladimirsky) beam having a distinct envelope size (radius) and a
     * distinct envelope slope (this value is related to the divergence angle).
     * </p>
     * <h3>NOTE</h3>
     * <p>
     * &middot; <em>We do not scale the emittance!</em>  It is common practice to multiply the
     * RMS emittance by 4 for 2D beams and other values for 3D beams with various distributions.
     * We assume the same second moments which yield the same RMS emittance.  Specially, the
     * emittance of the returned object IS THE RMS EMITTANCE.
     * </p>
     *
     * @param dblEnvRad     envelope size <i>X</i> of the uniform beam
     * @param dblEnvSlp     slope divergence angle <i>X'</i> of the uniform beam
     * @param dblEmit       emittance of the beam - This value is not scaled!
     * 
     * @return          Courant-Snyder parameters corresponding to the given equivalent 
     *                  uniform beam parameters
     *
     * @author Christopher K. Allen
     * @since  Aug 29, 2012
     */
    public static Twiss createFromEquivalentBeam(double dblEnvRad, double dblEnvSlp, double dblEmit) {
        double dblAlpha  = -dblEnvRad*dblEnvSlp/dblEmit;
        double dblBeta   = dblEnvRad*dblEnvRad/dblEmit;
        @SuppressWarnings("unused")
        double dblGamma  = (1.0 + dblAlpha*dblAlpha)/dblBeta;
   
        return new Twiss(dblAlpha, dblBeta, dblEmit);
    }
    

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
     *  Return the beam emittance
     */
    public double getEmittance()    { return m_dblEmitt; };
    
    /**
     *  Return the envelope radius extent
     */
    public double getEnvelopeRadius()   { return m_dblEnvRad; };
    
    /**
     *  Return the envelope slope 
     */
    public double getEnvelopeSlope()    { return m_dblEnvSlp; };
    
    
    /**
     * <pre>
     *  Return the Twiss matrix associated with these Twiss parameters.
     *  This matrix has the form
     *
     *      S = | gamma  alpha |
     *          | alpha  beta  |
     *
     *  so that the equation of the phase space ellipse is given by
     *
     *      (x,x')*S*(x,x') = emittance
     * </pre>
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
     * <pre>
     *  Computes and returns the semi-axes of the phase space ellipse
     *  represented by the Twiss parameters.  
     *
     *  NOTE:
     *  Since the ellipse may be rotated these values do not necessarily
     *  correspond to any particular values of x, and x' in the phase plane.
     * </pre>
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
     * <pre>
     *  Compute and return the eigenvalues of the Twiss matrix.  This matrix has
     *  the form
     *
     *          | gamma  alpha |
     *          | alpha  beta  |
     *          
     * </pre>
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
     * <pre>
     *  Compute and return the eigenvectors of the Twiss matrix.  This matrix has
     *  the form
     *
     *          | gamma  alpha |
     *          | alpha  beta  |
     *          
     * </pre>
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
