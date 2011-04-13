/*
 * Created on Apr 24, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package xal.model.xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;

import junit.framework.TestCase;

import xal.tools.data.IDataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

import xal.model.IComponent;
import xal.model.Lattice;
import xal.model.LatticeTest;

/**
 * @author Christopher Allen
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LatticeXmlTest extends TestCase {



    /*
     *  Global Attributes
     */ 
     
    /** lattice input file */
//    public static final String      s_strUrlLattice = "xml/ModelValidation.lat.mod.xal.xml";
    public static final String      s_strUrlLattice = "xml/MEBT_LANL_lattice.xml";

    /** lattice debug dump output file */
    public static final String      s_strUrlContents = "LatticeXmlTest.dmp.txt";
    
    /** element position output file */
    public static final String      s_strUrlElemPos = "LatticXmlTest.pos.txt";

    /** lattice xml writer output file */
    public static final String      s_strUrlLattDbg = "LatticeXmlTest.lat.mod.xal.xml"; 
    


    /*
     *  Local Attributes
     */
     
    /** lattice object used in validation test */
    private Lattice         m_lattTest = null;
    
    /** debugging string */
    private String          m_strDbg = null;
    



	/**
	 * Constructor for LatticeXmlTest.
	 * @param arg0
	 */
	public LatticeXmlTest(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(LatticeXmlTest.class);
	}


    /*
     *  Test Cases
     */

    
    /**
     *  Query the Lattice object for properties and display them.
     */
    public void testProperties()   {
        System.out.println("Lattice Properties");
        System.out.println("  IElement type string: " + this.m_lattTest.getType());
        System.out.println("  lattice id          : " + this.m_lattTest.getId());
        System.out.println("  comments            : " + this.m_lattTest.getComments());
        System.out.println("  length           (m): " + this.m_lattTest.getLength());
        System.out.println("  number of children  : " + this.m_lattTest.getChildCount());
        System.out.println("  number of leaves    : " + this.m_lattTest.getLeafCount());
    }
    
    /**
     *  Print out the contents of the lattice using its text debugging
     *  method.
     */
    public void testContents() {
        System.out.println("Writing out lattice contents...");
        
        // Open the file object
        FileOutputStream    osFile;
        PrintWriter         os;
        try {
            osFile = new FileOutputStream(s_strUrlContents);
            os = new PrintWriter(osFile);
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail("Unable to open contents file : " + e.getMessage());
            return;
            
        }
        
        // Print out the lattice debug contents
        this.m_lattTest.print(os); 
        os.close();
    }
    
    /**
     *  Print out lattice elements and their positions.
     */
    public void testPositions() {
        System.out.println("Writing lattice element position file...");
        
        // Open the file object
        FileOutputStream    osFile;
        PrintStream         os;
		try {
			osFile = new FileOutputStream(s_strUrlElemPos);
            os = new PrintStream(osFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            fail("Unable to open position file : " + e.getMessage());
            return;
		}
        
        // Print out the element position information
        Iterator<IComponent> iter = this.m_lattTest.localIterator();
        double      dblPos = 0.0;
        
        while (iter.hasNext())  {
            IComponent elem = iter.next();
            
            os.println(elem.getId() + ", l=" + elem.getLength() + ", s=" + dblPos);
            dblPos += elem.getLength();
        }
        os.close();
    }


    /**
     *  Write the current Lattice object to an XML file in 
     *  the XAL Model Lattice format.
     * 
     * @author Christopher Allen
     * @see TestCase#tearDown()
     */
    public void textWriter(){
        System.out.println("Writing lattice XML file...");
                
        // save lattice configuration 
        try {
            LatticeXmlWriter.writeXml(this.m_lattTest, s_strUrlLattDbg);
            
        } catch (java.io.IOException e) {
            fail("Lattice XML writer error: " + e.getMessage());
            
        }
    }
    
    /**
     * Test the loading of a lattice object from an XML file
     * using an appropriate data adaptor.
     * 
     *
     * @author Christopher K. Allen
     * @since  Apr 13, 2011
     */
    public void testParseDataAdaptor() {
    	IDataAdaptor adaptor = XmlDataAdaptor.adaptorForUrl(s_strUrlLattice, false);
    	try {
			LatticeXmlParser.parseDataAdaptor(adaptor);
		} catch (ParsingException e) {
			fail("parsing exception in testParseDataAdaptor");
		}
    }
        
    /*
     * Support Methods
     */

    /**
     * Parse an XML file in the XAL Model Lattice format.
     * 
     * @author Christopher Allen
	 * @see TestCase#setUp()
	 */
	@Override
    protected void setUp() throws Exception {
		super.setUp();

        System.out.println("Parsing lattice file...");
        
        // load the lattice object        
        this.m_lattTest = newTestLattice();
        
	}

    /**
     * @author Christopher Allen
	 * @see TestCase#tearDown()
	 */
	@Override
    protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	// Factory Methods =========================================================
	
	
	public static Lattice newTestLattice() {
		return LatticeTest.newTestLattice();
	}

}
