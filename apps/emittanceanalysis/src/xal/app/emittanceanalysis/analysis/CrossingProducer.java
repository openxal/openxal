package xal.app.emittanceanalysis.analysis;

import xal.extension.widgets.plot.*;

class CrossingProducer {

    private CrossingProducer() { }


    static double[] getRotatedLimits( double phi,
        double xc,
        double xpc,
        ColorSurfaceData emittance3Da ) {

        double xMin = emittance3Da.getMinX();
        double xMax = emittance3Da.getMaxX();

        double yMin = emittance3Da.getMinY();
        double yMax = emittance3Da.getMaxY();

        double scaleX = ( xMax - xMin );
        double scaleY = ( yMax - yMin );

        double coefX = scaleX * Math.sin( Math.PI * phi / 360. );
        double coefY = scaleY * Math.cos( Math.PI * phi / 360. );

        //top right corner

        double[] t = new double[4];

        t[0] = Double.MAX_VALUE;
        if ( coefY != 0. ) {
            t[0] = ( yMax - xpc ) / coefY;
        }

        t[1] = Double.MAX_VALUE;
        if ( coefX != 0. ) {
            t[1] = ( xMax - xc ) / coefX;
        }

        t[2] = Double.MAX_VALUE;
        if ( coefY != 0. ) {
            t[2] = ( yMin - xpc ) / coefY;
        }

        t[3] = Double.MAX_VALUE;
        if ( coefX != 0. ) {
            t[3] = ( xMin - xc ) / coefX;
        }

        double tTR = Double.MAX_VALUE;
        int iTR = -1;

        for ( int i = 0; i < 4; i++ ) {
            if ( Math.abs( tTR ) > Math.abs( t[i] ) ) {
                iTR = i;
                tTR = t[i];
            }
        }

        double tBL = Double.MAX_VALUE;
        int iBL = -1;

        for ( int i = 0; i < 4; i++ ) {
            if ( tTR * t[i] < 0. && Math.abs( tBL ) > Math.abs( t[i] ) ) {
                iBL = i;
                tBL = t[i];
            }
        }

        if ( tBL > 0. ) {
            double tmp = tBL;
            tBL = tTR;
            tTR = tmp;
        }

        double[] arr = new double[4];
        arr[0] = xc + coefX * tBL;
        arr[1] = xpc + coefY * tBL;
        arr[2] = xc + coefX * tTR;
        arr[3] = xpc + coefY * tTR;

        return arr;
    }
    
    
    //there is no scales phi in degree
     static double[] getRealRotatedLimits( double phi,
        double xc,
        double xpc,
        ColorSurfaceData emittance3Da ) {

        double xMin = emittance3Da.getMinX();
        double xMax = emittance3Da.getMaxX();

        double yMin = emittance3Da.getMinY();
        double yMax = emittance3Da.getMaxY();

        double coefX = Math.sin( Math.PI * phi / 180. );
        double coefY = Math.cos( Math.PI * phi / 180. );

        //top right corner

        double[] t = new double[4];

        t[0] = Double.MAX_VALUE;
        if ( coefY != 0. ) {
            t[0] = ( yMax - xpc ) / coefY;
        }

        t[1] = Double.MAX_VALUE;
        if ( coefX != 0. ) {
            t[1] = ( xMax - xc ) / coefX;
        }

        t[2] = Double.MAX_VALUE;
        if ( coefY != 0. ) {
            t[2] = ( yMin - xpc ) / coefY;
        }

        t[3] = Double.MAX_VALUE;
        if ( coefX != 0. ) {
            t[3] = ( xMin - xc ) / coefX;
        }

        double tTR = Double.MAX_VALUE;
        int iTR = -1;

        for ( int i = 0; i < 4; i++ ) {
            if ( Math.abs( tTR ) > Math.abs( t[i] ) ) {
                iTR = i;
                tTR = t[i];
            }
        }

        double tBL = Double.MAX_VALUE;
        int iBL = -1;

        for ( int i = 0; i < 4; i++ ) {
            if ( tTR * t[i] < 0. && Math.abs( tBL ) > Math.abs( t[i] ) ) {
                iBL = i;
                tBL = t[i];
            }
        }

        if ( tBL > 0. ) {
            double tmp = tBL;
            tBL = tTR;
            tTR = tmp;
        }

        double[] arr = new double[4];
        arr[0] = xc + coefX * tBL;
        arr[1] = xpc + coefY * tBL;
        arr[2] = xc + coefX * tTR;
        arr[3] = xpc + coefY * tTR;

        return arr;
    }   
    
    
    
}
