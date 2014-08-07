package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.api.thrift.TSensor;
import cgl.iotcloud.core.sensorsite.SensorState;

/**
 * These are events sent by the sensor sites about the sensor changes
 */
public class MSensorSiteEvent extends MSensorEvent {
    private String site;

    private TSensor sensor;

    public MSensorSiteEvent(String id, SensorState state, String site) {
        super(id, state);
        this.site = site;
    }

    public String getSite() {
        return site;
    }

    public TSensor getSensor() {
        return sensor;
    }

    public void setSensor(TSensor sensor) {
        this.sensor = sensor;
    }
}
