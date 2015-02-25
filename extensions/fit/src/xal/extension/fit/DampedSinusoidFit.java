//
// DampedSinusoid.java: Source file for 'DampedSinusoid'
// Project xal
//
// Created by t6p on 3/11/2010
//

package xal.extension.fit;

import xal.tools.math.DifferentialVariable;
import xal.extension.solver.*;
import xal.extension.solver.hint.InitialDelta;
import xal.tools.statistics.MutableUnivariateStatistics;

import java.util.*;


/** 
 DampedSinusoid provides an exact closed form solution for fitting a waveform to a damped sinusoid of the form <b><code><i>q</i> = <i>A</i>e<i><sup>&gamma;t</sup></i>sin(<i>&mu;t</i> + <i>&phi;</i>) + <i>C</i></code></b> which is adapted for efficient fitting in the presence of noise. The fits for frequency, offset and growth rate are good in the presence of relatively small noise. However, the estimation of phase and amplitude are relatively poor in the presence of noise. Also, the estimation breaks down when the frequency is near an integer or half integer.
 */
final public class DampedSinusoidFit {
	/** raw waveform */
	final private double[] WAVEFORM;
	
	/** number of points to use */
	final private int NUM_POINTS;
	
	/** waveform with the offset removed */
	private DifferentialVariable[] _initialZeroedWaveform;
	
	/** optimized waveform error */
	private double[] _waveformError;
	
	/** initial waveform error */
	private double[] _initialWaveformError;
	
	/** indicates whether the initial waveform error has been calculated */
	private boolean _initialWaveformErrorCalculated;
	
	/** initial fitted offset */
	private DifferentialVariable _initialOffset;
	
	/** initial estimate of the signal variance */
	private double _initialSignalVariance;
	
	/** indicates whether the offset has been calculated */
	private boolean _initialOffsetCalculated;
	
	/** optimal offset */
	private double _offset;
	
	/** optimized estimate of the signal variance */
	private double _signalVariance;
	
	/** initial fitted growth rate */
	private DifferentialVariable _initialGrowthRate;
	
	/** initial value of the exponent of the growth rate */
	private DifferentialVariable _initialGrowthFactor;
	
	/** array of initial growth factors demonstrating the variation in the growth factor calculations */
	private List<Double> _initialGrowthFactorArray;
	
	/** indicates whether the growth rate has been calculated */
	private boolean _initialGrowthRateCalculated;
	
	/** optimized growth rate */
	private double _growthRate;
	
	/** optimized growth factor */
	private double _growthFactor;
	
	/** fitted frequency */
	private DifferentialVariable _initialFrequency;
	
	/** initial fitted cosine of the angular frequency */
	private DifferentialVariable _initialCosineMu;
	
	/** indicates whether the frequency has been calculated */
	private boolean _initialFrequencyCalculated;
	
	/** optimized frequency */
	private double _frequency;
	
	/** optimized cosine mu */
	private double _cosineMu;
	
	/** fitted phase for the sine-like wave */
	private double _initialPhase;
	
	/** indicates whether the phase has been calculated */
	private boolean _initialPhaseCalculated;
	
	/** optimized phase */
	private double _phase;
	
	/** fitted amplitude */
	private double _initialAmplitude;
	
	/** indicates whether the amplitude has been calculated */
	private boolean _initialAmplitudeCalculated;
	
