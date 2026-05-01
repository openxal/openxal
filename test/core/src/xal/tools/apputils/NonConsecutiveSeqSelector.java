package xal.tools.apputils;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.*;
import java.awt.event.*;

import xal.tools.apputils.EdgeLayout;

/**
 * Non-consecutive sequence selector moved from db2xal app and can be used by
 * other apps. The other SeqSelector class is mainly for online model use and
 * the selected sequences have to be consecutive but this one does nopt require
 * so and this class does not prepare combo sequence but only a collection of
 * sequence names.
 * 
 * @author chu
 * 
 */
public class NonConsecutiveSeqSelector implements ActionListener {
	private boolean DEBUG = false;

	protected JDialog sequenceDialog;

	protected JTable table;

	protected java.util.ArrayList<Object> seqList;

	protected MyTableModel myModel;

	// private Db2XalDocument myDoc;

	public NonConsecutiveSeqSelector() {
		// myDoc = doc;
		myModel = new MyTableModel();
		table = new JTable(myModel);
		table.setPreferredScrollableViewportSize(new Dimension(300, 500));

	}

	public JDialog selectSequence() {
		sequenceDialog = new JDialog();
		sequenceDialog.setBounds(300, 300, 300, 300);
		sequenceDialog.setModal(true);
		sequenceDialog.setTitle("Select Sequence...");
		sequenceDialog.getContentPane().setLayout(new BorderLayout());

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel selectionDone = new JPanel();
		JButton done = new JButton("OK");
		JButton cancel = new JButton("Cancel");

		EdgeLayout edgeLayout = new EdgeLayout();
		selectionDone.setLayout(edgeLayout);
		edgeLayout.setConstraints(done, 0, 0, 200, 50, EdgeLayout.BOTTOM_RIGHT);
		selectionDone.add(done);
		edgeLayout.setConstraints(done, 0, 0, 150, 50, EdgeLayout.BOTTOM_RIGHT);
		selectionDone.add(cancel);
		done.setActionCommand("selectionDone");
		done.addActionListener(this);
		cancel.addActionListener(this);

		sequenceDialog.getContentPane().add(scrollPane, BorderLayout.WEST);
		sequenceDialog.getContentPane().add(selectionDone, BorderLayout.EAST);

		// sequenceDialog.addWindowListener(new WindowAdapter() {
		// public void windowClosing(WindowEvent e) {
		// System.exit(0);
		// }
		// });

		sequenceDialog.pack();
		sequenceDialog.setVisible(true);

		return sequenceDialog;
	}

	public synchronized void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("selectionDone")) {
			sequenceDialog.setVisible(false);

			seqList = new java.util.ArrayList<Object>();
			for (int i = 0; i < myModel.data.length; i++) {
				if (((Boolean) myModel.data[i][1]).booleanValue())
					seqList.add(myModel.data[i][0]);
			}

			// myDoc.setHasChanges(true);
		} else if (e.getActionCommand().equals("Cancel")) {
			sequenceDialog.setVisible(false);
		}

	}

	public java.util.ArrayList<Object> getSeqList() {
		return seqList;
	}

	class MyTableModel extends AbstractTableModel {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
		final String[] columnNames = { "Sequence", "Selected" };

		final Object[][] data = { { "LEBT", new Boolean(false) },
				{ "RFQ", new Boolean(false) }, { "MEBT", new Boolean(false) },
				{ "DTL1", new Boolean(false) }, { "DTL2", new Boolean(false) },
				{ "DTL3", new Boolean(false) }, { "DTL4", new Boolean(false) },
				{ "DTL5", new Boolean(false) }, { "DTL6", new Boolean(false) },
				{ "CCL1", new Boolean(false) }, { "CCL2", new Boolean(false) },
				{ "CCL3", new Boolean(false) }, { "CCL4", new Boolean(false) },
				{ "SCLMed", new Boolean(false) },
				{ "SCLHigh", new Boolean(false) },
				{ "HEBT1", new Boolean(false) },
				{ "LDmp", new Boolean(false) },
				{ "HEBT2", new Boolean(false) }, { "MDmp", new Boolean(false) },
				{ "IDmp-", new Boolean(false) },
				{ "IDmp+", new Boolean(false) },
				{ "Ring1", new Boolean(false) },
				{ "Ring2", new Boolean(false) },
				{ "Ring3", new Boolean(false) },
				{ "Ring4", new Boolean(false) },
				{ "Ring5", new Boolean(false) },
				{ "RTBT1", new Boolean(false) },
				{ "RTBT2", new Boolean(false) },
				{ "EDmp", new Boolean(false) }
		};

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col < 1) {
				return false;
			} else {
				return true;
			}
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		public void setValueAt(Object value, int row, int col) {
			if (DEBUG) {
				System.out.println("Setting value at " + row + "," + col
						+ " to " + value + " (an instance of "
						+ value.getClass() + ")");
			}

			int rowIndexStart = table.getSelectedRow();
			int rowIndexEnd = table.getSelectionModel().getMaxSelectionIndex();

			// for quick multiple sequence selection
			if (value.toString().equals("true")) {
				for (int i = rowIndexStart; i < rowIndexEnd; i++) {
					table.changeSelection(i, 1, false, false);
					setValueAt(new Boolean(true), i, 1);
					fireTableCellUpdated(i, 1);
				}
				// for quick multiple sequence un-selection
			} else {
				for (int i = rowIndexEnd - 1; i > rowIndexStart; i--) {
					table.changeSelection(i, 1, false, false);
					setValueAt(new Boolean(false), i, 1);
					fireTableCellUpdated(i, 1);
				}
			}

			if (data[0][col] instanceof Integer && !(value instanceof Integer)) {
				// With JFC/Swing 1.1 and JDK 1.2, we need to create
				// an Integer from the value; otherwise, the column
				// switches to contain Strings. Starting with v 1.3,
				// the table automatically converts value to an Integer,
				// so you only need the code in the 'else' part of this
				// 'if' block.
				// XXX: See TableEditDemo.java for a better solution!!!
				try {
					data[row][col] = new Integer(value.toString());
					fireTableCellUpdated(row, col);
				} catch (NumberFormatException e) {
					JFrame frame = new JFrame();
					JOptionPane.showMessageDialog(frame, "The \""
							+ getColumnName(col)
							+ "\" column accepts only integer values.");
				}
			} else {
				data[row][col] = value;
				fireTableCellUpdated(row, col);
			}

			if (DEBUG) {
				System.out.println("New value of data:");
				printDebugData();
			}
		}

		private void printDebugData() {
			int numRows = getRowCount();
			int numCols = getColumnCount();

			for (int i = 0; i < numRows; i++) {
				System.out.print("    row " + i + ":");
				for (int j = 0; j < numCols; j++) {
					System.out.print("  " + data[i][j]);
				}
				System.out.println();
			}
			System.out.println("--------------------------");
		}
	}

}
