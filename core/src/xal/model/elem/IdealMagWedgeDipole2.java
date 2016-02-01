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
import xal.model.IProbe;

/**
 * Represents a bending dipole magnet with arbitrary pole face angles.
 * This is a composite element constructed from three sub-elements - one
 * <code>IdealMagSectorDipole</code> sandwiched between two 
 * <code>IdealDipoleFace</code> elements that provided the thin lens
 * dynamics of the tilted pole faces.
 * 
 * NOTE:
 * A rectangle dipole can be specified by setting equal exit and entrance
 * pole face angles.
 * 
 * @author Christopher K. Allen
 * 
 * @see xal.model.elem#IdealMagSectorDipole
 * @see xal.model.elem#IdealMagDipoleFace
 *
 */
public class IdealMagWedgeDipole2 extends ElectromagnetSeq {



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
    
    /** local position of this element - position within parent sequence */
    private double      dblPos;
    
    
    /** magnet body */
    private final IdealMagSectorDipole2    magBody = new IdealMagSectorDipole2();
    
    /** magnet entrance pole face */
    private final IdealMagDipoleFace2      polEntr = new IdealMagDipoleFace2();
    
    /** magnet entrance pole face */
    private final IdealMagDipoleFace2      polExit = new IdealMagDipoleFace2();
    



    /*
     * Initialization
     */

    /**
     * Default constructor - creates a new uninitialized instance of 
     * <code>IdealMagWedgeDipole</code>.
     */
    public IdealMagWedgeDipole2() {
        this(null);
    }

    /**
     * Create new <code>IdealMagWedgeDipole</code> object and specify its
     * instance identifier string.
     * 
     * @param strId     instance identifier string
     */
    public IdealMagWedgeDipole2(String strId) {
        super(s_strType, strId, s_szReserve);
        
        this.dblPos = 0.0;
        
        this.addChild(this.polEntr);
        this.addChild(this.magBody);
        this.addChild(this.polExit);
    }