	/** optimized amplitude */
	private double _amplitude;
	
	
	/** 
	 * Primary Constructor 
	 * @param waveform data to fit
	 * @param count starting from the beginning of the waveform, the number of items in the waveform to use in the fit
	 */
    public DampedSinusoidFit( final double[] waveform, final int count ) {
		if ( count < 6 )  throw new IllegalArgumentException( "The element count must be at least six. The supplied element count was: " + count );
		if ( count > waveform.length )  throw new IllegalArgumentException( "The element count " + count + " is greater than the waveform length: " + waveform.length );
		
		WAVEFORM = waveform;
		NUM_POINTS = count;
		
		_waveformError = new double[count];

		_initialOffsetCalculated = false;
		_initialOffset = null;
		_offset = Double.NaN;

		_initialSignalVariance = Double.NaN;
		_signalVariance = Double.NaN;

		_initialGrowthRateCalculated = false;
		_initialGrowthRate = null;
		_initialGrowthFactor = null;
		_initialGrowthFactorArray = new ArrayList<Double>();
		_growthFactor = Double.NaN;
		_growthRate = Double.NaN;
		
		_initialFrequencyCalculated = false;
		_initialFrequency = null;
		_initialCosineMu = null;
		_frequency = Double.NaN;
		_cosineMu = Double.NaN;
		
		_initialWaveformErrorCalculated = false;
		_initialWaveformError = new double[count];
		
		_initialPhaseCalculated = false;
		_initialPhase = Double.NaN;
		_phase = Double.NaN;
		
		_initialAmplitudeCalculated = false;
		_initialAmplitude = Double.NaN;
		_amplitude = Double.NaN;
    }
	
	
	/** 
	 * Constructor accepting the entire waveform
	 * @param waveform data to fit
	 */
    public DampedSinusoidFit( final double[] waveform ) {
		this( waveform, waveform.length );
    }
	
	
	/** 
	 * Get a new instance of the damped sinusoid 
	 * @param waveform data to fit
	 * @param count starting from the beginning of the waveform, the number of items in the waveform to use in the fit
	 */
	static public DampedSinusoidFit getInstance( final double[] waveform, final int count ) {
		return new DampedSinusoidFit( waveform, count );
	}
	
	
	/** 
	 * Get a new instance of the damped sinusoid using the entire waveform
	 * @param waveform data to fit
	 */
	static public DampedSinusoidFit getInstance( final double[] waveform ) {
		return new DampedSinusoidFit( waveform );
	}
	
	
	/** calculate the initial mean square error */
	private double calculateInitialRMSError() {
		final double offset = getInitialOffset();
		final double growthRate = getInitialGrowthRate();
		final double growthFactor = getInitialGrowthRate();
		final double frequency = getInitialFrequency();
		final double cosmu = Math.cos( 2 * Math.PI * frequency );
		
		final double[] waveformError = calculateLeastWaveformError( offset, cosmu, growthFactor );
		
		return calculateRMSError( waveformError );
	}
		
	
	/** calculate the mean square error */
	private double calculateRMSError( final double[] errors ) {		
		double errorSum = 0.0;
		for ( final double epsilon : errors ) {
			errorSum += epsilon * epsilon;
		}
		
		return Math.sqrt( errorSum / NUM_POINTS );
	}
	
	
	/** Estimate the waveform at the specified index using the initial parameters */
	private double estimateWaveformWithInitialParameters( final int index ) {
		final double offset = getInitialOffset();
		final double growthFactor = getInitialGrowthFactor();
		final double frequency = getInitialFrequency();
		final double amplitude = getInitialAmplitude();
		final double phase = getInitialPhase();
		
		return amplitude * Math.pow( growthFactor, index ) * Math.sin( 2 * Math.PI * frequency * index + phase ) + offset;
	}

	
	/** calculate the initial waveform error */
	private void fitInitialWaveformError() {
		final double offset = getInitialOffset();
		final double growthFactor = getInitialGrowthFactor();
		final double cosmu = getInitialCosineMu();
		
		_initialWaveformError = calculateLeastWaveformError( offset, cosmu, growthFactor );
		
		_initialWaveformErrorCalculated = true;	
	}
	
	
	/** calculate the waveform error */
	private double[] calculateLeastWaveformError( final double offset, final double cosmu, final double growthFactor ) {		
		final double growthFactorSquared = growthFactor * growthFactor;
		final double alpha = 2.0 * growthFactor * cosmu;
		
		final double[] f = new double[NUM_POINTS];
		final double[] g = new double[NUM_POINTS];
		final double[] h = new double[NUM_POINTS];
		
		f[0] = 1.0;
		f[1] = 0.0;
		g[0] = 0.0;
		g[1] = 1.0;
		h[0] = 0.0;
		h[1] = 0.0;
		
		double ff_sum = 1.0;
		double gg_sum = 1.0;
		double fg_sum = 0.0;
		double gh_sum = 0.0;
		double fh_sum = 0.0;
		for ( int index = 2 ; index < NUM_POINTS ; index++ ) {
			final double f0 = f[index - 2];
			final double f1 = f[index - 1];
			final double g0 = g[index - 2];
			final double g1 = g[index - 1];
			final double h0 = h[index - 2];
			final double h1 = h[index - 1];
			f[index] = alpha * f1 - growthFactorSquared * f0;
			g[index] = alpha * g1 - growthFactorSquared * g0;
			h[index] = alpha * h1 - growthFactorSquared * h0 + alpha * ( WAVEFORM[index - 1] - offset ) - growthFactorSquared * ( WAVEFORM[index - 2] - offset ) + offset - WAVEFORM[index];
			
			ff_sum += f[index] * f[index];
			gg_sum += g[index] * g[index];
			fg_sum += f[index] * g[index];
			gh_sum += g[index] * h[index];
			fh_sum += f[index] * h[index];
		}
		
		final double[] leastErrors = new double[NUM_POINTS];
		final double wronskian = ff_sum * gg_sum - fg_sum * fg_sum;		
		leastErrors[0] = wronskian != 0.0 ? ( gh_sum * fg_sum - gg_sum * fh_sum ) / wronskian : 0.0;
		leastErrors[1] = wronskian != 0.0 ? ( fh_sum * fg_sum - ff_sum * gh_sum ) / wronskian : 0.0;
		
//		final double[] compErrors = calculateWaveformError( offset, cosmu, growthFactor, leastErrors[0], leastErrors[1] );
		for ( int index = 2; index < NUM_POINTS ; index++ ) {
			leastErrors[index] = f[index] * leastErrors[0] + g[index] * leastErrors[1] + h[index];
//			System.out.println( "comp[" + index + "] = " + compErrors[index] + ", least[" + index + "] = " + leastErrors[index] );
		}
		
		// comparison of RMS errors to validate whether we are calculating the least RMS error
//		final double[] testErrors = calculateWaveformError( offset, cosmu, growthFactor, 0.0, 0.0 );	// test with arbitrary first two initial errors
//		final double leastRMSError = calculateRMSError( leastErrors );
//		final double testRMSError = calculateRMSError( testErrors );
//		System.out.println( "Least RMS Error: " + leastRMSError + ", Test RMS Error: " + testRMSError );
		
		return leastErrors;
	}
	
	
	/** calculate the waveform error */
	private double[] calculateWaveformError( final double offset, final double cosmu, final double growthFactor, final double error0, final double error1 ) {
		final double growthFactorSquared = growthFactor * growthFactor;
				
		final double[] waveformError = new double[NUM_POINTS];
		waveformError[0] = error0;
		waveformError[1] = error1;
//		System.out.println( "error[0] = " + waveformError[0] );
//		System.out.println( "error[1] = " + waveformError[1] );
		
		final int count = NUM_POINTS - 2;
		for ( int index = 0 ; index < count ; index++ ) {
			final double epsilon = 2.0 * growthFactor * cosmu * ( WAVEFORM[index + 1] + waveformError[index + 1] - offset ) - ( WAVEFORM[index] + waveformError[index] - offset ) * growthFactorSquared + offset - WAVEFORM[index + 2];
			waveformError[index + 2] = epsilon;
//			System.out.println( "error[" + (index + 2) + "] = " + epsilon );
//			System.out.println( "error[" + (index + 2) + "] = " + ( estimateWaveformWithInitialParameters(index+2) - WAVEFORM[index+2] ) );
		}
		
		return waveformError;
	}
	
	
	
