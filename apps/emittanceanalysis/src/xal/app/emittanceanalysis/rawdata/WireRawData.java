package xal.app.emittanceanalysis.rawdata;

import java.io.*;
import javax.swing.*;
import java.util.regex.*;
import java.awt.*;
import java.lang.reflect.*;

/**
 *  This class keeps and operates with raw wires data.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public final class WireRawData {

	//message text field
	private JTextField messageTextLocal = new JTextField();

	//number of wires
	private int nWires = 0;

	//number of channels.
	//Sometimes last two wires are used for different purposes,
	//so number of wires can be not equal to number of wires
	private int nChannels = 0;

	//number of the slit and harp positions
	private int nPos_Slit = 0;

	//number of the slit and harp positions
	private int nPos_Harp = 0;

	//number of samples
	private int nSamples = 0;

	//array of data
	private double[] rawData = new double[0];

	//max in values of data as table [wire][position_slit][position_harp]
	private double[][][] data_max = new double[0][0][0];

	//slit positions in mm [slit_pos]
	private double[] slit_pos = new double[0];

	//harp positions in mm [slit_pos,harp_pos]
	private double[][] harp_pos = new double[0][0];

	//coefficient for transformation inch to cm
	private double inch2mm = 25.4;

	//index with type of device from ReadRawDataPanel class
	//parameters for diff. indexes
	//0 - MEBT emittance device
	//1 - DTL  emittance device
	//2 - new (fall 2004) MEBT device
	//3 - cvs input file for new (fall 2004) MEBT device
	private int devType = 0;


	/**
	 *  WireRawData constructor.
	 */
	public WireRawData() { }


	/**
	 *  Reads data. User has to defined how many wires and channels where used.
	 *
	 *@param  file         The file with data
	 *@param  nWiresIn     The number of wires in the harp
	 *@param  nChannelsIn  The number of channels
	 *@return              The file has been read correctly (true or false)
	 */
	public boolean readData(File file, int nWiresIn, int nChannelsIn, double data_polarity) {

		messageTextLocal.setText(null);

		//readData1 for ASCII file
		//readData2 for CSV file
		boolean result = false;
		if (devType == 0 || devType == 1 || devType == 2) {
			result = readData1(file, nWiresIn, nChannelsIn, data_polarity);
		}
		if (devType == 3) {
			result = readData2(file, nWiresIn, nChannelsIn, data_polarity);
		}

		if (!result) {
			return false;
		}

		//set max values table
		double v_tmp = 0.;
		data_max = new double[nWires][nPos_Slit][nPos_Harp];
		for (int iw = 0; iw < nWires; iw++) {
			for (int ips = 0; ips < nPos_Slit; ips++) {
				for (int iph = 0; iph < nPos_Harp; iph++) {
					data_max[iw][ips][iph] = 0.;
					for (int is = 0; is < nSamples; is++) {
						v_tmp = Math.abs(getValue(is, ips, iph, iw));
						if (v_tmp > data_max[iw][ips][iph]) {
							data_max[iw][ips][iph] = v_tmp;
						}
					}
				}
			}
		}
		return true;
	}


	/**
	 *  Reads data from an ASCII file with numbers only. User has to defined how
	 *  many wires and channels where used.
	 *
	 *@param  file         The file with ASCII data
	 *@param  nWiresIn     The number of wires in the harp
	 *@param  nChannelsIn  The number of channels
	 *@return              The file has been read correctly (true or false)
	 */
	public boolean readData1(File file, int nWiresIn, int nChannelsIn, double data_polarity) {
		int nWires_new = nWiresIn;
		int nChannels_new = nChannelsIn;

		String fName = file.getName();
		BufferedReader bReader = null;

		try {
			bReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException exept) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Cannot find file:" + fName);
			Toolkit.getDefaultToolkit().beep();
			return false;
		}

		Pattern p = Pattern.compile("[,\\s]+");
		String[] dataS;
		String lineIn = null;

		//calculate file parameters - number of lines and number of elements

		int n_lines = 0;
		int n_elements = 0;
		int line_index = 0;

		int nPos_new = 0;
		int nSamples_new = 0;
		int n_avg = 1;
		try {
			lineIn = bReader.readLine();
			line_index++;
			n_elements = p.split(lineIn).length;
			while (lineIn != null) {
				dataS = p.split(lineIn);
				if (dataS.length != n_elements) {
					messageTextLocal.setText(null);
					messageTextLocal.setText("Bad file structure. Line #" + line_index + " file: " + fName);
					Toolkit.getDefaultToolkit().beep();
					return false;
				} else {
					n_lines++;
				}

				if (line_index == 1) {
					nSamples_new = Integer.parseInt(dataS[0]);
					n_avg = Integer.parseInt(dataS[1]);
				}

				lineIn = bReader.readLine();
				line_index++;
			}
			bReader.close();
		} catch (IOException exept) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Bad file structure. Line #" + line_index + " file: " + fName);
			Toolkit.getDefaultToolkit().beep();
			return false;
		} catch (NumberFormatException exept) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Bad file structure. Line #" + line_index + " file: " + fName);
			Toolkit.getDefaultToolkit().beep();
			return false;
		}

		nPos_new = n_elements;

		nWires = nWires_new;
		nChannels = nChannels_new;

		//System.out.println("debug nPos="+nPos_new);
		//System.out.println("debug n_avg="+n_avg);
		//System.out.println("debug nSamples="+nSamples_new);
		//System.out.println("debug nWires="+nWires);
		//System.out.println("debug n_lines="+n_lines);

		if (n_avg * nWires * nSamples_new != (n_lines - 3)) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Bad file structure, file: " + fName);
			Toolkit.getDefaultToolkit().beep();
			return false;
		}

		//-------------------------------------
		//Actual reading of the data
		//-------------------------------------
		try {
			bReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException exept) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Cannot find file: " + fName);
			Toolkit.getDefaultToolkit().beep();
			return false;
		}

		nPos_Slit = nPos_new;
		nPos_Harp = 1;
		nSamples = nSamples_new;

		slit_pos = new double[nPos_Slit];
		harp_pos = new double[nPos_Slit][nPos_Harp];
		rawData = new double[(n_lines - 3) * n_elements];

		double coeff_mult = inch2mm;
		if (devType == 2) {
			coeff_mult = 1.0;
		}

		line_index = 0;
		try {
			lineIn = bReader.readLine();
			line_index++;
			while (lineIn != null) {
				dataS = p.split(lineIn);

				if (line_index == 2) {
					for (int i = 0; i < n_elements; i++) {
						slit_pos[i] = coeff_mult * Double.parseDouble(dataS[i]);
						//System.out.println("debug i="+i+" slit=" + slit_pos[i]);
					}
				}
				if (line_index == 3) {
					for (int i = 0; i < n_elements; i++) {
						harp_pos[i][0] = coeff_mult * Double.parseDouble(dataS[i]);
						//System.out.println("debug i="+i+" harp=" + harp_pos[i]);
					}
				}
				if (line_index > 3) {
					for (int i = 0; i < n_elements; i++) {
						rawData[line_index - 4 + i * (n_lines - 3)] = data_polarity * Double.parseDouble(dataS[i]);
					}
				}

				lineIn = bReader.readLine();
				line_index++;
			}
			bReader.close();
		} catch (IOException exept) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Bad file structure. Line #" + line_index + " file: " + fName);
			Toolkit.getDefaultToolkit().beep();
			return false;
		}

		//averaging
		if (n_avg > 1) {
			int data_size = rawData.length;
			double w_avg = 0.;
			int n_sets = data_size / n_avg;
			for (int i = 0; i < n_sets; i++) {
				w_avg = 0.;
				for (int i_avg = 0; i_avg < n_avg; i_avg++) {
					w_avg += rawData[i_avg + n_avg * i];
				}
				rawData[i] = w_avg / n_avg;
			}
			rawData = (double[]) Array.newInstance(rawData.getClass().getComponentType(), n_sets);
		}
		//double v_tmp = 0.;
		//if (devType == 2) {
		//	//special case for new MEBT device
		//	for (int iw = 0; iw < nWires; iw++) {
		//		for (int ip = 0; ip < nPos_Slit; ip++) {
		//			for (int is = 0; is < nSamples; is++) {
		//				if (iw > 0 && (iw - 1) % 2 == 0) {
		//					v_tmp = getValue(is, ip, 0, iw - 1);
		//					setValue(getValue(is, ip,0, iw), is, ip, 0, iw - 1);
		//					setValue(v_tmp, is, ip, 0, iw);
		//				}
		//			}
		//		}
		//	}
		//}
		return true;
	}


	/**
	 *  Reads data from a comma separated values (CSV) file.
	 *
	 *@param  file         The file with CVS data
	 *@param  nWiresIn     The number of wires in the harp
	 *@param  nChannelsIn  The number of channels
	 *@return              The file has been read correctly (true or false)
	 */
	public boolean readData2(File file, int nWiresIn, int nChannelsIn, double data_polarity) {

		int nWires_new = nWiresIn;
		int nChannels_new = nChannelsIn;

		String fName = file.getName();
		BufferedReader bReader = null;

		try {
			bReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException exept) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Cannot find file:" + fName);
			Toolkit.getDefaultToolkit().beep();
			return false;
		}

		//read the header
		String lineIn = null;
		int line_index = 0;
		String header = "";
		boolean header_success = false;

		try {
			lineIn = bReader.readLine();
			line_index++;

			int line_max_in_header = 100;
			int line_in_header = 1;

			while (lineIn != null && line_in_header < line_max_in_header) {
				if (lineIn.trim().matches(".*?tart of Data.*?")) {
					header_success = true;
					break;
				}
				header = header + " " + lineIn;
				//System.out.println("Line:" + lineIn);
				lineIn = bReader.readLine();
				line_index++;
				line_in_header++;
			}
		} catch (IOException exept) {
		}

		if (!header_success) {
			messageTextLocal.setText("Cannot find the right header in the file:" + fName);
			Toolkit.getDefaultToolkit().beep();
			return false;
		}

		//get parameters from header
		double slit_pos_ini = getCSVParam("Slit Initial Pos", header).doubleValue();
		int slit_steps = (int) (getCSVParam("Slit Steps", header).doubleValue());
		double slit_step_size = getCSVParam("Slit Step Size", header).doubleValue();
		double harp_pos_ini = getCSVParam("Harp Initial Pos", header).doubleValue();
		int harp_steps = (int) (getCSVParam("Harp Steps", header).doubleValue());
		double harp_step_size = getCSVParam("Harp Step Size", header).doubleValue();
		double harp_step_slit_size = getCSVParam("Harp Step Slit Size", header).doubleValue();
		int nChannels_csv = (int) (getCSVParam("Chans", header).doubleValue());
		int nSamples_csv = (int) (getCSVParam("Samples", header).doubleValue());

		Pattern p = Pattern.compile(",");
		String[] dataS;
		lineIn = null;

		//set parameters
		int nPos_new = slit_steps;
		int nSamples_new = nSamples_csv;
		int n_avg = 1;

		if (nWiresIn != nChannels_csv ||
				nChannelsIn != nChannels_csv) {
			messageTextLocal.setText("Wrong header of the file: " + fName);
			Toolkit.getDefaultToolkit().beep();
			return false;
		}

		nWires = nChannels_csv;
		nChannels = nChannels_csv;

		//System.out.println("debug nPos="+nPos_new);
		//System.out.println("debug n_avg="+n_avg);
		//System.out.println("debug nSamples="+nSamples_new);
		//System.out.println("debug nWires="+nWires);
		//System.out.println("debug n_lines="+n_lines);

		//-------------------------------------
		//Actual reading of the data
		//-------------------------------------
		nPos_Slit = nPos_new;
		nPos_Harp = harp_steps;
		nSamples = nSamples_new;

		slit_pos = new double[nPos_Slit];
		harp_pos = new double[nPos_Slit][nPos_Harp];
		rawData = new double[nPos_Slit * nPos_Harp * nChannels * nSamples];

		try {
			for (int i_pos_slit = 0; i_pos_slit < nPos_Slit; i_pos_slit++) {
				for (int i_pos_harp = 0; i_pos_harp < nPos_Harp; i_pos_harp++) {
					slit_pos[i_pos_slit] = slit_pos_ini + i_pos_slit * slit_step_size;
					harp_pos[i_pos_slit][i_pos_harp] = harp_pos_ini + i_pos_slit * harp_step_slit_size + i_pos_harp * harp_step_size;
					//System.out.println("debug i="+i_pos+" slit=" + slit_pos[i_pos] + " harp="+harp_pos[i_pos]);
					for (int i_smpl = 0; i_smpl < nSamples; i_smpl++) {
						lineIn = bReader.readLine();
						line_index++;
						dataS = p.split(lineIn);

						if (dataS.length != nChannels_csv) {
							messageTextLocal.setText("Bad file Line #" + line_index + " file: " + fName);
							Toolkit.getDefaultToolkit().beep();
							return false;
						}

						for (int i_wire = 0; i_wire < dataS.length; i_wire++) {
							int i_chan = i_wire;
							rawData[i_smpl +
									nSamples * i_chan +
									nSamples * nChannels * i_pos_slit +
									nSamples * nChannels * nPos_Slit * i_pos_harp]
									 = data_polarity * Double.parseDouble(dataS[i_wire]);

						}
					}
				}
			}

			bReader.close();
		} catch (IOException exept) {
			messageTextLocal.setText("Bad file Line #" + line_index + " file: " + fName);
			Toolkit.getDefaultToolkit().beep();
			return false;
		}

		return true;
	}


	/**
	 *  Returns the double value from the text that is a CSV (comma separated
	 *  values) data.
	 *
	 *@param  paramName  The parameter name
	 *@param  text       The CSV text
	 *@return            The double object
	 */
	private Double getCSVParam(String paramName, String text) {
		String sP = paramName + "\\s*+,\\s*+(\\S++)\\s*+";
		Pattern p = Pattern.compile(sP);
		Matcher m = p.matcher(text);
		if (m.find() && m.groupCount() == 1) {
			return new Double(m.group(1));
		}
		return null;
	}


	/**
	 *  Returns raw data value for particular sample, position and wire.
	 *
	 *@param  sampleInd    The sample index
	 *@param  wireInd      The index of the wire in the harp
	 *@param  posInd_Slit  Description of the Parameter
	 *@param  posInd_Harp  Description of the Parameter
	 *@return              The waveform signal value
	 */
	public double getValue(int sampleInd, int posInd_Slit, int posInd_Harp, int wireInd) {
		int ind = sampleInd + nSamples * wireInd + nSamples * nWires * posInd_Slit + nSamples * nWires * nPos_Slit * posInd_Harp;
		return rawData[ind];
	}


	/**
	 *  Sets the value of the wire signal for particular sample, position and wire
	 *
	 *@param  val          The new value
	 *@param  sampleInd    The sample index
	 *@param  wireInd      The waveform signal value
	 *@param  posInd_Slit  The new value value
	 *@param  posInd_Harp  The new value value
	 */
	private void setValue(double val, int sampleInd, int posInd_Slit, int posInd_Harp, int wireInd) {
		int ind = sampleInd +
				nSamples * wireInd +
				nSamples * nWires * posInd_Slit +
				nSamples * nWires * nPos_Slit * posInd_Harp;

		rawData[ind] = val;
	}


	/**
	 *  Returns the maximal value in the sample for particular wire and position.
	 *
	 *@param  posInd_Harp  Description of the Parameter
	 *@param  posInd_Slit  Description of the Parameter
	 *@param  wireInd      Description of the Parameter
	 *@return              The maximal value of signal
	 */
	public double getMaxValue(int posInd_Slit, int posInd_Harp, int wireInd) {
		return data_max[wireInd][posInd_Slit][posInd_Harp];
	}


	/**
	 *  Returns number of samples.
	 *
	 *@return    The number of samples
	 */
	public int getSamplesNumber() {
		return nSamples;
	}


	/**
	 *  Returns number of slit positions.
	 *
	 *@return    The number of slit positions
	 */
	public int getPositionsNumberSlit() {
		return nPos_Slit;
	}


	/**
	 *  Returns number of harp positions.
	 *
	 *@return    The number of harp positions
	 */
	public int getPositionsNumberHarp() {
		return nPos_Harp;
	}


	/**
	 *  Returns number of wires.
	 *
	 *@return    The number of wires in the harp
	 */
	public int getWiresNumber() {
		return nWires;
	}


	/**
	 *  Returns position of the harp.
	 *
	 *@param  indS  The index of the slit position
	 *@param  indH  The index of the harp position
	 *@return       The harp position
	 */
	public double getHarpPos(int indS, int indH) {
		return harp_pos[indS][indH];
	}


	/**
	 *  Returns position of the slit.
	 *
	 *@param  indS  The index of the slit position
	 *@return       The slit positions
	 */
	public double getSlitPos(int indS) {
		return slit_pos[indS];
	}


	/**
	 *  Returns number of channels. It is less than number of wires, because wires
	 *  with indexes 30-31 are used for other purposes.
	 *
	 *@return    The number of channels
	 */
	public int getChannelsNumber() {
		return nChannels;
	}


	/**
	 *  Sets the device type. This index has to be set from the ReadRawDataPanel
	 *  class
	 *
	 *@param  devType  The new device type index
	 */
	public void setDeviceType(int devType) {
		this.devType = devType;
	}



	/**
	 *  Returns the message text field.
	 *
	 *@return    The message text field
	 */
	public JTextField getMessageTextField() {
		return messageTextLocal;
	}


	/**
	 *  Sets the message text field.
	 *
	 *@param  messageTextLocal  The new message text field
	 */
	public void setMessageTextField(JTextField messageTextLocal) {
		this.messageTextLocal = messageTextLocal;
	}

}

