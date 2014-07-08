package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.MessageReceiver;
import cgl.iotcloud.core.msg.TransportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Channel {
    private Logger LOG = LoggerFactory.getLogger(Channel.class);

    private BlockingQueue userQueue;

    private List<MessageProcessor> messageProcessors = new ArrayList<MessageProcessor>();

    private Map properties = new HashMap();

    private Direction direction;

    private String name;

    private String sensorID;

    private MessageConverter converter;

    private MessageReceiver receiver;

    private BlockingQueue transportQueue;

    public Channel(String name, String sensorID, Direction direction) {
        this.name = name;
        this.direction = direction;
        this.sensorID = sensorID;
    }

    public void setTransportQueue(BlockingQueue transportQueue) {
        this.transportQueue = transportQueue;
    }

    public String getName() {
        return name;
    }

    public void addMessageProcessor(MessageProcessor messageProcessor) {
        this.messageProcessors.add(messageProcessor);
    }

    public Direction getDirection() {
        return direction;
    }

    public Map getProperties() {
        return properties;
    }

    public String getSensorID() {
        return sensorID;
    }

    public MessageConverter getConverter() {
        return converter;
    }

    @SuppressWarnings("unchecked")
    public void addProperties(Map properties) {
        this.properties.putAll(properties);
    }

    public BlockingQueue getUserQueue() {
        return userQueue;
    }

    public void publish(byte []message, Map<String, String> properties) {
        if (transportQueue != null) {
            TransportMessage transportMessage = new TransportMessage(sensorID, message, properties);
            try {
                transportQueue.put(transportMessage);
            } catch (InterruptedException e) {
                LOG.error("Error putting message to transport queue", e);
            }
        } else {
            throw new RuntimeException("The channel must be bound to a transport");
        }
    }

    public void subscribe(MessageReceiver receiver) {
        this.receiver = receiver;
    }

    public MessageReceiver getReceiver() {
        return receiver;
    }

    public void close() {

    }

    public void open() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Channel channel = (Channel) o;
        return !(name != null ? !name.equals(channel.name) : channel.name != null);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
