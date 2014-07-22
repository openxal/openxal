/**
 * SmplConfigPanel.java
 *
 *  Created	: Jan 16, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.devcfg;

import xal.app.pta.rscmgt.AppProperties;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.impl.WireHarp;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.impl.profile.ProfileDevice.ANGLE;
import xal.smf.scada.ScadaFieldDescriptor;

import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

/**
 * GUI panel for configuring and displaying the data acquisition sampling parameters of 
 * a wire scanner device.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since   May 6, 2014
 * @author  Christopher K. Allen
 */
public class HarpDaqCfgPanel extends DeviceConfigBasePanel<WireHarp.DaqConfig> {

    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;


    
    /**  Title of GUI component */
    private static final String STR_TITLE = "DAQ Configuration"; //$NON-NLS-1$

    
    /** Government warning label */
    static public final String STR_WARNING = "\n\n  NOTE:\n" +
                                             "  These parameters should only be modified" +
                                             " by qualified individuals.      \n";
//    /** Government warning label part 1 */
//    static public final String STR_WARNING = "\n\n  WARNING:\n" +
//                                             "  Do not change these parameters unless  \n" +
//                                             "  you really know what you are doing.  \n" + 
//                                             "  Invalid settings cause hardware failure.  \n";
//    /** Government warning label part 1 */
//    static public final String STR_WARNING = "\n\n  WARNING:\n" +
//                                             "  Changing these parameters can cause death.  \n";

    
    /** ordered list of field descriptors that we manage */
    static private final List<ScadaFieldDescriptor> LST_FLD_DESCRPS;
    
    /** Initialize the list of field descriptors */
    static {

        LST_FLD_DESCRPS = new LinkedList<ScadaFieldDescriptor>();
        
        LST_FLD_DESCRPS.add( WireHarp.DaqConfig.FLD_MAP.get("fitTypeCode") );
        
//        LST_FLD_DESCRPS = ScadaFieldDescriptor.makeFieldDescriptorList(WireHarp.DaqConfig.class);
//        
//        List<ScadaFieldDescriptor>  lstFdsRm = new LinkedList<ScadaFieldDescriptor>();
//        
//        for (ScadaFieldDescriptor fd : LST_FLD_DESCRPS) {
//            
//            if (fd.getFieldName().equalsIgnoreCase("arrPosHor")) //$NON-NLS-1$
//                lstFdsRm.add(fd);
//            
//            if (fd.getFieldName().equalsIgnoreCase("arrPosVer"))
//                lstFdsRm.add(fd);
//            
//            if (fd.getFieldName().equalsIgnoreCase("arrPosDia"))
//                lstFdsRm.add(fd);
//        }
//        
//        LST_FLD_DESCRPS.removeAll( lstFdsRm );
    }
    
    
    /*
     * Local Attributes
     */
    
    /** Warning displayed concerning the altering of sampling parameters */
    private JTextArea           txtWarning;
    
    /** The GUI box containing the warning text and its icon */
    private Box                 boxWarning;
    
    /** map containing the text displays for the wire status records */
    private Map<ProfileDevice.ANGLE, JTextField> mapStatTxt;
    
    /** map containing the text displays for the wire position arrays */
    private Map<ProfileDevice.ANGLE, JTextField>  mapPosTxt;
    
    /** map of the scroll panes used to display the wire position arrays */
    private Map<ProfileDevice.ANGLE, JScrollPane> mapPosScr;
    

    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DaqConfigPanel</code> object.
     *
     * @since     Jan 16, 2010
     * @author    Christopher K. Allen
     */
    public HarpDaqCfgPanel() {
        super(WireHarp.DaqConfig.class);
        
        this.buildGuiComponents();
        this.buildGuiActions();
        this.layoutGuiComponents();
    }

    
    /*
     * Abstract Method Implementation
     */
    
    /**
     * Returns the title of this GUI panel.
     * 
     * @return  title of this panel
     *
     * @since   Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getTitle()
     */
    @Override
    public String getTitle() {
        return STR_TITLE;
    }

    /**
     * Returns the set of descriptors for the device configuration parameters
     * to be managed by the base class.
     * 
     * @return  ordered list of descriptors for parameters that we do not manage
     *
     * @since 	Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getParamDescriptors()
     */
    @Override
    public List<ScadaFieldDescriptor> getParamDescriptors() {
        return LST_FLD_DESCRPS;
    }

