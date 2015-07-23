/*
 * LatticeParser.java
 *
 * Created on February 21, 2003, 10:23 AM
 */

package xal.model.xml;


import java.util.*;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;

import xal.tools.data.*;
import xal.tools.xml.*;

import xal.model.*;
import xal.model.elem.*;

/**
 *  Utility class for building an XAL Model Lattice from a corresponding XML file.
 *
 *
 * @author  Christopher Allen
 */
public class LatticeXmlParser {

    
    /*
     *  Global Attributes
     */
    
    
    /** Attributes for XAL/MODEL/LATTICE DTD */
    public static final String      s_strElemLatt   = "Lattice";
    public static final String      s_strElemSeq    = "Sequence";
    public static final String      s_strElemElem   = "Element";
    public static final String      s_strElemParam  = "Parameter";
    
    public static final String      s_strElemComm   = "comment";
    
    public static final String      s_strAttrId   = "id";
    public static final String      s_strAttrLen  = "len"; 
    
    public static final String      s_strAttrVer  = "ver";
    public static final String      s_strAttrAuth = "author";
    public static final String      s_strAttrDate = "date";
    public static final String      s_strAttrText = "text";
    
    public static final String      s_strAttrName = "name";
    public static final String      s_strAttrType = "type";
    public static final String      s_strAttrVal  = "value";
    
    public static final String      s_strAttrSep  = "|";

    

	// ********* constructors    
    
    
    /** 
     *  Creates a new instance of LatticeXmlParser 
     */
    public LatticeXmlParser() {
    }
    
    
    // ********* static parsing methods
    
    
    /**
     * Parses the XML file specified by the supplied URI.  Return a <code>
     * Lattice</code> object configured according to the file.
     * 
     * @param fileUri the URI specifying the XML file to parse
     * @param  bolValidate apply XML DTD validation
     * @return the lattice object described by the XML file
     * @exception  ParsingException An exception was encountered in parsing
     */
    public static Lattice parse(String fileUri, boolean bolValidate) 
        throws ParsingException 
    {
        LatticeXmlParser parser = new LatticeXmlParser();
        return parser.parseUrl(fileUri, bolValidate);
    }
    
    /**
     * Parses the supplied DataAdaptor and return a <code>
     * Lattice</code> object configured according to the Adaptor.
     * 
     * @param adaptor the DataAdaptor containing the Lattice definition
     * @return the lattice object described by the XML file
     * @exception  ParsingException An exception was encountered in parsing
     */
    public static Lattice parseDataAdaptor(DataAdaptor adaptor) 
    	throws ParsingException 
    {
    	LatticeXmlParser parser = new LatticeXmlParser();
    	return parser.parseAdaptor(adaptor);
    }
    
    
    // ********** instance based parsing methods
    
    
    /** 
     *  Parse an XAL Model lattice file and build the corresponding Lattice object.
     *  The lattice file can be validated by setting the DTD validation flag.  The
     *  file indicated must be a properly formated XML file.
     *
     *  @param  strFile     URL of lattice description file
     *  @param  bolValidate apply XML DTD validation
     *
     *  @return             Lattice object built according to file contents
     *
     *  @exception  ParsingException An exception was encountered in parsing
     */
    public Lattice parseUrl(String strFile, boolean bolValidate) 
        throws  ParsingException 
    {
        // Attach a data adaptor to the XML file then build the lattice from it
        XmlDataAdaptor  daptUrl = XmlDataAdaptor.adaptorForUrl(strFile, bolValidate);
        return parseAdaptor(daptUrl);
    }
    
