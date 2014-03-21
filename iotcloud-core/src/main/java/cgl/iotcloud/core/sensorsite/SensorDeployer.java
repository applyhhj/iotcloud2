package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Transport;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class SensorDeployer {
    private Logger LOG = LoggerFactory.getLogger(SensorDeployer.class);

    private BlockingQueue<SensorEvent> events;

    private SiteContext siteContext;

    private Map conf;

    private MasterClient client;

    public SensorDeployer(Map conf, SiteContext siteContext, BlockingQueue<SensorEvent> events) {
        this.conf = conf;
        this.siteContext = siteContext;
        this.events = events;
    }

    public void start() {
        try {
            client = new MasterClient(Configuration.getMasterHost(conf), Configuration.getMasterServerPort(conf));
        } catch (TTransportException e) {
            throw new RuntimeException("Failed to create the connection to the master server", e);
        }

        Thread t = new Thread(new Worker());
        t.start();
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            boolean run = true;
            int errorCount = 0;
            while (run) {
                try {
                    try {
                        SensorEvent event = events.take();
                        if (event.getState() == SensorEventState.DEPLOY) {
                            SensorDeployDescriptor deployDescriptor = event.getDeployDescriptor();
                            deploySensor(conf, siteContext, deployDescriptor);
                        } else if (event.getState() == SensorEventState.ACTIVATE) {
                            SensorDescriptor descriptor = siteContext.getSensor(event.getSensorId());
                            if (descriptor != null) {
                                ISensor sensor = descriptor.getSensor();
                                sensor.activate();
                            }
                        } else if (event.getState() == SensorEventState.DEACTIVATE) {
                            SensorDescriptor descriptor = siteContext.getSensor(event.getSensorId());
                            if (descriptor != null) {
                                ISensor sensor = descriptor.getSensor();
                                sensor.deactivate();
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Exception occurred in the worker listening for consumer changes", e);
                    }
                } catch (Throwable t) {
                    errorCount++;
                    if (errorCount <= 3) {
                        LOG.error("Error occurred " + errorCount + " times.. trying to continue the worker");
                    } else {
                        LOG.error("Error occurred " + errorCount + " times.. terminating the worker");
                        run = false;
                    }
                }
            }
            String message = "Unexpected notification type";
            LOG.error(message);
            throw new RuntimeException(message);
        }
    }

    public void stop() {

    }

    public void deploySensor(Map conf, SiteContext siteContext, SensorDeployDescriptor deployDescriptor) {
        try {
            ISensor sensor = Utils.loadSensor(new URL(deployDescriptor.getJarName()),
                    deployDescriptor.getClassName(), this.getClass().getClassLoader());

            // get the sensor specific configurations
            Configurator configurator = sensor.getConfigurator(conf);
            SensorContext sensorContext = configurator.configure(siteContext);

            // add the sensor to the site
            siteContext.addSensor(sensorContext, sensor);

            // get the channels registered for this sensor
            Map<String, List<Channel>> channels = sensorContext.getChannels();
            for (Map.Entry<String, List<Channel>> entry : channels.entrySet()) {
                Transport t = siteContext.getTransport(entry.getKey());
                if (t != null) {
                    for (Channel c : entry.getValue()) {
                        t.registerChannel(c.getName(), c);
                    }
                }
            }

            // open the sensor
            sensor.open(sensorContext);

            // notify the master about the sensor
            client.registerSensor(siteContext.getSiteId(), siteContext.getSensor(sensorContext.getId()));
        } catch (MalformedURLException e) {
            String msg = "The jar name is not a correct url";
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        } catch (TException e) {
            String msg = "Failed to add the sensor to master";
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
    }
}
