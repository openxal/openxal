package eu.ess.jels;
import java.io.IOException;

import eu.ess.jels.scenario.NewAndImprovedScenarioElementMapping;
import eu.ess.jels.scenario.ScenarioGenerator2;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;


public class ScenarioGeneratorTest {


	public static void main(String[] args) throws InstantiationException, ModelException {
		System.out.println("Running\n");
		
		Accelerator accelerator = loadAccelerator();
		
		for (AcceleratorSeq sequence : accelerator.getSequences()) {			
			// Generates lattice from SMF accelerator
			Scenario scenario = Scenario.newScenarioFor(sequence);
			ScenarioGenerator2 sg = new ScenarioGenerator2(sequence);
			//Scenario scenario = Scenario.newAndImprovedScenarioFor(sequence);
			//ScenarioGenerator2 sg = new ScenarioGenerator2(sequence, NewAndImprovedScenarioElementMapping.getInstance());
			sg.setDebug(true);
			sg.setVerbose(true);
			Scenario escenario = sg.generateScenario();
			//Scenario scenario = Scenario.newAndImprovedScenarioFor(sequence);
					
			// Outputting lattice elements
			saveLattice(scenario.getLattice(), "old/lattice-"+sequence.getId()+".xml");
			saveLattice(escenario.getLattice(), "new/lattice-"+sequence.getId()+".xml");			
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
		Accelerator accelerator = XMLDataManager.loadDefaultAccelerator();
		//Accelerator accelerator = XMLDataManager.acceleratorWithPath("../main.xal");
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