    /**
     *  Parses the given data source for modeling lattice information
     *  and creates the corresponding lattice.
     *  
     * @param adaptor   data source containing the modeling lattice
     * @return  model lattice object created from the given data source
     * 
     * @throws ParsingException     general format exception
     *
     * @author Christopher K. Allen
     * @since  Apr 13, 2011
     */
    public Lattice parseAdaptor(DataAdaptor adaptor) throws ParsingException {
    	
        DataAdaptor     daptLat = adaptor.childAdaptor(s_strElemLatt);
        
        
        // Extract lattice attributes and set them
        String          strId   = daptLat.stringValue(s_strAttrId);     // lattice id
        String          strVer  = daptLat.stringValue(s_strAttrVer);     // lattice version
        String          strAuth = daptLat.stringValue(s_strAttrAuth);    // lattice author
        String          strDate = daptLat.stringValue(s_strAttrDate);    // lattice date

        Lattice         latUrl  = new Lattice();

        latUrl.setId(strId);
        latUrl.setVersion(strVer);
        latUrl.setAuthor(strAuth);
        latUrl.setDate(strDate);

        try {
            this.loadComposite(latUrl, daptLat);
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ParsingException(e.getMessage());
            
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new ParsingException(e.getMessage());
            
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new ParsingException(e.getMessage());
            
        }
        
        return latUrl;
    }
    
    
    
    /*
     *  Internal Support
     */
    
    
    /**
     *  Build a sequence with its elements from a data source represented by a
     *  DataAdaptor interface.  The data adaptor should be configured according
     *  to the XAL_MODEL.DTD definition.  Therefore, the elements and sub-sequences
     *  contained in this sequence need to be represented as child adaptors.
     *
     *  @param  daptSeq     data source containing sequence structure
     *
     *  @return             new ElementSeq object built according to data adaptor
     *
     *  @exception  DataFormatException     data does not conform to XAL_MODEL.DTD specification
     *  @exception  NumberFormatException   numeric parameter is malformed and unparsable
     *  @exception  ClassNotFoundException  an unknown Element type was encountered
     *  @exception  InstantiationException  unable to instantiate an IElement instance
     *  @exception  NoSuchMethodException   unknown or invalid Parameter for an Element was encountered
     */
    protected ElementSeq buildSequence(DataAdaptor daptSeq)
        throws DataFormatException, NumberFormatException, ClassNotFoundException, InstantiationException, NoSuchMethodException
    {
        // Create a new ElementSeq instance
        String      strId     = daptSeq.stringValue(s_strAttrId);    // sequence id
        ElementSeq  seqNew    = new Sector(strId);

        this.loadComposite(seqNew, daptSeq);
        
        return seqNew;
    }
    

    /**
     *  Return the comment string from a comment element within a sequence.
     *
     *  @param  daptComm    data adaptor containing comment element
     *
     *  @return             comment string
     *
     *  @exception  MissingDataException    an attribute was missing from the comment
     */
    protected String buildComment(DataAdaptor daptComm)   {
        String      strAuth;        // author of comment
        String      strDate;        // date of comment
        String      strText;        // user comments for sequence
        String      strComm;        // comment string
                
        strAuth = daptComm.stringValue(s_strAttrAuth);
        strDate = daptComm.stringValue(s_strAttrDate);
        strText = daptComm.stringValue(s_strAttrText);
                
        strComm = strAuth + s_strAttrSep + strDate + s_strAttrSep + strText;
        
        return strComm;
    }
    
