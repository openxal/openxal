/*
 * MainWindow.java
 *
 * Created on August 25, 2008, 1:25 PM
 */


package xal.app.ioc2db;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import xal.app.ioc2db.cred.CredentialsDialog;
import xal.app.ioc2db.cred.DbCredentials;
import xal.app.ioc2db.cred.IocCredentials;


/**
 * <p>
 * Main window for the application.  This class also does pretty much
 * everything as the application really has no architecture.  The 
 * main window is created and displayed here.  All the GUI responses are
 * defined here and all the actions are implemented.
 * </p>
 * <p>
 * <h4>CKA Notes</h4>
 * &middot; I have created the class <code>{@link MainApplication}</code> and moved many of the
 * utility methods to there.  These methods have been made static.
 * <br/>
 * &middot; The application entry point 
 * <code>{@link MainApplication#main(String[])}</code> has also been moved to the
 * <code>MainApplication</code> class.
 * <br/>
 * &middot; I have renamed this class from <code>PV_Names</code> to <code>MainWindow</code>
 * in an attempt to be more descriptive and to differentiate the newer application version 
 * from the old.
 * </p>  
 *
 * @author  Mariano J. Padilla
 *          Internship / HERE program Internship
 *          
 * @author  Christopher K. Allen
 * @since   Aug 25, 2008
 * @version Aug 22, 2011
 *
 */
public class MainWindow extends javax.swing.JFrame {

    


    /*
     * Global Constants 
     */
    

    /** Serialization version */
    private static final long serialVersionUID = 1L;

    
    
    /** Title displayed in the credentials dialog for the IOC host */
    private static final String STR_TITLE_DLG_IOC_CREDS = "IOC Host Credentials";

    /** Title displayed in the credentials dialog for the SNS database */
    private static final String STR_TITLE_DLG_DB_CREDS = "SNS Database Credentials";

    
    
    
    /*
     * Internal Classes
     */
    
    /**
     * Information record about an IOC which is moved
     * in and out of the database.
     * 
     *
     * @author Christopher K. Allen
     * @since   Aug 22, 2011
     */
    public static class IocRecord {
        
        /** The IOC device identifier */
        public String       strDvcId;
        
        /** The IP address of the IOC */
        public String       strIpAddr;
        
        
        /**
         * Initializing constructor.  Create a new <code>IocRecord</code>
         * initialized to the given device identifier and its IP address.
         * 
         * @param strDvcId      device name 
         * @param strIpAddr     IP address off the device
         *
         * @author  Christopher K. Allen
         * @since   Aug 22, 2011
         */
        public IocRecord(String strDvcId, String strIpAddr) {
            this.strDvcId  = strDvcId;
            this.strIpAddr = strIpAddr;
        }


        
        /*
         * Attributes
         */
        
        
        /**
         * Returns the device identifier field of the record.
         * 
         * @return  device identifier string
         */
        public String getDeviceId() {
            return strDvcId;
        }


        /**
         * Returns the device's IP address.
         * 
         * @return the IOC IP address field
         */
        public String getIpAddress() {
            return strIpAddr;
        }


    };
    
    
    
    /*
     * Local Attributes
     */
    
    
    //
    // Application Resources
    //
    
//    /** User credentials for the (diagnostics) ICS computers */
//    public IocCredentials    crdIocHosts;
//    
//    /** User credentials for the SNS database */
//    public DbCredentials     crdSnsDb;
//    
    
    /** The application main data document */
    final private MainDocument          docMain;
    
    /** Collection of resources used for application tasks */
    final private AppResources          rscAppl;
    
  
    
    
    //
    // GUI Components
    //
     
    // </editor-fold>
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblRcrdCnt;
    private javax.swing.JLabel guiProgressLbl;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JProgressBar guiDbPrgBar;
    private javax.swing.JMenuItem jRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem3;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem4;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane paneMainTab;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable guiMainTbl;
    private javax.swing.JTextField txtDvcFilter;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    // End of variables declaration//GEN-END:variables

    
    
    /*
     * Initialization
     */
    
    /**
     * Creates new application object <code>MainWindow</code>.
     * The main GUI window is built, the response actions are
     * initialized, and initial settings are read from the 
     * configuration file.
     * 
     * @param   docMain     the main data document for the application
     * 
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    public MainWindow(MainDocument docMain) {
        
//        this.crdIocHosts = new IocCredentials();
//        this.crdSnsDb    = new DbCredentials();
        this.docMain = docMain;
        this.rscAppl = new AppResources();
        
        initGuiComponents();
        initAppSettings();
    }
    
    
    /**
     * <p> 
     * This method is called from within the constructor to
     * initialize the form.  It builds, places, and sets action
     * responses for all the GUI components.  Apparently it was
     * generated automatically, that's why it's awful. 
     * </p>
     * <p>
     * <h4>WARNING:</h4>
     * Do NOT modify this code unless you know what you're doing. 
     * The content of this method was regenerated by the Netbeans
     * Form Editor.  
     * <br/>
     * <br/>
     * CKA - I guess the Netbeans GUI builder generated this..
     * </p>
     */
    private void initGuiComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        paneMainTab = new javax.swing.JTabbedPane();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        guiDbPrgBar = new javax.swing.JProgressBar();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        txtDvcFilter = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        lblRcrdCnt = new javax.swing.JLabel();
        guiProgressLbl = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        jButton14 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        guiMainTbl = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jRadioButtonMenuItem1 = new javax.swing.JMenuItem();
        jRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem4 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem5 = new javax.swing.JRadioButtonMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem2 = new javax.swing.JCheckBoxMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PV Names File Retrieval");
        setResizable(false);

        jButton1.setText("Test IOCs");
        jButton1.setToolTipText("This button will allow you to test the application using User Directories defined as test IOC's.");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Get XLS content from selected");
        jButton2.setToolTipText("Gets all the XLS files from the selected IOC's on the table above and adds a tab in the pane below for each XLS");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actGetIocInfoFromXls(evt);
            }
        });

        paneMainTab.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        paneMainTab.setAutoscrolls(true);

        jButton3.setText("Select All");
        jButton3.setToolTipText("Selects all records on above table");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Remove Tabs");
        jButton4.setToolTipText("Removes all tabs from the below tabbed pane");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        // Connects to the IOC, reads the XLS file of PVs, and displays the results
        jButton5.setText("Parse PVs from selected");
        jButton5.setToolTipText("Parses the PV names from the XLS files of the selected IOC's on the table above. The PV names are displayed in a tab below for each selected IOC.");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Insert/Update PV names from All Records");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actUpdateDbWithAllIocRecords(evt);
            }
        });

        jButton7.setText("Insert/Update PV names from selected only");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actUpdateDbWithSelectedPvs(evt);
            }
        });

        jButton8.setText("Get IOC List from Oracle DB");
        jButton8.setToolTipText("Retrieves the list of IOC's and IP addresses from ORACLE Database");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actFetchIocListFromDb(evt);
            }
        });

        jButton9.setText("Get Selected IOC status");
        jButton9.setToolTipText("Gets the access status from selected IOC's on the table below");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jLabel1.setText("Filter for DB IOC's (MEBT or MEBT_Diag, etc):");

        jTextField2.setText("160.91.232.117");
        jTextField2.setToolTipText("IP enter ###.###.###.##");

        jLabel4.setText("Enter IP address of host");

        jButton10.setText("Get PV's from Specified IOC");
        jButton10.setToolTipText("This button gets the XLS file and parses the PV names from the XLS file for the specified IOC address given.");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actReadSignalsFromIocs(evt);
            }
        });

        jButton12.setText("Get DB contents from selected");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actReadEpicsIocDb(evt);
            }
        });

        jButton13.setText("Get pvs in Oracle DB");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jTextField3.setToolTipText("Enter in %BCM% format including %. For Multiple use sequence from SYS to Signal Name i.e. '%CCL%Diag%BCM%102%'");

        jButton14.setText("Show PV data to be uploaded for selected");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jLabel5.setText("jLabel5");

        jLabel6.setText("jLabel6");

        guiMainTbl.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {

                }
        ));
        jScrollPane1.setViewportView(guiMainTbl);

        jTabbedPane2.addTab("Hosts Table", jScrollPane1);

        jMenu1.setText("File");

        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Export");

        jMenuItem2.setText("Export Hosts Table");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuItem3.setText("Export Selected Tab");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Credentials");

        jMenuItem4.setText("IOC Host Credentials");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actEnterIocHostCredentials(evt);
            }
        });
        jMenu3.add(jMenuItem4);

        jMenuItem5.setText("Database Credentials");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem5);

        jMenuBar1.add(jMenu3);

        jMenu4.setText("Database");

        buttonGroup1.add(jRadioButtonMenuItem1);
//        jRadioButtonMenuItem1.setSelected(false);
        jRadioButtonMenuItem1.setText("Database network location...");
        jRadioButtonMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actSetDbAddress(evt);
            }
        });
        jMenu4.add(jRadioButtonMenuItem1);

