package cgl.iotcloud.core.transport;

public class ChannelGroupName {
    private String channelName;

    private String sensorGroup;

    public ChannelGroupName(String channelName, String sensorGroup) {
        this.channelName = channelName;
        this.sensorGroup = sensorGroup;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getSensorGroup() {
        return sensorGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChannelGroupName that = (ChannelGroupName) o;

        if (channelName != null ? !channelName.equals(that.channelName) : that.channelName != null) return false;
        if (sensorGroup != null ? !sensorGroup.equals(that.sensorGroup) : that.sensorGroup != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = channelName != null ? channelName.hashCode() : 0;
        result = 31 * result + (sensorGroup != null ? sensorGroup.hashCode() : 0);
        return result;
    }
}