	/** scores the RMS error */
	private class ErrorScorer implements Scorer {
		/** offset variable */
		final private Variable OFFSET_VARIABLE;
		
		/** variable for exponent of the growth rate */
		final private Variable GROWTH_FACTOR_VARIABLE;
		
		/** variable for cosine mu */
		final private Variable COSINE_MU_VARIABLE;
				
		
		/** Constructor */
		public ErrorScorer( final Variable offsetVariable, final Variable growthFactorVariable, final Variable cosineMuVariable ) {
			OFFSET_VARIABLE = offsetVariable;
			GROWTH_FACTOR_VARIABLE = growthFactorVariable;
			COSINE_MU_VARIABLE = cosineMuVariable;
		}
		
		
		/** score the RMS error */
		public double score( final Trial trial, final List<Variable> variables ) {							   
			final TrialPoint trialPoint = trial.getTrialPoint();
			final double offset = trialPoint.getValue( OFFSET_VARIABLE );
			final double cosmu = trialPoint.getValue( COSINE_MU_VARIABLE );
			final double growthFactor = trialPoint.getValue( GROWTH_FACTOR_VARIABLE );
			
			if ( Double.isNaN( offset ) || Double.isNaN( cosmu ) || Double.isNaN( growthFactor ) ) {
				return Double.POSITIVE_INFINITY;
			}
			
			final double[] waveformErrors = calculateLeastWaveformError( offset, cosmu, growthFactor );
			double sumSquareError = 0.0;
			for ( final double error : waveformErrors ) {
				sumSquareError += error * error;
			};
						
//			System.out.println( "Algorithm: " + trial.getAlgorithm().getLabel() );
//			System.out.println( "Scoring trial with offset: " + offset + ", cosmu: " + cosmu + ", growth: " + growthFactor + ", Square Score: " + sumSquareError );

			if ( Double.isNaN( sumSquareError ) ) {
				trial.vetoTrial( new TrialVeto( trial, null, "error is NaN" ) );
				return Double.POSITIVE_INFINITY;
			}

			return Math.sqrt( sumSquareError / waveformErrors.length );
		}
	}
	
	
	/** 
	 * Run the solver to find the best fit 
	 * @param noiseLevel estimate of the expected noise
	 */
	public void solveWithNoise( final double noiseLevel ) {
		solveWithNoiseMaxEvaluationsSatisfaction( noiseLevel, 100000, 0.95 );
	}		
	
	
	/** 
	 * Run the solver to find the best fit 
	 * @param noiseLevel estimate of the expected noise
	 * @param maxEvaluations the maximum number of evaluations to perform
	 */
	public void solveWithNoiseMaxEvaluations( final double noiseLevel, final int maxEvaluations ) {
		final Stopper stopper = SolveStopperFactory.maxEvaluationsStopper( maxEvaluations );
		solve( noiseLevel, stopper );
	}		
	
	
	/** 
	 * Run the solver to find the best fit 
	 * @param noiseLevel estimate of the expected noise
	 * @param maxEvaluations the maximum number of evaluations to perform
	 * @param satisfaction the satisfaction target to reach before stopping
	 */
	public void solveWithNoiseMaxEvaluationsSatisfaction( final double noiseLevel, final int maxEvaluations, final double satisfaction ) {
		final Stopper stopper = SolveStopperFactory.maxEvaluationsSatisfactionStopper( maxEvaluations, satisfaction );
		solve( noiseLevel, stopper );
	}		
	
	
	/** 
	 * Run the solver to find the best fit 
	 * @param noiseLevel estimate of the expected noise
	 * @param maxTime the maximum time to wait for the solution
	 */
	public void solveWithNoiseMaxTime( final double noiseLevel, final double maxTime ) {
		solveWithNoiseMaxTimeSatisfaction( noiseLevel, maxTime, 0.95 );
	}		
	
	
	/** 
	 * Run the solver to find the best fit 
	 * @param noiseLevel estimate of the expected noise
	 * @param maxTime the maximum time to wait for the solution
	 * @param satisfaction the satisfaction target to reach before stopping
	 */
	public void solveWithNoiseMaxTimeSatisfaction( final double noiseLevel, final double maxTime, final double satisfaction ) {
		final Stopper stopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( 0.01, maxTime, satisfaction );
		solve( noiseLevel, stopper );
	}		
	
	
	/** 
	 * Run the solver to find the best fit 
	 * @param noiseLevel the noise level used to estimate the error bounds and provide a measure for satisfaction
	 * @param stopper the stopper which determines when to stop the solver
	 */
	public void solve( final double noiseLevel, final Stopper stopper ) {
		final double offset = getInitialOffset();
		
		final double initialGrowthFactor = getInitialGrowthFactor();
		final double growthFactor = Double.isNaN( initialGrowthFactor ) ? 1.0 : initialGrowthFactor > 0.0 ? initialGrowthFactor : 1.0;
		
		final double initialCosMu = getInitialCosineMu();
		final double cosmu = Double.isNaN( initialCosMu ) ? 0.0 : initialCosMu < -1.0 ? -1.0 : initialCosMu > 1.0 ? 1.0 : initialCosMu;
		
		final double signalVariance = noiseLevel > 0.0 ? noiseLevel * noiseLevel : getInitialSignalVariance();
		final double initialOffsetSigma = Math.sqrt( _initialOffset.varianceWithSignalVariance( signalVariance ) );
		final double initialGrowthFactorSigma =  Math.sqrt( _initialGrowthFactor.varianceWithSignalVariance( signalVariance ) );
		final double initialCosMuSigma = Math.sqrt( _initialCosineMu.varianceWithSignalVariance( signalVariance ) );
		
		// if the initial sigmas are indeterminate we attempt to guess, but better guesses are needed
		final double offsetSigma = Double.isNaN( initialOffsetSigma ) ? 10.0 : initialOffsetSigma;
		final double growthFactorSigma = Double.isNaN( initialGrowthFactorSigma ) ? 1.0 : initialGrowthFactorSigma;
		final double cosMuSigma = Double.isNaN( initialCosMuSigma ) ? 0.1 : initialCosMuSigma;
				
		// allow ten sigma for ranges
		final double offsetSlack = 10.0 * offsetSigma;
		final double growthFactorSlack = 10.0 * growthFactorSigma;
		final double cosMuSlack = 10.0 * cosMuSigma;
		
		final double lowerCosine = Math.max( -1.0, cosmu - cosMuSlack );
		final double upperCosine = Math.min( 1.0, cosmu + cosMuSlack );
		
		final double lowerGrowthFactor = Math.max( 0.0, growthFactor - growthFactorSlack );
		
		final Variable offsetVariable = new Variable( "offset", offset, offset - offsetSlack, offset + offsetSlack );
		final Variable cosineMuVariable =  new Variable( "cosmu", cosmu, lowerCosine, upperCosine );
		final Variable growthFactorVariable = new Variable( "growthFactor", growthFactor, lowerGrowthFactor, growthFactor + growthFactorSlack );
		
		final List<Variable> variables = new ArrayList<Variable>();
		variables.add( offsetVariable );
		variables.add( cosineMuVariable );
		variables.add( growthFactorVariable );

		final double errorTolerance = Math.sqrt( signalVariance );
		
		final Solver solver = new Solver( stopper );
		final ErrorScorer scorer = new ErrorScorer( offsetVariable, growthFactorVariable, cosineMuVariable );
		final Problem problem = ProblemFactory.getInverseSquareMinimizerProblem( variables, scorer, errorTolerance );
		final InitialDelta initialRange = new InitialDelta();
		initialRange.addInitialDelta( offsetVariable, offsetSigma );
		initialRange.addInitialDelta( growthFactorVariable, growthFactorSigma );
		initialRange.addInitialDelta( cosineMuVariable, cosMuSigma );
		problem.addHint( initialRange );
		solver.solve( problem );
//		System.out.println( solver.getScoreBoard() );
				
		final TrialPoint solution = solver.getScoreBoard().getBestSolution().getTrialPoint();
		_offset = solution.getValue( offsetVariable );
		_growthFactor = solution.getValue( growthFactorVariable );
		_growthRate = Math.log( _growthFactor );
		_cosineMu = solution.getValue( cosineMuVariable );
		_frequency = 0.5 * Math.acos( _cosineMu ) / Math.PI;
		
		_waveformError = calculateLeastWaveformError( _offset, _cosineMu, _growthFactor );
		final double signalSigma = calculateRMSError( _waveformError );
		_signalVariance = signalSigma * signalSigma;
//		System.out.println( "Final RMS Error: " + calculateRMSError( _waveformError ) );
		final double[] zeroedWaveform = new double[NUM_POINTS];
		for ( int index = 0 ; index < NUM_POINTS ; index++ ) {
			zeroedWaveform[index] = WAVEFORM[index] - _offset;
		}
		
		final double mu = _frequency * 2.0 * Math.PI;
		fitPhaseAndAmplitude( _growthFactor, mu, zeroedWaveform, _waveformError );
	}
	
	
	/** get the fitted offset calculating it if necessary */
	public double getInitialOffset() {
		if ( !_initialOffsetCalculated ) {
			fitInitialOffset();
		}
		
		return _initialOffset.getValue();
	}
	
	
	/** get the variance in the initial offset estimate using the initial estimate of the signal variance */
	public double getInitialOffsetVariance() {
		if ( !_initialOffsetCalculated ) {
			fitInitialOffset();
		}
		
		return _initialOffset.varianceWithSignalVariance( _initialSignalVariance );
	}
	
	
	/** get the initial estimat of the signal variance */
	public double getInitialSignalVariance() {
		if ( !_initialOffsetCalculated ) {
			fitInitialOffset();
		}
		
		return _initialSignalVariance;
	}
	
	
	/** get an estimate of the initial offset's variance */
	public double estimateInitialOffsetVariance( final double signalVariance ) {
		if ( !_initialOffsetCalculated ) {
			fitInitialOffset();
		}
		
		return _initialOffset.varianceWithSignalVariance( signalVariance );
	}
	
	
	/** get the optimized offset */
	public double getOffset() {
		return _offset;
	}
	
	
	/** get the optimized estimate of the signal variance */
	public double getSignalVariance() {
		return _signalVariance;
	}
	
	
	/** get the initial growth factor */
	private double getInitialGrowthFactor() {
		if ( !_initialGrowthRateCalculated ) {
			fitInitialGrowthRate();
		}
		
		return _initialGrowthFactor.getValue();
	}
	
	
	/** get the fitted growth rate calculating it if necessary */
	public double getInitialGrowthRate() {
		if ( !_initialGrowthRateCalculated ) {
			fitInitialGrowthRate();
		}
		
		return _initialGrowthRate.getValue();
	}
	
	
	/** get the variance in the initial growth rate estimate using the initial estimate of the signal variance */
	public double getInitialGrowthRateVariance() {
		if ( !_initialGrowthRateCalculated ) {
			fitInitialGrowthRate();
		}
		
		return _initialGrowthRate.varianceWithSignalVariance( _initialSignalVariance );
	}	
	
	
	/** get an estimate of the initial growth rate's variance */
	public double estimateInitialGrowthRateVariance( final double signalVariance ) {
		if ( !_initialGrowthRateCalculated ) {
			fitInitialGrowthRate();
		}
		
		return _initialGrowthRate.varianceWithSignalVariance( signalVariance );
	}
	
	
	/** get the initial waveform error calculating it if necessary */
	public double[] getInitialWaveformError() {
		if ( !_initialWaveformErrorCalculated ) {
			fitInitialWaveformError();
		}
		
		return _initialWaveformError;
	}
	
	
	/** get the initial RMS error */
	public double getInitialWaveformRMSError() {
		return calculateRMSError( getInitialWaveformError() );
	}
	
	
	/** get the optimized growth rate */
	public double getGrowthRate() {
		return _growthRate;
	}
	
	
	/** get the initial fitted cosine of the angular frequency calculating it if necessary */
	private double getInitialCosineMu() {
		if ( !_initialFrequencyCalculated ) {
			fitInitialFrequency();
		}
		
		return _initialCosineMu.getValue();
	}
	
	
	/** get the fitted frequency calculating it if necessary */
	public double getInitialFrequency() {
		if ( !_initialFrequencyCalculated ) {
			fitInitialFrequency();
		}
		
		return _initialFrequency.getValue();
	}
	
	
	/** get the variance in the initial frequency estimate using the initial estimate of the signal variance */
	public double getInitialFrequencyVariance() {
		if ( !_initialFrequencyCalculated ) {
			fitInitialFrequency();
		}
		
		return _initialFrequency.varianceWithSignalVariance( _initialSignalVariance );
	}	
	
	
	/** get an estimate of the initial frequency's variance */
	public double estimateInitialFrequencyVariance( final double signalVariance ) {
		if ( !_initialFrequencyCalculated ) {
			fitInitialFrequency();
		}
		
		return _initialFrequency.varianceWithSignalVariance( signalVariance );
	}
	
	
	/** get the optimized frequency */
	public double getFrequency() {
		return _frequency;
	}
	
	
	/** Get the sine-like phase estimation calculating it if necessary. Note that this estimation is relatively poor. */
	public double getInitialPhase() {
		if ( !_initialPhaseCalculated ) {
			fitInitialPhaseAndAmplitude();
		}
		
		return _initialPhase;
	}
	
	
	/** get the optimized phase */
	public double getPhase() {
		return _phase;
	}
	
	
	/** Get the optimized sine-like phase. */
	public double getSineLikePhase() {
		return getPhase();
	}
	
	
	/** Get the optimized cosine-like phase (equivalent phase if the fitted equation were of the form of A * damping * cos( mu + phase ) ). */
	public double getCosineLikePhase() {
		return toCosineLikePhase( getPhase() );
	}
	
	
	/** get the waveform error */
	public double[] getWaveformError() {
		return _waveformError;
	}
	
	
	/** get the initial RMS error */
	public double getWaveformRMSError() {
		return calculateRMSError( getWaveformError() );
	}
	
	
	/** Get the sine-like phase calculating it if necessary. Note that this estimation is relatively poor. */
	public double getInitialSineLikePhase() {
		return getInitialPhase();
	}
	
	
	/** Get the cosine-like phase calculating it if necessary. Note that this estimation is relatively poor. */
	public double getInitialCosineLikePhase() {
		return toCosineLikePhase( getInitialPhase() );
	}


