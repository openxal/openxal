/**
 * ScadaPacket.java
 *
 *  Created	: Dec 17, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.smf.scada;

import xal.ca.Channel;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.ConnectionListener;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.tools.data.IDataAdaptor;
import xal.tools.data.DataListener;
import xal.smf.AcceleratorNode;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;

/**
 * Base class for data structures containing
 * data corresponding to sets of 
 * related process variables on a hardware device. 
 * Typically the structure will consist of a 
 * set of fields on a one-to-one basis with 
 * a related set of PVs. 
 *
 * @since  Dec 16, 2009
 * @author Christopher K. Allen
 */
public abstract class ScadaPacket implements DataListener {
    

    

    /**
     * Used by enumerations in data structures 
     * to indicate that they known aspects
     * of the data fields they represent.
     *
     * @since  Dec 16, 2009
     * @author Christopher K. Allen
     */
    public interface IFieldDescriptor  extends XalPvDescriptor.IPvDescriptor {
        
        /**
         * Returns the name of the field in the data structure.
         *
         * @return      data structure field name
         * 
         * @since  Dec 16, 2009
         * @author Christopher K. Allen
         */
        public String   getFieldName();
        
    }

    /**
     * Maintains the (field name, field PV) pairs for the
     * data sets formed of device PVs.  For example,
     * such data sets are supported by {@link ScadaPacket}.
     *
     * @since  Dec 18, 2009
     * @author Christopher K. Allen
     */
    public static class FieldDescriptor implements IFieldDescriptor {
        
        
        /*
         * Instance Attributes
         */
        
        /** Name of the data field */
        private final String                strFldNm;
        
        /** PV (descriptor) of the data field */
        private final XalPvDescriptor       pvdFld;
        
        
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>FieldDescriptor</code> object.
         *
         * @param strFldNm      the name of the data field
         * @param pvdFld        channel access PV descriptor of data field 
         *
         * @since     Dec 18, 2009
         * @author    Christopher K. Allen
         */
        public FieldDescriptor(String strFldNm, XalPvDescriptor pvdFld) {
            this.strFldNm = strFldNm;
            this.pvdFld   = pvdFld;
        }
        
        /**
         * Create a new <code>FieldDescriptor</code> object.
         *
         * @param strFldNm      the name of the data field
         * @param clsType       the class type of the data field
         * @param strHandleRb   the handle of the read back channel 
         *
         * @since     Jan 13, 2010
         * @author    Christopher K. Allen
         */
        public FieldDescriptor(String strFldNm, Class<?> clsType, String strHandleRb) {
            this.strFldNm = strFldNm;
            this.pvdFld   = new XalPvDescriptor(clsType, strHandleRb);
        }

        /**
         * Create a new <code>FieldDescriptor</code> object.
         *
         * @param strFldNm      the name of the data field
         * @param clsType       the class type of the data field
         * @param strHndRb      the handle of the read back channel 
         * @param strHndSet     the handle of the set PV value channel
         *
         * @since     Jan 13, 2010
         * @author    Christopher K. Allen
         */
        public FieldDescriptor(String strFldNm, Class<?> clsType, String strHndRb, String strHndSet) {
            this.strFldNm = strFldNm;
            this.pvdFld   = new XalPvDescriptor(clsType, strHndRb, strHndSet);
        }

        
        /*
         * Interface IFieldDescriptor 
         */
        
        /**
         * Return the data field's name.
         * 
         * @return  name of the data field
         *
         * @since   Dec 18, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.smf.scada.ScadaPacket.IFieldDescriptor#getFieldName()
         */
        @Override
        public String getFieldName() {
            return this.strFldNm;
        }

        /**
         * Return the PV descriptor for this data structure field.
         * 
         * @return  data field's PV descriptor
         *
         * @since   Dec 18, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.smf.scada.XalPvDescriptor.IPvDescriptor#getPvDescriptor()
         */
        @Override
        public XalPvDescriptor getPvDescriptor() {
            return this.pvdFld;
        }
    }
    
    /**
     * This class is used internally to check channel connections
     * for the PVs contained in this data structure.
     * 
     * @see ScadaPacket#testConnection(Class, AcceleratorNode, double)
     *
     * @author Christopher K. Allen
     * @since   Feb 4, 2011
     */
    public static class ConnectionMonitor implements ConnectionListener {

        /*
         * Local Attributes
         */
        
