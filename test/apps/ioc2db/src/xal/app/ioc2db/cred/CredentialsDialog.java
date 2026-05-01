package xal.app.ioc2db.cred;

import java.awt.Frame;

/**
 * Retrieves the user ID and password credentials for a remote
 * account.
 *
 * @author mji
 * @author Christopher K. Allen
 */
public class CredentialsDialog extends javax.swing.JDialog {

    
    /*
     * Global Constants
     */
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;

    
    
    
    /*
     * Global Operations
     */
    
    /**
     * Checks the user credentials for the SNS Database.  If the
     * credentials have not been set then a credentials dialogue
     * box is opened where the user may specify them.
     *
     * @param frmParent     the frame owner of the displayed dialog 
     * @param crdTarget     the <code>AccountCredentials</code> to be modified
     * @param strTitle      the title displayed in the dialog
     * @param bolModal      a modal dialog is displayed if <code>true</code>
     *  
     * @return  <code>true</code> if the user credentials exist and are valid,
     *          <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    public static boolean showCredentialDialog(Frame frmParent, AccountCredentials crdTarget, String strTitle, boolean bolModal) {
        //        if( !crdTarget.isValid() ) {
        
        CredentialsDialog dlgCreds = new CredentialsDialog(frmParent, crdTarget, strTitle, bolModal);
        dlgCreds.setVisible(true);

        if (dlgCreds.getUserId() == null) {
            crdTarget.clearCredentials();

        } else if (dlgCreds.getUserId().length()>0){
            crdTarget.setCredentials(dlgCreds.getUserId(), dlgCreds.getPassword());

        }else{
            crdTarget.clearCredentials();

        }
        //        }

        return crdTarget.isValid();
    }


    
    
    
    /*
     * Local Attributes
     */
    
    
    /*
     * User Input
     */
    
    /** The account user ID we are retrieving */
    private String      strUserId;

    /** The account password for the credentials */
    private char[]      chrPassword;

   

    /*
     * GUI Components - Machine generated
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    
    
    /*
     * Initialization
     */
    
    /** 
     * Creates new <code>CredentialsDialog</code> object 
     * with the given parameters.
     *  
     * @param frmParent    The owning SWING window frame 
     * @param bolModal     make this dialog modal or not 
     * @param strTitle     dialog title to display
     */
    public CredentialsDialog(Frame frmParent, String strTitle, boolean bolModal) {
        super(frmParent, strTitle, bolModal);
        
        this.strUserId   = null;
        this.chrPassword = null;
        
        initComponents();
        getRootPane().setDefaultButton(jButton1);
    }
    
    
    /** 
     * Creates new <code>CredentialsDialog</code> object 
     * with the given parameters.
     *  
     * @param frmParent    The owning SWING window frame 
     * @param crdTgt       the <code>AccountCredentials</code> to be modified
     * @param bolModal     make this dialog modal or not 
     * @param strTitle     dialog title to display
     */
    public CredentialsDialog(Frame frmParent, AccountCredentials crdTgt, String strTitle, boolean bolModal) {
        super(frmParent, strTitle, bolModal);
        
        this.strUserId = crdTgt.getUserId();
        if (crdTgt.getPassword() != null)
            this.chrPassword = crdTgt.getPassword().toCharArray();
        
        initComponents();
        getRootPane().setDefaultButton(jButton1);
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Returns the user ID for the remote account.
     *
     * @return  user ID account credentials
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    public String   getUserId() {
        return this.strUserId;
    }
    
    /**
     * Returns the password for the current remote account.
     *
     * @return  password for the current account.
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    public char[]   getPassword() {
        return this.chrPassword;
    }
    
    
    
    /*
     * Support Methods
     */
    
    /**
     * Initializes all the GUI components.  I mean everything.  From creation, to 
     * placement, to event response.  Pretty sure this was machine written that's why
     * it's such a piece of crap.
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField(this.getUserId());

        if (this.getPassword() == null)
            jPasswordField1 = new javax.swing.JPasswordField( );
        else
            jPasswordField1 = new javax.swing.JPasswordField( new String(this.getPassword()) );
            
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("<html>Please enter credentials to be used. \nThese credentials will not be stored in any file. \nThey will be used for all sessions during the program execution.</html>");

        jButton1.setText("OK");
        jButton1.setSelected(true);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel2.setText("User Name:");

        jLabel3.setText("Password:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(47, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel3))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jPasswordField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                                                .addContainerGap())
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGap(74, 74, 74)
                                                        .addComponent(jButton1)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                                                        .addComponent(jButton2)
                                                        .addGap(60, 60, 60))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jButton1)
                                                .addComponent(jButton2))
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        chrPassword = jPasswordField1.getPassword();
        strUserId = jTextField1.getText();
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        chrPassword = null;
        strUserId = null;
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed


    
    
    /*
     * Testing and Debugging
     */
    
//    /**
//     * This is a driver - I guess it's for testing and debugging.
//     * 
//     * @param args  the command line arguments
//     */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                CredentialsDialog dialog = new CredentialsDialog(new JFrame(), "IcsCompCredentials", true);
//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        System.exit(0);
//                    }
//                });
//                dialog.setVisible(true);
//            }
//        });
//    }

}
