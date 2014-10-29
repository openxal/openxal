package xal.app.experimentautomator.core;

import java.io.File;
import java.net.URL;
import java.util.Date;

import xal.app.experimentautomator.exception.NotificationException;

public class ExperimentConfig {

	private boolean elemScan;
	private boolean probeScan;
	private boolean boundMonitor;
	private boolean pvAcqSelected;
	private boolean probeAcqSelected;

	private File elementScanTablePath;
	private File probeScanTablePath;
	private File pvAcqListPath;
	private File probeAcqListPath;
	private File boundTablePath;
	private int delayTime1;
	private int delayTime2;
	private int delayTime3;
	private int thresholdChecks;

	private Date experimentTime;

	public ExperimentConfig(EAWindow mainWindow, PrefWindow prefWindow)
			throws NotificationException {

		loadBooleans(mainWindow);
		extractTextFields(mainWindow);
		experimentTime = new Date();
		extractPrefs(prefWindow);

	}

	public void extractPrefs(PrefWindow prefWindow)
			throws NotificationException {

		try {

			delayTime1 = prefWindow.getDelayTime1();
			delayTime2 = prefWindow.getDelayTime2();
			delayTime3 = prefWindow.getDelayTime3();
			thresholdChecks = prefWindow.getThresholdChecks();
		} catch (Exception e) {
			throw new NotificationException(
					"Error reading preferences. Please check for proper formatting.");
		}

	}

	private void extractTextFields(EAWindow window) {
		// TODO may throw null pointer exceptions. Deal with these.
		elementScanTablePath = new File(window.getElementScanTableTextField());
		probeScanTablePath = new File(window.getProbeScanTableTextField());
		pvAcqListPath = new File(window.getPvAcqTableTextField());
		probeAcqListPath = new File(window.getProbeAcqTableTextField());
		boundTablePath = new File(window.getBoundTableTextField());
	}

	private void loadBooleans(EAWindow window) throws NotificationException {
		elemScan = window.elemScanButton.isSelected();
		probeScan = window.probeScanButton.isSelected();
		if (elemScan == probeScan)
			throw new NotificationException(
					"Configuration Implementation Issue");
		boundMonitor = window.boundMonitorCheckBox.isSelected();
		pvAcqSelected = window.pvAcqTableCheckBox.isSelected();
		probeAcqSelected = window.probeAcqTableCheckBox.isSelected();
	}

	public boolean isElemScan() {
		return elemScan;
	}

	public boolean isProbeScan() {
		return probeScan;
	}

	public boolean pvAcqSelected() {
		return pvAcqSelected;
	}

	public boolean probeAcqSelected() {
		return probeAcqSelected;
	}

	public File getElementScanTablePath() {
		return elementScanTablePath;
	}

	public File getPvAcqListPath() {
		return pvAcqListPath;
	}

	public File getProbeAcqListPath() {
		return probeAcqListPath;
	}

	public File getProbeScanTablePath() {
		return probeScanTablePath;
	}

	public File getBoundTablePath() {
		return boundTablePath;
	}

	public int getDelay() {
		return delayTime1;
	}

	public Date getExperimentTime() {
		return experimentTime;
	}

	public boolean boundMonitor() {
		return boundMonitor;
	}

	public boolean usesModel() {
		if (probeScan || probeAcqSelected)
			return true;
		return false;
	}

	public Integer getDelay1() {
		return delayTime1;
	}

	public Integer getDelay2() {
		return delayTime2;
	}

	public Integer getDelay3() {
		return delayTime3;
	}

	public Integer getThresholdChecks() {
		return thresholdChecks;
	}

	/**
	 * Not for actual use! Creates an ExperimentConfig object solely for testing
	 * purposes. Containe pre-defined field values.
	 * 
	 * @author rnewhouse
	 */
	public ExperimentConfig() {

		this.elemScan = true;
		this.probeScan = false;
		this.boundMonitor = false;
		this.pvAcqSelected = true;
		this.probeAcqSelected = false;

		URL url = getClass().getResource(
				"apps.experimentautomator.src.test.testscan.txt");
		File file = new File(url.getPath());
		this.elementScanTablePath = file;
		this.probeScanTablePath = null;
		this.pvAcqListPath = null;
		this.probeAcqListPath = null;
		this.boundTablePath = null;
		this.delayTime1 = 1000;
		this.delayTime2 = 1000;
		this.delayTime3 = 1000;
		this.thresholdChecks = 15;

		experimentTime = new Date();

	}

	/**
	 * Not for actual use! Creates an ExperimentConfig object solely for testing
	 * purposes. Containe pre-defined field values.
	 * 
	 * @author rnewhouse
	 */
	public ExperimentConfig(File path) {

		this.elemScan = true;
		this.probeScan = false;
		this.boundMonitor = false;
		this.pvAcqSelected = true;
		this.probeAcqSelected = false;

		this.elementScanTablePath = path;
		this.probeScanTablePath = null;
		this.pvAcqListPath = null;
		this.probeAcqListPath = null;
		this.boundTablePath = null;
		this.delayTime1 = 1000;
		this.delayTime2 = 1000;
		this.delayTime3 = 1000;
		this.thresholdChecks = 15;

		experimentTime = new Date();

	}
}
