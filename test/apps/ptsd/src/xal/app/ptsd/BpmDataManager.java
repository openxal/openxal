/**
 * BpmDataProxy.java
 *
 *  Created	: Jul 25, 2008
 *  Author      : Christopher K. Allen 
 */
package xal.app.ptsd;

import xal.ca.Channel;
import xal.ca.correlator.ChannelCorrelator;
import xal.tools.correlator.Correlation;
import xal.tools.correlator.CorrelationNotice;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.BPM;
import xal.ca.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * <p>
 * <b>BpmDataProxy</b>: This class is a data object associated
 * with the document component of the <code>PTSD</code>
 * high-level control application.
 * </p>
 * <p>
 * The data are arranged by indices based upon the order of the
 * BPMs in the initializing <code>AcceleratorSeq</code> object.
 * </p>
 *
 * @since  Jul 25, 2008
 * @author Christopher K. Allen
 */
public class BpmDataManager {

    
    
    
    
   
    
    /*
     * Internal Class --------------------------------------------
     */

    /**
     *
     *
     * @since  Jul 28, 2008
     * @author Christopher K. Allen
     */
    public class BpmDataEvent implements CorrelationNotice<ChannelTimeRecord> {

        /**
         * Create a new <code>BpmDataEvent</code> object.
         *
         *
         * @since     Jul 28, 2008
         * @author    Christopher K. Allen
         */
        public BpmDataEvent() {
            // TODO Auto-generated constructor stub
        }

        
        /*
         * CorrelationNotice Interface
         */
        
        
        /**
         *
         * @since 	Jul 28, 2008
         * @author  Christopher K. Allen
         *
         * @see gov.sns.tools.correlator.CorrelationNotice#newCorrelation(java.lang.Object, gov.sns.tools.correlator.Correlation)
         */
        public void newCorrelation(Object sender, Correlation<ChannelTimeRecord> correlation) {

        }

        /**
         *
         * @since 	Jul 28, 2008
         * @author  Christopher K. Allen
         *
         * @see gov.sns.tools.correlator.CorrelationNotice#noCorrelationCaught(java.lang.Object)
         */
        public void noCorrelationCaught(Object sender) {
            // TODO Auto-generated method stub

        }

    }



    /*
     * Internal Class --------------------------------------------
     */

   /**
    * <p>
    * Simple record class for storing BPM properties useful
    * to the parent class.
    * </p>
    *
    * @since  Jul 25, 2008
    * @author Christopher K. Allen
    */
   private static class BpmRecord {
       
       /*
        * Global Constants
        */
       
       /**  Channel handle for BPM turn-by-turn amplitude (horizontal plane) */
       public static final String STR_BPM_AMP_TBT_X = BPM.X_TBT_HANDLE;

       /**  Channel handle for BPM turn-by-turn amplitude (horizontal plane) */
       public static final String STR_BPM_AMP_TBT_Y = BPM.Y_TBT_HANDLE;
       
       
       
       /*
        * Record Attributes
        */
       
       
       /** BPM string identifier */
       private final String   strId;
       
       /** User selected BPM for analysis */
       private final boolean  bolSelected;
       
       /** BPM has an active signal */
       private final boolean  bolActive; 
       
       /** Horizontal plane data channel for BPM */
       private final Channel  xChannel;
       
       /** Vertical plane data channel for BPM */
       private final Channel  yChannel;
       
       
       /*
        * Initialization
        */
       
       private BpmRecord(BPM bpm)       {
           this.strId = bpm.getId();
           this.bolSelected = true;
           this.bolActive = true;
           this.xChannel = bpm.getChannel(STR_BPM_AMP_TBT_X);
           this.yChannel = bpm.getChannel(STR_BPM_AMP_TBT_Y);
       }

   }

   
   
   /*
    * Class BpmDataProxy ------------------------------------------------
    */
   

   
   /*
    * Global Constants
    */
   
   /**  BPM string identifier for hardware types */
   public static final String STR_BPM_TYPE_ID = BPM.s_strType;


   /**  Correlated data maximum time interval (in <b>seconds</b> ) */
   private static final int CORRELATOR_TIME_INTERVAL = 0;


    
    
   
    /*
     * Local Attributes
     */


    /** The target accelerator sector under analysis */
    private final AcceleratorSeq  seqTarget;
    
    /** The current data correlator used by the application */
    private ChannelCorrelator     corData;
    
    /** list of BPM records */
    private ArrayList<BpmRecord>  arrRecords;
    
