package xal.tools.beam;

import xal.tools.annotation.AProperty.NoEdit;
import xal.tools.beam.Twiss3D.IND_3D;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.math.ElementaryFunction;
import xal.tools.math.r3.R3x3;


/**
 * <p>
 * A <code>CovarianceMatrix</code> in homogeneous coordinates represents
 * all moments of a phase space distribution up to and including second order.  This is 
 * seen by taken the moment of the outer product of two phase vectors.  We find
  * <pre>
 * &lt;zz&circ;T&gt;  = | &lt;xx&gt;   &lt;xx'   &lt;xy&gt;   &lt;xy'&gt;  &lt;xz&gt;   &lt;xz'&gt;  &lt;x&gt;  |
 *           | &lt;x'x&gt;  &lt;x'x'&gt; &lt;x'y&gt;  &lt;x'y'&gt; &lt;x'z&gt;  &lt;x'z'&gt; &lt;x'&gt; |
 *           | &lt;yx&gt;   &lt;yx'&gt;  &lt;yy&gt;   &lt;yy'&gt;  &lt;yz&gt;   &lt;yz'&gt;  &lt;y&gt;  |
 *           | &lt;y'x&gt;  &lt;y'x'&gt; &lt;y'y&gt;  &lt;y'y'&gt; &lt;y'z&gt;  &lt;y'z'&gt; &lt;y'&gt; |
 *           | &lt;zx&gt;   &lt;zx'&gt;  &lt;zy&gt;   &lt;zy'&gt;  &lt;zz&gt;   &lt;zz'&gt;  &lt;z&gt;  |
 *           | &lt;z'x&gt;  &lt;z'x'&gt; &lt;z'y&gt;  &lt;z'y&gt;  &lt;z'z&gt;  &lt;z'z'&gt; &lt;z'&gt; |
 *           | &lt;x&gt;     &lt;x&gt;    &lt;y&gt;     &lt;y&gt;   &lt;z&gt;    &lt;z&gt;   &lt;1&gt;  |
 * </pre>
 * where <i>x', y', z'</i> represent the momentum coordinate in the <i>x, y,</i> and <i>z</i>
 * directions, respectively.
 *
 * <p>
 * Note that the covariance matrix is not necessarily centralized.  Specifically, if the
 * beam is off axis, then at least one of the moments 
 * &lt;<i>x</i>&gt;, &lt;<i>y</i>&gt;, &lt;<i>z</i>&gt; is non-zero and the corresponding 
 * second-order moments will be skewed.  Likewise, if the beam has a coherent drift in
 * some direction, then the moments &lt;<i>x'</i>&gt;, &lt;<i>y'</i>&gt;, &lt;<i>z'</i>&gt;
 * will have at least one nonlinear value.  There are methods in this class for returning
 * centralized moments when such quantities are needed.
 * </p>
 * <p>
 * Note that the covariance matrix is not necessarily centralized.  Specifically, if the
 * beam is off axis, then at least one of the moments 
 * &lt;<i>x</i>&gt;, &lt;<i>y</i>&gt;, &lt;<i>z</i>&gt; is non-zero and the corresponding 
 * second-order moments will be skewed.  Likewise, if the beam has a coherent drift in
 * some direction, then the moments &lt;<i>xp</i>&gt;, &lt;<i>yp</i>&gt;, &lt;<i>zp</i>&gt;
 * will have at least one nonlinear value.  There are methods in this class for returning
 * centralized moments when such quantities are needed.
 * </p>
 * 
 * @author Christopher K. Allen
 * @author Craig McChesney
 */
public class CovarianceMatrix extends PhaseMatrix {

    
    /*
     * Global Constants
     */

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 1L;

    /** default number of significant digits to consider when testing for symmetry */
    private static final int CNT_SYMMETRY_DIGITS = 10;
    
    /** default number of ULPs to use for comparing numbers for equality */
    private static final int CNT_SYMMETRY_ULPS = 10000;

    

