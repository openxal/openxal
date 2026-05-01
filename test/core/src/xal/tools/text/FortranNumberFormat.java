/*
 *  FortranNumberFormat.java
 *
 *  Created at August 25, 2004
 */
package xal.tools.text;

import java.util.regex.*;
import java.text.*;

/**
 * @deprecated due to compatibility problems with Java 8. The advertised formatting is not satisfied under Java 8. Use ScientificNumberFormat instead.
 * This class is around temporarily for backward compatibility but will be removed in the near future.
 *  FortranNumberFormat is the subclass of the DecimalFormat class. It formats
 *  the double numbers and integers according to GN.F format specification in
 *  the FORTRAN programming language. Usually N = F + 7, because an exponential
 *  representation needs additional symbols for signs of the value and the power
 *  of ten, E symbol, a decimal point, and the value of power. For instance,
 *  -1.23E-101. If user will choose N < F + 7 the formatting still will work,
 *  but the length of the resulting string may be more that N. The property
 *  fixedLength can be used to fill out the resulting string with leading
 *  spaces. This property can be set with <code> setFixedLength </code>
 *  method or by using a proper constructor
 *
 *@author     shishlo
 *@version    1.0
 */
@Deprecated
public class FortranNumberFormat extends DecimalFormat {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    

    private int sgnDigits = 3;

    private int srtLength = 10;

    private boolean fixedLength = false;

    private String pattern = "G10.3";

    /**  The simple formats */
    protected static DecimalFormat[] simpleFormats = new DecimalFormat[15];

    /**  The scientific formats */
    protected static DecimalFormat[] scientificFormats = new DecimalFormat[15];

    static {
        simpleFormats[0] = new DecimalFormat( "#0" );
        simpleFormats[1] = new DecimalFormat( "#0.#" );
        simpleFormats[2] = new DecimalFormat( "#0.##" );
        simpleFormats[3] = new DecimalFormat( "#0.###" );
        simpleFormats[4] = new DecimalFormat( "#0.####" );
        simpleFormats[5] = new DecimalFormat( "#0.#####" );
        simpleFormats[6] = new DecimalFormat( "#0.######" );
        simpleFormats[7] = new DecimalFormat( "#0.#######" );
        simpleFormats[8] = new DecimalFormat( "#0.########" );
        simpleFormats[9] = new DecimalFormat( "#0.#########" );
        simpleFormats[10] = new DecimalFormat( "#0.##########" );
        simpleFormats[11] = new DecimalFormat( "#0.###########" );
        simpleFormats[12] = new DecimalFormat( "#0.############" );
        simpleFormats[13] = new DecimalFormat( "#0.#############" );
        simpleFormats[14] = new DecimalFormat( "#0.##############" );

        scientificFormats[0] = new DecimalFormat( "#.E0" );
        scientificFormats[1] = new DecimalFormat( "#.#E0" );
        scientificFormats[2] = new DecimalFormat( "#.##E0" );
        scientificFormats[3] = new DecimalFormat( "#.###E0" );
        scientificFormats[4] = new DecimalFormat( "#.####E0" );
        scientificFormats[5] = new DecimalFormat( "#.#####E0" );
        scientificFormats[6] = new DecimalFormat( "#.######E0" );
        scientificFormats[7] = new DecimalFormat( "#.#######E0" );
        scientificFormats[8] = new DecimalFormat( "#.########E0" );
        scientificFormats[9] = new DecimalFormat( "#.#########E0" );
        scientificFormats[10] = new DecimalFormat( "#.##########E0" );
        scientificFormats[11] = new DecimalFormat( "#.###########E0" );
        scientificFormats[12] = new DecimalFormat( "#.############E0" );
        scientificFormats[13] = new DecimalFormat( "#.#############E0" );
        scientificFormats[14] = new DecimalFormat( "#.##############E0" );
    }


    /**  Constructor with a default format pattern G10.3 */
    public FortranNumberFormat() {
        applyPattern( pattern );
    }


    /**
     *  Constructor for the FortranNumberFormat objectwith a formatting pattern
     *  as parameter
     *
     *@param  pattern  The formatting pattern
     */
    public FortranNumberFormat( String pattern ) {
        applyPattern( pattern );
    }


    /**
     *  Constructor for the FortranNumberFormat object with a formatting pattern
     *  and the fixed length property as parameters
     *
     *@param  pattern      The formatting pattern
     *@param  fixedLength  The fixed length property
     */
    public FortranNumberFormat( String pattern, boolean fixedLength ) {
        applyPattern( pattern );
        setFixedLength( fixedLength );
    }


    /**
     *  Constructor for the FortranNumberFormat object with G10.3 formatting
     *  pattern and the fixed length property as parameter
     *
     *@param  fixedLength  The fixed length property
     */
    public FortranNumberFormat( boolean fixedLength ) {
        applyPattern( pattern );
        setFixedLength( fixedLength );
    }


    /**
     *  Returns the formatting pattern
     *
     *@return    The formatting pattern
     */
    public String toPattern() {
        return pattern;
    }


