package cgl.iotcloud.core.sensor;

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
public class SensorDetails {
    private final SensorId sensorId;

    private Map<String, List<ChannelDetails>> channels = new HashMap<String, List<ChannelDetails>>();

    private Object metadata;

    private SensorState state = SensorState.ACTIVATE;

    public SensorDetails(SensorId sensorId) {
        this.sensorId = sensorId;
    }

    public void addChannel(String transport, ChannelDetails channelDetails) {
        List<ChannelDetails> channelsForTransport = channels.get(transport);
        if (channelsForTransport ==  null) {
            channelsForTransport = new ArrayList<ChannelDetails>();
            channels.put(transport, channelsForTransport);
        }

        channelsForTransport.add(channelDetails);
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    public Map<String, List<ChannelDetails>> getChannels() {
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

        SensorDetails that = (SensorDetails) o;

        if (sensorId != null ? !sensorId.equals(that.sensorId) : that.sensorId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sensorId != null ? sensorId.hashCode() : 0;
    }
}
