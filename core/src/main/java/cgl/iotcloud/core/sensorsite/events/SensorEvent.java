package cgl.iotcloud.core.sensorsite.events;

import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import cgl.iotcloud.core.sensorsite.SensorState;

public class SensorEvent {

    private SensorState state;

    private String sensorName;

    private SensorDeployDescriptor deployDescriptor;

    public SensorEvent(String sensorName, SensorState state) {
        this.state = state;
        this.sensorName = sensorName;
    }

    public SensorEvent(SensorDeployDescriptor deployDescriptor, SensorState state) {
        this.deployDescriptor = deployDescriptor;
        this.state = state;
    }

    public SensorState getState() {
        return state;
    }

    public String getSensorName() {
        return sensorName;
    }

    public SensorDeployDescriptor getDeployDescriptor() {
        return deployDescriptor;
    }
}
