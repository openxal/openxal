/**
 * Property.java
 *
 *  Created	: Jul 6, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.property;


import xal.app.pta.rscmgt.PtaResourceManager;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

/**
 * Encapsulation of an application property
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jul 6, 2010
 * @author Christopher K. Allen
 */
public class Property {

    /**
     * <p>
     * Defines the necessary operations of an enumeration class
     * representing the data in <code>Properties</code> objects.
     * </p>
     *
     * @since  Jun 30, 2009
     * @author Christopher K. Allen
     */
    public interface IProperty {
    
        /**
         * Return the value of the attribute through the
         * composite <code>PropertyValue</code> object.
         * That is, this method should be used in conjunction
         * with the methods 
         * <code>PropertyValue.as<i>Type</i>(Element)</code> 
         * where <code><i>Type</i></code> is the desired return type
         * of the attribute.
         *
         * @return  the attribute converter object  
         * 
         * @since  Jun 23, 2009
         * @author Christopher K. Allen
         */
        public Property getValue();
    
    }

    
    /**
     * Extracts the <code>{@link Property}</code> objects from the 
     * list of <code>{@link Property.IProperty}</code> property interface
     * objects (usually these are enumerations).
     *
     * @param arrIProps     array of property accessing interfaces     
     * 
     * @return              an ordered list of properties corresponding to those
     *                      described by the argument
     *
     * @author Christopher K. Allen
     * @since  Jan 13, 2011
     */
    public static List<Property> extractProperties(Property.IProperty[] arrIProps) {
        
        List<Property>  lstProps = new LinkedList<Property>();
        
        for (IProperty iprop : arrIProps)
            lstProps.add( iprop.getValue() );
        
        return lstProps;
    }
    
    
    /*
     * Private Attributes
     */
    
    /** The the property name */
    private final String                strName;
    
    /** Manager of this property */
    private final PropertiesManager       mgrProp;

    
    /*
     * Initialization
     */
    
    /**
     * <p> 
     * Constructs the properties initializing the
     * property name.
     * </p>
     * 
     * @param strName       the name of the property
     * @param mgrProp       the manager of this property
     * 
     * @since  Jun 11, 2009
     * @author Christopher K. Allen
     * 
     * 
     */
    public Property(String strName, PropertiesManager mgrProp) {
        this.strName  = strName;
        this.mgrProp  = mgrProp;
    }
    
    
    /*
     * Operations
     */
    
    
    /**
     * Sets the (persistent) value of this property to the 
     * new value.
     *
     * @param strVal    new value of the property
     * 
     * @since  Jul 9, 2010
     * @author Christopher K. Allen
     */
    public void set(String strVal) {
        this.mgrProp.setPropertyValue(this, strVal);
    }
    

    /**
     * Determines whether or not the property value
     * is null.
     *
     * @return      <code>true</code> if the value is <code>null</code>,
     *              <code>false</code> otherwise
     * 
     * @since  Dec 21, 2009
     * @author Christopher K. Allen
     */
    public boolean  isNull() {
        return (this.strName == null);
    }

    /**
     * Returns the raw name of this property.
     *
     * @return  string name of this property
     * 
     * @since  Jul 9, 2010
     * @author Christopher K. Allen
     */
    public String       getName() {
        return this.strName;
    }
    
    
    /*
     * Property Values
     */
    
    /**
     * Returns the property value in <code>String</code>format.
     *
     * @return      the value of the property in <code>String</code>format
     * 
     * @since  Jun 11, 2009
     * @author Christopher K. Allen
     */
    public String asString() {
        String  strVal = this.mgrProp.getRawPropertyValue(this.strName);
        
        return strVal;
    };
    
    /**
     * <p>
     * Returns the property value in <code>boolean</code> type
     * format.
     * </p>
     * <p>
     * Note that if the property string is any variation of the
     * case-insensitive value
     * <br/>
     * <br/>
     *   &nbsp; <tt>true</tt>
     * <br/>
     * <br/>  
     * then the value <code>true</code> is returned.  Otherwise
     * <code>false</code> is returned.
     *
     * @return      the property value in <code>boolean</code> format 
     * 
     * @since  Nov 24, 2009
     * @author Christopher K. Allen
     */
    public boolean asBoolean() {
        String      strVal = this.asString();
        
        return Boolean.parseBoolean( strVal.trim() );
    }
    
