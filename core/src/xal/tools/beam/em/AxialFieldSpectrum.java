/**
 * FieldSpectrum.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 23, 2015
 */
package xal.tools.beam.em;

import xal.tools.math.Complex;
import xal.tools.math.fnc.IRealFunction;

/**
 * <p>
 * Class that representing the spatial spectral properties of a time-harmonic, 
 * axial electric field.  
 * The most important properties from a beam physics standpoint are the transit time factors.
 * These are the components of the Fourier transform of the axial field, and their resulting 
 * Hilbert transforms.  The Hilbert transform of a transit time factor turns out to be
 * the transit time factor of axial field times the signum function.  The spectral 
 * pre- and post-envelopes are formed from a transit time factor and its Hilbert transform.
 * These pre- and post-envelopes are the primary entities for computing the pre- and post-gap
 * energy gain and phase jump, respectively.
 * </p>
 * <p>
 * <h3>Partial Field Model</h3>
 * This class attempts to maintain backward compatibility between the RF acceleration model
 * produced by Los Alamos and CERN.  There the <i>T<sub>q</sub></i> and <i>S</i> transit time
 * factors are zero.  Although usually labeled <i>S</i>, the "sine" transit time factor is
 * actually its quadrature conjugate <i>S<sub>q</sub></i>.  That model also requires an
 * "offset" &Delta;<i>z</i> which is the distance between the coordinate origin and the 
 * field center (the point of symmetry).  The assumptions are that the field is symmetric 
 * about the axial location &Delta;<i>z</i>, if not this information is lost.
 * </p>
 * <p>
 * In the partial field model the provided spectra, <i>T</i>, <i>S<sub>q</sub></i>, and their
 * derivatives, are assumed to be functions of normalized particle velocity &beta; &trie; 
 * <i>v</i>/<i>c</i>, where <i>v</i> is particle velocity and <i>c</i> is the speed of light.  
 * Thus, the functions <i>T</i>(&beta;), <i>dT</i>(&beta;)/<i>dk</i>, <i>S<sub>q</sub></i>(&beta;),
 * and <i>dS<sub>q</sub></i>(&beta;)/<i>dk</i> are provided to the partial field model
 * constructor.
 * </p>
 * <p>
 * Note the important difference that in the full-field model the arguments of the spectra
 * quantities are the particle wave number <i>k</i>.  That is, the constructor for the
 * full-field model takes spectral functions <i>T</i>(<i>k</i>), <i>dT</i>(<i>k</i>)/<i>dk</i>, 
 * <i>S<sub>q</sub></i>(<i>k</i>), and <i>dS<sub>q</sub></i>(<i>k</i>)/<i>dk</i>, along with all
 * the other spectral quantities.
 * </p>
 * <p>
 * <h3>Full Field Model</h3>
 * The current full-field model includes all four transit time factors and their derivatives.  Thus,
 * all the field information is kept, no offsets are necessary, and there is information
 * enough to compute the post gap energy gain and phase jump.  (In the above model the
 * post gap quantities are assumed to be equal to the pre-gap quantities.)
 * When function objects are provided for all four transit time factors this class
 * assumes that the new model is being used.  When only <i>T</i> and <i>S</i> are provided
 * the class assumes that the old model is being used.  Further assumptions are
 * <br/>
 * <br/>
 * &middot; The sine transit time factor <i>S</i> is actually the conjugate <i>S<sub>q</sub></i>
 * (see below)
 * <br/>
 * &middot; The offset &Delta;<i>z</i> must also be provided
 * <br/>
 * <br/>
 * The class then makes the appropriate conversions from the quantities 
 * (<i>T,T',S,S'</i>,&Delta;<i>z</i>) to
 * (<i>T,T',T<sub>q</sub>,T'<sub>q</sub>,S,S',S<sub>q</sub>,S'<sub>q</sub></i>).  Of course the
 * later set is incomplete.
 * </p>   
 * <p>
 * <h4>Axial Electric Fields</h4>
 * Let the longitudinal electric field 
 * along the beam axis <i>z</i> be denoted <i>E<sub>z</sub></i>(<i>z</i>).  Let the total voltage
 * gain long the field be denoted <i>V</i><sub>0</sub>, that is,
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(<i>z</i>) <i>dz</i> .
 * <br/>
 * <br/>
 * Note then that the total available energy gain &Delta;<i>W</i><sub>0</sub> 
 * for a particle falling through
 * the field <i>E<sub>z</sub></i>(<i>z</i>) is <i>qV</i><sub>0</sub>.  The value <i>V</i><sub>0</sub>
 * is used to create the normalized electric field <i>e<sub>z</sub></i>(<i>z</i>) given by
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>e</i><sub><i>z</i></sub>(<i>z</i>) &trie; (1/<i>V</i><sub>0</sub>)<i>E<sub>z</sub></i>(<i>z</i>) .
 * <br/>
 * <br/>
 * All spectral quantities in this class are with respect to this normalized field. 
 * </p> 
 * <p>
 * Next, let sgn(<i>z</i>) denote the signum function, that is,
 * <br/>
 * <pre>
 *     sgn(<i>z</i>) &trie; -1    <i>z</i>  &lt; 0
 *              +1    <i>z</i>  &gt; 0
 *</pre>
 * Finally, define the <i>quadrature field</i> <i>E<sub>q</sub></i>(<i>z</i>) as 
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>E<sub>q</sub></i>(<i>z</i>) &trie; sgn(<i>z</i>) <i>E<sub>z</sub></i>(<i>z</i>) .
 * <br/>
 * <br/>
 * Of course the quantities here will be with respect to the normalized quadrature field
 * <i>e</sub>q</sub></i>(<i>z</i>) defined as 
 * <br/>
 * <pre>
 *     <i>e</i><sub><i>z</i></sub>(<i>z</i>) &trie; (1/<i>V</i><sub>0</sub>)<i>E<sub>q</sub></i>(<i>z</i>) ,
 *           = sgn(<i>z</i>)<i>e<sub>z</sub></i>(</i>z</i>) .
 * </pre>
 * </p>
 * <p>  
 * <h4>Transit Time Factors</h4>  
 * There are four (4) transit time factors.  
 * These 
 * transit time factors are described as follows:
 * <br/>
 * <br/>
 * &middot; <i>T</i>(<i>k</i>) - The Fourier cosine transform of axial field <i>E<sub>z</sub></i>(<i>z</i>)
 * <br/>
 * &nbsp; &nbsp; <i>T</i>(<i>k</i>) &trie; (1/<i>V</i><sub>0</sub>)&int;<i>E<sub>z</sub></i>(<i>z</i>) cos <i>kz</i> <i>dz</i>
 * <br/> 
 * <br/>
 * &middot; <i>S</i>(<i>k</i>) - The Fourier sine transform of axial field <i>E<sub>z</sub></i>(<i>z</i>)
 * <br/>
 * &nbsp; &nbsp; <i>S</i>(<i>k</i>) &trie; (1/<i>V</i><sub>0</sub>)&int;<i>E<sub>z</sub></i>(<i>z</i>) sin <i>kz</i> <i>dz</i>
 * <br/>
 * <br/> 
 * &middot; <i>T<sub>q</sub></i>(<i>k</i>) - The Fourier cosine transform of the axial field sgn(<i>z</i>)<i>E<sub>z</sub></i>(<i>z</i>)
 * <br/>
 * &nbsp; &nbsp; <i>T<sub>q</sub></i>(<i>k</i>) &trie; (1/<i>V</i><sub>0</sub>)&int;sgn(<i>z</i>)<i>E<sub>z</sub></i>(<i>z</i>) cos <i>kz</i> <i>dz</i>
 * <br/>
 * <br/> 
 * &middot; <i>S<sub>q</sub></i>(<i>k</i>) - The Fourier sine transform of axial field sgn(<i>z</i>)<i>E<sub>z</sub></i>(<i>z</i>)
 * <br/>
 * &nbsp; &nbsp; <i>S<sub>q</i></i>(<i>k</i>) &trie; (1/<i>V</i><sub>0</sub>)&int; sgn(<i>z</i>)<i>E<sub>z</sub></i>(<i>z</i>) sin <i>kz</i> <i>dz</i>
 * <br/>
 * <br/>
 * where <i>k</i> is the synchronous particle wave number.  Sometimes the arguments to the transit
 * time factors is the synchronous particle velocity &beta;.  This includes that for 
 * the derivative functions as well (see below).  
 * One needs to check the method documentation for the argument type.
 * </p> 
 * <p>
 * The derivatives of the transit time factors are also available.  These are the derivatives with
 * respect to wave number <i>k</i> and will be denoted <i>T'</i>(<i>k</i>), <i>S'</i>(<i>k</i>),
 * <i>T'<sub>q</sub></i>(<i>k</i>), and <i>S'<sub>q</sub></i>(<i>k</i>).
 * </p>
 * <p>
 * <h4>Hilbert Transform</h4>
 * The transit time factors are related to each other
 * via the Hilbert transform &Hscr;.  Specifically,
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>T<sub>q</sub></i>(<i>k</i>) = -&Hscr;[<i>S</i>(<i>k</i>)] ,
 * <br/>
 * &nbsp; &nbsp; <i>S<sub>q</sub></i>(<i>k</i>) = +&Hscr;[<i>T</i>(<i>k</i>)] .
 * <br/>
 * <br/>
 * This is a transitive relation which follows from the anti-selfadjointness 
 * of the Hilbert transform, thus,
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>T</i>(<i>k</i>) = -&Hscr;[<i>S<sub>q</sub></i>(<i>k</i>)] ,
 * <br/>
 * &nbsp; &nbsp; <i>S</i>(<i>k</i>) = +&Hscr;[<i>T<sub>q</sub></i>(<i>k</i>)] .
 * <br/>
 * <br/> 
 * The Hilbert transform also relates the field spectra, as shown below.
 * </p>
 * <p>
 * <h4>Field Spectra</h4>
 * Denote
 * by &Escr;<sub><i>z</i></sub>(<i>k</i>) and &Escr;<sub><i>q</i></sub>(<i>k</i>) 
 * the Fourier transforms of
 * the axial field <i>e<sub>z</i>(<i>z</i>) and its conjugate <i>e<sub>q</i></i>(<i>z</i>), 
 * respectively.  That is, the field spectra are
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Escr;<sub><i>z</i></sub>(k) &trie; &Fscr;[<i>e<sub>z</sub></i>](<i>k</i>) ,
 * <br/>
 * &nbsp; &nbsp; &Escr;<sub><i>q</i></sub>(k) &trie; &Fscr;[<i>e<sub>q</sub></i>](<i>k</i>) ,
 * <br/>
 * <br/>
 * where &Fscr;[&middot;] is the Fourier transform operator.  The Fourier transforms of the fields have
 * the decomposition
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Escr;<sub><i>z</i></sub>(<i>k</i>) = T</i>(<i>k</i>) - <i>i</i><i>S</i>(<i>k</i>) ,
 * <br/>                                                 
 * &nbsp; &nbsp; &Escr;<i><sub>q</sub></i>(<i>k</i>) = T<sub>q</sub></i>(<i>k</i>) - <i>i</i><i>S<sub>q</sub></i>(<i>k</i>) ,
 * <br/>
 * <br/>
 * where <i>i</i> is the imaginary unit.  The spectra are then related by the Hilbert transforms
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Hscr;[&Escr;<sub><i>z</i></sub>(<i>k</i>)] = i&Escr;<i><sub>q</sub></i>(<i>k</i>) ,
 * <br/>                                                 
 * &nbsp; &nbsp; &Hscr;[&Escr;<sub><i>q</i></sub>(<i>k</i>)] = i&Escr;<i><sub>z</sub></i>(<i>k</i>) ,
 * <br/>
 * <br/>
 * Thus, we see &Escr;<sub><i>z</i></sub>(<i>k</i>) and &Escr;<sub><i>q</i></sub>(<i>k</i>) are 
 * conjugates of each other.
 * </p>
 * <p>
 * <h4>Pre- and Post-Envelope Spectra</h4>
 * The pre- and post-envelope spectra can be formed from the field spectra.  First, denote
 * by &Escr;<sup>-</sup>(<i>k</i>) and &Escr;<sup>+</sup>(<i>k</i>) the pre- and post-envelope
 * spectra, respectively.  They are defined
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Escr;<sup>-</sup>(k) &trie; (1/2)[ &Escr;<sub><i>z</i></sub>(<i>k</i>) + i&Hscr;[&Escr;<sub><i>z</i></sub>(<i>k</i>] ] 
 *                                            =  (1/2)[ &Escr;<sub><i>z</i></sub>(<i>k</i>) -  &Escr;<sub><i>q</i></sub>(<i>k</i>) ],
 * <br/>
 * &nbsp; &nbsp; &Escr;<sup>+</sup>(k) &trie; (1/2)[ &Escr;<sub><i>z</i></sub>(<i>k</i>) + i&Hscr;[&Escr;<sub><i>z</i></sub>(<i>k</i>] ] 
 *                                            =  (1/2)[ &Escr;<sub><i>z</i></sub>(<i>k</i>) +  &Escr;<sub><i>q</i></sub>(<i>k</i>) ] ,
 * <br/>
 * <br/>
 * Let &phi; be the synchronous particle phase at the gap center.  Then the quantities 
 * <i>e<sup>-i&phi;</sup></i>&Escr;<sup>-</sup>(<i>k</i>) and <i>e<sup>-i&phi;</sup></i>&Escr;<sup>+</sup>(<i>k</i>)
 * contain the pre- and post-gap energy gain &Delta;<i>W</i><sup>-</sup>, &Delta;<i>W</i><sup>+</sup> and 
 * phase jump &Delta;&phi;<sup>-</sup>, &Delta;&phi;<sup>+</sup>, respectively.  For example,
 * the real part of <i>e<sup>-i&phi;</sup></i>&Escr;<sup>-</sup>(<i>k</i>) tracks the pre-gap 
 * energy gain while the imaginary part tracks the phase jump.  We have
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Delta;<i>W</i><sup>-</sup>(&phi;,<i>k</i>) = <i>qV</i><sub>0</sub> Re &Escr;<sup>-</sup>(<i>k</i>)<i>e<sup>-i&phi;</sup></i> ,
 * <br/>
 * &nbsp; &nbsp; &Delta;&phi;<sup>-</sup>(&phi;,<i>k</i>) = <i>d</i>/<i>dk</i> Im <i>K<sub>i</sub></i>&Escr;<sup>-</sup>(<i>k</i>)<i>e<sup>-i&phi;</sup></i> ,
 * <br/>
 * <br/>
 * where <i>q</i> is the unit charge and <i>K<sub>i</sub></i> is the quantity
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>K<sub>i</sub></i> &trie; <i>k</i><sub>0</sub>(<i>qV</i><sub>0</sub>/<i>mc</i><sup>2</sup>)(1/&beta;<sub><i>i</i></sub><sup>3</sup>&gamma;<sub><i>i</i></sub><sup>3</sup>) .
 * <br/>
 * <br/>
 * The subscript <i>i</i> indicates initial, pre-gap values.  The post-gap quantities have 
 * analogous expressions
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Delta;<i>W</i><sup>+</sup>(<i>k</i>,&phi;) = (<i>q</i>/2) Re <i>e<sup>-i&phi;</sup></i>&Escr;<sup>-</sup>(<i>k</i>) ,
 * <br/>
 * &nbsp; &nbsp; &Delta;&phi;<sup>+</sup>(<i>k</i>,&phi;) = (<i>K<sub>f</sub></i>/2) Im <i>d</i>/<i>dk</i> <i>e<sup>-i&phi;</sup></i>&Escr;<sup>-</sup>(<i>k</i>) ,
 * <br/>
 * <br/>
 * The subscript <i>f</i> indicates final, post-gap quantities.  Methods to compute these 
 * values are provided.
 * </p>
 * <p>
 *
 * @author Christopher K. Allen
 * @since  Sep 23, 2015
 * @version Sep 23, 2015
 */
