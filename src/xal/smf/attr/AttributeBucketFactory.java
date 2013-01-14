package xal.smf.attr;

/**
 * Class factory for all (registered) AttributeBucket objects.
 *
 * @author  Nikolay Malitsky, Christopher K. Allen
 */

import  java.util.*;
import  java.lang.reflect.*;


public final class AttributeBucketFactory {


    /*
     *  Global Attributes
     */
    
    static private HashSet<AttributeBucket>                  m_setBuckTypes;     // set of all AttributeBucket derived classes
    static private HashMap<String,Constructor<?>>                  m_mapCtors;         // map of node type ids to constructors
    
    
    
    /** Classloader invoked script - must be modified to register all AttributeBucket types */
    static {

        registerClass(new AlignmentBucket());
        registerClass(new ApertureBucket());
        registerClass(new DisplaceBucket());
        registerClass(new MagnetBucket());
        registerClass(new RfCavityBucket());
        registerClass(new RotationBucket());
        registerClass(new TwissBucket());
        registerClass(new SequenceBucket());
        
        
        buildCtorMap();
  };
    

    
    
    /** Get set of all AccelerNode type strings */
    public static String[] getBucketTypes() {
        int                 nTypes;     // number of node types
        String[]            arrTypes;   // returned array of node type strings

        
        // Allocate the string array
        nTypes   = m_mapCtors.size();
        arrTypes = new String[nTypes];
        
        
        // Build the string array
        int                 iType;      // index of current type
        final Set<String> nodeTypes = m_mapCtors.keySet();
        iType    = 0;
		for ( final String nodeType : nodeTypes ) {
            arrTypes[iType++] = nodeType;
        }
            
        return arrTypes;
    };
        
    

    /** Creates the node with the specified string id. */
    public static AttributeBucket create(String strType) throws ClassNotFoundException {
        
        // Error check
        if (!m_mapCtors.containsKey(strType)) 
            throw new ClassNotFoundException("Unknown AttributeBucket type : " + strType);
        
        
        // Find the constructur object for the AttributeBucket and instantiate new node
        Constructor<?>         ctor;       // contructor object for node type
        Object[]            arrArgs;    // constructor arguments
        AttributeBucket     buck;       // the returned object

        ctor    = m_mapCtors.get(strType);
        arrArgs = null;
        
        try {
            buck    = (AttributeBucket)ctor.newInstance(arrArgs);
        } catch (Throwable e)   {
             throw new ClassNotFoundException("Unknown AttributeBucket type : " + strType);
        }
       
        
        // Return new node
        return buck;
    };
  
    
    
    /*
     *  Internal Support
     */

    private static void registerClass(AttributeBucket objInst)   {
        if (m_setBuckTypes == null)
            m_setBuckTypes = new HashSet<AttributeBucket>();

        m_setBuckTypes.add(objInst);
    };
    

	@SuppressWarnings( "rawtypes" )		// generics aren't supported in arrays
    private static void buildCtorMap()  {
        m_mapCtors = new HashMap<String,Constructor<?>>();

		for ( final AttributeBucket bucType : m_setBuckTypes ) {            
            try {                
                Class<?>           clsType = bucType.getClass();
                String          strType = bucType.getType();
                Constructor<?>     ctrType = clsType.getConstructor(new Class[] { });

                m_mapCtors.put(strType, ctrType);
                
           } catch (NoSuchMethodException e) { 
                System.out.println("NoSuchMethodException: " + e.getMessage());
           } catch (SecurityException e)    {
               System.out.println("SecurityException: " + e.getMessage());
           }
                
        }
    };
  
    /** Hide the constructor - should never be called */
    private AttributeBucketFactory() {};
   
  
};
