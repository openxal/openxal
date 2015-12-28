/*
 * IdealDipole.java
 *
 * Created on October 22, 2002, 1:08 PM
 *
 *  Modified:
 *      02/10/02 CKA - refactor to revised model architecture
 *      01/02/05 SAKO - when angleKick !=0, use it instead of dL*W
 */

package xal.model.elem;


import java.io.PrintWriter;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.PropagationException;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.tools.beam.IConstants;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;



/**
 *  Represents the action of an ideal magnetic dipole.  These structures are typically
 *  used for beam steering.
 *
 * @author  Christopher Allen
 */
public class IdealMagSteeringDipole extends ThinElectromagnet {

    
    
    /*
     *  Global Attributes
     */
    
    /** Parameters for XAL MODEL LATTICE dtd */
    
    /** the string type identifier for all IdealMagSteeringDipole's */
    public static final String      s_strType = "IdealMagSteeringDipole";
    
    
    /** Tag for the parameter within the XML configuration file */
    public static final String      s_strParamLenEff = "EffLength";
    
    /** Tag for the parameter within the XML configuration file */
    public static final String      s_strParamOrient = "Orientation";
    
    /** Tag for the parameter within the XML configuration file */
    public static final String      s_strParamField  = "MagField";
    
    
    
    
    
    /*
     *  Local Attributes
     */

    /** effective length of the dipole magnet */
    private double              m_dblLenEff = 0.0;
    
    /** the instantaneous position displacement going through the magnet */
    private double				m_dblPositionKick = 0.0;

    /** the instantaneous momentum displacement going through the magnet */
    private double				m_dblAngleKick = 0.0;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Default constructor - creates a new uninitialized instance 
     * of IdealMagSteeringDipole.  Typically used by automatic lattice
     * generation.
     */
    public IdealMagSteeringDipole() {
        super(s_strType);
    }
    
    /**
     * Create a new instance of IdealMagSteeringDipole and specify its
     * instance identifier.
     * 
     * @param   strId   string instance identifier of element
     */
    public IdealMagSteeringDipole(String strId) {
        super(s_strType, strId);
    }
    
    /** 
     *  Creates a new instance of <code>IdealMagSteeringDipole</code>.  
     *  The action of the kicker is completely unspecified.  
     *
     *  @param  strId       string identifier of element
     *  @param  dblFld      field strength (in <b>Tesla</b>)
     *  @param  enmOrient   dipole orientation (ORIENT_HOR or ORIENT_VER)
     *  @param  dblLenEff   effective length of dipole magnet
     */
    public IdealMagSteeringDipole(String strId, double dblLenEff, int enmOrient, double dblFld) {
        super(s_strType, strId);
        
        this.setOrientation(enmOrient);
        this.setEffLength(dblLenEff);
        this.setMagField(dblFld);
    };
    
    /**
     *  Set the effective length of the dipole magnet.  This value, along with the
     *  field strength, determines the action of the dipole.
     *
     *  @param  dblLenEff       effective length (in <b>meters</b>)
     */
    public void setEffLength(double dblLenEff)  {
        m_dblLenEff = dblLenEff;
    }
    
    
    /*
     *  Property Query
     */
    
    /**
     *  Return the effective length of this dipole magnet
     *
     *  @return     effective length (<b>in meters</b>)
     */
    public double   getEffLength()  { return m_dblLenEff; };
    
    
    
    /**
     *  Set the position kick of the dipole magnet.  This value, along with the
     *  field strength, determines the action of the dipole.
     *
     *  @param  dblPosKick       change in position going through magnet (in <b>meters</b>)
     */
    public void setPositionKick(double dblPosKick)  {
        m_dblPositionKick = dblPosKick;
    }
    
    /**
     *  Set the kick angle of the dipole magnet.  If this value, or position kick
     *  is non-zero. This determines the dipole bend angle.
     *
     *  @param  dblAngKick       effective length (in <b>meters</b>)
     */
    public void setAngleKick(double dblAngKick)  {
        m_dblAngleKick = dblAngKick;
    }
    
    
    /*
     *  Property Query
     */
    
