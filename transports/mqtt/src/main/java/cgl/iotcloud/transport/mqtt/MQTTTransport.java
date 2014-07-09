package cgl.iotcloud.transport.mqtt;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.ChannelName;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.Transport;
import org.fusesource.mqtt.client.QoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MQTTTransport implements Transport {
    private static Logger LOG = LoggerFactory.getLogger(MQTTTransport.class);

    public static final String URL_PROPERTY = "url";

    public static final String QUEUE_NAME_PROPERTY = "queueName";

    public static final String QOS = "qosLevel";

    private Map<ChannelName, MQTTConsumer> receivers = new HashMap<ChannelName, MQTTConsumer>();

    private Map<ChannelName, MQTTProducer> senders = new HashMap<ChannelName, MQTTProducer>();

    private String url;

    private String host;

    private int port;

    private String siteId;

    @Override
    public void configure(String siteId, Map properties) {
        this.siteId = siteId;
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
                if (url.contains(":")) {
                    this.host = url.substring(0, url.indexOf(":"));
                    this.port = Integer.parseInt(url.substring(url.indexOf(":") + 1));
                } else {
                    this.host = url;
                }
            }
        } catch (Exception e) {
            String msg = "Error in key management for rabbitMQ";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void registerChannel(ChannelName name, Channel channel) {
        Map channelConf = channel.getProperties();
        if (channelConf == null) {
            throw new IllegalArgumentException("Channel properties must be specified");
        }

        String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);
        String qosProp = (String) channelConf.get(QOS);
        QoS qoS = QoS.AT_MOST_ONCE;
        if (qosProp != null) {
            int qosInt = Integer.parseInt(qosProp);

            if (qosInt == 2) {
                qoS = QoS.EXACTLY_ONCE;
            } else if (qosInt == 1) {
                qoS = QoS.AT_LEAST_ONCE;
            }
        }

        if (channel.getDirection() == Direction.OUT) {
            MQTTProducer sender = new MQTTProducer(host, port, channel.getOutQueue(), siteId + "." + queueName, qoS);
            senders.put(name, sender);
            sender.open();
        } else if (channel.getDirection() == Direction.IN) {
            MQTTConsumer listener = new MQTTConsumer(host, port, channel.getInQueue(), siteId + "." + queueName, qoS);
            receivers.put(name, listener);
            listener.open();
        }
    }

    @Override
    public void start() {
        for (MQTTProducer producer : senders.values()) {
            producer.open();
        }

        for (MQTTConsumer consumer : receivers.values()) {
            consumer.open();
        }
    }

    @Override
    public void stop() {
        for (MQTTProducer producer : senders.values()) {
            producer.close();
        }

        for (MQTTConsumer consumer : receivers.values()) {
            consumer.close();
        }
    }
}
