package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;

import java.util.List;

public class SensorDeployEvent extends SensorEvent {
    private SensorDeployDescriptor deployDescriptor;

    public SensorDeployEvent(SensorDeployDescriptor deployDescriptor) {
        super(null);
        this.deployDescriptor = deployDescriptor;
    }

    public SensorDeployEvent(List<String> deploySites, SensorDeployDescriptor deployDescriptor) {
        super(null, deploySites);
        this.deployDescriptor = deployDescriptor;
    }

    public SensorDeployDescriptor getDeployDescriptor() {
        return deployDescriptor;
    }
}