    /**
     * Returns the property value in <code>integer</code>format.
     * 
     * @return      the value of the property in <code>integer</code>format
     * 
     * @since  Jun 11, 2009
     * @author Christopher K. Allen
     * 
     * @throws NumberFormatException        the property has no double representation 
     */
    public int      asInteger() throws NumberFormatException {
        String      strVal = this.asString();
        
        return Integer.parseInt( strVal.trim() );
    };
    
    /**
     * Returns the property value in <code>double</code> format.
     *
     * @return      the value of the property in <code>double</code> format
     * 
     * @since  Jun 11, 2009
     * @author Christopher K. Allen
     * 
     * @throws NumberFormatException        the property has no <code>double</code> representation 
     */
    public double asDouble() throws NumberFormatException {
        String      strVal = this.asString();
        
        return Double.parseDouble( strVal.trim() );
    };
    
    /**
     * If the property is a series of integers separated
     * by commas, it can be parsed and returned as an array of
     * integer values.
     *
     * @return      the value of the CSV property as <code>int</code> array
     * 
     * @throws NumberFormatException  the property has no <code>int</code> representation
     *                                or is not in CSV format 
     * 
     * @since  Mar 30, 2010
     * @author Christopher K. Allen
     */
    public int[]     asIntArray() throws NumberFormatException {
        String  strPropVal = this.asString();
 
        String[]        arrTokens  = strPropVal.split(","); //$NON-NLS-1$
        int[]           arrIntVals = new int[arrTokens.length];
        int             i = 0;
        for (String strIntVal : arrTokens) {
            arrIntVals[i] = Integer.parseInt( strIntVal.trim() );
         
            i++;
        }
        
        return arrIntVals;
    }
    
    /**
     * If the property is a series of double numbers separated
     * by commas, it can be parsed and returned as an array of
     * double values.
     *
     * @return      the value of the CSV property as <code>double</code> array
     * 
     * @throws NumberFormatException  the property has no <code>double</code> representation
     *                                or is not in CSV format 
     * 
     * @since  Mar 30, 2010
     * @author Christopher K. Allen
     */
    public double[]     asDblArray() throws NumberFormatException {
        String  strPropVal = this.asString();
 
        String[]        arrTokens  = strPropVal.split(","); //$NON-NLS-1$
        double[]        arrDblVals = new double[arrTokens.length];
        int             i = 0;
        for (String strDblVal : arrTokens) {
            arrDblVals[i] = Double.parseDouble( strDblVal.trim() );
         
            i++;
        }
        
        return arrDblVals;
    }
    
    /**
     * Splits the property string using the comma character (",") as the string
     * separator.  The commas are not returned.
     *
     * @return  array of strings formed by splitting the property string at commas.
     *
     * @see #asStringArray(String)
     * 
     * @author Christopher K. Allen
     * @since  May 4, 2012
     */
    public String[]     asStringArray() {
        return this.asStringArray(",");
    }
    
    /**
     * If the property string contains the separator string (maybe just a character) 
     * provided, the string is split into an array at those locations and the resulting
     * array is returned. The separator string is not returned.
     *
     * @param strSeparator  a character or character string defining the split location of the property string
     * 
     * @return              array of strings split at the separator
     *
     * @author Christopher K. Allen
     * @since  May 4, 2012
     */
    public String[]     asStringArray(String strSeparator) {
        String  strPropVal = this.asString();
        
        String[]        arrTokens = strPropVal.split(strSeparator);
        
        return arrTokens;
    }
    
