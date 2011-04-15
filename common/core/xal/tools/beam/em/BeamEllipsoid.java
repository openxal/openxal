/*
 * Created on Sep 15, 2003
 * Modified:
 *      9/03    - CKA: added rotation into ellipsoid coordinates
 *     11/03    - CKA: added full space charge matrix generation capabilities
 *     10/06    - CKA: updated architecture, corrected calculation adding 
 *                     Lorentz transform and Jacobi iteration for SO(3) rotation
 *                     matrix
 *                     Renamed from EllipsoidalCharge to BeamEllipsoid
 */
package xal.tools.beam.em;


import xal.tools.beam.CorrelationMatrix;
import xal.tools.beam.PhaseIndexHom;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;

import xal.tools.math.EllipticIntegral;
import xal.tools.math.r3.R3x3;
import xal.tools.math.r3.R3x3JacobiDecomposition;


/**
 * <p>
 * Encapsulates the properties of a ellipsoidally symmetric distribution
 * of charge in 6D phase space, in particular, the electromagnetic properties.
 * Provides convenience methods for creating arbitrarily oriented ellipsoids
 * and determining their fields, and their effects on beam particles, 
 * specifically, in the form of a linear transfer matrix generator.
 * </p>
 * <p>
 * The ellipsoid is assumed to be moving in the axial (z-axis) direction
 * in the laboratory frame.  All relativistic calculations are made with this
 * assumption.  In particular, the gamma (relativistic factor) is taken
 * with respect to this axis.
 * </p>
 * <p>
 * The reference ellipsoid in three-space is represented by the equation
 * <br/>
 * <br/>
 * &nbsp; &nbsp;     <b>r</b>'<b>&sigma;</b><sup>-1</sup><b>r</b> = 1
 * <br/>
 * <br/>
 * where <b>&sigma;</b> is the 3&times;3 matrix of spatial moments, 
 * <b>r</b> &equiv; (<i>x y z</i>) is the position vector in R3, 
 * and the prime indicates transposition.  
 * </p>
 * <p>
 * The matrix <b>&sigma;</b> is taken from the 7&times;7 covariance 
 * matrix <b>&tau;</b>
 * in homogeneous coordinates; it is the matrix formed from the six, 
 * second-order spatial moments &lt;<i>xx</i>&gt;, &lt;<i>xy</i>&gt;, 
 * &lt;<i>xz</i>&gt;, &lt;<i>yy</i>&gt;, &lt;<i>yz</i>&gt;, &lt;<i>zz</i>&gt; and is
 * arranged as follows:
 * <br/>
 * <br/>
 * <table>
 *   <tr>
 *     <td>&nbsp;</td> <td>&nbsp;</td> <td>| &lt;<i>xx</i>&gt; &lt;<i>xy</i>&gt; &lt;<i>xz</i>&gt; |</td>
 *   </tr>
 *   <tr>
 *     <td><b>&sigma;</b></td> <td> = </td> <td>| &lt;<i>xy</i>&gt; &lt;<i>yy</i>&gt; &lt;<i>yz</i>&gt; |</td>
 *   </tr>
 *   <tr>
 *     <td>&nbsp;</td> <td>&nbsp;</td> <td>| &lt;<i>xz</i>&gt; &lt;<i>yz</i>&gt; &lt;<i>zz</i>&gt; |</td>
 *   </tr>
 * </table>
 * <br/>
 * <br/>
 * Note that <b>&sigma;</b> must be symmetric and  positive definite.  
 * Thus, it is diagonalizable with the decomposition
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <b>&sigma;</b> = <b>R&Lambda;R</b>'
 * <br/>
 * <br/>
 * where the prime indicates transposition, <b>R</b> &isin; <i>SO</i>(3) is the orthogonal 
 * rotation matrix and <b>&Lambda;</b> is the diagonal matrix of real eigenvalues.  
 * Note from the above that these eigenvalues 
 * are the squares of the ellipsoid semi-axes.
 * </p>
 *      
 * @author Christopher K. Allen
 */

public class BeamEllipsoid {


    /*
     * Global Constants
     */
    
    
    /** distribution dependence factor - use that for the uniform beam */
    public static final double     CONST_UNIFORM_BEAM = Math.pow(5.0, 1.5);


    /*
     * Local Attributes
     */
     
     
    /** relativitic factor */
     private double             dblGamma = 1.0;
     
    /** Correlation matrix from which the ellipsoid charge is built */
    private CorrelationMatrix   tauLab;

    /** Correlation matrix in the stationary beam frame - laboratory coordinates */
    private CorrelationMatrix   tauBeam;

