package cgl.sensorstream.core;

import cgl.sensorstream.core.config.Configuration;
import cgl.sensorstream.core.updates.UpdateListener;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SensorStreamMain {
    public static void main(String[] args) {
        // read the configuration

        Map conf = Utils.readDefaultConfig();

        BlockingQueue<Update> updates = new ArrayBlockingQueue<Update>((Integer) conf.get(Configuration.SS_SENSOR_UPDATES_SIZE));
        ZooKeeperUpdater zkUpdater = null;
        UpdateListener listener = null;
        try {
            // now try to connect to zookeeper
            zkUpdater = new ZooKeeperUpdater();
            zkUpdater.start(conf, updates);

            // now create the listener for updates
            listener = new UpdateListener(conf, new Updater(updates));
        } finally {
            if (zkUpdater != null) {
                zkUpdater.stop();
            }
            if (listener != null) {
                listener.destroy();
            }
        }
    }
}
