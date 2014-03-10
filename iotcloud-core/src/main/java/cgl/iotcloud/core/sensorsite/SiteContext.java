package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.transport.Transport;
import com.sun.accessibility.internal.resources.accessibility_en;

import java.util.HashMap;
import java.util.Map;

public class SiteContext {
    private Map<String, Transport> transports = new HashMap<String, Transport>();

    public void init(Map conf) {


        // at the end we are going to start the transports
        for (Map.Entry<String, Transport> e : transports.entrySet()) {
            Transport t = e.getValue();
            t.start();
        }
    }

    public void addTransport(String tName, Transport t) {
        transports.put(tName, t);
    }

    public Transport getTransport(String tName) {
        return transports.get(tName);
    }
}
