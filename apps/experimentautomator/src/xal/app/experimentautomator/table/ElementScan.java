package xal.app.experimentautomator.table;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import xal.app.experimentautomator.core.ExperimentConfig;
import xal.app.experimentautomator.core.PVConnection;
import xal.app.experimentautomator.exception.ChannelAccessException;
import xal.app.experimentautomator.exception.NotificationException;
import xal.app.experimentautomator.exception.ThresholdException;
import xal.sim.scenario.Scenario;

public class ElementScan extends AbstractScan {
	/** Constants */
	private static final Integer DEFAULT_RELATIVE = 0;

	/** line 1: List of PVs to set */
	private List<String> pvNames;
	/** line 2: List of PVs for threshold checking & initial value recording */
	private List<String> thresholdPVs;
	/** line 3: Values used in comparison for threshold checking. */
	private List<Double> thresholdValues;
	/** line 4: Method used to check thresholds, may be "DIFFERENCE" or "RATIO */
	private List<String> thresholdCheckMethod;
	/**
	 * Control columns: The columns to the right of the standard input table are
	 * used to define the fine-grained controls of each step.
	 */
	private List<Integer> useRelative;
	private List<Integer> useAcquisition;

	private HashMap<String, Double> initialSetpointValues;
	@SuppressWarnings("unused") // To be implemented in the future
	private HashMap<String, Double> initialReadbackValues;

	public ElementScan(ExperimentConfig config) throws NotificationException {
		this.pvNames = new ArrayList<>();
		this.scanTable = new ArrayList<>();
		this.thresholdPVs = new ArrayList<>();
		this.thresholdCheckMethod = new ArrayList<>();
		this.thresholdValues = new ArrayList<>();
		this.useRelative = new ArrayList<>();
		this.useAcquisition = new ArrayList<>();
		this.stepNumber = 0;
		this.config = config;

		readElementScanFile(this.config.getElementScanTablePath());

		try {
			readInitialValues();
		} catch (ChannelAccessException e) {
			throw new NotificationException(
					"ChannelAccessException reading initial values");
		}
	}

	/**
	 * Read the file as defined in the configuration object. If an error is
	 * encountered, notify the user.
	 * 
	 * @throws NotificationException
	 */
	private void readElementScanFile(File path) throws NotificationException {
		// if (!path.isFile()) {
		// throw new NotificationException(
		// "Not a valid path: Element Scan Table");
		// }

		try {
			parseElementScanTable(path);
		} catch (FileNotFoundException e) {
			throw new NotificationException("File Not Found");
		}
	}

	/**
	 * Record the setpoint and readback values as described in the Element Scan
	 * Table. These will be used for reference in the relative scan mode.
	 * 
	 * @throws NotificationException
	 * @throws ChannelAccessException
	 */
	private void readInitialValues() throws NotificationException,
			ChannelAccessException {
		/** Read the initial setpoint values of PVs */
		initialSetpointValues = PVConnection.readPvValues(getPVs());
		// System.out.println(initialSetpointValues.toString());

		/** Read the initial readback values of corresponding PVs */
		initialReadbackValues = PVConnection
				.readPvValues(thresholdPVs);
	}

	/**
	 * Reads the element scan table and parses each line into respective
	 * structures. Various checks are performed a this stage to ensure the
	 * proper format of input file.
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 * @throws NotificationException
	 */
	private void parseElementScanTable(File file) throws FileNotFoundException,
			NotificationException {
		String[] tokens;
		/** Open Connection */
		Scanner elementScanTableStream = new Scanner(new FileInputStream(file));

		/** Get PV names */
		tokens = elementScanTableStream.nextLine().split("\\s+");
		pvNames.addAll(Arrays.asList(tokens));
		/** Check for duplicate PV names */
		if (duplicates(pvNames)) {
			elementScanTableStream.close();
			throw new NotificationException("Duplicates in Element Scan Table");
		}

		/** Get Threshold PV Names */
		tokens = elementScanTableStream.nextLine().split("\\s+");
		thresholdPVs.addAll(Arrays.asList(tokens));

		/** Get Threshold Comparison Values */
		tokens = elementScanTableStream.nextLine().split("\\s+");
		for (String val : tokens) {
			if (val.equals("SKIP"))
				thresholdValues.add(Double.NaN);
			else
				try {
					thresholdValues.add(Double.valueOf(val));
				} catch (Exception e) {
					elementScanTableStream.close();
					throw new NotificationException(
							"Value should be either a double or \"SKIP\" Refer to documnetation for instructions.");
				}
		}

		/** Get Threshold Comparison Method */
		tokens = elementScanTableStream.nextLine().split("\\s+");
		for (String comp : tokens) {
			if (!(comp.equals("RATIO") || comp.equals("DIFFERENCE") || comp
					.equals("IGNORE"))) {
				elementScanTableStream.close();
				throw new NotificationException(
						"Please use RATIO, DIFFERENCE, or IGNORE in element scan table. Refer to documnetation for instructions.");
			}
			thresholdCheckMethod.add(comp);
		}

		/** Read values to set */
		while (elementScanTableStream.hasNextLine()) {
			String line = elementScanTableStream.nextLine().trim();
			readValues(line);
		}

		elementScanTableStream.close();

		// Validate lengths
		if (thresholdCheckMethod.size() != thresholdPVs.size()
				|| thresholdPVs.size() != thresholdValues.size())
			throw new NotificationException(
					"Element scan table: Line Length Mismatch");
		if (useAcquisition.size() != useRelative.size()
				|| useRelative.size() != getTotalSteps()) {
			throw new NotificationException(
					"Element scan table: Read error, controls and element length mismatch");
		}

	}

