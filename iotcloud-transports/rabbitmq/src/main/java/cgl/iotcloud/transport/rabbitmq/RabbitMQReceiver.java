package cgl.iotcloud.transport.rabbitmq;

import cgl.iotcloud.core.transport.MessageConverter;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class RabbitMQReceiver {
    private static Logger LOG = LoggerFactory.getLogger(RabbitMQReceiver.class);

    private Channel channel;

    private Connection conn;

    private MessageConverter converter;

    private BlockingQueue inQueue;

    private String queueName;

    private boolean autoAck = false;

    private Address []addresses;

    private String url;

    private ExecutorService executorService;

    public RabbitMQReceiver(MessageConverter converter,
                            BlockingQueue inQueue,
                            String queueName,
                            ExecutorService executorService,
                            Address []addresses,
                            String url) {
        this.converter = converter;
        this.inQueue = inQueue;
        this.executorService = executorService;
        this.queueName = queueName;
        this.addresses = addresses;
        this.url = url;
    }

    public void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            if (addresses == null) {
                factory.setUri(url);
                if (executorService != null) {
                    conn = factory.newConnection(executorService);
                } else {
                    conn = factory.newConnection();
                }
            } else {
                if (executorService != null) {
                    conn = factory.newConnection(executorService, addresses);
                } else {
                    conn = factory.newConnection(addresses);
                }
            }

            channel = conn.createChannel();

            channel.basicConsume(queueName, autoAck, "myConsumerTag",
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body)
                                throws IOException {
                            long deliveryTag = envelope.getDeliveryTag();
                            try {
                                Object o = converter.convert(body, null);
                                inQueue.put(o);
                            } catch (InterruptedException e) {
                                LOG.error("Failed to put the object to the queue");
                            }
                            channel.basicAck(deliveryTag, false);
                        }
                    });
        } catch (IOException e) {
            String msg = "Error consuming the message";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (Exception e) {
            String msg = "Error connecting to broker";
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
}
