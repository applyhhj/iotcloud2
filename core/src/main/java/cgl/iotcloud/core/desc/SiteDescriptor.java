//package cgl.iotcloud.core.desc;
//
//import java.io.Serializable;
//
///**
// * Captures the details about a registered sensor site
// */
//public class SiteDescriptor implements Serializable {
//    // a unique string identifying the sensor site
//    private String id;
//
//    // the listening port of the heartbeat task
//    private int port;
//
//    // the host name
//    private String host;
//
//    // metadata about the sensor site
//    private Object metadata;
//
//    // the deactivated time
//    private long deactivatedTime;
//
//    public SiteDescriptor() {
//    }
//
//    public SiteDescriptor(String id, int port, String host) {
//        this.id = id;
//        this.port = port;
//        this.host = host;
//    }
//
//    public void setMetadata(Object metadata) {
//        this.metadata = metadata;
//    }
//
//    public void setDeactivatedTime(long deactivatedTime) {
//        this.deactivatedTime = deactivatedTime;
//    }
//
//    public long getDeactivatedTime() {
//        return deactivatedTime;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public int getPort() {
//        return port;
//    }
//
//    public String getHost() {
//        return host;
//    }
//
//    public Object getMetadata() {
//        return metadata;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public void setPort(int port) {
//        this.port = port;
//    }
//
//    public void setHost(String host) {
//        this.host = host;
//    }
//}
