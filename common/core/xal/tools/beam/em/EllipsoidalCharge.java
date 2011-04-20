/*
 * Created on Sep 15, 2003
 * Modified:
 *      9/03    - CKA: added rotation into ellipsoid coordinates
 *     11/03    - CKA: added full space charge matrix generation capabilities
 *
 */
package xal.tools.beam.em;


import xal.tools.beam.CorrelationMatrix;
import xal.tools.beam.PhaseMatrix;
import  xal.tools.math.EllipticIntegral;
import xal.tools.math.r3.R3;
import xal.tools.math.r3.R3x3;


/**
 * Encapsulates the properties of a ellipsoidally symmetric distribution
 * of charge in 3D space, in particular, the electromagnetic properties.
 * Provides convenience methods for creating arbitrarily oriented ellipsoids
 * and determining their fields, and their effects on beam particles, 
 * specifically, in the form of a linear transfer matrix generator.
 *  
 * @author Christopher K. Allen
 * 
 * @deprecated  replaced with gov.sns.tools.beam.em.BeamEllipsoid
 */

@Deprecated
public class EllipsoidalCharge {


    /*
     * Global Constants
     */
    
    /** distribution constant, we're using that for a uniform distribution */
    public static final double     FCONST = 2.0*Math.pow(5.0, 1.5);


    /*
     * Local Attributes
     */
     
    /** generalized beam perveance */
    private double      m_dblK = 0.0;
    
    /** semi-axis of the ellipsoid in x direction */
    private double      m_dblSemiX = 0.0;

    /** semi-axis of the ellipsoid in y direction */
    private double      m_dblSemiY = 0.0;

    /** semi-axis of the ellipsoid in z direction */
    private double      m_dblSemiZ = 0.0;
    
    /** displacement vector for the ellipsoid centroid from the coordinate origin */
    private R3          m_vecDispl = new R3(0.0, 0.0, 0.0);
     
    /** rotation matrix in S0(3) that aligns ellipsoid to standard coordinates */
    private R3x3        m_matRot = R3x3.identity();


    /*
     *  Initialization
     */
     
    /**
     * Default Constructor.  Creates a new, empty <code>EllipsoidalCharge</code> object
     */
    public EllipsoidalCharge()  {
        
    }
     
    /**
     * Constructs an ellipsoidal charge from its semi-axes values and the generalized
     * beam perveance.  The ellipsoid is assumed 
     * to be centered on the coordinate origin and aligned to the coordinate
     * axes.  Thus, the reference ellipsoid is described by the equation
     * 
     *      (x/a)^2 + (y/b)^2 + (z/c)^2 = 1
     * 
     * where a,b,c are the semi-axes values on the x,y,z coordinate directions,
     * respectively.
     * 
     * @param   K   generalized beam perveance 
     * @param   a   ellipsoid semi-axis in x direction
     * @param   b   ellipsoid semi-axis in y direction
     * @param   c   ellipsoid semi-axis in z direction
     *
     *  @see    #setBeamPerveance
     *  @see    #setSemiAxes  
     */     
    public EllipsoidalCharge(double K, double a, double b, double c)  {
        this.setBeamPerveance(K);
        this.setSemiAxes(a, b, c);
    }
     
     
    /**
     * Construct an ellipsoidal charge from its covariance matrix and the generalized
     * beam perveance.  The reference elllipsoid
     * is represented by the equation
     * 
     *      r'*matSigmaInv*r=1
     * 
     * where matSigmaInv is the inverse of the 3x3 matrix argument matSigma, 
     * r=(x y z) is the position vector in R3, and the prime indicates 
     * transposition.  Note that matSigma must be symmetric and 
     * positive definite.  Thus, it is diagonalizable with the
     * decomposition
     * 
     *      matSigma = R*D*R'
     * 
     * where the prime indicates transposition, R in SO(3) is the orthogonal 
     * rotation matrix and D is the diagonal
     * matrix of real eigenvalues.  Note from the above that these eigenvalues 
     * are the squares of the ellipsoid semi-axes.
     *      
     * @param   K   generalized beam perveance 
     * @param  matSigma    covariance matrix for ellipsoid
     * 
     * @exception  IllegalArgumentException    matrix is not symmetric and/or positive definite
     * 
     * @see    #setBeamPerveance
     * @see    #configureFromCovariance
     */
    public EllipsoidalCharge(double K, R3x3 matSigma)
        throws IllegalArgumentException
    {
//        this.checkSymmetry(matSigma);
        this.setBeamPerveance(K);
        this.configureFromCovariance(matSigma);            
    }
   