//        buttonGroup1.add(jRadioButtonMenuItem2);
//        jRadioButtonMenuItem2.setText("Development");
//        jRadioButtonMenuItem2.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                jRadioButtonMenuItem2ActionPerformed(evt);
//            }
//        });
//        jMenu4.add(jRadioButtonMenuItem2);
        jMenu4.add(jSeparator1);

        jMenuItem6.setText("Define schema and table...");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem6);
        jMenu4.add(jSeparator2);

        buttonGroup2.add(jRadioButtonMenuItem3);
        jRadioButtonMenuItem3.setSelected(true);
        jRadioButtonMenuItem3.setText("Insert new records only (Default)");
        jRadioButtonMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem3ActionPerformed(evt);
            }
        });
        jMenu4.add(jRadioButtonMenuItem3);

        buttonGroup2.add(jRadioButtonMenuItem4);
        jRadioButtonMenuItem4.setText("Update matching records only");
        jRadioButtonMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem4ActionPerformed(evt);
            }
        });
        jMenu4.add(jRadioButtonMenuItem4);

        buttonGroup2.add(jRadioButtonMenuItem5);
        jRadioButtonMenuItem5.setText("Insert new and Update matching records");
        jRadioButtonMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem5ActionPerformed(evt);
            }
        });
        jMenu4.add(jRadioButtonMenuItem5);

        jMenuBar1.add(jMenu4);

        jMenu5.setText("Options");

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("Show Progress in Progressbar");
        jCheckBoxMenuItem1.setToolTipText("Selecting Progressbar to update may decrease the performance.");
        jCheckBoxMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem1ActionPerformed(evt);
            }
        });
        jMenu5.add(jCheckBoxMenuItem1);

        jCheckBoxMenuItem2.setText("LANL Sftw. WS");
        jCheckBoxMenuItem2.setToolTipText("By selecting this, the program will look in the c:\\daDLL folder for the appropriate file.");
        jCheckBoxMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem2ActionPerformed(evt);
            }
        });
        jMenu5.add(jCheckBoxMenuItem2);

        jMenuBar1.add(jMenu5);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 973, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                .addComponent(jLabel4)
                                                                .addComponent(jLabel1))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                                        .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(txtDvcFilter, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                                .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                .addComponent(jButton10))
                                                                                .addGap(6, 6, 6)
                                                                                .addComponent(jButton9)
                                                                                .addGap(10, 10, 10)
                                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                        .addComponent(lblRcrdCnt, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addGroup(layout.createSequentialGroup()
                                                                                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(jButton13))))
                                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                                                                .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                                                .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                        .addComponent(jButton2)
                                                                                                                        .addComponent(jButton12))
                                                                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                                                                                .addComponent(jButton14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                                                                .addComponent(jButton5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                                                                                .addGap(114, 114, 114)
                                                                                                                                .addComponent(jButton1)
                                                                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                        .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                                                                                                                                        .addComponent(jButton7)))
                                                                                                                                        .addComponent(paneMainTab, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 963, Short.MAX_VALUE)
                                                                                                                                        .addComponent(guiProgressLbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 963, Short.MAX_VALUE)
                                                                                                                                        .addComponent(guiDbPrgBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 963, Short.MAX_VALUE)
                                                                                                                                        .addGroup(layout.createSequentialGroup()
                                                                                                                                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 494, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)))
                                                                                                                                                .addContainerGap())))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel4)
                                                .addComponent(jButton10)
                                                .addComponent(jButton13)
                                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel1)
                                                        .addComponent(txtDvcFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jButton8)
                                                        .addComponent(jButton9)))
                                                        .addComponent(lblRcrdCnt, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(jButton3)
                                                                .addComponent(jButton2)
                                                                .addComponent(jButton5)
                                                                .addComponent(jButton1)
                                                                .addComponent(jButton6))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jButton7)
                                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jButton4)
                                                                                .addComponent(jButton12)
                                                                                .addComponent(jButton14)))
                                                                                .addGap(10, 10, 10)
                                                                                .addComponent(paneMainTab, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(guiProgressLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(guiDbPrgBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                        .addComponent(jLabel5)
                                                                                        .addComponent(jLabel6))
                                                                                        .addContainerGap(17, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        initTableWithDefaultTestingIoc();
        guiDbPrgBar.setValue(0);
    }//GEN-LAST:event_jButton1ActionPerformed
    
    /**
     * I think we are getting the PV names from the Excel spreadsheet file
     * on the IOC host machine.  This is a response function to some user event,
     * apparently the "Button #2" action.
     *
     * @param evt   the event object
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    private void actGetIocInfoFromXls(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if(jCheckBoxMenuItem2.isSelected()){
            javax.swing.JOptionPane.showMessageDialog(this, 
                    "When LANL WS IOC is selected, there is no XLS file to read."+
                    "\n"+
                    "If this is not a LANL WS IOC, please clear the check box from the Options menu.", 
                    "No XLS file to be found", 
                    javax.swing.JOptionPane.WARNING_MESSAGE
                    );
            
            return;
        }
        
        if ( !this.checkIocCredentials() )
            return;
        
        checkSelectedIocAvailability();
        String skipped="";
        if(guiMainTbl.getSelectedRowCount()>0){
            for(int x=0;x<guiMainTbl.getRowCount();x++){
                if(guiMainTbl.isRowSelected(x)){
                    if(guiMainTbl.getValueAt(x, 1).toString().equalsIgnoreCase("true")){
                        if((rscAppl.IsSet()==false)||(guiMainTbl.getValueAt(x, 2).toString().compareToIgnoreCase(rscAppl.gethost())!=0)){
                            //rscAppl.setall(readPVJCIFSFile(jTable1.getValueAt(x, 2).toString()),xlsfilebyline(jTable1.getValueAt(x, 2).toString()) ,dblinebyline(jTable1.getValueAt(x, 2).toString()) , getJCIFSFileName(jTable1.getValueAt(x, 2).toString(),"xls"), getJCIFSFileName(jTable1.getValueAt(x, 2).toString(),"db"),jTable1.getValueAt(x, 2).toString());
                            loadAppResources(guiMainTbl.getValueAt(x, 2).toString());
                        }
                        String      strTitle   = guiMainTbl.getValueAt(x, 0).toString() +" xls file";
                        String      strXlsFile = MainApplication.arrayToString(rscAppl.getxlsfile() ) ;
                        JTextArea   txtXlsFile = new JTextArea( strXlsFile );
                        JScrollPane scrXlsFile = new JScrollPane( txtXlsFile );

                        paneMainTab.addTab(strTitle, scrXlsFile);
                        //                                paneMainTab.addTab(guiMainTbl.getValueAt(x, 0).toString() +" xls file", 
                        //                                        new javax.swing.JScrollPane(
                        //                                                new javax.swing.JTextArea(MainApplication.arrayToString(rscAppl.getxlsfile() )
                        //                                                        )
                        //                                                )
                        //                                );
                    }else{
                        skipped +=guiMainTbl.getValueAt(x, 0).toString()+"\n";
                    }
                }
            }
        }
        if(skipped.length()>0){
            paneMainTab.addTab("Skipped IOCs", new javax.swing.JScrollPane(new javax.swing.JTextArea(skipped)));
        }
    }//GEN-LAST:event_jButton2ActionPerformed
    
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        guiMainTbl.selectAll();
    }//GEN-LAST:event_jButton3ActionPerformed
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        if(paneMainTab.getTabCount()>0){
            paneMainTab.removeAll();
        }
    }//GEN-LAST:event_jButton4ActionPerformed
    
    
    /**
     * Connects to the IOC, reads the XLS file of PVs, and displays the results on a 
     * table in the GUI.
     *
     * @param evt   event object created by the GUI 
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if ( !this.checkIocCredentials() )
            return;

        checkSelectedIocAvailability();
        String skipped="";
        if(guiMainTbl.getSelectedRowCount()>0){
            for(int x=0;x<guiMainTbl.getRowCount();x++){
                if(guiMainTbl.isRowSelected(x)){
                    if(guiMainTbl.getValueAt(x, 1).toString().equalsIgnoreCase("true")){
                        if((rscAppl.IsSet()==false)||(guiMainTbl.getValueAt(x, 2).toString().compareToIgnoreCase(rscAppl.gethost())!=0)){
                            //rscAppl.setall(readPVJCIFSFile(jTable1.getValueAt(x, 2).toString()),xlsfilebyline(jTable1.getValueAt(x, 2).toString()) ,dblinebyline(jTable1.getValueAt(x, 2).toString()) , getJCIFSFileName(jTable1.getValueAt(x, 2).toString(),"xls"), getJCIFSFileName(jTable1.getValueAt(x, 2).toString(),"db"),jTable1.getValueAt(x, 2).toString());
                            loadAppResources(guiMainTbl.getValueAt(x, 2).toString());
                        }
                        String  strTitle    = guiMainTbl.getValueAt(x, 0).toString()+" PVs";
                        String  strPvInfo   = MainApplication.arrayToString( rscAppl.getpvs() );
                        JTextArea txtPvInfo = new JTextArea(strPvInfo);
                        JScrollPane scrPvInfo = new JScrollPane(txtPvInfo);

                        paneMainTab.addTab(strTitle, scrPvInfo);
                    }else{
                        skipped+=guiMainTbl.getValueAt(x, 0).toString()+"\n";
                    }
                }
            }
        }
        if(skipped.length()>0){
            paneMainTab.addTab("Skipped IOCs", new javax.swing.JScrollPane(new javax.swing.JTextArea(skipped)));
        }
    }//GEN-LAST:event_jButton5ActionPerformed
    

    /**
     *
     * @param evt
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    private void actUpdateDbWithAllIocRecords(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

        int ans = javax.swing.JOptionPane.showConfirmDialog(this,"Are you sure you want to update to ORACLE?","Update?",javax.swing.JOptionPane.YES_NO_OPTION);
        if (ans != javax.swing.JOptionPane.YES_OPTION)
            return;

        if( !this.checkIocCredentials() )
            return;

        if ( !this.checkDbCredentials() )
            return;
        
        String skippedIOCs = "";
        String text = "";
        String ipaddr = "";
        guiDbPrgBar.setMaximum(guiMainTbl.getSelectedRowCount());
        guiDbPrgBar.setMinimum(1);
        guiDbPrgBar.setValue(0);
        for(int x=0;x<guiMainTbl.getRowCount();x++){
            ipaddr=guiMainTbl.getValueAt(x, 2).toString();
            if(MainApplication.isIpAddress(ipaddr)){
                guiMainTbl.setValueAt(checkIocAvailability(ipaddr), x, 1);
            }else{
                guiMainTbl.setValueAt(MainApplication.isIpAddress(ipaddr), x, 1);
            }
            if(jCheckBoxMenuItem1.getState()){
                updateProgressBar(x,"Checking status of IOC's.");
            }
        }
        for(int x=0;x<guiMainTbl.getRowCount();x++){
            if(guiMainTbl.getValueAt(x, 1).toString().equals("false")){
                skippedIOCs += guiMainTbl.getValueAt(x, 0).toString();
            }
            if(jCheckBoxMenuItem1.getState()){
                updateProgressBar(x,"Making list of unavailable IOC's");
            }
        }
        for(int x=0;x<guiMainTbl.getRowCount();x++){
            if(guiMainTbl.getValueAt(x, 1).toString().equals("true")){
                if(jCheckBoxMenuItem1.getState()){
                    updateProgressBar(x,"Currently working on "+guiMainTbl.getValueAt(x, 0) + " - "+ guiMainTbl.getValueAt(x, 2));
                }
                Rectangle labelRect = guiProgressLbl.getBounds();
                labelRect.x = 0;
                labelRect.y = 0;
                guiProgressLbl.paintImmediately( labelRect );
                loadAppResources(guiMainTbl.getValueAt(x, 2).toString());
                String [][]pvs = rscAppl.getOraclePvs();
                for(int y=0;y<pvs.length-1;y++){
                    text += pvs[y][0].toString()+"\n";
                }
                paneMainTab.addTab(guiMainTbl.getValueAt(x, 0).toString()+" PVs", new javax.swing.JScrollPane(new javax.swing.JTextArea(text)));
                updateSnsDatabase(rscAppl.getOraclePvs());
                pvs = null;
            }
        }
        guiProgressLbl.setText("Process Completed");
        guiDbPrgBar.setValue(0);
        paneMainTab.addTab("Skipped IOCs",new javax.swing.JScrollPane(new javax.swing.JTextArea(skippedIOCs)));


    }//GEN-LAST:event_jButton6ActionPerformed
    

    /**
     * Updates the database with the selected PVs.
     *
     * @param evt   event object
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    private void actUpdateDbWithSelectedPvs(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        int ans = javax.swing.JOptionPane.showConfirmDialog(this,"Are you sure you want to update to ORACLE?","Update?",javax.swing.JOptionPane.YES_NO_OPTION);

        if (ans != javax.swing.JOptionPane.YES_OPTION)
            return;

        if ( !this.checkIocCredentials() || !this.checkDbCredentials() )
            return;

        String skippedIOCs = "";
        String text = "";//GEN-LAST:event_jButton7ActionPerformed
        String strIpAdd = "";

        guiDbPrgBar.setMaximum(guiMainTbl.getSelectedRowCount());
        guiDbPrgBar.setMinimum(1);
        guiDbPrgBar.setValue(0);

        for(int iRow=0;iRow<guiMainTbl.getRowCount();iRow++){
            if(jCheckBoxMenuItem1.getState()){
                updateProgressBar(iRow,"Checking status of selected IOC's.");
            }
            if(guiMainTbl.isRowSelected(iRow)){
                strIpAdd=guiMainTbl.getValueAt(iRow, 2).toString();
                if(MainApplication.isIpAddress(strIpAdd)){
                    guiMainTbl.setValueAt(checkIocAvailability(strIpAdd), iRow, 1);
                }else{
                    guiMainTbl.setValueAt(MainApplication.isIpAddress(strIpAdd), iRow, 1);
                }
            }
        }

        for(int iRow=0;iRow<guiMainTbl.getRowCount();iRow++){
            if(jCheckBoxMenuItem1.getState()){
                updateProgressBar(iRow,"Making list of unavailable IOC's");
            }
            if(guiMainTbl.isRowSelected(iRow)){
                if(guiMainTbl.getValueAt(iRow, 1).toString().equals("false")){
                    skippedIOCs += guiMainTbl.getValueAt(iRow, 0).toString();
                }
            }
        }

        for(int iRow=0;iRow<guiMainTbl.getRowCount();iRow++){
            if(guiMainTbl.isRowSelected(iRow)){
                if(guiMainTbl.getValueAt(iRow, 1).toString().equals("true")){
                    if(jCheckBoxMenuItem1.getState()){
                        updateProgressBar(iRow,"Currently working on "+guiMainTbl.getValueAt(iRow, 0) + " - "+ guiMainTbl.getValueAt(iRow, 2));
                    }
                    if( (rscAppl.IsSet()==false) || (guiMainTbl.getValueAt(iRow, 2)!=rscAppl.gethost()) ) {
                        // rscAppl.setall(readPVJCIFSFile(jTable1.getValueAt(x, 2).toString()),xlsfilebyline(jTable1.getValueAt(x, 2).toString()),
                        //  dblinebyline(jTable1.getValueAt(x, 2).toString()) , getJCIFSFileName(jTable1.getValueAt(x, 2).toString(),"xls"), 
                        //  getJCIFSFileName(jTable1.getValueAt(x, 2).toString(),"db"),jTable1.getValueAt(x, 2).toString());
                        loadAppResources(guiMainTbl.getValueAt(iRow, 2).toString());
                    }
                    String [][]pvs = rscAppl.getOraclePvs();
                    for(int y=0;y<pvs.length-1;y++){
                        text += pvs[y][0]+"\n";
                    }
                    paneMainTab.addTab(guiMainTbl.getValueAt(iRow, 0).toString()+" PVs", new javax.swing.JScrollPane(new javax.swing.JTextArea(text)));
                    updateSnsDatabase(rscAppl.getOraclePvs());
                    pvs = null;
                }
            }
        }

        guiProgressLbl.setText("Process Completed");
        guiDbPrgBar.setValue(0);
        paneMainTab.addTab("Skipped IOCs",new javax.swing.JScrollPane(new javax.swing.JTextArea(skippedIOCs)));
    }
    
    
    /**
     * Retrieves the list of valid IOC addresses for the ICS computers controlling
     * diagnostic equipment.
     *
     * @param evt   event object generated by GUI
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    private void actFetchIocListFromDb(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        if( !this.getDbCred().isValid() ) 
            CredentialsDialog.showCredentialDialog(this, this.getDbCred(), STR_TITLE_DLG_DB_CREDS, true);

        DefaultTableModel mdlTblData = new javax.swing.table.DefaultTableModel();
        
        mdlTblData.addColumn("IOC");
        mdlTblData.addColumn("Available");
        mdlTblData.addColumn("IP for Path");
        guiMainTbl.setModel(mdlTblData);

        List<IocRecord> lstIocRecs = this.queryIocList();
        int             x = 0;
        for ( IocRecord rec : lstIocRecs ) {
            mdlTblData.addRow(new Object[]{});

            String  strDvcId  = rec.getDeviceId();
            String  strIpAddr = rec.getIpAddress();
            
            guiMainTbl.setValueAt(strDvcId, x, 0);
            guiMainTbl.setValueAt(strIpAddr, x, 2);
            
            x++;
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        checkSelectedIocAvailability();
    }//GEN-LAST:event_jButton9ActionPerformed


    /**
     * Reads the PV signals from the ICS IOCs.
     *
     * @param evt   Object containing event info from GUI
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    private void actReadSignalsFromIocs(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        javax.swing.table.DefaultTableModel tb = new javax.swing.table.DefaultTableModel();
        tb.addColumn("IOC");
        tb.addColumn("Available");
        tb.addColumn("Path");
        int IOCct = 1;
        int x=0;
        while (x<=IOCct-1){
            tb.addRow(new Object[]{});
            x++;
        }
        guiMainTbl.setModel(tb);
        guiMainTbl.setValueAt("User defined IOC@"+jTextField2.getText(), 0, 0);
        guiMainTbl.setValueAt(jTextField2.getText(), 0, 2);

    }//GEN-LAST:event_jButton10ActionPerformed
    
    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        String mytext="";
        /*for(int x=0;x<jTable1.getRowCount();x++){
        mytext += jTable1.getValueAt(x, 0).toString()+"\t"+jTable1.getValueAt(x, 2).toString()+"\n";
    }*/
        mytext = MainApplication.parseTable(guiMainTbl);
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        int retval = fc.showSaveDialog(this);
        if(retval == javax.swing.JFileChooser.APPROVE_OPTION){
            boolean save = MainApplication.writeToFile(fc.getSelectedFile(),mytext);
            if(save){
                javax.swing.JOptionPane.showMessageDialog(this, "File saved at : "+fc.getSelectedFile().getAbsolutePath(),"File Saved",javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }else{
                javax.swing.JOptionPane.showMessageDialog(this, "File failed to save correctly. Please check name spelling or data to be saved.", "File did not save", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed
    
    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        javax.swing.JScrollPane pain = (javax.swing.JScrollPane)paneMainTab.getComponentAt(paneMainTab.getSelectedIndex());
        javax.swing.JViewport vp = pain.getViewport();
        String mytext="";
        String admsg="";
        if(vp.getComponent(0).toString().indexOf("Table")>0){
            javax.swing.JTable tp = (javax.swing.JTable)vp.getComponent(0);
            mytext = MainApplication.parseTable(tp);
            //System.out.println(mytext);
            tp=null;
            admsg = "Table exports are in comma delimited form. ";
        }else{
            javax.swing.JTextArea tp = (javax.swing.JTextArea)vp.getComponent(0);
            mytext = tp.getText();
            tp=null;
        }
        pain=null;
        vp=null;
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        int retval = fc.showSaveDialog(this);
        if(retval == javax.swing.JFileChooser.APPROVE_OPTION){
            boolean save = MainApplication.writeToFile(fc.getSelectedFile(),mytext);
            if(save){
                javax.swing.JOptionPane.showMessageDialog(this, admsg+"File saved at : "+fc.getSelectedFile().getAbsolutePath(),"File Saved",javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }else{
                javax.swing.JOptionPane.showMessageDialog(this, "File failed to save correctly. Please check name spelling or data to be saved.", "File did not save", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed
    
    
    /**
     * Responds to the user request for specifying new IOC host credentials.
     *
     * @param evt   the SWING event object (not used)
     *
     * @author Christopher K. Allen
     * @since  Sep 2, 2011
     */
    private void actEnterIocHostCredentials(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        CredentialsDialog.showCredentialDialog(this, this.getIocCred(), STR_TITLE_DLG_IOC_CREDS, true);
        
//        CredentialsDialog mycreds = new CredentialsDialog(this,true,"PV Host IcsCompCredentials");
//        mycreds.setVisible(true);
//        if(mycreds.uname.length()>0){
//            crdIocHosts.setAll(mycreds.uname,mycreds.psswd);
//        }else{
//            crdIocHosts.clearAll();
//        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed
    
    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        CredentialsDialog.showCredentialDialog(this, this.getDbCred(), STR_TITLE_DLG_DB_CREDS, true);
        
//        CredentialsDialog mycreds = new CredentialsDialog(this,true,"ORACLE IcsCompCredentials");
//        mycreds.setVisible(true);
//        if(mycreds.uname.length()>0){
//            crdSnsDb.setAll(mycreds.uname,mycreds.psswd);
//        }else{
//            crdSnsDb.clearAll();
//        }
    }//GEN-LAST:event_jMenuItem5ActionPerformed
    
    private void actReadEpicsIocDb(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed

        if(jCheckBoxMenuItem2.isSelected()){
            JOptionPane.showMessageDialog(this, 
                    "When LANL WS IOC is selected, there is no db file to read."+
                    "\n"+
                    "If this is not a LANL WS IOC, please clear the check box from the Options menu.", 
                    "No db file to be found", 
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }
        if( !this.checkIocCredentials() )
            return;

        checkSelectedIocAvailability();
        String skipped="";

        if(guiMainTbl.getSelectedRowCount()>0){
            for(int x=0;x<guiMainTbl.getRowCount();x++){
                if(guiMainTbl.isRowSelected(x)){
                    if(guiMainTbl.getValueAt(x, 1).toString().equalsIgnoreCase("true")){
                        if((rscAppl.IsSet()==false)||(guiMainTbl.getValueAt(x, 2).toString().compareToIgnoreCase(rscAppl.gethost())!=0)){
                            //rscAppl.setall(readPVJCIFSFile(jTable1.getValueAt(x, 2).toString()),xlsfilebyline(jTable1.getValueAt(x, 2).toString()) ,dblinebyline(jTable1.getValueAt(x, 2).toString()) , getJCIFSFileName(jTable1.getValueAt(x, 2).toString(),"xls"), getJCIFSFileName(jTable1.getValueAt(x, 2).toString(),"db"),jTable1.getValueAt(x, 2).toString());
                            loadAppResources(guiMainTbl.getValueAt(x, 2).toString());
                        }
                        String      strTitle        = guiMainTbl.getValueAt(x, 0).toString()+" db file";
                        String      strDbFile       = MainApplication.arrayToString( rscAppl.getdbfile() );
                        JTextArea   txtDbFile       = new JTextArea(strDbFile);
                        JScrollPane scrDbFile       = new JScrollPane(txtDbFile);

                        paneMainTab.addTab(strTitle, scrDbFile);
                    }else{
                        skipped +=guiMainTbl.getValueAt(x, 0).toString()+"\n";
                    }
                }
            }
        }
        if(skipped.length()>0){
            paneMainTab.addTab("Skipped IOCs", new javax.swing.JScrollPane(new javax.swing.JTextArea(skipped)));
        }

    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        int ans = 999;
        
        if ( !this.checkDbCredentials() )
            return;
        
        if(jTextField3.getText().isEmpty()){
            ans = javax.swing.JOptionPane.showConfirmDialog(this,"Are you sure you want to retrieve all records from ORACLE? If not specify filter","Get all PV's?",javax.swing.JOptionPane.YES_NO_OPTION);
        }else{
            ans = javax.swing.JOptionPane.YES_OPTION;
        }
        if (ans == javax.swing.JOptionPane.YES_OPTION){
            guiProgressLbl.setText("Querrying Database.");
            Rectangle rc = guiProgressLbl.getBounds();
            rc.x=0;
            rc.y=0;
            guiProgressLbl.paintImmediately(rc);
            String [][] IOCS= queryPvInfoFromOracle();
            javax.swing.table.DefaultTableModel tb = new javax.swing.table.DefaultTableModel();
            tb.addColumn("SGNL_ID");
            tb.addColumn("DVC_ID");
            tb.addColumn("REC_TYPE_ID");
            tb.addColumn("SGNL_NM");
            tb.addColumn("ARCH_FREQ");
            tb.addColumn("ARCH_TYPE");
            tb.addColumn("SGNL_ID_DESC");
            tb.addColumn("ACTIVE_IND");

            int x=0;
            while(x<IOCS.length){
                tb.addRow(new Object[]{});
                x++;
            }
            guiMainTbl.setModel(tb);
            x=0;
            while(x<IOCS.length){
                guiMainTbl.setValueAt(IOCS[x][0].toString(),x, 0);
                guiMainTbl.setValueAt(IOCS[x][1].toString(),x, 1);
                guiMainTbl.setValueAt(IOCS[x][2].toString(),x, 2);
                guiMainTbl.setValueAt(IOCS[x][3].toString(),x, 3);
                guiMainTbl.setValueAt(IOCS[x][4].toString(),x, 4);
                guiMainTbl.setValueAt(IOCS[x][5].toString(),x, 5);
                guiMainTbl.setValueAt(IOCS[x][6].toString(),x, 6);
                guiMainTbl.setValueAt(IOCS[x][7].toString(),x, 7);
                x++;
            }
        }
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        if( !this.checkIocCredentials() )
            return;


        checkSelectedIocAvailability();
        String skipped="";
        if(guiMainTbl.getSelectedRowCount()>0){
            for(int x=0;x<guiMainTbl.getRowCount();x++){
                if(guiMainTbl.isRowSelected(x)){
                    if(guiMainTbl.getValueAt(x, 1).toString().equalsIgnoreCase("true")){
                        if((rscAppl.IsSet()==false)||(guiMainTbl.getValueAt(x, 2)!=rscAppl.gethost())){
                            //rscAppl.setall(readPVJCIFSFile(jTable1.getValueAt(x, 2).toString()),xlsfilebyline(jTable1.getValueAt(x, 2).toString()) ,dblinebyline(jTable1.getValueAt(x, 2).toString()) , getJCIFSFileName(jTable1.getValueAt(x, 2).toString(),"xls"), getJCIFSFileName(jTable1.getValueAt(x, 2).toString(),"db"),jTable1.getValueAt(x, 2).toString());
                            loadAppResources(guiMainTbl.getValueAt(x, 2).toString());
                        }
                        javax.swing.table.DefaultTableModel tb = new javax.swing.table.DefaultTableModel();
                        tb.addColumn("SGNL_ID");
                        tb.addColumn("DVC_ID");
                        tb.addColumn("REC_TYPE_ID");
                        tb.addColumn("SGNL_NM");
                        tb.addColumn("ARCH_FREQ");
                        tb.addColumn("ARCH_TYPE");
                        tb.addColumn("SGNL_ID_DESC");
                        int y=0;
                        while(y<rscAppl.getOraclePvs().length){
                            tb.addRow(new Object[]{});
                            y++;
                        }
                        javax.swing.JTable jt = new javax.swing.JTable();
                        jt.setModel(tb);
                        String [][] opvs = rscAppl.getOraclePvs();
                        for(y=0;y<opvs.length;y++){
                            jt.setValueAt(opvs[y][0], y, 0);
                            jt.setValueAt(opvs[y][1], y, 1);
                            jt.setValueAt(opvs[y][2], y, 2);
                            jt.setValueAt(opvs[y][3], y, 3);
                            jt.setValueAt(opvs[y][4], y, 4);
                            jt.setValueAt(opvs[y][5], y, 5);
                            jt.setValueAt(opvs[y][6], y, 6);
                        }
                        paneMainTab.addTab(guiMainTbl.getValueAt(x, 0).toString()+" PVs to update to ORACLE", new javax.swing.JScrollPane(jt));
                        guiProgressLbl.setText("Total PV Count: "+opvs.length);
                        opvs=null;
                    }else{
                        skipped+=guiMainTbl.getValueAt(x, 0).toString()+"\n";
                    }
                }
            }
        }
        if(skipped.length()>0){
            paneMainTab.addTab("Skipped IOCs", new javax.swing.JScrollPane(new javax.swing.JTextArea(skipped)));
        }

    }//GEN-LAST:event_jButton14ActionPerformed

    private void actSetDbAddress(ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem1ActionPerformed

        DbCredentials   crdDb = this.getDbCred();
        
        String strAddr = (String)JOptionPane.showInputDialog(this,"Please enter database address: ",
                "Specify Database Address", 
                JOptionPane.PLAIN_MESSAGE, 
                null, 
                null, 
                crdDb.getDbAddress()
                );
        if( strAddr !=null && strAddr.length()>0 ){
            crdDb.setDbAddress(strAddr);
            jLabel5.setText("Currently using - "+crdDb.getDbAddress()+" using "+crdDb.getSchema()+"."+crdDb.getTableName());
        }
        
        jLabel5.setText("Currently using - " + crdDb.getDbAddress() + " using "+crdDb.getSchema()+"."+crdDb.getTableName());
        this.getDocument().getDbCredentials().setDbAddress(strAddr);
    }//GEN-LAST:event_jRadioButtonMenuItem1ActionPerformed

    private void jRadioButtonMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem2ActionPerformed
        DbCredentials       crdDb = this.getDbCred();
        String[]            items = crdDb.getDbItems();
        crdDb.setDbAddress(items[3]);
        jLabel5.setText("Currently using - "+items[3]+" using "+crdDb.getSchema()+"."+crdDb.getTableName());

    }//GEN-LAST:event_jRadioButtonMenuItem2ActionPerformed

    private void jCheckBoxMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem1ActionPerformed

    }//GEN-LAST:event_jCheckBoxMenuItem1ActionPerformed

    private void jRadioButtonMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem3ActionPerformed
        jLabel6.setText("Database mode: Insert new records only.");
    }//GEN-LAST:event_jRadioButtonMenuItem3ActionPerformed

    private void jRadioButtonMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem4ActionPerformed
        jLabel6.setText("Database mode: Update records only.");
    }//GEN-LAST:event_jRadioButtonMenuItem4ActionPerformed

    private void jRadioButtonMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem5ActionPerformed
        jLabel6.setText("Database mode: Insert new and Update records.");
    }//GEN-LAST:event_jRadioButtonMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed

        String s = (String)javax.swing.JOptionPane.showInputDialog(this,"Please enter Schema and Table to use in the following format: SCHEMA.TABLE","Define Schema and Table",javax.swing.JOptionPane.PLAIN_MESSAGE,null,null,"EPICS.SGNL_REC");
        if((s !=null) && (s.length()>0)){
            DbCredentials   crdDb = this.getDbCred();
            String[]        items = s.split("\\.");
            
            crdDb.setSchema(items[0]);
            crdDb.setTableName(items[1]);
            jLabel5.setText("Currently using - "+crdDb.getDbAddress()+" using "+crdDb.getSchema()+"."+crdDb.getTableName());
        }

    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jCheckBoxMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem2ActionPerformed
        rscAppl.clearall();
    }//GEN-LAST:event_jCheckBoxMenuItem2ActionPerformed

    
    /*
     * Support Methods
     */


    /**
     * REturns the main application document (we maintain a reference to it).
     *
     * @return  the main data document
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    private MainDocument    getDocument() {
        return this.docMain;
    }
    
    /**
     * Convenience method for returning the current credentials 
     * of the IOC user account.  These credentials are stored
     * in the application document.
     * 
     *
     * @return  IOC host user account credentials
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    private IocCredentials  getIocCred() {
        return this.getDocument().getIocCredentials();
    }
    
    /**
     * Convenience method for returning the current credentials 
     * of the SNS database user account.  These credentials are stored
     * in the application document.
     * 
     *
     * @return  SNS database user account credentials
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2011
     */
    private DbCredentials   getDbCred() {
        return this.getDocument().getDbCredentials();
    }
    
    /**
     * Makes a connection to the current database using the current
     * user account, then returns the connection object.
     *
     * @return  connect to the current database
     * 
     * @throws SQLException a database access error occurred
     *
     * @author Christopher K. Allen
     * @since  Sep 2, 2011
     */
    private Connection      makeDbConnection() throws SQLException {
        DbCredentials       crdSnsDb = this.getDbCred();
        
//        DriverManager.getConnection("jdbc:oracle:thin:@"+crdSnsDb.getDbAddress(),
//                crdSnsDb.getUserId(),
//                crdSnsDb.getPassword()
//                );
        Connection  cntDb = DriverManager.getConnection(crdSnsDb.getDbAddress(),
                crdSnsDb.getUserId(),
                crdSnsDb.getPassword()
                );
        
        return cntDb;
    }
    
    /**
     * Checks the user credentials for the IOC hosts.  A dialog box is
     * is shown to the user if the credentials are invalid.
     *
     * @return  <code>true</code> if the user credentials have been set,
     *          <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    private boolean checkIocCredentials() {
        if ( !this.getIocCred().isValid() ) {
            JOptionPane.showMessageDialog(
                    this, 
                    "No IOC credentials provided", 
                    "Cannot Continue", 
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        return true;
    }
    
    /**
     * Checks the user credentials for the IOC hosts.  A dialog box is
     * is shown to the user if the credentials are invalid.
     *
     * @return  <code>true</code> if the user credentials have been set,
     *          <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    private boolean checkDbCredentials() {
        if ( !this.getDbCred().isValid() ) {
            JOptionPane.showMessageDialog(
                    this, 
                    "No database credentials provided", 
                    "Cannot Continue", 
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        return true;
    }
    
    
//    /**
//     * Checks the user credentials for the IOC hosts.  If the
//     * credentials have not been set then a credentials dialogue
//     * box is opened where the user may specify them.
//     *
//     * @return  <code>true</code> if the user credentials exist and are valid,
//     *          <code>false</code> otherwise
//     *
//     * @author Christopher K. Allen
//     * @since  Aug 23, 2011
//     */
//    private boolean checkIocCredentials(){
//        if(crdIocHosts.isValid()==false){
//            CredentialsDialog mycreds = new CredentialsDialog(this,true,STR_TITLE_DLG_IOC_CREDS);
//            mycreds.setVisible(true);
//            if(mycreds.getUserId().length()>0){
//                crdIocHosts.setCredentials(mycreds.getUserId(),mycreds.getPassword());
//            }else{
//                crdIocHosts.clearCredentials();
//            }
//        }
//        return crdIocHosts.isValid();
//    }
//    
//    /**
//     * Checks the user credentials for the SNS Database.  If the
//     * credentials have not been set then a credentials dialogue
//     * box is opened where the user may specify them.
//     *
//     * @return  <code>true</code> if the user credentials exist and are valid,
//     *          <code>false</code> otherwise
//     *
//     * @author Christopher K. Allen
//     * @since  Aug 23, 2011
//     */
//    private boolean checkDbCredentials(){
//        if(crdSnsDb.isValid()==false){
//            CredentialsDialog mycreds = new CredentialsDialog(this,true,STR_TITLE_DLG_DB_CREDS);
//            mycreds.setVisible(true);
//            if(mycreds.getUserId().length()>0){
//                crdSnsDb.setCredentials(mycreds.getUserId(),mycreds.getPassword());
//            }else{
//                crdSnsDb.clearCredentials();
//            }
//        }
//        return crdSnsDb.isValid();
//    }
    
    /**
     * Checks the availability of all the IOCs currently listed in the the 
     * main table of the GUI (we assume that the IOCs have been previously
     * pulled into the table).   If an IOC is reachable via its IP address
     * we check the appropriate column in the main table, otherwise we set
     * it <code>false</code>.
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    private void checkSelectedIocAvailability(){
        if(!checkIocCredentials())
            return;

        // Set up the progress bar
        guiDbPrgBar.setMaximum(guiMainTbl.getRowCount());
        guiDbPrgBar.setValue(0);

        for(int x=0;x<guiMainTbl.getRowCount();x++) {
            
            if(jCheckBoxMenuItem1.getState()){
                updateProgressBar(x,"Getting status of all selected IOC's");
            }

            if(guiMainTbl.isRowSelected(x)){
                String strIpAddr=guiMainTbl.getValueAt(x, 2).toString();

                if(MainApplication.isIpAddress(strIpAddr)){
                    guiMainTbl.setValueAt(checkIocAvailability(strIpAddr), x, 1);

                }else{
                    guiMainTbl.setValueAt(false, x, 1);

                }
            }
        }

        guiProgressLbl.setText("");
        guiDbPrgBar.setValue(0);
    }
    
    // <editor-fold defaultstate="collapsed" desc="INI File">
    /**
     * Initializes the state of the application, including the GUI
     * state.
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    private void initAppSettings(){
//        File f = new File(System.getProperty("user.dir") + MainApplication.STR_CONFIG_FILE);
//        if( !f.exists() )
//            MainApplication.createInitFile(f);
//        
//        DbCredentials   crdSnsDb = this.getDbCred();
//        String[]        arrLines = MainApplication.loadFile(f).split("\n");
//        if(arrLines.length<8){
//            f.delete();
//            MainApplication.createInitFile(f);
//            arrLines = MainApplication.loadFile(f).split("\n");
//        }
//        
//        crdSnsDb.setDbItems(arrLines);
        
        DbCredentials   crdSnsDb = this.getDbCred();
        
        jLabel5.setText("Currently using - "+crdSnsDb.getDbAddress()+" using "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName());

//        if(jRadioButtonMenuItem1.isSelected()){
//            crdSnsDb.setDbAddress(arrLines[1]);
//            crdSnsDb.setSchema(arrLines[5]);
//            crdSnsDb.setTableName(arrLines[7]);
//            jLabel5.setText("Currently using - "+arrLines[1]+" using "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName());
//            jLabel5.setText("Currently using - "+crdSnsDb.getDbAddress()+" using "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName());
//        }
        if(jRadioButtonMenuItem2.isSelected()){
//            crdSnsDb.setDbAddress(arrLines[3]);
//            crdSnsDb.setSchema(arrLines[5]);
//            crdSnsDb.setTableName(arrLines[7]);
//            jLabel5.setText("Currently using - "+arrLines[3]+" using "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName());
            jLabel5.setText("Currently using - "+crdSnsDb.getDbAddress()+" using "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName());
        }
        jLabel6.setText("Database mode: Insert new records only.");
    }

    /**
     *  Sets GUI main table with the default IP addresses of
     *  for the IOC hosts.  <em>The default values are contained in 
     *  this method.</em>
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    private void initTableWithDefaultTestingIoc(){
        DefaultTableModel tb = new DefaultTableModel();
        tb.addColumn("IOC");
        tb.addColumn("Available");
        tb.addColumn("Path");
        int cntIocs = 3;
        int x = 0;
        while (x<=cntIocs-1){
            tb.addRow(new Object[]{});
            x++;
        }
        guiMainTbl.setModel(tb);
        guiMainTbl.setValueAt("Test IOC", 0, 0);
        guiMainTbl.setValueAt("160.91.232.117", 0, 2);
        guiMainTbl.setValueAt("Test IOC2", 1, 0);
        guiMainTbl.setValueAt("160.91.232.1117", 1, 2);
        guiMainTbl.setValueAt("Test IOC3", 2, 0);
        guiMainTbl.setValueAt("160.91.232.117", 2, 2);
    }
    
    /**
     * Updates the GUI's progress bar to the given value
     * and post the given message to the GUI's progress
     * text field.
     *
     * @param iVal         progress value (relative to the initialized value)
     * 
     * @param strMsg       message to be posted to the application progress label
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    private void updateProgressBar(int iVal, String strMsg){
        Rectangle rc;
        
        guiProgressLbl.setText(strMsg);
        rc = guiProgressLbl.getBounds();
        rc.x=0;
        rc.y=0;
        guiProgressLbl.paintImmediately(rc);
        
        guiDbPrgBar.setValue(iVal);
        rc = guiDbPrgBar.getBounds();
        rc.x=0;
        rc.y=0;
        guiDbPrgBar.paintImmediately(rc);
    }
    
    /**
     * I believe this method collects the PV signal data from the GUI main
     * table and inserts it into the database.  Thus, this is the primary
     * method for the application.  The PV data is passed in as an array of string,
     * rather than a data structure as it should be.  The form of the array appears
     * to be as follows:
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <code>arrPvs[] = { <tt>SGNL_ID, DVC_ID, REC_TYPE_ID, SGNL_NM, ARCH_FREQ, ARCH_TYPE, SGNL_ID_DESC, ACTIVE_IND</tt>}
     * <br/>
     * <br/> 
     *
     * @param arrPvs    String array containing PV information
     * 
     * @return          <code>true</code> if update was successful and <code>false</code> otherwise
     *
     * @author Mariano Ruiz
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    private boolean updateSnsDatabase(String [][] arrPvs){
        boolean bolUpdated = false;
        
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        }catch (Exception ex){
            System.out.println("Error at New instance of Driver. "+ex.toString());
        }
        
        DbCredentials   crdSnsDb = this.getDbCred();
        try{
            
//            Connection      conSnsDb = DriverManager.getConnection("jdbc:oracle:thin:@"+crdSnsDb.getDbAddress(),crdSnsDb.getUserId(),crdSnsDb.getPassword());
            Connection      conSnsDb = this.makeDbConnection();
            try{
                String SQLTxt1 = null;
                Statement stmt1=null;
                Statement stmt2=null;
                ResultSet rs = null;
                stmt1 = conSnsDb.createStatement();
                guiDbPrgBar.setMaximum(arrPvs.length);
                guiDbPrgBar.setMinimum(0);
                guiDbPrgBar.setValue(0);
                for(int x = 0;x<arrPvs.length-1;x++){
                    if(jCheckBoxMenuItem1.getState()){
                        updateProgressBar(x,"Processing: "+arrPvs[x][0]);
                        
                    }
                    if(jRadioButtonMenuItem3.isSelected()){
                        SQLTxt1 = "INSERT INTO "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName()+"(SGNL_ID,DVC_ID,REC_TYPE_ID,SGNL_NM,ARCH_FREQ,ARCH_TYPE,SGNL_ID_DESC,ACTIVE_IND) VALUES('"+arrPvs[x][0]+"','"+arrPvs[x][1]+"','"+arrPvs[x][2]+"','"+arrPvs[x][3]+"','"+arrPvs[x][4]+"','"+arrPvs[x][5]+"','"+arrPvs[x][6]+"','Y')";
                        
                    }
                    if(jRadioButtonMenuItem4.isSelected()){
                        SQLTxt1 = "Update "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName()+" SET DVC_ID='"+arrPvs[x][1]+"', REC_TYPE_ID='"+arrPvs[x][2]+"', SGNL_NM='"+arrPvs[x][3]+"', ARCH_FREQ='"+arrPvs[x][4]+"', ARCH_TYPE='"+arrPvs[x][5]+"', SGNL_ID_DESC='"+arrPvs[x][6]+"', ACTIVE_IND='Y' WHERE SGNL_ID='"+arrPvs[x][0]+"'";
                        
                    }
                    if(jRadioButtonMenuItem5.isSelected()){
                        
                        stmt2 = conSnsDb.createStatement();
                        String SQLText2="Select Count(SGNL_ID) ct FROM "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName()+" WHERE SGNL_ID='"+arrPvs[x][0]+"'";
                        rs = stmt2.executeQuery(SQLText2);
                        int r=0;
                        if(rs.next()){
                            r = rs.getInt(1);
                        }else{
                            r =0;
                        }
                        if(r>0){
                            SQLTxt1 = "Update "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName()+" SET DVC_ID='"+arrPvs[x][1]+"', REC_TYPE_ID='"+arrPvs[x][2]+"', SGNL_NM='"+arrPvs[x][3]+"', ARCH_FREQ='"+arrPvs[x][4]+"', ARCH_TYPE='"+arrPvs[x][5]+"', SGNL_ID_DESC='"+arrPvs[x][6]+"', ACTIVE_IND='Y' WHERE SGNL_ID='"+arrPvs[x][0]+"'";
                        }else{
                            SQLTxt1 = "INSERT INTO "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName()+"(SGNL_ID,DVC_ID,REC_TYPE_ID,SGNL_NM,ARCH_FREQ,ARCH_TYPE,SGNL_ID_DESC,ACTIVE_IND) VALUES('"+arrPvs[x][0]+"','"+arrPvs[x][1]+"','"+arrPvs[x][2]+"','"+arrPvs[x][3]+"','"+arrPvs[x][4]+"','"+arrPvs[x][5]+"','"+arrPvs[x][6]+"','Y')";
                        }
                        rs.close();
                        stmt2.close();
                    }
                    //System.out.println(SQLTxt1);
                    stmt1.executeUpdate(SQLTxt1);

                }
                guiProgressLbl.setText("Process completed.");
                guiDbPrgBar.setValue(0);
                bolUpdated = true;
                stmt1.close();
                rs = null;
                stmt2=null;
                stmt1 = null;
                conSnsDb.close();
                
            } catch (Exception ex){
                System.out.println("Error at SQL. "+ex.toString());
                javax.swing.JOptionPane.showMessageDialog(this, "ORACLE Error: "+ex.toString(), "Error accessing ORACLE", javax.swing.JOptionPane.ERROR_MESSAGE);
                //OraCred.clearall();
                conSnsDb.close();
            }
            
        } catch (Exception ex){
            System.out.println("Error at Connection. "+ex.toString());
            javax.swing.JOptionPane.showMessageDialog(this, "ORACLE Error: "+ex.toString(), "Error accessing ORACLE", javax.swing.JOptionPane.ERROR_MESSAGE);
            crdSnsDb.clearCredentials();
        }


        return bolUpdated;
    }
    
    
    private String [][] queryPvInfoFromOracle(){
        String [][] pvs = null;
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        }catch (Exception ex){
            System.out.println("Error at New instance of Driver. "+ex.toString());
        }
        
        DbCredentials   crdSnsDb = this.getDbCred();
        try{
            //Connection con = DriverManager.getConnection("jdbc:oracle:thin:@snsdb1.sns.ornl.gov:1521:prod",OraCred.getname(),OraCred.getpsswd());
//            Connection conSnsDb = DriverManager.getConnection("jdbc:oracle:thin:@"+crdSnsDb.getDbAddress(),crdSnsDb.getUserId(),crdSnsDb.getPassword());
            Connection conSnsDb = this.makeDbConnection();
            try{
                String SQLTxt1 = null;
//                String [] items = null;   // CKA - commented out 4/16/2015
                String add="";
                if (jTextField3.getText().compareTo("")==0){
                    //SQLTxt1 = "Select SGNL_ID, DVC_ID, SGNL_NM from mji.SGNL_REC_COPY ORDER BY SGNL_ID";
                    SQLTxt1 = "Select SGNL_ID,DVC_ID,REC_TYPE_ID,SGNL_NM,ARCH_FREQ,ARCH_TYPE,SGNL_ID_DESC,ACTIVE_IND from "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName()+" ORDER BY SGNL_ID";
                }else{
                    add = "WHERE SGNL_ID like '"+jTextField3.getText()+"'";
                    //SQLTxt1 = "Select SGNL_ID, DVC_ID, SGNL_NM from mji.SGNL_REC_COPY "+add+" ORDER BY SGNL_ID";
                    SQLTxt1 = "Select SGNL_ID,DVC_ID,REC_TYPE_ID,SGNL_NM,ARCH_FREQ,ARCH_TYPE,SGNL_ID_DESC,ACTIVE_IND from "+crdSnsDb.getSchema()+"."+crdSnsDb.getTableName()+" "+add+" ORDER BY SGNL_ID";
                }
                Statement stmt1=null;
                ResultSet rs1=null;
                stmt1 = conSnsDb.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
                rs1 = stmt1.executeQuery(SQLTxt1);
                rs1.last();
                int rec = rs1.getRow();
                lblRcrdCnt.setText("Number of records: "+rec);
                Rectangle rc = lblRcrdCnt.getBounds();
                rc.x=0;
                rc.y=0;
                lblRcrdCnt.paintImmediately(rc);
                rs1.first();
                rs1.close();
                stmt1 = conSnsDb.createStatement();
                rs1 = stmt1.executeQuery(SQLTxt1);
                int x=0;
                //String txt = "";
                pvs = new String [rec][8];
                guiDbPrgBar.setMaximum(rec);
                guiDbPrgBar.setMinimum(x);
                while(rs1.next()){
                    if(jCheckBoxMenuItem1.getState()){
                        updateProgressBar(x,"Retrieving "+Integer.toString(x)+"/"+Integer.toString(rec));
                    }
                    pvs[x][0]=rs1.getString(1);
                    pvs[x][1]=rs1.getString(2);
                    if(rs1.getString(3)==null){
                        pvs[x][2]="null";
                    }else{
                        pvs[x][2]=rs1.getString(3)+"";
                    }
                    if(rs1.getString(4)==null){
                        pvs[x][3]="null";
                    }else{
                        pvs[x][3]=rs1.getString(4)+"";
                    }
                    if(rs1.getString(5)==null){
                        pvs[x][4]="null";
                    }else{
                        pvs[x][4]=rs1.getString(5)+"";
                    }
                    if(rs1.getString(6)==null){
                        pvs[x][5]="null";
                    }else{
                        pvs[x][5]=rs1.getString(6)+"";
                    }
                    if(rs1.getString(7)==null){
                        pvs[x][6]="null";
                    }else{
                        pvs[x][6]=rs1.getString(7)+"";
                    }
                    if(rs1.getString(7)==null){
                        pvs[x][6]="null";
                    }else{
                        pvs[x][6]=rs1.getString(7)+"";
                    }
                    if(rs1.getString(8)==null){
                        pvs[x][7]="null";
                    }else{
                        pvs[x][7]=rs1.getString(8)+"";
                    }
                    x++;
                }
                guiDbPrgBar.setValue(0);
                guiProgressLbl.setText("Operation completed.");
                rs1.close();
                conSnsDb.close();
                return pvs;
            }catch (Exception ex){
                System.out.println("Error at SQL. "+ex.toString());
                conSnsDb.close();
//                Statement stmt1=null;   // CKA - commented out 4/16/2015
//                ResultSet rs=null;   // CKA - commented out 4/16/2015
                javax.swing.JOptionPane.showMessageDialog(this, "ORACLE Error: "+ex.toString(), "Error accessing ORACLE", javax.swing.JOptionPane.ERROR_MESSAGE);
                crdSnsDb.clearCredentials();
                guiProgressLbl.setText("Error:"+ex.toString());
            }
        }catch (Exception ex){
            System.out.println("Error at Connection. "+ex.toString());
            javax.swing.JOptionPane.showMessageDialog(this, "ORACLE Error: "+ex.toString(), "Error accessing ORACLE", javax.swing.JOptionPane.ERROR_MESSAGE);
            crdSnsDb.clearCredentials();
            guiProgressLbl.setText("Error:"+ex.toString());
        }
//        Statement stmt1=null;   // CKA - commented out 4/16/2015
//        ResultSet rs=null;   // CKA - commented out 4/16/2015
        return pvs;
    }
    
    

    /*
     * Remote File Access
     */
    
    
    /**
     * Selects the list of all ICS control computers in the SNS database having
     * a name similar to that provides by the user in the <tt>Device Name</tt>
     * text field.
     *
     * @return  an array (list) of IP (Device ID, IP Address) pairs for each device
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    private List<IocRecord>    queryIocList(){
        List<IocRecord>    lstIocRecs = new LinkedList<IocRecord>();
        
        if (!this.checkDbCredentials())
            return lstIocRecs;

        String  strDvcFlt = txtDvcFilter.getText();
        String SQLTxt = "SELECT dvc_id, ip_addr FROM epics.ioc_dvc WHERE dvc_id LIKE '%"+
                         strDvcFlt +
                         "%' GROUP BY dvc_id, ip_addr ORDER BY dvc_id";
    
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            
        }catch (Exception ex){
            System.out.println("Error at New instance of Driver. "+ex.toString());

        }
        
        DbCredentials   crdSnsDb = this.getDbCred();
        try{
            //Connection con = DriverManager.getConnection("jdbc:oracle:thin:@snsdb1.sns.ornl.gov:1521:prod","sns_reports","sns");
//            Connection conSnsDb = DriverManager.getConnection("jdbc:oracle:thin:@"+crdSnsDb.getDbAddress(),crdSnsDb.getUserId(),crdSnsDb.getPassword());
            Connection conSnsDb = this.makeDbConnection();
          try{
                Statement dbStmt = conSnsDb.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet dbRset = dbStmt.executeQuery(SQLTxt);

                while(dbRset.next()){

                    String  strDvcId  = dbRset.getString("dvc_id");
                    String  strIpAddr = dbRset.getString("ip_addr");
                    
                    IocRecord recIoc = new IocRecord(strDvcId, strIpAddr);
                    
                    lstIocRecs.add(recIoc);
                }

                int cntRecs = dbRset.getRow();
                lblRcrdCnt.setText("Number of records: "+cntRecs);

                dbRset.close();
                dbRset=null;
                dbStmt.close();
                dbStmt=null;
                conSnsDb.close();
                conSnsDb = null;
                guiProgressLbl.setText("Operation completed.");
                
            } catch (Exception ex){
                System.out.println("Error at SQL. "+ex.toString());
                javax.swing.JOptionPane.showMessageDialog(this, "ORACLE Error: "+ex.toString(), "Error accessing ORACLE", javax.swing.JOptionPane.ERROR_MESSAGE);
                crdSnsDb.clearCredentials();
                guiProgressLbl.setText("Error:"+ex.toString());
                conSnsDb.close();
            }
            
        } catch (Exception ex){
            System.out.println("Error at Connection. "+ex.toString());
            javax.swing.JOptionPane.showMessageDialog(this, "ORACLE Error: "+ex.toString(), "Error accessing ORACLE", javax.swing.JOptionPane.ERROR_MESSAGE);
            crdSnsDb.clearCredentials();
            guiProgressLbl.setText("Error:"+ex.toString());
        }

        return lstIocRecs;
    }


    /**
     * Loads the application resources object from the (IOC) host name provided.
     * The PV names are read for either the EPICS dB file or the Excel XLS 
     * file, or both, on the IOC.
     *
     * @param host  host name of the IOC containing PV signal lists
     * 
     * @return      always returns <code>false</code> (?)
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    private boolean loadAppResources(String host){
        boolean myset=false;
        
        if(jCheckBoxMenuItem2.isSelected()){
            String filename = findUrlOfRemotePvFile(host,"daDbaseDef.txt");
            String idaDb = readRemotePvFile(filename);
            idaDb = idaDb.replaceAll("'","''");
            String [] idaDbcont = idaDb.split("\n");
            String mytext = "";
            String [] items=null;
            for(int x = 1;x<idaDbcont.length;x++){
                if(idaDbcont[x].startsWith("RECORD")){
                    items = idaDbcont[x].split("\t");
                    if(items[0].length()>6){
                        idaDbcont[x] = idaDbcont[x].replaceAll(" ","");
                        idaDbcont[x] = idaDbcont[x].replaceAll("RECORD", "RECORD"+"\t");
                        items = idaDbcont[x].split("\t");
                    }
                    mytext += items[1]+"\n";
                }
            }
            String [] ipvs=mytext.split("\n");
            this.rscAppl.setall(ipvs, idaDbcont, host, filename);
            
        } else {
            this.rscAppl.clearall();
            String xlsfilename = findUrlOfRemotePvFile(host,"xls");
            String dbfilename = findUrlOfRemotePvFile(host,"db");
            String idb = readRemotePvFile(dbfilename);
            idb = idb.replaceAll("'","''");
            String ixls = readRemotePvFile(xlsfilename);
            ixls = ixls.replaceAll("'","''");
            String [] ixlscont = ixls.split("\n");
            String [] idbfile = idb.split("\n");
            String [] ipvs=null;
            String mytext="";
            String [] items=null;
            for(int x = 1;x<ixlscont.length;x++){
                items = ixlscont[x].split("\t");
                if(items[0].length()>3){
                    mytext += items[0]+"\n";
                }
            }
            ipvs=mytext.split("\n");
            this.rscAppl.setall(ipvs, ixlscont, idbfile, ixls, idb, host);
        }
        return myset;
    }
    
    
    /**
     * Checks whether or not the given host IOC is reachable.
     *
     * @param strHostAddr  IOC host machine
     * 
     * @return  <code>true</code> if the host machine is alive and responding
     *          <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2011
     */
    private boolean checkIocAvailability(String strHostAddr){
//        boolean validate=false;   // CKA - commented out 4/16/2015
        
        if (!this.checkIocCredentials())
            return false;

        System.setProperty("jcifs.smb.client.responseTimeout", "2500");
        IocCredentials   crdIocHosts = this.getIocCred();
        SmbFile[]       smbFiles     = null;
        if(jCheckBoxMenuItem2.isSelected()){
            smbFiles = getDirectoryFiles("smb://ORNL;"+crdIocHosts.getUserId()+":"+crdIocHosts.getPassword()+"@"+strHostAddr+MainApplication.STR_IOC_FILEPATH_XLS);
        }else{
            smbFiles = getDirectoryFiles("smb://ORNL;"+crdIocHosts.getUserId()+":"+crdIocHosts.getPassword()+"@"+strHostAddr+MainApplication.STR_IOC_FILEPATH_EPICSDB);
        }

        // Check the number of files
        if (smbFiles == null)
            return false;
        
        if (smbFiles.length >0)
            return true;
        
        return false;
    }
    
    
    /**
     * Returns all the files in the given network directory.
     *
     * @param strDir     URI of the directory location
     * 
     * @return              array of files in the above directory
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2011
     */
    private SmbFile [] getDirectoryFiles(String strDir){
        SmbFile [] files= null;
        try{
            SmbFile dir = new SmbFile(strDir);
            files = dir.listFiles();
            //System.out.println(files.length);
        }catch (IOException ex){
            //crdIocHosts.clearall();
            JOptionPane.showConfirmDialog(
                    this,ex.toString(),
                    "Login Error?",
                    JOptionPane.ERROR_MESSAGE
                    );
            return files;
        }
        return files;
    }
    
    /**
     * Reads the file at the given location as a stream of
     * bytes using the SMB protocol.
     *   
     * @param strSmbFilePath    URL of remote file
     * 
     * @return                  string containing contains of given file
     *
     * @author Christopher K. Allen
     * @since  Aug 26, 2011
     */
    private String readRemotePvFile(String strSmbFilePath){
        StringBuilder contents = new StringBuilder();    
        
        try{
            SmbFileInputStream istrRemoteFile = new SmbFileInputStream(strSmbFilePath);
            
            byte[] arrBytes = new byte[8192];
            int    cntBytes = 0;
            while( (cntBytes = istrRemoteFile.read( arrBytes )) > 0 ) 
                contents.append(new String(arrBytes, 0, cntBytes));
            
            istrRemoteFile.close();   // CKA - added 4/16/2015
            
        } catch (IOException e) {

            System.out.println(e.toString());
        }
        
        return contents.toString();

    }
    
//    private String readPvRemoteFile(String strSmbFilePath){
//        String        strFtpFilePath = strSmbFilePath.replaceFirst("smb", "ftp");
//        strFtpFilePath = "file://diagnostics:meas8ure@160.91.232.117/c$/Program Files/SharedMemoryIOC/db/WS.db";
//        StringBuilder bufChars       = new StringBuilder();
//        
//        try{
////            URL                 urlTest = new URL("ftp", "160.91.232.117", "/c$/Program Files/SharedMemoryIOC/db/WS.db");
//            URI                 uriTest = new URI("ftp", null, "160.91.232.117", 22, "/c$/Program Files/SharedMemoryIOC/db/WS.db", (String)null, (String)null);
//            URL                 urlRemoteFile = uriTest.toURL();
////            InputStream         istrRemoteFile = urlTest.openStream();
//            
////            URL                 urlRemoteFile  = new URL(strFtpFilePath);
//            InputStream         istrRemoteFile = urlRemoteFile.openStream();
//
//            
//            byte[] arrBytes = new byte[8192];
//            int    cntBytesRead;
//            while( (cntBytesRead = istrRemoteFile.read( arrBytes )) > 0 ) 
//                bufChars.append(new String(arrBytes, 0, cntBytesRead));
//            
//        } catch (IOException e) {
//            
//            System.out.println(e.toString());
//            
//        } catch (URISyntaxException e) {
//
//            System.out.println(e.toString());
//        }
//        
//        return bufChars.toString();
//    }
    
    /**
     * Given the file extension we determine the location the PV name file on 
     * the given remote.  Specifically, for the extension <tt>.xls</tt> we find the
     * Excel file containing the PV signal names and for the extension <tt>.db</tt>
     * we find the EPICS database file containing the PV definitions.  Once found,
     * the location is returned as an URL formed with the IOC credentials and the 
     * SMB protocol.
     *
     * @param ipaRemoteHost     IP address of the remote host.
     * @param strFileExt        File extension of PV signal name file,
     *                          either <i>xls</i> or <i>db</i>.
     * 
     * @return
     *
     * @author Christopher K. Allen
     * @since  Aug 26, 2011
     */
    private String findUrlOfRemotePvFile(String ipaRemoteHost, String strFileExt){
        IocCredentials  crdIocHosts = this.getIocCred();
        String          strFileName = "";
        SmbFile[]       smbFiles    = null;
        
        if(jCheckBoxMenuItem2.isSelected()){
            smbFiles = getDirectoryFiles("smb://ORNL;"+crdIocHosts.getUserId()+":"+crdIocHosts.getPassword()+"@"+ipaRemoteHost+MainApplication.STR_IOC_FILEPATH_XLS);
            
        }else{
            smbFiles = getDirectoryFiles("smb://ORNL;"+crdIocHosts.getUserId()+":"+crdIocHosts.getPassword()+"@"+ipaRemoteHost+MainApplication.STR_IOC_FILEPATH_EPICSDB);
            
        }
        //SmbFile [] files = getJCIFfiles("smb://ORNL;"+crdIocHosts.getname()+":"+crdIocHosts.getpsswd()+"@"+host+"/c$/Program Files/SharedMemoryIOC/db/");
        SmbFile selectedfile = null;
        for(int y=0;y<smbFiles.length;y++){
            if(selectedfile==null){
                if(smbFiles[y].getPath().endsWith(strFileExt)){
                    selectedfile = smbFiles[y];
                }
            }else{
                if(smbFiles[y].getPath().endsWith(strFileExt)){
                    if(smbFiles[y].getLastModified()>selectedfile.getLastModified()){
                        selectedfile = smbFiles[y];
                    }
                }
            }
        }
        strFileName=selectedfile.getPath();
        return strFileName;
    }
}
