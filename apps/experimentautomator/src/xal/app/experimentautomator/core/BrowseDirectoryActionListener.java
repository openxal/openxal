package xal.app.experimentautomator.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class BrowseDirectoryActionListener implements ActionListener {

	JFileChooser chooser;
	JTextField textField;

	public BrowseDirectoryActionListener(JTextField jtf) {
		textField = jtf;
	}

	public void actionPerformed(ActionEvent e) {
		chooser = new JFileChooser();
		int r = chooser.showOpenDialog(new JFrame());
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		System.out.println(chooser.isDirectorySelectionEnabled() + "");

		if (r == JFileChooser.APPROVE_OPTION) {
			textField.setText(chooser.getCurrentDirectory() + "");
			textField.setText("" + chooser.getSelectedFile());
		}

	}

}