    /**
     * Construct an ellipsoid charge from its covariance matrix, displacement 
     * vector and generalized beam perveance.  
     * The reference elllipsoid is represented by the equation
     * 
     *      (r-vecDispl)'*matSigmaInv*(r-vecDispl) = 1
     * 
     * where matSigmaInv is the inverse of the 3x3 matrix argument matSigma, 
     * r=(x y z) is the position vector in R3, and the prime indicates
     * tansposition.  Note that matSigma must be symmetric and positive 
     * definite.  Thus, it is diagonalizable with the decomposition
     * 
     *      matSigma = R*D*R'
     * 
     * where the prime indicates transposition, R in SO(3) is the orthogonal 
     * rotation matrix and D is the diagonal
     * matrix of real eigenvalues.  Note from the above that these eigenvalues 
     * are the squares of the ellipsoid semi-axes.
     * 
     *  @param   K   generalized beam perveance 
     *  @param  matSigma    covariance matrix for ellipsoid
     *  @param  vecDispl    displacement vector from the coordinate origin
     * 
     *  @exception  IllegalArgumentException    matrix is not symmetric and/or positive definite
     * 
     *  @see    EllipsoidalCharge#setBeamPerveance
     *  @see    #setDisplacement
     *  @see    #configureFromCovariance
     */     
    public EllipsoidalCharge(double K, R3x3 matSigma, R3 vecDispl)
        throws IllegalArgumentException
    {
//        this.checkSymmetry(matSigma);
        this.setBeamPerveance(K);
        this.setDisplacement(vecDispl);
        this.configureFromCovariance(matSigma);
    }


