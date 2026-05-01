/*
 * MainApplication.java
 *
 * @author Christopher K. Allen
 * @since  Aug 23, 2011
 *
 */

package xal.app.ioc2db;


import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import xal.app.ioc2db.cred.DbCredentials;
import xal.app.ioc2db.cred.IocCredentials;
import xal.tools.ResourceManager;
import xal.tools.database.ConnectionDialog;
import xal.tools.database.ConnectionDictionary;

/**
 * Main class for application <tt>MainWindow</tt>.  The application entry
 * point is here, along with many application utility methods.
 *
 * @author Christopher K. Allen
 * @since   Aug 23, 2011
 */
public class MainApplication  implements Runnable {

    

    /**
     * Application entry point.  Creates a new application object
     * and launches it as a string.
     *
     * @param args      command line arguments - unused
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    public static void main(String args[]) {
        
        MainApplication     appMain = new MainApplication();
        
        EventQueue.invokeLater(appMain);
    }
    

    
    /*
     * Global Constants
     */
    
    
    
    /** Location of the EPICS database file on the IOC hosts */
    public static final String STR_IOC_FILEPATH_EPICSDB = "/c$/Program Files/SharedMemoryIOC/db/";
   
    /** Location of the Excel file on the IOC hosts */
    public static final String STR_IOC_FILEPATH_XLS = "/c$/daDLL/";
    
    
    
    /** Locations of the application database configuration file */
    public static final String  STR_CONFIG_FILE = "dbconfig.properties";
    
    
    /** Property in DB configuration file - the default table */
    private static final String PRP_DB_CFG_TABLE = "DefTable";

    /** Property in DB configuration file - the default database schema */
    private static final String PRP_DB_CFG_SCHEMA = "DefSchema";

    /** Property in DB configuration file - the database address */
    private static final String PRP_DB_CFG_ADDR = "AddrProd";



    
    /*
     * Global Methods
     */
    
    /**
     * Creates a string representation of the given 
     * Swing <code>{@link JTable}</code> object.
     * 
     * @param guiTable  the table to be parsed 
     * 
     * @return          a string containing contents of the table
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    public static String parseTable(JTable guiTable){
        String strText="";
        System.out.println(guiTable.getColumnCount());
        for(int x=0;x<guiTable.getRowCount()-1;x++){
            if(x==0){
                for(int y = 0;y<guiTable.getColumnCount();y++){
                    strText+=guiTable.getColumnName(y)+",";
                }
                strText+="\n";
            }
            for(int y = 0;y<guiTable.getColumnCount();y++){
                try{
                    strText += "\""+guiTable.getValueAt(x, y).toString()+"\""+",";
                }catch(NullPointerException e){
                    strText +="\""+","+"\"";
                }
            }
            strText +="\n";
        }
        //System.out.println(mytext);
        return strText;
    }
    
    /**
     * Checks whether or not the given string represents a 
     * numeric value.
     * 
     * @param str   target string
     * 
     * @return      <code>true</code> if the string has a numeric representation,
     *              <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    public static boolean isNumeric(String str){
        try{
            Integer.parseInt(str);
            
        }catch(NumberFormatException NFE){
            System.out.println(NFE);
            return false;
            
        }
        
        return true;
    }
    
    /**
     * Checks whether or not the given string is a representation
     * of an IP address.  Specifically, the string must have the
     * form
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>n</i><sub>4</sub>.<i>n</i><sub>3</sub>.<i>n</i><sub>2</sub>.<i>n</i><sub>1</sub>
     * <br/>
     * <br/>
     * where the <i>n<sub>i</sub></i> are integers (hex, octal, or otherwise) less than 255.
     *
     * @param strIpAddr     the string to be examined
     * 
     * @return              <code>true</code> if the string can be converted to an IP address
     *                      <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    public static boolean isIpAddress(String strIpAddr){
        boolean valid = false;
        if((strIpAddr.length()>8) && (strIpAddr.length()<17)){

            String [] octets = strIpAddr.split("\\.");
            for(int x=0;x<octets.length;x++){
                if((isNumeric(octets[x])) && (octets[x].length()<4)){
                    if(Integer.parseInt(octets[x])>255){
                        return false;
                    }
                }else{
                    return false;
                }
            }
            valid=true;
        }
        return valid;
    }
    
    /**
     * Writes the given text string to the given file object.
     *
     * @param file      file receive text
     * @param strText   text to write
     * 
     * @return  <code>true</code> if the string was successfully written to the file,
     *          <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    public static boolean writeToFile(File file, String strText){
        try{
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write(strText);
            fw.close();
            return true;
            
        }catch (IOException e){
            System.out.println(e.toString());
            
        }
        return false;
    }
    
    /**
     * Converts an array of strings to a single string
     * by concatenation.
     *
     * @param arr   array of strings
     * 
     * @return      single string build of the given string array
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    public static String arrayToString(String [] arr){
        String text = "";
        if (arr.length>0){
            for(int x=0;x<arr.length;x++){
                text += arr[x]+System.getProperty("line.separator");
            }
        }else{
            text = "Empty string. Check file or credentials.";
        }
        return text;
    }
    
    /**
     * Reads the given file into a string buffer and returns
     * that buffer.
     *
     * @param f     file to be read
     * 
     * @return      string containing the contains of the file
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    public static String loadFile(File f){
        StringBuilder contents = new StringBuilder();
        try{
            FileInputStream in = new FileInputStream(f);
            byte[] b = new byte[8192];
            int n;
            while(( n = in.read( b )) > 0 ) {
                contents.append(new String(b, 0, n));
                contents.append(System.getProperty("line.separator"));
            }

            in.close(); // CKA - added 4/16/2015
            
        }catch(IOException e){

        }
        return contents.toString();
    }
    
    
//    /**
//     * <p>
//     * Creates a new initialization file for the applications.
//     * This method should is called whenever the initialization file
//     * is lost or corrupted.   
//     * </p>
//     * <p>
//     * <h4>Note:</h4> 
//     * There are values here that are hard-coded and potentially obsolete.
//     * </p>
//     * 
//     * 
//     * @param fileIni   initialization file handle
//     *
//     * @author Christopher K. Allen
//     * @since  Aug 22, 2011
//     */
//    public static void createInitFile(File fileIni){
//        try{
//            fileIni.createNewFile();
//            FileWriter fw = new FileWriter(fileIni);
//            String mytext = "[PROD]"+"\n";
//            //            mytext +="snsdb1.sns.ornl.gov:1521:prod"+"\n";
//            mytext +="snsapp1a.sns.ornl.gov:1521:prod"+"\n";
//            mytext +="[DEV]"+"\n";
//            mytext +="snsdev3.sns.ornl.gov:1521:devl"+"\n";
//            mytext +="[DEFAULT SCHEMA]"+"\n";
//            mytext +="EPICS"+"\n";
//            mytext +="[DEFAULT TABLE]"+"\n";
//            mytext +="SGNL_REC";
//            fw.write(mytext);
//            fw.close();
//            fw = null;
//        }catch (IOException e){
//            System.out.println(e.toString());
//        }
//    }
    

    
    /*
     * Local Attributes
     */
    
