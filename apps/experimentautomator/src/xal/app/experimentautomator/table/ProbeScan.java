package xal.app.experimentautomator.table;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import xal.app.experimentautomator.core.ExperimentConfig;
import xal.app.experimentautomator.exception.NotificationException;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.Scenario;
import xal.tools.beam.Twiss;

public class ProbeScan extends AbstractScan {

	// String so "SKIP" can be used
	Map<String, Integer> map = new HashMap<String, Integer>() {
	    /** serialization ID */
	    private static final long serialVersionUID = 1L;

		{
			put("KE", 0);
			put("alphaX", 1);
			put("betaX", 2);
			put("emitX", 3);
			put("alphaY", 4);
			put("betaY", 5);
			put("emitY", 6);
			put("alphaZ", 7);
			put("betaZ", 8);
			put("emitZ", 9);
		}
	};
	private List<String> probeNames;

	public ProbeScan(ExperimentConfig config, Scenario model)
			throws NotificationException {

		this.scanTable = new ArrayList<>();
		this.stepNumber = 0;
		this.config = config;

		// TODO Auto-generated constructor stub

		File path = this.config.getProbeScanTablePath();
		if (!path.isFile()) {
			throw new NotificationException(
					"Not a valid path: Probe Scan Table");
		}
		try {
			parseProbeScanTable(path);
		} catch (FileNotFoundException e) {
			throw new NotificationException("File Not Found");
		}

		// TODO make results and store them here

	}

	private void parseProbeScanTable(File file) throws FileNotFoundException,
			NotificationException {
		// Open Connection
		Scanner probeScanTableStream = new Scanner(new FileInputStream(file));

		// Get PV names
		String line = probeScanTableStream.nextLine();
		String[] tokens = line.split("\\s+");
		probeNames = Arrays.asList(tokens);
		// Check for if probe names are correct
		checkProbeNames(probeNames);

		while (probeScanTableStream.hasNextLine()) {
			line = probeScanTableStream.nextLine();
			tokens = line.split("\\s+"); // split on whitespace
			List<String> values = Arrays.asList(tokens);

			// Validate lengths
			if (probeNames.size() != values.size()) {
				probeScanTableStream.close();
				throw new NotificationException(
						"Probe scan table: Line Length Mismatch");
			}

			scanTable.add(values);
		}
		probeScanTableStream.close();
	}

	private void checkProbeNames(List<String> asList) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer setNextStep(Scenario model) throws NotificationException {

		// model.resetProbe();

		EnvelopeProbe probe = (EnvelopeProbe) model.getProbe();
		Integer step = getStepNumber();

		Twiss xTwiss = new Twiss(getValue(step, "alphaX"), getValue(step,
				"betaX"), getValue(step, "emitX"));
		Twiss yTwiss = new Twiss(getValue(step, "alphaY"), getValue(step,
				"betaY"), getValue(step, "emitY"));
		Twiss zTwiss = new Twiss(getValue(step, "alphaZ"), getValue(step,
				"betaZ"), getValue(step, "emitZ"));

		Twiss[] twissArray = { xTwiss, yTwiss, zTwiss };

		// TODO Does the offset issue (documented in initFromTwiss()) affect
		// this usage?
		probe.setKineticEnergy(getValue(step, "KE"));
		probe.initFromTwiss(twissArray);

		incrementStepNumber();
		return 0;
	}

	private Double getValue(Integer step, String name) {
		return Double.parseDouble(scanTable.get(step).get(map.get(name)));
	}

	@Override
	public boolean checkThresholds() {
		// TODO Auto-generated method stub
		return true;
	}

}
