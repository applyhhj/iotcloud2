package cgl.iotcloud.core.transport.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.Reference;

public class JMSUtils {
    private static Logger log = LoggerFactory.getLogger(JMSUtils.class);

    private static Context

    private static <T> T lookup(Context context, Class<T> clazz, String name) throws Exception {

        Object object = context.lookup(name);
        try {
            return clazz.cast(object);
        } catch (ClassCastException ex) {
            // Instead of a ClassCastException, throw an exception with some
            // more information.
            if (object instanceof Reference) {
                Reference ref = (Reference)object;
                throw new Exception("JNDI failed to de-reference Reference with name " +
                        name + "; is the factory " + ref.getFactoryClassName() +
                        " in your classpath?");
            } else {
                throw new IllegalArgumentException("JNDI lookup of name " + name + " returned a " +
                        object.getClass().getName() + " while a " + clazz + " was expected");
            }
        }
    }
}