    /** 
     * Construct a beam charge density ellipsoid described
     * by the phase space correlation matrix <code>matChi</code> and generalized
     * beam perveance K.
     * 
     * Note that the phase space correlation matrix in homogeneous coordinates
     * contains all moments up to and including second order; this includes the
     * first-order moments that describe the displacement of the ellipsoid from
     * the coordinate origin.  Thus, from the 7x7 phase space correlation matrix
     * we extract the displacement vector
     * 
     *      (&lt;x&gt;,&lt;y&gt;&lt;z&gt;)
     * 
     * and the 3x3 covariance matrix
     * 
     *      | &lt;x*x&gt; &lt;x*y&gt; &lt;x*z&gt; |    | &lt;x&gt;*&lt;x&gt; &lt;x&gt;*&lt;y&gt; &lt;x&gt;*&lt;z&gt; |
     *      | &lt;y*x&gt; &lt;y*y&gt; &lt;y*z&gt; | -  | &lt;y&gt;*&lt;x&gt; &lt;y&gt;*&lt;y&gt; &lt;y&gt;*&lt;z&gt; |
     *      | &lt;z*x&gt; &lt;z*y&gt; &lt;z*z&gt; |    | &lt;z&gt;*&lt;x&gt; &lt;z&gt;*&lt;y&gt; &lt;z&gt;*&lt;z&gt; |
     * 
     * to construct the ellipsoidal charge object according to the previous constructor
     * parameters.
     * 
     *  @param   K   generalized beam perveance 
     *  @param  matChi  envelope correlation matrix in homogeneous phase space coordinates
     * 
     *  @author Christopher K. Allen
     * 
     *  @see    #setBeamPerveance
     *  @see    #configureFromCorrelation
     */
    public EllipsoidalCharge(double K, CorrelationMatrix matChi)    {
        
        // Build the displacement vector
        double  xm = matChi.getMeanX();
        double  ym = matChi.getMeanY();
        double  zm = matChi.getMeanZ();
        
        R3      vecDispl = new R3(xm, ym, zm);
        
        
        // Build the configuration matrix
        double  covXX = matChi.computeCovXX();
        double  covXY = matChi.computeCovXY();
        double  covYY = matChi.computeCovYY();
        double  covYZ = matChi.computeCovYZ();
        double  covZZ = matChi.computeCovZZ();
        double  covXZ = matChi.computeCovXZ();
        
        R3x3    matTau = new R3x3();
        
        matTau.setElem(0,0, covXX);  matTau.setElem(0,1, covXY);  matTau.setElem(0,2, covXZ);
        matTau.setElem(1,0, covXY);  matTau.setElem(1,1, covYY);  matTau.setElem(1,2, covYZ);
        matTau.setElem(2,0, covXZ);  matTau.setElem(2,1, covYZ);  matTau.setElem(2,2, covZZ);


        this.setBeamPerveance(K);
        this.configureFromCovariance(matTau);
        this.setDisplacement(vecDispl);
    };
    
    
    /**
     * Configure the ellipsoidal charge from the given covariance matrix.
     * The reference ellipsoid is then described by the equation
     * 
     *      r'*matSigInv*r = 1
     * 
     * where r=(x y z) is the position vector in R3, matSigInv is the
     * inverse of covariance matrix argument, and the prime indicates
     * transposition.
     * 
     * Consider ellipsoidally symmetric distributions described by
     * the current reference ellipsoid, that is distributions F of 
     * the form
     * 
     *      F(x,y,z) = F(r'*matSigmaInv*r).
     * 
     * Then the second-order central moments, or covariances, of
     * the ellipsoid are exactly the elements of the covariance matrix.
     * That is
     * 
     *      &lt;(ri-&lt;ri&gr;)*(rj-&lt;rj&gt;)&gt; = C*matSigma_ij
     * 
     * where C is some constant that depends upon the distribution
     * profile.
     * 
     * The covariance matrix is decomposed into its eigensystem of
     * eigenvalues and orthogonal rotation.  Note that matSigma must 
     * be symmetric and positive definite.  Thus, it is diagonalizable with the
     * decomposition
     * 
     *      matSigma = R*D*R'
     * 
     * where R in SO(3) is the orthogonal rotation matrix and D is the diagonal
     * matrix of real eigenvalues.  Note from the above that these eigenvalues 
     * are the squares of the ellipsoid semi-axes.  This identifies
     * the ellipsoid uniquely (and clearly).  Note that the eigenvalues
     * in this case are the squares of the semi-axes. 
     * 
     * 
     * @param matSigma  the covariance matrix of the ellipsoid
     * 
     * @throws IllegalArgumentException     the covariance matrix is not symmetric 
     *                                      and/or positive definite
     * 
     * @see #setRotation
     * @see #setSemiAxes
     */
    public void configureFromCovariance(R3x3 matSigma)
        throws IllegalArgumentException
    {    
        
        /*
         * NOTE:
         * The Jama eigen decomposition is too noisey and causes
         * inaccurate dynamics.  The decomposition must be done by us 
         * to arbitrary precision.
         */
//        R3x3EigenDecomposition sysEigen = matSigma.eigenDecomposition();
//        
//        double[]    arrEigvals = sysEigen.getRealEigenvalues();
//        double      a = Math.sqrt(arrEigvals[0]);
//        double      b = Math.sqrt(arrEigvals[1]);
//        double      c = Math.sqrt(arrEigvals[2]);
//
//        this.setRotation(sysEigen.getConjugationFactor());        
//        this.setSemiAxes(a, b, c);


        /* 
         * TEMPORARY BUG FIX
         * 
         * This only works when ellipsoid is rotation in one plane only!
         */ 
        // Tuning parameters
        final double     dblTolSmall = 0.001;
        
        R3x3   matRot = R3x3.identity();
        
        double  dblAng;     // rotation angle
        double  dblSin;     // sine of rotation angle
        double  dblCos;     // cosine of rotation angle
        
        double corXX = matSigma.getElem(0,0);
        double corYY = matSigma.getElem(1,1);
        double corZZ = matSigma.getElem(2,2); 
        
        // x-y plane rotation
        double corXY = matSigma.getElem(0,1);
        if ( corXY/Math.sqrt(corXX*corYY) > dblTolSmall)    {
            dblAng = 0.5*Math.atan(2.0*corXY/(corXX - corYY));
            dblSin = Math.sin(dblAng);
            dblCos = Math.cos(dblAng);
            
            R3x3 matRz = R3x3.zero();   // z is primary axis
            matRz.setElem(0,0, dblCos);
            matRz.setElem(0,1, dblSin);
            matRz.setElem(1,0, -dblSin);
            matRz.setElem(1,1, dblCos);
            matRz.setElem(2,2, 1.0);
            
            matSigma = matSigma.conjugateTrans(matRz);
            matRot   = matRz.times(matRot);
            
        } 
        
        // x-z plane rotation
        double corXZ = matSigma.getElem(0,2);
        if (corXZ/Math.sqrt(corXX*corZZ) > dblTolSmall) {
            dblAng = 0.5*Math.atan(2.0*corXZ/(corXX - corZZ));
            dblSin = Math.sin(dblAng);
            dblCos = Math.cos(dblAng);
            
            R3x3 matRy = R3x3.zero();   // y is primary axis
            matRy.setElem(0,0, dblCos);
            matRy.setElem(0,2, dblSin);
            matRy.setElem(2,0, -dblSin);
            matRy.setElem(2,2, dblCos);
            matRy.setElem(1,1, 1.0);
            
            matSigma = matSigma.conjugateTrans(matRy);
            matRot   = matRy.times(matRot);
        }
        
        // y-z plane rotation
        double corYZ = matSigma.getElem(1,2);
        if (corYZ/Math.sqrt(corYY*corZZ) > dblTolSmall) {
            dblAng = 0.5*Math.atan(2.0*corYZ/(corYY - corZZ));
            dblSin = Math.sin(dblAng);
            dblCos = Math.cos(dblAng);
            
            R3x3 matRx = R3x3.zero();   // x is primary axis
            matRx.setElem(1,1, dblCos);
            matRx.setElem(1,2, dblSin);
            matRx.setElem(2,1, -dblSin);
            matRx.setElem(2,2, dblCos);
            matRx.setElem(0,0, 1.0);
            
            matSigma = matSigma.conjugateTrans(matRx);
            matRot   = matRx.times(matRot);
        }

        // We should be diagonal now under the assumed conditions
        double      a = Math.sqrt(matSigma.getElem(0,0));
        double      b = Math.sqrt(matSigma.getElem(1,1));
        double      c = Math.sqrt(matSigma.getElem(2,2));

        this.setRotation(matRot.transpose());        
        this.setSemiAxes(a, b, c);
    }

