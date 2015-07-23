package xal.tools.beam;

/**
 * Converts between Trace3D and XAL unit for envelope simulations.
 *
 * @author Craig McChesney
 * @author Christopher Allen
 */
public class TraceXalUnitConverter {

    /*
     *  Constants
     */

    /** Speed of light in a vacuum (meters/second) */
    private final double LightSpeed = IConstants.LightSpeed;

    static public final double xalToTraceDimension = 1000; // m to mm
    static public final double traceToXalDimension = 0.001; //mm to m

    /*
     * Global Methods
     */

    /**
     *  Creates a new TraceXalUnitConverter object configured to the specified machine and
     *  beam species parameters.  All conversions done by the returned object will
     *  assume the values provided in the initial configuration.
     *
     *  @param  f       machine electromagnetic frequency in Hz
     *  @param  ER      the rest energy of the beam particle species in eV
     *  @param  W       the kinetic energy of the beam in eV
     *
     *  @return     new unit conversion object configured to above parameters
     */
    public static TraceXalUnitConverter newConverter(double f, double ER, double W) {
        return new TraceXalUnitConverter(f, ER, W);
    }

    /*
     *  Local Attributes
     */

    //
    // Configuration Parameters

    /** machine frequency in Hertz */
    private double f;

    /** particle rest energy in electron-volts */
    private double ER;

    /** beam kinetic energy in electron-volts */
    private double W;


    //
    // Consistent Auxiliary Parameters

    /** wavelength of RF in free space */
    private double    lambda;

    /** relativistic parameter */
    private double    gamma;

    /** velocity of synch. particle normalized to c (usually denoted beta) */
    private double    vnorm;

    /*
     * Initialization
     */

    /**
     *  For the moment only allow construction of conversion objects through
     *  a factory method.  Configuration mechanism may be changed in the future.
     */
    private TraceXalUnitConverter(double f, double ER, double W) {
        this.f = f;
        this.ER = ER;
        this.W = W;

       this.computeAuxiliaryParameters();
    }

    /**
     *  Computes the auxiliary machine and beam parameters needed by the conversion
     *  methods.
     *
     */
    private void computeAuxiliaryParameters()   {
        lambda = LightSpeed / f;
        gamma = 1.0 + (W / ER);
        vnorm = Math.sqrt(1.0 - (1.0 / (gamma * gamma)));
    }

    /*
     * Configuration Methods
     */

    /**
     * Change the RF frequency parameter used for longitudinal calculations.
     *
     * @param   f     new RF frequency in <b>Hz</b>
     */
    public void setRfFrequency(double f)  {
        if(this.f == f) { return; }
        this.f = f;
        this.computeAuxiliaryParameters();
    }

    /**
     * Change the current beam kinetic energy.
     *
     * @param   W       new beam energy in <b>eV</b>
     */
    public void setKineticEnergy(double W)  {
        if(this.W == W) { return; }
        this.W = W;
        this.computeAuxiliaryParameters();
    }

    /**
     * Change the current particle species rest energy.
     *
     * @param   ER      new rest energy in <b>eV</b>
     */
    public void setRestEnergy(double ER)    {
        if(this.ER == ER) { return; }
        this.ER = ER;
        this.computeAuxiliaryParameters();
    }

    /*
     *  Conversion Utilities
     */

    /**
     *  <p>
     *  Converts phase vector values in Trace3D units to values in units used by XAL (MKS).
     *  Specifically, the argument <code>vecCoord</code> is assumed to be in the form
     *  <pre>
     *      vecPhase=(x,x',y,y',dPhi,dW,1)
     *
     *      x    in mm
     *      x'   in mrad
     *      y    in mm
     *      y'   in mrad
     *      dPhi in degrees
     *      dW   in keV
     *  </pre>
     *  The output vector in in the following form along with the units:
     *  <pre>
     *      returned=(x,x',y,y',z,z',1)
     *
     *      x  in meters
     *      x' in radians
     *      y  in meters
     *      y' in radians
     *      z  in meters
     *      z' in radians
     *  </pre>
     * </p>
     *
     *  @param   vecCoord    coordinate phase vector in Trace3D units
     *
     *  @return              coordinate phase vector in XAL (MKS) units
     */
    public PhaseVector  traceToXalCoordinates(PhaseVector vecCoords)    {

        // Convert the transverse coordinates
        double      x, xp;      // x phase plane coordinates
        double      y, yp;      // y phase plane coordinates

        x  = vecCoords.getx()*traceToXalDimension;
        xp = vecCoords.getxp()*traceToXalDimension;
        y  = vecCoords.gety()*traceToXalDimension;
        yp = vecCoords.getyp()*traceToXalDimension;


        // Convert the longitudinal coordinates
        double      dW;         // the energy difference in eV
        double      z, zp;      // z phase plane coordinates

        z  = -( (vnorm*lambda)/360.0 )*vecCoords.getz(); // to dz, offset from syncr
        dW = vecCoords.getzp()*1.0e+3;                   // from keV to eV
        zp = (gamma/(gamma+1))*(dW/W);                   // to dp/p
        zp = zp/(gamma*gamma);                          // to z' radians

        return new PhaseVector(x,xp,y,yp,z,zp);
    }