public class AxialFieldSpectrum {

    
    /*
     * Global Constants
     */

    /** the value of 2&pi; */
    private static final double     DBL_2PI = 2.0 * Math.PI;

    /** Speed of light in a vacuum (meters/second) */
    private static final double     DBL_LGHT_SPD = 299792458.0;   
    
    
    
    
    /*
     * Local Attributes
     */
    
    
    //
    //  Partial Field Model with field offset and frequency to convert from beta 
    //
    
    /** polynomial fit for the cosine transit time factor T versus beta */
    private final IRealFunction fncTz0;

    /** polynomial fit of the cosine transit time factor derivative T' versus beta */ 
    private final IRealFunction fncDTz0;

    
    /** polynomial fit of the conjugate sine transit time factor Sq versus beta */
    private final IRealFunction fncSq0;

    /** polynomial fit of the conjugate sine transit time factor derivative Sq' versus beta */
    private final IRealFunction fncDSq0;


    /** the offset between the field center and the geometric center */
    private final double    dblFldOSet;

    /** the time harmonic frequency of the field */ 
    private final double    dblFrq;
    

    //
    // Full Field Acceleration Model
    //
    
    /** polynomial fit for the cosine transit time factor T versus beta */
    private final IRealFunction fncTz;

    /** polynomial fit of the cosine transit time factor derivative T' versus beta */ 
    private final IRealFunction fncDTz;

    
    /** polynomial fit of the conjugate sine transit time factor Sq versus beta */
    private final IRealFunction fncSq;

