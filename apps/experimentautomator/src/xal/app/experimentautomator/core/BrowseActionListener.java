package xal.app.experimentautomator.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class BrowseActionListener implements ActionListener {

	private JTextField fileTextField;
	JFileChooser chooser;
	String fileName;

	public BrowseActionListener(JTextField textField) {
		fileTextField = textField;
	}

	public void actionPerformed(ActionEvent event) {
		chooser = new JFileChooser();
		int r = chooser.showOpenDialog(new JFrame());
		fileTextField.setText(fileName);
		if (r == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getPath();
		}
		fileTextField.setText(fileName);
	}
}