    /**
     *  <p>
     *  Converts phase vector values in XAL (MKS) units to values in units used by Trace3D.
     *  Specifically, the argument <code>vecCoords</code> is assumed to be in the form
     *  <pre>
     *      vecCoords=(x,x',y,y',z,z',1)
     *
     *      x  in meters
     *      x' in radians
     *      y  in meters
     *      y' in radians
     *      z  in meters
     *      z' in radians
     *  </pre>
     *  The output vector in in the following form along with the units:
     *  <pre>
     *      returned=(x,x',y,y',dPhi,dW,1)
     *
     *      x    in mm
     *      x'   in mrad
     *      y    in mm
     *      y'   in mrad
     *      dPhi in degrees
     *      dW   in keV
     *  </pre>
     * </p>
     *
     *  @param   vecCoord    coordinate phase vector in MKS units
     *
     *  @return              coordinate phase vector in Trace3D units
     */
    public PhaseVector  xalToTraceCoordinates(PhaseVector vecCoords)    {

        // Convert the transverse coordinates
        double      x, xp;      // x phase plane coordinates
        double      y, yp;      // y phase plane coordinates

        x  = vecCoords.getx()*xalToTraceDimension;
        xp = vecCoords.getxp()*xalToTraceDimension;
        y  = vecCoords.gety()*xalToTraceDimension;
        yp = vecCoords.getyp()*xalToTraceDimension;


        // Convert the longitudinal coordinates
        double      dPhi, dW;      // z phase plane coordinates

        dPhi = -(1.0/( (vnorm*lambda)/360.0 ))*vecCoords.getz();
        dW   = gamma*gamma*vecCoords.getzp();             // to dp/p
        dW   = W*((gamma+1.0)/gamma )*dW;                 // to eV
        dW   = dW*1.0e-3;                                 // to keV

        return new PhaseVector(x,xp,y,yp,dPhi,dW);
    }

    /**
     * <p>
     * Converts Twiss parameter in Trace3D units to XAL (MKS) units for the transverse phase
     * planes.  Method takes the set of Twiss parameters as a Twiss object argument
     * and returns a new object containing the same Twiss in the different units.  The
     * method is none destructive.
     * </p><p><table border=1>
     * <tr>
     *   <th/>
     *   <th>Trace3D</th>
     *   <th>XAL</th>
     * </tr>
     * <tr align="center">
     *   <th>alpha</th>
     *   <td>unitless</td>
     *   <td>unitless (no conversion)</td>
     * </tr>
     * <tr align="center">
     *   <th>beta</th>
     *   <td>m/rad</td>
     *   <td>m/rad</td>
     * </tr>
     * <tr align="center">
     *   <th>emittance</th>
     *   <td>eff. (5xRMS) mm-mrad</td>
     *   <td>RMS m-rad</td>
     * </tr>
     * </table></p>
     *
     * @param   t3dTwiss    Twiss parameters in Trace3D units
     *
     * @return              Twiss parameters in XAL units
     */
    public Twiss traceToXalTransverse(Twiss t3dTwiss) {
        double      alpha;          // Twiss alpha parameter in XAL (MKS) units
        double      beta;           // Twiss beta parameter in XAL (MKS) units
        double      emittance;      // beam emittance

        alpha = t3dTwiss.getAlpha();
        beta = t3dTwiss.getBeta();

        emittance = t3dTwiss.getEmittance() * (traceToXalDimension*traceToXalDimension);   // from mm-mrad to m-rad
        emittance = emittance / 5.;              // to rms emittance

        return new Twiss(alpha, beta, emittance);
    }

