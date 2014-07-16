package cgl.iotcloud.core.local;

import cgl.iotcloud.core.master.SensorMaster;
import cgl.iotcloud.core.sensorsite.SensorSite;

/**
 * Run both Master and site in the same sever.
 */
public class LocalCluster {
    public static void main(String[] args) {
        final SensorMaster master = new SensorMaster();
        master.start();

        final SensorSite site = new SensorSite();
        site.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                site.stop();

                master.stop();
            }
        });
    }
}
