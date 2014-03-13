package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.SensorId;

public class SensorEvent {

    private SensorEventState state;

    private SensorId sensorId;

    private SensorDeployDescriptor deployDescriptor;

    public SensorEvent(SensorId sensorId, SensorEventState state) {
        this.state = state;
        this.sensorId = sensorId;
    }

    public SensorEvent(SensorDeployDescriptor deployDescriptor, SensorEventState state) {
        this.deployDescriptor = deployDescriptor;
        this.state = state;
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
}