    /** The set of supported database connections and their parameters */
    private ConnectionDictionary        dctDbConn;

    
    
    /*
     * Initialization
     */
    

    /**
     * Creates a new instance of the Ioc2Db application. 
     *
     * @author  Christopher K. Allen
     * @since   Aug 30, 2011
     */
    public MainApplication() {
        ConnectionDictionary    dctDbConnection = ConnectionDictionary.getInstance();
        
        if (dctDbConnection == null) 
            this.dctDbConn = ConnectionDialog.showDialog(null);
        else
            this.dctDbConn = dctDbConnection;
    }


    
    /*
     * Operations
     */
    
    /**
     * <p>
     * Creates a new <code>DbCredentials</code> object with credentials taken
     * from the application configuration file, and the connection dictionary
     * instantiated with the user's persistent preferences. The process is as
     * follows:
     * <br/>
     * <br/>
     * &nbsp; &middot; The configuration properties are read from the application
     * configuration file.  The properties that are in the file are loaded into
     * the databse credentials object (address, default schema, and default table).
     * <br/>
     * <br/>
     * &nbsp; &middot; The user ID and password are take from the default connection
     * dictionary and stored into the database credentials if present.  The database
     * address is also take from the connection dictionary and, if valid, used to
     * overwrite the address from the configuration file.
     * <br/>
     * <br/>
     * The resultant database credentials object is then returned. 
     * </p>
     *
     * @return  database credentials containing properties from both the configuration file
     *          and the user preferences.
     *
     * @author Christopher K. Allen
     * @since  Sep 2, 2011
     */
    public DbCredentials    defaultDbCredentials() {
        
        DbCredentials   crdSnsDb = new DbCredentials();
        
        
        try {
            // Load the database properties file
            URL urlConfigFile = ResourceManager.getResourceURL(this.getClass(), STR_CONFIG_FILE);
            InputStream isConfigFile = urlConfigFile.openStream();
//            InputStream isConfigFile = MainDocument.class.getResourceAsStream(STR_CONFIG_FILE);
            Properties  prpDbConfig  = new Properties();
            prpDbConfig.load(isConfigFile);
            
            String  strAddr   = prpDbConfig.getProperty(PRP_DB_CFG_ADDR);
            String  strSchema = prpDbConfig.getProperty(PRP_DB_CFG_SCHEMA);
            String  strTable  = prpDbConfig.getProperty(PRP_DB_CFG_TABLE);
            
            // Set the database properties from the configuration file
            if (strAddr != null)   crdSnsDb.setDbAddress(strAddr);
            if (strSchema != null) crdSnsDb.setSchema(strSchema);
            if (strTable != null)  crdSnsDb.setTableName(strTable);
            
        } catch (IOException e) {
            String  strErrMsg = "Unable to load configuration file " + STR_CONFIG_FILE;
            
            System.err.println(strErrMsg);
            System.err.println("Exception error message: " + e.getMessage());
            
            JOptionPane.showInternalMessageDialog(null, strErrMsg, "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Overwrite database credentials associated with default user preference
        String  strUserId   = this.dctDbConn.getUser();
        String  strPassword = this.dctDbConn.getPassword();
        String  strUrlSpec  = this.dctDbConn.getURLSpec();
        
        if (strUserId != null && strPassword != null)
            crdSnsDb.setCredentials(strUserId, strPassword.toCharArray());
        
        if (strUrlSpec != null)
            crdSnsDb.setDbAddress(strUrlSpec);
        
        return crdSnsDb;
    }
    
    
    /*
     * Runnable Interface
     */
    
    /**
     * Application launch point.
     * 
     * @since Aug 30, 2011
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        DbCredentials   crdSnsDb   = this.defaultDbCredentials();
        IocCredentials  crdIocHost = new IocCredentials();
        
        MainDocument    docMain = new MainDocument(crdSnsDb, crdIocHost);
        MainWindow      winMain = new MainWindow(docMain);
        
        winMain.setVisible(true);
    }
    
    
    
}
