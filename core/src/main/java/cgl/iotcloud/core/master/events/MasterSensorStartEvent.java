package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;

import java.util.List;

public class MasterSensorStartEvent extends MSensorEvent {
    public MasterSensorStartEvent(SensorId id, List<String> sites) {
        super(id, sites);
    }

    public MasterSensorStartEvent(SensorId id) {
        super(id);
    }
}
