/**
 * PropertyDisplayDialog.java
 *
 * @author  Christopher K. Allen
 * @since	Jan 14, 2011
 */
package xal.app.pta.rscmgt;

import xal.app.pta.MainWindow;
import xal.app.pta.tools.property.PropertiesManager;
import xal.app.pta.tools.property.Property;
import xal.app.pta.tools.property.Property.IProperty;
import xal.app.pta.tools.property.PropertyPanel;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager2;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * <p>
 * Displays all the preferences (<code>Property</code> objects) for a 
 * static class of property enumerations.  The class must be a child of
 * <code>{@link PropertyManager}</code> and contain all the managed 
 * properties as enumeration classes.
 * </p>
 * <p>
 * The enumerations with the <code>PropertyManager</code>-derived class
 * expose the <code>{@link xal.app.pta.tools.property.Property.IProperty}</code> 
 * interface.  With these conditions the properties can be displayed properly.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Jan 14, 2011
 * 
 * @see xal.app.pta.tools.property.Property
 * @see xal.app.pta.tools.property.Property.IProperty
 */
public class PropertyDisplayDialog extends JDialog {

    /*
     * Global Constants 
     */
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;


    
    /** Alignment of tabs in tabbed pane */
    private static final int INT_TABS_ALGN   = JTabbedPane.RIGHT;
    
    /** Tab layout policy */
    private static final int INT_TABS_LAYOUT = JTabbedPane.WRAP_TAB_LAYOUT;
    
    
    
    /*
     * Local Attributes
     */
    
    /** List of property panels displayed on this panel */
    private final List<PropertyPanel>       lstPanels;
    
    
    /** The main property display interface */
    private JTabbedPane                     tabDisplay;
    
    /** The close window button */
    private JButton                         butClose;
    
    
    
    /*
     * Initialization
     */
    
    

    /**
     * Create a new <code>PropertyDisplay</code> panel for the
     * given property manager class.
     * 
     * @param winMain     main window of the application, dialog is displayed here
     * @param clsMgr      property manager class whose (static) properties will be displayed
     *
     * @author  Christopher K. Allen
     * @since   Jan 18, 2011
     */
    public PropertyDisplayDialog(MainWindow winMain, Class<? extends PropertiesManager> clsMgr) {
        super(winMain, clsMgr.getName(), Dialog.ModalityType.APPLICATION_MODAL);
        
        this.lstPanels = new LinkedList<PropertyPanel>();
        
        this.initPanels(clsMgr);
        this.initButton();
        this.initTabbedPane();
        this.buildGui();
    }
    
    /*
     * Operations
     */
    
    /**
     * Sets the visibility of the properties dialog
     * display.
     * 
     * @since Jan 19, 2011
     * @see java.awt.Dialog#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean b) {
        for (PropertyPanel pnl : this.lstPanels)
            pnl.setVisible(b);
        
        super.setVisible(b);
    }

    


    
    /*
     * Support Methods
     */
    
    /**
     * Iterates through all the internal classes of the given
     * <code>{@link PropertyManager}</code> class and identifies
     * the enumeration classes.  These classes should contain
     * the property constants exposing the 
     * <code>{@link Property.IProperty}</code> interface.
     * These property constants are parsed into a list, a
     * <code>{@link PropertyPanel}</code> object is created,
     * then added to the list of managed property panels.
     *
     * @param clsPrpMgr  <code>PropertyManager</code> derived class containing 
     *                  property enumerations
     *
     * @author Christopher K. Allen
     * @since  Jan 18, 2011
     */
    private void initPanels(Class<? extends PropertiesManager> clsPrpMgr) {
        Class<?>[]   arrPrpCls = clsPrpMgr.getClasses();
        
        for (Class<?> clsPrpGrp : arrPrpCls) {
            if (! clsPrpGrp.isEnum() )
                continue;

            String  strTitle = clsPrpGrp.getSimpleName();
            
            try {
                IProperty[]     arrIProps = (IProperty[]) clsPrpGrp.getEnumConstants();
                List<Property>  lstProps  = Property.extractProperties(arrIProps);

                PropertyPanel   pnlProps = new PropertyPanel(strTitle, lstProps);
                
                this.lstPanels.add(pnlProps);
                
                } catch (ClassCastException e) { 
            }

        }
    }
    
