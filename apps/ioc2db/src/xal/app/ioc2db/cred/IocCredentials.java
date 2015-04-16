/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.app.ioc2db.cred;

import xal.app.ioc2db.MainApplication;

import java.io.IOException;

import javax.swing.JOptionPane;

import jcifs.smb.SmbFile;

/**
 *
 * @author Mariano J. Padilla
 * HERE Internship 2009
 * Mentor Dr. Willem Blockland
 */
public class IocCredentials  extends AccountCredentials {

    
    /*
     * Local Attributes
     */
    
    
//    /** Userid of the IOC account we are accessing */
//    private String strUserid;
//    
//    /** User account password */
//    private char[] chrPasswd;
//    
//    /** Initialization flag ? */
//    private boolean bolValid;

    
    /*
     * Initialization
     */
    
    /**
     * Creates a new, empty ICS computer credentials
     * object.  
     * 
     * @author  Christopher K. Allen
     * @since   Aug 23, 2011
     */
    public IocCredentials() {
        super.clearCredentials();
    }
    
    
    
    /*
     * Operations
     */
    
//    /**
//     * Clears out all the current credentials for the IOC.
//     *
//     * @author Christopher K. Allen
//     * @since  Aug 30, 2011
//     */
//    public void clearAll(){
//        this.strUserid="";
//        this.chrPasswd=null;
//        this.bolValid = false;
//    }
//    
//    /**
//     * Sets the userid and password for the IOC credentials bolValid.
//     *
//     * @param strUserid     userid of the account we are to access
//     * @param chrPasswd     password for the above account
//     *
//     * @author Christopher K. Allen
//     * @since  Aug 30, 2011
//     */
//    public void setAll (String strUserid, char[] chrPasswd){
//        this.strUserid = strUserid;
//        this.chrPasswd = chrPasswd;
//        this.bolValid = true;
//    }
//    
//    /**
//     * Returns the User ID for the IOC account credentials encapsulated
//     * herein.
//     *
//     * @return  user ID of the current user account
//     *
//     * @author Christopher K. Allen
//     * @since  Aug 30, 2011
//     */
//    public String getName(){
//        return this.strUserid;
//    }
//    
//    /**
//     *  Returns the password credential for the user account
//     *  represented by this object.
//     *
//     * @return  the password for the IOC user account
//     *
//     * @author Christopher K. Allen
//     * @since  Aug 30, 2011
//     */
//    public String getPasswd(){
//        String strBuffer = "";
//        for(int x=0;x<=chrPasswd.length-1;x++){
//            strBuffer = strBuffer+chrPasswd[x];
//        }
//        return strBuffer;
//    }
//    
//    /**
//     * Are these credentials valid?
//     * 
//     * @return  <code>true</code> if the credentials have been set,
//     *          <code>false</code> otherwise
//     *
//     * @author Christopher K. Allen
//     * @since  Aug 30, 2011
//     */
//    public boolean isSet(){
//        return bolValid;
//    }
//    

    /*
     * Operations
     */
    
    /**
     * Returns all the files in the given network directory.
     *
     * @param strHostAddr   The IOC host address
     * 
     * @return              array of files in the above directory
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    public SmbFile [] getDirectoryFiles(String strHostAddr){
        String strFilePath = "smb://ORNL;" + 
                this.getUserId() + 
                ":" + 
                this.getPassword() +
                "@"+
                strHostAddr + 
                MainApplication.STR_IOC_FILEPATH_XLS;
        
        SmbFile [] files= null;
        try {
            SmbFile dir = new SmbFile(strFilePath);
            files = dir.listFiles();

        } catch (IOException ex){
            JOptionPane.showConfirmDialog(
                    null, 
                    ex.toString(),
                    "Login Error?",
                    JOptionPane.ERROR_MESSAGE
                    );
            
            return files;
        }
        return files;
    }
    
    
}
