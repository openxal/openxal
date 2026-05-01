/**
 *
 * @author T. Gorlov, created on 10/12/2020
 *  
 */

// five ramameters fit of waveform : (A0, A, k, mu, phi) or (A0, k, mu, Asin, Acos)
// fit(i) = A0 + A*exp(k*i*i)*cos(i*mu + phi) = A0 + exp(k*i*i)*(Acos*cos(i*mu) - Asin*sin(i*mu))
// where index i in this fit as well as in waveform[i] array begins with zero i = 0, 1, 2...

package xal.extension.fit;

import java.util.List;
import xal.tools.math.DifferentialVariable;
import xal.extension.solver.*;
import xal.extension.solver.hint.InitialDelta;
import xal.tools.statistics.MutableUnivariateStatistics;

import xal.extension.solver.algorithm.*;

import java.util.*;


public class GaussianSinusoidFit {

        private int count; //number of waveform points to be processed
        private double [] waveform; //waveform of data to be fit

        private double mu; //frequency of oscillations
        private double Acos;  //amplitude A multiplied by cos(phi)        
        private double Asin;  //amplitude A multiplied by sin(phi)        
        private double A0; //offset of waveform       
        private double expk; //exp(k) exponent parameter where k is negative value
        
        private double sumw; //sum of waveform points
        private double sumw2; //sum2 of waveform points
        

        
    public GaussianSinusoidFit( final double[] _waveform, final int _count ) {
        
            waveform = _waveform;
            count = _count;
            
            if ( count < 6 )  throw new IllegalArgumentException( "The element count must be at least six. The supplied element count was: " + count );
            if ( count > waveform.length )  throw new IllegalArgumentException( "The element count " + count + " is greater than the waveform length: " + waveform.length );
         
            sumw = 0;
            sumw2 = 0;
            for (int i = 0; i < count; i++){
                sumw += waveform[i];
                sumw2 += waveform[i]*waveform[i];
            }
    }

    
    public void solveWithNoiseMaxEvaluations( final double noiseLevel, final int maxEvaluations ) {
            final Stopper stopper = SolveStopperFactory.maxEvaluationsStopper( maxEvaluations );
            solve( noiseLevel, stopper );
    }
    
    public void solve( final double noiseLevel, final Stopper stopper ) {

            //final double signalVariance = noiseLevel;
            //final double errorTolerance = Math.sqrt( signalVariance );
        
            final Variable expkVariable = new Variable( "expk", 0.999, 0.0, 2.0);
            final Variable muVariable =  new Variable( "mu", 1.25, 0.0, 2.0);

            final List<Variable> variables = new ArrayList<Variable>();
            variables.add( expkVariable );
            variables.add( muVariable );

            final Solver solver = new Solver(new SimplexSearchAlgorithm(), stopper );
            final ErrorScorer scorer = new ErrorScorer();
            final Problem problem = ProblemFactory.getInverseSquareMinimizerProblem( variables, scorer, 0.00001);
            final InitialDelta initialRange = new InitialDelta();
            initialRange.addInitialDelta( expkVariable, 0.0001);
            initialRange.addInitialDelta( muVariable, 0.01);
            problem.addHint( initialRange );
            solver.solve( problem );
            
    }
        
    private class ErrorScorer implements Scorer {

            /** Constructor */
            public ErrorScorer() {}
            
            /** score the RMS error */
            public double score( final Trial trial, final List<Variable> variables ) {

                    final TrialPoint trialPoint = trial.getTrialPoint();

                    expk = trialPoint.getValue( variables.get(0) );
                    mu = trialPoint.getValue( variables.get(1) );	
                   
                    double    s1 = 0.0;
                    double    s2 = 0.0;
                    double    c1 = 1.0;
                    double    c2 = 1.0;     
                    double    sw = 0.0;
                    double    cw = waveform[0];     
                    double    sc = 0.0;
                    
                    double sinmu = Math.sin(mu);
                    double cosmu = Math.cos(mu);

                    double expki2 = expk;
                    
                    double s = 0.0;
                    double c = 1.0;
                    double st;
                    double ct;
                    
                    for (int i = 1; i < count; i++){

                        st = c*sinmu + s*cosmu;
                        ct = c*cosmu - s*sinmu;
                        
                        s = st*expki2;
                        c = ct*expki2;
                                               
                        expki2 *= expk*expk;
                        
                        s1 += s;
                        s2 += s*s;
                        c1 += c;
                        c2 += c*c;     
                        sw += s*waveform[i];
                        cw += c*waveform[i];     
                        sc += s*c;
                        
                    }

                    double k1 = sw*c2 - cw*sc;
                    double k2 = cw*s2 - sw*sc;
                    double k3 = s1*c2 - c1*sc;
                    double k4 = c1*s2 - s1*sc;
                    double k5 = sc*sc - c2*s2;

                    A0 = (s1*k1 + c1*k2 + k5*sumw)/(s1*k3 + c1*k4 + k5*count);
                    Acos = (A0*k4 - k2)/k5;
                    Asin = (k1 - A0*k3)/k5;

                    return sumw2 - count*A0*A0 + Acos*Acos*c2 - 2*Acos*Asin*sc + Asin*Asin*s2 + 2*Asin*sw - 2*Acos*cw;

            }
    }
    
    
    public double getOffset() {return A0;}
    
    public double getGrowthRate() {return Math.log(expk);}
    
    public double getExpK() {return expk;}
    
    public double getAmplitude() {return Math.sqrt(Acos*Acos + Asin*Asin);}
    
    public double getFrequency() {return mu;}
    
    public double getACosPhase() {return Acos;}
    
    public double getASinPhase() {return Asin;}
    
    public double getPhase() {return Math.atan2( Asin, Acos);}
    
    public double getFractionalTune() {return mu/(2*Math.PI);}
    
    public double getWaveformRMSError(){
        
        double sum = 0;
        for (int i = 0; i < count; i++) {
            sum += Math.pow( A0 + Math.pow(expk,i*i)*(Acos*Math.cos(mu*i) - Asin*Math.sin(mu*i)) - waveform[i] , 2);
        }

        return Math.sqrt(sum/count);        
        
    }
    
    public double[] getFittedWaveform( final double[] positions ) {
	final double[] waveform = new double[positions.length];
                
                for ( int i = 0; i < positions.length ; i++ ){
                    final double position = positions[i];
		waveform[i] = A0 + Math.pow(expk,position*position)*(Acos*Math.cos(mu*position) - Asin*Math.sin(mu*position));
                }
		return waveform;
	}
}
