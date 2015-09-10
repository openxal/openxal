package xal.app.ioc2db.cred;

/**
 * The credentials necessary to make connections to the
 * database.
 * 
 *
 * @author Mariano J. Padilla
 * @author Christopher K. Allen
 */
public class DbCredentials extends AccountCredentials {

    
    /*
     * Local Attributes
     */
    
    /** I think this is the network database address */
    private String      strDbAddr;
    
    /** These are items from a database query that the developer thought important to store? */
    private String[]    arrStrDbItems;
    
    /** The database schema we are using */
    private String      strSchema;
    
    /** I thin this a table name in the database where the items came from */
    private String      strTblNm;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new, empty <code>SnsDbCredential</code>
     * object.
     *
     * @author  Christopher K. Allen
     * @since   Aug 23, 2011
     */
    public DbCredentials(){
        super.clearCredentials();
    }
    
    
    
    /*
     * Operations
     */
    
    /**
     * The name of the database table where the items came from ?
     * 
     * @param strTableName     database table name?
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public void setTableName(String strTableName){
        strTblNm = strTableName;
    }
    
    /**
     * I guess it returns a DB table name
     *
     * @return  not sure
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public String getTableName(){
        return strTblNm;
    }
    
    /**
     * Sets the database scheme name
     *
     * @param strSchema
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public void setSchema(String strSchema){
        this.strSchema = strSchema;
    }
    
    
    /**
     * Returns the schema name currently used
     *
     * @return      database schema name ?
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public String getSchema(){
        return this.strSchema;
    }
    
    /**
     * Sets interesting items queried from the database.  (I think.)
     *
     * @param arrStrItems      Items queried from the database.
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public void setDbItems(String [] arrStrItems){
        this.arrStrDbItems=arrStrItems;
    }
    
    /**
     * Returns the items queried from the database and
     * set using <code>{@link #setDbItems(String[])}</code>.
     *
     * @return  array of strings queried from the database.
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public String [] getDbItems(){
        return arrStrDbItems;
    }
    
    /**
     * I think this is the database name on the network.
     *
     * @param strDbSel    network name of the database
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public void setDbAddress(String strDbSel){
        this.strDbAddr=strDbSel;
    }
    
    /**
     * I think this is the database name on the network.
     *
     * @return  network name of the database
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public String getDbAddress(){
        return strDbAddr;
    }

}
