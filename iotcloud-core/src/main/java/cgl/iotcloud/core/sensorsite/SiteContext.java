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

    private Map<SensorId, SiteSensorDescriptor> sensors = new HashMap<SensorId, SiteSensorDescriptor>();

    public void init() {
        // at the end we are going to start the transports
        for (Map.Entry<String, Transport> e : transports.entrySet()) {
            Transport t = e.getValue();
            t.start();
        }
    }

    public void addSensor(SensorContext context, ISensor sensor) {
        SiteSensorDescriptor details = new SiteSensorDescriptor(context, sensor);
        sensors.put(context.getId(), details);
    }

    public void addTransport(String tName, Transport t) {
        transports.put(tName, t);
    }

    public Transport getTransport(String tName) {
        return transports.get(tName);
    }

    public List<SiteSensorDescriptor> getRegisteredSensors() {
        return new ArrayList<SiteSensorDescriptor>(sensors.values());
    }

    public SiteSensorDescriptor getSensor(SensorId id) {
        return sensors.get(id);
    }
}
