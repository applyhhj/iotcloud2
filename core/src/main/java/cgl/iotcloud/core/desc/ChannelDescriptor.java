package cgl.iotcloud.core.desc;

import java.io.Serializable;

public class ChannelDescriptor implements Serializable {
    private String name;

    private TransportDescriptor transport;

    public ChannelDescriptor() {
    }

    public ChannelDescriptor(String name, TransportDescriptor transport) {
        this.name = name;
        this.transport = transport;
    }

    public String getName() {
        return name;
    }

    public TransportDescriptor getTransport() {
        return transport;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTransport(TransportDescriptor transport) {
        this.transport = transport;
    }
}
