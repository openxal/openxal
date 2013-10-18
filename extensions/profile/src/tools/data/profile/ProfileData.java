/**
 * WireScannerData.java
 * 
 * Author   : Christopher K. Allen
 * Created  : August, 2007
 */
package xal.tools.data.profile;


import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * <p>
 * Encapsulation of generic beam profile measurement data.  This data set consists of 
 * sampled data representing projections of the beam distribution from multiple
 * viewing angles.  The number of angles depends upon the number of "wire" orientations
 * available on the diagnostic device.  Currently the viewing angles are enumerated with
 * the Java enumeration <code>Angle</code>.
 * </p>
 * <p>
 * Also contained in this data set are the axes positions for each projection sample.  
 * Specifically, each projection has an associated vector of axis positions where the
 * sample was taken.  Moreover, a time stamp for the data and the device identifier for
 * the source of the data is stored.
 * </p> 
 * <p>
 * <br>NOTES:
 * <br> - Currently there are no initializing constructors, or public constructors
 * for that matter.  There is a <code>create(String, Date, int)</code> static method for
 * directly instantiating objects of this type.  The thinking here is that sub-classes
 * for the particular diagnostic device will handle initializing construction.  Thus, if
 * you wish to create a <code>ProfileData</code> object directly, you must call the factory
 * method <code>create(String, Data, int)</code> then pack the data using the setter methods.  
 * </p>
 * 
 * 
 * @author Christopher K. Allen
 *
 */
public class ProfileData {

    
    /**
     * Enumeration of supported projection-data view angles.  Typically these 
     * values are used as method arguments identifying the projection data
     * under consideration. 
     * 
     * @author Christopher K. Allen
     */
    public enum Angle {

        /*
         * Enumeration Types
         */

        /** Horizontal projection */ 	        HOR(0),     
        /** Vertical projection */ 		VER(1),     
        /** Diagonal projection */		DIA(2);     


        /**
         * Return the total number of supported viewing angles.
         * 
         * @return  number of view angles
         */
        public static int   getCount() {
            return Angle.values().length;
        }


        /*
         * Local Attributes
         */

        /** Array index used by type */
        private int     index;



        /** 
         * Construct each profile type with its corresponding type identifier
         * and array index.
         * 
         * @param   index   array index associated to projection 
         */
        Angle(int index)    {
            this.index = index;
        }

        /**
         * Return the array index associated with the view angle.
         * 
         * @return  index associated with this view
         */
        public int  getIndex()  {
            return this.index;
        }
    }



    /**
     * <p>
     * Enumeration of known profile measurement device types.
     * </p>
     * </p>
     * NOTE: <br>
     * Currently this class is not used.
     * </p>
     * 
     * @author Christopher K. Allen
     *
     */
    public enum DeviceType {
        
        WIRESCANNER("WS"),
        WIREHARP("WH"),
        LASERSCANNER("LS"),
        UNKNOWN("");
        
        
        /** Device type identifier prefix */
        private String  strTypeId;
        

        /** Construct each device type with its corresponding type identifier */
        DeviceType(String strTypeId) {
            this.strTypeId = strTypeId;
        }
        
        
        /**
         * Return the device type identifier string corresponding to this type.
         * 
         * @return  device type identifier string
         */
        public String   getTypeId() {
            return this.strTypeId;
        }
        
        /**
         * Return the device type which is inferred from the given
         * device identifier string.  Specifically, we assume that
         * the device identifier string <code>strDevId</code> is 
         * prefixed by the (two-character) device type string.  If
         * the first two characters of <code>strDevId</code> are not
         * a recognized device type identifier string, the device
         * type <code>UNKNOWN</code> is returned.
         * 
         * @param   strDevId   device identifier string
         * 
         * @return  <code>DeviceType</code> object corresponding to given device id
         */
        public static DeviceType   typeFromDeviceId(String strDevId)    {
            String      strDevName   = strDevId.split(":")[1];
            String      strDevTypeId = strDevName.substring(0, 2);
            
            for (DeviceType dt : DeviceType.values()) {
                if (dt.getTypeId().equals(strDevTypeId))
                    return dt;
            }
            
            return UNKNOWN;
        }
    }
    
    
    