    /**
     *  Build a IElement object according to parameters specified in a data
     *  adaptor.
     *
     *  @param  daptElem    data adaptor containing element parameters
     *
     *  @return             new IElement instance specified by the data adaptor
     *
     *  @exception  InstantiationException  unable to instantiate an IElement instance
     *  @exception  ClassNotFoundException  an unknown IElement type was encountered
     *  @exception  DataFormatException     bad parameter format encountered
     *  @exception  NoSuchMethodException   unknown or invalid Parameter for an IElement was encountered
     */
    protected IElement buildElement(DataAdaptor daptElem)  
        throws InstantiationException, ClassNotFoundException, DataFormatException, NoSuchMethodException
    {
//        // Create new element instance whose type is specified by the typeid
//        String      strType = attrValue(daptElem, s_strAttrType);
//        IElement    elemNew = ElementFactory.createIElement(strType);
        
        // Create a new element instance whose type is specified by its class type
        String      strType  = attrValue(daptElem, s_strAttrType);
        Class<?>    clsElem  = Class.forName(strType);
        
        Constructor<?> ctorElem = clsElem.getConstructor((Class<?>[])null); 
        IElement elemNew;
        try {
            elemNew = (IElement) ctorElem.newInstance((Object[])null);
            
        } catch (IllegalArgumentException e) {
            throw new InstantiationException("No default element contructor.");

        } catch (IllegalAccessException e) {
            throw new InstantiationException("Unable to access element constructor");
            
        } catch (InvocationTargetException e) {
            throw new InstantiationException("Unable to instantiate element.");
            
        }


        // Set the optional element attributes
        if (daptElem.hasAttribute(s_strAttrId) && elemNew instanceof Element) {
            String  strId   = daptElem.stringValue(s_strAttrId);
            ((Element)elemNew).setId(strId);
        }
//        if (daptElem.hasAttribute(s_strAttrLen)) {
//            double  dblLen = daptElem.doubleValue(s_strAttrLen);
//            ((ThickElement)elemNew).setLength(dblLen);
//        }
        
        this.loadElement(elemNew, daptElem);
        
        return elemNew;
    }

    
    /**
     *  Load an ElementSeq object with it's components
     *
     *  @param  secNew      ElementSeq object to be loaded
     *  @param  daptSeq     data adaptor containing sequence information
     *  
     *  @exception  ClassNotFoundException  an unknown Element type was encountered
     *  @exception  DataFormatException     bad parameter format 
     *  @exception  NumberFormatException   bad number format in parameter value
     *  @exception  NoSuchMethodException   unknown or invalid Parameter for an Element was encountered
     */
    protected void loadComposite(IComposite secNew, DataAdaptor daptSeq)    
        throws DataFormatException, NumberFormatException, ClassNotFoundException, NoSuchMethodException, InstantiationException
    {
        // Build the sequence from its components
        Iterator<? extends DataAdaptor> iterChild = daptSeq.childAdaptors().iterator();
        while (iterChild.hasNext()) {
            DataAdaptor     daptChild = iterChild.next();
            
            
            // Comments - Load any comments associated with sequence
            if (daptChild.name().equals(s_strElemComm))  {
                String      strComm = buildComment(daptChild);
                
            // Sequence - Load a subsequence within the sequence
            } else if (daptChild.name().equals(s_strElemSeq)) {
                ElementSeq  seqChild = buildSequence(daptChild);
        
                secNew.addChild(seqChild);
                
                
            // Element - Load an element into the sequence (a leaf node in tree)
            } else if (daptChild.name().equals(s_strElemElem))  {
                IElement elemNew = buildElement(daptChild);
                
                secNew.addChild(elemNew);
            
                
            // An error must have occurred
            } else  {
                throw new DataFormatException("LatticeParser#buildSequence() - unrecognized XML element tag " + daptChild.name());
                
            }
        }
    }
    
