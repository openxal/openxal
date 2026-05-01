package xal.extension.widgets.plot;

import java.text.*;

/**
 *  This is subclass of grid limits class (SmartFormatGridLimits ) with
 *  non-empty setSmartLimitsX() and setSmartLimitsY() methods. These methods
 *  will redefine format and limits to beautify the axises markers
 *
 *@author     shishlo
 *@version    1.0
 */
public class SmartGridLimits extends SmartFormatGridLimits {

    /**  Constructor for the SmartGridLimits object */
    public SmartGridLimits() {
        super();
    }


    /**  Sets the limits by using smart procedure for x-axis */
    public void setSmartLimitsX() {
        calculateScalesAndLimitsX();
        if ( !sucessX ) {
            return;
        }
        //we do not need extra digits
        setNumberFormatX( getSmartFormat( xSR, 0 ) );
    }


    /**  Sets the limits by using smart procedure for x-axis */
    public void setSmartLimitsY() {
        calculateScalesAndLimitsY();
        if ( !sucessY ) {
            return;
        }
        //we do not need extra digits
        setNumberFormatY( getSmartFormat( ySR, 0 ) );
    }


    /**  Calculates smart limits for the X axis and sets these limits to GridLimits */
    protected void calculateScalesAndLimitsX() {
        super.calculateScalesAndLimitsX();
        if ( !sucessX ) {
            return;
        }
        //the place to put x_min, x_max specs for GridLimits object
        int n = (int) ( Math.round( ( xSR[2] - xSR[1] ) / xSR[0] ) );
        //System.out.println( "debug X smart n=" + n +" Xmin="+xSR[1]+" Xmax="+ xSR[2]+" step="+xSR[0]);
        setLimitsAndTicksX( xSR[1], xSR[0], n );

    }


    /**  Calculates smart limits for the Y axis and sets these limits to GridLimits */
    protected void calculateScalesAndLimitsY() {
        super.calculateScalesAndLimitsY();
        if ( !sucessY ) {
            return;
        }
        //the place to put y_min, y_max specs for GridLimits objec
        int n = (int) ( Math.round( ( ySR[2] - ySR[1] ) / ySR[0] ) );
        //System.out.println( "debug Y smart n=" + n +" Ymin="+ySR[1]+" Ymax="+ ySR[2]+" step="+ySR[0]);
        setLimitsAndTicksY( ySR[1], ySR[0], n );
    }

}