    /*
     *  Global Methods
     */
    
    /**
     *  Create a new instance of a zero phase matrix.
     *
     *  @return         zero vector
     */
    public static CovarianceMatrix  newZero()   {
        return new CovarianceMatrix( PhaseMatrix.zero() );
    }
    
    /**
     *  Create an identity correlation matrix
     *
     *  @return         7x7 real identity matrix
     */
    public static CovarianceMatrix  newIdentity()   {
        return new CovarianceMatrix( PhaseMatrix.identity() );
    }
    
    /**
     * <p> Create a "center matrix" corresponding the given mean values 
     * (centroid location).  The returned matrix can be subtracted from
     * a covariance matrix to produce a <i>central</i> covariance matrix.
     *  </p>  
     *  <p>
     *  <h3>NOTE:</h3>
     *  The returned matrix is the outer product of the given argument.
     *  Specifically <b>&sigma;</b> = <b>v*v'</b>.
     *  
     *  @param  vecCen mean value vector of the phase space coordinates, i.e., &lt;z&gt;
     *
     *  @return     center matrix corresponding to mean value vector
     */
    public static CovarianceMatrix newCenter(PhaseVector vecCen) {
        
        // Add the average value squared to the covariance matrix to form the correlation matrix        
        PhaseMatrix     matCen;
         
        matCen = vecCen.outerProd(vecCen);

        return new CovarianceMatrix(matCen);
    }        

    /**
     * Create and return a <code>CovarianceMatrix</code> built according to the
     * given set of Twiss parameters.
     * 
     * @param   envTwiss    twiss parameter set describing RMS envelope
     * 
     * @return  correlation matrix with statistical properties of argument
     * 
     * @see CovarianceMatrix#buildCovariance(Twiss, Twiss, Twiss)
     */
    public static CovarianceMatrix buildCovariance(Twiss3D envTwiss)  {
        return CovarianceMatrix.buildCovariance(
                                    envTwiss.getTwiss(IND_3D.X), 
                                    envTwiss.getTwiss(IND_3D.Y),
                                    envTwiss.getTwiss(IND_3D.Z)
                                    );
    }
    
    /**
     *  <p>
     *  Create a PhaseMatrix that is the correlation matrix corresponding
     *  to the given Twiss parameters.  Note that the correlation matrix is
     *  for a centered beam (on axis).  Thus, the correlation matrix is actually
     *  the covariance matrix.
     *  </p>
     *  <h3>NOTE:</h3>
     *  <p>
     *  No unit conversion is done, the correlation matrix has the same
     *  unit system as the Twiss parameters.
     *  </p>
     *  <p>
     *  The returned matrix is in homogeneous coordinates of the block 
     *  diagonal form
     *  <br>
     *  <br>
     *  <br>    | R<sub><i>xx</i></sub>   0   0   0 |
     *  <br>    |   0 R<sub><i>yy</i></sub>   0   0 |
     *  <br>    |   0   0 R<sub><i>zz</i></sub>   0 |
     *  <br>    |   0   0   0   1 |
     *  <br>
     *  <br>
     *  where R<sub><i>ii</i></sub> are 2x2 symmetric blocks corresponding to each phase
     *  plane.  Clearly the phase planes are uncoupled.
     *  </p>
     *
     *  @param  twissX  twiss parameters of the x phase plane
     *  @param  twissY  twiss parameters of the y phase plane
     *  @param  twissZ  twiss parameters of the z phase plane
     *
     *  @return     correlation matrix corresponding to the above twiss parameters
     */
    public static CovarianceMatrix buildCovariance(Twiss twissX, Twiss twissY, Twiss twissZ) {
        
        CovarianceMatrix matCorr = new CovarianceMatrix(PhaseMatrix.zero());

        // Fill in x plane block
        double[][] Rxx = twissX.correlationMatrix();
        matCorr.setSubMatrix(0, 1, 0, 1, Rxx);

        // Fill in y plane block
        double[][] Ryy = twissY.correlationMatrix();
        matCorr.setSubMatrix(2, 3, 2, 3, Ryy);

        // Fill in z plane block
        double[][] Rzz = twissZ.correlationMatrix();
        matCorr.setSubMatrix(4, 5, 4, 5, Rzz);

        matCorr.setElem(6, 6, 1.0);

        return matCorr;
    };

