/*
 * IdealMagWedgeDipole
 * 
 * Created on May 20, 2004
 *
 */
package xal.model.elem;

import xal.sim.scenario.LatticeElement;
import xal.smf.impl.Bend;
import xal.tools.math.r3.R3;

/**
 * Represents a quadrupole magnet with thin face elements which represent fringe field.
 * This is a composite element constructed from three subelements - one
 * <code>IdealMagQuad</code> sandwiched between two 
 * <code>IdealFringeQuadFace</code> elements that provides fringe field based on;
 * H. Matsuda and H. Wollnik, NIM 103 (1972) 117
 * Structure is simular to that of IdealMagWedgeDipole2
 * 
 * @author Hiroyuki Sako
 * 
 * @see xal.model.elem#IdealMagQuad
 * @see xal.model.elem#IdealMagFringeQuadFace
 *
 */
public class IdealMagFringeQuad extends ElectromagnetSeq {



    /*
     *  Global Attributes
     */

    /** string type identifier for all IdealMagSectorDipole objects */
    public static final String  s_strType = "IdealMagFringeQuad";
    
    /** storage to reserve for child components */
    public static final int     s_szReserve = 3; 


    /*
     * Local Attributes
     */
     
    /** magnet body */
    private IdealMagQuad    magBody = new IdealMagQuad();
    
    /** magnet entrance pole face */
    private IdealMagFringeQuadFace      polEntr = new IdealMagFringeQuadFace();
    
    /** magnet entrance pole face */
    private IdealMagFringeQuadFace      polExit = new IdealMagFringeQuadFace();
    



    /*
     * Initialization
     */

    /**
     * Default constructor - creates a new unitialized instance of 
     * <code>IdealMagWedgeDipole</code>.
     */
    public IdealMagFringeQuad() {
        this(null);
    }

    /**
     * Create new <code>IdealMagWedgeDipole</code> object and specify its
     * instance identifier string.
     * 
     * @param strId     instance identifier string
     */
    public IdealMagFringeQuad(String strId) {
        super(s_strType, strId, s_szReserve);
        
        this.addChild(this.polEntr);
        this.addChild(this.magBody);
        this.addChild(this.polExit);
    }

    
    /**
     * Override the default <code>setId(String)</code> method for
     * <code>ElementSeq</code> objects so we can set the identifier
     * strings of each composite element.
     * 
     * @param   strId       identifier string of this compsite.
     * @see xal.model.elem.ElementSeq#setId(java.lang.String)
     */
    @Override
    public void setId(String strId) {
        super.setId(strId);
        
        this.getMagBody().setId(strId + "Body");
        this.getFaceEntr().setId(strId + "Entr");
        this.getFaceExit().setId(strId + "Exit");
    }

    /**
     * Set the alignment parameters for the magnet.
     * 
     * I don't know what they are or how they are used.
     * 
     * @param   vecAlign    (dx,dy,dz)
     */
    public void setAlignment(R3 vecAlign)   {
        this.getMagBody().setAlign(vecAlign);
        this.getFaceEntr().setAlign(vecAlign);
        this.getFaceExit().setAlign(vecAlign);
    }
    
