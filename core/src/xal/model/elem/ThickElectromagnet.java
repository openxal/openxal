package xal.model.elem;

import xal.model.elem.sync.IElectromagnet;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.Magnet;

/**
 *  This class implements IElectromagnet interface for thick magnets.
 *
 */
public abstract class ThickElectromagnet extends ThickElement implements IElectromagnet {

	/*
    *  Local Attributes
    */
	
	
	/** bending plane of dipole */
	protected int                 enmOrient = ORIENT_NONE;
   
   	/** Field strength of the dipole magnet */
   	protected double              dblField = 0.0;
	
   
	public ThickElectromagnet(String strType, String strId, double dblLen) {
		super(strType, strId, dblLen);
	}

	public ThickElectromagnet(String strType, String strId) {
		super(strType, strId);
	}

	public ThickElectromagnet(String strType) {
		super(strType);
	}

	
	
   /*
    *  IElectromagnet Interface
    */

    /**
     *  Return the orientation enumeration code.
     * Return the orientation enumeration code for the bending plane of the 
     * associated dipole magnet / focusing plane of quadrupole.
     *
     *  @return     ORIENT_HOR  - quadrupole focuses in x (horizontal) plane
     *  						  dipole has steering action in x (horizontal) plane
     *              ORIENT_VER  - quadrupole focuses in y ( vertical ) plane
     *                            dipole has steering action in y (vertical) plane
     *              ORIENT_NONE - error
     */
   public int getOrientation() {
       return this.enmOrient;
   };

   /**  
    *  Get the magnetic field strength of the electromagnet
    *
    *  @return     magnetic field (in <b>Tesla/meter</b> for quads, <b>Tesla</b> for dipoles).
    */
   public double getMagField() {
       return this.dblField;
   };

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
    *  @param  dblField    magnetic field (in <b>Tesla/meter</b> for quads, <b>Tesla</b> for dipoles).
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
	   	Magnet magnetNode =  (Magnet)latticeElement.getHardwareNode();			
		int orientation = IElectromagnet.ORIENT_NONE;
		if (magnetNode.isHorizontal()) {
			orientation = IElectromagnet.ORIENT_HOR;
		} else if (magnetNode.isVertical()) {
			orientation = IElectromagnet.ORIENT_VER;
		} else {
		    //  This is an exceptional condition!
		    //    Something went wrong if we made it here
		    //    Let's say so
			    
		    // CKA - we are going to skip this since skew quadrupoles (soft type = "QSC")
		    //    have no orientation and always throw this warning
//			    String    strSrc = magnetNode.getId() + "/" + magnetNode.getClass().getName();
//			    String    strMsg = "Encountered an un-oriented electromagnet hardware object";
//			    Logger    logGbl = Logger.getLogger("global");
//			    
//			    logGbl.log(Level.WARNING, strMsg + " : " + strSrc);
//	            System.out.println("WARNING!: " + strMsg + " : " + strSrc);		
		}
		setOrientation(orientation);
		setMagField(magnetNode.getDesignField());
	}
	
}
