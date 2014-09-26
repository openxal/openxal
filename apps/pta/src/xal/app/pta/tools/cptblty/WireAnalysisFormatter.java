/**
 * WireAnalysisFormatter.java
 *
 *  Created	: Mar 22, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.cptblty;

import xal.app.pta.MainApplication;
import xal.app.pta.daq.HarpData;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.daq.ScannerData;
import xal.app.pta.tools.analysis.SignalAnalyzer;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.impl.profile.ProfileDevice.ANGLE;
import xal.smf.impl.profile.ProfileDevice.IProfileData;
import xal.smf.impl.profile.Signal;
import xal.smf.impl.profile.SignalAttrSet;
import xal.smf.impl.profile.SignalAttrs;
import xal.smf.impl.profile.SignalSet;

/**
 * Converts the PTA measurement data into the text file
 * format of the Wirescan application.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Mar 22, 2010
 * @author Christopher K. Allen
 */
public class WireAnalysisFormatter {
    
    
    /*
     * Local Types
     */
    
    /**
     * Concrete class that allows creation of a temporary <code>SignalAttrSet</code>
     * object needed for computing signal attributes on the fly.
     *
     * @author Christopher K. Allen
     * @since  Apr 23, 2014
     */
    private static class DummyAttributes extends SignalAttrSet {

        /**
         * Default constructor for DummyAttributes.
         *
         * @author Christopher K. Allen
         * @since  Apr 23, 2014
         */
        public DummyAttributes() {
            super();
        }
        
        
    }
    
    /*
     * Local Attributes
     */

//    /** Signal analyzer used to compute RMS properties from signal data when none is available */
//    private final SignalAnalyzer    dsaRmsAttrs;
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>WireAnalysisFormatter</code> object.
     *
     *
     * @since     Mar 22, 2010
     * @author    Christopher K. Allen
     */
    public WireAnalysisFormatter() {
//        this.dsaRmsAttrs = new SignalAnalyzer();
    }
    
    
    /**
     * Write out the measurement data into a text string that
     * uses the same format as the <em>Wirescan</em> application
     * output file format.
     *
     * @param setData   measurement data to text format
     * 
     * @return  String representation of the given measurement data
     *          formatted according to the Wirescan application output file.
     * 
     * @since  Mar 22, 2010
     * @author Christopher K. Allen
     */
    public String       exportWireAnalAppFmt(MeasurementData setData) {
        StringBuffer    bufFmt = new StringBuffer();

        bufFmt.append("start time: " + setData.getTimeStamp() + "\n\n"); //$NON-NLS-1$ //$NON-NLS-2$

        // For each measurement device
        for (IProfileData datDev : setData.getDataSet()) {
            
            // Write out the device identifier
            bufFmt.append( datDev.getDeviceId() + "\n\n" ); //$NON-NLS-1$
            
            // Data is for a wire scanner
            if (datDev instanceof ScannerData)
                bufFmt.append( this.writeScanData( (ScannerData)datDev) );
            
            // Data is for a wire harp
            else if (datDev instanceof HarpData) 
                bufFmt.append( this.writeHarpData( (HarpData)datDev) );
            
            // Data is of unknown type
            else
                MainApplication.getEventLogger().logError(this.getClass(), "Uknown device data type " + datDev.getClass());

        }

        bufFmt.append("\nPVLoggerID = " + setData.getPvLoggerId()); //$NON-NLS-1$

        return bufFmt.toString();
    }
    
    
    /*
     * Support Methods 
     */

