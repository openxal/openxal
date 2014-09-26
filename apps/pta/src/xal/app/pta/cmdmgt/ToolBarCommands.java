/**
 * ToolBarCommands.java
 *
 *  Created	: Jul 28, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.cmdmgt;

import xal.app.pta.MainWindow;

import java.awt.event.ActionEvent;


/**
 * <p>
 * Manages all the application commands appearing on 
 * main window (<code>MainWindow</code>) tool bars.  
 * The main window tool bar is defined in the resource
 * <tt>menudef.properties</tt> file.    
 * </p>
 *
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jul 28, 2009
 * @author Christopher K. Allen
 */
public class ToolBarCommands extends CommandSet {

    /**
     * <h2>Enumeration TOOLBAR</h2>
     * <p>
     * Enumeration of all the recognized tool bar
     * commands for the application.
     * </p>
     * <p>
     * Each enumeration constant represents a tool bar
     * command.  The value of the constant is the name
     * of the command as defined in the <tt>menudef.properties</tt>
     * file.  The icon and the tooltip text are located in 
     * the configuration file indicated by constant
     * <code>{@link Command#STR_FILE_CMD_CFG}</code>.
     * Upon construction the file is loaded and these resources 
     * are retrievable from this class.
     * </p>
     *
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     */
    public enum TOOLBAR implements ICmdDescriptor {

//        /**  Application tool-bar command */
//        CONFIG("tbar.Cfg", Configure.class),

        /**  Application tool-bar command */
        CONTROL("tbar.Ctrl", Control.class),

        /**  Application tool-bar command */
        DAQ("tbar.Daq", DataAcquisition.class),
        
        /** Notes View toggling */
        NOTES_TOGGLE("tbar.Notes", ToggleNotepad.class),
        
        /** Bug report view toggling */
        BUGRPT_TOGGLE("tbar.BugRpt", ToggleBugReport.class);



        /*
         * ICmdDescriptor Interface
         */

        /**
         * Returns the (unique) identifier string for
         * the command, as given in the enumeration
         * constant constructor.
         * 
         * @return      unique command identifier string
         *
         * @since       Jul 20, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.CommandSet.ICmdDescriptor#getCommandId()
         */
        public String getCommandId() {
            return this.strCmdId;
        }

        /**
         * Returns the class type of the command associated with
         * this enumeration constant.
         * 
         * @return      the command class type
         *
         * @since       Jul 27, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.CommandSet.ICmdDescriptor#getCommandClass()
         */
        public Class<? extends Command> getCommandClass() {
            return this.clsCmd;
        }


        /*
         * Private
         */

        /** the command identifier string */ 
        private String                          strCmdId;

        /** the command class type */
        private Class<? extends Command>  clsCmd;


        /*
         * Support Methods
         */

        /**
         * Create a new <code>TOOLBAR</code> object.
         *
         * @param strCommandId    name of the tool-bar command
         *
         * @since     Jun 17, 2009
         * @author    Christopher K. Allen
         */
        private TOOLBAR(String strCmdId, Class<? extends Command> clsCmd)     {
            this.strCmdId = strCmdId;
            this.clsCmd   = clsCmd;
        }
    }

    
    /*
     * Instance Attributes
     */
    
    /** reference to the application */
    private final MainWindow    winMain;
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ToolBarCommands</code> object.
     *
     * @param winMain   the main GUI window for the application
     *  
     * @throws InstantiationException unable to create a command in command set
     *                                (see message)
     *
     * @since     Jul 28, 2009
     * @author    Christopher K. Allen
     */
    public ToolBarCommands(MainWindow winMain) throws InstantiationException {
        super(TOOLBAR.values());
        
        this.winMain = winMain;
    }





    /********************************************
     * 
     * Command Classes
     * 
     */

    
    /**
     *
     *
     * @since  Jul 28, 2009
     * @author Christopher K. Allen
     */
    class Configure extends Command {

        /**  Serialization Version */
        private static final long serialVersionUID = 1L;

        /**
         *
         * @since       Jul 28, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Configuration");
        }

        /**
         * Create a new <code>Configure</code> object.
         *
         * @param strCmdId
         *
         * @since     Jul 28, 2009
         * @author    Christopher K. Allen
         */
        public Configure(String strCmdId) {
            super(strCmdId);
        }

    }


    /**
     *
     *
     * @since  Jul 28, 2009
     * @author Christopher K. Allen
     */
    class DataAcquisition extends Command {

        /**  Serialization Version */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new <code>DataAcquisition</code> object.
         *
         * @param strCmdId
         *
         * @since     Jul 28, 2009
         * @author    Christopher K. Allen
         */
        public DataAcquisition(String strCmdId) {
            super(strCmdId);
        }

        /**
         *
         * @since       Jul 28, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Profile DAQ");
        }

    }

    /**
     *
     *
     * @since  Jul 28, 2009
     * @author Christopher K. Allen
     */
    class Control extends Command {


        /**  Serialization Version */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new <code>Control</code> object.
         *
         * @param strCmdId
         *
         * @since     Jul 28, 2009
         * @author    Christopher K. Allen
         */
        public Control(String strCmdId) {
            super(strCmdId);
        }

        /**
         *
         * @since       Jul 28, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Profile Control");
        }

    }

    
    /**
     * Command object to toggle notes view
     * between visible and invisible.
     *
     * @since  Aug 18, 2009
     * @author Christopher K. Allen
     */
    class ToggleNotepad extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        
        /**
         * Create a new <code>ToggleNotepad</code> object.
         *
         * @param strCmdId
         *
         * @since     Aug 17, 2009
         * @author    Christopher K. Allen
         */
        public ToggleNotepad(String strCmdId) {
            super(strCmdId);
        }

        /**
         *
         * @since       Aug 17, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            winMain.toggleNotepad();
        }
        
    }

    /**
     * Command object to toggle notes view
     * between visible and invisible.
     *
     * @since  Aug 18, 2009
     * @author Christopher K. Allen
     */
    class ToggleBugReport extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        
        /**
         * Create a new <code>ToggleNotepad</code> object.
         *
         * @param strCmdId
         *
         * @since     Aug 17, 2009
         * @author    Christopher K. Allen
         */
        public ToggleBugReport(String strCmdId) {
            super(strCmdId);
        }

        /**
         *
         * @since       Aug 17, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            winMain.toggleBugReport();
        }
        
    }

}
