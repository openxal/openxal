/**
 * FieldSpectrum.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 23, 2015
 */
package xal.tools.beam.em;

import xal.tools.math.fnc.IRealFunction;

/**
 * <p>
 * Class that representing the spectral properties of a longitudinal axial electric field.  
 * The most important properties from a beam physics standpoint are the transit time factors.
 * These are the components of the Fourier transform of the axial field, and their resulting 
 * Hilbert transforms.  The Hilbert transform of a transit time factor turns out to be
 * the transit time factor of axial field times the signum function.  The spectral 
 * pre- and post-envelopes are formed from a transit time factor and its Hilbert transform.
 * These pre- and post-envelopes are the primary entities for computing the pre- and post-gap
 * energy gain and phase jump, respectively.
 * </p>
 * <p>
 * This class attempts to maintain backward compatibility between the RF acceleration model
 * produced by Los Alamos and CERN.  There the <i>T<sub>q</sub></i> and <i>S</i> transit time
 * factors are zero.  Although usually labeled <i>S</i>, the "sine" transit time factor is
 * actually its quadrature conjugate <i>S<sub>q</sub></i>.  That model also requires an
 * "offset" &Delta;<i>z</i> which is the distance between the coordinate origin and the 
 * field center (the point of symmetry).  The assumptions are that the field is symmetric 
 * about the axial location &Delta;<i>z</i>, if not this information is lost.
 * </p>
 * <p>
 * The current model includes all four transit time factors and their derivatives.  Thus,
 * all the field information is kept, no offsets are necessary, and there is information
 * enough to compute the post gap energy gain and phase jump.  (In the above model the
 * post gap quantities are assumed to be equal to the pre-gap quantities.)
 * When polynomial approximations are provided for all four transit time factors this class
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
 * <h4>Transit Time Factors</h4>  
 * There are four (4) transit time factors.  Let the longitudinal electric field 
 * along the beam axis <i>z</i> be denoted <i>E<sub>z</sub></i>(<i>z</i>).  Let the total voltage
 * drop long the field be denoted <i>V</i><sub>0</sub>, that is,
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(<i>z</i>) <i>dz</i> .
 * <br/>
 * <br/>
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
 * Then the 
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
 * by &epsi;<sub><i>z</i></sub>(<i>k</i>) and &epsi;<sub><i>q</i></sub>(<i>k</i>) 
 * the Fourier transforms of
 * the axial field <i>E<sub>z</i>(<i>z</i>) and its conjugate <i>E<sub>q</i></i>(<i>z</i>), 
 * respectively.  That is, the field spectra are
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &epsi;<sub><i>z</i></sub>(k) &trie; &Fscr;[<i>E<sub>z</sub></i>](<i>k</i>) ,
 * <br/>
 * &nbsp; &nbsp; &epsi;<sub><i>q</i></sub>(k) &trie; &Fscr;[<i>E<sub>q</sub></i>](<i>k</i>) ,
 * <br/>
 * <br/>
 * where &Fscr;[&middot;] is the Fourier transform operator.  The Fourier transforms of the fields have
 * the decomposition
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &epsi;<sub><i>z</i></sub>(<i>k</i>) = <i>V</i><sub>0</sub><i>T</i>(<i>k</i>) 
 *                                                  - <i>iV</i><sub>0</sub><i>S</i>(<i>k</i>) ,
 * <br/>                                                 
 * &nbsp; &nbsp; &epsi;<i><sub>q</sub></i>(<i>k</i>) = <i>V</i><sub>0</sub><i>T<sub>q</sub></i>(<i>k</i>) 
 *                                                  - <i>iV</i><sub>0</sub><i>S<sub>q</sub></i>(<i>k</i>) ,
 * <br/>
 * <br/>
 * where <i>i</i> is the imaginary unit.  The spectra are then related by the Hilbert transforms
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Hscr;[&epsi;<sub><i>z</i></sub>(<i>k</i>)] = i&epsi;<i><sub>q</sub></i>(<i>k</i>) ,
 * <br/>                                                 
 * &nbsp; &nbsp; &Hscr;[&epsi;<sub><i>q</i></sub>(<i>k</i>)] = i&epsi;<i><sub>z</sub></i>(<i>k</i>) ,
 * <br/>
 * <br/>
 * Thus, we see &epsi;<sub><i>z</i></sub>(<i>k</i>) and &epsi;<sub><i>q</i></sub>(<i>k</i>) are 
 * conjugates of each other.
 * </p>
 * <p>
 * <h4>Pre- and Post-Envelope Spectra</h4>
 * The pre- and post-envelope spectra can be formed from the field spectra.  First, denote
 * by &epsi;<sup>-</sup>(<i>k</i>) and &epsi;<sup>+</sup>(<i>k</i>) the pre- and post-envelope
 * spectra, respectively.  They are defined
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &epsi;<sub><i>z</i></sub>(k) &trie; &epsi;<sub><i>z</i></sub>(<i>k</i>) - i&Hscr;[&epsi;<sub><i>z</i></sub>(<i>k</i>] 
 *                                            =  &epsi;<sub><i>z</i></sub>(<i>k</i>) -  &epsi;<sub><i>q</i></sub>(<i>k</i>) ,
 * <br/>
 * &nbsp; &nbsp; &epsi;<sub><i>z</i></sub>(k) &trie; &epsi;<sub><i>z</i></sub>(<i>k</i>) + i&Hscr;[&epsi;<sub><i>z</i></sub>(<i>k</i>] 
 *                                            =  &epsi;<sub><i>z</i></sub>(<i>k</i>) +  &epsi;<sub><i>q</i></sub>(<i>k</i>) ,
 * <br/>
 * <br/>
 * Let &phi; be the synchronous particle phase at the gap center.  Then the quantities 
 * <i>e<sup>-i&phi;</sup></i>&epsi;<sup>-</sup>(<i>k</i>) and <i>e<sup>-i&phi;</sup></i>&epsi;<sup>+</sup>(<i>k</i>)
 * contain the pre- and post-gap energy gain &Delta;<i>W</i><sup>-</sup>, &Delta;<i>W</i><sup>+</sup> and 
 * phase jump &Delta;&phi;<sup>-</sup>, &Delta;&phi;<sup>+</sup>, respectively.  For example,
 * the real part of <i>e<sup>-i&phi;</sup></i>&epsi;<sup>-</sup>(<i>k</i>) tracks the pre-gap 
 * energy gain while the imaginary part tracks the phase jump.  We have
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Delta;<i>W</i><sup>-</sup>(<i>k</i>,&phi;) = (<i>q</i>/2) Re <i>e<sup>-i&phi;</sup></i>&epsi;<sup>-</sup>(<i>k</i>) ,
 * <br/>
 * &nbsp; &nbsp; &Delta;&phi;<sup>-</sup>(<i>k</i>,&phi;) = (<i>K<sub>i</sub></i>/2) Im <i>d</i>/<i>dk</i> <i>e<sup>-i&phi;</sup></i>&epsi;<sup>-</sup>(<i>k</i>) ,
 * <br/>
 * <br/>
 * where <i>q</i> is the unit charge and <i>K<sub>i</sub></i> is the quantity
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>K<sub>i</sub></i> &trie; (<i>qk</i><sub>0</sub>/<i>mc</i><sup>2</sup>)(1/&beta;<sub><i>i</i></sub><sup>3</sup>&gamma;<sub><i>i</i></sub><sup>3</sup>) .
 * <br/>
 * <br/>
 * The subscript <i>i</i> indicates initial, pre-gap values.  The post-gap quantities have 
 * analogous expressions
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Delta;<i>W</i><sup>+</sup>(<i>k</i>,&phi;) = (<i>q</i>/2) Re <i>e<sup>-i&phi;</sup></i>&epsi;<sup>-</sup>(<i>k</i>) ,
 * <br/>
 * &nbsp; &nbsp; &Delta;&phi;<sup>+</sup>(<i>k</i>,&phi;) = (<i>K<sub>f</sub></i>/2) Im <i>d</i>/<i>dk</i> <i>e<sup>-i&phi;</sup></i>&epsi;<sup>-</sup>(<i>k</i>) ,
 * <br/>
 * <br/>
 * The subscript <i>f</i> indicates final, post-gap quantites.  Methods to compute these 
 * values are provided.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Sep 23, 2015
 * @version Sep 23, 2015
 */