    /**
     * Takes the given wire scanner data object and creates a string
     * representation with the WireAnalysis application data format (for wire
     * scanners).
     * 
     * @param datDevice     measurement data for a single wire scanner device
     * 
     * @return              (WireAnalysis) formatted string representation 
     *
     * @author Christopher K. Allen
     * @since  May 5, 2014
     */
    private String  writeScanData(ScannerData datDevice) {
        
        StringBuffer    bufDev = new StringBuffer();
        
        // Write out the statistics of the device data
        bufDev.append( this.writeStatistics(datDevice) );

        // Get the number of data points in the scan 
        //  then write out the raw data and the fit
        int         nPts = datDevice.getDataSize();
        
        bufDev.append("Position\tX Raw\tY Raw\tZ Raw\n"); //$NON-NLS-1$
        bufDev.append("--------\t-----\t-----\t-----\n"); //$NON-NLS-1$
        bufDev.append( this.writeSignalData(nPts, datDevice.getRawData()) );

        // Write out the fit signal
        bufDev.append("\n"); //$NON-NLS-1$
        bufDev.append("Position\tX Fit\tY Fit\tZ Fit\n"); //$NON-NLS-1$
        bufDev.append("--------\t-----\t-----\t-----\n"); //$NON-NLS-1$
        bufDev.append( this.writeSignalData(nPts, datDevice.getFitData()) );

        return bufDev.toString();
    }
    
    /**
     * Produces a string representation of the given harp data set
     * which is consistent with the WireAnalysis application (i.e.,
     * can be read by...).
     * 
     * @param datDevice     the measurement data for one harp device
     * 
     * @return              formatted string of WireAnalysis readable data for given harp
     *
     * @author Christopher K. Allen
     * @since  May 5, 2014
     */
    private String  writeHarpData(HarpData datDevice) {
        
        // Write to this string buffer
        StringBuffer    bufDev = new StringBuffer();
        
        // We are going to do it for each signal set for each wire
        int         cntWires = datDevice.getDataSize();
        SignalSet   datRaw   = datDevice.getRawData();
        
        for (int iWire=0; iWire<cntWires; iWire++) {
            if ( !datDevice.isValidWire(ANGLE.HOR, iWire) ||
                 !datDevice.isValidWire(ANGLE.VER, iWire) ||
                 !datDevice.isValidWire(ANGLE.DIA, iWire)
                    )
                continue;
            
            for (ANGLE angle : ANGLE.values()) {

                // Get the signal for this projection plane of this wire and write it out 
                Signal  sigRaw = datRaw.getSignal(angle);
                
//                if ( !datDevice.isValidWire(angle, iWire) )
//                    continue;
                
                double  dblPos = sigRaw.pos[iWire];
                double  dblVal = sigRaw.val[iWire];
                        
                bufDev.append(dblPos);
                bufDev.append('\t');
                bufDev.append(dblVal);
                bufDev.append('\t');
            }
            
            // Next line for next wire
            bufDev.append('\n');
        }
        
        return bufDev.toString();
    }
    
