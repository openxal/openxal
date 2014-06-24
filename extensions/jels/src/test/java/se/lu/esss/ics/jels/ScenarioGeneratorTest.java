package se.lu.esss.ics.jels;
import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;

@RunWith(JUnit4.class)
public class ScenarioGeneratorTest {

	@Test
	public void doScenarioGeneratorTest() throws InstantiationException, ModelException {
		System.out.println("Running\n");
		
		Accelerator accelerator = loadAccelerator();
		
		for (AcceleratorSeq sequence : accelerator.getSequences()) {			
			// Generates lattice from SMF accelerator
			//Scenario scenario = Scenario.newScenarioFor(sequence);			
			//Scenario scenario = Scenario.newAndImprovedScenarioFor(sequence);
			//ScenarioGenerator2 sg = new ScenarioGenerator2(sequence, NewAndImprovedScenarioElementMapping.getInstance());
			//sg.setDebug(true);
			//sg.setVerbose(true);
			Scenario escenario = Scenario.newScenarioFor(sequence);
			//Scenario scenario = Scenario.newAndImprovedScenarioFor(sequence);
			
			// Ensure files
			new File("temp/old").mkdirs();
			new File("temp/new").mkdirs();
			
			// Outputting lattice elements
			//saveLattice(scenario.getLattice(), "temp/old/lattice-"+sequence.getId()+".xml");
			saveLattice(escenario.getLattice(), "temp/new/lattice-"+sequence.getId()+".xml");			
		}
	}

	private static void saveLattice(Lattice lattice, String file) {		
		try {
			LatticeXmlWriter.writeXml(lattice, file);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}

	private static Accelerator loadAccelerator() {
		/* Loading SMF model */		
		Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("main.xal").toString());
		
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the default accelerator.");
		} 					
		
		//AcceleratorSeq sequence = accelerator.findSequence( "HEBT2" );				
		return accelerator;
		
		/* We can instead build lattice for the whole accelerator */
		//return accelerator;
	}
}
