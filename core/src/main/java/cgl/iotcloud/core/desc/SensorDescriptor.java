package cgl.iotcloud.core.desc;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SensorState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains static information about the sensor. This information can be
 * saved in persistant store.
 */
public class SensorDescriptor {
    private final SensorId sensorId;

    private Map<String, List<ChannelDescriptor>> channels = new HashMap<String, List<ChannelDescriptor>>();

    private Object metadata;

    private SensorState state = SensorState.ACTIVATE;

    public SensorDescriptor(SensorId sensorId) {
        this.sensorId = sensorId;
    }

    public void addChannel(String transport, ChannelDescriptor channelDescriptor) {
        List<ChannelDescriptor> channelsForTransport = channels.get(transport);
        if (channelsForTransport ==  null) {
            channelsForTransport = new ArrayList<ChannelDescriptor>();
            channels.put(transport, channelsForTransport);
        }

        channelsForTransport.add(channelDescriptor);
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    public Map<String, List<ChannelDescriptor>> getChannels() {
        return channels;
    }

    public SensorId getSensorId() {
        return sensorId;
    }

    public Object getMetadata() {
        return metadata;
    }

    public SensorState getState() {
        return state;
    }

    public void setState(SensorState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorDescriptor that = (SensorDescriptor) o;

        if (sensorId != null ? !sensorId.equals(that.sensorId) : that.sensorId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sensorId != null ? sensorId.hashCode() : 0;
    }
}
