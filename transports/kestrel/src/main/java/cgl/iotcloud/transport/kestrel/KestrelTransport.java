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
    private static Logger LOG = LoggerFactory.getLogger(KestrelTransport.class);

    public static final String URL_PROPERTY = "url";

    public static final String QUEUE_NAME_PROPERTY = "queueName";

    public static final String QOS = "qosLevel";

    private Map<String, KestrelConsumer> receivers = new HashMap<String, KestrelConsumer>();

    private Map<String, KestrelProducer> senders = new HashMap<String, KestrelProducer>();

    private String url;

    @Override
    public void configure(Map properties) {
        try {
            Map params = (Map)properties.get(Configuration.TRANSPORT_PROPERTIES);
            Object urlProp = params.get(URL_PROPERTY);
            if (urlProp == null) {
                String message = "Url is required by the MQTTTransport";
                LOG.error(message);
                throw new RuntimeException(message);
            }

            if (urlProp instanceof String) {
                this.url = (String) urlProp;
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
        String qosProp = (String) channelConf.get(QOS);
        int qosInt = Integer.parseInt(qosProp);

        QoS qoS = QoS.AT_MOST_ONCE;
        if (qosInt == 2) {
            qoS = QoS.EXACTLY_ONCE;
        } else if (qosInt == 1) {
            qoS = QoS.AT_LEAST_ONCE;
        }

        if (channel.getDirection() == Direction.OUT) {
            KestrelProducer sender = new KestrelProducer(url, channel.getInQueue(), queueName, channel.getConverter(), qoS);
            senders.put(name, sender);
        } else if (channel.getDirection() == Direction.IN) {
            Kestrel listener = new KestrelConsumer(url, channel.getInQueue(), queueName, channel.getConverter(), qoS);
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
}