        /** the channel we are monitoring */
        final private Channel           chnFld;
        
        /** list of channels that have had a connection request issued */
        final private List<Channel>     lstReqs;
        
       
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>ConnectionMonitor</code> object for monitoring
         * the given channel while testing the given DAQ data structure.
         * 
//         * @param daq   set of data acquisition parameters available from an SMF device
         * @param chnFld   EPICS channel connecting to a field within the parameter set.
         * @param lstReqs   list of channels that have been requested to connect
         *
         * @author  Christopher K. Allen
         * @since   Feb 4, 2011
         */
        public ConnectionMonitor(final Channel chnFld, final List<Channel> lstReqs) {
            this.chnFld = chnFld;
            this.lstReqs = lstReqs;
        }
        
        
        /*
         * Connection Listener Interface
         */
        
        /**
         * <p>
         * The requested connection was made.  Consequently, 
         * <br/>
         * &nbsp; &sdot; We remove ourself from the channel's set of connection listeners
         * <br/>
         * &nbsp; &sdot; We remove the channel from the list of open requests
         * </p>
         * 
         * @since Feb 4, 2011
         * @see xal.ca.ConnectionListener#connectionMade(xal.ca.Channel)
         */
        @Override
        public void connectionMade(Channel channel) {
            this.chnFld.removeConnectionListener(this);
            this.lstReqs.remove(this.chnFld);
        }

        /**
         * Nothing really to do here - as a precaution we remove ourself from
         * the channel's set of connection listeners.  We should have been removed
         * when the connection request was fulfilled.   However, something wrong
         * could have happened.
         * 
         * @since Feb 4, 2011
         * @see xal.ca.ConnectionListener#connectionDropped(xal.ca.Channel)
         */
        @Override
        public void connectionDropped(Channel channel) {
            this.chnFld.removeConnectionListener(this);
        }
        
    }



    
    
    /*
     * Global Operations 
     */
    
    /**
     * <p>
     * This method returns data of the <code>{@link Scada.Field}</code> annotation
     * used to identify fields in data structures as Supervisory Control And Data
     * Acquisition (SCADA) fields.  The data is taken from each field annotation and
     * is used to populate a <code>FieldDescriptor</code> object.  The sum of all
     * annotation data for each field is returned as an array of field descriptors.
     * </p>
     * <p>
     * This whole mechanism of <code>FieldDescriptor</code> usage is (hopefully) going
     * to be eradicated.  It is too clumsy and the Java Annotation mechanism seems
     * more appropriate.
     * </p>
     *
     * @param clsScada  class type which is meant to be a SCADA data structure 
     *                  It must be annotated with <code>Scada</code>
     * 
     * @return  Array of field descriptors describing all the field in the data structure 
     *          used for SCADA.
     *
     * @author Christopher K. Allen
     * @since  Feb 16, 2011
     */
    public static FieldDescriptor[] getFieldDescriptors(Class<? extends ScadaPacket> clsScada) {

        List<FieldDescriptor>       lstFldDscr = new LinkedList<FieldDescriptor>();
        
        Field[]       arrFlds = clsScada.getFields();
        for (Field fld : arrFlds) {
            
            //Process only data structure fields which are marked as process variables
            if (! fld.isAnnotationPresent(Scada.Field.class) )
                continue;
            
            Scada.Field  annFld = fld.getAnnotation(Scada.Field.class);

            String      strFldNm  = fld.getName();
            Class<?>    clsType   = annFld.type(); 
            String      strHndRb  = annFld.hndRb();
            String      strHndSet = annFld.hndSet();
            
            FieldDescriptor fd = new FieldDescriptor(strFldNm, clsType, strHndRb, strHndSet);
            
            lstFldDscr.add(fd);
        }
        
        return lstFldDscr.toArray(null);
    }

