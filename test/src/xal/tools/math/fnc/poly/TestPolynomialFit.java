/**
 * TestPolynomialFit.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jul 28, 2015
 */
package xal.tools.math.fnc.poly;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.test.ResourceManager;
import xal.tools.math.ElementaryFunction;
import xal.tools.math.rn.Rmxn;
import xal.tools.math.rn.Rn;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Jul 28, 2015
 */
public class TestPolynomialFit {
    
    
    /*
     * Global Constants
     */
    
    /** Minimum beta to use */
    final private static double BETA_MIN = 0.0651533;
    
    /** maximum beta */
    final private static double BETA_MAX = 0.07958691;

    /** Polynomial order */
    final private static int  DEG_POLY = 4;
    
    /** Number of samples */
    final private static int CNT_SMPS = 10;

//    db = (bmax - bmin)/(Ns - 1);
//    dk = (kmax - kmin)/(Ns - 1);
//    
//    /** */
//    k[b_] = (2 \[Pi] 402.5*10^6)/(b 3.0*10^8)
    
    
    /*
     * Global Variables
     */
    
    /** Stream for outputting test results */
    static private PrintStream      OSTR_OUTPUT;
    
    
    
    /*
     * Global Methods
     */
    
    /**
     * Computes the wave number for the given particle velocity &beta;
     * assuming 402.5 MHz RF.
     * 
     * @param dblBeta   normalized particle velocity
     * 
     * @return          wave number <i>k</i> 
     *
     * @since  Jul 28, 2015   by Christopher K. Allen
     */
    private static double waveNumber(double dblBeta) {
        double dblWavNum = (2.0*Math.PI*402.5e6)/(dblBeta*3.0e8);
        
        return dblWavNum;
    }

    /**
     * Provides the values of a test transit time factor (the sinc function)
     * for the given &beta;.
     * 
     * @param dblBeta   normalized particle velocity
     * 
     * @return      <i>T</i>(&beta;)
     *
     * @since  Jul 28, 2015   by Christopher K. Allen
     */
    private static double transitTimeFactor(double dblBeta) {
        double  dblWavNum = waveNumber(dblBeta);
        double  dblTtf = ElementaryFunction.sinc(0.01*dblWavNum);

        return dblTtf;
    };
    
    
    /**
     * Creates the array of sample locations for particle velocity
     * &beta; from the constants <code>CNT_SMPS</code>, <code>BETA_MIN</code>,
     * and <code>BETA_MAX</code>.
     * 
     * @return  array of [&beta;<sub>0</sub>,..., &beta;<sub><i>N</i>-1</sub>]
     *
     * @since  Jul 28, 2015   by Christopher K. Allen
     */
    private static double[]  buildSmpLocArray() {
        
        double      dblDelta = (BETA_MAX - BETA_MIN)/(CNT_SMPS - 1);
        double[]    arrBetaSmps = new double[CNT_SMPS];
        
        for (int n=0; n<CNT_SMPS; n++) {
            double  dblBeta = BETA_MIN + n*dblDelta;
            
            arrBetaSmps[n] = dblBeta;
        }
        
        return arrBetaSmps;
    }

    /**
     * @throws java.lang.Exception
     *
     * @since  Jul 28, 2015   by Christopher K. Allen
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        File    fileOutput = ResourceManager.getOutputFile(TestPolynomialFit.class, TestPolynomialFit.class.getName() + ".txt");
        TestPolynomialFit.OSTR_OUTPUT = new PrintStream(fileOutput);  
    }

    /**
     * @throws java.lang.Exception
     *
     * @since  Jul 28, 2015   by Christopher K. Allen
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    
    /*
     * Tests
     */
    
    /**
     * Test method for {@link xal.app.ttffactory.PolynomialFit#PolynomialFit(double[], double[])}.
     */
    @Test
    public final void testPolynomialFit() {
        
        double[]    arrBetaSmps = new double[CNT_SMPS];
        
    }

    /**
     *  Test mimicking the polynomial fitting in class <code>PolynomialFit</code>. 
     *
     * @since  Jul 28, 2015   by Christopher K. Allen
     */
    @Test
    public final void testFittingInitializationAlgorithm() {
        
        double[]    arrBetaSmps = buildSmpLocArray();

        Rn   T = this.buildDataVector(arrBetaSmps);
        Rmxn A = this.buildSystemMatrix(arrBetaSmps);
        
        OSTR_OUTPUT.println("A row cnt = " + A.getRowCnt());
        OSTR_OUTPUT.println("A col cnt = " + A.getColCnt());
        OSTR_OUTPUT.println("A = " + A.toStringMatrix());
        
        Rmxn At   = A.transpose();
        Rmxn AtA  = At.times(A);
        Rmxn Ainv = AtA.inverse();
        Rmxn Alsi = Ainv.times(At);
        
        Rn   c = Alsi.times(T);
        
        double dblRes = compResidual(arrBetaSmps, c);
    }