	private void readValues(String line) {
		List<String> tokenList = Arrays.asList(line.split("\\s+"));
		/** The values to which the PVs will be set */
		List<String> values = tokenList.subList(0, getPVs().size());
		/** The controls columns (if any) */
		List<String> controls = tokenList.subList(getPVs().size(),
				tokenList.size());

		/**
		 * Add controls to appropriate locations. Assume acquisition control if
		 * only one column.
		 */
		if (controls.isEmpty()) {
			useAcquisition.add(DEFAULT_ACQUISITION);
			useRelative.add(DEFAULT_RELATIVE);
		} else if (controls.size() == 1) {
			useAcquisition.add(Integer.valueOf(controls.get(0)));
			useRelative.add(DEFAULT_RELATIVE);
		} else if (controls.size() == 2) {
			useAcquisition.add(Integer.valueOf(controls.get(0)));
			useRelative.add(Integer.valueOf(controls.get(1)));
		}

		scanTable.add(values);
	}

	/**
	 * Steps
	 * 
	 * @throws NotificationException
	 */
	@Override
	public Integer setNextStep(Scenario model) throws NotificationException {

		/** Read from table new value to set */
		Map<String, Double> newPvValues = new HashMap<String, Double>();
		for (String pv : getPVs()) {
			String newValue = getPvValueByName(getStepNumber(), pv);
			if (newValue.equals("SKIP")) {
				// Do Nothing
			} else {
				try {
					// Check if real number
					newPvValues.put(pv, Double.valueOf(newValue));
				} catch (Exception e) {
					throw new NotificationException(
							newValue
									+ " Is not a valid number format. Plean use a valid format or \"SKIP\"");
				}
			}
		}

		/**
		 * If relative flag, add initial setpoint value. Applies to all values
		 * in current step.
		 */
		if (useRelative.get(getStepNumber()) == 1) {
			for (String pv : newPvValues.keySet()) {
				newPvValues.put(pv,
						newPvValues.get(pv) + initialSetpointValues.get(pv));
			}
		}

		/** Set PVs */
		try {
			PVConnection.setPvValues(newPvValues);
		} catch (ChannelAccessException e) {
			throw new NotificationException("ChannelAccessException");
		}

		/** Return information about use of acquisition */
		return useAcquisition.get(getStepNumber());
	}

	@Override
	public boolean checkThresholds() throws NotificationException,
			ThresholdException {

		for (int i = 0; i < thresholdPVs.size(); i++) {
			if (thresholdPVs.get(i).equals("EMPTY"))
				throw new NotificationException(
						"The \"EMPTY\" input is depricated. Please use \"IGNORE\" in comparison method line");

			if (thresholdCheckMethod.get(i).equals("IGNORE")) {
				continue;
			}

			String setValueString = getPvByIndex(getStepNumber(), i);
			if (setValueString.equals("SKIP"))
				continue; // Don't compare to values never set

			Double actual = Double.NaN;
			Double expected = Double.valueOf(setValueString);
			Double comparisonValue = thresholdValues.get(i);

			try {
				actual = PVConnection.readPvValue(thresholdPVs.get(i));
			} catch (ChannelAccessException e) {
				throw new NotificationException(
						"Error accessing threshold channel: "
								+ thresholdPVs.get(i));
			}

			if (thresholdCheckMethod.get(i).equals("RATIO")) { // Ratio
				if (Math.abs((expected - actual) / expected) > comparisonValue)
					throw new ThresholdException(getPVs().get(i), expected,
							actual);
			}

			else if (!thresholdCheckMethod.get(i).equals("DIFFERENCE")) { // Difference
				if (Math.abs(expected - actual) > comparisonValue)
					throw new ThresholdException(getPVs().get(i), expected,
							actual);
			}

		}
		return true;
	}

	/** Getters */
	public List<String> getPVs() {
		return pvNames;
	}

	public List<String> getThresholdPVs() {
		return thresholdPVs;
	}

	public List<Double> getThresholdValues() {
		return thresholdValues;
	}

	public List<String> getThresholdCheckMethod() {
		return thresholdCheckMethod;
	}

	@Override
	public Integer getAcquisitionCase(Integer step) {
		return useAcquisition.get(step);
	}

	public String getPvValueByName(int step, String pvName)
			throws NotificationException {
		int pvNumber = pvNames.indexOf(pvName);
		try {
			return scanTable.get(step).get(pvNumber);
		} catch (Exception e) {
			throw new NotificationException(
					"Element Scan Table PV Name not found in selected sequence");
		}
	}

	public String getPvByIndex(int step, int pvNumber) {
		return scanTable.get(step).get(pvNumber);
	}

	public List<String> getStep(int step) {
		return scanTable.get(step);
	}

	// Helpers
	public boolean duplicates(List<String> list) {
		Set<Object> set = new HashSet<Object>(list);

		if (set.size() < list.size()) {
			return true;
		} else {
			return false;
		}
	}

}