    /**
     *  Return the position kick strength of this dipole magnet
     *
     *  @return     kick displacement (<b>in meters</b>)
     */
    public double   getPositionKick()  { return m_dblPositionKick; };
    
    
    /**
     *  Return the angle kick [rad]
     *
     *  @return     angle kick (<b>in rad</b>)
     */
    public double   getAngleKick()  { return m_dblAngleKick; };    
    
    /*
     *  IElement Interface
     */
    
    /**
     * Returns the time taken for the probe to propagate through element.
     * 
     *  @param  probe   propagating probe
     *  
     *  @return         the value zero 
     */
    @Override
    public double elapsedTime(IProbe probe)  {
        return 0.0;
    }
    
    /**
     *  Return the energy gain for this Element.
     *
     *  @param  probe   dummy argument
     *
     *  @return         value of zero
     */
    @Override
    public double   energyGain(IProbe probe)     { 
        return 0.0; 
    };
    
    /**
     *  Computes the transfer map for an ideal magnetic dipole.
     *
     *  @param  probe   probe interface from which we get rest energy and kinetic energy
     *
     *  @return         7&times;7 transfer matrix in homogeneous coordinates
     *
     *  @exception  ModelException    bad orientation code
     */
    @Override
    protected PhaseMap transferMap(IProbe probe)  throws ModelException {

        // Get constants
//        double e  = UnitCharge;
        double c  = IConstants.LightSpeed;
        
        // Get element parameters
        double B  = this.getMagField();
        double dL = this.getEffLength();
        
        // Get probe parameters
        double q  = probe.getSpeciesCharge();
        double Er = probe.getSpeciesRestEnergy();

        double beta  = probe.getBeta();
        double gamma = probe.getGamma();
//        double gamma_2 = gamma*gamma;
        
        
        // Compute the cyclotron frequency and dipole strength
//        double w = (q/e)*(c/Er)*(B/(beta*gamma));
        double w = (q)*(c/Er)*(B/(beta*gamma));
        double dp = w*dL; // dp polarity = q*B polarity
//      B polarity for negative charged particle -> x: left +, y: upper +
// angle polarity was opposite up to 27 Nov 07
        //changed so that angle + is x+, angle - is y- for negatives
        // on 28 Nov 07
        if (m_dblAngleKick != 0.) {
        	System.out.println("***use anglekick ("+m_dblAngleKick+") instead of dp "+dp);
        	dp  = m_dblAngleKick; 
        }                           // then B polarity is defined in J-PARC also dp>0 for B>0 in x and y
        
        // Build transfer matrix
        PhaseMatrix  matPhi  = PhaseMatrix.identity();         // homogeneous coordinates
        
        switch (this.getOrientation())  {
            
       /* case ORIENT_HOR:
            matPhi.setElem(1,6, -dp);
            break;
            
        case ORIENT_VER:
        	  matPhi.setElem(3,6, dp);
            break;
          */
        
        case ORIENT_HOR:
            matPhi.setElem(1,6, -dp);
            break;
            
        case ORIENT_VER:
        	  matPhi.setElem(3,6, -dp); //this polarity with dp was wrong with orbitcorrect application. -dp is correct
            break;
                
            default:
                throw new PropagationException("IdealMagSteeringDipole::tranferMatrix() - unknown magnet orientation");
        }
        
        return new PhaseMap(matPhi);
    }
    
    
    
    /*
     *  Testing and Debugging
     */
    
    
    /**
     *  Dump current state and content to output stream.
     *
     *  @param  os      output stream object
     */
    @Override
    public void print(PrintWriter os)    {
        super.print(os);
        
        os.println("  effective length   : " + this.getEffLength() );
        os.println("  magnetic field     : " + this.getMagField() );
        os.println("  magnet orientation : " + this.getOrientation() );
    }
    
    
	/**
	 * Conversion method to be provided by the user
	 * 
	 * @param element the SMF node to convert
	 */
	@Override
	public void initializeFrom(LatticeElement element) {
		super.initializeFrom(element);
		Magnet magnet = (Magnet) element.getHardwareNode();
		setEffLength(magnet.getEffLength());		
	}
}