    /**
     *  Load an IElement object with its parameters specified in the 
     *  data adaptor.
     *
     *  @param  elem        IElement object to have parameter assigned
     *  @param  daptElem    data adaptor containing all parameter information for element
     *
     *  @exception  DataFormatException     bad paramter format
     *  @exception  NumberFormatException   numeric value was malformed and unparseable
     *  @exception  NoSuchMethodException   unknown or invalid Parameter for an Element was encountered
     */
    protected void loadElement(IElement elem, DataAdaptor daptElem) 
        throws  DataFormatException, NoSuchMethodException, NumberFormatException
    {
        
        // Iterate through all parameter elements
        Iterator<? extends DataAdaptor> iterParam = daptElem.childAdaptors(s_strElemParam).iterator();
        while (iterParam.hasNext()) {

            // Get the name, type, and value of the parameter
            DataAdaptor daptParam = iterParam.next();
            
            String  strName = daptParam.stringValue(s_strAttrName);
            String  strType = daptParam.stringValue(s_strAttrType);
            String  strValue = daptParam.stringValue(s_strAttrVal);
            
            // Use bean introspection to find and set the appropriate property
			try {
				BeanInfo bi = Introspector.getBeanInfo (elem.getClass());
				PropertyDescriptor pd[] = bi.getPropertyDescriptors();
				PropertyDescriptor property = null;
				for (int i=0;i<pd.length;i++) {
					// match a property with the same name and type
					if ((strName.equalsIgnoreCase(pd[i].getName())) &&
						(strType.equalsIgnoreCase(pd[i].getPropertyType().getName()))) {
						property = pd[i];
						break;
					}
				}
				if (property != null) {
					
					// get and invoke the set method
					Method setter = property.getWriteMethod();
					
					// make sure the property is writable
					if (setter != null) {
						
				        // Identify the parameter class and pack the value into the 
				        // appropriate object
				        Class<?>       clsParam;
				        Object      objParam;
				    
				        if (strType.equals("boolean"))  {
				            clsParam = boolean.class;
				            objParam = new Boolean(strValue);
				            
				        } else if (strType.equals("byte"))  {
				            clsParam = byte.class;
				            objParam = new Byte(strValue);
				            
				        } else if (strType.equals("int"))   {
				            clsParam = int.class;
				            objParam = new Integer(strValue);
				        
				        } else if (strType.equals("float")) {
				            clsParam = float.class;
				            objParam = new Float(strValue);
				            
				        } else if (strType.equals("double")){
				            clsParam = double.class;
				            objParam = new Double(strValue);
				            
				        } else {
				            clsParam  = Class.forName(strType);
				
				            Class<?>       arrCtorSig[] = { String.class };
				            Object      arrCtorArg[] = { strValue };
				            Constructor<?> ctorParam = clsParam.getConstructor(arrCtorSig);
				            
				            objParam  = ctorParam.newInstance( arrCtorArg ); 
				        }
						setter.invoke(elem, new Object[] {objParam} );
					}
				}
				
            } catch (NumberFormatException e)   {
                throw new NumberFormatException("LatticeParser#loadParameters() - bad parameter number format" 
                                        + strName + " for element " + elem.getId()
                                        );
                
            } catch (NoSuchMethodException e)   {
                throw new NoSuchMethodException("LatticeParser#loadParameters() - unknown parameter " 
                                       + strName + " for element " + elem.getId()
                                       );
            } catch (Exception e)  {
                throw new DataFormatException("LatticeParser#loadParameters() - unable to set parameter " 
                                        + strName + " for element " + elem.getId()
                                        );
            }
                
        }
        
    }

    
    /**
     *  Returns the attribute value string from a DataAdaptor interface.  Performs error
     *  checking in that a MissingDataException is thrown if attribute does not exist.
     *
     *  @param  strAttrName     attribute name
     *
     *  @return                 string value of attribute
     *
     *  @exception  MissingDataException     specified attribute not present in DataAdaptor
     */
    protected String    attrValue(DataAdaptor dapt, String strAttrName)  
        throws MissingDataException
    {
        if (!dapt.hasAttribute(strAttrName))
            throw new MissingDataException("LatticeParser#attrValue() - DataAdaptor does not have attribute " + strAttrName);

        return dapt.stringValue(strAttrName);
    }
    

    /**
     *  Add a parameter child to a DataAdaptor
     */
     protected static void addParameter(DataAdaptor dapt, String strName, String strType, String strValue) {
        DataAdaptor daptParam = dapt.createChild("Parameter");
        
        daptParam.setValue("name", strName);
        daptParam.setValue("type", strType);
        daptParam.setValue("value", strValue);
     }

    
    
     
    /*
     *  Testing and Debugging
     */
    
    
     
}





/*
 *  Storage
 */

        
/**
 *  Driver for LatticeXmlParser class tester.
 */
