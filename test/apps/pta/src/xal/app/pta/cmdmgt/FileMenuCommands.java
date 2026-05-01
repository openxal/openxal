/**
 * FileMenuCommands.java
 *
 *  Created	: Jul 24, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.cmdmgt;

import xal.app.pta.MainApplication;

import java.awt.event.ActionEvent;

/**
 * Application commands available under the
 * <tt>File</tt> menu .
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jul 24, 2009
 * @author Christopher K. Allen
 */
public class FileMenuCommands extends CommandSet {


    /**
     * Enumeration of the commands contained in 
     * this command set class.
     *
     * @since  Jul 24, 2009
     * @author Christopher K. Allen
     */
    public enum FILE implements CommandSet.ICmdDescriptor {

        /**  Create new application document */
        NEW("mbar.File.New" , New.class),

        /**  Open existing application document */
        OPEN("mbar.File.Open", Open.class),

        /**  Save current application document to disk */
        SAVE("mbar.File.Save", Save.class),

        /**  Save current application document under a given name */
        SAVEAS("mbar.File.SaveAs", SaveAs.class),

        /** Export the measurement data to different format */
        EXPORT("mbar.File.Export", Export.class),
        
        /**  Close the current application document */
        CLOSE("mbar.File.Close", Close.class),
        
        /** Closes all the application documents */
        CLOSEALL("mbar.File.CloseAll", CloseAll.class),
        
        /** Exit the application */
        EXIT("mbar.File.Exit", Exit.class),

        /** Application preferences dialog */
        APP_PREFS("mbar.File.AppPrefs", AppPrefs.class);



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
        FILE(String strCmdId, Class<? extends Command> clsCmd) {
            this.strCmdId = strCmdId;
            this.clsCmd   = clsCmd;
        }
    }
    
    
    /*
     * Instance Attributes
     */
    
    /** the main application to bind with these commands */
    private final MainApplication appMain;
    


    /*
     * Initialization
     */

    /**
     * Creates the set of commands associated with the 
     * <tt>File</tt> menu in main application menu.
     *
     * @param appMain   the command set will apply to this application 
     *  
     * @since     Jul 24, 2009
     * @author    Christopher K. Allen
     * 
     * @throws InstantiationException unable to create a command in command set
     *                                (see message)
     */
    public FileMenuCommands(MainApplication appMain) throws InstantiationException {
        super(FILE.values());
        
        this.appMain = appMain;
    }



    /************************************
     * 
     *  The Commands
     * 
     */


    /**
     * Implementation of the <code>File/New</code> command.
     *
     * @since  Jul 24, 2009
     * @author Christopher K. Allen
     */
    class New extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;


