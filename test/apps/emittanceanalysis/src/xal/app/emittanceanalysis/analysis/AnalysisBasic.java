package xal.app.emittanceanalysis.analysis;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 *  This is a superclass for all analysis class (except controller)
 *
 *@author     shishlo
 *@version    1.0
 */
abstract class AnalysisBasic {

    //message text field. It is actually message text field
    //from EmittanceDocument
    private JTextField messageTextLocal = new JTextField();

    //panels for custom GUI elements and plotting panels
    private JPanel leftTopPanel = new JPanel();
    private JPanel rightTopPanel = new JPanel();    
    private JPanel bottomPanel = new JPanel();

    /**  Parameters (various types) of the analyses across all instances keyed by parameter name */
    private HashMap<String,Object> crossParamMap = null;

    /**  The type index of the analysis */
    protected int analysisTypeIndex = -1;

    /**  The type index of the analysis */
    protected String analysisDescriptionString = "NONE";


    /**
     *  Constructor for the AnalysisBasic object
     *
     *@param  crossParamMap_In      The HashMap with Parameters of the analyses
     *@param  analysisTypeIndex_In  The type index of the analysis
     */
    AnalysisBasic( int analysisTypeIndex_In, HashMap<String,Object> crossParamMap_In ) {
        analysisTypeIndex = analysisTypeIndex_In;
        crossParamMap = crossParamMap_In;
    }


    /**  Performs actions before show the panel */
    abstract void goingShowUp();


    /**  Performs actions before close the panel */
    abstract void goingShowOff();


    /**  Sets all analyzes in the initial state with removing all temporary data */
    abstract void initialize();


    /**  Creates objects for the global HashMap using put method only */
    abstract void createHashMapObjects();


    /**  Connects to the objects in the global HashMap */
    abstract void connectToHashMapObjects();


    /**
     *  Sets all fonts.
     *
     *@param  fnt  The new font
     */
    abstract void setFontForAll( Font fnt );


    /**
     *  Returns the panel on the bottom of the analysis panel
     *
     *@return    The panel on the bottom of the analysis panel
     */
    JPanel getBottomPanel() {
        return bottomPanel;
    }


    /**
     *  Returns the panel on top right corner of the analysis panel
     *
     *@return    The panel on top right corner of the analysis panel
     */
    JPanel getRightTopPanel() {
        return rightTopPanel;
    }

    /**
     *  Returns the panel on top left corner of the analysis panel
     *
     *@return    The panel on top left corner of the analysis panel
     */
    JPanel getLeftTopPanel() {
        return leftTopPanel;
    }
    
    /**
     *  Returns the hash map table with global parameters of the analysis
     *
     *@return    The HashMap with global parameters keyed by parameter name
     */
    HashMap<String,Object> getParamsHashMap() {
        return crossParamMap;
    }


    /**
     *  Returns the short description of the analysis
     *
     *@return    The short description of the analysis
     */

    String getDescriptionText() {
        return analysisDescriptionString;
    }


    /**String
     *  Returns the type of analysis index
     *
     *@return    The type of analysis index
     */
    int getTypeIndex() {
        return analysisTypeIndex;
    }


    /**
     *  Returns the local text message field
     *
     *@return    The local text message field
     */
    JTextField getTextMessage() {
        return messageTextLocal;
    }


    /**
     *  Sets the message text field.
     *
     *@param  messageTextLocal  The new message text filed
     */
    void setMessageTextField( JTextField messageTextLocal ) {
        this.messageTextLocal = messageTextLocal;
    }

}
