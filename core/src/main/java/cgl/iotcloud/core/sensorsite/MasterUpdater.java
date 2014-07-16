package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.sensor.SensorDescriptor;
import cgl.iotcloud.core.sensorsite.events.SensorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterUpdater {
    private static Logger LOG = LoggerFactory.getLogger(MasterUpdater.class);

    private SiteContext siteContext;

    public MasterUpdater(SiteContext context) {
        this.siteContext = context;
    }

    public void updateMaster(SensorEvent event) {
        if (event.getState() == SensorState.DEPLOY) {
            SensorDeployDescriptor deployDescriptor = event.getDeployDescriptor();
            deploySensor(deployDescriptor);
        } else if (event.getState() == SensorState.ACTIVATE) {
            SensorDescriptor descriptor = siteContext.getSensorDescriptor(event.getSensorId());
            if (descriptor != null) {
                ISensor sensor = descriptor.getSensor();
                sensor.activate();
            } else {
                LOG.error("Trying to activate non-existing sensor: " + event.getSensorId());
            }
        } else if (event.getState() == SensorState.DEACTIVATE) {
            SensorDescriptor descriptor = siteContext.getSensorDescriptor(event.getSensorId());
            if (descriptor != null) {
                ISensor sensor = descriptor.getSensor();
                sensor.deactivate();
            } else {
                LOG.error("Trying to de-activate non-existing sensor: " + event.getSensorId());
            }
        } else if (event.getState() == SensorState.UN_DEPLOY) {
            unDeploySensor(event);
        }
    }

    private void deploySensor(SensorDeployDescriptor deployDescriptor) {

    }

    private void unDeploySensor(SensorEvent event) {

    }
}
