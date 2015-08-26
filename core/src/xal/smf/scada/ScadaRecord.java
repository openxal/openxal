/**
 * ScadaRecord.java
 *
 *  Created	: Dec 17, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.smf.scada;

import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.BadStructException;
import xal.ca.Channel;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.smf.AcceleratorNode;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * Base class for data records containing
 * data or parameters sets managed by 
 * related process variables on a hardware device. 
 * Typically the structure will consist of a 
 * set of fields on a one-to-one basis with 
 * a related set of PVs. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Dec 16, 2009
 * @author Christopher K. Allen
 */
public abstract class ScadaRecord implements DataListener, Cloneable {
    

    /*
     * Internal Classes
     */
    
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



    /** The set of PV field descriptors for this data set */
    private List<ScadaFieldDescriptor>        lstFldDscr; 

    /** The map of field name to PV field descriptor pairs */
    private Map<String, ScadaFieldDescriptor> mapNm2Fd;
    

    
    /*
     * Local Operations
     */
    
    /**
     * Returns the PV field descriptor for this structure with the given
     * name.  The name is typically the name of the structure field itself. 
     *
     * @param strName   field name
     * 
     * @return          PV field descriptor with given field name 
     *                  or <code>null</code> if not found
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2011
     */
    public ScadaFieldDescriptor     getFieldDescriptor(String strName) {
        return this.mapNm2Fd.get(strName);
    }
    
    /**
     * Returns the set of PV/data field descriptors for
     * this data structure.
     *
     * @return  set of descriptors describing the fields of this data structure
     * 
     * @since  Jan 13, 2010
     * @author Christopher K. Allen
     */
    public List<ScadaFieldDescriptor>     getFieldDescriptors() {
        return this.lstFldDscr;
    }
    
    /**
     * Returns a map of (field name, ScadaFieldDescriptor) pairs for this data 
     * structure.
     *
     * @return  map of descriptors that characterize the fields of this data structure
     *
     * @author Christopher K. Allen
     * @since  Jan 28, 2013
     */
    public Map<String, ScadaFieldDescriptor>    getFieldDescriptorMap() {
        return this.mapNm2Fd;
    }
    

    /**
     * Populate the fields of this data set with the current
     * Process Variable values of the given device.  We
     * assume that this data set has the appropriate fields
     * for the given device.
     *
     * @param smfDev    hardware device from which values are obtained
     * 
     * @throws BadStructException  data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to a descriptor read back channel 
     * @throws GetException         unable to get PV value from channel access or
     * 
     * @since  Dec 18, 2009
     * @author Christopher K. Allen
     */
    public void loadHardwareValues(AcceleratorNode smfDev) 
        throws BadStructException, ConnectionException, GetException 
    {
        for (ScadaFieldDescriptor pfdFld : this.lstFldDscr) {
            String      strFldNm = pfdFld.getFieldName();
            String      strHndPv = pfdFld.getRbHandle();
            
            this.setFieldFromPV(strFldNm, strHndPv, smfDev);
        }
    }
    
