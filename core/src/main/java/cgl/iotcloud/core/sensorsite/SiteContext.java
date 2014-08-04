package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.SensorContext;
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
    private Map<String, SensorInstance> sensors = new HashMap<String, SensorInstance>();

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
        sensors.put(context.getName(), details);
    }

    public SensorInstance removeSensor(String sensorId) {
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

    public SensorInstance getSensor(String id) {
        return sensors.get(id);
    }

    public SensorInstance getSensorDescriptor(String id) {
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