    /**
     * Configure the ellipsoidal charge from the given <code>CorrelationMatrix</code>.
     * 
     * Note that the phase space correlation matrix in homogeneous coordinates
     * contains all moments up to and including second order; this includes the
     * first-order moments that describe the displacement of the ellipsoid from
     * the coordinate origin.  Thus, from the 7x7 phase space correlation matrix
     * we extract the displacement vector
     * 
     *      (&lt;x&gt;,&lt;y&gt;&lt;z&gt;)
     * 
     * and the 3x3 covariance matrix
     * 
     *      | &lt;x*x&gt; &lt;x*y&gt; &lt;x*z&gt; |    | &lt;x&gt;*&lt;x&gt; &lt;x&gt;*&lt;y&gt; &lt;x&gt;*&lt;z&gt; |
     *      | &lt;y*x&gt; &lt;y*y&gt; &lt;y*z&gt; | -  | &lt;y&gt;*&lt;x&gt; &lt;y&gt;*&lt;y&gt; &lt;y&gt;*&lt;z&gt; |
     *      | &lt;z*x&gt; &lt;z*y&gt; &lt;z*z&gt; |    | &lt;z&gt;*&lt;x&gt; &lt;z&gt;*&lt;y&gt; &lt;z&gt;*&lt;z&gt; |
     * 
     * to construct the ellipsoidal charge object according to the previous constructor
     * parameters.
     * 
     * 
     * @param matChi    phase space correlation matrix (in homogeneous coordinates) for the ellipsoidal charge
     */
    public void configureFromCorrelation(CorrelationMatrix matChi)    {
        
        // Build the displacement vector
        double  xm = matChi.getMeanX();
        double  ym = matChi.getMeanY();
        double  zm = matChi.getMeanZ();
        
        R3      vecDispl = new R3(xm, ym, zm);
        
        
        // Build the configuration matrix
        double  covXX = matChi.computeCovXX();
        double  covXY = matChi.computeCovXY();
        double  covYY = matChi.computeCovYY();
        double  covYZ = matChi.computeCovYZ();
        double  covZZ = matChi.computeCovZZ();
        double  covXZ = matChi.computeCovXZ();
        
        R3x3    matTau = new R3x3();
        
        matTau.setElem(0,0, covXX);  matTau.setElem(0,1, covXY);  matTau.setElem(0,2, covXZ);
        matTau.setElem(1,0, covXY);  matTau.setElem(1,1, covYY);  matTau.setElem(1,2, covYZ);
        matTau.setElem(2,0, covXZ);  matTau.setElem(2,1, covYZ);  matTau.setElem(2,2, covZZ);


        this.configureFromCovariance(matTau);
        this.setDisplacement(vecDispl);
    }



    /**
     * Set the generalized beam perveance of the ellipsoidal charge 
     * directly.
     * 
     * @param K     generalized beam perveance <b>unitless</b>
     */
    public void setBeamPerveance(double K)  {
        this.m_dblK = K;
    }

