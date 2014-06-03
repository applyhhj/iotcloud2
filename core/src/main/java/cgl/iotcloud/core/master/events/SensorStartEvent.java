package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;

import java.util.ArrayList;
import java.util.List;

public class SensorStartEvent extends SensorEvent {
    private List<String> sites = new ArrayList<String>();

    public SensorStartEvent(SensorId id) {
        super(id);
    }

    public SensorStartEvent(SensorId id, List<String> sites) {
        super(id);
        this.sites.addAll(sites);
    }

    public List<String> getSites() {
        return sites;
    }
}
