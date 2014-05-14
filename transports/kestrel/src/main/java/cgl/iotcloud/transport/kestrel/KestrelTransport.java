package cgl.iotcloud.transport.kestrel;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.Transport;
import net.lag.kestrel.thrift.Kestrel;
import org.fusesource.mqtt.client.QoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class KestrelTransport implements Transport {
    private static final Integer KESTREL_DEFAULT_PORT = 1235;
    private static Logger LOG = LoggerFactory.getLogger(KestrelTransport.class);

    public static final String PROP_URLS = "servers";

    public static final String QUEUE_NAME_PROPERTY = "queueName";

    public static final String ACK = "ack";

    public static final String SERVER = "server";

    private Map<String, KestrelConsumer> receivers = new HashMap<String, KestrelConsumer>();

    private Map<String, KestrelProducer> senders = new HashMap<String, KestrelProducer>();

    private Map<String, Server> urls = new HashMap<String, Server>();

    @Override
    public void configure(Map properties) {
        try {
            Map params = (Map)properties.get(Configuration.TRANSPORT_PROPERTIES);
            Object urlProp = params.get(PROP_URLS);
            if (urlProp == null || !(urlProp instanceof Map)) {
                String message = "servers is required by the Kestrel Transport";
                LOG.error(message);
                throw new RuntimeException(message);
            }

            Map servers = (Map) urlProp;
            for (Object o : servers.keySet()) {
                if (o instanceof String) {
                    String url = ((Map) urlProp).get(o).toString();
                    String server = (String) o;
                    String tokens[] = url.split(":");
                    if (tokens.length == 2) {
                        urls.put(server, new Server(tokens[0], Integer.parseInt(tokens[1])));
                    } else {
                        urls.put(server, new Server(tokens[0], KESTREL_DEFAULT_PORT));
                    }
                }
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

        String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);
        String qosProp = (String) channelConf.get(ACK);
        String server = (String) channelConf.get(SERVER);
        int qosInt = Integer.parseInt(qosProp);

        QoS qoS = QoS.AT_MOST_ONCE;
        if (qosInt == 2) {
            qoS = QoS.EXACTLY_ONCE;
        } else if (qosInt == 1) {
            qoS = QoS.AT_LEAST_ONCE;
        }

        Server s = urls.get(server);

        if (channel.getDirection() == Direction.OUT) {
            KestrelProducer sender = new KestrelProducer(new KestrelDestination(s.getHost(), s.getPort(), queueName), channel.getInQueue(), queueName, channel.getConverter(), qoS);
            senders.put(name, sender);
        } else if (channel.getDirection() == Direction.IN) {
            Kestrel listener = new KestrelConsumer(new KestrelDestination(), channel.getInQueue(), queueName, channel.getConverter(), qoS);
            receivers.put(name, listener);
        }
    }

    @Override
    public void start() {
        for (KestrelProducer producer : senders.values()) {
            producer.open();
        }

        for (KestrelConsumer consumer : receivers.values()) {
            consumer.open();
        }
    }

    @Override
    public void stop() {
        for (KestrelProducer producer : senders.values()) {
            producer.close();
        }

        for (KestrelConsumer consumer : receivers.values()) {
            consumer.close();
        }
    }

    static class Server {
        private String host;
        private int port;

        Server(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
