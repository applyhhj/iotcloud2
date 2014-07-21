package cgl.iotcloud.core.desc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SensorDescriptor implements Serializable {
    private String name;

    private String group;

    private List<ChannelDescriptor> channels = new ArrayList<ChannelDescriptor>();

    public SensorDescriptor(String name, String group, List<ChannelDescriptor> channels) {
        this.name = name;
        this.group = group;
        this.channels = channels;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public List<ChannelDescriptor> getChannels() {
        return channels;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setChannels(List<ChannelDescriptor> channels) {
        this.channels = channels;
    }
}
