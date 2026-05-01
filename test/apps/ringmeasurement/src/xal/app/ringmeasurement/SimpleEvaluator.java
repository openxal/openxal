package xal.app.ringmeasurement;

//import xal.tools.optimizer.*;
import xal.extension.solver.*;
import java.util.List;

public class SimpleEvaluator implements Scorer {

    CalcQuadSettings cqs;
    public SimpleEvaluator(CalcQuadSettings m) {
        cqs = m;
    }
    
	/* (non-Javadoc)
	 * @see xal.tools.optimizer.Scorer#score()
	 */
	public double score(Trial trial, List<Variable> variables) {
		cqs.updateModel();
		double myScore = cqs.calcError();

		return myScore;
	}

	public boolean accept() {
		return true;
	}
}
