package cgl.iotcloud.transport.mqtt;

import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.Transport;

import java.util.HashMap;
import java.util.Map;

public class MQTTTransport implements Transport {
    public static final String URL_PROPERTY = "url";

    public static final String QUEUE_NAME_PROPERTY = "queueName";

    public static final String QOS = "qosLevel";

    private Map<String, MQTTConsumer> receivers = new HashMap<String, MQTTConsumer>();

    private Map<String, MQTTProducer> senders = new HashMap<String, MQTTProducer>();

    @Override
    public void configure(Map properties) {

    }

    @Override
    public void registerChannel(String name, Channel channel) {
        Map channelConf = channel.getProperties();
        if (channelConf == null) {
            throw new IllegalArgumentException("Channel properties must be specified");
        }

        String url = (String) channelConf.get(URL_PROPERTY);
        String queueName = (String) channelConf.get(QUEUE_NAME_PROPERTY);
        if (channel.getDirection() == Direction.OUT) {
            MQTTProducer sender = new MQTTProducer();
            sender.start();
            senders.put(name, sender);
        } else if (channel.getDirection() == Direction.IN) {
            MQTTConsumer listener = new MQTTConsumer(url, );
            listener.open();
            receivers.put(name, listener);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
