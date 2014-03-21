package cgl.iotcloud.core.transport.jms;

import cgl.iotcloud.core.transport.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.BlockingQueue;

public class JMSSender {
    private static Logger LOG = LoggerFactory.getLogger(JMSSender.class);

    private Connection connection;

    private Session session;

    private Destination dest;

    private BlockingQueue<Message> outQueue;

    private MessageProducer producer;

    private MessageConverter converter;

    private ConnectionFactory conFactory;

    boolean topic;

    String destination;

    public JMSSender(ConnectionFactory conFactory, String destination, boolean topic,
                     BlockingQueue outQueue, MessageConverter converter) {
        if (conFactory == null || destination == null || outQueue == null || converter == null) {
            throw new IllegalArgumentException("All the parameters are mandatory");
        }
        this.conFactory = conFactory;
        this.topic = topic;
        this.outQueue  = outQueue;
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
                        Object input = outQueue.take();
                        Object converted = converter.convert(input, session);
                        if (converted instanceof Message) {
                            producer.send(dest, (Message) converted);
                        }
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
}
