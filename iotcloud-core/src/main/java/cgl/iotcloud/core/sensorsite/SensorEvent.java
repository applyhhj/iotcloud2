package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.SensorId;

public class SensorEvent {
    public enum State {
        DEPLOY,
        DEACTIVATE,
        ACTIVATE
    }

    private State state;

    private SensorId sensorId;

    private SensorDeployDescriptor deployDescriptor;

    public SensorEvent(SensorId sensorId, State state) {
        this.state = state;
        this.sensorId = sensorId;
    }

    public SensorEvent(SensorDeployDescriptor deployDescriptor, State state) {
        this.deployDescriptor = deployDescriptor;
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public SensorId getSensorId() {
        return sensorId;
    }

    public SensorDeployDescriptor getDeployDescriptor() {
        return deployDescriptor;
    }
}
