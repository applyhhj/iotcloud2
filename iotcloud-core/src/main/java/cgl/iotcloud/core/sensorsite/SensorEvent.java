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

    private SensorDeployDetails deployDetails;

    public SensorEvent(SensorId sensorId, State state) {
        this.state = state;
        this.sensorId = sensorId;
    }

    public SensorEvent(SensorDeployDetails deployDetails, State state) {
        this.deployDetails = deployDetails;
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public SensorId getSensorId() {
        return sensorId;
    }

    public SensorDeployDetails getDeployDetails() {
        return deployDetails;
    }
}