    /**
     *  Returns the formatting pattern
     *
     *@return    The formatting pattern
     */
    public String toLocalizedPattern() {
        return pattern;
    }


    /**
     *  Applys the format pattern
     *
     *@param  pattern  The format pattern
     */
    public void applyPattern( String pattern ) {
        Pattern p = Pattern.compile( "[G|g]\\d+\\.+\\d+" );
        if ( p.matcher( pattern ).matches() ) {
            p = Pattern.compile( "G|g|\\." );
            String[] res = p.split( pattern );
            if ( res != null && res.length == 3 ) {
                srtLength = Integer.parseInt( res[1] );
                int sgnDigitsLocal = Integer.parseInt( res[2] );
                if ( sgnDigitsLocal > simpleFormats.length ) {
                    sgnDigitsLocal = simpleFormats.length;
                }
                sgnDigits = sgnDigitsLocal;
                this.pattern = pattern;
            }
        }
    }


    /**
     *  Sets the fixedLength attribute of the FortranNumberFormat object. If it
     *  is true the resulting string will be filled out with right spaces
     *
     *@param  fixedLength  True or false
     */
    public void setFixedLength( boolean fixedLength ) {
        this.fixedLength = fixedLength;
    }


    /**
     *  Applys the format pattern
     *
     *@param  pattern  The format pattern
     */
    public void applyLocalizedPattern( String pattern ) {
        applyPattern( pattern );
    }


    /**
     *  Returns the number of significant digits for this format
     *
     *@return    The number of significant digits for this format
     */
    public int getSignificantN() {
        return sgnDigits;
    }


    /**
     *  Returns the maximal length of the formatted number
     *
     *@return    The maximal length of the formatted number
     */
    public int getStringLength() {
        return srtLength;
    }


    /**
     *  The overridden method of the DecimalFormat that delegates formatting to
     *  the specific inner formatter
     *
     *@param  val         The double value to be formatted
     *@param  toAppendTo  Where the text is to be appended
     *@param  pos         On input: an alignment field, if desired. On output:
     *      the offsets of the alignment field
     *@return             The text that will be displayed
     */
    public StringBuffer format( double val, StringBuffer toAppendTo, FieldPosition pos ) {
        DecimalFormat df = simpleFormats[0];

        if ( val != 0. ) {
            int nP = (int) Math.floor( Math.log( Math.abs( val ) ) / Math.log( 10.0 ) );
            int nPa = Math.abs( nP );
            //System.out.println( "debug  val=" + val + " nP=" + nP + " nPa=" + nPa );

            int srtLengthLocal = srtLength;

            //one symbol for sign of the value
            if ( val < 0. ) {
                srtLengthLocal--;
            }

            if ( nP < 0 ) {
                //absolute values less than 1.0
                if ( ( nPa + sgnDigits ) < Math.min( srtLengthLocal - 1, simpleFormats.length ) ) {
                    df = simpleFormats[nPa + sgnDigits - 1];
                }
                else {
                    df = scientificFormats[sgnDigits - 1];
                }
            }
            else {
                //absolute values more than 1.0

                if ( nP >= sgnDigits ) {
                    if ( nP < srtLengthLocal ) {
                        df = simpleFormats[0];
                    }
                    else {
                        df = scientificFormats[sgnDigits - 1];
                    }
                }
                else {
                    int neg = sgnDigits - nP;
                    neg = Math.min( neg, simpleFormats.length );
                    df = simpleFormats[neg - 1];
                }
            }
        }

        StringBuffer sb = df.format( val, toAppendTo, pos );

        //fill out buffer with additinal spaces
        //if it is presribed by fixedLength = true
        if ( fixedLength == true && sb.length() < srtLength ) {
            int addSpaces = srtLength - sb.length();
            for ( int i = 0; i < addSpaces; i++ ) {
                sb.insert( 0, ' ' );
            }
        }

        return sb;
    }


    /**
     *  The overridden method of the DecimalFormat that delegates formatting to
     *  the specific inner formatter
     *
     *@param  val         The integer value to be formatted
     *@param  toAppendTo  Where the text is to be appended
     *@param  pos         On input: an alignment field, if desired. On output:
     *      the offsets of the alignment field
     *@return             The text that will be displayed
     */
    public StringBuffer format( long val, StringBuffer toAppendTo, FieldPosition pos ) {
        return format( (double) val, toAppendTo, pos );
    }


    /**
     *  The main method of the class. It is used for testing
     *
     *@param  args  The command line arguments
     */
    public static void main( String[] args ) {

        if ( args.length < 2 ) {
            System.out.println( "Usage: " +
                "java xal.tools.swing.FortranNumberFormat " +
                "<format pattern> <double value #1> ... " );
            return;
        }

        FortranNumberFormat frt = new FortranNumberFormat( args[0] );
        double val = 0;

        frt.setFixedLength( true );

        for ( int i = 1; i < args.length; i++ ) {
            val = Double.parseDouble( args[i] );
            System.out.println( "formatting pattern=" + frt.toPattern() +
                " value = " + val +
                " frmt = " + frt.format( val ) );
        }
    }
}
