/*
 * ElementFactory.java
 *
 * Created on October 9, 2002, 6:31 PM
 */

package xal.model.elem;


import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import xal.model.IElement;
import xal.tools.data.DataFormatException;

/**
 *  Utility class for instantiated XAL Model Elements.  It is the class factory 
 *  for all IElement based objects. 
 *  <p>
 *  Every element class exposing the <code>IElement</code> interface should register itself 
 *  in the static class-loader code (static {} block) in order to be recognized by this class 
 *  factory.  The requirements for registration are that the registering class expose the
 *  <code>IElement</code> interface and have a constructor taking the single argument of
 *  a <code>DataAdaptor</code> object.  The constructor should fully initialize the object
 *  according to the data available in the data adaptor.
 *  </p>
 *  <p>
 *  The factory is useful in parsing XML files in using the XAL Model Lattice DTD format.  
 *  Every IElement has a type code (in String format) which may be used to identify
 *  the class.
 *  </p>
 *
 * @author  Christopher Allen
 *
 * @see xal.tools.data.DataAdaptor
 * 
 * @deprecated  This class is being phased out since it requires "registration."  Currently
 *              there are no references to it and can be deleted.
 */
@Deprecated
public class ElementFactory {
    /** map of class type-string to data adaptor constructors */
    static private      Map<String, Constructor<? extends IElement>> s_mapCtors =
        new HashMap<String, Constructor<? extends IElement>>();
    
    /** map of class type-string to class class */
    static private      Map<String, Class<? extends IElement>> s_mapClass =
        new HashMap<String, Class<? extends IElement>>();
    
    static {
        // Allocate global resources and register all known element classes.

        registerIElement(IdealMagSextupole.s_strType,       IdealMagSextupole.class );
        registerIElement(IdealMagSteeringDipole.s_strType,  IdealMagSteeringDipole.class);
        registerIElement(IdealRfGap.s_strType,              IdealRfGap.class);
        registerIElement(Marker.s_strType,                  Marker.class);
        registerIElement(ThinLens.s_strType,                ThinLens.class);
        registerIElement(ThinMatrix.s_strType,              ThinMatrix.class);

        registerIElement(IdealDrift.s_strType,              IdealDrift.class);
        registerIElement(IdealMagQuad.s_strType,            IdealMagQuad.class);
        registerIElement(ThickMatrix.s_strType,             ThickMatrix.class);
        registerIElement(ThickDipole.s_strType,             ThickDipole.class);
    }

    
    /**
     *  Get the total number of registered elements, both thin and thick.
     * 
     *  @return     total number of element types registered
     */
    public static int  getRegisteredCount()   {
        return s_mapCtors.size();
    }
    
    /** 
     *  Get array of all element type strings, both thin and thick, that are 
     *  registered to class factory.
     *
     *  @return         string array of supported element types
     */
    public static String[] getRegisteredTypes() {
        return s_mapClass.keySet().toArray(new String[s_mapClass.size()]);
    }
    
    /**
     *  Get array of class types for registered IElement classes.
     *
     *  @return         array registered IElement class types
     */
    @SuppressWarnings( {"unchecked", "rawtypes"})	// arrays don't mix with generics
    public static Class<? extends IElement>[] getRegisteredClasses() {
        return s_mapClass.values().toArray(new Class[s_mapClass.size()]);
    }

