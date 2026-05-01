/*
 * MainWindow.java
 *
 * Created on December 16, 2002, 5:15 PM
 */

package xal.app.scope;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import xal.extension.application.*;
import xal.tools.apputils.*;
import xal.tools.IconLib;
import xal.tools.IconLib.*;
import xal.tools.data.*;

/**
 * Controller of the main window.
 *
 * @author  tap
 */
public class MainWindow extends XalWindow implements SwingConstants, DataListener {
	/** constant required to keep serializable happy */
	static final private long serialVersionUID = 1L;

    // constants
    final static String dataLabel = "MainWindow";
	final private static Insets buttonMargin; 
    
    // document hierarchy
    protected final ScopeModel scopeModel;
    protected ScopeScreen scopeScreen;
    
    // -------------------------------------------------------------------------------
    // Declare the main document window
    protected Container mainPanel;
    protected EdgeLayout mainLayout;
	protected JSplitPane mainSplitView;
    
    // Declare the screen console and its subviews
    protected Box screenConsole;
    protected Box settingsPanel;
    protected Box channelSelectorPanel;
    protected ChannelPanel channelPanel;
    protected MathPanel mathPanel;
    protected TimeBasePanel timeBasePanel;
    protected TriggerPanel triggerPanel;
    
    // Screen controls
    protected JToggleButton gridButton;
	protected JToggleButton legendButton;
    protected JButton refreshRateButton;
	protected JToggleButton fftHandlerButton;
	protected JToggleButton setupButton;
    protected JSlider brightnessSlider;
	
	
	static {
		buttonMargin = new Insets(2, 3, 2, 3);
	}
    
    
    /** Creates new MainWindow associated with a document */
    public MainWindow(XalDocument aDocument, DataAdaptor dataAdaptor) {
        super(aDocument);
        
        scopeModel = ((ScopeDocument)document).getModel();
        initComponents();
        
        if ( dataAdaptor != null ) {
            update(dataAdaptor);
        }
    }
    
    
    /**
     * Dispose of this window and remove its association with the document.
     */
    public void freeCustomResources() {
		scopeScreen = null;
		screenConsole = null;
		channelPanel = null;
		timeBasePanel = null;
		triggerPanel = null;
	}
	
    
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return The tag for this data node.
     */
    public String dataLabel() {
        return dataLabel;
    }
    
    
    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update(DataAdaptor adaptor) {
        DataAdaptor scopeScreenAdaptor = adaptor.childAdaptor( scopeScreen.dataLabel() );
        scopeScreen.update(scopeScreenAdaptor);
        
        updateView();
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external
     * storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write(DataAdaptor adaptor) {
        adaptor.writeNode(scopeScreen);
    }
    
    
    /**
     * Add the listener to be notified when a setting has changed.
     * @param listener Object to receive setting change events.
     */
    void addSettingListener(SettingListener listener) {
        scopeScreen.addSettingListener(listener);
    }
    
    
    /**
     * Remove the listener as a receiver of setting change events.
     * @param listener Object to remove from receiving setting change events.
     */
    void removeSettingListener(SettingListener listener) {
        scopeScreen.removeSettingListener(listener);
    }
    
    
    /**
     * Subclasses may override this method to not create the toolbar.
     */
    public boolean usesToolbar() {
        return false;
    }
    
    
    /** Update the status of components to reflect the underlying model */
    public void updateView() {
        gridButton.setSelected( scopeScreen.isGridVisible() );
		legendButton.setSelected( scopeScreen.isLegendVisible() );
        
        float brightness = scopeScreen.getBrightness();
        brightnessSlider.setValue( (int)(100*brightness) );
		fftHandlerButton.setSelected( scopeScreen.usingFFTHandler() );
    }
    
    
    /**
     * Create the main window subviews.
     */
    protected void initComponents() {
        // --------------------- add the main views ---------------------------------------------------
        final Dimension windowSize = new Dimension( 1020, 500 );
        setSize( windowSize );
        
        mainPanel = getContentPane();
        mainPanel.setSize( windowSize );
        
        screenConsole = new Box(VERTICAL);
		Box screenRowA = new Box(HORIZONTAL);
		screenConsole.add(screenRowA);
        screenConsole.setBorder( new BevelBorder(BevelBorder.LOWERED) );
        
        // create the scope screen that displays the traces
		JPanel screenContainer = new JPanel();
		EdgeLayout screenLayout = new EdgeLayout();
		screenContainer.setLayout(screenLayout);
		scopeScreen = new ScopeScreen(scopeModel);
		scopeScreen.setBorder( new BevelBorder(BevelBorder.LOWERED) );
        screenLayout.add(scopeScreen, screenContainer, 0, 0, EdgeLayout.ALL_SIDES, EdgeLayout.GROW_BOTH);
		screenRowA.add(screenContainer);
		
        // add a right button panel
        Box rightButtonPanel = new Box(VERTICAL);
		screenRowA.add(rightButtonPanel);
		
		Dimension buttonSize = new JButton("TTTTTT").getPreferredSize();
        
        //----------------- add a grid toggle button
        gridButton = new JToggleButton("Grid");
		gridButton.setMinimumSize(buttonSize);
		gridButton.setMaximumSize(buttonSize);
		gridButton.setToolTipText("Toggle the grid.");
        gridButton.setSelected( scopeScreen.isGridVisible() );
		addButtonToVertical(gridButton, rightButtonPanel);
        gridButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                scopeScreen.toggleGridVisible();
                updateView();
            }
        });
        
        //----------------- add a legend toggle button
        legendButton = new JToggleButton("Legend");
		legendButton.setMinimumSize(buttonSize);
		legendButton.setMaximumSize(buttonSize);
		legendButton.setToolTipText("Toggle the legend.");
        legendButton.setSelected( scopeScreen.isGridVisible() );
		addButtonToVertical(legendButton, rightButtonPanel);
        legendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                scopeScreen.toggleLegendVisible();
                updateView();
            }
        });
        
        //----------------- add a window image capture button
        JButton pngCaptureButton = new JButton( "" );
        pngCaptureButton.setAction( ActionFactory.captureWindowAsImageAction( this.document ) );
		pngCaptureButton.setText( null );
		pngCaptureButton.setMinimumSize( buttonSize );
		pngCaptureButton.setMaximumSize( buttonSize );
		pngCaptureButton.setToolTipText( "Capture the scope window to a PNG image file." );
		addButtonToVertical( pngCaptureButton, rightButtonPanel );
        
        //----------------- add a refresh rate button
        refreshRateButton = new JButton("Rate");
		refreshRateButton.setMinimumSize(buttonSize);
		refreshRateButton.setMaximumSize(buttonSize);
		refreshRateButton.setToolTipText("Set the refresh rate.");
		addButtonToVertical(refreshRateButton, rightButtonPanel);
        refreshRateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                RefreshRateDialog.show(MainWindow.this, scopeModel);
            }
        });
		
		//----------------- add a data grab button for writing out the waveform snapshot to a text file
		JButton dataCaptureButton = new JButton( "" );
		dataCaptureButton.setMinimumSize(buttonSize);
		dataCaptureButton.setMaximumSize(buttonSize);
		dataCaptureButton.setIcon( IconLib.getIcon( IconGroup.GENERAL, "Export24.gif") );
		dataCaptureButton.setToolTipText("Dump raw waveform data to a text file.");
		dataCaptureButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					WaveformOutputHandler.writeRawWaveformSnapshot(MainWindow.this, scopeModel);
				}
				catch(java.io.IOException exception) {
					displayError("Error writing data", "Error attempting to write out waveform data.", exception);
					System.err.println(exception);
				}
				catch(RuntimeException exception) {
					displayError("Error writing data", "Error attempting to write out waveform data.", exception);
					System.err.println(exception);
				}
			}
		});
		addButtonToVertical(dataCaptureButton, rightButtonPanel);
		
		//----------------- add a find button for finding the waveforms and setting the appropriate time scale and offset
		JButton findButton = new JButton( "" );
		findButton.setMinimumSize(buttonSize);
		findButton.setMaximumSize(buttonSize);
		findButton.setIcon( IconLib.getIcon( IconGroup.GENERAL, "Find24.gif") );
		findButton.setToolTipText( "Find the waveforms." );
		findButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					scopeScreen.findWaveforms();
				}
				catch(RuntimeException exception) {
					displayError("Error finding the waveforms", "Error attempting to find the waveforms.", exception);
					System.err.println(exception);
				}
			}
		});
		addButtonToVertical(findButton, rightButtonPanel);
		
		
		//--------------- Toggle between the FFT and default screen handlers ---------------------------------------------
		fftHandlerButton = new JToggleButton("FFT");
		fftHandlerButton.setMinimumSize(buttonSize);
		fftHandlerButton.setMaximumSize(buttonSize);
		fftHandlerButton.setToolTipText("Toggle between FFT and default screen.");
        fftHandlerButton.addActionListener(new AbstractAction("FFT") {
			/** constant required to keep serializable happy */
			static final private long serialVersionUID = 1L;
			
            public void actionPerformed(ActionEvent event) {
				scopeScreen.toggleFFTHandler();
                updateView();
            }
        });
		addButtonToVertical(fftHandlerButton, rightButtonPanel);
		
		rightButtonPanel.add( Box.createVerticalGlue() );
		setupButton = new JToggleButton("Configure");
		setupButton.setMargin( new Insets(1,1,1,1) );
		setupButton.setMinimumSize(buttonSize);
		setupButton.setMaximumSize(buttonSize);
		setupButton.setToolTipText("Reveal or hide the settings panel.");
		setupButton.setSelected(true);		// settings panel is visible by default
        setupButton.addActionListener(new AbstractAction("Setup") {
			/** constant required to keep serializable happy */
			static final private long serialVersionUID = 1L;
			
            public void actionPerformed(ActionEvent event) {
				if ( setupButton.isSelected() ) { 
					//mainSplitView.resetToPreferredSizes();
					mainSplitView.setDividerLocation(-1);
				}
				else {
					mainSplitView.setDividerLocation(1.0);
				}
                updateView();
            }
        });
		addButtonToVertical(setupButton, rightButtonPanel);
		
        
        // add bottom button row panel
        JPanel bottomButtonPanel = new JPanel();
        EdgeLayout bottomButtonLayout = new EdgeLayout();
        bottomButtonPanel.setPreferredSize( new Dimension(500, 40) );
		bottomButtonPanel.setMaximumSize( new Dimension(10000, 40) );
        bottomButtonPanel.setLayout(bottomButtonLayout);
		screenConsole.add(bottomButtonPanel);
        
        //----------------- add a brightness slider
        brightnessSlider = new JSlider(0, 100, (int)(100*scopeScreen.getBrightness()) );
		brightnessSlider.setToolTipText("Adjust the background brightness.");
        final Dictionary<Integer,JComponent> brightnessLabelTable = new Hashtable<>();
        brightnessLabelTable.put( new Integer(0), new JLabel("Dark") );
        brightnessLabelTable.put( new Integer(100), new JLabel("Light") );
        brightnessSlider.setLabelTable(brightnessLabelTable);
        brightnessSlider.setPaintLabels(true);
        brightnessSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                float brightness = ((float)( brightnessSlider.getValue() )) / 100;
                scopeScreen.setBrightness(brightness);
            }
        });
        bottomButtonLayout.setConstraints(brightnessSlider, 10, 5, 0, 0, EdgeLayout.TOP_LEFT);
        bottomButtonPanel.add(brightnessSlider);
        
        // Add the control panel
        Box controlPanel = new Box(HORIZONTAL);
        controlPanel.setPreferredSize( new Dimension(400, 350) );
        
        // Put the control panel inside a scrolling view
        JScrollPane controlScrollPane = new JScrollPane(controlPanel);
        controlScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        controlScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Create a summary panel
        SummaryView summaryView = new SummaryView(scopeModel);
        JScrollPane summaryScrollPane = new JScrollPane(summaryView);
        summaryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        summaryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Put the scroll panel inside a split view and try to prevent the control panel from growing
        Box upperView = new Box(VERTICAL);
        upperView.add(controlScrollPane);
        upperView.add( Box.createVerticalGlue() );
        JSplitPane controlSplitView = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, upperView, summaryScrollPane);
		controlSplitView.setResizeWeight(1.0);
        controlSplitView.setOneTouchExpandable(true);
		
		// Create the main split view that divides the scope screen and the channel setup panel
		mainSplitView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, screenConsole, controlSplitView);
		mainSplitView.setResizeWeight(1.0);
		//mainSplitView.setOneTouchExpandable(true);
		mainSplitView.setEnabled(false);
		mainSplitView.setDividerSize(1);
		mainSplitView.resetToPreferredSizes();
		mainPanel.add(mainSplitView);
        
        // Add the panel that holds the channel selectors
        channelSelectorPanel = new Box(VERTICAL);
        channelSelectorPanel.setBorder( new TitledBorder(new EtchedBorder(), "Channels") );
        JToggleButton selectedChannelButton = createChannelSelectors();
        channelSelectorPanel.setMinimumSize( channelSelectorPanel.getPreferredSize() );
        channelSelectorPanel.setMaximumSize( channelSelectorPanel.getPreferredSize() );
        
        // Need to a create a selector Box so we can force the channelSelectorPanel to be top justified
        Box selectorBox = new Box(VERTICAL);
        selectorBox.add(channelSelectorPanel);
        selectorBox.add(Box.createVerticalGlue());
        controlPanel.add(selectorBox);
        
        // Add the panel that has the setup for an individual channel
        settingsPanel = new Box(VERTICAL);
        controlPanel.add(settingsPanel);
        
        Dimension settingPanelSize = new Dimension(300, 350);
        settingsPanel.setPreferredSize(settingPanelSize);

        // add the channel panel to the settings panel
        channelPanel = new ChannelPanel();
        settingsPanel.add(channelPanel);
        ChannelModel channelModel = scopeModel.getChannelModel(0);
        selectedChannelButton.doClick();
        
        // Create a math panel
        mathPanel = new MathPanel();
         
        // Create a TimeBase panel
        timeBasePanel = new TimeBasePanel(scopeScreen.getDefaultScreenHandler().getTimeDisplaySettings(), scopeModel.getTimeModel());
        
        // Create a Trigger panel
        triggerPanel = new TriggerPanel( scopeModel.getTrigger() );
        
        // update the views
        updateView();
        
        // handle the first window activation by setting the focus to the default text box
        addWindowListener( new WindowAdapter() {
            public void windowOpened(WindowEvent event) {
                channelPanel.resetDefaultFocus();
            }
        });
    }
	
	
	/**
	 * Add a button to a vertical button panel.
	 * @param button The button to add
	 * @param verticalBox The vertical panel to which to ad the button.
	 */
	protected void addButtonToVertical(AbstractButton button, Box verticalBox) {
		verticalBox.add(button);
	}
    
    
    /**
     * Convenience method to add the component centered in the specified box.
     * @param component The component to add to the box.
     * @param box The container to which the component is added.
     */
    static protected void addCenteredComponentToBox(Component component, Box box) {
        int margin = 3;
        Box rowBox = new Box(HORIZONTAL);
        rowBox.add( Box.createHorizontalStrut(margin) );
        rowBox.add(component);
        rowBox.add( Box.createHorizontalStrut(margin) );
        box.add(rowBox);
        rowBox.setMaximumSize( rowBox.getPreferredSize() );
    }
    
    
    // --------------------------------------------------------------------------------------------
    // Implement GUI component events
    
    /** 
     * Create the channel selection buttons 
     * @return The selected button.
     */
    protected JToggleButton createChannelSelectors() {
        JToggleButton selectedButton = null;    // the default button
        ButtonGroup channelGroup = new ButtonGroup();
        int colorIndex = 0;
        
        // add the top margin
        channelSelectorPanel.add( Box.createVerticalStrut(5) );
        
        // add the channel selectors
        int numChannels = scopeModel.numChannels();
        for ( int index = 0 ; index < numChannels ; index++ ) {
            JToggleButton selectorButton = new JToggleButton("Ch" + (index+1));
            
            if ( index == 0 ) {
                selectedButton = selectorButton;
            }
            
            channelGroup.add(selectorButton);
            selectorButton.setForeground( ScopeScreen.getDefaultSeriesColor(colorIndex++) );
            addCenteredComponentToBox(selectorButton, channelSelectorPanel);
            channelSelectorPanel.add( Box.createVerticalStrut(5) );
            
            selectorButton.addActionListener( new ChannelSelector(index) );
        }
        
        // add the math channel selectors
        int numMathChannels = 3;        
        channelSelectorPanel.add( Box.createVerticalStrut(5) );
        for ( int index = 0 ; index < numMathChannels ; index++ ) {
            JToggleButton selectorButton = new JToggleButton("Math" + (index+1));
            
            channelGroup.add(selectorButton);
            selectorButton.setForeground( ScopeScreen.getDefaultSeriesColor(colorIndex++) );
            addCenteredComponentToBox(selectorButton, channelSelectorPanel);
            channelSelectorPanel.add( Box.createVerticalStrut(5) );
            
            selectorButton.addActionListener( new MathSelector(index) );
        }
                
        // add a trigger panel selector
        JToggleButton triggerButton = new JToggleButton("Trig");
        channelGroup.add(triggerButton);
        channelSelectorPanel.add( Box.createVerticalStrut(10) );
        addCenteredComponentToBox(triggerButton, channelSelectorPanel);
        triggerButton.addActionListener( new TriggerSelector() );
        
        // add a time base panel selector
        JToggleButton timeBaseButton = new JToggleButton("Time");
        channelGroup.add(timeBaseButton);
        channelSelectorPanel.add( Box.createVerticalStrut(10) );
        addCenteredComponentToBox(timeBaseButton, channelSelectorPanel);
        timeBaseButton.addActionListener( new TimeBaseSelector() );
        
        // add the bottom margin
        channelSelectorPanel.add( Box.createVerticalStrut(5) );
        channelSelectorPanel.add( Box.createVerticalGlue() );
        
        return selectedButton;
    }
    
    
    /** Member class for selecting a channel for the channel panel */
    protected class ChannelSelector implements ActionListener {
        final protected int channelIndex;
        
        public ChannelSelector(int newChannelIndex) {
            channelIndex = newChannelIndex;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent event) {
            // if the selected channel panel isn't visible, then make it visible
            if ( channelPanel.getParent() == null ) {
                settingsPanel.removeAll();
                settingsPanel.add(channelPanel);
                settingsPanel.validate();
            }
            ChannelModel channelModel = scopeModel.getChannelModel(channelIndex);
            channelPanel.setChannelModel((AbstractButton)event.getSource(), channelModel);
            channelPanel.resetDefaultFocus();
        }
    }
    
    
    /** Member class for selecting a math channel for the math panel */
    protected class MathSelector implements ActionListener {
        final protected int channelIndex;
        
        public MathSelector(int newChannelIndex) {
            channelIndex = newChannelIndex;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent event) {
            // if the selected math panel isn't visible, then make it visible
            if ( mathPanel.getParent() == null ) {
                settingsPanel.removeAll();
                settingsPanel.add(mathPanel);
                settingsPanel.validate();
            }
            MathModel mathModel = scopeModel.getMathModel(channelIndex);
            mathPanel.setModel((AbstractButton)event.getSource(), mathModel);
            mathPanel.resetDefaultFocus();
        }
    }
    
    
    /** Member class for selecting a time base panel */
    protected class TimeBaseSelector implements ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent event) {
            if ( timeBasePanel.getParent() == null ) {
                settingsPanel.removeAll();
                settingsPanel.add(timeBasePanel);
                settingsPanel.validate();
                timeBasePanel.updateView((AbstractButton)event.getSource());
            }
        }
    }
    
    
    
    /** Member class for selecting the trigger panel */
    protected class TriggerSelector implements ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent event) {
            if ( triggerPanel.getParent() == null ) {
                settingsPanel.removeAll();
                settingsPanel.add(triggerPanel);
                settingsPanel.validate();
                triggerPanel.updateView((AbstractButton)event.getSource());
                triggerPanel.resetDefaultFocus();
            }
        }
    }
}


