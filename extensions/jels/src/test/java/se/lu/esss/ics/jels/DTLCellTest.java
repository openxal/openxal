package se.lu.esss.ics.jels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.lu.esss.ics.jels.SingleElementTest.SingleElementTestData;
import se.lu.esss.ics.jels.model.elem.els.ElsElementMapping;
import se.lu.esss.ics.jels.model.elem.jels.JElsElementMapping;
import se.lu.esss.ics.jels.smf.impl.ESSRfCavity;
import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.sim.scenario.DefaultElementMapping;
import xal.sim.scenario.ElementMapping;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.RfGap;

@RunWith(Parameterized.class)
public class DTLCellTest extends SingleElementTest {
	public DTLCellTest(SingleElementTestData data) {
		super(data);
	}


	@Parameters
	public static Collection<Object[]> tests() {
		final double frequency = 4.025e8, current = 0;
		
		List<Object []> tests = new ArrayList<>();

		//		System.out.println("DTL Cell test");
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe( 2.5e6, frequency, current); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
					148174, -35, 10, 0,
					0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
			
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{+9.749908e-01, +6.610770e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-3.621475e+00, +7.571430e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.077946e+00, +7.155936e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +5.560820e+00, +1.276079e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.388414e-01, +6.531479e-02}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.794280e+00, +9.164720e-01},
			};
			
			// TW correlation matrix
			TWGamma = 1.002787652; 
			TWCorrelationMatrix = new double[][] {
					{+7.849968e-13, -1.616913e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-1.616913e-12, +1.337361e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.505982e-12, +9.194360e-12, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +9.194360e-12, +6.133377e-11, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.249099e-12, -3.800717e-12}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.800717e-12, +8.926809e-12},
			};
			
			// ELS results
			elsPosition = 6.853400E-02;
			elsSigma = new double[] {9.128969E-04, 1.266179E-03, 1.698689E-03};
			elsBeta = new double[] {2.901633E-01, 5.600682E-01, 7.393248E-01};
		}}});
		
//		System.out.println("DTL Cell test");
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe( 2.5e9, frequency, current); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
					148174, -35, 10, 0,
					0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{+9.988970e-01, +6.847455e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-9.571625e-02, +9.944901e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.001052e+00, +6.858989e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +9.580911e-02, +1.005461e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.999992e-01, +5.103468e-03}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.233478e-04, +9.999465e-01},
			};
			
			// TW correlation matrix
			TWGamma = 3.664587829; 
			TWCorrelationMatrix = new double[][] {
					{+1.711433e-14, +2.607576e-14, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+2.607576e-14, +2.466521e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.704184e-14, +3.315048e-14, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +3.315048e-14, +1.707244e-13, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.707843e-15, +5.088331e-14}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.088331e-14, +1.599327e-12},
			};			
		}}});