    /**
     * <p>
     * If the property has the format
     * <br/>
     * <br/>
     *  &nbsp; &nbsp; <i> key = R, G, B</i>
     * <br/>
     * <br/>
     * where <i>key</i> is the property key and
     * <i>R, G, B</i> are integers in 
     * the range 1 to 255, then the property can be
     * interpreted as a color value with <i>R, G, B</i>
     * specifying the relative intensities of
     * red, green, and blue, respectively.  
     * </p>
     *
     * @return  the RGB color corresponding to the property value  
     * 
     * @throws NumberFormatException    the values cannot be parsed as integers
     * @throws IllegalArgumentException the property is not in CSV form, or it is
     *                                  not a triple (<i>R,G,B</i>) of integers between
     *                                  1 and 255
     * 
     * @since  Mar 30, 2010
     * @author Christopher K. Allen
     */
    public Color        asColor() throws NumberFormatException, IllegalArgumentException {
        int[]        arrRgb = this.asIntArray();
        
        if (arrRgb.length != 3)
            throw new IllegalArgumentException("Must have 3 values (Red,Green,Blue) each in [0,255]."); //$NON-NLS-1$
        
        Color   clr = new Color(arrRgb[0], arrRgb[1], arrRgb[2]);
        
        return clr;
    }
    
    /**
     * <p>
     * Returns the property as a <code>{@link Font}</code> object.
     * </p>
     * <p>
     * If the property value has the format
     * <br/>
     * <br/>
     * &nbsp; &nbsp; key = <i>font_name, style_enum, font_size</i>
     * <br/>
     * <br/>
     * where <i>font_name</i> is the string descriptor of the font (recognized
     * by the system), <i>style_enum</i> is the integer-valued style constant
     * for the font (defined in the class <code>Font</code>), and <i>font_size</i>
     * is the integer-valued font size.
     * </p>
     *
     * @return  font object described by the given triplette of property values
     * 
     * @throws NumberFormatException    <i>style_enum</i> and <i>font_size</i> cannot 
     *                                  be parsed as integers
     * @throws IllegalArgumentException the property is not in CSV form, or it is
     *                                  not a triple (<i>name,style,size</i>) as described
     *                                  above
     *
     * @author Christopher K. Allen
     * @since  Jan 14, 2011
     */
    public Font     asFont() throws IllegalArgumentException, NumberFormatException {
        String  strPropVal = this.asString();
        
        String[]        arrTokens  = strPropVal.split(","); //$NON-NLS-1$

        if (arrTokens.length != 3)
            throw new IllegalArgumentException("Font must have 3 values ('font name', style const, pt size)."); //$NON-NLS-1$

        // Unpack the font properties
        String  strName  = arrTokens[0];
        int     enmStyle = Integer.parseInt( arrTokens[1].trim() );
        int     intSize  = Integer.parseInt( arrTokens[2].trim() );
        
        // Create the font
        Font        fnt = new Font(strName, enmStyle, intSize);
        
        return fnt;
    }
    
    /**
     * <p>
     * Returns the property as an <code>{@link ImageIcon}</code> object.
     * </p> 
     * <p>
     * The property should be the pathname of an image file that can be
     * converted to an <code>ImageIcon</code> object.  Such files
     * include PNG, JPG, BMP, etc. file formats.  The image file is 
     * loaded, converted, then returned as an <code>ImageIcon</code>. 
     * </p>
     *
     * @return  image created from the pathname specified by the property
     *
     * @author Christopher K. Allen
     * @since  Jan 21, 2011
     * 
     * @see ImageIcon
     */
    public ImageIcon    asIcon() {
        String  strPropVal = this.asString();
        
        ImageIcon   icnVal = PtaResourceManager.getImageIcon(strPropVal);
        
        return icnVal;
    }

    /**
     * Returns a decimal format object as if this property were the format
     * string for that object.  For example, if the string representation of this
     * property were "##0.0##" then the formatter would produce string representations
     * of numbers in decimal format such at least one significant digit is shown on 
     * either end of the decimal point, with no more than three on either side. 
     * 
     * @return  <code>NumberFormat</code> object for formatting numeric values
     * 
     * @throws IllegalArgumentException the string value of this property is not a proper format string
     *
     * @author Christopher K. Allen
     * @since  May 9, 2014
     */
    public NumberFormat      asFormat() throws IllegalArgumentException  {
        
            String          strFmt  = this.asString();
            DecimalFormat   fmtDspl = new DecimalFormat(strFmt);

            return fmtDspl;
        }


}
