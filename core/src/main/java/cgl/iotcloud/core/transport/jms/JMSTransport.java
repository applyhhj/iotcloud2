package cgl.iotcloud.core.transport.jms;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.msg.MessageContext;
import cgl.iotcloud.core.transport.*;
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
import java.util.concurrent.BlockingQueue;

public class JMSTransport extends AbstractTransport {
    private static Logger LOG = LoggerFactory.getLogger(JMSTransport.class);
    /** The JMS ConnectionFactory this definition refers to */
    private ConnectionFactory conFactory = null;
    /** Initial context */
    private Context context = null;

    private Map<ChannelName, JMSSender> senders = new HashMap<ChannelName, JMSSender>();
    private Map<ChannelName, JMSListener> listeners = new HashMap<ChannelName, JMSListener>();

    private String siteId;

    @Override
    public void configure(String siteId, Map properties) {
        // we are configuring everything by ourselves, no need to use the super
        try {
            this.siteId = siteId;
            Hashtable<String, String> params = new Hashtable<String, String>();
            params.putAll((Map)properties.get(Configuration.TRANSPORT_PROPERTIES));

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

    @Override
    public void configureTransport() {

    }

    @Override
    public Manageable registerProducer(BrokerHost host, Map channelConf, BlockingQueue<MessageContext> queue) {
        String destination = Configuration.getChannelJmsDestination(channelConf);
        String isTopic = Configuration.getChannelIsQueue(channelConf);
        boolean topic = true;
        if (isTopic != null && !Boolean.parseBoolean(isTopic)) {
            topic = false;
        }

        JMSSender sender = new JMSSender(conFactory, siteId + "." + destination, topic, queue);
        sender.start();
        return sender;
    }

    @Override
    public Manageable registerConsumer(BrokerHost host, Map channelConf, BlockingQueue<MessageContext> queue) {
        String destination = Configuration.getChannelJmsDestination(channelConf);
        String isTopic = Configuration.getChannelIsQueue(channelConf);
        boolean topic = true;
        if (isTopic != null && !Boolean.parseBoolean(isTopic)) {
            topic = false;
        }

        JMSListener listener = new JMSListener(conFactory, siteId + "." + destination, topic, queue);
        listener.start();
        return listener;
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