//		System.out.println("DTL Cell test");
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe( 0.2e6, frequency, current); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
					148174, -35, 10, 0,
					0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{-4.241750e-02, +1.701181e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-7.418281e+01, -2.216842e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +4.927015e-01, +4.133921e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, -3.606720e+01, -2.739446e-01, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.928342e+00, +2.152325e-01}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.146369e+02, +5.281615e+00},
			};
			
			// TW correlation matrix
			TWGamma = 1.000115926; 
			TWCorrelationMatrix = new double[][] {
					{+1.429478e-14, +4.097482e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+4.097482e-12, +1.445445e-08, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.159021e-12, -7.718000e-11, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, -7.718000e-11, +5.302167e-09, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.075229e-10, +7.164306e-09}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +7.164306e-09, +1.669067e-07},
			};			
			
		}}});

		// System.out.println("DTL Cell test only RF");
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 0 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe( 2.5e6, frequency, current); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
					148174, -35, 10, 0,	0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{+1.026033e+00, +6.881175e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+8.922687e-01, +1.012651e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.026033e+00, +6.881175e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +8.922687e-01, +1.012651e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.388414e-01, +6.531479e-02}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.794280e+00, +9.164720e-01},
			};
			
			// TW correlation matrix
			TWGamma = 1.002787652;
			TWCorrelationMatrix = new double[][] {
					{+8.672837e-13, +2.044325e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+2.044325e-12, +1.390906e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.366517e-12, +2.619081e-12, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.619081e-12, +1.075062e-11, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.249099e-12, -3.800717e-12}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.800717e-12, +8.926809e-12},
			};			
			
			// ELS results
			elsPosition = 6.853400E-02;
			elsSigma = new double[] { 9.128969E-04, 1.266179E-03, 1.698689E-03};
			elsBeta = new double[] {2.901633E-01, 5.600682E-01, 7.393248E-01};
		}}});

		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe( 2.5e9, frequency, current); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
					148174, -35, 10, 0,	0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
			
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{+9.999741e-01, +6.853221e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+1.203956e-05, +9.999740e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +9.999741e-01, +6.853221e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.203956e-05, +9.999740e-01, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.999992e-01, +5.103468e-03}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.233478e-04, +9.999465e-01},
			};
			
			// TW correlation matrix
			TWGamma = 3.664587829; 
			TWCorrelationMatrix = new double[][] {
					{+1.715036e-14, +2.771183e-14, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+2.771183e-14, +2.512650e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.698464e-14, +3.056533e-14, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +3.056533e-14, +1.649823e-13, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.707843e-15, +5.088331e-14}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.088331e-14, +1.599327e-12},
			};			
		}}});
		

		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe( 0.2e6, frequency, current); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
					148174, -35, 10, 0,	0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
			
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{+2.122435e-01, +2.862845e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-5.729959e+01, -1.339885e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.122435e-01, +2.862845e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, -5.729959e+01, -1.339885e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +4.928342e+00, +2.152325e-01}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +1.146369e+02, +5.281615e+00}, 
			};
			
			// TW correlation matrix
			TWGamma = 1.000115926; 
			TWCorrelationMatrix = new double[][] {
					{+1.687031e-13, -3.526898e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-3.526898e-11, +8.498568e-09, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.443334e-13, -5.635180e-11, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, -5.635180e-11, +1.376846e-08, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.075229e-10, +7.164306e-09}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +7.164306e-09, +1.669067e-07},
			};			
		}}});

		// System.out.println("DTL Cell test Only RF no TTF");
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 0 148174 -35 10 0 0 0 0 0 0 
		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe( 2.5e6, frequency, current); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
					148174, -35, 10, 0,
					0, 0, 0, 0, 0, 0);
			
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{+1.020163e+00, +6.881804e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+9.344715e-01, +1.020147e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.020163e+00, +6.881804e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +9.344715e-01, +1.020147e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.359490e-01, +6.517928e-02}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.879158e+00, +9.125075e-01},
			};
			
			// TW correlation matrix
			TWGamma = 1.002793768; 
			TWCorrelationMatrix = new double[][] {
					{+8.584677e-13, +2.079518e-12, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+2.079518e-12, +1.419830e-11, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.352122e-12, +2.670547e-12, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.670547e-12, +1.105212e-11, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.229414e-12, -4.077278e-12}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -4.077278e-12, +9.645998e-12},
			};			
			
			// ELS results
			elsPosition = 6.853400E-02;
			elsSigma = new double[] { 9.128969E-04, 1.266179E-03, 1.698689E-03};
			elsBeta = new double[] {2.901633E-01, 5.600682E-01, 7.393248E-01};
		}}});
		
		// System.out.println("DTL Cell test Only RF no TTF");
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 0 148174 -35 10 0 0 0 0 0 0
		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe( 2.5e9, frequency, current); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
					148174, -35, 10, 0,
					0, 0, 0, 0, 0, 0);
			
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{+9.999812e-01, +6.853270e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+8.719464e-06, +9.999812e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +9.999812e-01, +6.853270e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +8.719464e-06, +9.999812e-01, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.999994e-01, +5.103574e-03}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -2.341764e-04, +9.999613e-01},
			};
			
			// TW correlation matrix
			TWGamma = 3.664538568; 
			TWCorrelationMatrix = new double[][] {
					{+1.715061e-14, +2.771218e-14, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+2.771218e-14, +2.512685e-13, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.698502e-14, +3.056569e-14, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +3.056569e-14, +1.649845e-13, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.707857e-15, +5.088473e-14}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.088473e-14, +1.599382e-12},
			};			
		}}});
		
		// System.out.println("DTL Cell test Only RF no TTF");
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 0 148174 -35 10 0 0 0 0 0 0
		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe( 0.2e6, frequency, current); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 0,
					148174, -35, 10, 0,
					0, 0, 0, 0, 0, 0);
			
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{+1.783789e+00, +9.139507e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+2.626371e+01, +1.783335e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.783789e+00, +9.139507e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.626371e+01, +1.783335e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -8.001782e-01, -3.813379e-04}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -5.255661e+01, -1.010888e+00},
			};
			
			// TW correlation matrix
			TWGamma = 1.000342512; 
			TWCorrelationMatrix = new double[][] {
					{+8.837476e-12, +1.332337e-10, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+1.332337e-10, +2.015749e-09, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.411260e-11, +2.114815e-10, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +2.114815e-10, +3.173543e-09, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +7.678454e-12, +5.100915e-10}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +5.100915e-10, +3.390161e-08},
			};			
		}}});
		