    /**
     * Returns the statistical portion of the 
     * given device data formatted according to
     * the <em>Wirescan</em> application output
     * file format.
     *
     * @param datDev    measurement data for one profile device
     * 
     * @return  formatted string representation of the data 
     * 
     * @since  Mar 22, 2010
     * @author Christopher K. Allen
     */
    private String      writeStatistics(IProfileData      datDev) {
    
        // String buffer to build formatted results
        StringBuffer  bufFmt = new StringBuffer();
        
        // We only use data from these (sub)data structures
//        SignalAttrSet fit    = datDev.sigGauss;
//        SignalAttrSet rms    = datDev.sigStat;
        SignalAttrSet fit    = datDev.getDataAttrs();
        
//      SignalAttrSet rms    = datDev.sigStat;
        SignalAttrSet rms;
        if (datDev instanceof ScannerData) {
            ScannerData    datScan = (ScannerData)datDev;
            
            rms = datScan.getStatisticalAttributes();
            
        } else {
            
            rms = this.computeRmsSignalAttrs(datDev.getRawData());
        }
        
        
        bufFmt.append("Name\tX Fit\tX RMS\tY Fit\tY RMS\tZ Fit\tZ RMS\n"); //$NON-NLS-1$
        bufFmt.append("-------\t-----\t-----\t-----\t-----\t-----\t-----\n"); //$NON-NLS-1$

        bufFmt.append("Area\t");  //$NON-NLS-1$
          bufFmt.append(fit.ver.area + "\t" + rms.ver.area + "\t");  //$NON-NLS-1$ //$NON-NLS-2$
          bufFmt.append(fit.dia.area + "\t" + rms.dia.area + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
          bufFmt.append(fit.hor.area + "\t" + rms.hor.area + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
          
        bufFmt.append("Ampl\t");  //$NON-NLS-1$
          bufFmt.append(fit.ver.amp + "\t" + rms.ver.amp + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
          bufFmt.append(fit.dia.amp + "\t" + rms.dia.amp + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
          bufFmt.append(fit.hor.amp + "\t" + rms.hor.amp + "\n");  //$NON-NLS-1$ //$NON-NLS-2$
          
        bufFmt.append("Mean\t"); //$NON-NLS-1$
          bufFmt.append(fit.ver.mean + "\t" + rms.ver.mean + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
          bufFmt.append(fit.dia.mean + "\t" + rms.dia.mean + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
          bufFmt.append(fit.hor.mean + "\t" + rms.hor.mean + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
          
        bufFmt.append("Sigma\t"); //$NON-NLS-1$
          bufFmt.append(fit.ver.stdev + "\t" + rms.ver.stdev + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
          bufFmt.append(fit.dia.stdev + "\t" + rms.dia.stdev + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
          bufFmt.append(fit.hor.stdev + "\t" + rms.hor.stdev + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
          
        bufFmt.append("Offset\t"); //$NON-NLS-1$
          bufFmt.append(fit.ver.offset + "\t" + rms.ver.offset + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
          bufFmt.append(fit.dia.offset + "\t" + rms.dia.offset + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
          bufFmt.append(fit.hor.offset + "\t" + rms.hor.offset + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
          
        bufFmt.append("Slope\t");  // This field is not a part of the new data //$NON-NLS-1$
          bufFmt.append("0.0" + "\t" + "0.0" +"\t"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          bufFmt.append("0.0" + "\t" + "0.0" +"\t"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          bufFmt.append("0.0" + "\t" + "0.0" +"\n\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        
        
        
        return bufFmt.toString();
    }
    
    /**
     * Writes out the measurement data in the (schizophrenic)
     * format of Wirescan.
     *
     * @param nPts      number of samples taken
     * @param data      the actual scanner data
     * 
     * @return  a formatted string
     * 
     * @since  Mar 22, 2010
     * @author Christopher K. Allen
     */
    private String writeSignalData(int nPts, SignalSet data) {
        
        // String buffer to build formatted results
        StringBuffer  bufFmt = new StringBuffer();
     
        for (int j = 0; j < nPts; j++) {
            // warning: we should use the position array for diagonal data as "raw" position.
            double      pos  = data.dia.pos[j];

            double      xpos = data.hor.pos[j];
            double      ypos = data.ver.pos[j];
            double      zpos = data.dia.pos[j];
            
            double      x    = data.hor.val[j];
            double      y    = data.ver.val[j];
            double      z    = data.dia.val[j];
            
            bufFmt.append(pos + "\t\t" + y + "\t" + z + "\t" + x + "\t" + xpos + "\t" + ypos + "\t" + zpos + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    }                       
        
        return bufFmt.toString();
    }
    
    /**
     * Computes the RMS attributes of the given set of signals.
     * 
     * @param setSignals    target signal set
     * 
     * @return              computed RMS quantities for the given signals
     *
     * @author Christopher K. Allen
     * @since  Apr 23, 2014
     */
    private SignalAttrSet   computeRmsSignalAttrs(SignalSet setSignals) {
        
        SignalAttrSet   setAttrs = new DummyAttributes();
        
        for (ProfileDevice.ANGLE ang : ProfileDevice.ANGLE.values()) {
            Signal          sigPlane = setSignals.getSignal(ang);
            SignalAnalyzer  dsaPlane = new SignalAnalyzer(sigPlane);
            SignalAttrs     attPlane = dsaPlane.getRmsSignalAttrs();
            
            setAttrs.setSignalAttrs(ang, attPlane);
        }
        
        return setAttrs;
    }
}
