package cgl.iotcloud.core.transport.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.BlockingQueue;

public class JMSListener {
    private static Logger LOG = LoggerFactory.getLogger(JMSListener.class);

    private Connection connection;

    private Session session;

    private Destination destination;

    private BlockingQueue<Message> inQueue;

    private MessageConsumer consumer;

    public JMSListener(Connection connection, Session session, Destination destination, BlockingQueue<Message> inQueue) {
        this.connection = connection;
        this.session = session;
        this.destination = destination;
        this.inQueue = inQueue;
    }

    public void start(){
        try {
            consumer = session.createConsumer(destination);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        inQueue.put(message);
                    } catch (InterruptedException e) {
                        LOG.error("Failed to put the message to queue", e);
                    }
                }
            });
            connection.start();
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
