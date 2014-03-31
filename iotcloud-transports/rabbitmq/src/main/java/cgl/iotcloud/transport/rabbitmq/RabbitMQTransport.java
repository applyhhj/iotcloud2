package cgl.iotcloud.transport.rabbitmq;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.Transport;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.io.IOException;
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

    private com.rabbitmq.client.Channel channel;

    private Connection conn;

    private ExecutorService executorService;

    private Map<String, RabbitMQReceiver> receivers = new HashMap<String, RabbitMQReceiver>();

    private Map<String, RabbitMQSender> senders = new HashMap<String, RabbitMQSender>();

    private Address[] addresses;

    @Override
    public void configure(Map properties) {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            Map params = (Map)properties.get(Configuration.TRANSPORT_PROPERTIES);
            Object urlProp = params.get(URL_PROPERTY);
            if (urlProp == null) {
                String message = "Url is required by the RabbitMQTransport";
                LOG.error(message);
                throw new RuntimeException(message);
            }

            Address urls[] = null;
            if (urlProp instanceof String) {
                factory.setUri((String) urlProp);
            } else if (urlProp instanceof Object []) {
                int len = ((Object[]) urlProp).length;
                urls = new Address[len];
                for (int i = 0; i < len; i++) {
                    urls[i] = new Address ((String) ((Object[]) urlProp)[i]);
                }
            }

            this.addresses = urls;

            Map threads = (Map) params.get(THREAD_PROPERTY);
            if (threads != null) {
                int core = (int) threads.get(CORE_PROPERTY);
                executorService = Executors.newScheduledThreadPool(core);
            }
        } catch (IOException e) {
            String msg = "Error in creating the RabbitMQ connection";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
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

        String destination = Configuration.getChannelJmsDestination(channelConf);
        String isTopic = Configuration.getChannelIsQueue(channelConf);
        boolean topic = true;
        if (isTopic != null && !Boolean.parseBoolean(isTopic)) {
            topic = false;
        }

        try {
            if (channel.getDirection() == Direction.OUT) {
                RabbitMQSender sender = new RabbitMQSender();
                sender.start();
                senders.put(name, sender);
            } else if (channel.getDirection() == Direction.IN) {
                JMSListener listener = new JMSListener(conFactory, destination, topic, channel.getInQueue(), channel.getConverter());
                listener.start();
                listeners.put(name, listener);
            }
        } catch (JMSException e) {
            String msg = "Failed to create connection";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
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
