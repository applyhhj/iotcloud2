package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.desc.SensorDescriptor;
import cgl.iotcloud.core.sensorsite.SensorState;

/**
 * These are events sent by the sensor sites about the sensor changes
 */
public class MSensorSiteEvent extends MSensorEvent {
    private SensorDescriptor sensorDescriptor;

    private String site;

    public MSensorSiteEvent(SensorId id, SensorState state, String site) {
        super(id, state);
        this.site = site;
    }

    public String getSite() {
        return site;
    }

    public void setSensorDescriptor(SensorDescriptor sensorDescriptor) {
        this.sensorDescriptor = sensorDescriptor;
    }

    public SensorDescriptor getSensorDescriptor() {
        return sensorDescriptor;
    }
}
