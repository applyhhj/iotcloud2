package cgl.iotcloud.core.transport.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.BlockingQueue;

public class JMSSender {
    private static Logger LOG = LoggerFactory.getLogger(JMSSender.class);

    private Connection connection;

    private Session session;

    private Destination destination;

    private BlockingQueue<Message> outQueue;

    private MessageProducer producer;

    public JMSSender(Connection connection, Session session, Destination destination, BlockingQueue<Message> outQueue) {
        this.connection = connection;
        this.session = session;
        this.destination = destination;
        this.outQueue = outQueue;

        try {
            producer = session.createProducer(destination);
        } catch (JMSException e) {
            String msg = "Failed to create a message producer for destination: " + destination;
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    public void start(){
        try {
            connection.start();
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
                        Message input = outQueue.take();

                        producer.send(destination, input);
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
