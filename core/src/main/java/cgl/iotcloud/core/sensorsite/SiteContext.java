package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.desc.SensorDescriptor;
import cgl.iotcloud.core.sensor.SensorInstance;
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
    private Map<SensorId, SensorInstance> sensors = new HashMap<SensorId, SensorInstance>();

    // runtime information about the deployed sensors
    private Map<SensorId, SensorContext> sensorContexts = new HashMap<SensorId, SensorContext>();

    // information about the deployed sensors
    private Map<SensorId, SensorDescriptor> sensorDescriptions = new HashMap<SensorId, SensorDescriptor>();

    private Map conf;

    public SiteContext(String siteId, Map conf) {
        this.siteId = siteId;
        this.conf = conf;
    }

    public String getSiteId() {
        return siteId;
    }

    public void addSensor(SensorContext context, ISensor sensor) {
        SensorInstance details = new SensorInstance(context, sensor);
        sensors.put(context.getId(), details);

        sensorContexts.put(context.getId(), context);
    }

    public SensorInstance removeSensor(SensorId sensorId) {
        return sensors.remove(sensorId);
    }

    public void addTransport(String tName, Transport t) {
        transports.put(tName, t);
    }

    public Transport getTransport(String tName) {
        return transports.get(tName);
    }

    public List<SensorInstance> getRegisteredSensors() {
        return new ArrayList<SensorInstance>(sensors.values());
    }

    public SensorContext getSensor(SensorId id) {
        return sensorContexts.get(id);
    }

    public SensorInstance getSensorDescriptor(SensorId id) {
        return sensors.get(id);
    }

    public Map<String, Transport> getTransports() {
        return transports;
    }

    public String getMasterHost() {
        return Configuration.getMasterHost(conf);
    }

    public int getMasterPort() {
        return Configuration.getMasterServerPort(conf);
    }
}
