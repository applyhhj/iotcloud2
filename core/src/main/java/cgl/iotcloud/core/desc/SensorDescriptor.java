package cgl.iotcloud.core.desc;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SensorState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains static information about the sensor. This information can be
 * saved in persistant store.
 */
public class SensorDescriptor implements Serializable {
    private SensorId sensorId;

    private String siteId;

    private Map<String, List<ChannelDescriptor>> channels = new HashMap<String, List<ChannelDescriptor>>();

    private Object metadata;

    private SensorState state = SensorState.ACTIVATE;

    public SensorDescriptor() {
    }

    public SensorDescriptor(String siteId, SensorId sensorId) {
        this.sensorId = sensorId;
        this.siteId = siteId;
    }

    public void addChannel(String transport, ChannelDescriptor channelDescriptor) {
        List<ChannelDescriptor> channelsForTransport = channels.get(transport);
        if (channelsForTransport ==  null) {
            channelsForTransport = new ArrayList<ChannelDescriptor>();
            channels.put(transport, channelsForTransport);
        }

        channelsForTransport.add(channelDescriptor);
    }

    public void setSensorId(SensorId sensorId) {
        this.sensorId = sensorId;
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

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public void setChannels(Map<String, List<ChannelDescriptor>> channels) {
        this.channels = channels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorDescriptor that = (SensorDescriptor) o;

        if (sensorId != null ? !sensorId.equals(that.sensorId) : that.sensorId != null) return false;
        if (siteId != null ? !siteId.equals(that.siteId) : that.siteId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sensorId != null ? sensorId.hashCode() : 0;
        result = 31 * result + (siteId != null ? siteId.hashCode() : 0);
        return result;
    }
}
