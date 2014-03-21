package cgl.iotcloud.core.transport.jms;

import cgl.iotcloud.core.transport.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.BlockingQueue;

public class JMSListener {
    private static Logger LOG = LoggerFactory.getLogger(JMSListener.class);

    private Connection connection;

    private Session session;

    private Destination dest;

    private BlockingQueue inQueue;

    private MessageConsumer consumer;

    private MessageConverter converter;

    private ConnectionFactory conFactory;

    boolean topic;

    String destination;

    public JMSListener(ConnectionFactory conFactory, String destination, boolean topic, BlockingQueue inQueue,
                       MessageConverter converter) throws JMSException {

        if (conFactory == null || destination == null || inQueue == null || converter == null) {
            throw new IllegalArgumentException("All the parameters are mandatory");
        }
        this.conFactory = conFactory;
        this.topic = topic;
        this.inQueue = inQueue;
        this.converter = converter;
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
                        Object input = converter.convert(message, null);
                        if (input != null) {
                            inQueue.put(input);
                        }
                    } catch (InterruptedException e) {
                        LOG.error("Failed to put the message to queue", e);
                    }
                }
            });
        } catch (JMSException e) {
            String msg = "Failed to create a message consumer for destination: " + destination;
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
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
