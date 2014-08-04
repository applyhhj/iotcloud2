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

    // the sensor name, this is used to identify sensor logically
    private final String name;

    // the metadata about the sensor
    private Object metadata;

    // a generic property holder to pass information from the
    // configuration to sensor
    private Map properties = new HashMap();

    // a unique id to the sensor, this is used to identify the sensor uniquely
    private String sensorID;

    public SensorContext(String name) {
        if (name == null) {
            throw new IllegalArgumentException("A sensor should have an id");
        }

        this.name = name;
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

    public String getSensorID() {
        return sensorID;
    }

    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
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

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorContext that = (SensorContext) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