    /** polynomial fit of the conjugate sine transit time factor derivative Sq' versus beta */
    private final IRealFunction fncDSq;


    /** polynomial fit for the conjugate cosine transit time factor Tq versus beta */
    private final IRealFunction fncTq;

    /** polynomial fit of the conjugate cosine transit time factor derivative Tq' versus beta */ 
    private final IRealFunction fncDTq;

    
    /** polynomial fit of the sine transit time factor S versus beta */
    private final IRealFunction fncSz;

    /** polynomial fit of the sine transit time factor derivative S' versus beta */
    private final IRealFunction fncDSz;

    
    //
    // State Variables
    //
    
    /** flag indicating that old acceleration model is in use */
    private final boolean   bolPrtlFldMdl;
    
    
    /*
     * Initialization
     */
    
    /**
     * <p>
     * Constructor for creating a partial field model object 
     * using a central field offset.
     * </p>
     * <p>
     * In this case the given field spectra are assumed to be functions of
     * normalized particle velocity &beta;.  Thus, the RF frequency is needed
     * to convert from wave number <i>k</i> to normalized velocity &beta; in 
     * order to evaluate the functions.
     * </p>
     *
     * @param dblFreq       frequency of the time-harmonic electric field (Hz)
     * @param dblOffset     offset between axial origin and central field position (meters)
     * @param fncTz0         central cosine transform (transit time factor)
     * @param fncDTz0        derivative of the central cosine transform w.r.t. <i>k</i>
     * @param fncSq0        conjugate central sine transform (transit time factor)
     * @param fncDSq0       derivative of the conjugate central sine transform w.r.t. <i>k</i>
     *
     * @since  Sep 23, 2015   by Christopher K. Allen
     */
    public AxialFieldSpectrum(
            double dblFreq,
            double dblOffset,
            IRealFunction fncTz0, IRealFunction fncDTz0, 
            IRealFunction fncSq0, IRealFunction fncDSq0 
            ) 
    {
        // Get common parameters
        this.dblFrq = dblFreq;
        this.dblFldOSet = dblOffset;
        

        // Set parameters for the partial field acceleration model
        //  then set old model flag
        this.fncTz0  = fncTz0;
        this.fncSq0  = fncSq0;
        this.fncDTz0 = fncDTz0;
        this.fncDSq0 = fncDSq0;
        
        // Set the partial field model flag
        this.bolPrtlFldMdl = true;
        
        // Null out all the full field model parameters
        this.fncTz   = null;
        this.fncDTz  = null;
        this.fncSz   = null;
        this.fncDSz  = null;
        this.fncTq  = null;
        this.fncDTq = null;
        this.fncSq  = null;
        this.fncDSq = null;
    }


    /**
     * <p>
     * Constructor for creating the full field model using the sine and cosine
     * transform and their conjugates.  The pre- and post-envelope spectra are 
     * build from these objects and used to compute acceleration parameters.
     * </p>
     * <p>
     * Note that frequency <i>f</i> is not needed here since the given arguments are
     * assumed to be functions of <i>k</i>.  RF frequency is only needed to convert
     * from wave number <i>k</i> to normalized velocity &beta; in the case of 
     * spectral functions that are functions of &beta; 
     * (e.g., <i>T</i>(&beta;), <i>S</i>(&beta;), etc.).
     * </p>
     *
     * @param fncTz         field cosine transform (transit time factor)
     * @param fncDTz        derivative of the cosine transform w.r.t. <i>k</i>
     * @param fncSq         field conjugate sine transform (transit time factor)
     * @param fncDSq        derivative of the sine transform w.r.t. <i>k</i>
     * @param fncTq         field conjugate cosine transform (transit time factor)
     * @param fncDTq        derivative of the conjugate cosine transform w.r.t. <i>k</i>
     * @param fncSz         field sine transform (transit time factor)
     * @param fncDSz        derivative of the field sine transform w.r.t. <i>k</i>
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    public AxialFieldSpectrum(
            IRealFunction fncTz, IRealFunction fncDTz,
            IRealFunction fncSz, IRealFunction fncDSz,
            IRealFunction fncTq, IRealFunction fncDTq, 
            IRealFunction fncSq, IRealFunction fncDSq 
            ) 
    {
        super();
        
        // Set all the parameters for the full field model
        //  The primary spectra
        this.fncTz  = fncTz;
        this.fncDTz = fncDTz;
        this.fncSz  = fncSz;
        this.fncDSz = fncDSz;
        
        //  The quadrature components
        this.fncTq = fncTq;
        this.fncDTq = fncDTq;
        this.fncSq = fncSq;
        this.fncDSq = fncDSq;

        // Clear the partial field model flag and offset parameter
        this.bolPrtlFldMdl = false;
        
        // Null out all the partial field model parameters
        this.dblFldOSet = 0.0;
        this.dblFrq     = 0.0; // frequency is only used to convert from k to beta
        
        this.fncTz0  = null;
        this.fncDTz0 = null;
        this.fncSq0  = null;
        this.fncDSq0 = null;
    }
    
    
    /*
     * Attributes
     */
    
    /**
     * Returns <code>true</code> when this object has been initialized to use the
     * partial field model.
     * 
     * @return
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    public boolean  isPartialFieldModel() {
        return this.bolPrtlFldMdl;
    }
    
    /**
     * Returns the time-harmonic frequency of this electric field.
     * 
     * @return      the RF frequency of the electric field (Hz)
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    public double   getFrequency() {
        return this.dblFrq;
    }
    
    /**
     * <p>
     * Returns the offset between the axis origin and the center of the
     * field, however that is defined.  That is, it could be defined as the point
     * of maximum field, or the center of mass, etc.
     * </p>
     * <p>
     * If the full field model is being used then this parameter is not defined
     * and a <code>null</code> value will be returned.
     * </p>
     *  
     * @return  offset between field center and axis origin in partial field model (<b>meters</b>),
     *          or <code>null</code> if the full field model is being used
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    public Double getFieldOffset() {
        
        if (this.bolPrtlFldMdl)
            return this.dblFldOSet;
        
        return null;
    }
    
    
    /*
     * Complex Spectra
     */
    
