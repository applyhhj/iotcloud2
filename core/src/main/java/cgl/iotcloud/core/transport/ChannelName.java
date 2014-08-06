package cgl.iotcloud.core.transport;

public class ChannelName {
    private String sensorName;

    private String channelName;

    public ChannelName(String sensorName, String channelName) {
        this.sensorName = sensorName;
        this.channelName = channelName;
    }

    public String getSensorName() {
        return sensorName;
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
        if (sensorName != null ? !sensorName.equals(that.sensorName) : that.sensorName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sensorName != null ? sensorName.hashCode() : 0;
        result = 31 * result + (channelName != null ? channelName.hashCode() : 0);
        return result;
    }
}
