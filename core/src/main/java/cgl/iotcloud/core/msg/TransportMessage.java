package cgl.iotcloud.core.msg;

import java.util.HashMap;
import java.util.Map;

public class TransportMessage {
    private String sensorId;

    private Map<String, String> properties = new HashMap<String, String>();

    private byte[] body;

    public TransportMessage(String sensorId, byte []body) {
        this.sensorId = sensorId;
    }

    public TransportMessage(String sensorId, byte[] body, Map<String, String> properties) {
        this.sensorId = sensorId;
        this.properties = properties;
    }

    public String getSensorId() {
        return sensorId;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