    /**
     * <p>
     *  Create a CovarianceMatrix corresponding to the given Twiss parameters and 
     *  having the given mean values (centroid location).
     *  </p>  
     *  <h3>NOTE:</h3>
     * <p>
     *  The returned matrix is in homogeneous coordinates of the block 
     *  diagonal form
     *  <br>
     *  <br>
     *      | Rxx   0   0  &lt;x&gt; | <br>
     *      |   0 Ryy   0  &lt;y&gt; | <br>
     *      |   0   0 Rzz  &lt;z&gt; | <br>
     *      | &lt;x&gt; &lt;y&gt; &lt;z&gt;   1  | 
     *  <br>
     *  <br>
     *  where Rii are 2x2 symmetric blocks corresponding to each phase
     *  plane, and &lt;i&gt; is shorthand for the vector of phase averages
     *  for the i plane, eg. &lt;x&gt; = (&lt;x&gt;, &lt;x'&gt;).
     *  </p>
     *
     *  @param  twissX  twiss parameters of the x phase plane
     *  @param  twissY  twiss parameters of the y phase plane
     *  @param  twissZ  twiss parameters of the z phase plane
     *  @param  vecCen mean value vector of the phase space coordinates, i.e., &lt;z&gt;
     *
     *  @return     correlation matrix corresponding to the above twiss parameters and mean value vector
     */
    public static CovarianceMatrix buildCovariance(Twiss twissX, Twiss twissY, Twiss twissZ, PhaseVector vecCen) {
        
        // Build the covariance matrix
        CovarianceMatrix   matSig;
        
        matSig = buildCovariance(twissX, twissY, twissZ);
        matSig.setElem(IND.HOM, IND.HOM, 0.0);


        // Add the average value squared to the covariance matrix to form the correlation matrix        
        PhaseMatrix     matCen;
        PhaseMatrix     matTau;
         
        matCen = vecCen.outerProd(vecCen);
        matTau = matSig.plus(matCen);

        return new CovarianceMatrix(matTau);
    }        

    /**
     * Create a new <code>CovarianceMatrix</code> object and initialize with the data 
     * source behind the given <code>DataAdaptor</code> interface.  Currently
     * we are checking for symmetry within the loaded matrix so this method is using
     * extended CPU time.
     * 
     * @param   daSource    data source containing initialization data
     * 
     * @throws DataFormatException          malformed data in data source
     * @throws IllegalArgumentException     the described matrix is not symmetric
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     *
     * @since  Jan 4, 2016,   Christopher K. Allen
     */
    public static CovarianceMatrix loadFrom(DataAdaptor daSource) throws IllegalArgumentException, DataFormatException {
        PhaseMatrix         matBase = PhaseMatrix.loadFrom(daSource);
        CovarianceMatrix    matCov = new CovarianceMatrix(matBase);
        
        if (!matCov.checkSymmetryToSigDigits(matCov, CNT_SYMMETRY_DIGITS))
            throw new IllegalArgumentException("CovarianceMatrix(PhaseMatrix) - argument not symmetric.");
        
        return matCov;
    }




    /*
     *  Initialization
     */

    /**
     * Constructor for CovarianceMatrix. Creates a  zero values correlation
     * matrix.
     */
    public CovarianceMatrix() {
    	super();
    }

