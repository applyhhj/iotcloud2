package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;

import java.util.List;

public class SensorStopEvent extends SensorEvent{

    public SensorStopEvent(SensorId id, List<String> sites) {
        super(id, sites);
    }

    public SensorStopEvent(SensorId id) {
        super(id);
    }
}
