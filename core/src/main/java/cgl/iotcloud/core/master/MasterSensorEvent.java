package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SensorState;

public class MasterSensorEvent {
    private SensorState state;

    private SensorId sensorId;

    public MasterSensorEvent(SensorId sensorId, SensorState state) {
        this.state = state;
        this.sensorId = sensorId;
    }

    public SensorState getState() {
        return state;
    }

    public SensorId getSensorId() {
        return sensorId;
    }
}
