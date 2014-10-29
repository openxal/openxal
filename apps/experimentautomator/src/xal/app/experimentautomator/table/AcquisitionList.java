package xal.app.experimentautomator.table;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import xal.app.experimentautomator.exception.NotificationException;

/**
 * A class valid for stoing both PV Acquisition Lists and Probe Acquisition
 * lists. The difference being PV Acquisition Elements appear as:
 * EGUN:SOL1:RDFIELD and Probe Acquisition elements appear as EGUN:SOL1
 * 
 * @author rnewhouse
 * 
 */
public class AcquisitionList {

	private List<String> pvNames;

	public AcquisitionList(File path) throws NotificationException {
		pvNames = new ArrayList<>();
		if (!path.isFile()) {
			throw new NotificationException(
					"Not a valid path: Acquisition List");
		}

		try {
			parseAcqList(path);
		} catch (FileNotFoundException e) {
			throw new NotificationException("File Not Found");
		}
	}

	private void parseAcqList(File file) throws FileNotFoundException,
			NotificationException {
		// Open Connection
		Scanner acqListStream = new Scanner(new FileInputStream(file));

		// Get PV names
		while (acqListStream.hasNextLine()) {
			String line = acqListStream.nextLine();
			String[] tokens = line.split("\\s+");
			pvNames.addAll(Arrays.asList(tokens));
		}
		acqListStream.close();
		pvNames.removeAll(Collections.singleton(""));
	}

	// Getters
	public List<String> getPVs() {
		return pvNames;
	}

	// Iterator
	public Iterator<String> iterator() {
		return pvNames.iterator();
	}

}
