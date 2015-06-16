/*
 * Parser.java
 * @author James Ghawaly Jr.
 * Created on Mon June 15 13:23:35 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttfParser;

import java.net.URL;
import java.util.List;

import xal.tools.xml.XmlDataAdaptor;
import xal.tools.ResourceManager;
import xal.tools.data.*;

public class Parser {
	
	public void main(String filename){
		XmlDataAdaptor daptDoc = XmlDataAdaptor.adaptorForUrl(fileURL(filename),false);
		DataAdaptor daptLinac = daptDoc.childAdaptor("SNS_Linac");
		List<DataAdaptor> lstDaptSeq = daptLinac.childAdaptors("accSeq");
		for(int i = 0; i < lstDaptSeq.size(); i++){
			System.out.println(lstDaptSeq.get(i).toString());
		}
	}
	
	public String parse(String aString){
		return aString;
	}
	
	public URL fileURL(String aFile){
		URL aFileURL = ResourceManager.getResourceURL(this.getClass(), aFile); //Something like this
		return aFileURL;
	}
	
}