/*
public static void main(String arrArgs[])   {
        
    Lattice         lattice;
    PrintWriter     os     = new PrintWriter( System.out );
    String          strUrl = "xml/SnsMebt.lat.mod.xal.xml";
    LatticeXmlParser   parser = new LatticeXmlParser();
        
    try {
        lattice = parser.parseUrl(strUrl, false);
        
    } catch (ParsingException e)   {
        System.err.println(e.getMessage());
        return;            
    }
                
    lattice.print( os );
    os.flush();
}
*/
        
    /**
     *  Find and return the string value of a Parameter element in a data adaptor
     *  object.
     *
     *  @param  strNameParam    name of the parameter
     *  @param  daptElem        data adaptor for the element
     *  
     *  @return                 string value of the parameter
     *
     *  @exception  MissingDataException     parameter not found in data adaptor
     */
/*
    protected String getParameter(String strNameParam, DataAdaptor daptElem) 
        throws MissingDataException
    {
        Iterator    iterParam = daptElem.childAdaptorIterator("Parameter");
        while (iterParam.hasNext()) {
            DataAdaptor  daptParam = (DataAdaptor)iterParam.next();
            
            String      strName = daptParam.stringValue("name");
            String      strType = daptParam.stringValue("type");
            String      strVal  = daptParam.stringValue("value");

            if (strNameParam.equals(strName))
                return strVal;
        }
        
        throw new MissingDataException("Element#getParameter() - parameter not present : " + strNameParam);
    }
  */

    /**
     *  Set a parameter value in a data store.  Parameters are stored as child nodes in a
     *  DataAdaptor under the label "Parameter".  
     *
     *  @param  strName     the "name" attribute of parameter
     *  @param  strType     the data "type" attribrute of parameter
     *  @param  strValue    the "value" attribute of parameter
     *  @param  daptElem    data adaptor for the element 
     */