    /**
     * Test the connections in all the channels in this DAQ data structure for the
     * given accelerator device.  The test will wait up to the given length
     * of time before declaring failure.
     *
     * @param smfDev        accelerator device whose channel connections are under test
     * @param dblTmOut      time out before test fails (in seconds)
     * 
     * @return              <code>true</code> if all connections were successful,
     *                      <code>false</code> if not all connection were made within given time
     *                      
     * @throws BadStructDefinition      the data structure is not defined properly (bad PV Descriptor)
//     * @throws InterruptedException     Unknown interruption occurred while waiting for connections
     *
     * @author Christopher K. Allen
     * @since  Feb 4, 2011
     */
    public static boolean  testConnection(Class<? extends ScadaPacket> clsScada, AcceleratorNode smfDev, double dblTmOut) 
        throws BadStructDefinition 
    {
        
        // Check for no test
        if (dblTmOut == 0)
            return true;
        
        // Check that data structure is tagged for channel access
        if (! clsScada.isAnnotationPresent(Scada.class) )
            throw new BadStructDefinition("The data structure is not annotated as 'Scada'");
        
        // Here is where we store all the requests
        List<Channel>    lstReqChns = new LinkedList<Channel>();
        
        // Request connection for all the channels in the DAQ data structure
        FieldDescriptor[]   arrFldDscrs = ScadaPacket.getFieldDescriptors(clsScada);
        
        for ( FieldDescriptor fldDscr : arrFldDscrs ) {
            XalPvDescriptor     pvdFld   = fldDscr.getPvDescriptor();
            String              strHndRb = pvdFld.getRbHandle();
            ScadaPacket.requestAndMonitorConnection(smfDev, strHndRb, lstReqChns);
            
            if (pvdFld.isControllable()) {
                String  strHndSet = pvdFld.getSetHandle();
                
                ScadaPacket.requestAndMonitorConnection(smfDev, strHndSet, lstReqChns);
            }
        }
        
        // Check if the connections have been fulfilled
        if (lstReqChns.size() == 0) 
            return true;

        // If not, wait the given time out
        try {
            Double      dblMsec  = new Double(dblTmOut*1000.0);
            int         intTmOut = dblMsec.intValue();
            
            Thread.sleep(intTmOut);

        } catch (InterruptedException e) {
            return false;
            
        }
        
        // Check again, if connections are still out there then it didn't work
        if (lstReqChns.size() != 0)
            return false;
     
        return true;
    }
    
    
    /**
     * <p>
     * Retrieves the given channel from the accelerator device (by the given
     * <em>channel handle</em>.  Then,
     * we create a <code>ConnectionMonitor</code> object for the given channel.
     * The channel is added to the list of requested channel connects then
     * a request is made for connection.
     * </p>
     * <p>
     * If the channel is already connected the method simply returns.
     * </p>
     *
     * @param smfDev        the device containing the channel
     * @param strHnd        the XAL channel handle for the channel under test
     * @param lstReqChns    the channel is added to this request connect list 
     *
     * @author Christopher K. Allen
     * @since  Feb 4, 2011
     */
    protected static void requestAndMonitorConnection(AcceleratorNode smfDev, String strHnd, List<Channel> lstReqChns) 
        throws BadStructDefinition
    {
        // Retrieve the channel object from the accelerator device
        Channel     chnReq = smfDev.findChannel(strHnd);
    
        if (chnReq == null) {
            String strMsg = "No channel " + strHnd 
            + " on device " + smfDev.getId();
            throw new BadStructDefinition(strMsg);
    
        }
    
        // If the channel is already connected there is nothing to do
        if (chnReq.isConnected())
            return;
    
        // We are going to request connection for this channel
        ConnectionMonitor      lsnConn = new ConnectionMonitor(chnReq, lstReqChns);
        lstReqChns.add(chnReq);
        chnReq.addConnectionListener(lsnConn);
        chnReq.requestConnection();
    }
    
    
    
    
    /*
     * Local Attributes
     */
    
    /** The set of PV field descriptors for this data set */
    private final FieldDescriptor[]     arrFldDscr; 
    
    

    
    /*
     * Local Operations
     */
    
    
    /**
     * Returns the set of PV/data field descriptors for
     * this data structure.
     *
     * @return  set of descriptors describing the fields of this data structure
     * 
     * @since  Jan 13, 2010
     * @author Christopher K. Allen
     */
    public FieldDescriptor[] getPacketFieldDescriptors() {
        return this.arrFldDscr;
    }
    

    /**
     * Populate the fields of this data set with the current
     * Process Variable values of the given device.  We
     * assume that this data set has the appropriate fields
     * for the given device.
     *
     * @param smfDev    hardware device from which values are obtained
     * 
     * @throws BadStructDefinition  data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to a descriptor read back channel 
     * @throws GetException         unable to get PV value from channel access or
     * 
     * @since  Dec 18, 2009
     * @author Christopher K. Allen
     */
    public void loadHardwareValues(AcceleratorNode smfDev) 
        throws BadStructDefinition, ConnectionException, GetException 
    {
        for (FieldDescriptor pfdFld : this.arrFldDscr) {
            String      strFldNm = pfdFld.getFieldName();
            String      strHndPv = pfdFld.getPvDescriptor().getRbHandle();
            
            this.setFieldFromPV(strFldNm, strHndPv, smfDev);
        }
    }
    
