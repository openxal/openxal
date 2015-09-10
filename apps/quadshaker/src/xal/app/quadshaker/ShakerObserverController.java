package xal.app.quadshaker;

import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;

import xal.extension.scan.UpdatingEventController;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.tools.apputils.*;
import xal.extension.widgets.swing.*;
import xal.tools.text.ScientificNumberFormat;

import xal.extension.fit.lsm.Polynomial;

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class ShakerObserverController {

	//main panel
	private JPanel shakerObserverMainPanel = new JPanel();

	//Updating controller
	UpdatingEventController updatingController = null;

	//Tables and List models for BPMs and Quads
	private QuadsTable quadsTable = null;
	private BPMsTable bpmsTable = null;

	//the graphs panel to show the scan results
	private FunctionGraphsJPanel graphPanel = new FunctionGraphsJPanel();
	private BasicGraphData graphXData = new BasicGraphData();
	private BasicGraphData graphYData = new BasicGraphData();

	//left panel elements
	private JLabel titleOfListsLabel = new JLabel("======== Quads and BPMs =========");
	private TitledBorder quadListBorder = null;
	private TitledBorder bpmListBorder = null;

	private JList<Quad_Element> quadList = new JList<Quad_Element>(new DefaultListModel<Quad_Element>());
	private JList<BPM_Element> bpmList = new JList<BPM_Element>(new DefaultListModel<BPM_Element>());
	private ElementCellRenderer elmCellRend = new ElementCellRenderer();

	//analysis part
	private JLabel analysisResXLabel = new JLabel("Analysis for H(X):", JLabel.LEFT);
	private JLabel analysisResYLabel = new JLabel("Analysis for V(Y):", JLabel.LEFT);
	private JTextArea analysisXText = new JTextArea(2, 120);
	private JTextArea analysisYText = new JTextArea(2, 120);
	private ScientificNumberFormat numberFormat = new ScientificNumberFormat( 5, 10, false );
	//LSQ Fitters
	private Polynomial polinom_x_fitter = new Polynomial();
	private Polynomial polinom_y_fitter = new Polynomial();

	private BasicGraphData graphFitXData = new BasicGraphData();
	private BasicGraphData graphFitYData = new BasicGraphData();

	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();


	/**
	 *  Constructor for the ShakerObserverController object
	 *
	 *@param  updatingController_in  The Parameter
	 */
	public ShakerObserverController(UpdatingEventController updatingController_in) {

		Border border = BorderFactory.createEtchedBorder();

		updatingController = updatingController_in;

		shakerObserverMainPanel.setLayout(new BorderLayout());

		//define graph panel's properties
		SimpleChartPopupMenu.addPopupMenuTo(graphPanel);
		graphPanel.setOffScreenImageDrawing(true);
		graphPanel.setName("BPMs Readings vs. Quad RB Filed");
		graphPanel.setAxisNames("Quad G, [T/m]", "X,Y, mm");
		graphPanel.setGraphBackGroundColor(Color.white);
		graphPanel.setLegendButtonVisible(true);
		graphPanel.setLegendBackground(Color.white);
		graphPanel.addGraphData(graphXData);
		graphPanel.addGraphData(graphFitXData);
		graphPanel.addGraphData(graphYData);
		graphPanel.addGraphData(graphFitYData);
		graphPanel.setSmartGL(true);

		graphXData.setDrawLinesOn(false);
		graphXData.setGraphColor(Color.blue);
		graphXData.setGraphPointSize(5);
		graphXData.setImmediateContainerUpdate(false);
		graphXData.setGraphProperty(graphPanel.getLegendKeyString(), "null null");

		graphYData.setDrawLinesOn(false);
		graphYData.setGraphColor(Color.red);
		graphYData.setGraphPointSize(5);
		graphYData.setImmediateContainerUpdate(false);
		graphYData.setGraphProperty(graphPanel.getLegendKeyString(), "null null");

		graphFitXData.setDrawLinesOn(true);
		graphFitXData.setDrawPointsOn(false);
		graphFitXData.setGraphColor(Color.blue);
		graphFitXData.setGraphPointSize(3);
		graphFitXData.setImmediateContainerUpdate(false);
		graphFitXData.setGraphProperty(graphPanel.getLegendKeyString(), "Fit X");

		graphFitYData.setDrawLinesOn(true);
		graphFitYData.setDrawPointsOn(false);
		graphFitYData.setGraphColor(Color.red);
		graphFitYData.setGraphPointSize(3);
		graphFitYData.setImmediateContainerUpdate(false);
		graphFitYData.setGraphProperty(graphPanel.getLegendKeyString(), "Fit Y");

		analysisResXLabel.setForeground(Color.blue);
		analysisResYLabel.setForeground(Color.red);

		JPanel panel_G = new JPanel(new BorderLayout());
		panel_G.setBorder(border);
		panel_G.add(graphPanel, BorderLayout.CENTER);
		shakerObserverMainPanel.add(panel_G, BorderLayout.CENTER);

		JPanel panel_Anl = new JPanel(new BorderLayout());

		JPanel panel_Anl_Lbl = new JPanel(new BorderLayout());
		panel_Anl_Lbl.add(analysisResXLabel, BorderLayout.NORTH);
		panel_Anl_Lbl.add(analysisResYLabel, BorderLayout.SOUTH);

		JPanel panel_Anl_Txt = new JPanel(new BorderLayout());
		panel_Anl_Txt.setBorder(border);
		panel_Anl_Txt.add(analysisXText, BorderLayout.NORTH);
		panel_Anl_Txt.add(analysisYText, BorderLayout.SOUTH);

		analysisXText.setForeground(Color.blue);
		analysisYText.setForeground(Color.red);

		panel_Anl.add(panel_Anl_Lbl, BorderLayout.NORTH);
		panel_Anl.add(panel_Anl_Txt, BorderLayout.SOUTH);

		panel_G.add(panel_Anl, BorderLayout.SOUTH);

		quadListBorder = BorderFactory.createTitledBorder(border, "quads list");
		bpmListBorder = BorderFactory.createTitledBorder(border, "bpms list");

		JPanel panel_l0 = new JPanel(new BorderLayout());
		panel_l0.add(titleOfListsLabel, BorderLayout.CENTER);

		JPanel panel_l = new JPanel(new BorderLayout());

		JPanel panel_lT = new JPanel(new GridLayout(2, 1, 1, 1));

		panel_l.add(panel_l0, BorderLayout.NORTH);
		panel_l.add(panel_lT, BorderLayout.CENTER);

		shakerObserverMainPanel.add(panel_l, BorderLayout.WEST);

		JPanel panel_lT0 = new JPanel(new BorderLayout());
		JPanel panel_lT1 = new JPanel(new BorderLayout());

		panel_lT0.setBorder(quadListBorder);
		panel_lT1.setBorder(bpmListBorder);

		panel_lT.add(panel_lT0);
		panel_lT.add(panel_lT1);

		JScrollPane scrollPane0 = new JScrollPane(quadList);
		JScrollPane scrollPane1 = new JScrollPane(bpmList);
		panel_lT0.add(scrollPane0);
		panel_lT1.add(scrollPane1);

		quadList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bpmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		quadList.setCellRenderer(elmCellRend);
		bpmList.setCellRenderer(elmCellRend);

		//listeners
		ListSelectionListener selectListener =
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					//actions to show the graph data
					showGraphData();
				}
			};

		quadList.addListSelectionListener(selectListener);
		bpmList.addListSelectionListener(selectListener);

		//update panel when it will show up
		shakerObserverMainPanel.addComponentListener(
			new ComponentAdapter() {
				public void componentShown(ComponentEvent e) {
					showGraphData();
				}
			});
	}


	/**
	 *  Returns the panel attribute of the ShakerObserverController object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return shakerObserverMainPanel;
	}

	/**
	 *  Sets the tableModels attribute of the ShakerObserverController object
	 *
	 *@param  quadsTable  The new tableModels value
	 *@param  bpmsTable   The new tableModels value
	 */
	public void setTableModels(QuadsTable quadsTable, BPMsTable bpmsTable) {
		this.quadsTable = quadsTable;
		this.bpmsTable = bpmsTable;
		quadList.setModel(quadsTable.getListModel());
		bpmList.setModel(bpmsTable.getListModel());
	}

	/**
	 *  Description of the Method
	 */
	public void update() {
	}


	/**
	 *  Constructor for the showGraphData object
	 */
	public void showGraphData() {
		messageTextLocal.setText(null);
		int quad_ind = quadList.getSelectedIndex();
		int bpm_ind = bpmList.getSelectedIndex();
		if(quad_ind >= 0 && bpm_ind >= 0) {
			Quad_Element quadElm = quadsTable.getListModel().elementAt(quad_ind);
			BPM_Element bpmElm =  bpmsTable.getListModel().elementAt(bpm_ind);
			if(quadElm.isActive() && bpmElm.isActive()) {
				//prepare data and update graph panel
				graphXData.removeAllPoints();
				graphFitXData.removeAllPoints();
				graphXData.setGraphProperty(graphPanel.getLegendKeyString(),
						" X-pos Q=" + quadElm.getName() +
						" BPM=" + bpmElm.getName() + " ");
				graphYData.removeAllPoints();
				graphFitYData.removeAllPoints();
				graphYData.setGraphProperty(graphPanel.getLegendKeyString(),
						" Y-pos Q=" + quadElm.getName() +
						" BPM=" + bpmElm.getName() + " ");
				Iterator<QuadMeasure> itr = quadElm.getMeasures().iterator();
				while(itr.hasNext()) {
					QuadMeasure quadMeasure = itr.next();
					for(int i = 0, nBPMs = quadMeasure.getSize(); i < nBPMs; i++) {
						if(bpmElm == quadMeasure.getBPM_Element(i)) {
							double f = quadMeasure.getRBField();
							double x_bpm = quadMeasure.getXPos(i);
							double y_bpm = quadMeasure.getYPos(i);
							graphXData.addPoint(f, x_bpm);
							graphYData.addPoint(f, y_bpm);
						}
					}
				}
				performAnalysis(quadElm);
				graphPanel.refreshGraphJPanel();
			} else {
				clearGraphData();
			}
		} else {
			clearGraphData();
		}
	}


	/**
	 *  Clear the graph region of the sub-panel.
	 */
	public void clearGraphData() {
		graphXData.removeAllPoints();
		graphFitXData.removeAllPoints();
		graphXData.setGraphProperty(graphPanel.getLegendKeyString(), "X-pos null null");
		graphYData.removeAllPoints();
		graphFitYData.removeAllPoints();
		graphYData.setGraphProperty(graphPanel.getLegendKeyString(), "Y-pos null null");
		graphPanel.refreshGraphJPanel();
		messageTextLocal.setText(null);
		messageTextLocal.setForeground(Color.red);

		analysisResXLabel.setText("Analysis for H(X):");
		analysisResYLabel.setText("Analysis for V(Y):");
		analysisXText.setText(null);
		analysisYText.setText(null);
	}

	/**
	 *  Description of the Method
	 *
	 *@param  quadElm  The Parameter
	 */
	private void performAnalysis(Quad_Element quadElm) {

		polinom_x_fitter.clear();
		polinom_y_fitter.clear();
		graphFitXData.removeAllPoints();
		graphFitYData.removeAllPoints();

		for(int i = 0, n = graphXData.getNumbOfPoints(); i < n; i++) {
			polinom_x_fitter.addData(graphXData.getX(i), graphXData.getY(i));
		}
		for(int i = 0, n = graphYData.getNumbOfPoints(); i < n; i++) {
			polinom_y_fitter.addData(graphYData.getX(i), graphYData.getY(i));
		}

		polinom_x_fitter.fit();
		polinom_y_fitter.fit();

		double a_x = polinom_x_fitter.getParameter(0);
		double a_y = polinom_y_fitter.getParameter(0);
		double coef_x = polinom_x_fitter.getParameter(1);
		double coef_y = polinom_y_fitter.getParameter(1);
		double coef_x_err = polinom_x_fitter.getParameterError(1);
		double coef_y_err = polinom_y_fitter.getParameterError(1);

		for(int i = 0, n = graphXData.getNumbOfPoints(); i < n; i++) {
			graphFitXData.addPoint(graphXData.getX(i), a_x + coef_x * graphXData.getX(i));
		}
		for(int i = 0, n = graphYData.getNumbOfPoints(); i < n; i++) {
			graphFitYData.addPoint(graphYData.getX(i), a_y + coef_y * graphYData.getX(i));
		}

		//----------------------------------------------------------
		//prepare the labels
		//----------------------------------------------------------

		HashMap<BPM_Element, Double> coefXmap = quadElm.getSensitivityCoefsX();
		HashMap<BPM_Element, Double> coefYmap = quadElm.getSensitivityCoefsY();
		HashMap<BPM_Element, Double> coefErrXmap = quadElm.getSensitivityCoefsErrX();
		HashMap<BPM_Element, Double> coefErrYmap = quadElm.getSensitivityCoefsErrY();

		//find best coeffs (abs(value)/err ratio)
		double ratio_treshold = 2.5;

		double coef_best_x = 0.;
		double coef_best_x_err = 0.;
		double coef_max_x = 0.;
		double coef_max_x_err = 0.;
		double ratio_best_x = 0.;
		String bestBPM_x = "None";
		String maxBPM_x = "None";
		Iterator<BPM_Element> itr_x = coefXmap.keySet().iterator();
		while(itr_x.hasNext()) {
			BPM_Element bpmElm =  itr_x.next();
			double coef_tmp_x = ( coefXmap.get(bpmElm)).doubleValue();
			double coef_tmp_x_err = ( coefErrXmap.get(bpmElm)).doubleValue();
			if(coef_tmp_x_err > 0.) {
				double ratio_tmp_x = Math.abs(coef_tmp_x) / coef_tmp_x_err;
				if(ratio_tmp_x > ratio_best_x) {
					ratio_best_x = ratio_tmp_x;
					bestBPM_x = bpmElm.getName();
					coef_best_x = coef_tmp_x;
					coef_best_x_err = coef_tmp_x_err;
				}
				if(ratio_tmp_x > ratio_treshold) {
					if(Math.abs(coef_tmp_x) > Math.abs(coef_max_x)) {
						coef_max_x = coef_tmp_x;
						coef_max_x_err = coef_tmp_x_err;
						maxBPM_x = bpmElm.getName();
					}
				}
			}
		}

		double coef_best_y = 0.;
		double coef_best_y_err = 0.;
		double coef_max_y = 0.;
		double coef_max_y_err = 0.;
		double ratio_best_y = 0.;
		String bestBPM_y = "None";
		String maxBPM_y = "None";
		Iterator<BPM_Element> itr_y = coefYmap.keySet().iterator();
		while(itr_y.hasNext()) {
			BPM_Element bpmElm = itr_y.next();
			double coef_tmp_y = (coefYmap.get(bpmElm)).doubleValue();
			double coef_tmp_y_err = (coefErrYmap.get(bpmElm)).doubleValue();
			if(coef_tmp_y_err > 0.) {
				double ratio_tmp_y = Math.abs(coef_tmp_y) / coef_tmp_y_err;
				if(ratio_tmp_y > ratio_best_y) {
					ratio_best_y = ratio_tmp_y;
					bestBPM_y = bpmElm.getName();
					coef_best_y = coef_tmp_y;
					coef_best_y_err = coef_tmp_y_err;
				}
				if(ratio_tmp_y > ratio_treshold) {
					if(Math.abs(coef_tmp_y) > Math.abs(coef_max_y)) {
						coef_max_y = coef_tmp_y;
						coef_max_y_err = coef_tmp_y_err;
						maxBPM_y = bpmElm.getName();
					}
				}
			}
		}
		analysisXText.setText(null);
		analysisXText.setText("X: best BMP:" + bestBPM_x + " d(X)/d(G) =" +
				numberFormat.format(coef_best_x) + " +- " +
				numberFormat.format(coef_best_x_err) +
				System.getProperties().getProperty("line.separator") +
				"X: max BMP:" + maxBPM_x + " d(X)/d(G) =" +
				numberFormat.format(coef_max_x) + " +- " +
				numberFormat.format(coef_max_x_err));

		analysisYText.setText(null);
		analysisYText.setText("Y: best BMP:" + bestBPM_y + " d(Y)/d(G) =" +
				numberFormat.format(coef_best_y) + " +- " +
				numberFormat.format(coef_best_y_err) +
				System.getProperties().getProperty("line.separator") +
				"Y: max BMP:" + maxBPM_y + " d(Y)/d(G) =" +
				numberFormat.format(coef_max_y) + " +- " +
				numberFormat.format(coef_max_y_err));

		analysisResXLabel.setText("Analysis for H(X):" + " d(X)/d(G) =" +
				numberFormat.format(coef_x) + " +- " +
				numberFormat.format(coef_x_err));
		analysisResYLabel.setText("Analysis for V(Y):" + " d(Y)/d(G) =" +
				numberFormat.format(coef_y) + " +- " +
				numberFormat.format(coef_y_err));
	}


	/**
	 *  Sets the fontForAll attribute of the ShakerObserverController object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	public void setFontForAll(Font fnt) {
		quadList.setFont(fnt);
		bpmList.setFont(fnt);
		elmCellRend.setFont(fnt);
		titleOfListsLabel.setFont(fnt);
		quadListBorder.setTitleFont(fnt);
		bpmListBorder.setTitleFont(fnt);

		analysisResXLabel.setFont(fnt);
		analysisResYLabel.setFont(fnt);

		analysisXText.setFont(fnt);
		analysisYText.setFont(fnt);

	}


	/**
	 *  Returns the messageText attribute of the ShakerObserverController object
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}


	/**
	 *  Description of the Class
	 *
	 *@author     shishlo
	 */
	class ElementCellRenderer extends JLabel implements ListCellRenderer<Object> {
        
        /** ID for serializable version */
        private static final long serialVersionUID = 1L;
        
        
		/**
		 *  Constructor for the ElementCellRenderer object
		 */
		public ElementCellRenderer() {
			setOpaque(true);
		}

		/**
		 *  Returns the listCellRendererComponent attribute of the ElementCellRenderer
		 *  object
		 *
		 *@param  list          The Parameter
		 *@param  value         The Parameter
		 *@param  index         The Parameter
		 *@param  isSelected    The Parameter
		 *@param  cellHasFocus  The Parameter
		 *@return               The listCellRendererComponent value
		 */
        
         
		public Component getListCellRendererComponent(
				JList<?> list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus) {
			Dev_Element dev = (Dev_Element) value;
			setText(dev.getName());
			setBackground(dev.isActive() ? Color.white : Color.lightGray);
			setForeground((isSelected && dev.isActive()) ? Color.red : Color.blue);
			return this;
		}
	}
}

