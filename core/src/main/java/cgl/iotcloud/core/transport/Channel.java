package cgl.iotcloud.core.transport;

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

    private BlockingQueue outQueue;

    private List<MessageProcessor> messageProcessors = new ArrayList<MessageProcessor>();

    private Map properties = new HashMap();

    private Direction direction;

    private String name;

    private String sensorID;

    private String group;

    private MessageConverter converter;

    private boolean run = true;

    public Channel(String name, Direction direction,
                   BlockingQueue inQueue, BlockingQueue outQueue, MessageConverter converter) {
        this.name = name;
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.direction = direction;
        this.converter = converter;
    }

    public Channel(String name, String group, String sensorID, Direction direction,
                   BlockingQueue userQueue, MessageConverter converter) {
        this.name = name;
        if (direction == Direction.OUT) {
            this.inQueue = userQueue;
        } else if (direction == Direction.IN) {
            this.outQueue = userQueue;
        }
        this.direction = direction;
        this.converter = converter;
        this.group = group;
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

    public String getSensorID() {
        return sensorID;
    }

    public void open() {
        run = true;
        Thread t = new Thread(new Worker());
        t.start();
    }

    public MessageConverter getConverter() {
        return converter;
    }

    public void setTransportQueue(BlockingQueue transportQueue) {
        if (direction == Direction.OUT) {
            this.outQueue = transportQueue;
        } else if (direction == Direction.IN) {
            this.inQueue = transportQueue;
        }
    }

    @SuppressWarnings("unchecked")
    public void addProperties(Map properties) {
        this.properties.putAll(properties);
    }

    public BlockingQueue getUserQueue() {
        if (direction == Direction.OUT) {
            return this.inQueue;
        } else if (direction == Direction.IN) {
            return this.outQueue;
        }
        return null;
    }

    public BlockingQueue getTransportQueue() {
        if (direction == Direction.OUT) {
            return this.outQueue;
        } else if (direction == Direction.IN) {
            return this.inQueue;
        }

        return null;
    }

    public String getGroup() {
        return group;
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            int errorCount = 0;
            while (run) {
                try {
                    try {
                        Object input = inQueue.take();
                        Object out = null;
                        if (!messageProcessors.isEmpty()) {
                            for (MessageProcessor mp : messageProcessors) {
                                out = mp.process(input);
                                input = out;
                            }
                        } else {
                            out = input;
                        }
                        outQueue.put(out);
                    } catch (InterruptedException e) {
                        LOG.error("Exception occurred in the worker listening for consumer changes", e);
                    }
                } catch (Throwable t) {
                    errorCount++;
                    if (errorCount <= 3) {
                        LOG.error("Error occurred " + errorCount + " times.. trying to continue the worker", t);
                    } else {
                        LOG.error("Error occurred " + errorCount + " times.. terminating the worker", t);
                        run = false;
                    }
                }
            }
        }
    }

    public void close() {
        run = false;
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
