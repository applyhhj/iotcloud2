package cgl.iotcloud.transport.rabbitmq;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Transport;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            Map threads = (Map) params.get(THREAD_PROPERTY);
            if (threads != null) {
                int core = (int) threads.get(CORE_PROPERTY);
                executorService = Executors.newScheduledThreadPool(core);
                if (urls == null) {
                    conn = factory.newConnection(executorService);
                } else {
                    conn = factory.newConnection(executorService, urls);
                }
            } else {
                if (urls == null) {
                    conn = factory.newConnection();
                } else {
                    conn = factory.newConnection(urls);
                }
            }

            channel = conn.createChannel();
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