    /** Lorentz transform to beam coordinates - assuming propagation in z-direction */
    private PhaseMatrix         matLorentz;
    
    /** phase space translation matrix to beam centroid coordinates */
    private PhaseMatrix         matTranslate;
    
    /** phase space rotation in SO(6)x{1} to natural beam ellipsoid coordinates */
    private PhaseMatrix         matRotate;
    
    /** three-array of the diagonalized spatial covariance matrix */
    private double[]            arrMoments;
    
    /** array of normalized space-charge defocusing lengths */
    private double[]            arrDefocus;
    
    
    

    /*
     *  Initialization
     */
     
    /** 
     * <p>
     * Construct a beam charge density ellipsoid described
     * by the phase space correlation matrix <code>tauLab</code> and relativistic
     * factor <code>gamma</code>. 
     * </p>
     * <p>
     * The correlation matrix <code>tauLab</code> is taken in laboratory coordinates
     * in the laboratory frame.  The beam is assumed to be moving in the axial
     * (z-axis) direction in the laboratory frame with relativistic factor 
     * <code>dblGamma</code>.  
     * </p>
     * <p>
     * Note that the phase space correlation matrix in homogeneous coordinates
     * contains all moments up to and including second order; this includes the
     * first-order moments that describe the displacement of the ellipsoid from
     * the coordinate origin.  Thus, from the 7x7 phase space correlation matrix
     * we extract the displacement vector
     * <br/>
     * <br/>
     * &nbsp; &nbsp;  (&lt;x&gt;,&lt;y&gt;&lt;z&gt;)
     * <br/>
     * <br/>
     * and the 3&times;3 covariance matrix <b>&sigma;</b>
     * <br/>
     * <br/>
     * <table>
     *   <tr>
     *     <td>&nbsp;</td> <td>&nbsp;</td> <td>| &lt;x*x&gt; &lt;x*y&gt; &lt;x*z&gt; |</td> <td>&nbsp;</td> <td>| &lt;x&gt;*&lt;x&gt; &lt;x&gt;*&lt;y&gt; &lt;x&gt;*&lt;z&gt; |</td>
     *   </tr>
     *   <tr>
     *     <td><b>&sigma;</b></td> <td>&equiv;</td> <td>| &lt;y*x&gt; &lt;y*y&gt; &lt;y*z&gt; |</td> <td>-</td>  <td>| &lt;y&gt;*&lt;x&gt; &lt;y&gt;*&lt;y&gt; &lt;y&gt;*&lt;z&gt; |</td>
     *   </tr>
     *   <tr>
     *     <td>&nbsp;</td> <td>&nbsp;</td> <td>| &lt;z*x&gt; &lt;z*y&gt; &lt;z*z&gt; |</td> <td>&nbsp;</td> <td>| &lt;z&gt;*&lt;x&gt; &lt;z&gt;*&lt;y&gt; &lt;z&gt;*&lt;z&gt; |</td>
     *   </tr>
     * </table>
     * <br/>
     * <br/>
     * to construct the ellipsoidal charge object according to the class documentation
     * 
     *  @param  dblGamma        relativistic factor
     *  @param  tauLab          envelope correlation matrix in homogeneous phase space coordinates
     * 
     *  @author Christopher K. Allen
     * 
     */
    public BeamEllipsoid(double dblGamma, CorrelationMatrix tauLab)    {
        
        // Save the arguments
        this.dblGamma = dblGamma;
        this.tauLab = tauLab;
        
        
        // Compute Lorentz transform and perform transform to beam frame
        this.matLorentz = this.computeLorentzMatrix(dblGamma);
        this.tauBeam    = this.computeLorentzTransform(tauLab);

        
        // Build the translation matrix
        this.matTranslate = this.computeTranslation(this.tauBeam);
        
        
        // Decompose the beam ellipsoid
        R3x3                    matCov    = tauBeam.computeSpatialCovariance();
        R3x3JacobiDecomposition decompCov = new R3x3JacobiDecomposition(matCov);
        
        R3x3    matRot3 = decompCov.getRotationMatrix().transpose();
        
        this.matRotate  = PhaseMatrix.rotationProduct(matRot3);
        this.arrMoments = decompCov.getEigenvalues();
        
        
        // Compute the normalized space-charge defocusing lengths
        this.arrDefocus = this.compDefocusConstants(this.arrMoments);
    };

    
    
    
    /*
     * Property Queries
     */
     
    /**
     * Return the relativistic parameter for the ellipoidal charge
     * distribution.
     * 
     * @return  relativistic parameter = sqrt(1 + v^2/c^2)
     */
    public double   getGamma()      { return this.dblGamma; };
    