    /**
     *  Set the semi-axes of the ellipsoid directly.
     * 
     * @param   a   ellipsoid semi-axis in x direction
     * @param   b   ellipsoid semi-axis in y direction
     * @param   c   ellipsoid semi-axis in z direction
     */
    public void setSemiAxes(double a, double b, double c)   {
        this.m_dblSemiX = a;
        this.m_dblSemiY = b;
        this.m_dblSemiZ = c;
    }
    
    /**
     * Sets the displacement of the ellipsoid centroid from the 
     * coordinate origin.
     *
     */
    public void setDisplacement(R3 vecDispl)   {
        this.m_vecDispl = vecDispl;
    }
    
    /**
     * Set the rotation matrix for the ellipsoid.  The argument must
     * be an element of the special orthogonal group SO(3) that 
     * converts from the natural coordinates of the ellipsoid to the
     * standard cartesian coordinates.  That is, to convert a point
     * <bold>r</bold> in the ellipsoid coordinate frame to standard
     * cartesian coordinates apply the transform
     * 
     *      rs = matRot*r
     * 
     * where <bold>rs</bold> represents the point in standard cartesian
     * coordinates.  That is, <code>matRot</code> is the eigenvector
     * matrix of the covariance matrix for the ellipsoid.
     * 
     * @param matRot    rotation matrix in SO(3)
     * 
     */
    public void setRotation(R3x3 matRot)    {
        this.m_matRot = matRot;
    }
    
    /*
     * Property Queries
     */
     
    /**
     * Return the generalized beam perveance of the ellipsoidal charge.
     */
    public double   getBeamPerveance()  { return this.m_dblK; };

    /**
     *  Return the value of the first ellispoid semi-axis.
     */     
    public double   getSemiAxisX()  { return this.m_dblSemiX; };

    /**
     *  Return the value of the second ellispoid semi-axis.
     */     
    public double   getSemiAxisY()  { return this.m_dblSemiY; };

    /**
     *  Return the value of the third ellispoid semi-axis.
     */     
    public double   getSemiAxisZ()  { return this.m_dblSemiZ; };

    /**
     *  Return all the ellipsoid semi-axes as a vector.
     * 
     *  @return     vector (a,b,c) of ellipsoid semi-axes
     */
    public R3   getSemiAxes()       { 
        return new R3(getSemiAxisX(), getSemiAxisY(), getSemiAxisZ());
    }
    
    /**
     * Return the displacement from the standard cartesian coordinate
     * original.  That is, if <b>d</b> is this displacement, then the
     * centroid of the ellipsoid lies at the point <b>d</b> in standard
     * cartesian coordinates.
     * 
     * @return  location point (dx,dy,dz) of ellipsoid centroid 
     */
    public R3   getDisplacement()   {
        return this.m_vecDispl;
    }
    
    /**
     * Get orthogonal rotation matrix <b>R</b> in SO(3) that converts 
     * from ellipsoid coordinates to standard cartesian coordinates.  
     * 
     * From an alternate perspective, if we align the ellipsoid to the 
     * standard coordinate axes, a rotaton by <b>R'</b> will put the 
     * ellipsoid into its current position, or a rotation by <b>R</b>
     * will move it from its current position back into standard position.
     * 
     * @return  rotation matrix in SO(3) moving the ellipsoid into standard position
     */
    public R3x3 getRotation()   {
        return this.m_matRot;
    }
    
    /**
     * Compute the rotation matrix in phase space that corresponds to 
     * the current rotation matrix in 3D configuration space.  Since the
     * tangent bundles of 3D configuration space at a point (x,y,z) are
     * represented by the Cartesian planes (x',y',z'), they must be rotated
     * in the same manner as the point (x,y,z).  This is a convenience method
     * to build that corresponding phase matrix in SO(7).
     * 
     * @return  phase rotation matrix in S0(7) corresponding to the current rotation in S0(3) 
     */
    public PhaseMatrix  compPhaseRotation()  {
        R3x3        matSO3 = this.getRotation();
        PhaseMatrix matSO7 = PhaseMatrix.identity();
        
        // Populate the phase rotation matrix
        int         i, j;       // indices into the SO(3) matrix
        int         m, n;       // indices into the SO(7) matrix
        double      rij;        // temporary
        
        for (i=0; i<R3x3.DIM; i++)
            for (j=0; j<R3x3.DIM; j++)  {
                m   = 2*i;
                n   = 2*j;
                rij = matSO3.getElem(i,j);
                
                matSO7.setElem(  m,   n, rij);
                matSO7.setElem(m+1, n+1, rij);
            }
        
        return matSO7;
    }
    

