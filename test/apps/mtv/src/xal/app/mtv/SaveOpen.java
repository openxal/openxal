package xal.app.mtv;

import xal.tools.StringJoiner;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.data.XMLDataManager;
import xal.smf.AcceleratorSeq;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * A class to save and restore mtv documents to an xml file
 * @author  J. Galambos
 */

public class SaveOpen {

    /** The mtv documentto deal with */
    private final MTVDocument theDoc;

    private XmlDataAdaptor xdaRead, xdaWrite;
    
    private final String stringValue = "";
    private final StringJoiner joiner = new StringJoiner(",");   
    /** constructor:
     * @param doc the XyPlot object
     */
    public SaveOpen(MTVDocument doc) { 
     theDoc = doc;
    }

    /** save the object to a file
     * @param url the file to save it to
     */
    public void saveTo(URL url) { 

	xdaWrite = XmlDataAdaptor.newEmptyDocumentAdaptor();
	DataAdaptor mtvda = xdaWrite.createChild("MTVSetup");
	DataAdaptor daAccel = mtvda.createChild("accelerator");
	daAccel.setValue("xmlFile", theDoc.getAcceleratorFilePath());
	
	// save the selected combo seq components:
	ArrayList<String> seqs;

	if(theDoc.getSelectedSequence() != null) {
  	    DataAdaptor daSeq = daAccel.createChild("sequence");		
	    daSeq.setValue("name", theDoc.getSelectedSequence().getId() );

	    if(theDoc.getSelectedSequence().getClass() == AcceleratorSeqCombo.class) {
		AcceleratorSeqCombo asc = (AcceleratorSeqCombo)theDoc.getSelectedSequence();
		seqs = (ArrayList<String>) asc.getConstituentNames();
	    }
	    else {
		seqs = new ArrayList<String>();
		seqs.add(theDoc.getSelectedSequence().getId());
	    }
			 
	
	    Iterator<String> itr = seqs.iterator();

	    while (itr.hasNext()) {
		DataAdaptor daSeqComponents = daSeq.createChild("seq");
		daSeqComponents.setValue("name", itr.next());
	    }
	    
	    DataAdaptor daMagPanel = mtvda.createChild("magnets");
	    DataAdaptor daMagTypes = daMagPanel.createChild("magTypes");
	    for( String type : theDoc.myWindow().getMagPanel().getSelectedTypes()) {
		    daMagTypes.setValue("type", type);
	    }
	
	}
	xdaWrite.writeToUrl(url);

    }

    /** restore xyplot object settings from a file
     * @param url the file to read from
     */
    public void readSetupFrom(URL url) { 
	XmlDataAdaptor xdaWrite =  XmlDataAdaptor.adaptorForUrl(url, false);
	DataAdaptor mtvda = xdaWrite.childAdaptor("MTVSetup");

	// get the accelerator file
	DataAdaptor daAccel = mtvda.childAdaptor("accelerator");
	String acceleratorPath = daAccel.stringValue("xmlFile");

	if ( acceleratorPath.length() > 0 ) {
		theDoc.setAcceleratorFilePath(acceleratorPath);
		System.out.println("accelFile = " + theDoc.getAcceleratorFilePath());
		String accelUrl = "file://"+ theDoc.getAcceleratorFilePath();
		try {
		XMLDataManager  dMgr = new XMLDataManager(accelUrl);
		theDoc.setAccelerator(dMgr.getAccelerator(), theDoc.getAcceleratorFilePath());
		}
		catch(Exception exception) {
		JOptionPane.showMessageDialog(null, "Hey - I had trouble parsing the accelerator input xml file you fed me", "Xyz setup error",  JOptionPane.ERROR_MESSAGE);
		}
		//theDoc.acceleratorChanged();
	}
	// set up the right sequence combo from selected primaries:
	List<DataAdaptor> temp = daAccel.childAdaptors("sequence");
	if(temp.isEmpty() ) return;

	ArrayList<AcceleratorSeq> seqs = new ArrayList<AcceleratorSeq>();
	DataAdaptor daSeq = daAccel.childAdaptor("sequence");
	String seqName = daSeq.stringValue("name");
	//System.out.println("seq name = "+ seqName);

	temp = daSeq.childAdaptors("seq");
	Iterator<DataAdaptor> itr = temp.iterator();
	while (itr.hasNext()) {
	    DataAdaptor da = itr.next();
	seqs.add(theDoc.getAccelerator().getSequence(da.stringValue("name")));
	    //System.out.println("component = " + da.stringValue("name"));
	}
	theDoc.setSelectedSequence(new AcceleratorSeqCombo(seqName, seqs));
	//theDoc.selectedSequenceChanged();
	theDoc.selectedSequenceChanged();
	
	DataAdaptor daMags = mtvda.childAdaptor("magnets");
	if(daMags != null) {
		List<DataAdaptor> temp2 = daMags.childAdaptors("magTypes");
		Iterator<DataAdaptor> itr2 = temp2.iterator();
		while (itr2.hasNext()) {
			DataAdaptor da = itr2.next();
			theDoc.myWindow().getMagPanel().getSelectedTypes().add(da.stringValue("type"));
		}
	}
	theDoc.myWindow().getMagPanel().updateMagnetTable();
	
	theDoc.setHasChanges(false);
    }
}
