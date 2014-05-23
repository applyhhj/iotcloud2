package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.SensorId;

public class ChannelName {
    private SensorId id;

    private String channelName;

    public ChannelName(SensorId id, String channelName) {
        this.id = id;
        this.channelName = channelName;
    }

    public SensorId getId() {
        return id;
    }

    public String getChannelName() {
        return channelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChannelName that = (ChannelName) o;

        if (channelName != null ? !channelName.equals(that.channelName) : that.channelName != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (channelName != null ? channelName.hashCode() : 0);
        return result;
    }
}