    /**
     * <p>
     * Computes the normalized defocusing lengths for a space charge kick 
     * given the semi-axes of the beam ellipsoid.  The true defocusing lengths
     * are obtained by multiplying the result by the factor 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; 1/f = (1/fnorm)*K*ds
     * <br/>
     * <br/>
     * where f is the defocusing length, fnorm is the normalized defocusing
     * length (returned by this method), K is the generalized beam 
     * perveance, and ds is the pathlength over which the kick is being 
     * applied.
     * </p>
     * <p>
     * The defocusing lengths (1/fx,1/fy,1/fz) are given for the cartesian 
     * coordinate system x',y',z' which is aligned to the ellipsoid semi-axes.
     * If the ellipsoid is rotated with respect to the coordinate system in 
     * which the ellipsoid was defined (constructed), then any transfer matrix
     * built from these focusing constants must be rotated into the original
     * coordinate system.
     * </p>
     * <p>
     * The defocusing lengths are computed from a weighted linear
     * regression of the true fields generated by an ellipsoidally symmetric 
     * charge distribution.  By the equivalent uniform beam principle 
     * the space charge effects (to second order) are only loosely
     * coupled to the actual profile of the distribution (assuming that 
     * it is ellipsoidally symmetric).  The effect from the distribution
     * profile manifests itself as a factor, we take this factor to be that
     * for a uniform ellipsoid for computational purposes.
     * </p>
     * 
     * @return  vector (fxnorm,fynorm,fznorm) of defocusing constants
     * 
     * @see #getRotation
     * @see #compPhaseRotation
     * @see <a href="http://lib-www.lanl.gov/cgi-bin/getfile?00796950.pdf">Theory and Technique
     *      of Beam Envelope Simulation</a>
     * 
     * @author  Christopher K. Allen
     */
    public R3 compDefocusConstants()    {

        // Square the semi-axes
        double  a_2 = getSemiAxisX()*getSemiAxisX();
        double  b_2 = getSemiAxisY()*getSemiAxisY();
        double  c_2 = getSemiAxisZ()*getSemiAxisZ();
       
        // Compute the Carlson elliptic integral values
        double ellipticX = EllipticIntegral.RD(b_2/a_2, c_2/a_2, 1.0);
        double ellipticY = EllipticIntegral.RD(c_2/b_2, a_2/b_2, 1.0);
        double ellipticZ = EllipticIntegral.RD(a_2/c_2, b_2/c_2, 1.0);

        
        // Compute (de)focusing strengths from space charge
        double fX = FCONST*a_2*getSemiAxisX()/ellipticX;
        double fY = FCONST*b_2*getSemiAxisY()/ellipticY;
        double fZ = FCONST*c_2*getSemiAxisZ()/ellipticZ;
        
        return new R3(fX, fY, fZ);
    }

    /**
     * This method is provided as a comparison utility for validation against simulation
     * with Trace3D.  Trace3D uses an approximation to the elliptic integrals encountered
     * in the space charge field expressions.  These approximations break down to first order
     * as the beam ellipsoid becomes increasingly eccentric in the transverse direction.
     *   
     * @return vector (fxnorm,fynorm,fznorm) of defocusing constants
     * 
     * @see #compDefocusConstants
     */
    public R3 compDefocusConstantsAlaTrace3D()    {

        // Square the semi-axes
        double  a   = getSemiAxisX();
        double  b   = getSemiAxisY();
        double  c   = getSemiAxisZ();
        
        double  a_2 = a*a;
        double  b_2 = b*b;
        double  c_2 = c*c;
        
        double  s   = c/Math.sqrt(a*b);
       
       
        // Compute the Carlson elliptic integral values
        double approxX = (3.0/c)*(a_2/(a+b))*(1.0 - EllipticIntegral.formFactorD(s));
        double approxY = (3.0/c)*(b_2/(a+b))*(1.0 - EllipticIntegral.formFactorD(s));
        double approxZ = (3.0*c_2/(a*b))*EllipticIntegral.formFactorD(s); 

        
        // Compute (de)focusing strengths from space charge
        double fX = FCONST*a_2*a/approxX;
        double fY = FCONST*b_2*b/approxY;
        double fZ = FCONST*c_2*c/approxZ;
        
        return new R3(fX, fY, fZ);
    }


