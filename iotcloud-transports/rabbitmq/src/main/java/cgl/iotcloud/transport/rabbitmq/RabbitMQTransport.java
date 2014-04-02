package cgl.iotcloud.transport.rabbitmq;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.Transport;

import com.rabbitmq.client.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RabbitMQTransport implements Transport {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQTransport.class);

    public static final String URL_PROPERTY = "url";
    public static final String THREAD_PROPERTY = "threads";
    public static final String CORE_PROPERTY = "core";
    public static final String MAX_PROPERTY = "max";

    public static final String EXCHANGE_NAME_PROPERTY = "exchange";
    public static final String ROUTING_KEY_PROPERTY = "routingKey";
    public static final String QUEUE_NAME_PROPERTY = "queueName";

    private ExecutorService executorService;

    private Map<String, RabbitMQReceiver> receivers = new HashMap<String, RabbitMQReceiver>();

    private Map<String, RabbitMQSender> senders = new HashMap<String, RabbitMQSender>();

    private Address[] addresses;

    private String url;

    @Override
    public void configure(Map properties) {
        try {
            Map params = (Map)properties.get(Configuration.TRANSPORT_PROPERTIES);
            Object urlProp = params.get(URL_PROPERTY);
            if (urlProp == null) {
                String message = "Url is required by the RabbitMQTransport";
                LOG.error(message);
                throw new RuntimeException(message);
            }

            if (urlProp instanceof String) {
                this.url = (String) urlProp;
            } else if (urlProp instanceof Object []) {
                Address urls[];
                int len = ((Object[]) urlProp).length;
                urls = new Address[len];
                for (int i = 0; i < len; i++) {
                    urls[i] = new Address ((String) ((Object[]) urlProp)[i]);
                }
                this.addresses = urls;
            }

            Map threads = (Map) params.get(THREAD_PROPERTY);
            if (threads != null) {
                int core = (Integer) threads.get(CORE_PROPERTY);
                executorService = Executors.newScheduledThreadPool(core);
            }
        } catch (Exception e) {
            String msg = "Error in key management for rabbitMQ";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void registerChannel(String name, Channel channel) {
        Map channelConf = channel.getProperties();
        if (channelConf == null) {
            throw new IllegalArgumentException("Channel properties must be specified");
        }

        if (channel.getDirection() == Direction.OUT) {
            String exchangeName = (String) channelConf.get(EXCHANGE_NAME_PROPERTY);
            String routingKey = (String) channelConf.get(ROUTING_KEY_PROPERTY);
            String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);

            RabbitMQSender sender = new RabbitMQSender(channel.getConverter(), channel.getOutQueue(), exchangeName, routingKey, queueName, executorService, addresses, url);
            sender.start();
            senders.put(name, sender);
        } else if (channel.getDirection() == Direction.IN) {
            String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);

            RabbitMQReceiver listener = new RabbitMQReceiver(channel.getConverter(), channel.getInQueue(), queueName, executorService, addresses, url);
            listener.start();
            receivers.put(name, listener);
        }
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
