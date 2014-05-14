package cgl.iotcloud.transport.kestrel;

public class KestrelDestination {
    private String host;

    private int port;

    private String queue;

    public KestrelDestination(String host, int port, String queue) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getQueue() {
        return queue;
    }
}
