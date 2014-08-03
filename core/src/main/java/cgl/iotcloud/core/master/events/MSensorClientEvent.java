package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.TSensorDeployDescriptor;
import cgl.iotcloud.core.sensorsite.SensorState;

import java.util.ArrayList;
import java.util.List;

/**
 * Sensor events from the client side
 */
public class MSensorClientEvent extends MSensorEvent {
    private List<String> sites = new ArrayList<String>();

    private TSensorDeployDescriptor sensorDeployDescriptor;

    public MSensorClientEvent(SensorId id, SensorState state) {
        super(id, state);
    }

    public MSensorClientEvent(SensorId id, SensorState state, List<String> sites) {
        super(id, state);
        this.sites.addAll(sites);
    }

    public MSensorClientEvent(SensorState state, TSensorDeployDescriptor sensorDeployDescriptor, List<String> sites) {
        super(null, state);
        this.sensorDeployDescriptor = sensorDeployDescriptor;
        this.sites.addAll(sites);
    }

    public List<String> getSites() {
        return sites;
    }

    public TSensorDeployDescriptor getSensorDeployDescriptor() {
        return sensorDeployDescriptor;
    }
}