    /**
     * Return the original correlation matrix for the beam in the 
     * laboratory coordinates.
     * 
     * @return      beam correlation matrix in laboratory frame
     */
    public CorrelationMatrix  getCorrelationLab() { return this.tauLab; };
    
    
    /**
     * Return the value of the first ellipsoid second spatial moment in the
     * stationary beam frame and aligned to the coordinate axes.
     * 
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     *  
     *  @return     seond moment <x*x>
     */
    public double   get2ndMomentX() { return this.arrMoments[0]; };
    
    /**
     * Return the value of the second ellipsoid second spatial moment in the
     * stationary beam frame and aligned to the coordinate axes.
     * 
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     *  
     *  @return     seond moment <y*y>
     */
    public double   get2ndMomentY() { return this.arrMoments[1]; };
    
    /**
     * Return the value of the third ellipsoid second spatial moment in the
     * stationary beam frame and aligned to the coordinate axes.
     * 
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     *  
     *  @return     seond moment <z*z>
     */
    public double   get2ndMomentZ() { return this.arrMoments[2]; };
    
    /**
     * Return all the ellipsoid second spatial moments in the
     * stationary beam frame and aligned to the coordinate axes.
     * 
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     *  
     *  @return     three-arry (<x*x>,<y*y>,<z*z>) second moments
     */
    public double[] get2ndMoments() { return this.arrMoments; };
    
    /**
     *  Return the value of the first ellispoid semi-axis in the
     *  stationary beam frame.  The first value of <code>getSemiAxes()</code>.
     *  
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     *  
     *  @return     1st semi-axis value
     *  
     *  @see        BeamEllipsoid#getSemiAxes()
     */     
    public double   getSemiAxisX()  { return Math.sqrt( this.arrMoments[0] ); };

    /**
     *  Return the value of the second ellispoid semi-axis in the 
     *  stationary beam frame.  The second value of <code>getSemiAxes()</code>.
     *  
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     *  
     *  @return     2nd semi-axis value
     *  
     *  @see        BeamEllipsoid#getSemiAxes()
     */     
    public double   getSemiAxisY()  { return Math.sqrt( this.arrMoments[1] ); };

    /**
     *  Return the value of the third ellispoid semi-axis in the
     *  stationary beam frame.  The third value of <code>getSemiAxes()</code>.
     *  
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     *  
     *  @return     3rd semi-axis value
     *  
     *  @see        BeamEllipsoid#getSemiAxes()
     */     
    public double   getSemiAxisZ()  { return Math.sqrt( this.arrMoments[2] ); };

    /**
     *  Return all the ellipsoid semi-axes lengths as an array.
     *  Note that these semi-axes are the values in the stationary
     *  beam frame.
     *  
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     * 
     *  @return     array (a,b,c) of ellipsoid semi-axes
     */
    public double[] getSemiAxes()       {
        double[]    arrSemiAxes = new double[3];
        
        arrSemiAxes[0] = this.getSemiAxisX();
        arrSemiAxes[1] = this.getSemiAxisY();
        arrSemiAxes[2] = this.getSemiAxisZ();

        return arrSemiAxes;
    }
    
    
    /**
     * Return the 1st normalized space charge defocusing constant.
     * 
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     *  
     *  @return     1st nomalized defocusing constant knx
     *  
     *  @see    BeamEllipsoid#getDefocusingConstants()
     */
    public double   getDefocusingConstantX()    { return this.arrDefocus[0]; };
    
    /**
     * Return the 2nd normalized space charge defocusing constant.
     * 
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     *  
     *  @return     2nd nomalized defocusing constant knx
     *  
     *  @see    BeamEllipsoid#getDefocusingConstants()
     */
    public double   getDefocusingConstantY()    { return this.arrDefocus[1]; };
    
    /**
     * Return the 3rd normalized space charge defocusing constant.
     * 
     *  NOTE
     *  The after rotation the (x,y,z) coordinate are somewhat arbitrary
     *  and no longer coincide with the original laboratory coordinates.
     *  
     *  @return     3rd nomalized defocusing constant knz
     *  
     *  @see    BeamEllipsoid#getDefocusingConstants()
     */
    public double   getDefocusingConstantZ()    { return this.arrDefocus[2]; };
    
