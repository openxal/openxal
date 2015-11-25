/*
 * Created on May 20, 2004
 *
 */
package xal.model;

import xal.model.elem.ElementSeq;



/**
 * Represents a logical sector of beamline.  This is essentially a semantic
 * concept since it simply extends the <code>ElementSeq</code> base class.
 * 
 * @author Christopher K. Allen
 * @since  May 20, 2004
 */
public class Sector extends ElementSeq {

    
    /*
     *  Global Constants
     */
    
    /** the string type identifier for all Sector objects */
    public static final String      s_strType = "Sector";
    
    
    /*
     * Initialization
     */
    
    /**
     * Default constructor.  The <code>Sector</code> object is empty and has 
     * no string identifier.
     */
    public Sector() {
        super(s_strType);
    }

    /**
     * Create new <code>Sector</code> object and initialize the string 
     * identifier.
     * 
     * @param strId     string identifier of this sector
     */
    public Sector(String strId) {
        super(s_strType, strId);
    }

    /**
     * Create new <code>Sector</code> object specifying the amount of storage to
     * reserve for the direct child components.  (If not specified a default
     * value is used.)
     *  
     * @param strId         string identifier of this sector
     * @param szReserve     number of storage positions to reserve for children 
     */
    public Sector(String strId, int szReserve) {
        super(s_strType, strId, szReserve);
    }



//    /**
//     *  Return an <code>List</code> object that is a flattened
//     *  version of the current of the current (possibly nested) sequence.  
//     *  The sequential order of propagation is preserved but the nesting 
//     *  of modeling elements elements is destroyed.
//     * 
//     *  NOTE: 
//     *  - Any composite elements derived from ElementSeq are flattened.
//     * 
//     *  This method is nondestructive, the current sequence is 
//     *  unaltered.
//     * 
//     * @return flattened list containing only sequence leaves
//     */
//    public List   flatten()   {
//        ArrayList  arrFlat  = new ArrayList();
//        
//        Iterator    iter = this.childIterator();
//        while (iter.hasNext()) {
//            IElement ifcElem = (IElement)iter.next();
//            
//            if (ifcElem instanceof ElementSeq)  {
//                ElementSeq seqChild = (ElementSeq)ifcElem;
//
//                seqFlat.concatenateEquals(seqChild.flatten());
//
//            } else {
//                seqFlat.addChild(ifcElem);
//                                
//            }
//        }
//        
//        return seqFlat;
//    }
//    
}
