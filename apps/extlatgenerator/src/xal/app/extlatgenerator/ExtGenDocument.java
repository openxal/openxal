/*
 * @(#)ExtGenDocument.java          0.9 05/21/2003
 *
 * Copyright (c) 2001-2002 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.app.extlatgenerator;

import java.util.*;
import java.net.*;
import java.io.*;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.event.*;

import javax.swing.event.*;
import javax.swing.JToggleButton.ToggleButtonModel;

import xal.extension.application.smf.*;
import xal.extension.application.*;
import xal.smf.*;
import xal.smf.data.*;
import xal.sim.slg.*;   // for lattice generation
import xal.extension.extlatgen.*; // t3d and dynac adaptor
import xal.model.probe.*;  // Probe for t3d header
import xal.model.probe.traj.ProbeState;
import xal.model.xml.*;
import xal.model.alg.*;
import xal.sim.scenario.*;
import xal.extension.widgets.apputils.SimpleProbeEditor;
import xal.service.pvlogger.apputils.browser.PVLogSnapshotChooser;
import xal.tools.xml.*;
import xal.tools.data.*;

/**
 * ExtGenDocument is a custom AcceleratorDocument for generating external lattice input file application.
 * @version   0.9  21 May 2003
 * @author  t6p
 * @author  Paul Chu
 */

public class ExtGenDocument extends AcceleratorDocument {
	/** file chooser for the exported document */
	final protected JFileChooser EXPORT_FILE_CHOOSER;
	
    /** The document for the text pane in the main window. */
    protected PlainDocument textDocument;
    
    protected Lattice lattice = null;
    private Probe<?> myProbe;
    
	/** flag for the data source to use */
	private String _dataSourceFlag;
    
	/** data source for devices */
	private AbstractDeviceDataSource _deviceDataSource;
    
	/** indicates whether the design bend fields are used regardless of the global device data source selection */
	private boolean _useDesignBends;
    
	private PVLogSnapshotChooser _pvloggerSnapshotChooser;
    
    /** the name of the xml file containing the accelerator */
    protected String theProbeFile = "";
    
    
    // define an accelerator
    //    Accelerator             accel = new Accelerator();
    
    /** Create a new empty document */
    public ExtGenDocument() {
		this( null );
    }
    
    /**
     * Create a new document loaded from the URL file
     * @param url The URL of the file to load into the new document.
     */
    public ExtGenDocument(java.net.URL url) {
		// default to design mode
		_dataSourceFlag = Scenario.SYNC_MODE_DESIGN;
		_deviceDataSource = AbstractDeviceDataSource.getDesignDataSourceInstance();
		_useDesignBends = true;
		
		EXPORT_FILE_CHOOSER = new JFileChooser();
		EXPORT_FILE_CHOOSER.setSelectedFile( new File( "input.mad" ) );
		
        setSource( url );
        makeTextDocument();
    }
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        mainWindow = new ExtGenWindow(this);
        