    /*
     * Global Methods
     */
    
    
    /**
     * <p>
     * Create a new, uninitialized instance of <code>ProfileData</code>, reserving
     * the given amount of space for data.
     * </p>
     * 
     * @param   strDevId    profile measurement device identifier
     * @param   dateTmStamp measurement date of data
     * @param   szArrData   the size of each data array
     * 
     * @return  An empty single-device data structure ready for population.  
     */
    public static ProfileData   create(String strDevId, final Date dateTmStamp, final int szArrData) {

        return new ProfileData(strDevId, dateTmStamp, szArrData);
    }
    
//    /**
//     * <p>
//     * Create a new instance of <code>ProfileData</code> initializing it with
//     * the data provided.  Noted that each ordered list of data must have size 
//     * equal to the value of <code>szArrData</code>, which is used to allocate
//     * storage space.
//     * </p>
//     * <p>
//     * The data in variable argument arrays <var>arrPos</var> and <var>arrPrj</var> are
//     * ordered according to the ordering of the values of the enumeration <code>Angle</code>.
//     * Thus, for each argument the number of data objects supplied
//     * (of type <code>List<Double></code>) should be equal to 
//     * <code>ProfileData.Angle#getCount</code>.
//     * </p>
//     * 
//     * @param   strDevId    profile measurement device identifier
//     * @param   dateTmStamp measurement date of data
//     * @param   szArrData   the size of each data array
//     * 
//     * @param	arrPos		ordered list of axis position vectors, one for each view
//     * @param   argPrj      ordered list of projection data, one for each view (by View.getIndex())
//     * 
//     * @throws ArrayIndexOutOfBoundsException   list size not equal to storage size
//     * @throws IllegalArgumentException         wrong number of projection data vectors
//     * 
//     * @see gov.sns.tools.data.profile.ProfileData.Angle
//     */
//    public static ProfileData   create(String strDevId, final Date dateTmStamp, int szArrData,
//                                       final List<Double>[] arrPos,
//                                       final List<Double>[] arrPrj
//                                       ) 
//        throws ArrayIndexOutOfBoundsException, IllegalArgumentException
//    {
//    	ProfileData.checkVarArgArray(arrPos);
//        ProfileData.checkVarArgArray(arrPrj);
//        
//        ProfileData dataNew = new ProfileData(strDevId, dateTmStamp, szArrData);
//        
//        for (Angle view : Angle.values()) {
//        	dataNew.setAxisPositions(view, arrPos[view.getIndex()]);
//            dataNew.setProjection(view, arrPrj[view.getIndex()]);
//        }
//        
//        return dataNew;
//    }
    
    
    
    
//    /**
//     * Create a new instance of <code>ProfileData</code> initializing it with
//     * the data provided.  Noted that each ordered list of data must have size 
//     * equal to the value of <code>szArrData</code>, which is used to allocate
//     * storage space.
//     * 
//     * @param   strDevId    profile measurement device identifier
//     * @param   dateTmStamp measurement date of data
//     * @param   szArrData   the size of each data array
//     * 
//     * @param   lstPos      ordered list of axis positions
//     * @param   lstHor      ordered list of horizontal profile data
//     * @param   lstVer      ordered list of vertical profile data
//     * @param   lstDia      ordered list of diagonal profile data
//     * 
//     * @throws ArrayIndexOutOfBoundsException   list size not equal to storage size
//     */
//    public static ProfileData   create(String strDevId, final Date dateTmStamp, int szArrData,
//                                       final List<Double> lstPos,
//                                       final List<Double> lstHor,
//                                       final List<Double> lstVer,
//                                       final List<Double> lstDia
//                                       ) 
//        throws ArrayIndexOutOfBoundsException
//    {
//        ProfileData dataNew = new ProfileData(strDevId, dateTmStamp, szArrData);
//        
//        dataNew.setAxisPositions(lstPos);
//        dataNew.setProjection(View.HOR, lstHor);
//        dataNew.setProjection(View.VER, lstVer);
//        dataNew.setProjection(View.DIA, lstDia);
//        
//        return dataNew;
//    }
    

