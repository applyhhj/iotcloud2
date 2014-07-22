package cgl.iotcloud.core.desc;

import java.io.Serializable;

public class TransportDescriptor implements Serializable {
    private String name;

    private String url;

    public TransportDescriptor() {
    }

    public TransportDescriptor(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
