package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;

import java.util.ArrayList;
import java.util.List;

public class SensorDeployEvent extends SensorEvent {
    private List<String> sites = new ArrayList<String>();

    private SensorDeployDescriptor deployDescriptor;

    public SensorDeployEvent(SensorId id, SensorDeployDescriptor deployDescriptor) {
        super(id);
        this.deployDescriptor = deployDescriptor;
    }

    public SensorDeployEvent(SensorId id, List<String> deploySites, SensorDeployDescriptor deployDescriptor) {
        super(id);
        sites.addAll(deploySites);
        this.deployDescriptor = deployDescriptor;
    }

    public List<String> getSites() {
        return sites;
    }

    public SensorDeployDescriptor getDeployDescriptor() {
        return deployDescriptor;
    }
}
