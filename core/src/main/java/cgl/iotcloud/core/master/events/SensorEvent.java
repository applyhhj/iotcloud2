package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;

public class SensorEvent {
    protected SensorId id;

    public SensorEvent(SensorId id) {
        this.id = id;
    }

    public SensorId getId() {
        return id;
    }
}