/*
    protected void  setParameter(String strName, String strType, String strValue, DataAdaptor daptElem) {
        DataAdaptor daptParam = daptElem.createChild("Parameter");

        daptParam.setValue("name", strName);
        daptParam.setValue("type", strType);
        daptParam.setValue("value", strValue);
    }
 */
        
    /**
     *  Builds an <code>ElementSeq</code> XAL modeling element including all the 
     *  leaves of the sequence. 
     *
     *  @param  daptElem    XmlDataAdaptor contiaining attributes of an Sequence tag
     *
     *  @return             IElement interface of a new ElementSeq object
     *
     *  @exception  DtdException            <b>fam</b> attribute not present in DataAdaptor
     *  @exception  NumberFormatException   bad number format in numeric attribute
     *  @exception  ClassNotFoundException  <b>type</b> value not recognized by ElementFactory
     */
   /*
    private IElement buildSequence(DataAdaptor daptSeq) 
        throws DtdException, NoSuchMethodException, NumberFormatException, ClassNotFoundException
    {
        // Get the sequence attributes
        String      strId  = daptSeq.stringValue("id");
        ElementSeq  seqNew = new ElementSeq(strId);
        
        this.loadSequence(seqNew, daptSeq);
        return (IElement)seqNew;
    }
    */
    
    /**
     *  Load a sequence with its elements.
     */
    /*
    private void loadSequence(ElementSeq seq, DataAdaptor daptSeq)
        throws DtdException, NoSuchMethodException, NumberFormatException, ClassNotFoundException
    {
        Iterator    iterChild = daptSeq.childAdaptorIterator();
        while (iterChild.hasNext()) {
            IElement        ifcNew;  
            DataAdaptor     daptChild = (DataAdaptor)iterChild.next();
            
            if (daptChild.name().equals("comment"))  {
                String      strCom;         // user comments for sequence
                break;
                
            } else if (daptChild.name().equals("Sequence")) {
                ifcNew = this.buildSequence(daptChild);
                
            } else if (daptChild.name().equals("Element"))  {
                ifcNew = this.buildElement(daptChild);
            
            } else  {
                throw new DtdException("LatticeParser#buildSequence() - unrecognized element tag " + daptChild.name());
                
            }
            
            seq.addElement(ifcNew);
        }
    }
    */
    
    /**
     *  Builds an XAL modeling element based on the information in the DataAdaptor.
     *  The DataAdaptor should contain an XML node contain the <b>Element</bd> tag
     *  of the XAL Model DTD.
     *  <p>
     *  The element may be either a <code>ThickElement</code> or a <code>ThinElement
     *  </code> derived element depending upon the value of the <b>fam</b> attribute
     *  in the DataAdaptor.
     * 
     *  @param  daptElem    DataAdaptor contiaining attributes of an Element tag
     *
     *  @return             IElement interface of a new XAL modeling element
     *
     *  @exception  DtdException            <b>fam</b> attribute not present in DataAdaptor
     *  @exception  NumberFormatException   bad number format in numeric attribute
     *  @exception  ClassNotFoundException  <b>type</b> value not recognized by ElementFactory
     */
    /*
    private IElement    buildElement(DataAdaptor daptElem)  
        throws DtdException, NoSuchMethodException, NumberFormatException, ClassNotFoundException
    {
        IElement    ifcElem;        // returned IElement object
        String      strFam;         // element family
        String      strType;        // element type
        String      strId;          // element identifier
        String      strPos;         // element lattice position
        String      strSecs;        // number of thick element subsections
        String      strLen;         // element length (ThickElement)
        
        
        // Get all the element attributes
        strFam  = daptElem.stringValue("fam");
        strType = daptElem.stringValue("type");
        strId   = daptElem.stringValue("id");
        strPos  = daptElem.stringValue("pos");
        strSecs = daptElem.stringValue("nSecs");
        strLen  = daptElem.stringValue("len");
        
        
        // Create element according to its family
        if (strFam.equals("THIN")) {
            Double  dblPos = Double.valueOf(strPos);
            
            ifcElem = (IElement)ElementFactory.createThinElement(strType, strId, dblPos);

        } else if (strFam.equals("THICK"))  {
            Integer nSecs  = Integer.valueOf(strSecs);
            Double  dblLen = Double.valueOf(strLen);
            Double  dblPos = Double.valueOf(strPos);

            ifcElem = (IElement)ElementFactory.createThickElement(strType, strId, nSecs, dblLen, dblPos);
            
        } else  {
            throw new DtdException("LatticeParser#buildElement - 'fam' attribute not found in Element " + strId);
        }
        
        // Load any parameters associated with element
        this.loadElement(ifcElem, daptElem);
        
        return ifcElem;
    }
    */
    
    
    /**
     *  Load an element with parameters.
     *
     *  @param  ifcElem     Element to be loaded with parameters
     *  @param  daptElem    data adaptor containing element parameters
     *
     *  @exception  DtdException            lattice file does not conform to XAL Model DTD
     *  @exception  NoSuchMethodException   element does not have indicated parameter
     *  @exception  SecurityException       parameter of element unaccessible
     *  @exception  NumberFormatException   bad number format in parameter value
     */
/*
    private void    loadElement(IElement ifcElem, DataAdaptor daptElem)  
        throws DtdException, NoSuchMethodException, SecurityException, NumberFormatException
    {
        Iterator    iterChild = daptElem.childAdaptorIterator();
        while (iterChild.hasNext()) {
            XmlDataAdaptor  daptParam = (XmlDataAdaptor)iterChild.next();
            
            if (daptParam.name().equals("Parameter"))  {
                String      strName = daptParam.stringValue("name");
                String      strType = daptParam.stringValue("type");
                String      strVal  = daptParam.stringValue("value");

                try {
                    // Construct the value object
                    Class       clsValue  = Class.forName(strType);
                    Constructor ctorValue = clsValue.getConstructor(new Class[] { String.class });
                    Object      objValue  = ctorValue.newInstance( new Object[] { strVal } ); 
             
                    // Set the parameter of the element
                    Class       clsElem  = ifcElem.getClass();
                    Method      mthParam = clsElem.getMethod( "set" + strName, new Class[] { clsValue } );
                    mthParam.invoke(ifcElem, new Object[] { objValue });
                
                } catch (ClassNotFoundException e)  {
                    throw new DtdException("LatticeParse#loadElement() - unknown parameter type " 
                                            + strType + " for element " + ifcElem.getId()
                                            );
                } catch (Exception e)   {
                    throw new NoSuchMethodException(e.getMessage());
                }
            }
        }
    }
*/