    /**
     * Sets the parameters of the given hardware device, to the values 
     * in this data structure.
     *
     * @param smfDev    hardware device to receive new parameter values
     * 
     * @throws BadStructDefinition  data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to PV channel
     * @throws PutException  general put exception (unable to set all parameter values)
     * 
     * @since  Dec 17, 2009
     * @author Christopher K. Allen
     */
    public void     setHardwareValues(AcceleratorNode smfDev) 
        throws BadStructDefinition, ConnectionException, PutException 
    {
        for (IFieldDescriptor pfdFld : this.arrFldDscr) {
            String          strFldNm = pfdFld.getFieldName();
            String          strHndPv = pfdFld.getPvDescriptor().getSetHandle();
            
            this.setPvFromField(strFldNm, strHndPv, smfDev);
        }
    }

    
    /*
     * Object Overrides
     */
    
    /**
     * Write out a text description of the data structure field
     * values.
     * 
     * @return  string representation of the data structure values
     *
     * @since   Feb 5, 2010
     * @author  Christopher K. Allen
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer    bufStr = new StringBuffer();
        bufStr.append(this.getClass().getName() + " values\n");
        
        for (FieldDescriptor pktFld : this.arrFldDscr) {
            String          strFldNm   = pktFld.getFieldName();
            Class<?>        clsFldTyp  = pktFld.getPvDescriptor().getPvType();
            String          strFldVal;
            
            try {
                Field   fldDataSet = this.getClass().getField(strFldNm);
                
                if (clsFldTyp == double.class || clsFldTyp == Double.class) {
                    double  dblVal = fldDataSet.getDouble(this);
                    strFldVal = String.valueOf(dblVal);
                    
                } else if (clsFldTyp == int.class || clsFldTyp == Integer.class) {
                    int     intVal = fldDataSet.getInt(this);
                    strFldVal = String.valueOf(intVal);
                    
                } else {
                    strFldVal = fldDataSet.toString();
                    
                }

                bufStr.append(strFldNm + " = " + strFldVal + "\n");
                
            } catch (SecurityException e) {
                bufStr.append(strFldNm + " = ERROR\n");

            } catch (NoSuchFieldException e) {
                bufStr.append(strFldNm + " = ERROR\n");
                
            } catch (IllegalArgumentException e) {
                bufStr.append(strFldNm + " = ERROR\n");
                
            } catch (IllegalAccessException e) {
                bufStr.append(strFldNm + " = ERROR\n");
                
            }
        }

        return bufStr.toString();
    }


    
    
    /*
     * DataListener Interface
     */
    