    /**
     * Creates, populates, and returns the data structure of
     * data acquisition configuration parameters for the given
     * device.
     * 
     * @param   smfDev from which configuration parameters are fetched
     * 
     * @return  the current configuration parameters for the given device
     *
     * @since   Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getDeviceParameters(xal.smf.impl.WireScanner)
     */
    @Override
    public WireHarp.DaqConfig getDeviceParameters(ProfileDevice smfDev) throws ConnectionException, GetException {
        
        if ( !(smfDev instanceof WireHarp) )
            throw new IllegalArgumentException("Argument must be of type WireHarp, instead it is " + smfDev.getClass());
        
        WireHarp           smfHarp = (WireHarp)smfDev;
        WireHarp.DaqConfig cfgDaq  = WireHarp.DaqConfig.acquire(smfHarp);
        
        return cfgDaq;
    }
    
    
    /*
     * Base Class Overrides
     */
    
    /**
     * Adds the value of the signal gain configuration
     * parameter to the data structure of device
     * configuration parameters
     * 
     * @return  device configuration parameters including signal gain
     *
     * @since 	Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#getGuiFieldVals()
     */
    @Override
    protected WireHarp.DaqConfig retreiveParamValsFromGui() {
        WireHarp.DaqConfig cfgDev = super.retreiveParamValsFromGui();
        
        
        return cfgDev;
    }


    /**
     * Intercepts the <code>setGuiParameters()</code> call
     * to add the current value of the signal gain parameter.
     * 
     * @param   setVals set of configuration parameters sans the value of the signal gain
     *
     * @since 	Jan 16, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.view.devcfg.DeviceConfigBasePanel#setGuiFieldVals(xal.smf.impl.WireScanner.ParameterSet)
     */
    @Override
    protected void displayParameterVals(WireHarp.DaqConfig setVals) {

        // Display the wire status records
        String  strStatHor = this.createStringRepUnsigned(setVals.recWiresHor);
        this.mapStatTxt.get(ANGLE.HOR).setText(strStatHor);
        
        String  strStatVer = this.createStringRepUnsigned(setVals.recWiresVer);
        this.mapStatTxt.get(ANGLE.VER).setText(strStatVer);
        
        String  strStatDia = this.createStringRepUnsigned(setVals.recWiresDia);
        this.mapStatTxt.get(ANGLE.DIA).setText(strStatDia);
        
        // Display the wire position arrays
        String  strValsHor = this.createStringRepArr( setVals.arrPosHor );
        this.mapPosTxt.get(ANGLE.HOR).setText( strValsHor );;
        JScrollBar  barHor = this.mapPosScr.get(ANGLE.HOR).getHorizontalScrollBar();
        barHor.setMinimum( barHor.getMinimum() );
//        txtHor.repaint();
        
        String strValsVer = this.createStringRepArr( setVals.arrPosVer );
        this.mapPosTxt.get(ANGLE.VER).setText( strValsVer );
        JScrollBar  barVer = this.mapPosScr.get(ANGLE.VER).getHorizontalScrollBar();
        barVer.setMinimum( barVer.getMinimum() );
        
        String strValsDia = this.createStringRepArr( setVals.arrPosDia );
        this.mapPosTxt.get(ANGLE.DIA).setText( strValsDia );
        JScrollBar  barDia = this.mapPosScr.get(ANGLE.DIA).getHorizontalScrollBar();
        barDia.setMinimum( barDia.getMinimum() );
        
        super.displayParameterVals(setVals);
    }


    
    /*
     * Support Methods
     */
    
    /**
     * Builds all the GUI components used on this panel.
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2011
     */
    private void    buildGuiComponents() {
        
        // Make the position array text fields
        this.mapPosTxt = new HashMap<ANGLE, JTextField>();
        this.mapPosScr = new HashMap<ANGLE, JScrollPane>();
        
//        String  strZero = "(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)";
        
        for ( ANGLE angle : ANGLE.values() ) {
            JTextField      txtPosArr = new JTextField(35);
//            txtPosArr.setText(strZero);
            txtPosArr.setEditable(false);
            txtPosArr.setPreferredSize(new Dimension(250,20) );
            this.mapPosTxt.put(angle, txtPosArr);

            JScrollPane  scrPosArr = new JScrollPane(txtPosArr);
            scrPosArr.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrPosArr.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            this.mapPosScr.put(angle, scrPosArr);
        }
        
        // Make the wire status record text fields
        this.mapStatTxt = new HashMap<ANGLE, JTextField>();
        
        for (ANGLE angle : ANGLE.values()) {
            JTextField      txtStat = new JTextField(25);
            
            txtStat.setEditable(false);
            mapStatTxt.put(angle, txtStat);
        }
        
        // Make the warning text box
//        ImageIcon   imgIconWarning  = AppProperties.ICON.SMPL_WARNING.getValue().asIcon();
        ImageIcon   imgIconWarning  = AppProperties.ICON.EXPERT.getValue().asIcon();
        JLabel      lblIconWarning  = new JLabel(imgIconWarning);
        Color       clrTextWarning  = this.getBackground();
        
        this.txtWarning = new JTextArea(STR_WARNING);
        this.txtWarning.setBackground(clrTextWarning);
        this.txtWarning.setEditable(false);
        this.txtWarning.setFocusable(false);

        this.boxWarning = Box.createHorizontalBox();
        this.boxWarning.add(lblIconWarning);
        this.boxWarning.add(Box.createHorizontalStrut(10));
        this.boxWarning.add(this.txtWarning);
    }
    
