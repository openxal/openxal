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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import xal.app.experimentautomator.exception.ChannelAccessException;
import xal.app.experimentautomator.exception.NotificationException;

public class PVResult extends AbstractTableModel {

    /** serialization ID */
    private static final long serialVersionUID = 1L;
//	private static final int DECIMAL_PLACES = 8;
	private SortedMap<Integer, Map<String, Double>> resultMap;
	private ArrayList<ArrayList<String>> data;
	private List<String> pvList;

	public PVResult(List<String> pvList) {
		resultMap = new TreeMap<>();
		this.pvList = pvList;
	}

	/**
	 * Flattens the maps of results for readable format
	 * 
	 * @param resultArray
	 * @return
	 */
	private void renderArray() {

		data = new ArrayList<ArrayList<String>>();
		ArrayList<String> line;

		for (Integer step : resultMap.keySet()) {
			// Add step column
			line = new ArrayList<String>();
			line.add(step.toString());

			// Add values in order of acquisition list PVs
			Map<String, Double> pvMap = resultMap.get(step);
			for (String pv : pvList) {
				line.add(new DecimalFormat("0.00000000").format(pvMap.get(pv)));
			}
			data.add(line);
		}
	}

	public void fillCurrentStepResults(Integer stepNumber)
			throws NotificationException {

		Map<String, Double> currentStepResults = new HashMap<>();

		try {
			currentStepResults = PVConnection.readPvValues(pvList);
		} catch (ChannelAccessException e) {
			throw new NotificationException(
					"ChannelAccessException while reading acquisition PVs");
		}

		// Add step to results table
		resultMap.put(stepNumber, currentStepResults);
	}

	public void displayResults(JFrame resultsTableFrame,
			Component resultComponent) {

		// Flatten the data into a 2D array
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
			return pvList.get(col - 1);
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
				.toString() + "." + dateString + ".pv" + ".csv");

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
		writer.print(join(pvList, ","));
		writer.println();
		// Write data
		for (int i = 0; i < getRowCount(); i++) {
			for (int j = 0; j < getColumnCount() - 1; j++) {
				writer.print(getValueAt(i, j));
				writer.print(",");
			}
			// avoid trailing comma
			writer.print(getValueAt(i, getColumnCount() - 1));
			writer.println();
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
