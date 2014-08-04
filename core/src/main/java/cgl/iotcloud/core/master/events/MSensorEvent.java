package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.sensorsite.SensorState;

/**
 * Sensor events from
 */
public class MSensorEvent {
    private SensorState state;

    protected String id;

    public MSensorEvent(String id, SensorState state) {
        this.id = id;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public SensorState getState() {
        return state;
    }
}