    /**
     * <p>
     * Computes and returns the complex spectra of this spatial field at the given
     * wave number <i>k</i>.  The value returned has the formula 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sub><i>z</i></sub>(<i>k</i>) = <i>T</i><sub><i>z</i></sub>(</i>k</i>) - <i>i</i> <i>S</i><sub><i>z</i></sub>(<i>k</i>)
     * <br/>
     * <br/>
     * where &Escr;<sub><i>z</i></sub> is the returned spectral value, <i>T</i><sub><i>z</i></sub> is the cosine transit-time
     * factor, and <i>S</i><sub><i>z</i></sub> is the sine transit-time factor.
     * </p>
     * <p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total potential 
     * drop <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(<i>z</i>)<i>dz</i> so
     * that the resulting total potential of the field is 1.
     * </p>
     * 
     * @param k     particle wave number (in radians/meter)
     * 
     * @return      the Fourier spectrum &Escr;(<i>k</i>) of the field at the given wave number <i>k</i>
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public Complex  fldSpectrum(double k) {
        double  dblReal = +this.Tz(k);
        double  dblImag = -this.Sz(k);
        
        Complex cpxSpectra = new Complex(dblReal, dblImag);
        
        return cpxSpectra;
    }
    
    /**
     * <p>
     * Computes and returns the derivative, with respect to the wave number <i>k</i>,
     * of the field spectrum at the given wave number <i>k</i>.  The value returned 
     * has the formula 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sub><i>z</i></sub>'(<i>k</i>) = <i>T</i>'<sub><i>z</i></sub>(</i>k</i>) - <i>i</i> <i>S</i><sub><i>z</i></sub>'(<i>k</i>)
     * <br/>
     * <br/>
     * where &Escr;<sub><i>z</i></sub>' is the returned spectral derivative, 
     * <i>T</i><sub><i>z</i></sub>' is the cosine transit-time
     * factor derivative, and <i>S</i><sub><i>z</i></sub>' is the sine 
     * transit-time factor derivative of the field.
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total potential 
     * drop <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(<i>z</i>)<i>dz</i> so
     * that the resulting total potential of the field is 1.
     * </p>
     * 
     * @param k     particle wave number (in radians/meter)
     * 
     * @return      Fourier spectrum derivative &Escr;'(<i>k</i>), w.r.t. <i>k</i>, of the 
     *              field at the given wave number <i>k</i> (meters/radian)
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public Complex  dkFldSpectrum(double k) {
        double  dblReal = +this.dkTz(k);
        double  dblImag = -this.dkSz(k);
        
        Complex cpxSpectra = new Complex(dblReal, dblImag);
        
        return cpxSpectra;
    }
    
    /**
     * <p>
     * Computes and returns the complex spectral of the conjugate spatial field 
     * at the given wave number <i>k</i>.  The value returned has the formula 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sub><i>q</i></sub>(<i>k</i>) = <i>T<sub>q</sub></i>(</i>k</i>) - <i>i</i> <i>S<sub>q</sub></i>(<i>k</i>)
     * <br/>
     * <br/>
     * where &Escr;<i><sub>q</sub></i> is the returned spectral value, 
     * <i>T<sub>q</sub></i> is the cosine transit-time
     * factor of the quadrature field, and <i>S<sub>q</sub></i> is the sine 
     * transit-time factor of the quadrature field.
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total potential 
     * drop <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(<i>z</i>)<i>dz</i> so
     * that the resulting total potential of the field is 1.
     * </p>
     * 
     * @param k     particle wave number (in radians/meter)
     * 
     * @return      the Fourier spectrum &Escr;<sub><i>q</i></sub>(<i>k</i>) 
     *              of the quadrature field at the given wave number <i>k</i> (unitless)
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public Complex  cnjSpectrum(double k) {
        double  dblReal = +this.Tq(k);
        double  dblImag = -this.Sq(k);
        
        Complex cpxSpectra = new Complex(dblReal, dblImag);
        
        return cpxSpectra;
    }
    
    /**
     * <p>
     * Computes and returns the derivative, with respect to the wave number <i>k</i>,
     * of the quadrature field spectrum 
     * at the given wave number <i>k</i>.  The value returned has the formula 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sub><i>q</i></sub>'(<i>k</i>) = <i>T<sub>q</sub></i>'(</i>k</i>) - <i>i</i> <i>S<sub>q</sub></i>'(<i>k</i>)
     * <br/>
     * <br/>
     * where &Escr;<i><sub>q</sub></i>' is the returned spectral derivative, 
     * <i>T<sub>q</sub></i>' is the cosine transit-time
     * factor derivative of the quadrature field, and <i>S<sub>q</sub></i> is the sine 
     * transit-time factor derivative of the quadrature field.
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total potential 
     * drop <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(<i>z</i>)<i>dz</i> so
     * that the resulting total potential of the field is 1.
     * </p>
     * 
     * @param k     particle wave number (in radians/meter)
     * 
     * @return      Fourier spectrum derivative &Escr;<sub><i>q</i></sub>'(<i>k</i>) w.r.t. <i>k</i> of the quadrature 
     *              field at the given wave number <i>k</i> (meters/radian)
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public Complex  dkCnjSpectrum(double k) {
        double  dblReal = +this.dkTq(k);
        double  dblImag = -this.dkSq(k);
        
        Complex cpxSpectra = new Complex(dblReal, dblImag);
        
        return cpxSpectra;
    }
    

    /*
     * Spectral Operations
     */
    
    /**
     * <p>
     * Compute and return the spectral pre-envelope &Escr;<sup>-</sup>(<i>k</i>).
     * The spectral pre- and post-envelopes turn out to be equal to odd and even
     * combinations, respectively, of the field spectra.  Specifically, the pre- and
     * post-envelopes &Escr;<sup>-</sup>(<i>k</i>) and &Escr;<sup>+</sup>(<i>k</i>) are
     * given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sup>-</sup>(<i>k</i>) = (1/2)[ &Escr;<sub>z</sub>(<i>k</i>) - &Escr;<sub>q</sub>(<i>k</i>) ] ,
     * <br/>
     * &nbsp; &nbsp; &Escr;<sup>+</sup>(<i>k</i>) = (1/2)[ &Escr;<sub>z</sub>(<i>k</i>) + &Escr;<sub>q</sub>(<i>k</i>) ] ,
     * <br/>
     * <br/>
     * where &Escr;<sub>z</sub>(<i>k</i>) is the Fourier spectrum of the axial field and
     * &Escr;<sub>q</sub>(<i>k</i>) is the spectrum of the quadrature field 
     * (the conjugate Fourier transform).
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total potential 
     * drop <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(<i>z</i>)<i>dz</i> so
     * that the resulting total potential of the field is 1.
     * </p>
     * 
     * @param k     the particle wave number (radians/meter)
     * 
     * @return      the pre-envelope &Escr;<sup>-</sup>(<i>k</i>) at the given wave number (unitless)
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public Complex  preEnvSpectrum(double k) {
        Complex     cpxFldSpc = this.fldSpectrum(k);
        Complex     cpxCnjSpc = this.cnjSpectrum(k);
        Complex     cpxPreEnv = cpxFldSpc.minus(cpxCnjSpc);
        
        return cpxPreEnv.divide(2.0);
    }
    
    /**
     * <p>
     * Compute and return the derivative of the spectral pre-envelope 
     * &Escr;<sup>-</sup>(<i>k</i>), that is <i>d</i>&Escr;<sup>-</sup>(<i>k</i>)/<i>dk</i>.
     * For a description of the pre-envelope see <code>{@link #preEnvSpectrum(double)}</code>
     * or the class documentation.
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total potential 
     * drop <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(<i>z</i>)<i>dz</i> so
     * that the resulting total potential of the field is 1.
     * </p>
     * 
     * @param k     the particle wave number (radians/meter)
     * 
     * @return      derivative of the pre-envelope spectrum <i>d</i>&Escr;<sup>-</sup>(<i>k</i>)/<i>dk</i>
     *              at the given wave number (in meters/radian)
     *
     * @since  Oct 1, 2015,   Christopher K. Allen
     * 
     * @see #preEnvSpectrum(double)
     */
    public Complex  dkPreEnvSpectrum(double k) {
        Complex     cpxDFldSpc = this.dkFldSpectrum(k);
        Complex     cpxDCnjSpc = this.dkCnjSpectrum(k);
        Complex     cpxDPreEnv = cpxDFldSpc.minus(cpxDCnjSpc);
        
        return cpxDPreEnv.divide(2.0);
    }
    