    /** 
     * <p>
     * Constructor for CovarianceMatrix.  Creates an initialized correlation
     * matrix from a symmetric <code>PhaseMatrix</code> argument.
     * </p>
     * <p>
     * We are not checking symmetry of the given phase matrix in order to 
     * expedite the process.  So the caller must ensure symmetry.
     * </p>
     * 
     * @param matInit  symmetric <code>PhaseMatrix</code> object to initial this
     * 
     * @exception   IllegalArgumentException  initializing matrix is not symmetric
     */
    public CovarianceMatrix(PhaseMatrix matInit) throws IllegalArgumentException {
        super(matInit);

//        if (!this.checkSymmetry(this))
//            throw new IllegalArgumentException("CovarianceMatrix(PhaseMatrix) - argument not symmetric.");
    }

    /**
     * Constructor for CovarianceMatrix.  Takes a formatted text string containing
     * the initial values of the matrix.
     * 
     * @param strTokens     initial values of matrix in Mathematica format
     * 
     * @throws IllegalArgumentException wrong number of token string in argument
     * @throws NumberFormatException    malformatted number in argument string
     * 
     * @see PhaseMatrix#PhaseMatrix(String)
     */
    public CovarianceMatrix(String strTokens)
        throws IllegalArgumentException, NumberFormatException 
    {
        super(strTokens);

//        if (!this.checkSymmetry(this))
//            throw new IllegalArgumentException("CovarianceMatrix(PhaseMatrix) - argument not symmetric.");

        if (!this.checkSymmetryToSigDigits(this, CNT_SYMMETRY_DIGITS))
            throw new IllegalArgumentException("CovarianceMatrix(String) - argument not symmetric.");
    }
    
    /**
     * <p>
     *  (Re)sets the rms emittances for the beam.  This method scales the X,Y,Z diagonal blocks
     *  of the correlation matrix so the beam has the rms emittances provided.
     *  </p>  
     *  <p>
     *  <h4>NOTES:</h4>
     *  Since the emittance values are contained in the 
     *  correlation matrix attempting to set emittances with an empty (zero) correlation matrix 
     *  causes a floating point exception.  As such, this method can really only change existing
     *  emittances and is to be regarded as a convenience function.
     * 
     *  <h4>IMPORTANT:</h4>
     *  The current implementation is valid ONLY FOR ZERO-MEAN correlations.
     * 
     *  <h4>TODO</h4>
     *  Fix the implementation so that it is valid for off-centered distributions.
     *  </p>
     *
     *  @param  arrEmitNew  three element vector of rms emittances for X,Y,Z planes, respectively
     *                      <b>Units radian-meters</b>
     * 
     *  @author C.K. Allen
     */
    public void forceRmsEmittances(double[] arrEmitNew)    {
        double[]    arrEmitCurr;    // current emittances
        double      fac;            // multiplication factor to change emittance value
        double      val;            // auxiliary variable used when updating covariance matrix
        
        arrEmitCurr = this.computeRmsEmittances();

        // X plane emittances
        fac = arrEmitNew[0]/arrEmitCurr[0];
        for (int i=0; i<=1; i++)
            for (int j=0; j<=1; j++)    {
                val = this.getElem(i,j);
                this.setElem(i,j, fac*val);
            }
        
        // Y plane emittances
        fac = arrEmitNew[1]/arrEmitCurr[1];
        for (int i=2; i<=3; i++)
            for (int j=2; j<=3; j++)    {
                val = this.getElem(i,j);
                this.setElem(i,j, fac*val);
            }

        // Z plane emittances
        fac = arrEmitNew[2]/arrEmitCurr[2];
        for (int i=4; i<=5; i++)
            for (int j=4; j<=5; j++)    {
                val = this.getElem(i,j);
                this.setElem(i,j, fac*val);
            }
    }    
        
    


    /*
     *  Property Query
     */
     
     
    /**
     *  Return the mean value of the <i>x</i> phase variable.
     * 
     *  @return     the value of &lt;x&gt;
     */
    public double   getMeanX()   {
        return this.getElem(IND_HOM, IND_X);
    }

