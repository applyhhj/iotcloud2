package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensor.SensorDetails;
import cgl.iotcloud.core.sensorsite.SensorState;

/**
 * These are events sent by the sensor sites about the sensor changes
 */
public class MSensorSiteEvent extends MSensorEvent {
    private SensorDetails sensorDetails;

    private String site;

    public MSensorSiteEvent(SensorId id, SensorState state, String site) {
        super(id, state);
        this.site = site;
    }

    public String getSite() {
        return site;
    }

    public void setSensorDetails(SensorDetails sensorDetails) {
        this.sensorDetails = sensorDetails;
    }

    public SensorDetails getSensorDetails() {
        return sensorDetails;
    }
}
