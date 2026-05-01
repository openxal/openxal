package xal.app.beam_matcher;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.AcceleratorNode;
import xal.smf.impl.ProfileMonitor;
import xal.smf.impl.WireScanner;
import java.util.ArrayList;
import java.util.List;

/**
 * Returns all the wire scanner values in the given accelerator
 * sequence.
 *
 * @param
 *
 * @return              list of wire scanner values
 *
 * @since  Jun 25, 2011
 *
 * @author Eric Dai
 * @author Matthew Reat
 * @author Frank Cui
 */

public class WireScanData {
    AcceleratorHardware hware = new AcceleratorHardware();
    /**
     * Gets DSigmaF for select Profile Monitors in HEBT1
     *
     * @return	DSigmaF for select profile monitors in HEBT1
     * @throws ConnectionException
     * @throws GetException
     */
    public ArrayList<Double> wireScanDiag() throws ConnectionException, GetException {
        List <AcceleratorNode> lstws = hware.getWireScanners("HEBT1");
        ArrayList<Double> Alstws = new ArrayList<Double>();
        
        for (AcceleratorNode ws:lstws) {
            
            
            if (ws instanceof ProfileMonitor) {
                
                ProfileMonitor profMon = (ProfileMonitor)ws;
                
                double DiagLength = profMon.getDSigmaF();
                
                Alstws.add(DiagLength);
            }
        }
        
        return Alstws;
    }
    
    /**
     * Gets HSigmaF for select Profile Monitors in HEBT1
     *
     * @return	HSigmaF for select profile monitors in HEBT1
     * @throws ConnectionException
     * @throws GetException
     */
    public ArrayList<Double> wireScanHoriz() throws ConnectionException, GetException {
        List <AcceleratorNode> lstws = hware.getWireScanners("HEBT1");
        ArrayList<Double> Alstws = new ArrayList<Double>();
        
        for (AcceleratorNode ws:lstws) {
            
            
            if (ws instanceof ProfileMonitor) {
                
                ProfileMonitor profMon = (ProfileMonitor)ws;
                
                double HorizLength = profMon.getHSigmaF();
                
                Alstws.add(HorizLength);
            }
        }
        
        return Alstws;
    }
    
    /**
     * Gets VSigmaF for select Profile Monitors in HEBT1
     *
     * @return	VSigmaF for select profile monitors in HEBT1
     * @throws ConnectionException
     * @throws GetException
     */
    public ArrayList<Double> wireScanVert() throws ConnectionException, GetException {
        List <AcceleratorNode> lstws = hware.getWireScanners("HEBT1");
        ArrayList<Double> Alstws = new ArrayList<Double>();
        
        for (AcceleratorNode ws:lstws) {
            
            if (ws instanceof ProfileMonitor) {
                
                ProfileMonitor profMon = (ProfileMonitor)ws;
                
                double VertLength = profMon.getVSigmaF();
                
                Alstws.add(VertLength);
            }
        }
        
        return Alstws;
    }
    /**
     * Gets the RMS beam size in the vertical plane through XAL.
     * @param WS_ID is a String containing a wirescanner name e.g. HEBT_Diag:WS01
     * @return vertical RMS beam size value.
     * @throws ConnectionException
     * @throws GetException
     */
    public Double wireScanVertByName(String WS_ID) throws ConnectionException, GetException {
        
        AcceleratorNode ws = hware.getWireScannerByName(WS_ID);
        
        double vertRMS = 0.0;
        
        WireScanner wScanner = (WireScanner)ws;
        
        WireScanner.GaussFitAttrSet sigGauss = WireScanner.GaussFitAttrSet.acquire(wScanner);
        
        vertRMS = sigGauss.ver.stdev;
        
        return vertRMS;
    }
    
    /**
     * Gets the RMS beam size in the horizontal plane through XAL.
     * @param WS_ID is a String containing a wirescanner name e.g. HEBT_Diag:WS01
     * @return horizontal RMS beam size value.
     * @throws ConnectionException
     * @throws GetException
     */
    public Double wireScanHorizByName(String WS_ID) throws ConnectionException, GetException {
        
        AcceleratorNode ws = hware.getWireScannerByName(WS_ID);
        
        double horizRMS = 0.0;
        
        WireScanner wScanner = (WireScanner)ws;
        
        WireScanner.GaussFitAttrSet sigGauss = WireScanner.GaussFitAttrSet.acquire(wScanner);
        
        horizRMS = sigGauss.hor.stdev;
        
        return horizRMS;
    }
    
    /**
     * Gets the RMS beam size in the vertical plane using Channel Access.
     * @param WS_ID is a String containing a wirescanner name e.g. HEBT_Diag:WS01
     * @return vertical RMS beam size value.
     * @throws ConnectionException
     * @throws GetException
     */
    public Double wireScanVertByNameCA(String WS_ID) throws ConnectionException, GetException {
        
        Channel wschannel = ChannelFactory.defaultFactory().getChannel(WS_ID + ":Ver_Sigma_gs");
        
        wschannel.connectAndWait();
        
        double vertRMS = wschannel.getValDbl();
        
        return vertRMS;
    }
    
    /**
     * Gets the RMS beam size in the horizontal plane using Channel Access.
     * @param WS_ID is a String containing a wirescanner name e.g. HEBT_Diag:WS01
     * @return horizontal RMS beam size value.
     * @throws ConnectionException
     * @throws GetException
     */
    
    public Double wireScanHorizByNameCA(String WS_ID) throws ConnectionException, GetException {
        
        Channel wschannel = ChannelFactory.defaultFactory().getChannel(WS_ID + ":Hor_Sigma_gs");
        
        wschannel.connectAndWait();
        
        double horizRMS = wschannel.getValDbl();
        
        return horizRMS;
    }
}
