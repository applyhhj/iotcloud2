package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;

import java.util.List;

public class MasterSensorStopEvent extends MSensorEvent {

    public MasterSensorStopEvent(SensorId id, List<String> sites) {
        super(id, sites);
    }

    public MasterSensorStopEvent(SensorId id) {
        super(id);
    }
}