    /**
     * Return all the normalized space-charge defocusing constants
     * for the beam ellipsoid.  These are the inverses of the normalized defocal
     * lengths (fnx,fny,fnz) and are used to construct the space charge 
     * generator matrix and space charge transfer matrix.  The unnormalized 
     * focal lengths <i>f</i> are given by
     * 
     *      f = ds*K*fn
     *      
     * where <i>ds</i> is the increment path length over which the space charge 
     * kick is being applied, <i>K</i> is the generalized (3D) beam perveance,
     * and <i>fn</n> is the normalized defocal length.  The normalized defocal 
     * lenth is given by
     * 
     *      fn = 1/kn^2
     *      
     * where <i>kn^2</i> is the normalized (squared) focusing constant, i.e., the value
     * returned by this method.
     * 
     * @return  three-array (knx^2, kny^2, knz^2) of normalized defocusing constants
     */
    public double[] getDefocusingConstants()    {
        return this.arrDefocus;
    }

    
    /**
     * Return the correlation matrix for the beam in the 
     * stationary beam coordinates.
     * 
     * @return      beam correlation matrix in beam frame
     */
    public CorrelationMatrix  getCorrelationBeam() { 
        return this.tauBeam; 
    };
    
    /**
     * Get the Lorentz transform matrix which takes the laboratory coordinates
     * to the beam coordinates.  
     * 
     * Note that the laboratory coordinates actually move with the beam centroid
     * so they are note truly stationary w.r.t. to the machine.  Thus, the 
     * Lorentz transform provided here does not include any translation effects,
     * only the effects of length contraction and time dilation.
     * 
     * @return      relativistic Lorentz transform matrix from lab to beam coordinates
     */
    public PhaseMatrix  getLorentzTransform()  {
        return this.matLorentz;
    }
    
    /**
     * Return the translation matrix which transforms coordinates in the beam
     * frame to those with the ellipsoid centroid as the coordinate origin.
     * 
     * Note that because we are using homogeneous coordiates Galilean translations
     * may be performed with matrix multiplications.
     * 
     * @return  translation matrix moving the coordinate origin to the beam centroid
     */
    public PhaseMatrix  getTranslation()    {
        return this.matTranslate;
    }
    
    /**
     * Get orthogonal rotation matrix <b>R</b> in SO(7) that rotates the ellipsoid
     * spatial semi-axes onto the spatial coordinate axes.
     * 
     * The rotation <b>R</b> is actually the cartesian product of a single rotation 
     * <b>r</b> from SO(3) that rotates the ellipsoid's spatial coordinates onto the 
     * coordinate axes.  That is,
     * 
     *      <b>R</b> = <b>r</b>x<b>r</b> contained in SO(7) contained in R7x7
     *      
     * In this manner the momentum coordinates at each point (x,y,z) are rotated 
     * by an equal amount and so velocities are mapped accordingly.
     * 
     * @return  rotation matrix in SO(7) moving the ellipsoid into standard position
     */
    public PhaseMatrix getRotation()   {
        return this.matRotate;
    }
    

    /**
     * Return the complete transformation from the laboratory inertial
     * coordinates to the ellipsoid inertial coordinates.  The tranform takes coordinates
     * in the laboratory frame to the natural coordinates of the ellipsoid - this 
     * coordinate system has the centroid as the origin and the ellipsoid semi-axes
     * are aligned to the coordinate axes.  Denoting the returned transformation as
     * <b>M</b>, then it is composed of the following factors
     * 
     *      <b>M</b> = <b>R0*T0*L0</b>
     *      
     * where <b>L0</b> is the Lorentz transform into the beam frame, <b>T0</b> is
     * the Galilean transform to the ellipsoid centroid coordinates, and <b>R0</b>
     * is the rotation that aligns the ellipsoid semi-axes to the coorinates axes 
     * putting it into standard position.
     * 
     * @return  tranformation taking lab inertial coordinates to beam inertial coordinates
     * 
     * @see BeamEllipsoid#getLorentzTransform()
     * @see BeamEllipsoid#getTranslation()
     * @see BeamEllipsoid#getRotation()
     */
    public PhaseMatrix  getLabToBeamTransform() {

        // Get transform matrices
        PhaseMatrix     L0 = this.getLorentzTransform();
        PhaseMatrix     T0 = this.getTranslation();
        PhaseMatrix     R0 = this.getRotation();
        
        // Build the transform to ellipsoid coordinates in beam frame
        PhaseMatrix     M  = R0.times(T0.times(L0));

        return  M;
    }
    
