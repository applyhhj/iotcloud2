package cgl.iotcloud.core.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Channel {
    private BlockingQueue userQueue;

    private List<MessageProcessor> messageProcessors = new ArrayList<MessageProcessor>();

    private Map properties = new HashMap();

    private Direction direction;

    private String name;

    private String sensorID;

    private MessageConverter converter;

    public Channel(String name, String sensorID, Direction direction,
                   BlockingQueue userQueue, MessageConverter converter) {
        this.name = name;
        this.userQueue = userQueue;
        this.direction = direction;
        this.converter = converter;
        this.sensorID = sensorID;
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