    /**
     * Make and return a deep copy of the given data set.
     * 
     * @param dataOrg   original data set
     * 
     * @return          deep copy of given data
     */
    public static ProfileData   copy(final ProfileData dataOrg) {
        String  strDevId    = dataOrg.getDeviceId();
        Date    dateTmStamp = dataOrg.getTimeStamp();
        int     szData      = dataOrg.getDataSize();
        
        ProfileData dataCpy = new ProfileData(strDevId, dateTmStamp, szData);

        dataCpy.setComment(dataOrg.getComment());
        dataCpy.setPvLoggerId(dataOrg.getPvLoggerId());
        dataCpy.setActuatorPositions(dataOrg.getActuatorPositions());
        for (Angle view : Angle.values())	{
        	dataCpy.setAxisPositions(view, dataOrg.getAxisPositions(view));
            dataCpy.setProjection(view, dataOrg.getProjection(view));
        }
        
        return dataCpy;
    }

    
    
    
    /*
     * Local Attributes
     */

    /** Measurement device type */
    private DeviceType  dtSensor = DeviceType.UNKNOWN;
    
    /** Measurement device identifier */
    private String      strDevId = null;

    /** Date and time data was taken, i.e, time stamp*/
    private Date        dateTmStamp = null;
    
    /** Process Variable Logger record index */
    private int         intPvLogId = 0;
    
    /** User comments */
    private String      strComment = null;
   
    

    /** Missing data flag */
    private boolean     bolMissingData = false;

    /** Number of data points per scan */
    private int         szArrData = 0;

    /** actuator positions */
    private double[]                arrPosActr = null;

    /** projection axis positions */
    private ArrayList<double[]>     arrPosAxes = null;

    /** profile data */
    private ArrayList<double[]>     arrPrjData = null;




    /*
     * Initialization
     */
    
    
    /**
     * Create a new, uninitialized instance of <code>ProfileData</code>, reserving
     * the given amount of space for data.
     * 
     * @param   szArrData   the size of each data array
     * 
     * @deprecated
     */
    @Deprecated
    protected ProfileData(int szArrData)  {
        this.allocStorage(szArrData);
    }
    
    /**
     * Create a new, uninitialized instance of <code>ProfileData</code>, reserving
     * the given amount of space for data.
     * 
     * @param   strDevId    profile measurement device identifier
     * @param   dateTmStamp measurement date of data
     * @param   szArrData   the size of each data array
     */
    protected ProfileData(String strDevId, final Date dateTmStamp, final int szArrData)  {
        this.setDeviceId(strDevId);
        this.setTimeStamp(dateTmStamp);
        this.allocStorage(szArrData);
    }
    
//    /**
//     * Create and initialize a new <code>ProfileData</code> object.
//     *
//     * @param   strDevId    profile measurement device identifier
//     * @param   dateTmStamp measurement date of data
//     * @param   szArrData   the size of each data array
//     * 
//     * @param   arrDataLoc  vector array of wire scanner positions
//     * @param   arrDataHor  vector array of horizontal wire data
//     * @param   arrDataVer  vector array of vertical wire data
//     * @param   arrDataDia vector array of diagonal wire data
//     * 
//     * @deprecated
//     */
//    protected ProfileData(String strDevId,
//                          Date dateTmStamp, 
//                          int  szArrData, 
//                          double[] arrDataLoc, 
//                          double[] arrDataHor, double[] arrDataVer, double[] arrDataDia
//                          )
//    {
//        this.strDevId    = strDevId;
//        this.dateTmStamp = dateTmStamp;
//        
//        this.szArrData  = szArrData;
//        this.arrDataPos = arrDataLoc;
//        this.arrDataHor = arrDataHor;
//        this.arrDataVer = arrDataVer;
//        this.arrDataDia = arrDataDia;
//    }

    
    /*
     * Package Methods - Initializing Data
     */
    
    /**
     * Resets the data.  Space for the given array size is
     * allocated.  Any previous data is lost.
     * 
     * @param szArrData     new data vector size
     */
    void allocStorage(int szArrData) {
        this.szArrData = szArrData;
        this.arrPosActr = new double[szArrData];
        this.arrPosAxes = new ArrayList<double[]>(Angle.getCount());;
        this.arrPrjData = new ArrayList<double[]>(Angle.getCount());
        for (Angle view : Angle.values()) {
        	this.arrPosAxes.add(new double[szArrData]);
            this.arrPrjData.add(new double[szArrData]);
        }
    }

