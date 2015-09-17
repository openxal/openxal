package xal.app.emittanceanalysis.analysis;

import java.util.*;

import xal.extension.widgets.plot.*;
import xal.app.emittanceanalysis.phasespaceanalysis.*;

/**
 *  The set of static methods for different type of emittance calculations
 *
 *@author     shishlo
 *@version    1.0
 */
class EmtCalculations {

    private EmtCalculations() { }


    /**
     *  Returns the array with fraction, emittance, alpha, beta, and gamma
     *  values for specified threshold
     *
     *@param  threshold  The threshold value in [%]
     *@param  csd        The emittance3D data object
     *@return            The array with fraction, emittance, alpha, beta, and
     *      gamma values for specified threshold
     */
    public static double[] getFracEmtAlphaBetaGamma(
        double threshold,
        ColorSurfaceData csd ) {

        if ( csd == null ) {
            return null;
        }

        double z_gr = threshold * 0.01 * csd.getMaxZ();

        int nX = csd.getSizeX();
        int nY = csd.getSizeY();

        if ( nX * nY <= 0 ) {
            return null;
        }

        double z = 0.;
        double x = 0.;
        double xp = 0.;

        double sum = 0.;
        double sum_x = 0.;
        double sum_xp = 0.;

        for ( int i = 0; i < nX; i++ ) {
            x = csd.getX( i );
            for ( int j = 0; j < nY; j++ ) {
                z = csd.getValue( i, j );
                xp = csd.getY( j );
                if ( z >= z_gr ) {
                    sum += z;
                    sum_x += x * z;
                    sum_xp += xp * z;
                }
            }
        }

        if ( sum <= 0. ) {
            return null;
        }

        sum_x /= sum;
        sum_xp /= sum;

        sum = 0.;
        double sum_total = 0.;
        double sum_x2 = 0.;
        double sum_xp2 = 0.;
        double sum_x_xp = 0.;

        for ( int i = 0; i < nX; i++ ) {
            x = csd.getX( i );
            for ( int j = 0; j < nY; j++ ) {
                z = csd.getValue( i, j );
                xp = csd.getY( j );
                if ( z >= z_gr ) {
                    sum += z;
                    sum_x2 += ( x - sum_x ) * ( x - sum_x ) * z;
                    sum_xp2 += ( xp - sum_xp ) * ( xp - sum_xp ) * z;
                    sum_x_xp += ( x - sum_x ) * ( xp - sum_xp ) * z;
                }
                sum_total += z;
            }
        }

	sum = Math.abs(sum);
	sum_total = Math.abs(sum_total);
	
        if ( sum == 0. || sum_total == 0. ) {
            return null;
        }

        sum_x2 /= sum;
        sum_xp2 /= sum;
        sum_x_xp /= sum;

        double emt = sum_x2 * sum_xp2 - sum_x_xp * sum_x_xp;

        if ( emt < 0. ) {
            emt = -Math.sqrt( -emt );
        }
        else {
            emt = Math.sqrt( emt );
        }

        if ( emt == 0. ) {
            return null;
        }

        double[] result = new double[5];

        //emittance, alpha, beta, gamma
        result[1] = emt;
        result[2] = -sum_x_xp / emt;
        result[3] = sum_x2 / emt;
        result[4] = sum_xp2 / emt;

        //fraction
        result[0] = 100.0 * sum / sum_total;

        return result;
    }


    /**
     *  Returns the array with average x and xp values for specified threshold
     *
     *@param  threshold  The threshold in [%]
     *@param  csd        The emittance3D data object
     *@return            The array with average x and xp values
     */
    public static double[] getAvgXandXP(
        double threshold,
        ColorSurfaceData csd ) {
        double z_gr = threshold * 0.01 * csd.getMaxZ();

        if ( csd == null ) {
            return null;
        }

        int nX = csd.getSizeX();
        int nY = csd.getSizeY();

        if ( nX * nY <= 0 ) {
            return null;
        }

        double z = 0.;
        double x = 0.;
        double xp = 0.;

        double sum = 0.;
        double sum_x = 0.;
        double sum_xp = 0.;

        for ( int i = 0; i < nX; i++ ) {
            x = csd.getX( i );
            for ( int j = 0; j < nY; j++ ) {
                z = csd.getValue( i, j );
                xp = csd.getY( j );
                if ( z >= z_gr ) {
                    sum += z;
                    sum_x += x * z;
                    sum_xp += xp * z;
                }
            }
        }
	
	sum = Math.abs(sum);
        if ( sum == 0. ) {
            return null;
        }

        sum_x /= sum;
        sum_xp /= sum;

        double[] result = new double[2];

        //average x and xp
        result[0] = sum_x;
        result[1] = sum_xp;

        return result;
    }


    /**
     *  Returns the array with fraction, alpha, beta, and gamma values for
     *  specified emittance ellipse
     *
     *@param  emittanceEquation  The emittance ellipse
     *@param  csd                The emittance3D data object
     *@param  threshold          Description of the Parameter
     *@param  sum_x              Description of the Parameter
     *@param  sum_xp             Description of the Parameter
     *@return                    The array with fraction, alpha, beta, and gamma
     *      values for specified emittance ellipse
     */
    public static double[] getFracEmtAlphaBetaGamma(
        double threshold,
        double sum_x,
        double sum_xp,
        EmittanceEquation emittanceEquation,
        ColorSurfaceData csd ) {

        double z_gr = threshold * 0.01 * csd.getMaxZ();

        if ( csd == null ) {
            return null;
        }

        int nX = csd.getSizeX();
        int nY = csd.getSizeY();

        if ( nX * nY <= 0 ) {
            return null;
        }

        double z = 0.;
        double x = 0.;
        double xp = 0.;

        double sum_total = 0.;
        double sum = 0.;
        double sum_x2 = 0.;
        double sum_xp2 = 0.;
        double sum_x_xp = 0.;

        for ( int i = 0; i < nX; i++ ) {
            x = csd.getX( i );
            for ( int j = 0; j < nY; j++ ) {
                z = csd.getValue( i, j );
                xp = csd.getY( j );
                if ( z >= z_gr ) {
                    if ( emittanceEquation.isInside( x - sum_x, xp - sum_xp ) ) {
                        sum += z;
                        sum_x2 += ( x - sum_x ) * ( x - sum_x ) * z;
                        sum_xp2 += ( xp - sum_xp ) * ( xp - sum_xp ) * z;
                        sum_x_xp += ( x - sum_x ) * ( xp - sum_xp ) * z;
                    }
                    sum_total += z;
                }
            }
        }

	sum = Math.abs(sum);
	sum_total = Math.abs(sum_total);
	
        if ( sum == 0. || sum_total == 0. ) {
            return null;
        }	

        sum_x2 /= sum;
        sum_xp2 /= sum;
        sum_x_xp /= sum;

        double emt = sum_x2 * sum_xp2 - sum_x_xp * sum_x_xp;

        if ( emt < 0. ) {
            emt = -Math.sqrt( -emt );
        }
        else {
            emt = Math.sqrt( emt );
        }

        if ( emt == 0. ) {
            return null;
        }

        double[] result = new double[4];

        //fraction, alpha, beta, gamma
        result[0] = 100.0 * sum / sum_total;
        result[1] = -sum_x_xp / emt;
        result[2] = sum_x2 / emt;
        result[3] = sum_xp2 / emt;

        return result;
    }

}