	/** Convert a sine like phase (default) to a cosine like phase (equivalent phase if the fitted equation were of the form of A * damping * cos( mu + phase )) */
	private double toCosineLikePhase( final double sineLikePhase ) {
		final double rawCosinePhase = sineLikePhase - Math.PI / 2.0;	// shift by pi/2
		return rawCosinePhase < -Math.PI ? rawCosinePhase + 2 * Math.PI : rawCosinePhase;		// force the phase to be between -pi and pi
	}
	
	
	/** Get the sine-like amplitude calculating it if necessary. Note that this estimation is relatively poor. */
	public double getInitialAmplitude() {
		if ( !_initialAmplitudeCalculated ) {
			fitInitialPhaseAndAmplitude();
		}
		
		return _initialAmplitude;
	}

	
	/** Get the optimized sine-like amplitude */
	public double getAmplitude() {
		return _amplitude;
	}

	
	/** calculate the constant offset */
	private void fitInitialOffset() {        
		final int wcount = NUM_POINTS - 2;
		final DifferentialVariable[] w = new DifferentialVariable[wcount];
		final DifferentialVariable[] z = new DifferentialVariable[wcount];
		for ( int index = 0 ; index < wcount ; index++ ) {
			final double q0 = WAVEFORM[index];
			final double q1 = WAVEFORM[index+1];
			final double q2 = WAVEFORM[index+2];
			w[index] = new DifferentialVariable( q0 * q2 - q1 * q1, index, q2, -2.0 * q1, q0 );
			z[index] = new DifferentialVariable( 2 * q1 - q0 - q2, index, -1.0, 2.0, -1.0 );
		}
		
		final int rcount = NUM_POINTS - 4;
		final DifferentialVariable[] r = new DifferentialVariable[rcount];
		final DifferentialVariable[] s = new DifferentialVariable[rcount];
		final DifferentialVariable[] t = new DifferentialVariable[rcount];
		for ( int index = 0 ; index < rcount ; index++ ) {
			final DifferentialVariable z0 = z[index];
			final DifferentialVariable z1 = z[index+1];
			final DifferentialVariable z2 = z[index+2];
			final DifferentialVariable w0 = w[index];
			final DifferentialVariable w1 = w[index+1];
			final DifferentialVariable w2 = w[index+2];
            r[index] = z1.pow( 2.0 ).minus( z0.times( z2 ) );
            s[index] = z1.times( w1 ).times( 2.0 ).minus( z0.times( w2 ) ).minus( z2.times( w0 ) );
            t[index] = w1.pow( 2.0 ).minus( w0.times( w2 ) );
		}
		
		final int count = NUM_POINTS - 5;
        
        DifferentialVariable offsetSum = DifferentialVariable.ZERO;
		DifferentialVariable offsetEstimates[] = new DifferentialVariable[count]; 
        double totalWeight = 0.0;
		for ( int index = 0 ; index < count ; index++ ) {
			final DifferentialVariable r0 = r[index];
			final DifferentialVariable r1 = r[index+1];
			final DifferentialVariable s0 = s[index];
			final DifferentialVariable s1 = s[index+1];
			final DifferentialVariable t0 = t[index];
			final DifferentialVariable t1 = t[index+1];
            
            final DifferentialVariable numerator = r0.times( t1 ).minus( r1.times( t0 ) );
            final DifferentialVariable denominator = r1.times( s0 ).minus( r0.times( s1 ) );
            final DifferentialVariable offsetEstimate = numerator.over( denominator );
			offsetEstimates[index] = offsetEstimate;
            
            final double weight = 1.0 / offsetEstimate.varianceWithSignalVariance( 1.0 );	// signal variance must be one since we really want the sum of square of first partials
            final DifferentialVariable weightedOffset = offsetEstimate.times( weight );
            offsetSum = offsetSum.plus( weightedOffset );
            totalWeight += weight;
            
//			System.out.println( "offset: " + offsetEstimate.getValue() + ", variance: " + 1.0 / localWeight );
		}
        
        final DifferentialVariable offset = offsetSum.over( totalWeight );
//        final double sigma = Math.sqrt( offset.varianceWithCommonVariance( 0.25 ) );
//        System.out.println( "Offset Estimate: " + offset.getValue() + " +/- " + sigma );
		
		_initialOffset = offset;
		
		_initialZeroedWaveform = new DifferentialVariable[NUM_POINTS];
		final DifferentialVariable negativeOffset = offset.negate();
		for ( int index = 0 ; index < NUM_POINTS ; index++ ) {
			_initialZeroedWaveform[index] = new DifferentialVariable( WAVEFORM[index], index, 1.0 ).plus( negativeOffset );
		}
		
		final double offsetValue = offset.getValue();
		
		// calculate the estimated mean signal variance
		double penaltySum = 0.0;
		for ( final DifferentialVariable offsetEstimate : offsetEstimates ) {
			final double error = offsetValue - offsetEstimate.getValue();
			penaltySum += error * error / offsetEstimate.varianceWithSignalVariance( 1.0 );
		}
		_initialSignalVariance = penaltySum / count;
        
		_initialOffsetCalculated = true;
	}
	
	
	/** fit the growth rate to the waveform */
	private void fitInitialGrowthRate() {
		getInitialOffset();	// make sure the zeroed waveform is calculated
		
		final int count = NUM_POINTS - 3;		
		DifferentialVariable growthFactorSquareSum = DifferentialVariable.ZERO;
		double totalWeight = 0.0;
		for ( int index = 0 ; index < count ; index++ ) {
			final DifferentialVariable q0 = _initialZeroedWaveform[index];
			final DifferentialVariable q1 = _initialZeroedWaveform[index+1];
			final DifferentialVariable q2 = _initialZeroedWaveform[index+2];
			final DifferentialVariable q3 = _initialZeroedWaveform[index+3];
			
			final DifferentialVariable numerator = q1.times( q3 ).minus( q2.pow( 2 ) );
			final DifferentialVariable denominator = q0.times( q2 ).minus( q1.pow( 2 ) );
			final DifferentialVariable growthFactorSquareEstimate = numerator.over( denominator );
			if ( growthFactorSquareEstimate.getValue() >= 0.0 ) {	// exclude points where the growth factor is negative
				final double weight = 1.0 / growthFactorSquareEstimate.varianceWithSignalVariance( 1.0 );
				growthFactorSquareSum = growthFactorSquareSum.plus( growthFactorSquareEstimate.times( weight ) );
				totalWeight += weight;
			}
		}
		
		final DifferentialVariable growthFactor = growthFactorSquareSum.over( totalWeight ).sqrt();
		_initialGrowthFactor = growthFactor;
		
		_initialGrowthRate = _initialGrowthFactor.log();
		_initialGrowthRateCalculated = true;
	}
	
	
	/** fit the frequency to the waveform */
	private void fitInitialFrequency() {
		getInitialGrowthFactor();
		final DifferentialVariable growthFactor = _initialGrowthFactor;
		final DifferentialVariable reciprocolGrowthFactor = growthFactor.reciprocal();
		final double count = NUM_POINTS - 2;
		
		DifferentialVariable cosMuSum = DifferentialVariable.ZERO;
		double totalWeight = 0.0;
		for ( int index = 0 ; index < count ; index++ ) {
			final DifferentialVariable q0 = _initialZeroedWaveform[index];
			final DifferentialVariable q1 = _initialZeroedWaveform[index+1];
			final DifferentialVariable q2 = _initialZeroedWaveform[index+2];
			
			if ( q1.getValue() != 0.0 ) {	// exclude points where the denominator goes to zero
				final DifferentialVariable numerator = q0.times( growthFactor ).plus( q2.times( reciprocolGrowthFactor ) );
				final DifferentialVariable estimate = numerator.over( q1 );
				final DifferentialVariable cosMuEstimate = estimate.times( 0.5 );
				final double weight = 1.0 / cosMuEstimate.varianceWithSignalVariance( 1.0 );
				cosMuSum = cosMuSum.plus( cosMuEstimate.times( weight ) );
				totalWeight += weight;
			}
		}
		
		final DifferentialVariable cosMu = cosMuSum.over( totalWeight );
		final double cosMuValue = cosMu.getValue();
		// restrict the frequency to the real part of the arc cosine
		final DifferentialVariable mu = cosMuValue > 1.0 ? DifferentialVariable.ZERO : cosMuValue < -1.0 ? DifferentialVariable.newConstant( Math.PI ) : cosMu.acos();
				
		_initialCosineMu = cosMu;
		_initialFrequency = mu.over( 2.0 * Math.PI );
		_initialFrequencyCalculated = true;
	}
	
	
	/** fit the phase and amplitude */
	private void fitPhaseAndAmplitude( final double growthFactor, final double mu, final double[] zeroedWaveform, final double[] waveformErrors ) {
		final double q0 = zeroedWaveform[0] + waveformErrors[0];
		final double q1 = ( zeroedWaveform[1] + waveformErrors[1] ) / growthFactor;
		
		final double sinMu = Math.sin( mu );
		final boolean isZeroSineMu = Math.abs( sinMu ) < 0.0001;	// avoid catastrophe by setting the sin amplitude to zero near the integer and half integer frequencies
		final double cosAmp = isZeroSineMu ? 0.0 : ( q1 - q0 * Math.cos( mu ) ) / Math.sin( mu );
		final double sinAmp = q0;
		
		_amplitude = Math.sqrt( cosAmp * cosAmp + sinAmp * sinAmp );
		_phase = Math.atan2( sinAmp, cosAmp );
	}
	
	
	/** fit the frequency to the waveform */
	private void fitInitialPhaseAndAmplitude() {
		getInitialGrowthFactor();
		getInitialFrequency();
		
		final DifferentialVariable growthFactor = _initialGrowthFactor;
		final DifferentialVariable reciprocolGrowthFactor = growthFactor.reciprocal();
		final DifferentialVariable tune = _initialFrequency;
		final DifferentialVariable mu = tune.times( 2.0 * Math.PI );
		final DifferentialVariable sinMu = mu.sin();
		
		final double count = NUM_POINTS - 1;

		DifferentialVariable sinAmpSum = DifferentialVariable.ZERO;
		DifferentialVariable cosAmpSum = DifferentialVariable.ZERO;
		double totalCosWeight = 0.0;
		double totalSinWeight = 0.0;
		DifferentialVariable growth = DifferentialVariable.newConstant( 1.0 );
		final boolean isZeroSineMu = Math.abs( sinMu.getValue() ) < 0.0001;	// sine is too close to zero, so need to use approximation about it to avoid catastrophe
		for ( int turn = 0 ; turn < count ; turn++ ) {
			final DifferentialVariable q0 = _initialZeroedWaveform[turn].times( growth );
			final DifferentialVariable q1 = _initialZeroedWaveform[turn+1].times( growth ).times( reciprocolGrowthFactor );
			
			final DifferentialVariable muN = mu.times( turn );
			final DifferentialVariable muNP1 = mu.times( turn + 1 );
			final DifferentialVariable cosMuN = muN.cos();
			final DifferentialVariable cosMuNP1 = muNP1.cos();
			final DifferentialVariable sinMuN = muN.sin();
			final DifferentialVariable sinMuNP1 = muNP1.sin();
			
			final DifferentialVariable cosAmpEstimate = isZeroSineMu ? DifferentialVariable.ZERO : ( q1.times( cosMuN ).minus( q0.times( cosMuNP1 ) ) ).over( sinMu );
			final double cosWeight = isZeroSineMu ? 1.0 : 1.0 / cosAmpEstimate.varianceWithSignalVariance( 1.0 );
			cosAmpSum = cosAmpSum.plus( cosAmpEstimate.times( cosWeight ) );
			totalCosWeight += cosWeight;
			
			final DifferentialVariable sinAmpEstimate = isZeroSineMu ? q0.over( cosMuN ) : q0.minus( cosAmpEstimate.times( sinMuN ) ).over( cosMuN );
			final double sinWeight = 1.0 / sinAmpEstimate.varianceWithSignalVariance( 1.0 );
			sinAmpSum = sinAmpSum.plus( sinAmpEstimate.times( sinWeight ) );
			totalSinWeight += sinWeight;
			
			growth = growth.times( reciprocolGrowthFactor );
		}
				
		final double cosAmp = cosAmpSum.over( totalCosWeight ).getValue();
		final double sinAmp = sinAmpSum.over( totalSinWeight ).getValue();
		
		_initialAmplitude = Math.sqrt( cosAmp * cosAmp + sinAmp * sinAmp );
		_initialPhase = Math.atan2( sinAmp, cosAmp );
		
		_initialAmplitudeCalculated = true;
		_initialPhaseCalculated = true;
	}
	
	
	/** calculate the amplitude */
	static private double calculateAmplitude( final double growthFactor, final double mu, final double phase, final double[] zeroedWaveform, final double[] waveformErrors ) {
		double nsum = 0.0;
		double dsum = 0.0;
		double growth = 1.0;
		final int count = zeroedWaveform.length;
		for ( int index = 0 ; index < count ; index++ ) {
			final double numerator = zeroedWaveform[index] + waveformErrors[index];
			final double denominator = growth * Math.sin( mu * index + phase );
			nsum += numerator * Math.signum( denominator );
			dsum += Math.abs( denominator );
			growth *= growthFactor;
		}
		
		return nsum / dsum;
	}
	
	
	/** fit the amplitude */
	private void fitInitialAmplitude() {
		final double mu = 2 * Math.PI * getInitialFrequency();
		final double phase = getInitialPhase();
		final double gfactor = Math.exp( getInitialGrowthRate() );		
		final double[] zeroedWaveform = new double[ _initialZeroedWaveform.length ];
		for ( int index = 0 ; index < zeroedWaveform.length ; index++ ) {
			zeroedWaveform[index] = _initialZeroedWaveform[index].getValue();
		}
		_initialAmplitude = calculateAmplitude( gfactor, mu, phase, zeroedWaveform, getInitialWaveformError() );
		_initialAmplitudeCalculated = true;
	}


