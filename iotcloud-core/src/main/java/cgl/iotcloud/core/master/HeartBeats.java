package cgl.iotcloud.core.master;

import java.util.Timer;
import java.util.TimerTask;

public class HeartBeats {
    private Timer timer;

    int retries = 1;

    public HeartBeats() {
        timer = new Timer();
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    private void scheduleForSite(String host, int port) {
        timer.schedule(new HearBeatTask(host, port), 0, 500);
    }

    private class HearBeatTask extends TimerTask {
        String host;
        int port;

        private HearBeatTask(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public void run() {
            SensorSiteClient client = new SensorSiteClient(host, port);

            boolean result = client.sendHearBeat();
            if (!result) {
                // remove the site and its sensors from the master context
                
            }
        }
    }
}
