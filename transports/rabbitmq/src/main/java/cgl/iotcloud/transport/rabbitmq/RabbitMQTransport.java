package cgl.iotcloud.transport.rabbitmq;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.transport.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RabbitMQTransport extends AbstractTransport {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQTransport.class);

    public static final String URL_PROPERTY = "url";


    public static final String EXCHANGE_NAME_PROPERTY = "exchange";
    public static final String ROUTING_KEY_PROPERTY = "routingKey";
    public static final String QUEUE_NAME_PROPERTY = "queueName";



    private Map<ChannelName, RabbitMQReceiver> receivers = new HashMap<ChannelName, RabbitMQReceiver>();

    private Map<ChannelName, RabbitMQSender> senders = new HashMap<ChannelName, RabbitMQSender>();

    @Override
    public void configureTransport() {

    }

    @Override
    public Manageable registerProducer(BrokerHost host, Map channelConf, BlockingQueue queue) {
        String exchangeName = (String) channelConf.get(EXCHANGE_NAME_PROPERTY);
        String routingKey = (String) channelConf.get(ROUTING_KEY_PROPERTY);
        String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);

        RabbitMQSender sender = new RabbitMQSender(queue, exchangeName, siteId + "." + routingKey, siteId + "." + queueName, host.getUrl());
        if (executorService != null) {
            sender.setExecutorService(executorService);
        }
        return sender;
    }

    @Override
    public Manageable registerConsumer(BrokerHost host, Map channelConf, BlockingQueue queue) {
        String exchangeName = (String) channelConf.get(EXCHANGE_NAME_PROPERTY);
        String routingKey = (String) channelConf.get(ROUTING_KEY_PROPERTY);
        String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);

        RabbitMQReceiver listener = new RabbitMQReceiver(queue, siteId + "." + queueName, host.getUrl());
        if (executorService != null) {
            listener.setExecutorService(executorService);
        }

        listener.setExchangeName(exchangeName);
        listener.setRoutingKey(siteId + "." + routingKey);

        return listener;
    }

    @Override
    public void start() {
        for (RabbitMQReceiver receiver : receivers.values()) {
            receiver.start();
        }

        for (RabbitMQSender sender : senders.values()) {
            sender.start();
        }
    }

    @Override
    public void stop() {
        for (RabbitMQReceiver receiver : receivers.values()) {
            receiver.stop();
        }

        for (RabbitMQSender sender : senders.values()) {
            sender.stop();
        }
        executorService.shutdown();
    }
}
