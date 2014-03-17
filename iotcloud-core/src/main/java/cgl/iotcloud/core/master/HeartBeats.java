package cgl.iotcloud.core.master;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

public class HeartBeats {
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
            SiteClient client = new SiteClient(host, port);

            boolean result = client.sendHearBeat();
            if (!result && status != SiteEvent.State.DEACTIVATED) {
                // remove the site and its sensors from the master context
                SiteEvent event = new SiteEvent(id, SiteEvent.State.DEACTIVATED);
                status = SiteEvent.State.DEACTIVATED;
                eventsQueue.add(event);
            } else if (result && status == SiteEvent.State.DEACTIVATED) {
                SiteEvent event = new SiteEvent(id, SiteEvent.State.ACTIVE);
                status = SiteEvent.State.ACTIVE;
                eventsQueue.add(event);
            }
        }

        public void setStatus(SiteEvent.State status) {
            this.status = status;
        }
    }
}
