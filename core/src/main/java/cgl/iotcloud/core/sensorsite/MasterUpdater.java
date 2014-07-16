package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.sensorsite.events.SensorEvent;
import cgl.iotcloud.core.utils.MasterClientCache;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterUpdater {
    private static Logger LOG = LoggerFactory.getLogger(MasterUpdater.class);

    private SiteContext siteContext;

    private MasterClientCache clientCache;

    public MasterUpdater(SiteContext context) {
        this.siteContext = context;
        this.clientCache = new MasterClientCache(siteContext);
    }

    @Subscribe
    public void updateMaster(SensorEvent event) {
        if (event.getState() == SensorState.DEPLOY) {
            registerSensor(event);
        } else if (event.getState() == SensorState.ACTIVATE) {
            activateSensor(event);
        } else if (event.getState() == SensorState.DEACTIVATE) {
            deActivateSensor(event);
        } else if (event.getState() == SensorState.UN_DEPLOY) {
            unRegisterSensor(event);
        }
    }

    private void registerSensor(SensorEvent event) {
        MasterClient client = null;
        try {
            client = clientCache.getMasterClient();

            client.registerSensor(siteContext.getSiteId(), siteContext.getSensorDescriptor(event.getSensorId()));
        } catch (Exception e) {
            LOG.error("Failed to register the sensor: " + event.getSensorId(), e);
        } finally {
            if (client != null) {
                clientCache.done(client);
            }
        }
    }

    private void unRegisterSensor(SensorEvent event) {
        MasterClient client = null;
        try {
            client = clientCache.getMasterClient();

            client.unRegisterSensor(siteContext.getSiteId(), siteContext.getSensorDescriptor(event.getSensorId()));
        } catch (Exception e) {
            LOG.error("Failed to un-register the sensor: " + event.getSensorId(), e);
        } finally {
            if (client != null) {
                clientCache.done(client);
            }
        }
    }

    private void activateSensor(SensorEvent event) {
        MasterClient client = null;
        try {
            client = clientCache.getMasterClient();

            client.updateSensor(siteContext.getSiteId(), null);
        } catch (Exception e) {
            LOG.error("Failed to activate the sensor: " + event.getSensorId(), e);
        } finally {
            if (client != null) {
                clientCache.done(client);
            }
        }
    }

    private void deActivateSensor(SensorEvent event) {
        MasterClient client = null;
        try {
            client = clientCache.getMasterClient();

            client.updateSensor(siteContext.getSiteId(), null);
        } catch (Exception e) {
            LOG.error("Failed to de-activate the sensor: " + event.getSensorId(), e);
        } finally {
            if (client != null) {
                clientCache.done(client);
            }
        }
    }
}
