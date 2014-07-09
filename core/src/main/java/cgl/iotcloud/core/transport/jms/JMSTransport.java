package cgl.iotcloud.core.transport.jms;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.msg.MessageContext;
import cgl.iotcloud.core.transport.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.Reference;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class JMSTransport extends AbstractTransport {
    private static Logger LOG = LoggerFactory.getLogger(JMSTransport.class);

    @Override
    public void configureTransport() {}

    @Override
    public Manageable registerProducer(BrokerHost host, Map channelConf, BlockingQueue<MessageContext> queue) {
        String destination = Configuration.getChannelJmsDestination(channelConf);
        String isTopic = Configuration.getChannelIsQueue(channelConf);
        boolean topic = true;
        if (isTopic != null && !Boolean.parseBoolean(isTopic)) {
            topic = false;
        }

        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory(host.getUrl());

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

        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory(host.getUrl());

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