    /**
     * <p>Set the position of the magnet along the design path within the
     * containing lattice.
     * </p>
     * 
     * NOTE:
     * <p>We have a bit of a logitics problem here because this is a 
     * composite element.  So when setting the position of this element
     * we want to set the positions of all the internal elements, in 
     * particlar, the pole faces.  Thus, we need the physical length
     * of the magnet to do this.  Either we require the length to be
     * provided when invoked this method, or this method must be invoked
     * after setting the physical length.  I opted for the former.
     * </p>
     * 
     * <p>The physical length of this element is not set when invoking
     * this method.  That must be done separately with a call to 
     * <code>setPhysicalLength(double)</code>.
     * </p>
     * 
     * @param   dblPos      lattice position of element center (meters)
     * @param   dblLen      physical length of this element
     * 
     * @see IdealMagFringeQuad#setPhysicalLength(double)
     */
    public void setPosition(double dblPos, double dblLen)   {
        this.getMagBody().setPosition(dblPos);
        this.getFaceEntr().setPosition(dblPos - dblLen/2.0);
        this.getFaceExit().setPosition(dblPos + dblLen/2.0);
    }
    
 
    /**
     * Set the entrance fringe integral (a la H. Matsuda) which accounts  
     * for the first-order effects of the fringing field outside the quadrupole
     * magnet.
     * 
     * @param   dblFldInt   fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagFringeQuadFace#setFringeIntegral1
     */
    public void setEntrFringeIntegral1(double dblFldInt) {
        this.getFaceEntr().setFringeIntegral1(dblFldInt);
    }
    /**
     * Set the entrance fringe integral (a la H. Matsuda) which accounts  
     * for the first-order effects of the fringing field outside the quadrupole
     * magnet.
     * 
     * @param   dblFldInt   fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagFringeQuadFace#setFringeIntegral2
     */
    public void setEntrFringeIntegral2(double dblFldInt) {
        this.getFaceEntr().setFringeIntegral2(dblFldInt);
    }

    /**
     * Set the exit fringe integral (a la H. Matsuda) which accounts  
     * for the first-order effects of the fringing field outside the dipole
     * magnet.
     * 
     * @param   dblFldInt   fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagDipoleFace2#setFringeIntegral1
     */
    public void setExitFringeIntegral1(double dblFldInt) {
        this.getFaceExit().setFringeIntegral1(dblFldInt);
    }
    /**
     * Set the exit fringe integral (a la H. Matsuda) which accounts  
     * for the first-order effects of the fringing field outside the dipole
     * magnet.
     * 
     * @param   dblFldInt   fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagDipoleFace2#setFringeIntegral2
     */
    public void setExitFringeIntegral2(double dblFldInt) {
        this.getFaceExit().setFringeIntegral2(dblFldInt);
    }

    
    
    /**
     * Set the physical length of the bending dipole.  The design path length
     * is generally larger than this value because of the curvature.
     *  
     * @param dblLen    physical length through bend in <b>meters</b>
     */
    public void setPhysicalLength(double dblLen)    {
        this.getMagBody().setLength( dblLen );
    }
    
    /**
     * Set the reference (design) orbit path-length through
     * the magnet.
     * 
     * @param   dblPathLen      path length of design trajectory (meters)
     * 
     */
    public void setK1(double dblPathLen)  {
    	this.getFaceEntr().setK1( dblPathLen );
        this.getMagBody().setK1( dblPathLen );
        this.getFaceExit().setK1( dblPathLen );
    }
    
    /**
     * Set the bending angle of the reference (design) orbit.
     * 
     * @param   dblBendAng      design trajectory bending angle (radians) 
     */
    public void setNominalKineEnergy(double dblBendAng)   {
    	this.getFaceEntr().setNominalKineEnergy( dblBendAng );
        this.getMagBody().setNominalKineEnergy( dblBendAng );
        this.getFaceExit().setNominalKineEnergy( dblBendAng );
    }


    /**
     * sako use design field if fieldPathFlag = 1, and use bfield if 0
     */
    public void setFieldPathFlag(double dblFlag) {
    	this.getFaceEntr().setFieldPathFlag(dblFlag);
    	this.getMagBody().setFieldPathFlag(dblFlag);
    	this.getFaceExit().setFieldPathFlag(dblFlag);
    }

    /**
     * set align x
     * @param dx
     */
    public void setAlignX(double dx) {
    	this.getFaceEntr().setAlignX(dx);
    	this.getMagBody().setAlignX(dx);
    	this.getFaceExit().setAlignX(dx);
    }
    
    /**
     * set align y
     * @param dy
     */
    public void setAlignY(double dy) {
    	this.getFaceEntr().setAlignY(dy);
    	this.getMagBody().setAlignY(dy);
    	this.getFaceExit().setAlignY(dy);
    }
    /**
     * set align z
     * @param dz
     */
    public void setAlignZ(double dz) {
    	this.getFaceEntr().setAlignY(dz);
    	this.getMagBody().setAlignY(dz);
    	this.getFaceExit().setAlignY(dz);
    }
    
