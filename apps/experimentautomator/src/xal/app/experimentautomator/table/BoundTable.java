package xal.app.experimentautomator.table;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import xal.app.experimentautomator.core.ExperimentConfig;
import xal.app.experimentautomator.core.PVConnection;
import xal.app.experimentautomator.exception.ChannelAccessException;
import xal.app.experimentautomator.exception.NotificationException;
import xal.app.experimentautomator.exception.ThresholdException;

public class BoundTable {

	private List<String> boundPVs;
	private List<Double> boundUpper;
	private List<Double> boundLower;
	private ExperimentConfig config;

	public BoundTable(ExperimentConfig config) throws NotificationException {
		this.boundPVs = new ArrayList<>();
		this.boundUpper = new ArrayList<>();
		this.boundLower = new ArrayList<>();
		this.config = config;

		File path = this.config.getBoundTablePath();
		if (!path.isFile()) {
			throw new NotificationException("Not a valid path: Bound Table");
		}
		try {
			parseBoundTable(path);
		} catch (FileNotFoundException e) {
			throw new NotificationException("File Not Found");
		}

	}

	private void parseBoundTable(File file) throws FileNotFoundException,
			NotificationException {
		String[] tokens;
		// Open Connection
		Scanner elementScanTableStream = new Scanner(new FileInputStream(file));

		// Get bound PV Names
		tokens = elementScanTableStream.nextLine().split("\\s+");
		boundPVs.addAll(Arrays.asList(tokens));
		// Check for duplicate bound PV names
		if (duplicates(boundPVs)) {
			elementScanTableStream.close();
			throw new NotificationException("Duplicates in Bound Table");
		}

		// Get Lower Bound Values
		tokens = elementScanTableStream.nextLine().split("\\s+");
		for (String val : tokens) {
			try {
				boundLower.add(Double.parseDouble(val));
			} catch (Exception e) {
				elementScanTableStream.close();
				throw new NotificationException(
						"Value should be a Double. Refer to documnetation for instructions.");
			}
		}

		// Get Lower Bound Values
		tokens = elementScanTableStream.nextLine().split("\\s+");
		for (String val : tokens) {
			try {
				boundUpper.add(Double.parseDouble(val));
			} catch (Exception e) {
				elementScanTableStream.close();
				throw new NotificationException(
						"Value should be a Double. Refer to documnetation for instructions.");
			}
		}

		elementScanTableStream.close();

		// Validate lengths
		if (boundLower.size() != boundPVs.size()
				|| boundPVs.size() != boundLower.size())
			throw new NotificationException(
					"Bound scan table: Line Length Mismatch");

	}

	public boolean checkBounds() throws NotificationException,
			ThresholdException {
		HashMap<String, Double> actualValues;
		try {
			actualValues = PVConnection.readPvValues(boundPVs);
		} catch (ChannelAccessException e) {
			throw new NotificationException(
					"ChannelAccessException while checking bounds. Check spelling of PV names.");
		}

		for (int i = 0; i < boundPVs.size(); i++) {
			if (actualValues.get(boundPVs.get(i)) < boundLower.get(i)) {
				throw new ThresholdException(boundPVs.get(i),
						boundLower.get(i), actualValues.get(boundPVs.get(i)));
			}

			if (boundUpper.get(i) < actualValues.get(boundPVs.get(i))) {
				throw new ThresholdException(boundPVs.get(i),
						boundUpper.get(i), actualValues.get(boundPVs.get(i)));
			}
		}
		return true;
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
