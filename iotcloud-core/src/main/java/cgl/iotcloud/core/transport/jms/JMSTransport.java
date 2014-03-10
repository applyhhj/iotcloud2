package cgl.iotcloud.core.transport.jms;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
            params.putAll(properties);

            context = new InitialContext(params);
            conFactory = JMSUtils.lookup(context, javax.jms.ConnectionFactory.class,
                    (String) properties.get(Configuration.IOT_SENSOR_SITE_CONFAC_JNDI_NAME));
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
            Connection connection = conFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination dest = null;
            if (topic) {
                dest = session.createTopic(destination);
            } else {
                dest = session.createQueue(destination);
            }

            if (channel.getDirection() == Channel.Direction.OUT) {
                JMSSender sender = new JMSSender(connection, session, dest);
                senders.put(name, sender);
            } else {
                JMSListener listener = new JMSListener(connection, session, dest);
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
}
