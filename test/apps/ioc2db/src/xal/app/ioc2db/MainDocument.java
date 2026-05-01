/*
 * MainDocument.java
 *
 * @author Christopher K. Allen
 * @since  Aug 31, 2011
 *
 */

package xal.app.ioc2db;

import xal.app.ioc2db.cred.DbCredentials;
import xal.app.ioc2db.cred.IocCredentials;


/**
 * This is the centralized location for the application's resources, 
 * primarily database and IOC information. 
 *
 * @author Christopher K. Allen
 * @since   Aug 31, 2011
 */
public class MainDocument {

    
    
    /*
     * Local Attributes
     */
    
    
    /** The IOC account credentials for accessing PV signal name files */
    final private IocCredentials        crdIocHost;
    
    /** The database account credentials used for modifying the PV signal data */
    final private DbCredentials         crdSnsDb;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new <code>MainDocument</code> object that has its database
     * and IOC credentials initialized by the information in the given arguments.
     * 
     * @param crdSnsDb      the default SNS database credentials 
     * @param crdIocHost    the default IOC host account credentials
     *
     * @author  Christopher K. Allen
     * @since   Aug 31, 2011
     */
    public MainDocument(DbCredentials crdSnsDb, IocCredentials crdIocHost) {
        this.crdIocHost = crdIocHost;
        this.crdSnsDb   = crdSnsDb;
        
        // Initialize the database credentials
//        String  strUserId   = dctDefaultDb.getUser();
//        char[]  chrPassword = dctDefaultDb.getPassword().toCharArray();
//        String  strUrlSpec  = dctDefaultDb.getURLSpec();
//        
//        this.crdSnsDb.setCredentials(strUserId, chrPassword);
//        this.crdSnsDb.setDbAddress(strUrlSpec);
//        
//        InputStream isConfigFile = MainDocument.class.getResourceAsStream(urlConfigFile);
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Returns the credentials for the SNS database.
     *
     * @return  database credentials for the current user account  
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    public DbCredentials getDbCredentials() {
        return this.crdSnsDb;
    }
    
    /**
     * Returns the credentials for the IOC host account.
     * 
     * @return  return the current credentials for the IOC host account
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    public IocCredentials   getIocCredentials() {
        return this.crdIocHost;
    }
    
}