//		System.out.println("DTL Cell test with spacecharge I=30mA");
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe2( 2.5e6, frequency, 30e-3); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
					148174, -35, 10, 0,
					0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
			
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{+9.749908e-01, +6.610770e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-3.621475e+00, +7.571430e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.077946e+00, +7.155936e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +5.560820e+00, +1.276079e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.388414e-01, +6.531479e-02}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -1.794280e+00, +9.164720e-01}, 
			};
			
			// TW correlation matrix
			TWGamma = 1.002787652; 
			TWCorrelationMatrix = new double[][] {
					{+8.174030e-07, -1.233304e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-1.233304e-06, +1.150580e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.552823e-06, +1.005340e-05, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.005340e-05, +7.013171e-05, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.305220e-06, -3.031050e-06}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.031050e-06, +7.184365e-06}, 
			};
		}}});

//		System.out.println("DTL Cell test with spacecharge I=30mA E=200MeV");
		// DTL_CEL 68.534 22.5 22.5 0.00864202 0 46.964 148174 -35 10 0 0.0805777 0.772147 -0.386355 -0.142834
		tests.add(new Object[] {new SingleElementTestData() {{
			probe = setupOpenXALProbe2( 200e6, frequency, 30e-3); 
			elementMapping = JElsElementMapping.getInstance();
			sequence = dtlcell(4.025e8, 68.534, 22.5, 22.5, 0.00864202, 0, 46.964,
					148174, -35, 10, 0,
					0.0805777, 0.772147, -0.386355, -0.142834, 0, 0);
			
			// TW transfer matrix
			TWTransferMatrix = new double[][]{
					{+9.943127e-01, +6.822478e-02, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-4.887408e-01, +9.717297e-01, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.005366e+00, +6.881648e-02, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +4.937581e-01, +1.028010e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +9.998901e-01, +4.654681e-02}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -4.720550e-03, +9.994376e-01}, 
			};
			
			// TW correlation matrix
			TWGamma = 1.002787652; 
			TWCorrelationMatrix = new double[][] {
					{+8.174030e-07, -1.233304e-06, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{-1.233304e-06, +1.150580e-05, +0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.552823e-06, +1.005340e-05, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +1.005340e-05, +7.013171e-05, +0.000000e+00, +0.000000e+00}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, +3.305220e-06, -3.031050e-06}, 
					{+0.000000e+00, +0.000000e+00, +0.000000e+00, +0.000000e+00, -3.031050e-06, +7.184365e-06}, 
			};
		}}});

		
		return tests;
	}
	
	/**
	 * 
	 * @param L
	 * @param Lq1
	 * @param Lq2
	 * @param g
	 * @param B1
	 * @param B2
	 * @param E0TL Effective gap voltage
	 * @param Phis RF phase (deg) absolute or relative
	 * @param R aperture
	 * @param p 0: relative phase, 1: absolute phase
	 * @param betas particle reduced velocity
	 * @param Ts transit time factor
	 * @param kTs
	 * @param k2Ts
	 * @param kS
	 * @param k2S
	 * @return
	 */
	public static AcceleratorSeq dtlcell(double frequency, double L, double Lq1, double Lq2, double g, double B1, double B2, 
			double E0TL, double Phis, double R,	double p, 
			double betas, double Ts, double kTs, double k2Ts, double kS, double k2S)
	{
		AcceleratorSeq sequence = new AcceleratorSeq("DTLCellTest");
		
		// mm -> m
		L *= 1e-3;
		Lq1 *= 1e-3;
		Lq2 *= 1e-3;
		g *= 1e-3;
		
		// setup		
		// QUAD1,2
		Quadrupole quad1 = new Quadrupole("quad1") { // there's no setter for type (you need to extend class)
			{_type="Q"; }
		};
		quad1.setPosition(0.5*Lq1); //always position on center!
		quad1.setLength(Lq1); // effLength below is actually the only one read 
		quad1.getMagBucket().setEffLength(Lq1);
		quad1.setDfltField(B1);
		quad1.getMagBucket().setPolarity(1);
		
		Quadrupole quad2 = new Quadrupole("quad2") { // there's no setter for type (you need to extend class)
			{_type="Q"; }
		};
		quad2.setPosition(L-0.5*Lq2); //always position on center!
		quad2.setLength(Lq2); // effLength below is actually the only one read 
		quad2.getMagBucket().setEffLength(Lq2);
		quad2.setDfltField(B2);
		quad2.getMagBucket().setPolarity(1);
		
		
		// GAP
		RfGap gap = new RfGap("g");
		gap.setFirstGap(true); // this uses only phase for calculations
		gap.getRfGap().setEndCell(0);
		gap.setLength(0.0); // used only for positioning
		gap.setPosition(0.5*L-g);
		// following are used to calculate E0TL
		double length = g; // length is not given in TraceWin, but is used only as a factor in E0TL in OpenXal
		gap.getRfGap().setLength(length); 		
		gap.getRfGap().setAmpFactor(1.0);
		gap.getRfGap().setTTF(1.0);		
		/*gap.getRfGap().setGapOffset(dblVal)*/		
		
		ESSRfCavity dtlTank = new ESSRfCavity("d"); // this could also be rfcavity, makes no difference
		dtlTank.addNode(quad1);
		dtlTank.addNode(gap);
		dtlTank.addNode(quad2);
		dtlTank.getRfField().setPhase(Phis);		
		dtlTank.getRfField().setAmplitude(E0TL * 1e-6 / length);
		dtlTank.getRfField().setFrequency(frequency * 1e-6);		
		/*cavity.getRfField().setStructureMode(dblVal);*/
				
		// TTF		
		if (betas == 0.0) {			
			dtlTank.getRfField().setTTFCoefs(new double[] {0.0});
		} else {
			dtlTank.getRfField().setTTFCoefs(new double[] {betas, Ts, kTs, k2Ts});
			dtlTank.getRfField().setTTF_endCoefs(new double[] {betas, Ts, kTs, k2Ts});
			dtlTank.getRfField().setSTFCoefs(new double[] {betas, 0., kS, k2S});
			dtlTank.getRfField().setSTF_endCoefs(new double[] {betas, 0., kS, k2S});
		}		
		
		sequence.addNode(dtlTank);
		sequence.setLength(L);
		
		return sequence;		
	}
	
}
