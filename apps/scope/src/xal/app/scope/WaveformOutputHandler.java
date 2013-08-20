/*
 * WaveformOutputHandler.java
 *
 * Created on Mon Aug 25 09:12:54 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import java.io.*;
import java.awt.Component;
import javax.swing.*;


/**
 * WaveformOutputHandler is a utility for writing out a waveform snapshot to a text file. 
 *
 * @author  tap
 */
public class WaveformOutputHandler {
	/** The JFileChooser for saving the waveform snapshot to a text file. */
	static private JFileChooser fileChooser;
	
	
	static {
		fileChooser = new JFileChooser();
	}
	
	
	/**
	 * Write the raw waveform snapshot to a text file.  Queries the user for the output file and writes
	 * the raw waveforms to this output file.
	 * @param sender
	 * @param scopeModel
	 * @throws java.io.IOException if there was an error when writing the snapshot to the output file
	 */
	static void writeRawWaveformSnapshot(Component sender, ScopeModel scopeModel) throws java.io.IOException {
		String output = scopeModel.getRawWaveformSnapshot().toString();
		int status = fileChooser.showSaveDialog(sender);
		switch(status) {
			case JFileChooser.APPROVE_OPTION:
				File selectedFile = fileChooser.getSelectedFile();
				FileWriter writer = new FileWriter(selectedFile);
				writer.write(output);
				writer.flush();
				break;
			default:
				break;
		}
	}
}