    /**
     * <p>
     * Compute and return the spectral post-envelope &Escr;<sup>+</sup>(<i>k</i>).
     * The spectral pre- and post-envelopes turn out to be equal to odd and even
     * combinations, respectively, of the field spectra.  Specifically, the pre- and
     * post-envelopes &Escr;<sup>-</sup>(<i>k</i>) and &Escr;<sup>+</sup>(<i>k</i>) are
     * given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Escr;<sup>-</sup>(<i>k</i>) = (1/2)[ &Escr;<sub>z</sub>(<i>k</i>) - &Escr;<sub>q</sub>(<i>k</i>) ] ,
     * <br/>
     * &nbsp; &nbsp; &Escr;<sup>+</sup>(<i>k</i>) = (1/2)[ &Escr;<sub>z</sub>(<i>k</i>) + &Escr;<sub>q</sub>(<i>k</i>) ] ,
     * <br/>
     * <br/>
     * where &Escr;<sub>z</sub>(<i>k</i>) is the Fourier spectrum of the axial field and
     * &Escr;<sub>q</sub>(<i>k</i>) is the spectrum of the quadrature field 
     * (the conjugate Fourier transform).
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total potential 
     * drop <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(<i>z</i>)<i>dz</i> so
     * that the resulting total potential of the field is 1.
     * </p>
     * 
     * @param k     the particle wave number (radians/meter)
     * 
     * @return      the post-envelope &Escr;<sup>+</sup>(<i>k</i>) at the given wave number (unitless)
     *
     * @since  Sep 30, 2015,   Christopher K. Allen
     */
    public Complex  postEnvSpectrum(double k) {
        Complex     cpxFldSpc = this.fldSpectrum(k);
        Complex     cpxCnjSpc = this.cnjSpectrum(k);
        Complex     cpxPstEnv = cpxFldSpc.plus(cpxCnjSpc);
        
        return cpxPstEnv.divide(2.0);
    }
    
    /**
     * <p>
     * Compute and return the derivative of the spectral post-envelope 
     * &Escr;<sup>+</sup>(<i>k</i>), that is <i>d</i>&Escr;<sup>+</sup>(<i>k</i>)/<i>dk</i>.
     * For a description of the post-envelope see <code>{@link #postEnvSpectrum(double)}</code>
     * or the class documentation.
     * </p>
     * <h4>NOTES:</h4>
     * &middot; We assume that the electric field is normalized by its total potential 
     * drop <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(<i>z</i>)<i>dz</i> so
     * that the resulting total potential of the field is 1.
     * </p>
     * 
     * @param k     the particle wave number (radians/meter)
     * 
     * @return      derivative of the post-envelope spectrum <i>d</i>&Escr;<sup>-</sup>(<i>k</i>)/<i>dk</i>
     *              at the given wave number (in meters/radian)
     *
     * @since  Oct 1, 2015,   Christopher K. Allen
     * 
     * @see #postEnvSpectrum(double)
     */
    public Complex  dkPostEnvSpectrum(double k) {
        Complex     cpxDFldSpc = this.dkFldSpectrum(k);
        Complex     cpxDCnjSpc = this.dkCnjSpectrum(k);
        Complex     cpxDPstEnv = cpxDFldSpc.plus(cpxDCnjSpc);
        
        return cpxDPstEnv.divide(2.0);
    }
    
    
    /*
     * Spectral Components
     */
    
    /**
     * Returns the cosine transit time factor <i>T</i><sub><i>z</i></sub>, proportional to the Fourier
     * cosine transform, for the given wave number.
     * 
     * @param k     particle wave number with respect to the RF frequency (radians/meter)
     * 
     * @return      the value of <i>T</i>(<i>k</i>) (unitless)
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    public double   Tz(double k) {
        double      Tz;
        
        if (this.bolPrtlFldMdl)
            Tz = this.TzfromTz0(k);
        else
            Tz = this.fncTz.evaluateAt(k);
            
        return Tz;
    }
    
    /**
     * Returns the derivative of the cosine transit time factor <i>T</i><sub><i>z</i></sub> w.r.t.
     * the wave number <i>k</i>.
     * 
     * @param k     particle wave number with respect to the RF frequency (radians/meter)
     * 
     * @return      the value of <i>dT</i><sub><i>z</i></sub>(<i>k</i>)/<i>dk</i> (meters/rad)
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    public double   dkTz(double k) {
        double      dTz;
        
        if (this.bolPrtlFldMdl)
            dTz = this.dkTzfromDkTz0(k);
        else
            dTz = this.fncDTz.evaluateAt(k);
        
        return dTz;
    }
    
    /**
     * Returns the sine transit-time factor <i>S</i><sub><i>z</i></sub>, proportional to the Fourier 
     * sine transform of the axial field, for the given wave number.
     * 
     * @param k     particle wave number with respect to the RF frequency (radians/meter)
     * 
     * @return      the value of <i>S</i><sub><i>z</i></sub>(<i>k</i>) (unitless)
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    public double   Sz(double k) {
        double      Sz;
        
        if (this.bolPrtlFldMdl)
            Sz = this.SzfromTz0(k);
        else
            Sz = this.fncSz.evaluateAt(k);
        
        return Sz;
    }
    
    /**
     * Returns the derivative of the sine transit-time factor <i>dS</i><sub><i>z</i></sub>/<i>dk</i>
     * with respect to the particle wave number <i>k</i>
     * .
     * @param k     particle wave number with respect to the RF frequency (radians/meter)
     * 
     * @return      the value of <i>dS</i><sub><i>z</i></sub>(<i>k</i>)/<i>dk</i> (meters/radian)
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    public double   dkSz(double k) {
        double      dSz;
        
        if (this.bolPrtlFldMdl) 
            dSz = this.dkSzfromDkTz0(k);
        else
            dSz = this.fncDSz.evaluateAt(k);
        
        return dSz;
    }
    
    /**
     * Returns the conjugate cosine transit-time factor <i>T<sub>q</sub></i>, proportional
     * to the Fourier cosine transform of the field sgn(<i>z</i>)<i>E<sub>z</sub>(<i>z</i>).
     * 
     * @param k     the particle wave number w.r.t. the RF frequency (radians/meter)
     *  
     * @return      the conjugate cosine transit-time factor <i>T<sub>q</sub></i>(<i>k</i>) (unitless)
     *
     * @since  Sep 29, 2015   by Christopher K. Allen
     */
    public double   Tq(double k) {
        double      Tq;
        
        if (this.bolPrtlFldMdl)
            Tq = this.TqFromSq0(k);
        else
            Tq = this.fncTq.evaluateAt(k);
        
        return Tq;
    }
    
    /**
     * Returns the derivative <i>dT</i>(<i>k</i>)/<i>dk</i> of the conjugate cosine
     * transit-time factor w.r.t. the particle wave number <i>k</i>. 
     * 
     * @param k     the particle wave number w.r.t. the RF frequency (radians/meter)
     * 
     * @return      the value of <i>dT<sub>q</sub></i>(<i>k</i>)/<i>dk</i> (meters/rad)
     *
     * @since  Sep 29, 2015,   Christopher K. Allen
     */
    public double   dkTq(double k) {
        double      dTq;
        
        if (this.bolPrtlFldMdl) 
            dTq = this.dkTqFromDkSq0(k);
        else
            dTq = this.fncDTq.evaluateAt(k);
            
        return dTq;
    }
    