        /**
         * Perform the command.  This method is essential a proxy to the method
         * <code>{@link MainApplication#menuFileNew()}</code>.
         *
         * @since 	Jul 24, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            
            FileMenuCommands.this.appMain.menuFileNew();
        }

        /**
         * Create a new <code>New</code> object.
         *
         * @param strCmdId
         *
         * @since     Jul 24, 2009
         * @author    Christopher K. Allen
         */
        public New(String strCmdId) {
            super(strCmdId);
        }
    }

    /**
     * Implementation of the <code>File/Open</code> command.
     *
     * @since  Jul 24, 2009
     * @author Christopher K. Allen
     */
    class Open extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;


        /**
         * Perform the command.  This method is essential a proxy to the method
         * <code>{@link MainApplication#menuFileOpen()}</code>.
         *
         * @since       Jul 24, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent evt) {

            FileMenuCommands.this.appMain.menuFileOpen();
        }

        /**
         * Create a new <code>Open</code> object.
         *
         * @param strCmdId
         *
         * @since     Jul 24, 2009
         * @author    Christopher K. Allen
         */
        public Open(String strCmdId) {
            super(strCmdId);
        }
    }

    /**
     * Implementation of the <code>File/Save</code> command.
     *
     * @since  Jul 24, 2009
     * @author Christopher K. Allen
     */
    class Save extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;


        /**
         *
         * @since       Jul 24, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            
            FileMenuCommands.this.appMain.menuFileSave();
        }

        /**
         * Create a new <code>Save</code> object.
         *
         * @param strCmdId      command ID string
         *
         * @since     Jul 24, 2009
         * @author    Christopher K. Allen
         */
        public Save(String strCmdId) {
            super(strCmdId);
        }
    }

    /**
     *
     *
     * @since  Jul 24, 2009
     * @author Christopher K. Allen
     */
    class SaveAs extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;


        /**
         *
         * @since       Jul 24, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent evt) {

            FileMenuCommands.this.appMain.menuFileSaveAs();
        }

        /**
         * Create a new <code>New</code> object.
         *
         * @param strCmdId
         *
         * @since     Jul 24, 2009
         * @author    Christopher K. Allen
         */
        public SaveAs(String strCmdId) {
            super(strCmdId);
        }
    }
    
    /**
     *
     *
     * @since  Mar 22, 2010
     * @author Christopher K. Allen
     */
    class Export extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;


        /**
         *
         * @since       Jul 24, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            FileMenuCommands.this.appMain.menuFileExport();
        }

        /**
         * Create a new <code>Close</code> object.
         *
         * @param strCmdActId
         *
         * @since     Jul 24, 2009
         * @author    Christopher K. Allen
         */
        public Export(String strCmdActId) {
            super(strCmdActId);
        }

    }


    /**
     *
     * Implementation of the <code>File/Close</code> command.
     *
     * @since  Jul 24, 2009
     * @author Christopher K. Allen
     */
    class Close extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;


        /**
         *
         * @since       Jul 24, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            FileMenuCommands.this.appMain.menuFileClose();
        }

        /**
         * Create a new <code>Close</code> object.
         *
         * @param strCmdActId
         *
         * @since     Jul 24, 2009
         * @author    Christopher K. Allen
         */
        public Close(String strCmdActId) {
            super(strCmdActId);
        }

    }

    
    /**
     * Implementation of the <code>File/Close All</code> command.
     *
     * @since  Jul 24, 2009
     * @author Christopher K. Allen
     */
    class CloseAll extends Command {

        /**  Serialization version */
        private static final long serialVersionUID = 1L;


        /**
         *
         * @since       Jul 24, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
        }

        /**
         * Create a new <code>New</code> object.
         *
         * @param strCmdActId
         *
         * @since     Jul 24, 2009
         * @author    Christopher K. Allen
         */
        public CloseAll(String strCmdActId) {
            super(strCmdActId);
        }

    }

    
    /**
     * Implementation of the <code>File/Exit</code> command.
     *
     * @since  Jul 30, 2009
     * @author Christopher K. Allen
     */
    class Exit extends Command {


        /**  Serialization version */
        private static final long serialVersionUID = 1L;


        /**
         *
         * @since       Jul 30, 2009
         * @author  Christopher K. Allen
         *
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            FileMenuCommands.this.appMain.menuFileExit();
        }

        /**
         * Create a new <code>Exit</code> object.
         *
         * @param strCmdId
         *
         * @since     Jul 30, 2009
         * @author    Christopher K. Allen
         */
        public Exit(String strCmdId) {
            super(strCmdId);
        }

    }

    /**
     * Responds to the user event requesting display
     * of the application preferences dialog.
     *
     * @author Christopher K. Allen
     * @since   Jan 20, 2011
     */
    public class AppPrefs extends Command {


        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * @param strCmdId
         *
         * @author  Christopher K. Allen
         * @since   Jan 19, 2011
         */
        public AppPrefs(String strCmdId) {
            super(strCmdId);
        }

        /**
         * Opens the application preferences dialogue box.
         * 
         * @since Jan 19, 2011
         * @see gov.sns.apps.pta.cmdmgt.Command#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e) {
            FileMenuCommands.this.appMain.displayPreferencesDialog();
        }
        
    }
}
