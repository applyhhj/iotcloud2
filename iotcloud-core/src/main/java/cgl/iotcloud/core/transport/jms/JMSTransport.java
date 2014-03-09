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
import java.util.Hashtable;
import java.util.Map;

public class JMSTransport implements Transport {
    private static Logger LOG = LoggerFactory.getLogger(JMSTransport.class);
    /** The JMS ConnectionFactory this definition refers to */
    private ConnectionFactory conFactory = null;
    /** The shared JMS Connection for this JMS connection factory */
    private Connection sharedConnection = null;
    /** The shared JMS Session for this JMS connection factory */
    private Session sharedSession = null;
    /** The shared JMS MessageProducer for this JMS connection factory */
    private MessageProducer sharedProducer = null;
    /** The shared message consumer */
    private MessageConsumer sharedConsumer = null;
    /** The Shared Destination */
    private Destination sharedDestination = null;
    /** Initial context */
    private Context context = null;

    @Override
    public void open(Map properties) {
        try {
            Hashtable<String, String> params = new Hashtable<String, String>();
            params.putAll(properties);

            context = new InitialContext(params);
            conFactory = JMSUtils.lookup(context, javax.jms.ConnectionFactory.class,
                    (String) properties.get(Configuration.IOT_SENSOR_SITE_CONFAC_JNDI_NAME));
            LOG.debug("JMS ConnectionFactory initialized");
        } catch (NamingException e) {
            throw new RuntimeException("Cannot acquire JNDI context, JMS Connection factory : " +
                    properties.get(Configuration.IOT_SENSOR_SITE_CONFAC_JNDI_NAME), e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerChannel(Channel channel) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
