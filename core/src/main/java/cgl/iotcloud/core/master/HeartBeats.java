package cgl.iotcloud.core.master;

import cgl.iotcloud.core.master.events.MSiteEvent;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HeartBeats {
    private static Logger LOG = LoggerFactory.getLogger(HeartBeats.class);

    int retries = 1;

    private Map<String, Timer> hearBeatTasks = new HashMap<String, Timer>();

    private EventBus siteEvents;

    public HeartBeats(EventBus siteEvents) {
        this.siteEvents = siteEvents;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void scheduleForSite(String id, String host, int port) {
        LOG.info("Heart beats scheduled for site {}", id);
        HearBeatTask task = new HearBeatTask(id, host, port);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 500);
        hearBeatTasks.put(id, timer);
    }

    public void stopForSite(String id) {
        Timer timer = hearBeatTasks.get(id);
        if (timer != null) {
            timer.cancel();
        }
    }

    private class HearBeatTask extends TimerTask {
        String host;
        int port;
        String id;
        SiteState status = SiteState.ACTIVE;

        private HearBeatTask(String id, String host, int port) {
            this.host = host;
            this.port = port;
            this.id = id;
        }

        public void run() {
            boolean success = false;
            int tries = 0;
            while (!success && tries < retries) {
                SiteClient client = null;
                try {
                    client = new SiteClient(host, port);
                    boolean result = client.sendHearBeat();
                    if (result) success = true;
                } catch (Exception e) {
                    LOG.debug("Sensor site not reachable", e);
                    success = false;
                } finally {
                    if (client != null) {
                        client.close();
                    }
                }
                tries++;

                if (!success && tries >= retries && status != SiteState.DEACTIVATED) {
                    // remove the site and its sensors from the master context
                    MSiteEvent event = new MSiteEvent(id, SiteState.DEACTIVATED);
                    status = SiteState.DEACTIVATED;
                    siteEvents.post(event);

                    LOG.info("Deactivating the site with host: {} and port: {}", host, port);
                } else if (success && status == SiteState.DEACTIVATED) {
                    SiteEvent event = new SiteEvent(id, SiteEvent.State.ACTIVE);
                    status = SiteState.ACTIVE;
                    siteEvents.post(event);
                    LOG.info("Activating the site with host: {} and port: {}", host, port);
                }
            }
        }
    }
}