    /**
     *  Return the mean value of the <i>y</i> phase variable.
     * 
     *  @return     the value of &lt;y&gt;
     */
    public double   getMeanY()   {
        return this.getElem(IND_HOM, IND_Y);
    }

    /**
     *  Return the mean value of the <i>z</i> phase variable.
     * 
     *  @return     the value of &lt;z&gt;
     */
    public double   getMeanZ()   {
        return this.getElem(IND_HOM, IND_Z);
    }
    
    /** 
     *  Return the phase space coordinates of the centroid in homogeneous coordinates.
     *  Since the correlation matrix is represented in homogeneous coordinates
     *  the mean values are elements of the correlation matrix.  These values are extracted
     *  and returned as a vector. 
     * 
     *  @return     PhaseVector representing the mean values of the correlation
     */
	@NoEdit		// editors should ignore this property as it is really a computed value
    public PhaseVector getMean() {
        PhaseVector vec = new PhaseVector();
        
        for (PhaseIndex i : PhaseIndex.values())
            vec.setElem(i, this.getElem(i.val(),IND_HOM));
        return vec;
    }



    /**
     *  Compute and return the covariance value of the <i>xx</i> phase space 
     *  coordinate monomial.
     * 
     * @return  the value &lt;x^2&gt;-&lt;x&gt;^2
     */
    public double   computeCentralCovXX()  {
        double x  = this.getMeanX();
        double xx = this.getElem(IND_X, IND_X);
        
        return xx - x*x;
    }

    /** 
     *  Compute and return the covariance value of the <i>xy</i> phase space 
     *  coordinate monomial.
     * 
     *  @return     the value &lt;xy&gt;-&lt;x&gt;&lt;y&gt;
     */
    public double   computeCentralCovXY()   {
        double  x = this.getMeanX();
        double  y = this.getMeanY();
        
        double xy = this.getElem(IND_X, IND_Y);
        
        return xy - x*y; 
    }

    /**
     *  Compute and return the covariance value of the <i>yy</i> phase space 
     *  coordinate monomial.
     * 
     * @return  the value &lt;y^2&gt;-&lt;y&gt;^2
     */
    public double   computeCentralCovYY()  {
        double y  = this.getMeanY();
        double yy = this.getElem(IND_Y, IND_Y);
        
        return yy - y*y;
    }

    /** 
     *  Compute and return the covariance value of the <i>yz</i> phase space 
     *  coordinate monomial.
     * 
     *  @return     the value &lt;yz&gt;-&lt;y&gt;&lt;z&gt;
     */
    public double   computeCentralCovYZ()   {
        double  y = this.getMeanY();
        double  z = this.getMeanZ();
        
        double yz = this.getElem(IND_Y, IND_Z);
        
        return yz - y*z; 
    }

    /**
     *  Compute and return the covariance value of the <i>zz</i> phase space 
     *  coordinate monomial.
     * 
     * @return  the value &lt;z^2&gt;-&lt;z&gt;^2
     */
    public double   computeCentralCovZZ()  {
        double z  = this.getMeanZ();
        double zz = this.getElem(IND_Z, IND_Z);
        
        return zz - z*z;
    }

