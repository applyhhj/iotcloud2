package cgl.iotcloud.core.master;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.api.thrift.TSensorDeployDescriptor;
import cgl.iotcloud.core.master.events.*;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import cgl.iotcloud.core.sensorsite.SensorState;
import cgl.iotcloud.core.utils.SiteClientCache;
import cgl.iotcloud.core.zk.SensorUpdater;
import com.google.common.eventbus.Subscribe;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Subscribe to sensor events and act accordingly
 * Gets two types of events.
 * 1. Events from user clients, these events are propagated to sites
 * 2. Events from sites, results in updating the state locally and in zk
 */
public class ClientEventController {
    private static Logger LOG = LoggerFactory.getLogger(ClientEventController.class);

    private SiteClientCache clientCache;

    private MasterContext context;

    private CuratorFramework curatorFramework;

    public ClientEventController(SiteClientCache clientCache, MasterContext context) {
        this.clientCache = clientCache;
        this.context = context;

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorFramework = CuratorFrameworkFactory.newClient(Configuration.getZkConnectionString(context.getConf()), retryPolicy);
    }

    public void start() {
        this.curatorFramework.start();
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
        TSensorDeployDescriptor sensor = deployEvent.getSensorDeployDescriptor();
        SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor(sensor.getFilename(), sensor.getClassName());
        deployDescriptor.addDeploySites(deployEvent.getSites());

        if (sensor.getProperties() != null) {
            for (Map.Entry<String, String> e : sensor.getProperties().entrySet()) {
                deployDescriptor.addProperty(e.getKey(), e.getValue());
            }
        }

        List<String> sites = getSites(deployEvent);
        for (String site : sites) {
            SiteClient client;
            try {
                client = clientCache.getSiteClient(site);
                if (client != null) {
                    LOG.info("Requesting sensor site {} to deploy the sensor {} ", site, deployDescriptor);
                    client.deploySensor(deployDescriptor);
                } else {
                    LOG.error("Requesting the sensor {} to be deployed in no-exsisting site {}", deployDescriptor, site);
                }
            } catch (Exception e) {
                // we should report these kind of errors to some listening service or zookeeper in the future
                // there is nothing much we can do at this point except to log it
                LOG.error("Failed to deploy the sensor on the site {}", site, e);
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
}