    /**
     * Overrides the default <code>setHardwareNodeId(String)</code> method
     * for the <code>ElementSeq</code> base class so we can set the
     * hardware node IDs for the composite elements.
     *
     * @see xal.model.elem.ElementSeq#setHardwareNodeId(java.lang.String)
     *
     * @author Christopher K. Allen
     * @since  Sep 5, 2014
     */
    @Override
    public void setHardwareNodeId(String strSmfId) {
        super.setHardwareNodeId(strSmfId);
        
        this.getMagBody().setHardwareNodeId(strSmfId);
        this.getFaceEntr().setHardwareNodeId(strSmfId);
        this.getFaceExit().setHardwareNodeId(strSmfId);
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
    
    
    /**
     * <p>Set the position of the magnet along the design path within the
     * containing lattice.
     * </p>
     * 
     * NOTE:
     * <p>We have a bit of a logistics problem here because this is a 
     * composite element.  So when setting the position of this element
     * we want to set the positions of all the internal elements, in 
     * particular, the pole faces.  Thus, we need the physical length
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
     * @see IdealMagWedgeDipole2#setPhysicalLength(double)
     */
    public void setPosition(double dblPos)   {
//        this.getMagBody().setPosition(dblPos);
        this.dblPos = dblPos;
    }

    /**
     * Set the physical length of the bending dipole magnet itself.  
     * The design path length
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
    public void setDesignPathLength(double dblPathLen)  {
        this.getFaceEntr().setDesignPathLength( dblPathLen );
        this.getMagBody().setDesignPathLength( dblPathLen );
        this.getFaceExit().setDesignPathLength( dblPathLen );
    }
    
    /**
     * Set the bending angle of the reference (design) orbit.
     * 
     * @param   dblBendAng      design trajectory bending angle (radians) 
     */
    public void setDesignBendAngle(double dblBendAng)   {
        this.getFaceEntr().setDesignBendAngle( dblBendAng );
        this.getMagBody().setDesignBendAngle( dblBendAng );
        this.getFaceExit().setDesignBendAngle( dblBendAng );
    }


    /**
     * Set the magnetic field index of the magnet evaluated at the design
     * orbit.   The field index is defined as
     * <br>
     * <br>
     * &nbsp; &nbsp;   n &equiv; -(R0/B0)(dB/dR)
     * <br>
     * <br>
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
     *  for  SAD elements, K1 variable (=normal k1*L) in SAD
     *  
     * @param dblFldInd the field index for the quadrupole
     */
    public void setQuadComponent(double dblFldInd) {
        this.getMagBody().setQuadComponent(dblFldInd);
    }
    
    /**
     * Set the gap size between the dipole magnet poles.
     * 
     * @param dblGap    gap size in <b>meters</b>
     */
    public void setGapSize(double dblGap)  {
        this.getFaceEntr().setGapHeight(dblGap);
        this.getMagBody().setGapHeight(dblGap);
        this.getFaceExit().setGapHeight(dblGap);
    }
    
    /**
     * Set the entrance pole face angle with respect to the design trajectory
     * 
     * @param   dblAngPole  pole face angle in <b>radians</b>
     */
    public void setEntrPoleAngle(double dblAngPole) {
        this.getFaceEntr().setPoleFaceAngle(dblAngPole);
    }
    
    /**
     * Set the exit pole face angle with respect to the design trajectory
     * 
     * @param   dblAngPole  pole face angle in <b>radians</b>
     */
    public void setExitPoleAngle(double dblAngPole) {
        this.getFaceExit().setPoleFaceAngle( dblAngPole );
    }
    
    /**
     * Set the entrance fringe integral (a la D.C. Carey) which accounts  
     * for the first-order effects of the fringing field outside the dipole
     * magnet.
     * 
     * @param   dblFldInt   fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagDipoleFace2#setFringeIntegral
     */
    public void setEntrFringeIntegral(double dblFldInt) {
        this.getFaceEntr().setFringeIntegral(dblFldInt);
    }

    /**
     * Set the exit fringe integral (a la D.C. Carey) which accounts  
     * for the first-order effects of the fringing field outside the dipole
     * magnet.
     * 
     * @param   dblFldInt   fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagDipoleFace2#setFringeIntegral
     */
    public void setExitFringeIntegral(double dblFldInt) {
        this.getFaceExit().setFringeIntegral(dblFldInt);
    }

    
    
    /**
     * sako use design field if fieldPathFlag = 1, and use bfield if 0
     */
    public void setFieldPathFlag(boolean bolFlag) {
    	this.getFaceEntr().setFieldPathFlag(bolFlag);
    	this.getMagBody().setFieldPathFlag(bolFlag);
    	this.getFaceExit().setFieldPathFlag(bolFlag);
    }

    /**
     * Set the dipole strength of the magnet - I think.  I didn't
     * write this method.
     * 
     * @param k0        magnet strength ?
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public void setK0(double k0) {
    	this.getFaceEntr().setK0(k0);
    	this.getMagBody().setK0(k0);
    	this.getFaceExit().setK0(k0);
    }
    
    
    /*
     * Attribute Query
     */    
    

    /**
     * Returns the magnet strength I think.  I didn't write this
     * method so I am guessing (I better name or some documentation
     * would have helped).
     *
     * @return  magnet strengh ?
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public double getK0() {
        return this.getMagBody().getK0();
    }

    /**
     * Return the gap size between the dipole magnet poles.
     * 
     * @return  gap size in <b>meters</b>
     */
 
    public double   getGapHeight()  {
        return this.getMagBody().getGapHeight();
    }

    /**
     * Get the entrance pole face angle with respect to the design trajectory
     * 
     * @return  pole face angle in <b>radians</b>
     */
    public double   getEntrPoleAngle() {
        return this.getFaceEntr().getPoleFaceAngle();
    }
    
    /**
     * Get the exit pole face angle with respect to the design trajectory
     * 
     * @return  pole face angle in <b>radians</b>
     */
    public double   getExitPoleAngle() {
        return this.getFaceExit().getPoleFaceAngle();
    }
    
    /**
     * Get the entrance fringe integral (a la D.C. Carey) which accounts  
     * for the first-order effects of the fringing field outside the dipole
     * magnet.
     * 
     * @return  fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagDipoleFace2#getFringeIntegral
     */
    public double   getEntrFringeIntegral() {
        return this.getFaceEntr().getFringeIntegral();
    }

    /**
     * Get the exit fringe integral (a la D.C. Carey) which accounts  
     * for the first-order effects of the fringing field outside the dipole
     * magnet.
     * 
     * @return  fringe field integral (<b>unitless</b>)
     *  
     * @see IdealMagDipoleFace2#getFringeIntegral
     */
    public double   getExitFringeIntegral() {
        return this.getFaceExit().getFringeIntegral();
    }



    /**
     * <p>
     * Return the magnetic field index of the magnet evaluated at the design
     * orbit.   The field index is defined as
     * <br>
     * <br>
     * &nbsp; &nbsp; <i>n</i> &equiv; -(<i>R</i><sub>0</sub>/<i>B</i><sub>0</sub>)(<i>dB/dR</i>)
     * <br>
     * <br>
     * where <i>R</i><sub>0</sub> is the radius of the design orbit, <i>B</i><sub>0</sub>
     * is the field at the
     * design orbit (see {@link IdealMagSectorDipole2#getFieldIndex()}), and <i>dB/dR</i> is the
     * derivative of the field with respect to the path deflection - evaluated
     * at the design radius <i>R</i><sub>0</sub>.
     * 
     * @return  field index of the magnet at the design orbit (unitless)     
     */
    public double getFieldIndex()   {
        return this.getMagBody().getFieldIndex();
    }

    /**
     * Return the physical length of the bending dipole.  The design path length
     * is generally larger than this value because of the curvature.
     *  
     * @return  physical length through bend in <b>meters</b>
     * 
     */
    public double getPhysicalLength()    {
        return this.getMagBody().getLength();
    }
    
    /**
     * Return the path length of the design trajectory through the
     * magnet.
     * 
     * @return      design trajectory path length (in meters)
     * 
     */
    public double   getDesignPathLength()   {
        return this.getMagBody().getDesignPathLength();
    }
    
    /**
     * Return the bending angle of the magnet's design trajectory.
     * 
     * @return     design trajectory bending angle (in radians)
     * 
     */
    public double   getDesignBendingAngle() {
        return this.getMagBody().getDesignBendingAngle();
    }

    
    /*
     * Dynamic Parameters
     */
    
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
    public double   compProbeCurvature(IProbe probe) {
        return this.getMagBody().compProbeCurvature(probe);
    }


    /*
     * ElementSeq Overrides
     */
    
    /**
     * Override the default <code>setId(String)</code> method for
     * <code>ElementSeq</code> objects so we can set the identifier
     * strings of each composite element.
     * 
     * @param   strId       identifier string of this composite.
     * 
     * @see xal.model.elem.ElementSeq#setId(java.lang.String)
     */
    @Override
    public void setId(String strId) {
        super.setId(strId);
        
        this.getMagBody().setId(strId + "Body");
        this.getFaceEntr().setId(strId + "Entr");
        this.getFaceExit().setId(strId + "Exit");
    }

    
    /*
     * IComponent Interface
     */

    /**
     * This method overrides the <code>ElementSeq</code> base class implementation
     * to return an independent property for this element.  This property is 
     * set upon initialization since, although this element is a sequence, it
     * represents a single hardware node.  Thus, the position is a hardware
     * property, and not a dynamic property of the sequence.
     *
     * @see xal.model.elem.ElementSeq#getPosition()
     *
     * @since  Dec 3, 2015,  Christopher K. Allen
     */
    @Override
    public double getPosition() {
//        return this.getMagBody().getPosition();
        return this.dblPos;
    }

    /**
     * Override the method in base class <code>ElementSeq</code> to return an independent
     * property for this element.  Since this class represents a single hardware node
     * it has a length that does not change.
     *
     * @see xal.model.elem.ElementSeq#getLength()
     *
     * @since  Dec 3, 2015,  Christopher K. Allen
     */
    @Override
    public double getLength() {
        return this.getMagBody().getLength();
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
        this.setPosition(element.getCenterPosition());
    
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
        setDesignPathLength(len_path);      
        setFieldIndex(fld_ind0);
        setDesignBendAngle(ang_bend);
        
        this.magBody.setPosition(len_sect/2.0);
        this.polEntr.setPosition(0.0);
        this.polExit.setPosition(len_sect);
                        
        if (element.isFirstSlice()) // first piece
            setEntrPoleAngle(magnet.getEntrRotAngle() * Math.PI / 180.);
        if (element.isLastSlice()) // last piece
            setExitPoleAngle(magnet.getExitRotAngle() * Math.PI / 180.);
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
    @Override
    public int getOrientation() {
        return this.getMagBody().getOrientation();
    };

    /**  
     *  Get the magnetic field strength of the dipole electromagnet
     *
     *  @return     magnetic field (in <b>Tesla</b>).
     */
    @Override
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
    @Override
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
    @Override
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
    private IdealMagDipoleFace2  getFaceEntr()   {
        return this.polEntr;
    }
     
    /**
     * Return the exit dipole face object of this wedge dipole magnet.
     * 
     * @return     exit pole face
     */
    private IdealMagDipoleFace2   getFaceExit()   {
        return this.polExit;
    }
     
    /**
     * Return the dipole magnet body object of this wedge dipole magnet.  Note
     * that the body is of type <code>IdealMagSectorDipole</code> which has
     * no end effects.
     * 
     * @return     magnet body
     */
    private IdealMagSectorDipole2    getMagBody()    {
        return this.magBody;
    }

}
