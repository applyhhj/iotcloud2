package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.transport.Transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteContext {
    // a unique id for the site
    private String siteId;

    private Map<String, Transport> transports = new HashMap<String, Transport>();

    private Map<SensorId, SensorDescriptor> sensors = new HashMap<SensorId, SensorDescriptor>();

    public void init() {
        // at the end we are going to start the transports
        for (Map.Entry<String, Transport> e : transports.entrySet()) {
            Transport t = e.getValue();
            t.start();
        }
    }

    public void addSensor(SensorContext context, ISensor sensor) {
        SensorDescriptor details = new SensorDescriptor(context, sensor);
        sensors.put(context.getId(), details);
    }

    public void addTransport(String tName, Transport t) {
        transports.put(tName, t);
    }

    public Transport getTransport(String tName) {
        return transports.get(tName);
    }

    public List<SensorDescriptor> getRegisteredSensors() {
        return new ArrayList<SensorDescriptor>(sensors.values());
    }

    public SensorDescriptor getSensor(SensorId id) {
        return sensors.get(id);
    }

    public Map<String, Transport> getTransports() {
        return transports;
    }
}
