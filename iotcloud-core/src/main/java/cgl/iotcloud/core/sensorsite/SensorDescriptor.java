package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.SensorContext;

public class SensorDescriptor {
    private SensorContext sensorContext;

    private ISensor sensor;

    private SensorDeployDetails deployDetails;

    public SensorDescriptor(SensorContext sensorContext, ISensor sensor) {
        this.sensorContext = sensorContext;
        this.sensor = sensor;
    }

    public void setSensorContext(SensorContext sensorContext) {
        this.sensorContext = sensorContext;
    }

    public void setSensor(ISensor sensor) {
        this.sensor = sensor;
    }

    public void setDeployDetails(SensorDeployDetails deployDetails) {
        this.deployDetails = deployDetails;
    }

    public SensorContext getSensorContext() {
        return sensorContext;
    }

    public ISensor getSensor() {
        return sensor;
    }

    public SensorDeployDetails getDeployDetails() {
        return deployDetails;
    }
}
