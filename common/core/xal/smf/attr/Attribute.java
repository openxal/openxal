package xal.smf.attr;

/*
 * Attribute.java
 *
 * Created on September 10, 2001, 4:42 PM
 */

/**
 *
 * @author  CKAllen
 * @version 
 */


import  java.util.StringTokenizer;
import java.lang.reflect.*;
import java.util.*;
import xal.tools.StringJoiner;


public final class Attribute extends Object implements java.io.Serializable {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    
    /*
     *  Constants
     */
    
    public static String[] s_arrTypeNames = {   "Unknown",      // 0
                                                "Boolean",      // 1
                                                "Character",    // 2
                                                "Byte",         // 3
                                                "Short",        // 4
                                                "Integer",      // 5
                                                "Long",         // 6
                                                "Float",        // 7
                                                "Double",       // 8
                                                "String",       // 9
                                                "",                // 10
                                                "Array-Boolean",    // 11
                                                "Array-Character",  // 12
                                                "Array-Byte",       // 13
                                                "Array-Short",      // 14
                                                "Array-Integer",    // 15
                                                "Array-Long",       // 16
                                                "Array-Float",      // 17
                                                "Array-Double",     // 18
                                                "Array-String"      // 19
                            };
                            
                            
    public static final int     iUnknown        = 0;
    public static final int     iBoolean        = 1;
    public static final int     iCharacter      = 2;
    public static final int     iByte           = 3;
    public static final int     iShort          = 4;
    public static final int     iInteger        = 5;
    public static final int     iLong           = 6;
    public static final int     iFloat          = 7;
    public static final int     iDouble         = 8;
    public static final int     iString         = 9;
    public static final int     iArrBol         = 11;
    public static final int     iArrChr         = 12;
    public static final int     iArrByte        = 13;
    public static final int     iArrShr         = 14;
    public static final int     iArrInt         = 15;
    public static final int     iArrLng         = 16;
    public static final int     iArrFlt         = 17;
    public static final int     iArrDbl         = 18;
    public static final int     iArrStr         = 19;
  
    
  
    

    /*
     *  User Interface
     */
    
    
    /** 
     *  Create new Attribute 
     *  Note that Attribute must be initially instantiated to a particular type.
     */
    public Attribute(int val)           { set(val); };
    public Attribute(long val)          { set(val); };
    public Attribute(float val)         { set(val); };
    public Attribute(double val)        { set(val); };
    public Attribute(String val)        { set(val); };
    
    public Attribute(int[] arr)         { set(arr); };
    public Attribute(long[] arr)        { set(arr); };
    public Attribute(float[] arr)       { set(arr); };
    public Attribute(double[] arr)      { set(arr); };
    public Attribute(String[] arr)      { set(arr); };

    
   
    //  Data Query Methods
    public int      getType()       { return m_intTypeId; };
    public String   getTypeString() { return s_arrTypeNames[getType()]; };
    public boolean  isArray()       { return (m_intTypeId > 10); };
    
    public Object   getObject()     { return m_objValue; };
    

    // Get Methods
    public int      getInteger()    { return ((Integer)m_objValue).intValue(); };
    public long     getLong()       { return ((Long)m_objValue).longValue(); };
    public float    getFloat()      { return ((Float)m_objValue).floatValue(); };
    public double   getDouble()     { return ((Double)m_objValue).doubleValue(); };
    public String   getString()     { return (String)m_objValue; };
    
    public int[]    getArrInt()     { return (int[])m_objValue; };
    public long[]   getArrLng()     { return (long[])m_objValue; };
    public float[]  getArrFlt()     { return (float[])m_objValue; };
    public double[] getArrDbl()     { return (double[])m_objValue; };
    public String[] getArrStr()     { return (String[])m_objValue; };
    
    
    //  Set Methods
    public void set(int newVal)         { m_objValue = new Integer(newVal); m_intTypeId = iInteger; };
    public void set(long newVal)        { m_objValue = new Long(newVal);    m_intTypeId = iLong; };
    public void set(float newVal)       { m_objValue = new Float(newVal);   m_intTypeId = iFloat; };
    public void set(double newVal)      { m_objValue = new Double(newVal);  m_intTypeId = iDouble; };
    public void set(String newVal)      { m_objValue = newVal;  m_intTypeId = iString; };
    