    /** 
     * Calculates the transfer matrix generator for space charge effects from
     * this <code>BeamEllipsoid</code> object given the generalized beam 
     * three-dimensional perveance <code>dblPerveance</code>.  
     * 
     * Denoting the returned generator matrix generator as <b>G</b> then the
     * actual transfer matrix <b>M</b>(s) for the space charge effect is given as
     * 
     *      <b>M</b>(s) = exp(s<b>G</b>)
     * 
     * where <i>s</i> is the incremental path length for the dynamics. 
     * 
     * Note that to obtain this matrix a linear fit to the true fields was 
     * performed where the regression is weighted by the distribution itself.
     * According the "Equivalent Beam" principle by Sacherar this regression
     * then only loosely couples to the actual distribution profile of the the
     * ellipsoidal charge.  For computational purposes we have assumed a uniform
     * density ellipsoid, but that is of no real consequence practically.
     * 
     * The resulting generator matrix is the product of the generator matrix 
     * <b>G0</b> in the ellipsoid coordinate (which has a very simple form), and
     * a series of matrix transforms.  We have
     * 
     *      G = (R0*T0*L0)^-1 * G0 * (R0*T0*L0)
     *      
     * where <b>L0</b> is the Lorentz transform into the beam frame, <b>T0</b> is
     * the Galilean transform to the ellipsoid centroid coordinates, and <b>R0</b>
     * is the rotation that aligns the ellipsoid semi-axes to the coorinates axes 
     * putting it into standard position.
     * 
     * NOTES:
     * One should provide the three-dimensional value for the generalized beam perveance.
     * This value <i>K</i> is defined as
     * 
     *      K = (Q/(2*pi*eps0))*(1/(gamma^3*beta^2))*(q/(m*c^2))
     *      
     * where <i>Q</i> is the total beam charge, <i>eps0</i> is the permittivity of
     * free space, <i>gamma</i> is the relativistic factor, <i>beta</i> is the 
     * normalized velocity (to the speed of light), <i>q</i> is the unit charge,
     * <i>m</i> is the beam particle mass, and <i>c</i> is the speed of light.
     *
     * @param   dblPerveance    generalized three-dimensional beam perveance
     * 
     * @return  transfer matrix generator representing linear space charge effects
     * 
     * @author Christopher K. Allen
     * 
     * @see BeamEllipsoid#computeScheffMatrix
     */
    public PhaseMatrix computeScheffGenerator(double dblPerveance)    {
        
        // Check for pathelogical zero-space charge case
        if (dblPerveance == 0.0) 
            return PhaseMatrix.identity();

        
        // Compute generator matrix and transform it to laboratory frame 
        PhaseMatrix     M  = this.getLabToBeamTransform();
        PhaseMatrix     Mi = M.inverse(); 

        PhaseMatrix     G0 = this.buildScheffGeneratorLocal(dblPerveance);
        PhaseMatrix     G = Mi.times(G0.times(M));
        
        return G;
    };
    
    /**
     * Compute and return the transfer matrix for space charge effects due to 
     * this beam ellipsoid for the given incremental path lenth <code>dblLen</code>
     * and generalized beam <code>dblPerveance</code>.
     * 
     * Note that the returned matrix <b>M</b> has the form
     * 
     *      <b>M</b>(s) = exp(s<b>G</b>)
     * 
     * where <i>s</i> is the incremental path length for the dynamics, and <b>G</b>
     * is the generator matrix generator for the transform.  This generator matrix
     * is returned by the method <code>BeamEllipsoid#computeScheffGenerator</code>,
     * thus, we could compute the transfer matrix <b>M</b> simply by exponentiating
     * the returned value.  However, because the generator matrix in the ellipsoid 
     * coordinates, <b>G0</b>, is idempotent (i.e., <b>G0*G0</b> = 0) it is
     * computationally faster to assemble the transfer matrix as the product
     * 
     *      <b>M</b>(s) = exp(s<b>G</b>) = (R0*T0*L0)^-1 * (I + ds*G0) * (R0*T0*L0)
     * 
     * where <b>L0</b> is the Lorentz transform into the beam frame, <b>T0</b> is
     * the Galilean transform to the ellipsoid centroid coordinates, and <b>R0</b>
     * is the rotation that aligns the ellipsoid semi-axes to the coorinates axes 
     * putting it into standard position.
     * 
     * NOTES:
     * One should provide the three-dimensional value for the generalized beam perveance.
     * This value <i>K</i> is defined as
     * 
     *      K = (Q/(2*pi*eps0))*(1/(gamma^3*beta^2))*(q/(m*c^2))
     *      
     * where <i>Q</i> is the total beam charge, <i>eps0</i> is the permittivity of
     * free space, <i>gamma</i> is the relativistic factor, <i>beta</i> is the 
     * normalized velocity (to the speed of light), <i>q</i> is the unit charge,
     * <i>m</i> is the beam particle mass, and <i>c</i> is the speed of light.
     *
     * @param   dblLen          incremental path length for the space charge effects
     * @param   dblPerveance    generalized three-dimensional beam perveance
     * 
     * @return  transfer matrix representing linear space charge effects
     * 
     * @author Christopher K. Allen
     * 
     * @see BeamEllipsoid#computeScheffGenerator
     */
    public PhaseMatrix  computeScheffMatrix(double dblLen, double dblPerveance) {
        
        // Check for pathelogical zero-space charge case
        if (dblPerveance == 0.0) 
            return PhaseMatrix.identity();

        
        // Compute the laboratory/beam frame transform 
        PhaseMatrix     M  = this.getLabToBeamTransform();
        PhaseMatrix     Mi = M.inverse(); 

        // compute defocusing constants
        double dblMagScheff = dblLen*dblPerveance;
        
        double kx = dblMagScheff*this.getDefocusingConstantX();
        double ky = dblMagScheff*this.getDefocusingConstantY();
        double kz = dblMagScheff*this.getDefocusingConstantZ();
        
        // Build the transfer matrix and transform it to laboratory frame 
        PhaseMatrix     F0  = PhaseMatrix.identity();
        F0.setElem(PhaseIndexHom.Xp, PhaseIndexHom.X, kx);
        F0.setElem(PhaseIndexHom.Yp, PhaseIndexHom.Y, ky);
        F0.setElem(PhaseIndexHom.Zp, PhaseIndexHom.Z, kz);

        PhaseMatrix     F = Mi.times(F0.times(M));
        
        return F;
    }
    
    
    
