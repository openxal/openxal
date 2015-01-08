package xal.model.elem;

import xal.model.elem.sync.IElectromagnet;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.Magnet;

/**
 *  This class implements IElectromagnet interface for composed magnets.
 *
 */
public abstract class ElectromagnetSeq extends ElementSeq implements IElectromagnet {

	public ElectromagnetSeq(String strType, String strId, int szReserve) {
		super(strType, strId, szReserve);
	}

	public ElectromagnetSeq(String strType, String strId) {
		super(strType, strId);
	}

	public ElectromagnetSeq(String strType) {
		super(strType);
	}

	
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