    /**
     * Test method for {@link xal.app.ttffactory.PolynomialFit#getPolyConstants()}.
     */
    @Test
    public final void testGetPolyConstants() {
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Computes the system matrix from the array of beta values
     * and the order of the polynomial to be fitted.
     * 
     * @param arrBetaSmps   array of beta sampling locations
     * 
     * @return  the system matrix for polynomial fitting
     *
     * @since  Jul 28, 2015   by Christopher K. Allen
     */
    private Rmxn    buildSystemMatrix(double[] arrBetaSmps) {
        

        if (arrBetaSmps.length != CNT_SMPS) {
            fail("wrong number of samples");
            
            throw new IllegalArgumentException("wrong number of samples");
        }

        Rmxn    matSys = new Rmxn(CNT_SMPS, DEG_POLY+1);
        
        for (int n=0; n<CNT_SMPS; n++) {
            double dblBeta = arrBetaSmps[n];
            
            for (int m=0; m<=DEG_POLY; m++) { 
                double  dblElem = Math.pow(dblBeta, m);
                
                matSys.setElem(n, m, dblElem);
            }
        }
        
        return matSys;
    }
    
    /**
     * Samples the transit time factor function at the given &beta;
     * locations to create the data vector.
     * 
     * @param arrBetaSmps   array of beta sampling locations
     * 
     * @return              vector of sampled transit time factor values
     *
     * @since  Jul 28, 2015   by Christopher K. Allen
     */
    private Rn  buildDataVector(double[] arrBetaSmps) {
        
        if (arrBetaSmps.length != CNT_SMPS) {
            fail("wrong number of samples");
            
            throw new IllegalArgumentException("wrong number of samples");
        }
        
        Rn      vecData = new Rn(CNT_SMPS);
        
        for (int n=0; n<CNT_SMPS; n++) {
            double  dblBeta = arrBetaSmps[n];
            double  dblVal  = transitTimeFactor(dblBeta);
            
            vecData.setElem(n, dblVal);
        }
        
        return vecData;
    }
    
    /**
     * Evaluate the polynomial defined by the given vector of coefficients
     * at the given value of variable.
     * 
     * @param vecPolyCoeffs coefficients vector of the polynomial
     * 
     * @param dblLoc    location to evaluation polynomial
     * 
     * @return          value of polynomial at given location 
     *
     * @since  Jul 28, 2015   by Christopher K. Allen
     */
    private double  evalPolynomialAt(Rn vecPolyCoeffs, double dblLoc) {
        
        double  dblSum = 0.0;
        for (int n=0; n<vecPolyCoeffs.getSize(); n++) {
            double  dblCoeff = vecPolyCoeffs.getElem(n);
            double  dblTerm = dblCoeff * Math.pow(dblLoc, n);
            
            dblSum += dblTerm;
        }
        
        return dblSum;
    }
    
    /**
     * Computes the residual error (<i>l</i><sub>2</sub> norm) between the 
     * polynomial defined by the given vector of coefficients and the 
     * sample transit time factor used in this test class.  The error is
     * computed by evaluating these functions at the &beta; values contained
     * int the given array.
     * 
     * @param arrBetaSmps       array of &beta; values used to compute the error
     * @param vecPolyCoeffs     vector of coefficients defining the polynomial fit
     * 
     * @return                  ||<i>T</i> - <i>P</i>||<sub>2</sub>
     *
     * @since  Jul 28, 2015   by Christopher K. Allen
     */
    private double  compResidual(double[] arrBetaSmps, Rn vecPolyCoeffs) {
        
        double  dblRes2 = 0.0;
        
        for (double dblBeta : arrBetaSmps) {
            double  dblPolyVal = this.evalPolynomialAt(vecPolyCoeffs, dblBeta);
            double  dblFuncVal = transitTimeFactor(dblBeta);
            double  dblDelta = dblFuncVal - dblPolyVal;
            
            dblRes2 += dblDelta*dblDelta;
        }
        
        double dblRes = Math.sqrt(dblRes2/CNT_SMPS);
                
        return dblRes;
    }
}
