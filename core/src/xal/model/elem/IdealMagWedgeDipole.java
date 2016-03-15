/*
 * IdealMagWedgeDipole
 * 
 * Created on May 20, 2004
 *
 */
package xal.model.elem;

import xal.model.IProbe;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.Bend;

/**
 * <p>
 * Represents a bending dipole magnet with arbitrary pole face angles.
 * This is a composite element constructed from three sub-elements - one
 * <code>IdealMagSectorDipole</code> sandwiched between two 
 * <code>IdealDipoleFace</code> elements that provided the thin lens
 * dynamics of the tilted pole faces.
 * </p>
 * <h3>NOTE:</h3>
 * <p>
 * A rectangle dipole can be specified by setting equal exit and entrance
 * pole face angles.
 * </p>
 * @author Christopher K. Allen
 * 
 * @see xal.model.elem#IdealMagSectorDipole
 * @see xal.model.elem#IdealMagDipoleFace
 *
 * @deprecated  This class has been replaced by <code>IdealMagWedgeDipole2</code>
 */
@Deprecated
public class IdealMagWedgeDipole extends ElectromagnetSeq {



    /*
     *  Global Attributes
     */

    /** string type identifier for all IdealMagSectorDipole objects */
    public static final String  s_strType = "IdealMagWedgeDipole";
    
    /** storage to reserve for child components */
    public static final int     s_szReserve = 3; 


    /*
     * Local Attributes
     */
     
    /** magnet body */
    private IdealMagSectorDipole    m_magBody = new IdealMagSectorDipole();
    
    /** magnet entrance pole face */
    private IdealMagDipoleFace      m_polEntr = new IdealMagDipoleFace();
    
    /** magnet entrance pole face */
    private IdealMagDipoleFace      m_polExit = new IdealMagDipoleFace();
    



    /*
     * Initialization
     */

    /**
     * Default constructor - creates a new unitialized instance of 
     * <code>IdealMagWedgeDipole</code>.
     */
    public IdealMagWedgeDipole() {
        this(null);
    }

    /**
     * Create new <code>IdealMagWedgeDipole</code> object and specify its
     * instance identifier string.
     * 
     * @param strId     instance identifier string
     */
    public IdealMagWedgeDipole(String strId) {
        super(s_strType, strId, s_szReserve);
        
        this.addChild(this.m_polEntr);
        this.addChild(this.m_magBody);
        this.addChild(this.m_polExit);
    }


    /**
     * Set the path length of the bending dipole along the design trajectory.
     * Note that off-axis particles will experience a different path length
     * which is accounted for in the dynamics.
     *  
     * @param dblLen    design path length through bend in <b>meters</b>
     */
    public void setLength(double dblLen)    {
        this.getMagBody().setLength( dblLen );
    }
    
    /**
     * Set the magnetic field index of the magnet evaluated at the design
     * orbit.   The field index is defined as
     * 
     *      n := -(R0/B0)(dB/dR)
     * 
     * where R0 is the radius of the design orbit, B0 is the field at the
     * design orbit (@see IdealMagSectorDipole#getField), and dB/dR is the
     * derivative of the field with respect to the path deflection - evaluated
     * at the design radius R0.
     * 
     * @param dblFldInd     field index of the magnet (unitless)     
     */
    public void setFieldIndex(double dblFldInd) {
        this.getMagBody().setFieldIndex(dblFldInd);
    }
    
    /**
     * Set the gap size between the dipole magnet poles.
     * 
     * @param dblGap    gap size in <b>meters</b>
     */
    public void setGapSize(double dblGap)  {
        this.getEntrFace().setGapHeight(dblGap);
        this.getMagBody().setGapHeight(dblGap);
        this.getExitFace().setGapHeight(dblGap);
    }
    
    /**
     * Set the entrance pole face angle with respect to the design trajectory
     * 
     * @param   dblAngPole  pole face angle in <b>radians</b>
     */
    public void setEntrPoleAngle(double dblAngPole) {
        this.getEntrFace().setPoleFaceAngle(dblAngPole);
    }
    
    /**
     * Set the exit pole face angle with respect to the design trajectory
     * 
     * @param   dblAngPole  pole face angle in <b>radians</b>
     */
    public void setExitPoleAngle(double dblAngPole) {
        this.getExitFace().setPoleFaceAngle(dblAngPole);
    }
    
    /**
     * Set the entrance fringe integral (a la D.C. Carey) which accounts  
     * for the first-order effects of the fringing field outside the dipole
     * magnet.
     * 
     * @param   dblFldInt   fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagDipoleFace#setFringeIntegral
     */
    public void setEntrFringeIntegral(double dblFldInt) {
        this.getEntrFace().setFringeIntegral(dblFldInt);
    }

    /**
     * Set the exit fringe integral (a la D.C. Carey) which accounts  
     * for the first-order effects of the fringing field outside the dipole
     * magnet.
     * 
     * @param   dblFldInt   fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagDipoleFace#setFringeIntegral
     */
    public void setExitFringeIntegral(double dblFldInt) {
        this.getExitFace().setFringeIntegral(dblFldInt);
    }
    
    


    /*
     * Attribute Query
     */    
     
     
    /**
     * Return the entrance dipole face object of the wedge dipole.
     * 
     * @return     entrance pole face
     */
    public  IdealMagDipoleFace  getEntrFace()   {
        return this.m_polEntr;
    }
     