    /**
     * Returns the conjugate sine transit-time factor <i>S<sub>q</sub></i>, proportional
     * to the Fourier sine transform of the field sgn(<i>z</i>)<i>E<sub>z</sub></i>(<i>z</i>).
     * 
     * @param k     the particle wave number w.r.t. the RF frequency (radians/meter)
     *  
     * @return      the conjugate cosine transit-time factor <i>S<sub>q</sub></i>(<i>k</i>) (unitless)
     *
     * @since  Sep 29, 2015   by Christopher K. Allen
     */
    public double   Sq(double k) {
        double      Sq;
        
        if (this.bolPrtlFldMdl)
            Sq = this.SqFromSq0(k);
        else
            Sq = this.fncSq.evaluateAt(k);
        
        return Sq;
    }
    
    /**
     * Returns the derivative <i>dS</i>(<i>k</i>)/<i>dk</i> of the conjugate sine
     * transit-time factor w.r.t. the particle wave number <i>k</i>. 
     * 
     * @param k     the particle wave number w.r.t. the RF frequency (radians/meter)
     * 
     * @return      the value of <i>dS<sub>q</sub></i>(<i>k</i>)/<i>dk</i> (meters/rad)
     *
     * @since  Sep 29, 2015,   Christopher K. Allen
     */
    public double   dkSq(double k) {
        double      dSq;
        
        if (this.bolPrtlFldMdl) 
            dSq = this.dkSqFromDkSq0(k);
        else
            dSq = this.fncDSq.evaluateAt(k);
            
        return dSq;
    }
    

    /*
     * Support - Partial Field Operations
     */
    
    /**
     * <p>
     * Compute and return the standard transit time factor <i>T</i>(<i>k</i>) which
     * includes any gap "offsets", from the given particle velocity.  The value
     * is computed from the symmetric transit time factor <i>T</i><sub>0</sub> which
     * is evaluated with the coordinate origin at the point of field symmetry.
     * </p>
     * <p>
     * The returned value <i>T</i><sub><i>z</i></sub>(<i>k</i>) has the expression
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>T</i></i><sub><i>z</i></sub>(<i>k</i>) = <i>T</i></i><sub><i>z</i>,</sub><sub>0</sub>(&beta;) cos <i>k</i>&Delta;<i>z</i> ,
     * <br/>
     * <br/>
     * where <i>T</i></i><sub><i>z</i></sub><sub>0</sub> is the cosine transit time factor taken with 
     * origin at point of field symmetry, &beta; is the normalized particle velocity, 
     * <i>k</i> &trie; 2&pi;/&beta;&lambda; 
     * is the wave number, and &Delta;<i>z</i> is the offset of the point of field
     * symmetry from the origin.
     * </p>
     * 
     * @param k         particle wave number with respect to the RF frequency (radians/meter)
     * 
     * @return          the transit time factor (Fourier cosine transform) evaluated at <i>k</i> (unitless)
     *
     * @since  Feb 13, 2015   by Christopher K. Allen
     */
    private double  TzfromTz0(double k) {
//        double k   = this.waveNumber(beta);
        double dz  = - this.getFieldOffset();
        double cos = Math.cos(k*dz);

        double beta = this.computeVelocity(k);
        double T0   = this.fncTz0.evaluateAt(beta);
        double Tz   = T0*cos;
        
        return Tz;
    }
    
    /**
     * <p>
     * Compute and return the derivative of the standard transit time factor 
     * <i>T'</i></i><sub><i>z</i></sub>(<i>k</i>) with respect to <i>k</i> 
     * including any gap "offsets."  The value is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>T</i></i><sub><i>z</i></sub>'(<i>k</i>) = (-&beta;/<i>k</i>)<i>T'</i><sub><i>z</i></sub><sub>0</sub>(&beta;) cos <i>k</i>&Delta;<i>z</i> 
     *                                 - &Delta;<i>z</i> <i>T</i><sub>0</sub>(&beta;) sin <i>k</i>&Delta;<i>z</i>,
     * <br/>
     * <br/>
     * whenever <i>T'</i><sub><i>z</i></sub><sub>0</sub>(&beta;) is taken w.r.t. velocity &beta;, or
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>T</i></i><sub><i>z</i></sub>'(<i>k</i>) = <i>T'</i><sub><i>z</i></sub><sub>0</sub>(&beta;) cos <i>k</i>&Delta;<i>z</i> 
     *                                 - &Delta;<i>z</i> <i>T</i><sub>0</sub>(&beta;) sin <i>k</i>&Delta;<i>z</i>,
     * <br/>
     * <br/>
     * whenever <i>T'</i><sub><i>z</i></sub><sub>0</sub>(&beta;) is taken  w.r.t. wave number <i>k</i>.
     * where <i>T</i>'</i><sub><i>z</i></sub><sub>0</sub> is the derivative of the symmetric cosine transit time factor, 
     * <i>T</i><sub>0</sub>(&beta;) is the symmetric cosine transit time factor, &beta; is the normalized
     * particle velocity,
     * <i>k</i> &trie; 2&pi;/&beta;&lambda; 
     * is the wave number, and &Delta;<i>z</i> is the offset of the point of field
     * symmetry from the origin.
     * </p>
     * 
     * @param k         particle wave number with respect to the RF frequency (radians/meter)
     * 
     * @return          the derivative of cosine transit time factor w.r.t. wave number <i>k</i> (meters/radian)
     *
     * @since  Feb 16, 2015   by Christopher K. Allen
     * @version July 29, 2015: Modified to assume 
     *          <code>fitTTFPrime</code> = <i>dT</i><sub>0</sub>(&beta;)/<i>dk</i>
     * @version Nov 9, 2015: Modified to assume 
     *          <code>fitTTFPrime</code> = <b>-</b><i>dT</i><sub>0</sub>(&beta;)/<i>dk</i>
     */
    private double  dkTzfromDkTz0(double k) {
//        double k   = this.waveNumber(beta);
        double dz  = - this.getFieldOffset();
        double cos = Math.cos(k*dz);
        double sin = Math.sin(k*dz);

        double beta = this.computeVelocity(k);

        double dTz0 = this.fncDTz0.evaluateAt(beta); 
        double Tz0  = this.fncTz0.evaluateAt(beta);
        
        // TODO - Pick One :-)
//        double dT   = dTz0*cos - Tz0*dz*sin;          // this one if T' = dT/dk
//        double dT   = (-beta/k)*dTz0*cos - Tz0*dz*sin;  // this one if T' = dT/db
        
//        double dT   = (-1.0/DBL_2PI)*dTz0*cos - Tz0*dz*sin;  // Use: magic number seems correct
        double dT   = 0.01*dTz0*cos - Tz0*dz*sin;  // To compare with XAL implementation
        
        return dT;
    }
    
    /**
     * <p>
     * Compute and return the standard transit time factor <i>S</i></i><sub><i>z</i></sub>(<i>k</i>) which
     * includes any gap "offsets", from the given particle velocity.  The value
     * is computed from the symmetric transit time factor <i>S</i></i><sub><i>z</i></sub><sub>0</sub> which
     * is evaluated with the coordinate origin at the point of field symmetry.
     * </p>
     * <p>
     * The returned value <i>S</i></i><sub><i>z</i></sub>(<i>k</i>) has the expression
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>S</i></i><sub><i>z</i></sub>(<i>k</i>) = <i>T</i></i><sub><i>z</i></sub><sub>0</sub>(&beta;) sin <i>k</i>&Delta;<i>z</i> ,
     * <br/>
     * <br/>
     * where &beta; is the normalized particle velocity, 
     * <i>T</i></i><sub><i>z</i></sub><sub>0</sub> is the cosine transit time factor 
     * taken with origin at point of field symmetry, <i>k</i> &trie; 2&pi;/&beta;&lambda; 
     * is the wave number, and &Delta;<i>z</i> is the offset of the point of field
     * symmetry from the origin. Note that the transit time factor <i>S</i><sub>0</sub>
     * is zero since it is taken about the point of field symmetric.
     * </p>
     * 
     * @param k         particle wave number with respect to the RF frequency (radians/meter)
     * 
     * @return          the transit time factor (Fourier sine transform) evaluated at <i>k</i>
     *
     * @since  Feb 16, 2015   by Christopher K. Allen
     */
    private double  SzfromTz0(double k) {
//        double k   = this.waveNumber(beta);
        double dz  = - this.getFieldOffset();
        double sin = Math.sin(k*dz);

        double beta = this.computeVelocity(k);
        double Tz0  = this.fncTz0.evaluateAt(beta);
        double Sz    = Tz0*sin;
        
        return Sz;
    }
    
