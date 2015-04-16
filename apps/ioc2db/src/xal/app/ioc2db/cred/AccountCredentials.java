/*
 * BaseCredentials.java
 *
 * @author Christopher K. Allen
 * @since  Aug 30, 2011
 *
 */

package xal.app.ioc2db.cred;

/**
 * Manages credentials for needed for user account access.
 *
 * @author Christopher K. Allen
 * @since   Aug 30, 2011
 */
public class AccountCredentials {
    
    
    /*
     * Local Attributes
     */

    /** The user ID of the account credentials */
    protected String    strUserId;
    
    /** The password of the account credentials */
    protected char[]    chrPassword;
    
    /** Initialization and validation flag */
    protected boolean   bolValid;

    
    
    /**
     * Creates a new, empty <code>BaseCredentials</code> object.
     *
     * @author  Christopher K. Allen
     * @since   Aug 30, 2011
     */
    public AccountCredentials() {
        super();
    }



    /*
     * Operations
     */
    
    /**
     * Clears all the credentials for the current user account
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public void clearCredentials() {
        this.strUserId   = null;
        this.chrPassword = null;
        this.bolValid    = false;
    }

    /**
     * Sets both the user ID and the password for the user account
     * represented by this object.
     *
     * @param strUserId     user ID for this account
     * @param chrPassword   password for this account
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public void setCredentials(String strUserId, char[] chrPassword) {
        this.strUserId   = strUserId;
        this.chrPassword = chrPassword;
        this.bolValid    = true;
    }

    /**
     * Returns the user ID for the encapsulated credentials
     *
     * @return  user ID of current account
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public String getUserId() {
        return strUserId;
    }

    /**
     * Returns the account password.
     *
     * @return  returns the password for the current user ID
     *
     * @author Christopher K. Allen
     * @since  Aug 30, 2011
     */
    public String getPassword() {
        if (this.chrPassword == null)
            return null;
        
        String  strPassword = new String(this.chrPassword);
        
        return strPassword;
    }

    /**
     * Check whether or not the credentials have been set.
     *
     * @return  <code>true</code> if the credentials are set and <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    public boolean isValid() {
        return bolValid;
    }

}