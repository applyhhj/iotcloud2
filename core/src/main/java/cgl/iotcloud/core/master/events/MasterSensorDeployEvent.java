package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;

import java.util.List;

public class MasterSensorDeployEvent extends MSensorEvent {
    private SensorDeployDescriptor deployDescriptor;

    public MasterSensorDeployEvent(SensorDeployDescriptor deployDescriptor) {
        super(null);
        this.deployDescriptor = deployDescriptor;
    }

    public MasterSensorDeployEvent(List<String> deploySites, SensorDeployDescriptor deployDescriptor) {
        super(null, deploySites);
        this.deployDescriptor = deployDescriptor;
    }

    public SensorDeployDescriptor getDeployDescriptor() {
        return deployDescriptor;
    }
}
