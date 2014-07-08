package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.msg.TransportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Channel {
    private static Logger LOG = LoggerFactory.getLogger(Channel.class);

    private BlockingQueue inQueue;

    private BlockingQueue<TransportMessage> outQueue;

    private List<MessageProcessor> messageProcessors = new ArrayList<MessageProcessor>();

    private Map properties = new HashMap();

    private Direction direction;

    private String name;

    private MessageConverter converter;

    private String sensorID;

    public Channel(String name, Direction direction, MessageConverter converter) {
        this.name = name;
        this.direction = direction;
        this.converter = converter;
    }

    public void setInQueue(BlockingQueue inQueue) {
        this.inQueue = inQueue;
    }

    public void setOutQueue(BlockingQueue outQueue) {
        this.outQueue = outQueue;
    }

    public String getSensorID() {
        return sensorID;
    }

    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }

    public String getName() {
        return name;
    }

    public void addMessageProcessor(MessageProcessor messageProcessor) {
        this.messageProcessors.add(messageProcessor);
    }

    public BlockingQueue getInQueue() {
        return inQueue;
    }

    public BlockingQueue getOutQueue() {
        return outQueue;
    }

    public Direction getDirection() {
        return direction;
    }

    public Map getProperties() {
        return properties;
    }

    public void open() {

    }

    public MessageConverter getConverter() {
        return converter;
    }

    @SuppressWarnings("unchecked")
    public void addProperties(Map properties) {
        this.properties.putAll(properties);
    }

    public void publish(byte []message, Map<String, String> properties) {
        TransportMessage transportMessage = new TransportMessage(sensorID, message, properties);
        if (outQueue == null) {
            throw new RuntimeException("The channel must be bound to a transport");
        }

        try {
            outQueue.put(transportMessage);
        } catch (InterruptedException e) {
            LOG.error("Failed to put the message to queue", e);
        }
    }

    public void close() {
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