    /**
     * <p>
     * Converts Twiss parameters in Trace3D units to XAL (MKS) units for the longitudinal
     * phase planes.  Method takes the set of Twiss parameters as a Twiss object argument
     * and returns a new object containing the same Twiss in the different units.  The
     * method is non destructive.
     * </p><p><table border="1">
     * <tr>
     *   <th/>
     *   <th>Trace3D</th>
     *   <th>XAL</th>
     * </tr>
     * <tr align="center">
     *   <th>alpha</th>
     *   <td>unitless</td>
     *   <td>(-1)unitless (phase lag is positive)</td>
     * </tr>
     * <tr align="center">
     *   <th>beta</th>
     *   <td>deg/keV</td>
     *   <td>m/rad</td>
     * </tr>
     * <tr align="center">
     *   <th>emittance</th>
     *   <td>eff. (5xRMS) deg-keV</td>
     *   <td>RMS m-rad</td>
     * </tr>
     * </table></p>
     *
     * @param   t3dTwiss    Trace3D twiss parameters
     *
     * @return              Twiss parameters in XAL units
     */
    public Twiss traceToXalLongitudinal(Twiss t3dTwiss) {

        // Compute conversion factors
        double      t1;       // temporary conversion factor
        double      t2;       // temporary conversion factor
        double      t3;       // temporary conversion factor
        double      Cdeg;     // emittance conversion factor

        t1 = (vnorm * lambda / 360.0);
        t2 = (gamma / (gamma + 1.0)) * (1.0 / W);
        t3 = 1.0/(gamma*gamma);
        Cdeg = t1 * t2 * t3;

        // Compute value of Twiss parameters in MKS units
        double    alpha;      // Twiss alpha parameter
        double    beta;       // Twiss beta parameter
        double    emittance;  // beam emittance

        alpha = -t3dTwiss.getAlpha();

        beta = t3dTwiss.getBeta() * 0.001;            // to deg/eV
        beta = beta * t1 * (1.0/t2) * (1.0/t3);       // to m/rad

 //       double fParmila = vnorm*vnorm*vnorm*gamma*gamma*gamma;
 //       double fT3d     = vnorm*gamma*(gamma*gamma-1);
 //       System.out.println("beta, gamma, fParmila, fT3d = "+vnorm+" "+gamma+" "+fParmila+" "+fT3d);

        emittance = t3dTwiss.getEmittance() * 1000.0; // to deg-eV
        emittance = emittance / 5.0;                  // to rms emittance
        emittance = emittance * Cdeg;                 // to m-rad

        return new Twiss(alpha, beta, emittance);
    }

    /**
     * <p>
     * Converts Twiss parameter in XAL (MKS) units to Trace3D units for the transverse phase
     * planes.  Method takes the set of Twiss parameters as a Twiss object argument
     * and returns a new object containing the same Twiss in the different units.  The
     *  method is none destructive.
     * </p><p><table border=1>
     * <tr>
     *   <th/>
     *   <th>Trace3D</th>
     *   <th>XAL</th>
     * </tr>
     * <tr align="center">
     *   <th>alpha</th>
     *   <td>unitless</td>
     *   <td>unitless (no conversion)</td>
     * </tr>
     * <tr align="center">
     *   <th>beta</th>
     *   <td>m/rad</td>
     *   <td>m/rad</td>
     * </tr>
     * <tr align="center">
     *   <th>emittance</th>
     *   <td>eff. (5xRMS) mm-mrad</td>
     *   <td>RMS m-rad</td>
     * </tr>
     * </table></p>
     *
     * @param   twissXal    Twiss parameters in XAL (MKS) units
     *
     * @return              Twiss parameters in Trace3D units
     */
    public Twiss xalToTraceTransverse(Twiss twissXal) {
        double      alpha;          // Twiss alpha parameter
        double      beta;           // Twiss beta parameter
        double      emittance;      // beam emittance

        alpha = twissXal.getAlpha();
        beta = twissXal.getBeta();

        emittance = twissXal.getEmittance() * (xalToTraceDimension*xalToTraceDimension);     // to mm-mrad
        emittance = emittance * 5.0;                        // to effective emittance

        return new Twiss(alpha, beta, emittance);
    }

