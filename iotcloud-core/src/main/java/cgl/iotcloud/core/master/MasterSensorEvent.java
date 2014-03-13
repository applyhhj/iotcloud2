package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SensorEventState;

import java.util.ArrayList;
import java.util.List;

public class MasterSensorEvent {
    private SensorEventState state;

    private SensorId sensorId;

    // the sites this event should affect
    private List<String> sites = new ArrayList<String>();

    public MasterSensorEvent(SensorId sensorId, SensorEventState state) {
        this.state = state;
        this.sensorId = sensorId;
    }

    public MasterSensorEvent(SensorEventState state, List<String> sites) {
        this.state = state;
        this.sites = sites;
    }

    public SensorEventState getState() {
        return state;
    }

    public SensorId getSensorId() {
        return sensorId;
    }

    public List<String> getSites() {
        return sites;
    }
}
