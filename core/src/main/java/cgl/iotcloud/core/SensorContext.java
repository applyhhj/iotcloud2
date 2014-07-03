package cgl.iotcloud.core;

import cgl.iotcloud.core.transport.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorContext {
    private static Logger LOG = LoggerFactory.getLogger(SensorContext.class);

    // the list of channels registered in this sensor
    private Map<String, List<Channel>> channels = new HashMap<String, List<Channel>>();

    // the sensor id
    private final SensorId id;

    // the metadata about the sensor
    private Object metadata;

    // a generic property holder to pass information from the
    // configuration to sensor
    private Map properties = new HashMap();

    public SensorContext(SensorId id) {
        if (id == null) {
            throw new IllegalArgumentException("A sensor should have an id");
        }

        this.id = id;
    }

    public void addChannel(String transport, Channel channel) {
        List<Channel> channelsForTransport = channels.get(transport);
        if (channelsForTransport ==  null) {
            channelsForTransport = new ArrayList<Channel>();
            channels.put(transport, channelsForTransport);
        }

        channelsForTransport.add(channel);
    }

    public Channel getChannel(String transport, String channel) {
        if (channels.containsKey(transport)) {
            List<Channel> cs = channels.get(transport);
            for (Channel c : cs) {
                if (c.getName().equals(channel)) {
                    return c;
                }
            }
        }
        return null;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    public void addProperty(Object key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(Object key) {
        return properties.get(key);
    }

    public Map<String, List<Channel>> getChannels() {
        return channels;
    }

    public Map getProperties() {
        return properties;
    }

    public Object getMetadata() {
        return metadata;
    }

    public SensorId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorContext that = (SensorContext) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