    /** 
     *  Compute and return the covariance value of the <i>xz</i> phase space 
     *  coordinate monomial.
     * 
     *  @return     the value &lt;xz&gt;-&lt;x&gt;&lt;z&gt;
     */
    public double   computeCentralCovXZ()   {
        double  x = this.getMeanX();
        double  z = this.getMeanZ();
        
        double xz = this.getElem(IND_X, IND_Z);
        
        return xz - x*z; 
    }

    
    /**
     * Compute and return the 3x3 symmetric matrix of all centralized spatial covariance
     * values.  Recall that the covariance matrix <b>sig</b> is the matrix of 
     * central second moments and is related to the correlation matrix &lt;<b>zz</b>&gt;
     * according to
     *
     *      <b>sig</b> = &lt;<b>zz</b>&gt; - &lt;<b>z</b>&gt;&lt;<b>z</b>&gt;
     *      
     * where <b>z</b> = (x x' y y' z z' 1) is the phase space coordinate vector. 
     * 
     * Thus, the returned spatial covariance matrix has the form
     * 
     *      | &lt;xx&gt;-&lt;x&gt;&lt;x&gt;  &lt;xy&gt;-&lt;x&gt;&lt;y&gt;  &lt;xz&gt;-&lt;x&gt;&lt;z&gt; |
     *      | &lt;xy&gt;-&lt;x&gt;&lt;y&gt;  &lt;yy&gt;-&lt;y&gt;&lt;y&gt;  &lt;yz&gt;-&lt;y&gt;&lt;z&gt; |
     *      | &lt;xz&gt;-&lt;x&gt;&lt;z&gt;  &lt;yz&gt;-&lt;x&gt;&lt;z&gt;  &lt;zz&gt;-&lt;z&gt;&lt;z&gt; |
     *      
     *      
     * @return  3x3 symmetric matrix of central, spatial covariance moments 
     */
    public R3x3 computeSpatialCovariance()  {
        
        // Get all the matrix elements
        double  covXX = this.computeCentralCovXX();
        double  covXY = this.computeCentralCovXY();
        double  covYY = this.computeCentralCovYY();
        double  covYZ = this.computeCentralCovYZ();
        double  covZZ = this.computeCentralCovZZ();
        double  covXZ = this.computeCentralCovXZ();
        
        
        // Assemble the matrix and return it
        R3x3    matCov = new R3x3();
        
        matCov.setElem(0,0, covXX);  matCov.setElem(0,1, covXY);  matCov.setElem(0,2, covXZ);
        matCov.setElem(1,0, covXY);  matCov.setElem(1,1, covYY);  matCov.setElem(1,2, covYZ);
        matCov.setElem(2,0, covXZ);  matCov.setElem(2,1, covYZ);  matCov.setElem(2,2, covZZ);
        
        return matCov;
    };
    
    
    /**
     * Compute and return the standard deviation of the <i>x</i> phase variable
     * 
     * @return  sqrt( &lt;x^2&gt; - &lt;x&gt;^2 ) 
     */
    public double   getSigmaX() {
        double  dblCovX = this.computeCentralCovXX();
        double  dblSigX = Math.sqrt(dblCovX);
        
        return dblSigX;
    }

    /**
     * Compute and return the standard deviation of the <i>y</i> phase variable 
     * 
     * @return  sqrt( &lt;y^2&gt; - &lt;y&gt;^2 ) 
     */
    public double   getSigmaY() {
        double  dblCovY = this.computeCentralCovYY();
        double  dblSigY = Math.sqrt(dblCovY);
        
        return dblSigY;
    }

    /**
     * Compute and return the standard deviation of the <i>z</i> phase variable
     * 
     * @return  sqrt( &lt;z^2&gt; - &lt;z&gt;^2 ) 
     */
    public double   getSigmaZ() {
        double  dblCovZ = this.computeCentralCovZZ();
        double  dblSigZ = Math.sqrt(dblCovZ);
        
        return dblSigZ;
    }




    /**
     *  Compute and return the covariance matrix of the distribution.  
     *  Note that this can be computed from the correlation matrix in 
     *  homogeneous coordinates since the mean values are 
     *  included in that case.
     * 
     *  @return     &lt;(z-&lt;z&gt;)*(z-&lt;z&gt;)^T&gt; = &lt;z*z^T&gt; - &lt;z&gt;*&lt;z&gt;^T
     */
    public CovarianceMatrix computeCentralCovariance() {
        PhaseVector vecMean = this.getMean();
        PhaseMatrix matCorrel = this;
        PhaseMatrix matAve2 = vecMean.outerProd(vecMean);
        CovarianceMatrix   matCov = new CovarianceMatrix(matCorrel.minus(matAve2));
        
        matCov.setElem(IND_HOM,IND_HOM, 1.0);   // set the unity homogeneous diagonal
        return matCov;        
    }