    /**
     * Read in the values of the data structure fields from the data
     * source behind the data adaptor interface.  The attributes of the
     * data source are the field names, the values are taken from there.
     *
     * @since 	Mar 3, 2010
     * @author  Christopher K. Allen
     *
     * @throws  MissingResourceException        a data field was missing from the data source
     * @throws BadStructDefinition  data structure fields are ill-defined/incompatible
     *  
     * @see xal.tools.data.DataListener#update(xal.tools.data.IDataAdaptor)
     */
    @Override
    public void update(IDataAdaptor daptSrc) throws MissingResourceException, BadStructDefinition {
        String          strLabel = this.dataLabel();
        IDataAdaptor     daptVals = daptSrc.childAdaptor(strLabel);

        // Read in the data structure field values one by one
        //      the field names are the attributes in the data adaptor
        for (FieldDescriptor pktFld : this.arrFldDscr) {
            String          strFldName = pktFld.getFieldName();

            if ( !daptVals.hasAttribute(strFldName) ) {
                String strMsg = "Unable to find attribute " + strFldName + 
                                " in Data Adaptor " + daptSrc.name() + 
                                " for the XalPvDataStructure " + this.dataLabel();
                
                throw new MissingResourceException(strMsg, this.getClass().getName(), strFldName);
            }
            
            // Get the field object and its data type
            try {
                Class<? extends ScadaPacket> clsThis    = this.getClass();
                Field                            fldDataFld = clsThis.getField(strFldName);
                Class<?>                         typDataFld = fldDataFld.getType();

                if (typDataFld == double[].class) {
                    
                    double[] arrVals = daptVals.doubleArray(strFldName);

                    fldDataFld.set(this, arrVals);
                    
                } else if (typDataFld == double.class){
                    double dblVal = daptVals.doubleValue(strFldName);
                    
                    fldDataFld.setDouble(this, dblVal);
                    
                } else if (typDataFld == long.class) {
                    long lngVal = daptVals.longValue(strFldName);
                    
                    fldDataFld.setLong(this, lngVal);
                    
                } else if (typDataFld == int.class) {
                    int intVal = daptVals.intValue(strFldName);
                    
                    fldDataFld.setInt(this, intVal);
                    
                } else if (typDataFld == boolean.class) {
                    boolean bolVal = daptVals.booleanValue(strFldName);
                    
                    fldDataFld.setBoolean(this, bolVal);
                    
                } else if (typDataFld == String.class) {
                    String strVal = daptVals.stringValue(strFldName);
                    
                    fldDataFld.set(this, strVal);
                }

            } catch (IllegalAccessException e) {
                throw new BadStructDefinition("Data field " + strFldName + " is ill-defined.", e);
                
            } catch (SecurityException e) {
                throw new BadStructDefinition("Data field " + strFldName + " is ill-defined.", e);

            } catch (NoSuchFieldException e) {
                throw new BadStructDefinition("Data field " + strFldName + " is ill-defined.", e);
                
            }
        }
    }

    /**
     * Save the data structure field values to the given data sink behind
     * the <code>IDataAdaptor</code> interface.  Each value is stored
     * as the value of an attribute being the field name.
     *
     * @throws  TypeNotPresentException  Bad structure definition
     * 
     * @since 	Mar 3, 2010
     * @author  Christopher K. Allen
     *
     * @throws BadStructDefinition  data structure fields are ill-defined/incompatible
     *  
     * @see xal.tools.data.DataListener#write(xal.tools.data.IDataAdaptor)
     */
    @Override
    public void write(IDataAdaptor daptSink) throws BadStructDefinition {
        String          strLabel = this.dataLabel();
        IDataAdaptor     daptVals = daptSink.createChild(strLabel);
        
        // Write out the fields of this data structure one by one
        //      we use the field name as the attribute and then store the value
        for (FieldDescriptor pktFld : this.arrFldDscr) {
            String          strFldName = pktFld.getFieldName();

            // Get the field object and its data type
            try {
                Class<? extends ScadaPacket> clsThis    = this.getClass();
                Field                            fldDataFld = clsThis.getField(strFldName);
                Class<?>                         typDataFld = fldDataFld.getType();
                Object                           objFldVal  = fldDataFld.get(this);

                if (typDataFld == double[].class) {
                    double[]    arrVals = (double[])objFldVal;
                    
                    daptVals.setValue(strFldName, arrVals);
                    
                } else {
                    daptVals.setValue(strFldName, objFldVal.toString());

                }

            } catch (IllegalAccessException e) {
                throw new BadStructDefinition("Data field " + strFldName + " is ill-defined.", e);
                
            } catch (SecurityException e) {
                throw new BadStructDefinition("Data field " + strFldName + " is ill-defined.", e);

            } catch (NoSuchFieldException e) {
                throw new BadStructDefinition("Data field " + strFldName + " is ill-defined.", e);
                
            }
        }
    }

    
    
    
    /*
     * Derived Class Support
     */
    
    /**
     * Create a new <code>ScadaPacket</code> object.  The field
     * descriptors are taken from annotations in the source code.
     * The structure fields in the derived class should be annotated using the 
     * <code>Scada.Field</code> annotation to be recognized and
     * included in the list of field descriptors.
     *
     * @since     Jan 18, 2011
     * @author    Christopher K. Allen
     */
    protected ScadaPacket() {
        this.arrFldDscr = getFieldDescriptors(this.getClass());
    }
    
    /**
     * Create a new <code>ScadaPacket</code> object.  Called
     * by child classes to define their data fields and PVs
     * (with <code>FieldDescriptor</code>s) directly.  That is,
     * without the annotation option.
     *
     * @param arrFldDscr     set of PV field descriptors for this data set
     *
     * @since     Dec 18, 2009
     * @author    Christopher K. Allen
     */
    protected ScadaPacket(FieldDescriptor[] arrFldDscr) {
        this.arrFldDscr = arrFldDscr;
    }
    
    
    