    /**
     * Sets the parameters of the given hardware device, to the values 
     * in this data structure.
     *
     * @param smfDev    hardware device to receive new parameter values
     * 
     * @throws BadStructException  data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to PV channel
     * @throws PutException  general put exception (unable to set all parameter values)
     * 
     * @since  Dec 17, 2009
     * @author Christopher K. Allen
     */
    public void     setHardwareValues(AcceleratorNode smfDev) 
        throws BadStructException, ConnectionException, PutException 
    {
        for (ScadaFieldDescriptor pfdFld : this.lstFldDscr) {
            if (  pfdFld.isControllable() ) {
                String          strFldNm = pfdFld.getFieldName();
                String          strHndPv = pfdFld.getSetHandle();

                this.setPvFromField(strFldNm, strHndPv, smfDev);
            }
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
        bufStr.append(this.getClass().getName() + " values\n"); //$NON-NLS-1$
        
        for (ScadaFieldDescriptor pktFld : this.lstFldDscr) {
            String          strFldNm   = pktFld.getFieldName();
            Class<?>        clsFldTyp  = pktFld.getPvType();
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

                bufStr.append(strFldNm + " = " + strFldVal + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
                
            } catch (SecurityException e) {
                bufStr.append(strFldNm + " = ERROR\n"); //$NON-NLS-1$

            } catch (NoSuchFieldException e) {
                bufStr.append(strFldNm + " = ERROR\n"); //$NON-NLS-1$
                
            } catch (IllegalArgumentException e) {
                bufStr.append(strFldNm + " = ERROR\n"); //$NON-NLS-1$
                
            } catch (IllegalAccessException e) {
                bufStr.append(strFldNm + " = ERROR\n"); //$NON-NLS-1$
                
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
     * @throws BadStructException  data structure fields are ill-defined/incompatible
     *  
     * @see xal.tools.data.DataListener#update(xal.tools.data.DataAdaptor)
     */
    @Override
    public void update(DataAdaptor daptSrc) throws MissingResourceException, BadStructException {
        
        // Find the appropriate data adaptor according to version
        String          strLabel = this.dataLabel();
        DataAdaptor     daptVals = daptSrc.childAdaptor(strLabel);

        if (daptVals == null) {
            strLabel = "gov.sns." + strLabel;
            daptVals = daptSrc.childAdaptor(strLabel);
        }
        if (daptVals == null) 
            daptVals = daptSrc;
        
        // Read in the data structure field values one by one
        //      the field names are the attributes in the data adaptor
        for (ScadaFieldDescriptor pktFld : this.lstFldDscr) {
            String          strFldName = pktFld.getFieldName();

            if ( !daptVals.hasAttribute(strFldName) ) {
                String strMsg = "Unable to find attribute " + strFldName +  //$NON-NLS-1$
                                " in Data Adaptor " + daptSrc.name() +  //$NON-NLS-1$
                                " for the XalPvDataStructure " + this.dataLabel(); //$NON-NLS-1$
                
                throw new MissingResourceException(strMsg, this.getClass().getName(), strFldName);
            }
            
            // Get the field object and its data type
            try {
                Class<? extends ScadaRecord>     clsThis    = this.getClass();
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
                throw new BadStructException("Data field " + strFldName + " is ill-defined.", e); //$NON-NLS-1$ //$NON-NLS-2$
                
            } catch (SecurityException e) {
                throw new BadStructException("Data field " + strFldName + " is ill-defined.", e); //$NON-NLS-1$ //$NON-NLS-2$

            } catch (NoSuchFieldException e) {
                throw new BadStructException("Data field " + strFldName + " is ill-defined.", e); //$NON-NLS-1$ //$NON-NLS-2$
                
            }
        }
    }

    /**
     * Save the data structure field values to the given data sink behind
     * the <code>DataAdaptor</code> interface.  Each value is stored
     * as the value of an attribute being the field name.
     *
     * @throws  TypeNotPresentException  Bad structure definition
     * 
     * @since 	Mar 3, 2010
     * @author  Christopher K. Allen
     *
     * @throws BadStructException  data structure fields are ill-defined/incompatible
     *  
     * @see xal.tools.data.DataListener#write(xal.tools.data.DataAdaptor)
     */
    @Override
    public void write(DataAdaptor daptSink) throws BadStructException {
        String          strLabel = this.dataLabel();
        DataAdaptor     daptVals = daptSink.createChild(strLabel);
        
        // Write out the fields of this data structure one by one
        //      we use the field name as the attribute and then store the value
        for (ScadaFieldDescriptor pktFld : this.lstFldDscr) {
            String          strFldName = pktFld.getFieldName();

            // Get the field object and its data type
            try {
                Class<? extends ScadaRecord> clsThis    = this.getClass();
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
                throw new BadStructException("Data field " + strFldName + " is ill-defined.", e); //$NON-NLS-1$ //$NON-NLS-2$
                
            } catch (SecurityException e) {
                throw new BadStructException("Data field " + strFldName + " is ill-defined.", e); //$NON-NLS-1$ //$NON-NLS-2$

            } catch (NoSuchFieldException e) {
                throw new BadStructException("Data field " + strFldName + " is ill-defined.", e); //$NON-NLS-1$ //$NON-NLS-2$
                
            }
        }
    }

    
    /*
     * Object Overrides
     */
    
    /**
     * Make a deep copy of this <code>ScadaStruct</code> object.  
     * The field descriptors do not need to be duplicated, however, for they
     * are immutable.
     * 
     * @since Apr 19, 2012
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        ScadaRecord     strCopy = (ScadaRecord)super.clone();

        strCopy.lstFldDscr = new LinkedList<ScadaFieldDescriptor>();
        strCopy.mapNm2Fd   = new HashMap<String, ScadaFieldDescriptor>();

        for (ScadaFieldDescriptor sfd : this.lstFldDscr) {
            String                  strName = sfd.getFieldName();
            
            strCopy.lstFldDscr.add(sfd);
            strCopy.mapNm2Fd.put(strName, sfd);
        }
        
        return strCopy;
    }

    
    /*
     * Derived Class Support
     */
    
    /**
     * Create a new <code>ScadaStruct</code> object.  The field
     * descriptors are taken from annotations in the source code.
     * The structure fields in the derived class should be annotated using the 
     * <code>AScada.Field</code> annotation to be recognized and
     * included in the list of field descriptors.
     * 
     * @throws  BadStructException   no SCADA fields (@Scada.Field) were found in data structure,
     *                               CKA: I have removed this for now
     *
     * @since     Jan 18, 2011
     * @author    Christopher K. Allen
     */
    protected ScadaRecord() throws BadStructException {
        this.lstFldDscr = new ScadaFieldList( this.getClass() );
        this.mapNm2Fd   = new ScadaFieldMap( this.getClass() );
        
//        if (this.lstFldDscr.size() == 0)
//            throw new BadStructException("No SCADA fields (@AScada.Field) found in data structure."); //$NON-NLS-1$
    }
    
    /**
     * Creates a new <code>ScadaStruct</code> object which is a deep copy of the argument.
     * 
     * @param clone     the <code>ScadaStruct</code> to be cloned
     * 
     * @throws CloneNotSupportedException   occurs when a defining <code>ScadaFieldDescriptor</code> cannot be copied
     *
     * @author  Christopher K. Allen
     * @since   Apr 19, 2012
     */
    protected ScadaRecord(ScadaRecord clone) throws CloneNotSupportedException {
        
        this.lstFldDscr = new LinkedList<ScadaFieldDescriptor>();
        this.mapNm2Fd   = new HashMap<String, ScadaFieldDescriptor>();

        for (ScadaFieldDescriptor sfd : clone.lstFldDscr) {
            ScadaFieldDescriptor    sfdCopy = (ScadaFieldDescriptor) sfd.clone();
            String                  strName = sfdCopy.getFieldName();
            
            this.lstFldDscr.add(sfdCopy);
            this.mapNm2Fd.put(strName, sfdCopy);
        }
    }
    
    /**
     * Creates a new instance of <code>ScadaRecord</code> initializing the connections
     * from the SCADA records in the annotated argument.
     *
     * @param clsRecord connects the SCADA connections as annotation metadata
     *
     * @author Christopher K. Allen
     * @since  Feb 5, 2013
     */
    protected ScadaRecord(Class<? extends AScada.Record> clsRecord) {
        this(ScadaFieldDescriptor.makeFieldDescriptors(clsRecord));
    }
    
    /**
     * Creates a new <code>ScadaStruct</code> object which is initialized from the
     * data stored behind the given data source. 
     * 
     * @param daptInitSrc		data source containing initialization information
     * 
     * @author  Christopher K. Allen
     * @since	Jan 29, 2013
     */
    protected ScadaRecord(DataAdaptor daptInitSrc) {
    	this();
    	this.update(daptInitSrc);
    }
    
    /**
     * Creates a new <code>ScadaStruct</code> object which is initialized from the
     * data provided by the given hardware object. 
     * 
     * @param smfDev	device to be queried for initialization information
     * 
     * @author  Christopher K. Allen
     * 
     * @throws BadStructException  data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to a descriptor read back channel 
     * @throws GetException         unable to get PV value from channel access or
     * 
     * @since	Jan 29, 2013
     */
    protected ScadaRecord(AcceleratorNode smfDev) throws ConnectionException, GetException, BadStructException {
    	this();
    	this.loadHardwareValues(smfDev);
    }
    
    /**
     * Create a new <code>ScadaStruct</code> object.  Called
     * by child classes to define their data fields and PVs
     * (with <code>ScadaFieldDescriptor</code>s) directly.  That is,
     * without the annotation option.
     *
     * @param lstFldDscr     list of PV field descriptors for this data set
     * 
     * @throws  BadStructException   no SCADA fields (@Scada.Field) were found in data structure
     *
     * @since     Dec 18, 2009
     * @author    Christopher K. Allen
     */
    protected ScadaRecord(List<ScadaFieldDescriptor> lstFldDscr) {
        this( lstFldDscr.toArray(new ScadaFieldDescriptor[0]) );
    }

    /**
     * Create a new <code>ScadaStruct</code> object.  Called
     * by child classes to define their data fields and PVs
     * (with <code>ScadaFieldDescriptor</code>s) directly.  That is,
     * without the annotation option.
     *
     * @param arrFldDscr     set of PV field descriptors for this data set
     * 
     * @throws  BadStructException   no SCADA fields (@Scada.Field) were found in data structure
     *
     * @since     Dec 18, 2009
     * @author    Christopher K. Allen
     */
    protected ScadaRecord(ScadaFieldDescriptor... arrFldDscr) {
        this.lstFldDscr = new LinkedList<ScadaFieldDescriptor>();
        this.mapNm2Fd   = new HashMap<String, ScadaFieldDescriptor>();
        
        if (arrFldDscr.length == 0)
            throw new BadStructException("No SCADA fields in argument."); //$NON-NLS-1$
        
        for (ScadaFieldDescriptor fd : arrFldDscr) {
            String      strName = fd.getFieldName();
            
            this.lstFldDscr.add(fd);
            this.mapNm2Fd.put(strName, fd);
        }
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
     * @throws BadStructException  data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to descriptor read back channel
     * @throws GetException         unable to get PV value
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     */
    protected void setFieldFromPV(String strFldName, String strHndPv, AcceleratorNode smfDev) 
        throws BadStructException, ConnectionException, GetException
    {
        
        try {
            // Get the data structure field object and its data type
            Class<? extends ScadaRecord> clsThis  = this.getClass();
            Field                       fldDataFld = clsThis.getField(strFldName);
            Class<?>                    clsFldType = fldDataFld.getType();

            
            // Get the channel for the data structure process variable 
            Channel       chanPv = smfDev.getAndConnectChannel(strHndPv);
            if ( !chanPv.isConnected() )
                throw new ConnectionException(chanPv, "Channel to data structure PV will not connect." +  //$NON-NLS-1$
                        "XAL channel handle = " + strHndPv); //$NON-NLS-1$
            
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
                String  strMsg  = "ScadaStruct#setFieldFromPv(): " + //$NON-NLS-1$
                                  "Unsupported data type " + strType +  //$NON-NLS-1$
                                  " for channel handle " + strHndPv; //$NON-NLS-1$
                System.err.println(strMsg);
                throw new BadStructException(strMsg);
            }

        } catch (SecurityException e) {
            String  strMsg = "ScadaStruct#getHardwareValues:" //$NON-NLS-1$
                + " unable to initialize field " + strFldName //$NON-NLS-1$
                + ", incompatible types for " + strHndPv; //$NON-NLS-1$
            throw new BadStructException(strMsg, e);
            
        } catch (NoSuchFieldException e) {
            String  strMsg = "ScadaStruct#getHardwareValues:" //$NON-NLS-1$
                + " unable to initialize field " + strFldName //$NON-NLS-1$
                + ", incompatible types for " + strHndPv; //$NON-NLS-1$
            throw new BadStructException(strMsg, e);
            
        } catch (IllegalArgumentException e) {
            String  strMsg = "ScadaStruct#getHardwareValues:" //$NON-NLS-1$
                + " unable to initialize field " + strFldName //$NON-NLS-1$
                + ", incompatible types for " + strHndPv; //$NON-NLS-1$
            throw new BadStructException(strMsg, e);
            
        } catch (IllegalAccessException e) {
            String  strMsg = "ScadaStruct#getHardwareValues:" //$NON-NLS-1$
                + " unable to initialize field " + strFldName //$NON-NLS-1$
                + ", incompatible types for " + strHndPv; //$NON-NLS-1$
            throw new BadStructException(strMsg, e);
            
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
     * type (not the data type specified in the {@link PvDescriptor} 
     * given in the arguments.
     * </p>
     *
     * @param strFldName    name of the data field within this data structure
     * @param strHndPv      handle of the (set) PV channel
     * @param smfDev        Hardware device containing the PV
     * 
     * @throws BadStructException  general field incompatibility exception 
     * @throws ConnectionException  unable to connect to descriptor set value channel
     * @throws PutException         unable to set PV value
     * 
     * @since  Dec 17, 2009
     * @author Christopher K. Allen
     */
    protected void setPvFromField(String strFldName, String  strHndPv, AcceleratorNode smfDev) 
        throws BadStructException, ConnectionException, PutException
    {

        try {
            // Get the PV channel handle then fetch channel 
            Channel         chanPv = smfDev.getAndConnectChannel(strHndPv);

            
            // Get the field object and its data type
            Class<? extends ScadaRecord>    clsThis    = this.getClass();
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
                String  strMsg  = "ScadaRecord#setPv: " + //$NON-NLS-1$
                "Unknown data type " + strType +  //$NON-NLS-1$
                " for channel handle " + strHndPv; //$NON-NLS-1$
                System.err.println(strMsg);
                throw new BadStructException(strMsg);
                
            }
            
            
        } catch (SecurityException e) {
            throw new BadStructException("ScadaRecord#getPv(): Security Exception: inaccessible field " + strFldName); //$NON-NLS-1$
            
        } catch (NoSuchFieldException e) {
            throw new BadStructException("ScadaRecord#getPv(): ERROR: No such field " + strFldName); //$NON-NLS-1$
            
        } catch (IllegalArgumentException e) {
            throw new BadStructException("ScadaRecord#getPv(): Illegal type conversion for field " + strFldName); //$NON-NLS-1$

        } catch (IllegalAccessException e) {
            throw new BadStructException("ScadaRecord#getPv(): Illegal access attempt for field " + strFldName); //$NON-NLS-1$

        }
    }

}


