package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.sensor.SensorDescriptor;
import cgl.iotcloud.core.sensorsite.events.SensorEvent;
import cgl.iotcloud.core.utils.MasterClientCache;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterUpdater {
    private static Logger LOG = LoggerFactory.getLogger(MasterUpdater.class);

    private SiteContext siteContext;

    public MasterUpdater(SiteContext context) {
        this.siteContext = context;
    }

    @Subscribe
    public void updateMaster(SensorEvent event) {
        if (event.getState() == SensorState.DEPLOY) {
            deploySensor(event);
        } else if (event.getState() == SensorState.ACTIVATE) {
            activateSensor(event);
        } else if (event.getState() == SensorState.DEACTIVATE) {
            deActivateSensor(event);
        } else if (event.getState() == SensorState.UN_DEPLOY) {
            unDeploySensor(event);
        }
    }

    private void deploySensor(SensorEvent event) {
//        MasterClient client = MasterClientCache.c
    }

    private void unDeploySensor(SensorEvent event) {

    }

    private void activateSensor(SensorEvent event) {

    }

    private void deActivateSensor(SensorEvent event) {

    }
}
