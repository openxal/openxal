/**
 * package-info.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 24, 2014
 */

/**
 * <p>
 * The twissobserver package contains tools for computing the Courant-Snyder parameters
 * <b>&sigma;</b> at a hardware location along the beamline from the RMS beam sizes downstream of that
 * that location.  Rather than running a model of the beamline and adjusting the initial
 * conditions of the beam (i.e., the Courant-Snyder parameters) until a chi-squared fit
 * of the model results to the data is achieved, the tools in this package create an 
 * automorphic map <b>F</b> from the Courant-Snyder parameter domain to itself.  This is direct
 * approach to the problem rather than the "weak" solution found by the fitting technique where the 
 * Courant-Snyder parameters minimizing a functional.  
 * Here the map is iterated to the fixed point of <b>F</b>, the fixed point 
 * <b>&sigma;</b> = <b>F&sigma;</b> being the solution of Courant-Snyder parameters.
 * </p>
 * <p>
 * Let <b>T</b> : <i>CS</i> &rarr; <i>D</i> be the map that takes Courant-Snyder parameters to
 * the set of RMS beam sizes along the beamline.  The map <b>T</b> is computed numerically by
 * as a cascade of transfer maps along the beamline from the reconstruction location to each of 
 * the data locations.  We assume there are at least as many data
 * points as there are Courant-Snyder parameters so that |<i>CS</i>| &le; |<i>D</i>|.  Then the 
 * least-squares solution <b>&sigma;</b> to the reconstruction problem is
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <b>&sigma;</b> = (<b>T</b><sup>*</sup><b>T</b>)<sup>-1</sup><b>T</b><sup>*</sup><b>d</b> ,
 * <br/>
 * <br/>
 * where <b>T</b><sup>*</sup> is the adjoint of <b>T</b> and <b>d</b> is the vector of RMS beam
 * sizes (i.e., the data).  When no space charge is present <b>T</b> is a linear map and this
 * equation may be solved directly.  
 * </p>
 * <p>
 * When no space charge is present, indicated by <i>I</i> &gt> 0 where <i>I</i> is the generalized
 * beam current, the map <t> now depends upon the initial Courant-Snyder parameters, that is
 * <b>T</b> = <b>T</b>(<b>&sigma;</b>).  For any given beam current <i>I</i> we define the map 
 * <b>F</b> : <i>CS</i> &rarr; <i>CS</i> as 
 * <br/>
 * <br>
 * &nbsp; &nbsp; <b>F</b>(<b>&sigma;</b>,<i>I</i>) &#8796; [<b>T</b><sup>*</sup><b>(<b>&sigma;</b>,<i>I</i>)T</b>(<b>&sigma;</b>,<i>I</i>)]<sup>-1</sup><b>T</b><sup>*</sup>(<b>&sigma;</b>,<i>I</i>)<b>d</b>
 * <br/>
 * <br/>
 * The fixed point <b>&sigma;</b>(<i>I</i>) of <b>F</b> is the set of Courant-Snyder parameters for
 * the beam current <i>I</i>.  
 * </p>
 * <p>
 * One particularly simply way of solving this problem is to pick an initial
 * set of Courant-Snyder parameters <b>&sigma;</b><sub>0</sub>(<i>I</i>) and start iterating the above equation.
 * If <b>&sigma;</b><sub>0</sub>(<i>I</i>) is within the region of contraction for the fixed point of 
 * <b>F</b> then the iteration converges to the solution.  We can improve the convergence properties by
 * forming an outside loop where <i>I</i> is increased from 0 to its target values.  That is, <b>F</b> is
 * iterated to its fixed point for each increment of beam current, and that fixed point is used to initialize
 * the next iteration.
 * <p>
 * </p>
 * Another more sophisticated technique is a continuation method exploiting the smoothness of 
 * <b>F</b>(&middot;, &middot;).  Starting with <b>F</b>[<b>&sigma;</b>(<i>I</i>), <i>I</i>] = <b>&sigma;</b>(<i>I</i>)
 * we take the total derivative with respect to <i>I</i> yielding
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &part;<sub>I</sub><b>&sigma;</b>(<i>I</i>) = [<b>Id</b> - &part;<sub><b>&sigma;</b></sub><b>F</b>(<b>&sigma;</b>,<i>I</i>)]<sup>-1</sup>&part;<b>F</b>(<b>&sigma;</b>,<i>I</i>) ,
 * <br/>
 * <br/>
 * where <b>Id</b> is the identity map. Starting from <b>&sigma;</b><sub>0</sub> the solution for <i>I</i> = 0, which can be
 * computed exactly, we move along the curves
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <b>&sigma;</b><sub><i>n</i>+1</sub> &#8796; <b>&sigma;</b>(<i>I<sub>n</sub></i> + &Delta;<i>I</i>) = &part;<sub><i>I</i></sub><b>&sigma;</b><i><sub>n</sub></i>&Delta;<i>I</i> + <i>O</i>(&Delta;<i>I</i><sup>2</sup>) .
 * <br/>
 * <br/>
 * The derivatives are recomputed at each step and a brief fixed point iteration is executed to move 
 * <b>&sigma;</b><sub><i>n</i></sub> back onto the solution curve <b>&sigma;</b>(&middot;) since the 
 * above linear extrapolation cannot account for curvature.
 * </p> 
 * <p>
 * </p>
 * There are three classes which perform the Courant-Snyder parameter reconstructions from
 * RMS beam size data using the techniques described above.  The other classes in the package
 * are support classes for those classes.
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <code><b>CsZeroCurrentEstimator</b></code> - This class is used for
 * estimating the Courant-Snyder parameters whenever space charge effects are negligible.
 * As described above, in this case it is a direct calculation and very fast.  The class
 * should always be used for such a case.
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <code><b>CsFixedPointEstimator</b></code> - This class estimates Courant-Snyder
 * parameters using the fixed point iteration method, with space charge.  At current this
 * class performs very well; its convergence properties are good and it is fast.
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <code><b>CsContinuationEstimator
 * <br/>
 * <br>
 * If one has the RMS bunch lengths for the longitudinal direction then they can be
 * used in the transverse Courant-Snyder calculations.  However, as is usually the case,
 * if these values are unknown then the class <code>BunchLengthSimulator</code> can be
 * used to estimate them via simulation.
 * </p>
 * <p>
 * The measurement data is packaged in the class <code>Measurement</code>.  Thus,
 * the RMS sizes, the beamline locations from which they come, and the desired
 * reconstruction location will fill out the attributes of this class.  Class
 * instances are then passed to the above reconstruction engines to compute the
 * Courant-Snyder values.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Sep 24, 2014
 */
package xal.extension.twissobserver;