    /*
     * Local Support
     */
    
    /**
     * Set the value of of the given data field using the
     * value obtained from the PV attached to the given device.
     * The type of the field is determined using Java reflection.
     * Supported typed are all the native types plus their
     * array forms.  The value of the PV is read in as the
     * same type of the data field.  
     * This cowboy approach should be okay since the
     * method is meant to be used only by child classes. 
     *
     * @param strFldName    the name of the data field in this structure
     * @param strHndPv      the (get) channel handle for the PV 
     * @param smfDev        the PV belongs to this device
     * 
     * @throws BadStructDefinition  data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to descriptor read back channel
     * @throws GetException         unable to get PV value
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     */
    protected void setFieldFromPV(String strFldName, String strHndPv, AcceleratorNode smfDev) 
        throws BadStructDefinition, ConnectionException, GetException
    {
        
        try {
            // Get the data structure field object and its data type
            Class<? extends ScadaPacket> clsThis  = this.getClass();
            Field                       fldDataFld = clsThis.getField(strFldName);
            Class<?>                    clsFldType = fldDataFld.getType();

            
            // Get the channel for the data structure process variable 
            Channel       chanPv = smfDev.getAndConnectChannel(strHndPv);
            if ( !chanPv.isConnected() )
                throw new ConnectionException(chanPv, "Channel to data structure PV will not connect." + 
                        "XAL channel handle = " + strHndPv);
            
            ChannelRecord recPv  = chanPv.getRawValueRecord();
            
            // Get the PV value and set the field with it
            if (clsFldType == byte.class || clsFldType == Byte.class) {
                Byte    bytVal = recPv.byteValue();
                fldDataFld.set(this, bytVal);
                
            } else if (clsFldType == byte[].class) {
//                byte[]      arrBytVal = chanPv.getArrByte();
//                fldDataStr.set(this, arrBytVal);
                byte[]      arrBytVal = recPv.byteArray();
                fldDataFld.set(this, arrBytVal);

            } else if (clsFldType == int.class || clsFldType == Integer.class) {
                Integer intVal = recPv.intValue();
                fldDataFld.setInt(this, intVal);

            } else if (clsFldType == int[].class ) {
                int[]       arrIntVal = recPv.intArray();
                fldDataFld.set(this, arrIntVal);
            
            } else if (clsFldType == short.class || clsFldType == Short.class) {
                Short   sht = recPv.shortValue();
                fldDataFld.setInt(this, sht);

            } else if (clsFldType == short[].class) {
                short[]       arrLngVal = recPv.shortArray();
                fldDataFld.set(this, arrLngVal);
            
            } else if (clsFldType == float.class || clsFldType == Float.class) {
                Float   fltVal = recPv.floatValue();
                fldDataFld.setFloat(this, fltVal);
                
            } else if (clsFldType == float[].class) {
                float[]     arFltVal = recPv.floatArray();
                fldDataFld.set(this, arFltVal);
                
            } else if (clsFldType == double.class || clsFldType == Double.class) { 
                Double  dblVal = recPv.doubleValue();
                fldDataFld.setDouble(this, dblVal);
                
            } else if (clsFldType == double[].class) {
                double[]    arrDblVal = chanPv.getArrDbl();
                fldDataFld.set(this, arrDblVal);
                
            } else if (clsFldType == String.class) {
                String  strVal = chanPv.getValString();
                fldDataFld.set(this, strVal);
                
            } else {
                String  strType = clsFldType.getName();
                String  strMsg  = "ScadaPacket#setFieldFromPv(): " +
                                  "Unknown data type " + strType + 
                                  " for channel handle " + strHndPv;
                System.err.println(strMsg);
                throw new BadStructDefinition(strMsg);
            }

        } catch (SecurityException e) {
            String  strMsg = "ScadaPacket#getHardwareValues:"
                + " unable to initialize field " + strFldName
                + ", incompatible types for " + strHndPv;
            throw new BadStructDefinition(strMsg, e);
            
        } catch (NoSuchFieldException e) {
            String  strMsg = "ScadaPacket#getHardwareValues:"
                + " unable to initialize field " + strFldName
                + ", incompatible types for " + strHndPv;
            throw new BadStructDefinition(strMsg, e);
            
        } catch (IllegalArgumentException e) {
            String  strMsg = "ScadaPacket#getHardwareValues:"
                + " unable to initialize field " + strFldName
                + ", incompatible types for " + strHndPv;
            throw new BadStructDefinition(strMsg, e);
            
        } catch (IllegalAccessException e) {
            String  strMsg = "ScadaPacket#getHardwareValues:"
                + " unable to initialize field " + strFldName
                + ", incompatible types for " + strHndPv;
            throw new BadStructDefinition(strMsg, e);
            
        }
    }

    
    /**
     * <p>
     * Sets the given PV with the value of the data structure field
     * given by name.  Of course all this is for the SMF hardware device
     * given in the argument.  Thus, the given device must contain the given
     * PV, and this data structure must contain the named field.
     * </p>
     * <p>
     * We check the type of the named data field.  The PV is set using this
     * type (not the data type specified in the {@link XalPvDescriptor} 
     * given in the arguments.
     * </p>
     *
     * @param strFldName    name of the data field within this data structure
     * @param strHndPv      handle of the (set) PV channel
     * @param smfDev        Hardware device containing the PV
     * 
     * @throws BadStructDefinition  general field incompatibility exception 
     * @throws ConnectionException  unable to connect to descriptor set value channel
     * @throws PutException         unable to set PV value
     * 
     * @since  Dec 17, 2009
     * @author Christopher K. Allen
     */
    protected void setPvFromField(String strFldName, String  strHndPv, AcceleratorNode smfDev) 
        throws BadStructDefinition, ConnectionException, PutException
    {

        try {
            // Get the PV channel handle then fetch channel 
            Channel         chanPv = smfDev.getAndConnectChannel(strHndPv);

            
            // Get the field object and its data type
            Class<? extends ScadaPacket>     clsThis    = this.getClass();
            Field                           fldDataFld = clsThis.getField(strFldName);
            Class<?>                        clsFldType = fldDataFld.getType();

            // Use the value of the field to set the PV (using the correct type)
            if (clsFldType == byte.class) {
                byte        bytVal = fldDataFld.getByte(this);
                chanPv.putVal(bytVal);

            } else if (clsFldType == byte[].class) {
                byte[]      arrBytVal = (byte[])fldDataFld.get(this);
                chanPv.putVal(arrBytVal);

            } else if (clsFldType == int.class) {
                int         intVal = fldDataFld.getInt(this);
                chanPv.putVal(intVal);

            } else if (clsFldType == int[].class) {
                int[]       arrIntVal = (int[])fldDataFld.get(this);
                chanPv.putVal(arrIntVal);

            } else if (clsFldType == float.class ) {
                float       fltVal = fldDataFld.getFloat(this);
                chanPv.putVal(fltVal);

            } else if (clsFldType == float[].class) {
                float[]       arrFltVal = (float[])fldDataFld.get(this);
                chanPv.putVal(arrFltVal);

            } else if (clsFldType == double.class ) { 
                double      dblVal = fldDataFld.getDouble(this);
                chanPv.putVal(dblVal);

            } else if (clsFldType == double[].class) {
                double[]    arrDblVal = (double[])fldDataFld.get(this);
                chanPv.putVal(arrDblVal);

            } else if (clsFldType == String.class) {
                String      strVal = (String)fldDataFld.get(this);
                chanPv.putVal(strVal);
                
            } else {
                String  strType = clsFldType.getName();
                String  strMsg  = "DataSet#setPv: " +
                "Unknown data type " + strType + 
                " for channel handle " + strHndPv;
                System.err.println(strMsg);
                throw new BadStructDefinition(strMsg);
                
            }
            
            
        } catch (SecurityException e) {
            throw new BadStructDefinition("ScadaPacket#getPv(): Security Exception: inaccessible field " + strFldName);
            
        } catch (NoSuchFieldException e) {
            throw new BadStructDefinition("ScadaPacket#getPv(): ERROR: No such field " + strFldName);
            
        } catch (IllegalArgumentException e) {
            throw new BadStructDefinition("ScadaPacket#getPv(): Illegal type conversion for field " + strFldName);

        } catch (IllegalAccessException e) {
            throw new BadStructDefinition("ScadaPacket#getPv(): Illegal access attempt for field " + strFldName);

        }
    }

}