    public void set(int[] newArr)       { m_objValue = newArr;  m_intTypeId = iArrInt; };
    public void set(long[] newArr)      { m_objValue = newArr;  m_intTypeId = iArrLng; };
    public void set(float[] newArr)     { m_objValue = newArr;  m_intTypeId = iArrFlt; };
    public void set(double[] newArr)    { m_objValue = newArr;  m_intTypeId = iArrDbl; };
    public void set(String[] newArr)    { m_objValue = newArr;  m_intTypeId = iArrStr; };
 
    
    /** Set Attribute value from string parsing */
    public boolean parse(String strVal) throws NumberFormatException {

        // Parse string according to type
		switch (m_intTypeId)  {
		
			case iDouble:
				m_objValue = new Double(strVal);
				break;
			case iFloat:
				m_objValue = new Float(strVal);
				break;
			case iLong:
				m_objValue = new Long(strVal);
				break;
			case iInteger:
				m_objValue = new Integer(strVal);
				break;
			case iString:
				m_objValue = strVal;
				break;
			
			case iArrDbl:
				m_objValue = this.parseArrDbl(strVal);
				break;
			case iArrFlt:
				m_objValue = this.parseArrFlt(strVal);
				break;
			case iArrLng:
				m_objValue = this.parseArrLng(strVal);
				break;
			case iArrInt:
				m_objValue = this.parseArrInt(strVal);
				break;
			case iArrStr:
				m_objValue = this.parseArrStr(strVal);
				break;
			
			default:
				return false;
		}
        
        return true;
    };
        


    
    /*
     *  Local Attributes
     */
                            
    private int         m_intTypeId = iUnknown;
    private Object      m_objValue  = null;
    

    
    /*
     *  Local Support Functions
     */

    
    // Parsing Arrays
    private double[]    parseArrDbl(String strArr)  throws NumberFormatException
    {
        int                 iElem;              // index of current element
        double[]            arr;                // returned value
        StringTokenizer     tok;                // break string into array
        
        tok   = new StringTokenizer(strArr, ",");
        arr   = new double[tok.countTokens()];

        iElem = 0;
        while (tok.hasMoreTokens()) {
            arr[iElem] = Double.parseDouble(tok.nextToken());
            iElem++;
        }
        
        return arr;
    };
        
    private float[]    parseArrFlt(String strArr)  throws NumberFormatException
    {
        int                 iElem;              // index of current element
        float[]             arr;                // returned value
        StringTokenizer     tok;                // break string into array
        
        tok   = new StringTokenizer(strArr, ",");
        arr   = new float[tok.countTokens()];

        iElem = 0;
        while (tok.hasMoreTokens()) {
            arr[iElem] = Float.parseFloat(tok.nextToken());
            iElem++;
        }
        
        return arr;
    };
    
    private long[]    parseArrLng(String strArr)  throws NumberFormatException
    {
        int                 iElem;              // index of current element
        long[]              arr;                // returned value
        StringTokenizer     tok;                // break string into array
        
        tok   = new StringTokenizer(strArr, ",");
        arr   = new long[tok.countTokens()];

        iElem = 0;
        while (tok.hasMoreTokens()) {
            arr[iElem] = Long.parseLong(tok.nextToken());
            iElem++;
        }
        
        return arr;
    };
    
    private int[]    parseArrInt(String strArr)  throws NumberFormatException
    {
        int                 iElem;              // index of current element
        int[]               arr;                // returned value
        StringTokenizer     tok;                // break string into array
        
        tok   = new StringTokenizer(strArr, ",");
        arr   = new int[tok.countTokens()];

        iElem = 0;
        while (tok.hasMoreTokens()) {
            arr[iElem] = Integer.parseInt(tok.nextToken());
            iElem++;
        }
        
        return arr;
    };
    
    private String[]    parseArrStr(String strArr)  throws NumberFormatException
    {
        int                 iElem;              // index of current element
        String[]            arr;                // returned value
        StringTokenizer     tok;                // break string into array
        
        tok   = new StringTokenizer(strArr, ",");
        arr   = new String[tok.countTokens()];

        iElem = 0;
        while (tok.hasMoreTokens()) {
            arr[iElem] = tok.nextToken();
            iElem++;
        }
        
        return arr;
    };
    
    
    
    /**
     *  NOTE:
     *  Wrote these then found out they are not needed.  Keep them around for now
     */
    // Parsing Type Identification
    
