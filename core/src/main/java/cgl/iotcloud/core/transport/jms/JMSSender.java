package cgl.iotcloud.core.transport.jms;

import cgl.iotcloud.core.msg.MessageContext;
import cgl.iotcloud.core.transport.Manageable;
import cgl.iotcloud.core.transport.TransportConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class JMSSender implements Manageable {
    private static Logger LOG = LoggerFactory.getLogger(JMSSender.class);

    private Connection connection;

    private Session session;

    private Destination dest;

    private BlockingQueue<MessageContext> outQueue;

    private MessageProducer producer;

    private ConnectionFactory conFactory;

    boolean topic;

    String destination;

    public JMSSender(ConnectionFactory conFactory, String destination, boolean topic,
                     BlockingQueue<MessageContext> outQueue) {
        if (conFactory == null || destination == null || outQueue == null) {
            throw new IllegalArgumentException("All the parameters are mandatory");
        }
        this.conFactory = conFactory;
        this.topic = topic;
        this.outQueue  = outQueue;
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
            producer = session.createProducer(dest);
            // start the thread to listen
            Thread t = new Thread(new Worker());
            t.start();
        } catch (JMSException e) {
            String msg = "Failed to create a message producer for destination: " + destination;
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    public void stop() {
        try {
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            LOG.error("Error occurred while closing JMS connections");
        }

    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            boolean run = true;
            int errorCount = 0;
            while (run) {
                try {
                    try {
                        MessageContext input = outQueue.take();
                        // create a bytemessae
                        BytesMessage bytesMessage = session.createBytesMessage();
                        bytesMessage.setStringProperty(TransportConstants.SENSOR_ID, input.getSensorId());
                        setMessageProperties(bytesMessage, input.getProperties());

                        bytesMessage.writeBytes(input.getBody());

                        producer.send(dest, bytesMessage);
                    } catch (InterruptedException e) {
                        LOG.error("Exception occurred in the worker listening for consumer changes", e);
                    }
                } catch (Throwable t) {
                    errorCount++;
                    if (errorCount <= 3) {
                        LOG.error("Error occurred " + errorCount + " times.. trying to continue the worker");
                    } else {
                        LOG.error("Error occurred " + errorCount + " times.. terminating the worker");
                        run = false;
                    }
                }
            }
            String message = "Unexpected notification type";
            LOG.error(message);
            throw new RuntimeException(message);
        }
    }

    private static void setMessageProperties (Message msg, Map<String, Object> properties) throws JMSException {
        if (properties == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String propertyName = entry.getKey ();
            Object value = entry.getValue ();
            msg.setObjectProperty(propertyName, value);
        }
    }
}
