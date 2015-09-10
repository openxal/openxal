/**
 * Command.java
 *
 *  Created	: Jul 21, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.cmdmgt;

import xal.extension.application.Commander;
import xal.app.pta.rscmgt.ImageUtility;
import xal.app.pta.rscmgt.PtaResourceManager;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * <p>
 * Application commands should be derived from this class.  The
 * command actions can then be entered into the application's 
 * <code>{@link Commander}</code> instance.  Not only are
 * the actions then registered as event responses (under the event id
 * returned by 
 * <code>{@link Command#getCommandId()}</code>), 
 * additionally they are also available to any event generator 
 * where the action is desired (i.e., through the 
 * <code>{@link Commander}</code> class.
 * </p> 
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jul 21, 2009
 * @author Christopher K. Allen
 * 
 * @see Commander
 * @see Command#getCommandId()
 */
public abstract class Command extends AbstractAction {

    
    /**
     * Just emphasizing that child classes must define this
     * method to qualify as a command action.  This method
     * actually bubbles up from the base class
     * <code>{@link AbstractAction#actionPerformed(ActionEvent)}</code>.
     *
     * @since   Jul 21, 2009
     * @author  Christopher K. Allen
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public abstract void actionPerformed(ActionEvent e);



    /**
     * Commands Configuration File
     * <br/>
     * Location of the configuration properties dictionary
     * for the application commands. 
     */
    public static final String          STR_FILE_CMD_CFG = "menudef.properties";
//    public static final String          STR_FILE_CMD_CFG = "Commands.ini";
    
    

    /**
     * <p>
     * These constants represent the recognized configurable
     * properties for application commands.  The property values
     * are set in the file 
     * <tt>{@link Command#STR_FILE_CMD_CFG}</tt>.
     * </p>
     * <p>
     * The arguments to the enumeration constants are the suffixes 
     * added to command ID strings to produce the keys
     * for the various command properties.
     * </p>
     *
     * @since  Jul 23, 2009
     * @author Christopher K. Allen
     * 
     * @see     Command#STR_FILE_CMD_CFG
     */
    public enum PROP {
    
        /** Text label for the command (e.g., a <code>JButton</code> object) */
        LABEL(".label"),
        
        /** Text description of command (e.g., tool tip text) */
        TEXT(".Text"),

        /** Command action unique identifier string */ 
        ID_ACTION(".action"),
        
        /** Name of the resource file containing the command's small icon */
        ICON_SM(".SmIcon"),
    
        /** Name of the resource file containing the command's small icon */
        ICON_LG(".LgIcon"),
        
        /**  Button model associated with command */
        MDL_BTN(".ButtonMdl");
        
        
    
        /**
         * Returns the command property for the given
         * command identifier. Specifically the command
         * property that is represented by
         * this enumeration constant. 
         *
         * @param strCmdId      unique command identifier      
         * 
         * @return      the property for the given command ID      
         * 
         * @since  Jul 23, 2009
         * @author Christopher K. Allen
         * 
         * @see     Command#STR_FILE_CMD_CFG
         */
        public String getPropertyValue(String strCmdId)     {
            String      strKey = strCmdId + this.strSuffix;
            String      strVal = CLS_PROPS_CMD_CFG.getProperty(strKey);
            
            return strVal;
        }
        
        
        
        
        /*
         * Define and Initialize Command Configuration Properties
         */
        
        /** the tool bar properties map */
        private static Properties CLS_PROPS_CMD_CFG;
        
        
        /**
         * Load the property map containing the command
         * configuration data.  This data is contained in the
         * file identified by constant
         * <code>{@link CommandSet#STR_FILE_CMD_CFG}</code>. 
         */
        static {
            
            // Load the command properties
            try {
                CLS_PROPS_CMD_CFG = PtaResourceManager.getProperties(STR_FILE_CMD_CFG);
                
            } catch (IOException e) {
                String strMsg = "CommandSet: Missing or corrupted file ";
                strMsg += Command.STR_FILE_CMD_CFG;
                System.err.println(strMsg);
                e.printStackTrace();
                
            }
        }
        
        
        /*
         * Internal
         */
        
        /** suffix added to command ID producing property key */
        private final String  strSuffix;
        
        
        /**
         * Create a new <code>PROP</code> enumeration constant.  
         * These constants represent the recognized configurable
         * properties for application commands.  The properties are
         * set in the file 
         * <tt>{@link PROP#STR_FILE_CMD_CFG}</tt>.
         *
         * @param strSuffix     suffix of the property key
         *
         * @since     Jul 23, 2009
         * @author    Christopher K. Allen
         */
        private PROP(String strSuffix) {
            this.strSuffix = strSuffix;
        }
    }




    /**
     * Returns the unique string id of the command action
     * as defined in the 
     * <code>STR_FILE_CMD_CFG</code> 
     * file.
     *
     * @param strCmdId the command action unique identifier
     *  
     * @return      command action string identifier
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     * 
     * @see PROP#ID_ACTION
     */
    static public String buildCommandActionId(String strCmdId) {
        String strVal = PROP.ID_ACTION.getPropertyValue(strCmdId);
        
        return strVal;
    }




    /**  Serialization version */
    private static final long           serialVersionUID = 1L;
    
    

    /*
     * Local Attributes
     */
    
    /** the unique string identifier for this command */
    private final String      strCmdId;
    

    
    /*
     * Initialization (by Derived Classes)
     */
    