    private boolean isDouble(String s)  { 
        if (!hasNumeric(s))     return false;       // must contain numeric characters
        
        if (s.indexOf('F')>0 ||                     // must not have 'f' or 'F' suffix
            s.indexOf('f')>0)   return false;
        
        if (s.indexOf('.')<0 &&                     // must contain decimal or exponent
            s.indexOf('e')<0 &&
            s.indexOf('E')<0)   return false;
        
        try {                                       // must be parsable
            Double.valueOf(s);
        } catch (NumberFormatException e)   {
            return false;
        }

        return true;
    };
    
    private boolean isFloat(String s)   {
        if (!hasNumeric(s))     return false;       // must contain numeric characters
        
        if (s.indexOf('F')<0 &&                     // must contain 'f' or 'F' suffix
            s.indexOf('f')<0)   return false;
        
        if (s.indexOf('.')<0 &&                     // must contain decimal or exponent
            s.indexOf('e')<0 &&
            s.indexOf('E')<0)   return false;

        try {                                       // must be parsable
            Float.valueOf(s);
        } catch (NumberFormatException e)   {
            return false;
        }

        return true;
    };
    
    private boolean isLong(String s)    {
        if (!hasNumeric(s))     return false;       // must contain numeric characters
        
        if (s.indexOf('L')<0 &&                     // must contain 'L' or 'l' suffix
            s.indexOf('l')<0)   return false;
        
        if (s.indexOf('.')>=0 ||                     // must not contain decimal or exponent
            s.indexOf('e')>=0 ||
            s.indexOf('E')>=0)  return false;
        
        try {                                       // must be parsable
            Long.valueOf(s);
        } catch (NumberFormatException e)   {
            return false;
        }

        return true;
    };

    private boolean isInteger(String s) {
        if (!hasNumeric(s))     return false;       // must contain numeric characters
        
        if (s.indexOf('L')>0 ||                     // must not contain 'L' or 'l' suffix
            s.indexOf('l')>0)   return false;
        
        if (s.indexOf('.')>=0 ||                     // must not contain decimal or exponent
            s.indexOf('e')>=0 ||
            s.indexOf('E')>=0)  return false;
        
        try {                                       // must be parsable
            Integer.valueOf(s);
        } catch (NumberFormatException e)   {
            return false;
        }

        return true;
    };
    
    
    
    //  Parsing Auxiliary Functions

    private boolean hasLetter(String s)    {
        int i, l = s.length();
        for (i=0; i<l; ++i)
            if (Character.getType(s.charAt(i)) == Character.LOWERCASE_LETTER ||
                Character.getType(s.charAt(i)) == Character.UPPERCASE_LETTER )
                return true;
        return false;
    };
    
    private boolean hasNumeric(String s)    {
        int i, l = s.length();
        for (i=0; i<l; ++i)
            if (Character.getType(s.charAt(i)) == Character.DECIMAL_DIGIT_NUMBER)
                return true;
        return false;
    };
    
    
    
    // method added for writing data as text - tap 3/1/2002
    public String stringValue() {
        String stringValue = "";
        StringJoiner joiner = new StringJoiner(",");
        
        try {
          switch (m_intTypeId)  {                            
            case iArrDbl:
                joiner.append((double[])m_objValue);
                stringValue = joiner.toString();
                break;
            case iArrFlt:
                joiner.append((float[])m_objValue);
                stringValue = joiner.toString();
                break;
            case iArrLng:
                joiner.append((long[])m_objValue);
                stringValue = joiner.toString();
                break;
            case iArrInt:
                joiner.append((int[])m_objValue);
                stringValue = joiner.toString();
                break;
            case iArrStr:
                joiner.append((String[])m_objValue);
                stringValue = joiner.toString();
                break;
            case iArrBol:
                joiner.append((boolean[])m_objValue);
                stringValue = joiner.toString();
                break;
            case iArrChr:
                joiner.append((char[])m_objValue);
                stringValue = joiner.toString();
                break;
            case iArrByte:
                joiner.append((byte[])m_objValue);
                stringValue = joiner.toString();
                break;
            default:
                stringValue = m_objValue.toString();
                break;
            }
        }
        catch(Exception excpt) {
            System.err.println(excpt);
            excpt.printStackTrace();
        }
                
        return stringValue;
    }
    

 };
 
 
 
 
