package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.msg.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Channel {
    private static Logger LOG = LoggerFactory.getLogger(Channel.class);

    private BlockingQueue inQueue;

    private BlockingQueue<MessageContext> outQueue;

    private Map properties = new HashMap();

    private Direction direction;

    private String name;

    private String sensorID;

    private boolean grouped = false;

    private enum State {
        OPEN,
        CLOSED
    }

    private State state = State.OPEN;

    public Channel(String name, Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    public void setInQueue(BlockingQueue<MessageContext> inQueue) {
        this.inQueue = inQueue;
    }

    public void setOutQueue(BlockingQueue<MessageContext> outQueue) {
        this.outQueue = outQueue;
    }

    public String getSensorID() {
        return sensorID;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }

    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }

    public String getName() {
        return name;
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

    public void setName(String name) {
        this.name = name;
    }

    public void open() {
        this.state = State.OPEN;
    }

    public boolean isGrouped() {
        return grouped;
    }

    @SuppressWarnings("unchecked")
    public void addProperties(Map properties) {
        this.properties.putAll(properties);
    }

    public void publish(MessageContext message) {
        checkOpen();

        if (state == State.CLOSED) {
            String msg = "The channel is in closed state and cannot send";
            LOG.warn(msg);
            return;
        }

        try {
            outQueue.put(message);
        } catch (InterruptedException e) {
            LOG.error("Failed to put the message to queue", e);
        }
    }

    private void checkOpen() {
        if (outQueue == null) {
            throw new RuntimeException("The channel must be bound to a transport");
        }
    }

    public void publish(byte []message) {
        MessageContext messageContext = new MessageContext(sensorID, message);
        checkOpen();

        if (state == State.CLOSED) {
            String msg = "The channel is in closed state and cannot send";
            LOG.warn(msg);
            return;
        }

        try {
            outQueue.put(messageContext);
        } catch (InterruptedException e) {
            LOG.error("Failed to put the message to queue", e);
        }
    }

    public void publish(byte []message, Map<String, Object> properties) {
        MessageContext messageContext = new MessageContext(sensorID, message, properties);
        checkOpen();

        if (state == State.CLOSED) {
            String msg = "The channel is in closed state and cannot send";
            LOG.warn(msg);
            return;
        }

        try {
            outQueue.put(messageContext);
        } catch (InterruptedException e) {
            LOG.error("Failed to put the message to queue", e);
        }
    }

    public void close() {
        this.state = State.CLOSED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Channel channel = (Channel) o;

        if (name != null ? !name.equals(channel.name) : channel.name != null) return false;
        if (sensorID != null ? !sensorID.equals(channel.sensorID) : channel.sensorID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (sensorID != null ? sensorID.hashCode() : 0);
        return result;
    }
}
