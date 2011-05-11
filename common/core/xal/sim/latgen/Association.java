/**
 * Association.java
 *
 * @author Christopher K. Allen
 * @since  May 10, 2011
 *
 */

/**
 * Association.java
 *
 * @author  Christopher K. Allen
 * @since	May 10, 2011
 */
package xal.sim.latgen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import xal.model.IElement;
import xal.smf.AcceleratorNode;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.DataListener;

/**
 * xal.sim.latgen
 *
 *
 *
 * @author Christopher K. Allen
 * @since   May 10, 2011
 */
public class Association {

    
    /**
     * <p>
     * Represents a parameter association between the hardware and the
     * modeling element of the outer class.  Basically this consists 
     * of the matching "<i>getter</i>" 
     * method of the hardware device to the appropriate "<i>setter</i>" 
     * method of the modeling element.
     * There may be many parameter associations between a hardware device
     * and modeling element, or there may be none.
     * </p>
     * <p>
     * This class also pipes the parameter value through, from the hardware
     * device to the modeling element using the method 
     * <code>{@link #syncValue(AcceleratorNode, IElement)}</code>.  
     * That is, the parameter of the 
     * modeling element is updated, or synchronized, to the current value 
     * of the hardware.
     * </p>
     *
     * @author Christopher K. Allen
     * @since   May 10, 2011
     */
    public class Parameter implements DataListener {
        
        /*
         * Global Constants
         */
        
        /** The data label used to identify <code>Parameter</code> data (XML element name)*/
        static final public String      STR_LBL_PARAMETER = "parameter";
        
        /** Array of XML attributes for the <code>Parameter</code> data */
        final public String[]           ARR_STR_ATTR = { "label", "smfget", "mset", "type" }; 
        
        
        /*
         * Local Attributes
         */
        
        /** The name of the parameter */
        private String      strName;
        
        /** The data type of the parameter */
        private Class<?>    typParam;
        
        /** The source method for parameter values */
        private Method      mthSrc;
        
        /** The sink method for parameter values */
        private Method      mthSnk;
        
        
        
        /**
         * @param strTypeName
         * @param strMthSrc
         * @param strMthSnk
         * @throws GenerationException
         *
         * @author  Christopher K. Allen
         * @since   May 10, 2011
         */
        public Parameter(String strTypeName, String strMthSrc, String strMthSnk)
            throws GenerationException
        {
            this(strTypeName, strMthSrc, strMthSnk, null);
        }
        
        
        /**
         * Creates a new association <code>Parameter</code> object according to
         * the given specifications.
         * 
         * @param strTypeName
         * @param strMthSrc
         * @param strMthSnk
         * @param strParmName
         * 
         * @throws GenerationException
         *
         * @author  Christopher K. Allen
         * @since   May 10, 2011
         */
        public Parameter(String strTypeName, String strMthSrc, String strMthSnk, String strParmName) 
            throws GenerationException 
        {
            Class<? extends AcceleratorNode>    typHware = Association.this.getHardwareType();
            Class<? extends IElement>           typModel = Association.this.getModelType();
            
            try {
                this.strName = strParmName;
                this.typParam  = Class.forName(strTypeName);
                this.mthSrc    = typHware.getDeclaredMethod(strMthSrc, this.typParam);
                this.mthSnk    = typModel.getDeclaredMethod(strMthSnk, this.typParam);

            } catch (ClassNotFoundException e) {
                throw new GenerationException("Unable to identify parameter type", e);

            } catch (SecurityException e) {
                throw new GenerationException("Parameter method is unreachable", e);

            } catch (NoSuchMethodException e) {
                throw new GenerationException("A parameter method does not exist", e);

            }
        }
        
        /**
         * Returns the name identifier of this parameter.
         *  
         * @return  Parameter name
         *
         * @author Christopher K. Allen
         * @since  May 10, 2011
         */
        public String   getName() {
            return this.strName;
        }
        
        
        /*
         * Operations
         */
        
        /**
         * Synchronized the parameter of the model element with the
         * associated parameter (i.e., <code>this</code>) of the hardware 
         * element.
         *
         * @param smfNode   The SMF hardware device
         * @param modElem   The modeling element
         * 
         * @throws IllegalArgumentException  The arguments are of the wrong type
         * @throws IllegalAccessException    There is an access restriction on a setter or getter 
         * @throws InvocationTargetException The setter or getter threw an exception
         *
         * @author Christopher K. Allen
         * @since  May 11, 2011
         */
        public void syncValue(AcceleratorNode smfNode, IElement modElem)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
        {
            Class<? extends AcceleratorNode>    typHware = Association.this.getHardwareType();
            Class<? extends IElement>           typModel = Association.this.getModelType();
            
            if ( !smfNode.getClass().equals(typHware) || !modElem.getClass().equals(typModel) )
                throw new IllegalArgumentException("Argument types must be " + typHware.getName() +
                                                   " and " + typModel.getName()
                                                    );

            // Invoke the "getter" method of the hardware object
            Object  objVal = this.mthSrc.invoke(smfNode, (Object[])null);

            // Invoke the "setter method of the model element
            this.mthSnk.invoke(modElem, objVal);

        }


        /**
         * Returns the label identifier under which the <code>Parameter</code>
         * data is stored.
         * 
         * @return  value of the constant <code>{@link #STR_LBL_PARAMETER}</code>
         * @since May 11, 2011
         * @see xal.tools.data.DataListener#dataLabel()
         */
        @Override
        public String dataLabel() {
            return Association.Parameter.STR_LBL_PARAMETER;
        }


        /**
         * @since May 11, 2011
         * @see xal.tools.data.DataListener#update(xal.tools.data.DataAdaptor)
         */
        @Override
        public void update(DataAdaptor daSrc) throws DataFormatException {
            
            if ( !daSrc.hasAttribute("label") )
                throw new DataFormatException("'label' attribute is corrupt");
            if ( !daSrc.hasAttribute("smfget") )
                throw new DataFormatException("'smfget' attribute is corrupt");
        }


        /**
         * @since May 11, 2011
         * @see xal.tools.data.DataListener#write(xal.tools.data.DataAdaptor)
         */
        @Override
        public void write(DataAdaptor adaptor) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    
    /*
     * Local Attributes
     */
    
    /** The data type of the SMF hardware device */
    private Class<? extends AcceleratorNode>    typHware;
    
    /** the data type of the modeling element */
    private Class<? extends IElement>           typModel;


    /*
     * Initialization
     */
    
    
    
    /*
     * Attributes
     */
    
    /**
     * @return the typHware
     */
    public Class<? extends AcceleratorNode> getHardwareType() {
        return typHware;
    }

    /**
     * @return the typModel
     */
    public Class<? extends IElement> getModelType() {
        return typModel;
    }
    
    
    
}