    /**
     * <p>
     * Compute and return the derivative of the standard transit time factor 
     * <i>S'</i></i><sub><i>z</i></sub>(<i>k</i>) with respect to <i>k</i> including
     * any gap "offsets." The value is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>S</i>'</i><sub><i>z</i></sub>(<i>k</i>) = <i>T</i>'</i><sub><i>z</i></sub><sub>0</sub>(&beta;(<i>k</i>)) sin <i>k</i>&Delta;<i>z</i> 
     *                                 + &Delta;<i>z</i> <i>T</i></i><sub><i>z</i></sub><sub>0</sub>(&beta;(<i>k</i>)) cos <i>k</i>&Delta;<i>z</i>,
     * <br/>
     * <br/>
     * where <i>T</i>'</i><sub><i>z</i></sub><sub>0</sub> is the derivative of the symmetric cosine transit time factor, 
     * <i>T</i></i><sub><i>z</i>0</sub>(&beta;) is the cosine transit time factor, 
     * <i>k</i> &trie; 2&pi;/&beta;&lambda; 
     * is the wave number, and &Delta;<i>z</i> is the offset of the point of field
     * symmetry from the origin.
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * &middot; I am unsure whether or not the approximation for <i>dS</i>(&beta;)/<i>dk</i> or 
     * <i>dS</i>(&beta;)/<i>d</i>&beta; is stored.  If it is the later then the returned value should be
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>S</i>'</i><sub><i>z</i></sub>(<i>k</i>) = (-&beta;/<i>k</i>)<i>T</i>'</i><sub><i>z</i></sub><sub>0</sub>(&beta;) sin <i>k</i>&Delta;<i>z</i> 
     *                                 + &Delta;<i>z</i> <i>T</i></i><sub><i>z</i></sub><sub>0</sub>(&beta;) cos <i>k</i>&Delta;<i>z</i>,
     * <br/>
     * <br/>
     * </p> 
     * 
     * @param k         particle wave number with respect to the RF frequency (radians/meter)
     * 
     * @return          the derivative of sine transit time factor <i>S</i>(<i>k</i>) w.r.t. wave number <i>k</i> (meters/radian)
     *
     * @since  Feb 16, 2015   by Christopher K. Allen
     * @version July 29, 2015 modified to assume <code>fitSTFPrime</code> = <i>dS</i><sub>0</sub>(&beta;)/<i>dk</i>
     */
    private double  dkSzfromDkTz0(double k) {
//        double k   = this.waveNumber(beta);
        double dz  = - this.getFieldOffset();
        double sin = Math.sin(k*dz);
        double cos = Math.cos(k*dz);
        
        double beta = this.computeVelocity(k);
        double dTz0 = this.fncDTz0.evaluateAt(beta);
        double Tz0  = this.fncTz0.evaluateAt(beta);
        
        // TODO - Pick One :-)
//        double dS   = dTz0*sin + Tz0*dz*cos;          // this one if T' = dT/dk
//        double dS   = (-beta/k)*dTz0*sin + Tz0*dz*cos;  // this one if T' = dT/db
        
//        double dS   = (-1.0/DBL_2PI)*dTz0*sin + Tz0*dz*cos; // the magic factor gives results close to what I know is true
        double dS   = 0.01*dTz0*sin + Tz0*dz*cos; // compare with XAL implementation
        
        return dS;
    }
    
    
    /**
     * <p>
     * Compute and return the conjugate transit time factor <i>T<sub>q</sub></i>(<i>k</i>) which
     * includes any gap "offsets", from the given particle velocity.  The value
     * is computed from the conjugate symmetric transit time factor <i>S</i><sub><i>q</i>,0</sub> which
     * is evaluated with the coordinate origin at the point of field symmetry.
     * </p>
     * <p>
     * The returned value <i>T<sub>q</sub></i>(<i>k</i>) has the expression
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>T<sub>q</sub></i>(<i>k</i>) = -<i>S</i><sub><i>q</i>,0</sub>(&beta;) sin(<i>k</i>&Delta;<i>z</i>) ,
     * <br/>
     * <br/>
     * where <i>S</i><sub><i>q</i>,0</sub> is the conjugate sine transit time factor taken with 
     * origin at point of field symmetry, <i>k</i> &trie; 2&pi;/&beta;&lambda; 
     * is the wave number, and &Delta;<i>z</i> is the offset of the point of field
     * symmetry from the origin.
     * </p>
     * 
     * @param k         particle wave number with respect to the RF frequency (radians/meter)
     * 
     * @return          the transit time factor (conjugate Fourier cosine transform) evaluated at <i>k</i> (meters/radian)
     *
     * @since  Sept 23, 2015   by Christopher K. Allen
     */
    private double  TqFromSq0(double k) {
//        double k   = this.waveNumber(beta);
        double dz  = - this.getFieldOffset();
        double sin = Math.sin(k*dz);

        double beta = this.computeVelocity(k);
        double Sq0  = this.fncSq0.evaluateAt(beta);
        double Tq   = -Sq0*sin;
        
        return Tq;
    }
    
    /**
     * <p>
     * Compute and return the derivative of the conjugate transit time factor 
     * <i>T<sub>q</sub>'</i>(<i>k</i>) with respect to <i>k</i> or &beta; including any gap "offsets." 
     * We are unsure because the provided data for <i>S</i><sub><i>q</i>,0</sub>(&beta;) never specified
     * the derivative parameter.  So were are guessing for now.
     * </p>
     * <p>
     * The value is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>T<sub>q</sub></i>'(<i>k</i>) = -<i>S'</i><sub><i>q</i>,0</sub>(&beta;) sin <i>k</i>&Delta;<i>z</i> 
     *                                 - &Delta;<i>z</i> <i>S</i><sub><i>q</i>,0</sub>(&beta;) cos <i>k</i>&Delta;<i>z</i>,
     * <br/>
     * <br/>
     * where <i>S'</i><sub><i>q</i>,0</sub> is the derivative of the conjugate sine transit time factor
     * for the symmetric field, 
     * <i>S</i><sub><i>q</i>,0</sub>(&beta;) is the conjugate sine transit time factor, 
     * <i>k</i> &trie; 2&pi;/&beta;&lambda; 
     * is the wave number, and &Delta;<i>z</i> is the offset of the point of field
     * symmetry from the origin.
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * &middot;  I am unsure of whether the polynomial stored is for <i>dS<sub>q</sub></i>(&beta;)/<i>d</i>&beta; or for
     * d<i>S<sub>q</sub></i>(&beta;)/<i>dk</i>.  If it is the former we need <i>d</i>&beta;/<i>dk</i> = -&beta;/<i>k</i> and 
     * the value of <i>T'<sub>q</sub></i> becomes
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>T<sub>q</sub></i>'(&beta;) = -(-&beta;/<i>k</i>)<i>S'</i><sub><i>q</i>,0</sub>(&beta;) sin <i>k</i>&Delta;<i>z</i> 
     *                                 - &Delta;<i>z</i> <i>S</i><sub><i>q</i>,0</sub>(&beta;) cos <i>k</i>&Delta;<i>z</i> ,
     * <br/>
     * <br/>
     * </p>
     * 
     * @param k         particle wave number with respect to the RF frequency (radians/meter)
     * 
     * @return          the derivative of conjugate sine transit time factor w.r.t. velocity &beta; or wave number <i>k</i> (unitless or meters/radian)
     *
     * @since  Feb 16, 2015   by Christopher K. Allen
     * @version July 29, 2015: Modified to assume <code>fitTTFPrime</code> = <i>dT</i><sub>0</sub>(&beta;)/<i>dk</i>
     *          <br/>
     *          Sept 23, 2015: Modified to assume <code>fitSTF</code> actually contains <i>S<sub>q</i></sub>(&beta;)
     */
    private double  dkTqFromDkSq0(double k) {
        double dz  = - this.getFieldOffset();
        double cos = Math.cos(k*dz);
        double sin = Math.sin(k*dz);
        
        double beta = this.computeVelocity(k);
        double dSq0 = this.fncDSq0.evaluateAt(beta);
        double Sq0  = this.fncSq0.evaluateAt(beta);
        
        // TODO - Pick One :-)
//      double dTq  = -dSq0*sin - Sq0*dz*cos;   // this one if S' = dS/dk
//      double dTq  = -(-beta/k)*dSq0*sin - Sq0*dz*cos;   // this one if S' = dS/db
        
//        double dTq  = -(-1.0/DBL_2PI)*dSq0*sin - Sq0*dz*cos; // The magic number works for T'(b)
        double dTq  = -0.01*dSq0*sin - Sq0*dz*cos; // To emulate XAL implementation
        
        return dTq;
    }
    
