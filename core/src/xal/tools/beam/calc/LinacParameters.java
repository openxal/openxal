/**
 * LinacParameters.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 22, 2013
 */
package xal.tools.beam.calc;

import xal.model.probe.TwissProbe;
import xal.model.probe.traj.EnvelopeTrajectory;
import xal.model.probe.traj.IPhaseState;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.traj.TransferMapTrajectory;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.beam.Twiss3D.SpaceIndex3D;
import xal.tools.math.r3.R3;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Oct 22, 2013
 */
public class LinacParameters extends CalcEngine implements IMachineParameters<EnvelopeProbeState>{


    /*
     * Local Attributes
     */
    
    /** The trajectory around one turn of the ring */
    private final EnvelopeTrajectory        trjLinac;
    
    /** The final transfer map probe state (at the end of the ring) */
    private final EnvelopeProbeState        staFinal;

    /** The response matrix for the linac (between initial state and final state) */
    private final PhaseMatrix               matResp;

    
    /** The betatron phase advances at the at the exit of the linac */
    private final R3                        vecPhsAdv;
    
    /** The fixed orbit position at the ring entrance */
    private final PhaseVector               vecFxdPt;
    
    /** The matched beam Twiss parameters at the start of the ring */
    private final Twiss[]                   arrTwsMch;

    
    /*
     * Initialization
     */
    
    /**
     * Constructor for LinacParameters.
     *
     *
     * @author Christopher K. Allen
     * @since  Oct 22, 2013
     */
    public LinacParameters(EnvelopeTrajectory trjLinac) {
        ProbeState  pstFinal = trjLinac.finalState();
        
        // Check for correct probe types
        if ( !( pstFinal instanceof EnvelopeProbeState) )
            throw new IllegalArgumentException(
                    "Trajectory states are not EnvelopeProbeStates? - " 
                    + pstFinal.getClass().getName()
                    );
        
        this.trjLinac  = trjLinac;
        this.staFinal  = (EnvelopeProbeState)pstFinal;
        this.matResp   = this.staFinal.getResponseMatrix();
        
        this.vecPhsAdv = super.calculatePhaseAdvPerCell(this.matResp);
        this.vecFxdPt  = super.calculateFixedPoint(this.matPhiRng);
        this.arrTwsMch = super.calculateMatchedTwiss(this.matPhiRng); 
    }
    
    public R3 computePhaseAdvance(PhaseMatrix matPhi, Twiss twsInt,  Twiss twsFnl) {
        
    }
    
    /**
     * <p>
     * Advance the twiss parameters using the given transfer matrix based upon
     * formula 2.54 from S.Y. Lee's book.
     * </p>  
     * <p>
     * <h4>CKA NOTES:</h4>
     * - This method will only work correctly for a beam that is
     * uncorrelated in the phase planes.
     * </p>
     * 
     * @param probe     probe with target twiss parameters
     * @param matPhi    the transfer matrix of the element
     * @param dW        the energy gain of this element (eV)
     *
     * @return  set of new twiss parameter values
     */
    private Twiss3D computeTwiss(TwissProbe probe, PhaseMatrix matPhi, double dW) {
        
        
        // Compute relativistic parameters ratios
        double ratTran;     // emittance decrease ratio for transverse plane 
        double ratLong;     // emittance decrease ratio for longitudinal plane 
        
        if (dW == 0.0)  {
            ratTran = 1.0;
            ratLong = 1.0;
            
        } else {
            double  ER = probe.getSpeciesRestEnergy();
            double  W0 = probe.getKineticEnergy();
            double  W1 = W0 + dW;
            
            double g0 = probe.getGamma();
            double b0 = probe.getBeta();
            double g1 = RelativisticParameterConverter.computeGammaFromEnergies(W1, ER);
            double b1 = RelativisticParameterConverter.computeBetaFromGamma(g1);

            ratTran = (g0*b0)/(b1*g1);
            ratLong = ratTran*(g0*g0)/(g1*g1 );
            
        }
        
        
        // Twiss parameters
        Twiss3D twissEnv0 = probe.getTwiss();   // old values of Twiss parameters
        Twiss3D twissEnv1 = new Twiss3D();        // propagated values of twiss parameters
        
        double  alpha0, beta0, gamma0;  // old twiss parameters
        double  emit0;                  // old (unnormalized) emittance
        double  alpha1, beta1;          // new twiss parameters
        double  emit1;                  // new (unnormalized) emittance

        // Transfer matrix diagonal sub-block
        double Rjj;     // .
        double Rjjp;    //  | Rjj  Rjjp  |
        double Rjpj;    //  | Rjpj Rjpjp |
        double Rjpjp;   //                .

        int j = 0;
        for (SpaceIndex3D index : SpaceIndex3D.values()) { // for each phase plane
            j = 2 * index.val();
            
            // assume constant normalized emittance
            alpha0 = twissEnv0.getTwiss(index).getAlpha();
            beta0  = twissEnv0.getTwiss(index).getBeta();
            gamma0 = twissEnv0.getTwiss(index).getGamma();
            emit0  = twissEnv0.getTwiss(index).getEmittance();
            
            Rjj   = matPhi.getElem(j,  j);
            Rjjp  = matPhi.getElem(j,  j+1);
            Rjpj  = matPhi.getElem(j+1,j);
            Rjpjp = matPhi.getElem(j+1,j+1);
            
            beta1  = Rjj*Rjj*beta0 - 2.*Rjj*Rjjp*alpha0 + Rjjp*Rjjp*gamma0;
            alpha1 = -Rjj*Rjpj*beta0 + (Rjj*Rjpjp + Rjjp*Rjpj)*alpha0 - Rjjp*Rjpjp*gamma0;

            if (index==SpaceIndex3D.Z) // longitudinal plane
                emit1 = emit0 * ratLong; 
            else     // transver plane
                emit1 = emit0 * ratTran;
            
            twissEnv1.setTwiss(index, new Twiss(alpha1, beta1, emit1) );
        }
        
        return twissEnv1;
    }
    
    


}