    /** 
     * Calculates the transfer matrix generator for space charge effects from
     * this <code>EllipsoidalCharge</code> object.  Denoting the returned 
     * transfer matrix generator as <b>B</b> then the actual transfer matrix
     * <b>M</b>(s) for the space charge effect is given as
     * 
     *      <b>M</b>(s) = exp(s<b>B</b>)
     * 
     * where s is the path length of the dynamics. 
     * 
     * Note that to obtain this matrix a linear fit to the true fields was 
     * performed where the regression is weighted by the distribution itself.
     * According the "Equivalent Beam" principle by Sacherar this regression
     * then only loosely couples to the actual distribution profile of the the
     * ellipsoidal charge.  For computational purposes we have assumed a uniform
     * density ellipsoid, but that is of no real consequence practically.
     * 
     *  @return         transfer matrix generator representing linear space charge effects
     * 
     *  @author Christopher K. Allen
     */
    public PhaseMatrix compTransMatrixGen()    {
        
        // Check for zero-space charge case
        if (this.getBeamPerveance()==0.0) 
            return PhaseMatrix.identity();

        
        R3  vecFocus = this.compDefocusConstants();
//        R3  vecFocus = rho.compDefocusConstantsAlaTrace3D();
        
        double kX = this.getBeamPerveance()/vecFocus.getx();
        double kY = this.getBeamPerveance()/vecFocus.gety();
        double kZ = this.getBeamPerveance()/vecFocus.getz();


        // Get the beam displacement from the origin and the rotation matrix
        R3          vecDis = this.getDisplacement();
        PhaseMatrix matRot = this.compPhaseRotation();
        
        double  xm = vecDis.getx();
        double  ym = vecDis.gety();
        double  zm = vecDis.getz();
                 

        // Assemble the space charge matrix in the ellipsoid semi-axes coordinates
        PhaseMatrix matSC = PhaseMatrix.zero();

        matSC.setElem(1,0,  kX);
        matSC.setElem(1,6, -kX*xm);
        matSC.setElem(3,2,  kY);
        matSC.setElem(3,6, -kY*ym);
        matSC.setElem(5,4,  kZ);
        matSC.setElem(5,6, -kZ*zm);
        
        matSC = matSC.conjugateTrans(matRot);   // now rotate to beam cartesian coordinates
        
        return matSC;
    };
    
    
    
    /*
     *  Support Methods
     */

    /**
     *  Check matrix for symmetry.  Quadratic forms must be
     *  symmetric and positive definite.
     * 
     *  @param  mat     <code>R3x3</code> object to check
     * 
     *  @exception  IllegalArgumentException    matrix is not symmetric and/or positive definite
     * 
     *  @author Christopher K. Allen
     */
    @SuppressWarnings("unused")
    private void    checkSymmetry(R3x3 mat)
        throws IllegalArgumentException    
    {
        if ( !mat.isSymmetric() )
            throw new IllegalArgumentException("Matrix is not symmetric");
    }


}






/*
 * Storage
 */


///**
// * Construct an ellipsoid from its semi-axes values.  The ellipsoid is assumed 
// * to be centered on the coordinate origin and aligned to the coordinate
// * axes.  Thus, the reference ellipsoid is described by the equation
// * 
// *      (x/a)^2 + (y/b)^2 + (z/c)^2 = 1
// * 
// * where a,b,c are the semi-axes values on the x,y,z coordinate directions,
// * respectively.
// * 
// * @param   a   ellipsoid semi-axis in x direction
// * @param   b   ellipsoid semi-axis in y direction
// * @param   c   ellipsoid semi-axis in z direction
// *
// *  @see    #setSemiAxes  
// */
//public EllipsoidalCharge(double a, double b, double c)  {
//    this.setSemiAxes(a, b, c);
//}
//


///**
// * Construct an ellipsoid from its covariance matrix.  The reference elllipsoid
// * is represented by the equation
// * 
// *      r'*matSigmaInv*r=1
// * 
// * where matSigmaInv is the inverse of the 3x3 matrix argument matSigma, 
// * r=(x y z) is the position vector in R3, and the prime indicates 
// * transposition.  Note that matSigma must be symmetric and 
// * positive definite.  Thus, it is diagonalizable with the
// * decomposition
// * 
// *      matSigma = R*D*R'
// * 
// * where the prime indicates transposition, R in SO(3) is the orthogonal 
// * rotation matrix and D is the diagonal
// * matrix of real eigenvalues.  Note from the above that these eigenvalues 
// * are the squares of the ellipsoid semi-axes.
// * 
// *  @param  matSigma    covariance matrix for ellipsoid
// * 
// *  @exception  IllegalArgumentException    matrix is not symmetric and/or positive definite
// * 
// *  @see    #setCovariance
// */     
//public EllipsoidalCharge(R3x3 matSigma)
//    throws IllegalArgumentException
//{
////    this.checkSymmetry(matSigma);
//    this.setCovariance(matSigma);            
//}
//


