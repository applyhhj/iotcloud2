package cgl.iotcloud.core.msg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AbstractMessage implements Serializable {
    protected Map<Object, Object> properties = new HashMap<Object, Object>();

    public Map<Object, Object> getProperties() {
        return properties;
    }

    public void addProperty(Object key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(Object key) {
        return properties.get(key);
    }
}