    /**
     * <p>
     * Compute and return the conjugate transit time factor <i>S<sub>q</sub></i>(<i>k</i>) which
     * includes any gap "offsets", from the given particle velocity.  The value
     * is computed from the transit time factor <i>S</i><sub><i>q</i>,0</sub> for the 
     * symmetric field .
     * </p>
     * <p>
     * The returned value <i>S<sub>q</sub></i>(<i>k</i>) has the expression
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>S<sub>q</sub></i>(<i>k</i>) = <i>S</i><sub><i>q</i>,0</sub>(&beta;) cos <i>k</i>&Delta;<i>z</i> ,
     * <br/>
     * <br/>
     * where <i>S</i><sub><i>q</i>,0</sub> is the conjugate sine transit time factor taken with 
     * origin at point of field symmetry, <i>k</i> &trie; 2&pi;/&beta;&lambda; 
     * is the wave number, and &Delta;<i>z</i> is the offset of the point of field
     * symmetry from the origin. Note that the transit time factor <i>T</i><sub><i>q</i>,0</sub>
     * is zero since it is taken about the point of field symmetric.
     * </p>
     * 
     * @param k     particle wave number w.r.t. the RF frequency (radians/meter)
     * 
     * @return      conjugate transit time factor <i>S</i><sub><i>q</i></sub> evaluated at <i>k</i> (meters/radian)
     *
     * @since  Sep 23, 2015   by Christopher K. Allen
     */
    private double  SqFromSq0(double k) {
//        double k   = this.waveNumber(beta);
        double dz  = - this.getFieldOffset();
        double cos = Math.cos(k*dz);

        double beta = this.computeVelocity(k);
        double Sq0  = this.fncSq0.evaluateAt(beta);
        double Sq   = Sq0*cos;
        
        return Sq;
    }
    
    /**
     * <p>
     * Compute and return the derivative of the conjugate transit time factor 
     * <i>S<sub>q</sub></i>(<i>k</i>) with respect to <i>k</i> (or &beta; ?) 
     * including any gap "offsets." That is, compute the value <i>S'</i><sub><i>q</i></sub>(<i>k</i>).
     * The value is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>S'<sub>q</sub></i>(<i>k</i>) = <i>S'</i><sub><i>q</i>,0</sub>(&beta;) cos <i>k</i>&Delta;<i>z</i> 
     *                                 - &Delta;<i>z</i> <i>S</i><sub><i>q</i>,0</sub>(&beta;) sin <i>k</i>&Delta;<i>z</i> ,
     * <br/>
     * <br/>
     * where <i>S'</i><sub><i>q</i>,0</sub> is the derivative of the conjugate sine transit time factor, 
     * <i>k</i> &trie; 2&pi;/&beta;&lambda; 
     * is the wave number, and &Delta;<i>z</i> is the offset of the point of field
     * symmetry from the origin.
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * &middot;  I am unsure of whether the polynomial stored is for <i>dS<sub>q</sub></i>(&beta;)/<i>d</i>&beta; or for
     * d<i>S<sub>q</sub></i>(&beta;)/<i>dk</i>.  If it is the former we need <i>d</i>&beta;/<i>dk</i> = -&beta;/<i>k</i> and 
     * the value of <i>S'<sub>q</sub></i> becomes
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>S'<sub>q</sub></i>(<i>k</i>) = (-&beta;/<i>k</i>)<i>S</i>'<sub><i>q</i>,0</sub>(&beta;) cos <i>k</i>&Delta;<i>z</i> 
     *                                 - &Delta;<i>z</i> <i>S</i><sub><i>q</i>,0</sub>(&beta;) sin <i>k</i>&Delta;<i>z</i> ,
     * <br/>
     * <br/>
     * </p>
     * 
     * @param k     particle wave number w.r.t. the RF frequency (radians/meter)
     * 
     * @return      the derivative of sine transit time factor w.r.t. velocity &beta;
     *
     * @since  Feb 16, 2015   by Christopher K. Allen
     * @version July 29, 2015 modified to assume <code>fitSTFPrime</code> = <i>dS</i><sub>0</sub>(&beta;)/<i>dk</i>
     */
    private double  dkSqFromDkSq0(double k) {
        double dz  = - this.getFieldOffset();
        double sin = Math.sin(k*dz);
        double cos = Math.cos(k*dz);
        
        double beta = this.computeVelocity(k);
        double dSq0 = this.fncDSq0.evaluateAt(beta);
        double Sq0  = this.fncSq0.evaluateAt(beta);
        
        // TODO - Pick One :-)
//        double dSq  = dSq0*cos - Sq0*dz*sin; // this one if S' = dS/dk
//        double dSq  = (-beta/k)*dSq0*cos - Sq0*dz*sin; // this one if S' = dS/db
        
//        double dSq  = (-1.0/DBL_2PI)*dSq0*cos - Sq0*dz*sin; // magic number works for T'(b)
        double dSq  = 0.01*dSq0*cos - Sq0*dz*sin; // To compare with XAL implementation
        
        return dSq;
    }
    
    /**
     * Compute the normalized particle velocity &beta; for the given particle
     * wave number <i>k</i>.
     * 
     * @param k     wave number of the particle with respect to RF frequency (radians/meter)
     * 
     * @return      the normalized velocity &beta; of the particle for the given wave number <i>k</i>
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    private double computeVelocity(double k) {
        double  lambda = DBL_LGHT_SPD/this.getFrequency();
        double  beta   = DBL_2PI/(k*lambda);
        
        return beta;
    }
    
//  /**
//  * <p>
//  * Compute and return the particle wave number <i>k</i> for the given normalized 
//  * particle velocity &beta;.  The formula is
//  * <br/>
//  * <br/>
//  * &nbsp; &nbsp; <i>k</i> = 2&pi;/&beta;&lambda; ,
//  * <br/>
//  * <br/>
//  * where &lambda; is the wavelength of the accelerating RF.
//  * </p>
//  * 
//  * @param beta      normalized probe velocity
//  * 
//  * @return          particle wave number with respect to the RF
//  *
//  * @since  Feb 16, 2015   by Christopher K. Allen
//  */
// private double waveNumber(double beta) {
//     double lambda = DBL_LGHT_SPD/this.getFrequency();
//     double k      = DBL_2PI/(beta*lambda);
//
//     return k;
// }
 
}
