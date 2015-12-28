package xal.model.elem;

import xal.model.elem.sync.IElectrostatic;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.Electrostatic;

/**
 *  This class implements IElectrostatic interface for thick electrostatics.
 *
 */
public abstract class ThickElectrostatic extends ThickElement implements IElectrostatic {

	/*
    *  Local Attributes
    */
	
	
	/** bending plane of dipole */
	protected int                 enmOrient = ORIENT_NONE;
   
   	/** Field strength of the dipole magnet */
   	protected double              dblField = 0.0;
	
   
	public ThickElectrostatic(String strType, String strId, double dblLen) {
		super(strType, strId, dblLen);
	}

	public ThickElectrostatic(String strType, String strId) {
		super(strType, strId);
	}

	public ThickElectrostatic(String strType) {
		super(strType);
	}

	
	
   /*
    *  IElectromagnet Interface
    */

    /**
     *  Return the orientation enumeration code.
     *
     *  @return     ORIENT_HOR  - quadrupole focuses in x (horizontal) plane
     *              ORIENT_VER  - quadrupole focuses in y ( vertical ) plane
     *              ORIENT_NONE - error
     */
   /**
    * Return the orientation enumeration code for the bending plane of the 
    * associated dipole magnet.
    *
    *  @return     ORIENT_HOR  - dipole has steering action in x (horizontal) plane
    *              ORIENT_VER  - dipole has steering action in y (vertical) plane
    *              ORIENT_NONE - error
    */
   public int getOrientation() {
       return this.enmOrient;
   };

   
   /**  
    *  Get the magnetic field strength of the electromagnet
    *
    *  @return     magnetic field (in <b>Tesla/meter</b>).
    */

   /**  
    *  Get the magnetic field strength of the associated dipole
    *
    *  @return     magnetic field (in <b>Tesla</b>).
    */
   public double getMagField() {
       return this.dblField;
   };
	
   /**
    *  Return the orientation enumeration code specifying the bending plane.
    *
    *  @return     ORIENT_HOR  - dipole has steering action in x (horizontal) plane
    *              ORIENT_VER  - dipole has steering action in y (vertical) plane
    *              ORIENT_NONE - error
    */
   /**
    *  Set the dipole bending plane orientation
    *  
    *  @param  enmOrient   magnet orientation enumeration code
    *
    *  @see    #getOrientation
    */
   public void setOrientation(int enmOrient) {
       this.enmOrient = enmOrient;
   };

   /**  
    *  Set the magnetic field strength of the electromagnet.
    *
    *  @param  dblField    magnetic field (in <b>Tesla/meter</b>).
    */
   
   /**  
    *  Set the magnetic field strength of the dipole electromagnet.
    *
    *  @param  dblField    magnetic field (in <b>Tesla</b>).
    */
   public void setMagField(double dblField) {
       this.dblField = dblField;
   };

	
	/**
	 * Conversion method to be provided by the user
	 * 
	 * @param latticeElement the SMF node to convert
	 */
   @Override
	public void initializeFrom(LatticeElement latticeElement) {
	   	super.initializeFrom(latticeElement);
	   	Electrostatic magnetNode =  (Electrostatic)latticeElement.getHardwareNode();			
		int orientation = IElectrostatic.ORIENT_NONE;
		if (magnetNode.isHorizontal()) {
			orientation = IElectrostatic.ORIENT_HOR;
		} else if (magnetNode.isVertical()) {
			orientation = IElectrostatic.ORIENT_VER;
		}
		setOrientation(orientation);
		setVoltage(magnetNode.getDesignField());		
	}
	
}
