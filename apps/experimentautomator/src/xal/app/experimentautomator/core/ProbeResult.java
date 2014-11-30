package xal.app.experimentautomator.core;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import xal.app.experimentautomator.exception.NotificationException;
import xal.model.IElement;
import xal.model.elem.Element;
import xal.model.probe.Probe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.CovarianceMatrix;

public class ProbeResult extends AbstractTableModel {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    // private static final int DECIMAL_PLACES = 8;
	private CovarianceMatrix correlationMatrix;
	private String[] probeList;
	AcceleratorSeq seq;
	Scenario model;
	// private Accelerator accel;
	// private EADocument EADoc;
	private SortedMap<Integer, Map<String, Double[]>> resultMap;
	List<String> nodeList;
	private ArrayList<ArrayList<String>> data;

	public ProbeResult(Scenario model, AcceleratorSeq sequence,
			List<String> nodeList) throws NotificationException {

		this.model = model;
		this.seq = sequence;
		this.nodeList = nodeList;
		resultMap = new TreeMap<>();

		// loadSequence();
		// initializeModel();

		probeList = new String[] { "11", "12", "13", "14", "15", "16", "17",
				"22", "23", "24", "25", "26", "27", "33", "34", "35", "36",
				"37", "44", "45", "46", "47", "55", "56", "57", "66", "67",
				"77" };

	}

	public Double[] getResult() {

		Double[][] matrix = new Double[CovarianceMatrix.INT_SIZE][CovarianceMatrix.INT_SIZE];
		for (int r = 0; r < CovarianceMatrix.INT_SIZE; r++)
			for (int c = 0; c < CovarianceMatrix.INT_SIZE; c++)
				matrix[r][c] = correlationMatrix.getElem(r, c);

		ResultsCalculator ResultsCalculator = new ResultsCalculator(probeList,
				matrix, probeList.length);

		return ResultsCalculator.collectData();
	}

	public void fillCurrentStepResults(Integer stepNumber)
			throws NotificationException {
		Probe<?> probe = model.getProbe();
		Trajectory<?> traj = probe.getTrajectory();
		SortedMap<String, Double[]> currentStepResultMap = new TreeMap<String, Double[]>();

		for (String device : nodeList) {

			AcceleratorNode node = seq.getNodeWithId(device);
			if (node == null)
				throw new NotificationException("Cannot retrieve node: "
						+ device);
			List<IElement> elems = model.elementsMappedTo(node);
			if (elems == null)
				throw new NotificationException(
						"No elements associated with node: " + device);
			Object elem = elems.get(elems.size() - 1);

			ProbeState<?> state = traj.stateForElement(((Element) elem).getId());

			correlationMatrix = ((EnvelopeProbeState) state)
					.getCovarianceMatrix();

			// Prepare Results
			currentStepResultMap.put(device, getResult());
		}

		// Store results
		resultMap.put(stepNumber, currentStepResultMap);
	}

	public void displayResults(JFrame resultsTableFrame,
			Component resultComponent) {

		// Flatten array to 2D
		renderArray();

		try {
			// resultComponent saved as field to allow future removal
			resultsTableFrame.remove(resultComponent);
		} catch (Exception e) {
		}

		// resultsTable = new ResultsTable(resultMap, pvAcq.getPVs());
		JTable table = new JTable(this);
		table.setPreferredScrollableViewportSize(new Dimension(900, 300));
		JScrollPane scrollPane = new JScrollPane(table);
		resultComponent = resultsTableFrame.add(scrollPane);
		// JScrollBar vertical = scrollPane.getVerticalScrollBar();
		// vertical.setValue(vertical.getMaximum());

		// Display the window.
		resultsTableFrame.pack();
		resultsTableFrame.setVisible(true);
	}

	private void renderArray() {

		data = new ArrayList<ArrayList<String>>();
		ArrayList<String> line;

		for (Integer step : resultMap.keySet()) {

			// Add step column
			line = new ArrayList<String>();
			line.add(step.toString());
			for (int i = 0; i < probeList.length; i++) {
				line.add(""); // Filler space
			}
			data.add(line);

			// Add values in order of acquisition list nodes
			Map<String, Double[]> nodeList = resultMap.get(step);
			for (String node : nodeList.keySet()) {
				line = new ArrayList<>();
				line.add(node);
				Double[] currentNode = nodeList.get(node);
				for (int i = 0; i < probeList.length; i++) {
					line.add(new DecimalFormat("0.00000000")
							.format(currentNode[i]));
				}
				data.add(line);
			}
		}
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return data.get(0).size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex).get(columnIndex);
	}

	@Override
	public String getColumnName(int col) {
		if (col == 0)
			return "Step";
		else
			return probeList[col - 1];
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		data.get(row).set(col, value.toString());
	}

	public File saveData(ExperimentConfig config) throws NotificationException {

		renderArray();

		String dateString = new SimpleDateFormat("yyyy-MM-dd-HHmmss")
				.format(config.getExperimentTime());
		File saveLocation = new File(config.getElementScanTablePath()
				.toString() + "." + dateString + ".probe" + ".csv"); 

		// Write data to CSV
		PrintWriter writer;
		try {
			writer = new PrintWriter(saveLocation, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			throw new NotificationException("Error Writing File "
					+ saveLocation.toString());
		}

		// Write columns
		writer.print("#Step,");
		writer.print("Node,");
		writer.print(join(Arrays.asList(probeList), ","));
		writer.println();

		for (Integer step : resultMap.keySet()) {
			// Add values in order of acquisition list nodes
			Map<String, Double[]> nodeList = resultMap.get(step);
			for (String node : nodeList.keySet()) {
				writer.print(step.toString() + ",");
				writer.print(node + ",");
				Double[] currentNode = nodeList.get(node);
				for (int i = 0; i < probeList.length - 1; i++) {
					writer.print(currentNode[i].toString() + ",");
				}
				// avoid trailing comma
				writer.print(currentNode[probeList.length - 1].toString());
				writer.println();
			}
		}
		writer.close();
		return saveLocation;
	}

	/**
	 * From http://dzone.com/snippets/join-java
	 */
	public static String join(Collection<String> s, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		Iterator<String> iter = s.iterator();
		while (iter.hasNext()) {
			buffer.append(iter.next());
			if (iter.hasNext()) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}
}
