package xal.smf.attr;

import java.util.Map;
import java.util.HashMap;
import xal.tools.data.*;



/** 
 * The root class in the inheritance hierarchy of orthogonal sets 
 * of element attributes (such as a set of multipole harmonics, 
 * a set of aperture parameters, and others). 
 *
 *  Derived classes should call registerAttribute() in their constructor on all 
 *  Attribute objects belonging to the attribute bucket.
 *
 * @author  Nikolay Malitsky, Christopher K. Allen
 */

public abstract class AttributeBucket implements java.io.Serializable, DataListener {
	/** required for Serializable */
	static final private long serialVersionUID = 1L;
	
    /** map of attributes keyed by value */
    private Map<String,Attribute> m_mapAttrs;
    
    
    /** Derived class must furnish a unique type id */
    abstract public String getType();
    

    // DataListener interface
    
    /** implement DataListener interface */
    public String dataLabel() { return getType(); }
    
    
    /** implement DataListener interface */
    public void update(DataAdaptor adaptor) throws NumberFormatException {
        String[] attributeArray = adaptor.attributes();
        
        for ( int index = 0 ; index < attributeArray.length ; index++ ) {
            String name = attributeArray[index];
            String stringValue = adaptor.stringValue(name);
            parseAttrValue(name, stringValue);
        }
    }
    
    
    /** implement DataListener interface */
    public void write(DataAdaptor adaptor) {
        String[] attributeNames = getAttrNames();
        int numAttributes = attributeNames.length;
        
        for ( int index = 0 ; index < numAttributes ; index++ ) {
            String name = attributeNames[index];
            Attribute attribute = getAttr(name);
            if ( attribute == null ) {
                continue;
                //throw MissingAttributeException.newException(this, name);
            }
            Object value = attribute.getObject();
            String stringValue = attribute.stringValue();
            
            if ( stringValue.length() > 0 ) {
                adaptor.setValue(name, stringValue);
            }
            
        }
    }
    
    // end DataListener interface

    
    
    
    /*
     *  Attribute Query
     */
    
    public Attribute   getAttr(String strName)  { 
        return m_mapAttrs.get(strName); 
    };
    
    public String[]  getAttrNames()    {
        int             nNames;             // number of attribute names
        java.util.Set<String>   setKeys;            // keys of the map (i.e., attribute names)
        
        setKeys = m_mapAttrs.keySet();
        nNames  = setKeys.size();
        
        return setKeys.toArray( new String[] {} );
    };
    
    
    /*
     *  Parsing attribute values (Attribute know their own type)
     */
    
    public boolean parseAttrValue(String strName, String strVal)  throws NumberFormatException   {
        if ( !m_mapAttrs.containsKey( strName ) ) return false;
      
        return m_mapAttrs.get( strName ).parse( strVal );
    };
       
    
    
    /*
     *  Setting attribute values directly
     */
    
    public boolean setAttrValue(String strName, int newVal)  {
        if (!checkAttribute(strName, Attribute.iInteger)) return false;
      
        m_mapAttrs.get(strName).set(newVal);
        return true;
    };
        
    public boolean setAttrValue(String strName, long newVal)  {
        if (!checkAttribute(strName, Attribute.iLong)) return false;
      
        m_mapAttrs.get(strName).set(newVal);
        return true;
    };

    public boolean setAttrValue(String strName, float newVal)  {
        if (!checkAttribute(strName, Attribute.iFloat)) return false;
      
        m_mapAttrs.get(strName).set(newVal);
        return true;
    };
        
    public boolean setAttrValue(String strName, double newVal)  {
        if (!checkAttribute(strName, Attribute.iDouble)) return false;
      
        m_mapAttrs.get(strName).set(newVal);
        return true;
    };
        
    public boolean setAttrValue(String strName, String newVal)  {
        if (!checkAttribute(strName, Attribute.iString)) return false;
      
        m_mapAttrs.get(strName).set(newVal);
        return true;
    };
        

    // Arrays...
    
    public boolean setAttrValue(String strName, int[] newVal)   {
        if (!checkAttribute(strName, Attribute.iArrInt)) return false;
      
        m_mapAttrs.get(strName).set(newVal);
        return true;
    };

    public boolean setAttrValue(String strName, long[] newVal)   {
        if (!checkAttribute(strName, Attribute.iArrLng)) return false;
      
        m_mapAttrs.get(strName).set(newVal);
        return true;
    };

    public boolean setAttrValue(String strName, float[] newVal)   {
        if (!checkAttribute(strName, Attribute.iArrFlt)) return false;
      
        m_mapAttrs.get(strName).set(newVal);
        return true;
    };

    public boolean setAttrValue(String strName, double[] newVal)   {
        if (!checkAttribute(strName, Attribute.iArrDbl)) return false;
      
        m_mapAttrs.get(strName).set(newVal);
        return true;
    };

    public boolean setAttrValue(String strName, String[] newVal)   {
        if (!checkAttribute(strName, Attribute.iArrStr)) return false;
      
        m_mapAttrs.get(strName).set(newVal);
        return true;
    };

    
   
    /*
     *  Protected Members
     */
    
    
    /** AttributeBucket should only be instantiated by a derived class */
    protected AttributeBucket() {
        m_mapAttrs = new HashMap<String,Attribute>();
    }

    
    /** Used by derived classes to define particular attributes  */
    protected void registerAttribute(String strName, Attribute attr)    {
        m_mapAttrs.put(strName, attr);
    };
    
    
    
    // Auxilary Functions
    
    /** Check attribute validity */
    protected boolean   checkAttribute(String strName, int iType)   {
        if (!m_mapAttrs.containsKey(strName)) return false;
      
        Attribute attr = m_mapAttrs.get(strName);
        if (attr.getType() != iType) return false;
        
        return true;
    };
    
    
    
    public static class MissingAttributeException extends RuntimeException {
        /** ID for serializable version */
        private static final long serialVersionUID = 1L;
        
        public MissingAttributeException(String message) {
            super(message);
        }
        
        static private MissingAttributeException newException(AttributeBucket bucket, String attributeName) {
            String message = "Error, missing attribute: " + attributeName + 
                ", for bucket type: " + bucket.getType();
            return new MissingAttributeException(message);
        }
    }
}