    /** hash table of BPM records indexed by string id */
    private Hashtable<String,BpmRecord>  tblRecords;

    
    
    
    /*
     * Instance Initialization
     */
    
    
    /**
     * Create a new <code>BpmDataProxy</code> object.
     *
     * @param seqTarget Accelerator sector under analysis
     *
     * @since     Jul 25, 2008
     * @author    Christopher K. Allen
     */
    public BpmDataManager(AcceleratorSeq seqTarget)  {
        this.seqTarget = seqTarget;
        
        this.initRecords(seqTarget);
        this.initCorrelator();
    }
    
    
    

    /*
     * Attribute Query
     */
    



    /**
     * Return the current channel data correlation used by 
     * this data set. 
     *
     * @return The data correlator object
     *
     * @since  Jul 28, 2008
     * @author Christopher K. Allen
     */
    public ChannelCorrelator getCorrelator() {
        return this.corData;
    }
    
    /**
     * <p>
     * <b>getSequence</b> - Returns the current accelerator
     * sector for which this data is configured.
     * </p>
     *
     * @return  The associated accelerator sequence object
     * 
     * @since  Jul 28, 2008
     * @author Christopher K. Allen
     */
    public AcceleratorSeq    getSequence()      {
        return this.seqTarget;
    }


    
    
    /**
     * 
     * <p>
     * Returnes the number of BPM records
     * currently managed in this data source.
     * </p>
     * 
     * @return  The number of BPM records under management 
     *
     * @since  Jul 29, 2008
     * @author Christopher K. Allen
     *
     * @see java.util.ArrayList#size()
     */
    public int getBpmCount() {
        return this.arrRecords.size();
    }

    
    /**
     * <p>
     * <b>isSelected</b> - Return the flag indicating
     * whether or not the BPM at the given index
     * has been selected for active analysis.
     * <p>
     *
     * @param index     BPM record index
     * 
     * @return  The analysis flag of the associated BPM 
     * 
     * @since  Jul 29, 2008
     * @author Christopher K. Allen
     */
    public boolean      isSelected(int index)   {
        boolean bolSelected = this.arrRecords.get(index).bolSelected;
        return bolSelected;
       
    }
    
    /**
     * <p>
     * <b>getBpmId</b> - Return the string identifier
     * for the BPM at the given index.
     * </p>
     *
     * @param indBpm    BPM index
     * 
     * @return          BPM string identifier
     * 
     * @since  Jul 29, 2008
     * @author Christopher K. Allen
     */
    public String       getBpmId(int indBpm)        {
        return  this.arrRecords.get(indBpm).strId;
    }
    
    
    
    /**
     * <p>
     * <b>getSignalHor</b> - Return the horizontal signal
     * for the BPM at the given index.
     * </p>
     *
     * @param indBpm    index of the BPM within the accelerator sequence
     * 
     * @return          horizontal signal vector for the given BPM
     * 
     * @since  Aug 6, 2008
     * @author Christopher K. Allen
     */
    public double[]     getSignalHor(int indBpm) {
        Channel chanHor = this.arrRecords.get(indBpm).xChannel;

        return new double[1];
    }
    
    
    
   
    /*
     * Internal Support
     */
    
    /**
     * <binitRecords</b> - create and (re)intialize 
     * the records for all the 
     * BPMs in the give accelerator sector.  If the current
     * <code>BpmDataProxy</code> object already has a working
     * set of record, they are discarded and new ones are
     * created. 
     *
     * @param seqTarget The acclerator sector under analysis
     * 
     * @since  Jul 28, 2008
     * @author Christopher K. Allen
     */
    private void initRecords(AcceleratorSeq seqTarget)       {
        this.arrRecords = new ArrayList<BpmRecord>();
        this.tblRecords = new Hashtable<String,BpmRecord>();
        
        List<AcceleratorNode>   lstNodes = seqTarget.getNodesOfType(STR_BPM_TYPE_ID);
        
        for (AcceleratorNode node : lstNodes)
            try {
                BPM       bpm = (BPM)node;
                BpmRecord rec = new BpmRecord(bpm);
                
                this.arrRecords.add(rec);
                this.tblRecords.put(bpm.getId(), rec);
                
            } catch (ClassCastException e)     {
                String  strMsg = "AcceleratorNode: " + node.getId() + " attempt to cast as BPM.";
                
                PtsdMain.getAppLogger().warning(strMsg);
                
            }
    }
    
    
    
    /**
     * <p>
     * <b>initCorrelator</b> - Initialize the channel correlator
     * with the horizontal and vertical channels maintaining the 
     * turn-by-turn signals.
     * </p>
     *
     * 
     * @since  Jul 28, 2008
     * @author Christopher K. Allen
     */
    private void initCorrelator() {
        this.corData = new ChannelCorrelator(CORRELATOR_TIME_INTERVAL);
        
        for (BpmRecord rec : this.arrRecords)   {
            this.corData.addChannel(rec.xChannel);
            this.corData.addChannel(rec.yChannel);
        }
        
    }



}
