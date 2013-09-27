package ess.jels;
import java.io.IOException;

import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.ElsScenarioGenerator;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;


public class ScenarioGeneratorTest {


	public static void main(String[] args) throws InstantiationException, ModelException {
		System.out.println("Running\n");
		
		AcceleratorSeq sequence = loadAcceleratorSequence();
		
		// Generates lattice from SMF accelerator
		Scenario scenario = Scenario.newScenarioFor(sequence);
		Scenario escenario = new ElsScenarioGenerator(sequence).getScenario();
		//Scenario scenario = Scenario.newAndImprovedScenarioFor(sequence);
				
		// Outputting lattice elements
		saveLattice(scenario.getLattice(), "lattice.xml");
		saveLattice(escenario.getLattice(), "elattice.xml");
	}

	private static void saveLattice(Lattice lattice, String file) {		
		try {
			LatticeXmlWriter.writeXml(lattice, file);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}

	private static AcceleratorSeq loadAcceleratorSequence() {
		/* Loading SMF model */		
		Accelerator accelerator = XMLDataManager.loadDefaultAccelerator();
		//Accelerator accelerator = XMLDataManager.acceleratorWithPath("../main.xal");
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the default accelerator.");
		} 			
		
		/* Selects a section */
		AcceleratorSeq sequence = accelerator.findSequence( "MEBT" );
		//AcceleratorSeq sequence = accelerator.findSequence( "HEBT2" );				
		return sequence;
		
		/* We can instead build lattice for the whole accelerator */
		//return accelerator;
	}
}
