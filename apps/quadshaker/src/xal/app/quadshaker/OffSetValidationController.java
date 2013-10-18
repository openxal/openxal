package xal.app.quadshaker;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

import xal.extension.widgets.swing.*;

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class OffSetValidationController {

	private JPanel mainValidationPanel = new JPanel();

	private TitledBorder panelBorder = null;

	private JRadioButton isRatioLimitOnButton = new JRadioButton("Use validation by abs(Value)/Err limit", false);
	private DoubleInputTextField ratioLimitTextField = new DoubleInputTextField(8);

	private JRadioButton isMatrixElmLimitOnButton = new JRadioButton("Use validation by abs(TrMatrElm) limit", false);
	private DoubleInputTextField matrixElmLimitTextField = new DoubleInputTextField(8);


	/**
	 *  Constructor for the OffSetValidationController object
	 */
	public OffSetValidationController() {

		JPanel ratioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		ratioPanel.add(isRatioLimitOnButton);
		ratioPanel.add(ratioLimitTextField);

		JPanel matrixPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		matrixPanel.add(isMatrixElmLimitOnButton);
		matrixPanel.add(matrixElmLimitTextField);

		mainValidationPanel.setLayout(new BorderLayout());

		mainValidationPanel.add(ratioPanel, BorderLayout.NORTH);
		mainValidationPanel.add(matrixPanel, BorderLayout.SOUTH);

		Border border = BorderFactory.createEtchedBorder();
		panelBorder = BorderFactory.createTitledBorder(border, "offset validation");
		mainValidationPanel.setBorder(panelBorder);

		ratioLimitTextField.setValue(0.);
		matrixElmLimitTextField.setValue(0.);
	}

	/**
	 *  Returns the panel attribute of the OffSetValidationController object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return mainValidationPanel;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  quadElm  The Parameter
	 *@param  bpmElm   The Parameter
	 *@return          The Return Value
	 */
	public boolean validateX(Quad_Element quadElm, BPM_Element bpmElm) {
		boolean res = true;
		if(isRatioLimitOnButton.isSelected()) {
			HashMap<BPM_Element, Double> rationMap = quadElm.getRatioMapX();
			if(rationMap.containsKey(bpmElm)) {
				double ratio = (rationMap.get(bpmElm)).doubleValue();
				if(ratio < ratioLimitTextField.getValue()) {
					res = false;
				}
			} else {
				res = false;
			}
		}
		if(res) {
			if(isMatrixElmLimitOnButton.isSelected()) {
				HashMap<BPM_Element, Double>  elmMap = quadElm.getTrM01MapX();
				if(elmMap.containsKey(bpmElm)) {
					double elm = (elmMap.get(bpmElm)).doubleValue();
					if(Math.abs(elm) < matrixElmLimitTextField.getValue()) {
						res = false;
					}
				} else {
					res = false;
				}
			}
		}
		return res;
	}

	/**
	 *  Description of the Method
	 *
	 *@param  quadElm  The Parameter
	 *@param  bpmElm   The Parameter
	 *@return          The Return Value
	 */
	public boolean validateY(Quad_Element quadElm, BPM_Element bpmElm) {
		boolean res = true;
		if(isRatioLimitOnButton.isSelected()) {
			HashMap<BPM_Element, Double> rationMap = quadElm.getRatioMapY();
			if(rationMap.containsKey(bpmElm)) {
				double ratio = (rationMap.get(bpmElm)).doubleValue();
				if(ratio < ratioLimitTextField.getValue()) {
					res = false;
				}
			} else {
				res = false;
			}
		}
		if(res) {
			if(isMatrixElmLimitOnButton.isSelected()) {
				HashMap<BPM_Element, Double>  elmMap = quadElm.getTrM23MapY();
				if(elmMap.containsKey(bpmElm)) {
					double elm = (elmMap.get(bpmElm)).doubleValue();
					if(Math.abs(elm) < matrixElmLimitTextField.getValue()) {
						res = false;
					}
				} else {
					res = false;
				}
			}
		}
		return res;
	}

	/**
	 *  Sets the fontForAll attribute of the OffSetValidationController object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	public void setFontForAll(Font fnt) {
		isRatioLimitOnButton.setFont(fnt);
		ratioLimitTextField.setFont(fnt);

		isMatrixElmLimitOnButton.setFont(fnt);
		matrixElmLimitTextField.setFont(fnt);

		panelBorder.setTitleFont(fnt);
	}

}

