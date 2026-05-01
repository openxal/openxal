/**
 * WindowMenuCommands.java
 *
 *  Created	: Feb 1, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.cmdmgt;

import xal.app.pta.MainWindow;

import java.awt.event.ActionEvent;


/**
 * Implementation of the command set seen under 
 * the application's <tt>Window</tt> menu.
 *
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Feb 1, 2010
 * @author Christopher K. Allen
 */
public class WindowMenuCommands extends CommandSet {

    /**
     * Enumeration of the commands contained in 
     * this command set class.
     *
     * @since  Jul 24, 2009
     * @author Christopher K. Allen
     */
    public enum WINDOW implements CommandSet.ICmdDescriptor {

        /**  Toggle the document notebook editor */
        NOTEBK("mbar.Window.Notebook" , NoteBook.class),

        /**  Toggle the application bug report editor */
        BUGRPT("mbar.Window.BugReport", BugReport.class),
        
        /** Toggle the harp data acquisition panel */
        HARPDAQ("mbar.Window.HarpDaq", HarpDaq.class),

        /** Open the machine configuration screen */
        MACHCFG("mbar.Window.MachConfig", MachConfig.class),
        
        /**  Open the wire scanner configuration/status screen */
        SCANCFG("mbar.Window.ScanConfig", ScanConfig.class),
        
        /** Open the harp configuration/status screen */
        HARPCFG("mbar.Window.HarpConfig", HarpConfig.class),
        
        /** Toggle the DAQ triggering configuration screen */
        TRGCFG("mbar.Window.TrgConfig", TrgConfig.class),
        
        /** Toggle the processing window timing configuration screen */
        PRCCFG("mbar.Window.PrcConfig", PrcConfig.class);
        

        /*
         * ICommandEnum Interface
         */

        /**
         * Returns the command ID associated with this enumeration
         * constant.
         *
         * @return      unique command identifier string
         *  
         * @since       Jul 24, 2009
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
        private Class<? extends Command>        clsCmd;


        /** 
         * Creates a new command enumeration constant
         * with the given command ID string and 
         * command class type.
         * 
         * @param strCmdId      unique command ID string 
         * @param clsCmd        class type of the command
         *
         * @since     Jul 24, 2009
         * @author    Christopher K. Allen
         */
        WINDOW(String strCmdId, Class<? extends Command> clsCmd) {
            this.strCmdId = strCmdId;
            this.clsCmd   = clsCmd;
        }
    }
    
    
    
    
    /*
     * Instance Attributes
     */
    
    /** the main application to bind with these commands */
    private final MainWindow    winMain;
    


    /**
     * Create a new <code>WindowMenuCommands</code> object.
     *
     * @param winMain   the command set will apply to this application 
     *  
     * @since     Feb 1, 2010
     * @author    Christopher K. Allen
     * 
     * @throws InstantiationException unable to create a command in command set
     *                                (see message)
     */
    public WindowMenuCommands(MainWindow winMain) throws InstantiationException {
        super(WINDOW.values());
        
        this.winMain = winMain;
    }

    
    /*
     * The Commands 
     */
    
    /**
     * Toggle document notebook command.
     *
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    public class NoteBook extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new <code>NoteBook</code> object.
         *
         * @param strCmdId      command id string in the configuration file
         *
         * @since     Feb 1, 2010
         * @author    Christopher K. Allen
         */
        public NoteBook(String strCmdId) {
            super(strCmdId);
        }

        /**
         * Toggle document notebook command.
         *
         * @since 	Feb 1, 2010
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            winMain.toggleNotepad();
        }
    }

    
    /**
     * Toggle application bug report command.
     *
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    public class BugReport extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new <code>BugReport</code> object.
         *
         * @param strCmdId
         *
         * @since     Feb 1, 2010
         * @author    Christopher K. Allen
         */
        public BugReport(String strCmdId) {
            super(strCmdId);
        }

        /**
         * Toggles the bug report window on the
         * main screen.
         *
         * @since 	Feb 1, 2010
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            winMain.toggleBugReport();
        }
    }
    
    /**
     *  Display the machine configuration screen command.
     *
     * @since  May 18, 2012
     * @author Christopher K. Allen
     */
    public class HarpDaq extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new <code>ScanConfig</code> object.
         *
         * @param strCmdId string identifier of the command
         *
         * @since     Feb 1, 2010
         * @author    Christopher K. Allen
         */
        public HarpDaq(String strCmdId) {
            super(strCmdId);
        }

        /**
         * Displays the device configuration display.
         *
         * @since   Feb 1, 2010
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
//            winMain.toggleHarpDaqPanel();
        }
        
    }
    
    
    /**
     *  Display the machine configuration screen command.
     *
     * @since  May 18, 2012
     * @author Christopher K. Allen
     */
    public class MachConfig extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new <code>ScanConfig</code> object.
         *
         * @param strCmdId string identifier of the command
         *
         * @since     Feb 1, 2010
         * @author    Christopher K. Allen
         */
        public MachConfig(String strCmdId) {
            super(strCmdId);
        }

        /**
         * Displays the device configuration display.
         *
         * @since   Feb 1, 2010
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            winMain.toggleMachConfigPanel();
        }
        
    }
    
    /**
     *  Display the device configuration screen
     *  command.
     *
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    public class ScanConfig extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new <code>ScanConfig</code> object.
         *
         * @param strCmdId string identifier of the command
         *
         * @since     Feb 1, 2010
         * @author    Christopher K. Allen
         */
        public ScanConfig(String strCmdId) {
            super(strCmdId);
        }

        /**
         * Displays the device configuration display.
         *
         * @since 	Feb 1, 2010
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void actionPerformed(ActionEvent e) {
            winMain.toggleScanConfigPanel();
        }
        
    }
    
    /**
     *  Display the device configuration screen
     *  command.
     *
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    public class HarpConfig extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new <code>ScanConfig</code> object.
         *
         * @param strCmdId string identifier of the command
         *
         * @since     Feb 1, 2010
         * @author    Christopher K. Allen
         */
        public HarpConfig(String strCmdId) {
            super(strCmdId);
        }

        /**
         * Displays the device configuration display.
         *
         * @since   Feb 1, 2010
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            winMain.toggleHarpConfigPanel();
        }
        
    }
    
    /**
     *  Display the DAQ triggering configuration screen
     *  command.
     *
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    public class TrgConfig extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new <code>TrgConfig</code> object.
         *
         * @param strCmdId string identifier of the command
         *
         * @since     Feb 1, 2010
         * @author    Christopher K. Allen
         */
        public TrgConfig(String strCmdId) {
            super(strCmdId);
        }

        /**
         * Toggle the DAQ triggering configuration display.
         *
         * @since       Feb 1, 2010
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            winMain.toggleTimeConfigPanel();
        }
        
    }
    
    /**
     *  Display data processing timing 
     *  command.
     *
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    public class PrcConfig extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * Create a new <code>PrcgConfig</code> object.
         *
         * @param strCmdId string identifier of the command
         *
         * @since     Feb 1, 2010
         * @author    Christopher K. Allen
         */
        public PrcConfig(String strCmdId) {
            super(strCmdId);
        }

        /**
         * Toggles the timing configuration display.
         *
         * @since       Feb 1, 2010
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            winMain.togglePrcgConfigPanel();
        }
        
    }
    
    
}