    /**
     * <p>
     * Converts Twiss parameters in XAL (MKS) units to Trace3D units for the longitudinal
     * phase planes.  Method takes the set of Twiss parameters as a Twiss object argument
     * and returns a new object containing the same Twiss in the different units.  The
     * method is none destructive.
     * </p><p><table border=1>
     * <tr>
     *   <th/>
     *   <th>Trace3D</th>
     *   <th>XAL</th>
     * </tr>
     * <tr align="center">
     *   <th>alpha</th>
     *   <td>unitless</td>
     *   <td>(-1)unitless (phase lag is positive)</td>
     * </tr>
     * <tr align="center">
     *   <th>beta</th>
     *   <td>deg/keV</td>
     *   <td>m/rad</td>
     * </tr>
     * <tr align="center">
     *   <th>emittance</th>
     *   <td>eff. (5xRMS) deg-keV</td>
     *   <td>RMS m-rad</td>
     * </tr>
     * </table></p>
     *
     * @param   twissXal    Trace3D twiss parameters
     *
     * @return              Twiss parameters in XAL units
     */
    public Twiss xalToTraceLongitudinal(Twiss twissXal) {

        // Compute conversion factors
        double      t1;       // temporary conversion factor
        double      t2;       // temporary conversion factor
        double      t3;       // temporary conversion factor
        double      Cdeg;     // emittance conversion factor

        t1 = (vnorm * lambda / 360.0);
        t2 = (gamma / (gamma + 1.0)) * (1.0 / W);
        t3 = 1.0/(gamma*gamma);
        Cdeg = t1 * t2 * t3;

        // Compute value of Twiss parameters in Trace3D units
        double    alpha;      // Twiss alpha parameter
        double    beta;       // Twiss beta parameter
        double    emittance;  // beam emittance


        alpha = -twissXal.getAlpha();

        beta = twissXal.getBeta();
        beta = beta * t3 * t2 / t1;     // to deg/eV
        beta = beta * 1000.0;           // to deg/keV

        emittance = twissXal.getEmittance();
        emittance = emittance / Cdeg;                   // to deg-eV
        emittance = emittance / 1000.0;                 // to deg-keV
        emittance = emittance * 5.0;                    // to effective emittance

        return new Twiss(alpha, beta, emittance);
    }

    /**
     * Convert the dispersion from XAL units to Trace3D units.
     *
     * @param dXal      dispersion as z' (radians)
     * @param index     phase plane index
     * @return          dispersion as dp/p0 (radians)
     *
     * @author H. Sako
     */
    public double xalToTraceDispersion(double dXal, PhaseIndex index) {

        double dT3d = 0;
        if (index == PhaseIndex.X || index == PhaseIndex.Y) {
            //sako dT3d = dXal/(gamma*gamma);
        	 dT3d = dXal; //no transformation is necessary. Sako, 2008/07/07
        } else {
            dT3d = dXal;
        }
        return dT3d;
    }

    /**
     * Convert the dispersion from XAL units to Trace3D units.
     *
     * @param dT3d      dispersion as dp/p0 (radians)
     * @param index     phase plane index
     * @return          dispersion as z' (radians)
     *
     * @author H. Sako
     */
    public double traceToXalDispersion(double dT3d, PhaseIndex index) {
        double dXal = 0;

        if (index == PhaseIndex.X || index == PhaseIndex.Y) {
            //sako dXal = dT3d*(gamma*gamma);
        	dXal = dT3d; //no transformation is necessary. Sako, 2008/07/07
        } else {
            dXal = dT3d;
        }
        return dXal;
    }

