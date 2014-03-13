package cgl.iotcloud.core.master;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

public class HeartBeats {
    private Timer timer;

    int retries = 1;

    private BlockingQueue<SiteEvent> eventsQueue;

    public HeartBeats() {
        timer = new Timer();
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    private void scheduleForSite(String id, String host, int port) {
        timer.schedule(new HearBeatTask(id, host, port), 0, 500);
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
            SensorSiteClient client = new SensorSiteClient(host, port);

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
    }
}