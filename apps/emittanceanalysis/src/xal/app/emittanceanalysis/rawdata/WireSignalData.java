package xal.app.emittanceanalysis.rawdata;

import java.text.*;
import java.util.regex.*;

import xal.tools.xml.*;
import xal.extension.widgets.plot.*;

/**
 *  This class keeps wires signals calculated from WireRawData. The value is the
 *  integral from a sample n1 to n2 minus average noise up to n0 samples. This
 *  class will used inside MakeRawToEmtancePanel class and can be stored and
 *  restored in/from a xml data file.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public final class WireSignalData {

	//string that identifies the data in XML structure
	private String xmlName = "WIRE_SIGNAL_DATA";

	//number of channels.
	//Sometimes last two wires are used for different purposes,
	//so number of wires can be not equal to number of wires
	private int nChannels = 0;

	//number of the slit positions
	private int nPos_Slit = 0;

	//number of the harp positions
	private int nPos_Harp = 0;

	//slit positions in mm [slit_pos]
	private double[] slit_pos = new double[0];

	//harp positions in mm  [slit_pos,harp_pos]
	private double[][] harp_pos = new double[0][0];

	//wires data as ColorSurfaceData instance
	//array of[harp_pos]
	private ColorSurfaceData[] wireSignal3D = new ColorSurfaceData[1];

	//Copy of wires data as ColorSurfaceData instance. It is used to restore initial data.
	//array of[harp_pos]
	private ColorSurfaceData[] memWireSignal3D = new ColorSurfaceData[1];

	private DecimalFormat dbl_Format = new DecimalFormat("0.0###E0");
	private DecimalFormat int_Format = new DecimalFormat("####0");


	/**
	 *  WireSignalData constructor.
	 */
	public WireSignalData() {

		wireSignal3D[0] = Data3DFactory.getData3D(1, 1, "point like");
		memWireSignal3D[0] = Data3DFactory.getData3D(1, 1, "point like");
	}


	/**
	 *  Defines size of data from WireRawData instance
	 *
	 *@param  nChannels  The number of cannels
	 *@param  nPos_Slit  The new sizeParameters value
	 *@param  nPos_Harp  The new sizeParameters value
	 */
	public void setSizeParameters(int nPos_Slit, int nPos_Harp, int nChannels) {

		this.nChannels = nChannels;
		this.nPos_Slit = nPos_Slit;
		this.nPos_Harp = nPos_Harp;

		slit_pos = new double[nPos_Slit];
		harp_pos = new double[nPos_Slit][nPos_Harp];

		wireSignal3D = new ColorSurfaceData[nPos_Harp];
		memWireSignal3D = new ColorSurfaceData[nPos_Harp];

		for (int iph = 0; iph < nPos_Harp; iph++) {
			wireSignal3D[iph] = Data3DFactory.getData3D(1, 1, "point like");
			memWireSignal3D[iph] = Data3DFactory.getData3D(1, 1, "point like");
			wireSignal3D[iph].setSize(nPos_Slit, nChannels);
			wireSignal3D[iph].setMinMaxX(0.5, ((double) nPos_Slit) - 0.5);
			wireSignal3D[iph].setMinMaxY(0.5, ((double) nChannels) - 0.5);
		}
	}


	/**
	 *  Returns ColorSurfaceData instance.
	 *
	 *@param  indPos_Harp  Description of the Parameter
	 *@return              The ColorSurfaceData with data after integrating
	 */
	public ColorSurfaceData getPlotData(int indPos_Harp) {
		return wireSignal3D[indPos_Harp];
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
	 *  Returns number of channels. It is less than number of wires, because wires
	 *  with indexes 30-31 are used for other purposes.
	 *
	 *@return    The number of channels
	 */
	public int getChannelsNumber() {
		return nChannels;
	}


	/**
	 *  Returns position of the harp.
	 *
	 *@param  indSlit  The index of the slit position
	 *@param  indHarp  The index of the harp position
	 *@return          The harp positions value
	 */
	public double getHarpPos(int indSlit, int indHarp) {
		return harp_pos[indSlit][indHarp];
	}


	/**
	 *  Returns position of the slit.
	 *
	 *@param  ind  The index of a position
	 *@return      The slit positions value
	 */
	public double getSlitPos(int ind) {
		return slit_pos[ind];
	}


	/**
	 *  Returns the signal for particular position and channel.
	 *
	 *@param  iPosSlit  The position index of the slit
	 *@param  iPosHarp  The position index of the harp
	 *@param  iChan     The channel index
	 *@return           The signal value after integrating
	 */
	public double getValue(int iPosSlit, int iPosHarp, int iChan) {
		return wireSignal3D[iPosHarp].getValue(iPosSlit, iChan);
	}


	/**
	 *  Sets position of the harp.
	 *
	 *@param  indSlit  The index of the slit position
	 *@param  indHarp  The index of the harp position
	 *@param  val      The harp position
	 */
	public void setHarpPos(int indSlit, int indHarp, double val) {
		harp_pos[indSlit][indHarp] = val;
	}


	/**
	 *  Sets position of the slit.
	 *
	 *@param  ind  The index of the position
	 *@param  val  The slit position
	 */
	public void setSlitPos(int ind, double val) {
		slit_pos[ind] = val;
	}


	/**
	 *  Sets the signal for particular position and channel.
	 *
	 *@param  iChan     The channel index
	 *@param  val       The signal value
	 *@param  iPosSlit  The new value value
	 *@param  iPosHarp  The new value value
	 */
	public void setValue(int iPosSlit, int iPosHarp, int iChan, double val) {
		wireSignal3D[iPosHarp].setValue(iPosSlit, iChan, val);
	}


	/**
	 *  Memorizes the data that has not been edited.
	 */
	public void memorizeData3D() {

		for (int iph = 0; iph < nPos_Harp; iph++) {

			int nX = wireSignal3D[iph].getSizeX();
			int nY = wireSignal3D[iph].getSizeY();
			double xMin = wireSignal3D[iph].getMinX();
			double xMax = wireSignal3D[iph].getMaxX();
			double yMin = wireSignal3D[iph].getMinY();
			double yMax = wireSignal3D[iph].getMaxY();

			memWireSignal3D[iph].setSize(nX, nY);
			memWireSignal3D[iph].setMinMaxX(xMin, xMax);
			memWireSignal3D[iph].setMinMaxY(yMin, yMax);
			double val = 0.;
			for (int ix = 0; ix < nX; ix++) {
				for (int iy = 0; iy < nY; iy++) {
					val = wireSignal3D[iph].getValue(ix, iy);
					memWireSignal3D[iph].setValue(ix, iy, val);
				}
			}
		}
	}


	/**
	 *  Restores the data that has been memorized.
	 */
	public void restoreData3D() {

		for (int iph = 0; iph < nPos_Harp; iph++) {
			int nX = memWireSignal3D[iph].getSizeX();
			int nY = memWireSignal3D[iph].getSizeY();
			double xMin = memWireSignal3D[iph].getMinX();
			double xMax = memWireSignal3D[iph].getMaxX();
			double yMin = memWireSignal3D[iph].getMinY();
			double yMax = memWireSignal3D[iph].getMaxY();

			wireSignal3D[iph].setSize(nX, nY);
			wireSignal3D[iph].setMinMaxX(xMin, xMax);
			wireSignal3D[iph].setMinMaxY(yMin, yMax);
			double val = 0.;
			for (int ix = 0; ix < nX; ix++) {
				for (int iy = 0; iy < nY; iy++) {
					val = memWireSignal3D[iph].getValue(ix, iy);
					wireSignal3D[iph].setValue(ix, iy, val);
				}
			}
			wireSignal3D[iph].calcMaxMinZ();
		}
	}


	/**
	 *  Returns the string identifier in the XML structure.
	 *
	 *@return    The name of the sub-structure in the XML data adapter
	 */
	public String getNameXMLData() {
		return xmlName;
	}


	/**
	 *  Writes configuration information into the XML data adapter
	 *
	 *@param  makeRawToEmittPanelData  The XML data adapter
	 */
	public void dumpDataToXML(XmlDataAdaptor makeRawToEmittPanelData) {

		XmlDataAdaptor wireSignalData = (XmlDataAdaptor) makeRawToEmittPanelData.createChild(getNameXMLData());
		XmlDataAdaptor data = (XmlDataAdaptor) wireSignalData.createChild("DATA");

		XmlDataAdaptor params = (XmlDataAdaptor) data.createChild("PARAMS");
		params.setValue("nChannels", nChannels);
		params.setValue("nPosSlit", nPos_Slit);
		params.setValue("nPosHarp", nPos_Harp);

		XmlDataAdaptor posData = (XmlDataAdaptor) data.createChild("POSITIONS");

		StringBuffer sb = new StringBuffer(1000);

		XmlDataAdaptor slitPosData = (XmlDataAdaptor) posData.createChild("SLIT_POSITIONS");
		if (sb.length() > 0) {
			sb.delete(0, sb.length());
		}
		sb.append(" ");
		for (int i = 0; i < slit_pos.length; i++) {
			sb.append(dbl_Format.format(slit_pos[i]));
			sb.append(" ");
		}
		slitPosData.setValue("pos_mm", sb.toString());

		XmlDataAdaptor harpPosData = (XmlDataAdaptor) posData.createChild("HARP_POSITIONS");
		if (sb.length() > 0) {
			sb.delete(0, sb.length());
		}
		sb.append(" ");
		for (int is = 0; is < nPos_Slit; is++) {
			for (int ih = 0; ih < nPos_Harp; ih++) {
				sb.append(dbl_Format.format(harp_pos[is][ih]));
				sb.append(" ");
			}
		}
		harpPosData.setValue("pos_mm", sb.toString());

		for (int iph = 0; iph < nPos_Harp; iph++) {
			int nX = wireSignal3D[iph].getSizeX();
			int nY = wireSignal3D[iph].getSizeY();
			int nScrX = wireSignal3D[iph].getScreenSizeX();
			int nScrY = wireSignal3D[iph].getScreenSizeX();
			double xMin = wireSignal3D[iph].getMinX();
			double xMax = wireSignal3D[iph].getMaxX();
			double yMin = wireSignal3D[iph].getMinY();
			double yMax = wireSignal3D[iph].getMaxY();

			XmlDataAdaptor table3D = (XmlDataAdaptor) data.createChild("WIRE_SIGNALS_HARP_POS_" + iph);
			table3D.setValue("nX", nX);
			table3D.setValue("nY", nY);
			table3D.setValue("nScrX", nScrX);
			table3D.setValue("nScrY", nScrY);
			table3D.setValue("xMin", xMin);
			table3D.setValue("xMax", xMax);
			table3D.setValue("yMin", yMin);
			table3D.setValue("yMax", yMax);

			for (int ix = 0; ix < nX; ix++) {
				XmlDataAdaptor line = (XmlDataAdaptor) table3D.createChild("VALUES_" + ix);
				if (sb.length() > 0) {
					sb.delete(0, sb.length());
				}
				sb.append(" ");
				for (int iy = 0; iy < nY; iy++) {
					sb.append(dbl_Format.format(wireSignal3D[iph].getValue(ix, iy)));
					sb.append(" ");
				}
				line.setValue("vals", sb.toString());
			}

			nX = memWireSignal3D[iph].getSizeX();
			nY = memWireSignal3D[iph].getSizeY();
			nScrX = memWireSignal3D[iph].getScreenSizeX();
			nScrY = memWireSignal3D[iph].getScreenSizeX();
			xMin = memWireSignal3D[iph].getMinX();
			xMax = memWireSignal3D[iph].getMaxX();
			yMin = memWireSignal3D[iph].getMinY();
			yMax = memWireSignal3D[iph].getMaxY();

			table3D = (XmlDataAdaptor) data.createChild("WIRE_SIGNALS_MEMORY_HARP_POS_" + iph);
			table3D.setValue("nX", nX);
			table3D.setValue("nY", nY);
			table3D.setValue("nScrX", nScrX);
			table3D.setValue("nScrY", nScrY);
			table3D.setValue("xMin", xMin);
			table3D.setValue("xMax", xMax);
			table3D.setValue("yMin", yMin);
			table3D.setValue("yMax", yMax);

			for (int ix = 0; ix < nX; ix++) {
				XmlDataAdaptor line = (XmlDataAdaptor) table3D.createChild("VALUES_" + ix);
				if (sb.length() > 0) {
					sb.delete(0, sb.length());
				}
				sb.append(" ");
				for (int iy = 0; iy < nY; iy++) {
					sb.append(dbl_Format.format(memWireSignal3D[iph].getValue(ix, iy)));
					sb.append(" ");
				}
				line.setValue("vals", sb.toString());
			}
		}
	}


	/**
	 *  Configures this class from the XML data adapter information.
	 *
	 *@param  makeRawToEmittPanelData  The XML data adapter
	 */
	public void setDataFromXML(XmlDataAdaptor makeRawToEmittPanelData) {

		Pattern p = Pattern.compile("[,\\s\\t]+");

		XmlDataAdaptor wireSignalData = (XmlDataAdaptor) makeRawToEmittPanelData.childAdaptor(getNameXMLData());
		if (wireSignalData == null) {
			return;
		}

		XmlDataAdaptor data = (XmlDataAdaptor) wireSignalData.childAdaptor("DATA");
		if (data == null) {
			return;
		}

		XmlDataAdaptor params = (XmlDataAdaptor) data.childAdaptor("PARAMS");
		if (params == null) {
			return;
		}

		int nChannels_tmp = params.intValue("nChannels");
		int nPos_Slit_tmp = params.intValue("nPosSlit");
		int nPos_Harp_tmp = params.intValue("nPosHarp");

		XmlDataAdaptor posData = (XmlDataAdaptor) data.childAdaptor("POSITIONS");
		if (posData == null) {
			return;
		}

		XmlDataAdaptor slitPosData = (XmlDataAdaptor) posData.childAdaptor("SLIT_POSITIONS");
		if (slitPosData == null) {
			return;
		}

		XmlDataAdaptor harpPosData = (XmlDataAdaptor) posData.childAdaptor("HARP_POSITIONS");
		if (harpPosData == null) {
			return;
		}

		String strSlitPos = slitPosData.stringValue("pos_mm");
		if (strSlitPos == null) {
			return;
		}

		String strHarpPos = harpPosData.stringValue("pos_mm");
		if (strHarpPos == null) {
			return;
		}

		//set arrays sizes
		setSizeParameters(nPos_Slit_tmp, nPos_Harp_tmp, nChannels_tmp);

		String[] str_arr = new String[0];

		if (nChannels_tmp != 0 && nPos_Slit_tmp != 0 && nPos_Harp_tmp != 0) {

			str_arr = p.split(strSlitPos.trim());
			for (int i = 0; i < str_arr.length; i++) {
				setSlitPos(i, Double.parseDouble(str_arr[i]));
			}

			str_arr = p.split(strHarpPos.trim());
			int i_count = 0;
			for (int is = 0; is < nPos_Slit; is++) {
				for (int ih = 0; ih < nPos_Harp; ih++) {
					setHarpPos(is, ih, Double.parseDouble(str_arr[i_count]));
					i_count++;
				}
			}
		}

		for (int iph = 0; iph < nPos_Harp; iph++) {

			XmlDataAdaptor table3D = (XmlDataAdaptor) data.childAdaptor("WIRE_SIGNALS_HARP_POS_" + iph);
			XmlDataAdaptor table3Dmem = (XmlDataAdaptor) data.childAdaptor("WIRE_SIGNALS_MEMORY_HARP_POS_" + iph);
			if (table3D == null || table3Dmem == null) {
				return;
			}

			//wireSignal3D data definition
			int nX = table3D.intValue("nX");
			int nY = table3D.intValue("nY");
			int nScrX = table3D.intValue("nScrX");
			int nScrY = table3D.intValue("nScrY");
			double xMin = table3D.doubleValue("xMin");
			double xMax = table3D.doubleValue("xMax");
			double yMin = table3D.doubleValue("yMin");
			double yMax = table3D.doubleValue("yMax");

			wireSignal3D[iph].setSize(nX, nY);
			wireSignal3D[iph].setScreenResolution(nScrX, nScrY);
			wireSignal3D[iph].setMinMaxX(xMin, xMax);
			wireSignal3D[iph].setMinMaxY(yMin, yMax);

			for (int ix = 0; ix < nX; ix++) {
				XmlDataAdaptor lineData = (XmlDataAdaptor) table3D.childAdaptor("VALUES_" + ix);
				String line = lineData.stringValue("vals");
				str_arr = p.split(line.trim());
				for (int iy = 0; iy < nY; iy++) {
					wireSignal3D[iph].setValue(ix, iy, Double.parseDouble(str_arr[iy]));
				}
			}

			//memory wireSignal3D data definition
			nX = table3Dmem.intValue("nX");
			nY = table3Dmem.intValue("nY");
			nScrX = table3Dmem.intValue("nScrX");
			nScrY = table3Dmem.intValue("nScrY");
			xMin = table3Dmem.doubleValue("xMin");
			xMax = table3Dmem.doubleValue("xMax");
			yMin = table3Dmem.doubleValue("yMin");
			yMax = table3Dmem.doubleValue("yMax");

			memWireSignal3D[iph].setSize(nX, nY);
			memWireSignal3D[iph].setScreenResolution(nScrX, nScrY);
			memWireSignal3D[iph].setMinMaxX(xMin, xMax);
			memWireSignal3D[iph].setMinMaxY(yMin, yMax);

			for (int ix = 0; ix < nX; ix++) {
				XmlDataAdaptor lineData = (XmlDataAdaptor) table3Dmem.childAdaptor("VALUES_" + ix);
				String line = lineData.stringValue("vals");
				str_arr = p.split(line.trim());
				for (int iy = 0; iy < nY; iy++) {
					memWireSignal3D[iph].setValue(ix, iy, Double.parseDouble(str_arr[iy]));
				}
			}

			//all data are ready
			wireSignal3D[iph].calcMaxMinZ();
		}
	}

}

