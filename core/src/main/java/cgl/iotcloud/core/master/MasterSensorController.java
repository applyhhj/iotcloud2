package cgl.iotcloud.core.master;

import cgl.iotcloud.core.master.events.*;
import cgl.iotcloud.core.sensorsite.SensorState;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Subscribe to sensor events and act accordingly
 */
public class MasterSensorController {
    private static Logger LOG = LoggerFactory.getLogger(MasterSensorController.class);

    private SiteClientCache clientCache;

    private MasterContext context;

    public MasterSensorController(SiteClientCache clientCache, MasterContext context) {
        this.clientCache = clientCache;
        this.context = context;
    }

    @Subscribe
    public void handleClientEvents(MSensorClientEvent event) {
        if (event.getState() == SensorState.DEPLOY) {
            deploySensor(event);
        } else if (event.getState() == SensorState.ACTIVATE) {
            startSensor(event);
        } else if (event.getState() == SensorState.DEACTIVATE) {
            stopSensor(event);
        } else if (event.getState() == SensorState.UN_DEPLOY) {
            undeploySensor(event);
        } else {
            LOG.warn("Unrecognized event type {}", event.getState());
        }
    }

    private void deploySensor(MSensorClientEvent deployEvent) {
        List<String> sites = getSites(deployEvent);
        for (String site : sites) {
            SiteClient client;
            try {
                client = clientCache.getSiteClient(site);
                if (client != null) {
                    LOG.info("Requesting sensor site {} to deploy the sensor {} ", site, deployEvent.getDeployDescriptor());
                    client.deploySensor(deployEvent.getDeployDescriptor());
                } else {
                    LOG.error("Requesting the sensor {} to be deployed in no-exsisting site {}", deployEvent.getDeployDescriptor(), site);
                }
            } catch (Exception e) {
                // we should report these kind of errors to some listening service or zookeeper in the future
                // there is nothing much we can do at this point except to log it
                LOG.error("Failed to deploy the sensor on the site {}", site);
            }
        }
    }

    private List<String> getSites(MSensorClientEvent deployEvent) {
        List<String> sites = new ArrayList<String>(deployEvent.getSites());
        if (sites.isEmpty()) {
            sites.addAll(context.getSensorSites().keySet());
        }
        return sites;
    }

    private void stopSensor(MSensorClientEvent stopEvent) {
        List<String> sites = getSites(stopEvent);

        for (String site : sites) {
            try {
                LOG.info("Requesting sensor site {} to stop the sensor {} ", site, stopEvent.getId());
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

    private void startSensor(MSensorClientEvent startEvent) {
        List<String> sites = getSites(startEvent);

        for (String site : sites) {
            try {
                LOG.info("Requesting sensor site {} to start the sensor {} ", site, startEvent.getId());
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

    private void undeploySensor(MSensorClientEvent stopEvent) {
        List<String> sites = getSites(stopEvent);

        for (String site : sites) {
            try {
                LOG.info("Requesting sensor site {} to start the sensor {} ", site, stopEvent.getId());
                SiteClient client = clientCache.getSiteClient(site);
                if (client != null) {
                    client.unDeploySensor(stopEvent.getId());
                }
            } catch (Exception e) {
                // we should report these kind of errors to some listening service or zookeeper in the future
                // there is nothing much we can do at this point except to log it
                LOG.error("Failed to deploy the sensor on the site {}", site);
            }
        }
    }

    @Subscribe
    public void updateSensor(MSensorSiteEvent updateEvent) {
        if (updateEvent.getState() == SensorState.DEPLOY) {
            context.addSensor(updateEvent.getSite(), updateEvent.getSensorDetails());
        } else if (updateEvent.getState() == SensorState.UN_DEPLOY) {
            context.removeSensor(updateEvent.getSite(), updateEvent.getId());
        } else if (updateEvent.getState() == SensorState.ACTIVATE) {
            SensorDetails sensorDetails = context.getSensor(updateEvent.getSite(), updateEvent.getId());
            sensorDetails.setState(SensorState.ACTIVATE);
        } else if (updateEvent.getState() == SensorState.DEACTIVATE) {
            SensorDetails sensorDetails = context.getSensor(updateEvent.getSite(), updateEvent.getId());
            sensorDetails.setState(SensorState.DEACTIVATE);
        } else if (updateEvent.getState() == SensorState.UPDATE) {
            context.removeSensor(updateEvent.getSite(), updateEvent.getId());
            context.addSensor(updateEvent.getSite(), updateEvent.getSensorDetails());
        }
    }
}
