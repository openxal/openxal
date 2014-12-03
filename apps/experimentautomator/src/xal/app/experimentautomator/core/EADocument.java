/*
 * EADocument.java
 *
 * Created on March 19, 2003, 1:32 PM
 */

package xal.app.experimentautomator.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import xal.app.experimentautomator.exception.ChannelAccessException;
import xal.app.experimentautomator.exception.CompletedException;
import xal.app.experimentautomator.exception.NotificationException;
import xal.app.experimentautomator.exception.PausedException;
import xal.app.experimentautomator.exception.ThresholdException;
import xal.app.experimentautomator.table.AbstractScan;
import xal.app.experimentautomator.table.AcquisitionList;
import xal.app.experimentautomator.table.BoundTable;
import xal.app.experimentautomator.table.ElementScan;
import xal.app.experimentautomator.table.ProbeScan;
import xal.extension.application.smf.AcceleratorDocument;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;

/**
 * This class acts as the controller and top-level container for experiment automator.
 * 
 * @author robinnewhouse
 *
 */
public class EADocument extends AcceleratorDocument {

	public static final Integer ACQUISITION_CASE_FALSE = 0;
	public static final Integer ACQUISITION_CASE_TRUE = 1;
	public static final Integer ACQUISITION_CASE_PAUSE = 2;

	private ExperimentConfig config;
	AcquisitionList pvAcq;
	AcquisitionList probeAcq;
	AbstractScan scan;
	private Scenario model;
	private boolean requestPause;

	/**
	 * The document for the text pane in the main window.
	 */
	protected PlainDocument textDocument;
	// private JFrame pvResultTableFrame;
	// private JFrame probeResultTableFrame;
	protected PrefWindow prefWindow;
	private Map<String, Double> savedState = null;
	// private Component probeResultComponent;
	// private Component pvResultComponent;
	private BoundTable bound;

	private PVResult pvResult;
	private ProbeResult probeResult;

	private Accelerator accel;
	private AcceleratorSeq seq;

	/** Create a new empty document */
	public EADocument() {
		this(null);
	}

	/**
	 * Create a new document loaded from the URL file
	 * 
	 * @param url
	 *            The URL of the file to load into the new document.
	 */
	public EADocument(java.net.URL url) {
		setSource(url);
		makeTextDocument();

		if (url == null)
			return;

		try {
			final int charBufferSize = 1000;
			InputStream inputStream = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));

			StringBuffer stringBuffer = new StringBuffer();
			char[] charBuffer = new char[charBufferSize];
			int numRead = 0;
			while ((numRead = reader.read(charBuffer, 0, charBufferSize)) != -1) {
				stringBuffer.append(charBuffer, 0, numRead);
			}

