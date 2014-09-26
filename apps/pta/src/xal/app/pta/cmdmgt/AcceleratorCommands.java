/**
 * AcceleratorCommands.java
 *
 * @author  Christopher K. Allen
 * @since	Feb 2, 2011
 */
package xal.app.pta.cmdmgt;

import xal.app.pta.MainApplication;
import xal.app.pta.MainDocument;
import xal.app.pta.MainWindow;
import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Feb 2, 2011
 */
public class AcceleratorCommands extends CommandSet {

    /**
     * Enumeration of the commands contained in 
     * this command set class.
     *
     * @since  Jul 24, 2009
     * @author Christopher K. Allen
     */
    public enum ACCEL implements CommandSet.ICmdDescriptor {

        /**  Create new application document */
        NEW("mbar.Accel.New" , New.class),

        /**  Open existing application document */
        DEFAULT("mbar.Accel.Default", Default.class);




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
         * @see xal.app.pta.cmdmgt.CommandSet.ICmdDescriptor#getCommandId()
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
         * @see xal.app.pta.cmdmgt.CommandSet.ICmdDescriptor#getCommandClass()
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
        ACCEL(String strCmdId, Class<? extends Command> clsCmd) {
            this.strCmdId = strCmdId;
            this.clsCmd   = clsCmd;
        }
    }
    
    
    /*
     * Local Attributes
     */
    
    /** The main window of the application */
    private final MainWindow        winMain;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new set of <code>AcceleratorCommands</code>
     * menu commands.
     * 
     * @param winMain   application main window
     * 
     * @throws InstantiationException a command in the command set failed to create
     *
     * @author  Christopher K. Allen
     * @since   Feb 2, 2011
     */
    public AcceleratorCommands(MainWindow winMain) throws InstantiationException {
        super(ACCEL.values());
        
        this.winMain = winMain;
    }
    
    
    /*
     * Command classes in command set
     */

    
    /**
     * Performs the <em>Load New Accelerator</em>
     * command for the application.
     *
     * @author Christopher K. Allen
     * @since   Feb 2, 2011
     */
    class New extends Command {

        /** Serialization version ID */
        private static final long serialVersionUID = 1L;

        
        /**
         * Create a load new accelerator command. 
         * 
         * @param strCmdId  command lookup string
         *
         * @author  Christopher K. Allen
         * @since   Feb 2, 2011
         */
        public New(String strCmdId) {
            super(strCmdId);
        }

        /**
         * Loads a new accelerator object from disk.
         * 
         * @since Feb 2, 2011
         * @see xal.app.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            // Install the "Load Accelerator" menu command
            final JFileChooser fileChooser = MainApplication.getAcceleratorApplication().getAcceleratorFileChooser();
            final MainDocument docMain     = winMain.getDocument();
            int status = fileChooser.showOpenDialog( winMain );
            try {
                switch(status) {
                    case JFileChooser.CANCEL_OPTION:
                        break;
                    case JFileChooser.APPROVE_OPTION:
                        final File fileSelect = fileChooser.getSelectedFile();
                        final String strPath  = fileSelect.getAbsolutePath();
                        final Accelerator accelerator = XMLDataManager.acceleratorWithPath( strPath );
                        docMain.setAccelerator( accelerator, strPath );
                        break;
                    case JFileChooser.ERROR_OPTION:
                        break;
                }
            }
            catch(Exception exception) {
                final String strMsg = "Exception while loading the selected accelerator: ";
                System.err.println( strMsg + '\n' + exception );
                MainApplication.getEventLogger().logError(this.getClass(), strMsg);
                docMain.displayError( "Exception", strMsg, exception );
            }

        }
        
    }
    
    /**
     * This class supports the <em>Load Default
     * Accelerator</em> menu command from the
     * <em>Accelerator</em> menu.
     *
     * @author Christopher K. Allen
     * @since   Feb 2, 2011
     */
    class Default extends Command {

        
        
        /** Serialization version ID  */
        private static final long serialVersionUID = 1L;

        
        
        /**
         * Creates a new <em>Load Default Accelerator</code>
         * command object.
         * 
         * @param strCmdId  command lookup string
         *
         * @author  Christopher K. Allen
         * @since   Feb 2, 2011
         */
        public Default(String strCmdId) {
            super(strCmdId);
        }

        /**
         * (Re)loads the default accelerator object from
         * disk.
         * 
         * @since Feb 2, 2011
         * @see xal.app.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void actionPerformed(ActionEvent evt) {
            final MainDocument    docMain = winMain.getDocument();

            try {
                docMain.loadDefaultAccelerator();

            }
            catch(Exception e) {
                final String strMsg = "Exception while loading the default accelerator: ";
                System.err.println( strMsg + '\n' + e.getMessage() );
                MainApplication.getEventLogger().logError(this.getClass(), strMsg);
                docMain.displayError( "Exception", strMsg, e );

            }

        }

    }
}
