//
//  ISimEnvelopeResults.java
//  xal
//
//  Created by Thomas Pelaia on 2/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.beam.calc;

import xal.model.probe.traj.ProbeState;
import xal.tools.math.r3.R3;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;


/**
 * <p>
 * Interface for exposing machine parameters involving the state data produced by
 * numerical simulation.  The context of these
 * quantities should depend upon the type <code>S</code> of the probe
 * states produced by the simulation.
 * </p>
 * <p>
 * In particular, for <code>S</code> = <code>xal.model.probe.traj.TransferMapState</code> the
 * quantities of this interface are regarded as machine parameters, since the beam
 * does not influence transfer map calculations in the 
 * <code>xal.model.probe.TransferMapProbe</code>.  However, when 
 * <code>S</code> = <code>xal.model.probe.traj.EnvelopeProbeState</code> then the interface
 * quantities are essentially beam parameters since the beam dynamics figure into the
 * states of <code>xal.model.probe.EnvelopeProbe</code>.
 * </p> 
 *
 * @param <S>   probe state type
 * @param <T>   simulation trajectory type (container of probe states)
 *
 * @author Christopher K. Allen
 * @author Thomas Pelaia
 * @since   2/9/05
 * @version Nov 7, 2013
 */
public interface ISimEnvelopeResults <S extends ProbeState>   {
	

    /*
     * Interface Methods
     */

    /** 
     * Returns the array of twiss objects for this state for all three planes at the
     * location of the given simulation state. These could be the matched beam
     * Twiss parameters for a periodic structure or the dynamics Twiss parameters
     * of a mismatched or otherwise evolving beam.
     * 
     * @param   state   simulation state where Twiss parameters are computed
     * 
     * @return array (twiss-H, twiss-V, twiss-L)
     */
    public Twiss[] computeTwissParameters(S state);		
	
	
    /**
     * Get the betatron phase values at the given state location for 
     * all three phase planes.
     * 
     *  @param   state   simulation state where parameters are computed
     * 
     * @return  vector (&psi;<sub><i>x</i></sub>, &psi;<sub><i>y</i></sub>, &psi;<sub><i>x</i></sub>) of phases in radians
     */
    public R3 computeBetatronPhase(S state);

    /**
     * <p>
     * Calculates the fixed point (closed orbit) in transverse phase space
     * at the given state <i>S<sub>n</sub></i> location <i>s<sub>n</sub></i> in the presence of dispersion.  
     * </p>
     * <p>
     * Let the full-turn map a the state location be denoted <b>&Phi;</b><sub><i>n</i></sub> (or the transfer
     * matrix from entrance to location <i>s<sub>n</sub></i> for a linac).
     * The transverse plane dispersion vector <b>&Delta;</b> is defined  
     * <br/>
     * <br/> 
     * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; -(1/&gamma;<sup>2</sup>)[d<i>x</i>/d<i>z'</i>, d<i>x'</i>/d<i>z'</i>, d<i>y</i>/d<i>z'</i>, d<i>y'</i>/d<i>z'</i>]<sup><i>T</i></sup> .
     * <br/>
     * <br/>  
     * It can be identified as the first 4 entries of the 6<sup><i>th</i></sup> 
     * column in the transfer matrix <b>&Phi;</b></b><sub><i>n</i></sub>. The above vector
     * quantifies the change in the transverse particle phase 
     * coordinate position versus the change in particle momentum.  
     * The factor -(1/&gamma;<sup>2</sup>) is needed to convert from longitudinal divergence
     * angle <i>z'</i> used by XAL to momentum &delta;<i>p</i> &equiv; &Delta;<i>p</i>/<i>p</i> used in 
     * the dispersion definition.  Specifically,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &delta;<i>p</i> &equiv; &Delta;<i>p</i>/<i>p</i> = &gamma;<sup>2</sup><i>z</i>'
     * <br/>
     * <br/>
     * As such, the above vector can be better described
     * <br/>
     * <br/> 
     * &nbsp; &nbsp; <b>&Delta;</b><sub><i>t</i></sub> &equiv; [&Delta;<i>x</i>/&delta;<i>p</i>, &Delta;<i>x'</i>/&delta;<i>p</i>, &Delta;<i>y</i>/&delta;<i>p</i>, &Delta;<i>y'</i>/&delta;<i>p</i>]<sup><i>T</i></sup>
     * <br/>
     * <br/>
     * explicitly describing the change in transverse phase coordinate for fractional
     * change in momentum &delta;<i>p</i>.  
     * </p>
     * <p>
     * Since we are only concerned with transverse phase space coordinates, we restrict ourselves to the 
     * 4&times;4 upper diagonal block of <b>&Phi;</b></b><sub><i>n</i></sub>, which we denote take <b>T</b></b><sub><i>n</i></sub>.  
     * That is, <b>T</b></b><sub><i>n</i></sub> = &pi; &sdot; <b>&Phi;</b></b><sub><i>n</i></sub>
     * where &pi; : <b>R</b><sup>6&times;6</sup> &rarr; <b>R</b><sup>4&times;4</sup> is the
     * projection operator. 
     * </p>
     * <p>
     * This method finds that point <b>z</b><sub><i>t</i></sub> &equiv; 
     * (<i>x<sub>t</sub></i>, <i>x'<sub>t</sub></i>, <i>y<sub>t</sub></i>, <i>y'<sub>t</sub></i>)
     * in transvse phase space that is invariant under the action of the ring for a given momentum spread
     * &delta;<i>p</i>.  That is, the particle ends up
     * in the same location each revolution. With a finite momentum spread of &delta;<i>p</i> &gt; 0
     * we require this require that
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <b>T</b><sub><i>n</i><b></sub>z</b><sub><i>t</i></sub> + &delta;<i>p</i><b>&Delta;</b><sub><i>t</i></sub> = <b>z</b><sub><i>t</i></sub> ,
     * <br/>
     * <br/>
     * which can be written
     * <br/>
     * <br/>
     * &nbsp; <b>z</b><sub><i>t</i></sub> = &delta;<i>p</i>(<b>T</b></b><sub><i>n</i></sub> - <b>I</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
     * <br/>
     * <br/>
     * where <b>I</b> is the identity matrix.  Dividing both sides by &delta;<i>p</i> yields the final
     * result
     * <br/>
     * <br/>
     * &nbsp; <b>z</b><sub>0</sub> &equiv; <b>z</b><sub><i>t</i></sub>/&delta;<i>p</i> = (<b>T</b></b><sub><i>n</i></sub> - <b>I</b>)<sup>-1</sup><b>&Delta;</b><sub><i>t</i></sub> ,
     * <br/>
     * <br/>
     * which is the returned value of this method.  It is normalized by
     * &delta;<i>p</i> so that we can compute the closed orbit for any given momentum spread.
     * </p>
     *   
     * @param state    we are calculating the dispersion at this state location
     * 
     * @return         The closed orbit fixed point <b>z</b><sub>0</sub> for finite 
     *                 dispersion, normalized by momentum spread.
     *                 Returned as an array [<i>x</i><sub>0</sub>,<i>x'</i><sub>0</sub>,<i>y</i><sub>0</sub>,<i>y'</i><sub>0</sub>]/&delta;<i>p</i>
     *
     * @author Christopher K. Allen
     * @since  Nov 8, 2013
     */
    public PhaseVector computeChromDispersion(S state);
    
}
