package cgl.iotcloud.core.master;

import cgl.iotcloud.core.master.events.SensorDeployEvent;
import cgl.iotcloud.core.master.events.SensorEvent;
import cgl.iotcloud.core.master.events.SensorStartEvent;
import cgl.iotcloud.core.master.events.SensorStopEvent;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Subscribe to sensor events and act accordingly
 */
public class SensorController {
    private static Logger LOG = LoggerFactory.getLogger(SensorController.class);

    private SiteClientCache clientCache;

    private MasterContext context;

    public SensorController(SiteClientCache clientCache, MasterContext context) {
        this.clientCache = clientCache;
        this.context = context;
    }

    @Subscribe
    public void deploySensor(SensorDeployEvent deployEvent) {
        List<String> sites = getSites(deployEvent);

        for (String site : sites) {
            SiteClient client;
            try {
                client = clientCache.getSiteClient(site);
                if (client != null) {
                    client.deploySensor(deployEvent.getDeployDescriptor());
                }
            } catch (Exception e) {
                // we should report these kind of errors to some listening service or zookeeper in the future
                // there is nothing much we can do at this point except to log it
                LOG.error("Failed to deploy the sensor on the site {}", site);
            }
        }
    }

    private List<String> getSites(SensorEvent deployEvent) {
        List<String> sites = new ArrayList<String>(deployEvent.getSites());
        if (sites.isEmpty()) {
            sites.addAll(context.getSensorSites().keySet());
        }
        return sites;
    }

    @Subscribe
    public void stopSensor(SensorStopEvent stopEvent) {
        List<String> sites = getSites(stopEvent);

        for (String site : sites) {
            try {
                SiteClient client = clientCache.getSiteClient(site);
                if (client != null) {
                    client.stopSensor(stopEvent.getId());
                }
            } catch (Exception e) {
                // we should report these kind of errors to some listening service or zookeeper in the future
                // there is nothing much we can do at this point except to log it
                LOG.error("Failed to deploy the sensor on the site {}", site);
            }
        }
    }

    @Subscribe
    public void startSensor(SensorStartEvent startEvent) {
        List<String> sites = getSites(startEvent);

        for (String site : sites) {
            try {
                SiteClient client = clientCache.getSiteClient(site);
                if (client != null) {
                    client.startSensor(startEvent.getId());
                }
            } catch (Exception e) {
                // we should report these kind of errors to some listening service or zookeeper in the future
                // there is nothing much we can do at this point except to log it
                LOG.error("Failed to deploy the sensor on the site {}", site);
            }
        }
    }
}