    /*
     * Internal Support
     */
    
    
    /**
     * Compute and save the Lorentz transform matrix.  Note that the laboratory
     * frame is moving with the beam centroid, but is not the inertial frame
     * of the beam.  Thus, the Lorentz transform is somewhat special in that
     * there is no translation involved.  
     * 
     * @param   dblGamma    the relativistic factor
     * 
     * @return  Lorentz transform matrix
     */
    private PhaseMatrix computeLorentzMatrix(double dblGamma)   {
        PhaseMatrix matLorentz;
        
        matLorentz = PhaseMatrix.identity();
        matLorentz.setElem(PhaseIndexHom.Z,  PhaseIndexHom.Z,  dblGamma);
//        matLorentz.setElem(PhaseIndexHom.Zp, PhaseIndexHom.Zp, 1.0/dblGamma);
        matLorentz.setElem(PhaseIndexHom.Zp, PhaseIndexHom.Zp, dblGamma);
        
        return matLorentz;
    }
    
    /**
     * Compute and return the beam correlation matrix <code>tauBeam</code> in 
     * the beam frame.  The computation is a simple matrix transpose 
     * conjugation with the Lorentz transform matrix.
     * 
     * @param   tauLab  correlation matrix in the (moving) laboratory frame
     *  
     * @return          correltation matrix in the (stationary) beam frame
     */
    private CorrelationMatrix computeLorentzTransform(CorrelationMatrix tauLab) {
        PhaseMatrix L   = this.getLorentzTransform();
        
        PhaseMatrix         matTauBF = tauLab.conjugateTrans(L);
        CorrelationMatrix   tauBeam  = new CorrelationMatrix(matTauBF);
        
        return tauBeam;
    }
    
    /**
     * Compute and return the translation matrix which moves phase coordinates
     * in the beam frame to their values with respect to the centroid location
     * of the beam ellipsoid.  
     * 
     * Note that since we are using homogeneous coordinates, coordinate translations
     * (Galilean transforms) can be represented by matrix multiplications.
     * 
     * @param   tauBeam       correlation matrix in the beam frame
     * 
     * @return              translation transform matrix to beam centroid coordinates 
     */
    private PhaseMatrix computeTranslation(CorrelationMatrix tauBeam) {
        PhaseVector vecCent  = tauBeam.getMean();
        PhaseVector vecTrans = vecCent.negate();
        PhaseMatrix matTrans = PhaseMatrix.translation(vecTrans); 

        return matTrans;
    }
    


