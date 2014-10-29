package xal.app.experimentautomator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import xal.app.experimentautomator.core.ExperimentConfig;
import xal.app.experimentautomator.exception.NotificationException;
import xal.app.experimentautomator.table.ElementScan;
import xal.ca.Channel;
import xal.ca.ChannelFactory;

/**
 * JUnit test cases for class <code>ElementScan.java</code>.
 * 
 * 
 * @author rnewhouse
 * @since Oct 8, 2014
 */
@RunWith(JUnit4.class)
public class TestElementScan {

	/**
	 * Global Attributes
	 */

	private ExperimentConfig config;
	private ElementScan elScan;

	@BeforeClass
	public static void buildTestingResources() {
		System.out.println("Before");
	}

	/**
	 * TRIUMF ELINAC specific test. Checks if the Java Channel Access can open
	 * and read a commonly used channel
	 */
	@Test
	public void checkJcaConnection() {
		ChannelFactory cf = ChannelFactory.defaultFactory();
		Channel ch = cf.getChannel("EGUN:SOL1:CUR");
		assertTrue("Can connect to JCA", ch.connectAndWait());
	}

	/**
	 * Reads a predefined test file and compares the recorded fields
	 */
	@Test
	public void testReadFields() {
		testReadFile("TestScanNoControl.txt");
		readFields();
		testReadFile("TestScanOneControl.txt");
		readFields();
		testReadFile("TestScanTwoControl.txt");
		readFields();
	}

	/**
	 * Reads a test file
	 */
	public void testReadFile(String filename) {

		URL url = getClass().getResource(filename);
		File file = new File(url.getPath());
		this.config = new ExperimentConfig(file);

		try {
			elScan = new ElementScan(config);
		} catch (NotificationException e) {
			fail(e.getMessage());
		}
	}

	private void readFields() {

		/** line 1: List of PVs to set */
		List<String> pvNamesComp = new ArrayList<String>();
		/** line 2: List of PVs for threshold checking & initial value recording */
		List<String> thresholdPVsComp = new ArrayList<String>();
		/** line 3: Values used in comparison for threshold checking. */
		List<Double> thresholdValuesComp = new ArrayList<Double>();
		/**
		 * line 4: Method used to check thresholds, may be "DIFFERENCE" or
		 * "RATIO
		 */
		List<String> thresholdCheckMethodComp = new ArrayList<String>();

		/** Test the pvNames field */
		pvNamesComp.add("EGUN:SOL1:CUR");
		pvNamesComp.add("EGUN:XCB0:INTFLDREQ");
		pvNamesComp.add("EGUN:YCB0:INTFLDREQ");
		assertEquals(pvNamesComp, elScan.getPVs());

		/** Test the thresholdPVs field */
		thresholdPVsComp.add("EGUN:SOL1:RDCUR");
		thresholdPVsComp.add("EGUN:XCB0:RDINTFLD");
		thresholdPVsComp.add("EGUN:YCB0:RDINTFLD");
		assertEquals(thresholdPVsComp, elScan.getThresholdPVs());

		/** Test the thresholdValues field */
		thresholdValuesComp.add(0.05);
		thresholdValuesComp.add(0.02);
		thresholdValuesComp.add(0.03);
		assertEquals(thresholdValuesComp, elScan.getThresholdValues());

		/** Test threshold check method */
		thresholdCheckMethodComp.add("DIFFERENCE");
		thresholdCheckMethodComp.add("RATIO");
		thresholdCheckMethodComp.add("IGNORE");
		assertEquals(thresholdCheckMethodComp, elScan.getThresholdCheckMethod());

	}

	/**
	 * Executes a single step of the program
	 */
	@Test
	public void testExecuteSingleStep() {
		testReadFile("TestScanTwoControl.txt");
		try {
			elScan.setNextStep(null);
		} catch (NotificationException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Executes a single step of the program
	 */
	@Test
	public void testExecuteAllSteps() {
		testReadFile("TestScanTwoControl.txt");
		while (!elScan.isComplete()) {
			try {
				elScan.setNextStep(null);
				elScan.incrementStepNumber();
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}

	}
}
