package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;

import java.util.ArrayList;
import java.util.List;

public class SensorEvent {
    protected List<String> sites = new ArrayList<String>();

    protected SensorId id;

    public SensorEvent(SensorId id, List<String> sites) {
        this.id = id;
        this.sites = sites;
    }

    public SensorEvent(SensorId id) {
        this.id = id;
    }

    public SensorId getId() {
        return id;
    }

    public List<String> getSites() {
        return sites;
    }
}