    /**
     *  <p>
     *  Builds a correlation matrix from Twiss parameters in the three phase
     *  planes.  The Twiss parameters are assumed to be in the units used by
     *  Trace3D.   The beam is also assumed to be centered (on axis), thus the
     *  correlation matrix equals the covariance matrix (usually denoted sigma).
     *  </p><p>
     *  NOTE:
     *  The returned matrix is in homogeneous coordinates of the block
     *  diagonal form
     *  <pre>
     *      | Rxx   0   0   0 |
     *      |   0 Ryy   0   0 |
     *      |   0   0 Rzz   0 |
     *      |   0   0   0   1 |
     *  </pre>
     *  where Rii are 2x2 symmetric blocks corresponding to each phase
     *  plane.  Clearly the phase planes are uncoupled.
     *  </p><p>
     *  The covariance matrix is the second central moment of the beam
     *  distribution defined by
     *  <blockquote>
     *          sigma = &lt;( z-&lt;z&gt; )<sup>2</sup>&gt;
     *                = &lt;zz<sup>T</sup>&gt;-&lt;z&gt;&lt;z&gt;<sup>T</sup>
     *                = &lt;zz<sup>T</sup>&gt;
     *  </blockquote>
     *  since since the centroid &lt;z&gt;=0.
     * </p>
     *
     * @param   t3dX Twiss parameters describing the beam ellipse in the x plane
     * @param   t3dY Twiss parameters describing the beam ellipse in the y plane
     * @param   t3dZ Twiss parameters describing the beam ellipse in the z plane
     *
     * @return  correlation matrix corresponding to the above Twiss parameters
     *
     * @see #traceToXalLongitudinal
     * @see #traceToXalTransverse
     */
    public CovarianceMatrix correlationMatrixFromT3d(Twiss t3dX, Twiss t3dY, Twiss t3dZ) {

        // Convert to MKS units
        Twiss xalX = traceToXalTransverse(t3dX);
        Twiss xalY = traceToXalTransverse(t3dY);
        Twiss xalZ = traceToXalLongitudinal(t3dZ);

        // Build correlation matrix and return
        return CovarianceMatrix.buildCovariance(xalX, xalY, xalZ);
    }

    /**
     *  <p>
     *  Builds a correlation matrix from Twiss parameters in the three phase
     *  planes.  The Twiss parameters are assumed to be in the units used by
     *  Trace3D.  The correlation matrix also has the mean values corresponding
     *  to the mean value vector &lt;z&gt; provided in the argument.  The mean value vector
     *  <code>centroid</code> is also assumed to be in the units used by Trace3D.
     *  It is also assumed to have the form
     *  <blockquote>
     *      centroid = &lt;z&gt;
     *               = (&lt;x&gt;, &lt;x'&gt;, &lt;y&gt;, &lt;y'&gt;, &lt;dPhi&gt;, &lt;dW&gt;, 1)
     *  </blockquote>
     *  </p><p>
     *  The correlation matrix is computed by first computing the covariance matrix
     *  (usually denoted sigma in the literature) from the Twiss parameters then
     *  adjusting the value according to the effects of being "off center".  This is
     *  done by adding the tensor product of the mean value vector &lt;z&gt;.
     *  </p><p>
     *  To see this not that the correlation matrix is the second moment of the beam
     *  distribution defined by
     *  <blockquote>
     *          &lt;zz<sup>T</sup>&gt;
     *  </blockquote>
     *  Thus, denoting sigma the covariance matrix we have
     *  <blockquote>
     *          sigma = &lt;( z-&lt;z&gt; )<sup>2</sup>&gt;
     *                = &lt;zz<sup>T</sup>&gt;-&lt;z&gt;&lt;z&gt;<sup>T</sup>
     *  </blockquote>
     *  or
     *  <blockquote>
     *          &lt;zz<sup>T</sup>&gt; = sigma + &lt;z&gt;&lt;z&gt;<sup>T</sup>
     *  </blockquote>
     *  </p><p>
     *  NOTE:
     *  The returned matrix is in homogeneous coordinates of the block
     *  diagonal form
     *  <pre>
     *      | Rxx   0   0    0 |
     *      |   0 Ryy   0    0 |   +  &lt;z&gt;&lt;z&gt;<sup>T</sup>
     *      |   0   0 Rzz    0 |
     *      |   0   0   0    1 |
     *  </pre>
     *  where Rii are 2x2 symmetric blocks corresponding to each phase
     *  plane and &lt;z&gt; is the vector of mean values in the phase plane,
     *  e.g., &lt;z&gt;=(&lt;x&gt;, &lt;x'&gt;, &lt;y&gt;, &lt;y'&gt;, &lt;z&gt;, &lt;z'&gt;).
     *  </p>
     *
     * @param   t3dX        Twiss parameters describing the beam ellipse in the x plane
     * @param   t3dY        Twiss parameters describing the beam ellipse in the y plane
     * @param   t3dZ        Twiss parameters describing the beam ellipse in the z plane
     * @param   centroid    phase position of the beam centroid
     *
     * @return  correlation matrix corresponding to the above Twiss parameters and centroid
     *
     * @see #traceToXalLongitudinal
     * @see #traceToXalTransverse
     */
    public CovarianceMatrix correlationMatrixFromT3d(
        Twiss t3dX,
        Twiss t3dY,
        Twiss t3dZ,
        PhaseVector centroid) {

        // Convert to MKS units
        Twiss xalX = traceToXalTransverse(t3dX);
        Twiss xalY = traceToXalTransverse(t3dY);
        Twiss xalZ = traceToXalLongitudinal(t3dZ);

        PhaseVector xalMean = traceToXalCoordinates(centroid);

        // Build correlation matrix and return
        return CovarianceMatrix.buildCovariance(xalX, xalY, xalZ, xalMean);
    }