    /**
     * Computes the normalized defocusing constants (squared) for a space charge kick 
     * given the second moments of the beam ellipsoid.  The true defocusing 
     * constants are obtained by multiplying the result by the factor K*ds, i.e., 
     * 
     *   k^2 = kn^2*K*ds
     * 
     * where <i>k^2</i> is the true defocusing constant, <i>kn^2</i> is the normalized 
     * defocusing constant(returned by this method), <i>K</i> is the generalized beam
     * pervenace, and <i>ds</i> is the incremental pathlength 
     * over which the kick is being applied.
     * 
     * The defocusing constants (knx,kny,knz) are given for the cartesian 
     * coordinate system (x,y,z) which is aligned to the ellipsoid semi-axes.
     * If the ellipsoid is rotated with respect to the coordinate system in 
     * which the ellipsoid was defined (constructed), then any transfer matrix
     * built from these focusing constants must be rotated into the original
     * coordinate system.
     * 
     * In the natural coordinates of the ellipsoid the normalized focusing constants
     * <i>kn</i> are related to the space charge defocusing lengths <i>f</i> by the
     * formula
     * 
     *      f = 1/(ds*K*kn^2)
     *      
     * From an optics standpoint, we are essentially computing the defocusing
     * lengths due to space charge forces here.
     * 
     * The defocusing constants are computed from a weighted linear
     * regression of the true fields generated by an ellipsoidally symmetric 
     * charge distribution.  By the equivalent uniform beam principle 
     * the space charge effects (to second order) are only loosely
     * coupled to the actual profile of the distribution (assuming that 
     * it is ellipsoidally symmetric).  The effect from the distribution
     * profile manifests itself as a factor, we take this factor to be that
     * for a uniform ellipsoid for computational purposes.
     * 
     * @param   arrMoments  three-array (<x*x>,<y*y>,<z*z>) of ellipsoid spatial second moments
     * 
     * @return              three-array (knx^2,kny^2,knz^2) of defocusing constants
     * 
     * @see <a href="http://lib-www.lanl.gov/cgi-bin/getfile?00796950.pdf">Theory and Technique
     *      of Beam Envelope Simulation</a>
     * 
     * @author  Christopher K. Allen
     */
    private double[] compDefocusConstants(double[] arrMoments)    {

        // Get relativistic factor, and factor squared
        double  gamma   = this.getGamma();

        
        // Get the second-order spatial moments
        double  a_2 = arrMoments[0];
        double  b_2 = arrMoments[1];
        double  c_2 = arrMoments[2];
        
        
        // Compute the Carlson elliptic integral values
        double ellipticX = EllipticIntegral.RD(b_2, c_2, a_2);
        double ellipticY = EllipticIntegral.RD(c_2, a_2, b_2);
        double ellipticZ = EllipticIntegral.RD(a_2, b_2, c_2);

        
        // Compute (de)focusing strengths from space charge
        double Knx = (gamma*ellipticX)/CONST_UNIFORM_BEAM;
        double Kny = (gamma*ellipticY)/CONST_UNIFORM_BEAM;
        double Knz = (gamma*ellipticZ)/CONST_UNIFORM_BEAM;

        return new double[] {Knx, Kny, Knz};
    }

    /**
     * This method is provided as a comparison utility for validation against simulation
     * with Trace3D.  Trace3D uses an approximation to the elliptic integrals encountered
     * in the space charge field expressions.  These approximations break down to first order
     * as the beam ellipsoid becomes increasingly eccentric in the transverse direction.
     * Moreover, there is a preferred direction in these approximations, namely, the 
     * axial direction.  The approximation is most valid for the situation where the beam
     * is aligned to the axial direction.  Thus, for arbitrarily rotated ellipsoids, the 
     * approximation becomes less valid.
     *   
     * @param   arrMoments  three-array (&lt;xx&gt;,&lt;yy&gt;,&lt;zz&gt;) of ellipsoid spatial second moments
     * 
     * @return  three-array (knx^2,kny^2,knz^2) of defocusing constants
     * 
     * @see #compDefocusConstants
     */
    @SuppressWarnings("unused")
    private double[]    compDefocusConstantsAlaTrace3D(double[] arrMoments)    {

        // Get relativistic factor, and factor squared
        double  gamma   = this.getGamma();
  //      double  gamma_2 = gamma*gamma;

        // Get the second-order spatial moments
        double  a_2 = arrMoments[0];
        double  b_2 = arrMoments[1];
        double  c_2 = arrMoments[2];
        
        // Get the semi-axes
        double  a   = Math.sqrt(a_2);
        double  b   = Math.sqrt(b_2);
        double  c   = Math.sqrt(c_2);
        
        // Compute the form factor terms
        double  s   = c/Math.sqrt(a*b);
        double  xi  = EllipticIntegral.formFactorD(s);
       
       
        // Compute the Carlson elliptic integral approximate values
        double approxRdX = (3.0/(a*c))*(1.0/(a+b))*(1.0 - xi);
        double approxRdY = (3.0/(b*c))*(1.0/(a+b))*(1.0 - xi);
        double approxRdZ = (3.0/(a*b*c))*xi; 

        
        // Compute (de)focusing strengths from space charge
//        double KnX = (gamma_2*approxRdX)/CONST_UNIFORM_BEAM;
//        double KnY = (gamma_2*approxRdY)/CONST_UNIFORM_BEAM;
//        double KnZ = (gamma_2*approxRdZ)/CONST_UNIFORM_BEAM;
        
        double KnX = (gamma*approxRdX)/CONST_UNIFORM_BEAM;
        double KnY = (gamma*approxRdY)/CONST_UNIFORM_BEAM;
        double KnZ = (gamma*approxRdZ)/CONST_UNIFORM_BEAM;
        
        return new double[] {KnX, KnY, KnZ};
    }


