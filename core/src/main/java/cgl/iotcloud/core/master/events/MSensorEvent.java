package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SensorState;

public class MSensorEvent {
    private SensorState state;

    protected SensorId id;

    public MSensorEvent(SensorId id, SensorState state) {
        this.id = id;
        this.state = state;
    }

    public SensorId getId() {
        return id;
    }

    public SensorState getState() {
        return state;
    }
}