    /** 
     *  Return the x,y,z plane rms emittance of the beam <b>Units: radian-meters</b>
     *
     *  NOTE:
     *  This method ignores any coupling between phase planes and any offsets of the
     *  beam centroid from the beam axis.
     *
     *  (To implement a method which gives the emittances if the beam where centered
     *  use the method this.phaseCovariance() instead of this.phaseCorrelation().)
     *
     *  @return     array of double with length 3 where
     *              array[0] = emittance in x plane
     *              array[1] = emittance in y plane
     *              array[2] = emittance in z plane
     * 
     *  @author C.K. Allen
     */
    public double[] computeRmsEmittances() {
    	//        PhaseMatrix matSig = this.phaseCorrelation();
        CovarianceMatrix matSig = this.computeCentralCovariance();
	
        double ex_2 =
                matSig.getElem(0, 0) * matSig.getElem(1, 1)
                - matSig.getElem(0, 1) * matSig.getElem(1,0);
        double ey_2 =
                matSig.getElem(2, 2) * matSig.getElem(3, 3)
                - matSig.getElem(2, 3) * matSig.getElem(3, 2);
        double ez_2 =
                matSig.getElem(4, 4) * matSig.getElem(5, 5)
                - matSig.getElem(4, 5) * matSig.getElem(5, 4);

        double[] arrEmitt = new double[3];

        arrEmitt[0] = java.lang.Math.sqrt(ex_2);
        arrEmitt[1] = java.lang.Math.sqrt(ey_2);
        arrEmitt[2] = java.lang.Math.sqrt(ez_2);

        return arrEmitt;
    };

    /**
     *  Return the Twiss parameters for each plane that correspond to the current 
     *  correlation matrix.
     *
     *  NOTE:
     *  This method ignores any coupling between phase planes.
     * 
     *  TODO - Make the method consider the general case of coupling between phase planes
     *  and return the Twiss parameters as projections that one would observe in 
     *  experiments.
     *
     *  @return     array of Twiss with length 3 where
     *              array[0] = Twiss parameters in x plane
     *              array[1] = Twiss parameters in y plane
     *              array[2] = Twiss parameters in z plane
     */
    public Twiss[] computeTwiss() {
    //        return twissParameters(this.phaseCorrelation());
        CovarianceMatrix matSig = this.computeCentralCovariance();
	
        double[] arrEmit; // array of rms emittance values

        arrEmit = computeRmsEmittances();

        // Compute the X plane twiss parameters
        double ax, bx, ex; // x plane twiss parameters
        Twiss twissX; // twiss parameter object

        ex = arrEmit[0];
        bx = matSig.getElem(0, 0) / ex;
        ax = -matSig.getElem(0, 1) / ex;
        twissX = new Twiss(ax, bx, ex);

        // Compute the Y plane twiss parameters
        double ay, by, ey; // y plane twiss parameters
        Twiss twissY; // twiss parameter object
        ey = arrEmit[1];
        by = matSig.getElem(2, 2) / ey;
        ay = -matSig.getElem(2, 3) / ey;
        twissY = new Twiss(ay, by, ey);

        // Compute the Z plane twiss parameters
        double az, bz, ez; // z plane twiss parameters
        Twiss twissZ; // twiss parameter object

        ez = arrEmit[2];
        bz = matSig.getElem(4, 4) / ez;
        az = -matSig.getElem(4, 5) / ez;
        twissZ = new Twiss(az, bz, ez);

        return new Twiss[] { twissX, twissY, twissZ };
    }
    
    
    /*
     * Object Overrides
     */
    
    /**
     * Creates and returns a deep copy of this matrix.
     *
     * @see xal.tools.math.BaseMatrix#clone()
     *
     * @author Christopher K. Allen
     * @since  Jul 3, 2014
     */
    @Override
    public CovarianceMatrix clone() {
        return new CovarianceMatrix(this);
    }

    
    /*
     * Matrix Operations
     */


