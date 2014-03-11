package cgl.iotcloud.core.master;

/**
 * Captures the details about a registered sensor site
 */
public class SensorSiteDetails {
    // a unique string identifying the sensor site
    private String id;

    // the listening port of the heartbeat task
    private int port;

    // the host name
    private String host;

    // metadata about the sensor site
    private Object metadata;

    public SensorSiteDetails(String id, int port, String host) {
        this.id = id;
        this.port = port;
        this.host = host;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public Object getMetadata() {
        return metadata;
    }
}