        if (getSource() != null) {
            XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(getSource(), false);
            DataAdaptor da1 = xda.childAdaptor("extlatgenerator");
            
            //restore accelerator file
            this.setAcceleratorFilePath(da1.childAdaptor("accelerator").stringValue("xmlFile") );
            
            String accelUrl = "file://"+ this.getAcceleratorFilePath();
            try {
                XMLDataManager  dMgr = new XMLDataManager(accelUrl);
                this.setAccelerator(dMgr.getAccelerator(), this.getAcceleratorFilePath());
            }
            catch(Exception exception) {
                JOptionPane.showMessageDialog(null, "Hey - I had trouble parsing the accelerator input xml file you fed me", "ExtGen error",  JOptionPane.ERROR_MESSAGE);
            }
            this.acceleratorChanged();
            
            // set up the right sequence combo from selected primaries:
            List<DataAdaptor> temp = da1.childAdaptors("sequences");
            if (temp.isEmpty())
                return; // bail out, nothing left to do
            
            ArrayList<AcceleratorSeq> seqs = new ArrayList<AcceleratorSeq>();
            List<DataAdaptor> selectedSeqList = null;           // CKA - never used
            DataAdaptor da2a = da1.childAdaptor("sequences");
            String seqName = da2a.stringValue("name");
            
            temp = da2a.childAdaptors("seq");
            Iterator<DataAdaptor> itr = temp.iterator();
            while (itr.hasNext()) {
                DataAdaptor da = itr.next();
                seqs.add(getAccelerator().getSequence(da.stringValue("name")));
            }
            setSelectedSequence(new AcceleratorSeqCombo(seqName, seqs));
            setSelectedSequenceList(seqs.subList(0,seqs.size()));
            
            //restore probe file
            DataAdaptor probeFile = da1.childAdaptor("env_probe");
            theProbeFile = probeFile.stringValue("probeXmlFile");
            try{
                myProbe = (EnvelopeProbe)
                ProbeXmlParser.parse(theProbeFile);
            }
            catch (ParsingException e) {}
            
        }
        setHasChanges(false);
    }
    
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
        
        XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
        DataAdaptor daLevel1 = xda.createChild("extlatgenerator");
        //save accelerator file
        DataAdaptor daXMLFile = daLevel1.createChild("accelerator");
        daXMLFile.setValue("xmlFile", this.getAcceleratorFilePath());
        DataAdaptor envProbeXMLFile = daLevel1.createChild("env_probe");
        //save probe file
        envProbeXMLFile.setValue("probeXmlFile", theProbeFile);
        // save selected sequences
        ArrayList<String> seqs;
        if (getSelectedSequence() != null) {
            DataAdaptor daSeq = daLevel1.createChild("sequences");
            daSeq.setValue("name", getSelectedSequence().getId());
            if (getSelectedSequence().getClass()
                == AcceleratorSeqCombo.class) {
                AcceleratorSeqCombo asc =
				(AcceleratorSeqCombo) getSelectedSequence();
                seqs = (ArrayList<String>) asc.getConstituentNames();
            } else {
                seqs = new ArrayList<String>();
                seqs.add(getSelectedSequence().getId());
            }
            
            Iterator<String> itr = seqs.iterator();
            
            while (itr.hasNext()) {
                DataAdaptor daSeqComponents = daSeq.createChild("seq");
                daSeqComponents.setValue("name", itr.next());
            }
        }
        xda.writeToUrl(url);
    }
    
    
    /**
     * Convenience method for getting the main window cast to the proper subclass of XalWindow.
     * This allows me to avoid casting the window every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    private ExtGenWindow myWindow() {
        return (ExtGenWindow)mainWindow;
    }
    
    
    /**
     * Instantiate a new PlainDocument that servers as the document for the text pane.
     * Create a handler of text actions so we can determine if the document has
     * changes that should be saved.
     */
    private void makeTextDocument() {
        textDocument = new PlainDocument();
        textDocument.addDocumentListener(new DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent evt) {
                setHasChanges(true);
            }
            public void removeUpdate(DocumentEvent evt) {
                setHasChanges(true);
            }
            public void insertUpdate(DocumentEvent evt) {
                setHasChanges(true);
            }
        });
    }
	
	
	/** create a new MAD generator */
	private MadGenerator getMadGeneratorInstance( final Probe<?> probe, final List<AcceleratorSeq> sequences ) {
		if ( probe instanceof TransferMapProbe ) {
			return new MadGenerator( sequences, (TransferMapProbe)probe );
		}
		else if ( probe instanceof EnvelopeProbe ) {
			return new MadGenerator( sequences, (EnvelopeProbe)probe );
		}
		else {
			return null;
		}
	}
	
	
	/** create a new MAD generator */
	private MadGenerator getMadGeneratorInstance( final Probe<?> probe ) {
		final List<AcceleratorSeq> selectedSequences = getSelectedSequenceList();
		
		if ( selectedSequences.size() == 1 ) {
			final AcceleratorSeq sequence = selectedSequences.get(0);
			if ( sequence instanceof AcceleratorSeqCombo ) {
				final List<AcceleratorSeq> sequences = ((AcceleratorSeqCombo)sequence).getConstituents();
				return getMadGeneratorInstance( probe, sequences );
			}
			else {
				return getMadGeneratorInstance( probe, selectedSequences );
			}
		}
		else {
			return getMadGeneratorInstance( probe, selectedSequences );
		}
	}
    
    
    public void customizeCommands(Commander commander) {
        
		// action for probe XML file open
		Action openprobeAction = new AbstractAction() {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent event) {
				JFrame frame = new JFrame();
				JFileChooser fileChooser= new JFileChooser();
				fileChooser.addChoosableFileFilter(new ProbeFileFilter());
                
				int status= fileChooser.showOpenDialog(frame);
				if (status == JFileChooser.APPROVE_OPTION) {
					File file= fileChooser.getSelectedFile();
					theProbeFile = "file://" + file.getPath();
					try{
						myProbe = (EnvelopeProbe)
						ProbeXmlParser.parse(theProbeFile);
					}
					catch (ParsingException e) {}
				}
			}
		};
		openprobeAction.putValue(Action.NAME, "openprobe");
		commander.registerAction(openprobeAction);
        
		// open probe editor
		final Action probeEditorAction = new AbstractAction("probe-editor") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent event) {
				if ( myProbe != null ) {
                    final Probe<?> probe = Probe.newProbeInitializedFrom( myProbe );
					final SimpleProbeEditor probeEditor = new SimpleProbeEditor( getMainWindow() , probe );
					//final JDialog probeEditorDialog = probeEditor.createSimpleProbeEditor( probe );
					myProbe = probeEditor.getProbe();
				}
				else {
					JOptionPane.showMessageDialog( getMainWindow(), "No Probe has been specified for editing. For example, be sure to load an accelerator sequence, first.", "Missing Probe", JOptionPane.ERROR_MESSAGE	);
				}
			}
		};
		commander.registerAction( probeEditorAction );
        
		// action for create T3d input
		Action export1Action = new AbstractAction() {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
				if ( getSelectedSequence() == null ) {
					final JFrame frame = new JFrame();
					JOptionPane.showMessageDialog( frame, "You need to select sequence(s) first.", "Warning!", JOptionPane.WARNING_MESSAGE );
					frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
				}
				else if ( theProbeFile.equals("") && myProbe==null ) {
					final JFrame frame = new JFrame();
					JOptionPane.showMessageDialog( frame, "You need to select probe file first.", "Warning!", JOptionPane.WARNING_MESSAGE );
					frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
				}
				else if ( _dataSourceFlag == null ) {
					JOptionPane.showMessageDialog( mainWindow, "Trace 3D generator does not support PV Logger Data Source. You must select either Design or Channel Access.", "Warning!", JOptionPane.WARNING_MESSAGE );
				}
				else {
					createLattice();
					T3dGenerator t3dGenerator = new T3dGenerator( lattice, (EnvelopeProbe)myProbe );
					try {
						t3dGenerator.createT3dInput( _dataSourceFlag );
					} catch(IOException e) {}
                    
					myWindow().getTextField().setText( "The output Trace 3D file name is " + getSelectedSequence().getId() + ".t3d." );
				}
            }
		};
		export1Action.putValue( Action.NAME, "export-t3d" );
		commander.registerAction( export1Action );
        
		//action for create Dynac input
		Action export2Action = new AbstractAction() {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
				if ( getSelectedSequence() == null ) {
					JFrame frame = new JFrame();
					JOptionPane.showMessageDialog( frame, "You need to select sequence(s) first.", "Warning!", JOptionPane.WARNING_MESSAGE );
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
				else if ( _dataSourceFlag == null ) {
					JOptionPane.showMessageDialog( mainWindow, "Dynac generator does not support PV Logger Data Source. You must select either Design or Channel Access.", "Warning!", JOptionPane.WARNING_MESSAGE );
				}
				else {
					createLattice();
					DynacGenerator dynacGenerator = new DynacGenerator( lattice, getSelectedSequence(), (EnvelopeProbe)myProbe );
					try {
						dynacGenerator.createDynacInput(_dataSourceFlag);
					} catch(IOException e) {}
                    
					myWindow().getTextField().setText( "The output Dynac file name is " + getSelectedSequence().getId() + ".in." );
                    
				}
			}
		};
		export2Action.putValue(Action.NAME, "export-dynac");
		commander.registerAction(export2Action);
        
		//action for create MAD input
		Action export3Action = new AbstractAction() {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
				if ( getSelectedSequence() == null ) {
					JOptionPane.showMessageDialog( getMainWindow(), "You need to select sequence(s) first.", "Warning!", JOptionPane.WARNING_MESSAGE );
				}
				else {
					System.out.println( "MAD probe: " + myProbe );
					final MadGenerator madGenerator = getMadGeneratorInstance( myProbe );
					if ( madGenerator != null ) {
						madGenerator.setUseDesignBendAngles( _useDesignBends );
						try {
							final int status = EXPORT_FILE_CHOOSER.showSaveDialog( getMainWindow() );
							switch( status ) {
								case JFileChooser.APPROVE_OPTION:
									final File fileSelection = EXPORT_FILE_CHOOSER.getSelectedFile();
									madGenerator.createMadInput( _deviceDataSource, fileSelection );
									break;
								default:
									break;
							}
						}
						catch(IOException exception) {
							exception.printStackTrace();
						}
					}
					else {
						System.out.println( "No matching probe to generate the MAD Optics." );
					}
				}
			}
		};
		export3Action.putValue(Action.NAME, "export-mad");
		commander.registerAction(export3Action);
        
		// use-ca action
		////////////////////////////////////
		final ToggleButtonModel useCaModel = new ToggleButtonModel();
		useCaModel.setSelected(true);
		useCaModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_dataSourceFlag = Scenario.SYNC_MODE_LIVE;
				_deviceDataSource = AbstractDeviceDataSource.getLiveMachineDesignRFDataSourceInstance();
			}
		});
		commander.registerModel("use-ca", useCaModel);
        
		// use-design action
		////////////////////////////////////
		final ToggleButtonModel useDesignModel = new ToggleButtonModel();
		useDesignModel.setSelected(true);
		useDesignModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_dataSourceFlag = Scenario.SYNC_MODE_DESIGN;
				_deviceDataSource = AbstractDeviceDataSource.getDesignDataSourceInstance();
			}
		});
		commander.registerModel( "use-design", useDesignModel );
        
		// use-pvlogger action
		////////////////////////////////////
		final ToggleButtonModel usePVLoggerModel = new ToggleButtonModel();
		usePVLoggerModel.setSelected(true);
		usePVLoggerModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_dataSourceFlag = null;
                
				if ( _pvloggerSnapshotChooser == null ) {
					_pvloggerSnapshotChooser = new PVLogSnapshotChooser( mainWindow, true );
				}
				_pvloggerSnapshotChooser.choosePVLogId();
				final long loggerID = _pvloggerSnapshotChooser.getPVLogId();
				if ( loggerID > 0 ) {
					_deviceDataSource = AbstractDeviceDataSource.getPVLoggerDataSourceInstance( loggerID );
				}
			}
		});
		commander.registerModel( "use-pvlogger", usePVLoggerModel );
        
		// use-design-bends action
		final ToggleButtonModel useDesignBendsModel = new ToggleButtonModel();
		useDesignBendsModel.setSelected( _useDesignBends );
		useDesignBendsModel.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
				_useDesignBends = useDesignBendsModel.isSelected();
            }
		});
        commander.registerModel( "use-design-bends", useDesignBendsModel );
    }
    
    
	/** handle the accelerator change event */
    public void acceleratorChanged() {
        if ( accelerator != null ) {
            StringBuffer description = new StringBuffer("Selected Accelerator: " + accelerator.getId() + '\n');
            description.append("Sequences:\n");
            Iterator<AcceleratorSeq> sequenceIter = accelerator.getSequences().iterator();
            while( sequenceIter.hasNext() ) {
                AcceleratorSeq sequence = sequenceIter.next();
                description.append('\t' + sequence.getId() + '\n');
            }
            
			if ( mainWindow != null ) {
				myWindow().getTextView().setText( description.toString() );
			}
			setHasChanges(true);
        }
    }
    
    
	/** handle the selected accelerator sequence change event */
    public void selectedSequenceChanged() {
		System.out.println( "The selected sequence changed to: " + selectedSequence );
        if ( selectedSequence != null ) {
            StringBuffer description = new StringBuffer("Selected Sequence: " + selectedSequence.getId() + '\n');
            description.append("Nodes:\n");
            Iterator<AcceleratorNode> nodeIter = selectedSequence.getNodes().iterator();
            while( nodeIter.hasNext() ) {
                AcceleratorNode node = nodeIter.next();
                description.append('\t' + node.getId() + '\n');
            }
            
            myWindow().getTextView().setText(description.toString());
            try {
                // if it is part of the ring
                if (selectedSequence instanceof Ring) {
                    
                    try {
                        myProbe = ProbeFactory.getTransferMapProbe(selectedSequence, AlgorithmFactory.createTransferMapTracker(selectedSequence));
                        
                    } catch ( InstantiationException exception ) {
                        System.err.println( "Instantiation exception creating probe." );
                        exception.printStackTrace();
                    }
                    
                } else {
                    // if it is part of the Linac
                    
                    try {
                        myProbe = ProbeFactory.getEnvelopeProbe( selectedSequence, AlgorithmFactory.createEnvTrackerAdapt( selectedSequence ));
                        
                    } catch ( InstantiationException exception ) {
                        System.err.println( "Instantiation exception creating probe." );
                        exception.printStackTrace();
                    }
                }
				System.out.println( "The new probe is: " + myProbe );
            } catch (NullPointerException e) {
            	myWindow().getTextField().setText("There is no default probe for this sequence.");
				System.err.println( "There is no default probe for this sequence." );
            }
            setHasChanges(true);
        }
    }
    
    public void createLattice() {
        // create lattice using the (combo) sequence
        LatticeFactory factory=new LatticeFactory();
        factory.setDebug(false);
        factory.setVerbose(false);
        factory.setHalfMag(true);
        try {
            lattice=factory.getLattice(getSelectedSequence());
            lattice.clearMarkers();
            lattice.joinDrifts();
        } catch(LatticeError lerr) {
            System.out.println(lerr.getMessage());
        }
        
    }
}

class ProbeFileFilter extends javax.swing.filechooser.FileFilter {
    //Accept xml files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        
        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.xml) ) {
                return true;
            } else {
                return false;
            }
        }
        
        return false;
    }
    
    //The description of this filter
    public String getDescription() {
        return "Probe File";
    }
}

class Utils {
    
    public final static String xml = "probe";
    
    /*
     * Get the extension of a file.
     */  
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}

