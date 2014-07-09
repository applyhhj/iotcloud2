package cgl.iotcloud.core.msg;

import java.util.HashMap;
import java.util.Map;

public class MessageContext {
    private String sensorId;

    private Map<String, Object> properties = new HashMap<String, Object>();

    private byte[] body;

    public MessageContext(String sensorId, byte[] body) {
        this.sensorId = sensorId;
        this.body = body;
    }

    public MessageContext(String sensorId, byte[] body, Map<String, Object> properties) {
        if (sensorId == null) {
            throw new IllegalArgumentException("SensorID should be present");
        }

        if (body == null) {
            throw new IllegalArgumentException("The body should be present");
        }

        this.sensorId = sensorId;
        if (properties != null) {
            this.properties = properties;
        }
        this.body = body;
    }

    public String getSensorId() {
        return sensorId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public byte[] getBody() {
        return body;
    }
}
