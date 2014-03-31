package cgl.iotcloud.transport.rabbitmq;

import cgl.iotcloud.core.transport.MessageConverter;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class RabbitMQReceiver {
    private static Logger LOG = LoggerFactory.getLogger(RabbitMQReceiver.class);

    private Channel channel;

    private Connection conn;

    private MessageConverter converter;

    private BlockingQueue inQueue;

    private String queueName;

    private boolean autoAck;

    public RabbitMQReceiver(Channel channel,
                            Connection conn,
                            MessageConverter converter,
                            BlockingQueue inQueue,
                            String queueName) {
        this.channel = channel;
        this.conn = conn;
        this.converter = converter;
        this.inQueue = inQueue;
        this.queueName = queueName;
    }

    public void start() {
        try {
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