    /**
     * Create a new <code>Command</code> object and set the
     * command id to the given value.
     *
     * @param strCmdId  unique identifier string of the command
     *
     * @since     Jul 22, 2009
     * @author    Christopher K. Allen
     */
    public Command(String strCmdId)    {
        super( Command.buildCommandActionId(strCmdId) );
        
        this.strCmdId = strCmdId;
        
        this.putValue( Action.NAME,              this.getCommandActionId() );
        this.putValue( Action.SMALL_ICON,        this.retrieveMenuIcon() );
//        this.putValue( Action.LARGE_ICON_KEY,    this.retrieveLargeIcon() );
        this.putValue( Action.SHORT_DESCRIPTION, this.retrieveCommandText() );
        
    }


    /*
     * Operations
     */

    
    
    /**
     * <p>
     * Adds the new command action into the set of managed
     * commands.
     * </p>
     * 
     * @param cmdr    command manager object
     * 
     * @since  Jul 21, 2009
     * @author Christopher K. Allen
     */
    public void register(Commander cmdr) {

        // Check for a button model
        ButtonModel mdlBtn = this.retrieveButtonModel();

        // If there is a button model register it
        if (mdlBtn != null) {
            mdlBtn.addActionListener(this);
            cmdr.registerAction(this, mdlBtn);

        //      else register just the action
        } else {
            cmdr.registerAction(this);
            
        }
    }
    
    
    /**
     * Return the unique command identifier
     * for this command. 
     *
     * @return the command's identifier string
     *
     * @since  Jul 22, 2009
     * @author Christopher K. Allen
     */
    public String getCommandId() {
        return this.strCmdId;
    }
    
    /**
     * Returns the command action identifier,
     * which is the <code>NAME</code>
     * property of the <code>Action</code> class.
     *
     * @return  the command's action identifier
     * 
     * @since  Jul 29, 2009
     * @author Christopher K. Allen
     * 
     * @see     Action#getValue(String)
     */
    public String getCommandActionId() {
        return Command.buildCommandActionId(this.strCmdId);
    }
    
    
    /**
     * Returns the small icon associated with the given
     * command.  For example, that displayed on the tool bar 
     * command for the given command.
     *
     * @return      small (tool bar sized) icon for command
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     * 
     * @see PROP#ICON_SM
     */
    public Icon retrieveMenuIcon() {
        String strVal = PROP.ICON_SM.getPropertyValue(this.getCommandId());
        
        if (strVal == null)
            return null;
        
        ImageIcon icnFull = PtaResourceManager.getImageIcon(strVal);
        ImageIcon icnTBar = ImageUtility.createToolbarIcon(icnFull);
        
        return icnTBar;
    }

    /**
     * Returns the large icon displayed associated with the
     * given command.
     *
     * @return      full-sized icon for command
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     * 
     * @see PROP#ICON_LG
     */
    public Icon retrieveButtonIcon() {
        String  strVal = PROP.ICON_LG.getPropertyValue(this.getCommandId());
        
        if (strVal == null) 
            return null;
        
        Icon icnFull = PtaResourceManager.getImageIcon(strVal);
        
        return icnFull;
    }

    /**
     * Returns the description text associated with the 
     * given command.
     *
     * @return      command description text string
     * 
     * @since  Jun 17, 2009
     * @author Christopher K. Allen
     * 
     * @see PROP#TEXT
     */
    public String retrieveCommandText() {
        String strVal = PROP.TEXT.getPropertyValue(this.getCommandId());
        
        return strVal;
    }

    /**
     * Returns the label text associated with the given 
     * command.  For example, this is text text used in
     * a <code>JButton</code> instance.
     *
     * @return      command label 
     * 
     * @since  Jun 17, 2009
     * @author Christopher K. Allen
     * 
     * @see PROP#LABEL
     */
    public String retrieveCommandLabel() {
        String strVal = PROP.TEXT.getPropertyValue(this.getCommandId());
        
        return strVal;
    }
    
    /**
     * Retrieves the class name of any button model specified for
     * the command then instantiates (using reflection).  If something
     * goes wrong, or the button model isn't specified, then we
     * return <code>null</code>.
     *
     * @return button model for command or <code>null</code> if not
     *          present or ill formatted
     * 
     * @since  Feb 3, 2010
     * @author Christopher K. Allen
     */
    public ButtonModel retrieveButtonModel() {
        String strVal = PROP.MDL_BTN.getPropertyValue(this.getCommandId());
        
        if (strVal == null) 
            return null;
        
        try {
            Class<?> clsBtnMdl = Class.forName(strVal);
            
            Constructor<?> ctorBtnMdl = clsBtnMdl.getConstructor();
            
            ButtonModel    mdlBtn = (ButtonModel) ctorBtnMdl.newInstance();
            
            return mdlBtn;
            
        } catch (ClassNotFoundException e) {
            return null;
            
        } catch (SecurityException e) {
            return null;
            
        } catch (NoSuchMethodException e) {
            return null;
            
        } catch (IllegalArgumentException e) {
            return null;
            
        } catch (InstantiationException e) {
            return null;
            
        } catch (IllegalAccessException e) {
            return null;
            
        } catch (InvocationTargetException e) {
            return null;
            
        } catch (ClassCastException e) {
            return null;
            
        }
    }

    
    
    
}
