package cgl.iotcloud.transport.rabbitmq;

import cgl.iotcloud.core.msg.MessageContext;
import cgl.iotcloud.core.transport.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class RabbitMQTransport extends AbstractTransport {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQTransport.class);

    public static final String EXCHANGE_NAME_PROPERTY = "exchange";
    public static final String ROUTING_KEY_PROPERTY = "routingKey";
    public static final String QUEUE_NAME_PROPERTY = "queueName";

    @Override
    public void configureTransport() {

    }

    @Override
    public Manageable registerProducer(BrokerHost host, String prefix, Map channelConf, BlockingQueue<MessageContext> queue) {
        LOG.info("Registering producer to host {}", host);
        String exchangeName = (String) channelConf.get(EXCHANGE_NAME_PROPERTY);
        String routingKey = (String) channelConf.get(ROUTING_KEY_PROPERTY);
        String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);

        RabbitMQSender sender = new RabbitMQSender(queue, exchangeName, routingKey, prefix + "." + queueName, host.getUrl());
        if (executorService != null) {
            sender.setExecutorService(executorService);
        }
        return sender;
    }

    @Override
    public Manageable registerConsumer(BrokerHost host, String prefix, Map channelConf, BlockingQueue<MessageContext> queue) {
        LOG.info("Registering consumer to host {}", host);
        String exchangeName = (String) channelConf.get(EXCHANGE_NAME_PROPERTY);
        String routingKey = (String) channelConf.get(ROUTING_KEY_PROPERTY);
        String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);

        RabbitMQReceiver listener = new RabbitMQReceiver(queue, prefix + "." + queueName, host.getUrl());
        if (executorService != null) {
            listener.setExecutorService(executorService);
        }

        listener.setExchangeName(exchangeName);
        listener.setRoutingKey(prefix + "." + routingKey);

        return listener;
    }
}
