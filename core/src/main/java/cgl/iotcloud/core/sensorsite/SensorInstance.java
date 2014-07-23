package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;

import java.io.Serializable;

/**
 * Contains static information about the sensor. This information can be
 * saved in persistant store.
 */
public class SensorInstance implements Serializable {
    private SensorContext sensorContext;

    private ISensor sensor;

    private SensorDeployDescriptor deployDescriptor;

    public SensorInstance(SensorContext sensorContext, ISensor sensor) {
        this.sensorContext = sensorContext;
        this.sensor = sensor;
    }

    public void setSensorContext(SensorContext sensorContext) {
        this.sensorContext = sensorContext;
    }

    public void setSensor(ISensor sensor) {
        this.sensor = sensor;
    }

    public void setDeployDescriptor(SensorDeployDescriptor deployDescriptor) {
        this.deployDescriptor = deployDescriptor;
    }

    public SensorContext getSensorContext() {
        return sensorContext;
    }

    public ISensor getSensor() {
        return sensor;
    }

    public SensorDeployDescriptor getDeployDescriptor() {
        return deployDescriptor;
    }
}
