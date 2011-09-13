package xal.model.elem;

//import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * Provides custom <code>BeanInfo</code> for all <code>ThinElement</code>
 * instances.  This is used for, among other things, writing a description of
 * the element to an XML file.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class ThinElementBeanInfo extends SimpleBeanInfo {
    /**
     * Specifies those properties that should be treated as public BeanInfo
     * properties via the bean introspection mechanisms.  ThinElement exports
     * no public properties.  This implementation is required to hide the
     * apparent property <code>length</code>.
     * 
     * @return an array of <code>PropertyDescriptor</code> objects for the
     * corresponding bean class
     * 
     * @see java.beans.BeanInfo#getPropertyDescriptors()
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[] {};
    }
}
