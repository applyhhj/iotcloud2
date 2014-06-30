package cgl.iotcloud.core.transport;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String name;

    private List<Channel> channels = new ArrayList<Channel>();

    public Group(String name) {
        this.name = name;
    }

    public void addChannel(Channel channel) {

    }
}
