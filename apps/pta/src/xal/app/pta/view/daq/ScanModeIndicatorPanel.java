/**
 * ScanTypeSelectorPanel.java
 *
 * @author  Christopher K. Allen
 * @since	Nov 15, 2011
 */
package xal.app.pta.view.daq;

import xal.app.pta.MainScanController;
import xal.app.pta.MainScanController.SCAN_MODE;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.ImageUtility;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.smf.impl.WireScanner;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * This class presents a GUI that displays the current operation of the wire scanner
 * being run by the <code>{@link MainScanController}</code> object. 
 * That is, the scan is displayed in either the "easy scan" mode, or the default 
 * "expert scan" mode. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Nov 15, 2011
 * 
 * @see MainScanController
 */
public class ScanModeIndicatorPanel extends JPanel implements MainScanController.IScanControllerListener {

    
    /*
     *  Global Constants 
     */
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;

    
    
    /*
     * Internal Classes
     */
    
    /**
     * Specifies the layout of the selection buttons
     * across the GUI face.  The choice of layouts
     * is given by these enumeration constants.
     *
     * @author Christopher K. Allen
     * @since   Nov 16, 2011
     */
    public enum LAYOUT {
        
        /** The default layout - nothing specified */
        DEFAULT,
        
        /** Layout buttons horizontally */
        HOR,
        
        /** Layout buttons vertically */
        VER;
        
    }
    

    
    
    /*
     * Local Attributes
     */
    

    
    /** The button layout scheme */
    private LAYOUT                      enmLayout;
    
    
    /** Label for the title */
    private JLabel                      lblTitle;
    
    /** Label for the unspecified scan mode */
    private JLabel                      lblUnspecScan;
    
    /** Label encapsulating the expert scan icon */
    private JLabel                      lblXprtScan;
    
    /** Label encapsulating the easy scan icon */
    private JLabel                      lblEzScan;

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ScanTypeSelectorPanel</code> object which
     * acts on the given <code>ScannerController</code> object.
     * 
     * @param ctrDaq    the data acquisition controller being modified
     *
     * @author  Christopher K. Allen
     * @since   Nov 15, 2011
     */
    public ScanModeIndicatorPanel(MainScanController ctrDaq) {
        this(ctrDaq, LAYOUT.DEFAULT);
    }
    
    /**
     * Create a new <code>ScanTypeSelectorPanel</code> object which
     * acts on the given <code>ScannerController</code> object and has the
     * given button layout.
     * 
     * @param ctrDaq    the data acquisition controller being modified
     * @param enmLayout specifies the layout of buttons across the GUI face
     *
     * @author  Christopher K. Allen
     * @since   Nov 15, 2011
     */
    public ScanModeIndicatorPanel(MainScanController ctrDaq, LAYOUT enmLayout) {
        super();
        
        this.enmLayout = enmLayout;
        
        this.buildGuiComponents();
        this.layoutGuiComponents();
        this.initializeGui();

        ctrDaq.registerControllerListener(this);
    }
    
    
    
    /*
     * ScannerController.IDaqControllerListener Interface
     */
    
    /**
     * @since Mar 9, 2012
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanInitiated(java.util.List, xal.app.pta.MainScanController.SCAN_MODE)
     */
    @Override
    public void scanInitiated(List<WireScanner> lstDevs, SCAN_MODE mode) {
        
        this.setForeground(Color.BLACK);
        this.lblUnspecScan.setVisible(false);
        
        switch (mode) {
        
        case EASY:
            this.lblEzScan.setVisible(true);
            this.lblXprtScan.setVisible(false);
            break;
            
        case EXPERT:
            this.lblEzScan.setVisible(false);
            this.lblXprtScan.setVisible(true);
            break;
        }
    }

    /**
     * @since Mar 9, 2012
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanCompleted(java.util.List)
     */
    @Override
    public void scanCompleted(List<WireScanner> lstDevs) {
        
        this.setForeground(Color.GRAY);
    }

    /**
     * @since Mar 9, 2012
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanAborted()
     */
    @Override
    public void scanAborted() {

    }

    /**
     * @since Mar 9, 2012
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanActuatorsStopped()
     */
    @Override
    public void scanActuatorsStopped() {

    }

    /**
     * @since Mar 9, 2012
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanActuatorsParked()
     */
    @Override
    public void scanActuatorsParked() {

    }

