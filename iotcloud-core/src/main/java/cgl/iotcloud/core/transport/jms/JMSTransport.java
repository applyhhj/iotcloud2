package cgl.iotcloud.core.transport.jms;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class JMSTransport implements Transport {
    private static Logger LOG = LoggerFactory.getLogger(JMSTransport.class);
    /** The JMS ConnectionFactory this definition refers to */
    private ConnectionFactory conFactory = null;
    /** Initial context */
    private Context context = null;

    private Map<String, JMSSender> senders = new HashMap<String, JMSSender>();
    private Map<String, JMSListener> listeners = new HashMap<String, JMSListener>();

    @Override
    public void configure(Map properties) {
        try {
            Hashtable<String, String> params = new Hashtable<String, String>();
            params.putAll((Map)properties.get(Configuration.JMS_PROPERTIES));

            context = new InitialContext(params);
            conFactory = lookup(context, javax.jms.ConnectionFactory.class,
                    params.get(Configuration.IOT_SENSOR_SITE_CONFAC_JNDI_NAME));
            LOG.debug("JMS ConnectionFactory initialized");
        } catch (NamingException e) {
            String msg = "Cannot acquire JNDI context, JMS Connection factory : " +
                    properties.get(Configuration.IOT_SENSOR_SITE_CONFAC_JNDI_NAME);
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        } catch (Exception e) {
            String msg = "Cannot acquire JNDI context, JMS Connection factory : " +
                    properties.get(Configuration.IOT_SENSOR_SITE_CONFAC_JNDI_NAME);
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    public void registerChannel(String name, Channel channel) {
        Map channelConf = channel.getProperties();
        if (channelConf == null) {
            throw new IllegalArgumentException("Channel properties must be specified");
        }

        String destination = Configuration.getChannelJmsDestination(channelConf);
        String isTopic = Configuration.getChannelIsQueue(channelConf);
        boolean topic = true;
        if (isTopic != null && !Boolean.parseBoolean(isTopic)) {
            topic = false;
        }

        try {
            if (channel.getDirection() == Direction.OUT) {
                JMSSender sender = new JMSSender(conFactory, destination, topic, channel.getOutQueue(), channel.getConverter());
                sender.start();
                senders.put(name, sender);
            } else if (channel.getDirection() == Direction.IN) {
                JMSListener listener = new JMSListener(conFactory, destination, topic, channel.getInQueue(), channel.getConverter());
                listener.start();
                listeners.put(name, listener);
            }
        } catch (JMSException e) {
            String msg = "Failed to create connection";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void start() {
        for (Map.Entry<String, JMSSender> e : senders.entrySet()) {
            e.getValue().start();
        }

        for (Map.Entry<String, JMSListener> e : listeners.entrySet()) {
            e.getValue().start();
        }
    }

    @Override
    public void stop() {
        for (Map.Entry<String, JMSSender> e : senders.entrySet()) {
            e.getValue().stop();
        }

        for (Map.Entry<String, JMSListener> e : listeners.entrySet()) {
            e.getValue().stop();
        }
    }

    public static <T> T lookup(Context context, Class<T> clazz, String name) throws Exception {

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