    /*
     * Support Methods
     */

    /**
     * Check matrix for symmetry. This method uses the number of Units
     * in the Last Place (ULPs) to round numbers when comparing.  For specific
     * use of the ULPs bracketing procedure see <code>{@link ElementaryFunction#approxEq(double,double)}</code>. 
     * 
     * @param  matCov   <code>CovarianceMatrix</code> object to check
     * @param  cntUlps  number of ULPs of tolerance when comparing two numbers for equivalence
     * 
     * @return     true if symmetric, false if not
     * 
     * @version Jan 4, 2016,  Christopher K Allen
     * 
     * @see ElementaryFunction#approxEq(double, double)
     *
     */
    @SuppressWarnings("unused")
    private boolean checkSymmetryToUlps(CovarianceMatrix matCov, int cntUlps)    {
        int     i,j;        //loop control variables

        for (i=0; i<7; i++)
            for (j=i+1; j<7; j++) {
                double  dblValUp = this.getElem(i, j);
                double  dblValLw = this.getElem(j, i);
                
                if ( !ElementaryFunction.approxEq(dblValUp, dblValLw, cntUlps) )
                    return false;
            }

        return true;
    }
    
    /**
     * Checks the symmetric of the given covariance matrix to <i>N</i> significant digits
     * of accuracy.  Specifically, when comparing to opposing off-diagonal elements  
     * the first <i>N</i> digits behind the decimal must be equal for equivalence; any
     * differing digits beyond that are irrelevant.  (Clearly the exponents of the off-diagonals
     * under comparison must be equal.)
     * 
     * @param matCov        covariance matrix under test
     * @param cntDigits     number <i>N</i> of comparison digits 
     * 
     * @return              <code>true</code> if the off-diagonal elements of the matrix compare to <i>N</i> digits,
     *                      <code>false</code> otherwise
     *
     * @since  Jan 4, 2016,   Christopher K. Allen
     * 
     * @see ElementaryFunction#significantDigitsEqs(double, double, int)
     */
    private boolean checkSymmetryToSigDigits(CovarianceMatrix matCov, int cntDigits) {
        int     i,j;        //loop control variables

        for (i=0; i<7; i++)
            for (j=i+1; j<7; j++) {
                double  dblValUp = this.getElem(i, j);
                double  dblValLw = this.getElem(j, i);
                
                if ( !ElementaryFunction.significantDigitsEqs(dblValUp, dblValLw, cntDigits) )
                    return false;
            }

        return true;
    }
    
//    private double  enforceSymmetry() {
//        
//        for (int i=0; i<INT_SIZE; i++)
//            for (int j=i+1; j<INT_SIZE; j++) {
//                double  dblValUp = this.getElem(i, j);
//                double  dblValDn = this.getElem(j, i);
//                
//                double  dblAvg = (dblValUp + dblValDn)/2.0;
//                Double  dblErr = (dblValUp - dblValDn)/dblAvg;
//                
//                if (dblErr.isInfinite())
//            }
//        
//    }

//	/**
//     * Handles object creation required by the base class. 
//	 *
//	 * @see xal.tools.beam.PhaseMatrix#newInstance()
//	 *
//	 * @author Ivo List
//	 * @author Christopher K. Allen
//	 * @since  Jun 17, 2014
//	 */
//	@Override
//	protected PhaseMatrix newInstance() {
//		return new PhaseMatrix();
//	}

    /**
     * Handles object creation required by the base class. 
     *
     * @see xal.tools.beam.PhaseMatrix#newInstance()
     *
     * @author Ivo List
     * @author Christopher K. Allen
     * @since  Jun 17, 2014
     */
    @Override
    protected CovarianceMatrix newInstance() {
        return new CovarianceMatrix();
    }
}
