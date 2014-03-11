package cgl.iotcloud.core;

import cgl.iotcloud.core.transport.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorContext {
    private Map<String, List<Channel>> channels = new HashMap<String, List<Channel>>();

    private final SensorId id;

    private Object metadata;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorContext that = (SensorContext) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
