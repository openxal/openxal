package xal.app.experimentautomator.table;

import java.util.List;

import xal.app.experimentautomator.core.EADocument;
import xal.app.experimentautomator.core.ExperimentConfig;
import xal.app.experimentautomator.exception.NotificationException;
import xal.app.experimentautomator.exception.ThresholdException;
import xal.sim.scenario.Scenario;

public abstract class AbstractScan {

	protected static final Integer DEFAULT_ACQUISITION = EADocument.ACQUISITION_CASE_TRUE;

	protected Integer stepNumber;
	protected ExperimentConfig config;
	protected List<List<String>> scanTable; // String so "SKIP" can be used

	public abstract Integer setNextStep(Scenario model)
			throws NotificationException;

	public abstract boolean checkThresholds() throws NotificationException,
			ThresholdException;

	public Integer getStepNumber() {
		return stepNumber;
	}

	public Integer getTotalSteps() {
		return scanTable.size();
	}

	protected void setStepNumber(Integer stepNumber) {
		this.stepNumber = stepNumber;
	}

	public Integer incrementStepNumber() {
		stepNumber = stepNumber + 1;
		return stepNumber;
	}

	public void skipStep() {
		incrementStepNumber();
	}

	public boolean isComplete() {
		return (getStepNumber() >= getTotalSteps());
	}

	public Integer getAcquisitionCase(Integer step) {
		return DEFAULT_ACQUISITION;
	}
}