	/**
	 * Convenience method to calculate the fitted waveform over the specified positions
	 * @param positions array of positions over which to calculate the waveform
	 * @return array holding the calculated waveform over each of the positions
	 */
	public double[] getFittedWaveform( final double[] positions ) {
		final double[] waveform = new double[positions.length];
		calculateFittedWaveform( positions, waveform );
		return waveform;
	}


	/** 
	 * Convenience method to calculate the fitted waveform over the specified positions 
	 * @param positions array of positions over which to calculate the waveform
	 * @param array big enough to hold the calculated waveform over each of the positions
	 */
	public void calculateFittedWaveform( final double[] positions, double[] waveform ) {
		final double offset = Double.isNaN( _offset ) ? getInitialOffset() : _offset;
		final double growthFactor = Double.isNaN( _growthFactor ) ? getInitialGrowthFactor() : _growthFactor;
		final double frequency = Double.isNaN( _frequency ) ? getInitialFrequency() : _frequency;
		final double amplitude = Double.isNaN( _amplitude ) ? getInitialAmplitude() : _amplitude;
		final double phase = Double.isNaN( _phase ) ? getInitialPhase() : _phase;

		for ( int index = 0 ; index < waveform.length ; index++ ) {
			final double position = positions[index];
			waveform[index] = amplitude * Math.pow( growthFactor, position ) * Math.sin( 2 * Math.PI * frequency * position + phase ) + offset;
		}
	}
}