    /**
     *  Register the IElement exposing class with the class factory.  The class must 
     *  have an initializing constructor with the single argument of a DataAdaptor.
     *  That is, we require the method <code>Element(DataAdaptor daptInit)</code>.
     *  <p>
     *  The type string in the argument <code>strType</code> must be the same string 
     *  returned by the <code>IElement</code> method <code>getType():String</code>.  
     *  The argument <code>clsType</code> is the <code>Class</code> class of the
     *  class being registered with this factory.  The class must expose the <code>
     *  IElement</code> interface.
     *  </p>
     *  <p>
     *  Example:
     *      For the derived class <code>ThinLens</code> the static block should
     *      include the following:
     *  </p>
     *      <pre>
     *      static { 
     *      ...
     *      registerIElement(ThinLens.s_strType, ThinLens.class) 
     *      ...
     *      }
     *      </pre>
     *
     *  @param  strType     type identifier of the class 
     *  @param  clsType     <code>Class</code> class for registered class
     *
     *  @return             true if class was successfully registered, false otherwise
     */
    public static boolean registerIElement(String strType, Class<? extends IElement> clsType)   {
        try {
            Constructor<? extends IElement> ctorFac = clsType.getConstructor();
            s_mapClass.put(strType, clsType); 
            s_mapCtors.put(strType, ctorFac);
            
        } catch (NoSuchMethodException e) { 
            System.out.println("ElementFactory#registerIElement - invalid factory constructor for " + strType);
            return false;
            
        } catch (SecurityException e)    {
            System.out.println("ElementFactory#registerIElement - factor constructor unaccessable for " + strType);
            return false;
            
        }
        
        return true;
    }
    
    /** 
     *  Create a IElement object based on information stored in a <code>DataAdaptor</code>
     *  object.
     *
     *  @param  strType     string type identifier for IElement concrete class
     *
     *  @return             new <code>IElement</code> exposing object
     *
     *  @exception  ClassNotFoundException  unknown class or class does not have proper constructor
     *  @exception  DataFormatException     data in adaptor does not conform to DTD
     *  @exception  NumberFormatException   corrupted position field
     *  @exception  InstantiationException  unknown error occurred during element construction
     */
    public static IElement createIElement(String strType) 
        throws ClassNotFoundException, InstantiationException 
    {
        Constructor<? extends IElement> ctor = s_mapCtors.get(strType);
        if(ctor == null) {
            throw new ClassNotFoundException("ElementFactory#createIElement - Unknown Element type : " + strType);
        }

        
        try {
            return ctor.newInstance();
        } catch (Throwable e)   {
            throw new InstantiationException("ElementFactory#createIElement - error occurred during creation of " + strType);
        }
    }

    
    
  
    /*
     *  Testing and Debuging
     */
    
    /**
     *  Class test driver
     */
    public static void main(String[] arrArgs)  {

        PrintWriter     os = new PrintWriter(System.out);
        
        // Inspect all the registered elements
        testRegistration(os);
        
        // Test creation mechanism for all registered elements
        String  strTypes[] = getRegisteredTypes();
        for (int i=0; i<strTypes.length; i++)   
            ElementFactory.testCreation(os, strTypes[i]);
    }

    /**
     *  Test element registration mechanism.
     *  Print out the registered IElement classes.
     *
     *  @param  os      output stream
     */
    public static void testRegistration(PrintWriter os)  {
        os.println("Total number of registered IElement classes: " + getRegisteredCount() );
        for(String type : getRegisteredTypes()) {
            os.println("  Class " + s_mapClass.get(type).getName() + " is registered under " + type);
        }
     }
    
    /**
     *  Test element creation mechanism
     */
     public static void testCreation(PrintWriter os, String strType)  {
         
        try    {
            IElement elem = createIElement(strType);
         
            os.println("Class creation successful.");
            if ( elem instanceof Element )
                ((Element)elem).print(os);
         
        } catch (ClassNotFoundException e) {
            os.println("Class creation failed.");
            os.println("ClassNotFoundException occured.");
            os.println(e.getMessage());
             
        } catch (DataFormatException e) {
            os.println("Class creation failed.");
            os.println("DataFormatException occured.");
            os.println(e.getMessage());
            
        } catch (NumberFormatException e) {
            os.println("Class creation failed.");
            os.println("NumberFormatException occured.");
            os.println(e.getMessage());
             
        } catch (InstantiationException e) {
            os.println("Class creation failed.");
            os.println("InstantiationException occured.");
            os.println(e.getMessage());
             
        }
    }
     
    
    /** 
     *  Private constructor.  This is a utility class and no instance of this class should
     *  every be created.
     */
    private ElementFactory() {};
    
}