    /**
     * Sets the device identifier string for this data.
     * Also sets the device type as determined from the
     * string identifier.
     * 
     * @param strDevId  new device id
     * 
     * @see ProfileData#DeviceType()
     * @see ProfileData#getDeviceId()
     * @see ProfileData#getDeviceType()
     */
    void setDeviceId(String strDevId) {
        this.strDevId = strDevId;
        this.dtSensor = DeviceType.typeFromDeviceId(strDevId);
    }

    /**
     * Sets the time stamp for the data.
     * 
     * @param dateTmStamp   new time stamp
     */
    void setTimeStamp(final Date dateTmStamp) {
        this.dateTmStamp = new Date(dateTmStamp.getTime());
    }

    /**
     * Sets the Process Variable Logger (PVLogger) record identifier.
     * 
     * @param   intPvLogId  PVLogger snapshot id
     */
    void setPvLoggerId(int intPvLogId)  {
        this.intPvLogId = intPvLogId;
    }
    
    
    /**
     * Set any associated user comment.
     * 
     * @param strComment    data comment string
     */
    void setComment(String strComment)  {
        this.strComment = strComment;
    }
    

    /**
     * Clears the missing data flag.  For use by data processing
     * objects which are capable of "fixing" missing data points.
     */
    void clearMissingDataFlag() {
        this.bolMissingData = false;
    }
    
    
    
    
    /**
     * Set the actuator position for the given sample index.
     * 
     * @param   index   index of the actuator position array
     * @param   dblVal  new value for the actuator position
     * 
     * @throws  ArrayIndexOutOfBoundsException  index larger than storage capacity
     */
    public void setActuatorPositionAt(int index, double dblVal) throws ArrayIndexOutOfBoundsException {
        this.checkNewValue(index, dblVal);
        this.getActuatorPositions()[index] = dblVal;
    }

    /**
     * Set the entire actuator position vector.  
     * This is a copy operation, not a set by reference.  Note further that the 
     * given array must have length equal to the storage specified
     * by <code>ProfileData#allocStorage(int)</code>.  
     * 
     * @param   arrVals     array of actuator positions
     * 
     * @throws  ArrayIndexOutOfBoundsException  list size not equal storage allocated
     * 
     */
    public void setActuatorPositions(final double[] arrVals) throws ArrayIndexOutOfBoundsException {
        
        if (arrVals.length != this.getDataSize())
            throw new ArrayIndexOutOfBoundsException("ProfileData#setActuatorPositions: list size not equal to " + this.getDataSize());
        
        for (int index=0; index<arrVals.length; index++)   
            this.setActuatorPositionAt(index, arrVals[index]);
    }

    /**
     * Set the entire actuator position vector.  
     * Note that the given list must have length equal to the storage specified
     * by <code>ProfileData#allocStorage(int)</code>.
     * 
     * @param   lstVals     ordered list of actuator positions
     * 
     * @throws  ArrayIndexOutOfBoundsException  list size not equal storage allocated
     */
    public void setActuatorPositions(final List<Double> lstVals) throws ArrayIndexOutOfBoundsException {
        
        if (lstVals.size() != this.getDataSize())
            throw new ArrayIndexOutOfBoundsException("ProfileData#setActuatorPositions: list size not equal to " + this.getDataSize());
        
        int index = 0;
        for (double dblVal : lstVals)   
            this.setActuatorPositionAt(index++, dblVal);
    }

    
    
    
    /**
     * Set axis position at the given index.
     * 
     * @param view      viewing angle
     * @param index     index of the axis position array
     * @param dblVal    new value for the position at index
     * 
     * @throws  ArrayIndexOutOfBoundsException  index larger than storage capacity
     */
    public void setAxisPositionAt(Angle view, int  index, double dblVal) throws ArrayIndexOutOfBoundsException {
        this.checkNewValue(index, dblVal);
        this.getAxisPositions(view)[index] = dblVal;
    }

