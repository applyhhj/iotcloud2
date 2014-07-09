package cgl.iotcloud.core.transport.jms;

import cgl.iotcloud.core.msg.MessageContext;
import cgl.iotcloud.core.transport.Manageable;
import cgl.iotcloud.core.transport.TransportConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class JMSListener implements Manageable {
    private static Logger LOG = LoggerFactory.getLogger(JMSListener.class);

    private Connection connection;

    private Session session;

    private Destination dest;

    private BlockingQueue<MessageContext> inQueue;

    private MessageConsumer consumer;

    private ConnectionFactory conFactory;

    private boolean topic;

    private String destination;

    public JMSListener(ConnectionFactory conFactory, String destination, boolean topic, BlockingQueue<MessageContext> inQueue) {

        if (conFactory == null || destination == null || inQueue == null) {
            throw new IllegalArgumentException("All the parameters are mandatory");
        }
        this.conFactory = conFactory;
        this.topic = topic;
        this.inQueue = inQueue;
        this.destination = destination;
    }

    public void start(){
        try {
            this.connection = conFactory.createConnection();
            this.connection.start();

            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            if (topic) {
                dest = session.createTopic(destination);
            } else {
                dest = session.createQueue(destination);
            }

            consumer = session.createConsumer(dest);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        if (message instanceof BytesMessage) {
                            BytesMessage bytesMessage = (BytesMessage) message;
                            String sensorId = message.getStringProperty(TransportConstants.SENSOR_ID);

                            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                            bytesMessage.readBytes(bytes);

                            Map<String, Object> properties = getMessageProperties(message);
                            MessageContext messageContext = new MessageContext(sensorId, bytes, properties);
                            inQueue.put(messageContext);
                        }
                    } catch (InterruptedException e) {
                        LOG.error("Failed to put the message to queue", e);
                    } catch (JMSException e) {
                        LOG.warn("Message received without a sensor ID, discarding");
                    }
                }
            });
        } catch (JMSException e) {
            String msg = "Failed to create a message consumer for destination: " + destination;
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, Object> getMessageProperties(Message msg) throws JMSException {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        Enumeration srcProperties = msg.getPropertyNames();
        while (srcProperties.hasMoreElements()) {
            String propertyName = (String) srcProperties.nextElement();
            properties.put(propertyName, msg.getObjectProperty(propertyName));
        }
        return properties;
    }

    public void stop() {
        try {
            consumer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            LOG.error("Error occurred while closing JMS connections");
        }
    }
}