//  /**
//   * Construct an ellipsoid from its covariance matrix and displacement vector.  
//   * The reference elllipsoid is represented by the equation
//   * 
//   *      (r-vecDispl)'*matSigmaInv*(r-vecDispl) = 1
//   * 
//   * where matSigmaInv is the inverse of the 3x3 matrix argument matSigma, 
//   * r=(x y z) is the position vector in R3, and the prime indicates
//   * tansposition.  Note that matSigma must be symmetric and positive 
//   * definite.  Thus, it is diagonalizable with the decomposition
//   * 
//   *      matSigma = R*D*R'
//   * 
//   * where the prime indicates transposition, R in SO(3) is the orthogonal 
//   * rotation matrix and D is the diagonal
//   * matrix of real eigenvalues.  Note from the above that these eigenvalues 
//   * are the squares of the ellipsoid semi-axes.
//   * 
//   *  @param  matSigma    covariance matrix for ellipsoid
//   *  @param  vecDispl    displacement vector from the coordinate origin
//   * 
//   *  @exception  IllegalArgumentException    matrix is not symmetric and/or positive definite
//   * 
//   *  @see    #setCovariance
//   *  @see    #setDisplacement
//   */     
//  public EllipsoidalCharge(R3x3 matSigma, R3 vecDispl)
//      throws IllegalArgumentException
//  {
////        this.checkSymmetry(matSigma);
//      this.setCovariance(matSigma);
//      this.setDisplacement(vecDispl);
//  }
//    


///** 
// * Construct a beam charge density ellipsoid described
// * by the phase space correlation matrix <code>matChi</code>.
// * 
// * Note that the phase space correlation matrix in homogeneous coordinates
// * contains all moments up to and including second order; this includes the
// * first-order moments that describe the displacement of the ellipsoid from
// * the coordinate origin.  Thus, from the 7x7 phase space correlation matrix
// * we extract the displacement vector
// * 
// *      (&lt;x&gt;,&lt;y&gt;&lt;z&gt;)
// * 
// * and the 3x3 covariance matrix
// * 
// *      | &lt;x*x&gt; &lt;x*y&gt; &lt;x*z&gt; |    | &lt;x&gt;*&lt;x&gt; &lt;x&gt;*&lt;y&gt; &lt;x&gt;*&lt;z&gt; |
// *      | &lt;y*x&gt; &lt;y*y&gt; &lt;y*z&gt; | -  | &lt;y&gt;*&lt;x&gt; &lt;y&gt;*&lt;y&gt; &lt;y&gt;*&lt;z&gt; |
// *      | &lt;z*x&gt; &lt;z*y&gt; &lt;z*z&gt; |    | &lt;z&gt;*&lt;x&gt; &lt;z&gt;*&lt;y&gt; &lt;z&gt;*&lt;z&gt; |
// * 
// * to construct the ellipsoidal charge object according to the previous constructor
// * parameters.
// * 
// *  @param  matChi  envelope correlation matrix in homogeneous phase space coordinates
// * 
// *  @author Christopher K. Allen
// * 
// *  @see    #setCovariance
// *  @see    #setDisplacement
// */
//public EllipsoidalCharge(CorrelationMatrix matChi)    {
//        
//    // Build the displacement vector
//    double  xm = matChi.getMeanX();
//    double  ym = matChi.getMeanY();
//    double  zm = matChi.getMeanZ();
//        
//    R3      vecDispl = new R3(xm, ym, zm);
//        
//        
//    // Build the configuration matrix
//    double  covXX = matChi.getCovXX();
//    double  covXY = matChi.getCovXY();
//    double  covYY = matChi.getCovYY();
//    double  covYZ = matChi.getCovYZ();
//    double  covZZ = matChi.getCovZZ();
//    double  covXZ = matChi.getCovXZ();
//        
//    R3x3    matTau = new R3x3();
//        
//    matTau.setElem(0,0, covXX);  matTau.setElem(0,1, covXY);  matTau.setElem(0,2, covXZ);
//    matTau.setElem(1,0, covXY);  matTau.setElem(1,1, covYY);  matTau.setElem(1,2, covYZ);
//    matTau.setElem(2,0, covXZ);  matTau.setElem(2,1, covYZ);  matTau.setElem(2,2, covZZ);
//
//
//    this.setCovariance(matTau);
//    this.setDisplacement(vecDispl);
//};
//    
    
