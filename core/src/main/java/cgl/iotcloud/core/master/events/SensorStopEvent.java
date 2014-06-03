package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;

import java.util.ArrayList;
import java.util.List;

public class SensorStopEvent extends SensorEvent{
    private List<String> sites = new ArrayList<String>();

    public SensorStopEvent(SensorId id) {
        super(id);
    }

    public SensorStopEvent(SensorId id, List<String> sites) {
        super(id);
        this.sites.addAll(sites);
    }

    public List<String> getSites() {
        return sites;
    }
}
