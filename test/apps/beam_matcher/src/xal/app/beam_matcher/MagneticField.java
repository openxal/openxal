package xal.app.beam_matcher;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.AcceleratorNode;
import xal.smf.impl.Quadrupole;

import java.util.ArrayList;
import java.util.List;

public class MagneticField {
    AcceleratorHardware hware = new AcceleratorHardware();
    
    
    
    
    /**
     * @return list of magnet values on the HEBT1
     * @author Christopher K Allen
     * @author Eric Dai
     * @author Matthew Reat
     * @author Frank Cui
     * Class for finding magnet values on the HEBT1.
     */
    
    public ArrayList<Double> BFields(){
        List<AcceleratorNode> quads = hware.getAllQuadrupoles("HEBT1");
        ArrayList<Double> BFieldValues = new ArrayList<Double>();
        for (AcceleratorNode quad:quads) {
            Quadrupole cat = (Quadrupole)quad;
            
            try {
                double BFieldValue = cat.getField();
                
                BFieldValues.add(BFieldValue);
            }
            
            catch (ConnectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (GetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return BFieldValues;
        
    }
}
