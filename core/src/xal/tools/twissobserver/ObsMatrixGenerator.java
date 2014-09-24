package xal.tools.twissobserver;

import gov.sns.tools.beam.PhaseMatrix;
import gov.sns.xal.model.ModelException;

import java.util.ArrayList;

import Jama.Matrix;


/**
 * <p>
 * Generates the observer matrix using the model generated transfer matrix and
 * user input.
 * </p>
 * @author Eric Dai
 * @author Christopher K. Allen
 * @since June 19, 2012
 *
 * @deprecated  The functionality of this class has been moved to <code>TwissObserver</code>.
 *              There was too little done here to justify a separate class.
 */
@Deprecated
public class ObsMatrixGenerator {

    
	/*
	 * Global Constants
	 */

	/** Basis vector for horizontal plane phase space */
	static final PhaseMatrix	MAT_BASIS1X 	= PhaseMatrix.zero();
    /** Basis vector for horizontal plane phase space */
	static final PhaseMatrix	MAT_BASIS2X  	= PhaseMatrix.zero();	
    /** Basis vector for horizontal plane phase space */
	static final PhaseMatrix	MAT_BASIS3X 	= PhaseMatrix.zero();
    /** Basis vector for horizontal plane phase space */
	static final PhaseMatrix 	MAT_BASIS4X 	= PhaseMatrix.zero();

    /** Basis vector for vertical plane phase space */
	static final PhaseMatrix	MAT_BASIS1Y 	= PhaseMatrix.zero();
    /** Basis vector for vertical plane phase space */
	static final PhaseMatrix	MAT_BASIS2Y  	= PhaseMatrix.zero();	
    /** Basis vector for vertical plane phase space */
	static final PhaseMatrix	MAT_BASIS3Y 	= PhaseMatrix.zero();
    /** Basis vector for vertical plane phase space */
	static final PhaseMatrix 	MAT_BASIS4Y 	= PhaseMatrix.zero();



	/*
	 * Static Initialization Block
	 */

	static {
		ObsMatrixGenerator.MAT_BASIS1X.setElem(0, 0, 1.0);				
		ObsMatrixGenerator.MAT_BASIS2X.setElem(0, 1, 1.0);
		ObsMatrixGenerator.MAT_BASIS3X.setElem(1, 0, 1.0);				
		ObsMatrixGenerator.MAT_BASIS4X.setElem(1, 1, 1.0);

		ObsMatrixGenerator.MAT_BASIS1Y.setElem(2, 2, 1.0);
		ObsMatrixGenerator.MAT_BASIS2Y.setElem(2, 3, 1.0);
		ObsMatrixGenerator.MAT_BASIS3Y.setElem(3, 2, 1.0);
		ObsMatrixGenerator.MAT_BASIS4Y.setElem(3, 3, 1.0);
	}


	/*
	 * Local Attributes
	 */

	/** Transfer matrix generation engine */
	private final TransferMatrixGenerator      genTransMat;


	
	/*
	 * Initialization
	 */
	
	/**
	 * <p>
	 * Creates instance of observer matrix generator with using the given
	 * <code>TransferMatrixGenerator</code> object.  The argument is used
	 * as the generation engine for all internally computed transfer matrices.
	 * </p>
	 * 
	 * @param genTransMat  engine for producing transfer matrices from which
	 *                     all observation matrices are computed
	 */
	public ObsMatrixGenerator(TransferMatrixGenerator genTransMat) {
		this.genTransMat = genTransMat;
	}
	

	
	/*
	 * Operations
	 */

	/**
	 * Computes the observation matrix at the entrance of the given
	 * element using the given location data for the given phase plane.
	 * The location data contains the element locations for each profile 
	 * measurement and the RMS beam size; only the locations are used 
	 * in the computation of the observation matrix.  The reconstruction
	 * element <arg>strReconElemId</arg> is the location of the Courant-
	 * Snyder parameter reconstruction.
	 * 
	 * @param arrData          Array of location data
	 * @param strReconElemId   string ID of element where reconstruction is performed
	 * @param enmPlane         compute observation matrix for this phase plane
	 * 
	 * @return                 Observation matrix for the given phase plane
	 * 
	 * @throws ModelException  General e
	 */
	public Matrix compObservationMatrix(ArrayList <Measurement> arrData, String strReconElemId, PHASEPLANE enmPlane) 
	    throws ModelException 
	{
		Matrix matObs = this.doComputations(arrData, strReconElemId, enmPlane);
		return matObs;

	}


	/*
	 * Support Methods
	 */
	
    /**
     * Computes the observer matrix from the given data and target element ID
     * @param arrData Array list of beam size data
     * @param strTargElemId String name of target element ID
     * @param plane Phase plane to calculate Observer Matrix along
     * @return Observer Matrix to be used in calculation of sigma vector
     * @throws ModelException Model not loaded correctly
     */
    private Matrix doComputations(ArrayList <Measurement> arrData, String strTargElemId, PHASEPLANE plane) throws ModelException {
        ArrayList<PhaseMatrix>  arrTransMatrices = new ArrayList<PhaseMatrix>();    
        int n = 0;

        for (Measurement datum : arrData) {
            String strElemId = datum.strDevId;

            PhaseMatrix trans = genTransMat.retrieveTransferMatrix(strTargElemId, strElemId);
            arrTransMatrices.add(n, trans);
            n ++;
        }

        Matrix matObs = new Matrix(arrData.size(), 3);

        for (int i = 0; i < arrData.size(); i++) {
            PhaseMatrix matPhi = arrTransMatrices.get(i);

           PhaseMatrix matTe1, matTe23, matTe4;
            double      dblElem0, dblElem1, dblElem2;

            switch(plane) {
            case HOR:
                matTe1  = MAT_BASIS1X.conjugateTrans(matPhi); 
                matTe23 = MAT_BASIS2X.plus(MAT_BASIS3X).conjugateTrans(matPhi); 
                matTe4  = MAT_BASIS4X.conjugateTrans(matPhi); 
                
                dblElem0 = matTe1.getElem(0, 0);
                dblElem1 = matTe23.getElem(0, 0);
                dblElem2 = matTe4.getElem(0, 0);    
                break;

            case VER:
                matTe1  = MAT_BASIS1Y.conjugateInv(matPhi); 
                matTe23 = MAT_BASIS2Y.plus(MAT_BASIS3Y).conjugateTrans(matPhi); 
                matTe4  = MAT_BASIS4Y.conjugateTrans(matPhi);
                
                dblElem0 = matTe1.getElem(2, 2);
                dblElem1 = matTe23.getElem(2, 2);
                dblElem2 = matTe4.getElem(2, 2);    
                break;

            default:
                dblElem0 = 0.0;
                dblElem1 = 0.0;
                dblElem2 = 0.0;
            }

            matObs.set(i, 0, dblElem0);
            matObs.set(i, 1, dblElem1);
            matObs.set(i, 2, dblElem2);
        }
        return matObs;
    }
	
}
