package xal.model.elem;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * Provides custom <code>BeanInfo</code> for all <code>ThickElement</code>
 * instances.  This is used for, among other things, writing a description of
 * the element to an XML file.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class ThickElementBeanInfo extends SimpleBeanInfo {
    /**
     * Specifies those properties that should be treated as public BeanInfo
     * properties via the bean introspection mechanisms.  ThickElement exports
     * only the property <code>subCount</code>.
     * 
     * @return an array of <code>PropertyDescriptor</code> objects for the
     * corresponding bean class
     * 
     * @see java.beans.BeanInfo#getPropertyDescriptors()
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor length = new PropertyDescriptor("Length", ThickElement.class);
            return new PropertyDescriptor[] { length };
            
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
}