    /*
     * Attribute Query
     */    
     
   
    /**
     * Get the entrance fringe integral (a la H. Matsuda) which accounts  
     * for the first-order effects of the fringing field outside the quadrupole
     * magnet.
     * 
     * @return  fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagFringeQuadFace#setFrindIntegral1
     */
    public double   getEntrFringeIntegral1() {
        return this.getFaceEntr().getFringeIntegral1();
    }

    /**
     * Get the exit fringe integral (a la H. Matsuda) which accounts  
     * for the first-order effects of the fringing field outside the quadrupole
     * magnet.
     * 
     * @return  fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagFringeQuadFace#setFrindIntegral1
     */
    public double   getExitFringeIntegral1() {
        return this.getFaceExit().getFringeIntegral1();
    }

    /**
     * Get the entrance fringe integral (a la H. Matsuda) which accounts  
     * for the first-order effects of the fringing field outside the quadrupole
     * magnet.
     * 
     * @return  fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagFringeQuadFace#setFrindIntegral2
     */
    public double   getEntrFringeIntegral2() {
        return this.getFaceEntr().getFringeIntegral2();
    }

    /**
     * Get the exit fringe integral (a la D.C. Carey) which accounts  
     * for the first-order effects of the fringing field outside the dipole
     * magnet.
     * 
     * @return  fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagFringeQuadFace#setFrindIntegral2
     */
    public double   getExitFringeIntegral2() {
        return this.getFaceExit().getFringeIntegral2();
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
    public double getK1()   {
        return this.getMagBody().getK1();
    }

    /**
     * Return the physical length of the bending dipole.  The design path length
     * is generally larger than this value because of the curvature.
     *  
     * @return  physical length through bend in <b>meters</b>
     */
    public double getPhysicalLength()    {
        return this.getMagBody().getLength();
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
        this.getFaceEntr().setOrientation(enmOrient);
        this.getMagBody().setOrientation(enmOrient);
        this.getFaceExit().setOrientation(enmOrient);
    };

    /**  
     *  Set the magnetic field strength of the dipole electromagnet.
     *
     *  @param  dblField    magnetic field (in <b>Tesla</b>).
     */
    public void setMagField(double dblField) {
        this.getFaceEntr().setMagField(dblField);
        this.getMagBody().setMagField(dblField);
        this.getFaceExit().setMagField(dblField);
    };




    /*
     * Internal Support
     */
     
    /**
     * Return the entrance dipole face object of the wedge dipole.
     * 
     * @return     entrance pole face
     */
    private IdealMagFringeQuadFace  getFaceEntr()   {
        return this.polEntr;
    }
     
    /**
     * Return the exit dipole face object of this wedge dipole magnet.
     * 
     * @return     exit pole face
     */
    private IdealMagFringeQuadFace   getFaceExit()   {
        return this.polExit;
    }
     
    /**
     * Return the dipole magnet body object of this wedge dipole magnet.  Note
     * that the body is of type <code>IdealMagSectorDipole</code> which has
     * no end effects.
     * 
     * @return     magnet body
     */
    private IdealMagQuad    getMagBody()    {
        return this.magBody;
    }

	/**
	 * Conversion method to be provided by the user
	 * 
	 * @param element the SMF node to convert
	 */
	@Override
	public void initializeFrom(LatticeElement element) {
		super.initializeFrom(element);
		
		Bend magnet = (Bend) element.getHardwareNode();
		setPosition(element.getCenterPosition(), element.getLength());

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
		setPhysicalLength(len_sect);
	/*	setDesignPathLength(len_path);		
		setFieldIndex(fld_ind0);
		setDesignBendAngle(ang_bend);*/
						
		/*if (element.getPartNr() == 0) // first piece
			setEntrPoleAngle(magnet.getEntrRotAngle() * Math.PI / 180.);
		if (element.getParts()-1 == element.getPartNr()) // last piece					
			setExitPoleAngle(magnet.getExitRotAngle() * Math.PI / 180.);*/
	}
}