    /**
     * Set the entire axis position vector for the given viewing angle.  
     * This is a copy operation, not a set by reference.  Note further that the 
     * given array must have length equal to the storage specified
     * by <code>ProfileData#allocStorage(int)</code>.  
     * 
     * @param 	view      	viewing angle
     * @param   arrVals 	array of axis positions
     * 
     * @throws  ArrayIndexOutOfBoundsException  list size not equal storage allocated
     */
    public void setAxisPositions(Angle view, final double[] arrVals) throws ArrayIndexOutOfBoundsException {
        
        if (arrVals.length != this.getDataSize())
            throw new ArrayIndexOutOfBoundsException("ProfileData#setAxisPositions: list size not equal to " + this.getDataSize());
        
        for (int index=0; index<arrVals.length; index++)   
            this.setAxisPositionAt(view, index, arrVals[index]);
    }

    /**
     * Set the entire axis position data array for the given projection angle.  
     * Note that the given list must have length equal to the storage specified
     * by <code>ProfileData#allocStorage</code>.
     * 
     * @param	view	viewing angle
     * @param   lstVals ordered list of axis positions
     * 
     * @throws  ArrayIndexOutOfBoundsException  list size not equal storage allocated
     */
    public void setAxisPositions(Angle view, final List<Double> lstVals) throws ArrayIndexOutOfBoundsException {
        
        if (lstVals.size() != this.getDataSize())
            throw new ArrayIndexOutOfBoundsException("ProfileData#setAxisPositions: list size not equal to " + this.getDataSize());
        
        int index = 0;
        for (double dblVal : lstVals)   
            this.setAxisPositionAt(view, index++, dblVal);
    }

    
    
    
    
    /**
     * Set given view profile value at index.
     * 
     * @param view      viewing angle
     * @param index     index of the profile array
     * @param dblVal    new value of profile at index
     * 
     * @throws  ArrayIndexOutOfBoundsException  index larger than storage capacity
     */
    public void setProjectionAt(Angle view, int index, double dblVal) throws ArrayIndexOutOfBoundsException {
        this.checkNewValue(index, dblVal);
        this.getProjection(view)[index] = dblVal;
    }

    /**
     * Sets all the projection data at the given index.  The variable argument
     * list must contain only entry per <code>ProfileData.View</code>.
     * 
     * @param index     index of the axis position array
     * @param argPrj    projection values, one for each projection view
     * 
     * @throws  ArrayIndexOutOfBoundsException  index larger than storage capacity
     * @throws  IllegalArgumentException        wrong number of projection values 
     * 
     * @see gov.sns.tools.data.profile.ProfileData.Angle
     */
    public void setAllProjectionsAt(int  index, Double... argPrj) 
        throws ArrayIndexOutOfBoundsException, IllegalArgumentException
    {
        checkVarArgArray(argPrj);
        
        for (Angle view : Angle.values()) {
            double  dblVal = argPrj[view.getIndex()];
            
            this.checkNewValue(index, dblVal);
            this.getProjection(view)[index] = dblVal;
        }
    }

    /**
     * Set the entire projection data array.  This is a copy 
     * operation, not a set by reference.  Note further that the 
     * given array must have length equal to the storage specified
     * by <code>ProfileData#allocStorage(int)</code>.
     * 
     * @param   view        projection view angle
     * @param   arrVals     array of projection data
     * 
     * @throws  ArrayIndexOutOfBoundsException  data size not equal storage allocated
     */
    public void setProjection(Angle view, final double[] arrVals) throws ArrayIndexOutOfBoundsException {
        
        if (arrVals.length != this.getDataSize())
            throw new ArrayIndexOutOfBoundsException("ProfileData#setProfile: data size not equal to " + this.getDataSize());
        
        for (int index=0; index<arrVals.length; index++)
            this.setProjectionAt(view, index, arrVals[index]);
    }

