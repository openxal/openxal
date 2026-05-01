package xal.app.beam_matcher;


import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.model.ModelException;
import xal.model.elem.IdealMagQuad;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.proxy.ElectromagnetPropertyAccessor;

/**
 * Retrieve magnetic field values from the virtual accelerator
 * Set new magnetic field values
 *
 * @author Matthew Reat
 * @author Frank Cui
 * @author Eric Dai
 *
 * @since July 1, 2011
 */
public class ModelMagneticField {
    
    //@SuppressWarnings("unused")
    private final static String STR_ID = "HEBT1";
    //@SuppressWarnings("unused")
    private final static double multiplier = 1.05;
    
    //@SuppressWarnings("unused")
    private  Accelerator    accl;
    //@SuppressWarnings("unused")
    private  AcceleratorSeq seq;
    private  Scenario		model;
    
    private double q, dblFldNom, dblFldNew;
    
    
    
    /**
     * Default Constructor that can be used to retrieve
     * magnetic field values from the model
     *
     * @throws ModelException 		unable to retrieve model values
     */
    public ModelMagneticField() throws ModelException {
        GenDocument main;
        main = GenDocument.getInstance();
        accl = main.getAccelerator();
        model = main.getModel();
    }
    
    /**
     * Get the model magnetic field values
     *
     * @throws ModelException 		unable to retrieve model values
     * @throws SynchronizationException		failure in the server synchronization process
     * @throws GetException		get the current exception object
     * @throws ConnectionException		cannot connect to the model
     */
    public void ModelBFields() throws ModelException, SynchronizationException, GetException, ConnectionException {
        
        // Unused Method
        
    }
    
    /**
     * <p>
     * Set model magnetic field values
     * </p>
     * <p>
     * </p>
     * For an online model (type <code>Scenario</code>) labeled
     * <code>model</code>, we can set the magnetic field values
     * for all the modeling elements that represent a given
     * quadrupole <code>smfQuad</code> (say of type <code>Quadrupole</code>).
     * This is accomplished using the
     * <code>{@link Scenario#setModelInput(AcceleratorNode, String, double)}</code>
     * method.  Say we wish to set the magnetic fields for the modeling elements of
     * quadrupole <code>smfQuad</code> to the value in variable <code>dblFldVal</code>
     * (of type <code>double</code>), we use the following syntax:
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <code>model.setModelInput(smfQuad, ElectromagnetPropertyAccessor.PROPERTY_FIELD, dblFldVal);
     * <br/>
     * <br/>
     * where class <code>ElectromagnetPropertyAccessor</code> lives in the package
     * <code>gov.sns.xal.smf.proxy</code> and the class constant <code>PROPERTY_FIELD</code>
     * indicates to the magnetic field parameter of all electro-magnetics.
     * </p>
     * <p>
     * <h4>Notes: CKA</h4>
     * &middot; It would be wise to type the argument <code>smfQuad</code> to class
     * <code>Quadrupole</code> (derived from <code>AcceleratorNode</code>), since this
     * is what you want anyway.  The tighter typing will also save lives.
     * <br/>
     * &middot; This method should be about two lines in length if the method
     * <code>{@link Scenario#setModelInput(AcceleratorNode, String, double)}</code>
     * works. (Which indicates it either doesn't belong here or it isn't needed.)
     * </p>
     *
     * @param smfQuad		    key into the online model, elements modeling this hardware will be modified
     * @param dblFactor		    percentage to change the current magnet value
     *
     * @throws ModelException   unable to re-run online model after field value modification
     */
    public void changeQuadValue(AcceleratorNode smfQuad, double dblPct) throws ModelException {
 
        double dblFactor = dblPct*.01 + 1;
     
        
        //		AcceleratorHardware	acclHw = new AcceleratorHardware();
        //		List<AcceleratorNode> lstNodes = acclHw.getAllQuadrupoles(STR_ID);
        IdealMagQuad	modQuad = new IdealMagQuad();
        
        dblFldNom = modQuad.getMagField();
        dblFldNew = dblFldNom*dblFactor;
        
        model.setModelInput(smfQuad, ElectromagnetPropertyAccessor.PROPERTY_FIELD, dblFldNew);
        
        
        System.out.println(" ");
        System.out.println("Quad: " + smfQuad + " " + " changes by " + dblPct + "%");
    }
    
    public double getStepSize() {
        q = dblFldNew - dblFldNom;
        return q;
    }
    
}
