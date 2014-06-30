package cgl.iotcloud.transport.rabbitmq;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.transport.*;

import com.rabbitmq.client.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RabbitMQTransport extends AbstractTransport {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQTransport.class);

    public static final String URL_PROPERTY = "url";
    public static final String THREAD_PROPERTY = "threads";
    public static final String CORE_PROPERTY = "core";
    public static final String MAX_PROPERTY = "max";

    public static final String EXCHANGE_NAME_PROPERTY = "exchange";
    public static final String ROUTING_KEY_PROPERTY = "routingKey";
    public static final String QUEUE_NAME_PROPERTY = "queueName";

    private ExecutorService executorService;

    private Map<ChannelName, RabbitMQReceiver> receivers = new HashMap<ChannelName, RabbitMQReceiver>();

    private Map<ChannelName, RabbitMQSender> senders = new HashMap<ChannelName, RabbitMQSender>();

    private Address[] addresses;

    @Override
    public void configureTransport() {
        Map threads = (Map) transportConfiguration.get(THREAD_PROPERTY);
        if (threads != null) {
            int core = (Integer) threads.get(CORE_PROPERTY);
            executorService = Executors.newScheduledThreadPool(core);
        }
    }

    @Override
    public void registerProducer(BrokerHost host, Channel channel) {
        Map channelConf = channel.getProperties();
        if (channelConf == null) {
            throw new IllegalArgumentException("Channel properties must be specified");
        }

        String exchangeName = (String) channelConf.get(EXCHANGE_NAME_PROPERTY);
        String routingKey = (String) channelConf.get(ROUTING_KEY_PROPERTY);
        String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);

        RabbitMQSender sender = new RabbitMQSender(channel.getConverter(), channel.getOutQueue(), exchangeName, siteId + "." + routingKey, siteId + "." + queueName, executorService, addresses, url);
        sender.start();
        senders.put(name, sender);
    }

    @Override
    public void registerConsumer(BrokerHost host, Channel channel) {
        Map channelConf = channel.getProperties();
        if (channelConf == null) {
            throw new IllegalArgumentException("Channel properties must be specified");
        }

        String exchangeName = (String) channelConf.get(EXCHANGE_NAME_PROPERTY);
        String routingKey = (String) channelConf.get(ROUTING_KEY_PROPERTY);
        String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);

        RabbitMQReceiver listener = new RabbitMQReceiver(channel.getConverter(), channel.getInQueue(), siteId + "." + queueName, executorService, addresses, url);
        listener.setExchangeName(exchangeName);
        listener.setRoutingKey(siteId + "." + routingKey);
        listener.start();
        receivers.put(channel.getName(), listener);
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