public class AxialFieldSpectrum {

    
    /*
     * Local Attributes
     */
    
    /** polynomial fit for the cosine transit time factor T versus beta */
    private IRealFunction fncT;

    /** polynomial fit of the cosine transit time factor derivative T' versus beta */ 
    private IRealFunction fncDT;

    /** polynomial fit of the sine transit time factor S versus beta */
    private IRealFunction fncS;

    /** polynomial fit of the sine transit time factor derivative S' versus beta */
    private IRealFunction fncDS;


    /** polynomial fit for the conjugate cosine transit time factor Tq versus beta */
    private IRealFunction fncTq;

    /** polynomial fit of the conjugate cosine transit time factor derivative Tq' versus beta */ 
    private IRealFunction fncDTq;

    /** polynomial fit of the conjugate sine transit time factor Sq versus beta */
    private IRealFunction fncSq;

    /** polynomial fit of the conjugate sine transit time factor derivative Sq' versus beta */
    private IRealFunction fncDSq;

    
    /*
     * Initialization
     */
    
    /**
     * Constructor for FieldSpectrum.
     *
     *
     * @since  Sep 23, 2015   by Christopher K. Allen
     */
    public AxialFieldSpectrum(IRealFunction plyT) {
        // TODO Auto-generated constructor stub
    }

}
