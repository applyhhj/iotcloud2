package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.transport.Transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteContext {
    private Map<String, Transport> transports = new HashMap<String, Transport>();

    private Map<SensorContext, ISensor> sensors = new HashMap<SensorContext, ISensor>();

    public void init() {
        // at the end we are going to start the transports
        for (Map.Entry<String, Transport> e : transports.entrySet()) {
            Transport t = e.getValue();
            t.start();
        }
    }

    public void addSensor(SensorContext context, ISensor sensor) {
        sensors.put(context, sensor);
    }

    public void addTransport(String tName, Transport t) {
        transports.put(tName, t);
    }

    public Transport getTransport(String tName) {
        return transports.get(tName);
    }

    public List<SensorContext> getRegisteredSensors() {
        return new ArrayList<SensorContext>(sensors.keySet());
    }
}
