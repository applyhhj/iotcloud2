package cgl.iotcloud.core.sensorsite.events;

import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import cgl.iotcloud.core.sensorsite.SensorState;

public class SensorEvent {

    private SensorState state;

    private String sensorId;

    private SensorDeployDescriptor deployDescriptor;

    public SensorEvent(String sensorId, SensorState state) {
        this.state = state;
        this.sensorId = sensorId;
    }

    public SensorEvent(SensorDeployDescriptor deployDescriptor, SensorState state) {
        this.deployDescriptor = deployDescriptor;
        this.state = state;
    }

    public SensorState getState() {
        return state;
    }

    public String getSensorId() {
        return sensorId;
    }

    public SensorDeployDescriptor getDeployDescriptor() {
        return deployDescriptor;
    }
}