    /**
     * Creates and initializes the "Close Dialog"
     * button.
     *
     * @author Christopher K. Allen
     * @since  Jan 19, 2011
     */
    private void initButton() {
        
        // Create and initialize the close button
        this.butClose = new JButton("Close"); //$NON-NLS-1$
        this.butClose.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
//                JOptionPane.showMessageDialog(tabDisplay, 
//                        "Preference changes appear upon application restart.", 
//                        "NOTE", 
//                        JOptionPane.INFORMATION_MESSAGE
//                        );
                
                dispose();
            }
        });
    }
    
    /**
     * Creates and initializes the tabbed
     * pane which displays each property
     * panel.
     *
     * @author Christopher K. Allen
     * @since  Jan 19, 2011
     */
    private void initTabbedPane() {

        // Create the tabbed pane and initialize it
        this.tabDisplay = new JTabbedPane();

        this.tabDisplay.setTabPlacement(INT_TABS_ALGN);
        this.tabDisplay.setTabLayoutPolicy(INT_TABS_LAYOUT);
        
        for (PropertyPanel pnlProps : this.lstPanels) {
            String  strName = pnlProps.getTitle();
            
            this.tabDisplay.addTab(strName, pnlProps);
        }
    }
    
    /**
     * Adds all the property panels to the tabbed display.
     * 
     * @throws ClassCastException   the dialog's content pane is not a <code>JPanel</code>
     *
     * @author Christopher K. Allen
     * @since  Jan 18, 2011
     */
    private void buildGui() throws ClassCastException {
        int     szScrHt = AppProperties.PREFS.DLG_HT.getValue().asInteger();
        int     szScrWd = AppProperties.PREFS.DLG_WD.getValue().asInteger();
        String  strMsg  = AppProperties.PREFS.DLG_MSG.getValue().asString();
        
//        LayoutManager   mgrLayout = new FlowLayout();
//        LayoutManager2   mgrLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        JPanel  pnlGui = (JPanel) this.getContentPane();
        
        LayoutManager2      mgrLayout = new GridBagLayout();
        GridBagConstraints  gbcLayout = new GridBagConstraints();
        
        pnlGui.setLayout( mgrLayout );
        
        gbcLayout.gridy = 0; gbcLayout.anchor = GridBagConstraints.CENTER;
        pnlGui.add(this.tabDisplay, gbcLayout);
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 1; gbcLayout.anchor = GridBagConstraints.LINE_END;
        pnlGui.add(new JLabel(strMsg) , gbcLayout);

        gbcLayout.gridx = 1;
        gbcLayout.gridy = 1; gbcLayout.anchor = GridBagConstraints.LINE_END;
        pnlGui.add(this.butClose, gbcLayout);
        
        this.setSize( szScrWd, szScrHt );
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    

//    public static void main(String[] arrArg) {
//
//        
//        Class<AppProperties>    clsPrpMgr = AppProperties.class;
//        Class<?>[]              arrMgrCls = clsPrpMgr.getClasses();
//        
//        for (Class<?> clsPrpGrp : arrMgrCls) {
//            String  strName = clsPrpGrp.getName();
//            boolean bolEnum = clsPrpGrp.isEnum();
//            Property.IProperty[]    arrIProp = (IProperty[]) clsPrpGrp.getEnumConstants();
//            
//            System.out.println("Class " + strName + ":");
//            System.out.println("    Is enumeration class: " + bolEnum);
//            System.out.println("    Values: ");
//            for (IProperty iProp : arrIProp) {
//                Property    prop     = iProp.getValue();
//                String      strPrpNm = prop.getName();
//                String      strCstNm = iProp.toString();
//                System.out.println("      Enum " + strCstNm + " =" + strPrpNm);
//            }
//        }
//        
//        
////        Class<AppProperties>    clsPrpMgr = AppProperties.class;
////        Class<?>[]              arrMgrCls = clsPrpMgr.getClasses();
////        
////        for (Class<?> clsPrpGrp : arrMgrCls) {
////            String  strName = clsPrpGrp.getName();
////            boolean bolEnum = clsPrpGrp.isEnum();
////            Property.IProperty[]    arrIProp = (IProperty[]) clsPrpGrp.getEnumConstants();
////            
////            System.out.println("Class " + strName + ":");
////            System.out.println("    Is enumeration class: " + bolEnum);
////            System.out.println("    Values: ");
////            for (IProperty iProp : arrIProp) {
////                Property    prop     = iProp.getValue();
////                String      strPrpNm = prop.getName();
////                String      strCstNm = iProp.toString();
////                System.out.println("      Enum " + strCstNm + " =" + strPrpNm);
////            }
////        }
//
////        for (Class<?> clsPrpGrp : arrMgrCls) {
////            String  strName = clsPrpGrp.getName();
////            boolean bolEnum = clsPrpGrp.isEnum();
////            Object[]    arrConst = clsPrpGrp.getEnumConstants();
////            
////            System.out.println("Class " + strName + ":");
////            System.out.println("    Is enumeration class: " + bolEnum);
////            System.out.println("    Values: ");
////            for (Object objConst : arrConst)
////                System.out.println("      val=" + objConst);
////        }
//        
////        for (Class<?> clsPrpGrp : arrMgrCls) {
////            String  strName = clsPrpGrp.getName();
////            
////            Method[] arrMthds = clsPrpGrp.getDeclaredMethods();
////            
////            System.out.println("Class name " + strName + " has internal classes ");
////            for (Method mth: arrMthds)
////                System.out.println("    " + mth.getName());
////        }
//    }

}
