package cgl.iotcloud.core.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

public class HeartBeats {
    private static Logger LOG = LoggerFactory.getLogger(HeartBeats.class);

    int retries = 1;

    private BlockingQueue<SiteEvent> eventsQueue;

    private Map<String, Timer> hearBeatTasks = new HashMap<String, Timer>();

    public HeartBeats(BlockingQueue<SiteEvent> eventsQueue) {
        this.eventsQueue = eventsQueue;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void scheduleForSite(String id, String host, int port) {
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
        SiteEvent.State status = SiteEvent.State.ACTIVE;

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
                    LOG.debug("Sensor site not reachable",e);
                    success = false;
                } finally {
                    if (client != null) {
                        client.close();
                    }
                }
                tries++;

                if (!success && tries >= retries && status != SiteEvent.State.DEACTIVATED) {
                    // remove the site and its sensors from the master context
                    SiteEvent event = new SiteEvent(id, SiteEvent.State.DEACTIVATED);
                    status = SiteEvent.State.DEACTIVATED;
                    eventsQueue.add(event);

                    LOG.info("Deactivating the site with host: {} and port: {}", host, port);
                } else if (success && status == SiteEvent.State.DEACTIVATED) {
                    SiteEvent event = new SiteEvent(id, SiteEvent.State.ACTIVE);
                    status = SiteEvent.State.ACTIVE;
                    eventsQueue.add(event);
                    LOG.info("Activating the site with host: {} and port: {}", host, port);
                }
            }
        }
    }
}
