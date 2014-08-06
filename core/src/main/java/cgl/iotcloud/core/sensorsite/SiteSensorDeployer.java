package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.sensorsite.events.SensorEvent;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.ChannelName;
import cgl.iotcloud.core.transport.Transport;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SiteSensorDeployer {
    private Logger LOG = LoggerFactory.getLogger(SiteSensorDeployer.class);

    private SiteContext siteContext;

    private Map conf;

    private EventBus siteEventBus;

    public SiteSensorDeployer(Map conf, SiteContext siteContext, EventBus eventBus) {
        this.conf = conf;
        this.siteContext = siteContext;
        this.siteEventBus = eventBus;
    }

    @Subscribe
    public void handlerSensorEvent(SensorEvent event) {
        try {
            if (event.getState() == SensorState.DEPLOY) {
                SensorDeployDescriptor deployDescriptor = event.getDeployDescriptor();
                deploySensor(deployDescriptor);
            } else if (event.getState() == SensorState.ACTIVATE) {
                SensorInstance descriptor = siteContext.getSensorDescriptor(event.getSensorName());
                if (descriptor != null) {
                    ISensor sensor = descriptor.getSensor();
                    sensor.activate();
                } else {
                    LOG.error("Trying to activate non-existing sensor: " + event.getSensorName());
                }
            } else if (event.getState() == SensorState.DEACTIVATE) {
                SensorInstance descriptor = siteContext.getSensorDescriptor(event.getSensorName());
                if (descriptor != null) {
                    ISensor sensor = descriptor.getSensor();
                    sensor.deactivate();
                } else {
                    LOG.error("Trying to de-activate non-existing sensor: " + event.getSensorName());
                }
            } else if (event.getState() == SensorState.UN_DEPLOY) {
                unDeploySensor(event);
            }
        } catch (Exception e) {
            LOG.error("Exception occurred in the worker listening for consumer changes", e);
        }
    }

    public void unDeploySensor(SensorEvent event) {
        SensorInstance descriptor = siteContext.getSensor(event.getSensorName());
        if (descriptor == null) {
            LOG.error("Trying to un-deploy non existing sensor {}", event.getSensorName());
            return;
        }

        LOG.info("Un-Deploying sensor {}", descriptor.getSensorContext().getName());

        ISensor sensor = descriptor.getSensor();
        sensor.close();

        Map<String, List<Channel>> channels = descriptor.getSensorContext().getChannels();
        for (Map.Entry<String, List<Channel>> entry : channels.entrySet()) {
            Transport t = siteContext.getTransport(entry.getKey());
            if (t != null) {
                for (Channel c : entry.getValue()) {
                    // register with the transport
                    c.close();
                    t.unRegisterChannel(new ChannelName(descriptor.getSensorContext().getName(), c.getName()), c);
                }
            }
        }

        SensorEvent sensorEvent = new SensorEvent(event.getSensorName(), SensorState.UN_DEPLOY);
        siteEventBus.post(sensorEvent);
    }

    public void deploySensor(SensorDeployDescriptor deployDescriptor) {
        try {
            LOG.info("Deploying sensor with jar: {} and class: {}", deployDescriptor.getJarName(), deployDescriptor.getClassName());

            String url = "file://";
            File file = new File(deployDescriptor.getJarName());
            if (!file.isAbsolute()) {
                String iotHome = Configuration.getIoTHome(conf);
                String repo = Configuration.getSensorRepositoryPath(conf);
                url += iotHome + "/" + repo + "/" + deployDescriptor.getJarName();
            } else {
                url += deployDescriptor.getJarName();
            }

            LOG.info("The sensor jar URL is {}", url);

            ISensor sensor = Utils.loadSensor(new URL(url),
                    deployDescriptor.getClassName(), this.getClass().getClassLoader());

            // generate a unique id for the sensor
            final String sensorID = UUID.randomUUID().toString().replaceAll("-", "");

            // get the sensor specific configurations
            Configurator configurator = sensor.getConfigurator(conf);
            Map<String, String> config = new HashMap<String, String>(deployDescriptor.getProperties());
            SensorContext sensorContext = configurator.configure(siteContext, config);

            // set the sensor id
            sensorContext.setSensorID(sensorID);

            // get the channels registered for this sensor
            Map<String, List<Channel>> channels = sensorContext.getChannels();

            for (Map.Entry<String, List<Channel>> entry : channels.entrySet()) {
                Transport t = siteContext.getTransport(entry.getKey());
                if (t != null) {
                    for (Channel c : entry.getValue()) {
                        // set the sensor id to channels
                        c.setSensorID(sensorID);
                        // register with the transport
                        t.registerChannel(new ChannelName(sensorContext.getName(), c.getName()), c);
                        c.open();
                    }
                }
            }

            // open the sensor
            sensor.open(sensorContext);

            // add the sensor to the site
            siteContext.addSensor(sensorContext, sensor);

            // notify the master about the sensor
            SensorEvent event = new SensorEvent(sensorContext.getName(), SensorState.DEPLOY);
            siteEventBus.post(event);
        } catch (MalformedURLException e) {
            String msg = "The jar name is not a correct url";
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
    }
}