    /**
     *  Compute and return the beam centroid coordinates from the correlation
     *  matrix in homogeneous coordinates.  The correlation matrix is assumed
     *  to be in MKS units used by XAL and the returned centroid coordinate vector
     *  is in phase coordinates units used by Trace3D.
     *
     * @param   matCorrel   correlation matrix &lt;zz<sup>T</sup>&gt; in MKS units
     *
     * @return              coordinates of the centroid in Trace3D units
     *
     *  @see #xalToTraceCoordinates
     */
    public PhaseVector  centroidFromXal(CovarianceMatrix matCorrel)    {
        PhaseVector vecCentroid = matCorrel.getMean();

        return this.xalToTraceCoordinates(vecCentroid);
    }

    /**
     *  Computes and return the Twiss parameters for each plane that correspond to
     *  the given correlation matrix.  The Twiss parameters are returned in the
     *  units used by Trace3D.  The correlation matrix is assumed to be in MKS units
     *  used by XAL.
     *  <p>
     *  NOTE:
     *  This method ignores any coupling between phase planes and any offsets of the
     *  beam centroid from the beam axis.
     *  <p>
     *  TODO - Make the method consider the general case of coupling between phase planes
     *  and return the Twiss parameters as projections that one would observe in
     *  experiments.
     *
     * @param matCorrel     correlation matrix &lt;zz<sup>T</sup>&gt; in MKS units
     *
     *  @return     array of Twiss with length 3 where<br>
     *              array[0] = Twiss parameters in x plane<br>
     *              array[1] = Twiss parameters in y plane<br>
     *              array[2] = Twiss parameters in z plane<br>
     *
     */
    public Twiss[]  twissParametersFromXal(CovarianceMatrix mat) {
        // Twiss[]     arrTwissXal = mat.twissParameters();

        Twiss[]     arrTwissXal = mat.computeTwiss();

        Twiss[]     arrTwissT3d = new Twiss[3];

        arrTwissT3d[0] = this.xalToTraceTransverse(arrTwissXal[0]);
        arrTwissT3d[1] = this.xalToTraceTransverse(arrTwissXal[1]);
        arrTwissT3d[2] = this.xalToTraceLongitudinal(arrTwissXal[2]);

        return arrTwissT3d;
    }

    /**
     * calculate 1-sigma in xal unit from beta and emittance of xal unit
     */
    static public double calcSigmaXalFromTrace(double beta, double emit) {
        return Math.sqrt(beta*emit)*traceToXalDimension;
    }

    /**
     * calculate 1-sigma in trace3d unit from beta and emittance of trace3d unit
     */
    static public double calcSigmaTraceFromTrace(double beta, double emit) {
        return Math.sqrt(beta*emit/5);
    }
    /**
     * calculate 1-sigma in xal unit from beta and emittance of xal unit
     */
    static public double calcSigmaXalFromXal(double beta, double emit) {
        return Math.sqrt(beta*emit);
    }

    /**
     * calculate 1-sigma in trace3d unit from beta and emittance of trace3d unit
     */
    static public double calcSigmaTraceFromXal(double beta, double emit) {
        return Math.sqrt(beta*emit/5)*xalToTraceDimension;
    }
}
