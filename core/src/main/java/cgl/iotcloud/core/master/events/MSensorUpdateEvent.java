package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.master.SensorDetails;
import cgl.iotcloud.core.sensorsite.SensorEventState;

import java.util.List;

/**
 * These are events sent by the sensor sites about the sensor changes
 */
public class MSensorUpdateEvent extends MSensorEvent {
    private SensorEventState state;

    private SensorDetails sensorDetails;

    public MSensorUpdateEvent(SensorId id, List<String> sites, SensorEventState state) {
        super(id, sites);
        this.state = state;
    }

    public SensorEventState getState() {
        return state;
    }

    public void setSensorDetails(SensorDetails sensorDetails) {
        this.sensorDetails = sensorDetails;
    }

    public SensorDetails getSensorDetails() {
        return sensorDetails;
    }
}