    /**
     * Create the event responses to the user operation of the
     * GUI components.  We attach one response to each component,
     * plus a panel response to the <tt>CLEAR DEVICE</tt> event
     * which is used to clear the trigger event panel.
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2011
     */
    private void    buildGuiActions() {

//        ActionListener lsnGainSel = new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                setDeviceVals();
//            }
//        };
//        this.pnlGain.registerSelectionListener(lsnGainSel);
//        
//        EventListener   lsnClear = new EventListener() {
//            @Override
//            @SuppressWarnings("synthetic-access")
//            public void eventAction(EVENT evt, ProfileDevice ws) {
//                if (evt == EVENT.CLEARDEV) {
//                    HarpDaqCfgPanel.this.pnlGain.setGain(ProfileDevice.GAIN.UNKNOWN);
//                    HarpDaqCfgPanel.this.pnlTrgEvt.clearTriggerEvent();
//                }
//            }
//        };
//        this.registerEventListener(lsnClear);
    }
    
    /**
     * Arranges the individual GUI components on the
     * panel after they are made.
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2011
     */
    private void    layoutGuiComponents() {
//        super.insertComponentTop(this.pnlGain);
        super.insertComponentTop(this.boxWarning);
//        super.insertComponentBottom(this.pnlTrgEvt);
        
        Box boxStatTitle = Box.createHorizontalBox();
        boxStatTitle.add( new JLabel("Wire Status Records") );
        boxStatTitle.add( Box.createHorizontalStrut(50) );
        super.insertComponentBottom(boxStatTitle);

        for (ANGLE angle : ANGLE.values() ) {
            
            Box boxEntry = Box.createHorizontalBox();

            boxEntry.add( this.mapStatTxt.get(angle) );
            boxEntry.add( Box.createHorizontalStrut(5) );
            boxEntry.add( new JLabel(angle.getLabel()) );

            this.insertComponentBottom(boxEntry);
        }
        
        Box boxPosTitle = Box.createHorizontalBox();
        boxPosTitle.add( new JLabel("Wire Positions") );
        boxPosTitle.add( Box.createHorizontalStrut(50) );
        super.insertComponentBottom(boxPosTitle);
        
        for (ANGLE angle : ANGLE.values() ) {
            
            Box boxEntry = Box.createHorizontalBox();
            
//            JScrollPane  scrPosArr = new JScrollPane(this.mapPosTxt.get(angle));
//            scrPosArr.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//            scrPosArr.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

            JScrollPane scrPosArr = this.mapPosScr.get(angle);
            
            boxEntry.add( scrPosArr );
//            boxEntry.add( this.mapPosTxt.get(angle) );
            boxEntry.add( Box.createHorizontalStrut(5) );
            boxEntry.add( new JLabel(angle.getLabel()) );
            
            this.insertComponentBottom(boxEntry);
//            boxArrPos.add( Box.createVerticalStrut(5) );
//            boxArrPos.add( boxEntry );
        }
        
//        super.insertComponentBottom(boxArrPos);
    }
    
    /**
     * Creates a string representation for the given double array.
     * 
     * @param arrVals   n-tuple format string 
     * 
     * @return          comma separated n-tuple of double values
     *
     * @author Christopher K. Allen
     * @since  May 8, 2014
     */
    private String  createStringRepArr(double[] arrVals) {
        StringBuffer    buf = new StringBuffer();
        NumberFormat    fmt = AppProperties.NUMERIC.FMT_POSVALS.getValue().asFormat();
        
        buf.append('(');
        for (double dblVal : arrVals) {
            String strVal = fmt.format(dblVal);
            
            buf.append(strVal);
            buf.append(',');
        }
        
        int     iLast = buf.length() - 1;
        buf.setCharAt(iLast, ')');
        
        return buf.toString();
    }
    
    /**
     * Creates a string representation for the given integers as an
     * unsigned binary number.
     * 
     * @param intVal    integer in signed, twos-complement representation
     * 
     * @return          the string expression of the given number as an unsigned integer
     *
     * @author Christopher K. Allen
     * @since  May 9, 2014
     */
    private String  createStringRepUnsigned(int intVal) {

        Long    lngSgnVal   = Integer.valueOf( intVal ).longValue();
        Long    lngTwo32    = 01l<<32;
        Long    lngTwoCmpl  = lngTwo32 - lngSgnVal;
        String  strUnsgnVal = Long.toBinaryString(lngTwoCmpl);
        
        return strUnsgnVal;
    }
}