    /**
     * @since Mar 9, 2012
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanDeviceFailure(xal.smf.impl.WireScanner)
     */
    @Override
    public void scanDeviceFailure(WireScanner smfDev) {

    }

    
    /*
     * Support Methods
     */
    
    /**
     *  Creates all the GUI components used on this panel.
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2011
     */
    private void buildGuiComponents() {
        
        // The title
//        this.lblTitle = new JLabel(" Lastest scan mode                ");
        this.lblTitle = new JLabel("           ");
        
        // The unspecified scan label
        String      strLocUnspScrIcon = AppProperties.ICON.SCAN_MODE_UNSPEC.getValue().asString();
        ImageIcon   icnUspecScr       = PtaResourceManager.getImageIcon(strLocUnspScrIcon);
        ImageIcon   icnUnspecScrSm    = ImageUtility.createThumbnailIcon(icnUspecScr);
        this.lblUnspecScan = new JLabel(" Unspecified ", icnUnspecScrSm, SwingConstants.LEFT);
        this.lblUnspecScan.setToolTipText(" No scan run yet ");
        
        // The expert scan label
        String      strLocXprtScrIcon = AppProperties.ICON.SCAN_MODE_XPRT.getValue().asString();
        ImageIcon   icnXprtScr        = PtaResourceManager.getImageIcon(strLocXprtScrIcon);
        ImageIcon   icnXprtScrSm      = ImageUtility.createThumbnailIcon(icnXprtScr);
        this.lblXprtScan = new JLabel(" Expert ", icnXprtScrSm, SwingConstants.LEFT);
        this.lblXprtScan.setToolTipText("Expert scan mode");
        
        // The easy scan label
        String      strLocEzScrIcon   = AppProperties.ICON.SCAN_MODE_EZ.getValue().asString();
        ImageIcon   icnEzScr          = PtaResourceManager.getImageIcon(strLocEzScrIcon);
        ImageIcon   icnEzScrSm        = ImageUtility.createThumbnailIcon(icnEzScr);
        this.lblEzScan = new JLabel(" Easy   ", icnEzScrSm, SwingConstants.LEFT);
        this.lblEzScan.setToolTipText("Easy scan mode");
    }
    
    /**
     * Lays out the GUI components on the GUI face.
     *
     * @author Christopher K. Allen
     * @since  Nov 16, 2011
     */
    private void layoutGuiComponents() {
        this.setLayout( new GridBagLayout() );
        
        GridBagConstraints  gbc = new GridBagConstraints();
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Add title
//        gbc.anchor = GridBagConstraints.EAST;
        this.add(lblTitle);
        this.incrementComponentPosition(gbc);
  
        // Add all the possible scan modes
//        gbc.anchor = GridBagConstraints.WEST;
        
        this.add(lblUnspecScan, gbc);
        this.incrementComponentPosition(gbc);
        
        this.add(this.lblXprtScan, gbc);
        this.incrementComponentPosition(gbc);

        this.add(this.lblEzScan, gbc);
        this.incrementComponentPosition(gbc);
    }
    
    /**
     * Selects the clears all the display buttons.
     *
     * @author Christopher K. Allen
     * @since  Nov 16, 2011
     */
    private void initializeGui() {

        this.lblTitle.setVisible(true);
        this.lblUnspecScan.setVisible(true);
        this.lblEzScan.setVisible(false);
        this.lblXprtScan.setVisible(false);
        
        float   fltSzFnt = this.getFont().getSize();
        Font    fntTitle = this.getFont().deriveFont(fltSzFnt);
        fntTitle = fntTitle.deriveFont(Font.BOLD);
        
        this.lblTitle.setFont(fntTitle);
    }
    
    /**
     * Increments the grid position (gridx,gridy) within the given
     * <code>GridBagConstraints</code> object according to the current
     * value of the <code>LAYOUT</code> enumeration constant.
     *
     * @param gbc   the <code>GridBagConstraints</code> object that we are updating
     *
     * @author Christopher K. Allen
     * @since  Nov 16, 2011
     */
    private void incrementComponentPosition(GridBagConstraints gbc) {
        
        switch (this.enmLayout) {
        
        case DEFAULT:
        case HOR:
            gbc.gridx++;
            break;
            
        case VER:
            gbc.gridy++;
            break;
        }
    }

}