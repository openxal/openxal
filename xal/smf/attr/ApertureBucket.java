/*
 * ApertureBucket.java
 *
 * Created on September 18, 2001, 1:24 PM
 */

package xal.smf.attr;

/**
 *
 * @author  Christopher K. Allen
 * @version 1.0
 */


public class ApertureBucket extends AttributeBucket {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    
    /*
     *  Constants
     */
    
    public static final int     iUnkown     = 0;
    public static final int     iEllipse    = 1;
    public static final int     iRectangle  = 2;
    public static final int     iDiamond    = 3;
    public static final int     iIrregular  = 11;
 
    
    public final static String  c_strType = "aperture"; 

    final static String[]       c_arrNames = {  "shape",
                                                "x",
                                                "y"
                                };
    

                                
    /*
     *  Local Attributes
     */
    
    private Attribute m_attShape;
    private Attribute m_attAperX;
    private Attribute m_attAperY;
    
    
    /*
     *  User Interface
     */
    
    /** Furnish a unique type id  */
    public String getType()         { return c_strType; };

    public String[] getAttrNames()  { return c_arrNames; };
    

     
    
    /** Creates new ApertureBucket */
    public ApertureBucket() {
        super();
        
        m_attShape  = new Attribute(0);
        m_attAperX  = new Attribute(0.0);
        m_attAperY  = new Attribute(0.0);
        
        super.registerAttribute(c_arrNames[0], m_attShape);
        super.registerAttribute(c_arrNames[1], m_attAperX);
        super.registerAttribute(c_arrNames[2], m_attAperY);
    };

    
    public int      getShape()  { return m_attShape.getInteger(); };
    public double   getAperX()  { return m_attAperX.getDouble(); };
    public double   getAperY()  { return m_attAperY.getDouble(); };
    
    public void setShape(int intVal)    { m_attShape.set(intVal); };
    public void setAperX(double dblVal) { m_attAperX.set(dblVal); };
    public void setAperY(double dblVal) { m_attAperY.set(dblVal); };
    
};