			textDocument.insertString(0, stringBuffer.toString(), null);
			setHasChanges(false);
		} catch (java.io.IOException exception) {
			throw new RuntimeException(exception.getMessage());
		} catch (BadLocationException exception) {
			throw new RuntimeException(exception.getMessage());
		}

	}

	/**
	 * Make a main window by instantiating the my custom window. Set the text
	 * pane to use the textDocument variable as its document.
	 */
	public void makeMainWindow() {
		mainWindow = new EAWindow(this);

		prefWindow = new PrefWindow(this);
		prefWindow.setTitle("Preferences");
		prefWindow.setVisible(false);
	}

	/**
	 * 
	 * @return Default directory where interpolation files are stored. NULL on
	 *         error.
	 */
	public String constructDefaultInterpolationDirectory() {
		try {
			File file = new File(acceleratorFilePath);
			return file.getParent() + "/data/";
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Save the document to the specified URL.
	 * 
	 * @param url
	 *            The URL to which the document should be saved.
	 */
	public void saveDocumentAs(URL url) {
		try {
			int length = textDocument.getLength();
			String text = textDocument.getText(0, length);

			File file = new File(url.getPath());
			if (!file.exists()) {
				file.createNewFile();
			}

			@SuppressWarnings("resource")
			FileWriter writer = new FileWriter(file);
			writer.write(text, 0, text.length());
			writer.flush();
			setHasChanges(false);
		} catch (BadLocationException exception) {
			System.err.println(exception);
			Logger.getLogger("global").log(Level.WARNING, "Save Failed",
					exception);
			displayWarning("Save Failed!",
					"Save Failed due to an internal exception!", exception);
		} catch (IOException exception) {
			System.err.println(exception);
			Logger.getLogger("global").log(Level.WARNING, "Save Failed",
					exception);
			displayWarning("Save Failed!",
					"Save Failed due to an internal exception!", exception);
		}
	}

	/**
	 * Convenience method for getting the main window cast to the proper
	 * subclass of XalWindow. This allows me to avoid casting the window every
	 * time I reference it.
	 * 
	 * @return The main window cast to its dynamic runtime class
	 */
	private EAWindow myWindow() {
		return (EAWindow) mainWindow;
	}

	private PrefWindow prefWindow() {
		return prefWindow;
	}

	/**
	 * Instantiate a new PlainDocument that servers as the document for the text
	 * pane. Create a handler of text actions so we can determine if the
	 * document has changes that should be saved.
	 */
	private void makeTextDocument() {
		textDocument = new PlainDocument();
		textDocument.addDocumentListener(new DocumentListener() {
			public void changedUpdate(javax.swing.event.DocumentEvent evt) {
				setHasChanges(true);
			}

			public void removeUpdate(DocumentEvent evt) {
				setHasChanges(true);
			}

			public void insertUpdate(DocumentEvent evt) {
				setHasChanges(true);
			}
		});
	}

	/**
	 * Edit preferences for the document.
	 */
	public void editPreferences() {
		prefWindow.setVisible(true);
	}

	/**
	 * Loads the user input configuration from the current window.
	 * 
	 * @author rnewhouse
	 * @throws NotificationException
	 */
	public ExperimentConfig loadConfig() throws NotificationException {

		// Load User Inputs in Text Fields
		return new ExperimentConfig(myWindow(), prefWindow());

	}

	private void loadSequence() throws NotificationException {
		// Retrieve Sequence
		accel = XMLDataManager.acceleratorWithPath(this
				.getAcceleratorFilePath());
		try {
			// Select sequence or combo sequence from selected drop-down
			seq = accel.getComboSequence(this.getSelectedSequence().getId());
			if (seq == null) {
				// If not a combo sequence
				seq = accel.getSequence(this.getSelectedSequence().getId());
			}
		} catch (Exception e) {
			throw new NotificationException(
					"Please select an accelerator sequence");
		}
	}

	private void initializeModel() throws NotificationException {
		// Initialize Model
		EnvTrackerAdapt envTrackerAdapt = new EnvTrackerAdapt();
		EnvelopeProbe initProbe = ProbeFactory.getEnvelopeProbe(seq,
				envTrackerAdapt);
		try {
			model = Scenario.newScenarioFor(seq);
		} catch (ModelException e) {
			throw new NotificationException(
					"ModelException while loading configuration");
		}
		model.setProbe(initProbe);
		String runMode = Scenario.SYNC_MODE_LIVE;
		model.setSynchronizationMode(runMode);
		// model.setEmpiricalTrackingType(TrackingType.TranLin,
		// this.constructDefaultInterpolationDirectory());
	}

	public void updateModel() throws NotificationException {
		try {
			// model.resetProbe();
			model.resync();
			model.run();
		} catch (ModelException e) {
			throw new NotificationException("Error synchronizing model");
		}
	}

	/**
	 * Sets the scan object associated with this document. Currently ElementScan
	 * or ProbeScan. Sets the AcquisitionList object associated with this
	 * document. Sets the BoundTable object associated with this document.
	 * 
	 * @throws NotificationException
	 */
	public void initialize() throws NotificationException {

		// Load the current configuration
		config = loadConfig();

		// Set Start Button Label
		myWindow().startButtonLabel("Start");

		// Reset objects
		scan = null;
		pvAcq = null;
		probeAcq = null;
		bound = null;

		// Initialize request pause
		requestPause = false;

		// If model is in use, initialize it
		if (config.usesModel()) {
			loadSequence();
			initializeModel();
		}

		// Load scan objects
		{
			if (config.isElemScan())
				scan = new ElementScan(config);

			else if (config.isProbeScan())
				scan = new ProbeScan(config, model);
		}

		// Load bound monitor object
		if (config.boundMonitor())
			bound = new BoundTable(config);

		// Load acquisition objects
		if (config.pvAcqSelected()) {
			pvAcq = new AcquisitionList(config.getPvAcqListPath());

			pvResult = new PVResult(pvAcq.getPVs());
			// pvResultTableFrame = new JFrame("PV Results");
			// pvResultTableFrame.setSize(500, 300);
			// pvResultTableFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		}
		if (config.probeAcqSelected()) {
			probeAcq = new AcquisitionList(config.getProbeAcqListPath());

			probeResult = new ProbeResult(model, seq, probeAcq.getPVs());
			// probeResultTableFrame = new JFrame("Probe Results");
			// probeResultTableFrame.setSize(500, 300);
			// probeResultTableFrame
			// .setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		}

		setMessageText("Experiment Successfully Initialized -- "
				+ config.getExperimentTime().toString());
		setStatusText("Ready to Begin Experiment");
		myWindow().updateProgress(0, scan.getTotalSteps());
	}

	/**
	 * Sets the described values for a single step in the scan.
	 * 
	 * @author rnewhouse
	 * @return
	 * @throws NotificationException
	 * @throws CompletedException
	 * @throws PausedException
	 */
	public Integer executeStep() throws NotificationException,
			CompletedException, PausedException {

		if (scan == null)
			throw new NotificationException(
					"Please select a scan table (and optionally an acquisition list) and click \"Initialize\"");

		Integer step = scan.getStepNumber();

		// Check if pause requested
		if (requestPause)
			pauseProgram();

		// Check if completed
		if (scan.isComplete())
			throw new CompletedException();

		// Line Break
		setMessageText("===============================");

		// Reset Model
		if (config.usesModel())
			model.resetProbe();

		// Set new values from scan table
		setMessageText("Step " + step.toString() + " : Setting New Values");
		/**
		 * acquisitionCase - 0: do not acquire readbacks - 1: acquire readbacks
		 * - 2: acquire readbacks and pause program
		 */
		scan.setNextStep(this.model);

		// Delay for alloted time
		delay(config.getDelay1());

		// Update Model
		if (config.usesModel()) {
			try {
				model.resync();
				model.run();
			} catch (ModelException e) {
				throw new NotificationException("Error updating model");
			}
		}

		if (!scan.getAcquisitionCase(step).equals(ACQUISITION_CASE_FALSE)) {
			/** Check if PVs are within prescribed thresholds */
			validateThresholds();
		}

		return scan.getAcquisitionCase(step);

	}

	/**
	 * Checks if the values of the readback PVs are within a reasonable range of
	 * the setpoint values. These ranges and the comparison method are described
	 * in the scan table.
	 * 
	 * @throws NotificationException
	 */
	private void validateThresholds() throws NotificationException {
		setMessageText("Step " + scan.getStepNumber().toString()
				+ " : Checking Thresholds");

		Integer monitorCount = 0;

		while (true) {
			try {
				// Validate relative thresholds
				scan.checkThresholds();

				// Validate absolute bounds
				if (bound != null)
					bound.checkBounds();

				// Successful validation, Exit loop
				delay(config.getDelay3());
				break;

			} catch (ThresholdException e) {
				if (monitorCount > config.getThresholdChecks())
					throw new NotificationException(
							"Threshold re-check exceeded allowed number.");

				setMessageText("ThresholdException -- PV: " + e.pvName
						+ " Expected" + e.expected.toString() + " Actual: "
						+ e.actual.toString() + " ... Delaying and re-checking");

				monitorCount++;
				delay(config.getDelay2()); // Delay before re-checking
			}
		}
	}

	/**
	 * Retrieves the values of the PVs and/or probes described in the
	 * acquisition table.
	 * 
	 * @throws NotificationException
	 */
	public void acquireStep() throws NotificationException {
		Integer acquisitionCase = scan.getAcquisitionCase(scan.getStepNumber());

		if (acquisitionCase.equals(ACQUISITION_CASE_FALSE)) {
			/** Do not acquire data. */
			return;
		}

		if (acquisitionCase.equals(ACQUISITION_CASE_TRUE)
				|| acquisitionCase.equals(ACQUISITION_CASE_PAUSE)) {
			/** Update Results */
			if (config.pvAcqSelected()) {
				setMessageText("Step " + scan.getStepNumber().toString()
						+ " : Extracting PV Resutls");
				pvResult.fillCurrentStepResults(scan.getStepNumber());
				// pvResult.displayResults(pvResultTableFrame,
				// pvResultComponent);
			}
			if (config.probeAcqSelected()) {
				setMessageText("Step " + scan.getStepNumber().toString()
						+ " : Extracting Probe Resutls");
				probeResult.fillCurrentStepResults(scan.getStepNumber());
				// probeResult.displayResults(probeResultTableFrame,
				// probeResultComponent);
			}
		}
	}

	public void completeStep() {
		scan.incrementStepNumber();
		myWindow().updateProgress(scan.getStepNumber(), scan.getTotalSteps());
	}

	/**
	 * Calls saveData from the current results table
	 * 
	 * @throws NotificationException
	 */
	public void saveData() throws NotificationException {
		if (config.pvAcqSelected()) {
			File saveLocation = pvResult.saveData(config);
			setMessageText("PV Data saved to " + saveLocation.toString());
		}

		if (config.probeAcqSelected()) {
			File saveLocation = probeResult.saveData(config);
			setMessageText("Probe Data saved to " + saveLocation.toString());
		}
	}

	public void saveState() throws NotificationException {
		ExperimentConfig tmpConfig = loadConfig();
		if (!tmpConfig.isElemScan())
			throw new NotificationException(
					"Saving a PV state is only valid for element scan");
		ElementScan tmpScan = new ElementScan(tmpConfig);
		try {
			this.savedState = PVConnection.readPvValues(tmpScan.getPVs());
		} catch (ChannelAccessException e) {
			throw new NotificationException(
					"ChannelAccessException Reading PVs to save initial state");
		}
		setMessageText("Current machine state saved");
	}

	public void loadState() throws NotificationException {
		if (this.savedState == null)
			throw new NotificationException("A state has not yet been saved");
		try {
			PVConnection.setPvValues(this.savedState);
		} catch (ChannelAccessException e) {
			throw new NotificationException(
					"ChannelAccessException Setting PVs to return to initial state");
		}
		setMessageText("Previous machine state loaded");
	}

	/**
	 * Called to pause the execution of an experiment
	 */
	public void requestPause() {
		this.requestPause = true;
		setMessageText("Pause Requested");
	}

	public void unsetPause() {
		requestPause = false;
	}

	/**
	 * Performs activities necessary to pause an experiment's execution.
	 */
	private void pauseProgram() throws PausedException {
		setMessageText("Step " + scan.getStepNumber().toString()
				+ " : Paused. Ready to resume program.");
		myWindow().startButtonLabel("Resume");
		throw new PausedException();
	}

	/**
	 * Delays for time specified in configuration window
	 * 
	 * @author rnewhouse
	 */
	private void delay(Integer delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			displayError("Error", "Thread interrupted in delay");
			e.printStackTrace();
		}
	}

	public void skipStep() throws NotificationException, CompletedException {
		if (scan == null || pvAcq == null)
			throw new NotificationException(
					"Please select inputs and click \"Initialize\"");
		if (scan.isComplete())
			throw new CompletedException();
		setMessageText("Step " + scan.getStepNumber().toString() + " : Skipped");
		scan.skipStep();

	}

	public void setMessageText(String message) {
		System.out.println(message);
		myWindow().setMessageText(message);
	}

	public void setStatusText(String status) {
		myWindow().setStatusText(status);
	}

	public void disableButtons() {
		myWindow().diableButtons();
	}

	public void enableButtons() {
		myWindow().enableButtons();
	}

	public void updatePrefs() {
		if (config != null) {
			try {
				config.extractPrefs(prefWindow);
			} catch (NotificationException e) {
				displayError("Error", e.getMessage());
			}
		}
	}

	public void pauseForAcquisition() {
		displayWarning(
				"Experiment Paused",
				"The experimet is paused at step "
						+ scan.getStepNumber().toString()
						+ ". Please click OK to acquire data for this step and continue the experiment.");

	}
}
