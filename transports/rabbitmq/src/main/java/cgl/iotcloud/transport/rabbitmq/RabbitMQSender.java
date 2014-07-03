package cgl.iotcloud.transport.rabbitmq;

import cgl.iotcloud.core.transport.Manageable;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class RabbitMQSender implements Manageable {
    private static Logger LOG = LoggerFactory.getLogger(RabbitMQSender.class);

    private Channel channel;

    private Connection conn;

    private BlockingQueue outQueue;

    private String exchangeName;

    private String routingKey;

    private String queueName;

    private String url;

    private ExecutorService executorService;

    public RabbitMQSender(BlockingQueue outQueue,
                          String exchangeName,
                          String routingKey,
                          String queueName,
                          String url) {
        this.outQueue = outQueue;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.url = url;
        this.queueName = queueName;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void start() {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(url);
            if (executorService != null) {
                conn = factory.newConnection(executorService);
            } else {
                conn = factory.newConnection();
            }

            channel = conn.createChannel();
            channel.exchangeDeclare(exchangeName, "direct", false);
            channel.queueDeclare(this.queueName, true, false, false, null);
            channel.queueBind(queueName, exchangeName, routingKey);

            Thread t = new Thread(new Worker());
            t.start();
        } catch (IOException e) {
            String msg = "Error creating the RabbitMQ channel";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (Exception e) {
            String msg = "Error creating the RabbitMQ channel";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public void stop() {
        try {
            channel.close();
            conn.close();
        } catch (IOException e) {
            LOG.error("Error closing the rabbit MQ connection", e);
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
                        if (input instanceof byte []) {
                            channel.basicPublish(exchangeName, routingKey, null, (byte[]) input);
                        } else if (input instanceof RabbitMQMessage) {
                            channel.basicPublish(exchangeName, routingKey,
                                    ((RabbitMQMessage) input).getBasicProperties(), ((RabbitMQMessage) input).getBody());
                        } else {
                            throw new RuntimeException("Expepected byte array after conversion");
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
