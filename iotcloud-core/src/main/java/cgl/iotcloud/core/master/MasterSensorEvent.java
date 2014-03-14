package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SensorEventState;

public class MasterSensorEvent {
    private SensorEventState state;

    private SensorId sensorId;

    public MasterSensorEvent(SensorId sensorId, SensorEventState state) {
        this.state = state;
        this.sensorId = sensorId;
    }

    public SensorEventState getState() {
        return state;
    }

    public SensorId getSensorId() {
        return sensorId;
    }
}
