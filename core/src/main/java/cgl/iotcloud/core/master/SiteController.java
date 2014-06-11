package cgl.iotcloud.core.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class SiteController {
    private static Logger LOG = LoggerFactory.getLogger(SiteController.class);

    private final MasterContext context;

    private BlockingQueue<SiteEvent> siteEventsQueue;

    private HeartBeats heartBeats;

    private boolean active = false;

    // private Map<String, SiteClient> siteClients = new HashMap<String, SiteClient>();

    public SiteController(MasterContext context, BlockingQueue<SiteEvent> siteEventsQueue) {
        this.context = context;
        this.siteEventsQueue = siteEventsQueue;
    }

    public void start() {
        LOG.info("Starting the site monitor on master.");

        heartBeats = new HeartBeats(siteEventsQueue);
        active = true;

        SiteEventListener listener = new SiteEventListener();
        Thread t = new Thread(listener);
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
                            LOG.info("Deactivating the site {}", event.getSiteId());
                            // TODO we need to call a load balancer or something like that here
                            context.makeSiteOffline(event.getSiteId());
                            // stop the timers
                            heartBeats.stopForSite(event.getSiteId());
                        } else if (event.getStatus() == SiteEvent.State.ACTIVE) {
                            LOG.info("Activating the site {}", event.getSiteId());

                            SiteDescriptor descriptor = context.getSensorSite(event.getSiteId());
                            heartBeats.scheduleForSite(event.getSiteId(), descriptor.getHost(), descriptor.getPort());
                        } else if (event.getStatus() == SiteEvent.State.ADDED) {
                            SiteDescriptor descriptor = context.getSensorSite(event.getSiteId());
                            LOG.info("A new site added {} with host {} and port {}", event.getSiteId(), descriptor.getHost(), descriptor.getPort());


                            heartBeats.scheduleForSite(event.getSiteId(), descriptor.getHost(), descriptor.getPort());
                        }
                    } catch (InterruptedException e) {
                        LOG.error("Exception occurred in the worker listening for site changes", e);
                    }
                } catch (Throwable t) {
                    errorCount++;
                    if (errorCount <= 3) {
                        LOG.error("Error occurred " + errorCount + " times.. trying to continue the worker", t);
                    } else {
                        LOG.error("Error occurred " + errorCount + " times.. terminating the worker", t);
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
}