    /**
     * Return the exit dipole face object of this wedge dipole magnet.
     * 
     * @return     exit pole face
     */
    public  IdealMagDipoleFace   getExitFace()   {
        return this.m_polExit;
    }
     
    /**
     * Return the dipole magnet body object of this wedge dipole magnet.  Note
     * that the body is of type <code>IdealMagSectorDipole</code> which has
     * no end effects.
     * 
     * @return     magnet body
     */
    public  IdealMagSectorDipole    getMagBody()    {
        return this.m_magBody;
    }
    
    /**
     * Return the magnetic field index of the magnet evaluated at the design
     * orbit.   The field index is defined as
     * 
     *      n := -(R0/B0)(dB/dR)
     * 
     * where R0 is the radius of the design orbit, B0 is the field at the
     * design orbit (@see IdealMagSectorDipole#getField), and dB/dR is the
     * derivative of the field with respect to the path deflection - evaluated
     * at the design radius R0.
     * 
     * @return  field index of the magnet at the design orbit (unitless)     
     */
    public double getFieldIndex()   {
        return this.getMagBody().getFieldIndex();
    }

    /**
     * Return the gap size between the dipole magnet poles.
     * 
     * @return  gap size in <b>meters</b>
     */
    public double getGapHeight()  {
        return this.getMagBody().getGapHeight();
    }

    /**
     * Compute the path curvature within the dipole for the given probe. 
     * The path curvature is 1/R where R is the bending radius of the dipole
     * (radius of curvature).  Note that for zero fields the radius of
     * curvature is infinite while the path curvature is zero.
     * 
     * @param   probe   probe object to be deflected
     * 
     * @return  dipole path curvature for given probe (in <b>1/meters</b>)
     */
    public double   compCurvature(IProbe probe) {
        return this.getMagBody().compCurvature(probe);
    }



    /*
     *  IElectromagnet Interface
     */

    /**
     *  Return the orientation enumeration code specifying the bending plane.
     *
     *  @return     ORIENT_HOR  - dipole has steering action in x (horizontal) plane
     *              ORIENT_VER  - dipole has steering action in y (vertical) plane
     *              ORIENT_NONE - error
     */
    public int getOrientation() {
        return this.getMagBody().getOrientation();
    };

    /**  
     *  Get the magnetic field strength of the dipole electromagnet
     *
     *  @return     magnetic field (in <b>Tesla</b>).
     */
    public double getMagField() {
        return this.getMagBody().getMagField();
    };



    /**
     *  Set the dipole magnet bending orientation
     *  
     *  @param  enmOrient   magnet orientation enumeration code
     *
     *  @see    #getOrientation
     */
    public void setOrientation(int enmOrient) {
        this.getEntrFace().setOrientation(enmOrient);
        this.getMagBody().setOrientation(enmOrient);
        this.getExitFace().setOrientation(enmOrient);
    };

    /**  
     *  Set the magnetic field strength of the dipole electromagnet.
     *
     *  @param  dblField    magnetic field (in <b>Tesla</b>).
     */
    public void setMagField(double dblField) {
        this.getEntrFace().setMagField(dblField);
        this.getMagBody().setMagField(dblField);
        this.getExitFace().setMagField(dblField);
    };


	/**
	 * Conversion method to be provided by the user
	 * 
	 * @param element the SMF node to convert
	 */
	@Override
	public void initializeFrom(LatticeElement element) {
		super.initializeFrom(element);
		
		Bend magnet = (Bend) element.getHardwareNode();		

		// xal.model.elem.ThickDipole xalDipole =
		// new xal.model.elem.ThickDipole();
		// xalDipole.setId(element.getNode().getId());
		// xalDipole.setLength(element.getLength());
		// xalDipole.setMagField(magnet.getDesignField());
		// xalDipole.setKQuad(magnet.getQuadComponent());
		// double angle = magnet.getDfltBendAngle()*Math.PI/180. * element.getLength() / magnet.getDfltPathLength();
		// xalDipole.setReferenceBendAngle(angle);

		// Replace ThickDipole object with an IdealMagWedgeDipole2
		// First retrieve all the physical parameters for a bending dipole				
		double len_sect = element.getLength();		
		double len_path0 = magnet.getDfltPathLength();
		double ang_bend0 = magnet.getDfltBendAngle() * Math.PI / 180.0;
		double k_quad0 = magnet.getQuadComponent();

		// Now compute the dependent parameters
		double R_bend0 = len_path0 / ang_bend0;
		double fld_ind0 = -k_quad0 * R_bend0 * R_bend0;

		double ang_bend = ang_bend0 * (len_sect / len_path0);
		double len_path = R_bend0 * ang_bend;

		// Set the parameters for the new model element				
		/*setPhysicalLength(len_sect);
		setDesignPathLength(len_path);*/		
		setFieldIndex(fld_ind0);
		/*setDesignBendAngle(ang_bend);*/
						
		if (element.isFirstSlice()) // first piece
			setEntrPoleAngle(magnet.getEntrRotAngle() * Math.PI / 180.);
		if (element.isLastSlice()) // last piece					
			setExitPoleAngle(magnet.getExitRotAngle() * Math.PI / 180.);
	}

    /*
     * Internal Support
     */
     
}