    /**
     * Set the entire projection data array.  Note that the 
     * given list must have length equal to the storage specified
     * by <code>ProfileData#allocStorage(int)</code>.
     * 
     * @param   view        projection view angle
     * @param   lstVals     ordered list of projection data
     * 
     * @throws  ArrayIndexOutOfBoundsException  list size not equal storage allocated
     */
    public void setProjection(Angle view, final List<Double> lstVals) throws ArrayIndexOutOfBoundsException {
        
        if (lstVals.size() != this.getDataSize())
            throw new ArrayIndexOutOfBoundsException("ProfileData#setHorizontalProfile: list size not equal to " + this.getDataSize());
        
        int index = 0;
        for (double dblVal : lstVals)   
            this.setProjectionAt(view, index++, dblVal);
    }



    
    /**
     * Return measurement device for this data.
     * 
     * @return  the device identifier associated with this data
     */
    public String getDeviceId() {
        return this.strDevId;
    }
    
    /**
     * Return the measurement device type for this data.
     * 
     * @return  the device type associated with this data
     */
    public DeviceType getDeviceType()   {
        return this.dtSensor;
    }
    
    /**
     * Return the missing data flag.  If <code>true</code> then this
     * data set contains missing points which are represented with the
     * value <code>Double.NaN</code>.  Otherwise, the data set is 
     * complete.
     * 
     * @return  true if data set contains missing data points.
     */
    public boolean  hasMissingData() {
        return this.bolMissingData;
    }

    
    /**
     * Returns the time and date when the measurement was initiated.
     * 
     * @return  the time stamp of this data 
     */
    public Date getTimeStamp() {
        return new Date(this.dateTmStamp.getTime());
    }
    
    /**
     * Return the Process Variable Logger (PVLogger) record identifier
     * for the machine configuration when this data was taken
     * 
     * @return  PVLogger snapshot id
     */
    public int  getPvLoggerId() {
        return this.intPvLogId;
    }
    
    
    /**
     * Return the comment string associated with this data.
     * 
     * @return  user comment string or null if none
     */
    public String getComment()  {
        return this.strComment;
    }

    
    /**
     * Return the size of each data vector.
     * 
     * @return  data vector length. 
     */
    public int getDataSize() {
        return this.szArrData;
    }

    
    
    
    /**
     * Returns the vector array of axis positions for each data value.
     * 
     * @return  array of axis positions
     */
    public double[] getActuatorPositions() {
        return this.arrPosActr;
    }

    /**
     * <p>
     * Returns the actuator position at given index.
     * </p>
     * 
     * <p>
     * NOTE:
     * For the sake of speed, there is no error checking here.  Any
     * bounds overruns bubble up from here.
     * </p>
     * 
     * @param   index   index of the position vector
     * 
     * @return  axis position at index
     */
    public double getActuatorPositionAt(int index) {
        return this.arrPosActr[index];
    }
    
    
    
    
    
    /**
     * Returns the vector array of axis positions for each data value.
     * 
     * @param	view	projection viewing angle
     * 	
     * @return  array of axis positions
     */
    public double[] getAxisPositions(Angle view) {
        return this.arrPosAxes.get(view.getIndex());
    }

    /**
     * <p>
     * Returns the axis position at given index for the given viewing
     * angle.
     * </p>
     * 
     * <p>
     * NOTE:
     * For the sake of speed, there is no error checking here.  Any
     * bounds overruns bubble up from here.
     * </p>
     * 
     * @param   view    projection viewing angle
     * @param   index   index of the position vector
     * 
     * @return  axis position at index
     */
    public double getAxisPositionAt(Angle view, int index) {
        return this.arrPosAxes.get(view.getIndex())[index];
    }
    
    
    
    
    
    
    /**
     * Returns the projection data array for the given viewing angle.
     * 
     * @param	view	projection viewing angle
     * 	
     * @return projection data array
     */
    public double[] getProjection(Angle view) {
        return this.arrPrjData.get(view.getIndex());
    }

