package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensor.SensorDescriptor;
import cgl.iotcloud.core.sensor.SensorDetails;
import cgl.iotcloud.core.transport.Transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds information about an active site
 */
public class SiteContext {
    // a unique id for the site
    private String siteId;

    // the transports available for this site
    private Map<String, Transport> transports = new HashMap<String, Transport>();

    // static information about the deployed sensors
    private Map<SensorId, SensorDescriptor> sensors = new HashMap<SensorId, SensorDescriptor>();

    // runtime information about the deployed sensors
    private Map<SensorId, SensorContext> sensorContexts = new HashMap<SensorId, SensorContext>();

    // information about the deployed sensors
    private Map<SensorId, SensorDetails> sensorDescriptions = new HashMap<SensorId, SensorDetails>();

    public SiteContext(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void addSensor(SensorContext context, ISensor sensor) {
        SensorDescriptor details = new SensorDescriptor(context, sensor);
        sensors.put(context.getId(), details);

        sensorContexts.put(context.getId(), context);
    }

    public SensorDescriptor removeSensor(SensorId sensorId) {
        return sensors.remove(sensorId);
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

    public SensorContext getSensor(SensorId id) {
        return sensorContexts.get(id);
    }

    public SensorDescriptor getSensorDescriptor(SensorId id) {
        return sensors.get(id);
    }

    public Map<String, Transport> getTransports() {
        return transports;
    }
}
