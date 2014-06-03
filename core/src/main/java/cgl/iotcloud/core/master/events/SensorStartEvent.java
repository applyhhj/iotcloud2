package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;

import java.util.List;

public class SensorStartEvent extends SensorEvent {
    public SensorStartEvent(SensorId id, List<String> sites) {
        super(id, sites);
    }

    public SensorStartEvent(SensorId id) {
        super(id);
    }
}