    /**
     * <p>
     * Returns the value of given projection data at given index.
     * </p>
     * 
     * <p>
     * NOTE:
     * For the sake of speed, there is no error checking here.  Any
     * bounds overruns bubble up from here.
     * </p>
     * 
     * @param   view    projection viewing angle
     * @param   index   index of the projection data vector
     * 
     * @return projection data value at index
     */
    public double getProjectionAt(Angle view, int index) {
        return this.getProjection(view)[index];
    }

    
    
    
    /*
     * Debugging
     */
    
    
    /**
     * Check to see that given object is either this object, or contains
     * exactly the same data.
     * 
     * @param   objData     object under comparison
     * 
     * @return  true if <var>objData</var> is <code>this</code> or contains the same data
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object objData) {

        // Check the easy stuff first
        if (objData == this)
            return true;
        
        if (!(objData instanceof ProfileData))
            return false;            

        
        // So we have a ProfileData object.  Start the detailed testing...
        ProfileData pdoTest = (ProfileData)objData;
        
        if (this.getDataSize() != pdoTest.getDataSize())
            return false;
        if (!this.getDeviceId().equals(pdoTest.getDeviceId()))
            return false;
        if (!this.getTimeStamp().equals(pdoTest.getTimeStamp()))
            return false;
        if (this.getPvLoggerId() != pdoTest.getPvLoggerId())
            return false;
        if (!this.checkEquality(this.getActuatorPositions(), pdoTest.getActuatorPositions()))
        	return false;
        for (Angle view : Angle.values()) {
            if (!this.checkEquality(this.getAxisPositions(view), pdoTest.getAxisPositions(view)))
                return false;
            if (!this.checkEquality(this.getProjection(view), pdoTest.getProjection(view)))
                return false;
        }
        
        return true;
    }

    /**
     * Write out the contents of this data structure to a string.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        
        // Create a string formatter
        StringWriter    os = new StringWriter();

        
        // Write out data properties
        if (this.getComment() != null)
            os.write("Comments     = " + this.getComment());
        os.write("PV Logger ID = " + Integer.toString(this.getPvLoggerId()) + "\n");
        os.write("Device ID    = " + this.getDeviceId() + "\n");
        os.write("Device Type  = " + this.getDeviceType()+ "\n");
        
        
        // Write value header
        os.write("Index\t");
        os.write("Actuator\t");
        for (Angle view : Angle.values())	{ 
            os.write("Position \t");
            os.write(view.toString() + " \t");
        }
        os.write("\n");
        
        
        // Write out data
        for (int index=0; index<this.getDataSize(); index++) {

            os.write(Integer.toString(index) + "\t");
            os.write(Double.toString(this.getActuatorPositionAt(index)) + "\t");
            for (Angle view : Angle.values()) {
                os.write(Double.toString(this.getAxisPositionAt(view, index)) + "\t");
                os.write(Double.toString(this.getProjectionAt(view, index)) + "\t");
            }

            os.write("\n");
        }

        
        // Return the formatted string
        return os.toString();
    }
    

    
    /*
     * Support Methods
     */
    
    
    /**
     * Check that the number of elements in the variable argument 
     * array is equal to the number of projection views.
     * 
     * @param   arrArgs     array of argument objects
     * 
     * @throws  IllegalArgumentException    number of arguments not equal to number of views
     * 
     * @see gov.sns.tools.data.profile.ProfileData.Angle#getCount()
     */
    private static void    checkVarArgArray(Object[] arrArgs) throws IllegalArgumentException {
        if (arrArgs.length != Angle.getCount())
            throw new IllegalArgumentException(
                            "ProfileData#checkVarArgArray(): " +  
                            " number of arguments not equal " + Angle.getCount()
                            );
    }
    
    /**
     * Check a new data value for index position and not-a-number
     * condition.
     * 
     * @param index     index into data array
     * @param dblVal    new data value
     * 
     * @throws ArrayIndexOutOfBoundsException   index exceeds storage capacity
     */
    private void checkNewValue(int index, double dblVal) throws ArrayIndexOutOfBoundsException {
        if (index >= this.getDataSize()) 
            throw new ArrayIndexOutOfBoundsException(
                            "ProfileData#checkNewValue(): Index " + index + 
                            " exceeds storage capacity " + this.getDataSize()
                             );
        
        if (dblVal == Double.NaN)
            this.bolMissingData = true;
    }

    
    /**
     * Check equality between two vector arrays
     * 
     * @param arr1  array under test
     * @param arr2  array under test
     * 
     * @return  true if <var>arr1</var> = <var>arr2</var> as vectors, false otherwise
     */
    private boolean checkEquality(double[] arr1, double[] arr2) {
        if (arr1.length != arr2.length)
            return false;
        
        for (int index=0; index<arr1.length; index++)  
            if (arr1[index] != arr2[index])
                return false;
        
        return true;
    }
}