    /**
     * Build and return the space charge generator matrix in the local coordinates
     * of the ellipsoid.  This matrix has the simple form
     * 
     * 
     * 
     *                   | 0  0 0  0 0  0 0 |
     *                   | Kx 0 0  0 0  0 0 |
     *      <b>G0</b> =  | 0  0 0  0 0  0 0 |
     *                   | 0  0 Ky 0 0  0 0 |
     *                   | 0  0 0  0 0  0 0 |
     *                   | 0  0 0  0 Kz 0 0 |
     *                   | 0  0 0  0 0  0 0 |
     *                   
     * where <i>Kx</i>, <i>Ky</i>, <i>Kz</i> are the defocusing constants.  These
     * constants have the value (for example, in the x direction)
     * 
     *      Kx = ds*K*knx^2
     *      
     * where <i>ds</i> is the incremental path length, <i>K</i> is the beam perveance,
     * and <i>knx^2</i> is the normalized defocusing constant.  
     * 
     * The argument of this method is the magnitude of the space charge effect, which is
     * simply the product of <i>ds</i> and <i>K</i>.
     * 
     * @return      space charge generator matrix in ellipsoid coordinates
     */
    private PhaseMatrix buildScheffGeneratorLocal(double dblMagScheff) {

        // compute defocusing constants
        double kx = dblMagScheff*this.getDefocusingConstantX();
        double ky = dblMagScheff*this.getDefocusingConstantY();
        double kz = dblMagScheff*this.getDefocusingConstantZ();
        
        // Build the generator matrix in the ellipsoid coordinates
        PhaseMatrix     G0 = PhaseMatrix.zero();
        
        G0.setElem(PhaseIndexHom.Xp, PhaseIndexHom.X, kx);
        G0.setElem(PhaseIndexHom.Yp, PhaseIndexHom.Y, ky);
        G0.setElem(PhaseIndexHom.Zp, PhaseIndexHom.Z, kz);

        return G0;
    }
    
}






/*
 * Storage
 */

//
///**
// * Return the generalized beam perveance of the ellipsoidal charge.
// */
//public double   getBeamPerveance()  { return this.dblPerveance; };
//


///**
// * Compute the rotation in SO(7), corresponding to the given SO(3) rotation, 
// * which aligns the beam ellipsoid to the laboratory coordinate axes.  
// * 
// * NOTE:
// * The rotation takes place in the beam's inertial frame.
// * 
// * The returned rotation is the cartesian product of the given rotation, one
// * for the spatial coordinates and one for the momentum coordinates.
// * 
// * @param   matRot3 rotation matrix in three-space
// * 
// * @return          matRot3xmatRot3 matrix in six-space
// * 
// * @see gov.sns.tools.beam.PhaseMatrix#rotationProduct(R3x3)
// */
//private PhaseMatrix computeRotation(R3x3 matRot3)    {
//    return PhaseMatrix.rotationProduct(matRot3);
//}
//
///**
// * Computes and returns the ellipsoid semi-axes in the beam frame given the 
// * diagonalized spatial covariance matrix.  
// * 
// * NOTE:
// * Since the semi-axes are in the (stationary) beam frame, the
// * semi-axes values are (in general) larger due to length
// * contraction in the laboratory frame.
// *  
// * @param   matDiag     diagonalized spatial covariance matrix
// * @return              three-array (a,b,c) of semi-axes values
// */
//private double[]    computeSemiAxes(R3x3 matDiag)   {
//    int         i;
//    double      eigenVal;
//    double[]    arrSemiAxes;
//    
//    i = 0;
//    arrSemiAxes = new double[3];
//
//    for (R3x3.Position pos : R3x3.Position.getDiagonal())   {
//        eigenVal       = matDiag.getElem(pos);
//        arrSemiAxes[i] = Math.sqrt(eigenVal);
//        i++;
//    }
//    
//    return arrSemiAxes;
//}
//
//
///**
// *  Check matrix for symmetry.  Quadratic forms must be
// *  symmetric and positive definite.
// * 
// *  @param  mat     <code>R3x3</code> object to check
// * 
// *  @exception  IllegalArgumentException    matrix is not symmetric and/or positive definite
// * 
// *  @author ckallen
// */
//private void    checkSymmetry(R3x3 mat)
//    throws IllegalArgumentException    
//{
//    if ( !mat.isSymmetric() )
//        throw new IllegalArgumentException("Matrix is not symmetric");
//}
//
//
