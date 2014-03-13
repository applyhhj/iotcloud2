package cgl.iotcloud.core.master;

import cgl.iotcloud.core.sensorsite.SensorEvent;
import cgl.iotcloud.core.sensorsite.SensorEventState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SiteManager {
    private static Logger LOG = LoggerFactory.getLogger(SiteManager.class);

    private MasterContext context;

    private BlockingQueue<SiteEvent> siteEventsQueue;

    private BlockingQueue<MasterSensorEvent> sensorEvents;

    private HeartBeats heartBeats;

    private boolean active = false;

    public SiteManager(MasterContext context) {
        this.context = context;

        // maximum number of sites
        siteEventsQueue = new ArrayBlockingQueue<SiteEvent>(1024);
    }

    public void start() {
        LOG.info("Starting the site monitor on master.");
        active = true;

        SiteEventListener listener = new SiteEventListener();
        Thread t = new Thread(listener);
        t.start();

        SiteSensorEventListener sensorEventListener = new SiteSensorEventListener();
        Thread t2 = new Thread(sensorEventListener);
        t.start();
    }

    public void stop() {
        active = false;
    }

    private class SiteEventListener implements Runnable {
        @Override
        public void run() {
            boolean run = true;
            int errorCount = 0;
            while (run && active) {
                try {
                    try {
                        SiteEvent event = siteEventsQueue.take();

                        if (event.getStatus() == SiteEvent.State.DEACTIVATED) {
                            // TODO we need to call a load balancer or something like that here
                            context.makeSiteOffline(event.getId());
                        }
                    } catch (InterruptedException e) {
                        LOG.error("Exception occurred in the worker listening for site changes", e);
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
            if (!run) {
                String message = "Unexpected notification type";
                LOG.error(message);
                throw new RuntimeException(message);
            } else {
                LOG.info("Stopping the site monitor...");
            }
        }
    }

    private class SiteSensorEventListener implements Runnable {
        @Override
        public void run() {
            boolean run = true;
            int errorCount = 0;
            while (run && active) {
                try {
                    try {
                        MasterSensorEvent event = sensorEvents.take();
                        if (event.getState() == SensorEventState.DEACTIVATE) {

                        } else if (event.getState() == SensorEventState.ACTIVATE) {

                        } else if (event.getState() == SensorEventState.DEPLOY) {

                        }
                    } catch (InterruptedException e) {
                        LOG.error("Exception occurred in the worker listening for site changes", e);
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
            if (!run) {
                String message = "Unexpected notification type";
                LOG.error(message);
                throw new RuntimeException(message);
            } else {
                LOG.info("Stopping the site monitor...");
            }
        }
    }

    private void deploySensors(MasterSensorEvent event) {

    }

    private void startSensors(MasterSensorEvent event) {

    }

    private void stopSensors(MasterSensorEvent event) {

    }
}
