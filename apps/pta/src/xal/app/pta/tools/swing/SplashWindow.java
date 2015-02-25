/**
 * SplashWindow.java
 *
 * @author Christopher K. Allen
 * @since  Jan 13, 2011
 *
 */

/**
 * SplashWindow.java
 *
 * @author  Christopher K. Allen
 * @since	Jan 13, 2011
 */
package xal.app.pta.tools.swing;

import xal.app.pta.rscmgt.AppProperties;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

/**
 *  Provides a "splash" window displaying a given image
 *  icon for a given amount of time.  These objects are usually
 *  used at application startup to show a copyright or trademark.
 *  
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Jan 13, 2011
 */
public class SplashWindow extends JWindow implements Runnable {

    /*
     * Global Attributes
     */
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    /** The font used to display the splash screen title */
    private static final Font   FNT_TITLE = new Font("Sans-Serif", Font.BOLD, 16);
    
    
    
    
    /*
     * Local Attributes
     */
    
    /** Splash screen dimensions */
    final private Dimension     dimWnd;
    
    /** The image displayed */
    final private ImageIcon     icnImage;
    
    /** Title of the display panel */
    final private String        strTitle;
    
    
    
    /** Title of splash window */
    final private JLabel        lblTitle;
    
    /** Splash screen image to display */
    final private JLabel        lblImage;
    
    /** The text to be displayed */
    final private JLabel        lblText;
    
    /** The copyright to be displayed */
    final private JLabel        lblCpRt;
    
    /** The author(s) to be displayed */
    final private JLabel        lblAuth;
    

    
    /** Length of time (in seconds) to display splash screen */
    private int           cntTime;
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new <code>SplashWindow</code> which displays the given
     * image and text on a screen of the given size.
     * 
     * @param dimWnd    screen size 
     * @param icnImage  image to display
     * @param strTitle  title of the display
     *
     * @author  Christopher K. Allen
     * @since   Jan 13, 2011
     */
    public SplashWindow(Dimension dimWnd, ImageIcon icnImage, String strTitle) {
        this.dimWnd   = dimWnd;
        this.icnImage = icnImage;
        this.strTitle = strTitle;
        this.cntTime  = 0;
        
        this.lblTitle = new JLabel(this.strTitle, JLabel.CENTER);
        this.lblImage = new JLabel(this.icnImage);
        
        this.lblText = new JLabel();
        this.lblAuth = new JLabel();
        this.lblCpRt = new JLabel();

        this.lblTitle.setFont(FNT_TITLE);

        this.initWindow();
        this.sizeWindow();
    }
    
    /**
     * Displays additional text on the splash screen.
     *
     * @param strText   text to display on screen
     * @param fntText   font used for text
     *
     * @author Christopher K. Allen
     * @since  Jan 14, 2011
     */
    public void setText(String strText, Font fntText) {
        this.lblText.setText(strText);
        this.lblText.setHorizontalAlignment(JLabel.CENTER);
        this.lblText.setFont(fntText);
    }
    
    /**
     * Display the following author information on the splash
     * screen.
     *
     * @param strAuthors    author names
     * @param fntAuthors    font used to display authors
     *
     * @author Christopher K. Allen
     * @since  Jan 19, 2011
     */
    public void setAuthors(String strAuthors, Font fntAuthors) {
        this.lblAuth.setText(strAuthors);
        this.lblAuth.setHorizontalAlignment(JLabel.CENTER);
        this.lblAuth.setFont(fntAuthors);
    }
    
    /**
     * Display the given copyright information on the 
     * splash screen.
     *
     * @param strNotice   copyright notice
     * @param fntNotice   font used for the copyright notice
     *
     * @author Christopher K. Allen
     * @since  Jan 19, 2011
     */
    public void setCopyright(String strNotice, Font fntNotice) {
        this.lblCpRt.setText( strNotice );
        this.lblCpRt.setHorizontalAlignment(JLabel.RIGHT);
        this.lblCpRt.setFont(fntNotice);
    }

    /**
     * Sets the foreground and background colors of the 
     * splash screen.
     *
     * @param clrFrgnd  foreground color
     * @param clrBkgnd  background color
     * 
     * @throws ClassCastException   thrown if the content pane of the window is not
     *                              of type <code>JPanel</code>.  This is a serious,
     *                              technical error and should not occur.
     *
     * @author Christopher K. Allen
     * @since  Jan 14, 2011
     */
    public void setColors(Color clrFrgnd, Color clrBkgnd) throws ClassCastException {
        JPanel pnlContent = (JPanel) this.getContentPane();
        
        pnlContent.setBackground(clrBkgnd);
        pnlContent.setForeground(clrFrgnd);
    }

    
    /*
     * Operations
     */
    
