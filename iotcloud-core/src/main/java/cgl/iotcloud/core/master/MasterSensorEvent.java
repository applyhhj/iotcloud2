package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import cgl.iotcloud.core.sensorsite.SensorEventState;

import java.util.ArrayList;
import java.util.List;

public class MasterSensorEvent {
    private SensorEventState state;

    private List<String> sensorSites = new ArrayList<String>();

    private SensorId sensorId;

    private SensorDeployDescriptor deployDescriptor;

    public MasterSensorEvent(SensorId sensorId, SensorEventState state, List<String> sites) {
        this.state = state;
        this.sensorId = sensorId;
        this.sensorSites.addAll(sites);
    }

    public MasterSensorEvent(SensorDeployDescriptor deployDescriptor, SensorEventState state, List<String> sites) {
        this.deployDescriptor = deployDescriptor;
        this.state = state;
        this.sensorSites.addAll(sites);
    }

    public SensorEventState getState() {
        return state;
    }

    public SensorId getSensorId() {
        return sensorId;
    }

    public SensorDeployDescriptor getDeployDescriptor() {
        return deployDescriptor;
    }

    public MasterSensorEvent(SensorEventState state) {
        this.state = state;
    }

    public List<String> getSensorSites() {
        return sensorSites;
    }
}