    /**
     * Shows or hides this Window depending on the value of parameter 
     * <var>bolShow</var>.  This method is re-defined over that of the
     * base class simply for emphasis - you can turn this spash window
     * on and off manually.
     * 
     * @param   bolShow     window is visible if <code>true</code>,
     *                      invisible if <code>false</code>
     * @since Jan 26, 2011
     * @see java.awt.Window#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean bolShow) {
        super.setVisible(bolShow);
    }
    
    /**
     * Displays the splash screen for the given amount 
     * of time.
     *
     * @param cntTime   time to display <em>in seconds</em>
     *
     * @author Christopher K. Allen
     * @since  Jan 14, 2011
     */
    public void splash(int cntTime) {
        Thread  thrSplash = new Thread(this);
        
        this.cntTime = cntTime;
        thrSplash.start();
    }
    
    
    /*
     * Runnable Interface
     */
    
    /**
     * Runs the thread.  Displays the splash screen for the
     * defined number of seconds then dispose of thisself.
     * 
     * @since Jan 24, 2011
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        super.setVisible(true);
        
        try { Thread.sleep(cntTime*1000); } catch (InterruptedException e) {};
        
        super.setVisible(false);
    }
    
    
    
    
    /*
     * Support Methods
     */
    
    /**
     * Initializes the splash window.  Creates the GUI components
     * and adds them to the display.
     *
     * @throws ClassCastException   thrown if the content pane of the window is not
     *                              of type <code>JPanel</code>.  This is a serious,
     *                              technical error and should not occur.
     *
     * @author Christopher K. Allen
     * @since  Jan 14, 2011
     */
    private void initWindow() throws ClassCastException {
        
        // Container for GUI
        JPanel  pnlContent = (JPanel) this.getContentPane();
        
        pnlContent.setBorder( BorderFactory.createRaisedBevelBorder() );
        pnlContent.setLayout( new GridBagLayout() );
        
        GridBagConstraints  gbc = new GridBagConstraints();
        
        int cntPadX = AppProperties.TEXTFLD.PADX.getValue().asInteger();
        int cntPadY = AppProperties.TEXTFLD.PADY.getValue().asInteger();
        

        // Title and image 
        gbc.gridy = 0;  gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, cntPadX, 0, 0);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        pnlContent.add(this.lblTitle, gbc);
        
        // The splash window image
        gbc.gridy = 0;  gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill   = GridBagConstraints.BOTH;
        pnlContent.add(this.lblImage, gbc);
        
        // The description
        gbc.gridy = 1;  gbc.anchor = GridBagConstraints.CENTER; 
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.fill  = GridBagConstraints.HORIZONTAL;
        pnlContent.add(this.lblText, gbc);
        
        // Copyright and author notice
        gbc.gridy = 2;  gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;  
        gbc.gridwidth = 2;
        gbc.fill  = GridBagConstraints.NONE;
        gbc.insets = new Insets(cntPadY, 0, cntPadY, 0);
        pnlContent.add(this.lblAuth, gbc);
        
        gbc.gridy = 3;  gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx = 0;  
        gbc.gridwidth = 2;
        gbc.insets = new Insets(cntPadY, 0, 0, 0);
        gbc.fill  = GridBagConstraints.HORIZONTAL;
        pnlContent.add(this.lblCpRt, gbc);
        
//        gbc.gridy = 3;  gbc.anchor = GridBagConstraints.LINE_START;
//        gbc.gridx = 0;  gbc.gridwidth = 1;
//        gbc.insets = new Insets(0, 0, 0, 0);
//        gbc.fill  = GridBagConstraints.NONE;
//        pnlContent.add(new JLabel(""), gbc);
}
    
    /**
     * Sizes the splash screen according to the dimensions
     * specified in the constructor, then centers the 
     * window on the main console screen.
     *
     * @author Christopher K. Allen
     * @since  Jan 14, 2011
     */
    private void sizeWindow() {
        
        // Set the splash window in the middle of the console screen
        Dimension   dimCons = Toolkit.getDefaultToolkit().getScreenSize();
        
        int xPos = (dimCons.width  - this.dimWnd.width)/2;
        int yPos = (dimCons.height - this.dimWnd.height)/2;
        
        this.setBounds(xPos, yPos, this.dimWnd.width, this.dimWnd.height);
    }

    
}
